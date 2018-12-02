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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.engine.interfaces.security.AuthenticationContext;
import com.sap.engine.interfaces.security.SecurityContext;
import com.sap.engine.interfaces.security.auth.WebCallbackHandler;
import com.sap.engine.services.httpserver.lib.Responses;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.lib.Constants;
import com.sap.tc.logging.Location;

/**
 * The only responsibility of <code>ForwardServlet</code> is to forward
 * the incoming requests to a destination, relative to the server root,
 * defined by extra path information.
 * <p>
 * The servlet is mapped by default to /j_security_check/*, e.g. a request to
 * location .../j_security_check/login/LoginPage.jsp will be forwarded to a
 * resource from application with context root /login and path /LoginPage.jsp.
 * <p>
 * Forward locations are restricted to resources of the default form login
 * application.
 *
 * @author Marija Jurova
 * @author Nikolay Petkov
 * @since 710 (New York)
 */
public class ForwardServlet extends HttpServlet {
  /**
   * Holds logging location for this class
   */
  private static final Location location = LogContext.getLocationRequestInfoServer();;

  static final long serialVersionUID = 5216766859836483014L;

  public void init(ServletConfig servletConfig) throws ServletException {
    super.init(servletConfig);
  }

  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String pathInfo = request.getPathInfo();
    boolean beDebug = location.beDebug();
    if (pathInfo == null || pathInfo.length() == 0) {
      if (beDebug) {
				// Could not locate any resource
				location.debugT("doGet(HttpServletRequest, HttpServletResponse)",
						"Could not define forward location {0}", new Object[] { pathInfo });
			}
			response.sendError(HttpServletResponse.SC_NOT_FOUND, Responses.mess39);//here we do not need user action
      return;
    }

    SecurityContext securityContext =
      ServiceContext.getServiceContext().getSecurityContext();
    if (securityContext == null) {
      if (location.beError()) {
        LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceError("ASJ.web.000614",
          "Could not obtain security context", null, null);
			}
			return;
    }

    AuthenticationContext authenticationContext =
      securityContext.getAuthenticationContext();
    if (authenticationContext == null) {
      if (location.beError()) {
        LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceError("ASJ.web.000615",
          "Could not obtain authentication context", null, null);
			}
			return;
    }

    // Tries to get the default login application servlet context
    String contextRoot = WebCallbackHandler.getLogonApplicationAlias();
    ServletContext servletContext = getServletContext().getContext(contextRoot);
    if (servletContext == null) {
      if (beDebug) {
				// Could not extract the application context root
				location.debugT("doGet(HttpServletRequest, HttpServletResponse)",
						"Could not get the default form login application for alias {0}",
						new Object[] { contextRoot });
			}
			response.sendError(HttpServletResponse.SC_NOT_FOUND, Responses.mess40);//here we do not need user action
      return;
    }

    if (!pathInfo.startsWith(contextRoot)) {
      if (beDebug) {
				// Could not extract the application context root
				location.debugT("doGet(HttpServletRequest, HttpServletResponse)",
						"The forward location {0} isn't from the default form login application {1}",
						new Object[] { pathInfo, contextRoot });
			}
			response.sendError(HttpServletResponse.SC_NOT_FOUND, Responses.mess39);//here we do not need user action
      return;
    }

    String path = pathInfo.substring(contextRoot.length());
    RequestDispatcher requstDeispatcher =
      servletContext.getRequestDispatcher(path);
    if (requstDeispatcher == null) {
      if (beDebug) {
				// Could not obtain a request dispatcher for the path
				location.debugT("doGet(HttpServletRequest, HttpServletResponse)",
						"Could not locate a request dispatcher with path {0} for application {1}",
						new Object[] { path, contextRoot });
			}
			response.sendError(HttpServletResponse.SC_NOT_FOUND, Responses.mess39);//here we do not need user action
      return;
    }

    request.setAttribute(
      Constants.FORWARD_TO_STATIC_PARAMETER, new Boolean(true));
    requstDeispatcher.forward(request, response);
  }
}