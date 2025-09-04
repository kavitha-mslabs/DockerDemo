package tneb.ccms.admin.valuebeans;

import java.math.BigDecimal;

public class AgentWiseComplaintsValueBean {
	
	private String agent;
	private BigDecimal complaints;
	public String getAgent() {
		return agent;
	}
	public void setAgent(String agent) {
		this.agent = agent;
	}
	public BigDecimal getComplaints() {
		return complaints;
	}
	public void setComplaints(BigDecimal complaints) {
		this.complaints = complaints;
	}
	
	

}
