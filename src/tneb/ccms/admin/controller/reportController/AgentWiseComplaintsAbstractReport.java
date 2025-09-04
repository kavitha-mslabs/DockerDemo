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
import tneb.ccms.admin.valuebeans.AgentWiseComplaintsValueBean;

@Named
@ViewScoped
public class AgentWiseComplaintsAbstractReport implements Serializable {

	private static final long serialVersionUID = 1L;
	private boolean initialized = false;
	private SessionFactory sessionFactory;
	DataModel dmFilter;
	StreamedContent excelFile;
	StreamedContent pdfFile;
	private Date currentDate = new Date();
	private String currentYear = String.valueOf(Year.now().getValue());

	List<AgentWiseComplaintsValueBean> resultList; // AGENT DETAILS



	@PostConstruct
	public void init() {
		System.out.println("Initializing AGENT WISE COMPLAINTS RECEIVED REPORT...");
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
	public void getAgentWiseComplaintsAbstractReport() {

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

			String sql = "SELECT c.comp_ent_user AS AGENT, COUNT(*) AS COMPLAINTS_REVD " + "FROM complaint c "
					+ "JOIN minnagam_user_request m ON m.user_name = c.comp_ent_user "
					+ "where TRUNC(c.created_on)=TO_DATE(:date,'DD-MM-YYYY')" + "GROUP BY c.comp_ent_user";

			Query query = session.createSQLQuery(sql);
			query.setParameter("date", formattedFromDate);

			@SuppressWarnings("unchecked")
			List<Object[]> agentList = query.list();

			resultList = new ArrayList<AgentWiseComplaintsValueBean>();

			for (Object[] row : agentList) {
				AgentWiseComplaintsValueBean dto = new AgentWiseComplaintsValueBean();
				dto.setAgent((String) row[0]);
				dto.setComplaints((BigDecimal) row[1]);

				resultList.add(dto);
			}

			session.getTransaction().commit();

			if (resultList.size() == 0) {
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "",
						"No Details For The Given Date");
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
	public void clearagentWiseComplaints() {
		dmFilter = new DataModel();
		dmFilter.setFromDate(null);
		resultList = new ArrayList<AgentWiseComplaintsValueBean>();
	}

	// EXCEL DOWNLOAD 
	public void exportAgentWiseComplaintsToExcel(List<AgentWiseComplaintsValueBean> reportData) throws IOException {
		HSSFWorkbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet("AgentWise_Complaints_Report");

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
		headingCell.setCellValue("AGENT WISE COMPLAINTS REPORT");
		headingCell.setCellStyle(headingStyle);
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

		Row dateRow = sheet.createRow(1);
		Cell dateCell = dateRow.createCell(0);

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		String fromDateStr = dmFilter.getFromDate() != null ? dateFormat.format(dmFilter.getFromDate()) : "N/A";

		dateCell.setCellValue("Date: " + fromDateStr);
		dateCell.setCellStyle(dateStyle);
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));

		Row headerRow = sheet.createRow(2);

		String[] headers = { "S.NO", "USER NAME", "NO.OF.COMPLAINTS" };

		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(headerStyle);
		}

		int rowNum = 3;
		int serialNo = 1;
		for (AgentWiseComplaintsValueBean agent : reportData) {
			Row row = sheet.createRow(rowNum++);
			int col = 0;
			row.createCell(col++).setCellValue(serialNo++);
			row.createCell(col++).setCellValue(agent.getAgent());
			row.createCell(col++).setCellValue(agent.getComplaints().doubleValue());


		}
		for (int i = 0; i < headers.length; i++) {
			sheet.autoSizeColumn(i);
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		workbook.write(outputStream);
		workbook.close();

		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		try {
			excelFile = DefaultStreamedContent.builder().name("AgentWise_Complaints_Report.xls")
					.contentType("application/vnd.ms-excel").stream(() -> inputStream).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//PDF DOWNLOAD
	public void exportAgentWiseComplaintsToPdf(List<AgentWiseComplaintsValueBean> reportData) throws IOException {

		final int COLUMN_COUNT = 3;
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

			Paragraph title = new Paragraph("AGENT WISE COMPLAINTS REPORT", TITLE_FONT);
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

			String[] headers = { "S.NO", "USER NAME", "NO.OF.COMPLAINTS" };
			for (String header : headers) {
				PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
				table.addCell(cell);
			}
			int sno = 1;

			for (AgentWiseComplaintsValueBean report : reportData) {
				table.addCell(createDataCell(String.valueOf(sno++), DATA_FONT, Element.ALIGN_CENTER));

				table.addCell(createDataCell(report.getAgent(), DATA_FONT, Element.ALIGN_LEFT));
				table.addCell(createDataCell(report.getComplaints().toString(), DATA_FONT, Element.ALIGN_LEFT));
				

			}

			document.add(table);
			document.close();

			pdfFile = DefaultStreamedContent.builder().contentType("application/pdf").name("AgentWise_Complaints_Report.pdf")
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

	public List<AgentWiseComplaintsValueBean> getResultList() {
		return resultList;
	}

	public void setResultList(List<AgentWiseComplaintsValueBean> resultList) {
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
