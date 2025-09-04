package tneb.ccms.admin.valuebeans;

import java.math.BigDecimal;

public class ComparisonSectionValueBean {
	

	private String cirCode;
	private String secCode;
	private String circleName;
	private String sectionName;
	
	private BigDecimal tot1;
	private BigDecimal cpl1;
	private BigDecimal pend1;
	
	private BigDecimal tot2;
	private BigDecimal cpl2;
	private BigDecimal pend2;
	
	private String divisionName;
	
	private String divisionId;
	private String subDivisionId;
	
	

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

	public String getCirCode() {
		return cirCode;
	}

	public void setCirCode(String cirCode) {
		this.cirCode = cirCode;
	}

	public String getSecCode() {
		return secCode;
	}

	public void setSecCode(String secCode) {
		this.secCode = secCode;
	}

	public String getCircleName() {
		return circleName;
	}

	public void setCircleName(String circleName) {
		this.circleName = circleName;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
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

	public String getDivisionName() {
		return divisionName;
	}

	public void setDivisionName(String divisionName) {
		this.divisionName = divisionName;
	}


	public ComparisonSectionValueBean(String cirCode, String secCode, String circleName, String sectionName,
			BigDecimal tot1, BigDecimal cpl1, BigDecimal pend1, BigDecimal tot2, BigDecimal cpl2, BigDecimal pend2,
			String divisionName, String divisionId, String subDivisionId) {
		super();
		this.cirCode = cirCode;
		this.secCode = secCode;
		this.circleName = circleName;
		this.sectionName = sectionName;
		this.tot1 = tot1;
		this.cpl1 = cpl1;
		this.pend1 = pend1;
		this.tot2 = tot2;
		this.cpl2 = cpl2;
		this.pend2 = pend2;
		this.divisionName = divisionName;
		this.divisionId = divisionId;
		this.subDivisionId = subDivisionId;
	}

	public ComparisonSectionValueBean() {
		super();
	}
	
	
	
	

}
