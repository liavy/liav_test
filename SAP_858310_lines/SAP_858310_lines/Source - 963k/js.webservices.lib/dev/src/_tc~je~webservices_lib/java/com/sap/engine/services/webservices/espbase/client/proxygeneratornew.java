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

import java.awt.Image;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Remote;
import java.rmi.server.UID;
import java.util.*;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.encoding.TypeMappingRegistry;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import com.sap.engine.lib.schema.components.*;
import com.sap.engine.lib.schema.components.Base;
import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.lib.xml.parser.URLLoader;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.espbase.client.bindings.DynamicServiceImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.DynamicStubImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.ParameterObject;
import com.sap.engine.services.webservices.espbase.client.dynamic.DInterface;
import com.sap.engine.services.webservices.espbase.client.dynamic.WebServiceException;
import com.sap.engine.services.webservices.espbase.client.jaxws.bindext.*;
import com.sap.engine.services.webservices.espbase.client.migration.ConfigurationMigrationUtil;
import com.sap.engine.services.webservices.espbase.client.migration.TypeMappingMigrationUtil;
import com.sap.engine.services.webservices.espbase.configuration.*;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.mappings.*;
import com.sap.engine.services.webservices.espbase.wsdl.*;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;
import com.sap.engine.services.webservices.espbase.wsdl.misc.XIDefaultBindingAppender;
import com.sap.engine.services.webservices.espbase.wsdl.wsdl11.WSDL11Constants;
import com.sap.engine.services.webservices.jaxrpc.exceptions.ProxyGeneratorException;
import com.sap.engine.services.webservices.jaxrpc.exceptions.WebserviceClientException;
import com.sap.engine.services.webservices.jaxrpc.schema2java.*;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaToJavaConfig.ImplementationVersion;
import com.sap.engine.services.webservices.jaxrpc.util.CodeGenerator;
import com.sap.engine.services.webservices.jaxrpc.util.FileBuffer;
import com.sap.engine.services.webservices.jaxrpc.util.FileBufferSet;
import com.sap.engine.services.webservices.jaxrpc.util.NameConvertor;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPortType;
import com.sap.engine.services.webservices.jaxws.*;
import com.sap.engine.services.webservices.tools.ASKIIEncoderFilterWriter;
import com.sun.xml.bind.api.impl.NameConverter;
import com.sun.tools.xjc.model.Model;

/**
 * Proxy Generator (client/server) for JAX-RCP 1.1.
 * 
 * @version 2.0
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public class ProxyGeneratorNew {

  private SchemaToJavaGeneratorNew schemaGenerator;

  private ProxyGeneratorConfigNew config;

  private NameConvertor convertor;

  private CodeGenerator generator;

  // private ExtBindingCustomization extBindingCust = new
  // ExtBindingCustomization();
  private ExtendedDefinition extendedDefinitions;

  //private StandardDOMParser domparser = null;
  private DocumentBuilder dombuilder = null;
    
  private SchemaToJavaGeneratorNew getSchemaGenerator() {
    if (this.schemaGenerator == null) {
    schemaGenerator = new SchemaToJavaGeneratorNew(); 
    }
    return this.schemaGenerator;
  }
    
  public ProxyGeneratorNew() {     
    convertor = new NameConvertor(true);
    generator = new CodeGenerator();
  }

  private DocumentBuilder getDOMBuilder() {
    if (this.dombuilder != null)  {
      return this.dombuilder;
    }
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setExpandEntityReferences(true);
    dbf.setValidating(false);
    dbf.setNamespaceAware(true);
    try {
      dombuilder = dbf.newDocumentBuilder();
    } catch (ParserConfigurationException x) {
      x.printStackTrace();
    }
    dombuilder.setEntityResolver(new EntityResolver() {
      public InputSource resolveEntity(String a, String b) {
        return new InputSource(new StringReader(" "));
      }
    });
    return this.dombuilder;
  }
  
  /**
   * Compares two interface mappings. If the two interface mappings are
   * semantically equal the method returns true.
   * 
   * @param interface1
   * @param interface2
   * @return
   */
  private boolean compareInterfaceMappings(InterfaceMapping interface1, InterfaceMapping interface2) {
    if (!interface1.getPortType().equals(interface2.getPortType())) {
      return false;
    }
    if (!interface1.getSEIName().equals(interface2.getSEIName())) {
      return false;
    }
    if (!interface1.getBindingType().equals(interface2.getBindingType())) {
      return false;
    }
    if (interface1.getOperation().length != interface2.getOperation().length) {
      return true;
    }
    if (interface1.getProperty(InterfaceMapping.SOAP_VERSION) != null) {
      String soapVersion = interface1.getProperty(InterfaceMapping.SOAP_VERSION);
      if (!soapVersion.equals(interface2.getProperty(InterfaceMapping.SOAP_VERSION))) {
        return false;
      }
    } else {
      if (interface2.getProperty(InterfaceMapping.SOAP_VERSION) != null) {
        return false;
      }
    }
    OperationMapping[] op1 = interface1.getOperation();
    OperationMapping[] op2 = interface2.getOperation();
    for (int i = 0; i < op1.length; i++) {
      OperationMapping operation1 = op1[i];
      OperationMapping operation2 = op2[i];
      if (!operation1.equals(operation2)) {
        return false;
      }
      ParameterMapping[] params1 = operation1.getParameter();
      ParameterMapping[] params2 = operation2.getParameter();
      if (params1.length != params2.length) {
        return false;
      }
      for (int j = 0; j < params1.length; j++) {
        ParameterMapping param1 = params1[j];
        ParameterMapping param2 = params2[j];
        if (!param1.equals(param2)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Redirects all references to interface2 to interface1. This is done when
   * removing dublicate interfaces.
   * 
   * @param interface1
   * @param interface2
   */
  private void redirectMapping(InterfaceMapping interface1, InterfaceMapping interface2) {
    MappingRules mapping = config.getMappingRules();
    ConfigurationRoot configRoot = config.getProxyConfig();
    ServiceMapping[] services = mapping.getService();
    String interfaceNewMappingId = interface1.getInterfaceMappingID();
    QName bindingNewName = interface1.getBindingQName();
    QName portTypeNewName = interface1.getPortType();
    String interfaceOldMappingId = interface2.getInterfaceMappingID();
    QName bindingOldName = interface2.getBindingQName();
    QName portTypeOldName = interface2.getPortType();
    // Redirect Endpoint mappings
    if (services.length > 0) {
      ServiceMapping serviceMapping = services[0];
      EndpointMapping[] endpoints = serviceMapping.getEndpoint();
      for (int i = 0; i < endpoints.length; i++) {
        EndpointMapping endpoint = endpoints[i];
        if (endpoint.getPortPortType().equals(portTypeOldName) && endpoint.getPortBinding().equals(bindingOldName)) {
          // The endpoint points to interface mapping2, so redirect to interface
          // mapping1
          endpoint.setPortBinding(bindingNewName);
          endpoint.setPortPortType(portTypeNewName);
        }
      }
    }
    // Alter the configuration data
    InterfaceDefinition[] interfaces = configRoot.getDTConfig().getInterfaceDefinition();
    for (int i = 0; i < interfaces.length; i++) {
      if (interfaces[i].getInterfaceMappingId() == null) {
        // The portType has no binding - no interface mapping
        continue;
      }
      if (interfaces[i].getInterfaceMappingId().equals(interfaceOldMappingId)) {
        interfaces[i].setInterfaceMappingId(interfaceNewMappingId);
      }
    }
    com.sap.engine.services.webservices.espbase.configuration.Service[] serviceConfigs = configRoot.getRTConfig().getService();
    if (serviceConfigs.length > 0) {
      ServiceData serviceConfig = serviceConfigs[0].getServiceData();
      BindingData[] ports = serviceConfig.getBindingData();
      for (int i = 0; i < ports.length; i++) {
        BindingData logicalPort = ports[i];
        if (logicalPort.getInterfaceMappingId().equals(interfaceOldMappingId)) {
          logicalPort.setInterfaceMappingId(interfaceNewMappingId);
          logicalPort.setBindingName(bindingNewName.getLocalPart());
          logicalPort.setBindingNamespace(bindingNewName.getNamespaceURI());
        }
      }
    }
  }

  /**
   * Handles special case when we have single port type which is referenced by
   * two bindings. The problem is that the configuration model maps single
   * interface definition for this model. When the two interface mappings are
   * the same they are equivalent and the one can be deleted. All configuration
   * and mapping references to the unneeded mapping are deleted.
   */
  private void removeDublicatedInterfaceMappings() {
    MappingRules mapping = config.getMappingRules();
    InterfaceMapping[] interfaceMappings = mapping.getInterface();
    ServiceMapping[] services = mapping.getService();
    boolean[] bitMap = new boolean[interfaceMappings.length];
    int removed = 0;
    for (int i = 0; i < (interfaceMappings.length - 1); i++) {
      if (bitMap[i] == false) {
        InterfaceMapping interface1 = interfaceMappings[i];
        for (int j = i + 1; j < interfaceMappings.length; j++) {
          if (bitMap[j] == false) {
            InterfaceMapping interface2 = interfaceMappings[j];
            if (compareInterfaceMappings(interface1, interface2)) {
              bitMap[j] = true; // mark the interface for removal and redirect
              // all references to interface1
              redirectMapping(interface1, interface2);
              removed++;
            }
          }
        }
      }
    }
    if (removed > 0) {
      InterfaceMapping[] editedMappings = new InterfaceMapping[interfaceMappings.length - removed];
      int counter = 0;
      for (int i = 0; i < interfaceMappings.length; i++) {
        if (bitMap[i] == false) {
          editedMappings[counter] = interfaceMappings[i];
          counter++;
        }
      }
      mapping.setInterface(editedMappings);
    }
  }
  
  /**
   * Checks if the transport binding is supported by the jax-ws specification.
   *
   */
  private void checkTransportBinding() throws ProxyGeneratorException {
    if (isJaxWS()) {
      Definitions definitions = this.config.getWsdl();
      if (definitions == null) {
        return;
      }
      ObjectList bindings = definitions.getBindings();
      for (int i=0; i < bindings.getLength(); i++) {
        Binding binding = (Binding) bindings.item(i);
        if (binding instanceof SOAPBinding) {
          SOAPBinding soapBinding = (SOAPBinding) binding;
          ObjectList operations = soapBinding.getOperations();
          for (int j = 0; j < operations.getLength(); j++) {
            SOAPBindingOperation operation = (SOAPBindingOperation) operations.item(j);
            String use = operation.getProperty(SOAPBindingOperation.USE);
            if (SOAPBindingOperation.USE_ENCODED.equals(use)) {
              if (config.getGenerationMode() == ProxyGeneratorConfigNew.GENERIC_MODE) {
                throw new ProxyGeneratorException(ProxyGeneratorException.WSDL_IS_ENCODED_DYNAMIC);
              } else {
                throw new ProxyGeneratorException(ProxyGeneratorException.WSDL_IS_ENCODED_JAXWS);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Loads or Generates Client proxy from WSDL file.
   * 
   * @param config
   * @throws ProxyGeneratorException
   */
  public void generateAll(ProxyGeneratorConfigNew config) throws ProxyGeneratorException {
    this.convertor.clear();
    this.config = config;
    if (config.getGenerationMode() == ProxyGeneratorConfigNew.MIGRATION_MODE) {
      convertor.setUnderscoreSeparator(false);
    } else {
      convertor.setUnderscoreSeparator(true);
    }
    // Loads the WSDL Definitions
    loadWSDLapi();
    checkTransportBinding();
    PackageResolverInterface packageResolver = null;
    if (config.isUseCTSPackageMapping() == true) { 
      packageResolver = new PackageResolver(config, isJaxWS()); 
    } else {
      packageResolver = new PackageResolverJAXWS(config, isJaxWS());
    }
    if (config.isFileBufferOutput() && config.getOutputFiles() == null) {
      FileBufferSet fbs = new FileBufferSet("proxyFiles", config.getOutputPath());
      config.setOutputFiles(fbs);
    }
    processSchema(packageResolver);
    if (config.getGenerationMode() == ProxyGeneratorConfigNew.LOAD_MODE || config.getMappingRules() == null) {
      initInterfaceMappings(packageResolver);
      removeDublicatedInterfaceMappings();
      detectCollisions();
    }

    if (config.getGenerationMode() == ProxyGeneratorConfigNew.JAXWS_MODE || config.getGenerationMode() == ProxyGeneratorConfigNew.JAXWS_MODE_SERVER) {                     
      generateInterfaces();
      generateExceptions();

      // name priorities - 1. SEI, 2. Non-exception java class, 3. Exception, 4.
      // Service interface
      // if a name collision is possible (file name clobbering), resolve using
      // JAX-WS 2.0 specification.
      if (config.getGenerationMode() == ProxyGeneratorConfigNew.JAXWS_MODE) {
        generateServiceImplementations(true, packageResolver);
      }
    }
    if (config.getGenerationMode() == ProxyGeneratorConfigNew.INTERFACE_MODE || config.getGenerationMode() == ProxyGeneratorConfigNew.STANDALONE_MODE) {
      generateInterfaces();
      generateHolders();
      generateExceptions();
      generateServices();
    }
    if (config.getGenerationMode() == ProxyGeneratorConfigNew.ARTEFACT_MODE) {
      generateHolders();
      generateExceptions();
    }
    if (config.getGenerationMode() == ProxyGeneratorConfigNew.STANDALONE_MODE) {
      generateSEIImplementations();
      generateServiceImplementations(true, packageResolver);
    }
    if (config.getGenerationMode() == ProxyGeneratorConfigNew.IMPLEMENTATION_MODE) {
      generateServices();
      generateSEIImplementations();
      generateServiceImplementations(false, packageResolver);
    }
    if (config.getGenerationMode() == ProxyGeneratorConfigNew.MIGRATION_MODE) {
      ConfigurationMigrationUtil.migrateLogicalPorts(config);
      generateSEIImplementations();
      generateServiceImplementations(false, packageResolver);
    }
    this.config = null;
  }

  /**
   * Generates service interfaces.
   */
  private void generateServices() throws ProxyGeneratorException {
    ServiceMapping[] sMappings = config.getMappingRules().getService();
    for (int i = 0; i < sMappings.length; i++) {
      generateService(sMappings[i]);
    }
  }

  /**
   * Returns directory that leads to package files according to package name
   * Example : input: "com.example.package" output: "/com/example/package/"
   */
  public String packageToPath(String packageName) {
    char separator = '/';

    if (packageName == null || packageName.length() == 0) {
      return "";
    }
    return packageName.replace('.', '/') + "/";
  }

  /**
   * Generates service interfaces.
   */
  private void generateServiceImplementations(boolean standalone, PackageResolverInterface packageResolver) throws ProxyGeneratorException {
    String mappingPath = null;
    String configPath = null;
    String typePath = null;
    ServiceMapping[] sMappings = config.getMappingRules().getService();
    for (int i = 0; i < sMappings.length; i++) {
      ServiceMapping serviceMapping = sMappings[i];
      String packageName = packageResolver.resolve(serviceMapping.getServiceName().getNamespaceURI());
      if(packageName == null) {
        packageName = convertor.getPackageClass(serviceMapping.getSIName());
        }
      if (i == 0) { // The first service found contains the config files
        String path = packageToPath(packageName);
        mappingPath = path + "mapping.xml";
        configPath = path + "configuration.xml";
        typePath = path + "types.xml";
      }
      generateServiceImpl(serviceMapping, packageName, mappingPath, configPath, typePath, standalone);
    }
    if (standalone && !isJaxWS()) {
      saveConfigurationFiles(mappingPath, configPath, typePath);
    }
  }

  /**
   * Saves the standalone proxy configuration files.
   * 
   * @param mappingPath
   * @param configPath
   * @param typePath
   * @throws ProxyGeneratorException
   */
  private void saveConfigurationFiles(String mappingPath, String configPath, String typePath) throws ProxyGeneratorException {
    if (config.isFileBufferOutput()) {
      ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
      FileBufferSet fbs = config.getOutputFiles();
      try {
        this.config.getSchemaConfig().getTypeSet().saveSettings(byteOutput);
        byteOutput.flush();
        FileBuffer fb = new FileBuffer(typePath);
        fb.setContent(byteOutput.toByteArray());
        fbs.addFile(fb);
      } catch (Exception x) {
        throw new ProxyGeneratorException(ProxyGeneratorException.CREATE_FILE_ERROR, x, typePath);
      }
      byteOutput.reset();
      try {
        MappingFactory.save(this.config.getMappingRules(), byteOutput);
        byteOutput.flush();
        FileBuffer fb = new FileBuffer(mappingPath);
        fb.setContent(byteOutput.toByteArray());
        fbs.addFile(fb);
      } catch (Exception e) {
        throw new ProxyGeneratorException(ProxyGeneratorException.CREATE_FILE_ERROR, e, mappingPath);
      }
      byteOutput.reset();
      try {
        ConfigurationFactory.save(this.config.getProxyConfig(), byteOutput);
        byteOutput.flush();
        FileBuffer fb = new FileBuffer(configPath);
        fb.setContent(byteOutput.toByteArray());
        fbs.addFile(fb);
      } catch (Exception e) {
        throw new ProxyGeneratorException(ProxyGeneratorException.CREATE_FILE_ERROR, e, configPath);
      }
      byteOutput.reset();
    } else {
      File outputPath = new File(this.config.getOutputPath());
      File mappingFile = new File(outputPath, mappingPath);
      File configFile = new File(outputPath, configPath);
      File typeFile = new File(outputPath, typePath);
      FileOutputStream output = null;
      if (!isJaxWS()) {
        try {
          output = new FileOutputStream(typeFile);
          this.config.getSchemaConfig().getTypeSet().saveSettings(output);
          output.close();
        } catch (Exception x) {
          throw new ProxyGeneratorException(ProxyGeneratorException.CREATE_FILE_ERROR, x, typeFile.getAbsolutePath());
        }
        try {
          output = new FileOutputStream(mappingFile);
          MappingFactory.save(this.config.getMappingRules(), output);
          output.close();
        } catch (Exception e) {
          throw new ProxyGeneratorException(ProxyGeneratorException.CREATE_FILE_ERROR, e, mappingFile.getAbsolutePath());
        }
      }
      try {
        output = new FileOutputStream(configFile);
        ConfigurationFactory.save(this.config.getProxyConfig(), output);
        output.close();
      } catch (Exception e) {
        throw new ProxyGeneratorException(ProxyGeneratorException.CREATE_FILE_ERROR, e, configFile.getAbsolutePath());
      }
    }
  }

  /**
   * Loads WSDL contents in the Abstract WSDL API.
   * 
   * @throws ProxyGeneratorException
   */
  private void loadWSDLapi() throws ProxyGeneratorException {
    if (config.getWsdlPath() == null && config.getWsdl() == null && (config.getJaxWSFiles() == null)) {
      throw new ProxyGeneratorException(ProxyGeneratorException.NO_WSDLPATH);
    } else {
      if (config.getWsdlPath() != null) {
        WSDLLoader wsdlLoader = new WSDLLoader();
        if (isJaxWS()) {
          JAXWSFileParser fileParser = new JAXWSFileParser();
          if (config.getResolver() != null) {
            fileParser.setExternalResolver(config.getResolver());
          } else {
            fileParser.setHttpProxy(config.getHTTPProxyHost(), config.getHTTPProxyPort());
          }
          try {
            fileParser.loadWSDL(this.config.getWsdlPath());
            if (config.getExternalBindings() != null) {
              fileParser.loadCustomizations(config.getExternalBindings());
            }
            fileParser.applyJAXWSMappings();
            fileParser.applyJAXBMappings();
            this.config.setJaxWSFiles(fileParser);
            wsdlLoader.setURIResolver(fileParser);
            Definitions def = wsdlLoader.loadDefinitions(fileParser.getRootWSDL(), null);
            if(config.getAppendDefaultBindings()) {
              appendDefaultBindings(def);
            }
            config.setWsdl(def);
            // Loads extended binding definitions
            extendedDefinitions = new ExtendedDefinition(def, true);
            extendedDefinitions.applyExtensions();
          } catch (Exception e) {
            throw new ProxyGeneratorException(ProxyGeneratorException.WSDL_PARSING_PROBLEM, e);
          }
        } else {
          if (config.getResolver() != null) {
            wsdlLoader.setWSDLResolver(config.getResolver());
          } else {
            wsdlLoader.setHttpProxy(config.getHTTPProxyHost(), config.getHTTPProxyPort());
          }
          Hashtable locationMap = config.getLocationMap();
          Definitions def = null;
          try {
            if (locationMap != null) {              
              def = wsdlLoader.load(config.getWsdlPath(), locationMap);
            } else {
              def = wsdlLoader.load(config.getWsdlPath());
            }
            if(config.getAppendDefaultBindings()) {
              appendDefaultBindings(def);
            }
          } catch (Exception x) {
            throw new ProxyGeneratorException(ProxyGeneratorException.WSDL_PARSING_PROBLEM, x);
          }
          config.setWsdl(def);
        }
      } else {
        // The files are loaded externally - CAF use case.
        if (config.getJaxWSFiles() != null && isJaxWS()) {
          try {        
            WSDLLoader wsdlLoader = new WSDLLoader();
            JAXWSFileParser fileParser = config.getJaxWSFiles();
            if (fileParser.getFiles().getJaxWSResources().size() > 0) {
              fileParser.applyJAXWSMappings();
            }
            if (fileParser.getFiles().getJaxBResources().size() > 0) {
              fileParser.applyJAXBMappings();
            }          
            wsdlLoader.setURIResolver(fileParser);
            Definitions def = wsdlLoader.loadDefinitions(fileParser.getRootWSDL(), null);
            if(config.getAppendDefaultBindings()) {
              appendDefaultBindings(def);
            }          
            config.setWsdl(def);
            // Loads extended binding definitions
            extendedDefinitions = new ExtendedDefinition(def, true);
            extendedDefinitions.applyExtensions();
          } catch (Exception e) {
            throw new ProxyGeneratorException(ProxyGeneratorException.WSDL_PARSING_PROBLEM, e);
          }          
        }
      }
    }

    try {      
      if (config.getProxyConfig() == null) {
        ConfigurationMarshallerFactory configMarshaller = config.getConfigMarshaller();
        ConfigurationBuilder builder;
        if (configMarshaller == null) {
          builder = new ConfigurationBuilder();
        } else {
          builder = new ConfigurationBuilder(configMarshaller);
        }
        ConfigurationRoot root;
        if (config.isProvider()) {
          root = builder.create(config.getWsdl(), IConfigurationMarshaller.PROVIDER_MODE);
        } else {
          root = builder.create(config.getWsdl());
        }
        // Set binding "original" flag to true (required for Dynamic proxy mass config support).
        setServicesDefaultFlag(root);
        config.setProxyConfig(root);
      }      
    } catch (Exception e) {
      throw new ProxyGeneratorException(ProxyGeneratorException.WSDL_PARSING_PROBLEM, e);
    }
  }
  
  private void setServicesDefaultFlag(ConfigurationRoot config) {
    Service[] services = config.getRTConfig().getService();
    for (Service service : services) {
      ServiceData serviceData = service.getServiceData();
      BindingData[] bindingDatas = serviceData.getBindingData();
      for (BindingData data : bindingDatas) {
        data.setOriginal(true);
      }
    }    
  }

   private void appendDefaultBindings(Definitions wsdlDefs) throws WSDLException {
     ObjectList interfaces = wsdlDefs.getInterfaces();
     ObjectList bindings = wsdlDefs.getBindings();
     if(interfaces.getLength() == 1 && bindings.getLength() == 0) {
       XIDefaultBindingAppender xiDefaultBindingAdapter = new XIDefaultBindingAppender();
       try {
         xiDefaultBindingAdapter.appendDefaultBinding(wsdlDefs, "DefaultBinding");
       } catch (Exception e) {
         throw new WSDLException("Problem during appending Default Binding to XI WSDL", e);
       }
     }
   }

  /**
   * Saves the type mapping file.
   * 
   * @param config
   * @throws ProxyGeneratorException
   */
  private void saveTypeMappingFile(SchemaToJavaConfig config) throws ProxyGeneratorException {
    File perm = new File(config.getOutputDir(), convertor.packageToPath(config.getBasePackageName()));
    perm.mkdirs();
    File file = new File(perm, "types.xml");

    FileOutputStream output;
    try {
      output = new FileOutputStream(file);
      config.getTypeSet().saveSettings(output);
      output.close();
    } catch (FileNotFoundException e) {
      throw new ProxyGeneratorException(ProxyGeneratorException.SCHEMA_PROCESS_PROBLEM, e);
    } catch (IOException e) {
      throw new ProxyGeneratorException(ProxyGeneratorException.SCHEMA_PROCESS_PROBLEM, e);
    }
  }
  
  /**
   * Pass Schema element and get all top level imports.
   * @param element
   * @return ArrayList containing schema import elements.
   */
  private ArrayList<Element> getSchemaImport(Element element) {
    Node node = element.getFirstChild();
    ArrayList<Element> result = new ArrayList<Element>();
    while (node != null) {
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element child = (Element) node;
        if (child.getLocalName().equals("import") && "http://www.w3.org/2001/XMLSchema".equals(child.getNamespaceURI())) {
           result.add(child);
        }
      }
      node = node.getNextSibling();
    }
    return result;
  }
  

  /**
   * Loads or generates the XML Schema information. Also saves type.xml
   * configuration file.
   * 
   * @throws ProxyGeneratorException
   */
  private void processSchema(PackageResolverInterface packageResolver) throws ProxyGeneratorException {
    SchemaToJavaConfig schemaConfig = config.getSchemaConfig();
    if (schemaConfig == null) {
      // there is no default schema to java config create a new one
      schemaConfig = new SchemaToJavaConfig(ImplementationVersion.JAX_RPC);
      config.setSchemaConfig(schemaConfig);
    }
    schemaConfig.setFileBufferOutput(config.isFileBufferOutput());

    // JAX-WS does not allow this
    if (!isJaxWS()) {
      if (config.getOutputPackage() != null && config.getOutputPackage().length() != 0) {
        schemaConfig.setBasePackageName(config.getOutputPackage() + ".types");
      } else {
        schemaConfig.setBasePackageName("types");
      }
    }
    if (config.getOutputPath() == null && config.getGenerationMode() != ProxyGeneratorConfigNew.GENERIC_MODE_SDO && config.getGenerationMode() != ProxyGeneratorConfigNew.GENERIC_MODE && config.getGenerationMode() != ProxyGeneratorConfigNew.LOAD_MODE && config.getGenerationMode() != ProxyGeneratorConfigNew.JAXWS_MODE_LOAD) {
      throw new ProxyGeneratorException(ProxyGeneratorException.CANT_CREATEPATH,"NULL");
    }
    if (config.getOutputPath() != null) {
      schemaConfig.setOutputDir(new File(config.getOutputPath()));
    }
    ArrayList schemaSources = new ArrayList();
    schemaSources.addAll(config.getWsdl().getXSDTypeContainer().getSchemas());
    if (isJaxWS()) { // Extract nested schemas into separate documents.
      for (int i = 0; i < schemaSources.size(); i++) {
        DOMSource domSource = (DOMSource) schemaSources.get(i);
        Element elementRoot = (Element) domSource.getNode();
        Node parent = elementRoot.getParentNode();
        if (parent != null && parent.getNodeType() != Node.DOCUMENT_NODE) {
          // Get all valid namespace mappings in scope
          Hashtable hash = DOM.getNamespaceMappingsInScope(elementRoot);
          Enumeration enumeration = hash.keys();
          while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            String value = (String) hash.get(key);
            if (key == null || key.length() == 0) {
              elementRoot.setAttributeNS(NS.XMLNS,"xmlns", value);
            } else {
              elementRoot.setAttributeNS(NS.XMLNS, "xmlns:" + key, value);
            }
          }
        }
        ArrayList<Element> schemaImports = getSchemaImport(elementRoot);
        for (Element importTag : schemaImports) {
          importTag.removeAttribute("schemaLocation");
        }
      }
    }    
    schemaConfig.setSchemaSources(schemaSources);
    schemaConfig.setSchemaResolver(config.getWsdl().getXSDTypeContainer().getURIResolver());

    switch (config.getGenerationMode()) {
      case ProxyGeneratorConfigNew.JAXWS_MODE:
      case ProxyGeneratorConfigNew.JAXWS_MODE_SERVER:
      case ProxyGeneratorConfigNew.JAXWS_MODE_LOAD: {

        File outputPath = schemaConfig.getOutputDir();

        try {
          boolean generateFiles = true;
          if (config.getGenerationMode() == ProxyGeneratorConfigNew.JAXWS_MODE_LOAD) {
            generateFiles = false;
            //javaGen.setFileGeneration(false);
          }          
          JaxbSchemaParser schemaParser = new JaxbSchemaParser();
          schemaParser.setShortInnerNames(config.isShortInnerClassNames());
          if (config.isShortInnerClassNames() && config.isMapToTopLevel()) {
            schemaParser.setTopLevelMapping();
          }
          schemaParser.loadSchemaAndTypes(schemaConfig);
          JaxbSchemaToJavaWrapper javaGen = new JaxbSchemaToJavaWrapper((DOMSource[]) schemaSources.toArray(new DOMSource[schemaSources.size()]),outputPath, config.getJaxWSFiles(),generateFiles);
          //SerializationFRMClassLoader ldr = new SerializationFRMClassLoader(this.getClass().getClassLoader(), outputPath.getAbsolutePath());
          ClassAllocator allocator = initClassAllocator(packageResolver);
          javaGen.setAllocator(allocator);
          javaGen.setJAXBVersion(config.getJAXBVersion());
          Model schemaToJavaModel = javaGen.generate();

          JaxbSchemaToJavaMap typeMap = new JaxbSchemaToJavaMap(schemaConfig.getTypeSet(), schemaToJavaModel,schemaConfig.getSchema());
          typeMap.initializeMappings(schemaParser.getAllElements(),schemaParser.getAnonymousTypes());

        } catch (SchemaToJavaGeneratorException e) {
          throw new ProxyGeneratorException(ProxyGeneratorException.SCHEMA_PROCESS_PROBLEM, e);
        } catch (JaxbSchemaToJavaGenerationException e) {          
          throw new ProxyGeneratorException(ProxyGeneratorException.SCHEMA_PROCESS_PROBLEM, e);
        } // FIXME: better exception handling here!

        break;
      }
      case ProxyGeneratorConfigNew.MIGRATION_MODE: {                
        schemaConfig.setGenerationMode(SchemaToJavaConfig.LOAD_MODE);
        schemaConfig.setDocumentArrayUnwrapped(true);
        try {
          TypeMappingMigrationUtil.addMissingSchemaTypes(schemaConfig.getSchemaSources());
          TypeMappingMigrationUtil.preProcessTypeMapping(schemaConfig.getTypeSet());
          getSchemaGenerator().generateAll(schemaConfig);
          TypeMappingMigrationUtil.postProcessTypeMapping(schemaConfig.getTypeSet());
        } catch (SchemaToJavaGeneratorException e) {
          throw new ProxyGeneratorException(ProxyGeneratorException.SCHEMA_PROCESS_PROBLEM, e);
        }
        break;
      }
      case ProxyGeneratorConfigNew.GENERIC_MODE_SDO: { // TODO: SDO case does not need schema to java mapping, but most of the other code needs it
        break;
      }
      case ProxyGeneratorConfigNew.GENERIC_MODE: {
        schemaConfig.setUseGenericContent(true);
        schemaConfig.setGenerationMode(SchemaToJavaConfig.LOAD_MODE);
        try {
          getSchemaGenerator().generateAll(schemaConfig);
        } catch (SchemaToJavaGeneratorException e) {
          throw new ProxyGeneratorException(ProxyGeneratorException.SCHEMA_PROCESS_PROBLEM, e);
        }
        break;
      }
      case ProxyGeneratorConfigNew.LOAD_MODE:
      case ProxyGeneratorConfigNew.ARTEFACT_MODE: {
        schemaConfig.setGenerationMode(SchemaToJavaConfig.LOAD_MODE);
        try {
          getSchemaGenerator().generateAll(schemaConfig);
        } catch (SchemaToJavaGeneratorException e) {
          throw new ProxyGeneratorException(ProxyGeneratorException.SCHEMA_PROCESS_PROBLEM, e);
        }
        // saveTypeMappingFile(schemaConfig);
        break;
      }
      case ProxyGeneratorConfigNew.INTERFACE_MODE: {
        schemaConfig.setGenerationMode(SchemaToJavaConfig.CONTAINER_MODE);
        try {
          getSchemaGenerator().generateAll(schemaConfig);
        } catch (SchemaToJavaGeneratorException e) {
          throw new ProxyGeneratorException(ProxyGeneratorException.SCHEMA_PROCESS_PROBLEM, e);
        }
        // saveTypeMappingFile(schemaConfig);
        break;
      }
      case ProxyGeneratorConfigNew.IMPLEMENTATION_MODE: {
        schemaConfig.getUriToPackageMapping().clear();
        schemaConfig.setBasePackageName(schemaConfig.getBasePackageName() + ".frm");
        schemaConfig.setGenerationMode(SchemaToJavaConfig.FRAMEWORK_MODE);
        try {
          getSchemaGenerator().generateAll(schemaConfig);
        } catch (SchemaToJavaGeneratorException e) {
          throw new ProxyGeneratorException(ProxyGeneratorException.SCHEMA_PROCESS_PROBLEM, e);
        }
        saveTypeMappingFile(schemaConfig);
        break;
      }
      case ProxyGeneratorConfigNew.STANDALONE_MODE: { // Generate container and
        // serializers
        schemaConfig.setGenerationMode(SchemaToJavaConfig.CONTAINER_MODE);
        try {
          getSchemaGenerator().generateAll(schemaConfig);
        } catch (SchemaToJavaGeneratorException e) {
          throw new ProxyGeneratorException(ProxyGeneratorException.SCHEMA_PROCESS_PROBLEM, e);
        }
        schemaConfig.setBasePackageName(schemaConfig.getBasePackageName() + ".frm");
        schemaConfig.setGenerationMode(SchemaToJavaConfig.FRAMEWORK_MODE);
        schemaConfig.getUriToPackageMapping().clear();
        try {
          getSchemaGenerator().generateAll(schemaConfig);
        } catch (SchemaToJavaGeneratorException e) {
          throw new ProxyGeneratorException(ProxyGeneratorException.SCHEMA_PROCESS_PROBLEM, e);
        }
        // saveTypeMappingFile(schemaConfig);
        break;
      }
    }
  }
  
  private ClassAllocator initClassAllocator(PackageResolverInterface packageResolver) throws ProxyGeneratorException {
    ClassAllocator result = new ClassAllocator();
    Definitions wsdlDefinitions = config.getWsdl();
    ObjectList objectList = wsdlDefinitions.getBindings();
    for (int i = 0; i < objectList.getLength(); i++) {
      Binding binding = (Binding) objectList.item(i);
      QName interfaceName = binding.getInterface();
      Interface wsInterface = wsdlDefinitions.getInterface(interfaceName);
      if (wsInterface == null) {
        throw new ProxyGeneratorException(ProxyGeneratorException.MISSING_WSDL_INTERFACE, binding.getName().toString(), interfaceName.toString());
      }
      ExtendedPortType extInt = extendedDefinitions.getExtendedPortType(interfaceName);
      String javaClassName = NameConverter.smart.toClassName(interfaceName.getLocalPart());
      if (extInt != null) {
        String newName = extInt.getClassName();
        if (newName != null) {
          javaClassName = newName;
        }
      }     
      javaClassName = getNameWithPackageJAXWS(javaClassName, packageResolver.resolve(interfaceName.getNamespaceURI()));
      // Convert the SEI Class Name to lower case so name collision check could be non case sensitive.     
      result.outputInterfaceNames.add(javaClassName.toLowerCase(Locale.ENGLISH));
    }
    if (objectList.getLength() == 0) {
      throw new ProxyGeneratorException(ProxyGeneratorException.UNSUPPORTED_WSDL);
    }
    return result;
  }

  /**
   * Initializes the default WSDL to Java mapping information. 1. Gets all WSDL
   * Bindings 2. Gets related interfaces 3. Loads default interface mapping 4.
   * Calls binding implementation to load binding properties
   */
  private void initInterfaceMappings(PackageResolverInterface packageResolver) throws ProxyGeneratorException {
    Definitions wsdlDefinitions = config.getWsdl();
    ObjectList objectList = wsdlDefinitions.getBindings();
    for (int i = 0; i < objectList.getLength(); i++) {
      Binding binding = (Binding) objectList.item(i);
      QName interfaceName = binding.getInterface();
      String packageName = packageResolver.resolve(interfaceName.getNamespaceURI());
      Interface wsInterface = wsdlDefinitions.getInterface(interfaceName);
      if (wsInterface == null) {
        throw new ProxyGeneratorException(ProxyGeneratorException.MISSING_WSDL_INTERFACE, binding.getName().toString(), interfaceName.toString());
      }
      initSEI(wsInterface, binding, packageName);
    }
    if (objectList.getLength() == 0) {
      throw new ProxyGeneratorException(ProxyGeneratorException.UNSUPPORTED_WSDL);
    }
    objectList = wsdlDefinitions.getServices();
    if (objectList.getLength() == 0) {
      // Adds default service
      QName defaultServiceName = new QName("urn:defaultServiceNS", "DefaultService");
      try {
        wsdlDefinitions.appendService(defaultServiceName);
        objectList = wsdlDefinitions.getServices();
      } catch (WSDLException e) {
        throw new ProxyGeneratorException(ProxyGeneratorException.GENERATION_PROBLEM, e);
      }
    }
    for (int i = 0; i < objectList.getLength(); i++) {
      com.sap.engine.services.webservices.espbase.wsdl.Service wsdlService = (com.sap.engine.services.webservices.espbase.wsdl.Service) objectList.item(i);
      String packageName = packageResolver.resolve(wsdlService.getName().getNamespaceURI());
      initServiceInterface(wsdlService, packageName);
    }
  }

  private void setServiceMappingReference(String uid, com.sap.engine.services.webservices.espbase.wsdl.Service wsdlService) {
    QName serviceName = wsdlService.getName();
    ConfigurationRoot confRoot = this.config.getProxyConfig();
    com.sap.engine.services.webservices.espbase.configuration.Service[] services = confRoot.getRTConfig().getService();
    for (int i = 0; i < services.length; i++) {
      QName qname = new QName(services[i].getServiceData().getNamespace(), services[i].getServiceData().getName());
      if (serviceName.equals(qname)) {
        services[i].setServiceMappingId(uid);
      }
    }
  }

  /**
   * Inits service mapping from WSDL service.
   * 
   * @param wsdlService
   */
  private void initServiceInterface(com.sap.engine.services.webservices.espbase.wsdl.Service wsdlService, String packageName) {
    ServiceMapping sMapping = new ServiceMapping();
    ExtendedService exServ = null;
    QName serviceName = wsdlService.getName();
    sMapping.setServiceName(serviceName);
    if (isJaxWS()) {
      exServ = extendedDefinitions.getExtendedService(sMapping.getServiceName());
      if (exServ != null) {
        if (exServ.getNewName() != null) { 
          sMapping.setSIName(getNameWithPackageJAXWS(exServ.getNewName(), packageName));
        } else {
          String className = NameConverter.smart.toClassName(wsdlService.getName().getLocalPart());
          String javaClassName = getNameWithPackageJAXWS(className, packageName);
          sMapping.setSIName(javaClassName);
        }
        if (exServ.getJavaDoc() != null) { 
          sMapping.setJavaDoc(exServ.getJavaDoc());
        }
      } else {
        String className = NameConverter.smart.toClassName(wsdlService.getName().getLocalPart());
        String javaClassName = getNameWithPackageJAXWS(className, packageName);
        sMapping.setSIName(javaClassName);        
      }
    } else {        
      sMapping.setSIName(getConvertedNameWithPackage(wsdlService.getName().getLocalPart(), packageName));
    }
    // Sets unique mapping id
    UID uid = new UID();
    sMapping.setServiceMappingId(uid.toString());
    // Adds references from the config to the mapping
    setServiceMappingReference(uid.toString(), wsdlService);
    ObjectList objectList = wsdlService.getEndpoints();
    for (int i = 0; i < objectList.getLength(); i++) {
      Endpoint ep = (Endpoint) objectList.item(i);
      String epName = ep.getName();
      QName epBinding = ep.getBinding();
      Binding binding = config.getWsdl().getBinding(epBinding);
      QName pType = binding.getInterface();
      EndpointMapping epMapping = new EndpointMapping();
      epMapping.setPortQName(epName);
      String portJavaName = convertor.attributeToClassName(epName);
      if (isJaxWS() && exServ != null) {
        ExtendedEndpoint extEP = (ExtendedEndpoint) exServ.getExtendedChild(new QName(epMapping.getPortQName()));
        if (extEP != null) {
          // TODO: Provider support
          if (extEP.getMethodName() != null) {
            epMapping.setPortGetter(extEP.getMethodName());
          }
          if (extEP.getJavaDoc() != null) {
            epMapping.setJavaDoc(extEP.getJavaDoc());
          }
        }          
      }
      epMapping.setPortJavaName(portJavaName);
      epMapping.setPortBinding(epBinding);
      epMapping.setPortPortType(pType);
      // If the endpoint is not supported - do not add it
      if (this.config.getMappingRules().getInterface(pType, epBinding) != null) {
        sMapping.addEndpoint(epMapping);
      }
    }
    this.config.getMappingRules().addService(sMapping);
  }

  /**
   * Generates SEI classes using the mapping information.
   * 
   * @throws ProxyGeneratorException
   */
  private void generateInterfaces() throws ProxyGeneratorException {
    InterfaceMapping[] mappings = config.getMappingRules().getInterface();
    for (int i = 0; i < mappings.length; i++) {
      generateSEI(mappings[i]);
    }
  }

  /**
   * Generates SEI implementations.
   * 
   * @throws ProxyGeneratorException
   */
  private void generateSEIImplementations() throws ProxyGeneratorException {
    InterfaceMapping[] mappings = config.getMappingRules().getInterface();
    for (int i = 0; i < mappings.length; i++) {
      generateSEIImpl(mappings[i]);
    }
  }

  private void removeInterfaceMapping(String interfaceMappingId) {
    MappingRules mappingRules = config.getMappingRules();
    if (mappingRules != null) {
      InterfaceMapping interfaceMapping = mappingRules.removeInterface(interfaceMappingId);
      if (interfaceMapping != null) {
        // Interface mapping is deleted
        ConfigurationRoot wsConfig = config.getProxyConfig();
        InterfaceDefinition interfaceDefinition = wsConfig.getDTConfig().getInterfaceDefinition(interfaceMappingId);        
        if (interfaceDefinition != null) {
          // Gets the portType name.
          QName portTypeName = interfaceMapping.getPortType();
          // Find the first Interface Mapping which has different InterfaceMappingId and the same portType name.
          InterfaceMapping[] interfaces = mappingRules.getInterface();
          for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i].getPortType().equals(portTypeName)) {
              // There is other interface mapping which uses the same portType. Reassign the interface definition to this mapping.
              interfaceDefinition.setInterfaceMappingId(interfaces[i].getInterfaceMappingID());
              break;
            }
          }
          if (interfaceDefinition.getInterfaceMappingId().equals(interfaceMappingId)) {
            // There are no more interface mappings for this interface definition - remove it.
            wsConfig.getDTConfig().removeInterfaceDefinition(interfaceMappingId);
          }
          // Interface is deleted. Delete all logical ports that reference it.
          Service[] services = wsConfig.getRTConfig().getService();
          for (int i = 0; i < services.length; i++) {
            ServiceData serviceData = services[i].getServiceData();
            serviceData.removeAllPorts(interfaceMappingId);
          }
        }
      }
    }

  }

  /**
   * Returns the interface mapping for passed (binding,interface) pair.
   * 
   * @param bindingName
   * @param interfaceName
   * @return
   */
  private InterfaceMapping getInterfaceMapping(QName bindingName, QName interfaceName) {
    MappingRules mappingRules = config.getMappingRules();
    if (mappingRules == null) {
      mappingRules = new MappingRules();
      config.setMappingRules(mappingRules);
    }
    InterfaceMapping[] imappings = mappingRules.getInterface();
    for (int i = 0; i < imappings.length; i++) {
      if (imappings[i].getBindingQName().equals(bindingName) && imappings[i].getPortType().equals(interfaceName)) {
        return imappings[i];
      }
    }
    InterfaceMapping imapping = new InterfaceMapping();
    imapping.setBindingQName(bindingName);
    imapping.setPortType(interfaceName);
    mappingRules.addInterface(imapping);
    return imapping;
  }

  /**
   * Converts WSDL parameter type constant to corresponding ParameterMapping parameter type constant.
   * @param pMapping
   * @param parameter
   */
  private void setParameterType(ParameterMapping pMapping, Parameter parameter) {
    switch (parameter.getParamType()) {
      case Parameter.IN: {
        pMapping.setParameterType(ParameterMapping.IN_TYPE);
        break;
      }
      case Parameter.OUT: {
        pMapping.setParameterType(ParameterMapping.OUT_TYPE);
        break;
      }
      case Parameter.INOUT: {
        pMapping.setParameterType(ParameterMapping.IN_OUT_TYPE);
        break;
      }
      case Parameter.RETURN: {
        pMapping.setParameterType(ParameterMapping.RETURN_TYPE);
        break;
      }
      case Parameter.FAULT: {
        pMapping.setParameterType(ParameterMapping.FAULT_TYPE);
        break;
      }
    }
  }

  /**
   * Returns correct generated holder name.
   * 
   * @param parameterMapping
   * @return
   */
  private String getHolderName(ParameterMapping parameterMapping) {
    String javaName = parameterMapping.getJavaType();
    String holderName = convertor.getLocalClass(javaName);
    int pos1 = holderName.indexOf('[');
    if (pos1 != -1) {
      holderName = convertor.attributeToClassName(parameterMapping.getSchemaQName().getLocalPart());
    }
    holderName += "Holder";
    return holderName;
  }
  
  /**
   * Extracts JAXB specific information that should be annotated in the generated SEI.
   */
  private void extractJAXBInfo(ParameterMapping parameter, SchemaTypeInfo info) {
    ArrayList customizations = info.getCustomizations(SimpleTypeMapping.class);
    if (customizations != null && customizations.size() == 1) {
      SimpleTypeMapping simpleTypeMap = (SimpleTypeMapping) customizations.get(0);
      if (simpleTypeMap.isListType()) {
        parameter.setProperty(ParameterMapping.IS_PARAMETER_SIMPLE_LIST,"true");
      }
      if (simpleTypeMap.getAdapterClass() != null) {
        parameter.setProperty(ParameterMapping.VALUE_ADAPTER,simpleTypeMap.getAdapterClass());
      }
    }    
  }
  
  /**
   * Converts the parameter type constant to customization constant.
   * @param parameterMappingType
   * @return
   */
  private int convertParameterTypeToCustType(int parameterMappingType) {
    int paramType = ExtendedOperationMod.IN;
    if (parameterMappingType == ParameterMapping.OUT_TYPE) {
      paramType = ExtendedOperationMod.OUT;
    }
    if (parameterMappingType == ParameterMapping.IN_OUT_TYPE) {
      paramType = ExtendedOperationMod.INOUT;
    }
    return paramType;
  }
  
  /**
   * Loads default Parameter information from WSDL.
   * 
   * @param pMappingNew
   * @param param
   * @param schemaTypeSet
   */
  private void initParameterMapping(ParameterMapping pMappingNew, Parameter param, SchemaTypeSet schemaTypeSet, ExtendedPorttypeOperation extendedOperation) {
    XSDRef xsdreference = param.getXSDTypeRef();
    pMappingNew.setWSDLParameterName(param.getName());
    setParameterType(pMappingNew, param);
    // WSDL to JAVA mapping handling - sets default parameter java name.
    pMappingNew.setJavaParamName(convertor.attributeToIdentifier(pMappingNew.getWSDLParameterName()));
    if (isJaxWS()) { // Reads JAXWS customizations
      // TODO: Add specific JAXWS WSDL to Java name mapping rules.
      if (extendedOperation != null) {
        int paramType = convertParameterTypeToCustType(pMappingNew.getParameterType());
        ExtendedOperationMod pTypeMod = (ExtendedOperationMod) extendedOperation.getParameterCust(paramType,pMappingNew.getWSDLParameterName(),null);
        if (pTypeMod != null) {
          if (pTypeMod.getNewName() != null) {
            pMappingNew.setJavaParamName(pTypeMod.getNewName());
          }
        }
      }
    }
    pMappingNew.setIsElement(xsdreference.getXSDType() == XSDRef.ELEMENT);
    QName xsdParameterType = xsdreference.getQName();
    pMappingNew.setSchemaQName(xsdParameterType);
    if (config.getGenerationMode() == ProxyGeneratorConfigNew.GENERIC_MODE_SDO) {
      pMappingNew.setJavaType(Object.class.getName());
    } else {
      if (xsdreference.getXSDType() == XSDRef.ELEMENT) {
        xsdParameterType = schemaTypeSet.getElementType(xsdParameterType);
        if (xsdParameterType == null) {
          throw new RuntimeException("The definition of element " + xsdreference.getQName() + " is missing from the Schema !");
        }
      }
      SchemaTypeInfo info = schemaTypeSet.get(xsdParameterType);
      if (info != null) {
        extractJAXBInfo(pMappingNew, info);
        pMappingNew.setJavaType(info.getJavaClass());
      }
    }
    if (pMappingNew.getParameterType() == ParameterMapping.IN_OUT_TYPE || pMappingNew.getParameterType() == ParameterMapping.OUT_TYPE) {      
      // INOUT or OUT parameter type - must be mapped to holder class
      String holderName = null;
      if (config.getGenerationMode() == ProxyGeneratorConfigNew.GENERIC_MODE || config.getGenerationMode() == ProxyGeneratorConfigNew.GENERIC_MODE_SDO) {
        holderName = "javax.xml.rpc.holders.ObjectHolder";        
      } else {                
        QName schemaType = pMappingNew.getSchemaQName();
        boolean isComplex = false;
        if (this.config.getSchemaConfig().getSchema() != null) {
          TypeDefinitionBase type = this.config.getSchemaConfig().getSchema().getTopLevelTypeDefinition(schemaType.getNamespaceURI(), schemaType.getLocalPart());
          if (type != null && type instanceof ComplexTypeDefinition && !NS.SOAPENC.equals(schemaType.getNamespaceURI())) {
            isComplex = true;
          }
        }
        if (!isComplex && config.getGenerationMode() == ProxyGeneratorConfigNew.MIGRATION_MODE && schemaType.getNamespaceURI().equals("http://schemas.xmlsoap.org/soap/encoding/") && !schemaType.getLocalPart().equals("base64")) {
          // Holder handling for soap-encoding types - used for migration from 640
          // note that soapenc:base64 is a special case for which a custom holder is NOT generated by 640 - javax.xml.rpc.holders.ByteArrayHolder is used instead
          holderName = getHolderName(pMappingNew);
          if (config.getOutputPackage() != null && config.getOutputPackage().length() != 0) {
            holderName = config.getOutputPackage() + ".holders." + holderName;
          } else {
            holderName = "holders." + holderName;
          }
        } else { // Pure JAX-RPC or JAX-WS case
          if (isJaxWS()) {
            if (isComplex == false) {
              pMappingNew.setJavaType(convertor.wrap(pMappingNew.getJavaType()));
            }
            holderName = "javax.xml.ws.Holder<" + pMappingNew.getJavaType() + ">";
          } else {  
            if (isComplex == false && convertor.hasBuiltInHolder(pMappingNew.getJavaType())) {
              holderName = convertor.primitiveToHolder(pMappingNew.getJavaType());
            } else {
              holderName = getHolderName(pMappingNew);
              if (config.getOutputPackage() != null && config.getOutputPackage().length() != 0) {
                holderName = config.getOutputPackage() + ".holders." + holderName;
              } else {
                holderName = "holders." + holderName;
              }              
            }
          }
        }        
      }
      pMappingNew.setHolderName(holderName); // Holder name is never NULL, from the statements above
    }
  }

  /**
   * Loads default Fault information from WSDL.
   * 
   * @param pMappingNew
   * @param param
   * @param schemaTypeSet
   */
  private void initFaultMapping(ParameterMapping pMappingNew, Parameter param, SchemaTypeSet schemaTypeSet, ExtendedPorttypeOperation extendedOperation, String packageName) throws ProxyGeneratorException {
    XSDRef xsdreference = param.getXSDTypeRef();
    pMappingNew.setWSDLParameterName(param.getName());
    setParameterType(pMappingNew, param);
    pMappingNew.setIsElement(false);
    QName faultType;    
    TypeDefinitionBase type = null; // Fault type definition. 
    if (xsdreference.getXSDType() == XSDRef.ELEMENT) { // Fault reference to element (BP 1.1 compliant)
      pMappingNew.setFaultElementQName(xsdreference.getQName());
      if (config.getGenerationMode() == ProxyGeneratorConfigNew.GENERIC_MODE_SDO) {
        // WS DAS Case.
        pMappingNew.setIsElement(true);
        pMappingNew.setSchemaQName(xsdreference.getQName());
        pMappingNew.setJavaType(WebServiceException.class.getName());
        return;        
      } else {
        // Sets fault XSD Type into the parameter mapping.
        pMappingNew.setSchemaQName(schemaTypeSet.getElementType(xsdreference.getQName()));        
        ElementDeclaration element = config.getSchemaConfig().getSchema().getTopLevelElementDeclaration(xsdreference.getQName().getNamespaceURI(),xsdreference.getQName().getLocalPart());
        type = element.getTypeDefinition();
      }
    } else { // Fault reference to type (Non BP 1.1 compliant)      
      if (isJaxWS() || config.getGenerationMode() == ProxyGeneratorConfigNew.GENERIC_MODE_SDO) {
        throw new ProxyGeneratorException(ProxyGeneratorException.WRONG_FAULT_ELEMENT, pMappingNew.getWSDLParameterName());
      }
      pMappingNew.setSchemaQName(xsdreference.getQName());
      pMappingNew.setFaultElementQName(new QName(null, param.getName())); // Sets the fault element name to the WSDL part name.
      type = config.getSchemaConfig().getSchema().getTopLevelTypeDefinition(xsdreference.getQName().getNamespaceURI(), xsdreference.getQName().getLocalPart());
    }
    // Derives the Exception class name to be generated also it's structure.
    if (config.getGenerationMode() == ProxyGeneratorConfigNew.GENERIC_MODE) {
      // Dynamic proxy does not generate exceptions
      pMappingNew.setJavaType(WebServiceException.class.getName());
      return;
    }
    String javaClassName = null;
    QName messageQName = QName.valueOf(param.getProperty(WSDL11Constants.MESSAGE_QNAME));
    if (messageQName != null) {
      pMappingNew.setProperty(ParameterMapping.FAULT_MESSAGE, messageQName.toString());
    }
    if (isJaxWS()) { // JAX-WS mode
      String className = NameConverter.smart.toClassName(messageQName.getLocalPart());
      javaClassName = getNameWithPackageJAXWS(className, packageName);
      javaClassName = checkExceptionName(javaClassName);
      if (extendedOperation != null) {
        ExtendedOperationMod extOpMod = (ExtendedOperationMod) extendedOperation.getExtendedChild(new QName(pMappingNew.getWSDLParameterName()));
        if (extOpMod != null) {
          String jDoc = extOpMod.getJavaDoc();
          if (jDoc != null) {
            pMappingNew.setJavaDoc(jDoc);
          }
          String newFaultClass = extOpMod.getFaultClass();
          if (newFaultClass != null) {
            javaClassName = getNameWithPackageJAXWS(newFaultClass, packageName);
          }
        }
      }
    } else { // JAX-RPC Mode
      if (type instanceof SimpleTypeDefinition || (type.isBuiltIn() == true)) { // The fault content type is simple
        javaClassName = getConvertedNameWithPackage(messageQName.getLocalPart(), packageName);
      } else { // The fault content type is complex
        if (type.getName() == null || type.getName().length() == 0) {
          // Anonymous complex type - only elements can derive that use case
          javaClassName = getConvertedNameWithPackage(xsdreference.getQName().getLocalPart(), packageName);
          // 640 appends "Exception"
          if (config.getGenerationMode() == ProxyGeneratorConfigNew.MIGRATION_MODE) {
            javaClassName = javaClassName + "Exception";
          }
        } else {
          // Top levevel complex type
          javaClassName = getConvertedNameWithPackage(type.getName(), packageName);
        }
        ArrayList<ParameterMapping> attributes = new ArrayList<ParameterMapping>();
        ArrayList<ParameterMapping> elements = new ArrayList<ParameterMapping>();
        ArrayList<ParameterMapping> allParams = new ArrayList<ParameterMapping>();
        ArrayList elementJavaTypes = new ArrayList();
        getSequenceFields(type, attributes,elements);
        int attribCount = attributes.size();
        allParams.addAll(attributes);
        allParams.addAll(elements);
        StringBuffer params = new StringBuffer();
        StringBuffer pTypes = new StringBuffer();
        String attribIndex = "";
        for (int i = 0; i < allParams.size(); i++) {
          if (i > 0) {
            params.append(" ");
            pTypes.append(" ");
          }
          params.append(allParams.get(i).getWSDLParameterName());
          pTypes.append(allParams.get(i).getJavaType());
          if (i > 0) {
            attribIndex += " ";
          }
          if (i < attribCount) {
            attribIndex += "1";
          } else {
            attribIndex += "0";
          }

        }
        pMappingNew.setFaultConstructorParamOrder(params.toString());
        pMappingNew.setProperty(ParameterMapping.FAULT_CONSTRUCTOR_PARAM_TYPES, pTypes.toString());
        pMappingNew.setProperty(ParameterMapping.FAULT_ATTRIBUTE_COUNT, attribIndex);
      }
    }
    pMappingNew.setJavaType(javaClassName);
  }

  /**
   * Creates qualified class name from token.
   * 
   * @param name
   * @param packageName
   * @return
   */
  private String getNameWithPackage(String name, String packageName) {
    if (packageName != null && packageName.length() != 0) {
      return packageName + "." + name;
    } else {
      return name;
    }
  }
  
  /**
   * Creates qualified class name from token. Before concatenating the 
   * package name and the class name, the class name is converted according
   * to the java conventions.
   * 
   * @param name
   * @param packageName
   * @return
   */
  private String getConvertedNameWithPackage(String name, String packageName) {
    return getNameWithPackage(convertor.attributeToClassName(name), packageName);    
  }

  /**
   * Creates qualified class name from token.
   * 
   * @param name
   * @param packageName
   * @return
   */
  private String getNameWithPackageJAXWS(String className, String packageName) {
    if (packageName != null && packageName.length() != 0) {
      return packageName + "." + className;
    } else {
      return className;
    }
  }

  private ArrayList<ParameterMapping> collectFields(ComplexTypeDefinition complexType , boolean migration) throws ProxyGeneratorException {
    ArrayList<ParameterMapping> result = new ArrayList<ParameterMapping>();
    Particle cm = complexType.getContentTypeContentModel();
    if (cm != null && cm.getTerm() instanceof ModelGroup) {
      ModelGroup mg = (ModelGroup) cm.getTerm();
      if (mg.isCompositorSequence()) {        
        Particle[] elements = mg.getParticlesArray();
        if (migration && complexType.getBaseTypeDefinition() instanceof ComplexTypeDefinition
            && !((ComplexTypeDefinition) complexType).getBaseTypeDefinition().isBuiltIn()) {
          // 6.40 migration step. When operation with complex type that inherits other complex type is
          // unwrapped, only child elements are detected for unwrapping.
          if (elements.length == 2 && elements[1].getTerm() instanceof ModelGroup) {
            elements = ((ModelGroup) elements[1].getTerm()).getParticlesArray();
          } else {
            elements = new Particle[0];
          }
        }
        // Traverce the element declarations
        for (int i = 0; i < elements.length; i++) {
          Base term = elements[i].getTerm();
          if (term instanceof ElementDeclaration) {
            ElementDeclaration element = (ElementDeclaration) term;
            TypeDefinitionBase eTypeDef = element.getTypeDefinition();
            
            QName fieldName = new QName(element.getTargetNamespace(), element.getName());
            QName fieldType = getTypeName(eTypeDef);
            boolean isList = false;
            boolean isOptional = false;
            String typeAdapter = null;
            // Find the java type of the unwrapped field.
            String javaType = this.config.getSchemaConfig().getTypeSet().getJavaType(fieldType);
            if (javaType == null || cm.getMaxOccurs() != 1) { // The complex type model group is with changed cardinality so extract the field type information differently
              if (isJaxWS()) {
                QName parentTypeName = getTypeName(complexType);
                SchemaTypeInfo typeInfo = this.config.getSchemaConfig().getTypeSet().get(parentTypeName);
                if (typeInfo != null) {
                  ElementMapping elementMapping = typeInfo.getElementMapping(fieldName);
                  if (elementMapping == null) {
                    throw new ProxyGeneratorException(ProxyGeneratorException.MISSING_SCHEMA_NODE,fieldName.toString());
                  }
                  javaType = elementMapping.getJavaTypeName();
                  if (elementMapping.isListType()) {
                    // @XmlList annotation present
                    isList = true;                    
                  }
                  if (elementMapping.getAdapterClass() != null) {
                    typeAdapter = elementMapping.getAdapterClass();
                  }
                } else {
                  throw new ProxyGeneratorException(ProxyGeneratorException.MISSING_SCHEMA_NODE,parentTypeName.toString());
                }              
              } else {
                throw new ProxyGeneratorException(ProxyGeneratorException.MISSING_SCHEMA_NODE,fieldType.toString());
              }
            } else {
              if (elements[i].getMinOccurs() == 0) {
                isOptional = true;                
                javaType = convertor.wrap(javaType);
              }
              if (element.isNillable()) {
                isOptional = true;
                javaType = convertor.wrap(javaType);
              }            
              if (elements[i].getMaxOccurs() > 1) {                
                if (isJaxWS()) {
                  javaType = "java.util.List<"+convertor.wrap(javaType)+">";
                } else {
                  javaType += "[]";
                  // Fix-up to support unwrap of operations that contain 1 array only.
                  // It will be buggy if the complex type which is unwrapped contains more than one array.
                  String namespace = complexType.getTargetNamespace();
                  String name = null;
                  if (complexType.getName() == null || complexType.getName().length() == 0) {
                    name = DOM.toXPath(complexType.getAssociatedDOMNode());
                  } else {
                    name = complexType.getName();
                  }
                  fieldType = new QName(namespace, name);                
                }
              }            
            }
            if (isJaxWS()) {
              // If the type is nested class, repace the '$' with '.' in class name.
              javaType = javaType.replace('$','.');
            }
            // Support for MTOM "expected content type"
            if (SchemaMappingConstants.SCHEMA_NAMESPACE.equals(fieldType.getNamespaceURI()) && "base64Binary".equals(fieldType.getLocalPart()) && element.getAssociatedDOMNode() != null) {
              Element elementDom = (Element) element.getAssociatedDOMNode();
              String contentTypes = elementDom.getAttributeNS("http://www.w3.org/2005/05/xmlmime","expectedContentTypes");
              if (contentTypes.length() > 0) {
                javaType = getJavaMTOMType(contentTypes);
              }
            }         
            ParameterMapping parameter = new ParameterMapping();
            parameter.setIsElement(false);
            if (isOptional) {
              parameter.setProperty(ParameterMapping.IS_OPTIONAL,"true");
            }
            parameter.setWSDLParameterName(fieldName.getLocalPart());
            parameter.setNamespace(fieldName.getNamespaceURI());      
            parameter.setSchemaQName(fieldType);
            parameter.setJavaType(javaType);
            if (isList) {
              parameter.setProperty(ParameterMapping.IS_PARAMETER_SIMPLE_LIST,"true");
            }
            if (typeAdapter != null) {
              parameter.setProperty(ParameterMapping.VALUE_ADAPTER,typeAdapter);
            }
            result.add(parameter);          
          }
        }
      }            
    }
    return result;
  }
    
  /**
   * Returns list of complex type attributes as parameters. 
   * @param complexType
   * @return
   */
  private ArrayList<ParameterMapping> collectAttributes(ComplexTypeDefinition complexType) {
    ArrayList<ParameterMapping> result = new ArrayList<ParameterMapping>();
    AttributeUse[] attributes = complexType.getAttributeUsesArray();
    for (int i = 0; i < attributes.length; i++) {
      AttributeDeclaration attrib = attributes[i].getAttributeDeclaration();
      QName attribName = new QName(attrib.getTargetNamespace(), attrib.getName());
      QName attribType = getTypeName(attrib.getTypeDefinition());
      String javaType = this.config.getSchemaConfig().getTypeSet().getJavaType(attribType);
      if (attributes[i].isRequired() == false && (attrib.getValueConstraintDefault() == null)) {
        javaType = convertor.wrap(javaType);
      }
      ParameterMapping parameter = new ParameterMapping();
      parameter.setIsElement(false);
      parameter.setWSDLParameterName(attribName.getLocalPart());
      parameter.setNamespace(attribName.getNamespaceURI());      
      parameter.setSchemaQName(attribType);
      parameter.setJavaType(javaType);
      result.add(parameter);
    }    
    return result;
  }

  private QName getTypeName(TypeDefinitionBase type ) {
    String namespace = type.getTargetNamespace();
    String name = type.getName();
    if (name == null || name.length() == 0) {
      name = DOM.toXPath(type.getAssociatedDOMNode());
    } 
    return new QName(namespace,name);
  }
  
  private boolean isJaxWS() {
    if (this.config.getGenerationMode() == ProxyGeneratorConfigNew.JAXWS_MODE || this.config.getGenerationMode() == ProxyGeneratorConfigNew.JAXWS_MODE_LOAD
        || this.config.getGenerationMode() == ProxyGeneratorConfigNew.JAXWS_MODE_SERVER) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns the fields of some element declaration and their types.
   * 
   * @param elementName
   * @param elements
   * @param elementTypes
   */
  private void getSequenceFields(TypeDefinitionBase type, ArrayList<ParameterMapping> attributes, ArrayList<ParameterMapping> elements) throws ProxyGeneratorException {
    if (type instanceof ComplexTypeDefinition) {
      ComplexTypeDefinition complexType = (ComplexTypeDefinition) type;
      attributes.addAll(collectAttributes(complexType));
      elements.addAll(collectFields(complexType, (config.getGenerationMode() == ProxyGeneratorConfigNew.MIGRATION_MODE)));
    }
  }
  
  private ArrayList<ParameterMapping> getSequenceFields(QName elementName) throws ProxyGeneratorException {
    Schema schema = config.getSchemaConfig().getSchema();
    ElementDeclaration element = schema.getTopLevelElementDeclaration(elementName.getNamespaceURI(), elementName.getLocalPart());
    TypeDefinitionBase type = element.getTypeDefinition();
    ArrayList<ParameterMapping> result = new ArrayList<ParameterMapping>();
    if (type instanceof ComplexTypeDefinition) {
      ComplexTypeDefinition complexType = (ComplexTypeDefinition) type;
      result.addAll(collectAttributes(complexType));
      result.addAll(collectFields(complexType, (config.getGenerationMode() == ProxyGeneratorConfigNew.MIGRATION_MODE)));
    }
    return result;    
  }

  /**
   * Returns true if the wrapper element is proper element declaration.
   * 
   * @param elementName
   * @return
   */
  private boolean isProperSequenceElement(QName elementName) {
    Schema schema = config.getSchemaConfig().getSchema();
    ElementDeclaration element = schema.getTopLevelElementDeclaration(elementName.getNamespaceURI(), elementName.getLocalPart());
    TypeDefinitionBase type = element.getTypeDefinition();
    if (type instanceof ComplexTypeDefinition && type.isBuiltIn() == false) {
      ComplexTypeDefinition complexType = (ComplexTypeDefinition) type;
      if (complexType.getContentTypeSimpleTypeDefinition() != null) {
        return false; // Complex type with simple content
      }
      if (complexType.isAbstract()) {
        return false;
      }
      AttributeUse[] aus = complexType.getAttributeUsesArray();
      if (aus.length > 0) {
        return false;
      }
      // the content of complex type must be sequence with elements
      Particle cm = complexType.getContentTypeContentModel();
      if (cm != null && cm.getTerm() instanceof ModelGroup) {
        ModelGroup mg = (ModelGroup) cm.getTerm();
        //boolean flag = true;
        if (mg.isCompositorSequence()) {
          // Parameters that contain array and other nodes are not unwrapped
          Particle[] elements = mg.getParticlesArray();
          for (int i = 0; i < elements.length; i++) {
            Base term = elements[i].getTerm();
            if (!(term instanceof ElementDeclaration) || (elements[i].getMaxOccurs() > 1 && !isJaxWS() && elements.length > 1)) {
              return false;
          }
          }
          return true;
        }
      }
      if (cm == null) { // Empty Complex types does not have content model.
        return true;
      }
    }
    return false;
  }
  
  /**
   * Returns true if the operation possible to be unwrapped.
   * 
   * @param operation
   * @return
   */
  private boolean isDocumentStylePossible(Operation operation) {
    // For Dynamic Proxy and WS DAS there is no unwrapping allowed.    
    if (config.getGenerationMode() == ProxyGeneratorConfigNew.GENERIC_MODE  ||
        config.getGenerationMode() == ProxyGeneratorConfigNew.GENERIC_MODE_SDO) {
      return false;
    }
    ObjectList inParams = operation.getParameters(Parameter.IN);
    ObjectList outParams = operation.getParameters(Parameter.OUT);
    ObjectList inoutParams = operation.getParameters(Parameter.INOUT);
    ObjectList resultParams = operation.getParameters(Parameter.RETURN);
    // Operation with 1 input parameter and 1 return parameter
    if (inParams.getLength() == 1 && outParams.getLength() == 0 && resultParams.getLength() == 1 && inoutParams.getLength() == 0) {
      Parameter inParam = (Parameter) inParams.item(0);
      Parameter outParam = (Parameter) resultParams.item(0);
      XSDRef inType = inParam.getXSDTypeRef();
      XSDRef outType = outParam.getXSDTypeRef();
      boolean  requestOk = false;
      boolean  responseOk = false;
      if (inType.getXSDType() == XSDRef.ELEMENT && outType.getXSDType() == XSDRef.ELEMENT
          && inType.getQName().getLocalPart().equals(operation.getName())) {
        if (isProperSequenceElement(inType.getQName())) {
          requestOk = true;
        }
        if (isProperSequenceElement(outType.getQName())) {
          responseOk = true;
        }
      }
      if (requestOk && responseOk) {
        return true;
      }
    }
    // Operation with 1 input parameter and no return
    if (inParams.getLength() == 1 && outParams.getLength() == 0 && resultParams.getLength() == 0 && inoutParams.getLength() == 0) {
      Parameter inParam = (Parameter) inParams.item(0);
      XSDRef inType = inParam.getXSDTypeRef();
      if (inType.getXSDType() == XSDRef.ELEMENT && inType.getQName().getLocalPart().equals(operation.getName())) {
        if (isProperSequenceElement(inType.getQName())) {
          return true;
        }
      }
    }
    // Operation with one inout parameter
    if (inParams.getLength() == 0 && outParams.getLength() == 0 && resultParams.getLength() == 0 && inoutParams.getLength() == 1) {
      Parameter inoutParam = (Parameter) inoutParams.item(0);
      XSDRef inoutType = inoutParam.getXSDTypeRef();
      if (inoutType.getXSDType() == XSDRef.ELEMENT && inoutType.getQName().getLocalPart().equals(operation.getName())) {
        if (isProperSequenceElement(inoutType.getQName())) {
          return true;
        }
      }

    }
    return false;
  }

  /*
   * Return true if an operation is document style using the 640 rules. Used in
   * migration mode. Logic is copied from ClassGenerator.isDocumentStype()
   */
  private boolean isDocumentStylePossible640(Operation op) {

    ObjectList inparams = op.getParameters(Parameter.IN);
    ObjectList outparams = op.getParameters(Parameter.RETURN | Parameter.OUT | Parameter.INOUT);

    // not doc-style if 0 input or output params
    if (inparams.getLength() == 0 || outparams.getLength() == 0) {
      return false;
    }
    if (inparams.getLength() > 1) {
      return false;
    }
    Parameter inp = (Parameter) inparams.item(0);
    Parameter outp = (Parameter) outparams.item(0);

    if (inp.getXSDTypeRef() == null || outp.getXSDTypeRef() == null) {
      return false;
    }

    // must be element, not type
    if (inp.getXSDTypeRef().getXSDType() == XSDRef.TYPE || outp.getXSDTypeRef().getXSDType() == XSDRef.TYPE) {
      return false;
    }

    Schema sch = config.getSchemaConfig().getSchema();
    ElementDeclaration inElem = sch.getTopLevelElementDeclaration(inp.getXSDTypeRef().getQName().getNamespaceURI(), inp.getXSDTypeRef().getQName()
        .getLocalPart());

    TypeDefinitionBase inType = inElem.getTypeDefinition();
    ElementDeclaration outElem = sch.getTopLevelElementDeclaration(outp.getXSDTypeRef().getQName().getNamespaceURI(), outp.getXSDTypeRef().getQName()
        .getLocalPart());
    TypeDefinitionBase outType = outElem.getTypeDefinition();

    // input and output must be complex types
    if (!(inType instanceof ComplexTypeDefinition) || !(outType instanceof ComplexTypeDefinition)) {
      return false;
    }

    // special .NET style array handling - 640 specific
    String outJavaType = config.getSchemaConfig().getTypeSet().getJavaType(outp.getXSDTypeRef().getQName());
    String inJavaType = config.getSchemaConfig().getTypeSet().getJavaType(inp.getXSDTypeRef().getQName());
    if ((outJavaType != null && outJavaType.endsWith("[]")) || (inJavaType != null && inJavaType.endsWith("[]"))) {
      return false;
    }

    ComplexTypeDefinition inC = (ComplexTypeDefinition) inType;
    ComplexTypeDefinition outC = (ComplexTypeDefinition) outType;
    // must not have attributes!
    if (inC.getAttributeUsesArray().length > 0 || outC.getAttributeUsesArray().length > 0) {
      return false;
    }

    Particle inCm = inC.getContentTypeContentModel();
    Particle outCm = outC.getContentTypeContentModel();

    if (inCm != null && inCm.getTerm() instanceof ModelGroup) {
      ModelGroup mg = (ModelGroup) inCm.getTerm();

      if (mg.isCompositorSequence() || mg.isCompositorAll()) {

        Particle[] particles = mg.getParticlesArray();

        // must make sure we don't unwrap this special case - complextype name
        // same as particle name
        if (particles.length == 1) {
          QName elQname = new QName(((ElementDeclaration) particles[0].getTerm()).getTargetNamespace(), ((ElementDeclaration) particles[0].getTerm())
              .getName());
          QName typeQname = new QName(inC.getTargetNamespace(), inC.getName());

          if (typeQname.equals(elQname)) {
            return false;
          }
        }

        // abstract type handling
        if (inC.getBaseTypeDefinition() instanceof ComplexTypeDefinition && !((ComplexTypeDefinition) inC.getBaseTypeDefinition()).isBuiltIn()) {

          if (particles.length == 2 && particles[0].getTerm() instanceof ModelGroup && particles[1].getTerm() instanceof ModelGroup) {

            ModelGroup concreteMg = (ModelGroup) particles[1].getTerm();
            Particle[] concretes = concreteMg.getParticlesArray();

            for (int i = 0; i < concretes.length; ++i) {
              if (!(concretes[i].getTerm() instanceof ElementDeclaration)) {
                return false;
              }

            }
          }
        } else {

          // all particles in the <xsd:sequence> or <xsd:all> must be element
          // declarations
          // complex handling due to abstract types/inheritance
          for (int i = 0; i < particles.length; ++i) {

            if (!(particles[i].getTerm() instanceof ElementDeclaration)) {
              return false;
            }
          }
        }
      }
    }

    if (outCm != null && outCm.getTerm() instanceof ModelGroup) {

      ModelGroup mg = (ModelGroup) outCm.getTerm();
      if (mg.isCompositorSequence() || mg.isCompositorAll()) {

        if (outC.getBaseTypeDefinition() instanceof ComplexTypeDefinition && !((ComplexTypeDefinition) outC).getBaseTypeDefinition().isBuiltIn()) {
          Particle[] all = mg.getParticlesArray();
          if (all.length == 2 && all[0].getTerm() instanceof ModelGroup && all[1].getTerm() instanceof ModelGroup) {
            ModelGroup concretes = (ModelGroup) all[1].getTerm();
            Particle[] elems = concretes.getParticlesArray();
            if (elems.length != 0) {
              if (elems.length > 1)
                return false;
              else if (!(elems[0].getTerm() instanceof ElementDeclaration))
                return false;
            }
          }
        } else // first particle must be element
        if (mg.getParticlesArray().length != 0) {
          if (mg.getParticlesArray().length > 1) {
            return false;
          }
          Particle p = mg.getParticlesArray()[0];
          if (!(p.getTerm() instanceof ElementDeclaration)) {
            return false;
          }
        }
      }

    }

    return true;
  }

  /**
   * Writes package declaration.
   */
  private void writePackage(String packageName, CodeGenerator generator) {
    // Package level documentation
    if (isJaxWS()) {
      String jDoc = extendedDefinitions.getJavaDoc();
      if (jDoc != null) {
        generator.addLine(jDoc);
      }
    }
    if (packageName != null && packageName.length() != 0) {
      generator.addLine("package " + packageName + ";");
    }

  }

  /**
   * Returns path to directory derived from java package name.
   * 
   * @param packageName
   * @return
   */
  private File getPackageDir(String packageName) {
    String relativePath = convertor.packageToPath(packageName);
    File outputDir = new File(config.getOutputPath());
    File outputPath = new File(outputDir, relativePath);
    return outputPath;
  }

  /**
   * Returns true if some fault parameter is already added to the list.
   * 
   * @param holders
   * @param parameter
   * @return
   */
  private boolean containsException(ArrayList exceptions, ParameterMapping parameter) {
    for (int i = 0; i < exceptions.size(); i++) {
      ParameterMapping pm = (ParameterMapping) exceptions.get(i);
      if (pm.getJavaType().equals(parameter.getJavaType())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true if some holder parameter is already added to the list.
   * 
   * @param holders
   * @param parameter
   * @return
   */
  private boolean containsHolder(ArrayList holders, ParameterMapping parameter) {
    for (int i = 0; i < holders.size(); i++) {
      ParameterMapping pm = (ParameterMapping) holders.get(i);
      if (pm.getHolderName().equals(parameter.getHolderName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Generates Exception classes.
   * 
   * @throws ProxyGeneratorException
   */
  private void generateExceptions() throws ProxyGeneratorException {
    InterfaceMapping[] iMappings = config.getMappingRules().getInterface();
    ArrayList exceptionList = new ArrayList();
    for (int i = 0; i < iMappings.length; i++) {
      OperationMapping[] operations = iMappings[i].getOperation();
      for (int j = 0; j < operations.length; j++) {
        ParameterMapping[] params = operations[j].getParameter();
        for (int k = 0; k < params.length; k++) {
          if (params[k].getParameterType() == ParameterMapping.FAULT_TYPE) {
            if (!containsException(exceptionList, params[k])) {
              exceptionList.add(params[k]);
              generateException(params[k], operations[j], iMappings[i]);
            }
          }
        }
      }
    }
    // for (int i=0; i<exceptionList.size(); i++) {
    // ParameterMapping parameter = (ParameterMapping) exceptionList.get(i);
    // generateException(parameter);
    // }
  }

  /**
   * Checks exception names for name collisions.
   * 
   * @param exceptionName
   * @return
   */
  private String checkExceptionName(String exceptionName) {
    // Check exception name for collision with the generated types.
    SchemaTypeSet typeSet = this.config.getSchemaConfig().getTypeSet();
    Enumeration enumTypes = typeSet.getSchemaTypes();
    while (enumTypes.hasMoreElements()) {
      QName qname = (QName) enumTypes.nextElement();
      String className = typeSet.getJavaType(qname);
      if (exceptionName.equals(className)) {
        exceptionName += "_Exception";
        return exceptionName;
      }
    }
    // TODO: Might brake with multiple interfaces
    MappingRules mappings = config.getMappingRules();
    InterfaceMapping[] interfaces = mappings.getInterface();
    for (InterfaceMapping temp : interfaces ) {
      if (exceptionName.equals(temp.getSEIName())) {
        exceptionName += "_Exception";
        return exceptionName;
      }
    }
    return exceptionName;
  }

  /**
   * Generates exception for fault parameters.
   * 
   * @param parameter
   */
  private void generateException(ParameterMapping pMapping, OperationMapping oMapping, InterfaceMapping iMapping) throws ProxyGeneratorException {
    String[] parts = null;
    String[] javaTypes = null;
    String[] varNames = null;
    if (pMapping.getFaultConstructorParamOrder() != null) {
      // Complex content
      StringTokenizer partTokenizer = new StringTokenizer(pMapping.getFaultConstructorParamOrder(), " ", false);
      StringTokenizer typeTokenizer = new StringTokenizer(pMapping.getProperty(ParameterMapping.FAULT_CONSTRUCTOR_PARAM_TYPES), " ", false);
      parts = new String[partTokenizer.countTokens()];
      javaTypes = new String[typeTokenizer.countTokens()];
      varNames = new String[parts.length];
      int index = 0;
      while (partTokenizer.hasMoreTokens()) {
        String nextPart = partTokenizer.nextToken();
        String nextType = typeTokenizer.nextToken();
        parts[index] = nextPart;
        javaTypes[index] = nextType;
        varNames[index] = convertor.attributeToIdentifier(parts[index]);
        index++;
      }
    } else {
      // Simple content
      SchemaTypeSet typeSet = config.getSchemaConfig().getTypeSet();
      parts = new String[1];
      javaTypes = new String[1];
      varNames = new String[1];
      parts[0] = pMapping.getFaultElementQName().getLocalPart();
      javaTypes[0] = typeSet.getJavaType(pMapping.getSchemaQName());
      varNames[0] = convertor.attributeToIdentifier(parts[0]);
    }
    String javaClassName = pMapping.getJavaType();
    String packageName = convertor.getPackage(javaClassName);
    String className = convertor.getLocalClass(javaClassName);

    generator.clear();
    writePackage(packageName, generator);
    generator.addLine();
    if (pMapping.getJavaDoc() != null) {
      generator.add(pMapping.getJavaDoc());
      generator.addNewLine();
    } else {
      generator.addLine("/**");
      generator.addLine(" * Exception class for service fault.");
      generator.addLine(" */");
    }    
    if (isJaxWS()) {
      QName faultQName = pMapping.getFaultElementQName();
      String javaContentType = config.getSchemaConfig().getTypeSet().getJavaType(pMapping.getSchemaQName());
      generator.addLine("@javax.xml.ws.WebFault(name = " + packString(faultQName.getLocalPart()) + ", targetNamespace = "
          + packString(faultQName.getNamespaceURI()) + ", " + "faultBean = " + packString(javaContentType) + ")");
      String pvtMember = "_" + className;
      generator.addLine("public class " + className + " extends java.lang.Exception {");
      generator.startSection();
      generator.addNewLine();
      generator.addLine("private " + javaContentType + " " + pvtMember + ";");
      generator.addNewLine();

      // constructor
      generator.addLine("public " + className + "(String message, " + javaContentType + " faultInfo){");
      generator.startSection();
      generator.addLine("super(message);");
      generator.addLine("this." + pvtMember + " = faultInfo;");
      generator.endSection();
      generator.addLine("}");
      generator.addNewLine();

      // constructor
      generator.addLine("public " + className + "(String message, " + javaContentType + " faultInfo, Throwable cause){");
      generator.startSection();
      generator.addLine("super(message, cause);");
      generator.addLine("this." + pvtMember + " = faultInfo;");
      generator.endSection();
      generator.addLine("}");
      generator.addNewLine();

      // get fault info
      generator.addLine("public " + javaContentType + " getFaultInfo(){");
      generator.startSection();
      generator.addLine("return " + "this." + pvtMember + ";");
      generator.endSection();
      generator.addLine("}");
      generator.addNewLine();

      // wrap class up
      generator.endSection();
      generator.addLine("}");
      outputJavaFile(packageName + "." + className, generator);

    } else {
      generator.addLine("public class " + className + " extends java.lang.Exception {");
      generator.startSection();
      generator.addLine();
      StringBuffer constructorParams = new StringBuffer();
      for (int i = 0; i < parts.length; i++) {
        generator.addLine("private " + javaTypes[i] + " " + varNames[i] + ";");
        if (i > 0) {
          constructorParams.append(", ");
        }
        constructorParams.append(javaTypes[i]);
        constructorParams.append(" ");
        constructorParams.append(varNames[i]);
      }
      generator.addLine();
      generator.addLine("public " + className + "(" + constructorParams.toString() + ") {");
      generator.startSection();
      for (int i = 0; i < javaTypes.length; i++) {
        generator.addLine("this." + varNames[i] + " = " + varNames[i] + ";");
      }
      generator.endSection();
      generator.addLine("}");
      generator.addLine();
      for (int i = 0; i < javaTypes.length; i++) {
        String methodName = "get" + convertor.attributeToClassName(parts[i]);
        if (methodName.equals("getMessage")) {
          methodName += "Internal";
        }
        generator.addLine("public " + javaTypes[i] + " " + methodName + "() {");
        generator.addLine("  return this." + varNames[i] + ";");
        generator.addLine("}");
        generator.addLine();
      }
      generator.endSection();
      generator.addLine("}");
      outputJavaFile(javaClassName, generator);
    }
  }

  /**
   * Generates holder classes.
   * 
   * @param iMappings
   */
  private void generateHolders() throws ProxyGeneratorException {
    InterfaceMapping[] iMappings = config.getMappingRules().getInterface();
    ArrayList holderList = new ArrayList();
    for (int i = 0; i < iMappings.length; i++) {
      OperationMapping[] operations = iMappings[i].getOperation();
      for (int j = 0; j < operations.length; j++) {
        ParameterMapping[] params = operations[j].getParameter();
        for (int k = 0; k < params.length; k++) {
          if (params[k].getParameterType() == ParameterMapping.OUT_TYPE || params[k].getParameterType() == ParameterMapping.IN_OUT_TYPE) {
            if (!convertor.isBuildInHolder(params[k].getHolderName())) {
              if (!containsHolder(holderList, params[k]) && !isJaxWS()) {
                holderList.add(params[k]);
                generateHolder(params[k]);
              }
            }
          }
        }
      }
    }
    // for (int i=0; i<holderList.size(); i++) {
    // ParameterMapping parameter = (ParameterMapping) holderList.get(i);
    // generateHolder(parameter);
    // }
  }

  /**
   * Generates holder class for specific WS parameter.
   * 
   * @param pMapping
   * @throws ProxyGeneratorException
   */
  private void generateHolder(ParameterMapping pMapping) throws ProxyGeneratorException {
    String javaClassName = pMapping.getHolderName();
    generator.clear();
    String packageName = convertor.getPackage(javaClassName);
    String className = convertor.getLocalClass(javaClassName);
    writePackage(packageName, generator);
    generator.addLine();
    generator.addLine("/**");
    generator.addLine(" * Holder class for java class [" + pMapping.getJavaType() + "]");
    generator.addLine(" */");
    generator.addLine("public class " + className + " implements javax.xml.rpc.holders.Holder {");
    generator.startSection();
    generator.addLine();
    generator.addLine("public " + pMapping.getJavaType() + " value;");
    generator.addLine();
    generator.addLine("public " + className + "() {");
    generator.addLine("}");
    generator.addLine();
    generator.addLine("public " + className + "(" + pMapping.getJavaType() + " value) {");
    generator.addLine("  this.value = value;");
    generator.addLine("}");
    generator.endSection();
    generator.addLine("}");
    outputJavaFile(pMapping.getHolderName(), generator);
  }

  /**
   * Detects collisions between wsdl service elements and wsdl porttypes that
   * have the same names. This would lead to java file clobbering. This is
   * handled by appending _Service to the Service Interface name and _PortType
   * to the Service Endpoint Interface name
   * @throws ProxyGeneratorException 
   * 
   */
  private void detectCollisions() throws ProxyGeneratorException {

    ServiceMapping[] smaps = config.getMappingRules().getService();
    for (int i = 0; i < smaps.length; ++i) {

      InterfaceMapping[] intMaps = config.getMappingRules().getInterface();

      for (int j = 0; j < intMaps.length; ++j) {

        String SEIName = intMaps[j].getSEIName();
        // check the name of the interface.
        checkSEIName(SEIName);
        // check the name of the operations in it.
        checkOperationsNames(intMaps[j]);
        
        String serviceName = smaps[i].getSIName();

        if (SEIName.equalsIgnoreCase(serviceName)) {
          smaps[i].setSIName(serviceName + "_Service");
        }
      }
    }
  }
  
  
  /**
   * Check the name of the operation in a given interface.
   * They should match the java codelines.
   * 
   * @param mapping
   * @throws ProxyGeneratorException
   */
  private void checkOperationsNames(InterfaceMapping mapping) throws ProxyGeneratorException{
    OperationMapping[] opMappings = mapping.getOperation();
    
    for (OperationMapping currentMapping : opMappings){  
      String methodName = currentMapping.getJavaMethodName();
      String testLeading  = (String)methodName.subSequence(0,1);
      
      if (testLeading.matches("\\d")){
        throw new ProxyGeneratorException(ProxyGeneratorException.WRONG_METHOD_NAME, new Object[]{methodName, mapping.getSEIName()});
      }               
    }        
  }
  
  /**
   * Check if interface name. It should not start with a digit to match the
   * java naming conventions.
   * @param qualifiedName
   * @throws ProxyGeneratorException
   */
  private void checkSEIName(String qualifiedName) throws ProxyGeneratorException{
    
    String interfaceName = convertor.getLocalClass(qualifiedName);
              
    String testLeading  = (String) interfaceName.subSequence(0,1);
    
    if (testLeading.matches("\\d")){
      throw new ProxyGeneratorException(ProxyGeneratorException.WRONG_INTERFACE_NAME, new Object[]{interfaceName});
    }   
  }    
  

  /**
   * 
   * @param sMapping
   * @throws ProxyGeneratorException
   */
  private void generateService(ServiceMapping sMapping) throws ProxyGeneratorException {
    generator.clear();
    String className = sMapping.getSIName();
    String packageName = convertor.getPackage(className);
    String interfaceName = convertor.getLocalClass(className);
    if (javax.xml.rpc.Service.class.getName().equals(className)) {
      // The service is generated service interface
      return;
    }
    writePackage(packageName, generator);
    generator.addLine();
    generator.addLine("/**");
    generator.addLine(" * Service Interface (generated by SAP WSDL to Java generator).");
    generator.addLine(" */");

    String toExtend = "javax.xml.rpc.Service";
    String toThrow = "javax.xml.rpc.ServiceException";

    generator.addLine("public interface " + interfaceName + " extends " + toExtend + " {");
    generator.addLine();
    generator.startSection();
    EndpointMapping[] endpoints = sMapping.getEndpoint();
    for (int i = 0; i < endpoints.length; i++) {
      QName bindingName = endpoints[i].getPortBinding();
      QName pTypeName = endpoints[i].getPortPortType();
      String javaName = endpoints[i].getPortJavaName();
      InterfaceMapping iMapping = config.getMappingRules().getInterface(pTypeName, bindingName);
      String seiName = iMapping.getSEIName();
      if (isJaxWS()) {
        ExtendedService extServ = extendedDefinitions.getExtendedService(sMapping.getServiceName());
        ExtendedEndpoint extEP = (ExtendedEndpoint) extServ.getExtendedChild(new QName(endpoints[i].getPortQName()));
        String jDoc = extEP.getJavaDoc();
        if (jDoc != null) {
          generator.addLine(jDoc);
        }

        generator.addLine("@javax.xml.ws.WebEndpoint(name = " + packString(endpoints[i].getPortQName()) + ")");
      }
      generator.addLine("public " + seiName + " get" + javaName + "() throws " + toThrow + ";");
      generator.addLine();
    }
    // Compatibility methods
    generator.addLine();
    generator.addLine("public " + Remote.class.getName() + " getLogicalPort(java.lang.Class seiClass) throws " + ServiceException.class.getName()
        + ";");
    generator.addLine();
    generator.addLine("public " + Remote.class.getName() + " getLogicalPort(java.lang.String portName, java.lang.Class className) throws "
        + ServiceException.class.getName() + ";");
    generator.addLine();
    generator.addLine("public " + String.class.getName() + "[] getLogicalPortNames();");
    generator.addLine();
    MappingRules mappingRules = this.config.getMappingRules();
    InterfaceMapping[] interfaceMappings = mappingRules.getInterface();
    boolean commonInterface = true;
    for (int i = 0; i < interfaceMappings.length; i++) {
      if (!interfaceMappings[i].getSEIName().equals(interfaceMappings[0].getSEIName())) {
        commonInterface = false;
        break;
      }
    }
    if (interfaceMappings.length == 0) {
      commonInterface = false;
    }
    if (commonInterface) {
      String seiName = interfaceMappings[0].getSEIName();
      generator.addLine("public " + seiName + " getLogicalPort() throws " + ServiceException.class.getName() + ";");
      generator.addLine();
      generator.addLine("public " + seiName + " getLogicalPort(" + String.class.getName() + " portName) throws " + ServiceException.class.getName()
          + ";");
      generator.addLine();
    }
    generator.endSection();
    generator.addLine("}");
    outputJavaFile(className, generator);
  }

  /**
   * Returns reordered parameters.
   * 
   * @param inputParameters
   * @return
   */
  private ParameterMapping[] reorderParameters(String operationName, ParameterMapping[] inputParameters) throws ProxyGeneratorException {
    if (inputParameters.length == 0) {
      return inputParameters;
    }
    if (inputParameters.length > 0 && inputParameters[0].getPosition() == -1) {
      return inputParameters;
    }
    ParameterMapping[] result = new ParameterMapping[inputParameters.length];
    for (int i = 0; i < inputParameters.length; i++) {
      if (inputParameters[i].getPosition() == -1) {
        throw new ProxyGeneratorException(ProxyGeneratorException.PARAMETER_POS_MISSING, inputParameters[i].getWSDLParameterName(), operationName);
      }
      result[inputParameters[i].getPosition()] = inputParameters[i];
    }
    for (int i = 0; i < result.length; i++) {
      if (result[i] == null) {
        throw new ProxyGeneratorException(ProxyGeneratorException.PARAMETERS_SCRAMBLED, operationName);
      }
    }
    return result;
  }
  
  private boolean isDocumentStyle(OperationMapping oMapping) {
    boolean isDocument = false;
    if ("document".equals(oMapping.getProperty(OperationMapping.OPERATION_STYLE))) {
      isDocument = true;
    }        
    if ("rpc".equals(oMapping.getProperty(OperationMapping.OPERATION_STYLE))) {
      if (oMapping.getProperty(OperationMapping.REQUEST_WRAPPER_BEAN) != null) {
        // This is unwrapped document operation not bare
        isDocument = true;            
      } else {
        isDocument = false;
      }
    }
    return isDocument;
  }
  
  private boolean isDocumentBare(OperationMapping oMapping) {
    boolean isBare = false;
    if ("document".equals(oMapping.getProperty(OperationMapping.OPERATION_STYLE))) {
      isBare = true;
    }        
    return isBare;
  }
  
  private String getWebParamAnnotation(boolean isDocument, boolean isBare, ParameterMapping pMapping) {
    String webParamName = pMapping.getWSDLParameterName();
    if (pMapping.isElement()) {
      webParamName = pMapping.getSchemaQName().getLocalPart();
    }
    String targetNamespace = null;
    if (pMapping.isElement()) {
      targetNamespace = pMapping.getSchemaQName().getNamespaceURI();
      if (targetNamespace == null) {
        targetNamespace = "";
      }
    } else {
      if (isDocument) {
        targetNamespace = pMapping.getNamespace();
        if (targetNamespace == null) {
          targetNamespace = "";
        }            
      }
    }
    
    String partName = null;
    if (isDocument == false || (isDocument == true && isBare == true)) {
      partName = pMapping.getWSDLParameterName();
    }
    StringBuffer line = new StringBuffer("@javax.jws.WebParam(name = " + packString(webParamName));
    if (targetNamespace != null) {
      line.append(", targetNamespace = " + packString(targetNamespace));
    }
    if (partName != null) {
      line.append(", partName = " + packString(partName));
    }
    if (pMapping.isHeader()) {
      line.append(", header = " + pMapping.isHeader());
    }
    switch (pMapping.getParameterType()) {
      case ParameterMapping.IN_OUT_TYPE: {
        line.append(", mode = javax.jws.WebParam.Mode.INOUT) ");
        break;
      }
      case ParameterMapping.OUT_TYPE: {
        line.append(", mode = javax.jws.WebParam.Mode.OUT) ");
        break;
      }
      case ParameterMapping.IN_TYPE: {
        line.append(") ");
        break;
      }
    }
    if (pMapping.getProperty(ParameterMapping.IS_PARAMETER_SIMPLE_LIST) != null) {
      line.append("@javax.xml.bind.annotation.XmlList ");
    }
    if (pMapping.getProperty(ParameterMapping.VALUE_ADAPTER) != null) {
      line.append("@javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter("+pMapping.getProperty(ParameterMapping.VALUE_ADAPTER)+".class) ");
    }            
    return line.toString();
  }

  /**
   * Returns Web method Java Header.
   * 
   * @param generator
   * @param oMapping
   */
  private String getMethodHeader(OperationMapping oMapping) throws ProxyGeneratorException {
    ParameterMapping[] resultParams = oMapping.getParameters(ParameterMapping.RETURN_TYPE);
    //ParameterMapping[] inoutParams = oMapping.getParameters(ParameterMapping.IN_TYPE + ParameterMapping.IN_OUT_TYPE);
    //ParameterMapping[] outParams = oMapping.getParameters(ParameterMapping.OUT_TYPE);
    ParameterMapping[] faultParams = oMapping.getParameters(ParameterMapping.FAULT_TYPE);
    // operation reordered input parameters
    ParameterMapping[] operationParams = oMapping.getParameters(ParameterMapping.IN_TYPE + ParameterMapping.IN_OUT_TYPE+ParameterMapping.OUT_TYPE);
    /*
    ParameterMapping[] operationParams = new ParameterMapping[inoutParams.length + outParams.length];
    for (int i = 0; i < inoutParams.length; i++) {
      operationParams[i] = inoutParams[i];
    }
    for (int i = 0; i < outParams.length; i++) {
      operationParams[inoutParams.length + i] = outParams[i];
    }*/
    operationParams = reorderParameters(oMapping.getWSDLOperationName(), operationParams);
    StringBuffer result = new StringBuffer();
    String returnType = "void";
    if (resultParams.length == 1) {
      returnType = resultParams[0].getJavaType();
    }
    result.append(returnType + " " + oMapping.getJavaMethodName() + "(");
    boolean isDocument = isDocumentStyle(oMapping);
    boolean isBare = isDocumentBare(oMapping);    
    boolean beginFlag = true;
    for (int j = 0; j < operationParams.length; j++) {
      if (beginFlag == false) {
        result.append(", ");
      }
      if (isJaxWS()) {
        String webParamAnnotation = getWebParamAnnotation(isDocument,isBare,operationParams[j]);        
        result.append(webParamAnnotation);
      }
      switch (operationParams[j].getParameterType()) {
        case ParameterMapping.IN_OUT_TYPE: // $JL-SWITCH$ (case falls through on purpose)
        case ParameterMapping.OUT_TYPE: {
          String javaVarName = convertor.attributeToIdentifier(operationParams[j].getWSDLParameterName());
          if (operationParams[j].getJavaParamName() != null) {
            javaVarName = operationParams[j].getJavaParamName();
          }
          result.append(operationParams[j].getHolderName() + " " + javaVarName);
          beginFlag = false;
          break;
        }
        case ParameterMapping.IN_TYPE: {
          String javaVarName = convertor.attributeToIdentifier(operationParams[j].getWSDLParameterName());
          if (operationParams[j].getJavaParamName() != null) {
            javaVarName = operationParams[j].getJavaParamName();
          }
          result.append(operationParams[j].getJavaType() + " " + javaVarName);
          beginFlag = false;
          break;
        }
      }
    }
    result.append(")");
    boolean appendComma = false;
    if (!isJaxWS()) {
      result.append(" throws java.rmi.RemoteException");
      appendComma = true;
    }
    for (int j = 0; j < faultParams.length; j++) {
      if (appendComma) {
        result.append(",");        
      } else {
        result.append(" throws ");
      }
      result.append(faultParams[j].getJavaType());
      appendComma = true;
    }

    return result.toString();
  }

  /**
   * Packs string to be inserted into a generated file.
   * 
   * @param str
   * @return
   */
  private String packString(String str) {
    if (str == null) {
      return "null";
    } else {
      StringBuffer res = new StringBuffer();
      res.append("\"");
      for (int i = 0; i < str.length(); i++) {
        char x = str.charAt(i);
        switch (x) {
          case '\\': {
            res.append("\\\\");
            break;
          }
          case '\n': {
            res.append("\\n");
            break;
          }
          case '\r': {
            res.append("\\r");
            break;
          }
          case '\"': {
            res.append("\\\"");
            break;
          }
          default: {
            res.append(x);
          }
        }
      }
      res.append("\"");
      return res.toString();
    }
  }

  /**
   * Generates Service implementation.
   * 
   * @param sMapping
   * @throws ProxyGeneratorException
   */
  private void generateServiceImpl(ServiceMapping sMapping, String packageName, String mappingPath, String configurationPath, String typesPath, boolean standalone) throws ProxyGeneratorException {
    QName serviceName = sMapping.getServiceName();
    ImplementationLink iLink = sMapping.getImplementationLink();
    if (iLink == null) {
      iLink = new ImplementationLink();
      sMapping.setImplementationLink(iLink);
    }
    String interfaceName = convertor.getLocalClass(sMapping.getSIName());
    String className = interfaceName + "Impl";
    String baseClass = DynamicServiceImpl.class.getName();
    if (isJaxWS()) {
       className = interfaceName;
      baseClass = javax.xml.ws.Service.class.getName();
    }

    iLink.setSIImplName(getNameWithPackage(className, packageName));
    generator.clear();
    writePackage(packageName, generator);
    generator.addLine();
    if (sMapping.getJavaDoc() != null) {
      generator.add(sMapping.getJavaDoc());
      generator.addNewLine();
    } else {
      generator.addLine("/**");
      generator.addLine(" * Service implementation of {" + sMapping.getServiceName().getLocalPart() + "} (generated by SAP WSDL to Java generator).");
      generator.addLine(" */");
    }
    String serializable = "";
    if (this.config.isGenerateSerializable()) {
      serializable = Serializable.class.getName() + ",";
    }
    QName serviceQName = sMapping.getServiceName();
    String wsdlLocation = config.getWsdlPath();
    if (wsdlLocation != null) {
      try {
        // Try to convert to valid url.
        URL wsdlURL = URLLoader.fileOrURLToURL(null, wsdlLocation);
        wsdlLocation = wsdlURL.toExternalForm();
      } catch (Exception x) {
        wsdlLocation = wsdlLocation;
      }
    }
    if (config.getJaxWSWSDLLocation() != null) {
      wsdlLocation = config.getJaxWSWSDLLocation();
    }    
    if (isJaxWS()) {      
      if (this.config.getJaxWSFiles().getHandlerConfigurations().size() == 1) {
        String fileName = convertor.getLocalClass(sMapping.getSIName()) +"_handler.xml";
        generator.addLine("@javax.jws.HandlerChain(file ="+packString(fileName)+")");
        outputXMLHandlerFile(sMapping.getSIName(),fileName,this.config.getJaxWSFiles().getHandlerConfigurations().get(0));
      }
      
      if (wsdlLocation == null) {
        generator.addLine("@javax.xml.ws.WebServiceClient(name = " + packString(serviceQName.getLocalPart()) + ", targetNamespace = "
            + packString(serviceQName.getNamespaceURI())+ ")");        
      } else {
        generator.addLine("@javax.xml.ws.WebServiceClient(name = " + packString(serviceQName.getLocalPart()) + ", targetNamespace = "
          + packString(serviceQName.getNamespaceURI()) + ", wsdlLocation = " + packString(wsdlLocation) + ")");
      }
      generator.addLine("public class " + className + " extends " + baseClass + " {");
    } else {
      generator.addLine("public class " + className + " extends " + baseClass + " implements " + serializable + sMapping.getSIName() + " {");
    }
    generator.addLine();
    generator.startSection();
    if (standalone) {
      String wsdlLocationName = className.toUpperCase(Locale.ENGLISH)+"_WSDL_LOCATION";
      if (isJaxWS()) {
        if (config.isIllegalWSDLLocation()) {
          generator.addLine("private final static "+URL.class.getName()+" "+wsdlLocationName+" = null;");
        } else {
          generator.addLine("private final static "+URL.class.getName()+" "+wsdlLocationName+";");
          generator.addLine("static {");
          generator.addLine("  "+URL.class.getName()+" url = null;");
          generator.addLine("  try {");
          generator.addLine("     url = new "+URL.class.getName()+"("+packString(wsdlLocation)+");");
          generator.addLine("  } catch ("+MalformedURLException.class.getName()+" e) {");
          generator.addLine("    e.printStackTrace();");
          generator.addLine("  }");
          generator.addLine("  "+wsdlLocationName+" = url;");
          generator.addLine("}");
        }
      }
      generator.addLine("/**");
      generator.addLine(" * Default service constructor.");
      generator.addLine(" */");
      if (isJaxWS()) {
        // default constructor
        generator.addLine("public " + className + "() throws " + MalformedURLException.class.getName() + " {");
        generator.startSection();
        generator.addLine("super(" + wsdlLocationName + ", new " + QName.class.getName() + "(\""
            + serviceQName.getNamespaceURI() + "\", \"" + serviceQName.getLocalPart() + "\"));");
        generator.endSection();
        generator.addLine("}");

        // wsdl URL + Service QName constructor
        generator.addLine("public " + className + "(" + URL.class.getName() + " wsdlLocation, " + QName.class.getName() + " serviceName) {");
        generator.startSection();
        generator.addLine("super(wsdlLocation, serviceName);");
        generator.endSection();
        generator.addLine("}");
      } else {
        generator.addLine("public " + className + "() throws " + WebserviceClientException.class.getName() + " {");
        generator.startSection();
        String namespace = packString(serviceName.getNamespaceURI());
        String localName = packString(serviceName.getLocalPart());
        generator.addLine("super();");
        generator.addLine(QName.class.getName() + " serviceName = new " + QName.class.getName() + "(" + namespace + "," + localName + ");");
        generator.addLine(TypeMappingRegistry.class.getName() + " types = super.loadTypeRegistry(" + packString(typesPath)
            + ",this.getClass().getClassLoader());");
        generator.addLine(MappingRules.class.getName() + " mapping = super.loadMappingRules(" + packString(mappingPath)
            + ",this.getClass().getClassLoader());");
        generator.addLine(ConfigurationRoot.class.getName() + " config = super.loadConfiguration(" + packString(configurationPath)
            + ",this.getClass().getClassLoader());");
        generator.addLine("super.init(serviceName,types,mapping,config,this.getClass().getClassLoader());");
        generator.addLine("super.setServiceMode(JAXRPC_MODE);");
        generator.endSection();
        generator.addLine("}");
        generator.addLine();
      }

    }
    EndpointMapping[] endpoints = sMapping.getEndpoint();
    for (int i = 0; i < endpoints.length; i++) {
      EndpointMapping endpoint = endpoints[i];
      InterfaceMapping iMapping = this.config.getMappingRules().getInterface(endpoint.getPortPortType(), endpoint.getPortBinding());
      if (endpoint.getJavaDoc() != null) {
        generator.add(endpoint.getJavaDoc());
        generator.addNewLine();        
      } else {
        generator.addLine("/**");
        generator.addLine(" * Get method for webservice port [" + endpoint.getPortQName() + "].");
        generator.addLine(" */");
      }

      if (isJaxWS()) {
        generator.addLine("@javax.xml.ws.WebEndpoint(name = " + packString(endpoint.getPortQName()) + ")");
        if (endpoint.getPortGetter() != null) {
          generator.addLine("public " + iMapping.getSEIName() + " " + endpoint.getPortGetter() + "() {");
        } else {
          generator.addLine("public " + iMapping.getSEIName() + " get" + endpoint.getPortJavaName() + "() {");
        }
      } else {
        generator.addLine("public " + iMapping.getSEIName() + " get" + endpoint.getPortJavaName() + "() throws " + "javax.xml.rpc.ServiceException"
            + "  {");
      }
      generator.startSection();
      generator.addLine(QName.class.getName() + " portName = new " + QName.class.getName() + "(\"" + sMapping.getServiceName().getNamespaceURI()
          + "\",\"" + endpoint.getPortQName() + "\");");
      generator.addLine("return (" + iMapping.getSEIName() + ") super.getPort(portName," + iMapping.getSEIName() + ".class);");
      generator.endSection();
      generator.addLine("}");

    }
    if (this.config.isGenerateSerializable() && !isJaxWS()) {
      generator.addLine();
      generator.addComment("Service serialization code.");
      generator.addLine("private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {");
      generator.addLine("  super.writeObjectX(out);");
      generator.addLine("}");
      generator.addLine();
      generator.addComment("Service deserialization code.");
      generator.addLine("private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {");
      generator.addLine("  super.readObjectX(in);");
      generator.addLine("}");
      generator.addLine();
    }
    // Compatibility methods
    MappingRules mappingRules = this.config.getMappingRules();
    InterfaceMapping[] interfaceMappings = mappingRules.getInterface();
    boolean commonInterface = true;
    for (int i = 0; i < interfaceMappings.length; i++) {
      if (!interfaceMappings[i].getSEIName().equals(interfaceMappings[0].getSEIName())) {
        commonInterface = false;
        break;
      }
    }
    if (interfaceMappings.length == 0) {
      commonInterface = false;
    }
    if (commonInterface && !isJaxWS()) {
      String seiName = interfaceMappings[0].getSEIName();
      generator.addLine("public " + seiName + " getLogicalPort() throws " + ServiceException.class.getName() + "{");
      generator.addLine("  return (" + seiName + ") getPort(" + seiName + ".class);");
      generator.addLine("}");
      generator.addLine();
      generator.addLine("public " + seiName + " getLogicalPort(" + String.class.getName() + " portName) throws " + ServiceException.class.getName()
          + "{");
      generator.addLine("  return (" + seiName + ") getPort(new " + QName.class.getName() + "(null,portName)," + seiName + ".class);");
      generator.addLine("}");
      generator.addLine();
    }
    if (this.config.getGenerationMode() == ProxyGeneratorConfigNew.MIGRATION_MODE) {
      generator.addLine();
      generator.addLine("public " + LogicalPortType.class.getName() + " getLogicalPortConfiguration(java.lang.String lpName) {");
      generator.addLine("  return null;");
      generator.addLine("}");
      generator.addLine();
    }
    generator.endSection();
    generator.addLine("}");
    outputJavaFile(iLink.getSIImplName(), generator);
  }

  /**
   * Generates stub implementation. Also adds implementation link to the mapping
   * information.
   * 
   * @param iMapping
   * @throws ProxyGeneratorException
   */
  private void generateSEIImpl(InterfaceMapping iMapping) throws ProxyGeneratorException {
    QName bindingName = iMapping.getBindingQName();
    ImplementationLink iLink = iMapping.getImplementationLink();
    if (iLink == null) {
      iLink = new ImplementationLink();
      iMapping.setImplementationLink(iLink);
    }
    String packageName = convertor.getPackage(iMapping.getSEIName());
    if (this.config.getOutputPackage() != null) {
      packageName = this.config.getOutputPackage();
    }
    String interfaceName = convertor.getLocalClass(iMapping.getSEIName());
    String suffix = "_Stub";
    if (config.getGenerationMode() == ProxyGeneratorConfigNew.MIGRATION_MODE) {
      suffix = "Stub";
    }
    String className = convertor.attributeToClassName(bindingName.getLocalPart()) + suffix;
    String baseClass = DynamicStubImpl.class.getName();
    iLink.setStubName(getNameWithPackage(className, packageName));
    generator.clear();
    writePackage(packageName, generator);
    generator.addLine();
    generator.addLine("/**");
    generator.addLine(" * SEI Stub implementation of {" + iMapping.getSEIName() + "} (generated by SAP WSDL to Java generator).");
    generator.addLine(" */");
    if (isJaxWS()) {
      genWebServiceAnnotation(iMapping);
    }
    generator.addLine("public class " + className + " extends " + baseClass + " implements " + iMapping.getSEIName() + " {");
    generator.addLine();
    generator.startSection();
    OperationMapping[] operationMapping = iMapping.getOperation();
    for (int i = 0; i < operationMapping.length; i++) {
      OperationMapping oMapping = operationMapping[i];
      generator.addLine("/**");
      generator.addLine(" * Java representation of web method [" + oMapping.getWSDLOperationName() + "].");
      generator.addLine(" */");
      ParameterMapping[] allParams = oMapping.getParameter();
      generator.addIndent();
      String operationHeader = getMethodHeader(oMapping);
      generator.add("public " + operationHeader);
      generator.add(" {");
      generator.addNewLine();
      generator.startSection();
      String pObjectType = ParameterObject.class.getName();
      generator.addLine(pObjectType + "[] _params = new " + pObjectType + "[" + allParams.length + "];");
      ParameterMapping returnParameter = null;
      int returnIndex = 0;
      for (int j = 0; j < allParams.length; j++) {
        ParameterMapping parameter = allParams[j];
        String pObjectInst = "_params[" + j + "]";
        generator.addLine(pObjectInst + " = new " + pObjectType + "();");
        String pType = null;
        String pValue = null;
        switch (parameter.getParameterType()) {
          case ParameterMapping.IN_TYPE: {
            pType = parameter.getJavaType();
            pValue = convertor.attributeToIdentifier(parameter.getWSDLParameterName());
            if (parameter.getJavaParamName() != null) {
              pValue = parameter.getJavaParamName();
            }
            if (convertor.isPrimitive(pType)) {
              pValue = "new " + convertor.wrap(pType) + "(" + pValue + ")";
              pType = convertor.wrap(pType);
            }
            break;
          }
          case ParameterMapping.IN_OUT_TYPE: // $JL-SWITCH$ (case falls through
          // on purpose)
          case ParameterMapping.OUT_TYPE: {
            pType = parameter.getHolderName();
            pValue = convertor.attributeToIdentifier(parameter.getWSDLParameterName());
            if (parameter.getJavaParamName() != null) {
              pValue = parameter.getJavaParamName();
            }
            break;
          }
          case ParameterMapping.RETURN_TYPE: {
            pType = parameter.getJavaType();
            if (convertor.isPrimitive(pType)) {
              pType = convertor.wrap(pType);
            }
            returnParameter = parameter;
            returnIndex = j;
            break;
          }
          case ParameterMapping.FAULT_TYPE: {
            pType = parameter.getJavaType();
            break;
          }
        }
        if (parameter.getParameterType() == ParameterMapping.IN_OUT_TYPE || parameter.getParameterType() == ParameterMapping.OUT_TYPE) {
          pType = parameter.getHolderName();
        }/*
         * else { pType = parameter.getJavaType();
         *  }
         */
        generator.addLine(pObjectInst + ".parameterType = " + pType + ".class;");
        if (pValue != null) {
          generator.addLine(pObjectInst + ".parameterValue = " + pValue + ";");
        } else {
          generator.addLine(pObjectInst + ".parameterValue = null;");
        }
      }
      generator.addComment("setting invoked operation");
      generator.addLine("this.stubContext.setInvokedOperation(\"" + oMapping.getJavaMethodName() + "\",_params);");
      generator.addComment("operation invocation");
      generator.addLine("this.tBinding.call(this.stubContext);");
      generator.addComment("check for exceptions");
      for (int j = 0; j < allParams.length; j++) {
        ParameterMapping parameter = allParams[j];
        if (parameter.getParameterType() == ParameterMapping.FAULT_TYPE) {
          String pObjectInst = "_params[" + j + "]";
          generator.addLine("if (" + pObjectInst + ".parameterValue != null) {");
          if (isJaxWS()) {
            String pkg = parameter.getJavaType().substring(0, parameter.getJavaType().lastIndexOf('.'));
            String faultClass = QName.valueOf(parameter.getProperty(ParameterMapping.FAULT_MESSAGE)).getLocalPart();
            generator.addLine("  throw (" + pkg + "." + faultClass + ") " + pObjectInst + ".parameterValue;");
          } else {
            generator.addLine("  throw (" + parameter.getJavaType() + ") " + pObjectInst + ".parameterValue;");
          }
          generator.addLine("}");
        }
      }
      if (returnParameter != null) { // There is a return parameter
        String pObjectInst = "_params[" + returnIndex + "]";
        String pType = returnParameter.getJavaType();
        String pValue = pObjectInst + ".parameterValue";
        if (convertor.isPrimitive(pType)) {
          pValue = "((" + convertor.wrap(pType) + ") " + pValue + ")." + pType + "Value()";
        }
        generator.addLine("return (" + returnParameter.getJavaType() + ") " + pValue + ";");
      }
      generator.endSection();
      generator.addLine("}");
      generator.addLine();
    }
    generator.endSection();
    generator.addLine("}");
    outputJavaFile(iLink.getStubName(), generator);
  }
  
  /**
   * Returns the SOAPBinding annotation for specific operation.
   * @param oMapping
   * @return
   */
  private String getSOAPBindingAnnotation(OperationMapping oMapping) {
    boolean isDocument = false;
    boolean isBare = false;    
    if (oMapping.getProperty(OperationMapping.REQUEST_WRAPPER_BEAN) != null && oMapping.getProperty(OperationMapping.OPERATION_STYLE).equals("rpc")) {
      // This is unwrapped document operation not bare
      isDocument = true;            
    }
    if ("document".equals(oMapping.getProperty(OperationMapping.OPERATION_STYLE))) {
      // This is bare document operation
      isDocument = true;
    }                
    String style = null;
    
    if (isDocument) {
      style = "javax.jws.soap.SOAPBinding.Style.DOCUMENT";
    } else {
      style = "javax.jws.soap.SOAPBinding.Style.RPC";
    }
    String use = oMapping.getProperty(OperationMapping.OPERATION_USE);
    if (use.equals("literal")) {
      use = "javax.jws.soap.SOAPBinding.Use.LITERAL";
    } else {
      use = "javax.jws.soap.SOAPBinding.Use.ENCODED";
    }
    if (isDocument) {
      isBare = true;
      if (oMapping.getProperty(OperationMapping.REQUEST_WRAPPER_BEAN) != null && oMapping.getProperty(OperationMapping.OPERATION_STYLE).equals("rpc")) {
        // This is unwrapped document operation not bare
        isBare = false;
      }
    }
    String wrapperOrBare = null;;
    if (isDocument == false || isBare == true) {
      wrapperOrBare = "javax.jws.soap.SOAPBinding.ParameterStyle.BARE";
    } else {    
      if (isBare) {
        wrapperOrBare = "javax.jws.soap.SOAPBinding.ParameterStyle.BARE";
      } else {
        wrapperOrBare = "javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED";
      }
    }        
    if (isDocument) {
      return "@javax.jws.soap.SOAPBinding(parameterStyle = " + wrapperOrBare + ", style = " + style + ", use = " + use + ")";
    } else {
      return "@javax.jws.soap.SOAPBinding(style = " + style + ", use = " + use + ")";
    }
    
  }

  private String genWebServiceAnnotation(InterfaceMapping iMapping) throws ProxyGeneratorException {
    if (this.config.getJaxWSFiles().getHandlerConfigurations().size() == 1) {
      String fileName = convertor.getLocalClass(iMapping.getSEIName()) +"_handler.xml";
      generator.addLine("@javax.jws.HandlerChain(file ="+packString(fileName)+")");
      outputXMLHandlerFile(iMapping.getSEIName(),fileName,this.config.getJaxWSFiles().getHandlerConfigurations().get(0));
    }
    generator.addLine("@javax.jws.WebService(name = " + packString(iMapping.getPortType().getLocalPart()) + ", targetNamespace = " + packString(iMapping.getPortType().getNamespaceURI()) + ")");
    if (iMapping.getBindingType() != null && iMapping.getBindingType().equals(InterfaceMapping.SOAPBINDING)) {
      if (iMapping.getOperation().length > 0) {
        OperationMapping[] operations = iMapping.getOperation();
        String soapBindingAnnotation = null;
        for (OperationMapping oMapping:operations) {
          String temp = getSOAPBindingAnnotation(oMapping);
          if (soapBindingAnnotation == null) {
            soapBindingAnnotation = temp;
          } else {
            if (!soapBindingAnnotation.equals(temp)) {
              soapBindingAnnotation = null;
              break;
            }
          }
        }
        if (soapBindingAnnotation != null) {
          generator.addLine(soapBindingAnnotation);          
        }
        return soapBindingAnnotation;
      }
    }
    return null;
  }
  
  /**
   * Generates method level annotations.
   * @param oMapping
   * @param generator
   * @param globalSOAPBindinAnnotation
   * @param iMapping
   */
  private void generateMethodAnnotations(OperationMapping oMapping,CodeGenerator generator,String globalSOAPBindinAnnotation, InterfaceMapping iMapping) {
    String soapAction = oMapping.getProperty(OperationMapping.SOAP_ACTION);
    StringBuffer line = new StringBuffer("@javax.jws.WebMethod(operationName = " + packString(oMapping.getWSDLOperationName()));
    if (soapAction != null && soapAction.length() > 0) {
      line.append(", action = \"" + soapAction + "\"");
    }
    line.append(")");
    generator.addLine(line.toString());
    if (oMapping.getProperty(OperationMapping.REQUEST_WRAPPER_BEAN) != null) {
      StringBuffer requestWrapper = new StringBuffer("@javax.xml.ws.RequestWrapper(localName = "+packString(oMapping.getProperty(OperationMapping.SOAP_REQUEST_WRAPPER)));
      requestWrapper.append(", targetNamespace = "+packString(oMapping.getProperty(OperationMapping.INPUT_NAMESPACE)));
      requestWrapper.append(", className = "+packString(oMapping.getProperty(OperationMapping.REQUEST_WRAPPER_BEAN))+")");
      generator.addLine(requestWrapper.toString());
    }
    if (oMapping.getProperty(OperationMapping.RESPONSE_WRAPPER_BEAN) != null) {
      StringBuffer responseWrapper = new StringBuffer("@javax.xml.ws.ResponseWrapper(localName = "+packString(oMapping.getProperty(OperationMapping.SOAP_RESPONSE_WRAPPER)));
      responseWrapper.append(", targetNamespace = "+packString(oMapping.getProperty(OperationMapping.OUTPUT_NAMESPACE)));
      responseWrapper.append(", className = "+packString(oMapping.getProperty(OperationMapping.RESPONSE_WRAPPER_BEAN))+")");          
      generator.addLine(responseWrapper.toString());
    }
    if (globalSOAPBindinAnnotation == null && iMapping.getBindingType().equals(InterfaceMapping.SOAPBINDING)) {
      String soapBindingAnnotation = getSOAPBindingAnnotation(oMapping);
      if (soapBindingAnnotation != null) {
        generator.addLine(soapBindingAnnotation);
      }
    }  
  }

  /**
   * Generates Service Endpoint Interface from operation mapping.
   * 
   * @param oMapping
   * @throws ProxyGeneratorException
   */
  private void generateSEI(InterfaceMapping iMapping) throws ProxyGeneratorException {
    generator.clear();    
    String packageName = convertor.getPackage(iMapping.getSEIName());
    String interfaceName = convertor.getLocalClass(iMapping.getSEIName());
    writePackage(packageName, generator);
    generator.addLine();
    if (iMapping.getJavaDoc() != null) {
      generator.add(iMapping.getJavaDoc());
      generator.addNewLine();
    } else {
      generator.addLine("/**");
      generator.addLine(" * Service Endpoint Interface (generated by SAP WSDL to Java generator).");
      generator.addLine(" */");
    }
    // Used to detect global soapBinding annotation
    String globalSOAPBindinAnnotation = null;
    if (isJaxWS()) {
      globalSOAPBindinAnnotation = genWebServiceAnnotation(iMapping);
      generator.addLine("public interface " + interfaceName + " {");
    } else {
      generator.addLine("public interface " + interfaceName + " extends java.rmi.Remote,javax.xml.rpc.Stub {");
    }
    generator.addLine();
    generator.startSection();
    OperationMapping[] operationMapping = iMapping.getOperation();
    for (int i = 0; i < operationMapping.length; i++) {
      OperationMapping oMapping = operationMapping[i];
      if (oMapping.getJavaDoc() != null) {
        generator.add(oMapping.getJavaDoc());
        generator.addNewLine();
      } else {
        generator.addLine("/**");
        generator.addLine(" * Java representation of web method [" + oMapping.getWSDLOperationName() + "].");
        generator.addLine(" */");
      }
      String operationHeader = getMethodHeader(oMapping);
      if (isJaxWS()) {
        generateMethodAnnotations(oMapping,generator,globalSOAPBindinAnnotation,iMapping);
        ParameterMapping[] outParams = oMapping.getParameter();
        for (int pIx = 0; pIx < outParams.length; ++pIx) {
          if (outParams[pIx].getParameterType() == ParameterMapping.RETURN_TYPE) {            
            boolean isDocument = isDocumentStyle(oMapping);
            boolean isBare = isDocumentBare(oMapping);
            String webParamName = outParams[pIx].getWSDLParameterName();
            if (outParams[pIx].isElement()) {
              webParamName = outParams[pIx].getSchemaQName().getLocalPart();
            }
            String targetNamespace = null;
            if (outParams[pIx].isElement()) {
              targetNamespace = outParams[pIx].getSchemaQName().getNamespaceURI();
            } else {
              if (isDocument) {
                targetNamespace = outParams[pIx].getNamespace(); 
              }
            }            
            String partName = null;
            if (isDocument == false || (isDocument == true && isBare == true)) {
              partName = outParams[pIx].getWSDLParameterName();
            }
            StringBuffer line = new StringBuffer("@javax.jws.WebResult(name = " + packString(webParamName));
            if (targetNamespace != null) {
              line.append(", targetNamespace = " + packString(targetNamespace));
            }
            if (partName != null) {
              line.append(", partName = " + packString(partName));
            }
            if (outParams[pIx].isHeader()) {
              line.append(", header = " + outParams[pIx].isHeader());
            }
            line.append(")");
            if ((outParams[pIx].getProperty(ParameterMapping.IS_PARAMETER_SIMPLE_LIST)) != null) {
              generator.addLine("@javax.xml.bind.annotation.XmlList");
            }
            if (outParams[pIx].getProperty(ParameterMapping.VALUE_ADAPTER) != null) {
              generator.addLine("@javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter("+outParams[pIx].getProperty(ParameterMapping.VALUE_ADAPTER)+".class)");
            }            
            generator.addLine(line.toString());
            break;
          }
        }

        String oneWay = oMapping.getProperty(OperationMapping.OPERATION_MEP);
        if (OperationMapping.MEP_ONE_WAY.equals(oneWay)) {
          generator.addLine("@javax.jws.Oneway");
        }
      }

      generator.addIndent();
      generator.add("public " + operationHeader);
      generator.add(";");
      generator.addNewLine();
      generator.addLine();
      // Async operation support
      if ("true".equals(oMapping.getProperty(OperationMapping.ASYNC_METHODS))) {
        // Get all return,out and inout parameters
        boolean isDocument = isDocumentStyle(oMapping);
        boolean isBare = isDocumentBare(oMapping);
        ParameterMapping[] outParams = oMapping.getParameters(ParameterMapping.RETURN_TYPE+ParameterMapping.OUT_TYPE+ParameterMapping.IN_OUT_TYPE);
        ParameterMapping[] inParams = oMapping.getParameters(ParameterMapping.IN_TYPE+ParameterMapping.IN_OUT_TYPE);
        String asyncResultTypeName = null;        
        if (outParams.length == 1) { 
          // Will form a Response bean with single property or a single part that reffers to element 
          asyncResultTypeName = convertor.wrap(outParams[0].getJavaType());
        } else if (outParams.length > 1) {
          // TODO: Response Bean has to be generated.
        }        
        if (asyncResultTypeName != null) {
          generateMethodAnnotations(oMapping,generator,globalSOAPBindinAnnotation,iMapping);
          generator.addIndent();
          generator.add("public javax.xml.ws.Response<"+asyncResultTypeName+"> "+oMapping.getJavaMethodName()+"Async(");
          SchemaTypeSet schemaTypeSet = config.getSchemaConfig().getTypeSet();
          boolean addComma = false;
          for (ParameterMapping pMapping : inParams) {
            String javaType = pMapping.getJavaType();
            boolean isInout = false;
            if (pMapping.getParameterType() == ParameterMapping.IN_OUT_TYPE) {
              javaType = convertor.unwrap(javaType);
              isInout = true;
              pMapping.setParameterType(ParameterMapping.IN_TYPE);
            }
            if (addComma) {
              generator.add(",");
            }
            addComma = true;
            generator.add(getWebParamAnnotation(isDocument,isBare,pMapping));
            generator.add(javaType+" "+pMapping.getJavaParamName());
            if (isInout) {
              pMapping.setParameterType(ParameterMapping.IN_OUT_TYPE);              
            }
          }
          generator.add(");");
          generator.addNewLine();
          generator.addLine();
          generateMethodAnnotations(oMapping,generator,globalSOAPBindinAnnotation,iMapping);
          generator.addIndent();
          generator.add("public java.util.concurrent.Future<?> "+oMapping.getJavaMethodName()+"Async(");
          addComma = false;
          for (ParameterMapping pMapping : inParams) {
            String javaType = pMapping.getJavaType();
            boolean isInout = false;
            if (pMapping.getParameterType() == ParameterMapping.IN_OUT_TYPE) {
              javaType = convertor.unwrap(javaType);
              isInout = true;
              pMapping.setParameterType(ParameterMapping.IN_TYPE);
            }
            if (addComma) {
              generator.add(", ");
            }
            addComma = true;
            generator.add(getWebParamAnnotation(isDocument,isBare,pMapping));
            generator.add(javaType+" "+pMapping.getJavaParamName());
            if (isInout) {
              pMapping.setParameterType(ParameterMapping.IN_OUT_TYPE);              
            }
          }
          if (addComma) {
            generator.add(", ");
          }
          generator.add("javax.xml.ws.AsyncHandler<"+asyncResultTypeName+"> asyncHandler);");
          generator.addNewLine();
          generator.addLine();          
        }
      }
    }
    generator.endSection();
    generator.addLine("}");
    outputJavaFile(iMapping.getSEIName(), generator);
  }

  private void outputXMLHandlerFile(String className,String xmlFileName, DOMSource source) throws ProxyGeneratorException {
    String packageName = convertor.getPackage(className);
    // Print the XML file into a buffer.
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    StreamResult res = new StreamResult(buffer);    
    try {
      //TransformerFactory trFact = new TransformerFactoryImpl();
      TransformerFactory trFact = TransformerFactory.newInstance();
      Transformer transformer = trFact.newTransformer();    
      transformer.transform(source, res);
    } catch (TransformerException e) {
      throw new ProxyGeneratorException(ProxyGeneratorException.CREATE_FILE_ERROR, e, xmlFileName); 
    } 
    // Output the xml file
    if (config.isFileBufferOutput()) {
      FileBufferSet fbs = config.getOutputFiles();
      packageName = packageName.replace('.', File.separatorChar);
      FileBuffer fb = new FileBuffer(packageName, xmlFileName);
      fb.setContent(buffer.toByteArray());
      fbs.addFile(fb);
    } else {
      File outputDir = getPackageDir(packageName);
      outputDir.mkdirs();
      File outputFile = new File(outputDir, xmlFileName);
      try {
        outputFile.createNewFile();
      } catch (Exception e) {
        throw new ProxyGeneratorException(ProxyGeneratorException.CREATE_FILE_ERROR, e, outputFile.getAbsolutePath());
      }
      FileOutputStream output = null;
      try {
        output = new FileOutputStream(outputFile);
        output.write(buffer.toByteArray());
        output.flush();
      } catch (Exception x) {
        throw new ProxyGeneratorException(ProxyGeneratorException.CREATE_FILE_ERROR, x, outputFile.getAbsolutePath());
      } finally {
        if (output != null) {
          try {
            output.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }    
  }
  
  /**
   * Outputs Java file to file system.
   * 
   * @param className
   * @param generator
   * @throws ProxyGeneratorException
   */
  private void outputJavaFile(String className, CodeGenerator generator) throws ProxyGeneratorException {
    String packageName = convertor.getPackage(className);
    String localName = convertor.getLocalClass(className);
    if (config.isFileBufferOutput()) {
      FileBufferSet fbs = config.getOutputFiles();
      packageName = packageName.replace('.', File.separatorChar);
      String javaName = localName + ".java";
      FileBuffer fb = new FileBuffer(packageName, javaName);
      fb.setContent(generator.toString().getBytes());
      fbs.addFile(fb);
    } else {
      File outputDir = getPackageDir(packageName);
      outputDir.mkdirs();
      File outputFile = new File(outputDir, localName + ".java");
      try {
        outputFile.createNewFile();
      } catch (Exception e) {
        throw new ProxyGeneratorException(ProxyGeneratorException.CREATE_FILE_ERROR, e, outputFile.getAbsolutePath());
      }
      Writer output = null;
      try {
        output = new BufferedWriter(new ASKIIEncoderFilterWriter(new FileWriter(outputFile)));
        output.write(generator.toString().toCharArray());
        output.flush();
      } catch (Exception x) {
        throw new ProxyGeneratorException(ProxyGeneratorException.CREATE_FILE_ERROR, x, outputFile.getAbsolutePath());
      } finally {
        if (output != null){
          try {
            output.close();
          } catch (IOException e) {
            // $JL-EXC$
          }         
        }
      }
    }
  }

  private void setInterfaceMappingReference(String iMappingId, Interface wsInterface, Binding wsBinding) {
    QName pTypeName = wsInterface.getName();
    QName bName = wsBinding.getName();
    ConfigurationRoot confRoot = this.config.getProxyConfig();
    com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition[] interfaces = confRoot.getDTConfig().getInterfaceDefinition();
    for (int i = 0; i < interfaces.length; i++) {
      Variant[] variants = interfaces[i].getVariant();
      for (int j = 0; j < variants.length; j++) {
        InterfaceData iData = variants[j].getInterfaceData();
        QName qname = new QName(iData.getNamespace(), iData.getName());
        if (pTypeName.equals(qname)) {
          interfaces[i].setInterfaceMappingId(iMappingId);
        }
      }
    }
    com.sap.engine.services.webservices.espbase.configuration.Service[] services = confRoot.getRTConfig().getService();
    for (int i = 0; i < services.length; i++) {
      com.sap.engine.services.webservices.espbase.configuration.Service service = services[i];
      com.sap.engine.services.webservices.espbase.configuration.BindingData[] bindings = service.getServiceData().getBindingData();
      for (int j = 0; j < bindings.length; j++) {
        QName qname = new QName(bindings[j].getBindingNamespace(), bindings[j].getBindingName());
        if (bName.equals(qname)) {
          bindings[j].setInterfaceMappingId(iMappingId);
        }
      }
    }
  }

  /**
   * Loads default interface mapping data.
   * 
   * @throws ProxyGeneratorException
   */
  private void initSEI(Interface wsInterface, Binding binding, String packageName) throws ProxyGeneratorException {
    // working variables
    SchemaTypeSet schemaTypeSet = config.getSchemaConfig().getTypeSet();
    QName interfaceName = wsInterface.getName();
    InterfaceMapping imappingNew = getInterfaceMapping(binding.getName(), interfaceName);
    // JAX-WS extension support
    ExtendedPortType extInt = null;
    if (isJaxWS()) {
      extInt = extendedDefinitions.getExtendedPortType(imappingNew.getPortType());
    }
    // Setup SEI Name
    if (config.getGenerationMode() == ProxyGeneratorConfigNew.GENERIC_MODE || config.getGenerationMode() == ProxyGeneratorConfigNew.GENERIC_MODE_SDO) {
      imappingNew.setSEIName(DInterface.class.getName());
    } else {
      // JAX-RPC Mapping rules
      imappingNew.setSEIName(getConvertedNameWithPackage(wsInterface.getName().getLocalPart(), packageName));
      if (isJaxWS()) {
        String javaClassName = NameConverter.smart.toClassName(wsInterface.getName().getLocalPart());
        imappingNew.setSEIName(getNameWithPackageJAXWS(javaClassName, packageName));
        // JAX-WS Mapping rules
        if (extInt != null) {
          String newName = extInt.getClassName();
          if (newName != null) {
            imappingNew.setSEIName(getNameWithPackageJAXWS(newName, packageName));
          }
          String javaDoc = extInt.getJavaDoc();
          if (javaDoc != null) {
            imappingNew.setJavaDoc(javaDoc);
          }
        }
      }
    }
    UID uid = new UID();
    imappingNew.setInterfaceMappingID(uid.toString());
    // Set mapping ID in configuration
    setInterfaceMappingReference(uid.toString(), wsInterface, binding);
    // Setup operation list
    ObjectList oList = wsInterface.getOperations();
    // Check for overloaded operations
    HashSet<String> opNames = new HashSet<String>();
    for (int i = 0; i < oList.getLength(); i++) {      
      Operation operation = (Operation) oList.item(i);
      if (opNames.contains(operation.getName())) {
        throw new ProxyGeneratorException(ProxyGeneratorException.UNSUPPORTED_WSDL_OPERATION_OVERLOAD,operation.getName());
      }
      opNames.add(operation.getName());
    }
    opNames.clear();
    for (int i = 0; i < oList.getLength(); i++) {
      // Setup operation
      ExtendedPorttypeOperation extOp = null;
      Operation operation = (Operation) oList.item(i);
      OperationMapping oMappingNew = new OperationMapping();
      oMappingNew.setWSDLOperationName(operation.getName());
      String javaMethodName = convertor.attributeToMethodName(oMappingNew.getWSDLOperationName());
      if (isJaxWS()) {
        // TODO:Substitute with proper default jaxws operation name mapping  
        javaMethodName = NameConverter.smart.toVariableName(oMappingNew.getWSDLOperationName());
        if (extInt != null) {
          extOp = (ExtendedPorttypeOperation) extInt.getExtendedChild(new QName(oMappingNew.getWSDLOperationName()));
          if (extOp != null) {
            // set the new operation name
            if (extOp.getOpName() != null) {
              javaMethodName = extOp.getOpName();
            }
            if (extOp.getJavaDoc() != null) {
              oMappingNew.setJavaDoc(extOp.getJavaDoc());
            }
            if (extOp.getAsyncMapping().equals(Boolean.TRUE)) {
              oMappingNew.setProperty(OperationMapping.ASYNC_METHODS,"true");
            }
          }
        }
      }
      oMappingNew.setJavaMethodName(javaMethodName);
      oMappingNew.setProperty(OperationMapping.SOAP_REQUEST_WRAPPER, oMappingNew.getWSDLOperationName());
      oMappingNew.setProperty(OperationMapping.SOAP_RESPONSE_WRAPPER, oMappingNew.getWSDLOperationName() + OperationMapping.OPERATION_RESPONSE_SUFFIX);
      // setup operation MEP
      String mep = operation.getProperty(Operation.MEP);
      if (Operation.IN_MEP.equals(mep)) {
        oMappingNew.setProperty(OperationMapping.OPERATION_MEP, OperationMapping.MEP_ONE_WAY);
      }
      if (Operation.INOUT_MEP.equals(mep)) {
        oMappingNew.setProperty(OperationMapping.OPERATION_MEP, OperationMapping.MEP_REQ_RESP);
      }
      // IN+INOUT
      // TODO: Check if it is possible to have a single OUT Parameter and no
      // RETURN parameter
      ObjectList inOutParam = operation.getParameters(Parameter.INOUT | Parameter.OUT);
      // 640 holder rules - if only one in/out - 'unwrap' it. Used in migration
      // mode only
      if (config.getGenerationMode() == ProxyGeneratorConfigNew.MIGRATION_MODE && inOutParam.getLength() == 1) {

        // preserve the order of the parameters!
        ObjectList allOriginalParams = operation.getParameters(Parameter.IN | Parameter.INOUT | Parameter.OUT);
        ArrayList<Parameter> originalOrder = new ArrayList<Parameter>(allOriginalParams.getLength());
        for (int origIx = 0; origIx < allOriginalParams.getLength(); ++origIx) {
          originalOrder.add(origIx, (Parameter) allOriginalParams.item(origIx));
          operation.removeChild(allOriginalParams.item(origIx));
        }

        // remove the "old" in/out param
        Parameter theParam = (Parameter) inOutParam.item(0);
        int removedFromIx = originalOrder.indexOf(theParam);
        originalOrder.remove(removedFromIx);

        try {

          // create a new IN param from the old IN/OUT param, UNLESS it's an OUT
          // param
          if (theParam.getParamType() != Parameter.OUT) {
            Parameter newParam = new Parameter(theParam.getName(), Parameter.IN);
            newParam.setProperty(WSDL11Constants.MESSAGE_QNAME, theParam.getProperty(WSDL11Constants.MESSAGE_QNAME));
            newParam.appendXSDTypeRef(theParam.getXSDTypeRef().getQName(), theParam.getXSDTypeRef().getXSDType());
            originalOrder.add(removedFromIx, newParam);
          }

          // and a new RETURN param
          operation.appendParameter(theParam.getName(), Parameter.RETURN);
          Parameter outP = (Parameter) operation.getParameters(Parameter.RETURN).item(0);
          outP.setProperty(WSDL11Constants.MESSAGE_QNAME, theParam.getProperty(WSDL11Constants.MESSAGE_QNAME));
          outP.appendXSDTypeRef(theParam.getXSDTypeRef().getQName(), theParam.getXSDTypeRef().getXSDType());

          // add back the original parameters in their preserved order (might
          // include multiple IN params)
          for (Parameter fresh : originalOrder) {
            operation.appendChild(fresh);
          }

        } catch (WSDLException e) {
          throw new ProxyGeneratorException(ProxyGeneratorException.GENERATION_PROBLEM, e);
        }

      }
      // IN+INOUT+OUT
      ObjectList parameters = operation.getParameters(Parameter.IN + Parameter.INOUT + Parameter.OUT);
      for (int j = 0; j < parameters.getLength(); j++) {
        Parameter param = (Parameter) parameters.item(j);
        ParameterMapping pMappingNew = new ParameterMapping();
        initParameterMapping(pMappingNew, param, schemaTypeSet, extOp);
        oMappingNew.addParameter(pMappingNew);
      }
      // OUT
      /* commented
      parameters = operation.getParameters(Parameter.OUT);
      for (int j = 0; j < parameters.getLength(); j++) {
        Parameter param = (Parameter) parameters.item(j);
        ParameterMapping pMappingNew = new ParameterMapping();
        initParameterMapping(pMappingNew, param, schemaTypeSet, extOp);
        oMappingNew.addParameter(pMappingNew);
      }*/
      // RETURN
      parameters = operation.getParameters(Parameter.RETURN);
      for (int j = 0; j < parameters.getLength(); j++) {
        Parameter param = (Parameter) parameters.item(j);
        ParameterMapping pMappingNew = new ParameterMapping();
        initParameterMapping(pMappingNew, param, schemaTypeSet, extOp);
        oMappingNew.addParameter(pMappingNew);
      }
      // FAULT
      parameters = operation.getParameters(Parameter.FAULT);
      for (int j = 0; j < parameters.getLength(); j++) {
        Parameter param = (Parameter) parameters.item(j);
        ParameterMapping pMappingNew = new ParameterMapping();
        initFaultMapping(pMappingNew, param, schemaTypeSet, extOp, packageName);
        oMappingNew.addParameter(pMappingNew);
      }
      // Special case of name collision in fault parameters in the dynamic api case.
      if (config.getGenerationMode() == ProxyGeneratorConfigNew.GENERIC_MODE) {
        ParameterMapping[] faults = oMappingNew.getParameters(ParameterMapping.FAULT_TYPE);
        boolean specialDynamicCase = false;
        for (int k=0; k<faults.length; k++) {
          for (int l=k+1; l<faults.length; l++) {
            ParameterMapping part1 = faults[k];
            ParameterMapping part2 = faults[l];
            if (part1.getWSDLParameterName() != null && part1.getWSDLParameterName().equals(part2.getWSDLParameterName())) {
              specialDynamicCase = true;
              break;
            }
          }
          if (specialDynamicCase) {
            break;
          }
        }     
        if (specialDynamicCase) {
          for (int k=0; k<faults.length; k++) {
            faults[k].setWSDLParameterName(faults[k].getFaultElementQName().getLocalPart());
          }          
        }
      }      
      imappingNew.addOperation(oMappingNew);
    }
    if (binding instanceof SOAPBinding) {
      // Handling of SOAP Binding
      SOAPBinding soapBinding = (SOAPBinding) binding;
      imappingNew.setBindingType(InterfaceMapping.SOAPBINDING);
      String soapVer = ((SOAPBinding) binding).getSOAPVersion();
      if (SOAPBinding.SOAP_VERSION_12.equals(soapVer)) {
        imappingNew.setProperty(InterfaceMapping.SOAP_VERSION, InterfaceMapping.SOAP_VERSION_12);
      } else {
        imappingNew.setProperty(InterfaceMapping.SOAP_VERSION, InterfaceMapping.SOAP_VERSION_11);
      }
      processSOAPBinding(soapBinding, imappingNew, wsInterface);
      if (config.getGenerationMode() == ProxyGeneratorConfigNew.GENERIC_MODE ||
          config.getGenerationMode() == ProxyGeneratorConfigNew.GENERIC_MODE_SDO) {        
        if (SOAPBinding.SOAP_VERSION_12.equals(soapVer)) {
          // Dynamic proxy does not support soap12. Temporary.
          removeInterfaceMapping(imappingNew.getInterfaceMappingID());
        }
      }      
    } else if(binding instanceof HTTPBinding) {
      // Covers all cases when HTTPGetPost binding is not supported
      if (config.getGenerationMode() == ProxyGeneratorConfigNew.GENERIC_MODE || 
          config.getGenerationMode() == ProxyGeneratorConfigNew.GENERIC_MODE_SDO ||
          config.getGenerationMode() == ProxyGeneratorConfigNew.JAXWS_MODE ||
          config.getGenerationMode() == ProxyGeneratorConfigNew.JAXWS_MODE_LOAD ||
          config.getGenerationMode() == ProxyGeneratorConfigNew.JAXWS_MODE_SERVER) {
        removeInterfaceMapping(imappingNew.getInterfaceMappingID());                 
      } else {     
        // Handling of HTTP Binding
        HTTPBinding httpBinding = (HTTPBinding)binding;
        imappingNew.setBindingType(InterfaceMapping.HTTPBINDING);
        processHTTPBinding(httpBinding, imappingNew);
      }
    } else {
      // The binding is not supported so remove it
      // TODO: Check functionality
      // if (config.getGenerationMode() == ProxyGeneratorConfigNew.GENERIC_MODE)
      // {
      removeInterfaceMapping(imappingNew.getInterfaceMappingID());
      // }
      // removeInterfaceMappingReference(uid.toString(),wsInterface,binding);
    }
    if (isJaxWS()) { // This is JAX-WS generation mode. Do a parameter clash check
      checkInterfaceNameClashes(imappingNew); 
    }
  }
  
  /**
   * Checks for Name clashes inside the Java Interface.
   * @param iMapping
   * @throws ProxyGeneratorException
   */
  private void checkInterfaceNameClashes(InterfaceMapping iMapping) throws ProxyGeneratorException {
    OperationMapping[] oMapping = iMapping.getOperation();
    for (OperationMapping operation: oMapping) {
      checkOperationParameterClash(operation);  
    }
  }
  
  /**
   * This method reads all interface operations and checks for name collisions of parameters.
   * The operation throws exception with the problematic parameter names and operation name.
   * @param oMapping Operation description.
   */
  private void checkOperationParameterClash(OperationMapping oMapping) throws ProxyGeneratorException {
    HashSet<String> javaNames = new HashSet();
    ParameterMapping[] params = oMapping.getParameters(ParameterMapping.IN_TYPE+ParameterMapping.IN_OUT_TYPE+ParameterMapping.OUT_TYPE);
    for (ParameterMapping parameter : params) {
      String javaName = parameter.getJavaParamName();
      if (javaNames.contains(javaName)) {
        // Throw Exception because there is a parameter name collision.
        throw new ProxyGeneratorException(ProxyGeneratorException.DUBLICATE_PARAMETER_NAME,oMapping.getWSDLOperationName(),javaName);
      }
      javaNames.add(javaName);
    }
  }
  
  /**
   * Processes operation attachment parameters.
   * @param inputAttachments
   * @param outputAttachments
   * @param oMapping
   * @param extendedBindingOperation
   */
  private void markAttachmentParameters(AttachmentsContainer inputAttachments, AttachmentsContainer outputAttachments, OperationMapping oMapping, ExtendedBindingOperation extendedBindingOperation) {
    
    if (inputAttachments != null) {
      ObjectList mimeParts = inputAttachments.getMIMEParts();
      int partSize = mimeParts.getLength();
      for (int i=0; i < partSize; i++) {
        MIMEPart mimePart = (MIMEPart) mimeParts.item(i);
        String partName = mimePart.getPartName();
        List mimeTypes = mimePart.getMimeTypeAlternatives();
        if (partName != null && partName.length() != 0) {
          ParameterMapping[] parameters = oMapping.getParameter();
          for (ParameterMapping parameter : parameters) {                                    
            int parameterType = parameter.getParameterType();
            if (partName.equals(parameter.getWSDLParameterName()) && (parameterType == ParameterMapping.IN_TYPE || parameterType == ParameterMapping.IN_OUT_TYPE)) {
              updateJavaTypeToMime(parameter,mimeTypes,extendedBindingOperation);              
              break;
            }
          }
        }
      }
    }
    if (outputAttachments != null) {
      ObjectList mimeParts = outputAttachments.getMIMEParts();
      int partSize = mimeParts.getLength();
      for (int i=0; i < partSize; i++) {
        MIMEPart mimePart = (MIMEPart) mimeParts.item(i);
        String partName = mimePart.getPartName();
        List mimeTypes = mimePart.getMimeTypeAlternatives();
        if (partName != null && partName.length() != 0) {
          ParameterMapping[] parameters = oMapping.getParameter();
          for (ParameterMapping parameter : parameters) {
            int parameterType = parameter.getParameterType();
            if (partName.equals(parameter.getWSDLParameterName()) && (parameterType == ParameterMapping.OUT_TYPE || parameterType == ParameterMapping.RETURN_TYPE || parameterType == ParameterMapping.IN_OUT_TYPE)) {
              updateJavaTypeToMime(parameter,mimeTypes,extendedBindingOperation);
              break;
            }
          }
        }
      }    
    }
  }

  /**
   * Implements Mime type to java type mapping.
   * @param contentType
   * @return
   */
  private String getJavaMTOMType(String contentType) {
    String newJavaType = DataHandler.class.getName();
    if ("image/gif".equals(contentType) || "image/jpeg".equals(contentType)) {
      newJavaType = Image.class.getName();  
    }
    if ("text/xml".equals(contentType) || "application/xml".equals(contentType)) {
      newJavaType = Source.class.getName();
    }
    return newJavaType;
  }
  
  
  private void updateJavaTypeToMime(ParameterMapping parameter, List contentTypes, ExtendedBindingOperation extendedBindingOperation) {
    String contentType = (String) contentTypes.get(0);
    for (int j = 1; j < contentTypes.size(); j++) {
      contentType = contentType + ";" + (String) contentTypes.get(j);
    }              
    parameter.setProperty(ParameterMapping.IS_ATTACHMENT,"true");                
    parameter.setProperty(ParameterMapping.ATTACHMENT_CONTENT_TYPE,contentType);
    if (extendedBindingOperation != null) {
      if ("true".equalsIgnoreCase(extendedBindingOperation.getMimeContent())) {
        // Mime Mapping is enabled.
        String newJavaType = getJavaMTOMType(contentType);
        parameter.setJavaType(newJavaType);
        int parameterType = parameter.getParameterType();
        if (parameterType == ParameterMapping.IN_OUT_TYPE || parameterType == ParameterMapping.OUT_TYPE) {
          // IN-OUT or OUT parameter
          parameter.setHolderName("javax.xml.ws.Holder<" + newJavaType + ">");
        }
      }
    }    
  }

  /**
   * Marks header element parameters.
   * 
   * @param inputHeaders
   * @param outputHeaders
   * @param oMapping
   */
  private void markHeaderParameters(String inputHeaders, String outputHeaders, OperationMapping oMapping,ExtendedBindingOperation extendedBindingOperation) {
    ParameterMapping[] parameters = oMapping.getParameters(ParameterMapping.IN_TYPE + ParameterMapping.IN_OUT_TYPE);
    if (inputHeaders != null) {
      StringTokenizer tokenizer = new StringTokenizer(inputHeaders, " ", false);
      while (tokenizer.hasMoreTokens()) {
        String token = tokenizer.nextToken();
        for (int i = 0; i < parameters.length; i++) {
          if (token.equals(parameters[i].getWSDLParameterName())) {
            parameters[i].setHeader(true);
            if (extendedBindingOperation != null && isJaxWS()) {
              ExtendedOperationMod bMod = (ExtendedOperationMod) extendedBindingOperation.getParameterCust(parameters[i].getWSDLParameterName(),null);
              if (bMod != null) {
                if (bMod.getNewName() != null) {
                  parameters[i].setJavaParamName(bMod.getNewName());
                }
              }              
            }
          }
        }
      }
    }
    if (outputHeaders == null) {
      return;
    } else {
      StringTokenizer tokenizer = new StringTokenizer(outputHeaders, " ", false);
      parameters = oMapping.getParameters(ParameterMapping.OUT_TYPE + ParameterMapping.IN_OUT_TYPE + ParameterMapping.RETURN_TYPE);
      while (tokenizer.hasMoreTokens()) {
        String token = tokenizer.nextToken();
        for (int i = 0; i < parameters.length; i++) {
          if (token.equals(parameters[i].getWSDLParameterName())) {
            parameters[i].setHeader(true);
            if (extendedBindingOperation != null && isJaxWS()) {
              ExtendedOperationMod bMod = (ExtendedOperationMod) extendedBindingOperation.getParameterCust(parameters[i].getWSDLParameterName(),null);
              if (bMod != null) {
                if (bMod.getNewName() != null) {
                  parameters[i].setJavaParamName(bMod.getNewName());
                }
              }              
            }            
          }
        }
      }
    }
  }

  /**
   * Unwraps document/literal operation transforming it to rpc/literal operation.
   * If the generator is configured in MIGRATION mode for supporting 6.40/7.0 web services clients it saves copy of the 
   * original operation mapping to produce both document and rpc versions of the operation. 
   * 
   * @param operation
   */
  private OperationMapping unWrapDocumentOperation(OperationMapping operation, ExtendedPorttypeOperation extPortTypeOp) throws ProxyGeneratorException {
    ParameterMapping[] inParams = operation.getParameters(ParameterMapping.IN_TYPE+ParameterMapping.IN_OUT_TYPE);
    ParameterMapping[] outParams = operation.getParameters(ParameterMapping.RETURN_TYPE+ParameterMapping.IN_OUT_TYPE);
    OperationMapping originalOperation = null;
    // There should be exaxtly one input parameter
    if (inParams.length == 1) {
      operation.setProperty(OperationMapping.INPUT_NAMESPACE, inParams[0].getSchemaQName().getNamespaceURI());
      operation.setProperty(OperationMapping.SOAP_REQUEST_WRAPPER, inParams[0].getSchemaQName().getLocalPart());      
      if (this.config.getGenerationMode() == ProxyGeneratorConfigNew.MIGRATION_MODE) {
        try {
          originalOperation = (OperationMapping) operation.clone();
        } catch (CloneNotSupportedException x) {
          originalOperation = null; 
        }        
      }
      // Input parameters unwrap
      ArrayList<ParameterMapping> inputParams = null; 
      ArrayList<ParameterMapping> outputParams = null;
      operation.setProperty(OperationMapping.INPUT_NAMESPACE, inParams[0].getSchemaQName().getNamespaceURI());
      if (isJaxWS()) {
        operation.setProperty(OperationMapping.REQUEST_WRAPPER_BEAN, inParams[0].getJavaType());
        }
      
      inputParams = getSequenceFields(inParams[0].getSchemaQName());
      for (int i = 0; i < inputParams.size(); i++) {
        ParameterMapping pMapping = inputParams.get(i);
        // JAVA mapping handling - sets default parameter java name
        pMapping.setJavaParamName(convertor.attributeToIdentifier(pMapping.getWSDLParameterName()));
        if (extPortTypeOp != null) {
          String wrapperParamName = inParams[0].getWSDLParameterName();
          int parameterType = convertParameterTypeToCustType(inParams[0].getParameterType());
          ExtendedOperationMod extOpMod = extPortTypeOp.getParameterCust(parameterType, wrapperParamName, new QName(pMapping.getNamespace(),pMapping.getWSDLParameterName()));
          if (extOpMod != null) {
            if (extOpMod.getNewName() != null) {
              pMapping.setJavaParamName(extOpMod.getNewName());
            }
          }
        }
        pMapping.setParameterType(ParameterMapping.IN_TYPE);
      }
      
      operation.removeParameter(inParams[0]);
      // output unwrap
      if (outParams.length == 1) {
        operation.setProperty(OperationMapping.OUTPUT_NAMESPACE, outParams[0].getSchemaQName().getNamespaceURI());
        operation.setProperty(OperationMapping.SOAP_RESPONSE_WRAPPER, outParams[0].getSchemaQName().getLocalPart());
        if (isJaxWS()) {
          operation.setProperty(OperationMapping.RESPONSE_WRAPPER_BEAN, outParams[0].getJavaType());
        }
        outputParams = getSequenceFields(outParams[0].getSchemaQName());
        for (int i = 0; i < outputParams.size(); i++) {
          ParameterMapping pMapping = outputParams.get(i);
          // JAVA mapping handling - sets default parameter java name
          pMapping.setJavaParamName(convertor.attributeToIdentifier(pMapping.getWSDLParameterName()));
          if (extPortTypeOp != null) {
            String wrapperParamName = outParams[0].getWSDLParameterName();
            int parameterType = convertParameterTypeToCustType(outParams[0].getParameterType());
            ExtendedOperationMod extOpMod = extPortTypeOp.getParameterCust(parameterType,
                wrapperParamName, new QName(pMapping.getNamespace(),pMapping.getWSDLParameterName()));
            if (extOpMod != null) {
              if (extOpMod.getNewName() != null) {
                pMapping.setJavaParamName(extOpMod.getNewName());
              }
            }
          }
          if (outputParams.size() == 1) {
            pMapping.setParameterType(ParameterMapping.RETURN_TYPE);
          } else {
            pMapping.setParameterType(ParameterMapping.OUT_TYPE);
            String holderName = null;
            // Overwrite the holder in JAX-WS Mode
            if (isJaxWS()) {
              pMapping.setJavaType(convertor.wrap(pMapping.getJavaType()));
              holderName = "javax.xml.ws.Holder<" + pMapping.getJavaType() + ">";
            } else {
              if (convertor.hasBuiltInHolder(pMapping.getJavaType())) {
                holderName = convertor.primitiveToHolder(pMapping.getJavaType());
              } else {
                holderName = getHolderName(pMapping);
                if (config.getOutputPackage() != null && config.getOutputPackage().length() != 0) {
                  holderName = config.getOutputPackage() + ".holders." + holderName;
                } else {
                  holderName = "holders." + holderName;
                }
              }
            }
            pMapping.setHolderName(holderName);
          }
          //outputParams.add(pMapping);
        }
        operation.removeParameter(outParams[0]);
      }
      // Check for inout parameters
      if (outputParams != null) {
      for (int i = 0; i < inputParams.size(); i++) {
        ParameterMapping inputParam = (ParameterMapping) inputParams.get(i);
        ParameterMapping[] outputParamsArr = new ParameterMapping[outputParams.size()];
        outputParams.toArray(outputParamsArr);
        for (int j = 0; j < outputParamsArr.length; j++) {
          ParameterMapping outputParam = outputParamsArr[j];
          if (inputParam.getWSDLParameterName().equals(outputParam.getWSDLParameterName())
              && outputParam.getParameterType() != ParameterMapping.RETURN_TYPE) {
            if (convertor.wrap(inputParam.getJavaType()).equals(
                convertor.wrap(outputParam.getJavaType()))) {
              inputParam.setParameterType(ParameterMapping.IN_OUT_TYPE);
              // INOUT or OUT parameter type - must be mapped to holder class
              inputParam.setHolderName(outputParam.getHolderName());
              inputParam.setJavaType(convertor.wrap(inputParam.getJavaType()));
              outputParams.remove(outputParam);
              break;
            }
          }
        }
      }
      }
      for (int i = 0; i < inputParams.size(); i++) {
        operation.addParameter((ParameterMapping) inputParams.get(i));
      }
      if (outputParams != null) {
      for (int i = 0; i < outputParams.size(); i++) {
        operation.addParameter((ParameterMapping) outputParams.get(i));
      }
    }
    }
    return originalOperation;
  }

  /**
   * Hack method to unwrap document operation. Outside the generation loop. Not
   * reccomended to use.
   * 
   * @param contentInterface
   * @param operation
   * @param config
   */
  public void unwrapOperation(InterfaceMapping contentInterface, OperationMapping operationMapping, ProxyGeneratorConfigNew config) throws ProxyGeneratorException {
    this.config = config;
    Definitions definitions = config.getWsdl();
    Interface portTypeIf = definitions.getInterface(contentInterface.getPortType());
    Operation wsOperation = portTypeIf.getOperation(operationMapping.getWSDLOperationName());
    String operationStyle = operationMapping.getProperty(OperationMapping.OPERATION_STYLE);
    boolean docStylePossible = false;
    if (config.getGenerationMode() == ProxyGeneratorConfigNew.MIGRATION_MODE) {
      docStylePossible = isDocumentStylePossible640(wsOperation);
    } else {
      docStylePossible = isDocumentStylePossible(wsOperation);
    }
    if (docStylePossible && SOAPBinding.DOC_STYLE_VALUE.equals(operationStyle)) {
      // wrapper style document(literal) -> convert to rpc literal
      unWrapDocumentOperation(operationMapping,null);
      operationMapping.setProperty(OperationMapping.OPERATION_STYLE, SOAPBinding.RPC_STYLE_VALUE);
    }
    this.config = null;
  }

  /**
   * Process soap binding information.
   * 
   * @param binding
   * @param iMapping
   */
  private void processSOAPBinding(SOAPBinding binding, InterfaceMapping iMapping, Interface wsInterface) throws ProxyGeneratorException {
    ExtendedBinding extBinding = null;
    ExtendedPortType extPortType = null;
    if (isJaxWS()) {
      extBinding = extendedDefinitions.getExtendedBinding(iMapping.getBindingQName());
      extPortType = extendedDefinitions.getExtendedPortType(iMapping.getPortType());
    }
    ObjectList operations = binding.getOperations();
    if (operations.getLength() != iMapping.getOperation().length) {
      OperationMapping[] operationMappings = iMapping.getOperation();
      for (int i=0; i<operationMappings.length; i++) {
        try {
          SOAPBindingOperation operation = binding.getOperation(operationMappings[i].getWSDLOperationName());
          if (operation == null) {
            // This operation does not have corresponding in the SOAPBinding element.
            throw new ProxyGeneratorException(ProxyGeneratorException.MISSING_OPERATION_BINDING,binding.getName().toString(),operationMappings[i].getWSDLOperationName());
          }
        } catch (WSDLException x) {
          // This could never happen.
          x.printStackTrace();
        }
      }
    }   
    for (int i = 0; i < operations.getLength(); i++) {
      SOAPBindingOperation operation = (SOAPBindingOperation) operations.item(i);      
      Operation wsOperation = wsInterface.getOperation(operation.getName());
      OperationMapping operationMapping = iMapping.getOperationByWSDLName(operation.getName());
      ExtendedBindingOperation extBindingOp = null;
      ExtendedPorttypeOperation extPortOp = null;
      if (extBinding != null && isJaxWS()) {
        extBindingOp = (ExtendedBindingOperation) extBinding.getExtendedChild(new QName(operationMapping.getWSDLOperationName())); 
        extPortOp = (ExtendedPorttypeOperation) extPortType.getExtendedChild(new QName(operationMapping.getWSDLOperationName()));
      }      
      String style = operation.getProperty(SOAPBindingOperation.STYLE);
      String use = operation.getProperty(SOAPBindingOperation.USE);
      String soapAction = operation.getProperty(SOAPBindingOperation.SOAPACTION);
      if (soapAction != null) {
        operationMapping.setProperty(OperationMapping.SOAP_ACTION, soapAction);
      }
      operationMapping.setProperty(OperationMapping.OPERATION_STYLE, style);
      operationMapping.setProperty(OperationMapping.OPERATION_USE, use);            
      if (SOAPBinding.DOC_STYLE_VALUE.equals(style)) {
        // document(literal)
        String inHeaders = operation.getProperty(SOAPBindingOperation.IN_HEADERS);
        String outHeaders = operation.getProperty(SOAPBindingOperation.OUT_HEADERS);
        markHeaderParameters(inHeaders, outHeaders, operationMapping,extBindingOp);
        // Process mime attachments
        markAttachmentParameters(operation.getInputAttachmentsContainer(),operation.getOutputAttachmentsContainer(),operationMapping,extBindingOp);
        // Check WSI conformance.
        checkWSIConformance(style, operationMapping);        
        boolean docStylePossible = false;
        if (config.getGenerationMode() == ProxyGeneratorConfigNew.MIGRATION_MODE) {
          docStylePossible = isDocumentStylePossible640(wsOperation);
        } else {
          docStylePossible = isDocumentStylePossible(wsOperation);
        }
        boolean unWrapDocumentStyle = config.isUnwrapDocumentStyle();
        if (isJaxWS()) {
          unWrapDocumentStyle = config.isJaxWSDocUnwrap();
          if (extPortOp != null && extPortOp.hasWrapperStyleExtension()) {
            Boolean wrapperStyle = extPortOp.getWrapperStyle();
            unWrapDocumentStyle = wrapperStyle.booleanValue();
          }
        }
        if (docStylePossible && unWrapDocumentStyle) {
          // wrapper style document(literal) -> convert to rpc literal
          OperationMapping originalOperation = unWrapDocumentOperation(operationMapping,extPortOp);
          operationMapping.setProperty(OperationMapping.OPERATION_STYLE, SOAPBinding.RPC_STYLE_VALUE);
          if (originalOperation != null && config.getGenerationMode() == ProxyGeneratorConfigNew.MIGRATION_MODE) {
            originalOperation.setWSDLOperationName(originalOperation.getWSDLOperationName()+" ");
            originalOperation.setJavaMethodName(originalOperation.getJavaMethodName()+" ");
            iMapping.addOperation(originalOperation);
          }
        } else { // Fill soap request wrapper and soap response wrapper
          int inputParamIndex = -1;
          int outputParamIndex = -1;
          ParameterMapping[] inParams = operationMapping.getParameters(ParameterMapping.IN_TYPE);
          for (int j = 0; j < inParams.length; j++) {
            if (inParams[j].isHeader() == false && inParams[j].isAttachment() == false) {
              // Body parameter
              if (inputParamIndex >= 0) {
                inputParamIndex = -1;
                break;
              } else {
                inputParamIndex = j;
              }
            }
          }
          ParameterMapping[] outParams = operationMapping.getParameters(ParameterMapping.OUT_TYPE);
          for (int j = 0; j < outParams.length; j++) {
            if (outParams[j].isHeader() == false && outParams[j].isAttachment() == false) {
              // Body parameter
              if (outputParamIndex >= 0) {
                outputParamIndex = -1;
                break;
              } else {
                outputParamIndex = j;
              }
            }
          }
          if (inputParamIndex >= 0) {
            QName elementName = inParams[inputParamIndex].getSchemaQName();
            operationMapping.setProperty(OperationMapping.SOAP_REQUEST_WRAPPER, elementName.getLocalPart());
            operationMapping.setProperty(OperationMapping.INPUT_NAMESPACE, elementName.getNamespaceURI());
          }
          if (outputParamIndex >= 0) {
            QName elementName = outParams[outputParamIndex].getSchemaQName();
            operationMapping.setProperty(OperationMapping.SOAP_RESPONSE_WRAPPER, elementName.getLocalPart());
            operationMapping.setProperty(OperationMapping.OUTPUT_NAMESPACE, elementName.getNamespaceURI());
          }
        }
      } else {        
        // rpc(literal|encoded)
        String requestNS = operation.getProperty(SOAPBindingOperation.INPUT_NAMESPACE);
        String responseNS = operation.getProperty(SOAPBindingOperation.OUTPUT_NAMESPACE);
        String inHeaders = operation.getProperty(SOAPBindingOperation.IN_HEADERS);
        String outHeaders = operation.getProperty(SOAPBindingOperation.OUT_HEADERS);
        if (outHeaders == null) {
          outHeaders = "";
        }
        if (inHeaders == null) {
          inHeaders = "";
        }
        if (responseNS == null) {
          responseNS = "";
        }
        if (requestNS == null) {
          requestNS = "";
        }
        markHeaderParameters(inHeaders, outHeaders, operationMapping,extBindingOp);
        // Process mime attachments
        markAttachmentParameters(operation.getInputAttachmentsContainer(),operation.getOutputAttachmentsContainer(),operationMapping,extBindingOp);    
        // Check WSI conformance.
        checkWSIConformance(style, operationMapping);              
        operationMapping.setProperty(OperationMapping.INPUT_NAMESPACE, requestNS);
        operationMapping.setProperty(OperationMapping.OUTPUT_NAMESPACE, responseNS);
        if (SOAPBindingOperation.USE_ENCODED.equals(use)) {
          // rpc(encoded)
          String inStyle = operation.getProperty(SOAPBindingOperation.INPUT_ENCODINGSTYLE);
          String outStyle = operation.getProperty(SOAPBindingOperation.OUTPUT_ENCODINGSTYLE);
          if (outStyle == null) {
            outStyle = "";
          }
          operationMapping.setProperty(OperationMapping.IN_ENCODING_STYLE, inStyle);
          operationMapping.setProperty(OperationMapping.OUT_ENCODING_STYLE, outStyle);
          /*
          ParameterMapping[] faults = operationMapping.getParameters(ParameterMapping.FAULT_TYPE);
          ObjectList faultParameters = wsOperation.getParameters(Parameter.FAULT);
          for (int j=0; j<faults.length; j++) {
            Parameter parameter = (Parameter) faultParameters.item(j);
            String faultName = parameter.getProperty("fault-name");
            String faultNamespace = operation.getProperty("encoded-fault-ns-prefix:"+faultName);
            QName faultQName = faults[j].getFaultElementQName();
            if (faultQName.getNamespaceURI() == null || faultQName.getNamespaceURI().length() == 0) {
              faultQName = new QName(faultNamespace,faultQName.getLocalPart());
              faults[j].setFaultElementQName(faultQName);
            }
          }*/
        }
      }      
    }
  }

  private void processHTTPBinding(HTTPBinding binding, InterfaceMapping interfaceMapping) throws ProxyGeneratorException {
    String httpRequestMethod = binding.getProperty(HTTPBinding.HTTP_METHOD);
    interfaceMapping.setHTTPRequestMethod(httpRequestMethod);
    ObjectList bindingOperations = binding.getOperations();
    for(int i = 0; i < bindingOperations.getLength(); i++) {
      HTTPBindingOperation bindingOperation = (HTTPBindingOperation)(bindingOperations.item(i));
      OperationMapping operationMapping = interfaceMapping.getOperationByWSDLName(bindingOperation.getName());
      initHTTPOperationMapping(operationMapping, bindingOperation);
    }    
    checkHTTPBindingOperations(interfaceMapping);
  }
  
  private void initHTTPOperationMapping(OperationMapping operationMapping, HTTPBindingOperation bindingOperation) {
    operationMapping.setHTTPLocation(bindingOperation.getProperty(HTTPBindingOperation.LOCATION));
    operationMapping.setHTTPInputSerializationType(bindingOperation.getProperty(HTTPBindingOperation.INPUT_SERIALIZATION));
  }
  
  private void checkHTTPBindingOperations(InterfaceMapping interfaceMapping) throws ProxyGeneratorException {
    OperationMapping[] operationMappings = interfaceMapping.getOperation();
    for(int i = 0; i < operationMappings.length; i++) {
      checkHTTPBindingOperation(interfaceMapping, operationMappings[i]);
    }
  }
  
  private void checkHTTPBindingOperation(InterfaceMapping interfaceMapping, OperationMapping operationMapping) throws ProxyGeneratorException {
    ParameterMapping[] paramMappings = operationMapping.getParameter();
    for(int i = 0; i < paramMappings.length; i++) {
      ParameterMapping paramMapping = paramMappings[i];
      int paramType = paramMapping.getParameterType();
      switch(paramType) {
        case(ParameterMapping.IN_TYPE) : {
          if(paramMapping.isAttachment() || paramMapping.isElement() || paramMapping.isHeader()) {
            throw new ProxyGeneratorException(ProxyGeneratorException.HTTP_BINDING_WRONG_IN_PARAMETER, new Object[]{interfaceMapping.getBindingQName(), interfaceMapping.getPortType(), operationMapping.getWSDLOperationName(), paramMapping.getWSDLParameterName()}); 
          }
          break;
        }
        case(ParameterMapping.RETURN_TYPE) : {
          if(!paramMapping.isElement()) {
            throw new ProxyGeneratorException(ProxyGeneratorException.HTTP_BINDING_WRONG_RETURN_PARAMETER, new Object[]{interfaceMapping.getBindingQName(), interfaceMapping.getPortType(), operationMapping.getWSDLOperationName(), paramMapping.getWSDLParameterName()}); 
          }
          break;
        }
        case(ParameterMapping.FAULT_TYPE) : {
          break;          
        }
        default : {
          throw new ProxyGeneratorException(ProxyGeneratorException.HTTP_BINDING_WRONG_PARAMETER, new Object[]{interfaceMapping.getBindingQName(), interfaceMapping.getPortType(), operationMapping.getWSDLOperationName(), paramMapping.getWSDLParameterName()}); 
        }
      }
    }
  }

  /**
   * Checks if the wsdl meets the WSI requirements.
   * @param style
   * @param wsOperation
   * @param bindingOperation
   * @throws ProxyGeneratorException
   */
  private void checkWSIConformance(String style, OperationMapping oMapping) throws ProxyGeneratorException{
    ParameterMapping[] parameters = oMapping.getParameters(ParameterMapping.IN_OUT_TYPE+ParameterMapping.IN_TYPE+ParameterMapping.OUT_TYPE+ParameterMapping.RETURN_TYPE);
    for (ParameterMapping parameter : parameters) {
      
       // These are checked in the parser.
       if (parameter.isAttachment() || parameter.isHeader()){
         continue;
       }
       else{
         if (SOAPBinding.DOC_STYLE_VALUE.equals(style) && (!parameter.isElement())){
           throw new ProxyGeneratorException(ProxyGeneratorException.BAD_DOC_LIT_WSDL);
         }
         else if (SOAPBinding.RPC_STYLE_VALUE.equals(style) && (parameter.isElement())){
           throw new ProxyGeneratorException(ProxyGeneratorException.BAD_RPC_LIT_WSDL);
         }
       }
    }//for
  }
        
  

}
