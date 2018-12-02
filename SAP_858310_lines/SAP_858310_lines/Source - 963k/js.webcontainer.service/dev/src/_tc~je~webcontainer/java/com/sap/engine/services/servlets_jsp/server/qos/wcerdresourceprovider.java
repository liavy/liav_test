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

public class WCERDResourceProvider implements ResourceProvider {
  private ResourceManager rcm = new ResourceManagerImpl();
	private ThreadResource threadResource = null;
  private RDUsageMonitor wceMonitor = null;
  private RDConstraint constraint;  
  
  public WCERDResourceProvider() {
		super();
		
		//TODO max per consumer
		int maxPerConsumer = ServiceContext.getServiceContext().getWebContainerProperties().getRDThreadCountFactor() * 
      ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getFCAServerThreadCount();
		threadResource  = new ThreadResource("HTTP Worker", 0);
		rcm.registerResource(this);
		ResourceContext resCxt = rcm.createResourceContext(threadResource.getName(), RequestDispatcherConsumer.WCE_REQUEST_DISPATCHER_CONSUMER);
		       
		threadResource.setTotalQuantity(maxPerConsumer); 
		wceMonitor  = new RDUsageMonitor();
		resCxt.addNotification(wceMonitor);  
		constraint = new RDConstraint(wceMonitor);
		resCxt.addConstraint(constraint);
	}

	@Override
	public Constraint getDefaultConstrait() {
	  return constraint;
	}

	@Override
	public Notification getDefaultNotification() {
	  return wceMonitor;
	}

	@Override
	public Resource getResource() {		
		return threadResource;
	}

  
  public boolean consume(RequestDispatcherConsumer consumer) {      
    if (LogContext.getLocationQoS().beDebug()) {
      LogContext.getLocationQoS().logT(Severity.DEBUG, "WCE RD is consuming by [" + consumer.getId() + "]");
    }
    return rcm.consume(consumer, threadResource.getName(), 1);   
  }
  
  public void release(RequestDispatcherConsumer consumer) {       
    if (LogContext.getLocationQoS().beDebug()) {
      LogContext.getLocationQoS().logT(Severity.DEBUG, "WCE RD is released by [" + consumer.getId() + "]");
    }
    rcm.release(consumer, threadResource.getName(), 1);
  }  
  
  public RDUsageMonitor getMonitor() {
    return wceMonitor;
  }
}
