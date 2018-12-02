/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.jaxrpc.wsdl2java;

import com.sap.engine.services.webservices.wsdl.WSDLDefinitions;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPorts;

import java.util.*;
import java.io.File;

import org.xml.sax.EntityResolver;

import javax.xml.namespace.QName;

/**
 * Proxy generator configuration class. This class is placeholder for proxy generation configuration.
 * Pass filled intstance of this class to generateProxy method.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class ProxyGeneratorConfig {

  private String wsdlLocation; // WSDL Source location
  private String targetDir; // Generator output target dir
  private String outputPackage; // Generator output package
  private boolean compile = false; // Compile option
  private String jarName; // Output jar name
  private boolean interfacesOnly = false;   // --> DEPLOYABLE_MODE Forces Generator to generate interfaces only
  private boolean additionalMethods = false; // Forces Generator to generate rpc methods for document style wsdl
  private boolean jaxRpcMethods = false; // Forces Generator to create getXXX methods for ports
  private boolean useProxy = false; // Flag for using proxy to resolve wsdl location
  private String proxyHost;
  private String proxyPort;
  private Hashtable locationMap = null;
  private String additionalClassPath = null;
  private String[] jarExtensions = null; // File Extensions to be included in output jar
  private String logicalPortName;   // Gives the default lp name (optional)
  private String logicalPortPath;   // Gives the default lp path (optional)
  private File[] generatedSEIFiles; // --> FileBufferSet
  private File[] generatedServices; // --> FileBufferSet
  private File[] allGeneratedFiles; // --> FileBufferSet
  private Properties uriToPackageMapping = null; // --> SchemaToJavaConfig
  private WSDLDefinitions definitions = null;
  private boolean lponly = false;  // - LOAD_MODE
  private boolean stubsOnly = false; // - IMPLEMENTATION_MODE
  private ClientTransportBinding[] bindings = null;
  private EntityResolver resolver;
  private LogicalPorts logicalPorts;
  private boolean serverHosted = true; // - ARE STUBS hosted standalone or on engine (internal) == STANDALONE
  private String customServiceName = null;
  private QName isolatePort = null;
  private boolean containerMode = false;
  private String schemaFrameworkPath = null;
  private boolean genericMode = false;
  private HashMap applicationSchemaToJavaMapping;
  
  public void setApplicationSchemaToJavaMapping(HashMap schema2java) {
    this.applicationSchemaToJavaMapping = schema2java;    
  }
  
  public HashMap getApplicationSchemaToJavaMapping() {
    return this.applicationSchemaToJavaMapping;
  }

  public void setGenericMode(boolean flag) {
    this.genericMode = flag;
  }

  public boolean getGenericMode() {
    return this.genericMode;
  }

  public String getSchemaFrameworkPath() {
    return schemaFrameworkPath;
  }

  public void setSchemaFrameworkPath(String schemaFrameworkPath) {
    this.schemaFrameworkPath = schemaFrameworkPath;
  }

  public boolean isContainerMode() {
    return containerMode;
  }

  public void setContainerMode(boolean containerMode) {
    this.containerMode = containerMode;
  }

  /**
   * Gets custom service name.
   * @return
   */
  public String getCustomServiceName() {
    return customServiceName;
  }

  /**
   * Sets custom service name.
   * @param customServiceName
   */
  public void setCustomServiceName(String customServiceName) {
    this.customServiceName = customServiceName;
  }

  /**
   * Returns this property value.
   * @return
   */
  public QName getIsolatePortType() {
    return isolatePort;
  }

  /**
   * Tells the generator to use only this portType.
   * @param isolatePort
   */
  public void setIsolatePortType(QName isolatePort) {
    this.isolatePort = isolatePort;
  }




  /**
   * Gets parsed wsdl definisions in memory representation of wsdl file.
   * @return
   */
  public WSDLDefinitions getDefinitions() {
    return definitions;
  }

  /**
   * Sets parsed wsdl definitions to be used.
   * @param definitions
   */
  public void setDefinitions(WSDLDefinitions definitions) {
    this.definitions = definitions;
  }

  public boolean isLPonly() {
    return lponly;
  }

  public void setLPonly(boolean lponly) {
    this.lponly = lponly;
  }

  /**
   * Returns mapping from schema uri to java package.
   * @return
   */
  public Properties getUriToPackageMapping() {
    return uriToPackageMapping;
  }


  /**
   * Schema to java generator uses this mapping to generate packages for schema derived files.
   * @param uriToPackageMapping
   */
  public void setUriToPackageMapping(Properties uriToPackageMapping) {
    this.uriToPackageMapping = uriToPackageMapping;
  }

  /**
   * Constructor with minimal set of params.
   * @param wsdlLocation
   * @param targetDir
   * @param outputPackage
   */
  public ProxyGeneratorConfig(String wsdlLocation, String targetDir, String outputPackage) {
    this.wsdlLocation = wsdlLocation;
    this.targetDir = targetDir;
    this.outputPackage = outputPackage;
  }

  /**
   * Returns wsdl location that is used.
   * @return
   */
  public String getWsdlLocation() {
    return wsdlLocation;
  }

  /**
   * Sets wsdl location.
   * @param wsdlLocation
   */
  public void setWsdlLocation(String wsdlLocation) {
    this.wsdlLocation = wsdlLocation;
  }

  /**
   * Returns output target dir.
   * @return
   */
  public String getTargetDir() {
    return targetDir;
  }

  /**
   * Set generator output target dir.
   * @param targetDir
   */
  public void setTargetDir(String targetDir) {
    this.targetDir = targetDir;
  }

  /**
   * Returns output package for generated client.
   * @return
   */
  public String getOutputPackage() {
    return outputPackage;
  }

  /**
   * Set's output package for the generator.
   * @param outputPackage
   */
  public void setOutputPackage(String outputPackage) {
    this.outputPackage = outputPackage;
  }

  /**
   * Is complile option turned on.
   * @return
   */
  public boolean isCompile() {
    return compile;
  }

  /**
   * Set this to turn on the compilation.
   * @param compile
   */
  public void setCompile(boolean compile) {
    this.compile = compile;
  }

  /**
   * Returns output jar name.
   * @return
   */
  public String getJarName() {
    return jarName;
  }

  /**
   * Set this if you want output to be packaged in jar file. The compile option is automatically turned on.
   * @param jarName
   */
  public void setJarName(String jarName) {
    this.jarName = jarName;
  }

  /**
   * Returns true if generator generates only interfaces.
   * @return
   */
  public boolean isInterfacesOnly() {
    return interfacesOnly;
  }

  /**
   * Set this to true when you want only interfaces to be generated.
   * @param interfacesOnly
   */
  public void setInterfacesOnly(boolean interfacesOnly) {
    this.interfacesOnly = interfacesOnly;
  }

  /**
   * Returns is rpc style methods for document style is turned on.
   * @return
   */
  public boolean isAdditionalMethods() {
    return additionalMethods;
  }

  /**
   * Set this to force the proxy generator to generate rpc style methods for document style wsdl.
   * @param additionalMethods
   */
  public void setAdditionalMethods(boolean additionalMethods) {
    this.additionalMethods = additionalMethods;
  }

  /**
   * Is ProxyGenerator generating JAX-RPC style methods for getting proxy ports.
   * @return
   */
  public boolean isJaxRpcMethods() {
    return jaxRpcMethods;
  }

  /**
   * Set this to true if you want to have direct methods for getting proxy ports.
   * @param jaxRpcMethods
   */
  public void setJaxRpcMethods(boolean jaxRpcMethods) {
    this.jaxRpcMethods = jaxRpcMethods;
  }

  /**
   * Returns true if proxy generator uses proxy to resolve urls.
   * @return
   */
  public boolean isUseProxy() {
    return useProxy;
  }

  /**
   * Set this to true to tell the proxy generator to use proxy to resolve urls.
   * @param useProxy
   */
  public void setUsePoxy(boolean useProxy) {
    this.useProxy = useProxy;
  }

  /**
   * Returns used proxy host.
   * @return
   */
  public String getProxyHost() {
    return proxyHost;
  }

  /**
   * Use this method to set proxy for resolving urls.
   * @param proxyHost
   * @param proxyPort
   */
  public void setProxy(String proxyHost, String proxyPort) {
    this.useProxy = true;
    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
  }

  /**
   * Retuns proxy port used.
   * @return
   */
  public String getProxyPort() {
    return proxyPort;
  }

  /**
   * Set mapping between remote absolute locations to local relative to support parsing of downloadedd wslds.
   * @param locationMap
   */
  public void setLocationMap(Hashtable locationMap) {
    this.locationMap = locationMap;
  }

  /**
   * Returns the location map that is used.
   * @return
   */
  public Hashtable getLocationMap() {
    return this.locationMap;
  }

  /**
   * Gets additional class path for compiling.
   * @return
   */
  public String getAdditionalClassPath() {
    return additionalClassPath;
  }

  /**
   * Use this method to add some additional classpath to the proxy generator.
   * @param additionalClassPath
   */
  public void setAdditionalClassPath(String additionalClassPath) {
    this.additionalClassPath = additionalClassPath;
  }

  /**
   * Returns list of extensions that will be set in output jar.
   * @return
   */
  public String[] getJarExtensions() {
    return jarExtensions;
  }

  /**
   * Use this method to tell what extension will be added to output jar.
   * @param jarExtensions
   */
  public void setJarExtensions(String[] jarExtensions) {
    this.jarExtensions = jarExtensions;
  }

  /**
   * Returns logicalPort name.
   * @return
   */
  public String getLogicalPortName() {
    return logicalPortName;
  }

  /**
   *
   * @param logicalPortName
   */
  public void setLogicalPortName(String logicalPortName) {
    this.logicalPortName = logicalPortName;
  }

  public String getLogicalPortPath() {
    return logicalPortPath;
  }

  public void setLogicalPortPath(String logicalPortPath) {
    this.logicalPortPath = logicalPortPath;
  }

  public File[] getGeneratedSEIFiles() {
    return generatedSEIFiles;
  }

  public void setGeneratedSEIFiles(File[] generatedSEIFiles) {
    this.generatedSEIFiles = generatedSEIFiles;
  }

  public File[] getGeneratedServices() {
    return generatedServices;
  }

  public void setGeneratedServices(File[] generatedServices) {
    this.generatedServices = generatedServices;
  }

  public File[] getAllGeneratedFiles() {
    return allGeneratedFiles;
  }

  public void setAllGeneratedFiles(File[] allGeneratedFiles) {
    this.allGeneratedFiles = allGeneratedFiles;
  }

  /**
   * Returns true if generator generates only webservice implementation.
   * @return
   */
  public boolean isStubsOnly() {
    return stubsOnly;
  }

  /**
   * Tells the generator to generate only webservice implementations. Logical port path and name must not be null.
   * @param stubsOnly
   */
  public void setStubsOnly(boolean stubsOnly) {
    this.stubsOnly = stubsOnly;
  }

  /**
   * Returns extenrnal binding implementations.
   * @return
   */
  public ClientTransportBinding[] getBindings() {
    return bindings;
  }

  /**
   * Sets external binding implementation.
   * @param bindings
   */
  public void setBindings(ClientTransportBinding[] bindings) {
    this.bindings = bindings;
  }

  /**
   * Returns extenrnal entity resolver.
   * @return
   */
  public EntityResolver getResolver() {
    return resolver;
  }

  /**
   * Sets external entity resolver.
   * @param resolver
   */
  public void setResolver(EntityResolver resolver) {
    this.resolver = resolver;
  }

  public LogicalPorts getLogicalPorts() {
    return logicalPorts;
  }

  public void setLogicalPorts(LogicalPorts logicalPorts) {
    this.logicalPorts = logicalPorts;
  }

  public boolean isServerHosted() {
    return serverHosted;
  }

  public void setServerHosted(boolean serverHosted) {
    this.serverHosted = serverHosted;
  }
}
