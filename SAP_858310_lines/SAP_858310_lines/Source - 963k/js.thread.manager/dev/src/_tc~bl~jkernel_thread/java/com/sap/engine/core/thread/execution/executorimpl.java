package com.sap.engine.core.thread.execution;

import com.sap.engine.core.Names;
import com.sap.engine.core.thread.ThreadContextImpl;
import com.sap.engine.frame.core.thread.ThreadContext;
import com.sap.engine.frame.core.thread.exception.RejectedExecutionException;
import com.sap.engine.frame.core.thread.execution.ExecutionMonitor;
import com.sap.engine.frame.core.thread.execution.Executor;
import com.sap.engine.lib.util.PoolInstanceCreator;
import com.sap.engine.lib.util.PoolObjectWithCreator;
import com.sap.engine.lib.util.WaitQueue;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 *  
 * @author Elitsa Pancheva
 */
public class ExecutorImpl implements Executor {

  /**
   * Executable executor name. Usually it'll be the name of the using component + Executor added at the end.
   */
  private final String executorName;

  private final int maxParallelTasksCount;

  private final int maxQueuedTasksCount;

  private final RejectionPolicy rejectPolicy;
  
  private final boolean executeInCleanThreads;

  private final CentralExecutor centralQueueExecutor;

  private final Object localRunMonitor = new Object();

  private int scheduledForRun = 0;

  private final WaitQueue localQueue;

  private volatile byte executorState = STATE_RUNNING;
  
  private final static Location location = Location.getLocation(ExecutorImpl.class.getName(), Names.KERNEL_DC_NAME, Names.THREAD_MANAGER_CSN_COMPONENT);

  // task pool - used for reusing tasks, when they're not handed out to the callers.
  private static final PoolObjectWithCreator taskPool = new PoolObjectWithCreator(
      new PoolInstanceCreator() {
        public Object newInstance() {
          return new Executable();
        }
      }
  );

  public ExecutorImpl(String name, int maxParallelTasks, int maxQueuedTasksCount, byte policy, CentralExecutor centralQueueExecutor, boolean executeInCleanThreads) {
    //will be used for monitoring and naming of the threads, in case they don't have names.
    this.executorName = name;

    //maximum parallel tasks, allowed to be passed along to the central processing unit
    if (maxParallelTasks == Executor.MAX_CONCURRENCY_ALLOWED) {
    	this.maxParallelTasksCount = centralQueueExecutor.getMaxAllowedParallelismPerExecutor();
    	if (location.beInfo()) {
    	  String messageWithId = "Executor ["+name+"] will use the maximum concurrency rate allowed by Thread system ["+centralQueueExecutor.getMaxAllowedParallelismPerExecutor()+ "] which is " +centralQueueExecutor.getPercentageOfParallelismAllowed()+"% of the total thread pool capacity ["+
    	                         centralQueueExecutor.getMaxNumberOfPooledThreads()+"].";
        location.infoT(messageWithId);
    	}
    } else if (maxParallelTasks > centralQueueExecutor.getMaxAllowedParallelismPerExecutor()) {
    	this.maxParallelTasksCount = centralQueueExecutor.getMaxAllowedParallelismPerExecutor();
    	if (location.beWarning()) {
    		SimpleLogger.trace(Severity.WARNING, location, "ASJ.krn_thd.000012",
    							"Too big value [{0}] specified for maxConcurrency argument on Executor [{1}] creation. Allowed max concurrency rate for every Executor is not more than [{2}] % of the total thread pool capacity [{3}]. Thread Management will lower the max concurrency rate of this executor to [{4}]. Config tool can be used to increase the value of ApplicationThreadManager property [ExecutorPoolMaxSize] to reach higher quota for Executor concurrency. Switch the severity of this location to DEBUG to see the stack trace of the executor creation.",
    							maxParallelTasks, name, centralQueueExecutor.getPercentageOfParallelismAllowed(), centralQueueExecutor.getMaxNumberOfPooledThreads(), centralQueueExecutor.getMaxAllowedParallelismPerExecutor());
        if (location.beDebug()) {
          location.traceThrowableT(Severity.DEBUG, "Trace of the creation of executor with name ["+name+"]", new Exception("Trace of the creation of executor with name ["+name+"]"));  
        } 	
      }
    } else {
      this.maxParallelTasksCount = maxParallelTasks;
    }

    //define the local queue parameters
    this.maxQueuedTasksCount = maxQueuedTasksCount;
    this.localQueue = new WaitQueue(maxQueuedTasksCount);
    this.executeInCleanThreads = executeInCleanThreads;  
    //define the rejection policy - default is wait rejection policy
    switch (policy) {
      case REJECT_POLICY: {
        this.rejectPolicy = new RejectRejectionPolicy();
        break;
      }
      case RUN_IN_CURRENT_THREAD_POLICY: {
        this.rejectPolicy = new ProcessInCurrrentThreadRejectionPolicy();
        break;
      }
      case IMMEDIATE_START_POLICY: {
        this.rejectPolicy = new StartImmediatelyRejectionPolicy();
        break;
      }
      case WAIT_TO_QUEUE_POLICY: {
        this.rejectPolicy = new WaitRejectionPolicy();
        break;
      }
      default:
        throw new IllegalArgumentException("Invalid rejection policy parameter [" + policy + "]");
    }

    //get access to the central executor, where to push the tasks for execution
    this.centralQueueExecutor = centralQueueExecutor;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.lib.threadmgmt.api.execution.Executor#execute(java.lang.Runnable)
   */
  public void execute(Runnable runnable) {
    execute(runnable, null, null);
  }
  
  /* (non-Javadoc)
   * @see com.sap.engine.lib.threadmgmt.api.execution.Executor#execute(java.lang.Runnable, java.lang.String, java.lang.String)
   */
  public void execute(Runnable runnable, String taskName, String threadName) {
    amIRunning();
    startTaskInternal(wrapRunnable(runnable, threadName, taskName, true)); //wrap it into a reusable task and pass it along using a pooled object
  }

  /* (non-Javadoc)
  * @see com.sap.engine.lib.threadmgmt.api.execution.Executor#startTaskInternal(java.lang.Runnable)
  */
  public ExecutionMonitor executeMonitored(Runnable runnable) {
    return executeMonitored(runnable, null, null);
  }
  
  /* (non-Javadoc)
   * @see com.sap.engine.lib.threadmgmt.api.execution.Executor#executeMonitored(java.lang.Runnable, java.lang.String, java.lang.String)
   */
  public ExecutionMonitor executeMonitored(Runnable runnable, String taskName, String threadName) {
    amIRunning();
    Executable wrapper = wrapRunnable(runnable, threadName, taskName, false); //wrap it into a newly created task and pass it along - no pooling!
    startTaskInternal(wrapper);
    return wrapper; //monitor is returned back. It must not be reused later on!
  }

  /* (non-Javadoc)
  * @see com.sap.engine.lib.threadmgmt.api.execution.Executor#execute(java.lang.Runnable[])
  */
  public void execute(Collection runnables) {
    execute(runnables, null, null);
  }

  /* (non-Javadoc)
   * @see com.sap.engine.lib.threadmgmt.api.execution.Executor#execute(java.util.Collection, java.lang.String, java.lang.String)
   */
  public void execute(Collection runnables, String tasksNameTemplate, String threadsNameTemplate) {
    amIRunning();
    startTasksInternal(wrapRunnables(runnables, threadsNameTemplate, tasksNameTemplate, true)); //wrap them in reusable Executable objects
  }

  /**
   * Order is not guaranteed
   */
  public ExecutionMonitor[] executeMonitored(Collection runnables) {
    return executeMonitored(runnables, null, null);
  }
  
  /* (non-Javadoc)
   * @see com.sap.engine.lib.threadmgmt.api.execution.Executor#executeMonitored(java.util.Collection, java.lang.String, java.lang.String)
   */
  public ExecutionMonitor[] executeMonitored(Collection runnables, String tasksNameTemplate, String threadsNameTemplate) {
    amIRunning();
    List wrappers = wrapRunnables(runnables, threadsNameTemplate, tasksNameTemplate, false);
    ExecutionMonitor[] monitors = (ExecutionMonitor[]) wrappers.toArray(new ExecutionMonitor[runnables.size()]);
    // conversion has to be done in advance, because we're gonna modify the collection while adding it to the
    // queue within the startTasksInternal method. This a temporary structure to work with.
    startTasksInternal(wrappers);
    return reverseMonitors(monitors);
  }

  private void amIRunning() {
    if (executorState != STATE_RUNNING) {
      throw new RuntimeException("TaskExecutor cannot be used after shutdown is performed.");
    }
  }

  public String getName() {
    return executorName;
  }

  /**
   * This method is supposed to be used by the Executable wrappers only, to push along the next task for central
   * processing into the cenral executor. In case there is nothing to push - the local parallelizm counter
   * will be decreased and the thread will be freed.
   * Semantically - each task processed from the current queue, will "pull" one additional task
   * (if available) to the central queue.
   */
  void pushNextTask() {
    if (executorState != STATE_STOPPED) {
            
      Executable next = null;    // taka moje da ima w queue-to mente-ta i da dec-nem counter-a
      next = getNextGoodLocalTask();
                
      if (next == null) {
        synchronized (localRunMonitor) {
        	next = getNextGoodLocalTask();
        	if (next == null) {
            scheduledForRun--;
        	} else {
        		centralQueueExecutor.execute(next, false);
        	}
        }
      } else {
        centralQueueExecutor.execute(next, false);
      }
    }
  }
  private Executable getNextGoodLocalTask() {
    Executable next;
    do {
      next = (Executable)localQueue.poll();
      // must push in the central queue only the tasks with STATE_NOT_RUNNING
      // nust omit already canceled and finished tasks  
    } while (next != null && next.getState() != ExecutionMonitor.STATE_NOT_RUNNING);
    return next;
  }
  
  
  private void startTaskInternal(Executable wrapper) {
    if (!tryToPushCentrally(wrapper)) { //not pushed - we're still responsible to make it run
      if (!tryToPushLocally(wrapper)) {// can't push it locally - queue is full - reject will follow
        rejectPolicy.handle(wrapper);
      }
      
      //queue maintenance login from here on... 
      if (executorState == STATE_STOPPED) {
        return;
      }
      
      ensureMaxParalelismIsReached();
    }
  }
  private void ensureMaxParalelismIsReached() {
     Executable nextTask = null;
      //No matter whether we pushed it locally or not - we have to ensure that the
      // limit is still there, as one of the processing threads could have passed by in the meantime and
      // decreased the currently running count.
    
      synchronized (localRunMonitor) {
        while (scheduledForRun < maxParallelTasksCount && ((nextTask = getNextGoodLocalTask()) != null)) {
          scheduledForRun++;
          centralQueueExecutor.execute(nextTask, true);
        }
      }
  }

  private void startTasksInternal(List wrappers) {
    int count = 0;
    int remainingTasks = wrappers.size();

    if ((count = tryToPushCentrally(wrappers)) < remainingTasks) {  //push as much as possible centrally
      remainingTasks -= count;
      if ((count = tryToPushLocally(wrappers)) < remainingTasks) {
        remainingTasks -= count;
        rejectPolicy.handle(wrappers);
      }
      
      
      if (executorState == STATE_STOPPED) {
        return;
      }
      ensureMaxParalelismIsReached();
    }
  }

  private int tryToPushLocally(List wrappers) {
    int pushedCount = 0;
    while ((wrappers.size() > 0) && localQueue.offer(wrappers.get(0))) {
      pushedCount++;
      wrappers.remove(0);
    }
    return pushedCount;
  }

  private int tryToPushCentrally(List wrappers) {
    int pushedCount = 0;
    synchronized (localRunMonitor) {
      pushedCount = maxParallelTasksCount - scheduledForRun; // that's all we can push
      if (pushedCount == 0) {
        return 0; // if the limit is reached - no need of going further on.
      }
      int tasksToPush = wrappers.size();
      if (pushedCount >= tasksToPush) {
        pushedCount = tasksToPush;  //if it's enough - push them all
      }
      scheduledForRun += pushedCount;
    }

    Executable taskToPass;
    for (int i = 0; i < pushedCount; i++) {
      taskToPass = (Executable) wrappers.remove(0);
      centralQueueExecutor.execute(taskToPass, true);
    }
    return pushedCount;
  }


  private boolean tryToPushCentrally(Executable task) { //non blocking
    boolean result;
    synchronized (localRunMonitor) {
      if (result = (scheduledForRun < maxParallelTasksCount)) { //limit not reached
        scheduledForRun++;
      }
    }
    if (result) {
      centralQueueExecutor.execute(task, true);
    }
    return result;
  }

  private boolean tryToPushLocally(Executable task) { //non blocking
    return localQueue.offer(task); // if false - no space in the list
  }


  /**
   * The current executor will stop accepting new tasks, but will still flush all it's currently
   * pending tasks before going down. Sort of a soft shutdown.
   *
   * @see com.sap.engine.lib.threadmgmt.api.execution.Executor#shutdown()
   */
  public void shutdown() {
    executorState = STATE_STOPPING;
    //TODO implement waiting for the soft shutdown to finish.
  }

  /**
   * Immediately shutdown the current executor, returning back the list of currently pending tasks,
   * which has to be handled by the caller instead.
   */
  public List forceShutdown() {
    // set isShutdown to stop new comming task requests
    executorState = STATE_STOPPED;

    // cancel all currently pending tasks
    ArrayList leftTasks = new ArrayList(localQueue.size());
    while (localQueue.size() > 0) {
      Object o = localQueue.poll();
      if (o != null) {
        leftTasks.add(o);
      }
    } //TODO: currently the tasks, waiting to be pushed locally are not in the list. This has to be solved in the future. 

    return leftTasks;
  }

  private Executable wrapRunnable(Runnable myRunnable, String threadName, String taskName, boolean usePooledObject) {
    if (myRunnable == null) {
      throw new NullPointerException("Runnable to be executed can't be null");
    }

    Executable task = null;
    if (usePooledObject) { // if a pooled object is needed - get it from the pool
      synchronized (taskPool) {
        task = (Executable) taskPool.getObject();
      }
    } else {
      // in case a monitor will be returned - create a new one. We can't reuse an
      // object, which is handed out.
      task = new Executable();
    }
    task.initEnvironment(this, myRunnable, threadName, taskName, usePooledObject, executeInCleanThreads);

    return task;
  }

  private List wrapRunnables(Collection runnables, String threadsNameTemplate, String tasksNameTemplate, boolean poolWrappers) {
    if (runnables == null) {
      throw new NullPointerException("Collection of runnables must not be null");
    }
    int count = runnables.size();
    if (count == 0) {
      throw new IllegalArgumentException("Can't start empty set of tasks.");
    }

    Runnable r;
    ArrayList wrappers = new ArrayList(count);

    boolean setThreadNames = threadsNameTemplate != null;
    boolean setTaskNames = tasksNameTemplate != null;

    count = 0;
    for(Iterator iter = runnables.iterator();iter.hasNext();) {
      r = (Runnable) iter.next();
      wrappers.add(
          wrapRunnable(
              r,
              (setThreadNames)?threadsNameTemplate + "[" + count + "]":null,
              (setTaskNames)?tasksNameTemplate + "[" + count + "]":null,
              poolWrappers)
      );
      count+=1;
    }
    return wrappers;
  }

  void releaseTask(Executable task) {
    task.clean();
    synchronized(taskPool) {
      taskPool.releaseObject(task);
    }
  }

  /**
   * Get thread context of the current thread.
   *
   */
  final public ThreadContext getThreadContext() {
    return ThreadContextImpl.getThreadContext();
  }

// ----------- Rejection Policy implementations

  public class WaitRejectionPolicy implements RejectionPolicy {

    public void handle(Runnable r) {
      localQueue.enqueue(r);
    }

    public void handle(Collection r) {
      Iterator iter = r.iterator();
      while (iter.hasNext()) {
        handle((Runnable) iter.next());
      }
    }
  }

  public class RejectRejectionPolicy implements RejectionPolicy {

    public void handle(Runnable r) {
      throw new RejectedExecutionException("Maximum queue size " + maxQueuedTasksCount + " reached");
    }

    public void handle(Collection r) {
      throw new RejectedExecutionException("Unable to execute all passed tasks for execution. " + r.size() + " tasks are rejected.", r);
    }
  }

  public class StartImmediatelyRejectionPolicy implements RejectionPolicy {
    public void handle(Runnable r) {
      centralQueueExecutor.startInManagedThread((Executable) r, centralQueueExecutor.unmanagedThreadsGroup);
    }

    public void handle(Collection r) {
      Iterator iter = r.iterator();
      while (iter.hasNext()) {
        handle((Runnable) iter.next());
      }
    }
  }

  public class ProcessInCurrrentThreadRejectionPolicy implements RejectionPolicy {
    public void handle(Runnable r) {
      ((Executable)r).process();
    }

    public void handle(Collection r) {
      Iterator iter = r.iterator();
      while (iter.hasNext()) {
        handle((Runnable) iter.next());
      }
    }
  }
  
  
  private ExecutionMonitor[] reverseMonitors(ExecutionMonitor[] monitors) {
    int elementsToReverse = monitors.length/2;
    int endIndex = monitors.length - 1;
    ExecutionMonitor temp;
    for(int i = 0; i < elementsToReverse; i++) {
      temp = monitors[i];
      monitors[i] = monitors[endIndex - i];
      monitors[endIndex - i] = temp;
    }
    return monitors;
  }
  

}
