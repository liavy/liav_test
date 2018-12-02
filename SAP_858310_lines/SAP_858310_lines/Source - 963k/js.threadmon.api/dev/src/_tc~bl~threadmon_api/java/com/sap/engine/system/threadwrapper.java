package com.sap.engine.system;

/**
 * @author Petio Petev, Elitsa Pancheva
 */
public class ThreadWrapper {
  
  public static final int TS_IDLE = com.sap.bc.proj.jstartup.sadm.ShmThread.TS_IDLE;   
  public static final int TS_NONE = com.sap.bc.proj.jstartup.sadm.ShmThread.TS_NONE;   
  public static final int TS_PROCESSING = com.sap.bc.proj.jstartup.sadm.ShmThread.TS_PROCESSING;
  public static final int TS_WAITING_FOR_TASK = com.sap.bc.proj.jstartup.sadm.ShmThread.TS_WAITING_FOR_TASK;
  public static final int TS_WAITING_ON_IO = com.sap.bc.proj.jstartup.sadm.ShmThread.TS_WAITING_ON_IO;
  public static final long ID_NOT_AVAILABLE = -1;
  public static final String TrID_NOT_AVAILABLE = null;
  
  protected static boolean threadMonitoringEnabled = true;
  
  protected static MonitoredThread monitorThreadBuilder = null; 
  
  public static synchronized void setMonitorThreadBuilder(MonitoredThread builder, boolean isMonitoringEnabled) throws Exception {
  	if (monitorThreadBuilder != null) { 
  	  throw new Exception("MonitorThreadFactory is already set");
  	}
  	monitorThreadBuilder = builder;
  	threadMonitoringEnabled = isMonitoringEnabled;
  }

  public static void setSubTaskName(String name) {
  	if (threadMonitoringEnabled) {
	    Thread currentThread = Thread.currentThread();  // only current thread can be processed, no
	                                                    // synchronization is needed at java side
	    if (currentThread instanceof MonitoredThread) { // only monitorable threads can be processed
	      MonitoredThread monitorable = (MonitoredThread) currentThread;
	      monitorable.setSubtaskName(name);
	    } else if (monitorThreadBuilder != null) {
	    	monitorThreadBuilder.setSubtaskName(name);
	    }
  	}
  }

  public static String getSubTaskName() {
    String name = null;
    if (threadMonitoringEnabled) {
	    Thread currentThread = Thread.currentThread();  // only current thread can be processed, no
	                                                    // synchronization is needed at java side
	    if (currentThread instanceof MonitoredThread) { // only monitorable threads can be processed
	      MonitoredThread monitorable = (MonitoredThread) currentThread;
	      name = monitorable.getSubtaskName();
	    } else if (monitorThreadBuilder != null) {
	    	name = monitorThreadBuilder.getSubtaskName();
	    }
    }
    return name;
  }

  public static void setUser(String user) {
  	if (threadMonitoringEnabled) {
	    Thread currentThread = Thread.currentThread();  // only current thread can be processed, no
	                                                    // synchronization is needed at java side
	    if (currentThread instanceof MonitoredThread) { // only monitorable threads can be processed
	      MonitoredThread monitorable = (MonitoredThread) currentThread;
	      monitorable.setUser(user);
	    } else if (monitorThreadBuilder != null) {
	    	monitorThreadBuilder.setUser(user);
	    }
  	}
  }
  
  public static void setApplicationName(String appName) {
  	if (threadMonitoringEnabled) {
	  	Thread currentThread = Thread.currentThread();  // only current thread can be processed, no
	    // synchronization is needed at java side
			if (currentThread instanceof MonitoredThread) { // only monitorable threads can be processed
				MonitoredThread monitorable = (MonitoredThread) currentThread;
				monitorable.setApplicationName(appName);
			} else if (monitorThreadBuilder != null) {
			  monitorThreadBuilder.setApplicationName(appName);
			}
  	}
  }
  
  public static void setSessionID(String sessionID) {
  	if (threadMonitoringEnabled) {
	  	Thread currentThread = Thread.currentThread();  // only current thread can be processed, no
	    // synchronization is needed at java side
			if (currentThread instanceof MonitoredThread) { // only monitorable threads can be processed
				MonitoredThread monitorable = (MonitoredThread) currentThread;
				monitorable.setSessionID(sessionID);
			} else if (monitorThreadBuilder != null) {
			  monitorThreadBuilder.setSessionID(sessionID);
			}
  	}
  }
  
  public static void setRequestID(String requestID) {
  	if (threadMonitoringEnabled) {
	  	Thread currentThread = Thread.currentThread();  // only current thread can be processed, no
	    // synchronization is needed at java side
			if (currentThread instanceof MonitoredThread) { // only monitorable threads can be processed
				MonitoredThread monitorable = (MonitoredThread) currentThread;
				monitorable.setRequestID(requestID);
			} else if (monitorThreadBuilder != null) {
			  monitorThreadBuilder.setRequestID(requestID);
			}
  	}
  }

  public static String getTaskName() {
  	String name = null;
  	
  	if (threadMonitoringEnabled) {
	  	Thread currentThread = Thread.currentThread();  // only current thread can be processed, no
	                                                    // synchronization is needed at java side
	    if (currentThread instanceof MonitoredThread) { // only monitorable threads can be processed
	      MonitoredThread monitorable = (MonitoredThread) currentThread;
	      name = monitorable.getTaskName();
	    } else if (monitorThreadBuilder != null) {
	    	name = monitorThreadBuilder.getTaskName();
	    }
  	}
    return name;
  }

  public static void setTaskName(String name) {
  	if (threadMonitoringEnabled) {
	    Thread currentThread = Thread.currentThread();  // only current thread can be processed, no
	                                                    // synchronization is needed at java side
	    if (currentThread instanceof MonitoredThread) { // only monitorable threads can be processed
	      MonitoredThread monitorable = (MonitoredThread) currentThread;
	      monitorable.setTaskName(name);
	    } else if (monitorThreadBuilder != null) {
	    	monitorThreadBuilder.setTaskName(name);
	    }
  	}
  }
  
  public static int getState() {
    int result = TS_NONE;
    if (threadMonitoringEnabled) {
	    Thread currentThread = Thread.currentThread();  // only current thread can be processed, no
	                                                    // synchronization is needed at java side
	    if (currentThread instanceof MonitoredThread) { // only monitorable threads can be processed
	      MonitoredThread monitorable = (MonitoredThread) currentThread;
	      result = monitorable.getThreadState();
	    } else if (monitorThreadBuilder != null) {
	    	result = monitorThreadBuilder.getThreadState();
	    }
    }
    return result;
  }

  public static void setState(int state) {
  	if (threadMonitoringEnabled) {
	    Thread currentThread = Thread.currentThread();  // only current thread can be processed, no
	                                                    // synchronization is needed at java side
	    if (currentThread instanceof MonitoredThread) { // only monitorable threads can be processed
	      MonitoredThread monitorable = (MonitoredThread) currentThread;
	      monitorable.setThreadState(state);
	    } else if (monitorThreadBuilder != null) {
	    	monitorThreadBuilder.setThreadState(state);
	    }
  	}
  }
  
  public static void pushSubtask(String name, int state) {
  	if (threadMonitoringEnabled) {
	    Thread currentThread = Thread.currentThread();  // only current thread can be processed, no
	                                                    // synchronization is needed at java side
	    if (currentThread instanceof MonitoredThread) { // only monitorable threads can be processed
	      MonitoredThread monitorable = (MonitoredThread) currentThread;
	      monitorable.pushSubtask(name, state);
	    } else if (monitorThreadBuilder != null) {
	    	monitorThreadBuilder.pushSubtask(name, state);
	    }
  	}
  }
  
  public static void popSubtask() {
  	if (threadMonitoringEnabled) {
	    Thread currentThread = Thread.currentThread();  // only current thread can be processed, no
	                                                    // synchronization is needed at java side
	    if (currentThread instanceof MonitoredThread) { // only monitorable threads can be processed
	      MonitoredThread monitorable = (MonitoredThread) currentThread;
	      monitorable.popSubtask();
	    } else if (monitorThreadBuilder != null) {
	    	monitorThreadBuilder.popSubtask();
	    }
  	}
  }
  
  public static void pushTask(String name, int state) {
  	if (threadMonitoringEnabled) {
	    Thread currentThread = Thread.currentThread();  // only current thread can be processed, no
	                                                    // synchronization is needed at java side
	    if (currentThread instanceof MonitoredThread) { // only monitorable threads can be processed
	      MonitoredThread monitorable = (MonitoredThread) currentThread;
	      monitorable.pushTask(name, state);
	    } else if (monitorThreadBuilder != null) {
	    	monitorThreadBuilder.pushTask(name, state);
	    }
  	}
  }
  
  public static void popTask() {
  	if (threadMonitoringEnabled) {
	    Thread currentThread = Thread.currentThread();  // only current thread can be processed, no
	                                                    // synchronization is needed at java side
	    if (currentThread instanceof MonitoredThread) { // only monitorable threads can be processed
	      MonitoredThread monitorable = (MonitoredThread) currentThread;
	      monitorable.popTask();
	    } else if (monitorThreadBuilder != null) {
	    	monitorThreadBuilder.popTask();
	    }
  	}
  }
  
  /**
   * Gets the ID of the subtask that the current thread is executing at the moment. 
   * 
   * @return the most top task ID
   */
  public static long getCurrentSubtaskId() {
  	long result = ID_NOT_AVAILABLE;
  	if (threadMonitoringEnabled) {
	  	Thread currentThread = Thread.currentThread();  // only current thread can be processed, no
	                                                        // synchronization is needed at java side
	    if (currentThread instanceof MonitoredThread) { // only monitorable threads can be processed
	      MonitoredThread monitorable = (MonitoredThread) currentThread;
	      result = monitorable.getCurrentSubtaskId();
	    } else if (monitorThreadBuilder != null) {
	    	result = monitorThreadBuilder.getCurrentSubtaskId();
	    }
  	}
    return result;
  }

  /**
   * Gets the ID of the task that the current thread is executing at the moment. 
   * 
   * @return the most top task ID
   */
  public static long getCurrentTaskId() {
    long result = ID_NOT_AVAILABLE;
    if (threadMonitoringEnabled) {
	    Thread currentThread = Thread.currentThread();  // only current thread can be processed, no
	                                                        // synchronization is needed at java side
	    if (currentThread instanceof MonitoredThread) { // only monitorable threads can be processed
	      MonitoredThread monitorable = (MonitoredThread) currentThread;
	      result = monitorable.getCurrentTaskId();
	    } else if (monitorThreadBuilder != null) {
	    	result = monitorThreadBuilder.getCurrentTaskId();
	    }
    }
    return result;
  }
  
  /**
   * Sets the current task ID in the Thread
   * Used internally by infrastructure when inheriting parent thread data to set it in child thread.
   * @deprecated should be used only internally by the engine kernel
   */
  public static void setCurrentTaskId(long id) {
  	if (threadMonitoringEnabled) {
	  	Thread currentThread = Thread.currentThread();  // only current thread can be processed, no
	                                                    // synchronization is needed at java side
			if (currentThread instanceof MonitoredThread) { // only monitorable threads can be processed
			  MonitoredThread monitorable = (MonitoredThread) currentThread;
			  monitorable.setCurrentTaskId(id);
			} else if (monitorThreadBuilder != null) {
	    	monitorThreadBuilder.setCurrentTaskId(id);
	    }
  	}
  }
  
  /**
   * Gets the ID of the current user operation. 
   * 
   * @return String representation of GUID identifying the current user operation. 
   */
  public static String getTransactionId() {
    String result = TrID_NOT_AVAILABLE;
    if (threadMonitoringEnabled) {
	    Thread currentThread = Thread.currentThread();  // only current thread can be processed, no
	                                                        // synchronization is needed at java side
	    if (currentThread instanceof MonitoredThread) { // only monitorable threads can be processed
	      MonitoredThread monitorable = (MonitoredThread) currentThread;
	      result = monitorable.getTransactionId();
	    } else if (monitorThreadBuilder != null) {
	    	result = monitorThreadBuilder.getTransactionId();
	    }
    }
    return result;
  }
  
  /**
   * Checks if the thread monitoring is enabled. 
   * Thread monitoring can be disabled with property of the thread manager
   * 
   * @return true if the thread monitoring is enabled, false otherwise. 
   */
  public static boolean isthreadMonitoringEnabled() {
    return threadMonitoringEnabled;
  }

}