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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.Stub;
import javax.xml.rpc.soap.SOAPFaultException;

import com.sap.engine.interfaces.webservices.runtime.component.ClientComponentFactory;
import com.sap.engine.lib.schema.components.ComplexTypeDefinition;
import com.sap.engine.lib.schema.components.TypeDefinitionBase;
import com.sap.engine.services.webservices.espbase.client.dynamic.content.GenericObject;
import com.sap.engine.services.webservices.espbase.client.dynamic.content.ObjectFactory;
import com.sap.engine.services.webservices.espbase.client.dynamic.content.impl.GenericObjectImpl;
import com.sap.engine.services.webservices.jaxrpc.encoding.ExtendedTypeMapping;
import com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;
import com.sap.engine.services.webservices.jaxrpc.exceptions.ProxyGeneratorException;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaToJavaGenerator;
import com.sap.engine.services.webservices.jaxrpc.util.CodeGenerator;
import com.sap.engine.services.webservices.jaxrpc.util.NameConvertor;
import com.sap.engine.services.webservices.jaxrpc.util.PackageBuilder;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.dynamic.DynamicInterface;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.dynamic.OperationStructure;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.DefaultProviders;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.*;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding.MimeHttpBinding;
import com.sap.engine.services.webservices.wsdl.*;

/**
 * WSDL Proxy Generator. The real proxy generator that does the job of Proxy related stuff.
 *
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class ClassGenerator {

  protected WSDLDefinitions definitions = null;  // WSDLDefinitions structure
  protected SchemaToJavaGenerator schema = null; // Schema to java generator
  protected File workDir = null;
  protected String packageName = null;
  protected String holderPackage = null;        // Holder package name = "package.holders" JAX-RPC specification
  protected NameConvertor nameConvertor = null; // Basic util class responsible for name convertion
  protected File packageDir = null; // Output package dir
  protected ArrayList holders;      // List of all holders needed to generate
  protected HolderGenerator holderGenerator; // Gode generator for holder classes
  private ClientTransportBinding[] bindings; // List of bindings
  protected TypeMappingRegistryImpl registry;// TypeMapping registry
  private boolean isRegistryUsed = false;    // true if type Mapping registry is used
  private String registryPath;               // the path to the type mapping registry
  private String typeMappingFile = "types.xml";  // Default type mapping registry file
  private String logicalPortFile = "lports.xml"; // Default logical ports file
  private String protocolsFile = "protocols.txt";// Default property file from where standalone stub will load protocols
  private String lPortOutputPath = null;
  private ArrayList exceptionMessages; //  All Exception messages thrown by webservices
  private ArrayList files; // All Files generated
  private ArrayList seiGenerated; // ALL SEI-s generated
  private ArrayList serviceInterfaces; // All service interfaces generated
  private boolean showWarnings = false;
  private LogicalPortFactory lpFactory = new LogicalPortFactory();
  private String defaultServiceName = "DefaultService";
  private String customServiceName = null;
  private QName isolatePortType = null;
  private String schemaFrameworkPath = null;
  private boolean genericMode = false;
  private HashMap schemaToJavaMapping = null;
  
  /**
   * Method for specification of external SchemaToJava mapping.
   * @param schemaToJavaMapping
   */
  public void setSchemaToJavaMapping(HashMap schemaToJavaMapping) {
    this.schemaToJavaMapping = schemaToJavaMapping;
  }

  public void setGenericMode(boolean flag) {
    this.genericMode = flag;
  }

  private boolean alternativeMethods = false;
  private boolean containerMode = false;

  public void setContainerMode(boolean mode) {
    this.containerMode = mode;
  }


  private boolean generateGetPorts = false;
  private QNameManager portManager = new QNameManager();
  private QNameManager stubManager = new QNameManager();
  private Properties uriToPackageMapping = null; // This is used to specify package mapping
  // @todo remove this declarations may not be needed in the future.
  //private Hashtable portQNameToJava = new Hashtable(); // Mapping from port qName to Java interface name
  //private HashSet processedPortTypes = new HashSet();

  /**
   * Generate or not getXXX JAX-RPC alike methods in service interface.
   * @param flag
   */
  public void setUseGetPortMethods(boolean flag) {
    this.generateGetPorts = flag;
  }

  public void setSchemaFrameworkPath(String schemaFrameworkPath) {
    this.schemaFrameworkPath = schemaFrameworkPath;
  }

  public String getSchemaFrameworkPath() {
    return this.schemaFrameworkPath;
  }

  /**
   * Set's custom service name.
   * @param serviceName
   */
  public void setCustomServiceName(String serviceName) {
    this.customServiceName = serviceName;
  }

  public void setIsolatedPortType(QName portType) {
    this.isolatePortType = portType;
  }
  /**
   * Set's the generator to generate or not generate rpc style methods for document style methods.
   * @param flag
   */
  public void setAlternativeMethods(boolean flag) {
    this.alternativeMethods = flag;
  }

  /**
   * Sets default logical port file name.
   * @param newLPName
   */
  public void setLogicalPortName(String newLPName) {
    this.logicalPortFile = newLPName;
  }

  /**
   * Call this to cause generator show warnings.
   */
  public void showWarings() {
    this.showWarnings = true;
  }

  /**
   * Call this to hide warnings.
   */
  public void hideWarning() {
    this.showWarnings = false;
  }

  /**
   * Default constructor
   */
  public ClassGenerator() {
    nameConvertor = new NameConvertor();
    holders = new ArrayList();
    holderGenerator = new HolderGenerator(nameConvertor);
    exceptionMessages = new ArrayList();
    files = new ArrayList();
    seiGenerated = new ArrayList();
    serviceInterfaces = new ArrayList();
  }

  /**
   * Routine for object initialization
   */
  public void init(WSDLDefinitions definitions, File workDir, String packageName, ClientTransportBinding[] bindings) throws Exception {
    if (workDir.isDirectory() == false) {
      throw new Exception(" Parameter given is not a directory !");
    }
    this.definitions = definitions;
    this.definitions.loadSchemaInfo();
    this.schema = definitions.getSchemaInfo();
    this.workDir = workDir;
    this.packageName = packageName;
    this.customServiceName = null;
    this.holderPackage = "holders";
    if (packageName != null && packageName.length() != 0) {
      this.holderPackage = packageName + ".holders";
      this.packageDir = new File(workDir, nameConvertor.packageToPath(packageName));
    } else {
      this.packageDir = workDir;
    }
    this.containerMode = false;
    this.isolatePortType = null;
    this.packageDir.mkdirs();
    this.bindings = bindings;
    this.isRegistryUsed = false;
    this.registryPath = null;
    this.exceptionMessages.clear();
    this.files.clear();
    this.seiGenerated.clear();
    this.serviceInterfaces.clear();
    if (packageDir.isDirectory() == false) {
      throw new Exception(" Package name is not correct !");
    }
    this.nameConvertor.clear();
    // Name Collision managers
    portManager.clear();
    stubManager.clear();
  }

  /**
   * From this method you can set where the proxy generator to put logical port files.
   * @param outputPath
   */
  public void setLogicalPortPath(String outputPath) {
    this.lPortOutputPath = outputPath;
  }

  /**
   * Stops using Logical Port output path variable.
   */
  public void clearLogicalPortPath() {
    this.lPortOutputPath = null;
  }

  /**
   * Returns current Logical port output path set.
   * @return NULL if output path not set.
   */
  public String getLogicalPortPath() {
    return this.lPortOutputPath;
  }

  /**
   * Returns list of all generated files by the Schema generator.
   */
  public File[] getFileList() {
    File[] result = new File[files.size()];
    for (int i=0; i<result.length; i++) {
      result[i] = (File) files.get(i);
    }
    return result;
  }

  /**
   * Returns list of all generated SEI interfaces.
   * When generating stubs there are no SEI's generated.
   * @return
   */
  public File[] getGeneratedSeiList() {
    File[] result = new File[seiGenerated.size()];
    for (int i=0; i<result.length; i++) {
      result[i] = (File) seiGenerated.get(i);
    }
    return result;
  }

  /**
   * Returns list of all service interfaces generated.
   * Note ! When generating stubs or no service tag is found then there is no Service interfaces.
   * @return
   */
  public File[] getGeneratedServiceInterfaces() {
    File[] result = new File[serviceInterfaces.size()];
    for (int i=0; i<result.length; i++) {
      result[i] = (File) serviceInterfaces.get(i);
    }
    return result;
  }

  /**
   * Outputs binding context initialization.
   */
  public int operationBindingInitialization(String contextName, PropertyContext context, CodeGenerator output, int level, int maxlevel) {
    Enumeration keys = context.getProperyKeys();
    while (keys.hasMoreElements()) {
      String key =  (String) keys.nextElement();
      Object value = context.getProperty(key);
      if (value instanceof String) {
        output.addLine(contextName+".setProperty(\""+key+"\",\""+value+"\");");
      }
    }
    keys = context.getSubcontextKeys();
    if (keys.hasMoreElements()) {
      level = level+1;
      String newContextName = contextName+"X";
      int perm = maxlevel;
      if (level > maxlevel) {
        output.addLine("PropertyContext "+newContextName+";");
        perm = level;
      }
      while (keys.hasMoreElements()) {
        String key = (String) keys.nextElement();
        PropertyContext content = context.getSubContext(key);
        output.addLine(newContextName+" = "+contextName+".getSubContext(\""+key+"\");");
        int permX = operationBindingInitialization(newContextName, content, output, level, perm);
        if (permX > perm) {
          perm = permX;
        }
      }
      return perm;
    } else {
      return level;
    }
  }

  /**
   * Fills feature contents from sap feature to logical port feature. Existing properties are overlapped.
   * @param feature
   * @param sapFeature
   */
  private void fillFeature(FeatureType feature, SAPFeature sapFeature) {
    String provider =  DefaultProviders.getProvider(feature.getName());
    if (provider != null) {
      feature.setProvider(provider);
    }
    feature.setOriginal(true); // Sets original flag
    ArrayList sapProperties = sapFeature.getProperties();
    //PropertyType[] properties = new PropertyType[sapProperties.size()];
    for (int i=0; i<sapProperties.size(); i++) {
      PropertyType lproperty = new PropertyType();
      SAPProperty property = (SAPProperty) sapProperties.get(i);
      com.sap.engine.lib.xml.util.QName qname = property.getQname();
      lproperty.setName(qname.getLocalName());
      if (property.getOptions().size() !=0) {
        SAPOption option = (SAPOption) property.getOptions().get(0);
        lproperty.setValue(option.getValue());
      }
      feature.addProperty(lproperty);
    }
  }

  /**
   * Load Feature information from PortType. Needs only portType structure to load features.
   * @param lpTemplate
   * @param portType
   * @throws WSDLException
   */
  public void fillLogicalPortTemplate(LogicalPortType lpTemplate, WSDLPortType portType) throws WSDLException {
    if (lpTemplate.getName() == null) { // if template Name is not set then get port type name.
      lpTemplate.setName(portType.getName());
    }
    // Loading global features
    GlobalFeatures globalFeatures = null;
    if (lpTemplate.hasGlobalFeatures()) {
      globalFeatures = lpTemplate.getGlobalFeatures();
    }
    ArrayList global = portType.getUseFeatures();
    for (int i=0; i<global.size(); i++) {
      SAPUseFeature useFeature = (SAPUseFeature) global.get(i);
      SAPFeature sapFeature = definitions.getFeatureByName(useFeature.getFeatureQName());
      if (sapFeature == null) {
        throw new WSDLException(" Unrecognized WSDL feature '"+useFeature.getFeature()+"' !");
      }
      FeatureType feature = null;
      if (globalFeatures != null) {
        feature = globalFeatures.getFeature(sapFeature.getUri());
      } else { // if there are no global features create it
        globalFeatures = new GlobalFeatures();
        lpTemplate.setGlobalFeatures(globalFeatures);
      }
      if (feature == null) {
        feature = new FeatureType();
        feature.setName(sapFeature.getUri());
      }
      fillFeature(feature, sapFeature);
      globalFeatures.addFeature(feature);
    }
    // Loads local features
    LocalFeatures localFeatures = lpTemplate.getLocalFeatures();
    if (localFeatures == null) {
      localFeatures = new LocalFeatures();
      lpTemplate.setLocalFeatures(localFeatures);
    }
    ArrayList operations = portType.getOperations();
    for (int i=0; i<operations.size(); i++) {
      WSDLOperation operation = (WSDLOperation) operations.get(i);
      OperationType loperation = localFeatures.getOperation(operation.getName());
      if (loperation == null) {
        loperation = new OperationType();
        loperation.setName(operation.getName());
        localFeatures.addOperation(loperation);
      }
      if (operation.getUseFeatures().size() !=0) {
        ArrayList ouseFeatures = operation.getUseFeatures();
        //FeatureType[] features = new FeatureType[ouseFeatures.size()];
        for (int j=0; j<ouseFeatures.size(); j++) {
          SAPUseFeature sapUseFeature = (SAPUseFeature) ouseFeatures.get(j);
          SAPFeature sapFeature = definitions.getFeatureByName(sapUseFeature.getFeatureQName());
          if (sapFeature == null) {
            throw new WSDLException(" Unrecognized WSDL feature '"+sapUseFeature.getFeature()+"' !");
          }
          FeatureType feature = loperation.getFeature(sapFeature.getUri());
          if (feature == null) {
            feature = new FeatureType();
            feature.setName(sapFeature.getUri());
          }
          fillFeature(feature , sapFeature);
          loperation.addFeature(feature);
        }
      }
    }
  }

  /**
   * Load's runtime features from Binding and service.
   * @param stubName
   * @param endpoint
   * @param binding
   * @param lport
   * @throws WSDLException
   */
  public void fillLogicalPort(String stubName, String endpoint, WSDLBinding binding, LogicalPortType lport, ClientTransportBinding tbinding) throws WSDLException {
    // Some of this params may not be set
    if (endpoint != null) {
      lport.setEndpoint(endpoint);
    }
    if (stubName != null) {
      lport.setStubName(stubName);
    }
    // Template name is got from Binding
    if (lport.getName() == null) {
      lport.setName(binding.getName());
    }
    // Loading global features
    GlobalFeatures globalFeatures = null;
    if (lport.hasGlobalFeatures()) {
      globalFeatures = lport.getGlobalFeatures();
    }
    if (globalFeatures == null) {
      globalFeatures = new GlobalFeatures();
    }
    tbinding.importGlobalFeatures(globalFeatures, binding);
    ArrayList global = binding.getUseFeatures();
    for (int i=0; i<global.size(); i++) {
      SAPUseFeature useFeature = (SAPUseFeature) global.get(i);
      SAPFeature sapFeature = definitions.getFeatureByName(useFeature.getFeatureQName());
      if (sapFeature == null) {
        throw new WSDLException(" Unrecognized WSDL feature '"+useFeature.getFeature()+"' !");
      }
      FeatureType feature = null;
      if (globalFeatures != null) {
        feature = globalFeatures.getFeature(sapFeature.getUri());
      } else { // if there are no global features create it
        globalFeatures = new GlobalFeatures();
        lport.setGlobalFeatures(globalFeatures);
      }
      if (feature == null) {
        feature = new FeatureType();
        feature.setName(sapFeature.getUri());
      }
      fillFeature(feature, sapFeature);
      globalFeatures.addFeature(feature);
    }
    lport.setGlobalFeatures(globalFeatures);

    // Loads local features
    LocalFeatures localFeatures = lport.getLocalFeatures();
    if (localFeatures == null) {
      localFeatures = new LocalFeatures();
      lport.setLocalFeatures(localFeatures);
    }
    ArrayList operations = binding.getOperations();
    for (int i=0; i<operations.size(); i++) {
      WSDLBindingOperation operation = (WSDLBindingOperation) operations.get(i);
      OperationType loperation = localFeatures.getOperation(operation.getName());
      if (loperation == null) {
        loperation = new OperationType();
        loperation.setName(operation.getName());
        localFeatures.addOperation(loperation);
      }
      if (operation.getUseFeatures().size() !=0) {
        ArrayList ouseFeatures = operation.getUseFeatures();
        for (int j=0; j<ouseFeatures.size(); j++) {
          SAPUseFeature sapUseFeature = (SAPUseFeature) ouseFeatures.get(j);
          SAPFeature sapFeature = definitions.getFeatureByName(sapUseFeature.getFeatureQName());
          if (sapFeature == null) {
            throw new WSDLException(" Unrecognized WSDL feature '"+sapUseFeature.getFeature()+"' !");
          }
          FeatureType feature = loperation.getFeature(sapFeature.getUri());
          if (feature == null) {
            feature = new FeatureType();
            feature.setName(sapFeature.getUri());
          }
          fillFeature(feature , sapFeature);
          loperation.addFeature(feature);
        }
      }
    }
  }

  /**
   * Loads and generates schema java representation. For internal use.
   */
  private void generateSchema() throws Exception {
    if (this.schema == null) {
      // No schema is specified
      this.schema = new SchemaToJavaGenerator();
      this.schema.setPackageBuilder(new PackageBuilder());
      if (this.genericMode) {
        this.schema.setUnwrapArrays(false);
        this.schema.setGenericFrm(true);
      }      
      this.schema.prepareAll(packageName);
    } else {
      // If mirror image is used then set information to schema generator.
      this.schema.setMirrorLocations(definitions.getMirrorLocations());
      this.schema.setMirrorMapping(definitions.getMirrorMapping());
      if (this.genericMode) {
        this.schema.setUnwrapArrays(false);
        this.schema.setGenericFrm(true);
      }
      if (uriToPackageMapping != null) {
        schema.setUriToPackagetMapping(uriToPackageMapping);
      }
      if (this.schemaToJavaMapping != null) {
        schema.setApplicationSchemaMapping(this.schemaToJavaMapping);
      }
      String typesPackage = "types";
      if (packageName != null && packageName.length()!=0) {
        typesPackage = packageName+"."+typesPackage;
      }
      File outputDir = workDir;
      if (this.schemaFrameworkPath != null) { // Separate Schema framework
        outputDir = new File(this.schemaFrameworkPath);
        outputDir.mkdirs();
        this.schema.generateAll(outputDir, typesPackage);
      } else {
        this.schema.setContainerMode(this.containerMode);
        this.schema.generateAll(workDir, typesPackage);
      }
      if (containerMode == false || this.schemaFrameworkPath != null) {
        if (genericMode) {
          registry = new TypeMappingRegistryImpl(TypeMappingRegistryImpl.GENERIC,null,null);
        } else {
          registry = new TypeMappingRegistryImpl();
        }
        this.schema.registerTypes((ExtendedTypeMapping) registry.getDefaultTypeMapping());
        String outputFileName = typeMappingFile;
        if (packageName != null && packageName.length()!=0) {
          outputFileName = nameConvertor.packageToPath(packageName)+File.separatorChar+typeMappingFile;
        }
        File outputxml = new File(outputDir,outputFileName);
        files.add(outputxml);
        registry.toXmlFile(outputxml);
        registry = null;
        isRegistryUsed = true;
        if (packageName != null && packageName.length()!=0) {
          registryPath = escapeString(packageName+".")+typeMappingFile;
        } else {
          registryPath = typeMappingFile;
        }
        File[] filesArr = schema.getFileList();
        for (int i=0; i<filesArr.length; i++) {
          files.add(filesArr[i]);
        }
      }
    }
  }

  /**
   * Prints waring message to Syste.out
   * @param warningMessage
   */
  private void printWarining(String warningMessage) {
    if (showWarnings) {
      System.out.println("Waring : "+warningMessage);//$JL-SYS_OUT_ERR$
    }
  }

  private String getLPath() {
    String lpath;
    if (packageName != null && packageName.length() != 0) {
      lpath = escapeString(packageName+".")+logicalPortFile;
    } else {
      lpath = logicalPortFile;
    }
    return lpath;
  }

  private File getLPFile() {
    if (lPortOutputPath != null) { // lpPath specified
      return new File(lPortOutputPath,logicalPortFile);
    } else {
      String lpath;
      if (packageName != null && packageName.length() != 0) {
        lpath = escapeString(packageName+".")+logicalPortFile;
      } else {
        lpath = logicalPortFile;
      }

      return new File(workDir,lpath);
    }
  }

  private boolean checkPortType(WSDLPortType portType) {
    if (this.isolatePortType != null) { // If isolated portType set by pass others
      if (!isolatePortType.getLocalPart().equals(portType)) {
        return false;
      } else {
        if (isolatePortType.getNamespaceURI() != null) {
          if (!isolatePortType.getNamespaceURI().equals(portType.getNamespace())) {
            return false;
          }
        } else if (portType.getNamespace() != null) {
          return false;
        }
      }
    }
    return true;
  }
  /**
   * Generates only interfaces. Uses all port types available.
   * Returns logical port templates loaded prom extensions in WSDLPortType.
   */
  public void generateInterfaces() throws Exception {
    if (definitions.services.size() > 1) {
      printWarining("More than one service in wsdl is not supported so only first service will be used !");
    }
    if (definitions.services.size() == 0) {
      printWarining("No service tag found. Switching to binding mapping mode.");
    }
    generateSchema(); // Generating Schema types
    if (definitions.services.size() != 0) { // Service tag found get first service tag by default
      WSDLService service = (WSDLService) definitions.services.get(0);
      if (service.getPorts().size() == 0) {
        throw new Exception(" At least one port of the service must be specified !");
      }
      ArrayList ports = service.getPorts();
      LogicalPorts lports = new LogicalPorts();
      if (this.customServiceName != null) {
        lports.setName(this.customServiceName);
      } else {
        lports.setName(service.getName());
      }
      for (int i = 0; i < ports.size(); i++) {
        WSDLPort port = (WSDLPort) ports.get(i);
        WSDLBinding portbinding = definitions.getBinding(port.getBinding());
        if (portbinding == null) {
          throw new Exception(" Port points to unknown Binding " + port.getBinding().getName());
        }
        // Logical port initialization
        LogicalPortType lport = new LogicalPortType();
        lport.setName(port.getName());
        lport.setBindingName(port.getBinding().getLocalName());
        lport.setBindingUri(port.getBinding().getURI());
        ClientTransportBinding bindingImplementation = getBindingImplementation(portbinding);
        if (bindingImplementation == null) {
          printWarining("No available binding implementation can recognize: "+port.getBinding().getName()+" binding not used.");
        } else {
          String outputAddress = bindingImplementation.loadAddress(port.getExtension());
          WSDLPortType portType = this.definitions.getPortType(portbinding.getType().getName(),portbinding.getType().getURI());
          if (!checkPortType(portType)) {
            continue;
          }
          if (portType == null) {
            throw new Exception("PortType with name " + portbinding.getType().getName() + " not found !");
          }
          lport = generateSDI(portType, portbinding, lport);
          lport.setBindingImplementation(bindingImplementation.getName());
          fillLogicalPort(null,outputAddress,portbinding,lport,bindingImplementation);
          lports.addLogicalPort(lport);
        }
      }
      // Generate Holder Classes
      File[] fileArr = holderGenerator.generateHolders(workDir, holderPackage, holders);
      for (int i=0; i<fileArr.length; i++) {
        files.add(fileArr[i]);
      }
      // Generate Exception Classes
      generateExceptions(exceptionMessages);
      generateServiceInterfaces(lports,generateGetPorts);
      File lpFile = getLPFile();
      files.add(lpFile);
      lpFactory.saveLogicalPorts(lports,lpFile);
    } else { // No Service tag found try bindings as logical ports
      ArrayList bindings = definitions.getBindings();
      if (bindings.size() != 0) { // Bindings found work with them. May set endpoint runtime and properly work
        LogicalPorts lports = new LogicalPorts();
        if (this.customServiceName != null) {
          lports.setName(this.customServiceName);
        } else {
          lports.setName(definitions.getName());
          if (lports.getName() == null) {
            lports.setName(defaultServiceName);
          }
        }
        for (int i = 0; i < bindings.size(); i++) {
          WSDLBinding portbinding = (WSDLBinding) bindings.get(i);
          // Logical port initialization
          LogicalPortType lport = new LogicalPortType();
          lport.setName(portbinding.getName());
          lport.setBindingName(portbinding.getName());
          lport.setBindingUri(portbinding.getNamespace());
          ClientTransportBinding bindingImplementation = getBindingImplementation(portbinding);
          if (bindingImplementation == null) {
            throw new Exception(" No available binding can recognize this binding : "+portbinding.getName());
          }
          WSDLPortType portType = this.definitions.getPortType(portbinding.getType().getName(),portbinding.getType().getURI());
          if (!checkPortType(portType)) {
            continue;
          }
          if (portType == null) {
            throw new Exception("PortType with name " + portbinding.getType().getName() + " not found !");
          }
          lport = generateSDI(portType, portbinding, lport);
          lport.setBindingImplementation(bindingImplementation.getName());
          fillLogicalPort(null,null,portbinding,lport,bindingImplementation);
          lports.addLogicalPort(lport);
        }
        // Generate Holder Classes
        File[] fileArr = holderGenerator.generateHolders(workDir, holderPackage, holders);
        for (int i=0; i<fileArr.length; i++) {
          files.add(fileArr[i]);
        }
        // Generate Exception classes
        generateExceptions(exceptionMessages);
        generateServiceInterfaces(lports, false);
//        generateServiceInterface(lports.getName(),);
        File lpFile = getLPFile();
        files.add(lpFile);
        lpFactory.saveLogicalPorts(lports,lpFile);
      } else { // No bindings available create only interfaces
        printWarining("No binding found. Switching to portType mapping mode.");
        LogicalPorts lports = new LogicalPorts();
        if (this.customServiceName!= null) {
          lports.setName(this.customServiceName);
        } else {
          lports.setName(definitions.getName());
          if (lports.getName() == null) {
            lports.setName(defaultServiceName);
          }
        }
        ArrayList portTypes = definitions.getPortTypes();
        if (portTypes.size() == 0) {
          throw new Exception(" No PortTypes found. This WSDL is completly unusable !");
        }
        for  (int i=0; i<portTypes.size(); i++) {
          WSDLPortType portType = (WSDLPortType) portTypes.get(i);
          if (!checkPortType(portType)) {
            continue;
          }
          generateSDI(portType, null, null);
        }
        // Generate Holder Classes
        File[] fileArr = holderGenerator.generateHolders(workDir, holderPackage, holders);
        for (int i=0; i<fileArr.length; i++) {
          files.add(fileArr[i]);
        }
        // Generate Exception classes
        generateExceptions(exceptionMessages);
        generateServiceInterfaces(lports, false);
        File lpFile = getLPFile();
        files.add(lpFile);
        lpFactory.saveLogicalPorts(lports,lpFile);
      }
    }
  }

  /**
   * Prepares and initializes internal schema information without generating java files.
   */
  private void prepareSchema() throws Exception {
    String typesPackage = "types";
    if (packageName!= null && packageName.length()!=0) {
      typesPackage = packageName+"."+typesPackage;
    }
    if (this.schema == null) {
      // No schema presents in the WSDL
      this.schema = new SchemaToJavaGenerator();
      this.schema.setPackageBuilder(new PackageBuilder());
      if (this.uriToPackageMapping != null) {
        schema.setUriToPackagetMapping(this.uriToPackageMapping);
      }
      if (this.genericMode) {
        this.schema.setUnwrapArrays(false);
        this.schema.setGenericFrm(true);
      }
      this.schema.prepareAll(typesPackage);
    } else {
      this.schema.setContainerMode(this.containerMode);
      this.schema.setMirrorLocations(definitions.getMirrorLocations());
      this.schema.setMirrorMapping(definitions.getMirrorMapping());
      if (this.genericMode) {
        this.schema.setUnwrapArrays(false);
        this.schema.setGenericFrm(true);
      }
      if (this.schemaToJavaMapping != null) {
        this.schema.setApplicationSchemaMapping(schemaToJavaMapping);
      }
      if (schema.isLoaded() == false || uriToPackageMapping!=null) {
        if (this.uriToPackageMapping != null) {
          schema.setUriToPackagetMapping(this.uriToPackageMapping);
        }
        this.schema.prepareAll(typesPackage);
        isRegistryUsed = true;
        if (packageName != null && packageName.length()!=0) {
          registryPath = escapeString(packageName+".")+typeMappingFile;
        } else {
          registryPath = typeMappingFile;
        }
      }
    }
  }

  /**
   * Returns default schema uri to java mapping.
   * @return
   * @throws Exception
   */
  public Properties getDefaultSchemaMapping() throws Exception {
    prepareSchema();
    this.uriToPackageMapping = this.schema.getUriToPackageMapping();
    return this.uriToPackageMapping;
  }

  /**
   * Routine to set schema package mapping
   * @param defaultMapping
   * @throws Exception
   */
  public void setDefaultSchemaMapping(Properties defaultMapping) throws Exception {
    this.uriToPackageMapping = defaultMapping;
  }
  /**
   * Generates stubs from logical ports. Updates information in logical ports.
   * This should be used on server side to fill missing files in Proxies.
   */
  public LogicalPorts generateStubs(LogicalPorts logicalPorts) throws Exception {
    prepareSchema();
    LogicalPortType[] ports = logicalPorts.getLogicalPort(); // Gets selected logical ports
    for (int i=0; i<ports.length; i++) {
      String bindingName = ports[i].getBindingName();
      String bindingNamespace = ports[i].getBindingUri();
      WSDLBinding portbinding = definitions.getBinding(bindingName,bindingNamespace);
      if (portbinding == null) {
        throw new Exception(" Logical Port ["+ports[i].getName()+"] points to unavailable binding {" + bindingNamespace +"}"+bindingName);
      }
      ClientTransportBinding bindingImplementation = getBindingImplementation(portbinding);
      if (bindingImplementation == null) {
        throw new Exception(" No available binding implementation can recognize this binding : "+ports[i].getBindingName());
      }
      String outputAddress = ports[i].getEndpoint();
      WSDLPortType portType = this.definitions.getPortType(portbinding.getType().getName(),portbinding.getType().getURI());
      if (portType == null) {
        throw new Exception("PortType with name {"+portbinding.getType().getURI()+"}"+ portbinding.getType().getName() + " not found !");
      }
      stubGenerate(portType, portbinding, outputAddress, bindingImplementation, ports[i],false);
    }
    return logicalPorts;
  }

  /**
   * Generates server hosted webservice client implementation.
   * @param logicalPorts
   * @param serverHosted
   * @return
   * @throws Exception
   */
  public LogicalPorts generateImplementation(LogicalPorts logicalPorts, boolean serverHosted) throws Exception {
    if (containerMode && schemaFrameworkPath != null) {
      generateSchema();
    } else {
      prepareSchema();
    }
    LogicalPortType[] ports = logicalPorts.getLogicalPort(); // Gets selected logical ports
    for (int i=0; i<ports.length; i++) {
      String bindingName = ports[i].getBindingName();
      String bindingNamespace = ports[i].getBindingUri();
      WSDLBinding portbinding = definitions.getBinding(bindingName,bindingNamespace);
      if (portbinding == null) {
        throw new Exception(" Logical Port ["+ports[i].getName()+"] points to unavailable binding {" + bindingNamespace +"}"+bindingName);
      }
      ClientTransportBinding bindingImplementation = getBindingImplementation(portbinding);
      if (bindingImplementation == null) {
        throw new Exception(" No available binding implementation can recognize this binding : {"+ports[i].getBindingUri()+"}"+ports[i].getBindingName());
      }
      String outputAddress = ports[i].getEndpoint();
      WSDLPortType portType = this.definitions.getPortType(portbinding.getType().getName(),portbinding.getType().getURI());
      if (portType == null) {
        throw new Exception(" PortType with qname {"+portbinding.getType().getURI()+"}"+ portbinding.getType().getName() + " not found !");
      }
      stubGenerate(portType, portbinding, outputAddress, bindingImplementation, ports[i],serverHosted);
    }
    generateServiceImpl(logicalPorts,null,serverHosted);
    return logicalPorts;
  }

  /**
   * Generates only stubs using wsdl. Service tag must present.
   */
  public LogicalPorts generateStubs() throws Exception {
    if (definitions.services.size() == 0) {
      throw new Exception(" At least one service must be defined to generate Proxy !");
    }

    if (definitions.services.size() > 1) {
      throw new Exception(" More than one service at a time not supported yet !");
    }
    prepareSchema();
    // gets the service name and generates the directory to work
    WSDLService service = (WSDLService) definitions.services.get(0);
    if (service.getPorts().size() == 0) {
      throw new Exception("At least one port of the service must be specified !");
    }
    ArrayList ports = service.getPorts();
    LogicalPorts lports = new LogicalPorts();
    lports.setName(service.getName());
    for (int i = 0; i < ports.size(); i++) {
      WSDLPort port = (WSDLPort) ports.get(i);
      WSDLBinding portbinding = definitions.getBinding(port.getBinding());
      if (portbinding == null) {
        throw new Exception(" Port points to unknown Binding " + port.getBinding().getName());
      }
      // Logical port initialization
      LogicalPortType lport = new LogicalPortType();
      lport.setName(port.getName());
      lport.setBindingName(port.getBinding().getLocalName());
      lport.setBindingUri(port.getBinding().getURI());
      ClientTransportBinding bindingImplementation = getBindingImplementation(portbinding);
      if (bindingImplementation == null) {
        throw new Exception(" No available implementation can recognize this binding : "+port.getBinding().getName());
      }
      String outputAddress = bindingImplementation.loadAddress(port.getExtension());
      WSDLPortType portType = this.definitions.getPortType(portbinding.getType().getName(),portbinding.getType().getURI());
      if (portType == null) {
        throw new Exception("PortType with name " + portbinding.getType().getName() + " not found !");
      }
      lport = stubGenerate(portType, portbinding, outputAddress, bindingImplementation, lport,false);
      lports.addLogicalPort(lport);
    }
    // Generate Holder Classes
    String lpath;
    if (packageName != null && packageName.length()!=0) {
      lpath = escapeString(packageName+".")+logicalPortFile;
    } else {
      lpath = logicalPortFile;
    }
    File lpFile = new File(workDir,lpath);
    LogicalPortFactory factory = new LogicalPortFactory();
    factory.saveLogicalPorts(lports,lpFile);
    generateServiceImpl(lports,lpath,false);
    return lports;
  }

  /**
   * Generates only logical port information without generating any other information.
   * @throws Exception
   */
  public void generateLogicalPorts() throws Exception {
    if (definitions.services.size() > 1) {
      //throw new Exception(" More than one service at a time not supported !");
    }
    if (definitions.services.size() == 0) {
      System.out.println(" Warning : No service tag found - switching to binding mapping mode !");
    }
    // Loading and generating Schema types.
    generateSchema();
    if (definitions.services.size() != 0) { // Service tag found
      WSDLService service = (WSDLService) definitions.services.get(0);
      if (service.getPorts().size() == 0) {
        throw new Exception("At least one port of the service must be specified !");
      }
      ArrayList ports = service.getPorts();
      LogicalPorts lports = new LogicalPorts();
      lports.setName(service.getName());
      for (int i = 0; i < ports.size(); i++) {
        WSDLPort port = (WSDLPort) ports.get(i);
        WSDLBinding portbinding = definitions.getBinding(port.getBinding());
        if (portbinding == null) {
          throw new Exception(" Port points to unknown Binding " + port.getBinding().getName());
        }
        // Logical port initialization
        LogicalPortType lport = new LogicalPortType();
        lport.setName(port.getName());
        lport.setBindingName(port.getBinding().getLocalName());
        lport.setBindingUri(port.getBinding().getURI());
        ClientTransportBinding bindingImplementation = getBindingImplementation(portbinding);
        if (bindingImplementation == null) {
          System.out.println(" No available binding implementation can recognize this binding : "+port.getBinding().getName());
        } else {
          String outputAddress = bindingImplementation.loadAddress(port.getExtension());
          WSDLPortType portType = this.definitions.getPortType(portbinding.getType().getName(),portbinding.getType().getURI());
          if (portType == null) {
            throw new Exception("PortType with name " + portbinding.getType().getName() + " not found !");
          }
          fillLogicalPortTemplate(lport, portType);
          lport.setBindingImplementation(bindingImplementation.getName());
          fillLogicalPort(null,outputAddress,portbinding,lport,bindingImplementation);
          lports.addLogicalPort(lport);
        }
      }
      File lpFile = getLPFile();
      files.add(lpFile);
      lpFactory.saveLogicalPorts(lports,lpFile);
    } else { // No Service tag found try bindings as logical ports
      ArrayList bindings = definitions.getBindings();
      if (bindings.size() != 0) { // Bindings found work with them. May set endpoint runtime and properly work
        LogicalPorts lports = new LogicalPorts();
        lports.setName(definitions.getName());
        if (lports.getName() == null) {
          lports.setName(defaultServiceName);
        }
        for (int i = 0; i < bindings.size(); i++) {
          WSDLBinding portbinding = (WSDLBinding) bindings.get(i);
          // Logical port initialization
          LogicalPortType lport = new LogicalPortType();
          lport.setName(portbinding.getName());
          lport.setBindingName(portbinding.getName());
          lport.setBindingUri(portbinding.getNamespace());
          ClientTransportBinding bindingImplementation = getBindingImplementation(portbinding);
          if (bindingImplementation == null) {
            throw new Exception(" No available binding implementation can recognize this binding : "+portbinding.getName());
          }
          WSDLPortType portType = this.definitions.getPortType(portbinding.getType().getName(),portbinding.getType().getURI());
          if (portType == null) {
            throw new Exception("PortType with name " + portbinding.getType().getName() + " not found !");
          }
          fillLogicalPortTemplate(lport, portType);
          lport.setBindingImplementation(bindingImplementation.getName());
          fillLogicalPort(null,null,portbinding,lport,bindingImplementation);
          lports.addLogicalPort(lport);
        }
        File lpFile = getLPFile();
        files.add(lpFile);
        lpFactory.saveLogicalPorts(lports,lpFile);
      } else { // No bindings available - Do not create nothing
      }
    }
  }
  /**
   * Generates all proxy files from wsdl. Tries to react intilligent on different WSDLs.
   * Actions taken :
   *  1.Service tag found - created complete WSDL Proxy.
   *  2.Service tag not coud - creates Logical Port for every binding recognized without endpoint set - LPTemplate
   *  3.No Service tag, no binding tags, only interfaces and LPTemplate
   */
  public void generateAll() throws Exception {
    if (definitions.services.size() > 1) {
      //throw new Exception(" More than one service at a time not supported !");
    }
    if (definitions.services.size() == 0) {
      System.out.println(" Warning : No service tag found - switching to binding mapping mode !");
    }
    // Loading and generating Schema types.
    generateSchema();
    if (definitions.services.size() != 0) { // Service tag found
      WSDLService service = (WSDLService) definitions.services.get(0);
      if (service.getPorts().size() == 0) {
        throw new Exception("At least one port of the service must be specified !");
      }
      ArrayList ports = service.getPorts();
      LogicalPorts lports = new LogicalPorts();
      if (this.customServiceName != null) {
        lports.setName(this.customServiceName);
      } else {
        lports.setName(service.getName());
      }
      for (int i = 0; i < ports.size(); i++) {
        WSDLPort port = (WSDLPort) ports.get(i);
        WSDLBinding portbinding = definitions.getBinding(port.getBinding());
        if (portbinding == null) {
          throw new Exception(" Port points to unknown Binding " + port.getBinding().getName());
        }
        // Logical port initialization
        LogicalPortType lport = new LogicalPortType();
        lport.setName(port.getName());
        lport.setBindingName(port.getBinding().getLocalName());
        lport.setBindingUri(port.getBinding().getURI());
        ClientTransportBinding bindingImplementation = getBindingImplementation(portbinding);
        if (bindingImplementation == null) {
          System.out.println(" No available binding can recognize this binding : "+port.getBinding().getName());
        } else {
          if (genericMode && !(bindingImplementation.getName().equals(MimeHttpBinding.NAME))) {
            // Generic mode supports only SOAP Binding - bypass ports that are unsupported
            continue;
          }
          WSDLPortType portType = this.definitions.getPortType(portbinding.getType().getName(),portbinding.getType().getURI());
          if (!checkPortType(portType)) {
            continue;
          }
          String outputAddress = bindingImplementation.loadAddress(port.getExtension());
          if (portType == null) {
            throw new Exception("PortType with name " + portbinding.getType().getName() + " not found !");
          }
          lport = generateSDI(portType, portbinding, lport);
          lport = stubGenerate(portType, portbinding, outputAddress, bindingImplementation, lport,false);
          lports.addLogicalPort(lport);
        }
      }
      // Generate Holder Classes
      holderGenerator.generateHolders(workDir, holderPackage, holders);
      // Generate Exception Classes
      generateExceptions(exceptionMessages);
      generateServiceInterfaces(lports,generateGetPorts);
      File lpFile = getLPFile();
      files.add(lpFile);
      generateServiceImpl(lports,getLPath(),false);
      lpFactory.saveLogicalPorts(lports,lpFile);
      File outputDir = workDir;
      if (packageName!= null && packageName.length() !=0) {
        outputDir = new File(workDir, nameConvertor.packageToPath(packageName));
      }
      File outputFile = new File(outputDir, protocolsFile);
      files.add(outputFile);
      Properties implementations = DefaultProviders.getProtocolImplementations();
      OutputStream outputFileStream = new FileOutputStream(outputFile);
      implementations.store(outputFileStream, "Default Protocol implementations");
      outputFileStream.close();
    } else { // No Service tag found try bindings as logical ports
      ArrayList bindings = definitions.getBindings();
      if (bindings.size() != 0) { // Bindings found work with them. May set endpoint runtime and properly work
        LogicalPorts lports = new LogicalPorts();
        if (this.customServiceName != null) {
          lports.setName(this.customServiceName);
        } else {
          lports.setName(definitions.getName());
          if (lports.getName() == null) {
            lports.setName(defaultServiceName);
          }
        }
        for (int i = 0; i < bindings.size(); i++) {
          WSDLBinding portbinding = (WSDLBinding) bindings.get(i);
          // Logical port initialization
          LogicalPortType lport = new LogicalPortType();
          lport.setName(portbinding.getName());
          lport.setBindingName(portbinding.getName());
          ClientTransportBinding bindingImplementation = getBindingImplementation(portbinding);
          if (genericMode && !(bindingImplementation.getName().equals(MimeHttpBinding.NAME))) {
            // Generic mode supports only SOAP Binding - bypass ports that are unsupported
            continue;
          }
          if (bindingImplementation == null) {
            throw new Exception(" No available binding can recognize this binding : "+portbinding.getName());
          }
          WSDLPortType portType = this.definitions.getPortType(portbinding.getType().getName(),portbinding.getType().getURI());
          if (!checkPortType(portType)) {
            continue;
          }
          if (portType == null) {
            throw new Exception("PortType with name " + portbinding.getType().getName() + " not found !");
          }
          lport = generateSDI(portType, portbinding, lport);
          lport = stubGenerate(portType, portbinding, null, bindingImplementation, lport,false);
          lports.addLogicalPort(lport);
        }
        // Generate Holder Classes
        holderGenerator.generateHolders(workDir, holderPackage, holders);
        // Generate Exception classes
        generateExceptions(exceptionMessages);
        generateServiceInterfaces(lports,false);
        File lpFile = getLPFile();
        files.add(lpFile);
        generateServiceImpl(lports,getLPath(),false);
        lpFactory.saveLogicalPorts(lports,lpFile);
        File outputDir = workDir;
        if (packageName!= null && packageName.length() !=0) {
          outputDir = new File(workDir, nameConvertor.packageToPath(packageName));
        }
        File outputFile = new File(outputDir, protocolsFile);
        files.add(outputFile);
        Properties implementations = DefaultProviders.getProtocolImplementations();
        OutputStream outputFileStream = new FileOutputStream(outputFile);
        implementations.store(outputFileStream, "Default Protocol implementations");
        outputFileStream.close();
      } else { // No bindings available create only interfaces
        if (genericMode) {
          throw new Exception("WSDL does not contain enough information to load WS Client.");
        }
        System.out.println(" Warning : No binding found - switching to portType mapping mode !");
        LogicalPorts lports = new LogicalPorts();
        if (this.customServiceName != null) {
          lports.setName(this.customServiceName);
        } else {
          lports.setName(definitions.getName());
          if (lports.getName() == null) {
            lports.setName(defaultServiceName);
          }
        }
        ArrayList portTypes = definitions.getPortTypes();
        if (portTypes.size() == 0) {
          throw new Exception(" No PortTypes found. This WSDL is completly unusable !");
        }
        LogicalPortType perm = null;
        for  (int i=0; i<portTypes.size(); i++) {
          WSDLPortType portType = (WSDLPortType) portTypes.get(i);
//          LogicalPortType lport = new LogicalPortType();
//          lport.setName(portType.getName());
          perm = generateSDI(portType, null, null);
//          lport = generateSDI(portType, null, lport);
//          lports.addLogicalPort(lport);
        }
        // Generate Holder Classes
        holderGenerator.generateHolders(workDir, holderPackage, holders);
        generateExceptions(exceptionMessages);
        lports.addLogicalPort(perm);
        generateServiceInterfaces(lports,false);
        lports.setLogicalPort(new LogicalPortType[0]);
        File lpFile = getLPFile();
        files.add(lpFile);
        lpFactory.saveLogicalPorts(lports,lpFile);
      }
    }
  }

  /**
   * Returns Binding implementation that recognizes this binding.
   */
  private ClientTransportBinding getBindingImplementation(WSDLBinding binding) {
    for (int i=0; i<bindings.length; i++) {
      if (bindings[i].recognizeBinding(binding) == true) { // If binding is recognized
        return bindings[i];
      }
    }
    return null;
  }

  /**
   * Generates WSDL proxy Service Endpoint Interface.
   * Service endpoint interface uses the name of PortType.
   * The binding information is not used when generating endpoint interfaces.
   * @param portType
   * @param binding
   * @param logicalPort
   * @return Returns the updated logical port information.
   * @throws WSDLException
   */
  public LogicalPortType generateSDI(WSDLPortType portType, WSDLBinding binding, LogicalPortType logicalPort) throws Exception {
    // @todo use generator specific exceptions
    if (logicalPort == null) {
      logicalPort = new LogicalPortType();
    }
    fillLogicalPortTemplate(logicalPort, portType);
    logicalPort.setPTName("{"+portType.getQName().getURI()+"}"+portType.getQName().getLocalName());
    QName portName = new QName(portType.getNamespace(),portType.getName());
    String interfaceName = portManager.getNewJavaName(portName,packageName);
    logicalPort.setInterfaceName(portManager.getJavaQName(portName));
    if (portManager.isQNameUsed(portName)) {
      return logicalPort;
    } else {
      portManager.useQName(portName);
    }
    if (genericMode) {
      logicalPort.setInterfaceName(Remote.class.getName());
      return logicalPort;
    }
    File outFile = new File(packageDir, interfaceName + ".java");
    try {
      outFile.createNewFile();
    } catch (Exception e) {
      throw new Exception(" Output file " + interfaceName + ".java could not be created !");
    }
    files.add(outFile);
    seiGenerated.add(outFile);
    CodeGenerator generator = new CodeGenerator();
    writePackage(packageName, generator);
    generator.addLine();
    generator.addLine("/**");
    generator.addLine(" * Service Endpoint Interface (generated by SAP WSDL to Java generator).");
    generator.addLine(" */");
    generator.addNewLine();
    generator.addLine("public interface " + interfaceName + " extends java.rmi.Remote,javax.xml.rpc.Stub {");
    generator.addNewLine();
    generator.startSection();
    ArrayList operations = portType.getOperations();
    for (int i = 0; i < operations.size(); i++) {
      WSDLOperation operation = (WSDLOperation) operations.get(i);
      WSDLDocumentation doc = operation.getDocumentation();
      if (doc!=null && doc.getContent()!=null && doc.getContent().length()!=0) {
        generator.addLine("/**");
        generator.addLine(" * "+doc.getContent());
        generator.addLine(" */");
      }
      if (operation.getInput()==null) {
        String methodName = nameConvertor.attributeToMethodName(operation.getName());
        if (operation.getOutput() != null) {
          generator.addComment(" "+methodName+" is a notification operation and is unsupported.");
        } else {
          generator.addLine("public void "+methodName+"();");
        }
      } else {
        markParams(operation);  
        if (!alternativeMethods || isDocumentStyle(operation)==false) {
          String methodName = nameConvertor.attributeToMethodName(operation.getName());
          generateOperationHeader(generator, methodName, operation);
          generator.add(";");
          generator.addNewLine();
        } else {
          processAlternativeOperation(generator, operation, false);
        }
      }
    }
    generator.addNewLine();
    generator.endSection();
    generator.addLine("}");
    // Write Generated Code
    PrintWriter output = new PrintWriter(new FileOutputStream(outFile), true);
    output.write(generator.toString());
    output.close();
    return logicalPort;
  }

  /**
   * Generates Service Operation interface header.
   * Does not use any binding information just port type info.
   */
  public void generateOperationHeader(CodeGenerator generator, String methodName, WSDLOperation operation) throws Exception {
    // Prepares header generation
    StringBuffer returnParams = new StringBuffer();
    generator.unRegisterName("returnType");
    generator.unRegisterName("returnName");
    // Generates return type;
    generator.addIndent();
    generator.add("public ");
    WSDLChannel input = operation.getInput();
    WSDLChannel output = operation.getOutput();
    // Clears custom reserved buffer
    nameConvertor.disableCustomReserved();
    nameConvertor.clearCustomReserved();
    if (output != null) { // return may not be null
      com.sap.engine.lib.xml.util.QName messagelink = output.getMessage();
      WSDLMessage message = getMessage(messagelink,operation.getName());
      ArrayList parts = message.getParts();

      if ((parts.size() == 1) && ((WSDLPart) parts.get(0)).inout == false) { // this is method return
        WSDLPart part = (WSDLPart) parts.get(0);
        TypeDefinitionBase type = getPartType(part);
        String paramType = schema.getJavaPrimitive(type);
        // Lets se What binding says
        generator.add(paramType + " ");
        // saves return type and return name
        generator.registerName("returnType", paramType);
        generator.registerName("returnName", nameConvertor.attributeToIdentifier(part.getName()));
      } else { // method is void
        generator.add("void ");
        for (int i = 0; i < parts.size(); i++) {
          WSDLPart part = (WSDLPart) parts.get(i);
          if (part.inout == false) {
            TypeDefinitionBase type = getPartType(part);
            String paramType = getHolder(type);
            if (returnParams.length() != 0) {
              returnParams.append(", ");
            }
            returnParams.append(paramType + " " + nameConvertor.attributeToIdentifier(part.getName()));
            nameConvertor.addCustomReserved(nameConvertor.attributeToIdentifier(part.getName()));
          }
        }
      }
    } else { // return is void
      generator.add("void ");
    }
    generator.add(methodName + "(");
    boolean hasInputParams = false;
    if (input != null) {
      com.sap.engine.lib.xml.util.QName messagelink = input.getMessage();
      WSDLMessage message = getMessage(messagelink,operation.getName());
      ArrayList parts = message.getParts();
      for (int i = 0; i < parts.size(); i++) {
        WSDLPart part = (WSDLPart) parts.get(i);
        TypeDefinitionBase type = getPartType(part);
        String paramType;
        if (part.inout) {
          paramType = getHolder(type);
        } else {
          paramType = schema.getJavaPrimitive(type);
        }
        if (i != 0) {
          generator.add(", ");
        }
        generator.add(paramType + " " + nameConvertor.attributeToIdentifier(part.getName()));
        nameConvertor.addCustomReserved(nameConvertor.attributeToIdentifier(part.getName()));
        hasInputParams = true;
      }
    }
    if (returnParams.length() != 0) {
      if (hasInputParams) {
        generator.add(", " + returnParams.toString());
      } else {
        generator.add(returnParams.toString());
      }
    }
    generator.add(")");
    generator.add(" throws java.rmi.RemoteException");
    // Process custom Exceptions.
    ArrayList faults = operation.getFaultList();
    for (int i=0; i<faults.size(); i++) {
      WSDLFault fault = (WSDLFault) faults.get(i);
      com.sap.engine.lib.xml.util.QName messageName = fault.getMessage();
      WSDLMessage message = definitions.getMessage(messageName.getLocalName(),messageName.getURI());
      if (message == null) {
        throw new Exception(" Unable to find message named "+messageName+" linked to wsdl:fault "+fault.getName());
      }
      addExceptionMessage(message);
      generator.add(",");
      generator.add(getExceptionClassName(message,true));
    }
  }

  /**
   * Adds WSDL Message to List of custom messages.
   */
  private void addExceptionMessage(WSDLMessage message) throws Exception {
    for (int i=0; i<exceptionMessages.size(); i++) {
      WSDLMessage cmessage = (WSDLMessage) exceptionMessages.get(i);
      if (message == cmessage) {
        return;
      }
    }
    ArrayList messageParts = message.getParts();
    if (messageParts.size() ==1) {
      WSDLPart part = (WSDLPart) messageParts.get(0);
      TypeDefinitionBase type = getPartType(part);
      if (schema.isSimple(type) == false && schema.isSoapArray(type) == false) { // This type is complex
        for (int i=0; i<exceptionMessages.size(); i++) {
          WSDLMessage cmessage = (WSDLMessage) exceptionMessages.get(i);
          if (cmessage.getPartCount() ==1) {
            WSDLPart cpart = cmessage.getPart(0);
            TypeDefinitionBase ctype = getPartType(cpart);
            if (ctype == type) {
              return;
            }
          }
        }
      }
    }

    exceptionMessages.add(message);
  }

  private String escapeUri(String s) {
    StringBuffer result = new StringBuffer();
    for (int i=0; i<s.length(); i++) {
      if (s.charAt(i)=='\\') {
        result.append("\\\\");
        continue;
      }
      if (s.charAt(i)=='\'') {
        result.append("\\\'");
        continue;
      }
      if (s.charAt(i)=='\"') {
        result.append("\\\"");
        continue;
      }
      result.append(s.charAt(i));
    }
    return result.toString();
  }
  /**
   * Escapes '\' in string.
   */
  private String escapeString(String s) {
    StringBuffer result = new StringBuffer();
    for (int i=0; i<s.length(); i++) {
      if (s.charAt(i)=='.') {
        result.append("/");
      } else {
        result.append(s.charAt(i));
      }
    }
    return result.toString();
  }

  public void generateOperationInit(WSDLPortType portType, CodeGenerator generator) {
    for (int i=0; i<portType.getOperationCount(); i++) {
      WSDLOperation operation = portType.getOperation(i);
      generator.addLine("  this.localProtocols.put(\""+operation.getName()+"\",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());");
      generator.addLine("  this.localFeatures.put(\""+operation.getName()+"\",new PropertyContext());");
    }
  }


  /**
   * Generates WSDL proxy stub class.
   * @param portType - wsdl port type structure.
   * @param wsdlbinding - corresponding binding structure.
   * @param endpoint - service endpoint.
   * @param tbinding - transport binding implementation
   * @param lport - logical port structure
   * @param server - boolean true if stub is for server clients.
   * @return
   * @throws Exception
   */
  public LogicalPortType stubGenerate(WSDLPortType portType, WSDLBinding wsdlbinding, String endpoint, ClientTransportBinding tbinding, LogicalPortType lport, boolean server) throws Exception {
   if (lport ==  null) {
      lport = new LogicalPortType();
    }
    QName bindingName = new QName(wsdlbinding.getNamespace(),wsdlbinding.getName()+"_Stub");
    String className = stubManager.getNewJavaName(bindingName,packageName);
    lport.setBindingImplementation(tbinding.getName());
    if (server) { // In server deploy is assumed that all features are set.
      if (endpoint != null && lport.getEndpoint() == null) {
        lport.setEndpoint(endpoint);
      }
      if (stubManager.getJavaQName(bindingName) != null && lport.getStubName() == null) {
        lport.setStubName(stubManager.getJavaQName(bindingName));
      }
      if (lport.getName() == null) {
        lport.setName(wsdlbinding.getName());
      }
      // Add local features and operation list.
      LocalFeatures localFeatures = lport.getLocalFeatures();
      if (localFeatures == null) {
        localFeatures = new LocalFeatures();
        lport.setLocalFeatures(localFeatures);
      }
      ArrayList operations = wsdlbinding.getOperations();
      for (int i=0; i<operations.size(); i++) {
        WSDLBindingOperation operation = (WSDLBindingOperation) operations.get(i);
        OperationType loperation = localFeatures.getOperation(operation.getName());
        if (loperation == null) {
          loperation = new OperationType();
          loperation.setName(operation.getName());
          localFeatures.addOperation(loperation);
        }
      }
      GlobalFeatures globalFeatures = null;
      if (lport.hasGlobalFeatures()) {
        globalFeatures = lport.getGlobalFeatures();
      }
      if (globalFeatures == null) {
        globalFeatures = new GlobalFeatures();
      }
      tbinding.importGlobalFeatures(globalFeatures,wsdlbinding);
    } else {
      fillLogicalPort(stubManager.getJavaQName(bindingName),endpoint,wsdlbinding,lport,tbinding);
    }
    String interfaceName = portManager.getNewJavaQName(new QName(portType.getNamespace(),portType.getName()),packageName);
    if (lport.hasInterfaceName()) {
      interfaceName = lport.getInterfaceName();
    }
    if (genericMode) {
      interfaceName = Stub.class.getName();
    }
    if (stubManager.isQNameUsed(bindingName)) {
      return lport;
    } else {
      portManager.useQName(bindingName);
    }
    File outFile = new File(packageDir, className + ".java");
//    try {
      outFile.createNewFile();
//    } catch (Exception e) {
//      throw new Exception(" Output file '" + className + ".java' could not be created !");
//    }
    files.add(outFile);
    seiGenerated.add(outFile);
    CodeGenerator codeGenerator = new CodeGenerator();
    writePackage(packageName, codeGenerator);
    codeGenerator.addNewLine();
    generateImports(codeGenerator);
    codeGenerator.addNewLine();
    if (genericMode) {
      codeGenerator.addLine("public class " + className + " extends com.sap.engine.services.webservices.jaxrpc.wsdl2java.BaseGeneratedStub implements " + interfaceName + ","+DynamicInterface.class.getName()+" {");
    } else {
      codeGenerator.addLine("public class " + className + " extends com.sap.engine.services.webservices.jaxrpc.wsdl2java.BaseGeneratedStub implements " + interfaceName + " {");
    }
    codeGenerator.addNewLine();
    codeGenerator.startSection();
    generateVariables(codeGenerator,tbinding);
    codeGenerator.addNewLine();
    codeGenerator.addLine("public " + className + "() {");
    codeGenerator.addLine("  super();");
    if (endpoint != null) {
      codeGenerator.addLine("  super._setEndpoint(\"" + escapeUri(endpoint) + "\");");
    }

    if (server == false) { // Server provides binding instance.
      codeGenerator.addLine("  this.transportBinding = new "+tbinding.getClass().getName()+"();");
    }
    /*
    if (containerMode == false) { // serialization provided by server
      codeGenerator.addLine("  try {");
      codeGenerator.addLine("    this.typeRegistry = new "+TypeMappingRegistryImpl.class.getName()+"();");
      if (isRegistryUsed) { // if there are some types to load
        codeGenerator.addLine("    this.typeRegistry.fromXML(this.getClass().getClassLoader().getResourceAsStream(\""+registryPath+"\"),this.getClass().getClassLoader());");
      }
      if (server == false) {
        codeGenerator.addLine("    this.transportBinding.setTypeMappingRegistry(this.typeRegistry);");
      }      
      codeGenerator.addLine("  } catch (java.lang.Exception e) {");
      codeGenerator.addLine("    throw new BaseRuntimeException(XmlSerializationResourceAccessor.getResourceAccessor(),TypeMappingException.COMMON_EXCEPTION,e);");
      //codeGenerator.addLine("    throw new RuntimeException(\"Can not load type mapping information !\");");
      codeGenerator.addLine("  }");
      codeGenerator.addLine("  this.featureConfiguration.setProperty(\"typeMapping\",this.typeRegistry.getDefaultTypeMapping());");
    } */
    generateOperationInit(portType, codeGenerator);
    codeGenerator.addLine("}");
    codeGenerator.addNewLine();
    codeGenerator.addLine("public void _setTransportBinding(com.sap.engine.services.webservices.jaxrpc.wsdl2java.ClientTransportBinding binding) {");
    codeGenerator.addLine("  super._setTransportBinding(binding);");
    codeGenerator.addLine("  if (this.transportBinding.getTypeMappingRegistry() == null && this.typeRegistry != null) {");
    codeGenerator.addLine("    this.transportBinding.setTypeMappingRegistry(this.typeRegistry);");
    codeGenerator.addLine("  }");
    codeGenerator.addLine("  if (this.typeRegistry == null && this.transportBinding.getTypeMappingRegistry() != null) {");
    codeGenerator.addLine("    this.typeRegistry = (com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl) this.transportBinding.getTypeMappingRegistry();");
    codeGenerator.addLine("  }");
    codeGenerator.addLine("}");
    codeGenerator.addNewLine();
    codeGenerator.addLine("public void _setTypeRegistry("+TypeMappingRegistryImpl.class.getName()+" _registry) {");
    codeGenerator.addLine("  this.typeRegistry =  _registry;");
    codeGenerator.addLine("  if (this.transportBinding != null && this.typeRegistry != null) {");
    codeGenerator.addLine("    this.transportBinding.setTypeMappingRegistry(this.typeRegistry);");
    codeGenerator.addLine("  }");
    codeGenerator.addLine("  this.featureConfiguration.setProperty(\"typeMapping\",this.typeRegistry.getDefaultTypeMapping());");
    codeGenerator.addLine("}");
    codeGenerator.addNewLine();
    // Generates stub methods
    ArrayList operations = portType.getOperations();
    for (int i = 0; i < operations.size(); i++) {
      WSDLOperation currentOperation = (WSDLOperation) operations.get(i);
      WSDLDocumentation doc = currentOperation.getDocumentation();
      if (doc!=null && doc.getContent()!=null && doc.getContent().length()!=0) {
        codeGenerator.addLine("/**");
        codeGenerator.addLine(" * "+doc.getContent());
        codeGenerator.addLine(" */");
      }
      if (currentOperation.getInput()==null) {
        String methodName = nameConvertor.attributeToMethodName(currentOperation.getName());
        if (currentOperation.getOutput() != null) {
          codeGenerator.addLine();
          codeGenerator.addComment(" "+methodName+" is a notification operation and is unsupported.");
          codeGenerator.addLine();
        } else {
          codeGenerator.addLine();
          codeGenerator.addLine("public void "+methodName+"() {");
          codeGenerator.addLine("}");
          codeGenerator.addLine();
        }
      } else {
        if (genericMode == false) {  // Generic mode does not need methods
        markParams(currentOperation);
        processOperation(codeGenerator, currentOperation, wsdlbinding,tbinding);
        processAlternativeOperation(codeGenerator, currentOperation, true);
        }/* else {
          // Process custom Exceptions.
          ArrayList faults = currentOperation.getFaultList();
          for (int j=0; j<faults.size(); j++) {
            WSDLFault fault = (WSDLFault) faults.get(j);
            com.sap.engine.lib.xml.util.QName messageName = fault.getMessage();
            WSDLMessage message = definitions.getMessage(messageName.getLocalName(),messageName.getURI());
            if (message == null) {
              throw new Exception(" Unable to find message named "+messageName+" linked to wsdl:fault "+fault.getName());
            }
            addExceptionMessage(message);
          }
        } */
      }
    }
    codeGenerator.addNewLine();
    if (genericMode) {
      // Generates operation metadata
      codeGenerator.addLine("public "+OperationStructure.class.getName()+" _getOpMetadata(java.lang.String opName) {");
      codeGenerator.startSection();
      for (int i = 0; i < operations.size(); i++) {
        WSDLOperation currentOperation = (WSDLOperation) operations.get(i);
        if (currentOperation.getInput()!=null) {
          provideOpMetadata(codeGenerator, currentOperation, wsdlbinding,tbinding,"opName");
        }
      }
      codeGenerator.addLine("return null;");
      codeGenerator.endSection();
      codeGenerator.addLine("}");
      codeGenerator.addNewLine();
      // Generate operation list
      codeGenerator.addLine("public java.lang.String[] _getOpNames() {");
      codeGenerator.startSection();
      codeGenerator.addLine("java.lang.String[] result = new java.lang.String["+operations.size()+"];");
      for (int i = 0; i < operations.size(); i++) {
        WSDLOperation currentOperation = (WSDLOperation) operations.get(i);
        codeGenerator.addLine("result["+i+"] = \""+escapeString(currentOperation.getName())+"\";");
      }
      codeGenerator.addLine("return result;");
      codeGenerator.endSection();
      codeGenerator.addLine("}");
      codeGenerator.addNewLine();
      // Generate generic invoke method
      codeGenerator.addLine("public void _invoke("+OperationStructure.class.getName()+" opStruct,"+ObjectFactory.class.getName()+" objectFactory ) throws "+InvocationTargetException.class.getName()+","+RemoteException.class.getName()+" {");
      codeGenerator.startSection();
      codeGenerator.addLine("try {");
      codeGenerator.startSection();
      codeGenerator.addComment(" Operation input initialization");
      codeGenerator.addLine("this.inputParams = opStruct.inputParams;");
      codeGenerator.addComment(" Operation output params initialization");
      codeGenerator.addLine("this.outputParams = opStruct.outputParams;");
      codeGenerator.addComment(" Operation faults initialization");
      codeGenerator.addLine("this.faultParams = opStruct.faultParams;");
      codeGenerator.addLine("this.transportBinding.setTypeMappingRegistry(this.typeRegistry);");
      codeGenerator.addLine("this.transportBinding.startOperation(this.inputParams,this.outputParams,this.faultParams);");
      codeGenerator.addComment(" Binding Context initialization");
      codeGenerator.addLine("this.bindingConfiguration = opStruct.bindingConfig;");
      codeGenerator.addLine("super._fillEndpoint(bindingConfiguration);");
      codeGenerator.addLine("_buildOperationContext(opStruct.operationName,this.transportBinding);");
      codeGenerator.addLine("this.stubConfiguration.setProperty(OBJECT_FACTORY,objectFactory);");
      codeGenerator.addLine("this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols(opStruct.operationName));");
      codeGenerator.addLine("_setEndpoint((String) bindingConfiguration.getProperty(com.sap.engine.services.webservices.jaxrpc.wsdl2java.ClientTransportBinding.ENDPOINT));");
      codeGenerator.endSection();
      codeGenerator.addLine("} catch ("+SOAPFaultException.class.getName()+" x) {");
      codeGenerator.addLine("  throw new "+InvocationTargetException.class.getName()+"(x,x.getFaultString());");
      codeGenerator.addLine("} catch ("+InvocationTargetException.class.getName()+" x) {");
      codeGenerator.addLine("  throw x;");
      codeGenerator.addLine("} catch ("+RemoteException.class.getName()+" x) {");
      codeGenerator.addLine("  throw x;");
      codeGenerator.addLine("} catch ("+Exception.class.getName()+" e) {");
      codeGenerator.addLine("  throw new "+RemoteException.class.getName()+"(e.getMessage(),e);");
      codeGenerator.addLine("}");
      codeGenerator.endSection();
    codeGenerator.addLine("}");
    codeGenerator.addNewLine();
    }
    // closes the class
    codeGenerator.endSection();
    codeGenerator.addLine("}");
    PrintWriter output = new PrintWriter(new FileOutputStream(outFile), true);
    output.print(codeGenerator.toString());
    output.close();
    className = packageName+"."+className;
    return lport;
  }


  /**
   * Returns message from WSDL definitions and throws exception if not found.
   */
  public WSDLMessage getMessage(com.sap.engine.lib.xml.util.QName messageLink, String operationName) throws ProxyGeneratorException {
    WSDLMessage message = definitions.getMessage(messageLink.getLocalName(),messageLink.getURI());
    if (message == null) {
      throw new ProxyGeneratorException(ProxyGeneratorException.MISSING_MESSAGE,messageLink.toString(),operationName);
    }
    return message;
  }


  /**
   * Returns Java type corresponding to given WSDL part.
   */
  public TypeDefinitionBase getPartType(WSDLPart part) throws Exception {
    com.sap.engine.lib.xml.util.QName typeLink = part.getType();
    TypeDefinitionBase type;
    String linkType = "type";
    if (part.getStyle() == WSDLPart.SIMPLE_TYPE) { // 'type' link
      linkType = "type";
      type = schema.getType(typeLink.getURI(), typeLink.getLocalName());
    } else { // 'element' link
      linkType = "element";
      type = schema.getElementType(typeLink.getURI(), typeLink.getLocalName());
    }
    if (type == null) {
      throw new Exception(" WSDL part <"+part.getName()+"> points to unavailable "+linkType+" with qname: "+ "{" + typeLink.getURI() + "}<" + typeLink.getLocalName()+">");
    }
    return type;
  }

  /**
   * Returns holder name of given type.
   */
  public String getHolder(TypeDefinitionBase type) throws Exception {
    String paramType = schema.getJavaPrimitive(type);
    if (schema.isSoapArray(type)) {
      paramType = nameConvertor.attributeToClassName(type.getName()) + "Holder";
      addHolder(paramType, schema.getJavaPrimitive(type));
      paramType = holderPackage + "." + paramType;
    } else if (schema.isSimple(type)) {
      paramType = nameConvertor.primitiveToHolder(paramType);
    } else if (type instanceof ComplexTypeDefinition && schema.isDocumentArray((ComplexTypeDefinition) type)) {
      paramType = nameConvertor.complexToHolder(paramType);
      addHolder(paramType, schema.getJavaPrimitive(type));
      paramType = holderPackage + "."+paramType;
    } else {
      paramType = nameConvertor.complexToHolder(paramType);
      addHolder(paramType, schema.getJavaPrimitive(type));
      paramType = holderPackage + "." + paramType;
    }

    return paramType;
  }

  /**
   * Adds holder to Holder list of in/out params.
   */
  public void addHolder(String holderName, String holderType) {
    for (int i = 0; i < holders.size(); i++) {
      HolderInfo holderInfo = (HolderInfo) holders.get(i);

      if (holderInfo.getName().equals(holderName)) {
        return;
      }
    }
    holders.add(new HolderInfo(holderName, holderType));
  }

  /**
   * Generates import statements.
   */
  private void generateImports(CodeGenerator generator) {
    generator.addComment("Import libraries");
    generator.addLine("import javax.xml.rpc.holders.*;");
    generator.addLine("import java.rmi.RemoteException;");
    generator.addLine("import javax.xml.rpc.encoding.*;");
    generator.addLine("import javax.xml.namespace.QName;");
    generator.addLine("import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ServiceParam;");
    generator.addLine("import com.sap.engine.services.webservices.jaxrpc.wsdl2java.PropertyContext;");
    generator.addLine("import com.sap.engine.services.webservices.jaxrpc.exceptions.TypeMappingException;");
    //generator.addLine("import com.sap.engine.services.webservices.jaxrpc.exceptions.accessors.XmlSerializationResourceAccessor;");
    generator.addLine("import com.sap.exception.BaseRuntimeException;");
  }

  /**
   * Generates stub common variables.
   */
  private void generateVariables(CodeGenerator generator, ClientTransportBinding binding) {
    generator.addComment("Proxy variables");
    if (this.containerMode == false) {
      generator.addLine("private "+TypeMappingRegistryImpl.class.getName()+" typeRegistry;");
    }
    binding.addVariables(generator);
  }


  /**
   * Generates code for operation input parts initialization.
   */
  private void inputPartsParse(ArrayList parts, CodeGenerator generator, String strName) throws Exception {
    generator.addComment("Operation input params initialization");
    generator.addLine(strName+".inputParams = new ServiceParam["+parts.size()+"];");
    for (int i=0; i<parts.size(); i++) {
      generator.addLine(strName+".inputParams["+i+"] = new ServiceParam();");
      WSDLPart part = (WSDLPart) parts.get(i);
      TypeDefinitionBase type = getPartType(part);
      if (part.getStyle() == WSDLPart.STRUCTURED_TYPE) { // 'element' link
        generator.addLine(strName+".inputParams["+i+"].isElement = true;");
      }
      String paramName = nameConvertor.attributeToIdentifier(part.getName());
      String paramType = schema.getJavaPrimitive(type);
      String typeUri = part.getType().getURI();
      String typeName = part.getType().getLocalName();
      generator.addLine(strName+".inputParams["+i+"].schemaName = new QName(\""+typeUri+"\",\""+typeName+"\");");
      generator.addLine(strName+".inputParams["+i+"].name = \""+part.getName()+"\";");
      generator.addLine(strName+".inputParams["+i+"].contentClass = "+paramType+".class;");
      if ("this".equals(strName)) {
      if (part.inout) {
        paramName = paramName + ".value";
      }
      if (nameConvertor.isPrimitive(paramType)) {
        generator.addLine("this.inputParams["+i+"].content = new " + nameConvertor.wrap(paramType) + "(" + paramName + ");");;
      } else {
        generator.addLine("this.inputParams["+i+"].content = "+paramName+";");
      }
    }
  }
  }

  /**
   * Generates code for operation output parts initialization.
   */
  private void outputPartsParse(ArrayList parts, CodeGenerator generator, String strName) throws Exception {
    generator.addComment("Operation output params initialization");
    generator.addLine(strName+".outputParams = new ServiceParam["+parts.size()+"];");
    for (int i=0; i<parts.size(); i++) {
      generator.addLine(strName+".outputParams["+i+"] = new ServiceParam();");
      WSDLPart part = (WSDLPart) parts.get(i);
      TypeDefinitionBase type = getPartType(part);
      if (part.getStyle() == WSDLPart.STRUCTURED_TYPE) { // 'element' link
        generator.addLine(strName+".outputParams["+i+"].isElement = true;");
      }
      String paramType = schema.getJavaPrimitive(type);
      String typeUri = part.getType().getURI();
      String typeName = part.getType().getLocalName();
      generator.addLine(strName+".outputParams["+i+"].schemaName = new QName(\""+typeUri+"\",\""+typeName+"\");");
      generator.addLine(strName+".outputParams["+i+"].name = \""+part.getName()+"\";");
      generator.addLine(strName+".outputParams["+i+"].contentClass = "+paramType+".class;");
    }
  }

  /**
   * Generates code for operation fault initialization.
   */
  private void faultPartsParse(ArrayList faults, CodeGenerator generator,String strName) throws Exception {
    generator.addComment("Operation faults initialization");
    generator.addLine(strName+".faultParams = new ServiceParam["+faults.size()+"];");
    for (int i=0; i<faults.size(); i++) {
      WSDLFault fault = (WSDLFault) faults.get(i);
      com.sap.engine.lib.xml.util.QName messageLink = fault.getMessage();
      WSDLMessage message = definitions.getMessage(messageLink);
      ArrayList parts = getMessageParts(messageLink.getLocalName(),messageLink.getURI());
      WSDLPart part = (WSDLPart) parts.get(0); // 1 element length is granted
      generator.addLine(strName+".faultParams["+i+"] = new ServiceParam();");
      if (part.getStyle() == WSDLPart.STRUCTURED_TYPE) { // 'element' link
        generator.addLine(strName+".faultParams["+i+"].isElement = true;");
      }

      String paramType = getExceptionClassName(message, true);
      if (genericMode) {
        paramType = InvocationTargetException.class.getName();
      }
      String typeUri = part.getType().getURI();
      String typeName = part.getType().getLocalName();
      generator.addLine(strName+".faultParams["+i+"].schemaName = new QName(\""+typeUri+"\",\""+typeName+"\");");
      generator.addLine(strName+".faultParams["+i+"].name = \""+part.getName()+"\";");
      generator.addLine(strName+".faultParams["+i+"].contentClass = "+paramType+".class;");
    }
  }

  /**
   * Generates code for catching custom Exceptions and rethrowing them.
   */
  private void generateCustomExceptionThrow(ArrayList faults, CodeGenerator generator) throws Exception {
    //generator.addLine("for (int _i=0; i<"+faults.size()+"; i++) {");
    //generator.startSection();
    for (int i=0; i<faults.size(); i++) {
      WSDLFault fault = (WSDLFault) faults.get(i);
      com.sap.engine.lib.xml.util.QName messageLink = fault.getMessage();
      WSDLMessage message = definitions.getMessage(messageLink.getLocalName(),messageLink.getURI());
      String paramType = getExceptionClassName(message, true);
      generator.addLine("if (this.faultParams["+i+"].content != null) {");
      generator.addLine("  throw ("+paramType+") this.faultParams["+i+"].content;");
      generator.addLine("}");
    }
  }

  /**
   * Generates code for catching custom Exceptions and rethrowing them.
   */
  private void generateCustomExceptionCatch(ArrayList faults, CodeGenerator generator,String exceptionName) throws Exception {
    for (int i=0; i<faults.size(); i++) {
      WSDLFault fault = (WSDLFault) faults.get(i);
      com.sap.engine.lib.xml.util.QName messageLink = fault.getMessage();
      WSDLMessage message = definitions.getMessage(messageLink.getLocalName(),messageLink.getURI());
      String paramType = getExceptionClassName(message, true);
      generator.addLine("} catch ("+paramType+" "+exceptionName+") {");
      generator.addLine("  throw "+exceptionName+";");
    }
  }

  /**
   * Returns ArrayList of Message Parts by message Name. throws Exception if message not found.
   */
  private ArrayList getMessageParts(String messageName, String messageNamespace) throws Exception {
    WSDLMessage message = definitions.getMessage(messageName,messageNamespace);
    if (message == null) {
      throw new Exception("Message '" + messageName + "' not found in WSDL !");
    }
    return message.getParts();
  }

  /**
   * Checks if some operation is document style.
   * @param operation
   * @return
   * @throws ProxyGeneratorException
   */
  private boolean isDocumentStyle(WSDLOperation operation) throws Exception {
    WSDLChannel input = operation.getInput();
    WSDLChannel output = operation.getOutput();
    if (input == null || output == null) {
      return false;
    }
    WSDLMessage inputMessage = getMessage(input.getMessage(),operation.getName());
    WSDLMessage outputMessage = getMessage(output.getMessage(),operation.getName());
    if (inputMessage.getPartCount() != 1 || outputMessage.getPartCount() != 1) {
      return false;
    }
    WSDLPart ipart = inputMessage.getPart(0);
    WSDLPart opart = outputMessage.getPart(0);
    if (ipart.getStyle() == WSDLPart.SIMPLE_TYPE || opart.getStyle() == WSDLPart.SIMPLE_TYPE) {
      return false;
    }
    TypeDefinitionBase itype = getPartType(ipart);
    TypeDefinitionBase otype = getPartType(opart);
    //TypeDefinitionBase itype = schema.getElementType(ipart.getType().getURI(),ipart.getType().getLocalName());
    //TypeDefinitionBase otype = schema.getElementType(opart.getType().getURI(),opart.getType().getLocalName());
    if (schema.isSimple(itype) || schema.isSimple(otype)) {
      return false;
    }
    if (schema.isDocumentArray((ComplexTypeDefinition) itype)) {
      return false;
    }
    if (schema.isDocumentArray((ComplexTypeDefinition) otype)) {
      return false;
    }
    int ikind = schema.getComplexTypeKind((ComplexTypeDefinition) itype);
    int okind = schema.getComplexTypeKind((ComplexTypeDefinition) otype);
    if ((ikind == FieldInfo.FIELD_ALL || ikind == FieldInfo.FIELD_SEQUENCE) && (okind == FieldInfo.FIELD_ALL || okind == FieldInfo.FIELD_SEQUENCE)) {
      ArrayList inElements = schema.getComplexFieldInfo((ComplexTypeDefinition) itype);
      ArrayList outElements = schema.getComplexFieldInfo((ComplexTypeDefinition) otype);
      ArrayList outAttribs = schema.getComplexAttributeInfo((ComplexTypeDefinition) otype);
      ArrayList inAttribs = schema.getComplexAttributeInfo((ComplexTypeDefinition) itype);
      if (inAttribs.size() != 0) {
        return false;
      }
      if (outAttribs.size() != 0 || outElements.size() > 1) {
        return false;
      }
      for (int i = 0; i < inElements.size(); i++) {
        // The element description        
        FieldInfo info = (FieldInfo) inElements.get(i);
        if (info.getFieldModel() != FieldInfo.FIELD_ELEMENT) {
          return false;
        }
        if (inElements.size() == 1 && itype.getName().equals(info.getTypeLocalName())) {
          if (itype.getTargetNamespace() != null && itype.getTargetNamespace().equals(info.getTypeUri())) {
            return false;
          }
          // both null
          if (itype.getTargetNamespace() == info.getTypeUri()) { //$JL-STRING$
            return false;
          }
        }
      }
      if (outElements.size() != 0) {
        FieldInfo info = (FieldInfo) outElements.get(0);
        if (info.getFieldModel() != FieldInfo.FIELD_ELEMENT) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private void processAlternativeOperation(CodeGenerator generator, WSDLOperation operation,boolean generateBody) throws Exception {
    if (!isDocumentStyle(operation)) { // if operation is not suitable do nothing.
      return;
    }
    // Method header
    com.sap.engine.lib.xml.util.QName messageInName = operation.getInput().getMessage();
    com.sap.engine.lib.xml.util.QName messageOutName = operation.getOutput().getMessage();
    String methodName = nameConvertor.attributeToMethodName(operation.getName());
    WSDLMessage inMessage = definitions.getMessage(messageInName.getLocalName(),messageInName.getURI());
    WSDLMessage outMessage = definitions.getMessage(messageOutName.getLocalName(),messageOutName.getURI());
    WSDLPart inpart = inMessage.getPart(0);
    WSDLPart outpart = outMessage.getPart(0);
    com.sap.engine.lib.xml.util.QName inType = inpart.getType();
    com.sap.engine.lib.xml.util.QName outType = outpart.getType();
    ComplexTypeDefinition inTypeDefinition = (ComplexTypeDefinition) schema.getElementType(inType.getURI(),inType.getLocalName());
    ComplexTypeDefinition outTypeDefinition = (ComplexTypeDefinition) schema.getElementType(outType.getURI(),outType.getLocalName());
    String inTypeName = inTypeDefinition.getName();
    if (inTypeName == null) {
      inTypeName = com.sap.engine.lib.xml.dom.DOM.toXPath(inTypeDefinition.getAssociatedDOMNode());
    }
    String inTypeNamespace = inTypeDefinition.getTargetNamespace();
    ArrayList inFields = schema.getComplexFieldInfo(inTypeDefinition);
    ArrayList outFields = schema.getComplexFieldInfo(outTypeDefinition);
    generator.addIndent();
    generator.add("public ");
    String requestVariable = "_request_variable";
    String resultVariable = "_result_variable";
    String returnString = "";
    String javaPrimitive = schema.getJavaPrimitive(inTypeDefinition);

    String requestString = javaPrimitive+" "+requestVariable+" = new "+javaPrimitive+"();";
    if (genericMode) {
      if (GenericObject.class.getName().equals(javaPrimitive)) {
        requestString = javaPrimitive+" "+requestVariable+" = new "+GenericObjectImpl.class.getName()+"(\""+escapeString(inTypeNamespace)+"\",\""+escapeString(inTypeName)+"\");";
      }
    }
    if (outFields.size() != 0) {
      FieldInfo info = (FieldInfo) outFields.get(0);
      String type=info.getTypeJavaName();
      if (info.getMaxOccurs() != 1) {
        type +="[]";
      }
      generator.add(type);
      if (genericMode) {
        if (nameConvertor.isPrimitive(type)) {
          returnString = "return (("+nameConvertor.wrap(type)+") "+resultVariable+"._getField(\""+escapeString(info.fieldLocalName)+"\"))."+type+"Value();";
        } else {
          returnString = "return ("+type+") "+resultVariable+"._getField(\""+escapeString(info.fieldLocalName)+"\");";
        }
      } else {
      returnString = "return "+resultVariable+"."+info.getGetterMethod()+"();";
      }
    } else {
      generator.add("void");
    }
    generator.add(" "+methodName+"(");
    boolean flag = false;
    for (int i=0; i<inFields.size(); i++) {
      if (flag) {
        generator.add(", ");
      }
      FieldInfo info = (FieldInfo) inFields.get(i);
      // old version puts "_"
      //generator.add(info.getTypeJavaName()+" _"+info.getFieldJavaName());
      // new version do not put "_"
      String type = info.getTypeJavaName();
      if (info.getMaxOccurs() != 1) {
        type +="[]";
      }
      generator.add(type+" "+nameConvertor.attributeToIdentifier(info.getFieldLocalName()));
      flag = true;
    }
    generator.add(") ");
    // throws exception clauses
    generator.add(" throws java.rmi.RemoteException");
    // Process custom Exceptions.
    ArrayList faults = operation.getFaultList();
    for (int i=0; i<faults.size(); i++) {
      WSDLFault fault = (WSDLFault) faults.get(i);
      com.sap.engine.lib.xml.util.QName messageName = fault.getMessage();
      WSDLMessage message = definitions.getMessage(messageName.getLocalName(),messageName.getURI());
      if (message == null) {
        throw new Exception(" Unable to find message named "+messageName+" linked to wsdl:fault "+fault.getName());
      }
      addExceptionMessage(message);
      generator.add(",");
      generator.add(getExceptionClassName(message,true));
    }

    if (generateBody == false) {
      generator.add(";");
      generator.addNewLine();
      return;
    }
    generator.add(" {");
    generator.addNewLine();
    generator.startSection();
    generator.addLine(requestString);
    for (int i=0; i<inFields.size(); i++) {
      FieldInfo info = (FieldInfo) inFields.get(i);
      String type = info.getTypeJavaName();
      if (info.getMaxOccurs() != 1) {
        type +="[]";
      }
      if (genericMode) {
        if (nameConvertor.isPrimitive(type)) {
          generator.addLine(requestVariable+"._setField(\""+escapeString(info.fieldLocalName)+"\","+nameConvertor.wrap(nameConvertor.getValueAsObject(info.getFieldLocalName(),false,type))+");");
        } else {
          generator.addLine(requestVariable+"._setField(\""+escapeString(info.fieldLocalName)+"\","+nameConvertor.attributeToIdentifier(info.getFieldLocalName())+");");
        }
      } else {
      generator.addLine(requestVariable+"."+info.getSetterMethod()+"("+nameConvertor.attributeToIdentifier(info.getFieldLocalName())+");");
    }
    }
    generator.addLine(schema.getJavaPrimitive(outTypeDefinition)+" "+resultVariable+" = "+methodName+"("+requestVariable+");");
    generator.addLine(returnString);
    generator.endSection();
    generator.addLine("}");
    generator.addNewLine();
  }

  private void provideOpMetadata(CodeGenerator generator, WSDLOperation operation, WSDLBinding binding, ClientTransportBinding tbinding, String vName) throws Exception {
    WSDLBindingOperation bOperation = binding.getOperation(operation.getName());
    PropertyContext context = new PropertyContext();
    tbinding.getMainBindingConfig(binding,context);
    tbinding.getOperationBindingConfig(bOperation, operation, context, definitions);
    // Method name check
    generator.addLine("if (\""+escapeString(operation.getName())+"\".equals("+vName+")) {");
    // Method Implementation
    generator.startSection();
    generator.addLine(OperationStructure.class.getName()+" result = new "+OperationStructure.class.getName()+"();");
    generator.addLine("result.operationName = \""+escapeString(operation.getName())+"\";");
    WSDLChannel input = operation.getInput();
    if (input != null) { // init input params
      com.sap.engine.lib.xml.util.QName messagelink = input.getMessage();
      ArrayList parts = getMessageParts(messagelink.getLocalName(),messagelink.getURI());
      inputPartsParse(parts,generator,"result");
    }
    WSDLChannel output = operation.getOutput();
    if (output != null) { // init output params
      com.sap.engine.lib.xml.util.QName messagelink = output.getMessage();
      ArrayList parts = getMessageParts(messagelink.getLocalName(),messagelink.getURI());
      outputPartsParse(parts,generator,"result");
    }
    ArrayList faults = operation.getFaultList(); // init faults
    faultPartsParse(faults,generator,"result");
    generator.addComment("Binding Context initialization");
    generator.addLine(PropertyContext.class.getName()+" bindingConfig = new "+PropertyContext.class.getName()+"();");
    generator.addLine("result.bindingConfig = bindingConfig;");
    generator.addLine("bindingConfig.clear();");
    operationBindingInitialization("bindingConfig", context, generator,0,0);
    generator.addLine("return result;");
    generator.endSection();
    generator.addLine("}");
    generator.addNewLine();
  }
  /**
   * Generates stub operation code.
   */
  private void processOperation(CodeGenerator generator, WSDLOperation operation, WSDLBinding binding, ClientTransportBinding tbinding) throws Exception {
    generator.unRegisterName("returnType");
    generator.unRegisterName("returnName");
    WSDLBindingOperation bOperation = binding.getOperation(operation.getName());
    if (bOperation == null) {
      throw new Exception(" Operation '"+operation.getName()+"' not described in binding '"+binding.getName()+"' !");
    }
    PropertyContext context = new PropertyContext();
    tbinding.getMainBindingConfig(binding,context);
    tbinding.getOperationBindingConfig(bOperation, operation, context, definitions);
    // Method header
    String methodName = nameConvertor.attributeToMethodName(operation.getName());
    generateOperationHeader(generator, methodName, operation);
    generator.add(" {");
    generator.addNewLine();
    // Method Implementation
    generator.startSection();
    generator.addLine("try {");
    generator.startSection();
    generator.addLine("super._beginLogFrame();");
    WSDLChannel input = operation.getInput();
    if (input != null) { // init input params
      com.sap.engine.lib.xml.util.QName messagelink = input.getMessage();
      ArrayList parts = getMessageParts(messagelink.getLocalName(),messagelink.getURI());
      inputPartsParse(parts,generator,"this");
    }
    WSDLChannel output = operation.getOutput();
    if (output != null) { // init output params
      com.sap.engine.lib.xml.util.QName messagelink = output.getMessage();
      ArrayList parts = getMessageParts(messagelink.getLocalName(),messagelink.getURI());
      outputPartsParse(parts,generator,"this");
    }
    ArrayList faults = operation.getFaultList(); // init faults
    faultPartsParse(faults,generator,"this");
    if (generator.isRegistered("returnType")) {
      nameConvertor.enableCustomReserved();
      String paramName = nameConvertor.attributeToIdentifier(generator.getValue("returnName"));
      generator.registerName("returnName",paramName);
      generator.addLine("$returnType$ " + paramName + ";");
      nameConvertor.disableCustomReserved();
    }
    if (this.containerMode == false) {
      generator.addLine("this.transportBinding.setTypeMappingRegistry(this.typeRegistry);");
    }
    generator.addLine("this.transportBinding.startOperation(this.inputParams,this.outputParams,this.faultParams);");
    generator.addComment("Binding Context initialization");
    generator.addLine("this.bindingConfiguration.clear();");
    operationBindingInitialization("bindingConfiguration", context, generator,0,0);
    generator.addLine("super._fillEndpoint(bindingConfiguration);");
    generator.addLine("_buildOperationContext(\""+operation.getName()+"\",this.transportBinding);");
    generator.addLine("this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols(\""+operation.getName()+"\"));");
    generator.addLine("_setEndpoint((String) bindingConfiguration.getProperty(com.sap.engine.services.webservices.jaxrpc.wsdl2java.ClientTransportBinding.ENDPOINT));");
    generateCustomExceptionThrow(faults,generator);
    if (output != null) { // init output params
      //nameConvertor.enableCustomReserved();
      com.sap.engine.lib.xml.util.QName messagelink = output.getMessage();
      WSDLMessage message = definitions.getMessage(messagelink.getLocalName(),messagelink.getURI());
      if (message == null) {
        throw new Exception("Message '" + messagelink.getLocalName() + "' not found in WSDL");
      }
      ArrayList parts = message.getParts();
      for (int i=0; i<parts.size(); i++) {
        WSDLPart part = (WSDLPart) parts.get(i);
        TypeDefinitionBase type = getPartType(part);
        String paramName = null;
        String paramType = schema.getJavaPrimitive(type);
        if (generator.isRegistered("returnType")) { // have return type
          paramName = generator.getValue("returnName");
        } else {
          paramName = nameConvertor.attributeToIdentifier(part.getName());
          paramName = paramName + ".value"; // This case is met on multiple output params
        }
        //if (!(parts.size() == 1 && ((WSDLPart) parts.get(0)).inout == false)) {
        //  paramName = paramName + ".value"; // This case is met on multiple output params
        //}
        if (nameConvertor.isPrimitive(paramType)) {
          String perm = nameConvertor.wrap(paramType);
          generator.addLine(paramName + " = ((" + perm + ") this.outputParams["+i+"].content)." + paramType + "Value();");
        } else {
          generator.addLine(paramName + " = (" + paramType + ") this.outputParams["+i+"].content;");
        }

      }
      //nameConvertor.disableCustomReserved();
    }
    if (generator.isRegistered("returnType")) {
      //nameConvertor.enableCustomReserved();
      String paramName = generator.getValue("returnName");
      generator.addLine("return " + paramName + ";");
      //nameConvertor.disableCustomReserved();
    }
    generator.endSection();
    nameConvertor.enableCustomReserved();
    String exceptionName = "e";
    exceptionName = nameConvertor.attributeToIdentifier(exceptionName);
    nameConvertor.disableCustomReserved();
    generateCustomExceptionCatch(faults,generator,exceptionName);
    generator.addLine("} catch (javax.xml.rpc.soap.SOAPFaultException "+exceptionName+") {");
    generator.addLine("  throw "+exceptionName+";");
    generator.addLine("} catch (java.lang.Exception "+exceptionName+") {");
    generator.addLine("  throw new RemoteException(\"Service call exception\","+exceptionName+");");
    generator.addLine("} finally {");
    generator.addLine("  super._endLogFrame(\""+operation.getName()+"\");");
    generator.addLine("}");
    generator.endSection();
    generator.addLine("}");
    generator.addNewLine();
  }


  /**
   * Marks in/out params - must call before interface code generation so Interfaces to be correctly created
   * In operation param is in/out param it appears only as input param placed in holder class and no appear in output
   * Types of in/out params must be the same
   *
   */
  public void markParams(WSDLOperation operation) throws Exception {
    //nameConvertor.clearCustomReserved();
    //nameConvertor.disableCustomReserved();
    WSDLChannel input = operation.getInput();
    WSDLChannel output = operation.getOutput();

    if (input == null || output == null) {
      return;
    }

    com.sap.engine.lib.xml.util.QName inMessageName = input.getMessage();
    com.sap.engine.lib.xml.util.QName outMessageName = output.getMessage();
    WSDLMessage inMessage = definitions.getMessage(inMessageName);
    WSDLMessage outMessage = definitions.getMessage(outMessageName);

    if (inMessage == null || outMessage == null) {
      throw new Exception(" Invalid pointer from operation to message !");
    }

    ArrayList inMessageParts = inMessage.getParts();
    ArrayList outMessageParts = outMessage.getParts();

//    for (int i = 0; i < inMessageParts.size(); i++) {
//      WSDLPart part = (WSDLPart) inMessageParts.get(i);
//      //nameConvertor.addCustomReserved(nameConvertor.attributeToIdentifier(part.getName()));
//    }

    if (outMessageParts.size() == 1) {
      return;
    }

    for (int i = 0; i < inMessageParts.size(); i++) {
      WSDLPart partIn = (WSDLPart) inMessageParts.get(i);

      for (int j = 0; j < outMessageParts.size(); j++) {
        WSDLPart partOut = (WSDLPart) outMessageParts.get(j);

        if (partIn.getName().equals(partOut.getName())) {
          if (!partIn.getType().equals(partOut.getType())) {
            throw new Exception(" In/Out parameters of operations must be same type !");
          }

          partIn.inout = true;
          partOut.inout = true;
        }
      }
    }

    return;
  }

  /**
   * Generates service implementation by logical ports. Used only when stubs are generated.
   * @param logicalPorts
   * @param lpPath
   * @param serverCase - flag indicating that configuration will be made by server.
   * @throws Exception
   */
  public void generateServiceImpl(LogicalPorts logicalPorts,String lpPath, boolean serverCase) throws Exception {
    if (logicalPorts.getName() == null) {
      return; // Service has no name nothing is generated
    }
    String interfaceName = getClassName(logicalPorts.getName());
    if (logicalPorts.hasInterfaceName()) {
      interfaceName = logicalPorts.getInterfaceName();
    }
    //String className = nameConvertor.attributeToClassName(logicalPorts.getName())+"Impl";
    String className = nameConvertor.getClassName(interfaceName)+"Impl";
    // Sets service implementation name
    if (packageName != null && packageName.length() != 0) {
      logicalPorts.setImplementationName(packageName+"."+className);
    } else {
      logicalPorts.setImplementationName(className);
    }
    File outFile = new File(packageDir, className + ".java");
    try {
      outFile.createNewFile();
    } catch (Exception e) {
      throw new Exception(" Output file " + className + ".java could not be created !");
    }
    files.add(outFile);
    serviceInterfaces.add(outFile);
    CodeGenerator generator = new CodeGenerator();
    LogicalPortType[] lports = logicalPorts.getLogicalPort();
    writePackage(packageName,generator);
    generator.addLine();
    String extendsClass = "com.sap.engine.services.webservices.jaxrpc.wsdl2java.ServiceBase";
    if (serverCase) {
      extendsClass = extendsClass+"Server";
    }
    generator.addLine("public class " + className + " extends "+extendsClass+" implements "+interfaceName+ " {");
    generator.addLine();
    generator.startSection();
    if (lpPath != null) {
      String ppath = escapeString(packageName+".")+protocolsFile;
      generator.addLine("public "+className+"() throws java.lang.Exception {");
      generator.addLine("  super();");
      generator.addLine("  java.io.InputStream input;");
      generator.addLine("  input = this.getClass().getClassLoader().getResourceAsStream(\""+ppath+"\");");
      generator.addLine("  loadProtocolsFromPropertyFile(input);");
      generator.addLine("  input.close();");
      generator.addLine("  init(this.getClass().getClassLoader().getResourceAsStream(\""+lpPath+"\"));");
      // TODO: This is supposed to be server case meaning the registry is provided by the deploy server
      //if (serverCase == false) {
        if (genericMode) {
          if (isRegistryUsed) { // if there are some types to load
            generator.addLine("  this._typeRegistry = new "+TypeMappingRegistryImpl.class.getName()+"("+TypeMappingRegistryImpl.GENERIC + ",this.getClass().getClassLoader().getResourceAsStream(\""+registryPath+"\"),this.getClass().getClassLoader());");
          } else {
            generator.addLine("  this._typeRegistry = new "+TypeMappingRegistryImpl.class.getName()+"("+TypeMappingRegistryImpl.GENERIC + ",null,null);");
          }
        } else {
          generator.addLine("  this._typeRegistry = new "+TypeMappingRegistryImpl.class.getName()+"();");
        if (isRegistryUsed) { // if there are some types to load
          generator.addLine("  this._typeRegistry.fromXML(this.getClass().getClassLoader().getResourceAsStream(\""+registryPath+"\"),this.getClass().getClassLoader());");
        }
        }
      //}
      generator.addLine("}");
      generator.addLine();
      generator.addLine("public "+className+"("+ClientComponentFactory.class.getName()+" componentFactory) throws java.lang.Exception {");
      generator.addLine("  super();");
      generator.addLine("  this.componentFactory = componentFactory;");
      generator.addLine("  init(this.getClass().getClassLoader().getResourceAsStream(\""+lpPath+"\"));");
      // TODO: This is supposed to be server case meaning the registry is provided by the deploy server
      //if (serverCase == false) {
        if (genericMode) {
          if (isRegistryUsed) { // if there are some types to load
            generator.addLine("  this._typeRegistry = new "+TypeMappingRegistryImpl.class.getName()+"("+TypeMappingRegistryImpl.GENERIC + ",this.getClass().getClassLoader().getResourceAsStream(\""+registryPath+"\"),this.getClass().getClassLoader());");
          } else {
            generator.addLine("  this._typeRegistry = new "+TypeMappingRegistryImpl.class.getName()+"("+TypeMappingRegistryImpl.GENERIC + ",null,null);");
          }
        } else {
          generator.addLine("  this._typeRegistry = new "+TypeMappingRegistryImpl.class.getName()+"();");
        if (isRegistryUsed) { // if there are some types to load
          generator.addLine("  this._typeRegistry.fromXML(this.getClass().getClassLoader().getResourceAsStream(\""+registryPath+"\"),this.getClass().getClassLoader());");
        }
        }
      //}
      generator.addLine("}");
    }  else {
      // Server hosted case
      generator.addLine("public "+className+"() throws java.lang.Exception {");
      // TODO: This is supposed to be server case meaning the registry is provided by the deploy server
      //if (serverCase == false) {
        generator.addLine("  this._typeRegistry = new "+TypeMappingRegistryImpl.class.getName()+"();");
        if (isRegistryUsed) { // if there are some types to load
          generator.addLine("  this._typeRegistry.fromXML(this.getClass().getClassLoader().getResourceAsStream(\""+registryPath+"\"),this.getClass().getClassLoader());");
        }
      //}
      generator.addLine("}");
    }
    generator.addLine();
    boolean isNewStyle = false;
    String seiName = null;
    if (lports.length>0) {
      isNewStyle = true;
      seiName = lports[0].getInterfaceName();
      if (seiName == null) {
        isNewStyle = false;
      }
      for (int i=1; i<lports.length && isNewStyle == true; i++) {
        if (!seiName.equals(lports[i].getInterfaceName())) {
          isNewStyle = false;
        }
      }
    }
    if (isNewStyle) {
      generator.addLine("public "+seiName+" getLogicalPort(String portName) throws javax.xml.rpc.ServiceException {");
      generator.addLine("  return ("+seiName+") getPort(new javax.xml.namespace.QName(null,portName),null);");
      generator.addLine("}");
      generator.addLine("public "+seiName+" getLogicalPort() throws javax.xml.rpc.ServiceException {");
      generator.addLine("  return ("+seiName+") getLogicalPort("+seiName+".class);");
      generator.addLine("}");
    }

    for (int i=0; i<lports.length; i++) {
      if (lports[i].getOriginal()) {
        String bindingName = lports[i].getBindingName();
        String binfingNamespace = lports[i].getBindingUri();
        WSDLBinding binding = definitions.getBinding(bindingName,binfingNamespace);
        WSDLPortType portType = this.definitions.getPortType(binding.getType().getName(),binding.getType().getURI());
        String seiInterface = portManager.getNewJavaQName(new QName(portType.getNamespace(),portType.getName()),packageName);
        if (lports[i].hasInterfaceName()) {
          seiInterface = lports[i].getInterfaceName();
        }
        generator.addLine("public "+seiInterface+" get"+nameConvertor.attributeToClassName(lports[i].getName())+"() throws javax.xml.rpc.ServiceException {");
        String qName = "new javax.xml.namespace.QName(null,\""+lports[i].getName()+"\")";
        generator.addLine("  return ("+seiInterface+") super.getPort("+qName+","+seiInterface+".class);");
        generator.addLine("}");
        generator.addLine();
      }
    }
    generator.addNewLine();
    generator.addLine("public java.rmi.Remote getLogicalPort(String portName, Class seiClass) throws javax.xml.rpc.ServiceException {");
    String qName = "new javax.xml.namespace.QName(null,portName)";
    generator.addLine("  return super.getPort("+qName+",seiClass);");
    generator.addLine("}");
    generator.addLine();
    generator.addLine("public java.rmi.Remote getLogicalPort(Class seiClass) throws javax.xml.rpc.ServiceException {");
    generator.addLine("  return super.getLogicalPort(seiClass);");
    generator.addLine("}");
    generator.addLine();
    generator.endSection();
    generator.addLine("}");
    PrintWriter output = new PrintWriter(new FileOutputStream(outFile), true);
    output.write(generator.toString());
    output.close();
  }

  /**
   * Generates service interface by logical ports.
   * Called when there is a service tag.
   * @param logicalPorts
   * @param getMethods
   * @throws Exception
   */
  public void generateServiceInterfaces(LogicalPorts logicalPorts, boolean getMethods) throws Exception {
    if (logicalPorts.getName() == null) {
      // Name is null do not generate anything
      return;
    }
    if (genericMode) {
      logicalPorts.setInterfaceName(Service.class.getName());
      return;
    }
    String className = nameConvertor.attributeToClassName(logicalPorts.getName());
    className = portManager.resolveJavaName(className,"Service");
    className = stubManager.resolveJavaName(className,"Service");
    String classQName = className;
    if (packageName != null && packageName.length() != 0) {
      classQName = packageName+"."+classQName;
    }
    logicalPorts.setInterfaceName(classQName);
    File outFile = new File(packageDir, className + ".java");
    try {
      outFile.createNewFile();
    } catch (Exception e) {
      throw new Exception(" Output file " + className + ".java could not be created !");
    }
    files.add(outFile);
    serviceInterfaces.add(outFile);
    CodeGenerator generator = new CodeGenerator();
    LogicalPortType[] lports = logicalPorts.getLogicalPort();
    writePackage(packageName,generator);
    generator.addLine();
    generator.addLine("/**");
    generator.addLine(" * Service Interface (generated by SAP WSDL to Java generator).");
    generator.addLine(" */");
    generator.addLine();
    generator.addLine("public interface " + className + " extends javax.xml.rpc.Service {");
    generator.addLine();
    generator.startSection();
    boolean isNewStyle = false;
    String interfaceName = null;
    if (lports.length>0) {
      isNewStyle = true;
      interfaceName = lports[0].getInterfaceName();
      for (int i=1; i<lports.length; i++) {
        if (!interfaceName.equals(lports[i].getInterfaceName())) {
          isNewStyle = false;
          break;
        }
      }
    }
    if (isNewStyle) {
      generator.addLine("public "+interfaceName+" getLogicalPort(String portName) throws javax.xml.rpc.ServiceException;");
      generator.addLine("public "+interfaceName+" getLogicalPort() throws javax.xml.rpc.ServiceException;");
    }
    if (getMethods) {
      for (int i=0; i<lports.length; i++) {
        lports[i].setOriginal(true);
        String bindingName = lports[i].getBindingName();
        String bindingNamespace = lports[i].getBindingUri();
        WSDLBinding binding = definitions.getBinding(bindingName,bindingNamespace);
        WSDLPortType portType = definitions.getPortType(binding.getType().getName(),binding.getType().getURI());
        String seiInterface = portManager.getNewJavaQName(new QName(portType.getNamespace(),portType.getName()),packageName);
        if (lports[i].hasInterfaceName()) {
          seiInterface = lports[i].getInterfaceName();
        }
        generator.addLine("public "+seiInterface+" get"+nameConvertor.attributeToClassName(lports[i].getName())+"() throws javax.xml.rpc.ServiceException ;");
      }
    }
    generator.addLine("public java.rmi.Remote getLogicalPort(String portName, Class seiClass) throws javax.xml.rpc.ServiceException;");
    generator.addLine("public java.rmi.Remote getLogicalPort(Class seiClass) throws javax.xml.rpc.ServiceException;");
    generator.addLine("public String[] getLogicalPortNames();");
    generator.addLine("public com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPortType getLogicalPortConfiguration(String lpName);");
    generator.addLine();
    generator.endSection();
    generator.addLine("}");
    PrintWriter output = new PrintWriter(new FileOutputStream(outFile), true);
    output.write(generator.toString());
    output.close();

  }


  /**
   * By wsdl message returns Custom Exception class name.
   * @param message
   * @param packageSet
   * @return
   * @throws Exception
   */
  private String getExceptionClassName(WSDLMessage message, boolean packageSet) throws Exception {
    if (message.getPartCount() ==1) {
      WSDLPart part = message.getPart(0);
      TypeDefinitionBase type = getPartType(part);
      String result = message.getName(); // SimpleType
      if (schema.isSimple(type) == false && schema.isSoapArray(type) == false) { // Complex Type
        result = type.getName();
        if (result == null || result.length() == 0) { // Element link
          result = part.getType().getLocalName()+"Exception";
        }
      }
      result = nameConvertor.attributeToClassName(result);
      if (packageName != null && packageName.length() != 0 && packageSet) {
        result = packageName+"."+result;
      }
      return result;
    } else {
      throw new Exception(" WSDL:Fault messages should have one part inside according WSDL specification !");
    }
  }

  /**
   * Generates classes for all exceptions.
   * @param messages
   * @throws Exception
   */
  private void generateExceptions(ArrayList messages) throws Exception {
    for (int i=0; i<messages.size(); i++) {
      WSDLMessage message = (WSDLMessage) messages.get(i);
      WSDLPart part = message.getPart(0);
      TypeDefinitionBase type = getPartType(part);
      if (schema.isSimple(type) || schema.isSoapArray(type)) {
        processExceptionSimpleType(message);
      } else {
        processExceptionComplexType(message);
      }
    }
  }

  /**
   * Writes package declaration.
   */
  private void writePackage(String packageName, CodeGenerator generator) {
    if (packageName != null && packageName.length() != 0) {
      generator.addLine("package " + packageName + ";");
    }
  }

  /**
   * Returns class name uring current output package.
   * @param className
   * @return
   */
  private String getClassName(String className) {
    if (packageName == null || packageName.length() == 0) {
      return nameConvertor.attributeToClassName(className);
    } else {
      return packageName+"."+nameConvertor.attributeToClassName(className);
    }
  }

  /**
   * Generates custom exception code for compex type messages.
   * @param message
   * @throws Exception
   */
  private void processExceptionComplexType(WSDLMessage message) throws Exception {
    CodeGenerator generator = new CodeGenerator();
    ArrayList messageParts = message.getParts();
    String className = getExceptionClassName(message,false);
    if (messageParts.size() == 1) {
      WSDLPart part = (WSDLPart) messageParts.get(0);
      TypeDefinitionBase partType = getPartType(part);
      String typeName = schema.getJavaPrimitive(partType);
      boolean isDocumentArray = false;
      if (partType instanceof ComplexTypeDefinition) {
        isDocumentArray = schema.isDocumentArray((ComplexTypeDefinition) partType);
      }
      String t_namespace = partType.getTargetNamespace();
      String t_name = partType.getName();
      if (t_name == null || t_name.length() == 0) {
        t_name = com.sap.engine.lib.xml.dom.DOM.toXPath(partType.getAssociatedDOMNode());
      }
      String fieldJavaName = nameConvertor.attributeToIdentifier(part.getName());
      ArrayList fields = schema.getComplexFieldInfo((ComplexTypeDefinition) partType);
      writePackage(packageName,generator);
      generator.addLine();
      generator.addLine("public class " + className + " extends com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProxyException {");
      generator.addLine("  private " + typeName + " " + fieldJavaName + ";");
      generator.addLine();
      generator.addLine("  public void init(" + typeName + " " + fieldJavaName + ") {");
      generator.addLine("    this." + fieldJavaName + "=" + fieldJavaName + ";");
      generator.addLine("  }");
      generator.addLine();
      if (fields.size() != 0) {
        generator.addLine("  public " + className + "() {");
        if (isDocumentArray) { // Type schema type is mapped to Array
          generator.addLine("    this." + fieldJavaName + "= new " + typeName.substring(0,typeName.length()-2)+"[0];");
        } else {
          generator.addLine("    this." + fieldJavaName + "= new " + typeName+"();");
        }
        generator.addLine("  }");
      }
      generator.addLine();
      generator.addLine("  public Class getContentClass() {");
      generator.addLine("    return "+typeName + ".class;");
      generator.addLine("  }");
      generator.addLine();
      generator.add("  public " + className + "(");
      if (isDocumentArray) {
        FieldInfo field = (FieldInfo) fields.get(0);
        generator.add(typeName+" "+nameConvertor.attributeToIdentifier(field.fieldLocalName));
      } else {
        for (int i=0; i<fields.size(); i++) {
          FieldInfo field = (FieldInfo) fields.get(i);
          if (field.fieldModel != FieldInfo.FIELD_ELEMENT) {
            throw new Exception("Only Element Field's allowed in Custom WSDL Fault !");
          }
          if (i!=0) {
            generator.add(", ");
          }
          if (field.maxOccurs !=1) {
            generator.add(field.typeJavaName+"[] "+nameConvertor.attributeToIdentifier(field.fieldLocalName));
          } else {
            generator.add(field.typeJavaName+" "+nameConvertor.attributeToIdentifier(field.fieldLocalName));
          }
        }
      }
      generator.add(") {");
      generator.addNewLine();
      if (isDocumentArray) {
        FieldInfo field = (FieldInfo) fields.get(0);
        generator.addLine("    this." + fieldJavaName + " = " + nameConvertor.attributeToIdentifier(field.fieldLocalName)+";");
      } else {
        generator.addLine("    this." + fieldJavaName + "= new " + typeName+"();");
      for (int i=0; i<fields.size(); i++) {
        FieldInfo field = (FieldInfo) fields.get(i);
        generator.addLine("    this."+fieldJavaName+"."+field.getSetterMethod()+"("+nameConvertor.attributeToIdentifier(field.fieldLocalName)+");");
      }
      }
      generator.addLine("  }");
      generator.addLine();
      if (isDocumentArray) {
        FieldInfo field = (FieldInfo) fields.get(0);
        String externalGetMethod = resolveGetMethod(field.getGetterMethod(),fields);
        generator.addLine("  public " + typeName + externalGetMethod + "() {");
        generator.addLine("    return this." + fieldJavaName + ";");
        generator.addLine("  }");
      } else {
      for (int i=0; i<fields.size(); i++) {
        FieldInfo field = (FieldInfo) fields.get(i);
        if (field.fieldModel != FieldInfo.FIELD_ELEMENT) {
          throw new Exception("Only Element Field's allowed in Custom WSDL Fault !");
        }
        String externalGetMethod = resolveGetMethod(field.getGetterMethod(),fields);
        if (field.maxOccurs != 1) {
          generator.addLine("  public " + field.typeJavaName + "[] " + externalGetMethod + "() {");
          generator.addLine("    return this." + fieldJavaName + "."+field.getGetterMethod()+"();");
          generator.addLine("  }");
        } else {
          generator.addLine("  public " + field.typeJavaName + " " + externalGetMethod + "() {");
          generator.addLine("    return this." + fieldJavaName + "."+field.getGetterMethod()+"();");
            generator.addLine("  }");
          }
        }
      }
      generator.addLine("}");
      generator.addLine();

    } else {
      throw new Exception(" WSDL:Fault messages should have only one part inside according WSDL specification !");
    }
    File outFile = new File(packageDir, className + ".java");
    files.add(outFile);
    PrintWriter output = new PrintWriter(new FileOutputStream(outFile), true);
    output.write(generator.toString());
    output.close();
  }

  /**
   * Fixes name collision resolving.
   * @param getMethod
   * @param fields
   * @return
   */
  private String resolveGetMethod(String getMethod, ArrayList fields) {
    if (getMethod.equals("getMessage")) {
      getMethod = getMethod+"Internal";
    }
    if (getMethod.equals("getLocalizedMessage")) {
      getMethod = getMethod+"Internal";
    }
    if (getMethod.equals("getClass")) {
      getMethod = getMethod+"Internal";
    }
    /*
    HashSet set = new HashSet();
    for (int i=0; i<fields.size(); i++) {
      set.add(((FieldInfo) fields.get(i)).getGetterMethod());
    }
    int i=2;*/
    String perm = getMethod;
    /*
    while (set.contains(perm)) {
      perm = getMethod+String.valueOf(i);
      i++;
    } */
    return perm;
  }
  /**
   * Generates custom exception code for simple type messages.
   * @param message
   * @throws Exception
   */
  private void processExceptionSimpleType(WSDLMessage message) throws Exception {
    String className = nameConvertor.attributeToClassName(message.getName());
    if (nameConvertor.conains(packageName,className)) {
      className = className+"New";
    }
    CodeGenerator generator = new CodeGenerator();
    ArrayList messageParts = message.getParts();
    if (messageParts.size() == 1) {
      WSDLPart part = (WSDLPart) messageParts.get(0);
      com.sap.engine.lib.xml.util.QName type = part.getType();
      TypeDefinitionBase partType = getPartType(part);
      String typeName = schema.getJavaPrimitive(partType);
      String fieldName = part.getName(); // Returns part name
      if (part.getStyle() == WSDLPart.STRUCTURED_TYPE) { // Part links to element
        fieldName = type.getLocalName();
      }
      String fieldJavaName = nameConvertor.attributeToIdentifier(fieldName);
      String methodName = nameConvertor.attributeToMethodName("get" + nameConvertor.attributeToClassName(fieldName));
      writePackage(packageName,generator);
      generator.addLine();
      generator.addLine("public class " + className + " extends com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProxyException {");
      generator.addLine("  private " + typeName + " " + fieldJavaName + ";");
      generator.addLine();
      generator.addLine("  public " + className + "(" + typeName + " " + fieldJavaName + ") {");
      generator.addLine("    this." + fieldJavaName + "=" + fieldJavaName + ";");
      generator.addLine("  }");
      generator.addLine();
      generator.addLine("  public " + className + "() {");
      generator.addLine("  }");
      generator.addLine();
      generator.addLine("  public void init("+ typeName + " " + fieldJavaName + ") {");
      generator.addLine("    this." + fieldJavaName + "=" + fieldJavaName + ";");
      generator.addLine("  }");
      generator.addLine("  public " + typeName + " " + methodName + "() {");
      generator.addLine("    return this." + fieldJavaName + ";");
      generator.addLine("  }");
      generator.addLine("  public Class getContentClass() {");
      generator.addLine("    return "+typeName + ".class;");
      generator.addLine("  }");

      generator.addLine("}");
      generator.addLine();
    } else {
      throw new Exception(" WSDL:Fault messages should have only one part inside according WSDL specification !");
    }
    File outFile = new File(packageDir, className + ".java");
    files.add(outFile);
    PrintWriter output = new PrintWriter(new FileOutputStream(outFile), true);
    output.write(generator.toString());
    output.close();
  }


}
