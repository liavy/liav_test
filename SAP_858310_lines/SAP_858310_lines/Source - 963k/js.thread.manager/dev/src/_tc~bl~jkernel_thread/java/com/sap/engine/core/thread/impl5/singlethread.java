/*
 * Copyright (c) 1999 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.core.thread.impl5;

import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

import com.sap.engine.core.Names;
import com.sap.engine.core.thread.ContextDataImpl;
import com.sap.engine.core.thread.MonitoredThreadImpl;
import com.sap.engine.core.thread.ShmThreadCallbackImpl;
import com.sap.engine.core.thread.ThreadContextImpl;
import com.sap.engine.frame.ProcessEnvironment;
import com.sap.engine.frame.core.thread.ContextData;
import com.sap.engine.frame.core.thread.Task;
import com.sap.engine.frame.core.thread.ThreadInfo;
import com.sap.engine.lib.util.base.LinearItem;
import com.sap.engine.lib.util.base.NextItem;
import com.sap.engine.lib.util.base.PrevItem;
import com.sap.engine.lib.util.WaitQueue;
import com.sap.engine.lib.logging.LoggingHelper;
import com.sap.engine.system.MonitoredThread;
import com.sap.engine.system.ShmThread;
import com.sap.engine.system.ShmThreadImpl;
import com.sap.engine.system.SystemEnvironment;
import com.sap.engine.system.ThreadWrapper;
import com.sap.jvm.monitor.vm.VmInfo;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LoggingUtilities;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.SimpleLogger;

/**
 * This class is used for controling the threads. It has only two methods -
 * one for getting a Runnbale object from a ThreadManager and executing it
 * and one for stoping the thread running an instance of this class. When
 * <code>stop</code> method is used it is important to notice that this
 * method will finish exactly one object before the thread is stopped.
 *
 * @author Krasimir Semerdzhiev
 * @version 2.0
 */
final class SingleThread extends Thread implements LinearItem, MonitoredThread {

  private ShmThread shmThread = null;
  private ThreadInfo tInfo = null;
  private MonitoredThreadImpl monitoredThread = null;

  /** Category used for logging critical engine messages for AGS. */
  private static final Category catServerCritical = Category.getCategory(Category.SYS_SERVER, "Critical");

  protected ThreadManagerImpl threadManager;
  protected WaitQueue queue;
  protected boolean continueWork;
  // this flag shows if the Thread is meant to be pooled and used several times 
  protected boolean reusableThread = false;
  protected Task action = null;
  protected String originalName = null;
  /**
   * Used for logging
   */
  private final static Location location = Location.getLocation(SingleThread.class.getName(), Names.KERNEL_DC_NAME, Names.THREAD_MANAGER_CSN_COMPONENT);

  /**
   * Constructor.
   *
   * @param   threadManager  link to ThreadManager's works
   */
  protected SingleThread(ThreadManagerImpl threadManager, WaitQueue queue, String name) {
    super(ThreadManagerImpl.threadGroup, name);
    this.originalName = name;
    this.threadManager = threadManager;
    this.queue = queue;
    continueWork = true;
    reusableThread = true;
//    shmThread = new ShmThreadImpl();
//    shmThread.clean();
//    shmThread.setPoolName(ThreadManagerImpl.THREADS_NAME_HEADER);
//    shmThread.store();
//    monitoredThread = new MonitoredThreadImpl(shmThread);
  }
  
  //make the initialization of ShmThread in run() method so that it is executed in the pooled thread and not in its parent
  private void initMonitoring() {
  	if (ThreadWrapper.isthreadMonitoringEnabled()) {
	  	ShmThreadCallbackImpl callback = new ShmThreadCallbackImpl();
	  	shmThread = new ShmThreadImpl(callback);
	    shmThread.clean();
	    shmThread.setPoolName(ThreadManagerImpl.THREADS_NAME_HEADER);
	    shmThread.store();
	    monitoredThread = new MonitoredThreadImpl(shmThread, callback);
	    if (location.bePath()) {
	      location.pathT("Thread monitoring initialized successfully. shmThread=["+shmThread+"], monitoredThread=["+monitoredThread+"], currentThreadName is ["+this.getName()+"]");
	    }
  	} else {
  		monitoredThread = new MonitoredThreadImpl(null, null);
  	}
  }

  void setThreadInfo(com.sap.engine.frame.core.thread.ThreadInfo tInfo) {
    this.tInfo = tInfo; // keep a reference to the task to prevent returning in the pool
    
    if (ThreadWrapper.isthreadMonitoringEnabled()) {
	    if (this.tInfo.getPoolName() == null) {
	      shmThread.clean();
	      shmThread.setPoolName(ThreadManagerImpl.THREADS_NAME_HEADER);
	      this.tInfo.setPoolName(ThreadManagerImpl.THREADS_NAME_HEADER);
	    } else {
	      this.tInfo.setPoolName(ThreadManagerImpl.THREADS_NAME_HEADER);
	      shmThread.setClassName(this.tInfo.getRunnable().getClass().getName());
	      shmThread.setPoolName(this.tInfo.getPoolName());
	      shmThread.setSubtaskName(this.tInfo.getTaskName());
	      shmThread.setTaskName(this.tInfo.getTaskName());
	      if (this.tInfo.getThreadName() == null) {
	        this.tInfo.setThreadName(originalName);
	      }
	      this.setName(this.tInfo.getThreadName());
	      shmThread.setThreadName(this.tInfo.getThreadName());
	      shmThread.setUserName("");
	    }
    } else {
    	if (this.tInfo.getThreadName() == null) {
        this.tInfo.setThreadName(originalName);
      }
    	this.tInfo.setPoolName(ThreadManagerImpl.THREADS_NAME_HEADER);
      this.setName(this.tInfo.getThreadName());
    }
  }

  /**
   * Sets initial work. This metod must be used only before the thread is started
   * for the first time.
   *
   * @param   action
   */
  protected void setRunnable(Task action, boolean continueWork) {
    this.action = action;
    this.continueWork = continueWork;
    this.reusableThread = continueWork;
  }

  private void preRun(Task task) {
     ContextData data = task.getContextData();
    data.loadDataInTheCurrentThread();
  }

  private void execute() {
    try {
    	if (shmThread != null) {
        shmThread.store();
    	}
      //this.setContextClassLoader(action.getContextClassLoader());
      preRun(action);
      action.run();
    } catch (RuntimeException rex) {
      // Log critical error - uncatched RuntimeException from system thread
    	SimpleLogger.trace(Severity.ERROR, location, 
      		LoggingUtilities.getDcNameByClassLoader(action.getThreadInfo().getRunnable().getClass().getClassLoader()),
					null,
					"ASJ.krn_thd.000003", 
      		"Execution of Runnable [{0}] throws the following Exception [{1}].", rex, action.getThreadInfo().getRunnable(), rex.toString());
    } finally {
      Thread.interrupted();
      action.release();
      action = null;
      monitoredThread.clearTaskIds();
      this.setContextClassLoader(null);
      if (this.getPriority() != Thread.NORM_PRIORITY) {
        this.setPriority(Thread.NORM_PRIORITY);
      }
    }
  }
  
  private void resetThreadResourceCounters() {
    // first check if VM monitoring functionality is available 
    if (ContextDataImpl.VM_MONITORING_ENABLED) { 
    	try {
        // for SAP JVM thread stack dumps and accounting infrastructure - reset the CPU and memory counters when the thread is returned back in the pool
        VmInfo.resetThreadStackDumpCounters();
    	} catch (ThreadDeath td) {
    		throw td;
    	} catch (OutOfMemoryError oom) {
    		ProcessEnvironment.handleOOM(oom);
    	} catch (Throwable e) {
  	  	if (location.bePath()) {
  	  	  location.traceThrowableT(Severity.PATH, "Cannot reset the resource counters in the current thread ["+Thread.currentThread()+"]", e);
  	  	}
  	  }
    } 
	}

  /**
   * Implements run method of class Runnable.
   *
   */
  public void run() {
    try {
    	initMonitoring();
    	
      ThreadContextImpl currentThreadCtx = ((ThreadContextImpl)threadManager.getThreadContext());
      currentThreadCtx.setSystem(threadManager.isSystem);
      if (action != null) {
        try {
          setThreadInfo(action.getThreadInfo());
          execute();
        } catch (ThreadDeath td) {
          // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
          // Please do not remove this comment !
          threadManager.removeItem(this);
          // mark the thread not reusable anymore because it has been removed from the pool
          this.reusableThread = false;
          // this thread will not be used anymore => set continueWork=false
          this.continueWork = false;
          // do some logging 
          if (!this.threadManager.isShutDown) {
        	  
        	  LoggingUtilities.logAndTrace(Severity.ERROR, catServerCritical, location, td,
            				 "ASJ.krn_thd.000032", null, null,
            				 "System thread [{0}] got ThreadDeath error while processing [{1}]: [{2}]",
            				 new Object[]{this.getName(), action, td.toString()});
          } else {
            location.debugT("System thread [" + this.getName() + "] was stopped when shutting down the engine while processing [" + action + "].");
          }
        } catch (OutOfMemoryError o) {
            // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
            // Please do not remove this comment !
          ProcessEnvironment.handleOOM(o);
          // if there is enough memory this call will not restart the VM and we should take care to replace this thread with new one when needed
          // this thread will not be used anymore => set continueWork=false and rely on the finally block to remove it from the thread pool
          this.continueWork = false;
        } catch (Throwable tr) {
            // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
            // Please do not remove this comment !
         	SimpleLogger.trace(Severity.ERROR, location, 
        			LoggingUtilities.getDcNameByClassLoader(action.getThreadInfo().getRunnable().getClass().getClassLoader()),
    					null,
    					"ASJ.krn_thd.000088", 
          		"Execution of Runnable [{0}] throws the following Error [{1}].", tr, action.getThreadInfo().getRunnable(), tr.toString());
        } finally {
          if (shmThread != null) {
            shmThread.clean();
          }
          tInfo = null;
          // set the original name of the thread back in case the task has changed it
          this.setName(originalName);
        }
        if (!continueWork) {
          if (shmThread != null) {
            shmThread.close();
            shmThread = null;
          }
        }
      }

      while (continueWork) {
        try {
        	resetThreadResourceCounters(); // reset the resource counters just before returning the thread in the queue
          action = (Task)queue.dequeue(threadManager.timeToLiveIdle);
        } catch (InterruptedException e) {
          // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
          // Please do not remove this comment !
          if (!threadManager.isShutDown) {
          	SimpleLogger.trace(Severity.WARNING, location, 
            		LoggingUtilities.getDcNameByClassLoader(action.getThreadInfo().getRunnable().getClass().getClassLoader()),
      					null,
      					"ASJ.krn_thd.000030", 
            		"Execution of Runnable [{0}] was interupted by [{1}].", e, action.getThreadInfo().getRunnable(), e.toString());
          }
          threadManager.removeItem(this);
          // mark the thread not reusable anymore because it has been removed from the pool
          this.reusableThread = false;
          break;
        } catch (ShutDownException rException) {
          // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log here
          // Please do not remove this comment !
          threadManager.removeItem(this);
          // mark the thread not reusable anymore because it has been removed from the pool
          this.reusableThread = false;
          break;
        }
        try {
          if (action != null) {
            setThreadInfo(action.getThreadInfo());
            execute();
          } else {
            if (threadManager.minThreadCount < threadManager.size()) {
              if (threadManager.autoKillThread(this)) {
                // mark the thread not reusable anymore because it has been removed from the pool
                this.reusableThread = false;
                location.infoT("Thread ["+this.getName()+"] did not receive Task to process for ["+threadManager.timeToIdleString+"] seconds and is going down.");
                break;
              }
            }
          }
        } catch (ThreadDeath td) {
          // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
          // Please do not remove this comment !
          threadManager.removeItem(this);
          // mark the thread not reusable anymore because it has been removed from the pool
          this.reusableThread = false;
          if (!this.threadManager.isShutDown) {
        	  LoggingUtilities.logAndTrace(Severity.ERROR, catServerCritical, location, td,
        			  			"ASJ.krn_thd.000032", null, null,
        			  		  	"System thread [{0}] got ThreadDeath error while processing [{1}]: [{2}]",
        			  			new Object[]{this.getName(), action, td.toString()});
            break;
          } else {
            location.debugT("System thread [" + this.getName() + "] was stopped when shutting down the engine while processing [" + action + "].");
            break;
          }
        } catch (OutOfMemoryError o) {
          // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
          // Please do not remove this comment !
          ProcessEnvironment.handleOOM(o);
          // if there is enough memory this call will not restart the VM and we should take care to replace this thread with new one when needed
          // we exit the loop and rely on the finally block to remove it from the thread pool
          break;
        } catch (Throwable tr) {
          // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
          // Please do not remove this comment !
        	String dcName = null;
        	Object obj = action;
        	if (action != null) {
        		dcName = LoggingUtilities.getDcNameByClassLoader(action.getThreadInfo().getRunnable().getClass().getClassLoader());
        		obj = action.getThreadInfo().getRunnable();
        	}
        	SimpleLogger.trace(Severity.ERROR, location, 
          		dcName,
    					null,
    					"ASJ.krn_thd.000090", 
          		"Execution of Runnable [{0}] throws the following Error [{1}].", tr, obj, tr.toString());
        } finally {
          if (shmThread != null) {
            shmThread.clean();
          }
          tInfo = null;
          // set the original name of the thread back in case the task has changed it
          this.setName(originalName);
        }
      }
    } catch (OutOfMemoryError oom) {
      // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
      // Please do not remove this comment !
      ProcessEnvironment.handleOOM(oom);
    } catch (Throwable th) {
      SystemEnvironment.STD_ERR.println("[THREAD MANAGER["+SingleThread.class+"]["+getName()+"]]: Unrecoverable error thrown while processing tasks:");
      th.printStackTrace(SystemEnvironment.STD_ERR);
      LoggingUtilities.logAndTrace(Severity.ERROR, catServerCritical, location, th, "ASJ.krn_thd.000089", null, null, "Unrecoverable error thrown while processing tasks: [{0}]", th);

      // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
      // Please do not remove this comment !
    } finally {
      // continueWork became false (in shutdown of ThreadManager) => this thread cannot be reused anymore => remove it from pool
      if (this.reusableThread) {
        threadManager.removeItem(this);
      }

      if (shmThread != null) {
        shmThread.close();
        shmThread = null;
      }
    }
  }

  /**
   * This method sets work flag equals to false.
   *
   */
  public void stopThread() {
    continueWork = false;
  }

  /**
   *  Gets the identifier of the thread.
   *
   * @return  identifier of the thread.
   */
  public String getThreadId() {
    return getName();
  }

  /**
   * Returns the values of attributes associated with this node.
   *
   * @return   the values of attributes associated with this node.
   */
  public String[] getAttributes() {
    return new String[0];
  }

  // return null if the task is null otherwise return task.toString()
  protected String getTaskDetails() {
    if (action != null) {
      return action.toString();
    } else {
      return null;
    }
  }

  public String toString() {
    return getName();
  }

  /**
   * Successor of the item.<p>
   */
  protected NextItem next = null;
  /**
   * Predecessor of the item.<p>
   */
  protected PrevItem prev = null;

  /**
   * Sets the successor of this item.<p>
   *
   * @param   item the successor of this item.
   */
  public void setNext(NextItem item) {
    next = item;
  }

  /**
   * Retrieves the successor of this item.<p>
   *
   * @return  the successor of this item.
   */
  public NextItem getNext() {
    return next;
  }

  /**
   * Sets the predecessor of the item.<p>
   *
   * @param   item the predecessor of this item.
   */
  public void setPrev(PrevItem item) {
    prev = item;
  }

  /**
   * Retrieves the predecessor of the item.<p>
   *
   * @return  the predecessor of the item.
   */
  public PrevItem getPrev() {
    return prev;
  }

  /**
   * Prepare item to be pooled.<p>
   */
  public void clearItem() {
    prev = null;
    next = null;
  }


  public Object clone() {
    return null;
  }

  public void finalize() { //$JL-FINALIZE$
    if (shmThread != null) {
      shmThread.close();
      shmThread = null;
      Object result = null;
    }
  }

  // Delegation of monitored thread implementation

  public int getThreadState() { return monitoredThread.getThreadState();  }
  public String getSubtaskName() { return monitoredThread.getSubtaskName(); }
  public String getTaskName() { return monitoredThread.getTaskName(); }
  public void popSubtask() { monitoredThread.popSubtask(); }
  public void popTask() { monitoredThread.popTask(); }
  public void pushSubtask(String arg0, int arg1) { monitoredThread.pushSubtask(arg0, arg1); }
  public void pushTask(String arg0, int arg1) { monitoredThread.pushTask(arg0, arg1); }
  public void setThreadState(int arg0) { monitoredThread.setThreadState(arg0); }
  public void setSubtaskName(String arg0) { monitoredThread.setSubtaskName(arg0); }
  public void setTaskName(String arg0) { monitoredThread.setTaskName(arg0); }
  public void setUser(String arg0) { monitoredThread.setUser(arg0); }
  public long getCurrentSubtaskId() { return monitoredThread.getCurrentSubtaskId(); }
  public long getCurrentTaskId() { return monitoredThread.getCurrentTaskId(); }
  public void setCurrentTaskId(long id) { monitoredThread.setCurrentTaskId(id); }
  public void setApplicationName(String appName) { monitoredThread.setApplicationName(appName); }
  public void setRequestID(String requestID) { monitoredThread.setRequestID(requestID); }
  public void setSessionID(String sessionID) { monitoredThread.setSessionID(sessionID); }
  public void clearManagedThreadRelatedData() { monitoredThread.clearManagedThreadRelatedData(); }
  public String getTransactionId() { return monitoredThread.getTransactionId(); }
  public void setTransactionId(String id) { monitoredThread.setTransactionId(id); }
  public void setCurrentTaskId(long id, AtomicInteger counter) { monitoredThread.setCurrentTaskId(id, counter); }
  public AtomicInteger getCurrentTaskCounter() { return monitoredThread.getCurrentTaskCounter(); }
  
  private void writeObject(ObjectOutputStream oos) throws NotSerializableException {
    throw new NotSerializableException(this.getClass().getName());
  }


  private void readObject(ObjectInputStream oos) throws NotSerializableException {
    throw new NotSerializableException(this.getClass().getName());
  }

}

