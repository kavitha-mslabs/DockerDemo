package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.sql.Timestamp;

import tneb.ccms.admin.model.SectionBean;

public class SectionValueBean implements Serializable {
	
	private static final long serialVersionUID = 2942992119423029338L;

	private Integer id;

	private String name;

	private String code;

	private String address;

	private String pinCode;

	private String phoneNumber;

	private String mobile;

	private String email;

	private Timestamp createdOn;

	private Timestamp updatedOn;

	private Integer districtId;

	private Integer subDivisionId;
	
	private String subDivisionName;
	
	private Integer divisionId;
	
	private String divisionName;

	private Integer circleId;
	
	private String circleName;

	private Integer regionId;
	
	private String regionName;


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


	public Integer getDistrictId() {
		return districtId;
	}


	public void setDistrictId(Integer districtId) {
		this.districtId = districtId;
	}


	public Integer getSubDivisionId() {
		return subDivisionId;
	}


	public void setSubDivisionId(Integer subDivisionId) {
		this.subDivisionId = subDivisionId;
	}


	public String getSubDivisionName() {
		return subDivisionName;
	}


	public void setSubDivisionName(String subDivisionName) {
		this.subDivisionName = subDivisionName;
	}


	public Integer getDivisionId() {
		return divisionId;
	}


	public void setDivisionId(Integer divisionId) {
		this.divisionId = divisionId;
	}


	public String getDivisionName() {
		return divisionName;
	}


	public void setDivisionName(String divisionName) {
		this.divisionName = divisionName;
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


	public Integer getRegionId() {
		return regionId;
	}


	public void setRegionId(Integer regionId) {
		this.regionId = regionId;
	}


	public String getRegionName() {
		return regionName;
	}


	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}


	public static SectionValueBean convertSectionBeanToSectionValueBean (SectionBean sectionBean) {
		
		SectionValueBean sectionValueBean = new SectionValueBean();
		
		if(sectionBean.getRegionBean() != null) {
			sectionValueBean.setRegionId(sectionBean.getRegionBean().getId());
			sectionValueBean.setRegionName(sectionBean.getRegionBean().getName());
		}
		if(sectionBean.getCircleBean() != null) {
			sectionValueBean.setCircleId(sectionBean.getCircleBean().getId());
			sectionValueBean.setCircleName(sectionBean.getCircleBean().getName());
		}
		if(sectionBean.getDivisionBean() != null) {
			sectionValueBean.setDivisionId(sectionBean.getDivisionBean().getId());
			sectionValueBean.setDivisionName(sectionBean.getDivisionBean().getName());
		}
		if(sectionBean.getSubDivisionBean() != null) {
			sectionValueBean.setSubDivisionId(sectionBean.getSubDivisionBean().getId());
			sectionValueBean.setSubDivisionName(sectionBean.getSubDivisionBean().getName());
		}
		
		sectionValueBean.setAddress(sectionBean.getAddress());
		sectionValueBean.setCode(sectionBean.getCode());
		sectionValueBean.setCreatedOn(sectionBean.getCreatedOn());
		sectionValueBean.setEmail(sectionBean.getEmail());
		sectionValueBean.setMobile(sectionBean.getMobile());
		sectionValueBean.setId(sectionBean.getId());
		sectionValueBean.setName(sectionBean.getName());
		sectionValueBean.setPhoneNumber(sectionBean.getPhoneNumber());
		sectionValueBean.setPinCode(sectionBean.getPinCode());
		sectionValueBean.setSubDivisionName(sectionBean.getName());
		sectionValueBean.setUpdatedOn(sectionBean.getUpdatedOn());
		
		return sectionValueBean;
		
	}
	
	
	public static SectionBean convertSectionValueBeanToSectionBean (SectionValueBean sectionValueBean) {
		
		SectionBean sectionBean = new SectionBean();
		
		sectionBean.setAddress(sectionValueBean.getAddress());
		sectionBean.setCode(sectionValueBean.getCode());
		sectionBean.setCreatedOn(sectionValueBean.getCreatedOn());
		sectionBean.setEmail(sectionValueBean.getEmail());
		sectionBean.setMobile(sectionValueBean.getMobile());
		sectionBean.setId(sectionValueBean.getId());
		sectionBean.setName(sectionValueBean.getName());
		sectionBean.setPhoneNumber(sectionValueBean.getPhoneNumber());
		sectionBean.setPinCode(sectionValueBean.getPinCode());
		sectionBean.setUpdatedOn(sectionValueBean.getUpdatedOn());
		
		return sectionBean;
		
	}
}
