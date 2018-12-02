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

import com.sap.engine.lib.util.ConcurrentHashMapObjectObject;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.IDCounter;
import com.sap.engine.services.servlets_jsp.server.application.JspConfiguration;

/**
 * Transfers necessary attributes taken from Application context
 * @author Todor Mollov, Bojidar Kadrev
 * 
 * @version 7.0
 */
public class ParserParameters {

  /**
   * Computed props for the current file name
   */
  private JspConfigurationProperties jspConfigurationProperties = null;

  /**
   * The whole JspConfig , used further for staticaly included files
   */
  private JspConfiguration jspConfiguration = null;

  private ConcurrentHashMapObjectObject tagLibraryDescriptors = null; // context.getWebComponents().getTagLibraryDescriptors()

  private ConcurrentHashMapObjectObject tagLibraryValidators = null;

  private ConcurrentHashMapObjectObject includedFiles = null;

  private ConcurrentHashMapObjectObject portalProperties = new ConcurrentHashMapObjectObject();

  private String applicationRootDir = null;

  private TagCompilerParams tagCompilerParams = null;

  private ClassLoader appClassLoader = null;
  
  private IDCounter jspIdGenerator = null;
  
  /**
   * If the JSP is in http alias , this is the path for this http alias.
   * If the JSP is not in http alias, this is null.
   */
  private String httpAliasValue = null;

  /**
   * Default constructor.
   */
  public ParserParameters(){

  }

  /**
   * Used to initialize the data that previously was taken from Application and
   * Service Contexct use just after parser creation
   *
   * @param tagLibraryDescriptors   Hashmap containig TagLibraryDescriptors
   * @param tagLibraryValidators   Hashmap containig TagLibrary validators
   * @param webApplicationRootDir  the path ot root's directory of the application
   * @param jspConfigurationProperties JspConfigurationProperties object containing all properties for the JSP in the web descriptor
   * @param jspConfiguration  JspConfiguration object with jsp configuration from web descriptor
   * @param appClassLoader  application's classloader
   * @param tagCompilerParams the parameters for Tag compiler
   * @param includedFiles  Hashmap containing all included JSP/tag files
   * @param portalParameters Hashmap containing portal specific properties
   */
  public ParserParameters(ConcurrentHashMapObjectObject tagLibraryDescriptors, ConcurrentHashMapObjectObject tagLibraryValidators, String webApplicationRootDir,
                          JspConfigurationProperties jspConfigurationProperties, JspConfiguration jspConfiguration, ClassLoader appClassLoader, TagCompilerParams tagCompilerParams,
                          ConcurrentHashMapObjectObject includedFiles, ConcurrentHashMapObjectObject portalParameters,
                          IDCounter jspIdGenerator) {
    this.appClassLoader = appClassLoader;
    this.applicationRootDir = webApplicationRootDir;
    this.tagLibraryDescriptors = tagLibraryDescriptors;
    this.tagLibraryValidators = tagLibraryValidators;
    this.jspConfigurationProperties = jspConfigurationProperties;
    this.jspConfiguration = jspConfiguration;
    this.tagCompilerParams = tagCompilerParams;
    this.includedFiles = includedFiles;
    this.portalProperties = portalParameters;
    this.jspIdGenerator = jspIdGenerator;
  }


  /**
   * Getter for TLDs 
   * @return Hashtable containig TagLibraryDescriptors
   */
  public ConcurrentHashMapObjectObject getTagLibraryDescriptors() {
    return tagLibraryDescriptors;
  }

  /**
   * Uses ParseUtils.separator.
   * @return path to the application 
   */
  public String getApplicationRootDir() {
    return applicationRootDir;
  }

  /**
   * Computed JspConfigurationProperties for the current file
   * @return JspConfigurationProperties
   */
  public JspConfigurationProperties getJspConfigurationProperties() {
    return jspConfigurationProperties;
  }

  /**
   * The whole JspConfiguration for current application, used further for staticaly included files
   * @return  JspConfiguration
   */
  public JspConfiguration getJspConfiguration() {
    return jspConfiguration;
  }

  /**
   * Return TagCompilerParams which holds TagFile parser settings
   * @return TagCompilerParams
   */
  public TagCompilerParams getTagCompilerParams() {
    return tagCompilerParams;
  }

  /**
   * Hashmap containing all TLD validators
   * @return ConcurrentHashMapObjectObject
   */
  public ConcurrentHashMapObjectObject getTagLibraryValidators() {
    return tagLibraryValidators;
  }

  /**
   * Application's classloader used for tag parsing
   * 
   * @return Application's classloader used for tag parsing
   */
  public ClassLoader getAppClassLoader() {
    return appClassLoader;
  }

  /**
   * Returns the Hashmap containing all included (via include directive) files for JSP file
   * @return  ConcurrentHashMapObjectObject
   */
  public ConcurrentHashMapObjectObject getIncludedFilesHashtable() {
    return includedFiles;
  }

  /**
   * Setter for the Hashmap containing all included (via include directive) files for JSP file
   * @param files Hashmap containing included files
   */
  public void setIncludedFilesHashtable(ConcurrentHashMapObjectObject files) {
    this.includedFiles = files;
  }

  /**
   * Sets properties that only portal parser instance will use
   * @return ConcurrentHashMapObjectObject
   */
  public ConcurrentHashMapObjectObject getPortalProperties() {
    return portalProperties;
  }

  /**
   * Gets the properties that only the portal parser will use.
   * @param portalProperties Hashmap with properties
   */
  public void setPortalProperties(ConcurrentHashMapObjectObject portalProperties) {
    this.portalProperties = portalProperties;
  }

  /**
   * Sets the tag compiler parameters if they are null. You cannot override the reference if once set.
   * @param tagCompilerParams TagCompilerParams
   */
  public void setTagCompilerParams(TagCompilerParams tagCompilerParams) {
    if( this.tagCompilerParams == null ){
      this.tagCompilerParams = tagCompilerParams;
    }
  }
  /**
   * Setter for application classloader
   * @param appClassLoader The appClassLoader to set.
   */
  public void setAppClassLoader(ClassLoader appClassLoader) {
    this.appClassLoader = appClassLoader;
  }
  /**
   * Setter for the root directory of application
   * @param applicationRootDir The applicationRootDir to set.
   */
  public void setApplicationRootDir(String applicationRootDir) {
    this.applicationRootDir = applicationRootDir;
  }
  /**
   * Sets computed JspConfigurationProperties for current file
   * @param jspConfigurationProperties The jspConfigurationProperties to set.
   */
  public void setJspConfigurationProperties(JspConfigurationProperties jspConfigurationProperties) {
    this.jspConfigurationProperties = jspConfigurationProperties;
  }

  /**
   * Sets whole JSPConfiguration for the application
   * @param jspConfiguration   The JspConfiguration to set
   */
  public void setJspConfiguration(JspConfiguration jspConfiguration) {
    this.jspConfiguration = jspConfiguration;
  }
  /**
   * Setter for  TagLibraryDescriptors
   * @param tagLibraryDescriptors The tagLibraryDescriptors to set.
   */
  public void setTagLibraryDescriptors(ConcurrentHashMapObjectObject tagLibraryDescriptors) {
    this.tagLibraryDescriptors = tagLibraryDescriptors;
  }
  /**
   * Setter for TagLibraryValidators
   * @param tagLibraryValidators The tagLibraryValidators to set.
   */
  public void setTagLibraryValidators(ConcurrentHashMapObjectObject tagLibraryValidators) {
    this.tagLibraryValidators = tagLibraryValidators;
  }

  /**
   * Getter for IDCounter used for generating unique IDs for XML view
   * @return  IDCounter
   */
  public IDCounter getJspIdGenerator() {
    return jspIdGenerator;
  }

  /**
   * Set IDCounter  used for generating unique IDs for XML view
   * @param jspIdGenerator IDCounter
   */
  public void setJspIdGenerator(IDCounter jspIdGenerator) {
    this.jspIdGenerator = jspIdGenerator;
  }

  /**
   * Returns the alias value.
   * If the JSP is in http alias , this is the path for this http alias.
   * If the JSP is not in http alias, this is null.
   * @return the alias value.
   */
  public String getHttpAliasValue() {
    return httpAliasValue;
  }

  /**
   * If the JSP is in http alias , this is the path for this http alias.
   * If the JSP is not in http alias, this is null.
   * @param httpAliasValue String
   */
  public void setHttpAliasValue(String httpAliasValue) {
    this.httpAliasValue = httpAliasValue;
  }
  
}