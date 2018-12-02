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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.lib.io.FileUtils;
import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.accounting.measurement.AMeasurement;
import com.sap.engine.services.deploy.container.ApplicationDeployInfo;
import com.sap.engine.services.deploy.container.ContainerDeploymentInfo;
import com.sap.engine.services.deploy.container.ContainerInfo;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.deploy.container.WarningException;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.WebContainerInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.WebResourceAccessor;
import com.sap.engine.services.servlets_jsp.migration.MigrationManager;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.WebDeploymentDescriptor;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.IWebContainer;
import com.sap.engine.services.servlets_jsp.server.deploy.util.ConfigurationUtils;
import com.sap.engine.services.servlets_jsp.server.deploy.util.Constants;
import com.sap.engine.services.servlets_jsp.server.deploy.util.SecurityUtils;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebWarningException;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Location;

/**
 * Application update
 * @author Violeta Georgieva
 * @version 7.10
 */
public final class UpdateAction extends ActionBase {
  private static final Location currentLocation = Location.getLocation(UpdateAction.class);
  private static final Location traceLocation = LogContext.getLocationDeploy();
  /**
   * Mapping between alias and old descriptors used (previous version). Needed to calculate custom 
   * security settings that should be kept. Synchronized because of parallel deploy considerations 
   */
  public static Hashtable<String, WebDeploymentDescriptor> aliasToOldDescrMap = new Hashtable<String, WebDeploymentDescriptor>();
  private final RemoveAction removeApplication;
  private final DeployAction deployApplication;
  

  /**
   * @param iWebContainer
   * @param containerInfo
   * @param servletsAndJsp
   * @param runtimeInterface
   * @param deployApplication
   */
  public UpdateAction(IWebContainer iWebContainer, ContainerInfo containerInfo, WebContainerHelper servletsAndJsp,
                      WebContainerInterface runtimeInterface, DeployAction deployApplication, RemoveAction removeApplication) {
    super(iWebContainer, containerInfo, servletsAndJsp, runtimeInterface);
    this.deployApplication = deployApplication;
    this.removeApplication = removeApplication;
    aliasToOldDescrMap = new Hashtable<String, WebDeploymentDescriptor>();
  }//end of constructor

  /**
   * @param applicationName
   * @param applicationConfig
   * @throws WarningException
   */
  public void rollbackUpdate(String applicationName, Configuration applicationConfig) throws WarningException {
  	Configuration servlet_jspConfig = null;
  	ArrayList<LocalizableTextFormatter> warnings = new ArrayList<LocalizableTextFormatter>(); 
  	try {
			servlet_jspConfig = applicationConfig.getSubConfiguration(containerInfo.getName());
		} catch (Exception e1) {
			LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000098",
	      "Cannot get the servlet_jsp subconfiguration of the app config for application [{0}] during rollback update.", 
	      new Object[]{applicationName}, e1, null, null);
		}
		
    try {
      remove(applicationName, true, applicationConfig);// remove during update
    } catch (Exception e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000099",
        "Cannot remove application [{0}] during rollback update.", new Object[]{applicationName}, e, null, null);
    }

    String path = WebContainerHelper.getDeployTempDir(applicationName);
    if (!FileUtils.deleteDirectory(new File(path))) {
      if (traceLocation.beError()) {
		    LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceError("ASJ.web.000426",
		      "Cannot delete the working directory for application [{0}] during rollback update.", new Object[]{applicationName}, null, null);
      }
    }

    try {
      downloadAppFiles(applicationName, applicationConfig, "rollbackUpdate");
    } catch (Exception e) {
		  warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
        WebWarningException.CANNOT_SYNCHROMIZE_APPLICATION_DURING_ROLLBACK_UPDATE,
		    new Object[]{applicationName, e.toString()}));
    }

    String[] aliasesCannonicalized = ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalized(applicationName);
    if (aliasesCannonicalized != null && aliasesCannonicalized.length > 0) {
      try {
        File[] aliasesRootDirs = new File[aliasesCannonicalized.length];
        for (int i = 0; i < aliasesCannonicalized.length; i++) {
          String aliasDir = WebContainerHelper.getAliasDirName(aliasesCannonicalized[i]);
          String webApplWorkDirName = WebContainerHelper.getDirName(new String[]{WebContainerHelper.getDeployTempDir(applicationName), aliasDir});
          aliasesRootDirs[i] = new File(webApplWorkDirName + "root" + File.separator);
        }
        
        iWebContainer.deploy(applicationName, aliasesCannonicalized, aliasesRootDirs, true, null, servlet_jspConfig);
      } catch (Exception e) {
		  warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
          WebWarningException.CANNOT_NOTIFY_WCE_THAT_APPLICATION_IS_DEPLOYED_DURING_ROLLBACK_UPDATE,
		  new Object[]{applicationName, e.toString()}));
      }
    }

    try {
      deployApplication.commitDeploy(applicationName);
    } catch (Exception e) {
		 warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
     	 WebWarningException.CANNOT_COMMIT_APPLICATION_UPDATE,
	  	 new Object[]{applicationName, e.toString()}));
    }
    makeWarningException(warnings);
  }//end of rollbackUpdate(String applicationName, Configuration applicationConfig, Properties props)

  /**
   * @param archiveFiles
   * @param dInfo
   * @param props the new properties for the application (new aliases, the old ones are in the deploy communicator)
   * @return
   * @throws DeploymentException
   */
  public ApplicationDeployInfo makeUpdate(File[] archiveFiles, ContainerDeploymentInfo dInfo, Properties props) throws DeploymentException {
    String applicationName = dInfo.getApplicationName();

    Configuration appConfig = dInfo.getConfiguration();
    
    //download application files
    WarningException downloadFilesWarnings = null;
    Configuration servlet_jspConfig = null;
    try {
      if (appConfig.existsSubConfiguration(Constants.CONTAINER_NAME)) {
        servlet_jspConfig = ConfigurationUtils.getSubConfiguration(appConfig, Constants.CONTAINER_NAME, applicationName, true);
      } else {
        if (traceLocation.bePath()) {
        	traceLocation.pathT("The old existing version of application [" +
            applicationName + "] does not contain web applications. So old applications cannot be downloaded.");
        }
      }
    } catch (ConfigurationException e) {
      throw new WebDeploymentException(WebDeploymentException.EXCEPTION_OCCURED_IN_METHOD_FOR_APPLICATION,
        new Object[]{"existsSubConfiguration(" + containerInfo.getName() + ")", applicationName}, e);
    }

    //get old aliases
    String[] oldAliasesCanonicalized = ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalized(applicationName);

    //set old security resources in hashtable per alias and help security service estimate custom changes
    if (servlet_jspConfig != null) {
      try {//ACCOUNTING.start - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.beginMeasure("makeUpdate/setOldSecurityResources", UpdateAction.class);
        }//ACCOUNTING.start - END
        
        setOldSecurityResources(oldAliasesCanonicalized, servlet_jspConfig, applicationName);
      } catch (DeploymentException e) {
        int currentVersion = ServiceContext.getServiceContext().getApplicationServiceContext().getServiceState().getPersistentContainer().getMigrationVersion();
        if (currentVersion < MigrationManager.CURRENT_JINUP_VERSION) {
          throw new WebDeploymentException(WebDeploymentException.APPLICATION_WAS_NOT_MIGRATED, new Object[]{"" + currentVersion, "" + MigrationManager.CURRENT_JINUP_VERSION}, e);
        } else {
          throw e;
        }
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure("makeUpdate/setOldSecurityResources");
        }//ACCOUNTING.end - END
      }
    }

    //remove application stuff without security configurations
    WarningException removeAppWarnings = null;
    try {
      remove(applicationName, true, appConfig);
    } catch (WarningException e) {
      removeAppWarnings = e;
    }

    //delete application configurations except the WCE configurations and the ADMIN congregation
    Configuration servlet_jspAdmin = null;
    try {
      if (servlet_jspConfig != null) {
      	//Keep the ADMIN subconfig in order to preserve the custom settings from the WebAdmin:
      	if (servlet_jspConfig.existsSubConfiguration(Constants.ADMIN)) {
          servlet_jspAdmin = servlet_jspConfig.getSubConfiguration(Constants.ADMIN);
        }
      	//Remove all subconfigurations and propertysheets except the ADMIN and WCE, which have to be preserved during application update:
      	String[] allSubconfigs = servlet_jspConfig.getAllSubConfigurationNames();
      	for (String currentSCName : allSubconfigs) {
      		if (Constants.ADMIN.equals(currentSCName) || 
      				(currentSCName != null && currentSCName.startsWith(Constants.WCE_CONFIG_PREFIX))) {
      			continue;
      		}
      		ConfigurationUtils.deleteConfiguration(servlet_jspConfig, currentSCName, applicationName);
      	}      	
        //Delete all that remain:
        servlet_jspConfig.deleteAllConfigEntries();
        servlet_jspConfig.deleteAllFiles();
      }
    } catch (ConfigurationException e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000036",
        "Cannot delete the configuration for application [{0}].", new Object[]{applicationName}, e, null, null);
    }

    String[] oldAliases = ServiceContext.getServiceContext().getDeployContext().getAliases(applicationName);
    String[] newAliases = WebContainerHelper.getAliases(applicationName, props);
    Vector<String> deletedAliases = new Vector<String>();

    for (int i = 0; oldAliases != null && i < oldAliases.length; i++) {
      boolean aliasDeleted = true;

      for (int j = 0; newAliases != null && j < newAliases.length; j++) {
        if (newAliases[j].equals(oldAliases[i])) {
          aliasDeleted = false;
          break;
        }
      }

      if (aliasDeleted) {
      	removeWebModuleSubConfig(applicationName, servlet_jspConfig, oldAliases[i]);
      	
        deletedAliases.add(oldAliasesCanonicalized[i]);
        //delete security configurations for the deleted aliases
        String aliasDir = WebContainerHelper.getAliasDirName(oldAliasesCanonicalized[i]);
        ConfigurationUtils.deleteConfiguration(appConfig, aliasDir.replace('/', '_'), applicationName);

        try {
          securityUtils.removeSecurityResources(applicationName, oldAliasesCanonicalized[i]);
        } catch (OutOfMemoryError e) {
          throw e;
        } catch (ThreadDeath e) {
          throw e;
        } catch (Throwable e) {
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000100",
            "Error while trying to remove security resources for removed alias [{0}] during update of application.", 
            new Object[]{oldAliasesCanonicalized[i]}, e, null, null);
        }

        mapAliasSecurityRes.remove(oldAliasesCanonicalized[i]);
        final Location secLocation = LogContext.getLocationSecurity();
        if (secLocation.beDebug()) {
          secLocation.debugT("Removing old descriptor info for application [" + applicationName + "] and non-existing in new version canonicalized alias [" +  oldAliasesCanonicalized[i] + "]");
        }
        aliasToOldDescrMap.remove(oldAliasesCanonicalized[i]);
        

        //delete stuff in admin configuration for the deleted aliases
        try {
          if (servlet_jspAdmin != null) {
            deleteCustomSettings(oldAliases[i], applicationName, servlet_jspAdmin, aliasDir);
          }
        } catch (ConfigurationException e) {
          throw new WebDeploymentException(WebDeploymentException.CANNOT_DELETE_CUSTOM_SETTINGS_OF_DELETED_WEB_APPLICATION,
            new Object[]{oldAliasesCanonicalized[i], applicationName}, e);
        }
      }
    }

    // search for newly add aliases
    Vector<String> addedAliases = new Vector<String>();
    for (int i = 0; newAliases != null && i < newAliases.length; i++) {
      boolean aliasAdded = true;

      for (int j = 0; oldAliases != null && j < oldAliases.length; j++) {
        if (oldAliases[j].equals(newAliases[i])) {
          aliasAdded = false;
          break;
        }
      }

      if (aliasAdded) {
        addedAliases.add(newAliases[i]);
        //will create the WCE Provider's web module subconfig for this new alias later in the deploy
      }
    }

    //delete application directory
    String path = WebContainerHelper.getDeployTempDir(applicationName);
    if (!FileUtils.deleteDirectory(new File(path))) {
      if ((new File(path)).exists()) {
        if (!FileUtils.deleteDirectory(new File(path))) {
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000037",
            "Cannot delete the working directory for application [{0}] during update.", new Object[]{applicationName}, null, null);
        }
      }
    } else {
      if ((new File(path)).exists()) {
    	  
        if (traceLocation.beWarning()) {
        	LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning( "ASJ.web.000243", 
        		"The working directory for application [{0}] exists nevertheless that FileUtils.deleteDirectory() returns that it is deleted.", new Object[]{applicationName}, null, null);
        }
      }
    }

    //deploy the new updated application
    ApplicationDeployInfo appDeployInfo = null;
    if (newAliases != null && newAliases.length > 0) {
      appDeployInfo = deployApplication.deploy(archiveFiles, dInfo, props, true, (String[]) addedAliases.toArray(new String[addedAliases.size()]));
    } else if (servlet_jspConfig != null) {
    	//If there are no web aliases in the new application to update with, remove the servlet_jsp configuration
   		ConfigurationUtils.deleteConfiguration(appConfig, Constants.CONTAINER_NAME, applicationName);
    }

    if (appDeployInfo == null) {
      appDeployInfo = new ApplicationDeployInfo();
    }

    // stores deleted aliases to be used later in notifyUpdatedComponents
    if (deletedAliases.size() > 0) {
      String deletedAliasesStr = "";
      for (int i = 0; i < deletedAliases.size(); i++) {
        deletedAliasesStr = deletedAliasesStr + (String) deletedAliases.elementAt(i) + "::";
      }
      deletedAliasesStr = deletedAliasesStr.substring(0, deletedAliasesStr.length() - 2);

      props.setProperty("deleted_web_aliases", deletedAliasesStr);
      appDeployInfo.setDeployProperties(props);
    }

    if (removeAppWarnings != null) {
      appDeployInfo.addWarnings(removeAppWarnings.getWarnings());
    }
    if (downloadFilesWarnings != null) {
      appDeployInfo.addWarnings(downloadFilesWarnings.getWarnings());
    }

    return appDeployInfo;
  }//end of makeUpdate(File[] archiveFiles, ContainerDeploymentInfo dInfo, Properties props)

  /**
   * @param applicationName
   * @throws WarningException
   */
  public void notifyUpdatedComponents(String applicationName, Properties properties) throws WarningException {
    //notify for remove application e.g. destroy session domain.
    removeApplication.notifyRemove(applicationName, properties, true);

    //delete application directory
    String path = WebContainerHelper.getDeployTempDir(applicationName);
    if (!FileUtils.deleteDirectory(new File(path))) {
      if ((new File(path)).exists()) {
        if (!FileUtils.deleteDirectory(new File(path))) {
          if (traceLocation.beError()) {
          	LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceError("ASJ.web.000427",
          		"Cannot delete the working directory for application [{0}] during update.", new Object[]{applicationName}, null, null);
          }
        }
      }
    } else {
      if ((new File(path)).exists()) {
        if (traceLocation.beWarning()) {
        	LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000244", 					
						"The working directory for application [{0}] exists nevertheless that FileUtils.deleteDirectory() returns that it is deleted.", new Object[]{applicationName}, null, null);
        }
      }
    }

    //notify http provider
    deployApplication.notifyDeployedComponents(applicationName, null);
  }//end of notifyUpdatedComponents(String applicationName, Configuration applicationConfig, Properties properties)

  /**
   * @param applicationName
   * @return
   */
  public ApplicationDeployInfo commitUpdate(String applicationName) {
    ApplicationDeployInfo result = new ApplicationDeployInfo();
    try {
      deployApplication.commitDeploy(applicationName);
    } catch (Exception e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000101",
        "Cannot commit [{0}] application update.", new Object[]{applicationName}, e, null, null);
      result.addWarning(e.getMessage());
    }

		if (traceLocation.bePath()) {
			traceLocation.pathT("Update of application [" + applicationName + "] finished successfully in Web Container.");
		}
    return result;
  }//end of commitUpdate(String applicationName)

  /**
   * Gets old/previous version web descriptors extracts related security settings
   * @param aliasesCanonicalized
   * @param config
   * @param appName application name which is used for traces
   * @throws DeploymentException
   */
  private void setOldSecurityResources(String[] aliasesCanonicalized, Configuration config, 
      String appName) throws DeploymentException {
    WebDeploymentDescriptor globalWebDesc = ServiceContext.getServiceContext().getDeployContext().getGlobalDD();
    // global descriptor not stored in aliasToOldDescrMap.put(GLOBAL_DESCRIPTOR_ID, globalWebDesc); 
    // This is because we could not distinguish its old version 

    for (int i = 0; aliasesCanonicalized != null && i < aliasesCanonicalized.length; i++) {
      String aliasDir = WebContainerHelper.getAliasDirName(aliasesCanonicalized[i]);
      WebDeploymentDescriptor webDesc = loadWebDDObjectFromDBase(config, aliasDir); 

      Vector secRolesVec = new Vector();
      HashMap secRolesMap = new HashMap();
      // Init security resources from global-web.xml
      SecurityUtils.initSecurityRoles(globalWebDesc, secRolesVec, secRolesMap);
      // Init security resources from local web.xml
      SecurityUtils.initSecurityRoles(webDesc, secRolesVec, secRolesMap);
      //must be converted
      mapAliasSecurityRes.put(aliasesCanonicalized[i], secRolesMap);
      final Location secLocation = LogContext.getLocationSecurity();
      if (secLocation.beDebug()) {
        secLocation.debugT("Getting old descriptor for application [" + appName + "] and canonicalized alias [" +  aliasesCanonicalized[i] + "]: " + webDesc);
      }
      aliasToOldDescrMap.put(aliasesCanonicalized[i], webDesc);
    }
  }//end of setOldSecurityResources(String[] aliasesCanonicalized, Configuration config)

	private void removeWebModuleSubConfig(String applicationName, Configuration servlet_jspConfig, String oldWebModuleName) throws DeploymentException {
		//First remove its WCE Provider's WebModule sub configuration:
		//TODO: traces
		String oldWMConfigName = ConfigurationUtils.convertToConfigForm(ParseUtils.convertAlias(oldWebModuleName));
		try {
		  if (servlet_jspConfig != null) {
		  	String[] subconfigs = servlet_jspConfig.getAllSubConfigurationNames();
		  	for (String subConfigName : subconfigs) {
		  		if (subConfigName.startsWith(Constants.WCE_CONFIG_PREFIX)) {
		  			Configuration wceAppSubConfig = ConfigurationUtils.getSubConfiguration(servlet_jspConfig, subConfigName, applicationName, true);
		  			if (wceAppSubConfig != null) {
		  				ConfigurationUtils.deleteConfiguration(wceAppSubConfig, Constants.WCE_WEBMODULE_CONFIG_PREFIX + oldWMConfigName, applicationName);
		  			}
		  		}
		  	}
		  }
		} catch (ConfigurationException e) {
			
		  if (traceLocation.beWarning()) {
		  	LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000245",
					"Cannot delete the WCE configuration for application [{0}] and removed alias [{1}] and config name [{2}].", new Object[]{applicationName, oldWebModuleName, oldWMConfigName}, e, null, null);
		  }
		}
	}
  
}//end of class
