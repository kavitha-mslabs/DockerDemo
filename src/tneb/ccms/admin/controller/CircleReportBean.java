package tneb.ccms.admin.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.transaction.Transactional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tneb.ccms.admin.model.TmpCtCirBean;
import tneb.ccms.admin.model.TmpCtSecBean;
import tneb.ccms.admin.util.CCMSConstants;
import tneb.ccms.admin.util.DataModel;
import tneb.ccms.admin.util.HibernateUtil;

@Named
@ViewScoped
public class CircleReportBean implements Serializable {
	
	private Logger logger = LoggerFactory.getLogger(CircleReportBean.class.getName());

    private static final long serialVersionUID = 1L;
    private SessionFactory sessionFactory;
    private DataModel dmFilter = new DataModel();
    private List<TmpCtCirBean> reports = new ArrayList<>();
    private List<TmpCtSecBean> sectionList = new ArrayList<>();

	String selectedCircleCode;

    public String getSelectedCircleCode() {
		return selectedCircleCode;
	}

	public void setSelectedCircleCode(String selectedCircleCode) {
		this.selectedCircleCode = selectedCircleCode;
	}

	public List<TmpCtSecBean> getSectionList() {
        return sectionList;
    }

    public void setSectionList(List<TmpCtSecBean> sectionList) {
        this.sectionList = sectionList;
    }

    @PostConstruct
    public void init() {
        
        sessionFactory = HibernateUtil.getSessionFactory();
        setDefaultDatesIfNeeded();
        search();

        FacesContext context = FacesContext.getCurrentInstance();
        this.selectedCircleCode = (String) context.getExternalContext().getFlash().get("selectedCircleCode");
        this.sectionList = (List<TmpCtSecBean>) context.getExternalContext().getFlash().get("sectionList");
        
    }

    private void setDefaultDatesIfNeeded() {
        if (dmFilter.getFromDate() == null) {
            Calendar cal = Calendar.getInstance();
            cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0); // Default to 2023-01-01
            dmFilter.setFromDate(cal.getTime());
        }
        if (dmFilter.getToDate() == null) {
            dmFilter.setToDate(new Date()); // Default to today
        }
    }

    
    @Transactional
    public void search() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String formattedFromDate = sdf.format(dmFilter.getFromDate());
            String formattedToDate = sdf.format(dmFilter.getToDate());
            
            session.createNativeQuery("BEGIN CAT_CIR_ABST_ALL_DT(TO_DATE(:fromDate, 'YYYY-MM-DD'), TO_DATE(:toDate, 'YYYY-MM-DD')); END;")
                    .setParameter("fromDate", formattedFromDate)
                    .setParameter("toDate", formattedToDate)
                    .executeUpdate();

            session.createNativeQuery("BEGIN CAT_CIR_ABST_SINGLE_ALL; END;").executeUpdate();

            session.flush();
            session.getTransaction().commit();

            fetchReports(session);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(ExceptionUtils.getStackTrace(e));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", CCMSConstants.TECH_ERROR));
          
        }
    }

    @Transactional
    private void fetchReports(Session session) {
        try {
            String hql = "SELECT REGCODE, REGNAME, CIRCODE, CIRNAME, BLTOT, BLCOM, BLPEN, METOT, MECOM, MEPEN, PFTOT, PFCOM, PFPEN, VFTOT, VFCOM, VFPEN, FITOT,FICOM, FIPEN, THTOT, THCOM, THPEN, TETOT, TECOM, TEPEN, CSTOT, CSCOM, CSPEN, OTTOT, OTCOM, OTPEN FROM TMP_CT_CIR"; // Explicitly list columns
            List<Object[]> results = session.createNativeQuery(hql).getResultList();
            reports = new ArrayList<>();
            
            for (Object[] row : results) {
                TmpCtCirBean report = new TmpCtCirBean();
                report.setRegcode((String) row[0]);
                report.setRegname((String) row[1]);
                report.setCircode((String) row[2]);
                report.setCirname((String) row[3]);

                report.setBlTot((BigDecimal) row[4]);  
                report.setBlCom((BigDecimal) row[5]);
                report.setBlPen((BigDecimal) row[6]);
                report.setMeTot((BigDecimal) row[7]);
                report.setMeCom((BigDecimal) row[8]);
                report.setMePen((BigDecimal) row[9]);
                report.setPfTot((BigDecimal) row[10]);
                report.setPfCom((BigDecimal) row[11]);
                report.setPfPen((BigDecimal) row[12]);
                report.setVfTot((BigDecimal) row[13]);
                report.setVfCom((BigDecimal) row[14]);
                report.setVfPen((BigDecimal) row[15]);
                report.setFiTot((BigDecimal) row[16]);
                report.setFiCom((BigDecimal) row[17]);
                report.setFiPen((BigDecimal) row[18]);
                report.setThTot((BigDecimal) row[19]);
                report.setThCom((BigDecimal) row[20]);
                report.setThPen((BigDecimal) row[21]);
                report.setTeTot((BigDecimal) row[22]);
                report.setTeCom((BigDecimal) row[23]);
                report.setTePen((BigDecimal) row[24]);
                report.setCsTot((BigDecimal) row[25]);
                report.setCsCom((BigDecimal) row[26]);
                report.setCsPen((BigDecimal) row[27]);
                report.setOtTot((BigDecimal) row[28]);
                report.setOtCom((BigDecimal) row[29]);
                report.setOtPen((BigDecimal) row[30]);

                reports.add(report);
            }

            System.out.println("COUNT: " + reports.size());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in fetching report");
        }
    }

    @Transactional
    public void fetchReportByCircle(String circleCode) {
    	
        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String formattedFromDate = sdf.format(dmFilter.getFromDate());
            String formattedToDate = sdf.format(dmFilter.getToDate());

            session.createNativeQuery("BEGIN CAT_SEC_ABST_ALL_DT_CIR(:circd, TO_DATE(:fromDate, 'YYYY-MM-DD'), TO_DATE(:toDate, 'YYYY-MM-DD')); END;")
                    .setParameter("circd", circleCode)
                    .setParameter("fromDate", formattedFromDate)
                    .setParameter("toDate", formattedToDate)
                    .executeUpdate();

            session.createNativeQuery("BEGIN CAT_SEC_ABST_SINGLE_ALL; END;").executeUpdate();

            session.flush();
            session.getTransaction().commit();

            Query<TmpCtSecBean> query = session.createNativeQuery("SELECT c.* FROM TMP_CT_SEC c", TmpCtSecBean.class);
            List<TmpCtSecBean> circleSection = query.getResultList();

            sectionList = circleSection.stream().filter(circle -> circle.getCirCode().equalsIgnoreCase(circleCode)).collect(Collectors.toList());

            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("selectedCircleCode", dmFilter.getCirCode());
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("sectionList", sectionList);
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("circleCode", circleCode);

            FacesContext.getCurrentInstance().getExternalContext().redirect("sectionReport.xhtml");
        } catch (Exception e) {
            e.printStackTrace();
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

    public List<TmpCtCirBean> getReports() {
        return reports;
    }

    public void setReports(List<TmpCtCirBean> reports) {
        this.reports = reports;
    }
}