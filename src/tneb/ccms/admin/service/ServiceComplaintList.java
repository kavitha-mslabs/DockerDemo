package tneb.ccms.admin.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;

import tneb.ccms.admin.controller.ServiceMethods;
import tneb.ccms.admin.model.LoginParams;

@Path("/complaint_list")
public class ServiceComplaintList {

	@POST
	@Path("/post")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public JSONObject complaintList(JSONObject jsonObject) {

		LoginParams loginParams = new LoginParams();
		loginParams.fieldName = jsonObject.get("field_name").toString();
		loginParams.officeId = Integer.valueOf(jsonObject.get("office_id").toString());
		int statusId = Integer.parseInt(jsonObject.get("status_id").toString());
		int complaintCode = Integer.parseInt(jsonObject.get("complaint_code").toString());

		ServiceMethods serviceMethods = new ServiceMethods();
		return serviceMethods.getComplaintList(loginParams, statusId, complaintCode);
	}

}
