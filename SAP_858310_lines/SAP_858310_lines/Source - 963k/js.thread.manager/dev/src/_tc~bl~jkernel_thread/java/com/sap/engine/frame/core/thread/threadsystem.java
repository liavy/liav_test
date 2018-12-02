/**
 * Property of SAP AG, Walldorf
 * (c) Copyright SAP AG, Walldorf, 2000-2002.
 * All rights reserved.
 */
package com.sap.engine.frame.core.thread;

import com.sap.engine.frame.core.thread.execution.Executor;

/**
 * This is connection to internal thread system. There are many reasons that a
 * service has to use this system instead of directly creating java threads.
 * Using this system it gets performance, thread related data consistency,
 * thread resource loadbalancing and thread resource protection.
 *
 * @author Jasen Minov
 * @version 6.30
 */
public interface ThreadSystem {
	
  /**
   * It starts the <source> Runnable <source> object into a new thread from the
   * internal thread pool. You can't rely on priority, internal system can ignore it in
   * some situations. The thread context (the context objects) of the parent thread
   * is not copied to the new thread!
   *
   * @param   thread - runnable object that is going to be executed
   * @param   instantly - if no free thread is found in the object pool -
   *  with instantly set to false the runnable is added to a waiting queue.
   *  If instantly is true a new thread will be created
   * (this is only permited for deadlock-prevention purposes!).
   */
  public void startCleanThread(Runnable thread, boolean instantly);


  /**
   * It starts the <source> Task <source> object into a new thread from the
   * internal thread pool. You can't rely on priority, internal system can ignore it in
   * some situations. The thread context (the context objects) of the parent thread
   * is not copied to the new thread!
   *
   * @param   task - task object that is going to be executed
   * @param   instantly - if no free thread is found in the object pool -
   *  with instantly set to false the runnable is added to a waiting queue.
   *  If instantly is true a new thread will be created
   * (this is only permited for deadlock-prevention purposes!).
   */
  public void startCleanThread(Task task, boolean instantly);


  /**
   * It starts the <source> Runnable <source> object into a new thread from the
   * internal thread pool. You can't rely on priority, internal system can ignore it in
   * some situations. The thread context (the context objects) of the parent thread
   * is not copied to the new thread!
   *
   * @param   thread - runnable object that is going to be executed
   * @param   system - indicate the type of thread that has to be used for
   * execution of the runnable object (true - System thread (internal server activity),
   * false - Application thread (executing client code, or running to serve client request).
   * @param   instantly - if no free thread is found in the object pool -
   *  with instantly set to false the runnable is added to a waiting queue.
   *  If instantly is true a new thread will be created
   * (this is only permited for deadlock-prevention purposes!).
   */
  public void startCleanThread(Runnable thread, boolean system, boolean instantly);


  /**
   * It starts the <source> Task <source> object into a new thread from the
   * internal thread pool. You can't rely on priority, internal system can ignore it in
   * some situations. The thread context (the context objects) of the parent thread
   * is not copied to the new thread!
   *
   * @param   task - task object that is going to be executed
   * @param   system - indicate the type of thread that has to be used for
   * execution of the runnable object (true - System thread (internal server activity),
   * false - Application thread (executing client code, or running to serve client request).
   * @param   instantly - if no free thread is found in the object pool -
   *  with instantly set to false the runnable is added to a waiting queue.
   *  If instantly is true a new thread will be created
   * (this is only permited for deadlock-prevention purposes!).
   */
  public void startCleanThread(Task task, boolean system, boolean instantly);
  
  
  /**
   * It starts the <source> Runnable <source> object into a new thread from the
   * internal thread pool. If there is no free thread the object is added to a
   * waiting queue. The thread context of the parent thread is copied to the
   * new thread using child method of Context objects. This is done in the parent
   * thread.
   *
   * @param   thread - runnable object that is going to be executed
   * @param   system - indicate the type of thread that has to be used for
   * execution of the runnable object (true - System thread (internal server activity),
   * false - Application thread (executing client code, or running to serve client request).
   *
   */
  public void startThread(Runnable thread, boolean system);

  
  public void startThread(Runnable thread, String taskName, String threadName, boolean system);

  
  /**
   * It starts the <source> Runnable <source> object into a new thread from the
   * internal thread pool. If there is no free thread the object is added to a
   * waiting queue. You can't rely on priority, internal system can ignore it in
   * some situations. The thread context of the parent thread is copied to the
   * new thread using child method of Context objects. This is done in the parent
   * thread.
   *
   * @param   thread - runnable object that is going to be executed
   * @param   system - indicate the type of thread that has to be used for
   * execution of the runnable object (true - System thread (internal server activity),
   * false - Application thread (executing client code, or running to serve client request).
   * @param   instantly - if no free thread is found in the object pool - a new thread
   * will be started. Usage of this method is only permited for deadlock-prevention purposes!
   */
  public void startThread(Runnable thread, boolean system, boolean instantly);

  
  public void startThread(Runnable thread, String taskName, String threadName, boolean system, boolean instantly);

  
  /**
   * It starts the <source> Task <source> object into a new thread from the
   * internal application thread pool. If there is no free thread the object is
   * added to a waiting queue. You can't rely on priority, internal system will
   * ignore it! The thread context of the parent thread is copied to the
   * new thread using child method of Context objects. This is done in the parent
   * thread. This method always starts an Application thread!
   *
   * @param   task - Task object that is going to be executed
   * @param   instantly - indicate if the method will wait if no free threads
   * are available or a new one will be created. Should be true only for critical tasks.
   */
  public void startTask(Task task, boolean instantly);

  
  public void startThread(Task task, String taskName, String threadName, boolean instantly);

  
  /**
   * It starts the <source> Task <source> object into a new thread from the
   * internal application thread pool. If there is no free thread the object is
   * added to a waiting queue. You can't rely on priority, internal system will
   * ignore it! The thread context of the parent thread is copied to the
   * new thread using child method of Context objects. This is done in the parent
   * thread. This method always starts an Application thread!
   *
   * @param   task - Task object that is going to be executed
   * @param   timeout - (in milliseconds) the thread will be started within this timeout.
   * If possible, reusing a thread from the internal thread pool. If not (no free thread
   * for timeout time) a new thread will be created.
   */
  public void startTask(Task task, long timeout);

  
  public void startThread(Task task, String taskName, String threadName, long timeout);

  
  /**
   * Get thread context of the current thread.
   */
  public ThreadContext getThreadContext();
  
  
  /**
   * Register object that is connected to a thread. Depends from which thread
   * you access it, the object has different instances. You can access the
   * object by name. Returns -1 if working on client side and value >= 0 if
   * working on cluster node. Usually performed at service start.
   * 
   * @param  name    Name of the ContextObject
   * @param  object  ContextObject to be registered
   * @return ID of the registered object
   */ 
  public int registerContextObject(String name, ContextObject object);

  
  /**
   * Get the ID of a Context Object from Application Thread Context
   * @param name Name of the object to get the id of...
   * @return ID > 0 if the object is registered, or -1 if no such object exists.
   */
  public int getContextObjectId(String name);

  
  /**
   * Unregister Context Object - usually performed at service stop.
   *
   * @param name ID of the object to be unregistered.
   */
  public void unregisterContextObject(String name);
  
  /***********************************************************************/
  /* Thread Executor Methods */
  
  /**
   * Creates a new Executor instance with specified name, maximum parallel concurency and maximum queue size.
   * Default RejectionPolicy WAIT_TO_QUEUE_POLICY is used in case the user tries to queue for execution more 
   * runnables then maxQueueSize value.
   * 
   * Depending on the property [PercentageOfParallelismAllowed] thread system allows each executor to
	 * have not more than a certain percentage of concurrently executing threads with regards to the
	 * total number of threads in the central thread pool.
	 * If the component requests more than this percentage thread system traces WARNING and gives the
	 * component's executor the allowed maximum.
	 * If the component wants to specify exactly the max allowed concurrency it should use  
	 * Executor.MAX_CONCURRENCY_ALLOWED constant.
   * 
   * @see Executor#MAX_CONCURRENCY_ALLOWED
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
   * Depending on the property [PercentageOfParallelismAllowed] thread system allows each executor to
	 * have not more than a certain percentage of concurrently executing threads with regards to the
	 * total number of threads in the central thread pool.
	 * If the component requests more than this percentage thread system traces WARNING and gives the
	 * component's executor the allowed maximum.
	 * If the component wants to specify exactly the max allowed concurrency it should use  
	 * Executor.MAX_CONCURRENCY_ALLOWED constant.
   * 
   * @see Executor#MAX_CONCURRENCY_ALLOWED
   * 
   * @param name a custom name for the Executor instance. Cannot be null or empty String - IllegalArgumentException is 
   * thrown in this case.
   * @param maxConcurrency the maximum number of parallel running threads to execute the queued runnables of this Executor
   * Value must be greater then 0 otherwise IllegalArgumentException is thrown.
   * @param maxQueueSize the maximum number of runnables to be queued for execution.
   * @param rejectionPolicy the preferred rejection policy to use in case the user tries to queue for execution more 
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
   * Depending on the property [PercentageOfParallelismAllowed] thread system allows each executor to
	 * have not more than a certain percentage of concurrently executing threads with regards to the
	 * total number of threads in the central thread pool.
	 * If the component requests more than this percentage thread system traces WARNING and gives the
	 * component's executor the allowed maximum.
	 * If the component wants to specify exactly the max allowed concurrency it should use  
	 * Executor.MAX_CONCURRENCY_ALLOWED constant.
   * 
   * @see Executor#MAX_CONCURRENCY_ALLOWED
   * 
   * @param name a custom name for the Executor instance. Cannot be null or empty String - IllegalArgumentException is 
   * thrown in this case.
   * @param maxConcurrency the maximum number of parallel running threads to execute the queued runnables of this Executor
   * Value must be greater then 0 otherwise IllegalArgumentException is thrown.
   * @param maxQueueSize the maximum number of runnables to be queued for execution.
   * @param rejectionPolicy the preferred rejection policy to use in case the user tries to queue for execution more 
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
   * Thread Pool. The thread will be start immediately and won't be reused after 
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
   * Thread Pool. The thread will be start immediately and won't be reused after 
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

