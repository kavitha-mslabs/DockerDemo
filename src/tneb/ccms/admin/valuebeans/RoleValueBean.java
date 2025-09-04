package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.sql.Timestamp;

import tneb.ccms.admin.model.RoleBean;

public class RoleValueBean implements Serializable {
	
	private static final long serialVersionUID = 8183646978017231432L;

	private Integer id;
	
	private String code;

	private String name;

	private Timestamp createdOn;

	private Timestamp updatedOn;

	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Timestamp getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

	public Timestamp getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Timestamp updatedOn) {
		this.updatedOn = updatedOn;
	}

	public static RoleValueBean convertRoleBeanToRoleValueBean(RoleBean roles) {
		
		RoleValueBean roleValueBean = new RoleValueBean();
		
		roleValueBean.setCreatedOn(roles.getCreatedOn());
		roleValueBean.setId(roles.getId());
		roleValueBean.setCode(roles.getCode());
		roleValueBean.setName(roles.getName());
		roleValueBean.setUpdatedOn(roles.getUpdatedOn());
		
		return roleValueBean;
		
	}
	
	public static RoleBean convertRoleValueBeanToRoleBean(RoleValueBean roleValueBean) {
		
		RoleBean roles = new RoleBean();
		
		roles.setCreatedOn(roleValueBean.getCreatedOn());
		roles.setId(roleValueBean.getId());
		roles.setCode(roleValueBean.getCode());
		roles.setName(roleValueBean.getName());
		roles.setUpdatedOn(roleValueBean.getUpdatedOn());
		
		return roles;
		
	}
}
