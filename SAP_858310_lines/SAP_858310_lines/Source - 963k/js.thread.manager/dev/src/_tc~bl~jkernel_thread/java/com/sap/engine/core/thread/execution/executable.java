package com.sap.engine.core.thread.execution;

import com.sap.engine.core.Names;
import com.sap.engine.core.thread.ContextDataImpl;
import com.sap.engine.core.thread.ThreadContextImpl;
import com.sap.engine.frame.core.thread.ContextData;
import com.sap.engine.frame.core.thread.ContextObject;
import com.sap.engine.frame.core.thread.ThreadInfo;
import com.sap.engine.frame.core.thread.execution.ExecutionMonitor;
import com.sap.engine.frame.core.thread.execution.Executor;
import com.sap.engine.lib.logging.LoggingHelper;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LoggingUtilities;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;
import com.sap.tools.memory.trace.AllocationStatisticRegistry;

import java.util.HashMap;

/**
 * A wrapper class for each user's Runnable extending ExecutorMonitor.Provides
 * implementation for the runnable state information and management functionality.
 * 
 * @author Elitsa Pancheva
 */
public class Executable implements ExecutionMonitor, Runnable {

  private volatile byte currentState = 0;

  private ExecutorImpl myExecutor = null;
  private Runnable myRunnable = null;
  private boolean amIPooled = false;
  private final Object stateSynch = new Object();
  /**
   * logging data
   */
  private final static Location location = Location.getLocation(Executable.class.getName(), Names.KERNEL_DC_NAME, Names.THREAD_MANAGER_CSN_COMPONENT);
  /**
   * context data
   */
  private ContextData ctxData = new ContextDataImpl();

  private String taskName = null;
  
  private String threadName = null;
  /**
   * monitoring data
   */
  private ThreadInfo tInfo = new ThreadInfo();

  void initEnvironment(Executor myExecutor, Runnable myRunnable, String threadName, String taskName, boolean amIPooled, boolean cleanInheritance) {
    this.myExecutor = (ExecutorImpl) myExecutor;
    this.myRunnable = myRunnable;
    this.amIPooled = amIPooled;
    this.threadName = threadName;
    this.taskName = taskName;

    // set info in th monitoring object
    tInfo.setRunnable(myRunnable);
    tInfo.setClassName(myRunnable.getClass().getName());
    tInfo.setTaskName(taskName);
    tInfo.setThreadName(threadName);

    // set thread context specific data in the task
    ctxData.inheritFromCurrentThread(cleanInheritance);
    synchronized(stateSynch) {
      this.currentState = ExecutionMonitor.STATE_NOT_RUNNING;
    }
  }
  
  // used by the SingleThread to add the monitoring information to
  public ThreadInfo getThreadInfo() {
    return tInfo;
  }

  void clean() {
    synchronized(stateSynch) {
      this.currentState = 0;
    }
    this.myExecutor = null;
    this.myRunnable = null;
    this.amIPooled = false;
    this.ctxData.empty();
    
    // clear the information in the monitoring object
    tInfo.setClassName(null);
    tInfo.setPoolName(null);
    tInfo.setRunnable(null);
    tInfo.setSubtaskName(null);
    tInfo.setTaskName(null);
    tInfo.setThreadName(null);
    tInfo.setUserName(null);
  }

  public void run() {
  	run(false);
  }
  
  public void run(boolean processInCurrentThread) {
    // check state
    if (!compareAndSetState(ExecutionMonitor.STATE_NOT_RUNNING, ExecutionMonitor.STATE_RUNNING)) {
      return;
    }
    
    String currentThreadName = null;
    if (threadName != null) {
      currentThreadName = Thread.currentThread().getName();
      Thread.currentThread().setName(threadName);
    }

    //System.out.println("Thread name will be changed from " + currentThreadName + " to " + threadName);
    ThreadContextImpl currentThreadCtx = ((ThreadContextImpl) ThreadContextImpl.getThreadContext());
    try {
      preRun(currentThreadCtx);
      myRunnable.run(); //todo priviledgedAction?
    } catch (RuntimeException rex) {
      SimpleLogger.trace(Severity.ERROR, location, 
      		LoggingUtilities.getDcNameByClassLoader(myRunnable.getClass().getClassLoader()),
					null,
					"ASJ.krn_thd.000003", 
      		"Execution of Runnable [{0}] throws the following Exception [{1}].", rex, myRunnable, rex.toString());
    } finally {
      Thread.interrupted();
      try {
      	if (processInCurrentThread) {
      		currentThreadCtx.empty(false); //if this is execution of a task in the current thread we should not clean up the thread locals and statistics
      	} else {
          currentThreadCtx.empty();
      	}
      } catch (RuntimeException re) {
        SimpleLogger.trace(Severity.WARNING, location, "ASJ.krn_thd.000004", "Cannot clean the ThreadContext after execution of Runnable [{0}] because of [{1}]", re, myRunnable, re.toString());
      }
      Thread.currentThread().setContextClassLoader(null);
      
      if (currentThreadName != null) {
        //System.out.println("Thread name will be changed back from " + Thread.currentThread().getName() + " to " + currentThreadName);
        Thread.currentThread().setName(currentThreadName);
      }
      synchronized (stateSynch) {
        currentState = ExecutionMonitor.STATE_COMPLETE;
        stateSynch.notifyAll();
      }
      if (myExecutor != null) {
        myExecutor.pushNextTask();
      } // if myExecutor is null => this task is not meant to be executed by a pooled thread
    }
  }
  
  /**
   * Sets the ContextObject[] for the current thread context to the one, stored in the Task object.
   *
   * @param txCtx
   * @param task
   */
  private void preRun(ThreadContextImpl txCtx) {
  	ctxData.loadDataInTheCurrentThread();
  }

  private boolean compareAndSetState(byte oldValue, byte newValue) {
    boolean result;
    synchronized(stateSynch) {
      if (result = (currentState == oldValue)) {
        currentState = newValue;
      }
    }
    return result;
  }

// TaskMonitor methods 

  /* (non-Javadoc)
  * @see com.sap.engine.lib.threadmgmt.api.execution.ExecutionMonitor#getState()
  */
  public byte getState() {
    synchronized(stateSynch) {
      return currentState;
    }
  }

  /* (non-Javadoc)
  * @see com.sap.engine.lib.threadmgmt.api.execution.ExecutionMonitor#cancel()
  */
  public boolean cancel() {
    // check state
    return compareAndSetState(ExecutionMonitor.STATE_NOT_RUNNING, ExecutionMonitor.STATE_CANCELED);
  }

  /* (non-Javadoc)
   * @see com.sap.engine.lib.threadmgmt.api.execution.ExecutionMonitor#join()
   */
  public void join() {
    ThreadContextImpl parentTCtx = (ThreadContextImpl) ThreadContextImpl.getThreadContext();
    if (parentTCtx.isSystem() || (!this.process())) {
      synchronized (stateSynch) {
        while (currentState == ExecutionMonitor.STATE_RUNNING) {
          try {
            //System.out.println(taskName + " : starts waiting for the thread to finish current state is: " + currentState);
            stateSynch.wait();
          } catch (InterruptedException e) {
            LoggingHelper.traceThrowable(Severity.DEBUG, location, "Executable.join().InterruptedException", e);
          }
          //System.out.println(taskName + " : wait ended current state is -> " + currentState);
        }
      }
    }
  }
  
  /* (non-Javadoc)
   * @see com.sap.engine.lib.threadmgmt.api.execution.ExecutionMonitor#process()
   */
  public boolean process() {
    ThreadContextImpl tc = (ThreadContextImpl) ThreadContextImpl.getThreadContext();
    if (tc.isSystem()) {
      throw new RuntimeException("Cannot process application task from syshtem thread.");
    }
    Thread t = Thread.currentThread();
    //  backed up thread context, context object table, thread tag for memory analysis and thread name
    ClassLoader currentCCL = t.getContextClassLoader();

    //we need a new copy here, as the original one will be wiped out by the ContextDataImpl <-> ThreadContext contract during the execution. 
    HashMap<String, ContextObject> currentCoTable = tc.getCOTable();
    String name = t.getName();
    String threadTag = AllocationStatisticRegistry.getThreadTag();
    try {
      run(true);
    } finally {
      // return back the original the original data in the current thread
      t.setName(name);
      ThreadContextImpl tctx = (ThreadContextImpl) ThreadContextImpl.getThreadContext();
      tctx.setCOTable(currentCoTable);
      t.setContextClassLoader(currentCCL);
      // first clear the thread tag because task might have set it in the process time
      AllocationStatisticRegistry.clearThreadTag();
      // set the backup tag if not null
      if (threadTag != null) {
      	AllocationStatisticRegistry.setThreadTag(threadTag);
      }
    }
    
    synchronized(stateSynch) {
      if (currentState == ExecutionMonitor.STATE_RUNNING) {
        return false;
      } else {
        return true;
      }
    }  
  }

  /* (non-Javadoc)
   * @see com.sap.engine.lib.threadmgmt.api.execution.ExecutionMonitor#getRunnable()
   */
  public Runnable getRunnable() {
    return myRunnable;
  }

  public void tryToRelease() {
    if (amIPooled) {
      myExecutor.releaseTask(this);
    }
  }

  public String getTaskName() {
    return taskName;
  }

  public String getThreadName() {
    return threadName;
  }

  
}
