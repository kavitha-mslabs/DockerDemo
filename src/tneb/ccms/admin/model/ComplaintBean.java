package tneb.ccms.admin.model;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "COMPLAINT")
public class ComplaintBean {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "COMPLAINT_ID_SEQ")
	@Column(name = "ID")
	Integer id;

	@Column(name = "COMPLAINT_TYPE")
	String complaintType;

	@Column(name = "DESCRIPTION")
	String description;

	@Column(name = "SERVICE_NUMBER")
	String serviceNumber;

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
	
	@Column(name = "DEVICE")
	String device;
	
	@Column(name = "SERVICE_NAME")
	String serviceName;
	
	@Column(name = "SERVICE_ADDRESS")
	String serviceAddress;
	
	@Column(name = "FOC_COMPLAINT_ID")
	String focComplaintId;

	@Column(name = "COMPLAINT_CODE")
	Integer complaintCode;
	
	@Column(name = "STATUS_ID")
	Integer statusId;
	
	@Column(name = "RATING")
	Integer rating;

	@ManyToOne
	@JoinColumn(name = "USER_ID")
	PublicUserBean publicUserBean;
	
	@ManyToOne
	@JoinColumn(name = "RESCUE_ID")
	RescueBean rescueBean;
	
	@ManyToOne
	@JoinColumn(name = "CATEGORY_ID")
	CategoryBean categoryBean;
	
	@ManyToOne
	@JoinColumn(name = "SUB_CATEGORY_ID")
	SubCategoryBean subCategoryBean;
	
	@ManyToOne
	@JoinColumn(name = "REGION_ID")
	RegionBean regionBean;

	@ManyToOne
	@JoinColumn(name = "CIRCLE_ID")
	CircleBean circleBean;

	@ManyToOne
	@JoinColumn(name = "DIVISION_ID")
	DivisionBean divisionBean;

	@ManyToOne
	@JoinColumn(name = "SUB_DIVISION_ID")
	SubDivisionBean subDivisionBean;

	@ManyToOne
	@JoinColumn(name = "SECTION_ID")
	SectionBean sectionBean;
	
	@Column(name = "CREATED_ON", insertable = false, updatable = false)
	Timestamp createdOn;

	@Column(name = "UPDATED_ON", insertable = false, updatable = true)
	Timestamp updatedOn;
	
	@Column(name = "APPLICATION_NO")
	String applicationNo;
	
	@Column(name = "TAG")
	String tag;
	
	@Column(name = "RECEIVED_FROM")
	String receivedFrom;
	
	@Column(name="ALTERNATE_MOBILE_NO")
	String alternateMobileNo;
	
	@OneToMany(mappedBy = "complaintBean", cascade = CascadeType.ALL)
	Set<ComplaintHistoryBean> complaintHistoryList = new HashSet<ComplaintHistoryBean>();
	
	@Column(name = "PINCODE", nullable = true)
	private String pincode;
	
	@Column(name = "CALL_SPL_REMARKS", nullable = true)
	private String callSplRemarks;
	
	@OneToOne
	@JoinColumn(name = "CALL_DETAILS_ID")
	MinnagamCallDetailsBean minnagamCallDetailsBean;
	
	@Column(name = "INT_UPDATED_ON")
	Timestamp intUpdatedOn;
	
	@Column(name = "COMP_CL_USER")
	String compClUser;
	
	@Column(name = "IMAGEID")
	String imageId;
	
	@Column(name = "COMP_ENT_USER")
	String compEntUser;

	public String getCompEntUser() {
		return compEntUser;
	}
 
	public void setCompEntUser(String compEntUser) {
		this.compEntUser = compEntUser;
	}
	
	

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public String getCompClUser() {
		return compClUser;
	}

	public void setCompClUser(String compClUser) {
		this.compClUser = compClUser;
	}

	
	
	//Used to add the ComplaintHistory
	public void addToHistory(ComplaintHistoryBean complaintHistory) {
		this.complaintHistoryList.add(complaintHistory);
	}
	 
		public Set<ComplaintHistoryBean> getComplaintHistoryList() {
		return complaintHistoryList;
	}

	public void setComplaintHistoryList(Set<ComplaintHistoryBean> complaintHistoryList) {
		this.complaintHistoryList = complaintHistoryList;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getComplaintType() {
		return complaintType;
	}

	public void setComplaintType(String complaintType) {
		this.complaintType = complaintType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
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

	public String getFocComplaintId() {
		return focComplaintId;
	}

	public void setFocComplaintId(String focComplaintId) {
		this.focComplaintId = focComplaintId;
	}

	public Integer getComplaintCode() {
		return complaintCode;
	}

	public void setComplaintCode(Integer complaintCode) {
		this.complaintCode = complaintCode;
	}

	public Integer getStatusId() {
		return statusId;
	}

	public void setStatusId(Integer statusId) {
		this.statusId = statusId;
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	public PublicUserBean getPublicUserBean() {
		return publicUserBean;
	}

	public void setPublicUserBean(PublicUserBean publicUserBean) {
		this.publicUserBean = publicUserBean;
	}

	public RescueBean getRescueBean() {
		return rescueBean;
	}

	public void setRescueBean(RescueBean rescueBean) {
		this.rescueBean = rescueBean;
	}

	public CategoryBean getCategoryBean() {
		return categoryBean;
	}

	public void setCategoryBean(CategoryBean categoryBean) {
		this.categoryBean = categoryBean;
	}

	public SubCategoryBean getSubCategoryBean() {
		return subCategoryBean;
	}

	public void setSubCategoryBean(SubCategoryBean subCategoryBean) {
		this.subCategoryBean = subCategoryBean;
	}

	public RegionBean getRegionBean() {
		return regionBean;
	}

	public void setRegionBean(RegionBean regionBean) {
		this.regionBean = regionBean;
	}

	public CircleBean getCircleBean() {
		return circleBean;
	}

	public void setCircleBean(CircleBean circleBean) {
		this.circleBean = circleBean;
	}

	public DivisionBean getDivisionBean() {
		return divisionBean;
	}

	public void setDivisionBean(DivisionBean divisionBean) {
		this.divisionBean = divisionBean;
	}

	public SubDivisionBean getSubDivisionBean() {
		return subDivisionBean;
	}

	public void setSubDivisionBean(SubDivisionBean subDivisionBean) {
		this.subDivisionBean = subDivisionBean;
	}

	public SectionBean getSectionBean() {
		return sectionBean;
	}

	public void setSectionBean(SectionBean sectionBean) {
		this.sectionBean = sectionBean;
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

	public String getPincode() {
		return pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	public String getCallSplRemarks() {
		return callSplRemarks;
	}

	public void setCallSplRemarks(String callSplRemarks) {
		this.callSplRemarks = callSplRemarks;
	}

	public MinnagamCallDetailsBean getMinnagamCallDetailsBean() {
		return minnagamCallDetailsBean;
	}

	public void setMinnagamCallDetailsBean(MinnagamCallDetailsBean minnagamCallDetailsBean) {
		this.minnagamCallDetailsBean = minnagamCallDetailsBean;
	}

	public Timestamp getIntUpdatedOn() {
		return intUpdatedOn;
	}

	public void setIntUpdatedOn(Timestamp intUpdatedOn) {
		this.intUpdatedOn = intUpdatedOn;
	}

	@Override
	public String toString() {
		return "ComplaintBean [id=" + id + ", complaintType=" + complaintType + ", description=" + description
				+ ", serviceNumber=" + serviceNumber + ", landmark=" + landmark + ", latitude=" + latitude
				+ ", longitude=" + longitude + ", image1=" + image1 + ", image2=" + image2 + ", device=" + device
				+ ", serviceName=" + serviceName + ", serviceAddress=" + serviceAddress + ", focComplaintId="
				+ focComplaintId + ", complaintCode=" + complaintCode + ", statusId=" + statusId + ", rating=" + rating
				+ ", publicUserBean=" + publicUserBean + ", rescueBean=" + rescueBean + ", categoryBean=" + categoryBean
				+ ", subCategoryBean=" + subCategoryBean + ", regionBean=" + regionBean + ", circleBean=" + circleBean
				+ ", divisionBean=" + divisionBean + ", subDivisionBean=" + subDivisionBean + ", sectionBean="
				+ sectionBean + ", createdOn=" + createdOn + ", updatedOn=" + updatedOn + ", applicationNo="
				+ applicationNo + ", tag=" + tag + ", receivedFrom=" + receivedFrom + ", alternateMobileNo="
				+ alternateMobileNo + ", complaintHistoryList=" + complaintHistoryList + ", pincode=" + pincode
				+ ", callSplRemarks=" + callSplRemarks + ", minnagamCallDetailsBean=" + minnagamCallDetailsBean
				+ ", intUpdatedOn=" + intUpdatedOn + "]";
	}

	
	
}
