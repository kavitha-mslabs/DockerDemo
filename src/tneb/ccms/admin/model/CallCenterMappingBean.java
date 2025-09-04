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
@Table(name = "CALL_CENTER_MAPPING")
public class CallCenterMappingBean {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "CALL_CENTER_MAPPING_ID_SEQ")
	@Column(name = "ID")
	Integer id;

	@ManyToOne
	@JoinColumn(name = "CALL_CENTER_USER_ID")
	CallCenterUserBean callCenterUserBean;
	
	@ManyToOne
	@JoinColumn(name = "CIRCLE_ID")
	CircleBean circleBean;

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

	
	public CallCenterUserBean getCallCenterUserBean() {
		return callCenterUserBean;
	}

	public void setCallCenterUserBean(CallCenterUserBean callCenterUserBean) {
		this.callCenterUserBean = callCenterUserBean;
	}

	public CircleBean getCircleBean() {
		return circleBean;
	}

	public void setCircleBean(CircleBean circleBean) {
		this.circleBean = circleBean;
	}

	

	
	
}

