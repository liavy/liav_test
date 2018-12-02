/*
 * Copyright (c) 2004-2009 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG0, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.deploy;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationHandler;
import com.sap.engine.frame.core.configuration.ConfigurationHandlerFactory;
import com.sap.engine.lib.util.ArrayObject;
import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.accounting.measurement.AMeasurement;
import com.sap.engine.services.deploy.DeployCallback;
import com.sap.engine.services.deploy.DeployEvent;
import com.sap.engine.services.deploy.container.AdditionalAppInfo;
import com.sap.engine.services.deploy.container.ApplicationDeployInfo;
import com.sap.engine.services.deploy.container.ContainerDeploymentInfo;
import com.sap.engine.services.deploy.container.ContainerInfo;
import com.sap.engine.services.deploy.container.ContainerInterface;
import com.sap.engine.services.deploy.container.ContainerInterfaceExtension;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.deploy.container.FileUpdateInfo;
import com.sap.engine.services.deploy.container.ProgressEvent;
import com.sap.engine.services.deploy.container.ProgressListener;
import com.sap.engine.services.deploy.container.WarningException;
import com.sap.engine.services.deploy.container.op.start.ApplicationStartInfo;
import com.sap.engine.services.deploy.container.op.start.ContainerStartInfo;
import com.sap.engine.services.deploy.container.op.util.StartUp;
import com.sap.engine.services.deploy.container.op.util.Status;
import com.sap.engine.services.deploy.ear.J2EEModule;
import com.sap.engine.services.httpserver.interfaces.HttpProvider;
import com.sap.engine.services.httpserver.interfaces.exceptions.HttpShmException;
import com.sap.engine.services.servlets_jsp.WebContainerInterface;
import com.sap.engine.services.servlets_jsp.admin.WebContainerLazyMBeanProvider;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.WebContainerProvider;
import com.sap.engine.services.servlets_jsp.server.deploy.util.Constants;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebContainerServiceException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.IWebContainerProvider;
import com.sap.tc.logging.Location;

/**
 * This interface is used for services that will support deployment of their own components.
 * All the actions of deployment are made through the DeployService. A component must be deployed
 * whether the service is running or not.
 *
 * @author Violeta Georgieva
 * @version 7.10
 */
public class WebContainer implements ContainerInterface, ContainerInterfaceExtension, DeployCallback {
  private static final int APP_PRIORITY = 75;
  private static Location currentLocation = Location.getLocation(WebContainer.class);
  private static Location traceLocation = LogContext.getLocationDeploy();
  public static String sapDefaultApplName = "sap.com/com.sap.engine.docs.examples";                                                     
  private DeployAction deployApplication = null;
  private RemoveAction removeApplication = null;
  private UpdateAction updateApplication = null;
  private SingleFileUpdateAction singleFileUpdateApplication = null;
  private StartAction startApplication = null;
  private StopAction stopApplication = null;
  private RuntimeChangesAction runtimeChangesApplication = null;
  private AppInfoAction appInfoApplication = null;
  private DeployCallbackAction deployCallbackAction = null;
  /**
   * A reference to the frame of the service
   */
  private ContainerInfo containerInfo = new ContainerInfo();
  public ArrayObject startedApplications = new ArrayObject();
  private WebContainerHelper webContainerHelper = null;
  protected WebContainerProvider iWebContainer = new WebContainerProvider();
  private WebContainerLazyMBeanProvider mBeanProvider = null;

  public boolean allApplicationsStarted = false;
  public AtomicInteger allApplicationsStartedCounter = new AtomicInteger(0);

 
  //Local webcontainer startup measures - enabled when Accounting is on
  public static AtomicLong applicationsStartupTime = new AtomicLong(); //Sum of all apps startup times
  public static AtomicLong wceStartupTime = new AtomicLong(); //Sum of all WCE startup times for all apps
  public static AtomicLong getSubjectStartupTime = new AtomicLong(); //Sum of all subjects startup times
  public static AtomicLong doAsStartupTime = new AtomicLong(); //Sum of all subjects doAs startup times
  public static AtomicLong initStartupTime1 = new AtomicLong(); //Sum of all servlet's init methods startup times
  public static AtomicLong initStartupTime2 = new AtomicLong(); //Sum of all other identity servlet's init methods startup times
  public static AtomicLong parseTLD = new AtomicLong(); //Sum of the time for parsing the TLD files 
  public static AtomicLong downloadAppFilesTime = new AtomicLong(); //Sum of the time for all downloadAppFiles for web applications
  public static AtomicLong allApplicationsStartedTime = new AtomicLong(); //Sum of all WCE startup times for event all apps started
  public static AtomicLong loadTLDListenerFromClassLoaderRsourceTime = new AtomicLong();
  public static AtomicLong loadClassTime = new AtomicLong();
  
  /**
   * Creates new WebContainer object. This object is passed to deploy service.
   * DeployComunicator object is returned from Deploy Service.
   * @param serviceName
   */
  public WebContainer(String serviceName) {
    containerInfo.setName(Constants.CONTAINER_NAME);
    // we are only J2EE container
    containerInfo.setModuleName(null);
    containerInfo.setJ2EEModuleName(J2EEModule.web);
    containerInfo.setJ2EEContainer(true);
    // only *.war are accepted, if sda is standalone war - deploy service convert it to war (rename it)
    containerInfo.setFileExtensions(new String[]{".war"});
    // no specific files
    containerInfo.setFileNames(null);
    containerInfo.setServiceName(serviceName);
    containerInfo.setSupportingSingleFileUpdate(true);
    containerInfo.setPriority(APP_PRIORITY);
    containerInfo.setSupportingLazyStart(true);
    containerInfo.setSupportingParallelism(true);
    // invoked only if there is no application.xml (Java EE compatible) to check if we are interested in this module.
    // it is also called for standalone war
    containerInfo.setModuleDetector(new WebModuleDetector());
  }//end of constructor

  /**
   * Initializes all deploy actions.
   * @param runtimeInterface
   * @param configurationFactory
   * @throws WebContainerServiceException
   */
  public void initialize(WebContainerInterface runtimeInterface, ConfigurationHandlerFactory configurationFactory) throws WebContainerServiceException {
    webContainerHelper = new WebContainerHelper();
    deployApplication = new DeployAction(iWebContainer, containerInfo, webContainerHelper, runtimeInterface);
    removeApplication = new RemoveAction(iWebContainer, containerInfo, webContainerHelper, runtimeInterface);
    updateApplication = new UpdateAction(iWebContainer, containerInfo, webContainerHelper, runtimeInterface, deployApplication, removeApplication);
    singleFileUpdateApplication = new SingleFileUpdateAction(iWebContainer, containerInfo, webContainerHelper, runtimeInterface);
    startApplication = new StartAction(iWebContainer, containerInfo, webContainerHelper, runtimeInterface);
    stopApplication = new StopAction(iWebContainer, containerInfo, webContainerHelper, runtimeInterface);
    runtimeChangesApplication = new RuntimeChangesAction(iWebContainer, containerInfo, webContainerHelper, runtimeInterface);
    appInfoApplication = new AppInfoAction(iWebContainer, containerInfo, webContainerHelper, runtimeInterface, configurationFactory);
    deployCallbackAction = new DeployCallbackAction(iWebContainer, containerInfo, webContainerHelper, runtimeInterface);
  }//end of initialize(WebContainerInterface runtimeInterface, ConfigurationHandlerFactory configurationFactory)

  public IWebContainerProvider getIWebContainerProvider() {
    return iWebContainer;
  }//end of getIWebContainerProvider()

  /**
   * Invoked on WebContainer service stop
   *
   */
  public void destroyWebContainerExtensions() {
    iWebContainer.stop();
  }//end of destroyWebContainerExtensions()

  /**
   * Invoked on service start up to check consistency of aliases in deploy and http.
   *
   */
  public void checkApplicationAliasesInHttp() {
    //ContainerInfoAction is not needed anymore and replaced here by deployAction
    deployApplication.checkAppAliasesInHttp();
  }//end of checkApplicationAliasesInHttp()

  /**
   * Prepare all necessary data for WCEs
   * @param applicationName
   */
  public void prepareApplicationInfo(String applicationName, boolean serviceStart) {
    //ContainerInfoAction is not needed anymore and replaced here by deployAction
    deployApplication.prepareApplicationInfo(applicationName, serviceStart);
  }//end of prepareApplicationInfo(String applicationName)


  //Container information

  /**
   * @see com.sap.engine.services.deploy.container.ContainerInterface#getContainerInfo()
   */
  public ContainerInfo getContainerInfo() {
    return containerInfo;
  }//end of getContainerInfo()

  /**
   * @see com.sap.engine.services.deploy.container.ContainerInterface#getClientJar(java.lang.String)
   */
  public File[] getClientJar(String appName) {
    return null;
  }//end of getClientJar(String appName)

  /**
   * This method id invoked before deploy(), when standalone module is deploying.
   * It defines the official name of the application, which will contain the module.
   * <p/>
   * if the container doesn't support this convention.
   * Then the official name of the application will be <cont_name>_<module_file_name>
   *
   * @see com.sap.engine.services.deploy.container.ContainerInterface#getApplicationName(java.io.File)
   */
  public String getApplicationName(File standaloneFile) throws DeploymentException {
    throw new WebDeploymentException(WebDeploymentException.NO_SUPPORT);
  }//end of getApplicationName(File standaloneFile)

  /**
   * @see com.sap.engine.services.deploy.container.ContainerInterface#getResourcesForTempLoader(java.lang.String)
   */
  public String[] getResourcesForTempLoader(String applicationName) throws DeploymentException {    
    String[] aliasesCanonicalized = ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalized(applicationName);
    //ContainerInfoAction is not needed anymore and replaced here by deployAction
    return deployApplication.getFilesForClassPath(applicationName, aliasesCanonicalized, false);
  }//end of getResourcesForTempLoader(String applicationName)


  //Deploy application

  /**
   * Not implemented
   * @see com.sap.engine.services.deploy.container.ContainerInterface#prepareDeploy(java.lang.String, com.sap.engine.frame.core.configuration.Configuration)
   */
  public void prepareDeploy(String applicationName, Configuration appConfig) throws DeploymentException, WarningException {
    if (traceLocation.beInfo()) {
      traceLocation.infoT("Prepare deploy of application [" + applicationName + "] begin on web container.");
    }
  }//end of prepareDeploy(String applicationName, Configuration appConfig)

  /**
   * Invoked on deploy. Commits deploy transaction. Invoked only on the server responsible for deployment.
   * @see com.sap.engine.services.deploy.container.ContainerInterface#deploy(java.io.File[], com.sap.engine.services.deploy.container.ContainerDeploymentInfo, java.util.Properties)
   */
  public ApplicationDeployInfo deploy(File[] archiveFilesFromDeploy, ContainerDeploymentInfo dInfo, Properties props) throws DeploymentException {
    if (traceLocation.beInfo()) {
      traceLocation.infoT("Deploy of application [" + dInfo.getApplicationName() + "] begin on web container.");
    }
    final String tagName = "WebContainer.deploy [" + dInfo.getApplicationName() + "]";
    try {      
      if (Accounting.isEnabled()) { //ACCOUNTING.start - BEGIN 
        Accounting.beginMeasure(tagName, WebContainer.class);
      }//ACCOUNTING.start - END      
      return deployApplication.deploy(archiveFilesFromDeploy, dInfo, props, false, WebContainerHelper.getAliases(dInfo.getApplicationName(), props));
    } finally {//ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        AMeasurement measurement = Accounting.endMeasure(tagName);
        if (LogContext.getLocationStatistics().beDebug() && measurement != null) {
          LogContext.getLocationStatistics().debugT(measurement.toDocumentAsString());
        }
      }
    }//ACCOUNTING.end - END
  }//end of deploy(File[] archiveFilesFromDeploy, ContainerDeploymentInfo dInfo, Properties props)

  /**
   * Post deploy actions. Invoked only on the server responsible for deployment.
   * @see com.sap.engine.services.deploy.container.ContainerInterface#commitDeploy(java.lang.String)
   */
  public void commitDeploy(String applicationName) throws WarningException {
    if (traceLocation.beInfo()) {
      traceLocation.infoT("Commit deploy of application [" + applicationName + "] begin on web container.");
    }
    final String tagName = "WebContainer.commitDeploy [" + applicationName + "]";
    try {
      if (Accounting.isEnabled()) { //ACCOUNTING.start - BEGIN 
        Accounting.beginMeasure(tagName, WebContainer.class);
      }//ACCOUNTING.start - END
      
      deployApplication.commitDeploy(applicationName);
    } finally {//ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        AMeasurement measurement = Accounting.endMeasure(tagName);
        if (LogContext.getLocationStatistics().beDebug() && measurement != null) {
          LogContext.getLocationStatistics().debugT(measurement.toDocumentAsString());
        }
      }
    }//ACCOUNTING.end - END
    if (traceLocation.bePath()) {
      traceLocation.pathT("Deploy of application [" + applicationName + "] finished successfully in web container.");
    }
  }//end of commitDeploy(String applicationName)

  /**
   * Invoked if deploy action fails. It mast be always successful. Invoked only on the server responsible for deployment.
   * @see com.sap.engine.services.deploy.container.ContainerInterface#rollbackDeploy(java.lang.String)
   */
  public void rollbackDeploy(String applicationName) throws WarningException {
    if (traceLocation.beInfo()) {
      traceLocation.infoT("Rollback deploy of application [" + applicationName + "] begin on web container.");
    }
    final String tagName = "WebContainer.rollbackDeploy [" + applicationName + "]";
    try {
      if (Accounting.isEnabled()) { //ACCOUNTING.start - BEGIN 
        Accounting.beginMeasure(tagName, WebContainer.class);
      }//ACCOUNTING.start - END
      deployApplication.rollbackDeploy(applicationName);
    } finally {//ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        AMeasurement measurement = Accounting.endMeasure(tagName);
        if (LogContext.getLocationStatistics().beDebug() && measurement != null) {
          LogContext.getLocationStatistics().debugT(measurement.toDocumentAsString());
        }
      }
    }//ACCOUNTING.end - END
    if (traceLocation.bePath()) {
      traceLocation.pathT("Deploy of application [" + applicationName + "] rolled back successfully in web container.");
    }
  }//end of rollbackDeploy(String applicationName)

  /**
   * When deployment of components on one server from the cluster is completed all the others are
   * notified to check for the deployed application.
   * @see com.sap.engine.services.deploy.container.ContainerInterface#notifyDeployedComponents(java.lang.String, java.util.Properties)
   */
  public void notifyDeployedComponents(String applicationName, Properties props) throws WarningException {
    if (traceLocation.beInfo()) {
      traceLocation.infoT("notifyDeployedComponents() starts for application [" + applicationName + "] on web container.");
    }
    String accountingTag = "WebContainer.notifyDeployedComponents [" + applicationName + "]";
    try { //ACCOUNTING.start - BEGIN
      if (Accounting.isEnabled()) { 
        Accounting.beginMeasure(accountingTag, WebContainer.class);
      } //ACCOUNTING.start - END
      deployApplication.notifyDeployedComponents(applicationName, props);
    } finally { //ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        AMeasurement measurement = Accounting.endMeasure(accountingTag);
        if (LogContext.getLocationStatistics().beDebug() && measurement != null) {
          LogContext.getLocationStatistics().debugT(measurement.toDocumentAsString());
        }
      }
    } //ACCOUNTING.end - END
    if (traceLocation.bePath()) {
      traceLocation.pathT("notifyDeployedComponents() finished for application [" + applicationName + "] on web container.");
    }
  }//end of notifyDeployedComponents(String applicationName, Properties props)


  //Remove Application

  /**
   * This method invokes remove(String applicationName) method on all servers because of this
   * it is replaced with remove(String applicationName, ConfigurationHandler operationHandler, Configuration appConfiguration)
   * from ContainerInterfaceExtension, which is invoked on one server, the other servers will be just notified for removing application.
   *
   * @see com.sap.engine.services.deploy.container.ContainerInterface#remove(java.lang.String)
   */
  public void remove(String applicationName) throws DeploymentException, WarningException {
  }//end of remove(String applicationName)

  /**
   * Invoked on the server, which receives the request for removing an application.
   * After that the application configuration is removed from DB and all other servers
   * is invoked the method notifyRemove(String applicationName)
   * @see com.sap.engine.services.deploy.container.ContainerInterfaceExtension#remove(java.lang.String, com.sap.engine.frame.core.configuration.ConfigurationHandler, com.sap.engine.frame.core.configuration.Configuration)
   */
  public void remove(String applicationName, ConfigurationHandler operationHandler, Configuration appConfiguration) throws DeploymentException, WarningException {
    if (traceLocation.beInfo()) {
      traceLocation.infoT("Remove of application [" + applicationName + "] begin on web container.");
    }
    final String tagName = "WebContainer.remove [" + applicationName + "]";
    try { //ACCOUNTING.start - BEGIN
      if (Accounting.isEnabled()) { 
        Accounting.beginMeasure(tagName, WebContainer.class);
      } //ACCOUNTING.start - END
      removeApplication.remove(applicationName, appConfiguration);
    } finally { //ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        AMeasurement measurement = Accounting.endMeasure(tagName);
        if (LogContext.getLocationStatistics().beDebug() && measurement != null) {
          LogContext.getLocationStatistics().debugT(measurement.toDocumentAsString());
        }
      }
    } //ACCOUNTING.end - END
    if (traceLocation.bePath()) {
      traceLocation.pathT("Remove of application [" + applicationName + "] finished successfully in web container.");
    }
  }//end of remove(String applicationName, ConfigurationHandler operationHandler, Configuration appConfiguration)

  /**
   * Notifies all servers in the cluster, except the one that process the remove request.
   * @see com.sap.engine.services.deploy.container.ContainerInterfaceExtension#notifyRemove(java.lang.String)
   */
  public void notifyRemove(String applicationName) throws WarningException {
    if (traceLocation.beInfo()) {
      traceLocation.infoT("notifyRemove() of application [" + applicationName + "] begin on web container.");
    }
    final String tagName = "WebContainer.notifyRemove [" + applicationName + "]";
    try { //ACCOUNTING.start - BEGIN
      if (Accounting.isEnabled()) { 
        Accounting.beginMeasure(tagName, WebContainer.class);
      } //ACCOUNTING.start - END
      removeApplication.notifyRemove(applicationName, null, false);
    } finally { //ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        AMeasurement measurement = Accounting.endMeasure(tagName);
        if (LogContext.getLocationStatistics().beDebug() && measurement != null) {
          LogContext.getLocationStatistics().debugT(measurement.toDocumentAsString());
        }
      }
    } //ACCOUNTING.end - END
  }//end of notifyRemove(String applicationName)

  /**
   * @see com.sap.engine.services.deploy.container.ContainerInterfaceExtension#commitRemove(java.lang.String)
   */
  public void commitRemove(String applicationName) throws WarningException {
  }//end of commitRemove(String applicationName)


  //Update application

  /**
   * Checks whether an update is required.
   * @return true.
   */
  public boolean needUpdate(File[] archiveFiles, ContainerDeploymentInfo dInfo, Properties props) throws DeploymentException, WarningException {
    if (traceLocation.beInfo()) {
      traceLocation.infoT("needUpdate() of application will begin on Web Container.");
    }
    return true;
  }//end of needUpdate(File[] archiveFiles, ContainerDeploymentInfo dInfo, Properties props)

  /**
   * Checks whether application stop is required during update.
   * @see com.sap.engine.services.deploy.container.ContainerInterface#needStopOnUpdate(java.io.File[], com.sap.engine.services.deploy.container.ContainerDeploymentInfo, java.util.Properties)
   */
  public boolean needStopOnUpdate(File[] archiveFiles, ContainerDeploymentInfo dInfo, Properties props) throws DeploymentException, WarningException {
    if (traceLocation.beInfo()) {
      traceLocation.infoT("needStopOnUpdate() of application will begin on Web Container.");
    }
    return true;
  }//end of needStopOnUpdate(File[] archiveFiles, ContainerDeploymentInfo dInfo, Properties props)

  /**
   * Responsible for application update. Occurs on one server only.
   * @see com.sap.engine.services.deploy.container.ContainerInterface#makeUpdate(java.io.File[], com.sap.engine.services.deploy.container.ContainerDeploymentInfo, java.util.Properties)
   */
  public ApplicationDeployInfo makeUpdate(File[] archiveFiles, ContainerDeploymentInfo dInfo, Properties props) throws DeploymentException {
    if (traceLocation.beInfo()) {
      traceLocation.infoT("makeUpdate() of application will begin on Web Container.");
    }
    try { //ACCOUNTING.start - BEGIN
      if (Accounting.isEnabled()) { 
        Accounting.beginMeasure("WebContainer.makeUpdate", WebContainer.class);
      } //ACCOUNTING.start - END
      return updateApplication.makeUpdate(archiveFiles, dInfo, props);
    } finally { //ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        AMeasurement measurement = Accounting.endMeasure("WebContainer.makeUpdate");
        if (LogContext.getLocationStatistics().beDebug() && measurement != null) {
          LogContext.getLocationStatistics().debugT(measurement.toDocumentAsString());
        }
      }
    } //ACCOUNTING.end - END
  }//end of makeUpdate(File[] archiveFiles, ContainerDeploymentInfo dInfo, Properties props)

  /**
   * Notifies other servers that the specified application has been updated.
   * @see com.sap.engine.services.deploy.container.ContainerInterface#notifyUpdatedComponents(java.lang.String, com.sap.engine.frame.core.configuration.Configuration, java.util.Properties)
   */
  public void notifyUpdatedComponents(String applicationName, Configuration applicationConfig, Properties properties) throws WarningException {
    if (traceLocation.beInfo()) {
      traceLocation.infoT("notifyUpdatedComponents() of application [" + applicationName + "] will begin on Web Container.");
    }
    try { //ACCOUNTING.start - BEGIN
      if (Accounting.isEnabled()) { 
        Accounting.beginMeasure("WebContainer.notifyUpdatedComponents", WebContainer.class);
      } //ACCOUNTING.start - END
      updateApplication.notifyUpdatedComponents(applicationName, properties);
    } finally { //ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        AMeasurement measurement = Accounting.endMeasure("WebContainer.notifyUpdatedComponents");
        if (LogContext.getLocationStatistics().beDebug() && measurement != null) {
          LogContext.getLocationStatistics().debugT(measurement.toDocumentAsString());
        }
      }
    } //ACCOUNTING.end - END
  }//end of notifyUpdatedComponents(String applicationName, Configuration applicationConfig, Properties properties)

  /**
   * @see com.sap.engine.services.deploy.container.ContainerInterface#prepareUpdate(java.lang.String)
   */
  public void prepareUpdate(String applicationName) throws DeploymentException, WarningException {
    if (traceLocation.beInfo()) {
      traceLocation.infoT("Prepare update of application [" + applicationName + "] will begin on Web Container.");
    }
  }//end of prepareUpdate(String applicationName)

  /**
   * Post commit actions.
   * @see com.sap.engine.services.deploy.container.ContainerInterface#commitUpdate(java.lang.String)
   */
  public ApplicationDeployInfo commitUpdate(String applicationName) {
    if (traceLocation.beInfo()) {
      traceLocation.infoT("Commit update of application [" + applicationName + "] will begin on Web Container.");
    }
    final String tagName = "WebContainer.commitUpdate [" + applicationName + "]";
    try { //ACCOUNTING.start - BEGIN
      if (Accounting.isEnabled()) { 
        Accounting.beginMeasure(tagName, WebContainer.class);
      } //ACCOUNTING.start - END
      return updateApplication.commitUpdate(applicationName);
    } finally { //ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        AMeasurement measurement = Accounting.endMeasure(tagName);
        if (LogContext.getLocationStatistics().beDebug() && measurement != null) {
          LogContext.getLocationStatistics().debugT(measurement.toDocumentAsString());
        }
      }
    } //ACCOUNTING.end - END
  }//end of commitUpdate(String applicationName)

  /**
   * Restores old application
   * @see com.sap.engine.services.deploy.container.ContainerInterface#rollbackUpdate(java.lang.String, com.sap.engine.frame.core.configuration.Configuration, java.util.Properties)
   */
  public void rollbackUpdate(String applicationName, Configuration applicationConfig, Properties props) throws WarningException {
    if (traceLocation.beInfo()) {
      traceLocation.infoT("Rollback update of application [" + applicationName + "] will begin on Web Container.");
    }
    final String tagName = "WebContainer.rollbackUpdate [" + applicationName + "]";
    try { //ACCOUNTING.start - BEGIN
      if (Accounting.isEnabled()) { 
        Accounting.beginMeasure(tagName, WebContainer.class);
      } //ACCOUNTING.start - END
      updateApplication.rollbackUpdate(applicationName, applicationConfig);
    } finally { //ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        AMeasurement measurement = Accounting.endMeasure(tagName);
        if (LogContext.getLocationStatistics().beDebug() && measurement != null) {
          LogContext.getLocationStatistics().debugT(measurement.toDocumentAsString());
        }
      }
    } //ACCOUNTING.end - END
    if (traceLocation.bePath()) {
      traceLocation.pathT("Update of application [" + applicationName + "] rolled back successfully on Web Container.");
    }
  }//end of rollbackUpdate(String applicationName, Configuration applicationConfig, Properties props)


  //Start application

  /**
   * Download application binaries from DB.
   */
  public void downloadApplicationFiles(String applicationName, Configuration appConfig) throws DeploymentException, WarningException {
    if (traceLocation.bePath()) {
      traceLocation.pathT("Start downloading files of application [" + applicationName + "] will begin on Web Container.");
    }
    long startup = -1;
    try { //ACCOUNTING.start - BEGIN
      if (Accounting.isEnabled()) { 
        Accounting.beginMeasure("WebContainer.downloadApplicationFiles", WebContainer.class);
        startup = System.currentTimeMillis();
      } //ACCOUNTING.start - END
      startApplication.downloadAppFiles(applicationName, appConfig, "");
    } finally { //ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        AMeasurement measurement = Accounting.endMeasure("WebContainer.downloadApplicationFiles");
        startup = System.currentTimeMillis() - startup;
        downloadAppFilesTime.addAndGet(startup);
        if (LogContext.getLocationStatistics().beDebug() && measurement != null) {
          LogContext.getLocationStatistics().debugT(measurement.toDocumentAsString());
        }
      }
    } //ACCOUNTING.end - END
    if (traceLocation.beDebug()) {
      traceLocation.debugT(LogContext.getExceptionStackTrace(new Exception()));
    }
  }//end of downloadApplicationFiles(String applicationName, Configuration appConfig)

  /**
   * Invoked on each server node to start application
   * @see com.sap.engine.services.deploy.container.ContainerInterface#prepareStart(java.lang.String, com.sap.engine.frame.core.configuration.Configuration)
   */
  public void prepareStart(String applicationName, Configuration appConfig) throws DeploymentException, WarningException {
    if (traceLocation.beInfo()) {
      traceLocation.infoT("Prepare start of application [" + applicationName + "] will begin on Web Container.");
    }
    long startupTime = -1;
    final String tagName = "WebContainer.prepareStart [" + applicationName + "]";
    try { //ACCOUNTING.start - BEGIN
      if (Accounting.isEnabled()) {
        startupTime = System.currentTimeMillis();        
        Accounting.beginMeasure(tagName, WebContainer.class);
      } //ACCOUNTING.start - END
      
      startApplication.prepareStart(applicationName, appConfig);
      
    } finally { //ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {        
        AMeasurement measurement = Accounting.endMeasure(tagName);
        if (LogContext.getLocationStatistics().beDebug() && measurement != null) {
          LogContext.getLocationStatistics().debugT(measurement.toDocumentAsString());
        }
        startupTime = System.currentTimeMillis() - startupTime;
        applicationsStartupTime.addAndGet(startupTime);
      }
    } //ACCOUNTING.end - END
  }//end of prepareStart(String applicationName, Configuration appConfig)

  /**
   * Finalizes application start
   * @see com.sap.engine.services.deploy.container.ContainerInterface#commitStart(java.lang.String)
   */
  public void commitStart(String applicationName) throws WarningException {
    if (traceLocation.beInfo()) {
      traceLocation.infoT("Commit start of application [" + applicationName + "] will begin on Web Container.");
    }
    final String tagName = "WebContainer.commitStart [" + applicationName + "]";
    try { //ACCOUNTING.start - BEGIN
      if (Accounting.isEnabled()) {         
        Accounting.beginMeasure(tagName, WebContainer.class);
      } //ACCOUNTING.start - END
      
      startApplication.commitStart(applicationName);
      
    } finally { //ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        AMeasurement measurement = Accounting.endMeasure(tagName);
        if (LogContext.getLocationStatistics().beDebug() && measurement != null) {
          LogContext.getLocationStatistics().debugT(measurement.toDocumentAsString());
        }
      }
    } //ACCOUNTING.end - END
    if (traceLocation.bePath()) {
      traceLocation.pathT("Start of application [" + applicationName + "] finished successfully on Web Container.");
    }
  }//end of commitStart(String applicationName)

  /**
   * Stops application if start is unsuccessful.
   * @see com.sap.engine.services.deploy.container.ContainerInterface#rollbackStart(java.lang.String)
   */
  public void rollbackStart(String applicationName) throws WarningException {
    if (traceLocation.beInfo()) {
      traceLocation.infoT("Rollback start of application [" + applicationName + "] will begin on Web Container.");
    }
    final String tagName = "WebContainer.rollbackStart [" + applicationName + "]";
    try { //ACCOUNTING.start - BEGIN
      if (Accounting.isEnabled()) { 
        Accounting.beginMeasure(tagName, WebContainer.class);
      } //ACCOUNTING.start - END
      
      commitStop(applicationName);
    
    } finally { //ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        AMeasurement measurement = Accounting.endMeasure(tagName);
        if (LogContext.getLocationStatistics().beDebug() && measurement != null) {
          LogContext.getLocationStatistics().debugT(measurement.toDocumentAsString());
        }
      }
    } //ACCOUNTING.end - END
    if (traceLocation.bePath()) {
      traceLocation.pathT("Start of application [" + applicationName + "] rolled back successfully on Web Container.");
    }
  }//end of rollbackStart(String applicationName)

  /**
   * Invoked after deploy().
   * @see com.sap.engine.services.deploy.container.ContainerInterfaceExtension#makeStartInitially(com.sap.engine.services.deploy.container.op.start.ContainerStartInfo)
   */
  public ApplicationStartInfo makeStartInitially(ContainerStartInfo csInfo) throws DeploymentException {
    return null;
  }//end of makeStartInitially(ContainerStartInfo csInfo)


  //Stop application

  /**
   *
   */
  public void prepareStop(String applicationName, Configuration appConfig) throws DeploymentException, WarningException {
    if (traceLocation.beInfo()) {
      traceLocation.infoT("Prepare stop of application [" + applicationName + "] will begin on Web Container.");
    }
  }//end of prepareStop(String applicationName, Configuration appConfig)

  /**
   * Stops application. Invoked on each server node.
   * @see com.sap.engine.services.deploy.container.ContainerInterface#commitStop(java.lang.String)
   */
  public void commitStop(String applicationName) throws WarningException {
    if (traceLocation.beInfo()) {
      traceLocation.infoT("Commit stop of application [" + applicationName + "] will begin on Web Container.");
    }
    final String tagName = "WebContainer.commitStop [" + applicationName + "]";
    try { //ACCOUNTING.start - BEGIN
      if (Accounting.isEnabled()) { 
        Accounting.beginMeasure(tagName, WebContainer.class);
      } //ACCOUNTING.start - END
      
      stopApplication.commitStop(applicationName);

    } finally { //ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        AMeasurement measurement = Accounting.endMeasure(tagName);
        if (LogContext.getLocationStatistics().beDebug() && measurement != null) {
          LogContext.getLocationStatistics().debugT(measurement.toDocumentAsString());
        }
      }
    } //ACCOUNTING.end - END
    if (traceLocation.bePath()) {
      traceLocation.pathT("Stop of application [" + applicationName + "] finished successfully on Web Container.");
    }
  }//end of commitStop(String applicationName)

  /**
   * @see com.sap.engine.services.deploy.container.ContainerInterface#rollbackStop(java.lang.String)
   */
  public void rollbackStop(String applicationName) throws WarningException {
  }//end of rollbackStop(String applicationName)


  //Runtime Changes

  /**
   * Notifies container that some runtime changes were performed on the specified application.
   * @see com.sap.engine.services.deploy.container.ContainerInterface#notifyRuntimeChanges(java.lang.String, com.sap.engine.frame.core.configuration.Configuration)
   */
  public void notifyRuntimeChanges(String applicationName, Configuration appConfig) throws WarningException {
    if (traceLocation.bePath()) {
      traceLocation.pathT("Notify runtime changes for application [" + applicationName + "].");
    }
    final String tagName = "WebContainer.notifyRuntimeChanges [" + applicationName + "]";
    try { //ACCOUNTING.start - BEGIN
      if (Accounting.isEnabled()) { 
        Accounting.beginMeasure(tagName, WebContainer.class);
      } //ACCOUNTING.start - END
      
      runtimeChangesApplication.notifyRuntimeChanges(applicationName, appConfig);
      
    } finally { //ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        AMeasurement measurement = Accounting.endMeasure(tagName);
        if (LogContext.getLocationStatistics().beDebug() && measurement != null) {
          LogContext.getLocationStatistics().debugT(measurement.toDocumentAsString());
        }
      }
    } //ACCOUNTING.end - END
  }//end of notifyRuntimeChanges(String applicationName, Configuration appConfig)

  /**
   * @see com.sap.engine.services.deploy.container.ContainerInterface#prepareRuntimeChanges(java.lang.String)
   */
  public void prepareRuntimeChanges(String applicationName) throws DeploymentException, WarningException {
    if (traceLocation.bePath()) {
      traceLocation.pathT("Prepare runtime changes for application [" + applicationName + "].");
    }
  }//end of prepareRuntimeChanges(String applicationName)

  /**
   * Updates class loader changes.
   * @see com.sap.engine.services.deploy.container.ContainerInterface#commitRuntimeChanges(java.lang.String)
   */
  public ApplicationDeployInfo commitRuntimeChanges(String applicationName) throws WarningException {
    if (traceLocation.bePath()) {
      traceLocation.pathT("Commit runtime changes for application [" + applicationName + "].");
    }
    final String tagName = "WebContainer.commitRuntimeChanges [" + applicationName + "]";
    try { //ACCOUNTING.start - BEGIN
      if (Accounting.isEnabled()) { 
        Accounting.beginMeasure(tagName, WebContainer.class);
      } //ACCOUNTING.start - END
      
      return runtimeChangesApplication.commitRuntimeChanges(applicationName);
      
    } finally { //ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        AMeasurement measurement = Accounting.endMeasure(tagName);
        if (LogContext.getLocationStatistics().beDebug() && measurement != null) {
          LogContext.getLocationStatistics().debugT(measurement.toDocumentAsString());
        }
      }
    } //ACCOUNTING.end - END
  }//end of commitRuntimeChanges(String applicationName)

  /**
   * Not implemented.
   * @see com.sap.engine.services.deploy.container.ContainerInterface#rollbackRuntimeChanges(java.lang.String)
   */
  public void rollbackRuntimeChanges(String applicationName) throws WarningException {
    if (traceLocation.bePath()) {
      traceLocation.pathT("Rollback runtime changes for application [" + applicationName + "].");
    }
  }//end of rollbackRuntimeChanges(String applicationName)


  //SingleFileUpdate

  /**
   * Checks whether application stop is required for single file update
   */
  public boolean needStopOnSingleFileUpdate(FileUpdateInfo[] files, ContainerDeploymentInfo dInfo, Properties props) throws DeploymentException, WarningException {
    return singleFileUpdateApplication.needStopOnSingleFileUpdate(files, dInfo, props);
  }//end of needStopOnSingleFileUpdate(FileUpdateInfo[] files, ContainerDeploymentInfo dInfo, Properties props)

  /**
   * Real single file update action
   * @see com.sap.engine.services.deploy.container.ContainerInterface#makeSingleFileUpdate(com.sap.engine.services.deploy.container.FileUpdateInfo[], com.sap.engine.services.deploy.container.ContainerDeploymentInfo, java.util.Properties)
   */
  public ApplicationDeployInfo makeSingleFileUpdate(FileUpdateInfo[] files, ContainerDeploymentInfo dInfo, Properties props) throws DeploymentException {
    return singleFileUpdateApplication.makeSingleFileUpdate(files, dInfo, props);
  }//end of makeSingleFileUpdate(FileUpdateInfo[] files, ContainerDeploymentInfo dInfo, Properties props)

  /**
   * Notifies other server nodes about performed update.
   * @see com.sap.engine.services.deploy.container.ContainerInterface#notifySingleFileUpdate(java.lang.String, com.sap.engine.frame.core.configuration.Configuration, java.util.Properties)
   */
  public void notifySingleFileUpdate(String applicationName, Configuration config, Properties props) throws WarningException {
    if (traceLocation.bePath()) {
      traceLocation.pathT("Notify single file update for application [" + applicationName + "].");
    }
  }//end of notifySingleFileUpdate(String applicationName, Configuration config, Properties props)

  /**
   * @see com.sap.engine.services.deploy.container.ContainerInterface#prepareSingleFileUpdate(java.lang.String)
   */
  public void prepareSingleFileUpdate(String applicationName) throws DeploymentException, WarningException {
  }//end of prepareSingleFileUpdate(String applicationName)

  /**
   * Update class loader.
   * @see com.sap.engine.services.deploy.container.ContainerInterface#commitSingleFileUpdate(java.lang.String)
   */
  public ApplicationDeployInfo commitSingleFileUpdate(String applicationName) throws WarningException {
    return singleFileUpdateApplication.commitSingleFileUpdate(applicationName);
  }//end of commitSingleFileUpdate(String applicationName)

  /**
   * Reverts old application files.
   */
  public void rollbackSingleFileUpdate(String applicationName, Configuration config) throws WarningException {
    if (traceLocation.bePath()) {
      traceLocation.pathT("Rollback single file update for application [" + applicationName + "].");
    }  
  }//end of rollbackSingleFileUpdate(String applicationName, Configuration config)


  //Fail Over

  /**
   * @see com.sap.engine.services.deploy.container.ContainerInterface#acceptedAppInfoChange(java.lang.String, com.sap.engine.services.deploy.container.AdditionalAppInfo)
   */
  public boolean acceptedAppInfoChange(String appName, AdditionalAppInfo addAppInfo) throws DeploymentException {
    return appInfoApplication.acceptedAppInfoChange(appName, addAppInfo);
  }//end of acceptedAppInfoChange(String appName, AdditionalAppInfo addAppInfo)

  /**
   * @see com.sap.engine.services.deploy.container.ContainerInterface#needStopOnAppInfoChanged(java.lang.String, com.sap.engine.services.deploy.container.AdditionalAppInfo)
   */
  public boolean needStopOnAppInfoChanged(String appName, AdditionalAppInfo addAppInfo) {
    return true;
  }//end of needStopOnAppInfoChanged(String appName, AdditionalAppInfo addAppInfo)

  /**
   * Updates AppInfo changes in configuration.
   * @see com.sap.engine.services.deploy.container.ContainerInterface#makeAppInfoChange(java.lang.String, com.sap.engine.services.deploy.container.AdditionalAppInfo, com.sap.engine.frame.core.configuration.Configuration)
   */
  public void makeAppInfoChange(String appName, AdditionalAppInfo addAppInfo, Configuration configuration) throws WarningException, DeploymentException {
    appInfoApplication.makeAppInfoChange(appName, addAppInfo, configuration);
  }//end of makeAppInfoChange(String appName, AdditionalAppInfo addAppInfo, Configuration configuration)

  /**
   * @see com.sap.engine.services.deploy.container.ContainerInterface#appInfoChangedCommit(java.lang.String)
   */
  public void appInfoChangedCommit(String appName) throws WarningException {
  }//end of appInfoChangedCommit(String appName)

  /**
   * @see com.sap.engine.services.deploy.container.ContainerInterface#appInfoChangedRollback(java.lang.String)
   */
  public void appInfoChangedRollback(String appName) throws WarningException {
  }//end of appInfoChangedRollback(String appName)

  /**
   * Not necessary at the moment. Fail over value is read on application start.
   * @see com.sap.engine.services.deploy.container.ContainerInterface#notifyAppInfoChanged(java.lang.String)
   */
  public void notifyAppInfoChanged(String appName) throws WarningException {
  }//end of notifyAppInfoChanged(String appName)


  //Application status

  /**
   * This is a notification that application has changed its status. This method
   * has meaning only for applications with startup mode "always". Applications
   * defined as "lazy" are always treated as started (by ICM).
   *
   * @param applicationName the name of the application that has changed its status.
   * @param status          new status of the application; possible values:
   *                        DeployCommunicator.STARTED
   *                        DeployCommunicator.STOPPED
   *                        DeployCommunicator.STOPPING
   *                        DeployCommunicator.STARTING
   *                        DeployCommunicator.UPGRADING
   *                        DeployCommunicator.UNKNOWN
   *                        DeployCommunicator.IMPLICIT_STOPPED
   * @see com.sap.engine.services.deploy.container.ContainerInterface#applicationStatusChanged(java.lang.String, byte)
   */
  public void applicationStatusChanged(String applicationName, byte status) {
    try {
      //TODO This is only a workaround and MUST be think about correct way to register/unregister 'lazy' applications in/from HTTP provider and ICM.
      if (ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getStartUpMode(applicationName) != StartUp.LAZY.getId().byteValue()) {
        if ((status == Status.STARTED.getId().byteValue()) && sendAppStarted(applicationName)) {
          startedApplications.add(applicationName);
        } else if ((status == Status.STOPPED.getId().byteValue()) && sendAppStopped(applicationName)) {
          startedApplications.remove(applicationName);
        } else if ((status == Status.IMPLICIT_STOPPED.getId().byteValue()) && sendAppStopped(applicationName)) {
          startedApplications.remove(applicationName);
        }
      }
    } catch (RemoteException e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000102",
        "Cannot change load balancing for [{0}] application.", new Object[]{applicationName}, e, null, null);
    }
  }//end of applicationStatusChanged(String applicationName, byte status)

  /**
   * Start load balancing for the application
   * @param applicationName
   * @return
   */
  private boolean sendAppStarted(String applicationName) {
    String[] aliases = ServiceContext.getServiceContext().getDeployContext().getAliases(applicationName);
    if (aliases == null || aliases.length == 0) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000104",
        "Cannot start load balancing for the application [{0}].", new Object[]{applicationName}, null, null);
      return false;
    }

    HttpProvider httpProvider = ServiceContext.getServiceContext().getHttpProvider();
    if (httpProvider != null) {
      try {
        httpProvider.changeLoadBalance(applicationName, aliases, true);
      } catch (HttpShmException e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000105",
          "Cannot start load balancing for the application [{0}].", new Object[]{applicationName}, e, null, null);
        return false;
      }
    }

    return true;
  }//end of sendAppStarted(String applicationName)

  /**
   * Stop load balancing for the application.
   * @param applicationName
   * @return
   */
  private boolean sendAppStopped(String applicationName) {
    String[] aliases = ServiceContext.getServiceContext().getDeployContext().getAliases(applicationName);
    if (aliases == null || aliases.length == 0) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000106",
        "Cannot stop load balancing for the application [{0}].", new Object[]{applicationName}, null, null);
      return false;
    }

    HttpProvider httpProvider = ServiceContext.getServiceContext().getHttpProvider();
    if (httpProvider != null) {
      try {
        httpProvider.changeLoadBalance(applicationName, aliases, false);
      } catch (HttpShmException e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000038",
          "Cannot stop load balancing for the application [{0}].", new Object[]{applicationName}, e, null, null);
        return false;
      }
    } else {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000039",
        "HTTP Provider service is stopped and cannot stop load balancing for the application [{0}].", 
        new Object[]{applicationName}, null, null);
      return false;
    }

    return true;
  }//end of sendAppStopped(String applicationName)

  /**
   * Time Statistic listeners.
   * @see com.sap.engine.services.deploy.container.ContainerInterface#addProgressListener(com.sap.engine.services.deploy.container.ProgressListener)
   */
  public void addProgressListener(ProgressListener listener) {
  }//end of addProgressListener(ProgressListener listener)

  /**
   * @see com.sap.engine.services.deploy.container.ContainerInterface#removeProgressListener(com.sap.engine.services.deploy.container.ProgressListener)
   */
  public void removeProgressListener(ProgressListener listener) {
  }//end of removeProgressListener(ProgressListener listener)

  
  //DeployCallback

  /**
   * Take into account only in initial starting apps finished and before triggering
   * of applications stop.
   */
  public void processApplicationEvent(DeployEvent event) {
    if (LogContext.getLocationDeploy().beInfo()) {
      LogContext.getLocationDeploy().infoT("Process application event will begin on Web Container, event[" + event + "].");
    }
    
    deployCallbackAction.processApplicationEvent(event, mBeanProvider);
  }//end of processApplicationEvent(DeployEvent event)

  /**
   * @see com.sap.engine.services.deploy.DeployCallback#processServiceEvent(com.sap.engine.services.deploy.DeployEvent)
   */
  public void processServiceEvent(DeployEvent event) {
  }//end of processServiceEvent(DeployEvent event)

  /**
   * @see com.sap.engine.services.deploy.DeployCallback#processLibraryEvent(com.sap.engine.services.deploy.DeployEvent)
   */
  public void processLibraryEvent(DeployEvent event) {
  }//end of processLibraryEvent(DeployEvent event)

  /**
   * @see com.sap.engine.services.deploy.DeployCallback#processInterfaceEvent(com.sap.engine.services.deploy.DeployEvent)
   */
  public void processInterfaceEvent(DeployEvent event) {
  }//end of processInterfaceEvent(DeployEvent event)

  /**
   * @see com.sap.engine.services.deploy.DeployCallback#processContainerEvent(com.sap.engine.services.deploy.container.ProgressEvent)
   */
  public void processContainerEvent(ProgressEvent event) {
  }//end of processContainerEvent(ProgressEvent event)

  /**
   * @see com.sap.engine.services.deploy.DeployCallback#processReferenceEvent(com.sap.engine.services.deploy.DeployEvent)
   */
  public void processReferenceEvent(DeployEvent event) {
  }//end of processReferenceEvent(DeployEvent event)

  /**
   * @see com.sap.engine.services.deploy.DeployCallback#processStandaloneModuleEvent(com.sap.engine.services.deploy.DeployEvent)
   */
  public void processStandaloneModuleEvent(DeployEvent event) {
    if (LogContext.getLocationDeploy().beInfo()) {
      LogContext.getLocationDeploy().infoT("Process standalone module event will begin on Web Container, event[" + event + "].");
    }
    
    if (mBeanProvider != null) {
      deployCallbackAction.processDeployEventForMBeans(event, mBeanProvider);
    }
  }//end of processStandaloneModuleEvent(DeployEvent event)

  /**
   * @see com.sap.engine.services.deploy.DeployCallback#callbackLost(java.lang.String)
   */
  public void callbackLost(String s) {
  }//end of callbackLost(String s)

  /**
   * @see com.sap.engine.services.deploy.DeployCallback#serverAdded(java.lang.String)
   */
  public void serverAdded(String s) {
  }//end of serverAdded(String s)

  /**
   * @return Returns the webContainerHelper.
   */
  public WebContainerHelper getWebContainerHelper() {
    return webContainerHelper;
  }
  
  public void setWebContainerLazyMBeanProvider(WebContainerLazyMBeanProvider _mBeanProvider) {
    mBeanProvider = _mBeanProvider;
  }
  
  public WebContainerLazyMBeanProvider getWebContainerLazyMBeanProvider() {
    return mBeanProvider;
  }
}//end of class
