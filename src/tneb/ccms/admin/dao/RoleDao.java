package tneb.ccms.admin.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tneb.ccms.admin.model.RoleBean;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.valuebeans.RoleValueBean;


public class RoleDao {
	
	private Logger logger = LoggerFactory.getLogger(RoleDao.class.getName());

	public List<RoleValueBean> getAllRoles() throws Exception {
		
		List<RoleValueBean> roleList = new ArrayList<RoleValueBean>();
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<RoleBean> criteriaQuery = criteriaBuilder.createQuery(RoleBean.class);
			Root<RoleBean> root = criteriaQuery.from(RoleBean.class);
			criteriaQuery.select(root).orderBy(criteriaBuilder.asc(root.get("name")));
			List<RoleBean> rolesList = session.createQuery(criteriaQuery).getResultList();
			
			for (RoleBean roles : rolesList) {
				roleList.add(RoleValueBean.convertRoleBeanToRoleValueBean(roles));
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
	    
	    return roleList;
	}
	
}
