package tneb.ccms.admin.controller;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tneb.ccms.admin.model.ComplaintBean;
import tneb.ccms.admin.model.ComplaintFeedbackBean;
import tneb.ccms.admin.model.FeedbackBean;
import tneb.ccms.admin.model.MinnagamUserRequestBean;
import tneb.ccms.admin.util.HibernateUtil;

public class Feedback {

	public Feedback() {
		super();
		init();
		
	}
	
	private void init() {
		
		
	}

	public void save(FeedbackResponseValueBean input) {
		
		SessionFactory factory = null;
		Session session = null;
		Transaction transaction = null;
	        
	        try {
	        	factory = HibernateUtil.getSessionFactory();
				session = factory.openSession();
				
				ComplaintFeedbackBean feedbackBean = new ComplaintFeedbackBean();
	            
	            Integer rating = input.getFeedback();
	            String remarks;
	            
	            switch(rating) {
	            
			            case 1:
			            	 remarks = "Awesome";
			            	break;
			            case 2:
			            	remarks = "Good";
			            	break;
			            case 3:
			            	remarks = "Satisfied";
			            	break;
			            case 4:
			            	remarks = "Temporary Relief";
			            	break;
			            case 5:
			            	remarks = "Need Development";
			            	break;
			            default:
			            	remarks = "";
			   
	            }
	            
	            
					    System.err.println("Complaint ID: " + input.getComplaint_id());
					  //  System.err.println("Phone Number: " + input.getPhone_number());
					    System.err.println("Feedback: " + input.getFeedback());
					    System.err.println("Feedback DateTime: " + input.getFeedback_datetime());
					    
					    String datetimeStr = input.getFeedback_datetime();
					    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					    Date date = sdf.parse(datetimeStr);
					    Timestamp timestamp = new Timestamp(date.getTime());
					    
					    ComplaintBean complaintBean = session.get(ComplaintBean.class, Integer.parseInt(input.getComplaint_id()) );
					    
					    feedbackBean.setComplaintBean((complaintBean));
					   // feedbackBean.setPhone_number(input.getPhone_number());
					    feedbackBean.setRating(input.getFeedback());
					    feedbackBean.setRemarks(remarks);
					    feedbackBean.setEntryDt(timestamp);
					    
						transaction = session.beginTransaction();
						session.save(feedbackBean);
						transaction.commit();
					    
	        } catch (Exception e) {
	           
	        	 System.err.println("An error occurred while saving feedback: " + e.getMessage());
	        	    e.printStackTrace();
	        } finally {
	            HibernateUtil.closeSession(factory, session);
	        }
		
	}

}
