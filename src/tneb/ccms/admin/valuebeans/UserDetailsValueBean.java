package tneb.ccms.admin.valuebeans;

public class UserDetailsValueBean {
	private String userName;
	private String name;
	private String email;
	private String mobileNumber;
	private String updatedDate;
	private String toDate;
	private String circleCode;
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public String getUpdatedDate() {
		return updatedDate;
	}
	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}
	public String getToDate() {
		return toDate;
	}
	public void setToDate(String toDate) {
		this.toDate = toDate;
	}
	public String getCircleCode() {
		return circleCode;
	}
	public void setCircleCode(String circleCode) {
		this.circleCode = circleCode;
	}
	public UserDetailsValueBean(String userName, String name, String email, String mobileNumber, String updatedDate,
			String toDate, String circleCode) {
		super();
		this.userName = userName;
		this.name = name;
		this.email = email;
		this.mobileNumber = mobileNumber;
		this.updatedDate = updatedDate;
		this.toDate = toDate;
		this.circleCode = circleCode;
	}
	public UserDetailsValueBean() {
		super();
	}
	
	
	
	

}
