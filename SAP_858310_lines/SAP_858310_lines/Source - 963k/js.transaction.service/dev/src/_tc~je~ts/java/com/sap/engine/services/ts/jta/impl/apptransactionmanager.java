package com.sap.engine.services.ts.jta.impl;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import com.sap.engine.interfaces.resourceset.ResourceSet;
import com.sap.engine.interfaces.resourceset.ResourceSetFactory;
import com.sap.engine.interfaces.transaction.TransactionExtension;
import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.jta.impl2.TXR_TransactionManagerImpl;
import com.sap.engine.services.ts.jta.impl2.TXR_UserTransaction;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

/**
 * This class provides special implementation of javax.tramsaction.TransactionManager which will be used from
 * applications. The only one difference between this class and TransactionManagerImpl is implementation of resume and 
 * suspend methods. Suspend method delists resources from suspended transaction. Resume method enlists all currently opened 
 * connection into resumed transaction.
 *   
 * @author I024163
 *
 */
public class AppTransactionManager implements TransactionManager {

	  private static final Location LOCATION = Location.getLocation(AppTransactionManager.class);
	  private TXR_TransactionManagerImpl realTransactionManager = null;
	  
	  public AppTransactionManager(TXR_TransactionManagerImpl transactionManager) {
		  this.realTransactionManager = transactionManager;
	  }
	/**
	 * Create a new transaction and associate it with the current thread.
	 *
	 * @exception NotSupportedException Thrown if the thread is already
	 *    associated with a transaction and the Transaction Manager
	 *    implementation does not support nested transactions.
	 *
	 * @exception SystemException Thrown if the transaction manager
	 *    encounters an unexpected error condition.
	 *
	 */	  
	public void begin() throws NotSupportedException, SystemException {
		realTransactionManager.begin();		
		ResourceSetFactory resourceSetFactory = TransactionServiceFrame.getResourceSetFactory(); 
	    ResourceSet set = null;
	    if(resourceSetFactory != null){
	    	set = resourceSetFactory.getCurrentResourceSet();
	    }
	    if (set != null) {
	      try {
	      		TransactionExtension startedTransaction = (TransactionExtension)realTransactionManager.getTransaction();
	       		if(startedTransaction != set.getTransaction()){       			
	       			set.enlistAll(startedTransaction);
	       			startedTransaction.associateObjectWithTransaction(TXR_UserTransaction.UT_RESOURCE_SET_OBJECT_KEY, set);	       			
	      		}      	
	      } catch (RollbackException rolEx) { 
	    	realTransactionManager.rollback();
	        if (LOCATION.beLogged(Severity.ERROR)) {
//	          LOCATION.traceThrowableT(Severity.ERROR, "UserTransaction.begin", rolEx);
	        	SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,rolEx, "ASJ.trans.000001", "UserTransaction.begin");
	        }
	      } catch (RuntimeException runEx){
	    	realTransactionManager.rollback();
	        throw runEx;
	      } catch (SystemException sysEx){
	    	realTransactionManager.rollback();
	        throw sysEx;
	      }
	    }		
	}

	/**
	 * Complete the transaction associated with the current thread. When this
	 * method completes, the thread is no longer associated with a transaction.
	 *
	 * @exception RollbackException Thrown to indicate that
	 *    the transaction has been rolled back rather than committed.
	 *
	 * @exception HeuristicMixedException Thrown to indicate that a heuristic
	 *    decision was made and that some relevant updates have been committed
	 *    while others have been rolled back.
	 *
	 * @exception HeuristicRollbackException Thrown to indicate that a
	 *    heuristic decision was made and that all relevant updates have been
	 *    rolled back.
	 *
	 * @exception SecurityException Thrown to indicate that the thread is
	 *    not allowed to commit the transaction.
	 *
	 * @exception IllegalStateException Thrown if the current thread is
	 *    not associated with a transaction.
	 *
	 * @exception SystemException Thrown if the transaction manager
	 *    encounters an unexpected error condition.
	 *
	 */
	public void commit() throws RollbackException, HeuristicMixedException,
			HeuristicRollbackException, SecurityException,
			IllegalStateException, SystemException {
		realTransactionManager.commit();
		
	}

	/**
	 * Obtain the status of the transaction associated with the current thread.
	 *
	 * @return The transaction status. If no transaction is associated with
	 *    the current thread, this method returns the Status.NoTransaction
	 *    value.
	 *
	 * @exception SystemException Thrown if the transaction manager
	 *    encounters an unexpected error condition.
	 *
	 */
	public int getStatus() throws SystemException {
		return realTransactionManager.getStatus();
	}

	/**
	 * Get the transaction object that represents the transaction
	 * context of the calling thread.
	 *
	 * @return the <code>Transaction</code> object representing the
	 *	  transaction associated with the calling thread.
	 *
	 * @exception SystemException Thrown if the transaction manager
	 *    encounters an unexpected error condition.
	 *
	 */
	public Transaction getTransaction() throws SystemException {
		return realTransactionManager.getTransaction();
	}
	/**
	 * Resume the transaction context association of the calling thread
	 * with the transaction represented by the supplied Transaction object.
	 * When this method returns, the calling thread is associated with the
	 * transaction context specified.All resources that are created before 
	 * resume are enlisted into resumed transaction.
	 *
	 * @param tobj The <code>Transaction</code> object that represents the
	 *    transaction to be resumed.
	 *
	 * @exception InvalidTransactionException Thrown if the parameter
	 *    transaction object contains an invalid transaction.
	 *
	 * @exception IllegalStateException Thrown if the thread is already
	 *    associated with another transaction.
	 *
	 * @exception SystemException Thrown if the transaction manager
	 *    encounters an unexpected error condition.
	 */
	public void resume(Transaction transaction) throws InvalidTransactionException,
			IllegalStateException, SystemException {
		realTransactionManager.resumeAndEnlist(transaction);
	}

	/**
	* Roll back the transaction associated with the current thread. When this
	* method completes, the thread is no longer associated with a
	* transaction.
	*
	* @exception SecurityException Thrown to indicate that the thread is
	*    not allowed to roll back the transaction.
	*
	* @exception IllegalStateException Thrown if the current thread is
	*    not associated with a transaction.
	*
	* @exception SystemException Thrown if the transaction manager
	*    encounters an unexpected error condition.
	*
	*/
	public void rollback() throws IllegalStateException, SecurityException,
			SystemException {
		realTransactionManager.rollback();
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
	 *    encounters an unexpected error condition.
	 *
	 */
	public void setRollbackOnly() throws IllegalStateException, SystemException {
		realTransactionManager.setRollbackOnly();
	}

	/**
	 * Modify the timeout value that is associated with transactions started
	 * by the current thread with the begin method.
	 *
	 * <p> If an application has not called this method, the transaction
	 * service uses some default value for the transaction timeout.
	 *
	 * @param seconds The value of the timeout in seconds. If the value is zero,
	 *        the transaction service restores the default value. If the value
	 *        is negative a SystemException is thrown.
	 *
	 * @exception SystemException Thrown if the transaction manager
	 *    encounters an unexpected error condition.
	 *
	 */	
	public void setTransactionTimeout(int timeoutInSeconds) throws SystemException {
		realTransactionManager.setTransactionTimeout(timeoutInSeconds);
	}
	/**
	 * Suspend the transaction currently associated with the calling
	 * thread and return a Transaction object that represents the
	 * transaction context being suspended. If the calling thread is
	 * not associated with a transaction, the method returns a null
	 * object reference. When this method returns, the calling thread
	 * is not associated with a transaction.All resources are delisted 
	 * from suspended transaction.
	 *
	 * @return Transaction object representing the suspended transaction.
	 *
	 * @exception SystemException Thrown if the transaction manager
	 *    encounters an unexpected error condition.
	 *
	 */
	public Transaction suspend() throws SystemException {
		return realTransactionManager.suspendAndDelist();
	}
	  


}
