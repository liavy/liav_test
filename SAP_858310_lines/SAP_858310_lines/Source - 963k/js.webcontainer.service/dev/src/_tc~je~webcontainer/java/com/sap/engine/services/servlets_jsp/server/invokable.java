/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import java.security.PrivilegedActionException;
import javax.security.auth.*;

import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.httpserver.lib.protocol.HeaderNames;
import com.sap.engine.services.httpserver.server.memory.impl.RequestMemoryReportManager;
import com.sap.engine.services.servlets_jsp.server.lib.FilterUtils;
import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletRequestFacadeWrapper;
import com.sap.engine.services.servlets_jsp.server.security.PrivilegedActionImpl;
import com.sap.engine.interfaces.security.SecuritySession;

public class Invokable extends HttpServlet {

  protected void invoke(Servlet servlet, ServletRequest request, ServletResponse response,
                        Subject subject, boolean exitApplication)
    throws PrivilegedActionException, ServletException, IOException {
    try {
      HttpServletRequestFacadeWrapper requestFacade = FilterUtils.unWrapRequest(request);
      String servletName = requestFacade.getServletName();
      requestFacade.markServiceStarted(servletName);
      if (subject == null) {
        invoke(servlet, request, response, exitApplication, false, null);
      } else {
        if (LogContext.getLocationRequestInfoServer().beDebug()) {
          LogContext.getLocationRequestInfoServer().debugT(
            "Invoking servlet [" + servlet + "] with subject [" + subject + "].");
        }
        try {
          if (requestFacade.isStatisticTraceEnabled()) {
            RequestMemoryReportManager.getInstance().startIntermediateSection(requestFacade.getID(), 
                requestFacade.getApplicationContext().getApplicationName() + "/" + servletName);
            FilterUtils.unWrapResponse(response).setIntHeader(HeaderNames.propriatory_sap_request_id, requestFacade.getID());
          }
        } catch (Exception e) {
          if (LogContext.getLocationRequestInfoServer().beDebug()) {
            LogContext.getLocationRequestInfoServer().debugT(
                (new StringBuilder()).append("Cannot start intermediate section Subject.doAs[").
                append(servletName).append("] for request ID[").append(requestFacade.getID()).append("].").toString());
          }
        }
        try {
          Subject.doAs(subject, new PrivilegedActionImpl(servlet, request, response, exitApplication));
        } finally {
          try {
            if (requestFacade.isStatisticTraceEnabled()) {
              RequestMemoryReportManager.getInstance().stopIntermediateSection(requestFacade.getID());
            } 
          } catch (Exception e) {
            if (LogContext.getLocationRequestInfoServer().beDebug()) {
              LogContext.getLocationRequestInfoServer().debugT(
                  (new StringBuilder()).append("Cannot stop intermediate section Subject.doAs[").
                  append(servletName).append("] for request ID[").append(requestFacade.getID()).append("].").toString());
            }
          } 
        }
      }
    } finally {
      FilterUtils.unWrapRequest(request).markServiceFinished();
    }
  }

  protected void invoke(Servlet servlet, ServletRequest request, ServletResponse response, boolean exitApplication, boolean isInDoAs, SecuritySession ss)
    throws ServletException, IOException {
    try {
      HttpServletRequestFacadeWrapper requestFacade = FilterUtils.unWrapRequest(request);
      if (isInDoAs) {
        requestFacade.setUserAuthSessionInRunAS(true, ss);
      }
      String servletName = requestFacade.getServletName();
      requestFacade.markServiceStarted(servletName);
      if (servlet instanceof javax.servlet.SingleThreadModel) {
        synchronized (servlet) {
          if (LogContext.getLocationRequestInfoServer().beDebug()) {
            LogContext.getLocationRequestInfoServer().debugT(
              "Invoking servlet [" + servlet + "].");
          }
          try {
            if (requestFacade.isStatisticTraceEnabled()) {
              RequestMemoryReportManager.getInstance().startIntermediateSection(requestFacade.getID(), 
                  "[" + requestFacade.getApplicationContext().getApplicationName() + "]/[" + servletName + "]");
              FilterUtils.unWrapResponse(response).setIntHeader(HeaderNames.propriatory_sap_request_id, requestFacade.getID());
            }
          } catch (Exception e) {
            if (LogContext.getLocationRequestInfoServer().beDebug()) {
              LogContext.getLocationRequestInfoServer().debugT(
                  (new StringBuilder()).append("Cannot start intermediate section [").
                  append(servletName).append("] for request ID[").append(requestFacade.getID()).append("].").toString());
            }
          }
          boolean accountingOn = ServiceContext.isAccountingEnabled();
          try {
            if (accountingOn) {//accounting-begin
              Accounting.beginMeasure("Request in servlet [" + servletName + "]'s service method", servlet.getClass());
            }//accounting-begin
            servlet.service(request, response);
          } finally {            
            if (accountingOn) {//accounting-end
              Accounting.endMeasure("Request in servlet [" + servletName + "]'s service method");
            }//accounting-end
            try {
              if (requestFacade.isStatisticTraceEnabled()) {
                RequestMemoryReportManager.getInstance().stopIntermediateSection(requestFacade.getID());
              } 
            } catch (Exception e) {
              if (LogContext.getLocationRequestInfoServer().beDebug()) {
                LogContext.getLocationRequestInfoServer().debugT(
                    (new StringBuilder()).append("Cannot stop intermediate section [").
                    append(servletName).append("] for request ID[").append(requestFacade.getID()).append("].").toString());
              }
            }
          }
        }
      } else {
        if (LogContext.getLocationRequestInfoServer().beDebug()) {
          LogContext.getLocationRequestInfoServer().debugT(
            "Invoking servlet [" + servlet + "].");
        }
        try {
          if (requestFacade.isStatisticTraceEnabled()) {
            RequestMemoryReportManager.getInstance().startIntermediateSection(requestFacade.getID(), 
                "[" + requestFacade.getApplicationContext().getApplicationName() + "]/[" + servletName + "]");
            FilterUtils.unWrapResponse(response).setIntHeader(HeaderNames.propriatory_sap_request_id, requestFacade.getID());
          }
        } catch (Exception e) {
          if (LogContext.getLocationRequestInfoServer().beDebug()) {
            LogContext.getLocationRequestInfoServer().debugT(
                (new StringBuilder()).append("Cannot start intermediate section [").
                append(servletName).append("] for request ID[").append(requestFacade.getID()).append("].").toString());
          }
        }
        boolean accountingOn = ServiceContext.isAccountingEnabled();
        try {
          if (accountingOn) {//accounting-begin
            Accounting.beginMeasure("Request in servlet [" + servletName + "]'s service method", servlet.getClass());
          }//accounting-begin
          servlet.service(request, response);
        } finally {          
          if (accountingOn) {//accounting-end
            Accounting.endMeasure("Request in servlet [" + servletName + "]'s service method");
          }//accounting-end
          try {
            if (requestFacade.isStatisticTraceEnabled()) {
              RequestMemoryReportManager.getInstance().stopIntermediateSection(requestFacade.getID());
            } 
          } catch (Exception e) {
            if (LogContext.getLocationRequestInfoServer().beDebug()) {
              LogContext.getLocationRequestInfoServer().debugT(
                  (new StringBuilder()).append("Cannot stop intermediate section [").
                  append(servletName).append("] for request ID[").append(requestFacade.getID()).append("].").toString());
            }
          } 
        }
      }
    } finally {
      try {
        if (isInDoAs) {
          FilterUtils.unWrapRequest(request).setUserAuthSessionInRunAS(false, ss);
        }
      } finally {
        FilterUtils.unWrapRequest(request).markServiceFinished();
      }
    }
  }

  /**
   * @param servlet
   * @param request
   * @param response
   * @param subject
   * @param exitApplication
   * @throws PrivilegedActionException
   * @throws ServletException
   * @throws IOException
   * @deprecated
   */
  protected void invoke(SingleThreadModel servlet, ServletRequest request, ServletResponse response,
                        Subject subject, boolean exitApplication)
    throws PrivilegedActionException, ServletException, IOException {
    try {
      String servletName = FilterUtils.unWrapRequest(request).getServletName();
      FilterUtils.unWrapRequest(request).markServiceStarted(servletName);
      if (subject == null) {
        synchronized (servlet) {
          if (LogContext.getLocationRequestInfoServer().beDebug()) {
            LogContext.getLocationRequestInfoServer().debugT(
              "Invoking servlet [" + servlet + "].");
          }
          //accounting-begin
          boolean accountingOn = ServiceContext.isAccountingEnabled();
          if (accountingOn) {
            Accounting.beginMeasure("Request in servlet [" + servletName + "]'s service method", servlet.getClass());
          }//accounting-begin
          
          ((Servlet) servlet).service(request, response);
          
          if (accountingOn) {//accounting-end
            Accounting.endMeasure("Request in servlet [" + servletName + "]'s service method");
          }//accounting-end
        }
      } else {
        if (LogContext.getLocationRequestInfoServer().beDebug()) {
          LogContext.getLocationRequestInfoServer().debugT(
            "Invoking servlet [" + servlet + "] with subject [" + subject + "].");
        }
        Subject.doAs(subject, new PrivilegedActionImpl((Servlet) servlet, request, response, exitApplication));
      }
    } finally {
      FilterUtils.unWrapRequest(request).markServiceFinished();
    }
  }
}
