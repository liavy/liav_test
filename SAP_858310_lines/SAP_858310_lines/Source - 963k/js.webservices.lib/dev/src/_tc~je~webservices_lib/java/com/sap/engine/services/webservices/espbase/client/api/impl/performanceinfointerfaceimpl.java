package com.sap.engine.services.webservices.espbase.client.api.impl;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.api.PerformanceInfoInterface;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.client.performance.PerformanceInfo;

public class PerformanceInfoInterfaceImpl implements PerformanceInfoInterface {
  
  private PerformanceInfo performanceInfo;
  
  public PerformanceInfoInterfaceImpl(ClientConfigurationContext clientCfgCtx) {
    ConfigurationContext dynamicCtx = clientCfgCtx.getDynamicContext();
    performanceInfo = (PerformanceInfo)(dynamicCtx.getProperty(PublicProperties.P_PERFORMANCE_INFO));
    if(performanceInfo == null) {
      performanceInfo = new PerformanceInfo();
      dynamicCtx.setProperty(PublicProperties.P_PERFORMANCE_INFO, performanceInfo);
    }
  }
  
  public long getRequestPrepareTime() {
    return(performanceInfo.getTime(PerformanceInfo.REQUEST_PREPARE_INFO_TYPE));
  }
  
  public long getResponseProcessTime() {
    return(performanceInfo.getTime(PerformanceInfo.RESPONSE_PROCESS_INFO_TYPE));
  }
  
  public long getBackendResponseTime() {
    return(performanceInfo.getTime(PerformanceInfo.BACKEND_RESPONSE_INFO_TYPE));
  }
}
