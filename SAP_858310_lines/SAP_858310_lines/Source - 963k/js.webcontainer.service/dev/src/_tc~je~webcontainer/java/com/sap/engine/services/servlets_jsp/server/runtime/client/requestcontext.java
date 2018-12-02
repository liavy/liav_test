/*
 * Copyright (c) 20062007 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.runtime.client;

import java.io.File;
import java.security.AccessControlException;
import java.security.Policy;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.security.jacc.WebRoleRefPermission;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import com.sap.engine.interfaces.security.SecurityContextObject;
import com.sap.engine.interfaces.security.SecuritySession;
import com.sap.engine.interfaces.security.auth.SecurityRequest;
import com.sap.engine.lib.util.ArrayObject;
import com.sap.engine.services.httpserver.interfaces.HttpParameters;
import com.sap.engine.services.httpserver.interfaces.exceptions.ParseException;
import com.sap.engine.services.httpserver.lib.CookieParser;
import com.sap.engine.services.httpserver.lib.HttpCookie;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.application.SessionServletContext;
import com.sap.engine.services.servlets_jsp.server.exceptions.NewApplicationSessionException;
import com.sap.engine.session.SessionException;
import com.sap.engine.session.exec.ClientContextImpl;
import com.sap.engine.session.runtime.http.HttpSessionRequest;
import com.sap.tc.logging.Location;

/**
 * @author diyan-y
 */
public class RequestContext {
  
  private static Location currentLocation = Location.getLocation(RequestContext.class);
  private static Location traceLocation = LogContext.getLocationServletRequest();
  private static Location securityLocation = LogContext.getLocationSecurity();
  
  private HttpSessionRequest sessionRequest = null;
  private ApplicationContext applicationContext = null;
  private SessionServletContext sessionServletContext = null;
  private HttpParametersWrapper httpParameters = null;
  private HttpServletResponseFacadeWrapper response = null;
  
  private ConcurrentHashMap attributes = new ConcurrentHashMap(16);

  /**
   * Part of this request's URL from the protocol name up to the query string
   */
  private byte[] requestURI = null;

  /**
   * Real path for this request
   */
  private String realPath = null;
  
  private boolean reusedSessionCookie = false;
  private boolean cookieSet = false;
  private boolean sessionAccessed = false;
  
  private String pathInfo = null;
  private boolean pathInfoSet = false;
  private boolean pathInfoParsed = false;
  private String servletPathIncluded = null;
  private byte[] requestURI_internal = null;
  private String requestURIinternal = null;
  private boolean requestURIinternalParsed = false;
  private Cookie[] cookies = null;
  private boolean cookiesParsed = false;
  private String pathTranslated = null;
  private boolean pathTranslatedParsed = false;
  private String contextPath = null;
  private boolean contextPathParsed = false;
  private String requestedSessionId = null;
  private boolean requestedSessionIdParsed = false;
  private boolean isRequestedSessionIdFromCookie = false;
  private boolean isRequestedSessionIdFromCookieParsed = false;
  private boolean isRequestedSessionIdFromURL = false;
  private boolean isRequestedSessionIdFromURLParsed = false;
  
  private String forcedAuthType = null;
  private SecuritySession ss = null;
  private boolean isInDoAs = false;
  
  private String servletName = null;
  
  /**
   * Tracks the names of the servlets invoked in this request.
   */
  private LinkedList invokedServletNamesStack = new LinkedList();
  
  /**
   * The name of the currently invoked servlet.
   */
  private String currentlyInvokedServletName = null;
  
  private ReentrantLock invalidatedMonitor = new ReentrantLock();
  
  private boolean valid = true;
  
  public RequestContext(ApplicationContext applicationContext, HttpSessionRequest sessionRequest,
      HttpParameters httpParameters, HttpServletResponseFacadeWrapper response) {
    this.applicationContext = applicationContext;
    this.sessionServletContext = applicationContext.getSessionServletContext();
    this.sessionRequest = sessionRequest;
    this.httpParameters = new HttpParametersWrapper(httpParameters, sessionRequest);
    this.response = response;
    if (traceLocation.beDebug()) {
      trace("RequestContext()", "applicationContext = [" + this.applicationContext + "]");
    }
  }

  /**
   * @return Returns the applicationContext.
   */
  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  /**
   * @param applicationContext The applicationContext to set.
   */
  public void setApplicationContext(ApplicationContext applicationContext) {
    if (traceLocation.beDebug()) {
      trace("setApplicationContext", "new applicationContext = [" + applicationContext + "], alias [" + applicationContext.getAliasName() + "]");
    }
    this.applicationContext = applicationContext;
    this.sessionServletContext = applicationContext.getSessionServletContext();
    //TODO clear already parsed parameters
    contextReplaced();
  }

  /**
   * @return Returns the sessionRequest.
   */
  public HttpSessionRequest getSessionRequest() {
    return sessionRequest;
  }
  /**
   * @param sessionRequest The sessionRequest to set.
   */
  public void setSessionRequest(HttpSessionRequest sessionRequest) {
    this.sessionRequest = sessionRequest;
    this.httpParameters.setSessionRequest(sessionRequest);
  }
  
  public SessionServletContext getSessionServletContext() {
    return sessionServletContext;
  }
  
  public HttpServletResponseFacadeWrapper getResponse() {
    return response;
  }

  public void setServletName(String servletName) {
    this.servletName = servletName;  
  }
    
  public String getServletName() {
    return servletName;  
  }
  
  /**
   * 
   */
  private void contextReplaced() {
    cookiesParsed = false;
    requestedSessionIdParsed = false;
    pathTranslatedParsed = false;
    isRequestedSessionIdFromCookieParsed = false;
    isRequestedSessionIdFromURLParsed = false;
    contextPathParsed = false;
  }

  public ConcurrentHashMap getAttributes() {
    return attributes;
  }
  
  public void setAttributes(ConcurrentHashMap attribs) {
    ConcurrentHashMap result = new ConcurrentHashMap(16);
    result.putAll(attribs);
    
    attributes = result;
  }
  
  /**
   * Returns the value of the named attribute as an Object, or null if
   * no attribute of the given name exists.
   *
   * @param   s  name of attribute
   * @return     value of the attribute with name s or null
   */
  public Object getAttribute(String s) {
    Object result = attributes.get(s);
    if (traceLocation.beDebug()) {
      trace("getAttribute", "name = [" + s + "], result = [" + result + "]");
    }
    return result;
  }
  
  /**
   * Returns an Enumeration containing the names of the attributes available to this request.
   *
   * @return     enumeration with the namesof the attributes
   */
  public Enumeration getAttributeNames() {
    if (traceLocation.beDebug()) {
      StringBuilder atr = new StringBuilder("");
      Enumeration keys = attributes.keys();
      while (keys.hasMoreElements()) {          
        atr.append(keys.nextElement());
        atr.append(", ");      
      }
      
      if (atr.length() > 2) {
        if (atr.charAt(atr.length() - 2) == ',' && atr.charAt(atr.length() - 1) == ' ') {
          atr.delete(atr.length() - 2, atr.length() - 1);
        }
      }
      trace("getAttributeNames", "result: [" + atr + "]");    
    }
    return attributes.keys();
  }

  /**
   * Stores an attribute in this request.
   *
   * @param   s  name of the attribute
   * @param   obj  value of the attribute
   */
  public Object setAttribute(String s, Object obj) {
    if (traceLocation.beDebug()) {
      trace("setAttribute", "name = [" + s + "], value = [" + obj + "]");
    }

    Object oldValue = attributes.put(s, obj);
    return oldValue;
  }
  
  /**
   * Removes an attribute from this request.
   *
   * @param   s  name of the attribute
   */
  public Object removeAttribute(String s) {
    Object result = attributes.remove(s);
    if (traceLocation.beDebug()) {
      trace("removeAttribute", "name = [" + s + "], value = [" + result);
    }
    return result;
  }  
  
  public Cookie[] getCookies() {
    if (!cookiesParsed) {
      cookies = getCookiesInternal();
      cookiesParsed = true;
    }
    return cookies;
  }
  
  /**
   * @return
   */
  private Cookie[] getCookiesInternal() {
    ArrayObject cookies = httpParameters.getRequest().getCookies(applicationContext.getWebApplicationConfiguration().isURLSessionTracking());
    if (cookies == null || cookies.size() == 0) {
      return null;
    }
    Cookie[] res = new Cookie[cookies.size()];
    int resPtr = 0;
    for (int i = 0; i < res.length; i++, resPtr++) {
      HttpCookie httpCookie = (HttpCookie)cookies.elementAt(i);
      try {
        res[resPtr] = httpCookie.toCookie();
      } catch (IllegalArgumentException e) {
        //reserved cookie name => must not include this cookie
        resPtr--;
      }
    }
    if (resPtr < res.length) {
      Cookie[] resTmp = new Cookie[resPtr];
      System.arraycopy(res, 0, resTmp, 0, resTmp.length);
      res = resTmp;
    }
    return res;
  }

  /**
   * @return
   */
  public String getPathInfo() {
    if (traceLocation.beDebug()) {
      trace("getPathInfo", "pathInfoParsed = [" + pathInfoParsed + "]");
    }
    if (!pathInfoParsed) {
      pathInfo = parsePathInfo(pathInfoSet, pathInfo);
      pathInfoParsed = true;
    }
    if (traceLocation.beDebug()) {
      trace("getPathInfo", "pathInfo = [" + pathInfo + "]");
    }
    return pathInfo;
  }
  
  public void setPathInfo(String pathInfo) {
    if (traceLocation.beDebug()) {
      trace("setPathInfo", "pathInfo = [" + pathInfo + "]");
    }
    pathInfoSet = true;
    this.pathInfo = pathInfo;
    pathInfoParsed = false;
  }
  
  //TODO remove includePath
  public String getPathTranslated(String includePath) {
    if (!pathTranslatedParsed || !pathInfoParsed) {
      pathTranslated = getPathTranslatedInternal(includePath);
      pathTranslatedParsed = true;
    }
    if (traceLocation.beDebug()) {
      trace("getPathTranslated", "pathTranslated = [" + pathTranslated + "]");
    }
    return pathTranslated;
  }
  
  /**
   * @return
   */
  private String getPathTranslatedInternal(String includePath) {
    String path = getPathInfo();
    if (path == null || "".equals(path)) {
      return null;
    }
    return getRealPath(path, includePath);
  }
  
  //TODO remove includePath
  public String getRealPath(String path, String includePath) {
    if (realPath == null) {
      realPath = new String(httpParameters.getRequestPathMappings().getRealPath());
    }

    boolean beDebug = traceLocation.beDebug();
    if (!path.startsWith("/")) {
      //String tempPath = (String) getAttribute("javax.servlet.include.realpath_path");
      String tempPath = includePath;

      if (tempPath != null) {
        if (beDebug) {
          trace("getRealPath", "path = [" + path + "], return: [" +
              (tempPath + path).replace('/', File.separatorChar) + "]");
        }
        return (tempPath + path).replace('/', File.separatorChar);
      }
      if (beDebug) {
        trace("getRealPath", "path = [" + path + "], return: [" +
            (realPath + path).replace('/', File.separatorChar) + "]");
      }
      return (realPath + path).replace('/', File.separatorChar);
    } else {
      String temp = applicationContext.getServletContext().getRealPath(path);

      if (temp != null) {
        if (beDebug) {
          trace("getRealPath", "path = [" + path + "], return: [" + temp.replace('/', File.separatorChar) + "]");
        }        
        return temp.replace('/', File.separatorChar);
      } else {
        if (beDebug) {
          trace("getRealPath", "path = [" + path + "], return: [null]");
        }
        return null;
      }
    }
  }
  

  private String parsePathInfo(boolean pathInfoSet, String pathInfo) {
    String context_path = (getContextPath() + getServletPath()).trim();
    boolean beDebug = traceLocation.beDebug();
    if (beDebug) {
      trace("parsePathInfo", "context_path = [" + context_path + "], pathInfoSet [" + pathInfoSet + "], pathInfo [" + pathInfo + "]");
    }
    if (!pathInfoSet) {
      pathInfo = httpParameters.getRequestPathMappings().getPathInfo();
      if (beDebug) {
        trace("parsePathInfo", "httpParameters.getRequestPathMappings().getPathInfo(), pathInfo = [" + pathInfo + "]");
      }
    }
    if (pathInfo != null) {
      pathInfo = pathInfo.trim();
      if (pathInfo.startsWith(context_path)) {
        if (context_path.equals(pathInfo)) {
          pathInfo = null;
          if (beDebug) {
            trace("parsePathInfo", "context_path.equals(pathInfo), return pathInfo = [" + pathInfo + "]");
          }
          return pathInfo;
        } else {
          pathInfo = pathInfo.substring(context_path.length());
          if (beDebug) {
            trace("parsePathInfo", "pathInfo.substring(context_path.length()), pathInfo = [" + pathInfo + "]");
          }
        }
      }
      if (!getRequestURIinternal().equalsIgnoreCase(context_path + pathInfo)) {
        int csPathIndex = getRequestURIinternal().indexOf(context_path);
        if (csPathIndex != -1) {
          pathInfo = getRequestURIinternal().substring(csPathIndex + context_path.length());
          if (beDebug) {
            trace("parsePathInfo", "getRequestURIinternal().substring(csPathIndex + context_path.length()), pathInfo = [" + pathInfo + "]");
          }
        } else {
          if (beDebug) {
            trace("parsePathInfo", "csPathIndex == -1, pathInfo = [" + pathInfo + "], but will set to null");
          }
          pathInfo = null;
        }
      }
    }
    if (pathInfo == null || pathInfo.equals("")) {
      if (beDebug) {
        trace("parsePathInfo", "pathInfo == null || pathInfo.equals(\"\"), return pathInfo = [" + pathInfo + "]");
      }
      return null;
    }
    if (pathInfo.indexOf("?") > -1) {
      pathInfo = pathInfo.substring(0, pathInfo.indexOf("?"));
    }
    if (pathInfo.indexOf(";") > -1) {
      pathInfo = pathInfo.substring(0, pathInfo.indexOf(";"));
    }
    if (pathInfo.equals("")) {
      return null;
    }
    if (beDebug) {
      trace("parsePathInfo", "return pathInfo = [" + pathInfo + "]");
    }
    return pathInfo;
  }
  
  private String getContextPathInternal() {
    String res = null;
    if (applicationContext.isDefault()) {
      res = "";
    } else {
      res = "/".concat(applicationContext.getAliasName());
    }
    if (httpParameters.getRequestPathMappings().getZoneName() != null && !httpParameters.getRequestPathMappings().isZoneExactAlias()) {
      res = res + ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getZoneSeparator() + httpParameters.getRequestPathMappings().getZoneName();
    }
    return res;
  }
  
  public String getServletPath() {
    if (servletPathIncluded != null) {
      return servletPathIncluded;
    }
    if (traceLocation.beDebug()) {
      trace("getServletPath", "return: [" + httpParameters.getRequestPathMappings().getServletPath() + "]");
    }
    return httpParameters.getRequestPathMappings().getServletPath();
  }
  
  public void setServletPath(String servletPath) {
    servletPathIncluded = servletPath;
  }

  /**
   * Returns the part of this request's URL(not decoded) from the protocol name up to the query string
   * in the first line of the HTTP request.
   *
   * @return     the part of this request's URL from the protocol name up to the query string
   */
  public String getRequestURI() {
    if (requestURI == null) {
      try {
        requestURI = httpParameters.getRequest().getRequestLine().getUrlNotDecoded().getBytes();
      } catch (ParseException e) {
    	  //TODO:Polly type:ok
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000125",
            "Error in parsing request URI.", e, null, null); 
      }
    }
    char[] ch = new char[requestURI.length];
    for (int i = 0; i < ch.length; i++) {
      ch[i] = (char) (requestURI[i] & 0x00ff);
    }
    return new String(ch);
  }  
  
  public void setRequestURI(String newURI) {
    requestURIinternalParsed = false;
    //super.setRequestURI(newURI);
    this.requestURI = newURI.getBytes();
    this.requestURI_internal = newURI.getBytes();
  }
  
  public String getRequestURIinternal() {
    if (!requestURIinternalParsed) {
      requestURIinternal = parseRequestURIinternal();
      requestURIinternalParsed = true;
    }
    if (traceLocation.beDebug()) {
      trace("getRequestURIinternal", "requestURIinternal = [" + requestURIinternal + "]");
    }
    return requestURIinternal;
  }
  
  private String parseRequestURIinternal() {
    if (requestURI_internal == null) {
      requestURI_internal = httpParameters.getRequest().getRequestLine().getUrlDecoded().getBytes();
    }
    char[] ch = new char[requestURI_internal.length];
    for (int i = 0; i < ch.length; i++) {
      ch[i] = (char) (requestURI_internal[i] & 0x00ff);
    }
    return new String(ch);
  }
  
  public String getContextPath() {
    if (!contextPathParsed) {
      contextPath = getContextPathInternal();
      contextPathParsed = true;
    }
    if (traceLocation.beDebug()) {
      trace("getContextPath", "contextPath = [" + contextPath + "]");
    }
    return contextPath;
  }
  
  public String getRequestedSessionId() {
    if (!requestedSessionIdParsed) {
      requestedSessionId = getRequestedSessionIdInternal();
      requestedSessionIdParsed = false;
    }
    if (traceLocation.beDebug()) {
      trace("getRequestedSessionId", "requestedSessionId = [" + requestedSessionId + "]");
    }
    return requestedSessionId;
  }

  /**
   * @return
   */
  private String getRequestedSessionIdInternal() {
    HttpCookie cookie = httpParameters.getRequest().getSessionCookie(applicationContext.getWebApplicationConfiguration().isURLSessionTracking());
    if (cookie == null) {
      return null;
    }
    return cookie.getValue();
  }

  /**
   * Note that this method modifies the response object and
   * it is called from the reset() methods of the request,
   * which are also called after the resetInternal() of the response,
   * i.e. right before returning the response to the pool and
   * when it should not be modified.
   * For this purpose the flag isModifiable was added to the
   * response object and since it is not checked inside the response
   * setters (in order to minimize the changes and risk), 
   * use it here when setting something to the response.
   * @param flag
   * @return
   */
  public HttpSession getSession(boolean flag) {
    ApplicationSession applicationSession = (ApplicationSession) httpParameters.getApplicationSession();
    HttpCookie cokkk = httpParameters.getRequest().getSessionCookie(applicationContext.getWebApplicationConfiguration().isURLSessionTracking());
    //SessionRequest sRequest = httpParameters.getSessionRequest();
    boolean beDebug = traceLocation.beDebug();
    if (cokkk != null && !httpParameters.isSetSessionCookie()) {
      //TODO ne trqbva li da izpolzvam localniq sessionrequest
    	//sRequest.reDoSessionRequest(sessionServletContext.getSession(), cokkk.getValue());
      if (beDebug) {
        trace("getSession", "reDoSessionRequest, sessionRequest = [" + sessionRequest + "]");
      } 
      try{
    	 invalidatedMonitor.lock();
        if (valid) {
          sessionRequest.reDoSessionRequest(sessionServletContext.getSession(), cokkk.getValue(), SessionServletContext.getSessionIdFromJSession(cokkk.getValue()));
        } else {
          IllegalStateException ise = new IllegalStateException("Request object used outside its context. RequestContext has been destroyed.");
          if (traceLocation.beError()) {
            LogContext.getLocation(LogContext.LOCATION_SERVLET_REQUEST).traceError("ASJ.web.000659",
                "client [{0}] RequestContext.getSession [{1}] in application [{2}]: " + 
                "getSession([{3}]) method is called on a request object which associated " +
                "HttpSessionRequest object [{4}] is not valid. This means that the " +
                "Request object on which getSession() method is called is used outside the service scope. " +
                "ERROR: {5}",
                new Object[]{httpParameters.getRequest().getClientId(), getObjectInstance(),
                applicationContext.getAliasName(), flag, sessionRequest, LogContext.getExceptionStackTrace(ise)}, null, null);
          }
          throw ise;
        }
      }finally{
    	  invalidatedMonitor.unlock();
      }
      applicationSession = (ApplicationSession) sessionRequest.getSession(false);
      httpParameters.setApplicationSession(applicationSession);	 
    }
    if (beDebug) {
      trace("getSession", "flag = [" + flag + "], applicationSession in request = [" + applicationSession + "]");
    }
    if (applicationSession != null) {
      if (!applicationSession.isValid()) {
        if (beDebug) {
          trace("getSession", "session in request is not valid");
        }
        applicationSession = null;
        httpParameters.setApplicationSession(null);
      }
    }

    if (flag && (applicationSession == null)) { //Creates the session
      if (sessionServletContext.isForbiddenNewApplicationSession()) {
//        if (LogContext.isTracing()) {
//          traceWarning("getSession", "isForbiddenNewApplicationSession", false);
//        }
        throw new NewApplicationSessionException(NewApplicationSessionException.CANNOT_CREATE_A_NEW_APPLICATION_SESSION_BECAUSE_MAX_NUMBER_OF_SESSIONS_HAS_BEEN_REACHED);
      }
      String sessionId = getSessionID(httpParameters);
      if (beDebug) {
        trace("getSession", "sessionId = [" + sessionId + "], reusedSessionCookie = [" + reusedSessionCookie + "]");
      }
      try {
        /*
        * if there is no such session object the new one is created
        */
        SessionFactoryWrapper factory =  new SessionFactoryWrapper(this, null);
        try{
          invalidatedMonitor.lock();	
          if (valid) {
            String sessId = SessionServletContext.getSessionIdFromJSession(sessionId);
            sessionRequest.reDoSessionRequest(applicationContext.getSessionServletContext().getSession(), sessionId, sessId);
            if (!ServiceContext.getServiceContext().getWebContainerProperties().getSessionIDRegenerationEnabled()) {
              ClientContextImpl clientConext = ClientContextImpl.getByClientId(SessionServletContext.getSessionIdFromJSession(sessionId));
              if (clientConext != null && clientConext.getMarkId() == null) {
                ClientContextImpl.getByClientId(sessId).setMarkId(SessionServletContext.getMarkIdFromJSession(sessionId));
              }
            }
          } else {
            IllegalStateException ise = new IllegalStateException("Request object used outside its context. RequestContext has been destroyed.");
            if (traceLocation.beError()) {
              LogContext.getLocation(LogContext.LOCATION_SERVLET_REQUEST).traceError("ASJ.web.000660",
                  "client [{0}] RequestContext.getSession [{1}] in application [{2}]: " + 
                  "getSession([{3}]) method is called on a request object which associated " +
                  "HttpSessionRequest object [{4}] is not valid. This means that the " +
                  "Request object on which getSession() method is called is used outside the service scope. " +
                  "ERROR: {5}",
                  new Object[]{httpParameters.getRequest().getClientId(), getObjectInstance(),
                  applicationContext.getAliasName(), flag, sessionRequest, LogContext.getExceptionStackTrace(ise)}, null, null);
            }
            throw ise;
          }
        }finally{
        	invalidatedMonitor.unlock();
        }
        
        if (beDebug) {
          trace("getSession", "new session object is created, sessionRequest = [" + sessionRequest + "]");
        }
        applicationSession = (ApplicationSession) sessionRequest.getSession(factory);
        //add session cookie after session is created CSN 563459/2008
        setSessionCookieInResponse(sessionId);
        httpParameters.setApplicationSession(applicationSession);
      } catch (SessionException e) {
    	  //TODO:Polly type:ok
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000126",
            "Cannot create http session {0}.", new Object[]{sessionId}, e, null, null);
      }
    } else if (applicationSession != null) {
      if (!cookieSet && !httpParameters.isSetSessionCookie() && !applicationContext.getWebApplicationConfiguration().isURLSessionTracking()) {
        HttpCookie sCookie = httpParameters.getRequest().getSessionCookie(applicationContext.getWebApplicationConfiguration().isURLSessionTracking());
        if (sCookie != null && sCookie.getName().equals(CookieParser.jsessionid_url) && response.isModifiable()) {        
          response.addSessionCookie(CookieParser.createSessionCookie(sessionRequest.getClientCookie(),
            httpParameters.getRequest().getHost(),
            applicationContext.getWebApplicationConfiguration().getSessionCookieConfig()));
          response.setSessionCookie();
        }
      }
      if (!sessionAccessed) {
        applicationSession.accessed();
        sessionAccessed = true;
      }
    }
    
    addSapLBCookie();
    
    if (beDebug) {
      trace("getSession", "return: [" + applicationSession + "]");
    }
    return applicationSession;

  }

  /**
   * Add sap lb cookie to the response. It is added when jsessionid is added and it is used by web dispatcher for load balancing
   */
  private void addSapLBCookie() {
	//TODO - in the case when we have included servlets here could have problems when calling 
    //getSession(false) from web container source and there is no application session - WebIllegalStateException thrown from addSessionCookie
    //The problem is that the response is flushed and headers could not be changed and we try to add cookie (saplb)
    //possible solution could to check if there is application session (for example from httpparametsrs). If there is no one - do not add saplb cookie 
    if (!cookieSet && response.isModifiable()) {
      HttpCookie httpCookie = sessionServletContext.addApplicationCookie(httpParameters); //this adds saplb cookie as header
      //CSN 520951/2007 - set saplb cookie again , in order to avoid its removal when called HttpServletResponse.reset(), which clears all headers.
      if (httpCookie != null) {    	
        response.addSessionCookie(httpCookie); //this adds saplb cookie in ArrayObject cookies of HttpServletResponseBase
      }
      cookieSet = true;
    }
  }
  
  /**
   * Returns the JSESSIONID value. If the JSESSIONID is found in the request
   * the same id is reused.  If the JSESSIONID is not found returns <code>null</code>.
   * @param httpParameters
   * @return
   */
  private String getExistingSessionID(HttpParameters httpParameters) {
    HttpCookie sCookie = httpParameters.getRequest().getSessionCookie(applicationContext.getWebApplicationConfiguration().isURLSessionTracking());
    boolean beDebug = traceLocation.beDebug();
    if (sCookie != null) {
      if (beDebug) {
        trace("getSessionID", "request cookie: name = [" + sCookie.getName() + "], value = [" + sCookie.getValue() + "]");
      }
      reusedSessionCookie = true;
      if (beDebug) {
        trace("getSessionID", "return: [" + sCookie.getValue() + "]");
      }
      return sCookie.getValue();
    }
    if (beDebug) {
      trace("getSessionID", "response.isSessionCookieSet() = [" + response.isSessionCookieSet() + "]");
    }
    if (response.isSessionCookieSet()) {
      Object[] respCookies = response.getCookies().toArray();
      for (int i = 0; i < respCookies.length; i++) {
        HttpCookie httpCookie = (HttpCookie)respCookies[i];
        if (httpCookie.getName().equals(CookieParser.jsessionid_cookie)) {
          if (beDebug) {
            trace("getSessionID", "return: [" + httpCookie.getValue() + "]");
          }
          return httpCookie.getValue();
        }
      }
    }
    return null;
  }

  /**
   * Returns the JSESSIONID value. If the JSESSIONID is found in the request
   * the same id is reused. The respective session cookie is set in the response.
   * If there is no JSESSIONID found, new one is created. Session cookie is not
   * set in the response until the session is not created.
   * @param httpParameters
   * @return
   */
  private String getSessionID(HttpParameters httpParameters) {
    String sessionId = getExistingSessionID(httpParameters);
    if (sessionId != null) {
    	return sessionId;
    }
    
    // no sessionId found - a new one has to be created
    sessionId = generateSessionId();//sessionServletContext.generateSessionID(httpParameters);   
    if (traceLocation.beDebug()) {
      trace("getSessionID", "new cookie generated, return: [" + sessionId + "]");
    }
    return sessionId;
  }
  
  /**
   * Sets given SESSIONID in the response session cookie.
   * @param sessionId
   */
  private void setSessionCookieInResponse(String sessionId) {
    if(httpParameters.isSetSessionCookie()) {
      return;
    }
    HttpCookie sCookie = httpParameters.getRequest().getSessionCookie(applicationContext.getWebApplicationConfiguration().isURLSessionTracking());
    boolean beDebug = traceLocation.beDebug();
    if (sCookie != null) {
      if (beDebug) {
        trace("setSessionCookieInResponse", "request cookie: name = [" + sCookie.getName() + "], value = [" + sCookie.getValue() + "]");
      }
      if (sCookie.getName().charAt(0) == 'j' && !applicationContext.getWebApplicationConfiguration().isURLSessionTracking()) {
        response.addSessionCookie(CookieParser.createSessionCookie(sessionId,
          httpParameters.getRequest().getHost(),
          applicationContext.getWebApplicationConfiguration().getSessionCookieConfig()));
        response.setSessionCookie();
      }
      if (beDebug) {
        trace("setSessionCookieInResponse", "sessionId: [" + sessionId + "]");
      }
    } else if (!applicationContext.getWebApplicationConfiguration().isURLSessionTracking()) {
      if (!httpParameters.isSetSessionCookie()) {
        response.addSessionCookie(CookieParser.createSessionCookie(sessionId,
          httpParameters.getRequest().getHost(),
          applicationContext.getWebApplicationConfiguration().getSessionCookieConfig()));
        response.setSessionCookie();
        httpParameters.setSessionCookie(true);
      }
    }
    if (beDebug) {
      trace("setSessionCookieInResponse", "new session cookie set in response , return: [" + sessionId + "]");
    }
    
    addSapLBCookie();   
  }
  
  private synchronized String generateSessionId() {
  	//SessionRequest sRequest = httpParameters.getSessionRequest();
    if (!sessionRequest.isRequested()) {    
      String sessionId = sessionServletContext.generateJSessionIdValue();
      return sessionId;
//      try {
//        sRequest.requestNewSession(sessionServletContext.getSession(), sessionId);
//      } catch (SessionExistException e) {
//        // TODO Auto-generated catch block        
//      }
    }
    return sessionRequest.getClientCookie();
  }  
  
  public boolean isRequestedSessionIdFromCookie() {
    if (!isRequestedSessionIdFromCookieParsed) {
      isRequestedSessionIdFromCookie = isRequestedSessionIdFromCookieInternal();
      isRequestedSessionIdFromCookieParsed = true;
    }
    return isRequestedSessionIdFromCookie;
  }

  /**
   * @return
   */
  private boolean isRequestedSessionIdFromCookieInternal() {
    HttpCookie sCookie = httpParameters.getRequest().getSessionCookie(applicationContext.getWebApplicationConfiguration().isURLSessionTracking());
    if (traceLocation.beDebug()) {
      trace("isRequestedSessionIdFromCookie", "return: [" + (sCookie != null && CookieParser.jsessionid_cookie.equals(sCookie.getName())) + "]");
    }
    return sCookie != null && CookieParser.jsessionid_cookie.equals(sCookie.getName());
  }

  public boolean isRequestedSessionIdFromURL() {
    if (!isRequestedSessionIdFromURLParsed) {
      isRequestedSessionIdFromURL = isRequestedSessionIdFromURLInternal();
      isRequestedSessionIdFromURLParsed = true;
    }
    return isRequestedSessionIdFromURL;
  }

  
  /**
   * @return
   */
  private boolean isRequestedSessionIdFromURLInternal() {
    HttpCookie sCookie = httpParameters.getRequest().getSessionCookie(applicationContext.getWebApplicationConfiguration().isURLSessionTracking());
    if (traceLocation.beDebug()) {
      trace("isRequestedSessionIdFromURL", "return: [" + (sCookie != null && CookieParser.jsessionid_url.equals(sCookie.getName())) + "]");
    }
    return sCookie != null && CookieParser.jsessionid_url.equals(sCookie.getName());
  }

  /**
   * @return Returns the httpParameters.
   */
  public HttpParameters getHttpParameters() {
    return httpParameters;
  }
  
  protected void trace(String method, String msg) {
//  avoid NullPointers - fetch all values safe    
    String clientId = "<NA>";
    String aliasName = "<NA>";
    
    if (httpParameters != null && httpParameters.getRequest() != null) {
      clientId = String.valueOf(httpParameters.getRequest().getClientId());      
    }
    
    if (applicationContext != null && applicationContext.getAliasName() != null) {
      aliasName = applicationContext.getAliasName();
    }
    
    LogContext.getLocation(LogContext.LOCATION_SERVLET_REQUEST).trace(
        "client [" + clientId + "] RequestContext." + method +
        " [" + getObjectInstance() + "] in application [" + aliasName + "]: " + msg, aliasName);
  }
  
  protected void traceSecurityLocation(String method, String msg) {
	//  avoid NullPointers - fetch all values safe    
	    String clientId = "<NA>";
	    String aliasName = "<NA>";
	    
	    if (httpParameters != null && httpParameters.getRequest() != null) {
	      clientId = String.valueOf(httpParameters.getRequest().getClientId());      
	    }
	    
	    if (applicationContext != null && applicationContext.getAliasName() != null) {
	      aliasName = applicationContext.getAliasName();
	    }
	    
	    LogContext.getLocation(LogContext.LOCATION_SECURITY).trace(
	        "client [" + clientId + "] RequestContext." + method +
	        " [" + getObjectInstance() + "] in application [" + aliasName + "]: " + msg, aliasName);
	  }

  private String getObjectInstance() {
    String instance = super.toString();
    return instance.substring(instance.indexOf('@') + 1);
  }
  
  /** 
   * Called when the servlet service method has finished,
   * updates the name of the currently invoked servlet. 
   */
  public void markServiceFinished() {
    try {
      currentlyInvokedServletName = (String)invokedServletNamesStack.removeLast();
      checkMappedSlashStar();
    } catch (NoSuchElementException nsee) {
      if (traceLocation.beDebug()) {
        trace("markServiceFinished", "Internal error. The invocation stack with servlet names is empty.");
      }
    }
  }//markServiceFinished
  
  /**
   * Updates the name of the currently invoked servlet.
   * @param invokedServletName
   */
  public void markServiceStarted(String invokedServletName) {
    invokedServletNamesStack.addLast(invokedServletName);
    currentlyInvokedServletName = invokedServletName;
    checkMappedSlashStar();
  }
  
  /**
   * Returns the name of the servlet that is currently processed.
   * @return
   */
  public String getCurrentlyInvokedServletName() {
    return currentlyInvokedServletName;  
  }

  private void checkMappedSlashStar() {
    if (applicationContext.getWebMappings().getServletMappedSlashStar().equals(getCurrentlyInvokedServletName())) {
      servletPathIncluded = "";
    }
  }
  
  public String getRealPathLocal(String path, String realPathAttribute) {
    if (realPath == null) {
      byte[] data = httpParameters.getRequestPathMappings().getRealPath();
      char[] qch = new char[data.length];
      for (int i = 0; i < data.length; i++) {
        qch[i] = (char) (data[i] & 0x00ff);
        if (qch[i] == '/' || qch[i] == '\\') {
          qch[i] = ParseUtils.separatorChar;
        }
      }

      realPath = new String(qch);
    }

    if (!path.startsWith("/")) {
      //String tempPath = (String) getAttribute("javax.servlet.include.realpath_path");
      String tempPath = realPathAttribute;

      if (tempPath != null) {
        return ParseUtils.canonicalizeFS((tempPath + path).replace('/', ParseUtils.separatorChar).replace('\\', ParseUtils.separatorChar)).replace(File.separatorChar, ParseUtils.separatorChar);
      }
      return ParseUtils.canonicalizeFS((realPath + path).replace('/', ParseUtils.separatorChar).replace('\\', ParseUtils.separatorChar)).replace(File.separatorChar, ParseUtils.separatorChar);
    } else {
      return ParseUtils.canonicalizeFS(applicationContext.getRealPathLocal(
          path,
          httpParameters.getRequestPathMappings().getAliasName().toString(),
          httpParameters.getRequestPathMappings().getAliasValue().toString())).replace(File.separatorChar, ParseUtils.separatorChar);
    }
  }  
  
  /**
   * Returns the name of the authentication scheme used to protect the servlet
   * or null  if the servlet was not protected.
   *
   * @return     the name of the authentication scheme
   */
  public String getAuthType() {
    // If there is an programmatically forced authentication type
    if (forcedAuthType != null) { return forcedAuthType; }
    
    SecuritySession ss = getSecuritySession();
    if ((ss != null) && (ss.getAuthenticationConfiguration() != null)
        && !sessionServletContext.getAuthenticationContext().isLoginNeeded()) {
      if (traceLocation.beDebug()) {
        trace("getAuthType", "return: [" + sessionServletContext.getAuthType() + "]");
      }
      return sessionServletContext.getAuthType();
    }
    return null;
  }  
  
  public void setForcedAuthType(String authType) {
    forcedAuthType = authType;
  }
  
  public String getRemoteUser() {
    //SRV.12.3
    // If no user has been authenticated, the getRemoteUser method returns null, the
    //isUserInRole method always returns false, and the getUserPrincipal method
    //returns null.
    // Note : this is not valid for old applications , for backward compatibility
    if (!applicationContext.getWebApplicationConfiguration().isProgrammaticSecurityAgainstRunAsIdentity()){
      if (com.sap.engine.session.exec.SessionExecContext.getExecutionContext().threadLoginSession.isAnonymous()) {
        if (traceLocation.beDebug()) {
          trace("getRemoteUser", "threadLoginSession is anonymous, method return: [null]");
        }
        return null;
      } else {
        if (traceLocation.beDebug()) {
          trace("getRemoteUser",
            "threadLoginSession user = [" + com.sap.engine.session.exec.SessionExecContext.getExecutionContext().threadLoginSession.getPrincipal().getName() + "]");
        }
        return com.sap.engine.session.exec.SessionExecContext.getExecutionContext().threadLoginSession.getPrincipal().getName();
      }
    }

    SecuritySession ss = (SecuritySession)getSecuritySession();
    if ((ss != null) && (!ss.isAnonymous())) {
      if (traceLocation.beDebug()) {
        trace("getRemoteUser", "user = [" + ss.getPrincipal().getName() + "]");
      }
      return ss.getPrincipal().getName();
    }
    if (traceLocation.beDebug()) {
      trace("getRemoteUser", "user = [" + null + "]");
    }
    return null;
  }
  
  /**
   * Returns a java.security.Principal object containing the name of the current authenticated user.
   *
   * @return     the name of the current authenticated user
   */
  public Principal getUserPrincipal() {
    //SRV.12.3
    // If no user has been authenticated, the getRemoteUser method returns null, the
    //isUserInRole method always returns false, and the getUserPrincipal method
    //returns null.
    // Note : this is not valid for old applications , for backward compatibility
    if (!applicationContext.getWebApplicationConfiguration().isProgrammaticSecurityAgainstRunAsIdentity()) {
      if (com.sap.engine.session.exec.SessionExecContext.getExecutionContext().threadLoginSession.isAnonymous()) {
        if (traceLocation.beDebug()) {
          trace("getUserPrincipal", "threadLoginSession is anonymous, method return: [null]");
        }
        return null;
      } else {
        if (traceLocation.beDebug()) {
          trace("getUserPrincipal", "threadLoginSession return: [" + com.sap.engine.session.exec.SessionExecContext.getExecutionContext().threadLoginSession.getPrincipal() + "]");
        }
        return com.sap.engine.session.exec.SessionExecContext.getExecutionContext().threadLoginSession.getPrincipal();
      }
    }

    SecuritySession ss = (SecuritySession)getSecuritySession();
    if ((ss != null) && (!ss.isAnonymous())) {
      if (traceLocation.beDebug()) {
        trace("getUserPrincipal", "return: [" + ss.getPrincipal() + "]");
      }
      return ss.getPrincipal();
    }
    if (traceLocation.beDebug()) {
      trace("getUserPrincipal", "return: [" + null + "]");
    }
    return null;
  }
  
  /**
   * Returns a boolean indicating whether the authenticated user is included in the specified logical "role".
   *
   * @param   role  name of a role
   * @return     if the user is authenticated for this role
   */
  public boolean isUserInRole(String role) {
    Principal principal = null;
    //SRV.12.3
    // If no user has been authenticated, the getRemoteUser method returns null, the
    //isUserInRole method always returns false, and the getUserPrincipal method
    //returns null.
    if (!applicationContext.getWebApplicationConfiguration().isProgrammaticSecurityAgainstRunAsIdentity()){
      if (com.sap.engine.session.exec.SessionExecContext.getExecutionContext().threadLoginSession.isAnonymous()) {
        if (traceLocation.beDebug()) {
          trace("isUserInRole", "threadLoginSession is anonymous, role = [" + role + "], method return: [" + false + "]");
        }
        return false;
      } else {
        principal = com.sap.engine.session.exec.SessionExecContext.getExecutionContext().threadLoginSession.getPrincipal();
        if (traceLocation.beDebug()) {
          trace("isUserInRole", "threadLoginSession principal = [" + principal + "]");
        }
      }
    }
    String prevContextID = applicationContext.setPolicyContextID(true);
    boolean beDebug = traceLocation.beDebug();
    try {
      //should be empty String instead of null 
      WebRoleRefPermission webRoleRefPermission = new WebRoleRefPermission(getCurrentlyInvokedServletName() == null ? "":getCurrentlyInvokedServletName(), role);
      if (principal == null) {
        SecuritySession securitySession = getSecuritySession();
        if (securitySession != null) {
          principal = securitySession.getPrincipal();
        }
        if (traceLocation.beDebug()) {
          trace("isUserInRole", "principal = [" + principal + "]");
        }
      }
      //AccessController.checkPermission(webRoleRefPermission); todo - use it after fixed in UME
      boolean result = Policy.getPolicy().implies(new ProtectionDomain(null, null, null,
              new Principal[]{principal}),
              webRoleRefPermission);
      if (!result) {
        if (beDebug) {
          trace("isUserInRole", "role = [" + role + "], return: [" + false + "]");
        }
        return false;
      }
      if (beDebug) {
        trace("isUserInRole", "role = [" + role + "], return: [" + true + "]");
      }
      return true;
    } catch (AccessControlException e) {
      if (beDebug) {
        trace("isUserInRole", "role = [" + role + "], return: [" + false + "]");
      }
      return false;
    } finally {
      applicationContext.restorePrevPolicyContextID(prevContextID);
    }
  }
  
  public void setUserAuthSessionInRunAS(boolean isInDoAs, SecuritySession ss) {
    this.isInDoAs = isInDoAs;
    if (isInDoAs) {
      this.ss = ss;
    } else {
      this.ss = null;
    }
  }
  
  public SecuritySession getSecuritySession() {
    if (isInDoAs) { // todo - check if in Subject.doAs
      return ss;
    }
    SecurityContextObject securityContext = (SecurityContextObject) applicationContext.getSecurityContext();
    if (securityContext == null) {
        return null;
    }
    return securityContext.getSession();
  }

  /**
   * Returns a RequestDispatcher object that acts as a wrapper for the resource located at the given path.
   *
   * @param   path  path of some file
   * @return     a RequestDispatcher object that acts as a wrapper for the resource located at the given path
   */
  public RequestDispatcher getRequestDispatcher(String path) {
    boolean beDebug = traceLocation.beDebug();
    if (path == null) {
      if (beDebug) {
        trace("getRequestDispatcher", "path = [" + path + "], return: [null]");
      }
      return null;
    }

    if (path.startsWith("/")) {
      RequestDispatcher requestDispatcher = applicationContext.getServletContext().getRequestDispatcher(path);
      if (beDebug) {
        trace("getRequestDispatcher", "path = [" + path + "], return: [" + requestDispatcher + "]");
      }
      return requestDispatcher;
    } else { //relative to current request
      String req = getRequestURIinternal();
      String alias = getContextPath();

      if (!alias.equals("")) {
        req = req.substring(req.indexOf(alias) + alias.length());
      }

      int indSep = req.lastIndexOf('/');

      if (indSep > 0) {
        req = req.substring(0, indSep + 1) + path;
      } else {
        req = "/" + path;
      }

      RequestDispatcher requestDispatcher = applicationContext.getServletContext().getRequestDispatcher(req);
      if (beDebug) {
        trace("getRequestDispatcher", "path = [" + path + "], return: [" + requestDispatcher + "]");
      }
      return requestDispatcher;
    }
  }  
  
  /**
   * Method for storing data in runtime session model. It is called when stored post parameters in post
   * parameters preservation scenarios and a method is implemented in HttpServletFacadeWrapper
   * (implementation of the method from interface ServletRequest) which allow 
   * security to store data in runtime session model
   * @param key
   * @param data
   * @throws IllegalArgumentException if key is null or empty string; or data is null 
   */
  public void storeDataInSessionRuntime(String key, Object data) {   
    
    boolean beDebug = securityLocation.beDebug();
    if (beDebug) {
      traceSecurityLocation("storeDataInSessionRuntime", "storing data in the client session runtime. Key: " + key + "; Data: " + data);
    }
    
    if (key == null || key.length() == 0 ) {
      if (beDebug) {
        traceSecurityLocation("storeDataInSessionRuntime", "Key is null or empty string.");
      }
      throw new IllegalArgumentException("Key could not be null or empty string.");
    }
    
    if (data == null) {
      if (beDebug) {
        traceSecurityLocation("storeDataInSessionRuntime", "Data is null.");
      }
      throw new IllegalArgumentException("Data could not be null.");
    }
    try{
      invalidatedMonitor.lock();
      // create JSESSIONID if it doesn't exist       
      if(sessionRequest.getSessionId() == null) {
        String sessionId = this.getSessionID(httpParameters);
        
          if (valid) {
            sessionRequest.reDoSessionRequest(applicationContext.getSessionServletContext().getSession(), sessionId, SessionServletContext.getSessionIdFromJSession(sessionId));
          } else {
            IllegalStateException ise = new IllegalStateException("Request object used outside its context. RequestContext has been destroyed.");
            if (securityLocation.beError()) {              
              LogContext.getLocation(LogContext.LOCATION_SECURITY).traceError("ASJ.web.000707",
                  "client [{0}] RequestContext.storeDataInSessionRuntime [{1}] in application [{2}]: " + 
                  "storeDataInSessionRuntime(byte[]) method is called on a request object which associated " +
                  "HttpSessionRequest object [{3}] is not valid. This means that the " +
                  "Request object on which SecurityRequest.storeDataInSessionRuntime() method is called is used outside the service scope. " +
                  "ERROR: {4}",
                  new Object[]{httpParameters.getRequest().getClientId(), getObjectInstance(),
                  applicationContext.getAliasName(), sessionRequest, LogContext.getExceptionStackTrace(ise)}, null, null);
            }
            throw ise;
          }      
        setSessionCookieInResponse(sessionId);    
      }
      
      Object storedData = sessionRequest.getData();
      
      if (storedData != null && storedData instanceof ConcurrentHashMap) {
        if (beDebug) {          
          traceSecurityLocation("storeDataInSessionRuntime", "The map exists in runtime session model. Storing data.");
        }
        ConcurrentHashMap dataMap = (ConcurrentHashMap)storedData;
        //trace old object      
        if (beDebug) {
          Object oldObj = null;
          oldObj = dataMap.get(key);
          traceSecurityLocation("storeDataInSessionRuntime", "old value = [" + oldObj + "].");
        }
        dataMap.put(key, data);
      } else {
        if (beDebug) {          
          traceSecurityLocation("storeDataInSessionRuntime", "Create a  new map.");
        }
        
        //in all other cases create new hash map instance and store it in the session model
        //if there is any other data different from ConcurrentHashMap, the data will be lost
        ConcurrentHashMap dataMap = new ConcurrentHashMap();
        dataMap.put(key, data);
        sessionRequest.storeData(dataMap, applicationContext.getWebApplicationConfiguration().getSessionTimeout());
      }
    
    }finally{
      invalidatedMonitor.unlock();
    }
    if (beDebug) {      
      traceSecurityLocation("storeDataInSessionRuntime", "Data stored successfully in session runtime model. Sessionid: " + sessionRequest.getSessionId());
    }
  }
  
  /**
   * Method for getting data from runtime session model. It is called in post
   * parameters preservation scenarios and a method is implemented in HttpServletFacadeWrapper
   * (implementation of the method from interface ServletRequest) which allow 
   * security to get data from runtime session model
   * @param key
   * @return Data object if exists with this key or null otherwise
   * @throws IllegalArgumentException if key is null or empty string; or data is null 
   */
  public Object getDataFromSessionRuntime(String key) {
    boolean beDebug = securityLocation.beDebug();
    if (beDebug) {
      traceSecurityLocation("getDataFromSessionRuntime", "getting data from the client session runtime with key: " + key + ".");
    }
    
    if (key == null || key.length() == 0 ) {
      if (beDebug) {
        traceSecurityLocation("getDataFromSessionRuntime", "Key is null or empty string.");
      }
      throw new IllegalArgumentException("Key could not be null or empty string.");
    }
    
    String sessionId = this.getExistingSessionID(httpParameters);
    
    if (sessionId == null) {
      if (beDebug) {
        traceSecurityLocation("removeDataFromSessionRuntime", "sessionID from getExistingSessionID() is null; throwing exception");
      }      
      throw new IllegalStateException("JSession id could not be found in the request. The request has no associated runtime model and session.");
    }
    
    // create JSESSIONID if it doesn't exist       
    if(sessionRequest.getSessionId() == null) {      
      try{
        invalidatedMonitor.lock();
        if (valid) {
          sessionRequest.reDoSessionRequest(applicationContext.getSessionServletContext().getSession(), sessionId, SessionServletContext.getSessionIdFromJSession(sessionId));
        } else {
          IllegalStateException ise = new IllegalStateException("Request object used outside its context. RequestContext has been destroyed.");
          if (securityLocation.beError()) {            
            LogContext.getLocation(LogContext.LOCATION_SECURITY).traceError("ASJ.web.000708",
                "client [{0}] RequestContext.getDataFromSessionRuntime [{1}] in application [{2}]: " + 
                "getDataFromSessionRuntime method is called on a request object which associated " +
                "HttpSessionRequest object [{3}] is not valid. This means that the " +
                "Request object on which SecurityRequest.getDataFromSessionRuntime() method is called is used outside the service scope. " +
                "ERROR: {4}",
                new Object[]{httpParameters.getRequest().getClientId(), getObjectInstance(),
                applicationContext.getAliasName(), sessionRequest, LogContext.getExceptionStackTrace(ise)}, null, null);
          }
          throw ise;
        }
      }finally{
        invalidatedMonitor.unlock();
      }
      //setSessionCookieInResponse(sessionId);    
    }
    
    Object storedData = sessionRequest.getData();
    
    if (storedData != null && storedData instanceof ConcurrentHashMap) {
      ConcurrentHashMap dataMap = (ConcurrentHashMap)storedData;
      
      Object data = dataMap.get(key);
      //trace old data object      
      if (beDebug) {        
        traceSecurityLocation("getDataFromSessionRuntime", "data taken from the session model = [" + data + "].Sessionid: " + sessionId);
      }
      return data;
    } else { 
      if (beDebug) {        
        traceSecurityLocation("getDataFromSessionRuntime", "Nothing get. Data taken from the session model = [" + storedData + "]. Sessionid: " + sessionId);
      }
      return null;
    }
  }
  
  /**
   * Method for removing data from runtime session model. It is called when stored post parameters in post
   * parameters preservation scenarios and a method is implemented in HttpServletFacadeWrapper
   * (implementation of the method from interface ServletRequest) which allow 
   * security to remove data from runtime session model
   * @param key
   * @return Data object if exists with this key or null otherwise
   * @throws IllegalArgumentException if key is null or empty string; or data is null 
   */
  public Object removeDataFromSessionRuntime(String key) {
    boolean beDebug = securityLocation.beDebug();
    if (beDebug) {
      traceSecurityLocation("removeDataFromSessionRuntime", "Removing data from the client session runtime with key: " + key + ".");
    }
    
    if (key == null || key.length() == 0 ) {
      if (beDebug) {
        traceSecurityLocation("removeDataFromSessionRuntime", "Key is null or empty string.");
      }
      throw new IllegalArgumentException("Key could not be null or empty string.");
    }
    
    String sessionId = this.getExistingSessionID(httpParameters);
    
    if (sessionId == null) {
      if (beDebug) {
        traceSecurityLocation("removeDataFromSessionRuntime", "sessionID from getExistingSessionID() is null; throwing exception");
      }      
      throw new IllegalStateException("JSession id could not be found in the request. The request has no associated runtime model and session.");
    }
    try{
      invalidatedMonitor.lock();
      // create JSESSIONID if it doesn't exist       
      if(sessionRequest.getSessionId() == null) {      
        
          if (valid) {
            sessionRequest.reDoSessionRequest(applicationContext.getSessionServletContext().getSession(), sessionId, SessionServletContext.getSessionIdFromJSession(sessionId));
          } else {
            IllegalStateException ise = new IllegalStateException("Request object used outside its context. RequestContext has been destroyed.");
            if (securityLocation.beError()) {
              LogContext.getLocation(LogContext.LOCATION_SECURITY).traceError("ASJ.web.000709",
                  "client [{0}] RequestContext.removeDataFromSessionRuntime [{1}] in application [{2}]: " + 
                  "removeDataFromSessionRuntime method is called on a request object which associated " +
                  "HttpSessionRequest object [{3}] is not valid. This means that the " +
                  "Request object on which SecurityRequest.removeDataFromSessionRuntime() method is called is used outside the service scope. " +
                  "ERROR: {4}",
                  new Object[]{httpParameters.getRequest().getClientId(), getObjectInstance(),
                  applicationContext.getAliasName(), sessionRequest, LogContext.getExceptionStackTrace(ise)}, null, null);
            }
            throw ise;
          }
        
        //setSessionCookieInResponse(sessionId);    
      }
      
      Object storedData = sessionRequest.getData();
      
      if (storedData != null && storedData instanceof ConcurrentHashMap) {
        ConcurrentHashMap dataMap = (ConcurrentHashMap)storedData;
        
        Object data = dataMap.remove(key);
            
        if (beDebug) {          
          traceSecurityLocation("removeDataFromSessionRuntime", "data taken from the session model = [" + data + "]. Sessionid: " + sessionId);
        }
        
        if (dataMap.size() == 0) {
          if (beDebug) {          
            traceSecurityLocation("removeDataFromSessionRuntime", "Removing ");
          }
          sessionRequest.removeData();
        }
        return data;
      } else { 
        if (beDebug) {          
          traceSecurityLocation("removeDataFromSessionRuntime", "Nothing removed. Data taken from the session model = [" + storedData + "]. Sessionid: " + sessionId);
        }
        return null;
      }
    }finally{
      invalidatedMonitor.unlock();
    }
  }
  
  /**
   * store dataBytes in the client session runtime
   * @param dataBytes
   */
  public void storePostData(byte[] dataBytes) {
    boolean beDebug = securityLocation.beDebug();
    if (beDebug) {
      traceSecurityLocation("storePostData", "storing data in the client session runtime");
    }  
    
    storeDataInSessionRuntime(SecurityRequest.POST_PARAMETERS_KEY_FOR_RUNTIME_SESSION, dataBytes);
    if (beDebug) {
      String dataBytesStr = null;
      if (dataBytes == null) {
        dataBytesStr = "null";
      }
      else {
        dataBytesStr = new String(dataBytes);
      }
      traceSecurityLocation("storePostData", "Post parameters are stored in the client session runtime with sessionid = " + sessionRequest.getSessionId() + ". Parameters: " + dataBytesStr);
    }
  }  
  
  /**
   * restore information from client session runtime
   * @return
   */
  public byte[] restorePostData() {
    boolean beDebug = securityLocation.beDebug();    
    
    Object obj = removeDataFromSessionRuntime(SecurityRequest.POST_PARAMETERS_KEY_FOR_RUNTIME_SESSION);
    
    if (obj == null) {
      if (beDebug) {
        traceSecurityLocation("restorePostParams", "Object from session runtime model is null; returning null");
      }
      return null;
    } else if (!(obj instanceof byte[])) {
      if (beDebug) {
    	  traceSecurityLocation("restorePostParams", "Object from session runtime model is not a byte array; throwing exception. Object type: " + obj.getClass().toString());
      }
      throw new IllegalStateException("Unexpected object type found for post parameters preservation");
    } 
    
    if (beDebug) {
    	byte[] params = (byte[]) obj;
    	String dataBytesStr = null;
      if (params == null) {
        dataBytesStr = "null";
      }
      else {
        dataBytesStr = new String(params);
      }
    	traceSecurityLocation("restorePostParams", "Post parameters are restored from the session. Prameters: " + dataBytesStr);
    }
    return (byte[]) obj;    
  }
  
  public void resetSession() {
    if (traceLocation.beDebug()) {
      trace("resetSession", "sessionAccessed: [" + sessionAccessed + "]");
    }

    try {
      if (!sessionAccessed) {
        ApplicationSession applicationSession = (ApplicationSession) getSession(false);
        if (applicationSession != null && applicationSession.isValid()) {
          applicationSession.accessed();
        }
      }
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable t) {
      // TODO:Polly severity:warning
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(
          currentLocation, "ASJ.web.000120", "Cannot finalize session access.",
          t, null, null);
    }
  }
  
  public void invalidate() {
    if (traceLocation.beDebug()) {
      trace("invalidate", "sessionRequest: [" + sessionRequest + "]");
    }
    try{
      invalidatedMonitor.lock();	
      sessionRequest.endRequest(0);
      valid = false;
    }finally{
    	invalidatedMonitor.unlock();
    }
  }
  
  
  public void activateInvalidaterMonitor(){
	 try{
		 invalidatedMonitor.lock();
	 }catch (Exception e){
		 if (traceLocation.beError()) {
			 //TODO generate message ID
	          LogContext.getLocation(LogContext.LOCATION_SERVLET_REQUEST).traceError("ASJ.web.000665",
	              "client [{0}] RequestContext.activateInvalidaterMonitor [{1}] in application [{2}]:" +
	              "activateInvalidaterMonitor() method is throwing ERROR: {3}",
	              new Object[]{httpParameters.getRequest().getClientId(), getObjectInstance(),
	              applicationContext.getAliasName(), LogContext.getExceptionStackTrace(e)}, null, null);
	        };
	      invalidatedMonitor.unlock();
	 }
  }
  
  public void releaseInvalidaterMonitor(){
	  try{
		  	 invalidatedMonitor.unlock();
		 }catch (Exception e){
			 if (traceLocation.beWarning()) {
				 //TODO generate message ID
		          LogContext.getLocation(LogContext.LOCATION_SERVLET_REQUEST).traceWarning("ASJ.web.000667",
		        		  "client [{0}] RequestContext.releaseInvalidaterMonitor [{1}] in application [{2}]:" +
			              "releaseInvalidaterMonitor() method is throwing ERROR: {3}",
		              new Object[]{httpParameters.getRequest().getClientId(), getObjectInstance(),
		              applicationContext.getAliasName(), LogContext.getExceptionStackTrace(e)}, null, null);
		        };
		 }
  }

}



