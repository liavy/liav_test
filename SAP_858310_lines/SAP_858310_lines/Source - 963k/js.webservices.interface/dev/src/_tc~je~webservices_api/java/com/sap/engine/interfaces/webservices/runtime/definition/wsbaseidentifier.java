/*
 * Copyright (c) 2003 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.interfaces.webservices.runtime.definition;

import java.io.Serializable;

/**
 * Copyright (c) 2003, SAP-AG
 * @author Alexander Zubev (alexander.zubev@sap.com)
 * @version 1.1, 2004-2-11
 */
public abstract class WSBaseIdentifier implements Serializable {
  private String applicationName;
  private String jarName;
  private String componentName;
  
  public WSBaseIdentifier() {
  }

  public WSBaseIdentifier(String applicationName, String jarName, String componentName) {
    this.applicationName = applicationName;
    this.jarName = jarName;
    this.componentName = componentName;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  public String getJarName() {
    return jarName;
  }

  public void setJarName(String jarName) {
    this.jarName = jarName;
  }

  public String getComponentName() {
    return componentName;
  }

  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }

  public int hashCode() {
    return applicationName.hashCode() * componentName.hashCode();
  }

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || !(obj instanceof WSBaseIdentifier)) {
      return false;
    }
    WSBaseIdentifier id = (WSBaseIdentifier) obj;
    return this.applicationName.equals(id.getApplicationName())
           && this.componentName.equals(id.getComponentName());
  }

  public String toString() {
    String nl = System.getProperty("line.separator");
    StringBuffer buf = new StringBuffer();
    buf.append(nl);
    buf.append("Application : ");
    buf.append(applicationName);
    buf.append(nl);
    buf.append("Module      : ");
    buf.append(jarName);
    buf.append(nl);
    buf.append("Component   : ");
    buf.append(componentName);
    return buf.toString();
  }

}
