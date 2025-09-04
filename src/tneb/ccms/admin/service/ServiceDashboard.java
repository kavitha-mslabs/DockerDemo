package tneb.ccms.admin.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tneb.ccms.admin.controller.ServiceMethods;
import tneb.ccms.admin.model.LoginParams;

@Path("/dashboard")
public class ServiceDashboard {
	
	private Logger logger = LoggerFactory.getLogger(ServiceDashboard.class);

	@POST
	@Path("/post")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public JSONObject createDashboard(JSONObject jsonObject) {
		
		logger.info("ServiceDashboard: createDashboard: json object = " + jsonObject);

		LoginParams loginParams = new LoginParams();
		ServiceMethods serviceMethods = new ServiceMethods();
		loginParams.fieldName = jsonObject.get("field_name").toString();
		loginParams.officeId = Integer.valueOf(jsonObject.get("office_id").toString());

		return serviceMethods.createDashboard(loginParams);

	}
}
