package tneb.ccms.admin.valuebeans;

public class CurrentDateComplaintsValueBean {
	
	private Integer sectionId;
	private String sectionName;
	private String divisionName;
	private String circleName;
	private Long receivedComplaints;
	
	private Integer regionId;
	private Integer circleId;
	private Integer divisionId;
	private Integer subDivisionId;

	
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
	public String getSectionName() {
		return sectionName;
	}
	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}
	public String getDivisionName() {
		return divisionName;
	}
	public void setDivisionName(String divisionName) {
		this.divisionName = divisionName;
	}
	public String getCircleName() {
		return circleName;
	}
	public void setCircleName(String circleName) {
		this.circleName = circleName;
	}
	public Long getReceivedComplaints() {
		return receivedComplaints;
	}
	public void setReceivedComplaints(Long receivedComplaints) {
		this.receivedComplaints = receivedComplaints;
	}
	
	public CurrentDateComplaintsValueBean(Integer sectionId, String sectionName, String divisionName, String circleName,
			Long receivedComplaints, Integer regionId, Integer circleId, Integer divisionId, Integer subDivisionId) {
		super();
		this.sectionId = sectionId;
		this.sectionName = sectionName;
		this.divisionName = divisionName;
		this.circleName = circleName;
		this.receivedComplaints = receivedComplaints;
		this.regionId = regionId;
		this.circleId = circleId;
		this.divisionId = divisionId;
		this.subDivisionId = subDivisionId;
	}
	public CurrentDateComplaintsValueBean() {
		super();
	}
	
	

}
