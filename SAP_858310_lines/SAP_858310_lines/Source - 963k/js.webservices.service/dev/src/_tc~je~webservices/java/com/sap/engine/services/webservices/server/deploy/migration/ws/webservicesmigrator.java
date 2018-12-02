package com.sap.engine.services.webservices.server.deploy.migration.ws;

import java.io.IOException;

import com.sap.engine.services.deploy.container.DeployCommunicator;
import com.sap.engine.services.deploy.container.migration.CMigrationInterface;
import com.sap.engine.services.deploy.container.migration.exceptions.CMigrationException;
import com.sap.engine.services.deploy.container.migration.utils.CMigrationInfo;
import com.sap.engine.services.deploy.container.migration.utils.CMigrationResult;
import com.sap.engine.services.deploy.container.migration.utils.CMigrationStatus;
import com.sap.engine.services.webservices.server.container.ws.metaData.WSApplicationMetaDataContext;
import com.sap.engine.services.webservices.tools.ExceptionManager;
import com.sap.engine.services.webservices.webservices630.server.deploy.common.WSBaseConstants;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WebServicesConstants;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Title: WebServiceMigrator
 * Description: Implementation of CMigrationInterface for WebServices service.
 *              Migrates old data archives: download from the DB, change archive structure to new structure, upload new structure to DB.
 * Company: SAP Labs Sofia 
 * @author aneta-a
 */
public class WebServicesMigrator implements CMigrationInterface {
  
  private static final Location LOCATION = Location.getLocation(WebServicesMigrator.class); 
  private static final String METHOD = "migrateContainerLogic(CMigrationInfo cMigInfo)";
  private String tempDir; 
  private DeployCommunicator deployCommunicator;
  
  public WebServicesMigrator(String tempDir, DeployCommunicator deployCommunicator) {
    this.tempDir = tempDir;
    this.deployCommunicator = deployCommunicator;
  }   
   
  public CMigrationResult migrateContainerLogic(CMigrationInfo cMigInfo) throws CMigrationException {
    LOCATION.entering(METHOD);
    String applicationName = cMigInfo.getAppName();
    CMigrationResult migrationResult = null;       
    if (hasToMigrate(cMigInfo.getAppConfig(), applicationName)) {
      try {      
        MigrationController migrationController = new MigrationController(getSourceDir(tempDir, applicationName), getDestinationDir(tempDir, applicationName)); 
        LOCATION.debugT("The application to be converted is: " + applicationName);
        migrationResult = migrationController.migrateNW04DeployedContent(cMigInfo, deployCommunicator.getMyWorkDirectory(applicationName));
        LOCATION.infoT("The conversion of " + applicationName + " successfully finished.");
      } catch (Exception ex) {
        ExceptionManager.logThrowable(Severity.FATAL, Category.SYS_SERVER, LOCATION, METHOD, "Unsuccessful migration for application " + applicationName + " by WebServicesMigrator. The application can't be started.", ex);
//        Category.SYS_SERVER.fatalT(LOCATION, "Unsuccessful migration for application {0} by WebServicesMigrator. The application can't be started.", new Object[] { applicationName });
        throw new CMigrationException(MigrationConstants.UNSUCCESSFUL_MIGRATION, new Object[] { applicationName }, ex);
      }
    }    
    LOCATION.exiting(METHOD);
    return migrationResult;
  }
  
  public void notifyForMigrationResult(CMigrationStatus[] cmStatus) {
    try {
       IOUtil.deleteDir(tempDir + "/mgr");
       LOCATION.infoT("Temporary directory {0} was deleted.", new Object[] { tempDir + "/mgr" });
     } catch (IOException e1) {
      //$JL-EXC$
       LOCATION.errorT("Temporary directory {0} was not deleted.", new Object[] { tempDir + "/mgr" });
     }  
  }
  
  private String getSourceDir(String tempDir, String applicationName) {
    return tempDir + "/mgr/" +  applicationName + "/nw04";
  }
  
  private String getDestinationDir(String tempDir, String applicationName) {
    return tempDir + "/mgr/" +  applicationName + "/nw05" + WSBaseConstants.SEPARATOR + WSBaseConstants.WS_CONTAINER_NAME;
  }
  
  private boolean hasToMigrate(com.sap.engine.frame.core.configuration.Configuration applicationConfiguration, String applicationName) {
    com.sap.engine.frame.core.configuration.Configuration webservicesContainerConfiguration;
    com.sap.engine.frame.core.configuration.Configuration metaDataConfiguration;
    try {
        if (!applicationConfiguration.existsSubConfiguration(WSBaseConstants.WS_CONTAINER_NAME)) {
          return false;
        } else {
          webservicesContainerConfiguration = applicationConfiguration.getSubConfiguration(WSBaseConstants.WS_CONTAINER_NAME);
          if (webservicesContainerConfiguration.existsSubConfiguration(WebServicesConstants.WEBSERVICES_CONFIG_NAME)) {
            return true;
          } else {
            if (webservicesContainerConfiguration.existsSubConfiguration(WSApplicationMetaDataContext.METADATA)) {
              metaDataConfiguration = webservicesContainerConfiguration.getSubConfiguration(WSApplicationMetaDataContext.METADATA);
              LOCATION.infoT("Metadata configuration is successfully received.");
              String dbValue = (String)metaDataConfiguration.getConfigEntryDBValue(WSApplicationMetaDataContext.VERSION);
              LOCATION.debugT("Version of application is " + metaDataConfiguration.getConfigEntryDBValue(WSApplicationMetaDataContext.VERSION));
              if (WSApplicationMetaDataContext.VERSION_71.equals(dbValue)) {
                return false;
              } else {        
                return true;
              }
            } else {
              return false;
            }
          } 
        }
    } catch (Exception e) {
      ExceptionManager.logThrowable(Severity.FATAL, Category.SYS_SERVER, LOCATION, "hasToMigrate(Configuration config, String applicationName)", "Unsuccessful check for application's version " + applicationName +". The application will not be migrated.", e);
//      Category.SYS_SERVER.fatalT(LOCATION, "Unsuccessful check for application's version {0}. The application will not be migrated.", new Object[] { applicationName });
      return false;
    }
  }
 
}
