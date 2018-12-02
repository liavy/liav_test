package com.sap.engine.interfaces.transaction;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.resource.spi.XATerminator;

/**
 * @author I024163
 *
 */
public interface TransactionManagerExtension extends TransactionManager {

  /**
   * Used from transaction inflow. 
   * @param xid of the imported transaction
   * @param timeout of the imported transaction
   * @throws NotSupportedException - Thrown if this transaction is associated with other thread
   * @throws SystemException - Thrown if the transaction manager encounters an unexpected error condition
   */
public void beginJCATransaction(Xid xid, long timeout) throws NotSupportedException, SystemException;

	/**
	 * Suspend the current 
	 * @throws SystemException Thrown if the transaction manager encounters an unexpected error condition
	 */
public void suspendJCATransaction() throws SystemException;
  
  /**
   *  Create a new transaction with specified classifier and associate it with the current thread.
   *  
   * @param transactionClassifier the classifier of the transaction
   * @throws NotSupportedException - Thrown if the thread is already associated with a transaction and 
   * the Transaction Manager implementation does not support nested transactions.
   * @throws SystemException - Thrown if the transaction manager encounters an unexpected error condition
   */
public void begin(String transactionClassifier) throws NotSupportedException, SystemException; 
  
  /**
   * Sets specified classifier of the transaction which is associated with calling thread. 
   * This method will work only if there are no resource which were enlisted into transaction.
   * 
   * @param transactionClassifier the classifier of the transaction. 
   * @throws NotSupportedException Thrown when calling thread is not associated with transaction or if there are 
   * resources enlisted into transaction. 
   */
public void setTransactionClassifier(String transactionClassifier) throws NotSupportedException;
  
  /**
   * @return the classifier of the transaction which is associated with calling thread or null
   *  if transaction has no classifier or there is no transaction associated with current thread. 
   */
public String getTransactionClassifier(); 
  
  
  /**
   * Registers new external resource manager into transaction manager repository. TransactionManager 
   * and application server are not responsible for automatic enlistment of XAResources from specified
   * resource manager. 
   *   
   * @param rmName the name of the resource manager which will be registered. This name must be unique.
   * @param rmXAResource an extension of XAResource interface which provides information about resource manager 
   * @throws IllegalArgumentException - Thrown when provided resource manager name is null or when provided XAResource
   * instance is null;
   * @throws SystemException - Thrown if the transaction manager encounters an unexpected error condition or another
   * RM with same name was already registered.
   */
public void registerRM(String rmName, XAResource rmXAResource) throws IllegalArgumentException, SystemException;
  
  /**
   * Unregisters specified resource manager from TransactionManager repository
   * 
   * @param rmName the name of the resource manager which will be unregistered.
   * @throws IllegalArgumentException - Thrown when specified resource manager does not exist. 
   * @throws SystemException - Thrown if the transaction manager encounters an unexpected error condition
   */
public void unregisterRM(String rmName) throws IllegalArgumentException, SystemException;

	/**
	 * Return an XATerminator implementation
	 * 
	 * @return XATerminator implementation
	 */
public XATerminator getXATerminator() throws SystemException;
}
