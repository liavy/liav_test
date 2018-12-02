package com.sap.engine.services.webservices.webservices630.server.deploy.ws;

import com.sap.engine.services.webservices.runtime.definition.ServiceEndpointDefinition;
import com.sap.engine.services.webservices.runtime.definition.WSRuntimeDefinition;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2003
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */
public class WebServicesUtil {

  public static String[] getSEITransportIds(ServiceEndpointDefinition[] seiDefinitions) {
    if(seiDefinitions == null) {
      return new String[0];
    }

    String[] seiTransportIds = new String[seiDefinitions.length];
    for(int i = 0; i < seiDefinitions.length; i++) {
      seiTransportIds[i] = seiDefinitions[i].getServiceEndpointId();
    }

    return seiTransportIds;
  }

  public static WSDeploymentInfo[] unifyWSDeploymentInfoes(WSDeploymentInfo[][] wsDeploymentInfoes) {
    if(wsDeploymentInfoes == null) {
      return new WSDeploymentInfo[0];
    }

    WSDeploymentInfo[] allWSDeploymentInfoes = new WSDeploymentInfo[0];
    for(int i = 0; i < wsDeploymentInfoes.length; i++) {
      WSDeploymentInfo[] currentWSDeploymentInfoes = wsDeploymentInfoes[i];
      WSDeploymentInfo[] tempWSDeploymentInfoes = new WSDeploymentInfo[allWSDeploymentInfoes.length + currentWSDeploymentInfoes.length];
      System.arraycopy(allWSDeploymentInfoes, 0, tempWSDeploymentInfoes, 0, allWSDeploymentInfoes.length);
      System.arraycopy(currentWSDeploymentInfoes, 0, tempWSDeploymentInfoes, allWSDeploymentInfoes.length, currentWSDeploymentInfoes.length);
      allWSDeploymentInfoes = tempWSDeploymentInfoes;
    }

    return allWSDeploymentInfoes;
  }

  public static WSRuntimeDefinition[] unifyWSRuntimeDefinitiones(WSRuntimeDefinition[][] wsRuntimeDefinitiones) {
    if(wsRuntimeDefinitiones == null) {
      return new WSRuntimeDefinition[0];
    }

    WSRuntimeDefinition[] allWSRuntimeDefinitiones = new WSRuntimeDefinition[0];
    for(int i = 0; i < wsRuntimeDefinitiones.length; i++) {
      WSRuntimeDefinition[] currentWSRuntimeDefinitiones = wsRuntimeDefinitiones[i];
      WSRuntimeDefinition[] tempWSRuntimeDefinitiones = new WSRuntimeDefinition[allWSRuntimeDefinitiones.length + currentWSRuntimeDefinitiones.length];
      System.arraycopy(allWSRuntimeDefinitiones, 0, tempWSRuntimeDefinitiones, 0, allWSRuntimeDefinitiones.length);
      System.arraycopy(currentWSRuntimeDefinitiones, 0, tempWSRuntimeDefinitiones, allWSRuntimeDefinitiones.length, currentWSRuntimeDefinitiones.length);
      allWSRuntimeDefinitiones = tempWSRuntimeDefinitiones;
    }

    return allWSRuntimeDefinitiones;
  }

}
