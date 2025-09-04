package tneb.ccms.admin.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.primefaces.PrimeFaces;
import org.primefaces.event.ToggleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jsf2leaf.model.LatLong;
import com.jsf2leaf.model.Layer;
import com.jsf2leaf.model.Map;
import com.jsf2leaf.model.Marker;
import com.jsf2leaf.model.Pulse;

import tneb.ccms.admin.AdminMain;
import tneb.ccms.admin.dao.CallCenterDao;
import tneb.ccms.admin.dao.ComplaintsDao;
import tneb.ccms.admin.dao.GeneralDao;
import tneb.ccms.admin.dao.UserDao;
import tneb.ccms.admin.model.CallCenterUserBean;
import tneb.ccms.admin.model.CategoryBean;
import tneb.ccms.admin.model.CircleBean;
import tneb.ccms.admin.model.ComplaintBean;
import tneb.ccms.admin.model.ComplaintHistoryBean;
import tneb.ccms.admin.model.DistrictBean;
import tneb.ccms.admin.model.DivisionBean;
import tneb.ccms.admin.model.FieldWorkerBean;
import tneb.ccms.admin.model.FieldWorkerComplaintBean;
import tneb.ccms.admin.model.LoginParams;
import tneb.ccms.admin.model.PublicUserBean;
import tneb.ccms.admin.model.RegionBean;
import tneb.ccms.admin.model.SectionBean;
import tneb.ccms.admin.model.SubCategoryBean;
import tneb.ccms.admin.model.SubDivisionBean;
import tneb.ccms.admin.model.ViewComplaintBean;
import tneb.ccms.admin.util.CCMSConstants;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.DataModel.ComplaintLog;
import tneb.ccms.admin.util.GeneralUtil;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.util.Network;
import tneb.ccms.admin.util.SMSUtil;
import tneb.ccms.admin.util.SmsClient;
import tneb.ccms.admin.valuebeans.CallCenterUserValueBean;
import tneb.ccms.admin.valuebeans.CircleValueBean;
import tneb.ccms.admin.valuebeans.ComplaintValueBean;
import tneb.ccms.admin.valuebeans.ConsumerServiceValueBean;
import tneb.ccms.admin.valuebeans.DistrictValueBean;
import tneb.ccms.admin.valuebeans.DivisionValueBean;
import tneb.ccms.admin.valuebeans.FieldWorkerValueBean;
import tneb.ccms.admin.valuebeans.PublicUserValueBean;
import tneb.ccms.admin.valuebeans.SectionValueBean;
import tneb.ccms.admin.valuebeans.SubCategoriesValueBean;
import tneb.ccms.admin.valuebeans.SubDivisionValueBean;
import tneb.ccms.admin.valuebeans.ViewComplaintValueBean;

public class CallCenterDashboard {

	private Logger logger = LoggerFactory.getLogger(CallCenterDashboard.class.getName());

	@ManagedProperty("#{admin}")
	AdminMain admin;

	List<ViewComplaintValueBean> complaintList;
	List<ViewComplaintValueBean> viewComplaintList;
	List<ViewComplaintValueBean> viewConsumerComplaintList;

	DistrictValueBean districtValueBean = new DistrictValueBean();
	ConsumerServiceValueBean consumerServiceValueBean = new ConsumerServiceValueBean();
	ViewComplaintValueBean complaint = new ViewComplaintValueBean();
	LoginParams officer;
	String description;
	String descriptionTransfer;
	String descriptionComplete;
	int sectionId;
	int regionId;
	DataModel dm = new DataModel();
	DataModel dmFilter = new DataModel();
	Map map = new Map();
	int complaintCode;
	int statusId;

	private String complaintNumber;
	private String consumerMobileNumber;
	private String serviceApplicationNo;

	public CallCenterDashboard() {
		super();
		init();

	}

	public void init() {
		
		try {
			if (complaintList == null) {
				admin = (AdminMain) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("admin");
				officer = admin.getAuth().getOfficer();

				CallCenterDao callCenterDao = new CallCenterDao();
				dm.arrDash = callCenterDao.getDashboardData(officer);
				if (dm.arrDash[0][0] == -1) {
					dm.arrDash[0][0] = 0;
				}

			}
			ComplaintsDao complaintsDao = new ComplaintsDao();
			dmFilter.setListDistrict(complaintsDao.listDistrict(officer.getCircleIdList()));
			
			dmFilter.setSmListDistrict(complaintsDao.smListDistrict());
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
	}
	
	public void setFilter() {
		listCircles(officer.getCircleIdList());
	}

	public void listCircles(List<Integer> circleIdList) {

		SessionFactory factory = null;
		Session session = null;
		try {

			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();

			List<CircleValueBean> circleList = new ArrayList<CircleValueBean>();
			CircleValueBean circleValueBean = null;

			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<Object[]> criteriaQuery = criteriaBuilder.createQuery(Object[].class);
			Root<CircleBean> root = criteriaQuery.from(CircleBean.class);

			//Constructing list of parameters
			List<Predicate> predicates = new ArrayList<Predicate>();

			Expression<String> circleExpression = root.get("id");
			Predicate compliantCodePredicate = circleExpression.in(circleIdList);
			predicates.add(compliantCodePredicate);

			criteriaQuery.multiselect(root.get("id"), root.get("name")).where(predicates.toArray(new Predicate[]{}))
			.orderBy(criteriaBuilder.asc(root.get("name")));

			Query<Object[]> query = session.createQuery(criteriaQuery);

			List<?> rows = query.list();

			if (rows.size() > 0) {
				Object[] row = null;
				for (int i = 0; i < rows.size(); i++) {
					row = (Object[]) rows.get(i);
					circleValueBean = new CircleValueBean();
					circleValueBean.setId(Integer.parseInt(row[0].toString()));
					circleValueBean.setName(row[1].toString());
					circleList.add(circleValueBean);
				}
			}


			dmFilter.setLstCircles(circleList);

			if (dmFilter.getLstDivisions() != null) {
				dmFilter.getLstDivisions().clear();
			}

			if (dmFilter.getLstSubDivisions() != null) {
				dmFilter.getLstSubDivisions().clear();
			}

			if (dmFilter.getLstSections() != null) {
				dmFilter.getLstSections().clear();
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

	}

	public void listDivisions(int circleId) {

		SessionFactory factory = null;
		Session session = null;
		try {

			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();

			List<DivisionValueBean> divisionList = new ArrayList<DivisionValueBean>();

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

			dmFilter.setLstDivisions(divisionList);

			if (dmFilter.getLstSubDivisions() != null) {
				dmFilter.getLstSubDivisions().clear();
			}

			if (dmFilter.getLstSections() != null) {
				dmFilter.getLstSections().clear();
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

	}


	public void listSubDivisions(int divisionId) {

		SessionFactory factory = null;
		Session session = null;
		try {

			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();

			List<SubDivisionValueBean> subDivisionList = new ArrayList<SubDivisionValueBean>();

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

			dmFilter.setLstSubDivisions(subDivisionList);

			if (dmFilter.getLstSections() != null) {
				dmFilter.getLstSections().clear();
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
	}


	public void listSections(int subDivisionId) {

		SessionFactory factory = null;
		Session session = null;
		try {

			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();

			List<SectionValueBean> sectionList = new ArrayList<SectionValueBean>();

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

			dmFilter.setLstSections(sectionList);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
	}

	public void showMap(ViewComplaintValueBean comp) {
		System.out.println("getLatitude " + comp.getLatitude());
		System.out.println("getLongitude " + comp.getLongitude());
		Layer placesLayer = (new Layer()).setLabel("Places");

		placesLayer.addMarker(new Marker(new LatLong(comp.getLatitude(), comp.getLongitude()), comp.getDescription(),
				new Pulse(true, 10, "#ff0000")));

		map.setWidth("600px").setHeight("400px").setCenter(new LatLong(comp.getLatitude(), comp.getLongitude()))
		.setZoom(16);
		map.addLayer(placesLayer);

		complaint = comp;
	}

	public void onToggle(ToggleEvent event) {
		try {
			int complaintId = (int) event.getComponent().getAttributes().get("complaintId");
			ComplaintsDao complaintsDao = new ComplaintsDao();
			complaintsDao.getCompalintHistoryList(complaintId, dm);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}

	}


	public void loadSubCategory() {

		SessionFactory factory = null;
		Session session = null;
		try {

			if(StringUtils.isNotBlank(dmFilter.getComplaintType())) {

				Integer compCode = Integer.parseInt(dmFilter.getComplaintType());
				CategoryBean categoryBean = null;
				if(compCode == 0) {
					GeneralDao dao = new GeneralDao();
					categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_POWER_FAILURE);
				} else if(compCode == 1) {
					GeneralDao dao = new GeneralDao();
					categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_VOLTAGE_RELATED);
				} else if(compCode == 2) {
					GeneralDao dao = new GeneralDao();
					categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_METER_RELATED);
				} else if(compCode == 3) {
					GeneralDao dao = new GeneralDao();
					categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_BILLING);
				} else if(compCode == 4) {
					GeneralDao dao = new GeneralDao();
					categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_FIRE);
				} else if(compCode == 5) {
					GeneralDao dao = new GeneralDao();
					categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_POSTING_DANGER);
				} else if(compCode == 6) {
					GeneralDao dao = new GeneralDao();
					categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_THEFT_OF_POWER);
				} else if(compCode == 7) {
					GeneralDao dao = new GeneralDao();
					categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_APPLICATION_RELATED);
				} else if(compCode == 8) {
					GeneralDao dao = new GeneralDao();
					categoryBean = dao.getCategoryBeanByName(CCMSConstants.CATEGORY_CONDUCTOR_SNAPPING);
				} 

				factory = HibernateUtil.getSessionFactory();
				session = factory.openSession();

				List<SubCategoriesValueBean> subCategoriesList = new ArrayList<SubCategoriesValueBean>();

				CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<SubCategoryBean> criteriaQuery = criteriaBuilder.createQuery(SubCategoryBean.class);
				Root<SubCategoryBean> root = criteriaQuery.from(SubCategoryBean.class);

				criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("categoryBean"), categoryBean))
				.orderBy(criteriaBuilder.asc(criteriaBuilder.upper(root.get("name"))));

				Query<SubCategoryBean> query = session.createQuery(criteriaQuery);
				List<SubCategoryBean> list = query.getResultList();

				for(SubCategoryBean subCategoryBean : list) {
					subCategoriesList.add(SubCategoriesValueBean.convertSubCategoriesBeanToSubCategoriesValueBean(subCategoryBean));
				}

				dmFilter.setListSubCategory(subCategoriesList);
			} else {
				if (dmFilter.getListSubCategory() != null) {
					dmFilter.getListSubCategory().clear();
				}
			}

			if (dmFilter.getSubCategory() != null) {
				dmFilter.setSubCategory(null);
			}

		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
	}

	public void loadDistrictSections() {

		SessionFactory factory = null;
		Session session = null;
		try {

			if(StringUtils.isNotBlank(dmFilter.getDistrictDropDown())) {

				factory = HibernateUtil.getSessionFactory();
				session = factory.openSession();

				List<DistrictValueBean> districtList = new ArrayList<DistrictValueBean>();

				CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<DistrictBean> criteriaQuery = criteriaBuilder.createQuery(DistrictBean.class);
				Root<DistrictBean> root = criteriaQuery.from(DistrictBean.class);

				criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("name"), dmFilter.getDistrictDropDown()))
				.orderBy(criteriaBuilder.asc(criteriaBuilder.upper(root.get("sectionName"))));

				Query<DistrictBean> query = session.createQuery(criteriaQuery);
				List<DistrictBean> list = query.getResultList();

				for(DistrictBean bean : list) {
					districtList.add(DistrictValueBean.convertDistrictBeanToDistrictValueBean(bean));
				}

				dmFilter.setListDistrictSection(districtList);
			} else {
				if (dmFilter.getListDistrictSection() != null) {
					dmFilter.getListDistrictSection().clear();
				}
			}

			if (dmFilter.getSectionDropDown() != null) {
				dmFilter.setSectionDropDown(null);
			}

		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
	}
	
//	public void loadSmDistrictSections() {
//
//		SessionFactory factory = null;
//		Session session = null;
//		try {
//
//			if(StringUtils.isNotBlank(dmFilter.getSmDistrictDropdown())) {
//
//				factory = HibernateUtil.getSessionFactory();
//				session = factory.openSession();
//
//				List<DistrictValueBean> districtList = new ArrayList<DistrictValueBean>();
//
//				CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
//				CriteriaQuery<DistrictBean> criteriaQuery = criteriaBuilder.createQuery(DistrictBean.class);
//				Root<DistrictBean> root = criteriaQuery.from(DistrictBean.class);
//
//				criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("name"), dmFilter.getSmDistrictDropdown()))
//				.orderBy(criteriaBuilder.asc(criteriaBuilder.upper(root.get("sectionName"))));
//
//				Query<DistrictBean> query = session.createQuery(criteriaQuery);
//				List<DistrictBean> list = query.getResultList();
//
//				for(DistrictBean bean : list) {
//					districtList.add(DistrictValueBean.convertDistrictBeanToDistrictValueBean(bean));
//				}
//
//				dmFilter.setSmListDistrictSection(districtList);
//			} else {
//				if (dmFilter.getSmListDistrictSection() != null) {
//					dmFilter.getSmListDistrictSection().clear();
//				}
//			}
//
//			if (dmFilter.getSmSectionDropdown() != null) {
//				dmFilter.setSmSectionDropdown(null);
//			}
//
//		} catch (Exception e) {
//			logger.error(ExceptionUtils.getStackTrace(e));
//			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
//		} finally {
//			HibernateUtil.closeSession(factory, session);
//		}
//	}
	
	public void loadSmDistrictSections() {

		SessionFactory factory = null;
		Session session = null;
		try {
			
			 List<SectionValueBean> combinedSectionList = new ArrayList<>();


			if(StringUtils.isNotBlank(dmFilter.getSmDistrictDropdown())) {

				factory = HibernateUtil.getSessionFactory();
				session = factory.openSession();

				List<DistrictValueBean> districtList = new ArrayList<DistrictValueBean>();

				CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<DistrictBean> criteriaQuery = criteriaBuilder.createQuery(DistrictBean.class);
				Root<DistrictBean> root = criteriaQuery.from(DistrictBean.class);

				criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("name"), dmFilter.getSmDistrictDropdown()))
				.orderBy(criteriaBuilder.asc(criteriaBuilder.upper(root.get("sectionName"))));

				Query<DistrictBean> query = session.createQuery(criteriaQuery);
				List<DistrictBean> list = query.getResultList();

				for(DistrictBean bean : list) {
					//districtList.add(DistrictValueBean.convertDistrictBeanToDistrictValueBean(bean));
					 DistrictValueBean districtValueBean = DistrictValueBean.convertDistrictBeanToDistrictValueBean(bean);
		           
					SectionValueBean section = convertDistrictValueBeanToSectionValueBean(districtValueBean);
				    section.setName(bean.getSectionName());  
				    combinedSectionList.add(section); 
				}

				dmFilter.setLstSections(combinedSectionList);
			} else {
				if (dmFilter.getLstSections() != null) {
					dmFilter.getLstSections().clear();
				}
			}

			if (dmFilter.getSmSectionDropdown() != null) {
				dmFilter.setSmSectionDropdown(null);
			}

		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
	}
	private SectionValueBean convertDistrictValueBeanToSectionValueBean(DistrictValueBean districtValueBean) {
	    SectionValueBean sectionValueBean = new SectionValueBean();
	    sectionValueBean.setId(districtValueBean.getId());  
	    sectionValueBean.setName(districtValueBean.getName());
	    return sectionValueBean;
	}


	public void loadFieldWorker() {

		SessionFactory factory = null;
		Session session = null;
		try {

			if(StringUtils.isNotBlank(dmFilter.getSectionDropDown())) {

				factory = HibernateUtil.getSessionFactory();
				session = factory.openSession();

				List<FieldWorkerValueBean> workerList = new ArrayList<FieldWorkerValueBean>();

				CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<DistrictBean> criteriaQuery = criteriaBuilder.createQuery(DistrictBean.class);
				Root<DistrictBean> root = criteriaQuery.from(DistrictBean.class);

				criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), dmFilter.getSectionDropDown()));

				Query<DistrictBean> query = session.createQuery(criteriaQuery);
				List<DistrictBean> list = query.getResultList();

				DistrictBean districtBean = list.get(0);

				CriteriaBuilder criteriaBuilderSection = session.getCriteriaBuilder();
				CriteriaQuery<SectionBean> criteriaQuerySection = criteriaBuilderSection.createQuery(SectionBean.class);
				Root<SectionBean> rootSection = criteriaQuerySection.from(SectionBean.class);

				criteriaQuerySection.select(rootSection).where(criteriaBuilderSection.and(criteriaBuilderSection.equal(root.get("code"), districtBean.getSectionCode()),
						criteriaBuilderSection.equal(root.get("regionBean").get("id"), districtBean.getRegionBean().getId())));

				Query<SectionBean> querySection = session.createQuery(criteriaQuerySection);
				List<SectionBean> listSection = querySection.getResultList();

				SectionBean sectionBean = listSection.get(0);

				CriteriaBuilder criteriaBuilderWorker = session.getCriteriaBuilder();
				CriteriaQuery<FieldWorkerBean> criteriaQueryWorker = criteriaBuilderWorker.createQuery(FieldWorkerBean.class);
				Root<FieldWorkerBean> rootWorker = criteriaQueryWorker.from(FieldWorkerBean.class);

				criteriaQueryWorker.select(rootWorker).where(criteriaBuilderWorker.equal(rootWorker.get("sectionBean").get("id"), sectionBean.getId()));

				Query<FieldWorkerBean> queryWorker = session.createQuery(criteriaQueryWorker);
				List<FieldWorkerBean> listWorker = queryWorker.getResultList();

				for(FieldWorkerBean bean : listWorker) {
					workerList.add(FieldWorkerValueBean.convertBeanToValueBean(bean));
				}

				dmFilter.setListFieldWorker(workerList);
			} else {
				if (dmFilter.getListFieldWorker() != null) {
					dmFilter.getListFieldWorker().clear();
				}
			}

			if (dmFilter.getFieldWorkerDropDown() != null) {
				dmFilter.setFieldWorkerDropDown(null);
			}

		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
	}
	
	public void loadFieldWorkerSm() {

		SessionFactory factory = null;
		Session session = null;
		try {
			
			if(StringUtils.isNotBlank(dmFilter.getSmSectionDropdown())) {

				System.out.println(" shah : "+dmFilter.getSmSectionDropdown().toString());
				
				
				
				factory = HibernateUtil.getSessionFactory();
				session = factory.openSession();

				List<FieldWorkerValueBean> workerList = new ArrayList<FieldWorkerValueBean>();

				CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<SectionBean> criteriaQuery = criteriaBuilder.createQuery(SectionBean.class);
				Root<SectionBean> root = criteriaQuery.from(SectionBean.class);

				criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), dmFilter.getSmSectionDropdown()));

				Query<SectionBean> query = session.createQuery(criteriaQuery);
				List<SectionBean> listSection = query.getResultList();

				if (!listSection.isEmpty()) {
					SectionBean sectionBean = listSection.get(0);
					
					 CriteriaBuilder criteriaBuilderWorker = session.getCriteriaBuilder();
		                CriteriaQuery<FieldWorkerBean> criteriaQueryWorker = criteriaBuilderWorker.createQuery(FieldWorkerBean.class);
		                Root<FieldWorkerBean> rootWorker = criteriaQueryWorker.from(FieldWorkerBean.class);

		                criteriaQueryWorker.select(rootWorker).where(criteriaBuilderWorker.equal(rootWorker.get("sectionBean").get("id"), sectionBean.getId()));

		                Query<FieldWorkerBean> queryWorker = session.createQuery(criteriaQueryWorker);
		                List<FieldWorkerBean> listWorker = queryWorker.getResultList();

		                for (FieldWorkerBean bean : listWorker) {
		                    workerList.add(FieldWorkerValueBean.convertBeanToValueBean(bean));
		                }
		            }

				dmFilter.setListFieldWorker(workerList);
				System.out.println("FieldWorker : "+workerList);
				System.out.println("FieldWorker : "+dmFilter.getListFieldWorker().toString());
				
				
			} else {
				if (dmFilter.getListFieldWorker() != null) {
					dmFilter.getListFieldWorker().clear();
				}
			}

			if (dmFilter.getFieldWorkerDropDown() != null) {
				dmFilter.setFieldWorkerDropDown(null);
			}

		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
	}
	
	public void loadFieldWorkerBySection(Integer sectionId) {

		SessionFactory factory = null;
		Session session = null;
		try {

			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();

			List<FieldWorkerValueBean> workerList = new ArrayList<FieldWorkerValueBean>();

			CriteriaBuilder criteriaBuilderWorker = session.getCriteriaBuilder();
			CriteriaQuery<FieldWorkerBean> criteriaQueryWorker = criteriaBuilderWorker.createQuery(FieldWorkerBean.class);
			Root<FieldWorkerBean> rootWorker = criteriaQueryWorker.from(FieldWorkerBean.class);

			criteriaQueryWorker.select(rootWorker).where(criteriaBuilderWorker.equal(rootWorker.get("sectionBean").get("id"), sectionId))
			.orderBy(criteriaBuilderWorker.asc(rootWorker.get("name")));

			Query<FieldWorkerBean> queryWorker = session.createQuery(criteriaQueryWorker);
			List<FieldWorkerBean> listWorker = queryWorker.getResultList();

			for(FieldWorkerBean bean : listWorker) {
				workerList.add(FieldWorkerValueBean.convertBeanToValueBean(bean));
			}

			dmFilter.setListFieldWorker(workerList);


			if (dmFilter.getFieldWorkerDropDown() != null) {
				dmFilter.setFieldWorkerDropDown(null);
			}

		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
	}

	public void loadConsumerAddress() {

		viewConsumerComplaintList =  new ArrayList<ViewComplaintValueBean>();
		SessionFactory factory = null;
		Session session = null;
		try {

			if(StringUtils.isNotBlank(consumerServiceValueBean.getMobile()) && consumerServiceValueBean.getMobile().length() == 10) {
				this.dmFilter.setAddress("");
				this.dmFilter.setLandMark("");

				factory = HibernateUtil.getSessionFactory();
				session = factory.openSession();

				CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<PublicUserBean> criteriaQuery = criteriaBuilder.createQuery(PublicUserBean.class);
				Root<PublicUserBean> root = criteriaQuery.from(PublicUserBean.class);

				criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("mobile"), consumerServiceValueBean.getMobile()));

				Query<PublicUserBean> query = session.createQuery(criteriaQuery);
				List<PublicUserBean> list = query.getResultList();

				if(list.size() > 0) {
					PublicUserBean publicUserBean = list.get(0);
					if(StringUtils.isNotBlank(publicUserBean.getAddress())) {
						dmFilter.setAddress(publicUserBean.getAddress());
					}
					if(StringUtils.isNotBlank(publicUserBean.getLandMark())) {
						dmFilter.setLandMark(publicUserBean.getLandMark());
					}
				}


				CriteriaBuilder criteriaBuilderComplaint = session.getCriteriaBuilder();
				CriteriaQuery<ViewComplaintBean> criteriaQueryComplaint = criteriaBuilderComplaint.createQuery(ViewComplaintBean.class);
				Root<ViewComplaintBean> rootComplaint = criteriaQueryComplaint.from(ViewComplaintBean.class);
				criteriaQueryComplaint.select(rootComplaint).where(criteriaBuilderComplaint.and(criteriaBuilderComplaint.equal(rootComplaint.get("mobile"),
						consumerServiceValueBean.getMobile()), criteriaBuilderComplaint.lessThan(rootComplaint.get("statusId"), CCMSConstants.COMPLETED)));

				Query<ViewComplaintBean> queryComplaint = session.createQuery(criteriaQueryComplaint);
				List<ViewComplaintBean> complaintList = queryComplaint.getResultList();

				if(complaintList.size() > 0) {
					for(ViewComplaintBean viewComplaintBean : complaintList) {
						viewConsumerComplaintList.add(ViewComplaintValueBean.convertViewComplaintBeanToViewComplaintValueBean(viewComplaintBean));
					}

					PrimeFaces.current().executeScript("PF('dlgComp').show()");
				}

			} 			

		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
	}


	public void showAction(ViewComplaintValueBean comp, int statusId) {
		try {
			comp.setStatusId(statusId);
			dm.setActionStatus(statusId);
			complaint = comp;
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}

	}

	public void showComplete(ViewComplaintValueBean comp) {
		try {
			ComplaintsDao dao = new ComplaintsDao();
			dm.setLstReasons(dao.getReasons(comp.getComplaintCode()));
			complaint = comp;
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}

	}

	public void listComplaints(int complaintCodeValue, int statusValue) {
		try {
			complaintNumber = "";
			consumerMobileNumber = "";
			complaintCode = complaintCodeValue;
			statusId = statusValue;

			boolean filterApplied = getFilterApplied();

			if(filterApplied) {
				ComplaintsDao daoComplaints = new ComplaintsDao();

				complaintList = daoComplaints.getComplaintList(officer, statusId, complaintCode);
			} else {
				CallCenterDao callCenterDao = new CallCenterDao();

				complaintList = callCenterDao.getComplaintList(officer, statusId, complaintCode);
			}

			dm.setLstComplaintLog(new ArrayList<ComplaintLog>());
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
	}

	private  void listComplaints() {

		try {
			ComplaintsDao daoComplaints = new ComplaintsDao();

			if(StringUtils.isNotBlank(complaintNumber)) {
				complaintList = daoComplaints.getComplaintById(Integer.parseInt(complaintNumber));
			} else if (StringUtils.isNotBlank(consumerMobileNumber)){
				complaintList = daoComplaints.getComplaintByConsumerMobileNumber(consumerMobileNumber);
			} else {
				boolean filterApplied = getFilterApplied();

				if(filterApplied) {
					complaintList = daoComplaints.getComplaintList(officer, statusId, complaintCode);
				} else {
					CallCenterDao callCenterDao = new CallCenterDao();
					complaintList = callCenterDao.getComplaintList(officer, statusId, complaintCode);
				}
			}

			dm.setLstComplaintLog(new ArrayList<ComplaintLog>());
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
	}

	public static HttpServletResponse getResponse() {
		return (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
	}

	public void takeAction() {

		SessionFactory factory = null;
		Session session = null;
		Transaction transaction = null;
		ComplaintBean complaintBean = null;

		try {

			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();

			Timestamp updatedOn = new Timestamp(new Date().getTime());
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<ComplaintBean> criteriaQuery = criteriaBuilder.createQuery(ComplaintBean.class);
			Root<ComplaintBean> root = criteriaQuery.from(ComplaintBean.class);

			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), complaint.getId()));

			Query<ComplaintBean> query = session.createQuery(criteriaQuery).setMaxResults(1);

			try {
				complaintBean = query.getSingleResult();
			} catch (NoResultException nre) {
				System.out.println("No Complaint Found for the given complaint id.");
				logger.info("No Complaint Found for the given complaint id.");
			}

			if(complaintBean != null) {
				complaintBean.setStatusId(dm.getActionStatus());

				complaintBean.setUpdatedOn(updatedOn);

				CallCenterUserBean callCenterUserBean = new CallCenterUserBean();
				callCenterUserBean.setId(officer.userId);

				ComplaintHistoryBean complaintHistoryBean = new ComplaintHistoryBean();
				complaintHistoryBean.setComplaintBean(complaintBean);
				complaintHistoryBean.setStatusId(dm.getActionStatus());
				complaintHistoryBean.setDescription(description);
				complaintHistoryBean.setSectionBean(complaintBean.getSectionBean());
				complaintHistoryBean.setRegionBean(complaintBean.getRegionBean());
				complaintHistoryBean.setCircleBean(complaintBean.getCircleBean());
				complaintHistoryBean.setDivisionBean(complaintBean.getDivisionBean());
				complaintHistoryBean.setSubDivisionBean(complaintBean.getSubDivisionBean());
				complaintHistoryBean.setCallCenterUserBean(callCenterUserBean);

				complaintBean.addToHistory(complaintHistoryBean);

				transaction = session.beginTransaction();
				session.saveOrUpdate(complaintBean);
				transaction.commit();
				session.refresh(complaintBean);

				HibernateUtil.closeSession(factory, session);

				updateDashBoardFilterComplaint();

				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Complaint updated successfully."));

			} else {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "No Complaint Found for the given complaint id."));
			}

		} catch (Exception e) {
			if(transaction != null) {
				transaction.rollback();
			}
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to update your complaint."));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

		PrimeFaces.current().executeScript("PF('dlgTakeAction').hide()");

	}

	public void closeComplaint() {

		SessionFactory factory = null;
		Session session = null;
		Transaction transaction = null;
		ComplaintBean complaintBean = null;

		try {

			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();

			Timestamp updatedOn = new Timestamp(new Date().getTime());
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<ComplaintBean> criteriaQuery = criteriaBuilder.createQuery(ComplaintBean.class);
			Root<ComplaintBean> root = criteriaQuery.from(ComplaintBean.class);

			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), complaint.getId()));

			Query<ComplaintBean> query = session.createQuery(criteriaQuery).setMaxResults(1);

			try {
				complaintBean = query.getSingleResult();
			} catch (NoResultException nre) {
				System.out.println("No Complaint Found for the given complaint id.");
				logger.info("No Complaint Found for the given complaint id.");
			}

			if(complaintBean != null) {


				CriteriaBuilder criteriaBuilderFieldWorker = session.getCriteriaBuilder();
				CriteriaQuery<FieldWorkerComplaintBean> criteriaQueryFieldWorker = criteriaBuilderFieldWorker.createQuery(FieldWorkerComplaintBean.class);
				Root<FieldWorkerComplaintBean> rootFieldWorker = criteriaQueryFieldWorker.from(FieldWorkerComplaintBean.class);

				criteriaQueryFieldWorker.select(rootFieldWorker).where(criteriaBuilderFieldWorker.equal(rootFieldWorker.get("complaintBean").get("id"), complaintBean.getId()));

				Query<FieldWorkerComplaintBean> queryFieldWorker = session.createQuery(criteriaQueryFieldWorker);

				List<FieldWorkerComplaintBean> fieldWorkerComplaintBeanList = queryFieldWorker.getResultList();

				for(FieldWorkerComplaintBean fieldWorkerComplaintBean :fieldWorkerComplaintBeanList) {
					fieldWorkerComplaintBean.setStatusId(CCMSConstants.COMPLETED);
					session.update(fieldWorkerComplaintBean);
				}


				complaintBean.setStatusId(CCMSConstants.COMPLETED);

				complaintBean.setUpdatedOn(updatedOn);

				CallCenterUserBean callCenterUserBean = new CallCenterUserBean();
				callCenterUserBean.setId(officer.userId);

				ComplaintHistoryBean complaintHistoryBean = new ComplaintHistoryBean();
				complaintHistoryBean.setComplaintBean(complaintBean);
				complaintHistoryBean.setStatusId(CCMSConstants.COMPLETED);
				complaintHistoryBean.setDescription(descriptionComplete);
				complaintHistoryBean.setSectionBean(complaintBean.getSectionBean());
				complaintHistoryBean.setRegionBean(complaintBean.getRegionBean());
				complaintHistoryBean.setCircleBean(complaintBean.getCircleBean());
				complaintHistoryBean.setDivisionBean(complaintBean.getDivisionBean());
				complaintHistoryBean.setSubDivisionBean(complaintBean.getSubDivisionBean());
				complaintHistoryBean.setCallCenterUserBean(callCenterUserBean);

				complaintBean.addToHistory(complaintHistoryBean);

				transaction = session.beginTransaction();
				session.saveOrUpdate(complaintBean);
				transaction.commit();
				session.refresh(complaintBean);

				String message = "CmplNo:" + complaintBean.getId() + ":Registered by you is Attended and Closed "
						+ GeneralUtil.formatDateWithTime(new Date()) + " Save Electricity - TANGEDCO";

				String smsId = SMSUtil.sendSMS(null, complaintBean.getPublicUserBean().getMobile(), message);

				SmsClient.sendSms(smsId);

				HibernateUtil.closeSession(factory, session);

				updateDashBoardFilterComplaint();

				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Complaint closed successfully."));
			} else {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "No Complaint Found for the given complaint id."));
			}


		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to close your complaint."));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

		PrimeFaces.current().executeScript("PF('dlgCloseComplaint').hide()");

	}

	public void saveSection() {
		System.out.println(dm.getSections().getId());
		if(dm.getSections() != null && dm.getSections().getId() != null) {
			sectionId = dm.getSections().getId();
		}
	}

	public void updateDashBoard() {
		try {
			loadDashBoardData();
			ComplaintsDao daoComplaints = new ComplaintsDao();
			complaintList = daoComplaints.getComplaintList(officer);
			dm.setLstComplaintLog(new ArrayList<ComplaintLog>());
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
	}

	private boolean getFilterApplied() {

		boolean filterApplied = false;
		if (dmFilter.getCircles() != null && dmFilter.getCircles().getId() != null) {
			officer.fieldName = CCMSConstants.OFFICE_NAME[3];
			officer.circleId = dmFilter.getCircles().getId();
			officer.officeId = dmFilter.getCircles().getId();
			filterApplied = true;
		}
		if (dmFilter.getDivisions() != null && dmFilter.getDivisions().getId() != null) {
			officer.fieldName = CCMSConstants.OFFICE_NAME[2];
			officer.divisionId = dmFilter.getDivisions().getId();
			officer.officeId = dmFilter.getDivisions().getId();
			filterApplied = true;
		}
		if (dmFilter.getSubDivisions() != null && dmFilter.getSubDivisions().getId() != null) {
			officer.fieldName = CCMSConstants.OFFICE_NAME[1];
			officer.subDivisionId = dmFilter.getSubDivisions().getId();
			officer.officeId = dmFilter.getSubDivisions().getId();
			filterApplied = true;
		}
		if (dmFilter.getSections() != null && dmFilter.getSections().getId() != null) {
			officer.fieldName = CCMSConstants.OFFICE_NAME[0];
			officer.sectionId = dmFilter.getSections().getId();
			officer.officeId = dmFilter.getSections().getId();
			filterApplied = true;
		}

		return filterApplied;
	}

	private void loadDashBoardData() {

		boolean filterApplied = getFilterApplied();

		if(filterApplied) {
			ComplaintsDao daoComplaints = new ComplaintsDao();

			dm.arrDash = daoComplaints.getDashboardData(officer);
			if (dm.arrDash[0][0] == -1) {
				dm.arrDash[0][0] = 0;
			}
		} else {
			CallCenterDao callCenterDao = new CallCenterDao();

			dm.arrDash = callCenterDao.getDashboardData(officer);
			if (dm.arrDash[0][0] == -1) {
				dm.arrDash[0][0] = 0;
			}
		}

	}

	private void updateDashBoardFilterComplaint() {
		loadDashBoardData();
		listComplaints();
		dm.setLstComplaintLog(new ArrayList<ComplaintLog>());
	}

	public void transferComplaint() {

		SessionFactory factory = null;
		Session session = null;
		Transaction transaction = null;
		ComplaintBean complaintBean = null;

		try {

			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();

			Timestamp updatedOn = new Timestamp(new Date().getTime());
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<ComplaintBean> criteriaQuery = criteriaBuilder.createQuery(ComplaintBean.class);
			Root<ComplaintBean> root = criteriaQuery.from(ComplaintBean.class);

			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), complaint.getId()));

			Query<ComplaintBean> query = session.createQuery(criteriaQuery).setMaxResults(1);

			try {
				complaintBean = query.getSingleResult();
			} catch (NoResultException nre) {
				System.out.println("No Complaint Found for the given complaint id.");
				logger.info("No Complaint Found for the given complaint id.");
			}

			if(complaintBean != null) {

				CallCenterUserBean callCenterUserBean = new CallCenterUserBean();
				callCenterUserBean.setId(officer.userId);

				ComplaintHistoryBean complaintHistory = new ComplaintHistoryBean();
				ComplaintHistoryBean complaintHistoryTransfer = new ComplaintHistoryBean();

				complaintBean.setStatusId(dm.getActionStatus());

				RegionBean regionBean = new RegionBean();
				if(dm.getRegions() != null && dm.getRegions().getId() != null) {
					regionBean.setId(dm.getRegions().getId());
					complaintBean.setRegionBean(regionBean);
					complaintHistory.setRegionBean(regionBean);
					complaintHistoryTransfer.setRegionBean(regionBean);
				}

				CircleBean circleBean = new CircleBean();
				if(dm.getCircles() != null && dm.getCircles().getId() != null) {
					circleBean.setId(dm.getCircles().getId());
					complaintBean.setCircleBean(circleBean);
					complaintHistory.setCircleBean(circleBean);
					complaintHistoryTransfer.setCircleBean(circleBean);
				}

				DivisionBean divisionBean = new DivisionBean();
				if(dm.getDivisions() != null && dm.getDivisions().getId() != null) {
					divisionBean.setId(dm.getDivisions().getId());
					complaintBean.setDivisionBean(divisionBean);
					complaintHistory.setDivisionBean(divisionBean);
					complaintHistoryTransfer.setDivisionBean(divisionBean);
				}

				SubDivisionBean subDivisionBean = new SubDivisionBean();
				if(dm.getSubDivisions() != null && dm.getSubDivisions().getId() != null) {
					subDivisionBean.setId(dm.getSubDivisions().getId());
					complaintBean.setSubDivisionBean(subDivisionBean);
					complaintHistory.setSubDivisionBean(subDivisionBean);
					complaintHistoryTransfer.setSubDivisionBean(subDivisionBean);
				}

				SectionBean sectionBean = new SectionBean();
				if(sectionId > 0) {
					sectionBean.setId(sectionId);
					complaintBean.setSectionBean(sectionBean);
				}

				complaintBean.setUpdatedOn(updatedOn);

				complaintHistory.setCallCenterUserBean(callCenterUserBean);
				complaintHistory.setComplaintBean(complaintBean);
				complaintHistory.setStatusId(dm.getActionStatus());
				complaintHistory.setDescription(descriptionTransfer);

				complaintHistoryTransfer.setCallCenterUserBean(callCenterUserBean);
				complaintHistoryTransfer.setComplaintBean(complaintBean);
				complaintHistoryTransfer.setStatusId(dm.getActionStatus());
				complaintHistoryTransfer.setDescription("Compliant transferred by "+officer.getUserName()+" and transferred on "+GeneralUtil.formatDate(new Date()));


				complaintBean.addToHistory(complaintHistory);
				complaintBean.addToHistory(complaintHistoryTransfer);

				transaction = session.beginTransaction();
				session.saveOrUpdate(complaintBean);
				transaction.commit();
				session.refresh(complaintBean);

				HibernateUtil.closeSession(factory, session);

				updateDashBoardFilterComplaint();

				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Complaint transferred successfully."));

			} else {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "No Complaint Found for the given complaint id."));
			}


		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to transfer your complaint."));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

		PrimeFaces.current().executeScript("PF('dlgTransfer').hide()");

	}


	public void searchByComplaintNumber() {
		try {
			consumerMobileNumber = "";
			if(StringUtils.isBlank(complaintNumber) || !GeneralUtil.validateNumber(complaintNumber)) {
				complaintList = new ArrayList<ViewComplaintValueBean>();
				FacesContext.getCurrentInstance().addMessage(null, 
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Warn", "Please enter valid complaint number."));
			} else {
				ComplaintsDao daoComplaints = new ComplaintsDao();
				complaintList = daoComplaints.getComplaintById(Integer.parseInt(complaintNumber));
				dm.setLstComplaintLog(new ArrayList<ComplaintLog>());
				if(complaintList.size() == 0) {
					FacesContext.getCurrentInstance().addMessage(null, 
							new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "No Complaint Found for the given complaint id."));
				}
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
	} 

	public void searchByConsumerMobileNumber() {
		try {
			complaintNumber = "";
			if(StringUtils.isBlank(consumerMobileNumber) ||  !GeneralUtil.validateMobileNumber(consumerMobileNumber)) {
				complaintList = new ArrayList<ViewComplaintValueBean>();
				FacesContext.getCurrentInstance().addMessage(null, 
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Warn", "Please enter valid consumer mobile number."));
			} else {
				ComplaintsDao daoComplaints = new ComplaintsDao();
				complaintList = daoComplaints.getComplaintByConsumerMobileNumber(consumerMobileNumber);
				dm.setLstComplaintLog(new ArrayList<ComplaintLog>());
				if(complaintList.size() == 0) {
					FacesContext.getCurrentInstance().addMessage(null, 
							new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "No Complaint Found for the given consumer mobile number."));
				}
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
	} 

	private String validateConsumerNo(String consumerNo) {

		String err = "";

		if(GeneralUtil.validateNumber(consumerNo)) {
			if (!consumerNo.startsWith("0")) {
				err = "Please enter region code.";
			}

			if (consumerNo.length() < 7) {
				err = "Not a valid consumer no.";
			}
		} else {
			err = "Not a valid consumer no.";
		}

		return err;
	}

	public void addMessage(FacesMessage message) {
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public void searchConsumer() {

		if (dmFilter.getListFieldWorker() != null) {
			dmFilter.getListFieldWorker().clear();
		}
		if (dmFilter.getFieldWorkerDropDown() != null) {
			dmFilter.setFieldWorkerDropDown(null);
		}

		String consumerNo = consumerServiceValueBean.getConsumerNumber();
		String err = validateConsumerNo(consumerNo);
		JSONObject json = null;
		if (err == "") {

			Network network = new Network();
			JSONParser par = new JSONParser();
			try {
				System.out.println("Find the consumer service acc by number:::::"+consumerNo);
				json = (JSONObject) par.parse(network.findConsumer(consumerNo));
				if(json != null && json.get("consumer_name") != null) {
					consumerServiceValueBean.setServiceNumber(consumerNo);
					consumerServiceValueBean.setServiceName(json.get("consumer_name").toString());
					consumerServiceValueBean.setServiceAddress(json.get("consumer_address").toString());
					consumerServiceValueBean.setServiceTariff(json.get("consumer_tariff").toString());
					dmFilter.setAddress(json.get("consumer_address").toString());
					this.dmFilter.setLandMark("");

					if(json.get("region_id") != null && json.get("region_id").toString().trim().length() > 0) {
						consumerServiceValueBean.setRegionId(Integer.valueOf(json.get("region_id").toString()));
					}

					if(json.get("circle_id") != null && json.get("circle_id").toString().trim().length() > 0) {
						consumerServiceValueBean.setCircleId(Integer.valueOf(json.get("circle_id").toString()));
					}

					if(json.get("division_id") != null && json.get("division_id").toString().trim().length() > 0) {
						consumerServiceValueBean.setDivisionId(Integer.valueOf(json.get("division_id").toString()));
					}

					if(json.get("sub_division_id") != null && json.get("sub_division_id").toString().trim().length() > 0) {
						consumerServiceValueBean.setSubDivisionId(Integer.valueOf(json.get("sub_division_id").toString()));
					}

					if(json.get("section_id") != null && json.get("section_id").toString().trim().length() > 0) {
						consumerServiceValueBean.setSectionId(Integer.valueOf(json.get("section_id").toString()));
						loadFieldWorkerBySection(Integer.valueOf(json.get("section_id").toString()));
					}

				} else {
					consumerServiceValueBean.setServiceName("");
					consumerServiceValueBean.setServiceAddress("");
					consumerServiceValueBean.setServiceTariff("");
					addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Not a valid consumer no."));
				}

			} catch (Exception e) {
				addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
				logger.error(ExceptionUtils.getStackTrace(e));
			}

		} else {
			addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", err));
		}

	}

	public void searchConsumer_SocialMedia() {

		if (dmFilter.getListFieldWorker() != null) {
			dmFilter.getListFieldWorker().clear();
		}
		if (dmFilter.getFieldWorkerDropDown() != null) {
			dmFilter.setFieldWorkerDropDown(null);
		}

		String consumerNo = consumerServiceValueBean.getConsumerNumber();
		String err = validateConsumerNo(consumerNo);
		JSONObject json = null;
		if (err == "") {

			Network network = new Network();
			JSONParser par = new JSONParser();
			
			try {
				System.out.println("Find the consumer service acc by number:::::"+consumerNo);
				json = (JSONObject) par.parse(network.findConsumer(consumerNo));
				if(json != null && json.get("consumer_name") != null) {
					consumerServiceValueBean.setServiceNumber(consumerNo);
					consumerServiceValueBean.setServiceName(json.get("consumer_name").toString());
					consumerServiceValueBean.setServiceAddress(json.get("consumer_address").toString());
					consumerServiceValueBean.setServiceTariff(json.get("consumer_tariff").toString());
					dmFilter.setAddress(json.get("consumer_address").toString());
					this.dmFilter.setLandMark("");

					if(json.get("region_id") != null && json.get("region_id").toString().trim().length() > 0) {
						consumerServiceValueBean.setRegionId(Integer.valueOf(json.get("region_id").toString()));
					}

					if(json.get("circle_id") != null && json.get("circle_id").toString().trim().length() > 0) {
						consumerServiceValueBean.setCircleId(Integer.valueOf(json.get("circle_id").toString()));
					}

					if(json.get("division_id") != null && json.get("division_id").toString().trim().length() > 0) {
						consumerServiceValueBean.setDivisionId(Integer.valueOf(json.get("division_id").toString()));
					}

					if(json.get("sub_division_id") != null && json.get("sub_division_id").toString().trim().length() > 0) {
						consumerServiceValueBean.setSubDivisionId(Integer.valueOf(json.get("sub_division_id").toString()));
					}

					if(json.get("section_id") != null && json.get("section_id").toString().trim().length() > 0) {
						consumerServiceValueBean.setSectionId(Integer.valueOf(json.get("section_id").toString()));
						loadSectionsById(consumerServiceValueBean.getSectionId());
					}
					
					
					ComplaintsDao complaintDao = new ComplaintsDao();
					
					List<String[]> row = complaintDao.getDistrictSection(consumerNo);
					String sectionName = null;
					String distictName = null;
					
					for(String[] s:row) {
						sectionName = (String) s[0];
						distictName = (String) s[1];
					}
					
					List<DistrictValueBean> listDistrict 		= new ArrayList<>();
					List<DistrictValueBean> listDistrictSection = new ArrayList<>();
					
					
					DistrictValueBean districtValueBean = new DistrictValueBean();
				    
					   districtValueBean.setName(distictName);
					   listDistrict.add(districtValueBean);
					   
					   DistrictValueBean section = new DistrictValueBean();
					   
					   section.setSectionName(sectionName);
					   listDistrictSection.add(section);
										
					dmFilter.setSmListDistrict(listDistrict);
					
				} else {
					consumerServiceValueBean.setServiceName("");
					consumerServiceValueBean.setServiceAddress("");
					consumerServiceValueBean.setServiceTariff("");
					addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Not a valid consumer no."));
				}

			} catch (Exception e) {
				addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
				logger.error(ExceptionUtils.getStackTrace(e));
			}

		} else {
			addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", err));
		}

	}
	
	public void loadSectionsById (Integer sectionId) {
		 SessionFactory factory = null;
		    Session session = null;
		    try {
		        if (sectionId != null) {  
		            factory = HibernateUtil.getSessionFactory();
		            session = factory.openSession();

		            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		            CriteriaQuery<SectionBean> criteriaQuery = criteriaBuilder.createQuery(SectionBean.class);
		            Root<SectionBean> root = criteriaQuery.from(SectionBean.class);

		            criteriaQuery.select(root)
		                .where(criteriaBuilder.equal(root.get("id"), sectionId))
		                .orderBy(criteriaBuilder.asc(root.get("name"))); 

		            Query<SectionBean> query = session.createQuery(criteriaQuery);
		            List<SectionBean> resultList = query.getResultList();

		            List<SectionValueBean> sectionList = new ArrayList<>();
		            for (SectionBean sectionBean : resultList) {
		                sectionList.add(SectionValueBean.convertSectionBeanToSectionValueBean(sectionBean)); 
		            }

		            dmFilter.setLstSections(sectionList);  
		        }
		    } catch (Exception e) {
		        logger.error(ExceptionUtils.getStackTrace(e));
		        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		    } finally {
		        HibernateUtil.closeSession(factory, session);
		    }
	}

	public void createComplaint() {

		try {
			if(!GeneralUtil.validateMobileNumber(consumerServiceValueBean.getMobile())){
			
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warn", "Invalid mobile no."));
				
			} else {	
				
				HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
				CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession
						.getAttribute("sessionCallCenterUserValueBean");
				
				UserDao userDao = new UserDao();
				PublicUserValueBean publicUserValueBean = userDao.getConsumerByMobile(consumerServiceValueBean.getMobile());

				if(publicUserValueBean == null) { 
					PublicUserBean user = new PublicUserBean();
					user.setMobile(consumerServiceValueBean.getMobile());
					if(StringUtils.isNotBlank(dmFilter.getAddress())) {
						user.setAddress(dmFilter.getAddress());
					}
					if(StringUtils.isNotBlank(dmFilter.getLandMark())) {
						user.setLandMark(dmFilter.getLandMark());
					}
					userDao.registerUser(user);
					publicUserValueBean = userDao.getConsumerByMobile(consumerServiceValueBean.getMobile());
				} else {
					if(StringUtils.isNotBlank(dmFilter.getAddress())) {
						publicUserValueBean.setAddress(dmFilter.getAddress());
					}
					if(StringUtils.isNotBlank(dmFilter.getLandMark())) {
						publicUserValueBean.setLandMark(dmFilter.getLandMark());
					}
					userDao.updateUser(publicUserValueBean);
				}

				Integer compCode = Integer.parseInt(dmFilter.getComplaintType());

				ComplaintsDao complaintsDao = new ComplaintsDao();

				// fire, dangerous_poles, theft, conductor_snapping
				if((compCode == 4 || compCode == 5 || compCode == 6 || compCode == 8)&&(StringUtils.isBlank(consumerServiceValueBean.getServiceNumber()))) {

					DistrictValueBean districtValueBean = complaintsDao.getRegionIdByDistrictId(Integer.parseInt(dmFilter.getSectionDropDown()));
					SectionValueBean sectionValueBean = complaintsDao.getSectionByCodeAndRegionId(districtValueBean.getSectionCode(), districtValueBean.getRegionId());

					Integer complaintId = complaintsDao.registerGISComplaint(sectionValueBean, dmFilter, publicUserValueBean, false, callCenterValueBean.getUserName());

					clearComplaintOnSuccess(complaintId);

				} 
				else if((compCode == 7) && (StringUtils.isBlank(consumerServiceValueBean.getServiceNumber()))){


					DistrictValueBean districtValueBean = complaintsDao.getRegionIdByDistrictId(Integer.parseInt(dmFilter.getSectionDropDown()));
					SectionValueBean sectionValueBean = complaintsDao.getSectionByCodeAndRegionId(districtValueBean.getSectionCode(), districtValueBean.getRegionId());
					ComplaintValueBean complaintValueBean = complaintsDao.getComplaintByComplaintType(CCMSConstants.COMPLAINT_TYPE_CODE[7], publicUserValueBean.getId(), sectionValueBean.getId());

					Integer complaintId = complaintsDao.registerPowerComplaint(sectionValueBean, dmFilter, publicUserValueBean,serviceApplicationNo, false ,callCenterValueBean.getUserName());

					clearComplaintOnSuccess(complaintId);

					if(complaintValueBean != null) {
						addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO,
								"Info", "Complaint already registered " + complaintValueBean.getId()));
						return;
					}
				}

				else {

					if(StringUtils.isNotBlank(consumerServiceValueBean.getServiceNumber())) {

						ComplaintValueBean complaintValueBean = complaintsDao.getComplaintByServiceNumberAndComplaintType(consumerServiceValueBean.getServiceNumber(),
								CCMSConstants.COMPLAINT_TYPE_CODE[compCode], publicUserValueBean.getId());

						if(complaintValueBean != null) {
							addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO,
									"Info", "Complaint already registered " + complaintValueBean.getId()));
							return;
						}

						//If power failure we do not allow to create voltage fluctuation and vice versa
						if(compCode == 0 || compCode == 1) {

							if(compCode == 0) {

								complaintValueBean = complaintsDao.getComplaintByServiceNumberAndComplaintType(consumerServiceValueBean.getServiceNumber(),
										CCMSConstants.COMPLAINT_TYPE_CODE[1], publicUserValueBean.getId());

								if(complaintValueBean != null) {
									addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO,
											"Info", "Complaint already registered " + complaintValueBean.getId()));
									return;
								}
							} else {
								complaintValueBean = complaintsDao.getComplaintByServiceNumberAndComplaintType(consumerServiceValueBean.getServiceNumber(),
										CCMSConstants.COMPLAINT_TYPE_CODE[0], publicUserValueBean.getId());

								if(complaintValueBean != null) {
									addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO,
											"Info", "Complaint already registered " + complaintValueBean.getId()));
									return;
								}
							}

							Integer complaintId = complaintsDao.registerPowerComplaint(consumerServiceValueBean, dmFilter, publicUserValueBean, false,callCenterValueBean.getUserName());
							clearComplaintOnSuccess(complaintId);
						} else {
							Integer complaintId = complaintsDao.registerMeterAndBillComplaint(consumerServiceValueBean, dmFilter, publicUserValueBean, false, callCenterValueBean.getUserName());
							clearComplaintOnSuccess(complaintId);
						}
					} else {

						DistrictValueBean districtValueBean = complaintsDao.getRegionIdByDistrictId(Integer.parseInt(dmFilter.getSectionDropDown()));
						SectionValueBean sectionValueBean = complaintsDao.getSectionByCodeAndRegionId(districtValueBean.getSectionCode(), districtValueBean.getRegionId());
						
						ComplaintValueBean complaintValueBean = complaintsDao.getComplaintByComplaintType(CCMSConstants.COMPLAINT_TYPE_CODE[compCode], 
								publicUserValueBean.getId(), sectionValueBean.getId());

						if(complaintValueBean != null) {
							addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO,
									"Info", "Complaint already registered " + complaintValueBean.getId()));
							return;
						}

						//If power failure we do not allow to create voltage fluctuation and vice versa
						if(compCode == 0 || compCode == 1) {

							if(compCode == 0) {

								complaintValueBean = complaintsDao.getComplaintByComplaintType(CCMSConstants.COMPLAINT_TYPE_CODE[1], 
										publicUserValueBean.getId(), sectionValueBean.getId());

								if(complaintValueBean != null) {
									addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO,
											"Info", "Complaint already registered " + complaintValueBean.getId()));
									return;
								}
							} else {
								complaintValueBean = complaintsDao.getComplaintByComplaintType(CCMSConstants.COMPLAINT_TYPE_CODE[0], 
										publicUserValueBean.getId(), sectionValueBean.getId());

								if(complaintValueBean != null) {
									addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO,
											"Info", "Complaint already registered " + complaintValueBean.getId()));
									return;
								}
							}

							Integer complaintId = complaintsDao.registerPowerComplaint(sectionValueBean, dmFilter, publicUserValueBean,serviceApplicationNo, false,callCenterValueBean.getUserName());
							clearComplaintOnSuccess(complaintId);
						} else {
							Integer complaintId = complaintsDao.registerMeterAndBillComplaint(sectionValueBean, dmFilter, publicUserValueBean, false,callCenterValueBean.getUserName());
							clearComplaintOnSuccess(complaintId);
						}

					}
				}
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
	}

	public void createComplaint_SocialMedia() {

		try {
			
			String mobile_Number = consumerServiceValueBean.getMobile();
			
			if( mobile_Number != "" && !GeneralUtil.validateMobileNumber(consumerServiceValueBean.getMobile())){
			
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warn", "Invalid mobile no."));
				
			} else {		
				UserDao userDao = new UserDao();
				PublicUserValueBean publicUserValueBean = userDao.getConsumerByMobile(mobile_Number);

				if(publicUserValueBean == null) { 

					PublicUserBean user = new PublicUserBean();
					
					user.setMobile(mobile_Number);
					
					if(StringUtils.isNotBlank(dmFilter.getAddress())) {
						user.setAddress(dmFilter.getAddress());
					}
					if(StringUtils.isNotBlank(dmFilter.getLandMark())) {
						user.setLandMark(dmFilter.getLandMark());
					}
					userDao.registerUser(user);
					publicUserValueBean = PublicUserValueBean.convertPublicUserBeanToPublicUserValueBean(user);
					
				} else {
					if(StringUtils.isNotBlank(dmFilter.getAddress())) {
						publicUserValueBean.setAddress(dmFilter.getAddress());
					}
					if(StringUtils.isNotBlank(dmFilter.getLandMark())) {
						publicUserValueBean.setLandMark(dmFilter.getLandMark());
					}
					userDao.updateUser(publicUserValueBean);
				}
				

				Integer compCode = Integer.parseInt(dmFilter.getComplaintType());

				ComplaintsDao complaintsDao = new ComplaintsDao();

				// fire, dangerous_poles, theft, conductor_snapping
				if(compCode == 4 || compCode == 5 || compCode == 6 || compCode == 8) {

					DistrictValueBean districtValueBean = complaintsDao.getRegionIdByDistrictId(Integer.parseInt(dmFilter.getSmSectionDropdown()));
					SectionValueBean sectionValueBean = complaintsDao.getSectionByCodeAndRegionId(districtValueBean.getSectionCode(), districtValueBean.getRegionId());

					Integer complaintId = complaintsDao.registerGISComplaint(sectionValueBean, dmFilter, publicUserValueBean, true,null);

					clearComplaintOnSuccess(complaintId);

				} 
				else if(compCode == 7){


					DistrictValueBean districtValueBean = complaintsDao.getRegionIdByDistrictId(Integer.parseInt(dmFilter.getSmSectionDropdown()));
					SectionValueBean sectionValueBean = complaintsDao.getSectionByCodeAndRegionId(districtValueBean.getSectionCode(), districtValueBean.getRegionId());
					ComplaintValueBean complaintValueBean = complaintsDao.getComplaintByComplaintType(CCMSConstants.COMPLAINT_TYPE_CODE[7], publicUserValueBean.getId(), sectionValueBean.getId());

					Integer complaintId = complaintsDao.registerPowerComplaint(sectionValueBean, dmFilter, publicUserValueBean,serviceApplicationNo,true,null);

					clearComplaintOnSuccess(complaintId);

					if(complaintValueBean != null) {
						addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO,
								"Info", "Complaint already registered " + complaintValueBean.getId()));
						return;
					}
				}

				else {

					if(StringUtils.isNotBlank(consumerServiceValueBean.getServiceNumber())) {

						ComplaintValueBean complaintValueBean = complaintsDao.getComplaintByServiceNumberAndComplaintType(consumerServiceValueBean.getServiceNumber(),
								CCMSConstants.COMPLAINT_TYPE_CODE[compCode], publicUserValueBean.getId());

						if(complaintValueBean != null) {
							addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO,
									"Info", "Complaint already registered " + complaintValueBean.getId()));
							return;
						}

						//If power failure we do not allow to create voltage fluctuation and vice versa
						if(compCode == 0 || compCode == 1) {

							if(compCode == 0) {

								complaintValueBean = complaintsDao.getComplaintByServiceNumberAndComplaintType(consumerServiceValueBean.getServiceNumber(),
										CCMSConstants.COMPLAINT_TYPE_CODE[1], publicUserValueBean.getId());

								if(complaintValueBean != null) {
									addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO,
											"Info", "Complaint already registered " + complaintValueBean.getId()));
									return;
								}
							} else {
								complaintValueBean = complaintsDao.getComplaintByServiceNumberAndComplaintType(consumerServiceValueBean.getServiceNumber(),
										CCMSConstants.COMPLAINT_TYPE_CODE[0], publicUserValueBean.getId());

								if(complaintValueBean != null) {
									addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO,
											"Info", "Complaint already registered " + complaintValueBean.getId()));
									return;
								}
							}

							Integer complaintId = complaintsDao.registerPowerComplaint(consumerServiceValueBean, dmFilter, publicUserValueBean,true,null);
							clearComplaintOnSuccess(complaintId);
						} else {
							Integer complaintId = complaintsDao.registerMeterAndBillComplaint(consumerServiceValueBean, dmFilter, publicUserValueBean,true,null);
							clearComplaintOnSuccess(complaintId);
						}
					} else {

						DistrictValueBean districtValueBean = complaintsDao.getRegionIdByDistrictId(Integer.parseInt(dmFilter.getSmSectionDropdown()));
						SectionValueBean sectionValueBean = complaintsDao.getSectionByCodeAndRegionId(districtValueBean.getSectionCode(), districtValueBean.getRegionId());
						
						ComplaintValueBean complaintValueBean = complaintsDao.getComplaintByComplaintType(CCMSConstants.COMPLAINT_TYPE_CODE[compCode], 
								publicUserValueBean.getId(), sectionValueBean.getId());

						if(complaintValueBean != null) {
							addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO,
									"Info", "Complaint already registered " + complaintValueBean.getId()));
							return;
						}

						//If power failure we do not allow to create voltage fluctuation and vice versa
						if(compCode == 0 || compCode == 1) {

							if(compCode == 0) {

								complaintValueBean = complaintsDao.getComplaintByComplaintType(CCMSConstants.COMPLAINT_TYPE_CODE[1], 
										publicUserValueBean.getId(), sectionValueBean.getId());

								if(complaintValueBean != null) {
									addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO,
											"Info", "Complaint already registered " + complaintValueBean.getId()));
									return;
								}
							} else {
								complaintValueBean = complaintsDao.getComplaintByComplaintType(CCMSConstants.COMPLAINT_TYPE_CODE[0], 
										publicUserValueBean.getId(), sectionValueBean.getId());

								if(complaintValueBean != null) {
									addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO,
											"Info", "Complaint already registered " + complaintValueBean.getId()));
									return;
								}
							}

							Integer complaintId = complaintsDao.registerPowerComplaint(sectionValueBean, dmFilter, publicUserValueBean,serviceApplicationNo, true,null);
							clearComplaintOnSuccess(complaintId);
						} else {
							Integer complaintId = complaintsDao.registerMeterAndBillComplaint(sectionValueBean, dmFilter, publicUserValueBean, true,null);
							clearComplaintOnSuccess(complaintId);
						}

					}
				}
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
	}

	private void clearComplaintOnSuccess(Integer complaintId) {
		consumerServiceValueBean.setMobile("");
		consumerServiceValueBean.setConsumerNumber("");
		consumerServiceValueBean.setServiceName("");
		consumerServiceValueBean.setServiceAddress("");
		consumerServiceValueBean.setServiceTariff("");
		consumerServiceValueBean.setServiceNumber("");
		dmFilter.setComplaintType("");
		dmFilter.setSubCategory("");
		dmFilter.setFieldWorkerDropDown("");
		dmFilter.setComplaintDescription("");
		dmFilter.setDistrictDropDown("");
		dmFilter.setSectionDropDown("");
		dmFilter.setLandMark("");
		dmFilter.setAddress("");
		dmFilter.setTag("");
		dmFilter.setReceivedFrom("");
		dmFilter.setAlternateMobileNo("");
		addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Complaint Registered "+complaintId));
	}

	public ViewComplaintValueBean getComplaint() {
		return complaint;
	}

	public void setComplaint(ViewComplaintValueBean complaint) {
		this.complaint = complaint;
	}

	public LoginParams getOfficer() {
		return officer;
	}

	public void setOfficer(LoginParams officer) {
		this.officer = officer;
	}

	public List<ViewComplaintValueBean> getViewComplaintList() {
		return viewComplaintList;
	}

	public void setViewComplaintList(List<ViewComplaintValueBean> viewComplaintList) {
		this.viewComplaintList = viewComplaintList;
	}

	public List<ViewComplaintValueBean> getComplaintList() {
		return complaintList;
	}

	public void setComplaintList(List<ViewComplaintValueBean> complaintList) {
		this.complaintList = complaintList;
	}

	public DataModel getDm() {
		return dm;
	}

	public void setDm(DataModel dm) {
		this.dm = dm;
	}

	public int getRegionId() {
		return regionId;
	}

	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescriptionTransfer() {
		return descriptionTransfer;
	}

	public void setDescriptionTransfer(String descriptionTransfer) {
		this.descriptionTransfer = descriptionTransfer;
	}

	public String getDescriptionComplete() {
		return descriptionComplete;
	}

	public void setDescriptionComplete(String descriptionComplete) {
		this.descriptionComplete = descriptionComplete;
	}

	public Map getMap() {
		return map;
	}

	public void setMap(Map map) {
		this.map = map;
	}

	public DataModel getDmFilter() {
		return dmFilter;
	}

	public void setDmFilter(DataModel dmFilter) {
		this.dmFilter = dmFilter;
	}


	public int getComplaintCode() {
		return complaintCode;
	}

	public void setComplaintCode(int complaintCode) {
		this.complaintCode = complaintCode;
	}

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

	public String getComplaintNumber() {
		return complaintNumber;
	}

	public void setComplaintNumber(String complaintNumber) {
		this.complaintNumber = complaintNumber;
	}

	public String getConsumerMobileNumber() {
		return consumerMobileNumber;
	}

	public void setConsumerMobileNumber(String consumerMobileNumber) {
		this.consumerMobileNumber = consumerMobileNumber;
	}

	public ConsumerServiceValueBean getConsumerServiceValueBean() {
		return consumerServiceValueBean;
	}

	public void setConsumerServiceValueBean(ConsumerServiceValueBean consumerServiceValueBean) {
		this.consumerServiceValueBean = consumerServiceValueBean;
	}

	public List<ViewComplaintValueBean> getViewConsumerComplaintList() {
		return viewConsumerComplaintList;
	}

	public void setViewConsumerComplaintList(List<ViewComplaintValueBean> viewConsumerComplaintList) {
		this.viewConsumerComplaintList = viewConsumerComplaintList;
	}

	public String getServiceApplicationNo() {
		return serviceApplicationNo;
	}

	public void setServiceApplicationNo(String serviceApplicationNo) {
		this.serviceApplicationNo = serviceApplicationNo;
	}

	public DistrictValueBean getDistrictValueBean() {
		return districtValueBean;
	}

	public void setDistrictValueBean(DistrictValueBean districtValueBean) {
		this.districtValueBean = districtValueBean;
	}
	
	

}

