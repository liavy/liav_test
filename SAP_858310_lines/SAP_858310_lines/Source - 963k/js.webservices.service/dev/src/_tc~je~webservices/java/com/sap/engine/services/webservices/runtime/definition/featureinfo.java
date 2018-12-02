package com.sap.engine.services.webservices.runtime.definition;

import java.util.Properties;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */
public class FeatureInfo implements com.sap.engine.interfaces.webservices.runtime.Feature {

  private String featureName = null;
  private String protocolId = null;
  private com.sap.engine.interfaces.webservices.runtime.Config configuration = null;

  public FeatureInfo() {

  }

  public FeatureInfo(String name, String protocolId, Properties configuration) {
    this.featureName = name;
    this.protocolId = protocolId;
    this.configuration = new ConfigImpl(configuration);
  }

  public String getFeatureName() {
    return featureName;
  }

  public String getProtocolID() {
    return protocolId;
  }

  public void setName(String name) {
    this.featureName = name;
  }

//  public String getName() {
//    return featureName;
//  }

  public void setProtocol(String protocolId) {
    this.protocolId = protocolId;
  }

//  public String getProtocol() {
//    return protocolId;
//  }

  public void setConfiguration(Properties properties) {
    this.configuration = new ConfigImpl(properties);
  }

  public void setConfiguration(com.sap.engine.interfaces.webservices.runtime.Config configuration) {
    this.configuration = configuration;
  }

  public com.sap.engine.interfaces.webservices.runtime.Config getConfiguration() {
    return configuration;
  }

}

