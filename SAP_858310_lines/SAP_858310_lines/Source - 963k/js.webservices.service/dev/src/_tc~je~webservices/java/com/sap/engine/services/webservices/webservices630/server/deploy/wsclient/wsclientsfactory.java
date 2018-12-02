package com.sap.engine.services.webservices.webservices630.server.deploy.wsclient;

import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.runtime.definition.wsclient.ComponentDescriptor;
import com.sap.engine.services.webservices.runtime.definition.wsclient.WSClientRuntimeInfo;
import com.sap.engine.interfaces.webservices.runtime.definition.WSClientIdentifier;
import com.sap.engine.lib.descriptors.ws04clientsdd.ComponentScopedRefsDescriptor;
import com.sap.engine.lib.descriptors.ws04clientsdd.ServiceRefDescriptor;
import com.sap.engine.lib.descriptors.ws04clientsdd.WSClientDeploymentDescriptor;
import com.sap.engine.lib.descriptors.ws04clientsrt.ComponentDescriptorType;
import com.sap.engine.lib.descriptors.ws04clientsrt.WSClientRuntimeDescriptor;
import com.sap.engine.lib.descriptors.ws04clientsrt.WSClientsRuntimeDescriptor;
import com.sap.engine.lib.processor.SchemaProcessorFactory;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.jar.JarUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WSDirsHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WSRuntimeActivator;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.update.WSClientArchiveLocationWrapper;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.update.WSClientFileLocationWrapper;
import com.sap.tc.logging.Location;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Vector;
import java.util.Properties;
import java.util.jar.JarFile;

/**
 * Title: WSClientsFactory
 * Description: This class is used to load ws client runtime structures from ws-clients-deployment-descriptor.xml and ws-clients-runtime-descriptor.xml.
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSClientsFactory implements WSClientsConstants {

  public WSClientsFactory() {
	  
  }

  public WSClientArchiveLocationWrapper[] loadWSClientArchiveLocationWrappers(String applicatioName, File[] wsClientModuleArchives) throws WSDeploymentException {
    if(wsClientModuleArchives == null) {
      return new WSClientArchiveLocationWrapper[0];
    }

    WSClientArchiveLocationWrapper[] wsClientArchiveLocationWrappers = new WSClientArchiveLocationWrapper[0];
    for(int i = 0; i < wsClientModuleArchives.length; i++) {
      WSClientArchiveLocationWrapper[] currentWSClientArchiveLocationWrappers = loadWSClientArchiveLocationWrappers(applicatioName, wsClientModuleArchives[i]);
      wsClientArchiveLocationWrappers = unifyWSClientArchiveLocationWrappers(new WSClientArchiveLocationWrapper[][]{wsClientArchiveLocationWrappers, currentWSClientArchiveLocationWrappers});
    }

    return wsClientArchiveLocationWrappers;
  }

  public WSClientArchiveLocationWrapper[] loadWSClientArchiveLocationWrappers(String applicationName, File wsClientModuleArchive) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to load ws client archive location wrappers for module " + wsClientModuleArchive.getAbsolutePath() + ". ";

    WSClientArchiveLocationWrapper[] wsClientArchiveLocationWrappers = new WSClientArchiveLocationWrapper[0];
    WSClientArchiveFilesLocationHandler wsClientArchiveFilesLocationHandler = null; 
    WSClientArchiveLocationHandlerMultipleMode wsClientArchiveLocationHandlerMultipleMode = null; 
    try {
      if(containsWSClientsSingleMode(wsClientModuleArchive) != null) {
        wsClientArchiveFilesLocationHandler = new WSClientArchiveFilesLocationHandler(wsClientModuleArchive);
        WSClientArchiveLocationWrapper[] currentWSClientArchiveLocationWrappers = loadWSClientArchiveLocationWrappers(applicationName, wsClientArchiveFilesLocationHandler);
        wsClientArchiveLocationWrappers = unifyWSClientArchiveLocationWrappers(new WSClientArchiveLocationWrapper[][]{wsClientArchiveLocationWrappers, currentWSClientArchiveLocationWrappers});
      }
      if(containsWSClientsMultipleMode(wsClientModuleArchive) != null) {
        wsClientArchiveLocationHandlerMultipleMode = new WSClientArchiveLocationHandlerMultipleMode(wsClientModuleArchive);  
        WSClientArchiveLocationWrapper[] currentWSClientArchiveLocationWrappers = loadWSClientArchiveLocationWrappers(applicationName, wsClientArchiveLocationHandlerMultipleMode);
        wsClientArchiveLocationWrappers = unifyWSClientArchiveLocationWrappers(new WSClientArchiveLocationWrapper[][]{wsClientArchiveLocationWrappers, currentWSClientArchiveLocationWrappers});
      }
    } catch(IOException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    } finally {
      try {
        if(wsClientArchiveFilesLocationHandler != null)  {
          wsClientArchiveFilesLocationHandler.close(); 
        }
      } catch(IOException e) {     
        // $JL-EXC$    
      }
      try {
        if(wsClientArchiveLocationHandlerMultipleMode != null)  {
          wsClientArchiveLocationHandlerMultipleMode.close(); 
        }
      } catch(IOException e) {      
        // $JL-EXC$      
      }
    }

    return wsClientArchiveLocationWrappers;
  }

  private WSClientArchiveLocationWrapper[] loadWSClientArchiveLocationWrappers(String applicationName, WSClientArchiveFilesLocationHandler wsClientArchiveFilesLocationHandler) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to collect ws clients descriptors (single mode). ";

    WSClientDeploymentDescriptor wsClientsDDescriptor = null;
    WSClientArchiveLocationWrapper[] wsClientArchiveLocationWrappers = new WSClientArchiveLocationWrapper[0];
    try {
      if(wsClientArchiveFilesLocationHandler.existsWSClientsDeploymentDescriptor()) {
        InputStream wsClientsDDescriptorInputStream = wsClientArchiveFilesLocationHandler.getWSClientsDeploymentDescriptorInputStream();
        wsClientsDDescriptor = (WSClientDeploymentDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04CLIENTDD).parse(wsClientsDDescriptorInputStream);
        wsClientArchiveLocationWrappers = loadWSClientArchiveLocationWrappers(applicationName, wsClientsDDescriptor, wsClientArchiveFilesLocationHandler);
      }
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Unable to load: " + wsClientArchiveFilesLocationHandler.getWSClientsDeploymentDescriptorLocationMsg() + ".";
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    return wsClientArchiveLocationWrappers;
  }

  private WSClientArchiveLocationWrapper[] loadWSClientArchiveLocationWrappers(String applicationName, WSClientArchiveLocationHandlerMultipleMode wsClientArchiveFilesLocationHandler) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to collect ws clients descriptors (multiple mode). ";

    String[] wsClientsDDescriptorPaths = null;
    try {
      wsClientsDDescriptorPaths = wsClientArchiveFilesLocationHandler.getWSClientsDeploymentDescriptorPathsMultipleMode();
    } catch(IOException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Unable to collect " + WSClientDirsHandler.getWSClientsDeploymentDescriptorFileName() +  " paths, source location "  + wsClientArchiveFilesLocationHandler.getWSClientsDescriptorsDir() + ". ";
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    WSClientArchiveLocationWrapper[] wsClientArchiveLocationWrappers = new WSClientArchiveLocationWrapper[0];
    for(int i = 0; i < wsClientsDDescriptorPaths.length; i++) {
      String wsClientsDDescriptorPath = wsClientsDDescriptorPaths[i];
      try {
        WSClientDeploymentDescriptor wsClientDeploymentDescriptor = (WSClientDeploymentDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04CLIENTDD).parse(wsClientArchiveFilesLocationHandler.getInputStream(wsClientsDDescriptorPath));
        WSClientArchiveLocationWrapper[] currentWSClientArchiveLocationWrappers = loadWSClientArchiveLocationWrappers(applicationName, wsClientDeploymentDescriptor, wsClientArchiveFilesLocationHandler);
        wsClientArchiveLocationWrappers = unifyWSClientArchiveLocationWrappers(new WSClientArchiveLocationWrapper[][]{wsClientArchiveLocationWrappers, currentWSClientArchiveLocationWrappers});
      } catch(Exception e) {
        Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);                       
        wsDeployLocation.catching(e);

        String msg = excMsg + "Unable to load  " + wsClientsDDescriptorPath + ". ";
        Object[] args = new String[]{msg, "none"};
        throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
      }
    }

    return  wsClientArchiveLocationWrappers;
  }

  private WSClientArchiveLocationWrapper[] loadWSClientArchiveLocationWrappers(String applicationName, WSClientDeploymentDescriptor wsClientDDescriptor, WSClientArchiveFilesLocationHandler  wsClientArchiveFilesLocationHandler) {
    WSClientDeploymentDescriptor[] wsClientDeploymentDescriptors = collectSingleWSClientDeploymentDescriptors(wsClientDDescriptor);
    return loadWSClientArchiveLocationWrappers(applicationName, wsClientDDescriptor.getVersion().trim(), wsClientDeploymentDescriptors, wsClientArchiveFilesLocationHandler);
  }

  private WSClientDeploymentDescriptor[] collectSingleWSClientDeploymentDescriptors(WSClientDeploymentDescriptor wsClientDeploymentDescriptor) {
    String version = wsClientDeploymentDescriptor.getVersion().trim();

    WSClientDeploymentDescriptor[] wsClientDeploymentDescriptors1 = collectSingleWSClientDeploymentDescriptors(version, wsClientDeploymentDescriptor.getServiceRef());
    WSClientDeploymentDescriptor[] wsClientDeploymentDescriptors2 = collectSingleWSClientDeploymentDescriptors(version, wsClientDeploymentDescriptor.getComponentScopedRefs());

    WSClientDeploymentDescriptor[] wsClientDeploymentDescriptors = new WSClientDeploymentDescriptor[wsClientDeploymentDescriptors1.length + wsClientDeploymentDescriptors2.length];
    System.arraycopy(wsClientDeploymentDescriptors1, 0, wsClientDeploymentDescriptors, 0, wsClientDeploymentDescriptors1.length);
    System.arraycopy(wsClientDeploymentDescriptors2, 0, wsClientDeploymentDescriptors, wsClientDeploymentDescriptors1.length, wsClientDeploymentDescriptors2.length);

    return wsClientDeploymentDescriptors;
  }

  private WSClientDeploymentDescriptor[] collectSingleWSClientDeploymentDescriptors(String version, ServiceRefDescriptor[] serviceRefDescriptors) {
    if(serviceRefDescriptors == null) {
      return new WSClientDeploymentDescriptor[0];
    }

    WSClientDeploymentDescriptor[] wsClientDeploymentDescriptors = new WSClientDeploymentDescriptor[serviceRefDescriptors.length];
    for(int i = 0; i < serviceRefDescriptors.length; i++) {
      WSClientDeploymentDescriptor currentWSClientDeploymentDescriptor = new WSClientDeploymentDescriptor();
      currentWSClientDeploymentDescriptor.setVersion(version);
      currentWSClientDeploymentDescriptor.setServiceRef(new ServiceRefDescriptor[]{serviceRefDescriptors[i]});
      wsClientDeploymentDescriptors[i] = currentWSClientDeploymentDescriptor;
    }

    return wsClientDeploymentDescriptors;
  }

  private WSClientDeploymentDescriptor[] collectSingleWSClientDeploymentDescriptors(String version, ComponentScopedRefsDescriptor[] componentScopedRefsDescriptors) {
    if(componentScopedRefsDescriptors == null) {
      return new WSClientDeploymentDescriptor[0];                                                              
    }

    WSClientDeploymentDescriptor[] wsClientDeploymentDescriptors = new WSClientDeploymentDescriptor[0];
    for(int i = 0; i < componentScopedRefsDescriptors.length; i++) {
      WSClientDeploymentDescriptor[] currentWSClientDeploymentDescriptors = collectSingleWSClientDeploymentDescriptors(version, componentScopedRefsDescriptors[i]);
      WSClientDeploymentDescriptor[] newWSClientDeploymentDescriptors = new WSClientDeploymentDescriptor[wsClientDeploymentDescriptors.length + currentWSClientDeploymentDescriptors.length];
      System.arraycopy(wsClientDeploymentDescriptors, 0, newWSClientDeploymentDescriptors, 0, wsClientDeploymentDescriptors.length);
      System.arraycopy(currentWSClientDeploymentDescriptors, 0, newWSClientDeploymentDescriptors, wsClientDeploymentDescriptors.length, currentWSClientDeploymentDescriptors.length);
      wsClientDeploymentDescriptors = newWSClientDeploymentDescriptors;
    }

    return wsClientDeploymentDescriptors;
  }

  private WSClientDeploymentDescriptor[] collectSingleWSClientDeploymentDescriptors(String version, ComponentScopedRefsDescriptor componentScopedRefsDescriptor) {
    String componentName = null;
    if(componentScopedRefsDescriptor.getComponentName() != null) {
      componentName = componentScopedRefsDescriptor.getComponentName().trim();
    }

    ServiceRefDescriptor[] serviceRefDescriptors = componentScopedRefsDescriptor.getServiceRef();
    WSClientDeploymentDescriptor[] wsClientDeploymentDescriptors = new WSClientDeploymentDescriptor[serviceRefDescriptors.length];
    for(int i = 0; i < serviceRefDescriptors.length; i++) {
      ComponentScopedRefsDescriptor currentComponentScopedRefsDescriptor = new ComponentScopedRefsDescriptor();
      if(componentName != null) {
        componentScopedRefsDescriptor.setComponentName(componentName);
      }
      currentComponentScopedRefsDescriptor.setServiceRef(new ServiceRefDescriptor[]{serviceRefDescriptors[i]});
      WSClientDeploymentDescriptor currentWSClientDeploymentDescriptor = new WSClientDeploymentDescriptor();
      currentWSClientDeploymentDescriptor.setVersion(version);
      currentWSClientDeploymentDescriptor.setComponentScopedRefs(new ComponentScopedRefsDescriptor[]{currentComponentScopedRefsDescriptor});
      wsClientDeploymentDescriptors[i] = currentWSClientDeploymentDescriptor;
    }

    return wsClientDeploymentDescriptors;
  }

  private WSClientArchiveLocationWrapper[] loadWSClientArchiveLocationWrappers(String applicationName, String version, WSClientDeploymentDescriptor[] wsClientDeploymentDescriptors, WSClientArchiveFilesLocationHandler  wsClientArchiveFilesLocationHandler) {
    WSClientArchiveLocationWrapper[] wsClientArchiveLocationWrappers = new WSClientArchiveLocationWrapper[wsClientDeploymentDescriptors.length];
    for(int i = 0; i < wsClientDeploymentDescriptors.length; i++) {
      wsClientArchiveLocationWrappers[i] = loadWSClientArchiveLocationWrapper(applicationName, version, wsClientDeploymentDescriptors[i], wsClientArchiveFilesLocationHandler);
    }

    return wsClientArchiveLocationWrappers;
  }

  private WSClientArchiveLocationWrapper loadWSClientArchiveLocationWrapper(String applicationName, String version, WSClientDeploymentDescriptor wsClientDeploymentDescriptor, WSClientArchiveFilesLocationHandler wsClientArchiveFilesLocationHandler) {
    String serviceRefName = getServiceRefNames(wsClientDeploymentDescriptor)[0];
    serviceRefName = getActualServiceRefName(version, wsClientArchiveFilesLocationHandler.getModuleArchive().getName(), serviceRefName);

    WSClientArchiveLocationWrapper wsClientArchiveLocationWrapper = new WSClientArchiveLocationWrapper();
    wsClientArchiveLocationWrapper.setApplicationName(applicationName);
    wsClientArchiveLocationWrapper.setWsClientName(serviceRefName);
    wsClientArchiveLocationWrapper.setWsClientArchiveFilesLocationHandler(wsClientArchiveFilesLocationHandler);
    wsClientArchiveLocationWrapper.setVersion(version);
    wsClientArchiveLocationWrapper.setWsClientDeploymentDescriptor(wsClientDeploymentDescriptor);

    return wsClientArchiveLocationWrapper;
  }

  public WSClientDeploymentInfo[] loadWSClientDeploymentInfoes(String applicationName, String wsClientsDir, String wsClientsWorkingDir, WSClientArchiveLocationWrapper[] wsClientArchiveLocationWrappers) {
    if(wsClientArchiveLocationWrappers == null) {
      return new WSClientDeploymentInfo[0];
    }

    WSClientDeploymentInfo[] wsClientDeploymentInfoes = new WSClientDeploymentInfo[0];
    for(int i = 0; i < wsClientArchiveLocationWrappers.length; i++) {
      WSClientDeploymentInfo[] currentWSWsClientDeploymentInfoes = loadWSClientDeploymentInfoes(applicationName, wsClientsDir, wsClientsWorkingDir, wsClientArchiveLocationWrappers[i]);
      wsClientDeploymentInfoes = WSClientsUtil.unifyWSClientDeploymentInfoes(new WSClientDeploymentInfo[][]{wsClientDeploymentInfoes, currentWSWsClientDeploymentInfoes});
    }

    return wsClientDeploymentInfoes;
  }

  public WSClientFileLocationWrapper loadWSClientFileLocationWrapper(String applicationName, String wsClientDir) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to collect web service location information, ws client directory " + wsClientDir + ".";

    WSClientFileLocationWrapper wsClientLocationWrapper = null;
    try {
      Properties mappings = null;
      boolean isBaseMappings = false;
      if(WSClientsFactory.hasMappings(wsClientDir)) {
        mappings = WSRuntimeActivator.loadMappingsFile(WSDirsHandler.getMappingsPath(wsClientDir));
      } else {
        mappings = WSClientDirsHandler.generateBaseDefaultMappings();
        isBaseMappings = true;
      }

      WSClientDirsHandler wsClientDirsHandler = new WSClientDirsHandler(wsClientDir, mappings);

      wsClientLocationWrapper = loadWSClientFileLocationWrapper(applicationName, wsClientDirsHandler);

      if(isBaseMappings) {
        wsClientLocationWrapper.getWsClientDirsHandler().updateDefaultMappings(wsClientLocationWrapper.getWsClientName());
      }
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    return wsClientLocationWrapper;
  }

  private WSClientFileLocationWrapper loadWSClientFileLocationWrapper(String applicationName, WSClientDirsHandler wsClientDirsHandler) throws WSDeploymentException {
    String excMsg = "Error occurred, loading ws client file location wrapper from " + wsClientDirsHandler.getWSClientsDeploymentDescriptorPath() + " and " + wsClientDirsHandler.getWSClientsRuntimeDescriptorPath() + ". ";

    String wsClientsDDescriptorPath = wsClientDirsHandler.getWSClientsDeploymentDescriptorPath();
    WSClientDeploymentDescriptor wsClientsDDescriptor = null;
    try {
      wsClientsDDescriptor = (WSClientDeploymentDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04CLIENTDD).parse(wsClientsDDescriptorPath);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    String wsClientRuntimeDescriptorPath = wsClientDirsHandler.getWSClientsRuntimeDescriptorPath();
    WSClientsRuntimeDescriptor wsClientsRuntimeDescriptor = null;
    try {
      wsClientsRuntimeDescriptor =(WSClientsRuntimeDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04CLIENTRT).parse(wsClientRuntimeDescriptorPath);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    String wsClientServiceRefName = getServiceRefNames(wsClientsDDescriptor)[0];
    String moduleName = getModuleNames(wsClientsRuntimeDescriptor.getWsclientRuntimeDescriptor())[0];
    wsClientServiceRefName = getActualServiceRefName(wsClientsDDescriptor.getVersion().trim(), moduleName, wsClientServiceRefName);

    WSClientFileLocationWrapper wsClientFileLocationWrapper = new WSClientFileLocationWrapper();
    wsClientFileLocationWrapper.setApplicationName(applicationName);
    wsClientFileLocationWrapper.setWsClientName(wsClientServiceRefName);
    wsClientFileLocationWrapper.setModuleName(moduleName);
    wsClientFileLocationWrapper.setWsClientDirsHandler(wsClientDirsHandler);

    return wsClientFileLocationWrapper;
  }

  public static boolean hasMappings(String wsClientOwnDir) {
    return (new File(WSClientDirsHandler.getMappingsPath(wsClientOwnDir))).exists();
  }

  public static Properties loadMappings(String wsClientOwnDir) throws WSDeploymentException {
    String excMsg = "Error occurred, loading mappings for ws client from directory "  + wsClientOwnDir + ". ";
    String mappingsPath = WSClientDirsHandler.getMappingsPath(wsClientOwnDir);
    Properties mappings = new Properties();

    if(new File(mappingsPath).exists()) {
      FileInputStream in = null;
      try {
        in = new FileInputStream(mappingsPath);
        mappings.load(in);
      } catch(IOException e) {
        Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
        wsDeployLocation.catching(e);

        String msg = excMsg + "Unable to load mappings from file " + mappingsPath + ". ";
        Object[] args = new String[]{msg};
        throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
      } finally {
        if(in != null) {
          try {
            in.close();
          } catch(IOException iExc) {
            Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
            String msg = "Warning! " + excMsg + "Unable to close stream for file " + mappingsPath + ".";
            wsDeployLocation.catching(msg, iExc);
          }
        }
      }
    }

    return mappings;
  }

  public static void setMappings(String wsClientsDir, String wsClientsWorkingDir, WSClientDeploymentInfo[] wsClientDeploymentInfoes, int maxIndex, int[] freeIndexes) {
    if(wsClientDeploymentInfoes == null) {
      return;
    }

    int freeIndexesLength = freeIndexes.length;
    for(int i = 0; i < wsClientDeploymentInfoes.length; i++) {
      if(i < freeIndexesLength) {
        setMappings(wsClientsDir, wsClientsWorkingDir, wsClientDeploymentInfoes[i], freeIndexes[i]);
      } else {
        setMappings(wsClientsDir, wsClientsWorkingDir, wsClientDeploymentInfoes[i], maxIndex + 1 + i - freeIndexesLength);
      }
    }
  }

  public static void setDefaultMappings(String wsClientsDir, String wsClientsWorkingDir, WSClientDeploymentInfo[] wsClientDeploymentInfoes) {
    if(wsClientDeploymentInfoes == null) {
      return;
    }
    for(int i = 0; i < wsClientDeploymentInfoes.length; i++) {
      setDefaultMappings(wsClientsDir, wsClientsWorkingDir, wsClientDeploymentInfoes[i]);
    }
  }

  public static void setDefaultMappings(String wsClientsDir, String wsClientsWorkingDir, WSClientDeploymentInfo wsClientDeploymentInfo) {
    WSClientIdentifier wsClientId = wsClientDeploymentInfo.getWsClientId();
    String serviceRefName = wsClientId.getServiceRefName();

    String wsClientDir  = WSClientDirsHandler.getDefaultWSClientDir(wsClientsDir, serviceRefName);
    String wsClientWorkingDir = WSClientDirsHandler.getDefaultWSClientWorkingDir(wsClientsWorkingDir, serviceRefName);

    Properties mappings = WSClientDirsHandler.generateDefaultMappings(serviceRefName);
    WSClientDirsHandler wsClientDirsHandler = new WSClientDirsHandler(wsClientDir, mappings);
    wsClientDeploymentInfo.setWsClientDirsHandler(wsClientDirsHandler);
    wsClientDeploymentInfo.setWsClientWorkingDir(wsClientWorkingDir);
  }

  public static void setMappings(String wsClientsDir, String wsClientsWorkingDir, WSClientDeploymentInfo wsClientDeploymentInfo, int index) {
    WSClientIdentifier wsClientId = wsClientDeploymentInfo.getWsClientId();
    String wsClientDir  = WSClientDirsHandler.getWSClientDir(wsClientsDir, index);
    String wsClientWorkingDir = WSClientDirsHandler.getWSClientWorkingDir(wsClientsWorkingDir, index);

    Properties mappings = WSClientDirsHandler.generateMappings(wsClientId.getServiceRefName(), index);
    WSClientDirsHandler wsClientDirsHandler = new WSClientDirsHandler(wsClientDir, mappings);
    wsClientDeploymentInfo.setWsClientDirsHandler(wsClientDirsHandler);
    wsClientDeploymentInfo.setWsClientWorkingDir(wsClientWorkingDir);
  }

  public static void setMappings(String wsClientsDir, WSClientRuntimeInfo wsClientRuntimeInfo) {
    WSClientIdentifier wsClientId = wsClientRuntimeInfo.getWsClientId();
    String wsClientDir = WSClientDirsHandler.getDefaultWSClientDir(wsClientsDir, wsClientId.getServiceRefName());
    Properties mappings = WSClientDirsHandler.generateDefaultMappings(wsClientId.getServiceRefName());

    WSClientDirsHandler wsClientDirsHandler = new WSClientDirsHandler(wsClientDir, mappings);
    wsClientRuntimeInfo.setWsClientDirsHandler(wsClientDirsHandler);
  }

  public static void setMappings(String wsClientOwnDir, WSClientRuntimeInfo wsClientRuntimeInfo, Properties mappings) {
    WSClientDirsHandler wsClientDirsHandler = new WSClientDirsHandler(wsClientOwnDir, mappings);
    wsClientRuntimeInfo.setWsClientDirsHandler(wsClientDirsHandler);
  }

  public WSClientDeploymentInfo[] loadWSClientDeploymentInfoes(String applicationName, String wsClientsDir, String wsClientsWorkingDir, WSClientArchiveLocationWrapper wsClientArchiveLocationWrapper) {
    WSClientArchiveFilesLocationHandler wsClientArchiveFilesLocationHandler = wsClientArchiveLocationWrapper.getWsClientArchiveFilesLocationHandler();
    return parseWSClientDeploymentDescriptorDeploymentMode(applicationName, wsClientArchiveFilesLocationHandler.getModuleArchive(), wsClientArchiveLocationWrapper.getWsClientDeploymentDescriptor(), wsClientArchiveFilesLocationHandler.getMetaInfDir());
  }

  public WSClientDeploymentInfo[] loadWSClientDeploymentInfos(String wsClientsWorkingDir, String wsClientsDeployDir, String applicationName, File moduleArchive) throws WSDeploymentException {
    WSClientDeploymentInfo[] wsClientDeploymentInfosSingleMode = loadWSClientDeploymentInfosSingleMode(wsClientsWorkingDir, wsClientsDeployDir, applicationName, moduleArchive);
    WSClientDeploymentInfo[] wsClientDeploymentInfosMultipleMode = loadWSClientDeploymentInfosMultipleMode(wsClientsWorkingDir, wsClientsDeployDir, applicationName, moduleArchive);

    WSClientDeploymentInfo[] wsClientDeploymentInfos = WSClientsUtil.unifyWSClientDeploymentInfoes(new WSClientDeploymentInfo[][]{wsClientDeploymentInfosSingleMode, wsClientDeploymentInfosMultipleMode});

    return wsClientDeploymentInfos;
  }

  public WSClientDeploymentInfo[] loadWSClientDeploymentInfosSingleMode(String wsClientsWorkingDir, String wsClientsDeployDir, String applicationName, File moduleArchive) throws WSDeploymentException {
    String excMsg = "Error occurred trying to read " + WS_CLIENTS_DEPLOYMENT_DESCRIPTOR + "(in single mode) for module: " + moduleArchive.getName() + ". ";
    String metaInfValue = null;

    try {
      metaInfValue = containsWSClientsSingleMode(moduleArchive);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      String msg = excMsg + "Unable to check the module archive if it contains ws clients. ";

      wsDeployLocation.catching(msg, e);

      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args, e);
    }

    JarFile moduleJarFile = null;
    WSClientDeploymentInfo[] wsClientDeploymentInfos = new WSClientDeploymentInfo[0];

    try {
      if (metaInfValue != null) {
        moduleJarFile = new JarFile(moduleArchive);
        String wsClientsDDescriptorEntry = metaInfValue + SEPARATOR + WS_CLIENTS_DEPLOYMENT_DESCRIPTOR;
        String moduleWorkingDir = WSClientDirsHandler.getModuleWorkingDir(wsClientsWorkingDir, moduleArchive.getName()) ;
        (new File(moduleWorkingDir)).mkdirs();
        (new JarUtil()).extractFile(moduleJarFile, wsClientsDDescriptorEntry, moduleWorkingDir);

        String wsClientsDDescriptorFileName = moduleWorkingDir + SEPARATOR + wsClientsDDescriptorEntry;
        WSClientDeploymentDescriptor wsClientDeploymentDescriptor = (WSClientDeploymentDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04CLIENTDD).parse(wsClientsDDescriptorFileName);         
        if(wsClientDeploymentDescriptor.isKeepRuntimeMode()) {
        //if(true) {
          wsClientDeploymentInfos = parseWSClientDeploymentDescriptorDeploymentMode(applicationName, moduleArchive, wsClientDeploymentDescriptor, metaInfValue);
        }
      }
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      String msg = excMsg + "Unable to extract, load or parse " + WS_CLIENTS_DEPLOYMENT_DESCRIPTOR + ". ";

      wsDeployLocation.catching(msg, e);

      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args, e);
      
    } finally {
      try {
        if(moduleJarFile != null) {
          moduleJarFile.close();
        }
      } catch(IOException e) {
        Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
        String msg = "Warning! " + excMsg + "Unable to close jarFile for file: " + moduleArchive + ". ";

        wsDeployLocation.catching(msg, e);
      }
    }

    return wsClientDeploymentInfos;
  }

  public WSClientDeploymentInfo[] loadWSClientDeploymentInfosMultipleMode(String wsClientsWorkingDir, String wsClientsDeployDir, String applicationName, File moduleArchive) throws WSDeploymentException {
    String excMsg = "Error occurred trying to read " + WS_CLIENTS_DEPLOYMENT_DESCRIPTOR + "(in multipleMode mode) for module: " + moduleArchive.getName() + ". ";
    String warningMsg = "Not deployed ws clients for module " + moduleArchive.getName() + ". " + excMsg;
    String metaInfValue = null;

    try {
      metaInfValue = containsWSClientsMultipleMode(moduleArchive);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      String msg = warningMsg + "Unable to check the module archive if it contains ws clients. ";

      wsDeployLocation.catching(msg, e);


      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args, e);
    }

    JarFile moduleJarFile = null;
    WSClientDeploymentInfo[] wsClientDeploymentInfos = new WSClientDeploymentInfo[0];
    String wsClientDDescriptorFileName = null;
    try {
      if (metaInfValue != null) {
        moduleJarFile = new JarFile(moduleArchive);
        String wsClientsDDescriptorEntry = metaInfValue + SEPARATOR + WS_CLIENTS_DEPLOYMENT_DESCRIPTOR_DIR;
        String moduleWorkingDir = WSClientDirsHandler.getModuleWorkingDir(wsClientsWorkingDir, moduleArchive.getName()) ;
        (new File(moduleWorkingDir)).mkdirs();
        (new JarUtil()).extractDir(moduleJarFile, wsClientsDDescriptorEntry, moduleWorkingDir);

        String wsClientsDDescriptorDirName = moduleWorkingDir + SEPARATOR + wsClientsDDescriptorEntry;
        File[] wsClientsDDescriptorFiles = new File(wsClientsDDescriptorDirName).listFiles(new WSClientsFileNameFilter());       
        if(wsClientsDDescriptorFiles != null && wsClientsDDescriptorFiles.length != 0) {
          WSClientDeploymentDescriptor[] wsClientsDeploymentDescriptors = new WSClientDeploymentDescriptor[wsClientsDDescriptorFiles.length];
          int i = 0;
          for(File wsClientsDDescriptorFile: wsClientsDDescriptorFiles) {
            wsClientsDeploymentDescriptors[i++] = (WSClientDeploymentDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04CLIENTDD).parse(wsClientsDDescriptorFile.getAbsolutePath());	  
          }          
          
          if(checkWSClientsDeploymentDescriptorsOR(wsClientsDeploymentDescriptors)) {          
            for(WSClientDeploymentDescriptor wsClientsDeploymentDescriptor: wsClientsDeploymentDescriptors) {
              //String wsClientsDDescriptorFileName = wsClientsDDescriptorFiles[i].getAbsolutePath();
              //WSClientDeploymentDescriptor wsClientDeploymentDescriptor = (WSClientDeploymentDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04CLIENTDD).parse(wsClientsDDescriptorFileName);
              WSClientDeploymentInfo[] currentWSClientDeploymentInfos = parseWSClientDeploymentDescriptorDeploymentMode(applicationName, moduleArchive, wsClientsDeploymentDescriptor, metaInfValue + SEPARATOR + WS_CLIENTS_DEPLOYMENT_DESCRIPTOR_DIR);
              wsClientDeploymentInfos = WSClientsUtil.unifyWSClientDeploymentInfoes(new WSClientDeploymentInfo[][]{wsClientDeploymentInfos, currentWSClientDeploymentInfos});
            }
          }      
        }  
      }   
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      String msg = warningMsg;
      if (wsClientDDescriptorFileName != null) {
        msg += "Unable to extract, load or parse " + WSUtil.cutString(wsClientDDescriptorFileName, WS_CLIENTS_DEPLOYMENT_DESCRIPTOR_DIR) + ". ";
      }

      wsDeployLocation.catching(msg, e);


      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args, e);
    } finally {
      try {
        if(moduleJarFile != null) {
          moduleJarFile.close();
        }
      } catch(IOException e) {
        Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
        String msg = excMsg + "Unable to close jarFile for file: " + moduleArchive + ". ";
         wsDeployLocation.catching(msg, e);
      }
    }

    return wsClientDeploymentInfos;
  }

  public WSClientRuntimeInfo loadWSClientRuntimeInfo(String wsClientOwnDir) throws WSDeploymentException {
    String excMsg = "Error occurred, loading ws client from directory "  + wsClientOwnDir + ". ";

    Properties mappings = null;
    boolean setDefaultMappings = false;
    if(hasMappings(wsClientOwnDir)) {
      mappings = loadMappings(wsClientOwnDir);
    } else {
      setDefaultMappings = true;
      mappings = WSClientDirsHandler.generateBaseDefaultMappings();
    }

    String wsClientDeploymentDescriptor = WSClientDirsHandler.getWSClientsDeploymentDescriptorPath(wsClientOwnDir, mappings);
    String wsClientRuntimeDescriptor = WSClientDirsHandler.getWSClientsRuntimeDescriptorPath(wsClientOwnDir, mappings);
    WSClientRuntimeInfo wsClientRuntimeInfo = null;
    try {
      wsClientRuntimeInfo = loadWSClientRuntimeInfos(wsClientDeploymentDescriptor, wsClientRuntimeDescriptor)[0];
    } catch(WSDeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Error occurred, trying to load ws client from " + WSClientsConstants.WS_CLIENTS_DEPLOYMENT_DESCRIPTOR  + " and " + WSClientsConstants.WS_CLIENTS_RUNTIME_DESCRIPTOR + ". ";
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    if(setDefaultMappings) {
      WSClientDirsHandler.updateDefaultMappings(wsClientRuntimeInfo.getWsClientId().getServiceRefName(), mappings);
    }

    setMappings(wsClientOwnDir, wsClientRuntimeInfo, mappings);

    return wsClientRuntimeInfo;
  }

 public WSClientRuntimeInfo[] loadWSClientRuntimeInfos(String wsClientDDescriptorFileName, String wsClientsRuntimeDescriptorFileName) throws WSDeploymentException {
    String excMsg = "Error occurred trying to load ws clients from " + WS_CLIENTS_DEPLOYMENT_DESCRIPTOR  + " and " + WS_CLIENTS_RUNTIME_DESCRIPTOR + ". ";

    WSClientDeploymentDescriptor wsClientDeploymentDescriptor = null;
    try {
      wsClientDeploymentDescriptor = (WSClientDeploymentDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04CLIENTDD).parse(wsClientDDescriptorFileName);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      String msg = excMsg + "Unable to load " + WS_CLIENTS_DEPLOYMENT_DESCRIPTOR + ". ";
      wsDeployLocation.catching(msg, e);

      Object[] args = new String[]{msg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    WSClientsRuntimeDescriptor wsClientsRuntimeDescriptor = null;
    try {
      wsClientsRuntimeDescriptor = (WSClientsRuntimeDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04CLIENTRT).parse(wsClientsRuntimeDescriptorFileName);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      String msg = excMsg + "Unable to load " + WS_CLIENTS_RUNTIME_DESCRIPTOR + ". ";
      wsDeployLocation.catching(msg, e);

      Object[] args = new String[]{msg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    WSClientRuntimeInfo[] wsClientRuntimeInfos = new WSClientRuntimeInfo[0];
    try {
       wsClientRuntimeInfos = parseWSClientDeploymentAndRuntimeDescriptors(wsClientDeploymentDescriptor, wsClientsRuntimeDescriptor);
    } catch(WSDeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(excMsg, e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    return wsClientRuntimeInfos;
 }

  public WSClientRuntimeInfo[] parseWSClientDeploymentAndRuntimeDescriptors(WSClientDeploymentDescriptor wsClientDeploymentDescriptor, WSClientsRuntimeDescriptor wsClientsRuntimeDescriptor) throws WSDeploymentException {
    String excMsg = "Error occurred trying to parse 'ws-clients-deployment-descriptor' or 'ws-clients-runtime-descriptor' element. ";
    
    WSClientRuntimeInfo[] wsClientRuntimeInfos = parseWSClientDeploymentDescriptor(wsClientDeploymentDescriptor);
    WSClientRuntimeDescriptor[] wsClientRuntimeDescriptors = wsClientsRuntimeDescriptor.getWsclientRuntimeDescriptor();

    if (wsClientRuntimeInfos == null) {
      return new WSClientRuntimeInfo[0];
    }

    if(wsClientRuntimeDescriptors == null || wsClientRuntimeDescriptors.length == 0) {
      return wsClientRuntimeInfos;
    }

    if (wsClientRuntimeInfos.length != wsClientRuntimeDescriptors.length) {
      String msg = excMsg + "WS clients count in the two descriptors is not the same. ";

      Object[] args = new String[]{msg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args);
    }

    parseWSClientRuntimeDescriptors(wsClientRuntimeInfos, wsClientRuntimeDescriptors);

    return wsClientRuntimeInfos;
  }

  public WSClientsRuntimeDescriptor loadWSClientsRuntimeDescriptor(WSClientRuntimeInfo wsClientRuntimeInfo) {
    WSClientIdentifier wsClientId = wsClientRuntimeInfo.getWsClientId();

    WSClientsRuntimeDescriptor wsClientsRuntimeDescriptor = new WSClientsRuntimeDescriptor();
    WSClientRuntimeDescriptor wsClientRuntimeDescriptor = new WSClientRuntimeDescriptor();
    wsClientsRuntimeDescriptor.setWsclientRuntimeDescriptor(new WSClientRuntimeDescriptor[]{wsClientRuntimeDescriptor});

    wsClientRuntimeDescriptor.setApplicationName(wsClientId.getApplicationName());
    wsClientRuntimeDescriptor.setModuleName(wsClientId.getJarName());

    ComponentDescriptor[] componentDescriptors = wsClientRuntimeInfo.getComponentDescriptors();
    ComponentDescriptorType[] componentDescriptorTypes = null;
    if (componentDescriptors == null) {
      componentDescriptorTypes = new ComponentDescriptorType[0];
    } else {

      componentDescriptorTypes = new ComponentDescriptorType[componentDescriptors.length];
      for (int i = 0; i < componentDescriptors.length; i++) {
        ComponentDescriptor componentDescriptor = componentDescriptors[i];
        ComponentDescriptorType componentDescriptorType = new ComponentDescriptorType();

        if (componentDescriptor.hasName()) {
          componentDescriptorType.setName(componentDescriptor.getName());
        }
        if (componentDescriptor.hasJndiName()) {
          componentDescriptorType.setJndiName(componentDescriptor.getJndiName());
        }

        componentDescriptorTypes[i] = componentDescriptorType;
      }
    }

    wsClientRuntimeDescriptor.setComponentDescriptor(componentDescriptorTypes);
    return wsClientsRuntimeDescriptor;
  }

  public String[] extractWSClientNames(String wsClientDir) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to extract ws client names, working directory " + wsClientDir + ". ";

    Properties mappings = new Properties();
    if(hasMappings(wsClientDir)) {
      mappings = loadMappings(wsClientDir);
    } else {
      mappings = WSClientDirsHandler.generateBaseDefaultMappings();
    }

    String wsClientDeploymentDescriptor = WSClientDirsHandler.getWSClientsDeploymentDescriptorPath(wsClientDir, mappings);
    String wsClientRuntimeDescriptor = WSClientDirsHandler.getWSClientsRuntimeDescriptorPath(wsClientDir, mappings);

    String[] wsClientNames = new String[0];
    try {
      wsClientNames = extractWSClientNames(wsClientDeploymentDescriptor, wsClientRuntimeDescriptor);
    } catch(WSDeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Error occurred, extracting ws client names from " + WSClientsConstants.WS_CLIENTS_DEPLOYMENT_DESCRIPTOR + " and " + WSClientsConstants.WS_CLIENTS_RUNTIME_DESCRIPTOR + ". ";
      Object[] args = new String[]{msg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    return wsClientNames;
  }

  public String[] extractWSClientNames(String wsClientsDeploymentDescriptorPath, String wsClientsRuntimeDescriptorPath) throws WSDeploymentException{
    String excMsg = "Error occurred trying to extract ws client names. ";

    WSClientDeploymentDescriptor wsClientDeploymentDescriptor = null;
    try {
      wsClientDeploymentDescriptor = (WSClientDeploymentDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04CLIENTDD).parse(wsClientsDeploymentDescriptorPath);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      String msg = excMsg + "Unable to load " + wsClientsDeploymentDescriptorPath + ". ";
      wsDeployLocation.catching(msg, e);

      Object[] args = new String[]{msg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    WSClientsRuntimeDescriptor wsClientsRuntimeDescriptor = null;
    try {
      wsClientsRuntimeDescriptor = (WSClientsRuntimeDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04CLIENTRT).parse(wsClientsRuntimeDescriptorPath);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      String msg = excMsg + "Unable to load " + WS_CLIENTS_RUNTIME_DESCRIPTOR + ". ";
      wsDeployLocation.catching(msg, e);

      Object[] args = new String[]{msg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }


    String version = wsClientDeploymentDescriptor.getVersion().trim();
    String[] serviceRefNames = getServiceRefNames(wsClientDeploymentDescriptor);
    if(version.equals(VERSION_630)) {
     String[] moduleNames = getModuleNames(wsClientsRuntimeDescriptor.getWsclientRuntimeDescriptor());
     serviceRefNames = getActualServiceRefNames(moduleNames, serviceRefNames);
    }

    return serviceRefNames;
  }

  public static boolean containsWSClients(File moduleArchive) throws IOException {
    if(containsWSClientsSingleMode(moduleArchive) != null) {
      return true;
    }
    if(containsWSClientsMultipleMode(moduleArchive) != null) {
      return true;
    }

    return false;
  }

  public static String containsWSClientsSingleMode(File moduleArchive) throws IOException {
    String metaInfValue = null;
    for(int i = 0; i < META_INF.length; i++) {
      metaInfValue = META_INF[i];
      String wsClientsDescriptorEntry = metaInfValue + SEPARATOR + WS_CLIENTS_DEPLOYMENT_DESCRIPTOR;
      if (JarUtil.hasEntry(moduleArchive, wsClientsDescriptorEntry)) {
        return metaInfValue;
      }
    }
    return null;
  }

  public static String containsWSClientsMultipleMode(File moduleArchive) throws IOException {
    String metaInfValue = null;
    for(int i = 0; i < META_INF.length; i++) {
      metaInfValue = META_INF[i];
      String wsClientsDescriptorEntry = metaInfValue + SEPARATOR + WS_CLIENTS_DEPLOYMENT_DESCRIPTOR_DIR;
      if (JarUtil.hasEntry(moduleArchive, wsClientsDescriptorEntry)) {
        return metaInfValue;
      } else if (JarUtil.hasEntry(moduleArchive, wsClientsDescriptorEntry + WSClientsConstants.SEPARATOR + "ws-clients-deployment-descriptor_1.xml")) {
        return metaInfValue;
      }
    }
    return null;
  }

  private WSClientDeploymentInfo[] parseWSClientDeploymentDescriptorDeploymentMode(String applicationName, File moduleArchive, WSClientDeploymentDescriptor wsClientDeploymentDescriptor, String baseRelativeDirEntry) {
    WSClientDeploymentInfo[] wsClientDeploymentInfos = parseWSClientDeploymentDescriptorDeploymentMode(wsClientDeploymentDescriptor);

    setAdditionalWSClientSettings1(applicationName, moduleArchive.getName(), wsClientDeploymentInfos);

    setBaseRelativeDirEntry(baseRelativeDirEntry, wsClientDeploymentInfos);
    setModuleArchive(moduleArchive, wsClientDeploymentInfos);

    return wsClientDeploymentInfos;
  }

  private void setAdditionalWSClientSettings1(String applicationName, String moduleName, WSClientDeploymentInfo[] wsClientDeploymentInfos){
    if(wsClientDeploymentInfos == null) {
      return;
    }

    for(int i = 0; i < wsClientDeploymentInfos.length; i++) {
      setAdditionalWSClientSettings1(applicationName, moduleName, wsClientDeploymentInfos[i]);
    }
  }

  private void setAdditionalWSClientSettings1(String applicationName, String moduleName, WSClientRuntimeInfo wsClientRuntimeInfo) {
    WSClientIdentifier wsClientId = wsClientRuntimeInfo.getWsClientId();

    wsClientId.setApplicationName(applicationName);
    wsClientId.setJarName(moduleName);
    resetServiceRefName(wsClientRuntimeInfo);
  }

  private WSClientDeploymentInfo[] parseWSClientDeploymentDescriptorDeploymentMode(WSClientDeploymentDescriptor wsClientDeploymentDescriptor) {
    ComponentScopedRefsDescriptor[] componentDescriptors = wsClientDeploymentDescriptor.getComponentScopedRefs();
    ServiceRefDescriptor[] serviceRefDescriptors = wsClientDeploymentDescriptor.getServiceRef();
    if (componentDescriptors == null) {
      return new WSClientDeploymentInfo[0];
    }

    WSClientDeploymentInfo[] componentWSClientDeploymentInfos = new WSClientDeploymentInfo[0];
    for (int i = 0; i < componentDescriptors.length; i++) {
      ComponentScopedRefsDescriptor componentDescriptor = componentDescriptors[i];
      WSClientDeploymentInfo[] currentDeploymentInfos = parseComponentScopedDescriptorDeploymentMode(componentDescriptor);
      componentWSClientDeploymentInfos = WSClientsUtil.unifyWSClientDeploymentInfoes(new WSClientDeploymentInfo[][]{componentWSClientDeploymentInfos, currentDeploymentInfos});
    }

    WSClientDeploymentInfo[] aloneWSClientDeploymentInfos = getArrayOfWSClientDeploymentInfo(serviceRefDescriptors.length);
    parseServiceDescriptorsDeploymentMode(serviceRefDescriptors, aloneWSClientDeploymentInfos, true);

    WSClientDeploymentInfo[] wsClientDeploymentInfos = WSClientsUtil.unifyWSClientDeploymentInfoes(new WSClientDeploymentInfo[][]{componentWSClientDeploymentInfos, aloneWSClientDeploymentInfos});

    String version = wsClientDeploymentDescriptor.getVersion().trim();
    setVersion(version, wsClientDeploymentInfos);

    return wsClientDeploymentInfos;
  }

  private void setVersion(String version, WSClientRuntimeInfo[] wsClientRuntimeInfos) {
    if(wsClientRuntimeInfos == null) {
      return;
    }

    for(int i = 0; i < wsClientRuntimeInfos.length; i++) {
      WSClientRuntimeInfo wsClientRuntimeInfo = wsClientRuntimeInfos[i];
      wsClientRuntimeInfo.setVersion(version);
    }
  }

  private WSClientDeploymentInfo[] parseComponentScopedDescriptorDeploymentMode(ComponentScopedRefsDescriptor componentScopedDescriptor) {
    ComponentDescriptor[] componentDescriptors = new ComponentDescriptor[0];
    String componentName = null;
    if (componentScopedDescriptor.getComponentName() != null) {
      componentName = componentScopedDescriptor.getComponentName();
      componentDescriptors = getComponentDescriptors(new String[]{componentName});
    }

    ServiceRefDescriptor[] serviceDescriptors = componentScopedDescriptor.getServiceRef();

    if (serviceDescriptors == null) {
      return new WSClientDeploymentInfo[0];
    }

    WSClientDeploymentInfo[] wsClientDeploymentInfos = getArrayOfWSClientDeploymentInfo(serviceDescriptors.length);
    parseServiceDescriptorsDeploymentMode(serviceDescriptors, wsClientDeploymentInfos, false);
    setComponentDescriptors(componentDescriptors, wsClientDeploymentInfos);

    setComponentName(componentName, wsClientDeploymentInfos);

    return wsClientDeploymentInfos;
  }

  private void parseServiceDescriptorsDeploymentMode(ServiceRefDescriptor[] serviceDescriptors, WSClientDeploymentInfo[] wsClientDeploymentInfos, boolean isServiceAloneMode) {
    for(int i = 0; i < serviceDescriptors.length; i++) {
      parseServiceDescriptorDeploymentMode(serviceDescriptors[i], wsClientDeploymentInfos[i], isServiceAloneMode);
    }
  }

  private void parseServiceDescriptorDeploymentMode(ServiceRefDescriptor serviceDescriptor, WSClientDeploymentInfo wsClientDeploymentInfo, boolean isServiceAloneMode) {
    parseServiceDescriptor(serviceDescriptor, wsClientDeploymentInfo);

    WSClientDeploymentDescriptor wsClientDeploymentDescriptor = new WSClientDeploymentDescriptor();
    if (isServiceAloneMode) {
      wsClientDeploymentDescriptor.setServiceRef(new ServiceRefDescriptor[]{serviceDescriptor});
    } else {
      ComponentScopedRefsDescriptor componentScopedRefsDescriptor = new ComponentScopedRefsDescriptor();
      componentScopedRefsDescriptor.setServiceRef(new ServiceRefDescriptor[]{serviceDescriptor});
      wsClientDeploymentDescriptor.setComponentScopedRefs(new ComponentScopedRefsDescriptor[]{componentScopedRefsDescriptor});
    }

    wsClientDeploymentInfo.setWsClientSingleDescriptor(wsClientDeploymentDescriptor);
  }

  private void parseWSClientRuntimeDescriptors(WSClientRuntimeInfo[] wsClientRuntimeInfos, WSClientRuntimeDescriptor[] wsClientsRuntimeDescriptors) throws IllegalArgumentException {
    if(wsClientRuntimeInfos == null) {
      return;
    }

    if(wsClientRuntimeInfos.length != wsClientRuntimeInfos.length) {
      throw new IllegalArgumentException("The input parameter arrays are not equal by length.");
    }

    for(int i = 0; i < wsClientRuntimeInfos.length; i++) {
      parseWSClientRuntimeDescriptor(wsClientRuntimeInfos[i], wsClientsRuntimeDescriptors[i]);
    }
  }

  private void parseWSClientRuntimeDescriptor(WSClientRuntimeInfo wsClientRuntimeInfo, WSClientRuntimeDescriptor wsClientRuntimeDescriptor) {
    String applicationName = wsClientRuntimeDescriptor.getApplicationName().trim();
    String moduleName = wsClientRuntimeDescriptor.getModuleName().trim();

    setAdditionalWSClientSettings1(applicationName, moduleName, wsClientRuntimeInfo);

    ComponentDescriptor[] componentDescriptors = getComponentDescriptors(wsClientRuntimeDescriptor.getComponentDescriptor());
    wsClientRuntimeInfo.setComponentDescriptors(componentDescriptors);
  }

  private WSClientRuntimeInfo[] parseWSClientDeploymentDescriptor(WSClientDeploymentDescriptor wsClientDeploymentDescriptor) {
    ComponentScopedRefsDescriptor[] componentDescriptors = wsClientDeploymentDescriptor.getComponentScopedRefs();
    ServiceRefDescriptor[] serviceRefDescriptors = wsClientDeploymentDescriptor.getServiceRef();
    if (componentDescriptors == null || serviceRefDescriptors == null) {
      return new WSClientRuntimeInfo[0];
    }

    WSClientRuntimeInfo[] componentWSClientRuntimeInfos = new WSClientRuntimeInfo[0];
    for (int i = 0; i < componentDescriptors.length; i++) {
      ComponentScopedRefsDescriptor componentDescriptor = componentDescriptors[i];
      WSClientRuntimeInfo[] currentRuntimeInfos = parseComponentScopedDescriptor(componentDescriptor);
      componentWSClientRuntimeInfos = WSClientsUtil.unifyWSClientRuntimeInfoes(new WSClientRuntimeInfo[][]{componentWSClientRuntimeInfos, currentRuntimeInfos});
    }

    WSClientRuntimeInfo[] aloneWSClientRuntimeInfos = getArrayOfWSClientRuntimeInfo(serviceRefDescriptors.length);
    parseServiceDescriptors(serviceRefDescriptors, aloneWSClientRuntimeInfos);

    WSClientRuntimeInfo[] wsClientRuntimeInfos = WSClientsUtil.unifyWSClientRuntimeInfoes(new WSClientRuntimeInfo[][]{componentWSClientRuntimeInfos, aloneWSClientRuntimeInfos});
    String version = wsClientDeploymentDescriptor.getVersion().trim();
    setVersion(version, wsClientRuntimeInfos);

    return wsClientRuntimeInfos;
  }

  private WSClientRuntimeInfo[] parseComponentScopedDescriptor(ComponentScopedRefsDescriptor componentScopedDescriptor) {
    ComponentDescriptor[] componentDescriptors = new ComponentDescriptor[0];
    String componentName = null;
    if (componentScopedDescriptor.getComponentName() != null) {
      componentName = componentScopedDescriptor.getComponentName();
      componentDescriptors = getComponentDescriptors(new String[]{componentName});
    }

    ServiceRefDescriptor[] serviceDescriptors = componentScopedDescriptor.getServiceRef();
    if (serviceDescriptors == null) {
      return new WSClientRuntimeInfo[0];
    }

    WSClientRuntimeInfo[] wsClientRuntimeInfos = getArrayOfWSClientRuntimeInfo(serviceDescriptors.length);
    parseServiceDescriptors(serviceDescriptors, wsClientRuntimeInfos);
    setComponentDescriptors(componentDescriptors, wsClientRuntimeInfos);

    return wsClientRuntimeInfos;
  }

  private void setComponentDescriptors(ComponentDescriptor[] componentDescriptors, WSClientRuntimeInfo[] wsClientRuntimeInfos) {
    if (wsClientRuntimeInfos == null) {
      return;
    }

    if (componentDescriptors == null || componentDescriptors.length == 0) {
      return;
    }

    for (int i = 0; i < wsClientRuntimeInfos.length; i++) {
      WSClientRuntimeInfo wsClientRuntimeInfo = wsClientRuntimeInfos[i];
      wsClientRuntimeInfo.setComponentDescriptors(componentDescriptors);
    }
  }

  private void setComponentName(String componentName, WSClientDeploymentInfo[] wsClientDeploymentInfos) {
    if (wsClientDeploymentInfos == null)  {
      return;
    }

    if (componentName == null) {
      return;
    }

    for(int i = 0; i < wsClientDeploymentInfos.length; i++) {
      WSClientDeploymentInfo wsClientDeploymentInfo = wsClientDeploymentInfos[i];
      WSClientDeploymentDescriptor wsClientDeploymentDescriptor = wsClientDeploymentInfo.getWsClientSingleDescriptor();
      ComponentScopedRefsDescriptor[] componentScopedRefsDescriptors = wsClientDeploymentDescriptor.getComponentScopedRefs();
      for (int j = 0; j < componentScopedRefsDescriptors.length; j++) {
        componentScopedRefsDescriptors[j].setComponentName(componentName);
      }
    }
  }

  private void parseServiceDescriptors(ServiceRefDescriptor[] serviceDescriptors, WSClientRuntimeInfo[] wsClientRuntimeInfos) {
    for(int i = 0; i < serviceDescriptors.length; i++) {
      parseServiceDescriptor(serviceDescriptors[i], wsClientRuntimeInfos[i]);
    }
  }

  private void parseServiceDescriptor(ServiceRefDescriptor serviceDescriptor, WSClientRuntimeInfo wsClientRuntimeInfo) {
    WSClientIdentifier wsClientId = new WSClientIdentifier();
    wsClientId.setServiceRefName(serviceDescriptor.getServiceRefName().trim());
    wsClientRuntimeInfo.setWsClientId(wsClientId);

    wsClientRuntimeInfo.setServiceInterfaceName(serviceDescriptor.getServiceInterface().trim());
    wsClientRuntimeInfo.setPackageName(serviceDescriptor.getPackageName().trim());
    wsClientRuntimeInfo.setWsdlFileNames(WSUtil.getSeparateStrings(serviceDescriptor.getWsdlFile().trim(), ";"));
    if (serviceDescriptor.getLogicalPortsFile() != null) {
      wsClientRuntimeInfo.setLogicalPortsFileName(serviceDescriptor.getLogicalPortsFile());
    }

    if(serviceDescriptor.getUriMappingFile() != null) {
      wsClientRuntimeInfo.setUriMappingFiles(WSUtil.getSeparateStrings(serviceDescriptor.getUriMappingFile().trim(), ";"));
    }

    if (serviceDescriptor.getPackageMappingFile() != null) {
      wsClientRuntimeInfo.setPackageMappingFile(serviceDescriptor.getPackageMappingFile().trim());
    }
  }

  private void setModuleArchive(File moduleArchive, WSClientDeploymentInfo[] wsClientDeploymentInfos) {
    if (wsClientDeploymentInfos == null) {
      return;
    }

    for (int i = 0; i < wsClientDeploymentInfos.length; i++) {
      WSClientDeploymentInfo wsClientDeploymentInfo = wsClientDeploymentInfos[i];
      wsClientDeploymentInfo.setModuleArchive(moduleArchive);
    }
  }

  private void setBaseRelativeDirEntry(String baseRelativeDirEntry, WSClientDeploymentInfo[] wsClientDeploymentInfos) {
    if (wsClientDeploymentInfos == null) {
      return;
    }

    for (int i = 0; i < wsClientDeploymentInfos.length; i++) {
      WSClientDeploymentInfo wsClientDeploymentInfo = wsClientDeploymentInfos[i];
      wsClientDeploymentInfo.setBaseRelativeDirEntry(baseRelativeDirEntry);
    }
  }

  public static ComponentDescriptor[] getComponentDescriptors(String[] names) {
    ComponentDescriptor[] componentDescriptors = new ComponentDescriptor[0];
    if (names == null) {
      return componentDescriptors;
    }

    componentDescriptors = new ComponentDescriptor[names.length];
    for (int i = 0; i < componentDescriptors.length; i++) {
      ComponentDescriptor componentDescriptor = new ComponentDescriptor();
      String name = names[i];
      componentDescriptor.setName(name);

      componentDescriptors[i] = componentDescriptor;
    }

    return componentDescriptors;
  }

  private ComponentDescriptor[] getComponentDescriptors(ComponentDescriptorType[] componentDescriptorTypes) {
    if(componentDescriptorTypes == null) {
      return new ComponentDescriptor[0];
    }

    ComponentDescriptor[] componentDescriptors = new ComponentDescriptor[componentDescriptorTypes.length];
    for(int i = 0; i < componentDescriptorTypes.length; i++) {
      ComponentDescriptor componentDescriptor = new ComponentDescriptor();
      ComponentDescriptorType componentDescriptorType = componentDescriptorTypes[i];
      if(componentDescriptorType.getName() != null) {
        componentDescriptor.setName(componentDescriptorType.getName().trim());
      }
      if(componentDescriptorType.getJndiName() != null) {
        componentDescriptor.setJndiName(componentDescriptorType.getJndiName().trim());
      }

      componentDescriptors[i] = componentDescriptor;
    }

    return componentDescriptors;
  }

  private WSClientRuntimeInfo[] getArrayOfWSClientRuntimeInfo(int lenght) {
    if (lenght < 0) {
      return new WSClientRuntimeInfo[0];
    }

    WSClientRuntimeInfo[] wsClientRuntimeInfos = new WSClientRuntimeInfo[lenght];
    for (int i = 0; i < lenght; i++) {
      wsClientRuntimeInfos[i] = new WSClientRuntimeInfo();
    }

    return wsClientRuntimeInfos;
  }

  private WSClientDeploymentInfo[] getArrayOfWSClientDeploymentInfo(int lenght) {
    if (lenght < 0) {
      return new WSClientDeploymentInfo[0];
    }

    WSClientDeploymentInfo[] wsClientDeploymentInfos = new WSClientDeploymentInfo[lenght];
    for (int i = 0; i < lenght; i++) {
      wsClientDeploymentInfos[i] = new WSClientDeploymentInfo();
    }

    return wsClientDeploymentInfos;
  }

  private void resetServiceRefName(WSClientRuntimeInfo wsClientRuntimeInfo) {
    String version = wsClientRuntimeInfo.getVersion();
    WSClientIdentifier wsClientId = wsClientRuntimeInfo.getWsClientId();

    if(version.equals(VERSION_630)) {
      String actualServiceRefName = getActualServiceRefName(wsClientId.getJarName(), wsClientId.getServiceRefName());
      String linkServiceRefName = wsClientId.getServiceRefName();
      wsClientId.setServiceRefName(actualServiceRefName);
      wsClientRuntimeInfo.setLinkServiceRefName(linkServiceRefName);
    }
  }

  private String getActualServiceRefName(String version, String moduleName, String serviceRefName) {
    String actualServiceRefName = serviceRefName;
    if(version.equals(VERSION_630)) {
      actualServiceRefName =  getActualServiceRefName(moduleName, serviceRefName);
    }
    return actualServiceRefName;
  }

  private String getActualServiceRefName(String moduleName, String serviceRefName) {
    return WSUtil.getModuleNameByType(moduleName) + "_" + serviceRefName;
  }

  private String[] getActualServiceRefNames(String[] moduleNames, String[] serviceRefNames) {
    if (serviceRefNames == null) {
      return new String[0];
    }

    String[] actualServiceRefNames = new String[serviceRefNames.length];
    for (int i = 0; i < serviceRefNames.length; i++) {
      actualServiceRefNames[i] = getActualServiceRefName(moduleNames[i], serviceRefNames[i]);
    }

    return actualServiceRefNames;
  }

  private String[] getModuleNames(WSClientRuntimeDescriptor[] wsClientRuntimeDescriptors) {
    if(wsClientRuntimeDescriptors == null) {
      return new String[0];
    }

    String[] moduleNames = new String[wsClientRuntimeDescriptors.length];
    for(int i = 0; i < wsClientRuntimeDescriptors.length; i++) {
      moduleNames[i] = wsClientRuntimeDescriptors[i].getModuleName().trim();
    }

    return moduleNames;
  }

  private String[] getServiceRefNames(WSClientDeploymentDescriptor wsClientDeploymentDescriptor) {
    ComponentScopedRefsDescriptor[] componentScopedRefsDescriptors = wsClientDeploymentDescriptor.getComponentScopedRefs();
    ServiceRefDescriptor[] serviceRefDescriptors = wsClientDeploymentDescriptor.getServiceRef();

    String[] serviceRefNames0 = getServiceRefNames(componentScopedRefsDescriptors);
    String[] serviceRefNames1 = getServiceRefNames(serviceRefDescriptors);

    return WSUtil.unifyStrings(new String[][]{serviceRefNames0, serviceRefNames1});
  }

  private String[] getServiceRefNames(ServiceRefDescriptor[] serviceRefDescriptors) {
    if(serviceRefDescriptors == null) {
      return new String[0];
    }

    String[] serviceRefNames = new String[serviceRefDescriptors.length];
    for (int i = 0; i < serviceRefDescriptors.length; i++) {
      serviceRefNames[i] = serviceRefDescriptors[i].getServiceRefName().trim();
    }

    return serviceRefNames;
  }

  private String[] getServiceRefNames(ComponentScopedRefsDescriptor[] componentScopedRefsDescriptors) {
    if(componentScopedRefsDescriptors == null) {
      return new String[0];
    }

    String[] serviceRefNames = new String[0];
    for (int i = 0; i < componentScopedRefsDescriptors.length; i++) {
      String[] currentServiceRefNames = getServiceRefNames(componentScopedRefsDescriptors[i].getServiceRef());
      serviceRefNames = WSUtil.unifyStrings(new String[][]{serviceRefNames, currentServiceRefNames});
    }

    return serviceRefNames;
  }

  private WSClientArchiveLocationWrapper[] unifyWSClientArchiveLocationWrappers(WSClientArchiveLocationWrapper[][] wsClientArchiveLocationWrappers) {
    if(wsClientArchiveLocationWrappers == null) {
      return new WSClientArchiveLocationWrapper[0];
    }

    WSClientArchiveLocationWrapper[] allWSClientArchiveLocationWrappers = new WSClientArchiveLocationWrapper[0];
    for(int i = 0; i < wsClientArchiveLocationWrappers.length; i++) {
      WSClientArchiveLocationWrapper[] currentWSClientArchiveLocationWrappers = wsClientArchiveLocationWrappers[i];
      WSClientArchiveLocationWrapper[] newWSClientArchiveLocationWrappers = new WSClientArchiveLocationWrapper[allWSClientArchiveLocationWrappers.length + currentWSClientArchiveLocationWrappers.length];
      System.arraycopy(allWSClientArchiveLocationWrappers, 0, newWSClientArchiveLocationWrappers, 0, allWSClientArchiveLocationWrappers.length);
      System.arraycopy(currentWSClientArchiveLocationWrappers, 0, newWSClientArchiveLocationWrappers, allWSClientArchiveLocationWrappers.length, currentWSClientArchiveLocationWrappers.length);
      allWSClientArchiveLocationWrappers = newWSClientArchiveLocationWrappers;
    }
                                                       
    return allWSClientArchiveLocationWrappers;
  }
  
  public static boolean checkWSClientsDeploymentDescriptorsOR(WSClientDeploymentDescriptor[] wsClientsDeploymentDescriptors) {
    if(wsClientsDeploymentDescriptors == null || wsClientsDeploymentDescriptors.length == 0) {
      return true; 	
    }	  
    
	for(WSClientDeploymentDescriptor wsClientDeploymentDescriptor: wsClientsDeploymentDescriptors) {	    
	  if(wsClientDeploymentDescriptor.isKeepRuntimeMode()) {
	    return true;  
	  }	
	}
     
    return false;        
  }
  
}
