/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.jspparser_api;

import com.sap.engine.lib.util.ArrayObject;
import com.sap.engine.lib.util.ConcurrentHashMapObjectObject;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;


/**
 * needed for TagFileParser and GenerateTagFileJavaFIle
 * @author Todor Mollov, Bojidar Kadrev
 * @version 7.0
 */
public class TagCompilerParams {

  private String workDir = null; 

  private ArrayObject jarClassPathHashtable = null; //context.getJarClassPathHashtable(new ArrayObject());
  
  //TODO: Remove as jarClassPathHashtable holds the whole classpath of the app
  private String[] resourceNames = null; //serviceContext.getLoadContext().getResourceNames(applicationName)

  private ConcurrentHashMapObjectObject tagFiles = null; //context.getWebComponents().getTagFiles()

  private ConcurrentHashMapObjectObject exceptionsTable = null;
  
  /**
   * The java version may be set in the <java-version> element in the application-j2ee-engine.xml of an application.
   * The currently accepted values are 1.4 and 1.5.
   * If not explicitly set, the java version is set by default at the latest accepted � currently 1.5.
   * No sub versions are accepted � for example if <java-version>1.4.3</java-version> is specified,
   * it will be evaluated and a version 1.4 will be stored for the application.
   * This version must be used for compiling jsp files.
   */
  private String javaVersionForCompilation;

  private TagCompilerParams(){
    
  }
  
  /**
   * TODO: Remove as the empty constructor is used.
   * Wait until deprecated facade is removed (portal stops compiling old portal JSP parser).
   * @param workDir
   * @param jarClassPathHashtable
   * @param resourceNames
   * @param tagFiles
   * @param exceptionsTable
   * @deprecated
   */
  public TagCompilerParams(String workDir, ArrayObject jarClassPathHashtable, String[] resourceNames, 
      ConcurrentHashMapObjectObject tagFiles, ConcurrentHashMapObjectObject exceptionsTable
      ) {
    this.workDir = workDir;
    this.jarClassPathHashtable = jarClassPathHashtable;
    
    //TODO: Remove as jarClassPathHashtable holds the whole classpath of the app
    this.resourceNames = resourceNames;
    
    this.tagFiles = tagFiles;
    this.exceptionsTable = exceptionsTable;
    
  }

  /**
   * Returns set of jars to be included in classpath
   * @return ArrayObject
   */
  public ArrayObject getJarClassPathHashtable() {
    return jarClassPathHashtable;
  }

  //TODO: Remove as jarClassPathHashtable holds the whole classpath of the app
  /**
   *
   * @deprecated use  getJarClassPathHashtable
   * @return
   */
  public String[] getResourceNames() {
    return resourceNames;
  }

  /**
   * Hashmap containing TagFileInfos for tag files
   * @return ConcurrentHashMapObjectObject
   */
  public ConcurrentHashMapObjectObject getTagFiles() {
    return tagFiles;
  }

  /**
   * Returns the place where generated java files are stored. 
   * @return  path to work directory
   */
  public String getWorkDir() {
    return workDir;
  }

  /**
   * Holds all parsing exceptions for the current TagParser. 
   * New entries are written in the hashmap when tld is created for the tag files in goven directory 
   * but some of them throws translation time exception.
   * @return ConcurrentHashMapObjectObject
   */
  public ConcurrentHashMapObjectObject getExceptionsTable() {
    return exceptionsTable;
  }
  /**
   * Setter for Hashmap containing Exceptions
   * @param exceptionsTable The ConcurrentHashMapObjectObject to set.
   */
  public void setExceptionsTable(ConcurrentHashMapObjectObject exceptionsTable) {
    this.exceptionsTable = exceptionsTable;
  }
  /**
   * Sets ArrayObject containing jars to be added in tha classpath for compilation
   * @param jarClassPathHashtable The ArrayObject to set.
   */
  public void setJarClassPathHashtable(ArrayObject jarClassPathHashtable) {
    this.jarClassPathHashtable = jarClassPathHashtable;
  }
  /**
   * @deprecated
   * @param resourceNames The resourceNames to set.
   */
  public void setResourceNames(String[] resourceNames) {
    this.resourceNames = resourceNames;
  }
  /**
   * Setter for tag files
   * @param tagFiles ConcurrentHashMapObjectObject containing the tagFiles to be set.
   */
  public void setTagFiles(ConcurrentHashMapObjectObject tagFiles) {
    this.tagFiles = tagFiles;
  }
  /**
   * Setter for work directory
   * @param workDir path to work directory.
   */
  public void setWorkDir(String workDir) {
    this.workDir = workDir;
  }

  /**
   * The java version may be set in the <java-version> element in the application-j2ee-engine.xml of an application.
   * The currently accepted values are 1.4 and 1.5.
   * If not explicitly set, the java version is set by default at the latest accepted - currently 1.5.
   * No sub versions are accepted - for example if <java-version>1.4.3</java-version> is specified,
   * it will be evaluated and a version 1.4 will be stored for the application.
   * This version must be used for compiling jsp files.
   * @return String with Java version
   */
  public String getJavaVersionForCompilation() {
    return javaVersionForCompilation;
  }


  
  /**
   * Factory method for creating TagCompilerParams instances.
   * @param applicationContext ApplicationContext object representing current application
   * @return TagCompilerParams
   */
  public static TagCompilerParams createInstance(ApplicationContext applicationContext ) {
    TagCompilerParams compilerParams = new TagCompilerParams();
    compilerParams.setWorkDir(applicationContext.getWorkingDir());      
    compilerParams.setJarClassPathHashtable(applicationContext.getJarClassPathHashtable());
    compilerParams.setResourceNames(ServiceContext.getServiceContext().getLoadContext().getResourceNames(applicationContext.getApplicationName()));
    compilerParams.setTagFiles(applicationContext.getWebComponents().getTagFiles());
    compilerParams.setExceptionsTable(new ConcurrentHashMapObjectObject());
    compilerParams.javaVersionForCompilation = applicationContext.getJavaVersionForCompilation();
    return compilerParams;
  }
}