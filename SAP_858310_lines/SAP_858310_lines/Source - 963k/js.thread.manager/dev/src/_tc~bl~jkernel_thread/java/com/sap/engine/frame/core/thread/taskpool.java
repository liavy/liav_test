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

/**
 * 
 *
 * @author Krasimir Semerdzhiev (krasimir.semerdzhiev@sap.com)
 * @version 6.30
 */
public interface TaskPool {

  public Task getTask();
  public void releaseTask(Task task);
  public void freeMemory();
  public int size();
  public int getLimit();
  public void setLimit(int limit);


}
