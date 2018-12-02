package com.sap.engine.services.ts.tlog.util;

import java.util.concurrent.TimeoutException;

/**
 * Used from LogReaderWriter-s to lock TLog. The implementation is using enqueue locks.
 * 
 * @author I024163
 */
public interface TLogLocking {

	/**
	 * Owner name of all locks for the TLogLocking infrastructure
	 */
	public  static final String owner = "Transaction Manager";

	/**
	 * Namespace of the locks for the TLogLocking infrastructure
	 */
	public static final String LOCKING_NAMESPACE = "$ser.ts";

	/**
	 * Creates pessimistic lock with specified argument. 
	 * 
	 * @param tLogID argument and ID of the lock. 
	 * @throws TLogLockingException when this lock was already created.	
	 * @throws TLogLockingInfrastructureException when there are internal problems with enqueue.
	 */
	public void lockTLog(String tLogID) throws TLogLockingException, TLogLockingInfrastructureException;
	
	/**
	 * Releases the lock with specified argument. 
	 * 
	 * @param tLogID argument and ID of the lock.
	 * @throws TLogLockingInfrastructureException when there are internal problems with enqueue.
	 */
	public void unlockTLog(String tLogID) throws TLogLockingInfrastructureException;

	/**
	 * If specified lock exist wait to be released and obtain it. You must release the
	 * lock after you are done with it.
	 * @param lock the lock you want
	 * @throws TLogLockingInfrastructureException when there are internal problems with enqueue
	 * @throws TimeoutException if you wait too long for the lock
	 */
	public void obtainTLogLock(String lock) throws TLogLockingInfrastructureException, TimeoutException;

	/**
	 * If specified lock exist wait timeout milliseconds to be released and obtain it.
	 * You must release the lock after you are done with it.
	 * @param lock the lock you want
	 * @param timeout time to wait for the lock
	 * @throws TLogLockingInfrastructureException when there are internal problems with enqueue
	 * @throws TimeoutException if the timeout has expired
	 */
	public void obtainTLogLock(String lock, int timeout)
											throws TLogLockingInfrastructureException, TimeoutException;
}
