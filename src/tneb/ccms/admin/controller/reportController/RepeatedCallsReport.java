package tneb.ccms.admin.controller.reportController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import javax.swing.text.View;
import javax.transaction.Transactional;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import tneb.ccms.admin.model.ComplaintBean;
import tneb.ccms.admin.model.CustMasterBean;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.valuebeans.AdminUserValueBean;
import tneb.ccms.admin.valuebeans.CallCenterUserValueBean;
import tneb.ccms.admin.valuebeans.RepeatedCallComplaintValueBean;
import tneb.ccms.admin.valuebeans.TmpCtCirBean;
import tneb.ccms.admin.valuebeans.TmpCtSecBean;
import tneb.ccms.admin.valuebeans.ViewComplaintReportValueBean;

import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;

@Named
@ViewScoped
public class RepeatedCallsReport implements Serializable {

	private static final long serialVersionUID = 1L;
	private SessionFactory sessionFactory;
	private boolean initialized = false;
    private boolean cameFromInsideReport = false;
	DataModel dmFilter;
	StreamedContent excelFile;
	StreamedContent pdfFile;
	List<RepeatedCallComplaintValueBean> counts;
	List<ViewComplaintReportValueBean> contactHistory;
	ViewComplaintReportValueBean complaint;

	private Date currentDate = new Date();
	private String currentYear = String.valueOf(Year.now().getValue());



	@PostConstruct
	public void init() {
		System.out.println("Initializing REPEATED CALLS ABSTARCT REPORT...");
		sessionFactory = HibernateUtil.getSessionFactory();
		dmFilter = new DataModel();

	}
	public void resetIfNeeded() {
        if (!FacesContext.getCurrentInstance().isPostback() && !cameFromInsideReport) {
        	counts = null;
        	dmFilter.setFromDate(null);
        	dmFilter.setToDate(null);
        }
        cameFromInsideReport = false; 
    }
	public void refresh() {
        dmFilter.setFromDate(null);
        dmFilter.setToDate(null);
        counts = null;
    }

	@Transactional
	public void searchRepeatedCallsList() {
	    Session session = null;
	    try {
	        // Set default from date
	        if (dmFilter.getFromDate() == null) {
	            Calendar fromCal = Calendar.getInstance();
	            fromCal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
	            dmFilter.setFromDate(fromCal.getTime());
	        }

	        // Set default to date
	        if (dmFilter.getToDate() == null) {
	            dmFilter.setToDate(new Date());
	        }

	        Date fromDate = dmFilter.getFromDate();
	        Date toDate = dmFilter.getToDate();
	        String device = dmFilter.getDevice();
	        System.err.println("THE DEVICE ------------------"+device);

	        session = sessionFactory.openSession();
	        session.beginTransaction();

	        HttpSession httpSession = (HttpSession) FacesContext.getCurrentInstance()
	                .getExternalContext().getSession(false);

	        AdminUserValueBean adminUserValueBean = (AdminUserValueBean)
	                httpSession.getAttribute("sessionAdminValueBean");

	        CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean)
	                httpSession.getAttribute("sessionCallCenterUserValueBean");
	        
	        List<String> devices = new ArrayList<>();

	        if (callCenterValueBean != null) {
	            // Call center specific device filtering
	            if (callCenterValueBean.getRoleId() == 3) {
	                if ("M".equals(device)) {
	                    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", 
	                        "Not Authorized To View Minnagam Complaint Contact Details");
	                    FacesContext.getCurrentInstance().addMessage(null, message);
	                    return; // Exit early since not authorized
	                }
	                devices.add("SM");
	            } 
	            else if (callCenterValueBean.getRoleId() == 5 || callCenterValueBean.getRoleId() == 7) {
	                if ("O".equals(device)) {
	                    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", 
	                        "Not Authorized To View Other Than Minnagam Complaint Contact Details");
	                    FacesContext.getCurrentInstance().addMessage(null, message);
	                    return;
	                }
	                devices.add("MI");
	            }
	            else {
		            if ("M".equals(device)) {
		                devices.add("MI");
		            } 
		            else if ("O".equals(device)) {
		                devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile", "web", "SM", "admin", "FOC", "MM"));
		            }
	            }
	        } 
	        else if(adminUserValueBean!=null) {
	        	if(adminUserValueBean.getRoleId()==10) {
	        		if ("O".equals(device)) {
	                    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", 
	                        "Not Authorized To View Other Than Minnagam Complaint Contact Details");
	                    FacesContext.getCurrentInstance().addMessage(null, message);
	                    return;
	                }
	                devices.add("MI");
	            }else {
	            	 if ("M".equals(device)) {
	 	                devices.add("MI");
	 	            } 
	 	            else if ("O".equals(device)) {
	 	                devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile", "web", "SM", "admin", "FOC", "MM"));
	 	            }
	            }
	        }
	        else {
	            if ("M".equals(device)) {
	                devices.add("MI");
	            } 
	            else if ("O".equals(device)) {
	                devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile", "web", "SM", "admin", "FOC", "MM"));
	            }
	           
	        }
	        
	        System.err.println("THE DEVICES------------------"+devices.size());
	        
	        StringBuilder sql = new StringBuilder(
	            "SELECT p.mobile, COUNT(*) AS cnt " +
	            "FROM complaint c " +
	            "JOIN public_user p ON p.id = c.user_id " +
	            "WHERE TRUNC(c.created_on) >= :dateFrom " +
	            "AND TRUNC(c.created_on) <= :dateTo " +
	            "AND c.device IN (:devices) " +
	            "AND p.mobile IS NOT NULL "
	        );

	        Query query;

	        if (adminUserValueBean != null) {
	            Integer roleId = adminUserValueBean.getRoleId();
	            Integer regionID = adminUserValueBean.getRegionId();
	            Integer circleID = adminUserValueBean.getCircleId();
	            Integer divisionID = adminUserValueBean.getDivisionId();
	            Integer subDivisionID = adminUserValueBean.getSubDivisionId();
	            Integer sectionID = adminUserValueBean.getSectionId();

	            if (roleId >= 1 && roleId <= 5) {
	                sql.append("AND c.region_id = :regionId ");
	                if (roleId <= 4) sql.append("AND c.circle_id = :circleId ");
	                if (roleId <= 3) sql.append("AND c.division_id = :divisionId ");
	                if (roleId <= 2) sql.append("AND c.sub_division_id = :subDivisionId ");
	                if (roleId == 1) sql.append("AND c.section_id = :sectionId ");
	            }

	            sql.append("GROUP BY p.mobile HAVING COUNT(*) > 1 ORDER BY cnt DESC");

	            query = session.createNativeQuery(sql.toString());
	            query.setParameter("dateFrom", fromDate);
	            query.setParameter("dateTo", toDate);
	            query.setParameter("devices", devices);

	            if (roleId >= 1 && roleId <= 5) {
	                query.setParameter("regionId", regionID);
	                if (roleId <= 4) query.setParameter("circleId", circleID);
	                if (roleId <= 3) query.setParameter("divisionId", divisionID);
	                if (roleId <= 2) query.setParameter("subDivisionId", subDivisionID);
	                if (roleId == 1) query.setParameter("sectionId", sectionID);
	            }

	        } else if (callCenterValueBean != null) {
	        	List<Integer> callCenterCircleId = new ArrayList<Integer>();
	 			Session session2 = sessionFactory.openSession();
			    try {
			        session2.beginTransaction();
			        
			        Integer userId = callCenterValueBean.getId();
			        
			        @SuppressWarnings("unchecked")
					List<Integer> circleId =  session2.createQuery(
			            "select c.circleBean.id from CallCenterMappingBean c " +
			            "where c.callCenterUserBean.id = :userId")
			            .setParameter("userId", userId)
			            .getResultList();
			        
			        callCenterCircleId = circleId;
			        
			        session2.getTransaction().commit();
			        
			    } finally {
			        session2.close();
			    }
			    
	        	if(callCenterValueBean.getRoleId()==1) {
	        		sql.append( " AND c.circle_id IN :callCenterCircleId ");
		        	}
		        	else if(callCenterValueBean.getRoleId()==7) {
		        		sql.append(" AND c.circle_id IN :callCenterCircleId ");
		        		
		        	}

	            sql.append("GROUP BY p.mobile HAVING COUNT(*) > 1 ORDER BY cnt DESC");

	            query = session.createNativeQuery(sql.toString());
	            query.setParameter("dateFrom", fromDate);
	            query.setParameter("dateTo", toDate);
	            query.setParameter("devices", devices);
	            
	            if(callCenterValueBean.getRoleId()==1) {
	            	query.setParameter("callCenterCircleId", callCenterCircleId);
		        	}

		        	else if(callCenterValueBean.getRoleId()==7) {
		        		query.setParameter("callCenterCircleId", callCenterCircleId);
		       
		        	}

	        } else {
	            
	            sql.append("GROUP BY p.mobile HAVING COUNT(*) > 1 ORDER BY cnt DESC");

	            query = session.createNativeQuery(sql.toString());
	            query.setParameter("dateFrom", fromDate);
	            query.setParameter("dateTo", toDate);
	            query.setParameter("devices", devices);
	        }

	        List<Object[]> results = query.getResultList();
	        counts = new ArrayList<>();

	        for (Object[] row : results) {
	            String contactNo = (String) row[0];
	            BigDecimal count = (BigDecimal) row[1];
	            counts.add(new RepeatedCallComplaintValueBean(contactNo, count.longValue()));
	        }

	        System.out.println("THE REPEATED CALLS LIST SIZE ==" + counts.size());

	        if (counts.isEmpty()) {
	            String msg = "No Data Found For The Given Date and For the Device - " +
	                    ("M".equals(device) ? "Minnagam" : "Other Than Minnagam");
	            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", msg);
	            FacesContext.getCurrentInstance().addMessage(null, message);
	        }

	        session.getTransaction().commit();

	    } catch (Exception e) {
	        e.printStackTrace();
	        System.out.println("Error in database operation");
	        if (session != null && session.getTransaction().isActive()) {
	            session.getTransaction().rollback();
	        }
	    } finally {
	        if (session != null && session.isOpen()) {
	            session.close();
	        }
	    }
	}

	
	@Transactional
	public void redirectToContactHistoryData(String contactNumber) throws IOException {
		try (Session session = sessionFactory.openSession()) {
			Calendar fromCal = Calendar.getInstance();
			fromCal.setTime(dmFilter.getFromDate());
			fromCal.set(Calendar.HOUR_OF_DAY, 0);
			fromCal.set(Calendar.MINUTE, 0);
			fromCal.set(Calendar.SECOND, 0);
			fromCal.set(Calendar.MILLISECOND, 0);
			Date formattedFromDate = fromCal.getTime();

			Calendar toCal = Calendar.getInstance();
			toCal.setTime(dmFilter.getToDate());
			toCal.set(Calendar.HOUR_OF_DAY, 23);
			toCal.set(Calendar.MINUTE, 59);
			toCal.set(Calendar.SECOND, 59);
			toCal.set(Calendar.MILLISECOND, 999);
			Date formattedToDate = toCal.getTime();

			dmFilter.setContactNumber(contactNumber);

			String device = dmFilter.getDevice();
			List<String> devices = new ArrayList<String>();

			if (device != null && device.equals("M")) {
				devices.add("MI");
			} else if (device != null && device.equals("O")) {
				devices.addAll(
						Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile", "web", "SM", "admin", "FOC", "MM"));
			}


			String hql = "SELECT a.id as COMPLAINT_ID, "
					+ "to_char(a.created_on, 'dd-mm-yyyy-hh24:mi') AS COMPLAINT_DATE, "
					+ "DECODE(a.device, 'web', 'Web', 'FOC', 'FOC', 'admin', 'FOC', 'SM', 'Social Media', 'Android', 'Mobile', 'AMOB', 'Mobile', 'IMOB', 'Mobile', 'iOS', 'Mobile', 'mobile', 'Mobile', 'MI', 'Minnagam') AS DEVICE, "
					+ "a.SERVICE_NUMBER AS SERVICE_NUMBER, " + "a.SERVICE_NAME AS SERVICE_NAME, "
					+ "a.SERVICE_ADDRESS AS SERVICE_ADDRESS, " + "b.mobile AS CONTACT_NUMBER, "
					+ "k.name AS COMPLAINT_TYPE, " + "d.name AS SUB_CATEGORY, "
					+ "a.description AS COMPLAINT_DESCRIPTION, "
					+ "DECODE(a.status_id, 0, 'Pending', 1, 'In Progress', 2, 'Completed') AS COMPLAINT_STATUS, "
					+ "(CASE WHEN a.status_id=2 then TO_CHAR(a.updated_on, 'DD-MM-YYYY-HH24:MI') else '' end) AS ATTENDED_DATE, "
					+ "(CASE WHEN a.status_id=2 then c.description else '' end) AS ATTENDED_REMARKS, "
					+ "f.name AS REGION,a.region_id As Region_id, " + "g.name AS CIRCLE,a.circle_id as Circle_ID, " + "h.name AS DIVISION,a.division_id as Division_Id, " + "i.name AS SUB_DIVISION,a.sub_division_id as Sub_Division_ID, "
					+ "j.name AS SECTION,a.section_id as Section_id " + "FROM COMPLAINT a " + "LEFT JOIN COMP_CONTACT_MAP cm ON a.id=cm.COMP_ID "
					+ "LEFT JOIN PUBLIC_USER b ON a.user_id = b.id "
					+ "LEFT JOIN COMPLAINT_HISTORY c ON a.id = c.complaint_id AND a.status_id = c.status_id "
					+ "LEFT JOIN SUB_CATEGORY d ON a.sub_category_id = d.id "
					+ " JOIN CATEGORY k ON a.complaint_type = k.code " + " JOIN REGION f ON a.region_id = f.id "
					+ " JOIN CIRCLE g ON a.circle_id = g.id " + " JOIN DIVISION h ON a.division_id = h.id "
					+ " JOIN SUB_DIVISION i ON a.sub_division_id = i.id " + " JOIN SECTION j ON a.section_id = j.id "
					+ "WHERE (b.MOBILE =:contactNumber OR cm.CONTACTNO=:contactNumber) "
					+ "AND a.created_on BETWEEN :formattedFromDate AND :formattedToDate and a.DEVICE IN :devices";


			Query query = session.createNativeQuery(hql);
			query.setParameter("contactNumber", contactNumber);
			query.setParameter("formattedFromDate", formattedFromDate);
			query.setParameter("formattedToDate", formattedToDate);
			query.setParameter("devices", devices);

			List<Object[]> results = query.getResultList();

			List<ViewComplaintReportValueBean> resultList = new ArrayList<ViewComplaintReportValueBean>();
			contactHistory = new ArrayList<ViewComplaintReportValueBean>();
			for (Object[] row : results) {
				ViewComplaintReportValueBean dto = new ViewComplaintReportValueBean();
				dto.setComplaintId((BigDecimal) row[0]);
				dto.setCreatedOnFormatted((String) row[1]);
				dto.setDevice((String) row[2]);
				dto.setServiceNumber((String) row[3]);
				dto.setServiceName((String) row[4]);
				dto.setServiceAddress((String) row[5]);
				dto.setMobile((String) row[6]);
				dto.setComplaintType((String) row[7]);
				dto.setSubCategoryName((String) row[8]);
				dto.setComplaintDescription((String) row[9]);
				dto.setComplaintStatusValue((String) row[10]);
				dto.setAttendedDate((String) row[11]);
				dto.setAttendedRemarks((String) row[12]);
				dto.setRegionName((String) row[13]);
				dto.setRegionId(((BigDecimal) row[14]).intValue());
				dto.setCircleName((String) row[15]);
				dto.setCircleId(((BigDecimal) row[16]).intValue());
				dto.setDivisionName((String) row[17]);
				dto.setDivisionId(((BigDecimal) row[18]).intValue());
				dto.setSubDivisionName((String) row[19]);
				dto.setSubDivisionId(((BigDecimal) row[20]).intValue());
				dto.setSectionName((String) row[21]);
				dto.setSectionId(((BigDecimal) row[22]).intValue());

				resultList.add(dto);
			}
			
    
	        
	        HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
	                .getExternalContext().getSession(false);
			
			AdminUserValueBean adminUserValueBean = (AdminUserValueBean) 
					httpsession.getAttribute("sessionAdminValueBean");
	 		CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");

			        
	        if(adminUserValueBean!=null) {
	        Integer roleId = adminUserValueBean.getRoleId();
	        Integer regionID = adminUserValueBean.getRegionId();
	        Integer circleID = adminUserValueBean.getCircleId();
	        Integer divisionID = adminUserValueBean.getDivisionId();
	        Integer subDivisionID = adminUserValueBean.getSubDivisionId();
	        Integer sectionID = adminUserValueBean.getSectionId();
	        
	        System.out.println("THE SECTION ID ------------------"+sectionID);
	        
	        // HEAD QUATERS
	        if(roleId>=6 && roleId<=9) {
	        	contactHistory = resultList;
	        }
	        //REGION
	        else if(roleId==5) {      
	        	contactHistory = resultList.stream().filter(r ->r.getRegionId().equals(regionID)).collect(Collectors.toList());
	        }
	        //CIRCLE
	        else if(roleId==4) {
	        	contactHistory = resultList.stream().filter(r ->r.getRegionId().equals(regionID)).filter(c ->c.getCircleId().equals(circleID)).collect(Collectors.toList());
	        }
	        //DIVISION
	        else if(roleId==3) {
	        	contactHistory = resultList.stream().filter(r ->r.getRegionId().equals(regionID)).filter(c ->c.getCircleId().equals(circleID))
	        			.filter(d ->d.getDivisionId().equals(divisionID)).collect(Collectors.toList());
	        }
	      //SUB DIVISION
	        else if(roleId==2) {
	        	contactHistory = resultList.stream().filter(r ->r.getRegionId().equals(regionID)).filter(c ->c.getCircleId().equals(circleID))
	        			.filter(d ->d.getDivisionId().equals(divisionID)).filter(sd ->sd.getSubDivisionId().equals(subDivisionID)).collect(Collectors.toList());
	        }
	      //SECTION
	        else if(roleId==1) {
	        	contactHistory = resultList.stream().filter(r ->r.getRegionId().equals(regionID)).filter(c ->c.getCircleId().equals(circleID))
	        			.filter(d ->d.getDivisionId().equals(divisionID)).filter(sd ->sd.getSubDivisionId().equals(subDivisionID))
	        			.filter(sec->sec.getSectionId().equals(sectionID)).collect(Collectors.toList());

	        }
	        else if (roleId==10) {
	        	contactHistory = resultList.stream().filter(r ->r.getDevice().equals("Minnagam")).collect(Collectors.toList());
	        }
	        else {
	        	contactHistory = resultList;
	        }
	        }else if(callCenterValueBean!=null) {
	        	Integer roleId = callCenterValueBean.getRoleId();
	            Integer userId = callCenterValueBean.getId();

	            // Get circle IDs for the call center user
	            @SuppressWarnings("unchecked")
				List<Integer> circleIdList = session.createQuery(
	                "select c.circleBean.id from CallCenterMappingBean c " +
	                "where c.callCenterUserBean.id = :userId")
	                .setParameter("userId", userId)
	                .getResultList();
	            
	            if(roleId==1) {
	            	contactHistory = resultList.stream().filter(c ->circleIdList.contains(c.getCircleId())).collect(Collectors.toList());
	            }
	            if(roleId==5) {
	            	contactHistory = resultList.stream().filter(c ->c.getDevice().equals("Minnagam")).collect(Collectors.toList());
	            }
	            if(roleId==7) {
	            	contactHistory = resultList.stream().filter(c ->circleIdList.contains(c.getCircleId())).filter(c ->c.getDevice().equals("Minnagam")).collect(Collectors.toList());
	            }
	            if(roleId==3) {
	            	contactHistory = resultList.stream().filter(c ->c.getDevice().equals("Social Media")).collect(Collectors.toList());
	            }

	        }
		}

		cameFromInsideReport = true;
		System.out.println("THE CONTACT HISTORY------------" + contactHistory.size());

		FacesContext.getCurrentInstance().getExternalContext().redirect("repeatedCallHistory.xhtml");
	}
	
	// COMPLAINT'S DETAILED VIEW
	public void getComplaintDetailForAbstractReport() {
		
		try (Session session = sessionFactory.openSession()) {
		String complaintIdParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("complaintID");
		System.out.println("THE COMPLAINT IS IS CALLED =============="+complaintIdParam);

		
		String regCusId=null;
		    String distribCode =null;
			Session session3 = sessionFactory.openSession();
	    try {
	        session3.beginTransaction();
	        
	        
            int compId= Integer.parseInt(complaintIdParam);
 	        
	        ComplaintBean complaint = (ComplaintBean) session3.createQuery(
	                "select c from ComplaintBean c where c.id = :complaintId", ComplaintBean.class)
	            .setParameter("complaintId", compId)
	            .uniqueResultOptional()
	            .orElse(null);
	        
	        if (complaint != null && complaint.getServiceNumber() != null) {
	            regCusId = "1" + complaint.getServiceNumber();
	        } else {
	            regCusId = null; 
	        }
	        
	        
	        CustMasterBean custMaster =null;
	        
	        
	        if (regCusId != null) {
		        Long regCus = Long.parseLong(regCusId);

	            custMaster = session3.createQuery(
	                    "select cm from CustMasterBean cm where cm.regCusId = :regCusId", CustMasterBean.class)
	                .setParameter("regCusId", regCus)
	                .uniqueResultOptional()
	                .orElse(null);
	        }
	        
	        if(custMaster!=null && custMaster.getStCode() !=null) {
		        distribCode = custMaster.getStCode();
	        }else {
		        distribCode = null;
	        }

	        session3.getTransaction().commit();
	    } finally {
	        session3.close();
	    }
	    
		
		String hql = "SELECT a.id, "+
        		"to_char(a.created_on, 'dd-mm-yyyy-hh24:mi') AS Complaint_Date, " +
                "DECODE(a.device, 'web', 'Web', 'FOC', 'FOC', 'admin', 'FOC', 'SM', 'Social Media', 'Android', 'Mobile', 'AMOB', 'Mobile', 'IMOB', 'Mobile', 'iOS', 'Mobile', 'mobile', 'Mobile', 'MI', 'Minnagam') AS Device, "+
                "a.SERVICE_NUMBER AS Service_Number, " +
                "a.SERVICE_NAME AS Service_Name, "+
                "a.SERVICE_ADDRESS AS Service_Address, " +
                "b.mobile AS Contact_Number, k.name AS Complaint_Type, d.name AS subctyp, " +
                "a.description AS Complaint_Description, " +
                "DECODE(a.status_id, 0, 'Pending', 1, 'In Progress', 2, 'Completed') AS Complaint_Status, " +
                "(CASE WHEN a.status_id=2 then TO_CHAR(a.updated_on, 'DD-MM-YYYY-HH24:MI') else '' end) AS Attended_Date, " +
                "(CASE WHEN a.status_id=2 then c.DESCRIPTION else '' end) AS Attended_Remarks, " +
                "f.name AS Region, g.name AS Circle, h.name AS Division, i.name AS SubDivision, j.name AS Section, "+
                "b.FIRST_NAME AS UserName, "+
                "a.ALTERNATE_MOBILE_NO as RegisteredMobileNumber, "+
                "to_char(fb.ENTRYDT, 'dd-mm-yyyy-hh24:mi') AS feedBackEntryDate, "+
                "fb.REMARKS AS feedbackRemarks, "+
                "fb.RATING AS feedbackRating, "+
                "dm.DISTRIB_NAME As Distribution, "+
                "a.IMAGE_1 as image_1, "+
                "a.IMAGE_2 as image_2, "+
                "a.IMAGEID as closure_image "+
                "FROM COMPLAINT a " +
                "LEFT JOIN PUBLIC_USER b ON a.user_id = b.id " +
                "LEFT JOIN COMPLAINT_HISTORY c ON a.id = c.complaint_id AND a.status_id = c.status_id " +
                "LEFT JOIN COMP_FEEDBACK fb ON a.id = fb.COMP_ID "+
                "LEFT JOIN COMP_TRANSFER ct ON a.id = ct.COMP_ID "+
                "LEFT JOIN COMP_QC_DETAILS qc ON a.id = qc.COMP_ID "+
                "LEFT JOIN SUB_CATEGORY d ON a.sub_category_id = d.id " +
                "JOIN CATEGORY k ON a.complaint_type = k.code " +
                "JOIN REGION f ON a.region_id = f.id " +
                "JOIN CIRCLE g ON a.circle_id = g.id " +
                "JOIN DIVISION h ON a.division_id = h.id " +
                "JOIN SUB_DIVISION i ON a.sub_division_id = i.id " +
                "JOIN SECTION j ON a.section_id = j.id " +
                //"LEFT JOIN CUSTMASTER ct ON SUBSTR(ct.REGCUSID, 2) = a.SERVICE_NUMBER " +
                "LEFT JOIN DISTRIB_MASTER dm ON dm.reg_no = f.ID AND dm.SCODE = j.code AND dm.DISTRIB_CODE = :distribCode "+
                "WHERE a.id = :complaintIdParam " ;
		
		Query query = session.createNativeQuery(hql);
		query.setParameter("complaintIdParam", complaintIdParam);
		query.setParameter("distribCode", distribCode);

		
		List<Object[]> results = query.getResultList();

		complaint = new ViewComplaintReportValueBean();
        for (Object[] row : results) {
        	ViewComplaintReportValueBean dto = new ViewComplaintReportValueBean();
        	dto.setComplaintId((BigDecimal)row[0]); // COMPLAINT ID
        	dto.setCreatedOnFormatted((String) row[1]);
        	dto.setDevice((String)row[2]);  // Compl.Received through
        	dto.setServiceNumber((String)row[3]);
        	dto.setServiceName((String)row[4]);
        	dto.setServiceAddress((String) row[5]); // COMPLAINANT ADDRESS
        	dto.setMobile((String) row[6]);        // COMPLAINANT MOBILE NUM
        	dto.setComplaintType((String) row[7]);  //Complaint Category
        	dto.setSubCategoryName((String) row[8]);  //Sub Category
        	dto.setComplaintDescription((String) row[9]);  // Description
        	dto.setComplaintStatusValue((String) row[10]);
        	dto.setAttendedDate((String) row[11]);   // CLOSURE DATE
        	dto.setAttendedRemarks((String) row[12]); // CLOSURE REMARK
        	dto.setRegionName((String) row[13]);  
        	dto.setCircleName((String) row[14]);
        	dto.setDivisionName((String) row[15]);
        	dto.setSubDivisionName((String) row[16]);
        	dto.setSectionName((String) row[17]);
        	dto.setUserName((String) row[18]); // COMPLAINANT NAME
        	dto.setRegisteredMobileNumber((String) row[19]);
        	dto.setFeedBackEntryDate((String) row[20]);
        	dto.setFeedback((String) row[21]);
        	dto.setFeedbackRating(row[22] != null ? ((BigDecimal) row[22]).intValue() : null);
        	dto.setDistribution((String) row[23]);   
        	dto.setImage1((String) row[24]);
        	dto.setImage2((String) row[25]);
        	dto.setClosureImage((String) row[26]);
        	
        	complaint=dto;
        }	
        
        String transferHql = "SELECT to_char(TRF_ON, 'dd-mm-yyyy-hh24:mi'), TRF_USER, REMARKS " +
                "FROM COMP_TRANSFER WHERE COMP_ID = :complaintId " +
                "ORDER BY TRF_ON";
        
        Query transferQuery = session.createNativeQuery(transferHql);
        transferQuery.setParameter("complaintId", complaintIdParam);
        List<Object[]> transferResults = transferQuery.getResultList();
        List<ViewComplaintReportValueBean.TransferDetail> transfers = new ArrayList<>();

        for (Object[] transfer : transferResults) {
            ViewComplaintReportValueBean.TransferDetail td = new ViewComplaintReportValueBean.TransferDetail();
            td.setTransferDate((String) transfer[0]);
            td.setTransferredBy((String) transfer[1]);
            td.setTransferRemarks((String) transfer[2]);
            transfers.add(td);
        }
        complaint.setTransferDetails(transfers);
        
		String resendHql = "SELECT to_char(RESEND_ON, 'dd-mm-yyyy-hh24:mi'), ENTRYUSER, REMARKS " +
                "FROM COMP_RESEND WHERE COMP_ID = :complaintId " +
                "ORDER BY RESEND_ON";

Query resendQuery = session.createNativeQuery(resendHql); 
resendQuery.setParameter("complaintId", complaintIdParam);

List<Object[]> resendResults = resendQuery.getResultList(); 

List<ViewComplaintReportValueBean.ResendDetail> resendResultList = new ArrayList<>();

for (Object[] resend : resendResults) {
 ViewComplaintReportValueBean.ResendDetail td = new ViewComplaintReportValueBean.ResendDetail();
 td.setResendDate((String) resend[0]);
 td.setResendedBy((String) resend[1]);
 td.setResendRemarks((String) resend[2]);
 resendResultList.add(td);
}

complaint.setResendDetails(resendResultList);
        
        
        String qcHql = "SELECT to_char(QC_ON, 'dd-mm-yyyy-hh24:mi'), QC_STATUS, REMARKS " +
                "FROM COMP_QC_DETAILS WHERE COMP_ID = :complaintId " +
                "ORDER BY QC_ON";
		  Query qcQuery = session.createNativeQuery(qcHql);
		  qcQuery.setParameter("complaintId", complaintIdParam);
		  List<Object[]> qcResults = qcQuery.getResultList();
		  
		  List<ViewComplaintReportValueBean.QualityCheckDetail> qcs = new ArrayList<>();
		  for (Object[] qc : qcResults) {
		      ViewComplaintReportValueBean.QualityCheckDetail qcd = new ViewComplaintReportValueBean.QualityCheckDetail();
		      qcd.setQcDate((String) qc[0]);
		      qcd.setQcStatus((String) qc[1]);
		      qcd.setQcRemarks((String) qc[2]);
		      qcs.add(qcd);
		  }
		  complaint.setQualityCheckDetails(qcs);
		  
		  cameFromInsideReport=true;
		  
		  FacesContext.getCurrentInstance().getExternalContext().redirect("repeatedCallComplaintDetail.xhtml");
        
        System.out.println("THE SELECED COMPLAINT ID"+ complaint.getComplaintId());
        System.out.println("THE SELECED COMPLAINT ID"+ complaint.getDescription());

		
		}catch(Exception e) {
			e.printStackTrace();
			
		}

		
	}
	
	public void returnToSearchPage() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().redirect("repeatedCallHistory.xhtml");
	}
		
		
		public void redirectToRepeatedCallsReport() throws IOException {
			FacesContext.getCurrentInstance().getExternalContext().redirect("repeatedCallsReport.xhtml");
		}

	//CIRCLE REPORT TO EXCEL DOWNLOAD
	public void exportToExcel(List<RepeatedCallComplaintValueBean> reports) throws IOException {
		HSSFWorkbook workbook = new HSSFWorkbook();
	    Sheet sheet = workbook.createSheet("Minnagam_Repeated_Calls_Report");

        sheet.setColumnWidth(0, 2000); //S.NO
        sheet.setColumnWidth(1, 5000); // CIRCLE
        sheet.setColumnWidth(2, 5000); // CIRCLE
	    
	    Row headingRow = sheet.createRow(0);
	    Cell headingCell = headingRow.createCell(0);
	    headingCell.setCellValue("MINNAGAM REPEATED CALLS REPORT");
	    
	    Cell headingCell2 = headingRow.createCell(1);
	    headingCell2.setCellValue("");
	    
	    CellStyle headingStyle = workbook.createCellStyle();
	    HSSFFont headingFont = workbook.createFont();
	    headingFont.setBold(true);
	    headingFont.setFontHeightInPoints((short) 12);
	    headingStyle.setFont(headingFont);
	    headingStyle.setAlignment(HorizontalAlignment.CENTER);
	    headingStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	    headingStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	    headingStyle.setBorderBottom(BorderStyle.THIN);
	    headingStyle.setBorderTop(BorderStyle.THIN);
	    headingStyle.setBorderLeft(BorderStyle.THIN);
	    headingStyle.setBorderRight(BorderStyle.THIN);
	    headingStyle.setVerticalAlignment(VerticalAlignment.CENTER);
	    headingCell.setCellStyle(headingStyle);
	    
	    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3)); 

	    CellStyle headerStyle = workbook.createCellStyle();
	    HSSFFont headerFont = workbook.createFont();
	    headerFont.setBold(true);
	    headerStyle.setFont(headerFont);
	    headerStyle.setAlignment(HorizontalAlignment.CENTER);
	    headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
	    headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	    headerStyle.setBorderBottom(BorderStyle.THIN);
	    headerStyle.setBorderTop(BorderStyle.THIN);
	    headerStyle.setBorderLeft(BorderStyle.THIN);
	    headerStyle.setBorderRight(BorderStyle.THIN);

	    Row headerRow1 = sheet.createRow(2);

	    String[] mainHeaders = {"S.NO", "Contact Number", "No.Of.Times Of Call"};

	    
	    int colIndex = 0;
	    for (String mainHeader : mainHeaders) {
	        Cell cell = headerRow1.createCell(colIndex);
	        cell.setCellValue(mainHeader);
	        cell.setCellStyle(headerStyle);
            colIndex++;
	    }

	    int rowNum = 4;
	    int serialNumber = 1;

	    for (RepeatedCallComplaintValueBean report : reports) {
	        Row row = sheet.createRow(rowNum++);
	        row.createCell(0).setCellValue(serialNumber++);
	        row.createCell(1).setCellValue(report.getContactNumber());
	        row.createCell(2).setCellValue(report.getNoOfTimesOfCall());
	    }
	    CellStyle totalRowStyle = workbook.createCellStyle();
	    HSSFFont totalFont = workbook.createFont();
	    totalFont.setBold(true);
	    totalRowStyle.setFont(totalFont);
	    totalRowStyle.setAlignment(HorizontalAlignment.CENTER);
	    totalRowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
	    totalRowStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	    totalRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	    totalRowStyle.setBorderBottom(BorderStyle.THIN);
	    totalRowStyle.setBorderTop(BorderStyle.THIN);
	    totalRowStyle.setBorderLeft(BorderStyle.THIN);
	    totalRowStyle.setBorderRight(BorderStyle.THIN);


	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    workbook.write(outputStream);
	    workbook.close();

	    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
	    try {
	        excelFile = DefaultStreamedContent.builder()
	                .name("Minnagam_Repeated_Calls_Report.xls")
	                .contentType("application/vnd.ms-excel")
	                .stream(() -> inputStream)
	                .build();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	 

	
	 
	 private void setCellValue(Row row, int columnIndex, Number value) {
	        if (value != null) {
	            if (value instanceof BigDecimal) {
	                row.createCell(columnIndex).setCellValue(value.doubleValue());
	            } else if (value instanceof Integer || value instanceof Long || value instanceof Double || value instanceof Float) {
	                row.createCell(columnIndex).setCellValue(value.doubleValue());
	            } else {
	                row.createCell(columnIndex).setCellValue(value.toString()); 
	            }
	        } else {
	            row.createCell(columnIndex).setCellValue(""); 
	        }
	    }
	 
	// CIRCLE REPORT TO PDF DOWNLOAD
	 public void exportToPdf(List<RepeatedCallComplaintValueBean> circleList) throws IOException {

	     final int COLUMN_COUNT = 3;

	     final float FIRST_COLUMN_WIDTH = 5f;
	     final float OTHER_COLUMN_WIDTH = 15f;
	     final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
	     final Font TOTAL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
	     final Font DATA_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
	     final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);

	     Document document = new Document(PageSize.A4);
	     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	     try {
	         PdfWriter.getInstance(document, outputStream);
	         document.open();

	         // Title
	         Paragraph title = new Paragraph("MINNAGAM REPEATED CALLS REPORT", TITLE_FONT);
	         title.setAlignment(Element.ALIGN_CENTER);
	         document.add(title);

	         // Subtitle
	         SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	         String formattedFromDate = sdf.format(dmFilter.getFromDate());
	         String formattedToDate = sdf.format(dmFilter.getToDate());

	         String device = "";
	         if (dmFilter.getDevice().equals("M")) {
	             device = "MINNAGAM";
	         } else if (dmFilter.getDevice().equals("O")) {
	             device = "OTHERS";
	         }

	         Paragraph subTitle = new Paragraph("FROM : " + formattedFromDate + " TO : " + formattedToDate + "    DEVICE : " + device, DATA_FONT);
	         subTitle.setAlignment(Element.ALIGN_CENTER);
	         subTitle.setSpacingAfter(10f);
	         document.add(subTitle);

	         // Table
	         PdfPTable table = new PdfPTable(COLUMN_COUNT);
	         table.setWidthPercentage(100);
	         table.setSpacingBefore(10f);
	         float[] columnWidths = { FIRST_COLUMN_WIDTH, OTHER_COLUMN_WIDTH, OTHER_COLUMN_WIDTH };
	         table.setWidths(columnWidths);

	         // Table Header
	         String[] headers = { "S.NO", "CONTACT NUMBER", "NO OF TIMES OF CALL" };
	         for (String header : headers) {
	             PdfPCell headerCell = new PdfPCell(new Phrase(header, HEADER_FONT));
	             headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	             headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
	             table.addCell(headerCell);
	         }

	         // Table Data
	         int serialNo = 1;
	         for (RepeatedCallComplaintValueBean bean : circleList) {
	             // S.NO
	             PdfPCell cell1 = new PdfPCell(new Phrase(String.valueOf(serialNo++), DATA_FONT));
	             cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
	             table.addCell(cell1);

	             // CONTACT NUMBER
	             PdfPCell cell2 = new PdfPCell(new Phrase(bean.getContactNumber(), DATA_FONT));
	             cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
	             table.addCell(cell2);

	             // NO OF TIMES OF CALL
	             PdfPCell cell3 = new PdfPCell(new Phrase(String.valueOf(bean.getNoOfTimesOfCall()), DATA_FONT));
	             cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
	             table.addCell(cell3);
	         }

	         // Add table to document
	         document.add(table);
	         document.close();

	         pdfFile = DefaultStreamedContent.builder()
	                 .contentType("application/pdf")
	                 .name("Minnagam_Repeated_Calls_Report.pdf")
	                 .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
	                 .build();

	     } catch (Exception e) {
	         e.printStackTrace();
	     }
	 }

		 
		  

			


			private PdfPCell createDataCell(String text, Font font, int alignment) {
			    PdfPCell cell = new PdfPCell(new Phrase(text, font));
			    cell.setHorizontalAlignment(alignment);
			    return cell;
			}


	  
	  public void exportComplaintListToExcel(List<ViewComplaintReportValueBean> complaintList) throws IOException {
		  HSSFWorkbook workbook = new HSSFWorkbook();
		  Sheet sheet = workbook.createSheet("Category_Wise_Complaint_List_Report");
		  
		  CellStyle headerStyle = workbook.createCellStyle();
		    HSSFFont headerFont = workbook.createFont();
		    headerFont.setBold(true);
		    headerStyle.setFont(headerFont);
		    headerStyle.setAlignment(HorizontalAlignment.CENTER);
		    headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		    headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		    headerStyle.setBorderBottom(BorderStyle.THIN);
		    headerStyle.setBorderTop(BorderStyle.THIN);
		    headerStyle.setBorderLeft(BorderStyle.THIN);
		    headerStyle.setBorderRight(BorderStyle.THIN);
		    
		    
		  
		  Row headerRow = sheet.createRow(0);
		  
		  String[] headers = {
			        "Complaint ID", "Complaint Date", "Complaint Mode", "Consumer Number", "Consumer Details",
			        "Contact Number", "Complaint Type", "Complaint Description", "Complaint Status", "Attended Date",
			        "Attended Remarks", "Region", "Circle", "Division", "Sub Division", "Section"
			    };
          
          
          for (int i = 0; i < headers.length; i++) {
              Cell cell = headerRow.createCell(i);
              cell.setCellValue(headers[i]);
              cell.setCellStyle(headerStyle);
          }
          
          int rowNum = 1;
          for (ViewComplaintReportValueBean complaint : complaintList) {
              Row row = sheet.createRow(rowNum++);
              row.createCell(0).setCellValue(complaint.getComplaintId().doubleValue());
              row.createCell(1).setCellValue(complaint.getCreatedOnFormatted());
              row.createCell(2).setCellValue(complaint.getDevice());
              row.createCell(3).setCellValue(complaint.getServiceNumber());
              row.createCell(4).setCellValue(complaint.getServiceAddress());
              row.createCell(5).setCellValue(complaint.getMobile());
              row.createCell(6).setCellValue(complaint.getComplaintType());
              row.createCell(7).setCellValue(complaint.getComplaintDescription());
              row.createCell(8).setCellValue(complaint.getComplaintStatusValue());
              row.createCell(9).setCellValue(complaint.getAttendedDate());
              row.createCell(10).setCellValue(complaint.getAttendedRemarks());
              row.createCell(11).setCellValue(complaint.getRegionName());
              row.createCell(12).setCellValue(complaint.getCircleName());
              row.createCell(13).setCellValue(complaint.getDivisionName());
              row.createCell(14).setCellValue(complaint.getSubDivisionName());
              row.createCell(15).setCellValue(complaint.getSectionName());
              
          }
          for (int i = 0; i < headers.length; i++) {
              sheet.autoSizeColumn(i);
          }
          
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    workbook.write(outputStream);
		    workbook.close();

		    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		    try {
		        excelFile = DefaultStreamedContent.builder()
		                .name("Category_Wise_Complaint_List_Report.xls")
		                .contentType("application/vnd.ms-excel")
		                .stream(() -> inputStream)
		                .build();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
	  }
	  
	  public void exportComplaintListToPdf(List<ViewComplaintReportValueBean> complaintList) {
		  Document document = new Document(PageSize.A2.rotate());
		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		    try {
		        PdfWriter.getInstance(document, outputStream);
		        document.open();
		        
		        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD, BaseColor.BLACK);
		        Paragraph title = new Paragraph("CATEGORY WISE COMPLAINT LIST", titleFont);
		        title.setAlignment(Element.ALIGN_CENTER);
		        title.setSpacingAfter(10);
		        document.add(title);
		        

		        
		        PdfPTable table = new PdfPTable(16);
		        table.setWidthPercentage(100); 
		        table.setSpacingBefore(10); 
		        table.setSpacingAfter(10); 
		        
		        float[] columnWidths = {2f, 2f, 2f, 3f, 4f, 3f, 3f, 4f, 3f, 3f, 3f, 2f, 2f, 2f, 3f, 2f};
		        table.setWidths(columnWidths);
		        
		        String[] headers = {
		            "Complaint ID", "Complaint Date", "Complaint Mode", "Consumer Number", "Consumer Details",
		            "Contact Number", "Complaint Type", "Complaint Description", "Complaint Status", "Attended Date",
		            "Attended Remarks", "Region", "Circle", "Division", "Sub Division", "Section"
		        };

		        for (String header : headers) {
		            PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD)));
		            cell.setPadding(5);
		            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
		            table.addCell(cell);
		        }

		        if (complaintList != null && !complaintList.isEmpty()) {
		        	for (ViewComplaintReportValueBean complaint : complaintList) {
		                addCell(table, complaint.getComplaintId() != null ? complaint.getComplaintId().toString() : "");
		                addCell(table, complaint.getCreatedOnFormatted());
		                addCell(table, complaint.getDevice());
		                addCell(table, complaint.getServiceNumber());
		                addCell(table, complaint.getServiceAddress());
		                addCell(table, complaint.getMobile());
		                addCell(table, complaint.getComplaintType());
		                addCell(table, complaint.getComplaintDescription());
		                addCell(table, complaint.getComplaintStatusValue());
		                addCell(table, complaint.getAttendedDate());
		                addCell(table, complaint.getAttendedRemarks());
		                addCell(table, complaint.getRegionName());
		                addCell(table, complaint.getCircleName());
		                addCell(table, complaint.getDivisionName());
		                addCell(table, complaint.getSubDivisionName());
		                addCell(table, complaint.getSectionName());
		            }
		        } else {
		            PdfPCell noDataCell = new PdfPCell(new Phrase("No complaints available", FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC, BaseColor.RED)));
		            noDataCell.setColspan(16);
		            noDataCell.setPadding(10);
		            noDataCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		            table.addCell(noDataCell);
		        }

		        document.add(table);
		        
		        document.close();

		        pdfFile = DefaultStreamedContent.builder()
		            .contentType("application/pdf")
		            .name("Category_Wise_ComplaintList_Report.pdf")
		            .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
		            .build();

		    } catch (DocumentException e) {
		        e.printStackTrace();
		    }
		}

		private void addCell(PdfPTable table, String content) {
		    PdfPCell cell = new PdfPCell(new Phrase(content != null ? content : "", FontFactory.getFont(FontFactory.HELVETICA, 9)));
		    cell.setPadding(5);
		    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		    cell.setBorderWidth(0.5f);
		    table.addCell(cell);
		}
		
		public String getImagePath(String filename) {
		    if (filename == null || filename.isEmpty()) {
		        return "";
		    }

		    String domain = "https://ccms.tangedco.org";

		    try {
		        String secondaryUrl = domain + "/photos/" + filename;
		        if (urlExists(secondaryUrl)) {
		            System.err.println("FOUND OLD PATH: " + secondaryUrl);
		            return secondaryUrl;
		        }

		        String[] parts = filename.split("_");
		        if (parts.length >= 3) {
		            String datePartWithExtension = parts[2]; 
		            String datePart = datePartWithExtension.split("\\.")[0];
		            String[] dateParts = datePart.split("-");
		            if (dateParts.length == 3) {
		                String month = dateParts[1];
		                String yearFull = "20" + dateParts[2];

		                String primaryUrl = domain + "/ccms_images/" + yearFull + "/" + month + "/" + filename;
		                if (urlExists(primaryUrl)) {
		                    System.err.println("FOUND NEW PATH: " + primaryUrl);
		                    return primaryUrl;
		                }
		            }
		        }

		        return filename;

		    } catch (Exception e) {
		        System.err.println("Error parsing image path for: " + filename + " | " + e.getMessage());
		    }

		    return filename;
		}

		private static boolean urlExists(String urlString) {
		    try {
		        HttpURLConnection.setFollowRedirects(false);
		        HttpURLConnection con = (HttpURLConnection) new URL(urlString).openConnection();
		        con.setRequestMethod("HEAD");
		        return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
		    } catch (Exception e) {
		        return false;
		    }
		}


	 

	public DataModel getDmFilter() {
		return dmFilter;
	}

	public void setDmFilter(DataModel dmFilter) {
		this.dmFilter = dmFilter;
	}

	public StreamedContent getExcelFile() {
		return excelFile;
	}

	public void setExcelFile(StreamedContent excelFile) {
		this.excelFile = excelFile;
	}



	public StreamedContent getPdfFile() {
		return pdfFile;
	}

	public void setPdfFile(StreamedContent pdfFile) {
		this.pdfFile = pdfFile;
	}
    
	public List<RepeatedCallComplaintValueBean> getCounts() {
		return counts;
	}

	public void setCounts(List<RepeatedCallComplaintValueBean> counts) {
		this.counts = counts;
	}
    


	public Date getCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}

	public String getCurrentYear() {
		return currentYear;
	}

	public void setCurrentYear(String currentYear) {
		this.currentYear = currentYear;
	}

	public List<ViewComplaintReportValueBean> getContactHistory() {
		return contactHistory;
	}

	public void setContactHistory(List<ViewComplaintReportValueBean> contactHistory) {
		this.contactHistory = contactHistory;
	}
	public ViewComplaintReportValueBean getcomplaint() {
		return complaint;
	}

	public void setcomplaint(ViewComplaintReportValueBean complaint) {
		this.complaint = complaint;
	}

}
