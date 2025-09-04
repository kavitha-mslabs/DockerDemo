package tneb.ccms.admin.controller.reportController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Year;
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
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.poi.hssf.usermodel.*;
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
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import tneb.ccms.admin.model.CategoryBean;
import tneb.ccms.admin.model.CompDeviceBean;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.valuebeans.AdminUserValueBean;
import tneb.ccms.admin.valuebeans.CallCenterUserValueBean;
import tneb.ccms.admin.valuebeans.CategoriesValueBean;
import tneb.ccms.admin.valuebeans.CompDeviceValueBean;
import tneb.ccms.admin.valuebeans.ComparisonReportValueBean;
import tneb.ccms.admin.valuebeans.ComparisonSectionValueBean;
import tneb.ccms.admin.valuebeans.MonthValueBean;
import tneb.ccms.admin.valuebeans.ViewComplaintReportValueBean;

@Named
@ViewScoped
public class MonthWiseComparisonReport {
	
	SessionFactory sessionFactory;
	DataModel dmFilter;
	List<ComparisonReportValueBean> resultList;
	List<ComparisonSectionValueBean> sectionList;
    private boolean cameFromInsideReport = false;
    private boolean cameFromInsideSection= false;
	StreamedContent excelFile;
	StreamedContent pdfFile;
	List<ViewComplaintReportValueBean> complaintList = new ArrayList<>();
	String selectedCircleName =null;
	String selectedSectionName = null;
	ViewComplaintReportValueBean selectedComplaintId;
	private List<String> yearList = new ArrayList<String>();
	List<CompDeviceValueBean> devices;
	List<CategoriesValueBean> categories;
	int currentYear ;
	int currentMonth;
	private List<MonthValueBean> availableMonth1;
	private List<MonthValueBean> availableMonth2;
	
	



	
	@PostConstruct
	public void init() {
		System.out.println("Initializing MONTH WISE Comparison report...");
		sessionFactory = HibernateUtil.getSessionFactory();
		dmFilter = new DataModel();
		resultList = new ArrayList<ComparisonReportValueBean>();
     
		// YEAR LIST
        int yearNow = Year.now().getValue();

        for (int year = 2020; year <= yearNow; year++) {
            yearList.add(String.valueOf(year));
        }
        
        // CURRENT YEAR AVAILABLE MONTHS FOR YEAR 1 AND YEAR 2
        Calendar now = Calendar.getInstance();
        currentYear = now.get(Calendar.YEAR);
        currentMonth = now.get(Calendar.MONTH) + 1; 
        
        availableMonth1 = getAvailableMonths(String.valueOf(currentYear));
        availableMonth2 = getAvailableMonths(String.valueOf(currentYear));
       
        //CURRENT YEAR
        dmFilter.setYear1("2025");
        dmFilter.setYear2("2025");
        
        // CATEGORY AND DEVICE
        loadAllDevicesAndCategories();
        
	}
	
	private List<MonthValueBean> getAvailableMonths(String year) {
		List<MonthValueBean> months = new ArrayList<>();
	    int yearGiven = Integer.parseInt(year);
	    
	    int maxMonth = 12;
	    if (yearGiven == currentYear) {
	        maxMonth = currentMonth;
	    }
	    
	    String[] monthNames = {"January", "February", "March", "April", "May", "June", 
	                          "July", "August", "September", "October", "November", "December"};
	    
	    for (int i = 1; i <= maxMonth; i++) {
	        String monthValue = String.format("%02d", i);
	        months.add(new MonthValueBean(monthNames[i-1], monthValue));
	    }
	    
	    return months;
	}
	
	public void updateAvailableMonths1() {
	    availableMonth1 = getAvailableMonths(dmFilter.getYear1());
	}

	public void updateAvailableMonths2() {
	    availableMonth2 = getAvailableMonths(dmFilter.getYear2());
	}

	// REFERSH CIRCLE REPORT
	public void resetIfNeeded() {
	    if (!FacesContext.getCurrentInstance().isPostback() && !cameFromInsideReport) {
	        resultList = null;
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
	
	// REFRESH BUTTON
	public void clearFiltersAndPage() {
		resultList = new ArrayList<ComparisonReportValueBean>();
		dmFilter = new DataModel();
        System.out.println("THE CIRCLE REPORT INITIALIZED");
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
	public void search() {	
		
		updateLoginWiseFilters();
		
		Session session = null;
		try {
			
			if (!validateMonthAndYearInputs()) {
	            return; 
	        }
			
			session = sessionFactory.openSession();
			session.beginTransaction();
			
			String complaintType = dmFilter.getComplaintType();
			String device = dmFilter.getDevice();
			String year1 = dmFilter.getYear1();
			String month1 = dmFilter.getMonth1();
			String year2 = dmFilter.getYear2();
			String month2 = dmFilter.getMonth2();
			
			System.out.println("THE COMPLAINT TYPE--------------"+complaintType);
			System.out.println("THE DEVICE---------------------"+device);
			System.out.println("THE YEAR 1 AND MONTH 1-------------"+year1 +","+ month1);
			System.out.println("THE YEAR 2 AND MONTH 2-------------"+year2 +","+ month2);


			session.createNativeQuery("BEGIN comp_cir(:regionCode, :circleCode,:complaintType, :device,:year1,:month1,:year2,:month2); END;")
					.setParameter("regionCode", dmFilter.getRegionCode())
					.setParameter("circleCode", dmFilter.getCircleCode())
					.setParameter("complaintType", complaintType)
					.setParameter("device", device)
					.setParameter("year1", year1).setParameter("month1", month1)
					.setParameter("year2", year2).setParameter("month2", month2)
					.executeUpdate();

			session.flush();
			session.getTransaction().commit();

			fetchReports(session);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in database operation");
		}
	}
	
	private boolean validateMonthAndYearInputs() {
	    String year1 = dmFilter.getYear1();
	    String month1 = dmFilter.getMonth1();
	    String year2 = dmFilter.getYear2();
	    String month2 = dmFilter.getMonth2();

	    //IF MONTH & YEAR 1 AND MONTH & YEAR 2 ARE SAME
	    if (year1.equals(year2) && month1.equals(month2)) {
	        FacesContext.getCurrentInstance().addMessage(null,
	                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Month and Year 1 cannot be the same as Month and Year 2."));
	        return false;
	    }

	    Calendar currentDate = Calendar.getInstance();
	    int currentYear = currentDate.get(Calendar.YEAR);
	    int currentMonth = currentDate.get(Calendar.MONTH) + 1; 

	    // IF MONTH & YEAR 1 IS ABOVE MONTH & YEAR 2  
	    if (isDateAfterCurrent(Integer.parseInt(year1), Integer.parseInt(month1), currentYear, currentMonth)) {
	        FacesContext.getCurrentInstance().addMessage(null,
	                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Month and Year 1 cannot be above the current month and year."));
	        return false;
	    }

	    // IF MONTH & YEAR 1 AND MONTH & YEAR 2 ARE ABOVE CURRENT DATE
	    if (isDateAfterCurrent(Integer.parseInt(year2), Integer.parseInt(month2), currentYear, currentMonth)) {
	        FacesContext.getCurrentInstance().addMessage(null,
	                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Month and Year 2 cannot be above the current month and year."));
	        return false;
	    }

	    return true;
	}
	
	private boolean isDateAfterCurrent(int year, int month, int currentYear, int currentMonth) {
	    if (year > currentYear) {
	        return true;
	    } else if (year == currentYear && month > currentMonth) {
	        return true;
	    }
	    return false;
	}

	@Transactional
	private void fetchReports(Session session) {
		try {
			
			String hql ="SELECT c.regCode as Regioncode,c.cirCode as CircleCode,reg.name as RegionName,cir.name as CircleName,"
	        		+ "c.TOT1,c.CPL1,(c.LIVE1+c.TMP1),"
	        		+ "c.TOT2,c.CPL2,(c.LIVE2+c.TMP2) "
	        		+ "FROM TMP_CT_DEV_MONABST_CIR c,Circle cir,Region reg "
	        		+ "WHERE cir.id =c.cirCode and reg.id =c.regCode";
			
			List<Object[]> results = session.createNativeQuery(hql).getResultList();
			resultList = new ArrayList<>();

			for (Object[] row : results) {
				ComparisonReportValueBean report = new ComparisonReportValueBean();
				report.setRegCode((String) row[0]);
				report.setCirCode((String) row[1]);
				report.setRegionName((String) row[2]);
				report.setCircleName((String) row[3]);

				report.setTot1((BigDecimal) row[4]);
				report.setCpl1((BigDecimal) row[5]);
				report.setPend1((BigDecimal) row[6]);
				
				report.setTot2((BigDecimal) row[7]);
				report.setCpl2((BigDecimal) row[8]);
				report.setPend2((BigDecimal) row[9]);
						 

				resultList.add(report);
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

				resultList = resultList.stream()
					.filter(r -> regionId.contains(Integer.valueOf(r.getRegCode())))
					.filter(c -> circleId.contains(Integer.valueOf(c.getCirCode())))
					.collect(Collectors.toList());
	        }

			System.out.println("COUNT: " + resultList.size());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in fetching report");
		}
	}
	
	
	// SECTION REPORT
	@Transactional
	public void fetchComparisonSections(String circleCode,String circleName) {
		
		if(dmFilter.getSectionCode()==null) {
			dmFilter.setSectionCode("A");
		}
		Session session = null;
		try {
			session = sessionFactory.openSession();
			session.beginTransaction();
			
			String complaintType = dmFilter.getComplaintType();
			String device = dmFilter.getDevice();
			String year1 = dmFilter.getYear1();
			String month1 = dmFilter.getMonth1();
			String year2 = dmFilter.getYear2();
			String month2 = dmFilter.getMonth2();

			session.createNativeQuery(
					"BEGIN comp_sec(:circd,:sectionCode,:complaintType, :device,:year1,:month1,:year2,:month2); END;")
					.setParameter("circd", circleCode)
					.setParameter("sectionCode", dmFilter.getSectionCode())
					.setParameter("complaintType", complaintType).setParameter("device", device)
					.setParameter("year1", year1).setParameter("month1", month1)
					.setParameter("year2", year2).setParameter("month2", month2)
					.executeUpdate();


			session.flush();
			session.getTransaction().commit();
			
			String hql ="SELECT c.cirCode as CircleCode,c.secCode as SectionCode,cir.name as CircleName,sec.name as SectionName,"
	        		+ "c.TOT1,c.CPL1,(c.LIVE1+c.TMP1),"
	        		+ "c.TOT2,c.CPL2,(c.LIVE2+c.TMP2) "
	        		+ ",d.NAME "
	        		+ "FROM TMP_CT_DEV_MONABST_SEC c,Circle cir,Section sec,Division d "
	        		+ "WHERE cir.id =c.cirCode and sec.id =c.secCode and d.id = sec.DIVISION_ID";

			List<Object[]> results = session.createNativeQuery(hql).getResultList();
			sectionList = new ArrayList<>();
			
			for (Object[] row : results) {
				ComparisonSectionValueBean report = new ComparisonSectionValueBean();
				
				report.setCirCode((String) row[0]);
				report.setSecCode((String) row[1]);
				report.setCircleName((String) row[2]);
				report.setSectionName((String) row[3]);

				report.setTot1((BigDecimal) row[4]);
				report.setCpl1((BigDecimal) row[5]);
				report.setPend1((BigDecimal) row[6]);
				
				report.setTot2((BigDecimal) row[7]);
				report.setCpl2((BigDecimal) row[8]);
				report.setPend2((BigDecimal) row[9]);
				
				report.setDivisionName((String) row[10]);
						 

				sectionList.add(report);
			}
			
			selectedCircleName = circleName;
			cameFromInsideReport=true;
			cameFromInsideSection=true;
			
			FacesContext.getCurrentInstance().getExternalContext()
	        .getFlash().put("comingFromCircle", true);

			FacesContext.getCurrentInstance().getExternalContext().redirect("monthWiseComparisonSectionsReport.xhtml");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in database operation");
		}
	}
	
	@Transactional
	public void fetchReportForSectionUsers() {
		
		if (!validateMonthAndYearInputs()) {
            return; 
		}

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
		try {
			session = sessionFactory.openSession();
			session.beginTransaction();
			
			String complaintType = dmFilter.getComplaintType();
			String device = dmFilter.getDevice();
			String year1 = dmFilter.getYear1() != null ? dmFilter.getYear1() : "2025";
			String month1 = dmFilter.getMonth1() != null ? dmFilter.getMonth1() : "01"; 
			String year2 = dmFilter.getYear2() != null ? dmFilter.getYear2() : "2025";
			String month2 = dmFilter.getMonth2() != null ? dmFilter.getMonth2() : "01";


			session.createNativeQuery(
					"BEGIN comp_sec(:circd,:sectionCode,:complaintType, :device,:year1,:month1,:year2,:month2); END;")
					.setParameter("circd", circleCode)
					.setParameter("sectionCode", dmFilter.getSectionCode())
					.setParameter("complaintType", complaintType).setParameter("device", device)
					.setParameter("year1", year1).setParameter("month1", month1)
					.setParameter("year2", year2).setParameter("month2", month2)
					.executeUpdate();


			session.flush();
			session.getTransaction().commit();
			
			

			
			String hql ="SELECT c.cirCode as CircleCode,c.secCode as SectionCode,s.name as SectionName,"
	        		+ "c.TOT1,c.CPL1,(c.LIVE1+c.TMP1),"
	        		+ "c.TOT2,c.CPL2,(c.LIVE2+c.TMP2), "
	        		+"s.division_id AS DIVISION_ID, " 
		            +"d.name AS DIVISION_NAME, " 
		            +"s.sub_division_id AS SUB_DIVISION_ID " 
	        		+ "FROM TMP_CT_DEV_MONABST_SEC c "
		            +"JOIN SECTION s ON s.id = c.seccode " 
		            +"JOIN DIVISION d ON d.id = s.division_id";
			
			List<Object[]> results = session.createNativeQuery(hql).getResultList();
			List<ComparisonSectionValueBean> circleSection = new ArrayList<>();
			
			for (Object[] row : results) {
				ComparisonSectionValueBean report = new ComparisonSectionValueBean();
				
				report.setCirCode((String) row[0]);
				report.setSecCode((String) row[1]);
				report.setSectionName((String) row[2]);

				report.setTot1((BigDecimal) row[3]);
				report.setCpl1((BigDecimal) row[4]);
				report.setPend1((BigDecimal) row[5]);
				
				report.setTot2((BigDecimal) row[6]);
				report.setCpl2((BigDecimal) row[7]);
				report.setPend2((BigDecimal) row[8]);
				
				report.setDivisionId((String) row[9].toString());
				report.setDivisionName((String) row[10]);
				report.setSubDivisionId((String) row[11].toString());

				circleSection.add(report);
			}
			

			//SECTION
			if(adminUserValueBean.getRoleId()==1) {
				sectionList = circleSection.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
						.filter(section -> section.getSecCode().equalsIgnoreCase(sectionCode))
						.collect(Collectors.toList());
			}
			//DIVISION
			else if(adminUserValueBean.getRoleId()==2) {
				sectionList = circleSection.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
						.filter(sd -> sd.getSubDivisionId().equalsIgnoreCase(subDivisionId))
						.collect(Collectors.toList());
			}
			//SUB DIVISION
			else if(adminUserValueBean.getRoleId()==3) {
				sectionList = circleSection.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
						.filter(d -> d.getDivisionId().equalsIgnoreCase(divisionId))
						.collect(Collectors.toList());
			}
			//ALL SECTION
			else {
				sectionList = circleSection.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
						.collect(Collectors.toList());
			}
			
			selectedCircleName = circleName;
			cameFromInsideSection=true;

			FacesContext.getCurrentInstance().getExternalContext().redirect("monthWiseComparisonSectionsReport.xhtml");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in database operation");
		}
	}
	
	
	
	public void redirectToComparsionCircleReport() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().redirect("monthWiseComparisonCircleReport.xhtml");
	}
	public void redirectToSectionReport() throws IOException {

		FacesContext.getCurrentInstance().getExternalContext().redirect("monthWiseComparisonSectionsReport.xhtml");

		
	}
	@Transactional
	public void getComplaintListForSection(String secCode,String sectionName) throws IOException {
		try (Session session = sessionFactory.openSession()) {
			
			List<String> complaintTypes = new ArrayList<>();
	        List<String> devices = new ArrayList<>();
			
			
	        Date fromDate1 =null;
	        Date toDate1 =null;
	        Date fromDate2 =null;
	        Date toDate2=null;
	        
	        if (dmFilter.getYear1() != null && dmFilter.getMonth1() != null) {
	            Integer year1 = Integer.parseInt(dmFilter.getYear1());
	            Integer month1 = Integer.parseInt(dmFilter.getMonth1());

	            Calendar calendar = Calendar.getInstance();

	            calendar.set(year1, month1 - 1, 1, 0, 0, 0);
	            fromDate1 = calendar.getTime();

	            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
	            calendar.set(Calendar.HOUR_OF_DAY, 23);
	            calendar.set(Calendar.MINUTE, 59);
	            calendar.set(Calendar.SECOND, 59);
	            toDate1 =calendar.getTime();
	        }

	        if (dmFilter.getYear2() != null && dmFilter.getMonth2() != null) {
	            Integer year2 = Integer.parseInt(dmFilter.getYear2());
	            Integer month2 = Integer.parseInt(dmFilter.getMonth2());

	            Calendar calendar = Calendar.getInstance();

	            calendar.set(year2, month2 - 1, 1, 0, 0, 0);
	            fromDate2 = calendar.getTime();

	            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
	            calendar.set(Calendar.HOUR_OF_DAY, 23);
	            calendar.set(Calendar.MINUTE, 59);
	            calendar.set(Calendar.SECOND, 59);
	            toDate2 =calendar.getTime();
	        }
	       
	        
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
						
			
			Timestamp fromDateOne = new Timestamp(fromDate1.getTime());
			Timestamp toDateOne = new Timestamp(toDate1.getTime());
			
			Timestamp fromDateTwo = new Timestamp(fromDate2.getTime());
			Timestamp toDateTwo = new Timestamp(toDate2.getTime());
	
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
                "WHERE a.SECTION_ID = :sectionCode AND (" +
        		 "(a.CREATED_ON BETWEEN :fromDateOne AND :toDateOne) OR (a.CREATED_ON BETWEEN :fromDateTwo AND :toDateTwo)) ");
        if (!complaintTypes.isEmpty()) {
            hql.append(" AND a.COMPLAINT_TYPE IN (:complaintTypes)");
        }

        if (!devices.isEmpty()) {
            hql.append(" AND a.DEVICE IN (:devices)");
        }			
        Query query = session.createNativeQuery(hql.toString());
        query.setParameter("sectionCode", sectionCode);
        query.setParameter("fromDateOne", fromDateOne);
        query.setParameter("toDateOne", toDateOne);
        query.setParameter("fromDateTwo", fromDateTwo);
        query.setParameter("toDateTwo", toDateTwo);
        
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

        FacesContext.getCurrentInstance().getExternalContext().redirect("monthWiseComparisonComplaintList.xhtml");
		
		}catch(NumberFormatException e){
			System.out.println("ERROR..........."+e);
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
//                "to_char(ct.TRF_ON, 'dd-mm-yyyy-hh24:mi') AS transferedOn, "+
//                "ct.TRF_USER AS transferedUser, "+
//                "ct.REMARKS AS transferedRemarks, "+
//                "to_char(qc.QC_ON, 'dd-mm-yyyy-hh24:mi') AS qcDate, "+
//                "qc.QC_STATUS AS qcStatus, "+
//                "qc.REMARKS AS qcRemarks "+
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
//        	dto.setComplaintTransferedOn((String) row[23]);
//        	dto.setComplaintTransferedBy((String) row[24]);
//            dto.setComplaintTransferedRemarks((String) row[25]);
//            dto.setQcDate((String) row[26]);
//            dto.setQcStatus((String) row[27]);
//            dto.setQcRemarks((String) row[28]);        	
        	
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
	
	//CIRCLE REPORT TO EXCEL DOWNLOAD
	public void exportComparisonCircleToExcel(List<ComparisonReportValueBean> resultList) throws IOException {
	    HSSFWorkbook workbook = new HSSFWorkbook();
	    Sheet sheet = workbook.createSheet("Month_And_YearWise_Comparison_Abstract_Report_CircleWise");
	    final int COLUMN_COUNT = 8;
	    
	    sheet.setColumnWidth(0, 3000);  //S.NO 
	    sheet.setColumnWidth(1, 6000);  //CIRCLE 
	    for (int i = 2; i < COLUMN_COUNT; i++) {
	        sheet.setColumnWidth(i, 5000);
	    }
	    
	    Row headingRow = sheet.createRow(0);
	    Cell headingCell = headingRow.createCell(0);
	    headingCell.setCellValue("MONTH AND YEAR WISE COMPARISON ABSTRACT REPORT - CIRCLE WISE");
	    
	    Cell headingCell2 = headingRow.createCell(1);
	    headingCell2.setCellValue("");
	    
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
	    
	    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7)); 
	    
	    String complaintType=dmFilter.getComplaintType();
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

	    
	    Row subHeadingRow = sheet.createRow(1);
	    Cell subHeadingCell = subHeadingRow.createCell(0);
	    subHeadingCell.setCellValue("MONTH AND YEAR 1 : " +dmFilter.getMonth1()+" / "+dmFilter.getYear1()+" | "+"MONTH AND YEAR 2 : "+dmFilter.getMonth2()+" / "+dmFilter.getYear2() +"  Complaint Type :"+ complaintType+  "   Device :"+device);
	    subHeadingCell.setCellStyle(headingStyle);

	    
	    CellStyle dateStyle = workbook.createCellStyle();
	    HSSFFont dateFont = workbook.createFont();
	    dateFont.setFontHeightInPoints((short) 8);
	    dateStyle.setFont(dateFont);
	    dateStyle.setAlignment(HorizontalAlignment.CENTER);
	    dateStyle.setVerticalAlignment(VerticalAlignment.CENTER);
	    subHeadingCell.setCellStyle(dateStyle);
	    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, COLUMN_COUNT-1));


	    CellStyle headerStyle = createHeaderStyle(workbook);

	    Row headerRow1 = sheet.createRow(2);
	    Row headerRow2 = sheet.createRow(3);

	    String period1Header = getMonthName(dmFilter.getMonth1()) + " " + dmFilter.getYear1();
	    String period2Header = getMonthName(dmFilter.getMonth2()) + " " + dmFilter.getYear2();

	    String[] mainHeaders = { "S.NO","CIRCLE", period1Header, period2Header };
	    String[] subHeaders = { "Received", "Completed", "Pending" };

	    int colIndex = 0;
	    for (String mainHeader : mainHeaders) {
	        Cell mainCell = headerRow1.createCell(colIndex);
	        mainCell.setCellValue(mainHeader);
	        mainCell.setCellStyle(headerStyle);

	        if (mainHeader.equals(period1Header) || mainHeader.equals(period2Header)) {
	            sheet.addMergedRegion(new CellRangeAddress(2, 2, colIndex, colIndex + 2));

	            for (String subHeader : subHeaders) {
	                Cell subCell = headerRow2.createCell(colIndex);
	                subCell.setCellValue(subHeader);
	                subCell.setCellStyle(headerStyle);
	                colIndex++;
	            }
	        } else {
	            sheet.addMergedRegion(new CellRangeAddress(2, 3, colIndex, colIndex)); 
	            colIndex++;
	        }
	    }

	    // Create a consistent cell style with borders for all data cells
	    CellStyle dataCellStyle = workbook.createCellStyle();
	    dataCellStyle.setBorderBottom(BorderStyle.THIN);
	    dataCellStyle.setBorderTop(BorderStyle.THIN);
	    dataCellStyle.setBorderLeft(BorderStyle.THIN);
	    dataCellStyle.setBorderRight(BorderStyle.THIN);
	    dataCellStyle.setAlignment(HorizontalAlignment.LEFT);
	    
	    // Create a centered style with borders for S.NO column
	    CellStyle centerStyle = workbook.createCellStyle();
	    centerStyle.setBorderBottom(BorderStyle.THIN);
	    centerStyle.setBorderTop(BorderStyle.THIN);
	    centerStyle.setBorderLeft(BorderStyle.THIN);
	    centerStyle.setBorderRight(BorderStyle.THIN);
	    centerStyle.setAlignment(HorizontalAlignment.CENTER);
	    
	    int rowNum = 4;
	    BigDecimal[] totalSums = new BigDecimal[8]; 
	    Arrays.fill(totalSums, BigDecimal.ZERO);

	    int serialNumber = 1;
	    
	    for (ComparisonReportValueBean report : resultList) {
	        Row row = sheet.createRow(rowNum++);

	        // Apply border style to S.NO cell
	        Cell serialCell = row.createCell(0);
	        serialCell.setCellValue(serialNumber++);
	        serialCell.setCellStyle(centerStyle);
	        
	        // Apply border style to CIRCLE cell
	        Cell circleCell = row.createCell(1);
	        circleCell.setCellValue(report.getCircleName());
	        circleCell.setCellStyle(dataCellStyle);

	        setCellValue(row, 2, report.getTot1(),dataCellStyle);
	        setCellValue(row, 3, report.getCpl1(),dataCellStyle);
	        setCellValue(row, 4, report.getPend1(),dataCellStyle);
	        setCellValue(row, 5, report.getTot2() != null ? report.getTot2() : BigDecimal.ZERO,dataCellStyle);
	        setCellValue(row, 6, report.getCpl2() != null ? report.getCpl2() : BigDecimal.ZERO,dataCellStyle);
	        setCellValue(row, 7, report.getPend2() != null ? report.getPend2() : BigDecimal.ZERO,dataCellStyle);

	        totalSums[2] = totalSums[2].add(report.getTot1() != null ? report.getTot1() : BigDecimal.ZERO);
	        totalSums[3] = totalSums[3].add(report.getCpl1() != null ? report.getCpl1() : BigDecimal.ZERO);
	        totalSums[4] = totalSums[4].add(report.getPend1() != null ? report.getPend1() : BigDecimal.ZERO);
	        totalSums[5] = totalSums[5].add(report.getTot2() != null ? report.getTot2() : BigDecimal.ZERO);
	        totalSums[6] = totalSums[6].add(report.getCpl2() != null ? report.getCpl2() : BigDecimal.ZERO);
	        totalSums[7] = totalSums[7].add(report.getPend2() != null ? report.getPend2() : BigDecimal.ZERO);
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

	    Row totalRow = sheet.createRow(rowNum);
	    Cell totalSerialCell = totalRow.createCell(0);
	    totalSerialCell.setCellValue(""); // Empty cell for S.NO in total row
	    totalSerialCell.setCellStyle(totalRowStyle);
	    
	    // TOTAL label now goes in column 1
	    Cell totalLabelCell = totalRow.createCell(1);
	    totalLabelCell.setCellValue("TOTAL");
	    totalLabelCell.setCellStyle(totalRowStyle);

	    for (int i = 2; i < totalSums.length; i++) {
	        setCellValue(totalRow, i, totalSums[i],dataCellStyle);
	        totalRow.getCell(i).setCellStyle(totalRowStyle);
	    }

	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    try {
	        workbook.write(outputStream);
	    } finally {
	        workbook.close();
	    }

	    excelFile = DefaultStreamedContent.builder()
	            .name("Month_And_YearWise_Comparison_Abstract_Report_CircleWise.xls")
	            .contentType("application/vnd.ms-excel")
	            .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
	            .build();

	    outputStream.close();
	}

	
	
	//SECTION REPORT TO EXCEL DOWNLOAD
	public void exportComparisonSectionsToExcel(List<ComparisonSectionValueBean> sectionList) throws IOException {
	    HSSFWorkbook workbook = new HSSFWorkbook();
	    Sheet sheet = workbook.createSheet("Month_And_YearWise_Comparison_Abstract_Report_SectionWise");

	    sheet.setColumnWidth(0, 2000); // S.NO
	    sheet.setColumnWidth(1, 6000); // SECTION - Fixed: was setting column 0 width twice
	    sheet.setColumnWidth(2, 6000); // DIVISION - Fixed: this was column 1 before
	    for (int i = 3; i < 9; i++) {
	        sheet.setColumnWidth(i, 4000);
	    }
	    
	    Row headingRow = sheet.createRow(0);
	    Cell headingCell = headingRow.createCell(0);
	    headingCell.setCellValue("MONTH AND YEAR WISE COMPARSION ABSRACT REPORT - SECTION WISE");
	    
	    Cell headingCell2 = headingRow.createCell(1);
	    headingCell2.setCellValue("");

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
	    
	    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8)); 
	    
	    
	    String complaintType=dmFilter.getComplaintType();
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

	    
	    Row subHeadingRow = sheet.createRow(1);
	    Cell subHeadingCell = subHeadingRow.createCell(0);
	    subHeadingCell.setCellValue("MONTH AND YEAR 1 :"+dmFilter.getMonth1()+" / "+dmFilter.getYear1() +" MONTH AND YEAR 2 :"+dmFilter.getMonth2()+" / "+dmFilter.getYear2()+"   CIRCLE :"+selectedCircleName +"   Complaint Type :"+complaintType+"  Device :"+device);
	    subHeadingCell.setCellStyle(headingStyle);
	    
	    CellStyle dateStyle = workbook.createCellStyle();
	    HSSFFont dateFont = workbook.createFont();
	    dateFont.setFontHeightInPoints((short) 8);
	    dateStyle.setFont(dateFont);
	    dateStyle.setAlignment(HorizontalAlignment.CENTER);
	    dateStyle.setVerticalAlignment(VerticalAlignment.CENTER);
	    subHeadingCell.setCellStyle(dateStyle);
	    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 8));

	    CellStyle headerStyle = createHeaderStyle(workbook);

	    Row headerRow1 = sheet.createRow(2);
	    Row headerRow2 = sheet.createRow(3);

	    String period1Header = getMonthName(dmFilter.getMonth1()) + " " + dmFilter.getYear1();
	    String period2Header = getMonthName(dmFilter.getMonth2()) + " " + dmFilter.getYear2();

	    String[] mainHeaders = {"S.NO","SECTION", "DIVISION", period1Header, period2Header};
	    String[] subHeaders = {"Received", "Completed", "Pending"};

	    int colIndex = 0;

	    for (String mainHeader : mainHeaders) {
	        Cell mainCell = headerRow1.createCell(colIndex);
	        mainCell.setCellValue(mainHeader);
	        mainCell.setCellStyle(headerStyle);

	        if (colIndex >= 3) { 
	            sheet.addMergedRegion(new CellRangeAddress(2, 2, colIndex, colIndex + 2));

	            for (String subHeader : subHeaders) {
	                Cell subCell = headerRow2.createCell(colIndex);
	                subCell.setCellValue(subHeader);
	                subCell.setCellStyle(headerStyle);
	                colIndex++;
	            }
	        } else { 
	            sheet.addMergedRegion(new CellRangeAddress(2, 3, colIndex, colIndex));
	            colIndex++;
	        }
	    }

	    // Create data cell style with borders for all cells
	    CellStyle dataCellStyle = workbook.createCellStyle();
	    dataCellStyle.setBorderBottom(BorderStyle.THIN);
	    dataCellStyle.setBorderTop(BorderStyle.THIN);
	    dataCellStyle.setBorderLeft(BorderStyle.THIN);
	    dataCellStyle.setBorderRight(BorderStyle.THIN);
	    dataCellStyle.setAlignment(HorizontalAlignment.LEFT);
	    
	    // Create centered style with borders for S.NO column
	    CellStyle centerStyle = workbook.createCellStyle();
	    centerStyle.setBorderBottom(BorderStyle.THIN);
	    centerStyle.setBorderTop(BorderStyle.THIN);
	    centerStyle.setBorderLeft(BorderStyle.THIN);
	    centerStyle.setBorderRight(BorderStyle.THIN);
	    centerStyle.setAlignment(HorizontalAlignment.CENTER);
	    
	    int rowNum = 4;
	    BigDecimal[] totalSums = new BigDecimal[6]; 
	    Arrays.fill(totalSums, BigDecimal.ZERO);

	    int serialNumber = 1;
	    for (ComparisonSectionValueBean report : sectionList) {
	        Row row = sheet.createRow(rowNum++);

	        // Apply border style to S.NO cell with center alignment
	        Cell serialCell = row.createCell(0);
	        serialCell.setCellValue(serialNumber++);
	        serialCell.setCellStyle(centerStyle);

	        // Apply border styles to SECTION and DIVISION cells
	        Cell sectionCell = row.createCell(1);
	        sectionCell.setCellValue(report.getSectionName());
	        sectionCell.setCellStyle(dataCellStyle);
	        
	        Cell divisionCell = row.createCell(2);
	        divisionCell.setCellValue(report.getDivisionName());
	        divisionCell.setCellStyle(dataCellStyle);

	        setCellValue(row, 3, report.getTot1(), dataCellStyle);
	        setCellValue(row, 4, report.getCpl1(), dataCellStyle);
	        setCellValue(row, 5, report.getPend1(), dataCellStyle);
	        setCellValue(row, 6, report.getTot2(), dataCellStyle);
	        setCellValue(row, 7, report.getCpl2(), dataCellStyle);
	        setCellValue(row, 8, report.getPend2(), dataCellStyle);

	        totalSums[0] = totalSums[0].add(report.getTot1() != null ? report.getTot1() : BigDecimal.ZERO);
	        totalSums[1] = totalSums[1].add(report.getCpl1() != null ? report.getCpl1() : BigDecimal.ZERO);
	        totalSums[2] = totalSums[2].add(report.getPend1() != null ? report.getPend1() : BigDecimal.ZERO);
	        totalSums[3] = totalSums[3].add(report.getTot2() != null ? report.getTot2() : BigDecimal.ZERO);
	        totalSums[4] = totalSums[4].add(report.getCpl2() != null ? report.getCpl2() : BigDecimal.ZERO);
	        totalSums[5] = totalSums[5].add(report.getPend2() != null ? report.getPend2() : BigDecimal.ZERO);
	    }
	   
	    addTotalRowWithSNO(sheet, rowNum, totalSums, workbook);
	    
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    workbook.write(outputStream);
	    workbook.close();
	   
	    excelFile = DefaultStreamedContent.builder()
	            .name("Month_And_YearWise_Comparison_Abstract_Report_SectionWise.xls")
	            .contentType("application/vnd.ms-excel")
	            .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
	            .build();
	}

	private void addTotalRowWithSNO(Sheet sheet, int rowNum, BigDecimal[] totalSums, HSSFWorkbook workbook) {
	    Row totalRow = sheet.createRow(rowNum);
	    
	    CellStyle dataCellStyle = workbook.createCellStyle();
	    dataCellStyle.setBorderBottom(BorderStyle.THIN);
	    dataCellStyle.setBorderTop(BorderStyle.THIN);
	    dataCellStyle.setBorderLeft(BorderStyle.THIN);
	    dataCellStyle.setBorderRight(BorderStyle.THIN);
	    dataCellStyle.setAlignment(HorizontalAlignment.LEFT);
	    
	    
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
	    
	    // S.NO cell in total row - leave empty
	    Cell totalSerialCell = totalRow.createCell(0);
	    totalSerialCell.setCellValue("");
	    totalSerialCell.setCellStyle(totalRowStyle);
	    
	    // Create TOTAL label in column 1 (SECTION column)
	    Cell totalLabelCell = totalRow.createCell(1);
	    totalLabelCell.setCellValue("TOTAL");
	    totalLabelCell.setCellStyle(totalRowStyle);
	    
	    // Create empty cell in column 2 (DIVISION column)
	    Cell emptyDivisionCell = totalRow.createCell(2);
	    emptyDivisionCell.setCellValue("");
	    emptyDivisionCell.setCellStyle(totalRowStyle);
	    
	    // Add values starting from column 3
	    for (int i = 0; i < totalSums.length; i++) {
	        setCellValue(totalRow, i + 3, totalSums[i],dataCellStyle);
	        totalRow.getCell(i + 3).setCellStyle(totalRowStyle);
	    }
	}

	
	private CellStyle createHeaderStyle(HSSFWorkbook workbook) {
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
	    return totalRowStyle;
	}
	
	private void setCellValue(Row row, int columnIndex, BigDecimal value,CellStyle style) {
	    Cell cell = row.createCell(columnIndex);
	    if (value != null) {
	        cell.setCellValue(value.doubleValue());
	        cell.setCellStyle(style);
	    } else {
	        cell.setCellValue("");
	    }
	}
	
	
	//CIRCLE REPORT TO PDF DOWNLOAD
	public void exportComparisonCircleToPDF(List<ComparisonReportValueBean> resultList) {
	    try {
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        Document document = new Document(PageSize.A3); 
	        PdfWriter.getInstance(document, baos);
	        document.open();

	        // Increase column count from 7 to 8 to include S.NO
	        PdfPTable table = new PdfPTable(8);
	        table.setWidthPercentage(100);
	        table.setSpacingBefore(10f);
	        table.setSpacingAfter(10f);
	        // Updated column widths with S.NO column
	        table.setWidths(new float[]{1f, 3f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f}); 

	        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
	        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
	        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
	        
	        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
	        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.DARK_GRAY);
	        
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
	        
	        Paragraph title = new Paragraph("MONTH AND YEAR WISE COMPARISON CIRCLE REPORT",titleFont);
	        title.setAlignment(Element.ALIGN_CENTER);
	        title.setSpacingAfter(5f);
	        
	        String period1Header = getMonthName(dmFilter.getMonth1()) + " " + dmFilter.getYear1();
	        String period2Header = getMonthName(dmFilter.getMonth2()) + " " + dmFilter.getYear2();
	        
	        Paragraph subTitle = new Paragraph("Complaint Type: "+complaintType+ " Device: "+device + " Month And Year 1 :"+period1Header +"  Month And Year 2 :"+period2Header, subTitleFont);
	        subTitle.setAlignment(Element.ALIGN_CENTER);
	        subTitle.setSpacingAfter(10f);
	        
	        document.add(title);
	        document.add(subTitle);

	        PdfPCell headerCell = new PdfPCell();
	        headerCell.setBackgroundColor(new BaseColor(54, 69, 79)); 
	        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	        headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	        headerCell.setPadding(5);
	        headerCell.setBorderWidth(1);

	        // Add S.NO header
	        addMergedHeaderCell(table, "S.NO", headerFont, 1, 2);
	        // Add CIRCLE header
	        addMergedHeaderCell(table, "CIRCLE", headerFont, 1, 2);
	        addMergedHeaderCell(table, period1Header, headerFont, 3, 1);
	        addMergedHeaderCell(table, period2Header, headerFont, 3, 1);

	        String[] subHeaders = {"Revd.", "Comp.", "Pend."};
	        for (int i = 0; i < 2; i++) { 
	            for (String subHeader : subHeaders) {
	                PdfPCell cell = new PdfPCell(new Phrase(subHeader, headerFont));
	                cell.setBackgroundColor(new BaseColor(192, 192, 192));
	                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	                cell.setBorderWidth(1);
	                table.addCell(cell);
	            }
	        }

	        BigDecimal totalTot1 = BigDecimal.ZERO;
	        BigDecimal totalCpl1 = BigDecimal.ZERO;
	        BigDecimal totalPend1 = BigDecimal.ZERO;
	        BigDecimal totalTot2 = BigDecimal.ZERO;
	        BigDecimal totalCpl2 = BigDecimal.ZERO;
	        BigDecimal totalPend2 = BigDecimal.ZERO;

	        int serialNumber = 1; // Initialize serial number counter

	        for (ComparisonReportValueBean report : resultList) {
	            // Add serial number cell
	            PdfPCell snCell = new PdfPCell(new Phrase(String.valueOf(serialNumber++), normalFont));
	            snCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	            snCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	            table.addCell(snCell);
	            
	            // Add circle name
	            addDataCell(table, report.getCircleName(), normalFont);

	            totalTot1 = totalTot1.add(report.getTot1() != null ? report.getTot1() : BigDecimal.ZERO);
	            totalCpl1 = totalCpl1.add(report.getCpl1() != null ? report.getCpl1() : BigDecimal.ZERO);
	            totalPend1 = totalPend1.add(report.getPend1() != null ? report.getPend1() : BigDecimal.ZERO);

	            totalTot2 = totalTot2.add(report.getTot2() != null ? report.getTot2() : BigDecimal.ZERO);
	            totalCpl2 = totalCpl2.add(report.getCpl2() != null ? report.getCpl2() : BigDecimal.ZERO);
	            totalPend2 = totalPend2.add(report.getPend2() != null ? report.getPend2() : BigDecimal.ZERO);

	            addNumericCell(table, report.getTot1());
	            addNumericCell(table, report.getCpl1());
	            addNumericCell(table, report.getPend1());
	            addNumericCell(table, report.getTot2());
	            addNumericCell(table, report.getCpl2());
	            addNumericCell(table, report.getPend2());
	        }

	        // Add empty S.NO cell for the total row
	        PdfPCell emptySnCell = new PdfPCell(new Phrase("", totalFont));
	        emptySnCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	        emptySnCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	        emptySnCell.setPadding(5);
	        emptySnCell.setBorderWidth(2);
	        emptySnCell.setBackgroundColor(new BaseColor(192, 192, 192));
	        table.addCell(emptySnCell);

	        // Add TOTAL cell
	        PdfPCell totalCell = new PdfPCell(new Phrase("TOTAL", totalFont));
	        totalCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	        totalCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	        totalCell.setPadding(5);
	        totalCell.setBorderWidth(2);
	        totalCell.setBackgroundColor(new BaseColor(192, 192, 192)); 
	        table.addCell(totalCell);

	        addTotalNumericCell(table, totalTot1);
	        addTotalNumericCell(table, totalCpl1);
	        addTotalNumericCell(table, totalPend1);
	        addTotalNumericCell(table, totalTot2);
	        addTotalNumericCell(table, totalCpl2);
	        addTotalNumericCell(table, totalPend2);

	        document.add(table);
	        document.close();

	        pdfFile = DefaultStreamedContent.builder()
	                .name("Month_And_YearWise_Comparison_Circle_Report.pdf")
	                .contentType("application/pdf")
	                .stream(() -> new ByteArrayInputStream(baos.toByteArray()))
	                .build();

	    } catch (DocumentException e) {
	        e.printStackTrace();
	    }
	}

	// Helper method to add a merged header cell
	private void addMergedHeaderCell(PdfPTable table, String text, Font font, int colspan, int rowspan) {
	    PdfPCell cell = new PdfPCell(new Phrase(text, font));
	    cell.setBackgroundColor(new BaseColor(54, 69, 79));
	    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	    cell.setPadding(5);
	    cell.setBorderWidth(1);
	    cell.setColspan(colspan);
	    cell.setRowspan(rowspan);
	    cell.setBackgroundColor(new BaseColor(192, 192, 192));
	    table.addCell(cell);
	}

	// Helper method to add a data cell
	private void addDataCell(PdfPTable table, String text, Font font) {
	    PdfPCell cell = new PdfPCell(new Phrase(text, font));
	    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
	    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	    cell.setPadding(5);
	    table.addCell(cell);
	}

	// Helper method to add a numeric cell
	private void addNumericCell(PdfPTable table, BigDecimal value) {
	    String text = value != null ? value.toString() : "0";
	    PdfPCell cell = new PdfPCell(new Phrase(text));
	    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
	    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	    cell.setPadding(5);
	    table.addCell(cell);
	}

	// Helper method to add a total numeric cell
	private void addTotalNumericCell(PdfPTable table, BigDecimal value) {
	    String text = value != null ? value.toString() : "0";
	    PdfPCell cell = new PdfPCell(new Phrase(text));
	    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
	    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	    cell.setPadding(5);
	    cell.setBorderWidth(2);
	    cell.setBackgroundColor(new BaseColor(192, 192, 192));
	    table.addCell(cell);
	}


	//SECTION REPORT TO PDF DOWNLOAD
	public void exportComparisonSectionsToPDF(List<ComparisonSectionValueBean> sectionList) {
	    try {
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        Document document = new Document(PageSize.A3);
	        PdfWriter.getInstance(document, baos);
	        document.open();
	        
	        PdfPTable table = new PdfPTable(9);
	        table.setWidthPercentage(100);
	        table.setSpacingBefore(10f);
	        table.setSpacingAfter(10f);
	        table.setWidths(new float[]{1.5f,3f,2.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f}); 
	        
	        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
	        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
	        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
	        
	        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
	        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.DARK_GRAY);

	        Paragraph title = new Paragraph("MONTH AND YEAR WISE COMPARISON SECTION REPORT",titleFont);
	        title.setAlignment(Element.ALIGN_CENTER);
	        title.setSpacingAfter(5f);
	        
	        String period1Header = getMonthName(dmFilter.getMonth1()) + " " + dmFilter.getYear1();
	        String period2Header = getMonthName(dmFilter.getMonth2()) + " " + dmFilter.getYear2();
	        
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

	        
	        Paragraph subTitle = new Paragraph("CIRCLE: " + selectedCircleName +"  Complaint Type :"+complaintType+  "  Device :"+device +"  Month And Year 1 :"+period1Header +"  Month And Year 2 :"+period2Header, subTitleFont);
	        subTitle.setAlignment(Element.ALIGN_CENTER);
	        subTitle.setSpacingAfter(10f);
	        
	        document.add(title);
	        document.add(subTitle);
	        
	        PdfPCell headerCell = new PdfPCell();
	        headerCell.setBackgroundColor(new BaseColor(54, 69, 79)); 
	        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	        headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	        headerCell.setPadding(5);
	        headerCell.setBorderWidth(1);
	        
	        addMergedHeaderCell(table, "S.NO", headerFont, 1, 2);
	        addMergedHeaderCell(table, "SECTION", headerFont, 1, 2);
	        addMergedHeaderCell(table, "DIVISION", headerFont, 1, 2);
	        addMergedHeaderCell(table, period1Header, headerFont, 3, 1);
	        addMergedHeaderCell(table, period2Header, headerFont, 3, 1);
	        
	        String[] subHeaders = {"Revd.", "Comp.", "Pend."};
	        for (int i = 0; i < 2; i++) {
	        	for (String subHeader : subHeaders) {
	                PdfPCell cell = new PdfPCell(new Phrase(subHeader, headerFont));
	                cell.setBackgroundColor(new BaseColor(192, 192, 192));
	                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	                cell.setBorderWidth(1);
	                table.addCell(cell);
	            }
	        }

	        BigDecimal totalTot1 = BigDecimal.ZERO;
	        BigDecimal totalCpl1 = BigDecimal.ZERO;
	        BigDecimal totalPend1 = BigDecimal.ZERO;
	        BigDecimal totalTot2 = BigDecimal.ZERO;
	        BigDecimal totalCpl2 = BigDecimal.ZERO;
	        BigDecimal totalPend2 = BigDecimal.ZERO;

	        int serialNumber =1;
	        for (ComparisonSectionValueBean report : sectionList) {
	        	
	            addDataCell(table, String.valueOf(serialNumber++), normalFont);
	            addDataCell(table, report.getSectionName(), normalFont);
	            addDataCell(table, report.getDivisionName(), normalFont);
	            
	            totalTot1 = totalTot1.add(report.getTot1() != null ? report.getTot1() : BigDecimal.ZERO);
	            totalCpl1 = totalCpl1.add(report.getCpl1() != null ? report.getCpl1() : BigDecimal.ZERO);
	            totalPend1 = totalPend1.add(report.getPend1() != null ? report.getPend1() : BigDecimal.ZERO);

	            totalTot2 = totalTot2.add(report.getTot2() != null ? report.getTot2() : BigDecimal.ZERO);
	            totalCpl2 = totalCpl2.add(report.getCpl2() != null ? report.getCpl2() : BigDecimal.ZERO);
	            totalPend2 = totalPend2.add(report.getPend2() != null ? report.getPend2() : BigDecimal.ZERO);

	            addNumericCell(table, report.getTot1());
	            addNumericCell(table, report.getCpl1());
	            addNumericCell(table, report.getPend1());
	            addNumericCell(table, report.getTot2());
	            addNumericCell(table, report.getCpl2());
	            addNumericCell(table, report.getPend2());
	        }

	        PdfPCell emptySnCell = new PdfPCell(new Phrase("", totalFont));
	        emptySnCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	        emptySnCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	        emptySnCell.setPadding(5);
	        emptySnCell.setBorderWidth(2);
	        emptySnCell.setBackgroundColor(new BaseColor(192, 192, 192));
	        table.addCell(emptySnCell);
	        
	        PdfPCell emptySnCellCircle = new PdfPCell(new Phrase("", totalFont));
	        emptySnCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	        emptySnCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	        emptySnCell.setPadding(5);
	        emptySnCell.setBorderWidth(2);
	        emptySnCell.setBackgroundColor(new BaseColor(192, 192, 192));
	        table.addCell(emptySnCellCircle);

	        // Add TOTAL cell
	        PdfPCell totalCell = new PdfPCell(new Phrase("TOTAL", totalFont));
	        totalCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	        totalCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	        totalCell.setPadding(5);
	        totalCell.setBorderWidth(2);
	        totalCell.setBackgroundColor(new BaseColor(192, 192, 192)); 
	        table.addCell(totalCell);

	        addTotalNumericCell(table, totalTot1);
	        addTotalNumericCell(table, totalCpl1);
	        addTotalNumericCell(table, totalPend1);
	        addTotalNumericCell(table, totalTot2);
	        addTotalNumericCell(table, totalCpl2);
	        addTotalNumericCell(table, totalPend2);

	        document.add(table);
	        document.close();

	        pdfFile = DefaultStreamedContent.builder()
	                .name("Month_And_YearWise_Comparison_Section_Report.pdf")
	                .contentType("application/pdf")
	                .stream(() -> new ByteArrayInputStream(baos.toByteArray()))
	                .build();
	    } catch (DocumentException e) {
	        e.printStackTrace();
	    }
	}


	//COMPLAINT LIST TO EXCEL DOWNLOAD
	  public void exportComplaintListToExcel(List<ViewComplaintReportValueBean> complaintList) throws IOException {
		  HSSFWorkbook workbook = new HSSFWorkbook();
		  Sheet sheet = workbook.createSheet("Month_And_YearWise_Comparsion_ComplaintList_Report");
		  
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
		                .name("Month_And_YearWise_Comparison_ComplaintList_Report.xls")
		                .contentType("application/vnd.ms-excel")
		                .stream(() -> inputStream)
		                .build();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
	  }
	  
	  
	  //COMPLAINT LIST TO PDF DOWNLOAD
	  public void exportComplaintListToPdf(List<ViewComplaintReportValueBean> complaintList) {
		  Document document = new Document(PageSize.A4.rotate());
		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		    try {
		        PdfWriter.getInstance(document, outputStream);
		        document.open();
		        
		        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD, BaseColor.BLACK);
		        Paragraph title = new Paragraph("MONTH AND YEAR WISE COMPARISON COMPLAINT LIST", titleFont);
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
		            .name("Month_And_YearWise_Comparison_ComplaintList_Report.pdf")
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
	
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public DataModel getDmFilter() {
		return dmFilter;
	}

	public void setDmFilter(DataModel dmFilter) {
		this.dmFilter = dmFilter;
	}
	public List<ComparisonReportValueBean> getResultList() {
		return resultList;
	}


	public void setResultList(List<ComparisonReportValueBean> resultList) {
		this.resultList = resultList;
	}

	
	public List<ComparisonSectionValueBean> getSectionList() {
		return sectionList;
	}


	public void setSectionList(List<ComparisonSectionValueBean> sectionList) {
		this.sectionList = sectionList;
	}
	
	public String getMonth1Name() {
	    return getMonthName(dmFilter.getMonth1());
	}

	private String getMonthName(String monthNumber) {
	    switch (monthNumber) {
	        case "01": return "JANUARY";
	        case "02": return "FEBRUARY";
	        case "03": return "MARCH";
	        case "04": return "APRIL";
	        case "05": return "MAY";
	        case "06": return "JUNE";
	        case "07": return "JULY";
	        case "08": return "AUGUST";
	        case "09": return "SEPTEMBER";
	        case "10": return "OCTOBER";
	        case "11": return "NOVEMBER";
	        case "12": return "DECEMBER";
	        default: return "-";
	    }
	}


	public String getMonth2Name() {
	    return getMonthName(dmFilter.getMonth2());
	}

	public StreamedContent getPdfFile() {
		return pdfFile;
	}


	public void setPdfFile(StreamedContent pdfFile) {
		this.pdfFile = pdfFile;
	}


	public StreamedContent getExcelFile() {
		return excelFile;
	}


	public void setExcelFile(StreamedContent excelFile) {
		this.excelFile = excelFile;
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
	public List<String> getYearList() {
		return yearList;
	}

	public void setYearList(List<String> yearList) {
		this.yearList = yearList;
	}


	public int getCurrentYear() {
		return currentYear;
	}

	public void setCurrentYear(int currentYear) {
		this.currentYear = currentYear;
	}

	public int getCurrentMonth() {
		return currentMonth;
	}

	public void setCurrentMonth(int currentMonth) {
		this.currentMonth = currentMonth;
	}

	public List<MonthValueBean> getAvailableMonth1() {
		return availableMonth1;
	}

	public void setAvailableMonth1(List<MonthValueBean> availableMonth1) {
		this.availableMonth1 = availableMonth1;
	}

	public List<MonthValueBean> getAvailableMonth2() {
		return availableMonth2;
	}

	public void setAvailableMonth2(List<MonthValueBean> availableMonth2) {
		this.availableMonth2 = availableMonth2;
	}

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

	public boolean isCameFromInsideReport() {
		return cameFromInsideReport;
	}

	public void setCameFromInsideReport(boolean cameFromInsideReport) {
		this.cameFromInsideReport = cameFromInsideReport;
	}

	public boolean isCameFromInsideSection() {
		return cameFromInsideSection;
	}

	public void setCameFromInsideSection(boolean cameFromInsideSection) {
		this.cameFromInsideSection = cameFromInsideSection;
	}
	
	
}
