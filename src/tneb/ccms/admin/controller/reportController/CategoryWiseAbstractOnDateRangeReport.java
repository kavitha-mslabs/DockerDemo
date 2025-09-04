package tneb.ccms.admin.controller.reportController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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

import tneb.ccms.admin.model.ViewComplaintReportBean;
import tneb.ccms.admin.util.CCMSConstants;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.valuebeans.AdminUserValueBean;
import tneb.ccms.admin.valuebeans.TmpCtCirBean;
import tneb.ccms.admin.valuebeans.TmpCtSecBean;
import tneb.ccms.admin.valuebeans.ViewComplaintReportValueBean;

import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;

@Named
@ViewScoped
public class CategoryWiseAbstractOnDateRangeReport implements Serializable {

	private static final long serialVersionUID = 1L;
	private SessionFactory sessionFactory;
	private boolean cameFromInsideReport = false;
	DataModel dmFilter;
	List<TmpCtCirBean> reports = new ArrayList<>();
	List<TmpCtSecBean> sectionList = new ArrayList<>();
	StreamedContent excelFile;
	StreamedContent pdfFile;
	List<ViewComplaintReportValueBean> complaintList = new ArrayList<>();
	String selectedCircleName =null;
	String selectedSectionName = null;
	String redirectFrom;
	ViewComplaintReportValueBean selectedComplaintId;
	private Date currentDate = new Date();
	AdminUserValueBean adminUserValueBean;

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
		System.out.println("INITIALIZED CATEGORY WISE COMPLAINTS - ABSTRACT REPORT - AS ON DATE RANGE");
		sessionFactory = HibernateUtil.getSessionFactory();
		dmFilter = new DataModel();
		
		HttpSession httpSession = (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false);
		
		adminUserValueBean = (AdminUserValueBean) 
				httpSession.getAttribute("sessionAdminValueBean");
		
		selectedCircleName = (String) httpSession.getAttribute("loggedInCircleName");
	}

	// REFERSH BUTTON
	public void clearCircleReportData() {
		dmFilter = new DataModel();
		dmFilter.setFromDate(null);
		dmFilter.setToDate(null);
		reports = new ArrayList<TmpCtCirBean>();
		
	}
	
	// REFRESH REPORT  
	public void resetIfNeeded() {
        if (!FacesContext.getCurrentInstance().isPostback() && !cameFromInsideReport) {
        	reports = null;
        	dmFilter.setFromDate(null);
        	dmFilter.setToDate(null);
        }
        cameFromInsideReport = false; 
    }

public void updateLoginWiseFilters() {
    
    
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false);
		
		AdminUserValueBean adminUserValueBean = (AdminUserValueBean) 
				session.getAttribute("sessionAdminValueBean");
		
        Integer loggedInCircleId = (Integer) session.getAttribute("loggedInCircleId");
        
        
        Integer roleId = adminUserValueBean.getRoleId();
        
        // REGION
        if(roleId >= 5 && roleId <= 9) {      
        	dmFilter.setRegionCode(adminUserValueBean.getRegionId().toString());
        	dmFilter.setCircleCode("A");
        }
        //CIRCLE
        else if(roleId==4) {
        	dmFilter.setRegionCode(adminUserValueBean.getRegionId().toString());
        	dmFilter.setCircleCode(loggedInCircleId.toString());
        }
        else {
        	dmFilter.setRegionCode("A");
        	dmFilter.setCircleCode("A");
        }
        
	}

	// CIRCLE REPORT
	@Transactional
	public void searchCategoryWiseComplaintsAbstractReportOnDateRange() {
		
		updateLoginWiseFilters();
		
		Calendar fromCal = Calendar.getInstance();
		fromCal.set(2024,0, 1);
		if((dmFilter.getFromDate()==null) && (dmFilter.getToDate()==null)) {
			
			dmFilter.setFromDate(fromCal.getTime());
			dmFilter.setToDate(new Date());
		}
		if((dmFilter.getFromDate()!=null) && (dmFilter.getToDate()==null)) {
			dmFilter.setToDate(new Date());
		}
		if((dmFilter.getFromDate()==null) && (dmFilter.getToDate()!=null)) {
			dmFilter.setFromDate(fromCal.getTime());
		}
		if (dmFilter.getFromDate().after(dmFilter.getToDate())) {
		    	
		    	FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
		                "ERROR", "From Date Cannot Be After To Date");
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
			
			System.out.println("FROM -------------"+formattedFromDate);
			System.out.println("TO----------------"+formattedToDate);

			session.createNativeQuery(
					"BEGIN CAT_CIR_ABST_ALL_DT(TO_DATE(:fromDate, 'YYYY-MM-DD'), TO_DATE(:toDate, 'YYYY-MM-DD')); END;")
					.setParameter("fromDate", formattedFromDate)
					.setParameter("toDate", formattedToDate)
					.executeUpdate();

			session.createNativeQuery("BEGIN CAT_CIR_ABST_SINGLE_ALL; END;").executeUpdate();

			session.flush();
			session.getTransaction().commit();

			fetchReports(session);
			computeTotals();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in database operation");
		}
	}
	

	@Transactional
	private void fetchReports(Session session) {
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
			
			if(dmFilter.getRegionCode().equals("A")) {
				reports = circleList;
			}else {
				reports = circleList.stream().filter(cir -> cir.getRegcode().equals(adminUserValueBean.getRegionId().toString())).collect(Collectors.toList());
			}

			System.out.println("COUNT: " + reports.size());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in fetching report");
		}
	}
	

    // BACK BUTTON 		
	public void returnToCircleReportOnDateRange() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().redirect("categoryWiseCircleReportDateRange.xhtml");	
	}

	

    // SECTION REPORT
	@Transactional
	public void fetchReportByCircle(String circleCode,String circleName) {

		Session session = null;
		try {
			session = sessionFactory.openSession();
			session.beginTransaction();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String formattedFromDate = sdf.format(dmFilter.getFromDate());
			String formattedToDate = sdf.format(dmFilter.getToDate());

			session.createNativeQuery(
					"BEGIN CAT_SEC_ABST_ALL_DT_CIR(:circd, TO_DATE(:fromDate, 'YYYY-MM-DD'), TO_DATE(:toDate, 'YYYY-MM-DD')); END;")
					.setParameter("circd", circleCode).setParameter("fromDate", formattedFromDate)
					.setParameter("toDate", formattedToDate).executeUpdate();

			session.createNativeQuery("BEGIN CAT_SEC_ABST_SINGLE_ALL; END;").executeUpdate();

			session.flush();
			session.getTransaction().commit();
			
			String hql = "SELECT c.*,d.NAME as DIVISION_NAME FROM TMP_CT_SEC c,SECTION s,DIVISION d WHERE c.SECCODE = s.ID AND d.ID =s.DIVISION_ID ";

			List<Object[]> results = session.createNativeQuery(hql).getResultList();
			List<TmpCtSecBean> circleSection = new ArrayList<>();
			
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
			cameFromInsideReport= true;
			
			FacesContext.getCurrentInstance().getExternalContext().redirect("categoryWiseSectionReportDateRange.xhtml");
		
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in database operation");
		}
	}
	
	@Transactional
	public void fetchReportOtherThanRegionAndCircle() {
		
		
		Calendar fromCal = Calendar.getInstance();
		fromCal.set(2023,0, 1);
		if((dmFilter.getFromDate()==null) && (dmFilter.getToDate()==null)) {
			
			dmFilter.setFromDate(fromCal.getTime());
			dmFilter.setToDate(new Date());
		}
		if((dmFilter.getFromDate()!=null) && (dmFilter.getToDate()==null)) {
			dmFilter.setToDate(new Date());
		}
		if((dmFilter.getFromDate()==null) && (dmFilter.getToDate()!=null)) {
			dmFilter.setFromDate(fromCal.getTime());
		}
		if (dmFilter.getFromDate().after(dmFilter.getToDate())) {
		    	
		    	FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
		                "ERROR", "From Date Cannot Be After To Date");
		            FacesContext.getCurrentInstance().addMessage(null, message);
		            return;
		            
		    }
		
		if(adminUserValueBean!=null) {
			String circleCode = adminUserValueBean.getCircleId().toString();
			String circleName = adminUserValueBean.getCircleName();
		
			
			System.out.println("THE CIRCLE CODE ------"+circleCode);
			System.out.println("THE CIRCLE NAME ------"+circleName);

		Session session = null;
		try {
			session = sessionFactory.openSession();
			session.beginTransaction();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String formattedFromDate = sdf.format(dmFilter.getFromDate());
			String formattedToDate = sdf.format(dmFilter.getToDate());

			session.createNativeQuery(
					"BEGIN CAT_SEC_ABST_ALL_DT_CIR(:circd, TO_DATE(:fromDate, 'YYYY-MM-DD'), TO_DATE(:toDate, 'YYYY-MM-DD')); END;")
					.setParameter("circd", circleCode).setParameter("fromDate", formattedFromDate)
					.setParameter("toDate", formattedToDate).executeUpdate();

			session.createNativeQuery("BEGIN CAT_SEC_ABST_SINGLE_ALL; END;").executeUpdate();

			session.flush();
			session.getTransaction().commit();
			
			String hql = "SELECT c.*,d.NAME as DIVISION_NAME FROM TMP_CT_SEC c,SECTION s,DIVISION d WHERE c.SECCODE = s.ID AND d.ID =s.DIVISION_ID ";

			List<Object[]> results = session.createNativeQuery(hql).getResultList();
			List<TmpCtSecBean> circleSection = new ArrayList<>();
			
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
			
			if(adminUserValueBean.getRoleId()==1) {
				sectionList = sectionList.stream().filter(circ ->circ.getSecCode().equals(adminUserValueBean.getSectionId().toString())).collect(Collectors.toList());
			}
			
			selectedCircleName = circleName;
			cameFromInsideReport= true;
		

			FacesContext.getCurrentInstance().getExternalContext().redirect("categoryWiseSectionReportDateRange.xhtml");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in database operation");
		}
		}else {
			
		}
		
	}
	
	// TOTAL VALUE FOR CIRLCE REPORT
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

	    grandTotalRevd = totalBlTot.add(totalMeTot).add(totalPfTot).add(totalVfTot)
	                        .add(totalFiTot).add(totalThTot).add(totalTeTot)
	                        .add(totalCsTot).add(totalOtTot);

	    grandTotalComp = totalBlCom.add(totalMeCom).add(totalPfCom).add(totalVfCom)
	                        .add(totalFiCom).add(totalThCom).add(totalTeCom)
	                        .add(totalCsCom).add(totalOtCom);

	    grandTotalPend = totalBlPen.add(totalMePen).add(totalPfPen).add(totalVfPen)
	                        .add(totalFiPen).add(totalThPen).add(totalTePen)
	                        .add(totalCsPen).add(totalOtPen);
	}


	//CIRCLE REPORT TO EXCEL DOWNLOAD
	public void exportToExcel(List<TmpCtCirBean> reports) throws IOException {
		HSSFWorkbook workbook = new HSSFWorkbook();
	    Sheet sheet = workbook.createSheet("CategoryWise_Complaints_Abstract_Report_For_A_DateRange_And_Source");

        sheet.setColumnWidth(0, 2000); //S.NO
        sheet.setColumnWidth(1, 5000); // CIRCLE

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
	    
	    Row headingRow = sheet.createRow(0);
	    Cell headingCell = headingRow.createCell(0);
	    headingCell.setCellValue("CATEGORY WISE COMPLAINTS ABSTRACT REPORT FOR A DATE RANGE AND SOURCE - CIRCLE WISE");
	    headingCell.setCellStyle(headingStyle);
	    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 31)); 
	    
	    Row dateRow = sheet.createRow(1);
	    Cell dateCell = dateRow.createCell(0);
	    
	    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	    String fromDateStr = dmFilter.getFromDate() != null ? 
	                         dateFormat.format(dmFilter.getFromDate()) : "N/A";
	    String toDateStr = dmFilter.getToDate() != null ? 
	                       dateFormat.format(dmFilter.getToDate()) : "N/A";
	    
	    dateCell.setCellValue("From Date: " + fromDateStr + "  To Date: " + toDateStr);
	    dateCell.setCellStyle(dateStyle);
	    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 31));

	    Row headerRow1 = sheet.createRow(2);
	    Row headerRow2 = sheet.createRow(3);

	    String[] mainHeaders = {"S.NO", "CIRCLE", "Billing Related", "Meter Related", "Power Failure", "Voltage Fluctuation", 
	                            "Fire", "Dangerous Pole", "Theft", "Conductor Snapping", "Others", "TOTAL"};

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

	    int rowNum = 4;
	    BigDecimal[] totalSums = new BigDecimal[30];  
	    Arrays.fill(totalSums, BigDecimal.ZERO);
	    int serialNumber = 1;

	    for (TmpCtCirBean report : reports) {
	        Row row = sheet.createRow(rowNum++);
	        row.createCell(0).setCellValue(serialNumber++);
	        row.createCell(1).setCellValue(report.getCirname());

	        BigDecimal rowTotSum = BigDecimal.ZERO;
	        BigDecimal rowCompSum = BigDecimal.ZERO;
	        BigDecimal rowPendSum = BigDecimal.ZERO;

	        int dataIndex = 2; 

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
	            setCellValue(row, dataIndex++, values[i]);
	            setCellValue(row, dataIndex++, values[i + 1]);
	            setCellValue(row, dataIndex++, values[i + 2]);

	            rowTotSum = rowTotSum.add(values[i] != null ? values[i] : BigDecimal.ZERO);
	            rowCompSum = rowCompSum.add(values[i + 1] != null ? values[i + 1] : BigDecimal.ZERO);
	            rowPendSum = rowPendSum.add(values[i + 2] != null ? values[i + 2] : BigDecimal.ZERO);
	        }

	        setCellValue(row, dataIndex++, rowTotSum);
	        setCellValue(row, dataIndex++, rowCompSum);
	        setCellValue(row, dataIndex++, rowPendSum);

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
	    totalRow.createCell(1).setCellValue("TOTAL");
	    totalRow.getCell(0).setCellStyle(totalRowStyle);
	    totalRow.getCell(1).setCellStyle(totalRowStyle);


	    int totalDataIndex = 2;
	    for (int i = 0; i < totalSums.length; i++) {
	    	setCellValue(totalRow, totalDataIndex, totalSums[i]);
	    	totalRow.getCell(totalDataIndex++).setCellStyle(totalRowStyle);

	    }

	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    workbook.write(outputStream);
	    workbook.close();

	    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
	    try {
	        excelFile = DefaultStreamedContent.builder()
	                .name("CategoryWise_Complaints_Abstract_Report_For_A_DateRange_And_Source_CircleWise.xls")
	                .contentType("application/vnd.ms-excel")
	                .stream(() -> inputStream)
	                .build();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	//SECTION REPORT TO EXCEL DOWNLOAD
	 public void exportSectionsToExcel(List<TmpCtSecBean> reports) throws IOException {
		    HSSFWorkbook workbook = new HSSFWorkbook();
		    Sheet sheet = workbook.createSheet("CategoryWise_Complaints_Abstract_Report_For_A_DateRange_And_Source_SectionWise");

	        sheet.setColumnWidth(0, 3000); // S.NO
	        sheet.setColumnWidth(1, 5000); // SECTION
	        sheet.setColumnWidth(2, 5000); // DIVISION

		    for (int i = 3; i <= 31; i++) {  
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
		    headingCell.setCellValue("CATEGORY WISE COMPLAINTS ABSTRACT REPORT FOR A DATE RANGE AND SOURCE - SECTION WISE");
		    headingCell.setCellStyle(headingStyle);
		    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 32)); 
		    
		    Row dateRow = sheet.createRow(1);
		    Cell dateCell = dateRow.createCell(0);
		    
		    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		    String fromDateStr = dmFilter.getFromDate() != null ? 
		                         dateFormat.format(dmFilter.getFromDate()) : "N/A";
		    String toDateStr = dmFilter.getToDate() != null ? 
		                       dateFormat.format(dmFilter.getToDate()) : "N/A";
		    
		    dateCell.setCellValue("From Date: " + fromDateStr + "  To Date: " + toDateStr);
		    dateCell.setCellStyle(dateStyle);
		    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 32));

		    Row headerRow1 = sheet.createRow(2);
		    Row headerRow2 = sheet.createRow(3);

		    String[] mainHeaders = {"S.NO","SECTION", "DIVISION", "Billing Related", "Meter Related", "Power Failure", "Voltage Fluctuation", 
		                            "Fire", "Dangerous Pole", "Theft", "Conductor Snapping", "Others", "TOTAL"};

		    String[] subHeaders = {"Revd.", "Comp.", "Pend."};
		    
		    int colIndex = 0;
		    for (String mainHeader : mainHeaders) {
		        Cell cell = headerRow1.createCell(colIndex);
		        cell.setCellValue(mainHeader);
		        cell.setCellStyle(headerStyle);
		        
		        if (!mainHeader.equals("S.NO") && !mainHeader.equals("SECTION") && !mainHeader.equals("DIVISION")) {
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

		    int serialNumber =1;
		    for (TmpCtSecBean report : reports) {
		        Row row = sheet.createRow(rowNum++);
		        
		        row.createCell(0).setCellValue(String.valueOf(serialNumber++));
		        row.createCell(1).setCellValue(report.getSecName());
		        row.createCell(2).setCellValue(report.getDivisionName());

		        BigDecimal rowTotSum = BigDecimal.ZERO;
		        BigDecimal rowCompSum = BigDecimal.ZERO;
		        BigDecimal rowPendSum = BigDecimal.ZERO;

		        int dataIndex = 3; 

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
		            setCellValue(row, dataIndex++, values[i]);
		            setCellValue(row, dataIndex++, values[i + 1]);
		            setCellValue(row, dataIndex++, values[i + 2]);

		            rowTotSum = rowTotSum.add(values[i] != null ? values[i] : BigDecimal.ZERO);
		            rowCompSum = rowCompSum.add(values[i + 1] != null ? values[i + 1] : BigDecimal.ZERO);
		            rowPendSum = rowPendSum.add(values[i + 2] != null ? values[i + 2] : BigDecimal.ZERO);
		        }

		        setCellValue(row, dataIndex++, rowTotSum);
		        setCellValue(row, dataIndex++, rowCompSum);
		        setCellValue(row, dataIndex++, rowPendSum);

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
		    totalRow.getCell(0).setCellStyle(totalRowStyle);
		    totalRow.createCell(1).setCellValue("");
		    totalRow.getCell(1).setCellStyle(totalRowStyle);
		    totalRow.createCell(2).setCellValue("");
		    totalRow.getCell(2).setCellStyle(totalRowStyle);

		    int totalDataIndex = 3;
		    for (int i = 0; i < totalSums.length; i++) {
		    	setCellValue(totalRow, totalDataIndex, totalSums[i]);
		    	totalRow.getCell(totalDataIndex++).setCellStyle(totalRowStyle);

		    }

		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    workbook.write(outputStream);
		    workbook.close();

		    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		    try {
		        excelFile = DefaultStreamedContent.builder()
		                .name("CategoryWise_Complaints_Abstract_Report_For_A_DateRange_And_Source_SectionWise.xls")
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
	 public void exportToPdf(List<TmpCtCirBean> circleList) throws IOException {
			    final int CATEGORY = 9;
			    final int SUB_COLUMNS = 3;
			    final int COLUMN_COUNT = 1 + 1 + (CATEGORY * SUB_COLUMNS) + 3; 
			    
			    final float SNO_COLUMN_WIDTH = 4f;
			    final float FIRST_COLUMN_WIDTH = 15f;
			    final float OTHER_COLUMN_WIDTH = 4f;
			    final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
			    final Font TOTAL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
			    final Font DATA_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
			    final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
			    

			    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			    String fromDateStr = dmFilter.getFromDate() != null ? 
			                         dateFormat.format(dmFilter.getFromDate()) : "N/A";
			    String toDateStr = dmFilter.getToDate() != null ? 
			                       dateFormat.format(dmFilter.getToDate()) : "N/A";
			    
			    Document document = new Document(PageSize.A2.rotate());
			    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			    
			    try {
			        PdfWriter.getInstance(document, outputStream);
			        document.open();
			        
			        PdfPTable table = new PdfPTable(COLUMN_COUNT);
			        table.setWidthPercentage(100);
			        table.setSpacingBefore(10f);
			        
			        Paragraph title = new Paragraph("CATEGORY WISE CIRCLE ABSTRACT REPORT AS ON DATE", TITLE_FONT);
			        title.setAlignment(Element.ALIGN_CENTER);
			        document.add(title);
			        
			        Paragraph subTitle = new Paragraph("FROM : " + fromDateStr + " TO " +toDateStr);
			        subTitle.setAlignment(Element.ALIGN_CENTER);
			        subTitle.setSpacingAfter(10);
			        document.add(subTitle);
			        
			        float[] columnWidths = new float[COLUMN_COUNT];
			        
			        columnWidths[0] = SNO_COLUMN_WIDTH;
			        columnWidths[1] = FIRST_COLUMN_WIDTH;
			        
			        for (int i = 2; i < COLUMN_COUNT ; i++) {
			            columnWidths[i] = OTHER_COLUMN_WIDTH;
			        }
			        table.setWidths(columnWidths);
			        
			        addMergedHeaderCell(table, "S.NO", HEADER_FONT, 1, 2);
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
			                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
			                table.addCell(cell);
			            }
			        }
			        BigDecimal grandTot = BigDecimal.ZERO;
			        BigDecimal grandTotCpl =BigDecimal.ZERO;
			        BigDecimal grandTotPend = BigDecimal.ZERO;
			        
			        int serialNumber = 1;
			        
			        for (TmpCtCirBean report : circleList) {
			        	table.addCell(createDataCell(String.valueOf(serialNumber++), DATA_FONT, Element.ALIGN_CENTER));
			            
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


			            
			            addCategoryData(table,total, totalCpl,pend,TOTAL_FONT);
			        }
			        PdfPCell footerLabel1 = new PdfPCell(new Phrase("", TOTAL_FONT));
			        footerLabel1.setBackgroundColor(BaseColor.LIGHT_GRAY);
			        footerLabel1.setHorizontalAlignment(Element.ALIGN_CENTER);
			        table.addCell(footerLabel1);
			        
			        PdfPCell footerLabel = new PdfPCell(new Phrase("TOTAL", TOTAL_FONT));
			        footerLabel.setBackgroundColor(BaseColor.LIGHT_GRAY);
			        footerLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
			        table.addCell(footerLabel);

			        addCategoryData(table,
			            circleList.stream().map(r->r.getBlTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			            circleList.stream().map(r->r.getBlCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			            circleList.stream().map(r->r.getBlPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			            TOTAL_FONT);
			        
			        addCategoryData(table,
			                circleList.stream().map(r->r.getMeTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                circleList.stream().map(r->r.getMeCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                circleList.stream().map(r->r.getMePen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			        addCategoryData(table,
			                circleList.stream().map(r->r.getPfTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                circleList.stream().map(r->r.getPfCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                circleList.stream().map(r->r.getPfPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			        addCategoryData(table,
			                circleList.stream().map(r->r.getVfTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                circleList.stream().map(r->r.getVfCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                circleList.stream().map(r->r.getVfPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			        addCategoryData(table,
			                circleList.stream().map(r->r.getFiTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                circleList.stream().map(r->r.getFiCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                circleList.stream().map(r->r.getFiPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			        addCategoryData(table,
			                circleList.stream().map(r->r.getThTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                circleList.stream().map(r->r.getThCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                circleList.stream().map(r->r.getThPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			        addCategoryData(table,
			                circleList.stream().map(r->r.getTeTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                circleList.stream().map(r->r.getTeCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                circleList.stream().map(r->r.getTePen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			        addCategoryData(table,
			                circleList.stream().map(r->r.getCsTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                circleList.stream().map(r->r.getCsCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                circleList.stream().map(r->r.getCsPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			        addCategoryData(table,
			                circleList.stream().map(r->r.getOtTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                circleList.stream().map(r->r.getOtCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                circleList.stream().map(r->r.getOtPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			       
			        
			       
			        addCategoryData(table, grandTot, grandTotCpl, grandTotPend, TOTAL_FONT);

			        document.add(table);
			        document.close();
			        
			        pdfFile = DefaultStreamedContent.builder()
				            .contentType("application/pdf")
				            .name("Category_Wise_Circle_Abstract_Report.pdf")
				            .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
				            .build();
			        
			    
			    
			    }catch(Exception e) {
			    	e.printStackTrace();
			    }
		  }
	 
	 
	

		 //SECTION REPORT PDF DOWNLOAD
		 public void exportSectionsToPdf(List<TmpCtSecBean> sectionList) throws IOException {
			    final int CATEGORY = 9;
			    final int SUB_COLUMNS = 3;
			    final int COLUMN_COUNT = 1 + 2 + (CATEGORY * SUB_COLUMNS) + 3; 
			    final float SNO_COLUMN_WIDTH = 4f;
			    final float FIRST_COLUMN_WIDTH = 15f;
			    final float OTHER_COLUMN_WIDTH = 4f;
			    final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
			    final Font TOTAL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
			    final Font DATA_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
			    final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
			    

			    Document document = new Document(PageSize.A2.rotate());
			    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			    
			    try {
			        PdfWriter.getInstance(document, outputStream);
			        document.open();
			        
			        PdfPTable table = new PdfPTable(COLUMN_COUNT);
			        table.setWidthPercentage(100);
			        table.setSpacingBefore(10f);
			        
			        Paragraph title = new Paragraph("CATEGORY WISE COMPLAINTS ABSTRACT REPORT FOR A DATE RANGE AND SOURCE - SECTION WISE", TITLE_FONT);
			        title.setAlignment(Element.ALIGN_CENTER);
			        document.add(title);
			        
			        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
				    String fromDateStr = dmFilter.getFromDate() != null ? 
				                         dateFormat.format(dmFilter.getFromDate()) : "N/A";
				    String toDateStr = dmFilter.getToDate() != null ? 
				                       dateFormat.format(dmFilter.getToDate()) : "N/A";
			        
			        Paragraph subTitle = new Paragraph("CIRCLE :"+selectedCircleName+ " | "+"FROM : " + fromDateStr + " TO " +toDateStr);
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
			        addMergedHeaderCell(table, "S.No", HEADER_FONT, 1, 2);
			        addMergedHeaderCell(table, "SECTION", HEADER_FONT, 1, 2);
			        addMergedHeaderCell(table, "DIVISION", HEADER_FONT, 1, 2);
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
			        
			        int serialNumber = 1;
			        
			        for (TmpCtSecBean report : sectionList) {
			        	table.addCell(createDataCell(String.valueOf(serialNumber++), DATA_FONT, Element.ALIGN_CENTER));
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


			            
			            addCategoryData(table,total, totalCpl,pend,TOTAL_FONT);
			        }
			        PdfPCell footerLabel1 = new PdfPCell(new Phrase("", TOTAL_FONT));
			        footerLabel1.setBackgroundColor(BaseColor.LIGHT_GRAY);
			        footerLabel1.setHorizontalAlignment(Element.ALIGN_CENTER);
			        table.addCell(footerLabel1);
			        
			        PdfPCell footerLabel = new PdfPCell(new Phrase("TOTAL", TOTAL_FONT));
			        footerLabel.setBackgroundColor(BaseColor.LIGHT_GRAY);
			        footerLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
			        footerLabel.setColspan(2);
			        table.addCell(footerLabel);

			        addCategoryData(table,
			            sectionList.stream().map(r->r.getBlTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			            sectionList.stream().map(r->r.getBlCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			            sectionList.stream().map(r->r.getBlPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			            TOTAL_FONT);
			        
			        addCategoryData(table,
			        		sectionList.stream().map(r->r.getMeTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getMeCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getMePen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			        addCategoryData(table,
			        		sectionList.stream().map(r->r.getPfTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getPfCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getPfPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			        addCategoryData(table,
			        		sectionList.stream().map(r->r.getVfTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getVfCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getVfPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			        addCategoryData(table,
			        		sectionList.stream().map(r->r.getFiTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getFiCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getFiPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			        addCategoryData(table,
			        		sectionList.stream().map(r->r.getThTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getThCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getThPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			        addCategoryData(table,
			        		sectionList.stream().map(r->r.getTeTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getTeCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getTePen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			        addCategoryData(table,
			        		sectionList.stream().map(r->r.getCsTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getCsCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getCsPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			        addCategoryData(table,
			        		sectionList.stream().map(r->r.getOtTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getOtCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getOtPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			       
			        
			       
			        addCategoryData(table, grandTot, grandTotCpl, grandTotPend, TOTAL_FONT);

			        document.add(table);
			        document.close();
			        
			        pdfFile = DefaultStreamedContent.builder()
				            .contentType("application/pdf")
				            .name("CategoryWise_Complaints_Abstract_Report_For_A_DateRange_And_Source_SectionWise.pdf")
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
			    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
			    table.addCell(cell);
			}
			


			private PdfPCell createDataCell(String text, Font font, int alignment) {
			    PdfPCell cell = new PdfPCell(new Phrase(text, font));
			    cell.setHorizontalAlignment(alignment);
			    return cell;
			}

			private void addCategoryData(PdfPTable table, BigDecimal revd, BigDecimal cpl, BigDecimal pend, Font font) {
			    table.addCell(createDataCell(revd.toString(), font, Element.ALIGN_LEFT));
			    table.addCell(createDataCell(cpl.toString(), font, Element.ALIGN_LEFT));
			    table.addCell(createDataCell(pend.toString(), font, Element.ALIGN_LEFT));
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
	public Date getCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}

	public ViewComplaintReportValueBean getSelectedComplaintId() {
		return selectedComplaintId;
	}

	public void setSelectedComplaintId(ViewComplaintReportValueBean selectedComplaintId) {
		this.selectedComplaintId = selectedComplaintId;
	}

	public AdminUserValueBean getAdminUserValueBean() {
		return adminUserValueBean;
	}

	public void setAdminUserValueBean(AdminUserValueBean adminUserValueBean) {
		this.adminUserValueBean = adminUserValueBean;
	}

	
}
