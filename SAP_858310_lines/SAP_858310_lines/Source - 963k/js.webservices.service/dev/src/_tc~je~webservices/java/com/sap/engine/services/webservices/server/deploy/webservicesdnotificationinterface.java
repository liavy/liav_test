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
 * Title: WebServicesDNotificationInterface
 * Description: WebServicesDNotificationInterface
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public interface WebServicesDNotificationInterface {

  public void onExecutePhase() throws WSDeploymentException, WSWarningException;  
  
  public void onPostPhase() throws WSDeploymentException, WSWarningException;  
  
  public void onCommitPhase() throws WSWarningException;

  public void onRollbackPhase() throws WSWarningException; 
  
}
