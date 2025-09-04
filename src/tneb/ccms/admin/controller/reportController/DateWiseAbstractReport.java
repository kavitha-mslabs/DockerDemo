package tneb.ccms.admin.controller.reportController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.glassfish.jersey.process.internal.RequestContext;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.log.SysoCounter;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import tneb.ccms.admin.model.CategoryBean;
import tneb.ccms.admin.model.CompDeviceBean;
import tneb.ccms.admin.model.ViewComplaintReportBean;
import tneb.ccms.admin.util.CCMSConstants;
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
public class DateWiseAbstractReport implements Serializable {

	private static final long serialVersionUID = 1L;
	private SessionFactory sessionFactory;
	private boolean initialized = false;
	private boolean cameFromInsideReport = false;
	private boolean cameFromInsideSection = false;
	private boolean cameFromMonthlyAbstractReport = false;
	DataModel dmFilter;
	List<TmpYearWiseCircle> circleList = new ArrayList<>();
	List<TmpYearWiseSections> sectionList = new ArrayList<>();
	StreamedContent excelFile;
	StreamedContent pdfFile;
	List<ViewComplaintReportValueBean> complaintList = new ArrayList<>();
	String selectedCircleName = null;
	String selectedSectionName = null;
	String selectedCircleId = null;

	private List<Integer> daysToDisplay;
	private int maxDaysToShow;

	private String selectedMonth;

	private String selectedComplaintType;
	private String selectedDevice;
	private String circleIdFromMonthlyAbstract;
	private String circleNameFromMonthlyAbstract;

	String redirectFrom;
	ViewComplaintReportValueBean selectedComplaintId;
	private Date currentDate = new Date();
	private String currentYear = String.valueOf(Year.now().getValue());
	private List<String> columns ;
    BigDecimal totalReceived = BigDecimal.ZERO;
    BigDecimal totalCompleted = BigDecimal.ZERO;
    BigDecimal totalPending = BigDecimal.ZERO;
    
    BigDecimal totalReceivedSection = BigDecimal.ZERO;
    BigDecimal totalCompletedSection = BigDecimal.ZERO;
    BigDecimal totalPendingSection = BigDecimal.ZERO;
    private int daysInMonth;
	List<CompDeviceValueBean> devices;
	List<CategoriesValueBean> categories;
	private boolean cameViaCircleReport=false;

	@PostConstruct
	public void init() {
		System.out.println("Initializing DATE wise Abstract Report...");
		sessionFactory = HibernateUtil.getSessionFactory();
		dmFilter = new DataModel();
		columns = new ArrayList<String>();

		circleList = new ArrayList<>();
		loadAllDevicesAndCategories();
	}
	
	public void clearFiltersAndPage() {
		dmFilter = new DataModel();
		circleList = new ArrayList<>();
		System.out.println("THE CLEAR BUTTON WORKS");
	}
	
	// REFERSH CIRCLE REPORT
	public void resetIfNeeded() {
	    if (!FacesContext.getCurrentInstance().isPostback() && !cameFromInsideReport) {
	        circleList = null;
	        dmFilter.setComplaintType(null);
	        dmFilter.setDevice(null);
	        dmFilter.setFromDate(null);
	        dmFilter.setToDate(null);
	    }
	    cameFromInsideReport = false; 
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
	
	public List<Integer> getDaysToDisplay() {
        if (daysToDisplay == null) {
            daysToDisplay = new ArrayList<>();
            
            if (isCurrentMonthSelected()) {
                Calendar cal = Calendar.getInstance();
                maxDaysToShow = cal.get(Calendar.DAY_OF_MONTH);
            } else {
                maxDaysToShow = getNoOfDays().size();
            }
            
            for (int i = 1; i <= maxDaysToShow; i++) {
                daysToDisplay.add(i);
            }
        }
        return daysToDisplay;
    }
	
	
	private boolean isCurrentMonthSelected() {
        if (dmFilter.getFromDate() == null) return false;
        
        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTime(dmFilter.getFromDate());
        
        Calendar currentCal = Calendar.getInstance();
        
        return selectedCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) && 
               selectedCal.get(Calendar.MONTH) == currentCal.get(Calendar.MONTH);
    }
	
	
	 public BigDecimal getDayValue(TmpYearWiseCircle report, String prefix, int day) {
	        try {
	            Method method = report.getClass().getMethod("get" + prefix + day);
	            return (BigDecimal) method.invoke(report);
	        } catch (Exception e) {
	            return BigDecimal.ZERO;
	        }
	    }
	 
	 public BigDecimal getTotalForDay(String prefix) {
	        BigDecimal total = BigDecimal.ZERO;
	        for (TmpYearWiseCircle report : circleList) {
	            for (int day : getDaysToDisplay()) {
	                total = total.add(getDayValue(report, prefix, day));
	            }
	        }
	        return total;
	    }
	 
	 public String goToDateWiseCircleAbstract() {
		 
	         convertMonthYearToDate(selectedMonth);
	         
	         this.dmFilter.setComplaintType(selectedComplaintType);
	         this.dmFilter.setDevice(selectedDevice);
	         
	         searchCircleDateAbstract();
	         
	         this.cameFromInsideReport=true;
	         this.cameFromMonthlyAbstractReport=true;

	        try {
	            FacesContext.getCurrentInstance().getExternalContext()
	                .redirect("dateWiseCircleAbstract.xhtml");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	        return null;
	    }
	 
	 public void convertMonthYearToDate(String monthYearString) {

		    try {
		        SimpleDateFormat formatter = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
		        Date parsedDate = formatter.parse(monthYearString);
		        
		        Calendar calendar = Calendar.getInstance();
	            calendar.setTime(parsedDate);
	            
	            calendar.set(Calendar.DAY_OF_MONTH, 1);
	            this.dmFilter.setFromDate(calendar.getTime());
	            
	            
		    } catch (ParseException e) {
		        e.printStackTrace();
		    }
		}
	 
	 public void goToDateWiseSectionAbstract() throws IOException {
		 this.cameFromMonthlyAbstractReport = true;
	        
         convertMonthYearToDate(selectedMonth);
         
         this.dmFilter.setComplaintType(selectedComplaintType);
         this.dmFilter.setDevice(selectedDevice);
         
         HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
	                .getExternalContext().getSession(false);
			
			AdminUserValueBean adminUserValueBean = (AdminUserValueBean) 
					httpsession.getAttribute("sessionAdminValueBean");
			
			if(adminUserValueBean!=null && adminUserValueBean.getRoleId()>=1 && adminUserValueBean.getRoleId()<=3) {
				fetchReportForSectionUsers();
			}
			
			else {
		         fetchReportByCircle(circleIdFromMonthlyAbstract,circleNameFromMonthlyAbstract);
			}

         
			this.cameFromInsideReport = true;
			this.cameFromInsideSection = true;

	 }

	 public String backToMonthlyAbstract() {
		    this.cameFromMonthlyAbstractReport = false;
		    this.cameViaCircleReport=false;
		    
		    try {
		        FacesContext.getCurrentInstance().getExternalContext()
		            .redirect("yearWiseMonthlyCircleAbstract.xhtml");
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		    return null;
		}
	 
	 public void redirectFromHourlySectionAbstract() {
		 this.cameFromInsideSection = true;
		 
		 try {
		        FacesContext.getCurrentInstance().getExternalContext()
		            .redirect("dateWiseSectionAbstract.xhtml");
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
	 }

	 public String backToYearWiseMonthlySectionAbstract() {
		    this.cameFromMonthlyAbstractReport = false;
		    
		    try {
		        FacesContext.getCurrentInstance().getExternalContext()
		            .redirect("yearWiseMonthlySectionAbstract.xhtml");
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		    return null;
		}
       
	 @SuppressWarnings("unchecked")
		@Transactional
		private void loadAllDevicesAndCategories() {
		    Session session = null;
		    try {
		        session = sessionFactory.openSession();
		        
		        String hql = "FROM CompDeviceBean d ORDER BY d.id ASC";
		        Query<CompDeviceBean> query = session.createQuery(hql, CompDeviceBean.class);
		        List<CompDeviceBean> devicesBean = query.getResultList();

		        List<CompDeviceValueBean> orderedDeviceList = devicesBean.stream()
		                .map(CompDeviceValueBean::convertCompDeviceBeanToCompDeviceValueBean)
		                .collect(Collectors.toList());

		        CompDeviceValueBean allOption = new CompDeviceValueBean("L", "ALL");
		        orderedDeviceList.add(0, allOption);

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
		                    	dmFilter.setRegionCode("A");
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

	@Transactional
	public void searchCircleDateAbstract() {
		System.out.println("THE SEARCH BUTTON WORKS");
		
		updateLoginWiseFilters();
		
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
                    "Error", "Cannot select future month/year");
                FacesContext.getCurrentInstance().addMessage(null, message);
                return;
            }
        }
		
		
		if (dmFilter.getFromDate() == null) {
			 Date now = new Date();
		     dmFilter.setFromDate(now);
		}
		getColumns();

		try {
			session = sessionFactory.openSession();
			session.beginTransaction();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String formattedFromDate = sdf.format(dmFilter.getFromDate());
	        
	        if(dmFilter.getComplaintType()==null) {
	        	dmFilter.setComplaintType("AL");
	        }
	        if(dmFilter.getDevice()==null) {
	        	dmFilter.setDevice("L");
	        }
			
			daysInMonth = getNoOfDays().size();
			dmFilter.setDays(String.valueOf(daysInMonth));

			session.createNativeQuery(
					"BEGIN cat_dev_date_abst(:regionCode,:circleCode,:complaintCode,:device,TO_DATE(:fromDate, 'YYYY-MM-DD'), :days); END;")
					.setParameter("regionCode", dmFilter.getRegionCode())
					.setParameter("circleCode", dmFilter.getCircleCode())
					.setParameter("complaintCode", dmFilter.getComplaintType())
		            .setParameter("device", dmFilter.getDevice())
					.setParameter("fromDate", formattedFromDate)
					.setParameter("days", dmFilter.getDays())
					.executeUpdate();

			session.flush();
			session.getTransaction().commit();

			fetchReports(session,daysInMonth);
			System.out.println("THE NO OF DAYS FOR SELECETED MONTH IS ==="+dmFilter.getDays());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in database operation");
		}
	}


	
	@Transactional
	private void fetchReports(Session session, Integer days) {
		 try {
			 StringBuilder hqlBuilder = new StringBuilder("SELECT c.REGCODE, c.CIRCODE, ");
			 for (int i = 1; i <= days; i++) {
		            hqlBuilder.append(String.format("c.TOT%d, c.CPL%d, (c.LIVE%d+c.TMP%d), ", i, i, i, i));
		        }
			    hqlBuilder.append("cir.name as CircleName, reg.name as RegionName ");
		        hqlBuilder.append("FROM TMP_CT_DEV_MONABST_CIR c, Circle cir, Region reg ");
		        hqlBuilder.append("WHERE cir.id = c.cirCode and reg.id = c.regCode");
		        
		        List<Object[]> results = session.createNativeQuery(hqlBuilder.toString()).getResultList();

		        List<TmpYearWiseCircle> result = new ArrayList<TmpYearWiseCircle>();
		        circleList = new ArrayList<>();
		        for (Object[] row : results) {
		            TmpYearWiseCircle report = new TmpYearWiseCircle();
		            report.setRegCode((String) row[0]);
		            report.setCirCode((String) row[1]);
		            
		            BigDecimal totalR = BigDecimal.ZERO;
		            BigDecimal totalC = BigDecimal.ZERO;
		            BigDecimal totalP = BigDecimal.ZERO;
		            
		            try {
		                for (int i = 1; i <= days; i++) {
		                    int baseIndex = (i - 1) * 3 + 2;
		                    
		                    BigDecimal tot = row[baseIndex] != null ? (BigDecimal) row[baseIndex] : BigDecimal.ZERO;
		                    BigDecimal cpl = row[baseIndex + 1] != null ? (BigDecimal) row[baseIndex + 1] : BigDecimal.ZERO;
		                    BigDecimal pend = row[baseIndex + 2] != null ? (BigDecimal) row[baseIndex + 2] : BigDecimal.ZERO;

		                    
		                    Method setTotMethod = TmpYearWiseCircle.class.getMethod("setTot" + i, BigDecimal.class);
		                    setTotMethod.invoke(report, row[baseIndex]);
		                    
		                    Method setCplMethod = TmpYearWiseCircle.class.getMethod("setCpl" + i, BigDecimal.class);
		                    setCplMethod.invoke(report, row[baseIndex + 1]);
		                    
		                    Method setPendMethod = TmpYearWiseCircle.class.getMethod("setPend" + i, BigDecimal.class);
		                    setPendMethod.invoke(report, row[baseIndex + 2]);
		                    
		                    totalR = totalR.add(tot);
		                    totalC = totalC.add(cpl);
		                    totalP = totalP.add(pend);
		                }
		                
		                // Set circle and region names (position depends on number of days)
		                report.setCircleName((String) row[days * 3 + 2]);
		                report.setRegionName((String) row[days * 3 + 3]);
		                
		                report.setTotalReceived(totalR);
		                report.setTotalCompleted(totalC);
		                report.setTotalPending(totalP);
		                
		                result.add(report);
		            }
		           
		            catch (Exception e) {
		                System.out.println("Error setting values using reflection: " + e.getMessage());
		                continue;
		            }
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
		            	circleList =result.stream().filter(c->c.getRegCode().equals(dmFilter.getRegionCode())).collect(Collectors.toList());
	            	}else {
		            	circleList =result.stream().filter(c->c.getRegCode().equals(dmFilter.getRegionCode()))
		            			.filter(r ->r.getCirCode().equals(dmFilter.getCircleCode()))
		            			.collect(Collectors.toList());
	            	}
	            }
		        calculateTotals();
		        System.out.println("COUNT: " + circleList.size());
		        
		        this.cameViaCircleReport=true;
		            
		 }catch (Exception e) {
		        e.printStackTrace();
		        System.out.println("Error in fetching report: " + e.getMessage());
		    }
	}
		 
	public BigDecimal getTotalReceived() {
        return totalReceived;
    }
    
    public BigDecimal getTotalCompleted() {
       return totalCompleted;
    }
    
    public BigDecimal getTotalPending() {
        return totalPending;
    }
	
	    
	public void redirectToCircleReport() throws IOException {

		FacesContext.getCurrentInstance().getExternalContext().redirect("dateWiseCircleAbstract.xhtml");
		
		
	}
	public void redirectToSectionReport() throws IOException {
     if("circle".equals(redirectFrom)) {
 		FacesContext.getCurrentInstance().getExternalContext().redirect("dateWiseCircleAbstract.xhtml");
     }else {
 		FacesContext.getCurrentInstance().getExternalContext().redirect("dateWiseSectionAbstract.xhtml");
     }		
	}
	
	public void redirectFromHourlyCircleAbstract() throws IOException {
		this.cameFromInsideReport=true;
 		FacesContext.getCurrentInstance().getExternalContext().redirect("dateWiseCircleAbstract.xhtml");

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
                "TO_CHAR(a.updated_on, 'DD-MM-YYYY-HH24:MI') AS Attended_Date, " +
                "c.DESCRIPTION AS Attended_Remarks, " +
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

	@Transactional
	public void fetchReportByCircle(String circleCode,String circleName) {

		this.selectedCircleId=circleCode;
		Session session = null;
		
		try {
			session = sessionFactory.openSession();
			session.beginTransaction();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String formattedFromDate = sdf.format(dmFilter.getFromDate());
			
	        if(dmFilter.getComplaintType()==null) {
	        	dmFilter.setComplaintType("AL");
	        }
	        if(dmFilter.getDevice()==null) {
	        	dmFilter.setDevice("L");
	        }
	        if(dmFilter.getDays()==null) {
	        	dmFilter.setDays("31");
	        }

			if(dmFilter.getSectionCode()==null) {
				dmFilter.setSectionCode("A");
			}


			System.out.println("Processed From Date: " + formattedFromDate);
			System.out.println("COMPLAINT TYPE ==="+ dmFilter.getComplaintType());
			System.out.println("DEVICE TYPE ==="+ dmFilter.getDevice());
			System.out.println("DAY  ==="+ dmFilter.getDays());
			System.out.println("THE CIRCLE CODE: " + circleCode);

			session.createNativeQuery(
					"BEGIN cat_dev_date_secabst(:circd,:sectionCode,:complaintType,:device, TO_DATE(:fromDate, 'YYYY-MM-DD'),:day); END;")
					.setParameter("circd", circleCode)
					.setParameter("sectionCode", dmFilter.getSectionCode())
					.setParameter("complaintType", dmFilter.getComplaintType())
					.setParameter("device", dmFilter.getDevice())
					.setParameter("fromDate", formattedFromDate)
					.setParameter("day", dmFilter.getDays())
					.executeUpdate();

			session.flush();
			session.getTransaction().commit();
			
			String hql ="SELECT c.CIRCODE,c.SECCODE,"
					+ "c.TOT1,c.CPL1,(c.LIVE1+c.TMP1),"
	        		+ "c.TOT2,c.CPL2,(c.LIVE2+c.TMP2),"
	        		+ "c.TOT3,c.CPL3,(c.LIVE3+c.TMP3),"
	        		+ "c.TOT4,c.CPL4,(c.LIVE4+c.TMP4),"
	        		+ "c.TOT5,c.CPL5,(c.LIVE5+c.TMP5),"
	        		+ "c.TOT6,c.CPL6,(c.LIVE6+c.TMP6),"
	        		+ "c.TOT7,c.CPL7,(c.LIVE7+c.TMP7),"
	        		+ "c.TOT8,c.CPL8,(c.LIVE8+c.TMP8),"
	        		+ "c.TOT9,c.CPL9,(c.LIVE9+c.TMP9),"
	        		+ "c.TOT10,c.CPL10,(c.LIVE10+c.TMP10),"
	        		+ "c.TOT11,c.CPL11,(c.LIVE11+c.TMP11),"
	        		+ "c.TOT12,c.CPL12,(c.LIVE12+c.TMP12),"
	        		+ "c.TOT13,c.CPL13,(c.LIVE13+c.TMP13),"
	        		+ "c.TOT14,c.CPL14,(c.LIVE14+c.TMP14),"
	        		+ "c.TOT15,c.CPL15,(c.LIVE15+c.TMP15),"
	        		+ "c.TOT16,c.CPL16,(c.LIVE16+c.TMP16),"
	        		+ "c.TOT17,c.CPL17,(c.LIVE17+c.TMP17),"
	        		+ "c.TOT18,c.CPL18,(c.LIVE18+c.TMP18),"
	        		+ "c.TOT19,c.CPL19,(c.LIVE19+c.TMP19),"
	        		+ "c.TOT20,c.CPL20,(c.LIVE20+c.TMP20),"
	        		+ "c.TOT21,c.CPL21,(c.LIVE21+c.TMP21),"
	        		+ "c.TOT22,c.CPL22,(c.LIVE22+c.TMP22),"
	        		+ "c.TOT23,c.CPL23,(c.LIVE23+c.TMP23),"
	        		+ "c.TOT24,c.CPL24,(c.LIVE24+c.TMP24),"
	        		+ "c.TOT25,c.CPL25,(c.LIVE25+c.TMP25),"
	        		+ "c.TOT26,c.CPL26,(c.LIVE26+c.TMP26),"
	        		+ "c.TOT27,c.CPL27,(c.LIVE27+c.TMP27),"
	        		+ "c.TOT28,c.CPL28,(c.LIVE28+c.TMP28),"
	        		+ "c.TOT29,c.CPL29,(c.LIVE29+c.TMP29),"
	        		+ "c.TOT30,c.CPL30,(c.LIVE30+c.TMP30),"
	        		+ "c.TOT31,c.CPL31,(c.LIVE31+c.TMP31),"
	        		+ "cir.name as CircleName,sec.name as SectionName,d.NAME FROM TMP_CT_DEV_MONABST_SEC c,Circle cir,Section sec,Division d "
	        		+ "WHERE cir.id =c.cirCode and sec.id =c.secCode and d.id =sec.DIVISION_ID ";

			
	        List<Object[]> results = session.createNativeQuery(hql).getResultList();

	        sectionList = new ArrayList<>();
	        
	        for (Object[] row : results) {
	        	TmpYearWiseSections report = new TmpYearWiseSections();
				report.setCirCode((String) row[0]);
				report.setSecCode((String) row[1]);
				
				report.setTot1((BigDecimal) row[2]);
				report.setCpl1((BigDecimal) row[3]);
				report.setPend1((BigDecimal)row[4]);
				
				report.setTot2((BigDecimal) row[5]);
				report.setCpl2((BigDecimal) row[6]);
				report.setPend2((BigDecimal)row[7]);
				
				report.setTot3((BigDecimal) row[8]);
				report.setCpl3((BigDecimal) row[9]);
				report.setPend3((BigDecimal)row[10]);
			
				report.setTot4((BigDecimal) row[11]);
				report.setCpl4((BigDecimal) row[12]);
				report.setPend4((BigDecimal)row[13]);
			
				report.setTot5((BigDecimal) row[14]);
				report.setCpl5((BigDecimal) row[15]);
				report.setPend5((BigDecimal)row[16]);
				
				report.setTot6((BigDecimal) row[17]);
				report.setCpl6((BigDecimal) row[18]);
				report.setPend6((BigDecimal)row[19]);
				
				report.setTot7((BigDecimal) row[20]);
				report.setCpl7((BigDecimal) row[21]);
				report.setPend7((BigDecimal)row[22]);
				
				report.setTot8((BigDecimal) row[23]);
				report.setCpl8((BigDecimal) row[24]);
				report.setPend8((BigDecimal)row[25]);
				
				report.setTot9((BigDecimal) row[26]);
				report.setCpl9((BigDecimal) row[27]);
				report.setPend9((BigDecimal)row[28]);
				
				report.setTot10((BigDecimal) row[29]);
				report.setCpl10((BigDecimal) row[30]);
				report.setPend10((BigDecimal)row[31]);
				
				report.setTot11((BigDecimal) row[32]);
				report.setCpl11((BigDecimal) row[33]);
				report.setPend11((BigDecimal)row[34]);
				
				report.setTot12((BigDecimal) row[35]);
				report.setCpl12((BigDecimal) row[36]);
				report.setPend12((BigDecimal)row[37]);
				
				report.setTot13((BigDecimal) row[38]);
				report.setCpl13((BigDecimal) row[39]);
				report.setPend13((BigDecimal)row[40]);
				
				report.setTot14((BigDecimal) row[41]);
				report.setCpl14((BigDecimal) row[42]);
				report.setPend14((BigDecimal)row[43]);
				
				report.setTot15((BigDecimal) row[44]);
				report.setCpl15((BigDecimal) row[45]);
				report.setPend15((BigDecimal)row[46]);
				
				report.setTot16((BigDecimal) row[47]);
				report.setCpl16((BigDecimal) row[48]);
				report.setPend16((BigDecimal)row[49]);
				
				report.setTot17((BigDecimal) row[50]);
				report.setCpl17((BigDecimal) row[51]);
				report.setPend17((BigDecimal)row[52]);
				
				report.setTot18((BigDecimal) row[53]);
				report.setCpl18((BigDecimal) row[54]);
				report.setPend18((BigDecimal)row[55]);
				
				report.setTot19((BigDecimal) row[56]);
				report.setCpl19((BigDecimal) row[57]);
				report.setPend19((BigDecimal)row[58]);
				
				report.setTot20((BigDecimal) row[59]);
				report.setCpl20((BigDecimal) row[60]);
				report.setPend20((BigDecimal)row[61]);
				
				report.setTot21((BigDecimal) row[62]);
				report.setCpl21((BigDecimal) row[63]);
				report.setPend21((BigDecimal)row[64]);
				
				report.setTot22((BigDecimal) row[65]);
				report.setCpl22((BigDecimal) row[66]);
				report.setPend22((BigDecimal)row[67]);
				
				report.setTot23((BigDecimal) row[68]);
				report.setCpl23((BigDecimal) row[69]);
				report.setPend23((BigDecimal)row[70]);
				
				report.setTot24((BigDecimal) row[71]);
				report.setCpl24((BigDecimal) row[72]);
				report.setPend24((BigDecimal)row[73]);
				
				report.setTot25((BigDecimal) row[74]);
				report.setCpl25((BigDecimal) row[75]);
				report.setPend25((BigDecimal)row[76]);
				
				report.setTot26((BigDecimal) row[77]);
				report.setCpl26((BigDecimal) row[78]);
				report.setPend26((BigDecimal)row[79]);
				
				report.setTot27((BigDecimal) row[80]);
				report.setCpl27((BigDecimal) row[81]);
				report.setPend27((BigDecimal)row[82]);
				
				report.setTot28((BigDecimal) row[83]);
				report.setCpl28((BigDecimal) row[84]);
				report.setPend28((BigDecimal)row[85]);
				
				report.setTot29((BigDecimal) row[86]);
				report.setCpl29((BigDecimal) row[87]);
				report.setPend29((BigDecimal)row[88]);
				
				report.setTot30((BigDecimal) row[89]);
				report.setCpl30((BigDecimal) row[90]);
				report.setPend30((BigDecimal)row[91]);
			
				report.setTot31((BigDecimal) row[92]);
				report.setCpl31((BigDecimal) row[93]);
				report.setPend31((BigDecimal)row[94]);			
				
				report.setCircleName((String)row[95]);
				report.setSectionName((String)row[96]);
				report.setDivisionName((String) row[97]);
						 

				sectionList.add(report);
			}

	        System.out.println("DATE WISE SECTION List size: " + sectionList.size());
	        calculateTotalsForSection(sectionList);

	        selectedCircleName = circleName;   
			cameFromInsideReport = true;
			cameFromInsideSection = true;
	        
            FacesContext.getCurrentInstance().getExternalContext().redirect("dateWiseSectionAbstract.xhtml");
            System.out.println("THE PAGE IS REDIRECTED");

	    } catch (Exception e) {
	        e.printStackTrace();
	        System.out.println("Error in database operation");
	    }
	}
	
	@Transactional
	public void fetchReportForSectionUsers() {
		
		
		HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false);
		
		AdminUserValueBean adminUserValueBean = (AdminUserValueBean) 
				httpsession.getAttribute("sessionAdminValueBean");
		
		String circleCode= adminUserValueBean.getCircleId().toString();
		this.selectedCircleId=circleCode;
		String circleName =adminUserValueBean.getCircleName();
		
		String sectionCode = adminUserValueBean.getSectionId().toString();

		if(sectionCode==null || sectionCode.equals("0") || sectionCode.isEmpty()) {
			dmFilter.setSectionCode("A");
		}else {
			dmFilter.setSectionCode(sectionCode);
		}
		
		String subDivisionId = adminUserValueBean.getSubDivisionId().toString();
		String divisionId = adminUserValueBean.getDivisionId().toString();

        if (dmFilter.getFromDate() != null) {
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(dmFilter.getFromDate());
	        
	        Calendar currentCal = Calendar.getInstance();
	        currentCal.setTime(new Date());
        
        if (cal.get(Calendar.YEAR) > currentCal.get(Calendar.YEAR) || 
                (cal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) && 
                		cal.get(Calendar.MONTH) > currentCal.get(Calendar.MONTH))) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "Cannot select future month/year");
                FacesContext.getCurrentInstance().addMessage(null, message);
                return;
            }
        }
		
		
		if (dmFilter.getFromDate() == null) {
			Calendar cal = Calendar.getInstance();
			cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
			dmFilter.setFromDate(cal.getTime());
		}

		
		getColumns();
		
		daysInMonth = getNoOfDays().size();
		dmFilter.setDays(String.valueOf(daysInMonth));
		
		Session session = null;
		
		try {
			session = sessionFactory.openSession();
			session.beginTransaction();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String formattedFromDate = sdf.format(dmFilter.getFromDate());
			
	        if(dmFilter.getComplaintType()==null) {
	        	dmFilter.setComplaintType("AL");
	        }
	        if(dmFilter.getDevice()==null) {
	        	dmFilter.setDevice("L");
	        }


			System.out.println("Processed From Date: " + formattedFromDate);
			System.out.println("COMPLAINT TYPE ==="+ dmFilter.getComplaintType());
			System.out.println("DEVICE TYPE ==="+ dmFilter.getDevice());
			System.out.println("DAY  ==="+ dmFilter.getDays());
			System.out.println("THE CIRCLE CODE: " + circleCode);

			session.createNativeQuery(
					"BEGIN cat_dev_date_secabst(:circd,:sectionCode,:complaintType,:device, TO_DATE(:fromDate, 'YYYY-MM-DD'),:day); END;")
					.setParameter("circd", circleCode)
					.setParameter("sectionCode", dmFilter.getSectionCode())
					.setParameter("complaintType", dmFilter.getComplaintType())
					.setParameter("device", dmFilter.getDevice())
					.setParameter("fromDate", formattedFromDate)
					.setParameter("day", dmFilter.getDays())
					.executeUpdate();

			session.flush();
			session.getTransaction().commit();
			
			String hql ="SELECT c.CIRCODE,c.SECCODE,"
					+ "c.TOT1,c.CPL1,(c.LIVE1+c.TMP1),"
	        		+ "c.TOT2,c.CPL2,(c.LIVE2+c.TMP2),"
	        		+ "c.TOT3,c.CPL3,(c.LIVE3+c.TMP3),"
	        		+ "c.TOT4,c.CPL4,(c.LIVE4+c.TMP4),"
	        		+ "c.TOT5,c.CPL5,(c.LIVE5+c.TMP5),"
	        		+ "c.TOT6,c.CPL6,(c.LIVE6+c.TMP6),"
	        		+ "c.TOT7,c.CPL7,(c.LIVE7+c.TMP7),"
	        		+ "c.TOT8,c.CPL8,(c.LIVE8+c.TMP8),"
	        		+ "c.TOT9,c.CPL9,(c.LIVE9+c.TMP9),"
	        		+ "c.TOT10,c.CPL10,(c.LIVE10+c.TMP10),"
	        		+ "c.TOT11,c.CPL11,(c.LIVE11+c.TMP11),"
	        		+ "c.TOT12,c.CPL12,(c.LIVE12+c.TMP12),"
	        		+ "c.TOT13,c.CPL13,(c.LIVE13+c.TMP13),"
	        		+ "c.TOT14,c.CPL14,(c.LIVE14+c.TMP14),"
	        		+ "c.TOT15,c.CPL15,(c.LIVE15+c.TMP15),"
	        		+ "c.TOT16,c.CPL16,(c.LIVE16+c.TMP16),"
	        		+ "c.TOT17,c.CPL17,(c.LIVE17+c.TMP17),"
	        		+ "c.TOT18,c.CPL18,(c.LIVE18+c.TMP18),"
	        		+ "c.TOT19,c.CPL19,(c.LIVE19+c.TMP19),"
	        		+ "c.TOT20,c.CPL20,(c.LIVE20+c.TMP20),"
	        		+ "c.TOT21,c.CPL21,(c.LIVE21+c.TMP21),"
	        		+ "c.TOT22,c.CPL22,(c.LIVE22+c.TMP22),"
	        		+ "c.TOT23,c.CPL23,(c.LIVE23+c.TMP23),"
	        		+ "c.TOT24,c.CPL24,(c.LIVE24+c.TMP24),"
	        		+ "c.TOT25,c.CPL25,(c.LIVE25+c.TMP25),"
	        		+ "c.TOT26,c.CPL26,(c.LIVE26+c.TMP26),"
	        		+ "c.TOT27,c.CPL27,(c.LIVE27+c.TMP27),"
	        		+ "c.TOT28,c.CPL28,(c.LIVE28+c.TMP28),"
	        		+ "c.TOT29,c.CPL29,(c.LIVE29+c.TMP29),"
	        		+ "c.TOT30,c.CPL30,(c.LIVE30+c.TMP30),"
	        		+ "c.TOT31,c.CPL31,(c.LIVE31+c.TMP31),"
	        		+ "s.name as SectionName, "
	        		+"s.division_id AS DIVISION_ID, " 
		            +"d.name AS DIVISION_NAME, " 
		            +"s.sub_division_id AS SUB_DIVISION_ID " 
	        		+ "FROM TMP_CT_DEV_MONABST_SEC c "
	        		+"JOIN SECTION s ON s.id = c.seccode " 
		            +"JOIN DIVISION d ON d.id = s.division_id";

			
	        List<Object[]> results = session.createNativeQuery(hql).getResultList();

	        List<TmpYearWiseSections> result = new ArrayList<TmpYearWiseSections>();
	        sectionList = new ArrayList<>();
	        
	        for (Object[] row : results) {
	        	TmpYearWiseSections report = new TmpYearWiseSections();
				report.setCirCode((String) row[0]);
				report.setSecCode((String) row[1]);
				
				report.setTot1((BigDecimal) row[2]);
				report.setCpl1((BigDecimal) row[3]);
				report.setPend1((BigDecimal)row[4]);
				
				report.setTot2((BigDecimal) row[5]);
				report.setCpl2((BigDecimal) row[6]);
				report.setPend2((BigDecimal)row[7]);
				
				report.setTot3((BigDecimal) row[8]);
				report.setCpl3((BigDecimal) row[9]);
				report.setPend3((BigDecimal)row[10]);
			
				report.setTot4((BigDecimal) row[11]);
				report.setCpl4((BigDecimal) row[12]);
				report.setPend4((BigDecimal)row[13]);
			
				report.setTot5((BigDecimal) row[14]);
				report.setCpl5((BigDecimal) row[15]);
				report.setPend5((BigDecimal)row[16]);
				
				report.setTot6((BigDecimal) row[17]);
				report.setCpl6((BigDecimal) row[18]);
				report.setPend6((BigDecimal)row[19]);
				
				report.setTot7((BigDecimal) row[20]);
				report.setCpl7((BigDecimal) row[21]);
				report.setPend7((BigDecimal)row[22]);
				
				report.setTot8((BigDecimal) row[23]);
				report.setCpl8((BigDecimal) row[24]);
				report.setPend8((BigDecimal)row[25]);
				
				report.setTot9((BigDecimal) row[26]);
				report.setCpl9((BigDecimal) row[27]);
				report.setPend9((BigDecimal)row[28]);
				
				report.setTot10((BigDecimal) row[29]);
				report.setCpl10((BigDecimal) row[30]);
				report.setPend10((BigDecimal)row[31]);
				
				report.setTot11((BigDecimal) row[32]);
				report.setCpl11((BigDecimal) row[33]);
				report.setPend11((BigDecimal)row[34]);
				
				report.setTot12((BigDecimal) row[35]);
				report.setCpl12((BigDecimal) row[36]);
				report.setPend12((BigDecimal)row[37]);
				
				report.setTot13((BigDecimal) row[38]);
				report.setCpl13((BigDecimal) row[39]);
				report.setPend13((BigDecimal)row[40]);
				
				report.setTot14((BigDecimal) row[41]);
				report.setCpl14((BigDecimal) row[42]);
				report.setPend14((BigDecimal)row[43]);
				
				report.setTot15((BigDecimal) row[44]);
				report.setCpl15((BigDecimal) row[45]);
				report.setPend15((BigDecimal)row[46]);
				
				report.setTot16((BigDecimal) row[47]);
				report.setCpl16((BigDecimal) row[48]);
				report.setPend16((BigDecimal)row[49]);
				
				report.setTot17((BigDecimal) row[50]);
				report.setCpl17((BigDecimal) row[51]);
				report.setPend17((BigDecimal)row[52]);
				
				report.setTot18((BigDecimal) row[53]);
				report.setCpl18((BigDecimal) row[54]);
				report.setPend18((BigDecimal)row[55]);
				
				report.setTot19((BigDecimal) row[56]);
				report.setCpl19((BigDecimal) row[57]);
				report.setPend19((BigDecimal)row[58]);
				
				report.setTot20((BigDecimal) row[59]);
				report.setCpl20((BigDecimal) row[60]);
				report.setPend20((BigDecimal)row[61]);
				
				report.setTot21((BigDecimal) row[62]);
				report.setCpl21((BigDecimal) row[63]);
				report.setPend21((BigDecimal)row[64]);
				
				report.setTot22((BigDecimal) row[65]);
				report.setCpl22((BigDecimal) row[66]);
				report.setPend22((BigDecimal)row[67]);
				
				report.setTot23((BigDecimal) row[68]);
				report.setCpl23((BigDecimal) row[69]);
				report.setPend23((BigDecimal)row[70]);
				
				report.setTot24((BigDecimal) row[71]);
				report.setCpl24((BigDecimal) row[72]);
				report.setPend24((BigDecimal)row[73]);
				
				report.setTot25((BigDecimal) row[74]);
				report.setCpl25((BigDecimal) row[75]);
				report.setPend25((BigDecimal)row[76]);
				
				report.setTot26((BigDecimal) row[77]);
				report.setCpl26((BigDecimal) row[78]);
				report.setPend26((BigDecimal)row[79]);
				
				report.setTot27((BigDecimal) row[80]);
				report.setCpl27((BigDecimal) row[81]);
				report.setPend27((BigDecimal)row[82]);
				
				report.setTot28((BigDecimal) row[83]);
				report.setCpl28((BigDecimal) row[84]);
				report.setPend28((BigDecimal)row[85]);
				
				report.setTot29((BigDecimal) row[86]);
				report.setCpl29((BigDecimal) row[87]);
				report.setPend29((BigDecimal)row[88]);
				
				report.setTot30((BigDecimal) row[89]);
				report.setCpl30((BigDecimal) row[90]);
				report.setPend30((BigDecimal)row[91]);
			
				report.setTot31((BigDecimal) row[92]);
				report.setCpl31((BigDecimal) row[93]);
				report.setPend31((BigDecimal)row[94]);			
				
				report.setSectionName((String)row[95]);
				report.setDivisionId((String)row[96].toString());
				report.setDivisionName((String) row[97]);
				report.setSubDivisionId((String)row[98].toString());
						 

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

	        System.out.println("DATE WISE SECTION List size: " + sectionList.size());
	        calculateTotalsForSection(sectionList);

	        selectedCircleName = circleName;   
	        cameFromInsideSection= true;
            FacesContext.getCurrentInstance().getExternalContext().redirect("dateWiseSectionAbstract.xhtml");

	    } catch (Exception e) {
	        e.printStackTrace();
	        System.out.println("Error in database operation");
	    }
	}
	
	@Transactional
	public void getComplaintListForSection(String secCode, String sectionName) throws IOException, ParseException {
	    try (Session session = sessionFactory.openSession()) {

	        List<String> complaintTypes = new ArrayList<>();
	        List<String> devices = new ArrayList<>();

	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        String fromDateStr = sdf.format(dmFilter.getFromDate());

	        String noOfDays = dmFilter.getDays();
	        LocalDate fromLocalDate = LocalDate.parse(fromDateStr);
	        int daysToAdd = Integer.parseInt(noOfDays);
	        LocalDate toLocalDate = fromLocalDate.plusDays(daysToAdd);

	        Date toDateObj = Date.from(toLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	        String toDateStr = sdf.format(toDateObj);

	        Date fromDate = sdf.parse(fromDateStr);
	        Date toDate = sdf.parse(toDateStr);

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
	                case "AL": complaintTypes.addAll(Arrays.asList("BL", "ME", "PF", "VF", "FI", "TH", "TE", "OT", "CS")); break;
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
	                case "L": devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile", "web", "SM", "admin", "FOC", "MI", "MM")); break;
	                default: throw new IllegalArgumentException("Invalid Device Type");
	            }
	        }

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
	        redirectFrom ="section";

	        FacesContext.getCurrentInstance().getExternalContext().redirect("dateWiseComplaintList.xhtml");

	    } catch (NumberFormatException e) {
	        System.out.println("ERROR..........." + e);
	    }
	}
	
	@Transactional
	public void getComplaintListForCircle() throws IOException {
	    try (Session session = sessionFactory.openSession()) {
	        
	        FacesContext facesContext = FacesContext.getCurrentInstance();
	        String status = facesContext.getExternalContext().getRequestParameterMap().get("status");
	        
	        List<Integer> statusIDs= new ArrayList<>();
	        
	        if(status.equalsIgnoreCase("Received")) {
	            statusIDs = Arrays.asList(CCMSConstants.PENDING,CCMSConstants.COMPLETED,CCMSConstants.IN_PROGRESS);            
	        }
	        else if (status.equalsIgnoreCase("Completed")) {
	            statusIDs = Arrays.asList(CCMSConstants.COMPLETED);            
	        }
	        else if (status.equalsIgnoreCase("Pending")) {
	            statusIDs = Arrays.asList(CCMSConstants.PENDING);            
	        }
	        
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(dmFilter.getFromDate());
	        
	        cal.set(Calendar.DAY_OF_MONTH, 1);
	        Date fromDate = cal.getTime();
	        
	        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
	        cal.set(Calendar.HOUR_OF_DAY, 23);
	        cal.set(Calendar.MINUTE, 59);
	        cal.set(Calendar.SECOND, 59);
	        Date toDate = cal.getTime();
	        
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        String fromDateStr = sdf.format(fromDate);
	        String toDateStr = sdf.format(toDate);
	        
	        System.out.println("THE FROM DATE =="+fromDate);
	        System.out.println("THE TO DATE ===="+toDate);
	        System.err.println("***************************************");
	        System.out.println("THE FROM DATE =="+fromDateStr);
	        System.out.println("THE TO DATE ===="+toDateStr);
	        
	        List<ViewComplaintReportBean> complaintBeanList = new ArrayList<>();
	        
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
	                "WHERE a.STATUS_ID IN (:statusIDs) " +
	                "AND a.created_on BETWEEN :fromDate AND :toDate";
	                    
	        Query query = session.createNativeQuery(hql);
	        query.setParameter("statusIDs", statusIDs);
	        query.setParameter("fromDate", fromDate); 
	        query.setParameter("toDate", toDate); 
	        
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

	        redirectFrom = "circle";
	        selectedSectionName=null;
	        System.out.println("THE COMPLAINT LIST SIZE IS ======"+complaintList.size());
	    
	        FacesContext.getCurrentInstance().getExternalContext().redirect("dateWiseComplaintList.xhtml");
	    
	    }catch(Exception e){
	        System.out.println("ERROR..........."+e);
	        e.printStackTrace();
	    }
	}

	public void exportCirclesToExcel(List<TmpYearWiseCircle> yearWiseCircles) throws IOException {
	    HSSFWorkbook workbook = new HSSFWorkbook();
	    Sheet sheet = workbook.createSheet("Date_wise_Circle_Abstract_Report");

	    List<String> dates = getDayHeaders();
	    int daysInMonth = dates.size();
	    final int COLUMN_COUNT = 2 + (daysInMonth * 3) + 3;
	    
	    // Set column widths
	    sheet.setColumnWidth(0, 1500);
	    sheet.setColumnWidth(1, 4000);
	    for (int i = 2; i < COLUMN_COUNT; i++) {
	        sheet.setColumnWidth(i, 3000);
	    }
	    
	    // Create styles
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
	    
	    CellStyle dateStyle = workbook.createCellStyle();
	    HSSFFont dateFont = workbook.createFont();
	    dateFont.setBold(true);
	    dateFont.setFontHeightInPoints((short) 10);
	    dateStyle.setFont(dateFont);
	    dateStyle.setAlignment(HorizontalAlignment.CENTER);
	    dateStyle.setVerticalAlignment(VerticalAlignment.CENTER);
	    dateStyle.setBorderBottom(BorderStyle.THIN);
	    dateStyle.setBorderTop(BorderStyle.THIN);
	    dateStyle.setBorderLeft(BorderStyle.THIN);
	    dateStyle.setBorderRight(BorderStyle.THIN);

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

	    CellStyle dataCellStyle = workbook.createCellStyle();
	    dataCellStyle.setBorderBottom(BorderStyle.THIN);
	    dataCellStyle.setBorderTop(BorderStyle.THIN);
	    dataCellStyle.setBorderLeft(BorderStyle.THIN);
	    dataCellStyle.setBorderRight(BorderStyle.THIN);
	    dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
	    dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

	    // Create heading row
	    Row headingRow = sheet.createRow(0);
	    Cell headingCell = headingRow.createCell(0);
	    headingCell.setCellValue("DATE WISE CIRCLE ABSTRACT REPORT");
	    headingCell.setCellStyle(headingStyle);
	    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, COLUMN_COUNT-1)); 
	    
	    // Create date row
	    Row dateRow = sheet.createRow(1);
	    Cell dateCell = dateRow.createCell(0);
	    
	    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
	    String fromDateStr = dmFilter.getFromDate() != null ? 
	                         dateFormat.format(dmFilter.getFromDate()) : "N/A";

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

	    dateCell.setCellValue("MONTH and YEAR: " + fromDateStr + " | COMPLAINT TYPE: " + complaintType + " | DEVICE: " + device);
	    dateCell.setCellStyle(dateStyle);
	    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, COLUMN_COUNT-1));

	    // Create header rows
	    Row headerRow1 = sheet.createRow(2);
	    Row headerRow2 = sheet.createRow(3);
	    
	    List<String> headers = new ArrayList<>();
	    headers.add("S.NO");
	    headers.add("CIRCLE");
	    headers.addAll(dates);
	    headers.add("TOTAL");

	    String[] subHeaders = {"Revd.", "Comp.", "Pend."};

	    int colIndex = 0;
	    for (String mainHeader : headers) {
	        if (mainHeader.equals("S.NO") || mainHeader.equals("CIRCLE")) {
	            // For S.NO and CIRCLE columns
	            Cell cell = headerRow1.createCell(colIndex);
	            cell.setCellValue(mainHeader);
	            cell.setCellStyle(headerStyle);
	            sheet.addMergedRegion(new CellRangeAddress(2, 3, colIndex, colIndex));
	            colIndex++;
	        } else {
	            // For date and total columns
	            Cell cell = headerRow1.createCell(colIndex);
	            cell.setCellValue(mainHeader);
	            cell.setCellStyle(headerStyle);
	            sheet.addMergedRegion(new CellRangeAddress(2, 2, colIndex, colIndex + 2));

	            // Add subheaders
	            for (String subHeader : subHeaders) {
	                Cell subCell = headerRow2.createCell(colIndex);
	                subCell.setCellValue(subHeader);
	                subCell.setCellStyle(headerStyle);
	                colIndex++;
	            }
	        }
	    }

	    int rowNum = 4;
	    BigDecimal[] totalSums = new BigDecimal[COLUMN_COUNT - 2];
	    Arrays.fill(totalSums, BigDecimal.ZERO);

	    int serialNumber = 1;
	    for (TmpYearWiseCircle report : yearWiseCircles) {
	        Row row = sheet.createRow(rowNum++);
	        
	        // S.NO column
	        Cell snoCell = row.createCell(0);
	        snoCell.setCellValue(serialNumber++);
	        snoCell.setCellStyle(dataCellStyle);
	        
	        // CIRCLE column
	        Cell circleCell = row.createCell(1);
	        circleCell.setCellValue(report.getCircleName());
	        circleCell.setCellStyle(dataCellStyle);

	        BigDecimal rowTotSum = BigDecimal.ZERO;
	        BigDecimal rowCplSum = BigDecimal.ZERO;
	        BigDecimal rowPendSum = BigDecimal.ZERO;

	        int dataIndex = 2;
	        BigDecimal[] values = new BigDecimal[daysInMonth * 3];
	        for (int day = 1; day <= daysInMonth; day++) {
	            try {
	                Method getTotMethod = report.getClass().getMethod("getTot" + day);
	                Method getCplMethod = report.getClass().getMethod("getCpl" + day);
	                Method getPendMethod = report.getClass().getMethod("getPend" + day);
	                
	                values[(day-1)*3] = (BigDecimal) getTotMethod.invoke(report);
	                values[(day-1)*3 + 1] = (BigDecimal) getCplMethod.invoke(report);
	                values[(day-1)*3 + 2] = (BigDecimal) getPendMethod.invoke(report);
	            } catch (Exception e) {
	                values[(day-1)*3] = BigDecimal.ZERO;
	                values[(day-1)*3 + 1] = BigDecimal.ZERO;
	                values[(day-1)*3 + 2] = BigDecimal.ZERO;
	            }
	        }

	        for (int i = 0; i < values.length; i += 3) {
	            BigDecimal tot = values[i] != null ? values[i] : BigDecimal.ZERO;
	            BigDecimal cpl = values[i + 1] != null ? values[i + 1] : BigDecimal.ZERO;
	            BigDecimal pend = values[i + 2] != null ? values[i + 2] : BigDecimal.ZERO;

	            Cell totCell = row.createCell(dataIndex);
	            totCell.setCellValue(tot.doubleValue());
	            totCell.setCellStyle(dataCellStyle);
	            
	            Cell cplCell = row.createCell(dataIndex + 1);
	            cplCell.setCellValue(cpl.doubleValue());
	            cplCell.setCellStyle(dataCellStyle);
	            
	            Cell pendCell = row.createCell(dataIndex + 2);
	            pendCell.setCellValue(pend.doubleValue());
	            pendCell.setCellStyle(dataCellStyle);

	            rowTotSum = rowTotSum.add(tot);
	            rowCplSum = rowCplSum.add(cpl);
	            rowPendSum = rowPendSum.add(pend);

	            totalSums[dataIndex - 2] = totalSums[dataIndex - 2].add(tot);
	            totalSums[dataIndex - 1] = totalSums[dataIndex - 1].add(cpl);
	            totalSums[dataIndex] = totalSums[dataIndex].add(pend);

	            dataIndex += 3;
	        }

	        // TOTAL columns
	        Cell totTotalCell = row.createCell(dataIndex);
	        totTotalCell.setCellValue(rowTotSum.doubleValue());
	        totTotalCell.setCellStyle(dataCellStyle);
	        
	        Cell cplTotalCell = row.createCell(dataIndex + 1);
	        cplTotalCell.setCellValue(rowCplSum.doubleValue());
	        cplTotalCell.setCellStyle(dataCellStyle);
	        
	        Cell pendTotalCell = row.createCell(dataIndex + 2);
	        pendTotalCell.setCellValue(rowPendSum.doubleValue());
	        pendTotalCell.setCellStyle(dataCellStyle);

	        totalSums[dataIndex - 2] = totalSums[dataIndex - 2].add(rowTotSum);
	        totalSums[dataIndex - 1] = totalSums[dataIndex - 1].add(rowCplSum);
	        totalSums[dataIndex] = totalSums[dataIndex].add(rowPendSum);
	    }

	    // Create total row
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
	    
	    // S.NO column (empty)
	    Cell emptyCell = totalRow.createCell(0);
	    emptyCell.setCellStyle(totalRowStyle);
	    
	    // TOTAL label
	    Cell totalLabelCell = totalRow.createCell(1);
	    totalLabelCell.setCellValue("TOTAL");
	    totalLabelCell.setCellStyle(totalRowStyle);

	    // Total values
	    for (int i = 2; i < COLUMN_COUNT; i++) {
	        if (i - 2 < totalSums.length) {
	            Cell totalCell = totalRow.createCell(i);
	            totalCell.setCellValue(totalSums[i - 2].doubleValue());
	            totalCell.setCellStyle(totalRowStyle);
	        }
	    }

	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    workbook.write(outputStream);
	    workbook.close();

	    excelFile = DefaultStreamedContent.builder()
	            .name("Date_Wise_Circle_Abstract.xls")
	            .contentType("application/vnd.ms-excel")
	            .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
	            .build();
	}
		



		public void exportSectionsToExcel(List<TmpYearWiseSections> reports) throws IOException {
		    HSSFWorkbook workbook = new HSSFWorkbook();
		    Sheet sheet = workbook.createSheet("Date_wise_Section_Abstract_Report");

		    List<String> dates = getDayHeaders();
		    int daysInMonth = dates.size();
		    final int COLUMN_COUNT = 1 + 2 + (daysInMonth * 3) + 3; // S.NO + SECTION + DIVISION + (days * 3 columns) + TOTAL (3 columns)
		    
		    // Set column widths
		    sheet.setColumnWidth(0, 2000);  // S.NO column
		    sheet.setColumnWidth(1, 4000);  // SECTION column
		    sheet.setColumnWidth(2, 4000);  // DIVISION column
		    for (int i = 3; i < COLUMN_COUNT; i++) {
		        sheet.setColumnWidth(i, 3000);
		    }
		    
		    // Create styles
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
		    
		    CellStyle dateStyle = workbook.createCellStyle();
		    HSSFFont dateFont = workbook.createFont();
		    dateFont.setBold(true);
		    dateFont.setFontHeightInPoints((short) 10);
		    dateStyle.setFont(dateFont);
		    dateStyle.setAlignment(HorizontalAlignment.CENTER);
		    dateStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		    dateStyle.setBorderBottom(BorderStyle.THIN);
		    dateStyle.setBorderTop(BorderStyle.THIN);
		    dateStyle.setBorderLeft(BorderStyle.THIN);
		    dateStyle.setBorderRight(BorderStyle.THIN);

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

		    CellStyle dataCellStyle = workbook.createCellStyle();
		    dataCellStyle.setBorderBottom(BorderStyle.THIN);
		    dataCellStyle.setBorderTop(BorderStyle.THIN);
		    dataCellStyle.setBorderLeft(BorderStyle.THIN);
		    dataCellStyle.setBorderRight(BorderStyle.THIN);
		    dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
		    dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		    // Create heading row
		    Row headingRow = sheet.createRow(0);
		    Cell headingCell = headingRow.createCell(0);
		    headingCell.setCellValue("DATE WISE SECTION ABSTRACT REPORT");
		    headingCell.setCellStyle(headingStyle);
		    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, COLUMN_COUNT - 1)); 
		    
		    // Create date row
		    Row dateRow = sheet.createRow(1);
		    Cell dateCell = dateRow.createCell(0);
		    
		    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
		    String fromDateStr = dmFilter.getFromDate() != null ? 
		                         dateFormat.format(dmFilter.getFromDate()) : "N/A";

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

		    dateCell.setCellValue("MONTH and YEAR: " + fromDateStr + " | COMPLAINT TYPE: " + complaintType + " | DEVICE: " + device + " | CIRCLE: " + selectedCircleName);
		    dateCell.setCellStyle(dateStyle);
		    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, COLUMN_COUNT - 1));
		    
		    // Create header rows
		    Row headerRow1 = sheet.createRow(2);
		    Row headerRow2 = sheet.createRow(3);
		    
		    // Create S.NO, SECTION, DIVISION headers
		    headerRow1.createCell(0).setCellValue("S.NO");
		    headerRow1.getCell(0).setCellStyle(headerStyle);
		    sheet.addMergedRegion(new CellRangeAddress(2, 3, 0, 0));
		    
		    headerRow1.createCell(1).setCellValue("SECTION");
		    headerRow1.getCell(1).setCellStyle(headerStyle);
		    sheet.addMergedRegion(new CellRangeAddress(2, 3, 1, 1));
		    
		    headerRow1.createCell(2).setCellValue("DIVISION");
		    headerRow1.getCell(2).setCellStyle(headerStyle);
		    sheet.addMergedRegion(new CellRangeAddress(2, 3, 2, 2));
		    
		    // Create day headers with subheaders
		    int colIndex = 3;
		    for (String date : dates) {
		        Cell dateHeaderCell = headerRow1.createCell(colIndex);
		        dateHeaderCell.setCellValue(date);
		        dateHeaderCell.setCellStyle(headerStyle);
		        sheet.addMergedRegion(new CellRangeAddress(2, 2, colIndex, colIndex + 2));
		        
		        // Create subheaders
		        headerRow2.createCell(colIndex).setCellValue("Revd.");
		        headerRow2.getCell(colIndex).setCellStyle(headerStyle);
		        headerRow2.createCell(colIndex + 1).setCellValue("Comp.");
		        headerRow2.getCell(colIndex + 1).setCellStyle(headerStyle);
		        headerRow2.createCell(colIndex + 2).setCellValue("Pend.");
		        headerRow2.getCell(colIndex + 2).setCellStyle(headerStyle);
		        
		        colIndex += 3;
		    }
		    
		    // Create TOTAL header
		    Cell totalHeaderCell = headerRow1.createCell(colIndex);
		    totalHeaderCell.setCellValue("TOTAL");
		    totalHeaderCell.setCellStyle(headerStyle);
		    sheet.addMergedRegion(new CellRangeAddress(2, 2, colIndex, colIndex + 2));
		    
		    // Create TOTAL subheaders
		    headerRow2.createCell(colIndex).setCellValue("Revd.");
		    headerRow2.getCell(colIndex).setCellStyle(headerStyle);
		    headerRow2.createCell(colIndex + 1).setCellValue("Comp.");
		    headerRow2.getCell(colIndex + 1).setCellStyle(headerStyle);
		    headerRow2.createCell(colIndex + 2).setCellValue("Pend.");
		    headerRow2.getCell(colIndex + 2).setCellStyle(headerStyle);

		    // Populate data rows
		    int rowNum = 4;
		    BigDecimal[] totalSums = new BigDecimal[COLUMN_COUNT - 3]; // Exclude S.NO, SECTION, DIVISION
		    Arrays.fill(totalSums, BigDecimal.ZERO);

		    int serialNumber = 1;
		    for (TmpYearWiseSections report : reports) {
		        Row row = sheet.createRow(rowNum++);
		        
		        // Serial number
		        Cell snCell = row.createCell(0);
		        snCell.setCellValue(serialNumber++);
		        snCell.setCellStyle(dataCellStyle);
		        
		        // Section name
		        Cell sectionCell = row.createCell(1);
		        sectionCell.setCellValue(report.getSectionName());
		        sectionCell.setCellStyle(dataCellStyle);
		        
		        // Division name
		        Cell divisionCell = row.createCell(2);
		        divisionCell.setCellValue(report.getDivisionName());
		        divisionCell.setCellStyle(dataCellStyle);

		        BigDecimal rowTotSum = BigDecimal.ZERO;
		        BigDecimal rowCplSum = BigDecimal.ZERO;
		        BigDecimal rowPendSum = BigDecimal.ZERO;

		        int dataIndex = 3;
		        
		        // Populate day data
		        for (int day = 1; day <= daysInMonth; day++) {
		            try {
		                Method getTotMethod = report.getClass().getMethod("getTot" + day);
		                Method getCplMethod = report.getClass().getMethod("getCpl" + day);
		                Method getPendMethod = report.getClass().getMethod("getPend" + day);
		                
		                BigDecimal tot = (BigDecimal) getTotMethod.invoke(report);
		                BigDecimal cpl = (BigDecimal) getCplMethod.invoke(report);
		                BigDecimal pend = (BigDecimal) getPendMethod.invoke(report);
		                
		                tot = tot != null ? tot : BigDecimal.ZERO;
		                cpl = cpl != null ? cpl : BigDecimal.ZERO;
		                pend = pend != null ? pend : BigDecimal.ZERO;

		                // Set cell values
		                Cell totCell = row.createCell(dataIndex);
		                totCell.setCellValue(tot.doubleValue());
		                totCell.setCellStyle(dataCellStyle);
		                
		                Cell cplCell = row.createCell(dataIndex + 1);
		                cplCell.setCellValue(cpl.doubleValue());
		                cplCell.setCellStyle(dataCellStyle);
		                
		                Cell pendCell = row.createCell(dataIndex + 2);
		                pendCell.setCellValue(pend.doubleValue());
		                pendCell.setCellStyle(dataCellStyle);

		                // Update sums
		                rowTotSum = rowTotSum.add(tot);
		                rowCplSum = rowCplSum.add(cpl);
		                rowPendSum = rowPendSum.add(pend);

		                totalSums[dataIndex - 3] = totalSums[dataIndex - 3].add(tot);
		                totalSums[dataIndex - 2] = totalSums[dataIndex - 2].add(cpl);
		                totalSums[dataIndex - 1] = totalSums[dataIndex - 1].add(pend);

		                dataIndex += 3;
		            } catch (Exception e) {
		                // If method doesn't exist, set to zero
		                row.createCell(dataIndex).setCellValue(0);
		                row.createCell(dataIndex + 1).setCellValue(0);
		                row.createCell(dataIndex + 2).setCellValue(0);
		                dataIndex += 3;
		            }
		        }

		        // Add TOTAL columns
		        Cell totTotalCell = row.createCell(dataIndex);
		        totTotalCell.setCellValue(rowTotSum.doubleValue());
		        totTotalCell.setCellStyle(dataCellStyle);
		        
		        Cell cplTotalCell = row.createCell(dataIndex + 1);
		        cplTotalCell.setCellValue(rowCplSum.doubleValue());
		        cplTotalCell.setCellStyle(dataCellStyle);
		        
		        Cell pendTotalCell = row.createCell(dataIndex + 2);
		        pendTotalCell.setCellValue(rowPendSum.doubleValue());
		        pendTotalCell.setCellStyle(dataCellStyle);

		        // Update total sums
		        totalSums[dataIndex - 3] = totalSums[dataIndex - 3].add(rowTotSum);
		        totalSums[dataIndex - 2] = totalSums[dataIndex - 2].add(rowCplSum);
		        totalSums[dataIndex - 1] = totalSums[dataIndex - 1].add(rowPendSum);
		    }

		    // Create Total Row
		    Row totalRow = sheet.createRow(rowNum);
		    
		    // Create cells for S.NO, SECTION, DIVISION
		    totalRow.createCell(0).setCellValue("TOTAL");
		    totalRow.getCell(0).setCellStyle(headerStyle);
		    totalRow.createCell(1).setCellValue("");
		    totalRow.getCell(1).setCellStyle(headerStyle);
		    totalRow.createCell(2).setCellValue("");
		    totalRow.getCell(2).setCellStyle(headerStyle);

		    // Add total values
		    for (int i = 0; i < totalSums.length; i++) {
		        Cell totalCell = totalRow.createCell(i + 3);
		        totalCell.setCellValue(totalSums[i].doubleValue());
		        totalCell.setCellStyle(headerStyle);
		    }

		    // Write to output stream
		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    workbook.write(outputStream);
		    workbook.close();

		    // Create StreamedContent for download
		    excelFile = DefaultStreamedContent.builder()
		            .name("Date_wise_Section_Abstract_Report.xls")
		            .contentType("application/vnd.ms-excel")
		            .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
		            .build();
		}

	// Helper method to set cell value
	private void setCellValue(Row row, int columnIndex, BigDecimal value) {
	    Cell cell = row.createCell(columnIndex);
	    if (value != null) {
	        cell.setCellValue(value.doubleValue());
	    } else {
	        cell.setCellValue(0);
	    }
	}
	 
	 
	public void exportToPdf(List<TmpYearWiseCircle> circleList) throws DocumentException {
    	List<String> dates = getDayHeaders();
    	final int DAYS = dates.size();
	    final int SUB_COLUMNS = 3;
	    final int COLUMN_COUNT = 1 + 1 + (DAYS * SUB_COLUMNS) + 3; 
	    
	    final float SNO_COLUMN_WIDTH = 5f;
	    final float FIRST_COLUMN_WIDTH = 15f;
	    final float OTHER_COLUMN_WIDTH = 4f;
	    final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
	    final Font TOTAL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
	    final Font DATA_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
	    final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
	    
	    
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
	    
	    SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
	    String fromDate = sdf.format(dmFilter.getFromDate());

	    Document document = new Document(PageSize.A0.rotate());
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	    try {
	        PdfWriter.getInstance(document, outputStream);
	        document.open();
	        
	        PdfPTable table = new PdfPTable(COLUMN_COUNT);
	        table.setWidthPercentage(100);
	        table.setSpacingBefore(10f);
	        
	        Paragraph title = new Paragraph("DATE WISE CIRCLE REPORT", TITLE_FONT);
	        title.setAlignment(Element.ALIGN_CENTER);
	        document.add(title);
	        
	        Paragraph subTitle = new Paragraph("Month and Year: " + fromDate + " | " + "Complaint Type : " +complaintType +" | "+"Device: "+device);
	        subTitle.setAlignment(Element.ALIGN_CENTER);
	        subTitle.setSpacingAfter(10);
	        document.add(subTitle);
	        
	        // COLUMN WIDTH
	        float[] columnWidths = new float[COLUMN_COUNT];
	        columnWidths[0] = SNO_COLUMN_WIDTH;
	        columnWidths[1] = FIRST_COLUMN_WIDTH;
	        for (int i = 2; i < COLUMN_COUNT; i++) {
	            columnWidths[i] = OTHER_COLUMN_WIDTH;
	        }
	        table.setWidths(columnWidths);

	        //HEADER
	        addMergedHeaderCell(table, "S.NO", HEADER_FONT, 1, 2);

	        addMergedHeaderCell(table, "CIRCLE", HEADER_FONT, 1, 2);
	        
	        for (String date : dates) {
	            addMergedHeaderCell(table, date, HEADER_FONT, 3, 1);
	        }
	        addMergedHeaderCell(table, "TOTAL", HEADER_FONT, 3, 1);
	        
	        //SUB HEADER
	        String[] subHeaders = {"Revd.", "Comp.", "Pend."};
	        for (int i = 0; i < DAYS + 1; i++) { 
	            for (String subHeader : subHeaders) {
	                PdfPCell cell = new PdfPCell(new Phrase(subHeader, HEADER_FONT));
	                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
	                table.addCell(cell);
	            }
	        }
	        BigDecimal grandTot = BigDecimal.ZERO;
	        BigDecimal grandTotCpl = BigDecimal.ZERO;
	        BigDecimal grandTotPend = BigDecimal.ZERO;

	        //TABLE DATA
	        int serialNumber = 1;
	        for (TmpYearWiseCircle report : circleList) {
                table.addCell(createDataCell(String.valueOf(serialNumber++), DATA_FONT, Element.ALIGN_CENTER));

                table.addCell(createDataCell(report.getCircleName(), DATA_FONT, Element.ALIGN_LEFT));

                addHourDataForDays(table, report, DAYS, DATA_FONT);

                BigDecimal total = BigDecimal.ZERO;
                BigDecimal totalCpl = BigDecimal.ZERO;
                BigDecimal pend = BigDecimal.ZERO;

                for (int i = 1; i <= DAYS; i++) {
                    total = total.add(getFieldValue(report, "Tot" + i));
                    totalCpl = totalCpl.add(getFieldValue(report, "Cpl" + i));
                    pend = pend.add(getFieldValue(report, "Pend" + i));
                }

                addHourData(table, total, totalCpl, pend, DATA_FONT);

                grandTot = grandTot.add(total);
                grandTotCpl = grandTotCpl.add(totalCpl);
                grandTotPend = grandTotPend.add(pend);
            }

            // GRAND TOTAL ROW
            // Add S.NO cell for the total row (empty cell with appropriate styling)
            PdfPCell emptySnoCell = new PdfPCell(new Phrase("", TOTAL_FONT));
            emptySnoCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            emptySnoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(emptySnoCell);
            
            // Add TOTAL label in the CIRCLE column
            PdfPCell footerLabel = new PdfPCell(new Phrase("TOTAL", TOTAL_FONT));
            footerLabel.setBackgroundColor(BaseColor.LIGHT_GRAY);
            footerLabel.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(footerLabel);

            BigDecimal[] grandTotalsTot = new BigDecimal[DAYS];
            BigDecimal[] grandTotalsCpl = new BigDecimal[DAYS];
            BigDecimal[] grandTotalsPend = new BigDecimal[DAYS];

            // Initialize grand total arrays
            for (int i = 0; i < DAYS; i++) {
                grandTotalsTot[i] = BigDecimal.ZERO;
                grandTotalsCpl[i] = BigDecimal.ZERO;
                grandTotalsPend[i] = BigDecimal.ZERO;
            }

            // Calculate grand totals for each day
            for (TmpYearWiseCircle report : circleList) {
                for (int i = 1; i <= DAYS; i++) {
                    grandTotalsTot[i - 1] = grandTotalsTot[i - 1].add(getFieldValue(report, "Tot" + i));
                    grandTotalsCpl[i - 1] = grandTotalsCpl[i - 1].add(getFieldValue(report, "Cpl" + i));
                    grandTotalsPend[i - 1] = grandTotalsPend[i - 1].add(getFieldValue(report, "Pend" + i));
                }
            }

            // Add grand total for each day to the table
            for (int i = 0; i < DAYS; i++) {
                addHourData(table, grandTotalsTot[i], grandTotalsCpl[i], grandTotalsPend[i], TOTAL_FONT);
            }

            // Add final grand total columns
            addHourData(table, grandTot, grandTotCpl, grandTotPend, TOTAL_FONT);

            document.add(table);
            document.close();

            pdfFile = DefaultStreamedContent.builder()
                    .contentType("application/pdf")
                    .name("Date_wise_Circle_Abstract_Report.pdf")
                    .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
                    .build();

        } catch (Exception e) {
            throw new DocumentException("PDF generation failed: " + e.getMessage());
        }
    }
    
  
  

    private void addHourDataForDays(PdfPTable table, TmpYearWiseCircle report, int days, Font font) {
        for (int i = 1; i <= days; i++) {
            addHourData(table, getFieldValue(report, "Tot" + i), getFieldValue(report, "Cpl" + i), getFieldValue(report, "Pend" + i), font);
        }
    }

    private BigDecimal getFieldValue(TmpYearWiseCircle report, String fieldName) {
        try {
            java.lang.reflect.Method method = TmpYearWiseCircle.class.getMethod("get" + fieldName);
            Object value = method.invoke(report);
            return (BigDecimal) value;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
	
  public void exportSectionsToPdf(List<TmpYearWiseSections> sectionList) throws DocumentException {
	  List<String> dates = getDayHeaders();
	  
		final int DAYS = dates.size();
	    final int SUB_COLUMNS = 3;
	    final int COLUMN_COUNT = 1+2 + (DAYS * SUB_COLUMNS) + 3; 
	    
	    final float SNO_COLUMN_WIDTH = 5f;
	    final float FIRST_COLUMN_WIDTH = 15f;
	    final float OTHER_COLUMN_WIDTH = 4f;
	    final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
	    final Font TOTAL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
	    final Font DATA_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
	    final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
	    
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
	    
	    SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
	    String fromDate = sdf.format(dmFilter.getFromDate());

	    Document document = new Document(PageSize.A0.rotate());
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	    try {
	        PdfWriter.getInstance(document, outputStream);
	        document.open();
	        
	        PdfPTable table = new PdfPTable(COLUMN_COUNT);
	        table.setWidthPercentage(100);
	        table.setSpacingBefore(10f);
	        
	        Paragraph title = new Paragraph("DATE WISE SECTION REPORT", TITLE_FONT);
	        title.setAlignment(Element.ALIGN_CENTER);
	        document.add(title);
	        
	        Paragraph subTitle = new Paragraph("CIRCLE : "+selectedCircleName+" | "+"Month and Year: " + fromDate + " | " + "Complaint Type : " +complaintType +" | "+"Device: "+device);
	        subTitle.setAlignment(Element.ALIGN_CENTER);
	        subTitle.setSpacingAfter(10);
	        document.add(subTitle);
	        
	        // COLUMN WIDTH
	        float[] columnWidths = new float[COLUMN_COUNT];
	        columnWidths[0] = SNO_COLUMN_WIDTH;
	        columnWidths[1] = FIRST_COLUMN_WIDTH;
	        columnWidths[2] = FIRST_COLUMN_WIDTH;
	        for (int i = 3; i < COLUMN_COUNT; i++) {
	            columnWidths[i] = OTHER_COLUMN_WIDTH;
	        }
	        table.setWidths(columnWidths);

	        //HEADER
	        addMergedHeaderCell(table, "S.NO", HEADER_FONT, 1, 2);
	        addMergedHeaderCell(table, "SECTION", HEADER_FONT, 1, 2);
	        addMergedHeaderCell(table, "DIVISION", HEADER_FONT, 1, 2);
	        for (String date:dates) {
	            addMergedHeaderCell(table,date, HEADER_FONT, 3, 1);
	        }
	        addMergedHeaderCell(table, "TOTAL", HEADER_FONT, 3, 1);
	        
	        //SUB HEADER
	        String[] subHeaders = {"Revd.", "Comp.", "Pend."};
	        for (int i = 0; i < DAYS + 1; i++) { 
	            for (String subHeader : subHeaders) {
	                PdfPCell cell = new PdfPCell(new Phrase(subHeader, HEADER_FONT));
	                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
	                table.addCell(cell);
	            }
	        }
	        BigDecimal grandTot = BigDecimal.ZERO;
	        BigDecimal grandTotCpl =BigDecimal.ZERO;
	        BigDecimal grandTotPend = BigDecimal.ZERO;

	        //TABLE DATA
	        int serialNumber=1;
	        for (TmpYearWiseSections report : sectionList) {
                table.addCell(createDataCell(String.valueOf(serialNumber++), DATA_FONT, Element.ALIGN_LEFT));
                table.addCell(createDataCell(report.getSectionName(), DATA_FONT, Element.ALIGN_LEFT));
                table.addCell(createDataCell(report.getDivisionName(), DATA_FONT, Element.ALIGN_LEFT));

                addHourDataForDays(table, report, DAYS, DATA_FONT);

                BigDecimal total = BigDecimal.ZERO;
                BigDecimal totalCpl = BigDecimal.ZERO;
                BigDecimal pend = BigDecimal.ZERO;

                for (int i = 1; i <= DAYS; i++) {
                    total = total.add(getFieldValue(report, "Tot" + i));
                    totalCpl = totalCpl.add(getFieldValue(report, "Cpl" + i));
                    pend = pend.add(getFieldValue(report, "Pend" + i));
                }

                addHourData(table, total, totalCpl, pend, TOTAL_FONT);

                grandTot = grandTot.add(total);
                grandTotCpl = grandTotCpl.add(totalCpl);
                grandTotPend = grandTotPend.add(pend);
            }

            // GRAND TOTAL
            PdfPCell footerLabel = new PdfPCell(new Phrase("TOTAL", TOTAL_FONT));
            footerLabel.setBackgroundColor(BaseColor.LIGHT_GRAY);
            footerLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            footerLabel.setRowspan(2);
            footerLabel.setColspan(3);
            table.addCell(footerLabel);

            BigDecimal[] grandTotalsTot = new BigDecimal[DAYS];
            BigDecimal[] grandTotalsCpl = new BigDecimal[DAYS];
            BigDecimal[] grandTotalsPend = new BigDecimal[DAYS];

            // Initialize grand total arrays
            for (int i = 0; i < DAYS; i++) {
                grandTotalsTot[i] = BigDecimal.ZERO;
                grandTotalsCpl[i] = BigDecimal.ZERO;
                grandTotalsPend[i] = BigDecimal.ZERO;
            }

            // Calculate grand totals for each day
            for (TmpYearWiseSections report : sectionList) {
                for (int i = 1; i <= DAYS; i++) {
                    grandTotalsTot[i - 1] = grandTotalsTot[i - 1].add(getFieldValue(report, "Tot" + i));
                    grandTotalsCpl[i - 1] = grandTotalsCpl[i - 1].add(getFieldValue(report, "Cpl" + i));
                    grandTotalsPend[i - 1] = grandTotalsPend[i - 1].add(getFieldValue(report, "Pend" + i));
                }
            }

            // Add grand total for each day to the table
            for (int i = 0; i < DAYS; i++) {
                addHourData(table, grandTotalsTot[i], grandTotalsCpl[i], grandTotalsPend[i], TOTAL_FONT);
            }

            addHourData(table, grandTot, grandTotCpl, grandTotPend, TOTAL_FONT);

            document.add(table);
            document.close();

            pdfFile = DefaultStreamedContent.builder()
                    .contentType("application/pdf")
                    .name("Date_wise_Section_Abstract_Report.pdf")
                    .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
                    .build();

        } catch (Exception e) {
            throw new DocumentException("PDF generation failed: " + e.getMessage());
        }
    }
  private void addHourDataForDays(PdfPTable table, TmpYearWiseSections report, int days, Font font) {
      for (int i = 1; i <= days; i++) {
          addHourData(table, getFieldValue(report, "Tot" + i), getFieldValue(report, "Cpl" + i), getFieldValue(report, "Pend" + i), font);
      }
  }

  private BigDecimal getFieldValue(TmpYearWiseSections report, String fieldName) {
      try {
          java.lang.reflect.Method method = TmpYearWiseSections.class.getMethod("get" + fieldName);
          Object value = method.invoke(report);
          return (BigDecimal) value;
      } catch (Exception e) {
          return BigDecimal.ZERO;
      }
  }

	private void addMergedHeaderCell(PdfPTable table, String text, Font font, int colSpan, int rowSpan) {
	    PdfPCell cell = new PdfPCell(new Phrase(text, font));
	    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	    cell.setColspan(colSpan);
	    cell.setRowspan(rowSpan);
	    cell.setPadding(5);
	    cell.setBackgroundColor(BaseColor.GRAY);
	    table.addCell(cell);
	}
	


	private PdfPCell createDataCell(String text, Font font, int alignment) {
	    PdfPCell cell = new PdfPCell(new Phrase(text, font));
	    cell.setHorizontalAlignment(alignment);
	    return cell;
	}

	private void addHourData(PdfPTable table, BigDecimal revd, BigDecimal cpl, BigDecimal pend, Font font) {
	    table.addCell(createDataCell(revd.toString(), font, Element.ALIGN_CENTER));
	    table.addCell(createDataCell(cpl.toString(), font, Element.ALIGN_CENTER));
	    table.addCell(createDataCell(pend.toString(), font, Element.ALIGN_CENTER));
	}

	  public void exportComplaintListToExcel(List<ViewComplaintReportValueBean> complaintList) throws IOException {
		  HSSFWorkbook workbook = new HSSFWorkbook();
		  Sheet sheet = workbook.createSheet("Date_wise_Abstract_Report_Complaints");
		  
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
		                .name("Date_wise_Abstract_Report_Complaints.xls")
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
		        Paragraph title = new Paragraph("DATE WISE ABSTRACT COMPLAINT LIST", titleFont);
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
		            .name("Date_wise_Abstract_Report_Complaints.pdf")
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
		
		
		
		public List<String> getDayHeaders() {
		    if(dmFilter.getFromDate() == null) {
		        dmFilter.setFromDate(new Date());
		    }
		    
		    Date fromDate = dmFilter.getFromDate();
		    Calendar cal = Calendar.getInstance();
		    cal.setTime(fromDate);
		    
		    int selectedMonth = cal.get(Calendar.MONTH) + 1; 
		    int selectedYear = cal.get(Calendar.YEAR);
		    
		    
		    Calendar today = Calendar.getInstance();
		    int currentMonth = today.get(Calendar.MONTH) + 1;
		    int currentYear = today.get(Calendar.YEAR);
		    int currentDay = today.get(Calendar.DAY_OF_MONTH);
		    
		    YearMonth yearMonth = YearMonth.of(selectedYear, selectedMonth);
		    int daysInMonth = yearMonth.lengthOfMonth();
		    
		    List<String> dateList = new ArrayList<>();
		    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		    
		    int limitDay = (selectedMonth == currentMonth && selectedYear == currentYear) 
		            ? currentDay 
		            : daysInMonth;
		    
		    for (int day = 1; day <= limitDay; day++) {
		        cal.set(Calendar.DAY_OF_MONTH, day);
		        dateList.add(sdf.format(cal.getTime()));
		    }

		    
		    return dateList;
		}

		public List<Integer> getNoOfDays() {
		    if(dmFilter.getFromDate() == null) {
		        dmFilter.setFromDate(new Date());
		    }
		    
		    Calendar cal = Calendar.getInstance();
		    cal.setTime(dmFilter.getFromDate());
		    
		    Calendar currentCal = Calendar.getInstance();
		    
		    int selectedMonth = cal.get(Calendar.MONTH); // USER SELECTED MONTH
		    int selectedYear = cal.get(Calendar.YEAR);  // USER SELECTED YEAR
		    
		    int currentMonth = currentCal.get(Calendar.MONTH); // CURRENT MONTH
		    int currentYear = currentCal.get(Calendar.YEAR);  // CURRENT YEAR
		    
		    
		    if (selectedMonth == currentMonth && selectedYear == currentYear) {
		    	daysInMonth = currentCal.get(Calendar.DAY_OF_MONTH);
		    }
		    else {
		    	daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		    }
		    
		    List<Integer> dayIndexes = new ArrayList<>();
		    for (int i = 1; i <= daysInMonth; i++) {
		        dayIndexes.add(i);
		    }
		    return dayIndexes;
		}
		
			public List<String> getColumns() {
				int days =getNoOfDays().size();
		         columns = new ArrayList<>();
		        for (int i = 1; i <= days; i++) {
		            columns.add("report.tot" + String.valueOf(i));
		            columns.add("report.cpl" + String.valueOf(i));
		            columns.add("report.pend" + String.valueOf(i));
		        }
		        return columns;
		    }

			
			public boolean isNotEmpty(Object value) {
			    return value != null && !value.toString().trim().isEmpty();
			}
			
			

			public void calculateTotals() {
			    if (circleList != null) {
			        for (TmpYearWiseCircle report : circleList) {
			        	totalReceived=(calculateSum(report, "Tot"));
			        	totalCompleted=(calculateSum(report, "Cpl"));
			        	totalPending=(calculateSum(report, "Pend"));

			        }			        

			        this.totalReceived = circleList.stream()
			        	    .map(TmpYearWiseCircle::getTotalReceived)
			        	    .reduce(BigDecimal.ZERO, BigDecimal::add);

			        	this.totalCompleted = circleList.stream()
			        	    .map(TmpYearWiseCircle::getTotalCompleted)
			        	    .reduce(BigDecimal.ZERO, BigDecimal::add);

			        	this.totalPending = circleList.stream()
			        	    .map(TmpYearWiseCircle::getTotalPending)
			        	    .reduce(BigDecimal.ZERO, BigDecimal::add);
			    }
			}
			
			public void calculateTotalsForSection(List<TmpYearWiseSections> sectionList) {
			    if (sectionList == null || sectionList.isEmpty()) {
			        return;
			    }

			    
			    // Reset totals before calculation
			    this.totalReceivedSection = BigDecimal.ZERO;
			    this.totalCompletedSection = BigDecimal.ZERO;
			    this.totalPendingSection = BigDecimal.ZERO;

			    // Calculate sums for each section
			    sectionList.forEach(report -> {
			        this.totalReceivedSection = this.totalReceivedSection.add(calculateSumForSections(report, "Tot"));
			        this.totalCompletedSection = this.totalCompletedSection.add(calculateSumForSections(report, "Cpl"));
			        this.totalPendingSection = this.totalPendingSection.add(calculateSumForSections(report, "Pend"));
			    });

			    // Log final totals
			    System.out.println("THE TOTAL REVD---------------"+totalReceivedSection);
			}
			
			private BigDecimal calculateSumForSections(TmpYearWiseSections report, String prefix) {
				BigDecimal sum = BigDecimal.ZERO;
				int days =getNoOfDays().size();
			    for (int i = 1; i <= days; i++) {
			        try {
			        	String methodName = "get" + prefix + i;
			        	Method method = report.getClass().getMethod(methodName);
			        	Object value = method.invoke(report);

			        	if (value != null && value instanceof BigDecimal) {
			        		sum = sum.add((BigDecimal) value);
			            }
			            
			        } catch (Exception e) {
			        	e.printStackTrace();
			        }
			    }
			    return sum;
			}

			private BigDecimal calculateSum(TmpYearWiseCircle report, String prefix) {
				BigDecimal sum = BigDecimal.ZERO;
				int days =getNoOfDays().size();
			    for (int i = 1; i <= days; i++) {
			        try {
			        	String methodName = "get" + prefix + i;
			        	Method method = report.getClass().getMethod(methodName);
			        	Object value = method.invoke(report);

			        	if (value != null && value instanceof BigDecimal) {
			        		sum = sum.add((BigDecimal) value);
			            }
			            
			        } catch (Exception e) {
			        	e.printStackTrace();
			        }
			    }
			    return sum;
			}
	 
			public boolean shouldShowExtendedDays() {
			    return IntStream.of(29, 30, 31)
			        .anyMatch(day -> hasDayData(day, "received") || 
			                         hasDayData(day, "completed") || 
			                         hasDayData(day, "pending"));
			}

			public boolean hasDayData(int day, String type) {
			    return circleList.stream()
			        .anyMatch(r -> {
			            BigDecimal value = getDayValue(r, day, type);
			            return value != null && value.compareTo(BigDecimal.ZERO) > 0;
			        });
			}

			public BigDecimal getDayTotal(int day, String type) {
			    return circleList.stream()
			        .map(r -> getDayValue(r, day, type))
			        .filter(Objects::nonNull)
			        .reduce(BigDecimal.ZERO, BigDecimal::add);
			}

			private BigDecimal getDayValue(TmpYearWiseCircle r, int day, String type) {
			    switch (day) {
			        case 29:
			            return "received".equals(type) ? r.getTot29() : 
			                   "completed".equals(type) ? r.getCpl29() : r.getPend29();
			        case 30:
			            return "received".equals(type) ? r.getTot30() : 
			                   "completed".equals(type) ? r.getCpl30() : r.getPend30();
			        case 31:
			            return "received".equals(type) ? r.getTot31() : 
			                   "completed".equals(type) ? r.getCpl31() : r.getPend31();
			        default:
			            return BigDecimal.ZERO;
			    }
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


	public String getRedirectFrom() {
		return redirectFrom;
	}

	public void setRedirectFrom(String redirectFrom) {
		this.redirectFrom = redirectFrom;
	}

	public ViewComplaintReportValueBean getSelectedComplaintId() {
		return selectedComplaintId;
	}

	public void setSelectedComplaintId(ViewComplaintReportValueBean selectedComplaintId) {
		this.selectedComplaintId = selectedComplaintId;
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

	public BigDecimal getTotalReceivedSection() {
		return totalReceivedSection;
	}

	public void setTotalReceivedSection(BigDecimal totalReceivedSection) {
		this.totalReceivedSection = totalReceivedSection;
	}

	public BigDecimal getTotalCompletedSection() {
		return totalCompletedSection;
	}

	public void setTotalCompletedSection(BigDecimal totalCompletedSection) {
		this.totalCompletedSection = totalCompletedSection;
	}

	public BigDecimal getTotalPendingSection() {
		return totalPendingSection;
	}

	public void setTotalPendingSection(BigDecimal totalPendingSection) {
		this.totalPendingSection = totalPendingSection;
	}

	public String getSelectedCircleId() {
		return selectedCircleId;
	}

	public void setSelectedCircleId(String selectedCircleId) {
		this.selectedCircleId = selectedCircleId;
	}
	  

		public String getCircleIdFromMonthlyAbstract() {
			return circleIdFromMonthlyAbstract;
		}

		public void setCircleIdFromMonthlyAbstract(String circleIdFromMonthlyAbstract) {
			this.circleIdFromMonthlyAbstract = circleIdFromMonthlyAbstract;
		}

		public String getCircleNameFromMonthlyAbstract() {
			return circleNameFromMonthlyAbstract;
		}

		public void setCircleNameFromMonthlyAbstract(String circleNameFromMonthlyAbstract) {
			this.circleNameFromMonthlyAbstract = circleNameFromMonthlyAbstract;
		}

		public boolean isCameFromMonthlyAbstractReport() {
			return cameFromMonthlyAbstractReport;
		}

		public void setCameFromMonthlyAbstractReport(boolean cameFromMonthlyAbstractReport) {
			this.cameFromMonthlyAbstractReport = cameFromMonthlyAbstractReport;
		}

		public String getSelectedMonth() {
			return selectedMonth;
		}

		public void setSelectedMonth(String selectedMonth) {
			this.selectedMonth = selectedMonth;
		}

		public void setDaysToDisplay(List<Integer> daysToDisplay) {
			this.daysToDisplay = daysToDisplay;
		}

		public int getMaxDaysToShow() {
			return maxDaysToShow;
		}

		public void setMaxDaysToShow(int maxDaysToShow) {
			this.maxDaysToShow = maxDaysToShow;
		}

		public List<CompDeviceValueBean> getDevices() {
			return devices;
		}

		public void setDevices(List<CompDeviceValueBean> devices) {
			this.devices = devices;
		}
		
		

		public String getSelectedComplaintType() {
			return selectedComplaintType;
		}

		public void setSelectedComplaintType(String selectedComplaintType) {
			this.selectedComplaintType = selectedComplaintType;
		}

		public String getSelectedDevice() {
			return selectedDevice;
		}

		public void setSelectedDevice(String selectedDevice) {
			this.selectedDevice = selectedDevice;
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


		public int getDaysInMonth() {
			return daysInMonth;
		}

		public void setDaysInMonth(int daysInMonth) {
			this.daysInMonth = daysInMonth;
		}

		public void setTotalReceived(BigDecimal totalReceived) {
			this.totalReceived = totalReceived;
		}

		public void setTotalCompleted(BigDecimal totalCompleted) {
			this.totalCompleted = totalCompleted;
		}

		public void setTotalPending(BigDecimal totalPending) {
			this.totalPending = totalPending;
		}

		public void setColumns(List<String> columns) {
			this.columns = columns;
		}

		public boolean isCameViaCircleReport() {
			return cameViaCircleReport;
		}

		public void setCameViaCircleReport(boolean cameViaCircleReport) {
			this.cameViaCircleReport = cameViaCircleReport;
		}



}
