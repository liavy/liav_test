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

package com.sap.engine.services.ts.jta.impl2;

import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.omg.CosTransactions.PropagationContext;

import com.sap.engine.frame.core.load.Component;
import com.sap.engine.interfaces.resourcecontext.ResourceContext;
import com.sap.engine.interfaces.resourcecontext.ResourceContextException;
import com.sap.engine.interfaces.resourcecontext.ResourceContextFactory;
import com.sap.engine.interfaces.resourceset.ResourceSet;
import com.sap.engine.interfaces.resourceset.ResourceSetFactory;
import com.sap.engine.interfaces.transaction.TransactionExtension;
import com.sap.engine.interfaces.transaction.TransactionManagerExtension;
import com.sap.engine.services.ts.Log;
import com.sap.engine.services.ts.LogUtil;
import com.sap.engine.services.ts.TransactionContextObject;
import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.exceptions.BaseIllegalStateException;
import com.sap.engine.services.ts.exceptions.BaseInvalidTransactionException;
import com.sap.engine.services.ts.exceptions.BaseNotSupportedException;
import com.sap.engine.services.ts.exceptions.BaseSystemException;
import com.sap.engine.services.ts.exceptions.ExceptionConstants;
import com.sap.engine.services.ts.exceptions.TimeOutIsStoppedException;
import com.sap.engine.services.ts.inboundtx.JCATransaction;
import com.sap.engine.services.ts.inboundtx.XATerminatorImpl;
import com.sap.engine.services.ts.jta.statistics.TransactionStatistics;
import com.sap.engine.services.ts.jts.ots.OTSTransaction;
import com.sap.engine.services.ts.tlog.RMNameAlreadyInUseException;
import com.sap.engine.services.ts.tlog.RMPropsExtension;
import com.sap.engine.services.ts.tlog.TLog;
import com.sap.engine.services.ts.tlog.TLogIOException;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

/**
 * This class is an implementation of  javax.transaction.TransactionManager interface 
 * that supports transaction recovery. 
 *
 * @author I024163
 *
 */
public class TXR_TransactionManagerImpl implements TransactionManagerExtension {
  private static final Location LOCATION = Location.getLocation(TXR_TransactionManagerImpl.class);
  public static final String RESOURCE_CONTEXT_METHOD_NAME = "TxManager_tx_level"; 
  public static final String RESOURCE_CONTEXT_OBJECT_KEY = "resource_ctx";
  public static final String RM_CONTAINER_NAME_FOR_EXTERNAL_RMS = "DefaultContainerForExternalRMs";
  
  private int txSequenceNumberCounter = 0;
  private int rmSequenceNumberCounter = 0x0000FFFF;// this counter is used only when TLog is switched off
  private Object lockerFortxSequenceNumberCounter = new Object();  
  private Hashtable<String,RMPropsExtension> externalResourceManagers = null;
  private RMPropsExtension[] externalXAResources = null;
  
  protected final AtomicReference<XATerminatorImpl> xaTerminator = new AtomicReference<XATerminatorImpl>(null);

  /**
   * Constructor for TransactionManagerImpl
   */
  public TXR_TransactionManagerImpl() {
  }

  
  /**
   * Create a new transaction and associate it with the current thread.
   *
   * @param transactionClassifier the classifier of the transaction. Parameter will be ignored if it is null or empty. 
   * @exception NotSupportedException Thrown if the thread is already associated with a transaction
   * because Transaction Manager does not support nested transactions.
   * @exception SystemException Thrown if the transaction manager encounters an unexpected error condition
   */
  public void begin(String transactionClassifier) throws NotSupportedException, SystemException {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "TransactionManagerImpl.begin");
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000186", "TransactionManagerImpl.begin");
    }
    TransactionContextObject transactionContextObject = TransactionContextObject.getThransactionContextObject();
    
    TransactionExtension txOld = transactionContextObject.getTransaction();
        if (txOld != null && txOld.isAlive()) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
//        LOCATION.logT(Severity.DEBUG, "Thread already associated with transaction.");
        SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000187", "Thread already associated with transaction.");
      }
      throw new BaseNotSupportedException(ExceptionConstants.Thread_already_has_transaction, new Object[] {LogUtil.getDCNameByCaller(), LogUtil.getCSNComponentByCaller()} );
    } 
    
    int transactionSequenceNumber = 0;
    synchronized(lockerFortxSequenceNumberCounter) {
      transactionSequenceNumber = txSequenceNumberCounter++;
    }
    int timeout = transactionContextObject.getTimeout();    
    TXR_TransactionImpl newTx = new TXR_TransactionImpl(this, transactionSequenceNumber, timeout>0);
    transactionContextObject.setTransaction(newTx);
        
    if (timeout > 0) {
     try {
        TransactionServiceFrame.getTimeoutManager().registerTimeoutListener(newTx, (long)timeout * 1000, 0);
      } catch (TimeOutIsStoppedException e) {
        if (LOCATION.beLogged(Severity.WARNING)) {
//          LOCATION.logT(Severity.WARNING, "TransactionManagerImpl.begin timeout service is not started. There is no transaction timeout.");
          SimpleLogger.trace(Severity.WARNING,LOCATION, "ASJ.trans.000188", "TransactionManagerImpl.begin timeout service is not started. There is no transaction timeout.");
        }
        if (LOCATION.beLogged(Severity.DEBUG)) {
          LOCATION.traceThrowableT(Severity.DEBUG, "Full stacktrace: ", e);
        }
      }
    }

    if(transactionClassifier != null && !transactionClassifier.equals("")){
        setTransactionClassifier(transactionClassifier);    
    }
    
    ResourceSetFactory resourceSetFactory = TransactionServiceFrame.getResourceSetFactory();
    ResourceSet resourceSet = null;
    if(resourceSetFactory != null){
    	resourceSet = resourceSetFactory.getCurrentResourceSet();
    }    
    try{
        String dcName;
	    if (resourceSet == null) {
	      Component component = Component.getCallerComponent();
	      if (null!=component) {
		      dcName = component.getName();
	      } else {
	    	  dcName=null;
	      }
	      ResourceContextFactory rcf = TransactionServiceFrame.getResourceContextFactory();
	      if (rcf != null) {
		   	  ResourceContext resourceContext = rcf.createContext(dcName, "", true);
		      resourceContext.enterMethod(RESOURCE_CONTEXT_METHOD_NAME);
		      newTx.associateObjectWithTransaction(RESOURCE_CONTEXT_OBJECT_KEY, resourceContext);
	      }
	    } else {
	    	dcName = resourceSet.getApplicationName();
	    }
	    newTx.setDCName(dcName);
    } catch (ResourceContextException rce){
    	rollback();
      throw new BaseSystemException(ExceptionConstants.Cannot_Enlist_Resources_Into_Transaction, rce);
    }   
    
    TransactionStatistics.transactionStarted();
  }  
  
  /**
   *  Create a new transaction and associate it with the current thread.
   *  
   * @throws NotSupportedException - Thrown if the thread is already associated with a transaction because  
   * Transaction Manager does not support nested transactions.
   * @throws SystemException - Thrown if the transaction manager encounters an unexpected error condition,
   */
  public void begin() throws NotSupportedException, SystemException{
	begin(null);	
  }

  /**
   * Sets specified classifier of the transaction which is associated with calling thread. 
   * This method will work only if there are no resource which were enlisted into transaction.
   * 
   * @param transactionName the name of the transaction. 
   * @throws NotSupportedException Thrown when calling thread is not associated with transaction or if there are 
   * resources enlisted into transaction. 
   */
  public void setTransactionClassifier(String transactionClassifier) throws NotSupportedException{
	  TXR_TransactionImpl transacton = null;
	  try {
		transacton = (TXR_TransactionImpl)getTransaction();
	  } catch (SystemException e) {
		  NotSupportedException nspe = new NotSupportedException("It is not possible to set transaction classfier because unexpected exception was thrown.");
		  nspe.initCause(e);
		  throw nspe;
	  }
	  if(transacton == null){
		  throw new NotSupportedException(LogUtil.getFailedInComponentByCaller() + "It is not possible to set transaction classifier because current Thread is not associated with transaction.");
	  }
	  transacton.setTransactionClassifier(transactionClassifier);
  }
  
  /**
   * @return the classifier of the transaction which is associated with calling thread or null
   *  if transaction has no classifier or there is no transaction associated with current thread. 
   */
  public String getTransactionClassifier(){	  
	  TXR_TransactionImpl transacton = null;
	  try {
		transacton = (TXR_TransactionImpl)getTransaction();
	  } catch (SystemException e) {
		  NotSupportedException nspe = new NotSupportedException("It is not possible to get transaction classfier because unexpected exception was thrown.");
		  nspe.initCause(e);
		  // TODO logthrow nspe;
	  }
	  if(transacton == null){
		  return null;
	  }
	  return transacton.getTransactionClassifier();	  
} 	
  
  /**
   * Begins a new OTS Transaction. The transaction is propagated to the server from another server
   *
   * @param pgContext The propagation context of the transaction
   * @throws NotSupportedException when a new OTS Transaction is tried to be started while another transaction is active
   * @throws SystemException when an RuntimeException is cought while starting the OTS Transaction
   */
  // TO DO check if resourceContext must be created 
  public void beginOTStransaction(PropagationContext pgContext) throws NotSupportedException, SystemException {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "TransactionManagerImpl.beginOTStransaction({0})", new Object[]{Log.objectToString(pgContext)});
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000189", "TransactionManagerImpl.beginOTStransaction({0})", new Object[]{Log.objectToString(pgContext)});
    }
    TransactionContextObject transactionContextObject = TransactionContextObject.getThransactionContextObject();
    TransactionExtension txOld = transactionContextObject.getTransaction();

    if (txOld != null && txOld.isAlive()) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
//        LOCATION.logT(Severity.DEBUG, "Thread already associated with transaction.");
        SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000190", "Thread already associated with transaction.");
      }
      throw new BaseNotSupportedException(ExceptionConstants.Thread_already_has_transaction, new Object [] {LogUtil.getDCNameByCaller(), LogUtil.getCSNComponentByCaller()} );
    } else {
      OTSTransaction otsTX = new OTSTransaction(pgContext);
      transactionContextObject.setTransaction(otsTX);
      TransactionStatistics.transactionStarted();
      // todo timeContext.registerTimeoutListener(otsTX, timeout * 1000, 0);
      // todo not needed for now but if the original transaction hanged up this OTS fellow must be rolled back
    }
  } // beginOTStransaction()

  /**
   * Begins a new JCA Transaction. The transaction is imported in the server form an EIS system
   *
   * @param xid imported in the server with an ExecutionContext, passed to the Transaction Service from Work manager
   * @param timeout for expiration of this transaction
   * @return
   * @throws NotSupportedException when a new OTS Transaction is tried to be started while another transaction is active
   * @throws SystemException when an RuntimeException is caught while starting the OTS Transaction
   */
  // TO DO check if resource context must be created 
	public void beginJCATransaction(Xid xid, long timeout)
			throws NotSupportedException, SystemException {
		getXATerminator().beginJCATransaction(xid, timeout);
		TransactionStatistics.transactionStarted();		
	} // beginJCATransaction(Xid, long)

	public void suspendJCATransaction() throws SystemException {
		String errMsg = "Cannot suspend JCA transaction because the current thread is not associated with such.";

		try {
			JCATransaction tx = (JCATransaction)suspend();
			if (tx==null) {
				throw new SystemException(errMsg);
			}
			tx.inactivateInThread();

		} catch (ClassCastException e) {
//			SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, errMsg, e);
			SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,e, "ASJ.trans.000021", "Cannot suspend JCA transaction because the current thread is not associated with such.");
			SystemException sysEx = new SystemException(errMsg);
			sysEx.initCause(e);
			throw sysEx;
		}
	}

  /**
   * Complete the transaction associated with the current thread. When this
   * method completes, the thread becomes associated with no transaction.
   *
   * @exception RollbackException Thrown to indicate that
   *    the transaction has been rolled back rather than committed.
   *
   * @exception HeuristicMixedException Thrown to indicate that a heuristic
   *    decision was made and that some relevant updates have been committed
   *    while others have been rolled back.
   *
   * @exception HeuristicRollbackException Thrown to indicate that a
   *    heuristic decision was made and that some relevant updates have been
   *    rolled back.
   *
   * @exception SecurityException Thrown to indicate that the thread is
   *    not allowed to commit the transaction.
   *
   * @exception IllegalStateException Thrown if the current thread is
   *    not associated with a transaction.
   *
   * @exception SystemException Thrown if the transaction manager
   *    encounters an unexpected error condition
   *
   */   
   public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "TransactionManagerImpl.commit");
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000191", "TransactionManagerImpl.commit");
    }
    TransactionContextObject transactionContextObject = TransactionContextObject.getThransactionContextObject();
    
    TransactionExtension transaction = transactionContextObject.getTransaction();
    
    if (transaction == null || !transaction.isAlive()) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
//        LOCATION.logT(Severity.DEBUG, "Thread is not associated with transaction.");
        SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000192", "Thread is not associated with transaction.");
      }
      throw new BaseIllegalStateException(ExceptionConstants.Thread_is_not_associated_with_transaction, new Object[] {LogUtil.getDCNameByCaller(), LogUtil.getCSNComponentByCaller()} );
    } 
    
   	try{
       transaction.commit();
   	} finally {
       transactionContextObject.setTransaction(null);    	
   	}
   }

  /**
   * Obtain the status of the transaction associated with the current thread.
   *
   * @return The transaction status. If no transaction is associated with
   *    the current thread, this method returns the Status.NoTransaction
   *    value.
   *
   * @exception SystemException Thrown if the transaction manager
   *    encounters an unexpected error condition
   *
   */
  public int getStatus() throws SystemException {
    TransactionExtension tx = null;
    TransactionContextObject transactionContextObject = TransactionContextObject.getThransactionContextObject();

    if (transactionContextObject == null) {
      return Status.STATUS_NO_TRANSACTION;
    }

    try {
      tx = transactionContextObject.getTransaction();
      if (tx != null && tx.isAlive()) {
        return tx.getStatus();
      } else {
        return Status.STATUS_NO_TRANSACTION;
      }
    } catch (SystemException ex) {
      LOCATION.traceThrowableT(Severity.INFO, "Unexpected exception", ex);
      return Status.STATUS_UNKNOWN;
    }
  }

  /**
   * Get the transaction object that represents the transaction
   * context of the calling thread
   *
   * @return the Transaction object representing the transaction
   *    associated with the calling thread.
   *
   * @exception SystemException Thrown if the transaction manager
   *    encounters an unexpected error condition
   *
   */
  public javax.transaction.Transaction getTransaction() throws SystemException {
  	
    TransactionContextObject transactionContextObject = TransactionContextObject.getThransactionContextObject();
    
    if (transactionContextObject == null) {
      return null;
    }
    TransactionExtension tx = transactionContextObject.getTransaction();

    if (tx == null || !tx.isAlive()) {
      return null;
    } else {
      return tx;
    }
  }

  /**
   * Resume the transaction context association of the calling thread
   * with the transaction represented by the supplied Transaction object.
   * When this method returns, the calling thread is associated with the
   * transaction context specified. Resources that are acquired before 
   * resume are not enlisted into resumed transaction. 
   *
   * @param tobj The <code>Transaction</code> object that represents the
   *    transaction to be resumed.
   *
   * @exception InvalidTransactionException Thrown if the parameter
   *    transaction object contains an invalid transaction
   *
   * @exception IllegalStateException Thrown if the thread is already
   *    associated with another transaction.
   *
   * @exception SystemException Thrown if the transaction manager
   *    encounters an unexpected error condition
   */     
  public void resume(javax.transaction.Transaction tobj) throws InvalidTransactionException, IllegalStateException, SystemException {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "TransactionManagerImpl.resume({0})", new Object[]{Log.objectToString(tobj)});
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000193", "TransactionManagerImpl.resume({0})", new Object[]{Log.objectToString(tobj)});
    }
    TransactionContextObject transactionContextObject = TransactionContextObject.getThransactionContextObject();
    
    TransactionExtension tx = transactionContextObject.getTransaction();

    if (tobj == null) {
      return;
    }

    if (!(tobj instanceof TransactionExtension)) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
//        LOCATION.logT(Severity.DEBUG, "Not valid transaction object for this TransactionManager.");
        SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000194", "Not valid transaction object for this TransactionManager.");
      }
      throw new BaseInvalidTransactionException(ExceptionConstants.Not_valid_transaction_object, Log.objectToString(tobj));
    }

    if (tx != null && tx.isAlive()) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
//        LOCATION.logT(Severity.DEBUG, "Thread already associated with transaction.");
        SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000195", "Thread already associated with transaction.");
      }
      throw new BaseIllegalStateException(ExceptionConstants.Thread_already_has_transaction, new Object[] {LogUtil.getDCNameByCaller(), LogUtil.getCSNComponentByCaller()} );
    } else {    	
      transactionContextObject.setTransaction((TransactionExtension)tobj);

      TransactionStatistics.transactionResumed();
    }
  } // resume()

  /**
   * Resume the transaction context association of the calling thread
   * with the transaction represented by the supplied Transaction object.
   * When this method returns, the calling thread is associated with the
   * transaction context specified. Resources that are acquired before 
   * resume are enlisted into resumed transaction. 
   *
   * @param tobj The <code>Transaction</code> object that represents the
   *    transaction to be resumed.
   *
   * @exception InvalidTransactionException Thrown if the parameter
   *    transaction object contains an invalid transaction
   *
   * @exception IllegalStateException Thrown if the thread is already
   *    associated with another transaction.
   *
   * @exception SystemException Thrown if the transaction manager
   *    encounters an unexpected error condition
   */
  public void resumeAndEnlist(javax.transaction.Transaction tobj) throws InvalidTransactionException, IllegalStateException, SystemException {
	    if (LOCATION.beLogged(Severity.DEBUG)) {
//	      LOCATION.logT(Severity.DEBUG, "TransactionManagerImpl.resumeAndEnlist({0})", new Object[]{Log.objectToString(tobj)});
	      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000196", "TransactionManagerImpl.resumeAndEnlist({0})", new Object[]{Log.objectToString(tobj)});
	    }
        
	    resume(tobj);
	    
	    ResourceSetFactory rsf = TransactionServiceFrame.getResourceSetFactory();
	    ResourceSet set = null;
	    if (rsf != null) {
	    	set = rsf.getCurrentResourceSet();
	    }
	    if (set != null) {
	    	//there is no problem with stateful components because enlistAll will check if connections are associated with another transaction.
	    	  try {
				set.enlistAll(tobj);
			} catch (RollbackException e) {
				SystemException sysEx = new SystemException("Connections which were opened before resume are not enlisted into transaction due to " + e);
				sysEx.initCause(e);
				throw sysEx;
			}
	    }    
	    
  } // resume()
  
  /**
   * Roll back the transaction associated with the current thread. When this
   * method completes, the thread becomes associated with no transaction.
   *
   * @exception SecurityException Thrown to indicate that the thread is
   *    not allowed to roll back the transaction.
   *
   * @exception IllegalStateException Thrown if the current thread is
   *    not associated with a transaction.
   *
   * @exception SystemException Thrown if the transaction manager
   *    encounters an unexpected error condition
   *
   */
  public void rollback() throws java.lang.IllegalStateException, java.lang.SecurityException, SystemException {

    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "TransactionManagerImpl.rollback");
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000197", "TransactionManagerImpl.rollback");
    }
    TransactionContextObject transactionContextObject = TransactionContextObject.getThransactionContextObject();
    
    TransactionExtension transaction = transactionContextObject.getTransaction();
    
    if (transaction == null || !transaction.isAlive()) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
//        LOCATION.logT(Severity.DEBUG, "Thread is not associated with transaction.");
        SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000198", "Thread is not associated with transaction.");
      }
      throw new BaseIllegalStateException(ExceptionConstants.Thread_is_not_associated_with_transaction, new Object[] {LogUtil.getDCNameByCaller(), LogUtil.getCSNComponentByCaller()} );
    }
    
   	try{
      transaction.rollback();
   	} finally {
  	  transactionContextObject.setTransaction(null);
   	}
  	
  } 

  /**
   * Modify the transaction associated with the current thread such that
   * the only possible outcome of the transaction is to roll back the
   * transaction.
   *
   * @exception IllegalStateException Thrown if the current thread is
   *    not associated with a transaction.
   *
   * @exception SystemException Thrown if the transaction manager
   *    encounters an unexpected error condition
   *
   */
  public void setRollbackOnly() throws IllegalStateException, SystemException {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "TransactionManagerImpl.setRollbackOnly");
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000199", "TransactionManagerImpl.setRollbackOnly");
    }
    TransactionContextObject transactionContextObject = TransactionContextObject.getThransactionContextObject();

    TransactionExtension tx = transactionContextObject.getTransaction();
    if (tx == null || !tx.isAlive()) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
//        LOCATION.logT(Severity.DEBUG, "Thread is not associated with transaction.");
        SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000200", "Thread is not associated with transaction.");
      }
      throw new BaseIllegalStateException(ExceptionConstants.Thread_is_not_associated_with_transaction, new Object[] {LogUtil.getDCNameByCaller(), LogUtil.getCSNComponentByCaller()} );
    } else {
      tx.setRollbackOnly();
    }
  } // setRollbackOnly()

  /**
   * Modify the value of the timeout value that is associated with the
   * transactions started by the current thread with the begin method.
   *
   * <p> If an application has not called this method, the transaction
   * service uses some default value for the transaction timeout.
   *
   * @param seconds The value of the timeout in seconds. If the value
   *    is zero, the transaction service restores the default value.
   *
   * @exception SystemException Thrown if the transaction manager
   *    encounters an unexpected error condition
   *
   */
  public void setTransactionTimeout(int seconds) throws SystemException {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "TransactionManagerImpl.setTransactionTimeout({0})", new Object[]{Integer.toString(seconds)});
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000201", "TransactionManagerImpl.setTransactionTimeout({0})", new Object[]{Integer.toString(seconds)});
    }
    if(seconds < 0){
    	throw new SystemException(LogUtil.getFailedInComponentByCaller() + "It is not possible to set negative transaction timeout.");
    }
    TransactionContextObject transactionContextObject = TransactionContextObject.getThransactionContextObject();
    
    transactionContextObject.setTimeout(seconds);
  }

  /**
   * Suspend the transaction currently associated with the calling
   * thread and return a Transaction object that represents the
   * transaction context being suspended. If the calling thread is
   * not associated with a transaction, the method returns a null
   * object reference. When this method returns, the calling thread
   * is associated with no transaction. Resources are not delisted from 
   * suspended transaction.
   *
   * @return Transaction object representing the suspended transaction.
   *
   * @exception SystemException Thrown if the transaction manager
   *    encounters an unexpected error condition
   *
   * @exception SystemException Thrown if the transaction manager
   *    encounters an unexpected error condition
   *
   */
  public javax.transaction.Transaction suspend() throws SystemException {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "TransactionManagerImpl.suspend");
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000202", "TransactionManagerImpl.suspend");
    }
    TransactionContextObject transactionContextObject = TransactionContextObject.getThransactionContextObject();
    
    Transaction suspendedTx = transactionContextObject.getTransaction();

    if (suspendedTx == null || !((TransactionExtension)suspendedTx).isAlive()) {
      return null;
    }

    transactionContextObject.setTransaction(null);

    TransactionStatistics.trasactionSuspended();
    return suspendedTx;
  } // suspend()
  
  /**
   * Suspend the transaction currently associated with the calling
   * thread and return a Transaction object that represents the
   * transaction context being suspended. If the calling thread is
   * not associated with a transaction, the method returns a null
   * object reference. When this method returns, the calling thread
   * is associated with no transaction. Resources are delisted from 
   * suspended transaction.
   *
   * @return Transaction object representing the suspended transaction.
   *
   * @exception SystemException Thrown if the transaction manager
   *    encounters an unexpected error condition
   *
   * @exception SystemException Thrown if the transaction manager
   *    encounters an unexpected error condition
   *
   */
  public javax.transaction.Transaction suspendAndDelist() throws SystemException {
	    if (LOCATION.beLogged(Severity.DEBUG)) {
//	      LOCATION.logT(Severity.DEBUG, "TransactionManagerImpl.suspendAndDelist");
	      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000203", "TransactionManagerImpl.suspendAndDelist");
	    }

        Transaction result = suspend();

	    ResourceSetFactory rsf = TransactionServiceFrame.getResourceSetFactory();
	    ResourceSet set = null;
	    if (rsf != null) {
		    set = rsf.getCurrentResourceSet();
	    }
	    if (set != null) {
	    	//Resource set will prevent de-listment of connections from stateful beans 
	    	//This means that suspend is not supported from stateful bean which is in transaction. 
	    	  set.delistAll(XAResource.XA_OK);
	    }    
	        
	    return result;
	  } // suspend()	    
  
 //============= Methods for external RM management 
  
  /**
   * Registers new external resource manager into transaction manager repository. TransactionManager 
   * and application server are not responsible for automatic enlistment of XAResources from specified
   * resource manager. 
   *   
   * @param rmName the name of the resource manager which will be registered. This name must be unique.
   * @param rmXAResource an extension of XAResource interface which provides information about resource manager 
   * @throws SystemException - Thrown if the transaction manager encounters an unexpected error condition or another
   * RM with same name was already registered.
   */
  public synchronized void registerRM(String rmName, XAResource rmXAResource) throws SystemException{
	  if(externalResourceManagers == null){
		  externalResourceManagers = new Hashtable<String,RMPropsExtension>();
	  } else {
		  if(externalResourceManagers.get(rmName) != null){
			  throw new SystemException("TransactionManager is not able to register this RM because with name "+rmName+" because this name is already used from another resource manager.");
		  }
		  if(getRMIdOfExternalRM(rmXAResource) != 0){
			  throw new SystemException("TransactionManager is not able to register this RM because it is already registered with different name.");
		  }
	  }

      RMPropsExtension rmPropsExtension = new RMPropsExtension();
      rmPropsExtension.setKeyName(rmName);
      rmPropsExtension.setRmContainerName(RM_CONTAINER_NAME_FOR_EXTERNAL_RMS);
      rmPropsExtension.setNonSecureProperties(new Properties());
      rmPropsExtension.setSecureProperties(new Properties());
      int rmID = 0;
      try {
    	  TLog tlog = TransactionServiceFrame.getTLog();
    	  if(tlog != null){
    		  rmID = tlog.registerNewRM(rmPropsExtension);    		  
    	  } else {// used only when tlogging is switched off
    		  rmID = generateUniqueRMId();
    	  }

      } catch (TLogIOException e) {
    	  SystemException sysEx = new SystemException("TransactionManager is not able to register this RM because of unexpected exception.");
    	  sysEx.initCause(e);
    	  throw sysEx;
      } catch (RMNameAlreadyInUseException e) {
    	  SystemException sysEx = new SystemException("TransactionManager is not able to register this RM because it is already registered.");
    	  sysEx.initCause(e);
    	  throw sysEx;
      }
      if(rmID <= 0){
    	  throw new SystemException("TransactionManager is not able to register this resource manager because generated ID from TLog implementation is not positive.");
      }
      rmPropsExtension.setXAResource(rmXAResource);
      rmPropsExtension.setRmID(rmID);
      synchronized(externalResourceManagers){
          externalResourceManagers.put(rmName, rmPropsExtension);    	  
    	  externalXAResources = (RMPropsExtension[])externalResourceManagers.values().toArray();
      }
}
  
  /**
   * Unregisters specified resource manager from TransactionManager repository
   * 
   * @param rmName the name of the resource manager which will be unregistered.
   * @throws IllegalArgumentException - Thrown when specified resource manager does not exist. 
   * @throws SystemException - Thrown if the transaction manager encounters an unexpected error condition
   */
  public void unregisterRM(String rmName) throws IllegalArgumentException, SystemException{
	  if(externalResourceManagers == null || externalResourceManagers.get(rmName) != null){
		  throw new IllegalArgumentException("TransactionManager is not able to unregister resource manager"+rmName+" because it was not registered. ");
	  }
	  
	  synchronized (externalResourceManagers) {
		  externalResourceManagers.remove(rmName);
		  externalXAResources = (RMPropsExtension[])externalResourceManagers.values().toArray();
	  }
}  
  
/**
 * Checks if XAResource is from ResourceManager which is already registered and 
 * returns the ID of this RM.  
 * 
 * @param xaRes XAResource 
 * @return the ID of the resource manager to which provided XAResource instance belongs or 0
 *  if provided XAResource is from unknown resource manager. 
 */
  public int getRMIdOfExternalRM(XAResource xaRes)throws SystemException{
	  if(externalXAResources == null){
		  return 0;//RM is not registered
	  }
	  RMPropsExtension[] externalXAResources_localcopy = externalXAResources;
	  SystemException exceptionToThrow = null;
	  for(RMPropsExtension rmProps : externalXAResources_localcopy){
		  try{
			  if(rmProps.getXAResource() != null && rmProps.getXAResource().isSameRM(xaRes)){
				  return rmProps.getRmID();
			  }		
		  } catch (Exception e){// all exceptions from isSameRM
			  exceptionToThrow = new SystemException("Unexpected exception occurred while checking the XAResource");
			  exceptionToThrow.initCause(e);
		  }
	  }
	  return 0; //when it is not found.
}  
  
  /**
   * Generates unique resource manager ID when Tlog is not used.
   * @return unique id for resource manager. This id is used in XID generation.
   */
  public synchronized int generateUniqueRMId(){
	  return rmSequenceNumberCounter++;
  }
  
//======= methods for Monitoring

  public long getRollbackTransactionsCount() {
    return TransactionStatistics.getGlobalTransactionStatisticsData().getRollbackTransactionsCount();
  }

  public long getCommitedTransactionsCount() {
	  return TransactionStatistics.getGlobalTransactionStatisticsData().getCommitedTransactionsCount();
  }

  public long getTimedoutTransactionsCount() {
	  return TransactionStatistics.getGlobalTransactionStatisticsData().getTimedoutTransactionsCount();
  }

  public int getActiveTransactionsCount() {
	  return TransactionStatistics.getGlobalTransactionStatisticsData().getActiveTransactionsCount();
  }

  public int getSuspendedTransactionsCount() {
	  return TransactionStatistics.getGlobalTransactionStatisticsData().getSuspendedTransactionsCount();
  }


  public XATerminatorImpl getXATerminator() {
	  if (xaTerminator.get() == null) {
		  xaTerminator.compareAndSet(null, new XATerminatorImpl(this));
	  }
	  return xaTerminator.get();
  }

}
