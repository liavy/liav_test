/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.timeout;

/**
 * @author
 * @version 1.0.0
 */
public interface TimeoutListener {

  /**
   * After the TimeoutListener is registered this method is
   * invoked in dependence of the repeat time and delay time.
   */
  public void timeout();


  /**
   * If returns true <code>timeout()</code> will be called
   */
  public boolean check();

}

