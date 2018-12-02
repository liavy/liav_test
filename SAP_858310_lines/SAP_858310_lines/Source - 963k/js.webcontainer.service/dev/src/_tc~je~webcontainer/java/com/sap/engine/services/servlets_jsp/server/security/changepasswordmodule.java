/*
 * Copyright (c) 2000-2008 by SAP AG, Walldorf.,
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
import com.sap.engine.services.servlets_jsp.server.runtime.client.ApplicationSession;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.*;
import com.sap.engine.services.httpserver.lib.ResponseCodes;
import com.sap.engine.services.httpserver.lib.Responses;
import com.sap.engine.services.httpserver.interfaces.HttpParameters;
import com.sap.engine.services.httpserver.interfaces.ErrorData;
import com.sap.engine.services.httpserver.interfaces.SupportabilityData;

import java.io.IOException;

public class ChangePasswordModule {
  private static final String sap_j_security_check_original_request = "sap_j_security_check_original_request";
  private static final String sap_j_security_check = "sap_j_security_check";
  private static final String j_sap_current_password = "j_sap_current_password";
  private static final String j_sap_password = "j_sap_password";

  private String loginPageURL = "/ChangePassword";
  private String errorPageURL = "/ChangePassword?error=true";
  private ApplicationSession session = null;

  public void prepareChangePassword(HttpRequestClientInfoImpl clientInfo, HttpParameters httpParameters, ApplicationContext scf) {
    ApplicationSession applicationSession = (ApplicationSession)httpParameters.getApplicationSession();
    if (applicationSession == null) {
      applicationSession = scf.getSessionServletContext().createSession(httpParameters);
    }
    initPages(httpParameters, applicationSession, scf);
    OriginalRequestClientInfoImpl originalRequest = new OriginalRequestClientInfoImpl(clientInfo, scf.getSessionServletContext());
    applicationSession.putSecurityValue(sap_j_security_check,
                 scf.getSessionServletContext().encodeURL(httpParameters.getRequest().getRequestLine().getFullUrl().toString(),
                     httpParameters.getRequestPathMappings().getAliasName(), httpParameters, applicationSession));
    applicationSession.putSecurityValue(sap_j_security_check_original_request, originalRequest);
  }

  public HttpRequestClientInfo willChangePasswordRequest(HttpRequestClientInfoImpl newClientInfo, HttpParameters httpParameters, ApplicationContext scf) throws IOException {
    ApplicationSession session = (ApplicationSession)httpParameters.getApplicationSession();
    if (session == null) { 
      httpParameters.setErrorData(new ErrorData(
          ResponseCodes.code_forbidden, Responses.mess24, Responses.mess25, false,
          new SupportabilityData()));//here we do not need user action
      return null;
    }
    OriginalRequestClientInfoImpl originalRequest = (OriginalRequestClientInfoImpl)session.getSecurityValue(sap_j_security_check_original_request);
    if (originalRequest == null) {
      httpParameters.setErrorData(new ErrorData(
          ResponseCodes.code_forbidden, Responses.mess24, Responses.mess25, false,
          new SupportabilityData()));//here we do not need user action
      return null;
    }
    this.session = session;
    initPages(httpParameters, session, scf);
    String pass = newClientInfo.getParameter(j_sap_password);
    if (pass != null) {
      originalRequest.setNewPassword(pass.toCharArray());
      pass = newClientInfo.getParameter(j_sap_current_password);
      originalRequest.setPassword(pass.toCharArray());
    }
    originalRequest.setNewRequest(newClientInfo);
    return originalRequest;
  }

  public String getErrorPage() {
    return errorPageURL;
  }

  public String getLoginPage() {
    return loginPageURL;
  }

  public String getURL() {
    String url = (String)session.removeSecurityValue(sap_j_security_check);
    session.removeSecurityValue(sap_j_security_check_original_request);
    return url;
  }

  private void initPages(HttpParameters httpParameters, ApplicationSession applicationSession, ApplicationContext scf) {
    String alias = httpParameters.getRequestPathMappings().getAliasName().toString();
    if (httpParameters.getRequestPathMappings().getZoneName() != null && !httpParameters.getRequestPathMappings().isZoneExactAlias()) {
      alias = alias + ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getZoneSeparator() + httpParameters.getRequestPathMappings().getZoneName();
    }
    loginPageURL = "/" + alias + loginPageURL;
    errorPageURL = "/" + alias + errorPageURL;
    if (scf.getSessionServletContext().getChangePasswordLoginPage() != null) {
      loginPageURL = "/" + alias + "/" + scf.getSessionServletContext().getChangePasswordLoginPage();
    } else {
      loginPageURL = "/" + alias + loginPageURL;
    }
    if (scf.getSessionServletContext().getChangePasswordErrorPage() != null) {
      errorPageURL = "/" + alias + "/" + scf.getSessionServletContext().getChangePasswordErrorPage();
    } else {
      errorPageURL = "/" + alias + errorPageURL;
    }
    errorPageURL = scf.getSessionServletContext().encodeURL(errorPageURL, httpParameters.getRequestPathMappings().getAliasName(), httpParameters, applicationSession);
    loginPageURL = scf.getSessionServletContext().encodeURL(loginPageURL, httpParameters.getRequestPathMappings().getAliasName(), httpParameters, applicationSession);
  }
}
