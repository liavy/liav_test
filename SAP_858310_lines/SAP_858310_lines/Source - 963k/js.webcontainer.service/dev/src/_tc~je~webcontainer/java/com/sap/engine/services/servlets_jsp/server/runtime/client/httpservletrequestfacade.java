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

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import com.sap.engine.services.httpserver.interfaces.HttpParameters;
import com.sap.engine.services.httpserver.interfaces.client.SslAttributes;
import com.sap.engine.services.httpserver.lib.protocol.HeaderNames;
import com.sap.engine.services.httpserver.lib.util.ByteArrayUtils;
import com.sap.engine.services.servlets_jsp.lib.multipart.impl.MultipartMessageImpl;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.lib.DefaultLocaleEnumeration;
import com.sap.tc.logging.Location;

/*
 *
 * @author Galin Galchev
 * @version 4.0
 */

/**
 * The servlet container creates an HttpServletRequest object and passes it as an argument to the
 * servlet's service methods (doGet, doPost, etc). Represents the Http request for thi servlet or jsp.
 * Implements the ServletRequest interface to provide request information for HTTP servlets.
 *
 */
public abstract class HttpServletRequestFacade extends HttpServletRequestImpl {
  private static final String[] cipherPhrase = new String[] {"_WITH_NULL_", "_WITH_IDEA_CBC_", "_WITH_RC2_CBC_40_", "_WITH_RC4_40_", "_WITH_RC4_128_", "_WITH_DES40_CBC_", "_WITH_DES_CBC_", "_WITH_3DES_EDE_CBC_"};
  private static final int[] cipherSize = new int[] {0, 128, 40, 40, 128, 40, 56, 168};
  private static final byte[] multipart_sl = "multipart/".getBytes();
  public static final String ZONE_SEPARATOR = "com.sap.servlet.separator.zone";
  public static final String REQUEST_ALIAS = "com.sap.servlet.request.alias";
  public static final String REQUEST_ZONE = "com.sap.servlet.request.zone";
  private static Location currentLocation = Location.getLocation(HttpServletRequestFacade.class);
  private static Location traceLocation = LogContext.getLocationServletRequest();
  /**
   *  Available custom information about a request
   */
  protected ConcurrentHashMap attributes = new ConcurrentHashMap(16);
  private int contentLength = -1;
  private boolean contentLengthParsed = false;
  private String contentType = null;
  private boolean contentTypeParsed = false;
  private String protocol = null;
  private boolean protocolParsed = false;
  private String serverName = null;
  private boolean serverNameParsed = false;
  private int serverPort = -1;
  private boolean serverPortParsed = false;
  private String remoteAddr = null;
  private boolean remoteAddrParsed = false;
  private String remoteHost = null;
  private boolean remoteHostParsed = false;
  private int remotePort = -1;
  private boolean remotePortParsed = false;
  private String localName = null;
  private boolean localNameParsed = false;
  private String localAddr = null;
  private boolean localAddrParsed = false;
  private int localPort = -1;
  private boolean localPortParsed = false;
  private Locale locale = null;
  private boolean localeParsed = false;
  private Vector locales = null;
  private boolean localesParsed = false;
  private boolean isSecure = false;
  private boolean isSecureParsed = false;
  private Cookie[] cookies = null;
  private boolean cookiesParsed = false;
  private String method = null;
  private boolean methodParsed = false;
  private String pathInfo = null;
  private boolean pathInfoParsed = false;
  private boolean pathInfoSet = false;
  private String pathTranslated = null;
  private boolean pathTranslatedParsed = false;
  private String contextPath = null;
  private boolean contextPathParsed = false;
  private String requestedSessionId = null;
  private boolean requestedSessionIdParsed = false;
  private String requestURI = null;
  private boolean requestURIParsed = false;
  private String requestURIinternal = null;
  private boolean requestURIinternalParsed = false;
  private String requestURL = null;
  private boolean requestURLParsed = false;
  private boolean isRequestedSessionIdFromCookie = false;
  private boolean isRequestedSessionIdFromCookieParsed = false;
  private boolean isRequestedSessionIdFromURL = false;
  private boolean isRequestedSessionIdFromURLParsed = false;
  private boolean forwardAttsSet = false;
  private boolean errorHandler = false;
  
  private String servletName = null;
  
  public void setContext(ApplicationContext context) {
    super.setContext(context);
  }
  /**
   * Create an instance and initiate it from the Http request.
   *
   * @param   httpParameters  represents the Http request
   * @param   response  represents Http response for the servlet
   */
  public void init(ApplicationContext applicationContext, HttpParameters httpParameters, HttpServletResponseFacade response) {
    super.init(applicationContext, httpParameters, response);
    if (serviceContext.getWebContainerProperties().getMultipartBodyParameterName() != null) {
      byte[] contentType = httpParameters.getRequest().getHeaders().getHeader(HeaderNames.entity_header_content_type_);
      contentType = ByteArrayUtils.trim(contentType);
      if (ByteArrayUtils.startsWith(contentType, multipart_sl)) {
        try {
          attributes.put(serviceContext.getWebContainerProperties().getMultipartBodyParameterName(),
              new MultipartMessageImpl(getInputStreamNoCheck(), new String(contentType), this));
        } catch (IOException io) {
          LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000178",
              "Error initializing http Servlet request object. Cannot read multipart request body", io, null, null);
        }
      }
    }
    if (httpParameters.getRequestAttributes() != null) {
      Enumeration en = httpParameters.getRequestAttributes().keys();
      while (en.hasMoreElements()) {
        String key = (String)en.nextElement();
        attributes.put(key, httpParameters.getRequestAttributes().get(key));
      }
    }
    if (ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getZoneSeparator() != null) {
      attributes.put(ZONE_SEPARATOR, ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getZoneSeparator());
    }
    if (httpParameters.getRequestPathMappings().getZoneName() != null) {
      attributes.put(REQUEST_ALIAS, httpParameters.getRequestPathMappings().getAliasName());
      attributes.put(REQUEST_ZONE, httpParameters.getRequestPathMappings().getZoneName());
    }
    SslAttributes sslAttributes = httpParameters.getRequest().getSslAttributes();
    if (sslAttributes == null) {
      //Trace only:
      if (traceLocation.bePath()) {
        StringBuilder atr = new StringBuilder("");
        Enumeration keys = attributes.keys();
        while (keys.hasMoreElements()) {
          Object key = keys.nextElement();
          atr.append(key);
          atr.append(" = ");
          atr.append(attributes.get(key));
          atr.append(", ");
        }
        
        if (atr.length() > 2) {
          if (atr.charAt(atr.length() - 2) == ',' && atr.charAt(atr.length() - 1) == ' ') {
            atr.delete(atr.length() - 2, atr.length() - 1);
          }
        }

        tracePath("init", "new request initialized, attributes: [" + atr + "], url: [" + httpParameters.getRequest().getRequestLine().getRequestLine() + "]");
      }
      return;
    } //if (sslAttributes == null)
    X509Certificate[] certificates = sslAttributes.getCertificates(); //may be null
    if (certificates != null && certificates.length > 0) {
      attributes.put("javax.servlet.request.X509Certificate", certificates);
    }
    int keySize = sslAttributes.getKeySize();
    String cipherSuite = sslAttributes.getCipherSuite();
    if (cipherSuite != null) {
      attributes.put("javax.servlet.request.cipher_suite", cipherSuite);
      if (keySize <= 0) {
        for (int c = 0; c < cipherPhrase.length; c++) {
          if (cipherSuite.indexOf(cipherPhrase[c]) != -1) {
            keySize = cipherSize[c];
          }
        }
      }
    }

    if (keySize > -1) {
      attributes.put("javax.servlet.request.key_size", new Integer(keySize));
    }
    if (traceLocation.bePath()) {
      StringBuilder atr = new StringBuilder("");
      Enumeration keys = attributes.keys();
      while (keys.hasMoreElements()) {
        Object key = keys.nextElement();
        atr.append(key);
        atr.append(" = ");
        atr.append(attributes.get(key));
        atr.append(", ");
      }
      
      if (atr.length() > 2) {
        if (atr.charAt(atr.length() - 2) == ',' && atr.charAt(atr.length() - 1) == ' ') {
          atr.delete(atr.length() - 2, atr.length() - 1);
        }
      }
      
      tracePath("init", "new request initialized, attributes: [" + atr + "], url: [" + httpParameters.getRequest().getRequestLine().getRequestLine() + "]");
    }
  }

  public void reset() {
    try {
      super.reset();
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable t) {
    	//TODO:Polly type:ok
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000118", 
        "Cannot reset servlet request.", t, null, null);
    }
    // Clears files from disk in case of multipart requests
    String multipartBodyParameterName = ServiceContext.getServiceContext()
      .getWebContainerProperties().getMultipartBodyParameterName();
    if (multipartBodyParameterName != null) {
      MultipartMessageImpl mm = (MultipartMessageImpl) attributes
        .get(multipartBodyParameterName);
      if (mm != null) { mm.clear(); }
    }
    attributes.clear();
    contentLength = -1;
    contentLengthParsed = false;
    contentType = null;
    contentTypeParsed = false;
    protocol = null;
    protocolParsed = false;
    serverName = null;
    serverNameParsed = false;
    serverPort = -1;
    serverPortParsed = false;
    remoteAddr = null;
    remoteAddrParsed = false;
    remoteHost = null;
    remoteHostParsed = false;
    remotePort = -1;
    remotePortParsed = false;
    localName = null;
    localNameParsed = false;
    localAddr = null;
    localAddrParsed = false;
    localPort = -1;
    localPortParsed = false;
    locale = null;
    localeParsed = false;
    locales = null;
    localesParsed = false;
    isSecure = false;
    isSecureParsed = false;
    cookies = null;
    cookiesParsed = false;
    method = null;
    methodParsed = false;
    pathInfo = null;
    pathInfoParsed = false;
    pathInfoSet = false;
    pathTranslated = null;
    pathTranslatedParsed = false;
    contextPath = null;
    contextPathParsed = false;
    requestedSessionId = null;
    requestedSessionIdParsed = false;
    requestURI = null;
    requestURIParsed = false;
    requestURIinternal = null;
    requestURIinternalParsed = false;
    requestURL = null;
    requestURLParsed = false;
    isRequestedSessionIdFromCookie = false;
    isRequestedSessionIdFromCookieParsed = false;
    isRequestedSessionIdFromURL = false;
    isRequestedSessionIdFromURLParsed = false;
    forwardAttsSet = false;
    errorHandler = false;
    servletName = null;
  }

  protected void contextReplaced() {
    cookiesParsed = false;
    requestedSessionIdParsed = false;
    pathTranslatedParsed = false;
    isRequestedSessionIdFromCookieParsed = false;
    isRequestedSessionIdFromURLParsed = false;
    contextPathParsed = false;
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
      traceDebug("getAttribute", "name = [" + s + "], result = [" + result + "]");
    }
    return result;
  }

  /**
   * Returns an Enumeration containing the names of the attributes available to this request.
   *
   * @return     enumeration with the namesof the attributes
   */
  public Enumeration getAttributeNames() {
    if (traceLocation.bePath()) {
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

      tracePath("getAttributeNames", "result: [" + atr + "]");
    }
    return attributes.keys();
  }

  /**
   * Stores an attribute in this request.
   *
   * @param   s  name of the attribute
   * @param   obj  value of the attribute
   */
  public void setAttribute(String s, Object obj) {
    if (traceLocation.beDebug()) {
      traceDebug("setAttribute", "name = [" + s + "], value = [" + obj + "]");
    }
    if (obj == null) {
      removeAttribute(s);
      return;
    }
    Object oldValue = attributes.put(s, obj);
    if (oldValue != null) { //the attribute is replaced
      context.getWebEvents().requestAttributeReplaced(this, s, oldValue);
    } else {
      context.getWebEvents().requestAttributeAdded(this, s, obj);
    }
  }

  /**
   * Removes an attribute from this request.
   *
   * @param   s  name of the attribute
   */
  public void removeAttribute(String s) {
    if (traceLocation.beDebug()) {
      traceDebug("removeAttribute", "name = [" + s + "]");
    }
    Object obj = attributes.remove(s);
    context.getWebEvents().requestAttributeRemoved(this, s, obj);
  }

  public int getContentLength() {
    if (!contentLengthParsed) {
      contentLength = super.getContentLength();
      contentLengthParsed = true;
    }
    if (traceLocation.beDebug()) {
      traceDebug("getContentLength", "contentLength = [" + contentLength + "]");
    }
    return contentLength;
  }

  public String getContentType() {
    if (!contentTypeParsed) {
      contentType = super.getContentType();
      contentTypeParsed = true;
    }
    if (traceLocation.beDebug()) {
      traceDebug("getContentType", "contentType = [" + contentType + "]");
    }
    return contentType;
  }

  public String getProtocol() {
    if (!protocolParsed) {
      protocol = super.getProtocol();
      protocolParsed = true;
    }
    if (traceLocation.beDebug()) {
      traceDebug("getProtocol", "protocol = [" + protocol + "]");
    }
    return protocol;
  }

  public String getServerName() {
    if (!serverNameParsed) {
      serverName = super.getServerName();
      serverNameParsed = true;
    }
    if (traceLocation.beDebug()) {
     traceDebug("getServerName", "serverName = [" + serverName + "]");
    }
    return serverName;
  }

  public int getServerPort() {
    if (!serverPortParsed) {
      serverPort = super.getServerPort();
      serverPortParsed = true;
    }
    if (traceLocation.beDebug()) {
      traceDebug("getServerPort", "serverPort = [" + serverPort + "]");
    }
    return serverPort;
  }

  public String getRemoteAddr() {
    if (!remoteAddrParsed) {
      remoteAddr = super.getRemoteAddr();
      remoteAddrParsed = true;
    }
    if (traceLocation.beDebug()) {
      traceDebug("getRemoteAddr", "remoteAddr = [" + remoteAddr + "]");
    }
    return remoteAddr;
  }

  public String getRemoteHost() {
    if (!remoteHostParsed) {
      remoteHost = super.getRemoteHost();
      remoteHostParsed = true;
    }
    if (traceLocation.beDebug()) {
      traceDebug("getRemoteHost", "remoteHost = [" + remoteHost + "]");
    }
    return remoteHost;
  }

  public int getRemotePort() {
    if (!remotePortParsed) {
      remotePort = super.getRemotePort();
      remotePortParsed = true;
    }
    if (traceLocation.beDebug()) {
      traceDebug("getRemotePort", "remotePort = [" + remotePort + "]");
    }
    return remotePort;
  }

  public String getLocalName() {
    if (!localNameParsed) {
      localName = super.getLocalName();
      localNameParsed = true;
    }
    if (traceLocation.beDebug()) {
      traceDebug("getLocalName", "localName = [" + localName + "]");
    }
    return localName;
  }

  public String getLocalAddr() {
    if (!localAddrParsed) {
      localAddr = super.getLocalAddr();
      localAddrParsed = true;
    }
    if (traceLocation.beDebug()) {
      traceDebug("getLocalAddr", "localAddr = [" + localAddr + "]");
    }
    return localAddr;
  }

  public int getLocalPort() {
    if (!localPortParsed) {
      localPort = super.getLocalPort();
      localPortParsed = true;
    }
    if (traceLocation.beDebug()) {
      traceDebug("getLocalPort", "localPort = [" + localPort + "]");
    }
    return localPort;
  }

  public Locale getLocale() {
  	synchronized (this) {
	    if (!localeParsed) {
	      locales = super.getLocalesVector();
	      localesParsed = true;
	      
	      localeParsed = true;
	      if (locales == null) {
	        locale = Locale.getDefault();
	      } else {
	        locale = (Locale) getLocales().nextElement();
	      }
	    }
  	}
    if (traceLocation.beDebug()) {
      traceDebug("getLocale", "locale = [" + locale + "]");
    }
    return locale;
  }

  public Enumeration getLocales() {
    if (!localesParsed) {
      locales = super.getLocalesVector();
      localesParsed = true;
    }
    if (locales == null) {
      return new DefaultLocaleEnumeration();
    }
    if (traceLocation.beDebug()) {
      StringBuilder res = new StringBuilder("");
      Enumeration keys = attributes.keys();
      while (keys.hasMoreElements()) {        
        res.append(keys.nextElement());
        res.append(", ");
      }
      
      if (res.length() > 2) {
        if (res.charAt(res.length() - 2) == ',' && res.charAt(res.length() - 1) == ' ') {
          res.delete(res.length() - 2, res.length() - 1);
        }
      }

      traceDebug("getLocales", "return: [" + res + "]");
    }
    return locales.elements();
  }

  public boolean isSecure() {
    if (!isSecureParsed) {
      isSecure = super.isSecure();
      isSecureParsed = true;
    }
    if (traceLocation.beDebug()) {
      traceDebug("isSecure", "isSecure = [" + isSecure + "]");
    }
    return isSecure;
  }

  public Cookie[] getCookies() {
    if (!cookiesParsed) {
      cookies = super.getCookies();
      cookiesParsed = true;
    }
    if (traceLocation.beDebug()) {
       String res = "";
      for (int i = 0; cookies != null && i < cookies.length; i++) {
        if (cookies[i] == null) {
          res += cookies[i] + ", ";
        } else {
          res += cookies[i].getName() + " = " + cookies[i].getValue() + ", ";
        }
      }
      if (res.endsWith(", ")) {
        res = res.substring(0, res.length() - 2);
      }
      traceDebug("getCookies", "return: [" + res + "]");
    }
    return cookies;
  }

  public String getMethod() {
    if (!methodParsed) {
      method = super.getMethod();
      methodParsed = true;
    }
    if (traceLocation.beDebug()) {
      traceDebug("getMethod", "method = [" + method + "]");
    }
    return method;
  }

  public String getPathInfo() {
    if (!pathInfoParsed) {
      pathInfo = super.parsePathInfo(pathInfoSet, pathInfo);
      pathInfoParsed = true;
    }
    if (traceLocation.beDebug()) {
      traceDebug("getPathInfo", "pathInfo = [" + pathInfo + "]");
    }
    return pathInfo;
  }

  public void setPathInfo(String pathInfo) {
    pathInfoSet = true;
    this.pathInfo = pathInfo;
    pathInfoParsed = false;
  }

  public String getPathTranslated() {
    if (!pathTranslatedParsed || !pathInfoParsed) {
      pathTranslated = super.getPathTranslated();
      pathTranslatedParsed = true;
    }
    if (traceLocation.beDebug()) {
      traceDebug("getPathTranslated", "pathTranslated = [" + pathTranslated + "]");
    }
    return pathTranslated;
  }

  public String getContextPath() {
    if (!contextPathParsed) {
      contextPath = super.getContextPath();
      contextPathParsed = true;
    }
    if (traceLocation.beDebug()) {
      traceDebug("getContextPath", "contextPath = [" + contextPath + "]");
    }
    return contextPath;
  }

  public String getRequestedSessionId() {
    if (!requestedSessionIdParsed) {
      requestedSessionId = super.getRequestedSessionId();
      requestedSessionIdParsed = false;
    }
    if (traceLocation.beDebug()) {
      traceDebug("getRequestedSessionId", "requestedSessionId = [" + requestedSessionId + "]");
    }
    return requestedSessionId;
  }

  public String getRequestURI() {
    if (!requestURIParsed) {
      requestURI = super.getRequestURI();
      requestURIParsed = true;
    }
    if (traceLocation.beDebug()) {
      traceDebug("getRequestURI", "requestURI = [" + requestURI + "]");
    }
    return requestURI;
  }

  public String getRequestURIinternal() {
    if (!requestURIinternalParsed) {
      requestURIinternal = super.parseRequestURIinternal();
      requestURIinternalParsed = true;
    }
    if (traceLocation.beDebug()) {
      traceDebug("getRequestURIinternal", "requestURIinternal = [" + requestURIinternal + "]");
    }
    return requestURIinternal;
  }

  public void setRequestURI(String newURI) {
    requestURIParsed = false;
    requestURIinternalParsed = false;
    super.setRequestURI(newURI);
  }

  public StringBuffer getRequestURL() {
    if (!requestURLParsed || !requestURIinternalParsed) {
      requestURL = super.parseRequestURL();
      requestURLParsed = true;
    }
    if (traceLocation.beDebug()) {
      traceDebug("getRequestURL", "requestURL = [" + requestURL + "]");
    }
    return new StringBuffer(requestURL);
  }

  public HttpSession getSession() {
    HttpSession httpSession = getSession(true);
    if (traceLocation.beDebug()) {
      traceDebug("getSession", "httpSession = [" + httpSession + "]");
    }
    return httpSession;
  }

  public boolean isRequestedSessionIdFromCookie() {
    if (!isRequestedSessionIdFromCookieParsed) {
      isRequestedSessionIdFromCookie = super.isRequestedSessionIdFromCookie();
      isRequestedSessionIdFromCookieParsed = true;
    }
    return isRequestedSessionIdFromCookie;
  }

  public boolean isRequestedSessionIdFromURL() {
    if (!isRequestedSessionIdFromURLParsed) {
      isRequestedSessionIdFromURL = super.isRequestedSessionIdFromURL();
      isRequestedSessionIdFromURLParsed = true;
    }
    return isRequestedSessionIdFromURL;
  }

	/**
	 * @deprecated
	 */
  public boolean isRequestedSessionIdFromUrl() {
    return isRequestedSessionIdFromURL();
  }
  
  public boolean isForwardAttsSet() {
    return forwardAttsSet;
}
  public void setForwardAttsSet(boolean forwardAttsSet) {
    this.forwardAttsSet = forwardAttsSet; 
  }
  
  public boolean isErrorHandler() {
    return errorHandler;
  }
  
  public void setErrorHandler(boolean errorHandler) {
    this.errorHandler = errorHandler; 
  }
  
  public void setServletName(String servletName) {
    this.servletName = servletName;  
  }
    
  public String getServletName() {
    return servletName;  
  }
  
  
  public void setResponseLengthLog(int totalCount) {
    //sets content length in access log
    getHttpParameters().setResponseLength(totalCount);
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
  	String result = super.checkReset();
  	if (attributes != null && !attributes.isEmpty()) { 
    		result += "attributes=[" + attributes + "] are not cleared; ";
  	}
    if (contentLength != -1) { 
    		result += "contentLength=[" + contentLength + "] is not reset to -1; ";
  	}
    if (contentLengthParsed) { 
    		result += "contentLengthParsed=[" + contentLengthParsed + "] is not reset to false; ";
  	}
    if (contentType != null) { 
    		result += "contentType=[" + contentType + "] is not reset to null; ";
  	}
    if (contentTypeParsed) { 
    		result += "contentTypeParsed=[" + contentTypeParsed + "] is not reset to false; ";
  	}
    if (protocol != null) { 
    		result += "protocol=[" + protocol + "] is not reset to null; ";
  	}
    if (protocolParsed) { 
    		result += "protocolParsed=[" + protocolParsed + "] is not reset to false; ";
  	}
    if (serverName != null) { 
    		result += "serverName=[" + serverName + "] is not reset to null; ";
  	}
    if (serverNameParsed) { 
    		result += "serverNameParsed=[" + serverNameParsed + "] is not reset to false; ";
  	}
    if (serverPort != -1) { 
    		result += "serverPort=[" + serverPort + "] is not reset to -1; ";
  	}
    if (serverPortParsed) { 
    		result += "serverPortParsed=[" + serverPortParsed + "] is not reset to false; ";
  	}
    if (remoteAddr != null) { 
    		result += "remoteAddr=[" + remoteAddr + "] is not reset to null; ";
  	}
    if (remoteAddrParsed) { 
    		result += "remoteAddrParsed=[" + remoteAddrParsed + "] is not reset to false; ";
  	}
    if (remoteHost != null) { 
    		result += "remoteHost=[" + remoteHost + "] is not reset to null; ";
  	}
    if (remoteHostParsed) { 
    		result += "remoteHostParsed=[" + remoteHostParsed + "] is not reset to false; ";
  	}
    if (remotePort != -1) { 
    		result += "remotePort=[" + remotePort + "] is not reset to -1; ";
  	}
    if (remotePortParsed) { 
    		result += "remotePortParsed=[" + remotePortParsed + "] is not reset to false; ";
  	}
    if (localName != null) { 
    		result += "localName=[" + localName + "] is not reset to null; ";
  	}
    if (localNameParsed) { 
    		result += "localNameParsed=[" + localNameParsed + "] is not reset to false; ";
  	}
    if (localAddr != null) { 
    		result += "localAddr=[" + localAddr + "] is not reset to null; ";
  	}
    if (localAddrParsed) { 
    		result += "localAddrParsed=[" + localAddrParsed + "] is not reset to false; ";
  	}
    if (localPort != -1) { 
    		result += "localPort=[" + localPort + "] is not reset to -1; ";
  	}
    if (localPortParsed) { 
    		result += "localPortParsed=[" + localPortParsed + "] is not reset to false; ";
  	}
    if (locale != null) { 
    		result += "locale=[" + locale + "] is not reset to null; ";
  	}
    if (localeParsed) { 
    		result += "localeParsed=[" + localeParsed + "] is not reset to false; ";
  	}
    if (locales != null) { 
    		result += "locales=[" + locales + "] are not reset to null; ";
  	}
    if (localesParsed) { 
    		result += "localesParsed=[" + localesParsed + "] is not reset to false; ";
  	}
    if (isSecure) { 
    		result += "isSecure=[" + isSecure + "] is not reset to false; ";
  	}
    if (isSecureParsed) { 
    		result += "isSecureParsed=[" + isSecureParsed + "] is not reset to false; ";
  	}
    if (cookies != null) { 
    		result += "cookies=[" + cookies + "] are not reset to null; ";
  	}
    if (cookiesParsed) { 
    		result += "cookiesParsed=[" + cookiesParsed + "] is not reset to false; ";
  	}
    if (method != null) { 
    		result += "method=[" + method + "] is not reset to null; ";
  	}
    if (methodParsed) { 
    		result += "methodParsed=[" + methodParsed + "] is not reset to false; ";
  	}
    if (pathInfo != null) { 
    		result += "pathInfo=[" + pathInfo + "] is not reset to null; ";
  	}
    if (pathInfoParsed) { 
    		result += "pathInfoParsed=[" + pathInfoParsed + "] is not reset to false; ";
  	}
    if (pathInfoSet) { 
    		result += "pathInfoSet=[" + pathInfoSet + "] is not reset to false; ";
  	}
    if (pathTranslated != null) { 
    		result += "pathTranslated=[" + pathTranslated + "] is not reset to null; ";
  	}
    if (pathTranslatedParsed) { 
    		result += "pathTranslatedParsed=[" + pathTranslatedParsed + "] is not reset to false; ";
  	}
    if (contextPath != null) { 
    		result += "contextPath=[" + contextPath + "] is not reset to null; ";
  	}
    if (contextPathParsed) { 
    		result += "contextPathParsed=[" + contextPathParsed + "] is not reset to false; ";
  	}
    if (requestedSessionId != null) { 
    		result += "requestedSessionId=[" + requestedSessionId + "] is not reset to null; ";
  	}
    if (requestedSessionIdParsed) { 
    		result += "requestedSessionIdParsed=[" + requestedSessionIdParsed + "] is not reset to false; ";
  	}
    if (requestURI != null) { 
    		result += "requestURI=[" + requestURI + "] is not reset to null; ";
  	}
    if (requestURIParsed) { 
    		result += "requestURIParsed=[" + requestURIParsed + "] is not reset to false; ";
  	}
    if (requestURIinternal != null) { 
    		result += "requestURIinternal=[" + requestURIinternal + "] is not reset to null; ";
  	}
    if (requestURIinternalParsed) { 
    		result += "requestURIinternalParsed=[" + requestURIinternalParsed + "] is not reset to false; ";
  	}
    if (requestURL != null) { 
    		result += "requestURL=[" + requestURL + "] is not reset to null; ";
  	}
    if (requestURLParsed) { 
    		result += "requestURLParsed=[" + requestURLParsed + "] is not reset to false; ";
  	}
    if (isRequestedSessionIdFromCookie) { 
    		result += "isRequestedSessionIdFromCookie=[" + isRequestedSessionIdFromCookie + "] is not reset to false; ";
  	}
    if (isRequestedSessionIdFromCookieParsed) { 
    		result += "isRequestedSessionIdFromCookieParsed=[" + isRequestedSessionIdFromCookieParsed + "] is not reset to false; ";
  	}
    if (isRequestedSessionIdFromURL) { 
    		result += "isRequestedSessionIdFromURL=[" + isRequestedSessionIdFromURL + "] is not reset to false; ";
  	}
    if (isRequestedSessionIdFromURLParsed) { 
    		result += "isRequestedSessionIdFromURLParsed=[" + isRequestedSessionIdFromURLParsed + "] is not reset to false; ";
  	} 
    if (forwardAttsSet) { 
    		result += "forwardAttsSet=[" + forwardAttsSet + "] is not reset to false; ";
  	}
    if (errorHandler) { 
    		result += "errorHandler=[" + errorHandler + "] is not reset to false; ";
  	}
    if (servletName != null) { 
    		result += "servletName=[" + servletName + "] is not reset to null; ";
  	}
  	return result;
  }

}
