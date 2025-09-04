package tneb.ccms.admin.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CLOSURE_REASON")
public class ClosureReasonBean {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "CLOSURE_REASON_ID_SEQ")
	@Column(name = "ID")
	Integer id;

	@Column(name = "COMPLAINT_CODE")
	Integer complaintCode;
	
	@Column(name = "REASON")
	String closureReason;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getComplaintCode() {
		return complaintCode;
	}

	public void setComplaintCode(Integer complaintCode) {
		this.complaintCode = complaintCode;
	}

	public String getClosureReason() {
		return closureReason;
	}

	public void setClosureReason(String closureReason) {
		this.closureReason = closureReason;
	}

}
