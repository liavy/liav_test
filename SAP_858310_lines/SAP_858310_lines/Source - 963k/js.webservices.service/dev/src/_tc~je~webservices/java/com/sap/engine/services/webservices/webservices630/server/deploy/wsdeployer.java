package com.sap.engine.services.webservices.webservices630.server.deploy;

import com.sap.engine.frame.container.monitor.SystemMonitor;
import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.load.LoadContext;
import com.sap.engine.interfaces.webservices.server.WSContainerInterface;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.services.deploy.container.*;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.server.WebServicesFrame;
import com.sap.engine.services.webservices.server.WSContainerInterfaceImpl;
import com.sap.engine.services.webservices.server.deploy.WebServicesDeployManager;
import com.sap.engine.services.webservices.webservices630.server.deploy.common.*;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.*;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.update.WSAppUpdateResult;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.update.WSUpdateManager;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.*;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.deploy.WSClientsAppDeployResult;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.update.*;
import com.sap.engine.services.webservices.exceptions.*;
import com.sap.tc.logging.Location;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.*;
import java.util.*;

/**
 * Title: WSDeployer
 * Description: Implementation of ContainerInterface for WebServices service.
 *              Two element types can be deployed:
 *              - web services
 *              - ws clients
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSDeployer implements ContainerInterface {

  public static final int PRIORITY = 77;

  private DeployCommunicator deployCommunicator = null;
  private String serviceWorkingDir = null;

  private WSDeployManager wsDeployManager = null;
  private WSRuntimeActivator wsRuntimeActivator = null;

  private WSClientsDeployManager wsClientsDeployManager = null;
  private WSClientsRuntimeActivator wsClientsRuntimeActivator = null;

  private WSUpdateManager wsUpdateManager = null;
  private com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.update.WSClientsUpdateManager wsClientsUpdateManager = null;

  public WSDeployer() {
    ModuleDeployGenerator moduleDeployGenerator = new ModuleDeployGenerator();
    ModuleFileStorageHandler moduleFileStorageHandler = new ModuleFileStorageHandler();

    WSServerDeploySettingsProvider wsServerDeploySettingsProvider = new WSServerDeploySettingsProvider();
    WSDeployGenerator wsDeployGenerator = new WSDeployGenerator(wsServerDeploySettingsProvider, wsServerDeploySettingsProvider, moduleDeployGenerator, moduleFileStorageHandler);
    this.wsDeployManager = new WSDeployManager(wsDeployGenerator);
    this.wsRuntimeActivator = new WSRuntimeActivator();

    WSClientsDeployGenerator wsClientsDeployGenerator = new WSClientsDeployGenerator(moduleDeployGenerator, moduleFileStorageHandler);
    this.wsClientsDeployManager = new WSClientsDeployManager(wsClientsDeployGenerator);
    this.wsClientsRuntimeActivator = new WSClientsRuntimeActivator();

    this.wsUpdateManager = new WSUpdateManager(wsDeployManager, wsDeployGenerator, moduleDeployGenerator, moduleFileStorageHandler);
    this.wsClientsUpdateManager = new com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.update.WSClientsUpdateManager(wsClientsDeployManager, wsClientsDeployGenerator, moduleDeployGenerator, moduleFileStorageHandler);

    this.serviceWorkingDir = WSContainer.getServiceContext().getServiceState().getWorkingDirectoryName().replace('\\', WSBaseConstants.SEPARATOR);
  }

  public void setDeployCommunicator(DeployCommunicator deployCommunicator) {
    this.deployCommunicator = deployCommunicator;
  }

  public DeployCommunicator getDeployCommunicator() {
    return deployCommunicator;
  }

  public ContainerInfo getContainerInfo() {
    ContainerInfo info = new ContainerInfo();
    info.setJ2EEContainer(false);
    info.setFileExtensions(new String[]{".jar", ".par", ".wsar", ".war"});
    info.setName(WSBaseConstants.WS_CONTAINER_NAME);
    info.setServiceName(WSContainer.NAME);
    info.setModuleName(WSContainer.NAME);
    info.setPriority(PRIORITY);
    return info;
  }

  public String getApplicationName(File standaloneFile) throws DeploymentException {
    return "wsar" + System.currentTimeMillis();
  }

  public ApplicationDeployInfo deploy(File[] archiveFiles, ContainerDeploymentInfo dInfo, Properties props) throws DeploymentException {
    String applicationName = dInfo.getApplicationName();
    String excMsg = "Error occurred, trying to deploy web services and/or ws clients for application " + applicationName + ". ";

    String wsContainerDir = null;
    try {
      wsContainerDir = getWSContainerDir(applicationName, WSBaseConstants.WS_INTERNAL_COMPONENT);
    } catch(IOException e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      String msg = excMsg + "Unable to get deploying directory for " + WebServicesConstants.WS_CONTAINER_NAME + " container. ";
      Object[] args = new Object[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    String wsContainerWorkingDir = getWSContainerWorkingDir(applicationName);

    ApplicationDeployInfo applicationDeployInfo = null;
    try {
      WSAppDeployResult wsAppDeployResult = wsDeployManager.deploy(applicationName, wsContainerDir, wsContainerWorkingDir, archiveFiles, dInfo.getConfiguration());
      WSClientsAppDeployResult wsClientsAppDeployResult = wsClientsDeployManager.deploy(wsContainerDir, wsContainerWorkingDir, getAppDeployInfo(dInfo), archiveFiles);

      applicationDeployInfo = defineApplicationDeployInfo(wsAppDeployResult, wsClientsAppDeployResult);
    } finally {
      try {
        IOUtil.deleteDir(wsContainerWorkingDir);
      } catch (IOException e) {
        Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
        String msg = "Warning! Unable to delete ws temporary directory, used for deploy needs: " +  wsContainerWorkingDir + ". " ;
        wsLocation.catching(msg, e);
      }
    }

    return applicationDeployInfo;
  }

  public void notifyDeployedComponents(String applicationName, Properties props)
			throws WarningException {
  }

  public void prepareDeploy(String applicationName, Configuration appConfig) throws DeploymentException, WarningException {
    Vector warnings = new Vector();

    try {
      wsDeployManager.postDeploy(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      wsClientsDeployManager.postDeploy(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    if (warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void commitDeploy(String applicationName) throws WarningException {
    Vector warnings = new Vector();

    try {
      wsDeployManager.commitDeploy(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      wsClientsDeployManager.commitDeploy(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    if (warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void rollbackDeploy(String applicationName) throws WarningException {
    Vector warnings = new Vector();

    try {
      wsDeployManager.rollbackDeploy(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      String wsContainerDir = getWSContainerDir(applicationName, WSBaseConstants.WS_INTERNAL_COMPONENT);
      wsClientsDeployManager.rollbackDeploy(applicationName, wsContainerDir);    
    } catch (WSWarningException e) {      
      warnings.addAll(e.getWarningsVector());
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);
      
      String msg = "Error occurred during ws clients rollback deploy for application " + applicationName + ". ";      
      warnings.add(msg);       
    }  

    if (warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public boolean needUpdate(File[] archiveFiles, ContainerDeploymentInfo dInfo, Properties props) throws DeploymentException, WarningException {
    return true;
  }

  public boolean needStopOnUpdate(File[] archiveFiles, ContainerDeploymentInfo dInfo, Properties props)
      throws DeploymentException, WarningException {
    return true;
  }

  public ApplicationDeployInfo makeUpdate(File[] archiveFiles, ContainerDeploymentInfo dInfo, Properties props) throws DeploymentException {
    String applicationName = dInfo.getApplicationName();
    String excMsg = "Error occurred during update of application " + applicationName + ". ";
    Configuration appConfiguration = dInfo.getConfiguration();

    String wsContainerDir = null;
    try {
      wsContainerDir = getWSContainerDir(applicationName, WSBaseConstants.WS_INTERNAL_COMPONENT);
    } catch (IOException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      String msg = excMsg + "Can not resolve application deploy directory for " + WSBaseConstants.WS_CONTAINER_NAME + " container. ";
      wsDeployLocation.catching(msg, e);

      Object[] args =  new Object[]{msg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    String wsContainerWorkingDir = getWSContainerWorkingDir(applicationName);

    ApplicationDeployInfo appDeployInfo = null;
    try {
      WSAppUpdateResult wsAppUpdateResult = wsUpdateManager.makeUpdate(applicationName, WSDirsHandler.getWebServicesDir(wsContainerDir), WSDirsHandler.getWebServicesWorkingDir(wsContainerWorkingDir), archiveFiles, appConfiguration);
      WSClientsAppUpdateResult wsClientsAppDeployResult = wsClientsUpdateManager.makeUpdate(WSClientDirsHandler.getWSClientsDir(wsContainerDir), WSClientDirsHandler.getWSClientsWorkingDir(wsContainerWorkingDir), archiveFiles, getAppDeployInfo(dInfo));

      appDeployInfo = defineApplicationDeployInfo(wsAppUpdateResult, wsClientsAppDeployResult);
    } finally {
      try {
        IOUtil.deleteDir(wsContainerWorkingDir);
      } catch (IOException e) {
        Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
        String msg = "Warning! Unable to delete ws temporary directory, used for deploy needs: " +  wsContainerWorkingDir + ". " ;
        wsLocation.catching(msg, e);
      }
    }

    try {
      if(appDeployInfo.getDeployedComponentNames() == null || appDeployInfo.getDeployedComponentNames().length == 0) {
        if(WSConfigurationHandler.existsSubConfiguration(appConfiguration, WSBaseConstants.WS_CONTAINER_NAME)) {
          WSConfigurationHandler.deleteConfiguration(appConfiguration, WSBaseConstants.WS_CONTAINER_NAME, "/");
        }
        removeWSContainerDir(applicationName);
      }

    } catch (Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      String msg = excMsg + "Can not remove " + WSBaseConstants.WS_CONTAINER_NAME + "'s container DB configuration or file system directory. ";
      wsDeployLocation.catching(msg, e);

      Object[] args =  new Object[]{msg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    return appDeployInfo;
  }

  public void notifyUpdatedComponents(String applicationName, Configuration applicationConfig, Properties props) throws WarningException {
    Vector warnings = new Vector();

    try {
      wsUpdateManager.notifyUpdatedComponents(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      String wsContainerDir = getWSContainerDir(applicationName, WSBaseConstants.WS_INTERNAL_COMPONENT);
      wsClientsUpdateManager.notifyUpdatedComponents(applicationName, wsContainerDir);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);
      
      String msg = "Error occurred during ws clients notify update for application " + applicationName + ". ";      
      warnings.add(msg);       
    }

    try {
      removeWSContainerDir(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void prepareUpdate(String applicationName) throws DeploymentException, WarningException {
    Vector warnings = new Vector();

    try {
      wsUpdateManager.prepareUpdate(applicationName);
    } catch(WSWarningException e)
    {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      wsClientsUpdateManager.prepareUpdate(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public ApplicationDeployInfo commitUpdate(String applicationName) throws WarningException {
    Vector warnings = new Vector();

    try {
      wsUpdateManager.commitUpdate(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      wsClientsUpdateManager.commitUpdate(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }

    return null;
  }

  public void rollbackUpdate(String applicationName, Configuration applicationConfig, Properties props) throws WarningException {
    String excMsg = "Error occurred, trying to rollback update of web services and/or ws clients files for application " + applicationName + ". ";
    
    String webServicesContainerDir = null;
    try {
      webServicesContainerDir = getWSContainerDir(applicationName, WSBaseConstants.WS_INTERNAL_COMPONENT);
    } catch(IOException e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);
  
      String msg = excMsg + "Unable to get deploying directory for " + WebServicesConstants.WS_CONTAINER_NAME + " container. ";
      Object[] args = new Object[]{msg, "none"};
      throw new WSWarningException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }
    
    Vector warnings = new Vector();
    try {
      wsUpdateManager.rollbackUpdate(applicationName, applicationConfig, props);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      wsClientsUpdateManager.rollbackUpdate(applicationName, webServicesContainerDir, applicationConfig, props, true);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void remove(String applicationName) throws DeploymentException, WarningException {
    Vector warnings = new Vector();

    try {
      wsDeployManager.remove(applicationName);
    } catch (WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    } 
    
    try {
      String wsContainerDir = getWSContainerDir(applicationName, WSBaseConstants.WS_INTERNAL_COMPONENT);
      wsClientsDeployManager.remove(applicationName, wsContainerDir);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);
             
      String msg = "Error occurred during ws clients remove for application " + applicationName + ". ";      
      warnings.add(msg);        
    } 

    try {
      removeWSContainerDir(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    if (warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void downloadApplicationFiles(String applicationName, Configuration configuration) throws DeploymentException, WSWarningException {
    String excMsg = "Error occurred, trying to download web services and/or ws clients files for application " + applicationName + ". ";
    
    String webServicesContainerDir = null;
    try {
      webServicesContainerDir = getWSContainerDir(applicationName, WSBaseConstants.WS_INTERNAL_COMPONENT);
    } catch(IOException e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      String msg = excMsg + "Unable to get deploying directory for " + WebServicesConstants.WS_CONTAINER_NAME + " container. ";
      Object[] args = new Object[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }
    
    Vector warnings = new Vector();
    try {
      wsRuntimeActivator.downloadApplicationFiles(applicationName, configuration);
    } catch (WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      wsClientsRuntimeActivator.downloadApplicationFiles(applicationName, webServicesContainerDir,  configuration);
    } catch (WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    if (warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void prepareStart(String applicationName, Configuration configuration) throws DeploymentException, WarningException {
    String excMsg = "Unable to start web services and/or wsclients for application " + applicationName + ". ";

    String wsContainerDir = null;
    try {
      wsContainerDir = getWSContainerDir(applicationName, WSBaseConstants.WS_INTERNAL_COMPONENT);
    } catch (IOException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      String msg = excMsg + "Can not resolve application deploy directory for " + WSBaseConstants.WS_CONTAINER_NAME + " container. ";
      wsDeployLocation.catching(msg, e);

      Object[] args =  new Object[]{msg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    Vector warnings = new Vector();
    try {
      wsRuntimeActivator.start(applicationName, WSDirsHandler.getWebServicesDir(wsContainerDir), configuration);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      wsClientsRuntimeActivator.startWSClients(applicationName, WSClientDirsHandler.getWSClientsDir(wsContainerDir), configuration);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    if (warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void commitStart(String applicationName) throws WarningException {
    Vector warnings = new Vector();

    try {
      wsRuntimeActivator.commitStart(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      wsClientsRuntimeActivator.commitStart(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }
   
//    try {
//      if(WSContainer.getWSModelController() != null)
//          WSContainer.getWSModelController().registerModelsForApplication(applicationName);
//    } catch(Exception e) {
//      Location.getLocation(com.sap.engine.services.webservices.webservices630.server.deploy.WSDeployer.class).traceThrowableT(500, "Warning", e);
//      warnings.add("Warning: " + e.getLocalizedMessage());
//    }

    if (warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void rollbackStart(String applicationName) throws WSWarningException {
    Vector warnings = new Vector();

    try {
      wsRuntimeActivator.rollbackStart(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      wsClientsRuntimeActivator.rollbackStart(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      removeWSContainerDir(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    if (warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void prepareStop(String applicationName, Configuration appConfiguration) throws WSDeploymentException, WarningException {

  }

  public void commitStop(String applicationName) throws WarningException {
    Vector warnings = new Vector();

    try {
      wsRuntimeActivator.stop(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      wsClientsRuntimeActivator.stop(applicationName);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }
    
//    try {
//      if(WSContainer.getWSModelController() != null)
//          WSContainer.getWSModelController().unregisterModelsForApplication(applicationName);
//    } catch(Exception e) {
//      Location.getLocation(com.sap.engine.services.webservices.webservices630.server.deploy.WSDeployer.class).traceThrowableT(500, "Warning", e);
//      warnings.add("Warning" + e.getLocalizedMessage());
//    }

    if (warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void rollbackStop(String applicationName) throws WarningException {

  }

  public void notifyRuntimeChanges(String applicationName, Configuration appConfiguration)
			throws WarningException {
    WSContainer.getEventContext().getEventHandler().notifyRuntimeChanges(applicationName,  appConfiguration);
  }

  public void prepareRuntimeChanges(String applicationName)
      throws DeploymentException, WarningException {
    WSContainer.getEventContext().getEventHandler().makeRuntimeChanges(applicationName);
  }

   public ApplicationDeployInfo commitRuntimeChanges(String applicationName) throws WarningException {
     WSContainer.getEventContext().getEventHandler().commitRuntimeChanges(applicationName);
     return new ApplicationDeployInfo();
   }

  public void rollbackRuntimeChanges(String applicationName) throws WarningException {
    WSContainer.getEventContext().getEventHandler().rollbackRuntimeChanges(applicationName);
  }

  public File[] getClientJar(String applicationName) {
    return new File[0];
  }

  public void addProgressListener(ProgressListener listener) {
  }

  public void removeProgressListener(ProgressListener listener) {
  }

  public ExportInfo[] getCurrentStatus(String applicationName) throws WarningException {
    return new ExportInfo[0];
  }

  public boolean needStopOnSingleFileUpdate(FileUpdateInfo[] files, ContainerDeploymentInfo dInfo, Properties props) throws DeploymentException, WarningException {
    return false;
  }

  public void notifySingleFileUpdate(String applicationName, Configuration config, Properties props) throws WarningException {
  }

  public void notifySingleFileUpdate(String applicationName, Properties props) throws WarningException {
  }

  public ApplicationDeployInfo makeSingleFileUpdate(FileUpdateInfo[] files, ContainerDeploymentInfo dInfo, Properties props) throws DeploymentException {
    return null;
  }

  public void prepareSingleFileUpdate(String applicationName) throws DeploymentException, WarningException {
  }

  public ApplicationDeployInfo commitSingleFileUpdate(String applicationName) throws WarningException {
    return null;
  }

  public void rollbackSingleFileUpdate(String applicationName, Configuration config) throws WarningException {
  }

  public void applicationStatusChanged(String applicationName, byte status) {
  }

  public String[] getResourcesForTempLoader(String applicationName) throws DeploymentException {
    return new String[0];
  }

  public boolean acceptedAppInfoChange(String appName, AdditionalAppInfo addAppInfo) throws DeploymentException {
    return false;
  }

  public boolean needStopOnAppInfoChanged(String appName, AdditionalAppInfo addAppInfo) {
    return false;
  }

  public void makeAppInfoChange(String appName, AdditionalAppInfo addAppInfo, Configuration configuration) throws DeploymentException {
  }

  public void appInfoChangedCommit(String appName) throws WarningException {
  }

  public void appInfoChangedRollback(String appName) throws WarningException {
  }

  public void notifyAppInfoChanged(String appName) throws WarningException {
  }

  public int getWSType(String applicationName) throws Exception {
    int wsType = WSBaseConstants.WS_INTERNAL_COMPONENT;

    Context ctx = new InitialContext();
    WSContainerInterfaceImpl wsContainerInterface = (WSContainerInterfaceImpl)ctx.lookup(WebServicesFrame.WS_CONTEXT_NAME + "/" + WSContainerInterface.NAME);

    if (wsContainerInterface.isExernalWSComponent(applicationName)) {
      wsType = WSBaseConstants.WS_EXTERNAL_COMPONENT;
    }

    return wsType;
  }

  public String getWSContainerDir(String applicationName) throws Exception {
    return getWSContainerDir(applicationName, getWSType(applicationName));    
  }

  public String getWSContainerDir(String applicationName, int wsType) throws IOException {
    String wsContainerDir = null;
    if (wsType == WSBaseConstants.WS_INTERNAL_COMPONENT) {
      wsContainerDir = deployCommunicator.getMyWorkDirectory(applicationName);
    }
    if (wsType == WSBaseConstants.WS_EXTERNAL_COMPONENT) {
      wsContainerDir = serviceWorkingDir + WSBaseConstants.SEPARATOR + "apps" + WSBaseConstants.SEPARATOR + applicationName + WSBaseConstants.SEPARATOR + WSBaseConstants.WS_CONTAINER_NAME;
    }
    return wsContainerDir;
  }

  private String getWSContainerWorkingDir(String applicationName) {
   return serviceWorkingDir + WSBaseConstants.SEPARATOR + "deploy" + WSBaseConstants.SEPARATOR + WSUtil.replaceForbiddenChars(applicationName);
  }

  private AppDeployInfo getAppDeployInfo(ContainerDeploymentInfo containerDeploymentInfo) {
    AppDeployInfo appDeployInfo = new AppDeployInfo();

    appDeployInfo.setApplicationName(containerDeploymentInfo.getApplicationName());
    appDeployInfo.setLoader(containerDeploymentInfo.getLoader());
    appDeployInfo.setAppConfiguration(containerDeploymentInfo.getConfiguration());
    appDeployInfo.setWebMappings(getWebModuleMappings(containerDeploymentInfo));

    return appDeployInfo;
  }

  private Hashtable getWebModuleMappings(ContainerDeploymentInfo containerDeploymentInfo) {
    String aliases[] = containerDeploymentInfo.getAliases();
    if(aliases == null) {
      return new Hashtable();
    }

    Hashtable webMappings = new Hashtable();
    for(int i = 0; i < aliases.length; i++) {
        webMappings.put(containerDeploymentInfo.getUri(aliases[i]), aliases[i]);
    }

    return webMappings;
  }

  private ApplicationDeployInfo defineApplicationDeployInfo(WSAppDeployResult wsAppDeployResult, WSClientsAppDeployResult wsClientsAppDeployResult) {
    ApplicationDeployInfo applicationDeployInfo = new ApplicationDeployInfo();
    applicationDeployInfo.setDeployedComponentNames(WSUtil.unifyStrings(new String[][]{wsAppDeployResult.getDeployedComponentNames(), wsClientsAppDeployResult.getWsClientsDeployResult().getDeployedComponentNames()}));
    applicationDeployInfo.setFilesForClassloader(WSUtil.unifyStrings(new String[][]{wsAppDeployResult.getFilesForClassloader(), wsClientsAppDeployResult.getModuleDeployResult().getFilesForClassLoader(), wsClientsAppDeployResult.getWsClientsDeployResult().getFilesForClassLoader()}));

    Vector warnings = new Vector();
    warnings.addAll(wsAppDeployResult.getWarnings());
    warnings.addAll(wsClientsAppDeployResult.getWarnings());
    applicationDeployInfo.setWarnings(warnings);

    return applicationDeployInfo;
  }

  private ApplicationDeployInfo defineApplicationDeployInfo(WSAppUpdateResult wsAppUpdateResult, WSClientsAppUpdateResult wsClientsAppUpdateResult) {
    ApplicationDeployInfo applicationDeployInfo = new ApplicationDeployInfo();
    applicationDeployInfo.setDeployedComponentNames(WSUtil.unifyStrings(new String[][]{wsAppUpdateResult.getWsUpdateResult().getDeployedComponentNames(), wsClientsAppUpdateResult.getWsClientsUpdateResult().getDeployedComponentNames()}));
    applicationDeployInfo.setFilesForClassloader(WSUtil.unifyStrings(new String[][]{wsAppUpdateResult.getModuleDeployResult().getFilesForClassLoader(), wsAppUpdateResult.getWsUpdateResult().getFilesForClassLoader(), wsClientsAppUpdateResult.getModuleUpdateResult().getFilesForClassLoader(), wsClientsAppUpdateResult.getWsClientsUpdateResult().getFilesForClassLoader()}));

    Vector warnings = new Vector();
    warnings.addAll(wsAppUpdateResult.getWarnings());
    warnings.addAll(wsClientsAppUpdateResult.getWarnings());
    applicationDeployInfo.setWarnings(warnings);

    return applicationDeployInfo;
  }

  public static String[] getWSReferences() {
    return new String[]{"library:sapxmltoolkit",
                        "library:webservices_lib",
                        "library:tc~bl~base_webservices_lib",
                        "interface:webservices",
                        "library:jaxrpc_api",
                        "library:jaxr_api"};
  }

  public static String getJarsPath() {
    String[] resourceNames = getWSReferences();
    LoadContext loadContext = WSContainer.getServiceContext().getCoreContext().getLoadContext();
    SystemMonitor systemMonitor = WSContainer.getServiceContext().getContainerContext().getSystemMonitor();

    ArrayList<String> jars = new ArrayList<String>();     
    String resource; 
    String resourceType;
    String resourceName; 
    for(int i = 0; i < resourceNames.length; i++) {
      resource = resourceNames[i];
      resourceType = resource.substring(0, resource.indexOf(":"));
      resourceName = resource.substring(resource.indexOf(":") + 1);     
      if(resourceType.equals("library")) {
        WSUtil.addStrings(systemMonitor.getLibrary(resourceName).getJars(), jars);
      }
      if(resourceType.equals("interface")) {
        WSUtil.addStrings(systemMonitor.getInterface(resourceName).getJars(), jars); 
      }
      if(resourceType.equals("service")) {
        WSUtil.addStrings(systemMonitor.getService(resourceName).getJars(), jars); 
      }      
    }
    
    WSUtil.addStrings(loadContext.getResourceNames(WebServicesDeployManager.class.getClassLoader().getParent()), jars);  

    return WSUtil.concatStrings(jars, File.pathSeparator);
  }

  private void removeWSContainerDir(String applicationName) throws WSWarningException {
    String excMsg = "Error occurred, deleting " + WSBaseConstants.WS_CONTAINER_NAME + " directory. ";

    String wsContainerDir = null;
    try{
      wsContainerDir = getWSContainerDir(applicationName, WSBaseConstants.WS_INTERNAL_COMPONENT);
      IOUtil.deleteDir(wsContainerDir);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = null;
      if(wsContainerDir != null) {
        msg = excMsg + "Unable to delete" + wsContainerDir + " directory. ";
      } else {
        msg = excMsg + "Unable to delete" + WSBaseConstants.WS_CONTAINER_NAME + " directory. ";
      }

      WSWarningException wExc = new WSWarningException();
      wExc.addWarning(msg);
      throw wExc;
    }
  }

}


