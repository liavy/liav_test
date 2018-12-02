package com.sap.engine.core.thread;

import java.util.concurrent.atomic.AtomicInteger;

import com.sap.engine.system.MonitoredThread;
import com.sap.engine.system.ThreadWrapper;


/**
 * Implementation of MonitoredThread which is set in ThreadWrapper on startup of ThreadManager.
 * MonitoredThreadBuilder is used to associate MonitoredThread put in ThreadLocal with java.lang.Threads.  
 * 
 * @author I024135
 */
public class MonitoredThreadBuilder implements MonitoredThread {
	
	private MonitoredThread getMonitoredThread() {
	  return ((MonitoringInfo) JavaThreadsCallback.info.get()).getMonitoredThread();
	}
	
	public int getThreadState() {	
		MonitoredThread monthr = getMonitoredThread();
		if (monthr != null) {
		  return monthr.getThreadState();
		} else {
			return ThreadWrapper.TS_NONE;
		}
	}
	
	public String getSubtaskName() { 
		MonitoredThread monthr = getMonitoredThread();
		if (monthr != null) {
		  return monthr.getSubtaskName();
		} else {
			return null;
		}
	}
	
	public String getTaskName() {
		MonitoredThread monthr = getMonitoredThread();
		if (monthr != null) {
		  return monthr.getTaskName();
		} else {
			return null;
		}
	}
	
	public void popSubtask() { 
		MonitoredThread monthr = getMonitoredThread();
		if (monthr != null) {
		  monthr.popSubtask();
		}
	}
	
	public void popTask() {	
		MonitoredThread monthr = getMonitoredThread();
		if (monthr != null) {
		  monthr.popTask();
		}
	}
	
	public void pushSubtask(String name, int state) { 
		MonitoredThread monthr = getMonitoredThread();
		if (monthr != null) {
		  monthr.pushSubtask(name, state);
		}
	}
	
	public void pushTask(String name, int state) { 
		MonitoredThread monthr = getMonitoredThread();
		if (monthr != null) {
		  monthr.pushTask(name, state);
		}
	}
	
	public void setThreadState(int state) { 
		MonitoredThread monthr = getMonitoredThread();
		if (monthr != null) {
		  monthr.setThreadState(state);
		}
	}
	
	public void setSubtaskName(String name) { 
		MonitoredThread monthr = getMonitoredThread();
		if (monthr != null) {
		  monthr.setSubtaskName(name);
		}
	}
	
	public void setTaskName(String name) {
		MonitoredThread monthr = getMonitoredThread();
		if (monthr != null) {
		  monthr.setTaskName(name);
		}
	}
	
	public void setUser(String name) { 
		MonitoredThread monthr = getMonitoredThread();
		if (monthr != null) {
		  monthr.setUser(name);
		}
	}
	
  public long getCurrentSubtaskId() { 
  	MonitoredThread monthr = getMonitoredThread();
		if (monthr != null) {
		  return monthr.getCurrentSubtaskId();
		} else {
			return ThreadWrapper.ID_NOT_AVAILABLE;
		}
  }
  
  public long getCurrentTaskId() { 
  	MonitoredThread monthr = getMonitoredThread();
		if (monthr != null) {
		  return monthr.getCurrentTaskId();
		} else {
			return ThreadWrapper.ID_NOT_AVAILABLE;
		}
  }
  
  public void setCurrentTaskId(long id) { 
  	MonitoredThread monthr = getMonitoredThread();
		if (monthr != null) {
		  monthr.setCurrentTaskId(id);
		}
  }
  
  public void setCurrentTaskId(long id, AtomicInteger counter) { 
  	MonitoredThread monthr = getMonitoredThread();
		if (monthr != null) {
		  monthr.setCurrentTaskId(id, counter); 
		}
  }
  
  public AtomicInteger getCurrentTaskCounter() { 
  	MonitoredThread monthr = getMonitoredThread();
		if (monthr != null) {
			return monthr.getCurrentTaskCounter();  
		} else {
			return null;
		} 	
  }
  
  public String getTransactionId() { 
  	MonitoredThread monthr = getMonitoredThread();
		if (monthr != null) {
		  return monthr.getTransactionId();
		} else {
			return ThreadWrapper.TrID_NOT_AVAILABLE;
		}
  }
  
  public void setTransactionId(String id) { 
  	MonitoredThread monthr = getMonitoredThread();
		if (monthr != null) {
		  monthr.setTransactionId(id);
		}
  }

	public void setApplicationName(String applicationName) {
		MonitoredThread monthr = getMonitoredThread();
		if (monthr != null) {
		  monthr.setApplicationName(applicationName);
		}
	}

	public void setRequestID(String requestID) {
		MonitoredThread monthr = getMonitoredThread();
		if (monthr != null) {
		  monthr.setRequestID(requestID);
		}
	}

	public void setSessionID(String sessionID) {
		MonitoredThread monthr = getMonitoredThread();
		if (monthr != null) {
		  monthr.setSessionID(sessionID);
		}
	}
	
	public void clearManagedThreadRelatedData() {
		// do nothing
	}
	
}