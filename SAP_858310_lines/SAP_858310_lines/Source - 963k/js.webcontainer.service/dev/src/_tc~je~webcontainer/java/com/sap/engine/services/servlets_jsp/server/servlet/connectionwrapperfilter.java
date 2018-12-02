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
package com.sap.engine.services.servlets_jsp.server.servlet;

import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletRequestFacadeWrapper;
import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletResponseFacadeWrapper;
import com.sap.engine.services.servlets_jsp.server.lib.FilterUtils;
import com.sap.engine.services.servlets_jsp.server.*;
import com.sap.engine.services.httpserver.interfaces.HttpParameters;

import javax.servlet.*;
import java.io.IOException;

public class ConnectionWrapperFilter implements Filter {
  public void init(FilterConfig filterConfig) {
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    HttpServletRequestFacadeWrapper newRequest = new HttpServletRequestFacadeWrapper();
    HttpServletResponseFacadeWrapper newResponse = new HttpServletResponseFacadeWrapper();

    HttpServletResponseFacadeWrapper unwrappedResponse = FilterUtils.unWrapResponse(response);

    newRequest.setContext(unwrappedResponse.getServletContext());
    newResponse.setContext(unwrappedResponse.getServletContext());
    newResponse.setServletName(unwrappedResponse.getServletName());
    newRequest.setServletName(FilterUtils.unWrapRequest(request).getServletName());
    HttpParameters requestAnalizer = unwrappedResponse.getHttpParameters();
    requestAnalizer = (HttpParameters)requestAnalizer.clone();
    newRequest.init(unwrappedResponse.getServletContext(), requestAnalizer, newResponse);
    newResponse.init(unwrappedResponse.getServletContext(), newRequest, requestAnalizer.getResponse().getHeaders());
    //newRequest.setCurrentServletName(unwrappedResponse.getServletName());

    filterChain.doFilter(newRequest, newResponse);
 // TODO - i024079 fix: check with web container developers
 //   ServiceContext.getServiceContext().getConnectionsContext().addSleepingConnection(requestAnalizer.getRequest().getClientId(), newResponse);
  }

  public void destroy() {

  }
}
