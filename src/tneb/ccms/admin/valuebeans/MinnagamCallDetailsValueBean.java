package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.sql.Timestamp;

import tneb.ccms.admin.model.MinnagamCallDetailsBean;

public class MinnagamCallDetailsValueBean implements Serializable{
	
	private static final long serialVersionUID = -7640649954272888446L;

	Integer Id;
	
	String uniqueid;
	
	String phoneNumber;
	
	Timestamp startTime;
	
	String station;
	
	String agent;
	
	String status;
	
	Timestamp endTime;
	
	String duration;
	
	String recordingUrl;

	public Integer getId() {
		return Id;
	}

	public void setId(Integer id) {
		Id = id;
	}

	public String getUniqueid() {
		return uniqueid;
	}

	public void setUniqueid(String uniqueid) {
		this.uniqueid = uniqueid;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public String getStation() {
		return station;
	}

	public void setStation(String station) {
		this.station = station;
	}

	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Timestamp getEndTime() {
		return endTime;
	}

	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getRecordingUrl() {
		return recordingUrl;
	}

	public void setRecordingUrl(String recordingUrl) {
		this.recordingUrl = recordingUrl;
	}
	
public static MinnagamCallDetailsValueBean convertBeanToValueBean(MinnagamCallDetailsBean detailsBean) {
		
		MinnagamCallDetailsValueBean callDeatilsValueBean = new MinnagamCallDetailsValueBean();
	
		callDeatilsValueBean.setId(detailsBean.getId());
		callDeatilsValueBean.setUniqueid(detailsBean.getUniqueid());
		callDeatilsValueBean.setPhoneNumber(detailsBean.getPhoneNumber());
		callDeatilsValueBean.setStartTime(detailsBean.getStartTime());
		callDeatilsValueBean.setStation(detailsBean.getStation());
		callDeatilsValueBean.setAgent(detailsBean.getAgent());
		callDeatilsValueBean.setStatus(detailsBean.getStatus());
		callDeatilsValueBean.setEndTime(detailsBean.getEndTime());
		callDeatilsValueBean.setDuration(detailsBean.getDuration());
		callDeatilsValueBean.setRecordingUrl(detailsBean.getRecordingUrl());
		
		return callDeatilsValueBean;
	}
	
	public static MinnagamCallDetailsBean convertValueBeanToBean(MinnagamCallDetailsValueBean callDetailsValueBean) {
		
		MinnagamCallDetailsBean minnagamCallDetailsBean = new MinnagamCallDetailsBean();
		
		minnagamCallDetailsBean.setId(callDetailsValueBean.getId());
		minnagamCallDetailsBean.setUniqueid(callDetailsValueBean.getUniqueid());
		minnagamCallDetailsBean.setPhoneNumber(callDetailsValueBean.getPhoneNumber());
		minnagamCallDetailsBean.setStartTime(callDetailsValueBean.getStartTime());
		minnagamCallDetailsBean.setStation(callDetailsValueBean.getStation());
		minnagamCallDetailsBean.setAgent(callDetailsValueBean.getAgent());
		minnagamCallDetailsBean.setStatus(callDetailsValueBean.getStatus());
		minnagamCallDetailsBean.setEndTime(callDetailsValueBean.getEndTime());
		minnagamCallDetailsBean.setDuration(callDetailsValueBean.getDuration());
		minnagamCallDetailsBean.setRecordingUrl(callDetailsValueBean.getRecordingUrl());
		
		return minnagamCallDetailsBean;
	}
}
