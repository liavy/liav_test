package com.sap.engine.services.servlets_jsp.server.qos;

import java.util.Enumeration;
import java.util.Hashtable;

import com.sap.engine.lib.rcm.Notification;
import com.sap.engine.lib.rcm.ResourceConsumer;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;

public class RDUsageMonitor implements Notification {

  private Hashtable<String, ConsumerDetails> unavailableDetails;
  
  public RDUsageMonitor() {
    super();
  }
  
  /**
   * @see com.sap.engine.lib.rcm.Notification#update(ResourceConsumer, long, long)
   */
  public void update(ResourceConsumer consumer, long previousUsage, long currentUsage) {
    if (unavailableDetails == null) {
      unavailableDetails = new Hashtable<String, ConsumerDetails>();
    }
    ConsumerDetails details = unavailableDetails.get(consumer.getId());
    if (details == null) {
      details = new ConsumerDetails(consumer.getId());
    }
    details.setCurrentUsage(currentUsage);
    if (currentUsage - previousUsage > 0) {
      details.updateTotalUsage(currentUsage - previousUsage);      
    }
    //RDThreadCount is online modifiable
    int maxPerConsumer = ServiceContext.getServiceContext().getWebContainerProperties().getRDThreadCountFactor() *
      ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getFCAServerThreadCount();  
    if (currentUsage < maxPerConsumer) {      
      details.setAvailable();         
    }        
    unavailableDetails.put(consumer.getId(), details);    
  }
   
  /**
   * 
   * @param resourceName
   */
  void setUnavailable(String resourceName) { // keep package modifier to restrict the access to the method    
    ConsumerDetails details = unavailableDetails.get(resourceName);
    if (details == null) {
      details = new ConsumerDetails(resourceName);
    }
    details.setUnavailable();
    unavailableDetails.put(resourceName, details);
  }

  void incNumberOfUnavailable(String resourceName) {
    ConsumerDetails details = unavailableDetails.get(resourceName);
    if (details == null) {
      details = new ConsumerDetails(resourceName);
    }
    details.incNumberOfUnavailable();
    unavailableDetails.put(resourceName, details);    
  }
  
  
  /**
   * Returns the current unavailable period. The resource is defined as 
   * unavailable from the time when the last possible thread starts processing
   * it until on of the threads finishes processing the request
   *    
   * @param resourceName resource
   * 
   * @return the current unavailable period in milliseconds
   */
  public long getCurrentUnavailablePeriod(String resourceName) {
    ConsumerDetails details = unavailableDetails.get(resourceName);
    if (details == null) {
      return -1L; // not known as unavailable; do not put a logic for it here
    }
    return details.getCurrentUnavailablePeriod();
  }

  /**
   * Returns the total unavailable period for the resource
   *  
   * @param resourceName resource
   * @return
   */
  public long getTotalUnavailablePeriod(String resourceName) {
    ConsumerDetails details = unavailableDetails.get(resourceName);
    if (details == null) {
      return -1L; // not known as unavailable; do not put a logic for it here
    }
    return details.getTotalUnavailablePeriod();
  }
  
  /**
   * The number of threads which were processing the requests to this resource 
   * 
   * @param resourceName resource
   * @return
   */
  public long getTotalUsage(String resourceName) {
    ConsumerDetails details = unavailableDetails.get(resourceName);
    if (details == null) {
      return 0;
    }
    
    return details.getTotalUsage();
  }
  
  /**
   * 
   * @param resourceName
   * @return
   */
  public long getCurrentUsage(String resourceName) {
    ConsumerDetails details = unavailableDetails.get(resourceName);
    if (details == null) {
      return 0;
    }
    
    return details.getCurrentUsage();
  }         
  
  /**
   * The number of 503 response 
   * @param resourceName
   * @return
   */ 
  public long getUnavailableResonces(String resourceName) {
    ConsumerDetails details = unavailableDetails.get(resourceName);
    if (details == null) {
      return 0;
    }
    
    return details.getUnavailableResponses();
  }
  
  /**
   * Returns the names for all resources
   * @return
   */
  public String[] getAllResourceNames() {  
    if (unavailableDetails == null || unavailableDetails.isEmpty()) {
      return null;
    }
    String result[] = new String[unavailableDetails.size()];
    Enumeration<String> keys = unavailableDetails.keys();    
    int i = 0;
    while (keys.hasMoreElements()) {
      result[i++] = keys.nextElement();
    }
    return result;      
  }
}
