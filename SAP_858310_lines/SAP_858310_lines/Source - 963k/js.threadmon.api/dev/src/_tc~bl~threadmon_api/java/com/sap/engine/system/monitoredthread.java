package com.sap.engine.system;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Petio Petev, Elitsa Pancheva
 */
public interface MonitoredThread {

	/**
	 * @deprecated should be used only internally by the ThreadManager
	 */
  public void setSubtaskName(String name);
  public String getSubtaskName();
  public void setUser(String user);
  public void setApplicationName(String appName);
	public void setSessionID(String sessionID);
	public void setRequestID(String requestID);
  /**
   * @deprecated should be used only internally by the ThreadManager
   */
  public void setTaskName(String name);
  public String getTaskName();
	public int getThreadState();
	public void setThreadState(int state);

	public void pushSubtask(String name, int state);
	public void popSubtask();
	public void pushTask(String name, int state);
	public void popTask();
  
  /**
   * Gets the ID of the subtask that the current thread is executing at the moment. 
   * 
   * @return the most top task ID
   */
  public long getCurrentSubtaskId();
  
  /**
   * Gets the ID of the task that the current thread is executing at the moment. 
   * 
   * @return the most top task ID
   */
  public long getCurrentTaskId();
  
  /**
   * Gets the counter instance for task IDs generation from the current thread. 
   * 
   * @return the counter instance for task IDs generation
   */
  public AtomicInteger getCurrentTaskCounter();
  
  /**
   * Sets the current task ID in the Thread
   * Used internally by ThreadManager when inheriting parent thread data to set it in child thread.
   * @deprecated should be used only internally by the ThreadManager
   */
  public void setCurrentTaskId(long id);
  
  /**
   * Sets the current task ID in the Thread
   * Used internally by ThreadManager when inheriting parent thread data to set it in child thread.
   * @deprecated should be used only internally by the ThreadManager
   */
  public void setCurrentTaskId(long id, AtomicInteger counter);
  
  /**
   * Gets the ID of the current user operation. 
   * 
   * @return String representation of the current
   */
  public String getTransactionId();
    
  /**
   * Sets the current user operation ID in the Thread
   * Used internally by ThreadManager when inheriting parent thread data to set it in child thread.
   * @deprecated should be used only internally by the ThreadManager and DSR service
   */
  public void setTransactionId(String id);
    
  /**
   * Clears all the thread related data - ThreadContext, monitoring info, context classloader, thread annotations etc.
   * To be used only if multiple requests are processed in one and the same engine managed thread in order to clean the
   * thread before next request execution.
   *  
   * @deprecated should be used only internally and with caution. 
   */
  public void clearManagedThreadRelatedData ();
}