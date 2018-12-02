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

import javax.xml.rpc.encoding.TypeMappingRegistry;

import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.Variant;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;

/**
 * This interface provides methods for dynamic registration and unregistration of web services
 * endpoints which provide specific services to the WS framework. Such endpoints
 * are WS-RM and MetaDataExchange endpoints.
 * 
 * The web service endpoint implementations must be thread safe in order one instance 
 * to serve multiple concurrent requests. 
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-12-14
 */
public interface BuiltInWSEndpointManager {
  /**
   * Constant denoting the id of the 'BuiltIn' implementation container.  
   */
  public static final String IMPLEMENTATION_CONTAINER_ID  =  "BuiltInWSEndpointImplContainer";
  /**
   * Constant denoting the 'action' implementation container property.  
   */
  public static final String ACTION_PROPERTY  =  "action";
  /**
   * Registers <code>ep</code> instance with its corresponding <code>intfMapping</code> mapping 
   * as 'builtIn' endpoint for all runtime configurations. The <code>intfMapping</code> must have implementation link 
   * defined with 'implementation-id' set to <code>IMPLEMENTATION_CONTAINER_ID</code>.
   * The <code>actions</code> in conjunction with <code>appName</code>, <code>wsName</code> and <code>bdName</code> 
   * is used to constract a string key under which the <code>ep</code>, <code>intfMapping</code> and <code>tmReg</code> are mapped. 
   * The key is returned by the method, and the same key must be used as parameter of <code>unregisterEndpoint(String key)</code> in
   * order to unregister the endpoint.
   *
   * @param appName name of the application that contains the web service runtime config for which 'builtIn' endpoint will be registered.
   * @param wsName name of the webservice within the application
   * @param bdName name of BindingData (runtime configuration)
   * @param runtimeCfg runtime configuration for which the 'builtIn' endpoint is assigned. 
   * @param ep web service endpoint
   * @param intfMapping endpoint mapping
   * @param tmReg initialized and ready for use type mapping registry
   * @return the key under which the 'builtIn' endpoint is mapped.  
   */
  public List registerEndpoint(String appName, String wsName, String bdName, String[] actions, Object ep, InterfaceMapping intfMapping, TypeMappingRegistry tmReg) throws RuntimeProcessException;
  /**
   * Registers <code>ep</code> instance with its corresponding <code>intfMapping</code> mapping, variant <code>v</code>,
   * and bindingData <code>bd</code> as 'builtIn' endpoint for all runtime configurations.
   * The <code>intfMapping</code> must have implementation link defined with 'implementation-id' set to <code>IMPLEMENTATION_CONTAINER_ID</code> and property
   * <code>ACTION_PROPERTY</code> set to the action value for which the registered endpoint is relevant.
   * The value of <code>ACTION_PROPERTY</code> in conjunction with <code>appName</code>, <code>wsName</code> and <code>bdName</code> 
   * is used to constract a string key under which the <code>ep</code>, <code>intfMapping</code> and <code>tmReg</code> are mapped. 
   * The key is returned by the method, and the same key must be used as parameter of <code>unregisterEndpoint(String key)</code> in
   * order to unregister the endpoint.
   *    
   * @param appName name of the application that contains the web service runtime config for which 'builtIn' endpoint will be registered.
   * @param wsName name of the webservice within the application
   * @param bdName name of BindingData (runtime configuration)
   * @param ep web service endpoint
   * @param intfMapping endpoint mapping
   * @param tmReg initialized and ready for use type mapping registry
   * @param newVariant endpoint runtime configuration
   * @param newBD endpoint design-time configuration
   * @return the key under which the 'builtIn' endpoint is mapped.  
   */
  public List registerEndpoint(String appName, String wsName, String bdName, String[] actions, Object ep, InterfaceMapping intfMapping, TypeMappingRegistry tmReg, Variant newVariant, BindingData newBD) throws RuntimeProcessException;
  /**
   * Unregisters the endpoint bound under <code>key</code>.
   * @return the unregistered endpoint instance or null if nothing has been registered under <code>actionKey</code> key.
   */
  public Object unregisterEndpoint(String key) throws RuntimeProcessException;
}
