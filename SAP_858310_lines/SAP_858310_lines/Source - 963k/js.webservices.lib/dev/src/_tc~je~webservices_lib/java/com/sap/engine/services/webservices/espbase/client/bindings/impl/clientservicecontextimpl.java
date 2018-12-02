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
package com.sap.engine.services.webservices.espbase.client.bindings.impl;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.TypeMappingRegistry;

import com.sap.engine.services.webservices.espbase.ConfigurationContextImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientServiceContext;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.configuration.ServiceData;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;

/**
 * Context representind web service client service state. It contains the mains
 * service properties.
 * @version 1.0
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public class ClientServiceContextImpl extends ConfigurationContextImpl implements ClientServiceContext {
  
  public static final String APPLICATION_LOADER = "ApplicationLoader";
  public static final String TYPE_MAPPING_REGISTRY = "TypeMappingRegistry";
  public static final String HELPER_CONTEXT = "HelperContext";
  public static final String JAXB_CONTEXT = "JAXBContext";
  public static final String CONFIGURATION_ROOT = "ConfigurationRoot";
  public static final String SERVICE_DATA = "ServiceData";
  public static final String SERVICE_NAME = "ServiceName";
  public static final String HANDLER_REGISTRY = "HandlerRegistry";
  public static final String MAPPING_RULES = "MappingRules";
  public static final String SERVICE_PROTOCOLS = "ServiceProcols";  
  public static final String APPLICATION_NAME = "ApplicationName";
  public static final String JNDI_NAME = "JndiName";
  public static final String WSDL_URL = "WSDL_URL";
  public static final String LMT_NAME = "LMT_Name";
  public static final String IF_NAME = "IF_Name";
  public static final String SERVICE_REF_ID = "ServiceRefID";
  public static final String IS_STANDALONE = "IsStandalone";
  
  private transient Executor defaultExecutor = null;
  private transient Executor customExecutor = null;
  
  public ClientServiceContextImpl()  {
    super("ServiceContext",null,ConfigurationContextImpl.NORMAL_MODE);    
  }
  
  /**
   * Sets client type of this ClientServiceContext instance.
   * @param clientType
   */
  public void setClientType(String clientType) {
    if (clientType == null) {
      super.removeProperty(CLIENT_TYPE);
    } else {
      super.setProperty(CLIENT_TYPE,clientType);
    }
  }
  
  /**
   * Returns the client type of this ClientServiceContext instance.
   * @return
   */
  public String getClientType() {
    return (String) super.getProperty(CLIENT_TYPE);
  }
  
  public synchronized void setExecutor(Executor executor) {
    this.customExecutor = executor;
  }
  
  public synchronized Executor getExecutor() {
    if (this.customExecutor != null) {
      return this.customExecutor;
    } else {
      if (this.defaultExecutor == null) {
        this.defaultExecutor = Executors.newCachedThreadPool();
      }
      return this.defaultExecutor;
    }    
  }
  
  /**
   * Returns true if the client is standalone.
   * @return
   */
  public boolean isStandalone() {
    return (super.getProperty(IS_STANDALONE) != null);
  }
  
  /**
   * Sets proxy type.
   * @param isStandalone
   */
  public void setStandalone(boolean isStandalone) {
    if (isStandalone) {
      super.setProperty(IS_STANDALONE,"true");
    } else {
      super.removeProperty(IS_STANDALONE);
    }
  }

  /**
   * Returns the application class loader.
   * @return
   */
  public ClassLoader getApplicationClassLoader() {    
    return (ClassLoader) super.getProperty(APPLICATION_LOADER);
  }
  
  /**
   * Sets the service application class loader.
   * @param applicationLoader
   */
  public void setApplicationClassLoader(ClassLoader applicationLoader) {
    super.setProperty(APPLICATION_LOADER,applicationLoader);
  }

  /**
   * Returns complete service configuration.
   * @return
   */
  public ConfigurationRoot getCompleteConfiguration() {    
    return (ConfigurationRoot) super.getProperty(CONFIGURATION_ROOT);
  }
  
  /**
   * Sets complete service configuration.
   * @param configRoot
   */
  public void setCompleteConfiguration(ConfigurationRoot configRoot) {
    super.setProperty(CONFIGURATION_ROOT,configRoot);
  }

  /**
   * Returns the service data for this service config.
   * @return
   */
  public ServiceData getServiceData() {
    //return (ServiceData) super.getProperty(SERVICE_DATA);
    return _getServiceData(getServiceName());
  }
  
  /**
   * Returns the service data for this service. 
   * @param serviceName
   * @return
   */
  private com.sap.engine.services.webservices.espbase.configuration.ServiceData _getServiceData(QName serviceName) {
    com.sap.engine.services.webservices.espbase.configuration.Service[] services = getCompleteConfiguration().getRTConfig().getService();
    for (int i=0; i<services.length; i++) {
      
      com.sap.engine.services.webservices.espbase.configuration.ServiceData sData = services[i].getServiceData();
      
      if (serviceName.equals(new QName(sData.getNamespace(),sData.getName()))) {
        return sData;
      }
    }
    return null;
  }  
  
  
  /**
   * Sets the service data for corresponding service.
   * @param serviceData
   */
  public void setServiceData(ServiceData serviceData) {
    super.setProperty(SERVICE_DATA,serviceData);
  }

  /**
   * Returns service name.
   * @return
   */
  public QName getServiceName() {
    return (QName) super.getProperty(SERVICE_NAME);
  }
  
  /**
   * Sets service name.
   * @param serviceName
   */
  public void setServiceName(QName serviceName) {
    super.setProperty(SERVICE_NAME, serviceName);
  }

  /**
   * Returns type mapping registry.
   * @return
   */
  public TypeMappingRegistry getTypeMappingRegistry() {    
    return (TypeMappingRegistry) super.getProperty(TYPE_MAPPING_REGISTRY);
  }
  
  /**
   * Sets the type mapping registry. 
   */
  public void setTypeMappingRegistry(TypeMappingRegistry typeRegistry) {
    super.setProperty(TYPE_MAPPING_REGISTRY,typeRegistry);
  }
  
  /**
   * Sets Helper Context for SDO serialziation.
   * @param helperContext
   */
  /*
  public void setHelperContext(HelperContext helperContext) {
    super.setProperty(HELPER_CONTEXT, helperContext);    
  }*/
  
  /**
   * Returns Helper Context used for SDO serialization.
   * @return
   */
  /*
  public HelperContext getHelperContext() {
    return (HelperContext) super.getProperty(HELPER_CONTEXT);
  }*/
  
  /**
   * Sets JAXBContext used for serialization of JAXB Objects.
   * @param jaxbContext
   */
  public void setJAXBContext(JAXBContext jaxbContext) {
    super.setProperty(JAXB_CONTEXT,jaxbContext);
  }
  
  /**
   * Returns the JAXBContext used for serialization of JAXB Objects.
   * @return
   */
  public JAXBContext getJAXBContext() {
    return (JAXBContext) super.getProperty(JAXB_CONTEXT);
  }

  /**
   * Rerturns the mapping rules for this web service client.
   * @return
   */
  public MappingRules getMappingRules() {
    return (MappingRules) super.getProperty(MAPPING_RULES);
  }
  
  /**
   * Sets mapping rules for web service client.
   * @param mappingRules
   */
  public void setMappingRules(MappingRules mappingRules) {
    super.setProperty(MAPPING_RULES,mappingRules);
  }
  
  /**
   * Returns the application name.
   * @return
   */
  public String getApplicationName() {
    return (String) super.getProperty(APPLICATION_NAME);
  }
  
  /**
   * Sets the application name.
   * @param applicationName
   */
  public void setApplicationName(String applicationName) {
    super.setProperty(APPLICATION_NAME,applicationName);
  }
}
