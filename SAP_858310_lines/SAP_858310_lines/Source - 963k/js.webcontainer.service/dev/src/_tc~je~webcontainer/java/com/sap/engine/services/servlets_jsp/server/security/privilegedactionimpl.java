/*
 * Copyright (c) 2000-2009 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.security;

import static com.sap.engine.services.servlets_jsp.server.deploy.WebContainer.initStartupTime2;

import java.io.*;
import javax.servlet.*;

import java.security.PrivilegedExceptionAction;

import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.servlets_jsp.server.*;
import com.sap.engine.services.servlets_jsp.server.application.ServletContextImpl;
import com.sap.engine.services.servlets_jsp.server.application.WebApplicationConfig;
import com.sap.engine.interfaces.security.SecuritySession;
import com.sap.engine.interfaces.security.SecurityContextObject;

/*
 * Implements PrivilegedAction , used to override identity when servlet
 * is started with run-as identity.
 *
 * @author Boby Kadrev
 * @version 4.0
 */
public class PrivilegedActionImpl extends Invokable implements PrivilegedExceptionAction {
  private final static int INIT = 0;
  private final static int SERVICE = 1;
  private final static int DESTROY = 2;

  transient private Servlet servlet = null;
  transient private ServletRequest servletRequest = null;
  transient private ServletResponse servletResponse = null;
  transient private ServletConfig servletConfig = null;
  private int type = SERVICE;
  private boolean exitApplication = false;
  private SecuritySession ss = null;
  transient private WebApplicationConfig webAppConfig = null;

  public PrivilegedActionImpl(Servlet servlet, ServletRequest servletRequest, ServletResponse servletResponse, boolean exitApplication) {
    this.servlet = servlet;
    this.servletRequest = servletRequest;
    this.servletResponse = servletResponse;
    type = SERVICE;
    this.exitApplication = exitApplication;
    ServletConfig servletConfig = servlet.getServletConfig();
    ServletContextImpl sc;
    if (servletConfig != null) {
      sc = (ServletContextImpl) servletConfig.getServletContext();
    } else {
      throw new IllegalStateException("ServletConfig for servlet [" + servlet.getClass() + "] is null. " +
      	"Possible reason: When the servlet implements init(ServletConfig) method, it does not call super.init(ServletConfig).");
    }
    SecuritySession ss = ((SecurityContextObject) sc.getApplicationContext().getSecurityContext()).getSession();
    if (ss.getAuthenticationConfiguration() != null) {
      this.ss = ss;
    }

    webAppConfig = sc.getApplicationContext().getWebApplicationConfiguration();

  }

  public PrivilegedActionImpl(Servlet servlet, ServletConfig sci) {
    this.servlet = servlet;
    servletConfig = sci;
    type = INIT;
  }

  public PrivilegedActionImpl(Servlet servlet) {
    this.servlet = servlet;
    type = DESTROY;
  }

  public Object run() throws ServletException, IOException {
    switch (type) {
      case INIT: {
        long startupTime = -1;
        try {//Servlet.init() may throw ServletException
          if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
            startupTime = System.currentTimeMillis();
            Accounting.beginMeasure("PrivilegedActionImpl.initServlet", servlet.getClass());
          }//ACCOUNTING.start - END

        	servlet.init(servletConfig);
        } finally {
        	if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
            Accounting.endMeasure("PrivilegedActionImpl.initServlet");
            startupTime = System.currentTimeMillis() - startupTime;
            initStartupTime2.addAndGet(startupTime);
          }//ACCOUNTING.start - END
        }
        break;
      }
      case SERVICE:
        invoke(servlet, servletRequest, servletResponse, exitApplication,
            !webAppConfig.isProgrammaticSecurityAgainstRunAsIdentity() , ss);
        break;
      case DESTROY:
				servlet.destroy();
        break;
    }
    return null;
  }

}

