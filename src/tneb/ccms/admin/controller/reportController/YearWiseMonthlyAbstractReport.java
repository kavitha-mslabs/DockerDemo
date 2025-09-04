package tneb.ccms.admin.controller.reportController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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

import org.hibernate.query.Query;

import tneb.ccms.admin.controller.Authentication;
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

@ManagedBean
@ViewScoped
public class YearWiseMonthlyAbstractReport {
	
	private SessionFactory sessionFactory;
	private DataModel dmFilter ;
	List<TmpYearWiseCircle> yearWiseCircles = new ArrayList<>();
	List<TmpYearWiseSections> yearWiseSections = new ArrayList<>();
    List<String> monthHeaders;
    String cirCode;
	StreamedContent excelFile;
	StreamedContent pdfFile;
	String redirectFrom;
	ViewComplaintReportValueBean selectedComplaintId;
	private List<String> yearList = new ArrayList<String>();
	Authentication authentication = new Authentication();
	List<CompDeviceValueBean> devices;
	List<CategoriesValueBean> categories;
	private boolean cameFromInsideReport = false;
	private boolean cameFromInsideSection = false;
	AdminUserValueBean adminUserValueBean;
	private String selectedMonth;
	private String selectedCircleId;
	
	List<ViewComplaintReportValueBean> complaintList = new ArrayList<>();
	String selectedCircleName =null;
	String selectedSectionName = null;
	
	private Map<String, Boolean> futureMonthsMap;
	
	private BigDecimal totalAprilTot = BigDecimal.ZERO;
    private BigDecimal totalAprilCom = BigDecimal.ZERO;
    private BigDecimal totalAprilPen = BigDecimal.ZERO;
    
	private BigDecimal totalMayTot = BigDecimal.ZERO;
    private BigDecimal totalMayCom = BigDecimal.ZERO;
    private BigDecimal totalMayPen = BigDecimal.ZERO;
    
	private BigDecimal totalJuneTot = BigDecimal.ZERO;
    private BigDecimal totalJuneCom = BigDecimal.ZERO;
    private BigDecimal totalJunePen = BigDecimal.ZERO;
    
	private BigDecimal totalJulyTot = BigDecimal.ZERO;
    private BigDecimal totalJulyCom = BigDecimal.ZERO;
    private BigDecimal totalJulyPen = BigDecimal.ZERO;
    
	private BigDecimal totalAugTot = BigDecimal.ZERO;
    private BigDecimal totalAugCom = BigDecimal.ZERO;
    private BigDecimal totalAugPen = BigDecimal.ZERO;
    
	private BigDecimal totalSepTot = BigDecimal.ZERO;
    private BigDecimal totalSepCom = BigDecimal.ZERO;
    private BigDecimal totalSepPen = BigDecimal.ZERO;
    
	private BigDecimal totalOctTot = BigDecimal.ZERO;
    private BigDecimal totalOctCom = BigDecimal.ZERO;
    private BigDecimal totalOctPen = BigDecimal.ZERO;
    
	private BigDecimal totalNovTot = BigDecimal.ZERO;
    private BigDecimal totalNovCom = BigDecimal.ZERO;
    private BigDecimal totalNovPen = BigDecimal.ZERO;
    
	private BigDecimal totalDecTot = BigDecimal.ZERO;
    private BigDecimal totalDecCom = BigDecimal.ZERO;
    private BigDecimal totalDecPen = BigDecimal.ZERO;
    
	private BigDecimal totalJanTot = BigDecimal.ZERO;
    private BigDecimal totalJanCom = BigDecimal.ZERO;
    private BigDecimal totalJanPen = BigDecimal.ZERO;
    
	private BigDecimal totalFebTot = BigDecimal.ZERO;
    private BigDecimal totalFebCom = BigDecimal.ZERO;
    private BigDecimal totalFebPen = BigDecimal.ZERO;
    
	private BigDecimal totalMarTot = BigDecimal.ZERO;
    private BigDecimal totalMarCom = BigDecimal.ZERO;
    private BigDecimal totalMarPen = BigDecimal.ZERO;
    
	private BigDecimal grandTotalRevd = BigDecimal.ZERO;
    private BigDecimal grandTotalComp = BigDecimal.ZERO;
    private BigDecimal grandTotalPend = BigDecimal.ZERO;


		@PostConstruct
		public void init() {
			System.out.println("Initializing YearWiseReportCircle...");
			sessionFactory = HibernateUtil.getSessionFactory();
			monthHeaders = new ArrayList<>();
			dmFilter = new DataModel();
			int currentYear = Year.now().getValue();
	
			for (int year = 2020; year <= currentYear; year++) {
				yearList.add(String.valueOf(year));
			}
	
			loadAllDevicesAndCategories();
	
		}
		
		// REFERSH CIRCLE REPORT
		public void resetIfNeeded() {
		    if (!FacesContext.getCurrentInstance().isPostback() && !cameFromInsideReport ) {
		        yearWiseCircles = null;
		        dmFilter.setComplaintType(null);
		        dmFilter.setDevice(null);
		        dmFilter.setYear(null);
		    }
		    cameFromInsideReport = false;
		}

		
		// REFRESH SECTION REPORT
		public void resetSectionIfNeeded() {
			
		    if (!FacesContext.getCurrentInstance().isPostback() && !cameFromInsideSection ) {
		        yearWiseSections = null;
		        dmFilter.setComplaintType(null);
		        dmFilter.setDevice(null);
		        dmFilter.setYear(null);
		    }
		    cameFromInsideSection = false;
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

		        if (callCenterValueBean != null) {
		            int roleId = callCenterValueBean.getRoleId();

		            if (roleId == 5 || roleId == 7) {
		                devices = orderedDeviceList.stream()
		                        .filter(d -> "M".equals(d.getDeviceCode()))
		                        .collect(Collectors.toList());

		            } else if (roleId == 3) {
		                devices = orderedDeviceList.stream()
		                        .filter(d -> "S".equals(d.getDeviceCode()))
		                        .collect(Collectors.toList());

		            } else {
		                devices = orderedDeviceList;
		            }

		        } else if (adminUserValueBean != null) {
		            int roleId = adminUserValueBean.getRoleId();

		            if (roleId == 10) {
		                devices = orderedDeviceList.stream()
		                        .filter(d -> "M".equals(d.getDeviceCode()))
		                        .collect(Collectors.toList());

		            } else {
		                devices = orderedDeviceList;
		            }

		        } else {
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
		
		public void clearFiltersAndPage() {
			dmFilter = new DataModel();
			yearWiseCircles = new ArrayList<>();			
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
		
		@Transactional
	    public void searchYearWise() {
			
	    updateLoginWiseFilters();
			
	    Session session = null;
	    try {
	        session = sessionFactory.openSession();
	        session.beginTransaction();

	        if(dmFilter.getComplaintType()==null) {
	        	dmFilter.setComplaintType("AL");
	        }
	        if(dmFilter.getDevice()==null) {
	        	dmFilter.setDevice("L");
	        }

	        
	        session.createNativeQuery("BEGIN cat_dev_mon_abst_all(:regionCode,:circleCode,:complaintCode,:device, :year); END;")
			        .setParameter("regionCode", dmFilter.getRegionCode())
			        .setParameter("circleCode", dmFilter.getCircleCode())
	                .setParameter("complaintCode", dmFilter.getComplaintType())
	                .setParameter("device", dmFilter.getDevice())
	                .setParameter("year", dmFilter.getYear())
	                .executeUpdate();

	        session.flush();
	        session.getTransaction().commit();

	        String hql ="SELECT c.REGCODE,c.CIRCODE,"
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
	        		+ "cir.name as CircleName,reg.name as RegionName FROM TMP_CT_DEV_MONABST_CIR c,Circle cir,Region reg "
	        		+ "WHERE cir.id =c.cirCode and reg.id =c.regCode";
	        List<Object[]> results = session.createNativeQuery(hql).getResultList();
	        List<TmpYearWiseCircle> circleList = new ArrayList<>();
	        
	        for (Object[] row : results) {
	        	TmpYearWiseCircle report = new TmpYearWiseCircle();
				report.setRegCode((String) row[0]);
				report.setCirCode((String) row[1]);
				
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
				
				report.setCircleName((String)row[38]);
				report.setRegionName((String)row[39]);
						 

				circleList.add(report);
			}
	        
	        HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
	                .getExternalContext().getSession(false);
	        AdminUserValueBean adminUserValueBean = (AdminUserValueBean) httpsession.getAttribute("sessionAdminValueBean");
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

				circleList = circleList.stream()
					.filter(r -> regionId.contains(Integer.valueOf(r.getRegCode())))
					.filter(c -> circleId.contains(Integer.valueOf(c.getCirCode())))
					.collect(Collectors.toList());
	        }
	        
	        
	        if(dmFilter.getRegionCode().equals("A")) {
	        	yearWiseCircles = circleList;
	        }else {
	        	if(dmFilter.getCircleCode().equals("A")) {
		        	yearWiseCircles = circleList.stream().filter(c -> c.getRegCode().equals(dmFilter.getRegionCode())).collect(Collectors.toList());
	        	}else {
		        	yearWiseCircles = circleList.stream().filter(c -> c.getRegCode().equals(dmFilter.getRegionCode())).filter(cir ->cir.getCirCode().equals(dmFilter.getCircleCode())).collect(Collectors.toList());
	        	}
	        }
	        computeTotals();

	        System.out.println("YEAR WISE CIRCLE List size: " + yearWiseCircles.size());
	        generateMonthHeaders();

	    } catch (Exception e) {
	        e.printStackTrace();
	        System.out.println("Error in database operation");
	    }
	}
		
		@Transactional
		public void getSectionsByCircle(String cirCode,String circleName) {
			this.selectedCircleId=cirCode;
		    Session session = null;
		    try {
		        session = sessionFactory.openSession();
		        session.beginTransaction();
		        
		        if(authentication.getOfficer().getSectionId()!=null) {
		        	Integer sectionId= authentication.getOfficer().getSectionId();
		        	StringBuilder hql = new StringBuilder("SELECT c.CODE FROM SECTION c where c.id= :sectionId");
		        	
		        	@SuppressWarnings("unchecked")
					Query<String> result = session.createNativeQuery(hql.toString());
		        	result.setParameter("sectionId", sectionId);
		        	
		        	String sectionCode =  result.getSingleResult();
		        	dmFilter.setSectionCode(sectionCode);
		        }
		        else {
		        	dmFilter.setSectionCode("A");
		        }

		        session.createNativeQuery("BEGIN cat_dev_mon_abst_secall(:cirCode,:sectionCode,:complaintCode,:device, :year); END;")
		                .setParameter("cirCode", cirCode)
		                .setParameter("sectionCode", dmFilter.getSectionCode())
		                .setParameter("complaintCode", dmFilter.getComplaintType())
		                .setParameter("device", dmFilter.getDevice())
		                .setParameter("year", dmFilter.getYear())
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
		        		+ "cir.name as CircleName,sec.name as SectionName,d.NAME as DivisionName FROM TMP_CT_DEV_MONABST_SEC c,Circle cir,Section sec,Division d "
		        		+ "WHERE cir.id =c.cirCode and sec.id =c.secCode and d.id =sec.DIVISION_ID";
		        List<Object[]> results = session.createNativeQuery(hql).getResultList();

		        yearWiseSections = new ArrayList<>();
		        
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
					
					report.setCircleName((String)row[38]);
					report.setSectionName((String)row[39]);
							 
					report.setDivisionName((String) row[40]);

					yearWiseSections.add(report);
				}

		        System.out.println("YEAR WISE CIRCLE List size: " + yearWiseSections.size());
		        selectedCircleName =circleName;
		        cameFromInsideReport = true;
		        cameFromInsideSection=true;
		        

	            FacesContext.getCurrentInstance().getExternalContext().redirect("yearWiseMonthlySectionAbstract.xhtml");

		    } catch (Exception e) {
		        e.printStackTrace();
		        System.out.println("Error in database operation");
		    }
		}
		
		@Transactional
		public void getSectionReportForOtherThanRegionAndCircle() {			
			
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
			
	        generateMonthHeaders();
			
				
				if(dmFilter.getComplaintType()==null) {
					dmFilter.setComplaintType("AL");
				}
				if(dmFilter.getDevice()==null) {
					dmFilter.setDevice("L");
				}
				if(dmFilter.getYear()==null) {
					dmFilter.setYear("2021");
				}
				
		    Session session = null;
		    try {
		        session = sessionFactory.openSession();
		        session.beginTransaction();

		        session.createNativeQuery("BEGIN cat_dev_mon_abst_secall(:cirCode,:sectionCode,:complaintCode,:device, :year); END;")
		                .setParameter("cirCode", circleCode)
		                .setParameter("sectionCode", dmFilter.getSectionCode())
		                .setParameter("complaintCode", dmFilter.getComplaintType())
		                .setParameter("device", dmFilter.getDevice())
		                .setParameter("year", dmFilter.getYear())
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
		        		+ "s.division_id AS DIVISION_ID, d.name AS DIVISION_NAME, s.sub_division_id AS SUB_DIVISION_ID,s.name as SECTION  "
		        		+ "FROM TMP_CT_DEV_MONABST_SEC c "+
		        		"JOIN SECTION s ON s.id = c.seccode " +
			            "JOIN DIVISION d ON d.id = s.division_id";
		        
		        List<Object[]> results = session.createNativeQuery(hql).getResultList();

		        List<TmpYearWiseSections> sections = new ArrayList<>();
		        
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
					
					report.setDivisionId((String) row[38].toString());
					report.setDivisionName((String) row[39]);
					report.setSubDivisionId((String) row[40].toString());
					report.setSectionName((String) row[41]);
					

					sections.add(report);
				}
		        
		      //SECTION
				if(adminUserValueBean.getRoleId()==1) {
					yearWiseSections = sections.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
							.filter(section -> section.getSecCode().equalsIgnoreCase(sectionCode))
							.collect(Collectors.toList());
				}
				//DIVISION
				else if(adminUserValueBean.getRoleId()==2) {
					yearWiseSections = sections.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
							.filter(sd -> sd.getSubDivisionId().equalsIgnoreCase(subDivisionId))
							.collect(Collectors.toList());
				}
				//SUB DIVISION
				else if(adminUserValueBean.getRoleId()==3) {
					yearWiseSections = sections.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
							.filter(d -> d.getDivisionId().equalsIgnoreCase(divisionId))
							.collect(Collectors.toList());
				}
				//ALL SECTION
				else {
					yearWiseSections = sections.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
							.collect(Collectors.toList());
				}
		        
		        System.out.println("YEAR WISE CIRCLE List size: " + yearWiseSections.size());
		        selectedCircleName =circleName;
		        cameFromInsideSection=true;
		        
		    } catch (Exception e) {
		        e.printStackTrace();
		        System.out.println("Error in database operation");
		    }
			}
		
		
			public void redirectToCircleReport() throws IOException {
				FacesContext.getCurrentInstance().getExternalContext().redirect("yearWiseMonthlyCircleAbstract.xhtml");

			}

			public void redirectToReport() throws IOException {
				if ("circle".equals(redirectFrom)) {
					FacesContext.getCurrentInstance().getExternalContext()
							.redirect("yearWiseMonthlyCircleAbstract.xhtml");
				} else {
					FacesContext.getCurrentInstance().getExternalContext()
							.redirect("yearWiseMonthlySectionAbstract.xhtml");
				}
			}

			public void getMonthlyAbstract() throws IOException {
				this.cameFromInsideReport = true;
				FacesContext.getCurrentInstance().getExternalContext().redirect("yearWiseMonthlyCircleAbstract.xhtml");

			}

			public void redirectFromDateWiseSectionAbstract() throws IOException {
				this.cameFromInsideSection = true;
				FacesContext.getCurrentInstance().getExternalContext().redirect("yearWiseMonthlySectionAbstract.xhtml");
			}
		
		
		
		@Transactional
		public void getComplaintListForSection(String secCode,String sectionName) throws IOException {
			System.out.println("THE SELECTED SECTION CODE IS ============"+secCode);
			
			try (Session session = sessionFactory.openSession()) {
				
				List<String> complaintTypes = new ArrayList<>();
		        List<String> devices = new ArrayList<>();
				
				
				if (dmFilter.getYear() != null) {
					Integer yearInt =Integer.parseInt(dmFilter.getYear());
		            Calendar calendar = Calendar.getInstance();
		            calendar.set(yearInt, Calendar.JANUARY, 1, 0, 0, 0);
		            dmFilter.setFromDate(calendar.getTime());
		            calendar.set(yearInt, Calendar.DECEMBER, 31, 23, 59, 59);
		            dmFilter.setToDate(calendar.getTime());
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
							
				
				Timestamp fromDate = new Timestamp(dmFilter.getFromDate().getTime());
				Timestamp toDate = new Timestamp(dmFilter.getToDate().getTime());
				
				System.out.println("THE FROM DATE COMPLAINT LIST =========="+fromDate);
				System.out.println("THE TO DATE COMPLAINT LIST ============"+toDate);
		
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
	                "TO_CHAR(a.updated_on, 'DD-MM-YYYY-HH24:MI') AS Attended_Date, " +
	                "c.description AS Attended_Remarks, " +
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
	            hql.append(" AND a.complaint_type IN (:complaintTypes)");
	        }

	        if (!devices.isEmpty()) {
	            hql.append(" AND a.device IN (:devices)");
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
	        System.out.println("THE COMPLAINT TYPES ========"+complaintTypes);
	        System.out.println("THE DEVICES -=========="+devices);
			
					
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

	        redirectFrom = "section";
	        
	        FacesContext.getCurrentInstance().getExternalContext().redirect("yearWiseMonthlyComplaintList.xhtml");
			
			}catch(NumberFormatException e){
				System.out.println("ERROR..........."+e);
			}
			
		}
		
		
		@Transactional
		public void getComplaintListForCircle() throws IOException {
			try (Session session = sessionFactory.openSession()) {
				
				if (dmFilter.getYear() != null) {
					Integer yearInt =Integer.parseInt(dmFilter.getYear());
		            Calendar calendar = Calendar.getInstance();
		            calendar.set(yearInt, Calendar.JANUARY, 1, 0, 0, 0);
		            dmFilter.setFromDate(calendar.getTime());
		            calendar.set(yearInt, Calendar.DECEMBER, 31, 23, 59, 59);
		            dmFilter.setToDate(calendar.getTime());
		        }

				
				Timestamp fromDate = new Timestamp(dmFilter.getFromDate().getTime());
				Timestamp toDate = new Timestamp(dmFilter.getToDate().getTime());
				
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
		                "WHERE a.STATUS_ID IN :statusIDs " +
		                "AND a.created_on BETWEEN :fromDate AND :toDate";
				
				Query query = session.createNativeQuery(hql);
				query.setParameter("fromDate", fromDate); 
				query.setParameter("toDate", toDate); 
				query.setParameter("statusIDs", statusIDs);
				
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
			 selectedSectionName =null;
			
			FacesContext.getCurrentInstance().getExternalContext().redirect("yearWiseMonthlyComplaintList.xhtml");
			
			}catch(Exception e){
				System.out.println("ERROR..........."+e);
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
		
		
		
		
		private void computeTotals() {
			System.out.println("THE COMPUTE TOTAL ----- VALUE======="+yearWiseCircles.size());
		    totalAprilTot = yearWiseCircles.stream().map(TmpYearWiseCircle::getTot1).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalAprilCom = yearWiseCircles.stream().map(TmpYearWiseCircle::getCpl1).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalAprilPen = yearWiseCircles.stream().map(TmpYearWiseCircle::getPend1).reduce(BigDecimal.ZERO, BigDecimal::add);

		    totalMayTot = yearWiseCircles.stream().map(TmpYearWiseCircle::getTot2).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalMayCom = yearWiseCircles.stream().map(TmpYearWiseCircle::getCpl2).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalMayPen = yearWiseCircles.stream().map(TmpYearWiseCircle::getPend1).reduce(BigDecimal.ZERO, BigDecimal::add);

		    totalJuneTot = yearWiseCircles.stream().map(TmpYearWiseCircle::getTot3).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalJuneCom = yearWiseCircles.stream().map(TmpYearWiseCircle::getCpl3).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalJunePen = yearWiseCircles.stream().map(TmpYearWiseCircle::getPend3).reduce(BigDecimal.ZERO, BigDecimal::add);

		    totalJulyTot = yearWiseCircles.stream().map(TmpYearWiseCircle::getTot4).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalJulyCom = yearWiseCircles.stream().map(TmpYearWiseCircle::getCpl4).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalJulyPen = yearWiseCircles.stream().map(TmpYearWiseCircle::getPend4).reduce(BigDecimal.ZERO, BigDecimal::add);

		    totalAugTot = yearWiseCircles.stream().map(TmpYearWiseCircle::getTot5).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalAugCom = yearWiseCircles.stream().map(TmpYearWiseCircle::getCpl5).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalAugPen = yearWiseCircles.stream().map(TmpYearWiseCircle::getPend5).reduce(BigDecimal.ZERO, BigDecimal::add);

		    totalSepTot = yearWiseCircles.stream().map(TmpYearWiseCircle::getTot6).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalSepCom = yearWiseCircles.stream().map(TmpYearWiseCircle::getCpl6).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalSepPen = yearWiseCircles.stream().map(TmpYearWiseCircle::getPend6).reduce(BigDecimal.ZERO, BigDecimal::add);

		    totalOctTot = yearWiseCircles.stream().map(TmpYearWiseCircle::getTot7).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalOctCom = yearWiseCircles.stream().map(TmpYearWiseCircle::getCpl7).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalOctPen = yearWiseCircles.stream().map(TmpYearWiseCircle::getPend7).reduce(BigDecimal.ZERO, BigDecimal::add);

		    totalNovTot = yearWiseCircles.stream().map(TmpYearWiseCircle::getTot8).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalNovCom = yearWiseCircles.stream().map(TmpYearWiseCircle::getCpl8).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalNovPen = yearWiseCircles.stream().map(TmpYearWiseCircle::getPend8).reduce(BigDecimal.ZERO, BigDecimal::add);

		    totalDecTot = yearWiseCircles.stream().map(TmpYearWiseCircle::getTot9).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalDecCom = yearWiseCircles.stream().map(TmpYearWiseCircle::getCpl9).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalDecPen = yearWiseCircles.stream().map(TmpYearWiseCircle::getPend9).reduce(BigDecimal.ZERO, BigDecimal::add);
		    
		    totalJanTot = yearWiseCircles.stream().map(TmpYearWiseCircle::getTot10).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalJanCom = yearWiseCircles.stream().map(TmpYearWiseCircle::getCpl10).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalJanPen = yearWiseCircles.stream().map(TmpYearWiseCircle::getPend10).reduce(BigDecimal.ZERO, BigDecimal::add);
		    
		    totalFebTot = yearWiseCircles.stream().map(TmpYearWiseCircle::getTot11).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalFebCom = yearWiseCircles.stream().map(TmpYearWiseCircle::getCpl11).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalFebPen = yearWiseCircles.stream().map(TmpYearWiseCircle::getPend11).reduce(BigDecimal.ZERO, BigDecimal::add);
		    
		    totalMarTot = yearWiseCircles.stream().map(TmpYearWiseCircle::getTot12).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalMarCom = yearWiseCircles.stream().map(TmpYearWiseCircle::getCpl12).reduce(BigDecimal.ZERO, BigDecimal::add);
		    totalMarPen = yearWiseCircles.stream().map(TmpYearWiseCircle::getPend12).reduce(BigDecimal.ZERO, BigDecimal::add);

		    grandTotalRevd = totalAprilTot.add(totalMayTot).add(totalJuneTot).add(totalJulyTot)
                    .add(totalAugTot).add(totalSepTot).add(totalOctTot)
                    .add(totalNovTot).add(totalDecTot).add(totalJanTot)
                    .add(totalFebTot).add(totalMarTot);

			grandTotalComp = totalAprilCom.add(totalMayCom).add(totalJuneCom).add(totalJulyCom)
			                    .add(totalAugCom).add(totalSepCom).add(totalOctCom)
			                    .add(totalNovCom).add(totalDecCom).add(totalJanCom)
			                    .add(totalFebCom).add(totalMarCom);
			
			grandTotalPend = totalAprilPen.add(totalMayPen).add(totalJunePen).add(totalJulyPen)
			                    .add(totalAugPen).add(totalSepPen).add(totalOctPen)
			                    .add(totalNovPen).add(totalDecPen).add(totalJanPen)
			                    .add(totalFebPen).add(totalMarPen);
		}
		
		
		
		private void generateMonthHeaders() {
		    monthHeaders.clear();

		    String selectedYearStr = dmFilter.getYear();
		    int selectedYear;

		    try {
		        selectedYear = Integer.parseInt(selectedYearStr);
		    } catch (NumberFormatException e) {
		        selectedYear = java.time.Year.now().getValue(); 
		    }

		    java.time.Month month = java.time.Month.APRIL;

		    for (int i = 0; i < 12; i++) {
		        int yearToDisplay = selectedYear + (month.getValue() >= java.time.Month.APRIL.getValue() ? 0 : 1); 
		        String header = month.name() + " " + yearToDisplay;
		        monthHeaders.add(header);

		        month = month.plus(1);
		    }
		}
		
		public void exportCirclesToExcel(List<TmpYearWiseCircle> yearWiseCircles) throws IOException {
		    HSSFWorkbook workbook = new HSSFWorkbook();
		    Sheet sheet = workbook.createSheet("Monthly_SplitUp_Abstract_For_A_Given_Year_CircleWise");

		    final int COLUMN_COUNT = 41; 
		    sheet.setColumnWidth(0, 2000);
		    sheet.setColumnWidth(1, 4000);
		    for (int i = 2; i < COLUMN_COUNT; i++) {
		        sheet.setColumnWidth(i, 3000);
		    }
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
		    
		    Row headingRow = sheet.createRow(0);
		    Cell headingCell = headingRow.createCell(0);
		    headingCell.setCellValue("MONTHLY SPLITUP ABSTRACT FOR A GIVEN YEAR - CIRCLE WISE");
		    headingCell.setCellStyle(headingStyle);
		    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 41)); 
		    
		    Row dateRow = sheet.createRow(1);
		    Cell dateCell = dateRow.createCell(0);
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
		   
		    
		    dateCell.setCellValue("YEAR: " + dmFilter.getYear() + "  Complaint Type : " + complaintType  +"  Device :"+device);
		    dateCell.setCellStyle(dateStyle);
		    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 41));

		    Row headerRow1 = sheet.createRow(2);
		    Row headerRow2 = sheet.createRow(3);
		    
		    String year = dmFilter.getYear();
		    int startYear = Integer.parseInt(year);
		    int nextYear = startYear + 1;

		    String[] mainHeaders = {
		        "S.NO", "CIRCLE", "APRIL " + startYear, "MAY " + startYear, "JUNE " + startYear, "JULY " + startYear, "AUGUST " + startYear, "SEPTEMBER " + startYear,
		        "OCTOBER " + startYear, "NOVEMBER " + startYear, "DECEMBER " + startYear, "JANUARY " + nextYear, "FEBRUARY "+ nextYear, "MARCH " + nextYear, "TOTAL"
		    };
		    String[] subHeaders = {"Revd.", "Comp.", "Pend."};

		    int colIndex = 0;
		    for (String mainHeader : mainHeaders) {
		        Cell cell = headerRow1.createCell(colIndex);
		        cell.setCellValue(mainHeader);
		        cell.setCellStyle(headerStyle);

		        if (!mainHeader.equals("S.NO") && !mainHeader.equals("CIRCLE")) {
		            sheet.addMergedRegion(new CellRangeAddress(2, 2, colIndex, colIndex + 2));

		            for (String subHeader : subHeaders) {
		                Cell subCell = headerRow2.createCell(colIndex);
		                subCell.setCellValue(subHeader);
		                subCell.setCellStyle(headerStyle);
		                colIndex++;
		            }
		        } else {
		            colIndex++;
		        }
		    }
		    
		    CellStyle dataCellStyle = workbook.createCellStyle();
		    dataCellStyle.setBorderBottom(BorderStyle.THIN);
		    dataCellStyle.setBorderTop(BorderStyle.THIN);
		    dataCellStyle.setBorderLeft(BorderStyle.THIN);
		    dataCellStyle.setBorderRight(BorderStyle.THIN);
		    dataCellStyle.setAlignment(HorizontalAlignment.LEFT);

		    int rowNum = 4;
		    BigDecimal[] totalSums = new BigDecimal[COLUMN_COUNT];
		    Arrays.fill(totalSums, BigDecimal.ZERO);

		    int serialNumber = 1;
		    for (TmpYearWiseCircle report : yearWiseCircles) {
		        Row row = sheet.createRow(rowNum++);
		        row.createCell(0).setCellValue(serialNumber++);
		        row.createCell(1).setCellValue(report.getCircleName());

		        BigDecimal rowTotSum = BigDecimal.ZERO;
		        BigDecimal rowCplSum = BigDecimal.ZERO;
		        BigDecimal rowPendSum = BigDecimal.ZERO;

		        int dataIndex = 2;
		        BigDecimal[] values = {
		            report.getTot1(), report.getCpl1(), report.getPend1(),
		            report.getTot2(), report.getCpl2(), report.getPend2(),
		            report.getTot3(), report.getCpl3(), report.getPend3(),
		            report.getTot4(), report.getCpl4(), report.getPend4(),
		            report.getTot5(), report.getCpl5(), report.getPend5(),
		            report.getTot6(), report.getCpl6(), report.getPend6(),
		            report.getTot7(), report.getCpl7(), report.getPend7(),
		            report.getTot8(), report.getCpl8(), report.getPend8(),
		            report.getTot9(), report.getCpl9(), report.getPend9(),
		            report.getTot10(), report.getCpl10(), report.getPend10(),
		            report.getTot11(), report.getCpl11(), report.getPend11(),
		            report.getTot12(), report.getCpl12(), report.getPend12()
		        };

		        for (int i = 0; i < values.length; i += 3) { 
		            BigDecimal tot = values[i] != null ? values[i] : BigDecimal.ZERO;
		            BigDecimal cpl = values[i + 1] != null ? values[i + 1] : BigDecimal.ZERO;
		            BigDecimal pend = values[i + 2] != null ? values[i + 2] : BigDecimal.ZERO;

		            setCellValue(row, dataIndex++, tot,dataCellStyle);
		            setCellValue(row, dataIndex++, cpl,dataCellStyle);
		            setCellValue(row, dataIndex++, pend,dataCellStyle);

		            rowTotSum = rowTotSum.add(tot);
		            rowCplSum = rowCplSum.add(cpl);
		            rowPendSum = rowPendSum.add(pend);

		            totalSums[dataIndex - 3] = totalSums[dataIndex - 3].add(tot);
		            totalSums[dataIndex - 2] = totalSums[dataIndex - 2].add(cpl);
		            totalSums[dataIndex - 1] = totalSums[dataIndex - 1].add(pend);
		        }

		        setCellValue(row, dataIndex++, rowTotSum,dataCellStyle);
		        setCellValue(row, dataIndex++, rowCplSum,dataCellStyle);
		        setCellValue(row, dataIndex++, rowPendSum,dataCellStyle);

		        totalSums[38] = totalSums[38].add(rowTotSum);
		        totalSums[39] = totalSums[39].add(rowCplSum);
		        totalSums[40] = totalSums[40].add(rowPendSum);
		    }

		    // Total Row Style
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

		    // Add Total Row
		    Row totalRow = sheet.createRow(rowNum);
		    totalRow.createCell(0).setCellValue("TOTAL");
		    totalRow.getCell(0).setCellStyle(totalRowStyle);
		    totalRow.createCell(1).setCellValue("");
		    totalRow.getCell(1).setCellStyle(totalRowStyle);

		    for (int i = 2; i < COLUMN_COUNT; i++) {
		        setCellValue(totalRow, i, totalSums[i],dataCellStyle); 
		        totalRow.getCell(i).setCellStyle(totalRowStyle);
		    }
		    
		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    workbook.write(outputStream);
		    workbook.close();

		    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		    try {
		        excelFile = DefaultStreamedContent.builder()
		                .name("Monthly_SplitUp_Abstract_For_A_Given_Year_CircleWise.xls")
		                .contentType("application/vnd.ms-excel")
		                .stream(() -> inputStream)
		                .build();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}

		
       //SECTION REPORT TO EXCEL DOWNLOAD
		public void exportSectionsToExcel(List<TmpYearWiseSections> yearWiseSections) throws IOException {
		    HSSFWorkbook workbook = new HSSFWorkbook();
		    Sheet sheet = workbook.createSheet("Monthly_SplitUp_Abstract_For_A_Given_Year_SectionWise");

		    final int COLUMN_COUNT = 42;
		    sheet.setColumnWidth(0, 2000); // SNO
		    sheet.setColumnWidth(1, 4000); // SECTION
		    sheet.setColumnWidth(2, 4000); // DIVISION
		    for (int i = 3; i < COLUMN_COUNT; i++) {  
		        sheet.setColumnWidth(i, 2000);
		    }
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
		    
		    Row headingRow = sheet.createRow(0);
		    Cell headingCell = headingRow.createCell(0);
		    headingCell.setCellValue("MONTHLY SPLITUP ABSTRACT FOR A GIVEN YEAR - SECTION WISE");
		    headingCell.setCellStyle(headingStyle);
		    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 41)); // Fixed: Changed from 42 to 41 (0-indexed)
		    
		    Row dateRow = sheet.createRow(1);
		    Cell dateCell = dateRow.createCell(0);
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
		   
		    
		    dateCell.setCellValue("YEAR: " + dmFilter.getYear() + "  Complaint Type : " + complaintType +"  Device :"+device +"  Circle :"+selectedCircleName);
		    dateCell.setCellStyle(dateStyle);
		    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 41)); // Fixed: Changed from 42 to 41 (0-indexed)

		    Row headerRow1 = sheet.createRow(2);
		    Row headerRow2 = sheet.createRow(3);
		    
		    String year =dmFilter.getYear();
		    int startYear= Integer.parseInt(year);
		    int nextYear= startYear + 1;
		    

		    String[] mainHeaders = {"S.NO", "SECTION", "DIVISION", "APRIL " +startYear, "MAY " +startYear, "JUNE " +startYear, "JULY " +startYear, "AUGUST " +startYear, "SEPTEMBER " +startYear, 
		                            "OCTOBER " +startYear, "NOVEMBER " +startYear, "DECEMBER " +startYear, "JANUARY " +nextYear, "FEBRUARY " +nextYear, "MARCH " +nextYear, "TOTAL"};
		    String[] subHeaders = {"Revd.", "Comp.", "Pend."}; 

		    int colIndex = 0;

		    for (String mainHeader : mainHeaders) {
		        Cell cell = headerRow1.createCell(colIndex);
		        cell.setCellValue(mainHeader);
		        cell.setCellStyle(headerStyle);

		        if (mainHeader.equals("S.NO") || mainHeader.equals("SECTION") || mainHeader.equals("DIVISION")) {
		            sheet.addMergedRegion(new CellRangeAddress(2, 3, colIndex, colIndex));
		            Cell subCell = headerRow2.createCell(colIndex);
		            subCell.setCellStyle(headerStyle);
		            colIndex++;
		        } else {
		            // For month columns, merge horizontally and add sub-headers
		            sheet.addMergedRegion(new CellRangeAddress(2, 2, colIndex, colIndex + 2)); 

		            for (String subHeader : subHeaders) {
		                Cell subCell = headerRow2.createCell(colIndex);
		                subCell.setCellValue(subHeader);
		                subCell.setCellStyle(headerStyle);
		                colIndex++;
		            }
		        }
		    }

		    // Create cell borders for data cells
		    CellStyle dataCellStyle = workbook.createCellStyle();
		    dataCellStyle.setBorderBottom(BorderStyle.THIN);
		    dataCellStyle.setBorderTop(BorderStyle.THIN);
		    dataCellStyle.setBorderLeft(BorderStyle.THIN);
		    dataCellStyle.setBorderRight(BorderStyle.THIN);
		    dataCellStyle.setAlignment(HorizontalAlignment.LEFT);

		    int rowNum = 4;
		    BigDecimal[] totalSums = new BigDecimal[COLUMN_COUNT];  
		    Arrays.fill(totalSums, BigDecimal.ZERO);

		    int serialNumber = 1;
		    for (TmpYearWiseSections report : yearWiseSections) {
		        Row row = sheet.createRow(rowNum++);
		        
		        row.createCell(0).setCellValue(serialNumber++);
		        row.getCell(0).setCellStyle(dataCellStyle);
		        
		        row.createCell(1).setCellValue(report.getSectionName());
		        row.getCell(1).setCellStyle(dataCellStyle);
		        
		        row.createCell(2).setCellValue(report.getDivisionName());
		        row.getCell(2).setCellStyle(dataCellStyle);

		        BigDecimal rowTotSum = BigDecimal.ZERO;
		        BigDecimal rowCplSum = BigDecimal.ZERO;
		        BigDecimal rowPendSum = BigDecimal.ZERO;

		        int dataIndex = 3;

		        BigDecimal[] values = { 
		            report.getTot1(), report.getCpl1(), report.getPend1(),
		            report.getTot2(), report.getCpl2(), report.getPend2(),
		            report.getTot3(), report.getCpl3(), report.getPend3(),
		            report.getTot4(), report.getCpl4(), report.getPend4(),
		            report.getTot5(), report.getCpl5(), report.getPend5(),
		            report.getTot6(), report.getCpl6(), report.getPend6(),
		            report.getTot7(), report.getCpl7(), report.getPend7(),
		            report.getTot8(), report.getCpl8(), report.getPend8(),
		            report.getTot9(), report.getCpl9(), report.getPend9(),
		            report.getTot10(), report.getCpl10(), report.getPend10(),
		            report.getTot11(), report.getCpl11(), report.getPend11(),
		            report.getTot12(), report.getCpl12(), report.getPend12()
		        };

		        for (int i = 0; i < values.length; i += 3) {
		            BigDecimal tot = values[i] != null ? values[i] : BigDecimal.ZERO;
		            BigDecimal cpl = values[i + 1] != null ? values[i + 1] : BigDecimal.ZERO;
		            BigDecimal pend = values[i + 2] != null ? values[i + 2] : BigDecimal.ZERO;

		            setCellValue(row, dataIndex++, tot,dataCellStyle);
		            setCellValue(row, dataIndex++, cpl,dataCellStyle);
		            setCellValue(row, dataIndex++, pend,dataCellStyle);            

		            rowTotSum = rowTotSum.add(tot);
		            rowCplSum = rowCplSum.add(cpl);
		            rowPendSum = rowPendSum.add(pend);

		        // Calculate appropriate index for totalSums array
		            totalSums[dataIndex - 3] = totalSums[dataIndex - 3].add(tot);
		            totalSums[dataIndex - 2] = totalSums[dataIndex - 2].add(cpl);
		            totalSums[dataIndex - 1] = totalSums[dataIndex - 1].add(pend);
		        }

		        setCellValue(row, dataIndex++, rowTotSum,dataCellStyle);
		        setCellValue(row, dataIndex++, rowCplSum,dataCellStyle);
		        setCellValue(row, dataIndex++, rowPendSum,dataCellStyle);

		        // Update the grand total columns (last 3 columns)
		        totalSums[39] = totalSums[39].add(rowTotSum); 
		        totalSums[40] = totalSums[40].add(rowCplSum);
		        totalSums[41] = totalSums[41].add(rowPendSum);
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
		    Cell totalCell = totalRow.createCell(0);
		    totalCell.setCellValue("TOTAL");
		    totalCell.setCellStyle(totalRowStyle);
		    
		    // Merge the first 3 cells of the total row
		    sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 2));
		    totalRow.createCell(1).setCellStyle(totalRowStyle);
		    totalRow.createCell(2).setCellStyle(totalRowStyle);

		    int totalDataIndex = 3;
		    for (int i = 3; i < COLUMN_COUNT; i++) {
		        setCellValue(totalRow, totalDataIndex, totalSums[i],dataCellStyle);
		        totalRow.getCell(totalDataIndex).setCellStyle(totalRowStyle);
		        totalDataIndex++;
		    }

		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    workbook.write(outputStream);
		    workbook.close();

		    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		    try {
		        excelFile = DefaultStreamedContent.builder()
		                .name("Monthly_SplitUp_Abstract_For_A_Given_Year_SectionWise.xls")
		                .contentType("application/vnd.ms-excel")
		                .stream(() -> inputStream)
		                .build();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
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
		public void exportToPdf(List<TmpYearWiseCircle> circleList) throws IOException {
		    final int MONTHS = 12;
		    final int SUB_COLUMNS = 3;
		    final int COLUMN_COUNT = 1 + 1 + (MONTHS * SUB_COLUMNS) + 3;

		    final float SNO_COLUMN_WIDTH = 4f;
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
		    

		    Document document = new Document(PageSize.A2.rotate());
		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    
		    try {
		        PdfWriter.getInstance(document, outputStream);
		        document.open();
		        
		        PdfPTable table = new PdfPTable(COLUMN_COUNT);
		        table.setWidthPercentage(100);
		        table.setSpacingBefore(10f);
		        
		        Paragraph title = new Paragraph("MONTHLY SPLIT UP ABSTRACT FOR GIVEN YEAR - CIRCLE WISE", TITLE_FONT);
		        title.setAlignment(Element.ALIGN_CENTER);
		        document.add(title);
		        
		        Paragraph subTitle = new Paragraph("Complaint Type : " + complaintType + "  Device :" +device +"  YEAR : "+dmFilter.getYear());
		        subTitle.setAlignment(Element.ALIGN_CENTER);
		        subTitle.setSpacingAfter(10);
		        document.add(subTitle);
		        
		        float[] columnWidths = new float[COLUMN_COUNT];
		        columnWidths[0] = SNO_COLUMN_WIDTH;
		        columnWidths[1] = FIRST_COLUMN_WIDTH;
		        for (int i = 2; i < COLUMN_COUNT; i++) {
		            columnWidths[i] = OTHER_COLUMN_WIDTH;
		        }
		        table.setWidths(columnWidths);
		        
		        addMergedHeaderCell(table, "S.No", HEADER_FONT, 1, 2);
		        
		        addMergedHeaderCell(table, "CIRCLE", HEADER_FONT, 1, 2);
		        String[] categoryHeaders = {"APRIL", "MAY", "JUNE", "JULY", "AUGUST", "SEPTEMBER", 
		                "OCTOBER", "NOVEMBER", "DECEMBER", "JANUARY", "FEBRUARY", "MARCH"};
		        
		        int year = Integer.parseInt(dmFilter.getYear());
		        for (int i = 0; i < categoryHeaders.length; i++) {
		            int displayYear = (i < 9) ? year : (year + 1); 
		            
		            addMergedHeaderCell(table, categoryHeaders[i] + " " + displayYear, HEADER_FONT, 3, 1);
		        }
		        addMergedHeaderCell(table, "TOTAL", HEADER_FONT, 3, 1);

		        
		        // SUB HEADER
		        String[] subHeaders = {"Revd.", "Comp.", "Pend."};
		        for (int i = 0; i < MONTHS + 1; i++) { 
		            for (String subHeader : subHeaders) {
		                table.addCell(createHeaderCell(subHeader, HEADER_FONT));
		            }
		        }
		        BigDecimal grandTot = BigDecimal.ZERO;
		        BigDecimal grandTotCpl = BigDecimal.ZERO;
		        BigDecimal grandTotPend = BigDecimal.ZERO;
		        
		        int serialNumber = 1;
		        for (TmpYearWiseCircle report : circleList) {
		            table.addCell(createDataCell(String.valueOf(serialNumber++), DATA_FONT, Element.ALIGN_CENTER));
		            table.addCell(createDataCell(report.getCircleName(), DATA_FONT, Element.ALIGN_LEFT));
		            
		            addMonthlyData(table, report.getTot1(), report.getCpl1(), report.getPend1(), DATA_FONT);
		            addMonthlyData(table, report.getTot2(), report.getCpl2(), report.getPend2(), DATA_FONT);                
		            addMonthlyData(table, report.getTot3(), report.getCpl3(), report.getPend3(), DATA_FONT);                
		            addMonthlyData(table, report.getTot4(), report.getCpl4(), report.getPend4(), DATA_FONT);                
		            addMonthlyData(table, report.getTot5(), report.getCpl5(), report.getPend5(), DATA_FONT);                
		            
		            addMonthlyData(table, report.getTot6(), report.getCpl6(), report.getPend6(), DATA_FONT);
		            addMonthlyData(table, report.getTot7(), report.getCpl7(), report.getPend7(), DATA_FONT);
		            addMonthlyData(table, report.getTot8(), report.getCpl8(), report.getPend8(), DATA_FONT);
		            addMonthlyData(table, report.getTot9(), report.getCpl9(), report.getPend9(), DATA_FONT);
		            addMonthlyData(table, report.getTot10(), report.getCpl10(), report.getPend10(), DATA_FONT);
		            addMonthlyData(table, report.getTot11(), report.getCpl11(), report.getPend11(), DATA_FONT);
		            addMonthlyData(table, report.getTot12(), report.getCpl12(), report.getPend12(), DATA_FONT);

		            BigDecimal total = report.getTot1().add(report.getTot2()).add(report.getTot3()).add(report.getTot4())
		                    .add(report.getTot5()).add(report.getTot6()).add(report.getTot7()).add(report.getTot8())
		                    .add(report.getTot9()).add(report.getTot10()).add(report.getTot11()).add(report.getTot12());
		            
		            BigDecimal totalCpl = report.getCpl1().add(report.getCpl2()).add(report.getCpl3()).add(report.getCpl4())
		                    .add(report.getCpl5()).add(report.getCpl6()).add(report.getCpl7()).add(report.getCpl8())
		                    .add(report.getCpl9()).add(report.getCpl10()).add(report.getCpl11()).add(report.getCpl12());
		            
		            BigDecimal pend = total.subtract(totalCpl); 
		                 
		            grandTot = grandTot.add(total);
		            grandTotCpl = grandTotCpl.add(totalCpl);
		            grandTotPend = grandTotPend.add(pend);

		            addMonthlyData(table, total, totalCpl, pend, TOTAL_FONT);
		        }
		        
		        // FOOTER ROW DISPLAYING GRAND TOTALS
		        PdfPCell footerLabel = new PdfPCell(new Phrase(" TOTAL", TOTAL_FONT));
		        footerLabel.setBackgroundColor(BaseColor.LIGHT_GRAY);
		        footerLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
		        footerLabel.setColspan(2); 
		        table.addCell(footerLabel);
		        
		        // Add monthly totals for the footer row
		        addMonthlyData(table,
		            circleList.stream().map(r->r.getTot1()).reduce(BigDecimal.ZERO, BigDecimal::add),
		            circleList.stream().map(r->r.getCpl1()).reduce(BigDecimal.ZERO, BigDecimal::add),
		            circleList.stream().map(r->r.getPend1()).reduce(BigDecimal.ZERO, BigDecimal::add),
		            TOTAL_FONT);
		        
		        addMonthlyData(table,
		                circleList.stream().map(r->r.getTot2()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                circleList.stream().map(r->r.getCpl2()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                circleList.stream().map(r->r.getPend2()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                TOTAL_FONT);
		        
		        addMonthlyData(table,
		                circleList.stream().map(r->r.getTot3()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                circleList.stream().map(r->r.getCpl3()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                circleList.stream().map(r->r.getPend3()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                TOTAL_FONT);
		        
		        addMonthlyData(table,
		                circleList.stream().map(r->r.getTot4()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                circleList.stream().map(r->r.getCpl4()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                circleList.stream().map(r->r.getPend4()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                TOTAL_FONT);
		        
		        addMonthlyData(table,
		                circleList.stream().map(r->r.getTot5()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                circleList.stream().map(r->r.getCpl5()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                circleList.stream().map(r->r.getPend5()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                TOTAL_FONT);
		        
		        addMonthlyData(table,
		                circleList.stream().map(r->r.getTot6()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                circleList.stream().map(r->r.getCpl6()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                circleList.stream().map(r->r.getPend6()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                TOTAL_FONT);
		        
		        addMonthlyData(table,
		                circleList.stream().map(r->r.getTot7()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                circleList.stream().map(r->r.getCpl7()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                circleList.stream().map(r->r.getPend7()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                TOTAL_FONT);
		        
		        addMonthlyData(table,
		                circleList.stream().map(r->r.getTot8()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                circleList.stream().map(r->r.getCpl8()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                circleList.stream().map(r->r.getPend8()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                TOTAL_FONT);
		        
		        addMonthlyData(table,
		                circleList.stream().map(r->r.getTot9()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                circleList.stream().map(r->r.getCpl9()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                circleList.stream().map(r->r.getPend9()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                TOTAL_FONT);
		        
		        addMonthlyData(table,
		                circleList.stream().map(r->r.getTot10()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                circleList.stream().map(r->r.getCpl10()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                circleList.stream().map(r->r.getPend10()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                TOTAL_FONT);
		        
		        addMonthlyData(table,
		                circleList.stream().map(r->r.getTot11()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                circleList.stream().map(r->r.getCpl11()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                circleList.stream().map(r->r.getPend11()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                TOTAL_FONT);
		        
		        addMonthlyData(table,
		                circleList.stream().map(r->r.getTot12()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                circleList.stream().map(r->r.getCpl12()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                circleList.stream().map(r->r.getPend12()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                TOTAL_FONT);
		        
		        // Add the final grand total column
		        addMonthlyData(table, grandTot, grandTotCpl, grandTotPend, TOTAL_FONT);

		        document.add(table);
		        document.close();
		        
		        pdfFile = DefaultStreamedContent.builder()
		                .contentType("application/pdf")
		                .name("Monthly_SplitUp_Abstract_For_A_Given_Year_CircleWise.pdf")
		                .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
		                .build();
		    } catch(Exception e) {
		        e.printStackTrace();
		    }
		}

	
		 
		 private PdfPCell createHeaderCell(String text, Font font) {
			    PdfPCell cell = new PdfPCell(new Phrase(text, font));
			    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
			    cell.setBorderWidth(1f);
			    return cell;
			}
		 
		 public void exportSectionsToPdf(List<TmpYearWiseSections> sectionList) throws IOException {
			    final int MONTHS = 12;
			    final int SUB_COLUMNS = 3;
			    final int COLUMN_COUNT =  1+ 2 + (MONTHS * SUB_COLUMNS) + 3; 
			    
			    final float SNO_COLUMN_WIDTH = 4f;
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
			    

			    Document document = new Document(PageSize.A2.rotate());
			    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			    
			    try {
			        PdfWriter.getInstance(document, outputStream);
			        document.open();
			        
			        PdfPTable table = new PdfPTable(COLUMN_COUNT);
			        table.setWidthPercentage(100);
			        table.setSpacingBefore(10f);
			        
			        Paragraph title = new Paragraph("MONTHLY SPLIT UP ABSTRACT FOR GIVEN YEAR - SECTION WISE", TITLE_FONT);
			        title.setAlignment(Element.ALIGN_CENTER);
			        document.add(title);
			        
			        Paragraph subTitle = new Paragraph("CIRCLE :"+selectedCircleName +"  Complaint Type : " + complaintType + "  Device :" +device+"  YEAR : "+dmFilter.getYear());
			        subTitle.setAlignment(Element.ALIGN_CENTER);
			        subTitle.setSpacingAfter(10);
			        document.add(subTitle);
			        
			        float[] columnWidths = new float[COLUMN_COUNT];
			        columnWidths[0] = SNO_COLUMN_WIDTH;
			        columnWidths[1] = FIRST_COLUMN_WIDTH;
			        columnWidths[2] = FIRST_COLUMN_WIDTH;
			        for (int i = 3; i < COLUMN_COUNT; i++) {
			            columnWidths[i] = OTHER_COLUMN_WIDTH;
			        }
			        table.setWidths(columnWidths);
			        addMergedHeaderCell(table, "S.NO", HEADER_FONT, 1, 2);
			        addMergedHeaderCell(table, "SECTION", HEADER_FONT, 1, 2);
			        addMergedHeaderCell(table, "DIVISION", HEADER_FONT, 1, 2);
			        String[] categoryHeaders = {"APRIL", "MAY", "JUNE", "JULY", "AUGUST", "SEPTEMBER", 
                            "OCTOBER", "NOVEMBER", "DECEMBER", "JANUARY", "FEBRUARY", "MARCH"};
			        int year =Integer.parseInt(dmFilter.getYear());
			        for (int i = 0; i < categoryHeaders.length; i++) {
		                int displayYear = (i < 9) ? year : (year + 1); 
		                
		                addMergedHeaderCell(table, categoryHeaders[i] + " " + displayYear, HEADER_FONT, 3, 1);

		            }
			        addMergedHeaderCell(table, "TOTAL", HEADER_FONT, 3, 1);

			        
			        
			      //SUB HEADER
			        String[] subHeaders = {"Revd.", "Comp.", "Pend."};
			        for (int i = 0; i < MONTHS + 1; i++) { 
			            for (String subHeader : subHeaders) {
			            	table.addCell(createHeaderCell(subHeader, HEADER_FONT));
			            }
			        }
			        BigDecimal grandTot = BigDecimal.ZERO;
			        BigDecimal grandTotCpl =BigDecimal.ZERO;
			        BigDecimal grandTotPend = BigDecimal.ZERO;
			       
			        int serialNumber = 1;
			        for (TmpYearWiseSections report : sectionList) {
			        	table.addCell(createDataCell(String.valueOf(serialNumber++), DATA_FONT, Element.ALIGN_CENTER));
			            table.addCell(createDataCell(report.getSectionName(), DATA_FONT, Element.ALIGN_LEFT));
			            table.addCell(createDataCell(report.getDivisionName(), DATA_FONT, Element.ALIGN_LEFT));
			            
			            addMonthlyData(table, report.getTot1(), report.getCpl1(), report.getPend1(), DATA_FONT);
			            addMonthlyData(table, report.getTot2(), report.getCpl2(), report.getPend2(), DATA_FONT);			            
			            addMonthlyData(table, report.getTot3(), report.getCpl3(), report.getPend3(), DATA_FONT);			            
			            addMonthlyData(table, report.getTot4(), report.getCpl4(), report.getPend4(), DATA_FONT);			            
			            addMonthlyData(table, report.getTot5(), report.getCpl5(), report.getPend5(), DATA_FONT);			            
			            
			            addMonthlyData(table, report.getTot6(), report.getCpl6(), report.getPend6(), DATA_FONT);
			            addMonthlyData(table, report.getTot7(), report.getCpl7(), report.getPend7(), DATA_FONT);
			            addMonthlyData(table, report.getTot8(), report.getCpl8(), report.getPend8(), DATA_FONT);
			            addMonthlyData(table, report.getTot9(), report.getCpl9(), report.getPend9(), DATA_FONT);
			            addMonthlyData(table, report.getTot10(), report.getCpl10(), report.getPend10(), DATA_FONT);
			            addMonthlyData(table, report.getTot11(), report.getCpl11(), report.getPend11(), DATA_FONT);
			            addMonthlyData(table, report.getTot12(), report.getCpl12(), report.getPend12(), DATA_FONT);

			            BigDecimal total = report.getTot1().add(report.getTot2()).add(report.getTot3()).add(report.getTot4())
								.add(report.getTot5()).add(report.getTot6()).add(report.getTot7()).add(report.getTot8())
								.add(report.getTot9()).add(report.getTot10()).add(report.getTot11()).add(report.getTot12());
			            
			            BigDecimal totalCpl = report.getCpl1().add(report.getCpl2()).add(report.getCpl3()).add(report.getCpl4())
								.add(report.getCpl5()).add(report.getCpl6()).add(report.getCpl7()).add(report.getCpl8())
								.add(report.getCpl9()).add(report.getCpl10()).add(report.getCpl11()).add(report.getCpl12());
						
			            BigDecimal pend = report.getPend1().add(report.getPend2()).add(report.getPend3()).add(report.getPend4())
								.add(report.getPend5()).add(report.getPend6()).add(report.getPend7()).add(report.getPend8())
								.add(report.getPend9()).add(report.getPend10()).add(report.getPend11()).add(report.getPend12());					 
						 
				            grandTot = grandTot.add(total);
				            grandTotCpl =grandTotCpl.add(totalCpl);
				            grandTotPend = grandTotPend.add(pend);


			            
			            addMonthlyData(table,total, totalCpl,pend,TOTAL_FONT);
			        }
			        PdfPCell footerLabel = new PdfPCell(new Phrase("TOTAL", TOTAL_FONT));
			        footerLabel.setBackgroundColor(BaseColor.LIGHT_GRAY);
			        footerLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
			        footerLabel.setColspan(3);
			        table.addCell(footerLabel);

			        addMonthlyData(table,
				            sectionList.stream().map(r->r.getTot1()).reduce(BigDecimal.ZERO, BigDecimal::add),
				            sectionList.stream().map(r->r.getCpl1()).reduce(BigDecimal.ZERO, BigDecimal::add),
				            sectionList.stream().map(r->r.getPend1()).reduce(BigDecimal.ZERO, BigDecimal::add),
				            TOTAL_FONT);
				        
				        addMonthlyData(table,
				                sectionList.stream().map(r->r.getTot2()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                sectionList.stream().map(r->r.getCpl2()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                sectionList.stream().map(r->r.getPend2()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                TOTAL_FONT);
				        
				        addMonthlyData(table,
				                sectionList.stream().map(r->r.getTot3()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                sectionList.stream().map(r->r.getCpl3()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                sectionList.stream().map(r->r.getPend3()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                TOTAL_FONT);
				        
				        addMonthlyData(table,
				                sectionList.stream().map(r->r.getTot4()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                sectionList.stream().map(r->r.getCpl4()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                sectionList.stream().map(r->r.getPend4()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                TOTAL_FONT);
				        
				        addMonthlyData(table,
				                sectionList.stream().map(r->r.getTot5()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                sectionList.stream().map(r->r.getCpl5()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                sectionList.stream().map(r->r.getPend5()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                TOTAL_FONT);
				        
				        addMonthlyData(table,
				                sectionList.stream().map(r->r.getTot6()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                sectionList.stream().map(r->r.getCpl6()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                sectionList.stream().map(r->r.getPend6()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                TOTAL_FONT);
				        
				        addMonthlyData(table,
				                sectionList.stream().map(r->r.getTot7()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                sectionList.stream().map(r->r.getCpl7()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                sectionList.stream().map(r->r.getPend7()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                TOTAL_FONT);
				        
				        addMonthlyData(table,
				                sectionList.stream().map(r->r.getTot8()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                sectionList.stream().map(r->r.getCpl8()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                sectionList.stream().map(r->r.getPend8()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                TOTAL_FONT);
				        
				        addMonthlyData(table,
				                sectionList.stream().map(r->r.getTot9()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                sectionList.stream().map(r->r.getCpl9()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                sectionList.stream().map(r->r.getPend9()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                TOTAL_FONT);
				        
				        addMonthlyData(table,
				                sectionList.stream().map(r->r.getTot10()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                sectionList.stream().map(r->r.getCpl10()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                sectionList.stream().map(r->r.getPend10()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                TOTAL_FONT);
				        
				        addMonthlyData(table,
				                sectionList.stream().map(r->r.getTot11()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                sectionList.stream().map(r->r.getCpl11()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                sectionList.stream().map(r->r.getPend11()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                TOTAL_FONT);
				        
				        addMonthlyData(table,
				                sectionList.stream().map(r->r.getTot12()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                sectionList.stream().map(r->r.getCpl12()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                sectionList.stream().map(r->r.getPend12()).reduce(BigDecimal.ZERO, BigDecimal::add),
				                TOTAL_FONT);
				        
				       
				        
				       
				        addMonthlyData(table, grandTot, grandTotCpl, grandTotPend, TOTAL_FONT);


			        document.add(table);
			        document.close();
			        
			        pdfFile = DefaultStreamedContent.builder()
				            .contentType("application/pdf")
				            .name("Monthly_SplitUp_Abstract_For_A_Given_Year_SectionWise.pdf")
				            .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
				            .build();
			        
			    
			    
			    }catch(Exception e) {
			    	e.printStackTrace();
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

			private void addMonthlyData(PdfPTable table, BigDecimal revd, BigDecimal cpl, BigDecimal pend, Font font) {
			    table.addCell(createDataCell(revd.toString(), font, Element.ALIGN_CENTER));
			    table.addCell(createDataCell(cpl.toString(), font, Element.ALIGN_CENTER));
			    table.addCell(createDataCell(pend.toString(), font, Element.ALIGN_CENTER));
			}


		public void exportComplaintListToExcel(List<ViewComplaintReportValueBean> complaintList) throws IOException {
			  HSSFWorkbook workbook = new HSSFWorkbook();
			  Sheet sheet = workbook.createSheet("Monthly_SplitUp_Abstract_For_A_Given_Year -"+dmFilter.getYear());
			  
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
			                .name("Month_wise_Abstract_ComplaintList_Report.xls")
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
			        Paragraph title = new Paragraph("MONTHLY ABSTRACT FOR YEAR COMPLAINT LIST", titleFont);
			        title.setAlignment(Element.ALIGN_CENTER);
			        title.setSpacingAfter(10);
			        document.add(title);
			        
			        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY);
			        Paragraph subTitle = new Paragraph("Circle: " + selectedCircleName + "  |  Section: " + selectedSectionName + "  |  YEAR: " + dmFilter.getYear(), subtitleFont);
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
			            .name("Monthly_Abstract_For_Given_Year_Complaints.pdf")
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
			
			
			
			public Map<String, Boolean> getFutureMonthsMap() {
			    if (futureMonthsMap == null) {
			        futureMonthsMap = new HashMap<>();
			        for (String monthHeader : monthHeaders) {
			            futureMonthsMap.put(monthHeader, isFutureMonth(monthHeader));
			        }
			    }
			    return futureMonthsMap;
			}

			public boolean isFutureMonth(String monthHeader) {
			    try {
			        Date monthDate = getDateFromMonthHeader(monthHeader);
			        Date currentDate = new Date();
			        
			        Calendar monthCal = Calendar.getInstance();
			        monthCal.setTime(monthDate);
			        
			        Calendar currentCal = Calendar.getInstance();
			        currentCal.setTime(currentDate);
			        
			        boolean future = (monthCal.get(Calendar.YEAR) > currentCal.get(Calendar.YEAR)) ||
			               (monthCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) && 
			                monthCal.get(Calendar.MONTH) > currentCal.get(Calendar.MONTH));
			        
			        if (future) {
			            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
			                    "Error", "Cannot select future month/year");
			            FacesContext.getCurrentInstance().addMessage(null, message);
			        }
			        return future;
			    } catch (Exception e) {
			        return false;
			    }
			}

			public Date getDateFromMonthHeader(String monthYearString) {
			    try {
			        SimpleDateFormat formatter = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
			        Date parsedDate = formatter.parse(monthYearString);
			        
			        Calendar calendar = Calendar.getInstance();
			        calendar.setTime(parsedDate);
			        
			        calendar.set(Calendar.DAY_OF_MONTH, 1);
			        return calendar.getTime();
			        
			    } catch (ParseException e) {
			        e.printStackTrace();
			    }
			    return null;
			}
			
		
		
	    public DataModel getDmFilter() {
			return dmFilter;
		}

		public void setDmFilter(DataModel dmFilter) {
			this.dmFilter = dmFilter;
		}
		
	    public List<TmpYearWiseCircle> getYearWiseCircles() {
			return yearWiseCircles;
		}

		public void setYearWiseCircles(List<TmpYearWiseCircle> yearWiseCircles) {
			this.yearWiseCircles = yearWiseCircles;
		}
		public List<TmpYearWiseSections> getYearWiseSections() {
			return yearWiseSections;
		}

		public void setYearWiseSections(List<TmpYearWiseSections> yearWiseSections) {
			this.yearWiseSections = yearWiseSections;
		}

		public List<String> getMonthHeaders() {
	        return monthHeaders;
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
	public StreamedContent getPdfFile() {
		return pdfFile;
	}

	public void setPdfFile(StreamedContent pdfFile) {
		this.pdfFile = pdfFile;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public String getCirCode() {
		return cirCode;
	}

	public void setCirCode(String cirCode) {
		this.cirCode = cirCode;
	}

	public BigDecimal getTotalAprilTot() {
		return totalAprilTot;
	}

	public void setTotalAprilTot(BigDecimal totalAprilTot) {
		this.totalAprilTot = totalAprilTot;
	}

	public BigDecimal getTotalAprilCom() {
		return totalAprilCom;
	}

	public void setTotalAprilCom(BigDecimal totalAprilCom) {
		this.totalAprilCom = totalAprilCom;
	}

	public BigDecimal getTotalAprilPen() {
		return totalAprilPen;
	}

	public void setTotalAprilPen(BigDecimal totalAprilPen) {
		this.totalAprilPen = totalAprilPen;
	}

	public BigDecimal getTotalMayTot() {
		return totalMayTot;
	}

	public void setTotalMayTot(BigDecimal totalMayTot) {
		this.totalMayTot = totalMayTot;
	}

	public BigDecimal getTotalMayCom() {
		return totalMayCom;
	}

	public void setTotalMayCom(BigDecimal totalMayCom) {
		this.totalMayCom = totalMayCom;
	}

	public BigDecimal getTotalMayPen() {
		return totalMayPen;
	}

	public void setTotalMayPen(BigDecimal totalMayPen) {
		this.totalMayPen = totalMayPen;
	}

	public BigDecimal getTotalJuneTot() {
		return totalJuneTot;
	}

	public void setTotalJuneTot(BigDecimal totalJuneTot) {
		this.totalJuneTot = totalJuneTot;
	}

	public BigDecimal getTotalJuneCom() {
		return totalJuneCom;
	}

	public void setTotalJuneCom(BigDecimal totalJuneCom) {
		this.totalJuneCom = totalJuneCom;
	}

	public BigDecimal getTotalJunePen() {
		return totalJunePen;
	}

	public void setTotalJunePen(BigDecimal totalJunePen) {
		this.totalJunePen = totalJunePen;
	}

	public BigDecimal getTotalJulyTot() {
		return totalJulyTot;
	}

	public void setTotalJulyTot(BigDecimal totalJulyTot) {
		this.totalJulyTot = totalJulyTot;
	}

	public BigDecimal getTotalJulyCom() {
		return totalJulyCom;
	}

	public void setTotalJulyCom(BigDecimal totalJulyCom) {
		this.totalJulyCom = totalJulyCom;
	}

	public BigDecimal getTotalJulyPen() {
		return totalJulyPen;
	}

	public void setTotalJulyPen(BigDecimal totalJulyPen) {
		this.totalJulyPen = totalJulyPen;
	}

	public BigDecimal getTotalAugTot() {
		return totalAugTot;
	}

	public void setTotalAugTot(BigDecimal totalAugTot) {
		this.totalAugTot = totalAugTot;
	}

	public BigDecimal getTotalAugCom() {
		return totalAugCom;
	}

	public void setTotalAugCom(BigDecimal totalAugCom) {
		this.totalAugCom = totalAugCom;
	}

	public BigDecimal getTotalAugPen() {
		return totalAugPen;
	}

	public void setTotalAugPen(BigDecimal totalAugPen) {
		this.totalAugPen = totalAugPen;
	}

	public BigDecimal getTotalSepTot() {
		return totalSepTot;
	}

	public void setTotalSepTot(BigDecimal totalSepTot) {
		this.totalSepTot = totalSepTot;
	}

	public BigDecimal getTotalSepCom() {
		return totalSepCom;
	}

	public void setTotalSepCom(BigDecimal totalSepCom) {
		this.totalSepCom = totalSepCom;
	}

	public BigDecimal getTotalSepPen() {
		return totalSepPen;
	}

	public void setTotalSepPen(BigDecimal totalSepPen) {
		this.totalSepPen = totalSepPen;
	}

	public BigDecimal getTotalOctTot() {
		return totalOctTot;
	}

	public void setTotalOctTot(BigDecimal totalOctTot) {
		this.totalOctTot = totalOctTot;
	}

	public BigDecimal getTotalOctCom() {
		return totalOctCom;
	}

	public void setTotalOctCom(BigDecimal totalOctCom) {
		this.totalOctCom = totalOctCom;
	}

	public BigDecimal getTotalOctPen() {
		return totalOctPen;
	}

	public void setTotalOctPen(BigDecimal totalOctPen) {
		this.totalOctPen = totalOctPen;
	}

	public BigDecimal getTotalNovTot() {
		return totalNovTot;
	}

	public void setTotalNovTot(BigDecimal totalNovTot) {
		this.totalNovTot = totalNovTot;
	}

	public BigDecimal getTotalNovCom() {
		return totalNovCom;
	}

	public void setTotalNovCom(BigDecimal totalNovCom) {
		this.totalNovCom = totalNovCom;
	}

	public BigDecimal getTotalNovPen() {
		return totalNovPen;
	}

	public void setTotalNovPen(BigDecimal totalNovPen) {
		this.totalNovPen = totalNovPen;
	}

	public BigDecimal getTotalDecTot() {
		return totalDecTot;
	}

	public void setTotalDecTot(BigDecimal totalDecTot) {
		this.totalDecTot = totalDecTot;
	}

	public BigDecimal getTotalDecCom() {
		return totalDecCom;
	}

	public void setTotalDecCom(BigDecimal totalDecCom) {
		this.totalDecCom = totalDecCom;
	}

	public BigDecimal getTotalDecPen() {
		return totalDecPen;
	}

	public void setTotalDecPen(BigDecimal totalDecPen) {
		this.totalDecPen = totalDecPen;
	}

	public BigDecimal getTotalJanTot() {
		return totalJanTot;
	}

	public void setTotalJanTot(BigDecimal totalJanTot) {
		this.totalJanTot = totalJanTot;
	}

	public BigDecimal getTotalJanCom() {
		return totalJanCom;
	}

	public void setTotalJanCom(BigDecimal totalJanCom) {
		this.totalJanCom = totalJanCom;
	}

	public BigDecimal getTotalJanPen() {
		return totalJanPen;
	}

	public void setTotalJanPen(BigDecimal totalJanPen) {
		this.totalJanPen = totalJanPen;
	}

	public BigDecimal getTotalFebTot() {
		return totalFebTot;
	}

	public void setTotalFebTot(BigDecimal totalFebTot) {
		this.totalFebTot = totalFebTot;
	}

	public BigDecimal getTotalFebCom() {
		return totalFebCom;
	}

	public void setTotalFebCom(BigDecimal totalFebCom) {
		this.totalFebCom = totalFebCom;
	}

	public BigDecimal getTotalFebPen() {
		return totalFebPen;
	}

	public void setTotalFebPen(BigDecimal totalFebPen) {
		this.totalFebPen = totalFebPen;
	}

	public BigDecimal getTotalMarTot() {
		return totalMarTot;
	}

	public void setTotalMarTot(BigDecimal totalMarTot) {
		this.totalMarTot = totalMarTot;
	}

	public BigDecimal getTotalMarCom() {
		return totalMarCom;
	}

	public void setTotalMarCom(BigDecimal totalMarCom) {
		this.totalMarCom = totalMarCom;
	}

	public BigDecimal getTotalMarPen() {
		return totalMarPen;
	}

	public void setTotalMarPen(BigDecimal totalMarPen) {
		this.totalMarPen = totalMarPen;
	}

	public BigDecimal getGrandTotalRevd() {
		return grandTotalRevd;
	}

	public void setGrandTotalRevd(BigDecimal grandTotalRevd) {
		this.grandTotalRevd = grandTotalRevd;
	}

	public BigDecimal getGrandTotalComp() {
		return grandTotalComp;
	}

	public void setGrandTotalComp(BigDecimal grandTotalComp) {
		this.grandTotalComp = grandTotalComp;
	}

	public BigDecimal getGrandTotalPend() {
		return grandTotalPend;
	}

	public void setGrandTotalPend(BigDecimal grandTotalPend) {
		this.grandTotalPend = grandTotalPend;
	}

	public void setMonthHeaders(List<String> monthHeaders) {
		this.monthHeaders = monthHeaders;
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


	public Authentication getAuthentication() {
		return authentication;
	}

	public void setAuthentication(Authentication authentication) {
		this.authentication = authentication;
	}

	public List<String> getYearList() {
		return yearList;
	}

	public void setYearList(List<String> yearList) {
		this.yearList = yearList;
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

	public AdminUserValueBean getAdminUserValueBean() {
		return adminUserValueBean;
	}

	public void setAdminUserValueBean(AdminUserValueBean adminUserValueBean) {
		this.adminUserValueBean = adminUserValueBean;
	}

	public String getSelectedMonth() {
		return selectedMonth;
	}

	public void setSelectedMonth(String selectedMonth) {
		this.selectedMonth = selectedMonth;
	}

	public String getSelectedCircleId() {
		return selectedCircleId;
	}

	public void setSelectedCircleId(String selectedCircleId) {
		this.selectedCircleId = selectedCircleId;
	}

	public void setFutureMonthsMap(Map<String, Boolean> futureMonthsMap) {
		this.futureMonthsMap = futureMonthsMap;
	}


	
	
}
