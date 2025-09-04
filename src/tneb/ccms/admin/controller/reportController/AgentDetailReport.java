package tneb.ccms.admin.controller.reportController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.*;
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
import tneb.ccms.admin.valuebeans.CallCenterUserValueBean;
import tneb.ccms.admin.valuebeans.UserDetailsValueBean;

@Named
@ViewScoped
public class AgentDetailReport implements Serializable {

	private static final long serialVersionUID = 1L;
	private boolean initialized = false;
	private SessionFactory sessionFactory;
	DataModel dmFilter;
	StreamedContent excelFile;
	StreamedContent pdfFile;
	private Date currentDate = new Date();
	private String currentYear = String.valueOf(Year.now().getValue());

	List<UserDetailsValueBean> resultList; // AGENT DETAILS



	@PostConstruct
	public void init() {
		System.out.println("Initializing AGENT DETAILS REPORT...");
		sessionFactory = HibernateUtil.getSessionFactory();
		dmFilter = new DataModel();
	}

	// REFRESH PAGE
	public void resetIfNeeded() {
		if (!FacesContext.getCurrentInstance().isPostback() && initialized) {
			resultList = null;
			dmFilter.setFromDate(null);
			dmFilter.setToDate(null);
		}
		initialized = true;
	}
	
	// AGENT DETAIL REPORT
	@Transactional
	public void getAgentDetailReport() {

		if (dmFilter.getFromDate() == null) {
		    Calendar cal = Calendar.getInstance();
		    cal.setTime(new Date()); 
		    dmFilter.setFromDate(cal.getTime());
		}

		Session session = null;
		try {
			session = sessionFactory.openSession();
			session.beginTransaction();

			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			String formattedFromDate = sdf.format(dmFilter.getFromDate());

			String sql = 
				    "SELECT * FROM ( " +
				    "    SELECT USER_NAME AS un, " +
				    "           NAME, " +
				    "           EMAIL_ID, " +
				    "           TO_CHAR(MOBILE_NUMBER) AS MOBILE_NUMBER, " +
				    "           TO_CHAR(UPDATED_ON, 'dd-MM-yyyy') AS date_field, " +
				    "           TO_CHAR(SYSDATE, 'dd-MM-yyyy') AS to_date, " +
				    "           CIRCLE_CODE " +
				    "    FROM minnagam_user_request " +
				    "    WHERE TRUNC(UPDATED_ON) <= TO_DATE(:fromDate, 'dd-MM-yyyy') " +
				    "    UNION ALL " +
				    "    SELECT USER_NAME AS un, " +
				    "           NAME, " +
				    "           EMAIL_ID, " +
				    "           TO_CHAR(MOBILE_NUMBER) AS MOBILE_NUMBER, " +
				    "           TO_CHAR(FROM_DT, 'dd-MM-yyyy') AS date_field, " +
				    "           TO_CHAR(TO_DT, 'dd-MM-yyyy') AS to_date, " +
				    "           CIRCLE_CODE " +
				    "    FROM call_center_user_history c " +
				    "    WHERE TRUNC(FROM_DT) <= TO_DATE(:fromDate, 'dd-MM-yyyy') " +
				    "      AND NOT EXISTS ( " +
				    "          SELECT 1 FROM minnagam_user_request m " +
				    "          WHERE m.USER_NAME = c.USER_NAME " +
				    "            AND TRUNC(m.UPDATED_ON) <= TO_DATE(:fromDate, 'dd-MM-yyyy') " +
				    "      ) " +
				    ") ORDER BY un";


			Query query = session.createSQLQuery(sql);
			query.setParameter("fromDate", formattedFromDate);

			@SuppressWarnings("unchecked")
			List<Object[]> agentList = query.list();

			resultList = new ArrayList<UserDetailsValueBean>();

			for (Object[] row : agentList) {
				UserDetailsValueBean dto = new UserDetailsValueBean();
				dto.setUserName((String) row[0]);
				dto.setName((String) row[1]);
				dto.setEmail((String) row[2]);
				dto.setMobileNumber((String) row[3]);
				dto.setUpdatedDate((String) row[4]);
				dto.setToDate((String) row[5]);
				dto.setCircleCode((String) row[6]);

				resultList.add(dto);
			}
			
			HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
	                .getExternalContext().getSession(false);
	        CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");

	        if(callCenterValueBean!=null) {
	        	int roleId =callCenterValueBean.getRoleId();
	        	int userId = callCenterValueBean.getId();
	        	if(roleId==1) {
	        		 @SuppressWarnings("unchecked")
						List<String> circleId = session.createQuery(
	                            "select c.circleBean.code from CallCenterMappingBean c " +
	                            "where c.callCenterUserBean.id = :userId")
	                            .setParameter("userId", userId)
	                            .getResultList();

	        		 resultList = resultList.stream()
	        				 .filter(c->circleId.contains(c.getCircleCode())).collect(Collectors.toList());
	        		
	        	}
	        }

			session.getTransaction().commit();
			
			   if(resultList.size()==0) {
		        	FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,"","No Agent Details For The Given Date");
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

    // REFRESH BUTTON
	public void clearagentDetailReport() {
		dmFilter = new DataModel();
		dmFilter.setFromDate(null);
		resultList = new ArrayList<UserDetailsValueBean>();
	}

	// EXCEL DOWNLOAD 
	public void exportAgentDetailToExcel(List<UserDetailsValueBean> reportData) throws IOException {
		HSSFWorkbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet("Agent_Detail_Report");

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
		headingCell.setCellValue("AGENT DETAIL REPORT");
		headingCell.setCellStyle(headingStyle);
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

		Row dateRow = sheet.createRow(1);
		Cell dateCell = dateRow.createCell(0);

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		String fromDateStr = dmFilter.getFromDate() != null ? dateFormat.format(dmFilter.getFromDate()) : "N/A";

		dateCell.setCellValue("Date: " + fromDateStr);
		dateCell.setCellStyle(dateStyle);
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 8));

		Row headerRow = sheet.createRow(2);

		String[] headers = { "S.NO", "USER NAME", "NAME", "EMAIL", "MOBILE NUMBER", "UPDATED", "TO DATE",
				"CIRCLE CODE" };

		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(headerStyle);
		}

		int rowNum = 3;
		int serialNo = 1;
		for (UserDetailsValueBean agent : reportData) {
			Row row = sheet.createRow(rowNum++);
			int col = 0;
			row.createCell(col++).setCellValue(serialNo++);
			row.createCell(col++).setCellValue(agent.getUserName());
			row.createCell(col++).setCellValue(agent.getName());
			row.createCell(col++).setCellValue(agent.getEmail());
			row.createCell(col++).setCellValue(agent.getMobileNumber());
			row.createCell(col++).setCellValue(agent.getUpdatedDate());
			row.createCell(col++).setCellValue(agent.getToDate());
			row.createCell(col++).setCellValue(agent.getCircleCode());

		}
		for (int i = 0; i < headers.length; i++) {
			sheet.autoSizeColumn(i);
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		workbook.write(outputStream);
		workbook.close();

		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		try {
			excelFile = DefaultStreamedContent.builder().name("Agent_Detail_Report.xls")
					.contentType("application/vnd.ms-excel").stream(() -> inputStream).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//PDF DOWNLOAD
	public void exportAgentDetailToPdf(List<UserDetailsValueBean> reportData) throws IOException {

		final int COLUMN_COUNT = 8;
		final float SNO_COLUMN_WIDTH = 4f;
		final float OTHER_COLUMN_WIDTH = 15f;
		final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
		final Font TOTAL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
		final Font DATA_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
		final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		String fromDate = sdf.format(dmFilter.getFromDate());

		Document document = new Document(PageSize.A3);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try {
			PdfWriter.getInstance(document, outputStream);
			document.open();

			PdfPTable table = new PdfPTable(COLUMN_COUNT);
			table.setWidthPercentage(100);
			table.setSpacingBefore(10f);

			Paragraph title = new Paragraph("AGENT DETAIL REPORT", TITLE_FONT);
			title.setAlignment(Element.ALIGN_CENTER);
			document.add(title);

			Paragraph subTitle = new Paragraph("DATE : " + fromDate);
			subTitle.setAlignment(Element.ALIGN_CENTER);
			subTitle.setSpacingAfter(10);
			document.add(subTitle);

			float[] columnWidths = new float[COLUMN_COUNT];
			columnWidths[0] = SNO_COLUMN_WIDTH;
			for (int i = 1; i < COLUMN_COUNT; i++) {
				columnWidths[i] = OTHER_COLUMN_WIDTH;
			}
			table.setWidths(columnWidths);

			String[] headers = { "S.NO", "USER NAME", "NAME", "EMAIL", "MOBILE NUMBER", "UPDATED", "TO DATE",
					"CIRCLE CODE" };
			for (String header : headers) {
				PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
				table.addCell(cell);
			}
			int sno = 1;

			for (UserDetailsValueBean report : reportData) {
				table.addCell(createDataCell(String.valueOf(sno++), DATA_FONT, Element.ALIGN_CENTER));

				table.addCell(createDataCell(report.getUserName(), DATA_FONT, Element.ALIGN_LEFT));
				table.addCell(createDataCell(report.getName(), DATA_FONT, Element.ALIGN_LEFT));
				table.addCell(createDataCell(report.getEmail(), DATA_FONT, Element.ALIGN_LEFT));

				table.addCell(createDataCell(report.getMobileNumber(), DATA_FONT, Element.ALIGN_LEFT));
				table.addCell(createDataCell(report.getUpdatedDate(), DATA_FONT, Element.ALIGN_LEFT));
				table.addCell(createDataCell(report.getToDate(), DATA_FONT, Element.ALIGN_LEFT));
				table.addCell(createDataCell(report.getCircleCode(), DATA_FONT, Element.ALIGN_LEFT));

			}

			document.add(table);
			document.close();

			pdfFile = DefaultStreamedContent.builder().contentType("application/pdf").name("Agent_Detail_Report.pdf")
					.stream(() -> new ByteArrayInputStream(outputStream.toByteArray())).build();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private PdfPCell createDataCell(String text, Font font, int alignment) {
		PdfPCell cell = new PdfPCell(new Phrase(text, font));
		cell.setHorizontalAlignment(alignment);
		return cell;
	}

	public List<UserDetailsValueBean> getResultList() {
		return resultList;
	}

	public void setResultList(List<UserDetailsValueBean> resultList) {
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
