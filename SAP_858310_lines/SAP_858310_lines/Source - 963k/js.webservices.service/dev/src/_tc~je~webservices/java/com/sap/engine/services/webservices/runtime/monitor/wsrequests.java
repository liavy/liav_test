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

import com.sap.engine.lib.util.HashMapLongObject;
import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.services.webservices.runtime.definition.ServiceEndpointDefinition;
import com.sap.engine.services.webservices.tools.InstancesPool;

/**
 * @author Alexander Zubev
 */
public class WSRequests {
  private long successfulRequests = 0;
  private long failedRequests = 0;
  //key String(requestID), value WSRequest
  private HashMapLongObject runningClients = new HashMapLongObject();
  //key String(configName), value WSConfigData
  private HashMapObjectObject wsConfigs = new HashMapObjectObject();
  //used as UID for the request
  private long reqCounter = 0;
  //static pool for WSRequest objects
  private InstancesPool wsReqPool = new InstancesPool(8, 16);

  public synchronized WSRequest startRequest(ServiceEndpointDefinition endPointDefinition) {
    WSRequest request = (WSRequest) wsReqPool.getInstance();
    if (request == null) {
      request = new WSRequest();
    }
    String cfgName = endPointDefinition.getConfigurationName();
    //initilizing the request object
    request.owner = this;
    request.requestID = reqCounter++;
    request.configName = cfgName;
    request.successful = false;
    request.operationName = null;
    //registering it in the tabales
    runningClients.put(request.requestID, request);
    getConfigData(cfgName);
    return request;
  }

  public synchronized void endRequest(WSRequest request) {
    request = (WSRequest) runningClients.remove(request.requestID);
    WSConfigData wsConf = (WSConfigData) wsConfigs.get(request.configName);
    if (request.successful) {
      successfulRequests++;
      wsConf.successfulReqs++;
      wsConf.processOperationData(request);
    } else {
      failedRequests++;
      wsConf.failedReqs++;
    }
    //reusing the request instance
    wsReqPool.rollBackInstance(request);
  }

  public long getSuccessfulRequests() {
    return successfulRequests;
  }

  public long getFailedRequests() {
    return failedRequests;
  }

  public synchronized long getRunningClients() {
    return runningClients.getAllKeys().length;
  }

  public synchronized WSConfigData getConfigData(String configName) {
    WSConfigData wsConfig = (WSConfigData) wsConfigs.get(configName);
    if (wsConfig == null) {
      wsConfig = new WSConfigData();
      wsConfigs.put(configName, wsConfig);
    }

    return wsConfig;
  }


}
