package com.sap.engine.services.webservices.runtime.registry.wsclient;

import com.sap.engine.interfaces.webservices.runtime.definition.WSClientIdentifier;
import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.lib.util.Set;
import com.sap.engine.services.webservices.exceptions.RegistryException;
import com.sap.engine.services.webservices.exceptions.PatternKeys;
import com.sap.engine.services.webservices.runtime.definition.wsclient.WSClientRuntimeInfo;

import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Title:  
 * Description: 
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSClientRegistry {

  public static final int NO_FEATURE          = 0;
  public static final int APPLICATION_FEATURE = 1;

  private HashMapObjectObject wsclients = null;

  public WSClientRegistry() {

  }

  public boolean contains(WSClientIdentifier id) {
    if (wsclients == null) return false;
    return wsclients.get(id) != null ? true : false;
  }

  public void registerWSClient(WSClientIdentifier id, WSClientRuntimeInfo wsClientRuntimeInfo) throws RegistryException {
    if (contains(id)) throw new RegistryException(PatternKeys.WS_DUBLICATE_ELEMENT, new Object[]{id, this.getClass().getName()});
    if (wsclients == null) {
      wsclients = new HashMapObjectObject();
    }

    wsclients.put(id, wsClientRuntimeInfo);
  }

  public void unregisterWSClient(WSClientIdentifier id) {
    if (wsclients != null) {
      wsclients.remove(id);
    }
  }

  public WSClientRuntimeInfo getWSClient(String applicationName, String wsClientName) throws RegistryException {
    WSClientIdentifier wsClientId = new WSClientIdentifier(applicationName, null, wsClientName);
    return getWSClient(wsClientId);
  }

  public WSClientRuntimeInfo getWSClient(WSClientIdentifier id) throws RegistryException {
    if (!contains(id)) throw new RegistryException(PatternKeys.NO_SUCH_WS_ELEMENT, new Object[]{id.toString(), this.getClass().getName()});
    return (WSClientRuntimeInfo)wsclients.get(id);
  }

  public WSClientRuntimeInfo[] getWSClients() {
    return getWSClientsAsArray(getWSClientsByFeature(NO_FEATURE, ""));
  }

  public WSClientRuntimeInfo[] getWSClientsByApplicationName(String applicationName) {
    return getWSClientsAsArray(getWSClientsByFeature(APPLICATION_FEATURE, applicationName));
 }


  public WSClientIdentifier[] listWSClients() {
   return getWSClientIdsAsArray(listWSClientsByFeature(NO_FEATURE, ""));
  }

  public WSClientIdentifier[] listWSClientsByApplicationName(String applicationName) {
   return getWSClientIdsAsArray(listWSClientsByFeature(APPLICATION_FEATURE, applicationName));
  }

  public ArrayList listWSClientsByFeature(int featureType, String featureValue) {
    ArrayList wsclientsArr = new ArrayList();

    if (wsclients == null) {
      return wsclientsArr ;
    }

    Enumeration wsclientsEnum = wsclients.keys();
    while (wsclientsEnum.hasMoreElements()) {
      WSClientIdentifier wsClientId = (WSClientIdentifier)wsclientsEnum.nextElement();
      switch (featureType) {
        case NO_FEATURE : {
          wsclientsArr.add(wsClientId);
          break;
        }
        case APPLICATION_FEATURE : {
          if (wsClientId.getApplicationName().equals(featureValue)) {
            wsclientsArr.add(wsClientId);
          }
          break;
        }
      }
    }
    return wsclientsArr;
  }

  public ArrayList getWSClientsByFeature(int featureType, String featureValue) {
    ArrayList wsclientsArr = new ArrayList();

    if (wsclients == null) {
      return wsclientsArr ;
    }

    Enumeration wsclientsEnum = wsclients.keys();
    while (wsclientsEnum.hasMoreElements()) {
      WSClientIdentifier wsClientId = (WSClientIdentifier)wsclientsEnum.nextElement();
      switch (featureType) {
        case NO_FEATURE : {
          wsclientsArr.add(wsclients.get(wsClientId));
          break;
        }
        case APPLICATION_FEATURE : {
          if (wsClientId.getApplicationName().equals(featureValue)) {
            wsclientsArr.add(wsclients.get(wsClientId));
          }
          break;
        }
      }
    }
    return wsclientsArr;
  }

  public String[] listApplications() {
    if ( wsclients == null) return new String[0];
    Set appNamesSet = new Set();
    Enumeration wsEnum = wsclients.keys();
    while(wsEnum.hasMoreElements()) {
      WSClientIdentifier wsClientId = (WSClientIdentifier)wsEnum.nextElement();
      appNamesSet.add(wsClientId.getApplicationName());
    }

    int appNamesSize = appNamesSet.size();
    String[] appNames = new String[appNamesSize];
    Enumeration enum1 = appNamesSet.elements();
    int i = 0;
    while(enum1.hasMoreElements()) appNames[i++] = (String)enum1.nextElement();

    return appNames;
  }

  public void reuse() {
    this.wsclients = null;
  }

  private WSClientRuntimeInfo[] getWSClientsAsArray(ArrayList wsClientsArrayList) {
    int wsSize = wsClientsArrayList.size();
    WSClientRuntimeInfo[] wsClientsArray = new WSClientRuntimeInfo[wsSize];
    for (int i = 0; i < wsSize; i++) wsClientsArray[i] = (WSClientRuntimeInfo)wsClientsArrayList.get(i);
    return wsClientsArray;
  }

  private WSClientIdentifier[] getWSClientIdsAsArray(ArrayList wsClientIdsArrayList) {
    int wsSize = wsClientIdsArrayList.size();
    WSClientIdentifier[] wsClientIdsArray = new WSClientIdentifier[wsSize];

    for (int i = 0; i < wsSize; i++) {
      wsClientIdsArray[i] = (WSClientIdentifier)wsClientIdsArrayList.get(i);
    }

    return wsClientIdsArray;
  }

}
