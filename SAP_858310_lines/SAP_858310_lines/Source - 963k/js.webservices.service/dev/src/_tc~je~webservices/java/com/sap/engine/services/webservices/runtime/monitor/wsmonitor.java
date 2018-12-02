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

import com.sap.engine.services.webservices.runtime.definition.ServiceEndpointDefinition;
import com.sap.engine.interfaces.webservices.runtime.definition.WSIdentifier;
import com.sap.engine.lib.util.HashMapObjectObject;

/**
 * @author Alexander Zubev
 */
public class WSMonitor {
  //Key WSIdeintifier, value WSRequests object
  private static HashMapObjectObject requests = new HashMapObjectObject();

  public static synchronized WSRequests getWSRequestsContainer(WSIdentifier wsID) {
    WSRequests ws = (WSRequests) requests.get(wsID);
    if (ws == null) {
      ws = new WSRequests();
      requests.put(wsID, ws);
    }
    return ws;
  }

  public static WSRequest startRequest(ServiceEndpointDefinition endPoint) {
    WSRequests ws = getWSRequestsContainer(endPoint.getOwner().getWSIdentifier());
    return ws.startRequest(endPoint);
  }

  public static void clear() {
    requests.clear();
  }

//  public static void requestSize(WSRuntimeDefinition webService, long size) {
////    WSRequests ws = getWSRequestsContainer(webService.getWSIdentifier());
////    ws.setRequestSize(size);
//  }
//
//  public static void responseSize(WSRuntimeDefinition webService, long size) {
////    WSRequests ws = getWSRequestsContainer(webService.getWSIdentifier());
////    ws.setResponseSize(size);
//  }
//
//  public static void endRequest(WSRequest wsReq, boolean successful) {
//    wsReq.owner.endRequest();get
//    WSRequests ws = getWSRequestsContainer(webService.getWSIdentifier());
//    ws.endRequest(requestID, successful);
//  }
}
