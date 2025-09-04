package tneb.ccms.admin.valuebeans;

import java.math.BigDecimal;

public class ModeWiseAbstractValueBean {
	
	private String regionCode;
	private String regionName;
	private String circleCode;
	private String circleName;
	private String sectionCode;
	private String sectionName;
	private String divisionName;
	
	private BigDecimal webTotal;
	private BigDecimal webComp;
	private BigDecimal webPending;
	
	private BigDecimal mobileTotal;
	private BigDecimal mobileComp;
	private BigDecimal mobilePending;
	
	private BigDecimal adminTotal;
	private BigDecimal adminComp;
	private BigDecimal adminPending;
	
	private BigDecimal smsTotal;
	private BigDecimal smsComp;
	private BigDecimal smsPending;
	
	private BigDecimal miTotal;
	private BigDecimal miComp;
	private BigDecimal miPending;
	
	private String divisionId;
	private String subDivisionId;
	
	
	
	
	public ModeWiseAbstractValueBean(String regionCode, String regionName, String circleCode, String circleName,
			String sectionCode, String sectionName, String divisionName, BigDecimal webTotal, BigDecimal webComp,
			BigDecimal webPending, BigDecimal mobileTotal, BigDecimal mobileComp, BigDecimal mobilePending,
			BigDecimal adminTotal, BigDecimal adminComp, BigDecimal adminPending, BigDecimal smsTotal,
			BigDecimal smsComp, BigDecimal smsPending, BigDecimal miTotal, BigDecimal miComp, BigDecimal miPending,
			String divisionId, String subDivisionId) {
		super();
		this.regionCode = regionCode;
		this.regionName = regionName;
		this.circleCode = circleCode;
		this.circleName = circleName;
		this.sectionCode = sectionCode;
		this.sectionName = sectionName;
		this.divisionName = divisionName;
		this.webTotal = webTotal;
		this.webComp = webComp;
		this.webPending = webPending;
		this.mobileTotal = mobileTotal;
		this.mobileComp = mobileComp;
		this.mobilePending = mobilePending;
		this.adminTotal = adminTotal;
		this.adminComp = adminComp;
		this.adminPending = adminPending;
		this.smsTotal = smsTotal;
		this.smsComp = smsComp;
		this.smsPending = smsPending;
		this.miTotal = miTotal;
		this.miComp = miComp;
		this.miPending = miPending;
		this.divisionId = divisionId;
		this.subDivisionId = subDivisionId;
	}
	public String getDivisionId() {
		return divisionId;
	}
	public void setDivisionId(String divisionId) {
		this.divisionId = divisionId;
	}
	public String getSubDivisionId() {
		return subDivisionId;
	}
	public void setSubDivisionId(String subDivisionId) {
		this.subDivisionId = subDivisionId;
	}
	
	public ModeWiseAbstractValueBean() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String getRegionCode() {
		return regionCode;
	}
	public void setRegionCode(String regionCode) {
		this.regionCode = regionCode;
	}
	public String getRegionName() {
		return regionName;
	}
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
	public String getCircleCode() {
		return circleCode;
	}
	public void setCircleCode(String circleCode) {
		this.circleCode = circleCode;
	}
	public String getCircleName() {
		return circleName;
	}
	public void setCircleName(String circleName) {
		this.circleName = circleName;
	}
	public String getSectionCode() {
		return sectionCode;
	}
	public void setSectionCode(String sectionCode) {
		this.sectionCode = sectionCode;
	}
	public String getSectionName() {
		return sectionName;
	}
	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}
	public BigDecimal getWebTotal() {
		return webTotal;
	}
	public void setWebTotal(BigDecimal webTotal) {
		this.webTotal = webTotal;
	}
	public BigDecimal getWebComp() {
		return webComp;
	}
	public void setWebComp(BigDecimal webComp) {
		this.webComp = webComp;
	}
	public BigDecimal getWebPending() {
		return webPending;
	}
	public void setWebPending(BigDecimal webPending) {
		this.webPending = webPending;
	}
	public BigDecimal getMobileTotal() {
		return mobileTotal;
	}
	public void setMobileTotal(BigDecimal mobileTotal) {
		this.mobileTotal = mobileTotal;
	}
	public BigDecimal getMobileComp() {
		return mobileComp;
	}
	public void setMobileComp(BigDecimal mobileComp) {
		this.mobileComp = mobileComp;
	}
	public BigDecimal getMobilePending() {
		return mobilePending;
	}
	public void setMobilePending(BigDecimal mobilePending) {
		this.mobilePending = mobilePending;
	}
	public BigDecimal getAdminTotal() {
		return adminTotal;
	}
	public void setAdminTotal(BigDecimal adminTotal) {
		this.adminTotal = adminTotal;
	}
	public BigDecimal getAdminComp() {
		return adminComp;
	}
	public void setAdminComp(BigDecimal adminComp) {
		this.adminComp = adminComp;
	}
	public BigDecimal getAdminPending() {
		return adminPending;
	}
	public void setAdminPending(BigDecimal adminPending) {
		this.adminPending = adminPending;
	}
	public BigDecimal getSmsTotal() {
		return smsTotal;
	}
	public void setSmsTotal(BigDecimal smsTotal) {
		this.smsTotal = smsTotal;
	}
	public BigDecimal getSmsComp() {
		return smsComp;
	}
	public void setSmsComp(BigDecimal smsComp) {
		this.smsComp = smsComp;
	}
	public BigDecimal getSmsPending() {
		return smsPending;
	}
	public void setSmsPending(BigDecimal smsPending) {
		this.smsPending = smsPending;
	}
	public BigDecimal getMiTotal() {
		return miTotal;
	}
	public void setMiTotal(BigDecimal miTotal) {
		this.miTotal = miTotal;
	}
	public BigDecimal getMiComp() {
		return miComp;
	}
	public void setMiComp(BigDecimal miComp) {
		this.miComp = miComp;
	}
	public BigDecimal getMiPending() {
		return miPending;
	}
	public void setMiPending(BigDecimal miPending) {
		this.miPending = miPending;
	}
	public String getDivisionName() {
		return divisionName;
	}
	public void setDivisionName(String divisionName) {
		this.divisionName = divisionName;
	}

	
	

}
