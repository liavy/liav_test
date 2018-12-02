package com.sap.engine.services.webservices.runtime.registry;

import com.sap.engine.interfaces.webservices.runtime.definition.WSIdentifier;
import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.lib.util.Set;
import com.sap.engine.services.webservices.exceptions.RegistryException;
import com.sap.engine.services.webservices.exceptions.PatternKeys;
import com.sap.engine.services.webservices.runtime.definition.WSRuntimeDefinition;

import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2000
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */
public class WebServiceRegistry {

  public static final int NO_FEATURE          = 0;
  public static final int APPLICATION_FEATURE = 1;

  private HashMapObjectObject services = null;

  public WebServiceRegistry() {

  }

  public void registerWebService(WSRuntimeDefinition wsRuntimeDefintion) throws RegistryException {
    if (services == null) {
      services = new HashMapObjectObject();
    }
    WSIdentifier wsIdentifier = wsRuntimeDefintion.getWSIdentifier();
    if (services.get(wsIdentifier) != null) {
      throw new RegistryException(PatternKeys.WS_DUBLICATE_ELEMENT, new Object[]{wsIdentifier, this.getClass().getName()});
    }
    services.put(wsIdentifier, wsRuntimeDefintion);
  }

  public void unregisterWebService(WSIdentifier wsIdentifier) {
    if (services != null) {
      services.remove(wsIdentifier);
    }
  }

  public boolean contains(String applicationName, String wsName) {
    return contains(new WSIdentifier(applicationName, null, wsName));
  }

  public boolean contains(WSIdentifier wsIdentifier) {
    if (services == null) {
      return false;
    }
    return services.containsKey(wsIdentifier);
  }

  public WSRuntimeDefinition getWebService(String applicationName, String wsName) throws RegistryException {
    WSIdentifier wsIdentifier = new WSIdentifier(applicationName, null, wsName);
    return getWebService(wsIdentifier);
  }

  public WSRuntimeDefinition getWebService(WSIdentifier wsIdentifier) throws RegistryException {
    if (services != null) {
      return (WSRuntimeDefinition) services.get(wsIdentifier);
    } else throw new RegistryException(PatternKeys.NO_SUCH_WS_ELEMENT, new Object[]{wsIdentifier.toString(), this.getClass().getName()});
  }

  public WSIdentifier[] listWebServices() {
   return getWSIdsAsArray(listWebServicesByFeature(NO_FEATURE, ""));
  }

  public WSIdentifier[] listWebServicesByApplicationName(String applicationName) {
    return getWSIdsAsArray(listWebServicesByFeature(APPLICATION_FEATURE, applicationName));
  }

  public ArrayList listWebServicesByFeature(int featureType, String featureValue) {
    ArrayList servicesNamesArr = new ArrayList();
    if (services == null) return servicesNamesArr;

    Enumeration servicesEnum = services.keys();
    while (servicesEnum.hasMoreElements()) {
      WSIdentifier wsIdentifier = (WSIdentifier)servicesEnum.nextElement();
      switch (featureType) {
        case NO_FEATURE : {

          servicesNamesArr.add(wsIdentifier);
          break;
        }
        case APPLICATION_FEATURE: {
          String applicationName = wsIdentifier.getApplicationName();
          if (applicationName.equals(featureValue)) {
            servicesNamesArr.add(wsIdentifier);
          }
        }
      }
    }
    return servicesNamesArr;
  }

  public WSRuntimeDefinition[] getWebServices() {
    return getWSAsArray(getWebServicesByFeature(NO_FEATURE, ""));
  }

  public WSRuntimeDefinition[] getWebServicesByApplicationName(String applicationName) {
    return getWSAsArray(getWebServicesByFeature(APPLICATION_FEATURE, applicationName));
  }

  public ArrayList getWebServicesByFeature(int featureType, String featureValue) {
    ArrayList servicesArr = new ArrayList();
    if (services == null) {
      return servicesArr ;
    }

    Enumeration servicesEnum = services.elements();
    while (servicesEnum.hasMoreElements()) {
      WSRuntimeDefinition wsRuntimeDefinition = (WSRuntimeDefinition)servicesEnum.nextElement();
      switch (featureType) {
        case NO_FEATURE : {
          servicesArr.add(wsRuntimeDefinition);
          break;
        }
        case APPLICATION_FEATURE : {
          if (wsRuntimeDefinition.getApplicationName().equals(featureValue)) servicesArr.add(wsRuntimeDefinition);
          break;
        }
      }
    }
    return servicesArr;
  }

  public String[] listApplications() {
    if (services == null) return new String[0];
    Set appNamesSet = new Set();
    Enumeration wsEnum = services.elements();
    while(wsEnum.hasMoreElements()) {
      WSRuntimeDefinition wsRuntimeDefinition = (WSRuntimeDefinition)wsEnum.nextElement();
      appNamesSet.add(wsRuntimeDefinition.getApplicationName());
    }

    int appNamesSize = appNamesSet.size();
    String[] appNames = new String[appNamesSize];
    Enumeration enum1 = appNamesSet.elements();
    int i = 0;
    while(enum1.hasMoreElements()) appNames[i++] = (String)enum1.nextElement();

    return appNames;
  }

  private WSRuntimeDefinition[] getWSAsArray(ArrayList wsArrayList) {
    int wsSize = wsArrayList.size();
    WSRuntimeDefinition[] wsArray = new WSRuntimeDefinition[wsSize];
    for (int i = 0; i < wsSize; i++) wsArray[i] = (WSRuntimeDefinition)wsArrayList.get(i);
    return wsArray;
  }

  private WSIdentifier[] getWSIdsAsArray(ArrayList wsIdsArrayList) {
    int wsSize = wsIdsArrayList.size();
    WSIdentifier[] wsIdsArray = new WSIdentifier[wsSize];
    for (int i = 0; i < wsSize; i++) wsIdsArray[i] = (WSIdentifier)wsIdsArrayList.get(i);
    return wsIdsArray;
  }

}

