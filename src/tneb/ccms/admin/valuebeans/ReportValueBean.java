package tneb.ccms.admin.valuebeans;

import java.io.Serializable;

public class ReportValueBean implements Serializable {

	private static final long serialVersionUID = -5138649036131635371L;
	
	private Integer serialNumber;
	
	private String circleName;
	
	private String divisionName;
	
	private Integer totalCompRegistered = 0;
	
	private Integer totalCompRegisteredOneHour = 0;
	
	private Integer totalCompRegisteredThreeHour = 0;
	
	private Integer totalCompRegisteredSixHour = 0;
	
	private Integer totalCompRegisteredTwelveHour = 0;
	
	private Integer totalCompRegisteredOneDay = 0;
	
	private Integer compPendingCurrentDay = 0;
	
	private Integer compPending = 0;
	
	private Integer compResolved = 0;

	public Integer getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(Integer serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getCircleName() {
		return circleName;
	}

	public void setCircleName(String circleName) {
		this.circleName = circleName;
	}

	public Integer getTotalCompRegistered() {
		return totalCompRegistered;
	}

	public void setTotalCompRegistered(Integer totalCompRegistered) {
		this.totalCompRegistered = totalCompRegistered;
	}

	public Integer getTotalCompRegisteredOneHour() {
		return totalCompRegisteredOneHour;
	}

	public void setTotalCompRegisteredOneHour(Integer totalCompRegisteredOneHour) {
		this.totalCompRegisteredOneHour = totalCompRegisteredOneHour;
	}

	public Integer getTotalCompRegisteredThreeHour() {
		return totalCompRegisteredThreeHour;
	}

	public void setTotalCompRegisteredThreeHour(Integer totalCompRegisteredThreeHour) {
		this.totalCompRegisteredThreeHour = totalCompRegisteredThreeHour;
	}

	public Integer getTotalCompRegisteredSixHour() {
		return totalCompRegisteredSixHour;
	}

	public void setTotalCompRegisteredSixHour(Integer totalCompRegisteredSixHour) {
		this.totalCompRegisteredSixHour = totalCompRegisteredSixHour;
	}

	public Integer getTotalCompRegisteredTwelveHour() {
		return totalCompRegisteredTwelveHour;
	}

	public void setTotalCompRegisteredTwelveHour(Integer totalCompRegisteredTwelveHour) {
		this.totalCompRegisteredTwelveHour = totalCompRegisteredTwelveHour;
	}

	public Integer getTotalCompRegisteredOneDay() {
		return totalCompRegisteredOneDay;
	}

	public void setTotalCompRegisteredOneDay(Integer totalCompRegisteredOneDay) {
		this.totalCompRegisteredOneDay = totalCompRegisteredOneDay;
	}

	public Integer getCompPending() {
		return compPending;
	}

	public void setCompPending(Integer compPending) {
		this.compPending = compPending;
	}

	public Integer getCompPendingCurrentDay() {
		return compPendingCurrentDay;
	}

	public void setCompPendingCurrentDay(Integer compPendingCurrentDay) {
		this.compPendingCurrentDay = compPendingCurrentDay;
	}

	public String getDivisionName() {
		return divisionName;
	}

	public void setDivisionName(String divisionName) {
		this.divisionName = divisionName;
	}

	public Integer getCompResolved() {
		return compResolved;
	}

	public void setCompResolved(Integer compResolved) {
		this.compResolved = compResolved;
	}
	
}
