package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.sql.Timestamp;

import tneb.ccms.admin.model.FieldWorkerBean;

public class FieldWorkerValueBean implements Serializable {

	private static final long serialVersionUID = 1769688405708583500L;

	private Integer id;
	
	private String name;
	
	private String mobile;
	
	private String staffCode;
	
	private String staffRole;
	
	private Integer regionId;
	
	private Integer sectionId;
	
	private Timestamp createdOn;

	private Timestamp updatedOn;
	
	private String regionName;
	
	private String sectionName;
	
	
	
	
	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

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

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public Integer getRegionId() {
		return regionId;
	}

	public void setRegionId(Integer regionId) {
		this.regionId = regionId;
	}

	public Integer getSectionId() {
		return sectionId;
	}

	public void setSectionId(Integer sectionId) {
		this.sectionId = sectionId;
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
	

	public String getStaffCode() {
		return staffCode;
	}

	public void setStaffCode(String staffCode) {
		this.staffCode = staffCode;
	}

	public String getStaffRole() {
		return staffRole;
	}

	public void setStaffRole(String staffRole) {
		this.staffRole = staffRole;
	}

	public static FieldWorkerValueBean convertBeanToValueBean(FieldWorkerBean fieldWorkerBean) {
		
		FieldWorkerValueBean fieldWorkerValueBean = new FieldWorkerValueBean();
		
		fieldWorkerValueBean.setId(fieldWorkerBean.getId());
		fieldWorkerValueBean.setCreatedOn(fieldWorkerBean.getCreatedOn());
		fieldWorkerValueBean.setName(fieldWorkerBean.getName());
		fieldWorkerValueBean.setMobile(fieldWorkerBean.getMobile());
		fieldWorkerValueBean.setUpdatedOn(fieldWorkerBean.getUpdatedOn());
		fieldWorkerValueBean.setSectionId(fieldWorkerBean.getSectionBean().getId());
		fieldWorkerValueBean.setSectionName(fieldWorkerBean.getSectionBean().getName());
		fieldWorkerValueBean.setRegionId(fieldWorkerBean.getRegionBean().getId());
		fieldWorkerValueBean.setRegionName(fieldWorkerBean.getRegionBean().getName());
		fieldWorkerValueBean.setStaffCode(fieldWorkerBean.getStaffCode());
		fieldWorkerValueBean.setStaffRole(fieldWorkerBean.getStaffRole());
		
		return fieldWorkerValueBean;
	}
	
	public static FieldWorkerBean convertValueBeanToBean(FieldWorkerValueBean fieldWorkerValueBean) {
		
		FieldWorkerBean fieldWorkerBean = new FieldWorkerBean();
		
		fieldWorkerBean.setId(fieldWorkerValueBean.getId());
		fieldWorkerBean.setCreatedOn(fieldWorkerValueBean.getCreatedOn());
		fieldWorkerBean.setName(fieldWorkerValueBean.getName());
		fieldWorkerBean.setMobile(fieldWorkerValueBean.getMobile());
		fieldWorkerBean.setUpdatedOn(fieldWorkerValueBean.getUpdatedOn());
		fieldWorkerBean.setStaffCode(fieldWorkerValueBean.getStaffCode());
		fieldWorkerBean.setStaffRole(fieldWorkerValueBean.getStaffRole());
		
		return fieldWorkerBean;
	}
	
}
