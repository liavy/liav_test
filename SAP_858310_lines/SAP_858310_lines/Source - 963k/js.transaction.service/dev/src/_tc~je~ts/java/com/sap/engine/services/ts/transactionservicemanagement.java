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

package com.sap.engine.services.ts;

import com.sap.engine.frame.state.ManagementInterface;

/**
 * @author Iliyan Nenov, ilian.nenov@sap.com
 * @version SAP J2EE Engine 6.30
 */
public interface TransactionServiceManagement extends ManagementInterface {

   public int getOpenTransactionsCount();

   public int getSuspendedTransactionsCount();
   
   public long getRolledBackTransactionsCount();

   public long getCommittedTransactionsCount();

   public long getTimedOutTransactionsCount();

   public int getTransactionsSuccessRate();

}