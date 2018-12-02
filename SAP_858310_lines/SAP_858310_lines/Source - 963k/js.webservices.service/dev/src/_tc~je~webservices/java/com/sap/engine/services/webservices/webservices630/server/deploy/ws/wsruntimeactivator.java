package com.sap.engine.services.webservices.webservices630.server.deploy.ws;

import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.server.WebServicesFrame;
import com.sap.engine.services.webservices.webservices630.server.deploy.WSConfigurationHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.runtime.definition.*;
import com.sap.engine.services.webservices.runtime.registry.RuntimeRegistry;
import com.sap.engine.services.webservices.runtime.registry.OperationMappingRegistry;
//import com.sap.engine.services.webservices.runtime.RuntimeProcessor;
import com.sap.engine.services.webservices.runtime.servlet.ServletDispatcherImpl;
import com.sap.engine.services.webservices.exceptions.*;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;
import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.interfaces.webservices.runtime.ServletDispatcher;
import com.sap.engine.interfaces.webservices.runtime.JavaToQNameMappingRegistry;
import com.sap.engine.interfaces.webservices.runtime.OperationDefinition;
import com.sap.engine.interfaces.webservices.runtime.Key;
import com.sap.engine.interfaces.webservices.runtime.definition.WSIdentifier;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.tc.logging.Location;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Enumeration;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2003
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSRuntimeActivator implements WebServicesConstants {

  private WSBaseGlobalContext lcmContext = null;

  public WSRuntimeActivator() {
    this.lcmContext = new WSBaseGlobalContext();
  }

  public WSBaseGlobalContext getLcmContext() {
    return lcmContext;
  }

  public void setLcmContext(WSBaseGlobalContext lcmContext) {
    this.lcmContext = lcmContext;
  }

  public void downloadApplicationFiles(String applicationName, Configuration appConfiguration) throws WSDeploymentException, WSWarningException {
    Hashtable configsToDirsMappings = downloadWSFiles(applicationName, appConfiguration);
    notifyProtocolsOnDownloadFiles(applicationName, getStrings(configsToDirsMappings.elements(), configsToDirsMappings.size()), getStrings(configsToDirsMappings.keys(), configsToDirsMappings.size()) , appConfiguration);
  }

  public Hashtable downloadWSFiles(String applicationName, Configuration appConfiguration) throws WSDeploymentException {
    String excMsg = "Error occurred, downloading web services files for application: " + applicationName + ". ";

    try {
      if(!WSConfigurationHandler.existsWebServicesConfiguration(appConfiguration)) {
        return new Hashtable();
      }
    } catch(ConfigurationException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Error occurred, trying to get " + WS_CONTAINER_NAME + " DB configuration. ";
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    String webservicesDir = null;
    try {
      String wsContainerDir = WSContainer.getWSDeployer().getWSContainerDir(applicationName);
      webservicesDir = WSDirsHandler.getWebServicesDir(wsContainerDir);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Unable to get " + WS_CONTAINER_NAME + " directory. ";
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    Hashtable configsToDirsMappings = null;
    try {
      configsToDirsMappings = WSConfigurationHandler.downloadWebServicesConfiguration(webservicesDir, appConfiguration);
    } catch(WSConfigurationException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Error occurred, trying to download web services files. ";
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    return configsToDirsMappings;
  }

  public void start(String applicationName, String webServicesDir, Configuration appConfiguration) throws WSDeploymentException, WSWarningException {
    Vector warnings = new Vector();

    try {
      start(applicationName, new File(webServicesDir).listFiles());
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      notifyProtocolsOnStart(applicationName, appConfiguration);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void start(String applicationName, File[] wsDirs) throws WSDeploymentException, WSWarningException {
    if(wsDirs == null) {
      return;
    }

    Vector warnings = new Vector();
    for(int i = 0; i < wsDirs.length; i++) {
      String wsDir = wsDirs[i].getAbsolutePath();
      if (wsDir.endsWith(APP_JARS_NAME)) {
       continue;
      }

      try {
        startSingleWebService(applicationName, wsDir);
      } catch(WSWarningException e) {
        warnings.addAll(e.getWarningsVector());
      }
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  private void startSingleWebService(String applicationName, String wsDir) throws WSWarningException {
    String excMsg = "Error occurred, trying to start web service from directory " + wsDir + ", application " + applicationName + ".";

    WSRuntimeDefinition wsRuntimeDefinition = null;
    try {
      wsRuntimeDefinition = loadWebService(applicationName, wsDir);
      startSingleWebService(wsRuntimeDefinition);
    } catch(WSWarningException e) {
      throw e;
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching("Warning! ", e);

      String msg = excMsg;
      if(wsRuntimeDefinition != null) {
        msg += "Web Service " + wsRuntimeDefinition.getWSIdentifier().getServiceName() +  ". ";
      }

      try {
        if(wsRuntimeDefinition != null) {
          unregisterWebService(wsRuntimeDefinition);
        }
      } catch(WSWarningException iExc) {
        wsDeployLocation.catching("Warning! ", iExc);
        msg += "The web service has been unregistered with warnings: " + iExc.getLocalizedMessage();
      }

      WSWarningException wExc = new WSWarningException();
      wExc.addWarning(msg);
      throw wExc;
    }
  }

  public void commitStart(String applicationName) throws WSWarningException {
    notifyProtocolsOnCommitStart(applicationName);
  }

  public void rollbackStart(String applicationName) throws WSWarningException {
    Vector warnings = new Vector();

    try {
      notifyProtocolsOnRollbackStart(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      unregisterWebServices(WSContainer.getWSRegistry().getWebServicesByApplicationName(applicationName));
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void stop(String applicationName) throws WSWarningException {
    WSRuntimeDefinition[] wsRuntimeDefinitions = WSContainer.getWSRegistry().getWebServicesByApplicationName(applicationName);

    Vector warnings = new Vector();
    try {
      clearRuntimeCaches(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      notifyProtocolsOnStop(wsRuntimeDefinitions);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      unregisterWebServices(wsRuntimeDefinitions);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  private String[] getWSDirs(String webServicesDir) {
    Vector wsDirPaths = new Vector();

    File[] wsDirs = new File(webServicesDir).listFiles();
    for (int i = 0; i < wsDirs.length; i++) {
      File wsDir = wsDirs[i];

      if (wsDir.getName().equals(APP_JARS_NAME)) {
        continue;
      } else {
        wsDirPaths.add(wsDir.getAbsolutePath());
      }
    }

    String[] wsDirPathsArray = new String[wsDirPaths.size()];
    wsDirPaths.toArray(wsDirPathsArray);

    return wsDirPathsArray;
  }

  private String[] getWebServicesNames(String[] wsDirs) throws WSDeploymentException {
    Vector webServicesNames = new Vector();

    for (int i = 0; i < wsDirs.length; i++) {
      String wsDir = wsDirs[i];

      if (new File(wsDir).getName().equals(APP_JARS_NAME)) {
        continue;
      } else {
        webServicesNames.add(getWebServiceName(wsDir));
      }
    }

    String[] webServicesNamesArray = new String[webServicesNames.size()];
    webServicesNames.toArray(webServicesNamesArray);

    return webServicesNamesArray;
  }

  private String getWebServiceName(String wsDirectory) throws WSDeploymentException {
    String excMsg = "Error occurred, extracting web service name from directory " + wsDirectory + ". ";

    String wsName = null;
    try {
      Properties mappings = loadMappings(wsDirectory);
      String wsDeploymentDescriptorPath = WSDirsHandler.getWSDeploymentDescriptorPath(wsDirectory, mappings);
      wsName = WSDefinitionFactory.extractWebServicesNames(wsDeploymentDescriptorPath)[0];
    } catch(WSDeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg;
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    return wsName;
  }

  public static WSRuntimeDefinition loadWebService(String applicationName, String wsDirectory) throws WSDeploymentException {
    String excMsg = "Error occurred, loading web service from directory " + wsDirectory + ". ";
    Properties mappings = null;
    boolean baseMappings = false;

    WSRuntimeDefinition wsRuntimeDefinition = null;
    try {
      if(hasMappings(wsDirectory)) {
        mappings = loadMappingsFile(WSDirsHandler.getMappingsPath(wsDirectory));
      } else {
         mappings = WSDirsHandler.generateBaseDefaultMappings();
         baseMappings = true;
      }
      String wsDeploymentDescriptorPath = WSDirsHandler.getWSDeploymentDescriptorPath(wsDirectory, mappings);
      String wsRuntimeDescriptorPath = WSDirsHandler.getWSRuntimeDescriptorPath(wsDirectory, mappings);
      wsRuntimeDefinition = (new WSDefinitionFactory()).loadWebServiceRuntimeMode(applicationName, wsDirectory, mappings);
    } catch(WSDeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg;
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    if(baseMappings) {
      WSDirsHandler.upgradeBaseMappings(mappings, wsRuntimeDefinition.getServiceName());
    }

    setMappings(wsRuntimeDefinition, wsDirectory, mappings);

    return wsRuntimeDefinition;
  }

  public static boolean hasMappings(String wsDirectory){
    return (new File(WSDirsHandler.getMappingsPath(wsDirectory)).exists());
  }

  public static Properties loadMappings(String wsDirectory) throws WSDeploymentException {
    Properties mappings = null;
    if(hasMappings(wsDirectory)) {
      mappings = loadMappingsFile(WSDirsHandler.getMappingsPath(wsDirectory));
    } else {
      mappings = WSDirsHandler.generateBaseDefaultMappings();
    }

    return mappings;
  }

  public static Properties loadMappingsFile(String mappingsPath) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to load " + mappingsPath + ". ";

    Properties mappings = new Properties();
    FileInputStream in = null;
    try {
      in = new FileInputStream(mappingsPath);
      mappings.load(in);
    } catch(IOException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg;
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    } finally {
      if(in != null) {
        try {
          in.close();
        } catch(IOException e) {
          // $JL-EXC$
        }
      }
    }

    return mappings;
  }

  private void setMappings(WSRuntimeDefinition[] wsRuntimeDefinitions, String[] wsDirectories, Properties[] mappings) {
    if(wsRuntimeDefinitions == null) {
      return;
    }

    for(int i = 0; i < wsRuntimeDefinitions.length; i++) {
      setMappings(wsRuntimeDefinitions[i], wsDirectories[i], mappings[i]);
    }
  }

  private static void setMappings(WSRuntimeDefinition wsRuntimeDefinitions, String wsDirectory, Properties mappings) {
    WSDirsHandler wsDirsHandler = new WSDirsHandler(mappings, wsDirectory);
    wsRuntimeDefinitions.setWsDirsHandler(wsDirsHandler);
  }

  private void startSingleWebService(WSRuntimeDefinition wsRuntimeDefinition) throws WSDeploymentException, WSWarningException {
    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();
    String excMsg = "Error occurred, starting web service " + wsIdentifier.getServiceName() + ", application " + wsIdentifier.getApplicationName() + ". ";

    prepareWSRuntimeEnabled( wsRuntimeDefinition);

    registerWebService(wsRuntimeDefinition);

    try {
      WSLogging.applyAndStoreWSConfiguration(wsIdentifier.getApplicationName(), wsIdentifier.getServiceName());
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching("Warning! ", e);

      String msg = excMsg + "Unable to register log configurations. ";
      WSWarningException wExc = new WSWarningException();
      wExc.addWarning(msg);
      throw wExc;
    }
  }

  private void prepareWSRuntimeEnabled(WSRuntimeDefinition wsRuntimeDefinition) throws WSDeploymentException {
    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();
    ClassLoader appLoader = WSContainer.getServiceContext().getCoreContext().getLoadContext().getClassLoader(wsIdentifier.getApplicationName());

    WSDirsHandler wsDirsHandler = wsRuntimeDefinition.getWsDirsHandler();
    try {
      String typesJarFileName = wsDirsHandler.getJarPath();
      File typesJarFile = new File(typesJarFileName);
      if (typesJarFile.exists()) {
        String typeMappingFile = wsDirsHandler.getTypeMappingPath();
        TypeMappingRegistryImpl typeMappingRegistryImpl = new TypeMappingRegistryImpl();
        typeMappingRegistryImpl.fromXML(typeMappingFile, appLoader);
        wsRuntimeDefinition.setTypeMappingRegistry(typeMappingRegistryImpl);

        String javaQNameMappingPath = null;
        if(wsRuntimeDefinition.hasOutsideInDefinition()) {
          javaQNameMappingPath = wsDirsHandler.getOutsideInJavaToQNameMapppingPath(wsRuntimeDefinition.getOutsideInDefinition().getJavaQNameMappingFile());
        } else {
          javaQNameMappingPath = wsDirsHandler.getJavaToQNameMappingPath();
        }

        JavaToQNameMappingRegistry javaToQNameRegistry = JavaToQNameMappingRegistryImpl.loadFromFile(new File(javaQNameMappingPath));
        wsRuntimeDefinition.setJavaToQNameMappingRegistry(javaToQNameRegistry);
       } else {
        wsRuntimeDefinition.setJavaToQNameMappingRegistry(new JavaToQNameMappingRegistryImpl());
        wsRuntimeDefinition.setTypeMappingRegistry(new TypeMappingRegistryImpl());
      }

      ServiceEndpointDefinition[] endpointDefinitions = wsRuntimeDefinition.getServiceEndpointDefinitions();
      int endpointsSize = endpointDefinitions.length;
      for (int i = 0; i < endpointsSize; i++) {
        ServiceEndpointDefinition endpointDefinition = endpointDefinitions[i];
        OperationMappingRegistry operationMappingRegistry = new OperationMappingRegistry();
        endpointDefinition.setOperationMappingRegistry(operationMappingRegistry);
        OperationDefinition[] operations = endpointDefinition.getOperations();
        int operationsSize = operations.length;
        for (int j = 0; j < operationsSize; j++) {
          OperationDefinitionImpl operation = (OperationDefinitionImpl)operations[j];
          Key[] runtimeKeys = operation.getKeys();
          Key[] allKeys = runtimeKeys;

          Hashtable keysHash = getKeysAsHash(runtimeKeys);
          if (!keysHash.containsKey(SOAP_REQUEST_NAME)) {
            Key soapRequestKey = new Key();
            soapRequestKey.setName(SOAP_REQUEST_NAME);
            soapRequestKey.setValue(operation.getOperationName());
            allKeys = unifyKeys(new Key[][]{runtimeKeys, new Key[]{soapRequestKey}});
          }
          operationMappingRegistry.addOperation(allKeys, operation);
        }
      }

      Location wsLocation = WSLogging.getWSLocation(wsIdentifier.getApplicationName(), wsIdentifier.getServiceName());
      wsRuntimeDefinition.setWsLocation(wsLocation);
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{"Unable to prepare webservice runtime enabled ", wsIdentifier.getApplicationName(), wsIdentifier.getJarName(), wsIdentifier.getServiceName()};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  private void registerWebService(WSRuntimeDefinition wsRuntimeDefinition) throws WSDeploymentException {
    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();
    String excMsg = "Error occurred, trying to register web service " + wsIdentifier.getServiceName() + ", application " + wsIdentifier.getApplicationName() + ". ";

    try {
      WSContainer.getWSRegistry().registerWebService(wsRuntimeDefinition);
    } catch(RegistryException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg;
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    try {
      registerServiceEndpoints(wsRuntimeDefinition.getServiceEndpointDefinitions());
    } catch(WSDeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg;
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  private void registerServiceEndpoints(ServiceEndpointDefinition[] seiDefinitions) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to register service endpoints. ";

    RuntimeRegistry runtimeRegistry = WSContainer.getRuntimeRegistry();
    int endpointsSize = seiDefinitions.length;
    for(int i = 0; i < endpointsSize; i++) {
      ServiceEndpointDefinition endpointDefinition = seiDefinitions[i];
      try {
        runtimeRegistry.registerServiceEndpoint(endpointDefinition);
      } catch(RegistryException e) {
        Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
        wsDeployLocation.catching(e);

        String msg = excMsg + "Unable to register service endpoint " + endpointDefinition.getTransportBindingId() + ". ";
        Object[] args = new String[]{msg, "none"};
        throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
      }
    }
  }

  private Hashtable getKeysAsHash(Key[] keys) {
    Hashtable keysHash = new Hashtable();
    if (keys == null) {
      return keysHash;
    }

    int keySize = keys.length;
    for (int i = 0; i < keySize; i++) {
      Key key = keys[i];
      keysHash.put(key.getName(), key.getValue());
    }

    return keysHash;
  }

  private Key[] unifyKeys(Key[][] keys) {
    Key[] allKeys = new Key[0];
    int size = keys.length;
    for(int i = 0; i < size; i++) {
       Key[] currentKeys = keys[i];
       Key[] newKeys = new Key[allKeys.length + currentKeys.length];
       System.arraycopy(allKeys, 0, newKeys, 0, allKeys.length);
       System.arraycopy(currentKeys, 0, newKeys, allKeys.length, currentKeys.length);
       allKeys = newKeys;
    }
    return allKeys;
  }

  public void unregisterWebServices(WSRuntimeDefinition[] wsRuntimeDefinitions) throws WSWarningException {
    Vector warnings = new Vector();

    for(int i = 0; i < wsRuntimeDefinitions.length; i++) {
      try {
        unregisterWebService(wsRuntimeDefinitions[i]);
      } catch(WSWarningException e) {
        warnings.addAll(e.getWarningsVector());
      }
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void unregisterWebService(WSRuntimeDefinition wsRuntimeDefinition) throws WSWarningException {
    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();
    String excMsg = "Error occurred, trying to unregister web service from WS Runtime, application " + wsIdentifier.getApplicationName() + ", web service " + wsIdentifier.getServiceName() + ". ";

    Vector warnings = new Vector();
    try {
      WSLogging.removeAndStoreWSConfiguration(wsIdentifier.getApplicationName(), wsIdentifier.getServiceName());
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching("Warning! ", e);

      String msg = excMsg + "Unexpected error occurred: " + e.getLocalizedMessage() + ". " ;
      warnings.add(msg);
    }

    try {
      WSContainer.getWSRegistry().unregisterWebService(wsIdentifier);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching("Warning! ", e);

      String msg = excMsg + "Unexpected error occurred: " + e.getLocalizedMessage() + ". " ;
      warnings.add(msg);
    }

    try {
      unregisterSEIs(WebServicesUtil.getSEITransportIds(wsRuntimeDefinition.getServiceEndpointDefinitions()));      
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching("Warning! ", e);

      String msg = excMsg + "Unexpected error occurred: " + e.getLocalizedMessage() + ". " ;
      warnings.add(msg);
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void unregisterSEIs(String[] seiTransportIds) {
    RuntimeRegistry runtimeRegistry = WSContainer.getRuntimeRegistry();
    if(seiTransportIds == null) {
      return;
    }

    for(int i = 0; i < seiTransportIds.length; i++) {
      runtimeRegistry.unregisterServiceEndpoint(seiTransportIds[i]);
    }
  }

  private void clearRuntimeCaches(String applicationName) throws WSWarningException {
//    String excMsg = "Unexpected error occurred, trying to clear web services caches. ";
//    try {
//      Context ctx = new InitialContext();
//      ServletDispatcher wsDispatcher = (ServletDispatcher)ctx.lookup(WebServicesFrame.WS_CONTEXT_NAME + "/" + ServletDispatcher.NAME);
//      RuntimeProcessor runtimeProcessor = ((ServletDispatcherImpl)wsDispatcher).getWsProcessor();
//      runtimeProcessor.clearApplicationCaches(applicationName);
//    } catch(Exception e) {
//      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
//      wsLocation.catching(e);
//
//      WSWarningException wExc = new WSWarningException(e);
//      wExc.addWarning(excMsg);
//      throw wExc;
//    }
  }

  public void notifyProtocolsOnDownloadFiles(String applicationName, String[] wsDirNames, String[] wsConfigNames, Configuration appConfiguration) throws WSWarningException {
    String excMsg = "Error occurred, trying to notify protocols on " + WSProtocolNotificator.getModeName(WSProtocolNotificator.DOWNLOAD) + " phase for application " + applicationName + ". ";

    if(wsDirNames == null || wsDirNames.length == 0) {
      return;
    }

    String webservicesDir = null;
    try {
      String wsContainerDir = WSContainer.getWSDeployer().getWSContainerDir(applicationName);
      webservicesDir = WSDirsHandler.getWebServicesDir(wsContainerDir);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Unable to get " + WS_CONTAINER_NAME + " directory. ";
      WSWarningException wExc = new WSWarningException();
      wExc.addWarning(msg);
      throw wExc;
    }

    String[] wsDirs = IOUtil.getFilePaths(webservicesDir, wsDirNames);

    String[] webServicesNames = null;
    try {
      webServicesNames = getWebServicesNames(wsDirs);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching("Warning! ", e);

      String msg = excMsg + "Error occurred, trying to extract web services names. ";
      WSWarningException wExc = new WSWarningException();
      wExc.addWarning(msg);
      throw wExc;
    }

    new WSProtocolNotificator().downloadFiles(applicationName, webServicesNames, wsDirs, wsConfigNames, appConfiguration);
  }

  public void notifyProtocolsOnStart(String applicationName, Configuration appConfiguration) throws WSWarningException {
    WSRuntimeDefinition[] wsRuntimeDefinitions = WSContainer.getWSRegistry().getWebServicesByApplicationName(applicationName);
    lcmContext.putContext(applicationName, new WSBaseContext(applicationName, wsRuntimeDefinitions, appConfiguration));
    new WSProtocolNotificator().onStart(applicationName, wsRuntimeDefinitions, appConfiguration);    
  }

  public void notifyProtocolsOnCommitStart(String applicationName) throws WSWarningException {
    notifyProtocolsOnShortPhases(applicationName, WSProtocolNotificator.COMMIT_START);
  }

  public void notifyProtocolsOnRollbackStart(String applicationName) throws WSWarningException {
    notifyProtocolsOnShortPhases(applicationName, WSProtocolNotificator.ROLLBACK_START);
  }

  private void notifyProtocolsOnShortPhases(String applicationName, int mode) throws WSWarningException {
    WSBaseContext wsBaseContext = lcmContext.getContext(applicationName);
    if(wsBaseContext == null) {
      return;
    }

    switch (mode) {
      case WSProtocolNotificator.COMMIT_START: {
        new WSProtocolNotificator().onCommitStart(wsBaseContext.getWsRuntimeDefinitions());
        break;
      }
      case WSProtocolNotificator.ROLLBACK_START: {
        new WSProtocolNotificator().onRollbackStart(wsBaseContext.getWsRuntimeDefinitions());
        break;
      }
    }

    lcmContext.removeContext(applicationName);
  }

  private void notifyProtocolsOnStop(WSRuntimeDefinition[] wsRuntimeDefinitions) throws WSWarningException {
    new WSProtocolNotificator().onStop(wsRuntimeDefinitions);
  }

  private String[] getStrings(Enumeration strEnum, int size) {
    if(strEnum == null) {
      return new String[0];
    }

    String[] strs = new String[size];
    int i = 0;
    while(strEnum.hasMoreElements()) {
      strs[i++] = (String)strEnum.nextElement();
    }

    return strs;
  }

}
