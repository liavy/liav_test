package com.sap.security.core.admin;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sap.security.api.IUser;
import com.sap.security.api.UMFactory;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.ResourceBean;

public class ServletAccessToLogic implements IAccessToLogic {
    public final static String  VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/ServletAccessToLogic.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static IUMTrace trace = null;

    static {
        trace = InternalUMFactory.getTrace(VERSIONSTRING);
    } // static

    private HttpServletRequest req;
    private HttpServletResponse resp;
    private HttpSession session;

    public ServletAccessToLogic (HttpServletRequest req, HttpServletResponse resp) {
        this.req = req;
        this.resp = resp;
        this.session = req.getSession();
    } // servletAccessToLogic

    public Object getSessionAttribute (String attrName) {
        return this.session.getAttribute(attrName);
    } // getSessionAttribute

    public Object getRequestAttribute (String attrName) {
        return this.req.getAttribute(attrName);
    } // getReuqestAttribute

    public void setSessionAttribute (String attrName, Object attrValue) {
        this.session.setAttribute(attrName, attrValue);
    } // setSessionAttribute

    public void setRequestAttribute (String attrName, Object attrValue) {
        this.req.setAttribute(attrName, attrValue);
    } // setRequestAttribute

    public boolean isSessionNew() {
        return this.session.isNew();
    } // isSessionNew

    public HttpServletRequest getServletRequest() {
        return this.req;
    } // getServletRequest

    public HttpServletResponse getServletResponse() {
        return this.resp;
    } // getServletResponse

    public String getRequestParameter (String parName) {
        return this.req.getParameter(parName);
    } // getRequestParameter
	
    public void gotoPage (String page) throws AccessToLogicException, java.io.IOException {
		this.doForwarding(page, false);
    } // gotoPage

    /*gotoAddr forward directly to the passed address and does not treat the page as key*/
    public void gotoAddr (String addr) throws AccessToLogicException, IOException {
    	this.doForwarding(addr, true);
    } // gotoAddr

    public String getRequestURI() {
        return this.req.getRequestURI();
    } // getRequestURI

    public String getSessionId() {
        return this.session.getId();
    } // getSessionId

    public String[] getRequestParameterValues(String s) {
        return this.req.getParameterValues(s);
    } // getRequestParameterValues

    public String getRequestHeader(String s) {
        return this.req.getHeader(s);
    } // getRequestHeader

    public void setResponseContentType(String s) {
        this.resp.setContentType(s);
    } // setResponseContentType

    public void setRequestCharacterEncoding(String s) throws java.io.UnsupportedEncodingException {
        this.req.setCharacterEncoding(s);
    } // setRequestCharacterEncoding


    public void setResponseHeader(String s, String val) {
        this.resp.setHeader(s, val);
    } // setResponseHeader

    public java.io.PrintWriter getResponseWriter() throws java.io.IOException {
        return this.resp.getWriter();
    } // getResponseWriter

    public Enumeration getRequestParameterNames() {
        return this.req.getParameterNames();
    } // getRequestParametterNames

    public Enumeration getRequestAttributeNames() {
        return this.req.getAttributeNames();
    } // getRequestAttributeNames

    public Enumeration getSessionAttributeNames() {
        return session.getAttributeNames();
    } // getSessionAttributeNames

    public String getRequestCharacterEncoding () {
        return this.req.getCharacterEncoding();
    } // getRequestCharacterEncoding

    public String getContextURI () {
        return this.req.getContextPath();
    } // getContextURI

    public String getContextURI (String component) {
        //component name passed is ignored in the servlet implementation
        return this.getContextURI();
    } // getContextURI

    public String enforceContextURI (String component) {
        //component name passed is ignored in the servlet implementation
        return this.getContextURI();
    } // getContextURI

    public IUser getActiveUser() {
        return UMFactory.getAuthenticator().forceLoggedInUser(this.req, this.resp);
    } // getActiveUser

    public Object getSession() {
        return this.req.getSession();
    } // getSession

    public Object getSession(boolean flag) {
        return this.req.getSession(flag);
    } // getSession(boolean)

    public void removeSessionAttribute(String att) {
        this.session.removeAttribute(att);
    } // removeSessionAttribute

    public java.util.Locale getRequestLocale() {
        return this.req.getLocale();
    } // getReqeustLocale

    public void sendRedirect(String url) throws java.io.IOException {
        this.resp.sendRedirect(url);
    } // sendRedirect

    public void sessionInvalidate() {
        this.session.invalidate();
    } // sessionInvalidate

    HttpServletRequest getRequest() {
        return this.req;
    } // getRequest

    HttpServletResponse getResponse() {
        return this.resp;
    } // getResponse

    public String getQueryString() {
        return this.req.getQueryString();
    } //getQueryString

    public java.util.Locale getLocale() {
    	IUser user = UMFactory.getAuthenticator().getLoggedInUser(this.req, this.resp);
    	if ( null == user ) return this.getRequestLocale();
		Locale locale = user.getLocale();
		if ( locale == null ) {
			return this.getRequestLocale();
		} else {
			return locale; 
		} 
    }

    public void setGlobalSessionAttribute (String attrName, Object attrValue) {
      this.setSessionAttribute(attrName, attrValue);
    }

    public Object getGlobalSessionAttribute (String attrName) {
		return this.getSessionAttribute(attrName);
    }    

	private void doForwarding(String target, boolean isPathIncluded) 
		throws AccessToLogicException, java.io.IOException{
		String methodName = "gotoPage(String)";
		trace.entering(methodName, new String[]{target});

		String addr = target;
		if ( !isPathIncluded ) {
			ResourceBean localeBean = (ResourceBean) this.getSessionAttribute(UserAdminLocaleBean.beanId);
			if ( null == localeBean ) {
				localeBean = UserAdminLocaleBean.getInstance(this.getLocale());
			}

			addr = localeBean.getPage(target);			
		}

		trace.debugT(methodName, "found physical page name: ", new String[]{addr});

		// Workaround:
		// Since relative addresses (without leading backslash) do not work with
		// InQMy. We convert relative address to absolute addresses. Since this
		// is a hack, we will print a severe trace message on each occurance of
		// a relative address.
		if ( !addr.startsWith("/") ) {
			trace.warningT(methodName, "relative addr \"" + addr + "\" found");

			// make relative to absolute
			addr = "/" + addr;
		}

		try {
			this.req.getRequestDispatcher(addr).forward(this.req, this.resp);
		} catch (ServletException ex) {
			trace.errorT(methodName, "Can't do forwarding because of ", ex);
			if ( null != ex.getRootCause() ) {
				trace.errorT(methodName, "the rootCause of failed forwarding ", ex.getRootCause());
			} 
			throw new AccessToLogicException(ex);            
		} 		
	} // doForwarding    
}