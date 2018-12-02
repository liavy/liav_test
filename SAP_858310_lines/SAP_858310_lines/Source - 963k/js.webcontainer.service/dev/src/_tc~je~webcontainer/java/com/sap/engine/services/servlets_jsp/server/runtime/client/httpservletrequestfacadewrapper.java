/*
 * Copyright (c) 2006-2008 by SAP AG, Walldorf.,
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
import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import com.sap.engine.interfaces.security.SecuritySession;
import com.sap.engine.interfaces.security.auth.SecurityRequest;
import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.services.httpserver.interfaces.HttpParameters;
import com.sap.engine.services.httpserver.lib.ResponseCodes;
import com.sap.engine.services.httpserver.lib.Responses;
import com.sap.engine.services.httpserver.server.SessionRequestImpl;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.application.WebEvents;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIllegalStateException;
import com.sap.engine.session.runtime.http.HttpSessionRequest;
import com.sap.tc.logging.Location;


/**
 * @author diyan-y
 */
public class HttpServletRequestFacadeWrapper extends HttpServletRequestFacade
    implements SecurityRequest {
  private static Location currentLocation = Location.getLocation(HttpServletRequestFacadeWrapper.class);
  private static Location traceLocation = LogContext.getLocationServletRequest();
    
  private RequestContext requestContext = null;

  private ConcurrentHashMap<Long, RequestContext> requestContexts = new ConcurrentHashMap<Long, RequestContext>();

  /**
   * ID of the thread that initially starts request processing.
   */
  private Long threadId = null;

  private boolean recicled = true;
  private boolean timeout = false;

  public void init(ApplicationContext applicationContext, HttpParameters httpParameters, HttpServletResponseFacadeWrapper response) {
    requestContext = new RequestContext(applicationContext, httpParameters.getSessionRequest(), httpParameters, response);
    threadId = Thread.currentThread().getId();
    if (traceLocation.beDebug()) {
      traceDebug("init", threadId.toString());
    }
    RequestContextObject.setCurrentRequestContextId(threadId);
    requestContexts.put(threadId, requestContext);
    recicled = false;
    timeout = false;
    super.init(applicationContext, httpParameters, response);
    requestContext.setAttributes(attributes);
  }

  public void setContext(ApplicationContext applicationContext) {
    getRequestContext().setApplicationContext(applicationContext);
    // super.context = applicationContext;
  }

  public void reset() {
    if (traceLocation.beDebug()) {
      traceDebug("reset", threadId.toString());
    }
    try {
      //TODO try to do it other way
      //First notify listener, then reset request
      try {
        WebEvents events = getRequestContext().getApplicationContext().getWebEvents();
        if (events != null) {
          events.requestDestroyed(this);
        }
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable t) {
    	  //TODO:Polly type:trace reason- in order to avoid double logging for one and the same problem
    	  //requestDestroyed() method must have already logged warning message for this issue
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000119",
          "Cannot invoke requestDestroyed listeners.", t, null, null);
      }
    } finally {
        try{
        	getRequestContext().resetSession();
        }finally{
        	super.reset();
        }
        recicled = true;
        requestContext = null;
        requestContexts.clear();
        threadId = null;

    }
  }

  public HttpSessionRequest getSessionRequest() {
    if (traceLocation.beDebug()) {
      traceDebug("getSessionRequest", threadId.toString());
    }
    return getRequestContext().getSessionRequest();
  }

  public HttpSessionRequest getSessionRequest_activateInvalidatorMonitor() {
	    if (traceLocation.beDebug()) {
	      traceDebug("getSessionRequest", threadId.toString());
	    }
	    getRequestContext().activateInvalidaterMonitor();
	    return getRequestContext().getSessionRequest();
	  }
  public void releaseInvalidatorMonitor(){
	  getRequestContext().releaseInvalidaterMonitor();
  }

  public void setSessionRequest(HttpSessionRequest sessionRequest) {
    if (traceLocation.beDebug()) {
      traceDebug("setSessionRequest", threadId.toString());
    }
    getRequestContext().setSessionRequest(sessionRequest);
  }

  public HttpParameters getHttpParameters() {
    if (traceLocation.beDebug()) {
      traceDebug("getHttpParameters", threadId.toString());
    }
    return getRequestContext().getHttpParameters();
  }

  public ApplicationContext getApplicationContext() {
    if (traceLocation.beDebug()) {
      traceDebug("getServletContext", threadId.toString());
    }
    return getRequestContext().getApplicationContext();
  }

  /**
   * Gives access to the current <code>ServletContex<code> of the request
   *
   * @return
   * the current <code>ServletContex<code> of the request
   */
  public ServletContext getServletContext() {
    if (traceLocation.beDebug()) {
      traceDebug("getServletContext", threadId.toString());
    }
    return getRequestContext().getApplicationContext().getServletContext();
  }

  /**
   * Returns the name of the authentication scheme used to protect the servlet
   * or null  if the servlet was not protected.
   *
   * @return     the name of the authentication scheme
   */
  public String getAuthType() {
    // If there is an programmatically forced authentication type
    if (traceLocation.beDebug()) {
      traceDebug("getAuthType", threadId.toString());
    }
    return getRequestContext().getAuthType();
  }

  public void setForcedAuthType(String authType) {
    if (traceLocation.beDebug()) {
      traceDebug("setForcedAuthType", threadId + ", [" + authType + "]");
    }
    getRequestContext().setForcedAuthType(authType);
  }

  /**
   * Returns the value of the named attribute as an Object, or null if
   * no attribute of the given name exists.
   *
   * @param   s  name of attribute
   * @return     value of the attribute with name s or null
   */
  public Object getAttribute(String s) {
    Object result = getRequestContext().getAttribute(s);
    if (traceLocation.beDebug()) {
      traceDebug("getAttribute", threadId + ", name = [" + s + "], result = [" + result + "]");
    }
    return result;
  }

  /**
   * Returns an Enumeration containing the names of the attributes available to this request.
   *
   * @return     enumeration with the names of the attributes
   */
  public Enumeration getAttributeNames() {
    return getRequestContext().getAttributeNames();
  }

  /**
   * Stores an attribute in this request.
   *
   * @param   s  name of the attribute
   * @param   obj  value of the attribute
   */
  public void setAttribute(String s, Object obj) {
    if (traceLocation.beDebug()) {
      traceDebug("setAttribute", threadId + ", name = [" + s + "], value = [" + obj + "]");
    }
    if (obj == null) {
      removeAttribute(s);
      return;
    }
    Object oldValue = getRequestContext().setAttribute(s, obj);
    if (oldValue != null) { //the attribute is replaced
      getRequestContext().getApplicationContext().getWebEvents().requestAttributeReplaced(this, s, oldValue);
    } else {
      getRequestContext().getApplicationContext().getWebEvents().requestAttributeAdded(this, s, obj);
    }
  }

  /**
   * Removes an attribute from this request.
   *
   * @param   s  name of the attribute
   */
  public void removeAttribute(String s) {
    if (traceLocation.beDebug()) {
      traceDebug("removeAttribute", threadId + ", name = [" + s + "]");
    }
    Object obj = getRequestContext().removeAttribute(s);
    getRequestContext().getApplicationContext().getWebEvents().requestAttributeRemoved(this, s, obj);
  }

  public Cookie[] getCookies() {
    if (traceLocation.beDebug()) {
      traceDebug("getCookies", threadId.toString());
    }
    Cookie[] result = getRequestContext().getCookies();
    if (traceLocation.beDebug()) {
      String res = "";
      for (int i = 0; result != null && i < result.length; i++) {
        if (result[i] == null) {
          res += result[i] + ", ";
        } else {
          res += result[i].getName() + " = " + result[i].getValue() + ", ";
        }
      }
      if (res.endsWith(", ")) {
        res = res.substring(0, res.length() - 2);
      }
      traceDebug("getCookies", "return: [" + res + "]");
    }
    return result;
  }

  public String getPathInfo() {
    String pathInfo = getRequestContext().getPathInfo();
    if (traceLocation.beDebug()) {
      traceDebug("getPathInfo", threadId + "pathInfo = [" + pathInfo + "]");
    }
    return pathInfo;
  }

  public void setPathInfo(String pathInfo) {
    if (traceLocation.beDebug()) {
      traceDebug("setPathInfo", threadId + "pathInfo = [" + pathInfo + "]");
    }
    getRequestContext().setPathInfo(pathInfo);
  }

  public String getPathTranslated() {
    String pathTranslated = getRequestContext().getPathTranslated((String) getAttribute("javax.servlet.include.realpath_path"));
    if (traceLocation.beDebug()) {
      traceDebug("getPathTranslated", threadId + ", pathTranslated = [" + pathTranslated + "]");
    }
    return pathTranslated;
  }

  public String getRealPath(String path) {
    String realPath = getRequestContext().getRealPath(path, (String) getAttribute("javax.servlet.include.realpath_path"));
    if (traceLocation.bePath()) {
      if (realPath != null) {
        tracePath("getRealPath", threadId + ", path = [" + path + "], return: [" + realPath.replace('/', File.separatorChar) + "]");
      } else {
        tracePath("getRealPath", threadId + ", path = [" + path + "], return: [null]");
      }
    }
    return realPath;
  }

  public String getContextPath() {
    String contextPath = getRequestContext().getContextPath();
    if (traceLocation.beDebug()) {
      traceDebug("getContextPath", threadId + ", contextPath = [" + contextPath + "]");
    }
    return contextPath;
  }

  public String getRequestedSessionId() {
    String requestedSessionId = getRequestContext().getRequestedSessionId();
    if (traceLocation.beDebug()) {
      traceDebug("getRequestedSessionId", threadId + ", requestedSessionId = [" + requestedSessionId + "]");
    }
    return requestedSessionId;
  }

  public HttpSession getSession() {
    HttpSession httpSession = getSession(true);
    if (traceLocation.beDebug()) {
      traceDebug("getSession", threadId + ", httpSession = [" + httpSession + "]");
    }
    return httpSession;
  }

  public HttpSession getSession(boolean flag) {
	HttpSession httpSession  = getRequestContext().getSession(flag);

    if (traceLocation.beDebug()) {
      traceDebug("getSession", threadId + ", flag = [" + flag + "], httpSession = [" + httpSession + "]");
    }
    return httpSession;
  }

  public boolean isRequestedSessionIdFromCookie() {
    boolean result = getRequestContext().isRequestedSessionIdFromCookie();
    if (traceLocation.beDebug()) {
      traceDebug("isRequestedSessionIdFromCookie", threadId + ", return: [" + result + "]");
    }
    return result;
  }

  public boolean isRequestedSessionIdFromURL() {
    boolean result = getRequestContext().isRequestedSessionIdFromURL();
    if (traceLocation.beDebug()) {
      traceDebug("isRequestedSessionIdFromURL", threadId + ", return: [" + result + "]");
    }
    return result;
  }

  public void setServletName(String servletName) {
    getRequestContext().setServletName(servletName);
  }

  public String getServletName() {
    return getRequestContext().getServletName();
  }

  public String getServletPath() {
    String servletPath = getRequestContext().getServletPath();
    if (traceLocation.bePath()) {
      tracePath("getServletPath", threadId + "return: [" + servletPath + "]");
    }
    return servletPath;
  }

  public void setServletPath(String servletPath) {
    if (traceLocation.bePath()) {
      tracePath("setServletPath", threadId + "servletPath: [" + servletPath + "]");
    }
    getRequestContext().setServletPath(servletPath);
  }

  public String getRequestURIinternal() {
    String requestURIinternal = getRequestContext().getRequestURIinternal();
    if (traceLocation.beDebug()) {
      traceDebug("getRequestURIinternal", threadId + ", requestURIinternal = [" + requestURIinternal + "]");
    }
    return requestURIinternal;
  }

  /**
   * Called when the servlet service method has finished,
   * updates the name of the currently invoked servlet.
   */
  public void markServiceFinished() {
    getRequestContext().markServiceFinished();
  }//markServiceFinished

  /**
   * Updates the name of the currently invoked servlet.
   * @param invokedServletName
   */
  public void markServiceStarted(String invokedServletName) {
    getRequestContext().markServiceStarted(invokedServletName);
  }

  /**
   * Returns the name of the servlet that is currently processed.
   * @return
   */
  public String getCurrentlyInvokedServletName() {
    return getRequestContext().getCurrentlyInvokedServletName();
  }

  public String getRealPathLocal(String path) {
    return getRequestContext().getRealPathLocal(path, (String)getAttribute("javax.servlet.include.realpath_path"));
  }

  public String getRemoteUser() {
    return getRequestContext().getRemoteUser();
  }

  /**
   * Returns a boolean indicating whether the authenticated user is included in the specified logical "role".
   *
   * @param   role  name of a role
   * @return     if the user is authenticated for this role
   */
  public boolean isUserInRole(String role) {
    boolean result = getRequestContext().isUserInRole(role);
    if (traceLocation.bePath()) {
      tracePath("isUserInRole", "role = [" + role + "], return: [" + result + "]");
    }
    return result;
  }

  public void setUserAuthSessionInRunAS(boolean isInDoAs, SecuritySession ss) {
    getRequestContext().setUserAuthSessionInRunAS(isInDoAs, ss);
  }


  /**
   * Returns an absolute URI with the given scheme and path and adequate
   * host and port.
   * <p/>
   * The method accepts only absolute paths, e.g. such that start with a
   * leading '/' and interprets them as relative to the servlet container root.
   *
   * @param scheme The required scheme. Allowed values are "http" and "https"
   * @param path   An absolute path that start with a leading '/'
   * @return An absolute URI or <code>null</code> in case that servlet container
   *         can not find adequate host and port
   */
  public String getURLForScheme(String scheme, String path) {
    return getRequestContext().getResponse().getURLForScheme(scheme, path);
  }
  
  /**
   * Returns a byte array containing the POST data of the current POST request with encoding.   * 
   * @return
   */
  public byte[] getPostDataBytes() {    
    byte[] postBytes = getHttpParameters().getRequestParametersBody().getBytes();
    if (securityLocation.beDebug()) {
      String msg = "returning post data bytes. Bytes are taken from the request.[\r\n";
      if (postBytes == null) {
        msg += "(null)";
      } else {
        msg += new String(postBytes);
      }
      msg += "\r\n]\r\n";
      traceSecurityLocation("getPostDataBytes", msg);
    }
    return postBytes;
  }

  public void storeDataInSessionRuntime(String key, Object data) {    
    if (SecurityRequest.POST_PARAMETERS_KEY_FOR_RUNTIME_SESSION.equals(key)) {
      if (securityLocation.beDebug()) {
        traceSecurityLocation("storeDataInSessionRuntime", "Data with key " + SecurityRequest.POST_PARAMETERS_KEY_FOR_RUNTIME_SESSION +
            " could not be stored. This key is preserved for post parameters.");
      }
      throw new IllegalStateException("Data with key " + SecurityRequest.POST_PARAMETERS_KEY_FOR_RUNTIME_SESSION +
          "could not be stored. This key is preserved for post parameters.");
    }
    
    getRequestContext().storeDataInSessionRuntime(key, data);
  }
  
  /**
   * 
   * @param key
   * @return
   */
  public Object getDataFromSessionRuntime(String key) {
    if (SecurityRequest.POST_PARAMETERS_KEY_FOR_RUNTIME_SESSION.equals(key)) {
      if (securityLocation.beDebug()) {
        traceSecurityLocation("getDataFromSessionRuntime", "Data with key " + SecurityRequest.POST_PARAMETERS_KEY_FOR_RUNTIME_SESSION +
            " could not be received. This key is preserved for post parameters.");
      }
      throw new IllegalStateException("Data with key " + SecurityRequest.POST_PARAMETERS_KEY_FOR_RUNTIME_SESSION +
          "could not be received. This key is preserved for post parameters.");
    }
    return getRequestContext().getDataFromSessionRuntime(key);
  }
  
  /**
   * 
   * @param key
   * @return
   */
  public Object removeDataFromSessionRuntime(String key) {
    if (SecurityRequest.POST_PARAMETERS_KEY_FOR_RUNTIME_SESSION.equals(key)) {
      if (securityLocation.beDebug()) {
        traceSecurityLocation("getDataFromSessionRuntime", "Data with key " + SecurityRequest.POST_PARAMETERS_KEY_FOR_RUNTIME_SESSION +
            " could not be removed. This key is preserved for post parameters.");
      }
      throw new IllegalStateException("Data with key " + SecurityRequest.POST_PARAMETERS_KEY_FOR_RUNTIME_SESSION +
          "could not be removed. This key is preserved for post parameters.");
    }
    
    return getRequestContext().removeDataFromSessionRuntime(key);
  }
  
  /**
   * Stores the POST data of the current POST request in the Runtime Session Model.
   */
  public void storePostDataBytes() {
    if (securityLocation.beDebug()) {
    	traceSecurityLocation("storePostDataBytes", "storing post parameters in client "
          + "session. Post parameters are taken from the request\r\n");
    }
    byte[] postBytes = getHttpParameters().getRequestParametersBody().getBytes();
    storePostDataBytes(postBytes);
  }

  /**
   * Stores the given byte array as POST data information in the Runtime Session Model.
   * @param bytes
   */
  public void storePostDataBytes(byte[] bytes) {
    if (securityLocation.beDebug()) {
    	traceSecurityLocation("storePostDataBytes", "storePostDataBytes(byte[] bytes) method, "
          + " storing: [\r\n"
          + (bytes == null ? "(null)" : new String(bytes))
          + "\r\n]\r\n");

    }
    getRequestContext().storePostData(bytes);   
  }

  /**
   * Restores the POST data from the Runtime Session Model to the current request.
   */
  public void restorePostDataBytes() {
    if (securityLocation.beDebug()) {
    	traceSecurityLocation("restorePostDataBytes", "restoring post data bytes from application session ");
    }
    byte[] paramsData = null;
    try {
      paramsData = getRequestContext().restorePostData();
    }
    catch (IllegalStateException ex) {
      paramsData=null;
      LogContext.getLocation(LogContext.LOCATION_SECURITY).traceError("ASJ.web.000658",
          "Post data could not be restored. The server was overloaded. Error page could not be returned.", ex, null, null);
    }

    restorePostDataBytes(paramsData);
  }

  /**
   * Uses the given byte array to restore the POST data to the current request.
   * @param bytes
   */
  public void restorePostDataBytes(byte[] bytes) {
    if (securityLocation.beDebug()) {
    	traceSecurityLocation("restorePostDataBytes(byte[] bytes)", "restoring post data bytes; "
          + "bytes: " + (bytes == null ? "(null)" : new String(bytes)));
    }

    if (bytes == null) {
      try {        
        if (securityLocation.beError()) {          
          LogContext.getLocation(LogContext.LOCATION_SECURITY).traceError("ASJ.web.000657",
            "Service not available. Details: The server was overloaded. Impossible to process data.", null, null);          
        }
        HttpSession sess = getSession(false);
        if (sess != null) {
          sess.removeAttribute(SecurityRequest.ORIGINAL_URL);
        }
        
        this.postDataBytes = new byte[0];
        preservedPostParamsStream=new HttpInputStreamImplByteArray(this.postDataBytes);
        this.isPostDataBytesRestored = true;
        this.parametersParsed = false;
        
        response.sendError(ResponseCodes.code_service_unavailable,
            Responses.mess32, Responses.mess33, true);//here we do not need user action
        
      } catch (IOException e) {
        LogContext.getLocation(LogContext.LOCATION_SECURITY).traceError("ASJ.web.000656",
          "Post data could not be restored. The server was overloaded. Error page could not be returned - IOException.", null, null);
      }
    } else {
      this.postDataBytes = bytes;
      preservedPostParamsStream=new HttpInputStreamImplByteArray(bytes);
      this.isPostDataBytesRestored = true;
      this.parametersParsed = false;      
    }
  }

  protected SecuritySession getSecuritySession() {
    return getRequestContext().getSecuritySession();
  }

  /**
   * Returns a java.security.Principal object containing the name of the current authenticated user.
   *
   * @return     the name of the current authenticated user
   */
  public Principal getUserPrincipal() {
    return getRequestContext().getUserPrincipal();
  }

  /**
   * Returns a RequestDispatcher object that acts as a wrapper for the resource located at the given path.
   *
   * @param   path  path of some file
   * @return     a RequestDispatcher object that acts as a wrapper for the resource located at the given path
   *
   */
  public RequestDispatcher getRequestDispatcher(String path) {
    return getRequestContext().getRequestDispatcher(path);
  }

  /**
   * Returns the part of this request's URL(not decoded) from the protocol name up to the query string
   * in the first line of the HTTP request.
   *
   * @return     the part of this request's URL from the protocol name up to the query string
   */
  public String getRequestURI() {
    return getRequestContext().getRequestURI();
  }

  public void setRequestURI(String newURI) {
    getRequestContext().setRequestURI(newURI);
  }

  public void setResponseLengthLog(int totalCount) {
    if (recicled) {
      throw new WebIllegalStateException(WebIllegalStateException.INVALID_REQUEST_OBJECT_USED);
    }
    requestContext.getHttpParameters().setResponseLength(totalCount);
  }

  public int getID() {
    return getRequestContext().getHttpParameters().getRequest().getClientId();
  }

  public boolean isStatisticTraceEnabled() {
    return getRequestContext().getHttpParameters().isMemoryTrace();
  }

  private RequestContext getRequestContext() {
    RequestContext result = null;
    Long currentThreadId = Thread.currentThread().getId();
    if (recicled) {
    	if (timeout){

    		throw new WebIllegalStateException(WebIllegalStateException.TIMEOUT_REQUEST_OBJECT_USED);
    	}
      throw new WebIllegalStateException(WebIllegalStateException.INVALID_REQUEST_OBJECT_USED);
    }
    if (currentThreadId.equals(threadId)) {
      //it is the original thread - use the cached RequestContext
      result = requestContext;
    } else {
      //new thread handles request - get id from the ContextObject
      Long id = RequestContextObject.getCurrentRequestContextId();
      if (id != null) {
        //id is found in the ContextObject - get RequestContext from table - this may be the parent id to parent
        result = (RequestContext)requestContexts.get(id);
      }
    }
    if (result == null) {
      //ContextObject not found - unknown thread
      if (requestContexts.size() == 1) {
        //request is not dispatched in more than one context - use the same RequestContext
        result = requestContext;
      } else {
        String msg = "Request Context not found!";
        Enumeration keys = requestContexts.keys();
        String threads = "{";
        while (keys.hasMoreElements()) {
          Long tName = (Long)keys.nextElement();
          threads += tName + ", ";
        }
        threads += "}";
        msg = "currentThreadId: " + currentThreadId + ", parentThreadId: " + RequestContextObject.getCurrentRequestContextId() +
        ", context size [" + requestContexts.size() + "], requestContexts: " + threads + ", default app [" + requestContext.getApplicationContext().getAliasName() + "]";
        msg = LogContext.getExceptionStackTrace(new Exception(msg));
        throw new IllegalStateException("Request object used outside its context. [" + msg + "]");
      }
    }
    return result;
  }

  /**
   * Creates new RequestContext if the request has to be handled by new thread.
   */
  public HttpSessionRequest prepareRequestContext() {
    HttpSessionRequest newSessionRequest = null;
    Long currentThreadId = Thread.currentThread().getId();
    Long parentThreadId = RequestContextObject.getCurrentRequestContextId();
    boolean beDebug = traceLocation.beDebug();
    if (beDebug) {
      trace("prepareRequestContext", "currentThreadId: " + currentThreadId + ", parentThreadId: " + parentThreadId +
        ", context size [" + requestContexts.size() + "], sessionRequest [" + getRequestContext().getSessionRequest() + "]");
    }
    if (currentThreadId.equals(threadId) || currentThreadId.equals(parentThreadId)) {
      if (beDebug) {
        trace("prepareRequestContext", "currentThreadId: " + currentThreadId + ", parentThreadId: " + parentThreadId + ", no new context");
      }
      RequestContextObject.pushRequestContextId(parentThreadId);
    } else {
      // Request is handled in new thread - create new RequestContext
      RequestContext parentRequestContext = getRequestContext();
      // RequestContext found, now check if it is unknown thread
      Long id = RequestContextObject.getCurrentRequestContextId();

      //
      RequestContextObject.mark(currentThreadId);

      if (id != null && requestContexts.containsKey(id)) {
        //parent thread is found - prepare new request
        ApplicationContext pApplicationContext = parentRequestContext.getApplicationContext();
        HttpSessionRequest pSessionRequest = parentRequestContext.getSessionRequest();
        HttpParameters pHttpParameters = parentRequestContext.getHttpParameters();
        //TODO check this
        HttpServletResponseFacadeWrapper pServletResponse = parentRequestContext.getResponse();
        newSessionRequest = new SessionRequestImpl();
        if (pSessionRequest.isRequested()) {
          try {
            newSessionRequest.doSessionRequest(pSessionRequest.getSessionDomain(), pSessionRequest.getClientCookie(),pSessionRequest.getSessionId());
          } catch (IllegalArgumentException illegalArg) {
            if (traceLocation.beWarning()) {
              LogContext.getLocation(LogContext.LOCATION_SERVLET_REQUEST).traceWarning("ASJ.web.000535",
                    "client [{0}] HttpServletRequest.prepareRequestContext" +
                    " [{1}] in application [{2}]: {3}", new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(), LogContext.getExceptionStackTrace(new Exception("The session request was ended."))}, null, null);
            }
          }
        }
        HttpParameters httpParameters = pHttpParameters;
        RequestContext requestContext = new RequestContext(pApplicationContext,
            httpParameters.getSessionRequest(), httpParameters, pServletResponse);
        requestContext.setSessionRequest(newSessionRequest);
        requestContext.setAttributes(parentRequestContext.getAttributes());
        requestContexts.put(currentThreadId, requestContext);
        RequestContextObject.setCurrentRequestContextId(currentThreadId);
        RequestContextObject.pushRequestContextId(parentThreadId);
        if (beDebug) {
          trace("prepareRequestContext", "currentThreadId: " + currentThreadId + ", parentThreadId: " + parentThreadId +
              ", parentContext [" + pApplicationContext.getAliasName() + "], new context added [" + requestContexts.size() + "]");
        }
      } else {
        //no corresponding RequestContext is found - unknown thread - use default RequestContext
        //no action in needed
        if (beDebug) {
          trace("prepareRequestContext", "currentThreadId: " + currentThreadId + ", parentThreadId: " + parentThreadId +
              ", parentContext unknown, default will be used [" + parentRequestContext.getApplicationContext().getAliasName() +
              "], no context added [" + requestContexts.size() + "]");
        }
      }
    }
    return newSessionRequest;
  }

  public void restoreRequestContext(){
    Long currentThreadId = Thread.currentThread().getId();
    Long id = RequestContextObject.getCurrentRequestContextId();
    if (traceLocation.beDebug()) {
      trace("restoreRequestContext", "currentThreadId: " + currentThreadId + ", CurrentRequestContextId: " + id  +
          ", context size [" + requestContexts.size() + "], sessionRequest [" + getRequestContext().getSessionRequest() + "]");
    }
    if (!id.equals(threadId) && id.equals(currentThreadId)) {
      //new thread was triggered - clear RequestContext
      //TODO check this
      Long newId = RequestContextObject.popRequestContextId();
      if (!newId.equals(id)) {
        RequestContext reqContext = (RequestContext)requestContexts.get(id);
        if (reqContext != null) {
          if (!reqContext.getHttpParameters().isPreserved()) {
            requestContexts.remove(id);
            reqContext.invalidate();
          }
        } else {
          WebIllegalStateException wise = new WebIllegalStateException(WebIllegalStateException.INVALID_REQUEST_CONTEXT, new Object[]{id});
          if (traceLocation.beError()) {
            LogContext.getLocation(LogContext.LOCATION_SERVLET_REQUEST).traceError("ASJ.web.000429",
			        "client [{0}] HttpServletRequest.restoreRequestContext [{1}] in application [{2}]: " +
				      "Request object used outside its context. currentThreadId: {3}"+
				      ", CurrentRequestContextId: {4}, context size [{5}], sessionRequest [{6}]"+
				      ". ERROR: {7}",
				      new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(), currentThreadId, id, requestContexts.size(), getRequestContext().getSessionRequest(), LogContext.getExceptionStackTrace(wise)}, null, null);
          }
          throw wise;
        }
      }
      RequestContextObject.setCurrentRequestContextId(newId);

      if (traceLocation.beDebug()) {
        trace("restoreRequestContext", "currentThreadId: " + currentThreadId + ", removed Id: " + id  +
            ", set new id: " + newId + ", context size [" + requestContexts.size() + "]");
      }
    }
  }


  protected void trace(String method, String msg) {
    LogContext.getLocation(LogContext.LOCATION_SERVLET_REQUEST).trace(
        "client [" + getTraceClientId() + "] HttpServletRequest." + method +
        " [" + getObjectInstance() + "] in application [" + getTraceAliasName() + "]: " +  msg, getTraceAliasName());
  }

  //TODO:POlly : Internal trace methods
  protected void traceError( String msgId, String method, String msg, Throwable t, String dcName, String csnComponent) {
    LogContext.getLocation(LogContext.LOCATION_SERVLET_REQUEST).traceError(msgId,
        "client [" + getTraceClientId() + "] HttpServletRequest." + method +
        " [" + getObjectInstance() + "] in application [" + getTraceAliasName() + "]: " + msg +
        ". ERROR: " + LogContext.getExceptionStackTrace(t), dcName, csnComponent);
  }

  protected void traceWarning(String msgId, String method, String msg, boolean logTrace, String dcName, String csnComponent) {
    if (logTrace) {
      msg = LogContext.getExceptionStackTrace(new Exception(msg));
    }
    LogContext.getLocation(LogContext.LOCATION_SERVLET_REQUEST).traceWarning(msgId,
        "client [" + getTraceClientId() + "] HttpServletRequest." + method +
        " [" + getObjectInstance() + "] in application [" + getTraceAliasName() + "]: " + msg, dcName, csnComponent);
  }

  protected void traceInfo(String method, String msg) {
    traceLocation.infoT("client [" + getTraceClientId() + "] HttpServletRequest." + method +
        " [" + getObjectInstance() + "] in application [" + getTraceAliasName() + "]: " + msg);
  }

  protected void tracePath(String method, String msg) {
    traceLocation.pathT("client [" + getTraceClientId() + "] HttpServletRequest." + method +
        " [" + getObjectInstance() + "] in application [" + getTraceAliasName() + "]: " + msg);
  }

  /**
   * Returns the client id if the requestFacade is not null, otherwise: "<NA>".
   * For tracing purposes.
   * @return
   */
  private String getTraceClientId() {
    String clientId;
    try {
      if (getRequestContext() != null && getRequestContext().getHttpParameters() != null && getRequestContext().getHttpParameters().getRequest() != null) {
        clientId = String.valueOf(getRequestContext().getHttpParameters().getRequest().getClientId());
      } else {
        clientId = "<NA>";
      }
    }catch (IllegalStateException ise) {
      return "<NA>";
    }
    return clientId;
  }

  /**
   * Returns the alias name if context is not null, otherwise: "<NA>".
   * For tracing purposes.
   * @return
   */
  private String getTraceAliasName() {
    try {
      if (getRequestContext() != null && getRequestContext().getApplicationContext() != null) {
        return getRequestContext().getApplicationContext().getAliasName();
      } else {
        return "<NA>";
      }
    } catch (IllegalStateException ise) {
      return "<NA>";
    }
  }


  public void setAutoCompleteOff(long timeout) {
	long id =  Thread.currentThread().getId();
	RequestContextObject.setAutoComplete(id, timeout);
  }

  public long getRequestContextId(){
	  return this.threadId;
  }

  public void setTimeout(){
	  this.timeout = true;
  }

}
