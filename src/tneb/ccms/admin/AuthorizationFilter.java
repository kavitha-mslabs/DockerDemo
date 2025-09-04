package tneb.ccms.admin;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import tneb.ccms.admin.valuebeans.AdminUserValueBean;
import tneb.ccms.admin.valuebeans.CallCenterUserValueBean;

public class AuthorizationFilter implements Filter {


	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		HttpSession session = request.getSession(false);
		
		if (session == null) {
			response.sendRedirect(request.getContextPath());
		} else {
			if(request.getRequestURI().contains("faces/callcenter/")) {
				 if(session.getAttribute("sessionCallCenterUserValueBean") != null) {
						CallCenterUserValueBean callCenterUserValueBean =  (CallCenterUserValueBean) session.getAttribute("sessionCallCenterUserValueBean");
						if(callCenterUserValueBean.getRoleId() != 1) {
							if(request.getRequestURI().contains("faces/callcenter/manage/")) {
								response.sendRedirect(request.getContextPath()+"/faces/callcenter/dashboard.xhtml");
							}
						}
						filterChain.doFilter(servletRequest, servletResponse);
					} else {
						response.sendRedirect(request.getContextPath()+"/faces/associatelogin.xhtml");
					}
			}
			if(request.getRequestURI().contains("faces/admin/")) {
				if(session.getAttribute("sessionAdminValueBean") != null) {
					AdminUserValueBean adminUserValueBean =  (AdminUserValueBean) session.getAttribute("sessionAdminValueBean");
					if(adminUserValueBean.getRoleId() != 9) {
						if(request.getRequestURI().contains("faces/admin/manage/")) {
							response.sendRedirect(request.getContextPath()+"/faces/admin/dashboard.xhtml");
						}
					}					
					filterChain.doFilter(servletRequest, servletResponse);
				} else {
					response.sendRedirect(request.getContextPath());
				}
			}
		}
	}

}