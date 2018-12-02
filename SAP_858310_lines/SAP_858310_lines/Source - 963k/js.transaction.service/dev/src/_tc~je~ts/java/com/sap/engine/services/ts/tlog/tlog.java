package com.sap.engine.services.ts.tlog;

import java.util.Iterator;

import com.sap.engine.interfaces.transaction.RMProps;
import com.sap.engine.services.ts.utils.TLogVersion;


/**
 * This interface represents one logical TLog which is maintained from 
 * one TransactionManager instance or from one Recovery subsystem. 
 * TLog can be store on file system or into DBMS.  
 * 
 * @author I024163
 */
public interface TLog {
	
	/**
	 * Returns the stored resource manager name into TLog for given id of resource manager.
	 * @param id
	 * @return resource manager name by given id
	 * @throws TLogIOException when unexpected IOException or SQLException occurred.
	 * @throws InvalidRMIDException when there is no such id in TLog or it is negative or 0
	 */
	public String getRMName(int id) throws TLogIOException, InvalidRMIDException;
	
	/**
	 * Return the stored classifier into TLog for given id
	 * 
	 * @param id
	 * @return classifier associated with given id
	 * @throws TLogIOException when unexpected IOException or SQLException occurred.
	 * @throws InvalidClassifierIDException when there is no such id in TLog or it is negative or 0
	 */
	public String getTxClassifierById(int id) throws TLogIOException, InvalidClassifierIDException; 
	
	/**
	 * Stores specified classifier into TLog if it was not stored before.
	 * 
	 * @param classifier the classifier of the transaction which will be stored
	 * @return the ID of this classifier. This ID must be positive integer if provided string is not null and not empty.
	 * @throws TLogFullException when max number of different transaction classifiers is reached. 
	 */
	public int getIdForTxClassifier(String classifier) throws TLogIOException, TLogFullException ;
	
	/**
	 * 
	 * Used from recovery subsystem to retrieve all resource manager IDs which were used into this 
	 * logical TLog.
	 * 
	 * @return array of all resource manager IDs which were used into this logical TLog.
	 * @throws TLogIOException when unexpected IOException or SQLException occurred.
	 */
	public int[] getAllUsedRMIDs() throws TLogIOException;
	
	/**
	 * 
	 * Used from recovery subsystem to retrieve properties of the resource manager specified with provided ID.
	 * These properties are used for XAResource recreation.  
	 * 
	 * @param rmID ID of the resource manager.
	 * @return properties which were provided during registration of the specified resource manager
	 * @throws TLogIOException when unexpected IOException or SQLException occurred.
	 */
	public RMPropsExtension getRMProperties(int rmID) throws TLogIOException, InvalidRMKeyException; 
	
	/**
	 * 
	 * Used from TransactionManager to register resource manager into TLog before first usage. This method
	 *  will be used also when properties of the RM-s are changed. Previous RM properties will be deleted when 
	 *  all transactions which were using previous properties are completed.   
	 *	
	 * @param rmProps which contains unique name, secure and non secure properties of the resource manager.  
	 * @return ID for registered resource manager. The ID is positive and unique whitin this TLog.
	 * @throws TLogIOException when unexpected IOException or SQLException occurred.
	 */
	public int registerNewRM(RMProps rmProps) throws TLogIOException, RMNameAlreadyInUseException;
	
	/** 
	 * Used from recovery subsystem to retrieve ID of the specified resource manager. This ID is used for RM unregistration. 
	 * 
	 * @param keyName unique name of the resource manager.
	 * @return ID of the resource manager or 0 if specified resource manager does not exist. 
	 * @throws TLogIOException when unexpected IOException or SQLException occurred.
	 * @throws InvalidRMKeyException when specified resource manager name is null or resource manager does not exist into TLog. 
	 */
	public int getRMIDByName(String keyName) throws TLogIOException, InvalidRMKeyException;
	 
	/**
	 * Method is used from TransactionManager and recovery subsystem when specified resource manager is not used and will not be used.
	 * Properties for specified resource manager will be deleted physically from TLog only when there are no pending transactions which are using this RM. 
	 * 
	 * @param rmID ID of the resource manager
	 * @throws TLogIOException when unexpected IOException or SQLException occurred.
	 * @throws InvalidRMIDException when specified resource manager name is null or resource manager does not exist into TLog.
	 */
	public void unregisterRM(int rmID) throws TLogIOException, InvalidRMIDException;

	/**
	 * Method is used from TransactionManager to store a record for each transaction 
	 * which is successfully prepared.
	 * 
	 * @param transactionRecord record for one transaction which holds transaction name, global transactionID, 
	 * resource manager IDs and branch iterators.
	 * @throws TLogIOException when unexpected IOException or SQLException occurred.
	 * @throws InvalidRMIDException when some of the provided resource manager IDs are not valid.
	 */
	public void writeTransactionRecord(TransactionRecord transactionRecord)
							throws TLogIOException, InvalidRMIDException, InvalidTransactionClassifierID;   

	
	/**
	 * Used from recovery subsystem to get all transactions which were not completed successfully. 
	 *  
	 * @return all available transaction records into this TLog.
	 * @throws TLogIOException when unexpected IOException or SQLException occurred.
	 */
	public Iterator<TransactionRecord> getAllTransactionRecords() throws TLogIOException;
	
	
	/**
	 * Removes specified transaction record from TLog. 
	 * For optimization purposes it is possible to remove physically this record later.
	 * 
	 * @param logID is the transaction sequence number
	 * @throws TLogIOException when unexpected IOException or SQLException occurred.
	 */
	public void removeTransactionRecordLazily(long txSequenceNumber) throws TLogIOException;
	
	
	/**
	 * Removes specified transaction record from TLog. immediately 
	 * Will be used during recovery and in all cases when logID is not known. 
	 * Physical record will be removed immediately. This method does not provide asynchronous deletion.
	 * It will be used also when transaction is prepared but commit of the first RM failed and TM decides
	 * to rollback the transaction. 
	 * 
	 * @param logID is the transaction sequence number
	 * @throws TLogIOException when unexpected IOException or SQLException occurred.
	 */
	public void removeTransactionRecordImmediately(long txSequenceNumber) throws TLogIOException;

	/**
	 * Remove physically all transaction records which are prepared for remove but
	 * are still not removed physically
	 * @throws TLogIOException when unexpected IOException or SQLException occurred.
	 */
	public void flushRemovedTransactionRecords() throws TLogIOException;

	/**
	 * @return the version of this TLog. Returned object contains CID of the cluster where this TLog 
	 * was created, ID of the server node where this TLog was created and the time where the TLog was 
	 * created.  
	 */
	public TLogVersion getTLogVersion();
	/**
	 * Closes the transaction log. All caches will be flushed and all locks released. TLog will be deleted physically 
	 * if there are no pending transactions into it.
	 * @throws TLogIOException when unexpected IOException or SQLException occurred.
	 */
	public void close() throws TLogIOException;
}
