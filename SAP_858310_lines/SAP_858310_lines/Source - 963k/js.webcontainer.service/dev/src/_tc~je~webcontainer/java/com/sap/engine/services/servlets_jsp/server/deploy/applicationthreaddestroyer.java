/*
 * Copyright (c) 2002-2009 by SAP Labs Bulgaria AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */

package com.sap.engine.services.servlets_jsp.server.deploy;

import java.util.*;

import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.servlets_jsp.server.exceptions.SecuritySessionException;
import com.sap.engine.services.servlets_jsp.server.runtime.client.ApplicationSession;
import com.sap.engine.services.servlets_jsp.server.runtime.ServletConfigImpl;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.*;
import com.sap.engine.services.servlets_jsp.lib.jspruntime.JspApplicationContextImpl;
import com.sap.engine.interfaces.resourcecontext.ResourceContext;
import com.sap.engine.session.util.SessionEnumeration;
import com.sap.engine.system.ThreadWrapper;
import com.sap.tc.logging.Location;

import javax.servlet.jsp.JspFactory;

/**
 *
 * Destroy all web application components in application thread
 *
 * @author Violeta Uzunova
 * @version 6.30
 */
public class ApplicationThreadDestroyer implements Runnable {
  private static Location currentLocation = Location.getLocation(ApplicationThreadDestroyer.class);
  private static Location traceLocation = LogContext.getLocationDeploy();
  private ApplicationContext scf = null;
  private Thread currentThread = null;
  private ClassLoader threadLoader = null;
  private SessionEnumeration sessionEnumeration = null;

  public ApplicationThreadDestroyer(ApplicationContext scf) {
    this.scf = scf;
  }

  /**
   * When an object implementing interface <code>Runnable</code> is used
   * to create a thread, starting the thread causes the object's
   * <code>run</code> method to be called in that separately executing
   * thread.
   * <p>
   * The general contract of the method <code>run</code> is that it may
   * take any action whatsoever.
   *
   * @see     Thread#run()
   */
  public void run() {
    ResourceContext resourceContext = null;
    try {
      if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure("ApplicationThreadDestroyer", ApplicationThreadDestroyer.class);
      }//ACCOUNTING.start - END
      
      ThreadWrapper.pushTask("Destroying web application's components", ThreadWrapper.TS_PROCESSING);
      currentThread = Thread.currentThread();
      threadLoader = currentThread.getContextClassLoader();
      sessionEnumeration = scf.getSessionServletContext().getSession().enumerateSessions();
      currentThread.setContextClassLoader(scf.getClassLoader());
      boolean beInfo = traceLocation.beInfo();
      if (beInfo) {
      	traceLocation.infoT("Start destroying web components ...");
      }
      resourceContext = scf.enterResourceContext();
      if (beInfo) {
      	traceLocation.infoT("Start destroying servlets ...");
      }
      scf.getWebComponents().destroyServlets();
      if (beInfo) {
      	traceLocation.infoT("Servlets successfully destroyed.");
      	traceLocation.infoT("Start destroying jsp servlets ...");
      }
      scf.getWebComponents().destroyJsps();
      if (beInfo) {
      	traceLocation.infoT("JSP servlets successfully destroyed.");
      	traceLocation.infoT("Start destroying filters ...");
      }
      scf.getWebComponents().destroyFilters();
      if (beInfo) {
      	traceLocation.infoT("Filters successfully destroyed.");
      	traceLocation.infoT("Start destroying HTTP sessions ...");
      }      
      scf.getWebEvents().sessionsWillPassivate(sessionEnumeration);
      sessionEnumeration.reset();
      removeSecuritySessions(sessionEnumeration);
      if (beInfo) {
      	traceLocation.infoT("HTTP sessions successfully destroyed.");
      }
      //Remove cached JspApplicationContext
      //JspApplicationContextImpl.removeFromCache(scf.getServletContext());

      JspApplicationContextImpl jspAppCtx = ((JspApplicationContextImpl) JspFactory.getDefaultFactory().getJspApplicationContext(scf.getServletContext()));
      jspAppCtx.removeFromCache(scf.getServletContext());
      jspAppCtx = null;


      scf.getWebEvents().contextDestroyed();
      if (beInfo) {
      	traceLocation.infoT("Web components destroyed.");
      }
    } finally {
      try {
        sessionEnumeration.release();
        
        //Accounting here for CSN 737272 2009 : 3 min in removeActiveSessions()
        if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
          Accounting.beginMeasure("ApplicationThreadDestroyer.removeActiveSessions", scf.getSessionServletContext().getSession().getClass());
        }//ACCOUNTING.end - END
        
        scf.getSessionServletContext().getSession().removeActiveSessions();
        
        if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
          Accounting.endMeasure("ApplicationThreadDestroyer.removeActiveSessions");
        }//ACCOUNTING.end - END
        
        scf.getSessionServletContext().getSession().setDomainAttribute(ServletConfigImpl.SERVLET_CONTEXT, "");
        scf.getSessionServletContext().getSession().setDomainAttribute(ServletConfigImpl.SERVLET_INIT_PARAMETERS, "");
        currentThread.setContextClassLoader(threadLoader);
        scf.exitResourceContext(resourceContext);
      } catch (Exception e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000024",
            "Error in finalizing resources in starting web application [{0}].", new Object[]{scf.getAliasName()}, e, null, null);
      } finally {
        ThreadWrapper.popTask();
        
        if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
          Accounting.endMeasure("ApplicationThreadDestroyer");
        }//ACCOUNTING.end - END
      }
    }
  }

  private void removeSecuritySessions(Enumeration sessEn) {
    while (sessEn.hasMoreElements()) {
      try {
        scf.getSessionServletContext().getPolicyDomain().removeMe((ApplicationSession) sessEn.nextElement());
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000025",
            "Error in removing a security session while stopping the web application [{0}].", new Object[]{scf.getAliasName()}, e, null, null);
      }
    }
    try {
			ServiceContext.getServiceContext().getWebContainerPolicy().destroyDomain(scf.getWebApplicationConfiguration().getDomainName(), scf.getAliasName());
    } catch (SecuritySessionException se) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000073",
          "Error in destroying security sessions.", se, null, null);
    }
  }
}
