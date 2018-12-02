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
package com.sap.engine.services.servlets_jsp.webcontainer_api.container;

import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WebContainerExtensionDeploymentException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WebContainerExtensionWarningException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.module.IWebModuleContext;

/**
 * Handles web module life cycle events.
 *
 * @author Maria Jurova
 * @version 7.10
 */
public interface IWebContainerLifecycle {

  /**
   * This method is called when an application context of the web module is initialized in web container,
   * on these server nodes where the application will be started.
   * Throwing WebContainerExtensionDeploymentException here means that the start of the application fails;
   * the application will remain in STOPPED state on these server nodes.
   *
   * @param moduleContext the web module context for which start event is called.
   * @throws WebContainerExtensionDeploymentException
   *          throwing WebContainerExtensionDeploymentException means
   *          that the start of the application fails, it will remain in STOPPED state.
   * @throws WebContainerExtensionWarningException
   *          thrown if errors of little importance occur while preparing to start this application. The process will continue.
   */
  public void onStart(IWebModuleContext moduleContext) throws WebContainerExtensionDeploymentException, WebContainerExtensionWarningException;

  /**
   * This method is called in one of the following two cases:
   * <br><ol><li>On each server node before application context of the web module is about to be destroyed.
   * <br><li>When roll back start is called. There are three cases for roll back start:
   * <br><ul><li>onStart() method is passed, but after it roll back start is called.
   * <br><li>onStart() method has thrown WebContainerExtensionDeploymentException.
   * <br><li>Before calling onStart() method roll back start is called.
   * </ul></ol>
   *
   * @param moduleContext the web module context for which stop event is called.
   * @throws WebContainerExtensionWarningException
   *          thrown if errors of little importance occur while stopping this application. The process will continue.
   */
  public void onStop(IWebModuleContext moduleContext) throws WebContainerExtensionWarningException;

}//end of interface
