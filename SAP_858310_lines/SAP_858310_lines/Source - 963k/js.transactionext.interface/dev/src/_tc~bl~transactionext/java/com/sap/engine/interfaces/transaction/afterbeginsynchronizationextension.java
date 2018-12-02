package com.sap.engine.interfaces.transaction;

/**
 * 	The AfterBeginSynchronizationExtension interface allows applications and services to 
 * be notified by transaction management when Synchronization instance is effectively 
 * registered into transaction. This interface is useful when one module provides implementation 
 * of synchronization interface and another module is responsible for registration into 
 * transaction.    
 * 
 * @author I024163 - nikolai.tankov@sap.com
 *
 */
public interface AfterBeginSynchronizationExtension extends SynchronizationPriorityExtension {
	
	/**
	 * 	Called from transaction management when this transaction synchronization listener 
	 * is registered into transaction. 
	 * <p>
	 *  This method is called into context of enlisted transaction.   
	 */
	public void afterBegin();

}
