package tneb.ccms.admin.dao;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.swing.SortOrder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.primefaces.model.FilterMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tneb.ccms.admin.model.CategoryBean;
import tneb.ccms.admin.model.CircleBean;
import tneb.ccms.admin.model.ClosureReasonBean;
import tneb.ccms.admin.model.ComplaintBean;
import tneb.ccms.admin.model.ComplaintHistoryBean;
import tneb.ccms.admin.model.DistrictBean;
import tneb.ccms.admin.model.DivisionBean;
import tneb.ccms.admin.model.FieldWorkerBean;
import tneb.ccms.admin.model.FieldWorkerComplaintBean;
import tneb.ccms.admin.model.LoginParams;
import tneb.ccms.admin.model.PublicUserBean;
import tneb.ccms.admin.model.RegionBean;
import tneb.ccms.admin.model.RescueBean;
import tneb.ccms.admin.model.SectionBean;
import tneb.ccms.admin.model.SubCategoryBean;
import tneb.ccms.admin.model.SubDivisionBean;
import tneb.ccms.admin.model.ViewComplaintBean;
import tneb.ccms.admin.model.ViewComplaintReportBean;
import tneb.ccms.admin.util.CCMSConstants;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.DataModel.ComplaintLog;
import tneb.ccms.admin.util.GeneralUtil;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.util.SMSUtil;
import tneb.ccms.admin.util.SmsClient;
import tneb.ccms.admin.valuebeans.CircleValueBean;
import tneb.ccms.admin.valuebeans.ClosureReasonValueBean;
import tneb.ccms.admin.valuebeans.ComplaintValueBean;
import tneb.ccms.admin.valuebeans.ConsumerServiceValueBean;
import tneb.ccms.admin.valuebeans.DistrictValueBean;
import tneb.ccms.admin.valuebeans.DivisionValueBean;
import tneb.ccms.admin.valuebeans.PublicUserValueBean;
import tneb.ccms.admin.valuebeans.RegionValueBean;
import tneb.ccms.admin.valuebeans.ReportValueBean;
import tneb.ccms.admin.valuebeans.SectionValueBean;
import tneb.ccms.admin.valuebeans.SubCategoriesValueBean;
import tneb.ccms.admin.valuebeans.SubDivisionValueBean;
import tneb.ccms.admin.valuebeans.ViewComplaintReportValueBean;
import tneb.ccms.admin.valuebeans.ViewComplaintValueBean;

public class ComplaintsDao {
	
	private Logger logger = LoggerFactory.getLogger(ComplaintsDao.class);
	
	DataModel dmFilter = new DataModel();

	public int[][] getDashboardData(LoginParams office) {

		DataModel dm = new DataModel();
		String hql = null;
		SessionFactory factory = null;
		Session session = null;

		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();

			if ( (office.roleId != null ) && ( office.roleId == 6 || office.roleId == 7 || office.roleId == 8 )) {
				
				 hql = "SELECT complaintCode, statusId, count(*) as cnt  FROM  ViewComplaintBean "
						 + " GROUP BY " + " complaintCode, statusId ORDER BY complaintCode, statusId";
			}else if (office.roleId == 10) {
				hql = "SELECT complaintCode, statusId, count(*) as cnt  FROM  ViewComplaintBean WHERE " + office.fieldName
						+ "=" + office.officeId
						+ " AND device = \'MI\' "
						+ " GROUP BY " 
						+ " complaintCode, statusId ORDER BY complaintCode, statusId";
			}
			
			else {
				hql = "SELECT complaintCode, statusId, count(*) as cnt  FROM  ViewComplaintBean WHERE " + office.fieldName
					+ "=" + office.officeId + " GROUP BY " + " complaintCode, statusId ORDER BY complaintCode, statusId";
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
	
public List<ViewComplaintValueBean> getComplaintList(LoginParams office, int statusId) throws Exception {
		
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
			//predicates.add(criteriaBuilder.equal(root.get("complaintCode"), complaintCode));
			if (statusId >= 0) {
				predicates.add(criteriaBuilder.equal(root.get("statusId"), statusId));
			} else {
				predicates.add(criteriaBuilder.in(root.get("statusId")).value(0).value(1).value(2));
			}
			//predicates.add(criteriaBuilder.equal(root.get(office.fieldName), office.officeId));
			
			if (office.fieldName.equals("SECTION_ID")) {
				office.fieldName = "sectionId";
			}
			
				if(office.roleId == 7) {
					Expression<String> circleExpression = root.get("circleId");
					Predicate circlePredicate = circleExpression.in(office.circleIdList);
					predicates.add(circlePredicate);
				}
			
			predicates.add(criteriaBuilder.equal(root.get("device"), "MI"));
			//predicates.add(circlePredicate);
			
			//Constructing order list of parameters
//			List<Order> orderList = new ArrayList<Order>();
//			//orderList.add(criteriaBuilder.asc(root.get("complaintCode")));
//			orderList.add(criteriaBuilder.asc(root.get("statusId")));
//			orderList.add(criteriaBuilder.desc(root.get("updatedOn")));
			
			criteriaQuery.select(root).where(predicates.toArray(new Predicate[]{}));

			Query<ViewComplaintBean> query = session.createQuery(criteriaQuery).setMaxResults(1000);
			
			List<ViewComplaintBean> list = query.getResultList();
			
			System.out.println("LIST :::::::::::: "+list.size());
			
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
	
	
	LocalDate currentDate = LocalDate.now();
	
	//Convert it to java.util.Date if the field in your entity is of type Date
	Date today = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	
	public List<ViewComplaintValueBean> getCurrentComplaintList(LoginParams office, int statusId) throws Exception {
		
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
			//predicates.add(criteriaBuilder.equal(root.get("complaintCode"), complaintCode));
			if (statusId >= 0) {
				predicates.add(criteriaBuilder.equal(root.get("statusId"), statusId));
			} else {
				predicates.add(criteriaBuilder.in(root.get("statusId")).value(0).value(1).value(2));
			}
			Expression<Date> createdDateExpr = root.get("createdOn");
			predicates.add(criteriaBuilder.equal(criteriaBuilder.function("TRUNC", Date.class, createdDateExpr), today));
		//	predicates.add(criteriaBuilder.equal(root.get(office.fieldName), office.officeId));
			
			if (office.fieldName.equals("SECTION_ID")) {
				office.fieldName = "sectionId";
			}
			
			if(office.roleId == 7) {
				Expression<String> circleExpression = root.get("circleId");
				Predicate circlePredicate = circleExpression.in(office.circleIdList);
				predicates.add(circlePredicate);
			}
			predicates.add(criteriaBuilder.equal(root.get("device"), "MI"));
			
			
			//Constructing order list of parameters
//			List<Order> orderList = new ArrayList<Order>();
//			//orderList.add(criteriaBuilder.asc(root.get("complaintCode")));
//			orderList.add(criteriaBuilder.asc(root.get("statusId")));
//			orderList.add(criteriaBuilder.desc(root.get("updatedOn")));
			
			criteriaQuery.select(root).where(predicates.toArray(new Predicate[]{}));

			Query<ViewComplaintBean> query = session.createQuery(criteriaQuery).setMaxResults(1000);
			
			List<ViewComplaintBean> list = query.getResultList();
			
			System.out.println("LIST :::::::::::: "+list.size());
			
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

	public List<ViewComplaintValueBean> getComplaintList(LoginParams office) throws Exception {
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
			predicates.add(criteriaBuilder.and(criteriaBuilder.lessThan(root.get("statusId"), CCMSConstants.COMPLETED)));
			predicates.add(criteriaBuilder.equal(root.get(office.fieldName), office.officeId));
			
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
			predicates.add(criteriaBuilder.equal(root.get(office.fieldName), office.officeId));
			
			if (office.fieldName.equals("SECTION_ID")) {
				office.fieldName = "sectionId";
			}
			
			if (office.roleId == 10) {
				
				predicates.add(criteriaBuilder.equal(root.get("device"), "MI"));
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
	
	public List<ComplaintValueBean> getComplaintList(String fieldName, int fieldValue) throws Exception {
		
		Map<String, String> compliantNameMap = getCompliantNameMap();
		
		List<ComplaintValueBean> compliantList = new ArrayList<>();
		ComplaintValueBean complaintValueBean = null;
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<ComplaintBean> criteriaQuery = criteriaBuilder.createQuery(ComplaintBean.class);
			Root<ComplaintBean> root = criteriaQuery.from(ComplaintBean.class);
			
			//Constructing list of parameters
			List<Predicate> predicates = new ArrayList<Predicate>();
			predicates.add(criteriaBuilder.equal(root.get(fieldName).get("id"), fieldValue));
			
			//Constructing order list of parameters
			List<Order> orderList = new ArrayList<Order>();
			orderList.add(criteriaBuilder.asc(root.get("complaintCode")));
			orderList.add(criteriaBuilder.asc(root.get("statusId")));
			orderList.add(criteriaBuilder.desc(root.get("updatedOn")));
			
			criteriaQuery.select(root).where(predicates.toArray(new Predicate[]{}))
			.orderBy(orderList);

			Query<ComplaintBean> query = session.createQuery(criteriaQuery);
			
			List<ComplaintBean> viewComplaintList = query.getResultList();
			
			for (ComplaintBean complaints : viewComplaintList) {
				
				complaintValueBean = ComplaintValueBean.convertComplaintBeanTocomplaintValueBean(complaints);
				
				if(StringUtils.isNotBlank(complaintValueBean.getComplaintType())) {
					complaintValueBean.setComplaintType(compliantNameMap.get(complaintValueBean.getComplaintType()));
				}
				complaintValueBean.setStatus(CCMSConstants.STATUSES[complaintValueBean.getStatusId()]);
				
				
				compliantList.add(complaintValueBean);
			}
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		
		return compliantList;

	}

	public DataModel getCompalintHistoryList(int complaintId, DataModel dataModel) throws Exception{
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			dataModel.setLstComplaintLog(new ArrayList<ComplaintLog>());
			
			DataModel.ComplaintLog complaintLog;
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<ComplaintHistoryBean> criteriaQuery = criteriaBuilder.createQuery(ComplaintHistoryBean.class);
			Root<ComplaintHistoryBean> root = criteriaQuery.from(ComplaintHistoryBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("complaintBean").get("id"), complaintId))
			.orderBy(criteriaBuilder.desc(root.get("createdOn")));
			
			Query<ComplaintHistoryBean> query = session.createQuery(criteriaQuery);
			
			List<ComplaintHistoryBean> complaintHistoryList = query.getResultList();
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
			
			for (ComplaintHistoryBean complaintHistory : complaintHistoryList) {
				complaintLog = new DataModel().getComplaintLog();
				complaintLog.setCompalintDatetime(dateFormat.format(complaintHistory.getCreatedOn()));
				complaintLog.setDescription(complaintHistory.getDescription());
				complaintLog.setStatusId(complaintHistory.getStatusId());
				complaintLog.setDuration(GeneralUtil.findInterval(dateFormat.format(complaintHistory.getCreatedOn())));
				dataModel.getLstComplaintLog().add(complaintLog);
				GeneralUtil.findInterval(dateFormat.format(complaintHistory.getCreatedOn()));
			}
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		
		return dataModel;
	}

	public List<ClosureReasonValueBean> getReasons(int code) throws Exception {
		
		List<ClosureReasonValueBean> closureReasonList = new ArrayList<ClosureReasonValueBean>();
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<ClosureReasonBean> criteriaQuery = criteriaBuilder.createQuery(ClosureReasonBean.class);
			Root<ClosureReasonBean> root = criteriaQuery.from(ClosureReasonBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("complaintCode"), code));

			Query<ClosureReasonBean> query = session.createQuery(criteriaQuery);
			
			List<ClosureReasonBean> list = query.getResultList();
			
			for(ClosureReasonBean closureReasonBean : list) {
				closureReasonList.add(ClosureReasonValueBean.convertClosureReasonBeanToClosureReasonValueBean(closureReasonBean));
			}
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
				
		return closureReasonList;
		
	}
	
		
	private Map<String, String> getCompliantNameMap() {
		
		Map<String, String> compliantNameMap = new TreeMap<>();
		
		compliantNameMap.put(CCMSConstants.COMPLAINT_TYPE_CODE[0], CCMSConstants.COMPLAINT_NAME[0]);
		compliantNameMap.put(CCMSConstants.COMPLAINT_TYPE_CODE[1], CCMSConstants.COMPLAINT_NAME[1]);
		compliantNameMap.put(CCMSConstants.COMPLAINT_TYPE_CODE[2], CCMSConstants.COMPLAINT_NAME[2]);
		compliantNameMap.put(CCMSConstants.COMPLAINT_TYPE_CODE[3], CCMSConstants.COMPLAINT_NAME[3]);
		compliantNameMap.put(CCMSConstants.COMPLAINT_TYPE_CODE[4], CCMSConstants.COMPLAINT_NAME[4]);
		compliantNameMap.put(CCMSConstants.COMPLAINT_TYPE_CODE[5], CCMSConstants.COMPLAINT_NAME[5]);
		compliantNameMap.put(CCMSConstants.COMPLAINT_TYPE_CODE[6], CCMSConstants.COMPLAINT_NAME[6]);
		
		return compliantNameMap;
	}
	
	
	public List<ViewComplaintValueBean> getComplaintById(int id) throws Exception {
		
		List<ViewComplaintValueBean> viewComplaintList = new ArrayList<ViewComplaintValueBean>();
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<ViewComplaintBean> criteriaQuery = criteriaBuilder.createQuery(ViewComplaintBean.class);
			Root<ViewComplaintBean> root = criteriaQuery.from(ViewComplaintBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), id));

			Query<ViewComplaintBean> query = session.createQuery(criteriaQuery);
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
	
public List<ViewComplaintValueBean> getComplaintByConsumerMobileNumber(String mobileNumber) throws Exception {
		
		List<ViewComplaintValueBean> viewComplaintList = new ArrayList<ViewComplaintValueBean>();
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			ViewComplaintBean viewComplaintBean = new ViewComplaintBean();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<ViewComplaintBean> criteriaQuery = criteriaBuilder.createQuery(ViewComplaintBean.class);
			Root<ViewComplaintBean> root = criteriaQuery.from(ViewComplaintBean.class);
			
			String sql = "SELECT device FROM view_complaint WHERE comp_mobile = :mobileNumber AND device = 'MI'";

			String device = null;

			try  {
				Query<String> query = session.createNativeQuery(sql).setMaxResults(1);

			    query.setParameter("mobileNumber", mobileNumber);

			    device = query.uniqueResult(); // Returns null if no match found

			    if (device != null) {
			        System.out.println("DEVICE: " + device);
			    } else {
			        System.out.println("No 'MI' device found for this mobile number.");
			    }

			} catch (Exception e) {
			    System.err.println("Error fetching device: " + e.getMessage());
			}

			
			if(device != null && device.equals("MI"))
			{
				criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("comp_Mobile"), mobileNumber));
				
			}
			else
			{
				criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("mobile"), mobileNumber));
			}
			

			Query<ViewComplaintBean> query = session.createQuery(criteriaQuery);
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
	
	public List<ViewComplaintReportValueBean> searchComplaint1(DataModel dataModel, int complaintCodeValue, int statusValue, int first, int pageSize
			, int statusFilterValue, String serviceNumberValue, String mobileValue,
			String complaintTypeValue, int idValue) throws Exception {
        List<ViewComplaintReportValueBean> viewComplaintList = new ArrayList<ViewComplaintReportValueBean>();
        SessionFactory factory = null;
        Session session = null;

        try {
            factory = HibernateUtil.getSessionFactory();
            session = factory.openSession();

            // Prepare filter lists
            List<String> compliantCodeList = new ArrayList<String>();
            List<String> compliantSubCodeList = new ArrayList<String>();
            List<String> statusList = new ArrayList<String>();
            List<Integer> regionList = new ArrayList<Integer>();
            List<Integer> circleList = new ArrayList<Integer>();
            List<Integer> divisionList = new ArrayList<Integer>();
            List<Integer> subDivisionList = new ArrayList<Integer>();
            List<Integer> sectionList = new ArrayList<Integer>();
            List<String> tagList = new ArrayList<String>();
            List<String> receivedFromList = new ArrayList<String>();
            List<String> deviceList = new ArrayList<>();

//            if (dataModel.getCompType() != null && dataModel.getCompType().length > 0) {
//                compliantCodeList.addAll(Arrays.asList(dataModel.getCompType()));
//            }
            
            if ("ALL".equals(dataModel.getCompOneType())) {
                
//              List<Integer> allRegionIds = dataModel.getLstRegions().stream()
//                      .map(RegionValueBean::getId)  
//                      .collect(Collectors.toList());
//
//              regionList.addAll(allRegionIds);
          } else {
          	
  				String compType = dataModel.getCompOneType();
  				compliantCodeList.add(compType);
          	
  		  }
            
   if ("ALL".equals(dataModel.getSubCategory())) {
                
//              List<Integer> allRegionIds = dataModel.getLstRegions().stream()
//                      .map(RegionValueBean::getId)  
//                      .collect(Collectors.toList());
//
//              regionList.addAll(allRegionIds);
          } else {
          	
  				String compSubType = dataModel.getSubCategory();
  				compliantSubCodeList.add(compSubType);
          	
  		  }
//            if (dataModel.getStatuses() != null && dataModel.getStatuses().length > 0) {
//                statusList.addAll(Arrays.asList(dataModel.getStatuses()));
//            }
            
            if ("ALL".equals(dataModel.getStatusesOne())) {
                
//              List<Integer> allRegionIds = dataModel.getLstRegions().stream()
//                      .map(RegionValueBean::getId)  
//                      .collect(Collectors.toList());
//
//              regionList.addAll(allRegionIds);
          } else {
          	
  				String status = dataModel.getStatusesOne();
  				statusList.add(status);
          	
  		}
            
//            if (dataModel.getRegionFilter() != null && dataModel.getRegionFilter().length > 0) {
//                for (String region : dataModel.getRegionFilter()) {
//                    regionList.add(Integer.parseInt(region));
//                }
//            }
            if ("ALL".equals(dataModel.getRegionOneFilter()) || dataModel.getRegionOneFilter() == null) {
                
                List<Integer> allRegionIds = dataModel.getLstRegions().stream()
                        .map(RegionValueBean::getId)  
                        .collect(Collectors.toList());

                regionList.addAll(allRegionIds);
            } else {
            	
    				String region = dataModel.getRegionOneFilter();
    					regionList.add(Integer.parseInt(region));
            	
    		}
//            if (dataModel.getCircleFilter() != null && dataModel.getCircleFilter().length > 0) {
//                for (String circle : dataModel.getCircleFilter()) {
//                    circleList.add(Integer.parseInt(circle));
//                }
//            }
            if("ALL".equals(dataModel.getCircleOneFilter())) {
            	
//            	List<Integer> allCircleIds = dataModel.getLstCircles().stream()
//            			.map(circle -> circle.getId())
//            			.collect(Collectors.toList());
//            	
//            	circleList.addAll(allCircleIds);
            	
            }else {
            	if(!dataModel.getCircleOneFilter().isEmpty()) {
            		String circle = dataModel.getCircleOneFilter();
            		circleList.add(Integer.parseInt(circle));
            	}
            }
            
//            if (dataModel.getDivisionFilter() != null && dataModel.getDivisionFilter().length > 0) {
//                for (String division : dataModel.getDivisionFilter()) {
//                    divisionList.add(Integer.parseInt(division));
//                }
//            }
            
            if("ALL".equals(dataModel.getDivisionOneFilter())) {
            	
//            	List<Integer> allDivisionIds = dataModel.getLstDivisions().stream()
//            			.map(division -> division.getId())
//            			.collect(Collectors.toList());
//            	
//            	divisionList.addAll(allDivisionIds);
            }else {
            	if(!dataModel.getDivisionOneFilter().isEmpty()) {
	            	String division = dataModel.getDivisionOneFilter();
	            	divisionList.add(Integer.parseInt(division));
            	}
            }
            
//            if (dataModel.getSubDivisionFilter() != null && dataModel.getSubDivisionFilter().length > 0) {
//                for (String subDivision : dataModel.getSubDivisionFilter()) {
//                    subDivisionList.add(Integer.parseInt(subDivision));
//                }
//            }
            
            if ("ALL".equals(dataModel.getSubDivisionOneFilter())) {
                
//                List<Integer> allSubDivisionIds = dataModel.getLstSubDivisions().stream()
//                        .map(SubDivisionValueBean::getId)  
//                        .collect(Collectors.toList());
//
//                subDivisionList.addAll(allSubDivisionIds);
            } else {
            	if(!dataModel.getSubDivisionOneFilter().isEmpty()) {
    				String subDivision = dataModel.getSubDivisionOneFilter();
    				subDivisionList.add(Integer.parseInt(subDivision));
            	}
    				
    		}
//            if (dataModel.getSectionFilter() != null && dataModel.getSectionFilter().length > 0) {
//                for (String section : dataModel.getSectionFilter()) {
//                    sectionList.add(Integer.parseInt(section));
//                }
//            }
            
            if ("ALL".equals(dataModel.getSectionOneFilter())) {
                
//                List<Integer> allSectionIds = dataModel.getLstSections().stream()
//                        .map(SectionValueBean::getId)  
//                        .collect(Collectors.toList());
//
//                sectionList.addAll(allSectionIds);
            } else {
            	if(!dataModel.getSectionOneFilter().isEmpty()) {
    				String section = dataModel.getSectionOneFilter();
    				sectionList.add(Integer.parseInt(section));
            	}
    				
    		}
            if (dataModel.getTagArray() != null && dataModel.getTagArray().length > 0) {
                tagList.addAll(Arrays.asList(dataModel.getTagArray()));
            }
            if (dataModel.getReceivedFromArray() != null && dataModel.getReceivedFromArray().length > 0) {
                receivedFromList.addAll(Arrays.asList(dataModel.getReceivedFromArray()));
            }
//            if (dataModel.getDevices() != null && dataModel.getDevices().length > 0) {
//                deviceList.addAll(Arrays.asList(dataModel.getDevices()));
//            }
            
            if("ALL".equals(dataModel.getDevicesOne())) {
            	
//            	List<Integer> allDivisionIds = dataModel.getLstDivisions().stream()
//            			.map(division -> division.getId())
//            			.collect(Collectors.toList());
//            	
//            	divisionList.addAll(allDivisionIds);
            }else {
            	if(!dataModel.getDevicesOne().isEmpty()) {
	            	String device = dataModel.getDevicesOne();
	            	deviceList.add(device);
	            	
	            	if (deviceList.contains("FOC") && !deviceList.contains("admin")) {
		            	deviceList.add("FOC");
		            	deviceList.add("admin");
		            }
		            if (deviceList.contains("MOBILE")) {
		            	deviceList.add("AMOB");
		            	deviceList.add("IMOB");
		            	deviceList.add("Android");
		            	deviceList.add("mobile");
		            	deviceList.add("iOS");
		            }
            	}
            }

            // Date Filters
            Date fromDate = dataModel.getFromDate();
            Date toDate = dataModel.getToDate();

            // Format dates to include time boundaries
            if (fromDate != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(fromDate);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                fromDate = cal.getTime();
            }
            if (toDate != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(toDate);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                toDate = cal.getTime();
            }
            
            

            String whereCondition = "1 = 1"; 

      
            if (!compliantCodeList.isEmpty()) {
                whereCondition = loadWhereCondition(whereCondition, "complaintCode", compliantCodeList);
            }
            if (!compliantSubCodeList.isEmpty()) {
                whereCondition = loadWhereCondition(whereCondition, "subCategoryId", compliantSubCodeList);
            }
            if (!statusList.isEmpty()) {
                whereCondition = loadWhereCondition(whereCondition, "statusId", statusList);
            }
            if (!regionList.isEmpty()) {
                whereCondition = loadWhereNumberCondition(whereCondition, "regionId", regionList);
            }
            if (!circleList.isEmpty()) {
                whereCondition = loadWhereNumberCondition(whereCondition, "circleId", circleList);
            }
            if (!divisionList.isEmpty()) {
                whereCondition = loadWhereNumberCondition(whereCondition, "divisionId", divisionList);
            }
            if (!subDivisionList.isEmpty()) {
                whereCondition = loadWhereNumberCondition(whereCondition, "subDivisionId", subDivisionList);
            }
            if (!sectionList.isEmpty()) {
                whereCondition = loadWhereNumberCondition(whereCondition, "sectionId", sectionList);
            }
            if (!tagList.isEmpty()) {
                whereCondition = loadWhereCondition(whereCondition, "tag", tagList);
            }
            if (!receivedFromList.isEmpty()) {
                whereCondition = loadWhereCondition(whereCondition, "receivedFrom", receivedFromList);
            }
            if (!deviceList.isEmpty()) {
                whereCondition = loadWhereCondition(whereCondition, "device", deviceList);
            }

            // Date range condition
            if (fromDate != null && toDate == null) {
                whereCondition += " and TRUNC(createdOn) >= TO_DATE('" + GeneralUtil.formatDateForHql(fromDate) + "', 'YYYY-MM-DD')";
            } else if (fromDate != null && toDate != null) {
                whereCondition += " and TRUNC(createdOn) >= TO_DATE('" + GeneralUtil.formatDateForHql(fromDate) 
                                   + "', 'YYYY-MM-DD') and TRUNC(createdOn) <= TO_DATE('" + GeneralUtil.formatDateForHql(toDate) 
                                   + "', 'YYYY-MM-DD')";
            } else if (fromDate == null && toDate != null) {
                whereCondition += " and TRUNC(createdOn) <= TO_DATE('" + GeneralUtil.formatDateForHql(toDate) + "', 'YYYY-MM-DD')";
            }

            
            long totalRecords = getTotalComplaintsCount(whereCondition);
            dataModel.setRowCount((int) totalRecords); 

            String hql = "FROM ViewComplaintReportBean WHERE " + whereCondition;
            
            if (statusFilterValue != -1) {
              	 hql += " AND statusId = :statusFilterValue";
                 
              }else {
                  System.out.println("STATUS FILTER VALUE IS -1, Skipping parameter binding");
              }
            
            if (serviceNumberValue != null) {
             	 hql += " AND serviceNumber LIKE :serviceNumberValue";
             	 
             }else {
                 System.out.println("ServiceNumber FILTER VALUE IS null, Skipping parameter binding");
             }
            
            if (mobileValue != null) {
            	 hql += " AND mobile LIKE :mobileValue";
            	 
            }else {
                System.out.println("mobileValue FILTER VALUE IS null, Skipping parameter binding");
            }
            
            if (complaintTypeValue != null) {
           	 hql += " AND complaintType = :complaintTypeValue";
           }else {
               System.out.println("complaintTypeValue FILTER VALUE IS null, Skipping parameter binding");
           }
            if (idValue != -1) {
             	 hql += " AND id = :idValue";
             }else {
                 System.out.println("id FILTER VALUE IS null, Skipping parameter binding");
             }
            
            hql += " ORDER BY createdOn desc";

            Query<ViewComplaintReportBean> query = session.createQuery(hql, ViewComplaintReportBean.class);
            
            if (statusFilterValue != -1) {
                query.setParameter("statusFilterValue", statusFilterValue);
                
            }else {
                System.out.println("Status Filter Value is -1, no need to bind.");
            }
            
            if (serviceNumberValue != null) {
                query.setParameter("serviceNumberValue", serviceNumberValue);
                query.setParameter("serviceNumberValue", serviceNumberValue + "%");
                
            }else {
                System.out.println("service Filter Value is -1, no need to bind.");
            }
            
            if (mobileValue != null) {
                query.setParameter("mobileValue", mobileValue);
                query.setParameter("mobileValue", mobileValue + "%");
               
            }else {
                System.out.println("service Filter Value is -1, no need to bind.");
            }
            
            if (complaintTypeValue != null) {
                query.setParameter("complaintTypeValue", complaintTypeValue);
            }else {
                System.out.println("service Filter Value is -1, no need to bind.");
            }
            
            if (idValue != -1) {
                query.setParameter("idValue", idValue);
            }else {
                System.out.println("service Filter Value is -1, no need to bind.");
            }
            
            query.setFirstResult(dataModel.getFirst());  
            query.setMaxResults(dataModel.getPageSize());
            
            List<ViewComplaintReportBean> viewComplaintReportBeanList = query.getResultList();

            viewComplaintList = convertBeanListToValueBeanList(viewComplaintReportBeanList);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            HibernateUtil.closeSession(factory, session);
        }

        return viewComplaintList;
    }
	
	public List<ViewComplaintReportValueBean> searchComplaintAll(DataModel dataModel, int complaintCodeValue, int statusValue, int first, int pageSize
			, int statusFilterValue, String serviceNumberValue, String mobileValue,
			String complaintTypeValue) throws Exception {
        List<ViewComplaintReportValueBean> viewComplaintList = new ArrayList<ViewComplaintReportValueBean>();
        SessionFactory factory = null;
        Session session = null;

        try {
            factory = HibernateUtil.getSessionFactory();
            session = factory.openSession();

            // Prepare filter lists
            List<String> compliantCodeList = new ArrayList<String>();
            List<String> statusList = new ArrayList<String>();
            List<Integer> regionList = new ArrayList<Integer>();
            
            List<Integer> circleList = new ArrayList<Integer>();
            List<Integer> divisionList = new ArrayList<Integer>();
            List<Integer> subDivisionList = new ArrayList<Integer>();
            List<Integer> sectionList = new ArrayList<Integer>();
            List<String> tagList = new ArrayList<String>();
            List<String> receivedFromList = new ArrayList<String>();
            List<String> deviceList = new ArrayList<>();

            if (dataModel.getCompType() != null && dataModel.getCompType().length > 0) {
                compliantCodeList.addAll(Arrays.asList(dataModel.getCompType()));
            }
            if (dataModel.getStatuses() != null && dataModel.getStatuses().length > 0) {
                statusList.addAll(Arrays.asList(dataModel.getStatuses()));
            }
            
            dataModel.setRegionFilter(new String[] {"1","2","3","4","5","6","7","8","9","10","11","12"});
            if (dataModel.getRegionFilter() != null && dataModel.getRegionFilter().length > 0) {
                for (String region : dataModel.getRegionFilter()) {
                    regionList.add(Integer.parseInt(region));
                }
            }
            if (dataModel.getCircleFilter() != null && dataModel.getCircleFilter().length > 0) {
                for (String circle : dataModel.getCircleFilter()) {
                    circleList.add(Integer.parseInt(circle));
                }
            }
            if (dataModel.getDivisionFilter() != null && dataModel.getDivisionFilter().length > 0) {
                for (String division : dataModel.getDivisionFilter()) {
                    divisionList.add(Integer.parseInt(division));
                }
            }
            if (dataModel.getSubDivisionFilter() != null && dataModel.getSubDivisionFilter().length > 0) {
                for (String subDivision : dataModel.getSubDivisionFilter()) {
                    subDivisionList.add(Integer.parseInt(subDivision));
                }
            }
            if (dataModel.getSectionFilter() != null && dataModel.getSectionFilter().length > 0) {
                for (String section : dataModel.getSectionFilter()) {
                    sectionList.add(Integer.parseInt(section));
                }
            }
            if (dataModel.getTagArray() != null && dataModel.getTagArray().length > 0) {
                tagList.addAll(Arrays.asList(dataModel.getTagArray()));
            }
            if (dataModel.getReceivedFromArray() != null && dataModel.getReceivedFromArray().length > 0) {
                receivedFromList.addAll(Arrays.asList(dataModel.getReceivedFromArray()));
            }
            if (dataModel.getDevices() != null && dataModel.getDevices().length > 0) {
                deviceList.addAll(Arrays.asList(dataModel.getDevices()));
            }

            // Date Filters
            Date fromDate = dataModel.getFromDate();
            Date toDate = dataModel.getToDate();

            // Format dates to include time boundaries
            if (fromDate != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(fromDate);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                fromDate = cal.getTime();
            }
            if (toDate != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(toDate);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                toDate = cal.getTime();
            }
            
            String whereCondition = "1 = 1"; 

            

            
            if (!compliantCodeList.isEmpty()) {
                whereCondition = loadWhereCondition(whereCondition, "complaintCode", compliantCodeList);
            }
            if (!statusList.isEmpty()) {
                whereCondition = loadWhereCondition(whereCondition, "statusId", statusList);
            }
            if (!regionList.isEmpty()) {
                whereCondition = loadWhereNumberCondition(whereCondition, "regionId", regionList);
            }
            if (!circleList.isEmpty()) {
                whereCondition = loadWhereNumberCondition(whereCondition, "circleId", circleList);
            }
            if (!divisionList.isEmpty()) {
                whereCondition = loadWhereNumberCondition(whereCondition, "divisionId", divisionList);
            }
            if (!subDivisionList.isEmpty()) {
                whereCondition = loadWhereNumberCondition(whereCondition, "subDivisionId", subDivisionList);
            }
            if (!sectionList.isEmpty()) {
                whereCondition = loadWhereNumberCondition(whereCondition, "sectionId", sectionList);
            }
            if (!tagList.isEmpty()) {
                whereCondition = loadWhereCondition(whereCondition, "tag", tagList);
            }
            if (!receivedFromList.isEmpty()) {
                whereCondition = loadWhereCondition(whereCondition, "receivedFrom", receivedFromList);
            }
            if (!deviceList.isEmpty()) {
                whereCondition = loadWhereCondition(whereCondition, "device", deviceList);
            }

            // Date range condition
            if (fromDate != null && toDate == null) {
                whereCondition += " and TRUNC(createdOn) >= TO_DATE('" + GeneralUtil.formatDateForHql(fromDate) + "', 'YYYY-MM-DD')";
            } else if (fromDate != null && toDate != null) {
                whereCondition += " and TRUNC(createdOn) >= TO_DATE('" + GeneralUtil.formatDateForHql(fromDate) 
                                   + "', 'YYYY-MM-DD') and TRUNC(createdOn) <= TO_DATE('" + GeneralUtil.formatDateForHql(toDate) 
                                   + "', 'YYYY-MM-DD')";
            } else if (fromDate == null && toDate != null) {
                whereCondition += " and TRUNC(createdOn) <= TO_DATE('" + GeneralUtil.formatDateForHql(toDate) + "', 'YYYY-MM-DD')";
            }

            
            long totalRecords = getTotalComplaintsCount(whereCondition);
            dataModel.setRowCount((int) totalRecords); 

            String hql = "FROM ViewComplaintReportBean WHERE " + whereCondition ;
            
            if (statusFilterValue != -1) {
           	 hql += " AND statusId = :statusFilterValue";
           }else {
               System.out.println("STATUS FILTER VALUE IS -1, Skipping parameter binding");
           }
            
            if (serviceNumberValue != null) {
              	 hql += " AND serviceNumber = :serviceNumberValue";
              }else {
                  System.out.println("ServiceNumber FILTER VALUE IS null, Skipping parameter binding");
              }
            
            if (mobileValue != null) {
             	 hql += " AND mobile = :mobileValue";
             	 System.out.println("mobileValue ::::::::::::::::: "+mobileValue);
             }else {
                 System.out.println("mobileValue FILTER VALUE IS null, Skipping parameter binding");
             }
            
            if (complaintTypeValue != null) {
            	 hql += " AND complaintType = :complaintTypeValue";
            }else {
                System.out.println("complaintTypeValue FILTER VALUE IS null, Skipping parameter binding");
            }
            
            
            Query<ViewComplaintReportBean> query = session.createQuery(hql, ViewComplaintReportBean.class);
            
            System.out.println(hql);
            
            if (statusFilterValue != -1) {
                query.setParameter("statusFilterValue", statusFilterValue);
                System.out.println("NEW STATUS FILTER VALUE ::::::::::::: "+statusFilterValue);
            }else {
                System.out.println("Status Filter Value is -1, no need to bind.");
            }
            
            if (serviceNumberValue != null) {
                query.setParameter("serviceNumberValue", serviceNumberValue);
                System.out.println("SERVICE NUMBER FILTER VALUE ::::::::::::: "+serviceNumberValue);
            }else {
                System.out.println("service Filter Value is -1, no need to bind.");
            }
            
            if (mobileValue != null) {
                query.setParameter("mobileValue", mobileValue);
                System.out.println("mobileValue FILTER VALUE ::::::::::::: "+mobileValue);
               
            }else {
                System.out.println("service Filter Value is -1, no need to bind.");
            }
            
            if (complaintTypeValue != null) {
                query.setParameter("complaintTypeValue", complaintTypeValue);
            }else {
                System.out.println("service Filter Value is -1, no need to bind.");
            }
            
            query.setFirstResult(dataModel.getFirst());  
            query.setMaxResults(dataModel.getPageSize());
            
            List<ViewComplaintReportBean> viewComplaintReportBeanList = query.getResultList();

            viewComplaintList = convertBeanListToValueBeanList(viewComplaintReportBeanList);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            HibernateUtil.closeSession(factory, session);
        }

        return viewComplaintList;
    }
	

   
    private String buildWhereCondition(List<String> compliantCodeList, List<String> statusList, 
                                        List<Integer> regionList, List<Integer> circleList, 
                                        List<Integer> divisionList, List<Integer> subDivisionList, 
                                        List<Integer> sectionList, List<String> tagList, 
                                        List<String> receivedFromList, List<String> deviceList, 
                                        Date fromDate, Date toDate) {
        String whereCondition = " "; 

        if (!compliantCodeList.isEmpty()) {
            whereCondition = loadWhereCondition(whereCondition, "complaintCode", compliantCodeList);
        }
        if (!statusList.isEmpty()) {
            whereCondition = loadWhereCondition(whereCondition, "statusId", statusList);
        }
        if (!regionList.isEmpty()) {
            whereCondition = loadWhereNumberCondition(whereCondition, "regionId", regionList);
        }
        if (!circleList.isEmpty()) {
            whereCondition = loadWhereNumberCondition(whereCondition, "circleId", circleList);
        }
        if (!divisionList.isEmpty()) {
            whereCondition = loadWhereNumberCondition(whereCondition, "divisionId", divisionList);
        }
        if (!subDivisionList.isEmpty()) {
            whereCondition = loadWhereNumberCondition(whereCondition, "subDivisionId", subDivisionList);
        }
        if (!sectionList.isEmpty()) {
            whereCondition = loadWhereNumberCondition(whereCondition, "sectionId", sectionList);
        }
        if (!tagList.isEmpty()) {
            whereCondition = loadWhereCondition(whereCondition, "tag", tagList);
        }
        if (!receivedFromList.isEmpty()) {
            whereCondition = loadWhereCondition(whereCondition, "receivedFrom", receivedFromList);
        }
        if (!deviceList.isEmpty()) {
            whereCondition = loadWhereCondition(whereCondition, "device", deviceList);
        }

        if (fromDate != null && toDate == null) {
            whereCondition += " and TRUNC(createdOn) >= TO_DATE('" + GeneralUtil.formatDateForHql(fromDate) + "', 'YYYY-MM-DD')";
        } else if (fromDate != null && toDate != null) {
            whereCondition += " and TRUNC(createdOn) >= TO_DATE('" + GeneralUtil.formatDateForHql(fromDate) 
                               + "', 'YYYY-MM-DD') and TRUNC(createdOn) <= TO_DATE('" + GeneralUtil.formatDateForHql(toDate) 
                               + "', 'YYYY-MM-DD')";
        } else if (fromDate == null && toDate != null) {
            whereCondition += " and TRUNC(createdOn) <= TO_DATE('" + GeneralUtil.formatDateForHql(toDate) + "', 'YYYY-MM-DD')";
        }

        return whereCondition;
    }

    private long getTotalComplaintsCount(String whereCondition) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();

            String hql = "SELECT COUNT(*) FROM ViewComplaintReportBean WHERE " + whereCondition;
            
            Query<Long> query = session.createQuery(hql, Long.class);

            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private void addFiltersToLists(DataModel dataModel, List<String> compliantCodeList, List<String> statusList, 
                                    List<Integer> regionList, List<Integer> circleList, List<Integer> divisionList, 
                                    List<Integer> subDivisionList, List<Integer> sectionList, List<String> tagList, 
                                    List<String> receivedFromList, List<String> deviceList) {
        if (dataModel.getCompType() != null) {
            compliantCodeList.addAll(Arrays.asList(dataModel.getCompType()));
        }
        if (dataModel.getStatuses() != null) {
            statusList.addAll(Arrays.asList(dataModel.getStatuses()));
        }
        if (dataModel.getRegionFilter() != null) {
        	for (String region : dataModel.getRegionFilter()) {
				regionList.add(Integer.parseInt(region));
			}
        }
        if (dataModel.getCircleFilter() != null) {
        	for (String circle : dataModel.getCircleFilter()) {
				circleList.add(Integer.parseInt(circle));
			}
        }
        if (dataModel.getDivisionFilter() != null) {
        	for (String division : dataModel.getDivisionFilter()) {
				divisionList.add(Integer.parseInt(division));
			}
        }
        if (dataModel.getSubDivisionFilter() != null) {
        	for (String subDivision : dataModel.getSubDivisionFilter()) {
				subDivisionList.add(Integer.parseInt(subDivision));
			}
        }
        if (dataModel.getSectionFilter() != null) {
        	for (String section : dataModel.getSectionFilter()) {
				sectionList.add(Integer.parseInt(section));
			}
        }
        if (dataModel.getTagArray() != null) {
            tagList.addAll(Arrays.asList(dataModel.getTagArray()));
        }
        if (dataModel.getReceivedFromArray() != null) {
            receivedFromList.addAll(Arrays.asList(dataModel.getReceivedFromArray()));
        }
        if (dataModel.getDevices() != null) {
            deviceList.addAll(Arrays.asList(dataModel.getDevices()));
        }
    }
	

	
	public List<ViewComplaintReportValueBean> searchComplaint(DataModel dataModel, int complaintCodeValue, int statusValue) throws Exception {
		
		
		List<ViewComplaintReportValueBean> viewComplaintList = new ArrayList<ViewComplaintReportValueBean>();
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			List<String> compliantCodeList = new ArrayList<String>();
			List<String> statusList = new ArrayList<String>();
			List<Integer> regionList = new ArrayList<Integer>();
			List<Integer> circleList = new ArrayList<Integer>();
			List<Integer> divisionList = new ArrayList<Integer>();
			List<Integer> subDivisionList = new ArrayList<Integer>();
			List<Integer> sectionList = new ArrayList<Integer>();
			List<String> tagList = new ArrayList<String>();
			List<String> receivedFromList = new ArrayList<String>();
			List<String> mobileList = new ArrayList<>();
			List<String> deviceList = new ArrayList<>();
			
			
			if (dataModel.getCompType().length > 0) {
				for (String code : dataModel.getCompType()) {
					compliantCodeList.add(code);
				}
			}
			
			if (dataModel.getStatuses().length > 0) {
				for (String status : dataModel.getStatuses()) {
					statusList.add(status);
				}
			}
			
			if (dataModel.getRegionFilter() != null && dataModel.getRegionFilter().length > 0) {
				for (String region : dataModel.getRegionFilter()) {
					regionList.add(Integer.parseInt(region));
				}
			}
			if (dataModel.getCircleFilter() != null && dataModel.getCircleFilter().length > 0) {
				for (String circle : dataModel.getCircleFilter()) {
					circleList.add(Integer.parseInt(circle));
				}
			}
			if (dataModel.getDivisionFilter() != null && dataModel.getDivisionFilter().length > 0) {
				for (String division : dataModel.getDivisionFilter()) {
					divisionList.add(Integer.parseInt(division));
				}
			}
			if (dataModel.getSubDivisionFilter() != null && dataModel.getSubDivisionFilter().length > 0) {
				for (String subDivision : dataModel.getSubDivisionFilter()) {
					subDivisionList.add(Integer.parseInt(subDivision));
				}
			}
			if (dataModel.getSectionFilter() != null && dataModel.getSectionFilter().length > 0) {
				for (String section : dataModel.getSectionFilter()) {
					sectionList.add(Integer.parseInt(section));
				}
			}
			
			if (dataModel.getTagArray() != null && dataModel.getTagArray().length > 0) {
				for (String tag : dataModel.getTagArray()) {
					tagList.add(tag);
				}
			}
			
			if (dataModel.getReceivedFromArray() != null && dataModel.getReceivedFromArray().length > 0) {
				for (String receivedFrom : dataModel.getReceivedFromArray()) {
					receivedFromList.add(receivedFrom);
				}
			}
			
			if (dataModel.getDevices() != null && dataModel.getDevices().length > 0) { 
	            deviceList.addAll(Arrays.asList(dataModel.getDevices()));
	            
	            if (deviceList.contains("FOC") && !deviceList.contains("admin")) {
	            	deviceList.add("FOC");
	            	deviceList.add("admin");
	            }
	            if (deviceList.contains("MOBILE")) {
	            	deviceList.add("AMOB");
	            	deviceList.add("IMOB");
	            	deviceList.add("Android");
	            	deviceList.add("mobile");
	            	deviceList.add("iOS");
	            }
	        }
			
			Date fromDate = dataModel.getFromDate();
			Date toDate = dataModel.getToDate();
			
			if (fromDate != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(fromDate);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				fromDate = cal.getTime();
			}
			
			if (toDate != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(toDate);
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				toDate = cal.getTime();
			}
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<ViewComplaintReportBean> criteriaQuery = criteriaBuilder.createQuery(ViewComplaintReportBean.class);
			Root<ViewComplaintReportBean> root = criteriaQuery.from(ViewComplaintReportBean.class);
			
			
			//Constructing list of parameters
			List<Predicate> predicates = new ArrayList<Predicate>();
			
			if(complaintCodeValue == -1 && statusValue == -1) {
				if(compliantCodeList.size() > 0) {
					Expression<String> complaintExpression = root.get("complaintCode");
					Predicate compliantCodePredicate = complaintExpression.in(compliantCodeList);
					predicates.add(compliantCodePredicate);
				}
				if(statusList.size() > 0) {
					Expression<String> statusExpression = root.get("statusId");
					Predicate statusPredicate = statusExpression.in(statusList);
					predicates.add(statusPredicate);
				}
			} else {
				predicates.add(criteriaBuilder.equal(root.get("complaintCode"), complaintCodeValue));
				if (statusValue >= 0) {
					predicates.add(criteriaBuilder.equal(root.get("statusId"), statusValue));
				} else {
					predicates.add(criteriaBuilder.lessThan(root.get("statusId"), 2));
				}
			}
			
			if(regionList.size() > 0) {
				Expression<String> regionExpression = root.get("regionId");
				Predicate regionPredicate = regionExpression.in(regionList);
				predicates.add(regionPredicate);
			}
			if(circleList.size() > 0) {
				Expression<String> circleExpression = root.get("circleId");
				Predicate circlePredicate = circleExpression.in(circleList);
				predicates.add(circlePredicate);
			}
			if(divisionList.size() > 0) {
				Expression<String> divisionExpression = root.get("divisionId");
				Predicate divisionPredicate = divisionExpression.in(divisionList);
				predicates.add(divisionPredicate);
			}
			if(subDivisionList.size() > 0) {
				Expression<String> subDivisionExpression = root.get("subDivisionId");
				Predicate subDivisionPredicate = subDivisionExpression.in(subDivisionList);
				predicates.add(subDivisionPredicate);
			}
			if(sectionList.size() > 0) {
				Expression<String> sectionExpression = root.get("sectionId");
				Predicate sectionPredicate = sectionExpression.in(sectionList);
				predicates.add(sectionPredicate);
			}
			if(tagList.size() > 0) {
				Expression<String> tagExpression = root.get("tag");
				Predicate tagPredicate = tagExpression.in(tagList);
				predicates.add(tagPredicate);
			}
			if(receivedFromList.size() > 0) {
				Expression<String> receivedFromExpression = root.get("receivedFrom");
				Predicate receivedFromPredicate = receivedFromExpression.in(receivedFromList);
				predicates.add(receivedFromPredicate);
			}
			if(mobileList.size() > 0) {
				Expression<String> receivedFromExpression = root.get("mobile");
				Predicate mobilePredicate = receivedFromExpression.in(mobileList);
				predicates.add(mobilePredicate);
			}
			
			 if (!deviceList.isEmpty()) { 
		            predicates.add(root.get("device").in(deviceList));
		        }
			 
			if(fromDate != null && toDate == null) {
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.<Date>get("createdOn"), fromDate));
			} else if(fromDate != null && toDate != null) {
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.<Date>get("createdOn"), fromDate));
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.<Date>get("createdOn"), toDate));
			} else if(fromDate == null && toDate != null) {
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.<Date>get("createdOn"), toDate));
			}
			
			criteriaQuery.select(root).where(predicates.toArray(new Predicate[]{}))
										.orderBy(criteriaBuilder.desc(root.get("createdOn")));
			
			Query<ViewComplaintReportBean> query = session.createQuery(criteriaQuery).setMaxResults(30000);
			
			List<ViewComplaintReportBean> viewComplaintReportBeanList = query.getResultList();

			viewComplaintList = convertBeanListToValueBeanList(viewComplaintReportBeanList);
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return viewComplaintList;
	}
	
	public long countTotalComplaints(DataModel dataModel, int complaintCodeValue, int statusValue) throws Exception {
	    long totalCount = 0;
	    SessionFactory factory = null;
	    Session session = null;
	    
	    try {
	        factory = HibernateUtil.getSessionFactory();
	        session = factory.openSession();
	        
	        List<String> compliantCodeList = new ArrayList<>();
	        List<String> statusList = new ArrayList<>();
	        List<Integer> regionList = new ArrayList<>();
	        List<Integer> circleList = new ArrayList<>();
	        List<Integer> divisionList = new ArrayList<>();
	        List<Integer> subDivisionList = new ArrayList<>();
	        List<Integer> sectionList = new ArrayList<>();
	        List<String> tagList = new ArrayList<>();
	        List<String> receivedFromList = new ArrayList<>();
	        List<String> mobileList = new ArrayList<>();
	        List<String> deviceList = new ArrayList<>();
	        
	        if (dataModel.getCompType().length > 0) compliantCodeList.addAll(Arrays.asList(dataModel.getCompType()));
	        if (dataModel.getStatuses().length > 0) statusList.addAll(Arrays.asList(dataModel.getStatuses()));
	        if (dataModel.getRegionFilter() != null && dataModel.getRegionFilter().length > 0) {
	            for (String region : dataModel.getRegionFilter()) {
	                regionList.add(Integer.parseInt(region)); 
	            }
	        }

	        if (dataModel.getCircleFilter() != null && dataModel.getCircleFilter().length > 0) {
	            for (String circle : dataModel.getCircleFilter()) {
	                circleList.add(Integer.parseInt(circle)); 
	            }
	        }

	        if (dataModel.getDivisionFilter() != null && dataModel.getDivisionFilter().length > 0) {
	            for (String division : dataModel.getDivisionFilter()) {
	                divisionList.add(Integer.parseInt(division));  
	            }
	        }

	        if (dataModel.getSubDivisionFilter() != null && dataModel.getSubDivisionFilter().length > 0) {
	            for (String subDivision : dataModel.getSubDivisionFilter()) {
	                subDivisionList.add(Integer.parseInt(subDivision));  
	            }
	        }

	        if (dataModel.getSectionFilter() != null && dataModel.getSectionFilter().length > 0) {
	            for (String section : dataModel.getSectionFilter()) {
	                sectionList.add(Integer.parseInt(section));  
	            }
	        }if (dataModel.getTagArray() != null && dataModel.getTagArray().length > 0) tagList.addAll(Arrays.asList(dataModel.getTagArray()));
	        if (dataModel.getReceivedFromArray() != null && dataModel.getReceivedFromArray().length > 0) receivedFromList.addAll(Arrays.asList(dataModel.getReceivedFromArray()));
	        if (dataModel.getDevices() != null && dataModel.getDevices().length > 0) deviceList.addAll(Arrays.asList(dataModel.getDevices()));
	        
	        if (deviceList.contains("FOC") && !deviceList.contains("admin")) {
	            deviceList.add("FOC");
	            deviceList.add("admin");
	        }
	        if (deviceList.contains("MOBILE")) {
	            deviceList.addAll(Arrays.asList("AMOB", "IMOB", "Android", "mobile", "iOS"));
	        }

	        Date fromDate = dataModel.getFromDate();
	        Date toDate = dataModel.getToDate();

	        if (fromDate != null) {
	            Calendar cal = Calendar.getInstance();
	            cal.setTime(fromDate);
	            cal.set(Calendar.HOUR_OF_DAY, 0);
	            cal.set(Calendar.MINUTE, 0);
	            cal.set(Calendar.SECOND, 0);
	            fromDate = cal.getTime();
	        }
	        if (toDate != null) {
	            Calendar cal = Calendar.getInstance();
	            cal.setTime(toDate);
	            cal.set(Calendar.HOUR_OF_DAY, 23);
	            cal.set(Calendar.MINUTE, 59);
	            cal.set(Calendar.SECOND, 59);
	            toDate = cal.getTime();
	        }

	        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
	        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
	        Root<ViewComplaintReportBean> root = criteriaQuery.from(ViewComplaintReportBean.class);

	        List<Predicate> predicates = new ArrayList<>();
	        
	        if (complaintCodeValue == -1 && statusValue == -1) {
	            if (!compliantCodeList.isEmpty()) {
	                predicates.add(root.get("complaintCode").in(compliantCodeList));
	            }
	            if (!statusList.isEmpty()) {
	                predicates.add(root.get("statusId").in(statusList));
	            }
	        } else {
	            if (complaintCodeValue != -1) {
	                predicates.add(criteriaBuilder.equal(root.get("complaintCode"), complaintCodeValue));
	            }
	            if (statusValue >= 0) {
	                predicates.add(criteriaBuilder.equal(root.get("statusId"), statusValue));
	            } else {
	                predicates.add(criteriaBuilder.lessThan(root.get("statusId"), 2)); 
	            }
	        }

	        if (fromDate != null && toDate == null) {
	            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdOn"), fromDate));
	        } else if (fromDate != null && toDate != null) {
	            predicates.add(criteriaBuilder.between(root.get("createdOn"), fromDate, toDate));
	        } else if (fromDate == null && toDate != null) {
	            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdOn"), toDate));
	        }

	        if (!regionList.isEmpty()) predicates.add(root.get("regionId").in(regionList));
	        if (!circleList.isEmpty()) predicates.add(root.get("circleId").in(circleList));
	        if (!divisionList.isEmpty()) predicates.add(root.get("divisionId").in(divisionList));
	        if (!subDivisionList.isEmpty()) predicates.add(root.get("subDivisionId").in(subDivisionList));
	        if (!sectionList.isEmpty()) predicates.add(root.get("sectionId").in(sectionList));
	        if (!tagList.isEmpty()) predicates.add(root.get("tag").in(tagList));
	        if (!receivedFromList.isEmpty()) predicates.add(root.get("receivedFrom").in(receivedFromList));
	        if (!mobileList.isEmpty()) predicates.add(root.get("mobile").in(mobileList));
	        if (!deviceList.isEmpty()) predicates.add(root.get("device").in(deviceList));

	        criteriaQuery.select(criteriaBuilder.count(root)).where(predicates.toArray(new Predicate[0]));

	        Query<Long> query = session.createQuery(criteriaQuery);
	        totalCount = query.getSingleResult(); 

	    } catch (Exception e) {
	        e.printStackTrace();
	        return 0;  
	    } finally {
	        HibernateUtil.closeSession(factory, session);
	    }

	    return totalCount;
	}

	
	
	private List<ViewComplaintReportValueBean> convertBeanListToValueBeanList(List<ViewComplaintReportBean> viewComplaintReportBeanList) {
		List<ViewComplaintReportValueBean> viewComplaintReportValueBeanList = new ArrayList<ViewComplaintReportValueBean>();
		
		int serialNumber = 1;
		for(ViewComplaintReportBean viewComplaintReportBean : viewComplaintReportBeanList) {
			ViewComplaintReportValueBean viewComplaintReportValueBean = ViewComplaintReportValueBean.convertViewComplaintReportBeanToViewComplaintReportValueBean(viewComplaintReportBean);
			viewComplaintReportValueBean.setSerialNo(serialNumber++);
			viewComplaintReportValueBean.setStatusValue(CCMSConstants.STATUSES[viewComplaintReportValueBean.getStatusId()]);
			int complaintCode = viewComplaintReportBean.getComplaintCode();
			if (complaintCode >= 0 && complaintCode < CCMSConstants.COMPLAINT_NAME.length) {
				viewComplaintReportValueBean.setComplaintCodeValue(CCMSConstants.COMPLAINT_NAME[viewComplaintReportValueBean.getComplaintCode()]);
			}
			viewComplaintReportValueBeanList.add(viewComplaintReportValueBean);
		}
		
		return viewComplaintReportValueBeanList;
	}

	public int[][] getReportDashBoardCMD(DataModel dataModel) {
		DataModel dm = new DataModel();
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			List<String> compliantSubCodeList = new ArrayList<String>();
			List<String> compliantCodeList = new ArrayList<String>();
			List<String> statusList = new ArrayList<String>();
			List<Integer> regionList = new ArrayList<Integer>();
			List<Integer> circleList = new ArrayList<Integer>();
			List<Integer> divisionList = new ArrayList<Integer>();
			List<Integer> subDivisionList = new ArrayList<Integer>();
			List<Integer> sectionList = new ArrayList<Integer>();
			List<String> tagList = new ArrayList<>();
			List<String> receivedFromList = new ArrayList<>();
			List<String> deviceList = new ArrayList<>();
			
//			if (dataModel.getCompType().length > 0) {
//				for (String code : dataModel.getCompType()) {
//					compliantCodeList.add(code);
//				}
//			}
			
			if ("ALL".equals(dataModel.getCompOneType())) {
                
//              List<Integer> allRegionIds = dataModel.getLstRegions().stream()
//                      .map(RegionValueBean::getId)  
//                      .collect(Collectors.toList());
//
//              regionList.addAll(allRegionIds);
          } else {
          	
  				String compType = dataModel.getCompOneType();
  				compliantCodeList.add(compType);
          	
  		}
			
			
			if ("ALL".equals(dataModel.getSubCategory())) {
				
				
				
			}
			else
			{
				System.err.println("SUB CATEGORY::::::::::::"+dataModel.getSubCategory());
				String subCompType = dataModel.getSubCategory();
  				compliantSubCodeList.add(subCompType);
			}
			
//			if (dataModel.getStatuses().length > 0) {
//				for (String status : dataModel.getStatuses()) {
//					statusList.add(status);
//				}
//			}
			
			if ("ALL".equals(dataModel.getStatusesOne())) {
                
//              List<Integer> allRegionIds = dataModel.getLstRegions().stream()
//                      .map(RegionValueBean::getId)  
//                      .collect(Collectors.toList());
//
//              regionList.addAll(allRegionIds);
          } else {
          	
  				String status = dataModel.getStatusesOne();
  				statusList.add(status);
          	
  		}
			
//			if (dataModel.getRegionFilter() != null && dataModel.getRegionFilter().length > 0) {
//				for (String region : dataModel.getRegionFilter()) {
//					regionList.add(Integer.parseInt(region));
//				}
//			}
			
			if ("ALL".equals(dataModel.getRegionOneFilter())) {
		        
		        List<Integer> allRegionIds = dataModel.getLstRegions().stream()
		                .map(RegionValueBean::getId)  
		                .collect(Collectors.toList());

		        regionList.addAll(allRegionIds);
		    } else {
		    	
					String region = dataModel.getRegionOneFilter();
						regionList.add(Integer.parseInt(region));
					
			}
			
//			if (dataModel.getCircleFilter() != null && dataModel.getCircleFilter().length > 0) {
//				for (String circle : dataModel.getCircleFilter()) {
//					circleList.add(Integer.parseInt(circle));
//				}
//			}
			
			if("ALL".equals(dataModel.getCircleOneFilter())) {
				
//				List<Integer> allCircleIds = dataModel.getLstCircles().stream()
//						.map(circle -> circle.getId())
//						.collect(Collectors.toList());
//				
//				circleList.addAll(allCircleIds);
				
			}else {
				if(!dataModel.getCircleOneFilter().isEmpty()) {
					String circle = dataModel.getCircleOneFilter();
					circleList.add(Integer.parseInt(circle));
				}
			}
			
//			if (dataModel.getDivisionFilter() != null && dataModel.getDivisionFilter().length > 0) {
//				for (String division : dataModel.getDivisionFilter()) {
//					divisionList.add(Integer.parseInt(division));
//				}
//			}
			
			if("ALL".equals(dataModel.getDivisionOneFilter())) {
				
//				List<Integer> allDivisionIds = dataModel.getLstDivisions().stream()
//						.map(division -> division.getId())
//						.collect(Collectors.toList());
//				
//				divisionList.addAll(allDivisionIds);
				
			}else {
				if(!dataModel.getDivisionOneFilter().isEmpty()) {
					String division = dataModel.getDivisionOneFilter();
					divisionList.add(Integer.parseInt(division));
				}
			}
			
//			if (dataModel.getSubDivisionFilter() != null && dataModel.getSubDivisionFilter().length > 0) {
//				for (String subDivision : dataModel.getSubDivisionFilter()) {
//					subDivisionList.add(Integer.parseInt(subDivision));
//				}
//			}
			
			 if ("ALL".equals(dataModel.getSubDivisionOneFilter())) {
	                
//	                List<Integer> allSubDivisionIds = dataModel.getLstSubDivisions().stream()
//	                        .map(SubDivisionValueBean::getId)  
//	                        .collect(Collectors.toList());
//
//	                subDivisionList.addAll(allSubDivisionIds);
	            } else {
	            	if(!dataModel.getSubDivisionOneFilter().isEmpty()) {
	    				String subDivision = dataModel.getSubDivisionOneFilter();
	    				subDivisionList.add(Integer.parseInt(subDivision));
	            	}
	    				
	    		}
			
//			if (dataModel.getSectionFilter() != null && dataModel.getSectionFilter().length > 0) {
//				for (String section : dataModel.getSectionFilter()) {
//					sectionList.add(Integer.parseInt(section));
//				}
//			}
			 if ("ALL".equals(dataModel.getSectionOneFilter())) {
	                
//	                List<Integer> allSectionIds = dataModel.getLstSections().stream()
//	                        .map(SectionValueBean::getId)  
//	                        .collect(Collectors.toList());
//
//	                sectionList.addAll(allSectionIds);
	            } else {
	            	if(!dataModel.getSectionOneFilter().isEmpty()) {
	    				String section = dataModel.getSectionOneFilter();
	    				sectionList.add(Integer.parseInt(section));
	            	}
	    		}
			String[] tagArray = dataModel.getTagArray();
			if ( tagArray != null && tagArray.length > 0) {
				for (String tag : tagArray) {
					tagList.add(tag);
				}
			}
			String[] receivedFromArray = dataModel.getReceivedFromArray();
			if ( receivedFromArray != null && receivedFromArray.length > 0) {
				for (String receivedFrom: receivedFromArray) {
					receivedFromList.add(receivedFrom);
				}
			}
			
//			 String[] deviceArray = dataModel.getDevices(); 
//		        if (deviceArray != null && deviceArray.length > 0) {
//		            deviceList.addAll(Arrays.asList(deviceArray));
//		        }
			
			if("ALL".equals(dataModel.getDevicesOne())) {
            	
//            	List<Integer> allDivisionIds = dataModel.getLstDivisions().stream()
//            			.map(division -> division.getId())
//            			.collect(Collectors.toList());
//            	
//            	divisionList.addAll(allDivisionIds);
            }else {
            	if(!dataModel.getDevicesOne().isEmpty()) {
	            	String device = dataModel.getDevicesOne();
	            	deviceList.add(device);
	            	
	            	if (deviceList.contains("FOC") && !deviceList.contains("admin")) {
		            	deviceList.add("FOC");
		            	deviceList.add("admin");
		            }
		            if (deviceList.contains("MOBILE")) {
		            	deviceList.add("AMOB");
		            	deviceList.add("IMOB");
		            	deviceList.add("Android");
		            	deviceList.add("mobile");
		            	deviceList.add("iOS");
		            }
            	}
            }
			
			Date fromDate = dataModel.getFromDate();
			Date toDate = dataModel.getToDate();
			
			if (fromDate != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(fromDate);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				fromDate = cal.getTime();
			}
			
			if (toDate != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(toDate);
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				toDate = cal.getTime();
			}
			System.err.println("SUB CATE compliantSubCodeList.size()"+compliantSubCodeList.size());
			
			String whereCondition = " ";
			
			if(compliantCodeList.size() > 0) {
				whereCondition = loadWhereCondition(whereCondition, "complaintCode", compliantCodeList);
			}
			if(compliantSubCodeList.size() > 0) {
				whereCondition = loadWhereCondition(whereCondition, "subCategoryId", compliantSubCodeList);
			}
			if(statusList.size() > 0) {
				whereCondition = loadWhereCondition(whereCondition, "statusId", statusList);
			}
			if(regionList.size() > 0) {
				whereCondition = loadWhereNumberCondition(whereCondition, "regionId", regionList);
			}
			if(circleList.size() > 0) {
				whereCondition = loadWhereNumberCondition(whereCondition, "circleId", circleList);
			}
			if(divisionList.size() > 0) {
				whereCondition = loadWhereNumberCondition(whereCondition, "divisionId", divisionList);
			}
			if(subDivisionList.size() > 0) {
				whereCondition = loadWhereNumberCondition(whereCondition, "subDivisionId", subDivisionList);
			}
			if(sectionList.size() > 0) {
				whereCondition = loadWhereNumberCondition(whereCondition, "sectionId", sectionList);
			}
			if(tagList.size() > 0) {
				whereCondition = loadWhereCondition(whereCondition, "tag", tagList);
			}
			if(receivedFromList.size() > 0) {
				whereCondition = loadWhereCondition(whereCondition, "receivedFrom", receivedFromList);
			}
			if (deviceList.size() > 0) {
		        whereCondition = loadWhereCondition(whereCondition, "device", deviceList);
	        }
			
			
			if (fromDate != null && toDate == null) {
				if (StringUtils.isNotBlank(whereCondition)) {
					whereCondition += " and ";
				}
				whereCondition += " TRUNC(createdOn) >= TO_DATE('" + GeneralUtil.formatDateForHql(fromDate)	+ "', 'YYYY-MM-DD')";
			} else if (fromDate != null && toDate != null) {
				if (StringUtils.isNotBlank(whereCondition)) {
					whereCondition += " and ";
				}
				whereCondition += " TRUNC(createdOn) >= TO_DATE('" + GeneralUtil.formatDateForHql(fromDate)
						+ "', 'YYYY-MM-DD') and  TRUNC(createdOn) <= TO_DATE('" + GeneralUtil.formatDateForHql(toDate)	+ "', 'YYYY-MM-DD')";
			} else if (fromDate == null && toDate != null) {
				if (StringUtils.isNotBlank(whereCondition)) {
					whereCondition += " and ";
				}
				whereCondition += " TRUNC(createdOn) <= TO_DATE('" + GeneralUtil.formatDateForHql(toDate) + "', 'YYYY-MM-DD')";
			}
			
			String hql = "SELECT complaintCode, statusId, count(*) as cnt  FROM  ViewComplaintBean WHERE " + whereCondition  +
					" GROUP BY " + " complaintCode, statusId ORDER BY complaintCode, statusId";
			
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
					dm.arrReport[ct][status] = Integer.valueOf(row[2].toString());
				}

			} else {
				dm.arrReport[0][0] = -1;
			}
			
		} catch (Exception e) {
			dm.arrReport[0][0] = -1;
			logger.error(ExceptionUtils.getStackTrace(e));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

		return dm.arrReport;
	}
	
	public int[][] getReportDashBoard(DataModel dataModel) {
		DataModel dm = new DataModel();
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			List<String> compliantCodeList = new ArrayList<String>();
			List<String> statusList = new ArrayList<String>();
			List<Integer> regionList = new ArrayList<Integer>();
			List<Integer> circleList = new ArrayList<Integer>();
			List<Integer> divisionList = new ArrayList<Integer>();
			List<Integer> subDivisionList = new ArrayList<Integer>();
			List<Integer> sectionList = new ArrayList<Integer>();
			List<String> tagList = new ArrayList<>();
			List<String> receivedFromList = new ArrayList<>();
			List<String> deviceList = new ArrayList<>();
			
			if (dataModel.getCompType().length > 0) {
				for (String code : dataModel.getCompType()) {
					compliantCodeList.add(code);
				}
			}
			
			if (dataModel.getStatuses().length > 0) {
				for (String status : dataModel.getStatuses()) {
					statusList.add(status);
				}
			}
			
			if (dataModel.getRegionFilter() != null && dataModel.getRegionFilter().length > 0) {
				for (String region : dataModel.getRegionFilter()) {
					regionList.add(Integer.parseInt(region));
				}
			}
			
			
			
			if (dataModel.getCircleFilter() != null && dataModel.getCircleFilter().length > 0) {
				for (String circle : dataModel.getCircleFilter()) {
					circleList.add(Integer.parseInt(circle));
				}
			}
			if (dataModel.getDivisionFilter() != null && dataModel.getDivisionFilter().length > 0) {
				for (String division : dataModel.getDivisionFilter()) {
					divisionList.add(Integer.parseInt(division));
				}
			}
			if (dataModel.getSubDivisionFilter() != null && dataModel.getSubDivisionFilter().length > 0) {
				for (String subDivision : dataModel.getSubDivisionFilter()) {
					subDivisionList.add(Integer.parseInt(subDivision));
				}
			}
			if (dataModel.getSectionFilter() != null && dataModel.getSectionFilter().length > 0) {
				for (String section : dataModel.getSectionFilter()) {
					sectionList.add(Integer.parseInt(section));
				}
			}
			String[] tagArray = dataModel.getTagArray();
			if ( tagArray != null && tagArray.length > 0) {
				for (String tag : tagArray) {
					tagList.add(tag);
				}
			}
			String[] receivedFromArray = dataModel.getReceivedFromArray();
			if ( receivedFromArray != null && receivedFromArray.length > 0) {
				for (String receivedFrom: receivedFromArray) {
					receivedFromList.add(receivedFrom);
				}
			}
			
			 String[] deviceArray = dataModel.getDevices(); 
		        if (deviceArray != null && deviceArray.length > 0) {
		            deviceList.addAll(Arrays.asList(deviceArray));
		        }
			
			Date fromDate = dataModel.getFromDate();
			Date toDate = dataModel.getToDate();
			
			if (fromDate != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(fromDate);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				fromDate = cal.getTime();
			}
			
			if (toDate != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(toDate);
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				toDate = cal.getTime();
			}
			
			String whereCondition = " ";
			
			if(compliantCodeList.size() > 0) {
				whereCondition = loadWhereCondition(whereCondition, "complaintCode", compliantCodeList);
			}
			if(statusList.size() > 0) {
				whereCondition = loadWhereCondition(whereCondition, "statusId", statusList);
			}
			if(regionList.size() > 0) {
				whereCondition = loadWhereNumberCondition(whereCondition, "regionId", regionList);
			}
			if(circleList.size() > 0) {
				whereCondition = loadWhereNumberCondition(whereCondition, "circleId", circleList);
			}
			if(divisionList.size() > 0) {
				whereCondition = loadWhereNumberCondition(whereCondition, "divisionId", divisionList);
			}
			if(subDivisionList.size() > 0) {
				whereCondition = loadWhereNumberCondition(whereCondition, "subDivisionId", subDivisionList);
			}
			if(sectionList.size() > 0) {
				whereCondition = loadWhereNumberCondition(whereCondition, "sectionId", sectionList);
			}
			if(tagList.size() > 0) {
				whereCondition = loadWhereCondition(whereCondition, "tag", tagList);
			}
			if(receivedFromList.size() > 0) {
				whereCondition = loadWhereCondition(whereCondition, "receivedFrom", receivedFromList);
			}
			if (deviceList.size() > 0) {
		        whereCondition = loadWhereCondition(whereCondition, "device", deviceList);
	        }
			
			
			if (fromDate != null && toDate == null) {
				if (StringUtils.isNotBlank(whereCondition)) {
					whereCondition += " and ";
				}
				whereCondition += " TRUNC(createdOn) >= TO_DATE('" + GeneralUtil.formatDateForHql(fromDate)	+ "', 'YYYY-MM-DD')";
			} else if (fromDate != null && toDate != null) {
				if (StringUtils.isNotBlank(whereCondition)) {
					whereCondition += " and ";
				}
				whereCondition += " TRUNC(createdOn) >= TO_DATE('" + GeneralUtil.formatDateForHql(fromDate)
						+ "', 'YYYY-MM-DD') and  TRUNC(createdOn) <= TO_DATE('" + GeneralUtil.formatDateForHql(toDate)	+ "', 'YYYY-MM-DD')";
			} else if (fromDate == null && toDate != null) {
				if (StringUtils.isNotBlank(whereCondition)) {
					whereCondition += " and ";
				}
				whereCondition += " TRUNC(createdOn) <= TO_DATE('" + GeneralUtil.formatDateForHql(toDate) + "', 'YYYY-MM-DD')";
			}
			
			String hql = "SELECT complaintCode, statusId, count(*) as cnt  FROM  ViewComplaintBean WHERE " + whereCondition  +
					" GROUP BY " + " complaintCode, statusId ORDER BY complaintCode, statusId";
			
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
					dm.arrReport[ct][status] = Integer.valueOf(row[2].toString());
				}

			} else {
				dm.arrReport[0][0] = -1;
			}
			
		} catch (Exception e) {
			dm.arrReport[0][0] = -1;
			logger.error(ExceptionUtils.getStackTrace(e));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

		return dm.arrReport;
	}
	
	public int[][] getReportDashBoardAll(DataModel dataModel) {
		DataModel dm = new DataModel();
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			List<String> compliantSubCodeList = new ArrayList<String>();
			List<String> compliantCodeList = new ArrayList<String>();
			List<String> statusList = new ArrayList<String>();
			List<Integer> regionList = new ArrayList<Integer>();
			List<Integer> circleList = new ArrayList<Integer>();
			List<Integer> divisionList = new ArrayList<Integer>();
			List<Integer> subDivisionList = new ArrayList<Integer>();
			List<Integer> sectionList = new ArrayList<Integer>();
			List<String> tagList = new ArrayList<>();
			List<String> receivedFromList = new ArrayList<>();
			List<String> deviceList = new ArrayList<>();
			
			if (dataModel.getCompType().length > 0) {
				for (String code : dataModel.getCompType()) {
					compliantCodeList.add(code);
				}
			}
			if (dataModel.getCompSubType().length > 0) {
				for (String codes : dataModel.getCompSubType()) {
					compliantSubCodeList.add(codes);
				}
			}
			if (dataModel.getStatuses().length > 0) {
				for (String status : dataModel.getStatuses()) {
					statusList.add(status);
				}
			}
			
			dataModel.setRegionFilter(new String[] {"1","2","3","4","5","6","7","8","9","10","11","12"});
			if (dataModel.getRegionFilter() != null && dataModel.getRegionFilter().length > 0) {
				for (String region : dataModel.getRegionFilter()) {
					regionList.add(Integer.parseInt(region));
				}
			}
			if (dataModel.getCircleFilter() != null && dataModel.getCircleFilter().length > 0) {
				for (String circle : dataModel.getCircleFilter()) {
					circleList.add(Integer.parseInt(circle));
				}
			}
			if (dataModel.getDivisionFilter() != null && dataModel.getDivisionFilter().length > 0) {
				for (String division : dataModel.getDivisionFilter()) {
					divisionList.add(Integer.parseInt(division));
				}
			}
			if (dataModel.getSubDivisionFilter() != null && dataModel.getSubDivisionFilter().length > 0) {
				for (String subDivision : dataModel.getSubDivisionFilter()) {
					subDivisionList.add(Integer.parseInt(subDivision));
				}
			}
			if (dataModel.getSectionFilter() != null && dataModel.getSectionFilter().length > 0) {
				for (String section : dataModel.getSectionFilter()) {
					sectionList.add(Integer.parseInt(section));
				}
			}
			String[] tagArray = dataModel.getTagArray();
			if ( tagArray != null && tagArray.length > 0) {
				for (String tag : tagArray) {
					tagList.add(tag);
				}
			}
			String[] receivedFromArray = dataModel.getReceivedFromArray();
			if ( receivedFromArray != null && receivedFromArray.length > 0) {
				for (String receivedFrom: receivedFromArray) {
					receivedFromList.add(receivedFrom);
				}
			}
			
			 String[] deviceArray = dataModel.getDevices(); 
		        if (deviceArray != null && deviceArray.length > 0) {
		            deviceList.addAll(Arrays.asList(deviceArray));
		        }
			
			Date fromDate = dataModel.getFromDate();
			Date toDate = dataModel.getToDate();
			
			if (fromDate != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(fromDate);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				fromDate = cal.getTime();
			}
			
			if (toDate != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(toDate);
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				toDate = cal.getTime();
			}
			
			String whereCondition = " ";
			
			if(compliantCodeList.size() > 0) {
				whereCondition = loadWhereCondition(whereCondition, "complaintCode", compliantCodeList);
			}
			if(compliantSubCodeList.size() > 0) {
				whereCondition = loadWhereCondition(whereCondition, "subCategoryId", compliantSubCodeList);
			}
			if(statusList.size() > 0) {
				whereCondition = loadWhereCondition(whereCondition, "statusId", statusList);
			}
			if(regionList.size() > 0) {
				whereCondition = loadWhereNumberCondition(whereCondition, "regionId", regionList);
			}
			if(circleList.size() > 0) {
				whereCondition = loadWhereNumberCondition(whereCondition, "circleId", circleList);
			}
			if(divisionList.size() > 0) {
				whereCondition = loadWhereNumberCondition(whereCondition, "divisionId", divisionList);
			}
			if(subDivisionList.size() > 0) {
				whereCondition = loadWhereNumberCondition(whereCondition, "subDivisionId", subDivisionList);
			}
			if(sectionList.size() > 0) {
				whereCondition = loadWhereNumberCondition(whereCondition, "sectionId", sectionList);
			}
			if(tagList.size() > 0) {
				whereCondition = loadWhereCondition(whereCondition, "tag", tagList);
			}
			if(receivedFromList.size() > 0) {
				whereCondition = loadWhereCondition(whereCondition, "receivedFrom", receivedFromList);
			}
			if (deviceList.size() > 0) {
		        whereCondition = loadWhereCondition(whereCondition, "device", deviceList);
	        }
			
			
			if (fromDate != null && toDate == null) {
				if (StringUtils.isNotBlank(whereCondition)) {
					whereCondition += " and ";
				}
				whereCondition += " TRUNC(createdOn) >= TO_DATE('" + GeneralUtil.formatDateForHql(fromDate)	+ "', 'YYYY-MM-DD')";
			} else if (fromDate != null && toDate != null) {
				if (StringUtils.isNotBlank(whereCondition)) {
					whereCondition += " and ";
				}
				whereCondition += " TRUNC(createdOn) >= TO_DATE('" + GeneralUtil.formatDateForHql(fromDate)
						+ "', 'YYYY-MM-DD') and  TRUNC(createdOn) <= TO_DATE('" + GeneralUtil.formatDateForHql(toDate)	+ "', 'YYYY-MM-DD')";
			} else if (fromDate == null && toDate != null) {
				if (StringUtils.isNotBlank(whereCondition)) {
					whereCondition += " and ";
				}
				whereCondition += " TRUNC(createdOn) <= TO_DATE('" + GeneralUtil.formatDateForHql(toDate) + "', 'YYYY-MM-DD')";
			}
			
			String hql = "SELECT complaintCode, statusId, count(*) as cnt  FROM  ViewComplaintBean WHERE " + whereCondition  +
					" GROUP BY " + " complaintCode, statusId ORDER BY complaintCode, statusId";
			
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
					dm.arrReport[ct][status] = Integer.valueOf(row[2].toString());
				}

			} else {
				dm.arrReport[0][0] = -1;
			}
			
		} catch (Exception e) {
			dm.arrReport[0][0] = -1;
			logger.error(ExceptionUtils.getStackTrace(e));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

		return dm.arrReport;
	}
	
	public List<ComplaintValueBean> getReportTotal(DataModel dataModel) {
		
		List<ComplaintValueBean> viewComplaintList = new ArrayList<ComplaintValueBean>();
		
		SessionFactory factory = null;
		Session session = null;
		
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			

			List<Integer> subDivisionList = new ArrayList<Integer>();
			List<Integer> sectionList = new ArrayList<Integer>();

			if (dataModel.getSubDivisionFilter() != null && dataModel.getSubDivisionFilter().length > 0) {
				for (String subDivision : dataModel.getSubDivisionFilter()) {
					subDivisionList.add(Integer.parseInt(subDivision));
				}
			}
			
			if (dataModel.getSectionFilter() != null && dataModel.getSectionFilter().length > 0) {
				for (String section : dataModel.getSectionFilter()) {
					sectionList.add(Integer.parseInt(section));
				}
			}
		
			
			 StringBuilder hql = new StringBuilder("SELECT c.sectionBean.id, s.name, c.complaintCode, COUNT(c) " +
                     "FROM ComplaintBean c " +
                     "JOIN c.sectionBean s " +
                     "WHERE c.subDivisionBean.id = s.subDivisionBean.id ");
			 
			    if (!subDivisionList.isEmpty()) {
		            hql.append("AND c.subDivisionBean.id IN (:subDivisionList) ");
		        }
		        if (!sectionList.isEmpty()) {
		            hql.append("AND c.sectionBean.id IN (:sectionList) ");
		        }

		        hql.append("GROUP BY c.sectionBean.id, s.name, c.complaintCode " +
		                   "ORDER BY c.sectionBean.id");
			
		        Query<?> query = session.createQuery(hql.toString());
		        
		        if (!subDivisionList.isEmpty()) {
		            query.setParameterList("subDivisionList", subDivisionList);
		        }
		        if (!sectionList.isEmpty()) {
		            query.setParameterList("sectionList", sectionList);
		        }

			List<?> rows = query.list();		
			
			Map<Integer, ComplaintValueBean> sectionMap = new HashMap<>();

		        for (Object row : rows) {
		        	
		            Object[] data = (Object[]) row;
		            int sectionId = ((Number) data[0]).intValue();
		            String sectionName = (String) data[1];
		            int complaintCode = (Integer) data[2];
		            int count = ((Number) data[3]).intValue();

		            ComplaintValueBean bean = sectionMap.get(sectionId);
		            
		            if (bean == null) {
		                bean = new ComplaintValueBean();
		                bean.setSectionId(sectionId);
		                bean.setSectionName(sectionName);
		               
		                bean.setComplaintCounts(new int[10]); 
		                
		                sectionMap.put(sectionId, bean);
		            }
		            
		            
		            bean.getComplaintCounts()[complaintCode] += count;
		        }

		        viewComplaintList = new ArrayList<>(sectionMap.values());
		        
		} catch (Exception e) {
			
			logger.error(ExceptionUtils.getStackTrace(e));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return viewComplaintList;	
	}
	
	private static String loadWhereCondition(String whereCondition, String fieldName, List<String> list) {
		if (StringUtils.isNotBlank(whereCondition)) {
			whereCondition += " and ";
		}
		whereCondition += fieldName + " in ("+ loadStringCondition(list) +") ";
		return whereCondition;
	}
	
	private static String loadWhereNumberCondition(String whereCondition, String fieldName, List<Integer> list) {
		if (StringUtils.isNotBlank(whereCondition)) {
			whereCondition += " and ";
		}
		whereCondition += fieldName + " in ("+ loadIntegerCondition(list) +") ";
		return whereCondition;
	}
	
	private static String loadStringCondition(List<String> list) {
		String condition = "";
		for (String id : list) {
			if (StringUtils.isNotBlank(condition)) {
				condition += ",";
			}
			condition += "'" + id + "'";
		}
		return condition;
	}
	
	private static String loadIntegerCondition(List<Integer> list) {
		String condition = "";
		for (Integer id : list) {
			if (StringUtils.isNotBlank(condition)) {
				condition += ",";
			}
			condition += id;
		}
		return condition;
	}
	
	
	public List<RegionValueBean> getRegionList(List<Integer> regionIdList) throws Exception {
		
		List<RegionValueBean> regionList = new ArrayList<RegionValueBean>();
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<RegionBean> criteriaQuery = criteriaBuilder.createQuery(RegionBean.class);
			Root<RegionBean> root = criteriaQuery.from(RegionBean.class);
			
			//Constructing list of parameters
			List<Predicate> predicates = new ArrayList<Predicate>();
			Expression<String> regionExpression = root.get("id");
			Predicate regionPredicate = regionExpression.in(regionIdList);
			predicates.add(regionPredicate);
			
			//Constructing order list of parameters
			List<Order> orderList = new ArrayList<Order>();
			orderList.add(criteriaBuilder.asc(root.get("name")));
			
			criteriaQuery.select(root).where(predicates.toArray(new Predicate[]{}))
			.orderBy(orderList);

			Query<RegionBean> query = session.createQuery(criteriaQuery);
			
			List<RegionBean> regionBeanList = query.getResultList();
			
			for(RegionBean regionBean : regionBeanList) {
				regionList.add(RegionValueBean.convertRegionBeanToRegionValueBean(regionBean));
			}
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return regionList;
	}
	
	
	public List<CircleValueBean> getCircleList(List<Integer> circleIdList) throws Exception {
		
		List<CircleValueBean> circleList = new ArrayList<CircleValueBean>();
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<CircleBean> criteriaQuery = criteriaBuilder.createQuery(CircleBean.class);
			Root<CircleBean> root = criteriaQuery.from(CircleBean.class);
			
			//Constructing list of parameters
			List<Predicate> predicates = new ArrayList<Predicate>();
			Expression<String> circleExpression = root.get("id");
			Predicate circlePredicate = circleExpression.in(circleIdList);
			predicates.add(circlePredicate);
			
			//Constructing order list of parameters
			List<Order> orderList = new ArrayList<Order>();
			orderList.add(criteriaBuilder.asc(root.get("name")));
			
			criteriaQuery.select(root).where(predicates.toArray(new Predicate[]{}))
			.orderBy(orderList);

			Query<CircleBean> query = session.createQuery(criteriaQuery);
			
			List<CircleBean> circleBeanList = query.getResultList();
			
			for(CircleBean circleBean : circleBeanList) {
				circleList.add(CircleValueBean.convertCircleBeanToCircleValueBean(circleBean));
			}
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return circleList;
	}
	
	
public List<DivisionValueBean> getDivisionList(List<Integer> divisionIdList) throws Exception {
		
		List<DivisionValueBean> divisionList = new ArrayList<DivisionValueBean>();
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<DivisionBean> criteriaQuery = criteriaBuilder.createQuery(DivisionBean.class);
			Root<DivisionBean> root = criteriaQuery.from(DivisionBean.class);
			
			//Constructing list of parameters
			List<Predicate> predicates = new ArrayList<Predicate>();
			Expression<String> divisionExpression = root.get("id");
			Predicate divisionPredicate = divisionExpression.in(divisionIdList);
			predicates.add(divisionPredicate);
			
			//Constructing order list of parameters
			List<Order> orderList = new ArrayList<Order>();
			orderList.add(criteriaBuilder.asc(root.get("name")));
			
			criteriaQuery.select(root).where(predicates.toArray(new Predicate[]{}))
			.orderBy(orderList);

			Query<DivisionBean> query = session.createQuery(criteriaQuery);
			
			List<DivisionBean> divisionBeanList = query.getResultList();
			
			for(DivisionBean divisionBean : divisionBeanList) {
				divisionList.add(DivisionValueBean.convertDivisionBeanToDivisionValueBean(divisionBean));
			}
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return divisionList;
	}


	public List<SubDivisionValueBean> getSubDivisionList(List<Integer> subDivisionIdList) throws Exception {
		
		List<SubDivisionValueBean> subDivisionList = new ArrayList<SubDivisionValueBean>();
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<SubDivisionBean> criteriaQuery = criteriaBuilder.createQuery(SubDivisionBean.class);
			Root<SubDivisionBean> root = criteriaQuery.from(SubDivisionBean.class);
			
			//Constructing list of parameters
			List<Predicate> predicates = new ArrayList<Predicate>();
			Expression<String> subDivisionExpression = root.get("id");
			Predicate subDivisionPredicate = subDivisionExpression.in(subDivisionIdList);
			predicates.add(subDivisionPredicate);
			
			//Constructing order list of parameters
			List<Order> orderList = new ArrayList<Order>();
			orderList.add(criteriaBuilder.asc(root.get("name")));
			
			criteriaQuery.select(root).where(predicates.toArray(new Predicate[]{}))
			.orderBy(orderList);
	
			Query<SubDivisionBean> query = session.createQuery(criteriaQuery);
			
			List<SubDivisionBean> subDivisionBeanList = query.getResultList();
			
			for(SubDivisionBean subDivisionBean : subDivisionBeanList) {
				subDivisionList.add(SubDivisionValueBean.convertSubDivisionBeanToSubDivisionValueBean(subDivisionBean));
			}
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return subDivisionList;
	}
	
	public List<SectionValueBean> getSectionList(List<Integer> sectionIdList) throws Exception {
		
		List<SectionValueBean> sectionList = new ArrayList<SectionValueBean>();
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<SectionBean> criteriaQuery = criteriaBuilder.createQuery(SectionBean.class);
			Root<SectionBean> root = criteriaQuery.from(SectionBean.class);
			
			//Constructing list of parameters
			List<Predicate> predicates = new ArrayList<Predicate>();
			Expression<String> sectionExpression = root.get("id");
			Predicate sectionPredicate = sectionExpression.in(sectionIdList);
			predicates.add(sectionPredicate);
			
			//Constructing order list of parameters
			List<Order> orderList = new ArrayList<Order>();
			orderList.add(criteriaBuilder.asc(root.get("name")));
			
			criteriaQuery.select(root).where(predicates.toArray(new Predicate[]{}))
			.orderBy(orderList);

			Query<SectionBean> query = session.createQuery(criteriaQuery);
			
			List<SectionBean> sectionBeanList = query.getResultList();
			
			for(SectionBean sectionBean : sectionBeanList) {
				sectionList.add(SectionValueBean.convertSectionBeanToSectionValueBean(sectionBean));
			}
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return sectionList;
	}
	
	
	public List<DistrictValueBean> listDistrict(List<Integer> circleIdList) throws Exception {
		
		List<DistrictValueBean> districtList = new ArrayList<DistrictValueBean>();
		DistrictValueBean districtValueBean = null;
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<Object[]> criteriaQuery = criteriaBuilder.createQuery(Object[].class);
			Root<DistrictBean> root = criteriaQuery.from(DistrictBean.class);
			
			//Constructing list of parameters
			List<Predicate> predicates = new ArrayList<Predicate>();
			Expression<String> circleExpression = root.get("circleBean").get("id");
			Predicate circlePredicate = circleExpression.in(circleIdList);
			predicates.add(circlePredicate);
			
			//Constructing order list of parameters
			List<Order> orderList = new ArrayList<Order>();
			orderList.add(criteriaBuilder.asc(criteriaBuilder.upper(root.get("name"))));
			
			criteriaQuery.select(root.get("name")).where(predicates.toArray(new Predicate[]{})).distinct(true).orderBy(orderList);

			Query<Object[]> query = session.createQuery(criteriaQuery);
			
			List<Object[]> objectList = query.getResultList();
			
			for (Object object : objectList) {
				districtValueBean = new DistrictValueBean();
				districtValueBean.setName(object.toString());
				districtList.add(districtValueBean);
			}
				
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return districtList;
	}
	
	public List<DistrictValueBean> smListDistrict() throws Exception {
		
		List<DistrictValueBean> districtList = new ArrayList<DistrictValueBean>();
		DistrictValueBean districtValueBean = null;
		SessionFactory factory = null;
		Session session = null;
		try {
			
			 factory = HibernateUtil.getSessionFactory();
		        session = factory.openSession();

		        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		        CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
		        Root<DistrictBean> root = criteriaQuery.from(DistrictBean.class);

		        // Constructing order list of parameters
		        List<Order> orderList = new ArrayList<Order>();
		        orderList.add(criteriaBuilder.asc(criteriaBuilder.upper(root.get("name"))));

		        criteriaQuery.select(root.get("name")).distinct(true).orderBy(orderList);

		        Query<String> query = session.createQuery(criteriaQuery);

		        List<String> nameList = query.getResultList();

		        for (String name : nameList) {
		            districtValueBean = new DistrictValueBean();
		            districtValueBean.setName(name);
		            districtList.add(districtValueBean);
		        }

			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return districtList;
	}
	
	
	public ComplaintValueBean getComplaintByServiceNumberAndComplaintType(String serviceNumber, String complaintType, Integer userId) throws Exception {
		
		ComplaintValueBean complaintValueBean = null;
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<ComplaintBean> criteriaQuery = criteriaBuilder.createQuery(ComplaintBean.class);
			Root<ComplaintBean> root = criteriaQuery.from(ComplaintBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.and(criteriaBuilder.equal(root.get("serviceNumber"), serviceNumber),
					criteriaBuilder.equal(root.get("complaintType"), complaintType),
					criteriaBuilder.equal(root.get("publicUserBean").get("id"), userId),
					criteriaBuilder.notEqual(root.get("statusId"), CCMSConstants.COMPLETED),
					criteriaBuilder.equal(root.get("device"), "FOC")))
			.orderBy(criteriaBuilder.desc(root.get("id")));;

			Query<ComplaintBean> query = session.createQuery(criteriaQuery);
			
			List<ComplaintBean> complaintList = query.getResultList();
			
			if(complaintList.size() > 0) {
				complaintValueBean = ComplaintValueBean.convertComplaintBeanTocomplaintValueBean(complaintList.get(0));
			}
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return complaintValueBean;
	}
	
	public ComplaintValueBean getComplaintByComplaintType(String complaintType, Integer userId, Integer sectionId) throws Exception {
		
		ComplaintValueBean complaintValueBean = null;
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<ComplaintBean> criteriaQuery = criteriaBuilder.createQuery(ComplaintBean.class);
			Root<ComplaintBean> root = criteriaQuery.from(ComplaintBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.and(root.get("serviceNumber").isNull(),
					criteriaBuilder.equal(root.get("complaintType"), complaintType),
					criteriaBuilder.equal(root.get("publicUserBean").get("id"), userId),
					criteriaBuilder.equal(root.get("sectionBean").get("id"), sectionId),
					criteriaBuilder.notEqual(root.get("statusId"), CCMSConstants.COMPLETED)),
					criteriaBuilder.equal(root.get("device"), "FOC"))
			.orderBy(criteriaBuilder.desc(root.get("id")));;

			Query<ComplaintBean> query = session.createQuery(criteriaQuery);
			
			List<ComplaintBean> complaintList = query.getResultList();
			
			if(complaintList.size() > 0) {
				complaintValueBean = ComplaintValueBean.convertComplaintBeanTocomplaintValueBean(complaintList.get(0));
			}
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return complaintValueBean;

	}
	
	public DistrictValueBean getRegionIdByDistrictId(Integer districtId) throws Exception {
		
		DistrictValueBean districtValueBean = null;
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<DistrictBean> criteriaQuery = criteriaBuilder.createQuery(DistrictBean.class);
			Root<DistrictBean> root = criteriaQuery.from(DistrictBean.class);

			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), districtId));

			Query<DistrictBean> query = session.createQuery(criteriaQuery);
			
			List<DistrictBean> districtBeanList = query.getResultList();
			
			if(districtBeanList.size() > 0) {
				districtValueBean = DistrictValueBean.convertDistrictBeanToDistrictValueBean(districtBeanList.get(0));
			}
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return districtValueBean;
	
	}
	
	public SectionValueBean getSectionByCodeAndRegionId(String sectionCode, Integer regionId) throws Exception {
		
		SectionValueBean sectionValueBean = null;
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<SectionBean> criteriaQuery = criteriaBuilder.createQuery(SectionBean.class);
			Root<SectionBean> root = criteriaQuery.from(SectionBean.class);

			criteriaQuery.select(root).where(criteriaBuilder.and(criteriaBuilder.equal(root.get("code"), sectionCode),
					criteriaBuilder.equal(root.get("regionBean").get("id"), regionId)));

			Query<SectionBean> query = session.createQuery(criteriaQuery);
			
			List<SectionBean> sectionBeanList = query.getResultList();
			
			if(sectionBeanList.size() > 0) {
				 sectionValueBean = SectionValueBean.convertSectionBeanToSectionValueBean(sectionBeanList.get(0));
			}
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return sectionValueBean;
	}
	
	public Integer registerPowerComplaint(ConsumerServiceValueBean consumerServiceValueBean, DataModel dataModel, PublicUserValueBean publicUserValueBean, boolean socialMedia,String userName) {
		
		Integer complaintId = null;
				
		SessionFactory factory = null;
		Session session = null;
		Transaction transaction = null;
		
		try {
			
			GeneralDao dao = new GeneralDao();
			
			CategoryBean categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_POWER_FAILURE);
			SubCategoryBean subCategoryBean = dao.getSubCategoryBeanById(dataModel.getSubCategory());
			
			Integer complaintType = Integer.parseInt(dataModel.getComplaintType());
			
			if(complaintType == 1) {
				categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_VOLTAGE_RELATED);
			}
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			ComplaintBean complaintBean = new ComplaintBean();
			
			ComplaintHistoryBean complaintHistoryBean = new ComplaintHistoryBean();
			
			complaintBean.setComplaintType(CCMSConstants.COMPLAINT_TYPE_CODE[Integer.parseInt(dataModel.getComplaintType())]);
			
			if(consumerServiceValueBean.getRegionId() != null && consumerServiceValueBean.getRegionId() > 0) {
				RegionBean regionBean = new RegionBean();
				regionBean.setId(consumerServiceValueBean.getRegionId());
				complaintBean.setRegionBean(regionBean);
				complaintHistoryBean.setRegionBean(regionBean);
			}
			if(consumerServiceValueBean.getCircleId() != null && consumerServiceValueBean.getCircleId() > 0) {
				CircleBean circleBean = new CircleBean();
				circleBean.setId(consumerServiceValueBean.getCircleId());
				complaintBean.setCircleBean(circleBean);
				complaintHistoryBean.setCircleBean(circleBean);
			}
			if(consumerServiceValueBean.getDivisionId() != null && consumerServiceValueBean.getDivisionId() > 0) {
				DivisionBean divisionBean = new DivisionBean();
				divisionBean.setId(consumerServiceValueBean.getDivisionId());
				complaintBean.setDivisionBean(divisionBean);
				complaintHistoryBean.setDivisionBean(divisionBean);
			}
			if(consumerServiceValueBean.getSubDivisionId() != null && consumerServiceValueBean.getSubDivisionId() > 0) {
				SubDivisionBean subDivisionBean = new SubDivisionBean();
				subDivisionBean.setId(consumerServiceValueBean.getSubDivisionId());
				complaintBean.setSubDivisionBean(subDivisionBean);
				complaintHistoryBean.setSubDivisionBean(subDivisionBean);
			}
			if(consumerServiceValueBean.getSectionId() != null && consumerServiceValueBean.getSectionId() > 0) {
				SectionBean sectionBean = new SectionBean();
				sectionBean.setId(consumerServiceValueBean.getSectionId());
				complaintBean.setSectionBean(sectionBean);
				complaintHistoryBean.setSectionBean(sectionBean);
			}
			
			PublicUserBean publicUserBean = new PublicUserBean();
			publicUserBean.setId(publicUserValueBean.getId());
			complaintBean.setPublicUserBean(publicUserBean);
			complaintBean.setServiceNumber(consumerServiceValueBean.getServiceNumber());
			complaintBean.setLandmark(dataModel.getLandMark());
			complaintBean.setServiceName(consumerServiceValueBean.getServiceName());
			complaintBean.setServiceAddress(consumerServiceValueBean.getServiceAddress());
			complaintBean.setDescription(dataModel.getComplaintDescription());
			complaintBean.setReceivedFrom(dataModel.getReceivedFrom());
			complaintBean.setTag(dataModel.getTag());
			
			String defaultMobile = publicUserValueBean.getMobile();
			String alternateMobile = dataModel.getAlternateMobileNo();
			if (alternateMobile != null && alternateMobile.trim().length() > 0) {
				defaultMobile = alternateMobile;
			}
			complaintBean.setAlternateMobileNo(defaultMobile);

			RescueBean rescueBean = new RescueBean();
			rescueBean.setId(1);
			complaintBean.setRescueBean(rescueBean);
			complaintBean.setCategoryBean(categoryBean);
			complaintBean.setSubCategoryBean(subCategoryBean);
			complaintBean.setLatitude("");
			complaintBean.setLongitude("");
			complaintBean.setImage1("");
			complaintBean.setImage2("");
			complaintBean.setCompEntUser(userName);
			complaintBean.setStatusId(CCMSConstants.PENDING);
			if (socialMedia) {
				complaintBean.setDevice("SM");
			} else {
				complaintBean.setDevice("FOC");
			}
			complaintBean.setComplaintCode(Integer.parseInt(dataModel.getComplaintType()));

			
			complaintHistoryBean.setPublicUserBean(publicUserBean);
			complaintHistoryBean.setComplaintBean(complaintBean);
			complaintHistoryBean.setStatusId(complaintBean.getStatusId());
			complaintHistoryBean.setDescription(complaintBean.getDescription());
			complaintBean.addToHistory(complaintHistoryBean);

			transaction = session.beginTransaction();


			session.save(complaintBean);
			
			transaction.commit();
			
			session.refresh(complaintBean);
			
			complaintId = complaintBean.getId();
			
			if(StringUtils.isNotBlank(dataModel.getFieldWorkerDropDown())) {
				
				CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<FieldWorkerBean> criteriaQuery = criteriaBuilder.createQuery(FieldWorkerBean.class);
				Root<FieldWorkerBean> root = criteriaQuery.from(FieldWorkerBean.class);
				criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), dataModel.getFieldWorkerDropDown()));

				Query<FieldWorkerBean> query = session.createQuery(criteriaQuery);
				
				List<FieldWorkerBean> list = query.getResultList();
				
				if(list.size() > 0) {
					
					FieldWorkerBean fieldWorkerBean = list.get(0);
					
					FieldWorkerComplaintBean fieldWorkerComplaintBean = new FieldWorkerComplaintBean();
					fieldWorkerComplaintBean.setComplaintBean(complaintBean);
					fieldWorkerComplaintBean.setFieldWorkerId(fieldWorkerBean.getId());
					fieldWorkerComplaintBean.setStatusId(CCMSConstants.PENDING);
					
					session.save(fieldWorkerComplaintBean);
					
					String fieldValue = consumerServiceValueBean.getServiceNumber();
					fieldValue += ", " + consumerServiceValueBean.getServiceName();
					if(StringUtils.isNotBlank(dataModel.getLandMark())) {
						fieldValue += ", " + dataModel.getLandMark();
					}
					fieldValue += ", " + publicUserValueBean.getMobile();
										
					String message = "CmplNo:"+complaintBean.getId()+"SCno:"+fieldValue+"-TANGEDCO";
					
					String smsId = SMSUtil.sendSMS(null, fieldWorkerBean.getMobile(), message);
					System.err.println("THE PF /VF ---> FIELD WORKER SMS_____________"+fieldWorkerBean.getMobile());

					SmsClient.sendSms(smsId);
					
					message = "CmplNo"+complaintBean.getId()+" :Registered "+GeneralUtil.formatDateWithTime(new Date())+" allotted to "+fieldWorkerBean.getName()+"/"+fieldWorkerBean.getMobile()+". Save Electricity - TANGEDCO";
					
					String smsSentId = SMSUtil.sendSMS(null, publicUserValueBean.getMobile(), message);
					
					System.err.println("THE PF /VF ---> USER SMS_____________"+publicUserValueBean.getMobile());

					SmsClient.sendSms(smsSentId);
				}
				
			} else {
				
				String message = "Your CmplNo"+complaintBean.getId()+" has been Registered "+GeneralUtil.formatDateWithTime(new Date())+". Save Electricity - TANGEDCO";
				
				String smsId = SMSUtil.sendSMS(null, publicUserValueBean.getMobile(), message);
				
				SmsClient.sendSms(smsId);
			}
			

			
		} catch (Exception e) {
			if(transaction != null) {
				transaction.rollback();
			}
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
			return complaintId;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return complaintId;
	}
	
	
	public Integer registerPowerComplaint(SectionValueBean sectionValueBean, DataModel dataModel, PublicUserValueBean publicUserValueBean , String serviceApplicationNo, boolean socialMedia,String userName) {
		
		Integer complaintId = null;
		
		SessionFactory factory = null;
		Session session = null;
		Transaction transaction = null;
		CategoryBean categoryBean =null;
		try {
			
			GeneralDao dao = new GeneralDao();
			
			
			Integer complaintType = Integer.parseInt(dataModel.getComplaintType());
			
			if(complaintType == 1) {
				 categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_VOLTAGE_RELATED);
			}
			else if(complaintType == 0) {
				 categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_POWER_FAILURE);
			}else if(complaintType == 7){
				 categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_APPLICATION_RELATED);
			}
			SubCategoryBean subCategoryBean = dao.getSubCategoryBeanById(dataModel.getSubCategory());
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			ComplaintBean complaintBean = new ComplaintBean();
			
			ComplaintHistoryBean complaintHistoryBean = new ComplaintHistoryBean();
			
			complaintBean.setComplaintType(CCMSConstants.COMPLAINT_TYPE_CODE[Integer.parseInt(dataModel.getComplaintType())]);
			
			if(sectionValueBean.getRegionId() != null && sectionValueBean.getRegionId() > 0) {
				RegionBean regionBean = new RegionBean();
				regionBean.setId(sectionValueBean.getRegionId());
				complaintBean.setRegionBean(regionBean);
				complaintHistoryBean.setRegionBean(regionBean);
			}
			if(sectionValueBean.getCircleId() != null && sectionValueBean.getCircleId() > 0) {
				CircleBean circleBean = new CircleBean();
				circleBean.setId(sectionValueBean.getCircleId());
				complaintBean.setCircleBean(circleBean);
				complaintHistoryBean.setCircleBean(circleBean);
			}
			if(sectionValueBean.getDivisionId() != null && sectionValueBean.getDivisionId() > 0) {
				DivisionBean divisionBean = new DivisionBean();
				divisionBean.setId(sectionValueBean.getDivisionId());
				complaintBean.setDivisionBean(divisionBean);
				complaintHistoryBean.setDivisionBean(divisionBean);
			}
			if(sectionValueBean.getSubDivisionId() != null && sectionValueBean.getSubDivisionId() > 0) {
				SubDivisionBean subDivisionBean = new SubDivisionBean();
				subDivisionBean.setId(sectionValueBean.getSubDivisionId());
				complaintBean.setSubDivisionBean(subDivisionBean);
				complaintHistoryBean.setSubDivisionBean(subDivisionBean);
			}
			if(sectionValueBean.getId() != null && sectionValueBean.getId() > 0) {
				SectionBean sectionBean = new SectionBean();
				sectionBean.setId(sectionValueBean.getId());
				complaintBean.setSectionBean(sectionBean);
				complaintHistoryBean.setSectionBean(sectionBean);
			}
			
			PublicUserBean publicUserBean = new PublicUserBean();
			publicUserBean.setId(publicUserValueBean.getId());
			complaintBean.setPublicUserBean(publicUserBean);
			complaintBean.setServiceAddress(dataModel.getAddress());
			complaintBean.setLandmark(dataModel.getLandMark());
			complaintBean.setDescription(dataModel.getComplaintDescription());
			complaintBean.setReceivedFrom(dataModel.getReceivedFrom());
			complaintBean.setTag(dataModel.getTag());

			String defaultMobile = publicUserValueBean.getMobile();
			String alternateMobile = dataModel.getAlternateMobileNo();
			if (alternateMobile != null && alternateMobile.trim().length() > 0) {
				defaultMobile = alternateMobile;
			}
			complaintBean.setAlternateMobileNo(defaultMobile);
			
			RescueBean rescueBean = new RescueBean();
			rescueBean.setId(1);
			complaintBean.setRescueBean(rescueBean);
			complaintBean.setCategoryBean(categoryBean);
			complaintBean.setSubCategoryBean(subCategoryBean);
			complaintBean.setLatitude("");
			complaintBean.setLongitude("");
			complaintBean.setImage1("");
			complaintBean.setImage2("");
			complaintBean.setCompEntUser(userName);
			complaintBean.setStatusId(CCMSConstants.PENDING);
			if (socialMedia) {
				complaintBean.setDevice("SM");
			} else {
				complaintBean.setDevice("FOC");
			}
			complaintBean.setComplaintCode(Integer.parseInt(dataModel.getComplaintType()));
			if(serviceApplicationNo != null || serviceApplicationNo != "" ) {
				complaintBean.setApplicationNo(dataModel.getApplicationNo());
			}
			
			complaintHistoryBean.setPublicUserBean(publicUserBean);
			complaintHistoryBean.setComplaintBean(complaintBean);
			complaintHistoryBean.setStatusId(complaintBean.getStatusId());
			complaintHistoryBean.setDescription(complaintBean.getDescription());
			complaintBean.addToHistory(complaintHistoryBean);

			transaction = session.beginTransaction();

			session.save(complaintBean);
			
            transaction.commit();
			
			session.refresh(complaintBean);
			
			complaintId = complaintBean.getId();
			
			if(StringUtils.isNotBlank(dataModel.getFieldWorkerDropDown())) {
				
				CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<FieldWorkerBean> criteriaQuery = criteriaBuilder.createQuery(FieldWorkerBean.class);
				Root<FieldWorkerBean> root = criteriaQuery.from(FieldWorkerBean.class);
				criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), dataModel.getFieldWorkerDropDown()));

				Query<FieldWorkerBean> query = session.createQuery(criteriaQuery);
				
				List<FieldWorkerBean> list = query.getResultList();
				
				if(list.size() > 0) {
					
					FieldWorkerBean fieldWorkerBean = list.get(0);
					
					FieldWorkerComplaintBean fieldWorkerComplaintBean = new FieldWorkerComplaintBean();
					fieldWorkerComplaintBean.setComplaintBean(complaintBean);
					fieldWorkerComplaintBean.setFieldWorkerId(fieldWorkerBean.getId());
					fieldWorkerComplaintBean.setStatusId(CCMSConstants.PENDING);
					
					session.save(fieldWorkerComplaintBean);
					
					String fieldValue = dataModel.getAddress();
					if(StringUtils.isNotBlank(dataModel.getLandMark())) {
						fieldValue += ", " + dataModel.getLandMark();
					}
					fieldValue += ", " + publicUserValueBean.getMobile();
					
					String message = "CmplNo:"+complaintBean.getId()+"SCno:"+fieldValue+"-TANGEDCO";
					
					String smsId = SMSUtil.sendSMS(null, fieldWorkerBean.getMobile(), message);
					
					SmsClient.sendSms(smsId);
					
					message = "CmplNo"+complaintBean.getId()+" :Registered "+GeneralUtil.formatDateWithTime(new Date())+" allotted to "+fieldWorkerBean.getName()+"/"+fieldWorkerBean.getMobile()+". Save Electricity - TANGEDCO";
					
					String smsSentId = SMSUtil.sendSMS(null, publicUserValueBean.getMobile(), message);
					
					SmsClient.sendSms(smsSentId);
				}
				
			} else {
				
				String message = "Your CmplNo"+complaintBean.getId()+" has been Registered "+GeneralUtil.formatDateWithTime(new Date())+". Save Electricity - TANGEDCO";
				
				String smsId = SMSUtil.sendSMS(null, publicUserValueBean.getMobile(), message);
				
				SmsClient.sendSms(smsId);
			}
			
			
			
		} catch (Exception e) {
			if(transaction != null) {
				transaction.rollback();
			}
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
			return complaintId;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return complaintId;
	}
	
	public Integer registerMeterAndBillComplaint(ConsumerServiceValueBean consumerServiceValueBean, DataModel dataModel, PublicUserValueBean publicUserValueBean, boolean socialMedia,String userName) {
		
		Integer complaintId = null;
				
		SessionFactory factory = null;
		Session session = null;
		Transaction transaction = null;
		
		try {
			
			GeneralDao dao = new GeneralDao();
			
			CategoryBean categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_METER_RELATED);
			SubCategoryBean subCategoryBean = dao.getSubCategoryBeanById(dataModel.getSubCategory());
			
			Integer complaintType = Integer.parseInt(dataModel.getComplaintType());
			
			if(complaintType == 3) {
				categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_BILLING);
			}
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			ComplaintBean complaintBean = new ComplaintBean();
			
			ComplaintHistoryBean complaintHistoryBean = new ComplaintHistoryBean();
			
			complaintBean.setComplaintType(CCMSConstants.COMPLAINT_TYPE_CODE[Integer.parseInt(dataModel.getComplaintType())]);
			
			if(consumerServiceValueBean.getRegionId() != null && consumerServiceValueBean.getRegionId() > 0) {
				RegionBean regionBean = new RegionBean();
				regionBean.setId(consumerServiceValueBean.getRegionId());
				complaintBean.setRegionBean(regionBean);
				complaintHistoryBean.setRegionBean(regionBean);
			}
			if(consumerServiceValueBean.getCircleId() != null && consumerServiceValueBean.getCircleId() > 0) {
				CircleBean circleBean = new CircleBean();
				circleBean.setId(consumerServiceValueBean.getCircleId());
				complaintBean.setCircleBean(circleBean);
				complaintHistoryBean.setCircleBean(circleBean);
			}
			if(consumerServiceValueBean.getDivisionId() != null && consumerServiceValueBean.getDivisionId() > 0) {
				DivisionBean divisionBean = new DivisionBean();
				divisionBean.setId(consumerServiceValueBean.getDivisionId());
				complaintBean.setDivisionBean(divisionBean);
				complaintHistoryBean.setDivisionBean(divisionBean);
			}
			if(consumerServiceValueBean.getSubDivisionId() != null && consumerServiceValueBean.getSubDivisionId() > 0) {
				SubDivisionBean subDivisionBean = new SubDivisionBean();
				subDivisionBean.setId(consumerServiceValueBean.getSubDivisionId());
				complaintBean.setSubDivisionBean(subDivisionBean);
				complaintHistoryBean.setSubDivisionBean(subDivisionBean);
			}
			if(consumerServiceValueBean.getSectionId() != null && consumerServiceValueBean.getSectionId() > 0) {
				SectionBean sectionBean = new SectionBean();
				sectionBean.setId(consumerServiceValueBean.getSectionId());
				complaintBean.setSectionBean(sectionBean);
				complaintHistoryBean.setSectionBean(sectionBean);
			}
			
			PublicUserBean publicUserBean = new PublicUserBean();
			publicUserBean.setId(publicUserValueBean.getId());
			complaintBean.setPublicUserBean(publicUserBean);
			complaintBean.setServiceNumber(consumerServiceValueBean.getServiceNumber());
			complaintBean.setLandmark(dataModel.getLandMark());
			complaintBean.setServiceName(consumerServiceValueBean.getServiceName());
			complaintBean.setServiceAddress(consumerServiceValueBean.getServiceAddress());
			complaintBean.setDescription(dataModel.getComplaintDescription());
			complaintBean.setReceivedFrom(dataModel.getReceivedFrom());
			complaintBean.setTag(dataModel.getTag());
			
			String defaultMobile = consumerServiceValueBean.getMobile();
			String alternateMobile = dataModel.getAlternateMobileNo();
			if (alternateMobile != null && alternateMobile.trim().length() > 0) {
				defaultMobile = alternateMobile;
			}
			complaintBean.setAlternateMobileNo(defaultMobile);

			RescueBean rescueBean = new RescueBean();
			rescueBean.setId(1);
			complaintBean.setRescueBean(rescueBean);
			complaintBean.setCategoryBean(categoryBean);
			complaintBean.setSubCategoryBean(subCategoryBean);
			complaintBean.setLatitude("");
			complaintBean.setLongitude("");
			complaintBean.setImage1("");
			complaintBean.setImage2("");
			complaintBean.setCompEntUser(userName);
			complaintBean.setStatusId(CCMSConstants.PENDING);
			if (socialMedia) {
				complaintBean.setDevice("SM");
			} else {
				complaintBean.setDevice("FOC");
			}
			complaintBean.setComplaintCode(Integer.parseInt(dataModel.getComplaintType()));

			
			complaintHistoryBean.setPublicUserBean(publicUserBean);
			complaintHistoryBean.setComplaintBean(complaintBean);
			complaintHistoryBean.setStatusId(complaintBean.getStatusId());
			complaintHistoryBean.setDescription(complaintBean.getDescription());
			complaintBean.addToHistory(complaintHistoryBean);

			transaction = session.beginTransaction();

			session.save(complaintBean);
			
			transaction.commit();
			
			session.refresh(complaintBean);
			
			complaintId = complaintBean.getId();
			
			String message = "Your CmplNo"+complaintBean.getId()+" has been Registered "+GeneralUtil.formatDateWithTime(new Date())+". Save Electricity - TANGEDCO";
			
			String smsId = SMSUtil.sendSMS(null, publicUserValueBean.getMobile(), message);
            System.err.println("THE METER RELATED SERVCIE SMS---------------"+publicUserValueBean.getMobile());
			SmsClient.sendSms(smsId);
			
		} catch (Exception e) {
			if(transaction != null) {
				transaction.rollback();
			}
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
			return complaintId;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return complaintId;
	}
	
	
	public Integer registerMeterAndBillComplaint(SectionValueBean sectionValueBean, DataModel dataModel, PublicUserValueBean publicUserValueBean, boolean socialMedia,String userName) {
		
		Integer complaintId = null;
				
		SessionFactory factory = null;
		Session session = null;
		Transaction transaction = null;
		
		try {
			
			GeneralDao dao = new GeneralDao();
			
			CategoryBean categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_METER_RELATED);
			SubCategoryBean subCategoryBean = dao.getSubCategoryBeanById(dataModel.getSubCategory());
			
			Integer complaintType = Integer.parseInt(dataModel.getComplaintType());
			
			if(complaintType == 3) {
				categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_BILLING);
			}
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			ComplaintBean complaintBean = new ComplaintBean();
			
			ComplaintHistoryBean complaintHistoryBean = new ComplaintHistoryBean();
			
			complaintBean.setComplaintType(CCMSConstants.COMPLAINT_TYPE_CODE[Integer.parseInt(dataModel.getComplaintType())]);
			
			if(sectionValueBean.getRegionId() != null && sectionValueBean.getRegionId() > 0) {
				RegionBean regionBean = new RegionBean();
				regionBean.setId(sectionValueBean.getRegionId());
				complaintBean.setRegionBean(regionBean);
				complaintHistoryBean.setRegionBean(regionBean);
			}
			if(sectionValueBean.getCircleId() != null && sectionValueBean.getCircleId() > 0) {
				CircleBean circleBean = new CircleBean();
				circleBean.setId(sectionValueBean.getCircleId());
				complaintBean.setCircleBean(circleBean);
				complaintHistoryBean.setCircleBean(circleBean);
			}
			if(sectionValueBean.getDivisionId() != null && sectionValueBean.getDivisionId() > 0) {
				DivisionBean divisionBean = new DivisionBean();
				divisionBean.setId(sectionValueBean.getDivisionId());
				complaintBean.setDivisionBean(divisionBean);
				complaintHistoryBean.setDivisionBean(divisionBean);
			}
			if(sectionValueBean.getSubDivisionId() != null && sectionValueBean.getSubDivisionId() > 0) {
				SubDivisionBean subDivisionBean = new SubDivisionBean();
				subDivisionBean.setId(sectionValueBean.getSubDivisionId());
				complaintBean.setSubDivisionBean(subDivisionBean);
				complaintHistoryBean.setSubDivisionBean(subDivisionBean);
			}
			if(sectionValueBean.getId() != null && sectionValueBean.getId() > 0) {
				SectionBean sectionBean = new SectionBean();
				sectionBean.setId(sectionValueBean.getId());
				complaintBean.setSectionBean(sectionBean);
				complaintHistoryBean.setSectionBean(sectionBean);
			}
			
			PublicUserBean publicUserBean = new PublicUserBean();
			publicUserBean.setId(publicUserValueBean.getId());
			complaintBean.setPublicUserBean(publicUserBean);
			complaintBean.setServiceAddress(dataModel.getAddress());
			complaintBean.setLandmark(dataModel.getLandMark());
			complaintBean.setDescription(dataModel.getComplaintDescription());
			
			RescueBean rescueBean = new RescueBean();
			rescueBean.setId(1);
			complaintBean.setRescueBean(rescueBean);
			complaintBean.setCategoryBean(categoryBean);
			complaintBean.setSubCategoryBean(subCategoryBean);
			complaintBean.setLatitude("");
			complaintBean.setLongitude("");
			complaintBean.setImage1("");
			complaintBean.setImage2("");
			complaintBean.setCompEntUser(userName);
			complaintBean.setStatusId(CCMSConstants.PENDING);
			if (socialMedia) {
				complaintBean.setDevice("SM");
			} else {
				complaintBean.setDevice("FOC");
			}
			complaintBean.setComplaintCode(Integer.parseInt(dataModel.getComplaintType()));
			complaintBean.setReceivedFrom(dataModel.getReceivedFrom());
			complaintBean.setTag(dataModel.getTag());
			complaintBean.setAlternateMobileNo(dataModel.getAlternateMobileNo());
			
			complaintHistoryBean.setPublicUserBean(publicUserBean);
			complaintHistoryBean.setComplaintBean(complaintBean);
			complaintHistoryBean.setStatusId(complaintBean.getStatusId());
			complaintHistoryBean.setDescription(complaintBean.getDescription());
			complaintBean.addToHistory(complaintHistoryBean);

			transaction = session.beginTransaction();

			session.save(complaintBean);
			
			transaction.commit();
			
			session.refresh(complaintBean);
			
			complaintId = complaintBean.getId();
			
			System.err.println("THE SMS SEND TO METER RELATED-----------"+publicUserValueBean.getMobile() +"AND USER ID ---------------------"+publicUserValueBean.getId());
			
			String message = "Your CmplNo"+complaintBean.getId()+" has been Registered "+GeneralUtil.formatDateWithTime(new Date())+". Save Electricity - TANGEDCO";
			
			String smsId = SMSUtil.sendSMS(null, publicUserValueBean.getMobile(), message);
			
			SmsClient.sendSms(smsId);
			
		} catch (Exception e) {
			if(transaction != null) {
				transaction.rollback();
			}
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
			return complaintId;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return complaintId;
	}
	
	
	public Integer registerGISComplaint(SectionValueBean sectionValueBean, DataModel dataModel, PublicUserValueBean publicUserValueBean, boolean socialMedia,String userName) {
		
		Integer complaintId = null;
		
		SessionFactory factory = null;
		Session session = null;
		Transaction transaction = null;
		
		try {
			
			GeneralDao dao = new GeneralDao();
			
			CategoryBean categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_FIRE);
			SubCategoryBean subCategoryBean = dao.getSubCategoryBeanById(dataModel.getSubCategory());
			
			Integer complaintType = Integer.parseInt(dataModel.getComplaintType());
			
			if(complaintType == 5) {
				categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_POSTING_DANGER);
				
			} else if(complaintType == 6) {
				categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_THEFT_OF_POWER);
			} else if(complaintType == 8) {
				categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_CONDUCTOR_SNAPPING);
			}

			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			ComplaintBean complaintBean = new ComplaintBean();
			
			ComplaintHistoryBean complaintHistoryBean = new ComplaintHistoryBean();
			
			complaintBean.setComplaintType(CCMSConstants.COMPLAINT_TYPE_CODE[Integer.parseInt(dataModel.getComplaintType())]);
			
			if(sectionValueBean.getRegionId() != null && sectionValueBean.getRegionId() > 0) {
				RegionBean regionBean = new RegionBean();
				regionBean.setId(sectionValueBean.getRegionId());
				complaintBean.setRegionBean(regionBean);
				complaintHistoryBean.setRegionBean(regionBean);
			}
			if(sectionValueBean.getCircleId() != null && sectionValueBean.getCircleId() > 0) {
				CircleBean circleBean = new CircleBean();
				circleBean.setId(sectionValueBean.getCircleId());
				complaintBean.setCircleBean(circleBean);
				complaintHistoryBean.setCircleBean(circleBean);
			}
			if(sectionValueBean.getDivisionId() != null && sectionValueBean.getDivisionId() > 0) {
				DivisionBean divisionBean = new DivisionBean();
				divisionBean.setId(sectionValueBean.getDivisionId());
				complaintBean.setDivisionBean(divisionBean);
				complaintHistoryBean.setDivisionBean(divisionBean);
			}
			if(sectionValueBean.getSubDivisionId() != null && sectionValueBean.getSubDivisionId() > 0) {
				SubDivisionBean subDivisionBean = new SubDivisionBean();
				subDivisionBean.setId(sectionValueBean.getSubDivisionId());
				complaintBean.setSubDivisionBean(subDivisionBean);
				complaintHistoryBean.setSubDivisionBean(subDivisionBean);
			}
			if(sectionValueBean.getId() != null && sectionValueBean.getId() > 0) {
				SectionBean sectionBean = new SectionBean();
				sectionBean.setId(sectionValueBean.getId());
				complaintBean.setSectionBean(sectionBean);
				complaintHistoryBean.setSectionBean(sectionBean);
			}
			
			PublicUserBean publicUserBean = new PublicUserBean();
			publicUserBean.setId(publicUserValueBean.getId());
			
			complaintBean.setPublicUserBean(publicUserBean);
			complaintBean.setServiceAddress(dataModel.getAddress());
			complaintBean.setLandmark(dataModel.getLandMark());
			complaintBean.setDescription(dataModel.getComplaintDescription());
			complaintBean.setReceivedFrom(dataModel.getReceivedFrom());
			complaintBean.setTag(dataModel.getTag());
			
			String defaultMobile = publicUserValueBean.getMobile();
			String alternateMobile = dataModel.getAlternateMobileNo();
			if (alternateMobile != null && alternateMobile.trim().length() > 0) {
				defaultMobile = alternateMobile;
			}
			complaintBean.setAlternateMobileNo(defaultMobile);
			
			RescueBean rescueBean = new RescueBean();
			rescueBean.setId(2);
			complaintBean.setRescueBean(rescueBean);
			complaintBean.setCategoryBean(categoryBean);
			complaintBean.setSubCategoryBean(subCategoryBean);
			complaintBean.setLatitude("");
			complaintBean.setLongitude("");
			complaintBean.setImage1("");
			complaintBean.setImage2("");
			complaintBean.setCompEntUser(userName);
			complaintBean.setStatusId(CCMSConstants.PENDING);
			if (socialMedia) {
				complaintBean.setDevice("SM");
			} else {
				complaintBean.setDevice("FOC");
			}
			complaintBean.setComplaintCode(Integer.parseInt(dataModel.getComplaintType()));

			
			complaintHistoryBean.setPublicUserBean(publicUserBean);
			complaintHistoryBean.setComplaintBean(complaintBean);
			complaintHistoryBean.setStatusId(complaintBean.getStatusId());
			complaintHistoryBean.setDescription(complaintBean.getDescription());
			complaintBean.addToHistory(complaintHistoryBean);

			transaction = session.beginTransaction();

			session.save(complaintBean);
			
			transaction.commit();
			
			session.refresh(complaintBean);
			
			complaintId = complaintBean.getId();

				System.err.println("THE GENERAL COMPLAINT SMS SENT TO ---------------"+publicUserValueBean.getMobile());
				
				String message = "Your CmplNo"+complaintBean.getId()+" has been Registered "+GeneralUtil.formatDateWithTime(new Date())+". Save Electricity - TANGEDCO";
				
				String smsId = SMSUtil.sendSMS(null, publicUserValueBean.getMobile(), message);
				
				SmsClient.sendSms(smsId);
		
			
		} catch (Exception e) {
			if(transaction != null) {
				transaction.rollback();
			}
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
			return complaintId;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return complaintId;
	}
	
	public List<ReportValueBean> getCircleReport(String[] circle, Date fromDate, Date toDate, LoginParams office) throws Exception {
		
		List<ReportValueBean> circleReportList = new ArrayList<ReportValueBean>();
		ReportValueBean reportValueBean = null;
		Map<String, ReportValueBean> reportMap = new TreeMap<String, ReportValueBean>();
		Integer serialNumber = 1;
		Integer totalCompRegistered = 1;
		Integer totalCompRegisteredOneHour = 1;
		Integer totalCompRegisteredThreeHour = 1;
		Integer totalCompRegisteredSixHour = 1;
		Integer totalCompRegisteredTwelveHour = 1;
		Integer totalCompRegisteredOneDay = 1;
		Integer compPending  = 1;
		Integer compPendingCurrentDay = 1;
		Long timeInHours = 0L;
		Date createdOn;
		Date updatedOn;
		String circleName = null;
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			List<Integer> circleList = new ArrayList<Integer>();
			
			for (String circleId : circle) {
				circleList.add(Integer.parseInt(circleId));
			}
			
			if (fromDate != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(fromDate);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				fromDate = cal.getTime();
			}
			
			if (toDate != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(toDate);
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				toDate = cal.getTime();
			}
			
			String whereCondition = " device = 'admin' ";
			if (office != null && office.roleId != null && office.roleId == 3) {
				whereCondition = "device = 'SM'";
			}
			
			if(circleList.size() > 0) {
				whereCondition += " and comp.circleBean.id" + " in ("+ loadIntegerCondition(circleList) +") ";
			}
			
			if (fromDate != null && toDate == null) {
				if (StringUtils.isNotBlank(whereCondition)) {
					whereCondition += " and ";
				}
				whereCondition += " TRUNC(comp.createdOn) = TO_DATE('" + GeneralUtil.formatDateForHql(fromDate)	+ "', 'YYYY-MM-DD')";
			} else if (fromDate != null && toDate != null) {
				if (StringUtils.isNotBlank(whereCondition)) {
					whereCondition += " and ";
				}
				whereCondition += " TRUNC(comp.createdOn) >= TO_DATE('" + GeneralUtil.formatDateForHql(fromDate)
						+ "', 'YYYY-MM-DD') and TRUNC(comp.createdOn) <= TO_DATE('" + GeneralUtil.formatDateForHql(toDate)	+ "', 'YYYY-MM-DD')";
			} else if (fromDate == null && toDate != null) {
				if (StringUtils.isNotBlank(whereCondition)) {
					whereCondition += " and ";
				}
				whereCondition += " TRUNC(comp.createdOn) = TO_DATE('" + GeneralUtil.formatDateForHql(toDate) + "', 'YYYY-MM-DD')";
			}
			
			String hql = "SELECT comp.id, comp.statusId, comp.createdOn, comp.updatedOn, circle.name FROM  ComplaintBean as comp, CircleBean as circle where "
					+ " circle.id = comp.circleBean.id and " + whereCondition  +	" ORDER BY comp.circleBean.id";
			
			Query<?> query = session.createQuery(hql);

			List<?> rows = query.list();

			if (rows.size() > 0) {
				Object[] row = null;
				for (int i = 0; i < rows.size(); i++) {
					row = (Object[]) rows.get(i);

					circleName = row[4].toString();
					if(reportMap.containsKey(circleName)){
						reportValueBean = reportMap.get(circleName);
					} else {
						reportValueBean = new ReportValueBean();
						totalCompRegistered = 1;
						totalCompRegisteredOneHour = 1;
						totalCompRegisteredThreeHour = 1;
						totalCompRegisteredSixHour = 1;
						totalCompRegisteredTwelveHour = 1;
						totalCompRegisteredOneDay = 1;
						compPending  = 1;
						compPendingCurrentDay = 1;
					}
					reportValueBean.setCircleName(circleName);
					reportValueBean.setTotalCompRegistered(totalCompRegistered++);
					
					createdOn = GeneralUtil.parseDateWithTime(row[2].toString());
					updatedOn = GeneralUtil.parseDateWithTime(row[3].toString());
					
					Integer statusValue = Integer.parseInt(row[1].toString());
					
					if(statusValue.intValue() == CCMSConstants.COMPLETED) {
						timeInHours = GeneralUtil.findDifference(updatedOn, createdOn, "hour");
						if(timeInHours <= 1) {
							reportValueBean.setTotalCompRegisteredOneHour(totalCompRegisteredOneHour++);
						} else if(timeInHours > 1 && timeInHours <= 3) {
							reportValueBean.setTotalCompRegisteredThreeHour(totalCompRegisteredThreeHour++);
						} else if(timeInHours > 3 && timeInHours <= 6) {
							reportValueBean.setTotalCompRegisteredSixHour(totalCompRegisteredSixHour++);
						} else if(timeInHours > 6 && timeInHours <= 12) {
							reportValueBean.setTotalCompRegisteredTwelveHour(totalCompRegisteredTwelveHour++);
						} else if(timeInHours > 12 && timeInHours <= 24) {
							reportValueBean.setTotalCompRegisteredOneDay(totalCompRegisteredOneDay++);
						} else {
							reportValueBean.setCompPendingCurrentDay(compPendingCurrentDay++);
						}
					} else {
						reportValueBean.setCompPending(compPending++);
					}
		            
					reportMap.put(circleName, reportValueBean);
					
				}
			}

			
			for (Map.Entry<String, ReportValueBean> mapEntry : reportMap.entrySet()) {
				reportValueBean = mapEntry.getValue();
				reportValueBean.setSerialNumber(serialNumber++);
				circleReportList.add(reportValueBean);
			}
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return circleReportList;		
	}
	
	
	public Map<String, List<ReportValueBean>> getCircleDivisionReport(String[] circle, Date fromDate, Date toDate, LoginParams office) throws Exception {
		
		Map<String, List<ReportValueBean>> circleDivisionReportMap = new TreeMap<String, List<ReportValueBean>>();
		List<ReportValueBean> districtReportList = new ArrayList<ReportValueBean>();
		ReportValueBean reportValueBean = null;
		Map<String, ReportValueBean> reportMap = new TreeMap<String, ReportValueBean>();
		Map<String, String> circleMap = new TreeMap<String, String>();
		Integer totalCompRegistered = 1;
		Integer compPending  = 1;
		Integer compResolved = 1;
		String divisionName = null;
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			List<Integer> circleList = new ArrayList<Integer>();
			
			for (String circleId : circle) {
				circleList.add(Integer.parseInt(circleId));
			}

			if (fromDate != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(fromDate);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				fromDate = cal.getTime();
			}
			
			if (toDate != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(toDate);
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				toDate = cal.getTime();
			}
			
			String whereCondition = " device = 'admin' ";
			if (office != null && office.roleId != null && office.roleId == 3) {
				whereCondition = "device = 'SM'";
			}
			
			if(circleList.size() > 0) {
				whereCondition += " and comp.circleBean.id" + " in ("+ loadIntegerCondition(circleList) +") ";
			}
			
			if (fromDate != null && toDate == null) {
				if (StringUtils.isNotBlank(whereCondition)) {
					whereCondition += " and ";
				}
				whereCondition += " TRUNC(comp.createdOn) = TO_DATE('" + GeneralUtil.formatDateForHql(fromDate)	+ "', 'YYYY-MM-DD')";
			} else if (fromDate != null && toDate != null) {
				if (StringUtils.isNotBlank(whereCondition)) {
					whereCondition += " and ";
				}
				whereCondition += " TRUNC(comp.createdOn) >= TO_DATE('" + GeneralUtil.formatDateForHql(fromDate)
						+ "', 'YYYY-MM-DD') and TRUNC(comp.createdOn) <= TO_DATE('" + GeneralUtil.formatDateForHql(toDate)	+ "', 'YYYY-MM-DD')";
			} else if (fromDate == null && toDate != null) {
				if (StringUtils.isNotBlank(whereCondition)) {
					whereCondition += " and ";
				}
				whereCondition += " TRUNC(comp.createdOn) = TO_DATE('" + GeneralUtil.formatDateForHql(toDate) + "', 'YYYY-MM-DD')";
			}
			
			String hql = "SELECT comp.id, comp.statusId, comp.createdOn, comp.updatedOn, circle.name, division.name FROM  ComplaintBean as comp, CircleBean as circle, DivisionBean as division"
					+ " where circle.id = comp.circleBean.id and division.id = comp.divisionBean.id and " + whereCondition  +	" ORDER BY division.id";
			
			Query<?> query = session.createQuery(hql);
			List<?> rows = query.list();
			if (rows.size() > 0) {
				Object[] row = null;
				for (int i = 0; i < rows.size(); i++) {
					row = (Object[]) rows.get(i);

					circleMap.put(row[5].toString(), row[4].toString());
					divisionName = row[5].toString();
					if(reportMap.containsKey(divisionName)){
						reportValueBean = reportMap.get(divisionName);
					} else {
						reportValueBean = new ReportValueBean();
						totalCompRegistered = 1;
						compPending  = 1;
						compResolved = 1;
					}
					reportValueBean.setDivisionName(divisionName);
					reportValueBean.setTotalCompRegistered(totalCompRegistered++);
					
					Integer statusValue = Integer.parseInt(row[1].toString());
					
					if(statusValue.intValue() == CCMSConstants.COMPLETED) {
						reportValueBean.setCompResolved(compResolved++);
					} else {
						reportValueBean.setCompPending(compPending++);
					}
		            
					reportMap.put(divisionName, reportValueBean);
					
				}
			}

			
			for (Map.Entry<String, ReportValueBean> mapEntry : reportMap.entrySet()) {
				String circleName = circleMap.get(mapEntry.getKey());
				if(circleDivisionReportMap.containsKey(circleName)) {
					districtReportList = circleDivisionReportMap.get(circleName);
				} else {
					districtReportList = new ArrayList<ReportValueBean>();
				}
				reportValueBean = mapEntry.getValue();
				districtReportList.add(reportValueBean);
				circleDivisionReportMap.put(circleName, districtReportList);
			}
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		
		return circleDivisionReportMap;		
	}
	
	
	
	// FOR SOCIAL MEDIA REPORT PAGE
	
	
	public List<ViewComplaintReportValueBean> searchComplaintSm(DataModel dataModel, int complaintCodeValue, int statusValue) throws Exception {
		
		List<ViewComplaintReportValueBean> viewComplaintList = new ArrayList<ViewComplaintReportValueBean>();
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			List<String> compliantCodeList = new ArrayList<String>();
			List<String> statusList = new ArrayList<String>();
			List<Integer> regionList = new ArrayList<Integer>();
			List<Integer> circleList = new ArrayList<Integer>();
			List<Integer> divisionList = new ArrayList<Integer>();
			List<Integer> subDivisionList = new ArrayList<Integer>();
			List<Integer> sectionList = new ArrayList<Integer>();
			List<String> tagList = new ArrayList<String>();
			List<String> receivedFromList = new ArrayList<String>();
			
			if (dataModel.getCompType().length > 0) {
				for (String code : dataModel.getCompType()) {
					compliantCodeList.add(code);
				}
			}
			
			if (dataModel.getStatuses().length > 0) {
				for (String status : dataModel.getStatuses()) {
					statusList.add(status);
				}
			}
			
			if (dataModel.getRegionFilter() != null && dataModel.getRegionFilter().length > 0) {
				for (String region : dataModel.getRegionFilter()) {
					regionList.add(Integer.parseInt(region));
				}
			}
			if (dataModel.getCircleFilter() != null && dataModel.getCircleFilter().length > 0) {
				for (String circle : dataModel.getCircleFilter()) {
					circleList.add(Integer.parseInt(circle));
				}
			}
			if (dataModel.getDivisionFilter() != null && dataModel.getDivisionFilter().length > 0) {
				for (String division : dataModel.getDivisionFilter()) {
					divisionList.add(Integer.parseInt(division));
				}
			}
			if (dataModel.getSubDivisionFilter() != null && dataModel.getSubDivisionFilter().length > 0) {
				for (String subDivision : dataModel.getSubDivisionFilter()) {
					subDivisionList.add(Integer.parseInt(subDivision));
				}
			}
			if (dataModel.getSectionFilter() != null && dataModel.getSectionFilter().length > 0) {
				for (String section : dataModel.getSectionFilter()) {
					sectionList.add(Integer.parseInt(section));
				}
			}
			
			if (dataModel.getTagArray() != null && dataModel.getTagArray().length > 0) {
				for (String tag : dataModel.getTagArray()) {
					tagList.add(tag);
				}
			}
			
			if (dataModel.getReceivedFromArray() != null && dataModel.getReceivedFromArray().length > 0) {
				for (String receivedFrom : dataModel.getReceivedFromArray()) {
					receivedFromList.add(receivedFrom);
				}
			}
			
			Date fromDate = dataModel.getFromDate();
			Date toDate = dataModel.getToDate();
			
			if (fromDate != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(fromDate);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				fromDate = cal.getTime();
			}
			
			if (toDate != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(toDate);
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				toDate = cal.getTime();
			}
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<ViewComplaintReportBean> criteriaQuery = criteriaBuilder.createQuery(ViewComplaintReportBean.class);
			Root<ViewComplaintReportBean> root = criteriaQuery.from(ViewComplaintReportBean.class);
			
			
			//Constructing list of parameters
			List<Predicate> predicates = new ArrayList<Predicate>();
			
			if(complaintCodeValue == -1 && statusValue == -1) {
				if(compliantCodeList.size() > 0) {
					Expression<String> complaintExpression = root.get("complaintCode");
					Predicate compliantCodePredicate = complaintExpression.in(compliantCodeList);
					predicates.add(compliantCodePredicate);
				}
				if(statusList.size() > 0) {
					Expression<String> statusExpression = root.get("statusId");
					Predicate statusPredicate = statusExpression.in(statusList);
					predicates.add(statusPredicate);
				}
			} else {
				predicates.add(criteriaBuilder.equal(root.get("complaintCode"), complaintCodeValue));
				if (statusValue >= 0) {
					predicates.add(criteriaBuilder.equal(root.get("statusId"), statusValue));
				} else {
					predicates.add(criteriaBuilder.lessThan(root.get("statusId"), 2));
				}
			}
			
			if(regionList.size() > 0) {
				Expression<String> regionExpression = root.get("regionId");
				Predicate regionPredicate = regionExpression.in(regionList);
				predicates.add(regionPredicate);
			}
			if(circleList.size() > 0) {
				Expression<String> circleExpression = root.get("circleId");
				Predicate circlePredicate = circleExpression.in(circleList);
				predicates.add(circlePredicate);
			}
			if(divisionList.size() > 0) {
				Expression<String> divisionExpression = root.get("divisionId");
				Predicate divisionPredicate = divisionExpression.in(divisionList);
				predicates.add(divisionPredicate);
			}
			if(subDivisionList.size() > 0) {
				Expression<String> subDivisionExpression = root.get("subDivisionId");
				Predicate subDivisionPredicate = subDivisionExpression.in(subDivisionList);
				predicates.add(subDivisionPredicate);
			}
			if(sectionList.size() > 0) {
				Expression<String> sectionExpression = root.get("sectionId");
				Predicate sectionPredicate = sectionExpression.in(sectionList);
				predicates.add(sectionPredicate);
			}
			if(tagList.size() > 0) {
				Expression<String> tagExpression = root.get("tag");
				Predicate tagPredicate = tagExpression.in(tagList);
				predicates.add(tagPredicate);
			}
			if(receivedFromList.size() > 0) {
				Expression<String> receivedFromExpression = root.get("receivedFrom");
				Predicate receivedFromPredicate = receivedFromExpression.in(receivedFromList);
				predicates.add(receivedFromPredicate);
			}
			
			if(fromDate != null && toDate == null) {
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.<Date>get("createdOn"), fromDate));
			} else if(fromDate != null && toDate != null) {
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.<Date>get("createdOn"), fromDate));
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.<Date>get("createdOn"), toDate));
			} else if(fromDate == null && toDate != null) {
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.<Date>get("createdOn"), toDate));
			}
			
			predicates.add(criteriaBuilder.equal(root.get("device"), "SM"));
			
			criteriaQuery.select(root).where(predicates.toArray(new Predicate[]{}));

			Query<ViewComplaintReportBean> query = session.createQuery(criteriaQuery).setMaxResults(5000);
			
			List<ViewComplaintReportBean> viewComplaintReportBeanList = query.getResultList();

			viewComplaintList = convertBeanListToValueBeanList(viewComplaintReportBeanList);
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return viewComplaintList;
	}
	
	
	public int[][] getReportDashBoardSm(DataModel dataModel) {
		DataModel dm = new DataModel();
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			List<String> compliantCodeList = new ArrayList<String>();
			List<String> statusList = new ArrayList<String>();
			List<Integer> regionList = new ArrayList<Integer>();
			List<Integer> circleList = new ArrayList<Integer>();
			List<Integer> divisionList = new ArrayList<Integer>();
			List<Integer> subDivisionList = new ArrayList<Integer>();
			List<Integer> sectionList = new ArrayList<Integer>();
			List<String> tagList = new ArrayList<>();
			List<String> receivedFromList = new ArrayList<>();
			
			if (dataModel.getCompType().length > 0) {
				for (String code : dataModel.getCompType()) {
					compliantCodeList.add(code);
				}
			}
			
			if (dataModel.getStatuses().length > 0) {
				for (String status : dataModel.getStatuses()) {
					statusList.add(status);
				}
			}
			
			if (dataModel.getRegionFilter() != null && dataModel.getRegionFilter().length > 0) {
				for (String region : dataModel.getRegionFilter()) {
					regionList.add(Integer.parseInt(region));
				}
			}
			if (dataModel.getCircleFilter() != null && dataModel.getCircleFilter().length > 0) {
				for (String circle : dataModel.getCircleFilter()) {
					circleList.add(Integer.parseInt(circle));
				}
			}
			if (dataModel.getDivisionFilter() != null && dataModel.getDivisionFilter().length > 0) {
				for (String division : dataModel.getDivisionFilter()) {
					divisionList.add(Integer.parseInt(division));
				}
			}
			if (dataModel.getSubDivisionFilter() != null && dataModel.getSubDivisionFilter().length > 0) {
				for (String subDivision : dataModel.getSubDivisionFilter()) {
					subDivisionList.add(Integer.parseInt(subDivision));
				}
			}
			if (dataModel.getSectionFilter() != null && dataModel.getSectionFilter().length > 0) {
				for (String section : dataModel.getSectionFilter()) {
					sectionList.add(Integer.parseInt(section));
				}
			}
			String[] tagArray = dataModel.getTagArray();
			if ( tagArray != null && tagArray.length > 0) {
				for (String tag : tagArray) {
					tagList.add(tag);
				}
			}
			String[] receivedFromArray = dataModel.getReceivedFromArray();
			if ( receivedFromArray != null && receivedFromArray.length > 0) {
				for (String receivedFrom: receivedFromArray) {
					receivedFromList.add(receivedFrom);
				}
			}
			
			Date fromDate = dataModel.getFromDate();
			Date toDate = dataModel.getToDate();
			
			if (fromDate != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(fromDate);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				fromDate = cal.getTime();
			}
			
			if (toDate != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(toDate);
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				toDate = cal.getTime();
			}
			
			String whereCondition = " ";
			
			if(compliantCodeList.size() > 0) {
				whereCondition = loadWhereCondition(whereCondition, "complaintCode", compliantCodeList);
			}
			if(statusList.size() > 0) {
				whereCondition = loadWhereCondition(whereCondition, "statusId", statusList);
			}
			if(regionList.size() > 0) {
				whereCondition = loadWhereNumberCondition(whereCondition, "regionId", regionList);
			}
			if(circleList.size() > 0) {
				whereCondition = loadWhereNumberCondition(whereCondition, "circleId", circleList);
			}
			if(divisionList.size() > 0) {
				whereCondition = loadWhereNumberCondition(whereCondition, "divisionId", divisionList);
			}
			if(subDivisionList.size() > 0) {
				whereCondition = loadWhereNumberCondition(whereCondition, "subDivisionId", subDivisionList);
			}
			if(sectionList.size() > 0) {
				whereCondition = loadWhereNumberCondition(whereCondition, "sectionId", sectionList);
			}
			if(tagList.size() > 0) {
				whereCondition = loadWhereCondition(whereCondition, "tag", tagList);
			}
			if(receivedFromList.size() > 0) {
				whereCondition = loadWhereCondition(whereCondition, "receivedFrom", receivedFromList);
			}
			
			  if (StringUtils.isNotBlank(whereCondition)) {
		            whereCondition += " and ";
		        }
		        whereCondition += " device = 'SM'";
			
			if (fromDate != null && toDate == null) {
				if (StringUtils.isNotBlank(whereCondition)) {
					whereCondition += " and ";
				}
				
				whereCondition += " TRUNC(createdOn) >= TO_DATE('" + GeneralUtil.formatDateForHql(fromDate)	+ "', 'YYYY-MM-DD')";
			} else if (fromDate != null && toDate != null) {
				if (StringUtils.isNotBlank(whereCondition)) {
					whereCondition += " and ";
				}
				whereCondition += " TRUNC(createdOn) >= TO_DATE('" + GeneralUtil.formatDateForHql(fromDate)
						+ "', 'YYYY-MM-DD') and  TRUNC(createdOn) <= TO_DATE('" + GeneralUtil.formatDateForHql(toDate)	+ "', 'YYYY-MM-DD')";
			} else if (fromDate == null && toDate != null) {
				if (StringUtils.isNotBlank(whereCondition)) {
					whereCondition += " and ";
				}
				whereCondition += " TRUNC(createdOn) <= TO_DATE('" + GeneralUtil.formatDateForHql(toDate) + "', 'YYYY-MM-DD')";
			}
			
			String hql = "SELECT complaintCode, statusId, count(*) as cnt  FROM  ViewComplaintBean WHERE " + whereCondition  +
					" GROUP BY " + " complaintCode, statusId ORDER BY complaintCode, statusId";
			
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
					dm.arrReport[ct][status] = Integer.valueOf(row[2].toString());
				}

			} else {
				dm.arrReport[0][0] = -1;
			}
			
		} catch (Exception e) {
			dm.arrReport[0][0] = -1;
			logger.error(ExceptionUtils.getStackTrace(e));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

		return dm.arrReport;
	}
	
	public List<ReportValueBean> getCircleReportSm(String[] circle, Date fromDate, Date toDate, LoginParams office) throws Exception {
	    
	    List<ReportValueBean> circleReportList = new ArrayList<>();
	    Map<String, ReportValueBean> reportMap = new TreeMap<>();
	    Integer serialNumber = 1;

	    SessionFactory factory = null;
	    Session session = null;
	    try {
	        
	        factory = HibernateUtil.getSessionFactory();
	        session = factory.openSession();
	        
	        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
	        CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
	        
	        Root<CircleBean> root = criteriaQuery.from(CircleBean.class);
	        
	        criteriaQuery.select(root.get("name"));
	             
	        Query<String> allCirclesQuery = session.createQuery(criteriaQuery);
	        List<String> allCircles = allCirclesQuery.getResultList();
	        
	        for (String circleN : allCircles) {
	            ReportValueBean reportValueBean = new ReportValueBean();
	            reportValueBean.setCircleName(circleN);
	            reportValueBean.setTotalCompRegistered(0);
	            reportValueBean.setTotalCompRegisteredOneHour(0);
	            reportValueBean.setTotalCompRegisteredThreeHour(0);
	            reportValueBean.setTotalCompRegisteredSixHour(0);
	            reportValueBean.setTotalCompRegisteredTwelveHour(0);
	            reportValueBean.setTotalCompRegisteredOneDay(0);
	            reportValueBean.setCompPending(0);
	            reportValueBean.setCompPendingCurrentDay(0);
	            reportMap.put(circleN, reportValueBean);
	        }
	        
	        if (fromDate != null) {
	            Calendar cal = Calendar.getInstance();
	            cal.setTime(fromDate);
	            cal.set(Calendar.HOUR_OF_DAY, 0);
	            cal.set(Calendar.MINUTE, 0);
	            cal.set(Calendar.SECOND, 0);
	            fromDate = cal.getTime();
	        }
	        
	        if (toDate != null) {
	            Calendar cal = Calendar.getInstance();
	            cal.setTime(toDate);
	            cal.set(Calendar.HOUR_OF_DAY, 23);
	            cal.set(Calendar.MINUTE, 59);
	            cal.set(Calendar.SECOND, 59);
	            toDate = cal.getTime();
	        }
	        
	        
	        String whereCondition = "device = 'SM'";
	        
	        if (fromDate != null && toDate == null) {
	            whereCondition += " and TRUNC(comp.createdOn) = TO_DATE('" + GeneralUtil.formatDateForHql(fromDate) + "', 'YYYY-MM-DD')";
	        } else if (fromDate != null && toDate != null) {
	            whereCondition += " and TRUNC(comp.createdOn) >= TO_DATE('" + GeneralUtil.formatDateForHql(fromDate) + "', 'YYYY-MM-DD') " +
	                              "and TRUNC(comp.createdOn) <= TO_DATE('" + GeneralUtil.formatDateForHql(toDate) + "', 'YYYY-MM-DD')";
	        } else if (fromDate == null && toDate != null) {
	            whereCondition += " and TRUNC(comp.createdOn) = TO_DATE('" + GeneralUtil.formatDateForHql(toDate) + "', 'YYYY-MM-DD')";
	        }
	        
	        
	        String hql = "SELECT comp.id, comp.statusId, comp.createdOn, comp.updatedOn, circle.name " +
	                     "FROM ComplaintBean comp, CircleBean circle " +
	                     "WHERE circle.id = comp.circleBean.id and " + whereCondition + " ORDER BY comp.circleBean.id";
	        
	        Query<?> query = session.createQuery(hql);
	        List<?> rows = query.list();
	        
	        
	        if (!rows.isEmpty()) {
	            for (Object rowObj : rows) {
	                Object[] row = (Object[]) rowObj;
	                String circleName = row[4].toString();
	                
	                ReportValueBean reportValueBean = reportMap.get(circleName);
	                
	                reportValueBean.setTotalCompRegistered(reportValueBean.getTotalCompRegistered() + 1);
	                
	                Date createdOn = GeneralUtil.parseDateWithTime(row[2].toString());
	                Date updatedOn = GeneralUtil.parseDateWithTime(row[3].toString());
	                
	                Long timeInHours = GeneralUtil.findDifference(updatedOn, createdOn, "hour");
	                Integer statusValue = Integer.parseInt(row[1].toString());
	                
	                if (statusValue == CCMSConstants.COMPLETED) {
	                    if (timeInHours <= 1) {
	                        reportValueBean.setTotalCompRegisteredOneHour(reportValueBean.getTotalCompRegisteredOneHour() + 1);
	                    } else if (timeInHours > 1 && timeInHours <= 3) {
	                        reportValueBean.setTotalCompRegisteredThreeHour(reportValueBean.getTotalCompRegisteredThreeHour() + 1);
	                    } else if (timeInHours > 3 && timeInHours <= 6) {
	                        reportValueBean.setTotalCompRegisteredSixHour(reportValueBean.getTotalCompRegisteredSixHour() + 1);
	                    } else if (timeInHours > 6 && timeInHours <= 12) {
	                        reportValueBean.setTotalCompRegisteredTwelveHour(reportValueBean.getTotalCompRegisteredTwelveHour() + 1);
	                    } else if (timeInHours > 12 && timeInHours <= 24) {
	                        reportValueBean.setTotalCompRegisteredOneDay(reportValueBean.getTotalCompRegisteredOneDay() + 1);
	                    } else {
	                        reportValueBean.setCompPendingCurrentDay(reportValueBean.getCompPendingCurrentDay() + 1);
	                    }
	                } else {
	                    reportValueBean.setCompPending(reportValueBean.getCompPending() + 1);
	                }
	            }
	        }
	        
	        
	        for (Map.Entry<String, ReportValueBean> mapEntry : reportMap.entrySet()) {
	            ReportValueBean reportValueBean = mapEntry.getValue();
	            reportValueBean.setSerialNumber(serialNumber++);
	            circleReportList.add(reportValueBean);
	        }
	        
	    } catch (Exception e) {
	        logger.error(ExceptionUtils.getStackTrace(e));
	        throw e;
	    } finally {
	        HibernateUtil.closeSession(factory, session);
	    }
	    
	    return circleReportList;
	}
	
	
	public Map<String, List<ReportValueBean>> getCircleDivisionReportSm(String[] circle, Date fromDate, Date toDate, LoginParams office) throws Exception {
		
		Map<String, List<ReportValueBean>> circleDivisionReportMap = new TreeMap<String, List<ReportValueBean>>();
		
		List<ReportValueBean> districtReportList;
		Map<String, ReportValueBean> reportMap = new TreeMap<String, ReportValueBean>();
		Map<String, String> circleMap = new TreeMap<String, String>();
	
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			if (fromDate != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(fromDate);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				fromDate = cal.getTime();
			}
			
			if (toDate != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(toDate);
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				toDate = cal.getTime();
			}
			
			String whereCondition = "device = 'SM'";
		
			
			if (fromDate != null && toDate == null) {
				if (StringUtils.isNotBlank(whereCondition)) {
					whereCondition += " and ";
				}
				whereCondition += " TRUNC(comp.createdOn) = TO_DATE('" + GeneralUtil.formatDateForHql(fromDate)	+ "', 'YYYY-MM-DD')";
			} else if (fromDate != null && toDate != null) {
				if (StringUtils.isNotBlank(whereCondition)) {
					whereCondition += " and ";
				}
				whereCondition += " TRUNC(comp.createdOn) >= TO_DATE('" + GeneralUtil.formatDateForHql(fromDate)
						+ "', 'YYYY-MM-DD') and TRUNC(comp.createdOn) <= TO_DATE('" + GeneralUtil.formatDateForHql(toDate)	+ "', 'YYYY-MM-DD')";
			} else if (fromDate == null && toDate != null) {
				if (StringUtils.isNotBlank(whereCondition)) {
					whereCondition += " and ";
				}
				whereCondition += " TRUNC(comp.createdOn) = TO_DATE('" + GeneralUtil.formatDateForHql(toDate) + "', 'YYYY-MM-DD')";
			}
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<Object[]> criteriaQuery = criteriaBuilder.createQuery(Object[].class);
			
			Root<CircleBean> circleRoot = criteriaQuery.from(CircleBean.class);
			Root<DivisionBean> divisionRoot = criteriaQuery.from(DivisionBean.class);
			
			criteriaQuery.multiselect(circleRoot.get("name"), divisionRoot.get("name"));
			
			criteriaQuery.where(criteriaBuilder.equal(circleRoot.get("id"), divisionRoot.get("circleBean").get("id")));
			
			criteriaQuery.orderBy(criteriaBuilder.asc(circleRoot.get("name")));
			
			
			Query<Object[]> circleQuery = session.createQuery(criteriaQuery);
	        List<Object[]> circleRows = circleQuery.getResultList();

	        for (Object rowObj : circleRows) {
	            Object[] row = (Object[]) rowObj;
	            String circleName = row[0].toString();
	            String divisionName = row[1].toString();

	            circleMap.put(divisionName, circleName);

	            ReportValueBean reportValueBean = new ReportValueBean();
	            reportValueBean.setDivisionName(divisionName);
	            reportValueBean.setTotalCompRegistered(0);
	            reportValueBean.setCompPending(0);
	            reportValueBean.setCompResolved(0);

	            reportMap.put(divisionName, reportValueBean);
	        }
			
			String hql = "SELECT comp.id, comp.statusId, comp.createdOn, comp.updatedOn, circle.name, division.name FROM  ComplaintBean as comp, CircleBean as circle, DivisionBean as division"
					+ " where circle.id = comp.circleBean.id and division.id = comp.divisionBean.id and " + whereCondition  +	" ORDER BY division.id";
			
			Query<?> query = session.createQuery(hql);
			List<?> rows = query.list();
			
			 for (Object rowObj : rows) {
		            Object[] row = (Object[]) rowObj;
		            
		            String circleName = row[4].toString();
		            String divisionName = row[5].toString();
		           
		            ReportValueBean reportValueBean = reportMap.get(divisionName);
		            if (reportValueBean != null) {
		                reportValueBean.setTotalCompRegistered(reportValueBean.getTotalCompRegistered() + 1);

		                Integer statusValue = Integer.parseInt(row[1].toString());

		                if (statusValue == CCMSConstants.COMPLETED) {
		                    reportValueBean.setCompResolved(reportValueBean.getCompResolved() + 1);
		                } else {
		                    reportValueBean.setCompPending(reportValueBean.getCompPending() + 1);
		                }
		            }
		        }

			
			 for (Map.Entry<String, ReportValueBean> mapEntry : reportMap.entrySet()) {
		            String circleName = circleMap.get(mapEntry.getKey());
		            if (circleDivisionReportMap.containsKey(circleName)) {
		                districtReportList = circleDivisionReportMap.get(circleName);
		            } else {
		                districtReportList = new ArrayList<>();
		            }
		           ReportValueBean reportValueBean = mapEntry.getValue();
		            districtReportList.add(reportValueBean);
		            circleDivisionReportMap.put(circleName, districtReportList);
		        }
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		
		return circleDivisionReportMap;		
	}
	
	public List<String[]> getDistrictSection (String consumerNo) {
		
		SessionFactory factory = null;
		Session session = null;
		List<DistrictValueBean> listDistrict 		= new ArrayList<>();
		List<DistrictValueBean> listDistrictSection = new ArrayList<>();
		
		List<String[]> resultList = new ArrayList<String[]>();
		
		//Map<String, List<DistrictValueBean>> resultMap = new HashMap<>();
	    
		try {

			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<Object[]> criteriaQuery = criteriaBuilder.createQuery(Object[].class);
			Root<DistrictBean> root = criteriaQuery.from(DistrictBean.class);
				
				
			List<Predicate> predicates = new ArrayList<>();
				
			String codeValue = consumerNo.substring(2,5);
			Predicate codePredicate = criteriaBuilder.equal(root.get("sectionCode"), codeValue);
			predicates.add(codePredicate);
				
			String regionCode = consumerNo.substring(1,2);
			Predicate regionId = criteriaBuilder.equal(root.get("regionBean").get("id"), regionCode);
			predicates.add(regionId);
			
			
			
			criteriaQuery.select(criteriaBuilder.array(root.get("sectionName"), root.get("name")))
            .where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));

			
			Query<Object[]> query = session.createQuery(criteriaQuery);
			List<Object[]> results = query.getResultList();
			String sectionName = null;
			String districtName = null;
		
			
			for (Object[] result : results) {
				String[] row=new String[2];
			    row[0] = (String) result[0];
			   row[1] = (String) result[1];
			   
			   resultList.add(row);
	            
			}
			
//			DistrictValueBean districtValueBean = new DistrictValueBean();
//		    
//			   districtValueBean.setName(districtName);
//			   listDistrict.add(districtValueBean);
//			   
//			   DistrictValueBean section = new DistrictValueBean();
//			   
//			   section.setSectionName(sectionName);
//			   listDistrictSection.add(section);
//	            
//			
//			 dmFilter.setSmListDistrict(listDistrict);
//			 dmFilter.setSmListDistrictSection(listDistrictSection);
//			
//			 System.out.println("Section Name : "+dmFilter.getSmListDistrict());
//			 System.out.println("District Name : "+dmFilter.getSmListDistrictSection());
			
		
				
	}catch (Exception e) {
		logger.error(ExceptionUtils.getStackTrace(e));
		throw e;
	} finally {
		HibernateUtil.closeSession(factory, session);
	}

	    
	    return resultList ;
	
	
	}
}

