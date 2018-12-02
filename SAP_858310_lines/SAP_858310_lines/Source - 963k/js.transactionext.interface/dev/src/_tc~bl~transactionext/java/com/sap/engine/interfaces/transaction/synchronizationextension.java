/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */

package com.sap.engine.interfaces.transaction;

import javax.transaction.Synchronization;

/**
 * Interface for synchronization used to notify before rollback
 *
 * @author Iliyan Nenov, ilian.nenov@sap.com
 * @version SAP J2EE Engine 6.30
 */
public interface SynchronizationExtension extends Synchronization {

  /**
   * Called when the transaction is rolled back if the Status of the Transaction is PREPARING
   */
  public void beforeRollback();
}
