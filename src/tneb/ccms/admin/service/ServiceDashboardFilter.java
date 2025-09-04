package tneb.ccms.admin.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;

import tneb.ccms.admin.controller.ServiceMethods;
import tneb.ccms.admin.model.LoginParams;
import tneb.ccms.admin.util.CCMSConstants;

@Path("/dashboard/filter")
public class ServiceDashboardFilter {

	@POST
	@Path("/post")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public JSONObject createDashboard(JSONObject jsonObject) {

		LoginParams loginParams = new LoginParams();
		ServiceMethods serviceMethods = new ServiceMethods();
		
		Integer regionId = Integer.parseInt(jsonObject.get("region_id").toString());
		Integer circleId = Integer.parseInt(jsonObject.get("circle_id").toString());
		Integer divisionId = Integer.parseInt(jsonObject.get("division_id").toString());
		Integer subDivisionId = Integer.parseInt(jsonObject.get("sub_division_id").toString());
		Integer sectionId = Integer.parseInt(jsonObject.get("section_id").toString());
		
		if (regionId > 0) {
			loginParams.fieldName = CCMSConstants.OFFICE_NAME[4];
			loginParams.officeId = regionId;
		}
		if (circleId > 0) {
			loginParams.fieldName = CCMSConstants.OFFICE_NAME[3];
			loginParams.officeId = circleId;
		}
		if (divisionId > 0) {
			loginParams.fieldName = CCMSConstants.OFFICE_NAME[2];
			loginParams.officeId = divisionId;
		}
		if (subDivisionId > 0) {
			loginParams.fieldName = CCMSConstants.OFFICE_NAME[1];
			loginParams.officeId = subDivisionId;
		}
		if (sectionId > 0) {
			loginParams.fieldName = CCMSConstants.OFFICE_NAME[0];
			loginParams.officeId = sectionId;
		}
		
		return serviceMethods.createDashboard(loginParams);

	}
}
