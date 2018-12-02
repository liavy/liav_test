package com.sap.engine.services.webservices.runtime.definition;

import java.util.Properties;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      1.0
 */
public final class FaultImpl implements com.sap.engine.interfaces.webservices.runtime.Fault {

  private String javaClassName;
  private String faultName;
  private com.sap.engine.interfaces.webservices.runtime.Config faultConfiguration;

  //for WSD Sasho use
  private String faultDefaultNS;

  public void setJavaClassName(String javaClassName) {
    this.javaClassName = javaClassName;
  }

  public String getJavaClassName() {
    return this.javaClassName;
  }

  public void setFaultName(String faultName) {
    this.faultName = faultName;
  }

  public String getFaultName() {
    return faultName;
  }

  public void setFaultConfiguration(Properties faultConfiguration) {
    this.faultConfiguration = new ConfigImpl(faultConfiguration);
  }

  public void setFaultConfiguration(com.sap.engine.interfaces.webservices.runtime.Config faultConfiguration) {
    this.faultConfiguration = faultConfiguration;
  }

  public com.sap.engine.interfaces.webservices.runtime.Config getFaultConfiguration() {
    return faultConfiguration;
  }

  public String getFaultDefaultNS() {
    return faultDefaultNS;
  }

  public void setFaultDefaultNS(String faultDefaultNS) {
    this.faultDefaultNS = faultDefaultNS;
  }
}

