package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.sql.Timestamp;

import tneb.ccms.admin.model.SubDivisionBean;

public class SubDivisionValueBean implements Serializable {

	private static final long serialVersionUID = -6017514642927399720L;

	private Integer id;

	private String name;

	private String code;

	private String address;

	private String phone;

	private String mobile;

	private String email;

	private Timestamp createdOn;

	private Timestamp updatedOn;

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


	public String getPhone() {
		return phone;
	}


	public void setPhone(String phone) {
		this.phone = phone;
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


	public static SubDivisionValueBean convertSubDivisionBeanToSubDivisionValueBean(SubDivisionBean subDivisionBean) {
		
		SubDivisionValueBean subDivisionValueBean = new SubDivisionValueBean();

		if(subDivisionBean.getRegionBean() != null) {
			subDivisionValueBean.setRegionId(subDivisionBean.getRegionBean().getId());
			subDivisionValueBean.setRegionName(subDivisionBean.getRegionBean().getName());
		}
		if(subDivisionBean.getCircleBean() != null) {
			subDivisionValueBean.setCircleId(subDivisionBean.getCircleBean().getId());
			subDivisionValueBean.setCircleName(subDivisionBean.getCircleBean().getName());
		}
		if(subDivisionBean.getDivisionBean() != null) {
			subDivisionValueBean.setDivisionId(subDivisionBean.getDivisionBean().getId());
			subDivisionValueBean.setDivisionName(subDivisionBean.getDivisionBean().getName());
		}
		
		subDivisionValueBean.setAddress(subDivisionBean.getAddress());
		subDivisionValueBean.setCode(subDivisionBean.getCode());
		subDivisionValueBean.setCreatedOn(subDivisionBean.getCreatedOn());
		subDivisionValueBean.setEmail(subDivisionBean.getEmail());
		subDivisionValueBean.setMobile(subDivisionBean.getMobile());
		subDivisionValueBean.setId(subDivisionBean.getId());
		subDivisionValueBean.setName(subDivisionBean.getName());
		subDivisionValueBean.setPhone(subDivisionBean.getPhone());
		subDivisionValueBean.setUpdatedOn(subDivisionBean.getUpdatedOn());
		
		return subDivisionValueBean;
	}
	
	
	public static SubDivisionBean convertSubDivisionValueBeanToSubDivisionBean(SubDivisionValueBean subDivisionValueBean) {
		
		SubDivisionBean subDivisionBean = new SubDivisionBean();
		
		subDivisionBean.setAddress(subDivisionValueBean.getAddress());
		subDivisionBean.setCode(subDivisionValueBean.getCode());
		subDivisionBean.setCreatedOn(subDivisionValueBean.getCreatedOn());
		subDivisionBean.setEmail(subDivisionValueBean.getEmail());
		subDivisionBean.setMobile(subDivisionValueBean.getMobile());
		subDivisionBean.setId(subDivisionValueBean.getId());
		subDivisionBean.setName(subDivisionValueBean.getName());
		subDivisionBean.setPhone(subDivisionValueBean.getPhone());
		subDivisionBean.setUpdatedOn(subDivisionValueBean.getUpdatedOn());
		
		return subDivisionBean;
	}

}
