/*
 * Copyright (c) 1999 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.core.thread;

import com.sap.engine.core.Manager;
import com.sap.engine.frame.core.thread.ThreadContext;
import com.sap.engine.frame.core.thread.ContextObject;
import com.sap.engine.frame.core.thread.Task;

/**
 * Public interface ThreadManager. This is the base interface for all thread managers.
 * The function of thread managers is to control the count of currently running threads.
 *
 * @author Ivan Mitov, Jasen Minov, Kaloyan Raev
 * @version 2.0
 */
public interface ThreadManager
  extends Manager {

  public static final int MIN_PRIORITY = 1;
  public static final int DEF_PRIORITY = 5;
  public static final int MAX_PRIORITY = 10;
  public static final int OUT_OF_MEMORY_EXIT_CODE = 666;

  public void startThread(Runnable thread);
  
  public void startThread(Runnable thread, boolean instantly);

  public void startThread(Runnable thread, String taskName, String threadName);

  public void startThread(Runnable thread, String taskName, String threadName, boolean instantly);

  public void startThread(Task task, boolean instantly);

  public void startThread(Task task, long timeout);

  public void startThread(Task task, String taskName, String threadName, boolean instantly);

  public void startThread(Task task, String taskName, String threadName, long timeout);

  public void startCleanThread(Runnable thread, boolean instantly);

  public void startCleanThread(Task task, boolean instantly);

  public ThreadContext getThreadContext();
  
  public int registerContextObject(String name, ContextObject object);

  public int getContextObjectId(String name);

  void unregisterContextObject(String name);

}
