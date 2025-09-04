package tneb.ccms.admin.controller;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tneb.ccms.admin.AdminMain;
import tneb.ccms.admin.dao.ComplaintsDao;
import tneb.ccms.admin.dao.FieldWorkerDao;
import tneb.ccms.admin.model.DistrictBean;
import tneb.ccms.admin.model.LoginParams;
import tneb.ccms.admin.util.CCMSConstants;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.valuebeans.AdminUserValueBean;
import tneb.ccms.admin.valuebeans.DistrictValueBean;
import tneb.ccms.admin.valuebeans.FieldWorkerValueBean;

public class FieldWorkerController {

	private Logger logger = LoggerFactory.getLogger(FieldWorkerController.class.getName());
	
	@ManagedProperty("#{admin}")
	AdminMain admin;
	
	DataModel dmFilter = new DataModel();
	LoginParams officer;
	List<FieldWorkerValueBean> fieldWorkerList;
	
	
	FieldWorkerValueBean fieldWorkerValueBean = new FieldWorkerValueBean();
	AdminUserValueBean adminUserValueBean = new AdminUserValueBean();
	
	public FieldWorkerController() {
		super();
		init();

	}

	public void init() {
			try {
				admin = (AdminMain) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("admin");
				officer = admin.getAuth().getOfficer();

				ComplaintsDao complaintsDao = new ComplaintsDao();
				dmFilter.setListDistrict(complaintsDao.listDistrict(officer.getCircleIdList()));
			} catch (Exception e) {
				logger.error(ExceptionUtils.getStackTrace(e));
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
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
				
				CriteriaBuilder  criteriaBuilder = session.getCriteriaBuilder();
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
	
	
	
	public void getFieldWorkersList() {
		
		try {
			FieldWorkerDao fieldWorkerDao =  new FieldWorkerDao();
			
			setFieldWorkerList(fieldWorkerDao.getFieldWorkersList(dmFilter));
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}

	}
	
	
	public void addFieldUser() {
		
		try {
			FieldWorkerDao dao = new FieldWorkerDao();
			
			String errorMessage = dao.validateFieldUser(fieldWorkerValueBean.getMobile(), dmFilter.getSectionDropDown());
			
			if(StringUtils.isNotBlank(errorMessage)){
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warn", errorMessage));
				return;
			} else {
				FieldWorkerDao fieldWorkerDao = new FieldWorkerDao();
				fieldWorkerDao.addFieldWorker(fieldWorkerValueBean, dmFilter);
				clearFieldWorker();
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
		
	}
	
	public void clearFieldWorker() {
		dmFilter.setSectionDropDown("");
		dmFilter.setDistrictDropDown("");
		fieldWorkerValueBean.setMobile("");
		fieldWorkerValueBean.setStaffCode("");
		fieldWorkerValueBean.setStaffRole("");
		fieldWorkerValueBean.setName("");
		setFieldWorkerList(new ArrayList<FieldWorkerValueBean>());
	}
	
		
	public void updateFieldUser(FieldWorkerValueBean fieldWorkerValueBean) {

		try {
			FieldWorkerDao dao = new FieldWorkerDao();
			
			String errorMessage = dao.validateFieldUserOnUpdate(fieldWorkerValueBean, dmFilter.getSectionDropDown());
			
			if(StringUtils.isNotBlank(errorMessage)){
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warn", errorMessage));
				return;
			} else {
				dao.updateFieldUser(fieldWorkerValueBean);

				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Field User updated successfully."));
			}

		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}

	}
	
	public void editFieldUser(FieldWorkerValueBean workerValueBean) {
		
		try {
			FieldWorkerDao dao = new FieldWorkerDao();
			
			fieldWorkerValueBean = dao.getFieldUser(workerValueBean.getId());
			
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			
			FacesContext.getCurrentInstance().getExternalContext().redirect(externalContext.getRequestContextPath()+"/faces/callcenter/manage/updatefieldworker.xhtml");
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
		}
		
	}
	

	public DataModel getDmFilter() {
		return dmFilter;
	}

	public void setDmFilter(DataModel dmFilter) {
		this.dmFilter = dmFilter;
	}

	public LoginParams getOfficer() {
		return officer;
	}

	public void setOfficer(LoginParams officer) {
		this.officer = officer;
	}

	public void setFieldWorkerList(List<FieldWorkerValueBean> fieldWorkerList) {
		this.fieldWorkerList = fieldWorkerList;
	}
	
	public List<FieldWorkerValueBean> getFieldWorkerList() {
		return fieldWorkerList;
	}

	public FieldWorkerValueBean getFieldWorkerValueBean() {
		return fieldWorkerValueBean;
	}

	public void setFieldWorkerValueBean(FieldWorkerValueBean fieldWorkerValueBean) {
		this.fieldWorkerValueBean = fieldWorkerValueBean;
	}

	public AdminUserValueBean getAdminUserValueBean() {
		return adminUserValueBean;
	}

	public void setAdminUserValueBean(AdminUserValueBean adminUserValueBean) {
		this.adminUserValueBean = adminUserValueBean;
	}
	
	

}
