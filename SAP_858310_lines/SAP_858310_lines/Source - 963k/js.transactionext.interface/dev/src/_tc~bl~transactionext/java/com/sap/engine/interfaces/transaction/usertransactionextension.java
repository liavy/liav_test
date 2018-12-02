package com.sap.engine.interfaces.transaction;

import javax.transaction.UserTransaction;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;


/**
 * SAP specific extension of javax.transaction.UserTransaction interface. It is used to 
 * set a name for JTA transactions.
 *  
 * @author I024163
 *
 */
public interface UserTransactionExtension extends UserTransaction {

	/**
	 * Create a new transaction with specified classifier and associate it with the current thread.
	 * 
	 * @param TransactionClassifier the classifier of the transaction.
	 * @throws NotSupportedException - Thrown if the thread is already associated with a transaction and the
	 *  Transaction Manager implementation does not support nested transactions.
	 * @throws SystemException - Thrown if the transaction manager encounters an unexpected error condition
	 */
	public void begin(String TransactionClassifier) throws NotSupportedException, SystemException;
	
	 /**
	   * Sets specified classifier of the transaction which is associated with calling thread. 
	   * This method will work only if there are no resource which were enlisted into transaction.
	   * 
	   * @param transactionClassifier the classifier of the transaction. 
	   * @throws NotSupportedException Thrown when calling thread is not associated with transaction or if there are 
	   * resources enlisted into transaction. 
	   */
	public void setTransactionClassifier(String TransactionClassifier) throws NotSupportedException, SystemException;

	  /**
	   * @return the classifier of the transaction which is associated with calling thread or null
	   *  if transaction has no name or there is no transaction associated with current thread. 
	   */
	public String getTransactionClassifier() throws SystemException; 
	
}
