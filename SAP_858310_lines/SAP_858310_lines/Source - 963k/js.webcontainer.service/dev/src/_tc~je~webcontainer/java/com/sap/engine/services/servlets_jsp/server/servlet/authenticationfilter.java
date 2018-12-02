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
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.engine.interfaces.security.AuthenticationContext;
import com.sap.engine.interfaces.security.SecurityContext;
import com.sap.engine.interfaces.security.auth.WebCallbackHandler;
import com.sap.engine.services.httpserver.lib.ResponseCodes;
import com.sap.engine.services.httpserver.lib.Responses;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.application.ServletContextImpl;
import com.sap.engine.services.servlets_jsp.server.lib.FilterUtils;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * The only responsibility of the <code>AuthenticationFilter</code> is to
 * enforce authenticate of the remote user. Authentication is done by calling
 * <code>com.sap.engine.services.security.login.FastLoginContext.login()</code>
 * method.
 * <p>
 * According to the login modules stack response could be generated. In this case
 * filter blocks the request.
 * <p>
 * By default <code>AuthenticationFilter</code> is only defined as a name
 * inside the global-web.xml and the name is fixed to AuthenticationFilter.
 * <p>
 * <code>com.sap.engine.services.servlets_jsp.server.application.SessionServletContext.checkUser(...)</code>
 * method is responsible to add this filter in front of the available filters if
 * authentication is required.
 *
 * @author Nikolay Petkov
 * @since 710
 */
public class AuthenticationFilter implements Filter {
  /**
   * Holds the logging location for this class
   */
  private static final Location location = LogContext.getLocationSecurity();;

  /**
   * Holds the application context
   */
  private ApplicationContext applicationContext;

  /**
   * Holds the authentication context
   */
  private AuthenticationContext authenticationContext;

  public void init(FilterConfig config) throws ServletException {
    ServletContext servletContext = config.getServletContext();
    applicationContext =
      ((ServletContextImpl) servletContext).getApplicationContext();

    ServiceContext serviceContext = ServiceContext.getServiceContext();
    SecurityContext securityContext = serviceContext.getSecurityContext();
    String policyConfigurationName = applicationContext.getPolicyConfigID();
    SecurityContext applicationSecurityContext = securityContext
      .getPolicyConfigurationContext(policyConfigurationName);
    authenticationContext = applicationSecurityContext
      .getAuthenticationContext();
  }

  public void destroy() {
  }

  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    // Gets the application login context and makes login
    LoginContext loginContext = authenticationContext.getLoginContext(
      new Subject(), new WebCallbackHandler(httpRequest, httpResponse));
    boolean beDebug = location.beDebug();
    try {
      if (beDebug) {
        location.debugT(
          "doFilter(ServletRequest, ServletResponse, FilterChain)",
          "Trying to authorize the request for context root {0}"
          + " and path {1}", new Object[] { httpRequest.getContextPath(),
          httpRequest.getServletPath() });
      }

      loginContext.login();
      if (beDebug) {
        location.debugT(
          "doFilter(ServletRequest, ServletResponse, FilterChain)",
          "The request authorization succeed for context root {0}"
          + " and path {1}", new Object[] { httpRequest.getContextPath(),
          httpRequest.getServletPath() });
      }

      // Blocks the request if response is ready
      if (httpResponse.isCommitted()) {
        return;
      }

      if (authorize(httpRequest)) {
        chain.doFilter(request, response);
      } else {
        // Unauthorized
        httpResponse.sendError(ResponseCodes.code_forbidden, Responses.mess22);//here we do not need user action
      }
    } catch (LoginException le) {
      if (beDebug) {
        location.traceThrowableT(Severity.DEBUG,
          "doFilter(ServletRequest, ServletResponse, FilterChain)",
          "The request authorization failed for context root {0} and path {1}",
          new Object[] { httpRequest.getContextPath(),
            httpRequest.getServletPath() }, le);
      } else if (location.beInfo()) {
        location.infoT(
          "doFilter(ServletRequest, ServletResponse, FilterChain)",
          "The request authorization failed for context root {0} and path {1}",
          new Object[] { httpRequest.getContextPath(),
            httpRequest.getServletPath() });
      }
      if (WebCallbackHandler.CHANGE_SCHEMA_HTTPS.equals(httpRequest
          .getAttribute(WebCallbackHandler.REQUEST_SECURE_ACTION))) {
        String location = FilterUtils.unWrapResponse(httpResponse)
          .getRequestURLForScheme("https");
        httpResponse.sendRedirect(httpResponse.encodeRedirectURL(location));
      }
      if (FilterUtils.unWrapResponse(httpResponse).getStatus() == ResponseCodes.code_ok &&
          !httpResponse.isCommitted()) {
        httpResponse.sendError(ResponseCodes.code_unauthorized, Responses.mess67);
      }
    } catch (RuntimeException re) {
      if (re.getCause() != null && re.getCause() instanceof ServletException) {
        throw (ServletException)re.getCause();
      } else {
        throw re;
      }
    }
  }

  private boolean authorize(HttpServletRequest httpRequest) {
    return applicationContext.getSessionServletContext().doCheckPermissionsJacc(
            httpRequest.getMethod(), httpRequest.isSecure(), applicationContext.getRequestedResource(new MessageBytes(httpRequest.getRequestURI().getBytes()), httpRequest.getContextPath()));//todo - encoding???
	}
}
