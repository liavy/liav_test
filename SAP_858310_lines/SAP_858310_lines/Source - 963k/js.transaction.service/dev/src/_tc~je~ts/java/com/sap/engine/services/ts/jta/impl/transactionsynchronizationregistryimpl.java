package com.sap.engine.services.ts.jta.impl;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionSynchronizationRegistry;

import com.sap.engine.interfaces.transaction.TransactionExtension;
import com.sap.engine.interfaces.transaction.TransactionManagerExtension;
import com.sap.engine.services.ts.LogUtil;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

public class TransactionSynchronizationRegistryImpl implements TransactionSynchronizationRegistry {

	private static final Location LOCATION = Location.getLocation(TransactionSynchronizationRegistryImpl.class);	
	private TransactionManagerExtension transactionManager = null;
	
	
	public TransactionSynchronizationRegistryImpl(TransactionManagerExtension transactionManager){
		this.transactionManager = transactionManager;		
	}
	
	
    /**  
      * Return an opaque object to represent the transaction bound to the  
      * current thread at the time this method is called. This object  
      * overrides hashCode and equals to allow its use as the key in a  
      * hashMap for use by the caller. If there is no transaction currently  
      * active, return null.  
      *   
      * <P>This object will return the same hashCode and compare equal to  
      * all other objects returned by calling this method  
      * from any component executing in the same transaction context in the  
      * same application server.  
      *   
      * <P>The toString method returns a String that might be usable by a  
      * human reader to usefully understand the transaction context. The  
      * toString result is otherwise not defined. Specifically, there is no  
      * forward or backward compatibility guarantee of the results of  
      * toString.  
      *   
      * <P>The object is not necessarily serializable, and has no defined  
      * behavior outside the virtual machine whence it was obtained.  
      *  
      * @return an opaque object representing the transaction bound to the   
      * current thread at the time this method is called.  
      */
	public Object getTransactionKey(){		
		try {
			return transactionManager.getTransaction();
		} catch (SystemException e) {
		    if (LOCATION.beLogged(Severity.DEBUG)) {
//		        LOCATION.logT(Severity.DEBUG, "SystemException in getTransaction " + e.toString());
		        SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000164", "SystemException in getTransaction {0}", new Object[] {e.toString()});
		    }
            return null;// transaction key is not available            
		}		
	}
	
    /**  
	  * Add or replace an object in the Map of resources being managed for  
	  * the transaction bound to the current thread at the time this   
	  * method is called. The supplied key should be of an caller-  
	  * defined class so as not to conflict with other users. The class  
	  * of the key must guarantee that the hashCode and equals methods are  
	  * suitable for use as keys in a map. The key and value are not examined  
	  * or used by the implementation. The general contract of this method  
	  * is that of {@link java.util.Map#put(Object, Object)} for a Map that  
	  * supports non-null keys and null values. For example,   
	  * if there is already an value associated with the key, it is replaced   
	  * by the value parameter.   
	  *  
	  * @param key the key for the Map entry.  
	  * @param value the value for the Map entry.  
	  * @exception IllegalStateException if no transaction is active.  
	  * @exception NullPointerException if the parameter key is null.  
	  *  
   	  */  
	public void putResource(Object key, Object value) throws IllegalStateException, NullPointerException{
		if(key == null){
			throw new NullPointerException(LogUtil.getFailedInComponentByCaller() + "key parameter is null");			
		}
		
		TransactionExtension transaction = null;
		try {
			transaction = (TransactionExtension) transactionManager.getTransaction();
		} catch (SystemException e) {
		    if (LOCATION.beLogged(Severity.DEBUG)) {
//		        LOCATION.logT(Severity.DEBUG, "SystemException in getTransaction " + e.toString());
		        SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000165", "SystemException in getTransaction {0}", new Object[] {e.toString()});
		    }
			throw new IllegalStateException("Cannot access transaction object", e);
		}
		
		if(transaction == null){
			throw new IllegalStateException(LogUtil.getFailedInComponentByCaller() + "No active transaction");			
		}
		
		try {
			transaction.associateObjectWithTransaction(key,value);
		} catch (SystemException e) {
		    if (LOCATION.beLogged(Severity.DEBUG)) {
		        LOCATION.logT(Severity.DEBUG, "SystemException in associateObjectWithTransaction " + e.toString());
		        SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000166", "SystemException in associateObjectWithTransaction {0}", new Object[] {e.toString()});
		    }
			throw new IllegalStateException("Cannot access transaction object", e);
		}
	}
	
    /**  
	  * Get an object from the Map of resources being managed for  
	  * the transaction bound to the current thread at the time this   
	  * method is called. The key should have been supplied earlier  
	  * by a call to putResouce in the same transaction. If the key   
	  * cannot be found in the current resource Map, null is returned.  
	  * The general contract of this method  
	  * is that of {@link java.util.Map#get(Object)} for a Map that  
	  * supports non-null keys and null values. For example,   
	  * the returned value is null if there is no entry for the parameter  
	  * key or if the value associated with the key is actually null.  
	  *  
	  * @param key the key for the Map entry.  
	  * @return the value associated with the key.  
	  * @exception IllegalStateException if no transaction is active.  
	  * @exception NullPointerException if the parameter key is null.  
	  *  
	  */  
	public Object getResource(Object key) throws IllegalStateException, NullPointerException{		
		if(key == null){
			throw new NullPointerException(LogUtil.getFailedInComponentByCaller() + "key parameter is null");			
		}
		
		TransactionExtension transaction = null;
		try {
			transaction = (TransactionExtension) transactionManager.getTransaction();
		} catch (SystemException e) {
		    if (LOCATION.beLogged(Severity.DEBUG)) {
//		        LOCATION.logT(Severity.DEBUG, "SystemException in getTransaction " + e.toString());
		        SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000167", "SystemException in getTransaction {0}", new Object[] {e.toString()});
		    }
			throw new IllegalStateException("Cannot access transaction object", e);
		}
		
		if(transaction == null){
			throw new IllegalStateException(LogUtil.getFailedInComponentByCaller() + "No active transaction");			
		}
		
		return transaction.getAssociatedObjectWithTransaction(key);
	}
	
    /**  
	  * Register a Synchronization instance with special ordering  
	  * semantics. Its beforeCompletion will be called after all   
	  * SessionSynchronization beforeCompletion callbacks and callbacks   
	  * registered directly with the Transaction, but before the 2-phase  
	  * commit process starts. Similarly, the afterCompletion  
	  * callback will be called after 2-phase commit completes but before  
	  * any SessionSynchronization and Transaction afterCompletion callbacks.  
	  *   
	  * <P>The beforeCompletion callback will be invoked in the transaction  
	  * context of the transaction bound to the current thread at the time   
	  * this method is called.   
	  * Allowable methods include access to resources,  
	  * e.g. Connectors. No access is allowed to "user components" (e.g. timer  
	  * services or bean methods), as these might change the state of data  
	  * being managed by the caller, and might change the state of data that  
	  * has already been flushed by another caller of   
	  * registerInterposedSynchronization.   
	  * The general context is the component  
	  * context of the caller of registerInterposedSynchronization.  
	  *   
	  * <P>The afterCompletion callback will be invoked in an undefined   
	  * context. No access is permitted to "user components"  
	  * as defined above. Resources can be closed but no transactional  
	  * work can be performed with them.  
	  *   
	  * <P>If this method is invoked without an active transaction context, an  
	  * IllegalStateException is thrown.  
	  *   
	  * <P>Other than the transaction context, no component J2EE context is  
	  * active during either of the callbacks.  
	  *  
	  * @param sync the Synchronization instance.  
	  * @exception IllegalStateException if no transaction is active.  
	  *  
  	  */
	public void registerInterposedSynchronization(Synchronization sync) throws IllegalStateException{		
		//afterCompletion will be invoked AFTER sessionSynchronizations
		// this is not so correct and must be changed
		TransactionExtension transaction = null;
		try {
			transaction = (TransactionExtension) transactionManager.getTransaction();
		} catch (SystemException e) {
		    if (LOCATION.beLogged(Severity.DEBUG)) {
//		        LOCATION.logT(Severity.DEBUG, "SystemException in getTransaction " + e.toString());
		        SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000168", "SystemException in getTransaction {0}", new Object[] {e.toString()});
		    }
			throw new IllegalStateException("Cannot access transaction object", e);
		}
		
		if(transaction == null){
			throw new IllegalStateException(LogUtil.getFailedInComponentByCaller() + "No active transaction");			
		}
		
		try {
			transaction.registerSynchronization(new SynchronizationWrapper(sync));		
		} catch (RollbackException e) {
		    if (LOCATION.beLogged(Severity.DEBUG)) {
//		        LOCATION.logT(Severity.DEBUG, "RollbackException in registerSynchronization " + e.toString());
		        SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000169", "RollbackException in registerSynchronization {0}", new Object[] {e.toString()});
		    }
			throw new IllegalStateException("Transaction is marked for rollback", e);
		} catch (SystemException e) {
		    if (LOCATION.beLogged(Severity.DEBUG)) {
//		        LOCATION.logT(Severity.DEBUG, "SystemException in registerSynchronization " + e.toString());
		        SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000170", "SystemException in registerSynchronization {0}", new Object[] {e.toString()});
		    }
			throw new IllegalStateException("Cannot access transaction object", e);
		}
		
	}

    /**  
	  * Return the status of the transaction bound to the  
	  * current thread at the time this method is called.  
	  * This is the result of executing TransactionManager.getStatus() in  
	  * the context of the transaction bound to the current thread at the time  
	  * this method is called.  
	  *  
	  * @return the status of the transaction bound to the current thread   
	  * at the time this method is called.  
	  *  
      */
	public int getTransactionStatus(){		
		try {
			return transactionManager.getStatus();
		} catch (SystemException e) {
		    if (LOCATION.beLogged(Severity.DEBUG)) {
//		        LOCATION.logT(Severity.DEBUG, "SystemException in getStatus " + e.toString());
		        SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000171", "SystemException in getStatus {0}", new Object[] {e.toString()});
		    }
			return Status.STATUS_UNKNOWN;
		}
	}
	
    /**  
	  * Set the rollbackOnly status of the transaction bound to the  
	  * current thread at the time this method is called.  
	  *  
	  * @exception IllegalStateException if no transaction is active.  
	  *  
	  */
	public void setRollbackOnly() throws IllegalStateException{
		try {
			transactionManager.setRollbackOnly();		
		} catch (SystemException e) {
		    if (LOCATION.beLogged(Severity.DEBUG)) {
//		        LOCATION.logT(Severity.DEBUG, "SystemException in setRollbackOnly " + e.toString());
		        SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000172", "SystemException in setRollbackOnly {0}", new Object[] {e.toString()});
		    }
			throw new IllegalStateException("Cannot access transaction object", e);
		}
	}

    /**  
	  * Get the rollbackOnly status of the transaction bound to the  
	  * current thread at the time this method is called.  
	  *  
	  * @return the rollbackOnly status.  
	  * @exception IllegalStateException if no transaction is active.  
	  *  
	  */
	public boolean getRollbackOnly() throws IllegalStateException{
		int status = Status.STATUS_UNKNOWN;
		
		try {
			status = transactionManager.getStatus();
		} catch (SystemException e) {
		    if (LOCATION.beLogged(Severity.DEBUG)) {
//		        LOCATION.logT(Severity.DEBUG, "SystemException in getStatus " + e.toString());
		        SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000173", "SystemException in getStatus {0}", new Object[] {e.toString()});
		    }
			throw new IllegalStateException("Cannot access transaction object", e);
		}
		
		if(status == Status.STATUS_NO_TRANSACTION || status == Status.STATUS_UNKNOWN){
			throw new IllegalStateException(LogUtil.getFailedInComponentByCaller() + "No active transaction");		
		}
		
		return status == Status.STATUS_MARKED_ROLLBACK;
	}
	
}


