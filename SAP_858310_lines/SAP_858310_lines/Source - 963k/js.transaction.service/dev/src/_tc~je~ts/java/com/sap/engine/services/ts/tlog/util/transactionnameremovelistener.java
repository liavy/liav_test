package com.sap.engine.services.ts.tlog.util;

/**
 * This interface is implemented from TLogReaderWriters in order to receive events when some transaction
 * names are removed from the cache. 
 * @author I024163
 *
 */
public interface TransactionNameRemoveListener {
	
	/**
	 * Method is invoked from TransactionNamesCache when one or more transaction names were not used
	 * for a long time were removed from the cache. Method will remove transaction names from physical storage.  
	 * 
	 * @param transactionNames an array of transaction names which are removed. 
	 */
	public void transactionNamesRemoved(String[] transactionNames);

}
