package tneb.ccms.admin.controller.reportController;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import tneb.ccms.admin.model.ComplaintBean;
import tneb.ccms.admin.model.CustMasterBean;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.valuebeans.AdminUserValueBean;
import tneb.ccms.admin.valuebeans.CallCenterUserValueBean;
import tneb.ccms.admin.valuebeans.ViewComplaintReportValueBean;

@Named
@RequestScoped
public class SingleComplaintView {
	
	ViewComplaintReportValueBean complaint ;
	DataModel dmFilter ;
	private SessionFactory sessionFactory;
	
	public ViewComplaintReportValueBean getComplaint() {
		return complaint;
	}


	public void setComplaint(ViewComplaintReportValueBean complaint) {
		this.complaint = complaint;
	}


	@PostConstruct
	public void init() {
		System.out.println("Initializing SingleComplaintView...");
		sessionFactory = HibernateUtil.getSessionFactory();
		dmFilter = new DataModel();
		dmFilter.setComplaintID(null);
		complaint = new ViewComplaintReportValueBean();

	}
	

	@Transactional
	public void getComplaintById() {
	    try (Session session = sessionFactory.openSession()) {

	    	Integer complaintId=null;
	    	 
	    	if(dmFilter.getComplaintID()==null) {
	    		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,"","Complaint ID is Required");
	    		FacesContext.getCurrentInstance().addMessage(null, message);
	    		return;
	    	}else {
	         complaintId = dmFilter.getComplaintID();
	    	}
	    	
	    	
	        System.out.println("THE GIVEN COMPLAINT ID IS ======" + complaintId);
	        
	        HttpSession httpSession = (HttpSession) FacesContext.getCurrentInstance() .getExternalContext().getSession(false);
	        
	        //ADMIN USER LOGIN
	        AdminUserValueBean adminUserValueBean = (AdminUserValueBean) httpSession.getAttribute("sessionAdminValueBean");
	         
	        //CALL CENTER USER LOGIN
	 		CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpSession.getAttribute("sessionCallCenterUserValueBean");
	 		
			List<Integer> callCenterCircleId = new ArrayList<>();

			Integer roleId = null;
			Integer regionID = null;
			Integer circleID = null;
			Integer divisionID = null;
			Integer subDivisionID = null;
			Integer sectionID = null;
	 		
			// IF CALL CENTER USER LOGIN
	 		if(callCenterValueBean!=null) {
	 			Session session2 = sessionFactory.openSession();
			    try {
			        session2.beginTransaction();
			        
			        Integer userId = callCenterValueBean.getId();
			        
			        @SuppressWarnings("unchecked")
					List<Integer> circleId =  session2.createQuery(
			            "select c.circleBean.id from CallCenterMappingBean c " +
			            "where c.callCenterUserBean.id = :userId")
			            .setParameter("userId", userId)
			            .getResultList();
			        
			        callCenterCircleId = circleId;
			        
			        session2.getTransaction().commit();
			    } finally {
			        session2.close();
			    }
	 			
	 		}

	 		// IF ADMIN USER LOGIN
	 		else if(adminUserValueBean!=null) {
	         
	          roleId = adminUserValueBean.getRoleId();
	          regionID = adminUserValueBean.getRegionId();
	          circleID = adminUserValueBean.getCircleId();
	          divisionID = adminUserValueBean.getDivisionId();
	          subDivisionID = adminUserValueBean.getSubDivisionId();
	          sectionID = adminUserValueBean.getSectionId();
	 		}
	 		
	 		
	 		    String regCusId=null;
	 		    String distribCode =null;
	 			Session session3 = sessionFactory.openSession();
			    try {
			        session3.beginTransaction();
			        
			        			        
			        ComplaintBean complaint = (ComplaintBean) session3.createQuery(
			                "select c from ComplaintBean c where c.id = :complaintId", ComplaintBean.class)
			            .setParameter("complaintId", complaintId)
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
	                "LEFT JOIN DISTRIB_MASTER dm ON dm.reg_no = f.ID AND dm.SCODE = j.code AND dm.CIRCLE_CODE= g.code AND dm.DISTRIB_CODE = :distribCode "+
	                "WHERE a.id = :complaintIdParam " ;
	        
            //ADMIN USER LOGIN
	        if(adminUserValueBean!=null) {
	         if (adminUserValueBean.getRoleId() >= 1 && adminUserValueBean.getRoleId() <= 5) {
	             hql += " AND a.region_id = :regionId ";
	             if (adminUserValueBean.getRoleId() <= 4) {
	                 hql += " AND a.circle_id = :circleId ";
	             }
	             if (adminUserValueBean.getRoleId() <= 3) {
	                 hql += " AND a.division_id = :divisionId ";
	             }
	             if (adminUserValueBean.getRoleId() <= 2) {
	                 hql += " AND a.sub_division_id = :subDivisionId ";
	             }
	             if (adminUserValueBean.getRoleId() == 1) {
	                 hql += " AND a.section_id = :sectionId ";
	             }
	         }
	         else if (adminUserValueBean.getRoleId() == 10) {

	             hql += " AND a.device = :device ";
	         }
	        }
			
	        // CALL CENTER USER LOGIN
	        else   if (callCenterValueBean != null && callCenterCircleId != null) {
	        	//AE FOC
	        	if(callCenterValueBean.getRoleId()==1) {
	        	 hql += "AND a.circle_id IN :callCenterCircleId ";
	        	}
	        	// M ADMIN	
	        	else if(callCenterValueBean.getRoleId()==5) {
	        		hql += "AND a.device = :device ";
	        	}
	        	//CIRCLE AGENT
	        	else if(callCenterValueBean.getRoleId()==7) {
	        		hql += "AND a.circle_id IN :callCenterCircleId ";
	        		hql += "AND a.device = :device ";
	        	}
	        	// SM USERS
	        	else if(callCenterValueBean.getRoleId()==3) {
	        		hql += "AND a.device = :device ";
	        	}
	        	 
	         }
			
			Query query = session.createNativeQuery(hql);
			query.setParameter("complaintIdParam", complaintId);
			query.setParameter("distribCode", distribCode);
			
			 // Set parameters based on role
			if(adminUserValueBean!=null) {
	         if (adminUserValueBean.getRoleId() >= 1 && adminUserValueBean.getRoleId() <= 5) {
	             query.setParameter("regionId", regionID);
	             if (adminUserValueBean.getRoleId() <= 4) {
	                 query.setParameter("circleId", circleID);
	             }
	             if (adminUserValueBean.getRoleId() <= 3) {
	                 query.setParameter("divisionId", divisionID);
	             }
	             if (adminUserValueBean.getRoleId() <= 2) {
	                 query.setParameter("subDivisionId", subDivisionID);
	             }
	             if (adminUserValueBean.getRoleId() == 1) {
	                 query.setParameter("sectionId", sectionID);
	             }
	         } else if(adminUserValueBean.getRoleId()==10) {

                 query.setParameter("device", "MI");

	         }
			}
			
			else   if (callCenterValueBean != null && callCenterCircleId != null) {
				//AE FOC
	        	if(callCenterValueBean.getRoleId()==1) {
	        	 query.setParameter("callCenterCircleId", callCenterCircleId);
	        	}
	        	// M ADMIN
	        	else if(callCenterValueBean.getRoleId()==5) {
	        	 query.setParameter("device", "MI");
	        	}
	        	// CIRCLE AGENT
	        	else if(callCenterValueBean.getRoleId()==7) {
		        	 query.setParameter("callCenterCircleId", callCenterCircleId);
		        	 query.setParameter("device", "MI");
	        	}
	        	// SM USERS
	        	else if(callCenterValueBean.getRoleId()==3) {
	        		query.setParameter("device", "SM");
	        	}
	        	 
	         }
	
			List<Object[]> results = query.getResultList();

			complaint = new ViewComplaintReportValueBean();
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
	        	dto.setDistribution((String) row[23]);
	        	dto.setImage1((String) row[24]);
	        	dto.setImage2((String) row[25]);
	        	dto.setClosureImage((String) row[26]);
      	
	        	
	        	complaint=dto;
	        }	
	        

	        String transferHql = "SELECT to_char(TRF_ON, 'dd-mm-yyyy-hh24:mi'), TRF_USER, REMARKS " +
	                "FROM COMP_TRANSFER WHERE COMP_ID = :complaintId " +
	                "ORDER BY TRF_ON";
	        
	        Query transferQuery = session.createNativeQuery(transferHql);
	        transferQuery.setParameter("complaintId", complaintId);
	        List<Object[]> transferResults = transferQuery.getResultList();
	        List<ViewComplaintReportValueBean.TransferDetail> transfers = new ArrayList<>();

	        for (Object[] transfer : transferResults) {
	            ViewComplaintReportValueBean.TransferDetail td = new ViewComplaintReportValueBean.TransferDetail();
	            td.setTransferDate((String) transfer[0]);
	            td.setTransferredBy((String) transfer[1]);
	            td.setTransferRemarks((String) transfer[2]);
	            transfers.add(td);
	        }
	        complaint.setTransferDetails(transfers);
	        
	        
	        String resendHql = "SELECT to_char(RESEND_ON, 'dd-mm-yyyy-hh24:mi'), ENTRYUSER, REMARKS " +
	                   "FROM COMP_RESEND WHERE COMP_ID = :complaintId " +
	                   "ORDER BY RESEND_ON";

	Query resendQuery = session.createNativeQuery(resendHql); 
	resendQuery.setParameter("complaintId", complaintId);

	List<Object[]> resendResults = resendQuery.getResultList(); 

	List<ViewComplaintReportValueBean.ResendDetail> resendResultList = new ArrayList<>();

	for (Object[] resend : resendResults) {
	    ViewComplaintReportValueBean.ResendDetail td = new ViewComplaintReportValueBean.ResendDetail();
	    td.setResendDate((String) resend[0]);
	    td.setResendedBy((String) resend[1]);
	    td.setResendRemarks((String) resend[2]);
	    resendResultList.add(td);
	}

	this.complaint.setResendDetails(resendResultList);
	        
	        
	        String qcHql = "SELECT to_char(QC_ON, 'dd-mm-yyyy-hh24:mi'), QC_STATUS, REMARKS " +
	                "FROM COMP_QC_DETAILS WHERE COMP_ID = :complaintId " +
	                "ORDER BY QC_ON";
			  Query qcQuery = session.createNativeQuery(qcHql);
			  qcQuery.setParameter("complaintId", complaintId);
			  List<Object[]> qcResults = qcQuery.getResultList();
			  
			  List<ViewComplaintReportValueBean.QualityCheckDetail> qcs = new ArrayList<>();
			  for (Object[] qc : qcResults) {
			      ViewComplaintReportValueBean.QualityCheckDetail qcd = new ViewComplaintReportValueBean.QualityCheckDetail();
			      qcd.setQcDate((String) qc[0]);
			      qcd.setQcStatus((String) qc[1]);
			      qcd.setQcRemarks((String) qc[2]);
			      qcs.add(qcd);
			  }
			  complaint.setQualityCheckDetails(qcs);
	        
	        System.out.println("THE SELECED COMPLAINT ID"+ complaint.getComplaintId());
	        System.out.println("THE SELECED COMPLAINT ID"+ complaint.getDescription());
	        
			if(complaint.getComplaintId()==null) {
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,"","NOT AUTHORIZED TO VIEW THIS COMPLAINT / INVALID COMPLAINT ID");
				FacesContext.getCurrentInstance().addMessage("", message);
				dmFilter.setComplaintID(null);
				return;
			}
	        
		    FacesContext.getCurrentInstance().getExternalContext().redirect("singleComplaintDetails.xhtml");

			
			}catch(Exception e) {
				e.printStackTrace();
				
			}
	}
	
	public void resetAllFilters() {
		dmFilter = new DataModel();
		dmFilter.setComplaintID(null);
	}
	
	public void returnToSearchPage() throws IOException {
		dmFilter.setComplaintID(null);
		FacesContext.getCurrentInstance().getExternalContext().redirect("singleComplaintView.xhtml");
	}
	
	@Transactional
	public void getComplaintDetailedView() throws IOException {
	    try (Session session = sessionFactory.openSession()) {
	    	
	        String complaintIdParam = FacesContext.getCurrentInstance()
	                .getExternalContext()
	                .getRequestParameterMap()
	                .get("complaintID");
	        
	        System.out.println("THE GIVEN COMPLAINT ID IS ======" + complaintIdParam);

	        String hql = "SELECT a.id, " +
	                "to_char(a.created_on, 'dd-mm-yyyy-hh24:mi') AS Complaint_Date, " +
	                "DECODE(a.device, 'web', 'Web', 'FOC', 'FOC', 'admin', 'FOC', 'SM', 'Social Media', " +
	                "'Android', 'Mobile', 'AMOB', 'Mobile', 'IMOB', 'Mobile', 'iOS', 'Mobile', " +
	                "'mobile', 'Mobile', 'MI', 'Minnagam') AS Device, " +
	                "a.SERVICE_NUMBER AS Service_Number, " +
	                "a.SERVICE_NAME AS Service_Name, " +
	                "a.SERVICE_ADDRESS AS Service_Address, " +
	                "b.mobile AS Contact_Number, k.name AS Complaint_Type, d.name AS subctyp, " +
	                "a.description AS Complaint_Description, " +
	                "DECODE(a.status_id, 0, 'Pending', 1, 'In Progress', 2, 'Completed') AS Complaint_Status, " +
	                "TO_CHAR(a.updated_on, 'DD-MM-YYYY-HH24:MI') AS Attended_Date, " +
	                "c.DESCRIPTION AS Attended_Remarks, " +
	                "f.name AS Region, g.name AS Circle, h.name AS Division, i.name AS SubDivision, j.name AS Section, " +
	                "b.FIRST_NAME AS UserName, " +
	                "a.ALTERNATE_MOBILE_NO as RegisteredMobileNumber, " +
	                "to_char(fb.ENTRYDT, 'dd-mm-yyyy-hh24:mi') AS feedBackEntryDate, " +
	                "fb.REMARKS AS feedbackRemarks, " +
	                "fb.RATING AS feedbackRating " +
	                "FROM COMPLAINT a " +
	                "JOIN PUBLIC_USER b ON a.user_id = b.id " +
	                "JOIN COMPLAINT_HISTORY c ON a.id = c.complaint_id AND a.status_id = c.status_id " +
	                "LEFT JOIN COMP_FEEDBACK fb ON a.id = fb.COMP_ID " +
	                "LEFT JOIN COMP_TRANSFER ct ON a.id = ct.COMP_ID " +
	                "LEFT JOIN COMP_QC_DETAILS qc ON a.id = qc.COMP_ID " +
	                "JOIN SUB_CATEGORY d ON a.sub_category_id = d.id " +
	                "JOIN CATEGORY k ON a.complaint_type = k.code " +
	                "JOIN REGION f ON a.region_id = f.id " +
	                "JOIN CIRCLE g ON a.circle_id = g.id " +
	                "JOIN DIVISION h ON a.division_id = h.id " +
	                "JOIN SUB_DIVISION i ON a.sub_division_id = i.id " +
	                "JOIN SECTION j ON a.section_id = j.id " +
	                "WHERE a.id = :complaintIdParam";

	        Query query = session.createNativeQuery(hql);
	        query.setParameter("complaintIdParam", complaintIdParam);
	        
	        List<Object[]> results = query.getResultList();

	        complaint = new ViewComplaintReportValueBean();
	        for (Object[] row : results) {
	            ViewComplaintReportValueBean dto = new ViewComplaintReportValueBean();
	            dto.setComplaintId((BigDecimal) row[0]); // COMPLAINT ID
	            dto.setCreatedOnFormatted((String) row[1]);
	            dto.setDevice((String) row[2]); // Compl.Received through
	            dto.setServiceNumber((String) row[3]);
	            dto.setServiceName((String) row[4]);
	            dto.setServiceAddress((String) row[5]); // COMPLAINANT ADDRESS
	            dto.setMobile((String) row[6]); // COMPLAINANT MOBILE NUM
	            dto.setComplaintType((String) row[7]); // Complaint Category
	            dto.setSubCategoryName((String) row[8]); // Sub Category
	            dto.setComplaintDescription((String) row[9]); // Description
	            dto.setComplaintStatusValue((String) row[10]);
	            dto.setAttendedDate((String) row[11]); // CLOSURE DATE
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
	            dto.setFeedbackRating((Integer) row[22]);
	            
	            complaint = dto;
	        }
	        
            // TRANSFER DETAILS
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
	        complaint.setTransferDetails(transfers);

	        // QC DETAILS
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
	        complaint.setQualityCheckDetails(qcs);

	        System.out.println("THE SELECTED COMPLAINT ID " + complaint.getComplaintId());
	        System.out.println("THE SELECTED COMPLAINT DESCRIPTION " + complaint.getDescription());

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    //REDIRECTED TO SINGLE COMPLAINT DETAIL PAGE.
	    FacesContext.getCurrentInstance().getExternalContext().redirect("singleComplaintDetails.xhtml");
	}
	
//	public String getImagePath(String filename) {
//	    if (filename == null || filename.isEmpty()) {
//	        return "";
//	    }
//
//	    try {
//	        String[] parts = filename.split("_");
//	        if (parts.length >= 3) {
//	            String datePartWithExtension = parts[2]; // e.g., "31-07-25.jpg"
//	            String datePart = datePartWithExtension.split("\\.")[0]; // removes .jpg
//
//	            String[] dateParts = datePart.split("-");
//	            if (dateParts.length == 3) {
//	                String day = dateParts[0];
//	                String month = dateParts[1];
//	                String yearShort = dateParts[2];
//
//	                String yearFull = "20" + yearShort;
//
//	                return yearFull + "/" + month + "/" + filename;
//	            }
//	        }
//	    } catch (Exception e) {
//	        System.err.println("Error parsing image path for: " + filename);
//	    }
//
//	    return filename;
//	}
	
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


	
	
	public void refreshTheFilter() {
        this.dmFilter = new DataModel();
        this.dmFilter.setComplaintID(null);
        this.complaint = new ViewComplaintReportValueBean();
    }
	public String redirectToComplaintID() {
        return "singleComplaintView.xhtml?faces-redirect=true";
    }
	
	public StreamedContent getComplaintImage1() {
	    if (complaint != null && complaint.getImage1() != null) {
	        return DefaultStreamedContent.builder()
	            .contentType("image/jpeg") 
	            .stream(() -> new ByteArrayInputStream(complaint.getImages1()))
	            .build();
	    }
	    return null;
	}

	public StreamedContent getComplaintImage2() {
	    if (complaint != null && complaint.getImage2() != null) {
	        return DefaultStreamedContent.builder()
	            .contentType("image/jpeg")
	            .stream(() -> new ByteArrayInputStream(complaint.getImages2()))
	            .build();
	    }
	    return null;
	}
	

	public void refresh() {
        dmFilter.setComplaintID(null);
        complaint = new ViewComplaintReportValueBean();
    }

	public DataModel getDmFilter() {
		return dmFilter;
	}
	public void setDmFilter(DataModel dmFilter) {
		this.dmFilter = dmFilter;
	}
	
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}


	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	

}

