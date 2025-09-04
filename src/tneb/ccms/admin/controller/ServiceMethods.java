package tneb.ccms.admin.controller;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tneb.ccms.admin.dao.ComplaintsDao;
import tneb.ccms.admin.model.AdminUserBean;
import tneb.ccms.admin.model.CircleBean;
import tneb.ccms.admin.model.ComplaintBean;
import tneb.ccms.admin.model.ComplaintHistoryBean;
import tneb.ccms.admin.model.DivisionBean;
import tneb.ccms.admin.model.LoginParams;
import tneb.ccms.admin.model.RegionBean;
import tneb.ccms.admin.model.SectionBean;
import tneb.ccms.admin.model.SubDivisionBean;
import tneb.ccms.admin.util.CCMSConstants;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.DataModel.ComplaintLog;
import tneb.ccms.admin.util.GeneralUtil;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.util.PropertiesUtil;
import tneb.ccms.admin.valuebeans.AdminUserValueBean;
import tneb.ccms.admin.valuebeans.CircleValueBean;
import tneb.ccms.admin.valuebeans.ClosureReasonValueBean;
import tneb.ccms.admin.valuebeans.DivisionValueBean;
import tneb.ccms.admin.valuebeans.RegionValueBean;
import tneb.ccms.admin.valuebeans.SectionValueBean;
import tneb.ccms.admin.valuebeans.SubDivisionValueBean;
import tneb.ccms.admin.valuebeans.ViewComplaintValueBean;

public class ServiceMethods {
	
	private Logger logger = LoggerFactory.getLogger(ServiceMethods.class.getName());
	
	@SuppressWarnings("unchecked")
	public JSONObject checkAuthentication(AdminUserValueBean adminUserValueBean) {

		JSONObject jsonOfficer = new JSONObject();
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<AdminUserBean> criteriaQuery = criteriaBuilder.createQuery(AdminUserBean.class);
			Root<AdminUserBean> root = criteriaQuery.from(AdminUserBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.and(criteriaBuilder.equal(root.get("userName"), adminUserValueBean.getUserName()),
					criteriaBuilder.equal(root.get("password"), adminUserValueBean.getPassword())));

			Query<AdminUserBean> query = session.createQuery(criteriaQuery).setMaxResults(1);
			AdminUserBean adminUser = null;
			try {
				adminUser = query.getSingleResult();
			} catch (NoResultException nre) {
				System.out.println("No User Found for the given credentials.");
				logger.info("No User Found for the given credentials.");
			}

			if (adminUser == null) {
				adminUserValueBean.setId(-1);
				jsonOfficer.put("iserror", 1);
				jsonOfficer.put("message", "Unauthorized entry!");

			} else {

				jsonOfficer.put("iserror", 0);
				jsonOfficer.put("officer_id", adminUser.getId());

				if (adminUser.getRegionBean() != null) {
					jsonOfficer.put("region_name", adminUser.getRegionBean().getName());
					jsonOfficer.put("region_id", adminUser.getRegionBean().getId());
				}
				
				if (adminUser.getCircleBean() != null) {
					jsonOfficer.put("circle_name", adminUser.getCircleBean().getName());
					jsonOfficer.put("circle_id", adminUser.getCircleBean().getId());
				}

				if (adminUser.getDivisionBean() != null) {
					jsonOfficer.put("division_name", adminUser.getDivisionBean().getName());
					jsonOfficer.put("division_id", adminUser.getDivisionBean().getId());
				}

				if (adminUser.getSubDivisionBean() != null) {
					jsonOfficer.put("sub_division_name", adminUser.getSubDivisionBean().getName());
					jsonOfficer.put("sub_division_id", adminUser.getSubDivisionBean().getId());
				}

				if (adminUser.getSectionBean() != null) {
					jsonOfficer.put("section_name", adminUser.getSectionBean().getName());
					jsonOfficer.put("section_id", adminUser.getSectionBean().getId());
				}

				
				jsonOfficer.put("role_id", adminUser.getRoleBean().getId());
				jsonOfficer.put("user_id", adminUser.getId());
				

				switch (adminUser.getRoleBean().getId()) {
				case 1:
					jsonOfficer.put("office_id", jsonOfficer.get("section_id"));
					jsonOfficer.put("field_name", CCMSConstants.OFFICE_NAME[0]);
					break;
				case 2:
					jsonOfficer.put("office_id", jsonOfficer.get("sub_division_id"));
					jsonOfficer.put("field_name", CCMSConstants.OFFICE_NAME[1]);
					break;
				case 3:
					jsonOfficer.put("office_id", jsonOfficer.get("division_id"));
					jsonOfficer.put("field_name", CCMSConstants.OFFICE_NAME[2]);
					break;
				case 4:
					jsonOfficer.put("office_id", jsonOfficer.get("circle_id"));
					jsonOfficer.put("field_name", CCMSConstants.OFFICE_NAME[3]);
					break;
				case 5:
				case 6:
				case 7:
				case 8:
				case 9:
					jsonOfficer.put("office_id", jsonOfficer.get("region_id"));
					jsonOfficer.put("field_name", CCMSConstants.OFFICE_NAME[4]);
					break;
				}

			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			jsonOfficer.put("iserror", 1);
			jsonOfficer.put("message", e.getMessage());
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return jsonOfficer;

	}

	@SuppressWarnings("unchecked")
	public JSONObject createDashboard(LoginParams officer) {
		JSONObject jsonDash = new JSONObject();
		JSONArray jsonStaus = null;
		JSONObject jsonData = null;
/**		JSONObject jsonData1 = null;
		JSONObject jsonData2 = null;
		JSONObject jsonData3 = null; */
		
		ComplaintsDao comp = new ComplaintsDao();
		int dash[][] = null;
		dash = comp.getDashboardData(officer);
		
		if (dash[0][0] > -1) {
			
			jsonStaus = new JSONArray();
			
			for (int i = 0; i < dash.length; i++) {
				

 				jsonData = new JSONObject();
				jsonData.put("Complaintname", CCMSConstants.COMPLAINT_NAME[i]);
				jsonData.put("Pending", dash[i][0]);
				jsonData.put("InProgress", dash[i][1]);
				jsonData.put("Completed", dash[i][2]);
				jsonData.put("TotalCount", dash[i][1] + dash[i][0]);
				jsonStaus.add(jsonData);

				/*			
	 			logger.info("MadrasMani: data " + i + ") " + jsonStaus);				
				
				jsonStaus = new JSONArray();
				
				jsonData2 = new JSONObject();
				jsonData2.put("Pending", dash[i][0]);
				jsonStaus.add(jsonData2);

				jsonData1 = new JSONObject();
				jsonData1.put("InProgress", dash[i][1]);
				jsonStaus.add(jsonData1);
				
				jsonData = new JSONObject();
				jsonData.put("Completed", dash[i][2]);
				jsonStaus.add(jsonData);
				
				jsonData3 = new JSONObject();
				jsonData3.put("Total", dash[i][1] + dash[i][0]);
				jsonStaus.add(jsonData3);

				jsonDash.put(String.valueOf(i), jsonStaus);
				jsonDash.put(CCMSConstants.COMPLAINT_NAME[i], jsonStaus);
				*/			
			}
			jsonDash.put("data", jsonStaus);
			jsonDash.put("iserror", 0);
		} else {
			jsonDash.put("iserror", 1);
			jsonDash.put("message", "No Data Found!.");
		}

		return jsonDash;

	}

	@SuppressWarnings("unchecked")
	public JSONObject getComplaintList(LoginParams officer, int status, int complaintCode) {
		ComplaintsDao dao = new ComplaintsDao();
		JSONObject json = new JSONObject();
		JSONObject jsonRoot = new JSONObject();
		JSONArray arrData = new JSONArray();
		PropertiesUtil propertiesUtil = new PropertiesUtil();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		try {
			List<ViewComplaintValueBean> complaints = dao.getComplaintList(officer, status, complaintCode);

			if (complaints.size() > 0) {
				for (ViewComplaintValueBean viewComplaintValueBean : complaints) {

					json = new JSONObject();
					json = loadComplaintJson(json, viewComplaintValueBean, dateFormat, propertiesUtil);
					arrData.add(json);

				}

				jsonRoot.put("complaint_list", arrData);
				jsonRoot.put("iserror", 0);
			} else {
				jsonRoot.put("message", "no data found");
				jsonRoot.put("iserror", 0);
			}

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			jsonRoot.put("message", "Invalid Operation!!!");
			jsonRoot.put("iserror", 1);
		}
		return jsonRoot;

	}
	
	
	@SuppressWarnings("unchecked")
	public JSONObject complaintSearch(Integer complaintNumber, String consumerMobileNumber) {
		ComplaintsDao dao = new ComplaintsDao();
		JSONObject json = new JSONObject();
		JSONObject jsonRoot = new JSONObject();
		JSONArray arrData = new JSONArray();
		PropertiesUtil propertiesUtil = new PropertiesUtil();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		try {
			List<ViewComplaintValueBean> complaints = new ArrayList<ViewComplaintValueBean>();
			
			if(StringUtils.isNotBlank(consumerMobileNumber)) {
				complaints = dao.getComplaintByConsumerMobileNumber(consumerMobileNumber);
			} else {
				complaints = dao.getComplaintById(complaintNumber);
			}
			

			if (complaints.size() > 0) {
				for (ViewComplaintValueBean viewComplaintValueBean : complaints) {

					json = new JSONObject();
					json = loadComplaintJson(json, viewComplaintValueBean, dateFormat, propertiesUtil);
					arrData.add(json);

				}

				jsonRoot.put("complaint_list", arrData);
				jsonRoot.put("iserror", 0);
			} else {
				jsonRoot.put("message", "no data found");
				jsonRoot.put("iserror", 0);
			}

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			jsonRoot.put("message", "Invalid Operation!!!");
			jsonRoot.put("iserror", 1);
		}
		return jsonRoot;

	}
	
	@SuppressWarnings("unchecked")
	private JSONObject loadComplaintJson(JSONObject json, ViewComplaintValueBean viewComplaintValueBean, SimpleDateFormat dateFormat, PropertiesUtil propertiesUtil) {
		json.put("category", viewComplaintValueBean.getCategoryId());
		json.put("complaint_no", viewComplaintValueBean.getId());
		
		json.put("street", "");
		json.put("landmark", viewComplaintValueBean.getLandmark());
		json.put("description", viewComplaintValueBean.getDescription());
		json.put("circle", viewComplaintValueBean.getCircleName());
		json.put("section", viewComplaintValueBean.getSectionName());
		
		/**
		 * below 9 attributes are hidden and 'district' is added as per 
		 * the request from mobile front end code.
		 */
		json.put("district", viewComplaintValueBean.getCircleName());
	//	json.put("complaint_type", CCMSConstants.COMPLAINT_NAME[viewComplaintValueBean.getComplaintCode()]);
	//	json.put("complaint_code", viewComplaintValueBean.getComplaintCode());
	//	json.put("complaint_code_value", CCMSConstants.COMPLAINT_TYPE_CODE[viewComplaintValueBean.getComplaintCode()]);
		if (viewComplaintValueBean.getDevice().equals("Web")) {
			json.put("image_1", propertiesUtil.IMAGE_BASE_URL + viewComplaintValueBean.getImage1());
			json.put("image_2", propertiesUtil.IMAGE_BASE_URL + viewComplaintValueBean.getImage2());
		} else {
			json.put("image_1", viewComplaintValueBean.getImage1());
			json.put("image_2", viewComplaintValueBean.getImage2());
		}
	//	json.put("lat", viewComplaintValueBean.getLatitude());
	//	json.put("long", viewComplaintValueBean.getLongitude());
		json.put("complaint_date",dateFormat.format(viewComplaintValueBean.getCreatedOn()));
		json.put("src", viewComplaintValueBean.getDevice());
		json.put("status_name", CCMSConstants.STATUSES[viewComplaintValueBean.getStatusId()]);
	//	json.put("status_id", viewComplaintValueBean.getStatusId());
	//	json.put("mobile", viewComplaintValueBean.getMobile());
	//	json.put("service_address", viewComplaintValueBean.getServiceAddress());
		
		return json;
	}


	@SuppressWarnings("unchecked")
	public JSONObject getComplaintHistoryList(String complaint_id) {
		ComplaintsDao dao = new ComplaintsDao();
		JSONObject json = new JSONObject();
		JSONObject jsonRoot = new JSONObject();
		JSONArray arrData = new JSONArray();
		DataModel dataModel = new DataModel();
		try {
			dataModel = dao.getCompalintHistoryList(Integer.valueOf(complaint_id), dataModel);

			ArrayList<ComplaintLog> lstComplaintLog = dataModel.getLstComplaintLog();

			if (lstComplaintLog.size() > 0) {
				for (ComplaintLog complaintLog : lstComplaintLog) {
					json = new JSONObject();
					json.put("history_description", complaintLog.getDescription());
					json.put("history_status", CCMSConstants.STATUSES[complaintLog.getStatusId()]);
					json.put("history_date", complaintLog.getCompalintDatetime());
					//json.put("status_id", complaintLog.getStatusId());

					arrData.add(json);
				}
				jsonRoot.put("complaint_history", arrData);
				jsonRoot.put("iserror", 0);
			} else {
				jsonRoot.put("message", "no data found");
				jsonRoot.put("iserror", 0);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			jsonRoot.put("message", "Invalid Operation!!!");
			jsonRoot.put("iserror", 1);
		}

		return jsonRoot;

	}

	@SuppressWarnings("unchecked")
	public JSONObject takeAction(JSONObject jsonComplaint) {
		JSONObject objOpt = new JSONObject();

		SessionFactory factory = null;
		Session session = null;
		Transaction transaction = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			int complaintId = Integer.parseInt(jsonComplaint.get("complaint_id").toString());

			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<ComplaintBean> criteriaQuery = criteriaBuilder.createQuery(ComplaintBean.class);
			Root<ComplaintBean> root = criteriaQuery.from(ComplaintBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), complaintId));

			Query<ComplaintBean> query = session.createQuery(criteriaQuery).setMaxResults(1);
			
			ComplaintBean complaintBean = null;
			try {
				complaintBean = query.getSingleResult();
			} catch (NoResultException nre) {
				System.out.println("No Complaints Found for the given value.");
				logger.info("No Complaints Found for the given value.");
			}
			
			Timestamp updatedOn = new Timestamp(new Date().getTime());
			
			if(complaintBean != null) {
				
				AdminUserBean adminUserBean = new AdminUserBean();
				adminUserBean.setId(Integer.valueOf(jsonComplaint.get("user_id").toString()));
				
				ComplaintHistoryBean complaintHistoryBean = new ComplaintHistoryBean();
				complaintHistoryBean.setDescription(jsonComplaint.get("description").toString());
				complaintHistoryBean.setStatusId(Integer.parseInt(jsonComplaint.get("status_id").toString()));
				complaintHistoryBean.setSectionBean(complaintBean.getSectionBean());
				complaintHistoryBean.setRegionBean(complaintBean.getRegionBean());
				complaintHistoryBean.setCircleBean(complaintBean.getCircleBean());
				complaintHistoryBean.setDivisionBean(complaintBean.getDivisionBean());
				complaintHistoryBean.setSubDivisionBean(complaintBean.getSubDivisionBean());
				complaintHistoryBean.setAdminUserBean(adminUserBean);

				complaintBean.setUpdatedOn(updatedOn);
				complaintBean.setStatusId(Integer.parseInt(jsonComplaint.get("status_id").toString()));

				complaintHistoryBean.setComplaintBean(complaintBean);
				complaintBean.getComplaintHistoryList().add(complaintHistoryBean);

				try {
					transaction = session.beginTransaction();
					session.saveOrUpdate(complaintBean);
					transaction.commit();
					session.refresh(complaintBean);
					objOpt.put("iserror", 0);
					objOpt.put("messaage", "successfully saved");
				} catch (PersistenceException ex) {
					objOpt.put("iserror", 1);
					objOpt.put("messaage", ex.getMessage());
				}
			} else {
				objOpt.put("iserror", 1);
				objOpt.put("messaage", "Invalid Data");
			}
		} catch (Exception e) {
			if(transaction != null) {
				transaction.rollback();
			}
			logger.error(ExceptionUtils.getStackTrace(e));
			objOpt.put("iserror", 1);
			objOpt.put("messaage", e.getMessage());
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

		return objOpt;

	}

	@SuppressWarnings("unchecked")
	public JSONObject transferAction(JSONObject jsonTransfer) {
		Date date = new Date();
		JSONObject objOpt = new JSONObject();
		SessionFactory factory = null;
		Session session = null;
		Transaction transaction = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			Timestamp ts = new Timestamp(date.getTime());
			int complaintId = Integer.valueOf(jsonTransfer.get("complaint_id").toString());

			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<ComplaintBean> criteriaQuery = criteriaBuilder.createQuery(ComplaintBean.class);
			Root<ComplaintBean> root = criteriaQuery.from(ComplaintBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), complaintId));

			Query<ComplaintBean> query = session.createQuery(criteriaQuery).setMaxResults(1);
			ComplaintBean complaintBean = null;
			try {
				complaintBean = query.getSingleResult();
			} catch (NoResultException nre) {
				System.out.println("No Complaints Found for the given value.");
				logger.info("No Complaints Found for the given value.");
			}
			
			if(complaintBean != null) {
				
				AdminUserBean adminUserBean = new AdminUserBean();
				adminUserBean.setId(Integer.valueOf(jsonTransfer.get("user_id").toString()));
				
				ComplaintHistoryBean complaintHistory = new ComplaintHistoryBean();
				ComplaintHistoryBean complaintHistoryTransfer = new ComplaintHistoryBean();
				
				RegionBean regionBean = new RegionBean();
				if(jsonTransfer.get("region_id") != null && jsonTransfer.get("region_id").toString().trim().length() > 0) {
					regionBean.setId(Integer.valueOf(jsonTransfer.get("region_id").toString()));
					complaintBean.setRegionBean(regionBean);
					complaintHistory.setRegionBean(regionBean);
					complaintHistoryTransfer.setRegionBean(regionBean);
				}
				
				CircleBean circleBean = new CircleBean();
				if(jsonTransfer.get("circle_id") != null && jsonTransfer.get("circle_id").toString().trim().length() > 0) {
					circleBean.setId(Integer.valueOf(jsonTransfer.get("circle_id").toString()));
					complaintBean.setCircleBean(circleBean);
					complaintHistory.setCircleBean(circleBean);
					complaintHistoryTransfer.setCircleBean(circleBean);
				}
				
				DivisionBean divisionBean = new DivisionBean();
				if(jsonTransfer.get("division_id") != null && jsonTransfer.get("division_id").toString().trim().length() > 0) {
					divisionBean.setId(Integer.valueOf(jsonTransfer.get("division_id").toString()));
					complaintBean.setDivisionBean(divisionBean);
					complaintHistory.setDivisionBean(divisionBean);
					complaintHistoryTransfer.setDivisionBean(divisionBean);
				}
				
				SubDivisionBean subDivisionBean = new SubDivisionBean();
				if(jsonTransfer.get("sub_division_id") != null && jsonTransfer.get("sub_division_id").toString().trim().length() > 0) {
					subDivisionBean.setId(Integer.valueOf(jsonTransfer.get("sub_division_id").toString()));
					complaintBean.setSubDivisionBean(subDivisionBean);
					complaintHistory.setSubDivisionBean(subDivisionBean);
					complaintHistoryTransfer.setSubDivisionBean(subDivisionBean);
				}
				
				SectionBean sectionBean = new SectionBean();
				if(jsonTransfer.get("section_id") != null && jsonTransfer.get("section_id").toString().trim().length() > 0) {
					sectionBean.setId(Integer.valueOf(jsonTransfer.get("section_id").toString()));
					complaintBean.setSectionBean(sectionBean);
				}
				
				complaintBean.setUpdatedOn(ts);

				
				complaintHistory.setDescription(jsonTransfer.get("description").toString());
				complaintHistory.setStatusId(1);
				complaintHistory.setComplaintBean(complaintBean);
				complaintHistory.setAdminUserBean(adminUserBean);
				
				
				String userName = jsonTransfer.get("userName") != null ? jsonTransfer.get("userName").toString() : "Unknown";
				
				complaintHistoryTransfer.setDescription("Compliant transferred by "+userName+" and transferred on "+GeneralUtil.formatDate(new Date()));
				complaintHistoryTransfer.setStatusId(1);
				complaintHistoryTransfer.setComplaintBean(complaintBean);
				complaintHistoryTransfer.setAdminUserBean(adminUserBean);
				
				complaintBean.getComplaintHistoryList().add(complaintHistory);
				complaintBean.getComplaintHistoryList().add(complaintHistoryTransfer);

				try {
					transaction = session.beginTransaction();
					session.saveOrUpdate(complaintBean);
					transaction.commit();
					session.refresh(complaintBean);
					objOpt.put("iserror", 0);
					objOpt.put("messaage", "successfully saved");
				} catch (PersistenceException ex) {
					objOpt.put("iserror", 1);
					objOpt.put("messaage", "Invalid Data");
				}

			} else {
				objOpt.put("iserror", 1);
				objOpt.put("messaage", "Invalid Data");
			}
		} catch (Exception e) {
			if(transaction != null) {
				transaction.rollback();
			}
			logger.error(ExceptionUtils.getStackTrace(e));
			objOpt.put("iserror", 1);
			objOpt.put("messaage", e.getMessage());
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		
		return objOpt;
	}

	@SuppressWarnings("unchecked")
	public JSONObject getRegions() {

		JSONObject regionJsonObject = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		DataModel dm = new DataModel();
		
		List<RegionValueBean> regionList = new ArrayList<RegionValueBean>();
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<RegionBean> criteriaQuery = criteriaBuilder.createQuery(RegionBean.class);
			Root<RegionBean> root = criteriaQuery.from(RegionBean.class);
			criteriaQuery.select(root).orderBy(criteriaBuilder.asc(root.get("name")));

			Query<RegionBean> query = session.createQuery(criteriaQuery);

			List<RegionBean> list = query.getResultList();
			
			for (RegionBean regionBean : list) {
				regionList.add(RegionValueBean.convertRegionBeanToRegionValueBean(regionBean));
			}

			dm.setLstRegions(regionList);
			
			
			if (list.size() > 0) {
				for (RegionBean regions : list) {
					jsonObject = new JSONObject();
					jsonObject.put("id", regions.getId());
					jsonObject.put("name", regions.getName());
					jsonArray.add(jsonObject);
				}

				regionJsonObject.put("region", jsonArray);
				regionJsonObject.put("iserror", 0);
				regionJsonObject.put("messaage", "success");
			}
			else {
				regionJsonObject.put("message", "no data found");
				regionJsonObject.put("iserror", 0);
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			regionJsonObject.put("iserror", 1);
			regionJsonObject.put("messaage", e.getMessage());
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return regionJsonObject;
	}

	@SuppressWarnings("unchecked")
	public JSONObject getCircles(String regionId) {

		List<CircleValueBean> circleList = new ArrayList<CircleValueBean>();
		
		JSONObject circlesJsonObject = new JSONObject();
		JSONObject jsonObject = null;
		JSONArray jsonArray = new JSONArray();
		DataModel dm = new DataModel();
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<CircleBean> criteriaQuery = criteriaBuilder.createQuery(CircleBean.class);
			Root<CircleBean> root = criteriaQuery.from(CircleBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("regionBean").get("id"), regionId))
			.orderBy(criteriaBuilder.asc(root.get("name")));

			Query<CircleBean> query = session.createQuery(criteriaQuery);
			List<CircleBean> list = query.getResultList();
			
			for(CircleBean circleBean : list) {
				circleList.add(CircleValueBean.convertCircleBeanToCircleValueBean(circleBean));
			}

			dm.setLstCircles(circleList);

			for (CircleBean circles : list) {
				jsonObject = new JSONObject();
				jsonObject.put("id", circles.getId());
				jsonObject.put("name", circles.getName());
				jsonArray.add(jsonObject);
			}

			circlesJsonObject.put("circle", jsonArray);
			circlesJsonObject.put("iserror", 0);
			circlesJsonObject.put("messaage", "success");
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			circlesJsonObject.put("iserror", 1);
			circlesJsonObject.put("messaage", e.getMessage());
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

		return circlesJsonObject;
	}

	@SuppressWarnings("unchecked")
	public JSONObject getDivisions(String circleId) {
		JSONObject divisionsJsonObject = new JSONObject();
		JSONObject jsonObject = null;
		JSONArray jsonArray = new JSONArray();
		DataModel dm = new DataModel();
		
		List<DivisionValueBean> divisionList = new ArrayList<DivisionValueBean>();
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<DivisionBean> criteriaQuery = criteriaBuilder.createQuery(DivisionBean.class);
			Root<DivisionBean> root = criteriaQuery.from(DivisionBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("circleBean").get("id"), circleId))
			.orderBy(criteriaBuilder.asc(root.get("name")));

			Query<DivisionBean> query = session.createQuery(criteriaQuery);
			List<DivisionBean> list = query.getResultList();
			
			for(DivisionBean divisionBean : list) {
				divisionList.add(DivisionValueBean.convertDivisionBeanToDivisionValueBean(divisionBean));
			}

			dm.setLstDivisions(divisionList);

			for (DivisionBean divisions : list) {
				jsonObject = new JSONObject();
				jsonObject.put("id", divisions.getId());
				jsonObject.put("name", divisions.getName());
				jsonArray.add(jsonObject);
			}

			divisionsJsonObject.put("division", jsonArray);
			divisionsJsonObject.put("iserror", 0);
			divisionsJsonObject.put("messaage", "success");
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			divisionsJsonObject.put("iserror", 1);
			divisionsJsonObject.put("messaage", e.getMessage());
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

		return divisionsJsonObject;
	}

	@SuppressWarnings("unchecked")
	public JSONObject getSubDivisions(String divisionId) {
		JSONObject subDivisionsJsonObject = new JSONObject();
		JSONObject jsonObject = null;
		JSONArray jsonArray = new JSONArray();
		DataModel dm = new DataModel();

		List<SubDivisionValueBean> subDivisionList = new ArrayList<SubDivisionValueBean>();
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<SubDivisionBean> criteriaQuery = criteriaBuilder.createQuery(SubDivisionBean.class);
			Root<SubDivisionBean> root = criteriaQuery.from(SubDivisionBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("divisionBean").get("id"), divisionId))
			.orderBy(criteriaBuilder.asc(root.get("name")));

			Query<SubDivisionBean> query = session.createQuery(criteriaQuery);
			List<SubDivisionBean> list = query.getResultList();
			
			for(SubDivisionBean subDivisionBean : list) {
				subDivisionList.add(SubDivisionValueBean.convertSubDivisionBeanToSubDivisionValueBean(subDivisionBean));
			}

			dm.setLstSubDivisions(subDivisionList);

			for (SubDivisionBean subDivisions : list) {
				jsonObject = new JSONObject();
				jsonObject.put("id", subDivisions.getId());
				jsonObject.put("name", subDivisions.getName());
				jsonArray.add(jsonObject);
			}

			subDivisionsJsonObject.put("sub_division", jsonArray);
			subDivisionsJsonObject.put("iserror", 0);
			subDivisionsJsonObject.put("messaage", "success");
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			subDivisionsJsonObject.put("iserror", 1);
			subDivisionsJsonObject.put("messaage", e.getMessage());
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

		return subDivisionsJsonObject;
	}

	@SuppressWarnings("unchecked")
	public JSONObject getSections(String subDivisionId) {
		JSONObject sectionJsonObject = new JSONObject();
		JSONObject jsonObject = null;
		JSONArray jsonArray = new JSONArray();
		DataModel dm = new DataModel();
		
		List<SectionValueBean> sectionList = new ArrayList<SectionValueBean>();
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<SectionBean> criteriaQuery = criteriaBuilder.createQuery(SectionBean.class);
			Root<SectionBean> root = criteriaQuery.from(SectionBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("subDivisionBean").get("id"), subDivisionId))
			.orderBy(criteriaBuilder.asc(root.get("name")));

			Query<SectionBean> query = session.createQuery(criteriaQuery);
			List<SectionBean> list = query.getResultList();
			
			for(SectionBean sectionBean : list) {
				sectionList.add(SectionValueBean.convertSectionBeanToSectionValueBean(sectionBean));
			}


			dm.setLstSections(sectionList);

			for (SectionBean sections : list) {
				jsonObject = new JSONObject();
				jsonObject.put("id", sections.getId());
				jsonObject.put("name", sections.getName());
				jsonArray.add(jsonObject);
			}

			sectionJsonObject.put("section", jsonArray);
			sectionJsonObject.put("iserror", 0);
			sectionJsonObject.put("messaage", "success");
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			sectionJsonObject.put("iserror", 1);
			sectionJsonObject.put("messaage", e.getMessage());
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		
		return sectionJsonObject;
	}

	@SuppressWarnings("unchecked")
	public JSONObject getCompletionReason(String complaintCode) {
		JSONObject jsonObject = null;
		JSONObject reasonJsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		DataModel dm = new DataModel();
		ComplaintsDao dao = new ComplaintsDao();

		try {
			List<ClosureReasonValueBean> closureReasonList = dao.getReasons(Integer.valueOf(complaintCode));

			dm.setLstReasons(closureReasonList);

			for (ClosureReasonValueBean closureReasonValueBean : closureReasonList) {
				jsonObject = new JSONObject();
				jsonObject.put("id", closureReasonValueBean.getId());
				jsonObject.put("name", closureReasonValueBean.getClosureReason());
				jsonArray.add(jsonObject);
			}

			reasonJsonObject.put("reason", jsonArray);
			reasonJsonObject.put("iserror", 0);
			reasonJsonObject.put("messaage", "success");
		}  catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			reasonJsonObject.put("iserror", 1);
			reasonJsonObject.put("messaage", e.getMessage());
		}
		return reasonJsonObject;
	}

}
