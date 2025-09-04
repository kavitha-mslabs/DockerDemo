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
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.transform.Transformers;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.log.SysoCounter;
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
import tneb.ccms.admin.valuebeans.OutagesReportValueBean;
import tneb.ccms.admin.valuebeans.TmpCtCirBean;
import tneb.ccms.admin.valuebeans.TmpCtSecBean;
import tneb.ccms.admin.valuebeans.ViewComplaintReportValueBean;

import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;

@Named
@ViewScoped
public class OutagesReport implements Serializable {

	private static final long serialVersionUID = 1L;
	private SessionFactory sessionFactory;
	DataModel dmFilter;
	private boolean initialized = false;
	private boolean cameFromInsideReport = false;
	List<TmpCtCirBean> reports = new ArrayList<>();
	List<TmpCtSecBean> sectionList = new ArrayList<>();
	StreamedContent excelFile;
	StreamedContent pdfFile;
	List<ViewComplaintReportValueBean> complaintList = new ArrayList<>();
	String selectedCircleName =null;
	String selectedSectionName = null;
	String redirectFrom;
	private Date currentDate = new Date();
	private String currentYear = String.valueOf(Year.now().getValue());
	ViewComplaintReportValueBean selectedComplaintId;
	List<CompDeviceValueBean> devices;
	
	
	List<OutagesReportValueBean> reportData;
	

	public List<OutagesReportValueBean> getReportData() {
		return reportData;
	}

	public void setReportData(List<OutagesReportValueBean> reportData) {
		this.reportData = reportData;
	}

	public List<CompDeviceValueBean> getDevice() {
		return devices;
	}

	public void setDevice(List<CompDeviceValueBean> devices) {
		this.devices = devices;
	}


    


	@PostConstruct
	public void init() {
		System.out.println("Initializing OUTAGES REPORT...");
		sessionFactory = HibernateUtil.getSessionFactory();
		dmFilter = new DataModel();
		loadAllDevicesAndCategories();
	}
	
	public void resetIfNeeded() {
        if (!FacesContext.getCurrentInstance().isPostback() && initialized) {
        	reportData = null;
        	dmFilter.setFromDate(null);
        	dmFilter.setToDate(null);
        }
        initialized = true;
    }
    
	@SuppressWarnings("unchecked")
	@Transactional
	private void loadAllDevicesAndCategories() {
	    Session session = null;
	    try {
	        session = sessionFactory.openSession();
	        
	        String hql = "FROM CompDeviceBean d ORDER BY d.devName ASC";
	        Query<CompDeviceBean> query = session.createQuery(hql, CompDeviceBean.class);
	        
	        List<CompDeviceBean> devicesBean = query.getResultList();
	        
	        devices = devicesBean.stream()
	            .map(CompDeviceValueBean::convertCompDeviceBeanToCompDeviceValueBean)
	            .collect(Collectors.toList());
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        devices = new ArrayList<>(); 
	    } finally {
	        if (session != null && session.isOpen()) {
	            session.close();
	        }
	    }
	}

	
	public void clearFiltersAndCircleReport() {
		dmFilter = new DataModel();
		reports = new ArrayList<>();
	}

	private void setDefaultDatesIfNeeded() {

		if (dmFilter == null) {
			dmFilter = new DataModel();
		}

		if (dmFilter.getFromDate() == null && dmFilter.getToDate() == null) {

			Date now = new Date();
	        dmFilter.setFromDate(now);
	        dmFilter.setToDate(now);

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

	}

	@Transactional
	public void generateOutagesReport() {
	    setDefaultDatesIfNeeded();
	    Session session = null;
	    try {
	        session = sessionFactory.openSession();
	        session.beginTransaction();

	        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	        String formattedFromDate = sdf.format(dmFilter.getFromDate());
	        
	        
	    	HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
	    	        .getExternalContext().getSession(false);

	    	AdminUserValueBean adminUserValueBean = (AdminUserValueBean)
	    	        httpsession.getAttribute("sessionAdminValueBean");
	 		CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");
	        
	        List<OutagesReportValueBean> finalList = new ArrayList<>();

	        @SuppressWarnings("unchecked")
			List<Object[]> scheduledResults = session.createNativeQuery(
	                "SELECT b.ss_name, c.fdr_name, 'ALL DTS', c.fdr_code, " +
	                "TO_CHAR(a.tripdt,'DD-MM-YYYY'), a.trip_fmtime, a.trip_totime, " +
	                "a.type, a.reason, 'SS MONITORING' " +
	                "FROM sch_outage a " +
	                "JOIN ssmastergis b ON b.ss_code = a.sscode " +
	                "JOIN feedermastergis c ON c.fdr_code = a.ssfcode " +
	                "WHERE a.tripdt = TO_DATE(:date, 'dd-mm-yyyy')"
	            )
	            .setParameter("date", formattedFromDate)
	            .list();
			
			 for (Object[] row : scheduledResults) {
				    OutagesReportValueBean dto = new OutagesReportValueBean();
	                dto.setSsName((String) row[0]);
	                dto.setFeederName((String) row[1]);
	                dto.setDtName((String) row[2]);
	                String fdrCode = (String) row[3];
	                dto.setMaintainDate((String) row[4]);
	                dto.setFromTime((String) row[5]);
	                dto.setToTime((String) row[6]);
	                dto.setOutageType((String) row[7]);
	                dto.setReason((String) row[8]);
	                dto.setEnteredThrough((String) row[9]);

	                // 2. Circle and Section for Scheduled
	                @SuppressWarnings("unchecked")
	                Object[] areaList =  (Object[])session.createNativeQuery(
	                		"SELECT " +
	                	             "    LISTAGG(area_name, ',') WITHIN GROUP (ORDER BY area_name) AS area_names, " +
	                	             "    MIN(REGION_ID) AS REGION_ID, " +
	                	             "    MIN(CIRCLE_ID) AS CIRCLE_ID, " +
	                	             "    MIN(DIVISION_ID) AS DIVISION_ID, " +
	                	             "    MIN(SUB_DIVISION_ID) AS SUB_DIVISION_ID, " +
	                	             "    MIN(SECTION_ID) AS SECTION_ID " +
	                	             "FROM ( " +
	                	             "    SELECT DISTINCT " +
	                	             "        b.cir_name || '-' || a.name AS area_name, " +
	                	             "        a.region_id AS REGION_ID, " +
	                	             "        a.circle_id AS CIRCLE_ID, " +
	                	             "        a.division_id AS DIVISION_ID, " +
	                	             "        a.sub_division_id AS SUB_DIVISION_ID, " +
	                	             "        a.id AS SECTION_ID " +
	                	             "    FROM section a " +
	                	             "    JOIN dtmastergis b ON TO_NUMBER(b.sec_code) = TO_NUMBER(a.code) " +
	                	             "        AND b.region_id = a.region_id " +
	                	             "    WHERE b.fdr_code = :fdCd " +
	                	             ")"
	                	)
	                		.setParameter("fdCd", fdrCode)
	                		.getSingleResult();

	                dto.setCircleAndSection((String) areaList[0]);
	                dto.setRegionId((BigDecimal) areaList[1] != null ? ((BigDecimal) areaList[1]).intValue() : null);
	                dto.setCircleId((BigDecimal) areaList[2] != null ? ((BigDecimal) areaList[2]).intValue() : null);
	                dto.setDivisionId((BigDecimal) areaList[3] != null ? ((BigDecimal) areaList[3]).intValue() : null);
	                dto.setSubDivisionId((BigDecimal) areaList[4] != null ? ((BigDecimal) areaList[4]).intValue() : null);
	                dto.setSectionId((BigDecimal) areaList[5] != null ? ((BigDecimal) areaList[5]).intValue() : null);

	                finalList.add(dto);
	            }

			 @SuppressWarnings("unchecked")
			 List<Object[]> unscheduledResults = session.createNativeQuery(
			     "SELECT b.ss_name, b.fdr_name, b.dt_name, b.dt_code, " +
			     "TO_CHAR(TRUNC(a.frprd), 'DD-MM-YYYY'), " +
			     "TO_CHAR(a.frprd, 'HH24:MI'), TO_CHAR(a.toprd, 'HH24:MI'), " +
			     "'Unscheduled', a.description, DECODE(a.entryuser, 'mAdmin', 'Minnagam Admin',a.entryuser) " +
			     "FROM pl_outages a " +
			     "JOIN dtmastergis b ON a.ssfdrstrcode = b.dt_code " +
			     "WHERE TRUNC(a.frprd) = TO_DATE(:date, 'dd-mm-yyyy')"
			 )
			 .setParameter("date", formattedFromDate)
			 .list();


		            for (Object[] row : unscheduledResults) {
		                OutagesReportValueBean dto = new OutagesReportValueBean();
		                dto.setSsName((String) row[0]);
		                dto.setFeederName((String) row[1]);
		                dto.setDtName((String) row[2]);
		                String dtCode = (String) row[3];
		                dto.setMaintainDate((String) row[4]);
		                dto.setFromTime(row[5].toString());
		                dto.setToTime(row[6].toString());
		                dto.setOutageType((String) row[7]);
		                dto.setReason((String) row[8]);
		                dto.setEnteredThrough((String) row[9]);

		                // 4. Circle and Section for Unscheduled
		                @SuppressWarnings("unchecked")
		                Object[] areaList = (Object[]) session.createNativeQuery(
		                		 "SELECT " +
		                	             "    LISTAGG(area_name, ', ') WITHIN GROUP (ORDER BY area_name) AS area_names, " +
		                	             "    MIN(REGION_ID) AS REGION_ID, " +
		                	             "    MIN(CIRCLE_ID) AS CIRCLE_ID, " +
		                	             "    MIN(DIVISION_ID) AS DIVISION_ID, " +
		                	             "    MIN(SUB_DIVISION_ID) AS SUB_DIVISION_ID, " +
		                	             "    MIN(SECTION_ID) AS SECTION_ID " +
		                	             "FROM ( " +
		                	             "    SELECT DISTINCT " +
		                	             "        b.cir_name || ' - ' || a.name AS area_name, " +
		                	             "        a.region_id AS REGION_ID, " +
		                	             "        a.circle_id AS CIRCLE_ID, " +
		                	             "        a.division_id AS DIVISION_ID, " +
		                	             "        a.sub_division_id AS SUB_DIVISION_ID, " +
		                	             "        a.id AS SECTION_ID " +
		                	             "    FROM section a " +
		                	             "    JOIN dtmastergis b ON TO_NUMBER(b.sec_code) = TO_NUMBER(a.code) " +
		                	             "        AND b.region_id = a.region_id " +
		                	             "    WHERE b.dt_code = :dtCd " +
		                	             ")"
		                )
		                .setParameter("dtCd", dtCode)
		                .getSingleResult();
		                
		                System.err.println("THE DT CODE -------+"+dtCode);

		                dto.setCircleAndSection((String)areaList[0]);
		                dto.setRegionId((BigDecimal) areaList[1] != null ? ((BigDecimal) areaList[1]).intValue() : null);
		                dto.setCircleId((BigDecimal) areaList[2] != null ? ((BigDecimal) areaList[2]).intValue() : null);
		                dto.setDivisionId((BigDecimal) areaList[3] != null ? ((BigDecimal) areaList[3]).intValue() : null);
		                dto.setSubDivisionId((BigDecimal) areaList[4] != null ? ((BigDecimal) areaList[4]).intValue() : null);
		                dto.setSectionId((BigDecimal) areaList[5] != null ? ((BigDecimal) areaList[5]).intValue() : null);
		                
		                finalList.add(dto);
		            }
		            
		            if(adminUserValueBean!=null) {
		            
		    	    	Integer roleId = adminUserValueBean.getRoleId();
		    	    	Integer regionID = adminUserValueBean.getRegionId();
		    	    	Integer circleID = adminUserValueBean.getCircleId();
		    	    	Integer divisionID = adminUserValueBean.getDivisionId();
		    	    	Integer subDivisionID = adminUserValueBean.getSubDivisionId();
		    	    	Integer sectionID = adminUserValueBean.getSectionId();
		    	    	
		            // HEAD QUATERS
			        if(roleId>=6 && roleId<=9) {
			        	 this.reportData= finalList;
			        }
			        //REGION
			        else if(roleId==5) {      
			        	 this.reportData= finalList.stream().filter(r ->r.getRegionId().equals(regionID)).collect(Collectors.toList());
			        }
			        //CIRCLE
			        else if(roleId==4) {
			        	 this.reportData= finalList.stream().filter(r ->r.getRegionId().equals(regionID)).filter(c ->c.getCircleId().equals(circleID)).collect(Collectors.toList());
			        }
			        //DIVISION
			        else if(roleId==3) {
			        	 this.reportData= finalList.stream().filter(r ->r.getRegionId().equals(regionID)).filter(c ->c.getCircleId().equals(circleID))
			        			.filter(d ->d.getDivisionId().equals(divisionID)).collect(Collectors.toList());
			        }
			      //SUB DIVISION
			        else if(roleId==2) {
			        	 this.reportData= finalList.stream().filter(r ->r.getRegionId().equals(regionID)).filter(c ->c.getCircleId().equals(circleID))
			        			.filter(d ->d.getDivisionId().equals(divisionID)).filter(sd ->sd.getSubDivisionId().equals(subDivisionID)).collect(Collectors.toList());
			        }
			      //SECTION
			        else if(roleId==1) {
			        	 this.reportData= finalList.stream().filter(r ->r.getRegionId().equals(regionID)).filter(c ->c.getCircleId().equals(circleID))
			        			.filter(d ->d.getDivisionId().equals(divisionID)).filter(sd ->sd.getSubDivisionId().equals(subDivisionID))
			        			.filter(sec->sec.getSectionId().equals(sectionID)).collect(Collectors.toList());

			        }
			        else {
			        	 this.reportData= finalList;
			        }	
		            }
		            else if(callCenterValueBean!=null) {
		            	Integer roleId = callCenterValueBean.getRoleId();
		                Integer userId = callCenterValueBean.getId();

		                // Get circle IDs for the call center user
		                @SuppressWarnings("unchecked")
						List<Integer> circleIdList = session.createQuery(
		                    "select c.circleBean.id from CallCenterMappingBean c " +
		                    "where c.callCenterUserBean.id = :userId")
		                    .setParameter("userId", userId)
		                    .getResultList();
		                
		                if(roleId==1) {
		                	reportData = finalList.stream().filter(c->circleIdList.contains(c.getCircleId())).collect(Collectors.toList());
		                }
		                else if(roleId==7) {
		                	reportData = finalList.stream().filter(c->circleIdList.contains(c.getCircleId())).collect(Collectors.toList());
		                }

		            }
		            else {
		            	this.reportData = finalList;
		            }
		            
		            if(reportData.size()==0) {
		            	FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "NO DATA",
		    					"No Outages For the Given Date");
		    			FacesContext.getCurrentInstance().addMessage(null, message);
		    			return;

		            }
		            session.getTransaction().commit();
		        } catch (Exception e) {
	                   e.printStackTrace();
	    } finally {
	        if (session != null) {
	            session.close();
	        }
	    }
	}


	
	public void clearFilterAndPage() {
		dmFilter = new DataModel();
		dmFilter.setFromDate(null);
		reportData = new ArrayList<OutagesReportValueBean>();
	}
			 
			

	@Transactional
	private void fetchReports(Session session) {
		try {
			String hql = "SELECT REGCODE, REGNAME, CIRCODE, CIRNAME, BLTOT, BLCOM, BLPEN, METOT, MECOM, MEPEN, PFTOT, PFCOM, PFPEN, VFTOT, VFCOM, VFPEN, FITOT,FICOM, FIPEN, THTOT, THCOM, THPEN, TETOT, TECOM, TEPEN, CSTOT, CSCOM, CSPEN, OTTOT, OTCOM, OTPEN FROM TMP_CT_CIR";
																																																																					
																																																																					
			List<Object[]> results = session.createNativeQuery(hql).getResultList();
			reports = new ArrayList<>();

			for (Object[] row : results) {
				TmpCtCirBean report = new TmpCtCirBean();
				report.setRegcode((String) row[0]);
				report.setRegname((String) row[1]);
				report.setCircode((String) row[2]);
				report.setCirname((String) row[3]);

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
						 

				reports.add(report);
			}

			System.out.println("COUNT: " + reports.size());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in fetching report");
		}
	}
	
	public void redirectToCircleReport() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().redirect("categoryAndDeviceCircle.xhtml");
		
		
	}
	public void redirectToSectionReport() throws IOException {
		if ("circle".equals(redirectFrom)) {
			FacesContext.getCurrentInstance().getExternalContext().redirect("categoryAndDeviceCircle.xhtml");

		}else {
			FacesContext.getCurrentInstance().getExternalContext().redirect("categoryAndDeviceSection.xhtml");		
		}	
		
	}

	@Transactional
	public void fetchReportByCircle(String circleCode,String circleName) {

		Session session = null;
		try {
			session = sessionFactory.openSession();
			session.beginTransaction();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String formattedFromDate = sdf.format(dmFilter.getFromDate());
			String formattedToDate = sdf.format(dmFilter.getToDate());

			System.out.println("Processed From Date: " + formattedFromDate);
			System.out.println("Processed To Date: " + formattedToDate);
			System.out.println("THE CIRCLE CODE: " + circleCode);
			
			
			if(dmFilter.getSectionCode()==null) {
				dmFilter.setSectionCode("A");
			}

			session.createNativeQuery(
					"BEGIN CAT_SEC_ABST_ALL_DT_CIR_DEV(:circd,:sectionCode, :device, TO_DATE(:fromDate, 'YYYY-MM-DD'), TO_DATE(:toDate, 'YYYY-MM-DD')); END;")
					.setParameter("circd", circleCode)
					.setParameter("sectionCode", dmFilter.getSectionCode())
					.setParameter("device", dmFilter.getDevice())
					.setParameter("fromDate", formattedFromDate)
					.setParameter("toDate", formattedToDate)
					.executeUpdate();

			session.createNativeQuery("BEGIN CAT_SEC_ABST_SINGLE_ALL; END;").executeUpdate();

			session.flush();
			session.getTransaction().commit();
			
			String hql = "SELECT c.*,d.NAME as DIVISION_NAME FROM TMP_CT_SEC c,SECTION s,DIVISION d WHERE c.SECCODE = s.ID AND d.ID =s.DIVISION_ID ";
			
			List<Object[]> results = session.createNativeQuery(hql).getResultList();
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

			sectionList = circleSection.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode))
					.collect(Collectors.toList());
			selectedCircleName = circleName;	
			cameFromInsideReport= true;

			System.out.println("Section List size: " + sectionList.size());

			FacesContext.getCurrentInstance().getExternalContext().redirect("categoryAndDeviceSection.xhtml");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in database operation");
		}
	}
	
	
	
	
	@Transactional
	public void getComplaintListForSection(String secCode,String sectionName) throws IOException {
		try (Session session = sessionFactory.openSession()) {
			
			Timestamp fromDate = new Timestamp(dmFilter.getFromDate().getTime());
			Timestamp toDate = new Timestamp(dmFilter.getToDate().getTime());
			
			List<String> devices = new ArrayList<>();
			
			if (dmFilter.getDevice() != null) {
	            switch (dmFilter.getDevice().toUpperCase()) {
	                case "P": devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile")); break;
	                case "W": devices.add("web"); break;
	                case "S": devices.add("SM"); break;
	                case "A": devices.addAll(Arrays.asList("admin", "FOC")); break;
	                case "M": devices.add("MI"); break;
	                case "G": devices.add("MM"); break;
	                case "O": devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile","web","SM","MI","MM")); break;
	                case "L": devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile","web","SM","admin","FOC","MI","MM")); break;
	                default: throw new IllegalArgumentException("Invalid Device Type");
	            }
	        }
			
	
		Integer sectionCode = Integer.parseInt(secCode);
		
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
                "WHERE a.SECTION_ID = :sectionCode AND a.DEVICE IN :devices " +
                "AND a.created_on BETWEEN :fromDate AND :toDate";
				
		Query query = session.createNativeQuery(hql);
		query.setParameter("sectionCode", sectionCode);
		query.setParameter("fromDate", fromDate); 
		query.setParameter("toDate", toDate); 
		query.setParameter("devices", devices); 
		
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
		 redirectFrom = "section";
		
		FacesContext.getCurrentInstance().getExternalContext().redirect("categoryAndDeviceComplaintList.xhtml");
		
		}catch(Exception e){
			System.out.println("ERROR..........."+e);
		}
		
	}
	@Transactional
	public void getComplaintListForCircle() {

	    List<ViewComplaintReportBean> complaintBeanList;

	    try (Session session = sessionFactory.openSession()) {
	        Timestamp fromDate = new Timestamp(dmFilter.getFromDate().getTime());
	        Timestamp toDate = new Timestamp(dmFilter.getToDate().getTime());
	        
	        List<String> devices = new ArrayList<>();
	        
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
	        
	        if (dmFilter.getDevice() != null) {
	            switch (dmFilter.getDevice().toUpperCase()) {
	                case "P": devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile")); break;
	                case "W": devices.add("web"); break;
	                case "S": devices.add("SM"); break;
	                case "A": devices.addAll(Arrays.asList("admin", "FOC")); break;
	                case "M": devices.add("MI"); break;
	                case "G": devices.add("MM"); break;
	                case "O": devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile","web","SM","MI","MM")); break;
	                case "L": devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile","web","SM","admin","FOC","MI","MM")); break;
	                default: throw new IllegalArgumentException("Invalid Device Type");
	            }
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
	                "WHERE a.STATUS_ID IN :statusIDs AND a.DEVICE IN :devices " +
	                "AND a.created_on BETWEEN :fromDate AND :toDate";
					
			Query query = session.createNativeQuery(hql);
	        query.setParameter("fromDate", fromDate);
	        query.setParameter("toDate", toDate);
	        query.setParameter("statusIDs", statusIDs);
	        query.setParameter("devices", devices);
			
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
	        
	        redirectFrom = "circle";
	        selectedSectionName=null;

	        
	        FacesContext.getCurrentInstance().getExternalContext().redirect("categoryAndDeviceComplaintList.xhtml");
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
	//CIRCLE REPORT TO EXCEL DOWNLOAD
	public void exportToExcel(List<OutagesReportValueBean> reportData) throws IOException {
		 HSSFWorkbook workbook = new HSSFWorkbook();
		  Sheet sheet = workbook.createSheet("Outages_Entered_Report");
		  
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
		    headingCell.setCellValue("OUTAGES ENTERED REPORT");
		    headingCell.setCellStyle(headingStyle);
		    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10)); 
		    
		    Row dateRow = sheet.createRow(1);
		    Cell dateCell = dateRow.createCell(0);
		    
		    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		    String fromDateStr = dmFilter.getFromDate() != null ? 
		                         dateFormat.format(dmFilter.getFromDate()) : "N/A";
		    
		    dateCell.setCellValue("Date: " + fromDateStr );
		    dateCell.setCellStyle(dateStyle);
		    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 10));
		    
		  
		  Row headerRow = sheet.createRow(2);
		  
		  String[] headers = {
			        "S.NO","SS NAME", "FEEDER NAME", "DT NAME", "MAINTAIN. DATE", "FROM",
			        "TO", "TYPE", "REASON", "ENTERED THROUGH", "CIRCLE AND SECTION COVERED"
			    };
         
         
         for (int i = 0; i < headers.length; i++) {
             Cell cell = headerRow.createCell(i);
             cell.setCellValue(headers[i]);
             cell.setCellStyle(headerStyle);
         }
         
         int rowNum = 3;
         int serialNo = 1;
         for (OutagesReportValueBean complaint : reportData) {
             Row row = sheet.createRow(rowNum++);
             int col = 0;
             row.createCell(col++).setCellValue(serialNo++); 
             row.createCell(col++).setCellValue(complaint.getSsName());
             row.createCell(col++).setCellValue(complaint.getFeederName());
             row.createCell(col++).setCellValue(complaint.getDtName());
             row.createCell(col++).setCellValue(complaint.getMaintainDate());
             row.createCell(col++).setCellValue(complaint.getFromTime());
             row.createCell(col++).setCellValue(complaint.getToTime());
             row.createCell(col++).setCellValue(complaint.getOutageType());
             row.createCell(col++).setCellValue(complaint.getReason());
             row.createCell(col++).setCellValue(complaint.getEnteredThrough());
             row.createCell(col++).setCellValue(complaint.getCircleAndSection());

             
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
		                .name("Outages_Entered_Report.xls")
		                .contentType("application/vnd.ms-excel")
		                .stream(() -> inputStream)
		                .build();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
	  }
	 
	//SECTION REPORT TO EXCEL DOWNLOAD
	 public void exportSectionsToExcel(List<TmpCtSecBean> reports) throws IOException {
		    HSSFWorkbook workbook = new HSSFWorkbook();
		    Sheet sheet = workbook.createSheet("CategoryWise_Section_Abstract_As_On_Date_And_Device");

		    sheet.setColumnWidth(0, 2000); //SECTION
	        sheet.setColumnWidth(1, 4000); // DIVISION

		    for (int i = 2; i <= 31; i++) {  
		        sheet.setColumnWidth(i, 3000);
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
		    headingCell.setCellValue("CATEGORY WISE CIRCLE ABSTRACT REPORT");
		    headingCell.setCellStyle(headingStyle);
		    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 31)); 
		    
		    Row dateRow = sheet.createRow(1);
		    Cell dateCell = dateRow.createCell(0);
		    
		    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		    String fromDateStr = dmFilter.getFromDate() != null ? 
		                         dateFormat.format(dmFilter.getFromDate()) : "N/A";
		    String toDateStr = dmFilter.getToDate() != null ? 
		                       dateFormat.format(dmFilter.getToDate()) : "N/A";
		    
		    String device = dmFilter.getDevice();

		    Map<String, String> deviceMap = new HashMap<>();
		    deviceMap.put("L", "ALL");
		    deviceMap.put("P", "MOBILE");
		    deviceMap.put("W", "WEB");
		    deviceMap.put("S", "SM");
		    deviceMap.put("A", "FOC");
		    deviceMap.put("M", "MINNAGAM");
		    deviceMap.put("G", "MM");
		    
		    device = deviceMap.getOrDefault(device, "N/A");

		    dateCell.setCellValue("From Date: " + fromDateStr + "  To Date: " + toDateStr +"  CIRCLE :"+selectedCircleName +"  Device :"+device);
		    dateCell.setCellStyle(dateStyle);
		    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 31));

		    Row headerRow1 = sheet.createRow(2);
		    Row headerRow2 = sheet.createRow(3);

		    String[] mainHeaders = {"SECTION", "DIVISION", "Billing Related", "Meter Related", "Power Failure", "Voltage Fluctuation", 
		                            "Fire", "Dangerous Pole", "Theft", "Conductor Snapping", "Others", "TOTAL"};

		    String[] subHeaders = {"Revd.", "Comp.", "Pend."};
		    
		    int colIndex = 0;
		    for (String mainHeader : mainHeaders) {
		        Cell cell = headerRow1.createCell(colIndex);
		        cell.setCellValue(mainHeader);
		        cell.setCellStyle(headerStyle);
		        
		        if (!mainHeader.equals("SECTION") && !mainHeader.equals("DIVISION")) {
		            sheet.addMergedRegion(new CellRangeAddress(2, 2, colIndex, colIndex + 2));
		            for (String subHeader : subHeaders) {
		                Cell subCell = headerRow2.createCell(colIndex);
		                subCell.setCellValue(subHeader);
		                subCell.setCellStyle(headerStyle);
		                colIndex++;
		            }
		        } else {
		            colIndex++;
		        }
		    }

		    int rowNum = 4;
		    BigDecimal[] totalSums = new BigDecimal[30];  
		    Arrays.fill(totalSums, BigDecimal.ZERO);

		    for (TmpCtSecBean report : reports) {
		        Row row = sheet.createRow(rowNum++);
		        row.createCell(0).setCellValue(report.getSecName());
		        row.createCell(1).setCellValue(report.getDivisionName());

		        BigDecimal rowTotSum = BigDecimal.ZERO;
		        BigDecimal rowCompSum = BigDecimal.ZERO;
		        BigDecimal rowPendSum = BigDecimal.ZERO;

		        int dataIndex = 2; 

		        BigDecimal[] values = {
		            report.getBlTot(), report.getBlCom(), report.getBlPen(),
		            report.getMeTot(), report.getMeCom(), report.getMePen(),
		            report.getPfTot(), report.getPfCom(), report.getPfPen(),
		            report.getVfTot(), report.getVfCom(), report.getVfPen(),
		            report.getFiTot(), report.getFiCom(), report.getFiPen(),
		            report.getThTot(), report.getThCom(), report.getThPen(),
		            report.getTeTot(), report.getTeCom(), report.getTePen(),
		            report.getCsTot(), report.getCsCom(), report.getCsPen(),
		            report.getOtTot(), report.getOtCom(), report.getOtPen()
		        };

		        for (int i = 0; i < values.length; i += 3) {
		            setCellValue(row, dataIndex++, values[i]);
		            setCellValue(row, dataIndex++, values[i + 1]);
		            setCellValue(row, dataIndex++, values[i + 2]);

		            rowTotSum = rowTotSum.add(values[i] != null ? values[i] : BigDecimal.ZERO);
		            rowCompSum = rowCompSum.add(values[i + 1] != null ? values[i + 1] : BigDecimal.ZERO);
		            rowPendSum = rowPendSum.add(values[i + 2] != null ? values[i + 2] : BigDecimal.ZERO);
		        }

		        setCellValue(row, dataIndex++, rowTotSum);
		        setCellValue(row, dataIndex++, rowCompSum);
		        setCellValue(row, dataIndex++, rowPendSum);

		        for (int i = 0; i < values.length; i++) {
		            totalSums[i] = totalSums[i].add(values[i] != null ? values[i] : BigDecimal.ZERO);
		        }
		        totalSums[values.length] = totalSums[values.length].add(rowTotSum);
		        totalSums[values.length + 1] = totalSums[values.length + 1].add(rowCompSum);
		        totalSums[values.length + 2] = totalSums[values.length + 2].add(rowPendSum);
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
		    totalRow.createCell(0).setCellValue("TOTAL");
		    totalRow.createCell(1).setCellValue("");
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
		        excelFile = DefaultStreamedContent.builder()
		                .name("CategoryWise_Section_Abstract_As_On_Date_And_Device.xls")
		                .contentType("application/vnd.ms-excel")
		                .stream(() -> inputStream)
		                .build();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}
	 
	 private void setCellValue(Row row, int columnIndex, Number value) {
	        if (value != null) {
	            if (value instanceof BigDecimal) {
	                row.createCell(columnIndex).setCellValue(value.doubleValue());
	            } else if (value instanceof Integer || value instanceof Long || value instanceof Double || value instanceof Float) {
	                row.createCell(columnIndex).setCellValue(value.doubleValue());
	            } else {
	                row.createCell(columnIndex).setCellValue(value.toString()); 
	            }
	        } else {
	            row.createCell(columnIndex).setCellValue(""); 
	        }
	    }
	 
	     //CIRCLE REPORT TO PDF DOWNLOAD
		 public void exportToPdf(List<OutagesReportValueBean> reportData) throws IOException {

			    final int COLUMN_COUNT = 11;
			    final float SNO_COLUMN_WIDTH = 3f;
			    final float OTHER_COLUMN_WIDTH = 15f;
			    final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
			    final Font TOTAL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
			    final Font DATA_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
			    final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
    

			    
			    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			    String fromDate = sdf.format(dmFilter.getFromDate());
			    

			    Document document = new Document(PageSize.A3.rotate());
			    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			    
			    try {
			        PdfWriter.getInstance(document, outputStream);
			        document.open();
			        
			        PdfPTable table = new PdfPTable(COLUMN_COUNT);
			        table.setWidthPercentage(100);
			        table.setSpacingBefore(10f);
			        
			        Paragraph title = new Paragraph("OUTAGES ENTERED REPORT", TITLE_FONT);
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
			        
			        String[] headers = {"S.NO","SS NAME","FEEDER NAME","DT NAME","MAINTAIN.DATE","FROM","TO","TYPE","REASON","ENTERED THROUGH","CIRCLE AND SECTION COVERED"};
			        for(String header :headers) {
			        	PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
			            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
			            table.addCell(cell);
			        }
			        int sno = 1;
			        
			        for (OutagesReportValueBean report : reportData) {
			        	table.addCell(createDataCell(String.valueOf(sno++), DATA_FONT, Element.ALIGN_CENTER));
			            
			            // SS NAME
			            table.addCell(createDataCell(report.getSsName(), DATA_FONT, Element.ALIGN_LEFT));
			            
			            // FEEDER NAME
			            table.addCell(createDataCell(report.getFeederName(), DATA_FONT, Element.ALIGN_LEFT));
			            
			            // DT NAME
			            table.addCell(createDataCell(report.getDtName(), DATA_FONT, Element.ALIGN_LEFT));
			            
			            // MAINTAIN.DATE
			            table.addCell(createDataCell(report.getMaintainDate(), DATA_FONT, Element.ALIGN_CENTER));
			            
			            // FROM
			            table.addCell(createDataCell(report.getFromTime(), DATA_FONT, Element.ALIGN_CENTER));
			            
			            // TO
			            table.addCell(createDataCell(report.getToTime(), DATA_FONT, Element.ALIGN_CENTER));
			            
			            // TYPE
			            table.addCell(createDataCell(report.getOutageType(), DATA_FONT, Element.ALIGN_LEFT));
			            
			            // REASON
			            table.addCell(createDataCell(report.getReason(), DATA_FONT, Element.ALIGN_LEFT));
			            
			            // ENTERED THROUGH
			            table.addCell(createDataCell(report.getEnteredThrough(), DATA_FONT, Element.ALIGN_LEFT));
			            
			            // CIRCLE AND SECTION COVERED
			            table.addCell(createDataCell(report.getCircleAndSection(), DATA_FONT, Element.ALIGN_LEFT));
			        }
			        
			        document.add(table);
			        document.close();
			        
			        pdfFile = DefaultStreamedContent.builder()
			            .contentType("application/pdf")
			            .name("Outages_Report.pdf")
			            .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
			            .build();

			    
			    
			    }catch(Exception e) {
			    	e.printStackTrace();
			    }
		  }
		 
		 //SECTION REPORT TO PDF DOWNLOAD
		 public void exportSectionsToPdf(List<TmpCtSecBean> sectionList) throws IOException {
			    final int CATEGORY = 9;
			    final int SUB_COLUMNS = 3;
			    final int COLUMN_COUNT = 2 + (CATEGORY * SUB_COLUMNS) + 3; 
			    
			    final float FIRST_COLUMN_WIDTH = 15f;
			    final float OTHER_COLUMN_WIDTH = 4f;
			    final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
			    final Font TOTAL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
			    final Font DATA_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
			    final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
			    
			    
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
			    String toDate =sdf.format(dmFilter.getToDate());
			    

			    Document document = new Document(PageSize.A2.rotate());
			    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			    
			    try {
			        PdfWriter.getInstance(document, outputStream);
			        document.open();
			        
			        PdfPTable table = new PdfPTable(COLUMN_COUNT);
			        table.setWidthPercentage(100);
			        table.setSpacingBefore(10f);
			        
			        Paragraph title = new Paragraph("CATEGORY AND DEVICE SECTION ABSTRACT REPORT", TITLE_FONT);
			        title.setAlignment(Element.ALIGN_CENTER);
			        document.add(title);
			        
			        Paragraph subTitle = new Paragraph("CIRCLE :"+selectedCircleName+ " | "+"FROM : " + fromDate + " TO " +toDate+" | "+"Device: "+device);
			        subTitle.setAlignment(Element.ALIGN_CENTER);
			        subTitle.setSpacingAfter(10);
			        document.add(subTitle);
			        
			        float[] columnWidths = new float[COLUMN_COUNT];
			        columnWidths[0] = FIRST_COLUMN_WIDTH;
			        columnWidths[1] = FIRST_COLUMN_WIDTH;
			        for (int i = 2; i < COLUMN_COUNT; i++) {
			            columnWidths[i] = OTHER_COLUMN_WIDTH;
			        }
			        table.setWidths(columnWidths);
			        
			        addMergedHeaderCell(table, "SECTION", HEADER_FONT, 1, 2);
			        addMergedHeaderCell(table, "DIVISION", HEADER_FONT, 1, 2);
			        String[] categoryHeaders = {"Billing Releated","Meter Releated","Power Failure","Voltage Fluctuation","Fire","Dangerous Pole","Theft","Conductor Snapping","Others"};
			        for (int category = 0; category < categoryHeaders.length; category++) {
			            addMergedHeaderCell(table, categoryHeaders[category], HEADER_FONT, 3, 1);
			        }
			        addMergedHeaderCell(table, "TOTAL", HEADER_FONT, 3, 1);
			        
			        
			      //SUB HEADER
			        String[] subHeaders = {"Revd.", "Comp.", "Pend."};
			        for (int i = 0; i < CATEGORY + 1; i++) { 
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
			        
			        for (TmpCtSecBean report : sectionList) {
			            table.addCell(createDataCell(report.getSecName(), DATA_FONT, Element.ALIGN_LEFT));
			            table.addCell(createDataCell(report.getDivisionName(), DATA_FONT, Element.ALIGN_LEFT));
			            
			            addCategoryData(table, report.getBlTot(), report.getBlCom(), report.getBlPen(), DATA_FONT);
			            addCategoryData(table, report.getMeTot(), report.getMeCom(), report.getMePen(), DATA_FONT);
			            addCategoryData(table, report.getPfTot(), report.getPfCom(), report.getPfPen(), DATA_FONT);
			            addCategoryData(table, report.getVfTot(), report.getVfCom(), report.getVfPen(), DATA_FONT);
			            
			            addCategoryData(table, report.getFiTot(), report.getFiCom(), report.getFiPen(), DATA_FONT);
			            addCategoryData(table, report.getThTot(), report.getThCom(), report.getThPen(), DATA_FONT);
			            addCategoryData(table, report.getTeTot(), report.getTeCom(), report.getTePen(), DATA_FONT);
			            addCategoryData(table, report.getCsTot(), report.getCsCom(), report.getCsPen(), DATA_FONT);
			            
			            addCategoryData(table, report.getOtTot(), report.getOtCom(), report.getOtPen(), DATA_FONT);
			            

			            BigDecimal total = report.getBlTot().add(report.getMeTot()).add(report.getPfTot()).add(report.getVfTot())
								.add(report.getFiTot()).add(report.getThTot()).add(report.getTeTot()).add(report.getCsTot())
								.add(report.getOtTot());
			            
			            BigDecimal totalCpl = report.getBlCom().add(report.getMeCom()).add(report.getPfCom()).add(report.getVfCom())
								.add(report.getFiCom()).add(report.getThCom()).add(report.getTeCom()).add(report.getCsCom())
								.add(report.getOtCom());
						
			            BigDecimal pend = report.getBlPen().add(report.getMePen()).add(report.getPfPen()).add(report.getVfPen())
								.add(report.getFiPen()).add(report.getThPen()).add(report.getTePen()).add(report.getCsPen())
								.add(report.getOtPen());					 
						 
				            grandTot = grandTot.add(total);
				            grandTotCpl =grandTotCpl.add(totalCpl);
				            grandTotPend = grandTotPend.add(pend);


			            
			            addCategoryData(table,total, totalCpl,pend,TOTAL_FONT);
			        }
			        PdfPCell footerLabel = new PdfPCell(new Phrase("TOTAL", TOTAL_FONT));
			        footerLabel.setBackgroundColor(BaseColor.LIGHT_GRAY);
			        footerLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
			        footerLabel.setColspan(2);
			        table.addCell(footerLabel);

			        addCategoryData(table,
			            sectionList.stream().map(r->r.getBlTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			            sectionList.stream().map(r->r.getBlCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			            sectionList.stream().map(r->r.getBlPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			            TOTAL_FONT);
			        
			        addCategoryData(table,
			        		sectionList.stream().map(r->r.getMeTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getMeCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getMePen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			        addCategoryData(table,
			        		sectionList.stream().map(r->r.getPfTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getPfCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getPfPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			        addCategoryData(table,
			        		sectionList.stream().map(r->r.getVfTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getVfCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getVfPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			        addCategoryData(table,
			        		sectionList.stream().map(r->r.getFiTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getFiCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getFiPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			        addCategoryData(table,
			        		sectionList.stream().map(r->r.getThTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getThCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getThPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			        addCategoryData(table,
			        		sectionList.stream().map(r->r.getTeTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getTeCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getTePen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			        addCategoryData(table,
			        		sectionList.stream().map(r->r.getCsTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getCsCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getCsPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			        addCategoryData(table,
			        		sectionList.stream().map(r->r.getOtTot()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getOtCom()).reduce(BigDecimal.ZERO, BigDecimal::add),
			        		sectionList.stream().map(r->r.getOtPen()).reduce(BigDecimal.ZERO, BigDecimal::add),
			                TOTAL_FONT);
			        
			       
			        
			       
			        addCategoryData(table, grandTot, grandTotCpl, grandTotPend, TOTAL_FONT);

			        document.add(table);
			        document.close();
			        
			        pdfFile = DefaultStreamedContent.builder()
				            .contentType("application/pdf")
				            .name("CategoryWise_Section_Abstract_As_On_Date_And_Device.pdf")
				            .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
				            .build();
			        
			    
			    
			    }catch(Exception e) {
			    	e.printStackTrace();
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

			private void addCategoryData(PdfPTable table, BigDecimal revd, BigDecimal cpl, BigDecimal pend, Font font) {
			    table.addCell(createDataCell(revd.toString(), font, Element.ALIGN_CENTER));
			    table.addCell(createDataCell(cpl.toString(), font, Element.ALIGN_CENTER));
			    table.addCell(createDataCell(pend.toString(), font, Element.ALIGN_CENTER));
			}
	  
	  
	  public void exportComplaintListToExcel(List<ViewComplaintReportValueBean> complaintList) throws IOException {
		  HSSFWorkbook workbook = new HSSFWorkbook();
		  Sheet sheet = workbook.createSheet("CategoryWise_Abstract_As_On_Date_And_Device_ComplaintList");
		  
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
		                .name("CategoryWise_Abstract_As_On_Date_And_Device_Complaints.xls")
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
		        Paragraph title = new Paragraph("CATEGORY DEVICE COMPLAINT LIST", titleFont);
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
		            .name("CategoryWise_Abstract_As_On_Date_And_Device_Complaints.pdf")
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

	public List<TmpCtCirBean> getReports() {
		return reports;
	}

	public void setReports(List<TmpCtCirBean> reports) {
		this.reports = reports;
	}

	public List<TmpCtSecBean> getSectionList() {
		return sectionList;
	}

	public void setSectionList(List<TmpCtSecBean> sectionList) {
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

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public String getRedirectFrom() {
		return redirectFrom;
	}

	public void setRedirectFrom(String redirectFrom) {
		this.redirectFrom = redirectFrom;
	}

	public ViewComplaintReportValueBean getSelectedComplaintId() {
		return selectedComplaintId;
	}

	public void setSelectedComplaintId(ViewComplaintReportValueBean selectedComplaintId) {
		this.selectedComplaintId = selectedComplaintId;
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
