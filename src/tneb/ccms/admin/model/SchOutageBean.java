package tneb.ccms.admin.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "SCH_OUTAGE")
public class SchOutageBean {
	
	 @Id
	 @GeneratedValue(strategy = GenerationType.AUTO, generator = "SCH_OUTAGE_ID_SEQ")
	 @Column(name = "ID")
	 private Long id;
	 
	 @Column(name = "FCODE", length = 2)
	 private String fcode;

	 @Column(name = "SSCODE")
	 private Integer sscode;

	 @Column(name = "TRIPDT")
	 private Date tripDate;

	 @Column(name = "TRIP_FMTIME", length = 10)
	 private String tripFromTime;

	 @Column(name = "TRIP_TOTIME", length = 10)
	 private String tripToTime;

	 @Column(name = "TYPE", length = 20)
	 private String type;

	 @Column(name = "REASON", length = 100)
	 private String reason;

	 @Column(name = "SSFCODE")
	 private Integer ssfCode;

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
	 
	 
}
