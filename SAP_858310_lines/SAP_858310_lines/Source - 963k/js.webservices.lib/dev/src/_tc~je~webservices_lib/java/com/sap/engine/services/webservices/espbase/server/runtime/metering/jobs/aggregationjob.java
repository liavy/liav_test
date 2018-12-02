package com.sap.engine.services.webservices.espbase.server.runtime.metering.jobs;

import com.sap.engine.frame.core.thread.ThreadSystem;
import com.sap.engine.services.webservices.espbase.server.runtime.metering.ServiceMeter;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class AggregationJob implements Runnable {

  private ThreadSystem threadSystem;
  private boolean running = false;
  private ServiceMeter meter;
  private static final Location LOC = Location.getLocation(AggregationJob.class);
  
  public AggregationJob(ServiceMeter sm, ThreadSystem ts) {
    this.meter = sm;
    this.threadSystem = ts;
    
    if (sm == null) {
      throw new IllegalArgumentException("ServiceMeter is null");
    }
    if (ts == null) {
      throw new IllegalArgumentException("ThreadSystem is null");
    }
  }
  
  public synchronized boolean start(){
    LOC.pathT("[start] Starting aggregation job");
    if (running){
      LOC.pathT("[start] Job already running, aborted");
      return false;
    }
    running = true;
    threadSystem.startThread(this, true, true);
    LOC.pathT("[start] Thread started");
    return true;
  }
  
  public void run() {
    try{      
      LOC.pathT("[run] Invoking doAggregateServiceCalls");
      meter.doAggregateServiceCalls();
     } catch(Exception e) {
       LOC.traceThrowableT(Severity.WARNING, "[run] Error executing metering data aggregation", e);
     } finally {
      end();
    }
  }
  
  private synchronized void end(){
    LOC.pathT("[end] Aggregation job completed");
    running = false;
  }
}
