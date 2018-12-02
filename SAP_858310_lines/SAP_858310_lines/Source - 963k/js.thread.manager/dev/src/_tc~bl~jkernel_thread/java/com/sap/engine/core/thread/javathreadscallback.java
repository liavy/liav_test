package com.sap.engine.core.thread;

import com.sap.engine.core.Names;
import com.sap.engine.frame.ProcessEnvironment;
import com.sap.engine.system.MonitoredThread;
import com.sap.jvm.monitor.thread.ThreadWatcher;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Callback which is registered in the SAP VM and used to hook engine logic in every java.lang.Thread life cycle.
 * @author I024135 *
 */
public class JavaThreadsCallback extends ThreadWatcher.Callback {
	// ThreadLocal used for java.lang.Thread monitoring
	static MonitoringInfo info = new MonitoringInfo();
	// Utility class used to remove the ThreadLocal instances associated with a certain thread before this thread dies
	//ThreadLocalsRemover threadLocalsRemover = ThreadLocalsRemover.getInstanceOnce();
	static Location location = Location.getLocation(JavaThreadsCallback.class.getName(), Names.KERNEL_DC_NAME, Names.THREAD_MANAGER_CSN_COMPONENT); 
	
	public void started(Runnable thread) {
		try {
			if (location.bePath()) {
		    location.pathT("JavaThreadsCallback.started before run() is called for thread " + thread);
			}
		  if (Thread.currentThread() instanceof MonitoredThread) {
		  	if (location.bePath()) {
		  	  location.pathT("Engine thread => skip it");
		  	}
		  	// get MonitorInfo once to associate an InheritableThreadLocal to the current thread
		  	info.get();
		  	return;
		  } 
		  
		  ((MonitoringInfo) info.get()).initMonitoring(thread);
		  
		} catch (ThreadDeath td) {
			if (location.beInfo()) {
			  location.traceThrowableT(Severity.INFO, "JavaThreadsCallback.started(" + Thread.currentThread() + ") Unexpected ThreadDeath is caugth", td);
			}
      throw td;
    } catch (OutOfMemoryError o) {
        // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
        // Please do not remove this comment !
      ProcessEnvironment.handleOOM(o);
		} catch (Throwable t) {
			if (location.beInfo()) {
			  location.traceThrowableT(Severity.INFO, "JavaThreadsCallback.started(" + thread + ") Unexpected Throwable is caugth", t);
			}
		}
	}
	
	public void ended() {
		try {
			if (location.bePath()) {
			  location.pathT("JavaThreadsCallback.ended after run() is called for thread " + Thread.currentThread());
			}
			
			try {
				//Add clean up for native threads when a java thread is going to die. 
				//This feature was requested to solve the native thread leak in rfcengine - UWL scenario.
				com.sap.bc.proj.jstartup.JStartupNatives.cleanupThread();
				if (location.bePath()) {
				  location.pathT("Native thread cleanup is successfully executed");
				}
			} catch (ThreadDeath td) {
				throw td;
	    } catch (OutOfMemoryError o) {
	      throw o;
			} catch (Throwable e) {
				if (location.beInfo()) {
				  location.traceThrowableT(Severity.INFO, "JavaThreadsCallback.ended(" + Thread.currentThread() + ") Unexpected Exception is caugth", e);
				}
			}
			
			if (Thread.currentThread() instanceof MonitoredThread) {
		  	if (location.bePath()) {
				  location.pathT("Engine thread => skip it");
		  	}
		  	return;
		  }
			((MonitoringInfo) info.get()).cleanMonitoring();
			
		} catch (ThreadDeath td) {
			if (location.beInfo()) {
			  location.traceThrowableT(Severity.INFO, "JavaThreadsCallback.ended(" + Thread.currentThread() + ") Unexpected ThreadDeath is caugth", td);
			}
      throw td;
    } catch (OutOfMemoryError o) {
        // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
        // Please do not remove this comment !
      ProcessEnvironment.handleOOM(o);
		} catch (Throwable t) {
			if (location.beInfo()) {
			  location.traceThrowableT(Severity.INFO, "JavaThreadsCallback.ended(" + Thread.currentThread() + ") Unexpected Throwable is caugth", t);
			}
		}
	}


}