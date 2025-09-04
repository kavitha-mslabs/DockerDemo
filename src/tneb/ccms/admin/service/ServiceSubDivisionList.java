package tneb.ccms.admin.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;

import tneb.ccms.admin.controller.ServiceMethods;

@Path("/subDivisions")
public class ServiceSubDivisionList {

	@POST
	@Path("/post")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public JSONObject getsubDivisions(JSONObject jsonObject) {
		ServiceMethods serviceMethods = new ServiceMethods();
		return serviceMethods.getSubDivisions(jsonObject.get("division_id").toString());
	}

}
