package com.sap.engine.services.webservices.additions.client.metering;

import java.sql.Timestamp;
import java.util.GregorianCalendar;

import com.sap.engine.services.timeout.TimeoutListener;
import com.sap.engine.services.timeout.TimeoutManager;
import com.sap.engine.services.webservices.espbase.server.runtime.metering.ServiceMeter;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class MeteringTimeoutListener implements TimeoutListener{
  private final ServiceMeter serviceMeter;
  private final TimeoutManager timeManager;
  private static final Location LOC = Location.getLocation(MeteringTimeoutListener.class);

  public MeteringTimeoutListener(ServiceMeter sm, TimeoutManager timeMan){
    serviceMeter = sm;
    if (timeMan == null){
      throw new IllegalArgumentException("[MeteringTimeoutClient] TimeManager is null");
    }
    timeManager = timeMan;
  }

  public void timeout() {
    try {
      LOC.pathT("[TimeoutClient.timeout] Received timeout event for deletion of old metering records");
      Timestamp olderThan = ServiceMeter.getBeforeTimestamp(ServiceMeter.OLD_RECORDS_DELETION_TRESHOLD, GregorianCalendar.MONTH); 
      serviceMeter.triggerOldRecordDeletion(olderThan);
    } catch (Exception e) {
      LOC.traceThrowableT(Severity.WARNING, "Error deleting old metering records.", e);
    }
  }

  public void register(){
    timeManager.unregisterTimeoutListener(this);
    timeManager.registerTimeoutListener(this, true, ServiceMeter.OLD_RECORDS_DELETION_SCHEDULE, 
        ServiceMeter.OLD_RECORDS_DELETION_SCHEDULE);
  }

  public void unregister(){
    timeManager.unregisterTimeoutListener(this);
  }

  public boolean check() {
    return true;
  }
}
