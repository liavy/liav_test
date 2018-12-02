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
package com.sap.engine.services.servlets_jsp.server.servlet;

import java.io.IOException;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.sap.engine.interfaces.security.AuthenticationContext;
import com.sap.engine.interfaces.security.SecurityContext;
import com.sap.engine.interfaces.security.auth.WebCallbackHandler;
import com.sap.engine.services.httpserver.lib.ResponseCodes;
import com.sap.engine.services.httpserver.lib.Responses;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.application.ServletContextImpl;
import com.sap.engine.services.servlets_jsp.server.lib.FilterUtils;
import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletRequestFacadeWrapper;
import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletResponseFacadeWrapper;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class PutProtectionFilter implements Filter {
  /**
   * Holds role with permission to upload files
   * <p>
   * Defined in global standard and additional deployment
   * descriptors and mapped to server role administrators
   */
  private static final String UPLOAD_ROLE_NAME = "$SAP_J2EE_Engine_Upload";
  private static Location currentLocation = LogContext.getLocationSecurity();

  private ApplicationContext applicationContext = null;
  private SecurityContext securityContext = null;
  private AuthenticationContext authenticationContext = null;

  public void init(FilterConfig filterConfig) throws ServletException {
    applicationContext = ((ServletContextImpl)filterConfig.getServletContext()).getApplicationContext();
    securityContext = applicationContext.getSessionServletContext().getAppSecurityContext();
    authenticationContext = securityContext.getAuthenticationContext();
  }

  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    HttpServletRequestFacadeWrapper unwrappedRequest = FilterUtils.unWrapRequest(request);
    HttpServletResponseFacadeWrapper unwrappedResponse = FilterUtils.unWrapResponse(response);

    // If there is an already authenticated user
    boolean beDebug = currentLocation.beDebug();
    if (unwrappedRequest.isUserInRole(UPLOAD_ROLE_NAME)) {
      if (beDebug) {
        currentLocation.debugT(
          "doFilter(ServletRequest, ServletResponse, FilterChain)",
          "User [{0}] is in role [{1}] and upload is allowed",
          new Object[] { unwrappedRequest.getRemoteUser(), UPLOAD_ROLE_NAME });
      }

      filterChain.doFilter(request, response);
      return;
    }

    // If there is an already authenticated user but isn't in
    // required role (don't force re-authentication)
    boolean isLoginNeeded = authenticationContext.isLoginNeeded();
    if (!isLoginNeeded) {
      if (beDebug) {
        currentLocation.debugT(
          "doFilter(ServletRequest, ServletResponse, FilterChain)",
          "User [{0}] isn't in role [{1}] and upload is denied",
          new Object[] { unwrappedRequest.getRemoteUser(), UPLOAD_ROLE_NAME });
      }

      unwrappedResponse.sendError(ResponseCodes.code_forbidden,
        Responses.mess54.replace("{RESOURCE}", unwrappedRequest.getRequestURIinternal()),
        Responses.mess55, true);//here we do not need user action
      return;
    }

    // Forces BASIC authentication and makes login
    unwrappedRequest.setForcedAuthType(HttpServletRequest.BASIC_AUTH);
    LoginContext loginContext = authenticationContext.getLoginContext(
      new Subject(), new WebCallbackHandler(unwrappedRequest, unwrappedResponse));
    try {
      if (beDebug) {
        currentLocation.debugT(
          "doFilter(ServletRequest, ServletResponse, FilterChain)",
          "Trying to authorize the request for context root [{0}] and path [{1}]",
          new Object[] { unwrappedRequest.getContextPath(),
          unwrappedRequest.getServletPath() });
      }

      loginContext.login();
      if (beDebug) {
        currentLocation.debugT(
          "doFilter(ServletRequest, ServletResponse, FilterChain)",
          "The request authorization succeed for context root [{0}] and path [{1}]",
          new Object[] { unwrappedRequest.getContextPath(),
          unwrappedRequest.getServletPath() });
      }

      // Blocks the request if response is ready
      if (unwrappedResponse.isCommitted()) {
        return;
      }

      // Checks if authorized user is in the right role
      if (unwrappedRequest.isUserInRole(UPLOAD_ROLE_NAME)) {
        if (beDebug) {
          currentLocation.debugT(
            "doFilter(ServletRequest, ServletResponse, FilterChain)",
            "User [{0}] is in role [{1}] and upload is allowed",
            new Object[] { unwrappedRequest.getRemoteUser(), UPLOAD_ROLE_NAME });
        }

        filterChain.doFilter(request, response);
        return;
      }
    } catch (LoginException le) {
      if (beDebug) {
        currentLocation.traceThrowableT(Severity.DEBUG,
          "doFilter(ServletRequest, ServletResponse, FilterChain)",
          "The request authorization failed for context root [{0}] and path [{1}]",
          new Object[] { unwrappedRequest.getContextPath(),
          unwrappedRequest.getServletPath() }, le);
      } else if (currentLocation.beInfo()) {
        currentLocation.infoT(
          "doFilter(ServletRequest, ServletResponse, FilterChain)",
          "The request authorization failed for context root [{0}] and path [{1}]",
          new Object[] { unwrappedRequest.getContextPath(),
          unwrappedRequest.getServletPath() });
      }
    } finally {
      // Restores the original auth type
      unwrappedRequest.setForcedAuthType(null);
    }

    if (beDebug) {
      currentLocation.debugT(
        "doFilter(ServletRequest, ServletResponse, FilterChain)",
        "User [{0 }] isn't in role [{1}] and upload is denied",
        new Object[] { unwrappedRequest.getRemoteUser(), UPLOAD_ROLE_NAME });
    }

    unwrappedResponse.sendError(ResponseCodes.code_forbidden,
      Responses.mess54.replace("{RESOURCE}", unwrappedRequest.getRequestURIinternal()),
      Responses.mess55, true);//here we do not need user action
    return;
  }

  public void destroy() {}
}
