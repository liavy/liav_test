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
package com.sap.engine.services.servlets_jsp.server.runtime;

import java.io.IOException;
import java.security.PrivilegedActionException;
import java.util.Enumeration;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.httpserver.lib.ResponseCodes;
import com.sap.engine.services.httpserver.lib.Responses;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebServletException;
import com.sap.engine.services.servlets_jsp.server.lib.FilterUtils;
import com.sap.engine.services.servlets_jsp.server.lib.StaticResourceUtils;
import com.sap.engine.services.servlets_jsp.server.qos.RDResourceProvider;
import com.sap.engine.services.servlets_jsp.server.qos.RequestDispatcherConsumer;
import com.sap.engine.services.servlets_jsp.server.security.PrivilegedActionImpl;

/**
 * The FilterConfigImpl implements javax.servlet.FilterConfig
 *
 * @author Boby Kadrev
 * @version 4.0
 */
public class FilterChainImpl implements FilterChain {
  private Vector filters = new Vector();
  private Enumeration enFilters = null;
  private Servlet servlet = null;
  private Subject subject = null;
  private boolean aliasChanged = false;
  //Settings used to dispatch to static resources:
  private String jspFile = null;
  private ApplicationContext context = null;

  /**
   * Empty constructor.
   *
   */
  public FilterChainImpl() {
    super();
  }

  /**
   * Invoke the next filter in this chain, passing the specified request
   * and response.
   *
   * @param request The servlet request we are processing
   * @param response The servlet response we are creating
   */
  public void doFilter(ServletRequest request, ServletResponse response) throws ServletException, IOException {
    if (enFilters == null) {
      enFilters = filters.elements();
    }
    if (enFilters.hasMoreElements()) {
      Filter filter = (Filter) enFilters.nextElement();
      if (LogContext.getLocationRequestInfoServer().beInfo()) {
        LogContext.getLocationRequestInfoServer().infoT("Invoking filter " + filter);
      }
      filter.doFilter(request, response, this);
      return;
    }
	
    RDResourceProvider resourceProvider = ServiceContext.getServiceContext().getRDResourceProvider();
		if (servlet == null) { //apply filters to static resource
		  RequestDispatcherConsumer rdConsumer = new RequestDispatcherConsumer(RequestDispatcherConsumer.REQUEST_DISPATCHER_CONSUMER);
		  boolean isConsumed = true;
		  try {	       
	      rdConsumer.setId(resourceProvider.getConsumerType(context.getAliasName(), jspFile));	      
	      if (!resourceProvider.consume(rdConsumer)) {		                          
		      LogContext.getLocation(LogContext.LOCATION_QUALITY_OF_SERVICE_INTEGRATION).traceInfo("503 is returned for [" + context.getAliasName() + jspFile + "] resourse", context.getAliasName());
		      isConsumed = false;
		      FilterUtils.unWrapResponse(response).sendError(ResponseCodes.code_service_unavailable, Responses.mess16.replace("{URL}", context.getAliasName() + jspFile));
		    } else {
		      StaticResourceUtils.dispatchToResource(request, response, 
											! FilterUtils.unWrapResponse(response).isIncluded(), jspFile, context, aliasChanged);
		    }
		  } finally {
		    if (isConsumed) {		      
		      resourceProvider.release(rdConsumer);
		    }
		  }
		} else {
		  runServlet(request, response);
		}    
  }

  /**
   * Add a filter to the set of filters that will be executed in this chain.
   *
   * @param filter The Filter which be executed
   */
  public void addFilter(Filter filter) {
    if (filter == null) {
      return;
    }

    filters.add(filter);
  }

  /**
   * Sets a set of filters that will be executed in this chain (only if a filter
   * has not been executed)
   *
   * @param filterConfig Vector with Filter objects
   */
  protected void setFilters(Vector filterConfig) {
    if (enFilters == null && filterConfig != null) {
      filters = filterConfig;
    }
  }

  /**
   * Return number of filters in this chain.
   *
   * @return int  number of filters in this chain.
   */
  public int countFilters() {
    return filters.size();
  }

  /**
   * Sets the servlet at the end of this chain.
   */
  public void setServlet(Servlet srv, Subject subject) {
    servlet = srv;
    this.subject = subject;
  }
  
  /**
   * Sets flag if original alias is changed.
   */
  public void setAliasChanged(boolean flag) {
		this.aliasChanged = flag;
  }

  /**
   * Sets the properties needed to serve the static resource at the end of the chain.
   */
  public void setStaticResource(String jspFile, ApplicationContext context) {
		this.jspFile = jspFile;
		this.context = context;
  }

  /**
   * Starts the servlet at the end of this chain.
   * @deprecated
   */
  private void runServlet(ServletRequest sreq, ServletResponse sres) throws ServletException, IOException {    
    if (subject == null) {      
      if (servlet instanceof javax.servlet.SingleThreadModel) {
        synchronized (servlet) {
          try {
            String servletName = FilterUtils.unWrapRequest(sreq).getServletName();
            FilterUtils.unWrapRequest(sreq).markServiceStarted(servletName);
            
            //accounting-begin
            boolean accountingOn = ServiceContext.isAccountingEnabled();
            if (accountingOn) {
              Accounting.beginMeasure("Request in servlet [" + servletName + "]'s service method", servlet.getClass());
            }//accounting-begin
            
            servlet.service(sreq, sres);
                        
            if (accountingOn) {//accounting-end
              Accounting.endMeasure("Request in servlet [" + servletName + "]'s service method");
            }//accounting-end
          } finally {
            FilterUtils.unWrapRequest(sreq).markServiceFinished();
          }
        }
      } else {        
        try {
          String servletName = FilterUtils.unWrapRequest(sreq).getServletName();
          FilterUtils.unWrapRequest(sreq).markServiceStarted(servletName);
          
          //accounting-begin
          boolean accountingOn = ServiceContext.isAccountingEnabled();
          if (accountingOn) {
            Accounting.beginMeasure("Request in servlet [" + servletName + "]'s service method", servlet.getClass());
          }//accounting-begin
          
          servlet.service(sreq, sres);
                    
          if (accountingOn) {//accounting-end
            Accounting.endMeasure("Request in servlet [" + servletName + "]'s service method");
          }//accounting-end
        } finally {
          FilterUtils.unWrapRequest(sreq).markServiceFinished();
        }        
      }
    } else {
      try {        
        if (servlet instanceof javax.servlet.SingleThreadModel) {
          Subject.doAs(subject, new PrivilegedActionImpl(servlet, sreq, sres, false));
        } else {
          Subject.doAs(subject, new PrivilegedActionImpl(servlet, sreq, sres, false));
        }
      } catch (PrivilegedActionException e) {
        throw new WebServletException(WebServletException.Error_starting_servlet_as_privileged_action_at_filter_chain, new Object[]{servlet.getServletConfig().getServletName()}, e);
      }
    }
  }

  /**
   * Convert information about this chain to String.
   *
   * @return  String
   */
  public String toString() {
    StringBuffer sb = new StringBuffer("FilterChainImpl[");

    for (int i = 0; i < filters.size(); i++) {
      sb.append("filter=");
      sb.append(filters.elementAt(i));
      sb.append("; ");
    }

    sb.append("]");
    return (sb.toString());
  }
}

