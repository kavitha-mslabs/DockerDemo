package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.sql.Timestamp;

import tneb.ccms.admin.model.MinnagamUserRequestBean;

public class MinnagamUserRequestValueBean implements Serializable{
	
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Integer id;

	String agentRole;
	
	 Integer circleId;
	
	 String userName;

	 String password;
	
	 String name;
	
	 String mobileNumber;
	
	 String emailId;
	
	 Integer roleId;
	
	 Integer statusId;
	 
	 Timestamp createdOn;
	 
	 Timestamp updatedOn;
	 
	 String circleCode;
	 
	 String circleName;
	 
	 
	 

	public Timestamp getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Timestamp updatedOn) {
		this.updatedOn = updatedOn;
	}

	public String getCircleName() {
		return circleName;
	}

	public void setCircleName(String circleName) {
		this.circleName = circleName;
	}

	public String getCircleCode() {
		return circleCode;
	}

	public void setCircleCode(String circleCode) {
		this.circleCode = circleCode;
	}

	public String getAgentRole() {
		return agentRole;
	}

	public void setAgentRole(String agentRole) {
		this.agentRole = agentRole;
	}

	

	public Integer getCircleId() {
		return circleId;
	}

	public void setCircleId(Integer circleId) {
		this.circleId = circleId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public Integer getStatusId() {
		return statusId;
	}

	public void setStatusId(Integer statusId) {
		this.statusId = statusId;
	}
	
	 public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Timestamp getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

	public static MinnagamUserRequestValueBean convertRequestBeanToValueBean(MinnagamUserRequestBean requestBean) {
	        MinnagamUserRequestValueBean valueBean = new MinnagamUserRequestValueBean();
	        
	        valueBean.setId(requestBean.getId());
	        valueBean.setAgentRole(requestBean.getAgentRole());
	        valueBean.setCircleId(requestBean.getCircleBean() != null ? requestBean.getCircleBean().getId() : null);
	        valueBean.setUserName(requestBean.getUserName());
	        valueBean.setPassword(requestBean.getPassword());
	        valueBean.setName(requestBean.getName());
	        valueBean.setMobileNumber(requestBean.getMobileNumber());
	        valueBean.setEmailId(requestBean.getEmailId());
	        valueBean.setRoleId(requestBean.getRoleBean() != null ? requestBean.getRoleBean().getId() : null);
	        valueBean.setStatusId(requestBean.getStatusId());
	        valueBean.setCreatedOn(requestBean.getCreatedOn());
	        valueBean.setCircleCode(requestBean.getCircleCode());
	        valueBean.setUpdatedOn(requestBean.getUpdatedOn());
	        
	        return valueBean;
	    }

	    // Convert MinnagamUserRequestValueBean to MinnagamUserRequestBean
	    public static MinnagamUserRequestBean convertValueBeanToRequestBean(MinnagamUserRequestValueBean valueBean) {
	        MinnagamUserRequestBean requestBean = new MinnagamUserRequestBean();
	        
	        requestBean.setId(valueBean.getId());
	        requestBean.setAgentRole(valueBean.getAgentRole());
	        // Assuming CircleBean, CallCenterRoleBean, and RoleBean can be set manually or fetched based on their IDs.
	        // Example: requestBean.setCircleBean(fetchCircleById(valueBean.getCircleId()));
	        // For simplicity, I'm just setting null here.
	        requestBean.setCircleBean(null); // Fetch and set CircleBean using valueBean.getCircleId()
	        requestBean.setUserName(valueBean.getUserName());
	        requestBean.setPassword(valueBean.getPassword());
	        requestBean.setName(valueBean.getName());
	        requestBean.setMobileNumber(valueBean.getMobileNumber());
	        requestBean.setEmailId(valueBean.getEmailId());
	        requestBean.setRoleBean(null); // Fetch and set RoleBean using valueBean.getRoleId()
	        requestBean.setStatusId(valueBean.getStatusId());
	        requestBean.setCreatedOn(valueBean.getCreatedOn());
	        requestBean.setUpdatedOn(valueBean.getUpdatedOn());
	        
	        return requestBean;
}
	    
}
