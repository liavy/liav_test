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

package com.sap.engine.services.webservices.server.deploy.ws.notification;

import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.interfaces.webservices.server.deploy.ws.WebServicesDeploymentInterface;
import com.sap.engine.services.webservices.espbase.server.StaticConfigurationContext;
import com.sap.engine.services.webservices.espbase.server.runtime.RuntimeProcessingEnvironment;

/**
 * Title: WSStopNofiticationHandler
 * Description: WSStopNotificationHandler
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class WSStopNotificationHandler extends WSAbstractDNotificationHandler {
  
  public WSStopNotificationHandler(String applicationName, RuntimeProcessingEnvironment runtimeProcessingEnvironment) {   
    super(applicationName, null, runtimeProcessingEnvironment);
  }
  
  public void onExecutePhase() throws WSDeploymentException, WSWarningException { 
  
  }
  
  public void onPostPhase() throws WSDeploymentException, WSWarningException {
	  
  } 
  
  public void onCommitPhase() throws WSWarningException {	
    onShortPhasesForced(COMMIT_PHASE);        
  }       
     
  public void onRollbackPhase() throws WSWarningException {
	  
  }          
  
  protected void onExecutePhase(String applicationName, String serviceName, StaticConfigurationContext[] bindingDataStaticConfigurationContexts, int updateMode, WebServicesDeploymentInterface componentForNotification) throws WSDeploymentException, WSWarningException {    
  
  }

  protected void onPostPhase(String applicationName, String serviceName, StaticConfigurationContext[] bindingDataStaticConfigurationContexts, int updateMode, WebServicesDeploymentInterface componentForNotification) throws WSDeploymentException, WSWarningException {  
  
  }
  
  protected void onCommitPhase(String applicationName, WebServicesDeploymentInterface componentForNotification) throws WSWarningException {       
    componentForNotification.onStop(applicationName);           
  }
  
  protected void onRollbackPhase(String applicationName, WebServicesDeploymentInterface componentForNotification) throws WSWarningException {  
  
  }
  
  protected StaticConfigurationContext getBDStaticConfigurationContext(String bindingDataUrl) throws Exception {
    return null;         
  }
  
}
