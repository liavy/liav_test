package com.sap.engine.core.thread;

import java.security.AccessControlContext;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.sap.engine.core.Names;
import com.sap.engine.frame.core.thread.ContextData;
import com.sap.engine.frame.core.thread.ContextObject;
import com.sap.engine.system.ThreadWrapper;
import com.sap.engine.system.ThreadWrapperExt;
import com.sap.tc.logging.Location;
import com.sap.tools.memory.trace.AllocationStatisticRegistry;
import com.sap.jvm.Capabilities; 
import com.sap.jvm.monitor.vm.DebugState;
import com.sap.jvm.monitor.vm.VmDebug;
import com.sap.jvm.monitor.vm.VmDebugInfo;
import com.sap.jvm.monitor.vm.VmInfo;

/**
 * Class that keeps all the data which is needed to be taken from the parent thread and set to child thread.
 * 
 * @author Elitsa Pancheva
 */
public class ContextDataImpl implements ContextData {
	
	/**
	 * The context classloader of the parent thread which should be placed in the child thread
	 */
	private ClassLoader contextClassLoader = null;
	
	/**
	 * The current task id of the parent thread which should be placed in the child thread
	 */
	private long parentTaskId = -1;
	
	/**
	 * The current task id counter instance of the parent thread which should be placed in the child thread
	 */
	private AtomicInteger parentTaskIdCounter = null;
	
	/**
	 * The current user request/operation id of the parent thread which should be propagated to the child thread
	 */
	private String parentTransactionId = null;
		
	/**
	 * The local map (Name -> ContextObject) from the parent thread which should be inherited in the child thread. 
	 */
	private HashMap<String, ContextObject> localContextObjectMap = new HashMap<String, ContextObject>(BaseContext.initialSize);
	
	/**
	 * Keeps the type of the parent thread - system or application. On this value depends if the ContextObjects will 
	 * be inherited in the child thread or not.
	 */
	private boolean wasParentSystem = false;
	
	private AccessControlContext accessControlContext = null;
	
	/*
	 * This tag is used by the memory monitoring infrastructure to measure memory allocation per thread.
	 * The tag must be inherited from parent to child thread and cleared when the thread is returned back in the thread pool.
	 * Feature available only for SAP JVM.
	 */
	private String threadTag = null;
	
	/*
	 * Thread Annotations are used by SAP JVM statistics/profiling/etc.
	 * The Annotations must be inherited from parent to child thread and cleared when the thread is returned back in the thread pool.
	 * Feature available only for SAP JVM.
	 */
	Object[] threadAnnotations = null;
	
	/*
	 * Parent thread could be marked for restricted debug through VmDebug API.
	 * The isMarkedForRestrictedDebug flag must be inherited from parent to child thread and cleared when the thread is returned back in the thread pool.
	 * Feature available only for SAP JVM.
	 */
	private boolean isMarkedForRestrictedDebug = false;
	
	/*
	 * Shows if VM monitoring is enabled, i.e. if the engine is running with SAP VM.
	 */
	public static final boolean VM_MONITORING_ENABLED;
	
	/*
	 * Shows if VM restricted debug is enabled, i.e. if the engine is running with SAP VM.
	 */
	private static final boolean VM_RESTRICTED_DEBUG_ENABLED;
      
  static {
    boolean hasVMMonitoring = false;
    boolean hasVMRestrictedDebug = false;
    try {
      hasVMMonitoring = Capabilities.hasVmMonitoring();
      hasVMRestrictedDebug = Capabilities.hasRestrictedDebugging();
    } catch (Exception e) { 
      //$JL-EXC$ VM_MONITORING_ENABLED and VM_RESTRICTED_DEBUG_ENABLED are set to false
    }
    VM_MONITORING_ENABLED = hasVMMonitoring;
    VM_RESTRICTED_DEBUG_ENABLED = hasVMRestrictedDebug;
  }

	
	/**
   * Used for logging
   */
  private final static Location location = Location.getLocation(ContextDataImpl.class.getName(), Names.KERNEL_DC_NAME, Names.THREAD_MANAGER_CSN_COMPONENT);
	
	/**
	 * Extracts all the needed information from the parent ThreadContext and saves it so that when needed all the 
	 * related data will be loaded in the child thread.
	 * 
	 */
	public void inheritFromCurrentThread(boolean cleanInheritance) {
		contextClassLoader = Thread.currentThread().getContextClassLoader();
    ThreadContextImpl parentTCtx = (ThreadContextImpl) ThreadContextImpl.getThreadContext();
    wasParentSystem = parentTCtx.isSystem();
    if (!wasParentSystem) {
    	if (cleanInheritance) {
      	localContextObjectMap = parentTCtx.inheritClean(parentTCtx, localContextObjectMap);
    	} else {
      	localContextObjectMap = parentTCtx.inherit(parentTCtx, localContextObjectMap);
    	}
    }
    // for memory allocation analysis, keep the tag from the current thread in order to pass it to the child thread when the time comes
    threadTag = AllocationStatisticRegistry.getThreadTag();
    
    // first check if VM monitoring functionality is available 
    if (VM_MONITORING_ENABLED) {    
      // for SAP JVM profiler, keep the thread annotations from the current thread in order to pass them to the child thread when the time comes
      if (ThreadAnnotationHandler.THREAD_ANNOTATIONS_AVAILABLE) {
  		  threadAnnotations = ThreadAnnotationHandler.getHandler().getThreadAnnotations(); 
  		}
    } else {
    	if (location.beDebug()) {
    	  location.debugT("VM monitoring functionality is NOT available");
      }
    }
    
    //  first check if VM restricted debug functionality is available
    if (VM_RESTRICTED_DEBUG_ENABLED) {
      VmDebugInfo info = VmInfo.getDebugInfo();
      DebugState state = info.getDebugState();
      
      if (!state.equals(DebugState.STATE_NOT_ACTIVE)) {
        //  for restricted debugging, keep the flag isMarkedForRestrictedDebug from the current thread in order to pass it to the child thread when the time comes
      	isMarkedForRestrictedDebug = !VmDebug.isThreadRestricted();
      }
      if (location.beDebug()) {
    	  location.debugT("Current VM Debug state is ["+state.getDescription()+"]");
    	  location.debugT("Thread tag ["+threadTag+"], Annotations ["+ThreadAnnotationHandler.getHandler().getAnnotationsAsMap(threadAnnotations)+"] and isMarkedForRestrictedDebug flag ["+isMarkedForRestrictedDebug+"] are collected from parent thread ["+Thread.currentThread().getName()+"]");
      }
    } else {
    	if (location.beDebug()) {
    	  location.debugT("VM restricted debug functionality is NOT available");
      }
    }
    
    parentTaskId = ThreadWrapper.getCurrentTaskId();
    parentTaskIdCounter = ThreadWrapperExt.getCurrentTaskCounter();
    parentTransactionId = ThreadWrapper.getTransactionId();
  }
	
	/**
	 * Loads the extracted data from the parent thread in the clild thread. We need this method to ensure 
	 * proper inheritance among threads when working in thread pool environment.
	 */
	public void loadDataInTheCurrentThread() {
		ThreadContextImpl childTCtx = (ThreadContextImpl) ThreadContextImpl.getThreadContext();
		if (childTCtx != null) {
		  localContextObjectMap = childTCtx.setCOTable(localContextObjectMap);
		}
		Thread cthread = Thread.currentThread();
		cthread.setContextClassLoader(contextClassLoader);
    if (cthread.getPriority() != Thread.NORM_PRIORITY) {
    	cthread.setPriority(Thread.NORM_PRIORITY);
    }
    // for memory allocation analysis, pass the tag (if any) in the child thread
    if (threadTag != null) {
      AllocationStatisticRegistry.setThreadTag(threadTag);
      if (location.beDebug()) {
      	location.debugT("Thread tag ["+threadTag+"] is set in child thread ["+Thread.currentThread().getName()+"]");
      }
    }
    
    // for vm info, pass the user (if any) in the child thread
    if (threadAnnotations != null) {
    	if (ThreadAnnotationHandler.THREAD_ANNOTATIONS_AVAILABLE) {
  		  ThreadAnnotationHandler.getHandler().setThreadAnnotations(threadAnnotations); 
  		}
      if (location.beDebug()) {
      	location.debugT("Annotations ["+ThreadAnnotationHandler.getHandler().getAnnotationsAsMap(threadAnnotations)+"] are set in child thread ["+Thread.currentThread().getName()+"]");
      }
    }
    
    //  for vm debug, pass the isRestrictedDebug flag (if true) in the child thread
    if (isMarkedForRestrictedDebug) {
      VmDebug.addRestrictedDebugThread();
      if (location.beDebug()) {
      	location.debugT("The child thread ["+Thread.currentThread().getName()+"] is added for restricted debug");
      }
    }
    
    if (parentTaskIdCounter != null) {
      ThreadWrapperExt.setCurrentTaskId(parentTaskId, parentTaskIdCounter);
    } else {
    	ThreadWrapper.setCurrentTaskId(parentTaskId);
    }
    if (parentTransactionId != null) {
      ThreadWrapperExt.setTransactionId(parentTransactionId);
    }
    //isSystem is deliberately ignored, as it's supposed to be in the context of the processing thread
	}
	
	public void empty() {
		contextClassLoader = null;
		localContextObjectMap.clear();
		wasParentSystem = false;
		accessControlContext = null;
		parentTaskId = -1;
		parentTaskIdCounter = null;
		parentTransactionId = null;
		threadTag = null;
		// clear the tag from the thread as it will be returned back to the pool
		// This call must be the first call inside the finally block which guards
		// the Runnable in the pooled thread to ensure that the thread tag is cleared
		// in any case! If it is not cleared the thread will keep its tag and thereby
		// be slowed down considerably. Beware of any changes in between the finally
		// block in the executing pooled thread and this line.
		AllocationStatisticRegistry.clearThreadTag();
	  if (location.beDebug()) {
   	  location.debugT("Thread ["+Thread.currentThread().getName()+"] will be returned back in the pool => thread tag is cleared.");
    }
	  
	  if (VM_MONITORING_ENABLED) {
	    // always clear the annotations from the thread as the custom Runnable could be set it at its runtime
	    threadAnnotations = null;
		  // clear the userName from the thread as it will be returned back to the pool
	    if (ThreadAnnotationHandler.THREAD_ANNOTATIONS_AVAILABLE) {
  		  ThreadAnnotationHandler.getHandler().cleanThreadAnnotations();
  		}
	    if (location.beDebug()) {
   	    location.debugT("Thread ["+Thread.currentThread().getName()+"] will be returned back in the pool => annotations are set to null.");
      }
	  }
	  
	  if (VM_RESTRICTED_DEBUG_ENABLED) {
	  //if (isRestrictedDebug) { // always clear the restricted debug flag in case the custom task has added the thread in restricted debug
	  	isMarkedForRestrictedDebug = false;
		  // clear the isRestrictedDebug flag from the thread as it will be returned back to the pool
		  VmDebug.removeRestrictedDebugThread();
	    if (location.beDebug()) {
   	    location.debugT("Thread ["+Thread.currentThread().getName()+"] will be returned back in the pool => is removed from the restricted debugging.");
      }
	  //}
	  }
	}
	
	public AccessControlContext getAccessControlContext() {
		return accessControlContext;
	}
	
	public ClassLoader getContextClassLoader() {
		return contextClassLoader;
	}
	
	public boolean isSystem() {
		return wasParentSystem;
	}

	// this method is used only by the system thread manager to indicate that the trhead to be started is system
	// and there is no need to keep any parent data in case the parent thread is application one.
	public void startInSystemThread(boolean isSystem) {
    this.wasParentSystem = isSystem;	
    if (isSystem) {
    	localContextObjectMap = new HashMap<String, ContextObject>();
    }
	}
} 