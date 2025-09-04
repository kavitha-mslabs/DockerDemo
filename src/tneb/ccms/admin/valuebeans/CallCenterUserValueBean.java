package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;

import tneb.ccms.admin.model.CallCenterUserBean;

public class CallCenterUserValueBean implements Serializable {

	private static final long serialVersionUID = 1769688405708583500L;

	private Integer id;
	
	private String name;
	
	private String userName;
	
	private String mobile;
	
	private String email;
	
	private String password;
	
	private Integer roleId;
	
	private String roleName;
	
	private Timestamp createdOn;

	private Timestamp updatedOn;
	
    private String oldPassword;
	
	private String newPassword;
	
	private String confirmPassword;
	
	private boolean oldPasswordCorrect=true;
	
	
	

	
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public boolean validateOldPassword() {
		setOldPasswordCorrect(true);
		try {
			String hashedOldPassword1 = md5(oldPassword);
			
			 if(hashedOldPassword1.equals(password)) {
					this.setOldPasswordCorrect(false);
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
	
	public static CallCenterUserValueBean convertBeanToValueBean(CallCenterUserBean callCenterUserBean) {
		
		CallCenterUserValueBean callCenterUserValueBean = new CallCenterUserValueBean();
		
		callCenterUserValueBean.setId(callCenterUserBean.getId());
		callCenterUserValueBean.setCreatedOn(callCenterUserBean.getCreatedOn());
		callCenterUserValueBean.setEmail(callCenterUserBean.getEmail());
		callCenterUserValueBean.setName(callCenterUserBean.getName());
		callCenterUserValueBean.setUserName(callCenterUserBean.getUserName());
		callCenterUserValueBean.setMobile(callCenterUserBean.getMobile());
		callCenterUserValueBean.setPassword(callCenterUserBean.getPassword());
		callCenterUserValueBean.setUpdatedOn(callCenterUserBean.getUpdatedOn());
		callCenterUserValueBean.setRoleId(callCenterUserBean.getCallCenterRoleBean().getId());
		callCenterUserValueBean.setRoleName(callCenterUserBean.getCallCenterRoleBean().getName());
		
		return callCenterUserValueBean;
	}
	
	public static CallCenterUserBean convertValueBeanToBean(CallCenterUserValueBean callCenterUserValueBean) {
		
		CallCenterUserBean callCenterUserBean = new CallCenterUserBean();
		
		callCenterUserBean.setId(callCenterUserValueBean.getId());
		callCenterUserBean.setCreatedOn(callCenterUserValueBean.getCreatedOn());
		callCenterUserBean.setEmail(callCenterUserValueBean.getEmail());
		callCenterUserBean.setName(callCenterUserValueBean.getName());
		callCenterUserBean.setUserName(callCenterUserValueBean.getUserName());
		callCenterUserBean.setMobile(callCenterUserValueBean.getMobile());
		callCenterUserBean.setPassword(callCenterUserValueBean.getPassword());
		callCenterUserBean.setUpdatedOn(callCenterUserValueBean.getUpdatedOn());
		
		return callCenterUserBean;
	}
	
}
