package com.sap.engine.services.webservices.webservices630.server.deploy.wsclient;

import com.sap.engine.interfaces.webservices.runtime.definition.WSClientIdentifier;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.runtime.definition.wsclient.WSClientRuntimeInfo;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.tc.logging.Location;

import java.util.HashSet;

/**
 * Title: WSClientChecker
 * Description: The class provides methods for a check if the ws clients satisfy some base requirements (as unique service names in an application, etc.).
 * Copyright: Copyright (c) 2003
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSClientChecker {

  public WSClientChecker() {
  }

  public void checkWSClients(WSClientRuntimeInfo[] wsClientRuntimeInfos, String[] additionalServiceRefNames) throws WSDeploymentException {
    makeSingleWSClientCheck(wsClientRuntimeInfos);

    if(additionalServiceRefNames == null) {
      additionalServiceRefNames = new String[0];
    }
    String[] wsClientNames = WSClientsUtil.collectWSClientNames(wsClientRuntimeInfos);
    WSUtil.printStrings(wsClientNames);
    WSUtil.printStrings(additionalServiceRefNames);
    checkWSClientNamesForDublicates(WSUtil.unifyStrings(new String[][]{WSClientsUtil.collectWSClientNames(wsClientRuntimeInfos), additionalServiceRefNames}));
  }

  public void makeSingleWSClientCheck(WSClientRuntimeInfo[] wsClientRuntimeInfos) throws WSDeploymentException {
    if(wsClientRuntimeInfos == null) {
      return;
    }

    for(int i = 0; i < wsClientRuntimeInfos.length; i++) {
      WSClientRuntimeInfo wsClientRuntimeInfo = wsClientRuntimeInfos[i];
      makeSingleWSClientCheck(wsClientRuntimeInfo);
    }
  }

  public void makeSingleWSClientCheck(WSClientRuntimeInfo wsClientRuntimeInfo) throws WSDeploymentException {
    String excMessage = "Unable to deploy ws client.";
    WSClientIdentifier wsClientId = wsClientRuntimeInfo.getWsClientId();

    try {
      checkWsdlAndUriMappingFilesCount(wsClientRuntimeInfo);
    } catch(WSDeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(excMessage, e);

      Object[] args = new String[]{excMessage, wsClientId.getApplicationName(), wsClientId.getJarName(), wsClientId.getServiceRefName()};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_CLIENTS_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  public void checkWsdlAndUriMappingFilesCount(WSClientRuntimeInfo wsClientRuntimeInfo) throws WSDeploymentException {
    String excMessage = "Wsdl and uri mapping files counts are not equal!";
    if (!wsClientRuntimeInfo.hasUriMappingFiles()) {
      return;
    }

    int wsdlFilesCount = wsClientRuntimeInfo.getWsdlFileNames().length;
    int uriMappingFilesCount = wsClientRuntimeInfo.getUriMappingFiles().length;

    if (wsdlFilesCount != uriMappingFilesCount) {
      Object[] args = new String[]{excMessage};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args);
    }
  }

  public void checkWSClientNamesForDublicates(String[] wsClientNames) throws WSDeploymentException {
    String dublicateWSClientName = hasDublicates(wsClientNames);
    if(dublicateWSClientName != null) {
      String msg = "There is a duplicate element for ws client " + dublicateWSClientName + ". ";
      Object[] args = new String[]{msg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args);
    }
  }

  private String hasDublicates(String[] strs) {
    if(strs == null) {
      return null;
    }

    HashSet set = new HashSet();
    for(int i = 0; i < strs.length; i++) {
      String str = strs[i];
      if(set.contains(str))  {
        return str;
      }

      set.add(str);
    }

    return null;
  }

}
