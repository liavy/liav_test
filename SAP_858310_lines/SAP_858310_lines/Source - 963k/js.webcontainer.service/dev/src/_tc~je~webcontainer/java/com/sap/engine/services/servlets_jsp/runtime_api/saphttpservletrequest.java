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
package com.sap.engine.services.servlets_jsp.runtime_api;

import javax.servlet.http.HttpServletRequest;

/**
 * Defines an object that extends the <code>HttpServletRequest</code> interface
 * to provide additional request functionality and information for HTTP servlets. 
 */
public interface SapHttpServletRequest extends HttpServletRequest {
  /**
   * Removes header with given name from the request if exist
   * 
   * @param name the name of the header to remove
   */
  public void removeHeader(String name);
  
  /**
   * Gets an unique identifier of the request
   * 
   * @return an <code>int</code> that uniquely identifies the request
   */
  public int getID();
  
  /**
   * Checks whether resource consumption statistic for this request is started.
   * @return true, if the resource consumption statistic is started.
   */
  public boolean isStatisticTraceEnabled(); 
  
  /**
   * Notifies the Web Container to artificially extend the lifetime of the
   * request and response objects after completion of the client request processing.
   * The request and response are available for threads holding references to them.
   * The response is flushed to the client and the client connection is closed.
   * The timeout parameter defines the time for which the request and response are preserved.
   * If this method is invoked by multiple threads the biggest timeout is considered to be the actual
   * preservation timeout.
   *        
   * 
   * @param timeout set the preservation timeout
   */
  public void setAutoCompleteOff(long timeout);
  
}
