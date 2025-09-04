package tneb.ccms.admin.dao;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.management.relation.Role;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jsf2leaf.model.Circle;

import tneb.ccms.admin.controller.MinnagamController;
import tneb.ccms.admin.model.CallCenterRoleBean;
import tneb.ccms.admin.model.CallCenterUserBean;
import tneb.ccms.admin.model.CircleBean;
import tneb.ccms.admin.model.MinnagamUserRequestBean;
import tneb.ccms.admin.model.RoleBean;
import tneb.ccms.admin.util.CCMSConstants;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.valuebeans.MinnagamUserRequestValueBean; 

public class MinnagamUserDao {
	
	private Logger logger = LoggerFactory.getLogger(MinnagamUserDao.class);
	
	public String validateUser(String userName) {
		
		String errorMessage = null;
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<CallCenterUserBean> criteriaQuery = criteriaBuilder.createQuery(CallCenterUserBean.class);
			Root<CallCenterUserBean> root = criteriaQuery.from(CallCenterUserBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("userName"), userName));

			Query<CallCenterUserBean> query = session.createQuery(criteriaQuery);
			List<CallCenterUserBean> userList = query.getResultList();

			if (userList.size() > 0) {
				errorMessage = "User already registered with this user name.";
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		
		return errorMessage;
	}
	
	public void saveUserRequest(MinnagamUserRequestValueBean minnagamValueBean) {
		
		SessionFactory factory = null;
		Session session = null;
		Transaction transaction = null;
		
		try {
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			MinnagamUserRequestBean minnagamBean = new MinnagamUserRequestBean();
			
			Timestamp createdOn = new Timestamp(new Date().getTime());
			
			if(minnagamValueBean!=null) {
				
				minnagamBean.setAgentRole(minnagamValueBean.getAgentRole());
				
				if ("Circle Agent".equals(minnagamValueBean.getAgentRole())) {
		             
					int circleId = minnagamValueBean.getCircleId();
					
					 CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				     CriteriaQuery<CircleBean> criteriaQuery = criteriaBuilder.createQuery(CircleBean.class);
				     Root<CircleBean> root = criteriaQuery.from(CircleBean.class);
				        
				     criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), circleId));
				        
				     CircleBean circle = session.createQuery(criteriaQuery).uniqueResult();
				     
				     String circleCode = circle.getCode();
					
//				     String lastUserName = (String) session.createQuery("SELECT userName FROM MinnagamUserRequestBean WHERE userName LIKE 'c%' AND userName NOT LIKE 'ca%' ORDER BY id DESC")
//		                        .setMaxResults(1)
//		                        .uniqueResult();
              
				     //String newUserName = generateNextUserName(lastUserName, circleCode);
		            // minnagamBean.setUserName(newUserName);
		              
		              CircleBean circleEntity = new CircleBean();
		              circleEntity.setId(minnagamValueBean.getCircleId());
		              minnagamBean.setCircleBean(circleEntity);
		               
		              CallCenterRoleBean callCenterRoleBean = new CallCenterRoleBean();
		              callCenterRoleBean.setId(7); 
		              minnagamBean.setRoleBean(callCenterRoleBean);
              
		              //minnagamValueBean.setPassword("12345");
						
//			            String hashedPassword = md5(minnagamValueBean.getPassword());
//			            minnagamBean.setPassword(hashedPassword);
						
						minnagamBean.setName(minnagamValueBean.getName());
						
						minnagamBean.setMobileNumber(minnagamValueBean.getMobileNumber());
						
						minnagamBean.setEmailId(minnagamValueBean.getEmailId());
						
						minnagamBean.setCreatedOn(createdOn);
						
						//minnagamBean.setUpdatedOn(createdOn);
						
						minnagamBean.setStatusId(CCMSConstants.PENDING);
						
						minnagamBean.setCircleCode(circleCode);
						
		              
		       }else {
				
//				String lastUserName = (String) session.createQuery("SELECT userName FROM MinnagamUserRequestBean WHERE userName LIKE 'a%' ORDER BY id DESC")
//                          .setMaxResults(1).uniqueResult();
				
//				String newUserName = generateNextUserName(lastUserName);
//				minnagamBean.setUserName(newUserName);
		
				 CallCenterRoleBean callCenterRoleBean = new CallCenterRoleBean();
	              callCenterRoleBean.setId(6); 
	              minnagamBean.setRoleBean(callCenterRoleBean);
	              
	            //  minnagamValueBean.setPassword("12345");
					
//		            String hashedPassword = md5(minnagamValueBean.getPassword());
//		            minnagamBean.setPassword(hashedPassword);
					
					minnagamBean.setName(minnagamValueBean.getName());
					
					minnagamBean.setMobileNumber(minnagamValueBean.getMobileNumber());
					
					minnagamBean.setEmailId(minnagamValueBean.getEmailId());
					
					minnagamBean.setCreatedOn(createdOn);
					
					//minnagamBean.setUpdatedOn(createdOn);
					
					minnagamBean.setStatusId(CCMSConstants.PENDING);
					
		       }
				
				
					
				transaction = session.beginTransaction();
				session.save(minnagamBean);
				transaction.commit();
				session.refresh(minnagamBean);
					
//				FacesContext.getCurrentInstance().addMessage(null,
//				new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "User request sent successfully."));
//				
				PrimeFaces.current().executeScript("PF('messageDialog').show(); setTimeout(function() { PF('messageDialog').hide(); }, 3000);");
  	  		    
				PrimeFaces.current().executeScript("setTimeout(function(){ window.location.href = '" +
					    FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() +
					    "/faces/admin/addMinnagamUser.xhtml'; }, 2000);");
				
			} else {
//				FacesContext.getCurrentInstance().addMessage(null,
//				new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Not created."));
				PrimeFaces.current().executeScript("PF('messageDialogError').show(); setTimeout(function() { PF('messageDialogError').hide(); }, 3000);");
	  	  		   
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
				}
			}
			
		private String generateNextUserName(String lastUserName, String circleCode) {
		   
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

	
		private String generateNextUserName(String lastUserName) {
			
			if (lastUserName == null || lastUserName.isEmpty()) {
				return "a001"; 
			}
			
			int lastNumber = Integer.parseInt(lastUserName.substring(1));
			String nextUserName = String.format("a%03d", lastNumber + 1);
			
			return nextUserName;
		}
		
		private String generateNextCircleUserName(String lastUserName) {
			
			if (lastUserName == null || lastUserName.isEmpty()) {
				return "ca001"; 
			}
			
			int lastNumber = Integer.parseInt(lastUserName.substring(2));
			String nextUserName = String.format("ca%03d", lastNumber + 1);
			
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
}


