package tneb.ccms.admin.dao;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tneb.ccms.admin.model.LoginParams;
import tneb.ccms.admin.model.ViewComplaintBean;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.valuebeans.ViewComplaintValueBean;

public class CallCenterDao {
	
	private Logger logger = LoggerFactory.getLogger(CallCenterDao.class);
	
	private static String loadString(List<Integer> list) {
		String condition = "";
		for (Integer id : list) {
			if (StringUtils.isNotBlank(condition)) {
				condition += ", ";
			}
			condition += id;
		}
		return condition;
	}
	
	public int[][] getDashboardData(LoginParams office) {
		DataModel dm = new DataModel();

		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			String hql = "SELECT complaintCode, statusId, count(*) as cnt  FROM  ViewComplaintBean WHERE circleId in ("
					+ loadString(office.circleIdList) + ") GROUP BY " + " complaintCode, statusId ORDER BY complaintCode, statusId";

			if (office.roleId == 3) {
				hql = "SELECT complaintCode, statusId, count(*) as cnt  FROM  ViewComplaintBean WHERE device = \'SM\' GROUP BY " 
						+ " complaintCode, statusId ORDER BY complaintCode, statusId";
			}
			
			Query<?> query = session.createQuery(hql);

			List<?> rows = query.list();
			if (rows.size() > 0) {
				Object[] row = null;
				int ct = 0;
				int status = 0;
				for (int i = 0; i < rows.size(); i++) {
					row = (Object[]) rows.get(i);
					ct = Integer.valueOf(row[0].toString());
					status = Integer.valueOf(row[1].toString());
					dm.arrDash[ct][status] = Integer.valueOf(row[2].toString());
				}

			} else {
				dm.arrDash[0][0] = -1;
			}
		} catch (Exception e) {
			dm.arrDash[0][0] = -1;
			logger.error(ExceptionUtils.getStackTrace(e));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

		return dm.arrDash;
	}
	
public DataModel getMinnagamComplaintCount(LoginParams office){
		
		SessionFactory factory = null;
		Session session = null;
		
		DataModel dm = new DataModel();
		
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			String nativeQuery = "SELECT " +
                    "SUM(CASE WHEN v.status_id = 0 THEN 1 ELSE 0 END) + " +
                    "SUM(CASE WHEN v.status_id = 1 THEN 1 ELSE 0 END) + "
                    + "SUM(CASE WHEN v.status_id = 2 THEN 1 ELSE 0 END) AS total, " +
                    "SUM(CASE WHEN v.status_id = 0 THEN 1 ELSE 0 END) AS pending, " +
                    "SUM(CASE WHEN v.status_id = 1 THEN 1 ELSE 0 END) AS inprogress, " +
                    "SUM(CASE WHEN v.status_id = 2 THEN 1 ELSE 0 END) AS completed, " +
                    "(SUM(CASE WHEN v.status_id = 0 THEN 1 ELSE 0 END) + " +
                    "SUM(CASE WHEN v.status_id = 1 THEN 1 ELSE 0 END) + " +
                    "SUM(CASE WHEN v.status_id = 2 THEN 1 ELSE 0 END)) AS totalData, " +
                    "CASE WHEN (SUM(CASE WHEN v.status_id = 0 THEN 1 ELSE 0 END) + " +
                    "SUM(CASE WHEN v.status_id = 1 THEN 1 ELSE 0 END) + " +
                    "SUM(CASE WHEN v.status_id = 2 THEN 1 ELSE 0 END)) > 0 THEN " +
                    "ROUND((SUM(CASE WHEN v.status_id = 2 THEN 1 ELSE 0 END) / " +
                    "(SUM(CASE WHEN v.status_id = 0 THEN 1 ELSE 0 END) + " +
                    "SUM(CASE WHEN v.status_id = 1 THEN 1 ELSE 0 END) + " +
                    "SUM(CASE WHEN v.status_id = 2 THEN 1 ELSE 0 END))) * 100, 3) " +  
                    "ELSE 0 END AS completedPercentage " +
                    "FROM view_complaint v " +
                    "WHERE v.device = 'MI' " ;
                    //"AND v." + office.fieldNameMinnagam + " = " + office.officeId; 
			
				System.out.println("Minnagam Region Id :::::::::::::; "+office.fieldNameMinnagam);
			
			Query query = session.createNativeQuery(nativeQuery);
			Object[] result = (Object[]) query.getSingleResult();
			
			BigDecimal total = (BigDecimal) result[0];
			BigDecimal pending = (BigDecimal) result[1];
			BigDecimal inProgress = (BigDecimal) result[2];
			BigDecimal completed = (BigDecimal) result[3];
			BigDecimal completedPercentage = (BigDecimal) result[5];
			
			dm.setTotal(total);
			dm.setPending(pending);
			dm.setInProgress(inProgress);
			dm.setCompleted(completed);
			dm.setCompletedPercentage(completedPercentage);
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
		return dm;
		
	}
	
	public DataModel getMinnagamCurrentComplaintData(LoginParams office) {
		
		SessionFactory factory = null;
		Session session = null;
		
		DataModel dm = new DataModel();
		
		try {
			
			System.err.println("CHECKING CURRENT TOTAL::::::::::");
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			Integer region_Id = office.getRegionId();
			
			String nativeQuery = "SELECT " +
                    "SUM(CASE WHEN v.status_id = 0 THEN 1 ELSE 0 END) + " +
                    "SUM(CASE WHEN v.status_id = 1 THEN 1 ELSE 0 END) + "
                    + "SUM(CASE WHEN v.status_id = 2 THEN 1 ELSE 0 END) AS total, " +
                    "SUM(CASE WHEN v.status_id = 0 THEN 1 ELSE 0 END) AS pending, " +
                    "SUM(CASE WHEN v.status_id = 1 THEN 1 ELSE 0 END) AS inprogress, " +
                    "SUM(CASE WHEN v.status_id = 2 THEN 1 ELSE 0 END) AS completed, " +
                    "(SUM(CASE WHEN v.status_id = 0 THEN 1 ELSE 0 END) + " +
                    "SUM(CASE WHEN v.status_id = 1 THEN 1 ELSE 0 END) + " +
                    "SUM(CASE WHEN v.status_id = 2 THEN 1 ELSE 0 END)) AS totalData, " +
                    "CASE WHEN (SUM(CASE WHEN v.status_id = 0 THEN 1 ELSE 0 END) + " +
                    "SUM(CASE WHEN v.status_id = 1 THEN 1 ELSE 0 END) + " +
                    "SUM(CASE WHEN v.status_id = 2 THEN 1 ELSE 0 END)) > 0 THEN " +
                    "ROUND((SUM(CASE WHEN v.status_id = 2 THEN 1 ELSE 0 END) / " +
                    "(SUM(CASE WHEN v.status_id = 0 THEN 1 ELSE 0 END) + " +
                    "SUM(CASE WHEN v.status_id = 1 THEN 1 ELSE 0 END) + " +
                    "SUM(CASE WHEN v.status_id = 2 THEN 1 ELSE 0 END))) * 100, 3) " +  
                    "ELSE 0 END AS completedPercentage " +
                    "FROM view_complaint v " +
                    "WHERE v.device = 'MI' " +
                    "AND TRUNC(v.created_on) = TRUNC(SYSDATE) ";
//                    "AND v." + office.fieldNameMinnagam + " = " + office.officeId;
			System.err.println("office.officeId ::::::::::; "+office.officeId);			
			
			Query query = session.createNativeQuery(nativeQuery);
			
			Object[] result = (Object[]) query.getSingleResult();
			
			BigDecimal total = (BigDecimal) result[0];
			BigDecimal pending = (BigDecimal) result[1];
			BigDecimal inProgress = (BigDecimal) result[2];
			BigDecimal completed = (BigDecimal) result[3];
			BigDecimal completedPercentage = (BigDecimal) result[5];
			
			dm.setCurrentTotal(total);
			dm.setCurrentPending(pending);
			dm.setCurrentInProgress(inProgress);
			dm.setCurrentCompleted(completed);
			dm.setCurrentCompletedPercentage(completedPercentage);
			
			System.out.println("CURRENT TOTAL ::::::::::; "+dm.getCurrentTotal());
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
		
		return dm;
		
	}
	
	public List<ViewComplaintValueBean> getComplaintList(LoginParams office, int statusId, int complaintCode) throws Exception {
		
		List<ViewComplaintValueBean> viewComplaintList = new ArrayList<ViewComplaintValueBean>();
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<ViewComplaintBean> criteriaQuery = criteriaBuilder.createQuery(ViewComplaintBean.class);
			Root<ViewComplaintBean> root = criteriaQuery.from(ViewComplaintBean.class);
			
			//Constructing list of parameters
			List<Predicate> predicates = new ArrayList<Predicate>();
			predicates.add(criteriaBuilder.equal(root.get("complaintCode"), complaintCode));
			if (statusId >= 0) {
				predicates.add(criteriaBuilder.equal(root.get("statusId"), statusId));
			} else {
				predicates.add(criteriaBuilder.lessThan(root.get("statusId"), 2));
			}
			
			Expression<String> circleExpression = root.get("circleId");
			Predicate circlePredicate = circleExpression.in(office.circleIdList);
			
			if (office.roleId == 3) {
				predicates.add(criteriaBuilder.equal(root.get("device"), "SM"));
			} else {
				predicates.add(circlePredicate);
			}
			
			//Constructing order list of parameters
			List<Order> orderList = new ArrayList<Order>();
			orderList.add(criteriaBuilder.asc(root.get("complaintCode")));
			orderList.add(criteriaBuilder.asc(root.get("statusId")));
			orderList.add(criteriaBuilder.desc(root.get("updatedOn")));
			
			criteriaQuery.select(root).where(predicates.toArray(new Predicate[]{}))
			.orderBy(orderList);

			Query<ViewComplaintBean> query = session.createQuery(criteriaQuery).setMaxResults(1000);
			
			List<ViewComplaintBean> list = query.getResultList();
			
			for(ViewComplaintBean ViewComplaintBean : list) {
				viewComplaintList.add(ViewComplaintValueBean.convertViewComplaintBeanToViewComplaintValueBean(ViewComplaintBean));
			}
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		
		return viewComplaintList;

	}

	public List<ViewComplaintValueBean> getMinnagamCirclePending(LoginParams office) throws Exception {
	    List<ViewComplaintValueBean> viewComplaintList = new ArrayList<ViewComplaintValueBean>();
 
	    SessionFactory factory = null;
	    Session session = null;
	    try {
	    	System.err.println("WELCOME TO closeComplaintCircle");
	        factory = HibernateUtil.getSessionFactory();
	        session = factory.openSession();
 
	        String sqlQuery  = "SELECT " +
	        	    "COMP.ID AS id, " +
	        	    "COMP.ALTERNATE_MOBILE_NO AS mobile, " +
	        	    "COMP.comp_mobile AS compMobileNumber, " +
	        	    "COMP.created_on AS createdOnFormatted, " +
	        	    "COMP.SERVICE_NAME AS serviceName, " +
	        	    "COMP.description AS description, " +
	        	    "COMP.service_number AS serviceNumber, " +
	        	    "COMP.section_name AS sectionName, " +
	        	    "COMP.updated_on AS updatedOnFormatted, " +
	        	    "COMP.STATUS_ID AS statusId " +
	        	    "FROM VIEW_COMPLAINT COMP " +
	        	    "WHERE COMP.status_id IN (0,1) " +
	        	    //"AND COMP.CIRCLE_ID IN (:circleIds) " +
	        	    "AND COMP.DEVICE = 'MI' " +
	        	    "ORDER BY COMP.created_on DESC";
 
	                
 
	Query query = session.createSQLQuery(sqlQuery);
	query.setMaxResults(1000);
 
	((NativeQuery) query).addScalar("id", StandardBasicTypes.INTEGER);
	((NativeQuery) query).addScalar("mobile", StandardBasicTypes.STRING);
	((NativeQuery) query).addScalar("compMobileNumber", StandardBasicTypes.STRING);
	((NativeQuery) query).addScalar("createdOnFormatted", StandardBasicTypes.TIMESTAMP);
	((NativeQuery) query).addScalar("serviceName", StandardBasicTypes.STRING);
	((NativeQuery) query).addScalar("description", StandardBasicTypes.STRING);
	((NativeQuery) query).addScalar("serviceNumber", StandardBasicTypes.STRING);
	((NativeQuery) query).addScalar("sectionName", StandardBasicTypes.STRING);
	((NativeQuery) query).addScalar("updatedOnFormatted", StandardBasicTypes.TIMESTAMP);
	((NativeQuery) query).addScalar("statusId", StandardBasicTypes.INTEGER);
	
	 //query.setParameterList("circleIds", office.circleIdList);
 
	List<Object[]> list = query.getResultList();
 
	for (Object[] row : list) {
	    ViewComplaintBean viewComplaintBean = new ViewComplaintBean();
	    viewComplaintBean.setId((Integer) row[0]);
	    viewComplaintBean.setAlternateMobileNo((String) row[1]);
	    viewComplaintBean.setComp_Mobile((String) row[2]);
	    viewComplaintBean.setCreatedOn((Timestamp) row[3]);
	    viewComplaintBean.setServiceName((String) row[4]);
	    viewComplaintBean.setDescription((String) row[5]);
	    viewComplaintBean.setServiceNumber((String) row[6]);
	    viewComplaintBean.setSectionName((String) row[7]);
	    viewComplaintBean.setUpdatedOn((Timestamp) row[8]);
	    viewComplaintBean.setStatusId((Integer) row[9]);
	    //System.err.println("Status ID: " + row[9]);
	   
	    ViewComplaintValueBean vcv = ViewComplaintValueBean.convertViewComplaintBeanToViewComplaintValueBeans(viewComplaintBean);
	    viewComplaintList.add(vcv);
	}
 
 
	    } catch (Exception e) {
	        logger.error(ExceptionUtils.getStackTrace(e));
	        throw e;
	    } finally {
	        HibernateUtil.closeSession(factory, session);
	    }
 
	    return viewComplaintList;
	}
}
