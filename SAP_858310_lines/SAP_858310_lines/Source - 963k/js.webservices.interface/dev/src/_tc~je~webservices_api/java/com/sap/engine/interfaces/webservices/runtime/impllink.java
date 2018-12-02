/**
 * Copyright (c) 2002 by SAP Labs Bulgaria.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime;

import java.util.Properties;

/**
 *   Holds the implementation specific information
 * in form name-value.
 *
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class ImplLink implements java.io.Serializable {

  private String implId = null;
  private Properties properties = null;

  public ImplLink() {
  }

  /**
   *  Returns the ID of this implementation link.
   */
  public String getImplId() {
    return implId;
  }

  public void setImplId(String implId) {
    this.implId = implId;
  }

  /**
   *  Returns a reference to internal the java.util.Properties object.
   */
  public Properties getProperties() {
    if (properties == null) {
      properties = new Properties();
    }

    return properties;
  }

  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  public String toString() {
    return "ID: '" + implId + "', properties: " + properties;
  }

}