package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;

import tneb.ccms.admin.model.ComplaintBean;
import tneb.ccms.admin.util.GeneralUtil;

public class ComplaintValueBean implements Serializable {

	private static final long serialVersionUID = -5641861880126346375L;

	private Integer id;

	private Integer categoryId;

	private Integer subCategoryId;

	private Integer regionId;

	private Integer circleId;

	private Integer districtId;

	private Integer divisionId;

	private Integer subDivisionId;

	private Integer sectionId;

	private Integer userId;

	private Integer focComplaintId;

	private Integer complaintCode;

	private Integer reasonId;

	private Integer rating;

	private String complaintType;

	private String serviceNumber;

	private String landmark;

	private String description;

	private String latitude;

	private String longitude;

	private String image1;

	private String image2;

	private Integer statusId;
	
	private String status;

	private String device;

	private Timestamp updatedOn;

	private Timestamp createdOn;
	
	private String updatedOnFormatted;

	private String createdOnFormatted;

	private Integer rescueId;

	private String serviceAddress;
	
	private String regionName;
	
	private String circleName;
	
	private String divisionName;
	
	private String subDivisionName;
	
	private String sectionName;
	
	private String complaintName;
	
	private String categoryName;

	private String subCategoryName;
	
	private String applicationNo;
	
	private String receivedFrom;
	
	private String tag;
	
	private String distribution;
	
	private String pincode;
	
	private String phaseType;
	
	private Boolean vipCustomer;
	
	private Timestamp intUpdatedOn;
	
	private int[] complaintCounts;
	
	private String finalDescription;
	
	private String serviceName;
	

	
	
	public String getFinalDescription() {
		return finalDescription;
	}

	public void setFinalDescription(String finalDescription) {
		this.finalDescription = finalDescription;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public Timestamp getIntUpdatedOn() {
		return intUpdatedOn;
	}

	public void setIntUpdatedOn(Timestamp intUpdatedOn) {
		this.intUpdatedOn = intUpdatedOn;
	}

	public int[] getComplaintCounts() {
		return complaintCounts;
	}

	public void setComplaintCounts(int[] complaintCounts) {
		this.complaintCounts = complaintCounts;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getSubCategoryName() {
		return subCategoryName;
	}

	public void setSubCategoryName(String subCategoryName) {
		this.subCategoryName = subCategoryName;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	public String getCircleName() {
		return circleName;
	}

	public void setCircleName(String circleName) {
		this.circleName = circleName;
	}

	public String getDivisionName() {
		return divisionName;
	}

	public void setDivisionName(String divisionName) {
		this.divisionName = divisionName;
	}

	public String getSubDivisionName() {
		return subDivisionName;
	}

	public void setSubDivisionName(String subDivisionName) {
		this.subDivisionName = subDivisionName;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	public String getComplaintName() {
		return complaintName;
	}

	public void setComplaintName(String complaintName) {
		this.complaintName = complaintName;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public Integer getSubCategoryId() {
		return subCategoryId;
	}

	public void setSubCategoryId(Integer subCategoryId) {
		this.subCategoryId = subCategoryId;
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

	public Integer getDistrictId() {
		return districtId;
	}

	public void setDistrictId(Integer districtId) {
		this.districtId = districtId;
	}

	public Integer getDivisionId() {
		return divisionId;
	}

	public void setDivisionId(Integer divisionId) {
		this.divisionId = divisionId;
	}

	public Integer getSubDivisionId() {
		return subDivisionId;
	}

	public void setSubDivisionId(Integer subDivisionId) {
		this.subDivisionId = subDivisionId;
	}

	public Integer getSectionId() {
		return sectionId;
	}

	public void setSectionId(Integer sectionId) {
		this.sectionId = sectionId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getFocComplaintId() {
		return focComplaintId;
	}

	public void setFocComplaintId(Integer focComplaintId) {
		this.focComplaintId = focComplaintId;
	}

	public Integer getComplaintCode() {
		return complaintCode;
	}

	public void setComplaintCode(Integer complaintCode) {
		this.complaintCode = complaintCode;
	}

	public Integer getReasonId() {
		return reasonId;
	}

	public void setReasonId(Integer reasonId) {
		this.reasonId = reasonId;
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	public String getComplaintType() {
		return complaintType;
	}

	public void setComplaintType(String complaintType) {
		this.complaintType = complaintType;
	}

	public String getServiceNumber() {
		return serviceNumber;
	}

	public void setServiceNumber(String serviceNumber) {
		this.serviceNumber = serviceNumber;
	}

	public String getLandmark() {
		return landmark;
	}

	public void setLandmark(String landmark) {
		this.landmark = landmark;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getImage1() {
		return image1;
	}

	public void setImage1(String image1) {
		this.image1 = image1;
	}

	public String getImage2() {
		return image2;
	}

	public void setImage2(String image2) {
		this.image2 = image2;
	}

	public Integer getStatusId() {
		return statusId;
	}

	public void setStatusId(Integer statusId) {
		this.statusId = statusId;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public Timestamp getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Timestamp updatedOn) {
		this.updatedOn = updatedOn;
	}

	public Timestamp getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

	public Integer getRescueId() {
		return rescueId;
	}

	public void setRescueId(Integer rescueId) {
		this.rescueId = rescueId;
	}

	public String getServiceAddress() {
		return serviceAddress;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

	public String getUpdatedOnFormatted() {
		return updatedOnFormatted;
	}

	public void setUpdatedOnFormatted(String updatedOnFormatted) {
		this.updatedOnFormatted = updatedOnFormatted;
	}

	public String getCreatedOnFormatted() {
		return createdOnFormatted;
	}

	public void setCreatedOnFormatted(String createdOnFormatted) {
		this.createdOnFormatted = createdOnFormatted;
	}

	public String getApplicationNo() {
		return applicationNo;
	}

	public void setApplicationNo(String applicationNo) {
		this.applicationNo = applicationNo;
	}
	
	public String getReceivedFrom() {
		return receivedFrom;
	}

	public void setReceivedFrom(String receivedFrom) {
		this.receivedFrom = receivedFrom;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getDistribution() {
		return distribution;
	}

	public void setDistribution(String distribution) {
		this.distribution = distribution;
	}

	public String getPincode() {
		return pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	public String getPhaseType() {
		return phaseType;
	}

	public void setPhaseType(String phaseType) {
		this.phaseType = phaseType;
	}

	public Boolean getVipCustomer() {
		return vipCustomer;
	}

	public void setVipCustomer(Boolean vipCustomer) {
		this.vipCustomer = vipCustomer;
	}

	public static ComplaintValueBean convertComplaintBeanTocomplaintValueBean(ComplaintBean complaintBean) {
		
		ComplaintValueBean complaintValueBean = new ComplaintValueBean();
		
		if(complaintBean.getRegionBean() != null) {
			complaintValueBean.setRegionId(complaintBean.getRegionBean().getId());
			complaintValueBean.setRegionName(complaintBean.getRegionBean().getName());
		}
		if(complaintBean.getCircleBean() != null) {
			complaintValueBean.setCircleId(complaintBean.getCircleBean().getId());
			complaintValueBean.setCircleName(complaintBean.getCircleBean().getName());
		}
		if(complaintBean.getDivisionBean() != null) {
			complaintValueBean.setDivisionId(complaintBean.getDivisionBean().getId());
			complaintValueBean.setDivisionName(complaintBean.getDivisionBean().getName());
		}
		if(complaintBean.getSubDivisionBean() != null) {
			complaintValueBean.setSubDivisionId(complaintBean.getSubDivisionBean().getId());
			complaintValueBean.setSubDivisionName(complaintBean.getSubDivisionBean().getName());
		}
		if(complaintBean.getSectionBean() != null) {
			complaintValueBean.setSectionId(complaintBean.getSectionBean().getId());
			complaintValueBean.setSectionName(complaintBean.getSectionBean().getName());
		}
		if(complaintBean.getPublicUserBean() != null) {
			complaintValueBean.setUserId(complaintBean.getPublicUserBean().getId());
		}
		if(complaintBean.getCategoryBean() != null) {
			complaintValueBean.setCategoryId(complaintBean.getCategoryBean().getId());
			complaintValueBean.setCategoryName(complaintBean.getCategoryBean().getName());
		}
		if(complaintBean.getSubCategoryBean() != null) {
			complaintValueBean.setSubCategoryId(complaintBean.getSubCategoryBean().getId());
			complaintValueBean.setSubCategoryName(complaintBean.getSubCategoryBean().getName());
		}
		
		complaintValueBean.setId(complaintBean.getId());
		complaintValueBean.setServiceNumber(complaintBean.getServiceNumber());
		complaintValueBean.setLandmark(complaintBean.getLandmark());
		complaintValueBean.setDescription(complaintBean.getDescription());
		complaintValueBean.setLatitude(complaintBean.getLatitude());
		complaintValueBean.setLongitude(complaintBean.getLongitude());
		complaintValueBean.setStatusId(complaintBean.getStatusId());
		complaintValueBean.setCreatedOn(complaintBean.getCreatedOn());
		complaintValueBean.setUpdatedOn(complaintBean.getUpdatedOn());
		complaintValueBean.setServiceAddress(complaintBean.getServiceAddress());
		complaintValueBean.setComplaintCode(complaintBean.getComplaintCode());
		complaintValueBean.setComplaintType(complaintBean.getComplaintType());
		complaintValueBean.setCreatedOnFormatted(GeneralUtil.formatDate(complaintBean.getCreatedOn()));
		complaintValueBean.setUpdatedOnFormatted(GeneralUtil.formatDate(complaintBean.getUpdatedOn()));
		complaintValueBean.setApplicationNo(complaintBean.getApplicationNo());
		complaintValueBean.setReceivedFrom(complaintBean.getReceivedFrom());
		complaintValueBean.setTag(complaintBean.getTag());
		complaintValueBean.setIntUpdatedOn(complaintBean.getIntUpdatedOn());
		complaintValueBean.setServiceName(complaintBean.getServiceName());
		
		
		return complaintValueBean;
	}
	
	public static ComplaintBean convertComplaintValueBeanToCompliantBean(ComplaintValueBean complaintValueBean) {
		
		ComplaintBean complaintBean = new ComplaintBean();
		
		complaintBean.setServiceNumber(complaintValueBean.getServiceNumber());
		complaintBean.setLandmark(complaintValueBean.getLandmark());
		complaintBean.setDescription(complaintValueBean.getDescription());
		complaintBean.setLatitude(complaintValueBean.getLatitude());
		complaintBean.setLongitude(complaintValueBean.getLongitude());
		complaintBean.setStatusId(complaintValueBean.getStatusId());
		complaintBean.setCreatedOn(complaintValueBean.getCreatedOn());
		complaintBean.setUpdatedOn(complaintValueBean.getUpdatedOn());
		complaintBean.setServiceAddress(complaintValueBean.getServiceAddress());
		complaintBean.setComplaintCode(complaintValueBean.getComplaintCode());
		complaintBean.setComplaintType(complaintValueBean.getComplaintType());
		complaintBean.setApplicationNo(complaintValueBean.getApplicationNo());
		complaintBean.setTag(complaintValueBean.getTag());
		complaintBean.setReceivedFrom(complaintValueBean.getReceivedFrom());
		complaintBean.setServiceName(complaintValueBean.getServiceName());
		
		return complaintBean;
	}

}
