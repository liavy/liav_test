/*
 * Copyright (c) 2002 by SAP AG.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP AG.
 * Created : 2002-5-23
 */
package com.sap.engine.services.webservices.jaxrpc.wsdl2java;

/**
 * Help class to keep some holder info in one place.
 * @author Chavdar Baykov, Chavdarb@yahoo.com
 * @version 1.0
 */
public class HolderInfo {

  private String holderType;
  private String holderName;

  public String getType() {
    return this.holderType;
  }

  public String getName() {
    return this.holderName;
  }

  public HolderInfo(String holderName, String holderType) {
    this.holderName = holderName;
    this.holderType = holderType;
  }

}

