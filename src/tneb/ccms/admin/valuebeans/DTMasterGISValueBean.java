package tneb.ccms.admin.valuebeans;

import java.io.Serializable;

import tneb.ccms.admin.model.DTMasterGISBean;

public class DTMasterGISValueBean implements Serializable{
	
	private static final long serialVersionUID = -2603723944512176454L;
	
	public DTMasterGISValueBean() {
		// TODO Auto-generated constructor stub
	}
	
	public DTMasterGISValueBean(String ssCode, String ssName) {
		this.ssCode=ssCode;
		this.ssName=ssName;
	}	
	public void getDTMasterGISValueBean(String fdrCode, String fdrName) {
		this.fdrCode=fdrCode;
		this.fdrName=fdrName;
	}	
	public void getDTMasterGISValueBeanDt(String dtCode, String dtName) {
		this.dtCode=dtCode;
		this.dtName=dtName;
	}	

	private Long gid;

    private String dtName;

    private String dtCapKva;

    private String dtVoltKv;

    private String dtMake;

    private String metrExist;

    private Long noOfPole;

    private Long noHtFdr;

    private Long noLtFdr;

    private String hvds;

    private String dtScheme;

    private String fdrName;

    private String ssName;

    private String dtCode;

    private String fdrCode;

    private String ssCode;

    private String secCode;

    private String sdCode;

    private String divCode;

    private String cirCode;

    private String regionId;

    private String apfcExist;

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
    
	public static DTMasterGISValueBean convertBeanToValueBean(DTMasterGISBean dtMasterGISBean) {
		
		DTMasterGISValueBean dtMasterGISValueBean = new DTMasterGISValueBean();
		
		dtMasterGISValueBean.setGid(dtMasterGISBean.getGid());
		dtMasterGISValueBean.setDtName(dtMasterGISBean.getDtName());
		dtMasterGISValueBean.setDtCapKva(dtMasterGISBean.getDtCapKva());
		dtMasterGISValueBean.setDtVoltKv(dtMasterGISBean.getDtVoltKv());
		dtMasterGISValueBean.setDtMake(dtMasterGISBean.getDtMake());
		dtMasterGISValueBean.setMetrExist(dtMasterGISBean.getMetrExist());
		dtMasterGISValueBean.setNoOfPole(dtMasterGISBean.getNoOfPole());
		dtMasterGISValueBean.setNoHtFdr(dtMasterGISBean.getNoHtFdr());
		dtMasterGISValueBean.setNoLtFdr(dtMasterGISBean.getNoLtFdr());
		dtMasterGISValueBean.setHvds(dtMasterGISBean.getHvds());
		dtMasterGISValueBean.setDtScheme(dtMasterGISBean.getDtScheme());
		dtMasterGISValueBean.setFdrName(dtMasterGISBean.getFdrName());
		dtMasterGISValueBean.setSsName(dtMasterGISBean.getSsName());
		dtMasterGISValueBean.setDtCode(dtMasterGISBean.getDtCode());
		dtMasterGISValueBean.setFdrCode(dtMasterGISBean.getFdrCode());
		dtMasterGISValueBean.setSsCode(dtMasterGISBean.getSsCode());
		dtMasterGISValueBean.setSecCode(dtMasterGISBean.getSecCode());
		dtMasterGISValueBean.setSdCode(dtMasterGISBean.getSdCode());
		dtMasterGISValueBean.setDivCode(dtMasterGISBean.getDivCode());
		dtMasterGISValueBean.setCirCode(dtMasterGISBean.getCirCode());
		dtMasterGISValueBean.setRegionId(dtMasterGISBean.getRegionId());
		dtMasterGISValueBean.setApfcExist(dtMasterGISBean.getApfcExist());
		dtMasterGISValueBean.setCirName(dtMasterGISBean.getCirName());
		
		return dtMasterGISValueBean;
	}
    
	public static DTMasterGISBean convertValueBeanToBean(DTMasterGISValueBean dtMasterGISValueBean) {
		
		DTMasterGISBean dtMasterGISBean = new DTMasterGISBean();
		
		dtMasterGISBean.setGid(dtMasterGISValueBean.getGid());
		dtMasterGISBean.setDtName(dtMasterGISValueBean.getDtName());
		dtMasterGISBean.setDtCapKva(dtMasterGISValueBean.getDtCapKva());
		dtMasterGISBean.setDtVoltKv(dtMasterGISValueBean.getDtVoltKv());
		dtMasterGISBean.setDtMake(dtMasterGISValueBean.getDtMake());
		dtMasterGISBean.setMetrExist(dtMasterGISValueBean.getMetrExist());
		dtMasterGISBean.setNoOfPole(dtMasterGISValueBean.getNoOfPole());
		dtMasterGISBean.setNoHtFdr(dtMasterGISValueBean.getNoHtFdr());
		dtMasterGISBean.setNoLtFdr(dtMasterGISValueBean.getNoLtFdr());
		dtMasterGISBean.setHvds(dtMasterGISValueBean.getHvds());
		dtMasterGISBean.setDtScheme(dtMasterGISValueBean.getDtScheme());
		dtMasterGISBean.setFdrName(dtMasterGISValueBean.getFdrName());
		dtMasterGISBean.setSsName(dtMasterGISValueBean.getSsName());
		dtMasterGISBean.setDtCode(dtMasterGISValueBean.getDtCode());
		dtMasterGISBean.setFdrCode(dtMasterGISValueBean.getFdrCode());
		dtMasterGISBean.setSsCode(dtMasterGISValueBean.getSsCode());
		dtMasterGISBean.setSecCode(dtMasterGISValueBean.getSecCode());
		dtMasterGISBean.setSdCode(dtMasterGISValueBean.getSdCode());
		dtMasterGISBean.setDivCode(dtMasterGISValueBean.getDivCode());
		dtMasterGISBean.setCirCode(dtMasterGISValueBean.getCirCode());
		dtMasterGISBean.setRegionId(dtMasterGISValueBean.getRegionId());
		dtMasterGISBean.setApfcExist(dtMasterGISValueBean.getApfcExist());
		dtMasterGISBean.setCirName(dtMasterGISValueBean.getCirName());
		
		return dtMasterGISBean;
	}
}
