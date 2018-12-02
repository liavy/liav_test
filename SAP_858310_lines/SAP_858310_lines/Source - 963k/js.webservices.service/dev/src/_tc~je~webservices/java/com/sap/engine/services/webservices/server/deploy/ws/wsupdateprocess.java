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

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.services.webservices.espbase.server.runtime.RuntimeProcessingEnvironment;
import com.sap.engine.services.webservices.server.container.ws.ServiceContext;
import com.sap.engine.services.webservices.server.deploy.ws.notification.WSDeployNotificationHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;

/**
 * Title: WSUpdateProcess
 * Description: WSUpdateProcess
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class WSUpdateProcess extends WSAbstractDProcess {
  
  public WSUpdateProcess(String applicationName, String webServicesContainerDir, String webServicesContainerTempDir, Configuration appConfiguration, RuntimeProcessingEnvironment runtimeProcessingEnvironment) {
    this.applicationName = applicationName; 
    this.webServicesContainerDir = webServicesContainerDir; 
    this.webServicesContainerTempDir = webServicesContainerTempDir;
    this.appConfiguration = appConfiguration;
    this.serviceContext = new ServiceContext();
    this.wsDNotificationHandler = new WSDeployNotificationHandler(applicationName, (ServiceContext)this.serviceContext, runtimeProcessingEnvironment);     
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
	  
  }
  
  public void rollbackProcess() throws WSWarningException {
    ArrayList<String> warnings = new ArrayList<String>(); 
    
    try {     
      loadWebServicesJ2EEEngineDescriptorsInitially(); 
      loadWebServicesJ2EEEngineDescriptors();     
    } catch(Exception e) {
      //TODO - add warning message
      StringWriter strWriter = new StringWriter(); 
      e.printStackTrace(new PrintWriter(strWriter));       
      warnings.add(strWriter.toString());  
    } 
    
    try {    
      wsDNotificationHandler.onExecutePhase();
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);         
    } catch(Exception e) {
      //TODO - add message
      StringWriter strWriter = new StringWriter(); 
      e.printStackTrace(new PrintWriter(strWriter));       
      warnings.add(strWriter.toString());	
    }
    
    try  {    
      wsDNotificationHandler.onPostPhase();
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);         
    } catch(Exception e) {
      // TODO - add message
      StringWriter strWriter = new StringWriter(); 
      e.printStackTrace(new PrintWriter(strWriter));       
      warnings.add(strWriter.toString());	
    }
    
    try {    
      wsDNotificationHandler.onCommitPhase();
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);       
    }
    
    if(warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException(); 
      e.setWarning(warnings.toArray(new String[warnings.size()])); 
      throw e;      
    }   
  }

}
