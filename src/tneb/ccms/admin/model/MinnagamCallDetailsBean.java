package tneb.ccms.admin.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "MINNAGAM_CALL_DETAILS")
//Live - @Table(name = "MINNAGAM_CALL_DETAILS", schema = "TANGEDCODEMO")
public class MinnagamCallDetailsBean {
	
	@Id
	// Live - @GeneratedValue(strategy = GenerationType.AUTO, generator = "TANGEDCODEMO.MINNAGAM_CALL_DETAILS_ID_SEQ")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "MINNAGAM_CALL_DETAILS_ID_SEQ")
	@Column(name = "ID")
	Integer Id;
	
	@Column(name = "UNIQUEID")
	String uniqueid;
	
	@Column(name = "PHONE_NUMBER")
	String phoneNumber;
	
	@Column(name = "START_TIME")
	Timestamp startTime;
	
	@Column(name = "STATION")
	String station;
	
	@Column(name = "AGENT")
	String agent;
	
	@Column(name = "STATUS")
	String status;
	
	@Column(name = "END_TIME")
	Timestamp endTime;
	
	@Column(name = "DURATION")
	String duration;
	
	@Column(name = "RECORDING_URL")
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
	
	

}
