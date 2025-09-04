package tneb.ccms.admin.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;

import tneb.ccms.admin.controller.ServiceMethods;

@Path("/transfer_action")
public class ServiceTransfer {

	@POST
	@Path("/post")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public JSONObject takeAction(JSONObject jsonObject) {

		ServiceMethods serviceMethods = new ServiceMethods();

		return serviceMethods.transferAction(jsonObject);
	}

}
