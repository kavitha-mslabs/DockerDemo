package tneb.ccms.admin.valuebeans;

import tneb.ccms.admin.model.CompContactMap;
import tneb.ccms.admin.model.ComplaintBean;

public class CompContactMapValueBean {
	
	 private Long id;
	 
	 private String contactNo;
	 
	 private Integer complaint;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getContactNo() {
		return contactNo;
	}

	public void setContactNo(String contactNo) {
		this.contactNo = contactNo;
	}

	public Integer getComplaint() {
		return complaint;
	}

	public void setComplaint(Integer complaint) {
		this.complaint = complaint;
	}
	 
	public static CompContactMapValueBean convertBeanToValueBean(CompContactMap bean) {
		CompContactMapValueBean compContactMapValueBean = new CompContactMapValueBean();
		
		compContactMapValueBean.setId(bean.getId());
		compContactMapValueBean.setContactNo(bean.getContactNo());
		compContactMapValueBean.setComplaint(bean.getComplaint());
		
		return compContactMapValueBean;
	}
	
	public static CompContactMap convertValueBeanToBean(CompContactMapValueBean valueBean) {
		CompContactMap compContactMap = new CompContactMap();
		
		compContactMap.setId(valueBean.getId());
		compContactMap.setContactNo(valueBean.getContactNo());
		compContactMap.setComplaint(valueBean.getComplaint());
		
		return compContactMap;
	}
}
