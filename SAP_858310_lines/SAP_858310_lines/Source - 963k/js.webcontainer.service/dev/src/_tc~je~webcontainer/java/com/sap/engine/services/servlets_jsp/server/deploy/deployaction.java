/*
 * Copyright (c) 2004-2009 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.deploy;

import static com.sap.engine.services.httpserver.lib.ParseUtils.separator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.frame.core.configuration.addons.PropertySheet;
import com.sap.engine.lib.descriptors.webj2eeengine.SpecVersionType;
import com.sap.engine.lib.descriptors.webj2eeengine.WebJ2EeEngineType;
import com.sap.engine.lib.descriptors5.javaee.MessageDestinationLinkType;
import com.sap.engine.lib.descriptors5.javaee.MessageDestinationRefType;
import com.sap.engine.lib.descriptors5.javaee.MessageDestinationType;
import com.sap.engine.lib.descriptors5.javaee.XsdStringType;
import com.sap.engine.lib.descriptors5.web.WebAppType;
import com.sap.engine.lib.io.FileUtils;
import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.deploy.container.ApplicationDeployInfo;
import com.sap.engine.services.deploy.container.ContainerDeploymentInfo;
import com.sap.engine.services.deploy.container.ContainerInfo;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.deploy.container.WarningException;
import com.sap.engine.services.deploy.container.op.util.FailOver;
import com.sap.engine.services.deploy.container.op.util.FileType;
import com.sap.engine.services.deploy.container.op.util.J2EEModuleType;
import com.sap.engine.services.deploy.container.op.util.ModuleInfo;
import com.sap.engine.services.deploy.container.op.util.StartUp;
import com.sap.engine.services.ejb3.model.MessageDestination;
import com.sap.engine.services.ejb3.model.Module;
import com.sap.engine.services.httpserver.interfaces.HttpProvider;
import com.sap.engine.services.httpserver.interfaces.exceptions.HttpShmException;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.WebContainerInterface;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.WebDeploymentDescriptor;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.converter.WebConverter;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.DeployInfoExtension;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.IWebContainer;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.WebContainerProvider;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.module.WebModule;
import com.sap.engine.services.servlets_jsp.server.deploy.util.ConfigurationUtils;
import com.sap.engine.services.servlets_jsp.server.deploy.util.Constants;
import com.sap.engine.services.servlets_jsp.server.deploy.util.SecurityUtils;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIOException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebResourceAccessor;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebWarningException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.container.ReferenceObjectImpl;
import com.sap.engine.services.servlets_jsp.webcontainer_api.container.ResourceReference;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Location;

/**
 * This class is responsible for application deployment.
 * @author Violeta Georgieva
 * @version 7.10
 */
public final class DeployAction extends ActionBase {
  private static final Location currentLocation = Location.getLocation(DeployAction.class);
  private static final Location traceLocation = LogContext.getLocationDeploy();
  private static final String WS_END_POINT_KEY = "WsEndPoints";

  /**
   * @param iWebContainer
   * @param containerInfo
   * @param webContainerHelper
   * @param runtimeInterface
   */
  public DeployAction(IWebContainer iWebContainer, ContainerInfo containerInfo, WebContainerHelper webContainerHelper, WebContainerInterface runtimeInterface) {
    super(iWebContainer, containerInfo, webContainerHelper, runtimeInterface);
  }//end of constructor

  /**
   * Deploys an array of jars which contains the components for the service container.
   *
   * @param archiveFilesFromDeploy all files in ear.
   * @param dInfo all info needed by containers connected with application global properties.
   * @param props application global properties that can be changed and later saved in the configuration.
   * @param isUpdate if false it is deploy action, otherwise it is update.
   * @param addedAliases only during update is valid, and contains the newly added aliases.
   * @return
   * @throws DeploymentException
   */
  public ApplicationDeployInfo deploy(File[] archiveFilesFromDeploy, ContainerDeploymentInfo dInfo, Properties props, boolean isUpdate, String[] addedAliases) throws DeploymentException {
    Vector warnings = new Vector();

    String debugInfo = (!isUpdate) ? "deploy" : "makeUpdate";

    AppMetaData appMetaData = new AppMetaData();

    String applicationName = dInfo.getApplicationName();
    // Deploy configuration for the whole application
    Configuration appConfig = dInfo.getConfiguration();
    // Servlet_jsp configuration used by WebContainer
    Configuration servlet_jspConfig = ConfigurationUtils.createSubConfiguration(appConfig, containerInfo.getName(), applicationName, isUpdate);
    // servlet_jsp/update - contains application data changed during dingle file update and so on.
    Configuration servlet_jspUpdate = ConfigurationUtils.createSubConfiguration(servlet_jspConfig, Constants.UPDATE, applicationName, false);
    // servlet_jsp/backup - stores application info during convert - the original deployment descriptors.
    Configuration servlet_jspBackup = ConfigurationUtils.createSubConfiguration(servlet_jspConfig, Constants.BACKUP, applicationName, false);

    ConfigurationUtils.addConfigEntry(servlet_jspUpdate, Constants.FILE_COUNTER, "1", applicationName, false, false);

    storeFailOverToDB(dInfo, servlet_jspConfig);

    // mapping between URI and abs path of the archives <abs_path:URI>
    Hashtable fileMappings = dInfo.getFileMappings();

    //This vector contains all deployed applications, which have components for our container.
    String[] deployedApplications = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getMyApplications();

    String[] allAliases = WebContainerHelper.getAliases(applicationName, props);

    String[] aliasesCanonicalized = new String[allAliases.length];
    File[] aliasesRootDirs = new File[allAliases.length];
    // maps alias to archive
    for (int i = 0; i < allAliases.length; i++) {
      String warUri = props.getProperty("web:" + allAliases[i]);
      int index = getIndexForArchive(archiveFilesFromDeploy, warUri, fileMappings);
      warUri = warUri.replace('\\', '/');

      String aliasCanonicalized = ParseUtils.convertAlias(allAliases[i]);
      aliasesCanonicalized[i] = aliasCanonicalized;
      String aliasDir = WebContainerHelper.getAliasDirName(aliasCanonicalized);
      String tagName = null;

      String webApplWorkDirName = WebContainerHelper.getDirName(new String[]{WebContainerHelper.getDeployTempDir(applicationName), aliasDir});
      aliasesRootDirs[i] = new File(webApplWorkDirName + "root" + File.separator);
      File webApplWorkDir = new File(webApplWorkDirName);
      if (!webApplWorkDir.exists()) {
        if (!webApplWorkDir.mkdirs()) {
          if (!webApplWorkDir.mkdirs()) {
            throw new WebDeploymentException(WebIOException.CANNOT_MAKE_DIRS, new Object[]{webApplWorkDir.getAbsolutePath()});
          }
        }
      }

      //This map contains all descriptors cached during JLinEE application validation
      Map cache = dInfo.getCache(containerInfo.getName(), archiveFilesFromDeploy[index].getAbsolutePath());
      if (cache != null) {
    	  ArrayList<String> wsEndPoints = (ArrayList<String>)cache.get(WS_END_POINT_KEY);
          if (wsEndPoints != null) {
            try {
            	storeWsEndPointsToDB(wsEndPoints, servlet_jspConfig, aliasDir);
            } catch (Exception e) {
            	LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000673", "Servlet based web service end points of web application [{0}] cannot be stored to the data base. Future requests to these end points will fail.", new Object[]{aliasDir}, null,null);
            }
          }
      } else {
          throw new WebDeploymentException(WebDeploymentException.CACHE_IS_EMPTY);
      }
      try {
        if (Accounting.isEnabled()) { //ACCOUNTING.start - BEGIN
          tagName = debugInfo + "/convertWAR (" + aliasCanonicalized + ")";
          Accounting.beginMeasure(tagName, WebConverter.class);
        }//ACCOUNTING.start - END
        ServiceContext.getServiceContext().getDeployContext().getWebConverter().convertWAR(archiveFilesFromDeploy[index], warUri, servlet_jspBackup, "simple", cache);
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
        throw new WebDeploymentException(WebDeploymentException.EXCEPTION_OCCURED_WHILE_CONVERTING_WAR,
          new Object[]{warUri, applicationName}, e);
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      }//ACCOUNTING.end - END

      String warName = warUri.substring(warUri.lastIndexOf('/') + 1);

      File tempWarFile = new File(webApplWorkDir, "temp_" + warName);
      if (tempWarFile.exists()) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000029",
          "WAR file [{0}] exists on the file system.", new Object[]{tempWarFile.getName()}, null, null);
      }

      try {
        if (Accounting.isEnabled()) { //ACCOUNTING.start - BEGIN
          tagName = debugInfo + "/copyWAR (" + aliasCanonicalized + ")";
          Accounting.beginMeasure(tagName, FileUtils.class);
        }//ACCOUNTING.start - END

        FileUtils.copyFile(archiveFilesFromDeploy[index], tempWarFile);

        if (traceLocation.beDebug()) {
          traceLocation.debugT("WAR file for [" + aliasCanonicalized + "] web application is copied to the file system.");
        }

        if (!tempWarFile.canWrite()) {
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000030",
            "File.canWrite() returns false for file [{0}] after FileUtils.copyFile() invocation.",
            new Object[]{tempWarFile.getName()}, null, null);
        }
      } catch (IOException io) {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_COPY_FILE_IN_WORK_FOLDER_OF_APPLICATION,
          new Object[]{archiveFilesFromDeploy[index].getPath(), applicationName}, io);
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      }//ACCOUNTING.end - END

      extractWar(applicationName, webApplWorkDirName, tempWarFile, debugInfo);

      if (traceLocation.beDebug()) {
      	traceLocation.debugT("WAR file for [" + aliasCanonicalized + "] web application is extracted to the file system.");
      }

      if (!tempWarFile.canWrite()) {
        if (traceLocation.beWarning()) {
        	LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000513",
        			"File.canWrite() returns false for file [{0}] after JarExtractor.extractJar() invocation.", new Object[]{tempWarFile.getName()}, null, null);
        }
      }

      File warFile = new File(webApplWorkDir, warName);
      if (warFile.exists()) {
        if (traceLocation.beInfo()) {
        	traceLocation.infoT("WAR file [" + warFile.getName()
					+ "] exists on the file system. Web Container service will try to delete it.");
        }
        if (!warFile.delete()) {
		      if (traceLocation.beWarning()) {
		      	LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000233",
									"Cannot delete [{0}] file.", new Object[]{warFile.getName()}, null, null);
          }
        }
      }

      if (!tempWarFile.renameTo(warFile)) {
        if (!tempWarFile.canWrite()) {
          if (traceLocation.beWarning()) {
          	LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000234",
          			"File.canWrite() returns false for file [{0}] after JarExtractor.extractJar() invocation.", new Object[]{tempWarFile.getName()}, null, null);
          }
        }

        if (!tempWarFile.renameTo(warFile)) {
           throw new WebDeploymentException(WebIOException.CANNOT_RENAME_WAR_FILE,
            new Object[]{"temp_" + warName, applicationName});
        }
      }

      // stores under war URI
      try { //ACCOUNTING.start - BEGIN
        if (Accounting.isEnabled()) {
          tagName = debugInfo + "/storeWARToDBase (" + warUri + ")";
          Accounting.beginMeasure(tagName, Configuration.class);
        }//ACCOUNTING.start - END
        ConfigurationUtils.addFileEntryByKey(servlet_jspConfig, warUri.replace('/', '_'), warFile, applicationName, true, true);
        if (traceLocation.beDebug()) {
        	traceLocation.debugT("WAR file for [" + aliasCanonicalized + "] web application is stored to database.");
        }
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_STORE_FILE_FROM_APPLICATION_IN_DATABASE,
          new Object[]{warFile.getPath(), applicationName}, e);
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      }//ACCOUNTING.end - END

      // stores the alt DD and the additional DD in DB
      try { //ACCOUNTING.start - BEGIN
        if (Accounting.isEnabled()) {
          tagName = debugInfo + "/storeAltDDToDB (" + aliasCanonicalized + ")";
          Accounting.beginMeasure(tagName, DeployAction.class);
        }//ACCOUNTING.start - END
        storeAltDDToDB(dInfo, servlet_jspConfig, aliasCanonicalized, webApplWorkDirName);
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      }//ACCOUNTING.end - END

      warnings = initXmls(applicationName, allAliases[i], appConfig, servlet_jspConfig, dInfo, isUpdate,
        addedAliases, archiveFilesFromDeploy[index].getAbsolutePath(), appMetaData);

      // check in deploy whether the same alias exists for another application
      for (int j = 0; deployedApplications != null && j < deployedApplications.length; j++) {
        //get canonicalized aliases
        String[] deployedAliases = ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalized(deployedApplications[j]);
        for (int k = 0; deployedAliases != null && k < deployedAliases.length; k++) {
          if (aliasCanonicalized.equals(deployedAliases[k])) {
            //VillyU
            if (aliasCanonicalized.equalsIgnoreCase(separator) && applicationName.equalsIgnoreCase(WebContainer.sapDefaultApplName) && hasCustomerDefaultApplication()) {
              // do not rollback the deploy with already used alias because
              // the default alias (of sap default application) is deploying; there is a custom default application deployed on the system
              continue;
            } else if (!dInfo.getApplicationName().equals(deployedApplications[j])) {
              //roll-back due to already used alias!! (the same alias but different application name)
              throw new WebDeploymentException(WebDeploymentException.APPLICATION_ALIAS_FOR_APPLICATION_ALREADY_EXISTS_IN_HTTP_SERVICE,
                  new Object[]{aliasCanonicalized, dInfo.getApplicationName(), deployedApplications[j]});
            } else {
                break;
            }
          }
        }
      }

      // check in HTTP whether the same alias exists for another application and fix HTTP config if needed
      checkAliasInHttp(applicationName, aliasCanonicalized);
    }

    Properties appGlobalProps = new Properties();
    String startUpMode = StartUp.LAZY.getName(); //default value for standalone archive
    if (!dInfo.isStandAloneArchive()) {
      startUpMode = dInfo.getEarDescriptor().getStartUpO().getName();
    }
    appGlobalProps.setProperty("startupmode", startUpMode);

    String failOver = FailOver.DISABLE.getName(); //default value for standalone archive
    if (!dInfo.isStandAloneArchive()) {
      failOver = dInfo.getFailOver().getName();
    }
    appGlobalProps.setProperty("failover", failOver);

    DeployInfoExtension deployInfo = null;

    // pass servlet_jsp configuration in deploy()
    deployInfo = iWebContainer.deploy(applicationName, aliasesCanonicalized, aliasesRootDirs, isUpdate, appGlobalProps, servlet_jspConfig);

    if (traceLocation.beDebug() && deployInfo != null) {
    	traceLocation.debugT("Deployment information returned by the WCE providers.");
    }

    ReferenceObjectImpl[] publicReferences = deployInfo.getPublicReferences();
    ReferenceObjectImpl[] privateReferences = deployInfo.getPrivateReferences();
    Vector publicResourceReferences = deployInfo.getPublicResourceReferences();
    Vector privateResourceReferences = deployInfo.getPrivateResourceReferences();
    Hashtable deployedResource_Types = deployInfo.getDeployedResources_Types();
    Hashtable wceInDeploy = deployInfo.getWceInDeploy();
    WarningException warningExc = deployInfo.getWarningException();

    if (warningExc != null) {
      warnings.addElement(warningExc);
    }

    Vector<String> files = new Vector<String>();
    String[] fixedFilesForPrivateCL = getFilesForClassPath(applicationName, aliasesCanonicalized, true);
    if (fixedFilesForPrivateCL != null) {
      files.addAll(Arrays.asList(fixedFilesForPrivateCL));
    }
    if (files.size() > 0) {
      // removes absolute path, throw exception if no relative path - context root
      Vector<String> relativeFilePaths = getRelativePaths(applicationName, files);
      try {
        storeFilesForPrivateCL((String[]) relativeFilePaths.toArray(new String[relativeFilePaths.size()]), servlet_jspConfig);
      } catch (ConfigurationException e) {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_STORE_FILES_FOR_PRIVATE_CL, new Object[]{applicationName}, e);
      }
    }

    if (!wceInDeploy.isEmpty()) {
      try {
        storeWCEInDeploy(wceInDeploy, servlet_jspConfig);
      } catch (ConfigurationException e) {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_STORE_WCE_IN_DEPLOY, new Object[]{applicationName}, e);
      }
      storeWCEInDeploy(wceInDeploy, appMetaData);
    }

    ApplicationDeployInfo info = new ApplicationDeployInfo();
    info.setDeployedComponentNames(allAliases);

    info.addFilesForClassloader(getFilesForClassPath(applicationName, aliasesCanonicalized, false));

    info.setDeployProperties(props);

    for (int i = 0; i < aliasesCanonicalized.length; i++) {
      Properties resourceRef = null;
      if ((resourceRef = (Properties) mapAliasResourceRef.get(aliasesCanonicalized[i])) != null) {
        Enumeration en = resourceRef.keys();
        while (en.hasMoreElements()) {
          String resourceName = (String) en.nextElement();
          String resourceType = resourceRef.getProperty(resourceName);
          info.addResourceReference(resourceName, resourceType);
        }
        mapAliasResourceRef.remove(aliasesCanonicalized[i]);
      }
    }

    for (int i = 0; i < publicResourceReferences.size(); i++) {
      ResourceReference resourceReference = (ResourceReference) publicResourceReferences.get(i);
      info.addResourceReference(resourceReference.getResourceName(), resourceReference.getResourceType(), resourceReference.getReferenceType(), true, true);
    }

    if (privateResourceReferences.size() > 0) {
      for (int i = 0; i < privateResourceReferences.size(); i++) {
        ResourceReference resourceReference = (ResourceReference) privateResourceReferences.get(i);
        info.addResourceReference(resourceReference.getResourceName(), resourceReference.getResourceType(), resourceReference.getReferenceType(), true, false);
      }

      try {
        storeResRefs(privateResourceReferences, servlet_jspConfig);
      } catch (ConfigurationException e) {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_STORE_PRIVATE_RES_REF, new Object[]{applicationName}, e);
      }
    }

    Enumeration enumeration = deployedResource_Types.keys();
    while (enumeration.hasMoreElements()) {
      String component = (String) enumeration.nextElement();
      info.addDeployedResource_Types(component, (String[]) deployedResource_Types.get(component));
    }

    Hashtable<String, Hashtable<String, Vector<String>>> resourcesPerModule =
      (Hashtable<String, Hashtable<String, Vector<String>>>) ((WebContainerProvider) iWebContainer).getResourcesPerApplication().get(applicationName);
    if (resourcesPerModule != null) {
      try {
        storeDeployedResourcesPerModule(resourcesPerModule, servlet_jspConfig);
      } catch (ConfigurationException e) {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_STORE_PROVIDED_RESOURCES, new Object[]{applicationName}, e);
      }
      storeDeployedResourcesPerModule(resourcesPerModule, appMetaData);
    }

    Vector<ReferenceObjectImpl> references = new Vector<ReferenceObjectImpl>();
    if (publicReferences != null) {
      references.addAll(Arrays.asList(publicReferences));
    }
    // not implemented, act as public references
    if (privateReferences != null) {
      references.addAll(Arrays.asList(privateReferences));
    }
    if (references.size() > 0) {
      info.setReferences((ReferenceObjectImpl[]) references.toArray(new ReferenceObjectImpl[references.size()]));
    }

    if (warnings != null && warnings.size() != 0) {
      info.addWarning(warnings.toString());
    }

    storeAppMetaDataObject2DBase(appMetaData, servlet_jspConfig, applicationName);

    return info;
  }//end of deploy(File[] archiveFilesFromDeploy, ContainerDeploymentInfo dInfo, Properties props, boolean isUpdate, String[] addedAliases)

  /**
   * This is a confirmation of that the deploy of all components from the initial application
   * is completed with success and components can be initiated and are available to use.
   *
   * @param applicationName
   * @throws WarningException
   */
  public void commitDeploy(String applicationName) throws WarningException {
    if (applicationName.equalsIgnoreCase(WebContainer.sapDefaultApplName) && hasCustomerDefaultApplication()) {
      return;
    }
    String aliasesCanonicalized[] = ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalized(applicationName);
    if (aliasesCanonicalized != null) {
      registerAllAliasesInHttp(applicationName, aliasesCanonicalized, true);
    }

    //TODO This is only a workaround and MUST be think about correct way to register 'lazy' applications in HTTP provider and ICM.
    try {
      if (ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getStartUpMode(applicationName) == StartUp.LAZY.getId().byteValue()) {
        HttpProvider httpProvider = ServiceContext.getServiceContext().getHttpProvider();
        if (httpProvider != null) {
          String[] aliases = ServiceContext.getServiceContext().getDeployContext().getAliases(applicationName);
          if (aliases != null && aliases.length > 0) {
            try {
              httpProvider.changeLoadBalance(applicationName, aliases, true);
            } catch (HttpShmException she) {
              LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000082",
                "Cannot start load balancing for 'Lazy' application [{0}].", new Object[]{applicationName}, she, null, null);
            }
            ServiceContext.getServiceContext().getWebContainer().startedApplications.add(applicationName);
          } else {
            LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000083",
              "Cannot start load balancing for 'Lazy' application [{0}].", new Object[]{applicationName}, null, null);
          }
        }
      }
      // this is enough as deploy will call application start afterwards
    } catch (RemoteException e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000084",
        "Cannot notify HTTP Provider for 'Lazy' application [{0}].", new Object[]{applicationName}, e, null, null);
    }

    // TODO left from 640, check to remove
    try {
      runtimeInterface.update(applicationName);
    } catch (RemoteException e) {
        throw new WebWarningException(WebWarningException.CANNOT_NOTIFY_FOR_DEPLOYING_APPLICATION,
    	        new Object[]{applicationName, e.toString()});

    } catch (RuntimeException e) {
        throw new WebWarningException(WebWarningException.CANNOT_NOTIFY_FOR_DEPLOYING_APPLICATION,
    	        new Object[]{applicationName, e.toString()});
    }
  }//end of commitDeploy(String applicationName)

  /**
   * @param applicationName
   * @throws WarningException
   */
  public void rollbackDeploy(String applicationName) throws WarningException {
    if ((applicationName != null) && (!applicationName.equals(""))) {
      try {
        remove(applicationName, false, null); // remove from deploy
      } catch (DeploymentException e) {
        throw new WebWarningException(WebWarningException.CANNOT_REMOVE_APPLICATION_DURING_DEPLOY_ROLLBACK,
          new Object[]{applicationName}, e);
      }
    }
  }//end of rollbackDeploy(String applicationName)

  /**
   * Called on other server nodes
   * @param applicationName
   * @param props
   * @throws WarningException
   */
  public void notifyDeployedComponents(String applicationName, Properties props) throws WarningException {
    String aliasesCanonicalized[] = ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalized(applicationName);
    for (int i = 0; aliasesCanonicalized != null && i < aliasesCanonicalized.length; i++) {
      registerAliasInHttp(applicationName, aliasesCanonicalized[i], false); // only runtime structures to be updated
    }

    prepareApplicationInfo(applicationName, false);

    //TODO This is only a workaround and MUST be think about correct way to register 'lazy' applications in HTTP provider and ICM.
    try {
      if (ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getStartUpMode(applicationName) == StartUp.LAZY.getId().byteValue()) {
        HttpProvider httpProvider = ServiceContext.getServiceContext().getHttpProvider();
        if (httpProvider != null) {
          String[] aliases = ServiceContext.getServiceContext().getDeployContext().getAliases(applicationName);
          if (aliases != null && aliases.length > 0) {
            try {
              httpProvider.changeLoadBalance(applicationName, aliases, true);
            } catch (HttpShmException she) {
              LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000085",
                "Cannot start load balancing for 'Lazy' application [{0}].", new Object[]{applicationName}, she, null, null);
            }
            ServiceContext.getServiceContext().getWebContainer().startedApplications.add(applicationName);
          } else {
            LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000086",
              "Cannot start load balancing for 'Lazy' application [{0}].", new Object[]{applicationName}, null, null);
          }
        }
      }
    } catch (RemoteException e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000087",
        "Cannot notify HTTP Provider for 'Lazy' application [{0}].", new Object[]{applicationName}, e, null, null);
    }
  }//end of notifyDeployedComponents(String applicationName, Properties props)

  /**
   * Return the index in array for the given element.
   *
   * @param archiveFilesFromDeploy
   * @param warUri
   * @param fileMappings
   * @return
   * @throws DeploymentException
   */
  private int getIndexForArchive(File[] archiveFilesFromDeploy, String warUri, Hashtable fileMappings) throws DeploymentException {
    for (int i = 0; i < archiveFilesFromDeploy.length; i++) {
      if ((fileMappings.get(archiveFilesFromDeploy[i].getAbsolutePath())).equals(warUri)) {
        return i;
      }
    }
    throw new WebDeploymentException(WebDeploymentException.INCORRECT_PROPERTIES_FOR_DEPLOY_CANNOT_FIND_WAR_ARCHIVE_WITH_NAME,
      new Object[]{warUri});
  }//end of getIndexForArchive(File[] archiveFilesFromDeploy, String warUri, Hashtable fileMappings)

  public static void storeWCEInDeploy(Hashtable wceInDeploy, Configuration config) throws ConfigurationException {
    Configuration wceInDeployConfig = config.createSubConfiguration(Constants.WCE_IN_DEPLOY, Configuration.CONFIG_TYPE_PROPERTYSHEET);
    PropertySheet propertySheet = wceInDeployConfig.getPropertySheetInterface();

    Properties props = new Properties();
    Enumeration enumeration = wceInDeploy.keys();
    while (enumeration.hasMoreElements()) {
      String moduleName = (String) enumeration.nextElement();
      Vector wces = (Vector) wceInDeploy.get(moduleName);
      props.setProperty(moduleName, WebContainerHelper.vectorToString(wces));
    }

    propertySheet.createPropertyEntries(props);
  }//end of storeWCEInDeploy(Vector wceInDeploy, Configuration config)

  private static void storeWCEInDeploy(Hashtable wceInDeploy, AppMetaData appMetaData) {
    appMetaData.setWceInDeployPerModule(wceInDeploy);
  }//end of storeWCEInDeploy(Hashtable wceInDeploy, AppMetaData appMetaData)

  private void storeFilesForPrivateCL(String[] filesForPrivateCL, Configuration config) throws ConfigurationException {
    Configuration filesForCLConfig = config.createSubConfiguration(Constants.FILES_FOR_PRIVATE_CL, Configuration.CONFIG_TYPE_PROPERTYSHEET);
    PropertySheet propertySheet = filesForCLConfig.getPropertySheetInterface();

    Properties props = new Properties();
    for (int i = 0; i < filesForPrivateCL.length; i++) {
      props.setProperty("" + i, filesForPrivateCL[i]);
    }

    propertySheet.createPropertyEntries(props);
  }//end of storeFilesForPrivateCL(String[] filesForPrivateCL, Configuration config)

  private void storeResRefs(Vector resourceReferences, Configuration config) throws ConfigurationException {
    Configuration resRefsConfig = config.createSubConfiguration(Constants.PRIVATE_RESOURCE_REFERENCES, Configuration.CONFIG_TYPE_PROPERTYSHEET);
    PropertySheet propertySheet = resRefsConfig.getPropertySheetInterface();

    Properties props = new Properties();
    for (int i = 0; i < resourceReferences.size(); i++) {
      props.setProperty("resource_name_" + i, ((ResourceReference) resourceReferences.get(i)).getResourceName());
      props.setProperty("resource_type_" + i, ((ResourceReference) resourceReferences.get(i)).getResourceType());
      props.setProperty("reference_type_" + i, ((ResourceReference) resourceReferences.get(i)).getReferenceType());
    }

    propertySheet.createPropertyEntries(props);
  }//end of storeResRefs(Vector resourceReferences, Configuration config)

  private void storeDeployedResourcesPerModule(Hashtable resourcesPerModule, Configuration config) throws ConfigurationException {
    Enumeration enumeration = resourcesPerModule.keys();
    while (enumeration.hasMoreElements()) {
      String moduleName = (String) enumeration.nextElement();
      Configuration moduleConfig = config.createSubConfiguration((WebContainerHelper.getAliasDirName(moduleName)).replace('/', '_').replace('\\', '_'), Configuration.CONFIG_TYPE_PROPERTYSHEET);
      PropertySheet propertySheet = moduleConfig.getPropertySheetInterface();

      Hashtable deployedResources = (Hashtable) resourcesPerModule.get(moduleName);
      Properties props = new Properties();
      Enumeration en = deployedResources.keys();
      while (en.hasMoreElements()) {
        String resourceName = (String) en.nextElement();
        Vector resourceTypes = (Vector) deployedResources.get(resourceName);
        for (int i = 0; i < resourceTypes.size(); i++) {
          props.setProperty(i + "_" + resourceName, (String) resourceTypes.get(i));
        }
      }

      propertySheet.createPropertyEntries(props);
    }
  }//end of storeDeployedResourcesPerModule(Hashtable resourcesPerModule, Configuration config)

  private void storeDeployedResourcesPerModule(Hashtable resourcesPerModule, AppMetaData appMetaData) {
    appMetaData.setResourcesPerModule(resourcesPerModule);
  }

  private Vector<String> getRelativePaths(String applicationName, Vector<String> absolutePaths) throws DeploymentException {
    Vector<String> relativePaths = new Vector<String>();
    String appDir = WebContainerHelper.getDeployTempDir(applicationName);
    Enumeration enumeration = absolutePaths.elements();
    while (enumeration.hasMoreElements()) {
      String filePath = (String) enumeration.nextElement();
      if (filePath.startsWith(appDir)) {
        filePath = filePath.substring(appDir.length());
      } else {
       throw new WebDeploymentException(WebDeploymentException.CANNOT_REGISTER_FILES_FOR_PRVATE_CL, new Object[]{applicationName});
      }
      relativePaths.add(filePath);
    }
    return relativePaths;
  }//end of getRelativePaths(String applicationName, Vector<String> absolutePaths)

  /**
   * Create security resources and set resource references for the application
   * from global-web.xml and local web.xml.
   * The local web.xml is with high priority.
   *
   * @param applicationName
   * @param alias
   * @param appConfig
   * @param servlet_jspConfig
   * @param dInfo
   * @param isUpdate
   * @param addedAliases
   * @param warAbsolutePath
   * @throws DeploymentException
   */
  private Vector initXmls(String applicationName, String alias, Configuration appConfig, Configuration servlet_jspConfig,
      ContainerDeploymentInfo dInfo, boolean isUpdate, String[] addedAliases, String warAbsolutePath,
      AppMetaData appMetaData) throws DeploymentException {
    Vector warnings = new Vector();

    String debugInfo = (!isUpdate) ? "deploy" : "makeUpdate";

    String aliasCanonicalized = ParseUtils.convertAlias(alias);
    String aliasDir = WebContainerHelper.getAliasDirName(aliasCanonicalized);

    WebDeploymentDescriptor globalWebDesc = ServiceContext.getServiceContext().getDeployContext().getGlobalDD();
    WebDeploymentDescriptor webDesc = new WebDeploymentDescriptor();
    WebDeploymentDescriptor webAnnotationsDesc = null;
    Map cache = dInfo.getCache(containerInfo.getName(), warAbsolutePath);
    if (cache != null) {
      if (cache.get(WebConverter.WEB_FILENAME) != null) {
        try {
          webDesc.setWebApp((WebAppType) cache.get(WebConverter.WEB_FILENAME));
        } catch (Exception e) {
          throw new WebDeploymentException(WebDeploymentException.CANNOT_READ_XML_OR_ERROR_IN_XML_FOR_WEB_APPLICATION,
            new Object[]{"web.xml", aliasCanonicalized}, e);
        }
      } else {
        throw new WebDeploymentException(WebDeploymentException.WEB_XML_NOT_IN_CACHE);
      }

      if (cache.get(WebConverter.WEBJ2EE_FILENAME) != null) {

    	// check if application cookie and session cookie are configured in the local descriptor, set flags if so
       	org.w3c.dom.Document  web_j2ee_doc = (org.w3c.dom.Document) cache.get("DOCUMENT_" + WebConverter.WEBJ2EE_FILENAME);
       	if (web_j2ee_doc!=null){
       		int cookiesConfigured = checkConfiguredCookies(web_j2ee_doc);
       		switch (cookiesConfigured) {
       		case 1:
       			webDesc.setSessCookieConfigured(true);
       			break;
       		case 2:
       			webDesc.setAppCookieConfigured(true);
       			break;
       		case 3:
       			webDesc.setSessCookieConfigured(true);
       			webDesc.setAppCookieConfigured(true);
       			break;
       		default:
       			//flags are by default set to false => no need to set them
       			break;
       		}
       		if(isMaxAgeConfigured(web_j2ee_doc)){
       			webDesc.setMaxSessionsTagConfigured(true);
       		}
       	} else{
       		LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000649",
       		  "Cannot load the Document representation of the web-j2ee-engine descriptor from the deploy cache." +
       		  "As a result any local configurations of the cookie-config and max-sessions elements will not take effect for the web application [{0}].",
       		  new Object[]{aliasCanonicalized}, null, null);
       	}
    	webDesc.setWebJ2EEEngine((WebJ2EeEngineType) cache.get(WebConverter.WEBJ2EE_FILENAME));
      } else {
        String additonalXmlFile = WebContainerHelper.getDirName(
          new String[]{WebContainerHelper.getDeployTempDir(applicationName), aliasDir, "root", "WEB-INF", "web-j2ee-engine.xml"});
        if (new File(additonalXmlFile).exists()) {
          //this can happen only in cases when the web application version is < 2.4 and there is no web-j2ee-engine.xml
          //then we generate a new one (web-j2ee-engine.xml) and set the web application version to 2.3
          webDesc.getWebJ2EEEngine().setSpecVersion(new SpecVersionType("2.3"));
        }
      }

      if (cache.get("@annotation-generated-web.xml@") != null) {
        try {
          webAnnotationsDesc = new WebDeploymentDescriptor();
          webAnnotationsDesc.setWebApp((WebAppType) cache.get("@annotation-generated-web.xml@"));
        } catch (Exception e) {
          throw new WebDeploymentException(WebDeploymentException.CANNOT_READ_XML_OR_ERROR_IN_XML_FOR_WEB_APPLICATION,
            new Object[]{"web-annotations.xml", aliasCanonicalized}, e);

        }
      }
    } else {
      throw new WebDeploymentException(WebDeploymentException.CACHE_IS_EMPTY);
    }

    //Check whether application is 1.5 compatible if yes
    //store web.xml and web-annotations.xml in the configuration
    if ("2.5".equals(webDesc.getWebAppVersion().getValue())) {
      fixWebModel(webDesc, dInfo, warAbsolutePath, warnings, applicationName);
      store2DBase(webDesc, webAnnotationsDesc, servlet_jspConfig, applicationName, aliasDir);
    }

    boolean newAlias = false;
    for (int i = 0; addedAliases != null && i < addedAliases.length; i++) {
      if ((ParseUtils.convertAlias(addedAliases[i])).equals(aliasCanonicalized)) {
        newAlias = true;
        break;
      }
    }

    String tagName = null;
    try {//ACCOUNTING.start - BEGIN
      if (Accounting.isEnabled()) {
        tagName = debugInfo + "/initXmls/createSecurityResources (" + aliasCanonicalized + ")";
        Accounting.beginMeasure(tagName, SecurityUtils.class);
      }//ACCOUNTING.start - END

      //Create security resources
      securityUtils.createSecurityResources(applicationName, aliasCanonicalized, appConfig,
        dInfo.getAppConfigurationHandler(), globalWebDesc, webDesc, warnings, newAlias);
    } finally {//ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        Accounting.endMeasure(tagName);
      }
    }//ACCOUNTING.end - END


    //Init resource references from global-web.xml
    setResourceReferences(aliasCanonicalized, globalWebDesc);
    //Init resource references from local web.xml
    setResourceReferences(aliasCanonicalized, webDesc);

    if (isUpdate) {
      // check whether something is change by the NWA
      Configuration servlet_jspAdmin = null;
      try {
        if (servlet_jspConfig.existsSubConfiguration(Constants.ADMIN)) {
          servlet_jspAdmin = servlet_jspConfig.getSubConfiguration(Constants.ADMIN);

          store2DBase(alias, applicationName, servlet_jspAdmin, webDesc);

          String newFlag = ServiceContext.getServiceContext().getServerId() + "_" + System.currentTimeMillis();
          ConfigurationUtils.addConfigEntry(servlet_jspAdmin, Constants.MODIFICATION_FLAG, newFlag, applicationName, true, true);
        }
      } catch (Exception e) {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_UPDATE_WEB_DD_IN_ADMIN, new Object[]{applicationName}, e);
      }
    }

    //Store web model as a structure in the configuration
    storeWebDDObject2DBase(webDesc, servlet_jspConfig, aliasDir);

    //Store "url-session-tracking" value in the configuration, needed during start service to register lazy aliases in ICM
    ConfigurationUtils.addConfigEntry(servlet_jspConfig, Constants.URL_SESSION_TRACKING + aliasDir, webDesc.getWebJ2EEEngine().getUrlSessionTracking(), alias, true, true);

    //  adds "url-session-tracking" value in AppMetaData
    appMetaData.getUrlSessionTrackingPerModule().put(aliasDir, webDesc.getWebJ2EEEngine().getUrlSessionTracking());

    return warnings;
  }//end of initXmls(String applicationName, String alias, Configuration appConfig, Configuration servlet_jspConfig,
   //                ContainerDeploymentInfo dInfo, boolean isUpdate, String[] addedAliases, String warAbsolutePath)


  /**
   * Checks in the Document representation of the web-j2ee-engine descriptor if any max-session tag is configured.
   * @param doc - org.w3c.dom.Document representation of the additional deployment descriptor of a web application
   * @return
   * 			true	- if there is max-sessions element in the provided document and its value is not an empty string
   * 			false	- if there is no such element, or if its value is an empty string or null
   */
  private boolean isMaxAgeConfigured(org.w3c.dom.Document doc){
	  NodeList nodes = doc.getElementsByTagName("max-sessions");
	  for(int i=0; i < nodes.getLength(); i++){
			  Node maxAgeTag = (nodes.item(i)!= null)? nodes.item(i).getFirstChild() : null;
		  	  if (maxAgeTag!=null && maxAgeTag.getNodeValue()!= null && !maxAgeTag.getNodeValue().equals("")){
		  		  return true;
		  	  }
		  }
	  return false;
  }

  /**
   * Checks in the Document representation of the web-j2ee-engine descriptor if any cookies are configured.
   * @param doc - org.w3c.dom.Document representation of the additional deployment descriptor of a web application
   * @return
   * 			0	- if none is configured
   * 			1	- if only session cookie is configured
   * 			2	- if only application cookie is configured
   * 			3	- if both session and application cookies are configured
   */
   private int checkConfiguredCookies(org.w3c.dom.Document doc){
	  int result = 0;
	  NodeList nodes = doc.getElementsByTagName("cookie");
	  //iterate over all <cookie> tags found
	  if(nodes != null){
		  for(int i=0; i < nodes.getLength(); i++){
			  	  NodeList childNodes = nodes.item(i).getChildNodes();
		  	  	  if (childNodes != null){
		  	  		  result++;
		  	  		  //iterate over <cookie> tag's subtags - childNodes;
		  	  		  //if type is explicitly set to APPLICATION then appCookie is configured in all other cases it is the sessCookie that is configured
		  	  		  for(int j=0; j< childNodes.getLength(); j++){
		  	  			  Node child = (childNodes.item(j) != null)? childNodes.item(j).getFirstChild() : null;
		  	  			  if (child != null && "type".equals(childNodes.item(j).getNodeName()) && "APPLICATION".equals(child.getNodeValue())){
			  	  		  	  result ++;
		  	  			  }
		  	  		  }
		  	  	  }
		  }
	  }
	  return result;
  }

  /**
   * Stores fail over value in the servlet_jsp configuration.
   * @param dInfo
   * @param configuration
   * @throws DeploymentException
   */
  private void storeFailOverToDB(ContainerDeploymentInfo dInfo, Configuration configuration) throws DeploymentException {
    String failOver = FailOver.DISABLE.getName(); //default value for standalone archive
    if (!dInfo.isStandAloneArchive()) {
      failOver = dInfo.getFailOver().getName();
    }
    ConfigurationUtils.addConfigEntry(configuration, Constants.FAIL_OVER, failOver, dInfo.getApplicationName(), true, true);
    if (traceLocation.beDebug()) {
      traceLocation.debugT("Fail over value for [" + dInfo.getApplicationName() + "] application is stored to database.");
    }
  }//end of storeFailOverToDB(ContainerDeploymentInfo dInfo, Configuration configuration)

  /**
   * Stores the list of all servlet based web service end points for an application in the servlet_jsp configuration.
   * @param wsEndPoints - a list of ws end points that are previously found (during JLinEE phase).
   * @param configuration - servlet_jsp configuration
   * @param aliasDir - it is of the web application that contains the ws end points
   * @throws DeploymentException
   */
  private void storeWsEndPointsToDB(ArrayList<String> wsEndPoints, Configuration config, String aliasDir)throws DeploymentException{
	    ObjectOutputStream objectOutputStream = null;
	    try {
	      ByteArrayOutputStream baos = new ByteArrayOutputStream();
	      objectOutputStream = new ObjectOutputStream(baos);
	      objectOutputStream.writeObject(wsEndPoints);
	      ConfigurationUtils.addFileAsStream(config, Constants.WS_END_POINTS + aliasDir, new ByteArrayInputStream(baos.toByteArray()), aliasDir, true, true);
	    } catch (IOException e) {
	    	LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceError("ASJ.web.000677",
	                "Cannot store to the data base the list of servlet based web service end points for web application [{0}].", new Object[]{aliasDir}, e, null, null);
	    }finally {
	      if (objectOutputStream != null) {
	        try {
	          objectOutputStream.close();
	        } catch (IOException e) {
	          if (traceLocation.beWarning()) {
	          	LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000670",
									"Error while closing ObjectOutputStream during deployment of [{0}] web application.", new Object[]{aliasDir}, e, null, null);
	          }
	        }
	      }
	    }
  }
  /**
   * Searches for alt descriptor to be stored in the DB in the place of the original web.xm descriptor.
   *
   * @param dInfo
   * @param configuration
   * @param aliasCanonicalized canonicalized alias
   * @param webApplWorkDirName
   * @throws DeploymentException
   */
  private void storeAltDDToDB(ContainerDeploymentInfo dInfo, Configuration configuration, String aliasCanonicalized, String webApplWorkDirName) throws DeploymentException {
    String webInfDir = WebContainerHelper.getDirName(new String[]{webApplWorkDirName, "root", "WEB-INF"});
    File webDD = new File(webInfDir + "web.xml");
    if (!webDD.exists()) {
      throw new WebDeploymentException(WebDeploymentException.THE_ARCHIVE_FOR_WEB_APPLICATION_DOES_NOT_CONTAIN_WEBINF_WEBXML_FILE_OR_IT_IS_SPELT_INCORRECTLY,
        new Object[]{aliasCanonicalized});
    }

    File addWebDD = new File(webInfDir + "web-j2ee-engine.xml");

    String applicationName = dInfo.getApplicationName();

    String aliasDir = WebContainerHelper.getAliasDirName(aliasCanonicalized);

    if (!dInfo.isStandAloneArchive()) {
      ModuleInfo[] modules = dInfo.getModuleProvider().getModuleInfo(J2EEModuleType.WEB);
      for (int i = 0; modules != null && i < modules.length; i++) {
        String currentAlias = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getAlias(modules[i].getModuleUri(), applicationName);
        String currentAliasDir = WebContainerHelper.getAliasDirName(ParseUtils.convertAlias(currentAlias));
        if (aliasDir.equals(currentAliasDir)) {
          String altDD = modules[i].getFileInfo(FileType.ALT_DD)[0].getFilePath();
          if (altDD != null && !altDD.trim().equals("")) {
            InputStream is = null;
            try {
              is = new FileInputStream(altDD);
            } catch (FileNotFoundException e) {
                throw new WebDeploymentException(WebDeploymentException.ALTERNATIVE_DESCRIPTOR_NOT_FOUND,
                new Object[]{altDD, applicationName}, e);
            }

            try {
              byte[] tempStream = ServiceContext.getServiceContext().getDeployContext().getWebConverter().convertXML(is, "web", "simple");
              is = new ByteArrayInputStream(tempStream);
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            } catch (Throwable e) {
               throw new WebDeploymentException(WebDeploymentException.EXCEPTION_OCCURED_WHILE_CONVERTING_WAR,
                new Object[]{altDD, applicationName}, e);
            }

            try {
              FileUtils.writeToFile(is, webDD);
            } catch (IOException e) {
              LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000089",
                "Error while trying to write the alternative descriptor to the file system for application [{0}].", new Object[]{applicationName}, e, null, null);
            }

            if (is != null) {
              try {
                is.close();
              } catch (IOException e) {
                if (traceLocation.beWarning()) {
									LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000235",
											"Error while closing InputStream for alternative descriptor [{0}] for [{1}] application.", new Object[]{altDD, applicationName}, e, null, null);
                }
              }
            }
          }
        }
      }
    }

    ConfigurationUtils.addFileEntryByKey(configuration, Constants.WEB_DD + aliasDir, webDD, aliasDir, true, true);

    if (addWebDD.exists()) {
      ConfigurationUtils.addFileEntryByKey(configuration, Constants.ADD_WEB_DD + aliasDir, addWebDD, aliasDir, true, true);
    }
  }//end of storeAltDDToDB(ContainerDeploymentInfo dInfo, Configuration configuration, String aliasDir, String webApplWorkDirName)

  /**
   *
   * @param webDesc
   * @param webAnnotationsDesc
   * @param servlet_jspConfig
   * @param applicationName
   * @param aliasDir
   * @throws DeploymentException
   */
  private void store2DBase(WebDeploymentDescriptor webDesc, WebDeploymentDescriptor webAnnotationsDesc,
                           Configuration servlet_jspConfig, String applicationName, String aliasDir) throws DeploymentException {
    String webInfDirectory = WebContainerHelper.getDirName(new String[]{WebContainerHelper.getDeployTempDir(applicationName), aliasDir, "root", "WEB-INF"});
    String webApplWorkDirName = WebContainerHelper.getDirName(new String[]{WebContainerHelper.getDeployTempDir(applicationName), aliasDir});
    FileOutputStream fos1 = null;
    FileOutputStream fos2 = null;
    try {
      fos1 = new FileOutputStream(webInfDirectory + "web.xml");
      webDesc.writeStandartDescriptorToStream(fos1);
      if (webAnnotationsDesc != null) {
        fos2 = new FileOutputStream(webInfDirectory + "web-annotations.xml");
        webAnnotationsDesc.writeStandartDescriptorToStream(fos2);
      }
    } catch (Exception e) {
      throw new WebDeploymentException(WebDeploymentException.CANNOT_STORE_THE_MODIFIED_XML_FILE, new Object[]{aliasDir, "web.xml or web-annotations.xml"}, e);
    } finally {
      if (fos1 != null) {
        try {
          fos1.close();
        } catch (IOException e) {
          if (traceLocation.beWarning()) {
          	LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning( "ASJ.web.000236",
								"Error while closing InputStream for [{0} web.xml] while deploying [{1}] application.", new Object[]{webInfDirectory, aliasDir}, e, null, null);
          }
        }
      }
      if (fos2 != null) {
        try {
          fos2.close();
        } catch (IOException e) {
          if (traceLocation.beWarning()) {
          	LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000238",
								"Error while closing InputStream for [{0} web-annotations.xml] while deploying [{1}] application.", new Object[]{webInfDirectory, aliasDir}, e, null, null);
          }
        }
      }
    }

    File mergedWebDD = new File(webInfDirectory + "web.xml");
    ConfigurationUtils.addFileEntryByKey(servlet_jspConfig, Constants.MERGED_WEB_DD + aliasDir, mergedWebDD, aliasDir, true, true);
    File annotationsDD = new File(webInfDirectory + "web-annotations.xml");
    if (annotationsDD.exists()) {
      ConfigurationUtils.addFileEntryByKey(servlet_jspConfig, Constants.ANNOTATIONS_DD + aliasDir, annotationsDD, aliasDir, true, true);
    }
  }//end of store2DBase(WebDeploymentDescriptor webDesc, WebDeploymentDescriptor webAnnotationsDesc,
   //                   Configuration servlet_jspConfig, String applicationName, String aliasDir)

  /**
   * A helper method for resolving unknown reference links
   *
   * @param webDesc          - a raw model from the standard descriptor
   * @param deployInfo       - container deployment info from which we could get the other models
   * @param absoluteFilePath - absolute path to the current war file
   * @param warnings
   * @param applicationName
   */
  private void fixWebModel(WebDeploymentDescriptor webDesc, ContainerDeploymentInfo deployInfo,
                           String absoluteFilePath, Vector warnings, String applicationName) {
    MessageDestinationRefType[] originalMsgDestinationRefs = webDesc.getMsgDestinationRefs();
    if (originalMsgDestinationRefs != null && originalMsgDestinationRefs.length > 0) {
      for (MessageDestinationRefType originalMsgDestinationRef : originalMsgDestinationRefs) {
        MessageDestinationLinkType originalMsgDestinationLink = originalMsgDestinationRef.getMessageDestinationLink();
        if (originalMsgDestinationLink != null) {
          String msgDestLink = originalMsgDestinationLink.get_value();
          if (msgDestLink != null) {
            int diesIdx = msgDestLink.indexOf('#');
            if (diesIdx != -1) {
              String msgDestLinkJarName = msgDestLink.substring(0, diesIdx);
              String realMsgDestLink = msgDestLink.substring(diesIdx + 1);
              String realJarPath = null;
              try {
                File file = new File(absoluteFilePath + File.separatorChar + ".." + File.separatorChar + msgDestLinkJarName);
                realJarPath = file.getCanonicalPath();
              } catch (IOException iox) {
                warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
                  WebWarningException.PROBLEM_WHILE_RESOLVING_RELATIVE_JAR, new Object[]{msgDestLinkJarName, iox.toString()}).toString());
                continue;
              }

              Map ejbModels = deployInfo.getCache("EJBContainer", realJarPath);

              if (ejbModels == null || ejbModels.isEmpty()) {
                warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
                  WebWarningException.MISSING_EJB_CACHE_FOR_JAR, new Object[]{msgDestLinkJarName, msgDestLink}).toString());
                continue;
              }

              Module ejbModule = (Module) ejbModels.get("EJB module");

              if (ejbModule == null) {
                warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
                  WebWarningException.EJB_MODEL_FOR_JAR_IS_MISSING, new Object[]{msgDestLinkJarName, msgDestLink}).toString());
                continue;
              }

              boolean matchFound = checkEJBModel(webDesc, realMsgDestLink, msgDestLink, ejbModule);

              if (!matchFound) {
                warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
                  WebWarningException.LOGICAL_MESSAGE_DESTINATION_NOT_FOUND_IN_JAR, new Object[]{msgDestLink, msgDestLinkJarName}).toString());
              }
            } else {
              // the name of the link is without 'jar-name#' so we have to check if there is a message-destination
              // that describes it in web deployment descriptors
              boolean isMineLink = checkMessageDestinationLinkOwnership(webDesc, msgDestLink);
              if (!isMineLink) {
                // there is no message-destination element describing this link so we will
                // search for it in EJB models
                Map ejbContainerCache = (Map) deployInfo.getCache("EJBContainer");
                Set ejbModulesSet = ejbContainerCache.entrySet();
                boolean matchFound = false;
                for (Iterator itor = ejbModulesSet.iterator(); itor.hasNext();) {
                  Map.Entry<?, ?> entry = (Map.Entry<?, ?>) itor.next();
                  Map ejbModuleCacheMap = (Map) entry.getValue();
                  Module ejbModule = (Module) ejbModuleCacheMap.get("EJB module");
                  matchFound = checkEJBModel(webDesc, msgDestLink, msgDestLink, ejbModule);
                  if (matchFound) {
                    break;
                  }
                }

                if (!matchFound) {
                  warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
                    WebWarningException.LOGICAL_MESSAGE_DESTINATION_NOT_FOUND_IN_ALL_JARS, new Object[]{msgDestLink, applicationName}).toString());
                }
              }
            }
          }
        }
      }
    }
  }//end of fixWebModel(WebDeploymentDescriptor webDesc, ContainerDeploymentInfo deployInfo,
   //                   String absoluteFilePath, Vector warnings, String applicationName)

  /**
   * Checks if the message destination link is described in the passed EJB model
   * If so it creates a new MessageDestinationType object and attach it to web container's standard descriptor
   *
   * @param webDesc     - web container's standard descriptor
   * @param msgDestLink - the message destination link
   * @param ejbModel    - the target EJB model
   * @return - true if the logical message destination is described in the EJB model, false - otherwise
   */
  private boolean checkEJBModel(WebDeploymentDescriptor webDesc, String msgDestLink, String fullMsgDestLink, Module ejbModel) {
    boolean matchFound = false;
    Set<MessageDestination> ejbMessageDestinations = ejbModel.getMessageDestinations();
    for (MessageDestination ejbMessageDestination : ejbMessageDestinations) {
      if (msgDestLink.equals(ejbMessageDestination.getMessageDestinationName())) {
        createMessageDestination(webDesc, fullMsgDestLink, ejbMessageDestination.getJNDIName());
        matchFound = true;
        break;
      }
    }
    return matchFound;
  }//end of checkEJBModel(WebDeploymentDescriptor webDesc, String msgDestLink, String fullMsgDestLink, Module ejbModel)

  /**
   * A helper method for creating a new MessageDestinationType object in the web container's standard descriptor
   *
   * @param msgDestLink - original name of the link (i.e. ../some.jar#someMsgDestName)
   * @param jndiName    - the jndi name gotten from the EJB model
   */
  private void createMessageDestination(WebDeploymentDescriptor webDesc, String msgDestLink, String jndiName) {
    MessageDestinationType newMessageDestination = new MessageDestinationType();

    com.sap.engine.lib.descriptors5.javaee.String msgDestName = new com.sap.engine.lib.descriptors5.javaee.String();
    msgDestName.set_value(msgDestLink);
    newMessageDestination.setMessageDestinationName(msgDestName);

    XsdStringType mappedName = new XsdStringType();
    mappedName.set_value(jndiName);
    newMessageDestination.setMappedName(mappedName);

    MessageDestinationType[] oldMessageDestinations = webDesc.getMsgDestinations();
    MessageDestinationType[] newMessageDestinations = null;
    if (oldMessageDestinations != null && oldMessageDestinations.length > 0) {
      newMessageDestinations = new MessageDestinationType[oldMessageDestinations.length + 1];
      System.arraycopy(oldMessageDestinations, 0, newMessageDestinations, 0, oldMessageDestinations.length);
      System.arraycopy(new MessageDestinationType[]{newMessageDestination}, 0, newMessageDestinations, oldMessageDestinations.length, 1);
    } else {
      newMessageDestinations = new MessageDestinationType[]{newMessageDestination};
    }
    webDesc.setMsgDestinations(newMessageDestinations);
  }//end of createMessageDestination(WebDeploymentDescriptor webDesc, String msgDestLink, String jndiName)

  /**
   * Checks if this message-destination link is described in web.xml or web-j2ee-engine.xml
   *
   * @param webDesc     - web container's standard descriptor
   * @param msgDestLink - the link that should be checked
   * @return true if it is described in web DDs, false - otherwise
   */
  private boolean checkMessageDestinationLinkOwnership(WebDeploymentDescriptor webDesc, String msgDestLink) {
    boolean isMineLink = false;
    MessageDestinationType[] stdMsgDests = webDesc.getMsgDestinations();
    if (stdMsgDests != null && stdMsgDests.length > 0) {
      for (MessageDestinationType stdMsgDest : stdMsgDests) {
        com.sap.engine.lib.descriptors5.javaee.String stdMsgDestName = stdMsgDest.getMessageDestinationName();
        if (stdMsgDest != null) {
          String stdMsgDestNameValue = stdMsgDestName.get_value();
          if (stdMsgDestNameValue != null && msgDestLink.equals(stdMsgDestNameValue)) {
            isMineLink = true;
          }
        }
      }
    }
    return isMineLink;
  }//end of checkMessageDestinationLinkOwnership(WebDeploymentDescriptor webDesc, String msgDestLink)

  // this method could be invoked when the sap default application is already added into the hashtable
  private boolean hasCustomerDefaultApplication() {
    Vector<WebModule> deployedWebModules = ((WebContainerProvider)ServiceContext.getServiceContext().getWebContainer().getIWebContainerProvider()).getDeployedAppls("/");

    if (deployedWebModules == null || deployedWebModules.size() == 0) {
      if (traceLocation.beInfo()) {
        traceLocation.infoT("There is no default web modules");
      }
      return false;
    } else if (deployedWebModules.size() == 2) {
      if (traceLocation.beInfo()) {
        traceLocation.infoT("There is two default web modules defined by " + deployedWebModules.elementAt(0).getWholeApplicationName() + " and " + deployedWebModules.elementAt(1).getWholeApplicationName());
      }
      return true;
    } else { // it is only one default application, check its name
      String deployedApplName = deployedWebModules.elementAt(0).getWholeApplicationName();
      if (WebContainer.sapDefaultApplName.equalsIgnoreCase(deployedApplName)) {
        if (traceLocation.beInfo()) {
          traceLocation.infoT("There is only one default web modules defined by sap default application " + deployedApplName);
        }
        return false;
      } else {
        if (traceLocation.beInfo()) {
          traceLocation.infoT("There is only one default web modules defined by customer default application " + deployedApplName);
        }
        return true;
      }
    }
  }
}//end of class
