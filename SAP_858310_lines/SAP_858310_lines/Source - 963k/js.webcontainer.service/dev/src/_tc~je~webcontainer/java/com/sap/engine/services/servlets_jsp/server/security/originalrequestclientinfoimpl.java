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
package com.sap.engine.services.servlets_jsp.server.security;

import com.sap.engine.services.servlets_jsp.security.HttpRequestClientInfo;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.application.SessionServletContext;
import com.sap.engine.services.httpserver.lib.headers.MimeHeaders;
import com.sap.engine.services.httpserver.lib.protocol.HeaderNames;
import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.lib.security.Base64;
import com.sap.engine.lib.security.http.DigestCredentials;
import com.sap.tc.logging.Location;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.cert.X509Certificate;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

public class OriginalRequestClientInfoImpl implements HttpRequestClientInfo {
  private static Location currentLocation = Location.getLocation(OriginalRequestClientInfoImpl.class);
  private SessionServletContext application = null;
  private X509Certificate[] certificates = null;
  private Cookie[] cookies = null;
  private HashMapObjectObject headers = new HashMapObjectObject();
  private HashMapObjectObject parameters = new HashMapObjectObject();
  private String clientIP = null;
  private String method = null;
  private boolean isSecure = false;
  private HttpSession httpSession = null;

  private HttpRequestClientInfoImpl newRequest = null;
  private char[] newPassword = null;
  private char[] password = null;
  private boolean isSetPassword = false;

  public OriginalRequestClientInfoImpl(HttpRequestClientInfoImpl originalRequest, SessionServletContext application) {
    this.application = application;
    this.certificates = originalRequest.getCertificateChain();
    this.cookies = originalRequest.getAllCookies();
    MimeHeaders mimeHeaders = originalRequest.getAllHeaders();
    int headersCount = mimeHeaders.size();
    for (int i = 0; i < headersCount; i++) {
      //todo - headers with the same names but different values!
      headers.put(mimeHeaders.getHeaderName(i), mimeHeaders.getHeader(i));
    }
    this.parameters = originalRequest.getAllParameters();
    this.clientIP = originalRequest.getClientIp();
    this.method = originalRequest.getMethod();
    this.isSecure = originalRequest.isSecure();
    this.httpSession = originalRequest.getHttpSession();
  }

  public void setNewRequest(HttpRequestClientInfoImpl newRequest) {
    this.newRequest = newRequest;
  }

  /**
   * Gets a client certificate chain used for ssl connection.
   */
  public X509Certificate[] getCertificateChain() {
    return certificates;
  }

  /**
   * Require the client to open a ssl connection if hasn't and starts hadnshake to retrieve its certificate.
   */
  public void requireCertificateChain() {
    newRequest.requireCertificateChain();
  }

  /**
   * Gets cookie with the specified name from http request.
   */
  public String getHttpCookie(String name) {
    if (cookies == null) {
      return null;
    }
    for (int i = 0; i < cookies.length; i++) {
      if (cookies[i].getName().equals(name)) {
        return cookies[i].getValue();
      }
    }
    return null;
  }

  /**
   * Gets header with the specified name from http request.
   */
  public String getHttpHeader(String name) {
    return (String)headers.get(name);
  }

  /**
   * Gets parameter with the specified name from http request.
   */
  public String[] getHttpParameterValues(String name) {
    return (String[])parameters.get(name);
  }

  public void removeRequestHeader(String name, String value) {
    headers.remove(name);
  }

  /**
   * Retrives the user password from http request. The password is searched depending on the authentication method.
   */
  public char[] getPassword() {
    if (isSetPassword) {
      return password;
    }
    if (HttpServletRequest.BASIC_AUTH.equalsIgnoreCase(application.getAuthType())) {
    	String authHeaderValue = "";
      try {
        authHeaderValue = decode(getHttpHeader(HeaderNames.request_header_authorization));
        return getPasswordBasic(authHeaderValue);
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
    	  //TODO:Polly ok
          logWarning("ASJ.web.000228", "Cannot retrieve user credentials out of authorization request header.", e);
          if (LogContext.getLocationSecurity().beWarning()) {
          	  LogContext.getLocation(LogContext.LOCATION_SECURITY).traceWarning("ASJ.web.000562", 
          	    "Cannot retrieve user credentials out of authorization request header for web application [{0}]. " +
          	    "Incorrect header value [{1}]found.", new Object[]{application.getAliasName(), authHeaderValue},e, null, null);

        }
        return null;
      }
    } else if (HttpServletRequest.FORM_AUTH.equalsIgnoreCase(application.getAuthType())) {
      return getParameter(j_password).toCharArray();
    } else {
      return null;
    }
  }

  public void setNewPassword(char[] newPassword) {
    this.newPassword = newPassword;
  }

  public void setPassword(char[] password) {
    this.password = password;
    isSetPassword = true;
  }

  /**
   * Retrives the user new password, that will replace the old one.
   */
  public char[] getNewPassword() {
    return newPassword;
  }

  /**
   * Retrives the user name from http request. The user name is searched depending on the authentication method.
   */
  public String getUserName() {
    if (HttpServletRequest.BASIC_AUTH.equalsIgnoreCase(application.getAuthType())) {
    	String authHeaderValue = "";
      try {
        authHeaderValue = decode(getHttpHeader(HeaderNames.request_header_authorization));
        return getNameBasic(authHeaderValue);
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
    	  //TODO:polly ok
          logWarning("ASJ.web.000205", "Cannot retrieve user credentials out of authorization request header.", e);
          if (LogContext.getLocationSecurity().beWarning()) {
          	  LogContext.getLocation(LogContext.LOCATION_SECURITY).traceWarning("ASJ.web.000563", 
          	    "Cannot retrieve user credentials out of authorization request header for web application [{0}]. " +
          	    "Incorrect header value [{1}]found.", new Object[]{application.getAliasName(), authHeaderValue},e, null, null);

        }
        return null;
      }
    } else if (HttpServletRequest.DIGEST_AUTH.equalsIgnoreCase(application.getAuthType())) {
      DigestCredentials digestCredentials = getDigestCredentials();
      return digestCredentials.getUsername();
    } else if (HttpServletRequest.FORM_AUTH.equalsIgnoreCase(application.getAuthType())) {
      return getParameter(j_username);
    } else {
      return null;
    }
  }

  public String getClientIp() {
    return clientIP;
  }

  public String getMethod() {
    return method;
  }

  public boolean isSecure() {
    return isSecure;
  }

  public InputStream getBody() {
    return new ByteArrayInputStream(new byte[0]);
  }

  public Object getSessionAttribute(String name) {
    return httpSession.getAttribute(name);
  }

	/**
	 * @deprecated
	 */
  public String[] getSessionAttributesNames() {
    return httpSession.getValueNames();
  }

  /**
   * Sets the response code that must be returned to the client if the authorization fails.
   */
  public void setResponseStatusCode(int code) {
    newRequest.setResponseStatusCode(code);
  }

  /**
   * Puts cookie with the specified name in the http request.
   */
  public void addResponseCookie(String name, String value) {
    newRequest.addResponseCookie(name, value);
  }

  /**
   * Puts header with the specified name in the http request.
   */
  public void addResponseHeader(String name, String value) {
    newRequest.addResponseHeader(name, value);
  }

  /**
   * Sets header with the specified name in the http request.
   */
  public void setResponseHeader(String name, String value) {
    newRequest.setResponseHeader(name, value);
  }

  /**
   * Sets the body of the response that the http must send to client if the authorization fails.
   */
  public void setHttpBody(byte[] body) {
    newRequest.setHttpBody(body);
  }

  public void setSessionAttribute(String name, Object value) {
    newRequest.setSessionAttribute(name, value);
  }

  public void removeSessionAttribute(String name) {
    newRequest.removeSessionAttribute(name);
  }

  public void setRequestAttribute(String name, Object value) {
    newRequest.setRequestAttribute(name, value);
  }

  public void generate(boolean isError) {

  }

  //----------------------- private -----------------------

  private String getNameBasic(String decoded) {
    if (decoded == null) {
      return null;
    }
    return decoded.substring(0, decoded.indexOf(':'));
  }

  private char[] getPasswordBasic(String decoded) {
    return decoded.substring(decoded.indexOf(':') + 1).toCharArray();
  }

  private String decode(String coded) throws Exception {
    if (coded == null || coded.indexOf("Basic") == -1) {
      return null;
    }
    coded = coded.substring(coded.indexOf(" ", coded.indexOf("Basic")) + 1);
    return new String(Base64.decode(coded.getBytes()));
  }

  private String getParameter(String key) {
    String[] allValues = getHttpParameterValues(key);
    if (allValues != null) {
      return allValues[0];
    }
    return null;
  }

  private void logWarning(String msgId,String msg, Throwable t) {
    LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, msgId, msg, t, null, null);
  }

  private DigestCredentials getDigestCredentials() {
    String authHeader = getHttpHeader(HeaderNames.request_header_authorization);
    if (authHeader == null || authHeader.indexOf("Digest") == -1) {
      return null;
    }
    DigestCredentials digestCredentials = new DigestCredentials();
    digestCredentials.setURI(getClientIp());
    digestCredentials.setDirectivesValues(authHeader);  // mandatory
    digestCredentials.setMethod(getMethod()); // mandatory
    return digestCredentials;
  }
}
