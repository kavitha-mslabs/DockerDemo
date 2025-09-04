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
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
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
public class ComplaintTransferredReport implements Serializable {

	private static final long serialVersionUID = 1L;
	List<ComplaintDetailValueBean> complaintList ;
	private boolean initialized = false;
	private boolean cameFromInsideReport= false;
	DataModel dmFilter ;
	private SessionFactory sessionFactory;
	ViewComplaintReportValueBean complaint;
	private Date currentDate = new Date();
	StreamedContent excelFile;
	StreamedContent pdfFile;

	@PostConstruct
	public void init() {
		System.out.println("Initializing COMPLAINT TRANSFERRED REPORT...");
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
	public void getcomplaintTransferredDetail() {
	    try (Session session = sessionFactory.openSession()) {

	        if (dmFilter.getFromDate() == null) {
	            Calendar fromCal = Calendar.getInstance();
	            fromCal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
	            dmFilter.setFromDate(fromCal.getTime());
	        }
	        if (dmFilter.getToDate() == null) {
	            dmFilter.setToDate(new Date());
	        }

	        HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
	                .getExternalContext().getSession(false);

	        AdminUserValueBean adminUserValueBean = (AdminUserValueBean) httpsession.getAttribute("sessionAdminValueBean");
	        CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");

	        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	        String formattedFromDate = sdf.format(dmFilter.getFromDate());
	        String formattedToDate = sdf.format(dmFilter.getToDate());

	        String hql = "SELECT " +
	                " a.id AS complaintId, " +
	                " TO_CHAR(a.CREATED_ON, 'dd-mm-yyyy-hh24:mi') AS complaintDate, " +
	                " CASE a.device " +
	                "     WHEN 'web' THEN 'Web' " +
	                "     WHEN 'FOC' THEN 'FOC' " +
	                "     WHEN 'admin' THEN 'FOC' " +
	                "     WHEN 'SM' THEN 'Social Media' " +
	                "     WHEN 'Android' THEN 'Mobile' " +
	                "     WHEN 'AMOB' THEN 'Mobile' " +
	                "     WHEN 'IMOB' THEN 'Mobile' " +
	                "     WHEN 'iOS' THEN 'Mobile' " +
	                "     WHEN 'mobile' THEN 'Mobile' " +
	                "     WHEN 'MI' THEN 'Minnagam' " +
	                "     ELSE a.device END AS complaintMode, " +
	                " a.SERVICE_NUMBER AS consumerNumber, " +
	                " a.SERVICE_NAME AS consumerName, " +
	                " a.SERVICE_ADDRESS AS consumerDetail, " +
	                " k.name AS complaintType, " +
	                " a.DESCRIPTION AS complaintDescription, " +
	                " TO_CHAR(l.TRF_ON, 'dd-mm-yyyy-hh24:mi') AS transferredOn, " +
	                " l.TRF_USER AS transferredUser, " +
	                " l.REMARKS AS transferredRemarks, " +
	                " f.name AS fromRegion, g.name AS fromCircle, h.name AS fromDivision, " +
	                " i.name AS fromSubDivision, j.name AS fromSection, " +
	                " r.name AS toRegion, c.name AS toCircle, d.name AS toDivision, " +
	                " sd.name AS toSubDivision, sec.name AS toSection, " +
	                "(CASE WHEN  a.device='MI' then ccm.CONTACTNO else b.mobile end) AS Contact_Number "+
	                "FROM COMPLAINT a " +
	                "JOIN CATEGORY k ON a.complaint_type = k.code " +
	                "JOIN COMP_TRANSFER l ON a.id = l.comp_id " +
	                "JOIN ADMIN_USER m ON m.user_name = l.TRF_USER " +
	                "LEFT JOIN REGION f ON m.region_id = f.id " +
	                "LEFT JOIN CIRCLE g ON m.circle_id = g.id " +
	                "LEFT JOIN DIVISION h ON m.division_id = h.id " +
	                "LEFT JOIN SUB_DIVISION i ON m.sub_division_id = i.id " +
	                "LEFT JOIN SECTION j ON m.section_id = j.id " +
	                "LEFT JOIN REGION r ON a.region_id = r.id " +
	                "LEFT JOIN CIRCLE c ON a.circle_id = c.id " +
	                "LEFT JOIN DIVISION d ON a.division_id = d.id " +
	                "LEFT JOIN SUB_DIVISION sd ON a.sub_division_id = sd.id " +
	                "LEFT JOIN SECTION sec ON a.section_id = sec.id " +
	                "LEFT JOIN COMP_CONTACT_MAP ccm on ccm.COMP_ID=a.id "+	
	                "LEFT JOIN PUBLIC_USER b ON a.user_id = b.id " +
	                "WHERE TRUNC(l.TRF_ON) >= TO_DATE(:datefrom, 'DD-MM-YYYY') " +
	                "AND TRUNC(l.TRF_ON) <= TO_DATE(:dateto, 'DD-MM-YYYY') ";

	        Map<String, Object> paramMap = new HashMap<>();
	        paramMap.put("datefrom", formattedFromDate);
	        paramMap.put("dateto", formattedToDate);

	        // Append role-based conditions
	        if (adminUserValueBean != null) {
	            Integer roleId = adminUserValueBean.getRoleId();
	            if (roleId <= 5) {
	                hql += " AND :regionId IN (a.region_id, m.region_id) ";
	                paramMap.put("regionId", adminUserValueBean.getRegionId());
	            }
	            if (roleId <= 4) {
	                hql += " AND :circleId IN (a.circle_id, m.circle_id) ";
	                paramMap.put("circleId", adminUserValueBean.getCircleId());
	            }
	            if (roleId <= 3) {
	                hql += " AND :divisionId IN (a.division_id, m.division_id) ";
	                paramMap.put("divisionId", adminUserValueBean.getDivisionId());
	            }
	            if (roleId <= 2) {
	                hql += " AND :subDivisionId IN (a.sub_division_id, m.sub_division_id) ";
	                paramMap.put("subDivisionId", adminUserValueBean.getSubDivisionId());
	            }
	            if (roleId == 1) {
	                hql += " AND :sectionId IN (a.section_id, m.section_id) ";
	                paramMap.put("sectionId", adminUserValueBean.getSectionId());
	            }
	        } else if (callCenterValueBean != null) {
	            int roleId = callCenterValueBean.getRoleId();
	            Integer userId = callCenterValueBean.getId();

	            // Fetch circle IDs for this call center user
	            List<Integer> circleIds;
	            try (Session tempSession = sessionFactory.openSession()) {
	                circleIds = tempSession.createQuery(
	                        "SELECT c.circleBean.id FROM CallCenterMappingBean c WHERE c.callCenterUserBean.id = :userId", Integer.class)
	                        .setParameter("userId", userId)
	                        .getResultList();
	            }

	            if (roleId == 1 ) {
	                hql += " AND a.circle_id  IN (:circleId) OR m.circle_id IN (:circleId)";
	                paramMap.put("circleId", circleIds);
	            }

	            if (roleId == 5 ) {
	                hql += " AND a.device = :device ";
	                paramMap.put("device", "MI");
	            }
	            if (roleId == 7 ) {
	                hql += " AND a.device = :device ";
	                hql += " AND a.circle_id  IN (:circleId) OR m.circle_id IN (:circleId)";
	                paramMap.put("device", "MI");
	                paramMap.put("circleId", circleIds);
	                
	            }

	            if (roleId == 3) {
	                hql += " AND a.device = :device ";
	                paramMap.put("device", "SM");
	            }
	        }

	        hql += " ORDER BY l.TRF_ON";

	        Query query = session.createNativeQuery(hql);
	        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
	            query.setParameter(entry.getKey(), entry.getValue());
	        }

	        List<Object[]> results = query.getResultList();
	        List<ComplaintDetailValueBean> resultList = new ArrayList<>();
	        complaintList = new ArrayList<>();

	        for (Object[] row : results) {
	            ComplaintDetailValueBean dto = new ComplaintDetailValueBean();
	            dto.setComplaintId((BigDecimal) row[0]);
	            dto.setComplaintDate((String) row[1]);
	            dto.setComplaintMode((String) row[2]);
	            dto.setConsumerNumber((String) row[3]);
	            dto.setServiceName((String) row[4]);
	            dto.setConsumerDetail((String) row[5]);
	            dto.setComplaintType((String) row[6]);
	            dto.setComplaintDescription((String) row[7]);
	            dto.setTransferredDate((String) row[8]);
	            dto.setTransferredUser((String) row[9]);
	            dto.setTransferredRemarks((String) row[10]);
	            dto.setFromRegion((String) row[11]);
	            dto.setFromCircle((String) row[12]);
	            dto.setFromDivision((String) row[13]);
	            dto.setFromSubDivision((String) row[14]);
	            dto.setFromSection((String) row[15]);
	            dto.setToRegion((String) row[16]);
	            dto.setToCircle((String) row[17]);
	            dto.setToDivision((String) row[18]);
	            dto.setToSubDivision((String) row[19]);
	            dto.setToSection((String) row[20]);
	            dto.setContactNumber((String)row[21]);
	            resultList.add(dto);
	        }

	        if (adminUserValueBean != null && adminUserValueBean.getRoleId() == 10) {
	            complaintList = resultList.stream()
	                    .filter(r -> "Minnagam".equals(r.getComplaintMode()))
	                    .collect(Collectors.toList());
	        } else {
	            complaintList = resultList;
	        }

	        if (complaintList.isEmpty()) {
	            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "No Complaints Transferred For The Given Date");
	            FacesContext.getCurrentInstance().addMessage(null, message);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}


	
	public void exportComplaintListToPDF(List<ComplaintDetailValueBean> complaintList) {
	    Document document = new Document(PageSize.A2.rotate());
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	    try {
	        PdfWriter.getInstance(document, outputStream);
	        document.open();
	        
	        // Title
	        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD, BaseColor.BLACK);
	        Paragraph title = new Paragraph("COMPLAINT TRANSFERRED REPORT", titleFont);
	        title.setAlignment(Element.ALIGN_CENTER);
	        title.setSpacingAfter(10);
	        document.add(title);
	        
	        // Date formatting
	        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	        String fromDate = dmFilter.getFromDate() != null ? sdf.format(dmFilter.getFromDate()) : "N/A";
	        String toDate = dmFilter.getToDate() != null ? sdf.format(dmFilter.getToDate()) : "N/A";
	        
	        // Subtitle
	        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY);
	        Paragraph subTitle = new Paragraph("FROM : " + fromDate + "  | TO : " + toDate, subtitleFont);
	        subTitle.setAlignment(Element.ALIGN_CENTER);
	        subTitle.setSpacingAfter(10);
	        document.add(subTitle);
	        
	        // Table setup - 20 columns (19 original + S.NO)
	        PdfPTable table = new PdfPTable(20);
	        table.setWidthPercentage(100);
	        table.setSpacingBefore(10);
	        table.setSpacingAfter(10);
	        
	        // Adjusted column widths (added S.NO column)
	        float[] columnWidths = {
	            0.8f,  // S.NO
	            1.2f,  // Complaint ID
	            1.2f,  // Complaint Date
	            1.2f,  // Complaint Mode
	            1.5f,  // Consumer Number
	            2.5f,  // Consumer Details
	            1.5f,  // Complaint Type
	            2.5f,  // Complaint Description
	            1.5f,  // Transferred On
	            2.0f,  // Transferred Remarks
	            1.2f,  // From Region
	            1.2f,  // From Circle
	            1.2f,  // From Division
	            1.5f,  // From Sub Division
	            1.2f,  // From Section
	            1.2f,  // To Region
	            1.2f,  // To Circle
	            1.2f,  // To Division
	            1.5f,  // To Sub Division
	            1.2f   // To Section
	        };
	        table.setWidths(columnWidths);
	        
	        // Headers (added S.NO)
	        String[] headers = {
	            "S.NO", "Complaint ID", "Complaint Date", "Complaint Mode", "Consumer Number", "Consumer Details",
	            "Complaint Type", "Complaint Description", "Transferred On", "Transferred Remarks",
	            "From Region", "From Circle", "From Division", "From Sub Division", "From Section",
	            "To Region", "To Circle", "To Division", "To Sub Division", "To Section"
	        };

	        // Add headers
	        for (String header : headers) {
	            PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Font.BOLD)));
	            cell.setPadding(5);
	            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	            cell.setBackgroundColor(new BaseColor(220, 220, 220));
	            cell.setBorderWidth(0.5f);
	            table.addCell(cell);
	        }

	        // Add data rows
	        if (complaintList != null && !complaintList.isEmpty()) {
	            int serialNumber = 1;
	            for (ComplaintDetailValueBean complaint : complaintList) {
	                // Add S.NO column
	                addCell(table, String.valueOf(serialNumber++), Element.ALIGN_CENTER);
	                
	                // Original columns
	                addCell(table, complaint.getComplaintId() != null ? complaint.getComplaintId().toString() : "", Element.ALIGN_CENTER);
	                addCell(table, complaint.getComplaintDate(), Element.ALIGN_CENTER);
	                addCell(table, complaint.getComplaintMode(), Element.ALIGN_CENTER);
	                addCell(table, complaint.getConsumerNumber(), Element.ALIGN_LEFT);
	                addCell(table, complaint.getConsumerDetail(), Element.ALIGN_LEFT);
	                addCell(table, complaint.getComplaintType(), Element.ALIGN_LEFT);
	                addCell(table, complaint.getComplaintDescription(), Element.ALIGN_LEFT);
	                addCell(table, complaint.getTransferredDate(), Element.ALIGN_CENTER);
	                addCell(table, complaint.getTransferredRemarks(), Element.ALIGN_LEFT);
	                addCell(table, complaint.getFromRegion(), Element.ALIGN_LEFT);
	                addCell(table, complaint.getFromCircle(), Element.ALIGN_LEFT);
	                addCell(table, complaint.getFromDivision(), Element.ALIGN_LEFT);
	                addCell(table, complaint.getFromSubDivision(), Element.ALIGN_LEFT);
	                addCell(table, complaint.getFromSection(), Element.ALIGN_LEFT);
	                addCell(table, complaint.getToRegion(), Element.ALIGN_LEFT);
	                addCell(table, complaint.getToCircle(), Element.ALIGN_LEFT);
	                addCell(table, complaint.getToDivision(), Element.ALIGN_LEFT);
	                addCell(table, complaint.getToSubDivision(), Element.ALIGN_LEFT);
	                addCell(table, complaint.getToSection(), Element.ALIGN_LEFT);
	            }
	        } else {
	            PdfPCell noDataCell = new PdfPCell(new Phrase("No complaints available", 
	                FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC, BaseColor.RED)));
	            noDataCell.setColspan(20);
	            noDataCell.setPadding(10);
	            noDataCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	            table.addCell(noDataCell);
	        }

	        document.add(table);
	        document.close();

	        pdfFile = DefaultStreamedContent.builder()
	            .contentType("application/pdf")
	            .name("Complaint_Transferred_Report.pdf")
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
	
	public void exportComplaintListToExcel(List<ComplaintDetailValueBean> complaintList) throws IOException {
	    HSSFWorkbook workbook = new HSSFWorkbook();
	    Sheet sheet = workbook.createSheet("Complaint_Transferred_Report");

	    // Set column widths (added S.NO column)
	    int[] columnWidths = {2000, 4000, 4000, 4000, 5000, 8000, 5000, 8000, 4000, 6000, 
	                          4000, 4000, 4000, 5000, 4000, 4000, 4000, 4000, 5000, 4000};
	    for (int i = 0; i < columnWidths.length; i++) {
	        sheet.setColumnWidth(i, columnWidths[i]);
	    }

	    // Create styles
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
	    dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

	    // Create title row
	    Row titleRow = sheet.createRow(0);
	    Cell titleCell = titleRow.createCell(0);
	    titleCell.setCellValue("COMPLAINT TRANSFERRED REPORT");
	    CellStyle titleStyle = workbook.createCellStyle();
	    HSSFFont titleFont = workbook.createFont();
	    titleFont.setBold(true);
	    titleFont.setFontHeightInPoints((short) 14);
	    titleStyle.setFont(titleFont);
	    titleStyle.setAlignment(HorizontalAlignment.CENTER);
	    titleCell.setCellStyle(titleStyle);
	    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 19));

	    // Create date row
	    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	    String fromDate = dmFilter.getFromDate() != null ? sdf.format(dmFilter.getFromDate()) : "N/A";
	    String toDate = dmFilter.getToDate() != null ? sdf.format(dmFilter.getToDate()) : "N/A";
	    
	    Row dateRow = sheet.createRow(1);
	    Cell dateCell = dateRow.createCell(0);
	    dateCell.setCellValue("FROM: " + fromDate + " TO: " + toDate);
	    CellStyle dateStyle = workbook.createCellStyle();
	    HSSFFont dateFont = workbook.createFont();
	    dateFont.setBold(true);
	    dateFont.setFontHeightInPoints((short) 12);
	    dateStyle.setFont(dateFont);
	    dateStyle.setAlignment(HorizontalAlignment.CENTER);
	    dateCell.setCellStyle(dateStyle);
	    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 19));

	    // Create header row (added S.NO)
	    Row headerRow = sheet.createRow(2);
	    String[] headers = {
	        "S.NO", "Complaint ID", "Complaint Date", "Complaint Mode", "Consumer Number", "Consumer Details",
	        "Complaint Type", "Complaint Description", "Transferred On", "Transferred Remarks",
	        "From Region", "From Circle", "From Division", "From Sub Division", "From Section",
	        "To Region", "To Circle", "To Division", "To Sub Division", "To Section"
	    };

	    for (int i = 0; i < headers.length; i++) {
	        Cell cell = headerRow.createCell(i);
	        cell.setCellValue(headers[i]);
	        cell.setCellStyle(headerStyle);
	    }

	    // Add data rows
	    int rowNum = 3;
	    if (complaintList != null && !complaintList.isEmpty()) {
	        int serialNumber = 1;
	        for (ComplaintDetailValueBean complaint : complaintList) {
	            Row row = sheet.createRow(rowNum++);
	            
	            // Add S.NO column
	            createCell(row, 0, String.valueOf(serialNumber++), dataCellStyle);
	            
	            // Original columns
	            createCell(row, 1, complaint.getComplaintId() != null ? complaint.getComplaintId().toString() : "", dataCellStyle);
	            createCell(row, 2, complaint.getComplaintDate(), dataCellStyle);
	            createCell(row, 3, complaint.getComplaintMode(), dataCellStyle);
	            createCell(row, 4, complaint.getConsumerNumber(), dataCellStyle);
	            createCell(row, 5, complaint.getConsumerDetail(), dataCellStyle);
	            createCell(row, 6, complaint.getComplaintType(), dataCellStyle);
	            createCell(row, 7, complaint.getComplaintDescription(), dataCellStyle);
	            createCell(row, 8, complaint.getTransferredDate(), dataCellStyle);
	            createCell(row, 9, complaint.getTransferredRemarks(), dataCellStyle);
	            createCell(row, 10, complaint.getFromRegion(), dataCellStyle);
	            createCell(row, 11, complaint.getFromCircle(), dataCellStyle);
	            createCell(row, 12, complaint.getFromDivision(), dataCellStyle);
	            createCell(row, 13, complaint.getFromSubDivision(), dataCellStyle);
	            createCell(row, 14, complaint.getFromSection(), dataCellStyle);
	            createCell(row, 15, complaint.getToRegion(), dataCellStyle);
	            createCell(row, 16, complaint.getToCircle(), dataCellStyle);
	            createCell(row, 17, complaint.getToDivision(), dataCellStyle);
	            createCell(row, 18, complaint.getToSubDivision(), dataCellStyle);
	            createCell(row, 19, complaint.getToSection(), dataCellStyle);
	        }
	    } else {
	        Row noDataRow = sheet.createRow(rowNum);
	        Cell noDataCell = noDataRow.createCell(0);
	        noDataCell.setCellValue("No complaints available");
	        CellStyle noDataStyle = workbook.createCellStyle();
	        HSSFFont noDataFont = workbook.createFont();
	        noDataFont.setItalic(true);
	        noDataFont.setColor(IndexedColors.RED.getIndex());
	        noDataStyle.setFont(noDataFont);
	        noDataStyle.setAlignment(HorizontalAlignment.CENTER);
	        noDataCell.setCellStyle(noDataStyle);
	        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 19));
	    }

	    // Auto-size columns for better fit
	    for (int i = 0; i < headers.length; i++) {
	        sheet.autoSizeColumn(i);
	    }

	    // Write to output stream
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    workbook.write(outputStream);
	    workbook.close();

	    // Create StreamedContent for download
	    excelFile = DefaultStreamedContent.builder()
	            .name("Complaint_Transferred_Report.xls")
	            .contentType("application/vnd.ms-excel")
	            .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
	            .build();
	}

	private void createCell(Row row, int column, String value, CellStyle style) {
	    Cell cell = row.createCell(column);
	    cell.setCellValue(value != null ? value : "");
	    cell.setCellStyle(style);
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
	        
	        
	        Integer compId= Integer.parseInt(complaintIdParam);
	        
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
                "(CASE WHEN  a.device='MI' then ccm.CONTACTNO else b.mobile end) AS Contact_Number, "
                + " k.name AS Complaint_Type, d.name AS subctyp, " +
                "a.description AS Complaint_Description, " +
                "DECODE(a.status_id, 0, 'Pending', 1, 'In Progress', 2, 'Completed') AS Complaint_Status, " +
                "(CASE WHEN a.status_id=2 then TO_CHAR(a.updated_on, 'DD-MM-YYYY-HH24:MI') else '' end) AS Attended_Date, " +
        	    "       (CASE WHEN a.status_id = 2 THEN " +
        	    "           (SELECT ch1.description FROM " +
        	    "               (SELECT description, complaint_id, status_id " +
        	    "                FROM COMPLAINT_HISTORY " +
        	    "                WHERE complaint_id = a.id AND status_id = 2 " +
        	    "                ORDER BY updated_on DESC) ch1 " +
        	    "            WHERE ROWNUM = 1) " +
        	    "        ELSE '' END) AS Attended_Remarks, " +
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
                "LEFT JOIN COMP_CONTACT_MAP ccm on ccm.COMP_ID=a.id "+		
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
		  
		  FacesContext.getCurrentInstance().getExternalContext().redirect("complaintTransferredDetail.xhtml");
		  

		
		}catch(Exception e) {
			e.printStackTrace();
			
		}

		
	}

	public void returnComplaintTransferPage() throws IOException {
		  FacesContext.getCurrentInstance().getExternalContext().redirect("complaintTransferredReport.xhtml");
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


	
	public void refresh() {
        dmFilter.setFromDate(null);
        dmFilter.setToDate(null);
        complaintList = null;
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
