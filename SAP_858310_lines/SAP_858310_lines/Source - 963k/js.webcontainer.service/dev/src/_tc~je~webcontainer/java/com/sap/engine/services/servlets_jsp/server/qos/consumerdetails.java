package com.sap.engine.services.servlets_jsp.server.qos;

/**
 * Data holder for monitoring info
 * 
 * @author Violeta Uzunova(I024174)
 */
public class ConsumerDetails {  
  private String consumerName;
  private long startCurrentUnavailable;
  private long totalUnavailablePeriod;
  private long totalUsage;
  private long currentUsage;
  private long unavailableResponse;
  
  
  public ConsumerDetails(String consumerName) {
    this.consumerName = consumerName;
    this.startCurrentUnavailable = -1L;
    this.totalUnavailablePeriod = -1L;
    this.totalUsage = 0;
    this.currentUsage = 0;
    unavailableResponse = 0;
  }
  
  /**
   * Returns the resource name
   * @return
   */
  public String getConsumerName() {
    return consumerName;
  }  
  
  /**
   * An event that the resource is set in unavailable state
   */
  void setUnavailable() {   
    if (startCurrentUnavailable == -1) {
      startCurrentUnavailable = System.currentTimeMillis();     
    // } else { // ignore it; we are currently in unavailalbe status      
    }    
  }  
  
  /**
   * An event that notifies that the resource is set to available state
   */
  void setAvailable() {
    if (startCurrentUnavailable != -1) {
      totalUnavailablePeriod += (System.currentTimeMillis() - startCurrentUnavailable);
      startCurrentUnavailable = -1L;
    } // } else { ignore it; currently in available status
  } 
  
  /**
   * Returns the current unavailable period. The resource is defined as 
   * unavailable from the time when the last possible thread starts processing
   * it until on of the threads finishes processing the request
   * 
   * @return the current unavailable period in milliseconds
   */

  public long getCurrentUnavailablePeriod() {
    if (startCurrentUnavailable != -1) {
      return System.currentTimeMillis() - startCurrentUnavailable;
    } 
    return -1L;
  }
  
  /**
   * Returns the total unavailable period for the resource
   *  
   * @return
   */
  public long getTotalUnavailablePeriod() {
    return totalUnavailablePeriod + ((getCurrentUnavailablePeriod() == -1L) ? 0 : getCurrentUnavailablePeriod());
  }

  /**
   * The number of threads which were processing the requests to this resource 
   * 
   * @return
   */
  public long getTotalUsage() {
    return totalUsage;
  }

  /**
   * the number of 503 responses
   * 
   * @return
   */
  public long getUnavailableResponses() {
    return unavailableResponse; 
  }  
 
  void incNumberOfUnavailable() {
    unavailableResponse++;
  }
  
  void updateTotalUsage(long deltaUsage) {
    totalUsage += deltaUsage;
  }

  public long getCurrentUsage() {
    return currentUsage;
  }

  void setCurrentUsage(long currentUsage) {
    this.currentUsage = currentUsage;
  } 
}

