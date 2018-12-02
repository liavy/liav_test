package com.sap.engine.services.webservices.espbase.server.runtime.metering.jobs;

import java.sql.SQLException;
import java.sql.Timestamp;

import com.sap.engine.frame.core.thread.ThreadSystem;
import com.sap.engine.services.webservices.espbase.server.runtime.metering.ServiceMeter;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class DeletionJob implements Runnable {

  private ThreadSystem threadSystem;
  private boolean running = false;
  Timestamp olderThan;
  private ServiceMeter meter;
  private static final Location LOC = Location.getLocation(DeletionJob.class);
  
  public DeletionJob(ServiceMeter sm, ThreadSystem ts) {
    this.meter = sm;
    this.threadSystem = ts;
  }
  
  public synchronized boolean start(Timestamp olderThan){
    LOC.pathT("[start] Starting deletion job");
    if (running){
      LOC.pathT("[start] Job already running on this node, aborted");
      return false;
    }
    if (! mustTriggerDeletionJob()){
      LOC.pathT("[start] Deletion job already performed");
      return false;
    }
    running = true;
    this.olderThan = olderThan;
    threadSystem.startThread(this, true, true);
    LOC.pathT("[start] Deletion job thread started");
    return true;
  }
  private boolean mustTriggerDeletionJob(){
    boolean must = false;
    try{
      must = meter.mustTriggerDeletionJob(); 
    }catch (SQLException e) {
      LOC.traceThrowableT(Severity.WARNING, "[mustTriggerDeletionJob] Error while checking deletion job status; deletion job will not start", e);
      must = false;
    }
    return must;
  }
  
  public void run() {
    try{
      if (meter == null){
        return;
      }
      LOC.pathT("[run] Invoking doDeleteMeteringRecords");
      meter.doDeleteMeteringRecords(olderThan);
     }catch(Exception e){
       LOC.traceThrowableT(Severity.WARNING, "[run] Error while executing deletion job", e);
     }finally{
      end();
    }
  }
  
  private synchronized void end(){
    LOC.pathT("[end] Deletion job completed");
    running = false;
  }

}
