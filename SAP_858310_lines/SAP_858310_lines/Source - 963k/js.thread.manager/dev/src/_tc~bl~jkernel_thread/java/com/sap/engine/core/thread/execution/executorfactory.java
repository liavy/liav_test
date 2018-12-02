package com.sap.engine.core.thread.execution;

import com.sap.engine.frame.core.thread.execution.Executor;

/**
 * A factory class for thread queues creation.
 * The starting point for using the thread management library.
 * 
 * @author Elitsa Pancheva
 */
public abstract class ExecutorFactory {
  
  protected static ExecutorFactory instance = null;

  /**
   * Get an instance of the ExecutorFactory
   * @return instance of the ExecutorFactory implementation 
   */
  public static ExecutorFactory getInstance() {
    if (instance == null) {
      throw new RuntimeException("ExecutorFactory is not initialized.");
    }
    return instance;
  }

  /**
   * Creates a new Executor instance with specified name, maximum parallel concurency and maximum queue size.
   * Default RejectionPolicy WAIT_TO_QUEUE_POLICY is used in case the user tries to queue for execution more 
   * runnables then maxQueueSize value.
   * 
   * @param name a custom name for the Executor instance. Cannot be null or empty String - IllegalArgumentException is 
   * thrown in this case.
   * @param maxConcurrency the maximum number of parallel running threads to execute the queued runnables of this Executor
   * Value must be greater then 0 otherwise IllegalArgumentException is thrown.
   * @param maxQueueSize the maximum number of runnables to be queued for execution.
   * @return a new instance of Executor
   * @throws IllegalArgumentException in case the arguments of the method are not valid.
   */
  public abstract Executor createExecutor(String name, int maxConcurrency, int maxQueueSize);

  /**
   * Creates a new Executor instance with specified name, maximum parallel concurency, maximum queue size and
   * RejectionPolicy.
   * 
   * @see Executor#REJECT_POLICY
   * @see Executor#WAIT_TO_QUEUE_POLICY 
   * @see Executor#RUN_IN_CURRENT_THREAD_POLICY 
   * @see Executor#IMMEDIATE_START_POLICY  
   * 
   * @param name a custom name for the Executor instance. Cannot be null or empty String - IllegalArgumentException is 
   * thrown in this case.
   * @param maxConcurrency the maximum number of parallel running threads to execute the queued runnables of this Executor
   * Value must be greater then 0 otherwise IllegalArgumentException is thrown.
   * @param maxQueueSize the maximum number of runnables to be queued for execution.
   * @param rejectionPolicy the preffered rejection policy to use in case the user tries to queue for execution more 
   * runnables then maxQueueSize value. If the value is not one of the constants specified in the Executor interface
   * IllegalArgumentException is thrown.
   * @return a new instance of Executor
   * @throws IllegalArgumentException in case the arguments of the method are not valid.
   */
  public abstract Executor createExecutor(String name, int maxConcurrency, int maxQueueSize, byte rejectionPolicy);
  
  /**
   * Creates a new Executor instance with specified name, maximum parallel concurency, maximum queue size and
   * RejectionPolicy. The difference is that this Executor executes the provided custom Runnables in a clean
   * thread, i.e. ThreadContext is not inherited from its parent => no user/session/transaction/app context/etc. is propagated.
   *  
   * @see Executor#REJECT_POLICY
   * @see Executor#WAIT_TO_QUEUE_POLICY 
   * @see Executor#RUN_IN_CURRENT_THREAD_POLICY 
   * @see Executor#IMMEDIATE_START_POLICY  
   * 
   * @param name a custom name for the Executor instance. Cannot be null or empty String - IllegalArgumentException is 
   * thrown in this case.
   * @param maxConcurrency the maximum number of parallel running threads to execute the queued runnables of this Executor
   * Value must be greater then 0 otherwise IllegalArgumentException is thrown.
   * @param maxQueueSize the maximum number of runnables to be queued for execution.
   * @param rejectionPolicy the preffered rejection policy to use in case the user tries to queue for execution more 
   * runnables then maxQueueSize value. If the value is not one of the constants specified in the Executor interface
   * IllegalArgumentException is thrown.
   * @return a new instance of Executor
   * @throws IllegalArgumentException in case the arguments of the method are not valid.
   */
  public abstract Executor createCleanThreadExecutor(String name, int maxConcurrency, int maxQueueSize, byte rejectionPolicy);
  
  /**
   * Destroys the specified Executor instance. This method should be called when the
   * user does not need to use the Executor instance anymore in order release resources and 
   * remove it from the monitoring.
   * 
   * @param instance the Executor instance to be destroyed. Cannot be null - 
   * a NullPointerException will be thrown in this case.
   */
  public abstract void destroyExecutor(Executor instance);
    
  /**
   * This method is meant for users (protocol implementations) that want to start 
   * a lifetime (until the user is available in the engine) execution of a Runnable in an 
   * engine thread instead of a new java.lang.Thread in order to benefit from the 
   * exception handling and monitoring implemented in the engine threads.
   * The thread will be dedicated for this user and will not be got from the Application
   * Thread Pool. The thread will be start immediatelly and won't be reused after 
   * the custom Runnable execution is over.
   * 
   * @param runnable the user's Runnable instance to be executed
   * @param threadName the name to be given to the thread while processing the user's runnable. 
   * Cannot be null - a NullPointerException is thrown in this case.
   */
  public abstract void executeInDedicatedThread(Runnable runnable, String threadName);
  
  /**
   * This method is meant for users (protocol implementations) that want to start 
   * a lifetime (until the user is available in the engine) execution of a Runnable in an 
   * engine thread instead of a new java.lang.Thread in order to benefit from the 
   * exception handling and monitoring implemented in the engine threads.
   * The thread will be dedicated for this user and will not be got from the Application
   * Thread Pool. The thread will be start immediatelly and won't be reused after 
   * the custom Runnable execution is over.
   * 
   * @param runnable the user's Runnable instance to be executed
   * @param taskDescription a description of the task that the runnable execution 
   * will perform (for monitoring purposes)
   * @param threadName the name to be given to the thread while processing the user's runnable. 
   * Cannot be null - a NullPointerException is thrown in this case.
   */
  public abstract void executeInDedicatedThread(Runnable runnable, String taskDescription, String threadName);

}
