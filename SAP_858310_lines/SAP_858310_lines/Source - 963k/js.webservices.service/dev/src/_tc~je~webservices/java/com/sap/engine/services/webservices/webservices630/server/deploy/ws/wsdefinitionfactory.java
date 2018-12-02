package com.sap.engine.services.webservices.webservices630.server.deploy.ws;

import com.sap.engine.interfaces.webservices.runtime.*;
import com.sap.engine.interfaces.webservices.runtime.definition.WSIdentifier;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.lib.descriptors.ws04vi.*;
import com.sap.engine.lib.descriptors.ws04wsdd.*;
import com.sap.engine.lib.descriptors.ws04wsrt.RuntimeOperationConfigDescriptor;
import com.sap.engine.lib.descriptors.ws04wsrt.WSRuntimeConfigurationDescriptor;
import com.sap.engine.lib.descriptors.ws04wsrt.WSRuntimeDescriptor;
import com.sap.engine.lib.processor.SchemaProcessorFactory;
import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.services.webservices.exceptions.WSGenerationException;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.exceptions.PatternKeys;
import com.sap.engine.services.webservices.runtime.definition.*;
import com.sap.engine.services.webservices.runtime.wsdl.WSDProcessor;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.jar.JarUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.deploy.WSDescriptorsLocationTable;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.preprocess.*;
import com.sap.tc.logging.Location;

import javax.xml.namespace.QName;
import java.io.*;
import java.util.*;

/**
 * Title: WSDefinitionFactory
 * Description: This class contains methods for constructing web services runtime objects, using data from ws-deployment-descriptor.xml and ws-runtime-descriptor.xml.
 * Copyright: Copyright (c) 2000
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSDefinitionFactory implements WebServicesConstants {

  private JarUtil jarUtil = null;

  public WSDefinitionFactory() {
    jarUtil = new JarUtil();
  }

  public static String[] extractWebServicesNames(String wsDeploymentDescriptorPath) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to extract web service names from descriptor " + wsDeploymentDescriptorPath + ". ";

    WSDeploymentDescriptor wsDeploymentDescriptor = null;
    try {
      wsDeploymentDescriptor = (WSDeploymentDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04WSDD).parse(wsDeploymentDescriptorPath);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    WSDescriptor[] wsDescriptors = wsDeploymentDescriptor.getWebservice();
    if (wsDescriptors == null) {
      return new String[0];
    }

    String[] webServicesNames = new String[wsDescriptors.length];
    for(int i = 0; i < webServicesNames.length; i++) {
      webServicesNames[i] = wsDescriptors[i].getWebserviceInternalName().trim();
    }

    return webServicesNames;
  }

  public static String extractModuleName(String wsRuntimeDescriptorPath) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to extract module names from descriptor " + wsRuntimeDescriptorPath + ". ";

    WSRuntimeDescriptor wsRuntimeDescriptor = null;
    try {
      wsRuntimeDescriptor = (WSRuntimeDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04WSRT).parse(wsRuntimeDescriptorPath);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    return extractModuleNames(new WSRuntimeDescriptor[]{wsRuntimeDescriptor})[0];
  }

  public static String[] extractModuleNames(WSRuntimeDescriptor[] wsRuntimeDescriptors) {
    if(wsRuntimeDescriptors == null) {
      return new String[0];
    }

    String[] moduleNames = new String[wsRuntimeDescriptors.length];
    for(int i = 0; i < wsRuntimeDescriptors.length; i++) {
      moduleNames[i] = wsRuntimeDescriptors[i].getJarName().trim();
    }

    return moduleNames;
  }


  public WSDeploymentInfo[] loadWebServices(String applicationName, String webServicesWorkingDir, File moduleArchive) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to load web services for module " + moduleArchive.getAbsolutePath() + ". ";
        
    WSDeploymentInfo[] wsDeploymentInfoes = new WSDeploymentInfo[0];    
    WSArchiveFilesLocationHandler wsArchiveFilesLocationHandler = null; 
    try {      
      wsArchiveFilesLocationHandler = new WSArchiveFilesLocationHandler(moduleArchive, WSDirsHandler.getModuleWorkingDir(webServicesWorkingDir, moduleArchive.getName()));
      wsDeploymentInfoes = parseWSDeploymentDescriptor(applicationName, wsArchiveFilesLocationHandler);
    } catch(IOException e) {      
      Location wsDeplLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeplLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    } finally {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);

      String msg = excMsg + "Unable to close input stream for module. ";
      try {
        if (wsArchiveFilesLocationHandler != null) {
          wsArchiveFilesLocationHandler.close();
        }
      } catch(IOException ioExc) {
        wsDeployLocation.catching(msg, ioExc);
      }
    }

    return wsDeploymentInfoes;
  }

  public WSDeploymentInfo[] loadWebServices(String webServicesWorkingDir, WSArchiveLocationWrapper[] wsArchiveLocationWrappers) throws WSDeploymentException {
    if(wsArchiveLocationWrappers == null) {
      return new WSDeploymentInfo[0];
    }

    WSDeploymentInfo[] wsDeploymentInfoes = new WSDeploymentInfo[wsArchiveLocationWrappers.length];
    for(int i = 0; i < wsArchiveLocationWrappers.length; i++) {
      wsDeploymentInfoes[i] = loadWebService(webServicesWorkingDir, wsArchiveLocationWrappers[i]);
    }

    return wsDeploymentInfoes;
  }

  public WSDeploymentInfo loadWebService(String webServicesWorkingDir, WSArchiveLocationWrapper wsArchiveLocationWrapper) throws WSDeploymentException {
    WSArchiveFilesLocationHandler wsArchiveFilesLocationHandler = wsArchiveLocationWrapper.getWsArchiveFilesLocationHandler();
    String moduleWorkingDir = WSDirsHandler.getModuleWorkingDir(webServicesWorkingDir, wsArchiveFilesLocationHandler.getModuleArchive().getName());
    WSDeploymentInfo wsDeploymentInfo = new WSDeploymentInfo();
    wsArchiveFilesLocationHandler.setWorkingDir(moduleWorkingDir);
    wsArchiveFilesLocationHandler.setWsDeploymentInfo(wsDeploymentInfo);

    String applicationName = wsArchiveLocationWrapper.getApplicationName();
    String version = wsArchiveLocationWrapper.getVersion();
    WSDescriptor wsDescriptor = wsArchiveLocationWrapper.getWsDescriptor();

    try {
      parseWSDescriptor(applicationName, version, wsDescriptor, wsArchiveFilesLocationHandler, wsDeploymentInfo);
      setAdditionalWSDeploymentSettings(wsArchiveFilesLocationHandler.getModuleArchive().getName(), wsDeploymentInfo);
    } finally {
      try {
        if(wsArchiveFilesLocationHandler != null) {
          wsArchiveFilesLocationHandler.close();
        }
      } catch(IOException e) {
        Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
        wsDeployLocation.catching("Warning!", e);
      }
    }

    return wsDeploymentInfo;
  }

  public WSDeploymentInfo loadWebService(String webServicesWorkingDir, ExtArchiveLocationWrapper extArchiveLocationWrapper) throws WSDeploymentException {
    WSArchiveFilesLocationHandler wsArchiveFilesLocationHandler = extArchiveLocationWrapper.getWsArchiveFilesLocationHandler();
    String moduleWorkingDir = WSDirsHandler.getModuleWorkingDir(webServicesWorkingDir, wsArchiveFilesLocationHandler.getModuleArchive().getName());
    WSDeploymentInfo wsDeploymentInfo = new WSDeploymentInfo();
    wsArchiveFilesLocationHandler.setWorkingDir(moduleWorkingDir);
    wsArchiveFilesLocationHandler.setWsDeploymentInfo(wsDeploymentInfo);

    String applicationName = extArchiveLocationWrapper.getApplicationName();
    String version = extArchiveLocationWrapper.getVersion();
    WSDescriptor wsDescriptor = extArchiveLocationWrapper.getWsDescriptor();

    parseWSDescriptor(applicationName, version, wsDescriptor, wsArchiveFilesLocationHandler, wsDeploymentInfo);
    setAdditionalWSDeploymentSettings(wsArchiveFilesLocationHandler.getModuleArchive().getName(), wsDeploymentInfo);

    return wsDeploymentInfo;
  }

  public WSRuntimeDefinition loadWebServiceRuntimeMode(String applicationName, String wsDir, Properties mappings) throws WSDeploymentException {
    WSDirsHandler wsDirsHandler = new WSDirsHandler(mappings, wsDir);
    return loadWebServiceRuntimeMode(applicationName, wsDirsHandler);
  }

  public WSRuntimeDefinition loadWebServiceRuntimeMode(String applicationName, WSDirsHandler wsDirsHandler) throws WSDeploymentException {
    String excMsg = "Error occurred, loading web services. ";

    WSDeploymentDescriptor wsDeploymentDescriptor = null;
    InputStream wsDeploymentDescriptorInputStream = null;
    try {
      wsDeploymentDescriptorInputStream = wsDirsHandler.getWSDeploymentDescriptorInputStream();
      wsDeploymentDescriptor = (WSDeploymentDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04WSDD).parse(wsDeploymentDescriptorInputStream);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + " Unable to parse " + WSDeploymentDescriptor.class.getName() + " descriptor, source " + wsDirsHandler.getWSDeploymentDescriptorLocationMsg() + ". ";
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    } finally {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);

      String msg = excMsg + " Unable to close input stream for source " + wsDirsHandler.getWSDeploymentDescriptorLocationMsg() + ". ";
      IOUtil.closeInputStreams(new InputStream[]{wsDeploymentDescriptorInputStream}, new String[]{msg}, wsDeployLocation);
    }

    WSRuntimeDescriptor wsRuntimeDescriptor = null;
    String wsRuntimeDescriptorPath = wsDirsHandler.getWSRuntimeDescriptorPath();
    try {
      wsRuntimeDescriptor = loadWSRuntimeDescriptors(new String[]{wsRuntimeDescriptorPath})[0];
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg;
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    Hashtable notParsedDescriptors = new Hashtable();
    WSRuntimeDefinition wsRuntimeDefinition = loadWebServicesRuntimeMode(applicationName, wsDeploymentDescriptor.getVersion().trim(), wsDeploymentDescriptor.getWebservice(), new WSRuntimeDescriptor[]{wsRuntimeDescriptor}, wsDirsHandler, notParsedDescriptors)[0];

    if(notParsedDescriptors.size() > 0) {
      String[] prefixMsgs = WSUtil.addPrefixToStrings(excMsg + "Unable to load web service with corresponding descriptor ", new String[]{wsRuntimeDescriptorPath});
      prefixMsgs = WSUtil.addSuffixToStrings(prefixMsgs, ", nested exception is: ");
      Vector msgs = constructMsgs(prefixMsgs, notParsedDescriptors);

      Object[] args = new String[]{WSUtil.concatStrings(msgs, "")};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args);
    }

    return wsRuntimeDefinition;
  }

  public WSRuntimeDefinition[] loadWebServicesRuntimeMode(String applicationName, String version, WSDescriptor[] wsDescriptors, WSRuntimeDescriptor[] wsRuntimeDescriptors, WSDirsHandler wsDirsHandler, Hashtable notLoadedWS) {
    if(wsRuntimeDescriptors == null) {
      return new WSRuntimeDefinition[0];
    }

    Vector wsRuntimeDefinitions = new Vector();
    for(int i = 0; i < wsDescriptors.length; i++) {
      try {
        WSRuntimeDefinition wsRuntimeDefinition = loadWebServiceRuntimeMode(applicationName, version, wsDescriptors[i], wsRuntimeDescriptors[i], wsDirsHandler);
        wsRuntimeDefinitions.add(wsRuntimeDefinition);
      } catch(WSDeploymentException e) {
        Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
        wsDeployLocation.catching("Warning! ", e);

        notLoadedWS.put(new Integer(i), e.getLocalizedMessage());
      }
    }

    WSRuntimeDefinition[] wsRuntimeDefinitionsArr = new WSRuntimeDefinition[wsRuntimeDefinitions.size()];
    wsRuntimeDefinitions.copyInto(wsRuntimeDefinitionsArr);

    return wsRuntimeDefinitionsArr;
  }

  public WSRuntimeDefinition loadWebServiceRuntimeMode(String applicationName, String version, WSDescriptor wsDescriptor, WSRuntimeDescriptor wsRuntimeDescriptor, WSDirsHandler wsDirsHandler) throws WSDeploymentException{
    String excMsg = "Error occurred, loading web service from " + WSDescriptor.class.getName() + " and " + WSRuntimeDescriptor.class.getName() + ". ";
    WSRuntimeDefinition wsRuntimeDefinition = null;

    try {
      WSDeploymentInfo wsDeploymentInfo = new WSDeploymentInfo();
      parseWSDescriptor(applicationName, version, wsDescriptor, wsDirsHandler, wsDeploymentInfo);
      wsRuntimeDefinition = wsDeploymentInfo.getWsRuntimeDefinition();
      parseRuntimeSpecific(applicationName, wsRuntimeDefinition,  wsRuntimeDescriptor);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg;
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    return wsRuntimeDefinition;
  }

  public void saveRuntime(String fileName, WSRuntimeDefinition wsRuntimeDefinition) throws WSDeploymentException {
    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();
    String excMsg = "Error occurred, trying to generate and/or save " + WebServicesConstants.WS_RUNTIME_DESCRIPTOR  + ", application " + wsIdentifier.getApplicationName() + ", service " + wsIdentifier.getServiceName() + ". ";

    WSRuntimeDescriptor wsRuntimeDescriptor = getWSRuntimeDescriptor(wsRuntimeDefinition);
    try {
      SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04WSRT).build(wsRuntimeDescriptor, fileName);
      //WSRuntimeParser.saveWSRuntimeDescriptor(wsRuntimeDescriptor, fileName);
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{excMsg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  private WSRuntimeDescriptor[] loadWSRuntimeDescriptors(String[] wsRuntimeDescriptorPaths) throws Exception {
    if(wsRuntimeDescriptorPaths == null) {
      return new WSRuntimeDescriptor[0];
    }

    WSRuntimeDescriptor[] wsRuntimeDescriptors = new WSRuntimeDescriptor[wsRuntimeDescriptorPaths.length];
    for(int i = 0; i < wsRuntimeDescriptorPaths.length; i++) {
      wsRuntimeDescriptors[i] = (WSRuntimeDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04WSRT).parse(wsRuntimeDescriptorPaths[i]);
    }

    return wsRuntimeDescriptors;
  }

  private Vector constructMsgs(String[] prefixMsgs, Hashtable tailMsgs) {
    if(tailMsgs == null) {
      return new Vector();
    }

    Vector msgs = new Vector();
    Enumeration enum1 = tailMsgs.keys();
    int i = 0;
    while(enum1.hasMoreElements()) {
      Integer number = (Integer)enum1.nextElement();
      String msg = prefixMsgs[i] + (String)tailMsgs.get(number);
      msgs.add(msg);
      i++;
    }

    return msgs;
  }

  public WSArchiveLocationWrapper[] loadWSArchiveLocationWrappers(String applicationName, File[] moduleArchives) throws WSDeploymentException {
    if(moduleArchives == null) {
      return new WSArchiveLocationWrapper[0];
    }

    WSArchiveLocationWrapper[] wsArchiveLocationWrappers = new WSArchiveLocationWrapper[0];
    for(int i = 0; i < moduleArchives.length; i++) {
      WSArchiveLocationWrapper[] currentWSArchiveLocationWrappers = loadWSArchiveLocationWrappers(applicationName, moduleArchives[i]);
      WSArchiveLocationWrapper[] newWSArchiveLocationWrappers = new WSArchiveLocationWrapper[wsArchiveLocationWrappers.length + currentWSArchiveLocationWrappers.length];
      System.arraycopy(wsArchiveLocationWrappers, 0, newWSArchiveLocationWrappers, 0, wsArchiveLocationWrappers.length);
      System.arraycopy(currentWSArchiveLocationWrappers, 0, newWSArchiveLocationWrappers, wsArchiveLocationWrappers.length, currentWSArchiveLocationWrappers.length);
      wsArchiveLocationWrappers = newWSArchiveLocationWrappers;
    }

    return wsArchiveLocationWrappers;
  }

  public WSArchiveLocationWrapper[] loadWSArchiveLocationWrappers(String applicationName, File moduleArchive) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to load web services location information for module " + moduleArchive.getAbsolutePath() + ". ";

    try {
      if(containsWSDeploymentDescriptor(moduleArchive) == null) {
        return new WSArchiveLocationWrapper[0];
      }
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    WSArchiveLocationWrapper[] wsArchiveLocationWrappers = new WSArchiveLocationWrapper[0];
    WSArchiveFilesLocationHandler wsArchiveFilesLocationHandler = null;
    try {
      wsArchiveFilesLocationHandler = new WSArchiveFilesLocationHandler(moduleArchive);
      wsArchiveLocationWrappers = loadWSArchiveLocationWrappers(applicationName, wsArchiveFilesLocationHandler);
    } catch(IOException e) {
      Location wsDeplLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeplLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    } finally {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);

      String msg = excMsg + "Unable to close input stream for module. ";
      try {
        if (wsArchiveFilesLocationHandler != null) {
          wsArchiveFilesLocationHandler.close();
        }
      } catch(IOException ioExc) {
        wsDeployLocation.catching(msg, ioExc);
      }
    }

    return wsArchiveLocationWrappers;
  }

  public WSArchiveLocationWrapper[] loadWSArchiveLocationWrappers(String applicationName, WSArchiveFilesLocationHandler wsArchiveFilesLocationHandler) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to load web service archive location wrapper, from descriptors: " + wsArchiveFilesLocationHandler.getWSDeploymentDescriptorLocationMsg() + ". ";

    InputStream wsDeploymentDescriptorInputStream = null;
    WSDeploymentDescriptor wsDeploymentDescriptor = null;
    try {
      wsDeploymentDescriptorInputStream = wsArchiveFilesLocationHandler.getWSDeploymentDescriptorInputStream();
      wsDeploymentDescriptor = (WSDeploymentDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04WSDD).parse(wsDeploymentDescriptorInputStream);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    String version = wsDeploymentDescriptor.getVersion().trim();

    return loadWSArchiveLocationWrappers(applicationName, version, wsDeploymentDescriptor.getWebservice(), wsArchiveFilesLocationHandler);
  }

  private WSArchiveLocationWrapper[] loadWSArchiveLocationWrappers(String applicationName, String version, WSDescriptor[] wsDescriptors, WSArchiveFilesLocationHandler wsArchiveFilesLocationHandler) {
    if(wsDescriptors == null) {
      return new WSArchiveLocationWrapper[0];
    }

    WSArchiveLocationWrapper[] wsArchiveLocationWrappers = new WSArchiveLocationWrapper[wsDescriptors.length];
    for(int i = 0; i < wsDescriptors.length; i++) {
        wsArchiveLocationWrappers[i] = loadWSArchiveLocationWrapper(applicationName, version, wsDescriptors[i], wsArchiveFilesLocationHandler);
    }

    return wsArchiveLocationWrappers;
  }

  private WSArchiveLocationWrapper loadWSArchiveLocationWrapper(String applicationName, String version, WSDescriptor wsDescriptor, WSArchiveFilesLocationHandler wsArchiveFilesLocationHandler) {
    WSArchiveLocationWrapper wsArchiveLocationWrapper = new WSArchiveLocationWrapper();
    loadWSArchiveLocationWrapper(applicationName, version, wsDescriptor, wsArchiveFilesLocationHandler, wsArchiveLocationWrapper);
    return wsArchiveLocationWrapper;
  }

  private void loadWSArchiveLocationWrapper(String applicationName, String version, WSDescriptor wsDescriptor, WSArchiveFilesLocationHandler wsArchiveFilesLocationHandler, WSArchiveLocationWrapper wsArchiveLocationWrapper) {
    loadWSLocationWrapper(applicationName, wsDescriptor, wsArchiveLocationWrapper, wsArchiveFilesLocationHandler);
    wsArchiveLocationWrapper.setVersion(version);
    wsArchiveLocationWrapper.setWsDescriptor(wsDescriptor);
  }

  public ExtArchiveLocationWrapper loadExtArchiveLocationWrapper(WSArchiveLocationWrapper wsArchiveLocationWrapper, WSDescriptor wsDescriptor, WSArchiveFilesLocationHandler wsArchiveFilesLocationHandler) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to load extended information for web service " + wsArchiveLocationWrapper.getWebServiceName() + ", application " + wsArchiveLocationWrapper.getApplicationName() + ". ";

    ExtArchiveLocationWrapper extArchiveLocationWrapper = new ExtArchiveLocationWrapper();
    wsArchiveLocationWrapper.clone(extArchiveLocationWrapper);

    if(wsDescriptor.getOutsideInDescriptor() != null) {
      extArchiveLocationWrapper.setOutsideInMode(true);
    }

    try {
      extArchiveLocationWrapper.setWsDescriptorsLocationTable(loadWSDescriptorsTable(wsDescriptor, extArchiveLocationWrapper.getWsArchiveFilesLocationHandler()));
    } catch(WSDeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    extArchiveLocationWrapper.setSeiDescriptorsTable(loadSeiDescriptorsTable(wsDescriptor.getWsConfiguration()));
    return extArchiveLocationWrapper;
  }

  public WSFileLocationWrapper loadWSFileLocationWrapper(String applicationName, WSDirsHandler wsDirsHandler) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to load web service file location wrapper, from descriptors: " + wsDirsHandler.getWSDeploymentDescriptorPath() + " and " + wsDirsHandler.getWSRuntimeDescriptorPath();

    InputStream wsDeploymentDescriptorInputStream = null;
    WSDeploymentDescriptor wsDeploymentDescriptor = null;
    try {
      wsDeploymentDescriptorInputStream = wsDirsHandler.getWSDeploymentDescriptorInputStream();
      wsDeploymentDescriptor = (WSDeploymentDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04WSDD).parse(wsDeploymentDescriptorInputStream);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    String wsRuntimeDescriptorPath = null;
    WSRuntimeDescriptor wsRuntimeDescriptor = null;
    try {
      wsRuntimeDescriptorPath = wsDirsHandler.getWSRuntimeDescriptorPath();
      wsRuntimeDescriptor = (WSRuntimeDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04WSRT).parse(wsRuntimeDescriptorPath);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    return loadWSFileLocationWrapper(applicationName, wsDeploymentDescriptor.getWebservice()[0], wsRuntimeDescriptor, wsDirsHandler);
  }

  public ExtFileLocationWrapper loadExtFileLocationWrapper(WSFileLocationWrapper wsFileLocationWrapper, WSDirsHandler wsDirsHandler) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to load web service file location wrapper, from descriptors: " + wsDirsHandler.getWSDeploymentDescriptorPath() + " and " + wsDirsHandler.getWSRuntimeDescriptorPath();

    InputStream wsDeploymentDescriptorInputStream = null;
    WSDeploymentDescriptor wsDeploymentDescriptor = null;
    try {
      wsDeploymentDescriptorInputStream = wsDirsHandler.getWSDeploymentDescriptorInputStream();
      wsDeploymentDescriptor = (WSDeploymentDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04WSDD).parse(wsDeploymentDescriptorInputStream);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    return loadExtFileLocationWrapper(wsFileLocationWrapper, wsDeploymentDescriptor.getWebservice()[0], wsDirsHandler);
  }

  private WSFileLocationWrapper loadWSFileLocationWrapper(String applicationName, WSDescriptor wsDescriptor, WSRuntimeDescriptor wsRuntimeDescriptor, WSDirsHandler wsDirsHandler) {
    return loadWSFileLocationWrapper(applicationName, wsRuntimeDescriptor.getJarName().trim(), wsDescriptor, wsDirsHandler);
  }

  private WSFileLocationWrapper loadWSFileLocationWrapper(String applicationName, String moduleName, WSDescriptor wsDescriptor, WSDirsHandler wsDirsHandler) {
    WSFileLocationWrapper wsFileLocationWrapper = new WSFileLocationWrapper();
    loadWSFileLocationWrapper(applicationName, moduleName, wsDescriptor, wsDirsHandler, wsFileLocationWrapper);
    return wsFileLocationWrapper;
  }

  private void loadWSFileLocationWrapper(String applicationName, String moduleName, WSDescriptor wsDescriptor, WSDirsHandler wsDirsHandler, WSFileLocationWrapper wsFileLocationWrapper) {
    loadWSLocationWrapper(applicationName, wsDescriptor, wsFileLocationWrapper, wsDirsHandler);
    wsFileLocationWrapper.setModuleName(moduleName);
  }

  public ExtFileLocationWrapper loadExtFileLocationWrapper(WSFileLocationWrapper wsFileLocationWrapper, WSDescriptor wsDescriptor, WSDirsHandler wsDirsHandler) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to load extended information for web service " + wsFileLocationWrapper.getWebServiceName() + ", application " + wsFileLocationWrapper.getApplicationName() + ". ";

    ExtFileLocationWrapper extFileLocationWrapper = new ExtFileLocationWrapper();
    wsFileLocationWrapper.clone(extFileLocationWrapper);

    if(wsDescriptor.getOutsideInDescriptor() != null) {
      extFileLocationWrapper.setOutsideInMode(true);
    }

    try {
      extFileLocationWrapper.setWsDescriptorsLocationTable(loadWSDescriptorsTable(wsDescriptor, extFileLocationWrapper.getWsDirsHandler()));
    } catch(WSDeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    extFileLocationWrapper.setSeiDescriptorsTable(loadSeiDescriptorsTable(wsDescriptor.getWsConfiguration()));

    return extFileLocationWrapper;
  }

  private void loadWSLocationWrapper(String applicationName, WSDescriptor wsDescriptor, WSLocationWrapper wsLocationWrapper, WSFilesLocationHandler wsFilesLocationHandler) {
    String wsName = wsDescriptor.getWebserviceInternalName().trim();
    wsLocationWrapper.setApplicationName(applicationName);
    wsLocationWrapper.setWebServiceName(wsName);

    wsLocationWrapper.setWsConfigurationNames(loadWSConfigurationNames(wsDescriptor.getWsConfiguration()));
    wsLocationWrapper.setSeiTrAddressTable(loadSeiTrAddressesTable(wsDescriptor.getWsConfiguration()));
    wsLocationWrapper.setWsFilesLocationHandler(wsFilesLocationHandler);
  }

  private String[] loadWSConfigurationNames(WSConfigurationDescriptor[] wsConfigurationDescriptors) {
    if(wsConfigurationDescriptors == null) {
      return new String[0];
    }

    String[] wsConfigurationNames = new String[wsConfigurationDescriptors.length];
    for(int i = 0; i < wsConfigurationDescriptors.length; i++) {
      wsConfigurationNames[i] = wsConfigurationDescriptors[i].getConfigurationName().trim();
    }

    return wsConfigurationNames;
  }

  private Hashtable loadSeiTrAddressesTable(WSConfigurationDescriptor[] wsConfigurationDescriptors) {
    if(wsConfigurationDescriptors == null) {
      return new Hashtable();
    }

    Hashtable seiTrAddressesTable = new Hashtable();
    for(int i = 0; i < wsConfigurationDescriptors.length; i++) {
      WSConfigurationDescriptor wsConfigurationDescriptor = wsConfigurationDescriptors[i];
     seiTrAddressesTable.put(wsConfigurationDescriptor.getConfigurationName().trim(), wsConfigurationDescriptor.getTransportAddress().trim());
    }

    return seiTrAddressesTable;
  }

  private WSDescriptorsLocationTable loadWSDescriptorsTable(WSDescriptor wsDescriptor, WSFilesLocationHandler wsFilesLocationHandler) throws WSDeploymentException {
    WSDescriptorsLocationTable wsDescriptorsLocationTable = new WSDescriptorsLocationTable();

    if(wsDescriptor.getOutsideInDescriptor() != null) {

      OutsideInDescriptor outsideInDescriptor = wsDescriptor.getOutsideInDescriptor();
      String wsdlRelPath = IOUtil.convertPackageToDirPath(outsideInDescriptor.getWsdlRef().getPackage().trim(), outsideInDescriptor.getWsdlRef().getName().trim());
      String javaToQNameMappingRelPath = IOUtil.convertPackageToDirPath(outsideInDescriptor.getJavaQnameMappingRef().getPackage().trim(), outsideInDescriptor.getJavaQnameMappingRef().getName().trim());
      wsDescriptorsLocationTable.setDescriptor(WSDescriptorsLocationTable.WSDL, wsdlRelPath);
      wsDescriptorsLocationTable.setDescriptor(WSDescriptorsLocationTable.JAVA_TO_QNAME_MAPPING, javaToQNameMappingRelPath);
    }

    loadExtendedLocationInfo(wsDescriptorsLocationTable, wsDescriptor.getWsConfiguration(), wsFilesLocationHandler);

    return wsDescriptorsLocationTable;
  }

  private void loadExtendedLocationInfo(WSDescriptorsLocationTable wsDescriptorsLocationTable, WSConfigurationDescriptor[] wsConfigurationDescriptors, WSFilesLocationHandler wsFilesLocationHandler) throws WSDeploymentException {
    if(wsConfigurationDescriptors == null) {
      return;
    }

    for(int i = 0; i < wsConfigurationDescriptors.length; i++) {
      loadExtendedLocationInfo(wsDescriptorsLocationTable, wsConfigurationDescriptors[i], wsFilesLocationHandler);
    }
  }

  private void loadExtendedLocationInfo(WSDescriptorsLocationTable wsDescriptorsLocationTable, WSConfigurationDescriptor wsConfigurationDescriptor, WSFilesLocationHandler wsFilesLocationHandler) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to load extended information (descriptors locations) from " + wsConfigurationDescriptor.getClass().getName() + ", location " + wsFilesLocationHandler.getLocationMsg() + ". ";

    NameDescriptor wsdNameDescriptor = wsConfigurationDescriptor.getWebserviceDefinitionRef();
    String wsdPackage = wsdNameDescriptor.getPackage().trim();
    String wsdName = wsdNameDescriptor.getName().trim();
    String wsdRelPath = IOUtil.convertPackageToDirPath(wsdPackage, wsdName);
    if(!wsDescriptorsLocationTable.containsDesctiptor(WSDescriptorsLocationTable.WSD)) {
     wsDescriptorsLocationTable.setDescriptor(WSDescriptorsLocationTable.WSD, wsdRelPath);
    }

    try {
      if(!wsDescriptorsLocationTable.containsDesctiptor(WSDescriptorsLocationTable.DOC)) {
        String[] docRelPathTemplates = constructRelPathTemplates(wsdPackage.replace('.', WebServicesConstants.SEPARATOR), wsdName, WS_DOCUMENTATION_EXTENSIONS);
        String docRelPath = wsFilesLocationHandler.findDocRelPath(docRelPathTemplates);
        if(docRelPath != null) {
          wsDescriptorsLocationTable.setDescriptor(WSDescriptorsLocationTable.DOC, docRelPath);
        }
      }
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  private Hashtable loadSeiDescriptorsTable(WSConfigurationDescriptor[] wsConfigurationDescriptors) {
    if(wsConfigurationDescriptors == null) {
      return new Hashtable();
    }

    Hashtable seiDescriptorsTable = new Hashtable();
    for(int i = 0; i < wsConfigurationDescriptors.length; i++) {
      WSConfigurationDescriptor wsConfigurationDescriptor = wsConfigurationDescriptors[i];
      WSDescriptorsLocationTable singleSEIDescriptorsLocationTable = loadSingleSeiDesctiptorsTable(wsConfigurationDescriptor);
      seiDescriptorsTable.put(wsConfigurationDescriptor.getConfigurationName().trim(), singleSEIDescriptorsLocationTable);
    }

    return seiDescriptorsTable;
  }

  private WSDescriptorsLocationTable loadSingleSeiDesctiptorsTable(WSConfigurationDescriptor wsConfigurationDescriptor) {
    WSDescriptorsLocationTable wsDescriptorsLocationTable = new WSDescriptorsLocationTable();

    NameDescriptor viNameDescriptor = wsConfigurationDescriptor.getServiceEndpointViRef();
    wsDescriptorsLocationTable.setDescriptor(WSDescriptorsLocationTable.VI, IOUtil.convertPackageToDirPath(viNameDescriptor.getPackage().trim(), viNameDescriptor.getName().trim()));
    return wsDescriptorsLocationTable;
  }

  private WSDeploymentInfo[] parseWSDeploymentDescriptor(String applicationName, WSArchiveFilesLocationHandler wsArchiveFilesLocationHandler) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to parse " + WSDeploymentDescriptor.class.getName() + " descriptor, source " + wsArchiveFilesLocationHandler.getWSDeploymentDescriptorLocationMsg() + ". ";
    
    InputStream wsDeploymentDescriptorInputStream = null;
    WSDeploymentDescriptor wsDeploymentDescriptor = null;
    try {
      if(!wsArchiveFilesLocationHandler.hasWSDeploymentDesctiptor()) {        
        return new WSDeploymentInfo[0];
      }
      wsDeploymentDescriptorInputStream = wsArchiveFilesLocationHandler.getWSDeploymentDescriptorInputStream();
      wsDeploymentDescriptor = (WSDeploymentDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04WSDD).parse(wsDeploymentDescriptorInputStream);      
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    String version = wsDeploymentDescriptor.getVersion().trim();    
    WSDescriptor[] wsDescriptors = wsDeploymentDescriptor.getWebservice();   
    Vector wsDeploymentInfoes = new Vector();
    for(int i = 0; i < wsDescriptors.length; i++) {
        WSDeploymentInfo wsDeploymentInfo = new WSDeploymentInfo();
        wsArchiveFilesLocationHandler.setWsDeploymentInfo(wsDeploymentInfo);
        parseWSDescriptor(applicationName, version, wsDescriptors[i], wsArchiveFilesLocationHandler, wsDeploymentInfo);        
        setAdditionalWSDeploymentSettings(wsArchiveFilesLocationHandler.getModuleArchive().getName(), wsDeploymentInfo);
        wsDeploymentInfoes.add(wsDeploymentInfo);
    }

    WSDeploymentInfo[] wsDeploymentInfoesArr = new WSDeploymentInfo[wsDeploymentInfoes.size()];
    wsDeploymentInfoes.copyInto(wsDeploymentInfoesArr);

    return wsDeploymentInfoesArr;
  }

  private void setAdditionalWSDeploymentSettings(String moduleArchiveName, WSDeploymentInfo wsDeploymentInfo) {
    wsDeploymentInfo.getWsRuntimeDefinition().getWSIdentifier().setJarName(moduleArchiveName);
  }

  private WSDeploymentInfo parseWSDescriptor(String applicationName, String version, WSDescriptor wsDescriptor, WSFilesLocationHandler wsFilesLocationHandler, WSDeploymentInfo wsDeploymentInfo) throws WSDeploymentException {
    String serviceName = wsDescriptor.getWebserviceInternalName().trim();
    String excMsg = "Error occurred, parsing " + WSDescriptor.class.getName() + " descriptor, application " + applicationName + ", web service " + serviceName + ", location message: " + wsFilesLocationHandler.getLocationMsg() + ". ";

    try {
      WSRuntimeDefinition wsDefinition = new WSRuntimeDefinition();
      wsDeploymentInfo.setWsRuntimeDefinition(wsDefinition);
      wsDeploymentInfo.setWsDeploymentDescriptor(getWSDeploymentDescriptor(version, wsDescriptor));

      wsDefinition.setWSIdentifier(new WSIdentifier(applicationName, null, wsDescriptor.getWebserviceInternalName().trim()));

      if (wsDescriptor.getOutsideInDescriptor() != null) {
        wsDefinition.setOutsideInDefinition(parseOutsideInDescriptor(wsDescriptor.getOutsideInDescriptor(), wsFilesLocationHandler));
      }

      wsDefinition.setStandardNS(wsDescriptor.getStandardNamespaceURI().trim());
      wsDefinition.setWsQName(parseQNameDescriptor(wsDescriptor.getWebserviceName()));

      if(wsDescriptor.getUddiKey() != null) {
        wsDefinition.setUddiKey(wsDescriptor.getUddiKey());
      }

      WSConfigurationDescriptor[] wsConfigurations = wsDescriptor.getWsConfiguration();
      int endpointsSize = wsConfigurations.length;
      if (endpointsSize == 0) {
        Object[] args = new Object[]{"webservice definition error in the deployment descriptor: there are no ws configurations defined", applicationName, "not available", WS_DEPLOYMENT_DESCRIPTOR, "web service: " + wsDefinition.getWsQName()};
        throw new WSGenerationException(PatternKeys.BASE_WS_GENERATION_EXCEPTION, args);
      }
      ServiceEndpointDefinition[] serviceEndpoints = new ServiceEndpointDefinition[endpointsSize];
      wsDefinition.setServiceEndpointDefinitions(serviceEndpoints);
      for(int j = 0; j < endpointsSize; j++) {
        ServiceEndpointDefinition endpointDefinition = new ServiceEndpointDefinition();
        endpointDefinition.setOwner(wsDefinition);
        serviceEndpoints[j] = endpointDefinition;
        WSConfigurationDescriptor configurationDescriptor = wsConfigurations[j];

        endpointDefinition.setConfigurationName(configurationDescriptor.getConfigurationName().trim());
        OperationConfigurationDescriptor[] operationDescriptors = configurationDescriptor.getOperationConfiguration();

//        if (operationDescriptors.length == 0) {
//          Object[] args = new Object[]{" ws configuration error in the deployment descriptor: there are no operation configurations defined", applicationName, (workingJar != null) ? workingJar.getName() : "not available", WS_DEPLOYMENT_DESCRIPTOR, "web service: " + wsQName + configurationDescriptor.getConfigurationName().trim()};
//          throw new WSGenerationException(PatternKeys.BASE_WS_GENERATION_EXCEPTION, args);
//        }

        setImplLink(applicationName, configurationDescriptor, endpointDefinition);

        endpointDefinition.setServiceEndpointQualifiedName(parseQNameDescriptor(configurationDescriptor.getServiceEndpointName()));
        if(!endpointDefinition.getServiceEndpointQualifiedName().getNamespaceURI().equals(wsDefinition.getStandardNS()))  {
          Object[] args = new Object[]{" the namespaceURI of the service-endpoint does not match to the standard namespaceURI in the deployment descriptor", "not available", WS_DEPLOYMENT_DESCRIPTOR,
                                       "webservice: " + wsDefinition.getWsQName() + ", ws configuration: " + configurationDescriptor.getConfigurationName().trim()};

          throw new WSGenerationException(PatternKeys.BASE_WS_GENERATION_EXCEPTION, args);
        }

        endpointDefinition.setPortTypeName(parseQNameDescriptor(configurationDescriptor.getWsdlPorttypeName()));
        if (!endpointDefinition.getPortTypeName().getNamespaceURI().equals(wsDefinition.getStandardNS())) {
          Object[] args = new Object[]{" the namespaceURI of the porttype does not match to the standard namespaceURI in the deployment descriptor", applicationName, "not available", WS_DEPLOYMENT_DESCRIPTOR,
                                        "webservice: " + wsDefinition.getWsQName() + ", ws configuration: " + configurationDescriptor.getConfigurationName().trim()};

          throw new WSGenerationException(PatternKeys.BASE_WS_GENERATION_EXCEPTION, args);
        }

        if(wsDefinition.getWsdRelPath() == null) {
          parseWSDAndDocumentation(configurationDescriptor.getWebserviceDefinitionRef(), wsDefinition, wsFilesLocationHandler);
        }

        parseVI(applicationName, IOUtil.convertPackageToDirPath(configurationDescriptor.getServiceEndpointViRef().getPackage().trim(), configurationDescriptor.getServiceEndpointViRef().getName().trim()), wsFilesLocationHandler, endpointDefinition, endpointDefinition.getOwner().getWsdName());

        TrBindingDescriptor trBindingDescriptor = configurationDescriptor.getTransportBinding();
        endpointDefinition.setTransportBinding(trBindingDescriptor.getName().trim());
        com.sap.engine.lib.descriptors.ws04wsdd.QNameDescriptor
          bindingQNameDescriptor = trBindingDescriptor.getWsdlBindingName();
        QName bindingQName = new QName(bindingQNameDescriptor.getNamespaceURI().trim(),
                                       bindingQNameDescriptor.getLocalName().trim());
        if (!bindingQName.getNamespaceURI().equals(wsDefinition.getStandardNS())) {
          Object[] args = new Object[]{" the namespaceURI of the transport binding does not match to the standard namespaceURI in the deployment descriptor", applicationName, "not available", WS_DEPLOYMENT_DESCRIPTOR,
                                        "webservice: " + wsDefinition.getWsQName() + ", ws configuration: " + configurationDescriptor.getConfigurationName().trim()};

          throw new WSGenerationException(PatternKeys.BASE_WS_GENERATION_EXCEPTION, args);
        }

        endpointDefinition.setWsdlBindingName(bindingQName);
        endpointDefinition.setTrBindingConfig(new ConfigImpl(getExtensionProperties(trBindingDescriptor.getProperty())));
        if (configurationDescriptor.getTargetServerUrl() != null) endpointDefinition.setTargetServerURL(configurationDescriptor.getTargetServerUrl().trim());
        endpointDefinition.setServiceEndpointId(configurationDescriptor.getTransportAddress().trim());
        if (configurationDescriptor.getGlobalFeatures() != null) {
          FeatureDescriptor[] glFeatureDescriptor = configurationDescriptor.getGlobalFeatures().getFeature();
          endpointDefinition.setFeaturesChain(getFeaturesChain(glFeatureDescriptor));
        }

        HashMapObjectObject operationConfigsAsHash = getOperationConfigsAsHash(configurationDescriptor.getOperationConfiguration(), false);
        HashMapObjectObject uniqueOperationConfigsAsHash = getOperationConfigsAsHash(configurationDescriptor.getOperationConfiguration(), true);

        OperationDefinition[] operations = endpointDefinition.getOperations();
        int operationsSize = operations.length;
//        if(! (operationsSize == operationDescriptors.length)) {
//          Object[] args = new Object[] {"The number of operations in virtual interface xml file does not match to the number of operation configurations in the deployment descriptor",
//                                        applicationName, (workingJar != null) ? workingJar.getName() : "not available", WS_DEPLOYMENT_DESCRIPTOR,
//                                        "webservice: " + wsQName + ", ws configuration: " + configurationDescriptor.getConfigurationName().trim()};
//
//          throw new WSGenerationException(PatternKeys.BASE_WS_GENERATION_EXCEPTION, args);
//        }

        for (int k = 0; k < operationsSize; k++) {
          OperationDefinitionImpl operation = (OperationDefinitionImpl)operations[k];
          OperationConfigurationDescriptor operationDescriptor =
            (OperationConfigurationDescriptor)uniqueOperationConfigsAsHash.get(operation.getUniqueOperationName());
          if (operationDescriptor == null) {
            operationDescriptor = (OperationConfigurationDescriptor)operationConfigsAsHash.get(operation.getOperationName());
          }
//          if (operationDescriptor == null) {
//            Object[] args = new Object[] {"Operation with operation name: " + operation.getOperationName() + " and unique operation name: " + operation.getUniqueOperationName() + " is defined in the virtual interface descriptor, but not in the deployment descriptor " ,
//                                        applicationName, (workingJar != null) ? workingJar.getName() : "not available", WS_DEPLOYMENT_DESCRIPTOR,
//                                        "webservice: " + wsQName};
//
//            throw new WSGenerationException(PatternKeys.BASE_WS_GENERATION_EXCEPTION, args);
//          }
          if (operationDescriptor != null) {
            if (operationDescriptor.getDocumentation() != null) operation.setDescription(operationDescriptor.getDocumentation().trim());
            operation.setFeaturesChain(getFeaturesChain(operationDescriptor.getFeature()));
            TrBindingConfigDescriptor trBindingConfiguration =
            operationDescriptor.getTransportBindingConfiguration();
            if(trBindingConfiguration.getGeneralConfiguration() != null) {
              com.sap.engine.lib.descriptors.ws04wsdd.PropertyDescriptor[] generalConfiguration = trBindingConfiguration.getGeneralConfiguration().getProperty();
              Config generalConfig = operation.getGeneralConfiguration();
              Hashtable newProperties = getExtensionProperties(generalConfiguration);
              ((ConfigImpl)generalConfig).addProperties(newProperties);
            }

            com.sap.engine.lib.descriptors.ws04wsdd.PropertyDescriptor[] inputParameterDescriptor = trBindingConfiguration.getInput().getProperty();
            ((ConfigImpl) operation.getInputConfiguration()).addProperties(getExtensionProperties(inputParameterDescriptor));

            com.sap.engine.lib.descriptors.ws04wsdd.PropertyDescriptor[] outputParameterDescriptor = trBindingConfiguration.getOutput().getProperty();
            ((ConfigImpl) operation.getOutputConfiguration()).addProperties(getExtensionProperties(outputParameterDescriptor));

            Fault[] faults = operation.getFaults();
            HashMapObjectObject faultConfigsAsHash = getFaultConfigsAsHash(trBindingConfiguration.getFault());
            int faultsSize = faults.length;
            for (int l = 0; l < faultsSize; l++) {
              FaultImpl fault = (FaultImpl)faults[l];
              FaultConfigDescriptor faultConfigDescriptor =
                (FaultConfigDescriptor)faultConfigsAsHash.get(fault.getFaultName());
              if (faultConfigDescriptor != null)
                ((ConfigImpl) fault.getFaultConfiguration()).addProperties(getExtensionProperties(faultConfigDescriptor.getProperty()));
            }
          }
        }
        if (configurationDescriptor.getOutsideInConfiguration() != null) {
          endpointDefinition.setOutsideInConfiguration(configurationDescriptor.getOutsideInConfiguration());
        }
      }
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    return wsDeploymentInfo;
  }

  public void parseVI(String applicationName, String viRelPath, WSFilesLocationHandler wsFilesLocationHandler, ServiceEndpointDefinition endpointDefinition, String wsdName) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to parse source " +  wsFilesLocationHandler.getViLocationMsg(viRelPath);

    InputStream viInputStream = null;
    try {
      viInputStream = wsFilesLocationHandler.getViInputStream(viRelPath);
      endpointDefinition.setViRelPath(viRelPath);
      parseVInterface(applicationName, endpointDefinition, (VirtualInterfaceState)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04VI).parse(viInputStream), endpointDefinition.getOwner().getWsdName());
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);
      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    } finally {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);

      String msg = "Warning! " + excMsg + "Unable to close input stream for source. ";
      IOUtil.closeInputStreams(new InputStream[]{viInputStream}, new String[]{msg}, wsDeployLocation);
    }
  }

  public void parseVInterface(String applicationName, ServiceEndpointDefinition endpointDefinition, VirtualInterfaceState vInterface, String wsdName) throws WSGenerationException {
    try {
      endpointDefinition.setvInterfaceName(vInterface.getName().trim());

      VirtualInterfaceEndpointReference viReference = vInterface.getVirtualInterfaceEndpointReference();
      if (viReference.getEjbEndpointReference() != null) {
        endpointDefinition.setServiceEndpointInterface(viReference.getEjbEndpointReference().getRemoteInterfaceName());
      }

      FunctionState[] viFunctions = vInterface.getVirtualInterfaceFunctions().getFunction();
      int operationsSize = viFunctions.length;
      ArrayList operations = new ArrayList();
      for (int i = 0; i < operationsSize; i++) {
        FunctionState viFunction = viFunctions[i];
        if (viFunction.isIsExposed()) {
          OperationDefinitionImpl operation = new OperationDefinitionImpl();
          operations.add(operation);

          String isElFormDfQualified = "true";
          if (vInterface.getVirtualInterfaceSoapExtensionVI() != null) {
            if (! vInterface.getVirtualInterfaceSoapExtensionVI().getSoapExtensionVI().isUseNamespaces()) {
              isElFormDfQualified = "false";
            }
          }

          SoapExtensionFunctionState soapExtensionFunctionState = viFunction.getFunctionSoapExtensionFunction().getSoapExtensionFunction();
          ConfigImpl generalConfig = new ConfigImpl();
          generalConfig.setProperty(SOAP_REQUEST_NAME, soapExtensionFunctionState.getSOAPRequestName());
          generalConfig.setProperty(SOAP_RESPONSE_NAME, soapExtensionFunctionState.getSOAPResponseName());
          generalConfig.setProperty(IS_QUALIFIED, isElFormDfQualified);
          operation.setGeneralConfiguration(generalConfig);

          ConfigImpl inputConf = new ConfigImpl();
          inputConf.setProperty(NAMESPACE, soapExtensionFunctionState.getNamespace());
          operation.setInputOperationConfiguration(inputConf);

          ConfigImpl outputConf = new ConfigImpl();
          outputConf.setProperty(NAMESPACE, soapExtensionFunctionState.getNamespace());
          operation.setOutputOperationConfiguration(outputConf);

          operation.setUniqueOperationName(viFunction.getName().trim());
          operation.setOperationName(viFunction.getNameMappedTo().trim());
          operation.setJavaOperationName(viFunction.getOriginalName().trim());

          operation.setExposed(viFunction.isIsExposed());

          ParameterState[] inputParams;
          if (viFunction.getFunctionIncomingParameters()== null) {
            inputParams = new ParameterState[0];
          } else {
            inputParams = viFunction.getFunctionIncomingParameters().getParameter();
          }

          //this is in case the method in used from WSDLAbstractGeneratorImpl
          String serviceName = endpointDefinition.getOwner() != null ? endpointDefinition.getOwner().getServiceName() : null;
          operation.setInputParameters(getParameters(applicationName, inputParams, ParameterNode.IN, serviceName, endpointDefinition.getServiceEndpointQualifiedName(), operation.getOperationName()));

          ParameterState[] outputParams;
          if (viFunction.getFunctionOutgoingParameters() == null) {
            outputParams = new ParameterState[0];
          } else {
            outputParams = viFunction.getFunctionOutgoingParameters().getParameter();
          }
          operation.setOutputParameters(getParameters(applicationName, outputParams, ParameterNode.OUT, serviceName, endpointDefinition.getServiceEndpointQualifiedName(), operation.getOperationName()));

          FaultState[] faultStates;
          if (viFunction.getFunctionFaults() == null) {
            faultStates = new FaultState[0];
          } else {
            faultStates = viFunction.getFunctionFaults().getFault();
          }

          int faultsSize = faultStates.length;
          ArrayList faults = new ArrayList();
          for (int j = 0; j < faultsSize; j++) {
            FaultState faultState = faultStates[j];
            //skip remote exception declarations
            if (faultState.getName().trim().equals(java.rmi.RemoteException.class.getName())) {
              continue;
            }
            FaultImpl fault = new FaultImpl();
            fault.setFaultName(faultState.getName().trim());
            fault.setJavaClassName(faultState.getName().trim());// Andre  should correct this !!!
            ConfigImpl cfg = new ConfigImpl();
            cfg.setProperty(NAMESPACE, getDefaultFaultNS(wsdName, endpointDefinition.getvInterfaceName()));
            fault.setFaultConfiguration(cfg);
            faults.add(fault);
          }

          operation.setFaults((Fault[]) faults.toArray(new Fault[faults.size()]));

        }
      }

      int exposedOpsSize = operations.size();
      OperationDefinition[] operationsArray = new OperationDefinition[exposedOpsSize];
      for (int i = 0; i < exposedOpsSize; i++) operationsArray[i] = (OperationDefinition)operations.get(i);
      endpointDefinition.setOperations(operationsArray);
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{e.getClass().getName() + " exception occurred", applicationName, "not available", WS_DEPLOYMENT_DESCRIPTOR, "not available"};
      throw new WSGenerationException(PatternKeys.BASE_WS_GENERATION_EXCEPTION, args, e);
    }
  }

  private String getDefaultFaultNS(String wsdName, String viName) {
    return "urn:" + wsdName + "/" + viName;
  }

  private ParameterNode[] getParameters(String applicationName, ParameterState[] parameterStates, int parameterMode, String serviceName, QName endpointQName, String operationName) throws WSGenerationException {
    int paramsSize = parameterStates.length;
    ParameterNode[] parameters = new ParameterNode[paramsSize];
    ParameterMappedTypeReference mappedTypeReference;

    for (int i = 0; i < paramsSize; i++) {
      ParameterNodeImpl parameter = new ParameterNodeImpl();
      parameters[i] = parameter;
      ParameterState parameterState = parameterStates[i];
      parameter.setParameterMode(parameterMode);
      parameter.setParameterName(parameterState.getNameMappedTo().trim());
      parameter.setJavaParameterName(parameterState.getName().trim());
      mappedTypeReference = parameterState.getParameterMappedTypeReference();

      try {
        boolean isExposed = parameterState.isIsExposed();
        boolean isOptional = parameterState.isIsOptional();

        parameter.setExposed(isExposed);
        parameter.setOptional(isOptional);

        if (isOptional) {
          if (parameterState.getParameterDefaultValue()!= null) {
            parameter.setDefaultValue(parameterState.getParameterDefaultValue().getDefaultValue().getName().trim());
          }
          else {
           Object[] args = new Object[]{"Incorrect parameter settings. The parameter is optional, but does not have default value.", applicationName, "not available", WS_DEPLOYMENT_DESCRIPTOR, "webservice: " + serviceName + ", service endpoint: " + endpointQName + ", operation: " + operationName, "parameter: " + parameter.getParameterName()};
          throw new WSGenerationException(PatternKeys.BASE_WS_GENERATION_EXCEPTION, args);
          }
        }
        if (! (isExposed || isOptional)) {
          Object[] args = new Object[]{"Incorrect parameter settings. The parameter is not exposed. Must be optional" + mappedTypeReference, applicationName, "not available", WS_DEPLOYMENT_DESCRIPTOR, "webservice: " + serviceName + ", service endpoint: " + endpointQName + ", operation: " + operationName + ", parameter: " + parameter.getParameterName()};
          throw new WSGenerationException(PatternKeys.BASE_WS_GENERATION_EXCEPTION, args);
        }

        if (mappedTypeReference.getConvertedTypeReference() != null) { //simple types
          parameter.setJavaClassName(mappedTypeReference.getConvertedTypeReference().getName().trim());
          parameter.setOriginalClassName(mappedTypeReference.getConvertedTypeReference().getOriginalType().trim());
        } else if (mappedTypeReference.getConvertedTableReference() != null) {
          parameter.setJavaClassName(mappedTypeReference.getConvertedTableReference().getName().trim());
          parameter.setOriginalClassName(mappedTypeReference.getConvertedTableReference().getName().trim());
        } else if (mappedTypeReference.getComplexTypeReference() != null) {
          parameter.setJavaClassName(mappedTypeReference.getComplexTypeReference().getName().trim());
          parameter.setOriginalClassName(mappedTypeReference.getComplexTypeReference().getName().trim());
        } else {
          Object[] args = new Object[]{"Incorrect type reference found." + mappedTypeReference, applicationName, "not available", WS_DEPLOYMENT_DESCRIPTOR, "webservice: " + serviceName + ", service endpoint: " + endpointQName + ", operation: " + operationName};
          throw new WSGenerationException(PatternKeys.BASE_WS_GENERATION_EXCEPTION, args);
        }

        //adding header info
        if (parameterState.getParameterSoapExtensionParameter() != null
            && parameterState.getParameterSoapExtensionParameter().getSoapExtensionParameter().isIsHeader()
            && parameterState.getParameterSoapExtensionParameter().getSoapExtensionParameter().isIsHeader()) {
          parameter.setHeader(true);
          if (parameterState.getParameterSoapExtensionParameter().getSoapExtensionParameter().getNamespace() != null) {
            parameter.setHeaderElementNamespace(parameterState.getParameterSoapExtensionParameter().getSoapExtensionParameter().getNamespace());
          }
        }
      } catch (WSGenerationException e) {
        throw e;
      } catch(Exception e) {
        Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
        wsLocation.catching(e);

        Object[] args = new Object[]{"Error in extracting parameter info from the deployment descriptor", applicationName, "not available", WS_DEPLOYMENT_DESCRIPTOR, "not available"};
        throw new WSGenerationException(PatternKeys.BASE_WS_GENERATION_EXCEPTION, args, e);
      }
    }
    return parameters;
  }

  private void setImplLink(String applicationName, WSConfigurationDescriptor configurationDescriptor, ServiceEndpointDefinition endpointDefinition) throws WSGenerationException {

    ImplLink implLink = null;
    if(configurationDescriptor.getImplLink() != null) {
      ImplLinkDescriptor implLinkDescriptor = configurationDescriptor.getImplLink();
      String implId = implLinkDescriptor.getImplementationId().trim();
  
      implLink = new ImplLink();
      implLink.setImplId(implId);
      if (implLinkDescriptor.getProperties() != null) {
        com.sap.engine.lib.descriptors.ws04wsdd.PropertyDescriptor[] propertyDescriptors = implLinkDescriptor.getProperties().getProperty();
        Properties properties = getPropertiesAsHash(propertyDescriptors);
        implLink.setProperties(properties);
      }
    } else {
      if (configurationDescriptor.getEjbName() != null) {
        implLink = new ImplLink();
        implLink.setImplId(EJBImplConstants.EJB_ID);

        Properties properties = new Properties();
        properties.setProperty(EJBImplConstants.EJB_NAME, configurationDescriptor.getEjbName().trim());

        implLink.setProperties(properties);
      }
    }

    if (implLink == null) {
      Object[] args = new Object[]{"there is no implementation specified for the current configuration. You should specify 'impl-link' or 'ejb-name' tag", applicationName, "not available", WS_DEPLOYMENT_DESCRIPTOR,
                                   "webservice: " + endpointDefinition.getOwner().getServiceName() + ", ws configuration: " + configurationDescriptor.getConfigurationName().trim()};

      throw new WSGenerationException(PatternKeys.BASE_WS_GENERATION_EXCEPTION, args);
    }

    endpointDefinition.setImplLink(implLink);
  }


  private FeatureInfo[] getFeaturesChain(FeatureDescriptor[] featureDescriptors) throws Exception {
    int featuresSize = featureDescriptors.length;
    FeatureInfo[] featuresChain = new FeatureInfo[featuresSize];
    for (int i = 0; i < featuresSize; i++) {
      FeatureInfo feature = new FeatureInfo();
      featuresChain[i] = feature;
      FeatureDescriptor featureDescriptor = featureDescriptors[i];
      feature.setName(featureDescriptor.getName().trim());
      feature.setProtocol(featureDescriptor.getProtocol().trim());
      feature.setConfiguration(new ConfigImpl(getExtensionProperties(featureDescriptor.getProperty())));
    }
    return featuresChain;
  }

  private Hashtable getExtensionProperties(com.sap.engine.lib.descriptors.ws04wsdd.PropertyDescriptor[] propertyDescriptors) throws Exception {
    int propertiesSize = propertyDescriptors.length;
    Hashtable properties = new Hashtable();
    for (int l = 0 ; l < propertiesSize; l++) {
      com.sap.engine.lib.descriptors.ws04wsdd.PropertyDescriptor propertyDescriptor = propertyDescriptors[l];
      PropertyDescriptorInterfaceImpl propertyDescriptorInterface = new PropertyDescriptorInterfaceImpl();
      String propertyName = propertyDescriptor.getName().trim();
      properties.put(propertyName, propertyDescriptorInterface);
      propertyDescriptorInterface.setPropertyName(propertyName);
      propertyDescriptorInterface.setValue(propertyDescriptor.getValue());
      if (propertyDescriptor.getChoiceGroup1() != null) {
        if (propertyDescriptor.getChoiceGroup1().getSimpleContent() != null) {
          propertyDescriptorInterface.setSimpleContent(propertyDescriptor.getChoiceGroup1().getSimpleContent());
        }
        if (propertyDescriptor.getChoiceGroup1().getProperty() != null) {
          com.sap.engine.lib.descriptors.ws04wsdd.PropertyDescriptor[] inPropertyDescriptors = propertyDescriptor.getChoiceGroup1().getProperty();
          propertyDescriptorInterface.setInternals(getExtensionProperties(inPropertyDescriptors));
        } else {
          propertyDescriptorInterface.setInternals(new Hashtable());
        }
      }
    }

    return properties;
  }

  private HashMapObjectObject getOperationConfigsAsHash(OperationConfigurationDescriptor[] operations, boolean sortByUniqueName) {
    HashMapObjectObject operationsAsHash = new HashMapObjectObject();
    int operationsSize = operations.length;
    for (int i = 0; i < operationsSize; i ++) {
      OperationConfigurationDescriptor operation = operations[i];
      if (sortByUniqueName) {
        if (operation.getUniqueViName() != null) {
          operationsAsHash.put(operation.getUniqueViName().trim(), operation);
        }
      } else {
        if (operation.getName() != null) {
          operationsAsHash.put(operation.getName().trim(), operation);
        }
      }
    }
    return operationsAsHash;
  }

  private HashMapObjectObject getFaultConfigsAsHash(FaultConfigDescriptor[] faultConfigDescriptors) {
    HashMapObjectObject faultsAsHash = new HashMapObjectObject();
    int faultsSize = faultConfigDescriptors.length;
    for (int i = 0; i < faultsSize; i ++) {
      FaultConfigDescriptor fault = faultConfigDescriptors[i];
      faultsAsHash.put(fault.getName().trim(), fault);
    }
    return faultsAsHash;
  }

  public void parseRuntimeSpecific(String applicationName, WSRuntimeDefinition wsRuntimeDefinition, WSRuntimeDescriptor wsRuntimeDescriptor)
    throws WSGenerationException {

    try {
      wsRuntimeDefinition.setUddiKey(wsRuntimeDescriptor.getUddiKey());
      wsRuntimeDefinition.setUddiPublications(wsRuntimeDescriptor.getUddiPublications());
    } catch (Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{"Unable to get UDDI information from the ws-runtime-descriptor", applicationName, "not available", WS_RUNTIME_DESCRIPTOR, "not available"};
      throw new WSGenerationException(PatternKeys.BASE_WS_GENERATION_EXCEPTION, args, e);
    }

    ServiceEndpointDefinition endpointDefinition = null;
    try {
      wsRuntimeDefinition.getWSIdentifier().setJarName(wsRuntimeDescriptor.getJarName().trim());
      wsRuntimeDefinition.setWsdlSupportedStyles(wsRuntimeDescriptor.getWsdlStyles());

      HashMapObjectObject wsRuntimeConfigsAsHash =
        getRuntimeConfigsAsHash(wsRuntimeDescriptor.getWsRuntimeConfiguration());
      ServiceEndpointDefinition[] endpointDefinitions = wsRuntimeDefinition.getServiceEndpointDefinitions();
      int endpointsSize = endpointDefinitions.length;
      for (int i = 0; i < endpointsSize; i++) {
        endpointDefinition = endpointDefinitions[i];
        WSRuntimeConfigurationDescriptor wsRuntimeConfigDescriptor =
          (WSRuntimeConfigurationDescriptor)wsRuntimeConfigsAsHash.get(endpointDefinition.getServiceEndpointQualifiedName());

          com.sap.engine.lib.descriptors.ws04wsrt.ImplLinkDescriptor implLinkDescriptor =
            wsRuntimeConfigDescriptor.getImplLink();
        if (implLinkDescriptor != null) {
          setAdditionalImplLinkProperties(applicationName, endpointDefinition.getImplLink(), implLinkDescriptor);
        }

        HashMapObjectObject operationConfigsAsHash =
          getRuntimeOperationConfigsAsHash(wsRuntimeConfigDescriptor.getRuntimeOperationConfiguration());
        OperationDefinition[] operations = endpointDefinition.getOperations();
        int operationsSize = operations.length;
        for (int j = 0; j < operationsSize; j++) {
          OperationDefinitionImpl operation = (OperationDefinitionImpl)operations[j];
          RuntimeOperationConfigDescriptor runtimeOperationConfig =
            (RuntimeOperationConfigDescriptor)operationConfigsAsHash.get(operation.getOperationName());
          Key[] keys = getKeys(runtimeOperationConfig.getOperationMapping().getProperty());
          operation.setKeys(keys);
        }
      }
    } catch (WSGenerationException e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{"Setting runtime properties", applicationName, "not available", WS_RUNTIME_DESCRIPTOR, "webservice: " + wsRuntimeDefinition.getServiceName(), ", configuration: " + endpointDefinition.getConfigurationName()};
      throw new WSGenerationException(PatternKeys.BASE_WS_GENERATION_EXCEPTION, args, e);
    }
  }

  public void setAdditionalImplLinkProperties(String applicationName, ImplLink implLink, com.sap.engine.lib.descriptors.ws04wsrt.ImplLinkDescriptor implLinkDescriptor) throws WSGenerationException {
    if (implLinkDescriptor.getProperties() != null) {
      com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor[] propertyDescriptors = implLinkDescriptor.getProperties().getProperty();
      if (implLinkDescriptor.getImplementationId().equals(implLink.getImplId())) {
          Properties additionalProperties = getPropertiesAsHash(propertyDescriptors);

          Properties allProperties = new Properties(implLink.getProperties());
          allProperties.putAll(additionalProperties);
          implLink.setProperties(allProperties);
      } else {
        Object[] args = new Object[]{"Server error! The 'implementation-id' value does not match to the one, specified in the deployment descriptor", applicationName, "not available", WS_DEPLOYMENT_DESCRIPTOR, "not available"};
        throw new WSGenerationException(PatternKeys.BASE_WS_GENERATION_EXCEPTION, args);
      }
    }
  }

  public com.sap.engine.lib.descriptors.ws04wsrt.ImplLinkDescriptor getImplLinkDescriptor(ImplLink implLink) {
    com.sap.engine.lib.descriptors.ws04wsrt.ImplLinkDescriptor implLinkDescriptor =
      new com.sap.engine.lib.descriptors.ws04wsrt.ImplLinkDescriptor();

    String implId = implLink.getImplId();
    implLinkDescriptor.setImplementationId(implId);

    if (implId.equals(JavaImplConstants.JAVA_ID) || implId.equals(EJBImplConstants.EJB_ID)) {
      com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor[] propertyDescriptors =
        new com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor[1];
      com.sap.engine.lib.descriptors.ws04wsrt.PropertiesDescriptor pd = new com.sap.engine.lib.descriptors.ws04wsrt.PropertiesDescriptor();
      propertyDescriptors[0] = new com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor();
      propertyDescriptors[0].setName(EJBImplConstants.APPLICATION_NAME);
      propertyDescriptors[0].setValue(implLink.getProperties().getProperty(EJBImplConstants.APPLICATION_NAME));

      pd.setProperty(propertyDescriptors);
      implLinkDescriptor.setProperties(pd);
      getAdditionalImplLinkProperties(implLink,  implLinkDescriptor);
    }

    return implLinkDescriptor;
  }

     private void getAdditionalImplLinkProperties(ImplLink implLink, com.sap.engine.lib.descriptors.ws04wsrt.ImplLinkDescriptor implLinkDescriptor) {
       if (implLink.getImplId().equals(EJBImplConstants.EJB_ID)) {
        com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor[] additionalPropertyDescriptors = null;
        if (implLink.getProperties().getProperty(EJBImplConstants.EJB_JNDI_NAME_LOCAL) != null) {
          additionalPropertyDescriptors = new com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor[1];
        } else {
          additionalPropertyDescriptors = new com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor[0];
        }
   
//         additionalPropertyDescriptors[0] = new com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor();
//         additionalPropertyDescriptors[0].setName(EJBImplConstants.EJB_JNDI_NAME);
//         additionalPropertyDescriptors[0].setValue(implLink.getProperties().getProperty(EJBImplConstants.EJB_JNDI_NAME));
//         additionalPropertyDescriptors[1] = new com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor();
//         additionalPropertyDescriptors[1].setName(EJBImplConstants.EJB_SESSION_TYPE);
//         additionalPropertyDescriptors[1].setValue(implLink.getProperties().getProperty(EJBImplConstants.EJB_SESSION_TYPE));
        if (implLink.getProperties().getProperty(EJBImplConstants.EJB_JNDI_NAME_LOCAL) != null) {
          additionalPropertyDescriptors[0] = new com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor();
          additionalPropertyDescriptors[0].setName(EJBImplConstants.EJB_JNDI_NAME_LOCAL);
          additionalPropertyDescriptors[0].setValue(implLink.getProperties().getProperty(EJBImplConstants.EJB_JNDI_NAME_LOCAL));
        }
   
        
        com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor[] properties = null;
        if (implLinkDescriptor.getProperties() == null) {
          properties = new com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor[0];
        } else {
          properties = implLinkDescriptor.getProperties().getProperty();
        }
   
         com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor[][] descriptors =
           new com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor[][]{properties, additionalPropertyDescriptors};
         com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor[] allPropertyDescriptors =
           getAllProperties(descriptors);

         com.sap.engine.lib.descriptors.ws04wsrt.PropertiesDescriptor pd = new com.sap.engine.lib.descriptors.ws04wsrt.PropertiesDescriptor();
         pd.setProperty(allPropertyDescriptors);         
         implLinkDescriptor.setProperties(pd);
       }
     }
  private com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor[] getAllProperties(com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor[][] propertyDescriptors) {
    com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor[] allPropertyDescriptors =
            new com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor[0];
    int size = propertyDescriptors.length;
    for(int i = 0; i < size; i++) {
       com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor[] currentDescriptors =
         propertyDescriptors[i];
       com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor[] newDescriptors =
         new com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor[allPropertyDescriptors.length + currentDescriptors.length];
       System.arraycopy(allPropertyDescriptors, 0, newDescriptors, 0, allPropertyDescriptors.length);
       System.arraycopy(currentDescriptors, 0, newDescriptors, allPropertyDescriptors.length, currentDescriptors.length);
       allPropertyDescriptors = newDescriptors;
    }
    return allPropertyDescriptors;
  }

  public HashMapObjectObject getRuntimeConfigsAsHash(WSRuntimeConfigurationDescriptor[] wsRuntimeConfigDescriptors) {
    HashMapObjectObject runtimeConfigsAsHash = new HashMapObjectObject();
    int runtimeConfigsSize = wsRuntimeConfigDescriptors.length;
    for (int i = 0; i < runtimeConfigsSize; i++) {
      WSRuntimeConfigurationDescriptor wsRuntimeConfigDescriptor = wsRuntimeConfigDescriptors[i];
      com.sap.engine.lib.descriptors.ws04wsrt.QNameDescriptor endpointQNameDescriptor =
        wsRuntimeConfigDescriptor.getServiceEndpointName();
      QName endpointQName = new QName(endpointQNameDescriptor.getNamespaceURI().trim(),
                                      endpointQNameDescriptor.getLocalName().trim());

      runtimeConfigsAsHash.put(endpointQName,  wsRuntimeConfigDescriptor);
    }
    return runtimeConfigsAsHash;
  }

  public HashMapObjectObject getRuntimeOperationConfigsAsHash(RuntimeOperationConfigDescriptor[] runtimeOperationsDescriptors) {
    HashMapObjectObject runtimeOperationsAsHash = new HashMapObjectObject();
    int runtimeOperationsSize = runtimeOperationsDescriptors.length;
    for (int i = 0; i < runtimeOperationsSize; i++) {
      RuntimeOperationConfigDescriptor runtimeOperationConfigDescriptor = runtimeOperationsDescriptors[i];
      runtimeOperationsAsHash.put(runtimeOperationConfigDescriptor.getName().trim(),
                                  runtimeOperationConfigDescriptor);
    }
    return runtimeOperationsAsHash;
  }

  public Key[] getKeys(com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor[] propertyDescriptors) {
    int propertiesSize = propertyDescriptors.length;
    Key[] keys = new Key[propertiesSize];
    for (int i = 0; i < propertiesSize; i++) {
      com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor propertyDescriptor =
        propertyDescriptors[i];
      Key key = new Key(propertyDescriptor.getName().trim(),
                        propertyDescriptor.getValue().trim());
      keys[i] = key;
    }
    return keys;
  }

  public WSRuntimeDescriptor getWSRuntimeDescriptor(WSRuntimeDefinition wsRuntimeDefinition) {

    WSRuntimeDescriptor wsRuntimeDescriptor = new WSRuntimeDescriptor();
    if (wsRuntimeDefinition.getUddiKey() != null) {
      wsRuntimeDescriptor.setUddiKey(wsRuntimeDefinition.getUddiKey());
    }
    wsRuntimeDescriptor.setUddiPublications(wsRuntimeDefinition.getUddiPublications());

    wsRuntimeDescriptor.setJarName(wsRuntimeDefinition.getWSIdentifier().getJarName());

    wsRuntimeDescriptor.setWsdlStyles(wsRuntimeDefinition.getWsdlSupportedStyles());

    ServiceEndpointDefinition[] endpointDefinitions = wsRuntimeDefinition.getServiceEndpointDefinitions();
    int endpointsSize = endpointDefinitions.length;
    WSRuntimeConfigurationDescriptor[] wsRuntimeConfigs = new WSRuntimeConfigurationDescriptor[endpointsSize];
    for (int i = 0; i < endpointsSize; i++) {
      ServiceEndpointDefinition endpointDefinition = endpointDefinitions[i];
      WSRuntimeConfigurationDescriptor wsRuntimeConfigurationDescriptor = new WSRuntimeConfigurationDescriptor();
      wsRuntimeConfigs[i] = wsRuntimeConfigurationDescriptor;

      QName endpointQName = endpointDefinition.getServiceEndpointQualifiedName();
      com.sap.engine.lib.descriptors.ws04wsrt.QNameDescriptor qNameDescriptor =
        new com.sap.engine.lib.descriptors.ws04wsrt.QNameDescriptor();
      wsRuntimeConfigurationDescriptor.setServiceEndpointName(qNameDescriptor);
      qNameDescriptor.setNamespaceURI(endpointQName.getNamespaceURI());
      qNameDescriptor.setLocalName(endpointQName.getLocalPart());

      ImplLink implLink = endpointDefinition.getImplLink();
      com.sap.engine.lib.descriptors.ws04wsrt.ImplLinkDescriptor implLinkDescriptor =
         getImplLinkDescriptor(implLink);
      if (implLinkDescriptor != null) {
        wsRuntimeConfigurationDescriptor.setImplLink(implLinkDescriptor);
      }

      OperationDefinition[] operations = endpointDefinition.getOperations();
      int operationsSize = operations.length;
      RuntimeOperationConfigDescriptor[] operationConfigs = new RuntimeOperationConfigDescriptor[operationsSize];
      wsRuntimeConfigurationDescriptor.setRuntimeOperationConfiguration(operationConfigs);
      for (int j = 0; j < operationsSize; j++) {
        OperationDefinition operation = operations[j];
        RuntimeOperationConfigDescriptor runtimeOperationConfig = new RuntimeOperationConfigDescriptor();
        operationConfigs[j] = runtimeOperationConfig;
        runtimeOperationConfig.setName(operation.getOperationName());

        Key[] keys = ((OperationDefinitionImpl)operation).getKeys();
                        int keysSize = keys.length;
        com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor[] propertiesArr =
          new com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor[keysSize];
        com.sap.engine.lib.descriptors.ws04wsrt.PropertiesDescriptor pd = new com.sap.engine.lib.descriptors.ws04wsrt.PropertiesDescriptor();
        for (int l = 0; l < keysSize; l++) {
          Key key = keys[l];
          com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor property =
            new com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor();
          propertiesArr[l] = property;
          property.setName(key.getName());
          property.setValue(key.getValue());
        }
        pd.setProperty(propertiesArr);
        runtimeOperationConfig.setOperationMapping(pd);
      }
    }
    wsRuntimeDescriptor.setWsRuntimeConfiguration(wsRuntimeConfigs);
    return wsRuntimeDescriptor;
  }

  public Properties getPropertiesAsHash(com.sap.engine.lib.descriptors.ws04wsdd.PropertyDescriptor[] propertyDescriptors) {
    Properties propertiesAsHash = new Properties();
    int propertiesSize = propertyDescriptors.length;
    for(int i = 0; i < propertiesSize; i++) {
      com.sap.engine.lib.descriptors.ws04wsdd.PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
      propertiesAsHash.put(propertyDescriptor.getName().trim(), propertyDescriptor.getValue().trim());
    }
    return propertiesAsHash;
  }

  public Properties getPropertiesAsHash(com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor[] propertyDescriptors) {
    Properties propertiesAsHash = new Properties();
    int propertiesSize = propertyDescriptors.length;
    for(int i = 0; i < propertiesSize; i++) {
      com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
      propertiesAsHash.put(propertyDescriptor.getName().trim(), propertyDescriptor.getValue());
    }
    return propertiesAsHash;
  }

  private String[] constructRelPathTemplates(String baseRelativeDir, String baseFileNameTemplate, String[] fileExtensions) {
    String[] docFileNameTemplates = constructFileNamesTemplates(baseFileNameTemplate, fileExtensions);
    return WSUtil.addPrefixToStrings(baseRelativeDir + WebServicesConstants.SEPARATOR, docFileNameTemplates);
  }

  private String[] constructFileNamesTemplates(String baseFileNameTemplate, String[] fileExtensions) {
    String baseFileNameWithoutExt = IOUtil.getFileNameWithoutExt(baseFileNameTemplate);

    String[] docFileNameTemplates = new String[fileExtensions.length];
    for(int i = 0; i < docFileNameTemplates.length; i++) {
      docFileNameTemplates[i] = baseFileNameWithoutExt + fileExtensions[i];
    }

    return docFileNameTemplates;
  }

  private OutsideInDefinition parseOutsideInDescriptor(OutsideInDescriptor outsideInDescriptor, WSFilesLocationHandler wsFilesLocationHandler) throws WSDeploymentException {
    String excMsg = "Error occurred, parsing " + outsideInDescriptor.getClass().getName() + " descriptor. ";

    OutsideInDefinition outsideInDefinition = new OutsideInDefinition();
    String wsdlRelPath = IOUtil.convertPackageToDirPath(outsideInDescriptor.getWsdlRef().getPackage().trim(), outsideInDescriptor.getWsdlRef().getName().trim());
    String javaToQNameMappingRelPath = IOUtil.convertPackageToDirPath(outsideInDescriptor.getJavaQnameMappingRef().getPackage().trim(), outsideInDescriptor.getJavaQnameMappingRef().getName().trim());
    outsideInDefinition.setWsdlRelPath(wsdlRelPath);
    outsideInDefinition.setJavaQNameMappingFile(javaToQNameMappingRelPath);

    //TODO - move to new implementation!
    InputStream outsideInWsdlInputStream = null;
    InputStream outsideInJavaToQNameMappingStream = null;
    try {
      outsideInWsdlInputStream = wsFilesLocationHandler.getOutsideInWsdlInputStream(wsdlRelPath);
      outsideInJavaToQNameMappingStream = wsFilesLocationHandler.getOutsideInJavaToQNameMappingStream(javaToQNameMappingRelPath);
    } catch(IOException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    } finally {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);

      String wsdlMsg = "Warning! " + excMsg + "Unable to close input stream for source: " + wsFilesLocationHandler.getOutsideInWsdlLocationMsg(wsdlRelPath) + ". ";
      String javaToQNameMappingMsg = "Warning! " + excMsg + "Unable to close input stream for source: " + wsFilesLocationHandler.getOutsideInJavaToQNameMappingLocationMsg(javaToQNameMappingRelPath) + ". ";
      IOUtil.closeInputStreams(new InputStream[]{outsideInWsdlInputStream, outsideInJavaToQNameMappingStream}, new String[]{wsdlMsg, javaToQNameMappingMsg}, wsDeployLocation);
    }

    return outsideInDefinition;
  }

  private void parseWSDAndDocumentation(NameDescriptor wsdNameDescriptor, WSRuntimeDefinition wsRuntimeDefinition, WSFilesLocationHandler wsFilesLocationHandler) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to parse web service definition and documentation from location " + wsFilesLocationHandler.getLocationMsg() + ". ";

    String wsdPackage = wsdNameDescriptor.getPackage().trim();
    String wsdName = wsdNameDescriptor.getName().trim();
    String wsdRelPath = IOUtil.convertPackageToDirPath(wsdPackage, wsdName);
    wsRuntimeDefinition.setWsdRelPath(wsdRelPath);

    String[] docRelPathTemplates = constructRelPathTemplates(wsdPackage.replace('.', WebServicesConstants.SEPARATOR), wsdName, WS_DOCUMENTATION_EXTENSIONS);
    String docRelPath = null;
    try {
      docRelPath = wsFilesLocationHandler.findDocRelPath(docRelPathTemplates);
    } catch(IOException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }


    if(docRelPath != null) {
      wsRuntimeDefinition.setDocumentationRelPath(docRelPath);
    }
    parseDocumentation(wsdRelPath, docRelPath, wsRuntimeDefinition, wsFilesLocationHandler);

  }

  private void parseDocumentation(String wsdRelPath, String docRelPath, WSRuntimeDefinition wsRuntimeDefinition, WSFilesLocationHandler wsFilesLocationHandler) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to parse documentation for web service " + wsRuntimeDefinition.getWSIdentifier().getServiceName() + ". ";

    InputStream wsdInputStream = null;
    InputStream docInputStream = null;
    try {
      wsdInputStream = wsFilesLocationHandler.getWsdInputStream(wsdRelPath);
      if(docRelPath != null) {
        docInputStream = wsFilesLocationHandler.getDocInputStream(docRelPath);
      }

      WSDProcessor wsdProcessor = new WSDProcessor();
      wsdProcessor.process(wsdInputStream, docInputStream);
      wsRuntimeDefinition.setWsdUDDIPublications(wsdProcessor.getUDDIPublications());
      wsRuntimeDefinition.setWsdName(wsdProcessor.getName());
      wsRuntimeDefinition.setWsdDocumentation(wsdProcessor.getDocumentation());
      wsRuntimeDefinition.setDesigntimeFeatures(wsdProcessor.getFeatures());
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    } finally {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);

      String wsdMsg = "Warning! " + excMsg + " Unable to close input stream for source: " + wsFilesLocationHandler.getWsdLocationMsg(wsdRelPath) + ". ";
      String docMsg = "Warning! " + excMsg + " Unable to close input stream for source: " + wsFilesLocationHandler.getDocLocationMsg(docRelPath) + ". ";
      IOUtil.closeInputStreams(new InputStream[]{wsdInputStream, docInputStream}, new String[]{wsdMsg, docMsg}, wsDeployLocation);
    }
  }

  private QName parseQNameDescriptor(QNameDescriptor qNameDescriptor) {
    return new QName(qNameDescriptor.getNamespaceURI().trim(), qNameDescriptor.getLocalName().trim());
  }

  private WSDeploymentDescriptor getWSDeploymentDescriptor(String version, WSDescriptor wsDescriptor) {
    WSDeploymentDescriptor wsDeploymentDescriptor = new WSDeploymentDescriptor();
    wsDeploymentDescriptor.setVersion(version);
    wsDeploymentDescriptor.setWebservice(new WSDescriptor[]{wsDescriptor});
    return wsDeploymentDescriptor;
  }

  private String containsWSDeploymentDescriptor(File moduleArchive) throws IOException {
    String wsDeploymentDescriptorEntry = null;

    for(int i = 0; i < META_INF.length; i++) {
      wsDeploymentDescriptorEntry = META_INF[i] + SEPARATOR + WS_DEPLOYMENT_DESCRIPTOR;
      if(JarUtil.hasEntry(moduleArchive, wsDeploymentDescriptorEntry)) {        
        return wsDeploymentDescriptorEntry;
      }
    }

    return null;
  }


//  private String parseNameDescriptor(NameDescriptor nameDescriptor) {
//    String packageName = nameDescriptor.getPackage().trim();
//    String name = nameDescriptor.getName().trim();
//
//    return IOUtil.constructPackagePath(packageName, name);
//  }

}

