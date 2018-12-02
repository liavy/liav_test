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
 
package com.sap.engine.interfaces.webservices.server.deploy.ws;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;

import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;

/**
 * Title: WebServicesDeploymentInterface
 * Description: WebServicesDeploymentInterface
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public interface WebServicesDeploymentInterface {
        	  
  public static int NEW_SERVICE_MODE    = 0; 
  public static int UPDATE_SERVICE_MODE = 1; 
  public static int DELETE_SERVICE_MODE = 2;   
  
  public static final String UPDATE_PROCESS    = "update";  
  public static final ThreadLocal PROCESS_TYPE = new ThreadLocal();
  
  public void onDeploy(String applicationName, String serviceName, ConfigurationContext[] bindingDataStaticConfigurationContexts) throws WSDeploymentException, WSWarningException;   
  public void onPostDeploy(String applicationName, String serviceName, ConfigurationContext[] bindingDataStaticConfigurationContext) throws WSDeploymentException, WSWarningException;      
  public void onCommitDeploy(String applicationName) throws WSWarningException;  
  public void onRollbackDeploy(String applicationName) throws WSWarningException;      
  
  public void onRemove(String applicationName) throws WSWarningException; 
     
  public void onStart(String applicationName, String serviceName, ConfigurationContext[] bindingDataConfigurationContext) throws WSDeploymentException, WSWarningException;
  public void onCommitStart(String applicationName) throws WSWarningException;  
  public void onRollbackStart(String applicationName) throws WSWarningException ;  

  public void onStop(String applicationName) throws WSWarningException;
   
  public void onRuntimeChanges(String applicationName, String serviceName, ConfigurationContext[] bindingDataStaticConfigurationContexts, int updateMode) throws WSDeploymentException, WSWarningException;   
  public void onCommitRuntimeChanges(String applicationName) throws WSWarningException;  
  public void onRollbackRuntimeChanges(String applicationName) throws WSWarningException; 
      
}
