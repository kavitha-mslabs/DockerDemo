package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.sql.Timestamp;

import tneb.ccms.admin.model.PublicUserBean;

public class PublicUserValueBean implements Serializable {

	private static final long serialVersionUID = 1769688405708583500L;

	Integer id;
	
	String firstName;
	
	String lastName;
	
	String mobile;
	
	String email;
	
	String password;
	
	String address;
	
	String landMark;
	
	Timestamp createdOn;

	Timestamp updatedOn;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
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
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	

	public String getLandMark() {
		return landMark;
	}

	public void setLandMark(String landMark) {
		this.landMark = landMark;
	}

	public static PublicUserValueBean convertPublicUserBeanToPublicUserValueBean(PublicUserBean publicUserBean) {
		
		PublicUserValueBean publicUserValueBean = new PublicUserValueBean();
		
		publicUserValueBean.setId(publicUserBean.getId());
		publicUserValueBean.setCreatedOn(publicUserBean.getCreatedOn());
		publicUserValueBean.setEmail(publicUserBean.getEmail());
		publicUserValueBean.setFirstName(publicUserBean.getFirstName());
		publicUserValueBean.setLastName(publicUserBean.getLastName());
		publicUserValueBean.setMobile(publicUserBean.getMobile());
		publicUserValueBean.setPassword(publicUserBean.getPassword());
		publicUserValueBean.setUpdatedOn(publicUserBean.getUpdatedOn());
		publicUserValueBean.setAddress(publicUserBean.getAddress());
		publicUserValueBean.setLandMark(publicUserBean.getLandMark());
		
		return publicUserValueBean;
	}
	
	public static PublicUserBean convertPublicUserValueBeanToPublicUserBean(PublicUserValueBean publicUserValueBean) {
		
		PublicUserBean publicUserBean = new PublicUserBean();
		
		publicUserBean.setId(publicUserValueBean.getId());
		publicUserBean.setCreatedOn(publicUserValueBean.getCreatedOn());
		publicUserBean.setEmail(publicUserValueBean.getEmail());
		publicUserBean.setFirstName(publicUserValueBean.getFirstName());
		publicUserBean.setLastName(publicUserValueBean.getLastName());
		publicUserBean.setMobile(publicUserValueBean.getMobile());
		publicUserBean.setPassword(publicUserValueBean.getPassword());
		publicUserBean.setUpdatedOn(publicUserValueBean.getUpdatedOn());
		publicUserBean.setAddress(publicUserValueBean.getAddress());
		publicUserBean.setLandMark(publicUserValueBean.getLandMark());
		
		return publicUserBean;
	}
	
}
