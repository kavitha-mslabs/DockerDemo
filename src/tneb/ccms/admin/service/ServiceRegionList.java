package tneb.ccms.admin.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;

import tneb.ccms.admin.controller.ServiceMethods;

@Path("/regions")
public class ServiceRegionList {

	@GET
	@Path("/get")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getRegions() {
		ServiceMethods serviceMethods = new ServiceMethods();
		return serviceMethods.getRegions();
	}

}
