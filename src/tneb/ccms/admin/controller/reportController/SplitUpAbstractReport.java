package tneb.ccms.admin.controller.reportController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import tneb.ccms.admin.model.CategoryBean;
import tneb.ccms.admin.model.CompDeviceBean;
import tneb.ccms.admin.model.ViewComplaintReportBean;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.valuebeans.AdminUserValueBean;
import tneb.ccms.admin.valuebeans.CallCenterUserValueBean;
import tneb.ccms.admin.valuebeans.CategoriesValueBean;
import tneb.ccms.admin.valuebeans.CompDeviceValueBean;
import tneb.ccms.admin.valuebeans.TmpYearWiseCircle;
import tneb.ccms.admin.valuebeans.TmpYearWiseSections;
import tneb.ccms.admin.valuebeans.ViewComplaintReportValueBean;

import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;

@Named
@ViewScoped
public class SplitUpAbstractReport implements Serializable {

	private static final long serialVersionUID = 1L;
	private SessionFactory sessionFactory;
	DataModel dmFilter;
	List<TmpYearWiseCircle> reports = new ArrayList<>();
	List<TmpYearWiseSections> sectionList = new ArrayList<>();
	private boolean initialized = false;
	 private boolean cameFromInsideReport = false;
	 private boolean cameFromInsideSection= false;
	StreamedContent excelFile;
	StreamedContent pdfFile;
	List<ViewComplaintReportValueBean> complaintList = new ArrayList<>();
	String selectedCircleName =null;
	String selectedSectionName = null;
	String returnFrom;
	ViewComplaintReportValueBean selectedComplaintId;
	List<CompDeviceValueBean> devices;
	List<CategoriesValueBean> categories;



	@PostConstruct
	public void init() {
		System.out.println("Initializing SPLIT UP  ABSTRACT...");
		sessionFactory = HibernateUtil.getSessionFactory();
		dmFilter = new DataModel();
		loadAllDevicesAndCategories();

	}
	// REFERSH CIRCLE REPORT
	public void resetIfNeeded() {
	    if (!FacesContext.getCurrentInstance().isPostback() && !cameFromInsideReport) {
	        reports = null;
	        dmFilter.setFromDate(null);
	        dmFilter.setToDate(null);
	    }
	    cameFromInsideReport = false; // Always reset the flag
	}

	
	// REFRESH SECTION REPORT
	public void resetSectionIfNeeded() {
		
	    if (!FacesContext.getCurrentInstance().isPostback() && !cameFromInsideSection ) {
	    	System.out.println("THE BOOLEAN-----------"+cameFromInsideSection);
	        sectionList = null;
	        dmFilter.setFromDate(null);
	        dmFilter.setToDate(null);
	    }
	    cameFromInsideSection = false;
	}

	public void clearFilterAndPage() {
		dmFilter = new DataModel();
		reports = new ArrayList<>();
	}
	
	
	@SuppressWarnings("unchecked")
	@Transactional
	private void loadAllDevicesAndCategories() {
	    Session session = null;
	    try {
	        session = sessionFactory.openSession();
	        
	     // Get devices ordered by ID
	        String hql = "FROM CompDeviceBean d ORDER BY d.id ASC";
	        Query<CompDeviceBean> query = session.createQuery(hql, CompDeviceBean.class);
	        List<CompDeviceBean> devicesBean = query.getResultList();

	        // Convert to value beans
	        List<CompDeviceValueBean> orderedDeviceList = devicesBean.stream()
	                .map(CompDeviceValueBean::convertCompDeviceBeanToCompDeviceValueBean)
	                .collect(Collectors.toList());

	        // Add "ALL" as the first item
	        CompDeviceValueBean allOption = new CompDeviceValueBean("L", "ALL");
	        orderedDeviceList.add(0, allOption);

	        // Get session beans
	        HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
	                .getExternalContext().getSession(false);
	        AdminUserValueBean adminUserValueBean = (AdminUserValueBean) httpsession.getAttribute("sessionAdminValueBean");
	        CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");

	        // Apply filtering based on role
	        if (callCenterValueBean != null) {
	            int roleId = callCenterValueBean.getRoleId();

	            if (roleId == 5 || roleId == 7) {
	                // Minnagam Admin or Circle Agent – only Minnagam device
	                devices = orderedDeviceList.stream()
	                        .filter(d -> "M".equals(d.getDeviceCode()))
	                        .collect(Collectors.toList());

	            } else if (roleId == 3) {
	                // Social Media User – only SM
	                devices = orderedDeviceList.stream()
	                        .filter(d -> "S".equals(d.getDeviceCode()))
	                        .collect(Collectors.toList());

	            } else {
	                // Other call center roles – show all
	                devices = orderedDeviceList;
	            }

	        } else if (adminUserValueBean != null) {
	            int roleId = adminUserValueBean.getRoleId();

	            if (roleId == 10) {
	                // Minnagam Admin (roleId 10) – only Minnagam device
	                devices = orderedDeviceList.stream()
	                        .filter(d -> "M".equals(d.getDeviceCode()))
	                        .collect(Collectors.toList());

	            } else {
	                // Other admin roles – show all
	                devices = orderedDeviceList;
	            }

	        } else {
	            // Default fallback – show all
	            devices = orderedDeviceList;
	        }

	        String hql2 = "FROM CategoryBean d WHERE d.code IS NOT NULL ORDER BY d.id ASC ";
	        Query<CategoryBean> query2 = session.createQuery(hql2, CategoryBean.class);
	        
	        List<CategoryBean> categoryBean = query2.getResultList();
	        
	        categories = categoryBean.stream().map(CategoriesValueBean::convertCategoriesBeanToCategoriesValueBean).collect(Collectors.toList());
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        devices = new ArrayList<>(); 
	        categories = new ArrayList<>();
	    } finally {
	        if (session != null && session.isOpen()) {
	            session.close();
	        }
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
			                dmFilter.setDevice("M");
			                break;
			            }

			            // CIRCLE AGENT
			            case 7: {
			                dmFilter.setRegionCode("A");
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
				                    dmFilter.setCircleCode(String.valueOf(circleId.get(0)));	
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
			        } 
			     else if (roleId == 10) {
		            // M ADMIN
			    	    dmFilter.setRegionCode("A");
			            dmFilter.setCircleCode("A");
			            dmFilter.setDevice("M");
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
	public void searchSplitUpAbstractReport() {
		
		updateLoginWiseFilters();
		
		Session session = null;
		try {
			session = sessionFactory.openSession();
			session.beginTransaction();
			
			System.out.println("THE COMPLAINT TYPE ==="+dmFilter.getComplaintType());
			System.out.println("THE DEVICE ---------"+dmFilter.getDevice());
			
			if(dmFilter.getComplaintType()==null) {
				dmFilter.setComplaintType("AL");
			}
			
			session.createNativeQuery(
					"BEGIN split_pend_abst(:regionCode,:circleCode,:complaintType ,:device); END;")
					.setParameter("regionCode", dmFilter.getRegionCode())
					.setParameter("circleCode", dmFilter.getCircleCode())
			        .setParameter("complaintType", dmFilter.getComplaintType())
					.setParameter("device", dmFilter.getDevice())
					.executeUpdate();


			session.flush();
			session.getTransaction().commit();

			fetchReports(session);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in database operation");
		}
	}

	@Transactional
	private void fetchReports(Session session) {
		try {
			String hql ="SELECT c.REGCODE,c.CIRCODE,"
	        		+ "c.TOT1,c.LIVE1,c.TMP1,c.CPL1,"
	        		+ "c.TOT2,c.LIVE2,c.TMP2,c.CPL2,"
	        		+ "cir.name as CircleName,reg.name as RegionName "
	        		+ "FROM TMP_CT_DEV_MONABST_CIR c,Circle cir,Region reg "
	        		+ "WHERE cir.id =c.cirCode and reg.id =c.regCode";																																																																					
																																																																					
																																																																					
			List<Object[]> results = session.createNativeQuery(hql).getResultList();
			List<TmpYearWiseCircle> result = new ArrayList<TmpYearWiseCircle>();
			reports = new ArrayList<>();

			for (Object[] row : results) {
				TmpYearWiseCircle report = new TmpYearWiseCircle();
				report.setRegCode((String) row[0]);
				report.setCirCode((String) row[1]);
				
				report.setTot1((BigDecimal) row[2]);
				report.setLive1((BigDecimal) row[3]);
				report.setTmp1((BigDecimal) row[4]);
				report.setCpl1((BigDecimal) row[5]);
				
				report.setTot2((BigDecimal) row[6]);
				report.setLive2((BigDecimal) row[7]);
				report.setTmp2((BigDecimal) row[8]);
				report.setCpl2((BigDecimal) row[9]);
				
				report.setCircleName((String)row[10]);
				report.setRegionName((String)row[11]);
						 

				result.add(report);
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

				result = result.stream()
					.filter(r -> regionId.contains(Integer.valueOf(r.getRegCode())))
					.filter(c -> circleId.contains(Integer.valueOf(c.getCirCode())))
					.collect(Collectors.toList());
	        }
	        
			if(dmFilter.getRegionCode().equals("A")) {
				reports = result;
			}else {
				if(dmFilter.getCircleCode().equals("A")) {
					reports = result.stream().filter(a -> a.getRegCode().equals(dmFilter.getRegionCode())).collect(Collectors.toList());

				}else {
					reports = result.stream().filter(a -> a.getRegCode().equals(dmFilter.getRegionCode()))
							.filter(cir -> cir.getCirCode().equals(dmFilter.getCircleCode()))
							.collect(Collectors.toList());

				}
			}
			
			System.out.println("COUNT: " + reports.size());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in fetching report");
		}
	}
	
	public void redirectToCircleReport() throws IOException {

		FacesContext.getCurrentInstance().getExternalContext().redirect("splitUpCircleAbstract.xhtml");
		
		
	}
	public void redirectToSectionReport() throws IOException {
    if("circle".equals(returnFrom)) {
		FacesContext.getCurrentInstance().getExternalContext().redirect("splitUpCircleAbstract.xhtml");
    }else {
		FacesContext.getCurrentInstance().getExternalContext().redirect("splitUpSectionAbstract.xhtml");
    }
		
	}

	@Transactional
	public void fetchReportByCircle(String circleCode,String circleName) {

		Session session = null;
		try {
			session = sessionFactory.openSession();
			session.beginTransaction();
			
			System.out.println("THE CIRCLE CODE: " + circleCode);
			
			
			if(dmFilter.getSectionCode()==null) {
				dmFilter.setSectionCode("A");
			}

			session.createNativeQuery(
					"BEGIN split_pend_abst_sec(:circd,:sectionCode, :complaintType, :device); END;")
					.setParameter("circd", circleCode)
					.setParameter("sectionCode", dmFilter.getSectionCode())
					.setParameter("complaintType", dmFilter.getComplaintType())
					.setParameter("device", dmFilter.getDevice())
					.executeUpdate();


			session.flush();
			session.getTransaction().commit();
			
			String hql ="SELECT c.CIRCODE,c.SECCODE,"
					+ "c.TOT1,c.LIVE1,c.TMP1,c.CPL1,"
					+ "c.TOT2,c.LIVE2,c.TMP2,c.CPL2,"
	        		+ "cir.name as CircleName,sec.name as SectionName,d.NAME FROM TMP_CT_DEV_MONABST_SEC c,Circle cir,Section sec,Division d "
	        		+ "WHERE cir.id =c.cirCode and sec.id =c.secCode and d.id =sec.DIVISION_ID ";
			
			List<Object[]> results = session.createNativeQuery(hql).getResultList();
			List<TmpYearWiseSections> circleSection = new ArrayList<>();
			
			for (Object[] row : results) {
				TmpYearWiseSections report = new TmpYearWiseSections();
				report.setCirCode((String) row[0]);
				report.setSecCode((String) row[1]);
				
				report.setTot1((BigDecimal) row[2]);
				report.setLive1((BigDecimal) row[3]);
				report.setTmp1((BigDecimal) row[4]);
				report.setCpl1((BigDecimal) row[5]);
				
				report.setTot2((BigDecimal) row[6]);
				report.setLive2((BigDecimal) row[7]);
				report.setTmp2((BigDecimal) row[8]);
				report.setCpl2((BigDecimal) row[9]);		
				
				report.setCircleName((String)row[10]);
				report.setSectionName((String)row[11]);
				report.setDivisionName((String) row[12]);

				

				circleSection.add(report);
			}

			sectionList = circleSection.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
					.collect(Collectors.toList());
			selectedCircleName = circleName;
			cameFromInsideReport= true;
			cameFromInsideSection=true;
			
			FacesContext.getCurrentInstance().getExternalContext()
	        .getFlash().put("comingFromCircle", true);

			System.out.println("Section List size: " + sectionList.size());

			FacesContext.getCurrentInstance().getExternalContext().redirect("splitUpSectionAbstract.xhtml");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in database operation");
		}
	}
	
	
	@Transactional
	public void fetchReportForSectionUsers() {
		
		
		
		HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false);
		
		AdminUserValueBean adminUserValueBean = (AdminUserValueBean) 
				httpsession.getAttribute("sessionAdminValueBean");
		
		String circleCode= adminUserValueBean.getCircleId().toString();
		String circleName =adminUserValueBean.getCircleName();
		
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
			
			System.out.println("THE CIRCLE CODE: " + circleCode);


			session.createNativeQuery(
					"BEGIN split_pend_abst_sec(:circd,:sectionCode, :complaintType, :device); END;")
					.setParameter("circd", circleCode)
					.setParameter("sectionCode", dmFilter.getSectionCode())
					.setParameter("complaintType", dmFilter.getComplaintType())
					.setParameter("device", dmFilter.getDevice())
					.executeUpdate();


			session.flush();
			session.getTransaction().commit();

			
			String hql ="SELECT c.CIRCODE,c.SECCODE,"
					+ "c.TOT1,c.LIVE1,c.TMP1,c.CPL1,"
					+ "c.TOT2,c.LIVE2,c.TMP2,c.CPL2,"
	        		+ "s.name as SectionName, "
	        		+"s.division_id AS DIVISION_ID, " 
		            +"d.name AS DIVISION_NAME, " 
		            +"s.sub_division_id AS SUB_DIVISION_ID " 
	        		+ "FROM TMP_CT_DEV_MONABST_SEC c "
	        		+"JOIN SECTION s ON s.id = c.seccode " 
		            +"JOIN DIVISION d ON d.id = s.division_id";
			
			List<Object[]> results = session.createNativeQuery(hql).getResultList();
			List<TmpYearWiseSections> circleSection = new ArrayList<>();
			
			for (Object[] row : results) {
				TmpYearWiseSections report = new TmpYearWiseSections();
				report.setCirCode((String) row[0]);
				report.setSecCode((String) row[1]);
				
				report.setTot1((BigDecimal) row[2]);
				report.setLive1((BigDecimal) row[3]);
				report.setTmp1((BigDecimal) row[4]);
				report.setCpl1((BigDecimal) row[5]);
				
				report.setTot2((BigDecimal) row[6]);
				report.setLive2((BigDecimal) row[7]);
				report.setTmp2((BigDecimal) row[8]);
				report.setCpl2((BigDecimal) row[9]);		
				
				report.setSectionName((String)row[10]);
				report.setDivisionId((String)row[11].toString());
				report.setDivisionName((String)row[12]);
				report.setSubDivisionId((String)row[13].toString());

				

				circleSection.add(report);
			}

			//SECTION
			if(adminUserValueBean.getRoleId()==1) {
				sectionList = circleSection.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
						.filter(section -> section.getSecCode().equalsIgnoreCase(sectionCode))
						.collect(Collectors.toList());
			}
			//DIVISION
			else if(adminUserValueBean.getRoleId()==2) {
				sectionList = circleSection.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
						.filter(sd -> sd.getSubDivisionId().equalsIgnoreCase(subDivisionId))
						.collect(Collectors.toList());
			}
			//SUB DIVISION
			else if(adminUserValueBean.getRoleId()==3) {
				sectionList = circleSection.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
						.filter(d -> d.getDivisionId().equalsIgnoreCase(divisionId))
						.collect(Collectors.toList());
			}
			//ALL SECTION
			else {
				sectionList = circleSection.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
						.collect(Collectors.toList());
			}

			selectedCircleName = circleName;
			cameFromInsideSection= true;

			System.out.println("Section List size: " + sectionList.size());

			FacesContext.getCurrentInstance().getExternalContext().redirect("splitUpSectionAbstract.xhtml");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in database operation");
		}
	}
	
	
	@Transactional
	public void getComplaintListForSection(String secCode,String sectionName) throws IOException {
		System.out.println("THE SELECTED SECTION CODE IS ============"+secCode);
		try (Session session = sessionFactory.openSession()) {
			List<String> complaintTypes = new ArrayList<>();
	        List<String> devices = new ArrayList<>();
			
			if (dmFilter.getComplaintType() != null) {
	            switch (dmFilter.getComplaintType().toUpperCase()) {
	                case "BL": complaintTypes.add("BL"); break;
	                case "ME": complaintTypes.add("ME"); break;
	                case "PF": complaintTypes.add("PF"); break;
	                case "VF": complaintTypes.add("VF"); break;
	                case "FI": complaintTypes.add("FI"); break;
	                case "TH": complaintTypes.add("TH"); break;
	                case "TE": complaintTypes.add("TE"); break;
	                case "OT": complaintTypes.add("OT"); break;
	                case "CS": complaintTypes.add("CS"); break;
	                case "AL": complaintTypes.addAll(Arrays.asList("BL", "ME", "PF", "VF", "FI", "TH", "TE", "OT", "CS")); break;
	                default: throw new IllegalArgumentException("Invalid Complaint Type");
	            }
	        }

	        if (dmFilter.getDevice() != null) {
	            switch (dmFilter.getDevice().toUpperCase()) {
	                case "P": devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile")); break;
	                case "W": devices.add("web"); break;
	                case "S": devices.add("SM"); break;
	                case "A": devices.addAll(Arrays.asList("admin", "FOC")); break;
	                case "M": devices.add("MI"); break;
	                case "G": devices.add("MM"); break;
	                case "O": devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile","web","SM","MI","MM")); break;
	                case "L": devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile", "web", "SM", "admin", "FOC", "MI", "MM")); break;
	                default: throw new IllegalArgumentException("Invalid Device Type");
	            }
	        }
	
		Integer sectionCode = Integer.parseInt(secCode);
		
        StringBuilder hql = new StringBuilder("SELECT a.id, "+
        		"to_char(a.created_on, 'dd-mm-yyyy-hh24:mi') AS Complaint_Date, " +
                "DECODE(a.device, 'web', 'Web', 'FOC', 'FOC', 'admin', 'FOC', 'SM', 'Social Media', 'Android', 'Mobile', 'AMOB', 'Mobile', 'IMOB', 'Mobile', 'iOS', 'Mobile', 'mobile', 'Mobile', 'MI', 'Minnagam') AS Device, "+
                "a.SERVICE_NUMBER AS Service_Number, " +
                "a.SERVICE_NAME AS Service_Name, "+
                "a.SERVICE_ADDRESS AS Service_Address, " +
                "b.mobile AS Contact_Number, k.name AS Complaint_Type, d.name AS subctyp, " +
                "a.description AS Complaint_Description, " +
                "DECODE(a.status_id, 0, 'Pending', 1, 'In Progress', 2, 'Completed') AS Complaint_Status, " +
                "TO_CHAR(a.updated_on, 'DD-MM-YYYY-HH24:MI') AS Attended_Date, " +
                "c.description AS Attended_Remarks, " +
                "f.name AS Region, g.name AS Circle, h.name AS Division, i.name AS SubDivision, j.name AS Section " +
                "FROM COMPLAINT a " +
                "JOIN PUBLIC_USER b ON a.user_id = b.id " +
                "JOIN COMPLAINT_HISTORY c ON a.id = c.complaint_id AND a.status_id = c.status_id " +
                "JOIN SUB_CATEGORY d ON a.sub_category_id = d.id " +
                "JOIN CATEGORY k ON a.complaint_type = k.code " +
                "JOIN REGION f ON a.region_id = f.id " +
                "JOIN CIRCLE g ON a.circle_id = g.id " +
                "JOIN DIVISION h ON a.division_id = h.id " +
                "JOIN SUB_DIVISION i ON a.sub_division_id = i.id " +
                "JOIN SECTION j ON a.section_id = j.id " +
                "WHERE a.SECTION_ID = :sectionCode AND a.status_id =0 " );
        
        if (!complaintTypes.isEmpty()) {
            hql.append(" AND a.complaint_type IN (:complaintTypes)");
        }

        if (!devices.isEmpty()) {
            hql.append(" AND a.device IN (:devices)");
        }
		
        Query query = session.createNativeQuery(hql.toString());
		query.setParameter("sectionCode", sectionCode);
		
		if (!complaintTypes.isEmpty()) {
            query.setParameter("complaintTypes", complaintTypes);
        }

        if (!devices.isEmpty()) {
            query.setParameter("devices", devices);
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
		
		 selectedSectionName = sectionName;
		 returnFrom ="section";
		
		FacesContext.getCurrentInstance().getExternalContext().redirect("splitUpComplaintList.xhtml");
		
		}catch(Exception e){
			System.out.println("ERROR..........."+e);
		}
		
	}
	@Transactional
	public void getComplaintListForCircle() {

	    List<ViewComplaintReportBean> complaintBeanList;

	    try (Session session = sessionFactory.openSession()) {
			List<String> complaintTypes = new ArrayList<>();
	        List<String> devices = new ArrayList<>();
			
			if (dmFilter.getComplaintType() != null) {
	            switch (dmFilter.getComplaintType().toUpperCase()) {
	                case "BL": complaintTypes.add("BL"); break;
	                case "ME": complaintTypes.add("ME"); break;
	                case "PF": complaintTypes.add("PF"); break;
	                case "VF": complaintTypes.add("VF"); break;
	                case "FI": complaintTypes.add("FI"); break;
	                case "TH": complaintTypes.add("TH"); break;
	                case "TE": complaintTypes.add("TE"); break;
	                case "OT": complaintTypes.add("OT"); break;
	                case "CS": complaintTypes.add("CS"); break;
	                case "AL": complaintTypes.addAll(Arrays.asList("BL", "ME", "PF", "VF", "FI", "TH", "TE", "OT", "CS")); break;
	                default: throw new IllegalArgumentException("Invalid Complaint Type");
	            }
	        }

	        if (dmFilter.getDevice() != null) {
	            switch (dmFilter.getDevice().toUpperCase()) {
	                case "P": devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile")); break;
	                case "W": devices.add("web"); break;
	                case "S": devices.add("SM"); break;
	                case "A": devices.addAll(Arrays.asList("admin", "FOC")); break;
	                case "M": devices.add("MI"); break;
	                case "G": devices.add("MM"); break;
	                case "L": devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile", "web", "SM", "admin", "FOC", "MI", "MM")); break;
	                default: throw new IllegalArgumentException("Invalid Device Type");
	            }
	        }

	        StringBuilder hql = new StringBuilder("SELECT a.id, "+
	        		"to_char(a.created_on, 'dd-mm-yyyy-hh24:mi') AS Complaint_Date, " +
	                "DECODE(a.device, 'web', 'Web', 'FOC', 'FOC', 'admin', 'FOC', 'SM', 'Social Media', 'Android', 'Mobile', 'AMOB', 'Mobile', 'IMOB', 'Mobile', 'iOS', 'Mobile', 'mobile', 'Mobile', 'MI', 'Minnagam') AS Device, "+
	                "a.SERVICE_NUMBER AS Service_Number, " +
	                "a.SERVICE_NAME AS Service_Name, "+
	                "a.SERVICE_ADDRESS AS Service_Address, " +
	                "b.mobile AS Contact_Number, k.name AS Complaint_Type, d.name AS subctyp, " +
	                "a.description AS Complaint_Description, " +
	                "DECODE(a.status_id, 0, 'Pending', 1, 'In Progress', 2, 'Completed') AS Complaint_Status, " +
	                "TO_CHAR(a.updated_on, 'DD-MM-YYYY-HH24:MI') AS Attended_Date, " +
	                "c.description AS Attended_Remarks, " +
	                "f.name AS Region, g.name AS Circle, h.name AS Division, i.name AS SubDivision, j.name AS Section " +
	                "FROM COMPLAINT a " +
	                "JOIN PUBLIC_USER b ON a.user_id = b.id " +
	                "JOIN COMPLAINT_HISTORY c ON a.id = c.complaint_id AND a.status_id = c.status_id " +
	                "JOIN SUB_CATEGORY d ON a.sub_category_id = d.id " +
	                "JOIN CATEGORY k ON a.complaint_type = k.code " +
	                "JOIN REGION f ON a.region_id = f.id " +
	                "JOIN CIRCLE g ON a.circle_id = g.id " +
	                "JOIN DIVISION h ON a.division_id = h.id " +
	                "JOIN SUB_DIVISION i ON a.sub_division_id = i.id " +
	                "JOIN SECTION j ON a.section_id = j.id "
	                + "WHERE a.status_id = 0" );
	        
	        if (!complaintTypes.isEmpty()) {
	            hql.append("AND a.complaint_type IN (:complaintTypes)");
	        }

	        if (!devices.isEmpty()) {
	            hql.append(" AND a.device IN (:devices)");
	        }
	        Query query = session.createNativeQuery(hql.toString());
			
			if (!complaintTypes.isEmpty()) {
	            query.setParameter("complaintTypes", complaintTypes);
	        }

	        if (!devices.isEmpty()) {
	            query.setParameter("devices", devices);
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

	        returnFrom="circle";
	        selectedSectionName=null;
	        
	        FacesContext.getCurrentInstance().getExternalContext().redirect("splitUpComplaintList.xhtml");
	    } catch (Exception e) {
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
                "TO_CHAR(a.updated_on, 'DD-MM-YYYY-HH24:MI') AS Attended_Date, " +
                "c.DESCRIPTION AS Attended_Remarks, " +
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

	
	public void exportToExcel(List<TmpYearWiseCircle> reports) throws IOException {
	    HSSFWorkbook workbook = new HSSFWorkbook();
	    Sheet sheet = workbook.createSheet("CircleWise_PeriodWise_Pending_SplitUp_Abstract");

	    Row headingRow = sheet.createRow(0);
	    Cell headingCell = headingRow.createCell(0);
	    headingCell.setCellValue("CIRCLE WISE PERIODWISE PENDING SPLITUP ABSTRACT");

	    CellStyle headingStyle = workbook.createCellStyle();
	    HSSFFont headingFont = workbook.createFont();
	    headingFont.setBold(true);
	    headingFont.setFontHeightInPoints((short) 12);
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

	    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));

	    String complaintType = dmFilter.getComplaintType();

	    Map<String, String> complaintTypeMap = new HashMap<>();
	    complaintTypeMap.put("AL", "ALL");
	    complaintTypeMap.put("PF", "POWER FAILURE");
	    complaintTypeMap.put("VF", "VOLTAGE RELATED");
	    complaintTypeMap.put("ME", "METER RELATED");
	    complaintTypeMap.put("BL", "BILLING RELATED");
	    complaintTypeMap.put("FI", "FIRE");
	    complaintTypeMap.put("TH", "DANGEROUS POLE");
	    complaintTypeMap.put("TE", "THEFT OF POWER");
	    complaintTypeMap.put("CS", "CONDUCTOR SNAPPING");
	    complaintTypeMap.put("OT", "OTHERS");

	    complaintType = complaintTypeMap.getOrDefault(complaintType, "-");

	    String device = dmFilter.getDevice();

	    Map<String, String> deviceMap = new HashMap<>();
	    deviceMap.put("L", "ALL");
	    deviceMap.put("P", "MOBILE");
	    deviceMap.put("W", "WEB");
	    deviceMap.put("S", "SM");
	    deviceMap.put("A", "FOC");
	    deviceMap.put("M", "MINNAGAM");
	    deviceMap.put("G", "MM");

	    device = deviceMap.getOrDefault(device, "-");

	    CellStyle dateStyle = workbook.createCellStyle();
	    HSSFFont dateFont = workbook.createFont();
	    dateFont.setFontHeightInPoints((short) 10);
	    dateStyle.setFont(dateFont);
	    dateStyle.setAlignment(HorizontalAlignment.CENTER);
	    dateStyle.setVerticalAlignment(VerticalAlignment.CENTER);
	    
		CellStyle dataCellStyle = workbook.createCellStyle();
		dataCellStyle.setBorderBottom(BorderStyle.THIN);
		dataCellStyle.setBorderTop(BorderStyle.THIN);
		dataCellStyle.setBorderLeft(BorderStyle.THIN);
		dataCellStyle.setBorderRight(BorderStyle.THIN);
		dataCellStyle.setAlignment(HorizontalAlignment.LEFT);
		dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

	    Row subHeadingRow = sheet.createRow(1);
	    Cell subHeadingCell = subHeadingRow.createCell(0);
	    subHeadingCell.setCellValue("Complaint Type :" + complaintType + " | " + " Device :" + device);
	    subHeadingCell.setCellStyle(dateStyle);
	    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 10));

	    // Header Style
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

	    // Creating Header Row
	    Row headerRow = sheet.createRow(2);
	    String[] mainHeaders = {"S.NO", "Circle", "Within 24 Hrs", "1 Week", "15 Days", "1 Month", "3 Months", "6 Months", "1 Year", "More Than 1 Year", "Total"};

	    for (int i = 0; i < mainHeaders.length; i++) {
	        Cell cell = headerRow.createCell(i);
	        cell.setCellValue(mainHeaders[i]);
	        cell.setCellStyle(headerStyle);
	    }

	    int rowNum = 3;
	    BigDecimal[] columnTotals = new BigDecimal[9];  // Only for columns 2 to 10 excluding serial and circle
	    Arrays.fill(columnTotals, BigDecimal.ZERO);

	    int serialNumber = 1;

	    // Data Rows
	    for (TmpYearWiseCircle report : reports) {
	        Row row = sheet.createRow(rowNum++);

	        row.createCell(0).setCellValue(serialNumber++);
	        row.getCell(0).setCellStyle(dataCellStyle);
	        row.createCell(1).setCellValue(report.getCircleName());
	        row.getCell(1).setCellStyle(dataCellStyle);


	        BigDecimal[] values = {
	            report.getTot1(), report.getLive1(), report.getTmp1(), report.getCpl1(),
	            report.getTot2(), report.getLive2(), report.getTmp2(), report.getCpl2(),
	            BigDecimal.ZERO  // Placeholder for total per row
	        };

	        BigDecimal total = BigDecimal.ZERO;
	        for (int i = 0; i < 9; i++) {
	            if (i < 8) {
	            	BigDecimal value = values[i] != null ? values[i] : BigDecimal.ZERO;

	                Cell cell = row.createCell(i + 2);
	                cell.setCellValue(value.doubleValue());
	                cell.setCellStyle(dataCellStyle);  

	                columnTotals[i] = columnTotals[i].add(value);
	                total = total.add(value);
	            } else {
	            	Cell totalCell = row.createCell(10);
	                totalCell.setCellValue(total.doubleValue());
	                totalCell.setCellStyle(dataCellStyle);  

	                columnTotals[i] = columnTotals[i].add(total);
	            }
	        }
	    }

	    // Total Row Style
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
	    Cell totalCell = totalRow.createCell(0);
	    totalCell.setCellValue("TOTAL");
	    totalCell.setCellStyle(totalRowStyle);

	    Cell blankCell = totalRow.createCell(1);
	    blankCell.setCellStyle(totalRowStyle);

	    for (int i = 0; i < 9; i++) {
	        Cell cell = totalRow.createCell(i + 2);
	        cell.setCellStyle(totalRowStyle);
	        cell.setCellValue(columnTotals[i].doubleValue());
	    }

	    for (int i = 0; i < mainHeaders.length; i++) {
	        sheet.autoSizeColumn(i);
	    }

	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    workbook.write(outputStream);
	    workbook.close();

	    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
	    try {
	        excelFile = DefaultStreamedContent.builder()
	            .name("CircleWise_PeriodWise_Pending_SplitUp_Abstract.xls")
	            .contentType("application/vnd.ms-excel")
	            .stream(() -> inputStream)
	            .build();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}


	public void exportSectionsToExcel(List<TmpYearWiseSections> reports) throws IOException {
	    HSSFWorkbook workbook = new HSSFWorkbook();
	    Sheet sheet = workbook.createSheet("SectionWise_PeriodWise_Pending_SplitUp_Abstract_For_Circle");

	    // Heading
	    Row headingRow = sheet.createRow(0);
	    Cell headingCell = headingRow.createCell(0);
	    headingCell.setCellValue("SECTION WISE PERIODWISE PENDING SPLITUP ABSTRACT FOR CIRCLE :" + selectedCircleName);

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
	    
		CellStyle dataCellStyle = workbook.createCellStyle();
		dataCellStyle.setBorderBottom(BorderStyle.THIN);
		dataCellStyle.setBorderTop(BorderStyle.THIN);
		dataCellStyle.setBorderLeft(BorderStyle.THIN);
		dataCellStyle.setBorderRight(BorderStyle.THIN);
		dataCellStyle.setAlignment(HorizontalAlignment.LEFT);
		dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

	    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 12));

	    // Complaint Type
	    String complaintType = dmFilter.getComplaintType();
	    Map<String, String> complaintTypeMap = new HashMap<>();
	    complaintTypeMap.put("AL", "ALL");
	    complaintTypeMap.put("PF", "POWER FAILURE");
	    complaintTypeMap.put("VF", "VOLTAGE RELATED");
	    complaintTypeMap.put("ME", "METER RELATED");
	    complaintTypeMap.put("BL", "BILLING RELATED");
	    complaintTypeMap.put("FI", "FIRE");
	    complaintTypeMap.put("TH", "DANGEROUS POLE");
	    complaintTypeMap.put("TE", "THEFT OF POWER");
	    complaintTypeMap.put("CS", "CONDUCTOR SNAPPING");
	    complaintTypeMap.put("OT", "OTHERS");
	    complaintType = complaintTypeMap.getOrDefault(complaintType, "-");

	    // Device
	    String device = dmFilter.getDevice();
	    Map<String, String> deviceMap = new HashMap<>();
	    deviceMap.put("L", "ALL");
	    deviceMap.put("P", "MOBILE");
	    deviceMap.put("W", "WEB");
	    deviceMap.put("S", "SM");
	    deviceMap.put("A", "FOC");
	    deviceMap.put("M", "MINNAGAM");
	    deviceMap.put("G", "MM");
	    device = deviceMap.getOrDefault(device, "-");

	    CellStyle dateStyle = workbook.createCellStyle();
	    HSSFFont dateFont = workbook.createFont();
	    dateFont.setFontHeightInPoints((short) 8);
	    dateStyle.setFont(dateFont);
	    dateStyle.setAlignment(HorizontalAlignment.CENTER);
	    dateStyle.setVerticalAlignment(VerticalAlignment.CENTER);

	    Row subHeadingRow = sheet.createRow(1);
	    Cell subHeadingCell = subHeadingRow.createCell(0);
	    subHeadingCell.setCellValue("Complaint Type :" + complaintType + " | " + " Device :" + device + " | " + "Circle :" + selectedCircleName);
	    subHeadingCell.setCellStyle(dateStyle);
	    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 12));

	    // Header
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

	    Row headerRow = sheet.createRow(2);
	    String[] mainHeaders = {"S.NO", "Section", "Division", "Within 24 Hrs", "1 Week", "15 Days", "1 Month", "3 Months", "6 Months", "1 Year", "More Than 1 Year", "Total"};

	    for (int i = 0; i < mainHeaders.length; i++) {
	        Cell cell = headerRow.createCell(i);
	        cell.setCellValue(mainHeaders[i]);
	        cell.setCellStyle(headerStyle);
	    }

	    int rowNum = 3;
	    BigDecimal[] columnTotals = new BigDecimal[12];
	    Arrays.fill(columnTotals, BigDecimal.ZERO);

	    int serialNumber = 1;
	    for (TmpYearWiseSections report : reports) {
	        Row row = sheet.createRow(rowNum++);

	        row.createCell(0).setCellValue(serialNumber++);
	        row.getCell(0).setCellStyle(dataCellStyle);
	        row.createCell(1).setCellValue(report.getSectionName());
	        row.getCell(1).setCellStyle(dataCellStyle);
	        row.createCell(2).setCellValue(report.getDivisionName());
	        row.getCell(2).setCellStyle(dataCellStyle);

	        BigDecimal[] values = {
	            report.getTot1(), report.getLive1(), report.getTmp1(), report.getCpl1(),
	            report.getTot2(), report.getLive2(), report.getTmp2(), report.getCpl2()
	        };

	        BigDecimal total = BigDecimal.ZERO;

	        for (int i = 0; i < values.length; i++) {
	            Cell cell = row.createCell(i + 3);
	            if (values[i] != null) {
	                cell.setCellValue(values[i].doubleValue());
	               
	                columnTotals[i + 3] = columnTotals[i + 3].add(values[i]); // Update correct column total
	                total = total.add(values[i]);
	            } else {
	                cell.setCellValue(0);
	            }
	            cell.setCellStyle(dataCellStyle);
	        }

	        // Total column
	        Cell totalCell = row.createCell(11);
	        totalCell.setCellValue(total.doubleValue());
	        totalCell.setCellStyle(dataCellStyle);
	        columnTotals[11] = columnTotals[11].add(total); // Add to grand total
	    }

	    // Total Row
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
	 // Create "TOTAL" label and merge first 3 cells
	    totalRow.createCell(0).setCellValue("TOTAL");
	    sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 2));
	    totalRow.getCell(0).setCellStyle(totalRowStyle);
	    
	    for (int i = 3; i <= 11; i++) {
	        Cell cell = totalRow.createCell(i);
	        cell.setCellValue(columnTotals[i].doubleValue());
	        cell.setCellStyle(totalRowStyle);
	    }

	    // Auto-size columns
	    for (int i = 0; i <= 11; i++) {
	        sheet.autoSizeColumn(i);
	    }

	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    workbook.write(outputStream);
	    workbook.close();

	    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

	    try {
	        excelFile = DefaultStreamedContent.builder()
	                .name("SectionWise_PeriodWise_Pending_SplitUp_Abstract_For_Circle.xls")
	                .contentType("application/vnd.ms-excel")
	                .stream(() -> inputStream)
	                .build();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	 
	 
	 
	 
	  public void exportToPdf(List<TmpYearWiseCircle> reports) throws IOException, DocumentException {
		    
		        Document document = new Document(PageSize.A4.rotate());
		        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		        PdfWriter.getInstance(document, outputStream);

		        document.open();

		        //TITLE
		        Font titleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
		        Paragraph title = new Paragraph("PENDING SPLIT UP CIRCLE ABSTARCT REPORT", titleFont);
		        title.setAlignment(Element.ALIGN_CENTER);
		        title.setSpacingAfter(10);
		        document.add(title);
		        
			    String complaintType = dmFilter.getComplaintType();
			    
			    Map<String, String> complaintTypeMap = new HashMap<>();
			    complaintTypeMap.put("AL", "ALL");
			    complaintTypeMap.put("PF", "POWER FAILURE");
			    complaintTypeMap.put("VF", "VOLTAGE RELATED");
			    complaintTypeMap.put("ME", "METER RELATED");
			    complaintTypeMap.put("BL", "BILLING RELATED");
			    complaintTypeMap.put("FI", "FIRE");
			    complaintTypeMap.put("TH", "DANGEROUS POLE");
			    complaintTypeMap.put("TE", "THEFT OF POWER");
			    complaintTypeMap.put("CS", "CONDUCTOR SNAPPING");
			    complaintTypeMap.put("OT", "OTHERS");
			    
			    complaintType = complaintTypeMap.getOrDefault(complaintType, "-");
			    
			    String device = dmFilter.getDevice();

			    Map<String, String> deviceMap = new HashMap<>();
			    deviceMap.put("L", "ALL");
			    deviceMap.put("P", "MOBILE");
			    deviceMap.put("W", "WEB");
			    deviceMap.put("S", "SM");
			    deviceMap.put("A", "FOC");
			    deviceMap.put("M", "MINNAGAM");
			    deviceMap.put("G", "MM");
			    
			    device = deviceMap.getOrDefault(device, "-");
		        
		        Paragraph subTitle = new Paragraph("COMPLAINT TYPE : "+complaintType+" | "+"Device: "+device);
		        subTitle.setAlignment(Element.ALIGN_CENTER);
		        subTitle.setSpacingAfter(10);
		        document.add(subTitle);

		        // HEADERS
		        PdfPTable table = new PdfPTable(11); 
		        table.setWidthPercentage(100);
		        table.setSpacingBefore(10);
		        table.setSpacingAfter(10);
		        
		        // COLUMN WIDTH 
		        float[] columnWidths = {1.5f,3f, 2.5f, 2.5f, 2.5f, 2.5f, 2.5f, 2.5f, 2.5f, 2.5f, 2f};
		        table.setWidths(columnWidths);
		        
		        // HEADER STYLE
		        Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
		        BaseColor headerColor = new BaseColor(200, 200, 200); 

		        String[] mainHeaders = {"S.NO","CIRCLE", "Within 24 Hrs", "1 Week", "15 Days", "1 Month", "3 Months", "6 Months", "1 Year", "More Than 1 Year", "Total"};

		        for (String header : mainHeaders) {
		            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
		            cell.setBackgroundColor(headerColor);
		            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		            cell.setPadding(5);
		            table.addCell(cell);
		        }
		        
		        BigDecimal[] columnTotals = new BigDecimal[9]; 
		        Arrays.fill(columnTotals, BigDecimal.ZERO);
		        
		        int serialNumber=1;
		        Font dataFont = new Font(Font.FontFamily.HELVETICA, 9);
		        for (TmpYearWiseCircle report : reports) {
		            table.addCell(new PdfPCell(new Phrase(String.valueOf(serialNumber++), dataFont)));

		            table.addCell(new PdfPCell(new Phrase(report.getCircleName(), dataFont)));

		            BigDecimal[] values = {
		                report.getTot1(), report.getLive1(), report.getTmp1(), report.getCpl1(),
		                report.getTot2(), report.getLive2(), report.getTmp2(), report.getCpl2()
		            };

		            BigDecimal rowTotal = BigDecimal.ZERO;
		            for (int i = 0; i < values.length; i++) {
		            	BigDecimal val = values[i] != null ? values[i] : BigDecimal.ZERO;
		                PdfPCell valueCell = new PdfPCell(new Phrase(val.toPlainString(), dataFont));
		                valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		                table.addCell(valueCell);

		                columnTotals[i] = columnTotals[i].add(val);
		                rowTotal = rowTotal.add(val);
		            }

		            PdfPCell totalCell = new PdfPCell(new Phrase(rowTotal.toPlainString(), dataFont));
		            totalCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		            totalCell.setBackgroundColor(headerColor);
		            table.addCell(totalCell);

		            columnTotals[8] = columnTotals[8].add(rowTotal);
		        }
		        
		        Font totalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
		        PdfPCell totalCell = new PdfPCell(new Phrase("TOTAL", totalFont));
		        totalCell.setColspan(2);
		        totalCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		        totalCell.setBackgroundColor(headerColor);
		        totalCell.setPadding(5);
		        table.addCell(totalCell);
		        
		        for (int i = 0; i < 9; i++) {
		            PdfPCell totalValueCell = new PdfPCell(new Phrase(columnTotals[i].toPlainString(), totalFont));
		            totalValueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		            totalValueCell.setBackgroundColor(headerColor);
		            totalValueCell.setPadding(5);
		            table.addCell(totalValueCell);
		        }

		        document.add(table);
		        document.close();

		        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		        try {
		            pdfFile = DefaultStreamedContent.builder()
		                    .name("Pending_SplitUp_Circle_Abstract_Report.pdf")
		                    .contentType("application/pdf")
		                    .stream(() -> inputStream)
		                    .build();
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		    
	  }

	  public void exportSectionsToPdf(List<TmpYearWiseSections> reports) throws IOException, DocumentException {
		    
	        Document document = new Document(PageSize.A3.rotate());
	        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	        PdfWriter.getInstance(document, outputStream);

	        document.open();

	        //TITLE
	        Font titleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
	        Paragraph title = new Paragraph("PENDING SPLIT UP SECTION ABSTARCT REPORT", titleFont);
	        title.setAlignment(Element.ALIGN_CENTER);
	        title.setSpacingAfter(10);
	        document.add(title);
	        
		    String complaintType = dmFilter.getComplaintType();
		    
		    Map<String, String> complaintTypeMap = new HashMap<>();
		    complaintTypeMap.put("AL", "ALL");
		    complaintTypeMap.put("PF", "POWER FAILURE");
		    complaintTypeMap.put("VF", "VOLTAGE RELATED");
		    complaintTypeMap.put("ME", "METER RELATED");
		    complaintTypeMap.put("BL", "BILLING RELATED");
		    complaintTypeMap.put("FI", "FIRE");
		    complaintTypeMap.put("TH", "DANGEROUS POLE");
		    complaintTypeMap.put("TE", "THEFT OF POWER");
		    complaintTypeMap.put("CS", "CONDUCTOR SNAPPING");
		    complaintTypeMap.put("OT", "OTHERS");
		    
		    complaintType = complaintTypeMap.getOrDefault(complaintType, "-");
		    
		    String device = dmFilter.getDevice();

		    Map<String, String> deviceMap = new HashMap<>();
		    deviceMap.put("L", "ALL");
		    deviceMap.put("P", "MOBILE");
		    deviceMap.put("W", "WEB");
		    deviceMap.put("S", "SM");
		    deviceMap.put("A", "FOC");
		    deviceMap.put("M", "MINNAGAM");
		    deviceMap.put("G", "MM");
		    
		    device = deviceMap.getOrDefault(device, "-");
	        
	        
	        //SUB HEADING 	        
	        Font subHeadingFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
	        Paragraph subHeading = new Paragraph("CIRCLE NAME: " + selectedCircleName + "  |  COMPLAINT TYPE: " + complaintType + "  |  DEVICE: " + device, subHeadingFont);
	        subHeading.setAlignment(Element.ALIGN_CENTER);
	        subHeading.setSpacingAfter(5);
	        document.add(subHeading);


	        // HEADERS
	        PdfPTable table = new PdfPTable(12); // 11 COLUMNS
	        table.setWidthPercentage(100);
	        table.setSpacingBefore(10);
	        table.setSpacingAfter(10);
	        
	        // COLUMN WIDTH 
	        float[] columnWidths = {1.5f,3f,3f, 2.5f, 2.5f, 2.5f, 2.5f, 2.5f, 2.5f, 2.5f, 2.5f, 2f};
	        table.setWidths(columnWidths);
	        
	        // HEADER STYLE
	        Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
	        BaseColor headerColor = new BaseColor(200, 200, 200); 

	        String[] mainHeaders = {"S.NO","Section","Division", "Within 24 Hrs", "1 Week", "15 Days", "1 Month", "3 Months", "6 Months", "1 Year", "More Than 1 Year", "Total"};

	        for (String header : mainHeaders) {
	            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
	            cell.setBackgroundColor(headerColor);
	            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	            cell.setPadding(5);
	            table.addCell(cell);
	        }
	        
	        BigDecimal[] columnTotals = new BigDecimal[10]; 
	        Arrays.fill(columnTotals, BigDecimal.ZERO);
	        
	        int serialNumber=1;
	        Font dataFont = new Font(Font.FontFamily.HELVETICA, 9);
	        for (TmpYearWiseSections report : reports) {
	            table.addCell(new PdfPCell(new Phrase(String.valueOf(serialNumber++), dataFont)));
	            table.addCell(new PdfPCell(new Phrase(report.getSectionName(), dataFont)));
	            table.addCell(new PdfPCell(new Phrase(report.getDivisionName(), dataFont)));


	            BigDecimal[] values = {
	                report.getTot1(), report.getLive1(), report.getTmp1(), report.getCpl1(),
	                report.getTot2(), report.getLive2(), report.getTmp2(), report.getCpl2()
	            };

	            BigDecimal rowTotal = BigDecimal.ZERO;
	            for (int i = 0; i < values.length; i++) {
	            	BigDecimal val = values[i] != null ? values[i] : BigDecimal.ZERO;
	                PdfPCell valueCell = new PdfPCell(new Phrase(val.toPlainString(), dataFont));
	                valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	                table.addCell(valueCell);

	                columnTotals[i] = columnTotals[i].add(val);
	                rowTotal = rowTotal.add(val);
	            }

	            PdfPCell totalCell = new PdfPCell(new Phrase(rowTotal.toPlainString(), dataFont));
	            totalCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	            totalCell.setBackgroundColor(headerColor);
	            table.addCell(totalCell);

	            columnTotals[9] = columnTotals[9].add(rowTotal);
	        }
	        
	        Font totalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
	        PdfPCell totalCell = new PdfPCell(new Phrase("TOTAL", totalFont));
	        totalCell.setColspan(3);
	        totalCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	        totalCell.setBackgroundColor(headerColor);
	        totalCell.setPadding(5);
	        table.addCell(totalCell);
	        
	        for (int i = 0; i < 10; i++) {
	            PdfPCell totalValueCell = new PdfPCell(new Phrase(columnTotals[i].toPlainString(), totalFont));
	            totalValueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	            totalValueCell.setBackgroundColor(headerColor);
	            totalValueCell.setPadding(5);
	            table.addCell(totalValueCell);
	        }

	        document.add(table);
	        document.close();

	        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
	        try {
	            pdfFile = DefaultStreamedContent.builder()
	                    .name("Pending_SplitUp_Section_Abstract_Report.pdf")
	                    .contentType("application/pdf")
	                    .stream(() -> inputStream)
	                    .build();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    
}
	  
	  
	  
	  
	  public void exportComplaintListToExcel(List<ViewComplaintReportValueBean> complaintList) throws IOException {
		  HSSFWorkbook workbook = new HSSFWorkbook();
		  Sheet sheet = workbook.createSheet("Pending_SplitUp_Abstract_Complaints_Report");
		  
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
		  
		  String[] headers = {
			        "Complaint ID", "Complaint Date", "Complaint Mode", "Consumer Number", "Consumer Details",
			        "Contact Number", "Complaint Type", "Complaint Description", "Complaint Status", "Attended Date",
			        "Attended Remarks", "Region", "Circle", "Division", "Sub Division", "Section"
			    };
          
          
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
              row.createCell(7).setCellValue(complaint.getCompletedRemarks());
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
		        excelFile = DefaultStreamedContent.builder()
		                .name("Pending_SplitUp_Abstract_Complaints_Report.xls")
		                .contentType("application/vnd.ms-excel")
		                .stream(() -> inputStream)
		                .build();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
	  }
	  
	  public void exportComplaintListToPdf(List<ViewComplaintReportValueBean> complaintList) {
		  Document document = new Document(PageSize.A4.rotate());
		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		    try {
		        PdfWriter.getInstance(document, outputStream);
		        document.open();
		        
		        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD, BaseColor.BLACK);
		        Paragraph title = new Paragraph("PENDING SPLIT UP ABSTRACT COMPLAINTS REPORT", titleFont);
		        title.setAlignment(Element.ALIGN_CENTER);
		        title.setSpacingAfter(10);
		        document.add(title);
		        
		        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY);
		        Paragraph subTitle = new Paragraph("Circle: " + selectedCircleName + "  |  Section: " + selectedSectionName, subtitleFont);
		        subTitle.setAlignment(Element.ALIGN_CENTER);
		        subTitle.setSpacingAfter(10);
		        document.add(subTitle);
		        
		        PdfPTable table = new PdfPTable(16);
		        table.setWidthPercentage(100); 
		        table.setSpacingBefore(10); 
		        table.setSpacingAfter(10); 
		        
		        float[] columnWidths = {1.5f, 1.5f, 1.5f, 2f, 3f, 2f, 2f, 3f, 2f, 2f, 2f, 1.5f, 1.5f, 1.5f, 2f, 1.5f};
		        table.setWidths(columnWidths);
		        
		        String[] headers = {
		            "Complaint ID", "Complaint Date", "Complaint Mode", "Consumer Number", "Consumer Details",
		            "Contact Number", "Complaint Type", "Complaint Description", "Complaint Status", "Attended Date",
		            "Attended Remarks", "Region", "Circle", "Division", "Sub Division", "Section"
		        };


		        for (String header : headers) {
		            PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD)));
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
		                addCell(table, complaint.getCompletedRemarks());
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
		            noDataCell.setColspan(16);
		            noDataCell.setPadding(10);
		            noDataCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		            table.addCell(noDataCell);
		        }

		        document.add(table);
		        
		        document.close();

		        pdfFile = DefaultStreamedContent.builder()
		            .contentType("application/pdf")
		            .name("Pending_SplitUp_Abstract_Complaints_Report.pdf")
		            .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
		            .build();

		    } catch (DocumentException e) {
		        e.printStackTrace();
		    }
		}

		private void addCell(PdfPTable table, String content) {
		    PdfPCell cell = new PdfPCell(new Phrase(content != null ? content : "", FontFactory.getFont(FontFactory.HELVETICA, 9)));
		    cell.setPadding(5);
		    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		    cell.setBorderWidth(0.5f);
		    table.addCell(cell);
		}

	 

	public DataModel getDmFilter() {
		return dmFilter;
	}

	public void setDmFilter(DataModel dmFilter) {
		this.dmFilter = dmFilter;
	}

	public List<TmpYearWiseCircle> getReports() {
		return reports;
	}

	public void setReports(List<TmpYearWiseCircle> reports) {
		this.reports = reports;
	}

	public List<TmpYearWiseSections> getSectionList() {
		return sectionList;
	}

	public void setSectionList(List<TmpYearWiseSections> sectionList) {
		this.sectionList = sectionList;
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



	public ViewComplaintReportValueBean getSelectedComplaintId() {
		return selectedComplaintId;
	}

	public void setSelectedComplaintId(ViewComplaintReportValueBean selectedComplaintId) {
		this.selectedComplaintId = selectedComplaintId;
	}
	
	public List<CompDeviceValueBean> getDevices() {
		return devices;
	}

	public void setDevices(List<CompDeviceValueBean> devices) {
		this.devices = devices;
	}

	public List<CategoriesValueBean> getCategories() {
		return categories;
	}

	public void setCategories(List<CategoriesValueBean> categories) {
		this.categories = categories;
	}

	public List<CompDeviceValueBean> getDevice() {
		return devices;
	}

	public void setDevice(List<CompDeviceValueBean> devices) {
		this.devices = devices;
	}


}
