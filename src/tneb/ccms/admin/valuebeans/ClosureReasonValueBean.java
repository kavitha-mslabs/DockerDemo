package tneb.ccms.admin.valuebeans;

import java.io.Serializable;

import tneb.ccms.admin.model.ClosureReasonBean;

public class ClosureReasonValueBean implements Serializable {
	
	private static final long serialVersionUID = -908695235235827306L;

	Integer id;

	Integer complaintCode;

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
	
	public static ClosureReasonValueBean convertClosureReasonBeanToClosureReasonValueBean(ClosureReasonBean closureReasonBean) {
		
		ClosureReasonValueBean closureReasonValueBean = new ClosureReasonValueBean();
		
		closureReasonValueBean.setId(closureReasonBean.getId());
		closureReasonValueBean.setClosureReason(closureReasonBean.getClosureReason());
		closureReasonValueBean.setComplaintCode(closureReasonBean.getComplaintCode());
		
		return closureReasonValueBean;
		
	}

	public static ClosureReasonBean convertClosureReasonValueBeanToClosureReasonBean(ClosureReasonValueBean closureReasonValueBean) {
		
		ClosureReasonBean closureReasonBean = new ClosureReasonBean();
		
		closureReasonBean.setId(closureReasonValueBean.getId());
		closureReasonBean.setClosureReason(closureReasonValueBean.getClosureReason());
		closureReasonBean.setComplaintCode(closureReasonValueBean.getComplaintCode());
		
		return closureReasonBean;
		
	}
}
