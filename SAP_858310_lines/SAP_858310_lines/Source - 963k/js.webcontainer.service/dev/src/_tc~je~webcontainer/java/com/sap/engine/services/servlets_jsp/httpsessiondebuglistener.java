package com.sap.engine.services.servlets_jsp;

/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
public interface HttpSessionDebugListener {

  public boolean startDebugRequest(String sessionid, String debugParamValue);

  public void endSession(String sessionId);

  public boolean endRequest(String sessionId);
}
