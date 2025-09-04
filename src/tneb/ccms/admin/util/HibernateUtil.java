package tneb.ccms.admin.util;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtil {

	private static Logger logger = LoggerFactory.getLogger(HibernateUtil.class.getName());

	private HibernateUtil() {
	}
	
	private static SessionFactory factory = null;

	private static SessionFactory getDatabaseConnection() {
		return new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
	}

	public static synchronized SessionFactory getSessionFactory() {

		if(factory == null) {
			factory = getDatabaseConnection();
		} else {
			if(factory.isClosed()) {
				factory = null;
				factory = getDatabaseConnection();
			}
		}
		return factory;
	}

	public static void closeSession(SessionFactory sessionFactory, Session session) {

		try {
			if (session != null) {
				session.close();
			}
//			if (sessionFactory != null) {
//				sessionFactory.close();
//				sessionFactory = null;
//			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
	}

}
