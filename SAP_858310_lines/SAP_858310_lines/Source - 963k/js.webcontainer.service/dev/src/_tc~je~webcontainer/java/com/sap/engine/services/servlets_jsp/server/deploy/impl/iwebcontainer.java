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
package com.sap.engine.services.servlets_jsp.server.deploy.impl;

import java.io.File;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletContext;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.deploy.container.WarningException;

/**
 * @author Maria Jurova
 * @version 7.10
 */
public interface IWebContainer {

	/**
	 * The method is called at the end of WebContainer deploy() method
   * on one server node which is processing the actual deployment.
   * 
	 * @param applicationName
	 * @param aliases
	 * @param appRoots
	 * @param isUpdate
	 * @param appGlobalProps
	 * @param servlet_jspConfig the WebContainer's subconfiguration for the given application
	 * @return Files' names which will be loaded with the ApplicationLoader during startup of the application.
	 * @throws DeploymentException throwing DeploymentException means that deployment fails.
	 */
  public DeployInfoExtension deploy(String applicationName, String[] aliases, File[] appRoots, 
  		boolean isUpdate, Properties appGlobalProps, Configuration servlet_jspConfig) throws DeploymentException;

  /**
   * This method is called during initial server startup and when notifying for deploy/update.
   *
   * @param applicationName
   * @param aliases
   * @param appRoots
   * @param wceInDeployPerModule	maps the web aliases to Vector objects of WCE 
   * 	names that participated in the deploy of the <code>applicationName</code>
   * 	/the web module of each of the <code>aliases</code> 
   */
  public void appDeployedButNotStarted(String applicationName, String[] aliases, File[] appRoots, Hashtable wceInDeployPerModule);

  /**
   * This method is called before the module is about to be removed/undeployed from the web container.
   *
   * @param applicationName
   * @param aliases
   * @param isUpdate
   * @throws DeploymentException
   * @throws WarningException
   */
  public void remove(String applicationName, String[] aliases, boolean isUpdate, Configuration servlet_jspConfig) throws DeploymentException, WarningException;

  /**
   * This method is called when notifying for remove.
   *
   * @param applicationName
   * @param aliases
   */
  public void notifyRemove(String applicationName, String[] aliases);

  /**
   * The method is called when the specified module has been started.
   *
   * @param applicationName
   * @param aliases
   * @param servletContexts
   * @param publicClassloader
   * @throws DeploymentException
   * @throws WarningException
   */
  public void start(String applicationName, String[] aliases, ServletContext[] servletContexts, ClassLoader publicClassloader) throws DeploymentException, WarningException;

  /**
   * The method is called when a web container module is about to be stopped.
   *
   * @param applicationName
   * @param aliases
   * @throws WarningException
   */
  public void stop(String applicationName, String[] aliases) throws WarningException;

  /**
   * @throws WarningException
   * @deprecated
   */
  public void allApplicationsStarted() throws WarningException;

  /**
   * Removes the web module context from the runtime struction,
   * after that it will not be returned directly from the 
   * IWebContainerExtensionContext's method getWebModuleContext(String).
   * 
   * @param alias
   */
  public void removeWebModuleContextFromCache(String alias);
} //end of interface
