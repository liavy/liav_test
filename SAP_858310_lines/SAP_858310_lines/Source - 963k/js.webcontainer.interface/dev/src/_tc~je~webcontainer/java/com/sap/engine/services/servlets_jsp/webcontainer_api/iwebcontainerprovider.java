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
package com.sap.engine.services.servlets_jsp.webcontainer_api;

import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WebContainerExtensionDeploymentException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.extension.IWebContainerExtension;

/**
 * Entry point for registering/unregistering web container extensions (WCE providers).
 *
 * @author Maria Jurova
 * @author Violeta Georgieva
 * @version 7.10
 */
public interface IWebContainerProvider {

  /**
   * Registers an web container extension.
   * During this registration IWebContainerExtension.init(IWebContainerExtensionContext context) method will be called
   * and after this all applications that belong to this WCE provider will be started
   * (exceptions of this rule are applications which have "lazy" start up mode or applications which are stopped by default).
   * If WCE provider's name is <code>null</code> or descriptor name is <code>null</code> an exception will be thrown.
   * If there has been already registered another one WCE provider with the same name an exception will be thrown.
   *
   * @param webContainerExtension     this is WCE provider's implementation of IWebContainerExtension.
   * @param webContainerExtensionName this is the unique WCE provider's name.
   * @param descriptorName            the descriptor name which this WCE provider is interested for.
   *                                  WCE will listen only for events connected with this descriptor name.
   * @throws WebContainerExtensionDeploymentException
   *          thrown when the registration fails. This can happened in the following cases:
   *          <br><ul><li>IWebContainerExtension.init(IWebContainerExtensionContext context) throws an exception
   *          <li>WCE provider's name is <code>null</code> or descriptor name is <code>null</code>
   *          <li>There has been already registered another one WCE provider with the same name.
   *          </ul>
   */
  public void registerWebContainerExtension(IWebContainerExtension webContainerExtension, String webContainerExtensionName, String descriptorName) throws WebContainerExtensionDeploymentException;

  /**
   * Registers an web container extension.
   * During this registration IWebContainerExtension.init(IWebContainerExtensionContext context) method will be called
   * and after this all applications that belong to this WCE provider will be started
   * (exceptions of this rule are applications which have "lazy" start up mode or applications which are stopped by default).
   * If WCE provider's name is <code>null</code> or descriptors names are <code>null</code> an exception will be thrown.
   * If there has been already registered another one WCE provider with the same name an exception will be thrown.
   *
   * @param webContainerExtension     this is WCE provider's implementation of IWebContainerExtension.
   * @param webContainerExtensionName this is the unique WCE provider's name.
   * @param descriptorNames           the descriptor names which this WCE provider is interested for.
   *                                  WCE will listen only for events connected with these descriptor names.
   * @throws WebContainerExtensionDeploymentException
   *          thrown when the registration fails. This can happened in the following cases:
   *          <br><ul><li>IWebContainerExtension.init(IWebContainerExtensionContext context) throws an exception
   *          <li>WCE provider's name is <code>null</code> or descriptor name is <code>null</code>
   *          <li>There has been already registered another one WCE provider with the same name.
   *          </ul>
   */
  public void registerWebContainerExtension(IWebContainerExtension webContainerExtension, String webContainerExtensionName, String[] descriptorNames) throws WebContainerExtensionDeploymentException;

  /**
   * Unregisters an web container extension.
   * During this unregistration all applications that belong to this WCE provider will be stopped
   * and after this IWebContainerExtension.destroy() method will be called.
   *
   * @param webContainerExtensionName the unique name for this WCE provider.
   */
  public void unregisterWebContainerExtension(String webContainerExtensionName);

}//end of interface
