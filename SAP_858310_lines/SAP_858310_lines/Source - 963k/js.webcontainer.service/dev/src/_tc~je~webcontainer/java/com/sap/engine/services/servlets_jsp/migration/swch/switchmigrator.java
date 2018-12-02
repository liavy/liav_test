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
package com.sap.engine.services.servlets_jsp.migration.swch;


import com.sap.engine.frame.ApplicationServiceContext;
import com.sap.engine.frame.ServiceException;
import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.state.PersistentContainer;
import com.sap.engine.interfaces.security.JACCMigrationContext;
import com.sap.engine.services.deploy.container.migration.CMigrationInterface;
import com.sap.engine.services.deploy.container.migration.exceptions.CMigrationException;
import com.sap.engine.services.deploy.container.migration.utils.CMigrationInfo;
import com.sap.engine.services.deploy.container.migration.utils.CMigrationResult;
import com.sap.engine.services.deploy.container.migration.utils.CMigrationStatus;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.deploy.WebContainerHelper;
import com.sap.engine.services.servlets_jsp.server.deploy.util.SecurityUtils;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebCMigrationException;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;


/**
 * Migrator class used for Switch upgrade 6.40/6.45 to 7.10
 * @author Svilen Dikov
 * @version 7.1
 */
public class SwitchMigrator implements CMigrationInterface {
  private static final Location currentLocation = Location.getLocation(SwitchMigrator.class);
  private ApplicationServiceContext appServiceContext = null;
  public static final int CURRENT_SWITCH_VERSION = 71000; // NY 7.1000

  /**
   * Constructor
   * @param appServiceContext
   * @param currentVersion
   */
  public SwitchMigrator(ApplicationServiceContext appServiceContext) {
    this.appServiceContext = appServiceContext;
    LogContext.getLocationService().logT(Severity.DEBUG, "SwitchMigrator(version: " + CURRENT_SWITCH_VERSION + ") initialized.");
  }//end of constructor

  /**
   * @see com.sap.engine.services.deploy.container.migration.CMigrationInterface#migrateContainerLogic(com.sap.engine.services.deploy.container.migration.utils.CMigrationInfo)
   */
  public CMigrationResult migrateContainerLogic(CMigrationInfo arg) throws CMigrationException {
    CMigrationResult result = new CMigrationResult();

    String applicationName = arg.getAppName();
    // TODO - check severity
    Configuration appConfig = arg.getAppConfig();
    //Properties properties = arg.getProperties();
    if ((appConfig != null) && (applicationName != null)) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logInfo(currentLocation, "ASJ.web.000301",
          "Starting SWITCH migration of an old application [{0}].", new Object[]{applicationName}, null, null);

        String[] allAliases = ServiceContext.getServiceContext().getDeployContext().getAliases(applicationName);
        if (allAliases != null && allAliases.length > 0) {

          for (int i = 0; i < allAliases.length; i++) {
            int _currentVersion = CURRENT_SWITCH_VERSION;
            String alias = allAliases[i];
            alias = alias.replace("\\", ParseUtils.separator);
            alias = WebContainerHelper.getAliasDirName(alias);
            String aliasForSecurity = alias.replace('/', '_');

            String policyConfigID = SecurityUtils.getPolicyConfigurationID(applicationName, aliasForSecurity);

            // TODO - change severity
            //TODO:Polly type:trace severity:Info
            LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000258",
              "Starting security custom settings migration of web application [{0}], alias [{1}].", new Object[]{applicationName, alias}, null, null);
            boolean migrationOK = false;
            try {
              // TODO HIGH - Uncomment when interface is read
              JACCMigrationContext jaccMigrationContext = ServiceContext.getServiceContext().getSecurityContext().getJACCContext(policyConfigID).getMigrationContext();
              jaccMigrationContext.migratePolicyConfiguration(applicationName, aliasForSecurity);
              migrationOK = true;
            } catch (Exception e) {
              migrationOK = false;
              //TODO:Polly remove log
              LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000259",
                "Exception caught during security settings migration.", e, null, null);
              // TODO Log - localize; Maybe accumulate exceptions and throw them somehow at the end of iteration through all aliases
              throw new WebCMigrationException("Error while migrating custom security settings for application {0} alias {1}", new Object[]{applicationName, alias}, e); // WebCMigrationException.CANNOT_ADD_NEW_SECURITY_PROPERTIES
            } finally {
              String msg = migrationOK ? "" : " with error";
              // TODO - change severity
              //TODO:Polly type:trace severity:Info
              LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000260",
                "Migration of web application [{0}], alias [{1}] completed {2}.", new Object[]{applicationName, alias, msg}, null, null);
            }

            LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logInfo(currentLocation, "ASJ.web.000302",
              "Migration of an old web application [{0}] with alias [{1}] finished.", new Object[]{applicationName, alias}, null, null);
          } // for
          // TODO Log - localize; Maybe accumulate exceptions and throw them somehow at the end of iteration through all aliases
          //throw new WebCMigrationException(WebCMigrationException.CANNOT_ADD_NEW_SECURITY_PROPERTIES, new Object[]{alias}, e);
        } // if allAliases != null
      } // if (appConfig != null)

    //  TODO - check severity
    //TODO:Polly Check this
    //TODO:Polly severity:Info (Seems to me that the completion of this step (migration) should be indicated somehow to the administrators)
    LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000261",
      "Migration of whole application [{0}] is finished.", new Object[]{applicationName}, null, null);
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
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logInfo(currentLocation, "ASJ.web.000303",
            "servlet_jsp service migration: Application [{0}] migration PASSED.", new Object[]{arg[i].getAppName()}, null, null);
        } else {
          isMigrationDone = false;
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logInfo(currentLocation, "ASJ.web.000304",
            "servlet_jsp service migration: Application [{0}] migration FAILED.", new Object[]{arg[i].getAppName()}, null, null);
        }
      }

      try {
        if (isMigrationDone) {
          pc.setMigrationVersion(SwitchMigrator.CURRENT_SWITCH_VERSION);
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logInfo(currentLocation, "ASJ.web.000305",
            "New migration level of servlet_jsp service will be set [{0}].", new Object[]{SwitchMigrator.CURRENT_SWITCH_VERSION}, null, null);
        } else {
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000297",
            "New migration level of servlet_jsp service could not be set to [{0}].", new Object[]{SwitchMigrator.CURRENT_SWITCH_VERSION}, null, null);
        }
      } catch (ServiceException ex) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000298",
          "New migration level of servlet_jsp service could not be set to [{0}].", new Object[]{SwitchMigrator.CURRENT_SWITCH_VERSION}, ex, null, null);
      }
    }
  }//end of notifyForMigrationResult(CMigrationStatus[] arg0)

}//end of class
