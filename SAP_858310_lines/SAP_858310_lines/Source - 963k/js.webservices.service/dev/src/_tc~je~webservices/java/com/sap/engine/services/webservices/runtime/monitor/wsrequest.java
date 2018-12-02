/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.runtime.monitor;

/**
 * @author Alexander Zubev
 */
public class WSRequest {
  long requestID;
  String configName;
  WSRequests owner;
  boolean successful;
  //operation data
  String operationName;
  long prePrcssTime;
  long implPrcssTime;
  long postPrcssTime;

  public void endRequest(boolean success) {
    this.successful = success;
    this.owner.endRequest(this);
  }

  public void endRequest(boolean success, String operationName, long prePrcssTime, long implPrcssTime, long postPrcssTime) {
    this.operationName = operationName;
    this.prePrcssTime = prePrcssTime;
    this.implPrcssTime = implPrcssTime;
    this.postPrcssTime = postPrcssTime;
    endRequest(success);
  }

}
