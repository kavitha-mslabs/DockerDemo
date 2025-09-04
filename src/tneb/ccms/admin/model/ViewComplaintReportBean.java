package tneb.ccms.admin.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "VIEW_COMPLAINT_REPORT")
public class ViewComplaintReportBean {
	
	@Id
	@Column(name = "ID")
	Integer id;
	
	@Column(name = "SERVICE_NUMBER")
	String serviceNumber;

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

	@Column(name = "STATUS_ID")
	Integer statusId;

	@Column(name = "REGION_ID")
	Integer regionId;
	
	@Column(name = "REGION_NAME")
	String regionName;

	@Column(name = "CIRCLE_ID")
	Integer circleId;

	@Column(name = "DEVICE")
	String device;

	@Column(name = "COMPLAINT_TYPE")
	String complaintType;

	@Column(name = "SUB_DIVISION_ID")
	Integer subDivisionId;
	
	@Column(name = "SUB_DIVISION_NAME")
	String subDivsionName;

	@Column(name = "DIVISION_ID")
	Integer divisionId;
	
	@Column(name = "DIVISION_NAME")
	String divisionName;

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
	
	@Column(name = "REMARKS")
	String remarks;
	
	@Column(name = "COMPLETED_REMARKS")
	String completedRemarks;

	@Column(name = "TAG")
	String tag;
	
	@Column(name = "RECEIVED_FROM")
	String receivedFrom;

	@Column(name="ALTERNATE_MOBILE_NO")
	String alternateMobileNo;
	
	@Column(name="FEEDBACK_RATING")
	Integer feedbackRating;
	
	@Column(name = "COMP_MOBILE")
	String compMobileNumber;
	
	 public String getFeedbackEmojiAndText() {
	        if (feedbackRating == null) {
	            return "";
	        }

	        switch (feedbackRating) {
	            case 1:
	                return "😡 Not at all";
	            case 2:
	                return "😟 Just barely";
	            case 3:
	                return "😐 Somewhat";
	            case 4:
	                return "😊 Very much";
	            case 5:
	                return "😀 As much as possible";
	            default:
	                return "Invalid Rating";
	        }
	    }

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

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
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

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	public String getSubDivsionName() {
		return subDivsionName;
	}

	public void setSubDivsionName(String subDivsionName) {
		this.subDivsionName = subDivsionName;
	}

	public String getDivisionName() {
		return divisionName;
	}

	public void setDivisionName(String divisionName) {
		this.divisionName = divisionName;
	}

	public String getCompletedRemarks() {
		return completedRemarks;
	}

	public void setCompletedRemarks(String completedRemarks) {
		this.completedRemarks = completedRemarks;
	}

	public Integer getFeedbackRating() {
		return feedbackRating;
	}

	public void setFeedbackRating(Integer feedbackRating) {
		this.feedbackRating = feedbackRating;
	}

	public String getCompMobileNumber() {
		return compMobileNumber;
	}

	public void setCompMobileNumber(String compMobileNumber) {
		this.compMobileNumber = compMobileNumber;
	}
		
	
	
	
}
