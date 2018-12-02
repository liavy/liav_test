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

package com.sap.engine.services.webservices.server.deploy.wsclients;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.server.container.wsclients.ServiceRefContext;
import com.sap.engine.services.webservices.server.deploy.wsclients.notification.WSClientsStopNotificationHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;

/**
 * Title: WSClientsStopProcess 
 * Description: WSClientsStopProcess
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class WSClientsStopProcess extends WSClientsAbstractDProcess {
  
  public WSClientsStopProcess(String applicationName, ServiceRefContext serviceRefContext) {    
    this.applicationName = applicationName; 
    this.serviceContext = serviceRefContext; 
    this.wsClientsDNotificationHandler = new WSClientsStopNotificationHandler(applicationName, WSContainer.getRuntimeProcessingEnv());  
  }
    
  public void preProcess() throws WSDeploymentException, WSWarningException {
 
  }
  
  public void init() throws WSDeploymentException {   
  
  }

  public void execute() throws WSDeploymentException  {  
  
  }  
  
  public void finish() throws WSDeploymentException {
  
  } 
  
  public void postProcess() throws WSDeploymentException, WSWarningException {   
  
  }
  
  public void notifyProcess() throws WSWarningException {    
  
  }
  
  public void commitProcess() throws WSWarningException  {	
	ArrayList<String> warnings = new ArrayList<String>();
	
	try { 
      wsClientsDNotificationHandler.onCommitPhase();
	} catch(WSWarningException e) {
	  WSUtil.addStrings(e.getWarnings(), warnings);   
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
	  
  }

}
