package tneb.ccms.admin.controller.reportController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
import com.itextpdf.text.log.SysoCounter;
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
import tneb.ccms.admin.valuebeans.TmpCtCirBean;
import tneb.ccms.admin.valuebeans.TmpCtSecBean;
import tneb.ccms.admin.valuebeans.ViewComplaintReportValueBean;

import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;

@Named
@ViewScoped
public class TopPendingSectionReport implements Serializable {

	private static final long serialVersionUID = 1L;
	private SessionFactory sessionFactory;
	DataModel dmFilter;
	List<TmpCtSecBean> reports = new ArrayList<>();
	private boolean initialized = false;
	 private boolean cameFromInsideReport = false;
	 private boolean cameFromInsideSection=false;
	StreamedContent excelFile;
	StreamedContent pdfFile;
	List<ViewComplaintReportValueBean> complaintList = new ArrayList<>();
	String selectedCircleName =null;
	String selectedSectionName = null;
	ViewComplaintReportValueBean selectedComplaintId;
	private Date currentDate = new Date();
	private String currentYear = String.valueOf(Year.now().getValue());
	List<CompDeviceValueBean> devices;
	List<CategoriesValueBean> categories;




	@PostConstruct
	public void init() {
		System.out.println("Initializing Top Pending Section Abstract Report...");
		sessionFactory = HibernateUtil.getSessionFactory();
		dmFilter = new DataModel();
		loadAllDevicesAndCategories();
	}
	
	// REFERSH SECTION REPORT
	public void resetIfNeeded() {
	    if (!FacesContext.getCurrentInstance().isPostback() && !cameFromInsideReport) {
	        reports = null;
	        dmFilter.setFromDate(null);
	        dmFilter.setToDate(null);
	    }
	    cameFromInsideReport = false;
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
	
	
	public void clearFilterAndPage() {
		dmFilter = new DataModel();
		reports = new ArrayList<>();
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
			                dmFilter.setDevice("M");
			                break;
			            }

			         // CIRCLE AGENT
			            case 7: {
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
			                    	@SuppressWarnings("unchecked")
									List<Integer> regionId =session.createQuery(
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
			public void searchTopPendingSectionReport() {
			    initializeDateFilters();
			    
			    if (dmFilter.getDevice() == null) {
			        dmFilter.setDevice("L");
			    }

			    try {
			        fetchAndProcessReportData();
			        
			    } catch (Exception e) {
			    	e.printStackTrace();
			    }
			}

			private void initializeDateFilters() {
			    Calendar defaultFromDate = Calendar.getInstance();
			    defaultFromDate.set(2023, Calendar.JANUARY, 1,0,0,0);
			    defaultFromDate.set(Calendar.MILLISECOND, 0); 
			    
			    if (dmFilter.getFromDate() == null && dmFilter.getToDate() == null) {
			        dmFilter.setFromDate(defaultFromDate.getTime());
			        dmFilter.setToDate(new Date());
			    } else if (dmFilter.getFromDate() != null && dmFilter.getToDate() == null) {
			    	
			    	Calendar cal = Calendar.getInstance();
			        cal.setTime(dmFilter.getFromDate());
			        cal.set(Calendar.HOUR_OF_DAY, 0);
			        cal.set(Calendar.MINUTE, 0);
			        cal.set(Calendar.SECOND, 0);
			        cal.set(Calendar.MILLISECOND, 0);
			        dmFilter.setFromDate(cal.getTime());
			        
			        dmFilter.setToDate(new Date());
			        
			    } else if (dmFilter.getFromDate() == null && dmFilter.getToDate() != null) {
			        dmFilter.setFromDate(defaultFromDate.getTime());
			    }
			    
			    if (dmFilter.getFromDate().after(dmFilter.getToDate())) {
			        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
			            "ERROR", "From Date Cannot Be After To Date");
			        FacesContext.getCurrentInstance().addMessage(null, message);
			        throw new IllegalArgumentException("Invalid date range");
			    }
			}

			private void fetchAndProcessReportData() {
			    Session session = sessionFactory.openSession();
			    System.out.println("THE FROM DATE ---------------"+dmFilter.getFromDate());
			    System.out.println("THE TO DATE ---------------"+dmFilter.getToDate());
			    System.out.println("THE DEVICE ---------------"+dmFilter.getDevice());

			    
			    String reportQuery = buildReportQuery(dmFilter.getDevice(),dmFilter.getNoOfOffices());
			    
			    Query query = session.createNativeQuery(reportQuery)
			        .setParameter("fromDate", dmFilter.getFromDate())
			        .setParameter("toDate", dmFilter.getToDate());

			    
			    List<Object[]> results = query.getResultList();
			    reports = transformResults(results);
			}

			private String buildReportQuery(String device, Integer noOfOffice) {
			    String deviceCondition = buildDeviceCondition(device);
			    
			    String baseQuery = "SELECT " +
			           "  cir.code as cirCode, " +
			           "  sec.code as secCode, " +
			           "  sec.NAME as secName, " +
			           // BILLING RELATED
			           "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'BL' THEN 1 ELSE 0 END) as blTot, " +
			           "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'BL' AND c.STATUS_ID = '2' THEN 1 ELSE 0 END) as blCom, " +
			           "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'BL' AND c.STATUS_ID IN ('0','1') THEN 1 ELSE 0 END) as blPen, " +
			           //METER RELATED
			           "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'ME' THEN 1 ELSE 0 END) as meTot, " +
			           "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'ME' AND c.STATUS_ID = '2' THEN 1 ELSE 0 END) as meCom, " +
			           "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'ME' AND c.STATUS_ID IN ('0','1') THEN 1 ELSE 0 END) as mePen, " +
			           //POWER FAILURE
					   "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'PF' THEN 1 ELSE 0 END) as pfTot, " +
					   "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'PF' AND c.STATUS_ID = '2' THEN 1 ELSE 0 END) as pfCom, " +
					   "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'PF' AND c.STATUS_ID IN ('0','1') THEN 1 ELSE 0 END) as pfPen, " +
                       // VOLATGE FLUCTUTATION
					   "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'VF' THEN 1 ELSE 0 END) as vfTot, " +
					   "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'VF' AND c.STATUS_ID = '2' THEN 1 ELSE 0 END) as vfCom, " +
					   "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'VF' AND c.STATUS_ID IN ('0','1') THEN 1 ELSE 0 END) as vfPen, " +
					   //FIRE
					   "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'FI' THEN 1 ELSE 0 END) as fiTot, " +
					   "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'FI' AND c.STATUS_ID = '2' THEN 1 ELSE 0 END) as fiCom, " +
					   "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'FI' AND c.STATUS_ID IN ('0','1') THEN 1 ELSE 0 END) as fiPen, " +
					   //DANGEROUS POLE
					   "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'TH' THEN 1 ELSE 0 END) as thTot, " +
					   "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'TH' AND c.STATUS_ID = '2' THEN 1 ELSE 0 END) as thCom, " +
					   "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'TH' AND c.STATUS_ID IN ('0','1') THEN 1 ELSE 0 END) as thPen, " +
					   // THEFT
					   "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'TE' THEN 1 ELSE 0 END) as teTot, " +
					   "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'TE' AND c.STATUS_ID = '2' THEN 1 ELSE 0 END) as teCom, " +
					   "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'TE' AND c.STATUS_ID IN ('0','1') THEN 1 ELSE 0 END) as tePen, " +
					   //CONDUCTOR SNAPPING
					   "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'CS' THEN 1 ELSE 0 END) as csTot, " +
					   "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'CS' AND c.STATUS_ID = '2' THEN 1 ELSE 0 END) as csCom, " +
					   "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'CS' AND c.STATUS_ID IN ('0','1') THEN 1 ELSE 0 END) as csPen, " +
					   //OTHERS
					   "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'OT' THEN 1 ELSE 0 END) as otTot, " +
					   "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'OT' AND c.STATUS_ID = '2' THEN 1 ELSE 0 END) as otCom, " +
					   "  SUM(CASE WHEN c.COMPLAINT_TYPE = 'OT' AND c.STATUS_ID IN ('0','1') THEN 1 ELSE 0 END) as otPen, " +

			           "  cir.NAME as cirName, " +
			           "  c.REGION_ID as regCode, " +
			           "  div.NAME as divisionName, " +
			           //TOTAL PENDING
			           "    (SUM(CASE WHEN c.COMPLAINT_TYPE = 'BL' AND c.STATUS_ID IN ('0','1') THEN 1 ELSE 0 END) + " +
			           "    SUM(CASE WHEN c.COMPLAINT_TYPE = 'ME' AND c.STATUS_ID IN ('0','1') THEN 1 ELSE 0 END) + " +
			           "    SUM(CASE WHEN c.COMPLAINT_TYPE = 'PF' AND c.STATUS_ID IN ('0','1') THEN 1 ELSE 0 END) + " +
			           "    SUM(CASE WHEN c.COMPLAINT_TYPE = 'VF' AND c.STATUS_ID IN ('0','1') THEN 1 ELSE 0 END) + " +
			           "    SUM(CASE WHEN c.COMPLAINT_TYPE = 'FI' AND c.STATUS_ID IN ('0','1') THEN 1 ELSE 0 END) + " +
			           "    SUM(CASE WHEN c.COMPLAINT_TYPE = 'TH' AND c.STATUS_ID IN ('0','1') THEN 1 ELSE 0 END) + " +
			           "    SUM(CASE WHEN c.COMPLAINT_TYPE = 'TE' AND c.STATUS_ID IN ('0','1') THEN 1 ELSE 0 END) + " +
			           "    SUM(CASE WHEN c.COMPLAINT_TYPE = 'CS' AND c.STATUS_ID IN ('0','1') THEN 1 ELSE 0 END) + " +
			           "    SUM(CASE WHEN c.COMPLAINT_TYPE = 'OT' AND c.STATUS_ID IN ('0','1') THEN 1 ELSE 0 END)) as total_pending " +
			           "FROM SECTION sec " +
			           "JOIN DIVISION div ON sec.DIVISION_ID = div.ID " +
			           "JOIN CIRCLE cir ON div.CIRCLE_ID = cir.ID " +
			           "LEFT JOIN COMPLAINT c ON c.SECTION_ID = sec.ID " +
			           "AND TRUNC(c.CREATED_ON) BETWEEN :fromDate AND :toDate " +
			           deviceCondition + " " +  
			           "GROUP BY cir.code, sec.code, sec.NAME, cir.NAME, cir.REGION_ID, div.NAME, c.REGION_ID " +
			           "ORDER BY total_pending DESC";
			    
			    // If we need to limit results, wrap the query in a subquery
			    if (noOfOffice != null && noOfOffice > 0) {
			        baseQuery = "SELECT * FROM (" + baseQuery + ") WHERE ROWNUM <= " + noOfOffice;
			    }
			    
			    return baseQuery;
			}

			private String buildDeviceCondition(String device) {
			    if (device == null || "L".equals(device)) {
			        return "";  // Returns empty string (no extra AND)
			    }
			    
			    switch (device) {
			        case "P": return " AND c.DEVICE IN ('IMOB','iOS','Android','AMOB','mobile')";
			        case "W": return " AND c.DEVICE IN ('web')";
			        case "S": return " AND c.DEVICE IN ('SM')";
			        case "A": return " AND c.DEVICE IN ('admin','FOC')";
			        case "M": return " AND c.DEVICE IN ('MI')";
			        case "G": return " AND c.DEVICE IN ('MM')";
			        case "O": return " AND c.DEVICE NOT IN ('admin','FOC')";
			        default: return "";
			    }
			}

			private List<TmpCtSecBean> transformResults(List<Object[]> results) {
			    List<TmpCtSecBean> reportList = new ArrayList<>();
			    
			    for (Object[] row : results) {
			    	TmpCtSecBean report = new TmpCtSecBean();
					
					report.setCirCode((String)row[0]);
					report.setSecCode((String)row[1]);
					report.setSecName((String)row[2]);
					report.setBlTot((BigDecimal) row[3]);
					report.setBlCom((BigDecimal) row[4]);
					report.setBlPen((BigDecimal) row[5]);
					report.setMeTot((BigDecimal) row[6]);
					report.setMeCom((BigDecimal) row[7]);
					report.setMePen((BigDecimal) row[8]);
					report.setPfTot((BigDecimal) row[9]);
					report.setPfCom((BigDecimal) row[10]);
					report.setPfPen((BigDecimal) row[11]);
					report.setVfTot((BigDecimal) row[12]);
					report.setVfCom((BigDecimal) row[13]);
					report.setVfPen((BigDecimal) row[14]);
					report.setFiTot((BigDecimal) row[15]);
					report.setFiCom((BigDecimal) row[16]);
					report.setFiPen((BigDecimal) row[17]);
					report.setThTot((BigDecimal) row[18]);
					report.setThCom((BigDecimal) row[19]);
					report.setThPen((BigDecimal) row[20]);
					report.setTeTot((BigDecimal) row[21]);
					report.setTeCom((BigDecimal) row[22]);
					report.setTePen((BigDecimal) row[23]);
					report.setCsTot((BigDecimal) row[24]);
					report.setCsCom((BigDecimal) row[25]);
					report.setCsPen((BigDecimal) row[26]);
					report.setOtTot((BigDecimal) row[27]);
					report.setOtCom((BigDecimal) row[28]);
					report.setOtPen((BigDecimal) row[29]);
					report.setCirName((String) row[30]);
					report.setRegCode((String) (row[31] != null ? row[31].toString() : null));
					report.setDivisionName((String) row[32]);
			        
			        reportList.add(report);
			    }
			    
			    return reportList;
			}


			
	
	public void redirectToCircleReport() throws IOException {
        FacesContext.getCurrentInstance().getExternalContext().redirect("topOfficesCircleAbstract.xhtml");
	}

	
	@Transactional
	public void getComplaintListForSection(String secCode,String sectionName) throws IOException {
		System.out.println("THE SELECTED SECTION CODE IS ============"+secCode);
		try (Session session = sessionFactory.openSession()) {
			
			Timestamp fromDate = new Timestamp(dmFilter.getFromDate().getTime());
			Timestamp toDate = new Timestamp(dmFilter.getToDate().getTime());
	
		Integer sectionCode = Integer.parseInt(secCode);
		
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
                "AND a.created_on BETWEEN :fromDate AND :toDate";	
		
		Query query = session.createNativeQuery(hql);
		query.setParameter("sectionCode", sectionCode);
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
				
		 selectedSectionName = sectionName;
		
		FacesContext.getCurrentInstance().getExternalContext().redirect("topOfficesComplaintList.xhtml");
		
		}catch(Exception e){
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
	


	
	



	private void setCellValue(Row row, int colIndex, BigDecimal value, CellStyle style) {
	    Cell cell = row.createCell(colIndex);
	    cell.setCellValue(value != null ? value.doubleValue() : 0);
	    cell.setCellStyle(style);
	}
	 
	//SECTION REPORT TO EXCEL DOWNLOAD
	 public void exportSectionsToExcel(List<TmpCtSecBean> reports) throws IOException {
		    HSSFWorkbook workbook = new HSSFWorkbook();
		    Sheet sheet = workbook.createSheet("Pending_Top_Section_Offices_Abstract_Report");

		    sheet.setColumnWidth(0, 2000); //S.NO
		    sheet.setColumnWidth(1, 5000); // SECTION
		    sheet.setColumnWidth(2, 5000); // DIVISION
		    sheet.setColumnWidth(3, 4000); // CIRCLE

		    
		    for (int i = 4; i <= 32; i++) {  
		        sheet.setColumnWidth(i, 2000);
		    }
		    Row headingRow = sheet.createRow(0);
		    Cell headingCell = headingRow.createCell(0);
		    headingCell.setCellValue("PENDING TOP SECTION OFFICES ABSTRACT REPORT");
		    
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

		    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		    String fromDate = sdf.format(dmFilter.getFromDate());
		    String toDate = sdf.format(dmFilter.getToDate());
		   
		    
		    CellStyle mainHeadingStyle = workbook.createCellStyle();
		    HSSFFont mainHeadingFont = workbook.createFont();
		    mainHeadingFont.setBold(true);
		    mainHeadingFont.setFontHeightInPoints((short) 14);  // Increased font size
		    mainHeadingFont.setColor(IndexedColors.BLACK.getIndex());  // White font
		    mainHeadingStyle.setFont(mainHeadingFont);
		    mainHeadingStyle.setAlignment(HorizontalAlignment.CENTER);
		    mainHeadingStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());  // Dark blue background
		    mainHeadingStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		    mainHeadingStyle.setBorderBottom(BorderStyle.MEDIUM);
		    mainHeadingStyle.setBorderTop(BorderStyle.MEDIUM);
		    mainHeadingStyle.setBorderLeft(BorderStyle.MEDIUM);
		    mainHeadingStyle.setBorderRight(BorderStyle.MEDIUM);
		    mainHeadingStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		    // Create main heading with increased height
		    Row mainHeadingRow = sheet.createRow(0);
		    mainHeadingRow.setHeightInPoints(30);  // Increased row height
		    Cell mainHeadingCell = mainHeadingRow.createCell(0);
		    mainHeadingCell.setCellValue("PENDING TOP SECTION OFFICES ABSTRACT REPORT");
		    mainHeadingCell.setCellStyle(mainHeadingStyle);
		    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 31));
		    
		    // SUB-HEADING STYLE - Enhanced for better visibility
		    CellStyle subHeadingStyle = workbook.createCellStyle();
		    HSSFFont subHeadingFont = workbook.createFont();
		    subHeadingFont.setBold(true);
		    subHeadingFont.setFontHeightInPoints((short) 10);
		    subHeadingStyle.setFont(subHeadingFont);
		    subHeadingStyle.setAlignment(HorizontalAlignment.CENTER);
		    subHeadingStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		    subHeadingStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
		    subHeadingStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		    subHeadingStyle.setBorderBottom(BorderStyle.THIN);
		    subHeadingStyle.setBorderTop(BorderStyle.THIN);
		    subHeadingStyle.setBorderLeft(BorderStyle.THIN);
		    subHeadingStyle.setBorderRight(BorderStyle.THIN);
		    
		    CellStyle dataCellStyle = workbook.createCellStyle();
		    dataCellStyle.setBorderBottom(BorderStyle.THIN);
		    dataCellStyle.setBorderTop(BorderStyle.THIN);
		    dataCellStyle.setBorderLeft(BorderStyle.THIN);
		    dataCellStyle.setBorderRight(BorderStyle.THIN);
		    dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
		    dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		    
		    CellStyle dataCellStyle2 = workbook.createCellStyle();
		    dataCellStyle2.setBorderBottom(BorderStyle.THIN);
		    dataCellStyle2.setBorderTop(BorderStyle.THIN);
		    dataCellStyle2.setBorderLeft(BorderStyle.THIN);
		    dataCellStyle2.setBorderRight(BorderStyle.THIN);
		    dataCellStyle2.setAlignment(HorizontalAlignment.LEFT);
		    dataCellStyle2.setVerticalAlignment(VerticalAlignment.CENTER);


		    Row subHeadingRow = sheet.createRow(1);
		    subHeadingRow.setHeightInPoints(20);  
		    Cell subHeadingCell = subHeadingRow.createCell(0);
		    subHeadingCell.setCellValue("FROM: " + fromDate + " | TO: " + toDate + " | DEVICE: " + device);
		    subHeadingCell.setCellStyle(subHeadingStyle);
		    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 31));

		    CellStyle mainHeaderStyle = workbook.createCellStyle();
		    HSSFFont mainHeaderFont = workbook.createFont();
		    mainHeaderFont.setBold(true);
		    mainHeaderFont.setFontHeightInPoints((short) 10);
		    mainHeaderStyle.setFont(mainHeaderFont);
		    mainHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
		    mainHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		    mainHeaderStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
		    mainHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		    mainHeaderStyle.setBorderBottom(BorderStyle.THIN);
		    mainHeaderStyle.setBorderTop(BorderStyle.THIN);
		    mainHeaderStyle.setBorderLeft(BorderStyle.THIN);
		    mainHeaderStyle.setBorderRight(BorderStyle.THIN);
		    mainHeaderStyle.setWrapText(true);  // Enable text wrapping

		    CellStyle subHeaderStyle = workbook.createCellStyle();
		    HSSFFont subHeaderFont = workbook.createFont();
		    subHeaderFont.setBold(true);
		    subHeaderStyle.setFont(subHeaderFont);
		    subHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
		    subHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		    subHeaderStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		    subHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		    subHeaderStyle.setBorderBottom(BorderStyle.THIN);
		    subHeaderStyle.setBorderTop(BorderStyle.THIN);
		    subHeaderStyle.setBorderLeft(BorderStyle.THIN);
		    subHeaderStyle.setBorderRight(BorderStyle.THIN);

		    Row headerRow1 = sheet.createRow(2);
		    Row headerRow2 = sheet.createRow(3);

		    String[] mainHeaders = {"S.NO","SECTION", "DIVISION", "CIRCLE","Billing Related", "Meter Related", "Power Failure", "Voltage Fluctuation", 
		                            "Fire", "Dangerous Pole", "Theft", "Conductor Snapping", "Others", "TOTAL"};

		    String[] subHeaders = {"Revd.", "Comp.", "Pend."};
		    
		    int colIndex = 0;
		    for (String mainHeader : mainHeaders) {
		        Cell cell = headerRow1.createCell(colIndex);
		        cell.setCellValue(mainHeader);
		        cell.setCellStyle(mainHeaderStyle);
		        
		        if (!mainHeader.equals("S.NO") &&!mainHeader.equals("SECTION") && !mainHeader.equals("DIVISION") && !mainHeader.equals("CIRCLE")) {
		            sheet.addMergedRegion(new CellRangeAddress(2, 2, colIndex, colIndex + 2));
		            for (String subHeader : subHeaders) {
		                Cell subCell = headerRow2.createCell(colIndex);
		                subCell.setCellValue(subHeader);
		                subCell.setCellStyle(subHeaderStyle);
		                colIndex++;
		            }
		        } else {
		            colIndex++;
		        }
		    }

		    int rowNum = 4;
		    BigDecimal[] totalSums = new BigDecimal[30];  
		    Arrays.fill(totalSums, BigDecimal.ZERO);

		    int serialNumber=1;
		    for (TmpCtSecBean report : reports) {
		        Row row = sheet.createRow(rowNum++);
		        row.createCell(0).setCellValue(String.valueOf(serialNumber++));
		        row.createCell(1).setCellValue(report.getSecName());
		        row.createCell(2).setCellValue(report.getDivisionName());
		        row.createCell(3).setCellValue(report.getCirName());

		        for (int i = 0; i <= 3; i++) {
		            row.getCell(i).setCellStyle(dataCellStyle2);
		        }

		        BigDecimal rowTotSum = BigDecimal.ZERO;
		        BigDecimal rowCompSum = BigDecimal.ZERO;
		        BigDecimal rowPendSum = BigDecimal.ZERO;

		        int dataIndex = 4; 

		        BigDecimal[] values = {
		            report.getBlTot(), report.getBlCom(), report.getBlPen(),
		            report.getMeTot(), report.getMeCom(), report.getMePen(),
		            report.getPfTot(), report.getPfCom(), report.getPfPen(),
		            report.getVfTot(), report.getVfCom(), report.getVfPen(),
		            report.getFiTot(), report.getFiCom(), report.getFiPen(),
		            report.getThTot(), report.getThCom(), report.getThPen(),
		            report.getTeTot(), report.getTeCom(), report.getTePen(),
		            report.getCsTot(), report.getCsCom(), report.getCsPen(),
		            report.getOtTot(), report.getOtCom(), report.getOtPen()
		        };

		        for (int i = 0; i < values.length; i += 3) {
		            setCellValue(row, dataIndex++, values[i], dataCellStyle);
		            setCellValue(row, dataIndex++, values[i + 1] , dataCellStyle);
		            setCellValue(row, dataIndex++, values[i + 2] , dataCellStyle);

		            rowTotSum = rowTotSum.add(values[i] != null ? values[i] : BigDecimal.ZERO);
		            rowCompSum = rowCompSum.add(values[i + 1] != null ? values[i + 1] : BigDecimal.ZERO);
		            rowPendSum = rowPendSum.add(values[i + 2] != null ? values[i + 2] : BigDecimal.ZERO);
		        }

		        setCellValue(row, dataIndex++, rowTotSum, dataCellStyle);
		        setCellValue(row, dataIndex++, rowCompSum, dataCellStyle);
		        setCellValue(row, dataIndex++, rowPendSum, dataCellStyle);

		        for (int i = 0; i < values.length; i++) {
		            totalSums[i] = totalSums[i].add(values[i] != null ? values[i] : BigDecimal.ZERO);
		        }
		        totalSums[values.length] = totalSums[values.length].add(rowTotSum);
		        totalSums[values.length + 1] = totalSums[values.length + 1].add(rowCompSum);
		        totalSums[values.length + 2] = totalSums[values.length + 2].add(rowPendSum);
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
		    totalRow.createCell(0).setCellValue("");
		    totalRow.getCell(0).setCellStyle(totalRowStyle);
		    totalRow.createCell(1).setCellValue("");
		    totalRow.getCell(1).setCellStyle(totalRowStyle);
		    totalRow.createCell(2).setCellValue("");
		    totalRow.getCell(2).setCellStyle(totalRowStyle);
		    totalRow.createCell(3).setCellValue("TOTAL");
		    totalRow.getCell(3).setCellStyle(totalRowStyle);

		    int totalDataIndex = 4;
		    for (int i = 0; i < totalSums.length; i++) {
		    	setCellValue(totalRow, totalDataIndex, totalSums[i],dataCellStyle);
		    	totalRow.getCell(totalDataIndex++).setCellStyle(totalRowStyle);

		    }

		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    workbook.write(outputStream);
		    workbook.close();

		    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		    try {
		        excelFile = DefaultStreamedContent.builder()
		                .name("Pending_Top_Section_Offices_Abstract_Report.xls")
		                .contentType("application/vnd.ms-excel")
		                .stream(() -> inputStream)
		                .build();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}
	 
	
	 
	  
	  
	  
	  public void exportSectionsToPdf(List<TmpCtSecBean> sectionList) throws IOException {
		    final int CATEGORY = 9;
		    final int SUB_COLUMNS = 3;
		    final int COLUMN_COUNT = 1+3 + (CATEGORY * SUB_COLUMNS) + 3;  // S.NO+ SECTION + DIVISION +CIRCLE + 9*3 +3
		    
		    final float SNO_COLUMN_WIDTH = 5f;
		    final float FIRST_COLUMN_WIDTH = 15f;
		    final float OTHER_COLUMN_WIDTH = 4f;
		    final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
		    final Font TOTAL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
		    final Font DATA_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
		    final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
		    		    
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
		    
		    
		    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		    String fromDate = sdf.format(dmFilter.getFromDate());
		    String toDate =sdf.format(dmFilter.getToDate());
		    

		    Document document = new Document(PageSize.A2.rotate());
		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    
		    try {
		        PdfWriter.getInstance(document, outputStream);
		        document.open();
		        
		        PdfPTable table = new PdfPTable(COLUMN_COUNT);
		        table.setWidthPercentage(100);
		        table.setSpacingBefore(10f);
		        
		        Paragraph title = new Paragraph("PENDING TOP SECTION OFFICE ABSTRACT REPORT", TITLE_FONT);
		        title.setAlignment(Element.ALIGN_CENTER);
		        document.add(title);
		        
		        Paragraph subTitle = new Paragraph("FROM : " + fromDate +","+ " TO " +toDate +","+" Device: "+device);
		        subTitle.setAlignment(Element.ALIGN_CENTER);
		        subTitle.setSpacingAfter(10);
		        document.add(subTitle);
		        
		        float[] columnWidths = new float[COLUMN_COUNT];
		        columnWidths[0] = SNO_COLUMN_WIDTH;
		        columnWidths[1] = FIRST_COLUMN_WIDTH; // SECTION
		        columnWidths[2] = FIRST_COLUMN_WIDTH; // DIVISION
		        columnWidths[3] = FIRST_COLUMN_WIDTH; // CIRCLE


		        for (int i = 4; i < COLUMN_COUNT; i++) {
		            columnWidths[i] = OTHER_COLUMN_WIDTH;
		        }
		        table.setWidths(columnWidths);
		        
		        addMergedHeaderCell(table, "S.NO", HEADER_FONT, 1, 2);
		        addMergedHeaderCell(table, "SECTION", HEADER_FONT, 1, 2);
		        addMergedHeaderCell(table, "DIVISION", HEADER_FONT, 1, 2);
		        addMergedHeaderCell(table, "CIRCLE", HEADER_FONT, 1, 2);

		        String[] categoryHeaders = {"Billing Releated","Meter Releated","Power Failure","Voltage Fluctuation","Fire","Dangerous Pole","Theft","Conductor Snapping","Others"};
		        for (int category = 0; category < categoryHeaders.length; category++) {
		            addMergedHeaderCell(table, categoryHeaders[category], HEADER_FONT, 3, 1);
		        }
		        addMergedHeaderCell(table, "TOTAL", HEADER_FONT, 3, 1);
		        
		        
		      //SUB HEADER
		        String[] subHeaders = {"Revd.", "Comp.", "Pend."};
		        for (int i = 0; i < CATEGORY + 1; i++) { 
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
		        
		        int serialNumber =1;
		        for (TmpCtSecBean report : sectionList) {
		            table.addCell(createDataCell(String.valueOf(serialNumber++), DATA_FONT, Element.ALIGN_LEFT));
		            table.addCell(createDataCell(report.getSecName(), DATA_FONT, Element.ALIGN_LEFT));
		            table.addCell(createDataCell(report.getDivisionName(), DATA_FONT, Element.ALIGN_LEFT));
		            table.addCell(createDataCell(report.getCirName(), DATA_FONT, Element.ALIGN_LEFT));

		            
		            addHourData(table, report.getBlTot(), report.getBlCom(), report.getBlPen(), DATA_FONT);
		            addHourData(table, report.getMeTot(), report.getMeCom(), report.getMePen(), DATA_FONT);
		            addHourData(table, report.getPfTot(), report.getPfCom(), report.getPfPen(), DATA_FONT);
		            addHourData(table, report.getVfTot(), report.getVfCom(), report.getVfPen(), DATA_FONT);
		            
		            addHourData(table, report.getFiTot(), report.getFiCom(), report.getFiPen(), DATA_FONT);
		            addHourData(table, report.getThTot(), report.getThCom(), report.getThPen(), DATA_FONT);
		            addHourData(table, report.getTeTot(), report.getTeCom(), report.getTePen(), DATA_FONT);
		            addHourData(table, report.getCsTot(), report.getCsCom(), report.getCsPen(), DATA_FONT);
		            
		            addHourData(table, report.getOtTot(), report.getOtCom(), report.getOtPen(), DATA_FONT);
		            

		            BigDecimal total = report.getBlTot().add(report.getMeTot()).add(report.getPfTot()).add(report.getVfTot())
							.add(report.getFiTot()).add(report.getThTot()).add(report.getTeTot()).add(report.getCsTot())
							.add(report.getOtTot());
		            
		            BigDecimal totalCpl = report.getBlCom().add(report.getMeCom()).add(report.getPfCom()).add(report.getVfCom())
							.add(report.getFiCom()).add(report.getThCom()).add(report.getTeCom()).add(report.getCsCom())
							.add(report.getOtCom());
					
		            BigDecimal pend = report.getBlPen().add(report.getMePen()).add(report.getPfPen()).add(report.getVfPen())
							.add(report.getFiPen()).add(report.getThPen()).add(report.getTePen()).add(report.getCsPen())
							.add(report.getOtPen());					 
					 
			            grandTot = grandTot.add(total);
			            grandTotCpl =grandTotCpl.add(totalCpl);
			            grandTotPend = grandTotPend.add(pend);


		            
		            addHourData(table,total, totalCpl,pend,TOTAL_FONT);
		        }
		        PdfPCell footerLabel = new PdfPCell(new Phrase("TOTAL", TOTAL_FONT));
		        footerLabel.setBackgroundColor(BaseColor.LIGHT_GRAY);
		        footerLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
		        footerLabel.setColspan(4); 
		        table.addCell(footerLabel);
		        

		        addHourData(table,
		            sectionList.stream().map(r->r.getBlTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
		            sectionList.stream().map(r->r.getBlCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
		            sectionList.stream().map(r->r.getBlPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
		            TOTAL_FONT);
		        
		        addHourData(table,
		        		sectionList.stream().map(r->r.getMeTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
		        		sectionList.stream().map(r->r.getMeCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
		        		sectionList.stream().map(r->r.getMePen()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                TOTAL_FONT);
		        
		        addHourData(table,
		        		sectionList.stream().map(r->r.getPfTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
		        		sectionList.stream().map(r->r.getPfCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
		        		sectionList.stream().map(r->r.getPfPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                TOTAL_FONT);
		        
		        addHourData(table,
		        		sectionList.stream().map(r->r.getVfTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
		        		sectionList.stream().map(r->r.getVfCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
		        		sectionList.stream().map(r->r.getVfPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                TOTAL_FONT);
		        
		        addHourData(table,
		        		sectionList.stream().map(r->r.getFiTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
		        		sectionList.stream().map(r->r.getFiCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
		        		sectionList.stream().map(r->r.getFiPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                TOTAL_FONT);
		        
		        addHourData(table,
		        		sectionList.stream().map(r->r.getThTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
		        		sectionList.stream().map(r->r.getThCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
		        		sectionList.stream().map(r->r.getThPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                TOTAL_FONT);
		        
		        addHourData(table,
		        		sectionList.stream().map(r->r.getTeTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                sectionList.stream().map(r->r.getTeCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                sectionList.stream().map(r->r.getTePen()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                TOTAL_FONT);
		        
		        addHourData(table,
		        		sectionList.stream().map(r->r.getCsTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
		        		sectionList.stream().map(r->r.getCsCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
		        		sectionList.stream().map(r->r.getCsPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                TOTAL_FONT);
		        
		        addHourData(table,
		        		sectionList.stream().map(r->r.getOtTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
		        		sectionList.stream().map(r->r.getOtCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
		        		sectionList.stream().map(r->r.getOtPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
		                TOTAL_FONT);
		        
		       
		        
		       
		        addHourData(table, grandTot, grandTotCpl, grandTotPend, TOTAL_FONT);

		        document.add(table);
		        document.close();
		        
		        pdfFile = DefaultStreamedContent.builder()
			            .contentType("application/pdf")
			            .name("Pending_Top_Section_Offices_Abstract_Report.pdf")
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

		private void addHourData(PdfPTable table, BigDecimal revd, BigDecimal cpl, BigDecimal pend, Font font) {
		    table.addCell(createDataCell(revd.toString(), font, Element.ALIGN_CENTER));
		    table.addCell(createDataCell(cpl.toString(), font, Element.ALIGN_CENTER));
		    table.addCell(createDataCell(pend.toString(), font, Element.ALIGN_CENTER));
		}
	  
	  
	  public void exportComplaintListToExcel(List<ViewComplaintReportValueBean> complaintList) throws IOException {
		  HSSFWorkbook workbook = new HSSFWorkbook();
		  Sheet sheet = workbook.createSheet("Pending_Top_Offices_Abstract_Complaints_Report");
		  
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
		                .name("Pending_Top_Offices_Abstract_Complaints_Report.xls")
		                .contentType("application/vnd.ms-excel")
		                .stream(() -> inputStream)
		                .build();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
	  }
	  
	  public void exportComplaintListToPdf(List<ViewComplaintReportValueBean> complaintList) {
		  Document document = new Document(PageSize.A4.rotate());
		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		    try {
		        PdfWriter.getInstance(document, outputStream);
		        document.open();
		        
		        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD, BaseColor.BLACK);
		        Paragraph title = new Paragraph("PENDING TOP OFFICES ABSTRACT COMPLAINTS REPORT", titleFont);
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
		            .name("Pending_Top_Offices_Abstract_Complaints_Report.pdf")
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

	public List<TmpCtSecBean> getReports() {
		return reports;
	}

	public void setReports(List<TmpCtSecBean> reports) {
		this.reports = reports;
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
}
