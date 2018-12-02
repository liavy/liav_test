package com.sap.engine.services.webservices.webservices630.server.deploy.ws.update;

import com.sap.engine.services.webservices.webservices630.server.deploy.common.ModuleDeployResult;

import java.util.Vector;

/**
 * Title: WSAppUpdateResult
 * Description: The class is a container for web services deployment generated data.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSAppUpdateResult {

  private ModuleDeployResult moduleDeployResult = null;
  private WSUpdateResult wsUpdateResult = null;

  Vector warnings = new Vector();

  public WSAppUpdateResult() {
  }

  public ModuleDeployResult getModuleDeployResult() {
    if(moduleDeployResult == null) {
      return new ModuleDeployResult();
    }
    return moduleDeployResult;
  }

  public void setModuleDeployResult(ModuleDeployResult moduleDeployResult) {
    this.moduleDeployResult = moduleDeployResult;
  }

  public WSUpdateResult getWsUpdateResult() {
    if(wsUpdateResult == null) {
      return new WSUpdateResult();
    }
    return wsUpdateResult;
  }

  public void setWsUpdateResult(WSUpdateResult wsUpdateResult) {
    this.wsUpdateResult = wsUpdateResult;
  }

  public Vector getWarnings() {
    return warnings;
  }

  public void setWarnings(Vector warnings) {
    this.warnings = warnings;
  }

  public void addWarnings(Vector warnings) {
    this.warnings.addAll(warnings);
  }

}
