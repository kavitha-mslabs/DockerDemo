package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.sql.Timestamp;

import tneb.ccms.admin.model.CallCenterMappingBean;

public class CallCenterMappingValueBean implements Serializable {

	private static final long serialVersionUID = 1769688405708583500L;


	private Integer id;

	private Integer callCenterUserId;
	
	private String callCenterUserName;
	
	private Integer circleId;
	
	private String circleName;

	private Timestamp createdOn;

	private Timestamp updatedOn;   
	

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getCallCenterUserId() {
		return callCenterUserId;
	}

	public void setCallCenterUserId(Integer callCenterUserId) {
		this.callCenterUserId = callCenterUserId;
	}

	public String getCallCenterUserName() {
		return callCenterUserName;
	}

	public void setCallCenterUserName(String callCenterUserName) {
		this.callCenterUserName = callCenterUserName;
	}

	public Integer getCircleId() {
		return circleId;
	}

	public void setCircleId(Integer circleId) {
		this.circleId = circleId;
	}

	public String getCircleName() {
		return circleName;
	}

	public void setCircleName(String circleName) {
		this.circleName = circleName;
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

	public static CallCenterMappingValueBean convertCallCenterMappingBeanToCallCenterMappingValueBean(CallCenterMappingBean callCenterMappingBean) {
		
		CallCenterMappingValueBean callCenterMappingValueBean = new CallCenterMappingValueBean();
		
		callCenterMappingValueBean.setId(callCenterMappingBean.getId());
		callCenterMappingValueBean.setCreatedOn(callCenterMappingBean.getCreatedOn());
		callCenterMappingValueBean.setCallCenterUserId(callCenterMappingBean.getCallCenterUserBean().getId());
		callCenterMappingValueBean.setCallCenterUserName(callCenterMappingBean.getCallCenterUserBean().getName());
		callCenterMappingValueBean.setCircleId(callCenterMappingBean.getCircleBean().getId());
		callCenterMappingValueBean.setCircleName(callCenterMappingBean.getCircleBean().getName());
		callCenterMappingValueBean.setUpdatedOn(callCenterMappingBean.getUpdatedOn());
		
		return callCenterMappingValueBean;
	}
	
	public static CallCenterMappingBean convertCallCenterMappingValueBeanToCallCenterMappingBean(CallCenterMappingValueBean callCenterMappingValueBean) {
		
		CallCenterMappingBean callCenterMappingBean = new CallCenterMappingBean();
		
		callCenterMappingBean.setId(callCenterMappingValueBean.getId());
		callCenterMappingBean.setCreatedOn(callCenterMappingValueBean.getCreatedOn());
		callCenterMappingBean.setUpdatedOn(callCenterMappingValueBean.getUpdatedOn());
		
		return callCenterMappingBean;
	}
	
}
