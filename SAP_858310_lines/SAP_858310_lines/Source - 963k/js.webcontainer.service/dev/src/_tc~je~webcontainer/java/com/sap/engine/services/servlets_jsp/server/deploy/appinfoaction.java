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
package com.sap.engine.services.servlets_jsp.server.deploy;

import java.util.StringTokenizer;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.frame.core.configuration.ConfigurationHandler;
import com.sap.engine.frame.core.configuration.ConfigurationHandlerFactory;
import com.sap.engine.services.deploy.container.AdditionalAppInfo;
import com.sap.engine.services.deploy.container.ContainerInfo;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.servlets_jsp.WebContainerInterface;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.deploy.util.ConfigurationUtils;
import com.sap.engine.services.servlets_jsp.server.deploy.util.Constants;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.IWebContainer;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.tc.logging.Location;

/**
 * 
 * @author Violeta Georgieva
 * @version 7.0
 */
public class AppInfoAction extends ActionBase {
  private static Location currentLocation = Location.getLocation(AppInfoAction.class);
  private ConfigurationHandlerFactory configurationFactory = null;

	/**
   * Constructs new AppInfoAction object.
   *
   * @param iWebContainer
   * @param containerInfo
   * @param servletsAndJsp
   * @param runtimeInterface
   * @param configurationFactory
   */
  public AppInfoAction(IWebContainer iWebContainer, ContainerInfo containerInfo, WebContainerHelper servletsAndJsp,
                       WebContainerInterface runtimeInterface, ConfigurationHandlerFactory configurationFactory) {
    super(iWebContainer, containerInfo, servletsAndJsp, runtimeInterface);
    this.configurationFactory = configurationFactory;
  }//end of constructor

	/**
	 * 
	 * @param appName
	 * @param addAppInfo
	 * @return
	 * @throws DeploymentException
	 */
  public boolean acceptedAppInfoChange(String appName, AdditionalAppInfo addAppInfo) throws DeploymentException {
    String aliases[] = ServiceContext.getServiceContext().getDeployContext().getAliases(appName);
    if (aliases == null || aliases.length == 0) {
      return false;
    }

    boolean returnValue = true;
    ConfigurationHandler handler = null;

    try {
      handler = configurationFactory.getConfigurationHandler();
      Configuration appsConfig = handler.openConfiguration("apps", ConfigurationHandler.READ_ACCESS);

      StringTokenizer strTokenizer = new StringTokenizer(appName, "\\/");
      int numTokens = strTokenizer.countTokens();
      String token = null;
      for (int i = 0; i < numTokens; i++) {
        token = strTokenizer.nextToken();
        appsConfig = appsConfig.getSubConfiguration(token);
      }
      appsConfig = appsConfig.getSubConfiguration(Constants.CONTAINER_NAME);

      String failoverMode = (String)appsConfig.getConfigEntry(Constants.FAIL_OVER);
      if (failoverMode != null && addAppInfo.getFailOver().getName() != null && failoverMode.equals(addAppInfo.getFailOver().getName())) {
        returnValue = false;
      }
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable ce) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000071",
        "Error while reading the old value for failover during its value change.", ce, null, null);
      returnValue = false;
    } finally {
      try {
        handler.closeAllConfigurations();
      } catch (ConfigurationException ex) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000072",
          "Error while reading the old value for failover during its value change.", ex, null, null);
        returnValue = false;
      }
    }

    return returnValue;
  }//end of acceptedAppInfoChange(String appName, AdditionalAppInfo addAppInfo)

	/**
	 * 
	 * @param appName
	 * @param addAppInfo
	 * @param configuration
	 * @throws DeploymentException
	 */
  public void makeAppInfoChange(String appName, AdditionalAppInfo addAppInfo, Configuration configuration) throws DeploymentException {
    try {
      Configuration servlet_jspConfiguration = ConfigurationUtils.getSubConfiguration(configuration, containerInfo.getName(), appName, true);
      if (servlet_jspConfiguration == null) {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_GET_CONFIGURATION_FOR_APPLICATION,
            new Object[]{appName});
      }
      
      String originalFailover = (String)servlet_jspConfiguration.getConfigEntry(Constants.FAIL_OVER);
      if (!originalFailover.equals(addAppInfo.getFailOver().getName())) {
        ConfigurationUtils.addConfigEntry(servlet_jspConfiguration, Constants.FAIL_OVER, addAppInfo.getFailOver().getName(), appName, true, true);
      }
    } catch (ConfigurationException ce) {
      throw new WebDeploymentException(WebDeploymentException.CONFIGURATION_ERROR_WHILE_UPDATING_RUNTIME_PROPERTY_OF_APPLICATION,
          new Object[]{appName}, ce);
    }
  }//end of makeAppInfoChange(String appName, AdditionalAppInfo addAppInfo, Configuration configuration)
  
}//end of class
