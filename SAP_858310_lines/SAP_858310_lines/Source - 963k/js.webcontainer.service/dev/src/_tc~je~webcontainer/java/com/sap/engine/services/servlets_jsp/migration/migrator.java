/*
 * Copyright (c) 2004-2008 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.migration;

import com.sap.engine.frame.ApplicationServiceContext;
import com.sap.engine.frame.ServiceException;
import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.frame.state.PersistentContainer;
import com.sap.engine.interfaces.security.JACCContext;
import com.sap.engine.interfaces.security.JACCSecurityRoleMappingContext;
import com.sap.engine.interfaces.security.JACCUpdateContext;
import com.sap.engine.interfaces.security.SecurityContext;
import com.sap.engine.services.deploy.container.migration.CMigrationInterface;
import com.sap.engine.services.deploy.container.migration.exceptions.CMigrationException;
import com.sap.engine.services.deploy.container.migration.utils.CMigrationInfo;
import com.sap.engine.services.deploy.container.migration.utils.CMigrationResult;
import com.sap.engine.services.deploy.container.migration.utils.CMigrationStatus;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.WebContainerProperties;
import com.sap.engine.services.servlets_jsp.server.deploy.DeployAction;
import com.sap.engine.services.servlets_jsp.server.deploy.WebContainerHelper;
import com.sap.engine.services.servlets_jsp.server.deploy.ActionBase;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.WebDeploymentDescriptor;
import com.sap.engine.services.servlets_jsp.server.deploy.util.ConfigurationUtils;
import com.sap.engine.services.servlets_jsp.server.deploy.util.Constants;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebCMigrationException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.tc.logging.Location;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

/**
 * @author Violeta Georgieva
 * @version 7.10
 */
public class Migrator implements CMigrationInterface {
  private static final Location currentLocation = Location.getLocation(Migrator.class);
  private static final Location traceLocation =LogContext.getLocationService();
  private ApplicationServiceContext appServiceContext = null;
  private int currentVersion = 0;

  /**
   * Constructor
   *
   * @param appServiceContext
   * @param currentVersion
   */
  public Migrator(ApplicationServiceContext appServiceContext, int currentVersion) {
    this.appServiceContext = appServiceContext;
    this.currentVersion = currentVersion;
  }//end of constructor

  /**
   * @see com.sap.engine.services.deploy.container.migration.CMigrationInterface#migrateContainerLogic(com.sap.engine.services.deploy.container.migration.utils.CMigrationInfo)
   */
  public CMigrationResult migrateContainerLogic(CMigrationInfo arg) throws CMigrationException {
    CMigrationResult result = new CMigrationResult();

    String applicationName = arg.getAppName();
    Configuration appConfig = arg.getAppConfig();
    Properties properties = arg.getProperties();

    if (traceLocation.beDebug()) {
      traceLocation.debugT("WebMigrator RunAS: app: " +  applicationName + "| props: " + properties.toString());
    }

    JACCSecurityRoleMappingContext jaccSecurityRoleMappingContext = null;
    jaccSecurityRoleMappingContext = ServiceContext.getServiceContext().getSecurityContext().getJACCSecurityRoleMappingContext();

    if (appConfig != null) {
      Configuration servlet_jspConfig = null;
      Configuration servlet_jspBackup = null;
      try {
        servlet_jspConfig = ConfigurationUtils.getSubConfiguration(appConfig, Constants.CONTAINER_NAME, applicationName, true);
        if (currentVersion < 2) {
          servlet_jspBackup = ConfigurationUtils.createSubConfiguration(servlet_jspConfig, Constants.BACKUP, applicationName, true);
        }
      } catch (Exception e) {
        throw new WebCMigrationException(WebCMigrationException.CANNOT_GET_OR_CREATE_SUBCONFIG, new Object[]{"servlet_jsp", "backup", applicationName}, e);
      }

      if (applicationName != null) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logInfo(currentLocation, "ASJ.web.000306",  "Starting migration of an old web application [{0}].", new Object[]{applicationName}, null, null);

        String[] allAliases = ServiceContext.getServiceContext().getDeployContext().getAliases(applicationName);
        if (allAliases != null && allAliases.length > 0) {
          Hashtable wceInDeployPerModule = new Hashtable();

          for (int i = 0; i < allAliases.length; i++) {
            int _currentVersion = currentVersion;
            String alias = allAliases[i];
            String aliasDir = WebContainerHelper.getAliasDirName(ParseUtils.convertAlias(alias));
            String aliasForSecurity = aliasDir.replace('/', '_');
            if (traceLocation.beDebug()) { // TODO debug
              traceLocation.debugT("WebMigrator RunAS: alias '" + alias + "'| currentVersion: " + _currentVersion + "| aliasDir: " + aliasDir);
            }

            String policyConfigurationName = applicationName + "*" + aliasForSecurity;

            String warUri = (properties.getProperty("web:" + alias)).replace('\\', '/');
            String warName = warUri.substring(warUri.lastIndexOf('/') + 1);

            String webApplWorkDirName = WebContainerHelper.getDirName(new String[]{TaskConvertWars.getDeployTempDir(applicationName), aliasDir});
            String rootDirectory = webApplWorkDirName + "root" + File.separator;

            if (_currentVersion < 2) {
              // Convert and store alternative DD to DB
              try {
                TaskConvertWars.convertAndStoreAltDDToDB(servlet_jspConfig, aliasDir);
              } catch (Exception e) {
                throw new WebCMigrationException(WebCMigrationException.CANNOT_CONVERT_AND_STORE_ALTDD, new Object[]{applicationName}, e);
              }

              // Convert and store .war file to DB (convert web.xml, web-j2ee-engine.xml, all .tld files)
              try {
                TaskConvertWars.convertAndStoreWarToDB(servlet_jspConfig, servlet_jspBackup, new File(webApplWorkDirName + warName), warUri);
              } catch (Exception e) {
                throw new WebCMigrationException(WebCMigrationException.CANNOT_CONVERT_AND_STORE_WAR, new Object[]{warUri, applicationName}, e);
              }

              // Extract war to the file system
              try {
                TaskConvertWars.extractWar(applicationName, new File(rootDirectory), new File(webApplWorkDirName + warName));
              } catch (Exception e) {
                throw new WebCMigrationException(WebDeploymentException.CANNOT_EXTRACT_THE_WAR_FILE_OF_THE_APPLICATION, new Object[]{warName, applicationName}, e);
              }

              // Create security resources
              try {
                TaskMigrateSecurity.createSecurityResources(servlet_jspConfig, jaccSecurityRoleMappingContext, rootDirectory, aliasDir, policyConfigurationName, applicationName, alias, _currentVersion);
              } catch (Exception e) {
                throw new WebCMigrationException(WebCMigrationException.CANNOT_CREATE_SECURITY_RESOURCES, new Object[]{applicationName}, e);
              }

              _currentVersion = 2;
            }

            Configuration securityConfig = null;
            try {
              securityConfig = ConfigurationUtils.getSubConfiguration(appConfig, aliasForSecurity, applicationName, true);
              if (securityConfig == null) {
                throw new WebCMigrationException(WebCMigrationException.CANNOT_GET_SECURITY_SUBCONFIG, new Object[]{aliasForSecurity, applicationName});
              }
            } catch (Exception e) {
              throw new WebCMigrationException(WebCMigrationException.CANNOT_GET_SECURITY_SUBCONFIG, new Object[]{aliasForSecurity, applicationName}, e);
            }

            if (_currentVersion < 3) {
              // Check whether there is alternative descriptor, if true download it on the file system with name web.xml.
              // Then store in DB web.xml and web-j2ee-engine.xml and theirs CRCs.
              try {
                TaskConvertWars.storeWebXmlToDB(servlet_jspConfig, aliasDir, rootDirectory + "WEB-INF" + File.separator);
              } catch (Exception e) {
                throw new WebCMigrationException(WebCMigrationException.CANNOT_STORE_WEB_DDS, new Object[]{applicationName}, e);
              }

              // Add 4 new security properties:
              // form-login-page, form-error-page, password-change-login-page and password-change-error-page
              try {
                TaskMigrateSecurity.addSecurityProperties(securityConfig, policyConfigurationName, alias, rootDirectory, _currentVersion);
              } catch (Exception e) {
                throw new WebCMigrationException(WebCMigrationException.CANNOT_ADD_NEW_SECURITY_PROPERTIES, new Object[]{alias}, e);
              }

              _currentVersion = 3;
            }

            if (_currentVersion < 4) {
              // Create security resources
              try {
                TaskMigrateSecurity.createSecurityResources(servlet_jspConfig, jaccSecurityRoleMappingContext, rootDirectory, aliasDir, policyConfigurationName, applicationName, alias, _currentVersion);
              } catch (Exception e) {
                throw new WebCMigrationException(WebCMigrationException.CANNOT_CREATE_SECURITY_RESOURCES, new Object[]{applicationName}, e);
              }

              // Add 4 new security properties:
              // form-login-page, form-error-page, password-change-login-page and password-change-error-page
              try {
                TaskMigrateSecurity.addSecurityProperties(securityConfig, policyConfigurationName, alias, rootDirectory, _currentVersion);
              } catch (Exception e) {
                throw new WebCMigrationException(WebCMigrationException.CANNOT_ADD_NEW_SECURITY_PROPERTIES, new Object[]{alias}, e);
              }

              _currentVersion = 4;
            }

            if (_currentVersion < 5) {
              // Remove the auth method and auth template for the applications that do not have auth method in their deployment descriptors.
              try {
                TaskMigrateSecurity.addSecurityProperties(securityConfig, policyConfigurationName, alias, rootDirectory, _currentVersion);
              } catch (Exception e) {
                throw new WebCMigrationException(WebCMigrationException.CANNOT_REMOVE_AUTH_METHOD_AND_AUTH_TEMPLATE, new Object[]{alias}, e);
              }

              _currentVersion = 5;
            }

            if (_currentVersion < 6) {
              Vector wceInDeploy = new Vector();

              Hashtable wceProviders = ServiceContext.getServiceContext().getWebContainerProperties().getWCEProviders();
              Enumeration enumeration = wceProviders.keys();
              while (enumeration.hasMoreElements()) {
                String wceName = (String) enumeration.nextElement();
                Vector descriptorNames = (Vector) wceProviders.get(wceName);
                if (WebContainerHelper.isDescriptorExist(descriptorNames, rootDirectory)) {
                  wceInDeploy.addElement(wceName);
                }
              }

              if (wceInDeploy.size() > 0) {
                wceInDeployPerModule.put(allAliases[i], wceInDeploy);
              }

              _currentVersion = 6;
            }

            if (_currentVersion < 7) {
              try {
                WebDeploymentDescriptor webDesc = TaskMigrateSecurity.getWebDeploymentDescriptor(rootDirectory, alias);

                //Store web model as a structure in the configuration
                ActionBase.storeWebDDObject2DBase(webDesc, servlet_jspConfig, aliasDir);

                //Store "url-session-tracking" value in the configuration
                ConfigurationUtils.addConfigEntry(servlet_jspConfig, Constants.URL_SESSION_TRACKING + aliasDir, webDesc.getWebJ2EEEngine().getUrlSessionTracking(), aliasDir, true, true);
              } catch (Exception e) {
                throw new WebCMigrationException(WebCMigrationException.CANNOT_STORE_WEB_DD_OBJECT_2_DBASE, new Object[]{applicationName}, e);
              }

              _currentVersion = 7;
            }

            if (_currentVersion < 8) { // migrate JACC 1.0 web permissions to JACC 1.1. Invoke UME migrator
              // Create security resources
              try {
                SecurityContext secContext = ServiceContext.getServiceContext().getSecurityContext();
                JACCContext jaccContext = secContext.getJACCContext(policyConfigurationName);
                JACCUpdateContext jaccUpdateContext = jaccContext.getUpdateContext();
                jaccUpdateContext.migrateJaccPolicyConfiguration();
              } catch (Exception e) {
                throw new WebCMigrationException(WebCMigrationException.CANNOT_CREATE_SECURITY_RESOURCES, new Object[]{applicationName + "[alias:" + alias + "]"}, e);
              }
              _currentVersion = 8;
            } // 8

            // version 9 - added web-j2ee-engine tag programmatic-security-against
            if (_currentVersion < 9) { // added web-j2ee-engine tag programmatic-security-against
              if (traceLocation.beDebug()) {
                traceLocation.debugT("WebMigrator RunAS: In check for version 9");
              }
              if (currentVersion > 7) { // step 7 not executed now
                if (traceLocation.beDebug()) {
                  traceLocation.debugT("WebMigrator RunAS: currentVersion > 7");
                }
                try {
                  if (i == 0) {
                	  if (traceLocation.beWarning()) {
                		  LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning( "ASJ.web.000584", "WebMigrator: Starting migration version 9.", null, null);
                	  }
                  }
                  WebDeploymentDescriptor webDesc = TaskMigrateSecurity.getWebDeploymentDescriptor(rootDirectory, alias);
                  if (traceLocation.beDebug()) { // TODO debug
                    traceLocation.debugT("WebMigrator RunAS: Web descriptors got.");
                  }
                  //Store web model as a structure in the configuration
                  ActionBase.storeWebDDObject2DBase(webDesc, servlet_jspConfig, aliasDir);
                  if (traceLocation.beDebug()) { // TODO debug
                    traceLocation.debugT("WebMigrator RunAS: ActionBase.storeWebDDObject2DBase() passed");
                  }
                } catch (Exception e) {
                  throw new WebCMigrationException(WebCMigrationException.CANNOT_STORE_WEB_DD_OBJECT_2_DBASE, new Object[]{applicationName}, e);
                }
              }
              _currentVersion = 9;
            }

            // version 10 - revert to old SerialVersionUID of WebJ2eeEngineType (j2eedescriptors), before version 9
            if (_currentVersion < 10) {
              if (traceLocation.beDebug()) {
                traceLocation.debugT("WebMigrator RunAS: In check for version 10");
              }
              if (currentVersion == 9) {
                if (i == 0) {
                    if (traceLocation.beWarning()) {
                    	LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000585", "Starting migrator version 10. App. {0} alias {1}", new Object[]{applicationName, alias}, null, null);
                    }
                }
                try {
                  WebDeploymentDescriptor webDesc = TaskMigrateSecurity.getWebDeploymentDescriptor(rootDirectory, alias);
                  if (traceLocation.beDebug()) {
                    traceLocation.debugT("WebMigrator RunAS: Web descriptors got.");
                  }
                  //Store web model as a structure in the configuration
                  ActionBase.storeWebDDObject2DBase(webDesc, servlet_jspConfig, aliasDir);
                  if (traceLocation.beDebug()) {
                    traceLocation.debugT("WebMigrator RunAS: ActionBase.storeWebDDObject2DBase() passed, v 10.");
                  }
                } catch (Exception e) {
                  throw new WebCMigrationException(WebCMigrationException.CANNOT_STORE_WEB_DD_OBJECT_2_DBASE, new Object[]{applicationName}, e);
                }
              }

              _currentVersion = 10;
            }
          }  // for


          if (!wceInDeployPerModule.isEmpty()) {
            try {
              DeployAction.storeWCEInDeploy(wceInDeployPerModule, servlet_jspConfig);
            } catch (ConfigurationException e) {
              throw new WebCMigrationException(WebDeploymentException.CANNOT_STORE_WCE_IN_DEPLOY, new Object[]{applicationName}, e);
            }
          }
        }

        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logInfo(currentLocation, "ASJ.web.000307", "Migration of an old web application [{0}] is finished.", new Object[]{applicationName}, null, null);
      }
    }

    return result;
  }//end of migrateContainerLogic(CMigrationInfo arg0)

  /**
   * @see com.sap.engine.services.deploy.container.migration.CMigrationInterface#notifyForMigrationResult(com.sap.engine.services.deploy.container.migration.utils.CMigrationStatus[])
   */
  public void notifyForMigrationResult(CMigrationStatus[] arg) {
    PersistentContainer pc = appServiceContext.getServiceState().getPersistentContainer();

    if (arg != null) {
      boolean isMigrationDone = true;

      for (int i = 0; i < arg.length; i++) {
        if (arg[i].getStatus() == CMigrationStatus.PASSED) {
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logInfo(currentLocation, "ASJ.web.000308",
            "servlet_jsp service migration: Application [{0}] migration PASSED.", new Object[]{arg[i].getAppName()}, null, null);
        } else {
          isMigrationDone = false;
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logInfo(currentLocation, "ASJ.web.000309",
            "servlet_jsp service migration: Application [{0}] migration FAILED.", new Object[]{arg[i].getAppName()}, null, null);
        }
      }

      try {
        if (isMigrationDone) {
          pc.setMigrationVersion(MigrationManager.CURRENT_JINUP_VERSION);
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logInfo(currentLocation, "ASJ.web.000310",
            "New migration level of servlet_jsp service will be set [{0}].", new Object[]{MigrationManager.CURRENT_JINUP_VERSION}, null, null);
        } else {
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000299",
            "New migration level of servlet_jsp service could not be set to [{0}].", new Object[]{MigrationManager.CURRENT_JINUP_VERSION}, null, null);
        }
      } catch (ServiceException ex) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000300",
          "New migration level of servlet_jsp service could not be set to [{0}].", new Object[]{MigrationManager.CURRENT_JINUP_VERSION}, ex, null, null);
      }
    }
  }//end of notifyForMigrationResult(CMigrationStatus[] arg0)

}//end of class
