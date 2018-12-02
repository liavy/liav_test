/*
 * Copyright (c) 2003 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.client.bindings;

import java.util.concurrent.Executor;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.TypeMappingRegistry;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.configuration.ServiceData;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;

/**
 * Root context for web service client service instances.
 * It contains information about the service instance.
 * TypeMappingRegistry, MappingRules, ConfugurationRoot,
 * Service Name, Application ClassLoader, and also reference to 
 * ServiceData object for corresponding service.
 * @version 1.0
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public interface ClientServiceContext extends ConfigurationContext {

  public static final String CLIENT_TYPE = "ClientType";
  public static final String JEE5 = "JEE5";
  public static final String DYNAMIC = "DYNAMIC";
  public static final String JAXRPC = "JAXRPC";
  
  public boolean isStandalone();

  /**
   * Sets client type of this ClientServiceContext instance.
   * @param clientType
   */
  public void setClientType(String clientType);  
  
  /**
   * Returns the client type of this ClientServiceContext instance.
   * @return
   */
  public String getClientType();  
  
  public Executor getExecutor();
  
  public void setExecutor(Executor executor);
  
  /**
   * Returns working service name. 
   * @return
   */
  public QName getServiceName();
  
  /**
   * Returns application classloader.
   * @return
   */
  public ClassLoader getApplicationClassLoader();
  
  /**
   * Returns complete configuration for this web service client.
   * @return
   */
  public ConfigurationRoot getCompleteConfiguration();
  
  /**
   * Returns configuration information for the corresponfing service.
   * @return
   */ 
  public ServiceData getServiceData();
  
  /**
   * Returns type mapping registry used for this service.
   * @return
   */
  public TypeMappingRegistry getTypeMappingRegistry(); 
  
  /**
   * Returns mapping rules for this web service client.
   * @return
   */
  public MappingRules getMappingRules();     
  
  /**
   * Returns the application name in which this client was deployed.
   * @return
   */
  public String getApplicationName();
  
  /**
   * Sets Helper Context for SDO serialziation.
   * @param helperContext
   */
  //public void setHelperContext(HelperContext helperContext);
  
  /**
   * Returns Helper Context used for SDO serialization.
   * @return
   */
  //public HelperContext getHelperContext();
  
  /**
   * Sets JAXBContext used for serialization of JAXB Objects.
   * @param jaxbContext
   */
  public void setJAXBContext(JAXBContext jaxbContext);
  
  /**
   * Returns the JAXBContext used for serialization of JAXB Objects.
   * @return
   */
  public JAXBContext getJAXBContext();
  
}
