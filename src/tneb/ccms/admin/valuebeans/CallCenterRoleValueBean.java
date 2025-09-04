package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.sql.Timestamp;

import tneb.ccms.admin.model.CallCenterRoleBean;

public class CallCenterRoleValueBean implements Serializable {
	
	private static final long serialVersionUID = 8183646978017231432L;

	private Integer id;
	
	private String name;

	private Timestamp createdOn;

	private Timestamp updatedOn;

	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public static CallCenterRoleValueBean convertCallCenterRoleBeanToCallCenterRoleValueBean(CallCenterRoleBean callCenterRoleBean) {
		
		CallCenterRoleValueBean callCenterRoleValueBean = new CallCenterRoleValueBean();
		
		callCenterRoleValueBean.setCreatedOn(callCenterRoleBean.getCreatedOn());
		callCenterRoleValueBean.setId(callCenterRoleBean.getId());
		callCenterRoleValueBean.setName(callCenterRoleBean.getName());
		callCenterRoleValueBean.setUpdatedOn(callCenterRoleBean.getUpdatedOn());
		
		return callCenterRoleValueBean;
		
	}
	
	public static CallCenterRoleBean convertCallCenterRoleValueBeanToCallCenterRoleBean(CallCenterRoleValueBean callCenterRoleValueBean) {
		
		CallCenterRoleBean callCenterRoleBean = new CallCenterRoleBean();
		
		callCenterRoleBean.setCreatedOn(callCenterRoleValueBean.getCreatedOn());
		callCenterRoleBean.setId(callCenterRoleValueBean.getId());
		callCenterRoleBean.setName(callCenterRoleValueBean.getName());
		callCenterRoleBean.setUpdatedOn(callCenterRoleValueBean.getUpdatedOn());
		
		return callCenterRoleBean;
		
	}
}
