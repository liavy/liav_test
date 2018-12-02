/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.servlets_jsp.server.runtime.client;

import com.sap.engine.session.SessionHolder;
import com.sap.engine.session.SessionFactory;
import com.sap.engine.session.Session;
import com.sap.engine.services.servlets_jsp.server.application.SessionServletContext;
import com.sap.engine.services.httpserver.interfaces.HttpParameters;

/**
 * Author: georgi-s
 * Date: May 10, 2004
 */
public class SessionFactoryImpl implements SessionFactory {
//  private SessionHolder holder;
  private HttpServletRequestImpl request;

  public SessionFactoryImpl(HttpServletRequestImpl request, SessionHolder holder) {
    this.request = request;
//    this.holder = holder;
  }

  public Session getSession(String sessionId) {
    SessionServletContext sessionServletContext = request.context.getSessionServletContext();
    HttpParameters httpParameters = request.getHttpParameters();
    ApplicationSession session = new ApplicationSession(sessionId, sessionServletContext.getSessionTimeout(),
            httpParameters.getRequest().getClientIP(), request.context.getAliasName());
            httpParameters.setDebugRequest(request.context.initializeDebugInfo(httpParameters, session));
    httpParameters.setDebugRequest(request.context.initializeDebugInfo(httpParameters, session));
    return session;
  }
}
