package tneb.ccms.admin.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;

import tneb.ccms.admin.controller.ServiceMethods;
import tneb.ccms.admin.valuebeans.AdminUserValueBean;

@Path("/login")
public class ServiceAuthenticate {
	@POST
	@Path("/post")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public JSONObject authenticate_User(JSONObject jsonObject) {

		AdminUserValueBean adminUserValueBean = new AdminUserValueBean();
		adminUserValueBean.setUserName(jsonObject.get("username").toString());
		adminUserValueBean.setPassword(jsonObject.get("password").toString());

		ServiceMethods serviceMethods = new ServiceMethods();
		return serviceMethods.checkAuthentication(adminUserValueBean);

	}

}
