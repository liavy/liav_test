/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.runtime.client;

import java.io.IOException;

import com.sap.engine.services.httpserver.lib.headers.MimeHeaders;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.tc.logging.Location;

/**
 * @author diyan-y
 */
public class HttpServletResponseFacadeWrapper extends HttpServletResponseFacade {

  private static Location traceLocation = LogContext.getLocationServletResponse();
  
  private HttpServletRequestFacadeWrapper requestFacadeWrapper = null;
  /**
   * @throws IOException
   */
  public HttpServletResponseFacadeWrapper() throws IOException {
    super();
    if (traceLocation.beDebug()) {
      traceDebug("HttpServletResponseFacadeWrapper", "new response is created");
    }
  }

  public void init(ApplicationContext applicationContext, HttpServletRequestFacadeWrapper requestFacadeWrapper, MimeHeaders responseHeaders) {
    if (traceLocation.beDebug()) {
      traceDebug("init", "new HttpServletResponseFacadeWrapper is created");
    }
    this.requestFacadeWrapper = requestFacadeWrapper;
    super.init(applicationContext, requestFacadeWrapper, responseHeaders);
  }
  
  public HttpServletRequestFacade getServletRequest() {
    if (traceLocation.beDebug()) {
      traceDebug("getServletRequest", "HttpServletResponseFacadeWrapper: "  + requestFacadeWrapper);
    }
    return requestFacadeWrapper;
  }
  
  public void resetInternal(boolean initial) throws IOException {
    requestFacadeWrapper = null;
    super.resetInternal(initial);
  }
}
