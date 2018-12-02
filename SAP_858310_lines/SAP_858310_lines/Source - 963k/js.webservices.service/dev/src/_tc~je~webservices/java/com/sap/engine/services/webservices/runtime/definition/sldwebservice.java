/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.runtime.definition;

import java.io.Serializable;

/**
 * @author Alexander Zubev (alexander.zubev@sap.com)
 */
public class SLDWebService implements Serializable {
  private String name;
  private String caption;
  private String wsdlLocation;
  private String description;
  
  /**
   * @return
   */
  public String getCaption() {
    if (caption == null) {
      return "";
    }
    return caption;
  }

  /**
   * @return
   */
  public String getDescription() {
    if (description == null) {
      return "";
    }
    return description;
  }

  /**
   * @return
   */
  public String getName() {
    if (name == null) {
      return "";
    }
    return name;
  }

  /**
   * @return
   */
  public String getWsdlLocation() {
    if (wsdlLocation == null) {
      return "";
    }
    return wsdlLocation;
  }

  /**
   * @param string
   */
  public void setCaption(String caption) {
    this.caption = caption;
  }

  /**
   * @param string
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @param string
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @param string
   */
  public void setWsdlLocation(String wsdlLocation) {
    this.wsdlLocation = wsdlLocation;
  }
}
