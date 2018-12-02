package com.sap.engine.services.webservices.webservices630.server.deploy.ws.update;

import com.sap.engine.services.webservices.webservices630.server.deploy.common.DeployResult;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.deploy.WSDeployResult;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.preprocess.ExtFileLocationWrapper;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.preprocess.WSFileLocationWrapper;

/**
 * Title: WSUpdateResult
 * Description: The class is a container for web services deployment generated data.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSUpdateResult extends WSDeployResult {

  WSFileLocationWrapper[] wsFileLocationWrappers = new ExtFileLocationWrapper[0];

  public WSUpdateResult() {
  }

  public WSFileLocationWrapper[] getWsFileLocationWrappers() {
    return wsFileLocationWrappers;
  }

  public void setWsFileLocationWrappers(WSFileLocationWrapper[] wsFileLocationWrappers) {
    this.wsFileLocationWrappers = wsFileLocationWrappers;
  }

  public void addWSFileLocationWrappers(WSFileLocationWrapper[] wsFileLocationWrappers) {
    this.wsFileLocationWrappers = unifyWSFileLocationWrappers(new WSFileLocationWrapper[][]{this.wsFileLocationWrappers, wsFileLocationWrappers});
  }

  public void addWSUpdateResult(DeployResult deployResult) {
    super.addWSDeployResult(deployResult);

    if(deployResult instanceof WSUpdateResult) {
      WSUpdateResult wsUpdateResult = (WSUpdateResult)deployResult;
      this.addWSFileLocationWrappers(wsUpdateResult.getWsFileLocationWrappers());
    }
  }

  private WSFileLocationWrapper[] unifyWSFileLocationWrappers(WSFileLocationWrapper[][] wsFileLocationWrappers) {
    if(wsFileLocationWrappers == null) {
      return new ExtFileLocationWrapper[0];
    }

    WSFileLocationWrapper[] wsFileLocationWrappersAll = new ExtFileLocationWrapper[0];
    for(int i = 0; i < wsFileLocationWrappers.length; i++) {
      WSFileLocationWrapper[] currentWSFileLocationWrappers = wsFileLocationWrappers[i];
      WSFileLocationWrapper[] newWSFileLocationWrappers = new WSFileLocationWrapper[wsFileLocationWrappersAll.length + currentWSFileLocationWrappers.length];
      System.arraycopy(wsFileLocationWrappersAll, 0, newWSFileLocationWrappers, 0, wsFileLocationWrappersAll.length);
      System.arraycopy(currentWSFileLocationWrappers, 0, newWSFileLocationWrappers, wsFileLocationWrappersAll.length, currentWSFileLocationWrappers.length);
      wsFileLocationWrappersAll = newWSFileLocationWrappers;
    }

    return wsFileLocationWrappersAll;
  }


}
