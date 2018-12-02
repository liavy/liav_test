package com.sap.engine.services.servlets_jsp.server.runtime.client;

import com.sap.engine.services.httpserver.interfaces.HttpParameters;
import com.sap.engine.services.servlets_jsp.server.application.SessionServletContext;
import com.sap.engine.session.CreateException;
import com.sap.engine.session.Session;
import com.sap.engine.session.SessionFactory;
import com.sap.engine.session.SessionHolder;

/**
 * @author diyan-y
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SessionFactoryWrapper  implements SessionFactory {

  private RequestContext requestContext;
  
  public SessionFactoryWrapper(RequestContext requestContext, SessionHolder holder) {
    this.requestContext = requestContext;
//    this.holder = holder;
  }
  
  /* (non-Javadoc)
   * @see com.sap.engine.session.SessionFactory#getSession(java.lang.String)
   */
  public Session getSession(String sessionId) throws CreateException {
    SessionServletContext sessionServletContext = requestContext.getApplicationContext().getSessionServletContext();
    HttpParameters httpParameters = requestContext.getHttpParameters();
    ApplicationSession session = new ApplicationSession(sessionId, sessionServletContext.getSessionTimeout(),
            httpParameters.getRequest().getClientIP(), requestContext.getApplicationContext().getAliasName());
            httpParameters.setDebugRequest(requestContext.getApplicationContext().initializeDebugInfo(httpParameters, session));
    httpParameters.setDebugRequest(requestContext.getApplicationContext().initializeDebugInfo(httpParameters, session));
    return session;
  }

}
