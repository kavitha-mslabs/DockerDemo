package tneb.ccms.admin.controller.reportController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
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

import tneb.ccms.admin.model.CategoryBean;
import tneb.ccms.admin.model.CompDeviceBean;
import tneb.ccms.admin.model.ViewComplaintReportBean;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.valuebeans.AdminUserValueBean;
import tneb.ccms.admin.valuebeans.CallCenterUserValueBean;
import tneb.ccms.admin.valuebeans.CategoriesValueBean;
import tneb.ccms.admin.valuebeans.CompDeviceValueBean;
import tneb.ccms.admin.valuebeans.TmpYearWiseCircle;
import tneb.ccms.admin.valuebeans.TmpYearWiseSections;
import tneb.ccms.admin.valuebeans.ViewComplaintReportValueBean;

import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
@Named
@ViewScoped
public class MonthWiseAbstractReport implements Serializable {

	private static final long serialVersionUID = 1L;
	private SessionFactory sessionFactory;
	DataModel dmFilter;
	List<TmpYearWiseCircle> circleList = new ArrayList<>();
	List<TmpYearWiseSections> sectionList = new ArrayList<>();
	 private boolean cameFromInsideReport = false;
	 private boolean cameFromInsideSection=false;
	StreamedContent excelFile;
	StreamedContent pdfFile;
	List<ViewComplaintReportValueBean> complaintList = new ArrayList<>();
	String selectedCircleName =null;
	String selectedSectionName = null;
	ViewComplaintReportValueBean selectedComplaintId;
	String selectedMonth= null;
	String redirectFrom;
	
	private Date currentDate = new Date();
	private String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
	private Date currentMonthEndDate ;
	List<CompDeviceValueBean> devices;
	List<CategoriesValueBean> categories;


	public List<CompDeviceValueBean> getDevices() {
		return devices;
	}

	public void setDevices(List<CompDeviceValueBean> devices) {
		this.devices = devices;
	}

	public List<CategoriesValueBean> getCategories() {
		return categories;
	}

	public void setCategories(List<CategoriesValueBean> categories) {
		this.categories = categories;
	}

	public List<CompDeviceValueBean> getDevice() {
		return devices;
	}

	public void setDevice(List<CompDeviceValueBean> devices) {
		this.devices = devices;
	}


	@PostConstruct
	public void init() {
		System.out.println("Initializing Month wise Abstract Report...");
		sessionFactory = HibernateUtil.getSessionFactory();
		dmFilter = new DataModel();
		circleList = new ArrayList<TmpYearWiseCircle>();
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		currentMonthEndDate = cal.getTime();
		System.out.println("THE CURRENT MONTH END DATE ************"+currentMonthEndDate);
		
		
		loadAllDevicesAndCategories();

	}
	
	// REFERSH CIRCLE REPORT
	public void resetIfNeeded() {
	    if (!FacesContext.getCurrentInstance().isPostback() && !cameFromInsideReport) {
	        circleList = null;
	        dmFilter.setFromDate(null);
	        dmFilter.setToDate(null);
	    }
	    cameFromInsideReport = false; // Always reset the flag
	}

	
	// REFRESH SECTION REPORT
	public void resetSectionIfNeeded() {
		
	    if (!FacesContext.getCurrentInstance().isPostback() && !cameFromInsideSection ) {
	    	System.out.println("THE BOOLEAN-----------"+cameFromInsideSection);
	        sectionList = null;
	        dmFilter.setFromDate(null);
	        dmFilter.setToDate(null);
	    }
	    cameFromInsideSection = false;
	}

	
	
	@SuppressWarnings("unchecked")
	@Transactional
	private void loadAllDevicesAndCategories() {
	    Session session = null;
	    try {
	        session = sessionFactory.openSession();
	        
	     // Get devices ordered by ID
	        String hql = "FROM CompDeviceBean d ORDER BY d.id ASC";
	        Query<CompDeviceBean> query = session.createQuery(hql, CompDeviceBean.class);
	        List<CompDeviceBean> devicesBean = query.getResultList();

	        // Convert to value beans
	        List<CompDeviceValueBean> orderedDeviceList = devicesBean.stream()
	                .map(CompDeviceValueBean::convertCompDeviceBeanToCompDeviceValueBean)
	                .collect(Collectors.toList());

	        // Add "ALL" as the first item
	        CompDeviceValueBean allOption = new CompDeviceValueBean("L", "ALL");
	        orderedDeviceList.add(0, allOption);

	        // Get session beans
	        HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
	                .getExternalContext().getSession(false);
	        AdminUserValueBean adminUserValueBean = (AdminUserValueBean) httpsession.getAttribute("sessionAdminValueBean");
	        CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");

	        // Apply filtering based on role
	        if (callCenterValueBean != null) {
	            int roleId = callCenterValueBean.getRoleId();

	            if (roleId == 5 || roleId == 7) {
	                // Minnagam Admin or Circle Agent – only Minnagam device
	                devices = orderedDeviceList.stream()
	                        .filter(d -> "M".equals(d.getDeviceCode()))
	                        .collect(Collectors.toList());

	            } else if (roleId == 3) {
	                // Social Media User – only SM
	                devices = orderedDeviceList.stream()
	                        .filter(d -> "S".equals(d.getDeviceCode()))
	                        .collect(Collectors.toList());

	            } else {
	                // Other call center roles – show all
	                devices = orderedDeviceList;
	            }

	        } else if (adminUserValueBean != null) {
	            int roleId = adminUserValueBean.getRoleId();

	            if (roleId == 10) {
	                // Minnagam Admin (roleId 10) – only Minnagam device
	                devices = orderedDeviceList.stream()
	                        .filter(d -> "M".equals(d.getDeviceCode()))
	                        .collect(Collectors.toList());

	            } else {
	                // Other admin roles – show all
	                devices = orderedDeviceList;
	            }

	        } else {
	            // Default fallback – show all
	            devices = orderedDeviceList;
	        }

	        String hql2 = "FROM CategoryBean d WHERE d.code IS NOT NULL ORDER BY d.id ASC ";
	        Query<CategoryBean> query2 = session.createQuery(hql2, CategoryBean.class);
	        
	        List<CategoryBean> categoryBean = query2.getResultList();
	        
	        categories = categoryBean.stream().map(CategoriesValueBean::convertCategoriesBeanToCategoriesValueBean).collect(Collectors.toList());
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        devices = new ArrayList<>(); 
	        categories = new ArrayList<>();
	    } finally {
	        if (session != null && session.isOpen()) {
	            session.close();
	        }
	    }
	}

	public void clearFiltersAndCircleReport() {
		dmFilter = new DataModel();
		circleList = new ArrayList<TmpYearWiseCircle>();
	}
	
	
	
	public void adjustToFirstDay() {
	    if (dmFilter.getFromDate() != null) {
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(dmFilter.getFromDate());
	        cal.set(Calendar.DAY_OF_MONTH, 1); 
	        dmFilter.setFromDate(cal.getTime());
	    }
	}

	private void setDefaultDatesIfNeeded() {

		if (dmFilter == null) {
			dmFilter = new DataModel();
		}

		if (dmFilter.getFromDate() == null && dmFilter.getToDate() == null) {

			Calendar cal = Calendar.getInstance();
			cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
			dmFilter.setFromDate(cal.getTime());
			dmFilter.setToDate(new Date());
		} else if (dmFilter.getToDate() == null) {

			dmFilter.setToDate(new Date());
			if (dmFilter.getFromDate() == null) {
				Calendar cal = Calendar.getInstance();
				cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
				dmFilter.setFromDate(cal.getTime());
			}
		} else if (dmFilter.getFromDate() == null) {
			Calendar cal = Calendar.getInstance();
			cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
			dmFilter.setFromDate(cal.getTime());
		}

	}
	
	// LOGIN WISE FILTER
	public void updateLoginWiseFilters() {

	    HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance() .getExternalContext().getSession(false);

	    AdminUserValueBean adminUserValueBean =  (AdminUserValueBean) httpsession.getAttribute("sessionAdminValueBean");

	    CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");

	    // IF CALL CENTER USER LOGIN
	    if (callCenterValueBean != null) {
	        Integer callCenterRole = callCenterValueBean.getRoleId();
	        Integer userId = callCenterValueBean.getId();

	        switch (callCenterRole) {
	            // AE FOC
	            case 1: {
	                dmFilter.setRegionCode("A");

	                Session session = sessionFactory.openSession();
	                try {
	                    session.beginTransaction();

	                    @SuppressWarnings("unchecked")
						List<Integer> circleId = session.createQuery(
	                            "select c.circleBean.id from CallCenterMappingBean c " +
	                            "where c.callCenterUserBean.id = :userId")
	                            .setParameter("userId", userId)
	                            .getResultList();
	                    
	                    if(circleId.size()>1) {
	                    	dmFilter.setCircleCode("A");
	                    }else {
	                    	@SuppressWarnings("unchecked")
							List<Integer> regionId = session.createQuery(
		                            "select c.circleBean.regionBean.id from CallCenterMappingBean c " +
		                            "where c.callCenterUserBean.id = :userId")
		                            .setParameter("userId", userId)
		                            .getResultList();
		                    dmFilter.setCircleCode(String.valueOf(circleId.get(0)));	
		                    dmFilter.setRegionCode(String.valueOf(regionId.get(0)));
	                    }


	                    session.getTransaction().commit();
	                } finally {
	                    session.close();
	                }
	                break;
	            }

	            // SOCIAL MEDIA USER
	            case 3: {
	                dmFilter.setRegionCode("A");
	                dmFilter.setCircleCode("A");
	                dmFilter.setDevice("S");
	                break;
	            }

	            // MINNAGAM ADMIN
	            case 5: {
	                dmFilter.setRegionCode("A");
	                dmFilter.setCircleCode("A");
	                dmFilter.setDevice("MI");
	                break;
	            }

	            // CIRCLE AGENT
	            case 7: {
	                dmFilter.setRegionCode("A");
	                dmFilter.setDevice("M");

	                Session session = sessionFactory.openSession();
	                try {
	                    session.beginTransaction();

	                    @SuppressWarnings("unchecked")
						List<Integer> circleId =session.createQuery(
	                            "select c.circleBean.id from CallCenterMappingBean c " +
	                            "where c.callCenterUserBean.id = :userId")
	                            .setParameter("userId", userId)
	                            .getResultList();

	                    if(circleId.size()>1) {
	                    	dmFilter.setCircleCode("A");
	                    }else {
		                    dmFilter.setCircleCode(String.valueOf(circleId.get(0)));	
	                    }

	                    session.getTransaction().commit();
	                } finally {
	                    session.close();
	                }
	                break;
	            }
	        }
	    }

	    // IF ADMIN USER LOGIN
	    else if (adminUserValueBean != null) {
	        Integer roleId = adminUserValueBean.getRoleId();

	        if (roleId >= 6 && roleId <= 9) {
	            // HEADQUARTERS
	            dmFilter.setRegionCode("A");
	            dmFilter.setCircleCode("A");
	        } else if (roleId == 5) {
	            // REGION
	            dmFilter.setRegionCode(adminUserValueBean.getRegionId().toString());
	            dmFilter.setCircleCode("A");
	        } else if (roleId == 4) {
	            // CIRCLE
	            dmFilter.setRegionCode(adminUserValueBean.getRegionId().toString());
	            dmFilter.setCircleCode(adminUserValueBean.getCircleId().toString());
	        } else {
	            dmFilter.setRegionCode("A");
	            dmFilter.setCircleCode("A");
	        }
	    }

	    // IF NO USER LOGGED IN
	    else {
	        dmFilter.setRegionCode("A");
	        dmFilter.setCircleCode("A");
	    }
	}
	
	//CIRCLE REPORT
	@Transactional
	public void searchCircleMonthlyAbstract() {
		
		updateLoginWiseFilters();
		
		setDefaultDatesIfNeeded();		
		
		Session session = null;
		if (dmFilter.getFromDate() != null) {
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(dmFilter.getFromDate());
	        
	        Calendar currentCal = Calendar.getInstance();
            currentCal.setTime(new Date());
            
            if (cal.get(Calendar.YEAR) > currentCal.get(Calendar.YEAR) || 
                    (cal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) && 
                    		cal.get(Calendar.MONTH) > currentCal.get(Calendar.MONTH))) {
                    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error", "Selected Month is Greater Than The Current Month And Year");
                    FacesContext.getCurrentInstance().addMessage(null, message);
                    return;
                }
            
	        int year = cal.get(Calendar.YEAR);
	        int month = cal.get(Calendar.MONTH) + 1; 

	        YearMonth ym = YearMonth.of(year, month);
	        LocalDate lastDayOfMonth = ym.atEndOfMonth();

	        java.util.Date toDate = java.util.Date.from(lastDayOfMonth.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
	        dmFilter.setToDate(toDate);
	    }
		try {
			session = sessionFactory.openSession();
			session.beginTransaction();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String formattedFromDate = sdf.format(dmFilter.getFromDate());
			String formattedToDate = sdf.format(dmFilter.getToDate());
			
			
	        if(dmFilter.getComplaintType()==null) {
	        	dmFilter.setComplaintType("AL");
	        }
	        if(dmFilter.getDevice()==null) {
	        	dmFilter.setDevice("L");
	        }
	        
	        System.out.println("THE FROM DATE ============"+dmFilter.getFromDate());
	        System.out.println("THE FROM DATE ============"+dmFilter.getToDate());


			session.createNativeQuery(
					"BEGIN abst_mon_yr(:regionCode,:circleCode,:complaintCode,:device,TO_DATE(:fromDate, 'YYYY-MM-DD'), TO_DATE(:toDate, 'YYYY-MM-DD')); END;")
					.setParameter("regionCode", dmFilter.getRegionCode())
					.setParameter("circleCode", dmFilter.getCircleCode())
					.setParameter("complaintCode", dmFilter.getComplaintType())
		            .setParameter("device", dmFilter.getDevice())
					.setParameter("fromDate", formattedFromDate)
					.setParameter("toDate", formattedToDate)
					.executeUpdate();

			session.flush();
			session.getTransaction().commit();
			
			selectedMonth= new SimpleDateFormat("MMMM yyyy").format(this.dmFilter.getFromDate());

			fetchReports(session);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in database operation");
		}
	}

	public String getSelectedMonth() {
		return selectedMonth;
	}

	public void setSelectedMonth(String selectedMonth) {
		this.selectedMonth = selectedMonth;
	}

	@Transactional
	private void fetchReports(Session session) {
		try {
			 String hql ="SELECT c.REGCODE,c.CIRCODE,"
		        		+ "c.TOT1,c.LIVE1,c.TMP1,c.CPL1,"		        		
		        		+ "cir.name as CircleName,reg.name as RegionName FROM TMP_CT_DEV_MONABST_CIR c,Circle cir,Region reg "
		        		+ "WHERE cir.id =c.cirCode and reg.id =c.regCode";																																																																					
																																																																					
			List<Object[]> results = session.createNativeQuery(hql).getResultList();
			List<TmpYearWiseCircle> result = new ArrayList<>();

			for (Object[] row : results) {
				TmpYearWiseCircle report = new TmpYearWiseCircle();
				report.setRegCode((String) row[0]);
				report.setCirCode((String) row[1]);
				
				report.setTot1((BigDecimal) row[2]);//OPEN BAL
				report.setLive1((BigDecimal)row[3]);//CLOSE BAL
				report.setTmp1((BigDecimal) row[4]);// RECEIVED
				report.setCpl1((BigDecimal) row[5]);//COMPL
				
							
				report.setRegionName((String)row[7]);
				report.setCircleName((String)row[6]);
						 

				result.add(report);
			}
			
			HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
	                .getExternalContext().getSession(false);
	        CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");

	        if(callCenterValueBean!=null && (callCenterValueBean.getRoleId()==1 || callCenterValueBean.getRoleId()==7)) {
	        	int userId = callCenterValueBean.getId();
		    	@SuppressWarnings("unchecked")
				List<Integer> circleId = session.createQuery(
                        "select c.circleBean.id from CallCenterMappingBean c " +
                        "where c.callCenterUserBean.id = :userId")
                        .setParameter("userId", userId)
                        .getResultList();
		    	
		    	@SuppressWarnings("unchecked")
				List<Integer> regionId = session.createQuery(
						"select DISTINCT c.regionBean.id from CircleBean c " +
						"where c.id IN :circleId")
						.setParameter("circleId", circleId)
						.getResultList();

				result = result.stream()
					.filter(r -> regionId.contains(Integer.valueOf(r.getRegCode())))
					.filter(c -> circleId.contains(Integer.valueOf(c.getCirCode())))
					.collect(Collectors.toList());
	        }
			
			if(dmFilter.getRegionCode().equals("A")) {
				circleList = result;
			}else {
				if(dmFilter.getCircleCode().equals("A")) {
					circleList = result.stream().filter(a -> a.getRegCode().equals(dmFilter.getRegionCode())).collect(Collectors.toList());
				}else {
					circleList = result.stream().filter(a -> a.getRegCode().equals(dmFilter.getRegionCode()))
							.filter(cir -> cir.getCirCode().equals(dmFilter.getCircleCode()))
							.collect(Collectors.toList());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in fetching report");
		}
	}
	
	public void redirectToCircleReport() throws IOException {

		FacesContext.getCurrentInstance().getExternalContext().redirect("monthWiseCircleAbstract.xhtml");
		
		
	}
	public void redirectToSectionReport() throws IOException {
	
		FacesContext.getCurrentInstance().getExternalContext().redirect("monthWiseSectionAbstract.xhtml");
		
	}

	//SECTION REPORT
	@Transactional
	public void fetchReportByCircle(String circleCode,String circleName) {

		Session session = null;
		if (dmFilter.getFromDate() != null) {
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(dmFilter.getFromDate());
	        int year = cal.get(Calendar.YEAR);
	        int month = cal.get(Calendar.MONTH) + 1; 

	        YearMonth ym = YearMonth.of(year, month);
	        LocalDate lastDayOfMonth = ym.atEndOfMonth();

	        java.util.Date toDate = java.util.Date.from(lastDayOfMonth.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
	        dmFilter.setToDate(toDate);
	    }
		try {
			session = sessionFactory.openSession();
			session.beginTransaction();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String formattedFromDate = sdf.format(dmFilter.getFromDate());
			String formattedToDate = sdf.format(dmFilter.getToDate());
			
			if(dmFilter.getSectionCode()==null) {
				dmFilter.setSectionCode("A");
			}
			
	        if(dmFilter.getComplaintType()==null) {
	        	dmFilter.setComplaintType("AL");
	        }
	        if(dmFilter.getDevice()==null) {
	        	dmFilter.setDevice("L");
	        }

			session.createNativeQuery(
					"BEGIN abst_mon_yr_sec(:circd,:sectionCode,:complaintType,:device, TO_DATE(:fromDate, 'YYYY-MM-DD'), TO_DATE(:toDate, 'YYYY-MM-DD')); END;")
					.setParameter("circd", circleCode)
					.setParameter("sectionCode", dmFilter.getSectionCode())
					.setParameter("complaintType", dmFilter.getComplaintType())
					.setParameter("device", dmFilter.getDevice())
					.setParameter("fromDate", formattedFromDate)
					.setParameter("toDate", formattedToDate)
					.executeUpdate();

			session.flush();
			session.getTransaction().commit();
			
			String hql ="SELECT c.CIRCODE,c.SECCODE,"
	        		+ "c.TOT1,c.TMP1,c.CPL1,c.LIVE1,"
	        		+ "cir.name as CircleName,sec.name as SectionName,d.NAME FROM TMP_CT_DEV_MONABST_SEC c,Circle cir,Section sec,Division d "
	        		+ "WHERE cir.id =c.cirCode and sec.id =c.secCode and d.id =sec.DIVISION_ID ";

			
	        List<Object[]> results = session.createNativeQuery(hql).getResultList();

	        sectionList = new ArrayList<>();
	        
	        for (Object[] row : results) {
	        	TmpYearWiseSections report = new TmpYearWiseSections();
				report.setCirCode((String) row[0]);
				report.setSecCode((String) row[1]);
				
				report.setTot1((BigDecimal) row[2]);
				report.setTmp1((BigDecimal) row[3]);
				report.setCpl1((BigDecimal) row[4]);
				report.setLive1((BigDecimal) row[5]);
				
				report.setCircleName((String)row[6]);
				report.setSectionName((String)row[7]);
				report.setDivisionName((String) row[8]);
						 

				sectionList.add(report);
			}


	        selectedCircleName = circleName;
	        cameFromInsideReport= true;
	        cameFromInsideSection=true;
	        
	        FacesContext.getCurrentInstance().getExternalContext()
	        .getFlash().put("comingFromCircle", true);

            FacesContext.getCurrentInstance().getExternalContext().redirect("monthWiseSectionAbstract.xhtml");

	    } catch (Exception e) {
	        e.printStackTrace();
	        System.out.println("Error in database operation");
	    }
	}
	
	//SECTION REPORT
		@Transactional
		public void fetchReportForSectionUsers() {
			
			HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
	                .getExternalContext().getSession(false);
			
			AdminUserValueBean adminUserValueBean = (AdminUserValueBean) 
					httpsession.getAttribute("sessionAdminValueBean");
			
			String circleCode= adminUserValueBean.getCircleId().toString();
			String circleName =adminUserValueBean.getCircleName();
			
			String sectionCode = adminUserValueBean.getSectionId().toString();

			if(sectionCode==null || sectionCode.equals("0") || sectionCode.isEmpty()) {
				dmFilter.setSectionCode("A");
			}else {
				dmFilter.setSectionCode(sectionCode);
			}
			
			String subDivisionId = adminUserValueBean.getSubDivisionId().toString();
			String divisionId = adminUserValueBean.getDivisionId().toString();

			Session session = null;
			if (dmFilter.getFromDate() != null) {
		        Calendar cal = Calendar.getInstance();
		        cal.setTime(dmFilter.getFromDate());
		        
		        Calendar currentCal = Calendar.getInstance();
	            currentCal.setTime(new Date());
	            
	            if (cal.get(Calendar.YEAR) > currentCal.get(Calendar.YEAR) || 
	                    (cal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) && 
	                    		cal.get(Calendar.MONTH) > currentCal.get(Calendar.MONTH))) {
	                    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
	                        "Error", "Selected Month is Greater Than The Current Month And Year");
	                    FacesContext.getCurrentInstance().addMessage(null, message);
	                    return;
	                }
	            
		        int year = cal.get(Calendar.YEAR);
		        int month = cal.get(Calendar.MONTH) + 1; 

		        YearMonth ym = YearMonth.of(year, month);
		        LocalDate lastDayOfMonth = ym.atEndOfMonth();

		        java.util.Date toDate = java.util.Date.from(lastDayOfMonth.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
		        dmFilter.setToDate(toDate);
			}
			try {
				session = sessionFactory.openSession();
				session.beginTransaction();

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String formattedFromDate = sdf.format(dmFilter.getFromDate());
				String formattedToDate = sdf.format(dmFilter.getToDate());
				
		        if(dmFilter.getComplaintType()==null) {
		        	dmFilter.setComplaintType("AL");
		        }
		        if(dmFilter.getDevice()==null) {
		        	dmFilter.setDevice("L");
		        }

				session.createNativeQuery(
						"BEGIN abst_mon_yr_sec(:circd,:sectionCode,:complaintType,:device, TO_DATE(:fromDate, 'YYYY-MM-DD'), TO_DATE(:toDate, 'YYYY-MM-DD')); END;")
						.setParameter("circd", circleCode)
						.setParameter("sectionCode", dmFilter.getSectionCode())
						.setParameter("complaintType", dmFilter.getComplaintType())
						.setParameter("device", dmFilter.getDevice())
						.setParameter("fromDate", formattedFromDate)
						.setParameter("toDate", formattedToDate)
						.executeUpdate();

				session.flush();
				session.getTransaction().commit();
				
				
				String hql = "SELECT c.CIRCODE, c.SECCODE, " +
			             "c.TOT1, c.TMP1, c.CPL1, c.LIVE1, " +
			             "s.name AS SectionName, " +
			             "s.division_id AS DIVISION_ID, " +
			             "d.name AS DIVISION_NAME, " +
			             "s.sub_division_id AS SUB_DIVISION_ID " +
			             "FROM TMP_CT_DEV_MONABST_SEC c " +
			             "JOIN SECTION s ON s.id = c.seccode " +
			             "JOIN DIVISION d ON d.id = s.division_id";

				
		        List<Object[]> results = session.createNativeQuery(hql).getResultList();

		        List<TmpYearWiseSections> result = new ArrayList<TmpYearWiseSections>();
		        sectionList = new ArrayList<>();
		        
		        for (Object[] row : results) {
		        	TmpYearWiseSections report = new TmpYearWiseSections();
					report.setCirCode((String) row[0]);
					report.setSecCode((String) row[1]);
					
					report.setTot1((BigDecimal) row[2]);
					report.setTmp1((BigDecimal) row[3]);
					report.setCpl1((BigDecimal) row[4]);
					report.setLive1((BigDecimal) row[5]);
					
					report.setSectionName((String) row[6]);
					report.setDivisionId((String) row[7].toString());
					report.setDivisionName((String) row[8]);
					report.setSubDivisionId((String) row[9].toString());
							 

					result.add(report);
				}


				//SECTION
				if(adminUserValueBean.getRoleId()==1) {
					sectionList = result.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
							.filter(section -> section.getSecCode().equalsIgnoreCase(sectionCode))
							.collect(Collectors.toList());
				}
				//DIVISION
				else if(adminUserValueBean.getRoleId()==2) {
					sectionList = result.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
							.filter(sd -> sd.getSubDivisionId().equalsIgnoreCase(subDivisionId))
							.collect(Collectors.toList());
				}
				//SUB DIVISION
				else if(adminUserValueBean.getRoleId()==3) {
					sectionList = result.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
							.filter(d -> d.getDivisionId().equalsIgnoreCase(divisionId))
							.collect(Collectors.toList());
				}
				//ALL SECTION
				else {
					sectionList = result.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
							.collect(Collectors.toList());
				}

		        selectedCircleName = circleName;
		        cameFromInsideSection= true;
		        
				selectedMonth= new SimpleDateFormat("MMMM yyyy").format(this.dmFilter.getFromDate());

	            FacesContext.getCurrentInstance().getExternalContext().redirect("monthWiseSectionAbstract.xhtml");

		    } catch (Exception e) {
		        e.printStackTrace();
		        System.out.println("Error in database operation");
		    }
		}
	

	//CIRCLE REPORT TO EXCEL DOWNLOAD
	public void exportToExcel(List<TmpYearWiseCircle> reports) throws IOException {
	    HSSFWorkbook workbook = new HSSFWorkbook();
	    Sheet sheet = workbook.createSheet("Monthly_Consolidated_Abstarct_Report_CircleWise");
	    
	    // Set column widths
	    sheet.setColumnWidth(0, 2000);  // S.NO
	    sheet.setColumnWidth(1, 6000);  // CIRCLE
	    for (int i = 2; i < 6; i++) {  // Adjust to cover columns 2-5 (Open Balance to Closing Balance)
	        sheet.setColumnWidth(i, 4000); 
	    }
	    
	    // Create heading row
	    Row headingRow = sheet.createRow(0);
	    Cell headingCell = headingRow.createCell(0);
	    headingCell.setCellValue("MONTHLY CONSOLIDATED ABSTRACT REPORT - CIRCLE WISE");
	    
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
	    
	    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5)); // Adjusted to 5 columns (0-5)
	    
	    // Create subheading row
	    SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM yyyy");
	    String monthAndYear = dateFormatter.format(dmFilter.getFromDate());
	    
	    String complaintType = dmFilter.getComplaintType();
	    Map<String, String> complaintTypeMap = new HashMap<>();
	    complaintTypeMap.put("AL", "ALL");
	    complaintTypeMap.put("PF", "POWER FAILURE");
	    complaintTypeMap.put("VF", "VOLTAGE RELATED");
	    complaintTypeMap.put("ME", "METER RELATED");
	    complaintTypeMap.put("BL", "BILLING RELATED");
	    complaintTypeMap.put("FI", "FIRE");
	    complaintTypeMap.put("TH", "DANGEROUS POLE");
	    complaintTypeMap.put("TE", "THEFT OF POWER");
	    complaintTypeMap.put("CS", "CONDUCTOR SNAPPING");
	    complaintTypeMap.put("OT", "OTHERS");
	    complaintType = complaintTypeMap.getOrDefault(complaintType, "-");
	    
	    String device = dmFilter.getDevice();
	    Map<String, String> deviceMap = new HashMap<>();
	    deviceMap.put("L", "ALL");
	    deviceMap.put("P", "MOBILE");
	    deviceMap.put("W", "WEB");
	    deviceMap.put("S", "SM");
	    deviceMap.put("A", "FOC");
	    deviceMap.put("M", "MINNAGAM");
	    deviceMap.put("G", "MM");
	    device = deviceMap.getOrDefault(device, "-");
	    
	    Row subHeading = sheet.createRow(1);
	    Cell subHeadingCell = subHeading.createCell(0);
	    subHeadingCell.setCellValue("Month And Year: " + monthAndYear + "   Complaint Type: " + complaintType + "   Device: " + device);
	    
	    CellStyle headingStyle2 = workbook.createCellStyle();
	    HSSFFont dateFont = workbook.createFont();
	    dateFont.setBold(true);
	    dateFont.setFontHeightInPoints((short) 10);
	    headingStyle2.setFont(dateFont);
	    headingStyle2.setAlignment(HorizontalAlignment.CENTER);
	    headingStyle2.setVerticalAlignment(VerticalAlignment.CENTER);
	    headingStyle2.setBorderBottom(BorderStyle.THIN);
	    headingStyle2.setBorderTop(BorderStyle.THIN);
	    headingStyle2.setBorderLeft(BorderStyle.THIN);
	    headingStyle2.setBorderRight(BorderStyle.THIN);
	    subHeadingCell.setCellStyle(headingStyle2);
	    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 5)); // Adjusted to 5 columns (0-5)

	    // Create header style
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
	    
	    // Create data cell style with borders
	    CellStyle dataCellStyle = workbook.createCellStyle();
	    dataCellStyle.setBorderBottom(BorderStyle.THIN);
	    dataCellStyle.setBorderTop(BorderStyle.THIN);
	    dataCellStyle.setBorderLeft(BorderStyle.THIN);
	    dataCellStyle.setBorderRight(BorderStyle.THIN);
	    
	    // Create header rows
	    Row headerRow1 = sheet.createRow(2);
	    Row headerRow2 = sheet.createRow(3);
	    
	    // Header row 1
	    Cell snHeader1 = headerRow1.createCell(0);
	    snHeader1.setCellValue("S.NO");
	    snHeader1.setCellStyle(headerStyle);
	    
	    Cell circleHeader1 = headerRow1.createCell(1);
	    circleHeader1.setCellValue("CIRCLE");
	    circleHeader1.setCellStyle(headerStyle);
	    
	    Cell monthHeader1 = headerRow1.createCell(2);
	    monthHeader1.setCellValue(selectedMonth);
	    monthHeader1.setCellStyle(headerStyle);
	    sheet.addMergedRegion(new CellRangeAddress(2, 2, 2, 5)); // Merge month header across data columns
	    
	    // Header row 2
	    Cell snHeader2 = headerRow2.createCell(0);
	    snHeader2.setCellValue("");
	    snHeader2.setCellStyle(headerStyle);
	    
	    Cell circleHeader2 = headerRow2.createCell(1);
	    circleHeader2.setCellValue("");
	    circleHeader2.setCellStyle(headerStyle);
	    
	    Cell openBalHeader = headerRow2.createCell(2);
	    openBalHeader.setCellValue("Open Balance");
	    openBalHeader.setCellStyle(headerStyle);
	    
	    Cell receivedHeader = headerRow2.createCell(3);
	    receivedHeader.setCellValue("Received");
	    receivedHeader.setCellStyle(headerStyle);
	    
	    Cell completedHeader = headerRow2.createCell(4);
	    completedHeader.setCellValue("Completed");
	    completedHeader.setCellStyle(headerStyle);
	    
	    Cell closingBalHeader = headerRow2.createCell(5);
	    closingBalHeader.setCellValue("Closing Balance");
	    closingBalHeader.setCellStyle(headerStyle);
	    
	    // Add data rows
	    int rowNum = 4; 
	    BigDecimal[] totalSums = new BigDecimal[4];
	    Arrays.fill(totalSums, BigDecimal.ZERO);

	    int serialNumber = 1;
	    for (TmpYearWiseCircle report : reports) {
	        Row row = sheet.createRow(rowNum++);
	        
	        // S.NO column
	        Cell snCell = row.createCell(0);
	        snCell.setCellValue(serialNumber++);
	        snCell.setCellStyle(dataCellStyle);
	        
	        // CIRCLE column
	        Cell circleCell = row.createCell(1);
	        circleCell.setCellValue(report.getCircleName());
	        circleCell.setCellStyle(dataCellStyle);
	        
	        setCellValueWithStyle(row, 2, report.getTot1(), dataCellStyle);    // Open Balance
	        setCellValueWithStyle(row, 3, report.getTmp1(), dataCellStyle);    // Received
	        setCellValueWithStyle(row, 4, report.getCpl1(), dataCellStyle);    // Completed
	        setCellValueWithStyle(row, 5, report.getLive1(), dataCellStyle);   // Closing Balance

	        totalSums[0] = totalSums[0].add(report.getTot1() != null ? report.getTot1() : BigDecimal.ZERO);
	        totalSums[1] = totalSums[1].add(report.getTmp1() != null ? report.getTmp1() : BigDecimal.ZERO);
	        totalSums[2] = totalSums[2].add(report.getCpl1() != null ? report.getCpl1() : BigDecimal.ZERO);
	        totalSums[3] = totalSums[3].add(report.getLive1() != null ? report.getLive1() : BigDecimal.ZERO);
	    }

	    Row totalRow = sheet.createRow(rowNum);
	    CellStyle totalStyle = workbook.createCellStyle();
	    totalStyle.cloneStyleFrom(headerStyle); 
	    
	    Cell totalLabelCell = totalRow.createCell(0);
	    totalLabelCell.setCellValue("TOTAL");
	    totalLabelCell.setCellStyle(totalStyle);
	    
	    sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 1));
	    
	    setCellValueWithStyle(totalRow, 2, totalSums[0], totalStyle); // Open Balance
	    setCellValueWithStyle(totalRow, 3, totalSums[1], totalStyle); // Received
	    setCellValueWithStyle(totalRow, 4, totalSums[2], totalStyle); // Completed
	    setCellValueWithStyle(totalRow, 5, totalSums[3], totalStyle); // Closing Balance

	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    workbook.write(outputStream);
	    workbook.close();

	    excelFile = DefaultStreamedContent.builder()
	            .name("Monthly_Consolidated_Abstract_Report_CircleWise.xls")
	            .contentType("application/vnd.ms-excel")
	            .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
	            .build();
	}
	
	
	private void setCellValueWithStyle(Row row, int column, BigDecimal value, CellStyle style) {
	    Cell cell = row.createCell(column);
	    if (value != null) {
	        cell.setCellValue(value.doubleValue());
	    } else {
	        cell.setCellValue(0);
	    }
	    cell.setCellStyle(style);
	}

	
	//SECTION REPORT TO EXCEL DOWNLOAD 
	public void exportSectionsToExcel(List<TmpYearWiseSections> reports) throws IOException {
	    HSSFWorkbook workbook = new HSSFWorkbook();
	    Sheet sheet = workbook.createSheet("Monthly_Consolidated_Abstarct_Report_SectionWise");

	    sheet.setColumnWidth(0, 2000); //S.NO
	    sheet.setColumnWidth(1, 6000); //SECTION
	    sheet.setColumnWidth(2, 6000); //DIVISION
	    for (int i = 3; i < 7; i++) {  
	        sheet.setColumnWidth(i, 4000);
	    }
	    
	    // Main Heading Style
	    Row headingRow = sheet.createRow(0);
	    Cell headingCell = headingRow.createCell(0);
	    headingCell.setCellValue("MONTHLY CONSOLIDATED ABSTARCT REPORT - SECTION WISE");
	    
	    CellStyle headingStyle = workbook.createCellStyle();
	    HSSFFont headingFont = workbook.createFont();
	    headingFont.setBold(true);
	    headingFont.setFontHeightInPoints((short) 10);
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
	    
	    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6)); 
	    
	    SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM yyyy");
	    String monthAndYear = dateFormatter.format(dmFilter.getFromDate());
	    
	    String complaintType = dmFilter.getComplaintType();
	    
	    Map<String, String> complaintTypeMap = new HashMap<>();
	    complaintTypeMap.put("AL", "ALL");
	    complaintTypeMap.put("PF", "POWER FAILURE");
	    complaintTypeMap.put("VF", "VOLTAGE RELATED");
	    complaintTypeMap.put("ME", "METER RELATED");
	    complaintTypeMap.put("BL", "BILLING RELATED");
	    complaintTypeMap.put("FI", "FIRE");
	    complaintTypeMap.put("TH", "DANGEROUS POLE");
	    complaintTypeMap.put("TE", "THEFT OF POWER");
	    complaintTypeMap.put("CS", "CONDUCTOR SNAPPING");
	    complaintTypeMap.put("OT", "OTHERS");
	    
	    complaintType = complaintTypeMap.getOrDefault(complaintType, "-");
	    
	    String device = dmFilter.getDevice();

	    Map<String, String> deviceMap = new HashMap<>();
	    deviceMap.put("L", "ALL");
	    deviceMap.put("P", "MOBILE");
	    deviceMap.put("W", "WEB");
	    deviceMap.put("S", "SM");
	    deviceMap.put("A", "FOC");
	    deviceMap.put("M", "MINNAGAM");
	    deviceMap.put("G", "MM");
	    
	    device = deviceMap.getOrDefault(device, "-");
	    
	    // Subheading Style
	    Row subHeading = sheet.createRow(1);
	    Cell subHeadingCell = subHeading.createCell(0);
	    subHeadingCell.setCellValue("Month And Year :"+monthAndYear+"  Complaint Type :"+complaintType+"  Device :"+device +"  Circle :"+selectedCircleName);
	    
	    CellStyle headingStyle2 = workbook.createCellStyle();
	    HSSFFont dateFont = workbook.createFont();
	    dateFont.setBold(true);
	    dateFont.setFontHeightInPoints((short) 10);
	    headingStyle2.setFont(dateFont);
	    headingStyle2.setAlignment(HorizontalAlignment.CENTER);
	    headingStyle2.setVerticalAlignment(VerticalAlignment.CENTER);
	    subHeadingCell.setCellStyle(headingStyle2);
	    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6)); 

	    // Header Style for all header cells
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

	    // First header row (S.NO, SECTION, DIVISION, Month)
	    Row headerRow1 = sheet.createRow(2);
	    for (int i = 0; i < 4; i++) {
	        headerRow1.createCell(i).setCellStyle(headerStyle);
	    }
	    headerRow1.getCell(0).setCellValue("S.NO");
	    headerRow1.getCell(1).setCellValue("SECTION");
	    headerRow1.getCell(2).setCellValue("DIVISION");
	    headerRow1.getCell(3).setCellValue(selectedMonth);
	    
	    sheet.addMergedRegion(new CellRangeAddress(2, 2, 3, 6));
	    
	    // Second header row (empty for first 3 columns, then Open Balance, Received, Completed, Closing Balance)
	    Row headerRow2 = sheet.createRow(3);
	    for (int i = 0; i < 7; i++) {
	        headerRow2.createCell(i).setCellStyle(headerStyle);
	        if (i < 3) {
	            headerRow2.getCell(i).setCellValue("");
	        }
	    }
	    headerRow2.getCell(3).setCellValue("Open Balance");
	    headerRow2.getCell(4).setCellValue("Received");
	    headerRow2.getCell(5).setCellValue("Completed");
	    headerRow2.getCell(6).setCellValue("Closing Balance");

	    int rowNum = 4; 
	    BigDecimal[] totalSums = new BigDecimal[4];
	    
	    Arrays.fill(totalSums, BigDecimal.ZERO);
	    
	    int serialNumber = 1;
	    for (TmpYearWiseSections report : reports) {
	        Row row = sheet.createRow(rowNum++);
	        row.createCell(0).setCellValue(String.valueOf(serialNumber++)); // S.NO
	        row.createCell(1).setCellValue(report.getSectionName()); // SECTION
	        row.createCell(2).setCellValue(report.getDivisionName()); // DIVISION

	        setCellValue(row, 3, report.getTot1()); // Open Balance
	        setCellValue(row, 4, report.getTmp1()); // Received
	        setCellValue(row, 5, report.getCpl1()); // Completed
	        setCellValue(row, 6, report.getLive1()); // Closing Balance

	        totalSums[0] = totalSums[0].add(report.getTot1() != null ? report.getTot1() : BigDecimal.ZERO);
	        totalSums[1] = totalSums[1].add(report.getTmp1() != null ? report.getTmp1() : BigDecimal.ZERO);
	        totalSums[2] = totalSums[2].add(report.getCpl1() != null ? report.getCpl1() : BigDecimal.ZERO);
	        totalSums[3] = totalSums[3].add(report.getLive1() != null ? report.getLive1() : BigDecimal.ZERO);
	    }
	    
	    // Total row
	    Row totalRow = sheet.createRow(rowNum);
	    CellStyle totalStyle = workbook.createCellStyle();
	    totalStyle.setFont(headerFont);
	    totalStyle.setAlignment(HorizontalAlignment.CENTER);
	    totalStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	    totalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	    totalStyle.setBorderBottom(BorderStyle.THIN);
	    totalStyle.setBorderTop(BorderStyle.THIN);
	    totalStyle.setBorderLeft(BorderStyle.THIN);
	    totalStyle.setBorderRight(BorderStyle.THIN);

	    totalRow.createCell(0).setCellValue("TOTAL");
	    totalRow.getCell(0).setCellStyle(totalStyle);
	    
	    totalRow.createCell(1).setCellValue(""); 
	    totalRow.getCell(1).setCellStyle(totalStyle);
	    
	    totalRow.createCell(2).setCellValue(""); 
	    totalRow.getCell(2).setCellStyle(totalStyle);
	    
	    setCellValue(totalRow, 3, totalSums[0]); // Open Balance
	    setCellValue(totalRow, 4, totalSums[1]); // Received
	    setCellValue(totalRow, 5, totalSums[2]); // Completed
	    setCellValue(totalRow, 6, totalSums[3]); // Closing Balance

	    for (int i = 0; i < 7; i++) {
	        totalRow.getCell(i).setCellStyle(totalStyle);
	    }

	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    workbook.write(outputStream);
	    workbook.close();

	    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
	    try {
	        excelFile = DefaultStreamedContent.builder()
	                .name("Monthly_Consolidated_Abstarct_Report_SectionWise.xls")
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
	 
	 
	 //CIRCLE REPORT TO PDF DOWNLOAD
	 public void exportToPdf(List<TmpYearWiseCircle> reports) throws IOException {
		    try {
		        Document document = new Document(PageSize.A4);
		        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		        PdfWriter.getInstance(document, outputStream);

		        document.open();

		        Font titleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY);
		        Font subHeadingFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.DARK_GRAY);
		        Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
		        Font totalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);

		        Paragraph title = new Paragraph("Monthly Consolidated Abstarct Report Circle Wise", titleFont);
		        title.setAlignment(Element.ALIGN_CENTER);
		        title.setSpacingAfter(20);
		        document.add(title);

		        String complaintType = dmFilter.getComplaintType();

		        Map<String, String> complaintTypeMap = new HashMap<>();
		        complaintTypeMap.put("AL", "ALL");
		        complaintTypeMap.put("PF", "POWER FAILURE");
		        complaintTypeMap.put("VF", "VOLTAGE RELATED");
		        complaintTypeMap.put("ME", "METER RELATED");
		        complaintTypeMap.put("BL", "BILLING RELATED");
		        complaintTypeMap.put("FI", "FIRE");
		        complaintTypeMap.put("TH", "DANGEROUS POLE");
		        complaintTypeMap.put("TE", "THEFT OF POWER");
		        complaintTypeMap.put("CS", "CONDUCTOR SNAPPING");
		        complaintTypeMap.put("OT", "OTHERS");

		        complaintType = complaintTypeMap.getOrDefault(complaintType, "-");

		        String device = dmFilter.getDevice();

		        Map<String, String> deviceMap = new HashMap<>();
		        deviceMap.put("L", "ALL");
		        deviceMap.put("P", "MOBILE");
		        deviceMap.put("W", "WEB");
		        deviceMap.put("S", "SM");
		        deviceMap.put("A", "FOC");
		        deviceMap.put("M", "MINNAGAM");
		        deviceMap.put("G", "MM");

		        device = deviceMap.getOrDefault(device, "-");

		        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
		        String monthYear = dateFormat.format(dmFilter.getFromDate());

		        Paragraph subHeading = new Paragraph("MONTH : " + monthYear + " | " + "COMPLAINT TYPE :" + complaintType + " | " + "Device: " + device, subHeadingFont);
		        subHeading.setAlignment(Element.ALIGN_CENTER);
		        subHeading.setSpacingAfter(15);
		        document.add(subHeading);

		        PdfPTable table = new PdfPTable(6); 
		        table.setWidthPercentage(100);

		        float[] columnWidths = {10f,20f, 20f, 20f, 20f, 20f};
		        table.setWidths(columnWidths);

		        PdfPCell cell0 = new PdfPCell(new Phrase("S.NO", headerFont));
		        cell0.setHorizontalAlignment(Element.ALIGN_CENTER);
		        cell0.setBackgroundColor(BaseColor.GRAY);
		        cell0.setPadding(10);
		        cell0.setRowspan(2); 
		        table.addCell(cell0);

		        
		        PdfPCell cell1 = new PdfPCell(new Phrase("CIRCLE", headerFont));
		        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
		        cell1.setBackgroundColor(BaseColor.GRAY);
		        cell1.setPadding(10);
		        cell1.setRowspan(2); 
		        table.addCell(cell1);

		        PdfPCell cell2 = new PdfPCell(new Phrase(selectedMonth, headerFont));
		        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
		        cell2.setBackgroundColor(BaseColor.GRAY);
		        cell2.setPadding(10);
		        cell2.setColspan(4); 
		        table.addCell(cell2);

		        String[] subHeaders = {"OPEN BALANCE", "RECEIVED", "COMPLETED", "CLOSING BALANCE"};
		        for (String header : subHeaders) {
		            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
		            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		            cell.setBackgroundColor(BaseColor.GRAY);
		            cell.setPadding(10);
		            table.addCell(cell);
		        }

		        BigDecimal totalOpenBalance = BigDecimal.ZERO;
		        BigDecimal totalReceived = BigDecimal.ZERO;
		        BigDecimal totalCompleted = BigDecimal.ZERO;
		        BigDecimal totalClosingBalance = BigDecimal.ZERO;

		        int serialNumber=1;
		        for (TmpYearWiseCircle report : reports) {
		        	table.addCell(String.valueOf(serialNumber++)); //SNO
		            table.addCell(report.getCircleName()); // CIRCLE
		            addNumericCell(table, report.getTot1()); // OPEN BALANCE
		            addNumericCell(table, report.getTmp1()); // RECEIVED
		            addNumericCell(table, report.getCpl1()); // COMPLETED
		            addNumericCell(table, report.getLive1()); // CLOSING BALANCE

		            totalOpenBalance = totalOpenBalance.add(report.getTot1() != null ? report.getTot1() : BigDecimal.ZERO);
		            totalReceived = totalReceived.add(report.getTmp1() != null ? report.getTmp1() : BigDecimal.ZERO);
		            totalCompleted = totalCompleted.add(report.getCpl1() != null ? report.getCpl1() : BigDecimal.ZERO);
		            totalClosingBalance = totalClosingBalance.add(report.getLive1() != null ? report.getLive1() : BigDecimal.ZERO);
		        }

		        PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL", totalFont));
		        totalLabelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		        totalLabelCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
		        totalLabelCell.setPadding(10);
		        totalLabelCell.setRowspan(1);
		        totalLabelCell.setColspan(2);
		        table.addCell(totalLabelCell);

		        addTotalCell(table, totalOpenBalance); // OPEN BALANCE
		        addTotalCell(table, totalReceived); // RECEIVED
		        addTotalCell(table, totalCompleted); // COMPLETED
		        addTotalCell(table, totalClosingBalance); // CLOSING BALANCE

		        document.add(table);
		        document.close();

		        pdfFile = DefaultStreamedContent.builder()
		                .contentType("application/pdf")
		                .name("Monthly_Consolidated_Abstarct_Report_CircleWise.pdf")
		                .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
		                .build();

		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}


		private void addNumericCell(PdfPTable table, BigDecimal value) {
		    PdfPCell cell = new PdfPCell(new Phrase(value.toString()));
		    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		    cell.setPadding(5);
		    table.addCell(cell);
		}

		private void addTotalCell(PdfPTable table, BigDecimal value) {
		    PdfPCell cell = new PdfPCell(new Phrase(value.toString(), new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
		    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
		    cell.setPadding(10);
		    table.addCell(cell);
		}
	  
		//SECTION REPORT TO PDF DOWNLOAD
		public void exportSectionsToPdf(List<TmpYearWiseSections> reports) throws IOException {
		    try {
		        Document document = new Document(PageSize.A4.rotate()); 
		        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		        PdfWriter.getInstance(document, outputStream);

		        document.open();

		        Font titleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY);
		        Font subHeadingFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.DARK_GRAY);
		        Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
		        Font totalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);

		        Paragraph title = new Paragraph("MONTHLY CONSOLIDATED SECTION ABSTRACT REPORT", titleFont);
		        title.setAlignment(Element.ALIGN_CENTER);
		        title.setSpacingAfter(20);
		        document.add(title);

		        String complaintType = dmFilter.getComplaintType();
		        Map<String, String> complaintTypeMap = new HashMap<>();
		        complaintTypeMap.put("AL", "ALL");
		        complaintTypeMap.put("PF", "POWER FAILURE");
		        complaintTypeMap.put("VF", "VOLTAGE RELATED");
		        complaintTypeMap.put("ME", "METER RELATED");
		        complaintTypeMap.put("BL", "BILLING RELATED");
		        complaintTypeMap.put("FI", "FIRE");
		        complaintTypeMap.put("TH", "DANGEROUS POLE");
		        complaintTypeMap.put("TE", "THEFT OF POWER");
		        complaintTypeMap.put("CS", "CONDUCTOR SNAPPING");
		        complaintTypeMap.put("OT", "OTHERS");

		        complaintType = complaintTypeMap.getOrDefault(complaintType, "-");

		        String device = dmFilter.getDevice();
		        Map<String, String> deviceMap = new HashMap<>();
		        deviceMap.put("L", "ALL");
		        deviceMap.put("P", "MOBILE");
		        deviceMap.put("W", "WEB");
		        deviceMap.put("S", "SM");
		        deviceMap.put("A", "FOC");
		        deviceMap.put("M", "MINNAGAM");
		        deviceMap.put("G", "MM");

		        device = deviceMap.getOrDefault(device, "-");

		        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
		        String monthYear = dateFormat.format(dmFilter.getFromDate());

		        Paragraph subHeading = new Paragraph("CIRCLE :"+selectedCircleName +" | "+
		                "MONTH : " + monthYear +" | "+ "COMPLAINT TYPE :" +complaintType+" | "+
		                "Device: "+device, subHeadingFont);
		        subHeading.setAlignment(Element.ALIGN_CENTER);
		        subHeading.setSpacingAfter(15);
		        document.add(subHeading);

		        PdfPTable table = new PdfPTable(7);
		        table.setWidthPercentage(100);
		        float[] columnWidth = {10f,20f, 20f, 15f, 15f, 15f, 15f}; 
		        table.setWidths(columnWidth);

		        PdfPCell sNoHeader = new PdfPCell(new Phrase("S.NO", headerFont));
		        PdfPCell sectionHeader = new PdfPCell(new Phrase("SECTION", headerFont));
		        PdfPCell divisionHeader = new PdfPCell(new Phrase("DIVISION", headerFont));
		        PdfPCell monthHeader = new PdfPCell(new Phrase(monthYear, headerFont));
		        monthHeader.setColspan(4); 

		        sNoHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
		        sectionHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
		        divisionHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
		        monthHeader.setHorizontalAlignment(Element.ALIGN_CENTER);

		        sNoHeader.setBackgroundColor(BaseColor.GRAY);
		        sectionHeader.setBackgroundColor(BaseColor.GRAY);
		        divisionHeader.setBackgroundColor(BaseColor.GRAY);
		        monthHeader.setBackgroundColor(BaseColor.GRAY);

		        sNoHeader.setPadding(10);
		        sectionHeader.setPadding(10);
		        divisionHeader.setPadding(10);
		        monthHeader.setPadding(10);
		        
		        sNoHeader.setRowspan(2);
		        sectionHeader.setRowspan(2);
		        divisionHeader.setRowspan(2);

		        table.addCell(sNoHeader);
		        table.addCell(sectionHeader);
		        table.addCell(divisionHeader);
		        table.addCell(monthHeader);

		        // Header Row 2 (Sub-columns)
		        String[] subHeaders = {"OPEN BALANCE", "RECEIVED", "COMPLETED", "CLOSING BALANCE"};
		        for (String subHeader : subHeaders) {
		            PdfPCell subCell = new PdfPCell(new Phrase(subHeader, headerFont));
		            subCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		            subCell.setBackgroundColor(BaseColor.DARK_GRAY);
		            subCell.setPadding(10);
		            table.addCell(subCell);
		        }
		        
		        // Initialize totals
		        BigDecimal totalTot1 = BigDecimal.ZERO;
		        BigDecimal totalCpl1 = BigDecimal.ZERO;
		        BigDecimal totalPend1 = BigDecimal.ZERO;
		        BigDecimal totalOthers = BigDecimal.ZERO;

		        int serialNumber=1;
		        
		        for (TmpYearWiseSections report : reports) {
		        	table.addCell(String.valueOf(serialNumber++));
		            table.addCell(report.getSectionName());
		            table.addCell(report.getDivisionName());
		            addNumericCell(table, report.getTot1());
		            addNumericCell(table, report.getTmp1());
		            addNumericCell(table, report.getCpl1());
		            addNumericCell(table, report.getLive1()); // New Others Column

		            // Update totals
		            totalTot1 = totalTot1.add(report.getTot1());
		            totalCpl1 = totalCpl1.add(report.getTmp1());
		            totalPend1 = totalPend1.add(report.getCpl1());
		            totalOthers = totalOthers.add(report.getLive1());
		        }

		        // Total Row
		        PdfPCell totalCell = new PdfPCell(new Phrase("TOTAL", totalFont));
		        totalCell.setColspan(3);
		        totalCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		        totalCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
		        totalCell.setPadding(10);
		        table.addCell(totalCell);

		        addTotalCell(table, totalTot1);
		        addTotalCell(table, totalCpl1);
		        addTotalCell(table, totalPend1);
		        addTotalCell(table, totalOthers);

		        document.add(table);
		        document.close();

		        pdfFile = DefaultStreamedContent.builder()
		                .contentType("application/pdf")
		                .name("Monthly_Consolidated_Abstarct_Report_SectionWise.pdf")
		                .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
		                .build();

		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}

	  
	  //COMPLAINT LIST
	  @Transactional
		public void getComplaintListForSection(String secCode,String sectionName) throws IOException {
			try (Session session = sessionFactory.openSession()) {
				
				List<String> complaintTypes = new ArrayList<>();
		        List<String> devices = new ArrayList<>();

		        
				if (dmFilter.getComplaintType() != null) {
		            switch (dmFilter.getComplaintType().toUpperCase()) {
		                case "BL": complaintTypes.add("BL"); break;
		                case "ME": complaintTypes.add("ME"); break;
		                case "PF": complaintTypes.add("PF"); break;
		                case "VF": complaintTypes.add("VF"); break;
		                case "FI": complaintTypes.add("FI"); break;
		                case "TH": complaintTypes.add("TH"); break;
		                case "TE": complaintTypes.add("TE"); break;
		                case "OT": complaintTypes.add("OT"); break;
		                case "CS": complaintTypes.add("CS"); break;
		                case "AL": complaintTypes.addAll(Arrays.asList("BL","ME","PF","VF","FI","TH","TE","OT","CS")); break;
		                default: throw new IllegalArgumentException("Invalid Complaint Type");
		            }
		        }
				
				if (dmFilter.getDevice() != null) {
		            switch (dmFilter.getDevice().toUpperCase()) {
		                case "P": devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile")); break;
		                case "W": devices.add("web"); break;
		                case "S": devices.add("SM"); break;
		                case "A": devices.addAll(Arrays.asList("admin", "FOC")); break;
		                case "M": devices.add("MI"); break;
		                case "G": devices.add("MM"); break;
		                case "O": devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile","web","SM","MI","MM")); break;
		                case "L": devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile","web","SM","admin","FOC","MI","MM")); break;
		                default: throw new IllegalArgumentException("Invalid Device Type");
		            }
		        }
							
				
				Timestamp fromDate = new Timestamp(dmFilter.getFromDate().getTime());
				Timestamp toDate = new Timestamp(dmFilter.getToDate().getTime());
				
		
			Integer sectionCode = Integer.parseInt(secCode);
			
	        StringBuilder hql = new StringBuilder("SELECT a.id, "+
	        		"to_char(a.created_on, 'dd-mm-yyyy-hh24:mi') AS Complaint_Date, " +
	                "DECODE(a.device, 'web', 'Web', 'FOC', 'FOC', 'admin', 'FOC', 'SM', 'Social Media', 'Android', 'Mobile', 'AMOB', 'Mobile', 'IMOB', 'Mobile', 'iOS', 'Mobile', 'mobile', 'Mobile', 'MI', 'Minnagam') AS Device, "+
	                "a.SERVICE_NUMBER AS Service_Number, " +
	                "a.SERVICE_NAME AS Service_Name, "+
	                "a.SERVICE_ADDRESS AS Service_Address, " +
	                "b.mobile AS Contact_Number, k.name AS Complaint_Type, d.name AS subctyp, " +
	                "a.description AS Complaint_Description, " +
	                "DECODE(a.status_id, 0, 'Pending', 1, 'In Progress', 2, 'Completed') AS Complaint_Status, " +
	                "(CASE WHEN a.status_id=2 then TO_CHAR(a.updated_on, 'DD-MM-YYYY-HH24:MI') else '' end) AS Attended_Date, " +
	                "(CASE WHEN a.status_id=2 then c.description else '' end) AS Attended_Remarks, " +
	                "f.name AS Region, g.name AS Circle, h.name AS Division, i.name AS SubDivision, j.name AS Section " +
	                "FROM COMPLAINT a " +
	                "JOIN PUBLIC_USER b ON a.user_id = b.id " +
	                "JOIN COMPLAINT_HISTORY c ON a.id = c.complaint_id AND a.status_id = c.status_id " +
	                "JOIN SUB_CATEGORY d ON a.sub_category_id = d.id " +
	                "JOIN CATEGORY k ON a.complaint_type = k.code " +
	                "JOIN REGION f ON a.region_id = f.id " +
	                "JOIN CIRCLE g ON a.circle_id = g.id " +
	                "JOIN DIVISION h ON a.division_id = h.id " +
	                "JOIN SUB_DIVISION i ON a.sub_division_id = i.id " +
	                "JOIN SECTION j ON a.section_id = j.id " +
	                "WHERE a.SECTION_ID = :sectionCode " +
	                "AND a.created_on BETWEEN :fromDate AND :toDate");
	        
	        if (!complaintTypes.isEmpty()) {
	            hql.append(" AND a.COMPLAINT_TYPE IN (:complaintTypes)");
	        }

	        if (!devices.isEmpty()) {
	            hql.append(" AND a.DEVICE IN (:devices)");
	        }			
	        Query query = session.createNativeQuery(hql.toString());
	        query.setParameter("sectionCode", sectionCode);
	        query.setParameter("fromDate", fromDate);
	        query.setParameter("toDate", toDate);
	        
	        if (!complaintTypes.isEmpty()) {
	            query.setParameter("complaintTypes", complaintTypes);
	        }

	        if (!devices.isEmpty()) {
	            query.setParameter("devices", devices);
	        }
					
	        List<Object[]> results = query.getResultList();

	        complaintList = new ArrayList<ViewComplaintReportValueBean>();
	        for (Object[] row : results) {
	        	ViewComplaintReportValueBean dto = new ViewComplaintReportValueBean();
	        	dto.setComplaintId((BigDecimal)row[0]);
	        	dto.setCreatedOnFormatted((String) row[1]);
	        	dto.setDevice((String)row[2]);
	        	dto.setServiceNumber((String)row[3]);
	        	dto.setServiceName((String)row[4]);
	        	dto.setServiceAddress((String) row[5]);
	        	dto.setMobile((String) row[6]);
	        	dto.setComplaintType((String) row[7]);
	        	dto.setSubCategoryName((String) row[8]);
	        	dto.setComplaintDescription((String) row[9]);
	        	dto.setComplaintStatusValue((String) row[10]);
	        	dto.setAttendedDate((String) row[11]);
	        	dto.setAttendedRemarks((String) row[12]);
	        	dto.setRegionName((String) row[13]);
	        	dto.setCircleName((String) row[14]);
	        	dto.setDivisionName((String) row[15]);
	        	dto.setSubDivisionName((String) row[16]);
	        	dto.setSectionName((String) row[17]);
	        	
	        	complaintList.add(dto);
	        }	        
	        selectedSectionName = sectionName;
	        

	        
	        FacesContext.getCurrentInstance().getExternalContext().redirect("monthWiseAbstractComplaintList.xhtml");
			
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}

		// COMPLAINT'S DETAILED VIEW
		public void getComplaintDetailForAbstractReport() {
			
			try (Session session = sessionFactory.openSession()) {
			String complaintIdParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("complaintID");
			System.out.println("THE COMPLAINT IS IS CALLED =============="+complaintIdParam);

			
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
	                "(CASE WHEN a.status_id=2 then c.description else '' end) AS Attended_Remarks, " +
	                "f.name AS Region, g.name AS Circle, h.name AS Division, i.name AS SubDivision, j.name AS Section, "+
	                "b.FIRST_NAME AS UserName, "+
	                "a.ALTERNATE_MOBILE_NO as RegisteredMobileNumber, "+
	                "to_char(fb.ENTRYDT, 'dd-mm-yyyy-hh24:mi') AS feedBackEntryDate, "+
	                "fb.REMARKS AS feedbackRemarks, "+
	                "fb.RATING AS feedbackRating "+
//	                "to_char(ct.TRF_ON, 'dd-mm-yyyy-hh24:mi') AS transferedOn, "+
//	                "ct.TRF_USER AS transferedUser, "+
//	                "ct.REMARKS AS transferedRemarks, "+
//	                "to_char(qc.QC_ON, 'dd-mm-yyyy-hh24:mi') AS qcDate, "+
//	                "qc.QC_STATUS AS qcStatus, "+
//	                "qc.REMARKS AS qcRemarks "+
	                "FROM COMPLAINT a " +
	                "JOIN PUBLIC_USER b ON a.user_id = b.id " +
	                "JOIN COMPLAINT_HISTORY c ON a.id = c.complaint_id AND a.status_id = c.status_id " +
	                "LEFT JOIN COMP_FEEDBACK fb ON a.id = fb.COMP_ID "+
	                "LEFT JOIN COMP_TRANSFER ct ON a.id = ct.COMP_ID "+
	                "LEFT JOIN COMP_QC_DETAILS qc ON a.id = qc.COMP_ID "+
	                "JOIN SUB_CATEGORY d ON a.sub_category_id = d.id " +
	                "JOIN CATEGORY k ON a.complaint_type = k.code " +
	                "JOIN REGION f ON a.region_id = f.id " +
	                "JOIN CIRCLE g ON a.circle_id = g.id " +
	                "JOIN DIVISION h ON a.division_id = h.id " +
	                "JOIN SUB_DIVISION i ON a.sub_division_id = i.id " +
	                "JOIN SECTION j ON a.section_id = j.id " +
	                "WHERE a.id = :complaintIdParam " ;
			
			Query query = session.createNativeQuery(hql);
			query.setParameter("complaintIdParam", complaintIdParam);
			
			List<Object[]> results = query.getResultList();

			selectedComplaintId = new ViewComplaintReportValueBean();
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
//	        	dto.setComplaintTransferedOn((String) row[23]);
//	        	dto.setComplaintTransferedBy((String) row[24]);
//	            dto.setComplaintTransferedRemarks((String) row[25]);
//	            dto.setQcDate((String) row[26]);
//	            dto.setQcStatus((String) row[27]);
//	            dto.setQcRemarks((String) row[28]);        	
	        	
	        	selectedComplaintId=dto;
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
	        selectedComplaintId.setTransferDetails(transfers);
	        
	        
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
			  selectedComplaintId.setQualityCheckDetails(qcs);
	        
	        System.out.println("THE SELECED COMPLAINT ID"+ selectedComplaintId.getComplaintId());
	        System.out.println("THE SELECED COMPLAINT ID"+ selectedComplaintId.getDescription());

			
			}catch(Exception e) {
				e.printStackTrace();
				
			}

			
		}
	  
	  //COMPLAINT LIST EXCEL
	  public void exportComplaintListToExcel(List<ViewComplaintReportValueBean> complaintList) throws IOException {
		  HSSFWorkbook workbook = new HSSFWorkbook();
		  Sheet sheet = workbook.createSheet("Monthly_Abstract_ComplaintList_Report");
		  
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
              row.createCell(7).setCellValue(complaint.getCompletedRemarks());
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
		                .name("Monthly_Abstract_ComplaintList_Report.xls")
		                .contentType("application/vnd.ms-excel")
		                .stream(() -> inputStream)
		                .build();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
	  }
	  
	  //COMPLAINT LIST PDF
	  public void exportComplaintListToPdf(List<ViewComplaintReportValueBean> complaintList) {
		  Document document = new Document(PageSize.A2.rotate());
		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		    try {
		        PdfWriter.getInstance(document, outputStream);
		        document.open();
		        
		        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD, BaseColor.BLACK);
		        Paragraph title = new Paragraph("MONTHLY ABSTRACT COMPLAINT LIST", titleFont);
		        title.setAlignment(Element.ALIGN_CENTER);
		        title.setSpacingAfter(10);
		        document.add(title);
		        
		        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY);
		        Paragraph subTitle = new Paragraph("Circle: " + selectedCircleName + "  |  Section: " + selectedSectionName, subtitleFont);
		        subTitle.setAlignment(Element.ALIGN_CENTER);
		        subTitle.setSpacingAfter(10);
		        document.add(subTitle);
		        
		        PdfPTable table = new PdfPTable(16);
		        table.setWidthPercentage(100); 
		        table.setSpacingBefore(10); 
		        table.setSpacingAfter(10); 
		        
		        float[] columnWidths = {1.5f, 1.5f, 1.5f, 2f, 3f, 2f, 2f, 3f, 2f, 2f, 2f, 1.5f, 1.5f, 1.5f, 2f, 1.5f};
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
		                addCell(table, complaint.getCompletedRemarks());
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
		            .name("Monthly_Abstract_ComplaintList_Report.pdf")
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
		
	 

	public DataModel getDmFilter() {
		return dmFilter;
	}

	public void setDmFilter(DataModel dmFilter) {
		this.dmFilter = dmFilter;
	}

	public List<TmpYearWiseSections> getSectionList() {
		return sectionList;
	}

	public void setSectionList(List<TmpYearWiseSections> sectionList) {
		this.sectionList = sectionList;
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

	public List<TmpYearWiseCircle> getCircleList() {
		return circleList;
	}

	public void setCircleList(List<TmpYearWiseCircle> circleList) {
		this.circleList = circleList;
	}


	public List<ViewComplaintReportValueBean> getComplaintList() {
		return complaintList;
	}

	public void setComplaintList(List<ViewComplaintReportValueBean> complaintList) {
		this.complaintList = complaintList;
	}


	public String getSelectedCircleName() {
		return selectedCircleName;
	}

	public void setSelectedCircleName(String selectedCircleName) {
		this.selectedCircleName = selectedCircleName;
	}

	public String getSelectedSectionName() {
		return selectedSectionName;
	}

	public void setSelectedSectionName(String selectedSectionName) {
		this.selectedSectionName = selectedSectionName;
	}
	public ViewComplaintReportValueBean getSelectedComplaintId() {
		return selectedComplaintId;
	}

	public void setSelectedComplaintId(ViewComplaintReportValueBean selectedComplaintId) {
		this.selectedComplaintId = selectedComplaintId;
	}

	public String getRedirectFrom() {
		return redirectFrom;
	}

	public void setRedirectFrom(String redirectFrom) {
		this.redirectFrom = redirectFrom;
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


	public Date getCurrentMonthEndDate() {
		return currentMonthEndDate;
	}


	public void setCurrentMonthEndDate(Date currentMonthEndDate) {
		this.currentMonthEndDate = currentMonthEndDate;
	}



}
