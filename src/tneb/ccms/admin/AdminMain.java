package tneb.ccms.admin;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.primefaces.PrimeFaces;

import tneb.ccms.admin.controller.Authentication;
import tneb.ccms.admin.model.AdminUserBean;
import tneb.ccms.admin.model.CallCenterUserBean;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.valuebeans.AdminUserValueBean;
import tneb.ccms.admin.valuebeans.CallCenterUserValueBean;

public class AdminMain {
	CallCenterUserValueBean callCenterUser = new CallCenterUserValueBean();
	AdminUserValueBean officer = new AdminUserValueBean();
	Authentication auth;
	
	public AdminMain() {
		super();
		auth = new Authentication();
		
       //call();
	}


	public void selectedMenu(String option) {
		if (option.equals("P")) {
			PrimeFaces.current().executeScript("PF('dlgProfile').show()");
		}  else {
			if (option.equals("CL")) {
				auth.callCenterLogOut();
			} else {
				auth.logOut();
			} 
		}
	}
	
	public void selectedMenuReport(String option){	
		if(option.equals("R")) {
			redirectToPage("/faces/callcenter/reportSm.xhtml");
		}
		else {
			if(option.equals("D")) {
				redirectToPage("/faces/callcenter/reportSmDivision.xhtml");
			}else {
				if(option.equals("L")) {
					redirectToPage("/faces/callcenter/reportTag.xhtml");
				}else {
					auth.callCenterLogOut();
				}
				
			}
		}
	}
	public void selectedAdminReport(String option){	
		if(option.equals("R")) {
			redirectToPage("/faces/admin/detailreport.xhtml");
		}
		else {
			if(option.equals("D")) {
				redirectToPage("/faces/admin/abstract.xhtml");
			}else {
				auth.logOut();
				
			}
		}
	}
	public void redirectToPage(String page) {
		try {
			 FacesContext.getCurrentInstance().getExternalContext().redirect(
		                FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + page
		            );
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public void selectedMenuPassword(String option) {
		if (option.equals("P")) {
			System.out.println("Change Password selected"); 
			PrimeFaces.current().executeScript("PF('dlgChange').show()");
		}  else {
				if (option.equals("CL")) {
					auth.callCenterLogOut();
				} else {
					auth.logOut();
				}
		}
	}
	
	public void selectedMenuPasswordForCallCenter(String option) {
		if (option.equals("CC")) {
			System.out.println("Change Password selected for Call Center"); 
			PrimeFaces.current().executeScript("PF('dlgChange').show()");
		} 
		
	}
	
	
	
	public String checkOldPassword() throws Exception {
	    String opt = "oldpassword";
	    SessionFactory factory = null;
	    Session session = null;
	    Transaction transaction = null;
	    
	    try {
	        factory = HibernateUtil.getSessionFactory();
	        session = factory.openSession();
	        transaction = session.beginTransaction();
	        
	        String oldPassword = officer.getOldPassword();
	        String newPassword = officer.getNewPassword();
	        String confirmPassword = officer.getConfirmPassword();

	        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
	        CriteriaQuery<AdminUserBean> criteriaQuery = criteriaBuilder.createQuery(AdminUserBean.class);
	        Root<AdminUserBean> root = criteriaQuery.from(AdminUserBean.class);
	        
	        String hashOldPassword = md5(oldPassword);
	        
	        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("password"), hashOldPassword));
	        
	        AdminUserBean adminUserBean = session.createQuery(criteriaQuery).setMaxResults(1).getSingleResult();
	        
	        String hashNewPassword = md5(newPassword);
	        String hashConfirmPassword = md5(confirmPassword);
	        
	        if(hashNewPassword.equals(hashConfirmPassword)) {
	            adminUserBean.setPassword(hashNewPassword);
	            session.update(adminUserBean);
	            session.flush(); 
	            
	            transaction.commit();
	            
	            officer.setOldPassword(null);
	            officer.setNewPassword(null);
	            officer.setConfirmPassword(null);
	            
	            
	            PrimeFaces.current().executeScript(
	            	    "PF('resetSuccessDialog').show();" +
	            	    "setTimeout(function(){ window.location = '" + 
	            	    FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "'; }, 2000);"
	            	);
	            
	            opt = "success";
	        }
	        else {
	            FacesContext.getCurrentInstance().addMessage(null, 
	                new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Passwords don't match"));
	        }
	        
	    } catch (NoResultException nre) {
	        if (transaction != null) transaction.rollback();
	        FacesContext.getCurrentInstance().addMessage(null, 
	            new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Invalid Old Password"));
	    } catch(Exception ex) {
	        if (transaction != null) transaction.rollback();
	        FacesContext.getCurrentInstance().addMessage(null, 
	            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Password change failed"));
	    } finally {
	        if (session != null && session.isOpen()) {
	            session.close();
	        }
	    }
	    
	    return opt;
	}
	
	public String checkOldPasswordForCallCenter() throws Exception {
	    String opt = "oldpassword";
	    SessionFactory factory = null;
	    Session session = null;
	    Transaction transaction = null;
	    
	    try {
	        factory = HibernateUtil.getSessionFactory();
	        session = factory.openSession();
	        transaction = session.beginTransaction();
	        
	        String oldPassword = callCenterUser.getOldPassword();
	        String newPassword = callCenterUser.getNewPassword();
	        String confirmPassword = callCenterUser.getConfirmPassword();

	        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
	        CriteriaQuery<CallCenterUserBean> criteriaQuery = criteriaBuilder.createQuery(CallCenterUserBean.class);
	        Root<CallCenterUserBean> root = criteriaQuery.from(CallCenterUserBean.class);
	        
	        String hashOldPassword = md5(oldPassword);
	        
	        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("password"), hashOldPassword), criteriaBuilder.equal(root.get("userName"), callCenterUser.getUserName()));
	        
	        CallCenterUserBean callCenterUser = session.createQuery(criteriaQuery).setMaxResults(1).getSingleResult();
	        
	        String hashNewPassword = md5(newPassword);
	        String hashConfirmPassword = md5(confirmPassword);
	        
	        if(hashNewPassword.equals(hashConfirmPassword)) {
	        	callCenterUser.setPassword(hashNewPassword);
	            session.update(callCenterUser);
	            session.flush(); 
	            
	            transaction.commit();
	            
	            officer.setOldPassword(null);
	            officer.setNewPassword(null);
	            officer.setConfirmPassword(null);
	            
	            PrimeFaces.current().executeScript(
	            	    "PF('resetSuccessDialog').show();" +
	            	    "setTimeout(function(){ window.location = '" + 
	            	    FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/faces/associatelogin.xhtml'; }, 2000);"
	            	);
	            opt = "success";
	        }
	        else {
	            FacesContext.getCurrentInstance().addMessage(null, 
	                new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Passwords don't match"));
	        }
	        
	    } catch (NoResultException nre) {
	        if (transaction != null) transaction.rollback();
	        FacesContext.getCurrentInstance().addMessage(null, 
	            new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Invalid Old Password"));
	    } catch(Exception ex) {
	        if (transaction != null) transaction.rollback();
	        FacesContext.getCurrentInstance().addMessage(null, 
	            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Password change failed"));
	    } finally {
	        if (session != null && session.isOpen()) {
	            session.close();
	        }
	    }
	    
	    return opt;
	}
	
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
	
	

	public AdminUserValueBean getOfficer() {
		return officer;
	}

	public void setOfficer(AdminUserValueBean officer) {
		this.officer = officer;
	}

	public Authentication getAuth() {
		return auth;
	}

	public void setAuth(Authentication auth) {
		this.auth = auth;
	}

	public CallCenterUserValueBean getCallCenterUser() {
		return callCenterUser;
	}

	public void setCallCenterUser(CallCenterUserValueBean callCenterUser) {
		this.callCenterUser = callCenterUser;
	}

}
