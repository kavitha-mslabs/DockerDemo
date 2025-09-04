package tneb.ccms.admin.util;

public class CCMSConstants {

	public final static String[] STATUSES = { "Pending", "In Progress", "Completed" };
	
	public final static int PENDING = 0;
	
	public final static int APPROVED = 1;
	
	public final static int IN_PROGRESS = 1;
	
	public final static int COMPLETED = 2;
	
	public final static String TECH_ERROR = "Unable to perform the reqeuested operation due to technical error.";
	
	public final static String PDF_EXPORT = "pdf";
	
	public final static String[] OFFICE_NAME = { "sectionId", "subDivisionId", "divisionId", "circleId", "regionId" };
	
	public final static String[] OFFICE_NAME_MINNAGAM = {"region_Id"};
	
	public final static String[] OFFICE_BEAN_NAME = { "sectionBean", "subDivisionBean", "divisionBean", "circleBean", "regionBean" };
	
	public final static String[] COMPLAINT_TYPE = { "power_failure", "voltage_fluctuation", "meter", "payment", "fire",
			"dangerous_poles", "theft","new_service","conductor_snapping","others"  };
	
	public final static String[] COMPLAINT_NAME = { "Power Failure", "Voltage Fluctuation", "Meter Complaint",
			"Payment or Billing Complaint", "Fire Incident", "Dangerous Poles", "Theft of Energy","Application Related Complaint", "Conductor Snapping",
			"Others" };
	
	public final static String[] COMPLAINT_TYPE_CODE = { "PF", "VF", "ME", "BL", "FI", "TH", "TE", "AC", "CS" ,"OT"};
	
	public final static String CATEGORY_POWER_FAILURE = "Power Failure";

	public final static String CATEGORY_METER_RELATED = "Meter Related";
	
	public final static String CATEGORY_BILLING = "Billing Related";

	public final static String CATEGORY_VOLTAGE_RELATED = "Voltage Related";
	
	public final static String CATEGORY_FIRE = "Fire";

	//public final static String CATEGORY_POSTING_DANGER = "Posting Danger to Human Life";
	public final static String CATEGORY_POSTING_DANGER = "Dangerous Poles";
	
	public final static String CATEGORY_THEFT_OF_POWER = "Theft of Power";
	
	public final static String CATEGORY_APPLICATION_RELATED = "Application Related Complaint";
	
	public final static String CATEGORY_CONDUCTOR_SNAPPING = "Conductor Snapping";

	public final static String SUB_CATEGORY_POWER_FAILURE = "Power Failure Individual";
	
	public final static String SUB_CATEGORY_VOLTAGE_FLUCTUATIONS = "Voltage Fluctuations";

	public final static String SUB_CATEGORY_METER_PROBLEM = "Meter Problem";
	
	public final static String SUB_CATEGORY_BILLING_ISSUE = "Billing Issue";
	
	public final static String SUB_CATEGORY_FIRE = "Fire In Electrical Installation";
	
	public final static String SUB_CATEGORY_POSTING_DANGER = "Danger to Life";
	
	public final static String SUB_CATEGORY_THEFT_OF_POWER = "Theft of Power";
}
