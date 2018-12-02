package com.sap.engine.services.webservices.webservices630.server.deploy.ws.update;

import com.sap.engine.services.webservices.server.deploy.ws.*;
import com.sap.engine.services.webservices.webservices630.server.deploy.WSConfigurationHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.common.ModuleDirsHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.common.ModuleFileStorageHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.common.update.ModuleUpdateProcessor;
import com.sap.engine.services.webservices.webservices630.server.deploy.common.update.ModuleUpdateResult;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.jar.JarUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.*;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.deploy.WSDeployResult;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.preprocess.WSArchiveLocationWrapper;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.preprocess.WSFileLocationWrapper;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.preprocess.WSLocationWrapper;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.preprocess.WSPreprocessor;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.exceptions.WSConfigurationException;
import com.sap.engine.services.webservices.runtime.definition.WSRuntimeDefinition;
import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.lib.io.hash.HashUtils;
import com.sap.tc.logging.Location;

import java.util.*;
import java.io.File;
import java.io.IOException;

/**
 * Title: WSUpdateProcessor
 * Description: The class contains methods for processing and regenerating web services deployment files.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSUpdateProcessor {

  private WSDefinitionFactory wsDefinitionFactory = null;
  private WSPreprocessor wsPreprocessor = null;  
  private WSDeployGenerator wsDeployGenerator = null;
  private SingleWSUpdateProcessor singleWSUpdateProcessor = null;
  private ModuleUpdateProcessor moduleUpdateProcessor = null;
  private ModuleFileStorageHandler moduleFileStorageHandler = null;
  private JarUtil jarUtil = null;

  public WSUpdateProcessor(WSDeployGenerator wsDeployGenerator, ModuleUpdateProcessor moduleUpdateProcessor, ModuleFileStorageHandler moduleFileStorageHandler) {
    this.wsDefinitionFactory = new WSDefinitionFactory();
    this.wsPreprocessor = new WSPreprocessor();
    this.wsDeployGenerator = wsDeployGenerator;
    this.singleWSUpdateProcessor = new SingleWSUpdateProcessor(wsDeployGenerator);
    this.moduleUpdateProcessor = moduleUpdateProcessor;
    this.moduleFileStorageHandler = moduleFileStorageHandler;
    this.jarUtil = new JarUtil();
  }

  public WSAppUpdateResult updateWebServices(String applicationName, String webServicesDir, String webServicesWorkingDir, File[] moduleArchives, Configuration appConfiguration) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to update web services for application " + applicationName + ". ";

    wsPreprocessor.preprocess(applicationName, webServicesDir, appConfiguration);

    Hashtable notChangedModuleArchives = null;
    String[] notChangedWS = null;

    WSArchiveLocationWrapper[] newWSArchiveLocationWrappers = null;
    WSFileLocationWrapper[] oldFileLocationWrappersAll = null;
    try {
      notChangedModuleArchives = moduleUpdateProcessor.collectNotChangedModules(moduleArchives, WSConfigurationHandler.loadWSModuleCrcTable(appConfiguration));
      notChangedWS =  moduleUpdateProcessor.collectNotChangedWSComponentsPerModules(notChangedModuleArchives.keySet(), webServicesDir);
      File[] moduleArchivesChanged = moduleArchivesChanged = IOUtil.filterFiles(moduleArchives, notChangedModuleArchives.keySet());

      newWSArchiveLocationWrappers = wsDefinitionFactory.loadWSArchiveLocationWrappers(applicationName, moduleArchivesChanged);

      oldFileLocationWrappersAll = wsPreprocessor.loadWSFileLocationWrappers(applicationName, webServicesDir, appConfiguration);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + " Unable to collect web services update information. ";
      Object[] args = new String[]{msg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    WSUpdateResult wsUpdateResult = updateWebServices(applicationName, webServicesDir, webServicesWorkingDir, newWSArchiveLocationWrappers, oldFileLocationWrappersAll, WSUtil.collectSet(notChangedWS), appConfiguration);
    if (wsUpdateResult.getDeployedComponentNames() == null || wsUpdateResult.getDeployedComponentNames().length == 0) {
      WSAppUpdateResult wsAppUpdateResult = new WSAppUpdateResult();
      wsAppUpdateResult.setWsUpdateResult(wsUpdateResult);
      wsAppUpdateResult.setModuleDeployResult(new ModuleUpdateResult());
      return wsAppUpdateResult;
    }

    ModuleUpdateResult moduleUpdateResult = moduleUpdateProcessor.updateModuleFiles(applicationName, webServicesDir, getModuleArchives(newWSArchiveLocationWrappers), notChangedModuleArchives, /* wsDeployGenerator.collectKeySet(deployedWSPerModuleNotChanged),*/ appConfiguration, WSDirsHandler.getWebServicesConfigName(WebServicesConstants.WS_CONTAINER_NAME));

    WSAppUpdateResult wsAppUpdateResult = new WSAppUpdateResult();
    wsAppUpdateResult.setModuleDeployResult(moduleUpdateResult);
    wsAppUpdateResult.setWsUpdateResult(wsUpdateResult);
    return wsAppUpdateResult;
  }

  private WSUpdateResult updateWebServices(String applicationName, String webServicesDir, String webServicesWorkingDir, WSArchiveLocationWrapper[] newWSArchiveLocationWrappers, WSFileLocationWrapper[] oldWSFileLocationWrappersAll, Set notChangedWSSet, Configuration appConfiguration) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to update web services for application " + applicationName + ". ";

    WSFileLocationWrapper[] notChangedWSFileLocationWrappers = filterWSFileLocationWrappers(oldWSFileLocationWrappersAll, notChangedWSSet, false);
    WSFileLocationWrapper[] oldWSFileLocationWrappers = filterWSFileLocationWrappers(oldWSFileLocationWrappersAll, notChangedWSSet, true);

    WSUpdateInfo wsUpdateInfo = collectWSUpdateInfo(newWSArchiveLocationWrappers, oldWSFileLocationWrappers);
    WSUpdateResult notChangedWSUpdateResult = singleWSUpdateProcessor.defineWSUpdateResult(notChangedWSFileLocationWrappers);

    if((oldWSFileLocationWrappers == null || oldWSFileLocationWrappers.length == 0) && (newWSArchiveLocationWrappers == null || newWSArchiveLocationWrappers.length == 0)) {
      return notChangedWSUpdateResult;
    }

    int maxIndex = WSUtil.getMaxPositiveInt(collectIndexSet(oldWSFileLocationWrappersAll));
    int[] freeIndexes = WSUtil.getIntArray(WSUtil.getMissedInts(collectIndexSet(oldWSFileLocationWrappersAll), 0, maxIndex));
    String[] webServiceNamesNotChanged = collectWebServiceNames(notChangedWSFileLocationWrappers);
    WSUpdateResult changedWSUpdateResult = updateWebServices(applicationName, webServicesDir, webServicesWorkingDir, wsUpdateInfo, webServiceNamesNotChanged, freeIndexes, maxIndex, appConfiguration);

    WSUpdateResult wsUpdateResultFinal = new WSUpdateResult();
    wsUpdateResultFinal.addWSUpdateResult(notChangedWSUpdateResult);
    wsUpdateResultFinal.addWSUpdateResult(changedWSUpdateResult);

    String deployedComponentsPerModuleFileName = WSDirsHandler.getDeployedComponestPerModuleFileName();
    String deployedComponentsPerModuleParentDir = WSDirsHandler.getDeployedComponentsPerModuleParentDir(webServicesDir);
    try {
      moduleFileStorageHandler.saveDeployedComponentsPerModule(webServicesDir, wsUpdateResultFinal.getDeployedWSPerModule());
      WSConfigurationHandler.uploadFile(deployedComponentsPerModuleParentDir, deployedComponentsPerModuleFileName, appConfiguration, WSDirsHandler.getWebServicesConfigName(WebServicesConstants.WS_CONTAINER_NAME) + "/" + WSDirsHandler.getAppJarsRelDir());
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Unable to save and/or upload " + WSDirsHandler.getDeployedComponestPerModuleFileName() + ". ";
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    return wsUpdateResultFinal;
  }

  private WSUpdateResult updateWebServices(String applicationName, String webServicesDir, String webServicesWorkingDir, WSUpdateInfo wsUpdateInfo, String[] webServiceNamesNotChanged, int[]freeIndexes, int maxIndex, Configuration appConfiguration) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to update web services for application " + applicationName + ". ";

    WSUpdateResult wsUpdateResultFinal = new WSUpdateResult();
    try {
      WSFileLocationWrapper[] wsForDeleteFileLocationWrappers = wsUpdateInfo.getWsForDeleteFileLocationWrappers();
      singleWSUpdateProcessor.deleteWebServices(wsForDeleteFileLocationWrappers, appConfiguration);

      WSUpdateResult wsUpdateResult = singleWSUpdateProcessor.updateWebServices(applicationName, webServicesDir, webServicesWorkingDir, wsUpdateInfo.getWsForUpdateArchiveLocationWrappers(), wsUpdateInfo.getWsForUpdateFileLocationWrappers(), appConfiguration);

      String[] webServiceNamesUpdated = collectWebServiceNames(collectWSFileLocationWrappers(wsUpdateInfo.getWsForUpdateFileLocationWrappers()));
      String[] additionalWebServiceNames = WSUtil.unifyStrings(new String[][]{webServiceNamesNotChanged, webServiceNamesUpdated});
      int[] freeIndexesDeleted = WSUtil.getIntArray(collectIndexSet(wsForDeleteFileLocationWrappers));
      int[] allFreeIndexes = WSUtil.unifyInts(new int[][]{freeIndexes, freeIndexesDeleted});
      WSDeployResult wsDeployResult = deployWebServices(applicationName, webServicesDir, webServicesWorkingDir, wsUpdateInfo.getWsForDeployArchiveLocationWrappers(), additionalWebServiceNames, allFreeIndexes, maxIndex, appConfiguration);


      wsUpdateResultFinal.addWSUpdateResult(wsDeployResult);
      wsUpdateResultFinal.addWSUpdateResult(wsUpdateResult);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    return wsUpdateResultFinal;
  }

  private WSUpdateInfo collectWSUpdateInfo(WSArchiveLocationWrapper[] newWSArchiveLocationWrappers, WSFileLocationWrapper[] oldWSFIFileLocationWrappers) {
    return collectWSUpdateInfo(collectWSLocationWrappersTable(newWSArchiveLocationWrappers), collectWSLocationWrappersTable(oldWSFIFileLocationWrappers));
  }

  public WSUpdateInfo collectWSUpdateInfo(Hashtable newWSArchiveLocationWrappers, Hashtable oldWSFileLocationWrappers) {
    Hashtable wsForDeploy = WSUtil.makeSubtraction(newWSArchiveLocationWrappers, oldWSFileLocationWrappers);
    Hashtable wsForDelete = WSUtil.makeSubtraction(oldWSFileLocationWrappers, newWSArchiveLocationWrappers);
    Hashtable wsForUpdateNewWrappers = WSUtil.makeIntersection(newWSArchiveLocationWrappers, oldWSFileLocationWrappers);
    Hashtable wsForUpdateOldWrappers = WSUtil.makeIntersection(oldWSFileLocationWrappers, newWSArchiveLocationWrappers);

    WSUpdateInfo wsUpdateInfo = new WSUpdateInfo();
    wsUpdateInfo.setWsForDeployArchiveLocationWrappers(collectWSArchiveLocationWrappers(wsForDeploy));
    wsUpdateInfo.setWsForDeleteFileLocationWrappers(collectWSFileLocationWrappers(wsForDelete));
    wsUpdateInfo.setWsForUpdateArchiveLocationWrappers(wsForUpdateNewWrappers);
    wsUpdateInfo.setWsForUpdateFileLocationWrappers(wsForUpdateOldWrappers);

    return wsUpdateInfo;
  }

  public Hashtable collectNotChangedModules(String webServicesDir, File[] moduleArchivesNew, Configuration appConfiguration, Properties notChangedDeployedWSPerModule) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to collect not updated modules. ";

    Properties deployedWSPerModuleOld = null;
    Hashtable moduleCrcOld = null;
    try {
      moduleCrcOld = WSConfigurationHandler.loadWSModuleCrcTable(appConfiguration);
      deployedWSPerModuleOld = loadDeployedComponentsPerModuleFile(webServicesDir);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    return collectNotChangedModules(moduleArchivesNew, moduleCrcOld, deployedWSPerModuleOld, notChangedDeployedWSPerModule);
  }

  public Hashtable collectNotChangedModules(File[] moduleArchivesNew, Hashtable moduleCrcOld, Properties deployedWSPerModule, Properties notChangedDeployedWSPerModule) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to collect not updated modules. ";

    if(moduleArchivesNew == null) {
      return new Hashtable();
    }

    Hashtable notChangedModules = new Hashtable();
    for(int i = 0; i < moduleArchivesNew.length; i++) {
      File moduleArchiveNew = moduleArchivesNew[i];
      String moduleName = moduleArchiveNew.getName();
      try {
        if(moduleCrcOld.containsKey(moduleName)) {
          byte[] oldCrc = (byte[])moduleCrcOld.get(moduleName);
          byte[] newCrc = (byte[])HashUtils.generateFileHash(moduleArchiveNew);
          if(HashUtils.compareHash(newCrc, oldCrc)) {
            notChangedDeployedWSPerModule.put(moduleName, deployedWSPerModule.getProperty(moduleName));
            notChangedModules.put(moduleName, moduleArchiveNew);
          }
        }
      } catch(IOException e) {
        Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
        wsDeployLocation.catching(e);

        String msg = excMsg + "Unable to process module " + moduleArchiveNew.getAbsolutePath() + ". ";
        Object[] args = new String[]{msg};
        throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
      }
    }

    return notChangedModules;
  }

  private WSDeployResult deployWebServices(String applicationName, String webServicesDir, String webServicesWorkingDir, WSArchiveLocationWrapper[] wsForDeployArchiveLocationWrappers, String[] webServiceNamesUpdated, int[] freeIndexes, int maxIndex, Configuration appConfiguration) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to generate web services deployment files for application " + applicationName + ". ";

    boolean areDirsDeleted = false;
    try {
      areDirsDeleted = IOUtil.deleteDirs(new String[]{webServicesWorkingDir});
    } catch(IOException e)  {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Unable to clear web services directories. ";
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args, e);
    }

    if(!areDirsDeleted) {
      String msg = excMsg + "Unable to clear web services directories (" + webServicesWorkingDir + ") + this may cause problems on web services start or update phase. ";
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args);
    }

    WSDeployResult wsDeployResult = generateDeployFiles(applicationName, webServicesDir, webServicesWorkingDir, wsForDeployArchiveLocationWrappers, webServiceNamesUpdated, freeIndexes, maxIndex);
    WSRuntimeDefinition[] wsRuntimeDefinitions = collectWSRuntimeDefinitions(wsDeployResult.getWsDeploymentInfoes());

    try {
      WSConfigurationHandler.makeWSConfigurations(collectWSDirs(wsRuntimeDefinitions), appConfiguration);
    } catch(WSConfigurationException e)  {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Unable to make web services configurations. ";
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args, e);
    }

    return wsDeployResult;
  }

  private WSDeployResult generateDeployFiles(String applicationName, String webServicesDir, String webServicesWorkingDir, WSArchiveLocationWrapper[] wsForDeployArchiveLocationWrappers, String[] webServiceNamesUpdated, int[] freeIndexes, int maxIndex) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to generate web services deployment files for application " + applicationName + ". ";

    WSDeploymentInfo[] wsDeploymentInfoes = null;
    try {
      wsDeploymentInfoes = wsDefinitionFactory.loadWebServices(webServicesWorkingDir, wsForDeployArchiveLocationWrappers);
      WSRuntimeDefinition[] wsRuntimeDefinitions = collectWSRuntimeDefinitions(wsDeploymentInfoes);
      String[] webServiceNamesNew = collectWebServiceNames(wsRuntimeDefinitions);
      new WSChecker().checkServiceNamesForDublicates(WSUtil.unifyStrings(new String[][]{webServiceNamesNew, webServiceNamesUpdated}));

      wsDeployGenerator.generateWSDeployFiles(webServicesDir, webServicesWorkingDir, wsDeploymentInfoes, freeIndexes, maxIndex);
    } catch(WSDeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg;
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args, e);
    }

    return singleWSUpdateProcessor.defineWSDeployResult(wsDeploymentInfoes);
  }

  private WSUpdateResult updateWebServices(String applicationName, String webServiceName, String webServicesWorkingDir, Hashtable wsForUpdateArchiveLocationWrappers, Hashtable wsForUpdateFileLocationWrappers, Configuration appConfiguration) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to update web services for application " + applicationName + ". ";

    int[] freeIndexes = WSUtil.getIntArray(collectIndexSet(collectWSFileLocationWrappers(wsForUpdateFileLocationWrappers)));
    WSDeployResult wsDeployResult = null;
    try {
      wsDeployResult = deployWebServices(applicationName, webServiceName, webServicesWorkingDir, collectWSArchiveLocationWrappers(wsForUpdateArchiveLocationWrappers), new String[0], freeIndexes, -1, appConfiguration);
    } catch(WSDeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    WSUpdateResult wsUpdateResult = new WSUpdateResult();
    wsUpdateResult.addWSUpdateResult(wsDeployResult);
    return wsUpdateResult;
  }

  private boolean hasModuleMappingsFile(String webServicesDir) {
    String moduleMappingsPath = WSDirsHandler.getModuleMappingsPath(webServicesDir);
    return new File(moduleMappingsPath).exists();
  }

  private Properties loadDeployedComponentsPerModuleFile(String webServicesDir) throws WSDeploymentException {
    Properties deployedComponentsPerModule = null;
    if(hasModuleMappingsFile(webServicesDir)) {
      deployedComponentsPerModule = moduleUpdateProcessor.loadProperties(WSDirsHandler.getDeployedComponentsPerModulePath(webServicesDir));
    } else {
      deployedComponentsPerModule = new Properties();
    }

    return deployedComponentsPerModule;
  }

  private WSArchiveLocationWrapper[] collectWSArchiveLocationWrappers(Hashtable wsArchiveLocationWrappers) {
    if(wsArchiveLocationWrappers == null) {
      return new WSArchiveLocationWrapper[0];
    }

    WSArchiveLocationWrapper[] wsArchiveLocationWrappersArr = new WSArchiveLocationWrapper[wsArchiveLocationWrappers.size()];
    Enumeration wsEnum = wsArchiveLocationWrappers.elements();
    int i = 0;
    while(wsEnum.hasMoreElements()) {
      wsArchiveLocationWrappersArr[i++] = (WSArchiveLocationWrapper)wsEnum.nextElement();
    }

    return wsArchiveLocationWrappersArr;
  }

  private WSFileLocationWrapper[] collectWSFileLocationWrappers(Hashtable wsFileLocationWrappers) {
    if(wsFileLocationWrappers == null) {
      return new WSFileLocationWrapper[0];
    }

    WSFileLocationWrapper[] wsFileLocationWrappersArr = new WSFileLocationWrapper[wsFileLocationWrappers.size()];
    Enumeration wsEnum = wsFileLocationWrappers.elements();
    int i = 0;
    while(wsEnum.hasMoreElements()) {
      wsFileLocationWrappersArr[i++] = (WSFileLocationWrapper)wsEnum.nextElement();
    }

    return wsFileLocationWrappersArr;
  }

  private WSRuntimeDefinition[] collectWSRuntimeDefinitions(WSDeploymentInfo[] wsDeploymentInfoes) {
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

  private String[] collectWSDirs(WSRuntimeDefinition[] wsRuntimeDefinitions) {
    if(wsRuntimeDefinitions == null) {
      return new String[0];
    }

    String[] wsDirs = new String[wsRuntimeDefinitions.length];
    for(int i = 0; i < wsRuntimeDefinitions.length; i++) {
      wsDirs[i] = wsRuntimeDefinitions[i].getWsDirsHandler().getWsDirectory();
    }

    return wsDirs;
  }

  private String[] collectWebServiceNames(WSRuntimeDefinition[] wsRuntimeDefinitions) {
    if(wsRuntimeDefinitions == null) {
      return new String[0];
    }

    String[] webServiceNames = new String[wsRuntimeDefinitions.length];
    for(int i = 0; i < wsRuntimeDefinitions.length; i++) {
      webServiceNames[i] = wsRuntimeDefinitions[i].getWSIdentifier().getServiceName();
    }

    return webServiceNames;
  }

  private String[] collectWebServiceNames(WSLocationWrapper[] wsLocationWrappers) {
    if(wsLocationWrappers == null) {
      return new String[0];
    }

    String[] wsNames = new String[wsLocationWrappers.length];
    for(int i = 0; i < wsLocationWrappers.length; i++) {
      wsNames[i] = wsLocationWrappers[i].getWebServiceName();
    }

    return wsNames;
  }

  private Hashtable collectWSLocationWrappersTable(WSLocationWrapper[] wsLocationWrappers) {
    if(wsLocationWrappers == null) {
      return new Hashtable();
    }

    Hashtable wsLocationWrappersTable = new Hashtable();
    for(int i = 0; i < wsLocationWrappers.length; i++) {
      WSLocationWrapper wsLocationWrapper = wsLocationWrappers[i];
      wsLocationWrappersTable.put(wsLocationWrapper.getWebServiceName(), wsLocationWrapper);
    }

    return wsLocationWrappersTable;
  }

  private Set collectIndexSet(WSFileLocationWrapper[] wsFileLocationWrappers) {
    if(wsFileLocationWrappers == null) {
      return new HashSet();
    }

    HashSet indexSet = new HashSet();
    for(int i = 0; i < wsFileLocationWrappers.length; i++) {
      int index = wsFileLocationWrappers[i].getWsDirsHandler().getIndex();
      if(index != -1) {
        indexSet.add(new Integer(index));
      }
    }

    return indexSet;
  }

  private File[] getModuleArchives(WSArchiveLocationWrapper[] wsArchiveLocationWrappers) {
    if(wsArchiveLocationWrappers == null) {
      return new File[0];
    }

    Vector moduleArchives = new Vector();
    for(int i = 0; i < wsArchiveLocationWrappers.length; i++) {
      moduleArchives.add(wsArchiveLocationWrappers[i].getWsArchiveFilesLocationHandler().getModuleArchive());
    }

    File[] moduleArchivesArr = new File[moduleArchives.size()];
    moduleArchives.copyInto(moduleArchivesArr);

    return moduleArchivesArr;
  }

  private WSFileLocationWrapper[] filterWSFileLocationWrappers(WSFileLocationWrapper[] wsFileLocationWrappers, Set wsNamesFilterList, boolean excludeFlag) {
    if(wsFileLocationWrappers == null) {
      return new WSFileLocationWrapper[0];
    }

    Vector wsFileLocationWrappersFiltered = new Vector();
    for(int i = 0; i < wsFileLocationWrappers.length; i++) {
      WSFileLocationWrapper wsFileLocationWrapper = wsFileLocationWrappers[i];
      if(wsNamesFilterList.contains(wsFileLocationWrapper.getWebServiceName())) {
        if(!excludeFlag) {
          wsFileLocationWrappersFiltered.add(wsFileLocationWrapper);
        }
      } else {
        if(excludeFlag) {
          wsFileLocationWrappersFiltered.add(wsFileLocationWrapper);
        }
      }
    }

    WSFileLocationWrapper[] wsFileLocationWrappersFilteredArr = new WSFileLocationWrapper[wsFileLocationWrappersFiltered.size()];
    wsFileLocationWrappersFiltered.copyInto(wsFileLocationWrappersFilteredArr);

    return wsFileLocationWrappersFilteredArr;
  }

}
