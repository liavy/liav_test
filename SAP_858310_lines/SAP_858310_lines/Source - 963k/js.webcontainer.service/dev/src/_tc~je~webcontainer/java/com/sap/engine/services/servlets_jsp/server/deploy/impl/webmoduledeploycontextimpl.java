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
package com.sap.engine.services.servlets_jsp.server.deploy.impl;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.server.deploy.util.ConfigurationUtils;
import com.sap.engine.services.servlets_jsp.server.deploy.util.Constants;
import com.sap.engine.services.servlets_jsp.webcontainer_api.container.IWebModuleDeployContext;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WCEConfigurationException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.module.IWebModule;

/**
 * Implementation of IWebModuleDeployContext interface,
 * encapsulating the creation of the WCE web module sub configuration
 * and having additional method that allows the Web Container to
 * invalidate it in order to prevent future usage.
 *
 * @author Vera Buchkova
 * @author Violeta Georgieva
 * @version 7.10
 */
public class WebModuleDeployContextImpl implements IWebModuleDeployContext {

	private boolean isValid;
	private IWebModule webModule = null;
	private String wceProviderName = null;
  private Configuration wceWebModuleConfig = null;
  private Configuration wceAppConfig = null;
  private String[] myWebModules = null;

	public WebModuleDeployContextImpl(IWebModule webModule, String wceProviderName) {
		this.webModule = webModule;
		this.wceProviderName = wceProviderName;
		isValid = true;
	}

	public void invalidate() {
		isValid = false;
		wceWebModuleConfig = null;
		wceAppConfig = null;
	}

	public Configuration getMyWebModuleConfigWrite() throws WCEConfigurationException {
		if (!isValid) {
			throw new WCEConfigurationException("It is not allowed to get the configuration on invalidated IWebModuleDeployContext object.");
		}
		return wceWebModuleConfig;
	}

	/** Checks if it isn't already created in which case does nothing. */
	public void createAndStoreConfiguration(Configuration servlet_jspConfig) throws WCEConfigurationException {
		String wmConfigName = ConfigurationUtils.convertToConfigForm(ParseUtils.convertAlias(webModule.getModuleName()));
		String applicationName = webModule.getApplicationName();
		String wceAppConfigName = Constants.WCE_CONFIG_PREFIX + ConfigurationUtils.convertToConfigForm(wceProviderName);
		String wceWebModuleConfigName = Constants.WCE_WEBMODULE_CONFIG_PREFIX + wmConfigName;
		//Returns it without exception if it is already created
		try {
			wceAppConfig = ConfigurationUtils.createSubConfiguration(servlet_jspConfig, wceAppConfigName, applicationName, true);
			wceWebModuleConfig = ConfigurationUtils.createSubConfiguration(wceAppConfig, wceWebModuleConfigName, applicationName, true);
		} catch (DeploymentException e) {
			throw new WCEConfigurationException("Failed to create and store the WCE application configuration!", e);
		}
	}

	public void loadConfiguration(Configuration servlet_jspConfig) throws WCEConfigurationException {
		if (servlet_jspConfig == null) {
			return;
		}
		String wmConfigName = ConfigurationUtils.convertToConfigForm(ParseUtils.convertAlias(webModule.getModuleName()));
		String applicationName = webModule.getApplicationName();
		String wceAppConfigName = Constants.WCE_CONFIG_PREFIX + ConfigurationUtils.convertToConfigForm(wceProviderName);
		String wceWebModuleConfigName = Constants.WCE_WEBMODULE_CONFIG_PREFIX + wmConfigName;
		//Returns it without exception if it is already created
		try {
			wceAppConfig = ConfigurationUtils.getSubConfiguration(servlet_jspConfig, wceAppConfigName, applicationName, true);
			wceWebModuleConfig = ConfigurationUtils.getSubConfiguration(wceAppConfig, wceWebModuleConfigName, applicationName, true);
		} catch (DeploymentException e) {
			throw new WCEConfigurationException("Failed to read the WCE application configuration!", e);
		}
	}

	public String[] getMyWebModules() throws WCEConfigurationException {
		if (!isValid) {
			throw new WCEConfigurationException("It is not allowed to get web modules names on invalidated IWebModuleDeployContext object.");
		}
		return myWebModules;
	}//end of getMyWebModules()

	public void setMyWebModules(String[] myWebModules) {
		this.myWebModules = myWebModules;
	}//end of setMyWebModules(String[] myWebModules)

}//end of class
