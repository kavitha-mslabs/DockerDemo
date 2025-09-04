package tneb.ccms.admin.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;

import tneb.ccms.admin.controller.ServiceMethods;

@Path("/complaint_history")
public class ServiceComplaintHistory {

	@POST
	@Path("/post")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public JSONObject postuserID(JSONObject jsonObject) {

		String complaintId = jsonObject.get("complaint_id").toString();

		ServiceMethods serviceMethods = new ServiceMethods();
		return serviceMethods.getComplaintHistoryList(complaintId);
	}

}
