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
package com.sap.engine.services.servlets_jsp.server.lib;

/*
 * Some utils for filters.
 *
 * @author Boby Kadrev
 * @version 4.0
 */

import com.sap.engine.services.httpserver.interfaces.properties.HttpProperties;
import com.sap.engine.services.httpserver.lib.protocol.HeaderNames;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
//import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletRequestFacade;
//import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletResponseFacade;
import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletRequestFacadeWrapper;
import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletResponseFacadeWrapper;


import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;

/*
 * Contains some static method for filters.
 *
 */
public class FilterUtils {

  /**
   * Unwraps the request until get HttpServletRequestFacade
   *
   * @param   wrapped  Wrapped request
   */
  public static HttpServletRequestFacadeWrapper unWrapRequest(ServletRequest wrapped) {
    if (wrapped instanceof HttpServletRequestFacadeWrapper) {
      return (HttpServletRequestFacadeWrapper) wrapped;
    }
    ServletRequest unWrapped = wrapped;
    while (unWrapped instanceof javax.servlet.ServletRequestWrapper) {
      unWrapped = ((ServletRequestWrapper) unWrapped).getRequest();
    }
    return (HttpServletRequestFacadeWrapper) unWrapped;
  }
  
  /**
   * Unwraps the response until get HttpServletResponseFacade
   *
   * @param   wrapped  Wrapped response
   */
  public static HttpServletResponseFacadeWrapper unWrapResponse(ServletResponse wrapped) {
    if (wrapped instanceof HttpServletResponseFacadeWrapper) {
      return (HttpServletResponseFacadeWrapper) wrapped;
    }
    ServletResponse unWrapped = wrapped;
    while (unWrapped instanceof javax.servlet.ServletResponseWrapper) {
      unWrapped = ((ServletResponseWrapper) unWrapped).getResponse();
    }
    return (HttpServletResponseFacadeWrapper) unWrapped;
  }
  
  /**
   * This method is central place for setting cache headers for static resources on webcontainer. The same logic shoud be applied on the http server too. 
   * There is different logic for dynamic resources - see DisableDynamicCaching property.
   * @param response - The response where the headers should be set. 
   * @param httpMajorVersion - for HTTP version 1.0 - it is 1.
   * @param httpMinorVersion - for HTTP version 1.0 - it is 0.
   * @param httpProperties - server http properties
   */
  public static void addCacheHeaders(HttpServletResponse response,int httpMajorVersion, int httpMinorVersion) {
    HttpProperties httpProperties = ServiceContext.getServiceContext().getHttpProvider().getHttpProperties();
    response.addHeader(HeaderNames.entity_header_cache_control, httpProperties.getCacheValidationTimeString());
    if (httpMajorVersion == 0 || (httpMajorVersion == 1 && httpMinorVersion == 0) ){
      response.addDateHeader(HeaderNames.entity_header_expires, System.currentTimeMillis()+ httpProperties.getCacheValidationTime() * 1000);
    }
  }

}

