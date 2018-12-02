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

package com.sap.engine.interfaces.webservices.server.deploy;

/**
 * Title: WebServicesDeploymentInterface
 * Description: WebServicesDeploymentInterface
 * 
 * @author Dimitrina Stoyanova
 * @version
 */


public interface WSBaseDeploymentInterface {
  
  public void onDeploy(String applicationName) throws WSDeploymentException, WSWarningException;   
  public void onPostDeploy(String applicationName) throws WSDeploymentException, WSWarningException;      
  public void onCommitDeploy(String applicationName) throws WSWarningException;  
  public void onRollbackDeploy(String applicationName) throws WSWarningException;      
	
  public void onUpdate(String applicationName) throws WSDeploymentException, WSWarningException;         
  public void onCommitUpdate(String applicationName) throws WSWarningException;  
  public void onRollbackUpdate(String applicationName) throws WSWarningException;      

  public void onRemove(String applicationName) throws WSWarningException; 
	     
  public void onStart(String applicationName) throws WSDeploymentException, WSWarningException;
  public void onCommitStart(String applicationName) throws WSWarningException;  
  public void onRollbackStart(String applicationName) throws WSWarningException ;  

  public void onStop(String applicationName) throws WSWarningException;	

}
