/*
 * Copyright (c) 2003 by SAP AG.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.frame.core.thread;

import com.sap.engine.lib.util.base.LinearItemAdapter;

import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.AccessControlContext;
import java.security.PrivilegedAction;

/**
 *
 *
 * @author Krasimir Semerdzhiev, Elitsa Pancheva
 * @version 7.10
 */
public abstract class Task extends LinearItemAdapter implements PrivilegedAction  {

  private com.sap.engine.frame.core.thread.ThreadInfo tInfo = new ThreadInfo();
  
  private ContextData ctxData = null;
  
  protected Runnable work = null;
  
  boolean active = false;

  public Task() {
  }

  public void release() {
    work = null;
    ctxData.empty();
    tInfo.setClassName(null);
    tInfo.setPoolName(null);
    tInfo.setRunnable(null);
    tInfo.setSubtaskName(null);
    tInfo.setTaskName(null);
    tInfo.setThreadName(null);
    tInfo.setUserName(null);
  }
 
  public void setContextData(ContextData ctxData) {
    this.ctxData = ctxData;
  }
  
  public ContextData getContextData() {
    return ctxData;
  }

  public void init(Runnable work, String taskName, String threadName, String poolName) {
    this.work = work;
    tInfo.setRunnable(work);
    tInfo.setClassName(work.getClass().getName());
    tInfo.setPoolName(poolName);
    // tInfo.setSubtaskName(null);
    tInfo.setTaskName(taskName);
    tInfo.setThreadName(threadName);
    // tInfo.setUserName(null);
    active = true;
  }

  public Object run() {
    work.run();
    return null;
  }

  public boolean isParentSystem() {
    return ctxData.isSystem();
  }

  public ClassLoader getContextClassLoader() {
    return ctxData.getContextClassLoader();
  }

  public AccessControlContext getContext() {
    return ctxData.getAccessControlContext();
  }

  public void disableTask() {
    active = false;
  }

  public String toString() {
    if (work != null) {
      return getClass().getName() + " - Processing Task [classname: " + work.getClass().getName() + " | toString: " + work.toString() + "] with classloader ["+ctxData.getContextClassLoader()+"]";  
    } else {
      return getClass().getName() + " - No task is currently processed";
    }
  }

  public ThreadInfo getThreadInfo() {
    return tInfo;
  }
  
	private void writeObject(ObjectOutputStream oos) throws NotSerializableException {
	  throw new NotSerializableException(this.getClass().getName());
	}

	
	private void readObject(ObjectInputStream oos) throws NotSerializableException {
	  throw new NotSerializableException(this.getClass().getName());
	}
}
