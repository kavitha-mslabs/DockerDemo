package tneb.ccms.admin.controller;

public class FeedbackResponseValueBean {

	    private String complaint_id;
	 //   private String phone_number;
	    private int feedback;
	    private String feedback_datetime;
		public String getComplaint_id() {
			return complaint_id;
		}
		public void setComplaint_id(String complaint_id) {
			this.complaint_id = complaint_id;
		}
	/*	public String getPhone_number() {
			return phone_number;
		}
		public void setPhone_number(String phone_number) {
			this.phone_number = phone_number;
		}*/
		public int getFeedback() {
			return feedback;
		}
		public void setFeedback(int feedback) {
			this.feedback = feedback;
		}
		public String getFeedback_datetime() {
			return feedback_datetime;
		}
		public void setFeedback_datetime(String feedback_datetime) {
			this.feedback_datetime = feedback_datetime;
		}
		
		
}
