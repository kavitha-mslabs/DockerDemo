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
import tneb.ccms.admin.util.CCMSConstants;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.valuebeans.AdminUserValueBean;
import tneb.ccms.admin.valuebeans.CallCenterUserValueBean;
import tneb.ccms.admin.valuebeans.CategoriesValueBean;
import tneb.ccms.admin.valuebeans.CompDeviceValueBean;
import tneb.ccms.admin.valuebeans.ModeWiseAbstractValueBean;
import tneb.ccms.admin.valuebeans.ViewComplaintReportValueBean;

import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;

@Named
@ViewScoped
public class ModeWiseByDateAndCategoryReport implements Serializable {

	private static final long serialVersionUID = 1L;
	private SessionFactory sessionFactory;
	DataModel dmFilter;
	List<ModeWiseAbstractValueBean> circleList = new ArrayList<>(); 

	List<ModeWiseAbstractValueBean> sectionList = new ArrayList<>(); 
	 private boolean cameFromInsideReport = false;
	 private boolean cameFromInsideSection = false;
	StreamedContent excelFile;
	StreamedContent pdfFile;
	List<ViewComplaintReportValueBean> complaintList = new ArrayList<>();
	String selectedCircleName = null;
	String selectedSectionName = null;
	String redirectFrom;
	ViewComplaintReportValueBean selectedComplaintId;
	private Date currentDate = new Date();
	private String currentYear = String.valueOf(Year.now().getValue());
	
	List<CompDeviceValueBean> devices;
	List<CategoriesValueBean> categories;
	
	private BigDecimal grandTotalRevd = BigDecimal.ZERO;
	private BigDecimal grandTotalComp = BigDecimal.ZERO;
	private BigDecimal grandTotalPend = BigDecimal.ZERO;
	
	private BigDecimal grandTotalRevdSection = BigDecimal.ZERO;
	private BigDecimal grandTotalCompSection = BigDecimal.ZERO;
	private BigDecimal grandTotalPendSection = BigDecimal.ZERO;



	public List<ModeWiseAbstractValueBean> getCircleList() {
		return circleList;
	}

	public void setCircleList(List<ModeWiseAbstractValueBean> circleList) {
		this.circleList = circleList;
	}

	public List<ModeWiseAbstractValueBean> getSectionList() {
		return sectionList;
	}

	public void setSectionList(List<ModeWiseAbstractValueBean> sectionList) {
		this.sectionList = sectionList;
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

	public List<CategoriesValueBean> getCategories() {
		return categories;
	}

	public void setCategories(List<CategoriesValueBean> categories) {
		this.categories = categories;
	}

	public List<CompDeviceValueBean> getDevices() {
		return devices;
	}

	public void setDevices(List<CompDeviceValueBean> devices) {
		this.devices = devices;
	}

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

	@PostConstruct
	public void init() {
		System.out.println("Initializing MODE WISE ABSTRACT AS ON DATE AND CATEGORY...");
		sessionFactory = HibernateUtil.getSessionFactory();
		dmFilter = new DataModel();
		loadAllDevicesAndCategories();

	}
	// REFERSH CIRCLE REPORT
	// REFERSH CIRCLE REPORT
	public void resetIfNeeded() {
	    if (!FacesContext.getCurrentInstance().isPostback() && !cameFromInsideReport) {
	        circleList = null;
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
	
	    
	    
		HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false);
		
		AdminUserValueBean adminUserValueBean = (AdminUserValueBean) 
				httpsession.getAttribute("sessionAdminValueBean");
		
		CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");
		
		System.out.println("THE LOGGED USER ---- ADMIN USER---"+adminUserValueBean);
		System.out.println("THE LOGGED USER ---- CALL CENTER USER---"+callCenterValueBean);

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
        
		
		else if(adminUserValueBean !=null) {
        Integer roleId = adminUserValueBean.getRoleId();
        
        // HEAD QUATERS
        if(roleId>=6 && roleId<=9) {
        	dmFilter.setRegionCode("A");
        	dmFilter.setCircleCode("A");
        }
        //REGION
        else if(roleId==5) {      
        	dmFilter.setRegionCode(adminUserValueBean.getRegionId().toString());
        	dmFilter.setCircleCode("A");	
        }
        //CIRCLE
        else if(roleId==4) {
        	dmFilter.setRegionCode(adminUserValueBean.getRegionId().toString());
        	dmFilter.setCircleCode(adminUserValueBean.getCircleId().toString());
        }
        else {
        	dmFilter.setRegionCode("A");
        	dmFilter.setCircleCode("A");
        }
		}
		
		// IF LOGGED USER ANYONE NOT
		else {
			dmFilter.setRegionCode("A");
			dmFilter.setCircleCode("A");
		}
        
	}


	@Transactional
	public void searchModeWiseAbstractAsOnDateAndCategory() { // FORMAT ONE REPORT - MODE WISE

		updateLoginWiseFilters();

		// DEFAULT DATE
		Calendar fromCal = Calendar.getInstance();
		fromCal.set(2023, 0, 1);

		if ((dmFilter.getFromDate() == null) && (dmFilter.getToDate() == null)) {
			dmFilter.setFromDate(fromCal.getTime());
			dmFilter.setToDate(new Date());
		}

		if ((dmFilter.getFromDate() != null) && (dmFilter.getToDate() == null)) {
			dmFilter.setToDate(new Date());
		}

		if ((dmFilter.getFromDate() == null) && (dmFilter.getToDate() != null)) {
			dmFilter.setFromDate(fromCal.getTime());
		}

		if (dmFilter.getFromDate().after(dmFilter.getToDate())) {
			FacesMessage message = new FacesMessage(
				FacesMessage.SEVERITY_ERROR, 
				"ERROR", 
				"From Date Cannot Be After To Date"
			);
			FacesContext.getCurrentInstance().addMessage(null, message);
			return;
		}

		Session session = null;

		try {
			session = sessionFactory.openSession();
			session.beginTransaction();

			if (dmFilter.getComplaintType() == null) {
				dmFilter.setComplaintType("AL");
			}

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String formattedFromDate = sdf.format(dmFilter.getFromDate());
			String formattedToDate = sdf.format(dmFilter.getToDate());

			System.out.println("THE REGION CODE------------" + dmFilter.getRegionCode());
			System.out.println("THE CIRCLE CODE--------------" + dmFilter.getCircleCode());

			session.createNativeQuery(
				"BEGIN DEV_CIR_ABST_ALL_DT_CAT(:regionCode, :circleCode, :complaintType, TO_DATE(:fromDate, 'YYYY-MM-DD'), TO_DATE(:toDate, 'YYYY-MM-DD')); END;")
				.setParameter("regionCode", dmFilter.getRegionCode())
				.setParameter("circleCode", dmFilter.getCircleCode())
				.setParameter("complaintType", dmFilter.getComplaintType())
				.setParameter("fromDate", formattedFromDate)
				.setParameter("toDate", formattedToDate)
				.executeUpdate();

			session.createNativeQuery("BEGIN dev_cir_abst_single_all; END;").executeUpdate();

			session.flush();
			session.getTransaction().commit();

			try {
				String hql = "SELECT REGCODE, REGNAME, CIRCODE, CIRNAME, WBTOT, WBCOM, WBPEN, MBTOT, MBCOM, MBPEN, ADTOT, ADCOM, ADPEN, SMTOT, SMCOM, SMPEN, MITOT, MICOM, MIPEN FROM TMP_DEV_CIR";

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

				AdminUserValueBean adminUserValueBean = (AdminUserValueBean) httpsession
					.getAttribute("sessionAdminValueBean");

				CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession
					.getAttribute("sessionCallCenterUserValueBean");

				if (callCenterValueBean != null &&
					(callCenterValueBean.getRoleId() == 1 || callCenterValueBean.getRoleId() == 7)) {

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

				if (dmFilter.getRegionCode().equals("A")) {
					if (dmFilter.getCircleCode().equals("A")) {
						circleList = resultList;
					} else {
						circleList = resultList.stream()
							.filter(c -> c.getCircleCode().equals(dmFilter.getCircleCode()))
							.collect(Collectors.toList());
					}
				} else {
					if (dmFilter.getCircleCode().equals("A")) {
						circleList = resultList.stream()
							.filter(r -> r.getRegionCode().equals(dmFilter.getRegionCode()))
							.collect(Collectors.toList());
					} else {
						circleList = resultList.stream()
							.filter(r -> r.getRegionCode().equals(dmFilter.getRegionCode()))
							.filter(c -> c.getCircleCode().equals(dmFilter.getCircleCode()))
							.collect(Collectors.toList());
					}
				}

				System.out.println("THE CIRCLE LIST --------------------" + circleList.size());
				System.out.println("THE CIRCLE LIST -------------------" + circleList.get(0).getMiPending());

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
			totalMiTotal = circleList.stream().map(ModeWiseAbstractValueBean::getMiTotal).reduce(BigDecimal.ZERO,
					BigDecimal::add);
			
			totalMiComp = circleList.stream().map(ModeWiseAbstractValueBean::getMiComp).reduce(BigDecimal.ZERO,
					BigDecimal::add);
			
			totalMiPending = circleList.stream().map(ModeWiseAbstractValueBean::getMiPending)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			
			totalWebTotal = BigDecimal.ZERO;
			totalMobileTotal =  BigDecimal.ZERO;
			totalAdminTotal =  BigDecimal.ZERO;
			totalSmsTotal =  BigDecimal.ZERO;

			totalWebComp =  BigDecimal.ZERO;
			totalMobileComp =  BigDecimal.ZERO;
			totalAdminComp =  BigDecimal.ZERO;
			totalSmsComp = BigDecimal.ZERO;

			totalWebPending = BigDecimal.ZERO;
			totalMobilePending =  BigDecimal.ZERO;
			totalAdminPending =  BigDecimal.ZERO;
			totalSmsPending =  BigDecimal.ZERO;

			
			
			grandTotalRevd = totalMiTotal;
			grandTotalComp = totalMiComp;
			grandTotalPend = totalMiPending;
		}
		else {
		totalWebTotal = circleList.stream().map(ModeWiseAbstractValueBean::getWebTotal).reduce(BigDecimal.ZERO,
				BigDecimal::add);
		totalMobileTotal = circleList.stream().map(ModeWiseAbstractValueBean::getMobileTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalAdminTotal = circleList.stream().map(ModeWiseAbstractValueBean::getAdminTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalSmsTotal = circleList.stream().map(ModeWiseAbstractValueBean::getSmsTotal).reduce(BigDecimal.ZERO,
				BigDecimal::add);
		totalMiTotal = circleList.stream().map(ModeWiseAbstractValueBean::getMiTotal).reduce(BigDecimal.ZERO,
				BigDecimal::add);

		totalWebComp = circleList.stream().map(ModeWiseAbstractValueBean::getWebComp).reduce(BigDecimal.ZERO,
				BigDecimal::add);
		totalMobileComp = circleList.stream().map(ModeWiseAbstractValueBean::getMobileComp)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalAdminComp = circleList.stream().map(ModeWiseAbstractValueBean::getAdminComp)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalSmsComp = circleList.stream().map(ModeWiseAbstractValueBean::getSmsComp).reduce(BigDecimal.ZERO,
				BigDecimal::add);
		totalMiComp = circleList.stream().map(ModeWiseAbstractValueBean::getMiComp).reduce(BigDecimal.ZERO,
				BigDecimal::add);

		totalWebPending = circleList.stream().map(ModeWiseAbstractValueBean::getWebPending)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalMobilePending = circleList.stream().map(ModeWiseAbstractValueBean::getMobilePending)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalAdminPending = circleList.stream().map(ModeWiseAbstractValueBean::getAdminPending)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalSmsPending = circleList.stream().map(ModeWiseAbstractValueBean::getSmsPending)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalMiPending = circleList.stream().map(ModeWiseAbstractValueBean::getMiPending)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		
		grandTotalRevd = totalWebTotal.add(totalMobileTotal).add(totalAdminTotal).add(totalSmsTotal).add(totalMiTotal);
		grandTotalComp = totalWebComp.add(totalMobileComp).add(totalAdminComp).add(totalSmsComp).add(totalMiComp);
		grandTotalPend = totalWebPending.add(totalMobilePending).add(totalAdminPending).add(totalSmsPending).add(totalMiPending);

		}


	}

	public void redirectToCircleReport() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().redirect("modeWiseCircleAbstractDateAndCategory.xhtml");
	}

	// SECTION REPORT FORMAT ONE - MODE WISE
	@Transactional
	public void fetchReportByCircle(String circleCode, String circleName) {

		Session session = null;
		try {
			session = sessionFactory.openSession();
			session.beginTransaction();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String formattedFromDate = sdf.format(dmFilter.getFromDate());
			String formattedToDate = sdf.format(dmFilter.getToDate());

			if (dmFilter.getSectionCode() == null) {
				dmFilter.setSectionCode("A");
			}
			if (dmFilter.getComplaintType() == null) {
				dmFilter.setComplaintType("AL");
			}

			session.createNativeQuery(
					"BEGIN DEV_SEC_ABST_ALL_DT_CIR_CAT(:circd,:secCode,:complaintType, TO_DATE(:fromDate, 'YYYY-MM-DD'), TO_DATE(:toDate, 'YYYY-MM-DD')); END;")
					.setParameter("circd", circleCode).setParameter("secCode", dmFilter.getSectionCode())
					.setParameter("complaintType", dmFilter.getComplaintType())
					.setParameter("fromDate", formattedFromDate).setParameter("toDate", formattedToDate)
					.executeUpdate();

			session.createNativeQuery("BEGIN dev_sec_abst_single_all; END;").executeUpdate();

			session.flush();
			session.getTransaction().commit();

			String hql = "SELECT c.*,d.NAME as DIVISION_NAME FROM TMP_DEV_SEC c,SECTION s,DIVISION d WHERE c.SECCODE = s.ID AND d.ID =s.DIVISION_ID ";

			List<Object[]> results = session.createNativeQuery(hql).getResultList();
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

			sectionList = circleSection.stream().filter(c -> c.getCircleCode().equals(circleCode))
					.collect(Collectors.toList());
			computeTotalForSection(); // TOTAL FOOTER AND GRAND TOTAL VALUES
			selectedCircleName = circleName;
			cameFromInsideReport= true;
			cameFromInsideSection=true;

			
			FacesContext.getCurrentInstance().getExternalContext().redirect("modeWiseSectionAbstractDateAndCategory.xhtml");

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in database operation");
		}
	}
	
	// SECTION REPORT FORMAT ONE - MODE WISE
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
			
			
			//DEFAULT DATE FOR FILTER
			Calendar fromCal = Calendar.getInstance();
			fromCal.set(2023, 0, 1);
			if ((dmFilter.getFromDate() == null) && (dmFilter.getToDate() == null)) {

				dmFilter.setFromDate(fromCal.getTime());
				dmFilter.setToDate(new Date());
			}
			if ((dmFilter.getFromDate() != null) && (dmFilter.getToDate() == null)) {
				dmFilter.setToDate(new Date());
			}
			if ((dmFilter.getFromDate() == null) && (dmFilter.getToDate() != null)) {
				dmFilter.setFromDate(fromCal.getTime());
			}

			Session session = null;
			try {
				session = sessionFactory.openSession();
				session.beginTransaction();

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String formattedFromDate = sdf.format(dmFilter.getFromDate());
				String formattedToDate = sdf.format(dmFilter.getToDate());

				if (dmFilter.getComplaintType() == null) {
					dmFilter.setComplaintType("AL");
				}

				session.createNativeQuery(
						"BEGIN DEV_SEC_ABST_ALL_DT_CIR_CAT(:circd,:secCode,:complaintType, TO_DATE(:fromDate, 'YYYY-MM-DD'), TO_DATE(:toDate, 'YYYY-MM-DD')); END;")
						.setParameter("circd", circleCode)
						.setParameter("secCode", dmFilter.getSectionCode())
						.setParameter("complaintType", dmFilter.getComplaintType())
						.setParameter("fromDate", formattedFromDate)
						.setParameter("toDate", formattedToDate)
						.executeUpdate();

				session.createNativeQuery("BEGIN dev_sec_abst_single_all; END;").executeUpdate();

				session.flush();
				session.getTransaction().commit();

				String hql = "SELECT c.*, " +
			             "s.division_id AS DIVISION_ID, " +
			             "d.name AS DIVISION_NAME, " +
			             "s.sub_division_id AS SUB_DIVISION_ID " +
			             "FROM TMP_DEV_SEC c " +
			             "JOIN SECTION s ON s.id = c.seccode " +
			             "JOIN DIVISION d ON d.id = s.division_id";
				
				//String hql = "SELECT c.*,d.NAME as DIVISION_NAME FROM TMP_DEV_SEC c,SECTION s,DIVISION d WHERE c.SECCODE = s.ID AND d.ID =s.DIVISION_ID ";

				List<Object[]> results = session.createNativeQuery(hql).getResultList();
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

					report.setDivisionId((String) row[20].toString());
					report.setDivisionName((String) row[21]);
					report.setSubDivisionId((String) row[22].toString());

					circleSection.add(report);
				}
				
				//SECTION
				if(adminUserValueBean.getRoleId()==1) {
					sectionList = circleSection.stream().filter(circle -> circle.getCircleCode().equalsIgnoreCase(circleCode))
							.filter(section -> section.getSectionCode().equalsIgnoreCase(sectionCode))
							.collect(Collectors.toList());
				}
				//DIVISION
				else if(adminUserValueBean.getRoleId()==2) {
					sectionList = circleSection.stream().filter(circle -> circle.getCircleCode().equalsIgnoreCase(circleCode))
							.filter(sd -> sd.getSubDivisionId().equalsIgnoreCase(subDivisionId))
							.collect(Collectors.toList());
				}
				//SUB DIVISION
				else if(adminUserValueBean.getRoleId()==3) {
					sectionList = circleSection.stream().filter(circle -> circle.getCircleCode().equalsIgnoreCase(circleCode))
							.filter(d -> d.getDivisionId().equalsIgnoreCase(divisionId))
							.collect(Collectors.toList());
				}
				//ALL SECTION
				else {
					sectionList = circleSection.stream().filter(circle -> circle.getCircleCode().equalsIgnoreCase(circleCode))
							.collect(Collectors.toList());
				}

				computeTotalForSection(); // TOTAL FOOTER AND GRAND TOTAL VALUES
				
				selectedCircleName = circleName;
				cameFromInsideSection=true;
				FacesContext.getCurrentInstance().getExternalContext().redirect("modeWiseSectionAbstractDateAndCategory.xhtml");

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error in database operation");
			}
		}

	// REFRESH REPORT LIST BUTTON
	public void clearReportData() {

		circleList = null;
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
			totalMiTotalSection = sectionList.stream().map(ModeWiseAbstractValueBean::getMiTotal)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			
			totalMiCompSection = sectionList.stream().map(ModeWiseAbstractValueBean::getMiComp)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			
			totalMiPendingSection = sectionList.stream().map(ModeWiseAbstractValueBean::getMiPending)
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
		
		totalWebTotalSection = sectionList.stream().map(ModeWiseAbstractValueBean::getWebTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalMobileTotalSection = sectionList.stream().map(ModeWiseAbstractValueBean::getMobileTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalAdminTotalSection = sectionList.stream().map(ModeWiseAbstractValueBean::getAdminTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalSmsTotalSection = sectionList.stream().map(ModeWiseAbstractValueBean::getSmsTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalMiTotalSection = sectionList.stream().map(ModeWiseAbstractValueBean::getMiTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		totalWebCompSection = sectionList.stream().map(ModeWiseAbstractValueBean::getWebComp)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalMobileCompSection = sectionList.stream().map(ModeWiseAbstractValueBean::getMobileComp)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalAdminCompSection = sectionList.stream().map(ModeWiseAbstractValueBean::getAdminComp)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalSmsCompSection = sectionList.stream().map(ModeWiseAbstractValueBean::getSmsComp)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalMiCompSection = sectionList.stream().map(ModeWiseAbstractValueBean::getMiComp)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		totalWebPendingSection = sectionList.stream().map(ModeWiseAbstractValueBean::getWebPending)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalMobilePendingSection = sectionList.stream().map(ModeWiseAbstractValueBean::getMobilePending)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalAdminPendingSection = sectionList.stream().map(ModeWiseAbstractValueBean::getAdminPending)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalSmsPendingSection = sectionList.stream().map(ModeWiseAbstractValueBean::getSmsPending)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		totalMiPendingSection = sectionList.stream().map(ModeWiseAbstractValueBean::getMiPending)
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

        
	    boolean isRole3 = callCenterValueBean != null && callCenterValueBean.getRoleId() == 3;
	    boolean isRole7 = callCenterValueBean != null && callCenterValueBean.getRoleId() == 7;
	    boolean isRole10 = adminUserValueBean !=null && adminUserValueBean.getRoleId()==10;

	    HSSFWorkbook workbook = new HSSFWorkbook();
	    Sheet sheet = workbook.createSheet("Mode_Wise_Abstract_Report_For_A_DateRange_And_Category_CircleWise");

	    // Set column widths
	    sheet.setColumnWidth(0, 3000); // S.NO
	    sheet.setColumnWidth(1, 4000); // CIRCLE
	    
	    if (isRole3 || isRole7 ||isRole10) {
	        for (int i = 2; i <= 4; i++) {
	            sheet.setColumnWidth(i, 2500);
	        }
	    } else {
	        for (int i = 2; i <= 19; i++) {
	            sheet.setColumnWidth(i, 2500);
	        }
	    }

	    // Create cell styles
	    CellStyle titleStyle = workbook.createCellStyle();
	    HSSFFont headingFont = workbook.createFont();
	    headingFont.setBold(true);
	    headingFont.setFontHeightInPoints((short) 10);
	    titleStyle.setFont(headingFont);
	    titleStyle.setAlignment(HorizontalAlignment.CENTER);
	    titleStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
	    titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	    titleStyle.setBorderBottom(BorderStyle.THIN);
	    titleStyle.setBorderTop(BorderStyle.THIN);
	    titleStyle.setBorderLeft(BorderStyle.THIN);
	    titleStyle.setBorderRight(BorderStyle.THIN);
	    titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
	    
	    CellStyle subHeaderStyle = workbook.createCellStyle();
	    HSSFFont headerFont1 = workbook.createFont();
	    headerFont1.setBold(true);
	    subHeaderStyle.setFont(headerFont1);
	    headerFont1.setFontHeightInPoints((short) 10);
	    subHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
	    subHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
	    subHeaderStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
	    subHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	    subHeaderStyle.setBorderBottom(BorderStyle.THIN);
	    subHeaderStyle.setBorderTop(BorderStyle.THIN);
	    subHeaderStyle.setBorderLeft(BorderStyle.THIN);
	    subHeaderStyle.setBorderRight(BorderStyle.THIN);
	    
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
	    
	    CellStyle subHeadingStyle = workbook.createCellStyle();
	    HSSFFont subheaderFont = workbook.createFont();
	    subheaderFont.setBold(true);
	    subHeadingStyle.setFont(subheaderFont);
	    subHeadingStyle.setAlignment(HorizontalAlignment.CENTER);
	    subHeadingStyle.setVerticalAlignment(VerticalAlignment.CENTER);
	    subHeadingStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	    subHeadingStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	    subHeadingStyle.setBorderBottom(BorderStyle.THIN);
	    subHeadingStyle.setBorderTop(BorderStyle.THIN);
	    subHeadingStyle.setBorderLeft(BorderStyle.THIN);
	    subHeadingStyle.setBorderRight(BorderStyle.THIN);

	    // Title row
	    Row titleRow = sheet.createRow(0);
	    Cell titleCell = titleRow.createCell(0);
	    titleCell.setCellValue("MODE WISE ABSTRACT FOR A DATE RANGE AND CATEGORY REPORT - CIRCLE WISE");
	    titleCell.setCellStyle(titleStyle);
	    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, isRole3 || isRole7 ? 4 : 19));

	    // Date row
	    String complaintType = getComplaintTypeDisplay();
	    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	    String fromDateStr = dmFilter.getFromDate() != null ? dateFormat.format(dmFilter.getFromDate()) : "N/A";
	    String toDateStr = dmFilter.getToDate() != null ? dateFormat.format(dmFilter.getToDate()) : "N/A";
	    Row dateRow = sheet.createRow(1);
	    Cell dateCell = dateRow.createCell(0);
	    dateCell.setCellValue("From Date: " + fromDateStr + "  To Date: " + toDateStr + "  Complaint Type :" + complaintType);
	    dateCell.setCellStyle(subHeaderStyle);
	    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, isRole3 || isRole7 ? 4 : 19));

	    // Header rows
	    Row headerRow = sheet.createRow(2);
	    Row subHeaderRow = sheet.createRow(3);

	    // S.NO header
	    Cell sNoHeader = headerRow.createCell(0);
	    sNoHeader.setCellValue("S.NO");
	    sNoHeader.setCellStyle(headerStyle);
	    sheet.addMergedRegion(new CellRangeAddress(2, 3, 0, 0));
	    
	    // Circle header
	    Cell circleHeader = headerRow.createCell(1);
	    circleHeader.setCellValue("CIRCLE");
	    circleHeader.setCellStyle(headerStyle);
	    sheet.addMergedRegion(new CellRangeAddress(2, 3, 1, 1));

	    if (isRole3) {
	        // Role 3 - only show Social Media columns
	        Cell smHeader = headerRow.createCell(2);
	        smHeader.setCellValue("SOCIAL MEDIA");
	        smHeader.setCellStyle(headerStyle);
	        sheet.addMergedRegion(new CellRangeAddress(2, 2, 2, 4));
	        
	        // Sub headers
	        String[] subColumns = {"Revd", "Comp", "Pend"};
	        for (int i = 0; i < subColumns.length; i++) {
	            Cell subHeader = subHeaderRow.createCell(2 + i);
	            subHeader.setCellValue(subColumns[i]);
	            subHeader.setCellStyle(subHeadingStyle);
	        }
	    } else if (isRole7 || isRole10) {
	        // Role 7 - only show MI columns
	        Cell miHeader = headerRow.createCell(2);
	        miHeader.setCellValue("MINNAGAM");
	        miHeader.setCellStyle(headerStyle);
	        sheet.addMergedRegion(new CellRangeAddress(2, 2, 2, 4));
	        
	        // Sub headers
	        String[] subColumns = {"Revd", "Comp", "Pend"};
	        for (int i = 0; i < subColumns.length; i++) {
	            Cell subHeader = subHeaderRow.createCell(2 + i);
	            subHeader.setCellValue(subColumns[i]);
	            subHeader.setCellStyle(subHeadingStyle);
	        }
	    } else {
	        // Full report for other roles
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
	                subHeader.setCellStyle(subHeadingStyle);
	                colIndex++;
	            }
	        }
	    }

	    // Data rows
	    int rowNum = 4;
	    BigDecimal[][] totals = new BigDecimal[6][3];
	    for (int i = 0; i < 6; i++) {
	        Arrays.fill(totals[i], BigDecimal.ZERO);
	    }

	    int serialNumber = 1;
	    for (ModeWiseAbstractValueBean report : reports) {
	        Row row = sheet.createRow(rowNum++);

	        // S.NO and Circle (same for all roles)
	        row.createCell(0).setCellValue(String.valueOf(serialNumber++));
	        row.getCell(0).setCellStyle(dataStyle);
	        
	        row.createCell(1).setCellValue(report.getCircleName());
	        row.getCell(1).setCellStyle(dataStyle);

	        if (isRole3) {
	            addDataCells(row, 2, report.getSmsTotal(), report.getSmsComp(), report.getSmsPending(), totals[3], dataStyle);
	        } else if (isRole7 || isRole10) {
	            addDataCells(row, 2, report.getMiTotal(), report.getMiComp(), report.getMiPending(), totals[4], dataStyle);
	        } else {
	            // Full report for other roles
	            addDataCells(row, 2, report.getWebTotal(), report.getWebComp(), report.getWebPending(), totals[0], dataStyle);
	            addDataCells(row, 5, report.getMobileTotal(), report.getMobileComp(), report.getMobilePending(), totals[1], dataStyle);
	            addDataCells(row, 8, report.getAdminTotal(), report.getAdminComp(), report.getAdminPending(), totals[2], dataStyle);
	            addDataCells(row, 11, report.getSmsTotal(), report.getSmsComp(), report.getSmsPending(), totals[3], dataStyle);
	            addDataCells(row, 14, report.getMiTotal(), report.getMiComp(), report.getMiPending(), totals[4], dataStyle);

	            BigDecimal totalRevd = report.getWebTotal().add(report.getMobileTotal()).add(report.getAdminTotal())
	                    .add(report.getSmsTotal()).add(report.getMiTotal());
	            BigDecimal totalComp = report.getWebComp().add(report.getMobileComp()).add(report.getAdminComp())
	                    .add(report.getSmsComp()).add(report.getMiComp());
	            BigDecimal totalPend = report.getWebPending().add(report.getMobilePending()).add(report.getAdminPending())
	                    .add(report.getSmsPending()).add(report.getMiPending());
	            addDataCells(row, 17, totalRevd, totalComp, totalPend, totals[5], dataStyle);
	        }
	    }

	    // Total row
	    Row totalRow = sheet.createRow(rowNum);
	    totalRow.createCell(0).setCellValue("TOTAL");
	    totalRow.getCell(0).setCellStyle(totalStyle);
	    totalRow.createCell(1).setCellValue("");
	    totalRow.getCell(1).setCellStyle(totalStyle);

	    if (isRole3) {
	        // Role 3 - only show Social Media totals
	        for (int subCol = 0; subCol < 3; subCol++) {
	            Cell cell = totalRow.createCell(2 + subCol);
	            cell.setCellValue(totals[3][subCol].doubleValue());
	            cell.setCellStyle(totalStyle);
	        }
	    } else if (isRole7 || isRole10) {
	        // Role 7 - only show MI totals
	        for (int subCol = 0; subCol < 3; subCol++) {
	            Cell cell = totalRow.createCell(2 + subCol);
	            cell.setCellValue(totals[4][subCol].doubleValue());
	            cell.setCellStyle(totalStyle);
	        }
	    } else {
	        // Full report for other roles
	        for (int mode = 0; mode < 6; mode++) {
	            int startCol = 2 + (mode * 3);
	            for (int subCol = 0; subCol < 3; subCol++) {
	                Cell cell = totalRow.createCell(startCol + subCol);
	                cell.setCellValue(totals[mode][subCol].doubleValue());
	                cell.setCellStyle(totalStyle);
	            }
	        }
	    }

	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    workbook.write(outputStream);
	    workbook.close();

	    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
	    excelFile = DefaultStreamedContent.builder().name("Mode_Wise_Abstract_Report_For_A_DateRange_And_Category_CircleWise.xls")
	            .contentType("application/vnd.ms-excel").stream(() -> inputStream).build();
	}

	// SECTION REPORT TO EXCEL DOWNLOAD - FORMAT ONE - MODE WISE (ROLE BASED)
	public void exportSectionToExcelFormatOne(List<ModeWiseAbstractValueBean> reports) throws IOException {
	    HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
	    CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");
        AdminUserValueBean adminUserValueBean = (AdminUserValueBean) httpsession.getAttribute("sessionAdminValueBean");

        
	    boolean isRole3 = callCenterValueBean != null && callCenterValueBean.getRoleId() == 3;
	    boolean isRole7 = callCenterValueBean != null && callCenterValueBean.getRoleId() == 7;
	    boolean isRole10=adminUserValueBean!=null && adminUserValueBean.getRoleId()==10;

	    HSSFWorkbook workbook = new HSSFWorkbook();
	    Sheet sheet = workbook.createSheet("Mode_Wise_Abstract_Report_For_A_DateRange_And_Category_SectionWise");

	    sheet.setColumnWidth(0, 3000); // S.NO
	    sheet.setColumnWidth(1, 4000); // SECTION
	    sheet.setColumnWidth(2, 4000); // DIVISION
	    
	    if (isRole3 || isRole7 || isRole10) {
	        for (int i = 3; i <= 5; i++) {
	            sheet.setColumnWidth(i, 2500);
	        }
	    } else {
	        for (int i = 3; i <= 20; i++) {
	            sheet.setColumnWidth(i, 2500);
	        }
	    }

	    CellStyle titleStyle = workbook.createCellStyle();
	    HSSFFont headingFont = workbook.createFont();
	    headingFont.setBold(true);
	    headingFont.setFontHeightInPoints((short) 10);
	    titleStyle.setFont(headingFont);
	    titleStyle.setAlignment(HorizontalAlignment.CENTER);
	    titleStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
	    titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	    titleStyle.setBorderBottom(BorderStyle.THIN);
	    titleStyle.setBorderTop(BorderStyle.THIN);
	    titleStyle.setBorderLeft(BorderStyle.THIN);
	    titleStyle.setBorderRight(BorderStyle.THIN);
	    titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
	    
	    CellStyle subHeaderStyle = workbook.createCellStyle();
	    HSSFFont headerFont1 = workbook.createFont();
	    headerFont1.setBold(true);
	    subHeaderStyle.setFont(headerFont1);
	    headerFont1.setFontHeightInPoints((short) 10);
	    subHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
	    subHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
	    subHeaderStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
	    subHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	    subHeaderStyle.setBorderBottom(BorderStyle.THIN);
	    subHeaderStyle.setBorderTop(BorderStyle.THIN);
	    subHeaderStyle.setBorderLeft(BorderStyle.THIN);
	    subHeaderStyle.setBorderRight(BorderStyle.THIN);
	    
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
	    
	    CellStyle subHeadingStyle = workbook.createCellStyle();
	    HSSFFont subheaderFont = workbook.createFont();
	    subheaderFont.setBold(true);
	    subHeadingStyle.setFont(subheaderFont);
	    subHeadingStyle.setAlignment(HorizontalAlignment.CENTER);
	    subHeadingStyle.setVerticalAlignment(VerticalAlignment.CENTER);
	    subHeadingStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	    subHeadingStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	    subHeadingStyle.setBorderBottom(BorderStyle.THIN);
	    subHeadingStyle.setBorderTop(BorderStyle.THIN);
	    subHeadingStyle.setBorderLeft(BorderStyle.THIN);
	    subHeadingStyle.setBorderRight(BorderStyle.THIN);

	    // Title row
	    Row titleRow = sheet.createRow(0);
	    Cell titleCell = titleRow.createCell(0);
	    titleCell.setCellValue("MODE WISE ABSTRACT FOR A DATE RANGE AND CATEGORY REPORT - SECTION WISE");
	    titleCell.setCellStyle(titleStyle);
	    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, isRole3 || isRole7 ? 5 : 20));

	    // Date row
	    String complaintType = getComplaintTypeDisplay();
	    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	    String fromDateStr = dmFilter.getFromDate() != null ? dateFormat.format(dmFilter.getFromDate()) : "N/A";
	    String toDateStr = dmFilter.getToDate() != null ? dateFormat.format(dmFilter.getToDate()) : "N/A";
	    Row dateRow = sheet.createRow(1);
	    Cell dateCell = dateRow.createCell(0);
	    dateCell.setCellValue("From Date: " + fromDateStr + "  To Date: " + toDateStr + "  Complaint Type :" + complaintType);
	    dateCell.setCellStyle(subHeaderStyle);
	    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, isRole3 || isRole7 ? 5 : 20));

	    // Header rows
	    Row headerRow = sheet.createRow(2);
	    Row subHeaderRow = sheet.createRow(3);

	    // S.NO header
	    Cell sNoHeader = headerRow.createCell(0);
	    sNoHeader.setCellValue("S.NO");
	    sNoHeader.setCellStyle(headerStyle);
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

	    if (isRole3) {
	        // Role 3 - only show Social Media columns
	        Cell smHeader = headerRow.createCell(3);
	        smHeader.setCellValue("SOCIAL MEDIA");
	        smHeader.setCellStyle(headerStyle);
	        sheet.addMergedRegion(new CellRangeAddress(2, 2, 3, 5));
	        
	        // Sub headers
	        String[] subColumns = {"Revd", "Comp", "Pend"};
	        for (int i = 0; i < subColumns.length; i++) {
	            Cell subHeader = subHeaderRow.createCell(3 + i);
	            subHeader.setCellValue(subColumns[i]);
	            subHeader.setCellStyle(subHeadingStyle);
	        }
	    } else if (isRole7 || isRole10) {
	        // Role 7 - only show MI columns
	        Cell miHeader = headerRow.createCell(3);
	        miHeader.setCellValue("MINNAGAM");
	        miHeader.setCellStyle(headerStyle);
	        sheet.addMergedRegion(new CellRangeAddress(2, 2, 3, 5));
	        
	        // Sub headers
	        String[] subColumns = {"Revd", "Comp", "Pend"};
	        for (int i = 0; i < subColumns.length; i++) {
	            Cell subHeader = subHeaderRow.createCell(3 + i);
	            subHeader.setCellValue(subColumns[i]);
	            subHeader.setCellStyle(subHeadingStyle);
	        }
	    } else {
	        // Full report for other roles
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
	                subHeader.setCellStyle(subHeadingStyle);
	                colIndex++;
	            }
	        }
	    }

	    // Data rows
	    int rowNum = 4;
	    BigDecimal[][] totals = new BigDecimal[6][3];
	    for (int i = 0; i < 6; i++) {
	        Arrays.fill(totals[i], BigDecimal.ZERO);
	    }

	    int serialNumber = 1;
	    for (ModeWiseAbstractValueBean report : reports) {
	        Row row = sheet.createRow(rowNum++);

	        // S.NO, Section, Division (same for all roles)
	        row.createCell(0).setCellValue(String.valueOf(serialNumber++));
	        row.getCell(0).setCellStyle(dataStyle);
	        
	        row.createCell(1).setCellValue(report.getSectionName());
	        row.getCell(1).setCellStyle(dataStyle);

	        row.createCell(2).setCellValue(report.getDivisionName());
	        row.getCell(2).setCellStyle(dataStyle);

	        if (isRole3) {
	            addDataCells(row, 3, report.getSmsTotal(), report.getSmsComp(), report.getSmsPending(), totals[3], dataStyle);
	        } else if (isRole7 || isRole10) {
	            addDataCells(row, 3, report.getMiTotal(), report.getMiComp(), report.getMiPending(), totals[4], dataStyle);
	        } else {
	            // Full report for other roles
	            addDataCells(row, 3, report.getWebTotal(), report.getWebComp(), report.getWebPending(), totals[0], dataStyle);
	            addDataCells(row, 6, report.getMobileTotal(), report.getMobileComp(), report.getMobilePending(), totals[1], dataStyle);
	            addDataCells(row, 9, report.getAdminTotal(), report.getAdminComp(), report.getAdminPending(), totals[2], dataStyle);
	            addDataCells(row, 12, report.getSmsTotal(), report.getSmsComp(), report.getSmsPending(), totals[3], dataStyle);
	            addDataCells(row, 15, report.getMiTotal(), report.getMiComp(), report.getMiPending(), totals[4], dataStyle);

	            BigDecimal totalRevd = report.getWebTotal().add(report.getMobileTotal()).add(report.getAdminTotal())
	                    .add(report.getSmsTotal()).add(report.getMiTotal());
	            BigDecimal totalComp = report.getWebComp().add(report.getMobileComp()).add(report.getAdminComp())
	                    .add(report.getSmsComp()).add(report.getMiComp());
	            BigDecimal totalPend = report.getWebPending().add(report.getMobilePending()).add(report.getAdminPending())
	                    .add(report.getSmsPending()).add(report.getMiPending());
	            addDataCells(row, 18, totalRevd, totalComp, totalPend, totals[5], dataStyle);
	        }
	    }

	    // Total row
	    Row totalRow = sheet.createRow(rowNum);
	    totalRow.createCell(0).setCellValue("TOTAL");
	    totalRow.getCell(0).setCellStyle(totalStyle);

	    totalRow.createCell(1).setCellValue("");
	    totalRow.getCell(1).setCellStyle(totalStyle);
	    totalRow.createCell(2).setCellValue("");
	    totalRow.getCell(2).setCellStyle(totalStyle);

	    if (isRole3) {
	        for (int subCol = 0; subCol < 3; subCol++) {
	            Cell cell = totalRow.createCell(3 + subCol);
	            cell.setCellValue(totals[3][subCol].doubleValue());
	            cell.setCellStyle(totalStyle);
	        }
	    } else if (isRole7 || isRole10) {
	        for (int subCol = 0; subCol < 3; subCol++) {
	            Cell cell = totalRow.createCell(3 + subCol);
	            cell.setCellValue(totals[4][subCol].doubleValue());
	            cell.setCellStyle(totalStyle);
	        }
	    } else {
	        // Full report for other roles
	        for (int mode = 0; mode < 6; mode++) {
	            int startCol = 3 + (mode * 3);
	            for (int subCol = 0; subCol < 3; subCol++) {
	                Cell cell = totalRow.createCell(startCol + subCol);
	                cell.setCellValue(totals[mode][subCol].doubleValue());
	                cell.setCellStyle(totalStyle);
	            }
	        }
	    }

	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    workbook.write(outputStream);
	    workbook.close();

	    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
	    excelFile = DefaultStreamedContent.builder().name("Mode_Wise_Abstract_Report_For_A_DateRange_And_Category_SectionWise.xls")
	            .contentType("application/vnd.ms-excel").stream(() -> inputStream).build();
	}

	

	private String getComplaintTypeDisplay() {
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
	    
	    return complaintTypeMap.getOrDefault(complaintType, "-");
	}

	// CIRCLE REPORT PDF DOWNLOAD - FORMAT ONE - MODE WISE (ROLE BASED)
	public void exportCircleToPdfFormatOne(List<ModeWiseAbstractValueBean> reports)
	        throws IOException, DocumentException {
	    // Get user role information
	    HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
	    CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");
        AdminUserValueBean adminUserValueBean = (AdminUserValueBean) httpsession.getAttribute("sessionAdminValueBean");

        
	    boolean isRole3 = callCenterValueBean != null && callCenterValueBean.getRoleId() == 3;
	    boolean isRole7 = callCenterValueBean != null && callCenterValueBean.getRoleId() == 7;
	    boolean isRole10=adminUserValueBean!=null && adminUserValueBean.getRoleId()==10;

	    Document document = new Document(isRole3 || isRole7 || isRole10 ? PageSize.A4.rotate() : PageSize.A3.rotate());
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    PdfWriter.getInstance(document, outputStream);
	    document.open();

	    // Font styles
	    Font titleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
	    Font dateFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
	    Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
	    Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.BLACK);
	    Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
	    Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);

	    // Title
	    Paragraph title = new Paragraph("MODE WISE CIRCLE ABSTRACT REPORT", titleFont);
	    title.setAlignment(Element.ALIGN_CENTER);
	    title.setSpacingAfter(10f);
	    document.add(title);
	    
	    String complaintType = getComplaintTypeDisplay();

	    // Date range
	    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	    String fromDateStr = dmFilter.getFromDate() != null ? dateFormat.format(dmFilter.getFromDate()) : "N/A";
	    String toDateStr = dmFilter.getToDate() != null ? dateFormat.format(dmFilter.getToDate()) : "N/A";
	    Paragraph dateRange = new Paragraph("From Date: " + fromDateStr + "  To Date: " + toDateStr + "  Complaint Type :" + complaintType, dateFont);
	    dateRange.setAlignment(Element.ALIGN_CENTER);
	    dateRange.setSpacingAfter(15f);
	    document.add(dateRange);

	    // Create table with appropriate number of columns based on role
	    int columnCount = isRole3 || isRole7 || isRole10 ? 5 : 20; // S.NO + Circle + 3 data columns for roles 3/7
	    PdfPTable table = new PdfPTable(columnCount);
	    table.setWidthPercentage(100);
	    table.setSpacingBefore(10f);
	    table.setSpacingAfter(10f);

	    // Column widths
	    float[] columnWidths;
	    if (isRole3 || isRole7|| isRole10) {
	        columnWidths = new float[]{1.5f, 4f, 2.5f, 2.5f, 2.5f}; // Narrower for single mode
	    } else {
	        columnWidths = new float[20];
	        columnWidths[0] = 1.5f;
	        columnWidths[1] = 4f;
	        for (int i = 2; i < 20; i++) {
	            columnWidths[i] = 2.5f;
	        }
	    }
	    table.setWidths(columnWidths);

	    // Header row 1
	    PdfPCell sNoHeader = new PdfPCell(new Phrase("S.NO", headerFont));
	    sNoHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
	    sNoHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
	    sNoHeader.setBackgroundColor(new BaseColor(193,195,199));
	    sNoHeader.setRowspan(2);
	    table.addCell(sNoHeader);
	    
	    PdfPCell circleHeader = new PdfPCell(new Phrase("CIRCLE", headerFont));
	    circleHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
	    circleHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
	    circleHeader.setBackgroundColor(new BaseColor(193,195,199));
	    circleHeader.setRowspan(2);
	    table.addCell(circleHeader);

	    if (isRole3) {
	        // Role 3 - only show Social Media columns
	        PdfPCell smHeader = new PdfPCell(new Phrase("SOCIAL MEDIA (SM)", headerFont));
	        smHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
	        smHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
	        smHeader.setBackgroundColor(new BaseColor(193,195,199));
	        smHeader.setColspan(3);
	        table.addCell(smHeader);
	    } else if (isRole7 || isRole10) {
	        // Role 7 - only show MI columns
	        PdfPCell miHeader = new PdfPCell(new Phrase("MI", headerFont));
	        miHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
	        miHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
	        miHeader.setBackgroundColor(new BaseColor(193,195,199));
	        miHeader.setColspan(3);
	        table.addCell(miHeader);
	    } else {
	        // Full report for other roles
	        String[] modes = {"WEB", "MOBILE APP", "FOC", "SM", "MI", "TOTAL"};
	        for (String mode : modes) {
	            PdfPCell modeHeader = new PdfPCell(new Phrase(mode, headerFont));
	            modeHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
	            modeHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
	            modeHeader.setBackgroundColor(new BaseColor(193,195,199));
	            modeHeader.setColspan(3);
	            table.addCell(modeHeader);
	        }
	    }

	    // Header row 2 (subheaders)
	    if (!isRole3 && !isRole7 && !isRole10) {
	        // Only add subheaders for full report
	        for (int i = 0; i < 6; i++) {
	            String[] subColumns = {"Revd", "Comp", "Pend"};
	            for (String subCol : subColumns) {
	                PdfPCell subHeader = new PdfPCell(new Phrase(subCol, subHeaderFont));
	                subHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
	                subHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
	                subHeader.setBackgroundColor(new BaseColor(193,195,199));
	                table.addCell(subHeader);
	            }
	        }
	    } else {
	        // For roles 3/7, add subheaders for their single mode
	        String[] subColumns = {"Revd", "Comp", "Pend"};
	        for (String subCol : subColumns) {
	            PdfPCell subHeader = new PdfPCell(new Phrase(subCol, subHeaderFont));
	            subHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
	            subHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
	            subHeader.setBackgroundColor(new BaseColor(193,195,199));
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
	        // S.NO and Circle (same for all roles)
	        PdfPCell snoCell = new PdfPCell(new Phrase(String.valueOf(serialNumber++), dataFont));
	        snoCell.setPadding(5);
	        table.addCell(snoCell);
	        
	        PdfPCell circleCell = new PdfPCell(new Phrase(report.getCircleName(), dataFont));
	        circleCell.setPadding(5);
	        table.addCell(circleCell);

	        if (isRole3) {
	            // Role 3 - only show Social Media data
	            addPdfDataCells(table, report.getSmsTotal(), report.getSmsComp(), report.getSmsPending(), totals[3], dataFont);
	        } else if (isRole7 || isRole10) {
	            // Role 7 - only show MI data
	            addPdfDataCells(table, report.getMiTotal(), report.getMiComp(), report.getMiPending(), totals[4], dataFont);
	        } else {
	            // Full report for other roles
	            addPdfDataCells(table, report.getWebTotal(), report.getWebComp(), report.getWebPending(), totals[0], dataFont);
	            addPdfDataCells(table, report.getMobileTotal(), report.getMobileComp(), report.getMobilePending(), totals[1], dataFont);
	            addPdfDataCells(table, report.getAdminTotal(), report.getAdminComp(), report.getAdminPending(), totals[2], dataFont);
	            addPdfDataCells(table, report.getSmsTotal(), report.getSmsComp(), report.getSmsPending(), totals[3], dataFont);
	            addPdfDataCells(table, report.getMiTotal(), report.getMiComp(), report.getMiPending(), totals[4], dataFont);

	            BigDecimal totalRevd = report.getWebTotal().add(report.getMobileTotal()).add(report.getAdminTotal())
	                    .add(report.getSmsTotal()).add(report.getMiTotal());
	            BigDecimal totalComp = report.getWebComp().add(report.getMobileComp()).add(report.getAdminComp())
	                    .add(report.getSmsComp()).add(report.getMiComp());
	            BigDecimal totalPend = report.getWebPending().add(report.getMobilePending()).add(report.getAdminPending())
	                    .add(report.getSmsPending()).add(report.getMiPending());
	            addPdfDataCells(table, totalRevd, totalComp, totalPend, totals[5], dataFont);
	        }
	    }

	    // Total row
	    PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL", totalFont));
	    totalLabelCell.setBackgroundColor(new BaseColor(193,195,199));
	    totalLabelCell.setPadding(5);
	    totalLabelCell.setColspan(2);
	    table.addCell(totalLabelCell);

	    if (isRole3) {
	        // Role 3 - only show Social Media totals
	        for (int i = 0; i < 3; i++) {
	            PdfPCell totalCell = new PdfPCell(new Phrase(totals[3][i].toString(), totalFont));
	            totalCell.setBackgroundColor(new BaseColor(193,195,199));
	            totalCell.setHorizontalAlignment(Element.ALIGN_LEFT);
	            totalCell.setPadding(5);
	            table.addCell(totalCell);
	        }
	    } else if (isRole7 || isRole10) {
	        // Role 7 - only show MI totals
	        for (int i = 0; i < 3; i++) {
	            PdfPCell totalCell = new PdfPCell(new Phrase(totals[4][i].toString(), totalFont));
	            totalCell.setBackgroundColor(new BaseColor(193,195,199));
	            totalCell.setHorizontalAlignment(Element.ALIGN_LEFT);
	            totalCell.setPadding(5);
	            table.addCell(totalCell);
	        }
	    } else {
	        // Full report for other roles
	        for (int mode = 0; mode < 6; mode++) {
	            for (int subCol = 0; subCol < 3; subCol++) {
	                PdfPCell totalCell = new PdfPCell(new Phrase(totals[mode][subCol].toString(), totalFont));
	                totalCell.setBackgroundColor(new BaseColor(193,195,199));
	                totalCell.setHorizontalAlignment(Element.ALIGN_LEFT);
	                totalCell.setPadding(5);
	                table.addCell(totalCell);
	            }
	        }
	    }

	    document.add(table);
	    document.close();

	    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
	    pdfFile = DefaultStreamedContent.builder().name("Mode_Wise_Abstract_Report_For_A_DateRange_And_Category_CircleWise.pdf")
	            .contentType("application/pdf").stream(() -> inputStream).build();
	}

	// SECTION REPORT PDF DOWNLOAD - FORMAT ONE - MODE WISE (ROLE BASED)
	public void exportSectionToPdfFormatOne(List<ModeWiseAbstractValueBean> reports)
	        throws IOException, DocumentException {
	    // Get user role information
	    HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
	    CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");
        AdminUserValueBean adminUserValueBean = (AdminUserValueBean) httpsession.getAttribute("sessionAdminValueBean");

	    boolean isRole3 = callCenterValueBean != null && callCenterValueBean.getRoleId() == 3;
	    boolean isRole7 = callCenterValueBean != null && callCenterValueBean.getRoleId() == 7;
	    boolean isRole10=adminUserValueBean!=null && adminUserValueBean.getRoleId()==10;

	    Document document = new Document(isRole3 || isRole7 || isRole10 ? PageSize.A4.rotate() : PageSize.A3.rotate());
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    PdfWriter.getInstance(document, outputStream);
	    document.open();

	    // Font styles
	    Font titleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
	    Font dateFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
	    Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
	    Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.BLACK);
	    Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
	    Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);

	    // Title
	    Paragraph title = new Paragraph("MODE WISE SECTION ABSTRACT REPORT - FORMAT 2", titleFont);
	    title.setAlignment(Element.ALIGN_CENTER);
	    title.setSpacingAfter(10f);
	    document.add(title);
	    
	    String complaintType = getComplaintTypeDisplay();

	    // Date range
	    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	    String fromDateStr = dmFilter.getFromDate() != null ? dateFormat.format(dmFilter.getFromDate()) : "N/A";
	    String toDateStr = dmFilter.getToDate() != null ? dateFormat.format(dmFilter.getToDate()) : "N/A";
	    Paragraph dateRange = new Paragraph("From Date: " + fromDateStr + "  To Date: " + toDateStr + " Complaint Type:" + complaintType, dateFont);
	    dateRange.setAlignment(Element.ALIGN_CENTER);
	    dateRange.setSpacingAfter(15f);
	    document.add(dateRange);

	    // Create table with appropriate number of columns based on role
	    int columnCount = isRole3 || isRole7 || isRole10 ? 6 : 21; // S.NO + Section + Division + 3 data columns for roles 3/7
	    PdfPTable table = new PdfPTable(columnCount);
	    table.setWidthPercentage(100);
	    table.setSpacingBefore(10f);
	    table.setSpacingAfter(10f);

	    // Column widths
	    float[] columnWidths;
	    if (isRole3 || isRole7 || isRole10) {
	        columnWidths = new float[]{1.5f, 5f, 5f, 2.2f, 2.2f, 2.2f};
	    } else {
	        columnWidths = new float[21];
	        columnWidths[0] = 1.5f;
	        columnWidths[1] = 5f;
	        columnWidths[2] = 5f;
	        for (int i = 3; i < 21; i++) {
	            columnWidths[i] = 2.2f;
	        }
	    }
	    table.setWidths(columnWidths);

	    // Header row 1
	    PdfPCell snoHeader = new PdfPCell(new Phrase("S.NO", headerFont));
	    snoHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
	    snoHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
	    snoHeader.setBackgroundColor(new BaseColor(193,195,199));
	    snoHeader.setRowspan(2);
	    table.addCell(snoHeader);
	    
	    PdfPCell sectionHeader = new PdfPCell(new Phrase("SECTION", headerFont));
	    sectionHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
	    sectionHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
	    sectionHeader.setBackgroundColor(new BaseColor(193,195,199));
	    sectionHeader.setRowspan(2);
	    table.addCell(sectionHeader);

	    PdfPCell divisionHeader = new PdfPCell(new Phrase("DIVISION", headerFont));
	    divisionHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
	    divisionHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
	    divisionHeader.setBackgroundColor(new BaseColor(193,195,199));
	    divisionHeader.setRowspan(2);
	    table.addCell(divisionHeader);

	    if (isRole3) {
	        // Role 3 - only show Social Media columns
	        PdfPCell smHeader = new PdfPCell(new Phrase("SOCIAL MEDIA (SM)", headerFont));
	        smHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
	        smHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
	        smHeader.setBackgroundColor(new BaseColor(193,195,199));
	        smHeader.setColspan(3);
	        table.addCell(smHeader);
	    } else if (isRole7 || isRole10) {
	        // Role 7 - only show MI columns
	        PdfPCell miHeader = new PdfPCell(new Phrase("MI", headerFont));
	        miHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
	        miHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
	        miHeader.setBackgroundColor(new BaseColor(193,195,199));
	        miHeader.setColspan(3);
	        table.addCell(miHeader);
	    } else {
	        // Full report for other roles
	        String[] modes = {"WEB", "MOBILE APP", "FOC", "SM", "MI", "TOTAL"};
	        for (String mode : modes) {
	            PdfPCell modeHeader = new PdfPCell(new Phrase(mode, headerFont));
	            modeHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
	            modeHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
	            modeHeader.setBackgroundColor(new BaseColor(193,195,199));
	            modeHeader.setColspan(3);
	            table.addCell(modeHeader);
	        }
	    }

	    // Header row 2 (subheaders)
	    if (!isRole3 && !isRole7 && !isRole10) {
	        // Only add subheaders for full report
	        for (int i = 0; i < 6; i++) {
	            String[] subColumns = {"Revd", "Comp", "Pend"};
	            for (String subCol : subColumns) {
	                PdfPCell subHeader = new PdfPCell(new Phrase(subCol, subHeaderFont));
	                subHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
	                subHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
	                subHeader.setBackgroundColor(new BaseColor(193,195,199));
	                table.addCell(subHeader);
	            }
	        }
	    } else {
	        // For roles 3/7, add subheaders for their single mode
	        String[] subColumns = {"Revd", "Comp", "Pend"};
	        for (String subCol : subColumns) {
	            PdfPCell subHeader = new PdfPCell(new Phrase(subCol, subHeaderFont));
	            subHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
	            subHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
	            subHeader.setBackgroundColor(new BaseColor(193,195,199));
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
	        // S.NO, Section, Division (same for all roles)
	        PdfPCell snoCell = new PdfPCell(new Phrase(String.valueOf(serialNumber++), dataFont));
	        snoCell.setPadding(5);
	        table.addCell(snoCell);
	        
	        PdfPCell sectionCell = new PdfPCell(new Phrase(report.getSectionName(), dataFont));
	        sectionCell.setPadding(5);
	        table.addCell(sectionCell);

	        PdfPCell divisionCell = new PdfPCell(new Phrase(report.getDivisionName(), dataFont));
	        divisionCell.setPadding(5);
	        table.addCell(divisionCell);

	        if (isRole3) {
	            // Role 3 - only show Social Media data
	            addPdfDataCells(table, report.getSmsTotal(), report.getSmsComp(), report.getSmsPending(), totals[3], dataFont);
	        } else if (isRole7 || isRole10) {
	            // Role 7 - only show MI data
	            addPdfDataCells(table, report.getMiTotal(), report.getMiComp(), report.getMiPending(), totals[4], dataFont);
	        } else {
	            // Full report for other roles
	            addPdfDataCells(table, report.getWebTotal(), report.getWebComp(), report.getWebPending(), totals[0], dataFont);
	            addPdfDataCells(table, report.getMobileTotal(), report.getMobileComp(), report.getMobilePending(), totals[1], dataFont);
	            addPdfDataCells(table, report.getAdminTotal(), report.getAdminComp(), report.getAdminPending(), totals[2], dataFont);
	            addPdfDataCells(table, report.getSmsTotal(), report.getSmsComp(), report.getSmsPending(), totals[3], dataFont);
	            addPdfDataCells(table, report.getMiTotal(), report.getMiComp(), report.getMiPending(), totals[4], dataFont);

	            BigDecimal totalRevd = report.getWebTotal().add(report.getMobileTotal()).add(report.getAdminTotal())
	                    .add(report.getSmsTotal()).add(report.getMiTotal());
	            BigDecimal totalComp = report.getWebComp().add(report.getMobileComp()).add(report.getAdminComp())
	                    .add(report.getSmsComp()).add(report.getMiComp());
	            BigDecimal totalPend = report.getWebPending().add(report.getMobilePending()).add(report.getAdminPending())
	                    .add(report.getSmsPending()).add(report.getMiPending());
	            addPdfDataCells(table, totalRevd, totalComp, totalPend, totals[5], dataFont);
	        }
	    }

	    // Total row
	    PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL", totalFont));
	    totalLabelCell.setBackgroundColor(new BaseColor(193,195,199));
	    totalLabelCell.setColspan(3);
	    totalLabelCell.setPadding(5);
	    table.addCell(totalLabelCell);

	    if (isRole3) {
	        // Role 3 - only show Social Media totals
	        for (int i = 0; i < 3; i++) {
	            PdfPCell totalCell = new PdfPCell(new Phrase(totals[3][i].toString(), totalFont));
	            totalCell.setBackgroundColor(new BaseColor(193,195,199));
	            totalCell.setHorizontalAlignment(Element.ALIGN_LEFT);
	            totalCell.setPadding(5);
	            table.addCell(totalCell);
	        }
	    } else if (isRole7 || isRole10) {
	        // Role 7 - only show MI totals
	        for (int i = 0; i < 3; i++) {
	            PdfPCell totalCell = new PdfPCell(new Phrase(totals[4][i].toString(), totalFont));
	            totalCell.setBackgroundColor(new BaseColor(193,195,199));
	            totalCell.setHorizontalAlignment(Element.ALIGN_LEFT);
	            totalCell.setPadding(5);
	            table.addCell(totalCell);
	        }
	    } else {
	        // Full report for other roles
	        for (int mode = 0; mode < 6; mode++) {
	            for (int subCol = 0; subCol < 3; subCol++) {
	                PdfPCell totalCell = new PdfPCell(new Phrase(totals[mode][subCol].toString(), totalFont));
	                totalCell.setBackgroundColor(new BaseColor(193,195,199));
	                totalCell.setHorizontalAlignment(Element.ALIGN_LEFT);
	                totalCell.setPadding(5);
	                table.addCell(totalCell);
	            }
	        }
	    }

	    document.add(table);
	    document.close();

	    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
	    pdfFile = DefaultStreamedContent.builder().name("Mode_Wise_Abstract_Report_For_A_DateRange_And_Category_SectionWise.pdf")
	            .contentType("application/pdf").stream(() -> inputStream).build();
	}

	// Helper method to add data cells to PDF table
	private void addPdfDataCells(PdfPTable table, BigDecimal revd, BigDecimal comp, BigDecimal pend,
	        BigDecimal[] modeTotals, Font font) {
	    // Add Revd
	    PdfPCell revdCell = new PdfPCell(new Phrase(revd != null ? revd.toString() : "0", font));
	    revdCell.setPadding(5);
	    table.addCell(revdCell);
	    if (revd != null) {
	        modeTotals[0] = modeTotals[0].add(revd);
	    }

	    // Add Comp
	    PdfPCell compCell = new PdfPCell(new Phrase(comp != null ? comp.toString() : "0", font));
	    compCell.setPadding(5);
	    table.addCell(compCell);
	    if (comp != null) {
	        modeTotals[1] = modeTotals[1].add(comp);
	    }

	    // Add Pend
	    PdfPCell pendCell = new PdfPCell(new Phrase(pend != null ? pend.toString() : "0", font));
	    pendCell.setPadding(5);
	    table.addCell(pendCell);
	    if (pend != null) {
	        modeTotals[2] = modeTotals[2].add(pend);
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
	               + "(CASE WHEN a.status_id=2 then TO_CHAR(a.updated_on, 'DD-MM-YYYY-HH24:MI') else '' end) AS Attended_Date, " 
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
		Sheet sheet = workbook.createSheet("Mode_Wise_Complaint_List_Report");

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

	public List<ModeWiseAbstractValueBean> getcircleList() {
		return circleList;
	}

	public void setcircleList(List<ModeWiseAbstractValueBean> circleList) {
		this.circleList = circleList;
	}

	public List<ModeWiseAbstractValueBean> getsectionList() {
		return sectionList;
	}

	public void setsectionList(List<ModeWiseAbstractValueBean> sectionList) {
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

	public boolean isCameFromInsideReport() {
		return cameFromInsideReport;
	}

	public void setCameFromInsideReport(boolean cameFromInsideReport) {
		this.cameFromInsideReport = cameFromInsideReport;
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
