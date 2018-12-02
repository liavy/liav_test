package com.sap.engine.services.webservices.webservices630.server.deploy.ws;

import com.sap.engine.services.webservices.runtime.wsdl.WSDLGenerator;
import com.sap.engine.services.webservices.runtime.wsdl.SchemaConvertor;
import com.sap.engine.services.webservices.runtime.definition.*;
import com.sap.engine.services.webservices.runtime.interfaces.RuntimeTransportBinding;
import com.sap.engine.services.webservices.runtime.registry.OperationMappingRegistry;
import com.sap.engine.services.webservices.runtime.TransportBindingProvider;
import com.sap.engine.services.webservices.jaxrpc.util.PackageBuilder;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingImpl;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaToJavaGenerator;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.webservices630.server.deploy.common.ModuleDeployGenerator;
import com.sap.engine.services.webservices.webservices630.server.deploy.common.ModuleDeployResult;
import com.sap.engine.services.webservices.webservices630.server.deploy.common.ModuleFileStorageHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.preprocess.WSLocationWrapper;
import com.sap.engine.services.webservices.wsdl.WSDLDefinitions;
import com.sap.engine.services.webservices.wsdl.WSDLDOMLoader;
import com.sap.engine.lib.jar.JarUtils;
import com.sap.engine.lib.processor.SchemaProcessorFactory;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.lib.descriptors.ws04vi.VirtualInterfaceState;
import com.sap.engine.lib.io.hash.HashUtils;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.interfaces.webservices.runtime.JavaToQNameMappingRegistry;
import com.sap.engine.interfaces.webservices.runtime.OperationDefinition;
import com.sap.engine.interfaces.webservices.runtime.Key;
import com.sap.engine.interfaces.webservices.runtime.definition.WSIdentifier;
import com.sap.tc.logging.Location;

import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.*;

/**
 * Title: WSDeployGenerator
 * Description: The class contains base methods for generating web services deployment files.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSDeployGenerator {

  private static final String nl = WebServicesConstants.LINE_SEPARATOR;

  private WSDefinitionFactory wsDefinitionFactory = null;
  private WSFileStorageHandler wsFileStorageHandler = null;
  private WSDLGenerator wsdlGenerator = null;
  private JarUtils jarUtils = null;
  private PackageBuilder packageBuilder = null;
  private ModuleDeployGenerator moduleDeployGenerator = null;
  private ModuleFileStorageHandler moduleFileStorageHandler = null;

  private WSDeploySettingsProvider wsDeploySettingsProvider = null;
  private TransportBindingProvider trBindingProvider = null;

  public WSDeployGenerator(WSDeploySettingsProvider wsDeploySettingsProvider, TransportBindingProvider trBindingProvider, ModuleDeployGenerator moduleDepoyGenerator, ModuleFileStorageHandler moduleFileStorageHandler) {
    this.moduleDeployGenerator = moduleDepoyGenerator;
    this.moduleFileStorageHandler = moduleFileStorageHandler;

    this.wsDeploySettingsProvider = wsDeploySettingsProvider;
    this.trBindingProvider = trBindingProvider;
  }

  public WSDefinitionFactory getWsDefinitionFactory() {
    if(this.wsDefinitionFactory == null) {
      this.wsDefinitionFactory = new WSDefinitionFactory();
    }

    return wsDefinitionFactory;
  }

  public WSFileStorageHandler getWsFileStorageHandler() {
    if(this.wsFileStorageHandler == null) {
      this.wsFileStorageHandler = new WSFileStorageHandler();
    }

    return wsFileStorageHandler;
  }

  public WSDLGenerator getWsdlGenerator() {
    if(this.wsdlGenerator == null) {
      this.wsdlGenerator = new WSDLGenerator(trBindingProvider);
    }

    return this.wsdlGenerator;
  }

  public JarUtils getJarUtils() {
    if (this.jarUtils == null) {
      this.jarUtils = new JarUtils();
    }

    return this.jarUtils;
  }

  public PackageBuilder getPackageBuilder() {
    if (this.packageBuilder == null) {
      this.packageBuilder = new PackageBuilder();
    }

    return this.packageBuilder;
  }

  public WSAppDeployResult generateDeployFiles(String applicationName, String webServicesDir, String webServicesWorkingDir, File[] moduleArchives) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to generate web services deployment files for application " + applicationName + ". ";

    boolean areDirsDeleted = false;
    try {
      areDirsDeleted = IOUtil.deleteDirs(new String[]{webServicesDir, webServicesWorkingDir});
    } catch(IOException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Unable to clear web services directories. ";
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args, e);
    }

    if(!areDirsDeleted) {
      String msg = excMsg + "Unable to clear web services directories (" + webServicesDir + ", " + webServicesWorkingDir + ") - this may cause problems on web services start or update phase. ";
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args);
    }

    WSAppDeployResult wsAppDeployResult = new WSAppDeployResult();
    try {
      
      Hashtable wsDeploymentInfoesPerModule = loadWebServices(applicationName, webServicesWorkingDir, moduleArchives);
      if(wsDeploymentInfoesPerModule != null && wsDeploymentInfoesPerModule.size() != 0) {
        WSDeploymentInfo[] wsDeploymentInfoes = collectWSDeploymentInfoes(wsDeploymentInfoesPerModule);
        (new WSChecker()).checkWSAppLevel(collectWSRuntimeDefinitions(wsDeploymentInfoes));
        ModuleDeployResult moduleDeployResult = moduleDeployGenerator.generateAndSaveModuleDeployFiles(applicationName, webServicesDir, moduleArchives);
        //String[] moduleFilesForClassLoader = generateModuleFiles(webServicesDir, moduleArchives, collectDeployedWSNamesPerModule(wsDeploymentInfoesPerModule));
        generateWSDeployFiles(applicationName,  webServicesDir, webServicesWorkingDir, wsDeploymentInfoes, wsDeploymentInfoesPerModule);

        File[] deployedFiles = collectFiles(moduleArchives, WSUtil.collectKeySet(wsDeploymentInfoesPerModule));
        wsAppDeployResult = defineAppDeployResult(deployedFiles, moduleDeployResult.getFilesForClassLoader(), wsDeploymentInfoes);
      }
    } catch(WSDeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg;
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args, e);
    }

    return wsAppDeployResult;
  }

  public void getSchemas(ArrayList vInterfaceEntries, ArrayList vInterfacePaths, Hashtable viSchemaInfoes, DOMSource[][] allSchemas, HashMap[] mappings) throws WSDeploymentException {
    String excMessage = "Error occurred, getting schemas from virtual interfaces. ";
    InputStream viInputStream = null;
    try {
      int vInterfacesSize = vInterfacePaths.size();

      DOMSource[] literalSchemas = new DOMSource[0];
      DOMSource[] encodedSchemas = new DOMSource[0];

      SchemaConvertor literalSchemaConvertor = new SchemaConvertor();
      SchemaConvertor encodedSchemaConvertor = new SchemaConvertor();
      for (int i = 0; i < vInterfacesSize; i++) {
        String vInterfacePath = null;
        try {
          String vInterfaceEntry = (String)vInterfaceEntries.get(i);
          vInterfacePath = (String)vInterfacePaths.get(i);
          viInputStream = new FileInputStream(vInterfacePath);
          VirtualInterfaceState virtualInterfaceState = (VirtualInterfaceState)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04VI).parse(viInputStream);

          DOMSource[] currentLiteralSchemas = literalSchemaConvertor.parseInLiteralMode(virtualInterfaceState);
          DOMSource[] currentEncodedSchemas = encodedSchemaConvertor.parseInEncodedMode(virtualInterfaceState);

          VISchemasInfo currentViSchemasInfo = new VISchemasInfo();
          currentViSchemasInfo.setViPath(vInterfaceEntry);
          currentViSchemasInfo.setViName(virtualInterfaceState.getName().trim());

          currentViSchemasInfo.setLiteralSchemas(currentLiteralSchemas);
          currentViSchemasInfo.setEncodedSchemas(currentEncodedSchemas);
          viSchemaInfoes.put(currentViSchemasInfo.getViPath(), currentViSchemasInfo);

          DOMSource[] newLiteralSchemas = new DOMSource[literalSchemas.length + currentLiteralSchemas.length];
          System.arraycopy(literalSchemas, 0, newLiteralSchemas, 0, literalSchemas.length);
          System.arraycopy(currentLiteralSchemas, 0, newLiteralSchemas, literalSchemas.length, currentLiteralSchemas.length);
          literalSchemas = newLiteralSchemas;


          DOMSource[] newEncodedSchemas = new DOMSource[encodedSchemas.length + currentEncodedSchemas.length];
          System.arraycopy(encodedSchemas, 0, newEncodedSchemas, 0, encodedSchemas.length);
          System.arraycopy(currentEncodedSchemas, 0, newEncodedSchemas, encodedSchemas.length, currentEncodedSchemas.length);
          encodedSchemas = newEncodedSchemas;
        } finally {
          Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
          String msg = excMessage +
                       nl + "Unable to close stream for file: " + vInterfacePath;
          IOUtil.closeInputStreams(new InputStream[]{viInputStream}, new String[]{msg}, wsLocation);
        }
      }
      allSchemas[0] = literalSchemas;
      allSchemas[1] = encodedSchemas;
      mappings[0] = literalSchemaConvertor.getJavaToQNameMappings();
      mappings[1] = encodedSchemaConvertor.getJavaToQNameMappings();
   } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{"Unable to generate schemas for complex types from provided virtual interfaces", "not available"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  public Hashtable getModuleCrcTable(File[] moduleArchives) throws IOException {
    if(moduleArchives == null) {
      return new Hashtable();
    }

    Hashtable moduleCrcTable = new Hashtable();
    for(int i = 0; i < moduleArchives.length; i++) {
      File moduleArchive = moduleArchives[i];
      byte[] moduleCrc = HashUtils.generateFileHash(moduleArchive);
      moduleCrcTable.put(moduleArchive.getName(), moduleCrc);
    }

    return moduleCrcTable;
  }

  public String[] getWSFilesForClassLoader(WSRuntimeDefinition[] wsRuntimeDefinitions) {
    if(wsRuntimeDefinitions == null) {
      return new String[0];
    }

    String[] wsFilesForClassLoader = new String[wsRuntimeDefinitions.length];
    for(int i = 0; i < wsRuntimeDefinitions.length; i++) {
      wsFilesForClassLoader[i] = wsRuntimeDefinitions[i].getWsDirsHandler().getJarPath();
    }

    return wsFilesForClassLoader;
  }

  public String[] getDeployedComponents(WSRuntimeDefinition[] wsRuntimeDefinitions) {
    if(wsRuntimeDefinitions == null) {
      return new String[0];
    }

    String[] deployedComponents = new String[wsRuntimeDefinitions.length];
    for(int i = 0; i < wsRuntimeDefinitions.length; i++) {
      deployedComponents[i] = getDeployedSEIs(wsRuntimeDefinitions[i]);
    }

    return deployedComponents;
  }

  private Hashtable loadWebServices(String applicationName, String webServicesWorkingDir, File[] moduleArchives) throws WSDeploymentException {
    String excMsg = "Error occurred, loading web services for application " + applicationName + ". ";
    if(moduleArchives == null) {
      return new Hashtable();
    }

    WSDefinitionFactory wsDefinitionFactory = getWsDefinitionFactory();
    Hashtable wsDeploymentInfoesPerModule = new Hashtable();
    for(int i = 0; i < moduleArchives.length; i++) {
      File moduleArchive = moduleArchives[i];
        WSDeploymentInfo[] wsDeploymentInfoes = wsDefinitionFactory.loadWebServices(applicationName, webServicesWorkingDir, moduleArchive);
        if(wsDeploymentInfoes != null && wsDeploymentInfoes.length != 0) {
          wsDeploymentInfoesPerModule.put(moduleArchive.getName(), wsDeploymentInfoes);
        }

    }

    return wsDeploymentInfoesPerModule;
  }

  public void generateWSDeployFiles(String applicationName, String webServicesDir, String webServicesWorkingDir, WSDeploymentInfo[] wsDeploymentInfoes, Hashtable wsDeploymentInfoesPerModule) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to generate web services deploy files for application " + applicationName + ". ";

    generateWSDeployFiles(webServicesDir, webServicesWorkingDir, wsDeploymentInfoes, new int[0], -1);

    try {
      if(wsDeploymentInfoes != null && wsDeploymentInfoes.length != 0) {
        Properties deployedWSPerModule = collectDeployedWSNamesPerModule(wsDeploymentInfoesPerModule);
        moduleFileStorageHandler.saveDeployedComponentsPerModule(webServicesDir, deployedWSPerModule);
      }
    } catch(WSDeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  public void generateWSDeployFiles(String webServicesDir, String webServicesWorkingDir, WSDeploymentInfo[] wsDeploymentInfoes, int[] freeIndexes, int maxIndex) throws WSDeploymentException {
    setMappings(webServicesDir, webServicesWorkingDir, wsDeploymentInfoes, freeIndexes, maxIndex);
    generateWSDeployFiles0(webServicesDir, webServicesWorkingDir, wsDeploymentInfoes);
  }

  public void generateWSDeployFiles630(String webServicesDir, String webServicesWorkingDir, WSDeploymentInfo[] wsDeploymentInfoes) throws WSDeploymentException {
    setDefaultMappings(webServicesDir, webServicesWorkingDir, wsDeploymentInfoes);
    generateWSDeployFiles0(webServicesDir, webServicesWorkingDir, wsDeploymentInfoes);
  }

  public void generateWSDeployFiles0(String webServicesDir, String webServicesWorkingDir, WSDeploymentInfo[] wsDeploymentInfoes) throws WSDeploymentException {
    setOutsideImplSettings(collectWSRuntimeDefinitions(wsDeploymentInfoes));
    generateSingleWSDeployFiles(wsDeploymentInfoes);
  }

  private void setMappings(String webServicesDir, String webServicesWorkingDir, WSDeploymentInfo[] wsDeploymentInfoes, int[] freeIndexes, int maxIndex) {
    if(wsDeploymentInfoes == null) {
      return;
    }

    int freeIndexesLength = freeIndexes.length;
    for(int i = 0; i < wsDeploymentInfoes.length; i++) {
      if(i < freeIndexesLength) {
        setMappings(webServicesDir, webServicesWorkingDir, wsDeploymentInfoes[i], freeIndexes[i]);
      } else{
        setMappings(webServicesDir, webServicesWorkingDir, wsDeploymentInfoes[i], maxIndex + 1 + i - freeIndexesLength);
      }
    }
  }

  private void setDefaultMappings(String webServicesDir, String webServicesWorkingDir, WSDeploymentInfo[] wsDeploymentInfoes) {
    if(wsDeploymentInfoes == null) {
      return;
    }

    for(int i = 0; i < wsDeploymentInfoes.length; i++) {
      setDefaultMappings(webServicesDir, webServicesWorkingDir, wsDeploymentInfoes[i]);
    }
  }

  private void setDefaultMappings(String webServicesDir, String webServicesWorkingDir, WSDeploymentInfo wsDeploymentInfo) {
    WSRuntimeDefinition wsRuntimeDefinition = wsDeploymentInfo.getWsRuntimeDefinition();

    String serviceName = wsRuntimeDefinition.getWSIdentifier().getServiceName();
    Properties mappings = WSDirsHandler.generateDefaultMappings(serviceName);
    String wsDir = WSDirsHandler.getWSDefaultDirectory(webServicesDir, serviceName);
    WSDirsHandler wsDirsHandler = new WSDirsHandler(mappings, wsDir);
    wsRuntimeDefinition.setWsDirsHandler(wsDirsHandler);

    wsDeploymentInfo.setWsWorkingDirectory(WSDirsHandler.getWSDefaultWorkingDir(webServicesWorkingDir, serviceName));
  }

  private void setMappings(String webServicesDir, String webServicesWorkingDir, WSDeploymentInfo wsDeploymentInfo, int index) {
    WSRuntimeDefinition wsRuntimeDefinition = wsDeploymentInfo.getWsRuntimeDefinition();

    Properties mappings = WSDirsHandler.generateMappings(wsRuntimeDefinition.getWSIdentifier().getServiceName(), index);
    WSDirsHandler wsDirsHandler = new WSDirsHandler(mappings, WSDirsHandler.getWSDirectory(webServicesDir, index));
    wsRuntimeDefinition.setWsDirsHandler(wsDirsHandler);

    wsDeploymentInfo.setWsWorkingDirectory(WSDirsHandler.getWSWorkingDir(webServicesWorkingDir, index));
  }

  private void setOutsideImplSettings(WSRuntimeDefinition[] wsRuntimeDefinitions) throws WSDeploymentException {
    if(wsRuntimeDefinitions == null) {
      return;
    }

    for(int i = 0; i < wsRuntimeDefinitions.length; i++) {
      setOutsideImplSettings(wsRuntimeDefinitions[i]);
    }
  }

  private void setOutsideImplSettings(WSRuntimeDefinition wsRuntimeDefinition) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to make implementation specific settings. ";

    ServiceEndpointDefinition[] endpointDefinitions = wsRuntimeDefinition.getServiceEndpointDefinitions();
    for (int i = 0; i < endpointDefinitions.length; i++) {
      try {
        wsDeploySettingsProvider.defineImplLink(endpointDefinitions[i]);
      } catch(WSDeploymentException e) {
        Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
        wsDeployLocation.catching(e);

        Object[] args = new String[]{excMsg};
        throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
      }
    }
  }

  private void generateSEIAccessKeys(WSRuntimeDefinition wsRuntimeDefinition) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to make implementation specific settings. ";

    ServiceEndpointDefinition[] endpointDefinitions = wsRuntimeDefinition.getServiceEndpointDefinitions();
    for (int i = 0; i < endpointDefinitions.length; i++) {
      try {
        generateAccessKeys(endpointDefinitions[i]);
      } catch(WSDeploymentException e) {
        Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
        wsDeployLocation.catching(e);

        Object[] args = new String[]{excMsg};
        throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
      }
    }
  }

  private void generateAccessKeys(ServiceEndpointDefinition endpointDefinition) throws WSDeploymentException {
    WSRuntimeDefinition wsRuntimeDefinition = endpointDefinition.getOwner();
    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();
    String excMsg = "Error occurred, trying to generate runtime access keys for service endpoint " + endpointDefinition.getServiceEndpointId() + ", web service " + wsIdentifier.getServiceName() + ", application " + wsIdentifier.getApplicationName() + ". ";

    String trBindingId = endpointDefinition.getTransportBindingId();
    RuntimeTransportBinding trBinding = null;
    try {
      trBinding = (RuntimeTransportBinding)trBindingProvider.getTransportBinding(trBindingId);
      OperationMappingRegistry operationMappingRegistry = new OperationMappingRegistry();
      endpointDefinition.setOperationMappingRegistry(operationMappingRegistry);
      OperationDefinition[] operations = endpointDefinition.getOperations();
      int operationsSize = operations.length;
      for (int j = 0; j < operationsSize; j++) {
        OperationDefinitionImpl operation = (OperationDefinitionImpl)operations[j];
        Key[] keys = new Key[0];
        //Key[] keys = null;
        //keys = trBinding.getOperationKeys(operation);
        operation.setKeys(keys);
        //operationMappingRegistry.addOperation(keys, operation);
      }
    } catch(Exception e) {
      Location location = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      location.catching(e);

      Object[] args = new Object[]{excMsg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  private void generateSingleWSDeployFiles(WSDeploymentInfo[] wsDeploymentInfoes) throws WSDeploymentException {
    if(wsDeploymentInfoes == null) {
      return;
    }

    for(int i = 0; i < wsDeploymentInfoes.length; i++) {
      generateSingleWSDeployFiles(wsDeploymentInfoes[i]);
    }
  }

  private void generateSingleWSDeployFiles(WSDeploymentInfo wsDeploymentInfo) throws WSDeploymentException {
    WSRuntimeDefinition wsRuntimeDefinition = wsDeploymentInfo.getWsRuntimeDefinition();
    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();
    String excMsg = "Error occurred, deploying web service " + wsIdentifier.getServiceName() + ", application " + wsIdentifier.getApplicationName() + ". ";

    try {
      clearDirs(new String[]{wsRuntimeDefinition.getWsDirsHandler().getWsDirectory(), wsDeploymentInfo.getWsWorkingDirectory()});
    } catch(WSDeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    if(wsDeploymentInfo.hasOutsideInDescriptor()) {
      generateSingleWSDeployFilesOutsideIn(wsDeploymentInfo);
    } else {
      generateSingleWSDeployFilesStandard(wsDeploymentInfo);
    }
  }

  private void generateSingleWSDeployFilesStandard(WSDeploymentInfo wsDeploymentInfo) throws WSDeploymentException {
    WSRuntimeDefinition wsRuntimeDefinition = wsDeploymentInfo.getWsRuntimeDefinition();
    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();
    String excMsg = "Error occurred, deploying web service " + wsIdentifier.getServiceName() + ", application " + wsIdentifier.getApplicationName() + ". ";

    generateSEIAccessKeys(wsRuntimeDefinition);

    getWsFileStorageHandler().saveDescriptors(wsDeploymentInfo);

    try {
      DOMSource[][] allschemas = new DOMSource[2] [];
      Hashtable viSchemasInfoes = new Hashtable();
      HashMap[] mappings = new HashMap[2];
      getSchemas(wsDeploymentInfo.getVirtualInterfaceEntries(), wsDeploymentInfo.getVirtualInterfaces(), viSchemasInfoes, allschemas, mappings);
      generateTypesInfo(wsDeploymentInfo, allschemas, mappings);
      getWsdlGenerator().generateWsdls(wsRuntimeDefinition, viSchemasInfoes, mappings);
    } catch(WSDeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    getWsFileStorageHandler().saveWSRuntimeDescriptor(wsDeploymentInfo.getWsRuntimeDefinition());
  }

  private void generateSingleWSDeployFilesOutsideIn(WSDeploymentInfo wsDeploymentInfo) throws WSDeploymentException {
    WSRuntimeDefinition wsRuntimeDefinition = wsDeploymentInfo.getWsRuntimeDefinition();
    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();
    String excMsg = "Error occurred, deploying web service " + wsIdentifier.getServiceName() + ", application " + wsIdentifier.getApplicationName() + ". ";

    generateSEIAccessKeys(wsRuntimeDefinition);

    getWsFileStorageHandler().saveDescriptors(wsDeploymentInfo);
    getWsFileStorageHandler().saveOutsideIn(wsDeploymentInfo);

    try {
      DOMSource[][] allschemas = new DOMSource[2][];
      HashMap[] mappings = new HashMap[2];
      getOutsideInSchemas(wsDeploymentInfo.getWsdlRefPath(), allschemas);
      JavaToQNameMappingRegistry javaToQNameRegistry = JavaToQNameMappingRegistryImpl.loadFromFile(new File(wsDeploymentInfo.getJavaQNameMappingRefPath()));
      mappings[0] = javaToQNameRegistry.getLiteralMappings().getJavaToQNameMappings();
      mappings[1] = javaToQNameRegistry.getEncodedMappings().getJavaToQNameMappings();
      generateTypesInfo(wsDeploymentInfo, allschemas, mappings);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  private void getOutsideInSchemas(String wsdlPath, DOMSource[][] allSchemas) throws WSDeploymentException {
    try {
      WSDLDefinitions wsdlDefinitions = new WSDLDOMLoader().loadWSDLDocument(new File(wsdlPath).getAbsolutePath());
      ArrayList schemas = wsdlDefinitions.getSchemaDefinitions();
      int schemasSize = schemas.size();
      DOMSource[] schemasArr = new DOMSource[schemasSize];
      for (int i = 0; i < schemasSize; i++) {
        schemasArr[i] = (DOMSource)schemas.get(i);
      }
      allSchemas[0] = schemasArr;
      allSchemas[1] = schemasArr;
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{"Unable to load or parse wsdl file and to get the provided schemas in outside-in case", "not available"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  private void generateTypesInfo(WSDeploymentInfo wsDeploymentInfo, DOMSource[][] allSchemas, HashMap[] mappings) throws WSDeploymentException {
    WSRuntimeDefinition wsRuntimeDefinition = wsDeploymentInfo.getWsRuntimeDefinition();
    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();

    WSFileStorageHandler wsFileStorageHandler = getWsFileStorageHandler();
    try {
      wsFileStorageHandler.saveJavaToQNameMappingFile(wsRuntimeDefinition, mappings);
      generateTypes(wsDeploymentInfo, allSchemas, mappings);
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{   "Unable to generate complex types", wsIdentifier.getApplicationName(), wsIdentifier.getJarName(), wsIdentifier.getServiceName()};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  private void generateTypes(WSDeploymentInfo wsDeploymentInfo, DOMSource[][] allSchemas, HashMap[] mapppings) throws WSDeploymentException {
    WSRuntimeDefinition wsRuntimeDefinition = wsDeploymentInfo.getWsRuntimeDefinition();
    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();

    try {
      String wsIdentifierAsString = getWSIdentifierAsString(wsIdentifier);
      DOMSource[] literalSchemas = allSchemas[0];
      DOMSource[] encodedSchemas = allSchemas[1];
      HashMap literalMappings = mapppings[0];
      HashMap encodedMappings = mapppings[1];

      String wsWorkingDir = wsDeploymentInfo.getWsWorkingDirectory();
      TypeMappingRegistryImpl typeMappingRegistryImpl = new TypeMappingRegistryImpl();
      TypeMappingImpl literalTypeMapping = (TypeMappingImpl)typeMappingRegistryImpl.getDefaultTypeMapping();
      String packageNameForLiteral = wsIdentifierAsString + WebServicesConstants.LITERAL_SUFFIX;
      generateSerializationClasses(literalTypeMapping, literalSchemas, literalMappings, wsWorkingDir, packageNameForLiteral);

      TypeMappingImpl encodedTypeMapping = (TypeMappingImpl)typeMappingRegistryImpl.createTypeMapping();
      String packageNameForEncoded = wsIdentifierAsString + WebServicesConstants.SOAPENC_SUFFIX;
      generateSerializationClasses(encodedTypeMapping, encodedSchemas, encodedMappings, wsWorkingDir, packageNameForEncoded);

      typeMappingRegistryImpl.registerDefault(literalTypeMapping);
      typeMappingRegistryImpl.register(NS.SOAPENC, encodedTypeMapping);
      String typeMappingPath = wsRuntimeDefinition.getWsDirsHandler().getTypeMappingPath();
      typeMappingRegistryImpl.toXmlFile(typeMappingPath);

      Vector filters = new Vector();
      filters.add("class");
      String typesJarPath = wsRuntimeDefinition.getWsDirsHandler().getJarPath();
      IOUtil.createParentDir(new String[]{typesJarPath});
      getJarUtils().makeJarFromDir(typesJarPath, new String[]{wsWorkingDir}, filters);
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{"Unable to generate complex types", wsIdentifier.getApplicationName(), wsIdentifier.getJarName(), wsIdentifier.getServiceName()};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  private void generateSerializationClasses(TypeMappingImpl typeMapping, DOMSource[] schemas, HashMap mappings, String workingDir, String packageName) throws WSDeploymentException {
    try {
      SchemaToJavaGenerator schemaToJavaGenerator = new SchemaToJavaGenerator();
      int schemasSize = schemas.length;
      for (int i = 0; i < schemasSize; i++) {
        schemaToJavaGenerator.addSchemaSource(schemas[i]);
      }

      String classPath = wsDeploySettingsProvider.getClassPath();
      HashMap schemaJavaMap = revert(mappings);

      schemaToJavaGenerator.setApplicationSchemaMapping(schemaJavaMap);
      schemaToJavaGenerator.setApplicationJavaMapping(mappings);

      schemaToJavaGenerator.generateAll(new File(workingDir), packageName, false);
      //getPackageBuilder().compileExternal(classPath + File.pathSeparator + workingDir, new File(workingDir));
      schemaToJavaGenerator.registerTypes(typeMapping);
      
      if(!IOUtil.isEmptyDir(workingDir)) {
        getPackageBuilder().compileExternal(classPath + File.pathSeparator + workingDir, new File(workingDir));
      }
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{"Unable to generate complex types", "not available"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  private HashMap revert(HashMap hMap) {
    HashMap nMap = new HashMap();

    Iterator keys = hMap.keySet().iterator();
    Object tmpKey;
    while (keys.hasNext()) {
      tmpKey = keys.next();
      nMap.put(hMap.get(tmpKey), tmpKey);
    }

    return nMap;
  }

  private String getDeployedSEIs(WSRuntimeDefinition wsRuntimeDefinition) {
    String resultString = "WEB SERVICE PORTS: \n";

    ServiceEndpointDefinition[] endpointDefinitions = wsRuntimeDefinition.getServiceEndpointDefinitions();
    for (int i = 0; i < endpointDefinitions.length; i++) {
      resultString += endpointDefinitions[i].getServiceEndpointId() + "\n";
    }
    return resultString;
  }

  public WSRuntimeDefinition[] collectWSRuntimeDefinitions(WSDeploymentInfo[] wsDeploymentInfoes) {
    if(wsDeploymentInfoes == null) {
      return new WSRuntimeDefinition[0];
    }

    WSRuntimeDefinition[] wsRuntimeDefinitions = new WSRuntimeDefinition[wsDeploymentInfoes.length];
    for (int i = 0; i < wsDeploymentInfoes.length; i++) {
      WSDeploymentInfo wsDeploymentInfo = wsDeploymentInfoes[i];
      wsRuntimeDefinitions[i] = wsDeploymentInfo.getWsRuntimeDefinition();
    }
    return wsRuntimeDefinitions;
  }

  public String getWSIdentifierAsString(WSIdentifier wsIdentifier) {
    String jarName = WSUtil.getJarNameAsString(wsIdentifier.getJarName());
    String serviceName = getServiceNameAsString(wsIdentifier.getServiceName());
    return jarName + "_" + serviceName;
  }

  private String getServiceNameAsString(String serviceName) {
    return WSUtil.replaceForbiddenChars(serviceName);
  }

  private WSAppDeployResult defineAppDeployResult(File[] deployedModules, String[] moduleFilesForClassLoader, WSDeploymentInfo[] wsDeploymentInfoes) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to construct web services deploy result. ";
    if(wsDeploymentInfoes == null) {
      return new WSAppDeployResult();
    }

    WSAppDeployResult wsAppDeployResult = new WSAppDeployResult();
    WSRuntimeDefinition[] wsRuntimeDefinitions = collectWSRuntimeDefinitions(wsDeploymentInfoes);
    wsAppDeployResult.setDeployedComponentNames(getDeployedComponents(wsRuntimeDefinitions));
    wsAppDeployResult.setFilesForClassloader(WSUtil.unifyStrings(new String[][]{moduleFilesForClassLoader, getWSFilesForClassLoader(wsRuntimeDefinitions)}));    
    wsAppDeployResult.setWsDeploymentInfos(wsDeploymentInfoes);

    try {
      wsAppDeployResult.setModuleCrcTable(getModuleCrcTable(deployedModules));
    } catch(IOException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Unable to generate file hashes for module archives. ";
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    //wsAppDeployResult.setWarnings(warnings);

    return wsAppDeployResult;
  }

  public WSDeploymentInfo[] collectWSDeploymentInfoes(Hashtable wsDeploymentInfoesHash) {
    if (wsDeploymentInfoesHash == null) {
      return new WSDeploymentInfo[0];
    }

    Enumeration enum1 = wsDeploymentInfoesHash.elements();
    WSDeploymentInfo[] wsDeploymentInfoes = new WSDeploymentInfo[0];
    while(enum1.hasMoreElements()) {
      WSDeploymentInfo[] currentWsDeploymentInfoes = (WSDeploymentInfo[])enum1.nextElement();
      wsDeploymentInfoes = WebServicesUtil.unifyWSDeploymentInfoes(new WSDeploymentInfo[][]{wsDeploymentInfoes, currentWsDeploymentInfoes});
    }

    return wsDeploymentInfoes;
  }

  public Properties collectDeployedWSNamesPerModule(Hashtable wsDeploymentInfoesPerModule) {
    String delimiter = ";";
    if(wsDeploymentInfoesPerModule == null) {
      return new Properties();
    }

    Enumeration enum1 = wsDeploymentInfoesPerModule.keys();
    Properties deployedWSNamesPerModule = new Properties();
    while(enum1.hasMoreElements()) {
      String moduleName = (String)enum1.nextElement();
      Object wsPerModule = wsDeploymentInfoesPerModule.get(moduleName);
      if(wsPerModule instanceof WSDeploymentInfo[]) {
        WSDeploymentInfo[] wsDeploymentInfoes = (WSDeploymentInfo[])wsPerModule;
        deployedWSNamesPerModule.setProperty(moduleName, WSUtil.concatStrings(collectServiceNames(wsDeploymentInfoes), delimiter));
      } else if(wsPerModule instanceof WSLocationWrapper[]) {
        WSLocationWrapper[] wsLocationWrappers = (WSLocationWrapper[])wsPerModule;
        deployedWSNamesPerModule.setProperty(moduleName, WSUtil.concatStrings(collectServiceNames(wsLocationWrappers), delimiter));
      }

    }

    return deployedWSNamesPerModule;
  }

  private String[] collectServiceNames(WSDeploymentInfo[] wsDeploymentInfoes) {
    if(wsDeploymentInfoes == null) {
      return new String[0];
    }

    String[] serviceNames = new String[wsDeploymentInfoes.length];
    for(int i = 0; i < wsDeploymentInfoes.length; i++) {
      serviceNames[i] = wsDeploymentInfoes[i].getWsRuntimeDefinition().getWSIdentifier().getServiceName();
    }

    return serviceNames;
  }

  private String[] collectServiceNames(WSLocationWrapper[] wsLocationWrappers) {
    if(wsLocationWrappers == null) {
      return new String[0];
    }

    String[] serviceNames = new String[wsLocationWrappers.length];
    for(int i = 0; i < wsLocationWrappers.length; i++) {
      serviceNames[i] = wsLocationWrappers[i].getWebServiceName();
    }

    return serviceNames;
  }

  public void clearDirs(String[] dirs) throws WSDeploymentException {
    String excMsg = "Unable to clear directories ( " + WSUtil.concatStrings(dirs, ";") +  "). ";

    boolean areDirsDeleted = false;
    try {
      areDirsDeleted = IOUtil.deleteDirs(dirs);
    } catch(IOException e)  {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg;
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args, e);
    }

    if(!areDirsDeleted) {
      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args);
    }
  }

  private File[] collectFiles(File[] files, Set filterList) {
    if(files == null) {
      return new File[0];
    }

    if(filterList == null) {
      filterList = new HashSet();
    }

    Vector filteredFiles = new Vector();
    for(int i = 0; i < files.length; i++) {
      File file = files[i];
      if(filterList.contains(file.getName())) {
        filteredFiles.add(file);
      }
    }

    File[] filteredFilesArr = new File[filteredFiles.size()];
    filteredFiles.copyInto(filteredFilesArr);

    return filteredFilesArr;
  }

}
