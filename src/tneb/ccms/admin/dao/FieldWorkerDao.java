package tneb.ccms.admin.dao;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tneb.ccms.admin.model.DistrictBean;
import tneb.ccms.admin.model.FieldWorkerBean;
import tneb.ccms.admin.model.SectionBean;
import tneb.ccms.admin.util.CCMSConstants;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.valuebeans.FieldWorkerValueBean;

public class FieldWorkerDao {
	
	private Logger logger = LoggerFactory.getLogger(FieldWorkerDao.class);
	
		public List<FieldWorkerValueBean> getFieldWorkersList(DataModel dataModel) throws Exception {
			
			List<FieldWorkerValueBean> fieldWorkerList = new ArrayList<FieldWorkerValueBean>();
			FieldWorkerValueBean fieldWorkerValueBean = null;
			SessionFactory factory = null;
			Session session = null;
			try {
				
				factory = HibernateUtil.getSessionFactory();
				session = factory.openSession();
				
				CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<DistrictBean> criteriaQuery = criteriaBuilder.createQuery(DistrictBean.class);
				Root<DistrictBean> root = criteriaQuery.from(DistrictBean.class);
				
				criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), dataModel.getSectionDropDown()));
				
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
				
				CriteriaBuilder criteriaBuilderFieldWorker = session.getCriteriaBuilder();
				CriteriaQuery<Object[]> criteriaQueryFieldWorker = criteriaBuilderFieldWorker.createQuery(Object[].class);
				Root<FieldWorkerBean> rootFieldWorker = criteriaQueryFieldWorker.from(FieldWorkerBean.class);
				
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(criteriaBuilderFieldWorker.equal(rootFieldWorker.get("sectionBean").get("id"), sectionBean.getId()));
				
				//Constructing order list of parameters
				List<Order> orderList = new ArrayList<Order>();
				orderList.add(criteriaBuilderFieldWorker.asc(rootFieldWorker.get("name")));
				
				criteriaQueryFieldWorker.multiselect(rootFieldWorker.get("name"),rootFieldWorker.get("mobile"),rootFieldWorker.get("staffCode"),rootFieldWorker.get("staffRole"),rootFieldWorker.get("id"))
				.where(predicates.toArray(new Predicate[]{})).orderBy(orderList);
	
				Query<Object[]> queryFieldWorker = session.createQuery(criteriaQueryFieldWorker);
				
				List<?> rows = queryFieldWorker.list();
				if (rows.size() > 0) {
					Object[] row = null;
					for (int counter = 0; counter < rows.size(); counter++) {
						row = (Object[]) rows.get(counter);
						fieldWorkerValueBean = new FieldWorkerValueBean();
						fieldWorkerValueBean.setName(row[0].toString());
						fieldWorkerValueBean.setMobile(row[1].toString());
						if(row[2] != null){
							fieldWorkerValueBean.setStaffCode(row[2].toString());
						}
						if(row[3] != null){
							fieldWorkerValueBean.setStaffRole(row[3].toString());
						}
						fieldWorkerValueBean.setId(Integer.parseInt(row[4].toString()));
						fieldWorkerList.add(fieldWorkerValueBean);
					}

				}
				
			} catch (Exception e) {
				logger.error(ExceptionUtils.getStackTrace(e));
				throw e;
			} finally {
				HibernateUtil.closeSession(factory, session);
			}
	
			return fieldWorkerList;
		}
		
		public void updateFieldUser(FieldWorkerValueBean fieldWorkerValueBean) throws Exception {
			SessionFactory factory = null;
			Session session = null;
			Transaction transaction = null;
			try {
				factory = HibernateUtil.getSessionFactory();
				session = factory.openSession();
				
				CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<FieldWorkerBean> criteriaQuery = criteriaBuilder.createQuery(FieldWorkerBean.class);
				Root<FieldWorkerBean> root = criteriaQuery.from(FieldWorkerBean.class);
				criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), fieldWorkerValueBean.getId()));
	
				Query<FieldWorkerBean> query = session.createQuery(criteriaQuery).setMaxResults(1);
				FieldWorkerBean fieldWorkers = null;
				try {
					fieldWorkers = query.getSingleResult();
					
					fieldWorkers.setName(fieldWorkerValueBean.getName());
					fieldWorkers.setMobile(fieldWorkerValueBean.getMobile());
					fieldWorkers.setStaffCode(fieldWorkerValueBean.getStaffCode());
					fieldWorkers.setStaffRole(fieldWorkerValueBean.getStaffRole());
					
					
					transaction = session.beginTransaction();
					session.update(fieldWorkers);
					transaction.commit();
					
				} catch (NoResultException nre) {
					throw new Exception ("No Field User Found for the given credentials.");
				}
			} catch (Exception e) {
				if(transaction != null) {
					transaction.rollback();
				}
				logger.error(ExceptionUtils.getStackTrace(e));
				throw e;
			} finally {
				HibernateUtil.closeSession(factory, session);
			}
		}
		
		public FieldWorkerValueBean getFieldUser(int id) throws Exception {
			FieldWorkerBean fieldWorkerBean = null;
			FieldWorkerValueBean fieldWorkerValueBean = null;
			SessionFactory factory = null;
			Session session = null;
			try {
				
				factory = HibernateUtil.getSessionFactory();
				session = factory.openSession();
				
				CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<FieldWorkerBean> criteriaQuery = criteriaBuilder.createQuery(FieldWorkerBean.class);
				Root<FieldWorkerBean> root = criteriaQuery.from(FieldWorkerBean.class);
				criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), id));

				Query<FieldWorkerBean> query = session.createQuery(criteriaQuery).setMaxResults(1);
				try {
					fieldWorkerBean = query.getSingleResult();
					fieldWorkerValueBean = FieldWorkerValueBean.convertBeanToValueBean(fieldWorkerBean);
					
				} catch (NoResultException nre) {
					throw new Exception ("No User Found for the given id.");
				}
			} catch (Exception e) {
				logger.error(ExceptionUtils.getStackTrace(e));
				throw e;
			} finally {
				HibernateUtil.closeSession(factory, session);
			}
			return fieldWorkerValueBean;
		}
		
		public void addFieldWorker(FieldWorkerValueBean fieldWorkerValueBean, DataModel dataModel) throws Exception {

			SessionFactory factory = null;
			Session session = null;
			Transaction transaction = null;
			try {
				factory = HibernateUtil.getSessionFactory();
				session = factory.openSession();
				
				FieldWorkerBean fieldWorker = new FieldWorkerBean();
				
				SectionBean sectionBean =  session.load(SectionBean.class, Integer.parseInt(dataModel.getSectionDropDown()));
				
				fieldWorker.setSectionBean(sectionBean);
				fieldWorker.setRegionBean(sectionBean.getRegionBean());
				
				fieldWorker.setMobile(fieldWorkerValueBean.getMobile());
				fieldWorker.setName(fieldWorkerValueBean.getName());
				fieldWorker.setStaffCode(fieldWorkerValueBean.getStaffCode());
				fieldWorker.setStaffRole(fieldWorkerValueBean.getStaffRole());
				

				transaction = session.beginTransaction();
				session.save(fieldWorker);
				
				transaction.commit();
				
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info","Field User created successfully."));
				
				
			} catch (Exception e) {
				if(transaction != null) {
					transaction.rollback();
				}
				logger.error(ExceptionUtils.getStackTrace(e));
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
			} finally {
				HibernateUtil.closeSession(factory, session);
			}
		
		}
		
		
		public String validateFieldUser(String mobile, String sectionId) throws Exception {
			
			String errorMessage = null;
			
			SessionFactory factory = null;
			Session session = null;
			try {
				
				factory = HibernateUtil.getSessionFactory();
				session = factory.openSession();
				
				CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<FieldWorkerBean> criteriaQuery = criteriaBuilder.createQuery(FieldWorkerBean.class);
				Root<FieldWorkerBean> root = criteriaQuery.from(FieldWorkerBean.class);
				criteriaQuery.select(root).where(criteriaBuilder.and(criteriaBuilder.equal(root.get("mobile"), mobile),
						criteriaBuilder.equal(root.get("sectionBean").get("id"), sectionId)));

				Query<FieldWorkerBean> query = session.createQuery(criteriaQuery);
				List<FieldWorkerBean> userList = query.getResultList();

				if (userList.size() > 0) {
					errorMessage = "Field User already registered with this Mobile No.";
				}
			} catch (Exception e) {
				logger.error(ExceptionUtils.getStackTrace(e));
				throw e;
			} finally {
				HibernateUtil.closeSession(factory, session);
			}
			
			return errorMessage;
		}
		
		
		public String validateFieldUserOnUpdate(FieldWorkerValueBean fieldWorkerValueBean, String sectionId) throws Exception {
			
			String errorMessage = null;
			
			SessionFactory factory = null;
			Session session = null;
			try {
				
				factory = HibernateUtil.getSessionFactory();
				session = factory.openSession();
				
				CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
				CriteriaQuery<FieldWorkerBean> criteriaQuery = criteriaBuilder.createQuery(FieldWorkerBean.class);
				Root<FieldWorkerBean> root = criteriaQuery.from(FieldWorkerBean.class);
				criteriaQuery.select(root).where(criteriaBuilder.and(criteriaBuilder.equal(root.get("mobile"), fieldWorkerValueBean.getMobile()),
						criteriaBuilder.notEqual(root.get("id"), fieldWorkerValueBean.getId()),
						criteriaBuilder.equal(root.get("sectionBean").get("id"), sectionId)));

				Query<FieldWorkerBean> query = session.createQuery(criteriaQuery);
				List<FieldWorkerBean> userList = query.getResultList();

				if (userList.size() > 0) {
					errorMessage = "Field User already registered with this Mobile No.";
				}
			} catch (Exception e) {
				logger.error(ExceptionUtils.getStackTrace(e));
				throw e;
			} finally {
				HibernateUtil.closeSession(factory, session);
			}
			
			return errorMessage;
		}

}
