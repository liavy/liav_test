package com.sap.engine.services.webservices.webservices630.server.deploy.ws;

import com.sap.engine.services.webservices.runtime.definition.WSRuntimeDefinition;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;

import java.util.HashSet;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2003
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */
public class WSChecker {

  public void checkWSAppLevel(WSRuntimeDefinition[] wsRuntimeDefinitions) throws WSDeploymentException {
    if (wsRuntimeDefinitions == null) {
      return;
    }

    checkServiceNamesForDublicates(collectWebServiceNames(wsRuntimeDefinitions));
  }

  public void checkServiceNamesForDublicates(String[] webServiceNames) throws WSDeploymentException {
    String dublicateWSName = hasDublicates(webServiceNames);
    if(dublicateWSName != null) {
      String msg = "There is duplicate element for web service " + dublicateWSName + ". ";
      Object[] args = new String[]{msg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args);
    }
  }

  private String[] collectWebServiceNames(WSRuntimeDefinition[] wsRuntimeDefinitions) {
    if(wsRuntimeDefinitions == null) {
      return new String[0];
    }

    String[] webServiceNames = new String[wsRuntimeDefinitions.length];
    for(int i = 0; i < wsRuntimeDefinitions.length; i++) {
      webServiceNames[i] = wsRuntimeDefinitions[i].getWSIdentifier().getServiceName();
    }

    return webServiceNames;
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
