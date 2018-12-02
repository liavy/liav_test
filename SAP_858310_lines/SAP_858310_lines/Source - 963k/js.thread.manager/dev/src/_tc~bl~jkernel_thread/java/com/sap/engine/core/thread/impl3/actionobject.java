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

import com.sap.engine.frame.core.thread.Task;
import com.sap.engine.frame.core.thread.TaskPool;
import java.security.PrivilegedAction;

/**
 * This is object used from Thread Manager. It helps us to develop coping
 * of current security thread context to new thread.
 *
 * @author Krasimir Semerdzhiev
 * @version 4.0
 */
final class ActionObject extends Task implements PrivilegedAction {

  private static TaskPool pool;

  protected ActionObject(TaskPool pool) {
    ActionObject.pool = pool;
  }

  /**
   * Implements run method of interface PrivilegedAction.
   *
   */
  public Object run() {
    work.run();
    return null;
  }

  /**
   * Returns the values of attributes associated with this node.
   *
   * @return   the values of attributes associated with this node.
   */
  public String[] getAttributes() {
    return new String[0];
  }

  public void release() {
    super.release();
    pool.releaseTask(this);
  }

}

