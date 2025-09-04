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
@Table(name = "CATEGORY")
public class CategoryBean {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "CATEGORY_ID_SEQ")
	@Column(name = "ID")
	Integer id;

	@Column(name = "NAME")
	String name;
	
	@Column(name = "ACTIVE_FLAG")
	String activeFlag;

	@ManyToOne
	@JoinColumn(name = "RESCUE_ID")
	RescueBean rescueBean;

	@Column(name = "CREATED_ON", insertable = false, updatable = false)
	Timestamp createdOn;

	@Column(name = "UPDATED_ON", insertable = false, updatable = true)
	Timestamp updatedOn;
	
	@Column(name = "CODE")
	String code;
	
	@Column(name = "COMPLAINT_CODE")
	Integer complaint_code;

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

	public RescueBean getRescueBean() {
		return rescueBean;
	}

	public void setRescueBean(RescueBean rescueBean) {
		this.rescueBean = rescueBean;
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

	public String getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(String activeFlag) {
		this.activeFlag = activeFlag;
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Integer getComplaint_code() {
		return complaint_code;
	}

	public void setComplaint_code(Integer complaint_code) {
		this.complaint_code = complaint_code;
	}

}
