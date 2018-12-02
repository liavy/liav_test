package com.sap.engine.services.ts.tlog;

/**
 * The implementation of this interface provides information about one TLog record for one transaction.
 * The interface will be implemented from Transaction object and also from TLog modules.
 * 
 * @author I024163
 */
public interface TransactionRecord {
	
	/**
	 * @return the transaction abandon time in milliseconds. This method is used mainly from DBMS based transaction 
	 * loggers.
	 */
	public long getTransactionAbandonTimeout();
	
	/**
	 * @return the transaction birth time in milliseconds. This method is used mainly from DBMS based transaction 
	 * loggers. 
	 */
	public long getTransactionBirthTime();

	/**
	 * @return the ID of transaction classifier which is associated with current transaction.
	 */
	public int getTransactionClassifierID();
	
	/**
	 * @return the transaction sequence number. This number is unique only within one TM and one transaction log. 
	 * This uniqueness is enough because TM will create separate TLOG during each startup. This method is used mainly
	 * from DBMS based transaction loggers.  
	 */
	public long getTransactionSequenceNumber();
	
	/**	 
	 * @return Array with IDs for resource managers that are used in this transaction. 
	 */
	public int[] getRMIDs();

	/**	 
	 * @return Array with branch iterators for resource managers that are used in this transaction. 
	 * This array is a parallel array of the rmIDS. Branch iterators are used only for non shareable resources.
	 * Usually branch iterators are 0. 
	 */
	public byte[] getBranchIterators();
	
}
