package com.sap.engine.frame.core.thread.exception;

import java.util.Collection;

/**
 * Exception thrown by the Executor.REJECT_POLICY in case the maximum pool size is reached and
 * no more tasks can be enqueued.
 * 
 * @author Elitsa Pancheva
 */
public class RejectedExecutionException extends RuntimeException {
  private Collection rejectedTasks = null;

  public RejectedExecutionException(String message) {
    super(message);
  }

  public RejectedExecutionException(String message, Throwable cause) {
    super(message, cause);
  }

  public RejectedExecutionException(String message, Collection rejectedTasks) {
    this(message);
    this.rejectedTasks = rejectedTasks;
  }

  public Collection getRejectedTasks() {
    return rejectedTasks;
  }
}
