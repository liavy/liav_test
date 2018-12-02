package com.sap.engine.core.thread.impl3;

import com.sap.engine.core.Names;
import com.sap.engine.core.thread.impl3.ThreadManagerImpl;
import com.sap.engine.frame.ProcessEnvironment;
import com.sap.jvm.monitor.vm.ErrorQueue;
import com.sap.jvm.monitor.vm.ErrorQueueEntry;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LoggingUtilities;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;
import com.sap.jvm.Capabilities;


/**
 * A thread for ErrorQueue handling in case of SAP VM.
 * The thread waits on ErrorQueue.remove() method to get one of the following errors that might have occur on the system:
 * OutOfMemoryError, StackOverFlowError, InternalError, AssertionError 
 * 
 * @author Elitsa Pancheva
 * @version 710
 */
public class ErrorQueueHandler extends Thread {
  private ErrorQueue errorQueue = null;
  private ThreadManagerImpl threadManager = null;
  private static final String THREAD_NAME = "ErrorQueueWatchDog";
  private volatile boolean continueWork = true;
  /*
	 * Shows if VM monitoring is enabled, i.e. if the engine is running with SAP VM.
	 */
	private static final boolean VM_MONITORING_ENABLED;
	static {
    boolean hasVMMonitoring = false;
    try {
      hasVMMonitoring = Capabilities.hasVmMonitoring();
    } catch (Exception e) { 
      //$JL-EXC$ VM_MONITORING_ENABLED is set to false
    }
    VM_MONITORING_ENABLED = hasVMMonitoring;
  }
  
	/*
	 * Location used for messages tracing 
	 */
  private final static Location location = Location.getLocation(ErrorQueueHandler.class.getName(), Names.KERNEL_DC_NAME, Names.THREAD_MANAGER_CSN_COMPONENT);
  
  /*
   * Category used for logging critical engine messages for AGS. 
   */
  private static final Category catServerCritical = Category.getCategory(Category.SYS_SERVER, "Critical");
  
  public ErrorQueueHandler(ThreadManagerImpl threadManager) {
    // check first if we run on SAP VM
  	if (VM_MONITORING_ENABLED) {
  		this.errorQueue = ErrorQueue.getErrorQueue();
    	this.threadManager = threadManager;
      this.setName(THREAD_NAME);
      this.continueWork = true;
    } else {
    	SimpleLogger.trace(Severity.WARNING, 
    						location,
    						"ASJ.krn_thd.000083", 
    						"SAP VM ErrorQueue functionality is not currently available; [{0}] will not be started.", 
    						THREAD_NAME);
    	this.continueWork = false;
    }  	
  }
  
	public void run() {
  	ErrorQueueEntry entry = null;
  	
  	while (continueWork) {
      try {
        entry = errorQueue.remove();
      } catch (InterruptedException e) {
        // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
        // Please do not remove this comment !
        if (continueWork && !threadManager.isShutDown) {
        	LoggingUtilities.logAndTrace(Severity.WARNING, catServerCritical, location, e, 
        								"ASJ.krn_thd.000005",
        								null, null, 
        								"InterruptedException is caught in ErrorQueueHandler.run(). The thread will exit. No more critical errors will be processed by the framework.");
        }
        break;
      } 
      
      try {
        if (entry != null) { 
          if (entry.getError() instanceof OutOfMemoryError) {
        	  // ProcessEnvironment.performLogging() writes to System.err that a OOMError is detected, its stack trace, a thread dump
        	  // If the restart mode is ALWAYS, or if the OOMError is outside the Java heap and the restart mode is not DISABLED, 
        	  // the AS Java is restarted by the ProcessEnvironment.performLogging() method, so it is 
        	  // possible after the call to this method the AS Java to be restarted
        	  ProcessEnvironment.performLogging((OutOfMemoryError) entry.getError());
          } else {
        	  
        	  LoggingUtilities.logAndTrace(Severity.WARNING, catServerCritical, location, entry.getError(),
        			  						"ASJ.krn_thd.000025", null, null,
        			  						"The following Error is thrown by the VM [{0}]. ErrorQueueHandler will just trace it. The caller component should take care to process it properly.",
        			  						new Object[]{entry.getError()});
          }
        } 
      } catch (java.lang.ThreadDeath td) {
        // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
        // Please do not remove this comment !
      	if (continueWork && !threadManager.isShutDown) {
      		LoggingUtilities.logAndTrace(Severity.WARNING, catServerCritical, location, td, "ASJ.krn_thd.000024", null, null, 
      									"ThreadDeath is caught in ErrorQueueHandler.run(). The thread will exit. No more critical errors will be processed by the framework.");
        }
      	throw td;
      } catch (OutOfMemoryError o) {
        // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
        // Please do not remove this comment !
    	  ProcessEnvironment.performLogging(o);
      } catch (Throwable tr) {
        // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
        // Please do not remove this comment !

    	  LoggingUtilities.logAndTrace(Severity.WARNING, catServerCritical, location, tr,
      					"ASJ.krn_thd.000028", null, null,
      					"Throwable is caught in ErrorQueueHandler.run(). The thread will continue its work ignoring the exception: [{0}]",
      					new Object[]{tr});
      } finally {
      	// set entry to null before exit current iteration to release the entry value for garbage collection
      	entry = null;
      } 
    }
  }
	
	public synchronized void stopThread() {
    if (continueWork) {
  		continueWork = false;
      this.interrupt();
    }
	}
	
}
