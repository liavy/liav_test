package com.sap.engine.services.webservices.webservices630.server.deploy.ws.deploy;

import com.sap.engine.services.webservices.webservices630.server.deploy.common.DeployResult;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WSDeploymentInfo;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WebServicesUtil;
import com.sap.engine.services.webservices.runtime.definition.WSRuntimeDefinition;


/**
 * Title: WSDeployResult
 * Description: The class is a container for web services deployment generated data.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSDeployResult extends DeployResult {

  private WSDeploymentInfo[] wsDeploymentInfoes = new WSDeploymentInfo[0];

  public WSDeployResult() {
  }

  public WSDeploymentInfo[] getWsDeploymentInfoes() {
    return wsDeploymentInfoes;
  }

  public void setWsDeploymentInfoes(WSDeploymentInfo[] wsDeploymentInfoes) {
    this.wsDeploymentInfoes = wsDeploymentInfoes;
  }

  public void addWsDeploymentInfoes(WSDeploymentInfo[] wsDeploymentInfoes) {
    this.wsDeploymentInfoes = WebServicesUtil.unifyWSDeploymentInfoes(new WSDeploymentInfo[][]{this.wsDeploymentInfoes, wsDeploymentInfoes});
  }

  public void addWSDeployResult(DeployResult deployResult) {
    if(deployResult == null) {
      return;
    }

    super.addDeployResult(deployResult);

    if(deployResult instanceof WSDeployResult) {
      WSDeployResult wsDeployResult = (WSDeployResult)deployResult;
      this.addWsDeploymentInfoes(wsDeployResult.getWsDeploymentInfoes());
    }
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
