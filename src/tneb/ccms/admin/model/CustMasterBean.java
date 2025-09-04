package tneb.ccms.admin.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CUSTMASTER")
public class CustMasterBean {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "REGCUSID")
	private Long regCusId;

	@Column(name = "CUSCODE", nullable = false, length = 20)
	private String cusCode;

	@Column(name = "CNAME", nullable = false, length = 100)
	private String cName;

	@Column(name = "DCODE", nullable = false, length = 10)
	private String dCode;

	@Column(name = "SECCODE", nullable = false, length = 5)
	private String secCode;

	@Column(name = "STCODE", nullable = false, length = 5)
	private String stCode;

	@Column(name = "CIRCLE_CODE", nullable = false, length = 5)
	private String circleCode;

	@Column(name = "REG_NO", nullable = false)
	private Long regNo;

	@Column(name = "ADDRES", length = 275)
	private String address;

	@Column(name = "TCODE", length = 8)
	private String tCode;
	 
	@Column(name = "STAT", length = 15)
	private String stat;
	    
	@Column(name = "PHASE")
	private Integer phase;
	    
	@Column(name = "CLOAD", precision = 13, scale = 2)
	private BigDecimal cLoad;
	    
	@Column(name = "CTRANS", length = 1)
	private String cTrans;
	 
	@Column(name = "PHNO")
	private Long phNo;
	 
	@Column(name = "MOBILENO", length = 15)
	private String mobileNo;
	    
	@Column(name = "MOBILENO2", length = 15)
	private String mobileNo2;
	 
	@Column(name = "MOBILENO3", length = 15)
	private String mobileNo3;
	 
	@Column(name = "MOBILENO4", length = 15)
	private String mobileNo4;
	   
	@Column(name = "MOBILENO5", length = 15)
	private String mobileNo5;
	 
	@Column(name = "OWNRMOB", length = 15)
	private String ownerMob;
	    
	@Column(name = "SSCODE")
	private Integer ssCode;
	 
	@Column(name = "SSFCODE")
	private Integer ssFCode;

	@Column(name = "SSFDRSTRCODE")
	private Integer ssfdrstrCode;
	
	public Integer getSsfdrstrCode() {
		return ssfdrstrCode;
	}

	public void setSsfdrstrCode(Integer ssfdrstrCode) {
		this.ssfdrstrCode = ssfdrstrCode;
	}
	public Long getRegCusId() {
		return regCusId;
	}

	public void setRegCusId(Long regCusId) {
		this.regCusId = regCusId;
	}

	public String getCusCode() {
		return cusCode;
	}

	public void setCusCode(String cusCode) {
		this.cusCode = cusCode;
	}

	public String getcName() {
		return cName;
	}

	public void setcName(String cName) {
		this.cName = cName;
	}

	public String getdCode() {
		return dCode;
	}

	public void setdCode(String dCode) {
		this.dCode = dCode;
	}

	public String getSecCode() {
		return secCode;
	}

	public void setSecCode(String secCode) {
		this.secCode = secCode;
	}

	public String getStCode() {
		return stCode;
	}

	public void setStCode(String stCode) {
		this.stCode = stCode;
	}

	public String getCircleCode() {
		return circleCode;
	}

	public void setCircleCode(String circleCode) {
		this.circleCode = circleCode;
	}

	public Long getRegNo() {
		return regNo;
	}

	public void setRegNo(Long regNo) {
		this.regNo = regNo;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String gettCode() {
		return tCode;
	}

	public void settCode(String tCode) {
		this.tCode = tCode;
	}

	public String getStat() {
		return stat;
	}

	public void setStat(String stat) {
		this.stat = stat;
	}

	public Integer getPhase() {
		return phase;
	}

	public void setPhase(Integer phase) {
		this.phase = phase;
	}

	public BigDecimal getcLoad() {
		return cLoad;
	}

	public void setcLoad(BigDecimal cLoad) {
		this.cLoad = cLoad;
	}

	public String getcTrans() {
		return cTrans;
	}

	public void setcTrans(String cTrans) {
		this.cTrans = cTrans;
	}

	public Long getPhNo() {
		return phNo;
	}

	public void setPhNo(Long phNo) {
		this.phNo = phNo;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public String getMobileNo2() {
		return mobileNo2;
	}

	public void setMobileNo2(String mobileNo2) {
		this.mobileNo2 = mobileNo2;
	}

	public String getMobileNo3() {
		return mobileNo3;
	}

	public void setMobileNo3(String mobileNo3) {
		this.mobileNo3 = mobileNo3;
	}

	public String getMobileNo4() {
		return mobileNo4;
	}

	public void setMobileNo4(String mobileNo4) {
		this.mobileNo4 = mobileNo4;
	}

	public String getMobileNo5() {
		return mobileNo5;
	}

	public void setMobileNo5(String mobileNo5) {
		this.mobileNo5 = mobileNo5;
	}

	public String getOwnerMob() {
		return ownerMob;
	}

	public void setOwnerMob(String ownerMob) {
		this.ownerMob = ownerMob;
	}

	public Integer getSsCode() {
		return ssCode;
	}

	public void setSsCode(Integer ssCode) {
		this.ssCode = ssCode;
	}

	public Integer getSsFCode() {
		return ssFCode;
	}

	public void setSsFCode(Integer ssFCode) {
		this.ssFCode = ssFCode;
	}
	 
	 

}
