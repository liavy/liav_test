package com.sap.engine.services.webservices.webservices630.server.deploy.ws;

import com.sap.engine.services.webservices.runtime.definition.WSRuntimeDefinition;
import com.sap.engine.frame.core.configuration.Configuration;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSBaseContext {

  private String applicationName = null;
  private WSRuntimeDefinition[] wsRuntimeDefinitions = new WSRuntimeDefinition[0];
  private Configuration appConfiguration = null;

  public WSBaseContext() {
  }

  public WSBaseContext(String applicationName, WSRuntimeDefinition[] wsRuntimeDefinitions, Configuration appConfiguration) {
    this.applicationName = applicationName;
    this.wsRuntimeDefinitions = wsRuntimeDefinitions;
    this.appConfiguration = appConfiguration;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  public WSRuntimeDefinition[] getWsRuntimeDefinitions() {
    return wsRuntimeDefinitions;
  }

  public void setWsRuntimeDefinitions(WSRuntimeDefinition[] wsRuntimeDefinitions) {
    this.wsRuntimeDefinitions = wsRuntimeDefinitions;
  }

  public Configuration getAppConfiguration() {
    return appConfiguration;
  }

  public void setAppConfiguration(Configuration appConfiguration) {
    this.appConfiguration = appConfiguration;
  }

}
