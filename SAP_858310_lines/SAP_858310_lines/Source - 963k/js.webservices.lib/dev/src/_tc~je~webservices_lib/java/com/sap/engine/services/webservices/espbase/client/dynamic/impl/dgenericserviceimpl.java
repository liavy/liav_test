/*
 * Created on 2005-7-15
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.engine.services.webservices.espbase.client.dynamic.impl;

import java.util.*;

import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.TypeMappingRegistry;

import org.xml.sax.EntityResolver;


import com.sap.engine.services.webservices.espbase.client.ProxyGeneratorConfigNew;
import com.sap.engine.services.webservices.espbase.client.ProxyGeneratorNew;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientServiceContext;
import com.sap.engine.services.webservices.espbase.client.bindings.DynamicServiceImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientServiceContextImpl;
import com.sap.engine.services.webservices.espbase.client.dynamic.DGenericService;
import com.sap.engine.services.webservices.espbase.client.dynamic.DInterface;
import com.sap.engine.services.webservices.espbase.client.dynamic.DestinationsHelper;
import com.sap.engine.services.webservices.espbase.client.dynamic.GenericServiceFactory;
import com.sap.engine.services.webservices.espbase.client.dynamic.ServiceFactoryConfig;
import com.sap.engine.services.webservices.espbase.client.dynamic.util.Util;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.impl.DBaseTypeImpl;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.impl.MetadataLoader;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationMarshallerFactory;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.Interface;
import com.sap.engine.services.webservices.jaxrpc.encoding.ExtendedTypeMapping;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;
import com.sap.engine.services.webservices.jaxrpc.exceptions.ProxyGeneratorException;
import com.sap.engine.services.webservices.jaxrpc.exceptions.WSResourceAccessor;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaToJavaConfig;
import com.sap.engine.services.webservices.tools.WSDLDownloadResolver;
import com.sap.exception.BaseRuntimeException;
import com.sap.tc.logging.Location;

/**
 * Implementation of the generic service interface.
 * @author Ivan Markov
 */
public class DGenericServiceImpl extends DDocumentableImpl implements DGenericService {
  
  private Hashtable lPortTypeNamesToMetadataMapping;
  private ClientServiceContextImpl serviceContext;
  private DestinationsHelper destinationsHelper;
  private Hashtable<String,String> props = new  Hashtable<String,String>();
  public static final String INTIALIZATION_TIME = "init_time";
  public static final String SCHEMA_COMPILE_TIME = "schema_compile";
  public static final String SCHEMA_LOAD_TIME = "schema_load";
  public static final String METADATE_LOAD_TIME = "metadata_load";  
  public static final String OTHER_TIME = "other_time";
  
  private static final Location LOCATION = Location.getLocation(DGenericServiceImpl.class);
  
  private boolean isSDOSerialization(ServiceFactoryConfig serviceFactoryCfg) {
    return (serviceFactoryCfg.get(ServiceFactoryConfig.HELPER_CONTEXT) != null);
  }

  public DGenericServiceImpl(String wsdlURL, ServiceFactoryConfig serviceFactoryConfig, DestinationsHelper destinationsHelper) throws Exception {
  	this.destinationsHelper = destinationsHelper;
    ProxyGeneratorConfigNew proxyGeneratorConfig = loadProxy(wsdlURL, serviceFactoryConfig);    
    TypeMappingRegistryImpl typeRegistry = null;
    if (!isSDOSerialization(serviceFactoryConfig)) {
      typeRegistry = initTypeMappingRegistry(proxyGeneratorConfig);    
      MetadataLoader.loadMetaData(typeRegistry, proxyGeneratorConfig.getSchemaConfig().getSchema());
    }
    MappingRules mappingRules = proxyGeneratorConfig.getMappingRules();
    QName serviceName = mappingRules.getService()[0].getServiceName();
    initServiceContext(serviceName, typeRegistry, mappingRules, proxyGeneratorConfig.getProxyConfig(), this.getClass().getClassLoader(), serviceFactoryConfig);
    initLPortTypeNamesToMetadataMapping(proxyGeneratorConfig, destinationsHelper);
    initDocumentationElement(proxyGeneratorConfig.getWsdl().getService(serviceName));
  } 
  
  /**
   * Loads the type mapping framework.
   * @param wsdlURL
   * @param tempDir
   * @throws Exception
   */
  private TypeMappingRegistryImpl initTypeMappingRegistry(ProxyGeneratorConfigNew proxyGeneratorConfig) throws Exception {
    SchemaToJavaConfig schemaToJavaConfig = proxyGeneratorConfig.getSchemaConfig();
    TypeMappingRegistryImpl typeMappingRegistry = new TypeMappingRegistryImpl(schemaToJavaConfig.getTypeSet(), this.getClass().getClassLoader());
    return typeMappingRegistry;
  }  
  
  
  /**
   * Returns client service context.
   * @return
   */
  public ClientServiceContext getServiceContext() {
    return serviceContext;
  }
  
  public DestinationsHelper getDestinationsHelper() {
		return destinationsHelper;
	}
  
  public String getPropery(String name) {
    return props.get(name);
  }
  
  public ExtendedTypeMapping getTypeMetadata() { 
    if (serviceContext.getTypeMappingRegistry() != null) {
      return((ExtendedTypeMapping) serviceContext.getTypeMappingRegistry().getDefaultTypeMapping());
    } else {
      return null;
    }
  }

  /**
   * Returns interface names provided by the dynamic service.
   * @return
   */
  public QName[] getInterfaces() {
    QName[] interfaceNames = new QName[lPortTypeNamesToMetadataMapping.size()];
    Enumeration keys = lPortTypeNamesToMetadataMapping.keys();
    for(int i = 0 ; i < interfaceNames.length; i++) {
      interfaceNames[i] = (QName)(keys.nextElement());
    }
    return(interfaceNames);
  }

  public DInterface getInterfaceMetadata(QName interfaceName) {
  	DInterface result = (DInterface)(lPortTypeNamesToMetadataMapping.get(interfaceName));
  	if (result == null) {
  		interfaceName = GenericServiceFactory.convertName(interfaceName);
  		if (interfaceName != null) {
  			result = (DInterface)(lPortTypeNamesToMetadataMapping.get(interfaceName));
  		}
  	}
    // If the interface is not selected then select it
    if (serviceContext.getProperty(ClientServiceContextImpl.IF_NAME) == null) {
      serviceContext.setProperty(ClientServiceContextImpl.IF_NAME,interfaceName.toString());
    }
  	return result;	    
  }
  
  public DInterface getInterfaceMetadata() {
    if (this.destinationsHelper == null) {
      throw new BaseRuntimeException(LOCATION, WSResourceAccessor.getResourceAccessor(), GenericServiceFactory.METHOD_NOT_AVAILABLE, new Object[]{"getInterfaceMetadata()"});
    }
    
    QName interfaceName = destinationsHelper.getInterfaceName();    
    DInterface result = (DInterface)(lPortTypeNamesToMetadataMapping.get(interfaceName));
    
    if (result == null) {
        interfaceName = GenericServiceFactory.convertName(interfaceName);
        if (interfaceName != null) {
            result = (DInterface)(lPortTypeNamesToMetadataMapping.get(interfaceName));
        }
    }

    return result;      
  }
  
  public String toString() {
    StringBuffer toStringBuffer = new StringBuffer();
    initToStringBuffer(toStringBuffer);
    return(toStringBuffer.toString());
  }
  
  private final void initToStringBuffer(StringBuffer toStringBuffer) {
    toStringBuffer.append("DGenericService");
    //Util.initToStringBuffer_ObjectValue(toStringBuffer, Util.TO_STRING_OFFSET, "work dir : ", serializationFRMDirPath);
    initToStringBuffer_Documentation(toStringBuffer, Util.TO_STRING_OFFSET);
    initToStringBuffer_TypeMappingRegistry(toStringBuffer, Util.TO_STRING_OFFSET);
    initToStringBuffer_LPortTypeNamesToMetadataMapping(toStringBuffer, Util.TO_STRING_OFFSET);
  }
  
  private final void initToStringBuffer_TypeMappingRegistry(StringBuffer toStringBuffer, String offset) {
    Util.initToStringBuffer_AppendCReturnOffsetAndId(toStringBuffer, offset, "type mapping");
    ExtendedTypeMapping extendedTypeMapping = this.getTypeMetadata();
    Enumeration schemaTypesEnum = extendedTypeMapping.getRegisteredSchemaTypes();
    while(schemaTypesEnum.hasMoreElements()) {
      QName schemaTypeName = (QName)(schemaTypesEnum.nextElement());
      DBaseTypeImpl dBaseType = (DBaseTypeImpl)(extendedTypeMapping.getTypeMetadata(schemaTypeName));
      if(dBaseType != null) {
        toStringBuffer.append("\n");
        dBaseType.initToStringBuffer(toStringBuffer, offset + Util.TO_STRING_OFFSET);
      }
    }
  }
  
  private final void initToStringBuffer_LPortTypeNamesToMetadataMapping(StringBuffer toStringBuffer, String offset) {
    Util.initToStringBuffer_AppendCReturnOffsetAndId(toStringBuffer, offset, "interfaces");
    Enumeration dIntefacesEnum = lPortTypeNamesToMetadataMapping.elements();
    while(dIntefacesEnum.hasMoreElements()) {
      DInterfaceImpl dInterface = (DInterfaceImpl)(dIntefacesEnum.nextElement());
      toStringBuffer.append("\n");
      dInterface.initToStringBuffer(toStringBuffer, offset + Util.TO_STRING_OFFSET);
    }
  }
  
  /**
   * Call Proxy Generator New to intialize consumer.
   * @param wsdlURL
   * @param serviceFactoryConfig
   * @return
   * @throws ProxyGeneratorException
   */
  private final ProxyGeneratorConfigNew loadProxy(String wsdlURL, ServiceFactoryConfig serviceFactoryConfig) throws ProxyGeneratorException {
    ProxyGeneratorConfigNew proxyGeneratorConfig = new ProxyGeneratorConfigNew();
    proxyGeneratorConfig.setGenerationMode(ProxyGeneratorConfigNew.GENERIC_MODE);
    if (isSDOSerialization(serviceFactoryConfig)) {
      proxyGeneratorConfig.setGenerationMode(ProxyGeneratorConfigNew.GENERIC_MODE_SDO);
    }
    proxyGeneratorConfig.setOutputPackage("");
    // Sets WSDL source
    if (serviceFactoryConfig.get(ServiceFactoryConfig.WSDL_DEFINITIONS) != null) {
      proxyGeneratorConfig.setWsdl((Definitions) serviceFactoryConfig.get(ServiceFactoryConfig.WSDL_DEFINITIONS));
    } else {
      proxyGeneratorConfig.setWsdlPath(wsdlURL);
    }    
    ConfigurationMarshallerFactory cfgFactory = (ConfigurationMarshallerFactory) serviceFactoryConfig.get(serviceFactoryConfig.CONFIGURATION_FACTORY);
    if (cfgFactory != null) {
      proxyGeneratorConfig.setConfigMarshaller(cfgFactory);
    }
    proxyGeneratorConfig.setUnwrapDocumentStyle(false);
    proxyGeneratorConfig.setResolver(determineEntityResolver(serviceFactoryConfig));
    proxyGeneratorConfig.setAppendDefaultBindings(serviceFactoryConfig.getAppendDefaultBindings());
    ProxyGeneratorNew proxyGenerator = new ProxyGeneratorNew(); 
    proxyGenerator.generateAll(proxyGeneratorConfig);
    return(proxyGeneratorConfig);
  }
  
  private final EntityResolver determineEntityResolver(ServiceFactoryConfig serviceFactoryConfig) {
    EntityResolver entityResolver = serviceFactoryConfig.getEntityResolver(); 
    if (entityResolver == null) {
      entityResolver = createWSDLDownloadResolver(serviceFactoryConfig);
    }
    return(entityResolver);
  }
  
  private final EntityResolver createWSDLDownloadResolver(ServiceFactoryConfig serviceFactoryConfig) {
    WSDLDownloadResolver downloadResolver = new WSDLDownloadResolver();
    // Set HTTP Proxy host
    String proxyHost = serviceFactoryConfig.getProxyHost();
    if(proxyHost != null) {
      downloadResolver.setProxyHost(proxyHost);
      downloadResolver.setProxyPort(determineProxyPort(serviceFactoryConfig));
      if (serviceFactoryConfig.get(ServiceFactoryConfig.INET_PROXY_BYPASS) != null) {
        downloadResolver.setProxyExcludeList((String) serviceFactoryConfig.get(ServiceFactoryConfig.INET_PROXY_BYPASS));
      }
      String proxyUser = serviceFactoryConfig.getProxyUser();
      if(proxyUser != null) {
        downloadResolver.setProxyUser(proxyUser);
      }
      String proxyPass = serviceFactoryConfig.getProxyPassword();
      if(proxyPass != null) {
        downloadResolver.setProxyPass(proxyPass);
      }
    }
    String user = serviceFactoryConfig.getUser();
    if(user != null) {
      downloadResolver.setUsername(user);
    }
    String pass = serviceFactoryConfig.getPassword();
    if(pass != null) {
      downloadResolver.setPassword(pass);
    }
    return(downloadResolver);
  }
  
  private final int determineProxyPort(ServiceFactoryConfig serviceFactoryConfig) {
    String proxyPort = serviceFactoryConfig.getProxyPort();
    try {
      return(Integer.parseInt(proxyPort));
    } catch(Exception exc) {
      //$JL-EXC$
    }
    return(80);
  }
    
  private final void initLPortTypeNamesToMetadataMapping(ProxyGeneratorConfigNew proxyGeneratorConfig, DestinationsHelper destinationsHelper) throws Exception {
    lPortTypeNamesToMetadataMapping = new Hashtable();
    MappingRules mappingRules = proxyGeneratorConfig.getMappingRules();
    InterfaceMapping[] interfaceMappings = mappingRules.getInterface();    
    for(int i = 0; i < interfaceMappings.length; i++) {
      InterfaceMapping interfaceMapping = interfaceMappings[i];
      QName interfaceName = interfaceMapping.getPortType();
      Interface portType = proxyGeneratorConfig.getWsdl().getInterface(interfaceName);
      lPortTypeNamesToMetadataMapping.put(interfaceName, new DInterfaceImpl(portType, interfaceMapping, serviceContext, destinationsHelper));
    }
  }
  
  private final void initServiceContext(QName serviceName, TypeMappingRegistry registry, MappingRules mappings, ConfigurationRoot configuration, ClassLoader applicationLoader, ServiceFactoryConfig serviceFactoryCfg) {    
    this.serviceContext = new ClientServiceContextImpl();
    this.serviceContext.setClientType(ClientServiceContext.DYNAMIC);
    if (registry != null) {
      this.serviceContext.setTypeMappingRegistry(registry);
    }
    this.serviceContext.setMappingRules(mappings);
    this.serviceContext.setCompleteConfiguration(configuration);
    this.serviceContext.setServiceName(serviceName);
    this.serviceContext.setServiceData(DynamicServiceImpl._getServiceData(configuration,serviceName));
    this.serviceContext.setApplicationClassLoader(applicationLoader);
    this.serviceContext.setApplicationName("[N/A] Generic Web Services Client");
    if(serviceFactoryCfg.get(ServiceFactoryConfig.HELPER_CONTEXT) != null) {
      this.serviceContext.setProperty(ClientServiceContextImpl.HELPER_CONTEXT, serviceFactoryCfg.get(ServiceFactoryConfig.HELPER_CONTEXT));
    }    
  }
  
  
}
