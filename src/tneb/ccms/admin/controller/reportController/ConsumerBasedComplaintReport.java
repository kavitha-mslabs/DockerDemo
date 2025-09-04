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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import tneb.ccms.admin.valuebeans.ViewComplaintReportValueBean;

@Named
@ViewScoped
public class ConsumerBasedComplaintReport implements Serializable {

	private static final long serialVersionUID = 1L;
	List<ViewComplaintReportValueBean> complaintList = new ArrayList<ViewComplaintReportValueBean>();
	DataModel dmFilter = new DataModel();
	private SessionFactory sessionFactory;
	private boolean initialized = false;
	private boolean cameFromInsideReport = false;

	ViewComplaintReportValueBean complaint;
	private Date currentDate = new Date();
	StreamedContent excelFile;
	StreamedContent pdfFile;
	private String currentYear = String.valueOf(Year.now().getValue());


	public ViewComplaintReportValueBean getComplaint() {
		return complaint;
	}


	public void setComplaint(ViewComplaintReportValueBean complaint) {
		this.complaint = complaint;
	}


	public String getCurrentYear() {
		return currentYear;
	}


	public void setCurrentYear(String currentYear) {
		this.currentYear = currentYear;
	}

	@PostConstruct
	public void init() {
		System.out.println("Initializing CONSUMER BASED COMPLAINT VIEW...");
		sessionFactory = HibernateUtil.getSessionFactory();
		dmFilter = new DataModel();
	}
	
	
	private void setDefaultDatesIfNeeded() {

		if (dmFilter == null) {
			dmFilter = new DataModel();
		}

		if (dmFilter.getFromDate() == null && dmFilter.getToDate() == null) {

			Calendar cal = Calendar.getInstance();
			cal.set(2021, Calendar.JANUARY, 1, 0, 0, 0);
			this.dmFilter.setFromDate(cal.getTime());
			this.dmFilter.setToDate(new Date());
		} else if (dmFilter.getToDate() == null) {

			this.dmFilter.setToDate(new Date());
			if (dmFilter.getFromDate() == null) {
				Calendar cal = Calendar.getInstance();
				cal.set(2021, Calendar.JANUARY, 1, 0, 0, 0);
				this.dmFilter.setFromDate(cal.getTime());
			}
		} else if (dmFilter.getFromDate() == null) {
			Calendar cal = Calendar.getInstance();
			cal.set(2021, Calendar.JANUARY, 1, 0, 0, 0);
			this.dmFilter.setFromDate(cal.getTime());
		}

	}
	public void resetIfNeeded() {
        if (!FacesContext.getCurrentInstance().isPostback() && !cameFromInsideReport) {
        	complaintList = null;
        	dmFilter.setFromDate(null);
        	dmFilter.setToDate(null);
        	dmFilter.setConsumerNumber(null);
        }
        cameFromInsideReport = false;
    }

	//ADMIN USER LOGIN WISE FILTER
//			public void updateLoginWiseFilters() {
//				
//				HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
//		                .getExternalContext().getSession(false);
//				
//				AdminUserValueBean adminUserValueBean = (AdminUserValueBean) 
//						session.getAttribute("sessionAdminValueBean");
//				        
//		        
//		        
//		        // HEAD QUATERS
//		        if(roleId>=6 && roleId<=9) {
//		        	dmFilter.setRegionCode("A");
//		        	dmFilter.setCircleCode("A");
//		        }
//		        //REGION
//		        else if(roleId==5) {      
//		        	dmFilter.setRegionCode(adminUserValueBean.getRegionId().toString());
//		        	dmFilter.setCircleCode("A");	
//		        }
//		        //CIRCLE
//		        else if(roleId==4) {
//		        	dmFilter.setRegionCode(adminUserValueBean.getRegionId().toString());
//		        	dmFilter.setCircleCode(adminUserValueBean.getCircleId().toString());
//		        }
//		        else {
//		        	dmFilter.setRegionCode("A");
//		        	dmFilter.setCircleCode("A");
//		        }
//		        
//			}

	@Transactional
	public void getComplaintByConsumerNumber() {
		
		//updateLoginWiseFilters();
		
		setDefaultDatesIfNeeded();
		
	    try (Session session = sessionFactory.openSession()) {    	

	    	if(dmFilter.getConsumerNumber()==null || dmFilter.getConsumerNumber().isEmpty()) {
	    		FacesContext.getCurrentInstance().addMessage(null, 
		                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
		                "Error", "Please Enter Consumer Number"));
		            return;
	    	}

	         Date formattedFromDate = dmFilter.getFromDate();

	         Date formattedToDate = dmFilter.getToDate();
	         
	        String consumerNumber = dmFilter.getConsumerNumber();
	        System.out.println("THE CONSUMER NUMBER PROVIDED---------------"+consumerNumber);
	        
	        HttpSession httpSession = (HttpSession) FacesContext.getCurrentInstance() .getExternalContext().getSession(false);
  	      
	        AdminUserValueBean adminUserValueBean = (AdminUserValueBean) httpSession.getAttribute("sessionAdminValueBean");
	         
	 		CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpSession.getAttribute("sessionCallCenterUserValueBean");
	 		
			List<Integer> callCenterCircleId = new ArrayList<Integer>();

			Integer roleId = null;
			Integer regionID = null;
			Integer circleID = null;
			Integer divisionID = null;
			Integer subDivisionID = null;
			Integer sectionID = null;
	 		
			//IF CALL CENTER USER LOIGIN
	 		if(callCenterValueBean!=null) {
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
	 			
	 		}

	 		// IF ADMIN USER LOGIN
	 		else if(adminUserValueBean!=null) {
	         
	          roleId = adminUserValueBean.getRoleId();
	          regionID = adminUserValueBean.getRegionId();
	          circleID = adminUserValueBean.getCircleId();
	          divisionID = adminUserValueBean.getDivisionId();
	          subDivisionID = adminUserValueBean.getSubDivisionId();
	          sectionID = adminUserValueBean.getSectionId();
	 		}
	        
	        String hql = 
	        	    "SELECT a.id, " +
	        	    "       TO_CHAR(a.created_on, 'dd-mm-yyyy-hh24:mi') AS Complaint_Date, " +
	        	    "       DECODE(a.device, " +
	        	    "              'web', 'Web', " +
	        	    "              'FOC', 'FOC', " +
	        	    "              'admin', 'FOC', " +
	        	    "              'SM', 'Social Media', " +
	        	    "              'Android', 'Mobile', " +
	        	    "              'AMOB', 'Mobile', " +
	        	    "              'IMOB', 'Mobile', " +
	        	    "              'iOS', 'Mobile', " +
	        	    "              'mobile', 'Mobile', " +
	        	    "              'MI', 'Minnagam') AS Device, " +
	        	    "       a.SERVICE_NUMBER AS Service_Number, " +
	        	    "       a.SERVICE_NAME AS Service_Name, " +
	        	    "       a.SERVICE_ADDRESS AS Service_Address, " +
	                "       (CASE WHEN  a.device='MI' then ccm.CONTACTNO else b.mobile end) AS Contact_Number, "+
	        	    "       k.name AS Complaint_Type, " +
	        	    "       d.name AS subctyp, " +
	        	    "       a.description AS Complaint_Description, " +
	        	    "       DECODE(a.status_id, 0, 'Pending', 1, 'In Progress', 2, 'Completed') AS Complaint_Status, " +
	        	    "       (CASE WHEN a.status_id = 2 THEN TO_CHAR(a.updated_on, 'DD-MM-YYYY-HH24:MI') ELSE '' END) AS Attended_Date, " +
	        	    "       (CASE WHEN a.status_id = 2 THEN " +
	        	    "           (SELECT ch1.description FROM " +
	        	    "               (SELECT description, complaint_id, status_id " +
	        	    "                FROM COMPLAINT_HISTORY " +
	        	    "                WHERE complaint_id = a.id AND status_id = 2 " +
	        	    "                ORDER BY updated_on DESC) ch1 " +
	        	    "            WHERE ROWNUM = 1) " +
	        	    "        ELSE '' END) AS Attended_Remarks, " +
	        	    "       f.name AS Region, " +
	        	    "       g.name AS Circle, " +
	        	    "       h.name AS Division, " +
	        	    "       i.name AS SubDivision, " +
	        	    "       j.name AS Section " +
	        	    "FROM   COMPLAINT a " +
	        	    "       LEFT JOIN PUBLIC_USER b ON a.user_id = b.id " +
	        	    "       LEFT JOIN SUB_CATEGORY d ON a.sub_category_id = d.id " +
	        	    "       LEFT JOIN COMP_CONTACT_MAP ccm on ccm.COMP_ID=a.id "+		
	        	    "       JOIN CATEGORY k ON a.complaint_type = k.code " +
	        	    "       JOIN REGION f ON a.region_id = f.id " +
	        	    "       JOIN CIRCLE g ON a.circle_id = g.id " +
	        	    "       JOIN DIVISION h ON a.division_id = h.id " +
	        	    "       JOIN SUB_DIVISION i ON a.sub_division_id = i.id " +
	        	    "       JOIN SECTION j ON a.section_id = j.id " +
	        	    "WHERE  a.service_number = :consumerNumber " +
	        	    "AND    a.created_on BETWEEN :formattedFromDate AND :formattedToDate";


	        // Add role-based filtering
	        if(adminUserValueBean!=null) {
	         if (roleId >= 1 && roleId <= 5) {
	             hql += " AND a.region_id = :regionId ";
	             if (roleId <= 4) {
	                 hql += " AND a.circle_id = :circleId ";
	             }
	             if (roleId <= 3) {
	                 hql += " AND a.division_id = :divisionId ";
	             }
	             if (roleId <= 2) {
	                 hql += " AND a.sub_division_id = :subDivisionId ";
	             }
	             if (roleId == 1) {
	                 hql += " AND a.section_id = :sectionId ";
	             }
	         }else if (roleId==10) {
	        	 hql += " AND a.device = :device ";
	         }
	        }
			
	        else   if (callCenterValueBean != null && callCenterCircleId != null) {
	        	if(callCenterValueBean.getRoleId()==1) {
	        	 hql += " AND a.circle_id IN :callCenterCircleId ";
	        	}
	        	else if(callCenterValueBean.getRoleId()==5) {
	        		hql += " AND a.device = :device ";
	        	}
	        	else if(callCenterValueBean.getRoleId()==7) {
	        		hql += " AND a.circle_id IN :callCenterCircleId ";
	        		hql += " AND a.device = :device ";
	        	}
	        	else if(callCenterValueBean.getRoleId()==3) {
	        		hql += " AND a.device = :device ";
	        	}
	        	 
	         }
	        
	        Query query = session.createNativeQuery(hql);
	        query.setParameter("consumerNumber", consumerNumber);
	        query.setParameter("formattedFromDate", formattedFromDate);
	        query.setParameter("formattedToDate", formattedToDate);
	        
	     // Set parameters based on role
	     			if(adminUserValueBean!=null) {
	     	         if (roleId >= 1 && roleId <= 5) {
	     	             query.setParameter("regionId", regionID);
	     	             if (roleId <= 4) {
	     	                 query.setParameter("circleId", circleID);
	     	             }
	     	             if (roleId <= 3) {
	     	                 query.setParameter("divisionId", divisionID);
	     	             }
	     	             if (roleId <= 2) {
	     	                 query.setParameter("subDivisionId", subDivisionID);
	     	             }
	     	             if (roleId == 1) {
	     	                 query.setParameter("sectionId", sectionID);
	     	             }
	     	         }
	     	         else if (roleId==10) {
	     	        	 query.setParameter("device", "MI");
	     	         }
	     			}
	     			
	     			else   if (callCenterValueBean != null && callCenterCircleId != null) {
	     				//AE FOC
	     	        	if(callCenterValueBean.getRoleId()==1) {
	     	        	 query.setParameter("callCenterCircleId", callCenterCircleId);
	     	        	}
	     	        	// M ADMIN
	     	        	else if(callCenterValueBean.getRoleId()==5) {
	     	        	 query.setParameter("device", "MI");
	     	        	}
	     	        	// CIRCLE AGENT
	     	        	else if(callCenterValueBean.getRoleId()==7) {
	     		        	 query.setParameter("callCenterCircleId", callCenterCircleId);
	     		        	 query.setParameter("device", "MI");
	     	        	}
	     	        	// SM USERS
	     	        	else if(callCenterValueBean.getRoleId()==3) {
	     	        		query.setParameter("device", "SM");
	     	        	}	     	        	 
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
	        System.out.println("THE COMPLAINT LIST SIZE -----------------"+complaintList.size());
	        
	        if(complaintList.isEmpty() || complaintList==null) {
	        	FacesContext.getCurrentInstance().addMessage(null, 
		                new FacesMessage(FacesMessage.SEVERITY_FATAL, 
		                "No Data", "No Complaints For The Given Consumer Number And Given Date"));
		            return;
	        }
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void exportComplaintListToExcel(List<ViewComplaintReportValueBean> complaintList) throws IOException {
		
		
		
		System.out.println("THE COMPLAINT LIST ----------------------------------"+complaintList.size());
		
		
	    HSSFWorkbook workbook = new HSSFWorkbook();
	    Sheet sheet = workbook.createSheet("Consumer_Based_Complaints_Report");
	    
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
	    
	    Row titleRow = sheet.createRow(0);
	    Cell titleCell = titleRow.createCell(0);
	    titleCell.setCellValue("CONSUMER NUMBER BASED COMPLAINT LIST REPORT");
	    titleCell.setCellStyle(headingStyle);
	    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 16));
	    
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String formattedFromDate = sdf.format(dmFilter.getFromDate());
		String formattedToDate = sdf.format(dmFilter.getToDate());
	    
	    Row subtitleRow = sheet.createRow(1);
	    Cell subtitleCell = subtitleRow.createCell(0);
	    subtitleCell.setCellValue("FROM: " + formattedFromDate + "  |  TO: " + formattedToDate + " | "+"CONSUMER NUMBER :"+dmFilter.getConsumerNumber());
	    subtitleCell.setCellStyle(dateStyle);
	    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 16));
	    
	    Row headerRow = sheet.createRow(2);
	    
	    String[] headers = {
	        "S.No", "Complaint ID", "Complaint Date", "Complaint Mode", "Consumer Number", "Consumer Details",
	        "Contact Number", "Complaint Type", "Complaint Description", "Complaint Status", "Attended Date",
	        "Attended Remarks", "Region", "Circle", "Division", "Sub Division", "Section"
	    };
	    
	    for (int i = 0; i < headers.length; i++) {
	        Cell cell = headerRow.createCell(i);
	        cell.setCellValue(headers[i]);
	        cell.setCellStyle(headerStyle);
	    }
	    
	    int rowNum = 3;
	    int serialNumber = 1;
	    for (ViewComplaintReportValueBean complaint : complaintList) {
	        Row row = sheet.createRow(rowNum++);
	        row.createCell(0).setCellValue(serialNumber++);
	        row.createCell(1).setCellValue(complaint.getComplaintId().doubleValue());
	        row.createCell(2).setCellValue(complaint.getCreatedOnFormatted());
	        row.createCell(3).setCellValue(complaint.getDevice());
	        row.createCell(4).setCellValue(complaint.getServiceNumber());
	        row.createCell(5).setCellValue(complaint.getServiceAddress());
	        row.createCell(6).setCellValue(complaint.getMobile());
	        row.createCell(7).setCellValue(complaint.getComplaintType());
	        row.createCell(8).setCellValue(complaint.getComplaintDescription());
	        row.createCell(9).setCellValue(complaint.getComplaintStatusValue());
	        row.createCell(10).setCellValue(complaint.getAttendedDate());
	        row.createCell(11).setCellValue(complaint.getAttendedRemarks());
	        row.createCell(12).setCellValue(complaint.getRegionName());
	        row.createCell(13).setCellValue(complaint.getCircleName());
	        row.createCell(14).setCellValue(complaint.getDivisionName());
	        row.createCell(15).setCellValue(complaint.getSubDivisionName());
	        row.createCell(16).setCellValue(complaint.getSectionName());
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
	                .name("Consumer_Number_Based_ComplaintList_Report.xls")
	                .contentType("application/vnd.ms-excel")
	                .stream(() -> inputStream)
	                .build();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	// COMPLAINT LIST TO PDF
	public void exportComplaintListToPDF(List<ViewComplaintReportValueBean> complaintList) {
	    Document document = new Document(PageSize.A2.rotate());
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	    try {
	        PdfWriter.getInstance(document, outputStream);
	        document.open();
	        
	        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Font.BOLD, BaseColor.BLACK);
	        Paragraph title = new Paragraph("CONSUMER NUMBER BASED COMPLAINT LIST REPORT", titleFont);
	        title.setAlignment(Element.ALIGN_CENTER);
	        title.setSpacingAfter(5);
	        document.add(title);
	        
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			String formattedFromDate = sdf.format(dmFilter.getFromDate());
			String formattedToDate = sdf.format(dmFilter.getToDate());
	        
	        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY);
	        Paragraph subTitle = new Paragraph("FROM: " + formattedFromDate + "  |  TO: " + formattedToDate + " |  CONSUMER NUMBER : "+dmFilter.getConsumerNumber(), subtitleFont);
	        subTitle.setAlignment(Element.ALIGN_CENTER);
	        subTitle.setSpacingAfter(15);
	        document.add(subTitle);
	        
	        PdfPTable table = new PdfPTable(17); 
	        table.setWidthPercentage(100); 
	        table.setSpacingBefore(10); 
	        table.setSpacingAfter(10); 
	        
	        float[] columnWidths = {1.5f, 2f, 2f, 2f, 3f, 4f, 3f, 3f, 4f, 3f, 3f, 3f, 2f, 2f, 2f, 3f, 2f};
	        table.setWidths(columnWidths);
	        
	        String[] headers = {
	            "S.No", "Complaint ID", "Complaint Date", "Complaint Mode", "Consumer Number", "Consumer Details",
	            "Contact Number", "Complaint Type", "Complaint Description", "Complaint Status", "Attended Date",
	            "Attended Remarks", "Region", "Circle", "Division", "Sub Division", "Section"
	        };

	        for (String header : headers) {
	            PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD)));
	            cell.setPadding(5);
	            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
	            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
	            table.addCell(cell);
	        }

	        if (complaintList != null && !complaintList.isEmpty()) {
	            int serialNumber = 1;
	            for (ViewComplaintReportValueBean complaint : complaintList) {
	                addCell(table, String.valueOf(serialNumber++));
	                
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
	            noDataCell.setColspan(17);
	            noDataCell.setPadding(10);
	            noDataCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	            table.addCell(noDataCell);
	        }

	        document.add(table);
	        document.close();

	        pdfFile = DefaultStreamedContent.builder()
	            .contentType("application/pdf")
	            .name("Consumer_Number_Based_ComplaintList_Report.pdf")
	            .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
	            .build();

	    } catch (DocumentException e) {
	        e.printStackTrace();
	    }
	}

	private void addCell(PdfPTable table, String text) {
	    PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 9)));
	    cell.setPadding(5);
	    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
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
		  
		  cameFromInsideReport = true;
		    //REDIRECTED TO SINGLE COMPLAINT DETAIL PAGE.
		    FacesContext.getCurrentInstance().getExternalContext().redirect("consumerBasedComplaintDetail.xhtml");
        
        System.out.println("THE SELECED COMPLAINT ID"+ complaint.getComplaintId());
        System.out.println("THE SELECED COMPLAINT ID"+ complaint.getDescription());

		
		}catch(Exception e) {
			e.printStackTrace();
			
		}

		
	}
	
	public void returnToSearchPage() throws IOException {
	    FacesContext.getCurrentInstance().getExternalContext().redirect("consumerBasedComplaint.xhtml");
	}
	
	public void refresh() {
        dmFilter.setConsumerNumber(null);
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



	
	
	public List<ViewComplaintReportValueBean> getComplaintList() {
		return complaintList;
	}
	public void setComplaintList(List<ViewComplaintReportValueBean> complaintList) {
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
	public ViewComplaintReportValueBean getSelectedComplaintId() {
		return complaint;
	}


	public void setSelectedComplaintId(ViewComplaintReportValueBean complaint) {
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
