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
package com.sap.engine.core.thread;

import com.sap.engine.frame.state.ManagementInterface;

/**
 * Thread Manager's Management Object.
 *
 * @author Kaloyan Raev
 * @version 7.10
 */
public interface ThreadManagementInterface extends ManagementInterface {
  
  // public metrics
  public int getUsageRate();

  // private metrics
	public int getMinThreadCount();
  public int getMaxThreadCount();
  public int getInitialThreadCount();
  public int getCurrentThreadCount();
  public int getActiveThreadsCount();
  public int getThreadPoolUsageRate();
  public int getThreadPoolCapacityRate();
  public int getWaitingTasksCount();
  public int getMaxWaitingTasksQueueSize();
  public int getWaitingTasksQueueOverflow();
  public int getWaitingTasksUsageRate();

}
