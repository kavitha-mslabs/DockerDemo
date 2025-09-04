package tneb.ccms.admin.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tneb.ccms.admin.model.AdminUserBean;
import tneb.ccms.admin.model.CircleBean;
import tneb.ccms.admin.model.DistrictBean;
import tneb.ccms.admin.model.DivisionBean;
import tneb.ccms.admin.model.FieldWorkerBean;
import tneb.ccms.admin.model.RegionBean;
import tneb.ccms.admin.model.RoleBean;
import tneb.ccms.admin.model.SectionBean;
import tneb.ccms.admin.model.SubDivisionBean;
import tneb.ccms.admin.valuebeans.CircleValueBean;
import tneb.ccms.admin.valuebeans.ClosureReasonValueBean;
import tneb.ccms.admin.valuebeans.DTMasterGISValueBean;
import tneb.ccms.admin.valuebeans.DistrictValueBean;
import tneb.ccms.admin.valuebeans.DivisionValueBean;
import tneb.ccms.admin.valuebeans.FieldWorkerValueBean;
import tneb.ccms.admin.valuebeans.RegionValueBean;
import tneb.ccms.admin.valuebeans.RoleValueBean;
import tneb.ccms.admin.valuebeans.SectionValueBean;
import tneb.ccms.admin.valuebeans.SubCategoriesValueBean;
import tneb.ccms.admin.valuebeans.SubDivisionValueBean;

public class DataModel {
	
	private Logger logger = LoggerFactory.getLogger(DataModel.class.getName());

	RegionBean 		regions 		= new RegionBean();
	CircleBean 		circles 		= new CircleBean();
	DivisionBean 	divisions 		= new DivisionBean();
	SubDivisionBean subDivisions 	= new SubDivisionBean();
	SectionBean 	sections 		= new SectionBean();
	RoleBean 		roles 			= new RoleBean();
	AdminUserBean 	adminUsers 		= new AdminUserBean();
	ComplaintLog 	complaintLog 	= new ComplaintLog();
	DistrictBean 	districts 		= new DistrictBean();
	FieldWorkerBean fieldWorker		= new FieldWorkerBean();
	
	List<RegionValueBean>			lstRegions;
	List<CircleValueBean> 			lstCircles;
	List<DivisionValueBean> 		lstDivisions;
	List<SubDivisionValueBean> 		lstSubDivisions;
	List<SectionValueBean> 			lstSections;
	
	List<SectionValueBean>			lstSectionsMinnagam;
	List<DTMasterGISValueBean>		lstSSName;
	List<DTMasterGISValueBean>      lstFdrName;
	List<DTMasterGISValueBean>      lstDtName;
	
	List<ClosureReasonValueBean> 	lstReasons;
	List<RoleValueBean> 			lstRoles;
	List<CircleValueBean>           listCircles;
	
	List<DistrictValueBean> listDistrict;
	List<DistrictValueBean> smListDistrict;
	List<DistrictValueBean> listDistrictSection;
	List<DistrictValueBean> smListDistrictSection;
	List<FieldWorkerValueBean> listFieldWorker;
	List<SubCategoriesValueBean> listSubCategory;
	
	ArrayList<ComplaintLog> lstComplaintLog = new ArrayList<ComplaintLog>();
	private List<String> platForm = new ArrayList<>();
	private String[] regionFilter;
	private String regionOneFilter;
	private String[] circleFilter;
	private String circleOneFilter;
	private String[] divisionFilter;
	private String divisionOneFilter;
	private String[] subDivisionFilter;
	private String subDivisionOneFilter;
	private String[] sectionFilter;
	private String sectionOneFilter;
	private String[] compType;
	private String[] compSubType;
	public String[] getCompSubType() {
		return compSubType;
	}

	public void setCompSubType(String[] compSubType) {
		this.compSubType = compSubType;
	}

	private String compOneType;
	private String[] statuses;
	private String statusesOne;
	private String[] tagArray;
	private String[] receivedFromArray;
	private String[] devices;
	private String devicesOne;
	private Date fromDate;
	private Date toDate;
	private String device;
	
	private int first;
    private int pageSize; 
    private int rowCount;
    
    private String sortField;
    private Object sortOrder;
    private Map<String, Object> filters = new HashMap<>();
	
	private Date currentDate = new Date();
	private Date minDate = new Date();
	private String address;
	private String landMark;
	private String complaintDescription;
	private String complaintType;
	private String districtDropDown;
	private String smDistrictDropdown;
	private String sectionDropDown;
	private String smSectionDropdown;
	private String consumerNumber;
	private String fieldWorkerDropDown;
	private String subCategory;
	private String applicationNo;
	private String receivedFrom;
	private String tag;
	private String alternateMobileNo;
	
	private String cirCode;
	private String year;
	
	private String year1;
	private String month1;
	private String year2;
	private String month2;
	
	private BigDecimal total;
	private BigDecimal pending;
	private BigDecimal inProgress;
	private BigDecimal completed;
	private BigDecimal completedPercentage;
	
	private BigDecimal currentTotal;
	private BigDecimal currentPending;
	private BigDecimal currentInProgress;
	private BigDecimal currentCompleted;
	private BigDecimal currentCompletedPercentage;
	
	private Integer noOfOffices;
	private Integer complaintID;
	private String days;
	private String hours;
	
	
	private String regionCode;
	private String circleCode;
	private String sectionCode;
	private String contactNumber;
	private String userName;
	
	
	
	


	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	public void addFilter(String key, Object value) {
	        if (value != null) {
	            filters.put(key, value);
	        }
	    }
	
	public Date getMinDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(2021, Calendar.JANUARY, 1, 0, 0, 0);
		minDate = cal.getTime();
		return minDate;
	}
	
	int actionStatus;
	
	
	
	
	public String getRegionCode() {
		return regionCode;
	}

	public void setRegionCode(String regionCode) {
		this.regionCode = regionCode;
	}

	public String getCircleCode() {
		return circleCode;
	}

	public void setCircleCode(String circleCode) {
		this.circleCode = circleCode;
	}

	public String getSectionCode() {
		return sectionCode;
	}

	public void setSectionCode(String sectionCode) {
		this.sectionCode = sectionCode;
	}

	public List<SectionValueBean> getLstSectionsMinnagam() {
		return lstSectionsMinnagam;
	}

	public void setLstSectionsMinnagam(List<SectionValueBean> lstSectionsMinnagam) {
		this.lstSectionsMinnagam = lstSectionsMinnagam;
	}

	public List<DTMasterGISValueBean> getLstSSName() {
		return lstSSName;
	}

	public void setLstSSName(List<DTMasterGISValueBean> lstSSName) {
		this.lstSSName = lstSSName;
	}

	public List<DTMasterGISValueBean> getLstFdrName() {
		return lstFdrName;
	}

	public void setLstFdrName(List<DTMasterGISValueBean> lstFdrName) {
		this.lstFdrName = lstFdrName;
	}

	public String getHours() {
		return hours;
	}

	public void setHours(String hours) {
		this.hours = hours;
	}
	
	public String getDays() {
		return days;
	}

	public void setDays(String days) {
		this.days = days;
	}
	
	public Integer getComplaintID() {
		return complaintID;
	}

	public void setComplaintID(Integer complaintID) {
		this.complaintID = complaintID;
	}
	 public Integer getNoOfOffices() {
			return noOfOffices;
    }

    public void setNoOfOffices(Integer noOfOffices) {
			this.noOfOffices = noOfOffices;
    }
		
	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public BigDecimal getPending() {
		return pending;
	}

	public void setPending(BigDecimal pending) {
		this.pending = pending;
	}

	public BigDecimal getInProgress() {
		return inProgress;
	}

	public void setInProgress(BigDecimal inProgress) {
		this.inProgress = inProgress;
	}

	public BigDecimal getCompleted() {
		return completed;
	}

	public void setCompleted(BigDecimal completed) {
		this.completed = completed;
	}

	public BigDecimal getCompletedPercentage() {
		return completedPercentage;
	}

	public void setCompletedPercentage(BigDecimal completedPercentage) {
		this.completedPercentage = completedPercentage;
	}

	public BigDecimal getCurrentTotal() {
		return currentTotal;
	}

	public void setCurrentTotal(BigDecimal currentTotal) {
		this.currentTotal = currentTotal;
	}

	public BigDecimal getCurrentPending() {
		return currentPending;
	}

	public void setCurrentPending(BigDecimal currentPending) {
		this.currentPending = currentPending;
	}

	public BigDecimal getCurrentInProgress() {
		return currentInProgress;
	}

	public void setCurrentInProgress(BigDecimal currentInProgress) {
		this.currentInProgress = currentInProgress;
	}

	public BigDecimal getCurrentCompleted() {
		return currentCompleted;
	}

	public void setCurrentCompleted(BigDecimal currentCompleted) {
		this.currentCompleted = currentCompleted;
	}

	public BigDecimal getCurrentCompletedPercentage() {
		return currentCompletedPercentage;
	}

	public void setCurrentCompletedPercentage(BigDecimal currentCompletedPercentage) {
		this.currentCompletedPercentage = currentCompletedPercentage;
	}

	public String getDevicesOne() {
		return devicesOne;
	}

	public void setDevicesOne(String devicesOne) {
		this.devicesOne = devicesOne;
	}

	public String getStatusesOne() {
		return statusesOne;
	}

	public void setStatusesOne(String statusesOne) {
		this.statusesOne = statusesOne;
	}

	public String getCompOneType() {
		return compOneType;
	}

	public void setCompOneType(String compOneType) {
		this.compOneType = compOneType;
	}

	public String getSectionOneFilter() {
		return sectionOneFilter;
	}

	public void setSectionOneFilter(String sectionOneFilter) {
		this.sectionOneFilter = sectionOneFilter;
	}

	public String getSubDivisionOneFilter() {
		return subDivisionOneFilter;
	}

	public void setSubDivisionOneFilter(String subDivisionOneFilter) {
		this.subDivisionOneFilter = subDivisionOneFilter;
	}

	public String getDivisionOneFilter() {
		return divisionOneFilter;
	}

	public void setDivisionOneFilter(String divisionOneFilter) {
		this.divisionOneFilter = divisionOneFilter;
	}

	public String getCircleOneFilter() {
		return circleOneFilter;
	}

	public void setCircleOneFilter(String circleOneFilter) {
		this.circleOneFilter = circleOneFilter;
	}

	public String getRegionOneFilter() {
		return regionOneFilter;
	}

	public void setRegionOneFilter(String regionOneFilter) {
		this.regionOneFilter = regionOneFilter;
	}

	public String getCirCode() {
		return cirCode;
	}

	public void setCirCode(String cirCode) {
		this.cirCode = cirCode;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public Map<String, Object> getFilters() {
		return filters;
	}

	public void setFilters(Map<String, Object> filters) {
		this.filters = filters;
	}

	public String getSortField() {
		return sortField;
	}

	public void setSortField(String sortField) {
		this.sortField = sortField;
	}

	public Object getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(Object sortOrder) {
		this.sortOrder = sortOrder;
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public int getFirst() {
		return first;
	}

	public void setFirst(int first) {
		this.first = first;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public String[] getDevices() {
		return devices;
	}

	public void setDevices(String[] devices) {
		this.devices = devices;
	}

	public List<String> getPlatForm() {
		return platForm;
	}

	public void setPlatForm(List<String> platForm) {
		this.platForm = platForm;
	}

	public int getActionStatus() {
		return actionStatus;
	}

	public void setActionStatus(int actionStatus) {
		this.actionStatus = actionStatus;
	}

	public int[][] arrDash = new int[10][3];
	
	public int[][] arrReport = new int[10][3];
	
	
	
	
	public List<CircleValueBean> getListCircles() {
		return listCircles;
	}

	public void setListCircles(List<CircleValueBean> listCircles) {
		this.listCircles = listCircles;
	}

	public String getSmSectionDropdown() {
		return smSectionDropdown;
	}

	public void setSmSectionDropdown(String smSectionDropdown) {
		this.smSectionDropdown = smSectionDropdown;
	}

	public List<DistrictValueBean> getSmListDistrictSection() {
		return smListDistrictSection;
	}

	public void setSmListDistrictSection(List<DistrictValueBean> smListDistrictSection) {
		this.smListDistrictSection = smListDistrictSection;
	}

	public List<DistrictValueBean> getSmListDistrict() {
		return smListDistrict;
	}

	public void setSmListDistrict(List<DistrictValueBean> smListDistrict) {
		this.smListDistrict = smListDistrict;
	}

	public String getSmDistrictDropdown() {
		return smDistrictDropdown;
	}

	public void setSmDistrictDropdown(String smDistrictDropdown) {
		this.smDistrictDropdown = smDistrictDropdown;
	}

	public List<ClosureReasonValueBean> getLstReasons() {
		return lstReasons;
	}

	public void setLstReasons(List<ClosureReasonValueBean> lstReasons) {
		this.lstReasons = lstReasons;
	}

	public ComplaintLog getComplaintLog() {
		return complaintLog;
	}

	public void setComplaintLog(ComplaintLog complaintLog) {
		this.complaintLog = complaintLog;
	}

	public ArrayList<ComplaintLog> getLstComplaintLog() {
		return lstComplaintLog;
	}

	public void setLstComplaintLog(ArrayList<ComplaintLog> lstComplaintLog) {
		this.lstComplaintLog = lstComplaintLog;
	}
	
	public int[][] getArrDash() {
		return arrDash;
	}

	public void setArrDash(int[][] arrDash) {
		this.arrDash = arrDash;
	}
	
	public int[][] getArrReport() {
		return arrReport;
	}

	public void setArrReport(int[][] arrReport) {
		this.arrReport = arrReport;
	}

	public List<CircleValueBean> getLstCircles() {
		return lstCircles;
	}

	public void setLstCircles(List<CircleValueBean> lstCircles) {
		this.lstCircles = lstCircles;
	}

	public List<DivisionValueBean> getLstDivisions() {
		return lstDivisions;
	}

	public void setLstDivisions(List<DivisionValueBean> lstDivisions) {
		this.lstDivisions = lstDivisions;
	}

	public List<SubDivisionValueBean> getLstSubDivisions() {
		return lstSubDivisions;
	}

	public void setLstSubDivisions(List<SubDivisionValueBean> lstSubDivisions) {
		this.lstSubDivisions = lstSubDivisions;
	}

	public List<SectionValueBean> getLstSections() {
		return lstSections;
	}

	public void setLstSections(List<SectionValueBean> lstSections) {
		this.lstSections = lstSections;
	}

	public List<RegionValueBean> getLstRegions() {
		return lstRegions;
	}
	
	public void setLstRegions(List<RegionValueBean> lstRegions) {
		this.lstRegions = lstRegions;
	}

	public RegionBean getRegions() {

		return regions;
	}

	public void setRegions(RegionBean regions) {
		this.regions = regions;
	}

	public CircleBean getCircles() {
		return circles;
	}

	public void setCircles(CircleBean circles) {
		this.circles = circles;
	}

	public DivisionBean getDivisions() {
		return divisions;
	}

	public void setDivisions(DivisionBean divisions) {
		this.divisions = divisions;
	}

	public SubDivisionBean getSubDivisions() {
		return subDivisions;
	}

	public void setSubDivisions(SubDivisionBean subDivisions) {
		this.subDivisions = subDivisions;
	}

	public SectionBean getSections() {
		return sections;
	}

	public void setSections(SectionBean sections) {
		this.sections = sections;
	}
	

	public RoleBean getRoles() {
		return roles;
	}

	public void setRoles(RoleBean roles) {
		this.roles = roles;
	}

	public List<RoleValueBean> getLstRoles() {
		return lstRoles;
	}

	public void setLstRoles(List<RoleValueBean> lstRoles) {
		this.lstRoles = lstRoles;
	}

	public List<DistrictValueBean> getListDistrictSection() {
		return listDistrictSection;
	}

	public void setListDistrictSection(List<DistrictValueBean> listDistrictSection) {
		this.listDistrictSection = listDistrictSection;
	}
	
	
	public List<FieldWorkerValueBean> getListFieldWorker() {
		return listFieldWorker;
	}

	public void setListFieldWorker(List<FieldWorkerValueBean> listFieldWorker) {
		this.listFieldWorker = listFieldWorker;
	}

	public AdminUserBean getAdminUsers() {
		return adminUsers;
	}

	public void setAdminUsers(AdminUserBean adminUsers) {
		this.adminUsers = adminUsers;
	}
	

	public String[] getCompType() {
		return compType;
	}

	public void setCompType(String[] compType) {
		this.compType = compType;
	}

	public String[] getStatuses() {
		return statuses;
	}

	public void setStatuses(String[] statuses) {
		this.statuses = statuses;
	}
	
	public String[] getRegionFilter() {
		return regionFilter;
	}

	public void setRegionFilter(String[] regionFilter) {
		this.regionFilter = regionFilter;
	}

	public String[] getCircleFilter() {
		return circleFilter;
	}

	public void setCircleFilter(String[] circleFilter) {
		this.circleFilter = circleFilter;
	}

	public String[] getDivisionFilter() {
		return divisionFilter;
	}

	public void setDivisionFilter(String[] divisionFilter) {
		this.divisionFilter = divisionFilter;
	}

	public String[] getSubDivisionFilter() {
		return subDivisionFilter;
	}

	public void setSubDivisionFilter(String[] subDivisionFilter) {
		this.subDivisionFilter = subDivisionFilter;
	}

	public String[] getSectionFilter() {
		return sectionFilter;
	}

	public void setSectionFilter(String[] sectionFilter) {
		this.sectionFilter = sectionFilter;
	}

	public List<DistrictValueBean> getListDistrict() {
		return listDistrict;
	}

	public void setListDistrict(List<DistrictValueBean> listDistrict) {
		this.listDistrict = listDistrict;
	}

	public String getDistrictDropDown() {
		return districtDropDown;
	}

	public void setDistrictDropDown(String districtDropDown) {
		this.districtDropDown = districtDropDown;
	}

	public String getFieldWorkerDropDown() {
		return fieldWorkerDropDown;
	}

	public void setFieldWorkerDropDown(String fieldWorkerDropDown) {
		this.fieldWorkerDropDown = fieldWorkerDropDown;
	}

	public String getSectionDropDown() {
		return sectionDropDown;
	}

	public void setSectionDropDown(String sectionDropDown) {
		this.sectionDropDown = sectionDropDown;
	}

	public String getConsumerNumber() {
		return consumerNumber;
	}

	public void setConsumerNumber(String consumerNumber) {
		this.consumerNumber = consumerNumber;
	}

	public String getComplaintType() {
		return complaintType;
	}

	public void setComplaintType(String complaintType) {
		this.complaintType = complaintType;
	}
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getLandMark() {
		return landMark;
	}

	public void setLandMark(String landMark) {
		this.landMark = landMark;
	}

	public String getComplaintDescription() {
		return complaintDescription;
	}

	public void setComplaintDescription(String complaintDescription) {
		this.complaintDescription = complaintDescription;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public Date getCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}
	
	public List<SubCategoriesValueBean> getListSubCategory() {
		return listSubCategory;
	}

	public void setListSubCategory(List<SubCategoriesValueBean> listSubCategory) {
		this.listSubCategory = listSubCategory;
	}

	public String getSubCategory() {
		return subCategory;
	}

	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}

	public DistrictBean getDistricts() {
		return districts;
	}

	public void setDistricts(DistrictBean districts) {
		this.districts = districts;
	}

	public FieldWorkerBean getFieldWorker() {
		return fieldWorker;
	}

	public void setFieldWorker(FieldWorkerBean fieldWorker) {
		this.fieldWorker = fieldWorker;
	}

	public String getApplicationNo() {
		return applicationNo;
	}

	public void setApplicationNo(String applicationNo) {
		this.applicationNo = applicationNo;
	}

	public String getReceivedFrom() {
		return receivedFrom;
	}

	public void setReceivedFrom(String receivedFrom) {
		this.receivedFrom = receivedFrom;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public String[] getTagArray() {
		return tagArray;
	}

	public void setTagArray(String[] tagArray) {
		this.tagArray = tagArray;
	}

	public String[] getReceivedFromArray() {
		return receivedFromArray;
	}

	public void setReceivedFromArray(String[] receivedFromArray) {
		this.receivedFromArray = receivedFromArray;
	}

	public String getAlternateMobileNo() {
		return alternateMobileNo;
	}

	public void setAlternateMobileNo(String alternateMobileNo) {
		this.alternateMobileNo = alternateMobileNo;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}
	
	public String getYear1() {
		return year1;
	}

	public void setYear1(String year1) {
		this.year1 = year1;
	}

	public String getMonth1() {
		return month1;
	}

	public void setMonth1(String month1) {
		this.month1 = month1;
	}

	public String getYear2() {
		return year2;
	}

	public void setYear2(String year2) {
		this.year2 = year2;
	}

	public String getMonth2() {
		return month2;
	}

	public void setMonth2(String month2) {
		this.month2 = month2;
	}

	public List<DTMasterGISValueBean> getLstDtName() {
		return lstDtName;
	}

	public void setLstDtName(List<DTMasterGISValueBean> lstDtName) {
		this.lstDtName = lstDtName;
	}




	public class ComplaintLog {

		String description;
		String compalintDatetime;
		int statusId;
		String duration;

		public String getDuration() {
			return duration;
		}

		public void setDuration(String duration) {
			this.duration = duration;
		}

		public int getStatusId() {
			return statusId;
		}

		public void setStatusId(int statusId) {
			this.statusId = statusId;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getCompalintDatetime() {
			return compalintDatetime;
		}

		public void setCompalintDatetime(String compalintDatetime) {
			this.compalintDatetime = compalintDatetime;
		}

		
		
		

	}
	
	public void listRoles() {
		
		SessionFactory factory = null;
		Session session = null;
		try {
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			List<RoleValueBean> rolesList = new ArrayList<RoleValueBean>();
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<RoleBean> criteriaQuery = criteriaBuilder.createQuery(RoleBean.class);
			Root<RoleBean> root = criteriaQuery.from(RoleBean.class);
			criteriaQuery.select(root).orderBy(criteriaBuilder.asc(root.get("name")));

			Query<RoleBean> query = session.createQuery(criteriaQuery);
			List<RoleBean> list = query.getResultList();
			
			for(RoleBean roleBean : list) {
				rolesList.add(RoleValueBean.convertRoleBeanToRoleValueBean(roleBean));
			}

			setLstRoles(rolesList);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}
	}


	public List<RegionBean> getRegionsList() {

		List<RegionBean> regionList = null;
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
			regionList = query.getResultList();
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		} finally {
			HibernateUtil.closeSession(factory, session);
		}

		return regionList;
	}

}
