package tneb.ccms.admin.model;

import java.util.List;

import javax.faces.context.FacesContext;

import jakarta.servlet.http.HttpSession;

public class LoginParams {
	
	
		
	public List<Integer> circleIdList;
	
	public Integer userId;
	
	public Integer officerId;
	
	public Integer roleId;
	
	public Integer sectionId;
	
	public Integer subDivisionId;
	
	public Integer divisionId;
	
	public Integer circleId;
	
	public Integer districtId;
	
	public Integer regionId;
	
	public String regionName;
	
	public String circleName;
	
	public String divisionName;
	
	public String subDivisionName;
	
	public String sectionName;
	
	public String sectionCode;
	
	public String fieldBeanName;
	
	public String fieldName;
	
	public String userName;
	
	public String officeName;
	
	public String fieldNameMinnagam;
	
	public Integer officeCirlceId;
	
	public Integer officeId;
	
	public String mobile;
	
	public String email;
	
	public String imagePath;
	
	public String circleCode;

	public Integer adminRoleId;
	
	public Integer callCenterRoleId;
	
	public String adminOfficeName;
	
	public String adminUserName;
	
	public String callCenterUserName;
	
	
	

	public String getCallCenterUserName() {
		return callCenterUserName;
	}

	public void setCallCenterUserName(String callCenterUserName) {
		this.callCenterUserName = callCenterUserName;
	}

	public String getAdminOfficeName() {
		return adminOfficeName;
	}

	public void setAdminOfficeName(String adminOfficeName) {
		this.adminOfficeName = adminOfficeName;
	}

	public String getAdminUserName() {
		return adminUserName;
	}

	public void setAdminUserName(String adminUserName) {
		this.adminUserName = adminUserName;
	}

	public Integer getAdminRoleId() {
		return adminRoleId;
	}

	public void setAdminRoleId(Integer adminRoleId) {
		this.adminRoleId = adminRoleId;
	}

	public Integer getCallCenterRoleId() {
		return callCenterRoleId;
	}

	public void setCallCenterRoleId(Integer callCenterRoleId) {
		this.callCenterRoleId = callCenterRoleId;
	}

	public String getFieldNameMinnagam() {
		return fieldNameMinnagam;
	}

	public void setFieldNameMinnagam(String fieldNameMinnagam) {
		this.fieldNameMinnagam = fieldNameMinnagam;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public Integer getOfficerId() {
		return officerId;
	}

	public void setOfficerId(Integer officerId) {
		this.officerId = officerId;
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public Integer getSectionId() {
		return sectionId;
	}

	public void setSectionId(Integer sectionId) {
		this.sectionId = sectionId;
	}

	public Integer getSubDivisionId() {
		return subDivisionId;
	}

	public void setSubDivisionId(Integer subDivisionId) {
		this.subDivisionId = subDivisionId;
	}

	public Integer getDivisionId() {
		return divisionId;
	}

	public void setDivisionId(Integer divisionId) {
		this.divisionId = divisionId;
	}

	public Integer getCircleId() {
		return circleId;
	}

	public void setCircleId(Integer circleId) {
		this.circleId = circleId;
	}

	public Integer getDistrictId() {
		return districtId;
	}

	public void setDistrictId(Integer districtId) {
		this.districtId = districtId;
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

	public String getCircleName() {
		return circleName;
	}

	public void setCircleName(String circleName) {
		this.circleName = circleName;
	}

	public String getDivisionName() {
		return divisionName;
	}

	public void setDivisionName(String divisionName) {
		this.divisionName = divisionName;
	}

	public String getSubDivisionName() {
		return subDivisionName;
	}

	public void setSubDivisionName(String subDivisionName) {
		this.subDivisionName = subDivisionName;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}
	
	public String getFieldBeanName() {
		return fieldBeanName;
	}

	public void setFieldBeanName(String fieldBeanName) {
		this.fieldBeanName = fieldBeanName;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getOfficeName() {
		return officeName;
	}

	public void setOfficeName(String officeName) {
		this.officeName = officeName;
	}

	public Integer getOfficeId() {
		return officeId;
	}

	public void setOfficeId(Integer officeId) {
		this.officeId = officeId;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<Integer> getCircleIdList() {
		return circleIdList;
	}

	public void setCircleIdList(List<Integer> circleIdList) {
		this.circleIdList = circleIdList;
	}

	public String getSectionCode() {
		return sectionCode;
	}

	public void setSectionCode(String sectionCode) {
		this.sectionCode = sectionCode;
	}

	public Integer getOfficeCirlceId() {
		return officeCirlceId;
	}

	public void setOfficeCirlceId(Integer officeCirlceId) {
		this.officeCirlceId = officeCirlceId;
	}

	public String getCircleCode() {
		return circleCode;
	}

	public void setCircleCode(String circleCode) {
		this.circleCode = circleCode;
	}
	
}
