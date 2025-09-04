package tneb.ccms.admin.model;

import java.sql.Timestamp;

import javax.persistence.Column;

import javax.persistence.Entity;

import javax.persistence.Id;

import javax.persistence.Table;

@Entity

@Table(name = "VIEW_COMPLAINT")

public class ViewComplaintBean {

	@Id

	@Column(name = "ID")

	Integer id;

	@Column(name = "SERVICE_NUMBER")

	String serviceNumber;

	@Column(name = "SERVICE_NAME")

	String serviceName;

	@Column(name = "CATEGORY_ID")

	Integer categoryId;

	@Column(name = "SUB_CATEGORY_ID")

	Integer subCategoryId;

	@Column(name = "RESCUE_ID")

	Integer rescueId;

	@Column(name = "SECTION_ID")

	Integer sectionId;

	@Column(name = "SERVICE_ADDRESS")

	String serviceAddress;

	@Column(name = "LANDMARK")

	String landmark;

	@Column(name = "LATITUDE")

	String latitude;

	@Column(name = "LONGITUDE")

	String longitude;

	@Column(name = "IMAGE_1")

	String image1;

	@Column(name = "IMAGE_2")

	String image2;

	@Column(name = "DESCRIPTION")

	String description;

	@Column(name = "USER_ID")

	Integer userId;

	@Column(name = "MOBILE")

	String mobile;

	@Column(name = "APPLICATION_NO")

	String applicationNo;

	@Column(name = "STATUS_ID")

	Integer statusId;

	@Column(name = "REGION_ID")

	Integer regionId;

	@Column(name = "CIRCLE_ID")

	Integer circleId;

	@Column(name = "DEVICE")

	String device;

	@Column(name = "COMPLAINT_TYPE")

	String complaintType;

	@Column(name = "SUB_DIVISION_ID")

	Integer subDivisionId;

	@Column(name = "DIVISION_ID")

	Integer divisionId;

	@Column(name = "SECTION_NAME")

	String sectionName;

	@Column(name = "CIRCLE_NAME")

	String circleName;

	@Column(name = "CATEGORY_NAME")

	String categoryName;

	@Column(name = "SUB_CATEGORY_NAME")

	String subCategoryName;

	@Column(name = "COMPLAINT_CODE")

	Integer complaintCode;

	@Column(name = "RATING")

	Integer rating;

	@Column(name = "CREATED_ON")

	Timestamp createdOn;

	@Column(name = "UPDATED_ON")

	Timestamp updatedOn;

	@Column(name = "FOC_COMPLAINT_ID")

	String focComplaintId;

	@Column(name = "TAG")

	String tag;

	@Column(name = "RECEIVED_FROM")

	String receivedFrom;

	@Column(name = "ALTERNATE_MOBILE_NO")

	String alternateMobileNo;

	@Column(name = "FEEDBACK_RATING")

	Integer feedbackRating;

	@Column(name = "COMP_ENT_USER")

	String compEntUser;

	@Column(name = "COMP_MOBILE")

	String comp_Mobile;

	public Integer getId() {

		return id;

	}

	public void setId(Integer id) {

		this.id = id;

	}

	public String getServiceNumber() {

		return serviceNumber;

	}

	public void setServiceNumber(String serviceNumber) {

		this.serviceNumber = serviceNumber;

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

	public Integer getRescueId() {

		return rescueId;

	}

	public void setRescueId(Integer rescueId) {

		this.rescueId = rescueId;

	}

	public Integer getSectionId() {

		return sectionId;

	}

	public void setSectionId(Integer sectionId) {

		this.sectionId = sectionId;

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

	public String getDescription() {

		return description;

	}

	public void setDescription(String description) {

		this.description = description;

	}

	public Integer getUserId() {

		return userId;

	}

	public void setUserId(Integer userId) {

		this.userId = userId;

	}

	public Integer getStatusId() {

		return statusId;

	}

	public void setStatusId(Integer statusId) {

		this.statusId = statusId;

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

	public String getDevice() {

		return device;

	}

	public void setDevice(String device) {

		this.device = device;

	}

	public String getComplaintType() {

		return complaintType;

	}

	public void setComplaintType(String complaintType) {

		this.complaintType = complaintType;

	}

	public Integer getSubDivisionId() {

		return subDivisionId;

	}

	public void setSubDivisionId(Integer subDivisionId) {

		this.subDivisionId = subDivisionId;

	}

	public Integer getDivisionId() {

		return divisionId;

	}

	public void setDivisionId(Integer divisionId) {

		this.divisionId = divisionId;

	}

	public String getSectionName() {

		return sectionName;

	}

	public void setSectionName(String sectionName) {

		this.sectionName = sectionName;

	}

	public String getCircleName() {

		return circleName;

	}

	public void setCircleName(String circleName) {

		this.circleName = circleName;

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

	public Integer getComplaintCode() {

		return complaintCode;

	}

	public void setComplaintCode(Integer complaintCode) {

		this.complaintCode = complaintCode;

	}

	public Integer getRating() {

		return rating;

	}

	public void setRating(Integer rating) {

		this.rating = rating;

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

	public String getMobile() {

		return mobile;

	}

	public void setMobile(String mobile) {

		this.mobile = mobile;

	}

	public String getFocComplaintId() {

		return focComplaintId;

	}

	public void setFocComplaintId(String focComplaintId) {

		this.focComplaintId = focComplaintId;

	}

	public String getApplicationNo() {

		return applicationNo;

	}

	public void setApplicationNo(String applicationNo) {

		this.applicationNo = applicationNo;

	}

	public String getTag() {

		return tag;

	}

	public void setTag(String tag) {

		this.tag = tag;

	}

	public String getReceivedFrom() {

		return receivedFrom;

	}

	public void setReceivedFrom(String receivedFrom) {

		this.receivedFrom = receivedFrom;

	}

	public String getAlternateMobileNo() {

		return alternateMobileNo;

	}

	public void setAlternateMobileNo(String alternateMobileNo) {

		this.alternateMobileNo = alternateMobileNo;

	}

	public Integer getFeedbackRating() {

		return feedbackRating;

	}

	public void setFeedbackRating(Integer feedbackRating) {

		this.feedbackRating = feedbackRating;

	}

	public String getCompEntUser() {

		return compEntUser;

	}

	public void setCompEntUser(String compEntUser) {

		this.compEntUser = compEntUser;

	}



	public String getComp_Mobile() {
		return comp_Mobile;
	}

	public void setComp_Mobile(String comp_Mobile) {
		this.comp_Mobile = comp_Mobile;
	}

	public String getServiceName() {

		return serviceName;

	}

	public void setServiceName(String serviceName) {

		this.serviceName = serviceName;

	}

}
