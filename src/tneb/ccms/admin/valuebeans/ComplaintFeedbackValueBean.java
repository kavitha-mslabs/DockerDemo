package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.sql.Timestamp;

import tneb.ccms.admin.model.ComplaintFeedbackBean;

public class ComplaintFeedbackValueBean implements Serializable{
	
	private static final long serialVersionUID = 8189873998096032848L;

	private Integer id;
	
	private Integer complaintId;
	
	private Timestamp entryDt;
	
	private String remarks;
	
	private Integer rating;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getComplaintId() {
		return complaintId;
	}

	public void setComplaintId(Integer complaintId) {
		this.complaintId = complaintId;
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
	
	public static ComplaintFeedbackValueBean convertBeanToValueBean(ComplaintFeedbackBean complaintFeedbackBean ) {
		
		ComplaintFeedbackValueBean complaintFeedbackValueBean = new ComplaintFeedbackValueBean();
		
		complaintFeedbackValueBean.setId(complaintFeedbackBean.getId());
		complaintFeedbackValueBean.setComplaintId(complaintFeedbackBean.getComplaintBean().getId());
		complaintFeedbackValueBean.setEntryDt(complaintFeedbackBean.getEntryDt());
		complaintFeedbackValueBean.setRating(complaintFeedbackBean.getRating());
		complaintFeedbackValueBean.setRemarks(complaintFeedbackBean.getRemarks());
		
		return complaintFeedbackValueBean;
		 
	}
	
	 public static ComplaintFeedbackBean convertValueBeanToBean(ComplaintFeedbackValueBean valueBean) {
		 
		 ComplaintFeedbackBean complaintFeedbackBean = new ComplaintFeedbackBean();
		 
		 complaintFeedbackBean.setId(valueBean.getId());
		 complaintFeedbackBean.setEntryDt(valueBean.getEntryDt());
		 complaintFeedbackBean.setRating(valueBean.getRating());
		 complaintFeedbackBean.setRemarks(valueBean.getRemarks());
		 
		 return complaintFeedbackBean;
	 }
	
}
