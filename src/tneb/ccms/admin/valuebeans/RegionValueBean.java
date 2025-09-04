package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.sql.Timestamp;

import tneb.ccms.admin.model.RegionBean;

public class RegionValueBean implements Serializable {

	private static final long serialVersionUID = 2656306960858779094L;

	private Integer id;

	private String name;

	private Timestamp createdOn;

	private Timestamp updatedOn;

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

	public static RegionValueBean convertRegionBeanToRegionValueBean(RegionBean regionBean) {
		RegionValueBean regionValueBean = new RegionValueBean();
		
		regionValueBean.setCreatedOn(regionBean.getCreatedOn());
		regionValueBean.setId(regionBean.getId());
		regionValueBean.setName(regionBean.getName());
		regionValueBean.setUpdatedOn(regionBean.getUpdatedOn());
		
		return regionValueBean;
	}
	
	public static RegionBean convertRegionValueBeanToRegionBean(RegionValueBean regionValueBean) {
		RegionBean regionBean = new RegionBean();
		
		regionBean.setCreatedOn(regionValueBean.getCreatedOn());
		regionBean.setId(regionValueBean.getId());
		regionBean.setName(regionValueBean.getName());
		regionBean.setUpdatedOn(regionValueBean.getUpdatedOn());
		
		return regionBean;
	}
}
