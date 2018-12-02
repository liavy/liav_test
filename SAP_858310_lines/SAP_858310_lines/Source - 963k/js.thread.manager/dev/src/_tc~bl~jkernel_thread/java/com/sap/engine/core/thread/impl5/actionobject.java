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
package com.sap.engine.core.thread.impl5;

import com.sap.engine.frame.core.thread.Task;
import com.sap.engine.frame.core.thread.TaskPool;

/**
 * This is object used from Thread Manager. It helps us to develop coping
 * of current security thread context to new thread.
 *
 * @author Krasimir Semerdzhiev
 * @version 4.0
 */
final class ActionObject extends Task {

  private static TaskPool pool;

  protected ActionObject() {
  }

  public void release() {
    super.release();
    pool.releaseTask(this);
  }
  
  public static void setPool(TaskPool pool) {
    ActionObject.pool = pool;    
  }

}

