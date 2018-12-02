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

import java.util.Map;

import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;

/**
 * Title: WSBaseAbstractDNotificationHandler
 * Description: WSBaseAbstractDNotificationHandler
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public abstract class WSBaseAbstractDNotificationHandler implements WebServicesDNotificationInterface {
  
  public String EXECUTE_PHASE  = "execute";
  public String POST_PHASE     = "post"; 
  public String COMMIT_PHASE   = "commit";
  public String ROLLBACK_PHASE = "rollback"; 
		
  private String applicationName; 
  private Map componentsForNotification;  
	  
  public void onExecutePhase() throws WSDeploymentException, WSWarningException {
    onLongPhases(EXECUTE_PHASE);	 
  }
  
  public void onPostPhase() throws WSDeploymentException, WSWarningException {
    onLongPhases(POST_PHASE);	
  }     
  
  public void onCommitPhase() throws WSWarningException {
    onShortPhases(COMMIT_PHASE); 	
  }          
	   
  public void onRollbackPhase() throws WSWarningException {
    onShortPhases(ROLLBACK_PHASE);     
  }
  
  protected abstract void onLongPhases(String phase) throws WSDeploymentException, WSWarningException; 
  
  protected abstract void onShortPhases(String phase) throws WSWarningException; 
		
}
