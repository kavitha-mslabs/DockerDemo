package tneb.ccms.admin.controller;

import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tneb.ccms.admin.AdminMain;
import tneb.ccms.admin.dao.RoleDao;
import tneb.ccms.admin.dao.UserDao;
import tneb.ccms.admin.valuebeans.AdminUserValueBean;
import tneb.ccms.admin.valuebeans.RoleValueBean;

public class UserController {

	private Logger logger = LoggerFactory.getLogger(UserController.class.getName());

	@ManagedProperty("#{admin}")
	AdminMain admin;

	private AdminUserValueBean adminUserValueBean;
	private List<RoleValueBean> roleList;

	public UserController() {
		super();
		init();
	}

	public void init() {
		admin = (AdminMain) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("admin");
	}

	public void editUser(AdminUserValueBean userValueBean) {
		
		try {
			UserDao userDao = new UserDao();
			
			adminUserValueBean = userDao.getUser(userValueBean.getId());

			RoleDao dao = new RoleDao();

			roleList = dao.getAllRoles();
			
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			
			FacesContext.getCurrentInstance().getExternalContext().redirect(externalContext.getRequestContextPath()+"/faces/admin/manage/updateuser.xhtml");
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getLocalizedMessage()));
		}
		
	}

	public String updateUser(AdminUserValueBean userValueBean) {

		String path = "admin/manage/updateuser";
		
		try {
			UserDao dao = new UserDao();

			dao.updateUser(userValueBean);

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "User updated successfully."));


		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getLocalizedMessage()));
		}

		return path;
	}
	
	public String updateUserPassword(AdminUserValueBean userValueBean) {

		String path = "admin/manage/updateuser";
		
		try {
			UserDao dao = new UserDao();

			dao.updateUserPassword(userValueBean);

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "User password updated successfully."));
			
			adminUserValueBean.setPassword("");

		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getLocalizedMessage()));
		}
		return path;
	}


	public List<RoleValueBean> getRoleList() {
		return roleList;
	}

	public void setRoleList(List<RoleValueBean> roleList) {
		this.roleList = roleList;
	}

	public AdminUserValueBean getAdminUserValueBean() {
		return adminUserValueBean;
	}

	public void setAdminUserValueBean(AdminUserValueBean adminUserValueBean) {
		this.adminUserValueBean = adminUserValueBean;
	}

}
