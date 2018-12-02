package com.sap.engine.services.webservices.webservices630.server.deploy.common;

import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WSDirsHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WebServicesConstants;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.WSClientsConstants;

import java.util.Properties;
import java.util.HashSet;
import java.util.Set;
import java.io.File;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public abstract class ModuleDirsHandler {

  public static String getAppJarsRelDir() {
    return WebServicesConstants.APP_JARS_NAME;
  }

  public static String getAppJarsDir(String webServicesDir) {
    return webServicesDir + WebServicesConstants.SEPARATOR + getAppJarsRelDir();
  }

  public static String getAppJarsConfigRelName() {
    return WebServicesConstants.APP_JARS_NAME;
  }

  public static String getAppJarsConfigName(String wsComponentConfigPath) {
    return wsComponentConfigPath + WebServicesConstants.SEPARATOR + getAppJarsConfigRelName();
  }

  public static String getModuleWorkingDir(String wsClientsWorkingDir, String moduleName) {
    return wsClientsWorkingDir + WSClientsConstants.SEPARATOR + WSUtil.getModuleNameByType(moduleName);
  }

  public static String getModuleExtractDir(String moduleWorkingDir) {
    return moduleWorkingDir + WSClientsConstants.SEPARATOR + WSClientsConstants.EXTRACT_DIR_NAME;
  }

  public static String getModuleExtractFileName(String moduleName) {
    return IOUtil.getAsJarName(moduleName);
  }

  public static String getModuleExtractPath(String moduleWorkingDir, String moduleName) {
    return getModuleExtractDir(moduleWorkingDir) + WSClientsConstants.SEPARATOR + getModuleExtractFileName(moduleName);
  }

  public static String getModuleJarName(String moduleName) {
    return IOUtil.getAsJarName(moduleName);
  }

  public static String getModuleJarPath(String webServicesDir, String moduleName) {
    return getAppJarsDir(webServicesDir) + WebServicesConstants.SEPARATOR + moduleName;
  }

  public static String getModuleMappingsFileName() {
    return WebServicesConstants.MODULE_MAPPINS_FILE_NAME;
  }

  public static String getModuleMappingsPath(String webServicesDir) {
    return getAppJarsDir(webServicesDir) + WebServicesConstants.SEPARATOR + getModuleMappingsFileName();
  }

  public static String getDeployedComponestPerModuleFileName() {
    return WebServicesConstants.DEPLOYED_COMPONENTS_PER_MODULE_FILE_NAME;
  }

  public static String getDeployedComponentsPerModuleParentDir(String webServicesDir) {
    return getAppJarsDir(webServicesDir);
  }

  public static String getDeployedComponentsPerModulePath(String webServicesDir) {
    return getDeployedComponentsPerModuleParentDir(webServicesDir) + WebServicesConstants.SEPARATOR + getDeployedComponestPerModuleFileName();
  }

  public static Properties generateDefaultModuleMappings(File[] files) {
    HashSet fileNamesExcludeList = new HashSet();
    fileNamesExcludeList.add(WSDirsHandler.getDeployedComponestPerModuleFileName());
    return generateDefaultModuleMappings(files, fileNamesExcludeList);
  }

  public static Properties generateDefaultModuleMappings(File[] files, Set fileNamesExcludeList) {
    if(files == null) {
      return new Properties();
    }

    Properties moduleMappings = new Properties();
    for(int i = 0; i < files.length; i++) {
      File file = files[i];
      String fileName = file.getName();
      if(!fileNamesExcludeList.contains(fileName)) {
        moduleMappings.put(fileName, fileName);
      }
    }

    return moduleMappings;
  }

}
