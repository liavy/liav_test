package com.sap.engine.services.webservices.espbase.client.performance;

public class PerformanceInfo {
  
  public static int REQUEST_PREPARE_INFO_TYPE = 0;
  public static int RESPONSE_PROCESS_INFO_TYPE = 1;
  public static int BACKEND_RESPONSE_INFO_TYPE = 2;
  
  private PerformanceTimer[] performanceTimers;
  
  public PerformanceInfo() {
    performanceTimers = new PerformanceTimer[]{new PerformanceTimer(), new PerformanceTimer(), new PerformanceTimer()};
  }
  
  public void reset() {
    for(int i = 0; i < performanceTimers.length; i++) {
      performanceTimers[i].reset();
    }
  }
  
  public void notifyStartTime(int performanceInfoType) {
    checkPerformanceInfoType(performanceInfoType);
    performanceTimers[performanceInfoType].notifyStartTime();
  }
  
  public void notifyEndTime(int performanceInfoType) {
    checkPerformanceInfoType(performanceInfoType);
    performanceTimers[performanceInfoType].notifyEndTime();
  }
  
  public long getTime(int performanceInfoType) {
    checkPerformanceInfoType(performanceInfoType);
    return(performanceTimers[performanceInfoType].getTime());
  }
  
  private void checkPerformanceInfoType(int performanceInfoType) {
    if(performanceInfoType < 0 || performanceInfoType >= performanceTimers.length) {
      throw new IllegalArgumentException("Unrecognized performance info type : " + performanceInfoType);
    }
  }
}
