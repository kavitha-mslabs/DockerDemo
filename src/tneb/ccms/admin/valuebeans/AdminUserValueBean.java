package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import tneb.ccms.admin.model.AdminUserBean;

public class AdminUserValueBean implements Serializable {
	
	private static final long serialVersionUID = -7897236749310883796L;

	private Integer id;

	private Integer districtId;

	private Integer sectionCode;

	private String officeName;

	private String userName;

	private String password;
	
	private String oldPassword;
	
	private String newPassword;
	
	private String confirmPassword;
	
	private boolean oldPasswordCorrect=true;

	private String mobile;

	private String email;

	private Integer regionId;

	private String regionName;

	private Integer circleId;

	private String circleName;

	private Integer divisionId;

	private String divisionName;

	private Integer subDivisionId;

	private String subDivisionName;

	private Integer sectionId;

	private String sectionName;

	private Integer roleId;

	private String roleName;
	
	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public boolean isOldPasswordCorrect() {
		return oldPasswordCorrect;
	}

	public void setOldPasswordCorrect(boolean oldPasswordCorrect) {
		this.oldPasswordCorrect = oldPasswordCorrect;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getDistrictId() {
		return districtId;
	}

	public void setDistrictId(Integer districtId) {
		this.districtId = districtId;
	}

	public Integer getSectionCode() {
		return sectionCode;
	}

	public void setSectionCode(Integer sectionCode) {
		this.sectionCode = sectionCode;
	}

	public String getOfficeName() {
		return officeName;
	}

	public void setOfficeName(String officeName) {
		this.officeName = officeName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public Integer getCircleId() {
		return circleId;
	}

	public void setCircleId(Integer circleId) {
		this.circleId = circleId;
	}

	public String getCircleName() {
		return circleName;
	}

	public void setCircleName(String circleName) {
		this.circleName = circleName;
	}

	public Integer getDivisionId() {
		return divisionId;
	}

	public void setDivisionId(Integer divisionId) {
		this.divisionId = divisionId;
	}

	public String getDivisionName() {
		return divisionName;
	}

	public void setDivisionName(String divisionName) {
		this.divisionName = divisionName;
	}

	public Integer getSubDivisionId() {
		return subDivisionId;
	}

	public void setSubDivisionId(Integer subDivisionId) {
		this.subDivisionId = subDivisionId;
	}

	public String getSubDivisionName() {
		return subDivisionName;
	}

	public void setSubDivisionName(String subDivisionName) {
		this.subDivisionName = subDivisionName;
	}

	public Integer getSectionId() {
		return sectionId;
	}

	public void setSectionId(Integer sectionId) {
		this.sectionId = sectionId;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	
	public boolean validateOldPassword() {
		setOldPasswordCorrect(true);
		try {
			String hashedOldPassword1 = md5(oldPassword);
			
			 if(hashedOldPassword1.equals(password)) {
					setOldPasswordCorrect(false);
					return isOldPasswordCorrect();
				}
		}catch(NoSuchAlgorithmException e) {
			return false;
		}
		return isOldPasswordCorrect();
	}

	private String md5(String input) throws NoSuchAlgorithmException {
	    MessageDigest md = MessageDigest.getInstance("MD5");
	    md.update(input.getBytes());
	    byte[] digest = md.digest();
	    StringBuilder sb = new StringBuilder();
	    for (byte b : digest) {
	        sb.append(String.format("%02x", b & 0xff));
	    }
	    return sb.toString();
	}
	
	public static AdminUserValueBean convertAdminUserBeanToAdminUserValueBean(AdminUserBean adminUsers) {
		
		AdminUserValueBean adminUserValueBean = new AdminUserValueBean();
		
		if(adminUsers.getRegionBean() != null) {
			adminUserValueBean.setRegionId(adminUsers.getRegionBean().getId());
			adminUserValueBean.setRegionName(adminUsers.getRegionBean().getName());
		}
		if(adminUsers.getCircleBean() != null) {
			adminUserValueBean.setCircleId(adminUsers.getCircleBean().getId());
			adminUserValueBean.setCircleName(adminUsers.getCircleBean().getName());
		}
		if(adminUsers.getDivisionBean() != null) {
			adminUserValueBean.setDivisionId(adminUsers.getDivisionBean().getId());
			adminUserValueBean.setDivisionName(adminUsers.getDivisionBean().getName());
		}
		if(adminUsers.getSubDivisionBean() != null) {
			adminUserValueBean.setSubDivisionId(adminUsers.getSubDivisionBean().getId());
			adminUserValueBean.setSubDivisionName(adminUsers.getSubDivisionBean().getName());
		} else {
			adminUserValueBean.setSubDivisionId(0);
			adminUserValueBean.setSubDivisionName("N/A");
		}
		if(adminUsers.getSectionBean() != null) {
			adminUserValueBean.setSectionId(adminUsers.getSectionBean().getId());
			adminUserValueBean.setSectionName(adminUsers.getSectionBean().getName());
		} else {
			adminUserValueBean.setSectionId(0);
			adminUserValueBean.setSectionName("N/A");
		}
		if(adminUsers.getRoleBean() != null) {
			adminUserValueBean.setRoleId(adminUsers.getRoleBean().getId());
			adminUserValueBean.setRoleName(adminUsers.getRoleBean().getName());
		} 
		
		adminUserValueBean.setMobile(adminUsers.getMobile());
		adminUserValueBean.setUserName(adminUsers.getUserName());
		adminUserValueBean.setId(adminUsers.getId());
		adminUserValueBean.setOfficeName(adminUsers.getOfficeName());
		adminUserValueBean.setPassword(adminUsers.getPassword());
		adminUserValueBean.setEmail(adminUsers.getEmail());
		
		return adminUserValueBean;
	}
	
	
	public static AdminUserBean convertAdminUserValueBeanToAdminUserBean(AdminUserValueBean adminUserValueBean) {
		
		AdminUserBean adminUser = new AdminUserBean();
		
		
		adminUser.setMobile(adminUserValueBean.getMobile());
		adminUser.setUserName(adminUserValueBean.getUserName());
		adminUser.setId(adminUserValueBean.getId());
		adminUser.setOfficeName(adminUserValueBean.getOfficeName());
		adminUser.setPassword(adminUserValueBean.getPassword());
		adminUser.setEmail(adminUserValueBean.getEmail());
		
		return adminUser;
	}

}
