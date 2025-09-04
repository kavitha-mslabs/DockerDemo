package tneb.ccms.admin.controller;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.primefaces.PrimeFaces;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.jsf2leaf.model.LatLong;
import com.jsf2leaf.model.Layer;
import com.jsf2leaf.model.Map;
import com.jsf2leaf.model.Marker;
import com.jsf2leaf.model.Pulse;

import tneb.ccms.admin.AdminMain;
import tneb.ccms.admin.dao.CallCenterDao;
import tneb.ccms.admin.dao.ComplaintsDao;
import tneb.ccms.admin.dao.GeneralDao;
import tneb.ccms.admin.dao.OutageService;
import tneb.ccms.admin.dao.UserDao;
import tneb.ccms.admin.model.AdminUserBean;
import tneb.ccms.admin.model.CallCenterUserHistoryBean;
import tneb.ccms.admin.model.CategoryBean;
import tneb.ccms.admin.model.CircleBean;
import tneb.ccms.admin.model.CompContactMap;
import tneb.ccms.admin.model.CompTransferBean;
import tneb.ccms.admin.model.ComplaintBean;
import tneb.ccms.admin.model.ComplaintHistoryBean;
import tneb.ccms.admin.model.DTMasterGISBean;
import tneb.ccms.admin.model.DivisionBean;
import tneb.ccms.admin.model.FieldWorkerComplaintBean;
import tneb.ccms.admin.model.LoginParams;
import tneb.ccms.admin.model.MinnagamHistoryBean;
import tneb.ccms.admin.model.MinnagamUserRequestBean;
import tneb.ccms.admin.model.OutagesBean;
import tneb.ccms.admin.model.RegionBean;
import tneb.ccms.admin.model.RoleBean;
import tneb.ccms.admin.model.SchOutageBean;
import tneb.ccms.admin.model.SectionBean;
import tneb.ccms.admin.model.SubCategoryBean;
import tneb.ccms.admin.model.SubDivisionBean;
import tneb.ccms.admin.util.CCMSConstants;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.DataModel.ComplaintLog;
import tneb.ccms.admin.util.GeneralUtil;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.util.SMSUtil;
import tneb.ccms.admin.util.SmsClient;
import tneb.ccms.admin.valuebeans.AdminUserValueBean;
import tneb.ccms.admin.valuebeans.CallCenterUserValueBean;
import tneb.ccms.admin.valuebeans.CircleValueBean;
import tneb.ccms.admin.valuebeans.ClosureReasonValueBean;
import tneb.ccms.admin.valuebeans.CompContactMapValueBean;
import tneb.ccms.admin.valuebeans.ComplaintValueBean;
import tneb.ccms.admin.valuebeans.ConsumerServiceValueBean;
import tneb.ccms.admin.valuebeans.DTMasterGISValueBean;
import tneb.ccms.admin.valuebeans.DivisionValueBean;
import tneb.ccms.admin.valuebeans.OutageDetailsValueBean;
import tneb.ccms.admin.valuebeans.OutagesValueBean;
import tneb.ccms.admin.valuebeans.RegionValueBean;
import tneb.ccms.admin.valuebeans.RoleValueBean;
import tneb.ccms.admin.valuebeans.SectionValueBean;
import tneb.ccms.admin.valuebeans.SessionAdminValueBean;
import tneb.ccms.admin.valuebeans.SubCategoriesValueBean;
import tneb.ccms.admin.valuebeans.SubDivisionValueBean;
import tneb.ccms.admin.valuebeans.ViewComplaintReportValueBean;
import tneb.ccms.admin.valuebeans.ViewComplaintValueBean;

public class Dashboard {
	
	private Logger logger = LoggerFactory.getLogger(Dashboard.class.getName());
	
	@ManagedProperty("#{admin}")
	AdminMain admin;

	List<ViewComplaintValueBean> complaintList;
	List<ViewComplaintValueBean> viewComplaintList;
	
	ComplaintLazyDataModel lazyComplaintDataModel;
	List<ViewComplaintReportValueBean> viewComplaintReportList;
	
	List<ComplaintValueBean> complaintValueBean;
	
	ComplaintValueBean selectedComplaintId;
	CompContactMapValueBean selectedComplaintMobileNumber;
	
	ConsumerServiceValueBean consumerServiceValueBean = new ConsumerServiceValueBean();
	ViewComplaintValueBean complaint = new ViewComplaintValueBean();
	OutagesValueBean outagesValueBean = new OutagesValueBean();
	
	LoginParams officer;
	String description;
	String descriptionTransfer;
	String descriptionComplete;
	int sectionId;
	int regionId;
	DataModel dm = new DataModel();
	DataModel dmFilter = new DataModel();
	DataModel dmCurrentData = new DataModel();
	DataModel dmTotalData = new DataModel();
	Map map = new Map();
	List<AdminUserValueBean> userList;
	boolean regionDropDownDisabled = false;
	boolean circleDropDownDisabled = false;
	boolean divisionDropDownDisabled = false;
	boolean subDiviviondropDownDisabled = false;
	int complaintCode;
	int statusId;
	private boolean receivedFromEnabled;
	
	private String complaintNumber;
	private String consumerMobileNumber;
	
	private Date fromDate;
	private Date toDate;
	private Date currentDate = new Date();
	private Date minDate;
	private Date maxDate;
	private String fromTime;
	private String toTime;
	private Date combinedFromDateTime;
	private Date combinedToDateTime;
	
	private String fromTimeString;  // this replaces Date fromTime
	
	
	private String historyComplaintsUpTo;
	private Long totalComplaints;
	
		public String getHistoryComplaintsUpTo() {
		return historyComplaintsUpTo;
	}

	public void setHistoryComplaintsUpTo(String historyComplaintsUpTo) {
		this.historyComplaintsUpTo = historyComplaintsUpTo;
	}

	public Long getTotalComplaints() {
		return totalComplaints;
	}

	public void setTotalComplaints(Long totalComplaints) {
		this.totalComplaints = totalComplaints;
	}

	public String getFromTimeString() {
	    return fromTimeString;
	}

	public void setFromTimeString(String fromTimeString) {
	    this.fromTimeString = fromTimeString;
	}
	private Date minTime;
	
	public Date getMinTime() {
		return minTime;
	}

	public void setMinTime(Date minTime) {
		this.minTime = minTime;
	}

	public String getFromTime() {
		return fromTime;
	}

	public void setFromTime(String fromTime) {
		this.fromTime = fromTime;
	}

	public String getToTime() {
		return toTime;
	}

	public void setToTime(String toTime) {
		this.toTime = toTime;
	}

	public Date getCombinedFromDateTime() {
		return combinedFromDateTime;
	}

	public void setCombinedFromDateTime(Date combinedFromDateTime) {
		this.combinedFromDateTime = combinedFromDateTime;
	}

	public Date getCombinedToDateTime() {
		return combinedToDateTime;
	}

	public void setCombinedToDateTime(Date combinedToDateTime) {
		this.combinedToDateTime = combinedToDateTime;
	}

	private String circleName;
	private String sectionName;
	private String ssName;
	private String fdrName;
	private String[] dtName;
	private Long circleId;
	
	public Long getCircleId() {
		return circleId;
	}

	public void setCircleId(Long circleId) {
		this.circleId = circleId;
	}

	private String type;
	private String remarks;
	
	 private String previewExcelContent;
	 
	 Date date = new Date();
	 
	 SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy E <HH:mm>");
     String formattedDate = sdf.format(date);
	
     SimpleDateFormat sdfs = new SimpleDateFormat("dd-MM-yyyy");
     String formattedDates = sdfs.format(date);
     
	public Dashboard() {
		super();
		init();
		
	}
    public String getFormattedDates() {
        SimpleDateFormat sdfs = new SimpleDateFormat("dd-MM-yyyy");
        return sdfs.format(date);
    }

	public void updateToDateRange() {
	    if (fromDate != null) {
	        // Set toDate to be exactly 2 days after fromDate
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(fromDate);
	        cal.add(Calendar.DAY_OF_MONTH, 2);
	        toDate = cal.getTime();
	    }
	}
	
//	public boolean crossCheck(Timestamp fromTime, Timestamp toTime) {
//	    SessionFactory factory = null;
//	    Session session = null;
//
//	    boolean flag = true;
//
//	    try {
//	        
//	        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
//	        java.util.Date currentDate = new java.util.Date(fromTime.getTime());
//	        java.util.Date toTme = new java.util.Date(toTime.getTime());
//
//	        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");
//	        String formattedDate = dateFormat.format(currentDate); 
//	        
//	       
//	        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
//	        String formattedTime = timeFormat.format(currentDate); 
//	        String formattedToTime = timeFormat.format(toTme);
//	        
//	        Calendar calendar = Calendar.getInstance();
//	        calendar.setTime(currentDate);
//	        calendar.set(Calendar.HOUR_OF_DAY, 0);
//	        calendar.set(Calendar.MINUTE, 0);
//	        calendar.set(Calendar.SECOND, 0);
//	        calendar.set(Calendar.MILLISECOND, 0);
//
//	        Date currentDateOnly = calendar.getTime(); 
//
//	        Date fromDate = new Date(fromTime.getYear(), fromTime.getMonth(), fromTime.getDate());
//	        String formattedFromDate = dateFormat.format(fromDate); 
//
//	        String fromTimeStr = timeFormat.format(fromTime);  
//	        String toTimeStr = timeFormat.format(toTime);     
//
//	        factory = HibernateUtil.getSessionFactory();
//	        session = factory.openSession();
//
//	        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
//	        CriteriaQuery<SchOutageBean> criteriaQuery = criteriaBuilder.createQuery(SchOutageBean.class);
//	        Root<SchOutageBean> root = criteriaQuery.from(SchOutageBean.class);
//
//	        criteriaQuery.select(root)
//	            .where(criteriaBuilder.equal(root.get("sscode"), ssName),
//	                   criteriaBuilder.equal(root.get("tripDate"), currentDateOnly));
//
//	        System.out.println("SSCODE CHECKING :::::::::::::: " + ssName);
//
//	        Query<SchOutageBean> query = session.createQuery(criteriaQuery);
//	        List<SchOutageBean> dtList = query.getResultList();
//	        System.out.println("LIST :::::::::::: " + dtList);
//
//	        for (SchOutageBean outage : dtList) {
//	            String tripFromTime = outage.getTripFromTime();
//	            String tripToTime = outage.getTripToTime();
//
//	            Date tripFrom = timeFormat.parse(tripFromTime); 
//	            Date tripTo = timeFormat.parse(tripToTime);    
//	            Date currentTime = timeFormat.parse(formattedTime);  
//	            Date currentToTime = timeFormat.parse(formattedToTime);
//	            
//	            if (currentTime.after(tripFrom) && currentToTime.before(tripTo)) {
//	                flag = false; 
//	            }
//	        }
//	    } catch (Exception e) {
//	        logger.error(ExceptionUtils.getStackTrace(e));
//	        FacesContext.getCurrentInstance().addMessage(null, 
//	            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error retrieving circle ID", CCMSConstants.TECH_ERROR));
//	    } finally {
//	        HibernateUtil.closeSession(factory, session);
//	    }
//
//	    return flag;  
//	}
	
	
	public boolean crossCheck(Timestamp fromTime, Timestamp toTime) {
	    SessionFactory factory = null;
	    Session session = null;
	    boolean hasOverlap = false; // Default to no overlap

	    try {
	        // Format times only (ignore dates since we're comparing times within the same day)
	        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
	        String userFromTime = timeFormat.format(fromTime);  
	        String userToTime = timeFormat.format(toTime);
	        
	        Date userFrom = timeFormat.parse(userFromTime);
	        Date userTo = timeFormat.parse(userToTime);

	        // Get current date in SQL format for DB query
	        LocalDate currentDate = LocalDate.now();
	        java.sql.Date sqlDate = java.sql.Date.valueOf(currentDate);

	        factory = HibernateUtil.getSessionFactory();
	        session = factory.openSession();
	        
	        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
	        CriteriaQuery<SchOutageBean> criteriaQuery = criteriaBuilder.createQuery(SchOutageBean.class);
	        Root<SchOutageBean> root = criteriaQuery.from(SchOutageBean.class);

	        criteriaQuery.select(root)
	            .where(criteriaBuilder.equal(root.get("ssfCode"), fdrName),
	                   criteriaBuilder.equal(root.get("tripDate"), sqlDate));

	        List<SchOutageBean> outages = session.createQuery(criteriaQuery).getResultList();

	        for (SchOutageBean outage : outages) {
	            String tripFromTime = outage.getTripFromTime();
	            String tripToTime = outage.getTripToTime();

	            Date tripFrom = timeFormat.parse(tripFromTime); 
	            Date tripTo = timeFormat.parse(tripToTime);
	            
	            // Check for overlap - if any overlap found, set hasOverlap to true
	            if (!(userTo.before(tripFrom) || userFrom.after(tripTo))) {
	                hasOverlap = true;
	                System.out.println("Time range overlaps with scheduled outage (" + 
	                    tripFromTime + " to " + tripToTime + ")");
	                break;
	            }
	        }
	    } catch (Exception e) {
	        logger.error(ExceptionUtils.getStackTrace(e));
	        FacesContext.getCurrentInstance().addMessage(null, 
	            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error checking schedule", "Technical error occurred"));
	    } finally {
	        HibernateUtil.closeSession(factory, session);
	    }

	    return !hasOverlap; // Return true if NO overlap found
	}
	
	public String findSSCodeFdrCodeDtCode() {
		
		SessionFactory factory = null;
		Session session = null;
		
		String allCode = null;
		
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<DTMasterGISBean> criteriaQuery = criteriaBuilder.createQuery(DTMasterGISBean.class);
			Root<DTMasterGISBean> root = criteriaQuery.from(DTMasterGISBean.class);
			
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("ssCode"), ssName));
			
			Query<DTMasterGISBean> query = session.createQuery(criteriaQuery);
			List<DTMasterGISBean> dtList = query.getResultList();
			
			List<DTMasterGISValueBean> dtValueBeanList = dtList.stream().map(
					dtValue -> DTMasterGISValueBean.convertBeanToValueBean(dtValue))
					.collect(Collectors.toList());
			
			DTMasterGISValueBean codes = dtValueBeanList.get(0);
			
			String ssCode = codes.getSsCode();
			String fdrCode = codes.getFdrCode();
			String dtCode = codes.getDtCode();
			
			allCode = ssCode+fdrCode+dtCode;
			
			return dtCode;
			
		} catch (Exception e) {
			 logger.error(ExceptionUtils.getStackTrace(e));
		        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error retrieving circle ID", CCMSConstants.TECH_ERROR));
		    } finally {
		        HibernateUtil.closeSession(factory, session);
		    }	
		
		return allCode;
	}
	

	public void combineDateTime() {
	    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	    if (fromDate != null && fromTime != null) {
	        Date parsedTime = parseFlexibleTime(fromTime);
	        if (parsedTime != null) {
	            Calendar dateCal = Calendar.getInstance();
	            dateCal.setTime(fromDate);

	            Calendar timeCal = Calendar.getInstance();
	            timeCal.setTime(parsedTime);

	            dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
	            dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));

	            this.combinedFromDateTime = dateCal.getTime();
	            System.out.println("Combined From: " + sdf.format(combinedFromDateTime));
	        }
	    }

	    if (toDate != null && toTime != null) {
	        Date parsedTime = parseFlexibleTime(toTime);
	        if (parsedTime != null) {
	            Calendar dateCal = Calendar.getInstance();
	            dateCal.setTime(toDate);

	            Calendar timeCal = Calendar.getInstance();
	            timeCal.setTime(parsedTime);

	            dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
	            dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));

	            this.combinedToDateTime = dateCal.getTime();
	            System.out.println("Combined To: " + sdf.format(combinedToDateTime));
	        }
	    }
	}
	
	public void loadSubCategory() {

		SessionFactory factory = null;
		Session session = null;
		try {

			if(StringUtils.isNotBlank(dmFilter.getCompOneType())) {

				Integer compCode = Integer.parseInt(dmFilter.getCompOneType());
				CategoryBean categoryBean = null;
				if(compCode == 0) {
					GeneralDao dao = new GeneralDao();
					categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_POWER_FAILURE);
				} else if(compCode == 1) {
					GeneralDao dao = new GeneralDao();
					categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_VOLTAGE_RELATED);
				} else if(compCode == 2) {
					GeneralDao dao = new GeneralDao();
					categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_METER_RELATED);
				} else if(compCode == 3) {
					GeneralDao dao = new GeneralDao();
					categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_BILLING);
				} else if(compCode == 4) {
					GeneralDao dao = new GeneralDao();
					categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_FIRE);
				} else if(compCode == 5) {
					GeneralDao dao = new GeneralDao();
					categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_POSTING_DANGER);
				} else if(compCode == 6) {
					GeneralDao dao = new GeneralDao();
					categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_THEFT_OF_POWER);
				} else if(compCode == 7) {
					GeneralDao dao = new GeneralDao();
					categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_APPLICATION_RELATED);
				} else if(compCode == 8) {
					GeneralDao dao = new GeneralDao();
					categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_CONDUCTOR_SNAPPING);
				} 

				factory = HibernateUtil.getSessionFactory();
				session = factory.openSession();

				List<SubCategoriesValueBean> subCategoriesList = new ArrayList<SubCategoriesValueBean>();

				CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<SubCategoryBean> criteriaQuery = criteriaBuilder.createQuery(SubCategoryBean.class);
				Root<SubCategoryBean> root = criteriaQuery.from(SubCategoryBean.class);

				criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("categoryBean"), categoryBean))
				.orderBy(criteriaBuilder.asc(criteriaBuilder.upper(root.get("name"))));

				Query<SubCategoryBean> query = session.createQuery(criteriaQuery);
				List<SubCategoryBean> list = query.getResultList();

				for(SubCategoryBean subCategoryBean : list) {
					subCategoriesList.add(SubCategoriesValueBean.convertSubCategoriesBeanToSubCategoriesValueBean(subCategoryBean));
				}

				dmFilter.setListSubCategory(subCategoriesList);
			} else {
				if (dmFilter.getListSubCategory() != null) {
					dmFilter.getListSubCategory().clear();
				}
			}

			if (dmFilter.getSubCategory() != null) {
				dmFilter.setSubCategory(null);
			}

		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
	}

	private Date parseFlexibleTime(String timeStr) {
	    try {
	        if (timeStr == null || timeStr.trim().isEmpty()) return null;
	        if (!timeStr.contains(":")) {
	            timeStr = String.format("%02d:00", Integer.parseInt(timeStr));
	        }
	        return new SimpleDateFormat("HH:mm").parse(timeStr);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}

	/*  multiple DT NAMES SAVED*/
	public void outagesSchedule() {
		
		 startScheduledUpdates();
		 combineDateTime();
		 Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
		 
	
  		Timestamp timestampFromDate = new Timestamp(this.combinedFromDateTime.getTime());
  		Timestamp timestampToDate = new Timestamp(this.combinedToDateTime.getTime());
          
	       
	        for (String dt : dtName) {
	        	
	        	 OutagesBean outagesBean = new OutagesBean();
	        	 GeneralDao generalDao = new GeneralDao();
	        	 
	        	
	  	        if(crossCheck(timestampFromDate, timestampToDate)) {
	  	        	
	  	        	System.err.println("FLAG TRUE VALID");
	 		        String ipAddress = null;
			        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest(); 
			        ipAddress = request.getHeader("X-FORWARDED-FOR");
			        if (ipAddress == null) {
			            ipAddress = request.getRemoteAddr(); 
			        }
			        
			        outagesBean.setIpid(ipAddress);
			        outagesBean.setFrprd(timestampFromDate);
	 		        outagesBean.setToprd(timestampToDate);
	 		        outagesBean.setSchType(type);
	 		        outagesBean.setDesc(remarks);
	 		        outagesBean.setEntryUser(officer.getUserName());
			        outagesBean.setEntryDt(currentTimestamp);
			        outagesBean.setStatus("O");
			        
			        if(officer.roleId == 1) {
				        outagesBean.setCircleId(getCircleIdFromName(circleName));
				        
				        outagesBean.setSectionCode(getSectionIdFromName(sectionName));
				        
			        }else {
			        	
			        	 String sectionCodes = getSectionName();
			             String circleCodes = getCircleName();
			             
			             String[] parts = circleCodes.split("\\|");
			 		    String circleId = parts[0];    // e.g., "123"
//			 		    String circleCode = parts[1];  // e.g., "CIRCLE_A"
			 		    
			 		    String[] partss = sectionCodes.split("\\|");
			 		    String sectionId = partss[0];    // e.g., "123"
//			 		    String sectionCode = partss[1];  // e.g., "CIRCLE_A"
			 		  
			        	 outagesBean.setCircleId(Integer.parseInt(circleId));
					        
					     outagesBean.setSectionCode(Integer.parseInt(sectionId));
			        }
			      
			        outagesBean.setSsfdrstrcode(dt);
			        OutagesValueBean been = new OutagesValueBean();
			        boolean exists = generalDao.checkOutageExists("O",dt,timestampFromDate,timestampToDate);
		        if (exists) {
//			        	
		        	PrimeFaces.current().executeScript("PF('messageDialogalreadyexists').show(); setTimeout(function() { PF('messageDialogalreadyexists').hide(); }, 5000);");
		    		
//		        	PrimeFaces.current().executeScript("setTimeout(function(){ window.location.href = '" +
//						    FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() +
//						    "/faces/admin/schedule.xhtml'; }, 5000);");
		        

		        	clear();
		        	
		            continue; 
			        }
		        
			        generalDao.saveOutages(outagesBean);
			        
			        
			       PrimeFaces.current().executeScript("PF('messageDialog').show(); setTimeout(function() { PF('messageDialog').hide(); }, 5000);");
		
					
					PrimeFaces.current().executeScript("setTimeout(function(){ window.location.href = '" +
						    FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() +
						    "/faces/admin/schedule.xhtml'; }, 5000);");
			        
	  	        }
	  	        else
	  	        {
	        	PrimeFaces.current().executeScript("PF('messageDialogAlreadyEntered').show(); setTimeout(function() { PF('messageDialogAlreadyEntered').hide(); }, 5000);");
////	  	  		
//	  	        	 FacesContext.getCurrentInstance().addMessage(null,
//	 		                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Outage Entries Already available for the selected SS/Feeder and the Duration"));
//	 			        
//		        	 PrimeFaces.current().executeScript("setTimeout(function(){ window.location.href = '" +
//							    FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() +
//							    "/faces/admin/schedule.xhtml'; }, 5000);");
		        	 
	         
				            return; 
	  	        }
	  	          
	        }
    
	}
	
	public void clear()
	{
		System.err.println("CLEAR::::::::::::");

		this.setToTime(null);
		this.setFromTime(null);
	
	}
	
	public Integer getCircleIdFromName(String circleName) {
	    SessionFactory factory = null;
	    Session session = null;
	    Integer circleId = null; 
	    
	    try {
	        factory = HibernateUtil.getSessionFactory();
	        session = factory.openSession();
	        
	        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
	        CriteriaQuery<CircleBean> criteriaQuery = criteriaBuilder.createQuery(CircleBean.class);
	        
	        Root<CircleBean> root = criteriaQuery.from(CircleBean.class);
	       
	        criteriaQuery.select(root)
	                      .where(criteriaBuilder.equal(root.get("name"), circleName));
	        
	        CircleBean circleBean = session.createQuery(criteriaQuery).uniqueResult(); 
	        
	        if (circleBean != null) {
	            circleId = circleBean.getId(); 
	        }
	    } catch (Exception e) {
	        logger.error(ExceptionUtils.getStackTrace(e));
	        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error retrieving circle ID", CCMSConstants.TECH_ERROR));
	    } finally {
	        HibernateUtil.closeSession(factory, session);
	    }
	    
	    return circleId; 
	}
	
	public Integer getSectionIdFromName(String sectionName) {
	    SessionFactory factory = null;
	    Session session = null;
	    Integer sectionId = null; 
	    String sectionCode = null;
	    try {
	        factory = HibernateUtil.getSessionFactory();
	        session = factory.openSession();
	        
	        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
	        CriteriaQuery<SectionBean> criteriaQuery = criteriaBuilder.createQuery(SectionBean.class);
	        
	        Root<SectionBean> root = criteriaQuery.from(SectionBean.class);
	        
	        criteriaQuery.select(root)
	                      .where(criteriaBuilder.equal(root.get("name"), sectionName));
	        
	        SectionBean sectionBean = session.createQuery(criteriaQuery).uniqueResult();
	        
	        if (sectionBean != null) {
	        	sectionCode = sectionBean.getCode(); 
	        	sectionId = sectionBean.getId();
	        }
	    } catch (Exception e) {
	        logger.error(ExceptionUtils.getStackTrace(e));
	        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error retrieving section ID", CCMSConstants.TECH_ERROR));
	    } finally {
	        HibernateUtil.closeSession(factory, session);
	    }
	    
	    return sectionId; 
	}
	
	public void toggleReceivedFrom() {
		receivedFromEnabled = !dmFilter.getPlatForm().isEmpty();
	}

	public void init() {
		//feebackCallApis();
				if (complaintList == null) {
					
					admin = (AdminMain) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("admin");
					officer = admin.getAuth().getOfficer();
					
					FacesContext facesContext = FacesContext.getCurrentInstance();
					ExternalContext externalContext = facesContext.getExternalContext();
					HttpSession session1 = (HttpSession) externalContext.getSession(false); // true = create if needed
					
					
			ComplaintsDao daoComplaints = new ComplaintsDao();
			dm.arrDash = daoComplaints.getDashboardData(this.officer);
			if (dm.arrDash[0][0] == -1) {
				dm.arrDash[0][0] = 0;
			}
			
			if(this.officer.getRoleId() == 9) {
				setLstRoles();
			}
			
			if(this.officer.getRoleId() == 10) {
				loadCirclesMinnagam();
				
				CallCenterDao callCenterDao = new CallCenterDao();
				
				dmTotalData = callCenterDao.getMinnagamComplaintCount(this.officer);
			    dmCurrentData = callCenterDao.getMinnagamCurrentComplaintData(this.officer);
			}
			
			if(this.officer.getRoleId() == 1) {
				ssNameBySection();
			}
			
			setLstRegions();
				
			if(this.officer.getRoleId() < 6) {
				regionDropDownDisabled = true;
			}
			if(this.officer.getRoleId() < 5) {
				circleDropDownDisabled = true;
			}
			if(this.officer.getRoleId() < 4) {
				divisionDropDownDisabled = true;
			}
			if(this.officer.getRoleId() < 3) {
				subDiviviondropDownDisabled = true;
			}
			
			
			
		}
		
		 loadSessionData();
		 startScheduledUpdates();
		 
		 loadMinnagamHistoryComplaints();
		 
		  Calendar calendar = Calendar.getInstance();
	        calendar.setTime(currentDate);
	        calendar.add(Calendar.DATE, 0);  

	        this.maxDate=calendar.getTime();
	       
	        this.minDate = currentDate;  
	        Date now = new Date();
	        fromDate = new Date();
	        toDate = new Date();
	        
	        // Set fromTime and minTime to now
//	        this.fromTime = calendar.getTime();
//	        this.minTime = calendar.getTime();
	        
	     lazyComplaintDataModel = new ComplaintLazyDataModel(dmFilter,-1,-1);
				
	}
	
//	  public void updateToTime() {
//	    	
//	    	
//	        if (fromTime != null && toTime != null) {
//	            if (fromTime.after(toTime)) {
//	                // You can show an error message or reset the value
//	            	FacesContext.getCurrentInstance().addMessage(null, 
//	        	            new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "From Time cannot be after To Time."));
//	                toTime = null; // Reset the invalid time
//	            }
//	        }
//	    }
	
	
	
	
	
	
	private void loadMinnagamHistoryComplaints() {
		 SessionFactory factory = null;
		    Session session = null;
		    try {

		        factory = HibernateUtil.getSessionFactory();
		        session = factory.openSession();
		        
		        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();

		        CriteriaQuery<MinnagamHistoryBean> criteriaQuery = criteriaBuilder.createQuery(MinnagamHistoryBean.class);
		        Root<MinnagamHistoryBean> root = criteriaQuery.from(MinnagamHistoryBean.class);

		        criteriaQuery.select(root);

		      MinnagamHistoryBean complaintList = session.createQuery(criteriaQuery).getSingleResult();
		      
		      if(complaintList!=null) {
		    	  Timestamp ts = complaintList.getUpTo();
		    	  LocalDateTime ldt = ts.toLocalDateTime();
		    	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		    	    this.historyComplaintsUpTo = ldt.format(formatter); 
		    	  this.totalComplaints = complaintList.getTotalComp();
		      }
		        
		        
		    }catch(Exception e) {
		    	e.printStackTrace();
		    }
		
	}
	
	
	
	public void fetchComplaintsDetails() {
		
		String complaintIdParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("complaintId");
		//closureCloseComplaint(complaintIdParam);
		
		if (complaintIdParam != null && !complaintIdParam.isEmpty()) {
	        Long complaintId = Long.parseLong(complaintIdParam); 
	        
	        selectedComplaintId = new ComplaintValueBean();
	        SessionFactory factory = null;
	        Session session = null;

	        try {
	            factory = HibernateUtil.getSessionFactory();
	            session = factory.openSession();

	            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
	            CriteriaQuery<ComplaintBean> criteriaQuery = criteriaBuilder.createQuery(ComplaintBean.class);
	            Root<ComplaintBean> root = criteriaQuery.from(ComplaintBean.class);

	            criteriaQuery.select(root)
	                         .where(criteriaBuilder.equal(root.get("id"), complaintId)); 

	            Query<ComplaintBean> query = session.createQuery(criteriaQuery);
	            ComplaintBean complaint = query.uniqueResult(); 
	            
	            CriteriaBuilder criteriaBuilders = session.getCriteriaBuilder();
	            CriteriaQuery<CompContactMap> criteriaQuerys = criteriaBuilders.createQuery(CompContactMap.class);
	            Root<CompContactMap> roots = criteriaQuerys.from(CompContactMap.class);
	            
	            criteriaQuerys.select(roots)
	            .where(criteriaBuilders.equal(roots.get("complaint"), complaintId));

	            Query<CompContactMap> querys = session.createQuery(criteriaQuerys);
	            CompContactMap comContactMap = querys.uniqueResult(); 
	            
	            if(comContactMap != null){
	            	selectedComplaintMobileNumber = CompContactMapValueBean.convertBeanToValueBean(comContactMap);
	            }
	            
	            if(complaint != null) {
	            	selectedComplaintId = ComplaintValueBean.convertComplaintBeanTocomplaintValueBean(complaint);
					
					CriteriaQuery<ComplaintHistoryBean> historyQuery = criteriaBuilder.createQuery(ComplaintHistoryBean.class);
		            Root<ComplaintHistoryBean> historyRoot = historyQuery.from(ComplaintHistoryBean.class);
		            
		            historyQuery.select(historyRoot)
                    .where(criteriaBuilder.equal(historyRoot.get("complaintBean").get("id"), complaintId))
                    .orderBy(criteriaBuilder.desc(historyRoot.get("createdOn"))); 
		            
		            Query<ComplaintHistoryBean> historyResult = session.createQuery(historyQuery);
	                historyResult.setMaxResults(1);
	                
	                ComplaintHistoryBean latestHistory = historyResult.uniqueResult();
	                
	                if (latestHistory != null) {
	                   
	                    String latestDescription = latestHistory.getDescription();
	                    selectedComplaintId.setFinalDescription(latestDescription);
	                }
				} 
	        } catch (Exception e) {
	            logger.error(ExceptionUtils.getStackTrace(e));
	            FacesContext.getCurrentInstance().addMessage(null, 
	                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "An error occurred while fetching complaint details"));
	        } finally {
	            HibernateUtil.closeSession(factory, session);
	        }
	    } else {
	        FacesContext.getCurrentInstance().addMessage(null, 
	            new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "No complaint ID selected"));
	    }
	}
	
	
	private void startScheduledUpdates() {
		 OutageService outageService = new OutageService();
	       
	            outageService.updateExpiredOutages();
	         
	    }
	
	private void loadSessionData() {
        FacesContext context = FacesContext.getCurrentInstance();
        String loggedInCircleName = (String) context.getExternalContext().getSessionMap().get("loggedInCircleName");
        String loggedInSectionName = (String) context.getExternalContext().getSessionMap().get("loggedInSection");

        if (loggedInCircleName != null) {
            this.circleName = loggedInCircleName;
        }
        if (loggedInSectionName != null) {
            this.sectionName = loggedInSectionName;
        }
    }

	private void setLstRegions() {
		
		SessionFactory factory = null;
		Session session = null;
		try {
			System.err.println("WELCOME TO SETLISTWISE");
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			List<RegionValueBean> regionsList = new ArrayList<RegionValueBean>();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<RegionBean> criteriaQuery = criteriaBuilder.createQuery(RegionBean.class);
			Root<RegionBean> root = criteriaQuery.from(RegionBean.class);
			criteriaQuery.select(root).orderBy(criteriaBuilder.asc(root.get("name")));
			List<RegionBean> list = session.createQuery(criteriaQuery).getResultList();
			
			for(RegionBean regionBean : list) {
				regionsList.add(RegionValueBean.convertRegionBeanToRegionValueBean(regionBean));
			}

			dm.setLstRegions(regionsList);
			dmFilter.setLstRegions(regionsList);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
	}
	
	public void loadCirclesMinnagam() {
		
		SessionFactory factory = null;
		Session session = null;
		try {
			 String circleCode = this.circleName;
			 
			 
				factory = HibernateUtil.getSessionFactory();
				session = factory.openSession();
				
				List<CircleValueBean> circleList = new ArrayList<CircleValueBean>();
				
				CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<CircleBean> criteriaQuery = criteriaBuilder.createQuery(CircleBean.class);
				Root<CircleBean> root = criteriaQuery.from(CircleBean.class);
	
		        criteriaQuery.select(root)
		        .orderBy(criteriaBuilder.asc(root.get("regionBean").get("id")));

				Query<CircleBean> query = session.createQuery(criteriaQuery);
				
				List<CircleBean> list = query.getResultList();
				
				for(CircleBean circleBean : list) {
					circleList.add(CircleValueBean.convertCircleBeanToCircleValueBean(circleBean));
				}

				dmFilter.setListCircles(circleList);
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		
	}
	
public void loadSectionByCircleMinnagam() {
		
		SessionFactory factory = null;
		Session session = null; 
		
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
		   
			 String[] parts = circleName.split("\\|");
			    String id = parts[0];    // e.g., "123"
			    String code = parts[1];  // e.g., "CIRCLE_A"
			    
			    // Use id and code as needed
//			    System.out.println("ID: " + id + ", Code: " + code);
			    
	      
	        
			List<SectionValueBean> sectionList = new ArrayList<SectionValueBean>();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<SectionBean> criteriaQuery = criteriaBuilder.createQuery(SectionBean.class);
			Root<SectionBean> root = criteriaQuery.from(SectionBean.class);
			
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("circleBean").get("code"), code))
								.orderBy(criteriaBuilder.asc(root.get("id")));
			
			Query<SectionBean> query = session.createQuery(criteriaQuery);
			List<SectionBean> listSection = query.getResultList();
			
			sectionList = listSection.stream().map(section -> SectionValueBean.convertSectionBeanToSectionValueBean(section)).
						collect(Collectors.toList());
			
			dmFilter.setLstSectionsMinnagam(sectionList);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
	}
	
public void ssNameBySection() {
	
	SessionFactory factory = null;
	Session session = null;
	
	try {
		
		factory = HibernateUtil.getSessionFactory();
		session = factory.openSession();
		
		List<SectionValueBean> sectionValueBean = new ArrayList<SectionValueBean>();
		
		FacesContext context = FacesContext.getCurrentInstance();
		String loggedInSectionName = (String) context.getExternalContext().getSessionMap().get("loggedInSection");
		Integer loggedInSectionId = (Integer) context.getExternalContext().getSessionMap().get("loggedInSectionId");
		String loggedInCircleName = (String) context.getExternalContext().getSessionMap().get("loggedInCircleName");
		Integer loggedInCircleId = (Integer) context.getExternalContext().getSessionMap().get("loggedInCircleId");
		String secCode = (String) context.getExternalContext().getSessionMap().get("sectionCode");
		
		String hqlQuery = "SELECT DISTINCT a.ssCode, a.ssName FROM DTMasterGISBean a ";
		
		if(officer.roleId == 1) {
			
			
			 String circleQuery = "SELECT c FROM CircleBean c WHERE c.id = :circleId";
	            Query<CircleBean> circleQueryResult = session.createQuery(circleQuery, CircleBean.class);
	            circleQueryResult.setParameter("circleId", loggedInCircleId);
	            List<CircleBean> circleList = circleQueryResult.getResultList();

	            if (circleList.isEmpty()) {
	                return; 
	            }
	            
	            CircleBean circleBean = circleList.get(0);
	            String circleListCode = circleBean.getCode();

	            hqlQuery += "WHERE a.secCode = :secCode AND a.cirCode = :cirCode";

	            Query<Object[]> query = session.createQuery(hqlQuery, Object[].class);
	            query.setParameter("secCode", secCode);
	            query.setParameter("cirCode", circleListCode);
	            
	            System.out.println("Section Code ::::::::: "+secCode);
	            System.out.println("Circle Code :::::::: "+circleListCode);

	            List<Object[]> resultList = query.getResultList();

	            if (resultList != null && !resultList.isEmpty()) {
		            List<DTMasterGISValueBean> dtMasterGISValueBeans = resultList.stream().map(row -> {
		                String ssCode = (String) row[0];
		                String ssName = (String) row[1];
		                return new DTMasterGISValueBean(ssCode, ssName); 
		            }).collect(Collectors.toList());
		            

		            dmFilter.setLstSSName(dtMasterGISValueBeans);

	            }else {
	            	System.out.println("Not there");
	            }
			
			
		}else {
			
            String sectionCodes = getSectionName();
            String circleCodes = getCircleName();
            
            String[] parts = circleCodes.split("\\|");
		    String id = parts[0];    // e.g., "123"
		    String circleCode = parts[1];  // e.g., "CIRCLE_A"
		    
		    String[] partss = sectionCodes.split("\\|");
		    String sectionId = partss[0];    // e.g., "123"
		    String sectionCode = partss[1];  // e.g., "CIRCLE_A"
		    
		    // Use id and code as needed
		    System.err.println("CIRCLEID: " + id + ", CIRCLECode: " + circleCode);
		    System.err.println("SECTION ID: " + sectionId + ", SECTION ID: " + sectionCode);
		    
            
            hqlQuery = "SELECT DISTINCT a.ssCode, a.ssName FROM DTMasterGISBean a WHERE a.secCode = :secCode AND a.cirCode = :cirCode";
            Query<Object[]> query = session.createQuery(hqlQuery, Object[].class);
            query.setParameter("secCode", sectionCode);
            query.setParameter("cirCode", circleCode);

            List<Object[]> resultList = query.getResultList();
            
            if (resultList != null && !resultList.isEmpty()) {
	            List<DTMasterGISValueBean> dtMasterGISValueBeans = resultList.stream().map(row -> {
	                String ssCode = (String) row[0];
	                String ssName = (String) row[1];
	                return new DTMasterGISValueBean(ssCode, ssName); 
	            }).collect(Collectors.toList());

	            dmFilter.setLstSSName(dtMasterGISValueBeans);
            }else {
            	System.out.println("Not there");
            }
            
		}
	
	} catch (Exception e) {
		logger.error(ExceptionUtils.getStackTrace(e));
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
	} finally {
		HibernateUtil.closeSession(factory, session);
	}
}


public void fdrNameBySSName() {
	
	SessionFactory factory = null;
	Session session = null;
	
	try {
		
		factory = HibernateUtil.getSessionFactory();
		session = factory.openSession();
		
		FacesContext context = FacesContext.getCurrentInstance();
		String loggedInSectionName = (String) context.getExternalContext().getSessionMap().get("loggedInSection");
		Integer loggedInSectionId = (Integer) context.getExternalContext().getSessionMap().get("loggedInSectionId");
		String loggedInCircleName = (String) context.getExternalContext().getSessionMap().get("loggedInCircleName");
		Integer loggedInCircleId = (Integer) context.getExternalContext().getSessionMap().get("loggedInCircleId");
		String secCode = (String) context.getExternalContext().getSessionMap().get("sectionCode");
		String cirCode =(String) context.getExternalContext().getSessionMap().get("circleCode");
		
		String sectionCode = getSectionName();
       String circleCode = getCircleName();
        
//        String sectionCodes = getSectionName();
//        String circleCodes = getCircleName();
//        
//        String[] parts = circleCodes.split("\\|");
//	    String id = parts[0];    // e.g., "123"
//	    String circleCode = parts[1];  // e.g., "CIRCLE_A"
//	    
//	    String[] partss = sectionCodes.split("\\|");
//	    String sectionId = partss[0];    // e.g., "123"
//	    String sectionCode = partss[1];  // e.g., "CIRCLE_A"
//	    
//	    // Use id and code as needed
//	    System.err.println("CIRCLEID: " + id + ", CIRCLECode: " + circleCode);
//	    System.err.println("SECTION ID: " + sectionId + ", SECTION ID: " + sectionCode);
	    
        String ssCode = getSsName();
        System.err.println("SS ID: " + ssCode + ", SECTION ID: " + sectionCode);
		
        String sqlQuery = "SELECT DISTINCT a.fdrCode, a.fdrName " +
                "FROM DTMasterGISBean a " +
                "WHERE a.cirCode = :cirCode " +
                "AND a.secCode = :secCode " +
                "AND a.ssCode = :ssCode "+
                "ORDER BY a.fdrCode";
        
		Query<Object[]> query = session.createQuery(sqlQuery, Object[].class);
        query.setParameter("secCode", secCode);
        query.setParameter("cirCode", cirCode);
        query.setParameter("ssCode", ssCode);

        List<Object[]> resultList = query.getResultList();

        List<DTMasterGISValueBean> dtMasterGISValueBeans = resultList.stream().map(row -> {
            String fdrCode = (String) row[0];
            String fdrName = (String) row[1];
            
            DTMasterGISValueBean bean = new DTMasterGISValueBean();
            bean.getDTMasterGISValueBean(fdrCode, fdrName);  // Using the setter method
            return bean;
            
        }).collect(Collectors.toList());
		
		dmFilter.setLstFdrName(dtMasterGISValueBeans);
		
		
		
	} catch (Exception e) {
		logger.error(ExceptionUtils.getStackTrace(e));
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
	} finally {
		HibernateUtil.closeSession(factory, session);
	}
}

public void fdrNameBySSNameM() {
	
	SessionFactory factory = null;
	Session session = null;
	
	try {
		
		factory = HibernateUtil.getSessionFactory();
		session = factory.openSession();
		
		FacesContext context = FacesContext.getCurrentInstance();
		String loggedInSectionName = (String) context.getExternalContext().getSessionMap().get("loggedInSection");
		Integer loggedInSectionId = (Integer) context.getExternalContext().getSessionMap().get("loggedInSectionId");
		String loggedInCircleName = (String) context.getExternalContext().getSessionMap().get("loggedInCircleName");
		Integer loggedInCircleId = (Integer) context.getExternalContext().getSessionMap().get("loggedInCircleId");
		String secCode = (String) context.getExternalContext().getSessionMap().get("sectionCode");
		String cirCode =(String) context.getExternalContext().getSessionMap().get("circleCode");
		
//		String sectionCode = getSectionName();
//        String circleCode = getCircleName();
       
		 String sectionCodes = getSectionName();
	        String circleCodes = getCircleName();
	        
	        String[] parts = circleCodes.split("\\|");
		    String id = parts[0];    // e.g., "123"
		    String circleCode = parts[1];  // e.g., "CIRCLE_A"
		    
		    String[] partss = sectionCodes.split("\\|");
		    String sectionId = partss[0];    // e.g., "123"
		    String sectionCode = partss[1];  // e.g., "CIRCLE_A"
        String ssCode = getSsName();
		
        String sqlQuery = "SELECT DISTINCT a.fdrCode, a.fdrName " +
                "FROM DTMasterGISBean a " +
                "WHERE a.cirCode = :cirCode " +
                "AND a.secCode = :secCode " +
                "AND a.ssCode = :ssCode " +
                "ORDER BY a.fdrCode";
        
		Query<Object[]> query = session.createQuery(sqlQuery, Object[].class);
        query.setParameter("secCode", sectionCode);
        query.setParameter("cirCode", circleCode);
        query.setParameter("ssCode", ssCode);

        List<Object[]> resultList = query.getResultList();

        List<DTMasterGISValueBean> dtMasterGISValueBeans = resultList.stream().map(row -> {
            String fdrCode = (String) row[0];
            String fdrName = (String) row[1];
            
            DTMasterGISValueBean bean = new DTMasterGISValueBean();
            bean.getDTMasterGISValueBean(fdrCode, fdrName);  // Using the setter method
            return bean;
            
        }).collect(Collectors.toList());
		
		dmFilter.setLstFdrName(dtMasterGISValueBeans);
		
		
		
	} catch (Exception e) {
		logger.error(ExceptionUtils.getStackTrace(e));
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
	} finally {
		HibernateUtil.closeSession(factory, session);
	}
}
	
public void dtNameBySSName() {
	
	SessionFactory factory = null;
	Session session = null;
	
	try {
		
		factory = HibernateUtil.getSessionFactory();
		session = factory.openSession();
		
		FacesContext context = FacesContext.getCurrentInstance();
		String loggedInSectionName = (String) context.getExternalContext().getSessionMap().get("loggedInSection");
		Integer loggedInSectionId = (Integer) context.getExternalContext().getSessionMap().get("loggedInSectionId");
		String loggedInCircleName = (String) context.getExternalContext().getSessionMap().get("loggedInCircleName");
		Integer loggedInCircleId = (Integer) context.getExternalContext().getSessionMap().get("loggedInCircleId");
	    String secCode = (String) context.getExternalContext().getSessionMap().get("sectionCode");
		String cirCode =(String) context.getExternalContext().getSessionMap().get("circleCode");
//		
     	//String sectionCode = getSectionName();
       // String circleCode = getCircleName();
//		 String sectionCodes = getSectionName();
//	        String circleCodes = getCircleName();
//	        
//	        String[] parts = circleCodes.split("\\|");
//		    String id = parts[0];    // e.g., "123"
//		    String cirCode = parts[1];  // e.g., "CIRCLE_A"
//		    
//		    String[] partss = sectionCodes.split("\\|");
//		    String sectionId = partss[0];    // e.g., "123"
//		    String secCode = partss[1];  // e.g., "CIRCLE_A"
//		    
         String ssCode = getSsName();
         String fdrCode = getFdrName();
		
        String sqlQuery = "SELECT DISTINCT a.dtCode, a.dtName " +
                "FROM DTMasterGISBean a " +
                "WHERE a.cirCode = :cirCode " +
                "AND a.secCode = :secCode " +
                "AND a.ssCode = :ssCode " + 
                "AND a.fdrCode = :fdrCode "
                +
                "ORDER BY a.dtCode";
        
                
                
        
		Query<Object[]> query = session.createQuery(sqlQuery, Object[].class);
        query.setParameter("secCode", secCode);
        query.setParameter("cirCode", cirCode);
        query.setParameter("ssCode", ssCode);
        query.setParameter("fdrCode", fdrCode);

        List<Object[]> resultList = query.getResultList();

        List<DTMasterGISValueBean> dtMasterGISValueBeans = resultList.stream().map(row -> {
            String dtCode = (String) row[0];
            String dtName = (String) row[1];
            
            DTMasterGISValueBean bean = new DTMasterGISValueBean();
            bean.getDTMasterGISValueBeanDt(dtCode, dtName);  // Using the setter method
            return bean;
            
        }).collect(Collectors.toList());
		
		dmFilter.setLstDtName(dtMasterGISValueBeans);
		
		
		
	} catch (Exception e) {
		logger.error(ExceptionUtils.getStackTrace(e));
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
	} finally {
		HibernateUtil.closeSession(factory, session);
	}
}

public void dtNameBySSNameM() {
	
	SessionFactory factory = null;
	Session session = null;
	
	try {
		
		factory = HibernateUtil.getSessionFactory();
		session = factory.openSession();
		
		FacesContext context = FacesContext.getCurrentInstance();
		String loggedInSectionName = (String) context.getExternalContext().getSessionMap().get("loggedInSection");
		Integer loggedInSectionId = (Integer) context.getExternalContext().getSessionMap().get("loggedInSectionId");
		String loggedInCircleName = (String) context.getExternalContext().getSessionMap().get("loggedInCircleName");
		Integer loggedInCircleId = (Integer) context.getExternalContext().getSessionMap().get("loggedInCircleId");
		String secCode = (String) context.getExternalContext().getSessionMap().get("sectionCode");
		String cirCode =(String) context.getExternalContext().getSessionMap().get("circleCode");
		
//		String sectionCode = getSectionName();
//        String circleCode = getCircleName();
		 String sectionCodes = getSectionName();
	        String circleCodes = getCircleName();
	        
	        String[] parts = circleCodes.split("\\|");
		    String id = parts[0];    // e.g., "123"
		    String circleCode = parts[1];  // e.g., "CIRCLE_A"
		    
		    String[] partss = sectionCodes.split("\\|");
		    String sectionId = partss[0];    // e.g., "123"
		    String sectionCode = partss[1];  // e.g., "CIRCLE_A"
        String ssCode = getSsName();
        String fdrCode = getFdrName();
		
        String sqlQuery = "SELECT DISTINCT a.dtCode, a.dtName " +
                "FROM DTMasterGISBean a " +
                "WHERE a.cirCode = :cirCode " +
                "AND a.secCode = :secCode " +
                "AND a.ssCode = :ssCode " + 
                "AND a.fdrCode = :fdrCode " +
                "ORDER BY a.dtCode";
        
		Query<Object[]> query = session.createQuery(sqlQuery, Object[].class);
        query.setParameter("secCode", sectionCode);
        query.setParameter("cirCode", circleCode);
        query.setParameter("ssCode", ssCode);
        query.setParameter("fdrCode", fdrCode);

        List<Object[]> resultList = query.getResultList();

        List<DTMasterGISValueBean> dtMasterGISValueBeans = resultList.stream().map(row -> {
            String dtCode = (String) row[0];
            String dtName = (String) row[1];
            
            DTMasterGISValueBean bean = new DTMasterGISValueBean();
            bean.getDTMasterGISValueBeanDt(dtCode, dtName);  // Using the setter method
            return bean;
            
        }).collect(Collectors.toList());
		
		dmFilter.setLstDtName(dtMasterGISValueBeans);
		
		
		
	} catch (Exception e) {
		logger.error(ExceptionUtils.getStackTrace(e));
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
	} finally {
		HibernateUtil.closeSession(factory, session);
	}
}


	private void setLstRoles() {
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			List<RoleValueBean> rolesList = new ArrayList<RoleValueBean>();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<RoleBean> criteriaQuery = criteriaBuilder.createQuery(RoleBean.class);
			Root<RoleBean> root = criteriaQuery.from(RoleBean.class);
			criteriaQuery.select(root).orderBy(criteriaBuilder.asc(root.get("name")));
			Query<RoleBean> query = session.createQuery(criteriaQuery);
			List<RoleBean> list = query.getResultList();
			
			for(RoleBean roleBean : list) {
				rolesList.add(RoleValueBean.convertRoleBeanToRoleValueBean(roleBean));
			}


			dm.setLstRoles(rolesList);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
	}
	
	
	public void setFilter() {

		
		dmFilter.getRegions().setId(officer.regionId);
		dmFilter.getCircles().setId(officer.circleId);
		dmFilter.getDivisions().setId(officer.divisionId);
		dmFilter.getSubDivisions().setId(officer.subDivisionId);
		dmFilter.getSections().setId(officer.sectionId);

		if (officer.roleId >= 5) {
			System.err.println("ADMIN REGION ID"+officer.regionId);
			setLstRegions();
			listCircles(officer.regionId);
		}
		if (officer.roleId == 4) {
			if(officer.regionId > 0) {
				listCircles(officer.regionId);
			}
			if(officer.circleId > 0) {
				listDivisions(officer.circleId);
			}
		}
		if (officer.roleId == 3) {
			if(officer.circleId > 0) {
				listDivisions(officer.circleId);
			}
			if(officer.divisionId > 0) {
				listSubDivisions(officer.divisionId);
			}
		}
		if (officer.roleId == 2) {
			if(officer.divisionId > 0) {
				listSubDivisions(officer.divisionId);
				 listSections(officer.subDivisionId); 
			}
		}
		if (officer.roleId == 1) {
			System.err.println("CALLCENTER ROLL ID"+officer.roleId);
			if(officer.subDivisionId > 0) {
				  listSections(officer.subDivisionId, officer.sectionId);
			}
		}

	}
	
	public void setFilterForReport() {
		
		 HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
	                .getExternalContext().getSession(false);
	      CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");

	      if(callCenterValueBean!=null && callCenterValueBean.getRoleId()==1) {
	    	  
	    	  System.err.println("ENTERED FILTER LOGIC");
	    	  int userId= callCenterValueBean.getId();
	    	  SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
	    	  Session session = sessionFactory.openSession();
              try {
                  session.beginTransaction();

                  @SuppressWarnings("unchecked")
					List<CircleBean> circleId = session.createQuery(
                          "select c.circleBean from CallCenterMappingBean c " +
                          "where c.callCenterUserBean.id = :userId")
                          .setParameter("userId", userId)
                          .getResultList();
                  
                  
                  @SuppressWarnings("unchecked")
					List<RegionBean> regionId = session.createQuery(
                          "select DISTINCT c.circleBean.regionBean from CallCenterMappingBean c " +
                          "where c.callCenterUserBean.id = :userId")
                          .setParameter("userId", userId)
                          .getResultList();
                  
					List<RegionValueBean> regionList = new ArrayList<RegionValueBean>();
					for (RegionBean regBean : regionId) {
						regionList.add(RegionValueBean.convertRegionBeanToRegionValueBean(regBean));
					}
             
                  
					List<CircleValueBean> circleList = new ArrayList<CircleValueBean>();
					for (CircleBean circleBean : circleId) {
						circleList.add(CircleValueBean.convertCircleBeanToCircleValueBean(circleBean));
					}

    			this.dmFilter.setLstCircles(circleList);
    			this.dmFilter.setLstRegions(regionList);
  	    	  System.err.println("CIRCLE LIST SIZE--------------------"+circleList.size());



              } finally {
                  session.close();
              }
	      }
	      else {

		if (officer.roleId == 1) {
			if(officer.subDivisionId > 0) {
				  listSections(officer.subDivisionId, officer.sectionId);
			}
		}
		if(officer.roleId == 2 ) {
			if(officer.subDivisionId > 0) {
				  listSections(officer.subDivisionId);
			}
		}
		if (officer.roleId == 3) {
			if(officer.divisionId > 0) {
				listSubDivisions(officer.divisionId);
			}
		}
		if (officer.roleId == 4) {
			if(officer.circleId > 0) {
				listDivisions(officer.circleId);
			}
		}
		if (officer.roleId == 5) {
			if(officer.regionId > 0) {
				listCircles(officer.regionId);
			}
		}
	      }
	}

	public void showMap(ViewComplaintValueBean comp) {
		System.out.println("getLatitude " + comp.getLatitude());
		System.out.println("getLongitude " + comp.getLongitude());
		Layer placesLayer = (new Layer()).setLabel("Places");

		placesLayer.addMarker(new Marker(new LatLong(comp.getLatitude(), comp.getLongitude()), comp.getDescription(),
				new Pulse(true, 10, "#ff0000")));

		map.setWidth("600px").setHeight("400px").setCenter(new LatLong(comp.getLatitude(), comp.getLongitude()))
				.setZoom(16);
		map.addLayer(placesLayer);

		complaint = comp;
	}

	public void onToggle(ToggleEvent event) {
		try {
			int complaintId = (int) event.getComponent().getAttributes().get("complaintId");
			ComplaintsDao complaintsDao = new ComplaintsDao();
			complaintsDao.getCompalintHistoryList(complaintId, dm);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}

	}

	public void listCircles() {
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			List<CircleValueBean> circleList = new ArrayList<CircleValueBean>();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<CircleBean> criteriaQuery = criteriaBuilder.createQuery(CircleBean.class);
			Root<CircleBean> root = criteriaQuery.from(CircleBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("regionBean").get("id"), dm.getRegions().getId()))
			.orderBy(criteriaBuilder.asc(root.get("name")));

			Query<CircleBean> query = session.createQuery(criteriaQuery);
			List<CircleBean> list = query.getResultList();
			
			for(CircleBean circleBean : list) {
				circleList.add(CircleValueBean.convertCircleBeanToCircleValueBean(circleBean));
			}

			dm.setLstCircles(circleList);

			if (dm.getLstDivisions() != null) {
				dm.getLstDivisions().clear();
			}

			if (dm.getLstSubDivisions() != null) {
				dm.getLstSubDivisions().clear();
			}

			if (dm.getLstSections() != null) {
				dm.getLstSections().clear();
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		
	}

	public void listDivisions() {
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			List<DivisionValueBean> divisionList = new ArrayList<DivisionValueBean>();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<DivisionBean> criteriaQuery = criteriaBuilder.createQuery(DivisionBean.class);
			Root<DivisionBean> root = criteriaQuery.from(DivisionBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("circleBean").get("id"), dm.getCircles().getId()))
			.orderBy(criteriaBuilder.asc(root.get("name")));

			Query<DivisionBean> query = session.createQuery(criteriaQuery);
			List<DivisionBean> list = query.getResultList();
			
			for(DivisionBean divisionBean : list) {
				divisionList.add(DivisionValueBean.convertDivisionBeanToDivisionValueBean(divisionBean));
			}

			dm.setLstDivisions(divisionList);

			if (dm.getLstSubDivisions() != null) {
				dm.getLstSubDivisions().clear();
			}

			if (dm.getLstSections() != null) {
				dm.getLstSections().clear();
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

	}

	public void listSubDivisions() {
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			List<SubDivisionValueBean> subDivisionList = new ArrayList<SubDivisionValueBean>();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<SubDivisionBean> criteriaQuery = criteriaBuilder.createQuery(SubDivisionBean.class);
			Root<SubDivisionBean> root = criteriaQuery.from(SubDivisionBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("divisionBean").get("id"), dm.getDivisions().getId()))
			.orderBy(criteriaBuilder.asc(root.get("name")));

			Query<SubDivisionBean> query = session.createQuery(criteriaQuery);
			List<SubDivisionBean> list = query.getResultList();
			
			for(SubDivisionBean subDivisionBean : list) {
				subDivisionList.add(SubDivisionValueBean.convertSubDivisionBeanToSubDivisionValueBean(subDivisionBean));
			}

			dm.setLstSubDivisions(subDivisionList);

			if (dm.getLstSections() != null) {
				dm.getLstSections().clear();
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
	}

	public void listSections() {
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			List<SectionValueBean> sectionList = new ArrayList<SectionValueBean>();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<SectionBean> criteriaQuery = criteriaBuilder.createQuery(SectionBean.class);
			Root<SectionBean> root = criteriaQuery.from(SectionBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("subDivisionBean").get("id"), dm.getSubDivisions().getId()))
			.orderBy(criteriaBuilder.asc(root.get("name")));

			Query<SectionBean> query = session.createQuery(criteriaQuery);
			List<SectionBean> list = query.getResultList();
			
			for(SectionBean sectionBean : list) {
				sectionList.add(SectionValueBean.convertSectionBeanToSectionValueBean(sectionBean));
			}

			dm.setLstSections(sectionList);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
	}
	
	
	public void listCircles(int regionId) {
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			List<CircleValueBean> circleList = new ArrayList<CircleValueBean>();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<CircleBean> criteriaQuery = criteriaBuilder.createQuery(CircleBean.class);
			Root<CircleBean> root = criteriaQuery.from(CircleBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("regionBean").get("id"), regionId))
			.orderBy(criteriaBuilder.asc(root.get("name")));

			Query<CircleBean> query = session.createQuery(criteriaQuery);
			List<CircleBean> list = query.getResultList();
			
			for(CircleBean circleBean : list) {
				circleList.add(CircleValueBean.convertCircleBeanToCircleValueBean(circleBean));
			}

			dmFilter.setLstCircles(circleList);

			if (dmFilter.getLstDivisions() != null) {
				dmFilter.getLstDivisions().clear();
			}

			if (dmFilter.getLstSubDivisions() != null) {
				dmFilter.getLstSubDivisions().clear();
			}

			if (dmFilter.getLstSections() != null) {
				dmFilter.getLstSections().clear();
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		
	}
	
	
	public void loadCircles() {
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			if(dmFilter.getRegionFilter() != null && dmFilter.getRegionFilter().length > 0 ) {
				
				factory = HibernateUtil.getSessionFactory();
				session = factory.openSession();
				
				List<CircleValueBean> circleList = new ArrayList<CircleValueBean>();
				
				CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<CircleBean> criteriaQuery = criteriaBuilder.createQuery(CircleBean.class);
				Root<CircleBean> root = criteriaQuery.from(CircleBean.class);
				
				criteriaQuery.select(root).where((root.get("regionBean").get("id")).in(Arrays.asList(dmFilter.getRegionFilter())))
				.orderBy(criteriaBuilder.asc(root.get("name")));
				
				Query<CircleBean> query = session.createQuery(criteriaQuery);
				
				List<CircleBean> list = query.getResultList();
				
				for(CircleBean circleBean : list) {
					circleList.add(CircleValueBean.convertCircleBeanToCircleValueBean(circleBean));
				}

				dmFilter.setLstCircles(circleList);
			} else {
				if (dmFilter.getLstCircles() != null) {
					dmFilter.getLstCircles().clear();
				}
			}
			
			if (dmFilter.getCircleFilter() != null) {
				dmFilter.setCircleFilter(null);
			}
			if (dmFilter.getDivisionFilter() != null) {
				dmFilter.setDivisionFilter(null);
			}
			if (dmFilter.getSubDivisionFilter() != null) {
				dmFilter.setSubDivisionFilter(null);
			}
			if (dmFilter.getSectionFilter() != null) {
				dmFilter.setSectionFilter(null);
			}
			
			
			if (dmFilter.getLstDivisions() != null) {
				dmFilter.getLstDivisions().clear();
			}
			if (dmFilter.getLstSubDivisions() != null) {
				dmFilter.getLstSubDivisions().clear();
			}
			if (dmFilter.getLstSections() != null) {
				dmFilter.getLstSections().clear();
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		
	}
	
	public void loadCirclesCMD() {
	    SessionFactory factory = null;
	    Session session = null;
	    try {
	        if (dmFilter.getRegionOneFilter() != null) {
	           
	            factory = HibernateUtil.getSessionFactory();
	            session = factory.openSession();

	            List<CircleValueBean> circleList = new ArrayList<>();

	            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
	            CriteriaQuery<CircleBean> criteriaQuery = criteriaBuilder.createQuery(CircleBean.class);
	            Root<CircleBean> root = criteriaQuery.from(CircleBean.class);

	            Predicate predicate;
	            if ("ALL".equals(dmFilter.getRegionOneFilter())) {
	                
	                List<Integer> allRegionIds = dmFilter.getLstRegions().stream()
	                        .map(RegionValueBean::getId)  
	                        .collect(Collectors.toList());
	                
	                

	               
	                predicate = root.get("regionBean").get("id").in(allRegionIds);
	            } else {
	                
	                predicate = criteriaBuilder.equal(root.get("regionBean").get("id"), dmFilter.getRegionOneFilter());
	            }

	            criteriaQuery.select(root).where(predicate).orderBy(criteriaBuilder.asc(root.get("name")));

	            Query<CircleBean> query = session.createQuery(criteriaQuery);
	            List<CircleBean> list = query.getResultList();

	            for (CircleBean circleBean : list) {
	                circleList.add(CircleValueBean.convertCircleBeanToCircleValueBean(circleBean));
	            }

	            dmFilter.setLstCircles(circleList);
	        } else {
	            if (dmFilter.getLstCircles() != null) {
	                dmFilter.getLstCircles().clear();
	            }
	        }

	        dmFilter.setCircleFilter(null);
	        dmFilter.setDivisionFilter(null);
	        dmFilter.setSubDivisionFilter(null);
	        dmFilter.setSectionFilter(null);

	        if (dmFilter.getLstDivisions() != null) dmFilter.getLstDivisions().clear();
	        if (dmFilter.getLstSubDivisions() != null) dmFilter.getLstSubDivisions().clear();
	        if (dmFilter.getLstSections() != null) dmFilter.getLstSections().clear();
	        
	    } catch (Exception e) {
	        logger.error(ExceptionUtils.getStackTrace(e));
	        FacesContext.getCurrentInstance().addMessage(null, 
	            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
	    } finally {
	        HibernateUtil.closeSession(factory, session);
	    }
	}

	public void listDivisions(int circleId) {
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			List<DivisionValueBean> divisionList = new ArrayList<DivisionValueBean>();

			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<DivisionBean> criteriaQuery = criteriaBuilder.createQuery(DivisionBean.class);
			Root<DivisionBean> root = criteriaQuery.from(DivisionBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("circleBean").get("id"), circleId))
			.orderBy(criteriaBuilder.asc(root.get("name")));

			Query<DivisionBean> query = session.createQuery(criteriaQuery);
			List<DivisionBean> list = query.getResultList();
			
			for(DivisionBean divisionBean : list) {
				divisionList.add(DivisionValueBean.convertDivisionBeanToDivisionValueBean(divisionBean));
			}

			dmFilter.setLstDivisions(divisionList);

			if (dmFilter.getLstSubDivisions() != null) {
				dmFilter.getLstSubDivisions().clear();
			}

			if (dmFilter.getLstSections() != null) {
				dmFilter.getLstSections().clear();
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

	}
	
public void loadDivisions() {
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			if(dmFilter.getCircleFilter() != null && dmFilter.getCircleFilter().length > 0 ) {
			
				factory = HibernateUtil.getSessionFactory();
				session = factory.openSession();
				
				List<DivisionValueBean> divisionList = new ArrayList<DivisionValueBean>();

				CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<DivisionBean> criteriaQuery = criteriaBuilder.createQuery(DivisionBean.class);
				Root<DivisionBean> root = criteriaQuery.from(DivisionBean.class);
				
				criteriaQuery.select(root).where((root.get("circleBean").get("id")).in(Arrays.asList(dmFilter.getCircleFilter())))
				.orderBy(criteriaBuilder.asc(root.get("name")));

				Query<DivisionBean> query = session.createQuery(criteriaQuery);
				List<DivisionBean> list = query.getResultList();
				
				for(DivisionBean divisionBean : list) {
					divisionList.add(DivisionValueBean.convertDivisionBeanToDivisionValueBean(divisionBean));
				}

				dmFilter.setLstDivisions(divisionList);
			} else {
				if (dmFilter.getLstDivisions() != null) {
					dmFilter.getLstDivisions().clear();
				}
			}
			
			if (dmFilter.getDivisionFilter() != null) {
				dmFilter.setDivisionFilter(null);
			}
			if (dmFilter.getSubDivisionFilter() != null) {
				dmFilter.setSubDivisionFilter(null);
			}
			if (dmFilter.getSectionFilter() != null) {
				dmFilter.setSectionFilter(null);
			}

			if (dmFilter.getLstSubDivisions() != null) {
				dmFilter.getLstSubDivisions().clear();
			}

			if (dmFilter.getLstSections() != null) {
				dmFilter.getLstSections().clear();
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

	}
	
	public void loadDivisionsCMD() {
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			if(dmFilter.getCircleOneFilter() != null) {
			
				factory = HibernateUtil.getSessionFactory();
				session = factory.openSession();
				
				List<DivisionValueBean> divisionList = new ArrayList<DivisionValueBean>();

				CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<DivisionBean> criteriaQuery = criteriaBuilder.createQuery(DivisionBean.class);
				Root<DivisionBean> root = criteriaQuery.from(DivisionBean.class);
				
				Predicate predicate;
	            if ("ALL".equals(dmFilter.getCircleOneFilter())) {
	                
	             List<Integer> allCircleIds = dmFilter.getLstCircles().stream()
	            		 .map(circle -> circle.getId())
	            		 .collect(Collectors.toList());

	               
	                predicate = root.get("circleBean").get("id").in(allCircleIds);
	            } else {
	                
	                predicate = criteriaBuilder.equal(root.get("circleBean").get("id"), dmFilter.getCircleOneFilter());
	            }

	            criteriaQuery.select(root).where(predicate).orderBy(criteriaBuilder.asc(root.get("name")));


				Query<DivisionBean> query = session.createQuery(criteriaQuery);
				List<DivisionBean> list = query.getResultList();
				
				for(DivisionBean divisionBean : list) {
					divisionList.add(DivisionValueBean.convertDivisionBeanToDivisionValueBean(divisionBean));
				}

				dmFilter.setLstDivisions(divisionList);
			} else {
				if (dmFilter.getLstDivisions() != null) {
					dmFilter.getLstDivisions().clear();
				}
			}
			
			if (dmFilter.getDivisionFilter() != null) {
				dmFilter.setDivisionFilter(null);
			}
			if (dmFilter.getSubDivisionFilter() != null) {
				dmFilter.setSubDivisionFilter(null);
			}
			if (dmFilter.getSectionFilter() != null) {
				dmFilter.setSectionFilter(null);
			}

			if (dmFilter.getLstSubDivisions() != null) {
				dmFilter.getLstSubDivisions().clear();
			}

			if (dmFilter.getLstSections() != null) {
				dmFilter.getLstSections().clear();
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

	}

	public void listSubDivisions(int divisionId) {
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			List<SubDivisionValueBean> subDivisionList = new ArrayList<SubDivisionValueBean>();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<SubDivisionBean> criteriaQuery = criteriaBuilder.createQuery(SubDivisionBean.class);
			Root<SubDivisionBean> root = criteriaQuery.from(SubDivisionBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("divisionBean").get("id"), divisionId))
			.orderBy(criteriaBuilder.asc(root.get("name")));

			Query<SubDivisionBean> query = session.createQuery(criteriaQuery);
			List<SubDivisionBean> list = query.getResultList();
			
			for(SubDivisionBean subDivisionBean : list) {
				subDivisionList.add(SubDivisionValueBean.convertSubDivisionBeanToSubDivisionValueBean(subDivisionBean));
			}

			dmFilter.setLstSubDivisions(subDivisionList);

			if (dmFilter.getLstSections() != null) {
				dmFilter.getLstSections().clear();
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
	}
	
	
	public void loadSubDivisions() {
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			if(dmFilter.getDivisionFilter() != null && dmFilter.getDivisionFilter().length > 0 ) {
							
				factory = HibernateUtil.getSessionFactory();
				session = factory.openSession();
				
				List<SubDivisionValueBean> subDivisionList = new ArrayList<SubDivisionValueBean>();
				
				CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<SubDivisionBean> criteriaQuery = criteriaBuilder.createQuery(SubDivisionBean.class);
				Root<SubDivisionBean> root = criteriaQuery.from(SubDivisionBean.class);
				
				criteriaQuery.select(root).where((root.get("divisionBean").get("id")).in(Arrays.asList(dmFilter.getDivisionFilter())))
				.orderBy(criteriaBuilder.asc(root.get("name")));
				
				Query<SubDivisionBean> query = session.createQuery(criteriaQuery);
				List<SubDivisionBean> list = query.getResultList();
				
				for(SubDivisionBean subDivisionBean : list) {
					subDivisionList.add(SubDivisionValueBean.convertSubDivisionBeanToSubDivisionValueBean(subDivisionBean));
				}

				dmFilter.setLstSubDivisions(subDivisionList);
			} else {
				if (dmFilter.getLstSubDivisions() != null) {
					dmFilter.getLstSubDivisions().clear();
				}
			}
			
			if (dmFilter.getSubDivisionFilter() != null) {
				dmFilter.setSubDivisionFilter(null);
			}
			if (dmFilter.getSectionFilter() != null) {
				dmFilter.setSectionFilter(null);
			}
			
			if (dmFilter.getLstSections() != null) {
				dmFilter.getLstSections().clear();
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
	}
	
	public void loadSubDivisionsCMD() {
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			if(dmFilter.getDivisionOneFilter() != null) {
							
				factory = HibernateUtil.getSessionFactory();
				session = factory.openSession();
				
				List<SubDivisionValueBean> subDivisionList = new ArrayList<SubDivisionValueBean>();
				
				CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<SubDivisionBean> criteriaQuery = criteriaBuilder.createQuery(SubDivisionBean.class);
				Root<SubDivisionBean> root = criteriaQuery.from(SubDivisionBean.class);
				
				Predicate predicate;
				
				if("ALL".equals(dmFilter.getDivisionOneFilter())) {
					
					List<Integer> allDivisionIds = dmFilter.getLstDivisions().stream()
							.map(division -> division.getId())
							.collect(Collectors.toList());
					
					predicate = root.get("divisionBean").get("id").in(allDivisionIds);
					
				}else {
					predicate = criteriaBuilder.equal(root.get("divisionBean").get("id"), dmFilter.getDivisionOneFilter());
				}
				
				criteriaQuery.select(root).where(predicate)
				.orderBy(criteriaBuilder.asc(root.get("name")));
				
				Query<SubDivisionBean> query = session.createQuery(criteriaQuery);
				List<SubDivisionBean> list = query.getResultList();
				
				for(SubDivisionBean subDivisionBean : list) {
					subDivisionList.add(SubDivisionValueBean.convertSubDivisionBeanToSubDivisionValueBean(subDivisionBean));
				}

				dmFilter.setLstSubDivisions(subDivisionList);
			} else {
				if (dmFilter.getLstSubDivisions() != null) {
					dmFilter.getLstSubDivisions().clear();
				}
			}
			
			if (dmFilter.getSubDivisionFilter() != null) {
				dmFilter.setSubDivisionFilter(null);
			}
			if (dmFilter.getSectionFilter() != null) {
				dmFilter.setSectionFilter(null);
			}
			
			if (dmFilter.getLstSections() != null) {
				dmFilter.getLstSections().clear();
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
	}

	public void loadSections() {
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			if(dmFilter.getSubDivisionFilter() != null && dmFilter.getSubDivisionFilter().length > 0 ) {
				
				factory = HibernateUtil.getSessionFactory();
				session = factory.openSession();
				
				List<SectionValueBean> sectionList = new ArrayList<SectionValueBean>();
				
				CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<SectionBean> criteriaQuery = criteriaBuilder.createQuery(SectionBean.class);
				Root<SectionBean> root = criteriaQuery.from(SectionBean.class);
				
				criteriaQuery.select(root).where((root.get("subDivisionBean").get("id")).in(Arrays.asList(dmFilter.getSubDivisionFilter())))
				.orderBy(criteriaBuilder.asc(root.get("name")));
				
				Query<SectionBean> query = session.createQuery(criteriaQuery);
				List<SectionBean> list = query.getResultList();
				
				for(SectionBean sectionBean : list) {
					sectionList.add(SectionValueBean.convertSectionBeanToSectionValueBean(sectionBean));
				}

				dmFilter.setLstSections(sectionList);
			} else {
				if (dmFilter.getLstSections() != null) {
					dmFilter.getLstSections().clear();
				}
			}
			
			if (dmFilter.getSectionFilter() != null) {
				dmFilter.setSectionFilter(null);
			}
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
	}
	
	public void loadSectionsCMD() {
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			if(dmFilter.getSubDivisionOneFilter() != null) {
				
				factory = HibernateUtil.getSessionFactory();
				session = factory.openSession();
				
				List<SectionValueBean> sectionList = new ArrayList<SectionValueBean>();
				
				CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<SectionBean> criteriaQuery = criteriaBuilder.createQuery(SectionBean.class);
				Root<SectionBean> root = criteriaQuery.from(SectionBean.class);
				
				Predicate predicate;
				if("ALL".equals(dmFilter.getSubDivisionOneFilter())) {
					
					List<Integer> allSubDivisionIds = dmFilter.getLstSubDivisions().stream()
							.map(subDivision -> subDivision.getId())
							.collect(Collectors.toList());
					
					predicate = root.get("subDivisionBean").get("id").in(allSubDivisionIds);
					
				}else {
					predicate = criteriaBuilder.equal(root.get("subDivisionBean").get("id"), dmFilter.getSubDivisionOneFilter());
				}
				
				criteriaQuery.select(root).where(predicate)
				.orderBy(criteriaBuilder.asc(root.get("name")));
				
				Query<SectionBean> query = session.createQuery(criteriaQuery);
				List<SectionBean> list = query.getResultList();
				
				for(SectionBean sectionBean : list) {
					sectionList.add(SectionValueBean.convertSectionBeanToSectionValueBean(sectionBean));
				}

				dmFilter.setLstSections(sectionList);
			} else {
				if (dmFilter.getLstSections() != null) {
					dmFilter.getLstSections().clear();
				}
			}
			
			if (dmFilter.getSectionFilter() != null) {
				dmFilter.setSectionFilter(null);
			}
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
	}
	
	public void listSections(int subDivisionId) {
		SessionFactory factory = null;
		Session session = null;
		
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			List<SectionValueBean> sectionList = new ArrayList<SectionValueBean>();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<SectionBean> criteriaQuery = criteriaBuilder.createQuery(SectionBean.class);
			Root<SectionBean> root = criteriaQuery.from(SectionBean.class);

			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("subDivisionBean").get("id"), subDivisionId))
			.orderBy(criteriaBuilder.asc(root.get("name")));
			
			Query<SectionBean> query = session.createQuery(criteriaQuery);
			List<SectionBean> list = query.getResultList();
			
			for(SectionBean sectionBean : list) {
				
				sectionList.add(SectionValueBean.convertSectionBeanToSectionValueBean(sectionBean));
			}

			 dmFilter.setLstSections(sectionList);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		
	}

	
	public void listSections(int subDivisionId, int currentSectionId) {
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			List<SectionValueBean> sectionList = new ArrayList<SectionValueBean>();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<SectionBean> criteriaQuery = criteriaBuilder.createQuery(SectionBean.class);
			Root<SectionBean> root = criteriaQuery.from(SectionBean.class);
			criteriaQuery.select(root).where(
		            criteriaBuilder.and(
		                criteriaBuilder.equal(root.get("subDivisionBean").get("id"), subDivisionId),
		                criteriaBuilder.equal(root.get("id"), currentSectionId)
		            )
		        );
			
			Query<SectionBean> query = session.createQuery(criteriaQuery);
			List<SectionBean> list = query.getResultList();
			
			for(SectionBean sectionBean : list) {
				
				sectionList.add(SectionValueBean.convertSectionBeanToSectionValueBean(sectionBean));
			}

			 dmFilter.setLstSections(sectionList);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
	}
	
	public String getCurrentSectionName() {
		String[] sectionIds = dmFilter.getSectionFilter();
		List<SectionValueBean> lstSections = dmFilter.getLstSections();
		
		List<String> currentSectionNames = new ArrayList<>();
		for(String sectionId : sectionIds) {
			for(SectionValueBean section : lstSections) {
				if(Integer.toString(section.getId()).equals(sectionId)) {
					currentSectionNames.add(section.getName());
	                break;
				}
			}
		}
		  String currentSectionNamesString = String.join(", ", currentSectionNames);
		    
		    return currentSectionNamesString;
	}

	public void showAction(ViewComplaintValueBean comp, int statusId) {
		try {
			comp.setStatusId(statusId);
			dm.setActionStatus(statusId);
			complaint = comp;
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}

	}

	public void showComplete(ViewComplaintValueBean comp) {
		try {
			ComplaintsDao dao = new ComplaintsDao();
			dm.setLstReasons(dao.getReasons(comp.getComplaintCode()));
			complaint = comp;
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}

	}

	public void listComplaints(int complaintCodeValue, int statusValue) {
		try {
			complaintNumber = "";
			consumerMobileNumber = "";
			complaintCode = complaintCodeValue;
			statusId = statusValue;
			System.err.println("OFFICE ID"+officer.officeId);
			ComplaintsDao daoComplaints = new ComplaintsDao();
			complaintList = daoComplaints.getComplaintList(officer, statusId, complaintCode);
			dm.setLstComplaintLog(new ArrayList<ComplaintLog>());
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
	}
	
	private  void listComplaints() throws Exception {
		
		ComplaintsDao daoComplaints = new ComplaintsDao();
		
		if(StringUtils.isNotBlank(complaintNumber)) {
			complaintList = daoComplaints.getComplaintById(Integer.parseInt(complaintNumber));
		} else if (StringUtils.isNotBlank(consumerMobileNumber)){
			complaintList = daoComplaints.getComplaintByConsumerMobileNumber(consumerMobileNumber);
		} else {
			complaintList = daoComplaints.getComplaintList(officer, statusId, complaintCode);
		}
		
		dm.setLstComplaintLog(new ArrayList<ComplaintLog>());
	}

	public static HttpServletResponse getResponse() {
		return (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
	}
	
	private List<Integer> getIdList(String[] idList){
		List<Integer> list = new ArrayList<Integer>();
		for (String id : idList) {
			list.add(Integer.parseInt(id));
		}
		return list;
	}
	
	private static String loadString(List<String> list) {
		String condition = "";
		for (String id : list) {
			if (StringUtils.isNotBlank(condition)) {
				condition += ", ";
			}
			condition += id;
		}
		return condition;
	}
	
	public void exportPdfReport() throws IOException {

		FacesContext context = FacesContext.getCurrentInstance();
		Document document = new Document();
		PdfWriter writer = null;
		OutputStream fos = null;
		PdfPCell cell = null;
		try {
			
			ComplaintsDao complaintsDao = new ComplaintsDao();
			
		    HttpServletResponse res = getResponse();
			 
			fos = res.getOutputStream();

			writer = PdfWriter.getInstance(document, fos);
			
	        document.open();
	        
	        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
			Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
			
			PdfPTable dateTable = new PdfPTable(2); // 2 columns.
			dateTable.setWidthPercentage(100); //Width 100%
			dateTable.setSpacingBefore(10f); //Space before table
			dateTable.setSpacingAfter(10f); //Space after table
			
			//Set Column widths
			dateTable.setWidths(new float[]{3, 1});
			
			cell = new PdfPCell(new Paragraph("Report Generated On", boldFont));
			dateTable.addCell(cell);
		    cell = new PdfPCell(new Paragraph(GeneralUtil.formatDate(new Date()), normalFont));
		    dateTable.addCell(cell);
		    
		    document.add(dateTable);
			
			PdfPTable headerTable = new PdfPTable(2); // 2 columns.
			headerTable.setWidthPercentage(100); //Width 100%
			headerTable.setSpacingBefore(10f); //Space before table
			headerTable.setSpacingAfter(10f); //Space after table
			
			//Set Column widths
			headerTable.setWidths(new float[]{1, 3});
			
			if(dmFilter.getRegionFilter() != null && dmFilter.getRegionFilter().length > 0) {
				
				List<RegionValueBean> regionList = complaintsDao.getRegionList(getIdList(dmFilter.getRegionFilter()));
				
				List<String> names = regionList.stream().map(RegionValueBean::getName).collect(Collectors.toList());
				
				cell = new PdfPCell(new Paragraph("Region", boldFont));
				headerTable.addCell(cell);
			    cell = new PdfPCell(new Paragraph(loadString(names), normalFont));
			    headerTable.addCell(cell);
			}
			
			
			if(dmFilter.getCircleFilter() != null && dmFilter.getCircleFilter().length > 0) {
				
				List<CircleValueBean> circleList = complaintsDao.getCircleList(getIdList(dmFilter.getCircleFilter()));
				
				List<String> names = circleList.stream().map(CircleValueBean::getName).collect(Collectors.toList());
				
				cell = new PdfPCell(new Paragraph("Circle", boldFont));
				headerTable.addCell(cell);
			    cell = new PdfPCell(new Paragraph(loadString(names), normalFont));
			    headerTable.addCell(cell);
			}
			
			
			if(dmFilter.getDivisionFilter() != null && dmFilter.getDivisionFilter().length > 0) {
				
				List<DivisionValueBean> divisionList = complaintsDao.getDivisionList(getIdList(dmFilter.getDivisionFilter()));
				
				List<String> names = divisionList.stream().map(DivisionValueBean::getName).collect(Collectors.toList());
				
				cell = new PdfPCell(new Paragraph("Division", boldFont));
				headerTable.addCell(cell);
			    cell = new PdfPCell(new Paragraph(loadString(names), normalFont));
			    headerTable.addCell(cell);
			}
			
			if(dmFilter.getSubDivisionFilter() != null && dmFilter.getSubDivisionFilter().length > 0) {
				
				List<SubDivisionValueBean> subDivisionList = complaintsDao.getSubDivisionList(getIdList(dmFilter.getSubDivisionFilter()));
				
				List<String> names = subDivisionList.stream().map(SubDivisionValueBean::getName).collect(Collectors.toList());
				
				cell = new PdfPCell(new Paragraph("Sub Division", boldFont));
				headerTable.addCell(cell);
			    cell = new PdfPCell(new Paragraph(loadString(names), normalFont));
			    headerTable.addCell(cell);
			}
			
			
			if(dmFilter.getSectionFilter() != null && dmFilter.getSectionFilter().length > 0) {
				
				List<SectionValueBean> sectionList = complaintsDao.getSectionList(getIdList(dmFilter.getSectionFilter()));
				
				List<String> names = sectionList.stream().map(SectionValueBean::getName).collect(Collectors.toList());
				
				cell = new PdfPCell(new Paragraph("Section", boldFont));
				headerTable.addCell(cell);
			    cell = new PdfPCell(new Paragraph(loadString(names), normalFont));
			    headerTable.addCell(cell);
			}
			
			if(dmFilter.getCompType() != null && dmFilter.getCompType().length > 0) {
				List<String> complaintTypeList = new ArrayList<String>();
				for (String code : dmFilter.getCompType()) {
					complaintTypeList.add(CCMSConstants.COMPLAINT_NAME[Integer.parseInt(code)]);
				}
				cell = new PdfPCell(new Paragraph("Complaint Type", boldFont));
				headerTable.addCell(cell);
			    cell = new PdfPCell(new Paragraph(loadString(complaintTypeList), normalFont));
			    headerTable.addCell(cell);
			}
			
			
			if(dmFilter.getStatuses() != null && dmFilter.getStatuses().length > 0) {
				List<String> statusList = new ArrayList<String>();
				for (String status : dmFilter.getStatuses()) {
					statusList.add(CCMSConstants.STATUSES[Integer.parseInt(status)]);
				}
				cell = new PdfPCell(new Paragraph("Status", boldFont));
				headerTable.addCell(cell);
			    cell = new PdfPCell(new Paragraph(loadString(statusList), normalFont));
			    headerTable.addCell(cell);
			}
			
			Date fromDate = dmFilter.getFromDate();
			Date toDate = dmFilter.getToDate();
			
			if (fromDate != null) {
				cell = new PdfPCell(new Paragraph("From Date", boldFont));
				headerTable.addCell(cell);
			    cell = new PdfPCell(new Paragraph(GeneralUtil.formatDate(fromDate), normalFont));
			    headerTable.addCell(cell);
			}
			
			if (toDate != null) {
				cell = new PdfPCell(new Paragraph("To Date", boldFont));
				headerTable.addCell(cell);
			    cell = new PdfPCell(new Paragraph(GeneralUtil.formatDate(toDate), normalFont));
			    headerTable.addCell(cell);
			}
			
			document.add(headerTable);
	 
	        PdfPTable table = new PdfPTable(4); // 4 columns.
	        table.setWidthPercentage(100); //Width 100%
	        table.setSpacingBefore(10f); //Space before table
	        table.setSpacingAfter(10f); //Space after table
	 
	        //Set Column widths
			table.setWidths(new float[]{2, 1, 1, 1});
			
			cell = new PdfPCell(new Paragraph("Complaint Type", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Pending", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("In Progress", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Completed", boldFont));
		    table.addCell(cell);
			
		    cell = new PdfPCell(new Paragraph(CCMSConstants.COMPLAINT_NAME[0], normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[0][0]), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[0][1]), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[0][2]), normalFont));
		    table.addCell(cell);
		    
		    
		    cell = new PdfPCell(new Paragraph(CCMSConstants.COMPLAINT_NAME[1], normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[1][0]), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[1][1]), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[1][2]), normalFont));
		    table.addCell(cell);
		    
		    
		    cell = new PdfPCell(new Paragraph(CCMSConstants.COMPLAINT_NAME[2], normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[2][0]), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[2][1]), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[2][2]), normalFont));
		    table.addCell(cell);
		    
		    
		    cell = new PdfPCell(new Paragraph(CCMSConstants.COMPLAINT_NAME[3], normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[3][0]), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[3][1]), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[3][2]), normalFont));
		    table.addCell(cell);
		    
		    
		    cell = new PdfPCell(new Paragraph(CCMSConstants.COMPLAINT_NAME[4], normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[4][0]), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[4][1]), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[4][2]), normalFont));
		    table.addCell(cell);
		    
		    
		    cell = new PdfPCell(new Paragraph(CCMSConstants.COMPLAINT_NAME[5], normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[5][0]), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[5][1]), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[5][2]), normalFont));
		    table.addCell(cell);
		    
		    
		    cell = new PdfPCell(new Paragraph(CCMSConstants.COMPLAINT_NAME[6], normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[6][0]), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[6][1]), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[6][2]), normalFont));
		    table.addCell(cell);
		    
		    cell = new PdfPCell(new Paragraph(CCMSConstants.COMPLAINT_NAME[7], normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[7][0]), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[7][1]), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(dm.arrReport[7][2]), normalFont));
		    table.addCell(cell);


	        document.add(table);
	        
			res.setContentType("application/vnd.ms-excel");
			res.setHeader("Content-disposition", "attachment; filename=Complaints.pdf");

			
			fos.flush();
	 
			context.responseComplete();
			context.renderResponse();
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to export."));
		} finally {
			if(document != null) {
				document.close();
			}
			if(writer != null) {
				writer.close();
			}
			if(fos != null) {
				fos.close();
			}
		}
		
	
	}
	
	public void searchComplaintCMD() {
		System.err.println("welcome to report::::::::::::::::");
		try {
			
			HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
	                .getExternalContext().getSession(false);
	        AdminUserValueBean adminUserValueBean = (AdminUserValueBean) httpsession.getAttribute("sessionAdminValueBean");
	        CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");

	        if(adminUserValueBean!=null) {
	        	int role= adminUserValueBean.getRoleId();
	        	
	        	if(role==1) {
	        		this.dmFilter.setRegionOneFilter(adminUserValueBean.getRegionId().toString());
	        		this.dmFilter.setCircleOneFilter(adminUserValueBean.getCircleId().toString());
	        		this.dmFilter.setDivisionOneFilter(adminUserValueBean.getDivisionId().toString());
	        		this.dmFilter.setSubDivisionOneFilter(adminUserValueBean.getSubDivisionId().toString());
	        		this.dmFilter.setSectionOneFilter(adminUserValueBean.getSectionId().toString());
	        	}
	        	else if (role==2) {
	        		this.dmFilter.setRegionOneFilter(adminUserValueBean.getRegionId().toString());
	        		this.dmFilter.setCircleOneFilter(adminUserValueBean.getCircleId().toString());
	        		this.dmFilter.setDivisionOneFilter(adminUserValueBean.getDivisionId().toString());
	        		this.dmFilter.setSubDivisionOneFilter(adminUserValueBean.getSubDivisionId().toString());
	        	}
	        	else if(role==3) {
	        		this.dmFilter.setRegionOneFilter(adminUserValueBean.getRegionId().toString());
	        		this.dmFilter.setCircleOneFilter(adminUserValueBean.getCircleId().toString());
	        		this.dmFilter.setDivisionOneFilter(adminUserValueBean.getDivisionId().toString());
	        	}
	        	else if(role==4) {
	        		this.dmFilter.setRegionOneFilter(adminUserValueBean.getRegionId().toString());
	        		this.dmFilter.setCircleOneFilter(adminUserValueBean.getCircleId().toString());
	        	}
	        	else if(role==5) {
	        		this.dmFilter.setRegionOneFilter(adminUserValueBean.getRegionId().toString());
	        	}
	        }
	        else if(callCenterValueBean!=null && callCenterValueBean.getRoleId()==1) {
	  	    	  
	  	    	  System.err.println("ENTERED FILTER LOGIC");
	  	    	  int userId= callCenterValueBean.getId();
	  	    	  SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
	  	    	  Session session = sessionFactory.openSession();
	                try {
	                    session.beginTransaction();

	                    @SuppressWarnings("unchecked")
	  					List<Integer> circleId = session.createQuery(
	                            "select c.circleBean.id from CallCenterMappingBean c " +
	                            "where c.callCenterUserBean.id = :userId")
	                            .setParameter("userId", userId)
	                            .getResultList();
	                    
	                    
	                    @SuppressWarnings("unchecked")
	  					List<Integer> regionId = session.createQuery(
	                            "select DISTINCT c.circleBean.regionBean.id from CallCenterMappingBean c " +
	                            "where c.callCenterUserBean.id = :userId")
	                            .setParameter("userId", userId)
	                            .getResultList();
	                    
	                    this.dmFilter.setRegionOneFilter(regionId.get(0).toString());
	                    
	                    if(circleId.size()==1) {
	                    	this.dmFilter.setCircleOneFilter(circleId.get(0).toString());
	                    }

	                } finally {
	                    session.close();
	                }
	  	      
	        }
	        
			boolean validSearch = validateSearchFormCMD(dmFilter);
			
			if(validSearch) {
				
				System.err.println("VALID FOR DATE:;;;;;;");
				Date fromDate = dmFilter.getFromDate();
				Date toDate = dmFilter.getToDate();
				
				if (fromDate != null && toDate != null) {
					if(fromDate.after(toDate)) {
						validSearch = false;
						FacesContext.getCurrentInstance().addMessage(null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "To date can't be lesser than from date!"));
					}
				}
				
				if(validSearch) {
					System.err.println("VALID FOR FILTER:;;;;;;");
					ComplaintsDao daoComplaints = new ComplaintsDao();
					//viewComplaintReportList = daoComplaints.searchComplaint(dmFilter, -1, -1);
					lazyComplaintDataModel = new ComplaintLazyDataModel(dmFilter,-1,-1);
					
					
					dm.arrReport = daoComplaints.getReportDashBoardCMD(dmFilter);
					
					if (dm.arrReport[0][0] == -1) {
						dm.arrReport[0][0] = 0;
					}
				}
				
			} else {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Please select a place for search."));
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
		
	}
	
	public void searchComplaint() {
			
		try {
			boolean validSearch = validateSearchForm(dmFilter);
			
			if(validSearch) {
				
				Date fromDate = dmFilter.getFromDate();
				Date toDate = dmFilter.getToDate();
				
				if (fromDate != null && toDate != null) {
					if(fromDate.after(toDate)) {
						validSearch = false;
						FacesContext.getCurrentInstance().addMessage(null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "To date can't be lesser than from date!"));
					}
				}
				
				if(validSearch) {
					
					ComplaintsDao daoComplaints = new ComplaintsDao();
					viewComplaintReportList = daoComplaints.searchComplaint(dmFilter, -1, -1);
					//lazyComplaintDataModel = new ComplaintLazyDataModel(dmFilter,-1,-1);
					
					
					dm.arrReport = daoComplaints.getReportDashBoard(dmFilter);
					
					if (dm.arrReport[0][0] == -1) {
						dm.arrReport[0][0] = 0;
					}
				}
				
			} else {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Please select a place for search."));
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
		
	}
	
	public void searchComplaintAll() {
		
		try {
			
					ComplaintsDao daoComplaints = new ComplaintsDao();
					//viewComplaintReportList = daoComplaints.searchComplaint(dmFilter, -1, -1);
					
					lazyComplaintDataModel = new ComplaintLazyDataModel(dmFilter,-1,-1);
					
					
					dm.arrReport = daoComplaints.getReportDashBoardAll(dmFilter);
					
					if (dm.arrReport[0][0] == -1) {
						dm.arrReport[0][0] = 0;
					}
				
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
		
	}
	
//	public void generateExcelForPreview() {
//	    try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
//	        Sheet sheet = workbook.createSheet("Complaint Report");
//
//	        // Create header row
//	        Row headerRow = sheet.createRow(0);
//	        String[] headers = {
//	            "S.No", "Complaint ID", "Complaint Date", "Service Number",
//	            "Contact No", "Type", "Description", "Status",
//	            "Attended Date", "Attended Remarks", "Feedback",
//	            "Region", "Circle Name", "Division", "Sub Division", "Section Name", "Received From"
//	        };
//
//	        for (int i = 0; i < headers.length; i++) {
//	            Cell cell = headerRow.createCell(i);
//	            cell.setCellValue(headers[i]);
//	        }
//
//	        // Populate data rows
//	        if (viewComplaintReportList != null) {
//	            int rowNum = 1;
//	            for (ViewComplaintReportValueBean comp : viewComplaintReportList) {
//	                Row row = sheet.createRow(rowNum++);
//	                row.createCell(0).setCellValue(comp.getSerialNo() != null ? comp.getSerialNo() : 0);
//	                row.createCell(1).setCellValue(comp.getId() != null ? comp.getId() : 0);
//	                row.createCell(2).setCellValue(comp.getCreatedOnFormatted() != null ? comp.getCreatedOnFormatted() : "N/A");
//	                row.createCell(3).setCellValue(comp.getServiceNumber() != null ? comp.getServiceNumber() : "N/A");
//	                row.createCell(4).setCellValue(comp.getMobile() != null ? comp.getMobile() : "N/A");
//	                row.createCell(5).setCellValue(comp.getComplaintCodeValue() != null ? comp.getComplaintCodeValue() : "N/A");
//	                row.createCell(6).setCellValue(comp.getRemarks() != null ? comp.getRemarks() : "N/A");
//	                row.createCell(7).setCellValue(comp.getStatusValue() != null ? comp.getStatusValue() : "N/A");
//	                row.createCell(8).setCellValue(comp.getUpdatedOnFormatted() != null ? comp.getUpdatedOnFormatted() : "N/A");
//	                row.createCell(9).setCellValue(comp.getCompletedRemarks() != null ? comp.getCompletedRemarks() : "N/A");
//	                row.createCell(10).setCellValue(comp.getFeedbackEmojiAndText() != null ? comp.getFeedbackEmojiAndText() : "N/A");
//	                row.createCell(11).setCellValue(comp.getRegionName() != null ? comp.getRegionName() : "N/A");
//	                row.createCell(12).setCellValue(comp.getCircleName() != null ? comp.getCircleName() : "N/A");
//	                row.createCell(13).setCellValue(comp.getDivisionName() != null ? comp.getDivisionName() : "N/A");
//	                row.createCell(14).setCellValue(comp.getSubDivisionName() != null ? comp.getSubDivisionName() : "N/A");
//	                row.createCell(15).setCellValue(comp.getSectionName() != null ? comp.getSectionName() : "N/A");
//	                row.createCell(16).setCellValue(comp.getDevice() != null ? comp.getDevice() : "N/A");
//	            }
//
//	            // Auto-size columns
//	            for (int i = 0; i < headers.length; i++) {
//	                sheet.autoSizeColumn(i);
//	            }
//	        } else {
//	            logger.error("viewComplaintReportList is null");
//	            return;
//	        }
//
//	        // Write data to byteArrayOutputStream
//	        workbook.write(byteArrayOutputStream);
//
//	        // Send the Excel data as byte array to the frontend
//	        FacesContext context = FacesContext.getCurrentInstance();
//	        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
//	        response.setContentType("application/octet-stream");
//	        response.setHeader("Content-Disposition", "inline; filename=Complaint_Report.xlsx");
//	        response.getOutputStream().write(byteArrayOutputStream.toByteArray());
//	        context.responseComplete();
//	    } catch (Exception e) {
//	        logger.error(ExceptionUtils.getStackTrace(e));
//	    }
//	}


	
	public void searchComplaintForReport() {
		
		try {

				ComplaintsDao daoComplaints = new ComplaintsDao();
					
				complaintValueBean = daoComplaints.getReportTotal(dmFilter);
					
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
		
	}
	
	
	
	private boolean validateSearchForm(DataModel dataModel) {
		
		boolean validSearch = false;
		
		if ((dataModel.getRegionFilter() != null && dataModel.getRegionFilter().length > 0)
				|| (dataModel.getCircleFilter() != null && dataModel.getCircleFilter().length > 0) || (dataModel.getDivisionFilter() != null && dataModel.getDivisionFilter().length > 0)  || 
				(dataModel.getSubDivisionFilter() != null && dataModel.getSubDivisionFilter().length > 0) || (dataModel.getSectionFilter() != null && dataModel.getSectionFilter().length > 0)) {
			
			validSearch = true;
		}
		
		return validSearch;
	}
	
	private boolean validateSearchFormCMD(DataModel dataModel) {
		
		boolean validSearch = false;
		
		if (dataModel.getRegionOneFilter() != null) {
			
			validSearch = true;
		}
		
		return validSearch;
	}
	
	public void listReportComplaints(int complaintCodeValue, int statusValue) {
		try {
			System.err.println("DASHBOARD++++++++++++");
			ComplaintsDao daoComplaints = new ComplaintsDao();
			//viewComplaintReportList = daoComplaints.searchComplaint(dmFilter, complaintCodeValue, statusValue);
			lazyComplaintDataModel = new ComplaintLazyDataModel(dmFilter,complaintCodeValue,statusValue);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
	}
	
	public void takeAction() {
		
		SessionFactory factory = null;
		Session session = null;
		Transaction transaction = null;
		ComplaintBean complaintBean = null;

		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			Timestamp updatedOn = new Timestamp(new Date().getTime());
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<ComplaintBean> criteriaQuery = criteriaBuilder.createQuery(ComplaintBean.class);
			Root<ComplaintBean> root = criteriaQuery.from(ComplaintBean.class);
			
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), complaint.getId()));

			Query<ComplaintBean> query = session.createQuery(criteriaQuery).setMaxResults(1);
			
			try {
				complaintBean = query.getSingleResult();
			} catch (NoResultException nre) {
				System.out.println("No Complaint Found for the given complaint id.");
				logger.info("No Complaint Found for the given complaint id.");
			}
			
			if(complaintBean != null) {
				complaintBean.setStatusId(dm.getActionStatus());

				complaintBean.setUpdatedOn(updatedOn);

				AdminUserBean adminUserBean = new AdminUserBean();
				adminUserBean.setId(officer.userId);
				
				ComplaintHistoryBean complaintHistoryBean = new ComplaintHistoryBean();
				complaintHistoryBean.setComplaintBean(complaintBean);
				complaintHistoryBean.setStatusId(dm.getActionStatus());
				complaintHistoryBean.setDescription(description);
				complaintHistoryBean.setSectionBean(complaintBean.getSectionBean());
				complaintHistoryBean.setRegionBean(complaintBean.getRegionBean());
				complaintHistoryBean.setCircleBean(complaintBean.getCircleBean());
				complaintHistoryBean.setDivisionBean(complaintBean.getDivisionBean());
				complaintHistoryBean.setSubDivisionBean(complaintBean.getSubDivisionBean());
				complaintHistoryBean.setAdminUserBean(adminUserBean);
				
				complaintBean.addToHistory(complaintHistoryBean);

				transaction = session.beginTransaction();
				session.saveOrUpdate(complaintBean);
				transaction.commit();
				session.refresh(complaintBean);

				HibernateUtil.closeSession(factory, session);
				
				updateDashBoardFilterComplaint();
				
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Complaint updated successfully."));
				
			} else {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "No Complaint Found for the given complaint id."));
			}

		} catch (Exception e) {
			if(transaction != null) {
				transaction.rollback();
			}
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to update your complaint."));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		
		PrimeFaces.current().executeScript("PF('dlgTakeAction').hide()");

	}

	public void closeComplaint() {

		SessionFactory factory = null;
		Session session = null;
		Transaction transaction = null;
		ComplaintBean complaintBean = null;

		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			Timestamp updatedOn = new Timestamp(new Date().getTime());
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<ComplaintBean> criteriaQuery = criteriaBuilder.createQuery(ComplaintBean.class);
			Root<ComplaintBean> root = criteriaQuery.from(ComplaintBean.class);
			
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), complaint.getId()));

			Query<ComplaintBean> query = session.createQuery(criteriaQuery).setMaxResults(1);
			
			try {
				complaintBean = query.getSingleResult();
			} catch (NoResultException nre) {
				System.out.println("No Complaint Found for the given complaint id.");
				logger.info("No Complaint Found for the given complaint id.");
			}
			
			if(complaintBean != null) {
				
				CriteriaBuilder criteriaBuilderFieldWorker = session.getCriteriaBuilder();
				CriteriaQuery<FieldWorkerComplaintBean> criteriaQueryFieldWorker = criteriaBuilderFieldWorker.createQuery(FieldWorkerComplaintBean.class);
				Root<FieldWorkerComplaintBean> rootFieldWorker = criteriaQueryFieldWorker.from(FieldWorkerComplaintBean.class);
				
				criteriaQueryFieldWorker.select(rootFieldWorker).where(criteriaBuilderFieldWorker.equal(rootFieldWorker.get("complaintBean").get("id"), complaintBean.getId()));

				Query<FieldWorkerComplaintBean> queryFieldWorker = session.createQuery(criteriaQueryFieldWorker);
				
				List<FieldWorkerComplaintBean> fieldWorkerComplaintBeanList = queryFieldWorker.getResultList();
				
				for(FieldWorkerComplaintBean fieldWorkerComplaintBean :fieldWorkerComplaintBeanList) {
					fieldWorkerComplaintBean.setStatusId(CCMSConstants.COMPLETED);
					session.update(fieldWorkerComplaintBean);
				}
				
//				if(officer.roleId == 1 && complaintBean.getDevice().equals("MI")) {
//					
//					complaintBean.setStatusId(CCMSConstants.IN_PROGRESS);
//					complaintBean.setIntUpdatedOn(updatedOn);
//					
//				}else {
					complaintBean.setStatusId(CCMSConstants.COMPLETED);
				//}
				complaintBean.setUpdatedOn(updatedOn);
				
				AdminUserBean adminUserBean = new AdminUserBean();
				adminUserBean.setId(officer.userId);
				ClosureReasonValueBean closureReasonValueBean = new ClosureReasonValueBean();
				ComplaintHistoryBean complaintHistoryBean = new ComplaintHistoryBean();
				complaintHistoryBean.setComplaintBean(complaintBean);
				
//				if(officer.roleId == 1 && complaintBean.getDevice().equals("MI")) {
//					
//					complaintHistoryBean.setStatusId(CCMSConstants.IN_PROGRESS);
//				}else {
					complaintHistoryBean.setStatusId(CCMSConstants.COMPLETED);
				//}
				complaintHistoryBean.setDescription(descriptionComplete);
				complaintHistoryBean.setSectionBean(complaintBean.getSectionBean());
				complaintHistoryBean.setRegionBean(complaintBean.getRegionBean());
				complaintHistoryBean.setCircleBean(complaintBean.getCircleBean());
				complaintHistoryBean.setDivisionBean(complaintBean.getDivisionBean());
				complaintHistoryBean.setSubDivisionBean(complaintBean.getSubDivisionBean());
				complaintHistoryBean.setAdminUserBean(adminUserBean);
				//complaintHistoryBean.setReason(dm.);
				 String selectedReasonLabel;
				 Integer selectedId = dm.getSections().getId();
			        System.out.println("Selected Reason ID: " + selectedId);

			        // Find and store the human-readable label
			        selectedReasonLabel = dm.getLstReasons().stream()
			            .filter(reason -> reason.getComplaintCode().equals(selectedId))
			            .map(ClosureReasonValueBean::getClosureReason)
			            .findFirst()
			            .orElse("Unknown Reason");
			        
				System.err.println("CLOSE REASON"+selectedReasonLabel);
				complaintHistoryBean.setReason(selectedReasonLabel);
				
				complaintBean.addToHistory(complaintHistoryBean);

				transaction = session.beginTransaction();
				session.saveOrUpdate(complaintBean);
				transaction.commit();
				session.refresh(complaintBean);
				System.err.println("COMPLAINT ID"+complaintBean.getId());
				System.err.println("DEVICE"+complaintBean.getDevice());
				
				 String contactNo;
				if(complaintBean.getDevice().equals("MI"))
				{
					System.err.println("MINAGAM::::::::::");
					 CriteriaBuilder criteriaBuilders = session.getCriteriaBuilder();
			            CriteriaQuery<CompContactMap> criteriaQuerys = criteriaBuilders.createQuery(CompContactMap.class);
			            Root<CompContactMap> roots = criteriaQuerys.from(CompContactMap.class);
			            
			            criteriaQuerys.select(roots)
			            						.where(criteriaBuilders.equal(roots.get("complaint"), complaintBean.getId()));

			            Query<CompContactMap> querys = session.createQuery(criteriaQuerys);
			            CompContactMap comContactMap = querys.uniqueResult(); 
			            
			           
			            if (comContactMap != null) {
			               contactNo = comContactMap.getContactNo(); // ✅ Assuming this method exists
			                System.err.println("Contact No: " + contactNo);
			                FeedbackCaller.sendFeedback(contactNo, complaintBean.getId()); 
			                //feebackCallApi(contactNo ,complaintBean.getId());
			                
//			                String message = "CmplNo:" + complaintBean.getId() + ":Registered by you is Attended and Closed "
//									+ GeneralUtil.formatDateWithTime(new Date()) + " Save Electricity - TANGEDCO";
//							
//							String smsId = SMSUtil.sendSMS(null, comContactMap.getContactNo(), message);
//							SmsClient.sendSms(smsId);
							
			            } 
			            
				}
			     else {
			            	contactNo = complaintBean.getPublicUserBean().getMobile();
			            	 String message = "CmplNo:" + complaintBean.getId() + ":Registered by you is Attended and Closed "
										+ GeneralUtil.formatDateWithTime(new Date()) + " Save Electricity - TANGEDCO";
								
								String smsId = SMSUtil.sendSMS(null, contactNo, message);
								SmsClient.sendSms(smsId);
								
			            }
			            

			            
			           
						
						PrimeFaces.current().executeScript("PF('messageDialogalreadyexists').show(); setTimeout(function() { PF('messageDialog').hide(); }, 5000);");
			    		
			        	PrimeFaces.current().executeScript("setTimeout(function(){ window.location.href = '" +
							    FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() +
							    "/faces/admin//dashboard.xhtml'; }, 5000);");
				
				
				System.err.println("MOBILE NO NULL::::::::::::;");
				

				HibernateUtil.closeSession(factory, session);

				updateDashBoardFilterComplaint();
//				
//				PrimeFaces.current().executeScript("PF('messageDialogalreadyexists').show(); setTimeout(function() { PF('messageDialog').hide(); }, 5000);");
//	    		
//	        	PrimeFaces.current().executeScript("setTimeout(function(){ window.location.href = '" +
//					    FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() +
//					    "/faces/admin//dashboard.xhtml'; }, 5000);");
				
//				FacesContext.getCurrentInstance().addMessage(null,
//						new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Complaint closed successfully."));
			} else {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "No Complaint Found for the given complaint id."));
			}

			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to close your complaint."));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

		PrimeFaces.current().executeScript("PF('dlgCloseComplaint').hide()");

	}

	
	public void feebackCallApis() {
		
//		System.err.println("CONTACT NO:::::::::::"+phone_number);
//		System.err.println("COMPLAINT ID:::::::::::"+complaint_id);
		
		try {
			
			String phone_number ="7550392550";
			String source ="api";
			Integer complaint_id =1;
			 // URL of your local or external HTTP API
            String dooctiUrl = "http://localhost:8080/tangedco-admin/api/feedback";
            URL url = new URL(dooctiUrl);

            // Create connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Set method to POST
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");

            // If Authorization is required:
            conn.setRequestProperty("Authorization", "xxxxxx");

            // Enable writing to output stream
            conn.setDoOutput(true);

            String jsonInputString = "{"
                    + "\"fields\": {"
                    + "\"phone_number\": \"" + phone_number + "\","
                    + "\"source\": \"" + source + "\","
                    + "\"complaint_id\": " + complaint_id
                    + "}"
                    + "}";


            // Send request body
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read response code
            int responseCode = conn.getResponseCode();
            System.out.println("HTTP Response Code: " + responseCode);

            // Read response body
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
                System.out.println("Response Body: " + response);
            }
	          
	      } catch (IOException e) {
	          System.err.println("Error calling Doocti API: " + e.getMessage());
	          e.printStackTrace();
	      }
	}
	public void saveSection() {
		System.out.println(dm.getSections().getId());
		if(dm.getSections() != null && dm.getSections().getId() != null) {
			sectionId = dm.getSections().getId();
		}
	}

	public void updateDashBoard() {
		try {
			loadDashBoardData();
			ComplaintsDao daoComplaints = new ComplaintsDao();
			complaintList = daoComplaints.getComplaintList(officer);
			dm.setLstComplaintLog(new ArrayList<ComplaintLog>());
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
	}
	
	private void loadDashBoardData() {
		if (dmFilter.getRegions() != null && dmFilter.getRegions().getId() != null) {
			officer.fieldName = CCMSConstants.OFFICE_NAME[4];
			officer.regionId = dmFilter.getRegions().getId();
			officer.officeId = dmFilter.getRegions().getId();
		}
		if (dmFilter.getCircles() != null &&dmFilter.getCircles().getId() != null) {
			officer.fieldName = CCMSConstants.OFFICE_NAME[3];
			officer.circleId = dmFilter.getCircles().getId();
			officer.officeId = dmFilter.getCircles().getId();
		}
		if (dmFilter.getDivisions() != null &&dmFilter.getDivisions().getId() != null) {
			officer.fieldName = CCMSConstants.OFFICE_NAME[2];
			officer.divisionId = dmFilter.getDivisions().getId();
			officer.officeId = dmFilter.getDivisions().getId();
		}
		if (dmFilter.getSubDivisions() != null &&dmFilter.getSubDivisions().getId() != null) {
			officer.fieldName = CCMSConstants.OFFICE_NAME[1];
			officer.subDivisionId = dmFilter.getSubDivisions().getId();
			officer.officeId = dmFilter.getSubDivisions().getId();
		}
		if (dmFilter.getSections() != null &&dmFilter.getSections().getId() != null) {
			officer.fieldName = CCMSConstants.OFFICE_NAME[0];
			officer.sectionId = dmFilter.getSections().getId();
			officer.officeId = dmFilter.getSections().getId();
		}
		ComplaintsDao daoComplaints = new ComplaintsDao();

		dm.arrDash = daoComplaints.getDashboardData(officer);
		if (dm.arrDash[0][0] == -1) {
			dm.arrDash[0][0] = 0;
		}
	}
	
	private void updateDashBoardFilterComplaint() throws Exception {
		loadDashBoardData();
		listComplaints();
		dm.setLstComplaintLog(new ArrayList<ComplaintLog>());
	}

	public void transferComplaint() {
		
		SessionFactory factory = null;
		Session session = null;
		Transaction transaction = null;
		ComplaintBean complaintBean = null;
		

		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			CompTransferBean compTransferBean = new CompTransferBean();
			AdminUserBean selectadminUserBean = new AdminUserBean();
			
			Timestamp updatedOn = new Timestamp(new Date().getTime());
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<ComplaintBean> criteriaQuery = criteriaBuilder.createQuery(ComplaintBean.class);
			Root<ComplaintBean> root = criteriaQuery.from(ComplaintBean.class);
			
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), complaint.getId()));

			Query<ComplaintBean> query = session.createQuery(criteriaQuery).setMaxResults(1);
			
			try {
				complaintBean = query.getSingleResult();
			} catch (NoResultException nre) {
				System.out.println("No Complaint Found for the given complaint id.");
				logger.info("No Complaint Found for the given complaint id.");
			}
			
			if(complaintBean != null) {
				
				AdminUserBean adminUserBean = new AdminUserBean();
				adminUserBean.setId(officer.userId);
				
				ComplaintHistoryBean complaintHistory = new ComplaintHistoryBean();
				ComplaintHistoryBean complaintHistoryTransfer = new ComplaintHistoryBean();
				
				complaintBean.setStatusId(dm.getActionStatus());
				
				RegionBean regionBean = new RegionBean();
				if(dm.getRegions() != null && dm.getRegions().getId() != null) {
					regionBean.setId(dm.getRegions().getId());
					complaintBean.setRegionBean(regionBean);
					complaintHistory.setRegionBean(regionBean);
					complaintHistoryTransfer.setRegionBean(regionBean);
				}
				
				CircleBean circleBean = new CircleBean();
				if(dm.getCircles() != null && dm.getCircles().getId() != null) {
					circleBean.setId(dm.getCircles().getId());
					complaintBean.setCircleBean(circleBean);
					complaintHistory.setCircleBean(circleBean);
					complaintHistoryTransfer.setCircleBean(circleBean);
				}
				
				DivisionBean divisionBean = new DivisionBean();
				if(dm.getDivisions() != null && dm.getDivisions().getId() != null) {
					divisionBean.setId(dm.getDivisions().getId());
					complaintBean.setDivisionBean(divisionBean);
					complaintHistory.setDivisionBean(divisionBean);
					complaintHistoryTransfer.setDivisionBean(divisionBean);
				}
				
				SubDivisionBean subDivisionBean = new SubDivisionBean();
				if(dm.getSubDivisions() != null && dm.getSubDivisions().getId() != null) {
					subDivisionBean.setId(dm.getSubDivisions().getId());
					complaintBean.setSubDivisionBean(subDivisionBean);
					complaintHistory.setSubDivisionBean(subDivisionBean);
					complaintHistoryTransfer.setSubDivisionBean(subDivisionBean);
				}
				
				SectionBean sectionBean = new SectionBean();
				if(sectionId > 0) {
					sectionBean.setId(sectionId);
					complaintBean.setSectionBean(sectionBean);
				}
				
				complaintBean.setUpdatedOn(updatedOn);

				complaintHistory.setAdminUserBean(adminUserBean);
				complaintHistory.setComplaintBean(complaintBean);
				complaintHistory.setStatusId(dm.getActionStatus());
				complaintHistory.setDescription(descriptionTransfer);
				
				complaintHistoryTransfer.setAdminUserBean(adminUserBean);
				complaintHistoryTransfer.setComplaintBean(complaintBean);
				complaintHistoryTransfer.setStatusId(dm.getActionStatus());
				complaintHistoryTransfer.setDescription("Compliant transferred by "+officer.getUserName()+" and transferred on "+GeneralUtil.formatDate(new Date()));
				
				
				complaintBean.addToHistory(complaintHistory);
				complaintBean.addToHistory(complaintHistoryTransfer);

                 /* START COMP TRANSFER SAVE*/
				
				ComplaintBean compId = new ComplaintBean();
				compId.setId(complaintBean.getId());
				compTransferBean.setComplaint(compId);
				
				String ipAddress = null;
		        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest(); 
		        ipAddress = request.getHeader("X-FORWARDED-FOR");
		        if (ipAddress == null) {
		            ipAddress = request.getRemoteAddr(); 
		        }
		        
				compTransferBean.setIpid(ipAddress);
				
				compTransferBean.setRemarks(descriptionTransfer);
				compTransferBean.setTransferOn(new Timestamp(System.currentTimeMillis()));
				
				selectadminUserBean.setUserName(officer.userName);
				
			    compTransferBean.setTransferUser(selectadminUserBean.getUserName());
				
				/* END COMP TRANSFER SAVE*/
				
				transaction = session.beginTransaction();
				session.saveOrUpdate(complaintBean);
				session.save(compTransferBean);
				transaction.commit();
				session.refresh(complaintBean);
				session.refresh(compTransferBean);

				HibernateUtil.closeSession(factory, session);
				
				updateDashBoardFilterComplaint();
				
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Complaint transferred successfully."));
				
			} else {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "No Complaint Found for the given complaint id."));
			}
			
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to transfer your complaint."));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

		PrimeFaces.current().executeScript("PF('dlgTransfer').hide()");

	}
	
	
	public void getUsersList() {
		
		try {
			UserDao dao =  new UserDao();
			
			setUserList(dao.getUsersList(dm));
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}

	}
	
	public String addUser(DataModel dataModel) {
		
		String path = "admin/manage/adduser";
		
		String errorMessage = validateUser(dataModel.getAdminUsers().getUserName());
		
		if(StringUtils.isNotBlank(errorMessage)){
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warn", errorMessage));
			return path;
		}
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			AdminUserBean adminUser = new AdminUserBean();
			
			RoleBean roleBean =  new RoleBean();
			roleBean.setId(dataModel.getRoles().getId());
			adminUser.setRoleBean(roleBean);
			
			RegionBean regionBean =  new RegionBean();
			regionBean.setId(dataModel.getRegions().getId());
			adminUser.setRegionBean(regionBean);
			
			CircleBean circleBean =  new CircleBean();
			circleBean.setId(dataModel.getCircles().getId());
			adminUser.setCircleBean(circleBean);
			
			
			if(dataModel.getDivisions() != null && dataModel.getDivisions().getId() != null) {
				
				DivisionBean divisionBean =  session.load(DivisionBean.class, dataModel.getDivisions().getId());
				adminUser.setDivisionBean(divisionBean);
				adminUser.setOfficeName(divisionBean.getName());
				
			} 
			
			
			
			if(dataModel.getSubDivisions() != null && dataModel.getSubDivisions().getId() != null) {
				
				SubDivisionBean subDivisionBean =  session.load(SubDivisionBean.class, dataModel.getSubDivisions().getId());
				adminUser.setSubDivisionBean(subDivisionBean);
				adminUser.setOfficeName(subDivisionBean.getName());
				
			} 
			
			if(dataModel.getSections() != null && dataModel.getSections().getId() != null) {
				
				SectionBean sectionBean =  session.load(SectionBean.class, dataModel.getSections().getId());
				adminUser.setSectionBean(sectionBean);
				adminUser.setOfficeName(sectionBean.getName());
				
			} 
			
			adminUser.setMobile(dataModel.getAdminUsers().getMobile());
			adminUser.setEmail(dataModel.getAdminUsers().getUserName()+"@tnebnet.org");
			adminUser.setUserName(dataModel.getAdminUsers().getUserName());
			adminUser.setPassword(dataModel.getAdminUsers().getPassword());

			Transaction transaction = session.beginTransaction();
			session.save(adminUser);
			transaction.commit();
			session.refresh(adminUser);
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info","User created successfully."));
			
			dm.getRoles().setId(0);
			dm.getCircles().setId(0);
			dm.getDivisions().setId(0);
			dm.getSubDivisions().setId(0);
			dm.getSections().setId(0);
			dm.getAdminUsers().setMobile("");
			dm.getAdminUsers().setUserName("");
			dm.getAdminUsers().setPassword("");
			
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

		
		return path;
	}

	public void deleteUser(AdminUserValueBean userValueBean) {

		try {
			
			UserDao dao = new UserDao();

			dao.deleteUser(userValueBean);

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "User deleted successfully."));
			
			List<AdminUserValueBean> adminUserList = new ArrayList<AdminUserValueBean>();
			
			for (AdminUserValueBean adminUserValueBean : userList) {
				if(adminUserValueBean.getId() != userValueBean.getId()) {
					adminUserList.add(adminUserValueBean);
				}
			}
			
			userList = adminUserList;

		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
	}
	
	public String validateUser(String userName) {
		
		String errorMessage = null;
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<AdminUserBean> criteriaQuery = criteriaBuilder.createQuery(AdminUserBean.class);
			Root<AdminUserBean> root = criteriaQuery.from(AdminUserBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("userName"), userName));

			Query<AdminUserBean> query = session.createQuery(criteriaQuery);
			List<AdminUserBean> userList = query.getResultList();

			if (userList.size() > 0) {
				errorMessage = "User already registered with this user name.";
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		
		return errorMessage;
	}
	
	public void searchByComplaintNumber() {
		try {
			consumerMobileNumber = "";
			if(StringUtils.isBlank(complaintNumber) || !GeneralUtil.validateNumber(complaintNumber)) {
				complaintList = new ArrayList<ViewComplaintValueBean>();
				FacesContext.getCurrentInstance().addMessage(null, 
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Warn", "Please enter valid complaint number."));
			} else {
//				ComplaintsDao daoComplaints = new ComplaintsDao();
//				complaintList = daoComplaints.getComplaintById(Integer.parseInt(complaintNumber));
//				dm.setLstComplaintLog(new ArrayList<ComplaintLog>());
//				if(complaintList.size() == 0) {
//					FacesContext.getCurrentInstance().addMessage(null, 
//							new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "No Complaint Found for the given complaint id."));
//				}
				try {
				    ComplaintsDao daoComplaints = new ComplaintsDao();
				    complaintList = daoComplaints.getComplaintById(Integer.parseInt(complaintNumber));
				    dm.setLstComplaintLog(new ArrayList<ComplaintLog>());
				    if (complaintList.size() == 0) {
				        FacesContext.getCurrentInstance().addMessage(null, 
				            new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "No Complaint Found for the given complaint id."));
				    }
				} catch (Exception e) {
				    FacesContext.getCurrentInstance().addMessage(null,
				        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to load complaint details."));
				    logger.error("Error loading complaint by ID: " + complaintNumber, e);
				}

			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
	} 
	
	public void searchByConsumerMobileNumber() {
		try {
			complaintNumber = "";
			if(StringUtils.isBlank(consumerMobileNumber) ||  !GeneralUtil.validateMobileNumber(consumerMobileNumber)) {
				complaintList = new ArrayList<ViewComplaintValueBean>();
				FacesContext.getCurrentInstance().addMessage(null, 
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Warn", "Please enter valid consumer mobile number."));
			} else {
				try {
				    ComplaintsDao daoComplaints = new ComplaintsDao();
				    System.err.println("CONSUMNER MOBILE NO"+consumerMobileNumber);
				    complaintList = daoComplaints.getComplaintByConsumerMobileNumber(consumerMobileNumber);
				    dm.setLstComplaintLog(new ArrayList<ComplaintLog>());
				    for (ViewComplaintValueBean complaint : complaintList) {
				        System.err.println("Complaint ID: " + complaint.getComplaintCode());
				        System.err.println("Consumer Name: " + complaint.getMobile());
				      
				        System.err.println("-------------------------");
				    }
				    if (complaintList.isEmpty()) {
				        FacesContext.getCurrentInstance().addMessage(null,
				            new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "No Complaint Found for the given consumer mobile number."));
				    }
				} catch (Exception e) {
				    FacesContext.getCurrentInstance().addMessage(null,
				        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to load complaint details."));
				    logger.error("Error loading complaints for mobile: " + consumerMobileNumber, e);
				}

			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
	} 

	
	public ComplaintValueBean getSelectedComplaintId() {
		return selectedComplaintId;
	}

	public void setSelectedComplaintId(ComplaintValueBean selectedComplaintId) {
		this.selectedComplaintId = selectedComplaintId;
	}

	public CompContactMapValueBean getSelectedComplaintMobileNumber() {
		return selectedComplaintMobileNumber;
	}

	public void setSelectedComplaintMobileNumber(CompContactMapValueBean selectedComplaintMobileNumber) {
		this.selectedComplaintMobileNumber = selectedComplaintMobileNumber;
	}

	public ComplaintLazyDataModel getLazyComplaintDataModel() {
		return lazyComplaintDataModel;
	}

	public void setLazyComplaintDataModel(ComplaintLazyDataModel lazyComplaintDataModel) {
		this.lazyComplaintDataModel = lazyComplaintDataModel;
	}

	public List<ComplaintValueBean> getComplaintValueBean() {
		return complaintValueBean;
	}

	public void setComplaintValueBean(List<ComplaintValueBean> complaintValueBean) {
		this.complaintValueBean = complaintValueBean;
	}

	public boolean isReceivedFromEnabled() {
		return receivedFromEnabled;
	}

	public void setReceivedFromEnabled(boolean receivedFromEnabled) {
		this.receivedFromEnabled = receivedFromEnabled;
	}

	public void addMessage(FacesMessage message) {
		FacesContext.getCurrentInstance().addMessage(null, message);
	}
	
	public ViewComplaintValueBean getComplaint() {
		return complaint;
	}

	public void setComplaint(ViewComplaintValueBean complaint) {
		this.complaint = complaint;
	}

	public LoginParams getOfficer() {
		return officer;
	}

	public void setOfficer(LoginParams officer) {
		this.officer = officer;
	}
	
	public List<ViewComplaintValueBean> getViewComplaintList() {
		return viewComplaintList;
	}

	public void setViewComplaintList(List<ViewComplaintValueBean> viewComplaintList) {
		this.viewComplaintList = viewComplaintList;
	}

	public List<ViewComplaintValueBean> getComplaintList() {
		return complaintList;
	}

	public void setComplaintList(List<ViewComplaintValueBean> complaintList) {
		this.complaintList = complaintList;
	}
	
	public List<ViewComplaintReportValueBean> getViewComplaintReportList() {
		return viewComplaintReportList;
	}

	public void setViewComplaintReportList(List<ViewComplaintReportValueBean> viewComplaintReportList) {
		this.viewComplaintReportList = viewComplaintReportList;
	}

	public DataModel getDm() {
		return dm;
	}

	public void setDm(DataModel dm) {
		this.dm = dm;
	}

	public int getRegionId() {
		return regionId;
	}

	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescriptionTransfer() {
		return descriptionTransfer;
	}

	public void setDescriptionTransfer(String descriptionTransfer) {
		this.descriptionTransfer = descriptionTransfer;
	}

	public String getDescriptionComplete() {
		return descriptionComplete;
	}

	public void setDescriptionComplete(String descriptionComplete) {
		this.descriptionComplete = descriptionComplete;
	}

	public Map getMap() {
		return map;
	}

	public void setMap(Map map) {
		this.map = map;
	}

	public DataModel getDmFilter() {
		return dmFilter;
	}

	public void setDmFilter(DataModel dmFilter) {
		this.dmFilter = dmFilter;
	}

	public List<AdminUserValueBean> getUserList() {
		return userList;
	}

	public void setUserList(List<AdminUserValueBean> userList) {
		this.userList = userList;
	}

	public boolean isRegionDropDownDisabled() {
		return regionDropDownDisabled;
	}

	public void setRegionDropDownDisabled(boolean regionDropDownDisabled) {
		this.regionDropDownDisabled = regionDropDownDisabled;
	}

	public boolean isCircleDropDownDisabled() {
		return circleDropDownDisabled;
	}

	public void setCircleDropDownDisabled(boolean circleDropDownDisabled) {
		this.circleDropDownDisabled = circleDropDownDisabled;
	}

	public boolean isDivisionDropDownDisabled() {
		return divisionDropDownDisabled;
	}

	public void setDivisionDropDownDisabled(boolean divisionDropDownDisabled) {
		this.divisionDropDownDisabled = divisionDropDownDisabled;
	}

	public boolean isSubDiviviondropDownDisabled() {
		return subDiviviondropDownDisabled;
	}

	public void setSubDiviviondropDownDisabled(boolean subDiviviondropDownDisabled) {
		this.subDiviviondropDownDisabled = subDiviviondropDownDisabled;
	}

	public int getComplaintCode() {
		return complaintCode;
	}

	public void setComplaintCode(int complaintCode) {
		this.complaintCode = complaintCode;
	}

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

	public String getComplaintNumber() {
		return complaintNumber;
	}

	public void setComplaintNumber(String complaintNumber) {
		this.complaintNumber = complaintNumber;
	}

	public String getConsumerMobileNumber() {
		return consumerMobileNumber;
	}

	public void setConsumerMobileNumber(String consumerMobileNumber) {
		this.consumerMobileNumber = consumerMobileNumber;
	}

	public ConsumerServiceValueBean getConsumerServiceValueBean() {
		return consumerServiceValueBean;
	}

	public void setConsumerServiceValueBean(ConsumerServiceValueBean consumerServiceValueBean) {
		this.consumerServiceValueBean = consumerServiceValueBean;
	}
	
	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public Date getCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}

	public Date getMinDate() {
		return minDate;
	}

	public void setMinDate(Date minDate) {
		this.minDate = minDate;
	}
	
	public OutagesValueBean getOutagesValueBean() {
		return outagesValueBean;
	}

	public void setOutagesValueBean(OutagesValueBean outagesValueBean) {
		this.outagesValueBean = outagesValueBean;
	}
	

	public String getCircleName() {
		return circleName;
	}

	public void setCircleName(String circleName) {
		this.circleName = circleName;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Date getMaxDate() {
		return maxDate;
	}

	public void setMaxDate(Date maxDate) {
		this.maxDate = maxDate;
	}
	
	public String getPreviewExcelContent() {
		return previewExcelContent;
	}

	public void setPreviewExcelContent(String previewExcelContent) {
		this.previewExcelContent = previewExcelContent;
	}


	public String getSsName() {
		return ssName;
	}

	public void setSsName(String ssName) {
		this.ssName = ssName;
	}

	public String getFdrName() {
		return fdrName;
	}

	public void setFdrName(String fdrName) {
		this.fdrName = fdrName;
	}

	public String[] getDtName() {
		return dtName;
	}

	public void setDtName(String[] dtName) {
		this.dtName = dtName;
	}

	public DataModel getDmCurrentData() {
		return dmCurrentData;
	}

	public void setDmCurrentData(DataModel dmCurrentData) {
		this.dmCurrentData = dmCurrentData;
	}

	public DataModel getDmTotalData() {
		return dmTotalData;
	}

	public void setDmTotalData(DataModel dmTotalData) {
		this.dmTotalData = dmTotalData;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getFormattedDate() {
		return formattedDate;
	}

	public void setFormattedDate(String formattedDate) {
		this.formattedDate = formattedDate;
	}

	
	
	// FOR SOCIAL MEDIA REPORT PAGE	
	
	private boolean validateSearchFormSm(DataModel dataModel) {
		
		boolean validSearch = false;
		
		if ((dataModel.getRegionFilter() != null && dataModel.getRegionFilter().length > 0)
				|| (dataModel.getCircleFilter() != null && dataModel.getCircleFilter().length > 0) || (dataModel.getDivisionFilter() != null && dataModel.getDivisionFilter().length > 0)  || 
				(dataModel.getSubDivisionFilter() != null && dataModel.getSubDivisionFilter().length > 0) || (dataModel.getSectionFilter() != null && dataModel.getSectionFilter().length > 0)) {
			
			validSearch = true;
		}
		
		return validSearch;
	
	}
	
	public void searchComplaintSm() {
		
		try {
			boolean validSearch = validateSearchFormSm(dmFilter);
			
			if(validSearch) {
				
				Date fromDate = dmFilter.getFromDate();
				Date toDate = dmFilter.getToDate();
				
				if (fromDate != null && toDate != null) {
					if(fromDate.after(toDate)) {
						validSearch = false;
						FacesContext.getCurrentInstance().addMessage(null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "To date can't be lesser than from date!"));
					}else {
                       
                        long diffInMillis = toDate.getTime() - fromDate.getTime();
                       
                        long diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
                        
                       
//                        if (diffInDays > 31) {
//                            validSearch = false;
//                            FacesContext.getCurrentInstance().addMessage(null,
//                                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please select a date range within a one-month period!"));
//                        }
                    
					}
				}
				if(fromDate == null || toDate == null){
                    validSearch = false;
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", " Date can't be empty!"));
                }
			
				if(validSearch) {
					ComplaintsDao daoComplaints = new ComplaintsDao();
					viewComplaintReportList = daoComplaints.searchComplaintSm(dmFilter, -1, -1);
					
					dm.arrReport = daoComplaints.getReportDashBoardSm(dmFilter);
					
					if (dm.arrReport[0][0] == -1) {
						dm.arrReport[0][0] = 0;
					}
				}
				
			}
			else {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Please select a place for search."));
			}
					
			 
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
		
	}

}

