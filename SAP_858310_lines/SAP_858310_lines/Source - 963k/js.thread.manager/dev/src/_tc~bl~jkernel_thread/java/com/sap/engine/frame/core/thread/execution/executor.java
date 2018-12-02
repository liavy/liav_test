package com.sap.engine.frame.core.thread.execution;

import java.util.List;
import java.util.Collection;

/**
 * This interface allows the user to queue in it's thread pool runnables or collections of runnables
 * to be executed. If the runnables queued are more than one they will be executed in parallel in case 
 * there are free threads in the system.   
 * 
 * @author Elitsa Pancheva
 */
public interface Executor {

	/**
	 * This constant should be used when the component which creates Executor wants to specify
	 * the maximum concurrency rate allowed by the thread system.
	 */
	public static int MAX_CONCURRENCY_ALLOWED = -1111;
	
  /**
   * Rejection policy which is meant to handle the case when the maximum queue size is reached.
   * The behavior of this implementation is:
   * Executor will throw runtime exception RejectedExecutionException to the caller 
   * in case the maximum pool size is reached and no more tasks can be enqueued.
   */
  public static final byte REJECT_POLICY = 10;

  /**
   * Rejection policy which is meant to handle the case when the maximum queue size is reached.
   * The behavior of this implementation is:
   * Executor will block the caller thread until some tasks of the queue are processed and enqueue
   * operation is successful. This is the default behavior.
   */
  public static final byte WAIT_TO_QUEUE_POLICY = 20;

  /**
   * Rejection policy which is meant to handle the case when the maximum queue size is reached.
   * The behavior of this implementation is:
   * Executor will execute the caller task/tasks in the current thread instead of waiting to enqueue them in 
   * the queue.
   */
  public static final byte RUN_IN_CURRENT_THREAD_POLICY = 30;

  /**
   * Rejection policy which is meant to handle the case when the maximum queue size is reached.
   * The behavior of this implementation is:
   * Executor will execute the caller task in a new not pooled thread. This policy is not available when executing
   * colleations of Runnables.
   */
  public static final byte IMMEDIATE_START_POLICY = 40;

  /**
   * Executor state running means that the executor is able to accept and process new runnables.
   */
  public static final byte STATE_RUNNING = 10;

  /**
   * Executor state stopped means that the executor is already shut down and cannot be used anymore.
   */
  public static final byte STATE_STOPPED = 20;

  /**
   * Executor state stopping means that the executor is in process of stopping, no more runnables are 
   * accepted for execution in this state.
   */
  public static final byte STATE_STOPPING = 30;

  /**
   * Puts a custom Runnable instance in the thread queue for execution. 
   * If the queue is full the provided rejection policy at Executor creation is used.
   * 
   * @param runnable the user's Runnable instance to be executed
   */
  void execute(Runnable runnable);
  
  /**
   * Puts a custom Runnable instance in the thread queue for execution. 
   * If the queue is full the provided rejection policy at Executor creation is used.
   * <p>
   * The user has the opportunity to specify a name to be given to the thread which 
   * is going to process this runnable and a description of the task that the runnable 
   * execution will perform (for monitoring purposes).
   * 
   * @param runnable the user's Runnable instance to be executed
   * @param taskDescription a description of the task that the runnable execution 
   * will perform (for monitoring purposes)
   * @param threadName the name to be given to the thread while processing the user's runnable.
   */
  void execute(Runnable runnable, String taskDescription, String threadName);

  /**
   * Puts a custom Runnable instance in the thread queue for execution. 
   * If the queue is full the provided rejection policy at Executor creation is used.
   * <p>
   * This method creates and returns a monitor object which will provide the user with
   * information for the current execution state of the runnable as well as additional functionality
   * for waiting the runnable execution to be over or for processing the runnable in the user's thread.
   * 
   * @param runnable the user's Runnable instance to be executed
   * @return ExecutionMonitor instance providing information for the execution state of the runnable and
   * additional functionality for influencing the runnable execution.
   */
  ExecutionMonitor executeMonitored(Runnable runnable);
 
   /**
   * Puts a custom Runnable instance in the thread queue for execution. 
   * If the queue is full the provided rejection policy at Executor creation is used.
   * <p>
   * The user has the opportunity to specify a name to be given to the thread which 
   * is going to process this runnable and a description of the task that the runnable 
   * execution will perform (for monitoring purposes).
   * <p>
   * This method creates and returns a monitor object which will provide the user with
   * information for the current execution state of the runnable as well as additional functionality
   * for waiting the runnable execution to be over or for processing the runnable in the user's thread.
   * 
   * @param runnable the user's Runnable instance to be executed
   * @param taskDescription a description of the task that the runnable execution 
   * will perform (for monitoring purposes)
   * @param threadName the name to be given to the thread while processing the user's runnable.
   * @return ExecutionMonitor instance providing information for the execution state of the runnable and
   * additional functionality for influencing the runnable execution.
   */
  ExecutionMonitor executeMonitored(Runnable runnable, String taskDescription, String threadName);

  /**
   * Puts the Runnables instances of the specified collection in the thread queue for execution.
   * If the queue is full the provided rejection policy at Executor creation is used.
   * 
   * @param runnables the user's Runnable instances to be executed passed as collection 
   */
  void execute(Collection runnables);
  
  /**
   * Puts the Runnables instances of the specified collection in the thread queue for execution.
   * If the queue is full the provided rejection policy at Executor creation is used.
   * <p>
   * The user has the opportunity to specify a name template to be given to the threads which 
   * are going to process these runnables and a description of the task that the runnables 
   * execution will perform (for monitoring purposes).
   * 
   * @param runnables the user's Runnable instances to be executed passed as collection
   * @param tasksDescriptionTemplate a description of the task that the runnables execution 
   * will perform (for monitoring purposes). It will be used as template - the task description for each 
   * runnable will be formed by the task description + integer (runnable consecutive number)
   * @param threadsNameTemplate the name to be given to the threads while processing the user's runnables.
   * It will be used as template - the thread name for each runnable will be formed by the 
   * specified thread name + integer (runnable consecutive number)  
   */
  void execute(Collection tasks, String tasksDescriptionTemplate, String threadsNameTemplate);

  /**
   * Puts the Runnables instances of the specified collection in the thread queue for execution.
   * If the queue is full the provided rejection policy at Executor creation is used.
   * <p>
   * This method creates a monitor object for every runnable in the collection and returns 
   * these monitor instances as an array. The monitor object provides the user with
   * information for the current execution state of the runnable as well as additional functionality
   * for waiting the runnable execution to be over or for processing the runnable in the user's thread.
   *   
   * @param runnables the user's Runnable instances to be executed passed as collection 
   * @return ExecutionMonitor[] providing information for the execution state of each runnable and
   * additional functionality for influencing the runnables execution.
   */
  ExecutionMonitor[] executeMonitored(Collection tasks);
  
  /**
   * Puts the Runnables instances of the specified collection in the thread queue for execution.
   * If the queue is full the provided rejection policy at Executor creation is used.
   * <p>
   * The user has the opportunity to specify a name template to be given to the threads which 
   * are going to process these runnables and a description of the task that the runnables 
   * execution will perform (for monitoring purposes).
   * <p>
   * This method creates a monitor object for every runnable in the collection and returns 
   * these monitor instances as an array. The monitor object provides the user with
   * information for the current execution state of the runnable as well as additional functionality
   * for waiting the runnable execution to be over or for processing the runnable in the user's thread.
   *   
   * @param runnables the user's Runnable instances to be executed passed as collection
   * @param tasksDescriptionTemplate a description of the task that the runnables execution 
   * will perform (for monitoring purposes). It will be used as template - the task description for each 
   * runnable will be formed by the task description + integer (runnable consecutive number)
   * @param threadsNameTemplate the name to be given to the threads while processing the user's runnables.
   * It will be used as template - the thread name for each runnable will be formed by the 
   * specified thread name + integer (runnable consecutive number) 
   * @return ExecutionMonitor[] providing information for the execution state of each runnable and
   * additional functionality for influencing the runnables execution.
   */
  ExecutionMonitor[] executeMonitored(Collection tasks, String tasksDescriptionTemplate, String threadsNameTemplate);
 

  /**
   * Shuts this Executor down. No more new runnables can be added for execution to the queue.
   * The runnables that are already queued for execution will be processed whenever 
   * free threads are available.
   */
  void shutdown();

  /**
   * Shuts this Executor down. No more new runnables can be added for execution to the queue.
   * The runnables that are already queued will be removed from the queue and returned to the user as a list.
   * @return list of the runnables residing in the queue at the moment of forcedShutdown execution. 
   * These runnables are removed from the queue and won't be executed at all. 
   * If there are runnables which execution has begun no actions will be performed to terminate it, 
   * i.e. the execution will continue
   */
  List forceShutdown();

  /**
   * Returns the name of this executor instance specified at creation time. 
   * 
   * @return the name of the executor
   */
  String getName();

}
