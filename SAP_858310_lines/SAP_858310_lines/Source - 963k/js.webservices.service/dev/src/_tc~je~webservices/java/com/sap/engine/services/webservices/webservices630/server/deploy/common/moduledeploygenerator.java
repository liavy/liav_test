package com.sap.engine.services.webservices.webservices630.server.deploy.common;

import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.jar.JarUtil;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.tc.logging.Location;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Enumeration;

/**
 * Title: ModuleDeployGenerator
 * Description: The class contains base methods for generating module deployment files.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class ModuleDeployGenerator {

  private ModuleFileStorageHandler moduleFileStorageHandler = null;

  public ModuleDeployGenerator() {
    this.moduleFileStorageHandler = new ModuleFileStorageHandler();
  }

  public ModuleDeployResult generateAndSaveModuleDeployFiles(String applicationName, String wsComponentDir, File[] moduleArchves) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to generate ws clients module deploy files for application " + applicationName + ". ";

    ModuleDeployResult moduleDeployResult = generateModuleDeployFiles(applicationName, wsComponentDir, moduleArchves);

    try {
      moduleFileStorageHandler.saveModuleMappings(wsComponentDir, moduleDeployResult.getModuleMappings());
    } catch(WSDeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg;
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    return moduleDeployResult;
  }

  public ModuleDeployResult generateModuleDeployFiles(String applicationName, String wsComponentDir, File[] moduleArchives) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to generate ws clients module deploy files for application " + applicationName + ". ";

    ModuleDeployResult moduleDeployResult = generateModuleDeployFiles0(applicationName, wsComponentDir, moduleArchives);
    //ModuleDeployResult moduleDeployResult = generateAndSaveModuleDeployFiles(applicationName, wsComponentDir, moduleArchives);

    Hashtable moduleCrcTable = null;
    try {
      moduleCrcTable = IOUtil.getModuleCrcTable(moduleArchives);
    } catch(IOException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg;
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }
    moduleDeployResult.setModuleCrcTable(moduleCrcTable);

    return moduleDeployResult;
  }


  private ModuleDeployResult generateModuleDeployFiles0(String applicationName, String wsClientsDir, File[] wsClientsModuleArchves) throws WSDeploymentException {
    Properties moduleFullMappings = generateModuleDeployFiles1(applicationName, wsClientsDir, wsClientsModuleArchves);

    ModuleDeployResult moduleDeployResult = new ModuleDeployResult();
    moduleDeployResult.setFilesForClassLoader(WSUtil.collecProperties(moduleFullMappings));
    moduleDeployResult.setModuleMappings(collectFileNameMappings(moduleFullMappings));

    return moduleDeployResult;
  }

  private Properties generateModuleDeployFiles1(String applicationName, String wsClientsDir, File[] moduleArchves) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to generate ws clients module deploy files for application " + applicationName + ". ";
    if(moduleArchves == null) {
      return new Properties();
    }

    Properties moduleFullMappings = new Properties();
    for(int i = 0; i < moduleArchves.length; i++) {
      File moduleArchive = moduleArchves[i];
      String moduleJarAppPath = null;
      try {
        moduleJarAppPath = extractAndPackageAppJars(wsClientsDir, moduleArchive);
      } catch(WSDeploymentException e) {
        Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
        wsDeployLocation.catching(e);

        String msg = excMsg;
        Object[] args = new String[]{msg, "none"};
        throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
      }
      if(moduleJarAppPath != null) {
        moduleFullMappings.put(moduleArchive.getName(),moduleJarAppPath);
      }
    }

    return moduleFullMappings;
  }

  private Properties collectFileNameMappings(Properties filePathsMapping) {
    if(filePathsMapping == null) {
      return new Properties();
    }

    Properties fileNameMapping = new Properties();
    Enumeration enum1 = filePathsMapping.keys();
    while(enum1.hasMoreElements()) {
      String fileName = (String)enum1.nextElement();
      String filePath = filePathsMapping.getProperty(fileName);
      fileNameMapping.setProperty(fileName, new File(filePath).getName());
    }

    return fileNameMapping;
  }

  private String extractAndPackageAppJars(String wsClientsDir, File moduleArchive) throws WSDeploymentException {
    String excMsg = "Error occurred trying to extract and package class files for module: " + moduleArchive.getName() + ". ";

    String moduleAppJarPath = null;
    String moduleExtension = IOUtil.getFileExtension(moduleArchive);
    try {
      if (moduleExtension.equals(WSBaseConstants.WSAR_EXTENSION)) {
        moduleAppJarPath = ModuleDirsHandler.getModuleJarPath(wsClientsDir, ModuleDirsHandler.getModuleJarName(moduleArchive.getName()));
        IOUtil.createParentDir(new String[]{moduleAppJarPath});
        (new JarUtil()).makeJarFile(moduleArchive, new String[0], WSBaseConstants.META_INF, new String[0], new String[0], moduleAppJarPath);
      }
    } catch(IOException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(excMsg, e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    return moduleAppJarPath;
  }

}
