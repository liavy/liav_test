/*
 * Copyright (c) 2002-2008 by SAP AG, Walldorf.,
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
import com.sap.engine.services.servlets_jsp.server.lib.Constants;
import com.sap.engine.services.servlets_jsp.server.lib.WebParseUtils;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.httpserver.lib.*;
import com.sap.engine.services.httpserver.lib.protocol.HeaderNames;
import com.sap.engine.services.httpserver.lib.headers.MimeHeaders;
import com.sap.engine.services.httpserver.interfaces.HttpParameters;
import com.sap.engine.services.httpserver.interfaces.ErrorData;
import com.sap.engine.services.httpserver.interfaces.SupportabilityData;
import com.sap.engine.services.httpserver.interfaces.client.SslAttributes;
import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.lib.util.ArrayObject;
import com.sap.engine.lib.security.Base64;
import com.sap.engine.lib.security.http.DigestCredentials;
import com.sap.engine.lib.security.http.DigestAuthenticationInfo;
import com.sap.engine.lib.security.http.DigestChallenge;
import com.sap.engine.frame.core.thread.ThreadContext;
import com.sap.engine.interfaces.security.SecurityContextObject;
import com.sap.engine.interfaces.security.SecuritySession;
import com.sap.engine.services.servlets_jsp.server.*;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.runtime.client.ApplicationSession;
import com.sap.tc.logging.Location;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.cert.X509Certificate;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class HttpRequestClientInfoImpl implements HttpRequestClientInfo {
  private static Location currentLocation = Location.getLocation(HttpRequestClientInfoImpl.class);
  private WebCookieConfig defaultCookieConfig = null;
  private HttpParameters httpRequest = null;
  private ApplicationContext application = null;
  private ChangePasswordModule changePasswordModule = null;
  private HashMapObjectObject parameters = new HashMapObjectObject();
  private String characterEncoding = Constants.DEFAULT_CHAR_ENCODING;
  private boolean parametersParsed = false;
  private boolean requireCertificateChain = false;
  private int responseCode = HttpServletResponse.SC_UNAUTHORIZED;
  private byte[] body = "Unauthorized".getBytes();
  private boolean setAnything = false;
  private boolean passwordExpired = false;

  public HttpRequestClientInfoImpl(HttpParameters httpRequest, ApplicationContext application) {
    this.httpRequest = httpRequest;
    this.application = application;
    defaultCookieConfig = new WebCookieConfig(application.getAliasName(), WebCookieConfig.COOKIE_TYPE_OTHER);
    defaultCookieConfig.setPath(WebCookieConfig.OTHER, "/");

  }
  /**
   * Gets a client certificate chain used for ssl connection.
   */
  public X509Certificate[] getCertificateChain() {
    requireCertificateChain();
    SslAttributes sslAttributes = httpRequest.getRequest().getSslAttributes();
    if (sslAttributes == null) {
      return null;
    }
    return sslAttributes.getCertificates();
  }

  /**
   * Require the client to open a ssl connection if hasn't and starts handshake to retrieve its certificate.
   */
  public void requireCertificateChain() {
    requireCertificateChain = true;
  }

  /**
   * Gets cookie with the specified name from http request.
   */
  public String getHttpCookie(String name) {
    ArrayObject cookies = httpRequest.getRequest().getCookies(application.getWebApplicationConfiguration().isURLSessionTracking());
    if (cookies == null || cookies.size() == 0) {
      return null;
    }
    for (int i = 0; i < cookies.size(); i++) {
      HttpCookie httpCookie = (HttpCookie)cookies.elementAt(i);
      if (httpCookie.getName().equals(name)) {
        return httpCookie.getValue();
      }
    }
    return null;
  }

  Cookie[] getAllCookies() {
    ArrayObject cookies = httpRequest.getRequest().getCookies(application.getWebApplicationConfiguration().isURLSessionTracking());
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
   * Gets header with the specified name from http request.
   */
  public String getHttpHeader(String name) {
    return httpRequest.getRequest().getHeaders().getHeader(name);
  }

  MimeHeaders getAllHeaders() {
    return httpRequest.getRequest().getHeaders();
  }

  public void removeRequestHeader(String name, String value) {
    if (value == null) {
      httpRequest.getRequest().getHeaders().removeHeader(name);
    } else {
      httpRequest.getRequest().getHeaders().removeHeader(name, value);
    }
  }

  /**
   * Gets parameter with the specified name from http request.
   */
  public String[] getHttpParameterValues(String name) {
    if (!parametersParsed) {
      try {
        parseEncoding();
      } catch (UnsupportedEncodingException e) {
    	LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000198",
    	  "Cannot get parameter [{0}] value due to unsupported character encoding found in an http request to [{1}] web application.",
    	  new Object[]{name, application.getAliasName()}, e, null, null);
      }
      parseParameters();
    }
    return (String[]) parameters.get(name);
  }

  HashMapObjectObject getAllParameters() {
    if (!parametersParsed) {
      try {
        parseEncoding();
      } catch (UnsupportedEncodingException e) {
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000199",
          "Cannot get parameters due to unsupported character encoding found in an http request to [{0}] web application.",
          new Object[]{application.getAliasName()}, e, null, null);
      }
      parseParameters();
    }
    return parameters;
  }

  private void parseEncoding() throws UnsupportedEncodingException {
    String characterEncoding = null;

    characterEncoding = WebParseUtils.parseEncoding(httpRequest);
    if (characterEncoding == null || characterEncoding.equals("")) {
      characterEncoding = Constants.DEFAULT_CHAR_ENCODING;
    }
    try {
      setCharacterEncoding(characterEncoding);
    } catch (UnsupportedEncodingException uee) {
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000200",
          "Unsupported character encoding [{0}] found in an http request. " +
          "This invalid encoding cannot be used in reading request parameters or reading request input stream.",
          new Object[]{characterEncoding}, uee, null, null);
    	throw uee;
    }
  }

  /**
   * Retrieves the user password from http request. The password is searched depending on the authentication method.
   */
  public char[] getPassword() {
    if (HttpServletRequest.BASIC_AUTH.equalsIgnoreCase(application.getSessionServletContext().getAuthType())) {
    	String authHeaderValue = "";
      try {
        authHeaderValue = decode(getHttpHeader(HeaderNames.request_header_authorization));
        return getPasswordBasic(authHeaderValue);
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
    	  //tODO:polly ok
        logWarning("ASJ.web.000201", "Cannot retrieve user credentials out of authorization request header.", e);
        if (LogContext.getLocationSecurity().beWarning()) {
        	  LogContext.getLocation(LogContext.LOCATION_SECURITY).traceWarning("ASJ.web.000560",
        	    "Cannot retrieve user credentials out of authorization request header for web application [{0}]. " +
        	    "Incorrect header value [{1}]found.", new Object[]{application.getAliasName(), authHeaderValue},e, null, null);

      }

        return null;
      }
    } else if (HttpServletRequest.FORM_AUTH.equalsIgnoreCase(application.getSessionServletContext().getAuthType())) {
      if (httpRequest.getRequest().getRequestLine().getUrlDecoded().endsWith(j_security_check)) {
        return getParameterCharArray(j_password);
      }
    }
    return null;
  }

  /**
   * Retrieves the user new password, that will replace the old one.
   */
  public char[] getNewPassword() {
    passwordExpired = true;
    if (changePasswordModule == null) {
      changePasswordModule = new ChangePasswordModule();
    }
    return null;
  }

  /**
   * Retrieves the user name from http request. The user name is searched depending on the authentication method.
   */
  public String getUserName() {
    if (HttpServletRequest.BASIC_AUTH.equalsIgnoreCase(application.getSessionServletContext().getAuthType())) {
    	String authHeaderValue = "";
      try {
        authHeaderValue = decode(getHttpHeader(HeaderNames.request_header_authorization));
        return getNameBasic(authHeaderValue);
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
    	  //TODO:Polly ok
        logWarning("ASJ.web.000202", "Cannot retrieve user credentials out of authorization request header.", e);
        if (LogContext.getLocationSecurity().beWarning()) {
      	  LogContext.getLocation(LogContext.LOCATION_SECURITY).traceWarning("ASJ.web.000561",
      	    "Cannot retrieve user credentials out of authorization request header for web application [{0}]. " +
      	    "Incorrect header value [{1}]found.", new Object[]{application.getAliasName(), authHeaderValue},e, null, null);

    }
        return null;
      }
    } else if (HttpServletRequest.DIGEST_AUTH.equalsIgnoreCase(application.getSessionServletContext().getAuthType())) {
      DigestCredentials digestCredentials = getDigestCredentials();
      return digestCredentials.getUsername();
    } else if (HttpServletRequest.FORM_AUTH.equalsIgnoreCase(application.getSessionServletContext().getAuthType())) {
      if (httpRequest.getRequest().getRequestLine().getUrlDecoded().endsWith(j_security_check)) {
        return getParameter(j_username);
      }
    }
    return null;
  }

  public String getClientIp() {
    return new String(ParseUtils.inetAddressByteToString(httpRequest.getRequest().getClientIP()));
  }

  public String getMethod() {
    return new String(httpRequest.getRequest().getRequestLine().getMethod());
  }

  public boolean isSecure() {
    return httpRequest.getRequest().getRequestLine().isSecure();
  }

  public InputStream getBody() {
    return httpRequest.getRequest().getBody();
  }

  public Object getSessionAttribute(String name) {
    ApplicationSession applicationSession = (ApplicationSession)httpRequest.getApplicationSession();
    if (applicationSession == null) {
      applicationSession = application.getSessionServletContext().createSession(httpRequest);
    }
    return applicationSession.getAttribute(name);
  }

	/**
	 * @deprecated
	 */
  public String[] getSessionAttributesNames() {
    ApplicationSession applicationSession = (ApplicationSession)httpRequest.getApplicationSession();
    if (applicationSession == null) {
      applicationSession = application.getSessionServletContext().createSession(httpRequest);
    }
    return applicationSession.getValueNames();
  }

  /**
   * Sets the response code that must be returned to the client if the authorization fails.
   */
  public void setResponseStatusCode(int code) {
    responseCode = code;
    setAnything = true;
  }

  /**
   * Puts cookie with the specified name in the http request.
   */
  public void addResponseCookie(String name, String value) {
    HttpCookie cok = CookieParser.createCookie(name, value,
        httpRequest.getRequest().getHost(),
        defaultCookieConfig);
    //todo - getCookieHeader da vryshta samo value
    MessageBytes cook = new MessageBytes(CookieUtils.getCookieHeader(cok));
    int index = cook.indexOf(':');
    httpRequest.getResponse().getHeaders().addHeader(cook.getBytes(0, index), cook.getBytes(index + 2));
  }

  /**
   * Puts header with the specified name in the http request.
   */
  public void addResponseHeader(String name, String value) {
    httpRequest.getResponse().getHeaders().addHeader(name.getBytes(), value.getBytes());
  }

  /**
   * Sets header with the specified name in the http request.
   */
  public void setResponseHeader(String name, String value) {
    httpRequest.getResponse().getHeaders().putHeader(name.getBytes(), value.getBytes());
  }

  /**
   * Sets the body of the response that the http must send to client if the authorization fails.
   */
  public void setHttpBody(byte[] body) {
    this.body = body;
    setAnything = true;
  }

  public void setSessionAttribute(String name, Object value) {
    ApplicationSession applicationSession = (ApplicationSession)httpRequest.getApplicationSession();
    if (applicationSession == null) {
      applicationSession = application.getSessionServletContext().createSession(httpRequest);
    }
    applicationSession.setAttribute(name, value);
  }

  public void removeSessionAttribute(String name) {
    ApplicationSession applicationSession = (ApplicationSession)httpRequest.getApplicationSession();
    if (applicationSession == null) {
      applicationSession = application.getSessionServletContext().createSession(httpRequest);
    }
    applicationSession.removeAttribute(name);
  }

  public void setRequestAttribute(String name, Object value) {
    httpRequest.setRequestAttribute(name, value);
  }

// -------------- USED WITHIN THE HTTP --------------

  public boolean isRequiredCertificateChain() {
    return requireCertificateChain;
  }

  public boolean isPasswordExpired() {
    return passwordExpired;
  }

  public String getPasswordChangeLoginPage() {
    return changePasswordModule.getLoginPage();
  }

  public boolean generate(boolean isError, String errorMessage) throws IOException {
    if (isError) {
      if (!setAnything) {
        if (passwordExpired) {
          changePasswordModule.prepareChangePassword(this, httpRequest, application);
          return false;
        }
        if (requireCertificateChain) {
          if (!httpRequest.getRequest().getRequestLine().isSecure()) {
            httpRequest.getResponse().setSchemeHttps();
            return false;
          } else {
            requireCertificateChain = false;
//            httpRequest.sendFlag(Constants.VERIFY_CERT);
          }
//          return false;
        }
        body = errorMessage.getBytes();
        if (HttpServletRequest.BASIC_AUTH.equalsIgnoreCase(application.getSessionServletContext().getAuthType())) {
          httpRequest.getResponse().getHeaders().addHeader(HeaderNames.response_header_www_authenticate_,
                                                     ("Basic realm=\"" + application.getSessionServletContext().getLoginRealmName() + "\"").getBytes());
        } else if (HttpServletRequest.DIGEST_AUTH.equalsIgnoreCase(application.getSessionServletContext().getAuthType())) {
          setDigestUnAuthHeader();
        } else if (HttpServletRequest.FORM_AUTH.equalsIgnoreCase(application.getSessionServletContext().getAuthType())) {
          byte[] newUrl = null;
          MessageBytes aliasUsed = httpRequest.getRequestPathMappings().getAliasName();
          if (httpRequest.getRequestPathMappings().getZoneName() != null && !httpRequest.getRequestPathMappings().isZoneExactAlias()) {
            aliasUsed.appendAfter(ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getZoneSeparator().getBytes());
            aliasUsed.appendAfter(httpRequest.getRequestPathMappings().getZoneName().getBytes());
          }
          if (httpRequest.getRequest().getRequestLine().getUrlDecoded().endsWith(j_security_check)) {
            newUrl = ("/" + aliasUsed + application.getSessionServletContext().getFormLoginErrorPage()).getBytes();
          } else {
            newUrl = ("/" + aliasUsed + application.getSessionServletContext().getFormLoginLoginPage()).getBytes();
            createSessionFormLogin();
          }
          ApplicationSession applicationSession = (ApplicationSession)httpRequest.getApplicationSession();
          if (applicationSession == null) {
            applicationSession = application.getSessionServletContext().createSession(httpRequest);
          }
          newUrl = application.getSessionServletContext().encodeURL(new String(newUrl), aliasUsed, httpRequest, applicationSession).getBytes();
          responseCode = ResponseCodes.code_found;
          httpRequest.getResponse().getHeaders().addHeader(HeaderNames.response_header_location_, newUrl);
        }
      }
      httpRequest.setErrorData(new ErrorData(responseCode, new String(body), Responses.mess9, false,
        new SupportabilityData(true, "", "", "", "", "")));
      return true;
    } else {
      if (!setAnything) {
        if (HttpServletRequest.BASIC_AUTH.equalsIgnoreCase(application.getSessionServletContext().getAuthType())) {
          //ok
        } else if (HttpServletRequest.DIGEST_AUTH.equalsIgnoreCase(application.getSessionServletContext().getAuthType())) {
          setDigestAuthHeader();
        } else if (HttpServletRequest.FORM_AUTH.equalsIgnoreCase(application.getSessionServletContext().getAuthType())) {
          ApplicationSession applicationSession = (ApplicationSession)httpRequest.getApplicationSession();
          if (applicationSession == null) {
            applicationSession = application.getSessionServletContext().createSession(httpRequest);
          }
          String url = (String)applicationSession.removeSecurityValue("j_security_check");
          if (url != null) {
            applicationSession.setFormLoginBodyParameters(true);
            responseCode = ResponseCodes.code_found;
            httpRequest.getResponse().getHeaders().addHeader(HeaderNames.response_header_location_, url.getBytes());
            httpRequest.getResponse().sendResponse(responseCode);
            return true;
          }
        }
      }
    }
    return false;
  }

// -------------- PRIVATE --------------

  public HttpSession getHttpSession() {
    ApplicationSession applicationSession = (ApplicationSession)httpRequest.getApplicationSession();
    if (applicationSession == null) {
      applicationSession = application.getSessionServletContext().createSession(httpRequest);
    }
    return applicationSession;
  }

  public String getParameter(String key) {
    String[] allValues = getHttpParameterValues(key);
    if (allValues != null && allValues.length > 0) {
      return allValues[0];
    }
    return null;
  }

  private char[] getParameterCharArray(String key) {
    String[] allValues = getHttpParameterValues(key);
    if (allValues != null && allValues.length > 0 && allValues[0] != null) {
      return allValues[0].toCharArray();
    }
    return null;
  }

  private void parseParameters() {
    parametersParsed = true;
    try {
      WebParseUtils.parseParameters(parameters,  httpRequest, characterEncoding);
    } catch (UnsupportedEncodingException e) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000203",
        "Cannot parse the parameters of the client request to [{0}] web application. Invalid character encoding [{1}] found in it.",
        new Object[]{application.getAliasName(), characterEncoding}, e, null, null);
    }
  }

  private void setCharacterEncoding(String enc) throws UnsupportedEncodingException {
    // Ensure that the specified encoding is valid
    byte buffer[] = new byte[1];
    buffer[0] = (byte) 'a';
    new String(buffer, enc);
    characterEncoding = enc;
  }

  private String getNameBasic(String decoded) {
    if (decoded == null) {
      return null;
    }
    int ddInd = decoded.indexOf(':');
    if (ddInd == -1) {
      return null;
    }
    return decoded.substring(0, ddInd);
  }

  private char[] getPasswordBasic(String decoded) {
    if (decoded == null) {
      return null;
    }
    int ddInd = decoded.indexOf(':');
    if (ddInd == -1) {
      return null;
    }
    return decoded.substring(ddInd + 1).toCharArray();
  }

  private String decode(String coded) throws Exception {
    if (coded == null) {
      return null;
    }
    int basicInd = coded.indexOf("Basic");
    if (basicInd == -1) {
      return null;
    }
    int spInd = coded.indexOf(" ", basicInd);
    if (spInd == -1) {
      return null;
    }
    coded = coded.substring(spInd + 1);
    return new String(Base64.decode(coded.getBytes()));
  }

  private DigestCredentials getDigestCredentials() {
    String authHeader = getHttpHeader(HeaderNames.request_header_authorization);
    if (authHeader == null || authHeader.indexOf("Digest") == -1) {
      return null;
    }
    DigestCredentials digestCredentials = new DigestCredentials();
    digestCredentials.setURI(getClientIp());
    digestCredentials.setDirectivesValues(authHeader);  // mandatory
    digestCredentials.setMethod(new String(httpRequest.getRequest().getRequestLine().getMethod())); // mandatory
    return digestCredentials;
  }

  private void setDigestAuthHeader() {
    ThreadContext localTC = ServiceContext.getServiceContext().getThreadSystem().getThreadContext();
    SecurityContextObject security  = (SecurityContextObject)localTC.getContextObject(localTC.getContextObjectId(SecurityContextObject.NAME));
    SecuritySession securitySession = security.getSession();
    if (securitySession == null || securitySession.getSubject() == null) {
      return;
    }
    Iterator publicCred = securitySession.getSubject().getPublicCredentials().iterator();
    Object cred = null;
    while (publicCred.hasNext()) {
      cred = publicCred.next();
      if (cred instanceof DigestAuthenticationInfo) {
        httpRequest.getResponse().getHeaders().addHeader(HeaderNames.response_header_authentication_info_, cred.toString().getBytes());
      }
    }
  }

  private void setDigestUnAuthHeader() {
    DigestChallenge digestChallenge = new DigestChallenge();
    digestChallenge.setDomain("");
    digestChallenge.setStale(true);                // true or false
    digestChallenge.setDigestAlgorithm("MD5");
    digestChallenge.setMessageQOP("auth");
    String nonce = DigestChallenge.generateNonce(new String(ParseUtils.inetAddressByteToString(httpRequest.getRequest().getClientIP()))); // mandatory
    digestChallenge.setNonce(nonce); // mandatory
    digestChallenge.setRealm(application.getSessionServletContext().getLoginRealmName()); // mandatory
    httpRequest.getResponse().getHeaders().addHeader(HeaderNames.response_header_www_authenticate_, digestChallenge.toString().getBytes());
  }

  private void createSessionFormLogin() {
    String url = httpRequest.getRequest().getRequestLine().getFullUrl().toString();
    ApplicationSession applicationSession = (ApplicationSession)httpRequest.getApplicationSession();
    if (applicationSession == null) {
      applicationSession = application.getSessionServletContext().createSession(httpRequest);
    }
    applicationSession.putSecurityValue("j_security_check",
        application.getSessionServletContext().encodeURL(url, httpRequest.getRequestPathMappings().getAliasName(), httpRequest, applicationSession));
    MessageBytes paramBody = httpRequest.getRequestParametersBody();
    if (paramBody != null) {
      try {
        HashMapObjectObject parameters = new HashMapObjectObject(8);
        String characterEncoding = null;
        characterEncoding = WebParseUtils.parseEncoding(httpRequest);
        if (characterEncoding == null || characterEncoding.equals("")) {
          characterEncoding = Constants.DEFAULT_CHAR_ENCODING;
        }
        WebParseUtils.parseQueryString(parameters, paramBody.getBytes(), characterEncoding);
        applicationSession.putSecurityValue("j_request_parameters", parameters);
      } catch (UnsupportedEncodingException e) {
    	  //TODO:Polly
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000204",
          "Invalid character encoding [{0}] found in the client request to [{1}] web application.",
          new Object[]{characterEncoding, application.getAliasName()}, e, null, null);
      }
    }
  }

  private void logWarning(String msgId, String msg, Throwable t) {
    LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, msgId, msg, t, null, null);
  }
}
