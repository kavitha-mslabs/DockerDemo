package tneb.ccms.admin.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;

import tneb.ccms.admin.controller.ServiceMethods;

@Path("/reasons")
public class ServiceCompletionReason {

	@POST
	@Path("/post")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public JSONObject getReasons(JSONObject jsonObject) {
		ServiceMethods serviceMethods = new ServiceMethods();
		return serviceMethods.getCompletionReason(jsonObject.get("complaint_code").toString());
	}

}
