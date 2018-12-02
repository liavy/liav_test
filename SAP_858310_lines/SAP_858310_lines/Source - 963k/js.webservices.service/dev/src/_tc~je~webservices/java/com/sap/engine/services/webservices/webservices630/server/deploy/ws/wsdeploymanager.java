package com.sap.engine.services.webservices.webservices630.server.deploy.ws;

import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.webservices630.server.deploy.WSConfigurationHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.runtime.definition.*;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.exceptions.WSConfigurationException;
import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.tc.logging.Location;

import java.util.*;
import java.io.File;

/**
 * Title: WSDeployManager
 * Description: The class manages web services deploy and remove phases.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSDeployManager {

  private WSBaseGlobalContext deployContext = new WSBaseGlobalContext();
  private WSDeployGenerator wsDeployGenerator = null;

  public WSDeployManager(WSDeployGenerator wsDeployGenerator) {
    this.wsDeployGenerator = wsDeployGenerator;
  }

  public WSBaseGlobalContext getDeployContext() {
    return deployContext;
  }

  public WSAppDeployResult deploy(String applicationName, String wsContainerDir, String wsContainerWorkingDir, File[] moduleArchives, Configuration appConfiguration) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to deploy web services for application "  + applicationName + ". ";

    String webServicesDir = WSDirsHandler.getWebServicesDir(wsContainerDir);
    String webServicesWorkingDir = WSDirsHandler.getWebServicesWorkingDir(wsContainerWorkingDir);


    WSAppDeployResult wsAppDeployResult = wsDeployGenerator.generateDeployFiles(applicationName, webServicesDir, webServicesWorkingDir, moduleArchives);

    try {
      if(wsAppDeployResult.getDeployedComponentNames() != null && wsAppDeployResult.getDeployedComponentNames().length != 0) {
        WSConfigurationHandler.makeWebServicesConfiguration(webServicesDir, wsAppDeployResult.getModuleCrcTable(), appConfiguration);
      }
    } catch (WSConfigurationException e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{excMsg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    try {
      notifyProtocolsOnDeploy(applicationName, wsAppDeployResult.getWSRuntimeDefinitions(), appConfiguration);
    } catch(WSWarningException e) {
      wsAppDeployResult.addWarnings(e.getWarningsVector());
    }

    return wsAppDeployResult;
  }

  public void postDeploy(String applicationName) throws WSWarningException {
    notifyProtocolsOnPostDeploy(applicationName);
  }

  public void commitDeploy(String applicationName) throws WSWarningException {
    notifyProtocolsOnCommitDeploy(applicationName);
  }

  public void rollbackDeploy(String applicationName) throws WSWarningException {
    Vector warnings = new Vector();

    try {
      removeWebServicesDir(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      notifyProtocolsOnRollbackDeploy(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void remove(String applicationName) throws WSWarningException {
    Vector warnings = new Vector();

    try {
      notifyProtocolsOnRemove(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      removeWebServicesDir(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void notifyProtocolsOnDeploy(String applicationName, WSRuntimeDefinition[] wsRuntimeDefinitions, Configuration appConfiguration) throws WSWarningException {    
    deployContext.putContext(applicationName, new WSBaseContext(applicationName, wsRuntimeDefinitions, appConfiguration));
    notifyProtocolsOnDeployPhases(applicationName, wsRuntimeDefinitions, appConfiguration, WSProtocolNotificator.DEPLOY);    
  }

  public void notifyProtocolsOnPostDeploy(String applicationName) throws WSWarningException {
    WSBaseContext wsBaseContext = deployContext.getContext(applicationName);
    if(wsBaseContext == null) {
      return;
    }

    notifyProtocolsOnDeployPhases(applicationName, wsBaseContext.getWsRuntimeDefinitions(), wsBaseContext.getAppConfiguration(), WSProtocolNotificator.POST_DEPLOY);
  }

  public void notifyProtocolsOnCommitDeploy(String applicationName) throws WSWarningException {
    notifyProtocolsOnShortPhases(applicationName, WSProtocolNotificator.COMMIT_DEPLOY);
  }

  public void notifyProtocolsOnRollbackDeploy(String applicationName) throws WSWarningException {
    Vector warnings = new Vector();

    try {
      notifyProtocolsOnShortPhases(applicationName, WSProtocolNotificator.ROLLBACK_DEPLOY);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      removeWebServicesDir(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void notifyProtocolsOnRemove(String applicationName) throws WSWarningException {
    new WSProtocolNotificator().onRemove(applicationName);
  }

  private void notifyProtocolsOnDeployPhases(String applicationName, WSRuntimeDefinition[] wsRuntimeDefinitions, Configuration appConfiguration, int mode) throws  WSWarningException {
    switch (mode) {
      case WSProtocolNotificator.DEPLOY: {
        new WSProtocolNotificator().onDeploy(applicationName, wsRuntimeDefinitions, appConfiguration);
        break;
      }
      case WSProtocolNotificator.POST_DEPLOY: {
        new WSProtocolNotificator().onPostDeploy(applicationName, wsRuntimeDefinitions, appConfiguration);
        break;
      }
    }
  }

  private void notifyProtocolsOnShortPhases(String applicationName, int mode) throws WSWarningException {
    WSBaseContext wsBaseContext = deployContext.getContext(applicationName);
    if(wsBaseContext == null) {
      return;
    }

    switch (mode) {
      case WSProtocolNotificator.COMMIT_DEPLOY: {
        new WSProtocolNotificator().onCommitDeploy(wsBaseContext.getWsRuntimeDefinitions());
        break;
      }
      case WSProtocolNotificator.ROLLBACK_DEPLOY: {
        new WSProtocolNotificator().onRollbackDeploy(wsBaseContext.getWsRuntimeDefinitions());
        break;
      }
    }

    deployContext.removeContext(applicationName);
  }

  public void removeWebServicesDir(String applicationName) throws WSWarningException {
    String excMsg = "Error occurred, deleting " + WebServicesConstants.WEBSERVICES_CONFIG_NAME + " directory. ";

    String webServicesDir = null;
    boolean isWebServicesDirDeleted = false;
    try {
      String wsContainerDir = WSContainer.getWSDeployer().getWSContainerDir(applicationName, WebServicesConstants.WS_INTERNAL_COMPONENT);;
      webServicesDir = WSDirsHandler.getWebServicesDir(wsContainerDir);
      isWebServicesDirDeleted = IOUtil.deleteDir(webServicesDir);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = null;
      if(webServicesDir != null) {
        msg = excMsg + "Unable to delete" + webServicesDir + " directory. ";
      } else {
        msg = excMsg + "Unable to delete" + WebServicesConstants.WEBSERVICES_CONFIG_NAME + " directory. ";
      }

      msg += "Reason is: " + e.getLocalizedMessage();

      WSWarningException wExc = new WSWarningException();
      wExc.addWarning(msg);
      throw wExc;
    }

    if(!isWebServicesDirDeleted) {
      String msg = null;
      if(webServicesDir != null) {
        msg = excMsg + "Unable to delete" + webServicesDir + " directory. ";
      } else {
        msg = excMsg + "Unable to delete" + WebServicesConstants.WEBSERVICES_CONFIG_NAME + " directory. ";
      }

      WSWarningException wExc = new WSWarningException();
      wExc.addWarning(msg);
      throw wExc;
    }
  }

}
