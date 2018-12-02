package com.sap.engine.services.webservices.webservices630.server.deploy.common;

import com.sap.engine.frame.core.configuration.Configuration;

import java.util.Hashtable;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2003
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */
public class AppDeployInfo {

  String applicationName = null;
  ClassLoader loader = null;
  Configuration appConfiguration = null;
  Hashtable webMappings = new Hashtable();

  public AppDeployInfo() {
  }

  public String getApplicationName() {
    return applicationName;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  public Configuration getAppConfiguration() {
    return appConfiguration;
  }

  public void setAppConfiguration(Configuration appConfiguration) {
    this.appConfiguration = appConfiguration;
  }

  public AppDeployInfo(Hashtable webMappings) {
    this.webMappings = webMappings;
  }

  public Hashtable getWebMappings() {
    return webMappings;
  }

  public void setWebMappings(Hashtable webMappings) {
    this.webMappings = webMappings;
  }

  public void setLoader(ClassLoader loader) {
    this.loader = loader;
  }
  
  public ClassLoader getLoader() {
    return loader;
  }
}
