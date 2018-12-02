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

import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;

/**
 * Title: WebServiceDInterface 
 * Description: WebServiceDInterface defines the base phases of all deployment processes  
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public interface WebServicesDInterface {
      
  public void preProcess() throws WSDeploymentException, WSWarningException;  
  
  public void init() throws WSDeploymentException;  
  public void execute() throws WSDeploymentException; 
  public void finish() throws WSDeploymentException;  
  public void makeProcess() throws WSDeploymentException;

  public void notifyProcess() throws WSWarningException;
  public void postProcess() throws WSDeploymentException, WSWarningException; 
  public void commitProcess() throws WSWarningException; 
  public void rollbackProcess() throws WSWarningException;
    
}
