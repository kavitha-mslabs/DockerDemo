package tneb.ccms.admin.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "SSMASTERGIS")
public class SsMasterGisBean {

	    @Id
	    @GeneratedValue(strategy = GenerationType.AUTO, generator = "ssmasterGis_ID_SEQ") 
	    @Column(name = "GID", nullable = false, length = 20)
	    private Long gid;

	    @Column(name = "SS_NAME", length = 50)
	    private String ssName;

	    @Column(name = "VOLT_RATIO", length = 50)
	    private String voltRatio;

	    @Column(name = "SS_TYPE", length = 50)
	    private String ssType;

	    @Column(name = "NO_IN_FDR")
	    private Integer noInFdr;

	    @Column(name = "NO_OUT_FDR")
	    private Integer noOutFdr;

	    @Column(name = "IN_FDR_N_1", length = 50)
	    private String inFdrN1;

	    @Column(name = "IN_FDR_N_2", length = 50)
	    private String inFdrN2;

	    @Column(name = "IN_FDR_N_3", length = 50)
	    private String inFdrN3;

	    @Column(name = "NO_PR_TR")
	    private Integer noPrTr;

	    @Column(name = "TOT_CA_MVA")
	    private Integer totCaMva;

	    @Column(name = "MAX_D_MVA")
	    private Integer maxDMva;

	    @Column(name = "TOWN_CATEG", length = 50)
	    private String townCateg;

	    @Column(name = "SS_CODE", length = 50)
	    private String ssCode;

	    @Column(name = "CIR_CODE", length = 50)
	    private String cirCode;

	    @Column(name = "REGION_ID", length = 50)
	    private String regionId;

	    @Column(name = "CIR_NAME", length = 50)
	    private String cirName;

	    @Column(name = "HVKV")
	    private Integer hvkv;

	    @Column(name = "DISTRICTNAME", length = 50)
	    private String districtName;

		public Long getGid() {
			return gid;
		}

		public void setGid(Long gid) {
			this.gid = gid;
		}

		public String getSsName() {
			return ssName;
		}

		public void setSsName(String ssName) {
			this.ssName = ssName;
		}

		public String getVoltRatio() {
			return voltRatio;
		}

		public void setVoltRatio(String voltRatio) {
			this.voltRatio = voltRatio;
		}

		public String getSsType() {
			return ssType;
		}

		public void setSsType(String ssType) {
			this.ssType = ssType;
		}

		public Integer getNoInFdr() {
			return noInFdr;
		}

		public void setNoInFdr(Integer noInFdr) {
			this.noInFdr = noInFdr;
		}

		public Integer getNoOutFdr() {
			return noOutFdr;
		}

		public void setNoOutFdr(Integer noOutFdr) {
			this.noOutFdr = noOutFdr;
		}

		public String getInFdrN1() {
			return inFdrN1;
		}

		public void setInFdrN1(String inFdrN1) {
			this.inFdrN1 = inFdrN1;
		}

		public String getInFdrN2() {
			return inFdrN2;
		}

		public void setInFdrN2(String inFdrN2) {
			this.inFdrN2 = inFdrN2;
		}

		public String getInFdrN3() {
			return inFdrN3;
		}

		public void setInFdrN3(String inFdrN3) {
			this.inFdrN3 = inFdrN3;
		}

		public Integer getNoPrTr() {
			return noPrTr;
		}

		public void setNoPrTr(Integer noPrTr) {
			this.noPrTr = noPrTr;
		}

		public Integer getTotCaMva() {
			return totCaMva;
		}

		public void setTotCaMva(Integer totCaMva) {
			this.totCaMva = totCaMva;
		}

		public Integer getMaxDMva() {
			return maxDMva;
		}

		public void setMaxDMva(Integer maxDMva) {
			this.maxDMva = maxDMva;
		}

		public String getTownCateg() {
			return townCateg;
		}

		public void setTownCateg(String townCateg) {
			this.townCateg = townCateg;
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

		public String getCirName() {
			return cirName;
		}

		public void setCirName(String cirName) {
			this.cirName = cirName;
		}

		public Integer getHvkv() {
			return hvkv;
		}

		public void setHvkv(Integer hvkv) {
			this.hvkv = hvkv;
		}

		public String getDistrictName() {
			return districtName;
		}

		public void setDistrictName(String districtName) {
			this.districtName = districtName;
		}
	    

}
