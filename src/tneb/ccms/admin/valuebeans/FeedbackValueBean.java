package tneb.ccms.admin.valuebeans;

public class FeedbackValueBean {
	   private String phone_number;
	    private int complaint_id;
	    private int feedback;
	    private String feedback_datetime;
		public String getPhone_number() {
			return phone_number;
		}
		public void setPhone_number(String phone_number) {
			this.phone_number = phone_number;
		}
		public int getComplaint_id() {
			return complaint_id;
		}
		public void setComplaint_id(int complaint_id) {
			this.complaint_id = complaint_id;
		}
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
		 @Override
		    public String toString() {
		        return "FeedbackDTO{" +
		                "phone_number='" + phone_number + '\'' +
		                ", complaint_id=" + complaint_id +
		                ", feedback=" + feedback +
		                ", feedback_datetime='" + feedback_datetime + '\'' +
		                '}';
		    }
}
