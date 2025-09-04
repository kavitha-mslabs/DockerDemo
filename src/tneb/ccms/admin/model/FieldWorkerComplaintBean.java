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
@Table(name = "FIELD_WORKER_COMP")
public class FieldWorkerComplaintBean {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "FIELD_WORKER_COMP_ID_SEQ")
	@Column(name = "ID")
	Integer id;
	
	@Column(name = "FIELD_WORKER_ID")
	Integer fieldWorkerId;

	@Column(name = "STATUS_ID")
	Integer statusId;
	
	@Column(name = "TRANSFER_DATE")
	Timestamp transferDate;
	
	@Column(name = "TRANSFER_FROM")
	Integer transferFrom;
	
	@ManyToOne
	@JoinColumn(name = "COMPLAINT_ID")
	ComplaintBean complaintBean;

	@Column(name = "CREATED_ON", insertable = false, updatable = false)
	Timestamp createdOn;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getFieldWorkerId() {
		return fieldWorkerId;
	}

	public void setFieldWorkerId(Integer fieldWorkerId) {
		this.fieldWorkerId = fieldWorkerId;
	}

	public Integer getStatusId() {
		return statusId;
	}

	public void setStatusId(Integer statusId) {
		this.statusId = statusId;
	}

	public Timestamp getTransferDate() {
		return transferDate;
	}

	public void setTransferDate(Timestamp transferDate) {
		this.transferDate = transferDate;
	}

	public Integer getTransferFrom() {
		return transferFrom;
	}

	public void setTransferFrom(Integer transferFrom) {
		this.transferFrom = transferFrom;
	}

	public ComplaintBean getComplaintBean() {
		return complaintBean;
	}

	public void setComplaintBean(ComplaintBean complaintBean) {
		this.complaintBean = complaintBean;
	}

	public Timestamp getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

}

