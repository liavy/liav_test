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
package com.sap.engine.services.webservices.espbase.server;

import javax.xml.bind.JAXBContext;
import javax.xml.rpc.encoding.TypeMappingRegistry;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.Variant;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.tc.logging.Location;

/**
 *  <p>This context holds data that is not modifiable. In general this is the data provided
 * at deploy time.</p>
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-9-10
 */
public interface StaticConfigurationContext extends ConfigurationContext {
  /**
   * Returns the protocolsID, in order as specified at deployment time.
   * 
   */
  public String[] getProtocolsOrder();  
  /**
   * Returns the WS name
   */
  public String getWebServiceName();
  /**
   * Returns the name of the application which will be accessed.
   * If not resolved or Exception occurs returns null;
   */
  public String getTargetApplicationName();
  /**
   * Returns initialized type mapping registry ready for use. 
   * It is possible null to be returned. In this case the <code>getJAXBContext</code> method
   * should not return null. 
   */
  public TypeMappingRegistry getTypeMappingRegistry();
  /**
   * Returns initialized JAXBContext ready for use.
   * It is possible null to be returned. In this case the <code>getTypeMappingRegistry</code> method
   * should not return null. 
   */
  public JAXBContext getJAXBContext();
  /**
   * Returns the Location for the webservices.
   */
  @Deprecated
  public Location getLogLocation();
  /**
   * @return Design-time part of the configuration. Generally it should not be necessary runtime.
   */
  public Variant getDTConfiguration();
  /**
   * @return Run-time part of the configuration.
   */
  public BindingData getRTConfiguration();
  /**
   * @return InterfaceMapping object associated with the current call.
   */  
  public InterfaceMapping getInterfaceMapping();
  /**
   * @return the request URI for the current configuration.
   */
  public String getEndpointRequestURI();
///**
// * Returns features associated with <tt>protocolName</tt> protocol. 
// * @param protocolName
// */
//public Map getFeatures(String protocolName);
//
///**
// * Returns feature associated with the currently requested endpoint. 
// */
//public Map getFeatures();
//  
// 
//  /**
//   *
//   * @return the instance security policy
//   */
//  public String getSecurityPolicy();
//
//  /**
//   *  Returns the implementatin link associatied
//   * with this call.
//   */
//  public com.sap.engine.interfaces.webservices.runtime.ImplLink getImplementationLink();
//
//  public JavaToQNameMappingRegistry getJavaToQNameMappingRegistry();
 
//  /**
//   * Returns the WSD file name.
//   */
//  //public String getWSDName();
//
//  /**
//   * Returns the VI file name.
//   */
//  public String getVIName();
//
//  /**
//   * Lists all the operations configured for
//   * the endpoint.
//   */
//  public OperationDefinition[] listEndpointOperations();   
}
