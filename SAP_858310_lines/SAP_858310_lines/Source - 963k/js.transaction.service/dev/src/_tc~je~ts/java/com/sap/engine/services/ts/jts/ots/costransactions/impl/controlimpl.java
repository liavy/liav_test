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

package com.sap.engine.services.ts.jts.ots.CosTransactions.impl;

import com.sap.engine.interfaces.transaction.TransactionExtension;
import com.sap.engine.lib.util.LinkedList;
import com.sap.engine.services.ts.Util;
import com.sap.engine.services.ts.jta.impl2.TXR_TransactionImpl;
import com.sap.engine.services.ts.jts.TransactionServiceImpl;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.Inactive;
import org.omg.CosTransactions.NotPrepared;
import org.omg.CosTransactions.NotSubtransaction;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.RecoveryCoordinator;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.Status;
import org.omg.CosTransactions.SubtransactionsUnavailable;
import org.omg.CosTransactions.SynchronizationUnavailable;
import org.omg.CosTransactions.TransIdentity;
import org.omg.CosTransactions.Unavailable;
import org.omg.CosTransactions.otid_t;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

/**
 * This is implementation of control object using OTS API
 *
 * @author Iliyan Nenov, ilian.nenov@sap.com
 * @version SAP J2EE Engine 6.30
 */
public class ControlImpl extends ControlImplBase {//$JL-SER$

  private static final Location LOCATION = Location.getLocation(ControlImpl.class);

  /* A list with OMG resources registered to this control */
  private LinkedList omgResources = new LinkedList();
  /* Transaction object holded by this Control object */
  private TransactionExtension tx = null;
  /* PropagationContext of this Control */
  private PropagationContext pgContext = null;

  /* Constructor of the Control object */
  public ControlImpl(TransactionExtension txParam) {
    tx = txParam;
  }

  public LinkedList getOMGResources() {
    return omgResources;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  org.omg.CosTransaction.Control          IMPLEMENTATION                                              //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  /**
   * Returns terminator object to this transaction
   */
  public org.omg.CosTransactions.Coordinator get_coordinator() throws Unavailable {
    int txStatus = -1;
    try {
      txStatus = tx.getStatus();
    } catch (SystemException se) {
      if (LOCATION.beLogged(Severity.ERROR)) {
//        LOCATION.traceThrowableT(Severity.ERROR, "SystemException in transaction propagation.Coordinator is not available.", se);
        SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,se, "ASJ.trans.000023", "SystemException in transaction propagation.Coordinator is not available.");
      }
    }

    if (txStatus == javax.transaction.Status.STATUS_ACTIVE || txStatus == javax.transaction.Status.STATUS_MARKED_ROLLBACK) {
      return this;
    }

    throw new Unavailable();
  }

  /**
   * Returns a terminator object to transaction
   */
  public org.omg.CosTransactions.Terminator get_terminator() throws Unavailable {
    int txStatus = -1;
    try {
      txStatus = tx.getStatus();
    } catch (SystemException se) {
      if (LOCATION.beLogged(Severity.ERROR)) {
//        LOCATION.traceThrowableT(Severity.ERROR, "SystemException in transaction propagation.Terminator is not available.", se);
        SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,se, "ASJ.trans.000024", "SystemException in transaction propagation.Terminator is not available.");
      }
    }

    if (txStatus == javax.transaction.Status.STATUS_ACTIVE || txStatus == javax.transaction.Status.STATUS_MARKED_ROLLBACK) {
      return this;
    }

    throw new Unavailable();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  org.omg.CosTransaction.Coordinator      IMPLEMENTATION                 TEND IMPLEMENTED             //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the status of the transaction associated with this Control
   */
  public Status get_status() {
    try {
      return Util.jta2omgStatus(tx.getStatus());
    } catch (SystemException se) {
      if (LOCATION.beLogged(Severity.ERROR)) {
//        LOCATION.traceThrowableT(Severity.ERROR, "SystemException in transaction propagation. Status is not available.", se);
        SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,se, "ASJ.trans.000025", "SystemException in transaction propagation. Status is not available.");
      }
    }
    return null;
  }

  /**
   * Returns the status of the parent transaction, if the implementation does not support
   * nested transactions the status of this transaction os returned
   */
  public Status get_parent_status() {
    // nested transactions are not supported in this version
    try {
      return Util.jta2omgStatus(tx.getStatus());
    } catch (SystemException se) {
      if (LOCATION.beLogged(Severity.ERROR)) {
//        LOCATION.traceThrowableT(Severity.ERROR, "SystemException in transaction propagation. Parent status is not available.", se);
        SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,se, "ASJ.trans.000026", "SystemException in transaction propagation. Parent status is not available.");
      }
    }
    return null;
  }

  /**
   *
   */
  public Status get_top_level_status() {
    // nested transactions are not supported in this version
    try {
      return Util.jta2omgStatus(tx.getStatus());
    } catch (SystemException se) {
      if (LOCATION.beLogged(Severity.ERROR)) {
//        LOCATION.traceThrowableT(Severity.ERROR, "SystemException in transaction propagation. Top level status is not available.", se);
        SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,se, "ASJ.trans.000027", "SystemException in transaction propagation. Top level status is not available.");
      }
    }
    return null;
  }

  /**
   *
   */
  public boolean is_same_transaction(org.omg.CosTransactions.Coordinator tc) {
    if (tc == null) {
      return false;
    }

    return tc.hash_transaction() == this.hash_transaction();
  }

  /**
   *
   */
  public boolean is_related_transaction(org.omg.CosTransactions.Coordinator tc) {
    return false; // nested transactions are not supported in this version
  }

  /**
   *
   */
  public boolean is_ancestor_transaction(org.omg.CosTransactions.Coordinator tc) {
    return false; // nested transactions are not supported in this version
  }

  /**
   *
   */
  public boolean is_descendant_transaction(org.omg.CosTransactions.Coordinator tc) {
    return false; // nested transactions are not supported in this version
  }

  /**
   *
   */
  public boolean is_top_level_transaction() {
    return true; // nested transactions are not supported in this version
  }

  /**
   *
   */
  public int hash_transaction() {
    return tx.hashCode();
  }

  /**
   *
   */
  public int hash_top_level_tran() {
    return hash_transaction(); // nested transactions are not supported in this version
  }

  /**
   * Registers a resource to the coordinator of the transaction
   *
   * @param resourceParam an instance to OMG resource
   * @return an instance to the recovery coordinator
   * @throws Inactive if the JTA transaction is inactive
   */
  public RecoveryCoordinator register_resource(org.omg.CosTransactions.Resource resourceParam) throws Inactive {
    if (tx.isAlive()) {
      omgResources.add(resourceParam);
    } else {
      if (LOCATION.beLogged(Severity.DEBUG)) {
//        LOCATION.logT(Severity.DEBUG, "ControlImpl.register_resource is not successful because the transaction is not alive.");
        SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000209", "ControlImpl.register_resource is not successful because the transaction is not alive.");
      }
      throw new Inactive("Could not register resource because the transaction is not alive");
    }
    return this;
  }

  /**
   * Register synchronization to the transaction this control was created with
   *
   * @param sync an instance to the OMG Synchronization
   * @throws Inactive if Illegal state exception is thrown while registering the OTS wrapper or SystemException occures
   * @throws SynchronizationUnavailable if coordinator does not supports registering synchronizations
   */
  public void register_synchronization(org.omg.CosTransactions.Synchronization sync) throws Inactive, SynchronizationUnavailable {

    try {
      if (tx.isAlive()) {
        tx.registerSynchronization(new SynchronizationOTSWrapper(sync));
      }
    } catch (RollbackException rbe) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
        LOCATION.traceThrowableT(Severity.DEBUG, "ControlImpl.register_synchronization.", rbe);
      }
      throw new TRANSACTION_ROLLEDBACK(rbe.getMessage());
    } catch (IllegalStateException ise) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
        LOCATION.traceThrowableT(Severity.DEBUG, "ControlImpl.register_synchronization.", ise);
      }
      throw new Inactive(ise.getMessage());
    } catch (SystemException se) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
        LOCATION.traceThrowableT(Severity.DEBUG, "ControlImpl.register_synchronization.", se);
      }
      throw new Inactive(se.getMessage());
    }

  }

  /**
   *
   */
  public void register_subtran_aware(org.omg.CosTransactions.SubtransactionAwareResource rr) throws Inactive, NotSubtransaction {
    // nested transactions are not supported in this version
    // NO IMPLEMENTATION
  }

  /**
   * Marks the propagated transction for rollback only
   */

  public void rollback_only() throws Inactive {
    try {
      if (tx.getStatus() == javax.transaction.Status.STATUS_MARKED_ROLLBACK) {
        if (LOCATION.beLogged(Severity.DEBUG)) {
//          LOCATION.logT(Severity.DEBUG, "ControlImpl.rollback_only. Transactoion is already marked for rollback.");
          SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000210", "ControlImpl.rollback_only. Transactoion is already marked for rollback.");
        }
        throw new Inactive();
      } else {
        tx.setRollbackOnly();
      }
    } catch (SystemException se) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
        LOCATION.traceThrowableT(Severity.DEBUG, "SystemException in transaction propagation rollback_only.", se);
      }
    }
  }

  /**
   * Returns a string representation of the transaction in this control
   */
  public java.lang.String get_transaction_name() {
    if (!tx.isAlive()) {
      return null;
    }

    return tx.toString();
  }

  /**
   *
   */
  public Control create_subtransaction() throws SubtransactionsUnavailable, Inactive {
    // nested transactions are not supported in this version
    throw new SubtransactionsUnavailable();
  }

  /**
   * Returns the PropagationContext for this if such does not exist creates one
   *
   * @return a PropagationContext instance
   * @throws Unavailable if the transaction of this control is not alive
   */
  public PropagationContext get_txcontext() throws Unavailable {
    if (!tx.isAlive()) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
//        LOCATION.logT(Severity.DEBUG, "Transaction is not alive in method ControlImpl.get_txcontext.");
        SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000211", "Transaction is not alive in method ControlImpl.get_txcontext.");
      }
      throw new Unavailable();
    } else if (pgContext == null) {
      pgContext = new PropagationContext(86400, new TransIdentity(this, this, new otid_t(0, 0, ((TXR_TransactionImpl)tx).getGlobalTxIDWith_0_BranchIterator())), new TransIdentity[0], TransactionServiceImpl.orb.create_any());
    }

    return pgContext;
  }


  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  org.omg.CosTransaction.Terminator      IMPLEMENTATION                  TEND COMPLETED               //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  /**
   * Commits the transction associated to this Terminator
   *
   * @param reportHeuristic not used ...
   * @throws HeuristicMixed if HeuristicMixedException occures while commiting the JTA transction
   * @throws HeuristicHazard if SystemException occures while commiting the JTA transaction
   */
  public void commit(boolean reportHeuristic) throws HeuristicMixed, HeuristicHazard {

    try {
      tx.commit();
    } catch (IllegalStateException ise) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
        LOCATION.traceThrowableT(Severity.DEBUG, "ControlImpl.commit", ise);
      }
      throw new INVALID_TRANSACTION(ise.getMessage());
    } catch (SystemException ise) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
        LOCATION.traceThrowableT(Severity.DEBUG, "ControlImpl.commit", ise);
      }
      if (reportHeuristic) {
        throw new HeuristicHazard(); // taka e po specifikaciq, no nested tranzakcii ne se potdyrjat
      }
    } catch (RollbackException rbe) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
        LOCATION.traceThrowableT(Severity.DEBUG, "ControlImpl.commit", rbe);
      }
      throw new TRANSACTION_ROLLEDBACK(rbe.getMessage());
    } catch (HeuristicRollbackException hrbe) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
        LOCATION.traceThrowableT(Severity.DEBUG, "ControlImpl.commit", hrbe);
      }
      if (reportHeuristic) {
        throw new TRANSACTION_ROLLEDBACK(hrbe.getMessage());
      }
    } catch (HeuristicMixedException hme) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
        LOCATION.traceThrowableT(Severity.DEBUG, "ControlImpl.commit", hme);
      }
      if (reportHeuristic) {
        throw new HeuristicMixed();
      }
    } catch (SecurityException se) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
        LOCATION.traceThrowableT(Severity.DEBUG, "ControlImpl.commit", se);
      }
      if (reportHeuristic) {
        throw new HeuristicHazard(se.toString());
      }

      throw new INVALID_TRANSACTION(se.toString());
    } finally {
      if (_orb() != null) {
        _orb().disconnect(this);
      }
    }
  }

  /**
   * Rollback the transaction associated to this control
   */
  public void rollback() {
    try {
      tx.rollback();
    } catch (IllegalStateException ise) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
        LOCATION.traceThrowableT(Severity.DEBUG, "ControlImpl.rollback", ise);
      }
      throw new INVALID_TRANSACTION(ise.getMessage());
    } catch (SystemException se) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
        LOCATION.traceThrowableT(Severity.DEBUG, "ControlImpl.rollback", se);
      }
      throw new INVALID_TRANSACTION(se.toString());
    } finally {
      if (_orb() != null) {
        _orb().disconnect(this);
      }      
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  org.omg.CosTransaction.RecoveryCoordinator      IMPLEMENTATION                                      //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the status of the transaction associated with this control
   *
   * @param resource
   * @return
   * @throws NotPrepared
   */
  public Status replay_completion(Resource resource) throws NotPrepared {
    return get_status();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  //                                                Additional                                            //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the transaction associated with this control
   *
   * @return an instance to the transaction
   */
  public TransactionExtension getTransaction() {
    return tx;
  }
  
  public void disconnectFromORB(){
  	if (_orb() != null) {
      _orb().disconnect(this);
    }
  }
  
}

