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
@Table(name = "DISTRICT")
public class DistrictBean {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "DISTRICT_ID_SEQ")
	@Column(name = "ID")
	Integer id;

	@Column(name = "CODE")
	String code;
	
	@Column(name = "NAME")
	String name;
	
	@Column(name = "CIRCLE_CODE")
	String circleCode;
	
	@Column(name = "CIRCLE_NAME")
	String circleName;
	
	@Column(name = "SECTION_CODE")
	String sectionCode;
	
	@Column(name = "SECTION_NAME")
	String sectionName;

	@Column(name = "CREATED_ON", insertable = false, updatable = false)
	Timestamp createdOn;

	@Column(name = "UPDATED_ON", insertable = false, updatable = true)
	Timestamp updatedOn;
	
	@ManyToOne
	@JoinColumn(name = "REGION_ID")
	RegionBean regionBean;
	
	@ManyToOne
	@JoinColumn(name = "CIRCLE_ID")
	CircleBean circleBean;
	

	public CircleBean getCircleBean() {
		return circleBean;
	}

	public void setCircleBean(CircleBean circleBean) {
		this.circleBean = circleBean;
	}

	public RegionBean getRegionBean() {
		return regionBean;
	}

	public void setRegionBean(RegionBean regionBean) {
		this.regionBean = regionBean;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCircleCode() {
		return circleCode;
	}

	public void setCircleCode(String circleCode) {
		this.circleCode = circleCode;
	}

	public String getCircleName() {
		return circleName;
	}

	public void setCircleName(String circleName) {
		this.circleName = circleName;
	}

	public String getSectionCode() {
		return sectionCode;
	}

	public void setSectionCode(String sectionCode) {
		this.sectionCode = sectionCode;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
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
