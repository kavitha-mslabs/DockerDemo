package tneb.ccms.admin.valuebeans;

import java.sql.Timestamp;

import tneb.ccms.admin.model.OutagesBean;

public class OutagesValueBean {
	
	private Integer outgId;
	
	private Integer circleId;
	
	private Integer sectionCode;
	
	private Timestamp frprd;

	private Timestamp toprd;
	
	private String desc;
	
	private String status;
	
	private String schType;
	
	private Timestamp entryDt;
	
	private String ipid;
	
	private String ssfdrstrcode;
	
	private String entryUser;
	
	

	public String getEntryUser() {
		return entryUser;
	}

	public void setEntryUser(String entryUser) {
		this.entryUser = entryUser;
	}

	public Integer getOutgId() {
		return outgId;
	}

	public void setOutgId(Integer outgId) {
		this.outgId = outgId;
	}

	public Integer getCircleId() {
		return circleId;
	}

	public void setCircleId(Integer circleId) {
		this.circleId = circleId;
	}

	

	public Integer getSectionCode() {
		return sectionCode;
	}

	public void setSectionCode(Integer sectionCode) {
		this.sectionCode = sectionCode;
	}

	public Timestamp getFrprd() {
		return frprd;
	}

	public void setFrprd(Timestamp frprd) {
		this.frprd = frprd;
	}

	public Timestamp getToprd() {
		return toprd;
	}

	public void setToprd(Timestamp toprd) {
		this.toprd = toprd;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSchType() {
		return schType;
	}

	public void setSchType(String schType) {
		this.schType = schType;
	}

	public Timestamp getEntryDt() {
		return entryDt;
	}

	public void setEntryDt(Timestamp entryDt) {
		this.entryDt = entryDt;
	}

	public String getIpid() {
		return ipid;
	}

	public void setIpid(String ipid) {
		this.ipid = ipid;
	}
	
	public String getSsfdrstrcode() {
		return ssfdrstrcode;
	}

	public void setSsfdrstrcode(String ssfdrstrcode) {
		this.ssfdrstrcode = ssfdrstrcode;
	}

	public static OutagesValueBean convertBeanToValueBean(OutagesBean outagesBean) {
		
		OutagesValueBean outagesValueBean = new OutagesValueBean();
		
		outagesValueBean.setOutgId(outagesBean.getOutgId());
		outagesValueBean.setCircleId(outagesBean.getCircleId());
		outagesValueBean.setSectionCode(outagesBean.getSectionCode());
		outagesValueBean.setFrprd(outagesBean.getFrprd());
		outagesValueBean.setToprd(outagesBean.getToprd());
		outagesValueBean.setDesc(outagesBean.getDesc());
		outagesValueBean.setStatus(outagesBean.getStatus());
		outagesValueBean.setSchType(outagesBean.getSchType());
		outagesValueBean.setEntryDt(outagesBean.getEntryDt());
		outagesValueBean.setIpid(outagesBean.getIpid());
		outagesValueBean.setSsfdrstrcode(outagesBean.getSsfdrstrcode());
		outagesValueBean.setEntryUser(outagesBean.getEntryUser());
		
		return outagesValueBean;
	}
	
	public static OutagesBean convertValueBeanToBean(OutagesValueBean outagesValueBean) {
		
		OutagesBean outagesBean = new OutagesBean();
		
		outagesBean.setOutgId(outagesValueBean.getOutgId());
		outagesBean.setCircleId(outagesValueBean.getCircleId());
		outagesBean.setSectionCode(outagesValueBean.getSectionCode());
		outagesBean.setFrprd(outagesValueBean.getFrprd());
		outagesBean.setToprd(outagesValueBean.getToprd());
		outagesBean.setDesc(outagesValueBean.getDesc());
		outagesBean.setStatus(outagesValueBean.getStatus());
		outagesBean.setSchType(outagesValueBean.getSchType());
		outagesBean.setEntryDt(outagesValueBean.getEntryDt());
		outagesBean.setIpid(outagesValueBean.getIpid());
		outagesBean.setSsfdrstrcode(outagesValueBean.getSsfdrstrcode());
		outagesBean.setEntryUser(outagesValueBean.getEntryUser());
		
		return outagesBean;
	}
}
