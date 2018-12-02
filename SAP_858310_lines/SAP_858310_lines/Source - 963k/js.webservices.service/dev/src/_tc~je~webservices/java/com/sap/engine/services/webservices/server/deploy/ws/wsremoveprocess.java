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
 
package com.sap.engine.services.webservices.server.deploy.ws;

import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.services.webservices.espbase.server.runtime.RuntimeProcessingEnvironment;
import com.sap.engine.services.webservices.server.deploy.ws.notification.WSRemoveNotificationHandler;

/**
 * Title: WSRemoveProcess
 * Description: WSRemoveProcess
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class WSRemoveProcess extends WSAbstractDProcess {

  public WSRemoveProcess(String applicationName, RuntimeProcessingEnvironment runtimeProcessingEnvironment) {
     this.applicationName = applicationName; 
     this.wsDNotificationHandler = new WSRemoveNotificationHandler(applicationName, runtimeProcessingEnvironment); 
   }
  
   public void preProcess() throws WSDeploymentException, WSWarningException {
	   
   }
  
   public void init() throws WSDeploymentException {
   
   }
  
   public void execute() throws WSDeploymentException {  
        
   }
  
   public void finish() throws WSDeploymentException {
	   
   }
  
   public void makeProcess() {
	   
   }
   
   public void postProcess() throws WSDeploymentException, WSWarningException {
   
   }
  
   public void notifyProcess() throws WSWarningException {
   
   } 
    
   public void commitProcess() throws WSWarningException {        
     wsDNotificationHandler.onCommitPhase();     
   }
  
   public void rollbackProcess() throws WSWarningException {
	   
   }

}
