package com.sap.engine.core.thread;

import java.util.concurrent.atomic.AtomicInteger;

import com.sap.engine.core.Names;
import com.sap.engine.system.MonitoredThread;
import com.sap.engine.system.ShmThread;
import com.sap.engine.system.ShmThreadImpl;
import com.sap.engine.system.ThreadWrapper;
import com.sap.engine.system.ThreadWrapperExt;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;


/**
 * This class is used to associate MonitoredThread with java.lang.Thread using
 * ThreadLocal concept.
 * @author I024135
 */
public class MonitoringInfo extends InheritableThreadLocal {
	static Location location = Location.getLocation(MonitoringInfo.class.getName(), Names.KERNEL_DC_NAME, Names.THREAD_MANAGER_CSN_COMPONENT); 
	private MonitoredThreadImpl monitoredThread = null;
	private ShmThread shmThread = null;
	public static final String JAVA_LANG_THREAD_POOL_NAME = "java.lang.Thread";
	transient private long initialOperationID = -1;
	private String transactionID = null;
	private AtomicInteger taskCounter = null;
	
	
	public MonitoringInfo() {}
	
	public MonitoringInfo(long operationID, String transactionID, AtomicInteger counter) {
		this.initialOperationID = operationID;
		this.transactionID = transactionID;
		this.taskCounter = counter;
	}
  
	protected MonitoringInfo initialValue() {
		return new MonitoringInfo(-1, null, null);
  }
	
	protected Object childValue(Object parentValue) {
		MonitoredThread monThr = ((MonitoringInfo) parentValue).getMonitoredThread();
		if (monThr != null) {
			if (location.bePath()) {
	      location.pathT("MonitoringInfo.childValue() is called in thread ["+Thread.currentThread()+"] operation ID to be propagated to the new thread is["+monThr.getCurrentTaskId()+"], transaction ID is ["+monThr.getTransactionId()+"]. Ids are got through Monitored Thread.");
	    }
      return new MonitoringInfo(monThr.getCurrentTaskId(), monThr.getTransactionId(), monThr.getCurrentTaskCounter());
		} else {
			if (location.bePath()) {
	      location.pathT("MonitoringInfo.childValue() is called in thread ["+Thread.currentThread()+"] operation ID to be propagated to the new thread is["+ThreadWrapper.getCurrentTaskId()+"], transaction ID is ["+ThreadWrapper.getTransactionId()+"]. Ids are got through ThreadWrapper.");
	    }
			return new MonitoringInfo(ThreadWrapper.getCurrentTaskId(), ThreadWrapper.getTransactionId(), ThreadWrapperExt.getCurrentTaskCounter());
		}
  }
  
	public MonitoredThreadImpl getMonitoredThread() {
		return monitoredThread;
	}
	
	public void cleanMonitoring() {
		if (location.bePath()) {
      location.pathT("MonitoringInfo.cleanMonitoring() is called in thread ["+Thread.currentThread()+"] shmThread=["+shmThread+"], monitoredThread=["+monitoredThread+"], opperationID=["+initialOperationID+"], transactionID=["+transactionID+"].");
    }
		try {
			if (monitoredThread != null) {
				// clear both thread context (empty() method for all ContextObjects is called) and monitoring info
			  monitoredThread.clearThreadRelatedData();
			}
		} catch (Exception e) {
			if (location.bePath()) {
	      location.traceThrowableT(Severity.PATH, "Exception caught when performing MonitoredThread.clearTaskIds()", e);
	    }
		} finally {
			try {
	      if (shmThread != null) {
		    	if (location.bePath()) {
			      location.pathT("MonitoringInfo.cleanMonitoring() is called in thread ["+Thread.currentThread()+"] ShmThread is cleaned and closed to unregister this thread from MMC monitoring.");
			    }
		    	shmThread.clean();
		      shmThread.close();
		      shmThread = null;
		    }
			} catch (Exception e) {
				if (location.beWarning()) {
		      location.traceThrowableT(Severity.WARNING, "Exception caught during ShmThread clean and close procedure. As a result this thread will be still visible in MMC monitoring although the thread is already dead.", e);
		    }
			}
		}
	}
  
	public void initMonitoring(Runnable thread) {
  	if (ThreadWrapper.isthreadMonitoringEnabled()) {
			ShmThreadCallbackImpl callback = new ShmThreadCallbackImpl();
	  	this.shmThread = new ShmThreadImpl(callback);
	    shmThread.clean();
	    shmThread.setPoolName(JAVA_LANG_THREAD_POOL_NAME);
	    shmThread.setThreadName(Thread.currentThread().getName());
	    shmThread.setClassName(thread.getClass().getName());
	    shmThread.store();
	    this.monitoredThread = new MonitoredThreadImpl(shmThread, callback);
	    if (initialOperationID != -1) {
	    	monitoredThread.setCurrentTaskId(initialOperationID, taskCounter);
	    }
	    if (transactionID != null) {
	    	monitoredThread.setTransactionId(transactionID);
	    }
	    	    
	    if (location.bePath()) {
	      location.pathT("Thread monitoring initialized successfully. shmThread=["+shmThread+"], monitoredThread=["+monitoredThread+"], opperationID=["+initialOperationID+"], taskCounter=["+taskCounter+"], transactionID=["+transactionID+"] currentThreadName is ["+Thread.currentThread().getName()+"]");
	    }
  	} else {
  		this.monitoredThread = new MonitoredThreadImpl(null, null);
  	}
  }
	
}
