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

/**
 * This is the object that will allow dispatching a call into the
 * right web module context and providing the right request/response objects.
 * It has the same functionality as the well known servlet dispatcher
 * except that instead of calling the servlet it calls a given handler.
 * It's obtained from IWebModuleContext.getRequestDispatcher() method.
 *
 * @author Violeta Georgieva
 * @version 7.10
 */
public interface IRequestDispatcher {

  /**
   * This method allows dispatching a call into the right web module context and
   * providing the right request/response objects.
   * It has the same functionality as the well known servlet dispatcher
   * except that instead of calling the servlet it calls a given handler.
   *
   * @param handler the dispatch handler.
   */
  public void dispatch(IDispatchHandler handler);
}//end of interface
