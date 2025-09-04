package tneb.ccms.admin.controller.reportController;

import java.beans.Transient;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import tneb.ccms.admin.model.CategoryBean;
import tneb.ccms.admin.model.CompDeviceBean;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.HibernateUtil;
import tneb.ccms.admin.valuebeans.AdminUserValueBean;
import tneb.ccms.admin.valuebeans.CallCenterUserValueBean;
import tneb.ccms.admin.valuebeans.CategoriesValueBean;
import tneb.ccms.admin.valuebeans.CompDeviceValueBean;
import tneb.ccms.admin.valuebeans.CurrentDateComplaintsValueBean;

@Named
@ViewScoped
public class CurrentDateCallsReport implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	DataModel dmFilter ;
	private SessionFactory sessionFactory;
	private boolean initialized = false;
	List<CurrentDateComplaintsValueBean> sectionCounts=null;
	StreamedContent excelFile;
	StreamedContent pdfFile;
	List<CompDeviceValueBean> devices;
	public List<CompDeviceValueBean> getDevices() {
		return devices;
	}

	public void setDevices(List<CompDeviceValueBean> devices) {
		this.devices = devices;
	}

	List<CategoriesValueBean> categories;


	public List<CategoriesValueBean> getCategories() {
		return categories;
	}

	public void setCategories(List<CategoriesValueBean> categories) {
		this.categories = categories;
	}

	public List<CompDeviceValueBean> getDevice() {
		return devices;
	}

	public void setDevice(List<CompDeviceValueBean> devices) {
		this.devices = devices;
	}
	
	@PostConstruct
	public void init() {
		System.out.println("Initializing CURRENT DATE RECEIVED CALLS ABSTARCT...");
		sessionFactory = HibernateUtil.getSessionFactory();
		dmFilter = new DataModel();
		loadAllDevicesAndCategories();
		
	}
	
	public void resetIfNeeded() {
        if (!FacesContext.getCurrentInstance().isPostback() && initialized) {
        	sectionCounts = null;
        	dmFilter.setFromDate(null);
        	dmFilter.setToDate(null);
        }
        initialized = true;
    }
	
	@SuppressWarnings("unchecked")
	@Transactional
	private void loadAllDevicesAndCategories() {
	    Session session = null;
	    try {
	        session = sessionFactory.openSession();
	        
	     // Get devices ordered by ID
	        String hql = "FROM CompDeviceBean d ORDER BY d.id ASC";
	        Query<CompDeviceBean> query = session.createQuery(hql, CompDeviceBean.class);
	        List<CompDeviceBean> devicesBean = query.getResultList();

	        // Convert to value beans
	        List<CompDeviceValueBean> orderedDeviceList = devicesBean.stream()
	                .map(CompDeviceValueBean::convertCompDeviceBeanToCompDeviceValueBean)
	                .collect(Collectors.toList());

	        // Add "ALL" as the first item
	        CompDeviceValueBean allOption = new CompDeviceValueBean("L", "ALL");
	        orderedDeviceList.add(0, allOption);

	        // Get session beans
	        HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
	                .getExternalContext().getSession(false);
	        AdminUserValueBean adminUserValueBean = (AdminUserValueBean) httpsession.getAttribute("sessionAdminValueBean");
	        CallCenterUserValueBean callCenterValueBean = (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");

	        // Apply filtering based on role
	        if (callCenterValueBean != null) {
	            int roleId = callCenterValueBean.getRoleId();

	            if (roleId == 5 || roleId == 7) {
	                // Minnagam Admin or Circle Agent – only Minnagam device
	                devices = orderedDeviceList.stream()
	                        .filter(d -> "M".equals(d.getDeviceCode()))
	                        .collect(Collectors.toList());

	            } else if (roleId == 3) {
	                // Social Media User – only SM
	                devices = orderedDeviceList.stream()
	                        .filter(d -> "S".equals(d.getDeviceCode()))
	                        .collect(Collectors.toList());

	            } else {
	                // Other call center roles – show all
	                devices = orderedDeviceList;
	            }

	        } else if (adminUserValueBean != null) {
	            int roleId = adminUserValueBean.getRoleId();

	            if (roleId == 10) {
	                // Minnagam Admin (roleId 10) – only Minnagam device
	                devices = orderedDeviceList.stream()
	                        .filter(d -> "M".equals(d.getDeviceCode()))
	                        .collect(Collectors.toList());

	            } else {
	                // Other admin roles – show all
	                devices = orderedDeviceList;
	            }

	        } else {
	            // Default fallback – show all
	            devices = orderedDeviceList;
	        }

	        String hql2 = "FROM CategoryBean d WHERE d.code IS NOT NULL ORDER BY d.id ASC ";
	        Query<CategoryBean> query2 = session.createQuery(hql2, CategoryBean.class);
	        
	        List<CategoryBean> categoryBean = query2.getResultList();
	        
	        categories = categoryBean.stream().map(CategoriesValueBean::convertCategoriesBeanToCategoriesValueBean).collect(Collectors.toList());
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        devices = new ArrayList<>(); 
	        categories = new ArrayList<>();
	    } finally {
	        if (session != null && session.isOpen()) {
	            session.close();
	        }
	    }
	}
	
	
	@Transactional
	public void getCurrentDateReceivedCalls() {
	    try (Session session = sessionFactory.openSession()) {
	        String hours = dmFilter.getHours();
	        Integer noOfOffices = dmFilter.getNoOfOffices();
	        
	        List<String> complaintTypes = new ArrayList<>();
	        List<String> devices = new ArrayList<>();
	        
	        if (dmFilter.getComplaintType() != null) {
	            switch (dmFilter.getComplaintType().toUpperCase()) {
	                case "BL": complaintTypes.add("BL"); break;
	                case "ME": complaintTypes.add("ME"); break;
	                case "PF": complaintTypes.add("PF"); break;
	                case "VF": complaintTypes.add("VF"); break;
	                case "FI": complaintTypes.add("FI"); break;
	                case "TH": complaintTypes.add("TH"); break;
	                case "TE": complaintTypes.add("TE"); break;
	                case "OT": complaintTypes.add("OT"); break;
	                case "CS": complaintTypes.add("CS"); break;
	                case "AL": complaintTypes.addAll(Arrays.asList("BL","ME","PF","VF","FI","TH","TE","OT","CS")); break;
	                default: throw new IllegalArgumentException("Invalid Complaint Type");
	            }
	        }
			
			if (dmFilter.getDevice() != null) {
	            switch (dmFilter.getDevice().toUpperCase()) {
	                case "P": devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile")); break;
	                case "W": devices.add("web"); break;
	                case "S": devices.add("SM"); break;
	                case "A": devices.addAll(Arrays.asList("admin", "FOC")); break;
	                case "M": devices.add("MI"); break;
	                case "G": devices.add("MM"); break;
	                case "O": devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile","web","SM","MI","MM")); break;
	                case "L": devices.addAll(Arrays.asList("IMOB", "iOS", "Android", "AMOB", "mobile","web","SM","admin","FOC","MI","MM")); break;
	                default: throw new IllegalArgumentException("Invalid Device Type");
	            }
	        }
	        
			StringBuilder hql = new StringBuilder("SELECT a.section_id as sec, "
	                       + "j.name as SectionName, "
	                       + "h.name as DivisionName, "
	                       + "g.name as CircleName,  "
	                       + "f.id as REGION_ID,g.id as CIRCLE_ID, h.id as DIVISION_ID , i.id as SUB_DIVISION_ID,  "
	                       + "count(*) as cnt "
	                       + "FROM COMPLAINT a "
	                       + "JOIN REGION f ON a.region_id = f.id "
	                       + "JOIN CIRCLE g ON a.circle_id = g.id "
	                       + "JOIN DIVISION h ON a.division_id = h.id "
	                       + "JOIN SUB_DIVISION i ON a.sub_division_id = i.id "
	                       + "JOIN SECTION j ON a.section_id = j.id "
	                       + "WHERE a.created_on > SYSDATE-(:nohr/24) ");
	                     
			
			
			if (!complaintTypes.isEmpty()) {
	            hql.append(" AND a.complaint_type IN (:complaintTypes)");
	        }

	        if (!devices.isEmpty()) {
	            hql.append(" AND a.device IN (:devices)");
	        }
	        
	        
	        HttpSession httpsession = (HttpSession) FacesContext.getCurrentInstance()
	                .getExternalContext().getSession(false);
			
			AdminUserValueBean adminUserValueBean = (AdminUserValueBean) 
					httpsession.getAttribute("sessionAdminValueBean");
	        CallCenterUserValueBean callCenterValueBean =  (CallCenterUserValueBean) httpsession.getAttribute("sessionCallCenterUserValueBean");
	        
	        
        if (callCenterValueBean != null) {
	            
	            int roleId =callCenterValueBean.getRoleId();
	            
	            if(roleId==1) {
	            	hql.append(" AND a.circle_id IN (:circleIds)");
	            }
	            if(roleId==5) {
	            	hql.append(" AND a.device =:device");
	            }
	            if(roleId==7 ) {
	            	hql.append(" AND a.circle_id IN (:circleIds)");
	            	hql.append(" AND a.device =:device");
	            }
	            if(roleId==3) {
	            	hql.append(" AND a.device =:device");
	            }
	        } 
	        else if (adminUserValueBean != null) {
	            Integer roleId = adminUserValueBean.getRoleId();
	            
	            // Add role-based conditions directly in SQL
	            if (roleId >= 6 && roleId <= 9) {
	                // HEAD QUARTERS - no additional filtering needed
	            } 
	            else if (roleId == 5) { // REGION
	                hql.append(" AND a.region_id = :regionId");
	            } 
	            else if (roleId == 4) { // CIRCLE
	            	hql.append(" AND a.region_id = :regionId AND a.circle_id = :circleId");
	            } 
	            else if (roleId == 3) { // DIVISION
	            	hql.append(" AND a.region_id = :regionId AND a.circle_id = :circleId " +
	                           "AND a.division_id = :divisionId");
	            } 
	            else if (roleId == 2) { // SUB DIVISION
	            	hql.append(" AND a.region_id = :regionId AND a.circle_id = :circleId " +
	                           "AND a.division_id = :divisionId AND a.sub_division_id = :subDivisionId");
	            } 
	            else if (roleId == 1) { // SECTION
	            	hql.append(" AND a.region_id = :regionId AND a.circle_id = :circleId " +
	                           "AND a.division_id = :divisionId AND a.sub_division_id = :subDivisionId " +
	                           "AND a.section_id = :sectionId");
	            } 
	            else if (roleId == 10) { // M ADMIN
	            	hql.append(" AND a.device = 'MI'");
	            }
	        }
	        
	        hql.append(" GROUP BY a.section_id, j.name, h.name, g.name,f.id, g.id, h.id, i.id")
	           .append(" ORDER BY cnt DESC");
	        

	        Query query = session.createNativeQuery(hql.toString())
	                           .setParameter("nohr", hours);
	        
	        if (!complaintTypes.isEmpty()) {
	            query.setParameter("complaintTypes", complaintTypes);
	        }

	        if (!devices.isEmpty()) {
	            query.setParameter("devices", devices);
	        }

	        if (noOfOffices != null) {
	            query.setMaxResults(noOfOffices);
	        }
	        
	        if (callCenterValueBean != null ) {
	        	List<Integer> circleIds = session.createQuery(
	                    "select c.circleBean.id from CallCenterMappingBean c " +
	                    "where c.callCenterUserBean.id = :userId", Integer.class)
	                    .setParameter("userId", callCenterValueBean.getId())
	                    .getResultList();
	        	int roleId = callCenterValueBean.getRoleId();
	        	if(roleId==1) {
		            query.setParameter("circleIds", circleIds);
	        	}
	        	if(roleId==5) {
		            query.setParameter("device", "MI");
	        	}
	        	if(roleId==7) {
		            query.setParameter("circleIds", circleIds);
		            query.setParameter("device", "MI");
	        	}
	        	if(roleId==3) {
		            query.setParameter("device", "SM");
	        	}
	        } 
	        else if (adminUserValueBean != null) {
	            Integer roleId = adminUserValueBean.getRoleId();
	            if (roleId == 5) {
	                query.setParameter("regionId", adminUserValueBean.getRegionId());
	            } 
	            else if (roleId == 4) {
	                query.setParameter("regionId", adminUserValueBean.getRegionId());
	                query.setParameter("circleId", adminUserValueBean.getCircleId());
	            } 
	            else if (roleId == 3) {
	                query.setParameter("regionId", adminUserValueBean.getRegionId());
	                query.setParameter("circleId", adminUserValueBean.getCircleId());
	                query.setParameter("divisionId", adminUserValueBean.getDivisionId());
	            } 
	            else if (roleId == 2) {
	                query.setParameter("regionId", adminUserValueBean.getRegionId());
	                query.setParameter("circleId", adminUserValueBean.getCircleId());
	                query.setParameter("divisionId", adminUserValueBean.getDivisionId());
	                query.setParameter("subDivisionId", adminUserValueBean.getSubDivisionId());
	            } 
	            else if (roleId == 1) {
	                query.setParameter("regionId", adminUserValueBean.getRegionId());
	                query.setParameter("circleId", adminUserValueBean.getCircleId());
	                query.setParameter("divisionId", adminUserValueBean.getDivisionId());
	                query.setParameter("subDivisionId", adminUserValueBean.getSubDivisionId());
	                query.setParameter("sectionId", adminUserValueBean.getSectionId());
	            }
	        }

	        List<Object[]> results = query.getResultList();

	        List<CurrentDateComplaintsValueBean> resultList = results.stream()
	            .map(row -> {
	                CurrentDateComplaintsValueBean bean = new CurrentDateComplaintsValueBean();
	                bean.setSectionId(((BigDecimal) row[0]).intValue());
	                bean.setSectionName((String) row[1]);
	                bean.setDivisionName((String) row[2]);
	                bean.setCircleName((String) row[3]);
	                bean.setRegionId(((BigDecimal) row[4]).intValue());
	                bean.setCircleId(((BigDecimal) row[5]).intValue());
	                bean.setDivisionId(((BigDecimal) row[6]).intValue());
	                bean.setSubDivisionId(((BigDecimal) row[7]).intValue());
	                bean.setReceivedComplaints(((BigDecimal) row[8]).longValue());
	                
	                return bean;
	            })
	            .collect(Collectors.toList());
	        

//	        Integer roleId = adminUserValueBean.getRoleId();
//	        Integer regionID = adminUserValueBean.getRegionId();
//	        Integer circleID = adminUserValueBean.getCircleId();
//	        Integer divisionID = adminUserValueBean.getDivisionId();
//	        Integer subDivisionID = adminUserValueBean.getSubDivisionId();
//	        Integer sectionID = adminUserValueBean.getSectionId();
//	        
//	        // HEAD QUATERS
//	        if(roleId>=6 && roleId<=9) {
//	        	sectionCounts = resultList;
//	        }
//	        //REGION
//	        else if(roleId==5) {      
//	        	sectionCounts = resultList.stream().filter(r ->r.getRegionId().equals(regionID)).collect(Collectors.toList());
//	        }
//	        //CIRCLE
//	        else if(roleId==4) {
//	        	sectionCounts = resultList.stream().filter(r ->r.getRegionId().equals(regionID)).filter(c ->c.getCircleId().equals(circleID)).collect(Collectors.toList());
//	        }
//	        //DIVISION
//	        else if(roleId==3) {
//	        	sectionCounts = resultList.stream().filter(r ->r.getRegionId().equals(regionID)).filter(c ->c.getCircleId().equals(circleID))
//	        			.filter(d ->d.getDivisionId().equals(divisionID)).collect(Collectors.toList());
//	        }
//	      //SUB DIVISION
//	        else if(roleId==2) {
//	        	sectionCounts = resultList.stream().filter(r ->r.getRegionId().equals(regionID)).filter(c ->c.getCircleId().equals(circleID))
//	        			.filter(d ->d.getDivisionId().equals(divisionID)).filter(sd ->sd.getSubDivisionId().equals(subDivisionID)).collect(Collectors.toList());
//	        }
//	      //SECTION
//	        else if(roleId==1) {
//	        	
//	        	System.out.println("THE RESULT LIST SIZE ---------------"+resultList.size());
//	        	System.out.println("THE RESULT LIST REGION--------"+resultList.get(0).getRegionId());
//	        	System.out.println("THE RESULT LIST CIRCLE--------"+resultList.get(0).getCircleId());
//	        	System.out.println("THE RESULT LIST DVISION--------"+resultList.get(0).getDivisionId());
//	        	System.out.println("THE RESULT LIST SUB D--------"+resultList.get(0).getSubDivisionId());
//	        	System.out.println("THE RESULT LIST secton--------"+resultList.get(0).getSectionId());
//
//
//	        	sectionCounts = resultList.stream().filter(r ->r.getRegionId().equals(regionID)).filter(c ->c.getCircleId().equals(circleID))
//	        			.filter(d ->d.getDivisionId().equals(divisionID)).filter(sd ->sd.getSubDivisionId().equals(subDivisionID))
//	        			.filter(sec->sec.getSectionId().equals(sectionID)).collect(Collectors.toList());
//	        	
//	        	System.out.println("THE REGION ID ----"+regionID);
//	        	System.out.println("THE CIRCL ID ------+ "+ circleID);
//	        	System.out.println("THE DIV ID ------+ "+ divisionID);
//	        	System.out.println("THE SD ID ------+ "+ subDivisionID);
//	        	System.out.println("THE SEC ID ------+ "+ sectionID);
//
//	        }
//	        else {
//	        	sectionCounts = resultList;
//	        }
//	        }else if(callCenterValueBean!=null) {
//	        	int userId = callCenterValueBean.getId();
//		    	@SuppressWarnings("unchecked")
//				List<Integer> circleId = session.createQuery(
//                        "select c.circleBean.id from CallCenterMappingBean c " +
//                        "where c.callCenterUserBean.id = :userId")
//                        .setParameter("userId", userId)
//                        .getResultList();
//		    	
//		    	int roleId = callCenterValueBean.getRoleId();
//		    	if(roleId==1) {
//		    		sectionCounts = resultList.stream().filter(c->circleId.contains(c.getCircleId())).collect(Collectors.toList());	
//		    	}
//		    	if(roleId==5) {
//		    		sectionCounts = resultList.stream().filter(d ->d.get)
//		    	}
	        	
	         sectionCounts=resultList;
	        System.out.println("THE SECTION COUNT SIZE --------" + sectionCounts.size());
	        
	        FacesContext.getCurrentInstance()
	                   .getExternalContext()
	                   .redirect("currentDateReceivedCalls.xhtml");
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
public void redirectToAbstractReportListingPage() throws IOException {
	FacesContext.getCurrentInstance().getExternalContext().redirect("abstractReportsListing.xhtml");

}
	
	public void redirectToHome() throws IOException {
		 dmFilter = new DataModel();
		FacesContext.getCurrentInstance().getExternalContext().redirect("currentDateComplaintFilter.xhtml");
	}
	
	public void exportToExcel(List<CurrentDateComplaintsValueBean> resultList) {
	    try (HSSFWorkbook workbook = new HSSFWorkbook()) {
	        Sheet sheet = workbook.createSheet("Current_Date_Received_Calls_Abstract_Report");
	        
	        CellStyle headingStyle = workbook.createCellStyle();
		    HSSFFont headingFont = workbook.createFont();
		    headingFont.setBold(true);
		    headingFont.setFontHeightInPoints((short) 10);
		    headingStyle.setFont(headingFont);
		    headingStyle.setAlignment(HorizontalAlignment.CENTER);
		    headingStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		    headingStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		    headingStyle.setBorderBottom(BorderStyle.THIN);
		    headingStyle.setBorderTop(BorderStyle.THIN);
		    headingStyle.setBorderLeft(BorderStyle.THIN);
		    headingStyle.setBorderRight(BorderStyle.THIN);
		    headingStyle.setVerticalAlignment(VerticalAlignment.CENTER);

	        Row headingRow = sheet.createRow(0);
	        Cell headingCell = headingRow.createCell(0);
	        headingCell.setCellValue("CURRENT DATE RECEIVED CALLS ABSTRACT REPORT");
	        headingCell.setCellStyle(headingStyle);
	        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4)); 
	        
	        Row dateRow = sheet.createRow(1);
		    Cell dateCell = dateRow.createCell(0);
		    
		    
		    CellStyle dateStyle = workbook.createCellStyle();
		    HSSFFont dateFont = workbook.createFont();
		    dateFont.setBold(true);
		    dateFont.setFontHeightInPoints((short) 10);
		    dateStyle.setFont(dateFont);
		    dateStyle.setAlignment(HorizontalAlignment.CENTER);
		    dateStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		    
String complaintType = dmFilter.getComplaintType();
		    
		    Map<String, String> complaintTypeMap = new HashMap<>();
		    complaintTypeMap.put("AL", "ALL");
		    complaintTypeMap.put("PF", "POWER FAILURE");
		    complaintTypeMap.put("VF", "VOLTAGE RELATED");
		    complaintTypeMap.put("ME", "METER RELATED");
		    complaintTypeMap.put("BL", "BILLING RELATED");
		    complaintTypeMap.put("FI", "FIRE");
		    complaintTypeMap.put("TH", "DANGEROUS POLE");
		    complaintTypeMap.put("TE", "THEFT OF POWER");
		    complaintTypeMap.put("CS", "CONDUCTOR SNAPPING");
		    complaintTypeMap.put("OT", "OTHERS");
		    
		    complaintType = complaintTypeMap.getOrDefault(complaintType, "-");
		    
		    String device = dmFilter.getDevice();

		    Map<String, String> deviceMap = new HashMap<>();
		    deviceMap.put("L", "ALL");
		    deviceMap.put("P", "MOBILE");
		    deviceMap.put("W", "WEB");
		    deviceMap.put("S", "SM");
		    deviceMap.put("A", "FOC");
		    deviceMap.put("M", "MINNAGAM");
		    deviceMap.put("G", "MM");
		    
		    device = deviceMap.getOrDefault(device, "-");
		    
		    dateCell.setCellValue("Received Within: " + dmFilter.getHours() +" | "+ "  No.Of.Offices: " + dmFilter.getNoOfOffices() +" | "+" Device :"+device +" | "+" Complaint Type :"+complaintType);
		    dateCell.setCellStyle(dateStyle);
		    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));

	        Row headerRow = sheet.createRow(2);
	        String[] headers = {"S.No", "Section Name", "Division Name", "Circle Name", "Received Complaints"};

	        for (int i = 0; i < headers.length; i++) {
	            Cell cell = headerRow.createCell(i);
	            cell.setCellValue(headers[i]);
	            cell.setCellStyle(headingStyle);
	            sheet.setColumnWidth(i, 25 * 256); 
	        }

	        CellStyle dataStyle = workbook.createCellStyle();
	        dataStyle.setBorderBottom(BorderStyle.THIN);
	        dataStyle.setBorderTop(BorderStyle.THIN);
	        dataStyle.setBorderLeft(BorderStyle.THIN);
	        dataStyle.setBorderRight(BorderStyle.THIN);

	        int rowNum = 3;
	        int serialNo = 1;
	        for (CurrentDateComplaintsValueBean item : resultList) {
	            Row row = sheet.createRow(rowNum++);

	            Cell cell0 = row.createCell(0);
	            cell0.setCellValue(serialNo++);
	            cell0.setCellStyle(dataStyle);

	            Cell cell1 = row.createCell(1);
	            cell1.setCellValue(item.getSectionName());
	            cell1.setCellStyle(dataStyle);

	            Cell cell2 = row.createCell(2);
	            cell2.setCellValue(item.getDivisionName());
	            cell2.setCellStyle(dataStyle);

	            Cell cell3 = row.createCell(3);
	            cell3.setCellValue(item.getCircleName());
	            cell3.setCellStyle(dataStyle);

	            Cell cell4 = row.createCell(4);
	            cell4.setCellValue(item.getReceivedComplaints());
	            cell4.setCellStyle(dataStyle);
	        }

	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        workbook.write(out);

	        excelFile = DefaultStreamedContent.builder()
	            .name("Current_Date_Received_Calls_Abstract_Report.xls")
	            .contentType("application/vnd.ms-excel")
	            .stream(() -> new ByteArrayInputStream(out.toByteArray()))
	            .build();

	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public void exportToPdf(List<CurrentDateComplaintsValueBean> resultList) throws IOException {
		try {
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        Document document = new Document(PageSize.A4.rotate()); // Landscape mode
	        PdfWriter.getInstance(document, out);
	        document.open();
	        
String complaintType = dmFilter.getComplaintType();
		    
		    Map<String, String> complaintTypeMap = new HashMap<>();
		    complaintTypeMap.put("AL", "ALL");
		    complaintTypeMap.put("PF", "POWER FAILURE");
		    complaintTypeMap.put("VF", "VOLTAGE RELATED");
		    complaintTypeMap.put("ME", "METER RELATED");
		    complaintTypeMap.put("BL", "BILLING RELATED");
		    complaintTypeMap.put("FI", "FIRE");
		    complaintTypeMap.put("TH", "DANGEROUS POLE");
		    complaintTypeMap.put("TE", "THEFT OF POWER");
		    complaintTypeMap.put("CS", "CONDUCTOR SNAPPING");
		    complaintTypeMap.put("OT", "OTHERS");
		    
		    complaintType = complaintTypeMap.getOrDefault(complaintType, "-");
		    
		    String device = dmFilter.getDevice();

		    Map<String, String> deviceMap = new HashMap<>();
		    deviceMap.put("L", "ALL");
		    deviceMap.put("P", "MOBILE");
		    deviceMap.put("W", "WEB");
		    deviceMap.put("S", "SM");
		    deviceMap.put("A", "FOC");
		    deviceMap.put("M", "MINNAGAM");
		    deviceMap.put("G", "MM");
		    
		    device = deviceMap.getOrDefault(device, "-");
	        
	        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.DARK_GRAY);
	        Paragraph title = new Paragraph("CURRENT DATE - COMPLAINT RECEIVED ABSTRACT - " + 
	            dmFilter.getHours() + " Hour(s)", titleFont);
	        title.setAlignment(Element.ALIGN_CENTER);
	        title.setSpacingAfter(20f);
	        document.add(title);
	        
	        Font subHeadingFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
	        Paragraph subHeading = new Paragraph("Complaint Type: " + complaintType + "    Device: " + device, subHeadingFont);
	        subHeading.setAlignment(Element.ALIGN_CENTER);
	        subHeading.setSpacingAfter(10f);
	        document.add(subHeading);
	        
	        PdfPTable table = new PdfPTable(5);
	        table.setWidthPercentage(100);
	        table.setSpacingBefore(10f);
	        table.setSpacingAfter(10f);
	        
	        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
	        
	        String[] headers = {"S.No", "Section Name", "Division Name", "Circle Name", "Received Complaints"};
	        for (String header : headers) {
	            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
	            cell.setBackgroundColor(BaseColor.DARK_GRAY);
	            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	            cell.setPadding(8);
	            cell.setBorderWidth(1f);
	            table.addCell(cell);
	        }
	        
	        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
	        
	        int rowNum = 1;
	        for (CurrentDateComplaintsValueBean item : resultList) {
	            addCell(table, String.valueOf(rowNum++), dataFont, Element.ALIGN_CENTER);
	            addCell(table, item.getSectionName(), dataFont, Element.ALIGN_LEFT);
	            addCell(table, item.getDivisionName(), dataFont, Element.ALIGN_LEFT);
	            addCell(table, item.getCircleName(), dataFont, Element.ALIGN_LEFT);
	            addCell(table, String.valueOf(item.getReceivedComplaints()), dataFont, Element.ALIGN_CENTER);
	        }
	        
	        document.add(table);
	        document.close();
	        
	        pdfFile = DefaultStreamedContent.builder()
	            .name("Current_Date_Received_Calls_Abstract_Report.pdf")
	            .contentType("application/pdf")
	            .stream(() -> new ByteArrayInputStream(out.toByteArray()))
	            .build();
	            
	    } catch (DocumentException e) {
	        e.printStackTrace();
	    }
	}
	private void addCell(PdfPTable table, String text, Font font, int alignment) {
	    PdfPCell cell = new PdfPCell(new Phrase(text, font));
	    cell.setHorizontalAlignment(alignment);
	    cell.setPadding(5);
	    cell.setBorderWidth(1f);
	    table.addCell(cell);
	}
	
	public DataModel getDmFilter() {
		return dmFilter;
	}


	public void setDmFilter(DataModel dmFilter) {
		this.dmFilter = dmFilter;
	}


	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}


	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	
	public List<CurrentDateComplaintsValueBean> getSectionCounts() {
		return sectionCounts;
	}

	public void setSectionCounts(List<CurrentDateComplaintsValueBean> sectionCounts) {
		this.sectionCounts = sectionCounts;
	}

	public StreamedContent getExcelFile() {
		return excelFile;
	}

	public void setExcelFile(StreamedContent excelFile) {
		this.excelFile = excelFile;
	}

	public StreamedContent getPdfFile() {
		return pdfFile;
	}

	public void setPdfFile(StreamedContent pdfFile) {
		this.pdfFile = pdfFile;
	}

}
