package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;

import tneb.ccms.admin.model.ViewComplaintBean;
import tneb.ccms.admin.util.CCMSConstants;
import tneb.ccms.admin.util.GeneralUtil;


public class ViewComplaintValueBean implements Serializable {

	private static final long serialVersionUID = -9022243318312349492L;

	private Integer serialNo;
	
	private Integer id;

	private String serviceNumber;

	private Integer categoryId;

	private Integer subCategoryId;

	private Integer rescueId;

	private Integer sectionId;

	private String serviceAddress;

	private String landmark;

	private String latitude;

	private String longitude;

	private String image1;

	private String image2;

	private String description;

	private Integer userId;
	
	private String mobile;

	private Integer statusId;
	
	private String statusValue;

	private Integer regionId;

	private Integer circleId;

	private String device;

	private String complaintType;

	private Integer subDivisionId;

	private Integer divisionId;

	private String sectionName;

	private String circleName;

	private String categoryName;

	private String subCategoryName;

	private Integer complaintCode;
	
	private String complaintCodeValue;
	
	private String complaintStatusValue;

	private Integer rating;

	private Timestamp createdOn;

	private Timestamp updatedOn;
	
	private String focComplaintId;
	
	private String updatedOnFormatted;

	private String createdOnFormatted;
	
	private String applicationNo;

	private String tag;
	
	private String receivedFrom;

	private String alternateMobileNo;
	
	private String comp_Mobile;
	
	private Integer feedbackRating;
	
	private String serviceName;
	
	private String compEntUser;
	
	
	
	
	
	public String getCompEntUser() {
		return compEntUser;
	}


	public void setCompEntUser(String compEntUser) {
		this.compEntUser = compEntUser;
	}


	public String getServiceName() {
		return serviceName;
	}


	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}


	public String getComp_Mobile() {
		return comp_Mobile;
	}


	public void setComp_Mobile(String comp_Mobile) {
		this.comp_Mobile = comp_Mobile;
	}


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
	
	
	public String getFocComplaintId() {
		return focComplaintId;
	}

	public void setFocComplaintId(String focComplaintId) {
		this.focComplaintId = focComplaintId;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(Integer serialNo) {
		this.serialNo = serialNo;
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
	
	public String getStatusValue() {
		return statusValue;
	}

	public void setStatusValue(String statusValue) {
		this.statusValue = statusValue;
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
	
	public String getComplaintCodeValue() {
		return complaintCodeValue;
	}

	public void setComplaintCodeValue(String complaintCodeValue) {
		this.complaintCodeValue = complaintCodeValue;
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
	
	public String getComplaintStatusValue() {
		return complaintStatusValue;
	}

	public void setComplaintStatusValue(String complaintStatusValue) {
		this.complaintStatusValue = complaintStatusValue;
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

	public static ViewComplaintValueBean convertViewComplaintBeanToViewComplaintValueBean(
			ViewComplaintBean viewComplaintBean) {

		ViewComplaintValueBean viewComplaintValueBean = new ViewComplaintValueBean();

		viewComplaintValueBean.setCategoryId(viewComplaintBean.getCategoryId());
		viewComplaintValueBean.setCategoryName(viewComplaintBean.getCategoryName());
		viewComplaintValueBean.setCircleId(viewComplaintBean.getCircleId());
		viewComplaintValueBean.setCircleName(viewComplaintBean.getCircleName());
		viewComplaintValueBean.setComplaintCode(viewComplaintBean.getComplaintCode());
		viewComplaintValueBean.setComplaintCodeValue(CCMSConstants.COMPLAINT_NAME[viewComplaintBean.getComplaintCode()]);
		viewComplaintValueBean.setComplaintStatusValue(CCMSConstants.STATUSES[viewComplaintBean.getStatusId()]);
		viewComplaintValueBean.setComplaintType(viewComplaintBean.getComplaintType());
		viewComplaintValueBean.setCreatedOn(viewComplaintBean.getCreatedOn());
		viewComplaintValueBean.setCreatedOnFormatted(GeneralUtil.formatDate(viewComplaintBean.getCreatedOn()));
		viewComplaintValueBean.setDescription(viewComplaintBean.getDescription());
		viewComplaintValueBean.setDevice(viewComplaintBean.getDevice());
		viewComplaintValueBean.setDivisionId(viewComplaintBean.getDivisionId());
		viewComplaintValueBean.setId(viewComplaintBean.getId());
		viewComplaintValueBean.setImage1(viewComplaintBean.getImage1());
		viewComplaintValueBean.setImage2(viewComplaintBean.getImage2());
		viewComplaintValueBean.setLandmark(viewComplaintBean.getLandmark());
		viewComplaintValueBean.setLatitude(viewComplaintBean.getLatitude());
		viewComplaintValueBean.setLongitude(viewComplaintBean.getLongitude());
		viewComplaintValueBean.setRating(viewComplaintBean.getRating());
		viewComplaintValueBean.setRegionId(viewComplaintBean.getRegionId());
		viewComplaintValueBean.setRescueId(viewComplaintBean.getRescueId());
		viewComplaintValueBean.setSectionId(viewComplaintBean.getSectionId());
		viewComplaintValueBean.setSectionName(viewComplaintBean.getSectionName());
		viewComplaintValueBean.setServiceNumber(viewComplaintBean.getServiceNumber());
		viewComplaintValueBean.setStatusId(viewComplaintBean.getStatusId());
		viewComplaintValueBean.setServiceAddress(viewComplaintBean.getServiceAddress());
		viewComplaintValueBean.setSubCategoryId(viewComplaintBean.getSubCategoryId());
		viewComplaintValueBean.setSubCategoryName(viewComplaintBean.getSubCategoryName());
		viewComplaintValueBean.setSubDivisionId(viewComplaintBean.getSubDivisionId());
		viewComplaintValueBean.setUpdatedOn(viewComplaintBean.getUpdatedOn());
		viewComplaintValueBean.setUpdatedOnFormatted(GeneralUtil.formatDate(viewComplaintBean.getUpdatedOn()));
		viewComplaintValueBean.setUserId(viewComplaintBean.getUserId());
		viewComplaintValueBean.setMobile(viewComplaintBean.getMobile());
		viewComplaintValueBean.setFocComplaintId(viewComplaintBean.getFocComplaintId());
		viewComplaintValueBean.setApplicationNo(viewComplaintBean.getApplicationNo());
		viewComplaintValueBean.setTag(viewComplaintBean.getTag());
		viewComplaintValueBean.setReceivedFrom(viewComplaintBean.getReceivedFrom());
		viewComplaintValueBean.setFeedbackRating(viewComplaintBean.getFeedbackRating());
		viewComplaintValueBean.setComp_Mobile(viewComplaintBean.getComp_Mobile());
		// For Old complaints, consumer mobile number will be shown as Contact mobile number.
		String altMobile = viewComplaintBean.getAlternateMobileNo();
		if (altMobile != null && altMobile.trim().length() > 0) {
			viewComplaintValueBean.setAlternateMobileNo(altMobile);
		} else {
			viewComplaintValueBean.setAlternateMobileNo(viewComplaintBean.getMobile());
			viewComplaintValueBean.setMobile(viewComplaintBean.getMobile());
		}

		return viewComplaintValueBean;
	}

	public static ViewComplaintBean convertViewComplaintValueBeanToViewComplaintBean(
			ViewComplaintValueBean viewComplaintValueBean) {

		ViewComplaintBean viewComplaintBean = new ViewComplaintBean();
		viewComplaintBean.setApplicationNo(viewComplaintValueBean.getApplicationNo());
		viewComplaintBean.setCategoryId(viewComplaintValueBean.getCategoryId());
		viewComplaintBean.setCategoryName(viewComplaintValueBean.getCategoryName());
		viewComplaintBean.setCircleId(viewComplaintValueBean.getCircleId());
		viewComplaintBean.setCircleName(viewComplaintValueBean.getCircleName());
		viewComplaintBean.setComplaintCode(viewComplaintValueBean.getComplaintCode());
		viewComplaintBean.setComplaintType(viewComplaintValueBean.getComplaintType());
		viewComplaintBean.setCreatedOn(viewComplaintValueBean.getCreatedOn());
		viewComplaintBean.setDescription(viewComplaintValueBean.getDescription());
		viewComplaintBean.setDevice(viewComplaintValueBean.getDevice());
		viewComplaintBean.setDivisionId(viewComplaintValueBean.getDivisionId());
		viewComplaintBean.setId(viewComplaintValueBean.getId());
		viewComplaintBean.setImage1(viewComplaintValueBean.getImage1());
		viewComplaintBean.setImage2(viewComplaintValueBean.getImage2());
		viewComplaintBean.setLandmark(viewComplaintValueBean.getLandmark());
		viewComplaintBean.setLatitude(viewComplaintValueBean.getLatitude());
		viewComplaintBean.setLongitude(viewComplaintValueBean.getLongitude());
		viewComplaintBean.setRating(viewComplaintValueBean.getRating());
		viewComplaintBean.setRegionId(viewComplaintValueBean.getRegionId());
		viewComplaintBean.setRescueId(viewComplaintValueBean.getRescueId());
		viewComplaintBean.setSectionId(viewComplaintValueBean.getSectionId());
		viewComplaintBean.setSectionName(viewComplaintValueBean.getSectionName());
		viewComplaintBean.setServiceNumber(viewComplaintValueBean.getServiceNumber());
		viewComplaintBean.setStatusId(viewComplaintValueBean.getStatusId());
		viewComplaintBean.setServiceAddress(viewComplaintValueBean.getServiceAddress());
		viewComplaintBean.setSubCategoryId(viewComplaintValueBean.getSubCategoryId());
		viewComplaintBean.setSubCategoryName(viewComplaintValueBean.getSubCategoryName());
		viewComplaintBean.setSubDivisionId(viewComplaintValueBean.getSubDivisionId());
		viewComplaintBean.setUpdatedOn(viewComplaintValueBean.getUpdatedOn());
		viewComplaintBean.setUserId(viewComplaintValueBean.getUserId());
		viewComplaintBean.setMobile(viewComplaintValueBean.getMobile());
		viewComplaintBean.setFocComplaintId(viewComplaintValueBean.getFocComplaintId());
		viewComplaintBean.setAlternateMobileNo(viewComplaintValueBean.getAlternateMobileNo());
		viewComplaintBean.setFeedbackRating(viewComplaintValueBean.getFeedbackRating());
		viewComplaintBean.setComp_Mobile(viewComplaintValueBean.getComp_Mobile());
		return viewComplaintBean;
	}

	public static ViewComplaintValueBean convertViewComplaintBeanToViewComplaintValueBeans(
			ViewComplaintBean viewComplaintBean) {

		ViewComplaintValueBean viewComplaintValueBean = new ViewComplaintValueBean();

		viewComplaintValueBean.setCategoryId(viewComplaintBean.getCategoryId());
		viewComplaintValueBean.setCategoryName(viewComplaintBean.getCategoryName());
		viewComplaintValueBean.setCircleId(viewComplaintBean.getCircleId());
		viewComplaintValueBean.setCircleName(viewComplaintBean.getCircleName());
		
		viewComplaintValueBean.setCreatedOn(viewComplaintBean.getCreatedOn());
		viewComplaintValueBean.setCreatedOnFormatted(GeneralUtil.formatDate(viewComplaintBean.getCreatedOn()));
		viewComplaintValueBean.setDescription(viewComplaintBean.getDescription());
		viewComplaintValueBean.setDevice(viewComplaintBean.getDevice());
		viewComplaintValueBean.setDivisionId(viewComplaintBean.getDivisionId());
		viewComplaintValueBean.setId(viewComplaintBean.getId());
		viewComplaintValueBean.setImage1(viewComplaintBean.getImage1());
		viewComplaintValueBean.setImage2(viewComplaintBean.getImage2());
		viewComplaintValueBean.setLandmark(viewComplaintBean.getLandmark());
		viewComplaintValueBean.setLatitude(viewComplaintBean.getLatitude());
		viewComplaintValueBean.setLongitude(viewComplaintBean.getLongitude());
		viewComplaintValueBean.setRating(viewComplaintBean.getRating());
		viewComplaintValueBean.setRegionId(viewComplaintBean.getRegionId());
		viewComplaintValueBean.setRescueId(viewComplaintBean.getRescueId());
		viewComplaintValueBean.setSectionId(viewComplaintBean.getSectionId());
		viewComplaintValueBean.setSectionName(viewComplaintBean.getSectionName());
		viewComplaintValueBean.setServiceNumber(viewComplaintBean.getServiceNumber());
	
		viewComplaintValueBean.setServiceAddress(viewComplaintBean.getServiceAddress());
		viewComplaintValueBean.setSubCategoryId(viewComplaintBean.getSubCategoryId());
		viewComplaintValueBean.setSubCategoryName(viewComplaintBean.getSubCategoryName());
		viewComplaintValueBean.setSubDivisionId(viewComplaintBean.getSubDivisionId());
		viewComplaintValueBean.setUpdatedOn(viewComplaintBean.getUpdatedOn());
		viewComplaintValueBean.setUpdatedOnFormatted(GeneralUtil.formatDate(viewComplaintBean.getUpdatedOn()));
		viewComplaintValueBean.setUserId(viewComplaintBean.getUserId());
		viewComplaintValueBean.setMobile(viewComplaintBean.getMobile());
		viewComplaintValueBean.setFocComplaintId(viewComplaintBean.getFocComplaintId());
		viewComplaintValueBean.setApplicationNo(viewComplaintBean.getApplicationNo());
		viewComplaintValueBean.setTag(viewComplaintBean.getTag());
		viewComplaintValueBean.setReceivedFrom(viewComplaintBean.getReceivedFrom());
		viewComplaintValueBean.setFeedbackRating(viewComplaintBean.getFeedbackRating());
		viewComplaintValueBean.setCompEntUser(viewComplaintBean.getCompEntUser());
		viewComplaintValueBean.setComp_Mobile(viewComplaintBean.getComp_Mobile());
		viewComplaintValueBean.setServiceName(viewComplaintBean.getServiceName());
		viewComplaintValueBean.setStatusId(viewComplaintBean.getStatusId());
		// For Old complaints, consumer mobile number will be shown as Contact mobile number.
		String altMobile = viewComplaintBean.getAlternateMobileNo();
		if (altMobile != null && altMobile.trim().length() > 0) {
			viewComplaintValueBean.setAlternateMobileNo(altMobile);
		} else {
			viewComplaintValueBean.setAlternateMobileNo(viewComplaintBean.getMobile());
		}

		return viewComplaintValueBean;
	}

}
