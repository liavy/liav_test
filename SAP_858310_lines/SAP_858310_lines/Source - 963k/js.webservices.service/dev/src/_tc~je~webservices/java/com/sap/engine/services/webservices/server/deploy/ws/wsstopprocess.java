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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.services.webservices.espbase.server.runtime.RuntimeProcessingEnvironment;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.server.container.ws.ServiceContext;
import com.sap.engine.services.webservices.server.deploy.ws.notification.WSStopNotificationHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;

/**
 * Title: WSStopProcess 
 * Description: WSStopProcess
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class WSStopProcess extends WSAbstractDProcess {
  
  public WSStopProcess(String applicationName, ServiceContext serviceContext, RuntimeProcessingEnvironment runtimeProcessingEnvironment) {
    this.applicationName = applicationName; 
    this.serviceContext = serviceContext; 
    this.wsDNotificationHandler = new WSStopNotificationHandler(applicationName, runtimeProcessingEnvironment); 
  }
  
  public void preProcess() throws WSDeploymentException, WSWarningException {
  
  }
  
  public void init() throws WSDeploymentException {
  
  }
  
  public void execute() throws WSDeploymentException {  
	  
  }
  
  public void finish() throws WSDeploymentException {
  
  }
  
  public void postProcess() throws WSDeploymentException, WSWarningException {
    
  }
  
  public void notifyProcess() throws WSWarningException {
  
  } 
    
  public void commitProcess() throws WSWarningException {
    ArrayList<String> warnings = new ArrayList<String>(); 
    
	try {
      wsDNotificationHandler.onCommitPhase();
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);              
    }
    
    try {
      WSContainer.getRuntimeProcessingEnv().getWSDLVisualizer().clearWSDLCache(this.applicationName);
    } catch(Exception e ) {      
      //TODO - add message
      StringWriter strWriter = new StringWriter(); 
      e.printStackTrace(new PrintWriter(strWriter));
      warnings.add(strWriter.toString());           	      
    }
    
    try {
      unregister();
    } catch(Exception e) {
      //TODO - add message
      StringWriter strWriter = new StringWriter(); 
      e.printStackTrace(new PrintWriter(strWriter));
      warnings.add(strWriter.toString());                  
    }
    
    if(warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException(); 
      e.setWarning(warnings.toArray(new String[warnings.size()]));
      throw e; 
    }     
  }
  
  public void rollbackProcess() throws WSWarningException {
    wsDNotificationHandler.onRollbackPhase();         
  }
 
}
 