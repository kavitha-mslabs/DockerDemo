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
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.servlet.http.HttpSession;
import tneb.ccms.admin.controller.Authentication;
import tneb.ccms.admin.model.CompDeviceBean;
import tneb.ccms.admin.model.ComplaintBean;
import tneb.ccms.admin.model.CustMasterBean;
import tneb.ccms.admin.model.ViewComplaintReportBean;
import tneb.ccms.admin.util.CCMSConstants;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.valuebeans.AdminUserValueBean;
import tneb.ccms.admin.valuebeans.CallCenterUserValueBean;
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
public class CategoryAndDeviceWiseAbstract implements Serializable {

	private static final long serialVersionUID = 1L;
	private SessionFactory sessionFactory;
	private boolean cameFromInsideReport = false;   
	private boolean cameFromInsideSection = false;  

	DataModel dmFilter;
	List<TmpCtCirBean> reports = new ArrayList<>();
	List<TmpCtSecBean> sectionList = new ArrayList<>();
	StreamedContent excelFile;
	StreamedContent pdfFile;
	List<ViewComplaintReportValueBean> complaintList = new ArrayList<>();
	String selectedCircleName = null;
	String selectedSectionName = null;
	String redirectFrom;
	private Date currentDate = new Date();
	private String currentYear = String.valueOf(Year.now().getValue());
	ViewComplaintReportValueBean selectedComplaintId;
	List<CompDeviceValueBean> devices;
	Authentication authentication = new Authentication();
	

	private BigDecimal totalBlTot = BigDecimal.ZERO;
	private BigDecimal totalBlCom = BigDecimal.ZERO;
	private BigDecimal totalBlPen = BigDecimal.ZERO;

	private BigDecimal totalMeTot = BigDecimal.ZERO;
	private BigDecimal totalMeCom = BigDecimal.ZERO;
	private BigDecimal totalMePen = BigDecimal.ZERO;

	private BigDecimal totalPfTot = BigDecimal.ZERO;
	private BigDecimal totalPfCom = BigDecimal.ZERO;
	private BigDecimal totalPfPen = BigDecimal.ZERO;

	private BigDecimal totalVfTot = BigDecimal.ZERO;
	private BigDecimal totalVfCom = BigDecimal.ZERO;
	private BigDecimal totalVfPen = BigDecimal.ZERO;

	private BigDecimal totalFiTot = BigDecimal.ZERO;
	private BigDecimal totalFiCom = BigDecimal.ZERO;
	private BigDecimal totalFiPen = BigDecimal.ZERO;

	private BigDecimal totalThTot = BigDecimal.ZERO;
	private BigDecimal totalThCom = BigDecimal.ZERO;
	private BigDecimal totalThPen = BigDecimal.ZERO;

	private BigDecimal totalTeTot = BigDecimal.ZERO;
	private BigDecimal totalTeCom = BigDecimal.ZERO;
	private BigDecimal totalTePen = BigDecimal.ZERO;

	private BigDecimal totalCsTot = BigDecimal.ZERO;
	private BigDecimal totalCsCom = BigDecimal.ZERO;
	private BigDecimal totalCsPen = BigDecimal.ZERO;

	private BigDecimal totalOtTot = BigDecimal.ZERO;
	private BigDecimal totalOtCom = BigDecimal.ZERO;
	private BigDecimal totalOtPen = BigDecimal.ZERO;

	private BigDecimal grandTotalRevd = BigDecimal.ZERO;
	private BigDecimal grandTotalComp = BigDecimal.ZERO;
	private BigDecimal grandTotalPend = BigDecimal.ZERO;

	
	@PostConstruct
	public void init() {
		System.out.println("Initializing CATEGORY AND DEVICE WISE  ABSTRACT...");
		sessionFactory = HibernateUtil.getSessionFactory();
		dmFilter = new DataModel();
	
		loadAllDevicesAndCategories(); // LOAD CATEGORY AND DEVICE LIST FILTER

	}
	
	// REFERSH CIRCLE REPORT
	public void resetIfNeeded() {
	    if (!FacesContext.getCurrentInstance().isPostback() && !cameFromInsideReport) {
	        reports = null;
	        dmFilter.setFromDate(null);
	        dmFilter.setToDate(null);
	    }
	    cameFromInsideReport = false; // Always reset the flag
	}

	
	// REFRESH SECTION REPORT
	public void resetSectionIfNeeded() {
		
	    if (!FacesContext.getCurrentInstance().isPostback() && !cameFromInsideSection ) {
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

	    } catch (Exception e) {
	        e.printStackTrace();
	        devices = new ArrayList<>();
	    } finally {
	        if (session != null && session.isOpen()) {
	            session.close();
	        }
	    }
	}


	
	// REFRESH BUTTON
	public void clearFiltersAndCircleReport() {
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
		                    dmFilter.setCircleCode(String.valueOf(circleId.get(0)));
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


	
	// CIRCLE REPORT
	@Transactional
	public void search() {
		
		updateLoginWiseFilters();
		
		System.err.println("dmFilter.getDevice----------------"+dmFilter.getDevice());

		Calendar fromCal = Calendar.getInstance();
		fromCal.set(2023, 0, 1);
		if ((dmFilter.getFromDate() == null) && (dmFilter.getToDate() == null)) {

			dmFilter.setFromDate(fromCal.getTime());
			dmFilter.setToDate(new Date());
		}
		if ((dmFilter.getFromDate() != null) && (dmFilter.getToDate() == null)) {
			dmFilter.setToDate(new Date());
		}
		if ((dmFilter.getFromDate() == null) && (dmFilter.getToDate() != null)) {
			dmFilter.setFromDate(fromCal.getTime());
		}
		if (dmFilter.getFromDate().after(dmFilter.getToDate())) {

			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR",
					"From Date Cannot Be After To Date");
			FacesContext.getCurrentInstance().addMessage(null, message);
			return;

		}

		Session session = null;
		try {
			session = sessionFactory.openSession();
			session.beginTransaction();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String formattedFromDate = sdf.format(dmFilter.getFromDate());
			String formattedToDate = sdf.format(dmFilter.getToDate());

			session.createNativeQuery(
					"BEGIN cat_cir_abst_all_dt_dev(:regionCode,:circleCode,:device,TO_DATE(:fromDate, 'YYYY-MM-DD'), TO_DATE(:toDate, 'YYYY-MM-DD')); END;")
					.setParameter("regionCode", dmFilter.getRegionCode())
					.setParameter("circleCode", dmFilter.getCircleCode())
					.setParameter("device", dmFilter.getDevice())
					.setParameter("fromDate", formattedFromDate)
					.setParameter("toDate", formattedToDate)
					.executeUpdate();

			session.createNativeQuery("BEGIN rev_cat_cir_abst_single_all(:regionCode,:circleCode); END;")
			.setParameter("regionCode", dmFilter.getRegionCode())
			.setParameter("circleCode", dmFilter.getCircleCode())
			.executeUpdate();

			session.flush();
			session.getTransaction().commit();

			try {
			String hql = "SELECT REGCODE, REGNAME, CIRCODE, CIRNAME, BLTOT, BLCOM, BLPEN, METOT, MECOM, MEPEN, PFTOT, PFCOM, PFPEN, VFTOT, VFCOM, VFPEN, FITOT,FICOM, FIPEN, THTOT, THCOM, THPEN, TETOT, TECOM, TEPEN, CSTOT, CSCOM, CSPEN, OTTOT, OTCOM, OTPEN FROM TMP_CT_CIR";
			
			List<Object[]> results = session.createNativeQuery(hql).getResultList();
			List<TmpCtCirBean> circleList = new ArrayList<>();

			for (Object[] row : results) {
				TmpCtCirBean report = new TmpCtCirBean();
				report.setRegcode((String) row[0]);
				report.setRegname((String) row[1]);
				report.setCircode((String) row[2]);
				report.setCirname((String) row[3]);

				report.setBlTot((BigDecimal) row[4]);
				report.setBlCom((BigDecimal) row[5]);
				report.setBlPen((BigDecimal) row[6]);
				report.setMeTot((BigDecimal) row[7]);
				report.setMeCom((BigDecimal) row[8]);
				report.setMePen((BigDecimal) row[9]);
				report.setPfTot((BigDecimal) row[10]);
				report.setPfCom((BigDecimal) row[11]);
				report.setPfPen((BigDecimal) row[12]);
				report.setVfTot((BigDecimal) row[13]);
				report.setVfCom((BigDecimal) row[14]);
				report.setVfPen((BigDecimal) row[15]);
				report.setFiTot((BigDecimal) row[16]);
				report.setFiCom((BigDecimal) row[17]);
				report.setFiPen((BigDecimal) row[18]);
				report.setThTot((BigDecimal) row[19]);
				report.setThCom((BigDecimal) row[20]);
				report.setThPen((BigDecimal) row[21]);
				report.setTeTot((BigDecimal) row[22]);
				report.setTeCom((BigDecimal) row[23]);
				report.setTePen((BigDecimal) row[24]);
				report.setCsTot((BigDecimal) row[25]);
				report.setCsCom((BigDecimal) row[26]);
				report.setCsPen((BigDecimal) row[27]);
				report.setOtTot((BigDecimal) row[28]);
				report.setOtCom((BigDecimal) row[29]);
				report.setOtPen((BigDecimal) row[30]);

				circleList.add(report);
			}
		    HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance() .getExternalContext().getSession(false);

		    CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");
		    
		    if(callCenterValueBean!=null &&( callCenterValueBean.getRoleId()==1 || callCenterValueBean.getRoleId()==7)){
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
		    			.filter(r ->  String.valueOf(regionId).contains(r.getRegcode()))
		    		    .filter(c -> String.valueOf(circleId).contains(c.getCircode()))
		    		    .collect(Collectors.toList());
		    }
			
			
			if(dmFilter.getRegionCode().equals("A")) {
				reports = circleList;
			}else {
				reports = circleList.stream().filter(a -> a.getRegcode().equals(dmFilter.getRegionCode())).collect(Collectors.toList());
			}
			// TOTAL FOOTER VALUES
			computeTotals();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in database operation");
		}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in fetching report");
		}
		
	}


	
	// BACK BUTTON
	public void redirectToCircleReport() throws IOException {
		this.cameFromInsideReport=true;
		FacesContext.getCurrentInstance().getExternalContext().redirect("categoryAndDeviceCircle.xhtml");

	}

	public void redirectToSectionReport() throws IOException {
		if ("circle".equals(redirectFrom)) {
			FacesContext.getCurrentInstance().getExternalContext().redirect("categoryAndDeviceCircle.xhtml");

		} else {
			FacesContext.getCurrentInstance().getExternalContext().redirect("categoryAndDeviceSection.xhtml");
		}

	}

	
	// SECTION REPORT 
	@Transactional
	public void fetchReportByCircle(String circleCode, String circleName) {

		Session session = null;
		try {
			session = sessionFactory.openSession();
			session.beginTransaction();
			
			Calendar fromCal = Calendar.getInstance();
			fromCal.set(2023, 0, 1);
			if ((dmFilter.getFromDate() == null) && (dmFilter.getToDate() == null)) {

				dmFilter.setFromDate(fromCal.getTime());
				dmFilter.setToDate(new Date());
			}
			if ((dmFilter.getFromDate() != null) && (dmFilter.getToDate() == null)) {
				dmFilter.setToDate(new Date());
			}
			if ((dmFilter.getFromDate() == null) && (dmFilter.getToDate() != null)) {
				dmFilter.setFromDate(fromCal.getTime());
			}
			

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String formattedFromDate = sdf.format(dmFilter.getFromDate());
			String formattedToDate = sdf.format(dmFilter.getToDate());

			System.out.println("Processed From Date: " + formattedFromDate);
			System.out.println("Processed To Date: " + formattedToDate);
			System.out.println("THE CIRCLE CODE: " + circleCode);

			if (dmFilter.getSectionCode() == null) {
				dmFilter.setSectionCode("A");
			}
			if(dmFilter.getDevice()==null) {
				dmFilter.setDevice("L");
			}

			session.createNativeQuery(
					"BEGIN CAT_SEC_ABST_ALL_DT_CIR_DEV(:circd,:sectionCode, :device, TO_DATE(:fromDate, 'YYYY-MM-DD'), TO_DATE(:toDate, 'YYYY-MM-DD')); END;")
					.setParameter("circd", circleCode).setParameter("sectionCode", dmFilter.getSectionCode())
					.setParameter("device", dmFilter.getDevice()).setParameter("fromDate", formattedFromDate)
					.setParameter("toDate", formattedToDate).executeUpdate();

			session.createNativeQuery("BEGIN CAT_SEC_ABST_SINGLE_ALL; END;").executeUpdate();

			session.flush();
			session.getTransaction().commit();

			String hql = "SELECT c.*,d.NAME as DIVISION_NAME FROM TMP_CT_SEC c,SECTION s,DIVISION d WHERE c.SECCODE = s.ID AND d.ID =s.DIVISION_ID ";

			List<Object[]> results = session.createNativeQuery(hql).getResultList();
			List<TmpCtSecBean> circleSection = new ArrayList<>();

			for (Object[] row : results) {
				TmpCtSecBean report = new TmpCtSecBean();

				report.setCirCode((String) row[0]);
				report.setSecCode((String) row[1]);
				report.setSecName((String) row[2]);
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
				report.setCirName((String) row[27]);
				report.setRegCode((String) row[28]);
				report.setOtTot((BigDecimal) row[29]);
				report.setOtCom((BigDecimal) row[30]);
				report.setOtPen((BigDecimal) row[31]);
				report.setDivisionName((String) row[32]);

				circleSection.add(report);
			}

			sectionList = circleSection.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
					.collect(Collectors.toList());
			selectedCircleName = circleName;
			cameFromInsideReport = true;
			cameFromInsideSection = true;

			
			System.out.println("Section List size: " + sectionList.size());

			FacesContext.getCurrentInstance().getExternalContext().redirect("categoryAndDeviceSection.xhtml");
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
			
			//DEFAULT DATE FOR FILTER
			Calendar fromCal = Calendar.getInstance();
			fromCal.set(2023, 0, 1);
			if ((dmFilter.getFromDate() == null) && (dmFilter.getToDate() == null)) {

				dmFilter.setFromDate(fromCal.getTime());
				dmFilter.setToDate(new Date());
			}
			if ((dmFilter.getFromDate() != null) && (dmFilter.getToDate() == null)) {
				dmFilter.setToDate(new Date());
			}
			if ((dmFilter.getFromDate() == null) && (dmFilter.getToDate() != null)) {
				dmFilter.setFromDate(fromCal.getTime());
			}
			

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String formattedFromDate = sdf.format(dmFilter.getFromDate());
			String formattedToDate = sdf.format(dmFilter.getToDate());

			if(dmFilter.getDevice()==null) {
				dmFilter.setDevice("L");
			}

			session.createNativeQuery(
					"BEGIN CAT_SEC_ABST_ALL_DT_CIR_DEV(:circd,:sectionCode, :device, TO_DATE(:fromDate, 'YYYY-MM-DD'), TO_DATE(:toDate, 'YYYY-MM-DD')); END;")
					.setParameter("circd", circleCode)
					.setParameter("sectionCode", dmFilter.getSectionCode())
					.setParameter("device", dmFilter.getDevice())
					.setParameter("fromDate", formattedFromDate)
					.setParameter("toDate", formattedToDate).executeUpdate();

			session.createNativeQuery("BEGIN rev_cat_sec_abst_single_all(:circd,:sectionCode); END;")
					.setParameter("circd", circleCode)
					.setParameter("sectionCode", dmFilter.getSectionCode())
					.executeUpdate();

			session.flush();
			session.getTransaction().commit();

			
			String hql = "SELECT c.*, " +
		             "s.division_id AS DIVISION_ID, " +
		             "d.name AS DIVISION_NAME, " +
		             "s.sub_division_id AS SUB_DIVISION_ID " +
		             "FROM tmp_ct_sec c " +
		             "JOIN SECTION s ON s.id = c.seccode " +
		             "JOIN DIVISION d ON d.id = s.division_id";
			
			List<Object[]> results = session.createNativeQuery(hql).getResultList();
			List<TmpCtSecBean> circleSection = new ArrayList<>();

			for (Object[] row : results) {
				TmpCtSecBean report = new TmpCtSecBean();

				report.setCirCode((String) row[0]);
				report.setSecCode((String) row[1]);
				report.setSecName((String) row[2]);
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
				report.setCirName((String) row[27]);
				report.setRegCode((String) row[28]);
				report.setOtTot((BigDecimal) row[29]);
				report.setOtCom((BigDecimal) row[30]);
				report.setOtPen((BigDecimal) row[31]);

				report.setDivisionId((String) row[32].toString());
				report.setDivisionName((String) row[33]);
				report.setSubDivisionId((String) row[34].toString());

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
			
			cameFromInsideSection=true;
			selectedCircleName = circleName;

			System.out.println("Section List size: " + sectionList.size());

			FacesContext.getCurrentInstance().getExternalContext().redirect("categoryAndDeviceSection.xhtml");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in database operation");
		}
	}
	
	

	// TOTAL VALUE FOR CIRCLE REPORT
	private void computeTotals() {
		totalBlTot = reports.stream().map(TmpCtCirBean::getBlTot).reduce(BigDecimal.ZERO, BigDecimal::add);
		totalBlCom = reports.stream().map(TmpCtCirBean::getBlCom).reduce(BigDecimal.ZERO, BigDecimal::add);
		totalBlPen = reports.stream().map(TmpCtCirBean::getBlPen).reduce(BigDecimal.ZERO, BigDecimal::add);

		totalMeTot = reports.stream().map(TmpCtCirBean::getMeTot).reduce(BigDecimal.ZERO, BigDecimal::add);
		totalMeCom = reports.stream().map(TmpCtCirBean::getMeCom).reduce(BigDecimal.ZERO, BigDecimal::add);
		totalMePen = reports.stream().map(TmpCtCirBean::getMePen).reduce(BigDecimal.ZERO, BigDecimal::add);

		totalPfTot = reports.stream().map(TmpCtCirBean::getPfTot).reduce(BigDecimal.ZERO, BigDecimal::add);
		totalPfCom = reports.stream().map(TmpCtCirBean::getPfCom).reduce(BigDecimal.ZERO, BigDecimal::add);
		totalPfPen = reports.stream().map(TmpCtCirBean::getPfPen).reduce(BigDecimal.ZERO, BigDecimal::add);

		totalVfTot = reports.stream().map(TmpCtCirBean::getVfTot).reduce(BigDecimal.ZERO, BigDecimal::add);
		totalVfCom = reports.stream().map(TmpCtCirBean::getVfCom).reduce(BigDecimal.ZERO, BigDecimal::add);
		totalVfPen = reports.stream().map(TmpCtCirBean::getVfPen).reduce(BigDecimal.ZERO, BigDecimal::add);

		totalFiTot = reports.stream().map(TmpCtCirBean::getFiTot).reduce(BigDecimal.ZERO, BigDecimal::add);
		totalFiCom = reports.stream().map(TmpCtCirBean::getFiCom).reduce(BigDecimal.ZERO, BigDecimal::add);
		totalFiPen = reports.stream().map(TmpCtCirBean::getFiPen).reduce(BigDecimal.ZERO, BigDecimal::add);

		totalThTot = reports.stream().map(TmpCtCirBean::getThTot).reduce(BigDecimal.ZERO, BigDecimal::add);
		totalThCom = reports.stream().map(TmpCtCirBean::getThCom).reduce(BigDecimal.ZERO, BigDecimal::add);
		totalThPen = reports.stream().map(TmpCtCirBean::getThPen).reduce(BigDecimal.ZERO, BigDecimal::add);

		totalTeTot = reports.stream().map(TmpCtCirBean::getTeTot).reduce(BigDecimal.ZERO, BigDecimal::add);
		totalTeCom = reports.stream().map(TmpCtCirBean::getTeCom).reduce(BigDecimal.ZERO, BigDecimal::add);
		totalTePen = reports.stream().map(TmpCtCirBean::getTePen).reduce(BigDecimal.ZERO, BigDecimal::add);

		totalCsTot = reports.stream().map(TmpCtCirBean::getCsTot).reduce(BigDecimal.ZERO, BigDecimal::add);
		totalCsCom = reports.stream().map(TmpCtCirBean::getCsCom).reduce(BigDecimal.ZERO, BigDecimal::add);
		totalCsPen = reports.stream().map(TmpCtCirBean::getCsPen).reduce(BigDecimal.ZERO, BigDecimal::add);

		totalOtTot = reports.stream().map(TmpCtCirBean::getOtTot).reduce(BigDecimal.ZERO, BigDecimal::add);
		totalOtCom = reports.stream().map(TmpCtCirBean::getOtCom).reduce(BigDecimal.ZERO, BigDecimal::add);
		totalOtPen = reports.stream().map(TmpCtCirBean::getOtPen).reduce(BigDecimal.ZERO, BigDecimal::add);

		grandTotalRevd = totalBlTot.add(totalMeTot).add(totalPfTot).add(totalVfTot).add(totalFiTot).add(totalThTot)
				.add(totalTeTot).add(totalCsTot).add(totalOtTot);

		grandTotalComp = totalBlCom.add(totalMeCom).add(totalPfCom).add(totalVfCom).add(totalFiCom).add(totalThCom)
				.add(totalTeCom).add(totalCsCom).add(totalOtCom);

		grandTotalPend = totalBlPen.add(totalMePen).add(totalPfPen).add(totalVfPen).add(totalFiPen).add(totalThPen)
				.add(totalTePen).add(totalCsPen).add(totalOtPen);
	}

	
	// COMPLAINT LIST FOR SECTION
	@Transactional
	public void getComplaintListForSection(String secCode, String sectionName) throws IOException {
		try (Session session = sessionFactory.openSession()) {

			Timestamp fromDate = new Timestamp(dmFilter.getFromDate().getTime());
			Timestamp toDate = new Timestamp(dmFilter.getToDate().getTime());

			List<String> devices = new ArrayList<>();

			if (dmFilter.getDevice() != null) {
				switch (dmFilter.getDevice().toUpperCase()) {
				case "P":
					devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile"));
					break;
				case "W":
					devices.add("web");
					break;
				case "S":
					devices.add("SM");
					break;
				case "A":
					devices.addAll(Arrays.asList("admin", "FOC"));
					break;
				case "M":
					devices.add("MI");
					break;
				case "G":
					devices.add("MM");
					break;
				case "O":
					devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile", "web", "SM", "MI", "MM"));
					break;
				case "L":
					devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile", "web", "SM", "admin",
							"FOC", "MI", "MM"));
					break;
				default:
					throw new IllegalArgumentException("Invalid Device Type");
				}
			}

			Integer sectionCode = Integer.parseInt(secCode);

			String hql = "SELECT a.id, " + "to_char(a.created_on, 'dd-mm-yyyy-hh24:mi') AS Complaint_Date, "
					+ "DECODE(a.device, 'web', 'Web', 'FOC', 'FOC', 'admin', 'FOC', 'SM', 'Social Media', 'Android', 'Mobile', 'AMOB', 'Mobile', 'IMOB', 'Mobile', 'iOS', 'Mobile', 'mobile', 'Mobile', 'MI', 'Minnagam') AS Device, "
					+ "a.SERVICE_NUMBER AS Service_Number, " + "a.SERVICE_NAME AS Service_Name, "
					+ "a.SERVICE_ADDRESS AS Service_Address, "
					+ "b.mobile AS Contact_Number, k.name AS Complaint_Type, d.name AS subctyp, "
					+ "a.description AS Complaint_Description, "
					+ "DECODE(a.status_id, 0, 'Pending', 1, 'In Progress', 2, 'Completed') AS Complaint_Status, "
	                +"(CASE WHEN a.status_id=2 then TO_CHAR(a.updated_on, 'DD-MM-YYYY-HH24:MI') else '' end) AS Attended_Date, " 
	                +"(CASE WHEN a.status_id=2 then c.description else '' end) AS Attended_Remarks, " 
					 +"f.name AS Region, g.name AS Circle, h.name AS Division, i.name AS SubDivision, j.name AS Section "
					+ "FROM COMPLAINT a " + "JOIN PUBLIC_USER b ON a.user_id = b.id "
					+ "JOIN COMPLAINT_HISTORY c ON a.id = c.complaint_id AND a.status_id = c.status_id "
					+ "JOIN SUB_CATEGORY d ON a.sub_category_id = d.id "
					+ "JOIN CATEGORY k ON a.complaint_type = k.code " + "JOIN REGION f ON a.region_id = f.id "
					+ "JOIN CIRCLE g ON a.circle_id = g.id " + "JOIN DIVISION h ON a.division_id = h.id "
					+ "JOIN SUB_DIVISION i ON a.sub_division_id = i.id " + "JOIN SECTION j ON a.section_id = j.id "
					+ "WHERE a.SECTION_ID = :sectionCode AND a.DEVICE IN :devices "
					+ "AND a.created_on BETWEEN :fromDate AND :toDate";

			Query query = session.createNativeQuery(hql);
			query.setParameter("sectionCode", sectionCode);
			query.setParameter("fromDate", fromDate);
			query.setParameter("toDate", toDate);
			query.setParameter("devices", devices);

			List<Object[]> results = query.getResultList();

			complaintList = new ArrayList<ViewComplaintReportValueBean>();
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
				dto.setCircleName((String) row[14]);
				dto.setDivisionName((String) row[15]);
				dto.setSubDivisionName((String) row[16]);
				dto.setSectionName((String) row[17]);

				complaintList.add(dto);
			}

			selectedSectionName = sectionName;
			redirectFrom = "section";

			FacesContext.getCurrentInstance().getExternalContext().redirect("categoryAndDeviceComplaintList.xhtml");

		} catch (Exception e) {
			System.out.println("ERROR..........." + e);
		}

	}
	
	// COMPLAINT LIST FOR CIRCLE
	@Transactional
	public void getComplaintListForCircle() {

		List<ViewComplaintReportBean> complaintBeanList;

		try (Session session = sessionFactory.openSession()) {
			Timestamp fromDate = new Timestamp(dmFilter.getFromDate().getTime());
			Timestamp toDate = new Timestamp(dmFilter.getToDate().getTime());

			List<String> devices = new ArrayList<>();

			FacesContext facesContext = FacesContext.getCurrentInstance();
			String status = facesContext.getExternalContext().getRequestParameterMap().get("status");

			List<Integer> statusIDs = new ArrayList<>();

			if (status.equalsIgnoreCase("Received")) {
				statusIDs = Arrays.asList(CCMSConstants.PENDING, CCMSConstants.COMPLETED, CCMSConstants.IN_PROGRESS);
			} else if (status.equalsIgnoreCase("Completed")) {
				statusIDs = Arrays.asList(CCMSConstants.COMPLETED);
			} else if (status.equalsIgnoreCase("Pending")) {
				statusIDs = Arrays.asList(CCMSConstants.PENDING);
			}

			if (dmFilter.getDevice() != null) {
				switch (dmFilter.getDevice().toUpperCase()) {
				case "P":
					devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile"));
					break;
				case "W":
					devices.add("web");
					break;
				case "S":
					devices.add("SM");
					break;
				case "A":
					devices.addAll(Arrays.asList("admin", "FOC"));
					break;
				case "M":
					devices.add("MI");
					break;
				case "G":
					devices.add("MM");
					break;
				case "O":
					devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile", "web", "SM", "MI", "MM"));
					break;
				case "L":
					devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile", "web", "SM", "admin",
							"FOC", "MI", "MM"));
					break;
				default:
					throw new IllegalArgumentException("Invalid Device Type");
				}
			}

			String hql = "SELECT a.id, " + "to_char(a.created_on, 'dd-mm-yyyy-hh24:mi') AS Complaint_Date, "
					+ "DECODE(a.device, 'web', 'Web', 'FOC', 'FOC', 'admin', 'FOC', 'SM', 'Social Media', 'Android', 'Mobile', 'AMOB', 'Mobile', 'IMOB', 'Mobile', 'iOS', 'Mobile', 'mobile', 'Mobile', 'MI', 'Minnagam') AS Device, "
					+ "a.SERVICE_NUMBER AS Service_Number, " + "a.SERVICE_NAME AS Service_Name, "
					+ "a.SERVICE_ADDRESS AS Service_Address, "
					+ "b.mobile AS Contact_Number, k.name AS Complaint_Type, d.name AS subctyp, "
					+ "a.description AS Complaint_Description, "
					+ "DECODE(a.status_id, 0, 'Pending', 1, 'In Progress', 2, 'Completed') AS Complaint_Status, "
					+ "(CASE WHEN a.status_id=2 then TO_CHAR(a.updated_on, 'DD-MM-YYYY-HH24:MI') else '' end) AS Attended_Date, "
					+ "(CASE WHEN a.status_id=2 then c.description else '' end) AS Attended_Remarks, "
					+ "f.name AS Region, g.name AS Circle, h.name AS Division, i.name AS SubDivision, j.name AS Section "
					+ "FROM COMPLAINT a " + "JOIN PUBLIC_USER b ON a.user_id = b.id "
					+ "JOIN COMPLAINT_HISTORY c ON a.id = c.complaint_id AND a.status_id = c.status_id "
					+ "JOIN SUB_CATEGORY d ON a.sub_category_id = d.id "
					+ "JOIN CATEGORY k ON a.complaint_type = k.code " + "JOIN REGION f ON a.region_id = f.id "
					+ "JOIN CIRCLE g ON a.circle_id = g.id " + "JOIN DIVISION h ON a.division_id = h.id "
					+ "JOIN SUB_DIVISION i ON a.sub_division_id = i.id " + "JOIN SECTION j ON a.section_id = j.id "
					+ "WHERE a.STATUS_ID IN :statusIDs AND a.DEVICE IN :devices "
					+ "AND a.created_on BETWEEN :fromDate AND :toDate";

			Query query = session.createNativeQuery(hql);
			query.setParameter("fromDate", fromDate);
			query.setParameter("toDate", toDate);
			query.setParameter("statusIDs", statusIDs);
			query.setParameter("devices", devices);

			List<Object[]> results = query.getResultList();

			complaintList = new ArrayList<ViewComplaintReportValueBean>();
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
				dto.setCircleName((String) row[14]);
				dto.setDivisionName((String) row[15]);
				dto.setSubDivisionName((String) row[16]);
				dto.setSectionName((String) row[17]);

				complaintList.add(dto);
			}

			redirectFrom = "circle";
			selectedSectionName = null;

			FacesContext.getCurrentInstance().getExternalContext().redirect("categoryAndDeviceComplaintList.xhtml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// COMPLAINT'S DETAILED VIEW
	public void getComplaintDetailForAbstractReport() {

		try (Session session = sessionFactory.openSession()) {
			String complaintIdParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
					.get("complaintID");
			System.out.println("THE COMPLAINT IS IS CALLED ==============" + complaintIdParam);
			
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
		                "(CASE WHEN a.status_id=2 then c.description else '' end) AS Attended_Remarks, " +
		                "f.name AS Region, g.name AS Circle, h.name AS Division, i.name AS SubDivision, j.name AS Section, "+
		                "b.FIRST_NAME AS UserName, "+
		                "a.ALTERNATE_MOBILE_NO as RegisteredMobileNumber, "+
		                "to_char(fb.ENTRYDT, 'dd-mm-yyyy-hh24:mi') AS feedBackEntryDate, "+
		                "fb.REMARKS AS feedbackRemarks, "+
		                "fb.RATING AS feedbackRating, "+
		                "dm.DISTRIB_NAME As Distribution "+
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
		                "LEFT JOIN DISTRIB_MASTER dm ON dm.reg_no = f.ID AND dm.SCODE = j.code AND dm.DISTRIB_CODE = :distribCode "+
		                "WHERE a.id = :complaintIdParam " ;

			Query query = session.createNativeQuery(hql);
			query.setParameter("complaintIdParam", complaintIdParam);
			query.setParameter("distribCode", distribCode);


			List<Object[]> results = query.getResultList();

			selectedComplaintId = new ViewComplaintReportValueBean();
			for (Object[] row : results) {
				ViewComplaintReportValueBean dto = new ViewComplaintReportValueBean();
				dto.setComplaintId((BigDecimal) row[0]); // COMPLAINT ID
				dto.setCreatedOnFormatted((String) row[1]);
				dto.setDevice((String) row[2]); // Compl.Received through
				dto.setServiceNumber((String) row[3]);
				dto.setServiceName((String) row[4]);
				dto.setServiceAddress((String) row[5]); // COMPLAINANT ADDRESS
				dto.setMobile((String) row[6]); // COMPLAINANT MOBILE NUM
				dto.setComplaintType((String) row[7]); // Complaint Category
				dto.setSubCategoryName((String) row[8]); // Sub Category
				dto.setComplaintDescription((String) row[9]); // Description
				dto.setComplaintStatusValue((String) row[10]);
				dto.setAttendedDate((String) row[11]); // CLOSURE DATE
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

				selectedComplaintId = dto;
			}

			String transferHql = "SELECT to_char(TRF_ON, 'dd-mm-yyyy-hh24:mi'), TRF_USER, REMARKS "
					+ "FROM COMP_TRANSFER WHERE COMP_ID = :complaintId " + "ORDER BY TRF_ON";

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

			String qcHql = "SELECT to_char(QC_ON, 'dd-mm-yyyy-hh24:mi'), QC_STATUS, REMARKS "
					+ "FROM COMP_QC_DETAILS WHERE COMP_ID = :complaintId " + "ORDER BY QC_ON";
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

			System.out.println("THE SELECED COMPLAINT ID" + selectedComplaintId.getComplaintId());
			System.out.println("THE SELECED COMPLAINT ID" + selectedComplaintId.getDescription());

		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	// CIRCLE REPORT TO EXCEL DOWNLOAD
	public void exportToExcel(List<TmpCtCirBean> reports) throws IOException {
		HSSFWorkbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet("CategoryWise_Abstract_For_A_DateRange_And_Device_CircleWise");

		sheet.setColumnWidth(0, 2000); // S.NO
		sheet.setColumnWidth(1, 4000); // CIRCLE

		for (int i = 2; i < 31; i++) {
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
		
		CellStyle dataCellStyle = workbook.createCellStyle();
		dataCellStyle.setBorderBottom(BorderStyle.THIN);
		dataCellStyle.setBorderTop(BorderStyle.THIN);
		dataCellStyle.setBorderLeft(BorderStyle.THIN);
		dataCellStyle.setBorderRight(BorderStyle.THIN);
		dataCellStyle.setAlignment(HorizontalAlignment.LEFT);
		dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		

		Row headingRow = sheet.createRow(0);
		Cell headingCell = headingRow.createCell(0);
		headingCell.setCellValue("CATEGORY WISE ABSTRACT REPORT FOR A DATE RANGE AND DEVICE - CIRCLE WISE");
		headingCell.setCellStyle(headingStyle);
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 31));

		Row dateRow = sheet.createRow(1);
		Cell dateCell = dateRow.createCell(0);

		String device = dmFilter.getDevice();

		Map<String, String> deviceMap = new HashMap<>();
		deviceMap.put("L", "ALL");
		deviceMap.put("P", "MOBILE");
		deviceMap.put("W", "WEB");
		deviceMap.put("S", "SM");
		deviceMap.put("A", "FOC");
		deviceMap.put("M", "MINNAGAM");
		deviceMap.put("G", "MM");

		device = deviceMap.getOrDefault(device, "N/A");

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		String fromDateStr = dmFilter.getFromDate() != null ? dateFormat.format(dmFilter.getFromDate()) : "N/A";
		String toDateStr = dmFilter.getToDate() != null ? dateFormat.format(dmFilter.getToDate()) : "N/A";

		dateCell.setCellValue("From Date: " + fromDateStr + "  To Date: " + toDateStr + " Device :" + device);
		dateCell.setCellStyle(dateStyle);
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 31));

		Row headerRow1 = sheet.createRow(2);
		Row headerRow2 = sheet.createRow(3);

		String[] mainHeaders = { "S.NO", "CIRCLE", "Billing Related", "Meter Related", "Power Failure",
				"Voltage Fluctuation", "Fire", "Dangerous Pole", "Theft", "Conductor Snapping", "Others", "TOTAL" };

		String[] subHeaders = { "Revd.", "Comp.", "Pend." };

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

		int rowNum = 4;
		BigDecimal[] totalSums = new BigDecimal[30];
		Arrays.fill(totalSums, BigDecimal.ZERO);
		int serialNumber = 1;

		for (TmpCtCirBean report : reports) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(serialNumber++);
			row.getCell(0).setCellStyle(dataCellStyle);
			
			row.createCell(1).setCellValue(report.getCirname());
			row.getCell(1).setCellStyle(dataCellStyle);
			
			BigDecimal rowTotSum = BigDecimal.ZERO;
			BigDecimal rowCompSum = BigDecimal.ZERO;
			BigDecimal rowPendSum = BigDecimal.ZERO;

			int dataIndex = 2;

			BigDecimal[] values = { report.getBlTot(), report.getBlCom(), report.getBlPen(), report.getMeTot(),
					report.getMeCom(), report.getMePen(), report.getPfTot(), report.getPfCom(), report.getPfPen(),
					report.getVfTot(), report.getVfCom(), report.getVfPen(), report.getFiTot(), report.getFiCom(),
					report.getFiPen(), report.getThTot(), report.getThCom(), report.getThPen(), report.getTeTot(),
					report.getTeCom(), report.getTePen(), report.getCsTot(), report.getCsCom(), report.getCsPen(),
					report.getOtTot(), report.getOtCom(), report.getOtPen() };

			for (int i = 0; i < values.length; i += 3) {
				setCellValue(row, dataIndex++, values[i],dataCellStyle);
				setCellValue(row, dataIndex++, values[i + 1],dataCellStyle);
				setCellValue(row, dataIndex++, values[i + 2],dataCellStyle);

				rowTotSum = rowTotSum.add(values[i] != null ? values[i] : BigDecimal.ZERO);
				rowCompSum = rowCompSum.add(values[i + 1] != null ? values[i + 1] : BigDecimal.ZERO);
				rowPendSum = rowPendSum.add(values[i + 2] != null ? values[i + 2] : BigDecimal.ZERO);
			}

			setCellValue(row, dataIndex++, rowTotSum,dataCellStyle);
			setCellValue(row, dataIndex++, rowCompSum,dataCellStyle);
			setCellValue(row, dataIndex++, rowPendSum,dataCellStyle);

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
		totalRow.createCell(0).setCellValue("TOTAL");
		totalRow.createCell(1).setCellValue("");
		totalRow.getCell(1).setCellStyle(totalRowStyle);

		int totalDataIndex = 2;
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
					.name("CategoryWise_Abstract_For_A_DateRange_And_Device_CircleWise.xls")
					.contentType("application/vnd.ms-excel").stream(() -> inputStream).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// SECTION REPORT TO EXCEL DOWNLOAD
	public void exportSectionsToExcel(List<TmpCtSecBean> reports) throws IOException {
		HSSFWorkbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet("CategoryWise_Abstract_For_A_DateRange_And_Device_SectionWise");

		sheet.setColumnWidth(0, 2000); // S.NO
		sheet.setColumnWidth(0, 4000); // SECTION
		sheet.setColumnWidth(1, 4000); // DIVISION

		for (int i = 3; i <= 32; i++) {
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
		
		CellStyle dataCellStyle = workbook.createCellStyle();
		dataCellStyle.setBorderBottom(BorderStyle.THIN);
		dataCellStyle.setBorderTop(BorderStyle.THIN);
		dataCellStyle.setBorderLeft(BorderStyle.THIN);
		dataCellStyle.setBorderRight(BorderStyle.THIN);
		dataCellStyle.setAlignment(HorizontalAlignment.LEFT);
		dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		Row headingRow = sheet.createRow(0);
		Cell headingCell = headingRow.createCell(0);
		headingCell.setCellValue("CATEGORY WISE ABSTRACT REPORT FOR A DATE RANGE AND DEVICE - SECTION WISE");
		headingCell.setCellStyle(headingStyle);
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 32));

		Row dateRow = sheet.createRow(1);
		Cell dateCell = dateRow.createCell(0);

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		String fromDateStr = dmFilter.getFromDate() != null ? dateFormat.format(dmFilter.getFromDate()) : "N/A";
		String toDateStr = dmFilter.getToDate() != null ? dateFormat.format(dmFilter.getToDate()) : "N/A";

		String device = dmFilter.getDevice();

		Map<String, String> deviceMap = new HashMap<>();
		deviceMap.put("L", "ALL");
		deviceMap.put("P", "MOBILE");
		deviceMap.put("W", "WEB");
		deviceMap.put("S", "SM");
		deviceMap.put("A", "FOC");
		deviceMap.put("M", "MINNAGAM");
		deviceMap.put("G", "MM");

		device = deviceMap.getOrDefault(device, "N/A");

		dateCell.setCellValue("From Date: " + fromDateStr + "  To Date: " + toDateStr + "  CIRCLE :"
				+ selectedCircleName + "  Device :" + device);
		dateCell.setCellStyle(dateStyle);
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 32));

		Row headerRow1 = sheet.createRow(2);
		Row headerRow2 = sheet.createRow(3);

		String[] mainHeaders = { "S.NO","SECTION", "DIVISION", "Billing Related", "Meter Related", "Power Failure",
				"Voltage Fluctuation", "Fire", "Dangerous Pole", "Theft", "Conductor Snapping", "Others", "TOTAL" };

		String[] subHeaders = { "Revd.", "Comp.", "Pend." };

		int colIndex = 0;
		for (String mainHeader : mainHeaders) {
			Cell cell = headerRow1.createCell(colIndex);
			cell.setCellValue(mainHeader);
			cell.setCellStyle(headerStyle);

			if (!mainHeader.equals("S.NO") &&!mainHeader.equals("SECTION") && !mainHeader.equals("DIVISION")) {
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

		int rowNum = 4;
		BigDecimal[] totalSums = new BigDecimal[30];
		Arrays.fill(totalSums, BigDecimal.ZERO);

		int serialNumber=1;
		for (TmpCtSecBean report : reports) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(String.valueOf(serialNumber++));
			row.getCell(0).setCellStyle(dataCellStyle);
			row.createCell(1).setCellValue(report.getSecName());
			row.getCell(1).setCellStyle(dataCellStyle);
			row.createCell(2).setCellValue(report.getDivisionName());
			row.getCell(2).setCellStyle(dataCellStyle);

			BigDecimal rowTotSum = BigDecimal.ZERO;
			BigDecimal rowCompSum = BigDecimal.ZERO;
			BigDecimal rowPendSum = BigDecimal.ZERO;

			int dataIndex = 3;

			BigDecimal[] values = { report.getBlTot(), report.getBlCom(), report.getBlPen(), report.getMeTot(),
					report.getMeCom(), report.getMePen(), report.getPfTot(), report.getPfCom(), report.getPfPen(),
					report.getVfTot(), report.getVfCom(), report.getVfPen(), report.getFiTot(), report.getFiCom(),
					report.getFiPen(), report.getThTot(), report.getThCom(), report.getThPen(), report.getTeTot(),
					report.getTeCom(), report.getTePen(), report.getCsTot(), report.getCsCom(), report.getCsPen(),
					report.getOtTot(), report.getOtCom(), report.getOtPen() };

			for (int i = 0; i < values.length; i += 3) {
				setCellValue(row, dataIndex++, values[i],dataCellStyle);
				setCellValue(row, dataIndex++, values[i + 1],dataCellStyle);
				setCellValue(row, dataIndex++, values[i + 2],dataCellStyle);

				rowTotSum = rowTotSum.add(values[i] != null ? values[i] : BigDecimal.ZERO);
				rowCompSum = rowCompSum.add(values[i + 1] != null ? values[i + 1] : BigDecimal.ZERO);
				rowPendSum = rowPendSum.add(values[i + 2] != null ? values[i + 2] : BigDecimal.ZERO);
			}

			setCellValue(row, dataIndex++, rowTotSum,dataCellStyle);
			setCellValue(row, dataIndex++, rowCompSum,dataCellStyle);
			setCellValue(row, dataIndex++, rowPendSum,dataCellStyle);

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
		totalRow.createCell(0).setCellValue("TOTAL");
		totalRow.createCell(1).setCellValue("");
		totalRow.getCell(1).setCellStyle(totalRowStyle);
		totalRow.createCell(2).setCellValue("");
		totalRow.getCell(2).setCellStyle(totalRowStyle);

		int totalDataIndex = 3;
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
					.name("CategoryWise_Abstract_For_A_DateRange_And_Device_SectionWise.xls")
					.contentType("application/vnd.ms-excel").stream(() -> inputStream).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setCellValue(Row row, int columnIndex, Number value,CellStyle style) {
		if (value != null) {
			if (value instanceof BigDecimal) {
				row.createCell(columnIndex).setCellValue(value.doubleValue());
				row.getCell(columnIndex).setCellStyle(style);
			} else if (value instanceof Integer || value instanceof Long || value instanceof Double
					|| value instanceof Float) {
				row.createCell(columnIndex).setCellValue(value.doubleValue());
				row.getCell(columnIndex).setCellStyle(style);
			} else {
				row.createCell(columnIndex).setCellValue(value.toString());
				row.getCell(columnIndex).setCellStyle(style);
			}
		} else {
			row.createCell(columnIndex).setCellValue("");
			row.getCell(columnIndex).setCellStyle(style);
		}
	}

	// CIRCLE REPORT TO PDF DOWNLOAD
	public void exportToPdf(List<TmpCtCirBean> circleList) throws IOException {
		final int CATEGORY = 9;
		final int SUB_COLUMNS = 3;
		final int COLUMN_COUNT = 1+ 1 + (CATEGORY * SUB_COLUMNS) + 3;

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
		String toDate = sdf.format(dmFilter.getToDate());

		Document document = new Document(PageSize.A2.rotate());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try {
			PdfWriter.getInstance(document, outputStream);
			document.open();

			PdfPTable table = new PdfPTable(COLUMN_COUNT);
			table.setWidthPercentage(100);
			table.setSpacingBefore(10f);

			Paragraph title = new Paragraph("CATEGORY WISE ABSTRACT REPORT FOR A DATE RANGE AND DEVICE - CIRCLE WISE ",
					TITLE_FONT);
			title.setAlignment(Element.ALIGN_CENTER);
			document.add(title);

			Paragraph subTitle = new Paragraph("FROM : " + fromDate + " TO " + toDate + " | " + "DEVICE: " + device);
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

			addMergedHeaderCell(table, "S.NO", HEADER_FONT, 1, 2);
			addMergedHeaderCell(table, "CIRCLE", HEADER_FONT, 1, 2);
			String[] categoryHeaders = { "Billing Releated", "Meter Releated", "Power Failure", "Voltage Fluctuation",
					"Fire", "Dangerous Pole", "Theft", "Conductor Snapping", "Others" };
			for (int category = 0; category < categoryHeaders.length; category++) {
				addMergedHeaderCell(table, categoryHeaders[category], HEADER_FONT, 3, 1);
			}
			addMergedHeaderCell(table, "TOTAL", HEADER_FONT, 3, 1);

			// SUB HEADER
			String[] subHeaders = { "Revd.", "Comp.", "Pend." };
			for (int i = 0; i < CATEGORY + 1; i++) {
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

			int serialNumber=1;
			for (TmpCtCirBean report : circleList) {
				table.addCell(createDataCell(String.valueOf(serialNumber++), DATA_FONT, Element.ALIGN_LEFT));
				table.addCell(createDataCell(report.getCirname(), DATA_FONT, Element.ALIGN_LEFT));

				addCategoryData(table, report.getBlTot(), report.getBlCom(), report.getBlPen(), DATA_FONT);
				addCategoryData(table, report.getMeTot(), report.getMeCom(), report.getMePen(), DATA_FONT);
				addCategoryData(table, report.getPfTot(), report.getPfCom(), report.getPfPen(), DATA_FONT);
				addCategoryData(table, report.getVfTot(), report.getVfCom(), report.getVfPen(), DATA_FONT);

				addCategoryData(table, report.getFiTot(), report.getFiCom(), report.getFiPen(), DATA_FONT);
				addCategoryData(table, report.getThTot(), report.getThCom(), report.getThPen(), DATA_FONT);
				addCategoryData(table, report.getTeTot(), report.getTeCom(), report.getTePen(), DATA_FONT);
				addCategoryData(table, report.getCsTot(), report.getCsCom(), report.getCsPen(), DATA_FONT);

				addCategoryData(table, report.getOtTot(), report.getOtCom(), report.getOtPen(), DATA_FONT);

				BigDecimal total = report.getBlTot().add(report.getMeTot()).add(report.getPfTot())
						.add(report.getVfTot()).add(report.getFiTot()).add(report.getThTot()).add(report.getTeTot())
						.add(report.getCsTot()).add(report.getOtTot());

				BigDecimal totalCpl = report.getBlCom().add(report.getMeCom()).add(report.getPfCom())
						.add(report.getVfCom()).add(report.getFiCom()).add(report.getThCom()).add(report.getTeCom())
						.add(report.getCsCom()).add(report.getOtCom());

				BigDecimal pend = report.getBlPen().add(report.getMePen()).add(report.getPfPen()).add(report.getVfPen())
						.add(report.getFiPen()).add(report.getThPen()).add(report.getTePen()).add(report.getCsPen())
						.add(report.getOtPen());

				grandTot = grandTot.add(total);
				grandTotCpl = grandTotCpl.add(totalCpl);
				grandTotPend = grandTotPend.add(pend);

				addCategoryData(table, total, totalCpl, pend, TOTAL_FONT);
			}
			PdfPCell footerLabel = new PdfPCell(new Phrase("TOTAL", TOTAL_FONT));
			footerLabel.setBackgroundColor(BaseColor.LIGHT_GRAY);
			footerLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
			footerLabel.setColspan(2);
			table.addCell(footerLabel);

			addCategoryData(table, circleList.stream().map(r -> r.getBlTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
					circleList.stream().map(r -> r.getBlCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
					circleList.stream().map(r -> r.getBlPen()).reduce(BigDecimal.ZERO, BigDecimal::add), TOTAL_FONT);

			addCategoryData(table, circleList.stream().map(r -> r.getMeTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
					circleList.stream().map(r -> r.getMeCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
					circleList.stream().map(r -> r.getMePen()).reduce(BigDecimal.ZERO, BigDecimal::add), TOTAL_FONT);

			addCategoryData(table, circleList.stream().map(r -> r.getPfTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
					circleList.stream().map(r -> r.getPfCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
					circleList.stream().map(r -> r.getPfPen()).reduce(BigDecimal.ZERO, BigDecimal::add), TOTAL_FONT);

			addCategoryData(table, circleList.stream().map(r -> r.getVfTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
					circleList.stream().map(r -> r.getVfCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
					circleList.stream().map(r -> r.getVfPen()).reduce(BigDecimal.ZERO, BigDecimal::add), TOTAL_FONT);

			addCategoryData(table, circleList.stream().map(r -> r.getFiTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
					circleList.stream().map(r -> r.getFiCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
					circleList.stream().map(r -> r.getFiPen()).reduce(BigDecimal.ZERO, BigDecimal::add), TOTAL_FONT);

			addCategoryData(table, circleList.stream().map(r -> r.getThTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
					circleList.stream().map(r -> r.getThCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
					circleList.stream().map(r -> r.getThPen()).reduce(BigDecimal.ZERO, BigDecimal::add), TOTAL_FONT);

			addCategoryData(table, circleList.stream().map(r -> r.getTeTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
					circleList.stream().map(r -> r.getTeCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
					circleList.stream().map(r -> r.getTePen()).reduce(BigDecimal.ZERO, BigDecimal::add), TOTAL_FONT);

			addCategoryData(table, circleList.stream().map(r -> r.getCsTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
					circleList.stream().map(r -> r.getCsCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
					circleList.stream().map(r -> r.getCsPen()).reduce(BigDecimal.ZERO, BigDecimal::add), TOTAL_FONT);

			addCategoryData(table, circleList.stream().map(r -> r.getOtTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
					circleList.stream().map(r -> r.getOtCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
					circleList.stream().map(r -> r.getOtPen()).reduce(BigDecimal.ZERO, BigDecimal::add), TOTAL_FONT);

			addCategoryData(table, grandTot, grandTotCpl, grandTotPend, TOTAL_FONT);

			document.add(table);
			document.close();

			pdfFile = DefaultStreamedContent.builder().contentType("application/pdf")
					.name("CategoryWise_Abstract_For_A_DateRange_And_Device_CircleWise.pdf")
					.stream(() -> new ByteArrayInputStream(outputStream.toByteArray())).build();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// SECTION REPORT TO PDF DOWNLOAD
	public void exportSectionsToPdf(List<TmpCtSecBean> sectionList) throws IOException {
		final int CATEGORY = 9;
		final int SUB_COLUMNS = 3;
		final int COLUMN_COUNT = 1+ 2 + (CATEGORY * SUB_COLUMNS) + 3;

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
		String toDate = sdf.format(dmFilter.getToDate());

		Document document = new Document(PageSize.A2.rotate());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try {
			PdfWriter.getInstance(document, outputStream);
			document.open();

			PdfPTable table = new PdfPTable(COLUMN_COUNT);
			table.setWidthPercentage(100);
			table.setSpacingBefore(10f);

			Paragraph title = new Paragraph("CATEGORY WISE ABSTRACT REPORT FOR A DATE RANGE AND DEVICE - SECTION WISE",
					TITLE_FONT);
			title.setAlignment(Element.ALIGN_CENTER);
			document.add(title);

			Paragraph subTitle = new Paragraph("CIRCLE :" + selectedCircleName + " | " + "FROM : " + fromDate + " TO "
					+ toDate + " | " + "Device: " + device);
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
			String[] categoryHeaders = { "Billing Releated", "Meter Releated", "Power Failure", "Voltage Fluctuation",
					"Fire", "Dangerous Pole", "Theft", "Conductor Snapping", "Others" };
			for (int category = 0; category < categoryHeaders.length; category++) {
				addMergedHeaderCell(table, categoryHeaders[category], HEADER_FONT, 3, 1);
			}
			addMergedHeaderCell(table, "TOTAL", HEADER_FONT, 3, 1);

			// SUB HEADER
			String[] subHeaders = { "Revd.", "Comp.", "Pend." };
			for (int i = 0; i < CATEGORY + 1; i++) {
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

			int serialNumber =1;
			for (TmpCtSecBean report : sectionList) {
				table.addCell(createDataCell(String.valueOf(serialNumber++), DATA_FONT, Element.ALIGN_LEFT));
				table.addCell(createDataCell(report.getSecName(), DATA_FONT, Element.ALIGN_LEFT));
				table.addCell(createDataCell(report.getDivisionName(), DATA_FONT, Element.ALIGN_LEFT));

				addCategoryData(table, report.getBlTot(), report.getBlCom(), report.getBlPen(), DATA_FONT);
				addCategoryData(table, report.getMeTot(), report.getMeCom(), report.getMePen(), DATA_FONT);
				addCategoryData(table, report.getPfTot(), report.getPfCom(), report.getPfPen(), DATA_FONT);
				addCategoryData(table, report.getVfTot(), report.getVfCom(), report.getVfPen(), DATA_FONT);

				addCategoryData(table, report.getFiTot(), report.getFiCom(), report.getFiPen(), DATA_FONT);
				addCategoryData(table, report.getThTot(), report.getThCom(), report.getThPen(), DATA_FONT);
				addCategoryData(table, report.getTeTot(), report.getTeCom(), report.getTePen(), DATA_FONT);
				addCategoryData(table, report.getCsTot(), report.getCsCom(), report.getCsPen(), DATA_FONT);

				addCategoryData(table, report.getOtTot(), report.getOtCom(), report.getOtPen(), DATA_FONT);

				BigDecimal total = report.getBlTot().add(report.getMeTot()).add(report.getPfTot())
						.add(report.getVfTot()).add(report.getFiTot()).add(report.getThTot()).add(report.getTeTot())
						.add(report.getCsTot()).add(report.getOtTot());

				BigDecimal totalCpl = report.getBlCom().add(report.getMeCom()).add(report.getPfCom())
						.add(report.getVfCom()).add(report.getFiCom()).add(report.getThCom()).add(report.getTeCom())
						.add(report.getCsCom()).add(report.getOtCom());

				BigDecimal pend = report.getBlPen().add(report.getMePen()).add(report.getPfPen()).add(report.getVfPen())
						.add(report.getFiPen()).add(report.getThPen()).add(report.getTePen()).add(report.getCsPen())
						.add(report.getOtPen());

				grandTot = grandTot.add(total);
				grandTotCpl = grandTotCpl.add(totalCpl);
				grandTotPend = grandTotPend.add(pend);

				addCategoryData(table, total, totalCpl, pend, TOTAL_FONT);
			}
			PdfPCell footerLabel = new PdfPCell(new Phrase("TOTAL", TOTAL_FONT));
			footerLabel.setBackgroundColor(BaseColor.LIGHT_GRAY);
			footerLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
			footerLabel.setColspan(3);
			table.addCell(footerLabel);

			addCategoryData(table, sectionList.stream().map(r -> r.getBlTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
					sectionList.stream().map(r -> r.getBlCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
					sectionList.stream().map(r -> r.getBlPen()).reduce(BigDecimal.ZERO, BigDecimal::add), TOTAL_FONT);

			addCategoryData(table, sectionList.stream().map(r -> r.getMeTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
					sectionList.stream().map(r -> r.getMeCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
					sectionList.stream().map(r -> r.getMePen()).reduce(BigDecimal.ZERO, BigDecimal::add), TOTAL_FONT);

			addCategoryData(table, sectionList.stream().map(r -> r.getPfTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
					sectionList.stream().map(r -> r.getPfCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
					sectionList.stream().map(r -> r.getPfPen()).reduce(BigDecimal.ZERO, BigDecimal::add), TOTAL_FONT);

			addCategoryData(table, sectionList.stream().map(r -> r.getVfTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
					sectionList.stream().map(r -> r.getVfCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
					sectionList.stream().map(r -> r.getVfPen()).reduce(BigDecimal.ZERO, BigDecimal::add), TOTAL_FONT);

			addCategoryData(table, sectionList.stream().map(r -> r.getFiTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
					sectionList.stream().map(r -> r.getFiCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
					sectionList.stream().map(r -> r.getFiPen()).reduce(BigDecimal.ZERO, BigDecimal::add), TOTAL_FONT);

			addCategoryData(table, sectionList.stream().map(r -> r.getThTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
					sectionList.stream().map(r -> r.getThCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
					sectionList.stream().map(r -> r.getThPen()).reduce(BigDecimal.ZERO, BigDecimal::add), TOTAL_FONT);

			addCategoryData(table, sectionList.stream().map(r -> r.getTeTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
					sectionList.stream().map(r -> r.getTeCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
					sectionList.stream().map(r -> r.getTePen()).reduce(BigDecimal.ZERO, BigDecimal::add), TOTAL_FONT);

			addCategoryData(table, sectionList.stream().map(r -> r.getCsTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
					sectionList.stream().map(r -> r.getCsCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
					sectionList.stream().map(r -> r.getCsPen()).reduce(BigDecimal.ZERO, BigDecimal::add), TOTAL_FONT);

			addCategoryData(table, sectionList.stream().map(r -> r.getOtTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
					sectionList.stream().map(r -> r.getOtCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
					sectionList.stream().map(r -> r.getOtPen()).reduce(BigDecimal.ZERO, BigDecimal::add), TOTAL_FONT);

			addCategoryData(table, grandTot, grandTotCpl, grandTotPend, TOTAL_FONT);

			document.add(table);
			document.close();

			pdfFile = DefaultStreamedContent.builder().contentType("application/pdf")
					.name("CategoryWise_Abstract_For_A_DateRange_And_Device_SectionWise.pdf")
					.stream(() -> new ByteArrayInputStream(outputStream.toByteArray())).build();

		} catch (Exception e) {
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

	private void addCategoryData(PdfPTable table, BigDecimal revd, BigDecimal cpl, BigDecimal pend, Font font) {
		table.addCell(createDataCell(revd.toString(), font, Element.ALIGN_CENTER));
		table.addCell(createDataCell(cpl.toString(), font, Element.ALIGN_CENTER));
		table.addCell(createDataCell(pend.toString(), font, Element.ALIGN_CENTER));
	}

	

	public DataModel getDmFilter() {
		return dmFilter;
	}

	public void setDmFilter(DataModel dmFilter) {
		this.dmFilter = dmFilter;
	}

	public List<TmpCtCirBean> getReports() {
		return reports;
	}

	public void setReports(List<TmpCtCirBean> reports) {
		this.reports = reports;
	}

	public List<TmpCtSecBean> getSectionList() {
		return sectionList;
	}

	public void setSectionList(List<TmpCtSecBean> sectionList) {
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

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public BigDecimal getTotalBlTot() {
		return totalBlTot;
	}

	public void setTotalBlTot(BigDecimal totalBlTot) {
		this.totalBlTot = totalBlTot;
	}

	public BigDecimal getTotalBlCom() {
		return totalBlCom;
	}

	public void setTotalBlCom(BigDecimal totalBlCom) {
		this.totalBlCom = totalBlCom;
	}

	public BigDecimal getTotalBlPen() {
		return totalBlPen;
	}

	public void setTotalBlPen(BigDecimal totalBlPen) {
		this.totalBlPen = totalBlPen;
	}

	public BigDecimal getTotalMeTot() {
		return totalMeTot;
	}

	public void setTotalMeTot(BigDecimal totalMeTot) {
		this.totalMeTot = totalMeTot;
	}

	public BigDecimal getTotalMeCom() {
		return totalMeCom;
	}

	public void setTotalMeCom(BigDecimal totalMeCom) {
		this.totalMeCom = totalMeCom;
	}

	public BigDecimal getTotalMePen() {
		return totalMePen;
	}

	public void setTotalMePen(BigDecimal totalMePen) {
		this.totalMePen = totalMePen;
	}

	public BigDecimal getTotalPfTot() {
		return totalPfTot;
	}

	public void setTotalPfTot(BigDecimal totalPfTot) {
		this.totalPfTot = totalPfTot;
	}

	public BigDecimal getTotalPfCom() {
		return totalPfCom;
	}

	public void setTotalPfCom(BigDecimal totalPfCom) {
		this.totalPfCom = totalPfCom;
	}

	public BigDecimal getTotalPfPen() {
		return totalPfPen;
	}

	public void setTotalPfPen(BigDecimal totalPfPen) {
		this.totalPfPen = totalPfPen;
	}

	public BigDecimal getTotalVfTot() {
		return totalVfTot;
	}

	public void setTotalVfTot(BigDecimal totalVfTot) {
		this.totalVfTot = totalVfTot;
	}

	public BigDecimal getTotalVfCom() {
		return totalVfCom;
	}

	public void setTotalVfCom(BigDecimal totalVfCom) {
		this.totalVfCom = totalVfCom;
	}

	public BigDecimal getTotalVfPen() {
		return totalVfPen;
	}

	public void setTotalVfPen(BigDecimal totalVfPen) {
		this.totalVfPen = totalVfPen;
	}

	public BigDecimal getTotalFiTot() {
		return totalFiTot;
	}

	public void setTotalFiTot(BigDecimal totalFiTot) {
		this.totalFiTot = totalFiTot;
	}

	public BigDecimal getTotalFiCom() {
		return totalFiCom;
	}

	public void setTotalFiCom(BigDecimal totalFiCom) {
		this.totalFiCom = totalFiCom;
	}

	public BigDecimal getTotalFiPen() {
		return totalFiPen;
	}

	public void setTotalFiPen(BigDecimal totalFiPen) {
		this.totalFiPen = totalFiPen;
	}

	public BigDecimal getTotalThTot() {
		return totalThTot;
	}

	public void setTotalThTot(BigDecimal totalThTot) {
		this.totalThTot = totalThTot;
	}

	public BigDecimal getTotalThCom() {
		return totalThCom;
	}

	public void setTotalThCom(BigDecimal totalThCom) {
		this.totalThCom = totalThCom;
	}

	public BigDecimal getTotalThPen() {
		return totalThPen;
	}

	public void setTotalThPen(BigDecimal totalThPen) {
		this.totalThPen = totalThPen;
	}

	public BigDecimal getTotalTeTot() {
		return totalTeTot;
	}

	public void setTotalTeTot(BigDecimal totalTeTot) {
		this.totalTeTot = totalTeTot;
	}

	public BigDecimal getTotalTeCom() {
		return totalTeCom;
	}

	public void setTotalTeCom(BigDecimal totalTeCom) {
		this.totalTeCom = totalTeCom;
	}

	public BigDecimal getTotalTePen() {
		return totalTePen;
	}

	public void setTotalTePen(BigDecimal totalTePen) {
		this.totalTePen = totalTePen;
	}

	public BigDecimal getTotalCsTot() {
		return totalCsTot;
	}

	public void setTotalCsTot(BigDecimal totalCsTot) {
		this.totalCsTot = totalCsTot;
	}

	public BigDecimal getTotalCsCom() {
		return totalCsCom;
	}

	public void setTotalCsCom(BigDecimal totalCsCom) {
		this.totalCsCom = totalCsCom;
	}

	public BigDecimal getTotalCsPen() {
		return totalCsPen;
	}

	public void setTotalCsPen(BigDecimal totalCsPen) {
		this.totalCsPen = totalCsPen;
	}

	public BigDecimal getTotalOtTot() {
		return totalOtTot;
	}

	public void setTotalOtTot(BigDecimal totalOtTot) {
		this.totalOtTot = totalOtTot;
	}

	public BigDecimal getTotalOtCom() {
		return totalOtCom;
	}

	public void setTotalOtCom(BigDecimal totalOtCom) {
		this.totalOtCom = totalOtCom;
	}

	public BigDecimal getTotalOtPen() {
		return totalOtPen;
	}

	public void setTotalOtPen(BigDecimal totalOtPen) {
		this.totalOtPen = totalOtPen;
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
	
	public List<CompDeviceValueBean> getDevices() {
		return devices;
	}

	public void setDevices(List<CompDeviceValueBean> devices) {
		this.devices = devices;
	}

	public Authentication getAuthentication() {
		return authentication;
	}

	public void setAuthentication(Authentication authentication) {
		this.authentication = authentication;
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
