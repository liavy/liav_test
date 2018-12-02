package com.sap.engine.services.ts.tlog.util;

import java.util.concurrent.TimeoutException;

import com.sap.engine.frame.core.locking.LockException;
import com.sap.engine.frame.core.locking.ServerInternalLocking;
import com.sap.engine.frame.core.locking.TechnicalLockException;
import com.sap.engine.services.ts.TransactionServiceFrame;

public class TLogLockingImpl implements TLogLocking {

	protected static final String USERNAME = "TransactionLog";

	protected final ServerInternalLocking locking;

	public TLogLockingImpl(ServerInternalLocking locking) {
		this.locking = locking;
	}

	public void lockTLog(String lock) throws TLogLockingException,
			TLogLockingInfrastructureException {
		if (lock == null || lock.length() == 0) {
			throw new IllegalArgumentException(
					"The argument log ID must not be null or empty.");
		}

		try {
			locking.lock(LOCKING_NAMESPACE, lock,
					ServerInternalLocking.MODE_EXCLUSIVE_NONCUMULATIVE,
					USERNAME);

		} catch (LockException e) {
			throw new TLogLockingException("Lock already exists.", e);

		} catch (TechnicalLockException e) {
			throw new TLogLockingInfrastructureException(
					"Error when trying to obtain lock.", e);
		}
	}

	public void obtainTLogLock(String lock)
			throws TLogLockingInfrastructureException, TimeoutException {
		obtainTLogLock(lock, TransactionServiceFrame.lockingTimeout);
	}

	public void obtainTLogLock(String lock, int timeout)
			throws TLogLockingInfrastructureException, TimeoutException {
		if (lock == null || lock.length() == 0) {
			throw new IllegalArgumentException(
					"The argument lock must not be null or empty.");
		}

		try {
			locking.lock(LOCKING_NAMESPACE, lock,
					ServerInternalLocking.MODE_EXCLUSIVE_NONCUMULATIVE,
					timeout, USERNAME);

		} catch (LockException e) {
			TimeoutException timeoutException = new TimeoutException("Cannot obtain lock.");
			timeoutException.initCause(e);
			throw timeoutException;
		} catch (TechnicalLockException e) {
			throw new TLogLockingInfrastructureException(
					"Error when trying to obtain lock.", e);
		}
	}

	public void unlockTLog(String lock)
			throws TLogLockingInfrastructureException {
		if (lock == null || lock.length() == 0) {
			throw new IllegalArgumentException(
					"The argument lock must not be null or empty.");
		}

		try {
			locking.unlock(LOCKING_NAMESPACE, lock,
					ServerInternalLocking.MODE_EXCLUSIVE_NONCUMULATIVE);
		} catch (TechnicalLockException e) {
			throw new TLogLockingInfrastructureException(
					"Error when trying to release lock.", e);
		}
	}

}
