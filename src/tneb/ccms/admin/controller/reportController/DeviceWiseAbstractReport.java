package tneb.ccms.admin.controller.reportController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
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
import tneb.ccms.admin.valuebeans.CallCenterUserValueBean;
import tneb.ccms.admin.valuebeans.ModeWiseAbstractValueBean;
import tneb.ccms.admin.valuebeans.ViewComplaintReportValueBean;

import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;

@Named
@ViewScoped
public class DeviceWiseAbstractReport implements Serializable {

	private static final long serialVersionUID = 1L;
	private SessionFactory sessionFactory;
	DataModel dmFilter;
	List<ModeWiseAbstractValueBean> circleListFormatOne = new ArrayList<>(); // FORMAT 1 REPORT - MODE WISE

	List<ModeWiseAbstractValueBean> sectionListFormatOne = new ArrayList<>(); // FORMAT 1 REPORT - MODE WISE

	StreamedContent excelFile;
	StreamedContent pdfFile;
	List<ViewComplaintReportValueBean> complaintList = new ArrayList<>();
	String selectedCircleName = null;
	String selectedSectionName = null;
	String redirectFrom;
	ViewComplaintReportValueBean selectedComplaintId;
	private Date currentDate = new Date();
	private String currentYear = String.valueOf(Year.now().getValue());
	AdminUserValueBean adminUserValueBean;


	private BigDecimal totalMobileTotal = BigDecimal.ZERO;
	private BigDecimal totalAdminTotal = BigDecimal.ZERO;
	private BigDecimal totalSmsTotal = BigDecimal.ZERO;

	private BigDecimal totalMiTotal = BigDecimal.ZERO;
	private BigDecimal totalWebTotal = BigDecimal.ZERO;
	private BigDecimal totalWebComp = BigDecimal.ZERO;

	private BigDecimal totalMobileComp = BigDecimal.ZERO;
	private BigDecimal totalAdminComp = BigDecimal.ZERO;
	private BigDecimal totalSmsComp = BigDecimal.ZERO;

	private BigDecimal totalMiComp = BigDecimal.ZERO;
	private BigDecimal totalWebPending = BigDecimal.ZERO;
	private BigDecimal totalMobilePending = BigDecimal.ZERO;

	private BigDecimal totalAdminPending = BigDecimal.ZERO;
	private BigDecimal totalSmsPending = BigDecimal.ZERO;
	private BigDecimal totalMiPending = BigDecimal.ZERO;

	private BigDecimal totalMobileTotalSection = BigDecimal.ZERO;
	private BigDecimal totalAdminTotalSection = BigDecimal.ZERO;
	private BigDecimal totalSmsTotalSection = BigDecimal.ZERO;

	private BigDecimal totalMiTotalSection = BigDecimal.ZERO;
	private BigDecimal totalWebTotalSection = BigDecimal.ZERO;
	private BigDecimal totalWebCompSection = BigDecimal.ZERO;

	private BigDecimal totalMobileCompSection = BigDecimal.ZERO;
	private BigDecimal totalAdminCompSection = BigDecimal.ZERO;
	private BigDecimal totalSmsCompSection = BigDecimal.ZERO;

	private BigDecimal totalMiCompSection = BigDecimal.ZERO;
	private BigDecimal totalWebPendingSection = BigDecimal.ZERO;
	private BigDecimal totalMobilePendingSection = BigDecimal.ZERO;

	private BigDecimal totalAdminPendingSection = BigDecimal.ZERO;
	private BigDecimal totalSmsPendingSection = BigDecimal.ZERO;
	private BigDecimal totalMiPendingSection = BigDecimal.ZERO;

	
	private BigDecimal grandTotalRevd = BigDecimal.ZERO;
	private BigDecimal grandTotalComp = BigDecimal.ZERO;
	private BigDecimal grandTotalPend = BigDecimal.ZERO;
	
	private BigDecimal grandTotalRevdSection = BigDecimal.ZERO;
	private BigDecimal grandTotalCompSection = BigDecimal.ZERO;
	private BigDecimal grandTotalPendSection = BigDecimal.ZERO;
	
	
	@PostConstruct
	public void init() {
		System.out.println("Initializing DEVICE WISE ABSTRACT FORMAT 1...");
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
			
			searchDeviceWiseAbstractFormatOne(); // DEVICE WISE ABSTRACT FORMAT 1
		}
		}else {
			searchDeviceWiseAbstractFormatOne();
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
		                    	dmFilter.setRegionCode("A");
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
		                dmFilter.setDevice("M");

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
			                    dmFilter.setCircleCode(String.valueOf(circleId.get(0)));	
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
	public void searchDeviceWiseAbstractFormatOne() { // FORMAT ONE REPORT - MODE WISE
		System.out.println("THE FORMAT ONE REPORT REDIRETED");
		
		updateLoginWiseFilters();
		
		Session session = null;
		try {
			session = sessionFactory.openSession();
			session.beginTransaction();
			try {
				String hql = "SELECT REGCODE, REGNAME, CIRCODE, CIRNAME, WBTOT, WBCOM, WBPEN, MBTOT, MBCOM, MBPEN, ADTOT, ADCOM, ADPEN, SMTOT, SMCOM, SMPEN, MITOT, MICOM, MIPEN FROM ABST_CIR_DEV";

				List<Object[]> results = session.createNativeQuery(hql).getResultList();
				List<ModeWiseAbstractValueBean> resultList = new ArrayList<>();

				for (Object[] row : results) {
					ModeWiseAbstractValueBean report = new ModeWiseAbstractValueBean();
					report.setRegionCode((String) row[0]);
					report.setRegionName((String) row[1]);
					report.setCircleCode((String) row[2]);
					report.setCircleName((String) row[3]);

					report.setWebTotal((BigDecimal) row[4]);
					report.setWebComp((BigDecimal) row[5]);
					report.setWebPending((BigDecimal) row[6]);

					report.setMobileTotal((BigDecimal) row[7]);
					report.setMobileComp((BigDecimal) row[8]);
					report.setMobilePending((BigDecimal) row[9]);

					report.setAdminTotal((BigDecimal) row[10]);
					report.setAdminComp((BigDecimal) row[11]);
					report.setAdminPending((BigDecimal) row[12]);

					report.setSmsTotal((BigDecimal) row[13]);
					report.setSmsComp((BigDecimal) row[14]);
					report.setSmsPending((BigDecimal) row[15]);

					report.setMiTotal((BigDecimal) row[16]);
					report.setMiComp((BigDecimal) row[17]);
					report.setMiPending((BigDecimal) row[18]);

					resultList.add(report);
				}
				HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
		                .getExternalContext().getSession(false);
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

					resultList = resultList.stream()
						.filter(r -> regionId.contains(Integer.valueOf(r.getRegionCode())))
						.filter(c -> circleId.contains(Integer.valueOf(c.getCircleCode())))
						.collect(Collectors.toList());

		        }
		        
			    
				if(dmFilter.getRegionCode().equals("A")) {
					if(dmFilter.getCircleCode().equals("A")) {
						circleListFormatOne = resultList;
					}else {
						circleListFormatOne = resultList.stream().filter(c ->c.getCircleCode().equals(dmFilter.getCircleCode())).collect(Collectors.toList());
					}
				}else {
					if(dmFilter.getCircleCode().equals("A")) {
						circleListFormatOne = resultList.stream().filter(a -> a.getRegionCode().equals(dmFilter.getRegionCode())).collect(Collectors.toList());
					}else {

						circleListFormatOne = resultList.stream().filter(a -> dmFilter.getRegionCode().equals(a.getRegionCode())).filter(c->dmFilter.getCircleCode().contains(c.getCircleCode())).collect(Collectors.toList());

					}
				}
								
				System.out.println("THE RESULT LIST -----------"+resultList.size());
							
				System.out.println("THE REGION CODE ----------"+dmFilter.getRegionCode());
				System.out.println("THE CIRCLE CODE ------------"+dmFilter.getCircleCode());
				System.out.println("THE CIRCLE LIST -------------------------------"+circleListFormatOne.size());
				
				computeTotal(); // TOTAL FOOTER AND GRAND TOTAL VALUES
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("ERROR IN FETCHING REPORT");
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR IN DATABASE OPERATION");
		}
	}

	private void computeTotal() {
		
		HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);		
		AdminUserValueBean adminUserValueBean = (AdminUserValueBean) httpsession.getAttribute("sessionAdminValueBean");
		
		if(adminUserValueBean!=null && adminUserValueBean.getRoleId()==10) {
			
			totalWebTotal = BigDecimal.ZERO;
			totalMobileTotal = BigDecimal.ZERO;
			totalAdminTotal =BigDecimal.ZERO;
			totalSmsTotal = BigDecimal.ZERO;
			totalMiTotal = circleListFormatOne.stream().map(ModeWiseAbstractValueBean::getMiTotal).reduce(BigDecimal.ZERO,
					BigDecimal::add);

			totalWebComp =BigDecimal.ZERO;
			totalMobileComp = BigDecimal.ZERO;
			totalAdminComp = BigDecimal.ZERO;
			totalSmsComp =BigDecimal.ZERO;
			totalMiComp = circleListFormatOne.stream().map(ModeWiseAbstractValueBean::getMiComp).reduce(BigDecimal.ZERO,
					BigDecimal::add);

			totalWebPending = BigDecimal.ZERO;
			totalMobilePending =BigDecimal.ZERO;
			totalAdminPending = BigDecimal.ZERO;
			totalSmsPending = BigDecimal.ZERO;
			totalMiPending = circleListFormatOne.stream().map(ModeWiseAbstractValueBean::getMiPending)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			grandTotalRevd = totalMiTotal;
			grandTotalComp = totalMiComp;
			grandTotalPend = totalMiPending;
			
		}else {
			totalWebTotal = circleListFormatOne.stream().map(ModeWiseAbstractValueBean::getWebTotal).reduce(BigDecimal.ZERO,
					BigDecimal::add);
			totalMobileTotal = circleListFormatOne.stream().map(ModeWiseAbstractValueBean::getMobileTotal)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			totalAdminTotal = circleListFormatOne.stream().map(ModeWiseAbstractValueBean::getAdminTotal)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			totalSmsTotal = circleListFormatOne.stream().map(ModeWiseAbstractValueBean::getSmsTotal).reduce(BigDecimal.ZERO,
					BigDecimal::add);
			totalMiTotal = circleListFormatOne.stream().map(ModeWiseAbstractValueBean::getMiTotal).reduce(BigDecimal.ZERO,
					BigDecimal::add);

			totalWebComp = circleListFormatOne.stream().map(ModeWiseAbstractValueBean::getWebComp).reduce(BigDecimal.ZERO,
					BigDecimal::add);
			totalMobileComp = circleListFormatOne.stream().map(ModeWiseAbstractValueBean::getMobileComp)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			totalAdminComp = circleListFormatOne.stream().map(ModeWiseAbstractValueBean::getAdminComp)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			totalSmsComp = circleListFormatOne.stream().map(ModeWiseAbstractValueBean::getSmsComp).reduce(BigDecimal.ZERO,
					BigDecimal::add);
			totalMiComp = circleListFormatOne.stream().map(ModeWiseAbstractValueBean::getMiComp).reduce(BigDecimal.ZERO,
					BigDecimal::add);

			totalWebPending = circleListFormatOne.stream().map(ModeWiseAbstractValueBean::getWebPending)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			totalMobilePending = circleListFormatOne.stream().map(ModeWiseAbstractValueBean::getMobilePending)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			totalAdminPending = circleListFormatOne.stream().map(ModeWiseAbstractValueBean::getAdminPending)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			totalSmsPending = circleListFormatOne.stream().map(ModeWiseAbstractValueBean::getSmsPending)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			totalMiPending = circleListFormatOne.stream().map(ModeWiseAbstractValueBean::getMiPending)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			
			grandTotalRevd = totalWebTotal.add(totalMobileTotal).add(totalAdminTotal).add(totalSmsTotal).add(totalMiTotal);
			grandTotalComp = totalWebComp.add(totalMobileComp).add(totalAdminComp).add(totalSmsComp).add(totalMiComp);
			grandTotalPend = totalWebPending.add(totalMobilePending).add(totalAdminPending).add(totalSmsPending).add(totalMiPending);

		}
		

	}

	public void redirectToCircleReportFormatOne() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().redirect("deviceWiseCircleAbstract.xhtml");
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
				
				String hql = "SELECT c.*,d.NAME as DIVISION_NAME,d.id as Division_Id,s.sub_division_id as subDivision FROM ABST_SEC_DEV c,SECTION s,DIVISION d WHERE c.SECCODE = s.ID AND d.ID =s.DIVISION_ID AND c.CIRCODE= :circleCode ";

				List<Object[]> results = session.createNativeQuery(hql).setParameter("circleCode", circleCode).getResultList();
				
				List<ModeWiseAbstractValueBean> circleSection = new ArrayList<>();

				for (Object[] row : results) {
					ModeWiseAbstractValueBean report = new ModeWiseAbstractValueBean();

					report.setCircleCode((String) row[0]);
					report.setCircleName((String) row[1]);

					report.setSectionCode((String) row[2]);
					report.setSectionName((String) row[3]);

					report.setRegionCode((String) row[4]);

					report.setWebTotal((BigDecimal) row[5]);
					report.setWebComp((BigDecimal) row[6]);
					report.setWebPending((BigDecimal) row[7]);

					report.setMobileTotal((BigDecimal) row[8]);
					report.setMobileComp((BigDecimal) row[9]);
					report.setMobilePending((BigDecimal) row[10]);

					report.setAdminTotal((BigDecimal) row[11]);
					report.setAdminComp((BigDecimal) row[12]);
					report.setAdminPending((BigDecimal) row[13]);

					report.setSmsTotal((BigDecimal) row[14]);
					report.setSmsComp((BigDecimal) row[15]);
					report.setSmsPending((BigDecimal) row[16]);

					report.setMiTotal((BigDecimal) row[17]);
					report.setMiComp((BigDecimal) row[18]);
					report.setMiPending((BigDecimal) row[19]);

					report.setDivisionName((String) row[20]);
					report.setDivisionId((String) row[21].toString());
					report.setSubDivisionId((String) row[22].toString());

					circleSection.add(report);
				}
				

				//SECTION
				if(adminUserValueBean.getRoleId()==1) {
					sectionListFormatOne = circleSection.stream().filter(circle -> circle.getCircleCode().equalsIgnoreCase(circleCode))
							.filter(section -> section.getSectionCode().equalsIgnoreCase(sectionCode))
							.collect(Collectors.toList());
				}
				// SUB DIVISION
				else if(adminUserValueBean.getRoleId()==2) {
					sectionListFormatOne = circleSection.stream().filter(circle -> circle.getCircleCode().equalsIgnoreCase(circleCode))
							.filter(sd -> sd.getSubDivisionId().equalsIgnoreCase(subDivisionId))
							.collect(Collectors.toList());
				}
				// DIVISION
				else if(adminUserValueBean.getRoleId()==3) {
					sectionListFormatOne = circleSection.stream().filter(circle -> circle.getCircleCode().equalsIgnoreCase(circleCode))
							.filter(d -> d.getDivisionId().equalsIgnoreCase(divisionId))
							.collect(Collectors.toList());
				}
				//ALL SECTION
				else {
					sectionListFormatOne = circleSection.stream().filter(circle -> circle.getCircleCode().equalsIgnoreCase(circleCode))
							.collect(Collectors.toList());
				}
				selectedCircleName = circleName;
							
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error in database operation");
			}
		}
	
	// SECTION REPORT FORMAT ONE - MODE WISE - FOR REGION AND CIRCLE
	@Transactional
	public void fetchReportByCircleFormatOneForRegionAndCircle(String circleCode, String circleName) {
		
		Session session = null;
		try {
			session = sessionFactory.openSession();
			session.beginTransaction();

			if (dmFilter.getSectionCode() == null) {
				dmFilter.setSectionCode("A");
			}
			if (dmFilter.getComplaintType() == null) {
				dmFilter.setComplaintType("AL");
			}

			String hql = "SELECT c.*, d.NAME AS DIVISION_NAME FROM ABST_SEC_DEV c, SECTION s, DIVISION d WHERE c.SECCODE = s.ID AND d.ID = s.DIVISION_ID AND c.CIRCODE = :circleCode";

			List<Object[]> results = session.createNativeQuery(hql).setParameter("circleCode", circleCode).getResultList();
			List<ModeWiseAbstractValueBean> circleSection = new ArrayList<>();

			for (Object[] row : results) {
				ModeWiseAbstractValueBean report = new ModeWiseAbstractValueBean();

				report.setCircleCode((String) row[0]);
				report.setCircleName((String) row[1]);

				report.setSectionCode((String) row[2]);
				report.setSectionName((String) row[3]);

				report.setRegionCode((String) row[4]);

				report.setWebTotal((BigDecimal) row[5]);
				report.setWebComp((BigDecimal) row[6]);
				report.setWebPending((BigDecimal) row[7]);

				report.setMobileTotal((BigDecimal) row[8]);
				report.setMobileComp((BigDecimal) row[9]);
				report.setMobilePending((BigDecimal) row[10]);

				report.setAdminTotal((BigDecimal) row[11]);
				report.setAdminComp((BigDecimal) row[12]);
				report.setAdminPending((BigDecimal) row[13]);

				report.setSmsTotal((BigDecimal) row[14]);
				report.setSmsComp((BigDecimal) row[15]);
				report.setSmsPending((BigDecimal) row[16]);

				report.setMiTotal((BigDecimal) row[17]);
				report.setMiComp((BigDecimal) row[18]);
				report.setMiPending((BigDecimal) row[19]);

				report.setDivisionName((String) row[20]);

				circleSection.add(report);
			}


			sectionListFormatOne = circleSection.stream().filter(c -> c.getCircleCode().equals(circleCode))
					.collect(Collectors.toList());
			computeTotalForSection(); // TOTAL FOOTER AND GRAND TOTAL VALUES
			
//			if(adminUserValueBean.getRoleId()==1) {
//				sectionListFormatOne = sectionListFormatOne.stream().filter(section -> section.getSectionCode().equals(adminUserValueBean.getSectionId().toString())).collect(Collectors.toList());
//			}
			
			FacesContext.getCurrentInstance().getExternalContext().redirect("deviceWiseSectionAbstract.xhtml");
			selectedCircleName = circleName;

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in database operation");
		}
	}

	// REFRESH REPORT LIST BUTTON
	public void clearReportData() {

		circleListFormatOne = null;
		dmFilter = new DataModel();
		dmFilter.setFromDate(null);
		dmFilter.setToDate(null);

	}

	private void computeTotalForSection() {
		HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false);
		
		AdminUserValueBean adminUserValueBean = (AdminUserValueBean) 
				httpsession.getAttribute("sessionAdminValueBean");
		
		if(adminUserValueBean!=null && adminUserValueBean.getRoleId()==10) {
			totalMiTotalSection = sectionListFormatOne.stream().map(ModeWiseAbstractValueBean::getMiTotal)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			
			totalMiCompSection = sectionListFormatOne.stream().map(ModeWiseAbstractValueBean::getMiComp)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			
			totalMiPendingSection = sectionListFormatOne.stream().map(ModeWiseAbstractValueBean::getMiPending)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			
			totalWebTotalSection = BigDecimal.ZERO;
			totalMobileTotalSection = BigDecimal.ZERO;
			totalAdminTotalSection = BigDecimal.ZERO;
			totalSmsTotalSection = BigDecimal.ZERO;

			totalWebCompSection =BigDecimal.ZERO;
			totalMobileCompSection = BigDecimal.ZERO;
			totalAdminCompSection = BigDecimal.ZERO;
			totalSmsCompSection =BigDecimal.ZERO;


			totalWebPendingSection =BigDecimal.ZERO;
			totalMobilePendingSection =BigDecimal.ZERO;
			totalAdminPendingSection = BigDecimal.ZERO;
			totalSmsPendingSection = BigDecimal.ZERO;

			
			
			grandTotalRevdSection =totalMiTotalSection;
			grandTotalCompSection = totalMiCompSection;
			grandTotalPendSection = totalMiPendingSection;
			
			
		}else {
		
		totalWebTotalSection = sectionListFormatOne.stream().map(ModeWiseAbstractValueBean::getWebTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalMobileTotalSection = sectionListFormatOne.stream().map(ModeWiseAbstractValueBean::getMobileTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalAdminTotalSection = sectionListFormatOne.stream().map(ModeWiseAbstractValueBean::getAdminTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalSmsTotalSection = sectionListFormatOne.stream().map(ModeWiseAbstractValueBean::getSmsTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalMiTotalSection = sectionListFormatOne.stream().map(ModeWiseAbstractValueBean::getMiTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		totalWebCompSection = sectionListFormatOne.stream().map(ModeWiseAbstractValueBean::getWebComp)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalMobileCompSection = sectionListFormatOne.stream().map(ModeWiseAbstractValueBean::getMobileComp)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalAdminCompSection = sectionListFormatOne.stream().map(ModeWiseAbstractValueBean::getAdminComp)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalSmsCompSection = sectionListFormatOne.stream().map(ModeWiseAbstractValueBean::getSmsComp)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalMiCompSection = sectionListFormatOne.stream().map(ModeWiseAbstractValueBean::getMiComp)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		totalWebPendingSection = sectionListFormatOne.stream().map(ModeWiseAbstractValueBean::getWebPending)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalMobilePendingSection = sectionListFormatOne.stream().map(ModeWiseAbstractValueBean::getMobilePending)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalAdminPendingSection = sectionListFormatOne.stream().map(ModeWiseAbstractValueBean::getAdminPending)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalSmsPendingSection = sectionListFormatOne.stream().map(ModeWiseAbstractValueBean::getSmsPending)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalMiPendingSection = sectionListFormatOne.stream().map(ModeWiseAbstractValueBean::getMiPending)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		grandTotalRevdSection =totalWebTotalSection.add(totalMobileTotalSection).add(totalAdminTotalSection).add(totalSmsTotalSection).add(totalMiTotalSection);
		grandTotalCompSection = totalWebCompSection.add(totalMobileCompSection).add(totalAdminCompSection).add(totalSmsCompSection).add(totalMiCompSection);
		grandTotalPendSection = totalWebPendingSection.add(totalMobilePendingSection).add(totalAdminPendingSection).add(totalSmsPendingSection).add(totalMiPendingSection);
		}

	}

	// CIRCLE REPORT TO EXCEL DOWNLOAD - FORMAT ONE - MODE WISE
	public void exportCircleToExcelFormatOne(List<ModeWiseAbstractValueBean> reports) throws IOException {

	    HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
	    CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");
        AdminUserValueBean adminUserValueBean = (AdminUserValueBean) httpsession.getAttribute("sessionAdminValueBean");

	    // Create workbook and sheet
	    HSSFWorkbook workbook = new HSSFWorkbook();
	    Sheet sheet = workbook.createSheet("ModeWise_Complaints_Abstract_Report-Circle_Wise");

	    // Create styles
	    CellStyle titleStyle = workbook.createCellStyle();
	    HSSFFont titleFont = workbook.createFont();
	    titleFont.setBold(true);
	    titleFont.setFontHeightInPoints((short) 12);
	    titleStyle.setFont(titleFont);
	    titleStyle.setAlignment(HorizontalAlignment.CENTER);
	    titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

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

	    CellStyle subHeaderStyle = workbook.createCellStyle();
	    HSSFFont dateFont = workbook.createFont();
	    dateFont.setBold(true);
	    dateFont.setFontHeightInPoints((short) 10);
	    subHeaderStyle.setFont(dateFont);
	    subHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
	    subHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

	    CellStyle dataStyle = workbook.createCellStyle();
	    dataStyle.setAlignment(HorizontalAlignment.LEFT);
	    dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
	    dataStyle.setBorderBottom(BorderStyle.THIN);
	    dataStyle.setBorderTop(BorderStyle.THIN);
	    dataStyle.setBorderLeft(BorderStyle.THIN);
	    dataStyle.setBorderRight(BorderStyle.THIN);

	    CellStyle totalStyle = workbook.createCellStyle();
	    HSSFFont totalFont = workbook.createFont();
	    totalFont.setBold(true);
	    totalStyle.setFont(totalFont);
	    totalStyle.setAlignment(HorizontalAlignment.LEFT);
	    totalStyle.setVerticalAlignment(VerticalAlignment.CENTER);
	    totalStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	    totalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	    totalStyle.setBorderBottom(BorderStyle.THIN);
	    totalStyle.setBorderTop(BorderStyle.THIN);
	    totalStyle.setBorderLeft(BorderStyle.THIN);
	    totalStyle.setBorderRight(BorderStyle.THIN);

	    // Title row
	    Row titleRow = sheet.createRow(0);
	    Cell titleCell = titleRow.createCell(0);
	    titleCell.setCellValue("MODE WISE COMPLAINTS ABSTRACT REPORT - CIRCLE WISE");
	    titleCell.setCellStyle(titleStyle);

	    // Date row
	    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	    String fromDateStr = dmFilter.getFromDate() != null ? dateFormat.format(dmFilter.getFromDate()) : "N/A";
	    String toDateStr = dmFilter.getToDate() != null ? dateFormat.format(dmFilter.getToDate()) : "N/A";
	    Row dateRow = sheet.createRow(1);
	    Cell dateCell = dateRow.createCell(0);
	    dateCell.setCellValue("From Date: " + fromDateStr + "  To Date: " + toDateStr);
	    dateCell.setCellStyle(subHeaderStyle);

	    // Check for call center roles 3 or 7
	    boolean isSimplifiedView = (callCenterValueBean != null && 
	                              (callCenterValueBean.getRoleId() == 3 || callCenterValueBean.getRoleId() == 7 )) || (adminUserValueBean!=null && adminUserValueBean.getRoleId()==10 );

	    if (isSimplifiedView) {
	        // SIMPLIFIED VIEW FOR ROLES 3 AND 7
	        sheet.setColumnWidth(0, 1500); // S.NO
	        sheet.setColumnWidth(1, 4000); // CIRCLE
	        sheet.setColumnWidth(2, 2500); // Revd
	        sheet.setColumnWidth(3, 2500); // Comp
	        sheet.setColumnWidth(4, 2500); // Pend
	        
	        // Merge title and date cells
	        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
	        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));

	        // Create headers
	        Row headerRow = sheet.createRow(2);
	        Row subHeaderRow = sheet.createRow(3);

	        // S.No header
	        Cell snoHeader = headerRow.createCell(0);
	        snoHeader.setCellValue("S.No");
	        snoHeader.setCellStyle(headerStyle);
	        sheet.addMergedRegion(new CellRangeAddress(2, 3, 0, 0));
	        
	        // Circle header
	        Cell circleHeader = headerRow.createCell(1);
	        circleHeader.setCellValue("CIRCLE");
	        circleHeader.setCellStyle(headerStyle);
	        sheet.addMergedRegion(new CellRangeAddress(2, 3, 1, 1));

	        // Mode header based on role
	        String mode =(callCenterValueBean!=null && callCenterValueBean.getRoleId() == 3) ? "SOCIAL MEDIA " : "MINNAGAM";
	        Cell modeHeader = headerRow.createCell(2);
	        modeHeader.setCellValue(mode);
	        modeHeader.setCellStyle(headerStyle);
	        sheet.addMergedRegion(new CellRangeAddress(2, 2, 2, 4));

	        // Sub headers
	        String[] subColumns = {"Revd", "Comp", "Pend"};
	        for (int i = 0; i < subColumns.length; i++) {
	            Cell subHeader = subHeaderRow.createCell(2 + i);
	            subHeader.setCellValue(subColumns[i]);
	            subHeader.setCellStyle(headerStyle);
	        }

	        // Add data rows
	        int rowNum = 4;
	        BigDecimal[] totals = new BigDecimal[3];
	        Arrays.fill(totals, BigDecimal.ZERO);

	        int serialNumber = 1;
	        for (ModeWiseAbstractValueBean report : reports) {
	            Row row = sheet.createRow(rowNum++);

	            // S.No
	            Cell snoCell = row.createCell(0);
	            snoCell.setCellValue(serialNumber++);
	            snoCell.setCellStyle(dataStyle);
	            
	            // Circle
	            Cell circleCell = row.createCell(1);
	            circleCell.setCellValue(report.getCircleName());
	            circleCell.setCellStyle(dataStyle);

	            // Get data based on role
	            BigDecimal revd, comp, pend;
	            if (callCenterValueBean!=null  &&  callCenterValueBean.getRoleId() == 3) {
	                revd = report.getSmsTotal(); 
	                comp = report.getSmsComp();
	                pend = report.getSmsPending();
	            } else { // role 7
	                revd = report.getMiTotal();
	                comp = report.getMiComp();
	                pend = report.getMiPending();
	            }

	            // Add data cells
	            Cell revdCell = row.createCell(2);
	            revdCell.setCellValue(revd.doubleValue());
	            revdCell.setCellStyle(dataStyle);
	            totals[0] = totals[0].add(revd);

	            Cell compCell = row.createCell(3);
	            compCell.setCellValue(comp.doubleValue());
	            compCell.setCellStyle(dataStyle);
	            totals[1] = totals[1].add(comp);

	            Cell pendCell = row.createCell(4);
	            pendCell.setCellValue(pend.doubleValue());
	            pendCell.setCellStyle(dataStyle);
	            totals[2] = totals[2].add(pend);
	        }

	        // Add total row
	        Row totalRow = sheet.createRow(rowNum);
	        totalRow.createCell(0).setCellValue("TOTAL");
	        totalRow.getCell(0).setCellStyle(totalStyle);
	        totalRow.createCell(1).setCellValue("");
	        totalRow.getCell(1).setCellStyle(totalStyle);

	        for (int i = 0; i < 3; i++) {
	            Cell cell = totalRow.createCell(2 + i);
	            cell.setCellValue(totals[i].doubleValue());
	            cell.setCellStyle(totalStyle);
	        }
	    } else {
	        // FULL VIEW FOR OTHER ROLES
	        sheet.setColumnWidth(0, 1500); // S.NO
	        sheet.setColumnWidth(1, 4000); // CIRCLE
	        for (int i = 2; i <= 19; i++) {
	            sheet.setColumnWidth(i, 2500);
	        }

	        // Merge title and date cells
	        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 19));
	        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 19));

	        // Create headers
	        Row headerRow = sheet.createRow(2);
	        Row subHeaderRow = sheet.createRow(3);

	        // S.No header
	        Cell snoHeader = headerRow.createCell(0);
	        snoHeader.setCellValue("S.No");
	        snoHeader.setCellStyle(headerStyle);
	        sheet.addMergedRegion(new CellRangeAddress(2, 3, 0, 0));
	        
	        // Circle header
	        Cell circleHeader = headerRow.createCell(1);
	        circleHeader.setCellValue("CIRCLE");
	        circleHeader.setCellStyle(headerStyle);
	        sheet.addMergedRegion(new CellRangeAddress(2, 3, 1, 1));

	        // Mode headers
	        String[] modes = {"WEB", "MOBILE APP", "FOC", "SM", "MI", "TOTAL"};
	        String[] subColumns = {"Revd", "Comp", "Pend"};

	        int colIndex = 2;
	        for (String mode : modes) {
	            Cell modeHeader = headerRow.createCell(colIndex);
	            modeHeader.setCellValue(mode);
	            modeHeader.setCellStyle(headerStyle);
	            sheet.addMergedRegion(new CellRangeAddress(2, 2, colIndex, colIndex + 2));

	            for (String subCol : subColumns) {
	                Cell subHeader = subHeaderRow.createCell(colIndex);
	                subHeader.setCellValue(subCol);
	                subHeader.setCellStyle(headerStyle);
	                colIndex++;
	            }
	        }

	        // Add data rows
	        int rowNum = 4;
	        BigDecimal[][] totals = new BigDecimal[6][3];
	        for (int i = 0; i < 6; i++) {
	            Arrays.fill(totals[i], BigDecimal.ZERO);
	        }

	        int serialNumber = 1;
	        for (ModeWiseAbstractValueBean report : reports) {
	            Row row = sheet.createRow(rowNum++);

	            // S.No
	            row.createCell(0).setCellValue(serialNumber++);
	            row.getCell(0).setCellStyle(dataStyle);
	            
	            // Circle
	            row.createCell(1).setCellValue(report.getCircleName());
	            row.getCell(1).setCellStyle(dataStyle);

	            addDataCells(row, 2, report.getWebTotal(), report.getWebComp(), report.getWebPending(), totals[0], dataStyle);
	            addDataCells(row, 5, report.getMobileTotal(), report.getMobileComp(), report.getMobilePending(), totals[1], dataStyle);
	            addDataCells(row, 8, report.getAdminTotal(), report.getAdminComp(), report.getAdminPending(), totals[2], dataStyle);
	            addDataCells(row, 11, report.getSmsTotal(), report.getSmsComp(), report.getSmsPending(), totals[3], dataStyle);
	            addDataCells(row, 14, report.getMiTotal(), report.getMiComp(), report.getMiPending(), totals[4], dataStyle);

	            // Calculate and add totals
	            BigDecimal totalRevd = report.getWebTotal().add(report.getMobileTotal())
	                .add(report.getAdminTotal()).add(report.getSmsTotal()).add(report.getMiTotal());
	            BigDecimal totalComp = report.getWebComp().add(report.getMobileComp())
	                .add(report.getAdminComp()).add(report.getSmsComp()).add(report.getMiComp());
	            BigDecimal totalPend = report.getWebPending().add(report.getMobilePending())
	                .add(report.getAdminPending()).add(report.getSmsPending()).add(report.getMiPending());
	            addDataCells(row, 17, totalRevd, totalComp, totalPend, totals[5], dataStyle);
	        }

	        // Add total row
	        Row totalRow = sheet.createRow(rowNum);
	        totalRow.createCell(0).setCellValue("TOTAL");
	        totalRow.getCell(0).setCellStyle(totalStyle);
	        totalRow.createCell(1).setCellValue(""); 
	        totalRow.getCell(1).setCellStyle(totalStyle);

	        for (int mode = 0; mode < 6; mode++) {
	            for (int subCol = 0; subCol < 3; subCol++) {
	                int col = 2 + (mode * 3) + subCol;
	                Cell cell = totalRow.createCell(col);
	                cell.setCellValue(totals[mode][subCol].doubleValue());
	                cell.setCellStyle(totalStyle);
	            }
	        }
	    }

	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    workbook.write(outputStream);
	    workbook.close();

	    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
	    excelFile = DefaultStreamedContent.builder()
	            .name("ModeWise_Complaints_Abstract_Report-Circle_Wise.xls")
	            .contentType("application/vnd.ms-excel")
	            .stream(() -> inputStream)
	            .build();
	}



	public void exportSectionToExcelFormatOne(List<ModeWiseAbstractValueBean> reports) throws IOException {
	    // Get session information
	    HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
	    CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");
        AdminUserValueBean adminUserValueBean = (AdminUserValueBean) httpsession.getAttribute("sessionAdminValueBean");

	    // Create workbook and sheet
	    HSSFWorkbook workbook = new HSSFWorkbook();
	    Sheet sheet = workbook.createSheet("ModeWise_Complaints_Abstract_Report-Section_Wise");

	    // Create styles (same as before)
	    CellStyle titleStyle = workbook.createCellStyle();
	    HSSFFont titleFont = workbook.createFont();
	    titleFont.setBold(true);
	    titleFont.setFontHeightInPoints((short) 12);
	    titleStyle.setFont(titleFont);
	    titleStyle.setAlignment(HorizontalAlignment.CENTER);
	    titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

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

	    CellStyle subHeaderStyle = workbook.createCellStyle();
	    HSSFFont dateFont = workbook.createFont();
	    dateFont.setBold(true);
	    dateFont.setFontHeightInPoints((short) 10);
	    subHeaderStyle.setFont(dateFont);
	    subHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
	    subHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

	    CellStyle dataStyle = workbook.createCellStyle();
	    dataStyle.setAlignment(HorizontalAlignment.LEFT);
	    dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
	    dataStyle.setBorderBottom(BorderStyle.THIN);
	    dataStyle.setBorderTop(BorderStyle.THIN);
	    dataStyle.setBorderLeft(BorderStyle.THIN);
	    dataStyle.setBorderRight(BorderStyle.THIN);

	    CellStyle totalStyle = workbook.createCellStyle();
	    HSSFFont totalFont = workbook.createFont();
	    totalFont.setBold(true);
	    totalStyle.setFont(totalFont);
	    totalStyle.setAlignment(HorizontalAlignment.LEFT);
	    totalStyle.setVerticalAlignment(VerticalAlignment.CENTER);
	    totalStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	    totalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	    totalStyle.setBorderBottom(BorderStyle.THIN);
	    totalStyle.setBorderTop(BorderStyle.THIN);
	    totalStyle.setBorderLeft(BorderStyle.THIN);
	    totalStyle.setBorderRight(BorderStyle.THIN);

	    // Title row
	    Row titleRow = sheet.createRow(0);
	    Cell titleCell = titleRow.createCell(0);
	    titleCell.setCellValue("MODE WISE COMPLAINTS ABSTRACT REPORT - SECTION WISE");
	    titleCell.setCellStyle(titleStyle);

	    // Date row
	    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	    String fromDateStr = dmFilter.getFromDate() != null ? dateFormat.format(dmFilter.getFromDate()) : "N/A";
	    String toDateStr = dmFilter.getToDate() != null ? dateFormat.format(dmFilter.getToDate()) : "N/A";
	    Row dateRow = sheet.createRow(1);
	    Cell dateCell = dateRow.createCell(0);
	    dateCell.setCellValue("From Date: " + fromDateStr + "  To Date: " + toDateStr);
	    dateCell.setCellStyle(subHeaderStyle);

	    // Check for call center roles 3 or 7
	    boolean isSimplifiedView = (callCenterValueBean != null && 
                (callCenterValueBean.getRoleId() == 3 || callCenterValueBean.getRoleId() == 7 )) || (adminUserValueBean!=null && adminUserValueBean.getRoleId()==10 );

	    if (isSimplifiedView) {
	        // SIMPLIFIED VIEW FOR ROLES 3 AND 7
	        sheet.setColumnWidth(0, 1500); // S.NO
	        sheet.setColumnWidth(1, 6000); // SECTION
	        sheet.setColumnWidth(2, 6000); // DIVISION
	        sheet.setColumnWidth(3, 2500); // Revd
	        sheet.setColumnWidth(4, 2500); // Comp
	        sheet.setColumnWidth(5, 2500); // Pend
	        
	        // Merge title and date cells
	        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
	        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

	        // Create headers
	        Row headerRow = sheet.createRow(2);
	        Row subHeaderRow = sheet.createRow(3);

	        // S.No header
	        Cell snoHeader = headerRow.createCell(0);
	        snoHeader.setCellValue("S.No");
	        snoHeader.setCellStyle(headerStyle);
	        sheet.addMergedRegion(new CellRangeAddress(2, 3, 0, 0));
	        
	        // Section header
	        Cell sectionHeader = headerRow.createCell(1);
	        sectionHeader.setCellValue("SECTION");
	        sectionHeader.setCellStyle(headerStyle);
	        sheet.addMergedRegion(new CellRangeAddress(2, 3, 1, 1));

	        // Division header
	        Cell divisionHeader = headerRow.createCell(2);
	        divisionHeader.setCellValue("DIVISION");
	        divisionHeader.setCellStyle(headerStyle);
	        sheet.addMergedRegion(new CellRangeAddress(2, 3, 2, 2));

	        // Mode header based on role
	        String mode = (callCenterValueBean!=null && callCenterValueBean.getRoleId() == 3) ? "SOCIAL MEDIA " : "MINNAGAM";
	        Cell modeHeader = headerRow.createCell(3);
	        modeHeader.setCellValue(mode);
	        modeHeader.setCellStyle(headerStyle);
	        sheet.addMergedRegion(new CellRangeAddress(2, 2, 3, 5));

	        // Sub headers
	        String[] subColumns = {"Revd", "Comp", "Pend"};
	        for (int i = 0; i < subColumns.length; i++) {
	            Cell subHeader = subHeaderRow.createCell(3 + i);
	            subHeader.setCellValue(subColumns[i]);
	            subHeader.setCellStyle(headerStyle);
	        }

	        // Add data rows
	        int rowNum = 4;
	        BigDecimal[] totals = new BigDecimal[3];
	        Arrays.fill(totals, BigDecimal.ZERO);

	        int serialNumber = 1;
	        for (ModeWiseAbstractValueBean report : reports) {
	            Row row = sheet.createRow(rowNum++);

	            // S.No
	            row.createCell(0).setCellValue(serialNumber++);
	            row.getCell(0).setCellStyle(dataStyle);
	            
	            // Section
	            row.createCell(1).setCellValue(report.getSectionName());
	            row.getCell(1).setCellStyle(dataStyle);

	            // Division
	            row.createCell(2).setCellValue(report.getDivisionName());
	            row.getCell(2).setCellStyle(dataStyle);

	            // Get data based on role
	            BigDecimal revd, comp, pend;
	            if (callCenterValueBean!=null  &&  callCenterValueBean.getRoleId() == 3) {
	                revd = report.getSmsTotal(); 
	                comp = report.getSmsComp();
	                pend = report.getSmsPending();
	            } else { // role 7
	                revd = report.getMiTotal();
	                comp = report.getMiComp();
	                pend = report.getMiPending();
	            }

	            // Add data cells
	            Cell revdCell = row.createCell(3);
	            revdCell.setCellValue(revd.doubleValue());
	            revdCell.setCellStyle(dataStyle);
	            totals[0] = totals[0].add(revd);

	            Cell compCell = row.createCell(4);
	            compCell.setCellValue(comp.doubleValue());
	            compCell.setCellStyle(dataStyle);
	            totals[1] = totals[1].add(comp);

	            Cell pendCell = row.createCell(5);
	            pendCell.setCellValue(pend.doubleValue());
	            pendCell.setCellStyle(dataStyle);
	            totals[2] = totals[2].add(pend);
	        }

	        // Add total row
	        Row totalRow = sheet.createRow(rowNum);
	        totalRow.createCell(0).setCellValue("");
	        totalRow.getCell(0).setCellStyle(totalStyle);
	        totalRow.createCell(1).setCellValue("");
	        totalRow.getCell(1).setCellStyle(totalStyle);
	        totalRow.createCell(2).setCellValue("TOTAL");
	        totalRow.getCell(2).setCellStyle(totalStyle);

	        for (int i = 0; i < 3; i++) {
	            Cell cell = totalRow.createCell(3 + i);
	            cell.setCellValue(totals[i].doubleValue());
	            cell.setCellStyle(totalStyle);
	        }
	    } else {
	        // FULL VIEW FOR OTHER ROLES (original implementation)
	        sheet.setColumnWidth(0, 1500); // S.No
	        sheet.setColumnWidth(1, 6000); // SECTION
	        sheet.setColumnWidth(2, 6000); // DIVISION
	        for (int i = 3; i <= 20; i++) {
	            sheet.setColumnWidth(i, 2500);
	        }

	        // Merge title and date cells
	        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 20));
	        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 20));

	        // Create headers
	        Row headerRow = sheet.createRow(2);
	        Row subHeaderRow = sheet.createRow(3);

	        // S.No header
	        Cell snoHeader = headerRow.createCell(0);
	        snoHeader.setCellValue("S.No");
	        snoHeader.setCellStyle(headerStyle);
	        sheet.addMergedRegion(new CellRangeAddress(2, 3, 0, 0));
	        
	        // Section header
	        Cell sectionHeader = headerRow.createCell(1);
	        sectionHeader.setCellValue("SECTION");
	        sectionHeader.setCellStyle(headerStyle);
	        sheet.addMergedRegion(new CellRangeAddress(2, 3, 1, 1));

	        // Division header
	        Cell divisionHeader = headerRow.createCell(2);
	        divisionHeader.setCellValue("DIVISION");
	        divisionHeader.setCellStyle(headerStyle);
	        sheet.addMergedRegion(new CellRangeAddress(2, 3, 2, 2));

	        // Mode headers
	        String[] modes = {"WEB", "MOBILE APP", "FOC", "SM", "MI", "TOTAL"};
	        String[] subColumns = {"Revd", "Comp", "Pend"};

	        int colIndex = 3;
	        for (String mode : modes) {
	            Cell modeHeader = headerRow.createCell(colIndex);
	            modeHeader.setCellValue(mode);
	            modeHeader.setCellStyle(headerStyle);
	            sheet.addMergedRegion(new CellRangeAddress(2, 2, colIndex, colIndex + 2));

	            for (String subCol : subColumns) {
	                Cell subHeader = subHeaderRow.createCell(colIndex);
	                subHeader.setCellValue(subCol);
	                subHeader.setCellStyle(headerStyle);
	                colIndex++;
	            }
	        }

	        // Add data rows
	        int rowNum = 4;
	        BigDecimal[][] totals = new BigDecimal[6][3];
	        for (int i = 0; i < 6; i++) {
	            Arrays.fill(totals[i], BigDecimal.ZERO);
	        }

	        int serialNumber = 1;
	        for (ModeWiseAbstractValueBean report : reports) {
	            Row row = sheet.createRow(rowNum++);

	            // S.No
	            row.createCell(0).setCellValue(serialNumber++);
	            row.getCell(0).setCellStyle(dataStyle);
	            
	            // Section
	            row.createCell(1).setCellValue(report.getSectionName());
	            row.getCell(1).setCellStyle(dataStyle);

	            // Division
	            row.createCell(2).setCellValue(report.getDivisionName());
	            row.getCell(2).setCellStyle(dataStyle);

	            // Add mode data
	            addDataCells(row, 3, report.getWebTotal(), report.getWebComp(), report.getWebPending(), totals[0], dataStyle);
	            addDataCells(row, 6, report.getMobileTotal(), report.getMobileComp(), report.getMobilePending(), totals[1], dataStyle);
	            addDataCells(row, 9, report.getAdminTotal(), report.getAdminComp(), report.getAdminPending(), totals[2], dataStyle);
	            addDataCells(row, 12, report.getSmsTotal(), report.getSmsComp(), report.getSmsPending(), totals[3], dataStyle);
	            addDataCells(row, 15, report.getMiTotal(), report.getMiComp(), report.getMiPending(), totals[4], dataStyle);

	            // Calculate and add totals
	            BigDecimal totalRevd = report.getWebTotal().add(report.getMobileTotal())
	                .add(report.getAdminTotal()).add(report.getSmsTotal()).add(report.getMiTotal());
	            BigDecimal totalComp = report.getWebComp().add(report.getMobileComp())
	                .add(report.getAdminComp()).add(report.getSmsComp()).add(report.getMiComp());
	            BigDecimal totalPend = report.getWebPending().add(report.getMobilePending())
	                .add(report.getAdminPending()).add(report.getSmsPending()).add(report.getMiPending());
	            addDataCells(row, 18, totalRevd, totalComp, totalPend, totals[5], dataStyle);
	        }

	        // Add total row
	        Row totalRow = sheet.createRow(rowNum);
	        totalRow.createCell(0).setCellValue("");
	        totalRow.getCell(0).setCellStyle(totalStyle);
	        totalRow.createCell(1).setCellValue("");
	        totalRow.getCell(1).setCellStyle(totalStyle);
	        totalRow.createCell(2).setCellValue("TOTAL");
	        totalRow.getCell(2).setCellStyle(totalStyle);

	        for (int mode = 0; mode < 6; mode++) {
	            for (int subCol = 0; subCol < 3; subCol++) {
	                int col = 3 + (mode * 3) + subCol;
	                Cell cell = totalRow.createCell(col);
	                cell.setCellValue(totals[mode][subCol].doubleValue());
	                cell.setCellStyle(totalStyle);
	            }
	        }
	    }

	    // Create and return the Excel file
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    workbook.write(outputStream);
	    workbook.close();

	    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
	    excelFile = DefaultStreamedContent.builder()
	            .name("ModeWise_Complaints_Abstract_Report-Section_Wise.xls")
	            .contentType("application/vnd.ms-excel")
	            .stream(() -> inputStream)
	            .build();
	}

	public void exportCircleToPdfFormatOne(List<ModeWiseAbstractValueBean> reports) throws IOException, DocumentException {
	    // Get session information
	    HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
	    CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");

	    // Set document size based on role
	    boolean isSimplifiedView = (callCenterValueBean != null && 
                (callCenterValueBean.getRoleId() == 3 || callCenterValueBean.getRoleId() == 7 )) || (adminUserValueBean!=null && adminUserValueBean.getRoleId()==10 );
	    Document document = new Document(isSimplifiedView ? PageSize.A4 : PageSize.A2.rotate());
	    
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    PdfWriter.getInstance(document, outputStream);
	    document.open();

	    // Font Definitions
	    Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
	    Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY);
	    Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
	    Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);
	    Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
	    Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.BOLD);

	    // Title
	    Paragraph title = new Paragraph("MODE WISE COMPLAINTS ABSTRACT REPORT - CIRCLE WISE", titleFont);
	    title.setAlignment(Element.ALIGN_CENTER);
	    title.setSpacingAfter(10f);
	    document.add(title);



	    if (isSimplifiedView) {
	        // SIMPLIFIED VIEW FOR ROLES 3 AND 7
	        PdfPTable table = new PdfPTable(5); // S.No + Circle + 3 columns
	        table.setWidthPercentage(100);
	        table.setSpacingBefore(10f);
	        table.setSpacingAfter(10f);

	        // Column widths
	        float[] columnWidths = {4f, 8f, 4f, 4f, 4f};
	        table.setWidths(columnWidths);

	        // Headers
	        PdfPCell snoHeader = new PdfPCell(new Phrase("S.No", headerFont));
	        styleHeaderCell(snoHeader);
	        snoHeader.setRowspan(2);
	        table.addCell(snoHeader);

	        PdfPCell circleHeader = new PdfPCell(new Phrase("CIRCLE", headerFont));
	        styleHeaderCell(circleHeader);
	        circleHeader.setRowspan(2);
	        table.addCell(circleHeader);

	        String mode = (callCenterValueBean!=null && callCenterValueBean.getRoleId() == 3) ? "SOCIAL MEDIA " : "MINNAGAM";
	        PdfPCell modeHeader = new PdfPCell(new Phrase(mode, headerFont));
	        styleHeaderCell(modeHeader);
	        modeHeader.setColspan(3);
	        table.addCell(modeHeader);

	        // Sub headers
	        String[] subColumns = {"Revd", "Comp", "Pend"};
	        for (String subCol : subColumns) {
	            PdfPCell subHeader = new PdfPCell(new Phrase(subCol, subHeaderFont));
	            styleSubHeaderCell(subHeader);
	            table.addCell(subHeader);
	        }

	        // Data rows
	        BigDecimal[] totals = new BigDecimal[3];
	        Arrays.fill(totals, BigDecimal.ZERO);
	        int serialNumber = 1;

	        for (ModeWiseAbstractValueBean report : reports) {
	            // S.No
	            PdfPCell snoCell = new PdfPCell(new Phrase(String.valueOf(serialNumber++), dataFont));
	            styleDataCell(snoCell, Element.ALIGN_CENTER);
	            table.addCell(snoCell);

	            // Circle name
	            PdfPCell circleCell = new PdfPCell(new Phrase(report.getCircleName(), dataFont));
	            styleDataCell(circleCell, Element.ALIGN_LEFT);
	            table.addCell(circleCell);

	            // Get data based on role
	            BigDecimal revd, comp, pend;
	            if (callCenterValueBean!=null  &&  callCenterValueBean.getRoleId() == 3) {
	                revd = report.getSmsTotal();
	                comp = report.getSmsComp();
	                pend = report.getSmsPending();
	            } else { // role 7
	                revd = report.getMiTotal();
	                comp = report.getMiComp();
	                pend = report.getMiPending();
	            }

	            // Add data cells
	            addSimplifiedPdfDataCells(table, revd, comp, pend, totals, dataFont);
	        }

	        // Total row
	        addSimplifiedTotalRow(table, totals, totalFont);

	        document.add(table);
	    } else {
	        // FULL VIEW FOR OTHER ROLES
	        PdfPTable table = new PdfPTable(20);
	        table.setWidthPercentage(100);
	        table.setSpacingBefore(10f);
	        table.setSpacingAfter(10f);

	        float[] columnWidths = {
	            4f, // S.No    
	            4f, // CIRCLE
	            2.5f, 2.5f, 2.5f,  // WEB
	            2.5f, 2.5f, 2.5f,  // MOBILE APP
	            2.5f, 2.5f, 2.5f,  // FOC
	            2.5f, 2.5f, 2.5f,  // SMS
	            2.5f, 2.5f, 2.5f,  // MI
	            2.5f, 2.5f, 2.5f   // TOTAL
	        };
	        table.setWidths(columnWidths);
	        
	        // Headers
	        PdfPCell snoHeader = new PdfPCell(new Phrase("S.No", headerFont));
	        styleHeaderCell(snoHeader);
	        snoHeader.setRowspan(2);
	        table.addCell(snoHeader);

	        PdfPCell circleHeader = new PdfPCell(new Phrase("CIRCLE", headerFont));
	        styleHeaderCell(circleHeader);
	        circleHeader.setRowspan(2);
	        table.addCell(circleHeader);

	        // Mode headers
	        String[] modes = {"WEB", "MOBILE APP", "FOC", "SM", "MI", "TOTAL"};
	        for (String mode : modes) {
	            PdfPCell modeHeader = new PdfPCell(new Phrase(mode, headerFont));
	            styleHeaderCell(modeHeader);
	            modeHeader.setColspan(3);
	            table.addCell(modeHeader);
	        }

	        // Sub headers
	        String[] subColumns = {"Revd", "Comp", "Pend"};
	        for (int i = 0; i < 6; i++) {
	            for (String subCol : subColumns) {
	                PdfPCell subHeader = new PdfPCell(new Phrase(subCol, subHeaderFont));
	                styleSubHeaderCell(subHeader);
	                table.addCell(subHeader);
	            }
	        }

	        // Data rows
	        BigDecimal[][] totals = new BigDecimal[6][3];
	        for (int i = 0; i < 6; i++) {
	            Arrays.fill(totals[i], BigDecimal.ZERO);
	        }
	        int serialNumber = 1;

	        for (ModeWiseAbstractValueBean report : reports) {
	            // S.No
	            PdfPCell snoCell = new PdfPCell(new Phrase(String.valueOf(serialNumber++), dataFont));
	            styleDataCell(snoCell, Element.ALIGN_CENTER);
	            table.addCell(snoCell);
	            
	            // Circle name
	            PdfPCell circleCell = new PdfPCell(new Phrase(report.getCircleName(), dataFont));
	            styleDataCell(circleCell, Element.ALIGN_LEFT);
	            table.addCell(circleCell);

	            // Add mode data
	            addPdfDataCells(table, report.getWebTotal(), report.getWebComp(), report.getWebPending(), totals[0], dataFont);
	            addPdfDataCells(table, report.getMobileTotal(), report.getMobileComp(), report.getMobilePending(), totals[1], dataFont);
	            addPdfDataCells(table, report.getAdminTotal(), report.getAdminComp(), report.getAdminPending(), totals[2], dataFont);
	            addPdfDataCells(table, report.getSmsTotal(), report.getSmsComp(), report.getSmsPending(), totals[3], dataFont);
	            addPdfDataCells(table, report.getMiTotal(), report.getMiComp(), report.getMiPending(), totals[4], dataFont);

	            // Calculate and add totals
	            BigDecimal totalRevd = report.getWebTotal().add(report.getMobileTotal())
	                .add(report.getAdminTotal()).add(report.getSmsTotal()).add(report.getMiTotal());
	            BigDecimal totalComp = report.getWebComp().add(report.getMobileComp())
	                .add(report.getAdminComp()).add(report.getSmsComp()).add(report.getMiComp());
	            BigDecimal totalPend = report.getWebPending().add(report.getMobilePending())
	                .add(report.getAdminPending()).add(report.getSmsPending()).add(report.getMiPending());
	            addPdfDataCells(table, totalRevd, totalComp, totalPend, totals[5], dataFont);
	        }

	        // Total row
	        PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL", totalFont));
	        styleTotalCell(totalLabelCell, Element.ALIGN_RIGHT);
	        totalLabelCell.setColspan(2);
	        table.addCell(totalLabelCell);

	        for (int mode = 0; mode < 6; mode++) {
	            for (int subCol = 0; subCol < 3; subCol++) {
	                PdfPCell totalCell = new PdfPCell(new Phrase(totals[mode][subCol].toString(), totalFont));
	                styleTotalCell(totalCell, Element.ALIGN_CENTER);
	                table.addCell(totalCell);
	            }
	        }

	        document.add(table);
	    }

	    document.close();

	    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
	    pdfFile = DefaultStreamedContent.builder()
	            .name("ModeWise_Complaints_Abstract_Report-Circle_Wise.pdf")
	            .contentType("application/pdf")
	            .stream(() -> inputStream)
	            .build();
	}

	// Helper methods for cell styling
	private void styleHeaderCell(PdfPCell cell) {
	    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
	    cell.setPadding(5);
	}

	private void styleSubHeaderCell(PdfPCell cell) {
	    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	    cell.setBackgroundColor(new BaseColor(220, 220, 220));
	    cell.setPadding(5);
	}

	private void styleDataCell(PdfPCell cell, int alignment) {
	    cell.setHorizontalAlignment(alignment);
	    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	    cell.setPadding(5);
	}

	private void styleTotalCell(PdfPCell cell, int alignment) {
	    cell.setHorizontalAlignment(alignment);
	    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
	    cell.setPadding(5);
	}

	// Helper method for adding data cells (full view)
	private void addPdfDataCells(PdfPTable table, BigDecimal revd, BigDecimal comp, BigDecimal pend,
	        BigDecimal[] modeTotals, Font font) {
	    PdfPCell revdCell = new PdfPCell(new Phrase(revd.toString(), font));
	    styleDataCell(revdCell, Element.ALIGN_CENTER);
	    table.addCell(revdCell);
	    modeTotals[0] = modeTotals[0].add(revd);

	    PdfPCell compCell = new PdfPCell(new Phrase(comp.toString(), font));
	    styleDataCell(compCell, Element.ALIGN_CENTER);
	    table.addCell(compCell);
	    modeTotals[1] = modeTotals[1].add(comp);

	    PdfPCell pendCell = new PdfPCell(new Phrase(pend.toString(), font));
	    styleDataCell(pendCell, Element.ALIGN_CENTER);
	    table.addCell(pendCell);
	    modeTotals[2] = modeTotals[2].add(pend);
	}

	// Helper method for adding data cells (simplified view)
	private void addSimplifiedPdfDataCells(PdfPTable table, BigDecimal revd, BigDecimal comp, 
	                                     BigDecimal pend, BigDecimal[] totals, Font font) {
	    PdfPCell revdCell = new PdfPCell(new Phrase(revd.toString(), font));
	    styleDataCell(revdCell, Element.ALIGN_CENTER);
	    table.addCell(revdCell);
	    totals[0] = totals[0].add(revd);

	    PdfPCell compCell = new PdfPCell(new Phrase(comp.toString(), font));
	    styleDataCell(compCell, Element.ALIGN_CENTER);
	    table.addCell(compCell);
	    totals[1] = totals[1].add(comp);

	    PdfPCell pendCell = new PdfPCell(new Phrase(pend.toString(), font));
	    styleDataCell(pendCell, Element.ALIGN_CENTER);
	    table.addCell(pendCell);
	    totals[2] = totals[2].add(pend);
	}

	// Helper method for adding total row (simplified view)
	private void addSimplifiedTotalRow(PdfPTable table, BigDecimal[] totals, Font font) {
	    PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL", font));
	    styleTotalCell(totalLabelCell, Element.ALIGN_RIGHT);
	    totalLabelCell.setColspan(2);
	    table.addCell(totalLabelCell);

	    for (int i = 0; i < 3; i++) {
	        PdfPCell totalCell = new PdfPCell(new Phrase(totals[i].toString(), font));
	        styleTotalCell(totalCell, Element.ALIGN_CENTER);
	        table.addCell(totalCell);
	    }
	}

	// SECTION REPORT PDF DOWNLOAD - FORMAT ONE - MODE WISE
	public void exportSectionToPdfFormatOne(List<ModeWiseAbstractValueBean> reports) {
	    // Get session information
	    HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
	    CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");

	    // Set document size based on role
	    boolean isSimplifiedView = (callCenterValueBean != null && 
                (callCenterValueBean.getRoleId() == 3 || callCenterValueBean.getRoleId() == 7 )) || (adminUserValueBean!=null && adminUserValueBean.getRoleId()==10 );
	    Document document = new Document(isSimplifiedView ? PageSize.A4 : PageSize.A2.rotate());
	    
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	    try {
	        PdfWriter.getInstance(document, outputStream);
	        document.open();

	        // Font Definitions
	        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
	        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY);
	        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
	        Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.BOLD, BaseColor.BLACK);
	        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
	        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.BLACK);

	        // Title
	        Paragraph title = new Paragraph("MODE WISE COMPLAINTS ABSTRACT REPORT - SECTION WISE", titleFont);
	        title.setAlignment(Element.ALIGN_CENTER);
	        title.setSpacingAfter(10);
	        document.add(title);

	      

	        if (isSimplifiedView) {
	            // SIMPLIFIED VIEW FOR ROLES 3 AND 7
	            PdfPTable table = new PdfPTable(6); // S.No + Section + Division + 3 columns
	            table.setWidthPercentage(100);
	            table.setSpacingBefore(10);
	            table.setSpacingAfter(10);

	            // Column widths
	            float[] columnWidths = {4f, 6f, 6f, 4f, 4f, 4f};
	            table.setWidths(columnWidths);

	            // Headers
	            PdfPCell snoHeader = new PdfPCell(new Phrase("S.No", headerFont));
	            styleHeaderCell(snoHeader);
	            snoHeader.setRowspan(2);
	            table.addCell(snoHeader);

	            PdfPCell sectionHeader = new PdfPCell(new Phrase("SECTION", headerFont));
	            styleHeaderCell(sectionHeader);
	            sectionHeader.setRowspan(2);
	            table.addCell(sectionHeader);

	            PdfPCell divisionHeader = new PdfPCell(new Phrase("DIVISION", headerFont));
	            styleHeaderCell(divisionHeader);
	            divisionHeader.setRowspan(2);
	            table.addCell(divisionHeader);

	            String mode = (callCenterValueBean!=null && callCenterValueBean.getRoleId() == 3) ? "SOCIAL MEDIA " : "MINNAGAM";
	            PdfPCell modeHeader = new PdfPCell(new Phrase(mode, headerFont));
	            styleHeaderCell(modeHeader);
	            modeHeader.setColspan(3);
	            table.addCell(modeHeader);

	            // Sub headers
	            String[] subColumns = {"Revd", "Comp", "Pend"};
	            for (String subCol : subColumns) {
	                PdfPCell subHeader = new PdfPCell(new Phrase(subCol, subHeaderFont));
	                styleSubHeaderCell(subHeader);
	                table.addCell(subHeader);
	            }

	            // Data rows
	            BigDecimal[] totals = new BigDecimal[3];
	            Arrays.fill(totals, BigDecimal.ZERO);
	            int serialNumber = 1;

	            for (ModeWiseAbstractValueBean report : reports) {
	                // S.No
	                PdfPCell snoCell = new PdfPCell(new Phrase(String.valueOf(serialNumber++), dataFont));
	                styleDataCell(snoCell, Element.ALIGN_CENTER);
	                table.addCell(snoCell);

	                // Section
	                PdfPCell sectionCell = new PdfPCell(new Phrase(report.getSectionName(), dataFont));
	                styleDataCell(sectionCell, Element.ALIGN_LEFT);
	                table.addCell(sectionCell);

	                // Division
	                PdfPCell divisionCell = new PdfPCell(new Phrase(report.getDivisionName(), dataFont));
	                styleDataCell(divisionCell, Element.ALIGN_LEFT);
	                table.addCell(divisionCell);

	                // Get data based on role
	                BigDecimal revd, comp, pend;
		            if (callCenterValueBean!=null  &&  callCenterValueBean.getRoleId() == 3) {
	                    revd = report.getSmsTotal();
	                    comp = report.getSmsComp();
	                    pend = report.getSmsPending();
	                } else { // role 7
	                    revd = report.getMiTotal();
	                    comp = report.getMiComp();
	                    pend = report.getMiPending();
	                }

	                // Add data cells
	                addSimplifiedPdfDataCells(table, revd, comp, pend, totals, dataFont);
	            }

	            // Total row
	            PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL", totalFont));
	            styleTotalCell(totalLabelCell, Element.ALIGN_RIGHT);
	            totalLabelCell.setColspan(3);
	            table.addCell(totalLabelCell);

	            for (int i = 0; i < 3; i++) {
	                PdfPCell totalCell = new PdfPCell(new Phrase(totals[i].toString(), totalFont));
	                styleTotalCell(totalCell, Element.ALIGN_CENTER);
	                table.addCell(totalCell);
	            }

	            document.add(table);
	        } else {
	            // FULL VIEW FOR OTHER ROLES
	            PdfPTable table = new PdfPTable(21);
	            table.setWidthPercentage(100);
	            table.setSpacingBefore(10);
	            table.setSpacingAfter(10);

	            // Column widths
	            float[] columnWidths = {
	                4f, // S.No    
	                4f, // SECTION
	                4f, // DIVISION
	                2.5f, 2.5f, 2.5f,  // WEB
	                2.5f, 2.5f, 2.5f,  // MOBILE APP
	                2.5f, 2.5f, 2.5f,  // FOC
	                2.5f, 2.5f, 2.5f,  // SMS
	                2.5f, 2.5f, 2.5f,  // MI
	                2.5f, 2.5f, 2.5f   // TOTAL
	            };
	            table.setWidths(columnWidths);

	            // Headers
	            PdfPCell snoHeader = new PdfPCell(new Phrase("S.No", headerFont));
	            styleHeaderCell(snoHeader);
	            snoHeader.setRowspan(2);
	            table.addCell(snoHeader);

	            PdfPCell sectionHeader = new PdfPCell(new Phrase("SECTION", headerFont));
	            styleHeaderCell(sectionHeader);
	            sectionHeader.setRowspan(2);
	            table.addCell(sectionHeader);

	            PdfPCell divisionHeader = new PdfPCell(new Phrase("DIVISION", headerFont));
	            styleHeaderCell(divisionHeader);
	            divisionHeader.setRowspan(2);
	            table.addCell(divisionHeader);

	            // Mode headers
	            String[] modes = {"WEB", "MOBILE APP", "FOC", "SM", "MI", "TOTAL"};
	            for (String mode : modes) {
	                PdfPCell modeHeader = new PdfPCell(new Phrase(mode, headerFont));
	                styleHeaderCell(modeHeader);
	                modeHeader.setColspan(3);
	                table.addCell(modeHeader);
	            }

	            // Sub headers
	            String[] subColumns = {"Revd", "Comp", "Pend"};
	            for (int i = 0; i < 6; i++) {
	                for (String subCol : subColumns) {
	                    PdfPCell subHeader = new PdfPCell(new Phrase(subCol, subHeaderFont));
	                    styleSubHeaderCell(subHeader);
	                    table.addCell(subHeader);
	                }
	            }

	            // Data rows
	            BigDecimal[][] totals = new BigDecimal[6][3];
	            for (int i = 0; i < 6; i++) {
	                Arrays.fill(totals[i], BigDecimal.ZERO);
	            }
	            int serialNumber = 1;

	            for (ModeWiseAbstractValueBean report : reports) {
	                // S.No
	                PdfPCell snoCell = new PdfPCell(new Phrase(String.valueOf(serialNumber++), dataFont));
	                styleDataCell(snoCell, Element.ALIGN_CENTER);
	                table.addCell(snoCell);
	                
	                // Section
	                PdfPCell sectionCell = new PdfPCell(new Phrase(report.getSectionName(), dataFont));
	                styleDataCell(sectionCell, Element.ALIGN_LEFT);
	                table.addCell(sectionCell);

	                // Division
	                PdfPCell divisionCell = new PdfPCell(new Phrase(report.getDivisionName(), dataFont));
	                styleDataCell(divisionCell, Element.ALIGN_LEFT);
	                table.addCell(divisionCell);

	                // Add mode data
	                addPdfDataCells(table, report.getWebTotal(), report.getWebComp(), report.getWebPending(), totals[0], dataFont);
	                addPdfDataCells(table, report.getMobileTotal(), report.getMobileComp(), report.getMobilePending(), totals[1], dataFont);
	                addPdfDataCells(table, report.getAdminTotal(), report.getAdminComp(), report.getAdminPending(), totals[2], dataFont);
	                addPdfDataCells(table, report.getSmsTotal(), report.getSmsComp(), report.getSmsPending(), totals[3], dataFont);
	                addPdfDataCells(table, report.getMiTotal(), report.getMiComp(), report.getMiPending(), totals[4], dataFont);

	                // Calculate and add totals
	                BigDecimal totalRevd = report.getWebTotal().add(report.getMobileTotal())
	                    .add(report.getAdminTotal()).add(report.getSmsTotal()).add(report.getMiTotal());
	                BigDecimal totalComp = report.getWebComp().add(report.getMobileComp())
	                    .add(report.getAdminComp()).add(report.getSmsComp()).add(report.getMiComp());
	                BigDecimal totalPend = report.getWebPending().add(report.getMobilePending())
	                    .add(report.getAdminPending()).add(report.getSmsPending()).add(report.getMiPending());
	                addPdfDataCells(table, totalRevd, totalComp, totalPend, totals[5], dataFont);
	            }

	            // Total row
	            PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL", totalFont));
	            styleTotalCell(totalLabelCell, Element.ALIGN_RIGHT);
	            totalLabelCell.setColspan(3);
	            table.addCell(totalLabelCell);

	            for (int mode = 0; mode < 6; mode++) {
	                for (int subCol = 0; subCol < 3; subCol++) {
	                    PdfPCell totalCell = new PdfPCell(new Phrase(totals[mode][subCol].toString(), totalFont));
	                    styleTotalCell(totalCell, Element.ALIGN_CENTER);
	                    table.addCell(totalCell);
	                }
	            }

	            document.add(table);
	        }

	        document.close();

	        pdfFile = DefaultStreamedContent.builder()
	            .contentType("application/pdf")
	            .name("ModeWise_Complaints_Abstract_Report-Section_Wise.pdf")
	            .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
	            .build();

	    } catch (DocumentException e) {
	        e.printStackTrace();
	    }
	}


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
					+ "TO_CHAR(a.updated_on, 'DD-MM-YYYY-HH24:MI') AS Attended_Date, "
					+ "c.description AS Attended_Remarks, "
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

			FacesContext.getCurrentInstance().getExternalContext().redirect("deviceWiseAbstarctComplaintList.xhtml");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

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
//                "to_char(ct.TRF_ON, 'dd-mm-yyyy-hh24:mi') AS transferedOn, "+
//                "ct.TRF_USER AS transferedUser, "+
//                "ct.REMARKS AS transferedRemarks, "+
//                "to_char(qc.QC_ON, 'dd-mm-yyyy-hh24:mi') AS qcDate, "+
//                "qc.QC_STATUS AS qcStatus, "+
//                "qc.REMARKS AS qcRemarks "+
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
                "WHERE a.id = :complaintIdParam " ;
		
		Query query = session.createNativeQuery(hql);
		query.setParameter("complaintIdParam", complaintIdParam);
		
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
//        	dto.setComplaintTransferedOn((String) row[23]);
//        	dto.setComplaintTransferedBy((String) row[24]);
//            dto.setComplaintTransferedRemarks((String) row[25]);
//            dto.setQcDate((String) row[26]);
//            dto.setQcStatus((String) row[27]);
//            dto.setQcRemarks((String) row[28]);        	
        	
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

	public ViewComplaintReportValueBean getSelectedComplaintId() {
		return selectedComplaintId;
	}

	public void setSelectedComplaintId(ViewComplaintReportValueBean selectedComplaintId) {
		this.selectedComplaintId = selectedComplaintId;
	}

	private void addDataCells(Row row, int startCol, BigDecimal revd, BigDecimal comp, BigDecimal pend,
			BigDecimal[] modeTotals, CellStyle style) {
		Cell revdCell = row.createCell(startCol);
		revdCell.setCellValue(revd != null ? revd.doubleValue() : 0);
		revdCell.setCellStyle(style);
		modeTotals[0] = modeTotals[0].add(revd != null ? revd : BigDecimal.ZERO);

		Cell compCell = row.createCell(startCol + 1);
		compCell.setCellValue(comp != null ? comp.doubleValue() : 0);
		compCell.setCellStyle(style);
		modeTotals[1] = modeTotals[1].add(comp != null ? comp : BigDecimal.ZERO);

		Cell pendCell = row.createCell(startCol + 2);
		pendCell.setCellValue(pend != null ? pend.doubleValue() : 0);
		pendCell.setCellStyle(style);
		modeTotals[2] = modeTotals[2].add(pend != null ? pend : BigDecimal.ZERO);
	}

	public void exportComplaintListToExcel(List<ViewComplaintReportValueBean> complaintList) throws IOException {
		HSSFWorkbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet("ModeWise_Complaints_Abstract_Report-Complaint_List");

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

		Row headerRow = sheet.createRow(0);

		String[] headers = { "Complaint ID", "Complaint Date", "Complaint Mode", "Consumer Number", "Consumer Details",
				"Contact Number", "Complaint Type", "Complaint Description", "Complaint Status", "Attended Date",
				"Attended Remarks", "Region", "Circle", "Division", "Sub Division", "Section" };

		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(headerStyle);
		}

		int rowNum = 1;
		for (ViewComplaintReportValueBean complaint : complaintList) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(complaint.getComplaintId().doubleValue());
			row.createCell(1).setCellValue(complaint.getCreatedOnFormatted());
			row.createCell(2).setCellValue(complaint.getDevice());
			row.createCell(3).setCellValue(complaint.getServiceNumber());
			row.createCell(4).setCellValue(complaint.getServiceAddress());
			row.createCell(5).setCellValue(complaint.getMobile());
			row.createCell(6).setCellValue(complaint.getComplaintType());
			row.createCell(7).setCellValue(complaint.getComplaintDescription());
			row.createCell(8).setCellValue(complaint.getComplaintStatusValue());
			row.createCell(9).setCellValue(complaint.getAttendedDate());
			row.createCell(10).setCellValue(complaint.getAttendedRemarks());
			row.createCell(11).setCellValue(complaint.getRegionName());
			row.createCell(12).setCellValue(complaint.getCircleName());
			row.createCell(13).setCellValue(complaint.getDivisionName());
			row.createCell(14).setCellValue(complaint.getSubDivisionName());
			row.createCell(15).setCellValue(complaint.getSectionName());

		}
		for (int i = 0; i < headers.length; i++) {
			sheet.autoSizeColumn(i);
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		workbook.write(outputStream);
		workbook.close();

		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		try {
			excelFile = DefaultStreamedContent.builder().name("Mode_Wise_Complaint_List_Report.xls")
					.contentType("application/vnd.ms-excel").stream(() -> inputStream).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void exportComplaintListToPdf(List<ViewComplaintReportValueBean> complaintList) {
		Document document = new Document(PageSize.A2.rotate());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try {
			PdfWriter.getInstance(document, outputStream);
			document.open();

			Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD, BaseColor.BLACK);
			Paragraph title = new Paragraph("MODE WISE COMPLAINT LIST", titleFont);
			title.setAlignment(Element.ALIGN_CENTER);
			title.setSpacingAfter(10);
			document.add(title);

			Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY);
			Paragraph subTitle = new Paragraph("Circle: " + selectedCircleName + "  |  Section: " + selectedSectionName,
					subtitleFont);
			subTitle.setAlignment(Element.ALIGN_CENTER);
			subTitle.setSpacingAfter(10);
			document.add(subTitle);

			PdfPTable table = new PdfPTable(16);
			table.setWidthPercentage(100);
			table.setSpacingBefore(10);
			table.setSpacingAfter(10);

			float[] columnWidths = { 2f, 2f, 2f, 3f, 4f, 3f, 3f, 4f, 3f, 3f, 3f, 2f, 2f, 2f, 3f, 2f };
			table.setWidths(columnWidths);

			String[] headers = { "Complaint ID", "Complaint Date", "Complaint Mode", "Consumer Number",
					"Consumer Details", "Contact Number", "Complaint Type", "Complaint Description", "Complaint Status",
					"Attended Date", "Attended Remarks", "Region", "Circle", "Division", "Sub Division", "Section" };

			for (String header : headers) {
				PdfPCell cell = new PdfPCell(
						new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD)));
				cell.setPadding(5);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
				table.addCell(cell);
			}

			if (complaintList != null && !complaintList.isEmpty()) {
				for (ViewComplaintReportValueBean complaint : complaintList) {
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
				PdfPCell noDataCell = new PdfPCell(new Phrase("No complaints available",
						FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC, BaseColor.RED)));
				noDataCell.setColspan(16);
				noDataCell.setPadding(10);
				noDataCell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(noDataCell);
			}

			document.add(table);

			document.close();

			pdfFile = DefaultStreamedContent.builder().contentType("application/pdf")
					.name("Mode_Wise_ComplaintList_Report.pdf")
					.stream(() -> new ByteArrayInputStream(outputStream.toByteArray())).build();

		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	private void addCell(PdfPTable table, String content) {
		PdfPCell cell = new PdfPCell(
				new Phrase(content != null ? content : "", FontFactory.getFont(FontFactory.HELVETICA, 9)));
		cell.setPadding(5);
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		cell.setBorderWidth(0.5f);
		table.addCell(cell);
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public DataModel getDmFilter() {
		return dmFilter;
	}

	public void setDmFilter(DataModel dmFilter) {
		this.dmFilter = dmFilter;
	}

	public List<ModeWiseAbstractValueBean> getCircleListFormatOne() {
		return circleListFormatOne;
	}

	public void setCircleListFormatOne(List<ModeWiseAbstractValueBean> circleListFormatOne) {
		this.circleListFormatOne = circleListFormatOne;
	}

	public List<ModeWiseAbstractValueBean> getSectionListFormatOne() {
		return sectionListFormatOne;
	}

	public void setSectionListFormatOne(List<ModeWiseAbstractValueBean> sectionListFormatOne) {
		this.sectionListFormatOne = sectionListFormatOne;
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

	public String getCurrentYear() {
		return currentYear;
	}

	public void setCurrentYear(String currentYear) {
		this.currentYear = currentYear;
	}

	public BigDecimal getTotalMobileTotal() {
		return totalMobileTotal;
	}

	public void setTotalMobileTotal(BigDecimal totalMobileTotal) {
		this.totalMobileTotal = totalMobileTotal;
	}

	public BigDecimal getTotalAdminTotal() {
		return totalAdminTotal;
	}

	public void setTotalAdminTotal(BigDecimal totalAdminTotal) {
		this.totalAdminTotal = totalAdminTotal;
	}

	public BigDecimal getTotalSmsTotal() {
		return totalSmsTotal;
	}

	public void setTotalSmsTotal(BigDecimal totalSmsTotal) {
		this.totalSmsTotal = totalSmsTotal;
	}

	public BigDecimal getTotalMiTotal() {
		return totalMiTotal;
	}

	public void setTotalMiTotal(BigDecimal totalMiTotal) {
		this.totalMiTotal = totalMiTotal;
	}

	public BigDecimal getTotalWebTotal() {
		return totalWebTotal;
	}

	public void setTotalWebTotal(BigDecimal totalWebTotal) {
		this.totalWebTotal = totalWebTotal;
	}

	public BigDecimal getTotalWebComp() {
		return totalWebComp;
	}

	public void setTotalWebComp(BigDecimal totalWebComp) {
		this.totalWebComp = totalWebComp;
	}

	public BigDecimal getTotalMobileComp() {
		return totalMobileComp;
	}

	public void setTotalMobileComp(BigDecimal totalMobileComp) {
		this.totalMobileComp = totalMobileComp;
	}

	public BigDecimal getTotalAdminComp() {
		return totalAdminComp;
	}

	public void setTotalAdminComp(BigDecimal totalAdminComp) {
		this.totalAdminComp = totalAdminComp;
	}

	public BigDecimal getTotalSmsComp() {
		return totalSmsComp;
	}

	public void setTotalSmsComp(BigDecimal totalSmsComp) {
		this.totalSmsComp = totalSmsComp;
	}

	public BigDecimal getTotalMiComp() {
		return totalMiComp;
	}

	public void setTotalMiComp(BigDecimal totalMiComp) {
		this.totalMiComp = totalMiComp;
	}

	public BigDecimal getTotalWebPending() {
		return totalWebPending;
	}

	public void setTotalWebPending(BigDecimal totalWebPending) {
		this.totalWebPending = totalWebPending;
	}

	public BigDecimal getTotalMobilePending() {
		return totalMobilePending;
	}

	public void setTotalMobilePending(BigDecimal totalMobilePending) {
		this.totalMobilePending = totalMobilePending;
	}

	public BigDecimal getTotalAdminPending() {
		return totalAdminPending;
	}

	public void setTotalAdminPending(BigDecimal totalAdminPending) {
		this.totalAdminPending = totalAdminPending;
	}

	public BigDecimal getTotalSmsPending() {
		return totalSmsPending;
	}

	public void setTotalSmsPending(BigDecimal totalSmsPending) {
		this.totalSmsPending = totalSmsPending;
	}

	public BigDecimal getTotalMiPending() {
		return totalMiPending;
	}

	public void setTotalMiPending(BigDecimal totalMiPending) {
		this.totalMiPending = totalMiPending;
	}

	public BigDecimal getTotalMobileTotalSection() {
		return totalMobileTotalSection;
	}

	public void setTotalMobileTotalSection(BigDecimal totalMobileTotalSection) {
		this.totalMobileTotalSection = totalMobileTotalSection;
	}

	public BigDecimal getTotalAdminTotalSection() {
		return totalAdminTotalSection;
	}

	public void setTotalAdminTotalSection(BigDecimal totalAdminTotalSection) {
		this.totalAdminTotalSection = totalAdminTotalSection;
	}

	public BigDecimal getTotalSmsTotalSection() {
		return totalSmsTotalSection;
	}

	public void setTotalSmsTotalSection(BigDecimal totalSmsTotalSection) {
		this.totalSmsTotalSection = totalSmsTotalSection;
	}

	public BigDecimal getTotalMiTotalSection() {
		return totalMiTotalSection;
	}

	public void setTotalMiTotalSection(BigDecimal totalMiTotalSection) {
		this.totalMiTotalSection = totalMiTotalSection;
	}

	public BigDecimal getTotalWebTotalSection() {
		return totalWebTotalSection;
	}

	public void setTotalWebTotalSection(BigDecimal totalWebTotalSection) {
		this.totalWebTotalSection = totalWebTotalSection;
	}

	public BigDecimal getTotalWebCompSection() {
		return totalWebCompSection;
	}

	public void setTotalWebCompSection(BigDecimal totalWebCompSection) {
		this.totalWebCompSection = totalWebCompSection;
	}

	public BigDecimal getTotalMobileCompSection() {
		return totalMobileCompSection;
	}

	public void setTotalMobileCompSection(BigDecimal totalMobileCompSection) {
		this.totalMobileCompSection = totalMobileCompSection;
	}

	public BigDecimal getTotalAdminCompSection() {
		return totalAdminCompSection;
	}

	public void setTotalAdminCompSection(BigDecimal totalAdminCompSection) {
		this.totalAdminCompSection = totalAdminCompSection;
	}

	public BigDecimal getTotalSmsCompSection() {
		return totalSmsCompSection;
	}

	public void setTotalSmsCompSection(BigDecimal totalSmsCompSection) {
		this.totalSmsCompSection = totalSmsCompSection;
	}

	public BigDecimal getTotalMiCompSection() {
		return totalMiCompSection;
	}

	public void setTotalMiCompSection(BigDecimal totalMiCompSection) {
		this.totalMiCompSection = totalMiCompSection;
	}

	public BigDecimal getTotalWebPendingSection() {
		return totalWebPendingSection;
	}

	public void setTotalWebPendingSection(BigDecimal totalWebPendingSection) {
		this.totalWebPendingSection = totalWebPendingSection;
	}

	public BigDecimal getTotalMobilePendingSection() {
		return totalMobilePendingSection;
	}

	public void setTotalMobilePendingSection(BigDecimal totalMobilePendingSection) {
		this.totalMobilePendingSection = totalMobilePendingSection;
	}

	public BigDecimal getTotalAdminPendingSection() {
		return totalAdminPendingSection;
	}

	public void setTotalAdminPendingSection(BigDecimal totalAdminPendingSection) {
		this.totalAdminPendingSection = totalAdminPendingSection;
	}

	public BigDecimal getTotalSmsPendingSection() {
		return totalSmsPendingSection;
	}

	public void setTotalSmsPendingSection(BigDecimal totalSmsPendingSection) {
		this.totalSmsPendingSection = totalSmsPendingSection;
	}

	public BigDecimal getTotalMiPendingSection() {
		return totalMiPendingSection;
	}

	public void setTotalMiPendingSection(BigDecimal totalMiPendingSection) {
		this.totalMiPendingSection = totalMiPendingSection;
	}



	public AdminUserValueBean getAdminUserValueBean() {
		return adminUserValueBean;
	}



	public void setAdminUserValueBean(AdminUserValueBean adminUserValueBean) {
		this.adminUserValueBean = adminUserValueBean;
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



	public BigDecimal getGrandTotalRevdSection() {
		return grandTotalRevdSection;
	}



	public void setGrandTotalRevdSection(BigDecimal grandTotalRevdSection) {
		this.grandTotalRevdSection = grandTotalRevdSection;
	}



	public BigDecimal getGrandTotalCompSection() {
		return grandTotalCompSection;
	}



	public void setGrandTotalCompSection(BigDecimal grandTotalCompSection) {
		this.grandTotalCompSection = grandTotalCompSection;
	}



	public BigDecimal getGrandTotalPendSection() {
		return grandTotalPendSection;
	}



	public void setGrandTotalPendSection(BigDecimal grandTotalPendSection) {
		this.grandTotalPendSection = grandTotalPendSection;
	}

}
