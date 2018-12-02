package com.sap.engine.services.webservices.runtime.registry;

import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.services.webservices.exceptions.RegistryException;
import com.sap.engine.services.webservices.exceptions.PatternKeys;
import com.sap.engine.services.webservices.runtime.definition.ServiceEndpointDefinition;

import java.util.Enumeration;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2000
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */
public class RuntimeRegistry {

  private HashMapObjectObject serviceEndpoints = null;

  public void registerServiceEndpoint(ServiceEndpointDefinition serviceEndpoint) throws RegistryException {
    if (serviceEndpoints == null) {
      serviceEndpoints = new HashMapObjectObject();
    }
    String serviceEndpointId = serviceEndpoint.getServiceEndpointId();
    if (serviceEndpoints.get(serviceEndpointId) != null) {
      throw new RegistryException(PatternKeys.WS_DUBLICATE_ELEMENT, new Object[]{serviceEndpointId, this.getClass().getName()});
    }
    serviceEndpoints.put(serviceEndpointId, serviceEndpoint);
  }

  public void unregisterServiceEndpoint(String serviceEndpointId) {
    if (serviceEndpoints != null) {
      serviceEndpoints.remove(serviceEndpointId);
    }
  }

  public boolean contains(String serviceEndpointId) {
    if (serviceEndpoints == null) {
      return false;
    }
    return serviceEndpoints.containsKey(serviceEndpointId);
  }

  public ServiceEndpointDefinition getServiceEndpoint(String serviceEndpointId) throws RegistryException {
    if (serviceEndpoints != null) {
      ServiceEndpointDefinition endPoint = (ServiceEndpointDefinition) serviceEndpoints.get(serviceEndpointId);
      if (endPoint != null) {
        return endPoint;
      }
    }

    throw new RegistryException(PatternKeys.NO_SUCH_WS_ELEMENT, new Object[]{serviceEndpointId, this.getClass().getName()});
  }

  public String[] listServiceEndpoints() {
    if (serviceEndpoints == null) {
      return new String[0];
    }
    int serviceEndpointsSize = serviceEndpoints.size();
    String[] serviceEndpointsNames = new String[serviceEndpointsSize];
    Enumeration serviceEndpointsEnum = serviceEndpoints.keys();
    int i = 0;
    while (serviceEndpointsEnum.hasMoreElements()) {
      serviceEndpointsNames[i++] = (String) serviceEndpointsEnum.nextElement();
    }
    return serviceEndpointsNames;
  }

  public ServiceEndpointDefinition[] getServiceEndpoints() {
    if (serviceEndpoints == null) {
      return new ServiceEndpointDefinition[0];
    }
    int serviceEndpointsSize = serviceEndpoints.size();
    ServiceEndpointDefinition[] serviceEndpointsArr = new ServiceEndpointDefinition[serviceEndpointsSize];
    Enumeration serviceEndpointsEnum = serviceEndpoints.elements();
    int i = 0;
    while (serviceEndpointsEnum.hasMoreElements()) {
      serviceEndpointsArr[i++] = (ServiceEndpointDefinition) serviceEndpointsEnum.nextElement();
    }
    return serviceEndpointsArr;
  }

}

