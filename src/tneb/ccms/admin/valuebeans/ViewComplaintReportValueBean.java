package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import tneb.ccms.admin.model.ViewComplaintReportBean;
import tneb.ccms.admin.util.CCMSConstants;
import tneb.ccms.admin.util.GeneralUtil;


public class ViewComplaintReportValueBean implements Serializable {

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
	
	private String regionName;

	private Integer circleId;

	private String device;

	private String complaintType;

	private Integer subDivisionId;
	
	private String subDivisionName;

	private Integer divisionId;
	
	private String divisionName;

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
	
	private String remarks;
	
	private String completedRemarks;
	
	private String tag;
	
	private String receivedFrom;
	
	private Integer feedbackRating;
	
	private String compMobileNumber;
	
	private String serviceName;
	
	private String complaintDescription;
	
	private String attendedDate;
	
	private String attendedRemarks;
	
	private BigDecimal complaintId;
	
	private String userName;
	
	private String feedback;
	
	private byte[] images1;
	private byte[] images2;
	
	private String closureImage;
	private String feedBackEntryDate;
	private String registeredMobileNumber;
	
	private List<TransferDetail> transferDetails;
	private List<QualityCheckDetail> qualityCheckDetails;
	private List<ResendDetail>resendDetails;
	
	private String distribution;
	
	
	
	
	
	
	

	
	
	public List<ResendDetail> getResendDetails() {
		return resendDetails;
	}

	public void setResendDetails(List<ResendDetail> resendDetails) {
		this.resendDetails = resendDetails;
	}

	public String getClosureImage() {
		return closureImage;
	}

	public void setClosureImage(String closureImage) {
		this.closureImage = closureImage;
	}

	public String getDistribution() {
		return distribution;
	}

	public void setDistribution(String distribution) {
		this.distribution = distribution;
	}

	public List<QualityCheckDetail> getQualityCheckDetails() {
		return qualityCheckDetails;
	}

	public void setQualityCheckDetails(List<QualityCheckDetail> qualityCheckDetails) {
		this.qualityCheckDetails = qualityCheckDetails;
	}

	public List<TransferDetail> getTransferDetails() {
		return transferDetails;
	}

	public void setTransferDetails(List<TransferDetail> transferDetails) {
		this.transferDetails = transferDetails;
	}

	public byte[] getImages1() {
		return images1;
	}

	public void setImages1(byte[] images1) {
		this.images1 = images1;
	}

	public byte[] getImages2() {
		return images2;
	}

	public void setImages2(byte[] images2) {
		this.images2 = images2;
	}

	public String getFeedBackEntryDate() {
		return feedBackEntryDate;
	}

	public void setFeedBackEntryDate(String feedBackEntryDate) {
		this.feedBackEntryDate = feedBackEntryDate;
	}

	public String getRegisteredMobileNumber() {
		return registeredMobileNumber;
	}

	public void setRegisteredMobileNumber(String registeredMobileNumber) {
		this.registeredMobileNumber = registeredMobileNumber;
	}


	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getComplaintDescription() {
		return complaintDescription;
	}

	public void setComplaintDescription(String complaintDescription) {
		this.complaintDescription = complaintDescription;
	}

	public String getAttendedDate() {
		return attendedDate;
	}

	public void setAttendedDate(String attendedDate) {
		this.attendedDate = attendedDate;
	}

	public String getAttendedRemarks() {
		return attendedRemarks;
	}

	public void setAttendedRemarks(String attendedRemarks) {
		this.attendedRemarks = attendedRemarks;
	}

	public BigDecimal getComplaintId() {
		return complaintId;
	}

	public void setComplaintId(BigDecimal complaintId) {
		this.complaintId = complaintId;
	}

	public String getFeedback() {
		return feedback;
	}

	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	public String getSubDivisionName() {
		return subDivisionName;
	}

	public void setSubDivisionName(String subDivisionName) {
		this.subDivisionName = subDivisionName;
	}

	public String getDivisionName() {
		return divisionName;
	}

	public void setDivisionName(String divisionName) {
		this.divisionName = divisionName;
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

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
	public String getComplaintStatusValue() {
		return complaintStatusValue;
	}

	public void setComplaintStatusValue(String complaintStatusValue) {
		this.complaintStatusValue = complaintStatusValue;
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
                return "";  // Handle unexpected feedback ratings gracefully
        }
    }

	 public static class ResendDetail {
	        private String resendDate;
	        private String resendedBy;
	        private String resendRemarks;
			public String getResendDate() {
				return resendDate;
			}
			public void setResendDate(String resendDate) {
				this.resendDate = resendDate;
			}
			public String getResendedBy() {
				return resendedBy;
			}
			public void setResendedBy(String resendedBy) {
				this.resendedBy = resendedBy;
			}
			public String getResendRemarks() {
				return resendRemarks;
			}
			public void setResendRemarks(String resendRemarks) {
				this.resendRemarks = resendRemarks;
			}
			
	    }
	
	 public static class TransferDetail {
	        private String transferDate;
	        private String transferredBy;
	        private String transferRemarks;
			public String getTransferDate() {
				return transferDate;
			}
			public void setTransferDate(String transferDate) {
				this.transferDate = transferDate;
			}
			public String getTransferredBy() {
				return transferredBy;
			}
			public void setTransferredBy(String transferredBy) {
				this.transferredBy = transferredBy;
			}
			public String getTransferRemarks() {
				return transferRemarks;
			}
			public void setTransferRemarks(String transferRemarks) {
				this.transferRemarks = transferRemarks;
			}
	        
	        
	        
	    }
	 
	 public static class QualityCheckDetail {
	        private String qcDate;
	        private String qcStatus;
	        private String qcRemarks;
			public String getQcDate() {
				return qcDate;
			}
			public void setQcDate(String qcDate) {
				this.qcDate = qcDate;
			}
			public String getQcStatus() {
				return qcStatus;
			}
			public void setQcStatus(String qcStatus) {
				this.qcStatus = qcStatus;
			}
			public String getQcRemarks() {
				return qcRemarks;
			}
			public void setQcRemarks(String qcRemarks) {
				this.qcRemarks = qcRemarks;
			}
	        
	        
	        
	    }
	public static ViewComplaintReportValueBean convertViewComplaintReportBeanToViewComplaintReportValueBean(
			ViewComplaintReportBean viewComplaintReportBean) {

		ViewComplaintReportValueBean viewComplaintReportValueBean = new ViewComplaintReportValueBean();

		viewComplaintReportValueBean.setCategoryId(viewComplaintReportBean.getCategoryId());
		viewComplaintReportValueBean.setCategoryName(viewComplaintReportBean.getCategoryName());
		viewComplaintReportValueBean.setCircleId(viewComplaintReportBean.getCircleId());
		viewComplaintReportValueBean.setCircleName(viewComplaintReportBean.getCircleName());
		viewComplaintReportValueBean.setComplaintCode(viewComplaintReportBean.getComplaintCode());
		int complaintCode = viewComplaintReportBean.getComplaintCode();
		if (complaintCode >= 0 && complaintCode < CCMSConstants.COMPLAINT_NAME.length) {
			viewComplaintReportValueBean.setComplaintCodeValue(CCMSConstants.COMPLAINT_NAME[viewComplaintReportBean.getComplaintCode()]);
		}
		viewComplaintReportValueBean.setComplaintStatusValue(CCMSConstants.STATUSES[viewComplaintReportBean.getStatusId()]);
		viewComplaintReportValueBean.setComplaintType(viewComplaintReportBean.getComplaintType());
		viewComplaintReportValueBean.setCreatedOn(viewComplaintReportBean.getCreatedOn());
		viewComplaintReportValueBean.setCreatedOnFormatted(GeneralUtil.formatDate(viewComplaintReportBean.getCreatedOn()));
		viewComplaintReportValueBean.setDescription(viewComplaintReportBean.getDescription());
		viewComplaintReportValueBean.setDevice(viewComplaintReportBean.getDevice());
		viewComplaintReportValueBean.setDivisionId(viewComplaintReportBean.getDivisionId());
		viewComplaintReportValueBean.setId(viewComplaintReportBean.getId());
		viewComplaintReportValueBean.setImage1(viewComplaintReportBean.getImage1());
		viewComplaintReportValueBean.setImage2(viewComplaintReportBean.getImage2());
		viewComplaintReportValueBean.setLandmark(viewComplaintReportBean.getLandmark());
		viewComplaintReportValueBean.setLatitude(viewComplaintReportBean.getLatitude());
		viewComplaintReportValueBean.setLongitude(viewComplaintReportBean.getLongitude());
		viewComplaintReportValueBean.setRating(viewComplaintReportBean.getRating());
		viewComplaintReportValueBean.setRegionId(viewComplaintReportBean.getRegionId());
		viewComplaintReportValueBean.setRescueId(viewComplaintReportBean.getRescueId());
		viewComplaintReportValueBean.setSectionId(viewComplaintReportBean.getSectionId());
		viewComplaintReportValueBean.setSectionName(viewComplaintReportBean.getSectionName());
		viewComplaintReportValueBean.setServiceNumber(viewComplaintReportBean.getServiceNumber());
		viewComplaintReportValueBean.setStatusId(viewComplaintReportBean.getStatusId());
		viewComplaintReportValueBean.setServiceAddress(viewComplaintReportBean.getServiceAddress());
		viewComplaintReportValueBean.setSubCategoryId(viewComplaintReportBean.getSubCategoryId());
		viewComplaintReportValueBean.setSubCategoryName(viewComplaintReportBean.getSubCategoryName());
		viewComplaintReportValueBean.setSubDivisionId(viewComplaintReportBean.getSubDivisionId());
		viewComplaintReportValueBean.setUpdatedOn(viewComplaintReportBean.getUpdatedOn());
		viewComplaintReportValueBean.setUpdatedOnFormatted(GeneralUtil.formatDate(viewComplaintReportBean.getUpdatedOn()));
		viewComplaintReportValueBean.setUserId(viewComplaintReportBean.getUserId());
		viewComplaintReportValueBean.setMobile(viewComplaintReportBean.getMobile());
		viewComplaintReportValueBean.setFocComplaintId(viewComplaintReportBean.getFocComplaintId());
		viewComplaintReportValueBean.setRemarks(viewComplaintReportBean.getRemarks());
		viewComplaintReportValueBean.setTag(viewComplaintReportBean.getTag());
		viewComplaintReportValueBean.setReceivedFrom(viewComplaintReportBean.getReceivedFrom());
		viewComplaintReportValueBean.setRegionId(viewComplaintReportBean.getRegionId());
		viewComplaintReportValueBean.setRegionName(viewComplaintReportBean.getRegionName());
		viewComplaintReportValueBean.setDivisionName(viewComplaintReportBean.getDivisionName());
		viewComplaintReportValueBean.setSubDivisionName(viewComplaintReportBean.getSubDivsionName());
		viewComplaintReportValueBean.setCompletedRemarks(viewComplaintReportBean.getCompletedRemarks());
		viewComplaintReportValueBean.setFeedbackRating(viewComplaintReportBean.getFeedbackRating());
		viewComplaintReportValueBean.setCompMobileNumber(viewComplaintReportBean.getCompMobileNumber());
		
		return viewComplaintReportValueBean;
	}

	public static ViewComplaintReportBean convertViewComplaintReportValueBeanToViewComplaintReportBean(
			ViewComplaintReportValueBean viewComplaintReportValueBean) {

		ViewComplaintReportBean viewComplaintReportBean = new ViewComplaintReportBean();

		viewComplaintReportBean.setCategoryId(viewComplaintReportValueBean.getCategoryId());
		viewComplaintReportBean.setCategoryName(viewComplaintReportValueBean.getCategoryName());
		viewComplaintReportBean.setCircleId(viewComplaintReportValueBean.getCircleId());
		viewComplaintReportBean.setCircleName(viewComplaintReportValueBean.getCircleName());
		viewComplaintReportBean.setComplaintCode(viewComplaintReportValueBean.getComplaintCode());
		viewComplaintReportBean.setComplaintType(viewComplaintReportValueBean.getComplaintType());
		viewComplaintReportBean.setCreatedOn(viewComplaintReportValueBean.getCreatedOn());
		viewComplaintReportBean.setDescription(viewComplaintReportValueBean.getDescription());
		viewComplaintReportBean.setDevice(viewComplaintReportValueBean.getDevice());
		viewComplaintReportBean.setDivisionId(viewComplaintReportValueBean.getDivisionId());
		viewComplaintReportBean.setId(viewComplaintReportValueBean.getId());
		viewComplaintReportBean.setImage1(viewComplaintReportValueBean.getImage1());
		viewComplaintReportBean.setImage2(viewComplaintReportValueBean.getImage2());
		viewComplaintReportBean.setLandmark(viewComplaintReportValueBean.getLandmark());
		viewComplaintReportBean.setLatitude(viewComplaintReportValueBean.getLatitude());
		viewComplaintReportBean.setLongitude(viewComplaintReportValueBean.getLongitude());
		viewComplaintReportBean.setRating(viewComplaintReportValueBean.getRating());
		viewComplaintReportBean.setRegionId(viewComplaintReportValueBean.getRegionId());
		viewComplaintReportBean.setRescueId(viewComplaintReportValueBean.getRescueId());
		viewComplaintReportBean.setSectionId(viewComplaintReportValueBean.getSectionId());
		viewComplaintReportBean.setSectionName(viewComplaintReportValueBean.getSectionName());
		viewComplaintReportBean.setServiceNumber(viewComplaintReportValueBean.getServiceNumber());
		viewComplaintReportBean.setStatusId(viewComplaintReportValueBean.getStatusId());
		viewComplaintReportBean.setServiceAddress(viewComplaintReportValueBean.getServiceAddress());
		viewComplaintReportBean.setSubCategoryId(viewComplaintReportValueBean.getSubCategoryId());
		viewComplaintReportBean.setSubCategoryName(viewComplaintReportValueBean.getSubCategoryName());
		viewComplaintReportBean.setSubDivisionId(viewComplaintReportValueBean.getSubDivisionId());
		viewComplaintReportBean.setUpdatedOn(viewComplaintReportValueBean.getUpdatedOn());
		viewComplaintReportBean.setUserId(viewComplaintReportValueBean.getUserId());
		viewComplaintReportBean.setMobile(viewComplaintReportValueBean.getMobile());
		viewComplaintReportBean.setFocComplaintId(viewComplaintReportValueBean.getFocComplaintId());
		viewComplaintReportBean.setTag(viewComplaintReportValueBean.getTag());
		viewComplaintReportBean.setReceivedFrom(viewComplaintReportValueBean.getReceivedFrom());
		viewComplaintReportBean.setRegionId(viewComplaintReportValueBean.getRegionId());
		viewComplaintReportBean.setRegionName(viewComplaintReportValueBean.getRegionName());
		viewComplaintReportBean.setDivisionName(viewComplaintReportValueBean.getDivisionName());
		viewComplaintReportBean.setSubDivsionName(viewComplaintReportValueBean.getSubDivisionName());
		viewComplaintReportBean.setCompletedRemarks(viewComplaintReportValueBean.getCompletedRemarks());
		viewComplaintReportBean.setFeedbackRating(viewComplaintReportValueBean.getFeedbackRating());
		viewComplaintReportBean.setCompMobileNumber(viewComplaintReportValueBean.getCompMobileNumber());


		return viewComplaintReportBean;
	}
	
	

}
