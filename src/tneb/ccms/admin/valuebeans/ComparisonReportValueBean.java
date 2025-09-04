package tneb.ccms.admin.valuebeans;

import java.math.BigDecimal;

public class ComparisonReportValueBean {
	
	
	private String regCode;
	private String cirCode;
	private String regionName;
	private String circleName;
	
	private BigDecimal tot1;
	private BigDecimal cpl1;
	private BigDecimal pend1;
	
	private BigDecimal tot2;
	private BigDecimal cpl2;
	private BigDecimal pend2;
	public String getRegCode() {
		return regCode;
	}
	public void setRegCode(String regCode) {
		this.regCode = regCode;
	}
	public String getCirCode() {
		return cirCode;
	}
	public void setCirCode(String cirCode) {
		this.cirCode = cirCode;
	}
	public String getRegionName() {
		return regionName;
	}
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
	public String getCircleName() {
		return circleName;
	}
	public void setCircleName(String circleName) {
		this.circleName = circleName;
	}
	public BigDecimal getTot1() {
		return tot1;
	}
	public void setTot1(BigDecimal tot1) {
		this.tot1 = tot1;
	}
	public BigDecimal getCpl1() {
		return cpl1;
	}
	public void setCpl1(BigDecimal cpl1) {
		this.cpl1 = cpl1;
	}
	public BigDecimal getPend1() {
		return pend1;
	}
	public void setPend1(BigDecimal pend1) {
		this.pend1 = pend1;
	}
	public BigDecimal getTot2() {
		return tot2;
	}
	public void setTot2(BigDecimal tot2) {
		this.tot2 = tot2;
	}
	public BigDecimal getCpl2() {
		return cpl2;
	}
	public void setCpl2(BigDecimal cpl2) {
		this.cpl2 = cpl2;
	}
	public BigDecimal getPend2() {
		return pend2;
	}
	public void setPend2(BigDecimal pend2) {
		this.pend2 = pend2;
	}
	public ComparisonReportValueBean(String regCode, String cirCode, String regionName, String circleName,
			BigDecimal tot1, BigDecimal cpl1, BigDecimal pend1, BigDecimal tot2, BigDecimal cpl2, BigDecimal pend2) {
		super();
		this.regCode = regCode;
		this.cirCode = cirCode;
		this.regionName = regionName;
		this.circleName = circleName;
		this.tot1 = tot1;
		this.cpl1 = cpl1;
		this.pend1 = pend1;
		this.tot2 = tot2;
		this.cpl2 = cpl2;
		this.pend2 = pend2;
	}
	public ComparisonReportValueBean() {
		super();
	}
	
	
	

}
