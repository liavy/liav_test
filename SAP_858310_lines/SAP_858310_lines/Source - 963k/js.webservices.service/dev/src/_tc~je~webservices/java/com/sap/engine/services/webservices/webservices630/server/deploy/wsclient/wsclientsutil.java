package com.sap.engine.services.webservices.webservices630.server.deploy.wsclient;

import com.sap.engine.services.webservices.runtime.definition.wsclient.WSClientRuntimeInfo;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.update.WSClientFileLocationWrapper;

/**
 * Title: WSClientsUtil
 * Description: The class provides ws clients help methods.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSClientsUtil {

  public static WSClientDeploymentInfo[] unifyWSClientDeploymentInfoes(WSClientDeploymentInfo[][] wsClientDeploymentInfoes) {
    if(wsClientDeploymentInfoes == null) {
      return new WSClientDeploymentInfo[0];
    }

    WSClientDeploymentInfo[] allWSClientDeploymentInfoes= new WSClientDeploymentInfo[0];
    for(int i = 0; i < wsClientDeploymentInfoes.length; i++) {
      WSClientDeploymentInfo[] currentWSClientDeploymentInfoes = wsClientDeploymentInfoes[i];
      WSClientDeploymentInfo[] tempWSClientDeploymentInfoes = new WSClientDeploymentInfo[allWSClientDeploymentInfoes.length + currentWSClientDeploymentInfoes.length];
      System.arraycopy(allWSClientDeploymentInfoes, 0, tempWSClientDeploymentInfoes, 0, allWSClientDeploymentInfoes.length);
      System.arraycopy(currentWSClientDeploymentInfoes, 0, tempWSClientDeploymentInfoes, allWSClientDeploymentInfoes.length, currentWSClientDeploymentInfoes.length);
      allWSClientDeploymentInfoes = tempWSClientDeploymentInfoes;
    }

    return allWSClientDeploymentInfoes;
  }

  public static WSClientRuntimeInfo[] unifyWSClientRuntimeInfoes(WSClientRuntimeInfo[][] wsClientRuntimeInfoes) {
    if(wsClientRuntimeInfoes == null) {
      return new WSClientRuntimeInfo[0];
    }

    WSClientRuntimeInfo[] allWSClientRuntimeInfoes= new WSClientRuntimeInfo[0];
    for(int i = 0; i < wsClientRuntimeInfoes.length; i++) {
      WSClientRuntimeInfo[] currentWSClientRuntimeInfoes = wsClientRuntimeInfoes[i];
      WSClientRuntimeInfo[] tempWSClientRuntimeInfoes = new WSClientRuntimeInfo[allWSClientRuntimeInfoes.length + currentWSClientRuntimeInfoes.length];
      System.arraycopy(allWSClientRuntimeInfoes, 0, tempWSClientRuntimeInfoes, 0, allWSClientRuntimeInfoes.length);
      System.arraycopy(currentWSClientRuntimeInfoes, 0, tempWSClientRuntimeInfoes, allWSClientRuntimeInfoes.length, currentWSClientRuntimeInfoes.length);
      allWSClientRuntimeInfoes = tempWSClientRuntimeInfoes;
    }

    return allWSClientRuntimeInfoes;
  }

  public static WSClientFileLocationWrapper[] unifyWSClientFileLocationWrapper(WSClientFileLocationWrapper[][] wsClientFileLocationWrappers) {
    if(wsClientFileLocationWrappers == null) {
      return new WSClientFileLocationWrapper[0];
    }

    WSClientFileLocationWrapper[] allWSClientFileLocationWrappers = new WSClientFileLocationWrapper[0];
    for(int i = 0; i < wsClientFileLocationWrappers.length; i++) {
      WSClientFileLocationWrapper[] currentWSClientFileLocationWrappers = wsClientFileLocationWrappers[i];
      WSClientFileLocationWrapper[] tempWSClientFileLocationWrappers = new WSClientFileLocationWrapper[allWSClientFileLocationWrappers.length + currentWSClientFileLocationWrappers.length];
      System.arraycopy(allWSClientFileLocationWrappers, 0, tempWSClientFileLocationWrappers, 0, allWSClientFileLocationWrappers.length);
      System.arraycopy(currentWSClientFileLocationWrappers, 0, tempWSClientFileLocationWrappers, allWSClientFileLocationWrappers.length, currentWSClientFileLocationWrappers.length);
      allWSClientFileLocationWrappers = tempWSClientFileLocationWrappers;
    }

    return allWSClientFileLocationWrappers;
  }

  public static String[] collectWSClientNames(WSClientRuntimeInfo[] wsClientRuntimeInfoes) {
    if(wsClientRuntimeInfoes == null) {
      return new String[0];
    }

    String[] wsClientNames = new String[wsClientRuntimeInfoes.length];
    for(int i = 0; i < wsClientRuntimeInfoes.length; i++) {
      wsClientNames[i] = wsClientRuntimeInfoes[i].getWsClientId().getServiceRefName();
    }

    return wsClientNames;
  }

}
