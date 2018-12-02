package com.sap.engine.services.ts.jta;

import com.sap.engine.interfaces.transaction.TransactionExtension;


/**
 * This interface is used to allow usage of 2 switchable implementations of transaction management. Previous implementation 
 * which does not support transaction recovery and new implementation that supports recovery. When only one implementation is 
 * used this interface must be deteled. 
 * 
 * @author I024163
 *
 */
public interface TransactionInternalExtension extends TransactionExtension {

	
	public void removeSynchronizationStackWhenCompleted();
	
}
