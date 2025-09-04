package tneb.ccms.admin.controller;

import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.io.*;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.type.StandardBasicTypes;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.pdf.PdfStructTreeController.returnType;
import com.jsf2leaf.model.LatLong;
import com.jsf2leaf.model.Layer;
import com.jsf2leaf.model.Map;
import com.jsf2leaf.model.Marker;
import com.jsf2leaf.model.Pulse;

import tneb.ccms.admin.AdminMain;
import tneb.ccms.admin.dao.CallCenterDao;
import tneb.ccms.admin.dao.ComplaintsDao;
import tneb.ccms.admin.dao.GeneralDao;
import tneb.ccms.admin.dao.UserDao;
import tneb.ccms.admin.model.AdminUserBean;
import tneb.ccms.admin.model.CallCenterUserBean;
import tneb.ccms.admin.model.CategoryBean;
import tneb.ccms.admin.model.CircleBean;
import tneb.ccms.admin.model.CompContactMap;
import tneb.ccms.admin.model.ComplaintBean;
import tneb.ccms.admin.model.ComplaintHistoryBean;
import tneb.ccms.admin.model.CustMasterBean;
import tneb.ccms.admin.model.DistrictBean;
import tneb.ccms.admin.model.DivisionBean;
import tneb.ccms.admin.model.FieldWorkerBean;
import tneb.ccms.admin.model.FieldWorkerComplaintBean;
import tneb.ccms.admin.model.LoginParams;
import tneb.ccms.admin.model.PublicUserBean;
import tneb.ccms.admin.model.RegionBean;
import tneb.ccms.admin.model.SchOutageBean;
import tneb.ccms.admin.model.OutagesBean;
import tneb.ccms.admin.model.SectionBean;
import tneb.ccms.admin.model.SubCategoryBean;
import tneb.ccms.admin.model.SubDivisionBean;
import tneb.ccms.admin.model.ViewComplaintBean;
import tneb.ccms.admin.util.CCMSConstants;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.DataModel.ComplaintLog;
import tneb.ccms.admin.util.GeneralUtil;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.util.Network;
import tneb.ccms.admin.util.SMSUtil;
import tneb.ccms.admin.util.SmsClient;
import tneb.ccms.admin.util.ViewLedger;
import tneb.ccms.admin.valuebeans.AdminUserValueBean;
import tneb.ccms.admin.valuebeans.CallCenterUserValueBean;
import tneb.ccms.admin.valuebeans.CategoriesValueBean;
import tneb.ccms.admin.valuebeans.CircleValueBean;
import tneb.ccms.admin.valuebeans.ClosureReasonValueBean;
import tneb.ccms.admin.valuebeans.CompContactMapValueBean;
import tneb.ccms.admin.valuebeans.ComplaintValueBean;
import tneb.ccms.admin.valuebeans.ConsumerServiceValueBean;
import tneb.ccms.admin.valuebeans.DistrictValueBean;
import tneb.ccms.admin.valuebeans.DivisionValueBean;
import tneb.ccms.admin.valuebeans.FieldWorkerValueBean;
import tneb.ccms.admin.valuebeans.OutagesValueBean;
import tneb.ccms.admin.valuebeans.PublicUserValueBean;
import tneb.ccms.admin.valuebeans.SchOutagesValueBean;
import tneb.ccms.admin.valuebeans.SectionValueBean;
import tneb.ccms.admin.valuebeans.SubCategoriesValueBean;
import tneb.ccms.admin.valuebeans.SubDivisionValueBean;
import tneb.ccms.admin.valuebeans.ViewComplaintReportValueBean;
import tneb.ccms.admin.valuebeans.ViewComplaintValueBean;

public class SingleComplaintReport {

	private Logger logger = LoggerFactory.getLogger(SingleComplaintReport.class.getName());
	
	ViewLedger ledger = new ViewLedger();
	
	@ManagedProperty("#{admin}")
	AdminMain admin;

	List<ViewComplaintValueBean> complaintList;
	List<ViewComplaintValueBean> minnagamComplaintList;

	private List<ViewComplaintValueBean> minnagamComplaintListPendingClose;

	public List<ViewComplaintValueBean> getMinnagamComplaintListPendingClose() {
	    return minnagamComplaintListPendingClose;
	}

	public void setMinnagamComplaintListPendingClose(List<ViewComplaintValueBean> minnagamComplaintListPendingClose) {
	    this.minnagamComplaintListPendingClose = minnagamComplaintListPendingClose;
	}


	private Integer complaintCloseId;
	
	
	public Integer getComplaintCloseId() {
		return complaintCloseId;
	}

	public void setComplaintCloseId(Integer complaintCloseId) {
		this.complaintCloseId = complaintCloseId;
	}
	    private UploadedFile file;
	    private UploadedFiles files;
	    
	    public UploadedFile getFile() {
	        return file;
	    }

	    public void setFile(UploadedFile file) {
	        this.file = file;
	    }

	    public UploadedFiles getFiles() {
	        return files;
	    }

	    public void setFiles(UploadedFiles files) {
	        this.files = files;
	    }

//	    public void upload() {
//			System.err.println("FILENAME WELCOME TO ALL:::::::::::::::");
//	        if (file != null) {
//	        	System.err.println("FILENAME:::::::::::::::"+file.getFileName());
//	            FacesMessage message = new FacesMessage("Successful", file.getFileName() + " is uploaded.");
//	            FacesContext.getCurrentInstance().addMessage(null, message);
//	        }
//	        else
//	        {
//	        	System.err.println("FILENAME NULL:::::::::::::::");
//	        }
//	    }

	List<ViewComplaintValueBean> viewComplaintList;
	List<ViewComplaintValueBean> viewConsumerComplaintList;
	
	List<ViewComplaintReportValueBean> viewComplaintReportList;
	
	List<ViewComplaintValueBean> pendingComplaintsList;
	List<ViewComplaintValueBean> pendingAltComplaintsList;
	List<ComplaintValueBean> pendingCustmasterComplaintList;
	
	ViewComplaintValueBean selectedComplaintMobileNumberboth;
	
	
	public ViewComplaintValueBean getSelectedComplaintMobileNumberboth() {
		return selectedComplaintMobileNumberboth;
	}

	public void setSelectedComplaintMobileNumberboth(ViewComplaintValueBean selectedComplaintMobileNumberboth) {
		this.selectedComplaintMobileNumberboth = selectedComplaintMobileNumberboth;
	}


	ComplaintValueBean selectedComplaintId;
	CompContactMapValueBean selectedComplaintMobileNumber;
	List<SchOutagesValueBean> cusCodes;
	List<OutagesValueBean> cusCodess;
	
	private Integer selectedClosureType;

	public Integer getSelectedClosureType() {
	    return selectedClosureType;
	}

	public void setSelectedClosureType(Integer selectedClosureType) {
	    this.selectedClosureType = selectedClosureType;
	}
	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	private String remarks;
	
	private String imageId;
	
    
	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public List<OutagesValueBean> getCusCodess() {
		return cusCodess;
	}

	public void setCusCodess(List<OutagesValueBean> cusCodess) {
		this.cusCodess = cusCodess;
	}
	ConsumerServiceValueBean consumerServiceValueBean = new ConsumerServiceValueBean();
	ViewComplaintValueBean complaint = new ViewComplaintValueBean();
	ViewComplaintReportValueBean complaints ;
	
	
	public ViewComplaintReportValueBean getComplaints() {
		return complaints;
	}

	public void setComplaints(ViewComplaintReportValueBean complaints) {
		this.complaints = complaints;
	}
	LoginParams officer;
	String description;
	String descriptionTransfer;
	String descriptionComplete;
	String closureDescriptionComplete;
	
	int sectionId;
	int regionId;
	DataModel dm = new DataModel();
	DataModel dmFilter = new DataModel();
	DataModel dmCurrentData = new DataModel();
	Map map = new Map();
	int complaintCode;
	int statusId;

	private String complaintNumber;
	private String consumerMobileNumber;
	private String serviceApplicationNo;
	private String adminMobile;
	private List<ClosureReasonValueBean> lstReasons;
	
	public int getSectionId() {
		return sectionId;
	}

	public void setSectionId(int sectionId) {
		this.sectionId = sectionId;
	}

	private Integer selectedReasonId;

	public Integer getSelectedReasonId() {
	    return selectedReasonId;
	}

	public void setSelectedReasonId(Integer selectedReasonId) {
	    this.selectedReasonId = selectedReasonId;
	}

	
	public List<ClosureReasonValueBean> getLstReasons() {
	    return lstReasons;
	}

	public void setLstReasons(List<ClosureReasonValueBean> lstReasons) {
	    this.lstReasons = lstReasons;
	}

	
	public String getAdminMobile() {
	    return adminMobile;
	}

	public void setAdminMobile(String adminMobile) {
	    this.adminMobile = adminMobile;
	}
	private boolean fieldDisabled;
	  private String customerMobile;

	    public String getCustomerMobile() {
	        return customerMobile;
	    }

	    public void setCustomerMobile(String customerMobile) {
	        this.customerMobile = customerMobile;
	    }
	Date date = new Date();
	
	SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy E <HH:mm>");
    String formattedDate = sdf.format(date);

	private String circleName;
	
	    
	public String getCircleName() {
		return circleName;
	}

	public void setCircleName(String circleName) {
		this.circleName = circleName;
	}

	public SingleComplaintReport() {
		super();


	}
	
public void returnToSearchPageclose()
{
	 try {
		FacesContext.getCurrentInstance().getExternalContext().redirect("closeComplaintCircle.xhtml");
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

public void reloadPendingComplaints() throws Exception {
	this.minnagamComplaintListPendingClose=   loadPendingComplaints();
}

	
public List<ViewComplaintValueBean> loadPendingComplaints() {
    List<ViewComplaintValueBean> viewComplaintList = new ArrayList<>();
    List<Integer> callCenterCircleId = new ArrayList<>();

    try {
        HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext()
                .getSession(false);
        AdminUserValueBean adminUserValueBean = (AdminUserValueBean) httpsession.getAttribute("sessionAdminValueBean");
        CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession
                .getAttribute("sessionCallCenterUserValueBean");

        SessionFactory factory = HibernateUtil.getSessionFactory();
        Session session = factory.openSession();

        try {
            String sqlQuery = "SELECT " +
                    "COMP.ID AS id, " +
                    "COMP.ALTERNATE_MOBILE_NO AS mobile, " +
                    "(CASE WHEN  COMP.device='MI' then COMP.COMP_MOBILE else COMP.MOBILE end) AS compMobileNumber, " +
                    "COMP.created_on AS createdOnFormatted, " +
                    "COMP.SERVICE_NAME AS serviceName, " +
                    "COMP.description AS description, " +
                    "COMP.service_number AS serviceNumber, " +
                    "COMP.section_name AS sectionName, " +
                    "COMP.updated_on AS updatedOnFormatted, " +
                    "COMP.STATUS_ID AS statusId, " +
                    "COMP.DEVICE AS device " +
                    "FROM VIEW_COMPLAINT COMP WHERE COMP.status_id IN (0,1) ";

            if (adminUserValueBean != null && adminUserValueBean.getRoleId() != 10) {
                int roleId = adminUserValueBean.getRoleId();

                if (roleId >= 1 && roleId <= 5) {
                    sqlQuery += " AND COMP.REGION_ID = :regionId ";
                    if (roleId <= 4) sqlQuery += " AND COMP.CIRCLE_ID = :circleId ";
                    if (roleId <= 3) sqlQuery += " AND COMP.DIVISION_ID = :divisionId ";
                    if (roleId <= 2) sqlQuery += " AND COMP.SUB_DIVISION_ID = :subDivisionId ";
                    if (roleId == 1) sqlQuery += " AND COMP.SECTION_ID = :sectionId ";
                }
            } else if (callCenterValueBean != null && callCenterValueBean.getRoleId() != null) {
                Integer userId = callCenterValueBean.getId();
                int ccRoleId = callCenterValueBean.getRoleId();

                // Fetch associated circle IDs
                try (Session session2 = HibernateUtil.getSessionFactory().openSession()) {
                    session2.beginTransaction();
                    callCenterCircleId = session2.createQuery(
                            "SELECT c.circleBean.id FROM CallCenterMappingBean c WHERE c.callCenterUserBean.id = :userId",
                            Integer.class)
                            .setParameter("userId", userId)
                            .getResultList();
                    session2.getTransaction().commit();
                }

                if (ccRoleId == 1 && !callCenterCircleId.isEmpty()) {
                    sqlQuery += " AND COMP.CIRCLE_ID IN (:callCenterCircleId) ";
                }
            }
            
            sqlQuery += " ORDER BY COMP.ID DESC";

            Query query = session.createSQLQuery(sqlQuery).setMaxResults(1000);

            ((NativeQuery<?>) query).addScalar("id", StandardBasicTypes.INTEGER);
            ((NativeQuery<?>) query).addScalar("mobile", StandardBasicTypes.STRING);
            ((NativeQuery<?>) query).addScalar("compMobileNumber", StandardBasicTypes.STRING);
            ((NativeQuery<?>) query).addScalar("createdOnFormatted", StandardBasicTypes.TIMESTAMP);
            ((NativeQuery<?>) query).addScalar("serviceName", StandardBasicTypes.STRING);
            ((NativeQuery<?>) query).addScalar("description", StandardBasicTypes.STRING);
            ((NativeQuery<?>) query).addScalar("serviceNumber", StandardBasicTypes.STRING);
            ((NativeQuery<?>) query).addScalar("sectionName", StandardBasicTypes.STRING);
            ((NativeQuery<?>) query).addScalar("updatedOnFormatted", StandardBasicTypes.TIMESTAMP);
            ((NativeQuery<?>) query).addScalar("statusId", StandardBasicTypes.INTEGER);
            ((NativeQuery<?>) query).addScalar("device", StandardBasicTypes.STRING);
            
            if (adminUserValueBean != null && adminUserValueBean.getRoleId() != 10) {
                int roleId = adminUserValueBean.getRoleId();
                if (roleId >= 1 && roleId <= 5) {
                    query.setParameter("regionId", adminUserValueBean.getRegionId());
                    if (roleId <= 4) query.setParameter("circleId", adminUserValueBean.getCircleId());
                    if (roleId <= 3) query.setParameter("divisionId", adminUserValueBean.getDivisionId());
                    if (roleId <= 2) query.setParameter("subDivisionId", adminUserValueBean.getSubDivisionId());
                    if (roleId == 1) query.setParameter("sectionId", adminUserValueBean.getSectionId());
                }
            } else if (callCenterValueBean != null && callCenterValueBean.getRoleId() != null) {
                if (callCenterValueBean.getRoleId() == 1 && !callCenterCircleId.isEmpty()) {
                    query.setParameter("callCenterCircleId", callCenterCircleId);
                }
            }

            List<Object[]> resultList = query.getResultList();
            for (Object[] row : resultList) {
                ViewComplaintBean viewComplaintBean = new ViewComplaintBean();
                viewComplaintBean.setId((Integer) row[0]);
                viewComplaintBean.setAlternateMobileNo((String) row[1]);
                viewComplaintBean.setComp_Mobile((String) row[2]);
                viewComplaintBean.setCreatedOn((Timestamp) row[3]);
                viewComplaintBean.setServiceName((String) row[4]);
                viewComplaintBean.setDescription((String) row[5]);
                viewComplaintBean.setServiceNumber((String) row[6]);
                viewComplaintBean.setSectionName((String) row[7]);
                viewComplaintBean.setUpdatedOn((Timestamp) row[8]);
                viewComplaintBean.setStatusId((Integer) row[9]);
                viewComplaintBean.setDevice((String) row[10]);

                ViewComplaintValueBean vcv = ViewComplaintValueBean
                        .convertViewComplaintBeanToViewComplaintValueBeans(viewComplaintBean);
                viewComplaintList.add(vcv);
            }

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw e;
        } finally {
            HibernateUtil.closeSession(factory, session);
        }

    } catch (Exception e) {
        logger.error(ExceptionUtils.getStackTrace(e));
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
    }

    return viewComplaintList;
}

					



	
	
	
	public void getComplaintDetailForAbstractReportClose()
	{
		
		 SessionFactory factory = null;
		    Session session = null;
		    try {

		        factory = HibernateUtil.getSessionFactory();
		        session = factory.openSession();

			String complaintIdParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("complaintId");
	
			System.err.println("THE COMPLAINT IS IS CALLED =============="+complaintIdParam);
			
			String regCusId=null;
 		    String distribCode =null;
 			Session session3 = factory.openSession();
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
	                "(CASE WHEN  a.device='MI' then ccm.CONTACTNO else b.mobile end) AS Contact_Number, "+
	                 "k.name AS Complaint_Type, d.name AS subctyp, " +
	                "a.description AS Complaint_Description, " +
	                "DECODE(a.status_id, 0, 'Pending', 1, 'In Progress', 2, 'Completed') AS Complaint_Status, " +
	                "(CASE WHEN a.status_id=2 then TO_CHAR(a.updated_on, 'DD-MM-YYYY-HH24:MI') else '' end) AS Attended_Date, " +
	        	    "       (CASE WHEN a.status_id = 2 THEN " +
	        	    "           (SELECT ch1.description FROM " +
	        	    "               (SELECT description, complaint_id, status_id " +
	        	    "                FROM COMPLAINT_HISTORY " +
	        	    "                WHERE complaint_id = a.id AND status_id = 2 " +
	        	    "                ORDER BY updated_on DESC) ch1 " +
	        	    "            WHERE ROWNUM = 1) " +
	        	    "        ELSE '' END) AS Attended_Remarks, " +
	                "f.name AS Region, g.name AS Circle, h.name AS Division, i.name AS SubDivision, j.name AS Section, "+
	                "b.FIRST_NAME AS UserName, "+
	                "a.ALTERNATE_MOBILE_NO as RegisteredMobileNumber, "+
	                "to_char(fb.ENTRYDT, 'dd-mm-yyyy-hh24:mi') AS feedBackEntryDate, "+
	                "fb.REMARKS AS feedbackRemarks, "+
	                "fb.RATING AS feedbackRating, "+
	                "dm.DISTRIB_NAME As Distribution, "+
	                "a.IMAGE_1 as image_1, "+
	                "a.IMAGE_2 as image_2, "+
	                "a.IMAGEID as closure_image "+
	                "FROM COMPLAINT a " +
	                "LEFT JOIN PUBLIC_USER b ON a.user_id = b.id " +
	                "LEFT JOIN COMP_CONTACT_MAP ccm on ccm.COMP_ID=a.id "+
	                "LEFT JOIN COMPLAINT_HISTORY c ON a.id = c.complaint_id AND a.status_id = c.status_id " +
	                "LEFT JOIN COMP_FEEDBACK fb ON a.id = fb.COMP_ID "+
	                "LEFT JOIN COMP_TRANSFER ct ON a.id = ct.COMP_ID "+
	                "LEFT JOIN COMP_QC_DETAILS qc ON a.id = qc.COMP_ID "+
	                "LEFT JOIN SUB_CATEGORY d ON a.sub_category_id = d.id " +
	                "JOIN CATEGORY k ON a.complaint_type = k.code " +
	                "JOIN REGION f ON a.region_id = f.id " +
	                "JOIN CIRCLE g ON a.circle_id = g.id " +
	                "JOIN DIVISION h ON a.division_id = h.id " +
	                "JOIN SUB_DIVISION i ON a.sub_division_id = i.id " +
	                "JOIN SECTION j ON a.section_id = j.id " +
	               // "LEFT JOIN CUSTMASTER ct ON SUBSTR(ct.REGCUSID, 2) = a.SERVICE_NUMBER " +
	                "LEFT JOIN DISTRIB_MASTER dm ON dm.reg_no = f.ID AND dm.SCODE = j.code AND dm.DISTRIB_CODE = :distribCode "+
	                "WHERE a.id = :complaintIdParam " ;
			
			Query query = session.createNativeQuery(hql);
			query.setParameter("complaintIdParam", complaintIdParam);
			query.setParameter("distribCode", distribCode);
			
			List<Object[]> results = query.getResultList();
	 System.err.println("RESULTS:"+results);
	 
			 this.complaints = new ViewComplaintReportValueBean();
			
			
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
	        	dto.setFeedbackRating(row[22]!=null ? ((BigDecimal)row[22]).intValue():null);
	        	dto.setDistribution((String) row[23]);
	        	dto.setImage1((String) row[24]);
	        	dto.setImage2((String) row[25]);
	        	dto.setClosureImage((String) row[26]);
	        	this.complaints=dto;
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
	        this.complaints.setTransferDetails(transfers);
	        
			String resendHql = "SELECT to_char(RESEND_ON, 'dd-mm-yyyy-hh24:mi'), ENTRYUSER, REMARKS " +
	                   "FROM COMP_RESEND WHERE COMP_ID = :complaintId " +
	                   "ORDER BY RESEND_ON";

	Query resendQuery = session.createNativeQuery(resendHql); 
	resendQuery.setParameter("complaintId", complaintIdParam);

	List<Object[]> resendResults = resendQuery.getResultList(); 

	List<ViewComplaintReportValueBean.ResendDetail> resendResultList = new ArrayList<>();

	for (Object[] resend : resendResults) {
	    ViewComplaintReportValueBean.ResendDetail td = new ViewComplaintReportValueBean.ResendDetail();
	    td.setResendDate((String) resend[0]);
	    td.setResendedBy((String) resend[1]);
	    td.setResendRemarks((String) resend[2]);
	    resendResultList.add(td);
	}

	this.complaints.setResendDetails(resendResultList);
	        
	        String qcHql = "SELECT to_char(QC_ON, 'dd-mm-yyyy-hh24:mi'), QC_STATUS, REMARKS " +
	                "FROM COMP_QC_DETAILS WHERE COMP_ID = :complaintId " +
	                "ORDER BY QC_ON";
			  Query qcQuery = session.createNativeQuery(qcHql);
			  qcQuery.setParameter("complaintId", complaintIdParam);
			  List<Object[]> qcResults = qcQuery.getResultList();
			  System.err.println("qcResults:::::::::"+qcResults);
			  List<ViewComplaintReportValueBean.QualityCheckDetail> qcs = new ArrayList<>();
			  for (Object[] qc : qcResults) {
			      ViewComplaintReportValueBean.QualityCheckDetail qcd = new ViewComplaintReportValueBean.QualityCheckDetail();
			      qcd.setQcDate((String) qc[0]);
			      qcd.setQcStatus((String) qc[1]);
			      qcd.setQcRemarks((String) qc[2]);
			      qcs.add(qcd);
			  }
			  this.complaints.setQualityCheckDetails(qcs);
			  
			 // FacesContext.getCurrentInstance().getExternalContext().redirect("categoryWiseComplaintDetail.xhtml");
			  //FacesContext.getCurrentInstance().getExternalContext().getFlash().put("complaints", complaints);
			  FacesContext.getCurrentInstance().getExternalContext().redirect("getComplaintDetailForAbstractReportClose.xhtml");
			  selectedClosureType = null;
			    selectedReasonId = null;
			    remarks = null;
	        fetchComplaintsDetailspending();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void fetchComplaintsDetailspending()
	{
		String complaintIdParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("complaintId");
		ComplaintBean complaintBean = null;
		SessionFactory factory = null;
		Session session = null;
		Transaction transaction = null;
		try {
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			 Integer complaintId = (int) Long.parseLong(complaintIdParam); 
		      this.selectedComplaintId = new ComplaintValueBean();
		        
		      CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<ComplaintBean> criteriaQuery = criteriaBuilder.createQuery(ComplaintBean.class);
				Root<ComplaintBean> root = criteriaQuery.from(ComplaintBean.class);
				
				criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), complaintId));

				Query<ComplaintBean> query = session.createQuery(criteriaQuery).setMaxResults(1);
				complaintBean = query.getSingleResult();
				Integer code = complaintBean.getComplaintCode();
				
		        System.err.println("COMPLAINT ID::::::"+complaintId);
				ComplaintsDao dao = new ComplaintsDao();
			setLstReasons(dao.getReasons(code));
			 selectedComplaintId.setId(complaintId);
			
		}
	   catch (Exception e) {
		logger.error(ExceptionUtils.getStackTrace(e));
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
	}
}
	
	
	
public void circleCloseComplaints() {
	SessionFactory factory = null;
	Session session = null;
	Transaction transaction = null;
	ComplaintBean complaintBean = null;

	HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
	AdminUserValueBean adminUserValueBean = (AdminUserValueBean) httpsession.getAttribute("sessionAdminValueBean");
//	CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession
//			.getAttribute("sessionCallCenterUserValueBean");

	try {

		System.err.println("THE COMPLAINT IS IS CALLED CIRCLE==============" + complaintCloseId);

		factory = HibernateUtil.getSessionFactory();
		session = factory.openSession();

		Timestamp updatedOn = new Timestamp(new Date().getTime());
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<ComplaintBean> criteriaQuery = criteriaBuilder.createQuery(ComplaintBean.class);
		Root<ComplaintBean> root = criteriaQuery.from(ComplaintBean.class);

		criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), complaintCloseId));

		Query<ComplaintBean> query = session.createQuery(criteriaQuery).setMaxResults(1);

		try {
			complaintBean = query.getSingleResult();
		} catch (NoResultException nre) {
			System.out.println("No Complaint Found for the given complaint id.");
			logger.info("No Complaint Found for the given complaint id.");
		}

		AdminUserBean adminUserBean = new AdminUserBean();
		ComplaintHistoryBean complaintHistoryBean = new ComplaintHistoryBean();
		complaintHistoryBean.setComplaintBean(complaintBean);
		if (complaintBean != null) {
			if (selectedReasonId == null || Integer.valueOf(1).equals(selectedClosureType)) // Interim Closure’
			{
				System.err.println("No reason selected");

				complaintBean.setStatusId(CCMSConstants.IN_PROGRESS);
				complaintHistoryBean.setStatusId(CCMSConstants.IN_PROGRESS);

			} // END Interim Closure

			else // Final Closure
			{

				ClosureReasonValueBean selectedReason = null;
				for (ClosureReasonValueBean reason : lstReasons) {
					if (reason.getId().equals(selectedReasonId)) {
						selectedReason = reason;
						break;
					}
				}

				if (selectedReason != null) {
					System.out.println("Selected reason: " + selectedReason.getClosureReason());
				
				} else {
					System.out.println("Selected reason not found");
				}

				complaintBean.setStatusId(CCMSConstants.COMPLETED);
				complaintHistoryBean.setStatusId(CCMSConstants.COMPLETED);
				complaintHistoryBean.setReason(selectedReason.getClosureReason());

				if (file != null) {
					System.err.println("FILE NAME::::::::::::" + file.getFileName());
					try {

						LocalDate today = LocalDate.now();
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy");
						String formattedDate = today.format(formatter); // e.g., 04-07-25

						String originalFileName = file.getFileName(); // e.g., abc.jpg
						String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".")); // .jpg
						String baseFileName = "C_" + complaintCloseId + "_" + formattedDate;
						String newFileName = baseFileName + fileExtension;

						System.err.println("Generated filename: " + newFileName);

						String[] parts = formattedDate.split("-");
						if (parts.length == 3) {
							String month = parts[1];
							String year = "20" + parts[2];

							String uploadDirPath = "/opt/apache-tomcat-9.0.37/webapps/ccms_images/" + year + "/" + month
									+ "/";
							// String uploadDirPath = "C:\\Users\\Dell\\Desktop\\ccms_images\\" + year + "/"
							// + month + "/";
							Path uploadDir = Paths.get(uploadDirPath);

							if (!Files.exists(uploadDir)) {
								System.err.println("Upload folder does not exist: " + uploadDir);
								return;
							}

							Path destination = uploadDir.resolve(newFileName);
							int count = 1;
							while (Files.exists(destination)) {
								newFileName = baseFileName + "_" + count + fileExtension;
								destination = uploadDir.resolve(newFileName);
								count++;
							}

							System.err.println("Uploading to: " + destination);

							try (InputStream input = file.getInputStream()) {
								Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
								System.out.println("File saved as: " + newFileName);
							}
						}

						complaintBean.setImageId(newFileName);

					} catch (Exception e) {

						e.printStackTrace();
					}
				}

			} // END Final Closure

			// adminUserBean.setId(officer.userId);

			complaintBean.setUpdatedOn(updatedOn);
			
//			complaintBean.setCompClUser(officer.getUserName());
//			complaintHistoryBean.setCompClUser(officer.getUserName());
			

				complaintBean.setCompClUser(adminUserValueBean.getUserName());

				complaintHistoryBean.setCompClUser(adminUserValueBean.getUserName());
			


			complaintHistoryBean.setDescription(remarks);
			complaintHistoryBean.setSectionBean(complaintBean.getSectionBean());
			complaintHistoryBean.setRegionBean(complaintBean.getRegionBean());
			complaintHistoryBean.setCircleBean(complaintBean.getCircleBean());
			complaintHistoryBean.setDivisionBean(complaintBean.getDivisionBean());
			complaintHistoryBean.setSubDivisionBean(complaintBean.getSubDivisionBean());
			complaintHistoryBean.setAdminUserBean(null);
			complaintBean.addToHistory(complaintHistoryBean); 

			session.beginTransaction();
			session.saveOrUpdate(complaintBean); 
			session.getTransaction().commit();
			session.refresh(complaintBean);

			String contactNo;
			if (complaintBean.getDevice().equals("MI")) {
				System.err.println("MINAGAM::::::::::");
				CriteriaBuilder criteriaBuilders = session.getCriteriaBuilder();
				CriteriaQuery<CompContactMap> criteriaQuerys = criteriaBuilders.createQuery(CompContactMap.class);
				Root<CompContactMap> roots = criteriaQuerys.from(CompContactMap.class);

				criteriaQuerys.select(roots)
						.where(criteriaBuilders.equal(roots.get("complaint"), complaintBean.getId()));

				Query<CompContactMap> querys = session.createQuery(criteriaQuerys);
				CompContactMap comContactMap = querys.uniqueResult();

				if (comContactMap != null) {
					contactNo = comContactMap.getContactNo(); // ✅ Assuming this method exists
					System.err.println("Contact No: " + contactNo);
					
	                if(Integer.valueOf(2).equals(selectedClosureType)) {
	                	
	                	
	                	if(complaintBean.getAlternateMobileNo()!=null) {
			                FeedbackCaller.sendFeedback(complaintBean.getAlternateMobileNo(), complaintBean.getId()); 
			                
			                String message = "CmplNo:" + complaintBean.getId() + ":Registered by you is Attended and Closed "
									+ GeneralUtil.formatDateWithTime(new Date()) + " Save Electricity - TANGEDCO";
						
							String smsId = SMSUtil.sendSMS(null,complaintBean.getAlternateMobileNo(), message);
							SmsClient.sendSms(smsId);
	                	}else {
			                FeedbackCaller.sendFeedback(contactNo, complaintBean.getId()); 
			                
			                String message = "CmplNo:" + complaintBean.getId() + ":Registered by you is Attended and Closed "
									+ GeneralUtil.formatDateWithTime(new Date()) + " Save Electricity - TANGEDCO";
						
							String smsId = SMSUtil.sendSMS(null,contactNo, message);
							SmsClient.sendSms(smsId);
	                	}
		                
	                }

				}

			} else {
				
				if (complaintBean.getPublicUserBean() != null && complaintBean.getPublicUserBean().getMobile() != null) {
					contactNo = complaintBean.getPublicUserBean().getMobile();
					
					 if(Integer.valueOf(2).equals(selectedClosureType)) {
						 
						 if(complaintBean.getAlternateMobileNo()!=null) {
							 FeedbackCaller.sendFeedback(complaintBean.getAlternateMobileNo(), complaintBean.getId());
								
								String message = "CmplNo:" + complaintBean.getId() + ":Registered by you is Attended and Closed "
										+ GeneralUtil.formatDateWithTime(new Date()) + " Save Electricity - TANGEDCO";
			
								String smsId = SMSUtil.sendSMS(null, complaintBean.getAlternateMobileNo(), message);
								SmsClient.sendSms(smsId);
						 }else {
							 FeedbackCaller.sendFeedback(contactNo, complaintBean.getId());
								
								String message = "CmplNo:" + complaintBean.getId() + ":Registered by you is Attended and Closed "
										+ GeneralUtil.formatDateWithTime(new Date()) + " Save Electricity - TANGEDCO";
			
								String smsId = SMSUtil.sendSMS(null,contactNo, message);
								SmsClient.sendSms(smsId);
						 }
						
					 }
				}
				System.out.println("THE CALL INITIATED FROM PUBLIC USER");
				


			}

			if (Integer.valueOf(1).equals(selectedClosureType))// message for Interim Closure’
			{

				PrimeFaces.current().executeScript(
						"PF('messageDialogalreadyexists').show(); setTimeout(function() { PF('messageDialog').hide(); }, 7000);");

				// FacesContext.getCurrentInstance().getExternalContext().redirect("closeComplaintCircle.xhtml");


					PrimeFaces.current()
							.executeScript("setTimeout(function(){ window.location.href = '"
									+ FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
									+ "/faces/admin/closeComplaintCircle.xhtml'; }, 5000);");
				

			} else // message Final Closure
			{

				PrimeFaces.current().executeScript(
						// Step 1: Show message dialog immediately
						"PF('messageDialogalreadyexists1').show();" +

						// Step 2: After 5 seconds, hide message dialog and show confirm dialog
								"setTimeout(function() {" + "PF('messageDialogalreadyexists1').hide();"
								+ "PF('confirmDlgWidget1').show();" + "window.currentComplaintId = " + complaintCloseId
								+ ";" + "}, 5000);");

	
					PrimeFaces.current()
							.executeScript("setTimeout(function(){ window.location.href = '"
									+ FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
									+ "/faces/admin/closeComplaintCircle.xhtml'; }, 5000);");
				

			}

		}

	} catch (Exception e) {
		if (transaction != null) {
			transaction.rollback();
		}
		logger.error(ExceptionUtils.getStackTrace(e));
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
	} finally {
		HibernateUtil.closeSession(factory, session);
		PrimeFaces.current().executeScript("resetClosureForm();");
	}
}


public void circleCloseComplaintsCallCenterUser() {
	SessionFactory factory = null;
	Session session = null;
	Transaction transaction = null;
	ComplaintBean complaintBean = null;

	HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
	CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession
			.getAttribute("sessionCallCenterUserValueBean");

	try {

		System.err.println("THE COMPLAINT IS IS CALLED CIRCLE==============" + complaintCloseId);

		factory = HibernateUtil.getSessionFactory();
		session = factory.openSession();

		Timestamp updatedOn = new Timestamp(new Date().getTime());
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<ComplaintBean> criteriaQuery = criteriaBuilder.createQuery(ComplaintBean.class);
		Root<ComplaintBean> root = criteriaQuery.from(ComplaintBean.class);

		criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), complaintCloseId));

		Query<ComplaintBean> query = session.createQuery(criteriaQuery).setMaxResults(1);

		try {
			complaintBean = query.getSingleResult();
		} catch (NoResultException nre) {
			System.out.println("No Complaint Found for the given complaint id.");
			logger.info("No Complaint Found for the given complaint id.");
		}

		AdminUserBean adminUserBean = new AdminUserBean();
		ComplaintHistoryBean complaintHistoryBean = new ComplaintHistoryBean();
		complaintHistoryBean.setComplaintBean(complaintBean);
		if (complaintBean != null) {
			if (selectedReasonId == null || Integer.valueOf(1).equals(selectedClosureType)) // Interim Closure’
			{
				System.err.println("No reason selected");

				complaintBean.setStatusId(CCMSConstants.IN_PROGRESS);
				complaintHistoryBean.setStatusId(CCMSConstants.IN_PROGRESS);

			} // END Interim Closure

			else // Final Closure
			{

				ClosureReasonValueBean selectedReason = null;
				for (ClosureReasonValueBean reason : lstReasons) {
					if (reason.getId().equals(selectedReasonId)) {
						selectedReason = reason;
						break;
					}
				}

				if (selectedReason != null) {
					System.out.println("Selected reason: " + selectedReason.getClosureReason());
				
				} else {
					System.out.println("Selected reason not found");
				}

				complaintBean.setStatusId(CCMSConstants.COMPLETED);
				complaintHistoryBean.setStatusId(CCMSConstants.COMPLETED);
				complaintHistoryBean.setReason(selectedReason.getClosureReason());

				if (file != null) {
					System.err.println("FILE NAME::::::::::::" + file.getFileName());
					try {

						LocalDate today = LocalDate.now();
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy");
						String formattedDate = today.format(formatter); // e.g., 04-07-25

						String originalFileName = file.getFileName(); // e.g., abc.jpg
						String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".")); // .jpg
						String baseFileName = "C_" + complaintCloseId + "_" + formattedDate;
						String newFileName = baseFileName + fileExtension;

						System.err.println("Generated filename: " + newFileName);

						String[] parts = formattedDate.split("-");
						if (parts.length == 3) {
							String month = parts[1];
							String year = "20" + parts[2];

							String uploadDirPath = "/opt/apache-tomcat-9.0.37/webapps/ccms_images/" + year + "/" + month
									+ "/";
							// String uploadDirPath = "C:\\Users\\Dell\\Desktop\\ccms_images\\" + year + "/"
							// + month + "/";
							Path uploadDir = Paths.get(uploadDirPath);

							if (!Files.exists(uploadDir)) {
								System.err.println("Upload folder does not exist: " + uploadDir);
								return;
							}

							Path destination = uploadDir.resolve(newFileName);
							int count = 1;
							while (Files.exists(destination)) {
								newFileName = baseFileName + "_" + count + fileExtension;
								destination = uploadDir.resolve(newFileName);
								count++;
							}

							System.err.println("Uploading to: " + destination);

							try (InputStream input = file.getInputStream()) {
								Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
								System.out.println("File saved as: " + newFileName);
							}
						}

						complaintBean.setImageId(newFileName);

					} catch (Exception e) {

						e.printStackTrace();
					}
				}

			} // END Final Closure

			// adminUserBean.setId(officer.userId);

			complaintBean.setUpdatedOn(updatedOn);
			
//			complaintBean.setCompClUser(officer.getUserName());
//			complaintHistoryBean.setCompClUser(officer.getUserName());
			
			
				complaintBean.setCompClUser(callCenterValueBean.getUserName());

				complaintHistoryBean.setCompClUser(callCenterValueBean.getUserName());
			


			complaintHistoryBean.setDescription(remarks);
			complaintHistoryBean.setSectionBean(complaintBean.getSectionBean());
			complaintHistoryBean.setRegionBean(complaintBean.getRegionBean());
			complaintHistoryBean.setCircleBean(complaintBean.getCircleBean());
			complaintHistoryBean.setDivisionBean(complaintBean.getDivisionBean());
			complaintHistoryBean.setSubDivisionBean(complaintBean.getSubDivisionBean());
			complaintHistoryBean.setAdminUserBean(null);
			complaintBean.addToHistory(complaintHistoryBean); 

			session.beginTransaction();
			session.saveOrUpdate(complaintBean); 
			session.getTransaction().commit();
			session.refresh(complaintBean);

			String contactNo;
			if (complaintBean.getDevice().equals("MI")) {
				System.err.println("MINAGAM::::::::::");
				CriteriaBuilder criteriaBuilders = session.getCriteriaBuilder();
				CriteriaQuery<CompContactMap> criteriaQuerys = criteriaBuilders.createQuery(CompContactMap.class);
				Root<CompContactMap> roots = criteriaQuerys.from(CompContactMap.class);

				criteriaQuerys.select(roots)
						.where(criteriaBuilders.equal(roots.get("complaint"), complaintBean.getId()));

				Query<CompContactMap> querys = session.createQuery(criteriaQuerys);
				CompContactMap comContactMap = querys.uniqueResult();

				if (comContactMap != null) {
					contactNo = comContactMap.getContactNo(); // ✅ Assuming this method exists
					System.err.println("Contact No: " + contactNo);
					
	                if(Integer.valueOf(2).equals(selectedClosureType)) {
	                	
	                	
	                	if(complaintBean.getAlternateMobileNo()!=null) {
			                FeedbackCaller.sendFeedback(complaintBean.getAlternateMobileNo(), complaintBean.getId()); 
			                
			                String message = "CmplNo:" + complaintBean.getId() + ":Registered by you is Attended and Closed "
									+ GeneralUtil.formatDateWithTime(new Date()) + " Save Electricity - TANGEDCO";
						
							String smsId = SMSUtil.sendSMS(null,complaintBean.getAlternateMobileNo(), message);
							SmsClient.sendSms(smsId);
	                	}else {
			                FeedbackCaller.sendFeedback(contactNo, complaintBean.getId()); 
			                
			                String message = "CmplNo:" + complaintBean.getId() + ":Registered by you is Attended and Closed "
									+ GeneralUtil.formatDateWithTime(new Date()) + " Save Electricity - TANGEDCO";
						
							String smsId = SMSUtil.sendSMS(null,contactNo, message);
							SmsClient.sendSms(smsId);
	                	}
		                
	                }

				}

			} else {
				
				if (complaintBean.getPublicUserBean() != null && complaintBean.getPublicUserBean().getMobile() != null) {
					contactNo = complaintBean.getPublicUserBean().getMobile();
					
					 if(Integer.valueOf(2).equals(selectedClosureType)) {
						 
						 if(complaintBean.getAlternateMobileNo()!=null) {
							 FeedbackCaller.sendFeedback(complaintBean.getAlternateMobileNo(), complaintBean.getId());
								
								String message = "CmplNo:" + complaintBean.getId() + ":Registered by you is Attended and Closed "
										+ GeneralUtil.formatDateWithTime(new Date()) + " Save Electricity - TANGEDCO";
			
								String smsId = SMSUtil.sendSMS(null, complaintBean.getAlternateMobileNo(), message);
								SmsClient.sendSms(smsId);
						 }else {
							 FeedbackCaller.sendFeedback(contactNo, complaintBean.getId());
								
								String message = "CmplNo:" + complaintBean.getId() + ":Registered by you is Attended and Closed "
										+ GeneralUtil.formatDateWithTime(new Date()) + " Save Electricity - TANGEDCO";
			
								String smsId = SMSUtil.sendSMS(null,contactNo, message);
								SmsClient.sendSms(smsId);
						 }
						
					 }
				}
				System.out.println("THE CALL INITIATED FROM PUBLIC USER");
				


			}

			if (Integer.valueOf(1).equals(selectedClosureType))// message for Interim Closure’
			{

				PrimeFaces.current().executeScript(
						"PF('messageDialogalreadyexists').show(); setTimeout(function() { PF('messageDialog').hide(); }, 7000);");

				// FacesContext.getCurrentInstance().getExternalContext().redirect("closeComplaintCircle.xhtml");

				
					PrimeFaces.current()
							.executeScript("setTimeout(function(){ window.location.href = '"
									+ FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
									+ "/faces/callcenter/closeComplaintCircle.xhtml'; }, 5000);");
				

			} else // message Final Closure
			{

				PrimeFaces.current().executeScript(
						// Step 1: Show message dialog immediately
						"PF('messageDialogalreadyexists1').show();" +

						// Step 2: After 5 seconds, hide message dialog and show confirm dialog
								"setTimeout(function() {" + "PF('messageDialogalreadyexists1').hide();"
								+ "PF('confirmDlgWidget1').show();" + "window.currentComplaintId = " + complaintCloseId
								+ ";" + "}, 5000);");


				
					PrimeFaces.current()
							.executeScript("setTimeout(function(){ window.location.href = '"
									+ FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
									+ "/faces/callcenter/closeComplaintCircle.xhtml'; }, 5000);");
				

			}

		}

	} catch (Exception e) {
		if (transaction != null) {
			transaction.rollback();
		}
		logger.error(ExceptionUtils.getStackTrace(e));
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
	} finally {
		HibernateUtil.closeSession(factory, session);
		PrimeFaces.current().executeScript("resetClosureForm();");
	}
}



public String getImagePath(String filename) {
    if (filename == null || filename.isEmpty()) {
        return "";
    }

    String domain = "https://ccms.tangedco.org";

    try {
        String secondaryUrl = domain + "/photos/" + filename;
        if (urlExists(secondaryUrl)) {
            System.err.println("FOUND OLD PATH: " + secondaryUrl);
            return secondaryUrl;
        }

        String[] parts = filename.split("_");
        if (parts.length >= 3) {
            String datePartWithExtension = parts[2]; 
            String datePart = datePartWithExtension.split("\\.")[0];
            String[] dateParts = datePart.split("-");
            if (dateParts.length == 3) {
                String month = dateParts[1];
                String yearFull = "20" + dateParts[2];

                String primaryUrl = domain + "/ccms_images/" + yearFull + "/" + month + "/" + filename;
                if (urlExists(primaryUrl)) {
                    System.err.println("FOUND NEW PATH: " + primaryUrl);
                    return primaryUrl;
                }
            }
        }

        return filename;

    } catch (Exception e) {
        System.err.println("Error parsing image path for: " + filename + " | " + e.getMessage());
    }

    return filename;
}

private static boolean urlExists(String urlString) {
    try {
        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection con = (HttpURLConnection) new URL(urlString).openConnection();
        con.setRequestMethod("HEAD");
        return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
    } catch (Exception e) {
        return false;
    }
}




	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public ViewLedger getLedger() {
		return ledger;
	}

	public void setLedger(ViewLedger ledger) {
		this.ledger = ledger;
	}

	public AdminMain getAdmin() {
		return admin;
	}

	public void setAdmin(AdminMain admin) {
		this.admin = admin;
	}

	public List<ViewComplaintValueBean> getComplaintList() {
		return complaintList;
	}

	public void setComplaintList(List<ViewComplaintValueBean> complaintList) {
		this.complaintList = complaintList;
	}

	public List<ViewComplaintValueBean> getMinnagamComplaintList() {
		return minnagamComplaintList;
	}

	public void setMinnagamComplaintList(List<ViewComplaintValueBean> minnagamComplaintList) {
		this.minnagamComplaintList = minnagamComplaintList;
	}

	public List<ViewComplaintValueBean> getViewComplaintList() {
		return viewComplaintList;
	}

	public void setViewComplaintList(List<ViewComplaintValueBean> viewComplaintList) {
		this.viewComplaintList = viewComplaintList;
	}

	public List<ViewComplaintValueBean> getViewConsumerComplaintList() {
		return viewConsumerComplaintList;
	}

	public void setViewConsumerComplaintList(List<ViewComplaintValueBean> viewConsumerComplaintList) {
		this.viewConsumerComplaintList = viewConsumerComplaintList;
	}

	public List<ViewComplaintReportValueBean> getViewComplaintReportList() {
		return viewComplaintReportList;
	}

	public void setViewComplaintReportList(List<ViewComplaintReportValueBean> viewComplaintReportList) {
		this.viewComplaintReportList = viewComplaintReportList;
	}

	public List<ViewComplaintValueBean> getPendingComplaintsList() {
		return pendingComplaintsList;
	}

	public void setPendingComplaintsList(List<ViewComplaintValueBean> pendingComplaintsList) {
		this.pendingComplaintsList = pendingComplaintsList;
	}

	public List<ViewComplaintValueBean> getPendingAltComplaintsList() {
		return pendingAltComplaintsList;
	}

	public void setPendingAltComplaintsList(List<ViewComplaintValueBean> pendingAltComplaintsList) {
		this.pendingAltComplaintsList = pendingAltComplaintsList;
	}

	public List<ComplaintValueBean> getPendingCustmasterComplaintList() {
		return pendingCustmasterComplaintList;
	}

	public void setPendingCustmasterComplaintList(List<ComplaintValueBean> pendingCustmasterComplaintList) {
		this.pendingCustmasterComplaintList = pendingCustmasterComplaintList;
	}

	public ComplaintValueBean getSelectedComplaintId() {
		return selectedComplaintId;
	}

	public void setSelectedComplaintId(ComplaintValueBean selectedComplaintId) {
		this.selectedComplaintId = selectedComplaintId;
	}

	public CompContactMapValueBean getSelectedComplaintMobileNumber() {
		return selectedComplaintMobileNumber;
	}

	public void setSelectedComplaintMobileNumber(CompContactMapValueBean selectedComplaintMobileNumber) {
		this.selectedComplaintMobileNumber = selectedComplaintMobileNumber;
	}

	public List<SchOutagesValueBean> getCusCodes() {
		return cusCodes;
	}

	public void setCusCodes(List<SchOutagesValueBean> cusCodes) {
		this.cusCodes = cusCodes;
	}

	public ConsumerServiceValueBean getConsumerServiceValueBean() {
		return consumerServiceValueBean;
	}

	public void setConsumerServiceValueBean(ConsumerServiceValueBean consumerServiceValueBean) {
		this.consumerServiceValueBean = consumerServiceValueBean;
	}

	public ViewComplaintValueBean getComplaint() {
		return complaint;
	}

	public void setComplaint(ViewComplaintValueBean complaint) {
		this.complaint = complaint;
	}

	public LoginParams getOfficer() {
		return officer;
	}

	public void setOfficer(LoginParams officer) {
		this.officer = officer;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescriptionTransfer() {
		return descriptionTransfer;
	}

	public void setDescriptionTransfer(String descriptionTransfer) {
		this.descriptionTransfer = descriptionTransfer;
	}

	public String getDescriptionComplete() {
		return descriptionComplete;
	}

	public void setDescriptionComplete(String descriptionComplete) {
		this.descriptionComplete = descriptionComplete;
	}

	public String getClosureDescriptionComplete() {
		return closureDescriptionComplete;
	}

	public void setClosureDescriptionComplete(String closureDescriptionComplete) {
		this.closureDescriptionComplete = closureDescriptionComplete;
	}

	public int getRegionId() {
		return regionId;
	}

	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}

	public DataModel getDm() {
		return dm;
	}

	public void setDm(DataModel dm) {
		this.dm = dm;
	}

	public DataModel getDmFilter() {
		return dmFilter;
	}

	public void setDmFilter(DataModel dmFilter) {
		this.dmFilter = dmFilter;
	}

	public DataModel getDmCurrentData() {
		return dmCurrentData;
	}

	public void setDmCurrentData(DataModel dmCurrentData) {
		this.dmCurrentData = dmCurrentData;
	}

	public Map getMap() {
		return map;
	}

	public void setMap(Map map) {
		this.map = map;
	}

	public int getComplaintCode() {
		return complaintCode;
	}

	public void setComplaintCode(int complaintCode) {
		this.complaintCode = complaintCode;
	}

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

	public String getComplaintNumber() {
		return complaintNumber;
	}

	public void setComplaintNumber(String complaintNumber) {
		this.complaintNumber = complaintNumber;
	}

	public String getConsumerMobileNumber() {
		return consumerMobileNumber;
	}

	public void setConsumerMobileNumber(String consumerMobileNumber) {
		this.consumerMobileNumber = consumerMobileNumber;
	}

	public String getServiceApplicationNo() {
		return serviceApplicationNo;
	}

	public void setServiceApplicationNo(String serviceApplicationNo) {
		this.serviceApplicationNo = serviceApplicationNo;
	}

	public boolean isFieldDisabled() {
		return fieldDisabled;
	}

	public void setFieldDisabled(boolean fieldDisabled) {
		this.fieldDisabled = fieldDisabled;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public SimpleDateFormat getSdf() {
		return sdf;
	}

	public void setSdf(SimpleDateFormat sdf) {
		this.sdf = sdf;
	}

	public String getFormattedDate() {
		return formattedDate;
	}

	public void setFormattedDate(String formattedDate) {
		this.formattedDate = formattedDate;
	}
	
	

}

