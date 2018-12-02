package com.sap.engine.services.webservices.webservices630.server.deploy.ws.update;

import com.sap.engine.services.deploy.container.ApplicationDeployInfo;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.deploy.container.WarningException;
import com.sap.engine.services.webservices.server.deploy.ws.*;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.webservices630.server.deploy.WSConfigurationHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.common.ModuleDeployGenerator;
import com.sap.engine.services.webservices.webservices630.server.deploy.common.ModuleFileStorageHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.common.update.ModuleUpdateProcessor;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.*;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.preprocess.WSFileLocationWrapper;
import com.sap.engine.services.webservices.runtime.definition.WSRuntimeDefinition;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.exceptions.WSConfigurationException;
import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.tc.logging.Location;

import java.io.File;
import java.util.Properties;
import java.util.Vector;
import java.util.HashSet;

/**
 * Title: WSUpdateManager
 * Description: The class manages web services update phases.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */
                                                                          
public class WSUpdateManager {

  private WSUpdateProcessor wsUpdateProcessor = null;
  private WSDeployManager wsDeployManager = null;

  public WSUpdateManager(WSDeployManager wsDeployManager, WSDeployGenerator wsDeployGenerator, ModuleDeployGenerator moduleDeployGenerator, ModuleFileStorageHandler moduleFileStorageHandler) {
    this.wsUpdateProcessor = new WSUpdateProcessor(wsDeployGenerator, new ModuleUpdateProcessor(moduleDeployGenerator, moduleFileStorageHandler), moduleFileStorageHandler);
    this.wsDeployManager = wsDeployManager;
  }

  public WSAppUpdateResult makeUpdate(String applicationName, String webServicesDir, String webServicesWorkingDir, File[] moduleArchives, Configuration appConfiguration) throws DeploymentException {
    String excMsg = "Error occurred, trying to update web services for application " + applicationName + ". ";

    WSAppUpdateResult wsAppUpdateResult = wsUpdateProcessor.updateWebServices(applicationName, webServicesDir, webServicesWorkingDir, moduleArchives, appConfiguration);

    WSUpdateResult wsUpdateResult = wsAppUpdateResult.getWsUpdateResult();
     try {
      if(wsUpdateResult.getDeployedComponentNames() == null || wsUpdateResult.getDeployedComponentNames().length == 0) {
        if(WSConfigurationHandler.existsSubConfiguration(appConfiguration, WSDirsHandler.getWebServicesConfigName(WebServicesConstants.WS_CONTAINER_NAME))) {
          WSConfigurationHandler.deleteConfiguration(appConfiguration, WSDirsHandler.getWebServicesConfigName(WebServicesConstants.WS_CONTAINER_NAME), "/");
        }
        wsDeployManager.removeWebServicesDir(applicationName);
      }
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + " Unable to delete " + WSDirsHandler.getWebServicesConfigName(WebServicesConstants.WS_CONTAINER_NAME) + " configuration. ";
      Object[] args = new String[]{msg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    try {
      notifyProtocolsOnUpdate(applicationName, wsAppUpdateResult.getWsUpdateResult(), appConfiguration);
    } catch (WSWarningException e) {
      wsAppUpdateResult.addWarnings(e.getWarningsVector());
    }

    return wsAppUpdateResult;
  }

  public void notifyUpdatedComponents(String applicationName) throws WarningException {
    wsDeployManager.removeWebServicesDir(applicationName);
  }

  public void prepareUpdate(String applicationName) throws DeploymentException, WarningException {
    notifyProtocolsOnPostUpdate(applicationName);
  }

  public ApplicationDeployInfo commitUpdate(String applicationName) throws WarningException {
    notifyProtocolsOnCommitUpdate(applicationName);
    return null;
  }

  public void rollbackUpdate(String applicationName, Configuration appConfiguration, Properties props) throws WarningException {
    Vector warnings = new Vector();

    try {
      wsDeployManager.removeWebServicesDir(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      notifyProtocolsOnRollbackUpdate(applicationName, appConfiguration);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);

      throw e;
    }
  }

  private void notifyProtocolsOnUpdate(String applicationName, WSUpdateResult wsUpdateResult, Configuration appConfiguration) throws WSWarningException {
    String excMsg = "Error occurred, trying to notify protocols on " + WSProtocolNotificator.getModeName(WSProtocolNotificator.UPDATE) + " phase, application " + applicationName + ". ";

    WSRuntimeDefinition[] wsRuntimeDefinitions = wsUpdateResult.getWSRuntimeDefinitions();
    WSRuntimeDefinition[] wsRuntimeDefinitionsNotChanged = null;
    try {
      wsRuntimeDefinitionsNotChanged = loadWebServices(wsUpdateResult.getWsFileLocationWrappers(), appConfiguration);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching("Warning! ", e);

      WSWarningException wExc = new WSWarningException();
      String msg = excMsg + e.getLocalizedMessage() + ". ";
      wExc.addWarning(msg);
      throw wExc;
    }

    WSRuntimeDefinition[] allWSRuntimeDefinitions = WebServicesUtil.unifyWSRuntimeDefinitiones(new WSRuntimeDefinition[][]{wsRuntimeDefinitions, wsRuntimeDefinitionsNotChanged});
    notifyProtocolsOnUpdate(applicationName, allWSRuntimeDefinitions, appConfiguration);
  }

  private void notifyProtocolsOnUpdate(String applicationName, WSRuntimeDefinition[] wsRuntimeDefinitions, Configuration appConfiguration) throws WSWarningException {
    String excMsg = "Error occurred, trying to notify protocols on " + WSProtocolNotificator.getModeName(WSProtocolNotificator.UPDATE) + " phase, application " + applicationName + ". ";

    Vector warnings = new Vector();
    try {
      wsDeployManager.notifyProtocolsOnRemove(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(WSUtil.addPrefixToStrings(excMsg, e.getWarningsVector()));
    }

    try {
      wsDeployManager.notifyProtocolsOnDeploy(applicationName, wsRuntimeDefinitions, appConfiguration);
    } catch(WSWarningException e) {
      warnings.addAll(WSUtil.addPrefixToStrings(excMsg, e.getWarningsVector()));
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  private void notifyProtocolsOnPostUpdate(String applicationName) throws WSWarningException {
    String excMsg = "Error occurred, trying to notify protocols on " + WSProtocolNotificator.getModeName(WSProtocolNotificator.POST_UPDATE) + " phase, applicationName " + applicationName + ". ";

    Vector warnings = new Vector();
    try {
      wsDeployManager.notifyProtocolsOnPostDeploy(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(WSUtil.addPrefixToStrings(excMsg, e.getWarningsVector()));
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  private void notifyProtocolsOnCommitUpdate(String applicationName) throws WSWarningException {
    String excMsg = "Error occurred, trying to notify protocols on " + WSProtocolNotificator.getModeName(WSProtocolNotificator.COMMIT_UPDATE) + " phase, applicationName " + applicationName + ". ";

    Vector warnings = new Vector();
    try {
      wsDeployManager.notifyProtocolsOnCommitDeploy(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(WSUtil.addPrefixToStrings(excMsg, e.getWarningsVector()));
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  private void notifyProtocolsOnRollbackUpdate(String applicationName, Configuration appConfiguration) throws WSWarningException {
    String excMsg = "Error occurred, trying to notify protocols on rollback update for application " + applicationName + ". ";

    Vector warnings = new Vector();
    try {
      wsDeployManager.notifyProtocolsOnRollbackDeploy(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(WSUtil.addPrefixToStrings(excMsg, e.getWarningsVector()));
    }

    String webServicesDir = null;
    try {
      String wsContainerDir = WSContainer.getWSDeployer().getWSContainerDir(applicationName, WebServicesConstants.WS_INTERNAL_COMPONENT);
      webServicesDir = WSDirsHandler.getWebServicesDir(wsContainerDir);
      IOUtil.deleteDir(webServicesDir);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching("Warning! ", e);

      String msg = excMsg + "Unable to delete" + WebServicesConstants.WEBSERVICES_CONFIG_NAME + " directory. ";
      WSWarningException wExc = new WSWarningException();
      wExc.addWarnings(warnings);
      wExc.addWarning(msg);
      throw wExc;
    }

    try {
      (new WSRuntimeActivator()).downloadWSFiles(applicationName, appConfiguration);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching("Warning! ", e);

      String msg = excMsg + e.getLocalizedMessage();
      WSWarningException wExc = new WSWarningException();
      wExc.addWarnings(warnings);
      wExc.addWarning(msg);
      throw wExc;
    }


    WSRuntimeDefinition[] wsRuntimeDefinitions = null;
    try {
      wsRuntimeDefinitions = loadWebServices(applicationName, webServicesDir);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching("Warning! ", e);

      String msg = excMsg + e.getLocalizedMessage();
      WSWarningException wExc = new WSWarningException();
      wExc.addWarnings(warnings);
      wExc.addWarning(msg);

      throw wExc;
    }

    try {
      wsDeployManager.notifyProtocolsOnDeploy(applicationName, wsRuntimeDefinitions, appConfiguration);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      wsDeployManager.notifyProtocolsOnPostDeploy(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      wsDeployManager.notifyProtocolsOnCommitDeploy(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    if (warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }


  private WSRuntimeDefinition[] loadWebServices(String applicationName, String webServicesDir) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to load web services from directory " + webServicesDir + ", application " + applicationName + ". ";

    File appWSDeployDir = new File(webServicesDir);

    if (!appWSDeployDir.exists()) {
      return new WSRuntimeDefinition[0];
    }

    WSRuntimeDefinition[] wsRuntimeDefinitions = null;
    try {
      wsRuntimeDefinitions = loadWebServices(applicationName, appWSDeployDir.listFiles());
    } catch(WSDeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    return wsRuntimeDefinitions;
  }

  private WSRuntimeDefinition[] loadWebServices(String aplicationName, File[] wsDirs) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to load web services from the local file system, application " + aplicationName + ". ";

    if(wsDirs == null) {
      return new WSRuntimeDefinition[0];
    }

    Vector wsRuntimeDefinitions = new Vector();
    try {
      for (int i = 0; i < wsDirs.length; i++) {
        File wsDir = wsDirs[i];
        if (wsDir.getName().equals(WebServicesConstants.APP_JARS_NAME)) {
          continue;
        } else {
          WSRuntimeDefinition wsRuntimeDefinition = WSRuntimeActivator.loadWebService(aplicationName, wsDir.getAbsolutePath());
          wsRuntimeDefinitions.add(wsRuntimeDefinition);
        }
      }
    } catch(WSDeploymentException e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{excMsg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    WSRuntimeDefinition[] wsRuntimeDefinitionsArray = new WSRuntimeDefinition[wsRuntimeDefinitions.size()];
    wsRuntimeDefinitions.copyInto(wsRuntimeDefinitionsArray);

    return wsRuntimeDefinitionsArray;
  }

  private WSRuntimeDefinition[] loadWebServices(WSFileLocationWrapper[] wsFileLocationWrappers, Configuration appConfiguration) throws WSDeploymentException {
    if(wsFileLocationWrappers == null) {
      return new WSRuntimeDefinition[0];
    }

    WSRuntimeDefinition[] wsRuntimeDefinitions = new WSRuntimeDefinition[wsFileLocationWrappers.length];
    for(int i = 0; i < wsFileLocationWrappers.length; i++) {
      wsRuntimeDefinitions[i] = loadWebService(wsFileLocationWrappers[i], appConfiguration);
    }

    return wsRuntimeDefinitions;
  }

  private WSRuntimeDefinition loadWebService(WSFileLocationWrapper wsFileLocationWrapper, Configuration appConfiguration) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to load web service " + wsFileLocationWrapper.getWebServiceName() + ", application " + wsFileLocationWrapper.getApplicationName() + ". ";

    String wsDir = wsFileLocationWrapper.getWsDirsHandler().getWsDirectory();
    try {
      WSDirsHandler wsDirsHandler = wsFileLocationWrapper.getWsDirsHandler();
      WSConfigurationHandler.downloadConfiguration(wsDirsHandler.getDescriptorsDir(), wsDirsHandler.getWSConfigPath(WebServicesConstants.WS_CONTAINER_NAME) + WebServicesConstants.SEPARATOR + wsDirsHandler.getDescriptorsRelDir(), appConfiguration, new HashSet());
      //WSConfigurationHandler.downloadWSConfiguration(wsDir, wsFileLocationWrapper.getWsDirsHandler().getWSConfigName(), appConfiguration);
    } catch(WSConfigurationException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    return WSRuntimeActivator.loadWebService(wsFileLocationWrapper.getApplicationName(), wsDir);
  }

  private String[] collectWSConfigNames(WSFileLocationWrapper[] wsFileLocationWrappers) {
    if(wsFileLocationWrappers == null) {
      return new String[0];
    }

    String[] wsConfigNames = new String[wsFileLocationWrappers.length];
    for(int i = 0; i < wsFileLocationWrappers.length; i++) {
      wsConfigNames[i] = wsFileLocationWrappers[i].getWsDirsHandler().getWSConfigName();
    }

    return wsConfigNames;
  }

  private String[] collectWSDirs(WSFileLocationWrapper[] wsFileLocationWrappers) {
    if(wsFileLocationWrappers == null) {
      return new String[0];
    }

    String[] wsDirs = new String[wsFileLocationWrappers.length];
    for(int i = 0; i < wsFileLocationWrappers.length; i++) {
      wsDirs[i] = wsFileLocationWrappers[i].getWsDirsHandler().getWsDirectory();
    }

    return wsDirs;
  }

}
