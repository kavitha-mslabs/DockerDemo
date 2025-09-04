package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.util.Date;

import tneb.ccms.admin.model.SchOutageBean;

public class SchOutagesValueBean implements Serializable{

	private static final long serialVersionUID = 5320928037120430551L;

	private Long id;
	 
	 private String fcode;

	 private Integer sscode;

	 private Date tripDate;

	 private String tripFromTime;

	 private String tripToTime;

	 private String type;

	 private String reason;

	 private Integer ssfCode;
	 
	 private String cName;
	 
	 private String sectionName;
	 
	 private String cusNumber;
	 
	 

	public String getCusNumber() {
		return cusNumber;
	}

	public void setCusNumber(String cusNumber) {
		this.cusNumber = cusNumber;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	public String getcName() {
		return cName;
	}

	public void setcName(String cName) {
		this.cName = cName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFcode() {
		return fcode;
	}

	public void setFcode(String fcode) {
		this.fcode = fcode;
	}

	public Integer getSscode() {
		return sscode;
	}

	public void setSscode(Integer sscode) {
		this.sscode = sscode;
	}

	public Date getTripDate() {
		return tripDate;
	}

	public void setTripDate(Date tripDate) {
		this.tripDate = tripDate;
	}

	public String getTripFromTime() {
		return tripFromTime;
	}

	public void setTripFromTime(String tripFromTime) {
		this.tripFromTime = tripFromTime;
	}

	public String getTripToTime() {
		return tripToTime;
	}

	public void setTripToTime(String tripToTime) {
		this.tripToTime = tripToTime;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Integer getSsfCode() {
		return ssfCode;
	}

	public void setSsfCode(Integer ssfCode) {
		this.ssfCode = ssfCode;
	}
	 
	 
	public static SchOutagesValueBean convertBeanToValueBean(SchOutageBean schOutageBean) {
		
		SchOutagesValueBean schOutagesValueBean = new SchOutagesValueBean();
		
		if(schOutageBean != null) {
			schOutagesValueBean.setId(schOutageBean.getId());
			schOutagesValueBean.setFcode(schOutageBean.getFcode());
			schOutagesValueBean.setReason(schOutageBean.getReason());
			schOutagesValueBean.setSscode(schOutageBean.getSscode());
			schOutagesValueBean.setSsfCode(schOutageBean.getSsfCode());
			schOutagesValueBean.setTripDate(schOutageBean.getTripDate());
			schOutagesValueBean.setTripFromTime(schOutageBean.getTripFromTime());
			schOutagesValueBean.setTripToTime(schOutageBean.getTripToTime());
			schOutagesValueBean.setType(schOutageBean.getType());
			
		}
		
		return schOutagesValueBean;
		
	}
}
