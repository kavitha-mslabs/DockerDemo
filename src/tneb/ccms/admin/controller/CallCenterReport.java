package tneb.ccms.admin.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import tneb.ccms.admin.AdminMain;
import tneb.ccms.admin.dao.ComplaintsDao;
import tneb.ccms.admin.model.CircleBean;
import tneb.ccms.admin.model.LoginParams;
import tneb.ccms.admin.model.ViewComplaintReportBean;
import tneb.ccms.admin.util.CCMSConstants;
import tneb.ccms.admin.util.GeneralUtil;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.valuebeans.CircleValueBean;
import tneb.ccms.admin.valuebeans.ReportValueBean;
import tneb.ccms.admin.valuebeans.ViewComplaintReportValueBean;

public class CallCenterReport {

	private Logger logger = LoggerFactory.getLogger(CallCenterReport.class.getName());

	@ManagedProperty("#{admin}")
	AdminMain admin;

	private LoginParams officer;
	private String[] circleFilter;
	private String[] circleDivisionFilter;
	
	private List<CircleValueBean> listCircle;
	
	ViewComplaintReportValueBean selectedComplaintId;
	
	private Date fromDate;
	private Date toDate;
	private Date fromDateTwo;
	private Date toDateTwo;
	private Date currentDate = new Date();
	private Date minDate = new Date();
	
	public Date getMinDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(2021, Calendar.JANUARY, 1, 0, 0, 0);
		minDate = cal.getTime();
		return minDate;
	}

	public CallCenterReport() {
		super();
		init();

	}

	public void init() {
		admin = (AdminMain) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("admin");
		officer = admin.getAuth().getOfficer();
	}

	public void setFilter() {
		listCircles(officer.getCircleIdList());
	}

	public void listCircles(List<Integer> circleIdList) {

		SessionFactory factory = null;
		Session session = null;
		try {

			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();

			List<CircleValueBean> circleList = new ArrayList<CircleValueBean>();
			CircleValueBean circleValueBean = null;

			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<Object[]> criteriaQuery = criteriaBuilder.createQuery(Object[].class);
			Root<CircleBean> root = criteriaQuery.from(CircleBean.class);

			// Constructing list of parameters
			List<Predicate> predicates = new ArrayList<Predicate>();

			Expression<String> circleExpression = root.get("id");
			Predicate circlePredicate = circleExpression.in(circleIdList);
			predicates.add(circlePredicate);

			criteriaQuery.multiselect(root.get("id"), root.get("name")).where(predicates.toArray(new Predicate[]{}))
			.orderBy(criteriaBuilder.asc(root.get("name")));

			Query<Object[]> query = session.createQuery(criteriaQuery);
			
			List<?> rows = query.list();
			
			if (rows.size() > 0) {
				Object[] row = null;
				for (int i = 0; i < rows.size(); i++) {
					row = (Object[]) rows.get(i);
					circleValueBean = new CircleValueBean();
					circleValueBean.setId(Integer.parseInt(row[0].toString()));
					circleValueBean.setName(row[1].toString());
					circleList.add(circleValueBean);
				}
			}

			setListCircle(circleList);
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

	}
	
public void getComplaintDetailForAbstractReport() {
		
		String complaintIdParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("complaintID");
		
		if (complaintIdParam != null && !complaintIdParam.isEmpty()) {
	        Long complaintId = Long.parseLong(complaintIdParam); 
	        
	        selectedComplaintId = new ViewComplaintReportValueBean();
	        SessionFactory factory = null;
	        Session session = null;

	        try {
	            factory = HibernateUtil.getSessionFactory();
	            session = factory.openSession();

	            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
	            CriteriaQuery<ViewComplaintReportBean> criteriaQuery = criteriaBuilder.createQuery(ViewComplaintReportBean.class);
	            Root<ViewComplaintReportBean> root = criteriaQuery.from(ViewComplaintReportBean.class);

	            criteriaQuery.select(root)
	                         .where(criteriaBuilder.equal(root.get("id"), complaintId)); 

	            Query<ViewComplaintReportBean> query = session.createQuery(criteriaQuery);
	            ViewComplaintReportBean complaint = query.uniqueResult(); 

	            if(complaint != null) {
	            	selectedComplaintId = ViewComplaintReportValueBean.convertViewComplaintReportBeanToViewComplaintReportValueBean(complaint);
					
//					CriteriaQuery<ComplaintHistoryBean> historyQuery = criteriaBuilder.createQuery(ComplaintHistoryBean.class);
//		            Root<ComplaintHistoryBean> historyRoot = historyQuery.from(ComplaintHistoryBean.class);
//		            
//		            historyQuery.select(historyRoot)
//                    .where(criteriaBuilder.equal(historyRoot.get("complaintBean").get("id"), complaintId))
//                    .orderBy(criteriaBuilder.desc(historyRoot.get("createdOn"))); 
//		            
//		            Query<ComplaintHistoryBean> historyResult = session.createQuery(historyQuery);
//	                historyResult.setMaxResults(1);
	                
//	                ComplaintHistoryBean latestHistory = historyResult.uniqueResult();
	                
//	                if (latestHistory != null) {
//	                   
//	                    String latestDescription = latestHistory.getDescription();
//	                    selectedComplaintId.setFinalDescription(latestDescription);
//	                }
				} 
	        } catch (Exception e) {
	            logger.error(ExceptionUtils.getStackTrace(e));
	            FacesContext.getCurrentInstance().addMessage(null, 
	                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "An error occurred while fetching complaint details"));
	        } finally {
	            HibernateUtil.closeSession(factory, session);
	        }
	    } else {
	        FacesContext.getCurrentInstance().addMessage(null, 
	            new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "No complaint ID selected"));
	    }
	}
	
	
	public void generateCircleReport(String reportType) throws IOException {
		
		boolean validSearch = true;
		
		if (fromDate != null && toDate != null) {
			if(fromDate.after(toDate)) {
				validSearch = false;
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "To date can't be lesser than from date!"));
			} else {
				long dateDifference = GeneralUtil.findDifference(toDate, fromDate, "day");
				if(dateDifference > 31) {
					validSearch = false;
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Date difference between from date and to date can't be more than a month!"));
				}
			}
		} else if(fromDate == null) {
			validSearch = false;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please select from date."));
		} else if(toDate == null) {
			validSearch = false;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please select to date."));
		}
//		if(circleFilter.length == 0) {
//			validSearch = false;
//			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please select circle."));
//		}
		
		if(validSearch) {
			if(CCMSConstants.PDF_EXPORT.equalsIgnoreCase(reportType)) {
				exportCirclePdfReport();
			} else {
				exportCircleExcelReport();
			}
		}
		
	}
	
	public void generateCircleDivisionReport(String reportType) throws IOException {
		
		boolean validSearch = true;
		
		if (fromDateTwo != null && toDateTwo != null) {
			if(fromDateTwo.after(toDateTwo)) {
				validSearch = false;
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "To date can't be lesser than from date!"));
			} else {
				long dateDifference = GeneralUtil.findDifference(toDateTwo, fromDateTwo, "day");
				if(dateDifference > 31) {
					validSearch = false;
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Date difference between from date and to date can't be more than a month!"));
				}
			}
		} else if(fromDateTwo == null) {
			validSearch = false;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please select from date."));
		} else if(toDateTwo == null) {
			validSearch = false;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please select to date."));
		}
//		if(circleDivisionFilter.length == 0) {
//			validSearch = false;
//			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please select circle."));
//		}
		
		if(validSearch) {
			if(CCMSConstants.PDF_EXPORT.equalsIgnoreCase(reportType)) {
				exportCircleDivisionPdfReport();
			} else {
				exportCircleDivisionExcelReport();
			}
		}
		
	}
	
	
	public void exportCircleExcelReport() throws IOException {


		ComplaintsDao complaintsDao = new ComplaintsDao();
		
		FacesContext context = FacesContext.getCurrentInstance();
		
		HSSFWorkbook workbook = new HSSFWorkbook();
		OutputStream fos = null;
		try {
			
			List<ReportValueBean> circleReportList = complaintsDao.getCircleReport(circleFilter, fromDate, toDate, officer);
			
			HSSFSheet sheet = workbook.createSheet();
			
			sheet.setColumnWidth(0, 2500);
			sheet.setColumnWidth(1, 7000);
			sheet.setColumnWidth(2, 4500);
			sheet.setColumnWidth(3, 4500);
			sheet.setColumnWidth(4, 4500);
			sheet.setColumnWidth(5, 4500);
			sheet.setColumnWidth(6, 4500);
			sheet.setColumnWidth(7, 5000);
			sheet.setColumnWidth(8, 4000);
			
			HSSFRow row;
			HSSFCell cell;
			HSSFCellStyle cellStyle= workbook.createCellStyle();
			HSSFCellStyle boldCellStyle= workbook.createCellStyle();
			HSSFCellStyle blueCellStyle= workbook.createCellStyle();

			HSSFFont defaultFont = workbook.createFont();
			defaultFont.setFontHeightInPoints((short) 10);
			defaultFont.setFontName("Arial");
			defaultFont.setColor(IndexedColors.BLACK.getIndex());
			defaultFont.setBold(false);
			defaultFont.setItalic(false);
			
			HSSFFont boldFont = workbook.createFont();
			boldFont.setFontHeightInPoints((short) 10);
			boldFont.setFontName("Arial");
			boldFont.setColor(IndexedColors.BLACK.getIndex());
			boldFont.setBold(true);
			boldFont.setItalic(false);

			HSSFFont font = workbook.createFont();
			font.setFontHeightInPoints((short) 10);
			font.setFontName("Arial");
			font.setColor(IndexedColors.WHITE.getIndex());
			font.setBold(true);
			font.setItalic(false);

			
			int rowCounter = 0;
			
			row = sheet.createRow(rowCounter++);
			row = sheet.createRow(rowCounter++);
			
		
			boldCellStyle.setFont(boldFont);
			cell = row.createCell(1);
			cell.setCellValue("Call Center - Circle Calls Report Generated On");
			cell.setCellStyle(boldCellStyle);
			sheet.addMergedRegion(new CellRangeAddress(1,1,1,2));
			cellStyle = workbook.createCellStyle();
			cellStyle.setFont(defaultFont);
		    cell = row.createCell(3);
			cell.setCellValue(GeneralUtil.formatDate(new Date()));
			cell.setCellStyle(cellStyle);
			
			row = sheet.createRow(rowCounter++);
			row = sheet.createRow(rowCounter++);
			
			boldCellStyle.setFont(boldFont);
			
			if (fromDate != null) {
				cell = row.createCell(1);
				cell.setCellValue("From Date");
				cell.setCellStyle(boldCellStyle);
				cellStyle.setFont(defaultFont);
			    cell = row.createCell(3);
				cell.setCellValue(GeneralUtil.formatDate(fromDate));
				cell.setCellStyle(cellStyle);
			}
			
			row = sheet.createRow(rowCounter++);
			
			boldCellStyle.setFont(boldFont);
			
			if (toDate != null) {
				cell = row.createCell(1);
				cell.setCellValue("To Date");
				cell.setCellStyle(boldCellStyle);
				cellStyle.setFont(defaultFont);
				cell = row.createCell(3);
				cell.setCellValue(GeneralUtil.formatDate(toDate));
				cell.setCellStyle(cellStyle);
				
			}
			
			row = sheet.createRow(rowCounter++);
			row = sheet.createRow(rowCounter++);
			
			blueCellStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
			blueCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			blueCellStyle.setFont(font);
			blueCellStyle.setWrapText(true);
			
			cell = row.createCell(0);
			cell.setCellValue("S.No");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(1);
			cell.setCellValue("Circle Name");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(2);
			cell.setCellValue("Total Complaint Registered");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(3);
			cell.setCellValue("Closed in Less than 1 Hr");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(4);
			cell.setCellValue("Closed in Less than 3 Hrs (1 - 3)");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(5);
			cell.setCellValue("Closed in Less than 6 Hrs (3 - 6)");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(6);
			cell.setCellValue("Closed in Less than 12 Hrs (6 - 12)");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(7);
			cell.setCellValue("Closed in Less than 24 Hrs (12 - 24)");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(8);
			cell.setCellValue("Pending Complaint");
			cell.setCellStyle(blueCellStyle);
			
		    Integer totalCompRegistered = 0;
		    Integer compPending = 0;
		    
	        for(ReportValueBean reportValueBean : circleReportList) {
	        	totalCompRegistered = totalCompRegistered + reportValueBean.getTotalCompRegistered();
	        	compPending = compPending + reportValueBean.getCompPending();
	        	
	        	row = sheet.createRow(rowCounter++);
				
				cellStyle.setFont(defaultFont);
				
				cell = row.createCell(0);
				cell.setCellValue(reportValueBean.getSerialNumber());
				cell.setCellStyle(cellStyle);
				cell = row.createCell(1);
				cell.setCellValue(reportValueBean.getCircleName());
				cell.setCellStyle(cellStyle);
				cell = row.createCell(2);
				cell.setCellValue(reportValueBean.getTotalCompRegistered());
				cell.setCellStyle(cellStyle);
				cell = row.createCell(3);
				cell.setCellValue(reportValueBean.getTotalCompRegisteredOneHour());
				cell.setCellStyle(cellStyle);
				cell = row.createCell(4);
				cell.setCellValue(reportValueBean.getTotalCompRegisteredThreeHour());
				cell.setCellStyle(cellStyle);
				cell = row.createCell(5);
				cell.setCellValue(reportValueBean.getTotalCompRegisteredSixHour());
				cell.setCellStyle(cellStyle);
				cell = row.createCell(6);
				cell.setCellValue(reportValueBean.getTotalCompRegisteredTwelveHour());
				cell.setCellStyle(cellStyle);
				cell = row.createCell(7);
				cell.setCellValue(reportValueBean.getTotalCompRegisteredOneDay());
				cell.setCellStyle(cellStyle);
				cell = row.createCell(8);
				cell.setCellValue(reportValueBean.getCompPending());
				cell.setCellStyle(cellStyle);
				
	        }
	        
	        row = sheet.createRow(rowCounter++);
			
			cellStyle.setFont(defaultFont);
			
	        cell = row.createCell(2);
	        cell.setCellValue(totalCompRegistered);
			cell.setCellStyle(cellStyle);
			cell = row.createCell(8);
			cell.setCellValue(compPending);
			cell.setCellStyle(cellStyle);
			

			HttpServletResponse res = getResponse();
			res.setContentType("application/vnd.ms-excel");
			res.setHeader("Content-disposition", "attachment; filename=CallCircleReport-"+GeneralUtil.formatDate(new Date())+".xls");

			fos = res.getOutputStream();

			workbook.write(fos);
			
			fos.flush();
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			if (fos != null) {
				fos.close();
			}
			if (workbook != null) {
				workbook.close();
			}
		}

		context.responseComplete();
		context.renderResponse();
	
	
	}
	
	public void exportCirclePdfReport() throws IOException {

		FacesContext context = FacesContext.getCurrentInstance();
		Document document = new Document();
		PdfWriter writer = null;
		OutputStream fos = null;
		PdfPCell cell = null;
		try {
			
			ComplaintsDao complaintsDao = new ComplaintsDao();
			
			List<ReportValueBean> circleReportList = complaintsDao.getCircleReport(circleFilter, fromDate, toDate, officer);
			
		    HttpServletResponse res = getResponse();
			 
			fos = res.getOutputStream();

			writer = PdfWriter.getInstance(document, fos);
			
	        document.open();
	        
	        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
			Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.NORMAL);
			
			PdfPTable dateTable = new PdfPTable(2); // 2 columns.
			dateTable.setWidthPercentage(100); //Width 100%
			dateTable.setSpacingBefore(10f); //Space before table
			dateTable.setSpacingAfter(10f); //Space after table
			
			//Set Column widths
			dateTable.setWidths(new float[]{3, 1});
			
			cell = new PdfPCell(new Paragraph("Call Center - Circle Calls Report Generated On", boldFont));
			dateTable.addCell(cell);
		    cell = new PdfPCell(new Paragraph(GeneralUtil.formatDate(new Date()), normalFont));
		    dateTable.addCell(cell);
		    
		    document.add(dateTable);
		    
		    PdfPTable headerTable = new PdfPTable(2); // 2 columns.
			headerTable.setWidthPercentage(100); //Width 100%
			headerTable.setSpacingBefore(10f); //Space before table
			headerTable.setSpacingAfter(10f); //Space after table
			
			//Set Column widths
			headerTable.setWidths(new float[]{1, 3});
			
			
			Date fromDate = getFromDate();
			Date toDate = getToDate();
			
			if (fromDate != null) {
				cell = new PdfPCell(new Paragraph("From Date", boldFont));
				headerTable.addCell(cell);
			    cell = new PdfPCell(new Paragraph(GeneralUtil.formatDate(fromDate), normalFont));
			    headerTable.addCell(cell);
			}
			
			if (toDate != null) {
				cell = new PdfPCell(new Paragraph("To Date", boldFont));
				headerTable.addCell(cell);
			    cell = new PdfPCell(new Paragraph(GeneralUtil.formatDate(toDate), normalFont));
			    headerTable.addCell(cell);
			}
			
			document.add(headerTable);
	 
	        PdfPTable table = new PdfPTable(9); // 9 columns.
	        table.setWidthPercentage(100); //Width 100%
	        table.setSpacingBefore(10f); //Space before tableHeader
	        table.setSpacingAfter(10f); //Space after tableHeader
	 
	        //Set Column widths
			table.setWidths(new float[] { 0.5f, 1f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f });

			cell = new PdfPCell(new Paragraph("S.No", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Circle Name", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Total Complaint Registered", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Closed in Less than 1 Hr", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Closed in Less than 3 Hrs \n(1 - 3)", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Closed in Less than 6 Hrs \n(3 - 6)", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Closed in Less than 12 Hrs \n(6 - 12)", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Closed in Less than 24 Hrs \n(12 - 24)", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Pending Complaint", boldFont));
		    table.addCell(cell);
			
		    Integer totalCompRegistered = 0;
		    Integer compPending = 0;
		    
	        for(ReportValueBean reportValueBean : circleReportList) {
	        	totalCompRegistered = totalCompRegistered + reportValueBean.getTotalCompRegistered();
	        	compPending = compPending + reportValueBean.getCompPending();
			    cell = new PdfPCell(new Paragraph(String.valueOf(reportValueBean.getSerialNumber()), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(reportValueBean.getCircleName(), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(String.valueOf(reportValueBean.getTotalCompRegistered()), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(String.valueOf(reportValueBean.getTotalCompRegisteredOneHour()), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(String.valueOf(reportValueBean.getTotalCompRegisteredThreeHour()), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(String.valueOf(reportValueBean.getTotalCompRegisteredSixHour()), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(String.valueOf(reportValueBean.getTotalCompRegisteredTwelveHour()), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(String.valueOf(reportValueBean.getTotalCompRegisteredOneDay()), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(String.valueOf(reportValueBean.getCompPending()), normalFont));
			    table.addCell(cell);
	        }
	        
	        cell = new PdfPCell(new Paragraph(String.valueOf(""), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(""), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(totalCompRegistered), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(""), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(""), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(""), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(""), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(""), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(compPending), normalFont));
		    table.addCell(cell);
	        
	        
	        document.add(table);
	        
			res.setContentType("application/vnd.ms-excel");
			res.setHeader("Content-disposition", "attachment; filename=CallCircleReport-"+GeneralUtil.formatDate(new Date())+".pdf");

			
			fos.flush();
	 
			context.responseComplete();
			context.renderResponse();
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to export."));
		} finally {
			if(document != null) {
				document.close();
			}
			if(writer != null) {
				writer.close();
			}
			if(fos != null) {
				fos.close();
			}
		}
		
	
	}
	
	public void exportCircleDivisionExcelReport() throws IOException {

		ComplaintsDao complaintsDao = new ComplaintsDao();
		
		FacesContext context = FacesContext.getCurrentInstance();
		
		HSSFWorkbook workbook = new HSSFWorkbook();
		OutputStream fos = null;
		try {
			
			Map<String, List<ReportValueBean>> circleDivisionReportMap =  complaintsDao.getCircleDivisionReport(circleDivisionFilter, fromDateTwo, toDateTwo, officer);
			
			HSSFSheet sheet = workbook.createSheet();
			
			sheet.setColumnWidth(0, 2500);
			sheet.setColumnWidth(1, 10000);
			sheet.setColumnWidth(2, 4500);
			sheet.setColumnWidth(3, 4500);
			sheet.setColumnWidth(4, 4500);
			
			
			HSSFRow row;
			HSSFCell cell;
			HSSFCellStyle cellStyle = workbook.createCellStyle();
			HSSFCellStyle boldCellStyle = workbook.createCellStyle();
			HSSFCellStyle blueCellStyle = workbook.createCellStyle();
			HSSFCellStyle greenCellStyle = workbook.createCellStyle();

			HSSFFont defaultFont = workbook.createFont();
			defaultFont.setFontHeightInPoints((short) 10);
			defaultFont.setFontName("Arial");
			defaultFont.setColor(IndexedColors.BLACK.getIndex());
			defaultFont.setBold(false);
			defaultFont.setItalic(false);
			
			HSSFFont boldFont = workbook.createFont();
			boldFont.setFontHeightInPoints((short) 10);
			boldFont.setFontName("Arial");
			boldFont.setColor(IndexedColors.BLACK.getIndex());
			boldFont.setBold(true);
			boldFont.setItalic(false);

			HSSFFont font = workbook.createFont();
			font.setFontHeightInPoints((short) 10);
			font.setFontName("Arial");
			font.setColor(IndexedColors.WHITE.getIndex());
			font.setBold(true);
			font.setItalic(false);

			
			int rowCounter = 0;
			
			row = sheet.createRow(rowCounter++);
			row = sheet.createRow(rowCounter++);
			
		
			
			boldCellStyle.setFont(boldFont);
			cell = row.createCell(1);
			cell.setCellValue("Call Center - Division wise Complaints Report Generated On");
			cell.setCellStyle(boldCellStyle);
			sheet.addMergedRegion(new CellRangeAddress(1,1,1,2));
			cellStyle.setFont(defaultFont);
		    cell = row.createCell(3);
			cell.setCellValue(GeneralUtil.formatDate(new Date()));
			cell.setCellStyle(cellStyle);
			
			row = sheet.createRow(rowCounter++);
			row = sheet.createRow(rowCounter++);
			
			boldCellStyle.setFont(boldFont);
			
			if (fromDateTwo != null) {
				cell = row.createCell(1);
				cell.setCellValue("From Date");
				cell.setCellStyle(boldCellStyle);
				cellStyle.setFont(defaultFont);
			    cell = row.createCell(3);
				cell.setCellValue(GeneralUtil.formatDate(fromDateTwo));
				cell.setCellStyle(cellStyle);
			}
			
			row = sheet.createRow(rowCounter++);
			
			boldCellStyle.setFont(boldFont);
			
			if (toDateTwo != null) {
				cell = row.createCell(1);
				cell.setCellValue("To Date");
				cell.setCellStyle(boldCellStyle);
				cellStyle.setFont(defaultFont);
				cell = row.createCell(3);
				cell.setCellValue(GeneralUtil.formatDate(toDateTwo));
				cell.setCellStyle(cellStyle);
				
			}
			
			row = sheet.createRow(rowCounter++);
			row = sheet.createRow(rowCounter++);
			
			blueCellStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
			blueCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			blueCellStyle.setFont(font);
			blueCellStyle.setWrapText(true);
			
			cell = row.createCell(0);
			cell.setCellValue("S.No");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(1);
			cell.setCellValue("Name");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(2);
			cell.setCellValue("Total Complaints Registered");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(3);
			cell.setCellValue("Total Complaints Closed");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(4);
			cell.setCellValue("Total Complaints Pending");
			cell.setCellStyle(blueCellStyle);
			
			 Integer serialNumber = 1;
			 
			for(Map.Entry<String, List<ReportValueBean>> mapEntry : circleDivisionReportMap.entrySet()) {
				
				row = sheet.createRow(rowCounter++);
				
				greenCellStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
				greenCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				greenCellStyle.setFont(font);
				
				cell = row.createCell(1);
				cell.setCellValue(mapEntry.getKey());
				cell.setCellStyle(greenCellStyle);
				
	        	List<ReportValueBean> circleReportList = mapEntry.getValue();
	        	
	        	for (ReportValueBean reportValueBean : circleReportList) {
	        		
	        		row = sheet.createRow(rowCounter++);
					
					cellStyle.setFont(defaultFont);
					
					cell = row.createCell(0);
					cell.setCellValue(String.valueOf(String.valueOf(serialNumber++)));
					cell.setCellStyle(cellStyle);
					cell = row.createCell(1);
					cell.setCellValue(reportValueBean.getDivisionName());
					cell.setCellStyle(cellStyle);
					cell = row.createCell(2);
					cell.setCellValue(reportValueBean.getTotalCompRegistered());
					cell.setCellStyle(cellStyle);
					cell = row.createCell(3);
					cell.setCellValue(reportValueBean.getCompResolved());
					cell.setCellStyle(cellStyle);
					cell = row.createCell(4);
					cell.setCellValue(reportValueBean.getCompPending());
					cell.setCellStyle(cellStyle);
	        		
				}
	        }
			
			

			HttpServletResponse res = getResponse();
			res.setContentType("application/vnd.ms-excel");
			res.setHeader("Content-disposition", "attachment; filename=CallCircleDivisionReport-"+GeneralUtil.formatDate(new Date())+".xls");

			fos = res.getOutputStream();

			workbook.write(fos);
			
			fos.flush();
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			if (fos != null) {
				fos.close();
			}
			if (workbook != null) {
				workbook.close();
			}
		}

		context.responseComplete();
		context.renderResponse();
	
	}
	
	
	public void exportCircleDivisionPdfReport() throws IOException {

		FacesContext context = FacesContext.getCurrentInstance();
		Document document = new Document();
		PdfWriter writer = null;
		OutputStream fos = null;
		PdfPCell cell = null;
		try {
			
			ComplaintsDao complaintsDao = new ComplaintsDao();
			
			Map<String, List<ReportValueBean>> circleDivisionReportMap =  complaintsDao.getCircleDivisionReport(circleDivisionFilter, fromDateTwo, toDateTwo, officer);
			
		    HttpServletResponse res = getResponse();
			 
			fos = res.getOutputStream();

			writer = PdfWriter.getInstance(document, fos);
			
	        document.open();
	        
	        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
			Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.NORMAL);
			
			PdfPTable dateTable = new PdfPTable(2); // 2 columns.
			dateTable.setWidthPercentage(100); //Width 100%
			dateTable.setSpacingBefore(10f); //Space before table
			dateTable.setSpacingAfter(10f); //Space after table
			
			//Set Column widths
			dateTable.setWidths(new float[]{3, 1});
			
			cell = new PdfPCell(new Paragraph("Call Center - Division wise Complaints Report Generated On", boldFont));
			dateTable.addCell(cell);
		    cell = new PdfPCell(new Paragraph(GeneralUtil.formatDate(new Date()), normalFont));
		    dateTable.addCell(cell);
		    
		    document.add(dateTable);
		    
		    PdfPTable headerTable = new PdfPTable(2); // 2 columns.
			headerTable.setWidthPercentage(100); //Width 100%
			headerTable.setSpacingBefore(10f); //Space before table
			headerTable.setSpacingAfter(10f); //Space after table
			
			//Set Column widths
			headerTable.setWidths(new float[]{1, 3});
			
			
			Date fromDate = getFromDateTwo();
			Date toDate = getToDateTwo();
			
			if (fromDate != null) {
				cell = new PdfPCell(new Paragraph("From Date", boldFont));
				headerTable.addCell(cell);
			    cell = new PdfPCell(new Paragraph(GeneralUtil.formatDate(fromDate), normalFont));
			    headerTable.addCell(cell);
			}
			
			if (toDate != null) {
				cell = new PdfPCell(new Paragraph("To Date", boldFont));
				headerTable.addCell(cell);
			    cell = new PdfPCell(new Paragraph(GeneralUtil.formatDate(toDate), normalFont));
			    headerTable.addCell(cell);
			}
			
			document.add(headerTable);
	 
	        PdfPTable table = new PdfPTable(5); // 5 columns.
	        table.setWidthPercentage(100); //Width 100%
	        table.setSpacingBefore(10f); //Space before tableHeader
	        table.setSpacingAfter(10f); //Space after tableHeader
	 
	        //Set Column widths
			table.setWidths(new float[] { 0.5f, 2f, 0.5f, 0.5f, 0.5f });

			cell = new PdfPCell(new Paragraph("S.No", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Name", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Total Complaints Registered", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Total Complaints Closed", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Total Complaints Pending", boldFont));
		    table.addCell(cell);
		    
		    Integer serialNumber = 1;
			
	        for(Map.Entry<String, List<ReportValueBean>> mapEntry : circleDivisionReportMap.entrySet()) {
	        	cell = new PdfPCell(new Paragraph(String.valueOf(""), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(mapEntry.getKey(), boldFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(String.valueOf(""), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(String.valueOf(""), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(String.valueOf(""), normalFont));
			    table.addCell(cell);
			    
	        	List<ReportValueBean> circleReportList = mapEntry.getValue();
	        	
	        	for (ReportValueBean reportValueBean : circleReportList) {
	        		cell = new PdfPCell(new Paragraph(String.valueOf(serialNumber++), normalFont));
				    table.addCell(cell);
				    cell = new PdfPCell(new Paragraph(reportValueBean.getDivisionName(), normalFont));
				    table.addCell(cell);
				    cell = new PdfPCell(new Paragraph(String.valueOf(reportValueBean.getTotalCompRegistered()), normalFont));
				    table.addCell(cell);
				    cell = new PdfPCell(new Paragraph(String.valueOf(reportValueBean.getCompResolved()), normalFont));
				    table.addCell(cell);
				    cell = new PdfPCell(new Paragraph(String.valueOf(reportValueBean.getCompPending()), normalFont));
				    table.addCell(cell);
				}
	        }
	        
	        
	        document.add(table);
	        
			res.setContentType("application/vnd.ms-excel");
			res.setHeader("Content-disposition", "attachment; filename=CallCircleDivisionReport-"+GeneralUtil.formatDate(new Date())+".pdf");

			
			fos.flush();
	 
			context.responseComplete();
			context.renderResponse();
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to export."));
		} finally {
			if(document != null) {
				document.close();
			}
			if(writer != null) {
				writer.close();
			}
			if(fos != null) {
				fos.close();
			}
		}
		
	
	}


	public static HttpServletResponse getResponse() {
		return (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
	}

	public LoginParams getOfficer() {
		return officer;
	}

	public void setOfficer(LoginParams officer) {
		this.officer = officer;
	}

	public String[] getCircleFilter() {
		return circleFilter;
	}

	public void setCircleFilter(String[] circleFilter) {
		this.circleFilter = circleFilter;
	}

	public List<CircleValueBean> getListCircle() {
		return listCircle;
	}

	public void setListCircle(List<CircleValueBean> listCircle) {
		this.listCircle = listCircle;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public Date getCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}

	public String[] getCircleDivisionFilter() {
		return circleDivisionFilter;
	}

	public void setCircleDivisionFilter(String[] circleDivisionFilter) {
		this.circleDivisionFilter = circleDivisionFilter;
	}

	public Date getFromDateTwo() {
		return fromDateTwo;
	}

	public void setFromDateTwo(Date fromDateTwo) {
		this.fromDateTwo = fromDateTwo;
	}

	public Date getToDateTwo() {
		return toDateTwo;
	}

	public void setToDateTwo(Date toDateTwo) {
		this.toDateTwo = toDateTwo;
	}
	
	public ViewComplaintReportValueBean getSelectedComplaintId() {
		return selectedComplaintId;
	}

	public void setSelectedComplaintId(ViewComplaintReportValueBean selectedComplaintId) {
		this.selectedComplaintId = selectedComplaintId;
	}
	
		
	
// FOR SOCIAL MEDIA REPORT GENERATE
	
	public void generateCircleReportSm(String reportType) throws IOException {
		
		boolean validSearch = true;
		
		if (fromDate != null && toDate != null) {
			if(fromDate.after(toDate)) {
				validSearch = false;
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "To date can't be lesser than from date!"));
			} 
//			else {
//				long dateDifference = GeneralUtil.findDifference(toDate, fromDate, "day");
//				if(dateDifference > 31) {
//					validSearch = false;
//					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Date difference between from date and to date can't be more than a month!"));
//				}
//			}
		} else if(fromDate == null) {
			validSearch = false;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please select from date."));
		} else if(toDate == null) {
			validSearch = false;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please select to date."));
		}
//		if(circleFilter.length == 0) {
//			validSearch = false;
//			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please select circle."));
//		}
		
		if(validSearch) {
			if(CCMSConstants.PDF_EXPORT.equalsIgnoreCase(reportType)) {
				exportCirclePdfReportSm();
			} else {
				exportCircleExcelReportSm();
			}
		}
		
	}
	
	public void exportCirclePdfReportSm() throws IOException {

		FacesContext context = FacesContext.getCurrentInstance();
		Document document = new Document();
		PdfWriter writer = null;
		OutputStream fos = null;
		PdfPCell cell = null;
		try {
			
			ComplaintsDao complaintsDao = new ComplaintsDao();
			
			List<ReportValueBean> circleReportList = complaintsDao.getCircleReportSm(circleFilter, fromDate, toDate, officer);
			
		    HttpServletResponse res = getResponse();
			 
			fos = res.getOutputStream();

			writer = PdfWriter.getInstance(document, fos);
			
	        document.open();
	        
	        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
			Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.NORMAL);
			
			PdfPTable dateTable = new PdfPTable(2); // 2 columns.
			dateTable.setWidthPercentage(100); //Width 100%
			dateTable.setSpacingBefore(10f); //Space before table
			dateTable.setSpacingAfter(10f); //Space after table
			
			//Set Column widths
			dateTable.setWidths(new float[]{3, 1});
			
			cell = new PdfPCell(new Paragraph("Social Media - Circle Calls Report Generated On", boldFont));
			dateTable.addCell(cell);
		    cell = new PdfPCell(new Paragraph(GeneralUtil.formatDate(new Date()), normalFont));
		    dateTable.addCell(cell);
		    
		    document.add(dateTable);
		    
		    PdfPTable headerTable = new PdfPTable(2); // 2 columns.
			headerTable.setWidthPercentage(100); //Width 100%
			headerTable.setSpacingBefore(10f); //Space before table
			headerTable.setSpacingAfter(10f); //Space after table
			
			//Set Column widths
			headerTable.setWidths(new float[]{1, 3});
			
			
			Date fromDate = getFromDate();
			Date toDate = getToDate();
			
			if (fromDate != null) {
				cell = new PdfPCell(new Paragraph("From Date", boldFont));
				headerTable.addCell(cell);
			    cell = new PdfPCell(new Paragraph(GeneralUtil.formatDate(fromDate), normalFont));
			    headerTable.addCell(cell);
			}
			
			if (toDate != null) {
				cell = new PdfPCell(new Paragraph("To Date", boldFont));
				headerTable.addCell(cell);
			    cell = new PdfPCell(new Paragraph(GeneralUtil.formatDate(toDate), normalFont));
			    headerTable.addCell(cell);
			}
			
			document.add(headerTable);
	 
	        PdfPTable table = new PdfPTable(9); // 9 columns.
	        table.setWidthPercentage(100); //Width 100%
	        table.setSpacingBefore(10f); //Space before tableHeader
	        table.setSpacingAfter(10f); //Space after tableHeader
	 
	        //Set Column widths
			table.setWidths(new float[] { 0.5f, 1f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f });

			cell = new PdfPCell(new Paragraph("S.No", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Circle Name", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Total Complaint Registered", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Closed in Less than 1 Hr", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Closed in Less than 3 Hrs \n(1 - 3)", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Closed in Less than 6 Hrs \n(3 - 6)", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Closed in Less than 12 Hrs \n(6 - 12)", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Closed in Less than 24 Hrs \n(12 - 24)", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Pending Complaint", boldFont));
		    table.addCell(cell);
			
		    Integer totalCompRegistered = 0;
		    Integer compPending = 0;
		    
	        for(ReportValueBean reportValueBean : circleReportList) {
	        	totalCompRegistered = totalCompRegistered + reportValueBean.getTotalCompRegistered();
	        	compPending = compPending + reportValueBean.getCompPending();
			    cell = new PdfPCell(new Paragraph(String.valueOf(reportValueBean.getSerialNumber()), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(reportValueBean.getCircleName(), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(String.valueOf(reportValueBean.getTotalCompRegistered()), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(String.valueOf(reportValueBean.getTotalCompRegisteredOneHour()), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(String.valueOf(reportValueBean.getTotalCompRegisteredThreeHour()), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(String.valueOf(reportValueBean.getTotalCompRegisteredSixHour()), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(String.valueOf(reportValueBean.getTotalCompRegisteredTwelveHour()), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(String.valueOf(reportValueBean.getTotalCompRegisteredOneDay()), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(String.valueOf(reportValueBean.getCompPending()), normalFont));
			    table.addCell(cell);
	        }
	        
	        cell = new PdfPCell(new Paragraph(String.valueOf(""), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(""), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(totalCompRegistered), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(""), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(""), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(""), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(""), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(""), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(compPending), normalFont));
		    table.addCell(cell);
	        
	        
	        document.add(table);
	        
			res.setContentType("application/vnd.ms-excel");
			res.setHeader("Content-disposition", "attachment; filename=CallCircleReport-"+GeneralUtil.formatDate(new Date())+".pdf");

			
			fos.flush();
	 
			context.responseComplete();
			context.renderResponse();
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to export."));
		} finally {
			if(document != null) {
				document.close();
			}
			if(writer != null) {
				writer.close();
			}
			if(fos != null) {
				fos.close();
			}
		}
		
	
	}
	
	public void exportCircleExcelReportSm() throws IOException {


		ComplaintsDao complaintsDao = new ComplaintsDao();
		
		FacesContext context = FacesContext.getCurrentInstance();
		
		HSSFWorkbook workbook = new HSSFWorkbook();
		OutputStream fos = null;
		try {
			
			List<ReportValueBean> circleReportList = complaintsDao.getCircleReportSm(circleFilter, fromDate, toDate, officer);
			
			HSSFSheet sheet = workbook.createSheet();
			
			sheet.setColumnWidth(0, 2500);
			sheet.setColumnWidth(1, 7000);
			sheet.setColumnWidth(2, 4500);
			sheet.setColumnWidth(3, 4500);
			sheet.setColumnWidth(4, 4500);
			sheet.setColumnWidth(5, 4500);
			sheet.setColumnWidth(6, 4500);
			sheet.setColumnWidth(7, 5000);
			sheet.setColumnWidth(8, 4000);
			
			HSSFRow row;
			HSSFCell cell;
			HSSFCellStyle cellStyle= workbook.createCellStyle();
			HSSFCellStyle boldCellStyle= workbook.createCellStyle();
			HSSFCellStyle blueCellStyle= workbook.createCellStyle();

			HSSFFont defaultFont = workbook.createFont();
			defaultFont.setFontHeightInPoints((short) 10);
			defaultFont.setFontName("Arial");
			defaultFont.setColor(IndexedColors.BLACK.getIndex());
			defaultFont.setBold(false);
			defaultFont.setItalic(false);
			
			HSSFFont boldFont = workbook.createFont();
			boldFont.setFontHeightInPoints((short) 10);
			boldFont.setFontName("Arial");
			boldFont.setColor(IndexedColors.BLACK.getIndex());
			boldFont.setBold(true);
			boldFont.setItalic(false);

			HSSFFont font = workbook.createFont();
			font.setFontHeightInPoints((short) 10);
			font.setFontName("Arial");
			font.setColor(IndexedColors.WHITE.getIndex());
			font.setBold(true);
			font.setItalic(false);

			
			int rowCounter = 0;
			
			row = sheet.createRow(rowCounter++);
			row = sheet.createRow(rowCounter++);
			
		
			boldCellStyle.setFont(boldFont);
			cell = row.createCell(1);
			cell.setCellValue("Social Media - Circle Calls Report Generated On");
			cell.setCellStyle(boldCellStyle);
			sheet.addMergedRegion(new CellRangeAddress(1,1,1,2));
			cellStyle = workbook.createCellStyle();
			cellStyle.setFont(defaultFont);
		    cell = row.createCell(3);
			cell.setCellValue(GeneralUtil.formatDate(new Date()));
			cell.setCellStyle(cellStyle);
			
			row = sheet.createRow(rowCounter++);
			row = sheet.createRow(rowCounter++);
			
			boldCellStyle.setFont(boldFont);
			
			if (fromDate != null) {
				cell = row.createCell(1);
				cell.setCellValue("From Date");
				cell.setCellStyle(boldCellStyle);
				cellStyle.setFont(defaultFont);
			    cell = row.createCell(3);
				cell.setCellValue(GeneralUtil.formatDate(fromDate));
				cell.setCellStyle(cellStyle);
			}
			
			row = sheet.createRow(rowCounter++);
			
			boldCellStyle.setFont(boldFont);
			
			if (toDate != null) {
				cell = row.createCell(1);
				cell.setCellValue("To Date");
				cell.setCellStyle(boldCellStyle);
				cellStyle.setFont(defaultFont);
				cell = row.createCell(3);
				cell.setCellValue(GeneralUtil.formatDate(toDate));
				cell.setCellStyle(cellStyle);
				
			}
			
			row = sheet.createRow(rowCounter++);
			row = sheet.createRow(rowCounter++);
			
			blueCellStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
			blueCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			blueCellStyle.setFont(font);
			blueCellStyle.setWrapText(true);
			
			cell = row.createCell(0);
			cell.setCellValue("S.No");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(1);
			cell.setCellValue("Circle Name");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(2);
			cell.setCellValue("Total Complaint Registered");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(3);
			cell.setCellValue("Closed in Less than 1 Hr");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(4);
			cell.setCellValue("Closed in Less than 3 Hrs (1 - 3)");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(5);
			cell.setCellValue("Closed in Less than 6 Hrs (3 - 6)");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(6);
			cell.setCellValue("Closed in Less than 12 Hrs (6 - 12)");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(7);
			cell.setCellValue("Closed in Less than 24 Hrs (12 - 24)");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(8);
			cell.setCellValue("Pending Complaint");
			cell.setCellStyle(blueCellStyle);
			
		    Integer totalCompRegistered = 0;
		    Integer compPending = 0;
		    
	        for(ReportValueBean reportValueBean : circleReportList) {
	        	totalCompRegistered = totalCompRegistered + reportValueBean.getTotalCompRegistered();
	        	compPending = compPending + reportValueBean.getCompPending();
	        	
	        	row = sheet.createRow(rowCounter++);
				
				cellStyle.setFont(defaultFont);
				
				cell = row.createCell(0);
				cell.setCellValue(reportValueBean.getSerialNumber());
				cell.setCellStyle(cellStyle);
				cell = row.createCell(1);
				cell.setCellValue(reportValueBean.getCircleName());
				cell.setCellStyle(cellStyle);
				cell = row.createCell(2);
				cell.setCellValue(reportValueBean.getTotalCompRegistered());
				cell.setCellStyle(cellStyle);
				cell = row.createCell(3);
				cell.setCellValue(reportValueBean.getTotalCompRegisteredOneHour());
				cell.setCellStyle(cellStyle);
				cell = row.createCell(4);
				cell.setCellValue(reportValueBean.getTotalCompRegisteredThreeHour());
				cell.setCellStyle(cellStyle);
				cell = row.createCell(5);
				cell.setCellValue(reportValueBean.getTotalCompRegisteredSixHour());
				cell.setCellStyle(cellStyle);
				cell = row.createCell(6);
				cell.setCellValue(reportValueBean.getTotalCompRegisteredTwelveHour());
				cell.setCellStyle(cellStyle);
				cell = row.createCell(7);
				cell.setCellValue(reportValueBean.getTotalCompRegisteredOneDay());
				cell.setCellStyle(cellStyle);
				cell = row.createCell(8);
				cell.setCellValue(reportValueBean.getCompPending());
				cell.setCellStyle(cellStyle);
				
	        }
	        
	        row = sheet.createRow(rowCounter++);
			
			cellStyle.setFont(defaultFont);
			
	        cell = row.createCell(2);
	        cell.setCellValue(totalCompRegistered);
			cell.setCellStyle(cellStyle);
			cell = row.createCell(8);
			cell.setCellValue(compPending);
			cell.setCellStyle(cellStyle);
			

			HttpServletResponse res = getResponse();
			res.setContentType("application/vnd.ms-excel");
			res.setHeader("Content-disposition", "attachment; filename=CallCircleReport-"+GeneralUtil.formatDate(new Date())+".xls");

			fos = res.getOutputStream();

			workbook.write(fos);
			
			fos.flush();
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			if (fos != null) {
				fos.close();
			}
			if (workbook != null) {
				workbook.close();
			}
		}

		context.responseComplete();
		context.renderResponse();
	
	
	}
	
public void generateCircleDivisionReportSm(String reportType) throws IOException {
		
		boolean validSearch = true;
		
		if (fromDateTwo != null && toDateTwo != null) {
			if(fromDateTwo.after(toDateTwo)) {
				validSearch = false;
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "To date can't be lesser than from date!"));
			} 
//			else {
//				long dateDifference = GeneralUtil.findDifference(toDateTwo, fromDateTwo, "day");
//				if(dateDifference > 31) {
//					validSearch = false;
//					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Date difference between from date and to date can't be more than a month!"));
//				}
//			}
		} else if(fromDateTwo == null) {
			validSearch = false;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please select from date."));
		} else if(toDateTwo == null) {
			validSearch = false;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please select to date."));
		}
//		if(circleDivisionFilter.length == 0) {
//			validSearch = false;
//			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please select circle."));
//		}
		
		if(validSearch) {
			if(CCMSConstants.PDF_EXPORT.equalsIgnoreCase(reportType)) {
				exportCircleDivisionPdfReportSm();
			} else {
				exportCircleDivisionExcelReportSm();
			}
		}
		
	}

	public void exportCircleDivisionPdfReportSm() throws IOException {

	FacesContext context = FacesContext.getCurrentInstance();
	Document document = new Document();
	PdfWriter writer = null;
	OutputStream fos = null;
	PdfPCell cell = null;
	try {
		
		ComplaintsDao complaintsDao = new ComplaintsDao();
		
		Map<String, List<ReportValueBean>> circleDivisionReportMap =  complaintsDao.getCircleDivisionReportSm(circleDivisionFilter, fromDateTwo, toDateTwo, officer);
		
	    HttpServletResponse res = getResponse();
		 
		fos = res.getOutputStream();

		writer = PdfWriter.getInstance(document, fos);
		
        document.open();
        
        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
		Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.NORMAL);
		
		PdfPTable dateTable = new PdfPTable(2); // 2 columns.
		dateTable.setWidthPercentage(100); //Width 100%
		dateTable.setSpacingBefore(10f); //Space before table
		dateTable.setSpacingAfter(10f); //Space after table
		
		//Set Column widths
		dateTable.setWidths(new float[]{3, 1});
		
		cell = new PdfPCell(new Paragraph("Social Media - Division wise Complaints Report Generated On", boldFont));
		dateTable.addCell(cell);
	    cell = new PdfPCell(new Paragraph(GeneralUtil.formatDate(new Date()), normalFont));
	    dateTable.addCell(cell);
	    
	    document.add(dateTable);
	    
	    PdfPTable headerTable = new PdfPTable(2); // 2 columns.
		headerTable.setWidthPercentage(100); //Width 100%
		headerTable.setSpacingBefore(10f); //Space before table
		headerTable.setSpacingAfter(10f); //Space after table
		
		//Set Column widths
		headerTable.setWidths(new float[]{1, 3});
		
		
		Date fromDate = getFromDateTwo();
		Date toDate = getToDateTwo();
		
		if (fromDate != null) {
			cell = new PdfPCell(new Paragraph("From Date", boldFont));
			headerTable.addCell(cell);
		    cell = new PdfPCell(new Paragraph(GeneralUtil.formatDate(fromDate), normalFont));
		    headerTable.addCell(cell);
		}
		
		if (toDate != null) {
			cell = new PdfPCell(new Paragraph("To Date", boldFont));
			headerTable.addCell(cell);
		    cell = new PdfPCell(new Paragraph(GeneralUtil.formatDate(toDate), normalFont));
		    headerTable.addCell(cell);
		}
		
		document.add(headerTable);
 
        PdfPTable table = new PdfPTable(5); // 5 columns.
        table.setWidthPercentage(100); //Width 100%
        table.setSpacingBefore(10f); //Space before tableHeader
        table.setSpacingAfter(10f); //Space after tableHeader
 
        //Set Column widths
		table.setWidths(new float[] { 0.5f, 2f, 0.5f, 0.5f, 0.5f });

		cell = new PdfPCell(new Paragraph("S.No", boldFont));
	    table.addCell(cell);
	    cell = new PdfPCell(new Paragraph("Name", boldFont));
	    table.addCell(cell);
	    cell = new PdfPCell(new Paragraph("Total Complaints Registered", boldFont));
	    table.addCell(cell);
	    cell = new PdfPCell(new Paragraph("Total Complaints Closed", boldFont));
	    table.addCell(cell);
	    cell = new PdfPCell(new Paragraph("Total Complaints Pending", boldFont));
	    table.addCell(cell);
	    
	    Integer serialNumber = 1;
		
        for(Map.Entry<String, List<ReportValueBean>> mapEntry : circleDivisionReportMap.entrySet()) {
        	cell = new PdfPCell(new Paragraph(String.valueOf(""), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(mapEntry.getKey(), boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(""), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(""), normalFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph(String.valueOf(""), normalFont));
		    table.addCell(cell);
		    
        	List<ReportValueBean> circleReportList = mapEntry.getValue();
        	
        	for (ReportValueBean reportValueBean : circleReportList) {
        		cell = new PdfPCell(new Paragraph(String.valueOf(serialNumber++), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(reportValueBean.getDivisionName(), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(String.valueOf(reportValueBean.getTotalCompRegistered()), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(String.valueOf(reportValueBean.getCompResolved()), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(String.valueOf(reportValueBean.getCompPending()), normalFont));
			    table.addCell(cell);
			}
        }
        
        
        document.add(table);
        
		res.setContentType("application/vnd.ms-excel");
		res.setHeader("Content-disposition", "attachment; filename=CallCircleDivisionReport-"+GeneralUtil.formatDate(new Date())+".pdf");

		
		fos.flush();
 
		context.responseComplete();
		context.renderResponse();
		
	} catch (Exception e) {
		logger.error(ExceptionUtils.getStackTrace(e));
		FacesContext.getCurrentInstance().addMessage(null, 
				new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to export."));
	} finally {
		if(document != null) {
			document.close();
		}
		if(writer != null) {
			writer.close();
		}
		if(fos != null) {
			fos.close();
		}
	}
	

}
	
	public void exportCircleDivisionExcelReportSm() throws IOException {

		ComplaintsDao complaintsDao = new ComplaintsDao();
		
		FacesContext context = FacesContext.getCurrentInstance();
		
		HSSFWorkbook workbook = new HSSFWorkbook();
		OutputStream fos = null;
		try {
			
			Map<String, List<ReportValueBean>> circleDivisionReportMap =  complaintsDao.getCircleDivisionReportSm(circleDivisionFilter, fromDateTwo, toDateTwo, officer);
			
			HSSFSheet sheet = workbook.createSheet();
			
			sheet.setColumnWidth(0, 2500);
			sheet.setColumnWidth(1, 10000);
			sheet.setColumnWidth(2, 4500);
			sheet.setColumnWidth(3, 4500);
			sheet.setColumnWidth(4, 4500);
			
			
			HSSFRow row;
			HSSFCell cell;
			HSSFCellStyle cellStyle = workbook.createCellStyle();
			HSSFCellStyle boldCellStyle = workbook.createCellStyle();
			HSSFCellStyle blueCellStyle = workbook.createCellStyle();
			HSSFCellStyle greenCellStyle = workbook.createCellStyle();

			HSSFFont defaultFont = workbook.createFont();
			defaultFont.setFontHeightInPoints((short) 10);
			defaultFont.setFontName("Arial");
			defaultFont.setColor(IndexedColors.BLACK.getIndex());
			defaultFont.setBold(false);
			defaultFont.setItalic(false);
			
			HSSFFont boldFont = workbook.createFont();
			boldFont.setFontHeightInPoints((short) 10);
			boldFont.setFontName("Arial");
			boldFont.setColor(IndexedColors.BLACK.getIndex());
			boldFont.setBold(true);
			boldFont.setItalic(false);

			HSSFFont font = workbook.createFont();
			font.setFontHeightInPoints((short) 10);
			font.setFontName("Arial");
			font.setColor(IndexedColors.WHITE.getIndex());
			font.setBold(true);
			font.setItalic(false);

			
			int rowCounter = 0;
			
			row = sheet.createRow(rowCounter++);
			row = sheet.createRow(rowCounter++);
			
		
			
			boldCellStyle.setFont(boldFont);
			cell = row.createCell(1);
			cell.setCellValue("Social Media - Division wise Complaints Report Generated On");
			cell.setCellStyle(boldCellStyle);
			sheet.addMergedRegion(new CellRangeAddress(1,1,1,2));
			cellStyle.setFont(defaultFont);
		    cell = row.createCell(3);
			cell.setCellValue(GeneralUtil.formatDate(new Date()));
			cell.setCellStyle(cellStyle);
			
			row = sheet.createRow(rowCounter++);
			row = sheet.createRow(rowCounter++);
			
			boldCellStyle.setFont(boldFont);
			
			if (fromDateTwo != null) {
				cell = row.createCell(1);
				cell.setCellValue("From Date");
				cell.setCellStyle(boldCellStyle);
				cellStyle.setFont(defaultFont);
			    cell = row.createCell(3);
				cell.setCellValue(GeneralUtil.formatDate(fromDateTwo));
				cell.setCellStyle(cellStyle);
			}
			
			row = sheet.createRow(rowCounter++);
			
			boldCellStyle.setFont(boldFont);
			
			if (toDateTwo != null) {
				cell = row.createCell(1);
				cell.setCellValue("To Date");
				cell.setCellStyle(boldCellStyle);
				cellStyle.setFont(defaultFont);
				cell = row.createCell(3);
				cell.setCellValue(GeneralUtil.formatDate(toDateTwo));
				cell.setCellStyle(cellStyle);
				
			}
			
			row = sheet.createRow(rowCounter++);
			row = sheet.createRow(rowCounter++);
			
			blueCellStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
			blueCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			blueCellStyle.setFont(font);
			blueCellStyle.setWrapText(true);
			
			cell = row.createCell(0);
			cell.setCellValue("S.No");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(1);
			cell.setCellValue("Name");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(2);
			cell.setCellValue("Total Complaints Registered");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(3);
			cell.setCellValue("Total Complaints Closed");
			cell.setCellStyle(blueCellStyle);
			cell = row.createCell(4);
			cell.setCellValue("Total Complaints Pending");
			cell.setCellStyle(blueCellStyle);
			
			 Integer serialNumber = 1;
			 
			for(Map.Entry<String, List<ReportValueBean>> mapEntry : circleDivisionReportMap.entrySet()) {
				
				row = sheet.createRow(rowCounter++);
				
				greenCellStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
				greenCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				greenCellStyle.setFont(font);
				
				cell = row.createCell(1);
				cell.setCellValue(mapEntry.getKey());
				cell.setCellStyle(greenCellStyle);
				
	        	List<ReportValueBean> circleReportList = mapEntry.getValue();
	        	
	        	for (ReportValueBean reportValueBean : circleReportList) {
	        		
	        		row = sheet.createRow(rowCounter++);
					
					cellStyle.setFont(defaultFont);
					
					cell = row.createCell(0);
					cell.setCellValue(String.valueOf(String.valueOf(serialNumber++)));
					cell.setCellStyle(cellStyle);
					cell = row.createCell(1);
					cell.setCellValue(reportValueBean.getDivisionName());
					cell.setCellStyle(cellStyle);
					cell = row.createCell(2);
					cell.setCellValue(reportValueBean.getTotalCompRegistered());
					cell.setCellStyle(cellStyle);
					cell = row.createCell(3);
					cell.setCellValue(reportValueBean.getCompResolved());
					cell.setCellStyle(cellStyle);
					cell = row.createCell(4);
					cell.setCellValue(reportValueBean.getCompPending());
					cell.setCellStyle(cellStyle);
	        		
				}
	        }
			
			

			HttpServletResponse res = getResponse();
			res.setContentType("application/vnd.ms-excel");
			res.setHeader("Content-disposition", "attachment; filename=CallCircleDivisionReport-"+GeneralUtil.formatDate(new Date())+".xls");

			fos = res.getOutputStream();

			workbook.write(fos);
			
			fos.flush();
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			if (fos != null) {
				fos.close();
			}
			if (workbook != null) {
				workbook.close();
			}
		}

		context.responseComplete();
		context.renderResponse();
	
	}
	
}
