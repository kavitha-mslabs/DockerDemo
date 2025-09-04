package tneb.ccms.admin.dao;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tneb.ccms.admin.model.CategoryBean;
import tneb.ccms.admin.model.OutagesBean;
import tneb.ccms.admin.model.SubCategoryBean;
import tneb.ccms.admin.util.HibernateUtil;

public class GeneralDao {
	
	private Logger logger = LoggerFactory.getLogger(GeneralDao.class);


	public CategoryBean getCategoryBeanByName(String name) throws Exception {

		CategoryBean categoryBean = null;
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<CategoryBean> criteriaQuery = criteriaBuilder.createQuery(CategoryBean.class);
			Root<CategoryBean> root = criteriaQuery.from(CategoryBean.class);

			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("name"), name));

			Query<CategoryBean> query = session.createQuery(criteriaQuery).setMaxResults(1);
			try {
				categoryBean = query.getSingleResult();
			} catch (NoResultException nre) {
				System.out.println("No Category Found for the given name.");
				logger.info("No Category Found for the given name.");
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

		return categoryBean;
	}

	public SubCategoryBean getSubCategoryBeanByName(String name) throws Exception {

		SubCategoryBean subCategoryBean = null;
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<SubCategoryBean> criteriaQuery = criteriaBuilder.createQuery(SubCategoryBean.class);
			Root<SubCategoryBean> root = criteriaQuery.from(SubCategoryBean.class);

			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("name"), name));

			Query<SubCategoryBean> query = session.createQuery(criteriaQuery).setMaxResults(1);
			
			try {
				subCategoryBean = query.getSingleResult();
			} catch (NoResultException nre) {
				System.out.println("No Sub Category Found for the given name.");
				logger.info("No Sub Category Found for the given name.");
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

		return subCategoryBean;
	}

	
	public SubCategoryBean getSubCategoryBeanById(String id) throws Exception {

		SubCategoryBean subCategoryBean = null;
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<SubCategoryBean> criteriaQuery = criteriaBuilder.createQuery(SubCategoryBean.class);
			Root<SubCategoryBean> root = criteriaQuery.from(SubCategoryBean.class);

			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), id));

			Query<SubCategoryBean> query = session.createQuery(criteriaQuery).setMaxResults(1);
			
			try {
				subCategoryBean = query.getSingleResult();
			} catch (NoResultException nre) {
				System.out.println("No Sub Category Found for the given name.");
				logger.info("No Sub Category Found for the given name.");
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

		return subCategoryBean;
	}
	
	public void saveOutages(OutagesBean outages) {
		
		SessionFactory factory = null;
	    Session session = null;
	    Transaction transaction = null;
	    
	    try {
            factory = HibernateUtil.getSessionFactory();
            session = factory.openSession();
            transaction = session.beginTransaction();
            
            session.save(outages); 
            
            transaction.commit(); 
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace(); 
        } finally {
            HibernateUtil.closeSession(factory, session); 
        }
	    
	}
	public boolean checkOutageExists(String status, String dtCode,
            Timestamp timestampFromDate, Timestamp timestampToDate) {
				SessionFactory factory = null;
				Session session = null;
				boolean exists = false;
				System.err.println("DT CODE"+dtCode);
				System.err.println("timestampFromDate"+timestampFromDate);
				System.err.println("timestampToDate"+timestampToDate);
				try {
					
                    java.util.Date currentDate = new java.util.Date(timestampFromDate.getTime());
			        java.util.Date toTme = new java.util.Date(timestampToDate.getTime());

			        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");
			        String formattedDate1 = dateFormat.format(currentDate); 
			        
			        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
			        String formattedTime = timeFormat.format(currentDate); 
			        String formattedToTime = timeFormat.format(toTme);
			  		
  
					factory = HibernateUtil.getSessionFactory();
					session = factory.openSession();
					
					 // Use native SQL to perform the TRUNC date comparison
			        String sql = "SELECT frprd,toprd, TO_CHAR(frprd, 'HH24:MI') AS from_Time, TO_CHAR(toprd, 'HH24:MI') AS to_Time FROM pl_outages WHERE ssfdrstrcode = :dtCode " +
			                     "AND TRUNC(frprd) = TO_DATE(:formattedDate1, 'DD-MM-YY') " +
			                     "AND TRUNC(toprd) = TO_DATE(:formattedDate1, 'DD-MM-YY') " +
			                     "AND status_name = :status";

				        Query query = session.createNativeQuery(sql);
				        query.setParameter("dtCode", dtCode);
				        query.setParameter("formattedDate1", formattedDate1);
				        query.setParameter("status", status);

				        List<Object[]> resultList = query.getResultList();
	
				        for (Object[] row : resultList) {
				        	Timestamp fromprd = (Timestamp) row[0];   // Assuming row[0] is frprd (Timestamp)
				            Timestamp toprd = (Timestamp) row[1];     // Assuming row[1] is toprd (Timestamp)
				            String fromTimeStr = (String) row[2];     // Assuming row[2] is from_Time (String)
				            String toTimeStr = (String) row[3];       // Assuming row[3] is to_Time (String)

				            // Parse the time strings into Date objects for comparison
				            Date fromTime = timeFormat.parse(fromTimeStr);  
				            Date toTime = timeFormat.parse(toTimeStr);

				            // Parse the user input times
				            Date userFrom = timeFormat.parse(formattedTime);  
				            Date userTo = timeFormat.parse(formattedToTime);

				            
				            if (userFrom.before(toTime) && userTo.after(fromTime)) {
				            	 exists = true;  // There is an overlap, set exists to true
				                 break;  // No need to continue the loop if we found an overlap
				              
				            }
				          
				        }

				} catch (Exception e) {
					logger.error("Error checking outage existence for section ID: " + e);
				} finally {
					HibernateUtil.closeSession(factory, session);
				}
				System.err.println(exists);
				 return exists; 
}
//	public boolean checkOutageExists(String status, String dtCode,
//            Timestamp timestampFromDate, Timestamp timestampToDate) {
//				SessionFactory factory = null;
//				Session session = null;
//				boolean exists = false;
//				
//				try {
//					factory = HibernateUtil.getSessionFactory();
//					session = factory.openSession();
//					
//					CriteriaBuilder cb = session.getCriteriaBuilder();
//					CriteriaQuery<Long> cq = cb.createQuery(Long.class);
//					Root<OutagesBean> root = cq.from(OutagesBean.class);
//					
//					// Match identifiers
//					//Predicate sectionPredicate = cb.equal(root.get("sectionCode"), sectionCode);
//					Predicate statusPredicate = cb.equal(root.get("status"), status);
//					Predicate dtCodePredicate = cb.equal(root.get("ssfdrstrcode"), dtCode);
//					
//					// Overlap logic: existing.from < new.to AND existing.to > new.from
//					Predicate overlapStart = cb.lessThan(root.get("toprd"), timestampToDate); // existing.from < new.to
//					Predicate overlapEnd = cb.greaterThan(root.get("fromprd"), timestampFromDate); // existing.to > new.from
//					Predicate overlap = cb.and(overlapStart, overlapEnd);
//					
//					cq.select(cb.count(root))
//					.where(cb.and(statusPredicate, dtCodePredicate, overlap));
//					
//					Long count = session.createQuery(cq).uniqueResult();
//					exists = count != null && count > 0;
//				} catch (Exception e) {
//					logger.error("Error checking outage existence for section ID: " + e);
//				} finally {
//					HibernateUtil.closeSession(factory, session);
//				}
//				
//			return exists;
//}
//	
//	 
	 
	 public List<OutagesBean> findExpiredOutages(Timestamp currentTimestamp) {
		    SessionFactory factory = null;
		    Session session = null;
		    List<OutagesBean> expiredOutages = new ArrayList<>();

		    try {
		        factory = HibernateUtil.getSessionFactory();
		        session = factory.openSession();

		        // Create CriteriaBuilder and CriteriaQuery
		        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		        CriteriaQuery<OutagesBean> criteriaQuery = criteriaBuilder.createQuery(OutagesBean.class);
		        Root<OutagesBean> root = criteriaQuery.from(OutagesBean.class);

		        // Define the predicates for the query
		        Predicate expiredPredicate = criteriaBuilder.lessThan(root.get("toprd"), currentTimestamp);
		        Predicate statusPredicate = criteriaBuilder.equal(root.get("status"), "O");

		        // Combine predicates
		        criteriaQuery.select(root)
		                     .where(criteriaBuilder.and(expiredPredicate, statusPredicate));

		        // Execute the query and get the list of expired outages
		        expiredOutages = session.createQuery(criteriaQuery).getResultList();
		        
		        
		    } catch (Exception e) {
		        logger.error("Error fetching expired outages", e);
		    } finally {
		        // Ensure the session is closed properly
		        HibernateUtil.closeSession(factory, session);
		    }

		    return expiredOutages;
		}
	 
	 public void updateOutageStatus(OutagesBean outage) {
		    Session session = null;
		    Transaction transaction = null;

		    try {
		        // Obtain the Hibernate session from the session factory
		        session = HibernateUtil.getSessionFactory().openSession();
		        
		        // Start a transaction
		        transaction = session.beginTransaction();

		        // Update the outage status
		        session.update(outage);

		        // Commit the transaction
		        transaction.commit();
		        
		      

		    } catch (Exception e) {
		        // Rollback the transaction in case of an error
		        if (transaction != null) {
		            transaction.rollback();
		        }
		        logger.error("Error updating outage status for ID: " +  e);
		    } finally {
		        // Close the session
		        if (session != null) {
		            session.close();
		        }
		    }
		}

	    
}
