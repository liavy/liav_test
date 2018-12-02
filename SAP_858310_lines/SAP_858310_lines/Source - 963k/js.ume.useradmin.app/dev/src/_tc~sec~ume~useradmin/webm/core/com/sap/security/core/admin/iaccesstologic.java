package com.sap.security.core.admin;

import java.util.Locale;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.security.api.IUser;

public interface IAccessToLogic {
    public Object getSessionAttribute (String attrName);
    public Object getRequestAttribute (String attrName);

    public void setSessionAttribute (String attrName, Object AttrValue);
    public void setRequestAttribute (String attrName, Object AttrValue);

    public boolean isSessionNew();

    public Object getSession();
    public Object getSession(boolean flag);

    public HttpServletRequest getServletRequest();
    public HttpServletResponse getServletResponse();

    public String getRequestParameter (String parName);

    public void gotoPage (String page) throws AccessToLogicException, java.io.IOException;

    public void gotoAddr (String page) throws AccessToLogicException, java.io.IOException;

    public String getContextURI ();

    public String getContextURI (String component);

    public String getRequestURI();

    public String getSessionId();

    public void setResponseContentType(String s);

    public void setRequestCharacterEncoding(String s) throws java.io.UnsupportedEncodingException;

    public String getRequestHeader(String s);

    public void setResponseHeader(String s, String val);

    public java.io.PrintWriter getResponseWriter() throws java.io.IOException;

    public Enumeration getRequestParameterNames();

    public Enumeration getRequestAttributeNames();

    public Enumeration getSessionAttributeNames();

    public String[] getRequestParameterValues(String s);

    public String getRequestCharacterEncoding ();

    public IUser getActiveUser();

    public void removeSessionAttribute(String att);

    public Locale getRequestLocale();

    public void sendRedirect(String url) throws java.io.IOException;

    public void sessionInvalidate();

    public String getQueryString();

    public java.util.Locale getLocale();

    public Object getGlobalSessionAttribute (String attrName);

    public void setGlobalSessionAttribute (String attrName, Object attrValue);

    public String enforceContextURI(String component);
}