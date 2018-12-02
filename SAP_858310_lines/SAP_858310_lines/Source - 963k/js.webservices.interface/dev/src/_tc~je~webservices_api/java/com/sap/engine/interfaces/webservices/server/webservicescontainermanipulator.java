/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */

package com.sap.engine.interfaces.webservices.server;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

/**
 * Title: WebServicesContainerManipulator 
 * Description: WebServicesContainerManipulator
 * 
 * @author Aneta Angova
 * @author Dimitrina Stoyanova
 */

public interface WebServicesContainerManipulator {
  
  public static final int DEPLOY_MODE = 0;
  public static final int RUNTIME_MODE = 1;
  public static final int OFFLINE_MODE = 2; 
	
  /**
   * WSDL types
   */
  public enum WsdlSection{SERVICE, BINDING, PORTTYPE};
  
  /**
   * @param com.sap.engine.services.webservices.espbase.configuration.Service service  
   * @param applicationName
   * @exception Exception
   */
  public void createService(Object service, String applicationName) throws Exception;

  /**
   * @param com.sap.engine.services.webservices.espbase.configuration.Service service  
   * @param applicationName
   * @exception Exception
   */
  public void updateService(Object service, String applicationName) throws Exception;
  
  /**
   * @param com.sap.engine.services.webservices.espbase.configuration.Service service  
   * @param applicationName
   * @exception Exception
   */
  public void deleteService(Object service, String applicationName) throws Exception;

  public String getInterfaceDefinitionId(String bindingDataURL);

  /**
   * @return com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition
   * @deprecated use getInterfaceDefinitionById(...)
   */  
  public Object getInterfaceDefinitionByName(String ifDefName);
  
  /**
   * @return com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition
   */  
  public Object getInterfaceDefinitionById(String ifDefId);
  
  /**
   * @return com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition
   */
  public Object getInterfaceDefinitionById(String applicationName, String interfaceDefinitionId, int mode);
  
  public String[] getApplicationNames();
  
  /**
   * @return com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition[] 
   */
  public Object[] getInterfaceDefinitionsByApplication(String applicationName);
  
  /**
   * @return com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition[] 
   */
  public Object[] getInterfaceDefinitionsByApplicationName(String applicationName, boolean isRuntimeMode);

  /**
   * @return com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition[] 
   */
  public Object[] getInterfaceDefinitionsForApplication(String applicationName, int mode);
  
  /**
   * @return com.sap.engine.services.webservices.espbase.configuration.Service[]  
   */
  public Object[] getServicesByApplication(String applicationName);
  
  /**
   * @param com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition interfaceDefinition
   * @return com.sap.engine.services.webservices.espbase.configuration.Service[]  
   */
  public Object[] getServicesPerInterface(Object interfaceDefinition);
  
  /**
   * @return com.sap.engine.services.webservices.espbase.configuration.Service[]  
   */
  public Object[] getServicesForInterfaceDefinition(String applicationName, String interfaceDefinitionId, int mode); 
  
  /**
   * @param com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition interfaceDefinition  
   */
  public String getApplicationOfInterface(Object interfaceDefinition);
   
  public String getApplicationOfInterfaceDefinition(String interfaceDefinitionId, int mode) throws Exception; 
  
  public void startApplicationAndWait(String applicationName) throws RemoteException; 
 
  public Object getServiceImplInstance(String applicationName, String siName) throws NameNotFoundException, NamingException;
  
  public Set getWSDLStyles(String bdURL) throws Exception;
  
  /**
   * @return com.sap.engine.services.webservices.espbase.configuration.ConfigurationManipulator
   */
  public Object getConfigurationManipulator() throws Exception;
  
  /**
   * @return com.sap.engine.services.webservices.espbase.server.BuiltInWSEndpointManager
   */
  public Object getBuiltInWSEndpointManager();
    
  public Object getSRPublication(String applicationName, String interfaceDefinitionID, boolean isRuntimeMode) throws Exception;
  
  public Object[] getSRPublicationMetaDataDescriptors(String applicationName, boolean isRuntimeMode) throws Exception;
  
  public String constructBindingDataURLPath(String contextRoot, String bindingDataURL); 
  
  /**
   * Returns the ws endpoint url for a particular ws port with host and port omitted, e.g.
   *  
   *  http:///contextRoot/bdUrl
   *
   * @param contextRoot (as listed in telnet - list_ws)
   * @param bdUrl       (as listed in telnet - list_ws)
   * @return WS endpoint URL
   * @throws WSLocalCallException       - if the service is configured for local calls
   * @throws NoSuchWebServiceException  - if no binding data is found for this contextRoot/bdUrl
   */
  public String getWSEndpointURL(String contextRoot, String bdUrl);
  
  /** 
   * Returns url path for service, binding or porttype policy wsdls, depending on the value of wsSection 
   * @param contextRoot (as listed in telnet - list_ws)
   * @param bdUrl       (as listed in telnet - list_ws)
   * @param wsSection - indicates which url path is requested - service, binding, porttype 
   * @return policy url path
   * @throws NoSuchWebServiceException          - if no binding data is found for this contextRoot/bdUrl
   * @throws WSPolicyModeNotSupportedException  - if the web service at contextRoot/bdUrl does not support ws_policy wsdls
   */ 
  public String getWSPolicyWsdlURLPath(String contextRoot, String bdUrl, WsdlSection wsSection);
    
  /**
   *  Returns all the possible wsdl paths (policy, etc) for a web service. This would be used for visualization in the NWA.
   *  @param contextRoot (as listed in telnet - list_ws)
   *  @param bdUrl       (as listed in telnet - list_ws)
   *  @return all possible wsdl paths for the web service at contextRoot/bdUrl
   *  @throws NoSuchWebServiceException  - if no binding data is found for this contextRoot/bdUrl
   */ 
  public List<String> getAllWSDLURLPaths(String contextRoot, String bdUrl);
  
  public String getSCAName(String applicationName); 
	   
  public String getSCAVendor(String applicationName);
  
}
