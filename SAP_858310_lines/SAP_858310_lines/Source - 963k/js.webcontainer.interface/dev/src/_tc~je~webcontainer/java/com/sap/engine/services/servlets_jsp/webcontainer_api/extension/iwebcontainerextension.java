/*
 * Copyright (c) 2004-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.webcontainer_api.extension;

import com.sap.engine.services.servlets_jsp.webcontainer_api.container.IWebContainerDeploy;
import com.sap.engine.services.servlets_jsp.webcontainer_api.container.IWebContainerLifecycle;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WebContainerExtensionDeploymentException;

/**
 * Defines an extension of the web container.
 * It brings life cycle and deployment handlers.
 *
 * @author Maria Jurova
 * @version 7.10
 */
public interface IWebContainerExtension {

  /**
   * Determines the handler for all web container modules that have a life cycle.
   * One extension may be able to listen to all web container deployables and decide to perform some actions.
   *
   * @return an implementation of the deployment handler.
   *         This object must remain the same in the whole container life cycle.
   */
  public IWebContainerDeploy getWebDeployHandler();

  /**
   * Determines the handler for all web container modules that have a life cycle.
   * One extension may be able to listen to all web container deployables and decide to perform some actions.
   *
   * @return an implementation of the life cycle handler.
   *         This object must remain the same in the whole container life cycle.
   */
  public IWebContainerLifecycle getWebLifecycleHandler();

  /**
   * Called to initialize the web container extension during its registration and
   * to pass on a call-back interface from the web container.
   *
   * @param context a call-back interface from the web container.
   * @throws WebContainerExtensionDeploymentException
   *          thrown when the initialization of the web container extension fails.
   */
  public void init(IWebContainerExtensionContext context) throws WebContainerExtensionDeploymentException;

  /**
   * End of operation for a web container extension.
   * Called to destroy the web container extension during its unregistration or
   * during stopping web container.
   */
  public void destroy();

}//end of interface
