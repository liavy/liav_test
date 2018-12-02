package com.sap.engine.services.ts.tlog.util;

/**
 * This interface will be used from TLogReaderWriters to maintain memory cache with transaction names. 
 * To prevent frequent remove and insert operations unused transaction names will be removed from the
 * cache after some timeout or more sophisticated algorithm will be used. 
 * 
 * @author I024163
 *
 */
public interface TransactionNamesCache {
	
	/**
	 * Registers an array of transaction names into the cache. This method will be used from recovery subsystem. 
	 * @param transactionNames array of transaction names which are stored into physical TLog.
	 */
	public void registerActiveTransactions(String[] transactionNames);
	
	/**
	 * Retrieve unique id whitin this cache for specified transaction name. If transaction name does
	 * not exist into cache it will be added and and newly generated id will be returned. Invocation of this method
	 * means that specified transaction name is used. 
	 *  
	 * @param transactionName transaction name.
	 * @return unique id for the specified transaction name.
	 */
	public int getTransactionID(String transactionName);
	
	/**
	 * Invoked when Transaction is completed and physical record for it is removed from the TLog. 
	 * @param transactionName
	 */
	public void transactionCompleted(String transactionName);
	
	/**
	 * TLogReaderWriters will register listeners into this cache. Listener will be notified when cache
	 * managements decides to remove one or more transaction names from the cache. TLogReaderWriter must
	 * remove transaction names from physical storage.    
	 * @param transactionNameRemoveListener the listener which will be notified when transaction 
	 * names are removed from the cache
	 */
	public void registerTransactionNameRemoveListener(TransactionNameRemoveListener transactionNameRemoveListener);

}
