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
package com.sap.engine.core.thread.impl3;

import com.sap.engine.lib.util.base.ListPool;
import com.sap.engine.lib.util.base.NextItem;
import com.sap.engine.frame.core.thread.TaskPool;
import com.sap.engine.frame.core.thread.Task;

/**
 * This is a pool for resusable objects. Simple stack logic implementation.
 * Thread Manager use it.
 *
 * @author Krasimir Semerdzhiev
 * @version 4.0
 */
final class ActionObjectPool extends ListPool implements TaskPool {


  /**
   * Constructor.
   *
   * @param   initialSize  initial size of the pool
   * @param   maxSize      maximal size of the pool
   */
  protected ActionObjectPool(int initialSize, int maxSize) {
    super(initialSize, maxSize);
  }

  /**
   * Get object from pool. If pool is empty, creates new one.
   *
   * @return     the object from pool
   */
  public Task getTask() {
    return (Task)super.getObject();
  }

  /**
   * Add object to pool.
   *
   * @param   action  the object added to the pool
   */
  public void releaseTask(Task action) {
    super.releaseObject(action);
  }

  /**
   * Empty pool. If pool length is bigger then it is set equals to minSize.
   *
   */
  public void freeMemory() {
    super.clear();
  }

  public int size() {
    return super.size();
  }

  public int getLimit() {
    return super.getLimit();
  }

  public void setLimit(int limit) {
    super.setLimit(limit);
  }

  public NextItem newInstance() {
    return new ActionObject(this);
  }

}

