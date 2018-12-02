/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 */
package com.sap.engine.services.webservices.runtime.monitor;

import com.sap.engine.lib.util.HashMapObjectObject;

/**
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */
public class WSConfigData {

  long successfulReqs;
  long failedReqs;
  //Key String(OperationName), value WSOperationData
  private HashMapObjectObject operations = new HashMapObjectObject();

  public long getSuccessfulReq() {
    return successfulReqs;
  }

  public long getUnsuccessfulReq() {
    return failedReqs;
  }

  synchronized void processOperationData(WSRequest request) {
    //there is data to be processed(it is not switched on)
    if (request.operationName == null) {
      return;
    }

    WSOperationData wsOper = getWSOperationData(request.operationName);

    wsOper.registerOperationTimes(request.prePrcssTime, request.implPrcssTime, request.postPrcssTime);
  }
  
  public synchronized WSOperationData getWSOperationData(String operationName) {
    WSOperationData wsOper = (WSOperationData) operations.get(operationName);
    if (wsOper == null) {
      wsOper = new WSOperationData(operationName);
      operations.put(operationName, wsOper);
    }
    return (WSOperationData) operations.get(operationName);
  }
}
