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
package com.sap.engine.services.webservices.espbase.client;

import java.net.URL;
import java.util.Hashtable;

import org.xml.sax.EntityResolver;

import com.sap.engine.services.webservices.espbase.client.migration.PropertyListConverter;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationMarshallerFactory;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaToJavaConfig;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaToJavaConfig.ImplementationVersion;
import com.sap.engine.services.webservices.jaxrpc.util.FileBufferSet;
import com.sap.engine.services.webservices.jaxrpc.util.PackageBuilder;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPorts;

/**
 * Proxy Generator Configuration
 * @version 2.0
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public class ProxyGeneratorConfigNew {
  
  public static final int LOAD_MODE = 1; // Mode for loading of proxy model without generation of anything
  public static final int INTERFACE_MODE = 2; // Mode for generation of interfaces for deployable proxies
  public static final int IMPLEMENTATION_MODE = 3; // Mode for generation of implmentation classes for deployable classes
  public static final int STANDALONE_MODE = 4; // Mode for generation of stantalone web service proxies
  public static final int GENERIC_MODE = 5; // Mode for generation of generic client 
  public static final int GENERIC_MODE_SDO = 11; // Mode for generation of generic client  
  public static final int MIGRATION_MODE = 6; // Mode for generation of 6.40 compatible deployable client implementation
  public static final int ARTEFACT_MODE = 7;  // Mode for generation of the proxy artefacts (Exceptions and Holders)
  public static final int JAXWS_MODE = 8;  // JAX-WS 2.0 mode, uses JAXB!
  public static final int JAXWS_MODE_LOAD = 9;  // JAX-WS 2.0 mode, uses JAXB!
  public static final int JAXWS_MODE_SERVER = 10;  // JAX-WS 2.0 mode, uses JAXB!
  
  private int generationMode;
  
  private String wsdlPath;
  private String outputPackage;
  private SchemaToJavaConfig schemaConfig;
  private MappingRules mappingRules;
  private String outputPath;
  private Definitions wsdl;
  private FileBufferSet outputFiles;
  private boolean generateToFileBuffer = false;
  private ConfigurationRoot proxyConfig = null;
  private boolean unwrapDocumentOperations = false;
  private boolean generateSerializable = true;
  private String additionalClassPath = null;
  private String proxyHost;
  private String proxyPort;
  private String[] externalBindingFiles = null;
  private LogicalPorts logicalPorts = null;
  private Hashtable locationMap = null;
  private EntityResolver resolver = null;
  private ConfigurationMarshallerFactory configMarshaller = null;
  private JAXWSFileParser jaxWSFiles = null;
  private String jaxWSWSDLLocation = null;      
  private boolean appendDefaultBindings;
  private boolean isProvider = false;
  private boolean shortInnerClassNames = false;
  private boolean mapToTopLevel = false;
  private boolean illegalWSDLLocation = false;
  private boolean jaxwsDocUnwrapDefaultOverride = true;
  private PropertyListConverter propertyConvertor = null;
  private boolean useCTSPackageMapping = true;  
  
  public static final int JAXB_20 = 20;
  public static final int JAXB_21 = 21;
  
  private int jaxbVersion = JAXB_20;
  
  public void setJAXBVersion(int version) {
    this.jaxbVersion = version;   
  }
  
  public int getJAXBVersion() {
    return this.jaxbVersion;
  }
  
  public boolean isJaxWSDocUnwrap() {
    return this.jaxwsDocUnwrapDefaultOverride;
  }
  
  public void setJaxWSDocUnwrapDefault(boolean flag) {
    this.jaxwsDocUnwrapDefaultOverride = flag;
  }
  
  public boolean isIllegalWSDLLocation() {
    return illegalWSDLLocation;
  }

  public void setIllegalWSDLLocation(boolean illegalWSDLLocation) {
    this.illegalWSDLLocation = illegalWSDLLocation;
  }

  public void setMapToTopLevel(boolean flag) {
    this.mapToTopLevel = flag;
  }
  
  public boolean isMapToTopLevel() {
    return this.mapToTopLevel;
  }
  
  /**
   * In JAX-WS mode runs algorithm that shortens the inner class names in JAXB code.
   * This is not standard compatible. Do not use by default.
   * @param flag
   */
  public void setShortInnerClassNames(boolean flag) {
    this.shortInnerClassNames = flag;
  }
  
  public boolean isShortInnerClassNames() {
    return this.shortInnerClassNames;
  }

  public boolean isProvider() {
    return isProvider;
  }

  public void setProvider(boolean isProvider) {
    this.isProvider = isProvider;
  }

  /**
   * @return Returns the jaxWSWSDLLocation.
   */
  public String getJaxWSWSDLLocation() {
    return jaxWSWSDLLocation;
  }

  /**
   * @param jaxWSWSDLLocation The jaxWSWSDLLocation to set.
   */
  public void setJaxWSWSDLLocation(String jaxWSWSDLLocation) {
    this.jaxWSWSDLLocation = jaxWSWSDLLocation;
  }

  /**
   * @return Returns the jaxWSFiles.
   */
  public JAXWSFileParser getJaxWSFiles() {
    return jaxWSFiles;
  }

  /**
   * @param jaxWSFiles The jaxWSFiles to set.
   */
  public void setJaxWSFiles(JAXWSFileParser jaxWSFiles) {
    this.jaxWSFiles = jaxWSFiles;
  }

  /**
   * Returns the configuration marshaller for the WSDL parsing. This object parses the WS-Policies to the SAP Configuration format.
   * @return Returns the configMarshaller.
   */
  public ConfigurationMarshallerFactory getConfigMarshaller() {
    return configMarshaller;
  }

  /**
   * Sets the configuration marshaller for the WSDL parsing. This object parses the WS-Policies to the SAP Configuration format.
   * @param configMarshaller The configMarshaller to set.
   */
  public void setConfigMarshaller(ConfigurationMarshallerFactory configMarshaller) {
    this.configMarshaller = configMarshaller;
  }

  /**
   * Sets entity resolvor for WSDL download.
   * @param resolver
   */
  public void setResolver(EntityResolver resolver) {
    this.resolver = resolver;
  }
  
  /**
   * Returns the entity resolver used for WSDL download.
   * @return
   */
  public EntityResolver getResolver() {
    return this.resolver;
  }
 
  /**
   * Remote WSDL location mapping - used in 6.30 proxy archives.
   * @param locationMap
   */
  public void setLocationMap(Hashtable  locationMap) {
    this.locationMap = locationMap;
  }
  
  /**
   * Returns the remote location mapping for the wsdl location. Used only for 6.30 WSDLs.
   * @return
   */
  public Hashtable getLocationMap() {
    return this.locationMap;
  }
  
  /**
   * This field is used only for 6.30 client migration.
   * @param logicalPorts
   */
  public void setLogicalPorts(LogicalPorts logicalPorts) {
    this.logicalPorts = logicalPorts;
  }
  
  /**
   * Returns logical port variable. This parameter is used only for 6.30 client migration.
   * @return
   */
  public LogicalPorts getLogicalPorts() {
    return this.logicalPorts;
  }

  public void setAdditionalClassPath(String path) {
    this.additionalClassPath = path;
  }
  
  public String getAdditionalClassPath() {
    return this.additionalClassPath;
  }

  /**
   * Returns true if the generator should generate Serializable service implementations.
   * @return Returns the generateSerializable.
   */
  public boolean isGenerateSerializable() {
    return generateSerializable;
  }
  /**
   * Set this flag if generation of serializable Service implementations is required.
   * @param generateSerializable The generateSerializable to set.
   */
  public void setGenerateSerializable(boolean generateSerializable) {
    this.generateSerializable = generateSerializable;
  }
  /**
   * Returns setting for unwrapping document style operations.
   * @return
   */
  public boolean isUnwrapDocumentStyle() {
    return this.unwrapDocumentOperations;
  }
  
  /**
   * Sets setting for unwrapping document style operations. 
   * @param flag
   */
  public void setUnwrapDocumentStyle(boolean flag) {
    this.unwrapDocumentOperations = flag;
  }
  
  /**    
  
  /**
   * Returns the loaded p=roxy configuration.
   * @return
   */
  public ConfigurationRoot getProxyConfig() {
    return this.proxyConfig;
  }
  
  /**
   * Sets proxy configuration.
   * @param config
   */
  public void setProxyConfig(ConfigurationRoot config) {
    this.proxyConfig = config;
  }
  
  /**
   * Returns true if the generation output is to the buffer set.
   * @return
   */
  public boolean isFileBufferOutput() {
    return this.generateToFileBuffer;
  }
  
  /**
   * Sets the buffered output flag.
   */
  public void setBufferedOutput(boolean flag) {
    this.generateToFileBuffer = flag;
  }
  
  /**
   * Sets output file bufferset.
   * @param fileset
   */  
  public void setOutputFiles(FileBufferSet fileset) {
    this.outputFiles = fileset;
  }
  
  /**
   * Returns output file buffer set.
   * @return
   */
  public FileBufferSet getOutputFiles() {
    return this.outputFiles;
  }
  
  /**
   * Default constructor.
   */
  public ProxyGeneratorConfigNew() {
    //schemaConfig = new SchemaToJavaConfig();
    generationMode = LOAD_MODE;
  }  
  

  /**
   * @return
   */
  public int getGenerationMode() {
    return generationMode;    
  }

  /**
   * @return
   */
  public MappingRules getMappingRules() {
    return mappingRules;
  }

  /**
   * @return
   */
  public String getOutputPackage() {
    return outputPackage;
  }

  /**
   * @return
   */
  public String getOutputPath() {
    return outputPath;
  }

  /**
   * @return
   */
  public String getWsdlPath() {
    return wsdlPath;
  }

  /**
   * @param i
   */
  public void setGenerationMode(int i) {
    generationMode = i;
  }

  /**
   * @param rules
   */
  public void setMappingRules(MappingRules rules) {
    mappingRules = rules;
  }

  /**
   * @param string
   */
  public void setOutputPackage(String string) {
    outputPackage = string;
  }

  /**
   * @param string
   */
  public void setOutputPath(String string) {
    outputPath = string;
  }

  /**
   * @param string
   */
  public void setWsdlPath(String string) {
    wsdlPath = string;
  }

  /**
   * @return
   */
  public Definitions getWsdl() {
    return wsdl;
  }

  /**
   * @param definitions
   */
  public void setWsdl(Definitions definitions) {
    wsdl = definitions;
  }

  /**
   * @return
   */
  public SchemaToJavaConfig getSchemaConfig() {
    if(schemaConfig == null){
      if(getGenerationMode() == JAXWS_MODE || getGenerationMode() == JAXWS_MODE_LOAD || getGenerationMode() == JAXWS_MODE_SERVER){
        schemaConfig = new SchemaToJavaConfig(ImplementationVersion.JAX_WS_2);
      } else if (getGenerationMode() == GENERIC_MODE_SDO) {
        schemaConfig = new SchemaToJavaConfig(ImplementationVersion.EMPTY);
      } else {
        schemaConfig = new SchemaToJavaConfig(ImplementationVersion.JAX_RPC);
      }
    }
    schemaConfig.packageBuilder = new PackageBuilder();
    return schemaConfig;
  }

  /**
   * @param config
   */
  public void setSchemaConfig(SchemaToJavaConfig config) {
    schemaConfig = config;
  }

  public void setHTTPProxy(String proxyHost, String proxyPort) {
    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
  }
  
  public String getHTTPProxyHost() {
    return this.proxyHost;
  }

  public String getHTTPProxyPort() {
    return this.proxyPort;
  }
  
	public void setExternalBindings(String[] externalBindingFiles) {
		this.externalBindingFiles = externalBindingFiles;		
	}

	public String[] getExternalBindings(){
		return externalBindingFiles;
	}

  public boolean getAppendDefaultBindings() {
    return(appendDefaultBindings);
  }

  public void setAppendDefaultBindings(boolean appendDefaultBindings) {
    this.appendDefaultBindings = appendDefaultBindings;
  }

  public PropertyListConverter getPropertyConvertor() {
    return propertyConvertor;
  }

  public void setPropertyConvertor(PropertyListConverter propertyConvertor) {
    this.propertyConvertor = propertyConvertor;
  }
	
  public boolean isUseCTSPackageMapping() {
    return useCTSPackageMapping;
  }

  public void setUseCTSPackageMapping(boolean useCTSPackageMapping) {
    this.useCTSPackageMapping = useCTSPackageMapping;
  }
	
}
