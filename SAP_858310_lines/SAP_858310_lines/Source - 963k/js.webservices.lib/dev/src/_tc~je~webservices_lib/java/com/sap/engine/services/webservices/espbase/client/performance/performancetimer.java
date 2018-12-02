package com.sap.engine.services.webservices.espbase.client.performance;

import com.sap.engine.services.webservices.espbase.client.api.PerformanceInfoInterface;

public class PerformanceTimer {
  
  private long startTime;
  private long endTime;
  
  public PerformanceTimer() {
    reset();
  }
  
  void reset() {
    startTime = PerformanceInfoInterface.NOT_MEASURED;
    endTime = PerformanceInfoInterface.NOT_MEASURED;
  }
  
  void notifyStartTime() {
    this.startTime = System.nanoTime();
  }
  
  void notifyEndTime() {
    this.endTime = System.nanoTime(); 
  }
  
  long getTime() {
    return((startTime == PerformanceInfoInterface.NOT_MEASURED || endTime == PerformanceInfoInterface.NOT_MEASURED) ? PerformanceInfoInterface.NOT_MEASURED : endTime - startTime);
  }
}
