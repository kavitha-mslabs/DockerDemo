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
@Table(name = "SUB_DIVISION")
public class SubDivisionBean {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "SUB_DIVISION_ID_SEQ")
	@Column(name = "ID")
	Integer id;

	@Column(name = "NAME")
	String name;

	@Column(name = "CODE")
	String code;

	@Column(name = "ADDRESS")
	String address;

	@Column(name = "PHONE_NUMBER")
	String phone;

	@Column(name = "MOBILE")
	String mobile;

	@Column(name = "EMAIL")
	String email;

	@ManyToOne
	@JoinColumn(name = "REGION_ID")
	RegionBean regionBean;

	@ManyToOne
	@JoinColumn(name = "CIRCLE_ID")
	CircleBean circleBean;

	@ManyToOne
	@JoinColumn(name = "DIVISION_ID")
	DivisionBean divisionBean;

	@Column(name = "CREATED_ON", insertable = false, updatable = false)
	Timestamp createdOn;

	@Column(name = "UPDATED_ON", insertable = false, updatable = true)
	Timestamp updatedOn;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
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
	
}
