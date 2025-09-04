package tneb.ccms.admin.controller.reportController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
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
import java.util.stream.IntStream;

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
import com.itextpdf.text.Rectangle;
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
import tneb.ccms.admin.valuebeans.TmpYearWiseCircle;
import tneb.ccms.admin.valuebeans.TmpYearWiseSections;
import tneb.ccms.admin.valuebeans.ViewComplaintReportValueBean;

import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;

@Named
@ViewScoped
public class HourWiseAbstractReport implements Serializable {

	private static final long serialVersionUID = 1L;
	private SessionFactory sessionFactory;
	DataModel dmFilter = new DataModel();
	List<TmpYearWiseCircle> circleList = new ArrayList<>();
	List<TmpYearWiseSections> sectionList = new ArrayList<>();
	private boolean initialized = false;
	private boolean cameFromInsideSection = false;
	private boolean cameFromInsideReport = false;
	private boolean cameFromDateWiseAbstractReport = false;
	StreamedContent excelFile;
	StreamedContent pdfFile;
	List<ViewComplaintReportValueBean> complaintList = new ArrayList<>();
	String selectedCircleName = null;
	String selectedSectionName = null;
	ViewComplaintReportValueBean selectedComplaintId;
	String redirectFrom;
	private Date currentDate = new Date();
	private String currentYear = String.valueOf(Year.now().getValue());
	private String selectedDateFromDateWiseAbstract;
	private String selectedComplaintType;
	private String selectedDevice;
	private String selectedCircleIdFromDateWiseAbstract;
	private String selectedCircleNameFromDateWiseAbstract;
	private boolean cameViaCircleReport=false;

	List<CompDeviceValueBean> devices;
	List<CategoriesValueBean> categories;
	

	@PostConstruct
	public void init() {
		System.out.println("INITIALIZING HOUR WISE ABSTRACT REPORT...");
		sessionFactory = HibernateUtil.getSessionFactory();
		loadAllDevicesAndCategories();
	}
	
	public void clearFilterAndPage() {
		dmFilter = new DataModel();
		circleList = new ArrayList<>();
	}
	
	// REFERSH CIRCLE REPORT
	public void resetIfNeeded() {
	    if (!FacesContext.getCurrentInstance().isPostback() && !cameFromInsideReport) {
	        circleList = null;
	        dmFilter.setFromDate(null);
	        dmFilter.setToDate(null);
	        dmFilter.setComplaintType(null);
	        dmFilter.setDevice(null);
	    }
	    cameFromInsideReport = false; 
	}

	
	// REFRESH SECTION REPORT
	public void resetSectionIfNeeded() {
		
	    if (!FacesContext.getCurrentInstance().isPostback() && !cameFromInsideSection ) {
	        sectionList = null;
	        dmFilter.setFromDate(null);
	        dmFilter.setToDate(null);
	    }
	    cameFromInsideSection = false;
	}
	
	
	 public String goToHourlySplitUpCircleAbstract() throws ParseException {
	        
	        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
	        formatter.setLenient(false); 
	        formatter.parse(selectedDateFromDateWiseAbstract);
	        
	         dmFilter.setFromDate(formatter.parse(selectedDateFromDateWiseAbstract));
	         
	         this.dmFilter.setComplaintType(selectedComplaintType);
	         this.dmFilter.setDevice(selectedDevice);
	         

	         searchCircleHourWiseAbstract();
	         
	         this.cameFromInsideReport=true;
	         this.cameFromDateWiseAbstractReport=true;

	        try {
	            FacesContext.getCurrentInstance().getExternalContext()
	                .redirect("hourWiseCircleAbstract.xhtml");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	        return null;
	    }
	 
	 public void goToHourlySplitUpSectionAbstract() throws ParseException {
         this.cameFromDateWiseAbstractReport=true;
		 System.out.println("Selected Month from f:setPropertyActionListener: " + selectedDateFromDateWiseAbstract);
	        
	        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
	        formatter.setLenient(false); 
	        formatter.parse(selectedDateFromDateWiseAbstract);
	        
	         this.dmFilter.setFromDate(formatter.parse(selectedDateFromDateWiseAbstract));
	         
	         this.dmFilter.setComplaintType(selectedComplaintType);
	         this.dmFilter.setDevice(selectedDevice);
	         
	         System.err.println("THE FROM DATE FROM MONTHLY ABSTRACT----------"+dmFilter.getFromDate());
	         
	         HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
		                .getExternalContext().getSession(false);
				
				AdminUserValueBean adminUserValueBean = (AdminUserValueBean) 
						httpsession.getAttribute("sessionAdminValueBean");
				
				if(adminUserValueBean!=null &&  adminUserValueBean.getRoleId()>=1 && adminUserValueBean.getRoleId()<=3) {
					fetchReportForSectionUsers();
				}
				else {
			         fetchReportByCircle(selectedCircleIdFromDateWiseAbstract,selectedCircleNameFromDateWiseAbstract);
				}
	         
	         this.cameFromInsideReport=true;

	         

	    }
	 
	 public void goToDateWiseCircleAbstract() throws IOException {
		 this.cameFromDateWiseAbstractReport=false;
		 this.cameViaCircleReport=false;
		 FacesContext.getCurrentInstance().getExternalContext()
         .redirect("dateWiseCircleAbstract.xhtml");
	 }
	 public void goToDateWiseSectionAbstract() throws IOException {
		 this.cameFromDateWiseAbstractReport=false;
		 FacesContext.getCurrentInstance().getExternalContext()
         .redirect("dateWiseSectionAbstract.xhtml");
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
	                dmFilter.setDevice("MI");
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
	
	
		//CIRCLE REPORT
		@Transactional
		public void searchCircleHourWiseAbstract() {
			
			
			updateLoginWiseFilters();
			Session session = null;
			
			 if (dmFilter.getFromDate() != null) {
			        Calendar cal = Calendar.getInstance();
			        cal.setTime(dmFilter.getFromDate());
			        
			        Calendar currentCal = Calendar.getInstance();
			        currentCal.setTime(new Date());
		        
		        if (cal.get(Calendar.YEAR) > currentCal.get(Calendar.YEAR) || 
		                (cal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) && 
		                		cal.get(Calendar.MONTH) > currentCal.get(Calendar.MONTH))) {
		                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
		                    "Error", "Cannot select future month/year");
		                FacesContext.getCurrentInstance().addMessage(null, message);
		                return;
		            }
		        }
			
			if (dmFilter.getFromDate() == null) {
				Date now = new Date();		
				dmFilter.setFromDate(now);
			}
			if(dmFilter.getHours()==null) {
				dmFilter.setHours("24");
			}
	
			try {
				session = sessionFactory.openSession();
				session.beginTransaction();
	
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String formattedFromDate = sdf.format(dmFilter.getFromDate());
	
		        
		        if(dmFilter.getComplaintType()==null) {
		        	dmFilter.setComplaintType("AL");
		        }
		        if(dmFilter.getDevice()==null) {
		        	dmFilter.setDevice("L");
		        }
				if(dmFilter.getHours()==null) {
					dmFilter.setHours("24");
				}
				
				System.out.println("COMPLAINT TYPE------------------"+dmFilter.getComplaintType());
				System.out.println("Device-----------------"+dmFilter.getDevice());

	
				session.createNativeQuery(
						"BEGIN cat_dev_hour_abst(:regionCode,:circleCode,:complaintCode,:device,TO_DATE(:fromDate, 'YYYY-MM-DD'), :hours); END;")
						.setParameter("regionCode", dmFilter.getRegionCode())
						.setParameter("circleCode", dmFilter.getCircleCode())
						.setParameter("complaintCode", dmFilter.getComplaintType())
			            .setParameter("device", dmFilter.getDevice())
						.setParameter("fromDate", formattedFromDate)
						.setParameter("hours", dmFilter.getHours())
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
		        		+ "c.TOT1,c.CPL1,(c.LIVE1+c.TMP1),"
		        		+ "c.TOT2,c.CPL2,(c.LIVE2+c.TMP2),"
		        		+ "c.TOT3,c.CPL3,(c.LIVE3+c.TMP3),"
		        		+ "c.TOT4,c.CPL4,(c.LIVE4+c.TMP4),"
		        		+ "c.TOT5,c.CPL5,(c.LIVE5+c.TMP5),"
		        		+ "c.TOT6,c.CPL6,(c.LIVE6+c.TMP6),"
		        		+ "c.TOT7,c.CPL7,(c.LIVE7+c.TMP7),"
		        		+ "c.TOT8,c.CPL8,(c.LIVE8+c.TMP8),"
		        		+ "c.TOT9,c.CPL9,(c.LIVE9+c.TMP9),"
		        		+ "c.TOT10,c.CPL10,(c.LIVE10+c.TMP10),"
		        		+ "c.TOT11,c.CPL11,(c.LIVE11+c.TMP11),"
		        		+ "c.TOT12,c.CPL12,(c.LIVE12+c.TMP12),"
		        		+ "c.TOT13,c.CPL13,(c.LIVE13+c.TMP13),"
		        		+ "c.TOT14,c.CPL14,(c.LIVE14+c.TMP14),"
		        		+ "c.TOT15,c.CPL15,(c.LIVE15+c.TMP15),"
		        		+ "c.TOT16,c.CPL16,(c.LIVE16+c.TMP16),"
		        		+ "c.TOT17,c.CPL17,(c.LIVE17+c.TMP17),"
		        		+ "c.TOT18,c.CPL18,(c.LIVE18+c.TMP18),"
		        		+ "c.TOT19,c.CPL19,(c.LIVE19+c.TMP19),"
		        		+ "c.TOT20,c.CPL20,(c.LIVE20+c.TMP20),"
		        		+ "c.TOT21,c.CPL21,(c.LIVE21+c.TMP21),"
		        		+ "c.TOT22,c.CPL22,(c.LIVE22+c.TMP22),"
		        		+ "c.TOT23,c.CPL23,(c.LIVE23+c.TMP23),"
		        		+ "c.TOT24,c.CPL24,(c.LIVE24+c.TMP24),"
		        		+ "cir.name as CircleName,reg.name as RegionName FROM TMP_CT_DEV_MONABST_CIR c,Circle cir,Region reg "
		        		+ "WHERE cir.id =c.cirCode and reg.id =c.regCode";																																																																					
																																																																						
				List<Object[]> results = session.createNativeQuery(hql).getResultList();
				List<TmpYearWiseCircle> result = new ArrayList<TmpYearWiseCircle>();
				circleList = new ArrayList<>();
	
				for (Object[] row : results) {
		        	TmpYearWiseCircle report = new TmpYearWiseCircle();
					report.setRegCode((String) row[0]);
					report.setCirCode((String) row[1]);
					
					report.setTot1((BigDecimal) row[2]);
					report.setCpl1((BigDecimal) row[3]);
					report.setPend1((BigDecimal)row[4]);
					
					report.setTot2((BigDecimal) row[5]);
					report.setCpl2((BigDecimal) row[6]);
					report.setPend2((BigDecimal)row[7]);
					
					report.setTot3((BigDecimal) row[8]);
					report.setCpl3((BigDecimal) row[9]);
					report.setPend3((BigDecimal)row[10]);
				
					report.setTot4((BigDecimal) row[11]);
					report.setCpl4((BigDecimal) row[12]);
					report.setPend4((BigDecimal)row[13]);
				
					report.setTot5((BigDecimal) row[14]);
					report.setCpl5((BigDecimal) row[15]);
					report.setPend5((BigDecimal)row[16]);
					
					report.setTot6((BigDecimal) row[17]);
					report.setCpl6((BigDecimal) row[18]);
					report.setPend6((BigDecimal)row[19]);
					
					report.setTot7((BigDecimal) row[20]);
					report.setCpl7((BigDecimal) row[21]);
					report.setPend7((BigDecimal)row[22]);
					
					report.setTot8((BigDecimal) row[23]);
					report.setCpl8((BigDecimal) row[24]);
					report.setPend8((BigDecimal)row[25]);
					
					report.setTot9((BigDecimal) row[26]);
					report.setCpl9((BigDecimal) row[27]);
					report.setPend9((BigDecimal)row[28]);
					
					report.setTot10((BigDecimal) row[29]);
					report.setCpl10((BigDecimal) row[30]);
					report.setPend10((BigDecimal)row[31]);
					
					report.setTot11((BigDecimal) row[32]);
					report.setCpl11((BigDecimal) row[33]);
					report.setPend11((BigDecimal)row[34]);
					
					report.setTot12((BigDecimal) row[35]);
					report.setCpl12((BigDecimal) row[36]);
					report.setPend12((BigDecimal)row[37]);
					
					report.setTot13((BigDecimal) row[38]);
					report.setCpl13((BigDecimal) row[39]);
					report.setPend13((BigDecimal)row[40]);
					
					report.setTot14((BigDecimal) row[41]);
					report.setCpl14((BigDecimal) row[42]);
					report.setPend14((BigDecimal)row[43]);
					
					report.setTot15((BigDecimal) row[44]);
					report.setCpl15((BigDecimal) row[45]);
					report.setPend15((BigDecimal)row[46]);
					
					report.setTot16((BigDecimal) row[47]);
					report.setCpl16((BigDecimal) row[48]);
					report.setPend16((BigDecimal)row[49]);
					
					report.setTot17((BigDecimal) row[50]);
					report.setCpl17((BigDecimal) row[51]);
					report.setPend17((BigDecimal)row[52]);
					
					report.setTot18((BigDecimal) row[53]);
					report.setCpl18((BigDecimal) row[54]);
					report.setPend18((BigDecimal)row[55]);
					
					report.setTot19((BigDecimal) row[56]);
					report.setCpl19((BigDecimal) row[57]);
					report.setPend19((BigDecimal)row[58]);
					
					report.setTot20((BigDecimal) row[59]);
					report.setCpl20((BigDecimal) row[60]);
					report.setPend20((BigDecimal)row[61]);
					
					report.setTot21((BigDecimal) row[62]);
					report.setCpl21((BigDecimal) row[63]);
					report.setPend21((BigDecimal)row[64]);
					
					report.setTot22((BigDecimal) row[65]);
					report.setCpl22((BigDecimal) row[66]);
					report.setPend22((BigDecimal)row[67]);
					
					report.setTot23((BigDecimal) row[68]);
					report.setCpl23((BigDecimal) row[69]);
					report.setPend23((BigDecimal)row[70]);
					
					report.setTot24((BigDecimal) row[71]);
					report.setCpl24((BigDecimal) row[72]);
					report.setPend24((BigDecimal)row[73]);				
					
					report.setCircleName((String)row[74]);
					report.setRegionName((String)row[75]);
							 
	
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
					circleList = result;
				}else {
					if(dmFilter.getCircleCode().equals("A")) {
						circleList = result.stream().filter(a -> a.getRegCode().equals(dmFilter.getRegionCode())).collect(Collectors.toList());
					}else {
						circleList = result.stream().filter(a -> a.getRegCode().equals(dmFilter.getRegionCode()))
								.filter(c ->c.getCirCode().equals(dmFilter.getCircleCode()))
								.collect(Collectors.toList());
					}
				}
				this.cameViaCircleReport=true;
	
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error in fetching report");
			}
		}
	
	public void redirectToCircleReport() throws IOException {
		
		FacesContext.getCurrentInstance().getExternalContext().redirect("hourWiseCircleAbstract.xhtml");
		
		
	}
	public void redirectToSectionReport() throws IOException {
	 if("circle".equals(redirectFrom)) {
			FacesContext.getCurrentInstance().getExternalContext().redirect("hourWiseCircleAbstract.xhtml");
	 }else {
			FacesContext.getCurrentInstance().getExternalContext().redirect("hourWiseSectionAbstract.xhtml");
	 }
		
	}
	
	//SECTION REPORT

	@Transactional
	public void fetchReportByCircle(String circleCode,String circleName) {

		Session session = null;
		
		try {
			session = sessionFactory.openSession();
			session.beginTransaction();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String formattedFromDate = sdf.format(dmFilter.getFromDate());
			
	        if(dmFilter.getComplaintType()==null) {
	        	dmFilter.setComplaintType("AL");
	        }
	        if(dmFilter.getDevice()==null) {
	        	dmFilter.setDevice("L");
	        }
	        if(dmFilter.getHours()==null) {
	        	dmFilter.setHours("24");
	        }

			if(dmFilter.getSectionCode()==null) {
				dmFilter.setSectionCode("A");
			}
			
			System.out.println("ct-----------"+dmFilter.getComplaintType());
			System.out.println("ct-----------"+dmFilter.getSectionCode());
			System.out.println("ct-----------"+ dmFilter.getDevice());
			System.out.println("ct-----------"+ formattedFromDate);
			System.err.println("cc---------------"+circleCode);


			session.createNativeQuery(
					"BEGIN cat_dev_hour_secabst(:circd,:sectionCode,:complaintType,:device, TO_DATE(:fromDate, 'YYYY-MM-DD'),:hour); END;")
					.setParameter("circd", circleCode)
					.setParameter("sectionCode", dmFilter.getSectionCode())
					.setParameter("complaintType", dmFilter.getComplaintType())
					.setParameter("device", dmFilter.getDevice())
					.setParameter("fromDate", formattedFromDate)
					.setParameter("hour", dmFilter.getHours())
					.executeUpdate();

			session.flush();
			session.getTransaction().commit();
			
			String hql ="SELECT c.CIRCODE,c.SECCODE,"
					+ "c.TOT1,c.CPL1,(c.LIVE1+c.TMP1),"
	        		+ "c.TOT2,c.CPL2,(c.LIVE2+c.TMP2),"
	        		+ "c.TOT3,c.CPL3,(c.LIVE3+c.TMP3),"
	        		+ "c.TOT4,c.CPL4,(c.LIVE4+c.TMP4),"
	        		+ "c.TOT5,c.CPL5,(c.LIVE5+c.TMP5),"
	        		+ "c.TOT6,c.CPL6,(c.LIVE6+c.TMP6),"
	        		+ "c.TOT7,c.CPL7,(c.LIVE7+c.TMP7),"
	        		+ "c.TOT8,c.CPL8,(c.LIVE8+c.TMP8),"
	        		+ "c.TOT9,c.CPL9,(c.LIVE9+c.TMP9),"
	        		+ "c.TOT10,c.CPL10,(c.LIVE10+c.TMP10),"
	        		+ "c.TOT11,c.CPL11,(c.LIVE11+c.TMP11),"
	        		+ "c.TOT12,c.CPL12,(c.LIVE12+c.TMP12),"
	        		+ "c.TOT13,c.CPL13,(c.LIVE13+c.TMP13),"
	        		+ "c.TOT14,c.CPL14,(c.LIVE14+c.TMP14),"
	        		+ "c.TOT15,c.CPL15,(c.LIVE15+c.TMP15),"
	        		+ "c.TOT16,c.CPL16,(c.LIVE16+c.TMP16),"
	        		+ "c.TOT17,c.CPL17,(c.LIVE17+c.TMP17),"
	        		+ "c.TOT18,c.CPL18,(c.LIVE18+c.TMP18),"
	        		+ "c.TOT19,c.CPL19,(c.LIVE19+c.TMP19),"
	        		+ "c.TOT20,c.CPL20,(c.LIVE20+c.TMP20),"
	        		+ "c.TOT21,c.CPL21,(c.LIVE21+c.TMP21),"
	        		+ "c.TOT22,c.CPL22,(c.LIVE22+c.TMP22),"
	        		+ "c.TOT23,c.CPL23,(c.LIVE23+c.TMP23),"
	        		+ "c.TOT24,c.CPL24,(c.LIVE24+c.TMP24),"
	        		+ "cir.name as CircleName,sec.name as SectionName,d.NAME FROM TMP_CT_DEV_MONABST_SEC c,Circle cir,Section sec,Division d "
	        		+ "WHERE cir.id =c.cirCode and sec.id =c.secCode and d.id =sec.DIVISION_ID ";

			
	        List<Object[]> results = session.createNativeQuery(hql).getResultList();

	        sectionList = new ArrayList<>();
	        
	        for (Object[] row : results) {
	        	TmpYearWiseSections report = new TmpYearWiseSections();
				report.setCirCode((String) row[0]);
				report.setSecCode((String) row[1]);
				
				report.setTot1((BigDecimal) row[2]);
				report.setCpl1((BigDecimal) row[3]);
				report.setPend1((BigDecimal)row[4]);
				
				report.setTot2((BigDecimal) row[5]);
				report.setCpl2((BigDecimal) row[6]);
				report.setPend2((BigDecimal)row[7]);
				
				report.setTot3((BigDecimal) row[8]);
				report.setCpl3((BigDecimal) row[9]);
				report.setPend3((BigDecimal)row[10]);
			
				report.setTot4((BigDecimal) row[11]);
				report.setCpl4((BigDecimal) row[12]);
				report.setPend4((BigDecimal)row[13]);
			
				report.setTot5((BigDecimal) row[14]);
				report.setCpl5((BigDecimal) row[15]);
				report.setPend5((BigDecimal)row[16]);
				
				report.setTot6((BigDecimal) row[17]);
				report.setCpl6((BigDecimal) row[18]);
				report.setPend6((BigDecimal)row[19]);
				
				report.setTot7((BigDecimal) row[20]);
				report.setCpl7((BigDecimal) row[21]);
				report.setPend7((BigDecimal)row[22]);
				
				report.setTot8((BigDecimal) row[23]);
				report.setCpl8((BigDecimal) row[24]);
				report.setPend8((BigDecimal)row[25]);
				
				report.setTot9((BigDecimal) row[26]);
				report.setCpl9((BigDecimal) row[27]);
				report.setPend9((BigDecimal)row[28]);
				
				report.setTot10((BigDecimal) row[29]);
				report.setCpl10((BigDecimal) row[30]);
				report.setPend10((BigDecimal)row[31]);
				
				report.setTot11((BigDecimal) row[32]);
				report.setCpl11((BigDecimal) row[33]);
				report.setPend11((BigDecimal)row[34]);
				
				report.setTot12((BigDecimal) row[35]);
				report.setCpl12((BigDecimal) row[36]);
				report.setPend12((BigDecimal)row[37]);
				
				report.setTot13((BigDecimal) row[38]);
				report.setCpl13((BigDecimal) row[39]);
				report.setPend13((BigDecimal)row[40]);
				
				report.setTot14((BigDecimal) row[41]);
				report.setCpl14((BigDecimal) row[42]);
				report.setPend14((BigDecimal)row[43]);
				
				report.setTot15((BigDecimal) row[44]);
				report.setCpl15((BigDecimal) row[45]);
				report.setPend15((BigDecimal)row[46]);
				
				report.setTot16((BigDecimal) row[47]);
				report.setCpl16((BigDecimal) row[48]);
				report.setPend16((BigDecimal)row[49]);
				
				report.setTot17((BigDecimal) row[50]);
				report.setCpl17((BigDecimal) row[51]);
				report.setPend17((BigDecimal)row[52]);
				
				report.setTot18((BigDecimal) row[53]);
				report.setCpl18((BigDecimal) row[54]);
				report.setPend18((BigDecimal)row[55]);
				
				report.setTot19((BigDecimal) row[56]);
				report.setCpl19((BigDecimal) row[57]);
				report.setPend19((BigDecimal)row[58]);
				
				report.setTot20((BigDecimal) row[59]);
				report.setCpl20((BigDecimal) row[60]);
				report.setPend20((BigDecimal)row[61]);
				
				report.setTot21((BigDecimal) row[62]);
				report.setCpl21((BigDecimal) row[63]);
				report.setPend21((BigDecimal)row[64]);
				
				report.setTot22((BigDecimal) row[65]);
				report.setCpl22((BigDecimal) row[66]);
				report.setPend22((BigDecimal)row[67]);
				
				report.setTot23((BigDecimal) row[68]);
				report.setCpl23((BigDecimal) row[69]);
				report.setPend23((BigDecimal)row[70]);
				
				report.setTot24((BigDecimal) row[71]);
				report.setCpl24((BigDecimal) row[72]);
				report.setPend24((BigDecimal)row[73]);			
				
				report.setCircleName((String)row[74]);
				report.setSectionName((String)row[75]);
				report.setDivisionName((String) row[76]);
						 

				sectionList.add(report);
			}
	        
	        selectedCircleName = circleName;   
	        cameFromInsideReport= true;
	        cameFromInsideSection=true;
	        
	        
	        FacesContext.getCurrentInstance().getExternalContext()
	        .getFlash().put("comingFromCircle", true);
	        
            FacesContext.getCurrentInstance().getExternalContext().redirect("hourWiseSectionAbstract.xhtml");

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
		
		 if (dmFilter.getFromDate() != null) {
		        Calendar cal = Calendar.getInstance();
		        cal.setTime(dmFilter.getFromDate());
		        
		        Calendar currentCal = Calendar.getInstance();
		        currentCal.setTime(new Date());
	        
	        if (cal.get(Calendar.YEAR) > currentCal.get(Calendar.YEAR) || 
	                (cal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) && 
	                		cal.get(Calendar.MONTH) > currentCal.get(Calendar.MONTH))) {
	                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
	                    "Error", "Cannot select future month/year");
	                FacesContext.getCurrentInstance().addMessage(null, message);
	                return;
	            }
	        }
		
		if (dmFilter.getFromDate() == null) {
			Calendar cal = Calendar.getInstance();
			cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
			dmFilter.setFromDate(cal.getTime());
		}
		if(dmFilter.getHours()==null) {
			dmFilter.setHours("24");
		}

		Session session = null;
		
		try {
			session = sessionFactory.openSession();
			session.beginTransaction();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String formattedFromDate = sdf.format(dmFilter.getFromDate());
			
	        if(dmFilter.getComplaintType()==null) {
	        	dmFilter.setComplaintType("AL");
	        }
	        if(dmFilter.getDevice()==null) {
	        	dmFilter.setDevice("L");
	        }


			session.createNativeQuery(
					"BEGIN cat_dev_hour_secabst(:circd,:sectionCode,:complaintType,:device, TO_DATE(:fromDate, 'YYYY-MM-DD'),:hour); END;")
					.setParameter("circd", circleCode)
					.setParameter("sectionCode", dmFilter.getSectionCode())
					.setParameter("complaintType", dmFilter.getComplaintType())
					.setParameter("device", dmFilter.getDevice())
					.setParameter("fromDate", formattedFromDate)
					.setParameter("hour", dmFilter.getHours())
					.executeUpdate();

			session.flush();
			session.getTransaction().commit();
			
			String hql ="SELECT c.CIRCODE,c.SECCODE,"
					+ "c.TOT1,c.CPL1,(c.LIVE1+c.TMP1),"
	        		+ "c.TOT2,c.CPL2,(c.LIVE2+c.TMP2),"
	        		+ "c.TOT3,c.CPL3,(c.LIVE3+c.TMP3),"
	        		+ "c.TOT4,c.CPL4,(c.LIVE4+c.TMP4),"
	        		+ "c.TOT5,c.CPL5,(c.LIVE5+c.TMP5),"
	        		+ "c.TOT6,c.CPL6,(c.LIVE6+c.TMP6),"
	        		+ "c.TOT7,c.CPL7,(c.LIVE7+c.TMP7),"
	        		+ "c.TOT8,c.CPL8,(c.LIVE8+c.TMP8),"
	        		+ "c.TOT9,c.CPL9,(c.LIVE9+c.TMP9),"
	        		+ "c.TOT10,c.CPL10,(c.LIVE10+c.TMP10),"
	        		+ "c.TOT11,c.CPL11,(c.LIVE11+c.TMP11),"
	        		+ "c.TOT12,c.CPL12,(c.LIVE12+c.TMP12),"
	        		+ "c.TOT13,c.CPL13,(c.LIVE13+c.TMP13),"
	        		+ "c.TOT14,c.CPL14,(c.LIVE14+c.TMP14),"
	        		+ "c.TOT15,c.CPL15,(c.LIVE15+c.TMP15),"
	        		+ "c.TOT16,c.CPL16,(c.LIVE16+c.TMP16),"
	        		+ "c.TOT17,c.CPL17,(c.LIVE17+c.TMP17),"
	        		+ "c.TOT18,c.CPL18,(c.LIVE18+c.TMP18),"
	        		+ "c.TOT19,c.CPL19,(c.LIVE19+c.TMP19),"
	        		+ "c.TOT20,c.CPL20,(c.LIVE20+c.TMP20),"
	        		+ "c.TOT21,c.CPL21,(c.LIVE21+c.TMP21),"
	        		+ "c.TOT22,c.CPL22,(c.LIVE22+c.TMP22),"
	        		+ "c.TOT23,c.CPL23,(c.LIVE23+c.TMP23),"
	        		+ "c.TOT24,c.CPL24,(c.LIVE24+c.TMP24),"
	        		+ "s.name as SectionName, "
	        		+"s.division_id AS DIVISION_ID, " 
		            +"d.name AS DIVISION_NAME, " 
		            +"s.sub_division_id AS SUB_DIVISION_ID " 
	        		+ "FROM TMP_CT_DEV_MONABST_SEC c "
	        		+"JOIN SECTION s ON s.id = c.seccode " 
		            +"JOIN DIVISION d ON d.id = s.division_id";

			
	        List<Object[]> results = session.createNativeQuery(hql).getResultList();

	        List<TmpYearWiseSections> result = new ArrayList<TmpYearWiseSections>();
	        sectionList = new ArrayList<>();
	        
	        for (Object[] row : results) {
	        	TmpYearWiseSections report = new TmpYearWiseSections();
				report.setCirCode((String) row[0]);
				report.setSecCode((String) row[1]);
				
				report.setTot1((BigDecimal) row[2]);
				report.setCpl1((BigDecimal) row[3]);
				report.setPend1((BigDecimal)row[4]);
				
				report.setTot2((BigDecimal) row[5]);
				report.setCpl2((BigDecimal) row[6]);
				report.setPend2((BigDecimal)row[7]);
				
				report.setTot3((BigDecimal) row[8]);
				report.setCpl3((BigDecimal) row[9]);
				report.setPend3((BigDecimal)row[10]);
			
				report.setTot4((BigDecimal) row[11]);
				report.setCpl4((BigDecimal) row[12]);
				report.setPend4((BigDecimal)row[13]);
			
				report.setTot5((BigDecimal) row[14]);
				report.setCpl5((BigDecimal) row[15]);
				report.setPend5((BigDecimal)row[16]);
				
				report.setTot6((BigDecimal) row[17]);
				report.setCpl6((BigDecimal) row[18]);
				report.setPend6((BigDecimal)row[19]);
				
				report.setTot7((BigDecimal) row[20]);
				report.setCpl7((BigDecimal) row[21]);
				report.setPend7((BigDecimal)row[22]);
				
				report.setTot8((BigDecimal) row[23]);
				report.setCpl8((BigDecimal) row[24]);
				report.setPend8((BigDecimal)row[25]);
				
				report.setTot9((BigDecimal) row[26]);
				report.setCpl9((BigDecimal) row[27]);
				report.setPend9((BigDecimal)row[28]);
				
				report.setTot10((BigDecimal) row[29]);
				report.setCpl10((BigDecimal) row[30]);
				report.setPend10((BigDecimal)row[31]);
				
				report.setTot11((BigDecimal) row[32]);
				report.setCpl11((BigDecimal) row[33]);
				report.setPend11((BigDecimal)row[34]);
				
				report.setTot12((BigDecimal) row[35]);
				report.setCpl12((BigDecimal) row[36]);
				report.setPend12((BigDecimal)row[37]);
				
				report.setTot13((BigDecimal) row[38]);
				report.setCpl13((BigDecimal) row[39]);
				report.setPend13((BigDecimal)row[40]);
				
				report.setTot14((BigDecimal) row[41]);
				report.setCpl14((BigDecimal) row[42]);
				report.setPend14((BigDecimal)row[43]);
				
				report.setTot15((BigDecimal) row[44]);
				report.setCpl15((BigDecimal) row[45]);
				report.setPend15((BigDecimal)row[46]);
				
				report.setTot16((BigDecimal) row[47]);
				report.setCpl16((BigDecimal) row[48]);
				report.setPend16((BigDecimal)row[49]);
				
				report.setTot17((BigDecimal) row[50]);
				report.setCpl17((BigDecimal) row[51]);
				report.setPend17((BigDecimal)row[52]);
				
				report.setTot18((BigDecimal) row[53]);
				report.setCpl18((BigDecimal) row[54]);
				report.setPend18((BigDecimal)row[55]);
				
				report.setTot19((BigDecimal) row[56]);
				report.setCpl19((BigDecimal) row[57]);
				report.setPend19((BigDecimal)row[58]);
				
				report.setTot20((BigDecimal) row[59]);
				report.setCpl20((BigDecimal) row[60]);
				report.setPend20((BigDecimal)row[61]);
				
				report.setTot21((BigDecimal) row[62]);
				report.setCpl21((BigDecimal) row[63]);
				report.setPend21((BigDecimal)row[64]);
				
				report.setTot22((BigDecimal) row[65]);
				report.setCpl22((BigDecimal) row[66]);
				report.setPend22((BigDecimal)row[67]);
				
				report.setTot23((BigDecimal) row[68]);
				report.setCpl23((BigDecimal) row[69]);
				report.setPend23((BigDecimal)row[70]);
				
				report.setTot24((BigDecimal) row[71]);
				report.setCpl24((BigDecimal) row[72]);
				report.setPend24((BigDecimal)row[73]);			
				
				report.setSectionName((String)row[74]);
				report.setDivisionId((String)row[75].toString());
				report.setDivisionName((String) row[76]);
				report.setSubDivisionId((String)row[77].toString());
						 

				result.add(report);
			}
	        
	        

			//SECTION
			if(adminUserValueBean.getRoleId()==1) {
				sectionList = result.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
						.filter(section -> section.getSecCode().equalsIgnoreCase(sectionCode))
						.collect(Collectors.toList());
			}
			//DIVISION
			else if(adminUserValueBean.getRoleId()==2) {
				sectionList = result.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
						.filter(sd -> sd.getSubDivisionId().equalsIgnoreCase(subDivisionId))
						.collect(Collectors.toList());
			}
			//SUB DIVISION
			else if(adminUserValueBean.getRoleId()==3) {
				sectionList = result.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
						.filter(d -> d.getDivisionId().equalsIgnoreCase(divisionId))
						.collect(Collectors.toList());
			}
			//ALL SECTION
			else {
				sectionList = result.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
						.collect(Collectors.toList());
			}

			
	        selectedCircleName = circleName;   
	        cameFromInsideSection= true;
	        
            FacesContext.getCurrentInstance().getExternalContext().redirect("hourWiseSectionAbstract.xhtml");

	    } catch (Exception e) {
	        e.printStackTrace();
	        System.out.println("Error in database operation");
	    }
	}
	
	//COMPLAINT LIST FOR SECTION
	
	@Transactional
	public void getComplaintListForSection(String secCode, String sectionName) throws IOException, ParseException {

	    try (Session session = sessionFactory.openSession()) {

	        List<String> complaintTypes = new ArrayList<>();
	        List<String> devices = new ArrayList<>();

	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        String fromDateStr = sdf.format(dmFilter.getFromDate());

	        String hours = dmFilter.getHours();
	        Date fromDate = sdf.parse(fromDateStr);

	        Calendar cal = Calendar.getInstance();
	        cal.setTime(fromDate);
	        if (hours != null && !hours.isEmpty()) {
	            cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(hours));
	        }
	        Date toDate = cal.getTime();

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
	                case "AL": 
	                    complaintTypes.addAll(Arrays.asList("BL", "ME", "PF", "VF", "FI", "TH", "TE", "OT", "CS"));
	                    break;
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
	                case "L": 
	                    devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile", "web", "SM", "admin", "FOC", "MI", "MM"));
	                    break;
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
	                "WHERE a.SECTION_ID = :sectionCode " +
	                "AND a.created_on BETWEEN :fromDate AND :toDate");

	        if (!complaintTypes.isEmpty()) {
	            hql.append(" AND a.COMPLAINT_TYPE IN (:complaintTypes)");
	        }

	        if (!devices.isEmpty()) {
	            hql.append(" AND a.DEVICE IN (:devices)");
	        }

	        Query query = session.createNativeQuery(hql.toString());
	        query.setParameter("sectionCode", sectionCode);
	        query.setParameter("fromDate", fromDate);
	        query.setParameter("toDate", toDate);

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
	        redirectFrom ="section";

	        FacesContext.getCurrentInstance().getExternalContext().redirect("hourWiseComplaintList.xhtml");

	    } catch (NumberFormatException e) {
	        System.out.println("ERROR..........." + e);
	    }
	}
	

	//COMPLAINT LIST FOR CIRCLE
	@Transactional
	public void getComplaintListForCircle() throws IOException, ParseException {

	    try (Session session = sessionFactory.openSession()) {

	        List<String> complaintTypes = new ArrayList<>();
	        List<String> devices = new ArrayList<>();

	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        String fromDateStr = sdf.format(dmFilter.getFromDate());

	        String hours = dmFilter.getHours();
	        Date fromDate = sdf.parse(fromDateStr);

	        Calendar cal = Calendar.getInstance();
	        cal.setTime(fromDate);
	        if (hours != null && !hours.isEmpty()) {
	            cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(hours));
	        }
	        Date toDate = cal.getTime();

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
	                case "AL": 
	                    complaintTypes.addAll(Arrays.asList("BL", "ME", "PF", "VF", "FI", "TH", "TE", "OT", "CS"));
	                    break;
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
	                case "L": 
	                    devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile", "web", "SM", "admin", "FOC", "MI", "MM"));
	                    break;
	                default: throw new IllegalArgumentException("Invalid Device Type");
	            }
	        }
	        FacesContext facesContext = FacesContext.getCurrentInstance();
	        String status = facesContext.getExternalContext().getRequestParameterMap().get("status");
	        
	        List<Integer> statusIDs= new ArrayList<>();
	        
	        if(status.equalsIgnoreCase("Received")) {
	        	statusIDs = Arrays.asList(CCMSConstants.PENDING,CCMSConstants.COMPLETED,CCMSConstants.IN_PROGRESS);	        	
	        }
	        else if (status.equalsIgnoreCase("Completed")) {
	        	statusIDs = Arrays.asList(CCMSConstants.COMPLETED);	        	
	        }
	        else if (status.equalsIgnoreCase("Pending")) {
	        	statusIDs = Arrays.asList(CCMSConstants.PENDING);	        	
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
	                "JOIN SECTION j ON a.section_id = j.id " +
	                "WHERE a.STATUS_ID IN :statusIDs " +
	                "AND a.created_on BETWEEN :fromDate AND :toDate");

	        if (!complaintTypes.isEmpty()) {
	            hql.append(" AND a.COMPLAINT_TYPE IN (:complaintTypes)");
	        }

	        if (!devices.isEmpty()) {
	            hql.append(" AND a.DEVICE IN (:devices)");
	        }

	        Query query = session.createNativeQuery(hql.toString());
	        query.setParameter("fromDate", fromDate);
	        query.setParameter("toDate", toDate);
	        query.setParameter("statusIDs", statusIDs);

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

	        redirectFrom="circle";
	        selectedSectionName = null;

	        FacesContext.getCurrentInstance().getExternalContext().redirect("hourWiseComplaintList.xhtml");

	    } catch (NumberFormatException e) {
	        System.out.println("ERROR..........." + e);
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
	
	//CIRCLE REPORT EXCEL DOWNLOAD
	public void exportCirclesToExcel(List<TmpYearWiseCircle> yearWiseCircles) throws IOException {
	    HSSFWorkbook workbook = new HSSFWorkbook();
	    Sheet sheet = workbook.createSheet("CircleWise_Hourly_Splitup_Abstract_For_A_Date");

	    final int COLUMN_COUNT = 77;
	    sheet.setColumnWidth(0, 3000);     //S.NO
	    sheet.setColumnWidth(1, 5000);     //CIRCLE
	    for (int i = 2; i < COLUMN_COUNT; i++) {
	        sheet.setColumnWidth(i, 3000);
	    }

	    // Create styles
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
	    dateStyle.setBorderBottom(BorderStyle.THIN);
	    dateStyle.setBorderTop(BorderStyle.THIN);
	    dateStyle.setBorderLeft(BorderStyle.THIN);
	    dateStyle.setBorderRight(BorderStyle.THIN);

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

	    CellStyle dataCellStyle = workbook.createCellStyle();
	    dataCellStyle.setBorderBottom(BorderStyle.THIN);
	    dataCellStyle.setBorderTop(BorderStyle.THIN);
	    dataCellStyle.setBorderLeft(BorderStyle.THIN);
	    dataCellStyle.setBorderRight(BorderStyle.THIN);
	    dataCellStyle.setAlignment(HorizontalAlignment.CENTER); // Changed from LEFT to CENTER
	    dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

	    // Create heading row
	    Row headingRow = sheet.createRow(0);
	    Cell headingCell = headingRow.createCell(0);
	    headingCell.setCellValue("CIRCLE WISE HOURLY SPLITUP ABSTRACT FOR A DATE");
	    headingCell.setCellStyle(headingStyle);
	    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, COLUMN_COUNT-1)); 
	    
	    // Create date row
	    Row dateRow = sheet.createRow(1);
	    Cell dateCell = dateRow.createCell(0);
	    
	    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	    String fromDateStr = dmFilter.getFromDate() != null ? 
	                         dateFormat.format(dmFilter.getFromDate()) : "N/A";
	    
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

	    dateCell.setCellValue("From Date: " + fromDateStr + " | Complaint Type: " + complaintType + " | Device: " + device);
	    dateCell.setCellStyle(dateStyle);
	    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, COLUMN_COUNT-1));

	    // Create header rows
	    Row headerRow1 = sheet.createRow(2);
	    Row headerRow2 = sheet.createRow(3);

	    String[] mainHeaders = {
	        "S.NO", "CIRCLE", "0-1", "1-2", "2-3", "3-4", "4-5", "5-6", "6-7", "7-8", "8-9", "9-10", "10-11", "11-12",
	        "12-13", "13-14", "14-15", "15-16", "16-17", "17-18", "18-19", "19-20", "20-21", "21-22", "22-23", "23-24",
	        "TOTAL"
	    };
	    String[] subHeaders = {"Revd.", "Comp.", "Pend."};

	    int colIndex = 0;
	    for (String mainHeader : mainHeaders) {
	        if (mainHeader.equals("S.NO") || mainHeader.equals("CIRCLE")) {
	            // For S.NO and CIRCLE columns
	            Cell cell = headerRow1.createCell(colIndex);
	            cell.setCellValue(mainHeader);
	            cell.setCellStyle(headerStyle);
	            sheet.addMergedRegion(new CellRangeAddress(2, 3, colIndex, colIndex));
	            colIndex++;
	        } else {
	            // For hour range and total columns
	            Cell cell = headerRow1.createCell(colIndex);
	            cell.setCellValue(mainHeader);
	            cell.setCellStyle(headerStyle);
	            sheet.addMergedRegion(new CellRangeAddress(2, 2, colIndex, colIndex + 2));

	            // Add subheaders
	            for (String subHeader : subHeaders) {
	                Cell subCell = headerRow2.createCell(colIndex);
	                subCell.setCellValue(subHeader);
	                subCell.setCellStyle(headerStyle); // Changed from dataCellStyle to headerStyle
	                colIndex++;
	            }
	        }
	    }

	    int rowNum = 4;
	    BigDecimal[] totalSums = new BigDecimal[COLUMN_COUNT - 2]; // Adjusted for S.NO and CIRCLE columns
	    Arrays.fill(totalSums, BigDecimal.ZERO);

	    int serialNumber = 1;
	    for (TmpYearWiseCircle report : yearWiseCircles) {
	        Row row = sheet.createRow(rowNum++);
	        
	        // S.NO column
	        Cell snoCell = row.createCell(0);
	        snoCell.setCellValue(serialNumber++);
	        snoCell.setCellStyle(dataCellStyle);
	        
	        // CIRCLE column
	        Cell circleCell = row.createCell(1);
	        circleCell.setCellValue(report.getCircleName());
	        circleCell.setCellStyle(dataCellStyle);

	        BigDecimal rowTotSum = BigDecimal.ZERO;
	        BigDecimal rowCplSum = BigDecimal.ZERO;
	        BigDecimal rowPendSum = BigDecimal.ZERO;

	        int dataIndex = 2;
	        BigDecimal[] values = {
	            report.getTot1(), report.getCpl1(), report.getPend1(),
	            report.getTot2(), report.getCpl2(), report.getPend2(),
	            report.getTot3(), report.getCpl3(), report.getPend3(),
	            report.getTot4(), report.getCpl4(), report.getPend4(),
	            report.getTot5(), report.getCpl5(), report.getPend5(),
	            report.getTot6(), report.getCpl6(), report.getPend6(),
	            report.getTot7(), report.getCpl7(), report.getPend7(),
	            report.getTot8(), report.getCpl8(), report.getPend8(),
	            report.getTot9(), report.getCpl9(), report.getPend9(),
	            report.getTot10(), report.getCpl10(), report.getPend10(),
	            report.getTot11(), report.getCpl11(), report.getPend11(),
	            report.getTot12(), report.getCpl12(), report.getPend12(),
	            report.getTot13(), report.getCpl13(), report.getPend13(),
	            report.getTot14(), report.getCpl14(), report.getPend14(),
	            report.getTot15(), report.getCpl15(), report.getPend15(),
	            report.getTot16(), report.getCpl16(), report.getPend16(),
	            report.getTot17(), report.getCpl17(), report.getPend17(),
	            report.getTot18(), report.getCpl18(), report.getPend18(),
	            report.getTot19(), report.getCpl19(), report.getPend19(),
	            report.getTot20(), report.getCpl20(), report.getPend20(),
	            report.getTot21(), report.getCpl21(), report.getPend21(),
	            report.getTot22(), report.getCpl22(), report.getPend22(),
	            report.getTot23(), report.getCpl23(), report.getPend23(),
	            report.getTot24(), report.getCpl24(), report.getPend24()
	        };

	        for (int i = 0; i < values.length; i += 3) {
	            BigDecimal tot = values[i] != null ? values[i] : BigDecimal.ZERO;
	            BigDecimal cpl = values[i + 1] != null ? values[i + 1] : BigDecimal.ZERO;
	            BigDecimal pend = values[i + 2] != null ? values[i + 2] : BigDecimal.ZERO;

	            Cell totCell = row.createCell(dataIndex);
	            totCell.setCellValue(tot.doubleValue());
	            totCell.setCellStyle(dataCellStyle);
	            
	            Cell cplCell = row.createCell(dataIndex + 1);
	            cplCell.setCellValue(cpl.doubleValue());
	            cplCell.setCellStyle(dataCellStyle);
	            
	            Cell pendCell = row.createCell(dataIndex + 2);
	            pendCell.setCellValue(pend.doubleValue());
	            pendCell.setCellStyle(dataCellStyle);

	            rowTotSum = rowTotSum.add(tot);
	            rowCplSum = rowCplSum.add(cpl);
	            rowPendSum = rowPendSum.add(pend);

	            totalSums[dataIndex - 2] = totalSums[dataIndex - 2].add(tot);
	            totalSums[dataIndex - 1] = totalSums[dataIndex - 1].add(cpl);
	            totalSums[dataIndex] = totalSums[dataIndex].add(pend);

	            dataIndex += 3;
	        }

	        // TOTAL columns
	        Cell totTotalCell = row.createCell(dataIndex);
	        totTotalCell.setCellValue(rowTotSum.doubleValue());
	        totTotalCell.setCellStyle(dataCellStyle);
	        
	        Cell cplTotalCell = row.createCell(dataIndex + 1);
	        cplTotalCell.setCellValue(rowCplSum.doubleValue());
	        cplTotalCell.setCellStyle(dataCellStyle);
	        
	        Cell pendTotalCell = row.createCell(dataIndex + 2);
	        pendTotalCell.setCellValue(rowPendSum.doubleValue());
	        pendTotalCell.setCellStyle(dataCellStyle);

	        totalSums[dataIndex - 2] = totalSums[dataIndex - 2].add(rowTotSum);
	        totalSums[dataIndex - 1] = totalSums[dataIndex - 1].add(rowCplSum);
	        totalSums[dataIndex] = totalSums[dataIndex].add(rowPendSum);
	    }

	    // Create total row
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
	    
	    // S.NO column (empty)
	    Cell emptyCell = totalRow.createCell(0);
	    emptyCell.setCellStyle(totalRowStyle);
	    
	    // TOTAL label
	    Cell totalLabelCell = totalRow.createCell(1);
	    totalLabelCell.setCellValue("TOTAL");
	    totalLabelCell.setCellStyle(totalRowStyle);

	    // Total values
	    for (int i = 2; i < COLUMN_COUNT; i++) {
	        if (i - 2 < totalSums.length) {
	            Cell totalCell = totalRow.createCell(i);
	            totalCell.setCellValue(totalSums[i - 2].doubleValue());
	            totalCell.setCellStyle(totalRowStyle);
	        }
	    }

	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    workbook.write(outputStream);
	    workbook.close();

	    excelFile = DefaultStreamedContent.builder()
	            .name("CircleWise_Hourly_Splitup_Abstract_For_A_Date.xls")
	            .contentType("application/vnd.ms-excel")
	            .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
	            .build();
	}

	//SECTION REPORT EXCEL DOWNLOAD
	 
	public void exportSectionsToExcel(List<TmpYearWiseSections> reports) throws IOException {
	    HSSFWorkbook workbook = new HSSFWorkbook();
	    Sheet sheet = workbook.createSheet("SectionWise_Hourly_Splitup_Abstract_For_A_Date");

	    final int COLUMN_COUNT = 78;
	    sheet.setColumnWidth(0, 3000);   //sno
	    sheet.setColumnWidth(1, 5000);  //section
	    sheet.setColumnWidth(2, 5000);  // division

	    for (int i = 3; i < COLUMN_COUNT; i++) {
	        sheet.setColumnWidth(i, 3000);
	    }
	    
	    // Create styles
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
	    dateStyle.setBorderBottom(BorderStyle.THIN);
	    dateStyle.setBorderTop(BorderStyle.THIN);
	    dateStyle.setBorderLeft(BorderStyle.THIN);
	    dateStyle.setBorderRight(BorderStyle.THIN);

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

	    CellStyle dataCellStyle = workbook.createCellStyle();
	    dataCellStyle.setBorderBottom(BorderStyle.THIN);
	    dataCellStyle.setBorderTop(BorderStyle.THIN);
	    dataCellStyle.setBorderLeft(BorderStyle.THIN);
	    dataCellStyle.setBorderRight(BorderStyle.THIN);
	    dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
	    dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

	    // Create heading row
	    Row headingRow = sheet.createRow(0);
	    Cell headingCell = headingRow.createCell(0);
	    headingCell.setCellValue("SECTION WISE HOURLY SPLITUP ABSTRACT FOR A DATE FOR CIRCLE :"+selectedCircleName);
	    headingCell.setCellStyle(headingStyle);
	    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 77)); 
	    
	    // Create date row
	    Row dateRow = sheet.createRow(1);
	    Cell dateCell = dateRow.createCell(0);
	    
	    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	    String fromDateStr = dmFilter.getFromDate() != null ? 
	                         dateFormat.format(dmFilter.getFromDate()) : "N/A";
	    
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

	    dateCell.setCellValue("From Date: " + fromDateStr + " | Complaint Type: " + complaintType + " | Device: " + device);
	    dateCell.setCellStyle(dateStyle);
	    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 77));

	    // Create header rows with proper styling
	    Row headerRow1 = sheet.createRow(2);
	    Row headerRow2 = sheet.createRow(3);

	    String[] mainHeaders = {
	        "S.NO","SECTION","DIVISION", "0-1", "1-2", "2-3", "3-4", "4-5", "5-6", "6-7", "7-8", "8-9", "9-10", "10-11", "11-12",
	        "12-13", "13-14", "14-15", "15-16", "16-17", "17-18", "18-19", "19-20", "20-21", "21-22", "22-23", "23-24",
	        "TOTAL"
	    };
	    String[] subHeaders = {"Revd.", "Comp.", "Pend."};

	    int colIndex = 0;

	    // Create S.NO, SECTION, DIVISION headers
	    headerRow1.createCell(0).setCellValue("S.NO");
	    headerRow1.getCell(0).setCellStyle(headerStyle);
	    sheet.addMergedRegion(new CellRangeAddress(2, 3, 0, 0));

	    headerRow1.createCell(1).setCellValue("SECTION");
	    headerRow1.getCell(1).setCellStyle(headerStyle);
	    sheet.addMergedRegion(new CellRangeAddress(2, 3, 1, 1));

	    headerRow1.createCell(2).setCellValue("DIVISION");
	    headerRow1.getCell(2).setCellStyle(headerStyle);
	    sheet.addMergedRegion(new CellRangeAddress(2, 3, 2, 2));

	    colIndex = 3; // Start from first hour column

	    // Create hour columns with subheaders
	    for (int i = 3; i < mainHeaders.length - 1; i++) { // Skip S.NO, SECTION, DIVISION and TOTAL
	        // Main hour header
	        Cell hourCell = headerRow1.createCell(colIndex);
	        hourCell.setCellValue(mainHeaders[i]);
	        hourCell.setCellStyle(headerStyle);
	        sheet.addMergedRegion(new CellRangeAddress(2, 2, colIndex, colIndex + 2));
	        
	        // Subheaders (Revd, Comp, Pend)
	        for (String subHeader : subHeaders) {
	            Cell subCell = headerRow2.createCell(colIndex);
	            subCell.setCellValue(subHeader);
	            subCell.setCellStyle(headerStyle);
	            colIndex++;
	        }
	    }

	    // Create TOTAL header
	    Cell totalHeaderCell = headerRow1.createCell(colIndex);
	    totalHeaderCell.setCellValue("TOTAL");
	    totalHeaderCell.setCellStyle(headerStyle);
	    sheet.addMergedRegion(new CellRangeAddress(2, 2, colIndex, colIndex + 2));

	    // TOTAL subheaders
	    for (String subHeader : subHeaders) {
	        Cell subCell = headerRow2.createCell(colIndex);
	        subCell.setCellValue(subHeader);
	        subCell.setCellStyle(headerStyle);
	        colIndex++;
	    }

	    // Populate data rows
	    int rowNum = 4;
	    BigDecimal[] totalSums = new BigDecimal[COLUMN_COUNT - 3]; // Exclude S.NO, SECTION, DIVISION
	    Arrays.fill(totalSums, BigDecimal.ZERO);

	    int serialNumber = 1;
	    for (TmpYearWiseSections report : reports) {
	        Row row = sheet.createRow(rowNum++);
	        
	        // Serial number
	        Cell snCell = row.createCell(0);
	        snCell.setCellValue(serialNumber++);
	        snCell.setCellStyle(dataCellStyle);
	        
	        // Section name
	        Cell sectionCell = row.createCell(1);
	        sectionCell.setCellValue(report.getSectionName());
	        sectionCell.setCellStyle(dataCellStyle);
	        
	        // Division name
	        Cell divisionCell = row.createCell(2);
	        divisionCell.setCellValue(report.getDivisionName());
	        divisionCell.setCellStyle(dataCellStyle);

	        BigDecimal rowTotSum = BigDecimal.ZERO;
	        BigDecimal rowCplSum = BigDecimal.ZERO;
	        BigDecimal rowPendSum = BigDecimal.ZERO;

	        int dataIndex = 3;
	        
	        // Populate hour data
	        BigDecimal[] values = {
	            report.getTot1(), report.getCpl1(), report.getPend1(),
	            report.getTot2(), report.getCpl2(), report.getPend2(),
	            report.getTot3(), report.getCpl3(), report.getPend3(),
	            report.getTot4(), report.getCpl4(), report.getPend4(),
	            report.getTot5(), report.getCpl5(), report.getPend5(),
	            report.getTot6(), report.getCpl6(), report.getPend6(),
	            report.getTot7(), report.getCpl7(), report.getPend7(),
	            report.getTot8(), report.getCpl8(), report.getPend8(),
	            report.getTot9(), report.getCpl9(), report.getPend9(),
	            report.getTot10(), report.getCpl10(), report.getPend10(),
	            report.getTot11(), report.getCpl11(), report.getPend11(),
	            report.getTot12(), report.getCpl12(), report.getPend12(),
	            report.getTot13(), report.getCpl13(), report.getPend13(),
	            report.getTot14(), report.getCpl14(), report.getPend14(),
	            report.getTot15(), report.getCpl15(), report.getPend15(),
	            report.getTot16(), report.getCpl16(), report.getPend16(),
	            report.getTot17(), report.getCpl17(), report.getPend17(),
	            report.getTot18(), report.getCpl18(), report.getPend18(),
	            report.getTot19(), report.getCpl19(), report.getPend19(),
	            report.getTot20(), report.getCpl20(), report.getPend20(),
	            report.getTot21(), report.getCpl21(), report.getPend21(),
	            report.getTot22(), report.getCpl22(), report.getPend22(),
	            report.getTot23(), report.getCpl23(), report.getPend23(),
	            report.getTot24(), report.getCpl24(), report.getPend24()
	        };
	        
	        for (int i = 0; i < values.length; i += 3) {
	            BigDecimal tot = values[i] != null ? values[i] : BigDecimal.ZERO;
	            BigDecimal cpl = values[i + 1] != null ? values[i + 1] : BigDecimal.ZERO;
	            BigDecimal pend = values[i + 2] != null ? values[i + 2] : BigDecimal.ZERO;

	            // Set cell values
	            Cell totCell = row.createCell(dataIndex);
	            totCell.setCellValue(tot.doubleValue());
	            totCell.setCellStyle(dataCellStyle);
	            
	            Cell cplCell = row.createCell(dataIndex + 1);
	            cplCell.setCellValue(cpl.doubleValue());
	            cplCell.setCellStyle(dataCellStyle);
	            
	            Cell pendCell = row.createCell(dataIndex + 2);
	            pendCell.setCellValue(pend.doubleValue());
	            pendCell.setCellStyle(dataCellStyle);

	            // Update sums
	            rowTotSum = rowTotSum.add(tot);
	            rowCplSum = rowCplSum.add(cpl);
	            rowPendSum = rowPendSum.add(pend);

	            totalSums[dataIndex - 3] = totalSums[dataIndex - 3].add(tot);
	            totalSums[dataIndex - 2] = totalSums[dataIndex - 2].add(cpl);
	            totalSums[dataIndex - 1] = totalSums[dataIndex - 1].add(pend);

	            dataIndex += 3;
	        }

	        // Add TOTAL columns
	        Cell totTotalCell = row.createCell(dataIndex);
	        totTotalCell.setCellValue(rowTotSum.doubleValue());
	        totTotalCell.setCellStyle(dataCellStyle);
	        
	        Cell cplTotalCell = row.createCell(dataIndex + 1);
	        cplTotalCell.setCellValue(rowCplSum.doubleValue());
	        cplTotalCell.setCellStyle(dataCellStyle);
	        
	        Cell pendTotalCell = row.createCell(dataIndex + 2);
	        pendTotalCell.setCellValue(rowPendSum.doubleValue());
	        pendTotalCell.setCellStyle(dataCellStyle);

	        // Update total sums
	        totalSums[dataIndex - 3] = totalSums[dataIndex - 3].add(rowTotSum);
	        totalSums[dataIndex - 2] = totalSums[dataIndex - 2].add(rowCplSum);
	        totalSums[dataIndex - 1] = totalSums[dataIndex - 1].add(rowPendSum);
	    }

	    // Create Total Row
	    Row totalRow = sheet.createRow(rowNum);
	    
	    // Create cells for S.NO, SECTION, DIVISION
	    totalRow.createCell(0).setCellValue("TOTAL");
	    totalRow.getCell(0).setCellStyle(headerStyle);
	    sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 2));

	    // Add total values
	    for (int i = 0; i < totalSums.length; i++) {
	        Cell totalCell = totalRow.createCell(i + 3);
	        totalCell.setCellValue(totalSums[i].doubleValue());
	        totalCell.setCellStyle(headerStyle);
	    }

	    // Write to output stream
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    workbook.write(outputStream);
	    workbook.close();

	    // Create StreamedContent for download
	    excelFile = DefaultStreamedContent.builder()
	            .name("SectionWise_Hourly_Splitup_Abstract_For_A_Date.xls")
	            .contentType("application/vnd.ms-excel")
	            .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
	            .build();
	}

	private void setCellValue(Row row, int columnIndex, BigDecimal value) {
	    Cell cell = row.createCell(columnIndex);
	    if (value != null) {
	        cell.setCellValue(value.doubleValue());
	    } else {
	        cell.setCellValue(0);
	    }
	}
	
	
	// CIRCLE REPORT PDF DOWNLOAD 
	
	public void exportToPdf(List<TmpYearWiseCircle> circleList) throws DocumentException {
		
		final int HOURS = 24;
	    final int SUB_COLUMNS = 3;
	    final int COLUMN_COUNT = 1+ 1 + (HOURS * SUB_COLUMNS) + 3; 
	    final float SNO_COLUMN_WIDTH = 5f;
	    final float FIRST_COLUMN_WIDTH = 15f;
	    final float OTHER_COLUMN_WIDTH = 4f;
	    final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
	    final Font TOTAL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
	    final Font DATA_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
	    final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
	    
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
	    
	    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	    String fromDate = sdf.format(dmFilter.getFromDate());
        

	    Document document = new Document(PageSize.A0.rotate());
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	    try {
	        PdfWriter.getInstance(document, outputStream);
	        document.open();
	        
	        PdfPTable table = new PdfPTable(COLUMN_COUNT);
	        table.setWidthPercentage(100);
	        table.setSpacingBefore(10f);
	        
	        Paragraph title = new Paragraph("HOURLY CIRCLE ABSTRACT REPORT", TITLE_FONT);
	        title.setAlignment(Element.ALIGN_CENTER);
	        document.add(title);
	        
	        Paragraph subTitle = new Paragraph("Date: " + fromDate+ " | " + "Complaint Type : " +complaintType +" | "+"Device: "+device);
	        subTitle.setAlignment(Element.ALIGN_CENTER);
	        subTitle.setSpacingAfter(10);
	        document.add(subTitle);
	        
	        // COLUMN WIDTH
	        float[] columnWidths = new float[COLUMN_COUNT];
	        columnWidths[0] = SNO_COLUMN_WIDTH;
	        columnWidths[1] = FIRST_COLUMN_WIDTH;
	        
	        for (int i = 2; i < COLUMN_COUNT; i++) {
	            columnWidths[i] = OTHER_COLUMN_WIDTH;
	        }
	        table.setWidths(columnWidths);

	        //HEADER
	        addMergedHeaderCell(table, "S.NO", HEADER_FONT, 1, 2);

	        addMergedHeaderCell(table, "CIRCLE", HEADER_FONT, 1, 2);
	        for (int hour = 1; hour <= HOURS; hour++) {
	            addMergedHeaderCell(table, (hour - 1) + "-" + hour, HEADER_FONT, 3, 1);
	        }
	        addMergedHeaderCell(table, "TOTAL", HEADER_FONT, 3, 1);
	        
	        //SUB HEADER
	        String[] subHeaders = {"Revd.", "Comp.", "Pend."};
	        for (int i = 0; i < HOURS + 1; i++) { 
	            for (String subHeader : subHeaders) {
	                PdfPCell cell = new PdfPCell(new Phrase(subHeader, HEADER_FONT));
	                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
	                table.addCell(cell);
	            }
	        }
	        BigDecimal grandTot = BigDecimal.ZERO;
	        BigDecimal grandTotCpl =BigDecimal.ZERO;
	        BigDecimal grandTotPend = BigDecimal.ZERO;

	        //TABLE DATA
	        int serialNumber =1;
	        for (TmpYearWiseCircle report : circleList) {
	            table.addCell(createDataCell(String.valueOf(serialNumber++), DATA_FONT, Element.ALIGN_LEFT));
	            table.addCell(createDataCell(report.getCircleName(), DATA_FONT, Element.ALIGN_LEFT));
	            
	            addHourData(table, report.getTot1(), report.getCpl1(), report.getPend1(), DATA_FONT);
	            addHourData(table, report.getTot2(), report.getCpl2(), report.getPend2(), DATA_FONT);
	            addHourData(table, report.getTot3(), report.getCpl3(), report.getPend3(), DATA_FONT);
	            addHourData(table, report.getTot4(), report.getCpl4(), report.getPend4(), DATA_FONT);
	            
	            addHourData(table, report.getTot5(), report.getCpl5(), report.getPend5(), DATA_FONT);
	            addHourData(table, report.getTot6(), report.getCpl6(), report.getPend6(), DATA_FONT);
	            addHourData(table, report.getTot7(), report.getCpl7(), report.getPend7(), DATA_FONT);
	            addHourData(table, report.getTot8(), report.getCpl8(), report.getPend8(), DATA_FONT);
	            
	            addHourData(table, report.getTot9(), report.getCpl9(), report.getPend9(), DATA_FONT);
	            addHourData(table, report.getTot10(), report.getCpl10(), report.getPend10(), DATA_FONT);
	            addHourData(table, report.getTot11(), report.getCpl11(), report.getPend11(), DATA_FONT);
	            addHourData(table, report.getTot12(), report.getCpl12(), report.getPend12(), DATA_FONT);
	            
	            addHourData(table, report.getTot13(), report.getCpl13(), report.getPend13(), DATA_FONT);
	            addHourData(table, report.getTot14(), report.getCpl14(), report.getPend14(), DATA_FONT);
	            addHourData(table, report.getTot15(), report.getCpl15(), report.getPend15(), DATA_FONT);
	            addHourData(table, report.getTot16(), report.getCpl16(), report.getPend16(), DATA_FONT);

	            addHourData(table, report.getTot17(), report.getCpl17(), report.getPend17(), DATA_FONT);
	            addHourData(table, report.getTot18(), report.getCpl18(), report.getPend18(), DATA_FONT);
	            addHourData(table, report.getTot19(), report.getCpl19(), report.getPend19(), DATA_FONT);
	            addHourData(table, report.getTot20(), report.getCpl20(), report.getPend20(), DATA_FONT);

	            addHourData(table, report.getTot21(), report.getCpl21(), report.getPend21(), DATA_FONT);
	            addHourData(table, report.getTot22(), report.getCpl22(), report.getPend22(), DATA_FONT);
	            addHourData(table, report.getTot23(), report.getCpl23(), report.getPend23(), DATA_FONT);
	            addHourData(table, report.getTot24(), report.getCpl24(), report.getPend24(), DATA_FONT);

	            BigDecimal total = report.getTot1().add(report.getTot2()).add(report.getTot3()).add(report.getTot4())
						.add(report.getTot5()).add(report.getTot6()).add(report.getTot7()).add(report.getTot8())
						.add(report.getTot9()).add(report.getTot10()).add(report.getTot11()).add(report.getTot12())
						.add(report.getTot13()).add(report.getTot14()).add(report.getTot15()).add(report.getTot16())
						.add(report.getTot17()).add(report.getTot18()).add(report.getTot19()).add(report.getTot20())
						.add(report.getTot21()).add(report.getTot22()).add(report.getTot23()).add(report.getTot24());
	            
	            BigDecimal totalCpl = report.getCpl1().add(report.getCpl2()).add(report.getCpl3()).add(report.getCpl4())
						.add(report.getCpl5()).add(report.getCpl6()).add(report.getCpl7()).add(report.getCpl8())
						.add(report.getCpl9()).add(report.getCpl10()).add(report.getCpl11()).add(report.getCpl12())
						.add(report.getCpl13()).add(report.getCpl14()).add(report.getCpl15()).add(report.getCpl16())
						.add(report.getCpl17()).add(report.getCpl18()).add(report.getCpl19()).add(report.getCpl20())
						.add(report.getCpl21()).add(report.getCpl22()).add(report.getCpl23()).add(report.getCpl24());
				
	            BigDecimal pend = report.getPend1().add(report.getPend2()).add(report.getPend3()).add(report.getPend4())
						.add(report.getPend5()).add(report.getPend6()).add(report.getPend7()).add(report.getPend8())
						.add(report.getPend9()).add(report.getPend10()).add(report.getPend11()).add(report.getPend12())
						.add(report.getPend13()).add(report.getPend14()).add(report.getPend15()).add(report.getPend16())
						.add(report.getPend17()).add(report.getPend18()).add(report.getPend19()).add(report.getPend20())
						.add(report.getPend21()).add(report.getPend22()).add(report.getPend23()).add(report.getPend24());
				 
				 
		            grandTot = grandTot.add(total);
		            grandTotCpl =grandTotCpl.add(totalCpl);
		            grandTotPend = grandTotPend.add(pend);


	            
	            addHourData(table,total, totalCpl,pend,TOTAL_FONT);
	        }

           // GRAND TOTAL
	        PdfPCell footerLabel = new PdfPCell(new Phrase("TOTAL", TOTAL_FONT));
	        footerLabel.setBackgroundColor(BaseColor.LIGHT_GRAY);
	        footerLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
	        footerLabel.setColspan(2); 
	        table.addCell(footerLabel);

	        addHourData(table,
	            circleList.stream().map(r->r.getTot1()).reduce(BigDecimal.ZERO, BigDecimal::add),
	            circleList.stream().map(r->r.getCpl1()).reduce(BigDecimal.ZERO, BigDecimal::add),
	            circleList.stream().map(r->r.getPend1()).reduce(BigDecimal.ZERO, BigDecimal::add),
	            TOTAL_FONT);
	        
	        addHourData(table,
	                circleList.stream().map(r->r.getTot2()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getCpl2()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getPend2()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                circleList.stream().map(r->r.getTot3()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getCpl3()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getPend3()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                circleList.stream().map(r->r.getTot4()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getCpl4()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getPend4()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                circleList.stream().map(r->r.getTot5()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getCpl5()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getPend5()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                circleList.stream().map(r->r.getTot6()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getCpl6()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getPend6()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                circleList.stream().map(r->r.getTot7()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getCpl7()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getPend7()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                circleList.stream().map(r->r.getTot8()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getCpl8()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getPend8()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                circleList.stream().map(r->r.getTot9()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getCpl9()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getPend9()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                circleList.stream().map(r->r.getTot10()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getCpl10()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getPend10()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                circleList.stream().map(r->r.getTot11()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getCpl11()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getPend11()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                circleList.stream().map(r->r.getTot12()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getCpl12()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getPend12()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                circleList.stream().map(r->r.getTot13()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getCpl13()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getPend13()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                circleList.stream().map(r->r.getTot14()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getCpl14()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getPend14()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                circleList.stream().map(r->r.getTot15()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getCpl15()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getPend15()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                circleList.stream().map(r->r.getTot16()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getCpl16()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getPend16()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                circleList.stream().map(r->r.getTot17()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getCpl17()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getPend17()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                circleList.stream().map(r->r.getTot18()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getCpl18()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getPend18()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                circleList.stream().map(r->r.getTot19()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getCpl19()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getPend19()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                circleList.stream().map(r->r.getTot20()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getCpl20()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getPend20()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                circleList.stream().map(r->r.getTot21()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getCpl21()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getPend21()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                circleList.stream().map(r->r.getTot22()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getCpl22()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getPend22()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                circleList.stream().map(r->r.getTot23()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getCpl23()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getPend23()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                circleList.stream().map(r->r.getTot24()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getCpl24()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                circleList.stream().map(r->r.getPend24()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	       
	        addHourData(table, grandTot, grandTotCpl, grandTotPend, TOTAL_FONT);

	        document.add(table);
	        document.close();

	        pdfFile = DefaultStreamedContent.builder()
	            .contentType("application/pdf")
	            .name("CircleWise_Hourly_Splitup_Abstract_For_A_Date.pdf")
	            .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
	            .build();

	    } catch (Exception e) {
	        throw new DocumentException("PDF generation failed: " + e.getMessage());
	    }
	}
	
  public void exportSectionsToPdf(List<TmpYearWiseSections> sectionList) throws DocumentException {
		
		final int HOURS = 24;
	    final int SUB_COLUMNS = 3;
	    final int COLUMN_COUNT = 1+ 2 + (HOURS * SUB_COLUMNS) + 3; 
	    
	    final float SNO_COLUMN_WIDTH = 5f;
	    final float FIRST_COLUMN_WIDTH = 15f;
	    final float OTHER_COLUMN_WIDTH = 4f;
	    final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
	    final Font TOTAL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
	    final Font DATA_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
	    final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
	    
	    
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
	    
	    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	    String fromDate = sdf.format(dmFilter.getFromDate());

	    Document document = new Document(PageSize.A0.rotate());
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	    try {
	        PdfWriter.getInstance(document, outputStream);
	        document.open();
	        
	        PdfPTable table = new PdfPTable(COLUMN_COUNT);
	        table.setWidthPercentage(100);
	        table.setSpacingBefore(10f);
	        
	        Paragraph title = new Paragraph("HOURLY SECTION ABSTRACT REPORT", TITLE_FONT);
	        title.setAlignment(Element.ALIGN_CENTER);
	        document.add(title);
	        
	        Paragraph subTitle = new Paragraph("CIRCLE : "+selectedCircleName+" | "+"Date: " + fromDate + " | " + "Complaint Type : " +complaintType +" | "+"Device: "+device);
	        subTitle.setAlignment(Element.ALIGN_CENTER);
	        subTitle.setSpacingAfter(10);
	        document.add(subTitle);
	        
	        // COLUMN WIDTH
	        float[] columnWidths = new float[COLUMN_COUNT];
	        columnWidths[0] = SNO_COLUMN_WIDTH;
	        columnWidths[1] = FIRST_COLUMN_WIDTH;
	        columnWidths[2] = FIRST_COLUMN_WIDTH;
	        for (int i = 3; i < COLUMN_COUNT; i++) {
	            columnWidths[i] = OTHER_COLUMN_WIDTH;
	        }
	        table.setWidths(columnWidths);

	        //HEADER
	        addMergedHeaderCell(table, "S.NO", HEADER_FONT, 1, 2);
	        addMergedHeaderCell(table, "SECTION", HEADER_FONT, 1, 2);
	        addMergedHeaderCell(table, "DIVISION", HEADER_FONT, 1, 2);
	        for (int hour = 1; hour <= HOURS; hour++) {
	            addMergedHeaderCell(table, (hour - 1) + "-" + hour, HEADER_FONT, 3, 1);
	        }
	        addMergedHeaderCell(table, "TOTAL", HEADER_FONT, 3, 1);
	        
	        //SUB HEADER
	        String[] subHeaders = {"Revd.", "Comp.", "Pend."};
	        for (int i = 0; i < HOURS + 1; i++) { 
	            for (String subHeader : subHeaders) {
	                PdfPCell cell = new PdfPCell(new Phrase(subHeader, HEADER_FONT));
	                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
	                table.addCell(cell);
	            }
	        }
	        BigDecimal grandTot = BigDecimal.ZERO;
	        BigDecimal grandTotCpl =BigDecimal.ZERO;
	        BigDecimal grandTotPend = BigDecimal.ZERO;

	        //TABLE DATA
	        int serialNumber =1;
	        for (TmpYearWiseSections report : sectionList) {
	            table.addCell(createDataCell(String.valueOf(serialNumber++), DATA_FONT, Element.ALIGN_LEFT));
	            table.addCell(createDataCell(report.getSectionName(), DATA_FONT, Element.ALIGN_LEFT));
	            table.addCell(createDataCell(report.getDivisionName(), DATA_FONT, Element.ALIGN_LEFT));

	            
	            addHourData(table, report.getTot1(), report.getCpl1(), report.getPend1(), DATA_FONT);
	            addHourData(table, report.getTot2(), report.getCpl2(), report.getPend2(), DATA_FONT);
	            addHourData(table, report.getTot3(), report.getCpl3(), report.getPend3(), DATA_FONT);
	            addHourData(table, report.getTot4(), report.getCpl4(), report.getPend4(), DATA_FONT);
	            
	            addHourData(table, report.getTot5(), report.getCpl5(), report.getPend5(), DATA_FONT);
	            addHourData(table, report.getTot6(), report.getCpl6(), report.getPend6(), DATA_FONT);
	            addHourData(table, report.getTot7(), report.getCpl7(), report.getPend7(), DATA_FONT);
	            addHourData(table, report.getTot8(), report.getCpl8(), report.getPend8(), DATA_FONT);
	            
	            addHourData(table, report.getTot9(), report.getCpl9(), report.getPend9(), DATA_FONT);
	            addHourData(table, report.getTot10(), report.getCpl10(), report.getPend10(), DATA_FONT);
	            addHourData(table, report.getTot11(), report.getCpl11(), report.getPend11(), DATA_FONT);
	            addHourData(table, report.getTot12(), report.getCpl12(), report.getPend12(), DATA_FONT);
	            
	            addHourData(table, report.getTot13(), report.getCpl13(), report.getPend13(), DATA_FONT);
	            addHourData(table, report.getTot14(), report.getCpl14(), report.getPend14(), DATA_FONT);
	            addHourData(table, report.getTot15(), report.getCpl15(), report.getPend15(), DATA_FONT);
	            addHourData(table, report.getTot16(), report.getCpl16(), report.getPend16(), DATA_FONT);

	            addHourData(table, report.getTot17(), report.getCpl17(), report.getPend17(), DATA_FONT);
	            addHourData(table, report.getTot18(), report.getCpl18(), report.getPend18(), DATA_FONT);
	            addHourData(table, report.getTot19(), report.getCpl19(), report.getPend19(), DATA_FONT);
	            addHourData(table, report.getTot20(), report.getCpl20(), report.getPend20(), DATA_FONT);

	            addHourData(table, report.getTot21(), report.getCpl21(), report.getPend21(), DATA_FONT);
	            addHourData(table, report.getTot22(), report.getCpl22(), report.getPend22(), DATA_FONT);
	            addHourData(table, report.getTot23(), report.getCpl23(), report.getPend23(), DATA_FONT);
	            addHourData(table, report.getTot24(), report.getCpl24(), report.getPend24(), DATA_FONT);

	            BigDecimal total = report.getTot1().add(report.getTot2()).add(report.getTot3()).add(report.getTot4())
						.add(report.getTot5()).add(report.getTot6()).add(report.getTot7()).add(report.getTot8())
						.add(report.getTot9()).add(report.getTot10()).add(report.getTot11()).add(report.getTot12())
						.add(report.getTot13()).add(report.getTot14()).add(report.getTot15()).add(report.getTot16())
						.add(report.getTot17()).add(report.getTot18()).add(report.getTot19()).add(report.getTot20())
						.add(report.getTot21()).add(report.getTot22()).add(report.getTot23()).add(report.getTot24());
	            
	            BigDecimal totalCpl = report.getCpl1().add(report.getCpl2()).add(report.getCpl3()).add(report.getCpl4())
						.add(report.getCpl5()).add(report.getCpl6()).add(report.getCpl7()).add(report.getCpl8())
						.add(report.getCpl9()).add(report.getCpl10()).add(report.getCpl11()).add(report.getCpl12())
						.add(report.getCpl13()).add(report.getCpl14()).add(report.getCpl15()).add(report.getCpl16())
						.add(report.getCpl17()).add(report.getCpl18()).add(report.getCpl19()).add(report.getCpl20())
						.add(report.getCpl21()).add(report.getCpl22()).add(report.getCpl23()).add(report.getCpl24());
				
	            BigDecimal pend = report.getPend1().add(report.getPend2()).add(report.getPend3()).add(report.getPend4())
						.add(report.getPend5()).add(report.getPend6()).add(report.getPend7()).add(report.getPend8())
						.add(report.getPend9()).add(report.getPend10()).add(report.getPend11()).add(report.getPend12())
						.add(report.getPend13()).add(report.getPend14()).add(report.getPend15()).add(report.getPend16())
						.add(report.getPend17()).add(report.getPend18()).add(report.getPend19()).add(report.getPend20())
						.add(report.getPend21()).add(report.getPend22()).add(report.getPend23()).add(report.getPend24());
				 
				 
		            grandTot = grandTot.add(total);
		            grandTotCpl =grandTotCpl.add(totalCpl);
		            grandTotPend = grandTotPend.add(pend);


	            
	            addHourData(table,total, totalCpl,pend,TOTAL_FONT);
	        }

           // GRAND TOTAL
	        PdfPCell footerLabel = new PdfPCell(new Phrase("TOTAL", TOTAL_FONT));
	        footerLabel.setBackgroundColor(BaseColor.LIGHT_GRAY);
	        footerLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
	        footerLabel.setRowspan(2);
	        footerLabel.setColspan(3);
	        table.addCell(footerLabel);

	        addHourData(table,
	        		sectionList.stream().map(r->r.getTot1()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getCpl1()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getPend1()).reduce(BigDecimal.ZERO, BigDecimal::add),
	            TOTAL_FONT);
	        
	        addHourData(table,
	        		sectionList.stream().map(r->r.getTot2()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getCpl2()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getPend2()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	        		sectionList.stream().map(r->r.getTot3()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                sectionList.stream().map(r->r.getCpl3()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                sectionList.stream().map(r->r.getPend3()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	        		sectionList.stream().map(r->r.getTot4()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getCpl4()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getPend4()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	        		sectionList.stream().map(r->r.getTot5()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getCpl5()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getPend5()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	        		sectionList.stream().map(r->r.getTot6()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getCpl6()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getPend6()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	        		sectionList.stream().map(r->r.getTot7()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getCpl7()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getPend7()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	        		sectionList.stream().map(r->r.getTot8()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                sectionList.stream().map(r->r.getCpl8()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                sectionList.stream().map(r->r.getPend8()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	        		sectionList.stream().map(r->r.getTot9()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getCpl9()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getPend9()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	        		sectionList.stream().map(r->r.getTot10()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getCpl10()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getPend10()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	        		sectionList.stream().map(r->r.getTot11()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getCpl11()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getPend11()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	        		sectionList.stream().map(r->r.getTot12()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getCpl12()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getPend12()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	        		sectionList.stream().map(r->r.getTot13()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getCpl13()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getPend13()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	        		sectionList.stream().map(r->r.getTot14()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getCpl14()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getPend14()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	        		sectionList.stream().map(r->r.getTot15()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getCpl15()).reduce(BigDecimal.ZERO, BigDecimal::add),
	        		sectionList.stream().map(r->r.getPend15()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                sectionList.stream().map(r->r.getTot16()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                sectionList.stream().map(r->r.getCpl16()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                sectionList.stream().map(r->r.getPend16()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                sectionList.stream().map(r->r.getTot17()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                sectionList.stream().map(r->r.getCpl17()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                sectionList.stream().map(r->r.getPend17()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                sectionList.stream().map(r->r.getTot18()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                sectionList.stream().map(r->r.getCpl18()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                sectionList.stream().map(r->r.getPend18()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                sectionList.stream().map(r->r.getTot19()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                sectionList.stream().map(r->r.getCpl19()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                sectionList.stream().map(r->r.getPend19()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                sectionList.stream().map(r->r.getTot20()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                sectionList.stream().map(r->r.getCpl20()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                sectionList.stream().map(r->r.getPend20()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                sectionList.stream().map(r->r.getTot21()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                sectionList.stream().map(r->r.getCpl21()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                sectionList.stream().map(r->r.getPend21()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                sectionList.stream().map(r->r.getTot22()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                sectionList.stream().map(r->r.getCpl22()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                sectionList.stream().map(r->r.getPend22()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                sectionList.stream().map(r->r.getTot23()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                sectionList.stream().map(r->r.getCpl23()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                sectionList.stream().map(r->r.getPend23()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	        addHourData(table,
	                sectionList.stream().map(r->r.getTot24()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                sectionList.stream().map(r->r.getCpl24()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                sectionList.stream().map(r->r.getPend24()).reduce(BigDecimal.ZERO, BigDecimal::add),
	                TOTAL_FONT);
	        
	       
	        addHourData(table, grandTot, grandTotCpl, grandTotPend, TOTAL_FONT);

	        document.add(table);
	        document.close();

	        pdfFile = DefaultStreamedContent.builder()
	            .contentType("application/pdf")
	            .name("SectionWise_Hourly_Splitup_Abstract_For_A_Date.pdf")
	            .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
	            .build();

	    } catch (Exception e) {
	        throw new DocumentException("PDF generation failed: " + e.getMessage());
	    }
	}

	private void addMergedHeaderCell(PdfPTable table, String text, Font font, int colSpan, int rowSpan) {
	    PdfPCell cell = new PdfPCell(new Phrase(text, font));
	    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	    cell.setColspan(colSpan);
	    cell.setRowspan(rowSpan);
	    cell.setPadding(5);
	    cell.setBackgroundColor(BaseColor.GRAY);
	    table.addCell(cell);
	}
	


	private PdfPCell createDataCell(String text, Font font, int alignment) {
	    PdfPCell cell = new PdfPCell(new Phrase(text, font));
	    cell.setHorizontalAlignment(alignment);
	    return cell;
	}

	private void addHourData(PdfPTable table, BigDecimal revd, BigDecimal cpl, BigDecimal pend, Font font) {
	    table.addCell(createDataCell(revd.toString(), font, Element.ALIGN_CENTER));
	    table.addCell(createDataCell(cpl.toString(), font, Element.ALIGN_CENTER));
	    table.addCell(createDataCell(pend.toString(), font, Element.ALIGN_CENTER));
	}

	 
	 
	// COMPLAINT LIST EXCEL DOWNLOAD
	
	  public void exportComplaintListToExcel(List<ViewComplaintReportValueBean> complaintList) throws IOException {
		  HSSFWorkbook workbook = new HSSFWorkbook();
		  Sheet sheet = workbook.createSheet("Hourly_Abstract_ComplaintList_Report");
		  
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
		                .name("Hourly_Abstract_ComplaintList_Report.xls")
		                .contentType("application/vnd.ms-excel")
		                .stream(() -> inputStream)
		                .build();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
	  }
	  
	  //COMPLAINT LIST PDF DOWNLOAD
	  
	  public void exportComplaintListToPdf(List<ViewComplaintReportValueBean> complaintList) {
		  Document document = new Document(PageSize.A4.rotate());
		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		    try {
		        PdfWriter.getInstance(document, outputStream);
		        document.open();
		        
		        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD, BaseColor.BLACK);
		        Paragraph title = new Paragraph("HOURLY ABSTRACT COMPLAINT LIST", titleFont);
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
		            .name("Hourly_Abstract_ComplaintList_Report.pdf")
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
		
		
		
		public List<String> getHourHeaders() {
		    return IntStream.range(0, 24)
		            .mapToObj(this::formatHourRange)
		            .collect(Collectors.toList());
		}

		private String formatHourRange(int hour) {
		    return hour + "-" + (hour + 1);
		}


	 

	public DataModel getDmFilter() {
		return dmFilter;
	}

	public void setDmFilter(DataModel dmFilter) {
		this.dmFilter = dmFilter;
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

	public List<TmpYearWiseCircle> getCircleList() {
		return circleList;
	}

	public void setCircleList(List<TmpYearWiseCircle> circleList) {
		this.circleList = circleList;
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

	public String getSelectedComplaintType() {
		return selectedComplaintType;
	}

	public void setSelectedComplaintType(String selectedComplaintType) {
		this.selectedComplaintType = selectedComplaintType;
	}

	public String getSelectedDevice() {
		return selectedDevice;
	}

	public void setSelectedDevice(String selectedDevice) {
		this.selectedDevice = selectedDevice;
	}

	public boolean isCameFromDateWiseAbstractReport() {
		return cameFromDateWiseAbstractReport;
	}

	public void setCameFromDateWiseAbstractReport(boolean cameFromDateWiseAbstractReport) {
		this.cameFromDateWiseAbstractReport = cameFromDateWiseAbstractReport;
	}


	public String getSelectedCircleIdFromDateWiseAbstract() {
		return selectedCircleIdFromDateWiseAbstract;
	}

	public void setSelectedCircleIdFromDateWiseAbstract(String selectedCircleIdFromDateWiseAbstract) {
		this.selectedCircleIdFromDateWiseAbstract = selectedCircleIdFromDateWiseAbstract;
	}

	public String getSelectedCircleNameFromDateWiseAbstract() {
		return selectedCircleNameFromDateWiseAbstract;
	}

	public void setSelectedCircleNameFromDateWiseAbstract(String selectedCircleNameFromDateWiseAbstract) {
		this.selectedCircleNameFromDateWiseAbstract = selectedCircleNameFromDateWiseAbstract;
	}

	public String getSelectedDateFromDateWiseAbstract() {
		return selectedDateFromDateWiseAbstract;
	}

	public void setSelectedDateFromDateWiseAbstract(String selectedDateFromDateWiseAbstract) {
		this.selectedDateFromDateWiseAbstract = selectedDateFromDateWiseAbstract;
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

	public boolean isCameFromInsideSection() {
		return cameFromInsideSection;
	}

	public void setCameFromInsideSection(boolean cameFromInsideSection) {
		this.cameFromInsideSection = cameFromInsideSection;
	}

	public boolean isCameFromInsideReport() {
		return cameFromInsideReport;
	}

	public void setCameFromInsideReport(boolean cameFromInsideReport) {
		this.cameFromInsideReport = cameFromInsideReport;
	}

	public boolean isCameViaCircleReport() {
		return cameViaCircleReport;
	}

	public void setCameViaCircleReport(boolean cameViaCircleReport) {
		this.cameViaCircleReport = cameViaCircleReport;
	}



}
