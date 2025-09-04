package tneb.ccms.admin.valuebeans;

import java.io.Serializable;

import javax.annotation.ManagedBean;
import javax.faces.bean.SessionScoped;

import tneb.ccms.admin.model.LoginParams;

@ManagedBean(value = "sessionAdminValueBean")
@SessionScoped
public class SessionAdminValueBean implements Serializable{
	private LoginParams adminLoginParams;

    public SessionAdminValueBean() {
        adminLoginParams = new LoginParams();
    }

    public LoginParams getAdminLoginParams() {
        return adminLoginParams;
    }

    public void setAdminLoginParams(LoginParams adminLoginParams) {
        this.adminLoginParams = adminLoginParams;
    }
}
