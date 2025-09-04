package tneb.ccms.admin.controller;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tneb.ccms.admin.model.AdminUserBean;
import tneb.ccms.admin.model.CallCenterMappingBean;
import tneb.ccms.admin.model.CallCenterUserBean;
import tneb.ccms.admin.model.LoginParams;
import tneb.ccms.admin.util.CCMSConstants;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.util.PropertiesUtil;
import tneb.ccms.admin.valuebeans.AdminUserValueBean;
import tneb.ccms.admin.valuebeans.CallCenterUserValueBean;

public class Authentication {
	
	private Logger logger = LoggerFactory.getLogger(Authentication.class);

	LoginParams officer = new LoginParams();
	
	String style;
	
	
	public String callCenterAuthentication(CallCenterUserValueBean callCenterUserValueBean) throws Exception  {
		
		String opt = "associatelogin";
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<CallCenterUserBean> criteriaQuery = criteriaBuilder.createQuery(CallCenterUserBean.class);
			Root<CallCenterUserBean> root = criteriaQuery.from(CallCenterUserBean.class);
			
			criteriaQuery.select(root).where(criteriaBuilder.and(criteriaBuilder.equal(root.get("userName"), callCenterUserValueBean.getUserName()),
					criteriaBuilder.equal(root.get("password"), callCenterUserValueBean.getPassword())));

			Query<CallCenterUserBean> query = session.createQuery(criteriaQuery).setMaxResults(1);
			CallCenterUserBean callCenterUserBean = null;
			try {
				callCenterUserBean = query.getSingleResult();
			} catch (NoResultException nre) {
				System.out.println("No User Found for the given credentials.");
				style = "green";
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Invalid User Name / Passsword"));
			}

			if (callCenterUserBean != null) {
				

				FacesContext facesContext = FacesContext.getCurrentInstance();
				ExternalContext externalContext = facesContext.getExternalContext();
				//HttpSession session1 = (HttpSession) externalContext.getSession(false); // true = create if needed
				
				
				HttpSession session1 = (HttpSession) FacesContext.getCurrentInstance()
				        .getExternalContext().getSession(false);

				if (session1.getAttribute("sessionAdminValueBean") != null) {
					
				    System.err.println("Admin is already logged in — Call Center login denied");

			

				    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Already CCMS Software Screen to Opened."));
				  
//				   
//				    FacesContext.getCurrentInstance().getExternalContext()
//				        .redirect("associatelogin.xhtml");
				   // FacesContext.getCurrentInstance().responseComplete();
				   
				}
				else
				{
				CriteriaBuilder callMapCriteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<Object[]> callMapCriteriaQuery = callMapCriteriaBuilder.createQuery(Object[].class);
				Root<CallCenterMappingBean> callMapRoot = callMapCriteriaQuery.from(CallCenterMappingBean.class);

				callMapCriteriaQuery.multiselect(callMapRoot.get("circleBean").get("id")).where(callMapCriteriaBuilder.equal(callMapRoot.get("callCenterUserBean").get("id"), callCenterUserBean.getId()));

				Query<Object[]> callMapQuery = session.createQuery(callMapCriteriaQuery);
				
				List<?> rowList = callMapQuery.list();
				
				List<Integer> circleIdList = new ArrayList<Integer>();
				
				if (rowList.size() > 0) {
					for (int loop = 0; loop < rowList.size(); loop++) {
						circleIdList.add(Integer.parseInt(rowList.get(loop).toString()));
					}
				}
				
				if(circleIdList.size() == 0) {
					style = "red";
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "No Circles Mapped."));
					return opt;
				}
				
				officer.circleIdList = circleIdList;
				officer.userId = callCenterUserBean.getId();
				officer.roleId = callCenterUserBean.getCallCenterRoleBean().getId();
				officer.email = callCenterUserBean.getEmail();
				officer.mobile = callCenterUserBean.getMobile();
				officer.userName = callCenterUserBean.getUserName();
				officer.callCenterUserName = callCenterUserBean.getUserName();
				officer.officeName = callCenterUserBean.getName();
				officer.callCenterRoleId = callCenterUserBean.getCallCenterRoleBean().getId();
							
				PropertiesUtil propertyUtils = new PropertiesUtil();
				officer.imagePath =  propertyUtils.IMAGE_BASE_URL;
				
				
					System.err.println("ADMIN NOT LOGGED IN — cannot login as call center user");
				session1.setAttribute("sessionCallCenterUserValueBean", CallCenterUserValueBean.convertBeanToValueBean(callCenterUserBean));

				
				externalContext.redirect(externalContext.getRequestContextPath() + "/faces/callcenter/dashboard.xhtml");
				
				
				}
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

		return opt;
	}


	public String checkAuthentication(AdminUserValueBean adminUserValueBean) throws Exception {
		
		String opt = "login";
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<AdminUserBean> criteriaQuery = criteriaBuilder.createQuery(AdminUserBean.class);
			Root<AdminUserBean> root = criteriaQuery.from(AdminUserBean.class);
			
			criteriaQuery.select(root).where(criteriaBuilder.and(criteriaBuilder.equal(root.get("userName"), adminUserValueBean.getUserName()),
					criteriaBuilder.equal(root.get("password"), adminUserValueBean.getPassword())));

			Query<AdminUserBean> query = session.createQuery(criteriaQuery).setMaxResults(1);
			AdminUserBean adminUser = null;
			try {
				adminUser = query.getSingleResult();
			} catch (NoResultException nre) {
				System.out.println("No User Found for the given credentials.");
				style = "red";
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Invalid User Name / Passsword"));
			}

			if (adminUser != null) {
				FacesContext facesContext = FacesContext.getCurrentInstance();
				ExternalContext externalContext = facesContext.getExternalContext();
				HttpSession httpSession = (HttpSession) externalContext.getSession(false);

				if (httpSession != null && httpSession.getAttribute("sessionCallCenterUserValueBean") != null) {
					System.err.println("CALLCENTERis already logged in — ADMIN  login denied");
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Already CCMS Software Screen to Opened."));
					  
				   
				  //  FacesContext.getCurrentInstance().getExternalContext().redirect(externalContext.getRequestContextPath());
				   // FacesContext.getCurrentInstance().responseComplete();
				}
				 else
				 {
				if (adminUser.getCircleBean() != null) {
					officer.circleName = adminUser.getCircleBean().getName();
					officer.officeName = adminUser.getCircleBean().getName();
					officer.adminOfficeName = adminUser.getCircleBean().getName();
					officer.circleId = adminUser.getCircleBean().getId();
					officer.circleCode = adminUser.getCircleBean().getCode();
				}

				if (adminUser.getDivisionBean() != null) {
					officer.divisionName = adminUser.getDivisionBean().getName();
					officer.officeName = adminUser.getDivisionBean().getName();
					officer.adminOfficeName = adminUser.getDivisionBean().getName();
					officer.divisionId = adminUser.getDivisionBean().getId();
				}

				if (adminUser.getSubDivisionBean() != null) {
					officer.subDivisionName = adminUser.getSubDivisionBean().getName();
					officer.officeName = adminUser.getSubDivisionBean().getName();
					officer.adminOfficeName = adminUser.getSubDivisionBean().getName();
					officer.subDivisionId = adminUser.getSubDivisionBean().getId();
				}

				if (adminUser.getSectionBean() != null) {
					officer.sectionName = adminUser.getSectionBean().getName();
					officer.officeName = adminUser.getSectionBean().getName();
					officer.adminOfficeName = adminUser.getSectionBean().getName();
					officer.sectionId = adminUser.getSectionBean().getId();
					officer.sectionCode = adminUser.getSectionCode();
				}
				
				if (adminUser.getRegionBean() != null) {
					officer.regionId = adminUser.getRegionBean().getId();
					officer.regionName = adminUser.getRegionBean().getName();
				}

				officer.userId = adminUser.getId();
				officer.roleId = adminUser.getRoleBean().getId();
				officer.adminRoleId = adminUser.getRoleBean().getId();
				officer.email = adminUser.getEmail();
				officer.mobile = adminUser.getMobile();
				officer.userName = adminUser.getUserName();
                officer.adminUserName = adminUser.getUserName();
												
				PropertiesUtil propertyUtils = new PropertiesUtil();
				officer.imagePath =  propertyUtils.IMAGE_BASE_URL;

				switch (adminUser.getRoleBean().getId()) {
				case 1:
					officer.officeId = officer.sectionId;
					officer.fieldName = CCMSConstants.OFFICE_NAME[0];
					officer.fieldBeanName = CCMSConstants.OFFICE_BEAN_NAME[0];
					break;
				case 2:
					officer.officeId = officer.subDivisionId;
					officer.fieldName = CCMSConstants.OFFICE_NAME[1];
					officer.fieldBeanName = CCMSConstants.OFFICE_BEAN_NAME[1];
					break;
				case 3:
					officer.officeId = officer.divisionId;
					officer.fieldName = CCMSConstants.OFFICE_NAME[2];
					officer.fieldBeanName = CCMSConstants.OFFICE_BEAN_NAME[2];
					break;
				case 4:
					officer.officeId = officer.circleId;
					officer.fieldName = CCMSConstants.OFFICE_NAME[3];
					officer.fieldBeanName = CCMSConstants.OFFICE_BEAN_NAME[3];
					break;
				case 5:
				case 6:
				case 7:
				case 8:
				case 9:
					officer.officeId = officer.regionId;
					officer.fieldName = CCMSConstants.OFFICE_NAME[4];
					officer.fieldBeanName = CCMSConstants.OFFICE_BEAN_NAME[4];
					break;
				case 10:
					officer.officeId = officer.regionId;
					officer.officeCirlceId = officer.circleId;
					officer.fieldName = CCMSConstants.OFFICE_NAME[4];
					officer.fieldNameMinnagam = CCMSConstants.OFFICE_NAME_MINNAGAM[0];
					officer.fieldBeanName = CCMSConstants.OFFICE_BEAN_NAME[4];
					break;
				}

				//ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
				
				//HttpSession httpSession = (HttpSession) externalContext.getSession(false);
				
				
					 System.err.println("CALLCENTER NOT  LOGIN");
				
				httpSession.setAttribute("sessionAdminValueBean", AdminUserValueBean.convertAdminUserBeanToAdminUserValueBean(adminUser));
				httpSession.setAttribute("loggedInSectionId", officer.sectionId);
				httpSession.setAttribute("loggedInSectionName", officer.fieldName);
				httpSession.setAttribute("loggedInCircleName", officer.circleName);
				httpSession.setAttribute("loggedInCircleId", officer.circleId);
				httpSession.setAttribute("loggedInSection", officer.sectionName);
				httpSession.setAttribute("sectionCode", officer.sectionCode);
				httpSession.setAttribute("circleCode", officer.circleCode);
				httpSession.setAttribute("loginParams", officer); 
				if(officer.roleId !=10) {
					FacesContext.getCurrentInstance().getExternalContext().redirect(externalContext.getRequestContextPath()+"/faces/admin/dashboard.xhtml");
				}else {
					FacesContext.getCurrentInstance().getExternalContext().redirect(externalContext.getRequestContextPath()+"/faces/admin/dashboardminnagam.xhtml");
				}
				 }
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

		return opt;

	}
	
	
	public void checkAlreadyAdminLoggedIn() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);

        if (session != null && session.getAttribute("sessionAdminValueBean") != null) {
        	AdminUserValueBean admin = (AdminUserValueBean) session.getAttribute("sessionAdminValueBean");
        	String lastPage = (String) session.getAttribute("lastPage");
        	System.err.println("THE LAST PAGE sdsdsads---------------------------"+lastPage);
            try {
            	if (lastPage != null && !lastPage.contains("/associatelogin.xhtml")) {
            		facesContext.getExternalContext().redirect(
                            facesContext.getExternalContext().getRequestContextPath() + "/faces"+ lastPage);
            	}else {
            		if(admin.getRoleId()==10) {
            			facesContext.getExternalContext().redirect(
                                facesContext.getExternalContext().getRequestContextPath() + "/faces/admin/dashboardminnagam.xhtml");
            		}else {
            			facesContext.getExternalContext().redirect(
                                facesContext.getExternalContext().getRequestContextPath() + "/faces/admin/dashboard.xhtml");
            		}
            		

            	}
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
	public void checkAlreadyCallcenterLoggedIn() {
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);

        if (session != null && session.getAttribute("sessionCallCenterUserValueBean") != null) {
        	CallCenterUserValueBean admin = (CallCenterUserValueBean) session.getAttribute("sessionCallCenterUserValueBean");
        	String lastPage = (String) session.getAttribute("lastPage");
        	System.err.println("THE LAST PAGE sdsdsads---------------------------"+lastPage);
            try {
            	if (lastPage != null && !lastPage.contains("/associatelogin.xhtml")) {
            		facesContext.getExternalContext().redirect(
                            facesContext.getExternalContext().getRequestContextPath() + "/faces"+ lastPage);
            	}else {
            		
            			facesContext.getExternalContext().redirect(
                                facesContext.getExternalContext().getRequestContextPath() + "/faces/callcenter/dashboard.xhtml");
            		
            		

            	}
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		
	}
public String checkOldPassword(AdminUserValueBean adminUserValuBean)throws Exception {
		
		String opt="oldpassword";
		SessionFactory factory = null;
		Session session = null;
		Transaction transaction = null;
		
		 FacesContext facesContext = FacesContext.getCurrentInstance();
			String newPassword = facesContext.getExternalContext().getRequestParameterMap().get("myForm:new_password");
			String confirmPassword = facesContext.getExternalContext().getRequestParameterMap().get("myForm:confirm_password");
		try {
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<AdminUserBean> criteriaQuery = criteriaBuilder.createQuery(AdminUserBean.class);
			Root<AdminUserBean> root = criteriaQuery.from(AdminUserBean.class);
			
			String hashOldPassword = md5(adminUserValuBean.getOldPassword());
			
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("password"), hashOldPassword));
			
			Query<AdminUserBean> query = session.createQuery(criteriaQuery).setMaxResults(1);
			AdminUserBean adminUserBean = null;
			try {
				adminUserBean = query.getSingleResult();
				
				PrimeFaces.current().executeScript("PF('dlgChange').hide()");
				
				String hashNewPassword = md5(newPassword);
				String hashConfirmPassword = md5(confirmPassword);
				
				if(hashNewPassword.equals(hashConfirmPassword)) {
					adminUserBean.setPassword(hashNewPassword);
					
					transaction = session.beginTransaction();
					session.update(adminUserBean);
					transaction.commit();
					
					//FacesContext facesContext = FacesContext.getCurrentInstance();
					boolean passwordChanged = true;
					if(passwordChanged) {
						PrimeFaces.current().executeScript("PF('confirmationDialog').show()");					
						}else {
						  FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to change password"));
					}
				}
				else {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "new password and confirm not same."));
				}
				
			} catch (NoResultException nre) {
				System.out.println("No OldPassword Found for the given credentials.");
				style = "red";
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Invalid OldPasssword"));
			}
			
		}catch(Exception ex) {
			logger.error(ExceptionUtils.getStackTrace(ex));
		}finally {
			HibernateUtil.closeSession(factory, session);
		}
		
		
		return opt;
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

	public void logOut() {

		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

		try {
			HttpSession httpSession = (HttpSession) externalContext.getSession(false);
			if (httpSession != null && httpSession.getAttribute("sessionAdminValueBean") != null) {
				
	            httpSession.removeAttribute("sessionAdminValueBean");
	            httpSession.removeAttribute("lastPage");
	            //httpSession.invalidate();
	        }
			
			//FacesContext.getCurrentInstance().getExternalContext().redirect(externalContext.getRequestContextPath()+"/faces/associatelogin.xhtml");
			FacesContext.getCurrentInstance().getExternalContext().redirect(externalContext.getRequestContextPath());
		} catch (IOException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
		}
		//FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
	}
	
	public void callCenterLogOut() {

		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

		try {
			HttpSession httpSession = (HttpSession) externalContext.getSession(false);
			if (httpSession != null && httpSession.getAttribute("sessionCallCenterUserValueBean") != null) {
	            httpSession.removeAttribute("sessionCallCenterUserValueBean");
	            httpSession.removeAttribute("lastPage");
	            //httpSession.invalidate();
	        }
			FacesContext.getCurrentInstance().getExternalContext().redirect(externalContext.getRequestContextPath()+"/faces/associatelogin.xhtml");
		} catch (IOException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
		//FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
	}

	public LoginParams getOfficer() {
		return officer;
	}

	public void setOfficer(LoginParams officer) {
		this.officer = officer;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}


	
	

}
