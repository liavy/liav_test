/*
 * Copyright (c) 2002 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.core.thread.impl5;

import com.sap.engine.frame.state.ManagementListener;
import com.sap.engine.core.thread.ThreadManagementInterface;

/**
 * Management Interface implementation, localised for Thread Manager usages
 *
 * @author Krasimir Semerdzhiev (krasimir.semerdzhiev@sap.com)
 * @version 6.30
 */
public class ThreadManagementInterfaceImpl implements ThreadManagementInterface {

  ThreadManagerImpl tm = null;

  public ThreadManagementInterfaceImpl(ThreadManagerImpl tm) {
    this.tm = tm;
  }

  public void registerManagementListener(ManagementListener managementListener) {
    //not needed at the moment
  }
  
  public int getUsageRate() {
    return getWaitingTasksUsageRate();
  }

  public int getMinThreadCount() {
    return tm.minThreadCount;
  }

  public int getMaxThreadCount() {
    return tm.getLimit();
  }

  public int getInitialThreadCount() {
    return tm.initialThreadCount;
  }

  public int getCurrentThreadCount() {
    return tm.size();
  }

  public int getActiveThreadsCount() {
    return tm.size() - tm.queue.getWaitingDequeueThreadsCount();
  }

  public int getThreadPoolUsageRate() {
    return (getActiveThreadsCount() * 100) / getMaxThreadCount();
  }
  
  public int getThreadPoolCapacityRate() {
    return (getCurrentThreadCount() * 100) / getMaxThreadCount();
  }
  
  public int getWaitingTasksCount() {
    return tm.queue.size();
  }
  
  public int getMaxWaitingTasksQueueSize() {
    return tm.queue.getLimit();
  }

  public int getWaitingTasksQueueOverflow() {
    return tm.queue.getWaitingEnqueueThreadsCount();
  }
  
  public int getWaitingTasksUsageRate() {
    return (getWaitingTasksCount() * 100) / getMaxWaitingTasksQueueSize();
  }

}
