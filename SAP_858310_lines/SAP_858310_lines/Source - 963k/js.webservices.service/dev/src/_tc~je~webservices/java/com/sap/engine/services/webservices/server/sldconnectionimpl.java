/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.server;

import java.rmi.RemoteException;
import java.lang.reflect.Method;

import com.sap.engine.services.webservices.jaxrpc.wsdl2java.SLDConnection;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.SLDPort;
import com.sap.engine.services.webservices.server.lcr.api.cim.CIMClass;
import com.sap.engine.services.webservices.server.lcr.api.cim.CIMClassname;
//import com.sap.lcr.api.cim.CIMClassname;
//import com.sap.lcr.api.cim.CIMFactory;
import com.sap.engine.services.webservices.server.lcr.api.cim.CIMFactory;
//import com.sap.lcr.api.cim.CIMInstance;
import com.sap.engine.services.webservices.server.lcr.api.cim.CIMInstance;
//import com.sap.lcr.api.cim.CIMInstancename;
import com.sap.engine.services.webservices.server.lcr.api.cim.CIMInstancename;
//import com.sap.lcr.api.cim.CIMValueNamedInstance;
import com.sap.engine.services.webservices.server.lcr.api.cim.CIMValueNamedInstance;
//import com.sap.lcr.api.cim.CIMValueNamedInstanceList;
import com.sap.engine.services.webservices.server.lcr.api.cim.CIMValueNamedInstanceList;
//import com.sap.lcr.api.cimclient.CIMNames;
import com.sap.engine.services.webservices.server.lcr.api.cimclient.CIMNames;
import com.sap.engine.services.webservices.server.lcr.api.cimclient.CIMOMClient;
import com.sap.engine.services.webservices.server.lcr.ObjectWrapper;
import com.sap.engine.services.webservices.server.lcr.api.cimclient.LcrException;
//import com.sap.lcr.api.cimclient.LcrException;

/**
 * @author Alexander Zubev (alexander.zubev@sap.com)
 */
public class SLDConnectionImpl implements SLDConnection {

  public SLDConnectionImpl() {

  }

  public static CIMOMClient getCIMOMClient() throws RemoteException {
    Object sldInterface = WSContainer.getSLDInterface();
    if (sldInterface == null) {
      throw new RemoteException("SLD service is stopped.");
    }

    CIMOMClient cimClient = null;
    try {
      Method getCimClientMethod = sldInterface.getClass().getMethod("getCimClient", null);
      Object clientObj = getCimClientMethod.invoke(sldInterface, null);
      cimClient = new CIMOMClient(clientObj); 
    } catch (Exception e) {
      throw new RemoteException("Unable to create CIM Client. SLDInterface instance of " + sldInterface.getClass().getName(), e);
    }

    if (cimClient == null) {
      throw new RemoteException("Cannot create CIM Client. Check SLD settings.");
    }
    return cimClient;
  }
  
  public static CIMValueNamedInstance getOldInstance(CIMOMClient cimClient, CIMClassname className, String name) throws Exception {
    return getOldInstance(cimClient, className, name, null);
  }

  public static CIMValueNamedInstance getOldInstance(CIMOMClient cimClient, CIMClassname className, String name, String systemName) throws Exception {
    CIMValueNamedInstanceList instances = cimClient.enumerateInstances(className, false, true, true, true, null);
    for (int i = 0; i < instances.size(); i++) {
      CIMValueNamedInstance valueNamedInstance = instances.get(i); 
      CIMInstance instance = valueNamedInstance.getInstance();
      String nameProp = instance.getPropertyValue(CIMNames.getProperty("P_NAME"));
      if (name.equals(nameProp)) {
        if (systemName == null || systemName.equals(instance.getPropertyValue(CIMNames.getProperty("P_SYSTEMNAME")))) {
          return valueNamedInstance;
        }
      }
    }
    return null;
  }
  
  public static void makeAssociation(CIMOMClient cimClient, CIMInstance leftInstance, CIMInstance rightInstance, CIMClassname associationType) throws Exception {
    CIMClass leftClass = cimClient.getCIMClass(leftInstance.getCIMClassname(), false, true, true, null);
    CIMClass rightClass = cimClient.getCIMClass(rightInstance.getCIMClassname(), false, true, true, null);
    CIMInstancename leftSide = leftInstance.buildInstanceName(leftClass);
    CIMInstancename rightSide = rightInstance.buildInstanceName(rightClass);
    CIMInstance associatedInstance = CIMFactory.instance(associationType, leftSide, "antecedent", rightSide, "dependent");
      
    try {
      cimClient.createInstance(associatedInstance);
    } catch (Exception exc) {
      if(ObjectWrapper.isInstanceOf(exc, LcrException.LCR_EXCEPTION_CLASS_NAME)) {
        LcrException lcre = new LcrException(exc);
        if (lcre.getStatusCode() != LcrException.getErrorCode("CIM_ERR_ALREADY_EXISTS")) {
          throw exc;
        }
      } else {
        throw exc;
      }
    }
  }
  
  /* 
   * @see com.sap.engine.services.webservices.jaxrpc.wsdl2java.SLDConnection#getFromSLD(java.lang.String, java.lang.String, java.lang.String)
   */
  public SLDPort getFromSLD(String sldSystem, String serviceName, String portName) throws Exception {
    CIMOMClient cimClient = getCIMOMClient();
    try {
      CIMValueNamedInstance webService = getOldInstance(cimClient, CIMNames.getCimClassNameValue("C_SAP_WebService"), serviceName, sldSystem);
      if (webService == null) {
        return null;
      }
      CIMValueNamedInstance webServicePort = getOldInstance(cimClient, CIMNames.getCimClassNameValue("C_SAP_WebServicePort"), portName, sldSystem);
      if (webServicePort == null) {
        return null;
      }
      String wsdlLocation = webService.getInstance().getPropertyValue(CIMNames.getProperty("P_WSDLLOCATION"));
      String wsdlPortName = webServicePort.getInstance().getPropertyValue(CIMNames.getProperty("P_WSDLPORTNAME"));
      return new SLDPort(wsdlLocation, wsdlPortName); 
    } finally {
      cimClient.disconnect();
    }
  }
}
