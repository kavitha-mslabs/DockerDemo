package tneb.ccms.admin.controller.reportController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.*;

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
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.valuebeans.AdminUserValueBean;
import tneb.ccms.admin.valuebeans.AgentDetailValueBean;
import tneb.ccms.admin.valuebeans.CallCenterUserValueBean;
import tneb.ccms.admin.valuebeans.OutagesReportValueBean;


@Named
@ViewScoped
public class UserDetailsReport implements Serializable {

	private static final long serialVersionUID = 1L;
	private boolean initialized = false;
	private SessionFactory sessionFactory;
	DataModel dmFilter;
	StreamedContent excelFile;
	StreamedContent pdfFile;
	private Date currentDate = new Date();
	private String currentYear = String.valueOf(Year.now().getValue());

    List<AgentDetailValueBean> resultList;
    List<AgentDetailValueBean> logDetailForAgent;
    
    List<String> userNames;
    

	


	public List<String> getUserNames() {
		return userNames;
	}




	public void setUserNames(List<String> userNames) {
		this.userNames = userNames;
	}




	public List<AgentDetailValueBean> getLogDetailForAgent() {
		return logDetailForAgent;
	}




	public void setLogDetailForAgent(List<AgentDetailValueBean> logDetailForAgent) {
		this.logDetailForAgent = logDetailForAgent;
	}


	@PostConstruct
	public void init() {
		System.out.println("Initializing USER DETAILS REPORT...");
		sessionFactory = HibernateUtil.getSessionFactory();
		dmFilter = new DataModel();
		
		userNames =new ArrayList<String>();
		
		loadUserNamesForFilter();
	}
	
	@Transactional
	public void loadUserNamesForFilter() {
		Session session = null;
		session = sessionFactory.openSession();
		
		String sql = "SELECT (m.user_name || '-' || m.name) FROM MINNAGAM_USER_REQUEST m WHERE m.status_id = 1 AND m.USER_NAME IS NOT NULL ";
				
		
		HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false);
        AdminUserValueBean adminUserValueBean = (AdminUserValueBean) httpsession.getAttribute("sessionAdminValueBean");
        CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");

        if(callCenterValueBean!=null && (callCenterValueBean.getRoleId()==1 || callCenterValueBean.getRoleId()==7 )) {
        	sql+="AND m.circle_id IN :circleId ";
        }
        sql+= "ORDER BY m.USER_NAME";
		Query query = session.createSQLQuery(sql);
		
        if(callCenterValueBean!=null && (callCenterValueBean.getRoleId()==1 || callCenterValueBean.getRoleId()==7 )) {
        	int userId = callCenterValueBean.getId();
			@SuppressWarnings("unchecked")
			List<Integer> circleId = session.createQuery(
                    "select c.circleBean.id from CallCenterMappingBean c " +
                    "where c.callCenterUserBean.id = :userId")
                    .setParameter("userId", userId)
                    .getResultList();
			query.setParameter("circleId", circleId);
        }
		
		List<String> result =query.getResultList();
		
		this.userNames = result;
		System.out.println("THE USERNAMES LOADED-----------"+userNames.size());
		
		
	}
    
	public void resetIfNeeded() {
        if (!FacesContext.getCurrentInstance().isPostback() && initialized) {
        	resultList = null;
        	logDetailForAgent =null;
        	dmFilter.setFromDate(null);
        	dmFilter.setToDate(null);
        }
        initialized = true;
    }




	@Transactional
	public void getUserDetailReport() {
		
		if (dmFilter.getFromDate() == null) {
		    Calendar cal = Calendar.getInstance();
		    cal.setTime(new Date()); 
		    dmFilter.setFromDate(cal.getTime());
		}

		Session session = null;
		try {
			session = sessionFactory.openSession();
			session.beginTransaction();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String formattedFromDate = sdf.format(dmFilter.getFromDate());
			
			HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
	                .getExternalContext().getSession(false);
	        AdminUserValueBean adminUserValueBean = (AdminUserValueBean) httpsession.getAttribute("sessionAdminValueBean");
	        CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");

	        
			
			String sql = "SELECT e.user_name, "
	                   + "EXTRACT(HOUR FROM e.LOGINDT) || ':' || EXTRACT(MINUTE FROM e.LOGINDT) AS login_time, "
	                   + "EXTRACT(HOUR FROM e.LOGOUTDT) || ':' || EXTRACT(MINUTE FROM e.LOGOUTDT) AS logout_time "
	                   + "FROM entryuser_log e "
	                   + "JOIN minnagam_user_request m on m.user_name= e.user_name "
	                   + "WHERE TRUNC(e.LOGINDT) = TO_DATE(:fromDate, 'yyyy-MM-dd') ";
//	                   + "ORDER BY e.id";
			
			if(callCenterValueBean!=null) {
				if(callCenterValueBean.getRoleId()==1 || callCenterValueBean.getRoleId()==7) {
					sql +="AND m.circle_id IN :circleId ";
				}
			}
			
			sql+="ORDER BY e.id";
			
			Query query = session.createSQLQuery(sql);
	        query.setParameter("fromDate", formattedFromDate);
	        
			if(callCenterValueBean!=null) {
				if(callCenterValueBean.getRoleId()==1 || callCenterValueBean.getRoleId()==7) {
					int userId = callCenterValueBean.getId();
					@SuppressWarnings("unchecked")
					List<Integer> circleId = session.createQuery(
                            "select c.circleBean.id from CallCenterMappingBean c " +
                            "where c.callCenterUserBean.id = :userId")
                            .setParameter("userId", userId)
                            .getResultList();
					query.setParameter("circleId", circleId);
				}
			}
	        
	        @SuppressWarnings("unchecked")
		    List<Object[]> agentList = query.list();

	        resultList = new ArrayList<AgentDetailValueBean>();
	        
	        for (Object[] row : agentList) {
	        	AgentDetailValueBean dto = new AgentDetailValueBean();
	            dto.setUserName((String) row[0]);
	            dto.setLoginTime(row[1].toString());
	            dto.setLogoutTime(row[2].toString());
	            
	            resultList.add(dto);
	        }
	        
	        session.getTransaction().commit();
	        System.out.println("THE AGENT DETAILS ---------------"+resultList.size());
	        
	        if(resultList.size()==0) {
	        	FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
		                "", "No Log Details For The Given Date");
		            FacesContext.getCurrentInstance().addMessage(null, message);
		            return;
	        }
			
		 } catch (Exception e) {
		        e.printStackTrace();
		        System.out.println("Error in database operation");
		        if (session != null) {
		            session.getTransaction().rollback();
		        }
		    } finally {
		        if (session != null) {
		            session.close();
		        }
		    }
		}
	
	
	
	@Transactional
	public void getDetailReportForAgentAndDateRange() {
		
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
		
		if(dmFilter.getUserName()==null || dmFilter.getUserName().isEmpty()) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
	                "Error", "User Name Cannot Be Empty");
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
			
			String userNameWithName = dmFilter.getUserName();
			
			String userName = userNameWithName.substring(0, userNameWithName.indexOf("-"));
			
			
			String sql = "SELECT TO_CHAR(logindt, 'dd-MM-yyyy HH24:MI:SS') AS logindt, TO_CHAR(logoutdt, 'dd-MM-yyyy HH24:MI:SS') AS logoutdt "
	                   + "FROM entryuser_log "
	                   + "WHERE user_name= :userName AND TRUNC(LOGINDT) >= TO_DATE(:fromDate, 'yyyy-MM-dd') "
	                   + "AND TRUNC(LOGOUTDT) <= TO_DATE(:toDate, 'yyyy-MM-dd') ";
			
			Query query = session.createSQLQuery(sql);
	        query.setParameter("userName", userName);
	        query.setParameter("fromDate", formattedFromDate);
	        query.setParameter("toDate", formattedToDate);

	        
	        @SuppressWarnings("unchecked")
			List<Object[]> agentList = query.list();

			logDetailForAgent = new ArrayList<AgentDetailValueBean>();
	        
	        for (Object[] row : agentList) {
	        	AgentDetailValueBean dto = new AgentDetailValueBean();
	            dto.setLoginTime(row[0].toString());
	            dto.setLogoutTime(row[1].toString());
	            
	            logDetailForAgent.add(dto);
	        }
	        
	        session.getTransaction().commit();
	        System.out.println("THE LOG DETAILS FOR AGENT---------------"+logDetailForAgent.size());
	        
	        if(logDetailForAgent.size()==0) {
	        	FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
		                "", "No Detail For the Given Date And The Given User");
		            FacesContext.getCurrentInstance().addMessage(null, message);
		            return;
	        }
			
		 } catch (Exception e) {
		        e.printStackTrace();
		        System.out.println("Error in database operation");
		        if (session != null) {
		            session.getTransaction().rollback();
		        }
		    } finally {
		        if (session != null) {
		            session.close();
		        }
		    }
		}
	
	public void clearUserDetailReport() {
		dmFilter= new DataModel();
		dmFilter.setFromDate(null);
		resultList = new ArrayList<AgentDetailValueBean>();
	}
	public void clearLogDetailForAgentAndDateRangeReport() {
		dmFilter= new DataModel();
		dmFilter.setFromDate(null);
		dmFilter.setToDate(null);
		logDetailForAgent = new ArrayList<AgentDetailValueBean>();
	}
	
	public void exportToExcel(List<AgentDetailValueBean> reportData) throws IOException {
		 HSSFWorkbook workbook = new HSSFWorkbook();
		  Sheet sheet = workbook.createSheet("User_Detail_Report");
		  
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
		    
		    Row headingRow = sheet.createRow(0);
		    Cell headingCell = headingRow.createCell(0);
		    headingCell.setCellValue("USER DETAIL REPORT");
		    headingCell.setCellStyle(headingStyle);
		    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4)); 
		    
		    Row dateRow = sheet.createRow(1);
		    Cell dateCell = dateRow.createCell(0);
		    
		    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		    String fromDateStr = dmFilter.getFromDate() != null ? 
		                         dateFormat.format(dmFilter.getFromDate()) : "N/A";
		    
		    dateCell.setCellValue("Date: " + fromDateStr );
		    dateCell.setCellStyle(dateStyle);
		    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));
		    
		  
		  Row headerRow = sheet.createRow(2);
		  
		  String[] headers = {
			        "S.NO","USER NAME", "LOGIN TIME", "LOGOUT TIME"
			    };
        
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        int rowNum = 3;
        int serialNo = 1;
        for (AgentDetailValueBean agent : reportData) {
            Row row = sheet.createRow(rowNum++);
            int col = 0;
            row.createCell(col++).setCellValue(serialNo++); 
            row.createCell(col++).setCellValue(agent.getUserName());
            row.createCell(col++).setCellValue(agent.getLoginTime());
            row.createCell(col++).setCellValue(agent.getLogoutTime());
             
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
		                .name("User_Detail_Report.xls")
		                .contentType("application/vnd.ms-excel")
		                .stream(() -> inputStream)
		                .build();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
	  }
	
	
	public void exportLogDetailForAgentToExcel(List<AgentDetailValueBean> reportData) throws IOException {
		 HSSFWorkbook workbook = new HSSFWorkbook();
		  Sheet sheet = workbook.createSheet("Log_Detail_For_Agent_And_DateRange");
		  
		  int[] columnWidths = {2000, 8000, 8000};
		    for (int i = 0; i < columnWidths.length; i++) {
		        sheet.setColumnWidth(i, columnWidths[i]);
		    }
		    
		    Row headingRow = sheet.createRow(0);
		    Cell headingCell = headingRow.createCell(0);
		    headingCell.setCellValue("LOG DETAIL FOR AGENT AND GIVEN DATE RANGE");
		    
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
		    
		    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));
		    
		    
		    CellStyle dateStyle = workbook.createCellStyle();
		    HSSFFont dateFont = workbook.createFont();
		    dateFont.setBold(true);
		    dateFont.setFontHeightInPoints((short) 10);
		    dateStyle.setFont(dateFont);
		    dateStyle.setAlignment(HorizontalAlignment.CENTER);
		    dateStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		    
		    Row dateRow = sheet.createRow(1);
		    Cell dateCell = dateRow.createCell(0);
		    
		    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		    String fromDateStr = dmFilter.getFromDate() != null ? 
		                         dateFormat.format(dmFilter.getFromDate()) : "N/A";
		    String toDateStr = dmFilter.getToDate() != null ? 
                    dateFormat.format(dmFilter.getToDate()) : "N/A";
		    
		    dateCell.setCellValue("From Date: " + fromDateStr + " | "+ "To Date: "+toDateStr+ " | "+"User Name: "+dmFilter.getUserName());
		    dateCell.setCellStyle(dateStyle);
		    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 2));
		    
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
		    
		    Row mainHeaderRow = sheet.createRow(2);
		    Cell circleHeader = mainHeaderRow.createCell(0);
		    circleHeader.setCellValue("S.NO");
		    circleHeader.setCellStyle(headerStyle);
		    
		    Cell dateHeader = mainHeaderRow.createCell(1);
		    dateHeader.setCellValue(dmFilter.getUserName());
		    dateHeader.setCellStyle(headerStyle);
		    
		    sheet.addMergedRegion(new CellRangeAddress(2, 2, 1, 2));
		    
		    Row subHeaderRow = sheet.createRow(3);
		    String[] subHeaders = {"LOGIN TIME", "LOGOUT TIME"};

		    for (int i = 0; i < subHeaders.length; i++) {
		        Cell cell = subHeaderRow.createCell(i + 1); 
		        cell.setCellValue(subHeaders[i]);
		        cell.setCellStyle(headerStyle);
		    }

       int rowNum = 4;
       int serialNo = 1;
       for (AgentDetailValueBean agent : reportData) {
           Row row = sheet.createRow(rowNum++);
           int col = 0;
           row.createCell(col++).setCellValue(serialNo++); 
           row.createCell(col++).setCellValue(agent.getLoginTime());
           row.createCell(col++).setCellValue(agent.getLogoutTime());
            
       }
       
       ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    workbook.write(outputStream);
		    workbook.close();

		    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		    try {
		        excelFile = DefaultStreamedContent.builder()
		                .name("Log_Detail_For_Agent_And_DateRange.xls")
		                .contentType("application/vnd.ms-excel")
		                .stream(() -> inputStream)
		                .build();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
	  }
	
	public void exportLogDetailForAgentToPdf(List<AgentDetailValueBean> reportData) throws IOException {
	    try {
	        Document document = new Document(PageSize.A4);
	        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	        PdfWriter.getInstance(document, outputStream);

	        document.open();

	        Font titleFont = new Font(Font.FontFamily.HELVETICA, 15, Font.BOLD, BaseColor.DARK_GRAY);
	        Font subHeadingFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY);
	        Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
	        Font mainHeadingFont = new Font(Font.FontFamily.HELVETICA, 15, Font.BOLD, BaseColor.WHITE);

	        Font totalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);

	        Paragraph title = new Paragraph("LOG DETAIL FOR AGENT AND GIVEN DATE RANGE", titleFont);
	        title.setAlignment(Element.ALIGN_CENTER);
	        title.setSpacingAfter(20);
	        document.add(title);

	        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

	        String fromDateStr = dmFilter.getFromDate() != null ? dateFormat.format(dmFilter.getFromDate()) : "N/A";
	        String toDateStr = dmFilter.getToDate() != null ? dateFormat.format(dmFilter.getToDate()) : "N/A";

	        Paragraph subHeading = new Paragraph("FROM DATE : " + fromDateStr + " | " + "TO DATE :" + toDateStr + " | " + "USER NAME :" + dmFilter.getUserName());
	        subHeading.setAlignment(Element.ALIGN_CENTER);
	        subHeading.setSpacingAfter(15);
	        document.add(subHeading);

	        PdfPTable table = new PdfPTable(3);
	        table.setWidthPercentage(100);

	        float[] columnWidths = {20f, 55f, 55f};
	        table.setWidths(columnWidths);

	        PdfPCell cell1 = new PdfPCell(new Phrase("S.NO", headerFont));
	        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
	        cell1.setBackgroundColor(BaseColor.GRAY);
	        cell1.setPadding(10);
	        cell1.setRowspan(2);
	        table.addCell(cell1);

	        PdfPCell cell2 = new PdfPCell(new Phrase(dmFilter.getUserName(), mainHeadingFont));
	        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
	        cell2.setBackgroundColor(BaseColor.GRAY);
	        cell2.setPadding(10);
	        cell2.setColspan(2); // changed from 4 to 2 since table has 3 columns in total
	        table.addCell(cell2);

	        String[] subHeaders = {"LOGIN TIME", "LOGOUT TIME"};
	        for (String header : subHeaders) {
	            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
	            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	            cell.setBackgroundColor(BaseColor.GRAY);
	            cell.setPadding(10);
	            table.addCell(cell);
	        }

	        int sno = 1;
	        for (AgentDetailValueBean report : reportData) {
	            table.addCell(String.valueOf(sno++));
	            addNumericCell(table, report.getLoginTime());
	            addNumericCell(table, report.getLogoutTime());
	        }

	        document.add(table);
	        document.close();

	        pdfFile = DefaultStreamedContent.builder()
	                .contentType("application/pdf")
	                .name("Log_Detail_For_Agent_And_Given_DateRange.pdf")
	                .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
	                .build();

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	    
private void addNumericCell(PdfPTable table, String value) {
    PdfPCell cell = new PdfPCell(new Phrase(value.toString()));
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    cell.setPadding(5);
    table.addCell(cell);
}
	
	 public void exportToPdf(List<AgentDetailValueBean> reportData) throws IOException {

		    final int COLUMN_COUNT = 4;
		    final float SNO_COLUMN_WIDTH = 3f;
		    final float OTHER_COLUMN_WIDTH = 15f;
		    final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
		    final Font TOTAL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
		    final Font DATA_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
		    final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);


		    
		    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		    String fromDate = sdf.format(dmFilter.getFromDate());
		    

		    Document document = new Document(PageSize.A4);
		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    
		    try {
		        PdfWriter.getInstance(document, outputStream);
		        document.open();
		        
		        PdfPTable table = new PdfPTable(COLUMN_COUNT);
		        table.setWidthPercentage(100);
		        table.setSpacingBefore(10f);
		        
		        Paragraph title = new Paragraph("USER DETAIL REPORT", TITLE_FONT);
		        title.setAlignment(Element.ALIGN_CENTER);
		        document.add(title);
		        
		        Paragraph subTitle = new Paragraph("DATE : " + fromDate );
		        subTitle.setAlignment(Element.ALIGN_CENTER);
		        subTitle.setSpacingAfter(10);
		        document.add(subTitle);
		        
		        float[] columnWidths = new float[COLUMN_COUNT];
		        columnWidths[0] = SNO_COLUMN_WIDTH;
		        for (int i = 1; i < COLUMN_COUNT; i++) {
		            columnWidths[i] = OTHER_COLUMN_WIDTH;
		        }
		        table.setWidths(columnWidths);
		        
		        String[] headers = {"S.NO","USER NAME","LOGIN TIME","LOGOUT TIME"};
		        for(String header :headers) {
		        	PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
		            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
		            table.addCell(cell);
		        }
		        int sno = 1;
		        
		        for (AgentDetailValueBean report : reportData) {
		        	table.addCell(createDataCell(String.valueOf(sno++), DATA_FONT, Element.ALIGN_CENTER));
		            
		            // USER NAME
		            table.addCell(createDataCell(report.getUserName(), DATA_FONT, Element.ALIGN_LEFT));
		            
		            // LOGIN TIME
		            table.addCell(createDataCell(report.getLoginTime(), DATA_FONT, Element.ALIGN_LEFT));
		            
		            // LOGOUT TIME
		            table.addCell(createDataCell(report.getLogoutTime(), DATA_FONT, Element.ALIGN_LEFT));
		         }
		        
		        document.add(table);
		        document.close();
		        
		        pdfFile = DefaultStreamedContent.builder()
		            .contentType("application/pdf")
		            .name("User_Detail_Report.pdf")
		            .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
		            .build();

		    
		    
		    }catch(Exception e) {
		    	e.printStackTrace();
		    }
		    }
		    
		    private PdfPCell createDataCell(String text, Font font, int alignment) {
			    PdfPCell cell = new PdfPCell(new Phrase(text, font));
			    cell.setHorizontalAlignment(alignment);
			    return cell;
			}
		  




	public List<AgentDetailValueBean> getResultList() {
		return resultList;
	}




	public void setResultList(List<AgentDetailValueBean> resultList) {
		this.resultList = resultList;
	}




	public DataModel getDmFilter() {
		return dmFilter;
	}




	public void setDmFilter(DataModel dmFilter) {
		this.dmFilter = dmFilter;
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




	public String getCurrentYear() {
		return currentYear;
	}




	public void setCurrentYear(String currentYear) {
		this.currentYear = currentYear;
	}

	


}
