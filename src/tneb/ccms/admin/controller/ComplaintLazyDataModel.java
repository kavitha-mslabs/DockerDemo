package tneb.ccms.admin.controller;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import tneb.ccms.admin.dao.ComplaintsDao;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.valuebeans.ViewComplaintReportValueBean;

public class ComplaintLazyDataModel extends LazyDataModel<ViewComplaintReportValueBean>{

	 /**
	 * 
	 */
	private static final long serialVersionUID = 2113986571617014863L;
	
	private List<ViewComplaintReportValueBean> complaints;
	 private int complaintCodeValue;
	 private int statusValue;
	 private DataModel dmFilter;
	 public String subCategoryNames;
	 public String getSubCategoryNames() {
		return subCategoryNames;
	}

	public void setSubCategoryNames(String subCategoryNames) {
		this.subCategoryNames = subCategoryNames;
	}

	public String statuses;
	 public String serviceNumber;
	 public String mobile;
	 public String complaintType;
	 public int id;
	 
	 private Map<Integer, List<ViewComplaintReportValueBean>> pageCache = new HashMap<>();
	 
	 public ComplaintLazyDataModel() {
		// TODO Auto-generated constructor stub
	}
	
	 public ComplaintLazyDataModel(DataModel dmFilter, int complaintCodeValue, int statusValue) {
		 	this.dmFilter =  dmFilter;
	        this.complaintCodeValue = complaintCodeValue;
	        this.statusValue = statusValue;
	    }
	 	@Override
		public List<ViewComplaintReportValueBean> load(int first, int pageSize, Map<String, SortMeta> sortBy,
				Map<String, FilterMeta> filterBy) {
		 
		 	SessionFactory factory = null;
			Session session = null;
			
			factory = HibernateUtil.getSessionFactory();
			session = factory.openSession();
			
			String hql = "FROM ViewComplaintReportBean c WHERE 1=1";
	         
			 if (filterBy != null) {
				 if (filterBy.containsKey("statuses") && filterBy.get("statuses") != null) {
					 hql += " AND c.statusId LIKE :statuses";
					 
		            }
		         
				 if (filterBy.containsKey("serviceNumber") && filterBy.get("serviceNumber") != null) {
					 hql += " AND c.serviceNumber LIKE :serviceNumber";
					 
		            }
				 
				 if (filterBy.containsKey("mobile") && filterBy.get("mobile") != null) {
					 hql += " AND c.mobile LIKE :mobile";
					 
		            }else {
		            	hql += " AND c.compMobileNumber LIKE :compMobileNumber";
		            }
				 
				 if (filterBy.containsKey("complaintType") && filterBy.get("complaintType") != null) {
					 hql += " AND c.complaintType LIKE :complaintType";
					 
		            }
				 if (filterBy.containsKey("subCategoryNames") && filterBy.get("subCategoryNames") != null) {
					 hql += " AND c.subCategoryName LIKE :subCategoryNames";
					 
		            }
				 if (filterBy.containsKey("id") && filterBy.get("id") != null) {
					 hql += " AND c.id LIKE :id";
					 
		            }
			 }
			 
			 Query hqlQuery = session.createQuery(hql);
			 
			 if (filterBy.containsKey("statuses") && filterBy.get("statuses") != null) {
		            hqlQuery.setParameter("statuses", "%" + filterBy.get("statuses") + "%");
		        }
			   
			 if (filterBy.containsKey("serviceNumber") && filterBy.get("serviceNumber") != null) {
		            hqlQuery.setParameter("serviceNumber", "%" + filterBy.get("serviceNumber") + "%");
		        }
			 
			 if (filterBy.containsKey("mobile") && filterBy.get("mobile") != null) {
		            hqlQuery.setParameter("mobile", "%" + filterBy.get("mobile") + "%");
		        }else {
		        	hqlQuery.setParameter("compMobileNumber", "%" + filterBy.get("compMobileNumber") + "%");
		        }
			 
			 if (filterBy.containsKey("complaintType") && filterBy.get("complaintType") != null) {
		            hqlQuery.setParameter("complaintType", "%" + filterBy.get("complaintType") + "%");
		        }
			 if (filterBy.containsKey("subCategoryNames") && filterBy.get("subCategoryNames") != null) {
		            hqlQuery.setParameter("subCategoryNames", "%" + filterBy.get("subCategoryNames") + "%");
		        }
			 if (filterBy.containsKey("id") && filterBy.get("id") != null) {
		            hqlQuery.setParameter("id", "%" + filterBy.get("id") + "%");
		        }
			 
	        dmFilter.setFirst(first);
	        dmFilter.setPageSize(pageSize);
	       
	        int statusFilterValue = -1;
	        String complaintCodeFilterValue = null;
	        String serviceNumberValue = null;
	        String subCategoryNameValue = null;
	        String mobileValue = null;
	        String complaintTypeValue = null;
	        int id = -1;
	        
	        if (filterBy != null && filterBy.containsKey("statuses")) {
	            FilterMeta statusFilterMeta = filterBy.get("statuses");
	            if (statusFilterMeta.getFilterValue() != null) {
	                statusFilterValue =  Integer.parseInt((String) statusFilterMeta.getFilterValue());
	                
	            }
	        }
	        
	        if (filterBy != null && filterBy.containsKey("subCategoryName")) {
	            FilterMeta statusFilterMeta = filterBy.get("subCategoryName");
	            if (statusFilterMeta.getFilterValue() != null) {
	            	subCategoryNameValue =  (String) statusFilterMeta.getFilterValue();
	                
	            }
	        }
	        if (filterBy != null && filterBy.containsKey("serviceNumber")) {
	            FilterMeta statusFilterMeta = filterBy.get("serviceNumber");
	            if (statusFilterMeta.getFilterValue() != null) {
	            	serviceNumberValue =  (String) statusFilterMeta.getFilterValue();
	                
	            }
	        }
	        if (filterBy != null && filterBy.containsKey("mobile")) {
	            FilterMeta statusFilterMeta = filterBy.get("mobile");
	            if (statusFilterMeta.getFilterValue() != null) {
	            	mobileValue =  (String) statusFilterMeta.getFilterValue();
	                
	            }
	        }else {
	        	if(filterBy != null && filterBy.containsKey("compMobileNumber")) {
		        	FilterMeta statusFilterMeta = filterBy.get("compMobileNumber");
		            if (statusFilterMeta.getFilterValue() != null) {
		            	mobileValue =  (String) statusFilterMeta.getFilterValue();
		                
		            }
	            }
	        }
	        
	        if (filterBy != null && filterBy.containsKey("complaintCodeValue")) {
	            FilterMeta complaintCodeFilterMeta = filterBy.get("complaintCodeValue");
	            if (complaintCodeFilterMeta.getFilterValue() != null) {
	                complaintCodeFilterValue = (String) complaintCodeFilterMeta.getFilterValue();
	            }
	        }
	        
	        if (filterBy != null && filterBy.containsKey("complaintType")) {
	            FilterMeta complaintCodeFilterMeta = filterBy.get("complaintType");
	            if (complaintCodeFilterMeta.getFilterValue() != null) {
	            	complaintTypeValue = (String) complaintCodeFilterMeta.getFilterValue();
	            }
	        }
	        
	        if (filterBy != null && filterBy.containsKey("subCategoryNames")) {
	            FilterMeta complaintCodeFilterMeta = filterBy.get("subCategoryNames");
	            if (complaintCodeFilterMeta.getFilterValue() != null) {
	            	subCategoryNameValue = (String) complaintCodeFilterMeta.getFilterValue();
	            }
	        }
	        
	        if (filterBy != null && filterBy.containsKey("id")) {
	            FilterMeta complaintCodeFilterMeta = filterBy.get("id");
	            if (complaintCodeFilterMeta.getFilterValue() != null) {
	            	id = Integer.parseInt((String) complaintCodeFilterMeta.getFilterValue());
	            }
	        }

	        String sortField = null;
		       
	        SortOrder sortOrder = SortOrder.UNSORTED;
	        if (sortBy != null && !sortBy.isEmpty()) {
	            for (Map.Entry<String, SortMeta> entry : sortBy.entrySet()) {
	                sortField = entry.getKey();
	                sortOrder = entry.getValue().getOrder();
	            }
	        }

	        try {
	        	ComplaintsDao daoComplaints = new ComplaintsDao();
	        	
	            complaints = daoComplaints.searchComplaint1(dmFilter, complaintCodeValue, statusValue,first,pageSize,statusFilterValue,
	            		serviceNumberValue, mobileValue,complaintTypeValue,id); 
	        	
	        	if (sortField != null && !sortField.isEmpty()) {
	                Comparator<ViewComplaintReportValueBean> comparator = null;
	                switch (sortField) {
	                    case "statusValue":
	                        comparator = Comparator.comparing(ViewComplaintReportValueBean::getStatusValue);
	                        break;
	                    // Add other cases for different fields
	                    default:
	                        break;
	                }

	                if (comparator != null) {
	                    if (sortOrder == SortOrder.DESCENDING) {
	                        comparator = comparator.reversed();
	                    }
	                    complaints.sort(comparator);
	                }
	            }
	            setRowCount(dmFilter.getRowCount());
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        return complaints;
		}
		
	

		public List<ViewComplaintReportValueBean> getComplaints() {
			return complaints;
		}

		public void setComplaints(List<ViewComplaintReportValueBean> complaints) {
			this.complaints = complaints;
		}

		public int getComplaintCodeValue() {
			return complaintCodeValue;
		}

		public void setComplaintCodeValue(int complaintCodeValue) {
			this.complaintCodeValue = complaintCodeValue;
		}

		public int getStatusValue() {
			return statusValue;
		}

		public void setStatusValue(int statusValue) {
			this.statusValue = statusValue;
		}

		public DataModel getDmFilter() {
			return dmFilter;
		}

		public void setDmFilter(DataModel dmFilter) {
			this.dmFilter = dmFilter;
		}
		
		public String getStatuses() {
			return statuses;
		}
		public void setStatuses(String statuses) {
			this.statuses = statuses;
		}
		
		public String getServiceNumber() {
			return serviceNumber;
		}

		public void setServiceNumber(String serviceNumber) {
			this.serviceNumber = serviceNumber;
		}

		public String getMobile() {
			return mobile;
		}

		public void setMobile(String mobile) {
			this.mobile = mobile;
		}
		
		public String getComplaintType() {
			return complaintType;
		}

		public void setComplaintType(String complaintType) {
			this.complaintType = complaintType;
		}
		
		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		@Override
	    public int count(Map<String, FilterMeta> filterBy) {
	        // This method can be implemented if you want to return total row count
	        return getRowCount();
	    }
		 

		
	
}
