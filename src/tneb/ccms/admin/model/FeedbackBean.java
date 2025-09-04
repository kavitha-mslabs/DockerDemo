package tneb.ccms.admin.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "COMPLAINT_FEEDBACK")
public class FeedbackBean {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "FEEDBACK_ID_SEQ")
	@Column(name = "FEEDBACK_ID")
	Integer feedback_id;
	
	
	@Column(name = "COMPLAINT_ID")
	Integer complaint_id;
	
	

	@Column(name = "REMARKS")
	String remarks;
	
	@Column(name = "FEEDBACK")
	Integer feedback;
	

	@Column(name = "PHONE_NUMBER")
	String phone_number;
	
	@Column(name = "FEEDBACK_DATETIME")
	Timestamp feedback_datetime;

	public Integer getFeedback_id() {
		return feedback_id;
	}

	public void setFeedback_id(Integer feedback_id) {
		this.feedback_id = feedback_id;
	}

	public Integer getComplaint_id() {
		return complaint_id;
	}

	public void setComplaint_id(Integer complaint_id) {
		this.complaint_id = complaint_id;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Integer getFeedback() {
		return feedback;
	}

	public void setFeedback(Integer feedback) {
		this.feedback = feedback;
	}

	public String getPhone_number() {
		return phone_number;
	}

	public void setPhone_number(String phone_number) {
		this.phone_number = phone_number;
	}

	public Timestamp getFeedback_datetime() {
		return feedback_datetime;
	}

	public void setFeedback_datetime(Timestamp feedback_datetime) {
		this.feedback_datetime = feedback_datetime;
	}
	
	
	
}


