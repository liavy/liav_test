package com.sap.engine.services.webservices.espbase.client.api;

public interface PerformanceInfoInterface {

  public static final long NOT_MEASURED = -1;
  
  long getRequestPrepareTime();
  
  long getResponseProcessTime();
  
  long getBackendResponseTime();
}
