package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.sql.Timestamp;

import tneb.ccms.admin.model.CallCenterUserHistoryBean;

public class CallCenterUserHistoryValueBean implements Serializable{

	private static final long serialVersionUID = 1769688405708583500L;
	
     private Integer id;
	 private String name;
     private String userName;
     private String mobileNumber;
	 private String emailId;
	 private Integer roleId;
	 private Timestamp fromDt;
	 private Timestamp toDt;
	 private String circleCode;
	 private String reason;
//	 private Timestamp entryDt;
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
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
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
	public Timestamp getFromDt() {
		return fromDt;
	}
	public void setFromDt(Timestamp fromDt) {
		this.fromDt = fromDt;
	}
	public Timestamp getToDt() {
		return toDt;
	}
	public void setToDt(Timestamp toDt) {
		this.toDt = toDt;
	}
	public String getCircleCode() {
		return circleCode;
	}
	public void setCircleCode(String circleCode) {
		this.circleCode = circleCode;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
//	public Timestamp getEntryDt() {
//		return entryDt;
//	}
//	public void setEntryDt(Timestamp entryDt) {
//		this.entryDt = entryDt;
//	}
	public static CallCenterUserHistoryValueBean convertRequestBeanToValueBean(CallCenterUserHistoryValueBean requestBean) {
		CallCenterUserHistoryValueBean valueBean = new CallCenterUserHistoryValueBean();
		valueBean.setRoleId(requestBean.roleId);
		valueBean.setName(requestBean.name);
		valueBean.setEmailId(requestBean.emailId);
		valueBean.setReason(requestBean.reason);
//		valueBean.setEntryDt(requestBean.entryDt);
		valueBean.setFromDt(requestBean.fromDt);
		valueBean.setCircleCode(requestBean.circleCode);
		valueBean.setMobileNumber(requestBean.mobileNumber);
		valueBean.setToDt(requestBean.toDt);
		valueBean.setUserName(requestBean.userName);
		return valueBean;
    }
	
	public static CallCenterUserHistoryBean convertValueBeanToRequestBean(CallCenterUserHistoryValueBean valueBean) {
		CallCenterUserHistoryBean requestBean = new CallCenterUserHistoryBean();
		
		requestBean.setCircleCode(requestBean.getCircleCode());
		requestBean.setName(requestBean.getName());
		requestBean.setEmailId(requestBean.getEmailId());
		requestBean.setMobileNumber(requestBean.getMobileNumber());
//		requestBean.setEntryDt(requestBean.getEntryDt());
		requestBean.setFromDt(requestBean.getFromDt());
		requestBean.setReason(requestBean.getReason());
		requestBean.setUserName(requestBean.getUserName());
		requestBean.setRoleId(requestBean.getRoleId());
		requestBean.setToDt(requestBean.getToDt());
		
		
		return requestBean;
     
        
        
	}
	 
}
