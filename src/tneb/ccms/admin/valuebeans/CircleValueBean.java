package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.sql.Timestamp;

import tneb.ccms.admin.model.CircleBean;

public class CircleValueBean implements Serializable {

	private static final long serialVersionUID = 895301870490839152L;

	private Integer id;

	private String name;

	private String code;
	
	private String ipAddress;

	private Timestamp createdOn;

	private Timestamp updatedOn;

	private Integer regionId;
	
	private String regionName;

	public Integer getId() {
		return id;
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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Timestamp getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

	public Timestamp getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Timestamp updatedOn) {
		this.updatedOn = updatedOn;
	}

	public Integer getRegionId() {
		return regionId;
	}

	public void setRegionId(Integer regionId) {
		this.regionId = regionId;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public static CircleValueBean convertCircleBeanToCircleValueBean(CircleBean circleBean) {
		CircleValueBean circleValueBean = new CircleValueBean();
		
		circleValueBean.setCode(circleBean.getCode());
		circleValueBean.setCreatedOn(circleBean.getCreatedOn());
		circleValueBean.setId(circleBean.getId());
		circleValueBean.setName(circleBean.getName());
		if(circleBean.getRegionBean() != null) {
			circleValueBean.setRegionId(circleBean.getRegionBean().getId());
			circleValueBean.setRegionName(circleBean.getRegionBean().getName());
		}
		circleValueBean.setUpdatedOn(circleBean.getUpdatedOn());
		circleValueBean.setIpAddress(circleBean.getIpAddress());

		return circleValueBean;
	}
	
	public static CircleBean convertCircleValueBeanToCircleBean(CircleValueBean circleValueBean) {
		CircleBean circleBean = new CircleBean();
		
		circleBean.setCode(circleValueBean.getCode());
		circleBean.setCreatedOn(circleValueBean.getCreatedOn());
		circleBean.setId(circleValueBean.getId());
		circleBean.setName(circleValueBean.getName());
		circleBean.setUpdatedOn(circleValueBean.getUpdatedOn());
		circleBean.setIpAddress(circleValueBean.getIpAddress());
		
		return circleBean;
	}
}
