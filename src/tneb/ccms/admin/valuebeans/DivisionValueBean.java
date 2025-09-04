package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.sql.Timestamp;

import tneb.ccms.admin.model.DivisionBean;

public class DivisionValueBean implements Serializable {

	private static final long serialVersionUID = 8491474025719745709L;

	private Integer id;

	private Integer regionId;
	
	private Integer circleId;

	private String name;
	
	private String code;

	private String address;
	
	private String pinCode;
	
	private String phoneNumber;
	
	private String mobile;
	
	private String email;

	private Timestamp createdOn;
	
	private Timestamp updatedOn;


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public Integer getRegionId() {
		return regionId;
	}


	public void setRegionId(Integer regionId) {
		this.regionId = regionId;
	}


	public Integer getCircleId() {
		return circleId;
	}


	public void setCircleId(Integer circleId) {
		this.circleId = circleId;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getCode() {
		return code;
	}


	public void setCode(String code) {
		this.code = code;
	}


	public String getAddress() {
		return address;
	}


	public void setAddress(String address) {
		this.address = address;
	}


	public String getPinCode() {
		return pinCode;
	}


	public void setPinCode(String pinCode) {
		this.pinCode = pinCode;
	}


	public String getPhoneNumber() {
		return phoneNumber;
	}


	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}


	public String getMobile() {
		return mobile;
	}


	public void setMobile(String mobile) {
		this.mobile = mobile;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
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


public static DivisionValueBean convertDivisionBeanToDivisionValueBean(DivisionBean divisionBean) {
		
		DivisionValueBean divisionValueBean = new DivisionValueBean();
		
		if(divisionBean.getRegionBean() != null) {
			divisionValueBean.setRegionId(divisionBean.getRegionBean().getId());
		}
		if(divisionBean.getCircleBean() != null) {
			divisionValueBean.setCircleId(divisionBean.getCircleBean().getId());
		}
		
		divisionValueBean.setAddress(divisionBean.getAddress());
		divisionValueBean.setCode(divisionBean.getCode());
		divisionValueBean.setCreatedOn(divisionBean.getCreatedOn());
		divisionValueBean.setEmail(divisionBean.getEmail());
		divisionValueBean.setMobile(divisionBean.getMobile());
		divisionValueBean.setId(divisionBean.getId());
		divisionValueBean.setName(divisionBean.getName());
		divisionValueBean.setPhoneNumber(divisionBean.getPhoneNumber());
		divisionValueBean.setUpdatedOn(divisionBean.getUpdatedOn());
		
		return divisionValueBean;
	}
	
	
	public static DivisionBean convertDivisionValueBeanToDivisionBean(DivisionValueBean divisionValueBean) {
		
		DivisionBean divisionBean = new DivisionBean();
		
		divisionBean.setAddress(divisionValueBean.getAddress());
		divisionBean.setCode(divisionValueBean.getCode());
		divisionBean.setCreatedOn(divisionValueBean.getCreatedOn());
		divisionBean.setEmail(divisionValueBean.getEmail());
		divisionBean.setMobile(divisionValueBean.getMobile());
		divisionBean.setId(divisionValueBean.getId());
		divisionBean.setName(divisionValueBean.getName());
		divisionBean.setPhoneNumber(divisionValueBean.getPhoneNumber());
		divisionBean.setUpdatedOn(divisionValueBean.getUpdatedOn());
		
		return divisionBean;
	}

}
