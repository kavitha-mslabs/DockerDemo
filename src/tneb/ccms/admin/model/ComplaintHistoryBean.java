package tneb.ccms.admin.model;
 
import java.sql.Timestamp;
 
import javax.persistence.Column;

import javax.persistence.Entity;

import javax.persistence.GeneratedValue;

import javax.persistence.GenerationType;

import javax.persistence.Id;

import javax.persistence.JoinColumn;

import javax.persistence.ManyToOne;

import javax.persistence.Table;
 
@Entity

@Table(name = "COMPLAINT_HISTORY")

public class ComplaintHistoryBean {

	@Id

	@GeneratedValue(strategy = GenerationType.AUTO, generator = "COMPLAINT_HISTORY_ID_SEQ")

	@Column(name = "ID")

	Integer id;

	@Column(name = "DESCRIPTION")

	String description;

	@Column(name = "STATUS_ID")

	Integer statusId;

	@ManyToOne

	@JoinColumn(name = "COMPLAINT_ID")

	ComplaintBean complaintBean;

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

	@ManyToOne

	@JoinColumn(name = "ADMIN_USER_ID")

	AdminUserBean adminUserBean;

	@ManyToOne

	@JoinColumn(name = "PUBLIC_USER_ID")

	PublicUserBean publicUserBean;

	@ManyToOne

	@JoinColumn(name = "CALL_USER_ID")

	CallCenterUserBean callCenterUserBean;

	@Column(name = "CREATED_ON", insertable = false, updatable = false)

	Timestamp createdOn;

	@Column(name = "UPDATED_ON", insertable = false, updatable = true)

	Timestamp  updatedOn;
 
	@Column(name = "CLOSURE_REASON")

	String reason;

	@Column(name = "COMP_CL_USER")

	String compClUser;
 
	public String getCompClUser() {

		return compClUser;

	}
 
	public void setCompClUser(String compClUser) {

		this.compClUser = compClUser;

	}
 
	

 
	public String getReason() {

		return reason;

	}
 
	public void setReason(String reason) {

		this.reason = reason;

	}
 
	public Integer getId() {

		return id;

	}
 
	public void setId(Integer id) {

		this.id = id;

	}
 
	public String getDescription() {

		return description;

	}
 
	public void setDescription(String description) {

		this.description = description;

	}
 
	public Integer getStatusId() {

		return statusId;

	}
 
	public void setStatusId(Integer statusId) {

		this.statusId = statusId;

	}
 
	public ComplaintBean getComplaintBean() {

		return complaintBean;

	}
 
	public void setComplaintBean(ComplaintBean complaintBean) {

		this.complaintBean = complaintBean;

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
 
	public AdminUserBean getAdminUserBean() {

		return adminUserBean;

	}
 
	public void setAdminUserBean(AdminUserBean adminUserBean) {

		this.adminUserBean = adminUserBean;

	}
 
	public PublicUserBean getPublicUserBean() {

		return publicUserBean;

	}
 
	public void setPublicUserBean(PublicUserBean publicUserBean) {

		this.publicUserBean = publicUserBean;

	}
 
	public CallCenterUserBean getCallCenterUserBean() {

		return callCenterUserBean;

	}
 
	public void setCallCenterUserBean(CallCenterUserBean callCenterUserBean) {

		this.callCenterUserBean = callCenterUserBean;

	}
 
	

}

 