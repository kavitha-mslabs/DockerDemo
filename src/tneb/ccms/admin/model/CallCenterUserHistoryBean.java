package tneb.ccms.admin.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "CALL_CENTER_USER_HISTORY")

public class CallCenterUserHistoryBean {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "CALL_CENTER_USER_HISTORY_ID_SEQ")
	@Column(name = "ID")
	Integer id;

	    @Column(name = "NAME", length = 100)
	    private String name;

	    @Column(name = "USER_NAME", length = 20)
	    private String userName;

	    @Column(name = "MOBILE_NUMBER", length = 10)
	    private String mobileNumber;

	    @Column(name = "EMAIL_ID", length = 100)
	    private String emailId;

	    @Column(name = "ROLE_ID", nullable = false)
	    private Integer roleId;

	    @Column(name = "FROM_DT", nullable = false)
	    private Timestamp fromDt;

	    @Column(name = "TO_DT", nullable = false)
	    private Timestamp toDt;

	    @Column(name = "CIRCLE_CODE", length = 5)
	    private String circleCode;

	    @Column(name = "REASON", length = 25)
	    private String reason;

//	    @Column(name = "ENTRYDT")
//	    private Timestamp entryDt;

	    // -- Getters and Setters --

	    
	    public Integer getId() {
	        return id;
	    }

	    public Integer getRoleId() {
			return roleId;
		}

		public void setRoleId(Integer roleId) {
			this.roleId = roleId;
		}

		public void setId(Integer id) {
	        this.id = id;
	    }

	    public String getName() {
	        return name;
	    }

	    public void setName(String name) {
	        this.name = name;
	    }

	    public String getUserName() {
	        return userName;
	    }

	    public void setUserName(String userName) {
	        this.userName = userName;
	    }

	    public String getMobileNumber() {
	        return mobileNumber;
	    }

	    public void setMobileNumber(String mobileNumber) {
	        this.mobileNumber = mobileNumber;
	    }

	    public String getEmailId() {
	        return emailId;
	    }

	    public void setEmailId(String emailId) {
	        this.emailId = emailId;
	    }

	   

	    public Timestamp getFromDt() {
	        return fromDt;
	    }

	    public void setFromDt(Timestamp fromDt) {
	        this.fromDt = fromDt;
	    }

	    public Timestamp getToDt() {
	        return toDt;
	    }

	    public void setToDt(Timestamp toDt) {
	        this.toDt = toDt;
	    }

	    public String getCircleCode() {
	        return circleCode;
	    }

	    public void setCircleCode(String circleCode) {
	        this.circleCode = circleCode;
	    }

	    public String getReason() {
	        return reason;
	    }

	    public void setReason(String reason) {
	        this.reason = reason;
	    }

//	    public Timestamp getEntryDt() {
//	        return entryDt;
//	    }
//
//	    public void setEntryDt(Timestamp entryDt) {
//	        this.entryDt = entryDt;
//	    }
}
