﻿/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
 
package com.sap.engine.services.webservices.server.deploy.ws.notification;

import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.interfaces.webservices.server.deploy.ws.WebServicesDeploymentInterface;
import com.sap.engine.services.webservices.espbase.server.StaticConfigurationContext;
import com.sap.engine.services.webservices.espbase.server.runtime.RuntimeProcessingEnvironment;
import com.sap.engine.services.webservices.server.container.ws.ServiceContext;

/**
 * Title: WSStartNotificationHandler
 * Description: WSStartNotificationHandler
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class WSStartNotifictionHandler extends WSAbstractDNotificationHandler {
 
  public WSStartNotifictionHandler(String applicationName, ServiceContext serviceContext, RuntimeProcessingEnvironment runtimeProcessingEnvironment) {   
    super(applicationName, serviceContext, runtimeProcessingEnvironment); 
  }
  
  public void onPostPhase() throws WSDeploymentException, WSWarningException {
	  
  }   
  
  protected void onExecutePhase(String applicationName, String serviceName, StaticConfigurationContext[] bindingDataStaticConfigurationContexts, int updateMode, WebServicesDeploymentInterface componentForNotification) throws WSDeploymentException, WSWarningException {
    componentForNotification.onStart(applicationName, serviceName, bindingDataStaticConfigurationContexts);                
  }
  
  protected void onPostPhase(String applicationName, String serviceName, StaticConfigurationContext[] bindingDataStaticConfigurationContexts, int updateMode, WebServicesDeploymentInterface componentForNotification) throws WSDeploymentException, WSWarningException {    
  
  }
  
  protected void onCommitPhase(String applicationName, WebServicesDeploymentInterface componentForNotification) throws WSWarningException {            
    componentForNotification.onCommitStart(applicationName);          
  }
  
  protected void onRollbackPhase(String applicationName, WebServicesDeploymentInterface componentForNotification) throws WSWarningException {
    componentForNotification.onRollbackStart(applicationName);          
  }
  
  protected StaticConfigurationContext getBDStaticConfigurationContext(String bindingDataUrl) throws Exception {        
    return runtimeProcessingEnvironment.getInitializedStaticContext(serviceContext, bindingDataUrl, true);           
  }
  
}
