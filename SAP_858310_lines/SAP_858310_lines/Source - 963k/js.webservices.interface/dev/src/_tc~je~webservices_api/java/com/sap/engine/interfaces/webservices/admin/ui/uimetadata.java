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
package com.sap.engine.interfaces.webservices.admin.ui;

/**
 * Copyright (c) 2004, SAP-AG
 * @author Alexander Zubev
 * @version 1.1, 2005-4-26
 */
public class UIMetaData {
  private String namespace;
  private UIProperty[] properties;
  
  public UIMetaData(String namespace) {
    this.namespace = namespace;
  }
  
  /**
   * @return
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * @return
   */
  public UIProperty[] getProperties() {
    return properties;
  }

  /**
   * @param properties
   */
  public void setProperties(UIProperty[] properties) {
    this.properties = properties;
  }
}
