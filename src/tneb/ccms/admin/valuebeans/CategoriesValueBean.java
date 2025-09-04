package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.sql.Timestamp;

import tneb.ccms.admin.model.CategoryBean;

public class CategoriesValueBean implements Serializable {

	private static final long serialVersionUID = 3357451013237053033L;

	private Integer id;
	
	private Integer rescueId;

	private String name;
	
	private String activeFlag;

	private Timestamp createdOn;

	private Timestamp updatedOn;
	
	private String code;
	
	
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Integer getRescueId() {
		return rescueId;
	}

	public void setRescueId(Integer rescueId) {
		this.rescueId = rescueId;
	}

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

	public String getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(String activeFlag) {
		this.activeFlag = activeFlag;
	}

	public static CategoriesValueBean convertCategoriesBeanToCategoriesValueBean(CategoryBean categoriesBean) {
		CategoriesValueBean categoriesValueBean = new CategoriesValueBean();
		
		if(categoriesBean.getRescueBean() != null) {
			categoriesValueBean.setRescueId(categoriesBean.getRescueBean().getId());
		}
		
		categoriesValueBean.setCreatedOn(categoriesBean.getCreatedOn());
		categoriesValueBean.setId(categoriesBean.getId());
		categoriesValueBean.setName(categoriesBean.getName());
		categoriesValueBean.setUpdatedOn(categoriesBean.getUpdatedOn());
		categoriesValueBean.setActiveFlag(categoriesBean.getActiveFlag());
		categoriesValueBean.setCode(categoriesBean.getCode());

		return categoriesValueBean;
	}
	
	public static CategoryBean convertCategoriesValueBeanToCategoriesBean(CategoriesValueBean categoriesValueBean) {
		CategoryBean categoriesBean = new CategoryBean();
		
		categoriesBean.setCreatedOn(categoriesValueBean.getCreatedOn());
		categoriesBean.setId(categoriesValueBean.getId());
		categoriesBean.setName(categoriesValueBean.getName());
		categoriesBean.setUpdatedOn(categoriesValueBean.getUpdatedOn());
		categoriesBean.setUpdatedOn(categoriesValueBean.getUpdatedOn());
		categoriesBean.setActiveFlag(categoriesValueBean.getActiveFlag());
		categoriesBean.setCode(categoriesValueBean.getCode());


		return categoriesBean;
	}
}
