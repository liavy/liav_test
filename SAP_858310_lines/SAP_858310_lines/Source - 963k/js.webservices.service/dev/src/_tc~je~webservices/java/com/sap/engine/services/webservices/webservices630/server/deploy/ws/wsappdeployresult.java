package com.sap.engine.services.webservices.webservices630.server.deploy.ws;

import com.sap.engine.services.webservices.webservices630.server.deploy.common.AppDeployResult;
import com.sap.engine.services.webservices.runtime.definition.WSRuntimeDefinition;

/**
 * Title: WSAppDeployResult
 * Description: This class represents web services deployment result.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSAppDeployResult extends AppDeployResult {

  private WSDeploymentInfo[] wsDeploymentInfoes = new WSDeploymentInfo[0];

  public WSAppDeployResult() {
  }

  public WSDeploymentInfo[] getWsDeploymentInfos() {
    return wsDeploymentInfoes;
  }

  public void setWsDeploymentInfos(WSDeploymentInfo[] wsDeploymentInfoes) {
    this.wsDeploymentInfoes = wsDeploymentInfoes;
  }
  
  public WSRuntimeDefinition[] getWSRuntimeDefinitions() {
    WSRuntimeDefinition[] wsRuntimeDefinitions = new WSRuntimeDefinition[wsDeploymentInfoes.length];
    for (int i = 0; i < wsDeploymentInfoes.length; i++) {
      WSDeploymentInfo wsDeploymentInfo = wsDeploymentInfoes[i];
      wsRuntimeDefinitions[i] = wsDeploymentInfo.getWsRuntimeDefinition();
    }
    return wsRuntimeDefinitions;
  }

}
