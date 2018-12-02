/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.servlets_jsp.server.application;

import com.sap.engine.services.httpserver.interfaces.HttpParameters;
import com.sap.engine.services.httpserver.lib.HttpCookie;
import com.sap.engine.services.servlets_jsp.server.runtime.client.ApplicationSession;
import com.sap.engine.session.Session;
import com.sap.engine.session.SessionFactory;
import com.sap.engine.session.SessionHolder;

/**
 * Author: georgi-s
 * Date: Jun 20, 2004
 */
public class SessionFactoryImpl implements SessionFactory {
  private SessionServletContext sessionContext;
  private ApplicationContext servletContextFacade;
  private SessionHolder holder;
  private HttpCookie cookie;
  private HttpParameters httpParameters;

  public SessionFactoryImpl(SessionServletContext servletContextFacade, ApplicationContext appContext, SessionHolder holder, HttpCookie cookie, HttpParameters httpParameters) {
    this.sessionContext = servletContextFacade;
    this.servletContextFacade = appContext;
    this.holder = holder;
    this.cookie = cookie;
    this.httpParameters = httpParameters;
  }

  public Session getSession(String sessionId) {
    ApplicationSession applicationSession = new ApplicationSession(sessionId, sessionContext.getSessionTimeout(), httpParameters.getRequest().getClientIP(), sessionContext.getAliasName());
    httpParameters.setDebugRequest(servletContextFacade.initializeDebugInfo(httpParameters, applicationSession));
    return applicationSession;
  }

}
