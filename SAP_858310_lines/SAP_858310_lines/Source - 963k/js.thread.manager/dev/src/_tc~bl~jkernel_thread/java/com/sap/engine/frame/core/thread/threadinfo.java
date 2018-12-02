package com.sap.engine.frame.core.thread;

/**
 * @author Petev, Petio, i024139
 */
public class ThreadInfo {

  private Runnable runnable = null;
  private String className;
  private String poolName;
  private String subtaskName;
  private String taskName;
  private String threadName;
  private String userName;

  public String getClassName() {
    return className;
  }

  public String getPoolName() {
    return poolName;
  }

  public String getSubtaskName() {
    return subtaskName;
  }

  public String getTaskName() {
    return taskName;
  }

  public String getThreadName() {
    return threadName;
  }

  public String getUserName() {
    return userName;
  }

  public Runnable getRunnable() {
    return runnable;
  }

  public void setClassName(String s) {
    className = s;
  }

  public void setPoolName(String s) {
    poolName = s;
  }

  public void setSubtaskName(String s) {
    subtaskName = s;
  }

  public void setTaskName(String s) {
    taskName = s;
  }

  public void setThreadName(String s) {
    threadName = s;
  }

  public void setUserName(String s) {
    userName = s;
  }

  public void setRunnable(Runnable r) {
    runnable = r;
  }

}
