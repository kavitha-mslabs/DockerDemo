package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.sql.Timestamp;

import tneb.ccms.admin.model.ConsumerServiceBean;

public class ConsumerServiceValueBean implements Serializable {

	private static final long serialVersionUID = -2092646017900323696L;

	private Integer id;
	
	private String consumerNumber;

	private String serviceNumber;

	private String serviceTariff;

	private String serviceName;

	private String serviceAddress;

	private String landmark;

	private String mobile;

	private Integer userId;

	private Integer regionId;

	private String regionName;

	private Integer circleId;

	private String circleName;

	private Integer divisionId;

	private String divisionName;

	private Integer subDivisionId;

	private String subDivisionName;

	private Integer sectionId;

	private String sectionName;

	private Timestamp createdOn;

	private Timestamp updatedOn;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	

	public String getConsumerNumber() {
		return consumerNumber;
	}

	public void setConsumerNumber(String consumerNumber) {
		this.consumerNumber = consumerNumber;
	}

	public String getServiceNumber() {
		return serviceNumber;
	}

	public void setServiceNumber(String serviceNumber) {
		this.serviceNumber = serviceNumber;
	}

	public String getServiceTariff() {
		return serviceTariff;
	}

	public void setServiceTariff(String serviceTariff) {
		this.serviceTariff = serviceTariff;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceAddress() {
		return serviceAddress;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

	public String getLandmark() {
		return landmark;
	}

	public void setLandmark(String landmark) {
		this.landmark = landmark;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
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

	public Integer getSectionId() {
		return sectionId;
	}

	public void setSectionId(Integer sectionId) {
		this.sectionId = sectionId;
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

	public static ConsumerServiceValueBean convertConsumerServiceBeanToConsumerServiceValueBean(ConsumerServiceBean consumerServiceBean) {
		
		ConsumerServiceValueBean consumerServiceValueBean = new ConsumerServiceValueBean();
		
		consumerServiceValueBean.setCreatedOn(consumerServiceBean.getCreatedOn());
		consumerServiceValueBean.setId(consumerServiceBean.getId());
		consumerServiceValueBean.setLandmark(consumerServiceBean.getLandmark());
		consumerServiceValueBean.setMobile(consumerServiceBean.getMobile());
		consumerServiceValueBean.setServiceAddress(consumerServiceBean.getServiceAddress());
		consumerServiceValueBean.setServiceName(consumerServiceBean.getServiceName());
		consumerServiceValueBean.setServiceNumber(consumerServiceBean.getServiceNumber());
		consumerServiceValueBean.setServiceTariff(consumerServiceBean.getServiceTariff());
		consumerServiceValueBean.setUpdatedOn(consumerServiceBean.getUpdatedOn());
		
		if(consumerServiceBean.getRegionBean() != null) {
			consumerServiceValueBean.setRegionId(consumerServiceBean.getRegionBean().getId());
			consumerServiceValueBean.setRegionName(consumerServiceBean.getRegionBean().getName());
		}
		if(consumerServiceBean.getCircleBean() != null) {
			consumerServiceValueBean.setCircleId(consumerServiceBean.getCircleBean().getId());
			consumerServiceValueBean.setCircleName(consumerServiceBean.getCircleBean().getName());
		}
		if(consumerServiceBean.getDivisionBean() != null) {
			consumerServiceValueBean.setDivisionId(consumerServiceBean.getDivisionBean().getId());
			consumerServiceValueBean.setDivisionName(consumerServiceBean.getDivisionBean().getName());
		}
		if(consumerServiceBean.getSubDivisionBean() != null) {
			consumerServiceValueBean.setSubDivisionId(consumerServiceBean.getSubDivisionBean().getId());
			consumerServiceValueBean.setSubDivisionName(consumerServiceBean.getSubDivisionBean().getName());
		} else {
			consumerServiceValueBean.setSubDivisionId(0);
			consumerServiceValueBean.setSubDivisionName("N/A");
		}
		if(consumerServiceBean.getSectionBean() != null) {
			consumerServiceValueBean.setSectionId(consumerServiceBean.getSectionBean().getId());
			consumerServiceValueBean.setSectionName(consumerServiceBean.getSectionBean().getName());
		} else {
			consumerServiceValueBean.setSectionId(0);
			consumerServiceValueBean.setSectionName("N/A");
		}
		if(consumerServiceBean.getPublicUserBean() != null) {
			consumerServiceValueBean.setUserId(consumerServiceBean.getPublicUserBean().getId());
		}
		
		return consumerServiceValueBean;
		
	}
	
	
	public static ConsumerServiceBean convertConsumerServiceValueBeanToConsumerServiceBean(ConsumerServiceValueBean consumerServiceValueBean) {
		
		ConsumerServiceBean consumerServiceBean = new ConsumerServiceBean();
		
		consumerServiceBean.setCreatedOn(consumerServiceValueBean.getCreatedOn());
		consumerServiceBean.setId(consumerServiceValueBean.getId());
		consumerServiceBean.setLandmark(consumerServiceValueBean.getLandmark());
		consumerServiceBean.setMobile(consumerServiceValueBean.getMobile());
		consumerServiceBean.setServiceAddress(consumerServiceValueBean.getServiceAddress());
		consumerServiceBean.setServiceName(consumerServiceValueBean.getServiceName());
		consumerServiceBean.setServiceNumber(consumerServiceValueBean.getServiceNumber());
		consumerServiceBean.setServiceTariff(consumerServiceValueBean.getServiceTariff());
		consumerServiceBean.setUpdatedOn(consumerServiceValueBean.getUpdatedOn());
		
		
		return consumerServiceBean;
		
	}
}
