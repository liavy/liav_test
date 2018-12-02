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

import com.sap.engine.frame.state.ManagementListener;
import com.sap.engine.services.ts.jta.impl2.TXR_TransactionManagerImpl;


/**
 * @author Iliyan Nenov, ilian.nenov@sap.com
 * @version SAP J2EE Engine 6.30
 */
public class TransactionServiceManagementImpl implements TransactionServiceManagement {

  /* a reference to the TransactionManager instance */
  private TXR_TransactionManagerImpl tManager;

  /**
   * Constructor
   */
  public TransactionServiceManagementImpl(TXR_TransactionManagerImpl tManagerParam) {
    this.tManager = tManagerParam;
  }

  
  // TODO get values from new statistics 
  public int getOpenTransactionsCount(){
    return tManager.getActiveTransactionsCount();  	
  }

  public int getSuspendedTransactionsCount(){
    return tManager.getSuspendedTransactionsCount();
  }

  public long getRolledBackTransactionsCount(){
    return tManager.getRollbackTransactionsCount();
  }

  public long getCommittedTransactionsCount(){
    return tManager.getCommitedTransactionsCount();
  }

  public long getTimedOutTransactionsCount(){
    return tManager.getTimedoutTransactionsCount();
  }

  public int getTransactionsSuccessRate(){
    long committedTrsCount = tManager.getCommitedTransactionsCount();
    long rolledBackTrsCount = tManager.getRollbackTransactionsCount();
    long finishedTrsCount = committedTrsCount + rolledBackTrsCount;
    if (finishedTrsCount == 0) {
      return 100;
    }
    return Math.round((committedTrsCount*100)/(finishedTrsCount)); 
  }
  

  /**
   * Registers ManagementInterface
   */
  public void registerManagementListener(ManagementListener managementListener) {
    // no listener is registered
  }

}