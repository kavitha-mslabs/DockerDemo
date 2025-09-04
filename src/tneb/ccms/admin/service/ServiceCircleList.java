package tneb.ccms.admin.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;

import tneb.ccms.admin.controller.ServiceMethods;

@Path("/circles")
public class ServiceCircleList {
	
	@POST
	@Path("/post")
    @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	 public JSONObject getCircles(JSONObject jsonObject)
	 {
		  ServiceMethods serviceMethods =new ServiceMethods();
		  return serviceMethods.getCircles(jsonObject.get("region_id").toString());
	 }

}
