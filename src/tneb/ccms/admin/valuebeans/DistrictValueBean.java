package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.sql.Timestamp;

import tneb.ccms.admin.model.DistrictBean;

public class DistrictValueBean implements Serializable {

	private static final long serialVersionUID = 2425282252949575484L;

	private Integer id;

	private String code;
	
	private String name;
	
	private Integer circleId;
	
	private String circleCode;
	
	private String circleName;
	
	private String sectionCode;
	
	private String sectionName;

	private Timestamp createdOn;

	private Timestamp updatedOn;
	
	private Integer regionId;
	
	private String regionName;
	
	
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

	public String getCircleCode() {
		return circleCode;
	}

	public void setCircleCode(String circleCode) {
		this.circleCode = circleCode;
	}

	public String getCircleName() {
		return circleName;
	}

	public void setCircleName(String circleName) {
		this.circleName = circleName;
	}

	public String getSectionCode() {
		return sectionCode;
	}

	public void setSectionCode(String sectionCode) {
		this.sectionCode = sectionCode;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
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
	
	public Integer getCircleId() {
		return circleId;
	}

	public void setCircleId(Integer circleId) {
		this.circleId = circleId;
	}

	public static DistrictValueBean convertDistrictBeanToDistrictValueBean(DistrictBean districtBean) {
		
		DistrictValueBean districtValueBean = new DistrictValueBean();
		
		districtValueBean.setId(districtBean.getId());
		districtValueBean.setName(districtBean.getName());
		districtValueBean.setCode(districtBean.getCode());
		districtValueBean.setCircleCode(districtBean.getCircleCode());
		districtValueBean.setCircleName(districtBean.getCircleName());
		districtValueBean.setSectionCode(districtBean.getSectionCode());
		districtValueBean.setSectionName(districtBean.getSectionName());
		districtValueBean.setUpdatedOn(districtBean.getUpdatedOn());
		districtValueBean.setCreatedOn(districtBean.getCreatedOn());
		districtValueBean.setRegionId(districtBean.getRegionBean().getId());
		districtValueBean.setRegionName(districtBean.getRegionBean().getName());
		districtValueBean.setCircleId(districtBean.getCircleBean().getId());
		
		return districtValueBean;
	}
	
	public static DistrictBean convertDistrictValueBeanToDistrictBean(DistrictValueBean districtValueBean) {
		
		DistrictBean districtBean = new DistrictBean();
		
		districtBean.setId(districtValueBean.getId());
		districtBean.setName(districtValueBean.getName());
		districtBean.setCode(districtValueBean.getCode());
		districtBean.setCircleCode(districtValueBean.getCircleCode());
		districtBean.setCircleName(districtValueBean.getCircleName());
		districtBean.setSectionCode(districtValueBean.getSectionCode());
		districtBean.setSectionName(districtValueBean.getSectionName());
		districtBean.setUpdatedOn(districtValueBean.getUpdatedOn());
		districtBean.setCreatedOn(districtValueBean.getCreatedOn());
		
		return districtBean;
	}
}
