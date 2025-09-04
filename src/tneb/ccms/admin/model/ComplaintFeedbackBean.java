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
@Table(name = "COMP_FEEDBACK")
public class ComplaintFeedbackBean {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "COMP_FEEDBACK_ID_SEQ")
	@Column(name = "ID")
	Integer id;
	
	@OneToOne
	@JoinColumn(name = "COMP_ID")
	ComplaintBean complaintBean;
	
	@Column(name = "ENTRYDT", insertable = true, updatable = false)
	Timestamp entryDt;
	
	@Column(name = "REMARKS")
	String remarks;
	
	@Column(name = "RATING")
	Integer rating;
	
	/*@Column(name = "PHONE_NUMBER")
	String phone_number;
	
	

	public String getPhone_number() {
		return phone_number;
	}

	public void setPhone_number(String phone_number) {
		this.phone_number = phone_number;
	}*/

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public ComplaintBean getComplaintBean() {
		return complaintBean;
	}

	public void setComplaintBean(ComplaintBean complaintBean) {
		this.complaintBean = complaintBean;
	}

	public Timestamp getEntryDt() {
		return entryDt;
	}

	public void setEntryDt(Timestamp entryDt) {
		this.entryDt = entryDt;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}
	
	
}
