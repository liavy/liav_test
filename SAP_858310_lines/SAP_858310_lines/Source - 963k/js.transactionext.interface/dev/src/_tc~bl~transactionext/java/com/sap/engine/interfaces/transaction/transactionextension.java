/*
 * Copyright (c) 2002 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */

package com.sap.engine.interfaces.transaction;

/**
 * @author : Iliyan Nenov, ilian.nenov@sap.com
 * @version 1.0 initial version
 * @version 1.1 Moved to engine.interfaces and replaced ResourceReference with LocalTxProvider
 */

import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.xa.XAResource;

import java.util.List;

/**
 * Interface Transaction with some extra methods giving extra functionality
 * 
 * @author  Iliyan Nenov, ilian.nenov@sap.com
 * @version 1.0 initial version
 * @version 1.1 Moved to engine.interfaces and replaced ResourceReference with LocalTxProvider
 */
public interface TransactionExtension
    extends javax.transaction.Transaction {

  /**
   * Checks if transaction is empty
   */
  public boolean isEmpty();


  /**
   * Returns managed connection for this transaction
   */
  public LocalTxProvider getLocalResource();


  /**
   * Enlists the ResourceReference with LocalTransaction
   *
   * @exception ResourceException thrown by LocalTransaction begin method
   * see implementation
   */
  public void enlistLocalResource(LocalTxProvider localRef) throws SystemException;

  /**
   * Returns the managed connections with 2PC support associated to this JTA transaction. 
   */
  public List getXAResourceConnections();  
  
  /**
   * Checks if the transaction is alive
   */
  public boolean isAlive();


  /**
   * Returns the ID of the Transaction
   */
   
  public long getID();
  
  /**
   * Associates an Object with the transaction.
   *
   * @param sid  The registration id of the object
   *
   * @exception SystemException thrown when specified sid is used from 
   * another object
   */
  public void associateObjectWithTransaction(Object sid, Object obj) throws SystemException;
  
  /**
   * Returns the object that is associated with this transaction with 
   * specified SID. Returns null if such object does not exist
   *
   * @param sid  The registration id of the object
   *
   */  
  public Object getAssociatedObjectWithTransaction(Object sid);
  
  /**
   * Register a synchronization object for the transaction currently
   * associated with the calling thread. The transction manager invokes
   * the beforeCompletion method prior to starting the transaction
   * commit process. After the transaction is completed, the transaction
   * manager invokes the afterCompletion method. This method does not check 
   * if transaction is marked for rollback. This method can be used when 
   * synchronization must be registered into transaction that is marked for rollback
   *
   * @param newSynchronizaton The Synchronization object for the transaction associated
   *    with the target object
   *
   * @exception RollbackException  this exception is not thrown
   *
   * @exception IllegalStateException Thrown if the transaction in the
   *    target object is in prepared state or the transaction is inactive.
   *
   * @exception SystemException Thrown if the transaction manager
   *    encounters an unexpected error condition
   *
   */
  public void registerSynchronizationWithoutStatusChecks(Synchronization newSynchronizaton) throws RollbackException, IllegalStateException, SystemException;
  
  
  /**
   * Used from connection management system to enlist XAResources together with information about 
   * resource manager from which this XAResource was created.
   *   
   * @param rmID Id of the resource manager from which XAResource was created.
   * @param xaRes The XAResource object representing the resource to delist 
   * @return true if the resource was enlisted successfully; otherwise false. 
   * @throws RollbackException Thrown to indicate that the transaction has been marked for rollback only. 
   * @throws IllegalStateException Thrown if the transaction in the target object is in prepared state or the transaction is inactive.
   * @throws SystemException Thrown if the transaction manager encounters an unexpected error condition 

   */
  public boolean enlistResource(int rmID, XAResource xaRes) throws RollbackException, IllegalStateException, SystemException;

}