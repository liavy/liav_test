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
package com.sap.engine.services.ts.jts.ots.CosTransactions.impl;

import com.sap.engine.services.ts.Util;
import com.sap.engine.interfaces.transaction.SynchronizationExtension;

/**
 * This class is implementation of OMG Synchronization and acts as a
 * wrapper to an JTA Synchronization. When an JTA synchronization is
 * about to be registered in a OTS Transaction a wrapper is created and
 * then the wrapper is registered in the coordinator of the transaction
 *
 * @author Iliyan Nenov, ilian.nenov@sap.com
 * @version SAP J2EE Engine 6.30
 */
public class SynchronizationJTAWrapper extends SynchronizationImplBase {//$JL-SER$

  javax.transaction.Synchronization synch = null;

  /**
   * Constructs a new wrapper of OMG synchronization
   *
   * @param synchParam a JTA synchronization
   */
  public SynchronizationJTAWrapper(javax.transaction.Synchronization synchParam) {
    synch = synchParam;
  }

  /**
   * Implementation of beforeCompletion method, because this class acts as a proxy
   * to the JTA synchronization the implementation calls beforeCompetion of the JTA synchronization
   */
  public void before_completion() {
    synch.beforeCompletion();
  }

  /**
   * Implementation of afterCompletion method, because this class acts as a proxy
   * to the JTA synchronization the implementation calls afterCompetion of the JTA synchronization
   */
  public void after_completion(org.omg.CosTransactions.Status statusParam) {
  	 synch.afterCompletion(Util.omg2jtaStatus(statusParam));
  }

  /**
   * Implementation of before?ompletion method, because this class acts as a proxy
   * to the JTA synchronization the implementation calls beforeCompetion of the JTA synchronization
   *
   * Note: this method is SAP J2EE Engine 6.30 specific and is used by the EJB container
   */
  public void beforeRollback(){
   if (synch instanceof SynchronizationExtension) {
     ((SynchronizationExtension) synch).beforeRollback();
   }
  }
  
}

