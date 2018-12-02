/*
 * Copyright (c) 2002 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.security;

import java.security.cert.X509Certificate;
import java.io.InputStream;

public interface HttpRequestClientInfo {
  public static final String j_username = "j_username";
  public static final String j_password= "j_password";
  public static final byte[] j_security_check = "/j_security_check".getBytes();
  /**
   * Gets a client certificate chain used for ssl connection.
   */
  public X509Certificate[] getCertificateChain();

  /**
   * Require the client to open a ssl connection if hasn't and starts hadnshake to retrieve its certificate.
   */
  public void requireCertificateChain();

  /**
   * Gets cookie with the specified name from http request.
   */
  public String getHttpCookie(String name);

  /**
   * Gets header with the specified name from http request.
   */
  public String getHttpHeader(String name);

  public void removeRequestHeader(String name, String value);

  /**
   * Gets parameter with the specified name from http request.
   */
  public String[] getHttpParameterValues(String name);

  /**
   * Retrives the user password from http request. The password is searched depending on the authentication method.
   */
  public char[] getPassword();

  /**
   * Retrives the user new password, that will replace the old one.
   */
  public char[] getNewPassword();

  /**
   * Retrives the user name from http request. The user name is searched depending on the authentication method.
   */
  public String getUserName();

  public String getClientIp();

  public String getMethod();

  public boolean isSecure();

  public InputStream getBody();

  public Object getSessionAttribute(String name);

  public String[] getSessionAttributesNames();

  /**
   * Sets the response code that must be returned to the client if the authorization fails.
   */
  public void setResponseStatusCode(int code);

  /**
   * Puts cookie with the specified name in the http request.
   */
  public void addResponseCookie(String name, String value);

  /**
   * Puts header with the specified name in the http request.
   */
  public void addResponseHeader(String name, String value);

  /**
   * Setss header with the specified name in the http request.
   */
  public void setResponseHeader(String name, String value);

  /**
   * Sets the body of the response that the http must send to client if the authorization fails.
   */
  public void setHttpBody(byte[] body);

  public void setSessionAttribute(String name, Object value);

  public void removeSessionAttribute(String name);

  public void setRequestAttribute(String name, Object value);
}
