package com.sap.engine.system;

import java.util.concurrent.atomic.AtomicInteger;

public class ThreadWrapperExt extends ThreadWrapper {
	
	 /**
   * Clears all the thread related data - ThreadContext, monitoring info, context classloader, thread annotations etc.
   * To be used only if multiple requests are processed in one and the same engine managed thread in order to clean the
   * thread before next request execution.
   *  
   * @deprecated should be used only internally and with caution. 
   */
	public static void clearManagedThreadRelatedData () {
		Thread currentThread = Thread.currentThread();  
		if (currentThread instanceof MonitoredThread) { // only monitorable threads can be processed
			MonitoredThread monitorable = (MonitoredThread) currentThread;
			monitorable.clearManagedThreadRelatedData();
		} 		
	}
	
	 /**
   * Sets the current user operation ID in the Thread
   * Used internally by ThreadManager when inheriting parent thread data to set it in child thread.
   * @deprecated should be used only internally by the ThreadManager and DSR service
   */
  public static void setTransactionId(String id) {
  	if (threadMonitoringEnabled) {
	  	Thread currentThread = Thread.currentThread();  // only current thread can be processed, no
	                                                    // synchronization is needed at java side
			if (currentThread instanceof MonitoredThread) { // only monitorable threads can be processed
			  MonitoredThread monitorable = (MonitoredThread) currentThread;
			  monitorable.setTransactionId(id);
			} else if (monitorThreadBuilder != null) {
	    	monitorThreadBuilder.setTransactionId(id);
	    }
  	}
  }
  
  /**
   * Sets the current task ID in the Thread
   * Used internally by infrastructure when inheriting parent thread data to set it in child thread.
   * @deprecated should be used only internally by the engine kernel
   */
  public static void setCurrentTaskId(long id, AtomicInteger counter) {
  	if (threadMonitoringEnabled) {
	  	Thread currentThread = Thread.currentThread();  // only current thread can be processed, no
	                                                    // synchronization is needed at java side
			if (currentThread instanceof MonitoredThread) { // only monitorable threads can be processed
			  MonitoredThread monitorable = (MonitoredThread) currentThread;
			  monitorable.setCurrentTaskId(id, counter);
			} else if (monitorThreadBuilder != null) {
	    	monitorThreadBuilder.setCurrentTaskId(id, counter);
	    }	
  	}
  }
  
  /**
   * Gets the counter instance for task IDs generation from the current thread. 
   * 
   * @return the counter instance for task IDs generation
   * @deprecated should be used only internally by the engine kernel
   */
  public static AtomicInteger getCurrentTaskCounter() {
  	if (threadMonitoringEnabled) {
	  	Thread currentThread = Thread.currentThread();  // only current thread can be processed, no
	    // synchronization is needed at java side
			if (currentThread instanceof MonitoredThread) { // only monitorable threads can be processed
				MonitoredThread monitorable = (MonitoredThread) currentThread;
				return monitorable.getCurrentTaskCounter();
			} else if (monitorThreadBuilder != null) {
				return monitorThreadBuilder.getCurrentTaskCounter();
			}
  	}
		return null;
  }
  
  
}
