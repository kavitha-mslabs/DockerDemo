package tneb.ccms.admin.valuebeans;

public class OutagesReportValueBean {
	 private String ssName;
	 private String feederName;
	 private String dtName;
	 private String maintainDate;
	 private String fromTime;
	 private String toTime;
	 private String outageType;
	 private String reason;
	 private String enteredThrough;
	 private String circleAndSection;
	 
	 private Integer regionId;
	 private Integer circleId;
	 private Integer divisionId;
	 private Integer subDivisionId;
	 private Integer sectionId;

	 
	
	public OutagesReportValueBean(String ssName, String feederName, String dtName, String maintainDate, String fromTime,
			String toTime, String outageType, String reason, String enteredThrough, String circleAndSection,
			Integer regionId, Integer circleId, Integer divisionId, Integer subDivisionId, Integer sectionId) {
		super();
		this.ssName = ssName;
		this.feederName = feederName;
		this.dtName = dtName;
		this.maintainDate = maintainDate;
		this.fromTime = fromTime;
		this.toTime = toTime;
		this.outageType = outageType;
		this.reason = reason;
		this.enteredThrough = enteredThrough;
		this.circleAndSection = circleAndSection;
		this.regionId = regionId;
		this.circleId = circleId;
		this.divisionId = divisionId;
		this.subDivisionId = subDivisionId;
		this.sectionId = sectionId;
	}
	public Integer getRegionId() {
		return regionId;
	}
	 public void setRegionId(Integer regionId) {
		 this.regionId = regionId;
	 }
	 public Integer getCircleId() {
		 return circleId;
	 }
	 public void setCircleId(Integer circleId) {
		 this.circleId = circleId;
	 }
	 public Integer getDivisionId() {
		 return divisionId;
	 }
	 public void setDivisionId(Integer divisionId) {
		 this.divisionId = divisionId;
	 }
	 public Integer getSubDivisionId() {
		 return subDivisionId;
	 }
	 public void setSubDivisionId(Integer subDivisionId) {
		 this.subDivisionId = subDivisionId;
	 }
	 public Integer getSectionId() {
		 return sectionId;
	 }
	 public void setSectionId(Integer sectionId) {
		 this.sectionId = sectionId;
	 }
	public OutagesReportValueBean() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String getSsName() {
		return ssName;
	}
	public void setSsName(String ssName) {
		this.ssName = ssName;
	}
	public String getFeederName() {
		return feederName;
	}
	public void setFeederName(String feederName) {
		this.feederName = feederName;
	}
	public String getDtName() {
		return dtName;
	}
	public void setDtName(String dtName) {
		this.dtName = dtName;
	}
	public String getMaintainDate() {
		return maintainDate;
	}
	public void setMaintainDate(String maintainDate) {
		this.maintainDate = maintainDate;
	}
	public String getFromTime() {
		return fromTime;
	}
	public void setFromTime(String fromTime) {
		this.fromTime = fromTime;
	}
	public String getToTime() {
		return toTime;
	}
	public void setToTime(String toTime) {
		this.toTime = toTime;
	}
	public String getOutageType() {
		return outageType;
	}
	public void setOutageType(String outageType) {
		this.outageType = outageType;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getEnteredThrough() {
		return enteredThrough;
	}
	public void setEnteredThrough(String enteredThrough) {
		this.enteredThrough = enteredThrough;
	}
	public String getCircleAndSection() {
		return circleAndSection;
	}
	public void setCircleAndSection(String circleAndSection) {
		this.circleAndSection = circleAndSection;
	}
	 
	 
	 

}
