package tneb.ccms.admin.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;

import tneb.ccms.admin.controller.ServiceMethods;

@Path("/searchComplaint")
public class ServiceSearchComplaint {

	@POST
	@Path("/complaintId")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public JSONObject complaintList(JSONObject jsonObject) {

		String complaintId = jsonObject.get("complaint_id").toString();

		ServiceMethods serviceMethods = new ServiceMethods();
		return serviceMethods.complaintSearch(Integer.parseInt(complaintId), null);

	}

	
	@POST
	@Path("/consumerMobile")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public JSONObject consumerMobile(JSONObject jsonObject) {

		String consumerMobile = jsonObject.get("consumer_mobile").toString();

		ServiceMethods serviceMethods = new ServiceMethods();
		return serviceMethods.complaintSearch(null, consumerMobile);

	}
}
