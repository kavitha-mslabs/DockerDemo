package tneb.ccms.admin.controller.reportController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
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
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import tneb.ccms.admin.model.ComplaintBean;
import tneb.ccms.admin.model.CustMasterBean;
import tneb.ccms.admin.model.ViewComplaintReportBean;
import tneb.ccms.admin.util.CCMSConstants;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.valuebeans.AdminUserValueBean;
import tneb.ccms.admin.valuebeans.CallCenterUserValueBean;
import tneb.ccms.admin.valuebeans.TmpCtCirBean;
import tneb.ccms.admin.valuebeans.TmpCtSecBean;
import tneb.ccms.admin.valuebeans.ViewComplaintReportValueBean;

import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;

@Named
@ViewScoped
public class CategoryWiseAbstractFormatTwoReport implements Serializable {

	private static final long serialVersionUID = 1L;
	private SessionFactory sessionFactory;
	DataModel dmFilter;
	List<TmpCtCirBean> circleReportFormatTwo = new ArrayList<>();
	List<TmpCtSecBean> sectionReportFormatTwo = new ArrayList<>();
	StreamedContent excelFile;
	StreamedContent pdfFile;
	List<ViewComplaintReportValueBean> complaintList = new ArrayList<>();
	String selectedCircleName = null;
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
		System.out.println("INITIALIZED CATEGORY WISE COMPLAINTS - ABSTRACT REPORT- FORMAT 2");
		sessionFactory = HibernateUtil.getSessionFactory();
		dmFilter = new DataModel();
		
		HttpSession httpSession = (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false);
		
		 adminUserValueBean = (AdminUserValueBean) 
				httpSession.getAttribute("sessionAdminValueBean");
		 
		 if(adminUserValueBean!=null) {
		Integer roleId = adminUserValueBean.getRoleId();
		
		// IF ROLE ID IN 1,2,3 --> SHOW SECTION ABSTRACT REPORT
		if (roleId >= 1 && roleId <= 3) {	
			
			Integer loggedInCircleId = (Integer) httpSession.getAttribute("loggedInCircleId");
			String loggedInCircleName = (String) httpSession.getAttribute("loggedInCircleName");
			fetchReportForSectionUsers(loggedInCircleId.toString(), loggedInCircleName);
			
		}else {
			
			searchCircleAbstractReportFormatTwo(); // FORMAT TWO REPORT - CIRCLE WISE
		}
		 }else {
			 searchCircleAbstractReportFormatTwo();
		 }
		
		
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
		                    	@SuppressWarnings("unchecked")
								List<Integer> regionId = session.createQuery(
			                            "select c.circleBean.regionBean.id from CallCenterMappingBean c " +
			                            "where c.callCenterUserBean.id = :userId")
			                            .setParameter("userId", userId)
			                            .getResultList();
			                    dmFilter.setCircleCode(String.valueOf(circleId.get(0)));	
			                    dmFilter.setRegionCode(String.valueOf(regionId.get(0)));
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
		                dmFilter.setDevice("MI");

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
		                    	@SuppressWarnings("unchecked")
								List<Integer> regionId =session.createQuery(
			                            "select c.circleBean.regionBean.id from CallCenterMappingBean c " +
			                            "where c.callCenterUserBean.id = :userId")
			                            .setParameter("userId", userId)
			                            .getResultList();
			                    dmFilter.setCircleCode(String.valueOf(circleId));	
			                    dmFilter.setRegionCode(String.valueOf(regionId.get(0)));
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
	
	
	@Transactional
	public void searchCircleAbstractReportFormatTwo() {
		
		updateLoginWiseFilters(); // UPDATE REGION CODE, CIRCLE CODE BASED ON LOGIN WISE
		

		Session session = null;
		try {
			session = sessionFactory.openSession();
			session.beginTransaction();


			String hql = "SELECT CIRCODE, CIRNAME, REGCODE, REGNAME, BLTOT, BLCOM, BLPEN, METOT, MECOM, MEPEN, PFTOT, PFCOM, PFPEN, VFTOT, VFCOM, VFPEN, FITOT, FICOM, FIPEN, THTOT, THCOM, THPEN, TETOT, TECOM, TEPEN, CSTOT, CSCOM, CSPEN, OTTOT, OTCOM, OTPEN FROM ABST_CIR_CT";

			List<Object[]> results = session.createNativeQuery(hql).getResultList();
			List<TmpCtCirBean> circleReport = new ArrayList<>();
			
			for (Object[] row : results) {
				TmpCtCirBean report = new TmpCtCirBean();
				report.setCircode((String) row[0]);
				report.setCirname((String) row[1]);
				report.setRegcode((String) row[2]);
				report.setRegname((String) row[3]);

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
						 

				circleReport.add(report);
			}
				
			HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance() .getExternalContext().getSession(false);

		    //AdminUserValueBean adminUserValueBean =  (AdminUserValueBean) httpsession.getAttribute("sessionAdminValueBean");

		    CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");
		    
		    if(callCenterValueBean!=null && (callCenterValueBean.getRoleId()==1 || callCenterValueBean.getRoleId()==7)) {
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

		    	circleReport = circleReport.stream()
					.filter(r -> regionId.contains(Integer.valueOf(r.getRegcode())))
					.filter(c -> circleId.contains(Integer.valueOf(c.getCircode())))
					.collect(Collectors.toList());
	        }
		    
		    
		    
			if(dmFilter.getRegionCode().equals("A")) {
				if(dmFilter.getCircleCode().equals("A")) {
					circleReportFormatTwo = circleReport;
				}else {
					circleReportFormatTwo = circleReport.stream().filter(c ->c.getCircode().equals(dmFilter.getCircleCode())).collect(Collectors.toList());
				}
			}else {
				if(dmFilter.getCircleCode().equals("A")) {
					circleReportFormatTwo = circleReport.stream().filter(a -> a.getRegcode().equals(dmFilter.getRegionCode())).collect(Collectors.toList());
				}else {

					circleReportFormatTwo = circleReport.stream().filter(a -> dmFilter.getRegionCode().equals(a.getRegcode())).filter(c->dmFilter.getCircleCode().contains(c.getCircode())).collect(Collectors.toList());

				}
			}

				System.out.println("COUNT: " + circleReportFormatTwo.size());

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in database operation");
		}
		
	}
	
	// BACK BUTTONS
    public void backToCircleReportFormatTwo() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().redirect("categoryWiseCircleAbstractReportFormat2.xhtml");
	}


	
	// SECTION REPORT FOR REGION AND CIRCLE
		@SuppressWarnings("unchecked")
		@Transactional
		public void fetchReportForSectionUsers(String circleCode,String circleName) {

			HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
	                .getExternalContext().getSession(false);
			
			AdminUserValueBean adminUserValueBean = (AdminUserValueBean) 
					httpsession.getAttribute("sessionAdminValueBean");
			
			
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

				String hql = "SELECT c.*,d.NAME as DIVISION_NAME,d.id as DivisionId,s.sub_division_id as SubDivision FROM ABST_SEC_CT c,SECTION s,DIVISION d WHERE c.SECCODE = s.ID AND d.ID =s.DIVISION_ID AND c.CIRCODE= :circleCode ";

				List<Object[]> results = session.createNativeQuery(hql).setParameter("circleCode", circleCode).getResultList();
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
					report.setDivisionId((String) row[33].toString());
					report.setSubDivisionId((String) row[34].toString());
							 

					circleSection.add(report);
				}
				

				//SECTION
				if(adminUserValueBean.getRoleId()==1) {
					sectionReportFormatTwo = circleSection.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
							.filter(section -> section.getSecCode().equalsIgnoreCase(sectionCode))
							.collect(Collectors.toList());
				}
				//DIVISION
				else if(adminUserValueBean.getRoleId()==2) {
					sectionReportFormatTwo = circleSection.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
							.filter(sd -> sd.getSubDivisionId().equalsIgnoreCase(subDivisionId))
							.collect(Collectors.toList());
				}
				//SUB DIVISION
				else if(adminUserValueBean.getRoleId()==3) {
					sectionReportFormatTwo = circleSection.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
							.filter(d -> d.getDivisionId().equalsIgnoreCase(divisionId))
							.collect(Collectors.toList());
				}
				//ALL SECTION
				else {
					sectionReportFormatTwo = circleSection.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
							.collect(Collectors.toList());
				}
				selectedCircleName = circleName;
						
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error in database operation");
			}
		}
	
	// SECTION REPORT FORMAT TWO - FOR REGION , CIRCLE
		@SuppressWarnings("unchecked")
		@Transactional
		public void fetchReportByCircleFormatTwoForRegionAndCircle(String circleCode, String circleName) {

			Session session = null;
			try {
				session = sessionFactory.openSession();
				session.beginTransaction();

				String hql = "SELECT c.*,d.NAME as DIVISION_NAME FROM ABST_SEC_CT c,SECTION s,DIVISION d WHERE c.SECCODE = s.ID AND d.ID =s.DIVISION_ID AND c.CIRCODE= :circleCode ";

				List<Object[]> results = session.createNativeQuery(hql).setParameter("circleCode", circleCode).getResultList();
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

				// FILTER ONLY SELECTED CIRCLE
				sectionReportFormatTwo = circleSection.stream()
						.filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode)).collect(Collectors.toList());
				
				// IF SECTION
//				if(adminUserValueBean.getRoleId()==1) {
//					sectionReportFormatTwo = sectionReportFormatTwo.stream().filter(section -> section.getSecCode().equals(adminUserValueBean.getSectionId().toString())).collect(Collectors.toList());
//				}
				
				selectedCircleName = circleName;
				
				FacesContext.getCurrentInstance().getExternalContext().redirect("categoryWiseSectionAbstractReportFormat2.xhtml");
			
				System.out.println("THE SECTION LIST----"+sectionReportFormatTwo.size());
				
				
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error in database operation");
			}
		}

	//COMPLAINT LIST FOR SECTION
	@Transactional
	public void getComplaintListForSection(String secCode, String sectionName) throws IOException {
		try (Session session = sessionFactory.openSession()) {

			Timestamp fromDate = new Timestamp(dmFilter.getFromDate().getTime());
			Timestamp toDate = new Timestamp(dmFilter.getToDate().getTime());

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
					+ "f.name AS Region, g.name AS Circle, h.name AS Division, i.name AS SubDivision, j.name AS Section "
					+ "FROM COMPLAINT a " + "JOIN PUBLIC_USER b ON a.user_id = b.id "
					+ "JOIN COMPLAINT_HISTORY c ON a.id = c.complaint_id AND a.status_id = c.status_id "
					+ "JOIN SUB_CATEGORY d ON a.sub_category_id = d.id "
					+ "JOIN CATEGORY k ON a.complaint_type = k.code " + "JOIN REGION f ON a.region_id = f.id "
					+ "JOIN CIRCLE g ON a.circle_id = g.id " + "JOIN DIVISION h ON a.division_id = h.id "
					+ "JOIN SUB_DIVISION i ON a.sub_division_id = i.id " + "JOIN SECTION j ON a.section_id = j.id "
					+ "WHERE a.SECTION_ID = :sectionCode " + "AND a.created_on BETWEEN :fromDate AND :toDate";

			Query query = session.createNativeQuery(hql);
			query.setParameter("sectionCode", sectionCode);
			query.setParameter("fromDate", fromDate);
			query.setParameter("toDate", toDate);

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

			FacesContext.getCurrentInstance().getExternalContext().redirect("categoryWiseComplaintList.xhtml");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	//COMPLAINT LIST FOR CIRCLE
	@Transactional
	public void getComplaintListForCircle() throws IOException {
		try (Session session = sessionFactory.openSession()) {

			Timestamp fromDate = new Timestamp(dmFilter.getFromDate().getTime());
			Timestamp toDate = new Timestamp(dmFilter.getToDate().getTime());

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

			List<ViewComplaintReportBean> complaintBeanList = new ArrayList<>();

			String hql = "SELECT a.id, " + "to_char(a.created_on, 'dd-mm-yyyy-hh24:mi') AS Complaint_Date, "
					+ "DECODE(a.device, 'web', 'Web', 'FOC', 'FOC', 'admin', 'FOC', 'SM', 'Social Media', 'Android', 'Mobile', 'AMOB', 'Mobile', 'IMOB', 'Mobile', 'iOS', 'Mobile', 'mobile', 'Mobile', 'MI', 'Minnagam') AS Device, "
					+ "a.SERVICE_NUMBER AS Service_Number, " + "a.SERVICE_NAME AS Service_Name, "
					+ "a.SERVICE_ADDRESS AS Service_Address, "
					+ "b.mobile AS Contact_Number, k.name AS Complaint_Type, d.name AS subctyp, "
					+ "a.description AS Complaint_Description, "
					+ "DECODE(a.status_id, 0, 'Pending', 1, 'In Progress', 2, 'Completed') AS Complaint_Status, "
	                +"(CASE WHEN a.status_id=2 then TO_CHAR(a.updated_on, 'DD-MM-YYYY-HH24:MI') else '' end) AS Attended_Date, " 
	                +"(CASE WHEN a.status_id=2 then c.description else '' end) AS Attended_Remarks, " 
					+ "f.name AS Region, g.name AS Circle, h.name AS Division, i.name AS SubDivision, j.name AS Section "
					+ "FROM COMPLAINT a " + "JOIN PUBLIC_USER b ON a.user_id = b.id "
					+ "JOIN COMPLAINT_HISTORY c ON a.id = c.complaint_id AND a.status_id = c.status_id "
					+ "JOIN SUB_CATEGORY d ON a.sub_category_id = d.id "
					+ "JOIN CATEGORY k ON a.complaint_type = k.code " + "JOIN REGION f ON a.region_id = f.id "
					+ "JOIN CIRCLE g ON a.circle_id = g.id " + "JOIN DIVISION h ON a.division_id = h.id "
					+ "JOIN SUB_DIVISION i ON a.sub_division_id = i.id " + "JOIN SECTION j ON a.section_id = j.id "
					+ "WHERE a.STATUS_ID IN :statusIDs " + "AND a.created_on BETWEEN :fromDate AND :toDate";

			Query query = session.createNativeQuery(hql);
			query.setParameter("statusIDs", statusIDs);
			query.setParameter("fromDate", fromDate);
			query.setParameter("toDate", toDate);

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

			FacesContext.getCurrentInstance().getExternalContext().redirect("categoryWiseComplaintList.xhtml");

		} catch (Exception e) {
			System.out.println("ERROR..........." + e);
			e.printStackTrace();
		}

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
                "(CASE WHEN a.status_id=2 then c.description else '' end) AS Attended_Remarks, " +
                "f.name AS Region, g.name AS Circle, h.name AS Division, i.name AS SubDivision, j.name AS Section, "+
                "b.FIRST_NAME AS UserName, "+
                "a.ALTERNATE_MOBILE_NO as RegisteredMobileNumber, "+
                "to_char(fb.ENTRYDT, 'dd-mm-yyyy-hh24:mi') AS feedBackEntryDate, "+
                "fb.REMARKS AS feedbackRemarks, "+
                "fb.RATING AS feedbackRating "+
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
                "LEFT JOIN DISTRIB_MASTER dm ON dm.reg_no = f.ID AND dm.SCODE = j.code AND dm.CIRCLE_CODE= g.code AND dm.DISTRIB_CODE = :distribCode "+
                "WHERE a.id = :complaintIdParam " ;
		
		Query query = session.createNativeQuery(hql);
		query.setParameter("complaintIdParam", complaintIdParam);
		query.setParameter("distribCode", distribCode);

		
		List<Object[]> results = query.getResultList();

		selectedComplaintId = new ViewComplaintReportValueBean();
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

        	
        	selectedComplaintId=dto;
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
        selectedComplaintId.setTransferDetails(transfers);
        
        
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
		  selectedComplaintId.setQualityCheckDetails(qcs);
        
        System.out.println("THE SELECED COMPLAINT ID"+ selectedComplaintId.getComplaintId());
        System.out.println("THE SELECED COMPLAINT ID"+ selectedComplaintId.getDescription());

		
		}catch(Exception e) {
			e.printStackTrace();
			
		}

		
	}


	// FORMAT TWO CIRCLE REPORT EXCEL DOWNLOAD
	public void exportFormatTwoCircleReportToExcel(List<TmpCtCirBean> circleReportFormatTwo) throws IOException {
		HSSFWorkbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet("Categorywise_Complaints_Abstract_Report-Circlewise-Format_2");

		sheet.setColumnWidth(0, 2000); // S.NO
		sheet.setColumnWidth(1, 5000); // CIRCLE

		for (int i = 2; i < 33; i++) {
			sheet.setColumnWidth(i, 4000);
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
		headingCell.setCellValue("CATEGORY WISE COMPLAINTS ABSTRACT REPORT - CIRCLE WISE - FORMAT TWO");
		headingCell.setCellStyle(headingStyle);
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 33));

		Row dateRow = sheet.createRow(1);
		Cell dateCell = dateRow.createCell(0);

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		String fromDateStr = dmFilter.getFromDate() != null ? dateFormat.format(dmFilter.getFromDate()) : "N/A";
		String toDateStr = dmFilter.getToDate() != null ? dateFormat.format(dmFilter.getToDate()) : "N/A";

		dateCell.setCellValue("From Date: " + fromDateStr + "  To Date: " + toDateStr);
		dateCell.setCellStyle(dateStyle);
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 33));

		Row headerRow1 = sheet.createRow(2);
		Row headerRow2 = sheet.createRow(3);

		Cell snoHeader = headerRow1.createCell(0);
		snoHeader.setCellValue("S.NO");
		snoHeader.setCellStyle(headerStyle);
		sheet.addMergedRegion(new CellRangeAddress(2, 3, 0, 0));

		Cell circleHeader = headerRow1.createCell(1);
		circleHeader.setCellValue("CIRCLE");
		circleHeader.setCellStyle(headerStyle);
		sheet.addMergedRegion(new CellRangeAddress(2, 3, 1, 1));

		Cell receivedHeader = headerRow1.createCell(2);
		receivedHeader.setCellValue("RECEIVED");
		receivedHeader.setCellStyle(headerStyle);
		sheet.addMergedRegion(new CellRangeAddress(2, 2, 2, 11));

		Cell completedHeader = headerRow1.createCell(12);
		completedHeader.setCellValue("COMPLETED");
		completedHeader.setCellStyle(headerStyle);
		sheet.addMergedRegion(new CellRangeAddress(2, 2, 12, 21));

		Cell pendingHeader = headerRow1.createCell(22);
		pendingHeader.setCellValue("PENDING");
		pendingHeader.setCellStyle(headerStyle);
		sheet.addMergedRegion(new CellRangeAddress(2, 2, 22, 31));

		String[] subHeaders = { "Billing Related", "Meter Related", "Power Failure", "Voltage Fluctuation", "Fire",
				"Dangerous Pole", "Theft", "Conductor Snapping", "Others", "TOTAL" };

		for (int i = 0; i < subHeaders.length; i++) {
			Cell subCell = headerRow2.createCell(2 + i);
			subCell.setCellValue(subHeaders[i]);
			subCell.setCellStyle(headerStyle);
		}

		for (int i = 0; i < subHeaders.length; i++) {
			Cell subCell = headerRow2.createCell(12 + i);
			subCell.setCellValue(subHeaders[i]);
			subCell.setCellStyle(headerStyle);
		}

		for (int i = 0; i < subHeaders.length; i++) {
			Cell subCell = headerRow2.createCell(22 + i);
			subCell.setCellValue(subHeaders[i]);
			subCell.setCellStyle(headerStyle);
		}

		int rowNum = 4;
		BigDecimal[] totalSums = new BigDecimal[30];
		Arrays.fill(totalSums, BigDecimal.ZERO);
		int serialNumber = 1;

		for (TmpCtCirBean report : circleReportFormatTwo) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(serialNumber++);
			row.createCell(1).setCellValue(report.getCirname());

			BigDecimal[] receivedValues = { report.getBlTot(), report.getMeTot(), report.getPfTot(), report.getVfTot(),
					report.getFiTot(), report.getThTot(), report.getTeTot(), report.getCsTot(), report.getOtTot(),
					report.getBlTot().add(report.getMeTot()).add(report.getPfTot()).add(report.getVfTot())
							.add(report.getFiTot()).add(report.getThTot()).add(report.getTeTot()).add(report.getCsTot())
							.add(report.getOtTot()) };

			BigDecimal[] completedValues = { report.getBlCom(), report.getMeCom(), report.getPfCom(), report.getVfCom(),
					report.getFiCom(), report.getThCom(), report.getTeCom(), report.getCsCom(), report.getOtCom(),
					report.getBlCom().add(report.getMeCom()).add(report.getPfCom()).add(report.getVfCom())
							.add(report.getFiCom()).add(report.getThCom()).add(report.getTeCom()).add(report.getCsCom())
							.add(report.getOtCom()) };

			BigDecimal[] pendingValues = { report.getBlPen(), report.getMePen(), report.getPfPen(), report.getVfPen(),
					report.getFiPen(), report.getThPen(), report.getTePen(), report.getCsPen(), report.getOtPen(),
					report.getBlPen().add(report.getMePen()).add(report.getPfPen()).add(report.getVfPen())
							.add(report.getFiPen()).add(report.getThPen()).add(report.getTePen()).add(report.getCsPen())
							.add(report.getOtPen()) };

			for (int i = 0; i < receivedValues.length; i++) {
				setCellValue(row, 2 + i, receivedValues[i]);
				totalSums[i] = totalSums[i].add(receivedValues[i] != null ? receivedValues[i] : BigDecimal.ZERO);
			}

			for (int i = 0; i < completedValues.length; i++) {
				setCellValue(row, 12 + i, completedValues[i]);
				totalSums[10 + i] = totalSums[10 + i]
						.add(completedValues[i] != null ? completedValues[i] : BigDecimal.ZERO);
			}

			for (int i = 0; i < pendingValues.length; i++) {
				setCellValue(row, 22 + i, pendingValues[i]);
				totalSums[20 + i] = totalSums[20 + i]
						.add(pendingValues[i] != null ? pendingValues[i] : BigDecimal.ZERO);
			}

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
			excelFile = DefaultStreamedContent.builder().name("Categorywise_Complaints_Abstract_Report-Circlewise-Format_2.xls")
					.contentType("application/vnd.ms-excel").stream(() -> inputStream).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// SECTION REPORT TO EXCEL DOWNLOAD
	public void exportFormatTwoSectionReportToExcel(List<TmpCtSecBean> sectionReportFormatTwo) throws IOException {
		HSSFWorkbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet("Categorywise_Complaints_Abstract_Report-Sectionwise-Format_2");

		sheet.setColumnWidth(0, 2000); // S.NO
		sheet.setColumnWidth(1, 5000); // SECTION
		sheet.setColumnWidth(2, 5000); // DIVISION

		for (int i = 3; i < 34; i++) {
			sheet.setColumnWidth(i, 4000);
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
		headingCell.setCellValue("CATEGORY WISE COMPLAINTS ABSTRACT REPORT - SECTION WISE - FORMAT TWO");
		headingCell.setCellStyle(headingStyle);
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 34));

		Row dateRow = sheet.createRow(1);
		Cell dateCell = dateRow.createCell(0);

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		String fromDateStr = dmFilter.getFromDate() != null ? dateFormat.format(dmFilter.getFromDate()) : "N/A";
		String toDateStr = dmFilter.getToDate() != null ? dateFormat.format(dmFilter.getToDate()) : "N/A";

		dateCell.setCellValue("From Date: " + fromDateStr + "  To Date: " + toDateStr);
		dateCell.setCellStyle(dateStyle);
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 34));

		Row headerRow1 = sheet.createRow(2);
		Row headerRow2 = sheet.createRow(3);

		Cell snoHeader = headerRow1.createCell(0);
		snoHeader.setCellValue("S.NO");
		snoHeader.setCellStyle(headerStyle);
		sheet.addMergedRegion(new CellRangeAddress(2, 3, 0, 0));

		Cell sectionHeader = headerRow1.createCell(1);
		sectionHeader.setCellValue("SECTION");
		sectionHeader.setCellStyle(headerStyle);
		sheet.addMergedRegion(new CellRangeAddress(2, 3, 1, 1));

		Cell divisionHeader = headerRow1.createCell(2);
		divisionHeader.setCellValue("DIVISION");
		divisionHeader.setCellStyle(headerStyle);
		sheet.addMergedRegion(new CellRangeAddress(2, 3, 2, 2));

		Cell receivedHeader = headerRow1.createCell(3);
		receivedHeader.setCellValue("RECEIVED");
		receivedHeader.setCellStyle(headerStyle);
		sheet.addMergedRegion(new CellRangeAddress(2, 2, 3, 12));

		Cell completedHeader = headerRow1.createCell(13);
		completedHeader.setCellValue("COMPLETED");
		completedHeader.setCellStyle(headerStyle);
		sheet.addMergedRegion(new CellRangeAddress(2, 2, 13, 22));

		Cell pendingHeader = headerRow1.createCell(23);
		pendingHeader.setCellValue("PENDING");
		pendingHeader.setCellStyle(headerStyle);
		sheet.addMergedRegion(new CellRangeAddress(2, 2, 23, 32));

		String[] subHeaders = { "Billing Related", "Meter Related", "Power Failure", "Voltage Fluctuation", "Fire",
				"Dangerous Pole", "Theft", "Conductor Snapping", "Others", "TOTAL" };

		for (int i = 0; i < subHeaders.length; i++) {
			Cell subCell = headerRow2.createCell(3 + i);
			subCell.setCellValue(subHeaders[i]);
			subCell.setCellStyle(headerStyle);
		}

		for (int i = 0; i < subHeaders.length; i++) {
			Cell subCell = headerRow2.createCell(13 + i);
			subCell.setCellValue(subHeaders[i]);
			subCell.setCellStyle(headerStyle);
		}

		for (int i = 0; i < subHeaders.length; i++) {
			Cell subCell = headerRow2.createCell(23 + i);
			subCell.setCellValue(subHeaders[i]);
			subCell.setCellStyle(headerStyle);
		}

		int rowNum = 4;
		BigDecimal[] totalSums = new BigDecimal[30];
		Arrays.fill(totalSums, BigDecimal.ZERO);
		int serialNumber = 1;

		for (TmpCtSecBean report : sectionReportFormatTwo) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(serialNumber++);
			row.createCell(1).setCellValue(report.getSecName());
			row.createCell(2).setCellValue(report.getDivisionName());

			BigDecimal[] receivedValues = { report.getBlTot(), report.getMeTot(), report.getPfTot(), report.getVfTot(),
					report.getFiTot(), report.getThTot(), report.getTeTot(), report.getCsTot(), report.getOtTot(),
					report.getBlTot().add(report.getMeTot()).add(report.getPfTot()).add(report.getVfTot())
							.add(report.getFiTot()).add(report.getThTot()).add(report.getTeTot()).add(report.getCsTot())
							.add(report.getOtTot()) };

			BigDecimal[] completedValues = { report.getBlCom(), report.getMeCom(), report.getPfCom(), report.getVfCom(),
					report.getFiCom(), report.getThCom(), report.getTeCom(), report.getCsCom(), report.getOtCom(),
					report.getBlCom().add(report.getMeCom()).add(report.getPfCom()).add(report.getVfCom())
							.add(report.getFiCom()).add(report.getThCom()).add(report.getTeCom()).add(report.getCsCom())
							.add(report.getOtCom()) };

			BigDecimal[] pendingValues = { report.getBlPen(), report.getMePen(), report.getPfPen(), report.getVfPen(),
					report.getFiPen(), report.getThPen(), report.getTePen(), report.getCsPen(), report.getOtPen(),
					report.getBlPen().add(report.getMePen()).add(report.getPfPen()).add(report.getVfPen())
							.add(report.getFiPen()).add(report.getThPen()).add(report.getTePen()).add(report.getCsPen())
							.add(report.getOtPen()) };

			for (int i = 0; i < receivedValues.length; i++) {
				setCellValue(row, 3 + i, receivedValues[i]);
				totalSums[i] = totalSums[i].add(receivedValues[i] != null ? receivedValues[i] : BigDecimal.ZERO);
			}

			for (int i = 0; i < completedValues.length; i++) {
				setCellValue(row, 13 + i, completedValues[i]);
				totalSums[10 + i] = totalSums[10 + i]
						.add(completedValues[i] != null ? completedValues[i] : BigDecimal.ZERO);
			}

			for (int i = 0; i < pendingValues.length; i++) {
				setCellValue(row, 23 + i, pendingValues[i]);
				totalSums[20 + i] = totalSums[20 + i]
						.add(pendingValues[i] != null ? pendingValues[i] : BigDecimal.ZERO);
			}
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
		totalRow.createCell(2).setCellValue("");
		totalRow.getCell(0).setCellStyle(totalRowStyle);
		totalRow.getCell(1).setCellStyle(totalRowStyle);
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
			excelFile = DefaultStreamedContent.builder().name("Categorywise_Complaints_Abstract_Report-Sectionwise-Format_2.xls")
					.contentType("application/vnd.ms-excel").stream(() -> inputStream).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setCellValue(Row row, int columnIndex, Number value) {
		if (value != null) {
			if (value instanceof BigDecimal) {
				row.createCell(columnIndex).setCellValue(value.doubleValue());
			} else if (value instanceof Integer || value instanceof Long || value instanceof Double
					|| value instanceof Float) {
				row.createCell(columnIndex).setCellValue(value.doubleValue());
			} else {
				row.createCell(columnIndex).setCellValue(value.toString());
			}
		} else {
			row.createCell(columnIndex).setCellValue("");
		}
	}

	// CIRCLE REPORT TO PDF DOWNLOAD
	public void exportFormatTwoCircleReportToPDF(List<TmpCtCirBean> reports) throws IOException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Document document = new Document(PageSize.A2.rotate());
			PdfWriter.getInstance(document, baos);
			document.open();

			// TITLE
			Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
			Paragraph title = new Paragraph("CATEGORY WISE COMPLAINTS ABSTRACT REPORT - CIRCLE WISE - FORMAT TWO", titleFont);
			title.setAlignment(Element.ALIGN_CENTER);
			title.setSpacingAfter(20);
			document.add(title);

			// SUB TITLE - DATE RANGE
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			String fromDateStr = dmFilter.getFromDate() != null ? dateFormat.format(dmFilter.getFromDate()) : "N/A";
			String toDateStr = dmFilter.getToDate() != null ? dateFormat.format(dmFilter.getToDate()) : "N/A";

			Font subTitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
			Paragraph subTitle = new Paragraph("FROM :" + fromDateStr + " | " + "TO :" + toDateStr, subTitleFont);
			subTitle.setAlignment(Element.ALIGN_CENTER);
			subTitle.setSpacingAfter(20);
			document.add(subTitle);

			// TABLE
			PdfPTable table = new PdfPTable(32);
			table.setWidthPercentage(100);
			table.setSpacingBefore(10f);
			table.setSpacingAfter(10f);

			float[] columnWidths = new float[] { 1f, 5f, 3f, 3f, 3f, 3f, 3f,3f, 3f, 3f, 3f, 3f, 3f, 3f, 3f, 3f, 3f, 3f,
					3f, 3f, 3f, 3f, 3f, 3f, 3f, 3f, 3f, 3f, 3f, 3f, 3f, 3f };
			table.setWidths(columnWidths);

			// TABLE HEADER
			Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
			BaseColor lightGray = new BaseColor(211, 211, 211);
			BaseColor borderColor = new BaseColor(13, 1, 1);

			
			addCell(table, "S.NO", headerFont, lightGray, borderColor, Element.ALIGN_CENTER);
			addCell(table, "CIRCLE", headerFont, lightGray, borderColor, Element.ALIGN_CENTER);

			// MERGED HEADER
			PdfPCell receivedCell = new PdfPCell(new Phrase("RECEIVED", headerFont));
			receivedCell.setColspan(10);
			receivedCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			receivedCell.setBackgroundColor(lightGray);
			receivedCell.setBorderColor(borderColor);
			table.addCell(receivedCell);

			PdfPCell completedCell = new PdfPCell(new Phrase("COMPLETED", headerFont));
			completedCell.setColspan(10);
			completedCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			completedCell.setBackgroundColor(lightGray);
			completedCell.setBorderColor(borderColor);
			table.addCell(completedCell);

			PdfPCell pendingCell = new PdfPCell(new Phrase("PENDING", headerFont));
			pendingCell.setColspan(10);
			pendingCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			pendingCell.setBackgroundColor(lightGray);
			pendingCell.setBorderColor(borderColor);
			table.addCell(pendingCell);

			// SUB HEADER FOR MERGED HEADER
			Font subHeaderFont = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);
			String[] subHeaders = { "Billing Related", "Meter Related", "Power Failure", "Voltage Fluctuation", "Fire",
					"Dangerous Pole", "Theft", "Conductor Snapping", "Others", "TOTAL" };

			
			addCell(table, "", subHeaderFont, null, borderColor, Element.ALIGN_CENTER);
			addCell(table, "", subHeaderFont, null, borderColor, Element.ALIGN_CENTER);

			
			for (int i = 0; i < 3; i++) {
				for (String header : subHeaders) {
					addCell(table, header, subHeaderFont, lightGray, borderColor, Element.ALIGN_CENTER);
				}
			}

			// DATA
			BigDecimal[] columnTotals = new BigDecimal[30];
			Arrays.fill(columnTotals, BigDecimal.ZERO);

			int serialNumber = 1;
			for (TmpCtCirBean report : reports) {
				// S.NO 
				addCell(table, String.valueOf(serialNumber++), new Font(Font.FontFamily.HELVETICA, 8), null,
						borderColor, Element.ALIGN_CENTER);

				// CIRCLE
				addCell(table, report.getCirname(), new Font(Font.FontFamily.HELVETICA, 8), null, borderColor,
						Element.ALIGN_LEFT);

				// RECEIVED values (columns 2-11)
				BigDecimal[] receivedValues = { report.getBlTot(), report.getMeTot(), report.getPfTot(),
						report.getVfTot(), report.getFiTot(), report.getThTot(), report.getTeTot(), report.getCsTot(),
						report.getOtTot(),
						report.getBlTot().add(report.getMeTot()).add(report.getPfTot()).add(report.getVfTot())
								.add(report.getFiTot()).add(report.getThTot()).add(report.getTeTot())
								.add(report.getCsTot()).add(report.getOtTot()) };
				addValueCells(table, receivedValues, columnTotals, 0);

				// COMPLETED values (columns 12-21)
				BigDecimal[] completedValues = { report.getBlCom(), report.getMeCom(), report.getPfCom(),
						report.getVfCom(), report.getFiCom(), report.getThCom(), report.getTeCom(), report.getCsCom(),
						report.getOtCom(),
						report.getBlCom().add(report.getMeCom()).add(report.getPfCom()).add(report.getVfCom())
								.add(report.getFiCom()).add(report.getThCom()).add(report.getTeCom())
								.add(report.getCsCom()).add(report.getOtCom()) };
				addValueCells(table, completedValues, columnTotals, 10);

				// PENDING values (columns 22-31)
				BigDecimal[] pendingValues = { report.getBlPen(), report.getMePen(), report.getPfPen(),
						report.getVfPen(), report.getFiPen(), report.getThPen(), report.getTePen(), report.getCsPen(),
						report.getOtPen(),
						report.getBlPen().add(report.getMePen()).add(report.getPfPen()).add(report.getVfPen())
								.add(report.getFiPen()).add(report.getThPen()).add(report.getTePen())
								.add(report.getCsPen()).add(report.getOtPen()) };
				addValueCells(table, pendingValues, columnTotals, 20);
			}

			//FOOTER ROW - TOTAL
			Font footerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);
			BaseColor footerBgColor = new BaseColor(230, 230, 230);

			addCell(table, "", footerFont, null, borderColor, Element.ALIGN_CENTER);
			addCell(table, "TOTAL", footerFont, footerBgColor, borderColor, Element.ALIGN_CENTER);

			for (BigDecimal total : columnTotals) {
				addCell(table, total.toString(), footerFont, footerBgColor, borderColor, Element.ALIGN_CENTER);
			}

			document.add(table);
			document.close();

			pdfFile = DefaultStreamedContent.builder().name("Categorywise_Complaints_Abstract_Report-Sectionwise-Format_2.pdf")
					.contentType("application/pdf").stream(() -> new ByteArrayInputStream(baos.toByteArray())).build();

		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	private void addCell(PdfPTable table, String text, Font font, BaseColor bgColor, BaseColor borderColor,
			int alignment) {
		PdfPCell cell = new PdfPCell(new Phrase(text, font));
		if (bgColor != null) {
			cell.setBackgroundColor(bgColor);
		}
		cell.setBorderColor(borderColor != null ? borderColor : BaseColor.BLACK);
		cell.setBorderWidth(1f);
		cell.setHorizontalAlignment(alignment);
		cell.setPadding(3);
		table.addCell(cell);
	}

	private void addValueCells(PdfPTable table, BigDecimal[] values, BigDecimal[] columnTotals, int offset) {
		Font dataFont = new Font(Font.FontFamily.HELVETICA, 8);
		BaseColor borderColor = new BaseColor(100, 100, 100);

		for (int i = 0; i < values.length; i++) {
			addCell(table, values[i].toString(), dataFont, null, borderColor, Element.ALIGN_RIGHT);
			columnTotals[offset + i] = columnTotals[offset + i].add(values[i]);
		}
	}

	// SECTION REPORT TO PDF DOWNLOAD
	public void exportFormatTwoSectionReportToPDF(List<TmpCtSecBean> sectionReportFormatTwo)
			throws IOException, DocumentException {
		Document document = new Document(PageSize.A1.rotate());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfWriter.getInstance(document, baos);
		document.open();

		Font titleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
		Font dateFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
		Font headerFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.BLACK);
		Font dataFont = new Font(Font.FontFamily.HELVETICA, 8);
		Font totalFont = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);
		BaseColor headerBgColor = new BaseColor(208, 208, 208);
		BaseColor borderColor = new BaseColor(0, 0, 0);
		
		// TITLE
		Paragraph title = new Paragraph("CATEGORY WISE COMPLAINTS ABSTRACT REPORT - SECTION WISE - FORMAT TWO", titleFont);
		title.setAlignment(Element.ALIGN_CENTER);
		document.add(title);

		// SUB TITLE - DATE
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		String fromDateStr = dmFilter.getFromDate() != null ? dateFormat.format(dmFilter.getFromDate()) : "N/A";
		String toDateStr = dmFilter.getToDate() != null ? dateFormat.format(dmFilter.getToDate()) : "N/A";

		Paragraph dateRange = new Paragraph("From Date: " + fromDateStr + "  To Date: " + toDateStr, dateFont);
		dateRange.setAlignment(Element.ALIGN_CENTER);
		document.add(dateRange);
		document.add(Chunk.NEWLINE);

		// TABLE
		PdfPTable table = new PdfPTable(33);
		table.setWidthPercentage(100);
		table.setSpacingBefore(10f);
		table.setSpacingAfter(10f);

		float[] columnWidths = new float[] { 2f, 4f, 4f, // S.NO, SECTION, DIVISION
				// RECEIVED (10 columns)
				3f, 3f, 3f, 3f, 3f, 3f, 3f, 3f, 3f, 3f,
				// COMPLETED (10 columns)
				3f, 3f, 3f, 3f, 3f, 3f, 3f, 3f,3f, 3f,
				// PENDING (10 columns)
				3f, 3f, 3f, 3f, 3f, 3f, 3f,3f, 3f, 3f };
		table.setWidths(columnWidths);

		// SUB HEADER FOR MERGED HEADER
		String[] subHeaders = { "Billing Related", "Meter Related", "Power Failure", "Voltage Fluctuation", "Fire",
				"Dangerous Pole", "Theft", "Conductor Snapping", "Others", "TOTAL" };

		
		addCell(table, "S.NO", headerFont, headerBgColor, borderColor, 1, 1, Element.ALIGN_CENTER);
		addCell(table, "SECTION", headerFont, headerBgColor, borderColor, 1, 1, Element.ALIGN_CENTER);
		addCell(table, "DIVISION", headerFont, headerBgColor, borderColor, 1, 1, Element.ALIGN_CENTER);
		addCell(table, "RECEIVED", headerFont, headerBgColor, borderColor, 10, 1, Element.ALIGN_CENTER);
		addCell(table, "COMPLETED", headerFont, headerBgColor, borderColor, 10, 1, Element.ALIGN_CENTER);
		addCell(table, "PENDING", headerFont, headerBgColor, borderColor, 10, 1, Element.ALIGN_CENTER);

		
		addCell(table, "", headerFont, null, borderColor, 1, 1, Element.ALIGN_CENTER); // Empty for S.NO
		addCell(table, "", headerFont, null, borderColor, 1, 1, Element.ALIGN_CENTER); // Empty for SECTION
		addCell(table, "", headerFont, null, borderColor, 1, 1, Element.ALIGN_CENTER); // Empty for DIVISION

		for (int i = 0; i < 3; i++) {
			for (String header : subHeaders) {
				addCell(table, header, headerFont, headerBgColor, borderColor, 1, 1, Element.ALIGN_CENTER);
			}
		}

		// DATA
		int serialNumber = 1;
		BigDecimal[] totalSums = new BigDecimal[30];
		Arrays.fill(totalSums, BigDecimal.ZERO);

		for (TmpCtSecBean report : sectionReportFormatTwo) {
			// S.NO, SECTION, DIVISION
			addCell(table, String.valueOf(serialNumber++), dataFont, null, borderColor, 1, 1, Element.ALIGN_CENTER);
			addCell(table, report.getSecName(), dataFont, null, borderColor, 1, 1, Element.ALIGN_LEFT);
			addCell(table, report.getDivisionName(), dataFont, null, borderColor, 1, 1, Element.ALIGN_LEFT);

			// VALUE FOR RECEIVED, COMPLETED,PENDING
			BigDecimal[] receivedValues = calculateValues(report, "Tot");
			BigDecimal[] completedValues = calculateValues(report, "Com");
			BigDecimal[] pendingValues = calculateValues(report, "Pen");

			addValueCells(table, receivedValues, dataFont, borderColor, totalSums, 0);
			addValueCells(table, completedValues, dataFont, borderColor, totalSums, 10);
			addValueCells(table, pendingValues, dataFont, borderColor, totalSums, 20);
		}

		// FOOTER
		addCell(table, "", totalFont, null, borderColor, 1, 1, Element.ALIGN_CENTER);
		addCell(table, "TOTAL", totalFont, new BaseColor(220, 220, 220), borderColor, 1, 1, Element.ALIGN_LEFT);
		addCell(table, "", totalFont, new BaseColor(220, 220, 220), borderColor, 1, 1, Element.ALIGN_LEFT);

		for (BigDecimal sum : totalSums) {
			addCell(table, sum.toString(), totalFont, new BaseColor(220, 220, 220), borderColor, 1, 1,
					Element.ALIGN_RIGHT);
		}

		document.add(table);
		document.close();

		pdfFile = DefaultStreamedContent.builder().name("Categorywise_Complaints_Abstract_Report-Sectionwise-Format_2.pdf")
				.contentType("application/pdf").stream(() -> new ByteArrayInputStream(baos.toByteArray())).build();
	}

	private BigDecimal[] calculateValues(TmpCtSecBean report, String suffix) {
		BigDecimal bl = getValue(report, "Bl" + suffix);
		BigDecimal me = getValue(report, "Me" + suffix);
		BigDecimal pf = getValue(report, "Pf" + suffix);
		BigDecimal vf = getValue(report, "Vf" + suffix);
		BigDecimal fi = getValue(report, "Fi" + suffix);
		BigDecimal th = getValue(report, "Th" + suffix);
		BigDecimal te = getValue(report, "Te" + suffix);
		BigDecimal cs = getValue(report, "Cs" + suffix);
		BigDecimal ot = getValue(report, "Ot" + suffix);

		BigDecimal total = bl.add(me).add(pf).add(vf).add(fi).add(th).add(te).add(cs).add(ot);

		return new BigDecimal[] { bl, me, pf, vf, fi, th, te, cs, ot, total };
	}

	private BigDecimal getValue(TmpCtSecBean report, String fieldName) {
		try {
			Method method = report.getClass().getMethod("get" + fieldName);
			BigDecimal value = (BigDecimal) method.invoke(report);
			return value != null ? value : BigDecimal.ZERO;
		} catch (Exception e) {
			return BigDecimal.ZERO;
		}
	}

	private void addValueCells(PdfPTable table, BigDecimal[] values, Font font, BaseColor borderColor,
			BigDecimal[] totals, int offset) {
		for (int i = 0; i < values.length; i++) {
			addCell(table, values[i].toString(), font, null, borderColor, 1, 1, Element.ALIGN_RIGHT);
			totals[offset + i] = totals[offset + i].add(values[i]);
		}
	}

	private void addCell(PdfPTable table, String text, Font font, BaseColor bgColor, BaseColor borderColor, int colspan,
			int rowspan, int alignment) {
		PdfPCell cell = new PdfPCell(new Phrase(text, font));
		cell.setColspan(colspan);
		cell.setRowspan(rowspan);
		cell.setHorizontalAlignment(alignment);
		if (bgColor != null) {
			cell.setBackgroundColor(bgColor);
		}
		cell.setBorderColor(borderColor);
		cell.setBorderWidth(1f);
		cell.setPadding(3);
		table.addCell(cell);
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

	public List<TmpCtCirBean> getCircleReportFormatTwo() {
		return circleReportFormatTwo;
	}

	public void setCircleReportFormatTwo(List<TmpCtCirBean> circleReportFormatTwo) {
		this.circleReportFormatTwo = circleReportFormatTwo;
	}

	public List<TmpCtSecBean> getSectionReportFormatTwo() {
		return sectionReportFormatTwo;
	}

	public void setSectionReportFormatTwo(List<TmpCtSecBean> sectionReportFormatTwo) {
		this.sectionReportFormatTwo = sectionReportFormatTwo;
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
