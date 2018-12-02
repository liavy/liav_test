/*
 * Copyright (c) 2007 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.webcontainer_api.container;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WCEConfigurationException;

/**
 * Defines methods that WCE Provider can use to get restricted access to
 * more sensitive information from the Web Container, like write access to
 * the configuration and the web modules names that are currently in deployment process i.e. not committed data.
 *  
 * It exposes data specific to the associated web module and to the 
 * WCE Provider that uses it.
 *  
 * It has validity period and Web Container will invalidate it outside
 * the scope of the methods in which it is designed to be used. 
 * After it is invalidated by the Web Container 
 * it will not be possible to access the data through it. 
 * WCE Providers should not rely on first obtaining data from it at the time 
 * it was valid, keeping reference to this data and using it correctly later 
 * after this object is invalidated. 
 * 
 * If the WCE Provider wants to pass data back to Web Container after
 * the deploy completes, it should use the DeployInfo class. 
 * (@see com.sap.engine.services.servlets_jsp.webcontainer_api.container.DeployInfo)
 * 
 * @author Vera Buchkova
 * @author Violeta Georgieva
 * @version 7.10
 */
public interface IWebModuleDeployContext {
	
  /**
   * Returns this WCE provider's configuration for the underlying web module
   * opened in write access.
   * If such configuration does not exist, NULL will be returned.
   * 
   * <p>Note for this WCE web module configuration the followings:</p>
   * 
   * <ol>
   * <li>It is reserved for this WCE provider only and it exists only
   * for web modules in whose deployment this WCE provider participates.
   * </li>
   * <li>It is created at deploy time (or at update time if it does not exist yet) 
   * and is not removed during application update. 
   * It will be automatically removed by the deploy service when removing the 
   * application.
   * </li>
   * <li>It is the WCE's responsibility to maintain this configuration up to date,
   * e.g. by merging its contents with the new data after the application update.
   * </li>
   * <li>This configuration can be accessed for WRITE only during deploying/updating
   * the web module; in all other cases (which we recommend to be administration 
   * needs only) the configuration will be open for reading.
   * </li>
   * <li>Any attempts to obtain this configuration using this method outside the 
   * deploy/update will fail with WCEConfigurationException.
   * </li>
   * <li>Caller should not rely on keeping reference to the configuration and using 
   * it outside the deploy/update.
   * </li>
   * <li>It is only the Web Container's responsibility to close the configuration 
   * opened for WRITE and the WCE Provider should not close its parent or opened 
   * root configurations in these cases.</li>
   * <li>However in the other cases - when it is open for READ - it is the WCE 
   * Provider's responsibility to close it by closing the open root, e.g. using 
   * <code>wceConfig.getOpenRoot().close()</code>
   * </li>
   * </ol>
   *   		
   * @return the WCE provider's configuration for the passed application, or null 
   * if such configuration does not exist
   * 
   * @throws WCEConfigurationException when it is not possible to return the
   * WCE web module sub configuration opened for write access, for example if the underlying
   * object has been invalidated.
   */
  public Configuration getMyWebModuleConfigWrite() throws WCEConfigurationException;
  
  /**
   * Returns all web modules names that belongs to the WCE provider and are currently in 
   * deployment/update process for a given application i.e. not committed data.
   * Any attempts to obtain this information using this method outside the 
   * deployment/update process will fail with WCEConfigurationException.
   * For obtaining this information outside the deployment/update process 
   * <code>IWebContainerExtensionContext.getMyWebModules(String applicationName)</code>
   * must be used.
   * 
   * @return a string array with all web modules names that belongs to the WCE provider and 
   * are currently in deployment/update process for a given application i.e. not committed data.
   * @throws WCEConfigurationException when it is not possible to return the
   * WCE providers' web modules names, for example if the underlying object has been invalidated.
   */
  public String[] getMyWebModules() throws WCEConfigurationException;
}
