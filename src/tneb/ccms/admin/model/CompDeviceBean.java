package tneb.ccms.admin.model;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "COMP_DEVICE")
public class CompDeviceBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="ID")
	private Integer id;
	
	@Column(name = "DEVCODE")
    private String devCode;
	
	@Column(name = "DEVNAME")
    private String devName;
	
	

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDevCode() {
		return devCode;
	}

	public void setDevCode(String devCode) {
		this.devCode = devCode;
	}

	public String getDevName() {
		return devName;
	}

	public void setDevName(String devName) {
		this.devName = devName;
	}

	public CompDeviceBean(String devCode, String devName) {
		super();
		this.devCode = devCode;
		this.devName = devName;
	}

	public CompDeviceBean() {
		super();
	}

	@Override
	public String toString() {
		return "CompDeviceBean [devCode=" + devCode + ", devName=" + devName + "]";
	}

	
	
}
