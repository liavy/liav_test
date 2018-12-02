package com.sap.engine.services.webservices.webservices630.server.deploy;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.lib.io.hash.HashUtils;
import com.sap.engine.lib.io.hash.HashCompare;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.exceptions.WSConfigurationException;
import com.sap.engine.services.webservices.exceptions.PatternKeys;
import com.sap.engine.services.webservices.webservices630.server.deploy.common.WSBaseConstants;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WSDirsHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WSRuntimeActivator;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WebServicesConstants;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.WSClientDirsHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.WSClientsConstants;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.WSClientsFactory;
import com.sap.tc.logging.Location;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.*;

/**
 * Title: WSConfigurationHandler
 * Description: The class provides methods, for uploading and downloading web services and ws clients configurations.
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSConfigurationHandler implements WebServicesConstants, WSClientsConstants {

  private static final char SEPARATOR = '/';

  public WSConfigurationHandler() {
  }

  public static void makeWebServicesConfiguration(String webservicesDir, Hashtable moduleCrcTable, Configuration appConfiguration) throws WSConfigurationException {
    String excMsg = "Error occurred, trying to make " + WSDirsHandler.getWebServicesConfigName(WSBaseConstants.WS_CONTAINER_NAME) + ", root configuration " + appConfiguration.getPath() + ", uploading directory " + webservicesDir + ". ";

    Configuration webServicesConfiguration = null;
    try {
      webServicesConfiguration = getAndMakeConfiguration(appConfiguration, WSDirsHandler.getWebServicesConfigName(WSBaseConstants.WS_CONTAINER_NAME));
    } catch(ConfigurationException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }

    makeUniversalWSConfiguration(webservicesDir, moduleCrcTable, webServicesConfiguration);
  }

  public static void makeWSClientsConfiguration(String wsClientsDir, Hashtable moduleCrcTable, Configuration appConfiguration) throws WSConfigurationException {
    String excMsg = "Error occurred, trying to make " + WSClientDirsHandler.getWSClientsConfigName(WSBaseConstants.WS_CONTAINER_NAME) + ", root configuration " + appConfiguration.getPath() + ", uploading directory " + wsClientsDir + ". ";

    Configuration wsClientsConfiguration = null;
    try {
      wsClientsConfiguration = getAndMakeConfiguration(appConfiguration, WSClientDirsHandler.getWSClientsConfigName(WSBaseConstants.WS_CONTAINER_NAME));
    } catch(ConfigurationException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }

    makeUniversalWSConfiguration(wsClientsDir, moduleCrcTable, wsClientsConfiguration);
  }

  public static void makeUniversalWSConfiguration(String wsComponentsRootDir, Hashtable moduleCrcTable, Configuration wsComponentsRootConfiguration) throws WSConfigurationException {
    String excMsg = "Error occurred, trying to make universal ws components configuration, root configuration " + wsComponentsRootConfiguration.getPath() + ", uploading directory " + wsComponentsRootDir + ". ";

    File[] wsComponentsDirs = new File(wsComponentsRootDir).listFiles();
    if(wsComponentsDirs == null) {
      return;
    }

    try {
      for(int i = 0; i < wsComponentsDirs.length; i++) {
        File wsComponentDir = wsComponentsDirs[i];
        if(wsComponentDir.getName().equals(WSBaseConstants.APP_JARS_NAME)) {
          uploadModuleJarsDir(wsComponentDir, moduleCrcTable, wsComponentsRootConfiguration);
        } else{
          uploadDirectory(wsComponentDir.getAbsolutePath(), wsComponentDir.getName(), wsComponentsRootConfiguration);
        }
      }
    } catch(WSConfigurationException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }
  }

  public static void makeWebServicesModuleJarsDir(File moduleJarsDir, Hashtable moduleCrcTable, Configuration appConfiguration) throws WSConfigurationException {
    String excMsg = "Error occurred, trying to upload " + moduleJarsDir.getAbsolutePath() + ", root configuration " + appConfiguration.getPath() + ", relative configuration " + WSDirsHandler.getWebServicesConfigName(WSBaseConstants.WS_CONTAINER_NAME);

    try {
      Configuration webServicesConfiguration = getAndMakeConfiguration(appConfiguration, WSDirsHandler.getWebServicesConfigName(WSBaseConstants.WS_CONTAINER_NAME));
      uploadModuleJarsDir(moduleJarsDir, moduleCrcTable, webServicesConfiguration);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }
  }

  public static void makeWSConfigurations(String[] dirs, Configuration appConfiguration) throws WSConfigurationException {
    makeUniversalWSConfigurations(dirs, appConfiguration, WSDirsHandler.getWebServicesConfigName(WSBaseConstants.WS_CONTAINER_NAME));
  }

  public static void makeWSClientConfigurations(String[] dirs, Configuration appConfiguration) throws WSConfigurationException {
    makeUniversalWSConfigurations(dirs, appConfiguration, WSClientDirsHandler.getWSClientsConfigName(WSBaseConstants.WS_CONTAINER_NAME));
  }

  private static void makeUniversalWSConfigurations(String[] dirs, Configuration appConfiguration, String relConfigPath) throws WSConfigurationException {
    String excMsg = "Error occurred, trying to make web services configuration,  root configuration " + appConfiguration.getPath() + ". ";

    if(dirs == null) {
      return;
    }

    Configuration configuration = null;
    try {
      configuration = getAndMakeConfiguration(appConfiguration, relConfigPath);
    } catch(ConfigurationException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }

    for(int i = 0; i < dirs.length; i++) {
      String wsDir = dirs[i];
      uploadDirectory(wsDir, new File(dirs[i]).getName(), configuration);
    }
  }

  public static Hashtable downloadWebServicesConfiguration(String webServicesDir, Configuration appConfiguration) throws WSConfigurationException {
    String excMsg = "Error occurred, trying to download " + WSDirsHandler.getWebServicesConfigName(WSBaseConstants.WS_CONTAINER_NAME) + ", root configuration " + appConfiguration.getPath() + ", downloading directory " + webServicesDir + ". ";

    Configuration webServicesConfiguration = null;
    try {
      webServicesConfiguration = getConfiguration(appConfiguration, WSDirsHandler.getWebServicesConfigName(WSBaseConstants.WS_CONTAINER_NAME));
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }

    return downloadUniversalWSConfiguration(webServicesDir, webServicesConfiguration);
  }

  public static Hashtable downloadWSClientsConfiguration(String wsClientsDir, Configuration appConfiguration) throws WSConfigurationException {
    String excMsg = "Error occurred, trying to download " + WSClientDirsHandler.getWSClientsConfigName(WSBaseConstants.WS_CONTAINER_NAME) + ", root configuration " + appConfiguration.getPath() + ", downloading directory " + wsClientsDir + ". ";

    Configuration wsClientsConfiguration = null;
    try {
      wsClientsConfiguration = getConfiguration(appConfiguration, WSClientDirsHandler.getWSClientsConfigName(WSBaseConstants.WS_CONTAINER_NAME));
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }

    return downloadUniversalWSConfiguration(wsClientsDir, wsClientsConfiguration);
  }

  public static Hashtable downloadUniversalWSConfiguration(String wsComponentsRootDir, Configuration wsComponentsRootConfiguration) throws WSConfigurationException {
    String excMsg = "Error occurred, trying to download universal ws component configuration, root configuration " + wsComponentsRootConfiguration.getPath() + ", downloading directory " + wsComponentsRootDir + ". ";

    Hashtable configsToDirsMapping = new Hashtable();
    try {
      deleteDirs(wsComponentsRootDir,  wsComponentsRootConfiguration);

      Map wsComponentsConfigurations = wsComponentsRootConfiguration.getAllSubConfigurations();
      Iterator iter = wsComponentsConfigurations.keySet().iterator();

      while(iter.hasNext()) {
        String wsComponentConfigName = (String)iter.next();
        if(wsComponentConfigName.equals(WSBaseConstants.APP_JARS_NAME)) {
          downloadModuleJarsConfig(IOUtil.getFilePath(wsComponentsRootDir, WSBaseConstants.APP_JARS_NAME), (Configuration)wsComponentsConfigurations.get(wsComponentConfigName));
        } else {
          Configuration wsComponentConfiguration = (Configuration)wsComponentsConfigurations.get(wsComponentConfigName);
          String relDir = downloadConfigurationToParent(wsComponentsRootDir, wsComponentConfiguration, new HashSet());
          configsToDirsMapping.put(wsComponentConfiguration.getMetaData().getName(), relDir);
        }
      }
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }

    return configsToDirsMapping;
  }

  public static void downloadWebServicesBaseConfiguration(String webServicesDir, Configuration appConfiguration) throws WSConfigurationException {
    String excMsg = "Error occurred, trying to download " + WSDirsHandler.getWebServicesConfigName(WSBaseConstants.WS_CONTAINER_NAME) + " base configuration, root configuration " + appConfiguration.getPath() + ", downloading directory " + webServicesDir + ". ";

    try {
      Configuration webServicesConfiguration = getConfiguration(appConfiguration, WSDirsHandler.getWebServicesConfigName(WSBaseConstants.WS_CONTAINER_NAME));

      deleteDirs(webServicesDir, webServicesConfiguration);

      Hashtable configsToDirsMapping = new Hashtable();
      Map wsConfigurations = webServicesConfiguration.getAllSubConfigurations();
      Iterator iter = wsConfigurations.keySet().iterator();
      while(iter.hasNext()) {
        String wsConfigName = (String)iter.next();
        if(wsConfigName.equals(WSDirsHandler.getAppJarsRelDir())) {
          downloadModuleJarsConfig(WSDirsHandler.getAppJarsDir(webServicesDir), (Configuration)wsConfigurations.get(wsConfigName));
        } else {
          Configuration wsConfiguration = (Configuration)wsConfigurations.get(wsConfigName);
          String wsRelDir = wsConfigName;
          String wsDir = IOUtil.getFilePath(webServicesDir, wsRelDir);
          downloadWSBaseConfiguration(wsDir, wsConfiguration);
          configsToDirsMapping.put(wsConfigName, wsRelDir);
        }
      }
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }
  }

  public static void downloadWSClientsBaseConfiguration(String wsClientsDir, Configuration appConfiguration) throws WSConfigurationException {
    String excMsg = "Error occurred, trying to download " + WSClientDirsHandler.getWSClientsConfigName(WSBaseConstants.WS_CONTAINER_NAME) + " base configuration, root configuration " + appConfiguration.getPath() + ", downloading directory " + wsClientsDir + ". ";

    try {
      Configuration wsClientsConfiguration = getConfiguration(appConfiguration, WSClientDirsHandler.getWSClientsConfigName(WSBaseConstants.WS_CONTAINER_NAME));

      deleteDirs(wsClientsDir, wsClientsConfiguration);

      Hashtable configsToDirsMapping = new Hashtable();
      Map wsConfigurations = wsClientsConfiguration.getAllSubConfigurations();
      Iterator iter = wsConfigurations.keySet().iterator();
      while(iter.hasNext()) {
        String wsConfigName = (String)iter.next();
        if(wsConfigName.equals(WSBaseConstants.APP_JARS_NAME)) {
          downloadModuleJarsConfig(IOUtil.getFilePath(wsClientsDir, WSBaseConstants.APP_JARS_NAME), (Configuration)wsConfigurations.get(wsConfigName));
        } else {
          Configuration wsConfiguration = (Configuration)wsConfigurations.get(wsConfigName);
          String wsRelDir = wsConfigName;
          String wsClientDir = IOUtil.getFilePath(wsClientsDir, wsRelDir);
          downloadWSClientBaseConfiguration(wsClientDir, wsConfiguration);
          configsToDirsMapping.put(wsConfigName, wsRelDir);
        }
      }
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }
  }

  public static void downloadWSBaseConfiguration(String wsDir, Configuration wsConfiguration) throws WSConfigurationException {
    String excMsg = "Error occurred, trying to download web service base configuration. Configuration " + wsConfiguration.getPath()  + ", root directory " + wsDir + ". ";
    if(wsConfiguration == null) {
      return;
    }

    try {
      deleteDirs(wsDir, wsConfiguration);

      String mappingsFileName = WSDirsHandler.getMappingsFileName();
      String mappingsParentRelDir = WSDirsHandler.getMappingsParentRelDir();
      String mappingsParentDir = WSDirsHandler.getMappingsParentDir(wsDir);
      Properties mappings = null;
      if(existsFile(mappingsFileName, wsConfiguration, mappingsParentRelDir)) {
        downloadFile(mappingsParentDir, mappingsFileName, wsConfiguration, mappingsParentRelDir);
        mappings = WSRuntimeActivator.loadMappingsFile(WSDirsHandler.getMappingsPath(wsDir));
      } else {
        mappings = WSDirsHandler.generateBaseDefaultMappings();
      }

      String wsDDescriptorFileName = WSDirsHandler.getWSDeploymentDescriptorFileName();
      String wsDDescriptorParentRelDir = WSDirsHandler.getWSDeploymentDescriptorParentRelDir(mappings);
      String wsDDescriptorParentDir = WSDirsHandler.getWSDeploymentDescriptorParentDir(wsDir, mappings);
      downloadFile(wsDDescriptorParentDir, wsDDescriptorFileName, wsConfiguration, wsDDescriptorParentRelDir);

      String wsRuntimeDescriptorFileName = WSDirsHandler.getWSRuntimeDescriptorFileName();
      String wsRuntimeDescriptorRelDir = WSDirsHandler.getWSRuntimeDescriptorParentRelDir(mappings);
      String wsRuntimeDescriptorParentDir = WSDirsHandler.getWSRuntimeDescriptorParentDir(wsDir, mappings);
      downloadFile(wsRuntimeDescriptorParentDir, wsRuntimeDescriptorFileName, wsConfiguration, wsRuntimeDescriptorRelDir);

      String moduleJarsRelDir = WSDirsHandler.getJarsRelDir(mappings);
      String moduleJarsDir = WSDirsHandler.getJarsDir(wsDir, mappings);
      downloadConfiguration(moduleJarsDir, moduleJarsRelDir, wsConfiguration, new HashSet());
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }
  }

  public static void downloadWSClientBaseConfiguration(String wsClientDir, Configuration wsClientConfiguration) throws WSConfigurationException {
    String excMsg = "Error occurred, trying to download ws client base configuration. Configuration " + wsClientConfiguration.getPath()  + ", root directory " + wsClientDir + ". ";
    if(wsClientConfiguration == null) {
      return;
    }

    try {
      deleteDirs(wsClientDir, wsClientConfiguration);

      String mappingsFileName = WSClientDirsHandler.getMappingsFileName();
      String mappingsParentRelDir = WSClientDirsHandler.getMappingsParentRelDir();
      String mappingsParentDir = WSClientDirsHandler.getMappingsParentDir(wsClientDir);
      Properties mappings = null;
      if(existsFile(mappingsFileName, wsClientConfiguration, mappingsParentRelDir)) {
        downloadFile(mappingsParentDir, mappingsFileName, wsClientConfiguration, mappingsParentRelDir);
        mappings = WSClientsFactory.loadMappings(wsClientDir);
      } else {
        mappings = WSClientDirsHandler.generateBaseDefaultMappings();
      }

      String wsClientsDDescriptorFileName = WSClientDirsHandler.getWSClientsDeploymentDescriptorFileName();
      String wsClientsDDescriptorParentRelDir = WSClientDirsHandler.getWSClientsDDescriptorParentRelDir(mappings);
      String wsClientsDDescriptorParentDir = WSClientDirsHandler.getWSClientsDDescriptorParentDir(wsClientDir, mappings);
      downloadFile(wsClientsDDescriptorParentDir, wsClientsDDescriptorFileName, wsClientConfiguration, wsClientsDDescriptorParentRelDir);

      String wsClientRuntimeDescriptorFileName = WSClientDirsHandler.getWSClientsRuntimeDescriptorFileName();
      String wsClientRuntimeDescriptorRelDir = WSClientDirsHandler.getWSClientsRuntimeDescriptorParentRelDir(mappings);
      String wsClientRuntimeDescriptorParentDir = WSClientDirsHandler.getWSClientsRuntimeDescriptorParentDir(wsClientDir, mappings);
      downloadFile(wsClientRuntimeDescriptorParentDir, wsClientRuntimeDescriptorFileName, wsClientConfiguration, wsClientRuntimeDescriptorRelDir);

      String jarsRelDir = WSClientDirsHandler.getJarsRelDir(mappings);
      String jarsDir = WSClientDirsHandler.getJarsDir(wsClientDir, mappings);
      downloadConfiguration(jarsDir, jarsRelDir, wsClientConfiguration, new HashSet());
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }
  }

  public static void downloadWSConfiguration(String wsDir, String wsConfigName, Configuration rootConfiguration) throws WSConfigurationException {
    String excMsg = "Error occurred, trying to download ws configuration. Root configuration " + rootConfiguration.getPath() + ", relative configuration " + WSDirsHandler.getWebServicesConfigName(WSBaseConstants.WS_CONTAINER_NAME) + ", ws configuration " + wsConfigName + ", downloading directory " + wsDir + ". ";

    Configuration webServicesConfiguration = null;
    try {
      webServicesConfiguration = getConfiguration(rootConfiguration, WSDirsHandler.getWebServicesConfigName(WSBaseConstants.WS_CONTAINER_NAME));
    } catch(ConfigurationException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }

    downloadConfiguration(wsDir, wsConfigName, webServicesConfiguration, new HashSet());
  }

  public static void deleteWSConfiguration(Configuration appConfiguration, String wsConfigName) throws WSConfigurationException {
    deleteConfiguration(appConfiguration, WSDirsHandler.getWebServicesConfigName(WSBaseConstants.WS_CONTAINER_NAME), wsConfigName, "/");
  }

  public static void deleteWSClientConfiguration(Configuration appConfiguration, String wsClientConfigName) throws WSConfigurationException {
    deleteConfiguration(appConfiguration, WSClientDirsHandler.getWSClientsConfigName(WSBaseConstants.WS_CONTAINER_NAME), wsClientConfigName, "/");
  }

  public static boolean existsWebServicesConfiguration(Configuration appConfiguration) throws ConfigurationException {
    return existsSubConfiguration(appConfiguration, WSDirsHandler.getWebServicesConfigName(WSBaseConstants.WS_CONTAINER_NAME));
  }

  public static Configuration getWebServicesConfiguration(Configuration appConfiguration) throws ConfigurationException {
    return getConfiguration(appConfiguration, WSDirsHandler.getWebServicesConfigName(WSBaseConstants.WS_CONTAINER_NAME));
  }

  public static boolean existsWSClientsConfiguration(Configuration appConfiguration) throws ConfigurationException {
    return existsSubConfiguration(appConfiguration, WSClientDirsHandler.getWSClientsConfigName(WSBaseConstants.WS_CONTAINER_NAME));
  }

  public static Configuration getWSClientsConfiguration(Configuration appConfiguration) throws ConfigurationException {
    return getConfiguration(appConfiguration, WSClientDirsHandler.getWSClientsConfigName(WSBaseConstants.WS_CONTAINER_NAME));
  }

  public static Hashtable downloadConfiguration(String rootDir, String configRelPath, Configuration rootConfiguration, Set configExcludeList) throws WSConfigurationException {
    String excMsg = "Error occurred, trying to download " + configRelPath + " configuration, root configuratrion " + rootConfiguration.getPath() + ". ";

    Hashtable configsToDirsMapping = null;
    try {
      if(existsSubConfiguration(rootConfiguration, configRelPath)) {
        Configuration configuration = getConfiguration(rootConfiguration, configRelPath);
        configsToDirsMapping = downloadConfiguration(rootDir, configuration, configExcludeList);
      }
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{excMsg, "none"};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }

    return configsToDirsMapping;
  }

  public static Hashtable downloadConfiguration(String rootDir, Configuration configuration, Set configExcludeList) throws WSConfigurationException {
    downloadFiles(rootDir, configuration);
    return downloadSubConfigurations(rootDir, configuration, configExcludeList);
  }

  public static void downloadFiles(String rootDir, Configuration configuration) throws WSConfigurationException {
    String excMsg = "Error occurred, downloading files from configuration " + configuration.getPath() + ". ";

    Map fileRelPathEntriesMap = null;
    try {
      fileRelPathEntriesMap = configuration.getAllConfigEntries();
    } catch(ConfigurationException e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      String msg = excMsg + " Unable to extract file relative paths. ";
      Object[] args = new Object[]{msg, "none"};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }

    Set fileRelPathEntries = fileRelPathEntriesMap.keySet();
    Iterator iter = fileRelPathEntries.iterator();
    while(iter.hasNext()) {
      String fileRelPath = (String)iter.next();
      downloadFile(rootDir, fileRelPath, configuration);
    }
  }

//  private static void deleteFiles(String rootDir, Configuration configuration) throws WSConfigurationException {
//    String excMsg = "Error occurred, downloading files from configuration " + configuration.getPath() + ". ";
//
//    if(rootDir == null) {
//      return;
//    }
//
//    File[] files = new File(rootDir).listFiles();
//    if(files == null) {
//      return;
//    }
//
//    Map fileRelPathEntriesMap = null;
//    try {
//      fileRelPathEntriesMap = configuration.getAllConfigEntries();
//    } catch(ConfigurationException e) {
//      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
//      wsLocation.catching(e);
//
//      String msg = excMsg + " Unable to extract file relative paths. ";
//      Object[] args = new Object[]{msg, "none"};
//      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
//    }
//
//    for(int i = 0; i < files.length; i++) {
//      if()
//    }
//  }

  public static void downloadFileToRootDir(String rootDir, String fileRelPath, Configuration configuration) throws WSConfigurationException {
    String excMsg = "Error occurred, downloading " + fileRelPath + ", root directory " + rootDir + ", from configuration " + configuration.getPath() + ". ";

    String parentDir = null;
    try {
      parentDir = rootDir + WebServicesConstants.SEPARATOR + configuration.getMetaData().getName();
    } catch(ConfigurationException e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      String msg = excMsg + " Unable to construct parent directory name. ";
      Object[] args = new Object[]{msg, "none"};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }

    downloadFile(parentDir, fileRelPath, configuration);
  }

  public static boolean existsFile630(String fileRelPath, Configuration rootConfiguration) throws ConfigurationException {
    if(fileRelPath.startsWith("/")) {
      fileRelPath = fileRelPath.substring(1);
    }

    return rootConfiguration.existsConfigEntry(fileRelPath) || rootConfiguration.existsConfigEntry("/" + fileRelPath);
  }

  public static void downloadFile630(String parentDir, String fileRelPath, Configuration configuration) throws WSConfigurationException {
    String excMsg = "Error occurred, downloading " + fileRelPath + ", parent directory " + parentDir + ", from configuration " + configuration.getPath() + ". ";

    InputStream in = null;
    OutputStream out = null;
    try {
      File file = new File(parentDir, fileRelPath);
      fileRelPath = fileRelPath.replace('\\', '/');
      boolean isForDownload = isForDownload(new File(parentDir, fileRelPath), (byte[])configuration.getConfigEntry(fileRelPath));

      if (isForDownload) {
        IOUtil.mkDirs(new File[]{file.getParentFile()});
        Configuration fileConfiguration = getConfiguration(configuration, IOUtil.getRelativeDir(fileRelPath));
        in = fileConfiguration.getFile(file.getName());
        out = new FileOutputStream(file);
        IOUtil.copy(in, out);
      }
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{excMsg, "none"};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    } finally {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);

      IOUtil.closeInputStreams(new InputStream[]{in}, new String[]{"Warning! " + excMsg + " Unable to close input stream. "}, wsLocation);
      IOUtil.closeOutputStreams(new OutputStream[]{out}, new String[]{"Warning! " + excMsg + "Unable to close output stream. "}, wsLocation);
    }
  }

  public static boolean existsFile70(String relFilePath, Configuration rootConfiguration) throws ConfigurationException {
    String relativeDir = IOUtil.getRelativeDir(relFilePath);
    String fileName = new File(relFilePath).getName();

    if(!existsSubConfiguration(rootConfiguration, relativeDir)) {
      return false;
    }

    Configuration configuration = getConfiguration(rootConfiguration, relativeDir);
    return configuration.existsConfigEntry(fileName);
  }

  public static void downloadFile70(String parentDir, String relFilePath, Configuration rootConfiguration) throws WSConfigurationException {
    File file = new File(parentDir, relFilePath);
    String excMsg = "Error occurred, downloading file" + file.getAbsolutePath() + " to configuration " + rootConfiguration.getPath() + ". ";

    Configuration fileConfiguration = null;
    try {
      fileConfiguration = getConfiguration(rootConfiguration, IOUtil.getRelativeDir(relFilePath));
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{excMsg, "none"};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }

    downloadFile630(file.getParent(), file.getName(), fileConfiguration);
  }

  public static boolean existsFile(String relFilePath, Configuration rootConfiguration, String relConfigPath) throws ConfigurationException {
    if(relConfigPath == null || relConfigPath.equals("")) {
      return existsFile(relFilePath, rootConfiguration);
    }

    if(!existsSubConfiguration(rootConfiguration, relConfigPath)) {
      return false;
    }

    return existsFile(relFilePath, getConfiguration(rootConfiguration, relConfigPath));
  }

  public static boolean existsFile(String relFilePath, Configuration rootConfiguration) throws ConfigurationException {
    return existsFile630(relFilePath, rootConfiguration) || existsFile70(relFilePath, rootConfiguration);
  }

  public static void downloadFile(String parentDir, String relFilePath, Configuration rootConfiguration) throws WSConfigurationException {
    File file = new File(parentDir, relFilePath);
    String excMsg = "Error occurred, downloading file" + file.getAbsolutePath() + " to configuration " + rootConfiguration.getPath() + ". ";

    if (relFilePath.startsWith("/") || relFilePath.startsWith("\\")) {
      relFilePath = relFilePath.substring(1);
    }

    try {
      if(rootConfiguration.existsConfigEntry(relFilePath)) {
        downloadFile630(parentDir, relFilePath, rootConfiguration);
      } else if (rootConfiguration.existsConfigEntry(WebServicesConstants.SEPARATOR + relFilePath)) {
        downloadFile630(parentDir, WebServicesConstants.SEPARATOR + relFilePath, rootConfiguration);
      } else {
        downloadFile70(parentDir, relFilePath, rootConfiguration);
      }
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{excMsg, "none"};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }
  }

  public static void downloadFile(String parentDir, String relFilePath, Configuration rootConfiguration, String relConfigPath) throws WSConfigurationException {
    String excMsg = "Error occurred, downloading file" + new File(parentDir, relFilePath).getAbsolutePath() + " to  root configuration " + rootConfiguration.getPath() + ", relative configuration " + relConfigPath + ". ";

    try {
       downloadFile(parentDir, relFilePath, getConfiguration(rootConfiguration, relConfigPath));
    } catch(ConfigurationException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + " Unable to retrieve relative configuration. ";
      Object[] args = new Object[]{msg};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }
  }

  public static void downloadFiles(Hashtable parentDirs, String[] relFilePaths, Configuration rootConfiguration, String relConfigPath) throws WSConfigurationException {
    if(relFilePaths == null) {
      return;
    }

    for(int i = 0; i < relFilePaths.length; i++) {
      String relFilePath = relFilePaths[i];
      String parentDir = (String)parentDirs.get(relFilePath);
      downloadFile(parentDir , relFilePaths[i], rootConfiguration, relConfigPath);
    }
  }

  public static void downloadFilesToRootDir(String rootDir, String[] relFilePaths, Configuration rootConfiguration, String relConfigPath) throws WSConfigurationException {
    if(relFilePaths == null) {
      return;
    }

    String parentDir = rootDir + WSBaseConstants.SEPARATOR + relConfigPath;
    for(int i = 0; i < relFilePaths.length; i++) {
      downloadFile(parentDir, relFilePaths[i], rootConfiguration, relConfigPath);
    }
  }

  public static Hashtable downloadSubConfigurations(String rootDir, Configuration configuration, Set configExcludeList) throws WSConfigurationException {
    String excMsg = "Error occurred, downloading subconfigurations for configuration " + configuration.getPath() + ". ";

    Hashtable configsToDirsMapping = null;
    try {
      deleteDirs(rootDir, configuration);
      configsToDirsMapping = downloadConfigurations(rootDir, configuration.getAllSubConfigurations(), configExcludeList);
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      String msg = excMsg;
      Object[] args = new Object[]{msg, "none"};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }

    return configsToDirsMapping;                  
  }

  public static void deleteDirs(String rootDir, Configuration configuration) throws WSConfigurationException {
    String excMsg = "Error occurred, deleting child directories for directory " + rootDir +  ", configuration " + configuration.getPath() + ". ";

    if(rootDir == null) {
      return;
    }

    File[] files = new File(rootDir).listFiles();
    if(files == null) {
      return;
    }

    Map subConfigurations = new HashMap();
    try {
      subConfigurations = configuration.getAllSubConfigurations();
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      String msg = excMsg;
      Object[] args = new Object[]{msg, "none"};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }

    for(int i = 0; i < files.length; i++) {
      File currentFile = files[i];
      if(currentFile.isDirectory() && !subConfigurations.containsKey(currentFile.getName())) {
        boolean isDeleted = true;
        try {
          isDeleted = IOUtil.deleteDir(currentFile);
        } catch(Exception e) {
          Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
          wsLocation.catching(e);

          String msg = excMsg;
          Object[] args = new Object[]{msg, "none"};
          throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
        }

        if(!isDeleted) {
          String msg = excMsg + "Unable to delete directory " + currentFile + ". ";
          Object[] args = new Object[]{msg, "none"};
          throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args);
        }
      }
    }
  }

  public static Hashtable downloadConfigurations(String parentDir, Map configurations, Set configExcludeList) throws WSConfigurationException {
    String excMsg = "Error occurred, trying to download configurations to parent directory " + parentDir + ". ";

    if(configurations == null) {
      return new Hashtable();
    }

    Hashtable congigsToDirsMapping = new Hashtable();
    Iterator iter = configurations.keySet().iterator();
    while(iter.hasNext()) {
      String configurationName = (String)iter.next();
      if(configExcludeList.contains(configurationName)) {
        continue;
      }

      Configuration configuration = (Configuration)configurations.get(configurationName);
      String relDir = downloadConfigurationToParent(parentDir, configuration, configExcludeList);
      try {
        congigsToDirsMapping.put(configuration.getMetaData().getName(), relDir);
      } catch(ConfigurationException e) {
        Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
        wsLocation.catching(e);

        String msg = excMsg + "Unable to construct configuration to directory name mapping for configuration " + configuration.getPath() + ". ";
        Object[] args = new Object[]{msg, "none"};
        throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
      }
    }

    return congigsToDirsMapping;
  }

  public static String downloadConfigurationToParent(String parentDir, Configuration configuration, Set configExcludeList) throws WSConfigurationException {
    String excMsg = "Error occurred, trying to download configuration" + configuration.getPath() + " to parent directory " + parentDir + ". ";

    String relDir = null;
    String rootDir = null;
    try {
      relDir = configuration.getMetaData().getName();
      rootDir = parentDir + WSBaseConstants.SEPARATOR + configuration.getMetaData().getName();
    } catch(ConfigurationException e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      String msg = excMsg + " Unable to construct directory name for download.";
      Object[] args = new Object[]{msg, "none"};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }

    downloadConfiguration(rootDir, configuration, configExcludeList);
    return relDir;
  }

  public static void uploadDirectory(String dir, String configRelPath, Configuration rootConfiguration) throws WSConfigurationException {
    String excMsg = "Error occurred, trying to make " + configRelPath + " configuration, root configuratrion " + rootConfiguration.getPath() + ". ";

    try {
      if(!IOUtil.isEmptyDir(dir)) {
        Configuration configuration = getAndMakeConfiguration(rootConfiguration, configRelPath);
        uploadDirectory(dir, configuration);
      }
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{excMsg, "none"};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }
  }

  public static void uploadDirectory(String dir, Configuration configuration) throws WSConfigurationException {
    uploadDirectory(new File(dir), configuration);
  }

  public static void uploadDirectory(File dir, Configuration configuration) throws WSConfigurationException {
    String excMsg = "Error occurred, uploading directory " + dir.getAbsolutePath() + " to configuration " + configuration.getPath() + ". ";

    File[] files = dir.listFiles();
    if(files == null) {
      return;
    }

    for(int i = 0; i < files.length; i++) {
      File file = files[i];
      try {
        if (file.isFile()) {
          uploadFile(file.getParent(), file.getName(), configuration);
        } else {
          uploadDirectory(file, getAndMakeConfiguration(configuration, file.getName()));
        }
      } catch(Exception e) {
        Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
        wsLocation.catching(e);

        Object[] args = new Object[]{excMsg, "none"};
        throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
      }
    }
  }

  public static void uploadFile630(String parentDir, String relFilePath, Configuration rootConfiguration) throws WSConfigurationException {
    File file = new File(parentDir, relFilePath);
    String excMsg = "Error occurred, uploading file" + file.getAbsolutePath() + " to configuration " + rootConfiguration.getPath() + ". ";

    try {
      Configuration fileConfiguration = getAndMakeConfiguration(rootConfiguration, IOUtil.getRelativeDir(relFilePath));

      relFilePath = relFilePath.replace('\\', '/');
      if (relFilePath.startsWith("/") || relFilePath.startsWith("\\")) {
        relFilePath = relFilePath.substring(1);
      }
      if (!rootConfiguration.existsConfigEntry(relFilePath)) {
        if(!rootConfiguration.existsConfigEntry(WebServicesConstants.SEPARATOR + relFilePath)) {
          rootConfiguration.addConfigEntry(relFilePath, HashUtils.generateFileHash(file));
          fileConfiguration.addFileEntry(file);
        } else {
          rootConfiguration.modifyConfigEntry(WebServicesConstants.SEPARATOR + relFilePath, HashUtils.generateFileHash(file));
          fileConfiguration.updateFile(file);
        }
      } else {
        rootConfiguration.modifyConfigEntry(relFilePath, HashUtils.generateFileHash(file));
        fileConfiguration.updateFile(file);
      }
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{excMsg, "none"};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }
  }

  public static void uploadFile(String parentDir, String relFilePath, Configuration rootConfiguration) throws WSConfigurationException {
    File file = new File(parentDir, relFilePath);
    String excMsg = "Error occurred, uploading file" + file.getAbsolutePath() + " to configuration " + rootConfiguration.getPath() + ". ";

    if (relFilePath.startsWith("/") || relFilePath.startsWith("\\")) {
      relFilePath = relFilePath.substring(1);
    }

    try {
      if(rootConfiguration.existsConfigEntry(relFilePath) || rootConfiguration.existsConfigEntry(WebServicesConstants.SEPARATOR + relFilePath)) {
        uploadFile630(parentDir, relFilePath, rootConfiguration);
      } else {
        uploadFile70(parentDir, relFilePath, rootConfiguration);
      }
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{excMsg, "none"};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }
  }

  public static void uploadFile(String parentDir, String relFilePath, Configuration rootConfiguration, String relConfigPath) throws WSConfigurationException {
    String excMsg = "Error occurred, uploading file" + new File(parentDir, relFilePath).getAbsolutePath() + " to  root configuration " + rootConfiguration.getPath() + ", relative configuration " + relConfigPath + ". ";

    try {
      uploadFile(parentDir, relFilePath, getAndMakeConfiguration(rootConfiguration, relConfigPath));
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{excMsg, "none"};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }
  }

  public static void uploadFile70(String parentDir, String relFilePath, Configuration rootConfiguration) throws WSConfigurationException {
    File file = new File(parentDir, relFilePath);
    String excMsg = "Error occurred, uploading file" + file.getAbsolutePath() + " to configuration " + rootConfiguration.getPath() + ". ";

    Configuration fileConfiguration = null;
    try {
      fileConfiguration = getAndMakeConfiguration(rootConfiguration, IOUtil.getRelativeDir(relFilePath));
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{excMsg, "none"};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }

    uploadFile630(file.getParent(), file.getName(), fileConfiguration);
  }

  public static boolean existsSubConfiguration(Configuration rootConfiguration, String str) throws ConfigurationException {
    String delimiter = new String(new char[]{SEPARATOR});
    str = str.replace('\\', '/');
    return existsSubConfiguration(rootConfiguration, str, delimiter);
  }

  public static boolean existsSubConfiguration(Configuration rootConfiguration, String str, String delimiter) throws ConfigurationException {
    StringTokenizer stringTokenizer = new StringTokenizer(str, delimiter);
    Configuration configuration = rootConfiguration;
    while(stringTokenizer.hasMoreTokens()) {
      String currentToken = stringTokenizer.nextToken();
      if(!configuration.existsSubConfiguration(currentToken)) {
       return false;
      } else {
        configuration = configuration.getSubConfiguration(currentToken);
      }
    }

    return true;
  }

  public static Configuration getConfiguration(Configuration rootConfiguration, String str) throws ConfigurationException {
    return getConfiguration(rootConfiguration, str, false);
  }

  public static Configuration getAndMakeConfiguration(Configuration rootConfiguration, String str) throws ConfigurationException {
    return getConfiguration(rootConfiguration, str, true);
  }

  public static Configuration getConfiguration(Configuration rootConfiguration, String str, boolean toBeMade) throws ConfigurationException {
    String delimiter = new String(new char[]{SEPARATOR});
    str = str.replace('\\', '/');
    return getConfiguration(rootConfiguration, str, delimiter, toBeMade);
  }

  public static Configuration getConfiguration(Configuration rootConfiguration, String str, String delimiter, boolean toBeMade) throws ConfigurationException {
    StringTokenizer stringTokenizer = new StringTokenizer(str, delimiter);
    Configuration configuration = rootConfiguration;
    while(stringTokenizer.hasMoreTokens()) {
      String currentToken = stringTokenizer.nextToken();
      if(!configuration.existsSubConfiguration(currentToken)) {
        if(toBeMade) {
          configuration.createSubConfiguration(currentToken);
        }
      }
      configuration = configuration.getSubConfiguration(currentToken);
    }
    return configuration;
  }

  public static void deleteConfiguration(Configuration rootConfiguration, String relConfigName, String delimiter) throws WSConfigurationException {
    deleteConfiguration(rootConfiguration, "", relConfigName, delimiter);
  }

  public static void deleteConfiguration(Configuration rootConfiguration, String relConfigPath, String relConfigName, String delimiter) throws WSConfigurationException {
    String excMsg = "Error occurred, trying to delete configuration " + relConfigName + ", root configuration " + rootConfiguration.getPath() + ", relative configuration " + relConfigPath + ". ";

    try {
      if(!existsSubConfiguration(rootConfiguration, relConfigPath)) {
        return;
      }

      Configuration relConfiguration = getConfiguration(rootConfiguration, relConfigPath, delimiter, false);
      String relConfigPath2 = IOUtil.getParentPath(relConfigName, delimiter);
      String configName = IOUtil.getName(relConfigName, delimiter);

      if(!existsSubConfiguration(relConfiguration, relConfigPath2)) {
        return;
      }

      Configuration relConfiguration2 = getConfiguration(relConfiguration, relConfigPath2);
      relConfiguration2.deleteSubConfigurations(new String[]{configName});
    } catch(ConfigurationException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }
  }

  public static boolean isForDownload(String filePath, byte[] dbCRC) throws IOException {
    return isForDownload(new File(filePath), dbCRC);
  }

  private static boolean isForDownload(File file, byte[] dbCRC) throws IOException {
    boolean isForDownload = false;
    if(file.exists()) {
      byte[] crc = HashUtils.generateFileHash(file);
      isForDownload = !HashCompare.compareHash(crc, dbCRC);
    } else {
      isForDownload = true;
    }

    return isForDownload;
  }

  public static void uploadModuleJarsDir(File moduleJarsDir, Hashtable moduleCrcTable, Configuration configuration) throws WSConfigurationException {
    String excMsg = "Error occurred, trying to upload " + moduleJarsDir.getAbsolutePath() + " into configuration " + configuration.getPath() + ". ";

    try {
      String moduleJarsConfigName = moduleJarsDir.getName();
      if(existsSubConfiguration(configuration, moduleJarsConfigName)) {
        deleteConfiguration(configuration, moduleJarsConfigName, "/");
      }
      Configuration moduleJarsConfig = getAndMakeConfiguration(configuration, moduleJarsConfigName);
      uploadDirectory(moduleJarsDir, moduleJarsConfig);

      Configuration moduleCrcConfiguration = getAndMakeConfiguration(moduleJarsConfig, WSBaseConstants.MODULE_CRC_TABLE);
      uploadTable(moduleCrcTable, moduleCrcConfiguration);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }
  }

  private static void uploadTable(Hashtable table, Configuration configuration) throws ConfigurationException{
    if(table == null) {
      return;
    }

    Enumeration keysEnum = table.keys();
    while(keysEnum.hasMoreElements()) {
      String key = (String)keysEnum.nextElement();
      configuration.addConfigEntry(key, table.get(key));
    }
  }

  private static Hashtable downloadModuleJarsConfig(String rootDir, Configuration moduleJarsConfig) throws WSConfigurationException {
    String excMsg = "Error occurred, trying to download " + moduleJarsConfig.getPath() + ", root directory " + rootDir + ". ";

    Hashtable moduleCrcTable = new Hashtable();
    try {
      HashSet configExcludeList = new HashSet();
      configExcludeList.add(WSBaseConstants.MODULE_CRC_TABLE);
      downloadConfiguration(rootDir, moduleJarsConfig, configExcludeList);

      if(existsSubConfiguration(moduleJarsConfig, WSBaseConstants.MODULE_CRC_TABLE)) {
        Configuration moduleCrcConfiguration = getConfiguration(moduleJarsConfig, WSBaseConstants.MODULE_CRC_TABLE);
        moduleCrcTable = loadTable(moduleCrcConfiguration);
      }
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }

    return moduleCrcTable;
  }

  public static Hashtable loadWSModuleCrcTable(Configuration appConfiguration) throws WSConfigurationException {
    String excMsg = "Error occurred, trying to download web services module crc table, configuration " + appConfiguration.getPath() + ". ";

    Hashtable moduleCrcTable = null;
    try {
      if(!existsSubConfiguration(appConfiguration, WSDirsHandler.getAppJarsConfigName(WSDirsHandler.getWebServicesConfigName(WSBaseConstants.WS_CONTAINER_NAME)))) {
        return new Hashtable();
      }

      Configuration moduleJarsConfig = getConfiguration(appConfiguration, WSDirsHandler.getAppJarsConfigName(WSDirsHandler.getWebServicesConfigName(WSBaseConstants.WS_CONTAINER_NAME)));
      if(!existsSubConfiguration(moduleJarsConfig,  WSBaseConstants.MODULE_CRC_TABLE)) {
        return new Hashtable();
      }
      Configuration moduleCrcConfiguration = getConfiguration(moduleJarsConfig, WSBaseConstants.MODULE_CRC_TABLE);
      moduleCrcTable = loadTable(moduleCrcConfiguration);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }

    return moduleCrcTable;
  }

  public static Hashtable loadWSClientsModuleCrcTable(Configuration appConfiguration) throws WSConfigurationException {
    String excMsg = "Error occurred, trying to download ws clients module crc table, configuration " + appConfiguration.getPath() + ". ";

    Hashtable moduleCrcTable = null;
    try {
      if(!existsSubConfiguration(appConfiguration, WSClientDirsHandler.getAppJarsConfigName(WSClientDirsHandler.getWSClientsConfigName(WSBaseConstants.WS_CONTAINER_NAME)))) {
        return new Hashtable();
      }

      Configuration moduleJarsConfig = getConfiguration(appConfiguration, WSClientDirsHandler.getAppJarsConfigName(WSClientDirsHandler.getWSClientsConfigName(WSBaseConstants.WS_CONTAINER_NAME)));
      if(!existsSubConfiguration(moduleJarsConfig, WSBaseConstants.MODULE_CRC_TABLE)) {
        return new Hashtable();
      }
      Configuration moduleCrcConfiguration = getConfiguration(moduleJarsConfig, WSBaseConstants.MODULE_CRC_TABLE);
      moduleCrcTable = loadTable(moduleCrcConfiguration);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSConfigurationException(PatternKeys.WS_CONFIGURATION_EXCEPTION, args, e);
    }

    return moduleCrcTable;
  }

  private static Hashtable loadTable(Configuration configuration) throws ConfigurationException {
    if(configuration == null) {
      return new Hashtable();
    }

    Hashtable table = new Hashtable();
    Map configEntries = configuration.getAllConfigEntries();
    Iterator iter = configEntries.keySet().iterator();
    while(iter.hasNext()) {
      String key = (String)iter.next();
      table.put(key, configEntries.get(key));
    }

    return table;
  }

  private static HashMap constructDirs(String parentDir, Iterator relDirsIterator) {
    if(relDirsIterator == null) {
      return new HashMap();
    }

    HashMap dirs = new HashMap();
    while(relDirsIterator.hasNext()) {
      String relDir = (String)relDirsIterator.next();
      dirs.put(relDir, IOUtil.getFilePaths(parentDir, new String[]{relDir})[0]);
    }

    return dirs;
  }

}