package com.sap.engine.services.webservices.webservices630.server.deploy.ws.deploy;

import com.sap.engine.services.webservices.webservices630.server.deploy.common.ModuleDeployResult;

/**
 * Title: WSAppDeployResult
 * Description: The class is a container for web services deployment generated data.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSAppDeployResult {

  private ModuleDeployResult moduleDeployResult = null;
  private WSDeployResult wsDeployResult = null;

  public WSAppDeployResult() {
  }

  public ModuleDeployResult getModuleDeployResult() {
    return moduleDeployResult;
  }

  public void setModuleDeployResult(ModuleDeployResult moduleDeployResult) {
    this.moduleDeployResult = moduleDeployResult;
  }

  public WSDeployResult getWsDeployResult() {
    return wsDeployResult;
  }

  public void setWsDeployResult(WSDeployResult wsDeployResult) {
    this.wsDeployResult = wsDeployResult;
  }

}
