package tneb.ccms.admin.controller.reportController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
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

import tneb.ccms.admin.model.ComplaintBean;
import tneb.ccms.admin.model.CustMasterBean;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.valuebeans.AdminUserValueBean;
import tneb.ccms.admin.valuebeans.CallCenterUserValueBean;
import tneb.ccms.admin.valuebeans.ComplaintDetailValueBean;
import tneb.ccms.admin.valuebeans.ViewComplaintReportValueBean;

@Named
@ViewScoped
public class ComplaintQualityCheckedReport implements Serializable {

	private static final long serialVersionUID = 1L;
	List<ComplaintDetailValueBean> complaintList ;
	private boolean initialized = false;
	private boolean cameFromInsideReport = false;
	DataModel dmFilter ;
	private SessionFactory sessionFactory;
	ViewComplaintReportValueBean complaint;
	private Date currentDate = new Date();
	StreamedContent excelFile;
	StreamedContent pdfFile;

	@PostConstruct
	public void init() {
		System.out.println("Initializing COMPLAINT QUALITY CHECKED REPORT...");
		sessionFactory = HibernateUtil.getSessionFactory();
		dmFilter = new DataModel();
	}
	
	public void resetIfNeeded() {
        if (!FacesContext.getCurrentInstance().isPostback() && !cameFromInsideReport) {
        	complaintList = null;
        	dmFilter.setFromDate(null);
        	dmFilter.setToDate(null);
        }
        cameFromInsideReport = false;
    }

	@Transactional
	public void getcomplaintQualityCheckedDetail() {
	    try (Session session = sessionFactory.openSession()) {
	    	
	    	if (dmFilter.getFromDate()==null) {
	    		Calendar fromCal = Calendar.getInstance();
	    	    fromCal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
	    	    dmFilter.setFromDate(fromCal.getTime());
	        }
	    	if (dmFilter.getToDate()==null) {
	    		 dmFilter.setToDate(new Date());
	        }
	    	if (dmFilter.getToDate()==null && dmFilter.getFromDate()==null) {
	    		Calendar fromCal = Calendar.getInstance();
	    	    fromCal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
	    	    dmFilter.setFromDate(fromCal.getTime());
	    	    dmFilter.setToDate(new Date());
	        }
	        
	        
	        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	        String formattedFromDate = sdf.format(dmFilter.getFromDate()); 
	        String formattedToDate = sdf.format(dmFilter.getToDate());
	        
	        String sql = "SELECT a.id, TO_CHAR(a.created_on, 'dd-mm-yyyy-hh24:mi') AS createdOn, " +
	                "       CASE a.device WHEN 'web' THEN 'Web' " +
	                "                     WHEN 'FOC' THEN 'FOC' " +
	                "                     WHEN 'admin' THEN 'FOC' " +
	                "                     WHEN 'SM' THEN 'Social Media' " +
	                "                     WHEN 'Android' THEN 'Mobile' " +
	                "                     WHEN 'AMOB' THEN 'Mobile' " +
	                "                     WHEN 'IMOB' THEN 'Mobile' " +
	                "                     WHEN 'iOS' THEN 'Mobile' " +
	                "                     WHEN 'mobile' THEN 'Mobile' " +
	                "                     WHEN 'MI' THEN 'Minnagam' " +
	                "                     ELSE a.device END AS cmod, " +
	                "       a.service_number, a.service_name, a.service_address, " +
	                "       b.mobile AS contnumber, k.name AS complaintType, " +
	                "       a.description, " +
	                "       CASE a.status_id WHEN 0 THEN 'Pending' " +
	                "                       WHEN 1 THEN 'In Progress' " +
	                "                       WHEN 2 THEN 'Completed' END AS cstat, " +
	                "       TO_CHAR(a.updated_on, 'DD-MM-YYYY-HH24:MI') AS updatedOn, " +
	                "       c.description AS attendedRemarks, " +
	                "       f.name AS regname, g.name AS cirname, " +
	                "       h.name AS divname, i.name AS sdivname, " +
	                "       j.name AS secname, l.remarks AS qcRemarks, " +
	                "       f.id AS regID, g.id AS cirID, " +
	                "       h.id AS divID, i.id AS sdivID, " +
	                "       j.id AS secID,  " +
	                "       TO_CHAR(l.qc_on, 'dd-mm-yyyy-hh24:mi') AS qcDate, l.ENTRYUSER AS qcUser, " +
	                "       DECODE(l.qc_status,'R','Resent','C','Completed',l.qc_status) AS qcStatus " + 
	                "FROM COMPLAINT a " +
	                "JOIN PUBLIC_USER b ON a.user_id = b.id " +
	                "JOIN COMPLAINT_HISTORY c ON a.id = c.complaint_id AND a.status_id = c.status_id " +
	                "JOIN REGION f ON a.region_id = f.id " +
	                "JOIN CIRCLE g ON a.circle_id = g.id " +
	                "JOIN DIVISION h ON a.division_id = h.id " +
	                "JOIN SUB_DIVISION i ON a.sub_division_id = i.id " +
	                "JOIN SECTION j ON a.section_id = j.id " +
	                "JOIN CATEGORY k ON a.complaint_type = k.code " +
	                "JOIN COMP_QC_DETAILS l ON l.comp_id = a.id " +
	                "WHERE a.region_id IS NOT NULL " +
	                "  AND a.circle_id <> 0 " +
	                "  AND a.division_id <> 0 " +
	                "  AND a.sub_division_id <> 0 " +
	                "  AND a.section_id <> 0 " +
	                "  AND TRUNC(l.qc_on) >= TO_DATE(:datefrom, 'DD-MM-YYYY') " +
	                "  AND TRUNC(l.qc_on) <= TO_DATE(:dateto, 'DD-MM-YYYY') " +
	                "UNION " +
	                "SELECT a.id, TO_CHAR(a.created_on, 'dd-mm-yyyy-hh24:mi') AS createdOn, " +
	                "       CASE a.device WHEN 'web' THEN 'Web' " +
	                "                     WHEN 'FOC' THEN 'FOC' " +
	                "                     WHEN 'admin' THEN 'FOC' " +
	                "                     WHEN 'SM' THEN 'Social Media' " +
	                "                     WHEN 'Android' THEN 'Mobile' " +
	                "                     WHEN 'AMOB' THEN 'Mobile' " +
	                "                     WHEN 'IMOB' THEN 'Mobile' " +
	                "                     WHEN 'iOS' THEN 'Mobile' " +
	                "                     WHEN 'mobile' THEN 'Mobile' " +
	                "                     WHEN 'MI' THEN 'Minnagam' " +
	                "                     ELSE a.device END AS cmod, " +
	                "       a.service_number, a.service_name, a.service_address, " +
	                "       b.CONTACTNO AS contnumber, k.name AS complaintType, " +
	                "       a.description, " +
	                "       CASE a.status_id WHEN 0 THEN 'Pending' " +
	                "                      WHEN 1 THEN 'In Progress' " +
	                "                      WHEN 2 THEN 'Completed' END AS cstat, " +
	                "       TO_CHAR(a.updated_on, 'DD-MM-YYYY-HH24:MI') AS updatedOn, " +
	                "       c.description AS attendedRemarks, " +
	                "       f.name AS regname, g.name AS cirname, " +
	                "       h.name AS divname, i.name AS sdivname, " +
	                "       j.name AS secname, l.remarks AS qcRemarks, " +
	                "       f.id AS regID, g.id AS cirID, " +
	                "       h.id AS divID, i.id AS sdivID, " +
	                "       j.id AS secID,  " +
	                "       TO_CHAR(l.qc_on, 'dd-mm-yyyy-hh24:mi') AS qcDate, l.ENTRYUSER AS qcUser, " +
	                "       DECODE(l.qc_status,'R','Resent','C','Completed',l.qc_status) as qcStatus " +
	                "FROM COMPLAINT a " +
	                "JOIN COMP_CONTACT_MAP b ON a.id = b.comp_id " +
	                "JOIN COMPLAINT_HISTORY c ON a.id = c.complaint_id AND a.status_id = c.status_id " +
	                "JOIN REGION f ON a.region_id = f.id " +
	                "JOIN CIRCLE g ON a.circle_id = g.id " +
	                "JOIN DIVISION h ON a.division_id = h.id " +
	                "JOIN SUB_DIVISION i ON a.sub_division_id = i.id " +
	                "JOIN SECTION j ON a.section_id = j.id " +
	                "JOIN CATEGORY k ON a.complaint_type = k.code " +
	                "JOIN COMP_QC_DETAILS l ON l.comp_id = a.id " +
	                "WHERE a.region_id IS NOT NULL " +
	                "  AND a.circle_id <> 0 " +
	                "  AND a.division_id <> 0 " +
	                "  AND a.sub_division_id <> 0 " +
	                "  AND a.section_id <> 0 " +
	                "  AND TRUNC(l.qc_on) >= TO_DATE(:datefrom, 'DD-MM-YYYY') " +
	                "  AND TRUNC(l.qc_on) <= TO_DATE(:dateto, 'DD-MM-YYYY') " +
	                "ORDER BY qcDate";
	        
	        Query query = session.createNativeQuery(sql);
	        query.setParameter("datefrom", formattedFromDate);
	        query.setParameter("dateto", formattedToDate);

	        List<Object[]> results = query.getResultList();
           List<ComplaintDetailValueBean> resultList = new ArrayList<ComplaintDetailValueBean>();
	        complaintList = new ArrayList<ComplaintDetailValueBean>();
	        for (Object[] row : results) {
	        	ComplaintDetailValueBean dto = new ComplaintDetailValueBean();
	        	dto.setComplaintId((BigDecimal)row[0]);
	        	dto.setComplaintDate((String) row[1]);
	        	dto.setComplaintMode((String) row[2]);
	        	dto.setConsumerNumber((String) row[3]);
	        	dto.setServiceName((String) row[4]);
	        	dto.setConsumerDetail((String) row[5]);
	        	dto.setContactNumber((String) row[6]);
	        	dto.setComplaintType((String) row[7]);
	        	dto.setComplaintDescription((String) row[8]);
	        	dto.setComplaintStatus((String) row[9]);
	        	dto.setComplaintUpdatedOn((String) row[10]);
	        	dto.setAttendedRemarks((String) row[11]);
	        	dto.setRegion((String) row[12]);
	        	dto.setCircle((String) row[13]);
	        	dto.setDivision((String) row[14]);
	        	dto.setSubDivision((String) row[15]);
	        	dto.setSection((String) row[16]);
	        	dto.setQcDoneRemarks((String) row[17]);
	        	dto.setRegionId(row[18] != null ? ((BigDecimal) row[18]).intValue() : null);
	        	dto.setCircleId(row[19] != null ? ((BigDecimal) row[19]).intValue() : null);
	        	dto.setDivisionId(row[20] != null ? ((BigDecimal) row[20]).intValue() : null);
	        	dto.setSubDivisionId(row[21] != null ? ((BigDecimal) row[21]).intValue() : null);
	        	dto.setSectionId(row[22] != null ? ((BigDecimal) row[22]).intValue() : null);
	        	dto.setQcDoneDate((String) row[23]);
	        	dto.setQcDoneBy((String) row[24]);
	        	dto.setQcDoneStatus((String) row[25]);

	        	
	        	
	        	resultList.add(dto);
	        }
	        
	        HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
	                .getExternalContext().getSession(false);
			
			AdminUserValueBean adminUserValueBean = (AdminUserValueBean) httpsession.getAttribute("sessionAdminValueBean");
			CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");
			        
	        if(adminUserValueBean!=null) {
	        Integer roleId = adminUserValueBean.getRoleId();
	        Integer regionID = adminUserValueBean.getRegionId();
	        Integer circleID = adminUserValueBean.getCircleId();
	        Integer divisionID = adminUserValueBean.getDivisionId();
	        Integer subDivisionID = adminUserValueBean.getSubDivisionId();
	        Integer sectionID = adminUserValueBean.getSectionId();
	        
	        // HEAD QUATERS
	        if(roleId>=6 && roleId<=9) {
	        	complaintList = resultList;
	        }
	        //REGION
	        else if(roleId==5) {      
	        	complaintList = resultList.stream().filter(r ->r.getRegionId().equals(regionID)).collect(Collectors.toList());
	        }
	        //CIRCLE
	        else if(roleId==4) {
	        	complaintList = resultList.stream().filter(r ->r.getRegionId().equals(regionID)).filter(c ->c.getCircleId().equals(circleID)).collect(Collectors.toList());
	        }
	        //DIVISION
	        else if(roleId==3) {
	        	complaintList = resultList.stream().filter(r ->r.getRegionId().equals(regionID)).filter(c ->c.getCircleId().equals(circleID))
	        			.filter(d ->d.getDivisionId().equals(divisionID)).collect(Collectors.toList());
	        }
	      //SUB DIVISION
	        else if(roleId==2) {
	        	complaintList = resultList.stream().filter(r ->r.getRegionId().equals(regionID)).filter(c ->c.getCircleId().equals(circleID))
	        			.filter(d ->d.getDivisionId().equals(divisionID)).filter(sd ->sd.getSubDivisionId().equals(subDivisionID)).collect(Collectors.toList());
	        }
	      //SECTION
	        else if(roleId==1) {
	        	complaintList = resultList.stream().filter(r ->r.getRegionId().equals(regionID)).filter(c ->c.getCircleId().equals(circleID))
	        			.filter(d ->d.getDivisionId().equals(divisionID)).filter(sd ->sd.getSubDivisionId().equals(subDivisionID))
	        			.filter(sec->sec.getSectionId().equals(sectionID)).collect(Collectors.toList());
	        }
	        else if (roleId==10) {
	        	complaintList = resultList.stream().filter(r ->r.getComplaintMode().equals("Minnagam")).collect(Collectors.toList());
	        }
	        else {
	        	complaintList = resultList;
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
	            	complaintList = resultList.stream().filter(c ->circleIdList.contains(c.getCircleId())).collect(Collectors.toList());
	            }
	            if(roleId==5) {
	            	complaintList = resultList.stream().filter(c ->c.getComplaintMode().equals("Minnagam")).collect(Collectors.toList());
	            }
	            if(roleId==7) {
	            	complaintList = resultList.stream().filter(c ->circleIdList.contains(c.getCircleId())).filter(c ->c.getComplaintMode().equals("Minnagam")).collect(Collectors.toList());
	            }
	            if(roleId==3) {
	            	complaintList = resultList.stream().filter(c ->c.getComplaintMode().equals("Social Media")).collect(Collectors.toList());
	            }

	        }else {
	        	complaintList= resultList;
	        }
				    
	        
	        System.out.println("THE COMPLAINT LIST SIZE ----------------"+complaintList.size());
	        
	        
	        if(complaintList.size()==0) {
	        	FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,"","No Complaints Quality Checked For The Given Date");
	        	FacesContext.getCurrentInstance().addMessage(null, message);
	        	return;
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void exportComplaintListToExcel(List<ComplaintDetailValueBean> complaintList) throws IOException {
	    HSSFWorkbook workbook = new HSSFWorkbook();
	    Sheet sheet = workbook.createSheet("Complaint_Quality_Checked_Report");
	    
	    CellStyle mainHeadingStyle = createMainHeadingStyle(workbook);
	    CellStyle subHeadingStyle = createSubHeadingStyle(workbook);
	    CellStyle headerStyle = createHeaderStyle(workbook);
	    CellStyle dataStyle = createDataStyle(workbook);
	    
	    //HEADING
	    Row mainHeadingRow = sheet.createRow(0);
	    Cell mainHeadingCell = mainHeadingRow.createCell(0);
	    mainHeadingCell.setCellValue("COMPLAINT QUALITY CHECKED REPORT");
	    mainHeadingCell.setCellStyle(mainHeadingStyle);
	    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 16)); 
	    
	    // SUBHEADING
	    Row subHeadingRow = sheet.createRow(1);
	    Cell subHeadingCell = subHeadingRow.createCell(0);
	    
	    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	    String fromDateStr = dmFilter.getFromDate() != null ? 
	                         dateFormat.format(dmFilter.getFromDate()) : "N/A";
	    String toDateStr = dmFilter.getToDate() != null ? 
	                       dateFormat.format(dmFilter.getToDate()) : "N/A";
	    
	    subHeadingCell.setCellValue("From Date: " + fromDateStr + "  To Date: " + toDateStr);
	    subHeadingCell.setCellStyle(subHeadingStyle);
	    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 16)); 
	    
	    Row headerRow = sheet.createRow(2);
	    
	    String[] headers = new String[] {
	        "S.No", "Complaint ID", "Complaint Date", "Complaint Mode", "Consumer Number", 
	        "Consumer Details", "Contact Number", "Complaint Type", "Complaint Description", 
	        "QC Done By", "QC Done Date", "QC Done Remarks", "QC Done Status", 
	        "Region", "Circle", "Division", "Sub Division", "Section"
	    };
	    
	    for (int i = 0; i < headers.length; i++) {
	        Cell cell = headerRow.createCell(i);
	        cell.setCellValue(headers[i]);
	        cell.setCellStyle(headerStyle);
	    }
	    
	    // Data Rows
	    int rowNum = 3; 
	    int serialNumber = 1;
	    
	    for (ComplaintDetailValueBean complaint : complaintList) {
	        Row row = sheet.createRow(rowNum++);
	        
	        // S.No
	        Cell serialCell = row.createCell(0);
	        serialCell.setCellValue(serialNumber++);
	        serialCell.setCellStyle(dataStyle);
	        
	        // Complaint Data
	        row.createCell(1).setCellValue(complaint.getComplaintId() != null ? complaint.getComplaintId().doubleValue() : null);
	        row.createCell(2).setCellValue(complaint.getComplaintDate() != null ? complaint.getComplaintDate() : "");
	        row.createCell(3).setCellValue(complaint.getComplaintMode() != null ? complaint.getComplaintMode() : "");
	        row.createCell(4).setCellValue(complaint.getConsumerNumber() != null ? complaint.getConsumerNumber() : "");
	        row.createCell(5).setCellValue(complaint.getConsumerDetail() != null ? complaint.getConsumerDetail() : "");
	        row.createCell(6).setCellValue(complaint.getContactNumber() != null ? complaint.getContactNumber() : "");
	        row.createCell(7).setCellValue(complaint.getComplaintType() != null ? complaint.getComplaintType() : "");
	        row.createCell(8).setCellValue(complaint.getComplaintDescription() != null ? complaint.getComplaintDescription() : "");
	        row.createCell(9).setCellValue(complaint.getQcDoneBy() != null ? complaint.getQcDoneBy() : "");
	        row.createCell(10).setCellValue(complaint.getQcDoneDate() != null ? complaint.getQcDoneDate() : "");
	        row.createCell(11).setCellValue(complaint.getQcDoneRemarks() != null ? complaint.getQcDoneRemarks() : "");
	        row.createCell(12).setCellValue(complaint.getQcDoneStatus() != null ? complaint.getQcDoneStatus() : "");
	        row.createCell(13).setCellValue(complaint.getRegion() != null ? complaint.getRegion() : "");
	        row.createCell(14).setCellValue(complaint.getCircle() != null ? complaint.getCircle() : "");
	        row.createCell(15).setCellValue(complaint.getDivision() != null ? complaint.getDivision() : "");
	        row.createCell(16).setCellValue(complaint.getSubDivision() != null ? complaint.getSubDivision() : "");
	        row.createCell(17).setCellValue(complaint.getSection() != null ? complaint.getSection() : "");
	        
	        for (int i = 0; i < headers.length; i++) {
	            if (row.getCell(i) != null) {
	                row.getCell(i).setCellStyle(dataStyle);
	            }
	        }
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
	                .name("Complaint_Quality_Checked_Report.xls")
	                .contentType("application/vnd.ms-excel")
	                .stream(() -> inputStream)
	                .build();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	// Style creation methods
	private CellStyle createMainHeadingStyle(Workbook workbook) {
	    CellStyle style = workbook.createCellStyle();
	    HSSFFont font = ((HSSFWorkbook) workbook).createFont();
	    font.setBold(true);
	    font.setFontHeightInPoints((short) 14);
	    style.setFont(font);
	    style.setAlignment(HorizontalAlignment.CENTER);
	    style.setVerticalAlignment(VerticalAlignment.CENTER);
	    style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	    style.setBorderBottom(BorderStyle.THIN);
	    style.setBorderTop(BorderStyle.THIN);
	    style.setBorderLeft(BorderStyle.THIN);
	    style.setBorderRight(BorderStyle.THIN);
	    return style;
	}

	private CellStyle createSubHeadingStyle(Workbook workbook) {
	    CellStyle style = workbook.createCellStyle();
	    HSSFFont font = ((HSSFWorkbook) workbook).createFont();
	    font.setBold(true);
	    font.setFontHeightInPoints((short) 10);
	    style.setFont(font);
	    style.setAlignment(HorizontalAlignment.CENTER);
	    style.setVerticalAlignment(VerticalAlignment.CENTER);
	    style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
	    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	    style.setBorderBottom(BorderStyle.THIN);
	    style.setBorderTop(BorderStyle.THIN);
	    style.setBorderLeft(BorderStyle.THIN);
	    style.setBorderRight(BorderStyle.THIN);
	    return style;
	}

	private CellStyle createHeaderStyle(Workbook workbook) {
	    CellStyle style = workbook.createCellStyle();
	    HSSFFont font = ((HSSFWorkbook) workbook).createFont();
	    font.setBold(true);
	    font.setFontHeightInPoints((short) 10);
	    style.setFont(font);
	    style.setAlignment(HorizontalAlignment.CENTER);
	    style.setVerticalAlignment(VerticalAlignment.CENTER);
	    style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	    style.setBorderBottom(BorderStyle.THIN);
	    style.setBorderTop(BorderStyle.THIN);
	    style.setBorderLeft(BorderStyle.THIN);
	    style.setBorderRight(BorderStyle.THIN);
	    return style;
	}

	private CellStyle createDataStyle(Workbook workbook) {
	    CellStyle style = workbook.createCellStyle();
	    style.setAlignment(HorizontalAlignment.LEFT);
	    style.setVerticalAlignment(VerticalAlignment.CENTER);
	    style.setBorderBottom(BorderStyle.THIN);
	    style.setBorderTop(BorderStyle.THIN);
	    style.setBorderLeft(BorderStyle.THIN);
	    style.setBorderRight(BorderStyle.THIN);
	    style.setWrapText(true); // For better text wrapping
	    return style;
	}
	
	public void exportComplaintListToPDF(List<ComplaintDetailValueBean> complaintList) {
	    Document document = new Document(PageSize.A3.rotate());
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	    try {
	        PdfWriter.getInstance(document, outputStream);
	        document.open();

	        // Title
	        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Font.BOLD, BaseColor.BLACK);
	        Paragraph title = new Paragraph("COMPLAINT QUALITY CHECKED REPORT", titleFont);
	        title.setAlignment(Element.ALIGN_CENTER);
	        title.setSpacingAfter(10);
	        document.add(title);

	        // Subheading
	        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	        String formattedFromDate = sdf.format(dmFilter.getFromDate()); 
	        String formattedToDate = sdf.format(dmFilter.getToDate());
	        
	        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY);
	        Paragraph subTitle = new Paragraph("FROM : " + formattedFromDate + "      TO : " + formattedToDate, subtitleFont);
	        subTitle.setAlignment(Element.ALIGN_CENTER);
	        subTitle.setSpacingAfter(10);
	        document.add(subTitle);

	        // Table setup - 18 columns (you listed 18 headers but had 17 in the code)
	        PdfPTable table = new PdfPTable(18); // Changed to 18 to match headers
	        table.setWidthPercentage(100);
	        table.setSpacingBefore(10);
	        table.setSpacingAfter(10);

	        // Adjusted column widths for better fit
	        float[] columnWidths = {
	            0.8f,  // S. NO
	            1.2f,  // Complaint ID
	            1.2f,  // Complaint Date
	            1.2f,  // Complaint Mode
	            1.5f,  // Consumer Number
	            2.5f,  // Consumer Details
	            1.5f,  // Contact Number
	            1.5f,  // Complaint Type
	            2.5f,  // Complaint Description
	            1.5f,  // QC Done By
	            1.5f,  // QC Done Date
	            2.0f,  // QC Done Remarks
	            1.5f,  // QC Done Status
	            1.2f,  // Region
	            1.2f,  // Circle
	            1.5f,  // Division
	            1.5f,  // Sub Division
	            1.5f   // Section
	        };
	        table.setWidths(columnWidths);

	        // Header row
	        String[] headers = {
	            "S. NO", "Complaint ID", "Complaint Date", "Complaint Mode", "Consumer Number",
	            "Consumer Details", "Contact Number", "Complaint Type", "Complaint Description",
	            "QC Done By", "QC Done Date", "QC Done Remarks", "QC Done Status",
	            "Region", "Circle", "Division", "Sub Division", "Section"
	        };

	        for (String header : headers) {
	            PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Font.BOLD)));
	            cell.setPadding(5);
	            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	            cell.setBackgroundColor(new BaseColor(220, 220, 220)); // Light gray
	            cell.setBorderWidth(0.5f);
	            table.addCell(cell);
	        }

	        // Data rows
	        if (complaintList != null && !complaintList.isEmpty()) {
	            int serialNo = 1;
	            for (ComplaintDetailValueBean complaint : complaintList) {
	                // S.NO - Centered
	                addCell(table, String.valueOf(serialNo++), Element.ALIGN_CENTER);
	                
	                // Complaint ID - Centered
	                addCell(table, complaint.getComplaintId() != null ? complaint.getComplaintId().toString() : "", Element.ALIGN_CENTER);
	                
	                // Complaint Date - Centered
	                addCell(table, complaint.getComplaintDate(), Element.ALIGN_CENTER);
	                
	                // Complaint Mode - Centered
	                addCell(table, complaint.getComplaintMode(), Element.ALIGN_CENTER);
	                
	                // Consumer Number - Left aligned
	                addCell(table, complaint.getConsumerNumber(), Element.ALIGN_LEFT);
	                
	                // Consumer Details - Left aligned
	                addCell(table, complaint.getConsumerDetail(), Element.ALIGN_LEFT);
	                
	                // Contact Number - Centered
	                addCell(table, complaint.getContactNumber(), Element.ALIGN_CENTER);
	                
	                // Complaint Type - Left aligned
	                addCell(table, complaint.getComplaintType(), Element.ALIGN_LEFT);
	                
	                // Complaint Description - Left aligned
	                addCell(table, complaint.getComplaintDescription(), Element.ALIGN_LEFT);
	                
	                // QC Done By - Left aligned
	                addCell(table, complaint.getQcDoneBy(), Element.ALIGN_LEFT);
	                
	                // QC Done Date - Centered
	                addCell(table, complaint.getQcDoneDate(), Element.ALIGN_CENTER);
	                
	                // QC Done Remarks - Left aligned
	                addCell(table, complaint.getQcDoneRemarks(), Element.ALIGN_LEFT);
	                
	                // QC Done Status - Centered
	                addCell(table, complaint.getQcDoneStatus(), Element.ALIGN_CENTER);
	                
	                // Region - Left aligned
	                addCell(table, complaint.getRegion(), Element.ALIGN_LEFT);
	                
	                // Circle - Left aligned
	                addCell(table, complaint.getCircle(), Element.ALIGN_LEFT);
	                
	                // Division - Left aligned
	                addCell(table, complaint.getDivision(), Element.ALIGN_LEFT);
	                
	                // Sub Division - Left aligned
	                addCell(table, complaint.getSubDivision(), Element.ALIGN_LEFT);
	                
	                // Section - Left aligned
	                addCell(table, complaint.getSection(), Element.ALIGN_LEFT);
	            }
	        } else {
	            PdfPCell noDataCell = new PdfPCell(new Phrase("No complaints available",
	                FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC, BaseColor.RED)));
	            noDataCell.setColspan(18); // Updated to 18
	            noDataCell.setPadding(10);
	            noDataCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	            table.addCell(noDataCell);
	        }

	        document.add(table);
	        document.close();

	        pdfFile = DefaultStreamedContent.builder()
	            .contentType("application/pdf")
	            .name("Complaint_Quality_Checked_Report.pdf")
	            .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
	            .build();

	    } catch (DocumentException e) {
	        e.printStackTrace();
	    }
	}

	private void addCell(PdfPTable table, String content, int alignment) {
	    PdfPCell cell = new PdfPCell(new Phrase(content != null ? content : "", 
	        FontFactory.getFont(FontFactory.HELVETICA, 8)));
	    cell.setPadding(5);
	    cell.setHorizontalAlignment(alignment);
	    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	    cell.setBorderWidth(0.5f);
	    table.addCell(cell);
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
                "JOIN SUB_CATEGORY d ON a.sub_category_id = d.id " +
                "JOIN CATEGORY k ON a.complaint_type = k.code " +
                "JOIN REGION f ON a.region_id = f.id " +
                "JOIN CIRCLE g ON a.circle_id = g.id " +
                "JOIN DIVISION h ON a.division_id = h.id " +
                "JOIN SUB_DIVISION i ON a.sub_division_id = i.id " +
                "JOIN SECTION j ON a.section_id = j.id " +
               // "LEFT JOIN CUSTMASTER ct ON SUBSTR(ct.REGCUSID, 2) = a.SERVICE_NUMBER " +
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
        
		  FacesContext.getCurrentInstance().getExternalContext().redirect("qualityCheckedComplaintDetail.xhtml");

		
		}catch(Exception e) {
			e.printStackTrace();
			
		}

		
	}
	
	public void returnComplaintQcPage() throws IOException {
		  FacesContext.getCurrentInstance().getExternalContext().redirect("complaintQualityCheckedReport.xhtml");
	}
	
	public void refresh() {
        dmFilter.setFromDate(null);
        dmFilter.setToDate(null);
        complaintList = null;
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


	
	
	public List<ComplaintDetailValueBean> getComplaintList() {
		return complaintList;
	}
	public void setComplaintList(List<ComplaintDetailValueBean> complaintList) {
		this.complaintList = complaintList;
	}
	public DataModel getDmFilter() {
		return dmFilter;
	}
	public void setDmFilter(DataModel dmFilter) {
		this.dmFilter = dmFilter;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}


	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	public ViewComplaintReportValueBean getcomplaint() {
		return complaint;
	}


	public void setcomplaint(ViewComplaintReportValueBean complaint) {
		this.complaint = complaint;
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


	public Date getCurrentDate() {
		return currentDate;
	}


	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}
	

}
