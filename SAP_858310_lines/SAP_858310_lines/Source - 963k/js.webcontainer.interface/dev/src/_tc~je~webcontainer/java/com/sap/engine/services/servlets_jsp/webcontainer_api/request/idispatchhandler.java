/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.webcontainer_api.request;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * A handler called in the context of a web module when dispatching a request.
 *
 * @author Violeta Georgieva
 * @version 7.10
 */
public interface IDispatchHandler {

  /**
   * Call back when using a IRequestDispatcher.dispatch() method.
   *
   * @param request  an initial request.
   * @param response an initial response.
   */
  public void service(ServletRequest request, ServletResponse response);

  /**
   * Call back when using a IRequestDispatcher.dispatch() method.
   */
  public void service();

}//end of interface
