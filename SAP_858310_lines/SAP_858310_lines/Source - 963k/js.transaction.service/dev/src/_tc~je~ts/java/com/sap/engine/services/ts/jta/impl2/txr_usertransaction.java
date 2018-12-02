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

import com.sap.engine.interfaces.resourcecontext.ResourceContext;
import com.sap.engine.interfaces.resourcecontext.ResourceContextFactory;
import com.sap.engine.interfaces.resourceset.ResourceSet;
import com.sap.engine.interfaces.resourceset.ResourceSetFactory;
import com.sap.engine.interfaces.transaction.TransactionExtension;
import com.sap.engine.interfaces.transaction.TransactionManagerExtension;
import com.sap.engine.interfaces.transaction.UserTransactionExtension;
import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

/**
 * This class implements UserTransaction interface and supports methods for transaction recovery.
 * 
 * @author I024163
 *
 */
public final class TXR_UserTransaction implements UserTransactionExtension, java.io.Serializable {//$JL-SER$

  private static final Location LOCATION = Location.getLocation(TXR_UserTransaction.class);
  private static final long serialVersionUID = 0L;
  public static final String UT_RESOURCE_SET_OBJECT_KEY = "ut_resource_set_object_key";
  private TransactionManagerExtension tManager;

  public TXR_UserTransaction(TransactionManagerExtension tManager) {
	this.tManager = tManager;
  }	

  /**
   * Create a new transaction with specified classifier and associate it with the current thread.
   * 
   * @param TransactionClassifier the classifier of the transaction.
   * @throws NotSupportedException - Thrown if the thread is already associated with a transaction and the
   *  Transaction Manager implementation does not support nested transactions.
   * @throws SystemException - Thrown if the transaction manager encounters an unexpected error condition
   */
  public void begin(String transactionClassifier) throws NotSupportedException, SystemException{  
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "UserTransaction.begin");
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000204", "UserTransaction.begin");
    }
    checkAccess();    
    tManager.begin(transactionClassifier);
    ResourceSetFactory rsf = TransactionServiceFrame.getResourceSetFactory();
    ResourceSet set = null;
    if (rsf!=null) {
        set = rsf.getCurrentResourceSet();
    }
    if (set != null) {
      try {
      		TransactionExtension startedTransaction = (TransactionExtension)tManager.getTransaction();
       		if(startedTransaction != set.getTransaction()){       			
       			set.enlistAll(startedTransaction);
       			startedTransaction.associateObjectWithTransaction(UT_RESOURCE_SET_OBJECT_KEY, set);
      		}      	
      } catch (RollbackException rolEx) { 
      	tManager.rollback();
        if (LOCATION.beLogged(Severity.ERROR)) {
//        LOCATION.traceThrowableT(Severity.ERROR, "UserTransaction.begin", rolEx);
          SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,rolEx, "ASJ.trans.000022", "UserTransaction.begin");
        }
      } catch (RuntimeException runEx){
      	tManager.rollback();
        throw runEx;
      } catch (SystemException sysEx){
        tManager.rollback();
        throw sysEx;
      }
    }
  }

  /**
   * Create a new transaction and associate it with the current thread.
   * 
   * @throws NotSupportedException - Thrown if the thread is already associated with a transaction and the
   *  Transaction Manager implementation does not support nested transactions.
   * @throws SystemException - Thrown if the transaction manager encounters an unexpected error condition
   */  
  public void begin() throws NotSupportedException, SystemException {
	  checkAccess();
	  begin(null);
  }
  
  /**
   * Sets specified classifier of the transaction which is associated with calling thread. 
   * This method will work only if there are no resource which were enlisted into transaction.
   * 
   * @param transactionClassifier the classifier of the transaction. 
   * @throws NotSupportedException Thrown when calling thread is not associated with transaction or if there are 
   * resources enlisted into transaction. 
 * @throws SystemException 
   */
  public void setTransactionClassifier(String transactionClassifier ) throws NotSupportedException, SystemException{
	  checkAccess();	  
	  tManager.setTransactionClassifier(transactionClassifier);
  }

  /**
   * @return the classifier of the transaction which is associated with calling thread or null
   *  if transaction has no classifier or there is no transaction associated with current thread. 
 * @throws SystemException 
   */
  public String getTransactionClassifier() throws SystemException {
	  checkAccess();
	  return tManager.getTransactionClassifier();
  }  

  /**
   * Commits the transaction, associated with the current thread.
   *
   * @exception  RollbackException  if transaction has been rolled back rather than committed
   * @exception  HeuristicMixedException  if heuristic decision has been
   *                                      made and some relevant branches have been
   *                                      rolled back, while others have been committed.
   * @exception  HeuristicRollbackException  if heuristic decision has been made and some
   *                                         relevant branches have been rolled back.
   * @exception  SecurityException  if thread is not allowed to commit the transaction.
   * @exception  IllegalStateException  if current thread is not associated with a transaction
   * @exception  SystemException  if general failure occurs.
   */
  public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "UserTransaction.commit");
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000205", "UserTransaction.commit");
    }    
    checkAccess();
    tManager.commit();
  } // commit()

  /**
   * Obtain the status of the transaction associated with the current thread.
   *
   * @return  the transaction status. If no transaction is associated with the current
   *          thread, this method returns the STATUS_NO_TRANSACTION value.
   *
   * @exception  SystemException  if general failure occurs.
   */
  public int getStatus() throws SystemException {
	checkAccess();	  
    return tManager.getStatus();
  }

  /**
   * Rolls back the transaction associated with the current thread.
   *
   * @exception  SecurityException  if thread is not allowed to commit the transaction.
   * @exception  IllegalStateException  if current thread is not associated with a transaction
   * @exception  SystemException  if general failure occurs.
   */
  public void rollback() throws IllegalStateException, SystemException, SecurityException {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "UserTransaction.rollback");
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000206", "UserTransaction.rollback");
    }
    checkAccess();
    tManager.rollback();
  } // rollback()

  /**
   * Marks the transaction associated with the current thread for rollback only.
   *
   * @exception  IllegalStateException  if current thread is not associated with a transaction
   * @exception  SystemException  if general failure occurs.
   */
  public void setRollbackOnly() throws IllegalStateException, SystemException {
    // This method must not be used
    // This class is only for Session Beans with Bean Managed transactions
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "UserTransaction.setRollbackOnly");
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000207", "UserTransaction.setRollbackOnly");
    }
    checkAccess();
    tManager.setRollbackOnly();
  } // setRollbackOnly()

  public void setTransactionTimeout(int timeout) throws SystemException {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "UserTransaction.setTransactionTimeout: " + timeout);
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000208", "UserTransaction.setTransactionTimeout: {0}", new Object[] {timeout});
    }
    checkAccess();
    tManager.setTransactionTimeout(timeout);
  } // setTransactionTimeout()
  
  private void checkAccess() throws SystemException{	 
	  ResourceContextFactory  resourceContextFactory = TransactionServiceFrame.getResourceContextFactory();
	  if(resourceContextFactory != null){
		  if(resourceContextFactory.isAccessRestricted(ResourceContext.RESTRICT_UT_ACESS)){
			  throw new IllegalStateException("UserTransaction access is forbidden. EJB spec: ");			  
		  }	  
	  }//else there is no active restriction
  }

}