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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.http.Cookie;

import com.sap.engine.lib.util.ArrayObject;
import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.lib.util.iterators.SnapShotEnumeration;
import com.sap.engine.services.httpserver.interfaces.HttpParameters;
import com.sap.engine.services.httpserver.lib.CookieParser;
import com.sap.engine.services.httpserver.lib.CookieUtils;
import com.sap.engine.services.httpserver.lib.HttpCookie;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.httpserver.lib.ResponseCodes;
import com.sap.engine.services.httpserver.lib.WebCookieConfig;
import com.sap.engine.services.httpserver.lib.headers.MimeHeaders;
import com.sap.engine.services.httpserver.lib.protocol.HeaderNames;
import com.sap.engine.services.httpserver.lib.protocol.HeaderValues;
import com.sap.engine.services.httpserver.lib.util.ByteArrayUtils;
import com.sap.engine.services.httpserver.lib.util.EncodingUtils;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.servlets_jsp.runtime_api.SapHttpServletResponse;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.WebContainerProperties;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.application.SessionServletContext;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIllegalStateException;
import com.sap.engine.services.servlets_jsp.server.lib.Constants;
import com.sap.engine.services.servlets_jsp.server.lib.StringUtils;
import com.sap.engine.services.servlets_jsp.server.lib.WebParseUtils;
import com.sap.engine.session.SessionException;
import com.sap.engine.session.runtime.SessionRequest;
import com.sap.tc.logging.Location;

public abstract class HttpServletResponseBase implements SapHttpServletResponse {
  public static final String FORCE_ENCODING = "forceEncoding:";

  private Location currentLocation = Location.getLocation(getClass());
  private Location traceLocation = LogContext.getLocationServletResponse();

  private static final byte[] HTTP_11 = "HTTP/1.1".getBytes();
  private static final byte[] SPACE = " ".getBytes();
  private static final byte[] RN = "\r\n".getBytes();
  private static final byte SP = ' ';
  private static final byte R = '\r';
  private static final byte N = '\n';

  protected int status = ResponseCodes.code_ok;
  private String statusSrc = null;
  private MimeHeaders headers = null;
  private ArrayObject cookies = null;
  protected String characterEncoding = null;
  protected String contentType = null;
  private Locale locale = null;

  protected ServiceContext serviceContext = null;
  private ApplicationContext context;
  private HttpServletRequestFacade requestFacade;
  private boolean committed = false;
  protected boolean isIncluded = false;
  private boolean setSessionCookie = false;
  protected boolean isCharacterEncodingSet = false;
  //protected boolean statusLocked = false; //For servlet 2.4 SRV.9.9 Error Handling/Request Attributes.

  /**
   * Whether the output stream is taken as PrintWriter object
   */
  protected boolean getWriter = false;

  protected int contentLength = -1;

  public HttpServletResponseBase() {
    serviceContext = ServiceContext.getServiceContext();
  }

  protected void init(ApplicationContext applicationContext, MimeHeaders responseHeaders, HttpServletRequestFacade requestFacade) {
    this.context = applicationContext;
    this.headers = responseHeaders;
    this.requestFacade = requestFacade;
  }

  public void setContext(ApplicationContext applicationContext) {
    this.context = applicationContext;
  }

  public ApplicationContext getServletContext() {
    return context;
  }

  public HttpServletRequestFacade getServletRequest() {
    return requestFacade;
  }

  public HttpParameters getHttpParameters() {
    return requestFacade.getHttpParameters();
  }

  /**
   * Returns a boolean indicating if the response has been committed.
   *
   * @return     whether the response have been committed
   */
  public boolean isCommitted() {
    if (traceLocation.beDebug()) {
      traceDebug("isCommitted", "return: [" + committed + "]");
    }
    return committed;
  }

	/**
	 * @deprecated - As of version 2.1, due to ambiguous meaning of the message parameter.
	 * To set a status code use setStatus(int), to send an error with a description use sendError(int, String).
	 * Sets the status code and message for this response.
	 */
  public void setStatus(int i, String status) {
    if (traceLocation.bePath()) {
      trace("setStatus", "code = [" + i + "], message = [" + status + "], isIncluded = [" + isIncluded + "]");
    }
    if (locked(true)) {
      return;
    }
//    //TODO: Reverted in Servlet 2.5
//    if (isStatusLocked()) {
//      trace("setStatus", "Ignoring setStatus to [" + i + "] Status remains [" + this.status + "]");
//      return;
//    }
 	  this.statusSrc = StringUtils.replaceCRLF(status);
		if (status != this.statusSrc) {
			LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000189", 
					"CR or LF found in response status:[{0}].Replaced with spaces.", new Object[]{status}, context.getApplicationName(), context.getCsnComponent());

		} else {
			this.statusSrc = status;
		}
    this.status = i;
  }

  public void setStatus(int i) {
    if (traceLocation.bePath()) {
      trace("setStatus", "code = [" + i + "], isIncluded = [" + isIncluded + "]");
    }
    if (locked(true)) {
      return;
    }
//    //TODO: Reverted in Servlet 2.5
//    if (isStatusLocked()) {
//      trace("setStatus", "Ignoring setStatus to [" + i + "] Status remains [" + this.status + "]");
//      return;
//    }
    setStatus(i, ResponseCodes.reason(i, getServletContext().getAliasName()));
  }
  
  public int getStatus() {
    return status;
  }

  public boolean containsHeader(String s) {
    boolean result = headers.containsHeader(s);
    if (traceLocation.beDebug()) {
      traceDebug("containsHeader", "name = [" + s + "], result: [" + result + "], value = [" +
        headers.getHeader(s) + "]");
    }
    return result;
  }

  public String getHeader(String s) {
    return headers.getHeader(s);
  }

  public byte[] getHeader(byte[] s) {
    return headers.getHeader(s);
  }

  public int getIntHeader(byte[] name) {
    return headers.getIntHeader(name);
  }

  public void addHeader(String headername, String value) {
    if (traceLocation.bePath()) {
      trace("addHeader", "name = [" + headername + "], value = [" + value + "], isIncluded = [" + isIncluded + "]");
    }
    if (locked(false)) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000190", 
          "Trying to set a header with method [addHeader] after the response is committed or in an included servlet. " +
          "The header will be ignored. " +
          "The header is: [{0} : {1}]", new Object[]{headername, value}, context.getApplicationName(), context.getCsnComponent());
      return;
    }

    if (headername.equalsIgnoreCase(HeaderNames.entity_header_content_length)) {
      try {
        setContentLengthHeader(Integer.parseInt(value));
      } catch (NumberFormatException nfe) {
        if (traceLocation.beError()) {
            LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceError("ASJ.web.000432", 
                "client [{0}] HttpServletResponseFacade.addHeader" +
                " [{1}] in application [{2}]: Cannot add value for header: {3}." +
                " ERROR: {4}", new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(), HeaderNames.entity_header_content_length, LogContext.getExceptionStackTrace(nfe)},  context.getApplicationName(), context.getCsnComponent());
        }
      }
    }
    if (headername.equalsIgnoreCase(HeaderNames.entity_header_content_encoding)) {
      headers.putHeader(headername.getBytes(), value.getBytes());
    } else if (headername.equalsIgnoreCase(HeaderNames.entity_header_content_type)) {
      setContentType(value);
    } else {
      if (headername.equalsIgnoreCase(HeaderNames.response_header_set_cookie) &&
          ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getSystemCookiesDataProtection()) {
        String lowerCaseValue = value.toLowerCase();
        if (lowerCaseValue.startsWith(CookieParser.jsessionid_cookie.toLowerCase()) ||
            lowerCaseValue.startsWith(CookieParser.app_cookie_prefix.toLowerCase())) {          
          if (!lowerCaseValue.endsWith(CookieUtils.sHttpOnly.toLowerCase()) &&
              lowerCaseValue.indexOf(CookieUtils.sHttpOnly.toLowerCase() + ";") == -1) {
            value += CookieUtils.sHttpOnly;
          }          
        }        
      }
      if (headername.equalsIgnoreCase(HeaderNames.response_header_set_cookie) &&
          ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getSystemCookiesHTTPSProtection()) {
        String lowerCaseValue = value.toLowerCase();
        if (lowerCaseValue.startsWith(CookieParser.jsessionid_cookie.toLowerCase()) ||
            lowerCaseValue.startsWith(CookieParser.app_cookie_prefix.toLowerCase())) {          
          if (!lowerCaseValue.contains(CookieUtils.sSecure.toLowerCase())) {
            value += CookieUtils.sSecure;
          }          
        }     
      }
      headers.addHeader(StringUtils.filterHeaderName(headername), value.getBytes());
    }
  }

  /**
   * Adds a Content-Encoding response header with the given integer value.
   * If the value is gzip (invoked by GzipResponseStream), the method adds
   * "-gzip" extinction to each ETag header
   *
   * @param   value  the assigned integer value
   */
  public void addHeaderContentEncoding(String value) {
    if (traceLocation.bePath()) {
      trace("addHeader", "name = [" + HeaderNames.entity_header_content_encoding + "], value = [" + value + "], isIncluded = [" + isIncluded + "]");
    }
    headers.addHeader(HeaderNames.entity_header_content_encoding_, value.getBytes());

    if (value.equals("gzip")) {
      adjustETagHeader();
    }
  }

  public void addDateHeader(String headername, long date) {
    if (traceLocation.bePath()) {
      trace("addDateHeader", "name = [" + headername + "], value = [" + date + "], isIncluded = [" + isIncluded + "]");
    }
    if (locked(false)) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000191",
          "Trying to set a header with method [addDateHeader] after the response is committed or in an included servlet. " +
          "The header will be ignored. " +
          "The header is: [{0} : {1}].", new Object[]{headername, date}, context.getApplicationName(), context.getCsnComponent());
      return;
    }
    headers.addDateHeader(StringUtils.filterHeaderName(headername), date);
  }

  public void addIntHeader(String headername, int value) {
    if (traceLocation.bePath()) {
      trace("addIntHeader", "name = [" + headername + "], value = [" + value + "], isIncluded = [" + isIncluded + "]");
    }
    if (locked(false)) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000192",
          "Trying to set a header with method [addIntHeader] after the response is committed or in an included servlet. " +
          "The header will be ignored. " +
          "The header is: [{0} : {1}].", new Object[]{headername, value}, context.getApplicationName(), context.getCsnComponent());
      return;
    }
    if (headername.equalsIgnoreCase(new String(HeaderNames.entity_header_content_length))) {
      setContentLengthHeader(value);
    } else {
      headers.addIntHeader(StringUtils.filterHeaderName(headername), value);
    }
  }

  public void setDateHeader(String headername, long date) {
    if (traceLocation.bePath()) {
      trace("setDateHeader", "name = [" + headername + "], value = [" + date + "], isIncluded = [" + isIncluded + "]");
    }
    if (locked(false)) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000193",
          "Trying to set a header with method [setDateHeader] after the response is committed or in an included servlet. " +
          "The header will be ignored. " +
          "The header is: [{0} : {1}].", new Object[]{headername, date}, context.getApplicationName(), context.getCsnComponent());
      return;
    }
    headers.putDateHeader(StringUtils.filterHeaderName(headername), date);
  }

  public void setDateHeader(String headername) {
    if (locked(true)) {
      return;
    }
    headers.putDateHeader(StringUtils.filterHeaderName(headername));
  }

  public void setHeader(String headername, String s1) {
    if (traceLocation.bePath()) {
      trace("setHeader", "name = [" + headername + "], value = [" + s1 + "], isIncluded = [" + isIncluded + "], committed = [" + isCommitted() + "]");
    }
    if (locked(false)) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000194",
          "Trying to set a header with method [setHeader] after the response is committed or in an included servlet. " +
          "The header will be ignored. " +
          "The header is: [{0} : {1}].", new Object[]{headername, s1}, context.getApplicationName(), context.getCsnComponent());
      return;
    }
    if (headername.equalsIgnoreCase(HeaderNames.entity_header_content_length)) {
      try {
        setContentLengthHeader(Integer.parseInt(s1));
      } catch (NumberFormatException nfe) {
        if (traceLocation.beError()) {
          LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceError("ASJ.web.000433", 
        	        "client [{0}] HttpServletResponseFacade.setHeader" +
        	        " [{1}] in application [{2}]: Cannot set value for header: {3}." +
        	        " ERROR: {4}", new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(), HeaderNames.entity_header_content_length, LogContext.getExceptionStackTrace(nfe)}, context.getApplicationName(), context.getCsnComponent());
        	            
        }
      }
    } else if (headername.equalsIgnoreCase(HeaderNames.entity_header_content_type)) {
      setContentType(s1);
    } else {
      if (headername.equalsIgnoreCase(HeaderNames.response_header_set_cookie) &&
          ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getSystemCookiesDataProtection()) {
        String lowerCaseValue = s1.toLowerCase();
        if (lowerCaseValue.startsWith(CookieParser.jsessionid_cookie.toLowerCase()) ||
            lowerCaseValue.startsWith(CookieParser.app_cookie_prefix.toLowerCase())) {
          if (!lowerCaseValue.endsWith(CookieUtils.sHttpOnly.toLowerCase()) &&
              lowerCaseValue.indexOf(CookieUtils.sHttpOnly.toLowerCase() + ";") == -1) {
            s1 += CookieUtils.sHttpOnly;
          }
        }
      }
      
      if (headername.equalsIgnoreCase(HeaderNames.response_header_set_cookie) &&
          ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getSystemCookiesHTTPSProtection()) {
        String lowerCaseValue = s1.toLowerCase();
        if (lowerCaseValue.startsWith(CookieParser.jsessionid_cookie.toLowerCase()) ||
            lowerCaseValue.startsWith(CookieParser.app_cookie_prefix.toLowerCase())) {
          if (!lowerCaseValue.contains(CookieUtils.sSecure.toLowerCase())) {              
            s1 += CookieUtils.sSecure;
          }
        }
      }      
      headers.putHeader(StringUtils.filterHeaderName(headername), s1.getBytes());
    }
  }

  public void setIntHeader(String headername, int i) {
    if (traceLocation.bePath()) {
      trace("setIntHeader", "name = [" + headername + "], value = [" + i + "], isIncluded = [" + isIncluded + "]");
    }
    if (locked(false)) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000195",
          "Trying to set a header with method [setIntHeader] after the response is committed or in an included servlet. " +
          "The header will be ignored. " +
          "The header is: [{0} : {1}].", new Object[]{headername, i}, context.getApplicationName(), context.getCsnComponent());
      return;
    }
    if (headername.equalsIgnoreCase(new String(HeaderNames.entity_header_content_length))) {
      setContentLengthHeader(i);
    } else {
      headers.putIntHeader(StringUtils.filterHeaderName(headername), i);
    }
  }

  protected void setContentLengthHeader(int i) {
    headers.putIntHeader(HeaderNames.entity_header_content_length_, i);
    contentLength = i;
    //printwriter.setContentLength(i);
    //gzipprintwriter.setContentLength(i);
    //output.useChunking = false;
  }

  protected void setHeader(byte[] headername, byte[] s1) {
    if (traceLocation.bePath()) {
      String name = null;
      String value = null;
      if (headername != null) {
        name = new String(headername);
      }
      if (s1 != null) {
        value = new String(s1);
      }
      trace("setHeader", "name = [" + name + "], value = [" + value + "], isIncluded = [" + isIncluded + "]");
    }
    if (locked(true)) {
      return;
    }
    if (ByteArrayUtils.equalsIgnoreCase(headername, HeaderNames.entity_header_content_length_)) {
      if (s1 != null) {
        try {
          setContentLengthHeader(Integer.parseInt(new String(s1)));
        } catch (NumberFormatException nfe) {
          if (traceLocation.beError()) {
            LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceError("ASJ.web.000434", 
                "client [{0}] HttpServletResponseFacade.setHeader" +
                " [{1}] in application [{2}]: Cannot set value for header: {3}." +
                " ERROR: {4}", new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(), HeaderNames.entity_header_content_length, LogContext.getExceptionStackTrace(nfe)}, context.getApplicationName(), context.getCsnComponent());
            
          }
        }
      }
    } else {
      headers.putHeader(headername, s1);
    }
  }

  protected void setHeaderWhithoutCheckForLock(byte[] headername, byte[] s1) {
    if (traceLocation.bePath()) {
      String name = null;
      String value = null;
      if (headername != null) {
        name = new String(headername);
      }
      if (s1 != null) {
        value = new String(s1);
      }
      trace("setHeaderWhithoutCheckForLock", "name = [" + name + "], value = [" + value + "], isIncluded = [" + isIncluded + "]");
    }
    headers.putHeader(headername, s1);
  }
  protected void addHeader(byte[] headername, byte[] value) {
    if (traceLocation.bePath()) {
      String name = null;
      String valueStr = null;
      if (headername != null) {
        name = new String(headername);
      }
      if (value != null) {
        valueStr = new String(value);
      }
      trace("addHeader", "name = [" + name + "], value = [" + valueStr + "], isIncluded = [" + isIncluded + "]");
    }
    if (locked(true)) {
      return;
    }
    if (ByteArrayUtils.equalsIgnoreCase(headername, HeaderNames.entity_header_content_length_)) {
      if (value != null) {
        try {
          setContentLengthHeader(Integer.parseInt(new String(value)));
        } catch (NumberFormatException nfe) {
          if (traceLocation.beError()) {
            LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceError("ASJ.web.000435", 
                "client [{0}] HttpServletResponseFacade.addHeader" +
                " [{1}] in application [{2}]: Cannot set value for header: {3}." +
                " ERROR: {4}", new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(), HeaderNames.entity_header_content_length , LogContext.getExceptionStackTrace(nfe)},  context.getApplicationName(), context.getCsnComponent());
          }
        }
      }
    } else {
      headers.addHeader(headername, value);
    }
  }

  protected void removeHeader(String headername) {
    if (traceLocation.bePath()) {
      trace("removeHeader", "name = [" + headername + "]");
    }
    if (locked(true)) {
      return;
    }
    headers.removeHeader(headername);
  }

  public void addCookie(Cookie cookie) {
    if (traceLocation.bePath()) {
      if (cookie == null) {
        trace("addCookie", "cookie = [" + cookie + "], isIncluded = [" + isIncluded + "]");
      } else {
        //todo - cookie attributes
        trace("addCookie", "cookie name = [" + cookie.getName() + "], cookie value = [" + cookie.getValue() + "], isIncluded = [" + isIncluded + "]");
      }
    }
    if (locked(true)) {
      return;
    }
    HttpCookie newCookie = new HttpCookie(cookie.getName(), cookie.getValue());
    newCookie.setComment(cookie.getComment());
    newCookie.setDomain(cookie.getDomain());
    newCookie.setMaxAge(cookie.getMaxAge());
    newCookie.setPath(cookie.getPath());
    newCookie.setVersion(cookie.getVersion());
    MessageBytes cookieNameBytes = new MessageBytes(cookie.getName().getBytes());
    
    if (cookieNameBytes.equals(CookieParser.jsessionid_cookie) || cookie.getName().startsWith(CookieParser.app_cookie_prefix)) {
      if (ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getSystemCookiesHTTPSProtection()) {
        // if the cookie is system and the SystemCookiesHTTPSProtection is switch on, 
        // the Secure attribute is set alway, does not matter if the application is 
        // setting the Secure attribute  
        newCookie.setSecure(true);
      } else {
        // custom cookie is added; keep the Secure attribute as the application is setting it
        newCookie.setSecure(cookie.getSecure());
      }
    } else {
      // custom cookie is added; keep the Secure attribute as the application is setting it 
      newCookie.setSecure(cookie.getSecure());
    }
    
    
    if (!cookieNameBytes.equals(CookieParser.jsessionid_cookie) && !cookie.getName().startsWith(CookieParser.app_cookie_prefix)) {
      // custom cookie add via response interface, different from jsession and saplb cookies
      // do not set httpOnly attribute
      newCookie.setHttpOnly(false);      
    }
    addCookie(newCookie);
  }

  public void addCookie(HttpCookie cookie) {
    if (traceLocation.bePath()) {
      if (cookie == null) {
        trace("addCookie(internal)", "cookie = [" + cookie + "], isIncluded = [" + isIncluded + "]");
      } else {
        trace("addCookie(internal)", "cookie name = [" + cookie.getName() + "], cookie value = [" + cookie.getValue() + "], isIncluded = [" + isIncluded + "]");
      }
    }
    if (locked(true)) {
      return;
    }
    if (cookies == null) {
      cookies = new ArrayObject(4);
    }
    if (setSessionCookie) {
      cookies.add(cookies.size() - 1, cookie);
    } else {
      cookies.addElement(cookie);
    }
  }

  public void addSessionCookie(HttpCookie cookie) {
    if (traceLocation.bePath()) {
      if (cookie == null) {
        trace("addSessionCookie(internal)", "cookie = [" + cookie + "], isIncluded = [" + isIncluded + "]");
      } else {
        trace("addSessionCookie(internal)", "cookie name = [" + cookie.getName() + "], cookie value = [" + cookie.getValue() + "], cookie header = [" + 
            new String(CookieUtils.getCookieHeader(cookie))+ "], isIncluded = [" + isIncluded + "]");
      }
    }
    if (isIncluded) {
      //adding a Cookie response header must throw
      //an IllegalStateException if the response has been committed.
      if (isCommitted()) {
        if (traceLocation.beWarning()) {
          LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000540", 
              "client [{0}] HttpServletResponseFacade.addSessionCookie()" +
        	  " [{1}] in application [{2}]: {3}", new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName() , LogContext.getExceptionStackTrace(new Exception(WebIllegalStateException.GET_SESSION_METHOD_CALLED_ON_COMMITTED_STREAM))}, null, null);
        }
        throw new WebIllegalStateException(WebIllegalStateException.GET_SESSION_METHOD_CALLED_ON_COMMITTED_STREAM);
      }
    }
    if (cookies == null) {
      cookies = new ArrayObject(4);
    }
    if (setSessionCookie) {
      cookies.add(cookies.size() - 1, cookie);
    } else {
      cookies.addElement(cookie);
    }
  }

  public ArrayObject getCookies() {
    return cookies;
  }

  public String getCharacterEncoding() {
    if (characterEncoding == null) {
      byte[] contentTypeBytes = headers.getHeader(HeaderNames.entity_header_content_type_);

      if (contentTypeBytes != null) {
        //String contentType = new String(headers.getHeader(content_type));
        char[] contentTypeChars = new char[contentTypeBytes.length];

        for (int i = 0; i < contentTypeBytes.length; i++) {
          contentTypeChars[i] = (char) (contentTypeBytes[i] & 0x00ff);
        }

        String tempContentType = new String(contentTypeChars);
        int charsetLocation = ByteArrayUtils.indexOf(contentTypeBytes, Constants.charset);
        int semi = ByteArrayUtils.indexOf(contentTypeBytes, (byte) ';', charsetLocation);

        if (charsetLocation > -1) {
          if (semi > -1) {
            characterEncoding = tempContentType.substring(charsetLocation + 8, semi).trim();
          } else {
            characterEncoding = tempContentType.substring(charsetLocation + 8).trim();
          }
        }
      }

      if (characterEncoding == null) {
        if (traceLocation.beDebug()) {
          String contentTypeHeader = null;
          if (contentTypeBytes != null) {
            contentTypeHeader = new String(contentTypeBytes);
          }
          traceDebug("getCharacterEncoding", "contentTypeHeader = [" + contentTypeHeader + "], result: [" + Constants.DEFAULT_CHAR_ENCODING + "]");
        }
        return Constants.DEFAULT_CHAR_ENCODING;
      } else {
        if (traceLocation.beDebug()) {
          String contentTypeHeader = null;
          if (contentTypeBytes != null) {
            contentTypeHeader = new String(contentTypeBytes);
          }
          traceDebug("getCharacterEncoding", "contentTypeHeader = [" + contentTypeHeader + "], result: [" + characterEncoding + "]");
        }
        return characterEncoding;
      }
    } else {
      if (traceLocation.beDebug()) {
        traceDebug("getCharacterEncoding", "return cached value: [" + characterEncoding + "]");
      }
      return characterEncoding;
    }
  }

  /**
   * Returns the content type used for the MIME body sent in this response.
   * @return a String specifying the content type, for example, text/html;
   * charset=UTF-8, or null.
   *
   * @see javax.servlet.ServletResponse#getContentType()
   */
  public String getContentType() {
    String result = contentType;
    if (traceLocation.beDebug()) {
      traceDebug("getContentType", "Content-Type = [" + result + "]");
    }
    return result;
  }

  public Locale getLocale() {
    if (traceLocation.beDebug()) {
      traceDebug("getLocale", "locale = [" + locale + "]");
    }
    return locale;
  }


  /**
   * Sets the locale of the response, if the response has not been committed
   * yet. It also sets the response character encoding appropriately for the
   * locale,
   *
   * @param locale
   * the locale of the response.
   *
   * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
   */
  public void setLocale(Locale locale) {
    if (traceLocation.bePath()) {
      trace("setLocale", "locale = [" + locale + "], isCommitted = ["
        + isCommitted() + "], isIncluded = [" + isIncluded + "]");
    }
    if (locale == null) {
      if (traceLocation.beWarning()) {
        LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000541", 
    	    "client [{0}] HttpServletResponseFacade.setLocale" +
    	    " [{1}] in application [{2}]: Method has no effect. Locale is invalid = [{3}]", new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(), locale}, context.getApplicationName(), context.getCsnComponent());
      }
      return;
    }
    // SRV.5.4 Internationalization - The method can be called repeatedly;
    // but calls made after the response is committed have no effect.
    if (locked(false)) {
      // Severity info if suggested locale is equal with already set one
      // See CSN: 00003573782 2006 for more details
      String message = "Method has no effect. It is called after the "
        + "response has been committed! isCommitted = [" + isCommitted()
        + "], isIncluded = [" + isIncluded + "]";
      if (locale.equals(this.locale)) {
        traceInfo("setLocale", message);
      } else {
        LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000542", 
            "client [{0}] HttpServletResponseFacade.setLocale" +
            " [{1}] in application [{2}]: {3}", new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(), message}, context.getApplicationName(), context.getCsnComponent());
        
      }
      return;
    }
    this.locale = locale;
    // SRV.5.4 Internationalization - Calls to setLocale set the character
    // encoding only if neither setCharacterEncoding nor setContentType has
    // set the character encoding before.
    if (isCharacterEncodingSet) {
      return;
    }
    // Checks web.xml for locale mapping
    HashMapObjectObject localeMappings = context
      .getWebApplicationConfiguration().getLocaleMappings();
    String language = locale.getLanguage();
    String country = locale.getCountry();
    //Try a full name match (language and country)
    if (localeMappings != null){
      characterEncoding = (String)localeMappings.get(locale.toString());
      if (characterEncoding == null) {
        //Try to match just the language
        characterEncoding = (String)localeMappings.get(language);
      }
    }
    if ((language != null) && (language.length() > 0)) {
      if (characterEncoding == null) {
        //Check web container defined encoding mappings
        characterEncoding = getCharsetEncoding(locale);
      }

      //Set Content Type plus character encoding
      if ((country != null) && (country.length() > 0)) {
        language = language + "-" + country;
      }
      setHeader("Content-Language", language);

      //Character encoding is not set after setContentType(String) and
      //setCharacterEncoding(String)
      if ((getHeader(HeaderNames.entity_header_content_type) != null) &&
        (!isCharacterEncodingSet && (characterEncoding != null) && !getWriter)) {
          String temp = getHeader(HeaderNames.entity_header_content_type);
          int charsetLocation = temp.indexOf("charset=");
          int semi = temp.indexOf(';', charsetLocation);
          if (charsetLocation > -1) {
            if (semi > -1){
              String tail = temp.substring(semi);
              temp = temp.substring(0, charsetLocation + 8) + characterEncoding;
              temp += tail;
            } else {
              temp = temp.substring(0, charsetLocation + 8) + characterEncoding;
            }
          } else {
            temp = temp + ";charset=" + characterEncoding;
          }

          contentType = temp;
          char[] ct = contentType.toCharArray();
          byte[] ctBytes = new byte[ct.length];

          for (int i = 0; i < ct.length; i++) {
            ctBytes[i] = (byte) ct[i];
          }

          if (traceLocation.bePath()) {
            trace("setLocale", "set character encoding = [" + characterEncoding + "]");
          }

          setHeader(HeaderNames.entity_header_content_type_, ctBytes);
      } else {
        if (traceLocation.beWarning()) {
          String charset = getCharacterEncoding();
          String message = "Method has no effect on character encoding. "
            + "It is called after setContentType(String) or setCharacterEncoding(String) "
            + "or after getWriter(), " + "locale = [" + locale
            + "], isCharacterEncodingSet = [" + isCharacterEncodingSet
            + "], characterEncoding = [" + characterEncoding
            + "], getWriter = [" + getWriter + "]";
          // Severity info if suggested charset is equal with already set one
          // See CSN: 00003573782 2006 for more details
          if (charset != null && charset.equals(characterEncoding)) {
            traceInfo("setLocale", message);
          } else {
            LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000543", 
                "client [{0}] HttpServletResponseFacade.setLocale" +
                " [{1}] in application [{2}]: {3}", new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(), message}, context.getApplicationName(), context.getCsnComponent());            
          }
        }
      }
    }
  }

  public String encodeRedirectURL(String location) {
    return encodeURL(location);
  }

	/**
	 * @deprecated
	 */
  public String encodeRedirectUrl(String location) {
    return encodeRedirectURL(location);
  }

  public String encodeURL(String url) {
    String originalUrl = null;
    boolean beDebug = traceLocation.beDebug();
    if (beDebug && url != null) {
      originalUrl = new String(url);
    }
    boolean forceEncoding = false;
    if (url != null && url.startsWith(FORCE_ENCODING)) {
      forceEncoding = true;
      url = url.substring(FORCE_ENCODING.length());
    }
    int clientId = requestFacade.getHttpParameters().getRequest().getClientId();
    if (forceEncoding || context.getWebApplicationConfiguration().isURLSessionTracking()) {
      if (requestFacade.getSession(false) != null) {
        String result = WebParseUtils.encodeURL(url, requestFacade.getHttpParameters().getSessionRequest().getClientCookie(),
            true,
            context.getSessionServletContext().getApplicationCookieEncoded(getHttpParameters().getRequestPathMappings().getZoneName(),
						ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getZoneSeparator(),
                getHttpParameters().getRequest().getDispatcherId()),
            getServletContext().getSessionServletContext().getApplicationCookieName(),
            clientId);
        if (beDebug) {
          traceDebug("encodeURL", "url = [" + originalUrl + "], forceEncoding, url session tracking, has session, result: [" + result + "]");
        }
        return result;
      } else {
        String result = WebParseUtils.encodeURL(url, null, false,
            context.getSessionServletContext().getApplicationCookieEncoded(getHttpParameters().getRequestPathMappings().getZoneName(),
						ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getZoneSeparator(),
                getHttpParameters().getRequest().getDispatcherId()),
            getServletContext().getSessionServletContext().getApplicationCookieName(),
            clientId);
        if (beDebug) {
          traceDebug("encodeURL", "url = [" + originalUrl + "], forceEncoding, url session tracking, has not session, result: [" + result + "]");
        }
        return result;
      }
    }
    ArrayObject cookies = requestFacade.getHttpParameters().getRequest().getCookies(context.getWebApplicationConfiguration().isURLSessionTracking());
    if (cookies != null && cookies.size() > 0) {
      if (beDebug) {
        traceDebug("encodeURL", "url = [" + originalUrl + "], not url session tracking, has cookies, result: [" + url + "]");
      }
      return url;
    }
    if (requestFacade.isRequestedSessionIdFromCookie()) {
      if (beDebug) {
        traceDebug("encodeURL", "url = [" + originalUrl + "], not url session tracking, has cookies, result: [" + url + "]");
      }
      return url;
    }
    if (requestFacade.getRequestedSessionId() == null) {
      if (requestFacade.getSession(false) != null) {
        String result = WebParseUtils.encodeURL(url,
            //((ApplicationSession)requestFacade.getSession(false)).getIdInternal(),
            requestFacade.getHttpParameters().getSessionRequest().getClientCookie(),
            true,
            context.getSessionServletContext().getApplicationCookieEncoded(getHttpParameters().getRequestPathMappings().getZoneName(),
						ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getZoneSeparator(),
                getHttpParameters().getRequest().getDispatcherId()),
            getServletContext().getSessionServletContext().getApplicationCookieName(),
            clientId);
        if (beDebug) {
          traceDebug("encodeURL", "url = [" + originalUrl + "], not url session tracking, has not cookies, has session, result: [" + result + "]");
        }
        return result;
      } else {
        String result = WebParseUtils.encodeURL(url, null, false,
            "",
            getServletContext().getSessionServletContext().getApplicationCookieName(),
            clientId);
        if (beDebug) {
          traceDebug("encodeURL", "url = [" + originalUrl + "], not url session tracking, has not cookies, has not session, result: [" + result + "]");
        }
        return result;
      }
    } else {
      String result = WebParseUtils.encodeURL(url, requestFacade.getRequestedSessionId(), true,
            context.getSessionServletContext().getApplicationCookieEncoded(getHttpParameters().getRequestPathMappings().getZoneName(),
						ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getZoneSeparator(),
                getHttpParameters().getRequest().getDispatcherId()),
          	getServletContext().getSessionServletContext().getApplicationCookieName(),
          	clientId);
      if (beDebug) {
        traceDebug("encodeURL", "url = [" + originalUrl + "], not url session tracking, has not cookies, cookies in url, result: [" + result + "]");
      }
      return result;
    }
  }

  public void setSessionCookie() {
    setSessionCookie = true;
  }

  protected boolean isSessionCookieSet() {
    return setSessionCookie;
  }

  /**
   * Converts alias in well-formated form, i.e. fixes (back-)slashes, canonicalizes path
   * @param toConvert
   * @return
   */
  private String convertAlias(String toConvert) {
    if (toConvert == null) {
      return null;
    }
    toConvert = toConvert.trim();
    toConvert = ParseUtils.convertAlias(toConvert);
    toConvert = ParseUtils.canonicalize(toConvert);
    if (toConvert.startsWith(ParseUtils.separator)) {
      toConvert = toConvert.substring(1);
    }
    if (toConvert.endsWith(ParseUtils.separator)) {
      toConvert = toConvert.substring(0, toConvert.length() - 2);
    }
    return toConvert;
  }


  public String addLoadBalancingCookie(String logonGroup, String alias, int serverId) {
    // Note: Those parameters come from application logic so they should be safely escaped against
    // malicious code. Cookie values will be escaped against response splitting (removed  CR and LF)
    // due to the general protection via addHeader(). This method is used also for Set-Cookie
    final String methodName = "addLoadBalancingCookie";
    if (traceLocation.bePath()) {
      tracePath(methodName, "logonGroup = [" + logonGroup + "], alias = [" + alias + "], serverId = [" + serverId + "]");
    }

    SessionServletContext sessionServletContext = context.getSessionServletContext();
    
    String newSessionID = sessionServletContext.generateJSessionIdValue();
    if (traceLocation.bePath()) {
      tracePath(methodName, "new ID = [" + newSessionID + "]");
    }
    // TODO - check if serverID is from the same instance
    // String groupName_Prefix = ServiceContext.getServiceContext().getClusterContext().getInstanceName(serverId);
    // if (!serverID.startsWith(groupName_Prefix)) - throw IllegalArgumentException()
    try {
      sessionServletContext.getSession().createSessionOnNode(Integer.toString(serverId), SessionServletContext.getSessionIdFromJSession(newSessionID));
    } catch (SessionException e) {
      if (traceLocation.beError()) {
 	    LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceError("ASJ.web.000436", 
 		    "client [{0}] HttpServletResponseFacade.{1}" +
 			" [{2}] in application [{3}]: Error preparing session ({4}) to remote server ({5})." +
 			" ERROR: {6}", new Object[]{ getTraceClientId(), methodName, getObjectInstance(), getTraceAliasName(), newSessionID, serverId, LogContext.getExceptionStackTrace(e)}, null, null);
      }
    }

    // old code
    if (alias == null) {
      alias = "";
    }
    alias = convertAlias(alias);
    if (logonGroup == null) {
      //TODO no zones but logon groups
      //logonGroup = ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getLogonTable().getGroup(alias);
      //if (logonGroup == null) {
        logonGroup = "*";
      //}
    }

    logonGroup = convertAlias(logonGroup);
    logonGroup.replace('/', '*');
    logonGroup.replace('\\', '*');
    String instanceName = ServiceContext.getServiceContext().getInstanceName();
    if (instanceName == null) {
      instanceName = "J2EE" + getHttpParameters().getRequest().getDispatcherId();
    }
    String cookieValue = "(" + instanceName + ")" + serverId;
    //add encodeRedirectURL(location)

    //TODO no zones but logon groups
//    if (alias.equalsIgnoreCase(context.getAliasName())
//            || logonGroup.equalsIgnoreCase(ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getLogonTable().getGroup(context.getAliasName()))) {
    if (alias.equalsIgnoreCase(context.getAliasName())) {
      //in order to generate the lb cookies for this request and override them if needed
      getServletRequest().getSession();
    }
    // TODO change
    HttpCookie lbCookie = CookieParser.createCookie(CookieParser.app_cookie_prefix + logonGroup, cookieValue
            , getHttpParameters().getRequest().getHost(), context.getWebApplicationConfiguration().getApplicationCookieConfig());
    if (alias.equals("")) {
      lbCookie.setPath("/");
    } else {
      lbCookie.setPath("/" + alias + "/");
    }
    removeLBCookie(lbCookie.getName(), lbCookie.getPath());
    addCookie(lbCookie);
    //add default lb cookie
    if (logonGroup.equals("*")) {
      // TODO - should return;
    }
    HttpCookie defaultLbCookie = CookieParser.createCookie(CookieParser.app_cookie_prefix + "*", cookieValue
            , getHttpParameters().getRequest().getHost(), context.getWebApplicationConfiguration().getApplicationCookieConfig());
    if (alias.equals("")) {
      defaultLbCookie.setPath("/");
    } else {
      defaultLbCookie.setPath("/" + alias + "/");
    }
    //TODO should we remove it
    removeLBCookie(defaultLbCookie.getName(), defaultLbCookie.getPath());
    addCookie(defaultLbCookie);


    // TODO check for jsessionid
    // TODO !!! Is needed mapping for already redirected client id1(server1) -> id2(server2)
    //if (!context.getWebApplicationConfiguration().isURLSessionTracking()) {
    //  if (!getHttpParameters().isSetSessionCookie()) { // TODO - check if there is possibility not to be created session on server1 and we should create
    String hostStr = getHttpParameters().getRequest().getHost();
    HttpCookie jsessionidCookie = CookieParser.createSessionCookie(newSessionID, hostStr,
            new WebCookieConfig(alias, WebCookieConfig.COOKIE_TYPE_APPLICATION));
        if (alias.equals("")) {
          jsessionidCookie.setPath("/");
        } else {
          jsessionidCookie.setPath("/" + alias + "/");
        }
        addCookie(jsessionidCookie);
//        response.setSessionCookie();
//        httpParameters.setSessionCookie(true);
//      }
//    }
    return newSessionID;
  } // addLoadBalancingCookie(String logonGroup, String alias, int serverId)

  public String addLoadBalancingCookie(String logonGroup, int serverId) {
    if (logonGroup == null) {
      logonGroup = "*";
    }
    logonGroup = convertAlias(logonGroup);
    logonGroup.replace('/', '*');
    logonGroup.replace('\\', '*');
    String cookieValue = "" + serverId;
    String instanceName = ServiceContext.getServiceContext().getClusterContext().getInstanceName(serverId);
    if (instanceName != null) {
      cookieValue = "(" + instanceName + ")" + cookieValue;
    }
    HttpCookie lbCookie = CookieParser.createCookie(CookieParser.app_cookie_prefix + logonGroup, cookieValue
            , getHttpParameters().getRequest().getHost(), context.getWebApplicationConfiguration().getApplicationCookieConfig());
    addCookie(lbCookie);
    return null; // todo - return the new jsessionid
  }

  private void removeLBCookie(String name, String path) {
    if (cookies == null) {
      return;
    }
    Object[] allCookies = cookies.toArray();
    for (int i = 0; allCookies != null && i < allCookies.length; i++) {
      HttpCookie nextCookie = (HttpCookie)allCookies[i];
      if (nextCookie.getName().equals(name)
              && nextCookie.getPath().equals(path)) {
        cookies.remove(nextCookie);
      }
    }
  }

  public String addLoadBalancingCookie(String logonGroup, String alias) {
    return addLoadBalancingCookie(logonGroup, alias, ServiceContext.getServiceContext().getClusterContext().getCurrentClusterId());
  }

  public String addLoadBalancingCookie(String logonGroup) {
    return addLoadBalancingCookie(logonGroup, ServiceContext.getServiceContext().getClusterContext().getCurrentClusterId());
  }

  public Cookie getMyLoadBalancingCookie() {
    String cookieValue = null;
    if ("sapj2ee_".equals(ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getLoadBalancingCookiePrefix())) {
      cookieValue = "" + ServiceContext.getServiceContext().getClusterContext().getCurrentClusterId();
    } else {
      String instanceId = ServiceContext.getServiceContext().getInstanceName();
      if (instanceId == null) {
        instanceId = "J2EE" + getHttpParameters().getRequest().getDispatcherId();
      }
      cookieValue = "(" + instanceId + ")" + ServiceContext.getServiceContext().getClusterContext().getCurrentClusterId();
    }
    String aplicationCookieNameZoneEncoded = getServletContext().getSessionServletContext().getApplicationCookieName();
    if (getHttpParameters().getRequestPathMappings().getZoneName() != null
            && getHttpParameters().getRequestPathMappings().getZoneName().length() != 0) {
      //aplicationCookieNameZoneEncoded = aplicationCookieNameZoneEncoded + httpParameters.getHttpProperties().getZoneSeparator() + httpParameters.getRequestPathMappings().getZoneName();
      aplicationCookieNameZoneEncoded = CookieParser.app_cookie_prefix + getHttpParameters().getRequestPathMappings().getZoneName();
    }
    HttpCookie httpCookie = CookieParser.createCookie(aplicationCookieNameZoneEncoded, cookieValue,
            getHttpParameters().getRequest().getHost(),
            getServletContext().getWebApplicationConfiguration().getApplicationCookieConfig());
    Cookie cookie = new Cookie(httpCookie.getName(), httpCookie.getValue());
    if (httpCookie.getComment() != null) {
      cookie.setComment(httpCookie.getComment());
    }
    if (httpCookie.getDomain() != null) {
      cookie.setDomain(httpCookie.getDomain());
    }
    cookie.setMaxAge(httpCookie.getMaxAge());
    if (httpCookie.getPath() != null) {
      cookie.setPath(httpCookie.getPath());
    }
    cookie.setSecure(httpCookie.getSecure());
    cookie.setVersion(httpCookie.getVersion());
    return cookie;
  }

  // ------------------------ INTERNAL ------------------------

  /**
   * Returns true if writing or headers modifying is not allowed.
   * It is possible in include, response committed and so on.
   * @return
   * todo - check in spec when exception must be thrown and when not!
   */
  protected boolean locked(boolean throwError) {
    if (isIncluded) {
      return true;
    }
    if (isCommitted()) {
      if (throwError) {
        throw new WebIllegalStateException(WebIllegalStateException.Stream_is_already_commited);
      } else {
        return true;
      }
    }
    return isIncluded;
  }

  public boolean isIncluded() {
    return isIncluded;
  }

  public void setIncluded(boolean isIncluded) {
    this.isIncluded = isIncluded;
  }

//  /**
//   * Checks whether the setStatus method is disabled and ignored if called
//   * as by servlet 2.4 SRV.9.9 Error Handling/Request Attributes.
//   */
//  //TODO: Reverted in Servlet 2.5
//  public boolean isStatusLocked() {
//    return statusLocked;
//  }

//  /**
//   * Disables or enables the setStatus method.
//   */
//  //TODO: Reverted in Servlet 2.5
//  public void setStatusLocked(boolean statusLocked) {
//    this.statusLocked = statusLocked;
//  }

  protected void resetInternal(boolean initial) throws IOException {
    cookies = null;
    headers = null;
    characterEncoding = null;
    contentType = null;
    context = null;
    requestFacade = null;
    setSessionCookie = false;
    status = ResponseCodes.code_ok;
    statusSrc = null;
    committed = false;
    isIncluded = false;
    locale = Locale.getDefault();
  }

  protected void reset(boolean keepHeaders) {
    if (!keepHeaders) {
      headers.clear();
//TODO: check
      contentType = null;
      characterEncoding = null;
      isCharacterEncodingSet = false;
    }
    setSessionCookie = false;
    status = ResponseCodes.code_ok;
    statusSrc = null;
  }

  protected void commit(boolean useChunking) throws IOException {
    if (traceLocation.bePath()) {
      trace("commit", "commit");
    }
    if (getHttpParameters().getRequest().getRequestLine().isSimpleRequest()) {
      if (traceLocation.bePath()) {
        trace("commit", "isSimpleRequest");
      }
      committed = true;
      return;
    }

    addRemoveHeadersBeforeCommit(useChunking);

    writeStatusLine();
    if (SessionRequest.isCheckMarkIdEnabled()) {
      Cookie[] cookies = requestFacade.getCookies();
      if (cookies != null) {
        String markID = requestFacade.getHttpParameters().getSessionRequest().getMarkId();
        boolean found = false;
        boolean foundCorrectMarID = false;
        for (int i = 0; i < cookies.length; i++) {
          if (CookieParser.jsession_mark_cookie.equals(cookies[i].getName())){
            found = true;
            if (cookies[i].getValue().equals(markID)) {
              if (traceLocation.beInfo()) {
                traceInfo("commit", "JSESSIONMARKID found : "+cookies[i].getValue());
              }
              foundCorrectMarID = true;
              break;
            }
          }
        }
        if(!foundCorrectMarID) {
          if (traceLocation.beInfo()) {
                traceInfo("commit", "JSESSIONMARKID not found in request, markID form sessionmanagement = " + markID);
          }
          //if there is header set-cookie with new JsessionID
          if (!isJsessionIDSetCookieAdded(markID)) {
            markID = null;
          }
          if (markID != null) {
             setMarkIDCookie(markID);
          } else if (found){
             clearMarkIDCookie();
          }
        }
      } else if (requestFacade.getHttpParameters().getSessionRequest().getMarkId() != null){
        //in case of request without cookies but containing credentials and authentication is succesfull
        //we need to send generated markid
        //TODO check for setCookie header for jsessionid is redundant?
        if (traceLocation.beInfo()) {
          traceInfo("commit", "No cookies found in the request");
        }
        setMarkIDCookie(requestFacade.getHttpParameters().getSessionRequest().getMarkId());        
      }
    }

    byte[] cookies = null;
    if (getCookies() != null) {
      if (this.headers.containsHeader(HeaderNames.response_header_set_cookie)) {
        SnapShotEnumeration en = getCookies().elementsEnumeration();
        HttpCookie httpCookie = null;
        while(en.hasNext()) {
          httpCookie = (HttpCookie) en.next();
          if (httpCookie.getName().startsWith(CookieParser.app_cookie_prefix)) {
            break;
          } else {
            httpCookie = null;
          }
        }
        // removes header with found app cookie to prevent duplication
        if (httpCookie != null) {
          String headerValue = null;
          try {
            headerValue = new String(CookieUtils.getCookieHeaderValue(httpCookie), "UTF-8");
          } catch (UnsupportedEncodingException uee) {
            headerValue = new String(CookieUtils.getCookieHeaderValue(httpCookie));
          }
          this.headers.removeHeader(HeaderNames.response_header_set_cookie, headerValue);
        }
      }
      cookies = CookieUtils.getAllCookieHeaders(getCookies());
    }
    writeHeaders(cookies);
    committed = true;
    return;
  }

  protected void setConnectionType(boolean useChunking) {
    String connectionHeader = getHeader(HeaderNames.hop_header_connection);
    if (connectionHeader != null && connectionHeader.equalsIgnoreCase(HeaderValues.close)) {
      getHttpParameters().getResponse().setPersistentConnection(false);
    } else if (!useChunking && !containsHeader(HeaderNames.entity_header_content_length)) {
      getHttpParameters().getResponse().setPersistentConnection(false);
    }
  }

  private static String getCharsetEncoding(Locale locale) {
    String temp = EncodingUtils.getEncoding(locale.toString());
    if (temp == null) {
      return EncodingUtils.getEncoding(locale.getLanguage());
    } else {
      return temp;
    }
  }

  private void writeStatusLine() throws IOException {
    getHttpParameters().getResponse().setResponseCode(status);
    if (ResponseCodes.status_code_byte[status] == null) {
      ResponseCodes.status_code_byte[status] = (" " + status).getBytes();
    }
    if (statusSrc == null) {
      statusSrc = ResponseCodes.reason(status, getServletContext().getAliasName());
    }
    byte[] result = new byte[HTTP_11.length + ResponseCodes.status_code_byte[status].length + 1 + statusSrc.length() + 2];
    int ptr = 0;
    System.arraycopy(HTTP_11, 0, result, ptr, HTTP_11.length);
    ptr += HTTP_11.length;
    System.arraycopy(ResponseCodes.status_code_byte[status], 0, result, ptr, ResponseCodes.status_code_byte[status].length);
    ptr += ResponseCodes.status_code_byte[status].length;
    result[ptr++] = SP;
    char[] pic = statusSrc.toCharArray();
    for (int i = 0; i < pic.length; i++) {
      result[ptr++] = (byte) pic[i];
    }
    result[ptr++] = R;
    result[ptr++] = N;
    sendToClient(result);
  }

  private void writeHeaders(byte[] cookiesArr) throws IOException {
    // 50 => in order to prevent resizing
    ByteArrayOutputStream baos = new ByteArrayOutputStream(headers.size() * 50 + (cookiesArr == null ? 0 : cookiesArr.length));
    headers.write(baos);
    if (cookiesArr != null) {
      baos.write(cookiesArr);
    }
    baos.write(RN);
    sendToClient(baos.toByteArray());
  }

  protected void trace(String method, String msg) {
    LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).trace(
        "client [" + getTraceClientId() + "] HttpServletResponseFacade." + method +
        " [" + getObjectInstance() + "] in application [" + getTraceAliasName() + "]: " + msg, getTraceAliasName());
  }
  
//TODO:Polly internal trace method
  protected void traceError(String msgId, String method, String msg, Throwable t, String dcName, String csnComponent) {
    LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceError(msgId, 
        "client [" + getTraceClientId() + "] HttpServletResponseFacade." + method +
        " [" + getObjectInstance() + "] in application [" + getTraceAliasName() + "]: " + msg +
        ". ERROR: " + LogContext.getExceptionStackTrace(t), dcName, csnComponent);
  }

  protected void traceWarning(String msgId, String method, String msg, boolean logTrace, String dcName, String csnComponent) {
    if (logTrace) {
      msg = LogContext.getExceptionStackTrace(new Exception(msg));
    }
    LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning(msgId, 
        "client [" + getTraceClientId() + "] HttpServletResponseFacade." + method +
        " [" + getObjectInstance() + "] in application [" + getTraceAliasName() + "]: " + msg, dcName, csnComponent);
  }

  protected void traceInfo(String method, String msg) {
    traceLocation.infoT("client [" + getTraceClientId() + "] HttpServletResponseFacade." + method +
        " [" + getObjectInstance() + "] in application [" + getTraceAliasName() + "]: " + msg);
  }

  protected void tracePath(String method, String msg) {
    traceLocation.pathT("client [" + getTraceClientId() + "] HttpServletResponseFacade." + method +
        " [" + getObjectInstance() + "] in application [" + getTraceAliasName() + "]: " + msg);
  }

  protected void traceDebug(String method, String msg) {
    traceLocation.debugT("client [" + getTraceClientId() + "] HttpServletResponseFacade." + method +
        " [" + getObjectInstance() + "] in application [" + getTraceAliasName() + "]: " + msg);
  }

  protected void traceDebug(String method, String msg, Throwable ex) {
    traceLocation.debugT("client [" + getTraceClientId() + "] HttpServletResponseFacade." + method +
        " [" + getObjectInstance() + "] in application [" + getTraceAliasName() + "]: " + msg
        + "The error is:\r\n" + LogContext.getExceptionStackTrace(ex));
  }

  protected String getObjectInstance() {
    String instance = super.toString();
    return instance.substring(instance.indexOf('@') + 1);
  }

  /**
   * If this object is reset, empty string will be returned;
   * ow a message containing the unset fields will be returned.
   * @return
   */
  public String checkReset() {
  	String result = "";
  	if (cookies != null) { //resetInternal
  		result += "cookies=[" + cookies + "] are not reset to null;";
  	}
  	if (headers != null && headers.size() > 0) {
  		result += "headers.size=[" + headers.size() + "], headers=[";
  		for (Enumeration e = headers.names(); e.hasMoreElements(); ) {
  			result+= e.nextElement() + ",";
  		}
  		result += "] are not cleared;";
  	}
  	if (characterEncoding != null) { //resetInternal
  		result += "characterEncoding=[" + characterEncoding + "] is not reset to null;";
  	}
  	if (contentType != null) { //resetInternal
  		result += "contentType=[" + contentType + "] is not reset to null;";
  	}
  	if (context != null) { //resetInternal
  		result += "context=[" + context + "] is not reset to null;";
  	}
  	if (requestFacade != null) { //resetInternal
  		result += "requestFacade=[" + requestFacade + "] is not reset to null;";
  	}
  	if (setSessionCookie) { //resetInternal
  		result += "setSessionCookie=[" + setSessionCookie + "] is not reset to false;";
  	}
  	if (status != ResponseCodes.code_ok) {
  		result += "status=[" + status + "] is not reset to ResponseCodes.code_ok;";
  	}
  	if (statusSrc != null) {
  		result += "statusSrc=[" + statusSrc + "] is not reset to null;";
  	}
  	if (committed) {
  		result += "committed=[" + committed + "] is not reset to null;";
  	}
  	if (isIncluded) {
  		result += "isIncluded=[" + isIncluded + "] is not reset to null;";
  	}
  	if (locale != Locale.getDefault()) {
  		result += "locale=[" + locale + "] is not reset to default=[" + Locale.getDefault() + "];";
  	}
  	if (isCharacterEncodingSet) {
  		result += "isCharacterEncodingSet=[" + isCharacterEncodingSet + "] is not reset to false.";
  	}
  	return result;
  }


  /**
   * Returns the client id if the requestFacade is not null, otherwise: "<NA>".
   * For tracing purposes.
   * @return
   */
  protected String getTraceClientId() {
    String clientId;
    if (requestFacade != null && requestFacade.getHttpParameters() != null && requestFacade.getHttpParameters().getRequest() != null) {
      clientId = String.valueOf(requestFacade.getHttpParameters().getRequest().getClientId());
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
  protected String getTraceAliasName() {
    if (context != null) {
      return context.getAliasName();
    } else {
      return "<NA>";
    }
  }

  private void addRemoveHeadersBeforeCommit(boolean useChunking){
    WebContainerProperties webContainerProperties = ServiceContext.getServiceContext().getWebContainerProperties();
    setDateHeader(HeaderNames.entity_header_date);
    if (status == ResponseCodes.code_not_found) {
      setHeader(HeaderNames.propriatory_sap_isc_etag_, HeaderValues.getSapIscEtag(getServletContext().getAliasName()));
    }
    if (useChunking) {
      // if we decided to write chunked response, we should ensure that TransferEncoding is preset
      // Siemens DSA application has a servlet which includes some JSPs. The result is "chunked" response without appropriate header.
      setHeaderWhithoutCheckForLock(HeaderNames.hop_header_transfer_encoding_, HeaderValues.chunked_);
    } else if (!containsHeader(HeaderNames.entity_header_content_length)) {
      setHeader(HeaderNames.hop_header_connection_, HeaderValues.close_);
    }

    if (getHeader(HeaderNames.hop_header_connection_) == null) {
      if (getHttpParameters().getRequest().getRequestLine().getHttpMajorVersion() == 0
              || getHttpParameters().getRequest().getRequestLine().getHttpMajorVersion() == 1
                && getHttpParameters().getRequest().getRequestLine().getHttpMinorVersion() == 0) {
        setHeader(HeaderNames.hop_header_connection_, HeaderValues.keep_alive_);
      }
    }

    if (webContainerProperties.headerForNoCompression() != null
          && webContainerProperties.headerForNoCompression().length() != 0) {
      removeHeader(webContainerProperties.headerForNoCompression());
    }
    if (webContainerProperties.headerForCompression() != null
          && webContainerProperties.headerForCompression().length() != 0) {
      removeHeader(webContainerProperties.headerForCompression());
    }
    //if the value disableDynamicResponseCaching is true and application didn't specify caching constraints
    // "Cache-control: no-cache" and "Pragma: no-cache" are added to response headers.
    if( webContainerProperties.isDisableDynamicResponseCaching() &&
        !containsHeader(HeaderNames.entity_header_cache_control) &&
        !containsHeader(HeaderNames.entity_header_pragma) &&
        !containsHeader(HeaderNames.entity_header_expires) ){
      		setHeader(HeaderNames.entity_header_cache_control_, HeaderValues.no_cache_);
      		setHeader(HeaderNames.entity_header_pragma_, HeaderValues.no_cache_);
    }
    //The property is not defined in webcontainers_properties.xml file but the logic is working with the default value "true"
    if( ( status == 304 || status == 204 || status < 200) ){
      removeHeader(HeaderNames.entity_header_content_type);
	    if( webContainerProperties.isRemoveContentLengthFrom304() ){
	      removeHeader(HeaderNames.entity_header_content_length);
	    }
    }
  }

  /**
   * Adds an '-gzip' extension to each value of ETag headers;
   *
   * ETag header syntax (HTTP RFC): ETag = "ETag" ":" entity-tag
   * ex.: ETag: "xyzzy"
   *      ETag: W/"xyzzy"
   *      ETag: ""
   *
   * entity-tag = [ weak ] opaque-tag
   *   weak       = "W/"
   *   opaque-tag = quoted-string
   */
  private void adjustETagHeader() {
    String etagValue = headers.getHeader(HeaderNames.response_header_etag);
    if (etagValue != null) {
      if (etagValue.endsWith("\"")) {
        //TODO: what about "" value
        etagValue = etagValue.substring(0, etagValue.length() - 1);
        etagValue = etagValue + "-gzip\"";
      } else {
        etagValue = etagValue + "-gzip";
      }
      headers.putHeader(HeaderNames.response_header_etag_, etagValue.getBytes());
    }
  }

  private void sendToClient(byte[] result) throws IOException {
    getHttpParameters().getResponse().sendResponse(result, 0, result.length);
  }

  public void clearMarkIDCookie() {
    if (traceLocation.beInfo()) {
      traceInfo("clearMarkIDCookie()", "cookies = [" + cookies + "]");
    }
    if (cookies == null) {
      //todo add empty MARKID cookie in response
      setMarkIDCookie("");
      return;
    }
    Object[] respCookies = cookies.toArray();
    boolean found = false;
    for (int i = 0; i < respCookies.length; i++) {
      HttpCookie httpCookie = (HttpCookie) respCookies[i];
      if (httpCookie.getName().equals(CookieParser.jsession_mark_cookie)) {
        //RFC2109 if the Set-
        //Cookie has a value for Max-Age of zero, the (old and new) cookie is
        //discarded.
        httpCookie.setMaxAge(0);
        found = true;
        if (traceLocation.beInfo()) {
          traceInfo("clearMarkIDCookie()", "cookie found in response name = [" + httpCookie.getName() + "], cookie value = [" + httpCookie.getValue() + "], MaxAge set to 0");
        }
      }
    }
    if (!found) {
      setMarkIDCookie("");
    }
  }

  private void setMarkIDCookie(String markID) {
    HttpCookie cok = CookieParser.createMarkIDCookie(markID,
      getServletRequest().getServerName(), getServletContext().getWebApplicationConfiguration().getSessionCookieConfig());
    if("".equals(markID)) {
      cok.setMaxAge(0);
    }
    if (traceLocation.beInfo()) {
      if (cok == null) {
        traceInfo("setMarkIDCookie()", "cookie = [" + cok + "], isIncluded = [" + isIncluded + "]");
      } else {
        traceInfo("setMarkIDCookie()", "cookie name = [" + cok.getName() + "], value = [" + cok.getValue() + "], domain = [" + cok.getDomain() + "], path = [" + cok.getPath() +"], maxAge = [" + cok.getMaxAge() + "]");
      }
    }
    if (cookies == null) {
      cookies = new ArrayObject(4);
    }
    cookies.addElement(cok);
  }

  private boolean isJsessionIDSetCookieAdded(String markID) {
    if (this.headers.containsHeader(HeaderNames.response_header_set_cookie)) {
      String[] setCook = this.headers.getHeaders(HeaderNames.response_header_set_cookie);
      for (int i = 0; i < setCook.length; i++) {
        if (setCook[i].startsWith(CookieParser.jsessionid_cookie)) {
          if (traceLocation.beInfo()) {
            traceInfo("isJsessionIDSetCookieAdded", "JSESSIONID set-cookie found! ["+setCook[i]+"]");
          }
          String csmID = requestFacade.getHttpParameters().getSessionRequest().getSessionId();
          if (csmID != null && setCook[i].indexOf(csmID) > -1) {
            if (traceLocation.beInfo()) {
              traceInfo("isJsessionIDSetCookieAdded", "JSESSIONID set-cookie value is the same like in CSM!");
            }
            return true;
          } else {
            if (traceLocation.beInfo()) {
              traceInfo("isJsessionIDSetCookieAdded", "JSESSIONID set-cookie value is NOT the same in CSM! csmID=" + csmID);
            }
            return false;
          }
        }
      }
    }
    if(markID != null) {
      if (traceLocation.beInfo()) {
        traceInfo("isJsessionIDSetCookieAdded", "JSESSIONID set-cookie NOT FOUND, but there is MarkID");
      }
      return true;
    }
    if (traceLocation.beInfo()) {
      traceInfo("isJsessionIDSetCookieAdded", "JSESSIONID set-cookie NOT FOUND!");
    }
    return false;
  }
}
