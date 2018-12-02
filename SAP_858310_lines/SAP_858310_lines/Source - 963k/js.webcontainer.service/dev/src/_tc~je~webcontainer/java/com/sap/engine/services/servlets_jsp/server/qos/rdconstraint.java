package com.sap.engine.services.servlets_jsp.server.qos;

import com.sap.engine.lib.rcm.Constraint;
import com.sap.engine.lib.rcm.ResourceConsumer;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.tc.logging.Severity;

public class RDConstraint implements Constraint {
  private RDUsageMonitor monitor; 
  
  public RDConstraint(RDUsageMonitor monitor) {    
    this.monitor = monitor;    
  }
  
  public boolean preConsume(ResourceConsumer consumer, long currentUsage, long proposedUsage) {
    if (!ServiceContext.getServiceContext().getWebContainerProperties().isEnableQoSRestrictionsForRD()) {
      return true;
    }
    //RDThreadCount is online modifiable
    int maxPerConsumer = ServiceContext.getServiceContext().getWebContainerProperties().getRDThreadCountFactor() *
      ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getFCAServerThreadCount(); 
    if (proposedUsage >= maxPerConsumer) {
      monitor.setUnavailable(consumer.getId());
    }
    boolean result = !(proposedUsage > maxPerConsumer);
    if (!result) {
      monitor.incNumberOfUnavailable(consumer.getId());
    } 
    if (LogContext.getLocationQoS().beDebug()) {
      LogContext.getLocationQoS().logT(Severity.DEBUG, "Condition for consuming by [" + consumer.getId() + "] resource returns [" + result + "]; " +
          		"proposedUsage = [" + proposedUsage + "]; maxPerConsumer = [" + maxPerConsumer + "];");
    }
    return result;   
  }
}
