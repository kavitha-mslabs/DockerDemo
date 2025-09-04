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
@Table(name = "CALL_CENTER_USER")
public class CallCenterUserBean {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "CALL_CENTER_USER_ID_SEQ")
	@Column(name = "ID")
	Integer id;
	
	@Column(name = "NAME")
	String name;
	
	@Column(name = "USER_NAME")
	String userName;
	
	@Column(name = "MOBILE")
	String mobile;
	
	@Column(name = "EMAIL")
	String email;
	
	@Column(name = "PASSWORD")
	String password;
	
	@ManyToOne
	@JoinColumn(name = "ROLE_ID")
	CallCenterRoleBean callCenterRoleBean;
	
	@Column(name = "CREATED_ON", insertable = false, updatable = false)
	Timestamp createdOn;

	@Column(name = "UPDATED_ON", insertable = false, updatable = true)
	Timestamp updatedOn;

	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public CallCenterRoleBean getCallCenterRoleBean() {
		return callCenterRoleBean;
	}

	public void setCallCenterRoleBean(CallCenterRoleBean callCenterRoleBean) {
		this.callCenterRoleBean = callCenterRoleBean;
	}

}
