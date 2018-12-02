package com.sap.engine.frame.core.thread.execution;

/**
 * This interface gives information for the state of the user's Runnable - if it's being 
 * already processed, processing at the moment or waiting for a free thread to come.
 * <p>
 * This interface provides the users with the opportunity to cancel the execution of their Runnable
 * when the runnable is still not running. 
 * <p>
 * The user thread may wait for the runnable to finish or execute it itself.
 * 
 * @author Elitsa Pancheva
 */
public interface ExecutionMonitor {

  /**
   * The runnable resides in the queue waiting to be processed.
   * At this point the runnable can be canceled.
   */
  public static final byte STATE_NOT_RUNNING = 5;
 
  /**
   * The runnable is being processed at the moment.
   * At this point the runnable cannot be canceled. 
   */
  public static final byte STATE_RUNNING = 10;
  
  /**
   * The runnable execution is complete.
   * At this point the runnable cannot be canceled.
   */
  public static final byte STATE_COMPLETE = 15; 
  
  /**
   * The runnable will not be executed at all.
   * The runnable has been canceled. 
   */
  public static final byte STATE_CANCELED = 20;
  
  /**
   * Returns the current state of the user's runnable.
   * 
   * @return current runnable state
   */
  public byte getState();
  
  /**
   * Returns the user's runnable.
   * 
   * @return runnable
   */
  public Runnable getRunnable();
  
  /**
   * Tries to cancel the runnable. The runnable can be canceled only if its current state is STATE_NOT_RUNNING.
   * In this case the state of the runnable will be set to SATE_CANCELED and "true" will be returned as a result.
   * If the current state of the runnable is different than STATE_NOT_RUNNING the runnable cannot be canceled so "false"
   * will be returned as a result.
   * 
   * @return true if the runnable execution is successfully canceled and false otherwise.
   */
  public boolean cancel();
  
  /**
   * The current thread will wait this runnable to finish its execution.
   * If the runnable is in STATE_NOT_RUNNING the current thread will execute it itself instead of just wait for the
   * other threads to finish their jobs.
   * If the runnable is in state STATE_RUNNING the current thread will block until the runnable execution is over. 
   * Once the runnable is executed the current thread will be returned to the caller.
   * 
   * If the runnable is in state STATE_COMPLETE or SATE_CANCELED the current thread will be 
   * returned to the caller immediately. 
   * 
   * Note: If the thread in which this join method is performed is a system one, the thread will just wait
   * for the execution of the runnable to finish and will not try to run it itself because the runnable
   * must be executed in an applcation thread and not in system one. 
   */
  public void join();
  
  /**
   * The current thread will try to execute the Runnable held by this monitor.
   * 
   * If the runnable is in STATE_NOT_RUNNING the current thread will execute it itself and
   * will return the control to the caller.
   * 
   * If the runnable is in state STATE_RUNNING, STATE_COMPLETE or SATE_CANCELED the current thread will be 
   * returned to the caller immediately. 
   * 
   * Note: If the thread in which this process method is performed is a system one, 
   * a RuntimeException will be thrown to the caller because the runnable
   * must be executed in an applcation thread and not in system one
   * 
   * @return true if the runnable has been processed no matter if it was processed by a thread 
   * from the pool or by the current thread, also true is returned if the task has been canceled, 
   * false is returned only in case the runnable is currently being executed by another thread. 
   * @throws RuntimeException in case the thread calling this method is a system one.
   */
  public boolean process();
  
}
