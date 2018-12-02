package com.sap.engine.core.thread.execution;

import com.sap.engine.frame.core.thread.execution.Executor;
import com.sap.engine.lib.util.ConcurrentHashMapObjectObject;


public class ExecutorFactoryImpl extends ExecutorFactory {
  
  private static CentralExecutor centralQueueExecutor;
  /**
   *  boolean flag showing whether an instance of ExecutorFactory has been created already or not
   */
  private static boolean initialized = false;
  
  private ConcurrentHashMapObjectObject executors = new ConcurrentHashMapObjectObject();

  public static synchronized void init(CentralExecutor executor) {
    if (!initialized) {
      centralQueueExecutor = executor;
      instance = new ExecutorFactoryImpl();
      initialized = true;
    }
  }
  
  private void checkParameters(String name, int maxParallelTasks, int maxQueuedTasksCount) {
    if (name == null || "".equals(name)) {
      throw new IllegalArgumentException("Invalid name parameter [" + name + "]");
    }  
    
    if (maxParallelTasks <0 && maxParallelTasks != Executor.MAX_CONCURRENCY_ALLOWED) {
      throw new IllegalArgumentException("Invalid maxParallelTasks parameter [" + maxParallelTasks + "]");
    }
    
    if (maxQueuedTasksCount <0) {
      throw new IllegalArgumentException("Invalid maxQueuedTasksCount parameter [" + maxParallelTasks + "]");
    }    
  }
  
  /* (non-Javadoc)
   * @see com.sap.engine.frame.core.thread.execution.ExecutorFactory#createExecutor(java.lang.String, int, int)
   */
  public Executor createExecutor(String name, int maxParallelTasks, int maxQueuedTasksCount) {
    return this.createExecutor(name, maxParallelTasks, maxQueuedTasksCount, Executor.WAIT_TO_QUEUE_POLICY);
  }
  
  /* (non-Javadoc)
   * @see com.sap.engine.frame.core.thread.execution.ExecutorFactory#createExecutor(java.lang.String, int, int, byte)
   */ 
  public Executor createExecutor(String name, int maxParallelTasks, int maxQueuedTasksCount, byte policy) {
    checkParameters(name, maxParallelTasks, maxQueuedTasksCount);
    ExecutorImpl executor = new ExecutorImpl(name, maxParallelTasks, maxQueuedTasksCount, policy, centralQueueExecutor, false);
    executors.put(formExecutorName(name, executor), executor);
    return executor;
  }
  
  /* (non-Javadoc)
   * @see com.sap.engine.frame.core.thread.execution.ExecutorFactory#createCleanThreadExecutor(java.lang.String, int, int, byte)
   */ 
  public Executor createCleanThreadExecutor(String name, int maxParallelTasks, int maxQueuedTasksCount, byte policy) {
    checkParameters(name, maxParallelTasks, maxQueuedTasksCount);
    ExecutorImpl executor = new ExecutorImpl(name, maxParallelTasks, maxQueuedTasksCount, policy, centralQueueExecutor, true);
    executors.put(formExecutorName(name, executor), executor);
    return executor;
  }
  
  private String formExecutorName(String customName, Executor instance) {
    return customName + "$" + instance.hashCode();
  }

  /* (non-Javadoc)
   * @see com.sap.engine.frame.core.thread.execution.ExecutorFactory#destroyExecutor(com.sap.engine.frame.core.thread.execution.Executor)
   */
  public void destroyExecutor(Executor instance) {
    if (instance != null) {
  	  String name = instance.getName();
      executors.remove(formExecutorName(name, instance));
      // TODO Remove it from monitoring
    } else {
    	throw new NullPointerException("Executor instance parameter in destroyExecutor() method cannot be null.");
    }
  }
  
  public static synchronized void shutdown() {
    if (initialized) {
      if (centralQueueExecutor != null) {
        centralQueueExecutor.forceShutdown();
        centralQueueExecutor = null;
      }
      initialized = false;
    }
  }

  /* (non-Javadoc)
   * @see com.sap.engine.frame.core.thread.execution.ExecutorFactory#executeInDedicatedThread(java.lang.Runnable, java.lang.String)
   */
  public void executeInDedicatedThread(Runnable runnable, String threadName) {
    this.executeInDedicatedThread(runnable, null, threadName);   
  }

  /* (non-Javadoc)
   * @see com.sap.engine.frame.core.thread.execution.ExecutorFactory#executeInDedicatedThread(java.lang.Runnable, java.lang.String, java.lang.String)
   */
  public void executeInDedicatedThread(Runnable runnable, String taskDescription, String threadName) {
    if (threadName == null || "".equals(threadName)) {
      throw new IllegalArgumentException("Invalid thread name [" + threadName + "]");
    }
    centralQueueExecutor.executeInDedicatedThread(runnable, taskDescription, threadName);    
  }

    
}
