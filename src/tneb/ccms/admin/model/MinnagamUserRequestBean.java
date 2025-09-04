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
@Table(name = "MINNAGAM_USER_REQUEST")
// Live - @Table(name = "MINNAGAM_USER_REQUEST" , schema = "TANGEDCODEMO")
public class MinnagamUserRequestBean {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "MINNAGAM_USER_REQUEST_ID_SEQ")
	// Live - @GeneratedValue(strategy = GenerationType.AUTO, generator = "TANGEDCODEMO.MINNAGAM_USER_REQUEST_ID_SEQ")
	@Column(name = "ID")
	Integer id;

	@Column(name = "AGENT_ROLE")
	String agentRole;
	
	@ManyToOne
	@JoinColumn(name = "CIRCLE_ID")
	CircleBean circleBean;
	
	@Column(name = "USER_NAME")
	String userName;
	
	@Column(name = "PASSWORD")
	String password;
	
	@Column(name = "NAME")
	String name;
	
	@Column(name = "MOBILE_NUMBER")
	String mobileNumber;
	
	@Column(name = "EMAIL_ID")
	String emailId;

	@Column(name = "CREATED_ON", insertable = true, updatable = false)
	Timestamp createdOn;

	@Column(name = "UPDATED_ON", insertable = true, updatable = true)
	Timestamp updatedOn;
	
	@ManyToOne
	@JoinColumn(name = "ROLE_ID")
	CallCenterRoleBean roleBean;
	
	@Column(name = "STATUS_ID")
	Integer statusId;
	
	@Column(name = "CIRCLE_CODE", length = 10)
    private String circleCode;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getAgentRole() {
		return agentRole;
	}

	public void setAgentRole(String agentRole) {
		this.agentRole = agentRole;
	}

	public CircleBean getCircleBean() {
		return circleBean;
	}

	public void setCircleBean(CircleBean circleBean) {
		this.circleBean = circleBean;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
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

	public CallCenterRoleBean getRoleBean() {
		return roleBean;
	}

	public void setRoleBean(CallCenterRoleBean roleBean) {
		this.roleBean = roleBean;
	}

	public Integer getStatusId() {
		return statusId;
	}

	public void setStatusId(Integer statusId) {
		this.statusId = statusId;
	}

	public String getCircleCode() {
		return circleCode;
	}

	public void setCircleCode(String circleCode) {
		this.circleCode = circleCode;
	}

	
}
