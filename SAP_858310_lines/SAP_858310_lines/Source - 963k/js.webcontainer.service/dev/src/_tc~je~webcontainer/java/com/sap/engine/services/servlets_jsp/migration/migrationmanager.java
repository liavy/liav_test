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
import com.sap.engine.frame.core.monitor.CoreMonitor;
import com.sap.engine.frame.state.PersistentContainer;
import com.sap.engine.services.deploy.container.DeployCommunicator;
import com.sap.engine.services.deploy.container.migration.exceptions.CMigrationException;
import com.sap.engine.services.servlets_jsp.migration.swch.SwitchMigrator;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.tc.logging.Location;

/**
 * @author Violeta Georgieva
 * @version 7.0
 */
public class MigrationManager {
  private static final Location currentLocation = Location.getLocation(MigrationManager.class);
  private ApplicationServiceContext appServiceContext = null;
  private static Location traceLocation = LogContext.getLocationService();
  /*
   * Current migration version. Incremented on incompatibility change.
   * As a general case here is the JINUP version whereas the SWITCH version is bigger and located
   * in SwitchMigrator.INTERNAL_VERSION
   */
  public static final int CURRENT_JINUP_VERSION = 10; // JINUP version
  private int currentVersion = 0; // web container migration version of the server before upgrade

  /**
   * Constructor
   *
   * @param appServiceContext
   */
  public MigrationManager(ApplicationServiceContext appServiceContext) {
    this.appServiceContext = appServiceContext;
  }//end of constructor

  /**
   * Migrates the old applications if needed.
   *
   * @throws ServiceException if the migration is not possible
   */
  public void initializeMigration() throws ServiceException {
    traceLocation.pathT("starting initializeMigration()...");
    PersistentContainer pc = appServiceContext.getServiceState().getPersistentContainer();

    currentVersion = pc.getMigrationVersion();
    String logMsg = "MigrationManager: currentVersion: " + currentVersion + "| jinup: " + CURRENT_JINUP_VERSION + "| switch: " + SwitchMigrator.CURRENT_SWITCH_VERSION;
    if (currentVersion < CURRENT_JINUP_VERSION) {
      if (traceLocation.beWarning()) {
      	LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000580",
      		"MigrationManager: currentVersion: {0}| jinup: {1} | switch: {2}",
      		new Object[] {currentVersion, CURRENT_JINUP_VERSION,SwitchMigrator.CURRENT_SWITCH_VERSION }, null, null);
      }
    } else {
      traceLocation.debugT(logMsg);
    }

    if (((currentVersion > CURRENT_JINUP_VERSION) && isJinupMigration())
        || ((currentVersion > SwitchMigrator.CURRENT_SWITCH_VERSION) && isSwitchMigration())) {
      LogContext.getLocation(LogContext.LOCATION_SERVICE).traceError("ASJ.web.000613",
        "Downgrade migration is not supported.", null, null);
      throw new ServiceException("Downgrade migration is not supported.");
    }
    if (currentVersion < CURRENT_JINUP_VERSION) { // at least JINUP is needed
      DeployCommunicator deployCommunicator = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator();

      if (deployCommunicator.getDeployedApplications().length == 0) { // no applications to migrate
        if (isSwitchMigration()) {
          pc.setMigrationVersion(SwitchMigrator.CURRENT_SWITCH_VERSION); // on the safe side
        } else {
          // do not check is JINUP mode set. Set the smaller value between JINUP and SWITCH
          if (CURRENT_JINUP_VERSION < SwitchMigrator.CURRENT_SWITCH_VERSION) {
            pc.setMigrationVersion(CURRENT_JINUP_VERSION);
          } else {
            pc.setMigrationVersion(SwitchMigrator.CURRENT_SWITCH_VERSION);
          }
        }
      } else if (isSwitchMigration()) {
        try {
          traceLocation.debugT("Registering switch migrator");
          deployCommunicator.setMigrator(new SwitchMigrator(appServiceContext));
        } catch (CMigrationException cme) {
          throw new ServiceException(cme);
        }
        // Set migration version is done into the migration interface implementation: notifyForMigrationResult
        // end SWITCH case
      } else if (isJinupMigration()) {
        try {
          traceLocation.debugT("Registering jinup migrator");
          deployCommunicator.setMigrator(new Migrator(appServiceContext, currentVersion));
        } catch (CMigrationException cme) {
          throw new ServiceException(cme);
        }
        //end JINUP case
        //Set migration version is done into the migration interface implementation: notifyForMigrationResult
      } else {
        // not migration mode set but at least JINUP is needed.
    	  //TODO:Polly type:ok
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000262",
          "Migration is needed. The server should be run in migration mode.", null, null);
      }
    }
    if (currentVersion < SwitchMigrator.CURRENT_SWITCH_VERSION) {
      if (isSwitchMigration()) {
        DeployCommunicator deployCommunicator = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator();
        if (deployCommunicator.getDeployedApplications().length == 0) { // no applications to migrate
          pc.setMigrationVersion(SwitchMigrator.CURRENT_SWITCH_VERSION); // on the safe side
        } else {
          try {
            traceLocation.debugT("Registering switch migrator");
            deployCommunicator.setMigrator(new SwitchMigrator(appServiceContext));
          } catch (CMigrationException cme) {
            throw new ServiceException(cme);
          }
        } // else apps > 0
      }
    }
  }//end of initializeMigration()

  /**
   * Returns true if the server is in mode to do the JINUP migration.
   *
   * @return returns true if the server is in mode to do the migration.
   */
  private boolean isJinupMigration() {
    CoreMonitor cm = appServiceContext.getCoreContext().getCoreMonitor();
    if (cm.getRuntimeMode() == CoreMonitor.RUNTIME_MODE_SAFE) {
      if (cm.getRuntimeAction() == CoreMonitor.RUNTIME_ACTION_MIGRATE) {
        return true;
      }
    }
    return false;
  }//end of isMigrating()

  /**
   * Returns true if the server is in mode to do the SWITCH migration.
   *
   * @return returns true if the server is in mode to do the SWITCH migration.
   */
  private boolean isSwitchMigration() {
    CoreMonitor cm = appServiceContext.getCoreContext().getCoreMonitor();
    if (cm.getRuntimeMode() == CoreMonitor.RUNTIME_MODE_SAFE) {
      if (cm.getRuntimeAction() == CoreMonitor.RUNTIME_ACTION_SWITCH) {
        return true;
      }
    }
    return false;
  }//end of isMigrating()

}//end of class
