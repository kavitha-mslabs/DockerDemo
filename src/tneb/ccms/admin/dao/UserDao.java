package tneb.ccms.admin.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

import tneb.ccms.admin.model.AdminUserBean;
import tneb.ccms.admin.model.PublicUserBean;
import tneb.ccms.admin.model.RoleBean;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.valuebeans.AdminUserValueBean;
import tneb.ccms.admin.valuebeans.PublicUserValueBean;


public class UserDao {
	
	private Logger logger = LoggerFactory.getLogger(UserDao.class.getName());

	public AdminUserValueBean getUser(int id) throws Exception {
		AdminUserBean adminUsers = null;
		AdminUserValueBean adminUserValueBean = null;
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<AdminUserBean> criteriaQuery = criteriaBuilder.createQuery(AdminUserBean.class);
			Root<AdminUserBean> root = criteriaQuery.from(AdminUserBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), id));

			Query<AdminUserBean> query = session.createQuery(criteriaQuery).setMaxResults(1);
			try {
				adminUsers = query.getSingleResult();
				
				adminUserValueBean = AdminUserValueBean.convertAdminUserBeanToAdminUserValueBean(adminUsers);
				
			} catch (NoResultException nre) {
				throw new Exception ("No User Found for the given id.");
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return adminUserValueBean;
	}

	public AdminUserBean getUserbyMobile(String mobileNumber) throws Exception {

		AdminUserBean adminUsers = null;
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<AdminUserBean> criteriaQuery = criteriaBuilder.createQuery(AdminUserBean.class);
			Root<AdminUserBean> root = criteriaQuery.from(AdminUserBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("mobile"), mobileNumber));

			Query<AdminUserBean> query = session.createQuery(criteriaQuery).setMaxResults(1);
			try {
				adminUsers = query.getSingleResult();
			} catch (NoResultException nre) {
				System.out.println("No User Found for the given credentials.");
				logger.info("No User Found for the given credentials.");
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		
		return adminUsers;

	}

	public boolean validateUser(String mobileNumber) throws Exception {
		
		boolean validUser = false;
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<AdminUserBean> criteriaQuery = criteriaBuilder.createQuery(AdminUserBean.class);
			Root<AdminUserBean> root = criteriaQuery.from(AdminUserBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("mobile"), mobileNumber));

			Query<AdminUserBean> query = session.createQuery(criteriaQuery);
			List<AdminUserBean> userList = query.getResultList();

			if (userList.size() > 0) {
				validUser = true;
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

		return validUser;

	}

	public void registerUser(AdminUserBean user) throws Exception {
		SessionFactory factory = null;
		Session session = null;
		Transaction transaction = null;
		try {
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			transaction = session.beginTransaction();
			session.save(user);
			transaction.commit();
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
	
	public void updateUser(AdminUserValueBean adminUserValueBean) throws Exception {
		SessionFactory factory = null;
		Session session = null;
		Transaction transaction = null;
		try {
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<AdminUserBean> criteriaQuery = criteriaBuilder.createQuery(AdminUserBean.class);
			Root<AdminUserBean> root = criteriaQuery.from(AdminUserBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), adminUserValueBean.getId()));

			Query<AdminUserBean> query = session.createQuery(criteriaQuery).setMaxResults(1);
			AdminUserBean adminUsers = null;
			try {
				adminUsers = query.getSingleResult();
				
				adminUsers.setUserName(adminUserValueBean.getUserName());
				adminUsers.setMobile(adminUserValueBean.getMobile());
				
				RoleBean roleBean = session.load(RoleBean.class, adminUserValueBean.getRoleId());
				adminUsers.setRoleBean(roleBean);
				
				transaction = session.beginTransaction();
				session.update(adminUsers);
				transaction.commit();
				
			} catch (NoResultException nre) {
				throw new Exception ("No User Found for the given credentials.");
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
	
	public void updateUserPassword(AdminUserValueBean adminUserValueBean) throws Exception {
		SessionFactory factory = null;
		Session session = null;
		Transaction transaction = null;
		try {
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<AdminUserBean> criteriaQuery = criteriaBuilder.createQuery(AdminUserBean.class);
			Root<AdminUserBean> root = criteriaQuery.from(AdminUserBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), adminUserValueBean.getId()));

			Query<AdminUserBean> query = session.createQuery(criteriaQuery).setMaxResults(1);
			AdminUserBean adminUsers = null;
			try {
				adminUsers = query.getSingleResult();
				
				adminUsers.setPassword(adminUserValueBean.getPassword());
				
				transaction = session.beginTransaction();
				session.update(adminUsers);
				transaction.commit();
				
			} catch (NoResultException nre) {
				throw new Exception ("No User Found.");
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
	
	public void deleteUser(AdminUserValueBean adminUserValueBean) throws Exception {
		SessionFactory factory = null;
		Session session = null;
		Transaction transaction = null;
		try {
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<AdminUserBean> criteriaQuery = criteriaBuilder.createQuery(AdminUserBean.class);
			Root<AdminUserBean> root = criteriaQuery.from(AdminUserBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), adminUserValueBean.getId()));

			Query<AdminUserBean> query = session.createQuery(criteriaQuery).setMaxResults(1);
			AdminUserBean adminUsers = null;
			try {
				adminUsers = query.getSingleResult();
				
				transaction = session.beginTransaction();
				session.delete(adminUsers);
				transaction.commit();
				
			} catch (NoResultException nre) {
				throw new Exception ("No User Found.");
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


	public List<AdminUserValueBean> getUsersList(DataModel dataModel) throws Exception {
		
		List<AdminUserValueBean> userList = new ArrayList<AdminUserValueBean>();
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<AdminUserBean> criteriaQuery = criteriaBuilder.createQuery(AdminUserBean.class);
			Root<AdminUserBean> root = criteriaQuery.from(AdminUserBean.class);
			
			List<Predicate> predicates = new ArrayList<Predicate>();
			predicates.add(criteriaBuilder.equal(root.get("regionBean").get("id"), dataModel.getRegions().getId()));
			predicates.add(criteriaBuilder.equal(root.get("circleBean").get("id"), dataModel.getCircles().getId()));
			predicates.add(criteriaBuilder.equal(root.get("divisionBean").get("id"), dataModel.getDivisions().getId()));
			
			if(dataModel.getSubDivisions() != null && dataModel.getSubDivisions().getId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("subDivisionBean").get("id"), dataModel.getSubDivisions().getId()));
			}
			if(dataModel.getSections() != null && dataModel.getSections().getId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("sectionBean").get("id"), dataModel.getSections().getId()));
			}
					
			//Constructing order list of parameters
			List<Order> orderList = new ArrayList<Order>();
			orderList.add(criteriaBuilder.asc(root.get("regionBean").get("name")));
			orderList.add(criteriaBuilder.asc(root.get("circleBean").get("name")));
			orderList.add(criteriaBuilder.asc(root.get("divisionBean").get("name")));
			//orderList.add(criteriaBuilder.asc(root.get("subDivisionBean").get("name")));
			//orderList.add(criteriaBuilder.asc(root.get("sectionBean").get("name")));
			
			criteriaQuery.select(root).where(predicates.toArray(new Predicate[]{}))
			.orderBy(orderList);

			Query<AdminUserBean> query = session.createQuery(criteriaQuery);
			List<AdminUserBean> listAdminUserList = query.getResultList();
			
			for(AdminUserBean adminUsers : listAdminUserList) {
				userList.add(AdminUserValueBean.convertAdminUserBeanToAdminUserValueBean(adminUsers));
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

		return userList;
	}
	
	
	public PublicUserValueBean getConsumerByMobile(String mobileNumber) throws Exception {
		PublicUserValueBean publicUserValueBean = null;
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<PublicUserBean> criteriaQuery = criteriaBuilder.createQuery(PublicUserBean.class);
			Root<PublicUserBean> root = criteriaQuery.from(PublicUserBean.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("mobile"), mobileNumber));

			Query<PublicUserBean> query = session.createQuery(criteriaQuery);
			
			List<PublicUserBean> list = query.getResultList();
			
			if(list.size() > 0 ) {
				publicUserValueBean = PublicUserValueBean.convertPublicUserBeanToPublicUserValueBean(list.get(0));
				
			}
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
		return publicUserValueBean;

	}
	
	
	public void registerUser(PublicUserBean user) throws Exception {
		SessionFactory factory = null;
		Session session = null;
		Transaction transaction = null;
		try {
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			transaction = session.beginTransaction();
			session.save(user);
			transaction.commit();
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
	
	public void updateUser(PublicUserValueBean publicUserValueBean) throws Exception {
		SessionFactory factory = null;
		Session session = null;
		Transaction transaction = null;
		try {
			PublicUserBean user = PublicUserValueBean.convertPublicUserValueBeanToPublicUserBean(publicUserValueBean);
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			transaction = session.beginTransaction();
			session.update(user);
			transaction.commit();
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
}
