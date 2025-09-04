package tneb.ccms.admin.valuebeans;

public class RepeatedCallComplaintValueBean {
	
	private String contactNumber;
	private Long noOfTimesOfCall;
	
	public RepeatedCallComplaintValueBean(String contactNumber, Long noOfTimesOfCall) {
		super();
		this.contactNumber = contactNumber;
		this.noOfTimesOfCall = noOfTimesOfCall;
	}
	public RepeatedCallComplaintValueBean() {
		super();
	}
	public String getContactNumber() {
		return contactNumber;
	}
	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}
	public Long getNoOfTimesOfCall() {
		return noOfTimesOfCall;
	}
	public void setNoOfTimesOfCall(Long noOfTimesOfCall) {
		this.noOfTimesOfCall = noOfTimesOfCall;
	}
	
	

}
