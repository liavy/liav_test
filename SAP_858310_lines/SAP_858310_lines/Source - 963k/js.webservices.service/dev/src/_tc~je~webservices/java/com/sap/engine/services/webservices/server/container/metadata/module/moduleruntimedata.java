/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
 
package com.sap.engine.services.webservices.server.container.metadata.module;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Title: ModuleRuntimeData
 * Description: ModuleRuntimeData
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class ModuleRuntimeData {
  
  public static String MODULE_DIR_NAME = "module_dir_name";
  public static String ARCHIVE_FILE = "archive_file";
  
  public static String META_INF = "META-INF";
  public static String WEB_INF = "WEB-INF";
  
  public static String EJB_EXTENSION = ".jar";
  public static String WEB_EXTENSION = ".war";
  public static String PRT_EXTENSION = ".par";
  public static String WS_EXTENSION = ".wsar";
  
  public static String EJB_SUFFIX = "_EJB";
  public static String WEB_SUFFIX = "_WEB";
  public static String PRT_SUFFIX = "_PRT";
  public static String WS_SUFFIX = "_WS";  
  
  public static Hashtable moduleMappingTable;
  
  private String moduleName;  
  private String moduleType; 
  private String moduleDir;
  private String moduleWorkingDir;  
  private String archiveFileRelPath; 
  private String metaInfRelDir; 
  private String binRelDir; 
  
  static {
    moduleMappingTable = new Hashtable(); 
    moduleMappingTable.put(EJB_EXTENSION, EJB_SUFFIX);
    moduleMappingTable.put(WEB_EXTENSION, WEB_SUFFIX);
    moduleMappingTable.put(PRT_EXTENSION, PRT_SUFFIX);
    moduleMappingTable.put(WS_EXTENSION, WS_SUFFIX);    
  }
   
  public ModuleRuntimeData() {
  }
  
  public ModuleRuntimeData(String moduleName, String moduleDir, String moduleWorkingDir) {
    this.moduleName = moduleName;
    this.moduleType = getType(moduleName); 
    this.moduleDir = moduleDir; 
    this.moduleWorkingDir = moduleWorkingDir;
    this.metaInfRelDir = getMetaInfRelDir(moduleType);
    this.binRelDir = getBinRelDir(moduleType); 
  }
  
  public ModuleRuntimeData(String moduleName, String archiveFileRelPath, String moduleDir, String moduleWorkingDir) {
    this.moduleName = moduleName;
    this.moduleType = getType(moduleName); 
    this.moduleDir = moduleDir; 
    this.moduleWorkingDir = moduleWorkingDir;
    this.archiveFileRelPath = archiveFileRelPath;
    this.metaInfRelDir = getMetaInfRelDir(moduleType);
    this.binRelDir = getBinRelDir(moduleType); 
  }
  
  public ModuleRuntimeData(String moduleName, String parentDir, String parentWorkingDir, boolean useDefaultDir) {
    this.moduleName = moduleName;
    this.moduleType = getType(moduleName); 
    if(useDefaultDir) {    
      String moduleDirName = getModuleDirName(moduleName);
      this.moduleDir = parentDir + "/" + moduleDirName;
      this.moduleWorkingDir = parentWorkingDir + "/" + moduleDirName; 
    }    
    this.metaInfRelDir = getMetaInfRelDir(moduleType);
    this.binRelDir = getBinRelDir(moduleType); 
  }
  
  public ModuleRuntimeData(String moduleName, String archiveFileRelPath, String parentDir, String parentWorkingDir, boolean useDefaultDir) {
    this.moduleName = moduleName;
    this.moduleType = getType(moduleName); 
    if(useDefaultDir) {    
      String moduleDirName = getModuleDirName(moduleName);
      this.moduleDir = parentDir + "/" + moduleDirName;
      this.moduleWorkingDir = parentWorkingDir + "/" + moduleDirName; 
    }
    this.archiveFileRelPath = archiveFileRelPath;
    this.metaInfRelDir = getMetaInfRelDir(moduleType);
    this.binRelDir = getBinRelDir(moduleType);
  }    
  
  /**
   * @return - module name
   */
  public String getModuleName() {
    return moduleName;
  }
  
  /**
   * @return - type
   */
  public String getType() {
    return moduleType;
  }

  /**
   * @return - module directory 
   */
  public String getModuleDir() {
    return moduleDir;
  }
   
  /**
   * @return - module working directory
   */
  public String getModuleWorkingDir() {
    return moduleWorkingDir;
  }

  /**
   * @param archiveFileRelPath
   */
  public void setArchiveFileRelPath(String archiveFileRelPath) {
    this.archiveFileRelPath = archiveFileRelPath;
  } 
  
  /**
   * @return - archive file relative path
   */
  public String getArchiveFileRelPath() {
    return archiveFileRelPath;
  }
  
  /**
   * @return - meta inf rel dir 
   */
  public String getMetaInfRelDir() {
    return metaInfRelDir;
  }
  
  /**
   * @return - binary rel dir 
   */
  public String getBinRelDir() {
    return binRelDir; 	  
  }
  
  public static String getType(String fileName) {
    int dotIndex = fileName.lastIndexOf("."); 
    if(dotIndex != -1) {       
      return (String)moduleMappingTable.get(fileName.substring(dotIndex)); 
    } 

    return null;
  }

  public static String getType(File file) {
    return getType(file.getName());
  }
    
  public static String getModuleDirName(String moduleName) {
    String moduleDirName = moduleName;
    String extension;
    Enumeration enum1 = moduleMappingTable.keys();    
    while(enum1.hasMoreElements()) {
      extension = (String)enum1.nextElement();
      if(moduleName.endsWith(extension)) {
        moduleDirName = moduleName.substring(0, moduleName.lastIndexOf(extension)) + moduleMappingTable.get(extension);
      }           
    }
    
    return moduleDirName;  
  }
  
  public String getFilePathName(String relativeFilePathName) {
    return moduleDir + "/" + relativeFilePathName;
  }
  
  public static String getMetaInfRelDir(String moduleType) { 
    if(moduleType == null) {
      return META_INF; 
    }
    if(moduleType.equals(EJB_SUFFIX)) {
      return META_INF; 
    }
    if(moduleType.equals(WEB_SUFFIX)) {
      return WEB_INF;
    }    
    if(moduleType.equals(WS_SUFFIX)) {
      return META_INF;
    }
    
    return META_INF;    
  }
  
  public static String getBinRelDir(String moduleType) { 
    if(moduleType == null) {
      return ""; 
    }    
    if(moduleType.equals(EJB_SUFFIX)) {
      return ""; 
    }
    if(moduleType.equals(WEB_SUFFIX)) {
      return WEB_INF + "/classes";
    }    
    if(moduleType.equals(WS_SUFFIX)) {
      return "";
    }
    
    return "";    
  }
  
  public String toString() {
    String resultStr = "";
    String nl = System.getProperty("line.separator"); 
    
    resultStr += "Module name             : " + moduleName + nl;
    resultStr += "Module type             : " + moduleType + nl; 
    resultStr += "Module directory        : " + moduleDir + nl;
    resultStr += "Module working directory: " + moduleWorkingDir + nl;
    resultStr += "Archive file rel path   : " + archiveFileRelPath == null ? archiveFileRelPath : "N/A";
    resultStr += "Meta inf rel dir        : " + metaInfRelDir;   
    
    return resultStr;       
  }

}
