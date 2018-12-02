/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.runtime.client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import com.sap.engine.interfaces.security.auth.SecurityRequest;
import com.sap.engine.lib.util.ArrayObject;
import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.lib.util.Stack;
import com.sap.engine.services.httpserver.interfaces.HttpParameters;
import com.sap.engine.services.httpserver.interfaces.exceptions.ParseException;
import com.sap.engine.services.httpserver.lib.CookieParser;
import com.sap.engine.services.httpserver.lib.HttpCookie;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.httpserver.lib.ProtocolParser;
import com.sap.engine.services.httpserver.lib.protocol.HeaderNames;
import com.sap.engine.services.httpserver.lib.util.Ascii;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.servlets_jsp.lib.multipart.MultipartMessage;
import com.sap.engine.services.servlets_jsp.lib.multipart.MultipartParseException;
import com.sap.engine.services.servlets_jsp.lib.multipart.MultipartPart;
import com.sap.engine.services.servlets_jsp.lib.multipart.impl.MultipartMessageImpl;
import com.sap.engine.services.servlets_jsp.runtime_api.SapHttpServletRequest;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.application.SessionServletContext;
import com.sap.engine.services.servlets_jsp.server.application.WebEvents;
import com.sap.engine.services.servlets_jsp.server.exceptions.NewApplicationSessionException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIllegalStateException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebUnsupportedEncodingException;
import com.sap.engine.services.servlets_jsp.server.lib.Constants;
import com.sap.engine.services.servlets_jsp.server.lib.WebParseUtils;
import com.sap.engine.session.SessionException;
import com.sap.engine.session.runtime.http.HttpSessionRequest;
import com.sap.tc.logging.Location;

public abstract class HttpServletRequestImpl implements SapHttpServletRequest {
  private static final String jsp_precompile = "jsp_precompile";
  private static Location currentLocation = Location.getLocation(HttpServletRequestImpl.class);
  private static Location traceLocation = LogContext.getLocationServletRequest();
  protected static Location securityLocation = LogContext.getLocationSecurity();
  protected ServiceContext serviceContext = null;
  /**
   * Gives access to the Web container for the servlet instance.
   */
  protected ApplicationContext context = null;
  private SessionServletContext sessionServletContext = null;
  /**
   * Represents Http request
   */
  private HttpParameters httpParameters = null;
  /**
   * Represents the Http request.
   */
  protected HttpServletResponseFacade response = null;
  /**
   * extra information sent with the request. For HTTP servlets,
   * parameters are contained in the query string or posted form data.
   */
  private HashMapObjectObject parameters = new HashMapObjectObject(8);
  /**
   * Part of this request's URL from the protocol name up to the query string
   */
  private byte[] requestURI = null;
  /**
   * Part of this request's URL from the protocol name up to the query string
   */
  private byte[] requestURI_internal = null;
  /**
   * The query string that is contained in the request URL
   */
  private String queryString = null;
  /**
   * The name of the character encoding used in the body of this request.
   */
  private String characterEncoding = Constants.DEFAULT_CHAR_ENCODING;
  private boolean characterEncodingSet = false;
  /**
   * Real path for this request
   */
  private String realPath = null;
  /**
   * Used for include to store original parameters
   */
  private Stack paramStack = new Stack();
  private Stack queriesStack = new Stack();
  private Stack isParsedParamStack = new Stack();
  private boolean getInputStream = false;
  private boolean getReader = false;
  private ServletInputStream inputStream = null;
  private BufferedReader reader = null;
  //private String currentServletName = null;
  private boolean sessionAccessed = false;
  public boolean parametersParsed = false;
  private boolean queryStringParsed = false;
  private String jspPrecompile = null;
  private boolean jspFlag = false;
  private ClassLoader threadLoader = null;
  private String servletPathIncluded = null;
  private boolean cookieSet = false;
  private boolean reusedSessionCookie = false;

  // the following are used for (re)storing POST parameter data  
  protected byte[] postDataBytes = null;  
  protected boolean isPostDataBytesRestored = false;
  protected String origRequestEncoding = "";  
  //contains post params after restore; when it is set isPostDataBytesRestored is set to true
  protected HttpInputStreamImplByteArray preservedPostParamsStream=null;
  
  protected static final String URLENCODED_CONTENT_TYPE = "application/x-www-form-urlencoded";
    
  private int parametersLength = -1;
  
  private Servlet currentServlet = null;
  
  /**
   * Tracks the names of the servlets invoked in this request.
   */
  private LinkedList invokedServletNamesStack = new LinkedList();
  
  /**
   * The name of the currently invoked servlet.
   */
  private String currentlyInvokedServletName = null;
  
  /**
   * Holds programmatically forced authentication type
   */
  private String forcedAuthType = null;
  
  private Object retrieveRequestBodySyncObj = new Object();
  
  public HttpServletRequestImpl() {
    serviceContext = ServiceContext.getServiceContext();
  }

  /**
   * Create an instance and initiate it from the Http request.
   *
   * @param   httpParameters  represents the Http request
   * @param   response  represents Http response for the servlet
   */
  void init(ApplicationContext applicationContext, HttpParameters httpParameters, HttpServletResponseFacade response) {
    this.httpParameters = httpParameters;    
    
    this.response = response;
    parseEncoding();
    
    setContext(applicationContext);
    jspFlag = false;
    WebEvents events = applicationContext.getWebEvents();
    if (events != null) {
      events.requestInitialized(this);
    }
  }

  /**
   * Clear this object, ready to  return it in ObjectPool
   *
   */
  void reset() {
    if (traceLocation.beDebug()) {
      traceDebug("reset", "clearing request object");
    }
//    try {
//    if (!sessionAccessed) {
//      ApplicationSession applicationSession = (ApplicationSession) getSession(false);
//      if (applicationSession != null && applicationSession.isValid()) {
//        applicationSession.accessed();
//      }
//    }
//    } catch (OutOfMemoryError e) {
//      throw e;
//    } catch (ThreadDeath e) {
//      throw e;
//    } catch (Throwable t) {
//    	//TODO:Polly severity:warning
//      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000120", 
//        "Cannot finalize session access.", t, null, null);
//    }

//    try {
//    WebEvents events = context.getWebEvents();
//    if (events != null) {
//      events.requestDestroyed(this);
//    }    
//    } catch (OutOfMemoryError e) {
//      throw e;
//    } catch (ThreadDeath e) {
//      throw e;
//    } catch (Throwable t) {
//      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "Cannot invoke requestDestroyed listeners.", t, null);
//    }

    context = null;
    sessionServletContext = null;
    parameters.clear();
    requestURI = null;
    requestURI_internal = null;
    queryString = null;
    characterEncoding = Constants.DEFAULT_CHAR_ENCODING;
    characterEncodingSet = false;
    httpParameters = null;
    realPath = null;
    response = null;
    paramStack.clear();
    queriesStack.clear();
    isParsedParamStack.clear();
    getInputStream = false;
    getReader = false;
    inputStream = null;
    reader = null;
    sessionAccessed = false;
    parametersParsed = false;
    queryStringParsed = false;
    jspPrecompile = null;
    jspFlag = false;
    threadLoader = null;
    servletPathIncluded = null;
    cookieSet = false;
    reusedSessionCookie = false;
    currentServlet = null;
    invokedServletNamesStack.clear();
    currentlyInvokedServletName = null;
    forcedAuthType = null;
    parametersLength = -1;
    
    //fields connected with preserve post parameters
    postDataBytes = null;
    isPostDataBytesRestored = false;
    origRequestEncoding = "";
    preservedPostParamsStream=null;   
    
//    isInDoAs = false; // moved to RequestContext. No need for reset
//    ss = null;
  }

  /**
   * Returns the name of the authentication scheme used to protect the servlet
   * or null  if the servlet was not protected.
   *
   * @return     the name of the authentication scheme
   */
  public abstract String getAuthType();
//  {
//    // If there is an programmatically forced authentication type
//    if (forcedAuthType != null) { return forcedAuthType; }
//    
//    SecuritySession ss = getSecuritySession();
//    if ((ss != null) && (ss.getAuthenticationConfiguration() != null)
//        && !sessionServletContext.getAuthenticationContext().isLoginNeeded()) {
//      if (traceLocation.beDebug()) {
//        traceDebug("getAuthType", "return: [" + sessionServletContext.getAuthType() + "]");
//      }
//      return sessionServletContext.getAuthType();
//    }
//    return null;
//  }

  /**
   * Returns the name of the character encoding used in the body of this request.
   *
   * @return     the name of the character encoding
   */
  public String getCharacterEncoding() {
    if (!characterEncodingSet) {
      return null;
    }
    return characterEncoding;
  }

  /**
   * Returns the length, in bytes, of the request body and made available by the input stream,
   * or -1 if the length is not known.
   *
   * @return     the length, in bytes, of the request body
   */
  public int getContentLength() {
    int result = -1;
    
    //set content length according post params preservation
    if (isPostDataBytesRestored) {
      return postDataBytes.length;
    }
    
    byte[] len = httpParameters.getRequest().getHeaders().getHeader(HeaderNames.entity_header_content_length_);
    if (len != null) {
      try {
        result = Ascii.asciiArrToInt(len, 0, len.length);
        if (result < 0) {
          result = -1;
        }
      } catch (NumberFormatException _) {
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000179",
          "Incorrect value of the content-length header {0}.", new Object[]{len}, _, null, null);
      }
    }
    return result;
  }

  /**
   * Returns the MIME type of the body of the request, or null if the type is not known.
   *
   * @return     the MIME type of the body of the request
   */
  public String getContentType() {
    return httpParameters.getRequest().getHeaders().getHeader("Content-Type");
  }

  /**
   * Returns a Cookie from the Http request represented as Cookie object or null if
   * no cookies were found in the request.
   *
   * @return     a Cookie from the Http request
   */
  public Cookie[] getCookies() {
    ArrayObject cookies = httpParameters.getRequest().getCookies(context.getWebApplicationConfiguration().isURLSessionTracking());
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
   * Returns the value of the specified request header as a long value that represents a Date object.
   *
   * @param   s  name of a header
   * @return     the value of the specified request header
   * @exception   IllegalArgumentException
   */
  public long getDateHeader(String s) throws IllegalArgumentException {
    if (traceLocation.beDebug()) {
      traceDebug("getDateHeader", "name = [" + s + "], return: [" + httpParameters.getRequest().getHeaders().getDateHeader(s) + "]");
    }
    return httpParameters.getRequest().getHeaders().getDateHeader(s);
  }

  /**
   * Returns the value of the specified request header as a String.
   *
   * @param   s  name of a header
   * @return     the value of the specified request header
   */
  public String getHeader(String s) {
    if (traceLocation.beDebug()) {
      traceDebug("getHeader", "name = [" + s + "], return: [" + httpParameters.getRequest().getHeaders().getHeader(s) + "]");
    }
    return httpParameters.getRequest().getHeaders().getHeader(s);
  }

  /**
   * Returns an enumeration of all the header names this request contains.
   *
   * @return     all header names
   */
  public Enumeration getHeaderNames() {
    if (traceLocation.beDebug()) {
      String atr = "";
      Enumeration keys = httpParameters.getRequest().getHeaders().names();
      while (keys.hasMoreElements()) {
        atr += keys.nextElement() + ", ";
      }
      if (atr.endsWith(", ")) {
        atr = atr.substring(0, atr.length() - 2);
      }
      traceDebug("getHeaderNames", "return: [" + atr + "]");
    }
    return httpParameters.getRequest().getHeaders().names();
  }
  
  /**
   * Removes header with given name from the request if exist
   * 
   * @param name
   * The name of the header to remove
   */
  public void removeHeader(String name) {
    if (traceLocation.beDebug()) {
      traceDebug("removeHeader", "name = [" + name + "], value: [" 
        + httpParameters.getRequest().getHeaders().getHeader(name) + "]");
    }
    httpParameters.getRequest().getHeaders().removeHeader(name);
  }

  /**
   * Retrieves the body of the request as binary data using a ServletInputStream.
   *
   * @return     the body of the request as binary data
   * @exception   java.io.IOException
   */
  public ServletInputStream getInputStream() throws IOException {
  	synchronized (retrieveRequestBodySyncObj) {
	    if (getReader) {
	      if (traceLocation.beWarning()) {
	        LogContext.getLocation(LogContext.LOCATION_SERVLET_REQUEST).traceWarning("ASJ.web.000539", 
	          "client [{0}] HttpServletRequest.getInputStream" +
	          " [{1}] in application [{2}]: getReader == true, throwing error", 
	          new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName()}, getApplicationContext().getApplicationName(), getApplicationContext().getCsnComponent());	        
	      }
	      throw new WebIllegalStateException(WebIllegalStateException.Stream_is_already_taken_with_method, new String[]{"getReader()"});
	    } else {
	      getInputStream = true;
	    }    
      
	    /*
	     * The next part could be excluded from the synch block if the
	     * com.sap.engine.services.httpserver.server.RequestImpl.getBody() is made thread safe.
	     */
	    if (inputStream != null) {
	      if (traceLocation.bePath()) {
	        tracePath("getInputStream", "will return: [" + inputStream + "]");
	      }
	      return inputStream;
	    }    
	    
	    //Return input stream after restore data
      //restore post data bytes if necessary
      //else get it from the body
      if (isPostDataBytesRestored || restorePostParametersFromSession()){
        if (preservedPostParamsStream!=null){
          
          //this is done because specification SRV 3.1.1
          //getInputStream or getReader may be called to read the body, not both. 
          postDataBytes=null;
          isPostDataBytesRestored = false;
          inputStream = preservedPostParamsStream;
          preservedPostParamsStream = null;
          LogContext.getCategory(LogContext.CATEGORY_SECURITY).logWarning(securityLocation, "ASJ.web.000756",
              "Input stream with post data taken from initial request after authentication.", null, null);
        }      
      } else if (httpParameters.getRequest().getBody() != null) {	      
	      inputStream = httpParameters.getRequest().getBody();
	    } else {
	      inputStream = new EmptyServletInputStreamImpl();
	    }
    }
    if (traceLocation.bePath()) {
      tracePath("getInputStream", "will return: [" + inputStream + "]");
    }
    return inputStream;
  }

  public ServletInputStream getInputStreamNoCheck() throws IOException {
    if (inputStream != null) {
      return inputStream;
    }
    //Return input stream after restore data
    //restore post data bytes if necessary
    //else get it from the body
    if (isPostDataBytesRestored || restorePostParametersFromSession()){
      if (preservedPostParamsStream!=null){
        
        //this is done because specification SRV 3.1.1
        //getInputStream or getReader may be called to read the body, not both. 
        postDataBytes=null;
        isPostDataBytesRestored = false;
        inputStream = preservedPostParamsStream;
        preservedPostParamsStream = null;
        LogContext.getCategory(LogContext.CATEGORY_SECURITY).logWarning(securityLocation, "ASJ.web.000757",
            "Input stream with post data taken from initial request after authentication.", null, null);
      }      
    } else if (httpParameters.getRequest().getBody() != null) {       
      inputStream = httpParameters.getRequest().getBody();
    } else {
      inputStream = new EmptyServletInputStreamImpl();
    }
    return inputStream;
  }

  /**
   * Returns the value of the specified request header as an int.
   *
   * @param   s  name of a header
   * @return     the value of the specified request header as an int
   */
  public int getIntHeader(String s) {
    if (traceLocation.beDebug()) {
      traceDebug("getIntHeader", "name = [" + s + "], return: [" + httpParameters.getRequest().getHeaders().getIntHeader(s) + "]");
    }
    return httpParameters.getRequest().getHeaders().getIntHeader(s);
  }

  /**
   * Returns the name of the HTTP method with which this request was made, for example, GET, POST, or PUT.
   *
   * @return     the name of the HTTP method
   */
  public String getMethod() {
    byte[] pib = httpParameters.getRequest().getRequestLine().getMethod();
    char[] pic = new char[pib.length];
    for (int i = 0; i < pib.length; i++) {
      pic[i] = (char) (pib[i] & 0x00ff);
    }
    return new String(pic);
  }

  /**
   * Returns the value of a request parameter as a String, or null if the parameter does not exist.
   *
   * @param   s  name of the parameter
   * @return     value of the parameter
   */
  public String getParameter(String s) {
    String as[] = getParameterValues(s);
    if (as != null) {
      if (traceLocation.beDebug()) {
        traceDebug("getParameter", "name = [" + s + "], return: [" + as[0] + "]");
      }
      return as[0];
    } else {
      if (traceLocation.beDebug()) {
        traceDebug("getParameter", "name = [" + s + "], return: [null]");
      }
      return null;
    }
  }

  /**
   * Returns the value of a request parameter starting with "jsp_" as a String,
   * or null if the parameter does not exist.
   *
   * @param   s  name of the parameter
   * @return     value of the parameter
   */
  public String getParam(String s) {
  	synchronized (this) {
	    if (!parametersParsed) {
	      parseParameters();
	    }
  	}

    String as[] = (String[])parameters.get(s);

    if (as != null) {
      if (traceLocation.beDebug()) {
        traceDebug("getParam", "name = [" + s + "], return: [" + as[0] + "], parameters: [" + dumpParameters() + "]");
      }
      return as[0];
    } else {
      if (traceLocation.beDebug()) {
        traceDebug("getParam", "name = [" + s + "], return: [null]");
      }
      return null;
    }
  }

  /**
   * Returns an array of String objects containing all of the values the given request
   * parameter has, or null if the parameter does not exist.
   *
   * @param   s  name of parameter
   * @return     the all values for this parameter
   */
  public String[] getParameterValues(String s) {
    if (jspFlag && s.startsWith("jsp_")) {
      return null;
    }
    synchronized (this) {
	    if (!parametersParsed) {
        //restore post data bytes if necessary
	      restorePostParametersFromSession();	      
        
        parseParameters();
      }
    }
    String[] result = (String[])parameters.get(s);
    if (traceLocation.bePath()) {
      String atr = "";
      for (int i = 0; result != null && i < result.length; i++) {
        atr += result[i] + ", ";
      }
      if (atr.endsWith(", ")) {
        atr = atr.substring(0, atr.length() - 2);
      }
      tracePath("getParameterValues", "name = [" + s + "], return: [" + atr + "], parameters: [" + dumpParameters() + "]");
    }
    return result;
  }
  
  /**
   * 
   * @return
   */
  private boolean restorePostParametersFromSession() {
      if (securityLocation.beDebug()) {
        traceSecurityLocation("restorePostParametersFromSession()",
            "restore post parameters");
      }
      
      if (!"POST".equalsIgnoreCase(getMethod())) {
			if (securityLocation.beDebug()) {
				traceSecurityLocation("restorePostParametersFromSession()",
						"post parameters are not restored. The conditions are not fulfilled."
								+ "Method is not [post]. ");
			}
			return false;
      }
      
      if (getHttpParameters().getApplicationSession() == null) {
    	  if (securityLocation.beDebug()) {
    		  traceSecurityLocation("restorePostParametersFromSession()",
						"Post parameters are not restored. There is no application session!");
			}
    	  return false;
      }
      
      HttpSession sess = getSession(false);
      if (sess == null) {
        if (securityLocation.beDebug()) {
        	traceSecurityLocation("restorePostParametersFromSession()",
              "post parameters are not restored. Http session is [null]");

        }
        return false;
      }
      Object o = sess.getAttribute(SecurityRequest.ORIGINAL_URL);
      String storedURL = null;
      String requestedURL = null;
      if (o != null && o instanceof java.lang.String) {
        storedURL = (String) o;
        
        if (securityLocation.beDebug()) {
          traceSecurityLocation("restorePostParametersFromSession()",
              "restore post parameters. The url is " + storedURL + ".");
        }
        
        //The following code is connected with forwarded request
        //When the request is forwarded we take the URL of the original request url, which is stored in
        //session attributes: "javax.servlet.forward.request_uri" and "javax.servlet.forward.query_string".
        //The security team stores this url in forwarded case because they needed the parent url of the request
        //for SAML2 scenarios - IdP should return parent url (Portal and NWA uses this case),
        //not the url of the current request.        
        String parentUrlRelative = (String) getAttribute(FORWARD_URI_REQUEST_ATRIBUTE);        
        if (parentUrlRelative != null && parentUrlRelative.length() != 0) {
  	      if (securityLocation.beDebug()) {
  	    	  traceSecurityLocation("getOriginalUrlFromRequest", "Forward case. URL is taken from forward request attribute.");
  	      }
  	      requestedURL = parentUrlRelative; 
  	      String queryStr = (String) getAttribute(FORWARD_QUERY_STRING_REQUEST_ATRIBUTE);
  	      if (queryStr != null) {
  	        requestedURL += "?" + queryStr;
  	      }  	      
  	    }
        else {
          if (securityLocation.beDebug()) {
            traceSecurityLocation("getOriginalUrlFromRequest", "URL is taken from the request.");
          }
        	requestedURL = getRequestURI();        	
        	String queryStr = getQueryString();
        	 if (queryStr != null) {
             requestedURL += "?" + queryStr;
           }
        }
        
        // return error page if stored data are lost
        if (storedURL.equals(requestedURL)) {
          restorePostDataBytes();
          sess.removeAttribute(SecurityRequest.ORIGINAL_URL);
          if (securityLocation.beDebug()) {
        	  traceSecurityLocation("restorePostParametersFromSession()",
                "Post parameters are restored successfully.");
          }
          return true;
        }
      }

      if (securityLocation.beDebug()) {
    	  traceSecurityLocation(
            "restorePostParametersFromSession()",
            "post parameters are not restored. The conditions are not fulfilled."
                + "Stored url is not coincided with original url. [Stored url] = " + storedURL + 
                "[; Original url = ]" + requestedURL);
      }    
    return false;
  }
  
  private static final String FORWARD_URI_REQUEST_ATRIBUTE = "javax.servlet.forward.request_uri";
  private static final String FORWARD_QUERY_STRING_REQUEST_ATRIBUTE = "javax.servlet.forward.query_string";

  /**
   * Returns an Enumeration of String  objects containing the names of the parameters
   * contained in this request.
   *
   * @return     enumeration with all names of parameters
   */
  public Enumeration getParameterNames() {
  	synchronized (this) {
	    if (!parametersParsed) {
	      //restore post data bytes if necessary
        restorePostParametersFromSession();
	              
	      parseParameters();
	    }
  	}
    if (traceLocation.bePath()) {
      String atr = "";
      Enumeration en = parameters.keys();
      while (en.hasMoreElements()) {
        atr += en.nextElement() + ", ";
      }
      if (atr.endsWith(", ")) {
        atr = atr.substring(0, atr.length() - 2);
      }
      tracePath("getParameterNames", "return: [" + atr + "], parameters: [" + dumpParameters() + "]");
    }
    return parameters.keys();
  }

  String parsePathInfo(boolean pathInfoSet, String pathInfo) {
    String context_path = (getContextPath() + getServletPath()).trim();
    if (!pathInfoSet) {
      pathInfo = httpParameters.getRequestPathMappings().getPathInfo();
    }
    if (pathInfo != null) {
      pathInfo = pathInfo.trim();
      if (pathInfo.startsWith(context_path)) {
        if (context_path.equals(pathInfo)) {
          pathInfo = null;
          return pathInfo;
        } else {
          pathInfo = pathInfo.substring(context_path.length());
        }
      }
      if (!getRequestURIinternal().equalsIgnoreCase(context_path + pathInfo)) {
        int csPathIndex = getRequestURIinternal().indexOf(context_path);
        if (csPathIndex != -1) {
          pathInfo = getRequestURIinternal().substring(csPathIndex + context_path.length());
        } else {
          pathInfo = null;
        }
      }
    }
    if (pathInfo == null || pathInfo.equals("")) {
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
    return pathInfo;
  }

  /**
   * Returns any extra path information associated with the URL the client sent when it made this request.
   *
   * @return     any extra path information
   */
  public String getPathTranslated() {
    String path = getPathInfo();
    if (path == null || "".equals(path)) {
      return null;
    }
    return getRealPath(path);
  }

  /**
   * Returns the name and version of the protocol the request uses in the form
   * protocol/majorVersion.minorVersion, for example, HTTP/1.1.
   *
   * @return     the name and version of the protocol
   */
  public String getProtocol() {
    return "HTTP/" + httpParameters.getRequest().getRequestLine().getHttpMajorVersion() + "." + httpParameters.getRequest().getRequestLine().getHttpMinorVersion();
  }

  /**
   * Returns the query string that is contained in the request URL after the path.
   *
   * @return     the query string
   */
  public String getQueryString() {
  	synchronized (this) {
	    if (!queryStringParsed && !parametersParsed) {
	      if (queryString == null && httpParameters.getRequest().getRequestLine().getQuery() != null) {
	        try {
	          queryString = httpParameters.getRequest().getRequestLine().getQuery().toString(characterEncoding);
	        } catch (UnsupportedEncodingException e) {
	          LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000180",
	            "Cannot parse the parameters of the request. Incorrect encoding specified in it: {0}.", new Object[]{characterEncoding}, e, null, null);
	        }
	      }
	      queryStringParsed = true;
	    }
  	}
    if (traceLocation.bePath()) {
      tracePath("getQueryString", "queryString = [" + queryString + "]");
    }
    return queryString;
  }

//  public void setUserAuthSessionInRunAS(boolean isInDoAs, SecuritySession ss) {
//    this.isInDoAs = isInDoAs;
//    if (isInDoAs) {
//      this.ss = ss;
//    } else {
//      this.ss = null;
//    }
//  }
//
//  protected SecuritySession getSecuritySession() {
//    if (isInDoAs) { // todo - check if in Subject.doAs
//      return ss;
//    }
//    SecurityContextObject securityContext = (SecurityContextObject) context.getSecurityContext();
//    if (securityContext == null) {
//        return null;
//    }
//    return securityContext.getSession();
//  }
  
  public abstract void restorePostDataBytes();
  
  /**
   * Returns the login of the user making this request, if the user has been authenticated,
   * or null if the user has not been authenticated.
   *
   * @return     name of the user
   */
  public abstract String getRemoteUser(); 
//  {
//    SecuritySession ss = getSecuritySession();
//    if ((ss != null) && (ss.getAuthenticationConfiguration() != null)) {
//      if (traceLocation.bePath()) {
//        tracePath("getRemoteUser", "user = [" + ss.getPrincipal().getName() + "]");
//      }
//      return ss.getPrincipal().getName();
//    }
//    if (traceLocation.bePath()) {
//      tracePath("getRemoteUser", "user = [" + null + "]");
//    }
//    return null;
//  }

  /**
   * Returns the name of the scheme used to make this request.
   *
   * @return     the name of the scheme
   */
  public String getScheme() {
    if (traceLocation.bePath()) {
      tracePath("getScheme", "scheme = [" + httpParameters.getRequest().getScheme() + "]");
    }
    return httpParameters.getRequest().getScheme();
  }

  /**
   * Returns the host name of the server that received the request.
   *
   * @return     the host name of the server
   */
  public String getServerName() {
    return httpParameters.getRequest().getHost();
  }

  /**
   * Returns the port number on which this request was received.
   *
   * @return     the port number
   */
  public int getServerPort() {
    return httpParameters.getRequest().getPort();
  }

  public HttpParameters getHttpParameters() {
    return httpParameters;
  }



  /**
   * Returns the current HttpSession  associated with this request or,
   * if there is no current session and create is true, returns a new session.
   *
   * @param   flag  if false doesn't create a new session
   * @return     current session
   */
  public HttpSession getSession(boolean flag) {
    //TODO - this method should not be used - the method which is used is httpservletrequesrwrapper - make this method abstract
    //and delete all private methods which it uses
    ApplicationSession applicationSession = (ApplicationSession) httpParameters.getApplicationSession();
    HttpCookie cokkk = httpParameters.getRequest().getSessionCookie(context.getWebApplicationConfiguration().isURLSessionTracking());
    HttpSessionRequest sRequest = httpParameters.getSessionRequest();
    if (cokkk != null) {
    	sRequest.reDoSessionRequest(sessionServletContext.getSession(), cokkk.getValue(), SessionServletContext.getSessionIdFromJSession(cokkk.getValue()));
      applicationSession = (ApplicationSession) sRequest.getSession(false);
      httpParameters.setApplicationSession(applicationSession);	 
    }
    boolean beDebug = traceLocation.beDebug();
    if (beDebug) {
      traceDebug("getSession", "flag = [" + flag + "], applicationSession in request = [" + applicationSession + "]");
    }
    if (applicationSession != null) {
      if (!applicationSession.isValid()) {
        if (beDebug) {
          traceDebug("getSession", "session in request is not valid");
        }
        applicationSession = null;
        httpParameters.setApplicationSession(null);
      }
    }

    if (flag && (applicationSession == null)) { //Creates the session
      if (sessionServletContext.isForbiddenNewApplicationSession()) {
        if (traceLocation.beWarning()) {
           LogContext.getLocation(LogContext.LOCATION_SERVLET_REQUEST).traceWarning("ASJ.web.000536", 
           	 "client [{0}] HttpServletRequest.getSession" +
        	   " [{1}] in application [{2}]: isForbiddenNewApplicationSession", 
        	   new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName()}, null, null);
           
        }
        throw new NewApplicationSessionException(NewApplicationSessionException.CANNOT_CREATE_A_NEW_APPLICATION_SESSION_BECAUSE_MAX_NUMBER_OF_SESSIONS_HAS_BEEN_REACHED);
      }
      String sessionId = getSessionID(httpParameters);
      if (beDebug) {
        traceDebug("getSession", "sessionId = [" + sessionId + "], reusedSessionCookie = [" + reusedSessionCookie + "]");
      }
      try {
        /*
        * if there is no such session object the new one is created
        */
        SessionFactoryImpl factory =  new SessionFactoryImpl(this, null);
        sRequest.reDoSessionRequest(context.getSessionServletContext().getSession(), sessionId);
        applicationSession = (ApplicationSession) sRequest.getSession(factory);
        httpParameters.setApplicationSession(applicationSession);
      } catch (SessionException e) {
    	  //TODO:Polly type:ok
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000121",
            "Error in creating of http session {0}.", new Object[]{sessionId}, e, null, null);
      }

//      boolean createSession = true;
//      if (reusedSessionCookie) {
//         synchronized (sessionServletContext.sessionIdSynchObject) {
//          createSession = sessionServletContext.sessionIDlocks.get(sessionId) == null;
//          if (createSession) {
//            if (sessionServletContext.getSession().get(sessionId) != null) {
//              createSession = false;
//            } else {
//              sessionServletContext.sessionIDlocks.put(sessionId, new Object());
//            }
//          } else {
//            boolean sleep = true;
//            while (sleep) {
//              try {
//                sessionServletContext.sessionIdSynchObject.wait(Constants.WAIT_TIMEOUT);
//              } catch (Exception e) {
//                LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation,
//                    "Error in synchronizing the creation of http session " + sessionId + ".", e, context.getAliasName());
//              }
//              sleep = sessionServletContext.sessionIDlocks.contains(sessionId);
//            }
//          }
//        }
//      }
//      try {
//        if (LogContext.isTracing()) {
//          traceDebug("getSession", "createSession = [" + createSession + "]");
//        }
//        if (createSession) {
//          applicationSession = new ApplicationSession(sessionId, sessionServletContext.getSessionTimeout(),
//              httpParameters.getRequest().getClientIP(), context.getAliasName());
//          sessionServletContext.getSession().put(sessionId, applicationSession);
//          httpParameters.setDebugRequest(context.initializeDebugInfo(httpParameters, applicationSession));
//        } else {
//          applicationSession = (ApplicationSession)sessionServletContext.getSession().get(sessionId);
//        }
//        sessionAccessed = true;
//        httpParameters.setApplicationSession(applicationSession);
//      } finally {
//        if (reusedSessionCookie && createSession) {
//          synchronized (sessionServletContext.sessionIdSynchObject) {
//            try {
//              sessionServletContext.sessionIDlocks.remove(sessionId);
//              sessionServletContext.sessionIdSynchObject.notifyAll();
//            } catch (Exception e) {
//              LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation,
//                  "Error in synchronizing the creation of http session " + sessionId + ".", e, context.getAliasName());
//            }
//          }
//        }
//      }
    } else if (applicationSession != null) {
      if (!cookieSet && !context.getWebApplicationConfiguration().isURLSessionTracking()) {
        HttpCookie sCookie = httpParameters.getRequest().getSessionCookie(context.getWebApplicationConfiguration().isURLSessionTracking());
        if (sCookie != null && sCookie.getName().equals(CookieParser.jsessionid_url) && response.isModifiable()) {        
          response.addCookie(CookieParser.createSessionCookie(applicationSession.getIdInternal(),
            httpParameters.getRequest().getHost(),
            context.getWebApplicationConfiguration().getSessionCookieConfig()));
          response.setSessionCookie();
        }
      }
      if (!sessionAccessed) {
        applicationSession.accessed();
        sessionAccessed = true;
      }
    }
    if (!cookieSet) {
      sessionServletContext.addApplicationCookie(httpParameters);
      cookieSet = true;
    }
    if (traceLocation.bePath()) {
      tracePath("getSession", "return: [" + applicationSession + "]");
    }
    return applicationSession;
  }

  public abstract ApplicationContext getApplicationContext();
  
  /**
   * Creates an unique session ID.
   *
   * @return     unique session id
   */
  private String getSessionID(HttpParameters httpParameters) {
    HttpCookie sCookie = httpParameters.getRequest().getSessionCookie(context.getWebApplicationConfiguration().isURLSessionTracking());
    boolean beDebug = traceLocation.beDebug();
    if (sCookie != null) {
      if (beDebug) {
        traceDebug("getSessionID", "request cookie: name = [" + sCookie.getName() + "], value = [" + sCookie.getValue() + "]");
      }
      reusedSessionCookie = true;
      if (sCookie.getName().charAt(0) == 'j' && !context.getWebApplicationConfiguration().isURLSessionTracking()) {
        response.addCookie(CookieParser.createSessionCookie(sCookie.getValue(),
          httpParameters.getRequest().getHost(),  
          context.getWebApplicationConfiguration().getSessionCookieConfig()));
        response.setSessionCookie();
      }
      if (beDebug) {
        traceDebug("getSessionID", "return: [" + sCookie.getValue() + "]");
      }
      return sCookie.getValue();
    }
    if (beDebug) {
      traceDebug("getSessionID", "response.isSessionCookieSet() = [" + response.isSessionCookieSet() + "]");
    }
    if (response.isSessionCookieSet()) {
      Object[] respCookies = response.getCookies().toArray();
      for (int i = 0; i < respCookies.length; i++) {
        HttpCookie httpCookie = (HttpCookie)respCookies[i];
        if (httpCookie.getName().equals(CookieParser.jsessionid_cookie)) {
          if (beDebug) {
            traceDebug("getSessionID", "return: [" + httpCookie.getValue() + "]");
          }
          return httpCookie.getValue();
        }
      }
    }
    String sessionId = generateSessionId();//sessionServletContext.generateSessionID(httpParameters);
    if (!context.getWebApplicationConfiguration().isURLSessionTracking()) {
      if (!httpParameters.isSetSessionCookie()) {
        response.addCookie(CookieParser.createSessionCookie(sessionId,
          httpParameters.getRequest().getHost(), 
          context.getWebApplicationConfiguration().getSessionCookieConfig()));
        response.setSessionCookie();
        httpParameters.setSessionCookie(true);
      }
    }
    if (beDebug) {
      traceDebug("getSessionID", "new cookie generated, return: [" + sessionId + "]");
    }
    return sessionId;
  }

  private synchronized String generateSessionId() {
  	HttpSessionRequest sRequest = httpParameters.getSessionRequest();
    if (!sRequest.isRequested()) {    
      //String sessionId = sessionServletContext.generateSessionID(httpParameters) + sessionServletContext.generatePrivatePartJSessionId();
      String sessionId = sessionServletContext.generateJSessionIdValue();
      return sessionId;
//      try {
//        sRequest.requestNewSession(sessionServletContext.getSession(), sessionId);
//      } catch (SessionExistException e) {
//        // TODO Auto-generated catch block        
//      }
    }
    return sRequest.getClientCookie();
  }

  /**
   * Retrieves the body of the request as character data using a BufferedReader.
   *
   * @return     the body of the request
   * @exception   java.io.IOException
   */
  public BufferedReader getReader() throws IOException {
  	synchronized (retrieveRequestBodySyncObj) {
	    if (getInputStream) {
	      if (traceLocation.beWarning()) {
	        LogContext.getLocation(LogContext.LOCATION_SERVLET_REQUEST).traceWarning("ASJ.web.000537", 
	          "client [{0}] HttpServletRequest.getReader" +
	          " [{1}] in application [{2}]: getInputStream == true, throwing exception", 
	          new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName()}, getApplicationContext().getApplicationName(), getApplicationContext().getCsnComponent());
	        
	      }
	      throw new WebIllegalStateException(WebIllegalStateException.Stream_is_already_taken_with_method, new Object[]{"getInputStream()"});
	    } else {
	      getReader = true;
	    }      
	    
	    /*
	     * The next part could be excluded from the synch block if the
	     * com.sap.engine.services.httpserver.server.RequestImpl.getBody() is made thread safe.
	     */
	    boolean bePath = traceLocation.bePath();
	    if (reader != null) {
	      if (bePath) {
	        tracePath("getReader", "will return: [" + reader + "]");
	      }
	      return reader;
	    }
	    
	    //get reader if post parameter preservation      
      if (isPostDataBytesRestored || restorePostParametersFromSession()) {      
        //this is done because specification
        //getInputStream or getReader may be called to read the body, not both.         
        preservedPostParamsStream=null;
        isPostDataBytesRestored=false;
        reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(postDataBytes), characterEncoding));
        postDataBytes = null;       
        LogContext.getCategory(LogContext.CATEGORY_SECURITY).logWarning(securityLocation, "ASJ.web.000758",
            "Reader with post data taken from initial request after authentication.", null, null);
        return reader;
      } else if (httpParameters.getRequest().getBody() != null) {
	      reader = new BufferedReader(new InputStreamReader(httpParameters.getRequest().getBody(), characterEncoding));
	      if (bePath) {
	        tracePath("getReader", "will return: [" + reader + "]");
	      }
	      return reader;
	    } else {
	      reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(new byte[0]), characterEncoding));
	      if (bePath) {
	        tracePath("getReader", "will return: [" + reader + "]");
	      }
	      return reader;
	    }
  	}
  }

  /**
   * Returns the Internet Protocol (IP) address of the client that sent the request.
   *
   * @return     the Internet Protocol (IP) address
   */
  public String getRemoteAddr() {
    byte[] b = ParseUtils.inetAddressByteToString(httpParameters.getRequest().getClientIP());
    char[] ch = new char[b.length];
    for (int i = 0; i < b.length; i++) {
      ch[i] = (char) (b[i] & 0x00ff);
    }
    return new String(ch);
  }

  /**
   * Returns the fully qualified name of the client that sent the request,
   * or the IP address of the client if the name cannot be determined.
   *
   * @return     the fully qualified name of the client
   */
  public String getRemoteHost() {
    if (serviceContext.getWebContainerProperties().resolveHostName()) {
      try {
        return InetAddress.getByName(getRemoteAddr()).getHostName();
      } catch (Exception ex) {
        return getRemoteAddr();
      }
    } else{
      return getRemoteAddr();
    }
  }

  /**
   * Returns the Internet Protocol (IP) source port of the client or last proxy
   * that sent the request.
   * 
   * @return an integer specifying the port number
   * 
   * @see javax.servlet.ServletRequest#getRemotePort()
   */
  public int getRemotePort() {
    return httpParameters.getRequest().getRemotePort();
  }

  /**
   * Returns the host name of the Internet Protocol (IP) interface on which
   * the request was received.
   * 
   * @return a String containing the host name of the IP on which the request
   * was received.
   * 
   * @see javax.servlet.ServletRequest#getLocalName()
   */
  public String getLocalName() {
    return httpParameters.getRequest().getHost();
  }

  /**
   * Returns the Internet Protocol (IP) address of the interface on which the 
   * request was received.
   * @return a String containing the IP address on which the request was received.
   * 
   * @see javax.servlet.ServletRequest#getLocalAddr()
   */
  public String getLocalAddr() {
    String host = httpParameters.getRequest().getHost();
    InetAddress inetAddress = null;
    String localAddress = null;
    try {
      inetAddress = InetAddress.getByName(host);
      localAddress = inetAddress.getHostAddress();
    } catch (UnknownHostException e) {
      if (traceLocation.beError()) {
        LogContext.getLocation(LogContext.LOCATION_SERVLET_REQUEST).traceError("ASJ.web.000430", 
            "client [{0}] HttpServletRequest.getLocalAddr" +
            " [{1}] in application [{2}]: cannot parse the IP address for host = [{3}]" +
            ". ERROR: {4}", new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(), host, LogContext.getExceptionStackTrace(e)}, null, null);
      }
    }
    return localAddress;
  }

  /**
   * Returns the Internet Protocol (IP) port number of the interface on which
   * the request was received.
   * @return an integer specifying the port number.
   *  
   * @see javax.servlet.ServletRequest#getLocalPort()
   */
  public int getLocalPort() {
    return httpParameters.getRequest().getPort();
  }

  /**
   * Returns the session ID specified by the client.
   *
   * @return     session ID of the client
   */
  public String getRequestedSessionId() {
    HttpCookie cookie = httpParameters.getRequest().getSessionCookie(context.getWebApplicationConfiguration().isURLSessionTracking());
    if (cookie == null) {
      return null;
    }
    return cookie.getValue();
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
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000123",
            "Error in parsing request URI.", e, null, null); 
      }
    }
    char[] ch = new char[requestURI.length];
    for (int i = 0; i < ch.length; i++) {
      ch[i] = (char) (requestURI[i] & 0x00ff);
    }
    return new String(ch);
  }

  protected abstract String getRequestURIinternal();

  void setRequestURI(String newURI) {
    this.requestURI = newURI.getBytes();
    this.requestURI_internal = newURI.getBytes();
  }

  /**
   * Returns the part of this request's URL(DECODED) from the protocol name up to the query string
   * in the first line of the HTTP request.
   *
   * @return     the part of this request's URL from the protocol name up to the query string
   */
  String parseRequestURIinternal() {
    if (requestURI_internal == null) {
      requestURI_internal = httpParameters.getRequest().getRequestLine().getUrlDecoded().getBytes();
    }
    char[] ch = new char[requestURI_internal.length];
    for (int i = 0; i < ch.length; i++) {
      ch[i] = (char) (requestURI_internal[i] & 0x00ff);
    }
    return new String(ch);
  }

  /**
   * Returns the part of this request's URL that calls the servlet or empty string if
   * servlet is mapped with /* .
   *
   * @return     the part of this request's URL that calls the servlet
   */
  public String getServletPath() {
    if (servletPathIncluded != null) {
      return servletPathIncluded;
    }
    if (traceLocation.bePath()) {
      tracePath("getServletPath", "return: [" + httpParameters.getRequestPathMappings().getServletPath() + "]");
    }
    return httpParameters.getRequestPathMappings().getServletPath();
  }

  public void setServletPath(String servletPath) {
    servletPathIncluded = servletPath;
  }

  /**
   * Returns a String containing the real path for a given virtual path.
   *
   * @param   path  virtual path
   * @return     the real path for a given virtual path
   * @deprecated
   */
  public String getRealPath(String path) {
    if (realPath == null) {
      realPath = new String(httpParameters.getRequestPathMappings().getRealPath());
    }

    boolean bePath = traceLocation.bePath();
    if (!path.startsWith("/")) {
      String tempPath = (String) getAttribute("javax.servlet.include.realpath_path");

      if (tempPath != null) {
        if (bePath) {
          tracePath("getRealPath", "path = [" + path + "], return: [" +
              (tempPath + path).replace('/', File.separatorChar) + "]");
        }
        return (tempPath + path).replace('/', File.separatorChar);
      }
      if (bePath) {
        tracePath("getRealPath", "path = [" + path + "], return: [" +
            (realPath + path).replace('/', File.separatorChar) + "]");
      }
      return (realPath + path).replace('/', File.separatorChar);
    } else {
      String temp = context.getServletContext().getRealPath(path);

      if (temp != null) {
        if (bePath) {
          tracePath("getRealPath", "path = [" + path + "], return: [" + temp.replace('/', File.separatorChar) + "]");
        }        return temp.replace('/', File.separatorChar);
      } else {
        if (bePath) {
          tracePath("getRealPath", "path = [" + path + "], return: [null]");
        }
        return null;
      }
    }
  }

  public String getRealPathLocal(String path) {
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
      String tempPath = (String) getAttribute("javax.servlet.include.realpath_path");

      if (tempPath != null) {
        return ParseUtils.canonicalize((tempPath + path).replace('/', ParseUtils.separatorChar).replace('\\', ParseUtils.separatorChar));
      }
      return ParseUtils.canonicalize((realPath + path).replace('/', ParseUtils.separatorChar).replace('\\', ParseUtils.separatorChar));
    } else {
      return ParseUtils.canonicalize(context.getRealPathLocal(
          path,
          httpParameters.getRequestPathMappings().getAliasName().toString(),
          httpParameters.getRequestPathMappings().getAliasValue().toString()));
    }
  }

  /**
   * Checks whether the requested session ID is still valid.
   *
   * @return     whether the requested session ID is still valid
   */
  public boolean isRequestedSessionIdValid() {
    boolean result = getSession(false) != null;
    if (traceLocation.bePath()) {
      tracePath("isRequestedSessionIdValid", "return: [" + result + "]");
    }
    return result;
  }

  /**
   * Checks whether the requested session ID came in as a cookie.
   *
   * @return     whether the requested session ID came in as a cookie
   */
  public boolean isRequestedSessionIdFromCookie() {
    HttpCookie sCookie = httpParameters.getRequest().getSessionCookie(context.getWebApplicationConfiguration().isURLSessionTracking());
    if (traceLocation.bePath()) {
      tracePath("isRequestedSessionIdFromCookie", "return: [" + (sCookie != null && CookieParser.jsessionid_cookie.equals(sCookie.getName())) + "]");
    }
    return sCookie != null && CookieParser.jsessionid_cookie.equals(sCookie.getName());
  }

  /**
   * Checks whether the requested session ID came in as part of the request URL.
   *
   * @return     whether the requested session ID came in as part of the request URL
   */
  public boolean isRequestedSessionIdFromURL() {
    HttpCookie sCookie = httpParameters.getRequest().getSessionCookie(context.getWebApplicationConfiguration().isURLSessionTracking());
    if (traceLocation.bePath()) {
      tracePath("isRequestedSessionIdFromURL", "return: [" + (sCookie != null && CookieParser.jsessionid_url.equals(sCookie.getName())) + "]");
    }
    return sCookie != null && CookieParser.jsessionid_url.equals(sCookie.getName());
  }

  /**
   * Sets reference to ServletContextFacade that gives the servlet access to the Web Container
   *
   * @param   context  reference to ServletContextFacade
   */
  public void setContext(ApplicationContext context) {
    this.context = context;
    this.sessionServletContext = context.getSessionServletContext();
    contextReplaced();
    if (traceLocation.bePath()) {
      //tracePath("setContext", "new context name: [" + context.getAliasName() + "]");
    }
  }

  protected abstract void contextReplaced();

  Vector getLocalesVector() {
    return ProtocolParser.getLocalesVector(httpParameters.getRequest().getHeaders().getHeader(HeaderNames.request_header_accpet_lenguage_));
  }

  /**
   * Returns a boolean indicating whether this request was made using a secure channel, such as HTTPS.
   *                          q
   * @return     whether this request was made using a secure channel
   */
  public boolean isSecure() {
    return httpParameters.getRequest().getRequestLine().isSecure();
  }

  /**
   * Returns a RequestDispatcher object that acts as a wrapper for the resource located at the given path.
   *
   * @param   path  path of some file
   * @return     a RequestDispatcher object that acts as a wrapper for the resource located at the given path
   */
  public RequestDispatcher getRequestDispatcher(String path) {
    boolean bePath = traceLocation.bePath();
    if (path == null) {
      if (bePath) {
        tracePath("getRequestDispatcher", "path = [" + path + "], return: [null]");
      }
      return null;
    }

    if (path.startsWith("/")) {
      RequestDispatcher requestDispatcher = context.getServletContext().getRequestDispatcher(path);
      if (bePath) {
        tracePath("getRequestDispatcher", "path = [" + path + "], return: [" + requestDispatcher + "]");
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

      RequestDispatcher requestDispatcher = context.getServletContext().getRequestDispatcher(req);
      if (bePath) {
        tracePath("getRequestDispatcher", "path = [" + path + "], return: [" + requestDispatcher + "]");
      }
      return requestDispatcher;
    }
  }

  /**
   * Returns all the values of the specified request header as an Enumeration of String objects.
   *
   * @param   name  request header
   * @return     all the values of the specified request header
   */
  public Enumeration getHeaders(String name) {
    String[] str = httpParameters.getRequest().getHeaders().getHeaders(name);
    if (str == null || str.length == 0) {
      return new Vector().elements();
    }
    Vector v = new Vector();
    for (int i = 0; i < str.length; i++) {
      v.addElement(str[i]);
    }
    if (traceLocation.bePath()) {
      String atr = "";
      Enumeration en = v.elements();
      while (en.hasMoreElements()) {
        atr += en.nextElement() + ", ";
      }
      if (atr.endsWith(", ")) {
        atr = atr.substring(0, atr.length() - 2);
      }
      tracePath("getHeaders", "name = [" + name + "], return: [" + atr + "]");
    }
    return v.elements();
  }

  /**
   * Returns the portion of the request URI that indicates the context of the request.
   *
   * @return     the portion of the request URI that indicates the context
   */
  public String getContextPath() {
    String res = null;
    if (context.isDefault()) {
      res = "";
    } else {
      res = "/".concat(context.getAliasName());
    }
    if (httpParameters.getRequestPathMappings().getZoneName() != null && !httpParameters.getRequestPathMappings().isZoneExactAlias()) {
      res = res + ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getZoneSeparator() + httpParameters.getRequestPathMappings().getZoneName();
    }
    return res;
  }

  /**
   * Returns a boolean indicating whether the authenticated user is included in the specified logical "role".
   *
   * @param   role  name of a role
   * @return     if the user is authenticated for this role
   */
  public abstract boolean isUserInRole(String role); 
//  {
//    String prevContextID = context.setPolicyContextID(true);
//    try {
//      //should be empty String instead of null 
//      WebRoleRefPermission webRoleRefPermission = new WebRoleRefPermission(getCurrentlyInvokedServletName() == null ? "":getCurrentlyInvokedServletName(), role);
//      //AccessController.checkPermission(webRoleRefPermission); todo - use it after fixed in UME
//      SecuritySession securitySession = getSecuritySession();
//      Principal principal = null;
//      if (securitySession != null) {
//        principal = securitySession.getPrincipal();
//      }
//      boolean result = Policy.getPolicy().implies(new ProtectionDomain(null, null, null,
//              new Principal[]{principal}),
//              webRoleRefPermission);
//      if (!result) {
//        if (traceLocation.bePath()) {
//          tracePath("isUserInRole", "role = [" + role + "], return: [" + false + "]");
//        }
//        return false;
//      }
//      if (traceLocation.bePath()) {
//        tracePath("isUserInRole", "role = [" + role + "], return: [" + true + "]");
//      }
//      return true;
//    } catch (AccessControlException e) {
//      if (traceLocation.bePath()) {
//        tracePath("isUserInRole", "role = [" + role + "], return: [" + false + "]");
//      }
//      return false;
//    } finally {
//      context.restorePrevPolicyContextID(prevContextID);
//    }
//  }

  /**
   * Returns a java.security.Principal object containing the name of the current authenticated user.
   *
   * @return     the name of the current authenticated user
   */
  public abstract Principal getUserPrincipal(); 
//  {
//    SecuritySession ss = getSecuritySession();
//    if ((ss != null) && (ss.getAuthenticationConfiguration() != null)) {
//      if (traceLocation.bePath()) {
//        tracePath("getUserPrincipal", "return: [" + ss.getPrincipal() + "]");
//      }
//      return ss.getPrincipal();
//    }
//    if (traceLocation.bePath()) {
//      tracePath("getUserPrincipal", "return: [" + null + "]");
//    }
//    return null;
//  }

  /**
   * Used for include/forward to store original parameters of the request.
   *
   * @param   query  parameters of the request
   */
  public void setParam(String query, boolean include) {
    if (!include) {
      queryString = query;
    }
    queriesStack.push(query);
    paramStack.push(parameters);
    isParsedParamStack.push(new Boolean(parametersParsed));
    parameters = (HashMapObjectObject)parameters.clone();
    try {
      WebParseUtils.parseQueryString(parameters, query.getBytes(), characterEncoding);
    } catch (java.io.UnsupportedEncodingException e) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000181",
          "Unsupported character encoding found. Cannot parse the request parameters.", e, null, null);
    }
  }

  /**
   * Used after include/forward to restore original parameters of the request.
   * Removes only the parameters that come from include/forward and not from the parsed query string.
   */
  public void removeParam() {
    queriesStack.pop();
    Boolean lastParsed = (Boolean) isParsedParamStack.pop();
    parameters = (HashMapObjectObject)paramStack.pop();   
    if (traceLocation.bePath()) {
      trace("removeParam", "parameters: [" + dumpParameters() + "]");
    }
    if (parameters == null) {
      parameters = new HashMapObjectObject();
      parametersParsed = false;
      queryStringParsed = false;
      queryString = null;
    } else {
      //added fix of the removing the original parameters after include/forward have finished
      parametersParsed = lastParsed.booleanValue();
    }
  }

  public void setThreadClassLoader(ClassLoader threadLoader) {
    this.threadLoader = threadLoader;
  }

  public ClassLoader getThreadClassLoader() {
    return threadLoader;
  }

  public void setCurrentServletName(String servletName) {
    //this.currentServletName = servletName;
    checkMappedSlashStar();
  }

  /**
   * Overrides the name of the character encoding used in the body of this
   * request. This method must be called prior to reading request parameters or
   * reading input using <code>getReader()</code>.
   * 
   * @param enc
   * String containing the name of the character encoding
   * 
   * @exception java.io.UnsupportedEncodingException
   * if this is not a valid encoding
   * 
   * @since 2.3
   */
  public void setCharacterEncoding(String enc)
      throws UnsupportedEncodingException {
    if (traceLocation.bePath()) {
      tracePath("setCharacterEncoding", "enc = [" + enc + "]");
    }
    if (enc == null || enc.equals("")) {
      throw new WebUnsupportedEncodingException(
        WebUnsupportedEncodingException.Trying_to_set_empty_encoding);
    }
    // Tests whether encoding is valid
    byte buffer[] = {(byte) 'a'};
    new String(buffer, enc);
    // SRV.3.9 Request data encoding - It must be called prior to parsing any
    // post data or reading any input from the request. Calling this method
    // once data has been read will not affect the encoding.
    if (isPostDataParsed() || getReader || getInputStream) {
      if (traceLocation.beWarning()) {
        String message = 
          "Suggested character encoding is ignored. Request body has been read." 
          + " suggested character encoding = [" + enc 
          + "], available character encoding = [" + characterEncoding 
          + "], isPostDataParsed() = [" + isPostDataParsed() 
          + "], getReader = [" + getReader 
          + "], getInputStream = [" + getInputStream + "]";
        // Severity info if suggested charset is equal with already set one
        // See CSN: 00003573782 2006 for more details
        if (enc.equals(characterEncoding)) {
          traceInfo("setCharacterEncoding", message);
        } else {
          message += " New encoding:" + enc;
          LogContext.getLocation(LogContext.LOCATION_SERVLET_REQUEST).traceWarning("ASJ.web.000538", 
          	   "client [{0}] HttpServletRequest.setCharacterEncoding" +
        	   " [{1}] in application [{2}]: {3}", new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(), message}, getApplicationContext().getApplicationName(), getApplicationContext().getCsnComponent());          
        }
      }
      return;
    }
    characterEncoding = enc;
    characterEncodingSet = true;
    parametersParsed = false;
    queryStringParsed = false;
    queryString = null;
    paramStack.clear();
    isParsedParamStack.clear();
  }

  /**
   * Reconstructs the URL the client used to make the request.
   * The returned URL contains a protocol, server name, port number,
   * and server path, but it does not include query string parameters.
   *
   * @return  a StringBuffer object containing the reconstructed URL
   */
  String parseRequestURL() {
    return getScheme() + "://" + getServerName() + ":" + getServerPort() + getRequestURIinternal();
  }

  /**
   * Returns a java.util.Map of the parameters of this request.
   * Request parameters are extra information sent with the request.
   *
   * @return Hashtable,  containing parameter names as keys and
   *         parameter values as map values.
   *         The keys in the parameter map are of type String.
   *         The values in the parameter map are of type String array.
   */
  public Map getParameterMap() {
  	synchronized (this) {
	    if (!parametersParsed) {	   
        restorePostParametersFromSession();
	              
	      parseParameters();
	    }
  	}
    Map hash = new HashMap();
    Enumeration en = parameters.keys();
    while (en.hasMoreElements()) {
      Object key = en.nextElement();
      hash.put(key, parameters.get(key));
    }
    if (traceLocation.bePath()) {
      String atr = "";
      Enumeration params = parameters.keys();
      while (params.hasMoreElements()) {
        atr += params.nextElement() + ", ";
      }
      if (atr.endsWith(", ")) {
        atr = atr.substring(0, atr.length() - 2);
      }
      tracePath("getParameterMap", "return: [" + atr + "], parameters: [" + dumpParameters() + "]");
    }
    return hash;
  }

  public String getJspPrecompile() {
    if (jspPrecompile == null && !parametersParsed) {
      if (getQueryString() != null) {
        try {
          WebParseUtils.parseQueryString(parameters, getQueryString().getBytes(), characterEncoding);
        } catch (java.io.UnsupportedEncodingException e) {
          LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000183",
              "Unsupported character encoding found. Cannot parse the request parameters.", e, null, null);
        }
      }
      String[] ar = (String[])parameters.get(jsp_precompile);
      if (ar != null) {
        jspPrecompile = ar[0];
      }
      parameters.clear();
      if (traceLocation.bePath()) {
        tracePath("getJspPrecompile", "parameters.clear");
      }
    }
    return jspPrecompile;
  }

  public void setJspFlag(boolean isJsp) {
    jspFlag = isJsp;
  }

  private void checkMappedSlashStar() {
    if (context.getWebMappings().getServletMappedSlashStar().equals(getCurrentlyInvokedServletName())) {
      servletPathIncluded = "";
    }
  }

  private void parseEncoding() {
    characterEncoding = WebParseUtils.parseEncoding(httpParameters);
    if (characterEncoding == null || characterEncoding.equals("")) {
      characterEncoding = Constants.DEFAULT_CHAR_ENCODING;
    } else {
      characterEncodingSet = true;
    }
  }

  private void parseParameters() {
    parametersParsed = true;
    queryStringParsed = true;
    try {
      if (httpParameters.getRequest().getBody() != null) {
        try {
          parametersLength = httpParameters.getRequest().getBody().available();
        } catch (IOException ioe) {
            LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000184",
            "parseParameters(), cannot process input stream.", ioe, null, null);
          }
        }
      if (queryString == null && httpParameters.getRequest().getRequestLine().getQuery() != null) {
        queryString = httpParameters.getRequest().getRequestLine().getQuery().toString(characterEncoding);
      }
      MessageBytes totalQuery = null;
      if (!queriesStack.isEmpty()) {
        totalQuery = new MessageBytes();
        Object[] queries = queriesStack.toArray();
        //for (int i = queries.length - 1; i >= 0; i--) {
        for (int i = 0; i < queries.length; i++) {
          if (totalQuery.length() == 0) {
            totalQuery.appendAfter(((String)queries[i]).getBytes());
          } else {
            totalQuery.appendAfter(("&" + (String)queries[i]).getBytes());
          }
        }
      }
      if (httpParameters.getRequest().getRequestLine().getQuery() != null) {
        if (totalQuery == null || totalQuery.length() == 0) {
          totalQuery = httpParameters.getRequest().getRequestLine().getQuery();
        } else {
          totalQuery.appendAfter("&".getBytes());
          totalQuery.appendAfter(httpParameters.getRequest().getRequestLine().getQuery().getBytes());
        }
      }
      parameters.clear();
      if (traceLocation.bePath()) {
        tracePath("parseParameters", "parameters.clear");
      }
      if (totalQuery == null) {
        WebParseUtils.parseParameters(parameters, httpParameters, null, characterEncoding, !isPostDataBytesRestored);
      } else {
        WebParseUtils.parseParameters(parameters, httpParameters, totalQuery.getBytes(), characterEncoding, !isPostDataBytesRestored);
      }
      if (traceLocation.bePath()) {
        tracePath("parseParameters", "parameters: [" + dumpParameters() + "]");
      }
      if (serviceContext.getWebContainerProperties().getMultipartBodyParameterName() != null) {
        MultipartMessageImpl multipartMessage = (MultipartMessageImpl)getAttribute(serviceContext.getWebContainerProperties().getMultipartBodyParameterName());
        if (multipartMessage != null && multipartMessage.isFormParametersToRequest()) {
          String content_type = getContentType();
          int scInd = content_type.indexOf(';');
          if (scInd > -1) {
            content_type = content_type.substring(0, scInd);
          }
          scInd = content_type.indexOf('/');
          if (scInd > -1 && scInd < content_type.length() - 1) {
            content_type = content_type.substring(scInd + 1);
          }
          if (MultipartMessage.FORM_DATA_TYPE.equalsIgnoreCase(content_type)) {
            try {
              Enumeration en = multipartMessage.getBodyParts();
              while (en.hasMoreElements()) {
                MultipartPart nextPart = (MultipartPart)en.nextElement();
                if (nextPart.isFormParameter()) {
                  String[] ar = (String[])parameters.get(nextPart.getName());
                  if (ar == null) {
                    parameters.put(nextPart.getName(), new String[]{new String(nextPart.getBody(), characterEncoding)});
                  } else {
                    String[] arTmp = new String[ar.length + 1];
                    System.arraycopy(ar, 0, arTmp, 0, ar.length);
                    arTmp[ar.length] = new String(nextPart.getBody(), characterEncoding);
                    parameters.put(nextPart.getName(), arTmp);
                  }
                }
              }
            } catch (IOException e) {
              LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000185",
                  "Cannot parse form based parameters from multipart request.",
                  e, null, null);
            } catch (MultipartParseException e) {
              LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000186",
                  "Cannot parse form based parameters from multipart request.",
                  e, null, null);
            }
          }
        }
      }      
      
      // add the parameters retrieved from the Session
      if (this.isPostDataBytesRestored) {        
        WebParseUtils.parseParameters(parameters, httpParameters, this.postDataBytes, characterEncoding, false);
        //parameters are parsed so the input stream for restore params is set to null (SRV 3.1.1)
        preservedPostParamsStream=null;
        this.isPostDataBytesRestored=false;
        
        LogContext.getCategory(LogContext.CATEGORY_SECURITY).logWarning(securityLocation, "ASJ.web.000759",
            "Post parameters are taken and parsed after authentication", null, null);
    }
      
      String[] ar = (String[])parameters.get(jsp_precompile);
      if (ar != null) {
        jspPrecompile = ar[0];
      }
    } catch (UnsupportedEncodingException e) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000187",
          "Cannot parse the parameters of the request. Incorrect encoding [{0}] specified in it.", new Object[]{characterEncoding},
          e, getApplicationContext().getApplicationName(), getApplicationContext().getCsnComponent());
    }
  }

  private String dumpParameters() {
    StringBuilder res = new StringBuilder();
    if (parameters == null) {
      return "";
    }
    Object[] keys = parameters.getAllKeys();
    for (int i = 0; keys != null && i < keys.length; i++) {
      res.append("[").append(keys[i]).append(" = ");
      String[] values = (String[])parameters.get(keys[i]);
      for (int j = 0; j < values.length; j++) {
        res.append(values[j]);
        if ((j + 1) < values.length) {
          res.append(", ");
        }
      } 
      if ((i + 1) < keys.length) {
        res.append("], ");
      } else {
        res.append("]");
      }
    }
    return res.toString();
  }

  private boolean isPostDataParsed() {
    boolean result = false;
    ServletInputStream servletInputStream = httpParameters.getRequest().getBody();
    try {
      if (servletInputStream != null && parametersLength > 0) {
        if (parametersLength > servletInputStream.available()) {
          result = true;
        }
      } 
    } catch (IOException e) {
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000188",
          "Cannot parse the request parameters read from request stream. isPostDataParsed == true", e, null, null);
      result = true;
    }

    return result;
  }

  protected void trace(String method, String msg) {
    LogContext.getLocation(LogContext.LOCATION_SERVLET_REQUEST).trace(
        "client [" + getTraceClientId() + "] HttpServletRequest." + method +
        " [" + getObjectInstance() + "] in application [" + getTraceAliasName() + "]: " + msg, getTraceAliasName());
  }
  
  protected void traceSecurityLocation(String method, String msg) {
	  LogContext.getLocation(LogContext.LOCATION_SECURITY).trace(
	        "client [" + getTraceClientId() + "] HttpServletRequest." + method +
	        " [" + getObjectInstance() + "] in application [" + getTraceAliasName() + "]: " + msg, getTraceAliasName());
	  }

  //TODO:POlly : Internal trace methods
  protected void traceError(String msgId, String method, String msg, Throwable t, String dcName, String csnComponent) {
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

  protected void traceDebug(String method, String msg) {
    trace(method, msg);
  }

  protected String getObjectInstance() {
    String instance = super.toString();
    return instance.substring(instance.indexOf('@') + 1);
  }

  /**
   * Returns the client id if the requestFacade is not null, otherwise: "<NA>".
   * For tracing purposes.
   * @return
   */
  private String getTraceClientId() {
    String clientId;
    if (httpParameters != null && httpParameters.getRequest() != null) {
      clientId = "" + httpParameters.getRequest().getClientId();      
    } else {
      clientId = "<NA>";
    }
    return clientId;
  }
  
  /**
   * Returns the alias name if context is not null, otherwise: "<NA>".
   * For tracing purposes.
   * @return
   */
  private String getTraceAliasName() {
    if (context != null) {
      return context.getAliasName();
    } else {
      return "<NA>";
    }
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
      if (traceLocation.beError()) {
        LogContext.getLocation(LogContext.LOCATION_SERVLET_REQUEST).traceError("ASJ.web.000431", 
            "client [{0}] HttpServletRequest.markServiceFinished" +
            " [{1}] in application [{2}]: Internal error. The invocation stack with servlet names is empty." +
            " ERROR: {3}", new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(), LogContext.getExceptionStackTrace(nsee)}, null, null);        
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
  
  /**
   * If this object is reset, empty string will be returned;
   * otherwise a message describing the unset fields will be returned.
   * 
   * See CSN 1342454 2007 - Non-readable warnings in the default trace
   * 
   * @return
   */
  public String checkReset() {
  	String result = "";
  	if (context != null) { 
  		result += "context=[" + context + "] is not reset to null; ";
  	}
  	if (sessionServletContext != null) { 
  		result += "sessionServletContext=[" + sessionServletContext + "] is not reset to null; ";
  	}
  	if (parameters != null && !parameters.isEmpty()) { 
  		result += "parameters=[" + parameters + "] are not cleared; ";
  	}
  	if (requestURI != null) { 
  		result += "requestURI=[" + requestURI + "] is not reset to null; ";
  	}
  	if (requestURI_internal != null) { 
  		result += "requestURI_internal=[" + requestURI_internal + "] is not reset to null; ";
  	}
  	if (queryString != null) { 
  		result += "queryString=[" + queryString + "] is not reset to null; ";
  	}
  	if (characterEncoding != Constants.DEFAULT_CHAR_ENCODING) { 
  		result += "characterEncoding=[" + characterEncoding + "] is not reset to default encoding [" + Constants.DEFAULT_CHAR_ENCODING + "]; ";
  	}
  	if (characterEncodingSet) { 
  		result += "characterEncodingSet=[" + characterEncodingSet + "] not reset to false; ";
  	}
  	if (httpParameters != null) { 
  		result += "httpParameters=[" + httpParameters + "] is not reset to null; ";
  	}
  	if (realPath != null) { 
  		result += "realPath=[" + realPath + "] is not reset to null; ";
  	}
  	if (response != null) { 
  		result += "response=[" + response + "] is not reset to null; ";
  	}
  	if (paramStack != null && !paramStack.isEmpty()) { 
  		result += "paramStack=[" + paramStack + "] is not cleared; ";
  	}
  	if (queriesStack != null && !queriesStack.isEmpty()) { 
  		result += "queriesStack=[" + queriesStack + "] is not cleared; ";
  	}
  	if (isParsedParamStack != null && !isParsedParamStack.isEmpty()) { 
  		result += "isParsedParamStack=[" + isParsedParamStack + "] is not cleared; ";
  	}
  	if (getInputStream) { 
  		result += "getInputStream=[" + getInputStream + "] is not reset to false; ";
  	}
  	if (getReader) { 
  		result += "getReader=[" + getReader + "] is not reset to false; ";
  	}
  	if (inputStream != null) { 
  		result += "inputStream=[" + inputStream + "] is not reset to null; ";
  	}
  	if (reader != null) { 
  		result += "reader=[" + reader + "] is not reset to null; ";
  	}
  	if (sessionAccessed) { 
  		result += "sessionAccessed=[" + sessionAccessed + "] is not reset to false; ";
  	}
  	if (parametersParsed) { 
  		result += "parametersParsed=[" + parametersParsed + "] is not reset to false; ";
  	}
  	if (queryStringParsed) { 
  		result += "queryStringParsed=[" + queryStringParsed + "] is not reset to false; ";
  	}
  	if (jspPrecompile != null) { 
  		result += "jspPrecompile=[" + jspPrecompile + "] is not reset to null; ";
  	}
  	if (jspFlag) { 
  		result += "jspFlag=[" + jspFlag + "] is not reset to false; ";
  	}
  	if (threadLoader != null) { 
  		result += "threadLoader=[" + threadLoader + "] is not reset to null; ";
  	}
  	if (servletPathIncluded != null) { 
  		result += "servletPathIncluded=[" + servletPathIncluded + "] is not reset to null; ";
  	}
  	if (cookieSet) { 
  		result += "cookieSet=[" + cookieSet + "] is not reset to false; ";
  	}
  	if (reusedSessionCookie) { 
  		result += "reusedSessionCookie=[" + reusedSessionCookie + "] is not reset to false; ";
  	}
  	if (currentServlet != null) { 
  		result += "currentServlet=[" + currentServlet + "] is not reset to null; ";
  	}
  	if (invokedServletNamesStack != null && !invokedServletNamesStack.isEmpty()) { 
  		result += "invokedServletNamesStack=[" + invokedServletNamesStack + "] is not cleared; ";
  	}
  	if (currentlyInvokedServletName != null) { 
  		result += "currentlyInvokedServletName=[" + currentlyInvokedServletName + "] is not reset to null; ";
  	}
  	if (parametersLength != -1){ 
  		result += "parametersLength=[" + parametersLength + "] is not reset to -1; ";
  	}
  	return result;
  }
  
  public void setForcedAuthType(String authType) {
    forcedAuthType = authType;
  }

  /**
   * Setter method for the <code>parameters</code>.
   * @param parameters
   */
  protected void setParameters(HashMapObjectObject parameters) {
    if (traceLocation.beDebug()) {
      // dump the content of parameters
      String text = "";
      if (parameters == null) {
        text += "the new parameters are [null]\r\n";
      } else {
        Object[] keys = (Object[])(parameters.getAllKeys());
        for (int i=0; i<keys.length; i++) {
          text += "key: [" + (String)keys[i] + "]\r\n";
          text += "     values:\r\n";
          String[] values = (String[])(parameters.get(keys[i]));
          if (values == null) {
            text += "     values are [null]\r\n"; 
          } else {
            for (int j=0; j<values.length; j++) {
              text += "     [" + values[j] + "]\r\n";
            }
            text += "\r\n";
          }
        }
      }

      traceDebug("setParameters", text);
    }
    if (parameters == null) {
      this.parameters = new HashMapObjectObject();
    } else {
      this.parameters = parameters;
    }
  }
}
