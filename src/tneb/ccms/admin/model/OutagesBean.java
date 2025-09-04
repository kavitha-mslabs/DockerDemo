package tneb.ccms.admin.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PL_OUTAGES")
public class OutagesBean {
		
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "PL_OUTAGES_ID_SEQ")
	@Column(name = "OUTG_ID")
	Integer outgId;
	
	@Column(name = "CIRCLE_ID")
	Integer circleId;
	
	@Column(name = "SECTION_ID")
	Integer sectionCode;
	
	@Column(name = "FRPRD", insertable = true, updatable = false)
	Timestamp frprd;

	@Column(name = "TOPRD", insertable = true, updatable = false)
	Timestamp toprd;
	
	@Column(name = "DESCRIPTION")
	String desc;
	
	@Column(name = "STATUS_NAME")
	String status;
	
	@Column(name = "SCHTYPE")
	String schType;
	
	@Column(name = "ENTRYDT", insertable = true, updatable = true)
	Timestamp entryDt;
	
	@Column(name = "IPID")
	String ipid;
	
	@Column(name = "SSFDRSTRCODE")
	String ssfdrstrcode;
	
	@Column(name = "ENTRYUSER")
    private String entryUser;

	@Column(name = "DELUSER")
    private String deleteUser;

	

	public String getDeleteUser() {
		return deleteUser;
	}

	public void setDeleteUser(String deleteUser) {
		this.deleteUser = deleteUser;
	}

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
	
	
}
