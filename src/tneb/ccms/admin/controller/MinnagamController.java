package tneb.ccms.admin.controller;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.type.StandardBasicTypes;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tneb.ccms.admin.AdminMain;
import tneb.ccms.admin.dao.ComplaintsDao;
import tneb.ccms.admin.dao.MinnagamUserDao;
import tneb.ccms.admin.model.AdminUserBean;
import tneb.ccms.admin.model.CallCenterMappingBean;
import tneb.ccms.admin.model.CallCenterRoleBean;
import tneb.ccms.admin.model.CallCenterUserBean;
import tneb.ccms.admin.model.CallCenterUserHistoryBean;
import tneb.ccms.admin.model.CircleBean;
import tneb.ccms.admin.model.ComplaintBean;
import tneb.ccms.admin.model.LoginParams;
import tneb.ccms.admin.model.MinnagamUserRequestBean;
import tneb.ccms.admin.model.OutagesBean;
import tneb.ccms.admin.model.RoleBean;
import tneb.ccms.admin.util.CCMSConstants;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.DataModel.ComplaintLog;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.valuebeans.CallCenterUserHistoryValueBean;
import tneb.ccms.admin.valuebeans.CallCenterUserValueBean;
import tneb.ccms.admin.valuebeans.MinnagamUserRequestValueBean;
import tneb.ccms.admin.valuebeans.OutageDetailsValueBean;
import tneb.ccms.admin.valuebeans.OutagesValueBean;
import tneb.ccms.admin.valuebeans.ViewComplaintValueBean;


public class MinnagamController {
	
	CallCenterUserValueBean callCenterUserValueBean = new CallCenterUserValueBean();
	MinnagamUserRequestValueBean minnagamUserRequestValueBean = new MinnagamUserRequestValueBean();
	
	private CallCenterUserHistoryValueBean callCenterUserHistoryValueBean = new CallCenterUserHistoryValueBean();

	public CallCenterUserHistoryValueBean getCallCenterUserHistoryValueBean() {
	    return callCenterUserHistoryValueBean;
	}

	public void setCallCenterUserHistoryValueBean(CallCenterUserHistoryValueBean callCenterUserHistoryValueBean) {
	    this.callCenterUserHistoryValueBean = callCenterUserHistoryValueBean;
	}

	private String reason;
	
	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	DataModel dataModal = new DataModel();
	
	List<MinnagamUserRequestValueBean> userRequests;
	
	private List<MinnagamUserRequestValueBean> requestsedit;

	public List<MinnagamUserRequestValueBean> getRequestsedit() {
	    return requestsedit;
	}

	public void setRequestsedit(List<MinnagamUserRequestValueBean> requestsedit) {
	    this.requestsedit = requestsedit;
	}
	
//	List<MinnagamUserRequestValueBean> userRequestsEdit;
	private List<MinnagamUserRequestValueBean> userApproveRequest;
	
	

	public List<MinnagamUserRequestValueBean> getUserApproveRequest() {
		return userApproveRequest;
	}

	public void setUserApproveRequest(List<MinnagamUserRequestValueBean> userApproveRequest) {
		this.userApproveRequest = userApproveRequest;
	}


	List<ViewComplaintValueBean> minnagamComplaint;
	
	String complaintNumber;
	String consumerMobileNumber;
	String resetId;
	
	public String getResetId() {
		return resetId;
	}

	public void setResetId(String resetId) {
		this.resetId = resetId;
	}

	LoginParams office;
	
	DataModel dm = new DataModel();
	
	int statusId;
	 private List<OutageDetailsValueBean> plOutageRequest;
	
	 LoginParams officer;
	private Logger logger = LoggerFactory.getLogger(MinnagamController.class.getName());
	
	@ManagedProperty("#{admin}")
	AdminMain admin;
	
	private String userName;
	
	
	public MinnagamController() {
		super();
		init();
	}
	
	public void reloadRequests()
	{
		  userApproveRequest   = acceptRequest();
	}
	public void init() {
		System.err.println(">>> INIT CALLED");
		
		admin = (AdminMain) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("admin");
		officer = admin.getAuth().getOfficer();
             
             
		userRequests         = loadUserRequestsFromDatabase();
        userApproveRequest   = acceptRequest();
		requestsedit = loadUserRequestsEditFromDatabase();
		plOutageRequest = loadPlOutagesFromDatabase();
	     //System.err.println("✅ Loaded size: " + (plOutageRequest != null ? plOutageRequest.size() : "null"));
		
	}
	
	public void newusercreateschedule()
	{
		 PrimeFaces.current().executeScript("window.location.href = '" +
			        FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() +
			        "/faces/admin/schedule.xhtml';");
	}
	public void newusercreate() {
		minnagamUserRequestValueBean.setName("");
		minnagamUserRequestValueBean.setEmailId("");
		minnagamUserRequestValueBean.setMobileNumber("");
		minnagamUserRequestValueBean.setUserName("");
		minnagamUserRequestValueBean.setAgentRole(null);
		
			    PrimeFaces.current().executeScript("window.location.href = '" +
	        FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() +
	        "/faces/admin/addMinnagamUser.xhtml';");
	}

	public void closeMinnagamUserRequest()
	{
		try
		{
			this.callCenterUserHistoryValueBean.setReason(null);
			// ADD this line to clear/reset the form bean
		    minnagamUserRequestValueBean = new MinnagamUserRequestValueBean();
		    minnagamUserRequestValueBean.setName(null);
		    minnagamUserRequestValueBean.setMobileNumber(null);
		    minnagamUserRequestValueBean.setEmailId(null);
		    minnagamUserRequestValueBean.setCircleId(null);
		   
			
		}
		catch (Exception e) {
        	logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		
        } 
	}
	public void selectedUser(MinnagamUserRequestValueBean userRequest) {
	    this.minnagamUserRequestValueBean = userRequest;
	   
	}
	public void loadUserRequests() {
		 userRequests         = loadUserRequestsFromDatabase();
		 //userApproveRequest   = acceptRequest();
		
	}
	
	public void loadUserApproveRequest()
	{
		userApproveRequest   = acceptRequest();
	}
	
	public List<OutageDetailsValueBean> getPlOutageRequest() {
 		return plOutageRequest;
 	}

 	public void setPlOutageRequest(List<OutageDetailsValueBean> plOutageRequest) {
 		this.plOutageRequest = plOutageRequest;
 	}
	@SuppressWarnings("unchecked")
	public List<OutageDetailsValueBean> loadPlOutagesFromDatabase() {
	
		List<OutageDetailsValueBean> plOutageRequest = new ArrayList<>();
		   
		    SessionFactory factory = HibernateUtil.getSessionFactory();
		    Session session = null;

		    try {
		        session = factory.openSession();

		        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

		     // Format as a string with milliseconds + pad to nanoseconds
		     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		     String timestampString = sdf.format(currentTimestamp) + "000000"; // pad to FF9
		     String sql;
		     List<Object[]> resultList;
		     if(officer.getRoleId()==10) {
		    	 System.err.println("ROLE ID  ADMIN"+officer.getRoleId());
		    	 
		    	 sql = "SELECT pl.outg_id, dt.cir_name, sec.name, dt.ss_name, dt.fdr_name, dt.dt_name, pl.frprd, pl.toprd " +
			                "FROM pl_outages pl " +
			                "JOIN dtmastergis dt ON dt.dt_code = pl.ssfdrstrcode " +
			                "JOIN section sec ON sec.id = pl.section_id " +
			                "WHERE pl.status_name = 'O' AND pl.frprd > TO_TIMESTAMP(:tsString, 'YYYY-MM-DD HH24:MI:SS.FF9')";
		    	 resultList = session.createSQLQuery(sql)
                         .setParameter("tsString", timestampString)
                         .getResultList();
		     }
		        
		     else
		     {
		    	 System.err.println("ROLE ID SECTION"+officer.getRoleId());
		    	 System.err.println("CIRCLE ID"+officer.circleId);
		    	 
		    	 
		    	 sql = "SELECT pl.outg_id, dt.cir_name, sec.name, dt.ss_name, dt.fdr_name, dt.dt_name, pl.frprd, pl.toprd " +
			                "FROM pl_outages pl " +
			                "JOIN dtmastergis dt ON dt.dt_code = pl.ssfdrstrcode " +
			                "JOIN section sec ON sec.id = pl.section_id " +
			                "WHERE pl.status_name = 'O' AND pl.frprd > TO_TIMESTAMP(:tsString, 'YYYY-MM-DD HH24:MI:SS.FF9') AND pl.CIRCLE_ID = :cirId AND pl.SECTION_ID = :secId";
		    	 resultList = session.createSQLQuery(sql)
                         .setParameter("tsString", timestampString)
                         .setParameter("cirId", officer.circleId, StandardBasicTypes.INTEGER)
                         .setParameter("secId", officer.sectionId, StandardBasicTypes.INTEGER)
                         .getResultList();
		     }

	
		   
		   for (Object[] row : resultList) {
		       OutageDetailsValueBean dto = new OutageDetailsValueBean();

		       dto.setOutgId(((String) row[0]));
		       dto.setCirName((String) row[1]);
		       dto.setSecName((String) row[2]);
		       dto.setSsName((String) row[3]);
		       dto.setFdrName((String) row[4]);
		       dto.setDtName((String) row[5]);
		       dto.setFrprd((Date) row[6]);
		       dto.setToprd((Date) row[7]);

		       //System.err.println("ADFSDFSDFSDF::::::::::::"+row[0]);
		       plOutageRequest.add(dto);
		   }


		    } catch (Exception e) {
		        logger.error("Error loading user requests: ", e);
		        FacesContext.getCurrentInstance().addMessage(null, 
		            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		    } finally {
		        if (session != null) {
		            session.close();
		        }
		    }
		    this.plOutageRequest = plOutageRequest;
		    return plOutageRequest;
		
		
		
	}
	
	
	public boolean isPasswordDefault(MinnagamUserRequestValueBean user) {
	    if (user == null || user.getPassword() == null) {
	        return false;
	    }
	    try {
	        String defaultPasswordHash = md5("12345");
	        if( defaultPasswordHash.equals(user.getPassword())) {
	        	return false;
	        }
	        else {
	        	return true;
	        }
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
		return false;
	}
	
	
	public void resetPassword() {
	    SessionFactory factory = HibernateUtil.getSessionFactory();
	    Session session = null;
	    Transaction transaction = null;
	    try {
	        System.out.println("Reset ID: " + resetId);
	        String hashedPassword = md5("12345");
	        String userName = resetId;
	        session = factory.openSession();
	        transaction = session.beginTransaction();
	            
	        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
	        CriteriaQuery<MinnagamUserRequestBean> criteriaQuery = criteriaBuilder.createQuery(MinnagamUserRequestBean.class);
	        Root<MinnagamUserRequestBean> root = criteriaQuery.from(MinnagamUserRequestBean.class);

	        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("userName"), userName));

	        MinnagamUserRequestBean minnagamUserBean = session.createQuery(criteriaQuery).uniqueResult();

	        if (minnagamUserBean != null) {
	            if (hashedPassword.equals(minnagamUserBean.getPassword())) {

		            PrimeFaces.current().executeScript("PF('resetSuccessDialog').show(); setTimeout(function() { PF('resetSuccessDialog').hide(); }, 2000);");

	                return;
	            }
	            
	            minnagamUserBean.setPassword(hashedPassword);
	            minnagamUserBean.setUpdatedOn(new java.sql.Timestamp(System.currentTimeMillis()));
	            
	            session.update(minnagamUserBean);
	            session.flush(); 
	            transaction.commit();
	            
	            PrimeFaces.current().executeScript("PF('resetSuccessDialog').show(); setTimeout(function() { PF('resetSuccessDialog').hide(); }, 2000);");
	        	
	        }
	    } catch(Exception e) {
	        if (transaction != null) transaction.rollback();
	        e.printStackTrace();
	        FacesContext.getCurrentInstance().addMessage(null,
	            new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Error resetting password"));
	    } finally {
	        if (session != null) session.close();
	    }
	}
	
	public void deleteSchedule()
	{
		    Session session = null;
		    Transaction transaction = null;
		    try {
		    	String schId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("scheduleId");
				System.err.println("SCHID:::::"+schId);
				
		        session = HibernateUtil.getSessionFactory().openSession();
		        transaction = session.beginTransaction();
		        CriteriaBuilder cb = session.getCriteriaBuilder();
		        
		        CriteriaUpdate<OutagesBean> update = cb.createCriteriaUpdate(OutagesBean.class);
		        Root<OutagesBean> root = update.from(OutagesBean.class);
		        
		        update.set(root.get("status"), "D")
		               .set(root.get("deleteUser"), officer.getUserName())
		               .where(cb.equal(root.get("outgId"), schId));
		        
		        session.createQuery(update).executeUpdate();
		        transaction.commit();
		        // ✅ 3. Refresh the full list (optional but ensures consistency)
		        this.plOutageRequest = loadPlOutagesFromDatabase();

		        // ✅ 4. Update the table in the UI
		        PrimeFaces.current().ajax().update("userRequestsTable");
		        
		        PrimeFaces.current().executeScript("PF('messageDialogAlreadyEntered').show(); setTimeout(function() { PF('messageDialogAlreadyEntered').hide(); }, 5000);");
		    	PrimeFaces.current().executeScript("setTimeout(function(){ window.location.href = '" +
					    FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() +
					    "/faces/admin/scheduleDetails.xhtml'; }, 5000);");
				
		    }
		    catch (Exception e) {
		        if (transaction != null) transaction.rollback();
		        logger.error("Error updating status: " + e.getMessage());
		        FacesContext.getCurrentInstance().addMessage(null,
		            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Update failed."));
		    } finally {
		        if (session != null && session.isOpen()) {
		            session.close();
		        }
		    }
		
		
	}
	public List<MinnagamUserRequestValueBean> loadUserRequestsEditFromDatabase() {
	    List<MinnagamUserRequestValueBean> requestsedit = new ArrayList<>();
	    SessionFactory factory = HibernateUtil.getSessionFactory();
	    Session session = null;

	    try {
	        session = factory.openSession();
	        CriteriaBuilder cb = session.getCriteriaBuilder();
	        CriteriaQuery<MinnagamUserRequestBean> cq = cb.createQuery(MinnagamUserRequestBean.class);
	        Root<MinnagamUserRequestBean> root = cq.from(MinnagamUserRequestBean.class);

	        // Build query: SELECT * FROM MinnagamUserRequestBean WHERE statusId = 1 ORDER BY id DESC
	        cq.select(root)
	          .where(cb.isNotNull(root.get("userName")))
	          .orderBy(cb.desc(root.get("id")));

	        List<MinnagamUserRequestBean> resultList = session.createQuery(cq).getResultList();

	        for (MinnagamUserRequestBean requestBean : resultList) {
	            MinnagamUserRequestValueBean valueBean = MinnagamUserRequestValueBean.convertRequestBeanToValueBean(requestBean);
	        	String circleCode = valueBean.getCircleCode();
	        	String circleName = null;
	        	Integer circleId = valueBean.getCircleId();
	        	

	        	if (circleCode != null && !circleCode.isEmpty()) {

	        	    CircleBean circleBean = session.createQuery(
	        	            "from CircleBean c where c.code = :circleCode and c.id = :circleId", CircleBean.class)
	        	        .setParameter("circleCode", circleCode)  // ✅ Corrected line
	        	        .setParameter("circleId", circleId)
	        	        .getSingleResult();

	        	    circleName = circleBean.getName();
	        	    valueBean.setCircleName(circleName);
	        	}

	            requestsedit.add(valueBean);
	        }

	    } catch (Exception e) {
	        logger.error("Error loading user requests: ", e);
	        FacesContext.getCurrentInstance().addMessage(null, 
	            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
	    } finally {
	        if (session != null) {
	            session.close();
	        }
	        PrimeFaces.current().executeScript("PF('reasonWidget').clear();");
	    }
	    
	    this.requestsedit=requestsedit;

	    return requestsedit;
	}

	public void updateMinnagamUserRequestStatus(MinnagamUserRequestValueBean userRequest) {
	    Session session = null;
	    Transaction transaction = null;

	    try {
	        session = HibernateUtil.getSessionFactory().openSession();
	        transaction = session.beginTransaction();

	        // ✅ 1. Toggle the status in the UI object FIRST (for instant update)
	        int newStatus = (userRequest.getStatusId() == 1) ? 0 : 1;
	        userRequest.setStatusId(newStatus); // Immediate UI update

	        System.err.println("user id:::::::::"+userRequest.getId());
	        
	        // ✅ 2. Update the database
	        CriteriaBuilder cb = session.getCriteriaBuilder();
	        CriteriaUpdate<MinnagamUserRequestBean> update = cb.createCriteriaUpdate(MinnagamUserRequestBean.class);
	        Root<MinnagamUserRequestBean> root = update.from(MinnagamUserRequestBean.class);
	        
	        update.set(root.get("statusId"), newStatus)
	              .where(cb.equal(root.get("id"), userRequest.getId()));
	        
	        session.createQuery(update).executeUpdate();
	        transaction.commit();

	        // ✅ 3. Refresh the full list (optional but ensures consistency)
	        requestsedit = loadUserRequestsEditFromDatabase(); // Reload fresh data

	        // ✅ 4. Update the table in the UI
	        PrimeFaces.current().ajax().update("userRequestsTable");

	        // Show success message
	        PrimeFaces.current().executeScript(
	            "PF('messageDialogAlreadyEntered').show(); " +
	            "setTimeout(function() { PF('messageDialogAlreadyEntered').hide(); }, 3000);"
	        );
	        
	    } catch (Exception e) {
	        if (transaction != null) transaction.rollback();
	        logger.error("Error updating status: " + e.getMessage());
	        FacesContext.getCurrentInstance().addMessage(null,
	            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Update failed."));
	    } finally {
	        if (session != null && session.isOpen()) {
	            session.close();
	        }
	    }
	}
	
	public List<MinnagamUserRequestValueBean> loadUserRequestsFromDatabase() {
	    SessionFactory factory = HibernateUtil.getSessionFactory();
	    Session session = null;
	    List<MinnagamUserRequestValueBean> requests = new ArrayList<>();
	    
	    try {
	        session = factory.openSession();
	        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
	        CriteriaQuery<MinnagamUserRequestBean> criteriaQuery = criteriaBuilder.createQuery(MinnagamUserRequestBean.class);
	        Root<MinnagamUserRequestBean> root = criteriaQuery.from(MinnagamUserRequestBean.class);
	        
	        criteriaQuery.select(root);
	        criteriaQuery.where(criteriaBuilder.isNull(root.get("userName")));
	        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("id"))); 
	       
	        Query<MinnagamUserRequestBean> query = session.createQuery(criteriaQuery);
	        List<MinnagamUserRequestBean> valueBen = query.getResultList();
	        
	        for(MinnagamUserRequestBean requestBean : valueBen) {
	        	
	        	MinnagamUserRequestValueBean minnagamValueBean = MinnagamUserRequestValueBean.convertRequestBeanToValueBean(requestBean);
	        	String circleCode = minnagamValueBean.getCircleCode();
	        	Integer circleId = minnagamValueBean.getCircleId();
	        	String circleName = null;

	        	if (circleCode != null && !circleCode.isEmpty()) {

	        	    CircleBean circleBean = session.createQuery(
	        	            "from CircleBean c where c.code = :circleCode and c.id= :circleId", CircleBean.class)
	        	        .setParameter("circleCode", circleCode)  
	        	        .setParameter("circleId", circleId)
	        	        .getSingleResult();

	        	    circleName = circleBean.getName();
	        	    minnagamValueBean.setCircleName(circleName);
	        	}

	        	requests.add(minnagamValueBean);
	        	
	        	
	        //	reloadRequests();

	        }
	        
	    } catch (Exception e) {
	        logger.error(ExceptionUtils.getStackTrace(e));
	        FacesContext.getCurrentInstance().addMessage(null, 
	            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
	    } finally {
	        if (session != null) {
	            session.close();
	            this.setReason(null);
	        }
	    }
	    return requests;
	}
	public List<MinnagamUserRequestValueBean> acceptRequest() {
		System.out.println("FETCH DATABASE FROM USER REQUESTS");
        SessionFactory factory = HibernateUtil.getSessionFactory();
        Session session = null;
        List<MinnagamUserRequestValueBean> requestsApprove = new ArrayList<MinnagamUserRequestValueBean>();
        try {
            session = factory.openSession();
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<MinnagamUserRequestBean> criteriaQuery = criteriaBuilder.createQuery(MinnagamUserRequestBean.class);
            Root<MinnagamUserRequestBean> root = criteriaQuery.from(MinnagamUserRequestBean.class);
            
            root.fetch("circleBean", JoinType.LEFT);
           
            criteriaQuery.select(root).where(criteriaBuilder.isNull(root.get("userName")))
            .orderBy(criteriaBuilder.asc(root.get("createdOn")));
           
            Query<MinnagamUserRequestBean> query = session.createQuery(criteriaQuery);
            
            List<MinnagamUserRequestBean>  valueBen = query.getResultList();
            
            for(MinnagamUserRequestBean requestBean : valueBen) {
                 MinnagamUserRequestValueBean minnagamValueBean = MinnagamUserRequestValueBean.convertRequestBeanToValueBean(requestBean);
                 
                 if (requestBean.getCircleBean() != null) {
                     minnagamValueBean.setCircleName(requestBean.getCircleBean().getName());
                 } 
                 
                 
                 requestsApprove.add(minnagamValueBean);
            }
             
            
        } catch (Exception e) {
        	logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		
        } finally {
        	 HibernateUtil.closeSession(factory, session);
        }
		return requestsApprove;
       
    }
	
	public void generateUsername() {
		 
        SessionFactory factory = HibernateUtil.getSessionFactory();
        Session session = factory.openSession();
        String lastUserName = null;
        
        String agentRole = minnagamUserRequestValueBean.getAgentRole();
		
        if ("Call Center Agent".equals(agentRole)) {
        	lastUserName = (String) session.createQuery("SELECT userName FROM MinnagamUserRequestBean WHERE userName LIKE 'a%' ORDER BY id DESC")
                    .setMaxResults(1).uniqueResult();
            this.userName = generateNextUserName(lastUserName);
            
        } else {
        	 int circleId = minnagamUserRequestValueBean.getCircleId();
             CircleBean circle = session.get(CircleBean.class, circleId);
             String circleCode = circle.getCode(); 
             
            
             lastUserName = (String) session.createQuery("SELECT userName FROM MinnagamUserRequestBean WHERE userName LIKE 'c%' AND userName NOT LIKE 'ca%' ORDER BY id DESC")
                     .setMaxResults(1)
                     .uniqueResult();
             
             
             this.userName = generateNextCircleUserName(lastUserName, circleCode);
        }
    }
	
	private String generateNextCircleUserName(String lastUserName, String circleCode) {
	   
	    if (lastUserName == null || lastUserName.isEmpty()) {
	        return "c" + circleCode + "001";
	    }
	    
	    
	    int lastNumber = Integer.parseInt(lastUserName.substring(5)); 
	    
	    
	    return String.format("c" + circleCode + "%03d", lastNumber + 1); 
	}
	
    private String generateNextCircleUserName(String lastUserName) {
        if (lastUserName == null || lastUserName.isEmpty()) {
            return "ca001";
        }
        int lastNumber = Integer.parseInt(lastUserName.substring(2));
        return String.format("ca%03d", lastNumber + 1);
    }

    private String generateNextUserName(String lastUserName) {
        if (lastUserName == null || lastUserName.isEmpty()) {
            return "a001";
        }
        int lastNumber = Integer.parseInt(lastUserName.substring(1));
        return String.format("a%03d", lastNumber + 1);
    }
    public void closeUserRequest() {
       
    }
    
    public void updateMinnagamUserRequest() {
        SessionFactory factory = null;
        Session session = null;
        Transaction transaction = null;
        System.err.println("DATE WELCOME");
        try {
            factory = HibernateUtil.getSessionFactory();
            session = factory.openSession();

            Timestamp updatedOn = new Timestamp(new Date().getTime());

            // Build the criteria query to fetch user requests
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<MinnagamUserRequestBean> cq = cb.createQuery(MinnagamUserRequestBean.class);
            Root<MinnagamUserRequestBean> roots = cq.from(MinnagamUserRequestBean.class);

            cq.select(roots)
              .where(cb.equal(roots.get("userName"), minnagamUserRequestValueBean.getUserName()))
              .orderBy(cb.desc(roots.get("id")));

            List<MinnagamUserRequestBean> resultList = session.createQuery(cq).getResultList();

            // Begin transaction
            transaction = session.beginTransaction();
            CallCenterUserHistoryBean historyBean = new CallCenterUserHistoryBean();
            // Save user history
            for (MinnagamUserRequestBean userBean : resultList) {
               
            	
            	   CriteriaQuery<CallCenterUserHistoryBean> cqcall = cb.createQuery(CallCenterUserHistoryBean.class);
                   Root<CallCenterUserHistoryBean> rootscall = cqcall.from(CallCenterUserHistoryBean.class);

                   cqcall.select(rootscall)
                     .where(cb.equal(rootscall.get("userName"), minnagamUserRequestValueBean.getUserName()))
                     .orderBy(cb.desc(rootscall.get("id")));
                   
                   List<CallCenterUserHistoryBean> historyList = session.createQuery(cqcall).getResultList();
                   CallCenterUserHistoryBean callhistroy = null;
                //historyBean.setFromDt(historyBean.getToDt());
                   if (!historyList.isEmpty()) {
                	   callhistroy = historyList.get(0); // latest history because of orderBy desc
                	    historyBean.setFromDt(callhistroy.getToDt());
                	} else {
                	    historyBean.setFromDt(userBean.getCreatedOn());
                	}
				
                historyBean.setCircleCode(userBean.getCircleCode());
                historyBean.setName(userBean.getName());
                historyBean.setEmailId(userBean.getEmailId());
                historyBean.setUserName(userBean.getUserName());
                historyBean.setRoleId(userBean.getRoleBean().getId());
                historyBean.setMobileNumber(userBean.getMobileNumber());
               
             
                historyBean.setToDt(updatedOn);
                historyBean.setReason(callCenterUserHistoryValueBean.getReason());
                //historyBean.setId(callCenterUserHistoryValueBean.getId()); // Ensure this is only set if not auto-generated

                session.save(historyBean);
            }

            // Update MinnagamUserRequestBean
            CriteriaUpdate<MinnagamUserRequestBean> criteriaUpdate =
                    cb.createCriteriaUpdate(MinnagamUserRequestBean.class);
            Root<MinnagamUserRequestBean> root = criteriaUpdate.from(MinnagamUserRequestBean.class);

            criteriaUpdate
                .set(root.get("emailId"), minnagamUserRequestValueBean.getEmailId())
                .set(root.get("mobileNumber"), minnagamUserRequestValueBean.getMobileNumber())
                .set(root.get("updatedOn"), updatedOn)
                .set(root.get("name"), minnagamUserRequestValueBean.getName())
                .where(cb.equal(root.get("userName"), minnagamUserRequestValueBean.getUserName()));

            session.createQuery(criteriaUpdate).executeUpdate();

            // Commit the transaction
            transaction.commit();

            // Close session and factory
            HibernateUtil.closeSession(factory, session);

           
//            historyBean.setReason("");
            // Show success message and redirect
            PrimeFaces.current().executeScript("PF('messageDialog').show(); setTimeout(function() { PF('messageDialog').hide(); }, 3000);");
            PrimeFaces.current().executeScript("setTimeout(function(){ window.location.href = '" +
                    FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() +
                    "/faces/admin/listminnagamrequestedit.xhtml'; }, 2000);");
            PrimeFaces.current().executeScript("PF('reasonWidget').clear();");
            
            
            this.callCenterUserHistoryValueBean.setReason("");

        } catch (Exception e) {
            if (transaction != null && transaction.getStatus().canRollback()) {
                transaction.rollback();
            }

            logger.error(ExceptionUtils.getStackTrace(e));
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));

        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    public void updateMinnagamUserRequestBeforeApprove() {
        SessionFactory factory = null;
        Session session = null;
        Transaction transaction = null;

        try {
            factory = HibernateUtil.getSessionFactory();
            session = factory.openSession();

            Timestamp updatedOn = new Timestamp(new Date().getTime());

            // Build the criteria query to fetch user requests
            CriteriaBuilder cb = session.getCriteriaBuilder();

            // Begin transaction
            transaction = session.beginTransaction();
          //  CallCenterUserHistoryBean historyBean = new CallCenterUserHistoryBean();
            // Save user history
//            for (MinnagamUserRequestBean userBean : resultList) {
//                
//
//                historyBean.setCircleCode(userBean.getCircleCode());
//                historyBean.setName(userBean.getName());
//                historyBean.setEmailId(userBean.getEmailId());
//                historyBean.setUserName(userBean.getUserName());
//                historyBean.setRoleId(userBean.getRoleBean().getId());
//                historyBean.setMobileNumber(userBean.getMobileNumber());
//                historyBean.setFromDt(userBean.getCreatedOn());
//                historyBean.setToDt(updatedOn);
//                historyBean.setReason(callCenterUserHistoryValueBean.getReason());
//                //historyBean.setId(callCenterUserHistoryValueBean.getId()); // Ensure this is only set if not auto-generated
//
//                session.save(historyBean);
//            }

            // Update MinnagamUserRequestBean
            CriteriaUpdate<MinnagamUserRequestBean> criteriaUpdate =
                    cb.createCriteriaUpdate(MinnagamUserRequestBean.class);
            Root<MinnagamUserRequestBean> root = criteriaUpdate.from(MinnagamUserRequestBean.class);

            criteriaUpdate
                .set(root.get("emailId"), minnagamUserRequestValueBean.getEmailId())
                .set(root.get("mobileNumber"), minnagamUserRequestValueBean.getMobileNumber())
                .set(root.get("updatedOn"), updatedOn)
                .set(root.get("name"), minnagamUserRequestValueBean.getName())
                .where(cb.equal(root.get("id"), minnagamUserRequestValueBean.getId()));

            session.createQuery(criteriaUpdate).executeUpdate();

            // Commit the transaction
            transaction.commit();

            // Close session and factory
            HibernateUtil.closeSession(factory, session);

           
//            historyBean.setReason("");
            // Show success message and redirect
            PrimeFaces.current().executeScript("PF('messageDialog').show(); setTimeout(function() { PF('messageDialog').hide(); }, 3000);");
            PrimeFaces.current().executeScript("setTimeout(function(){ window.location.href = '" +
                    FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() +
                    "/faces/admin/listminnagamrequest.xhtml'; }, 2000);");
            PrimeFaces.current().executeScript("PF('reasonWidget').clear();");
            
            
            this.callCenterUserHistoryValueBean.setReason("");

        } catch (Exception e) {
            if (transaction != null && transaction.getStatus().canRollback()) {
                transaction.rollback();
            }

            logger.error(ExceptionUtils.getStackTrace(e));
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));

        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

	public void saveMinnagamUserRequest() {
		try{
			
			MinnagamUserDao dao = new MinnagamUserDao();
			dao.saveUserRequest(minnagamUserRequestValueBean);
			
			minnagamUserRequestValueBean = new MinnagamUserRequestValueBean();
			
			loadUserRequests();
			
		}catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		
		}
	}
	
//	public void createRequest(MinnagamUserRequestValueBean userRequests) {
//		
//		
//		  SessionFactory factory = null;
//		  Session session = null;
//		  Transaction transaction = null;
//		  
//		  try {
//			  factory = HibernateUtil.getSessionFactory();
//	          session = factory.openSession();
//	          transaction = session.beginTransaction();
//	          
//	          CallCenterUserBean callCenterBean = new CallCenterUserBean();
//	          CallCenterMappingBean callCenterMapping = new CallCenterMappingBean();
//				
//				Timestamp createdOn = new Timestamp(new Date().getTime());
//				Timestamp updatedOn = new Timestamp(new Date().getTime());
//				
//				if(userRequests!=null) {
//					
//					
//					if ("Circle Agent".equals(userRequests.getAgentRole())) {
//			             
//			              callCenterBean.setUserName(userRequests.getUserName());
//			              
//			              userRequests.setPassword("12345");
//							
//				            String hashedPassword = md5(userRequests.getPassword());
//				            callCenterBean.setPassword(hashedPassword);
//							
//				            callCenterBean.setName(userRequests.getName());
//							
//				            callCenterBean.setMobile(userRequests.getMobileNumber());
//							
//				            callCenterBean.setEmail(userRequests.getEmailId());
//							
//				            callCenterBean.setCreatedOn(createdOn);
//							
//				            callCenterBean.setUpdatedOn(createdOn);
//							
//							CallCenterRoleBean callCenterRoleBean = new CallCenterRoleBean();
//				            callCenterRoleBean.setId(7);
//				            callCenterBean.setCallCenterRoleBean(callCenterRoleBean);
//				              
//				            session.save(callCenterBean);
//							 
//				             callCenterMapping.setCallCenterUserBean(callCenterBean);
//				              
//				             CircleBean circle = new CircleBean();
//				             circle.setId(userRequests.getCircleId());
//				             callCenterMapping.setCircleBean(circle);
//				               
//				             session.save(callCenterMapping);
//							
//			              
//			       }else {
//					
//					callCenterBean.setUserName(userRequests.getUserName());
//			
//					 CallCenterRoleBean callCenterRoleBean = new CallCenterRoleBean();
//		              callCenterRoleBean.setId(6); 
//		              callCenterBean.setCallCenterRoleBean(callCenterRoleBean);
//		              
//		              userRequests.setPassword("12345");
//						
//			            String hashedPassword = md5(userRequests.getPassword());
//			            callCenterBean.setPassword(hashedPassword);
//						
//			            callCenterBean.setName(userRequests.getName());
//						
//			            callCenterBean.setMobile(userRequests.getMobileNumber());
//						
//			            callCenterBean.setEmail(userRequests.getEmailId());
//						
//			            callCenterBean.setCreatedOn(createdOn);
//						
//			            callCenterBean.setUpdatedOn(createdOn);
//
//			            session.save(callCenterBean);
//			       }
//					
//					
//					
//					String userName = callCenterBean.getUserName(); 
//					
//					CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
//					CriteriaQuery<MinnagamUserRequestBean> criteriaQuery = criteriaBuilder.createQuery(MinnagamUserRequestBean.class);
//					Root<MinnagamUserRequestBean> root = criteriaQuery.from(MinnagamUserRequestBean.class);
//
//					criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("userName"), userName));
//
//					MinnagamUserRequestBean minnagamBean = session.createQuery(criteriaQuery).uniqueResult();
//
//					if (minnagamBean != null) {
//					    minnagamBean.setStatusId(1); 
//					    minnagamBean.setUpdatedOn(updatedOn);
//					    session.update(minnagamBean);  
//					    
//					}
//					
//					transaction.commit();
//					userApproveRequest   = acceptRequest();
//					session.refresh(callCenterBean);
//						
//					FacesContext.getCurrentInstance().addMessage(null,
//					new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "User created successfully."));
//				} else {
//					FacesContext.getCurrentInstance().addMessage(null,
//					new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Not created."));
//					}
//	           
//	           
//	           
//	        } catch (Exception e) {
//	        	logger.error(ExceptionUtils.getStackTrace(e));
//				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
//			
//	        } finally {
//	            if (session != null) {
//	                session.close();
//	            }
//	        }
//		
//	}
	
	public void createRequest(MinnagamUserRequestValueBean userRequests) {
		
		System.out.println("THE USER REQ---------------"+userRequests.getUserName());
		
		
		  SessionFactory factory = null;
		  Session session = null;
		  Transaction transaction = null;
		  
		  try {
			  factory = HibernateUtil.getSessionFactory();
	          session = factory.openSession();
	          transaction = session.beginTransaction();
	          
//	          CallCenterUserBean callCenterBean = new CallCenterUserBean();
//	          CallCenterMappingBean callCenterMapping = new CallCenterMappingBean();
	          
	          MinnagamUserRequestBean minnagamBean = new MinnagamUserRequestBean();
				
				Timestamp createdOn = new Timestamp(new Date().getTime());
				Timestamp updatedOn = new Timestamp(new Date().getTime());
				
				if(userRequests!=null) {
					
					
					if ("Circle Agent".equals(userRequests.getAgentRole())) {
						
						String code = userRequests.getCircleCode();  // e.g., "c0402017"
					
						String lastUserName = (String) session.createQuery(
						        "SELECT m.userName " +
						        "FROM MinnagamUserRequestBean m " +
						        "WHERE m.statusId IN (0,1)" +
						        "AND m.userName IS NOT NULL " +
						        "AND m.circleCode = ?1 " +  // Positional parameter
						        "AND m.userName NOT LIKE 'ca%' " +
						        "ORDER BY m.userName DESC")
						    .setParameter(1, code)  // Positional parameter binding
						    .setMaxResults(1)
						    .uniqueResult();

						 
						 System.out.println("LAST USERNAME :::::::::: "+lastUserName);
						 
						 
						 System.out.println("CODE ::::::::::::: "+code);
				    	   
				    	   String newUserName = generateNextUserNameCircle(lastUserName,code);
			             
						System.out.println("NEW USERNAME :::::::::::::: "+newUserName);
			              
			              userRequests.setPassword("12345");
							
				            String hashedPassword = md5(userRequests.getPassword());
				           
				            
				            int id = userRequests.getId();
							
							CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
							CriteriaQuery<MinnagamUserRequestBean> criteriaQuery = criteriaBuilder.createQuery(MinnagamUserRequestBean.class);
							Root<MinnagamUserRequestBean> root = criteriaQuery.from(MinnagamUserRequestBean.class);

							criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), id));

							MinnagamUserRequestBean minnagamUserBean = session.createQuery(criteriaQuery).uniqueResult();

							if (minnagamUserBean != null) {
								
								minnagamUserBean.setUserName(newUserName);
								minnagamUserBean.setPassword(hashedPassword);
								minnagamUserBean.setStatusId(1); 
								minnagamUserBean.setUpdatedOn(updatedOn);
								
							
							    session.saveOrUpdate(minnagamUserBean);
								
							}
							

			              
			       }else {
					
			    	   String lastUserName = (String) session.createQuery("SELECT userName " +
			    		        "FROM MinnagamUserRequestBean " +
			    		        "WHERE statusId = 1 AND userName IS NOT NULL AND userName LIKE 'a%' " +
			    		        "ORDER BY userName DESC")
	                          .setMaxResults(1).uniqueResult();
			    	   
			    	   System.out.println("LAST USERNAME :::::::::: "+lastUserName);
			    	   
			    	   String newUserName = generateNextUserNameUpdation(lastUserName);
				
			           System.out.println("NEW USERNAME :::::: "+newUserName);
			    	   
		              userRequests.setPassword("12345");
						
			            String hashedPassword = md5(userRequests.getPassword());
			           
					
			        	int id = userRequests.getId();
						
						CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
						CriteriaQuery<MinnagamUserRequestBean> criteriaQuery = criteriaBuilder.createQuery(MinnagamUserRequestBean.class);
						Root<MinnagamUserRequestBean> root = criteriaQuery.from(MinnagamUserRequestBean.class);

						criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), id));

						MinnagamUserRequestBean minnagamUserBean = session.createQuery(criteriaQuery).uniqueResult();

						if (minnagamUserBean != null) {
							
							minnagamUserBean.setUserName(newUserName);
							minnagamUserBean.setPassword(hashedPassword);
							minnagamUserBean.setStatusId(1); 
							minnagamUserBean.setUpdatedOn(updatedOn);
							
						
						    session.saveOrUpdate(minnagamUserBean);
						   
						}

			       }
					
					transaction.commit();
				  //  HibernateUtil.closeSession(factory, session);

					// session.flush(); 
					userApproveRequest = acceptRequest();
					loadUserRequestsFromDatabase();
					
					
					
				    // ✅ Show message first (JSF context still alive here)
				    PrimeFaces.current().executeScript("PF('messageDialog').show(); setTimeout(function() { PF('messageDialog').hide(); }, 3000);");

				    // ✅ Optionally delay redirect or use PrimeFaces `<p:dialog>` with onHide
				    PrimeFaces.current().executeScript("setTimeout(function() { window.location.href = '" +
				        FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() +
				        "/faces/admin/adduserrequest.xhtml'; }, 3100);");
				    
				   
				
				} else 
				{
					PrimeFaces.current().executeScript("PF('messageDialogAlreadyEntered').show(); setTimeout(function() { PF('messageDialogAlreadyEntered').hide(); }, 3000);");
					 PrimeFaces.current().executeScript("window.location.href = '" +
						        FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() +
						        "/faces/admin//adduserrequest.xhtml';");
					}
	           
	           
	           
	        } catch (Exception e) {
	        	// transaction.rollback();
	        	if (transaction != null && transaction.isActive()) {
                transaction.rollback(); // ✅ Rollback if failure
            }
	        	logger.error(ExceptionUtils.getStackTrace(e));
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
			
	        } finally {
	            if (session != null) {
	            	
	                session.close();
	               
	            }
	        }
		
	}
	
	private String generateNextUserNameCircle(String lastUserName, String circleCode) {
		   
	    if (lastUserName == null || lastUserName.isEmpty()) {
	        return "c" + circleCode + "001";
	    }

	    
	    String lastDigits = lastUserName.substring(1 + circleCode.length());

	    int lastNumber = 0;
	    try {
	        lastNumber = Integer.parseInt(lastDigits);  
	    } catch (NumberFormatException e) {
	        lastNumber = 0;  
	    }

	    lastNumber++;

	    return "c" + circleCode + String.format("%03d", lastNumber);
	}
	
	private String generateNextUserNameUpdation(String lastUserName) {
		
		if (lastUserName == null || lastUserName.isEmpty()) {
			return "a001"; 
		}
		
		int lastNumber = Integer.parseInt(lastUserName.substring(1));
		String nextUserName = String.format("a%03d", lastNumber + 1);
		
		return nextUserName;
	}
	
	//MD5 hashing method
			private String md5(String input) throws NoSuchAlgorithmException {
			    MessageDigest md = MessageDigest.getInstance("MD5");
			    md.update(input.getBytes());
			    byte[] digest = md.digest();	
			    StringBuilder sb = new StringBuilder();
			    for (byte b : digest) {
			        sb.append(String.format("%02x", b & 0xff));
			    }
			    return sb.toString();
			}
			
			public void listCurrentComplaints(int statusValue) {
				try {
					complaintNumber = "";
					consumerMobileNumber = "";
					
					statusId = statusValue;

				//	boolean filterApplied = getFilterApplied();

//					if(filterApplied) {
						ComplaintsDao daoComplaints = new ComplaintsDao();
						
						CallCenterDashboard call = new CallCenterDashboard();
						office = call.officer;

						minnagamComplaint = daoComplaints.getCurrentComplaintList(office,statusId);
//					} else {
//						CallCenterDao callCenterDao = new CallCenterDao();
		//
//						complaintList = callCenterDao.getComplaintList(officer, statusId, complaintCode);
//						
//					}
						
						System.out.println("COMPLAINT LIST :::::::::::::: "+minnagamComplaint.size());
						
						if(minnagamComplaint != null) {
							FacesContext.getCurrentInstance().getApplication().getNavigationHandler()
				            .handleNavigation(FacesContext.getCurrentInstance(), null, "complaintPage?faces-redirect=true");

						}
					
					
					dm.setLstComplaintLog(new ArrayList<ComplaintLog>());
				} catch (Exception e) {
					logger.error(ExceptionUtils.getStackTrace(e));
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
				}
			}
			
			public void listComplaints(int statusValue) {
				try {
					complaintNumber = "";
					consumerMobileNumber = "";
					
					statusId = statusValue;

				//	boolean filterApplied = getFilterApplied();

//					if(filterApplied) {
						ComplaintsDao daoComplaints = new ComplaintsDao();
						CallCenterDashboard call = new CallCenterDashboard();
						office = call.officer;

						minnagamComplaint = daoComplaints.getComplaintList(office,statusId);
//					} else {
//						CallCenterDao callCenterDao = new CallCenterDao();
		//
//						complaintList = callCenterDao.getComplaintList(officer, statusId, complaintCode);
//						
//					}
						
						System.out.println("COMPLAINT LIST :::::::::::::: "+minnagamComplaint.size());
						
						if(minnagamComplaint != null) {
							FacesContext.getCurrentInstance().getApplication().getNavigationHandler()
				            .handleNavigation(FacesContext.getCurrentInstance(), null, "complaintPage?faces-redirect=true");

						}
					
					
					dm.setLstComplaintLog(new ArrayList<ComplaintLog>());
				} catch (Exception e) {
					logger.error(ExceptionUtils.getStackTrace(e));
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
				}
			}
	
	public CallCenterUserValueBean getCallCenterUserValueBean() {
		return callCenterUserValueBean;
	}

	public void setCallCenterUserValueBean(CallCenterUserValueBean callCenterUserValueBean) {
		this.callCenterUserValueBean = callCenterUserValueBean;
	}

	public DataModel getDataModal() {
		return dataModal;
	}

	public void setDataModal(DataModel dataModal) {
		this.dataModal = dataModal;
	}

	public MinnagamUserRequestValueBean getMinnagamUserRequestValueBean() {
		return minnagamUserRequestValueBean;
	}

	public void setMinnagamUserRequestValueBean(MinnagamUserRequestValueBean minnagamUserRequestValueBean) {
		this.minnagamUserRequestValueBean = minnagamUserRequestValueBean;
	}

	public String getUserName() {
		return userName;
	}
	

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public List<MinnagamUserRequestValueBean> getUserRequests() {
		return userRequests;
	}

	public void setUserRequests(List<MinnagamUserRequestValueBean> userRequests) {
		this.userRequests = userRequests;
	}

	
	public List<ViewComplaintValueBean> getMinnagamComplaint() {
		return minnagamComplaint;
	}

	public void setMinnagamComplaint(List<ViewComplaintValueBean> minnagamComplaint) {
		this.minnagamComplaint = minnagamComplaint;
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

	public LoginParams getOffice() {
		return office;
	}

	public void setOffice(LoginParams office) {
		this.office = office;
	}

	public DataModel getDm() {
		return dm;
	}

	public void setDm(DataModel dm) {
		this.dm = dm;
	}

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}
	

}

