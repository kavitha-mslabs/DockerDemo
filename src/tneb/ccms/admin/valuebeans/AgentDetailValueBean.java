package tneb.ccms.admin.valuebeans;

public class AgentDetailValueBean {

	private String userName;
    private String loginTime;
    private String logoutTime;
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getLoginTime() {
		return loginTime;
	}
	public void setLoginTime(String loginTime) {
		this.loginTime = loginTime;
	}
	public String getLogoutTime() {
		return logoutTime;
	}
	public void setLogoutTime(String logoutTime) {
		this.logoutTime = logoutTime;
	}
	public AgentDetailValueBean(String userName, String loginTime, String logoutTime) {
		super();
		this.userName = userName;
		this.loginTime = loginTime;
		this.logoutTime = logoutTime;
	}
	public AgentDetailValueBean() {
		super();
	}
    
    
}
