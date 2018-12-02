/**
 * Copyright (c) 2002 by SAP Labs Bulgaria.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime.soaphttp;

import javax.servlet.http.HttpServletResponse;

/**
 *   Encapsulates the HTTP transport specific data.
 * @author       Dimiter Angelov
 * @version      6.30
 */
public class HTTPTransport extends com.sap.engine.interfaces.webservices.runtime.Transport {

  private javax.servlet.http.HttpServletRequest request;
  private javax.servlet.http.HttpServletResponse response;

  public HTTPTransport(String id, javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) {
    super(id);
    this.request = request;
    this.response = response;
  }

  public HTTPTransport() {
  }

  public void setRequest(javax.servlet.http.HttpServletRequest request) {
    this.request = request;
  }

  public void setResponse(javax.servlet.http.HttpServletResponse response) {
    this.response = response;
  }

  public javax.servlet.http.HttpServletRequest getRequest() {
    return request;
  }

  public String getTransportID() {
    return null;
  }

  public javax.servlet.http.HttpServletResponse getResponse() {
    return response;
  }

  public void clear() {
    super.clear();
    this.request = null;
    this.response = null;
  }

  public boolean sendServerError(Throwable thr) {
    if(response != null) {
      try {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, thr.getLocalizedMessage());
      } catch(Exception exc) {
        throw new RuntimeException(exc);
      }
      return(true);
    }
    return false;
  }

}

