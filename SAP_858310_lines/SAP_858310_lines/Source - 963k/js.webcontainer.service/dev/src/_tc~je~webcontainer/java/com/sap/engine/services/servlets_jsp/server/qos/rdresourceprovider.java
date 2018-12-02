package com.sap.engine.services.servlets_jsp.server.qos;

import com.sap.engine.lib.rcm.Constraint;
import com.sap.engine.lib.rcm.Notification;
import com.sap.engine.lib.rcm.Resource;
import com.sap.engine.lib.rcm.ResourceContext;
import com.sap.engine.lib.rcm.ResourceManager;
import com.sap.engine.lib.rcm.ResourceProvider;
import com.sap.engine.lib.rcm.impl.ResourceManagerImpl;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.tc.logging.Severity;

public class RDResourceProvider implements ResourceProvider {
  private ResourceManager rcm = new ResourceManagerImpl();
  private ThreadResource threadResource;
  private RDUsageMonitor monitor;
  private RDConstraint constraint;
  
  public RDResourceProvider() {
    super();    
    int maxPerConsumer = ServiceContext.getServiceContext().getWebContainerProperties().getRDThreadCountFactor() * 
    	ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getFCAServerThreadCount();
    threadResource = new ThreadResource("HTTP Worker", 0);
    rcm.registerResource(this);
    ResourceContext resCxt = rcm.createResourceContext(threadResource.getName(), RequestDispatcherConsumer.REQUEST_DISPATCHER_CONSUMER);
         
    threadResource.setTotalQuantity(maxPerConsumer); 
    monitor = new RDUsageMonitor();
    resCxt.addNotification(monitor);      
    constraint = new RDConstraint(monitor);
    resCxt.addConstraint(constraint);
  }
  
  public Constraint getDefaultConstrait() {
    return constraint;
  }

  public Notification getDefaultNotification() {    
    return monitor;
  }

  public Resource getResource() {
    return threadResource;
  }
  
  public boolean consume(RequestDispatcherConsumer consumer) {  
    if (LogContext.getLocationQoS().beDebug()) {
      LogContext.getLocationQoS().logT(Severity.DEBUG, "The RD is consuming by [" + consumer.getId() + "]");
    }
  
    return rcm.consume(consumer, threadResource.getName(), 1);
  }
  
  public void release(RequestDispatcherConsumer consumer) {
    if (LogContext.getLocationQoS().beDebug()) {
      LogContext.getLocationQoS().logT(Severity.DEBUG, "The RD is released by [" + consumer.getId() + "]");
    }
    rcm.release(consumer, threadResource.getName(), 1);
  }
  
  
  public String getConsumerType(String aliasName, String dispatchURL) {
    if (ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().isConsumerTypeIsAlias()) {
      return aliasName;
    } else {
      return aliasName + dispatchURL;
    }
  }
  
  public RDUsageMonitor getMonitor() {
    return monitor;
  }
}
