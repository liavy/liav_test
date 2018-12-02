/*
 * Copyright (c) 2000-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.lib.util;

import com.sap.engine.session.exec.ExecutionDetails;

/**
 * This class is used to provide client IP to the Session Management
 * See ApplicationSelector
 */
public class WebContainerExecituionDetails implements ExecutionDetails {

  String clientIP = null;

  public WebContainerExecituionDetails(String ip){
    clientIP = ip;
  }

  public Object getExecutionDetails() {
    return this;
  }

  /**
   * Return the IP of the client. It is obatined form FCA connection or from X-Forwarded-For header
   * @return IP client as String
   */
  public String getHost() {
    return clientIP;
  }

  public String toString() {
    return super.toString()+" Client IP ["+clientIP+"]";
  }
}
