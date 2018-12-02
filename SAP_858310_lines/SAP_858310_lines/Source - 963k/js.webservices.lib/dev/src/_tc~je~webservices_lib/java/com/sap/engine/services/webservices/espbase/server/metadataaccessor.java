/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.server;

import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.rpc.encoding.TypeMappingRegistry;

import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;


/**
 * This interface serves as a bridge to the web services deploy registries.
 * It provides access methods which are used by the runtime in order to 
 * obtain the data necessary at runtime.  
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-10-5
 */
public interface MetaDataAccessor {
  /**
   * Denotes 'http' port id
   */
  public static final String PORT_HTTP = "http";
  /**
   * Denotes 'https' port id
   */
  public static final String PORT_HTTPS = "https";
  
  /**
   * @param uriID the requestURI on which the runtime has been invoked.
   * @return BindingData mapped under the <code>uriID</code>. If nothing is bound under <code>uriID</code>
   *         null is returned.  
   */
  public BindingData getBindingData(String uriID);
  /**
   * @param uriID the requestURI on which the runtime has been invoked.
   * @return InterfaceMapping mapped under the <code>uriID</code>. If nothing is bound under <code>uriID</code>
   *         null is returned.  
   */
  public InterfaceMapping getInterfaceMappingForBindingData(String uriID);
  /**
   * @param uriID the requestURI on which the runtime has been invoked.
   * @return InterfaceDefinition mapped under the <code>uriID</code>. If nothing is bound under <code>uriID</code>
   *         null is returned.  
   */
  public InterfaceDefinition getInterfaceDefinitionForBindingData(String uriID);
  /**
   * @param uriID the requestURI on which the runtime has been invoked.
   * @return TypeMappingRegistry mapped under the <code>uriID</code>. If nothing is bound under <code>uriID</code>
   *         null is returned.  
   */
  public TypeMappingRegistry getTypeMappingRegistryForBindingData(String uriID);
  /**
   * @param uriID the requestURI on which the runtime has been invoked.
   * @return the name of the application which provides the <code>uriID</code>. If for <code>uriID</code>
   * has no application defined null is returned.  
   */
  public String getApplicationName(String uriID);
  /**
   * @param uriID the requestURI on which the runtime has been invoked.
   * @return the name of the web service which provides the <code>uriID</code>. If for <code>uriID</code>
   * has no web service defined null is returned.  
   */
  public String getWebServiceName(String uriID);
  /**
   * @param appName application name
   * @return array of Service instances relevant for <code>appName</code>, or null if the application has no 
   *         web services
   */
  public Service[] getServices(String appName);
  /**
   * @param uriID the requestURI on which the runtime has been invoked.
   * @param type the wsdl type (binding | interface |...)
   * @param style the wsdl style (rpc | document | default |...);
   * @return the absolute path to the wsdl, or null if the wsdl path cannot be resolved.
   */
  public String getWsdlPath(String uriID, String type, String style);
  /**
   * @param uriID the requestURI on which the runtime has been invoked.
   * @param type the wsdl type (binding | interface |...)
   * @param style the wsdl style (rpc | document | default |...);
   * @return the relative path of a wsdl, or null if the wsdl path cannot be resolved.
   */
  public String getWSDLRelPath(String uriID, String type, String style);
  /**
   * @param uriID the requestURI on which the runtime has been invoked.
   * @return Service mapped under the <code>uriID</code>. If nothing is bound under <code>uriID</code>
   *         null is returned.  
   */
  public Service getServiceForBindingData(String uriID);
  /**
   * @param portID the type of the server port. The constants PORT_HTTP and PORT_HTTPS should used as parameters.
   * @return the port number, or -1 if such port is not initialized.
   */
  public int getServerPort(String portID);
  /**
   * @param uriID the requestURI on which the runtime has been invoked.
   * @return a Set of string objects representing the available wsdl styles for the <code>uriID</code>.
   */
  public Set getWSDLStyles(String uriID);
  /**
   * Returns the JAXBContext for particular binding data. If this is nоt a JEE service null is returned.
   * @param bindingDataUrl
   * @return
   */
  public JAXBContext getJAXBContextForBindingData(String bindingDataUrl);
}
