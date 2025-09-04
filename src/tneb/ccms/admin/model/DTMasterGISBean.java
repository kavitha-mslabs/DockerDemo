package tneb.ccms.admin.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity 
@Table(name = "DTMASTERGIS")
public class DTMasterGISBean {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "DtmasterGis_ID_SEQ")
    @Column(name = "GID" , nullable = false, length = 20)
 
    private Long gid;

    @Column(name = "DT_NAME", length = 250)
    private String dtName;

    @Column(name = "DT_CAP_KVA", length = 50)
    private String dtCapKva;

    @Column(name = "DT_VOLT_KV", length = 50)
    private String dtVoltKv;

    @Column(name = "DT_MAKE", length = 250)
    private String dtMake;

    @Column(name = "METR_EXIST", length = 50)
    private String metrExist;

    @Column(name = "NO_OF_POLE")
    private Long noOfPole;

    @Column(name = "NO_HT_FDR")
    private Long noHtFdr;

    @Column(name = "NO_LT_FDR")
    private Long noLtFdr;

    @Column(name = "HVDS", length = 50)
    private String hvds;

    @Column(name = "DT_SCHEME", length = 50)
    private String dtScheme;

    @Column(name = "FDR_NAME", length = 50)
    private String fdrName;

    @Column(name = "SS_NAME", length = 50)
    private String ssName;

    @Column(name = "DT_CODE", length = 50)
    private String dtCode;

    @Column(name = "FDR_CODE", length = 50)
    private String fdrCode;

    @Column(name = "SS_CODE", length = 50)
    private String ssCode;

    @Column(name = "SEC_CODE", length = 50)
    private String secCode;

    @Column(name = "SD_CODE", length = 50)
    private String sdCode;

    @Column(name = "DIV_CODE", length = 50)
    private String divCode;

    @Column(name = "CIR_CODE", length = 50)
    private String cirCode;

    @Column(name = "REGION_ID", length = 50)
    private String regionId;

    @Column(name = "APFC_EXIST", length = 50)
    private String apfcExist;

    @Column(name = "CIR_NAME", length = 50)
    private String cirName;

	public Long getGid() {
		return gid;
	}

	public void setGid(Long gid) {
		this.gid = gid;
	}

	public String getDtName() {
		return dtName;
	}

	public void setDtName(String dtName) {
		this.dtName = dtName;
	}

	public String getDtCapKva() {
		return dtCapKva;
	}

	public void setDtCapKva(String dtCapKva) {
		this.dtCapKva = dtCapKva;
	}

	public String getDtVoltKv() {
		return dtVoltKv;
	}

	public void setDtVoltKv(String dtVoltKv) {
		this.dtVoltKv = dtVoltKv;
	}

	public String getDtMake() {
		return dtMake;
	}

	public void setDtMake(String dtMake) {
		this.dtMake = dtMake;
	}

	public String getMetrExist() {
		return metrExist;
	}

	public void setMetrExist(String metrExist) {
		this.metrExist = metrExist;
	}

	public Long getNoOfPole() {
		return noOfPole;
	}

	public void setNoOfPole(Long noOfPole) {
		this.noOfPole = noOfPole;
	}

	public Long getNoHtFdr() {
		return noHtFdr;
	}

	public void setNoHtFdr(Long noHtFdr) {
		this.noHtFdr = noHtFdr;
	}

	public Long getNoLtFdr() {
		return noLtFdr;
	}

	public void setNoLtFdr(Long noLtFdr) {
		this.noLtFdr = noLtFdr;
	}

	public String getHvds() {
		return hvds;
	}

	public void setHvds(String hvds) {
		this.hvds = hvds;
	}

	public String getDtScheme() {
		return dtScheme;
	}

	public void setDtScheme(String dtScheme) {
		this.dtScheme = dtScheme;
	}

	public String getFdrName() {
		return fdrName;
	}

	public void setFdrName(String fdrName) {
		this.fdrName = fdrName;
	}

	public String getSsName() {
		return ssName;
	}

	public void setSsName(String ssName) {
		this.ssName = ssName;
	}

	public String getDtCode() {
		return dtCode;
	}

	public void setDtCode(String dtCode) {
		this.dtCode = dtCode;
	}

	public String getFdrCode() {
		return fdrCode;
	}

	public void setFdrCode(String fdrCode) {
		this.fdrCode = fdrCode;
	}

	public String getSsCode() {
		return ssCode;
	}

	public void setSsCode(String ssCode) {
		this.ssCode = ssCode;
	}

	public String getSecCode() {
		return secCode;
	}

	public void setSecCode(String secCode) {
		this.secCode = secCode;
	}

	public String getSdCode() {
		return sdCode;
	}

	public void setSdCode(String sdCode) {
		this.sdCode = sdCode;
	}

	public String getDivCode() {
		return divCode;
	}

	public void setDivCode(String divCode) {
		this.divCode = divCode;
	}

	public String getCirCode() {
		return cirCode;
	}

	public void setCirCode(String cirCode) {
		this.cirCode = cirCode;
	}

	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public String getApfcExist() {
		return apfcExist;
	}

	public void setApfcExist(String apfcExist) {
		this.apfcExist = apfcExist;
	}

	public String getCirName() {
		return cirName;
	}

	public void setCirName(String cirName) {
		this.cirName = cirName;
	}
    
    
}
