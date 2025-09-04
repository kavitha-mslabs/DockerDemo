package tneb.ccms.admin.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import tneb.ccms.admin.AdminMain;
import tneb.ccms.admin.dao.ComplaintsDao;
import tneb.ccms.admin.model.LoginParams;
import tneb.ccms.admin.util.CCMSConstants;
import tneb.ccms.admin.valuebeans.ComplaintValueBean;

public class ReportController {

	private Logger logger = LoggerFactory.getLogger(ReportController.class.getName());

	@ManagedProperty("#{admin}")
	AdminMain admin;
	
	LoginParams officer;

	public ReportController() {
		super();
		init();
	}

	public void init() {
		admin = (AdminMain) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("admin");
		officer = admin.getAuth().getOfficer();
	}
	
	public static HttpServletResponse getResponse() {
		return (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
	}
	
	public void excelExport() {
		
		try {
			ComplaintsDao dao = new ComplaintsDao();
			
			List<ComplaintValueBean> compliantList = dao.getComplaintList(officer.fieldBeanName, officer.officeId);
			
			generateReport(compliantList);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
	}
	
	public void pdfExport() {
		
		try {
			ComplaintsDao dao = new ComplaintsDao();
			
			List<ComplaintValueBean> compliantList = dao.getComplaintList(officer.fieldBeanName, officer.officeId);
			
			generatePdfReport(compliantList);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
	}

	
	public void generatePdfReport(List<ComplaintValueBean> compliantList) throws IOException {
		FacesContext context = FacesContext.getCurrentInstance();
		Document document = new Document();
		PdfWriter writer = null;
		OutputStream fos = null;
		PdfPCell cell = null;
		try {
			
		    HttpServletResponse res = getResponse();
			 
			fos = res.getOutputStream();

			writer = PdfWriter.getInstance(document, fos);
			
	        document.open();
	 
	        PdfPTable table = new PdfPTable(6); // 6 columns.
	        table.setWidthPercentage(100); //Width 100%
	        table.setSpacingBefore(10f); //Space before table
	        table.setSpacingAfter(10f); //Space after table
	 
	        //Set Column widths
			table.setWidths(new float[]{0.7f, 0.7f, 1, 1, 0.8f, 1.5f});
			
			Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
			Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
			
			cell = new PdfPCell(new Paragraph("Type", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Status", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Service Number", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Description", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Date", boldFont));
		    table.addCell(cell);
		    cell = new PdfPCell(new Paragraph("Address", boldFont));
		    table.addCell(cell);
			
			for(ComplaintValueBean complaintValueBean : compliantList) {
				
			    cell = new PdfPCell(new Paragraph((complaintValueBean.getComplaintType() != null) ? complaintValueBean.getComplaintType() : "", normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(complaintValueBean.getStatus(), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph((complaintValueBean.getServiceNumber() != null) ? complaintValueBean.getServiceNumber() : "", normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph((complaintValueBean.getDescription() != null) ? complaintValueBean.getDescription() : "", normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph(complaintValueBean.getCreatedOnFormatted(), normalFont));
			    table.addCell(cell);
			    cell = new PdfPCell(new Paragraph((complaintValueBean.getServiceAddress() != null) ? complaintValueBean.getServiceAddress() : "", normalFont));
			    table.addCell(cell);
		        
			}

	    
	 
	        document.add(table);
	        
			res.setContentType("application/vnd.ms-excel");
			res.setHeader("Content-disposition", "attachment; filename=Complaints.pdf");

			
			fos.flush();
	 
			context.responseComplete();
			context.renderResponse();
			
		} catch (IOException | DocumentException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
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

	public void generateReport(List<ComplaintValueBean> compliantList) throws IOException {
		
		FacesContext context = FacesContext.getCurrentInstance();
		
		HSSFWorkbook workbook = new HSSFWorkbook();
		OutputStream fos = null;
		try {
			HSSFSheet sheet = workbook.createSheet();
			
			sheet.setColumnWidth(0, 2500);
			sheet.setColumnWidth(1, 5000);
			sheet.setColumnWidth(2, 5000);
			sheet.setColumnWidth(3, 5000);
			sheet.setColumnWidth(4, 5000);
			sheet.setColumnWidth(5, 5000);
			sheet.setColumnWidth(6, 4000);
			sheet.setColumnWidth(7, 4000);
			sheet.setColumnWidth(8, 5000);
			sheet.setColumnWidth(9, 5000);
			sheet.setColumnWidth(10, 4000);
			sheet.setColumnWidth(11, 12000);
			sheet.setColumnWidth(12, 3000);
			sheet.setColumnWidth(13, 6000);
			sheet.setColumnWidth(14, 15000);
			
			
			HSSFRow row;
			HSSFCell cell;
			HSSFCellStyle cellStyle;

			HSSFFont defaultFont = workbook.createFont();
			defaultFont.setFontHeightInPoints((short) 10);
			defaultFont.setFontName("Arial");
			defaultFont.setColor(IndexedColors.BLACK.getIndex());
			defaultFont.setBold(false);
			defaultFont.setItalic(false);

			HSSFFont font = workbook.createFont();
			font.setFontHeightInPoints((short) 10);
			font.setFontName("Arial");
			font.setColor(IndexedColors.WHITE.getIndex());
			font.setBold(true);
			font.setItalic(false);

			
			int rowCounter = 0;
			
			row = sheet.createRow(rowCounter++);
			
			cellStyle = workbook.createCellStyle();
			cellStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
			cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			cellStyle.setFont(font);
			
			cell = row.createCell(0);
			cell.setCellValue("S.No");
			cell.setCellStyle(cellStyle);
			cell = row.createCell(1);
			cell.setCellValue("Region Name");
			cell.setCellStyle(cellStyle);
			cell = row.createCell(2);
			cell.setCellValue("Circle Name");
			cell.setCellStyle(cellStyle);
			cell = row.createCell(3);
			cell.setCellValue("Division Name");
			cell.setCellStyle(cellStyle);
			cell = row.createCell(4);
			cell.setCellValue("Sub Divison Name");
			cell.setCellStyle(cellStyle);
			cell = row.createCell(5);
			cell.setCellValue("Section Name");
			cell.setCellStyle(cellStyle);
			cell = row.createCell(6);
			cell.setCellValue("Type");
			cell.setCellStyle(cellStyle);
			cell = row.createCell(7);
			cell.setCellValue("Status");
			cell.setCellStyle(cellStyle);
			cell = row.createCell(8);
			cell.setCellValue("Category");
			cell.setCellStyle(cellStyle);
			cell = row.createCell(9);
			cell.setCellValue("Sub Category");
			cell.setCellStyle(cellStyle);
			cell = row.createCell(10);
			cell.setCellValue("Service Number");
			cell.setCellStyle(cellStyle);
			cell = row.createCell(11);
			cell.setCellValue("Description");
			cell.setCellStyle(cellStyle);
			cell = row.createCell(12);
			cell.setCellValue("Date");
			cell.setCellStyle(cellStyle);
			cell = row.createCell(13);
			cell.setCellValue("LandMark");
			cell.setCellStyle(cellStyle);
			cell = row.createCell(14);
			cell.setCellValue("Street");
			cell.setCellStyle(cellStyle);
			
			
			for(ComplaintValueBean complaintValueBean : compliantList) {
				row = sheet.createRow(rowCounter++);
				
				cellStyle = workbook.createCellStyle();
				cellStyle.setFont(defaultFont);
				
				cell = row.createCell(0);
				cell.setCellValue(String.valueOf(rowCounter-1));
				cell.setCellStyle(cellStyle);
				cell = row.createCell(1);
				cell.setCellValue((complaintValueBean.getRegionName() != null) ? complaintValueBean.getRegionName() : "");
				cell.setCellStyle(cellStyle);
				cell = row.createCell(2);
				cell.setCellValue((complaintValueBean.getCircleName() != null) ? complaintValueBean.getCircleName() : "");
				cell.setCellStyle(cellStyle);
				cell = row.createCell(3);
				cell.setCellValue((complaintValueBean.getDivisionName() != null) ? complaintValueBean.getDivisionName() : "");
				cell.setCellStyle(cellStyle);
				cell = row.createCell(4);
				cell.setCellValue((complaintValueBean.getSubDivisionName() != null) ? complaintValueBean.getSubDivisionName() : "");
				cell.setCellStyle(cellStyle);
				cell = row.createCell(5);
				cell.setCellValue((complaintValueBean.getSectionName() != null) ? complaintValueBean.getSectionName() : "");
				cell.setCellStyle(cellStyle);
				cell = row.createCell(6);
				cell.setCellValue((complaintValueBean.getComplaintType() != null) ? complaintValueBean.getComplaintType() : "");
				cell.setCellStyle(cellStyle);
				cell = row.createCell(7);
				cell.setCellValue(complaintValueBean.getStatus());
				cell.setCellStyle(cellStyle);
				cell = row.createCell(8);
				cell.setCellValue((complaintValueBean.getCategoryName() != null) ? complaintValueBean.getCategoryName() : "");
				cell.setCellStyle(cellStyle);
				cell = row.createCell(9);
				cell.setCellValue((complaintValueBean.getSubCategoryName() != null) ? complaintValueBean.getSubCategoryName() : "");
				cell.setCellStyle(cellStyle);
				cell = row.createCell(10);
				cell.setCellValue((complaintValueBean.getServiceNumber() != null) ? complaintValueBean.getServiceNumber() : "");
				cell.setCellStyle(cellStyle);
				cell = row.createCell(11);
				cell.setCellValue((complaintValueBean.getDescription() != null) ? complaintValueBean.getDescription() : "");
				cell.setCellStyle(cellStyle);
				cell = row.createCell(12);
				cell.setCellValue(complaintValueBean.getCreatedOnFormatted());
				cell.setCellStyle(cellStyle);
				cell = row.createCell(13);
				cell.setCellValue((complaintValueBean.getLandmark() != null) ? complaintValueBean.getLandmark() : "");
				cell.setCellStyle(cellStyle);
				cell = row.createCell(14);
				cell.setCellValue((complaintValueBean.getServiceAddress() != null) ? complaintValueBean.getServiceAddress() : "");
				cell.setCellStyle(cellStyle);
				
			}

			HttpServletResponse res = getResponse();
			res.setContentType("application/vnd.ms-excel");
			res.setHeader("Content-disposition", "attachment; filename=Complaints.xls");

			fos = res.getOutputStream();

			workbook.write(fos);
			
			fos.flush();
			
		} catch (IOException e) {
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
