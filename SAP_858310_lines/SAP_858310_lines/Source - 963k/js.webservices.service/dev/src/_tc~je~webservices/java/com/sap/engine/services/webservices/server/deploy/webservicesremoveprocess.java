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

package com.sap.engine.services.webservices.server.deploy;

import java.util.ArrayList;

import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.server.deploy.ws.WSRemoveProcess;
import com.sap.engine.services.webservices.server.deploy.wsclients.notification.WSClientsRemoveNofiticationNandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;

/**
 * Title: WebServicesRemoveProcess
 * Description: WebServicesRemoveProcess
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class WebServicesRemoveProcess implements WebServicesDInterface {
	
  private String applicationName; 
  private String webServicesContainerDir; 
  private WSRemoveProcess wsRemoveProcess;
  private WebServicesDNotificationInterface wsClientsDNotificationHandler; 
 
  public WebServicesRemoveProcess(String applicationName, String webServicesContainerDir, WSRemoveProcess wsRemoveProcess) {
    this.applicationName = applicationName; 
    this.webServicesContainerDir = webServicesContainerDir;
    this.wsRemoveProcess =  wsRemoveProcess;
    this.wsClientsDNotificationHandler = new WSClientsRemoveNofiticationNandler(applicationName, WSContainer.getRuntimeProcessingEnv()); 
  } 

  public void preProcess() throws WSDeploymentException, WSWarningException {
    
  }

  public void init() throws WSDeploymentException {   
    
  }

  public void execute() throws WSDeploymentException {  
    
  }  

  public void finish() throws WSDeploymentException {  
  
  } 
  
  public void makeProcess() throws WSDeploymentException {
    
	  
  }

  public void postProcess() throws WSDeploymentException, WSWarningException {
 
  }

  public void notifyProcess() throws WSWarningException {    
  
  }

  public void commitProcess() throws WSWarningException { 
    ArrayList<String> warnings = new ArrayList<String>();     
	
    try {
      wsRemoveProcess.commitProcess(); 
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);            
    }
    
    try {
      wsClientsDNotificationHandler.onCommitPhase();  	
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);      
    }
    
    try {
      IOUtil.deleteDir(webServicesContainerDir);
    } catch(Exception e) {
      // $JL-EXC$
      // TODO - add trace 
    }
    
    if(warnings == null || warnings.size() != 0) {
      WSWarningException e = new WSWarningException(); 
      e.setWarning(warnings.toArray(new String[warnings.size()])); 
      throw e; 
    }
  }  

  public void rollbackProcess() throws WSWarningException {   
	  
  }
  
}
