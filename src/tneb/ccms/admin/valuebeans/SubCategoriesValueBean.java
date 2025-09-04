package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.sql.Timestamp;

import tneb.ccms.admin.model.SubCategoryBean;

public class SubCategoriesValueBean implements Serializable {

	private static final long serialVersionUID = 5009465380453713702L;

	private Integer id;
	
	private Integer categoryId;
	
	private String categoryName;

	private String name;

	private Timestamp createdOn;

	private Timestamp updatedOn;
	
	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
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

	public static SubCategoriesValueBean convertSubCategoriesBeanToSubCategoriesValueBean(SubCategoryBean subCategoriesBean) {
		SubCategoriesValueBean subCategoriesValueBean = new SubCategoriesValueBean();
		
		subCategoriesValueBean.setCategoryId(subCategoriesBean.getCategoryBean().getId());
		subCategoriesValueBean.setCategoryName(subCategoriesBean.getCategoryBean().getName());
		subCategoriesValueBean.setCreatedOn(subCategoriesBean.getCreatedOn());
		subCategoriesValueBean.setId(subCategoriesBean.getId());
		subCategoriesValueBean.setName(subCategoriesBean.getName());
		subCategoriesValueBean.setUpdatedOn(subCategoriesBean.getUpdatedOn());
		
		return subCategoriesValueBean;
	}
	
	public static SubCategoryBean convertSubCategoriesValueBeanToSubCategoriesBean(SubCategoriesValueBean subCategoriesValueBean) {
		SubCategoryBean subCategoriesBean = new SubCategoryBean();
		
		subCategoriesBean.setCreatedOn(subCategoriesValueBean.getCreatedOn());
		subCategoriesBean.setId(subCategoriesValueBean.getId());
		subCategoriesBean.setName(subCategoriesValueBean.getName());
		subCategoriesBean.setUpdatedOn(subCategoriesValueBean.getUpdatedOn());
		
		return subCategoriesBean;
	}
}
