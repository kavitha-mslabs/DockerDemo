package tneb.ccms.admin;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import tneb.ccms.admin.valuebeans.AdminUserValueBean;
import tneb.ccms.admin.valuebeans.CallCenterUserValueBean;

@SuppressWarnings("serial")
public class PageTrackingPhaseListener implements javax.faces.event.PhaseListener {

    @Override
    public void afterPhase(PhaseEvent event) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
       // String viewId = facesContext.getViewRoot().getViewId();

        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
        
        if (session != null && session.getAttribute("sessionAdminValueBean") != null) {
        	AdminUserValueBean admin = (AdminUserValueBean) session.getAttribute("sessionAdminValueBean");
        	
        	String viewId = (facesContext.getViewRoot() != null) 
                    ? facesContext.getViewRoot().getViewId()
                    : null;
        	
        	if(viewId ==null) {
        		if(admin.getRoleId()==10) {
            		session.setAttribute("lastPage", "/faces/admin/dashboardminnagam.xhtml");
        		}else {
            		session.setAttribute("lastPage", "/faces/admin/dashboard.xhtml");
        		}
        	}else {
        		session.setAttribute("lastPage", viewId);
        	}
            
        }
        if (session != null && session.getAttribute("sessionCallCenterUserValueBean") != null) {
        	
        	
        	String viewIds = (facesContext.getViewRoot() != null) 
                    ? facesContext.getViewRoot().getViewId()
                    : null;
        	
        	if(viewIds ==null) {
        		session.setAttribute("lastPage", "/faces/callcenter/dashboard.xhtml");
        		
        		
        	}else {
        		session.setAttribute("lastPage", viewIds);
        	}
        }
    }

    @Override
    public void beforePhase(PhaseEvent event) {}

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }
}

