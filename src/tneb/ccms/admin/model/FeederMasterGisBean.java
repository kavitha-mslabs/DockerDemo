package tneb.ccms.admin.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "FEEDERMASTERGIS")
public class FeederMasterGisBean {

	 @Id
	 @GeneratedValue(strategy = GenerationType.AUTO, generator = "FeederMasterGis_ID_SEQ") 
	 @Column(name = "GID" , nullable = false, length = 20)
	 private Long gid;

	    @Column(name = "FDR_NAME", length = 50)
	    private String fdrName;

	    @Column(name = "FDR_CODE", length = 50)
	    private String fdrCode;

	    @Column(name = "FDR_LENGTH")
	    private Integer fdrLength;

	    @Column(name = "SS_NAME", length = 50)
	    private String ssName;

	    @Column(name = "SS_CODE", length = 50)
	    private String ssCode;

	    @Column(name = "CIR_CODE", length = 50)
	    private String cirCode;

	    @Column(name = "REGION_ID", length = 50)
	    private String regionId;

	    @Column(name = "VOLT_KV", length = 50)
	    private String voltKv;

	    @Column(name = "TOWN_NAME", length = 250)
	    private String townName;

	    @Column(name = "CONN_CAP")
	    private Integer connCap;

	    @Column(name = "NO_OF_DT")
	    private Integer noOfDt;

	    @Column(name = "DISTRICTNAME", length = 50)
	    private String districtName;

	    @Column(name = "FDRCONFIG", length = 50)
	    private String fdrConfig;

	    @Column(name = "FEEDAREA", length = 50)
	    private String feedArea;

	    @Column(name = "FEEDOWN", length = 50)
	    private String feedOwn;

	    @Column(name = "FEEDTYPE", length = 50)
	    private String feedType;

	    // Getters and Setters

	    public Long getGid() {
	        return gid;
	    }

	    public void setGid(Long gid) {
	        this.gid = gid;
	    }

	    public String getFdrName() {
	        return fdrName;
	    }

	    public void setFdrName(String fdrName) {
	        this.fdrName = fdrName;
	    }

	    public String getFdrCode() {
	        return fdrCode;
	    }

	    public void setFdrCode(String fdrCode) {
	        this.fdrCode = fdrCode;
	    }

	    public Integer getFdrLength() {
	        return fdrLength;
	    }

	    public void setFdrLength(Integer fdrLength) {
	        this.fdrLength = fdrLength;
	    }

	    public String getSsName() {
	        return ssName;
	    }

	    public void setSsName(String ssName) {
	        this.ssName = ssName;
	    }

	    public String getSsCode() {
	        return ssCode;
	    }

	    public void setSsCode(String ssCode) {
	        this.ssCode = ssCode;
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

	    public String getVoltKv() {
	        return voltKv;
	    }

	    public void setVoltKv(String voltKv) {
	        this.voltKv = voltKv;
	    }

	    public String getTownName() {
	        return townName;
	    }

	    public void setTownName(String townName) {
	        this.townName = townName;
	    }

	    public Integer getConnCap() {
	        return connCap;
	    }

	    public void setConnCap(Integer connCap) {
	        this.connCap = connCap;
	    }

	    public Integer getNoOfDt() {
	        return noOfDt;
	    }

	    public void setNoOfDt(Integer noOfDt) {
	        this.noOfDt = noOfDt;
	    }

	    public String getDistrictName() {
	        return districtName;
	    }

	    public void setDistrictName(String districtName) {
	        this.districtName = districtName;
	    }

	    public String getFdrConfig() {
	        return fdrConfig;
	    }

	    public void setFdrConfig(String fdrConfig) {
	        this.fdrConfig = fdrConfig;
	    }

	    public String getFeedArea() {
	        return feedArea;
	    }

	    public void setFeedArea(String feedArea) {
	        this.feedArea = feedArea;
	    }

	    public String getFeedOwn() {
	        return feedOwn;
	    }

	    public void setFeedOwn(String feedOwn) {
	        this.feedOwn = feedOwn;
	    }

	    public String getFeedType() {
	        return feedType;
	    }

	    public void setFeedType(String feedType) {
	        this.feedType = feedType;
	    }
}
