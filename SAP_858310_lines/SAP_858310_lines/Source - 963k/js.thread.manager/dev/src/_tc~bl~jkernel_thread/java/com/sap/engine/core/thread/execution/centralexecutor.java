package com.sap.engine.core.thread.execution;

import com.sap.engine.core.Names;
import com.sap.engine.core.thread.MonitoredThreadImpl;
import com.sap.engine.core.thread.ShmThreadCallbackImpl;
import com.sap.engine.core.thread.ThreadContextImpl;
import com.sap.engine.core.thread.impl3.ShutDownException;
import com.sap.engine.core.thread.impl3.ThreadManagerImpl;
import com.sap.engine.frame.ProcessEnvironment;
import com.sap.engine.frame.core.thread.ThreadInfo;
import com.sap.engine.lib.util.WaitQueue;
import com.sap.engine.lib.util.ConcurrentSet;
import com.sap.engine.system.MonitoredThread;
import com.sap.engine.system.ShmThread;
import com.sap.engine.system.ShmThreadImpl;
import com.sap.engine.system.SystemEnvironment;
import com.sap.engine.system.ThreadWrapper;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LoggingUtilities;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

import java.security.AccessController;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;


public class CentralExecutor {
	
  private static final String managedNamePattern = "Managed_Application_Thread";
  
  private static final String dedicatedNamePattern = "Dedicated_Application_Thread";
  
  private static final String unmanagedNamePattern = "Complementary_Application_Thread";

  private final WaitQueue centralQueue = new WaitQueue(Integer.MAX_VALUE);
  
  // the maximum number of threads that can be started in the CentralExecutor pool
  private int maxNumberOfThreads = 0;
  
  // the current number of threads started in the central pool.
  private int currentNumberOfPooledThreads = 0;
  
  // the maximum parallelism rate that every executor can get.
  private int maxAllowedParallelismPerExecutor = 0;
  
  //the maximum percentage of thread pools size which is aggreable to be used by one executor.
  private int percentageOfParallelismAllowed = 0;
  
  // monitor to lock when doing resize of the pool
  private Object resizeMonitor = new Object();

  private volatile boolean isShutDown = false;

  private ConcurrentSet processors;

  private int processorCounter = 0;

  ThreadGroup managedThreadsGroup = new ThreadGroup(managedNamePattern);
  
  ThreadGroup dedicatedThreadsGroup = new ThreadGroup(dedicatedNamePattern);
  
  ThreadGroup unmanagedThreadsGroup = new ThreadGroup(unmanagedNamePattern);
  
  /**
   * logging data
   */
  private final static Location location = Location.getLocation(CentralExecutor.class.getName(), Names.KERNEL_DC_NAME, Names.THREAD_MANAGER_CSN_COMPONENT);

  public CentralExecutor(int numberOfThreads, int percentageOfParallelismAllowed) {
  	this.maxNumberOfThreads = numberOfThreads;
  	this.percentageOfParallelismAllowed = percentageOfParallelismAllowed;
  	this.maxAllowedParallelismPerExecutor = (int)(maxNumberOfThreads*percentageOfParallelismAllowed*0.01); // only a certain percentage of all the threads are allowed to be 
    processors = new ConcurrentSet(numberOfThreads);
    if (location.beDebug()) {
  	  location.debugT("Create CentralExecutor with maximum count of the pooled threads ["+maxNumberOfThreads+"] and maxAllowedParallelismPerExecutor["+maxAllowedParallelismPerExecutor+"]. No threads will be initially created. Threads will be created and put in the pool on demand.");
  	}
//    for (int i = 0; i < numberOfThreads; i++) {
//      SingleThread st = createSingleThread(null, managedThreadsGroup);
//      processors.add(st);
//      st.start();
//    }
  }

  void execute(Executable task, boolean tryToResize) {
  	// in case the internal thread at the eng of its run method tries to enqueue next executable no resize is needed,
  	// because this thread will be returned back in the pool so it can process this executable.
  	// Putting one task in the queue but also returning one thread in the pool.
  	if (location.beDebug()) {
  	  location.debugT("Thread ["+Thread.currentThread().getName()+"] tries to put an Executable ["+task+"] in the CentralQueue. TryToResize flag is [" +tryToResize+ "]");
  	}
  	if (tryToResize) {
  		// if there are threads waiting to dequeue a task from the queue (in practise this means that there are no Runnables in the
  		// queue waiting to be processed) we can directly enqueue this task (it will be processed immediately) 
  	  if (!suitableForEnqueue(task)) { //If true - it's already enqueued => exit
        // if there are no free threads at the moment => try to resize the thread pool, i.e. to create new threads
  	  	if (!canResize(task)) { // If true the task is already assigned to the newly started thread 
  	  		// cannot resize => put the task in the queue to wait for free processing thread
      	  centralQueue.enqueue(task);
      	  if (location.beDebug()) {
        	  location.debugT("Thread ["+Thread.currentThread().getName()+"]. Cannot resize => put the task in the queue to wait for free processing thread.");
        	}
        }
      }
  	} else {
  		if (location.beDebug()) {
    	  location.debugT("Thread ["+Thread.currentThread().getName()+"]. Not need to resize => put the task in the queue and return the processing thread in the poool.");
    	}
      centralQueue.enqueue(task);
  	}
  }
    
  private boolean canResize(Executable task) {
    boolean toStartNewThread = false;
    if (location.beDebug()) {
  	  location.debugT("Thread ["+Thread.currentThread().getName()+"]. Try to resize: current number of pooled thread["+currentNumberOfPooledThreads+"], max number of threads can reach["+maxNumberOfThreads+"].");
  	}
  	if (currentNumberOfPooledThreads < maxNumberOfThreads) {
    	synchronized (resizeMonitor) {
    		if (currentNumberOfPooledThreads < maxNumberOfThreads) {
    			currentNumberOfPooledThreads ++;
    			toStartNewThread = true;
    			if (location.beDebug()) {
        	  location.debugT("Thread ["+Thread.currentThread().getName()+"], Successfully incremented current number of pooled threads to ["+currentNumberOfPooledThreads+"]. Will start new thread and pass the Executable to it.");
        	}
    		}
			}
    	
    	if (toStartNewThread) {
    	  SingleThread st = createSingleThread(task, managedThreadsGroup, true);
        processors.add(st);
        st.start();
        return true;
    	} else {
    		if (location.beDebug()) {
      	  location.debugT("Thread ["+Thread.currentThread().getName()+"]. Unable to resize the max traed count is reached.");
      	}
    		return false;
    	}
    } else {
    	if (location.beDebug()) {
    	  location.debugT("Thread ["+Thread.currentThread().getName()+"]. Unable to resize the max traed count is reached.");
    	}
    	return false;
    }
  }

  private boolean suitableForEnqueue(Executable task) {
    synchronized(centralQueue) {
      if (centralQueue.getWaitingDequeueThreadsCount() > centralQueue.size()) {
      	centralQueue.enqueue(task);
      	if (location.beDebug()) {
      	  location.debugT("Thread ["+Thread.currentThread().getName()+"], Executable ["+task+"] is suitable for enqueue => is put in the queue.");
      	}
        return true;
      } else {
      	if (location.beDebug()) {
      	  location.debugT("Thread ["+Thread.currentThread().getName()+"], Executable ["+task+"] is NOT suitable for enqueue.");
      	}
        return false;
      }
    }
  }
  
  public int getMaxNumberOfPooledThreads() {
  	return maxNumberOfThreads;
  }
  
  public int getMaxAllowedParallelismPerExecutor() {
  	return maxAllowedParallelismPerExecutor;
  }
  
  public int getPercentageOfParallelismAllowed() {
  	return percentageOfParallelismAllowed;
  }   

  /**
   * Immediately shutdown the current executor, returning back the list of currently pending tasks,
   * which has to be handled by the caller instead.
   *
   */
  public List forceShutdown() {
    // set isShutdown to stop new comming task requests
    isShutDown = true;

    // cancel all currently running threads
    Object[] workers = processors.toArray();
    for (int i = 0; i < workers.length; i++) {
      ((SingleThread)workers[i]).stop(true);
    }

    // cancel all currently pending tasks
    ArrayList leftTasks = new ArrayList(centralQueue.size());
    while (centralQueue.size() > 0) {
      Object o = centralQueue.poll();
      if (o != null) {
        // TODO log that some tasks are left unprocessed
        leftTasks.add(o);
      }
    }

    return leftTasks;
  }

  private void workerDone(SingleThread t) {
    processors.remove(t); //thread will unregister itself
    if (t.pooledThread) {
    	if (location.beDebug()) {
    	  location.debugT("Thread ["+Thread.currentThread().getName()+"]. will die => decrease the current pooled thread counter.");
    	}
    	synchronized (resizeMonitor) {
        // for some reason one of the pooled threads will die => decrease the current pooled thread counter, so that next execute()
    		// call will try to upsize the pool again
    		currentNumberOfPooledThreads --;
			}
    }
  }

  public void startInManagedThread(Executable r, ThreadGroup threadGroup) {
    SingleThread st = createSingleThread(r, threadGroup, false);
    processors.add(st);
    st.start();
  }
  
  public void executeInDedicatedThread(Runnable runnable, String taskDescription, String threadName) {
     startInManagedThread(wrapRunnable(runnable, threadName, taskDescription), dedicatedThreadsGroup);    
  }
  
  private Executable wrapRunnable(Runnable myRunnable, String threadName, String taskName) {
    if (myRunnable == null) {
      throw new NullPointerException("Runnable to be executed can't be null");
    }

    Executable task = new Executable();
    task.initEnvironment(null, myRunnable, threadName, taskName, false, false);
    return task;
  }

  private SingleThread createSingleThread(Executable r, ThreadGroup group, boolean pooledThread) {
    int currentNumber = 0;
    synchronized (this) {
      currentNumber = processorCounter++;
    }
    return new SingleThread(group.getName() + "_" + currentNumber, r, group, pooledThread);
  }

  /**
   * Single Thread instance, continously executng Runnables from the surrounding central queue.
   */
  private class SingleThread extends Thread implements MonitoredThread {
    private Thread currentThread;
    private Executable initialTask;
    private boolean pooledThread;
    private String myName;
    private String poolName;
    private MonitoredThreadImpl monitoredThread = null;
    private ShmThread shmThread = null;
    private ThreadInfo tInfo = null;
    
    public SingleThread(String myName, Executable initialRunnable, ThreadGroup group, boolean pooledThread) {
      super(group, myName);
      this.initialTask = initialRunnable;
      this.pooledThread = pooledThread;
      this.myName = myName;
      this.poolName = group.getName();
//      shmThread = new ShmThreadImpl();
//      shmThread.clean();
//      shmThread.setPoolName(group.getName());
//      shmThread.store();
//      monitoredThread = new MonitoredThreadImpl(shmThread);
    }
    
    //  make the initialization of ShmThread in run() method so that it is executed in the pooled thread and not in its parent
    private void initMonitoring() {
    	if (ThreadWrapper.isthreadMonitoringEnabled()) {
	    	ShmThreadCallbackImpl callback = new ShmThreadCallbackImpl();
	    	shmThread = new ShmThreadImpl(callback);
	      shmThread.clean();
	      shmThread.setPoolName(poolName);
	      shmThread.store();
	      monitoredThread = new MonitoredThreadImpl(shmThread, callback);
	      if (location.bePath()) {
	        location.pathT("Thread monitoring initialized successfully. shmThread=["+shmThread+"], monitoredThread=["+monitoredThread+"], currentThreadName is ["+this.getName()+"]");
	      }
    	} else {
    		monitoredThread = new MonitoredThreadImpl(null, null);
    	}
    }

    public void stop(boolean immediately) {
      //TODO raise a flag if not immediately
      final Thread myThread = currentThread;
      if ( myThread != null) {
        myThread.interrupt();
      }
    }
        
    public void run() {
    	initMonitoring();
    	
      ThreadContextImpl currentThreadCtx = ((ThreadContextImpl) ThreadContextImpl.getThreadContext());
      currentThreadCtx.setSystem(false); // TODO not hardcode the value
      currentThread = Thread.currentThread();
      Executable currentTask = initialTask;
      initialTask = null;
      try {
        while ((currentTask != null) || //there is a first task to execute - if so - execute it.
              (pooledThread && !isShutDown && (currentTask = (Executable) centralQueue.dequeue()) != null)) {
          try {
            setThreadInfo(currentTask.getThreadInfo());
            if (shmThread != null) {
              shmThread.store();
            }
            currentTask.run();
          } catch (java.lang.ThreadDeath td) {
              // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
              // Please do not remove this comment !
            SimpleLogger.trace(Severity.WARNING, location, 
            					"ASJ.krn_thd.000082", 
            					"Thread [{0}] was stopped while processing [{1}].",
            					this.getName(), currentTask.getRunnable());
            
            workerDone(this);
            break;
          } catch (OutOfMemoryError o) {
              // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
              // Please do not remove this comment !
            ProcessEnvironment.handleOOM(o);
            // if the vm is not restarted after OOM, remove the thread from the pool and decrease the active threads counter
            workerDone(this);
          } catch (Throwable tr) {
              // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
              // Please do not remove this comment !
            SimpleLogger.trace(Severity.ERROR, location,
            		LoggingUtilities.getDcNameByClassLoader(currentTask.getRunnable().getClass().getClassLoader()),
								null,
            		"ASJ.krn_thd.000001", 
            		"Execution of Runnable [{0}] throws the following Error [{1}].", tr, currentTask, tr.toString());
          } finally {
          	currentTask.tryToRelease(); //release if pooled - the task itself knows what to do.
          	currentTask = null; //must dereference!
            if (shmThread != null) {
              shmThread.clean();
            }
            monitoredThread.clearTaskIds();
            tInfo = null;
            
            if (AccessController.getContext().getDomainCombiner() != null) {
          		// the thread is created in "run as" code => has predefined DomainCombiner => cannot reuse this thread further since there is
          		// no way do clean the combiner
            	if (location.bePath()) {
            	  location.pathT("Thread ["+this.getName()+"] has non null DomainCombiner ["+AccessController.getContext().getDomainCombiner()+"] => cannot return the this thread in the pool since there is no way to clean the DomainCombiner => thread is left to die.");
            	}
          		workerDone(this);
          		break;
          	}             
            
            // set the original name of the thread back in case the task has changed it
            this.setName(myName);
          }
        }
       
//      } catch (InterruptedException e) {
//        // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
//        // Please do not remove this comment !
//        LoggingHelper.traceThrowable(Severity.DEBUG, location, "Thread interrupted while waiting for new task to process. Probably the system is shutting down.", e);
//        workerDone(this);
      } catch (ShutDownException rException) {
        // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
        // Please do not remove this comment !
        workerDone(this);
      } catch (OutOfMemoryError oom) {
        // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
        // Please do not remove this comment !
        ProcessEnvironment.handleOOM(oom);
      } catch (Throwable th) {
        // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
        // Please do not remove this comment !
        SystemEnvironment.STD_ERR.println("[CENTRAL EXECUTOR["+this.getClass().getName()+"]["+getName()+"]]: Unrecoverable error thrown while processing tasks:");
        th.printStackTrace(SystemEnvironment.STD_ERR);
        SimpleLogger.traceThrowable(Severity.ERROR, location, "ASJ.krn_thd.000002", "Unrecoverable error thrown while processing tasks", th);
      } finally {
        workerDone(this);
        if (shmThread != null) {
          shmThread.close();
          shmThread = null;
        }
      }
    }
    
    void setThreadInfo(ThreadInfo tInfo) {
      this.tInfo = tInfo; // keep a reference to the task to prevent returning in the pool
      this.tInfo.setPoolName(poolName);
      if (ThreadWrapper.isthreadMonitoringEnabled()) {
	      shmThread.setClassName(this.tInfo.getRunnable().getClass().getName());
	      shmThread.setPoolName(this.tInfo.getPoolName());
	      shmThread.setSubtaskName(this.tInfo.getTaskName());
	      shmThread.setTaskName(this.tInfo.getTaskName());
	      if (this.tInfo.getThreadName() == null) {
	        this.tInfo.setThreadName(myName);
	      }
	      this.setName(this.tInfo.getThreadName());
	      shmThread.setThreadName(this.tInfo.getThreadName()); 
      } else {
      	if (this.tInfo.getThreadName() == null) {
	        this.tInfo.setThreadName(myName);
	      }
	      this.setName(this.tInfo.getThreadName());
      }
    }
    
//  Delegation of monitored thread implementation

    public int getThreadState() {  return monitoredThread.getThreadState();  }
    public String getSubtaskName() { return monitoredThread.getSubtaskName(); }
    public String getTaskName() { return monitoredThread.getTaskName(); }
    public void popSubtask() { monitoredThread.popSubtask(); }
    public void popTask() {  monitoredThread.popTask(); }
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
  }
    

}
