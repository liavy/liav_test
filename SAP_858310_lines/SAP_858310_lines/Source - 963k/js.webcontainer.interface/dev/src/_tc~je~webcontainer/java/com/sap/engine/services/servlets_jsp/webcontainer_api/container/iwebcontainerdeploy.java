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
package com.sap.engine.services.servlets_jsp.webcontainer_api.container;

import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WebContainerExtensionDeploymentException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WebContainerExtensionWarningException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.module.IWebModule;

/**
 * Handles web module deployment events.
 *
 * @author Maria Jurova
 * @version 7.10
 */
public interface IWebContainerDeploy {

  /**
   * This method is call on the end of the web module deployment before returning application deployment information,
   * on one server node which is processing the actual deployment
   * (for this deployment any kind of events will not be called on the other server nodes).
   * Web container extension can return here file names which will be added to the public and
   * private class loaders during startup of the application,
   * public and private references to the server components,
   * public and private resource references and resource references that web module provides.
   * Throwing WebContainerExtensionDeploymentException here means that deployment fails 
   * roll back deploy of the whole application will be called.
   *
   * @param module        this is the web module for which deploy event is called.
   * @param rootDirectory the abstract path to the root directory where .war file is extracted.
   * @return the deployment information for this web module.
   * @throws WebContainerExtensionDeploymentException
   *          thrown if deployment fails - roll back deploy of the whole application will be called.
   * @throws WebContainerExtensionWarningException
   *          thrown if errors of little importance occur during deployment. The process will continue.
   */
  public DeployInfo onDeploy(IWebModule module, String rootDirectory) throws WebContainerExtensionDeploymentException, WebContainerExtensionWarningException;

  /**
   * This method is called in the following two cases:
   * <br><ol><li>Before the web module is about to be removed.
   * On one server node, which is processing the actual removal
   * (for this removal any kind of events will not be called on the other server nodes).
   * <br><li>When roll back deploy is called. There are three cases for roll back deployment:
   * <br><ul><li>onDeploy() method is passed, but after it roll back deploy is called.
   * <br><li>onDeploy() method has thrown WebContainerExtensionDeploymentException.
   * <br><li>Before calling onDeploy() method, roll back deploy is called.
   * </ul></ol>
   *
   * @param module this is the web module for which remove event is called.
   * @throws WebContainerExtensionDeploymentException
   *          thrown if very serious errors occur while removing this application. The process will continue.
   * @throws WebContainerExtensionWarningException
   *          thrown if errors of little importance occur during remove. The process will continue.
   */
  public void onRemove(IWebModule module) throws WebContainerExtensionDeploymentException, WebContainerExtensionWarningException;

}//end of interface
