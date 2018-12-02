package com.sap.security.core.logonadmin;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Locale;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sap.security.api.IUser;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sap.security.api.UMRuntimeException;
import com.sap.security.api.logon.IAuthScheme;
import com.sap.security.core.IEngineResourceHelper;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.sapmimp.logon.LogonLocaleBean;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.ResourceBean;
import com.sap.security.core.util.imp.LogonUtils;


public class ServletAccessToLogic implements IAccessToLogic {
  public final static String  VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~logon/webapp/WEB-INF/java/com/sap/security/core/logonadmin/ServletAccessToLogic.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
  private static IUMTrace trace = null;

//  private static IUMParameters mySAPProperties = null;
//  private final static String CERT_AUTHSCHEME = "ume.logon.cert_authscheme_name";
//  private final static String CERT_LOGIN_DEFAULT = "certlogin";

  static {
    trace = InternalUMFactory.getTrace(VERSIONSTRING);
//    mySAPProperties = UMFactory.getProperties();
  } // static

  private HttpServletRequest req;
  private HttpServletResponse resp;
  private HttpSession session;
  private String env;

  private final static String SCHEMA                  = "schema";
  private final static String UM_DEFAULT_SCHEMA       = "";

  public final static String logon_servlet_alias      = "/logon/logonServlet";
  public final static String logon_certservlet_alias  = "/logon/logonCertServlet";

  public ServletAccessToLogic(HttpServletRequest req, HttpServletResponse resp,
                              String env) {
    this.req = req;
    this.resp = resp;
    this.session = req.getSession();
    this.env = env;
  } // servletAccessToLogic

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getEnvironment()
   */
  public String getEnvironment() {
    return this.env;
  }

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getSessionAttribute(java.lang.String)
   */
  public Object getSessionAttribute(String attrName) {
    return this.session.getAttribute(attrName);
  } // getSessionAttribute

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getRequestAttribute(java.lang.String)
   */
  public Object getRequestAttribute(String attrName) {
    return this.req.getAttribute(attrName);
  } // getReuqestAttribute

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#setSessionAttribute(java.lang.String, java.lang.Object)
   */
  public void setSessionAttribute(String attrName, Object attrValue) {
    this.session.setAttribute(attrName, attrValue);
  } // setSessionAttribute

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#setRequestAttribute(java.lang.String, java.lang.Object)
   */
  public void setRequestAttribute(String attrName, Object attrValue) {
    this.req.setAttribute(attrName, attrValue);
  } // setRequestAttribute

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#isSessionNew()
   */
  public boolean isSessionNew() {
    return this.session.isNew();
  } // isSessionNew

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getRequestParameter(java.lang.String)
   */
  public String getRequestParameter(String parName) {
    return this.req.getParameter(parName);
  } // getRequestParameter

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#gotoPage(java.lang.String)
   */
  public void gotoPage(String page) throws AccessToLogicException, IOException {
    ResourceBean localeBean = (ResourceBean) getSessionAttribute(LogonLocaleBean.beanId);
    String methodName = "gotoPage(String)";

    if (trace.bePath()) {
      trace.entering(methodName, new String[]{page});
    }

    String addr = localeBean.getPage(page);
    // add schema
    String schema = this.req.getParameter(SCHEMA);

    if (schema == null) {
      schema = (String) this.req.getSession().getAttribute(SCHEMA);

      if (schema != null) {
        if (trace.beDebug()) {
          trace.debugT(methodName, "Schema retrieved from session attribute: " + schema);
        }
      }
    } else {
      if (trace.beDebug()) {
        trace.debugT(methodName, "Schema retrieved from URL: " + schema);
      }
    }

    if (schema == null) {
      schema = UMFactory.getProperties().get(LogonUtils.UM_LOGON_SCHEMA,
                                             UM_DEFAULT_SCHEMA).toLowerCase();

      if (trace.beDebug()) {
        if (schema.equals(UM_DEFAULT_SCHEMA)) {
          trace.debugT(methodName, "Default schema used: " + schema);
        } else {
          trace.debugT(methodName, "Schema retrieved from properties: " + schema);
        }
      }
    } else {
      schema = schema.toLowerCase();
    }

    if (!(schema == null || schema.equals(""))) {
      addr = "/" + schema + addr;
    }

    if (trace.beDebug()) {
      trace.debugT(methodName, "found physical page name: ", new String[] {addr});
    }

    // Workaround:
    // Since relative addresses (without leading backslash) do not work with
    // InQMy. We convert relative address to absolute addresses. Since this
    // is a hack, we will print a severe trace message on each occurance of
    // a relative address.
    if (!addr.startsWith("/")) {
      // create exception object to print stack trace
      Exception ex = new UMException();
      trace.warningT(methodName, "relative addr \"" + addr + "\" found", ex);

      // make relative to absolute
      addr = "/" + addr;
    }

    try {
      this.req.getRequestDispatcher(addr).forward(this.req, this.resp);
    } catch (ServletException ex) {
      trace.errorT(methodName, "Can't do forwarding", ex);
      throw new AccessToLogicException(ex);
    }
  } // gotoPage

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getRequestURI()
   */
  public String getRequestURI() {
    return this.req.getRequestURI();
  } // getRequestURI

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getSessionId()
   */
  public String getSessionId() {
    return this.session.getId();
  } // getSessionId

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getRequestParameterValues(java.lang.String)
   */
  public String[] getRequestParameterValues(String s) {
    return this.req.getParameterValues(s);
  } // getRequestParameterValues

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getRequestHeader(java.lang.String)
   */
  public String getRequestHeader(String s) {
    return this.req.getHeader(s);
  } // getRequestHeader

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#setResponseContentType(java.lang.String)
   */
  public void setResponseContentType(String s) {
    this.resp.setContentType(s);
  } // setResponseContentType

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#setRequestCharacterEncoding(java.lang.String)
   */
  public void setRequestCharacterEncoding(String s) throws java.io.UnsupportedEncodingException {
    this.req.setCharacterEncoding(s);
  } // setRequestCharacterEncoding


  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#setResponseHeader(java.lang.String, java.lang.String)
   */
  public void setResponseHeader(String s, String val) {
    this.resp.setHeader(s, val);
  } // setResponseHeader

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getResponseWriter()
   */
  public PrintWriter getResponseWriter() throws IOException {
    return this.resp.getWriter();
  } // getResponseWriter

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getRequestParameterNames()
   */
  public Enumeration getRequestParameterNames() {
    return this.req.getParameterNames();
  } // getRequestParametterNames

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getRequestAttributeNames()
   */
  public Enumeration getRequestAttributeNames() {
    return this.req.getAttributeNames();
  } // getRequestAttributeNames

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getSessionAttributeNames()
   */
  public Enumeration getSessionAttributeNames() {
    return session.getAttributeNames();
  } // getSessionAttributeNames

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getRequestCharacterEncoding()
   */
  public String getRequestCharacterEncoding() {
    return this.req.getCharacterEncoding();
  } // getRequestCharacterEncoding

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getContextURI()
   */
  public String getContextURI() {
    return this.req.getContextPath();
  } // getContextURI

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getActiveUser()
   */
  public IUser getActiveUser() {
    return UMFactory.getAuthenticator().forceLoggedInUser(this.req, this.resp);
  } // getActiveUser

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#logon(java.lang.String)
   */
  public Subject logon(String s) throws LoginException {
    return UMFactory.getLogonAuthenticator().logon(this.req, this.resp, s);
  } // logon

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#logout()
   */
  public void logout() {
    UMFactory.getAuthenticator().logout(this.req, this.resp);
  }

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getSession()
   */
  public Object getSession() {
    return this.req.getSession();
  } // getSession

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getSession(boolean)
   */
  public Object getSession(boolean flag) {
    return this.req.getSession(flag);
  } // getSession(boolean)

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#removeSessionAttribute(java.lang.String)
   */
  public void removeSessionAttribute(String att) {
    this.session.removeAttribute(att);
  } // removeSessionAttribute

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getRequestLocale()
   */
  public Locale getRequestLocale() {
    return this.req.getLocale();
  } // getReqeustLocale

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#sendRedirect(java.lang.String)
   */
  public void sendRedirect(String url) throws IOException {
    if (trace.beDebug()) {
      trace.debugT("sendRedirect", "Send redirect to " + url);
    }
    // remove Security Session
    // Check if EngineResourceHelper is available (= running in J2EE Engine)
    IEngineResourceHelper ierh = InternalUMFactory.getEngineResourceHelper();

    if (ierh != null) {
      if (trace.beDebug()) {
        trace.debugT("sendRedirect", "EngineResourceHelper is available");
      }

      ierh.invalidateSecuritySession();

      if (trace.beDebug()) {
        trace.debugT("sendRedirect", "Invalidated the security session");
      }
    }

    LogonUtils.doRedirect(url, this.req, this.resp);
  } // sendRedirect

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#sessionInvalidate()
   */
  public void sessionInvalidate() {
    this.session.invalidate();
  } // sessionInvalidate

  public HttpServletRequest getRequest() {
    return this.req;
  } // getRequest

  public HttpServletResponse getResponse() {
    return this.resp;
  } // getResponse

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getQueryString()
   */
  public String getQueryString() {
    return this.req.getQueryString();
  } //getQueryString

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getServerName()
   */
  public String getServerName() {
    return this.req.getServerName();
  }

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getAlias(java.lang.String)
   */
  public String getAlias(String alias) {
    if (this.env.equals(IAccessToLogic.ENV_LOGONSERVLET)) {
      return logon_servlet_alias;
    } else if (this.env.equals(IAccessToLogic.ENV_LOGONCERTSERVLET)) {
      return logon_certservlet_alias;
    } else {
      return "unknown_env";
    }
  }

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getAlias(java.lang.String, java.lang.String)
   */
  public String getAlias( String context, String event ) {
    if (event != null) {
      String parameter = getRequestParameter(event);

      if (parameter != null) {
        return context + "?redirectURL=" + URLEncoder.encode(URLDecoder.decode(parameter));
      }
    }
    
    return context;
  }

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getAlias(java.lang.String, java.lang.String, java.lang.String)
   */
  public String getAlias( String context, String event, String redirectURL ) {
    throw new UMRuntimeException("getAlias(String, String, String) not implemented");
  }

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getRequiredAuthScheme()
   */
  public String getRequiredAuthScheme() {
    //return mySAPProperties.get(CERT_AUTHSCHEME, CERT_LOGIN_DEFAULT);
    // no use for authscheme in standalone.
    return null;
  }

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#getAuthSchemes()
   */
  public IAuthScheme[] getAuthSchemes() {
    throw new UMRuntimeException("getAuthSchemes not implemented");
  }

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#isAction(java.lang.String)
   */
  public boolean isAction(String s) {
    // which type of help?
    String action = this.req.getParameter(s);

    if (action == null) {
      String helpAction = this.req.getParameter("helpActionPage");
      action = (s.equals(helpAction) ? helpAction : null);
    }

    return (action != null);
  }

  public static String getAbsoluteURL(String webpath, String url) {
    if (webpath == null || "".equals(webpath)) {
      //nothing to add
      return url;
    }

    if (url == null || "".equals(url)) {
      //nothing to add
      return webpath;
    }

    String tmpcont = url.toLowerCase();

    if (tmpcont.startsWith("/") || tmpcont.startsWith("http:")
        || tmpcont.startsWith("https:")) {
      //url is already absolute
      return url;
    }

    //url is relative to web path
    StringBuffer result = new StringBuffer(webpath.length() + url.length());
    result.append(webpath);
    result.append(url);
    return result.toString();
  }

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.IAccessToLogic#setCookie(javax.servlet.http.Cookie)
   */
  public void setCookie(Cookie cookie) {
    this.resp.addCookie(cookie);
  }
}