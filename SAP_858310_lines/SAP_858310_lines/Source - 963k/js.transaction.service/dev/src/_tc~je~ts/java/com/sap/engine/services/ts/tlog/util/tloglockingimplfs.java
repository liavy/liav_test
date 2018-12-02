package com.sap.engine.services.ts.tlog.util;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

public class TLogLockingImplFS implements TLogLocking {

	protected final int defaultTimeout;
	
	private static TLogLockingImplFS instance = null;

	protected TLogLockingImplFS(int defaultTimeout) {
		this.defaultTimeout = defaultTimeout;
	}
	
	public static TLogLockingImplFS getInstance(int defaultTimeout) {
		if(instance == null) {
			instance = new TLogLockingImplFS(2000);
		}
		return instance;
	}
	
	Set<String> list = new HashSet<String>();
	
	public void lockTLog(String tlogVersion) throws TLogLockingException,
			TLogLockingInfrastructureException {
		if (tlogVersion == null || tlogVersion.length() == 0) {
			throw new IllegalArgumentException("The argument log ID must not be null or empty.");
		}
		
		if(!list.contains(tlogVersion)) {
			synchronized (list) {
				if(!list.contains(tlogVersion)) {
					list.add(tlogVersion);
				} else {
					throw new TLogLockingException("Already locked");
				}
			}
		} else {
			throw new TLogLockingException("Already locked");
		}
		
	}

	public void obtainTLogLock(String lock)
			throws TLogLockingInfrastructureException, TimeoutException {
		obtainTLogLock(lock, defaultTimeout);
	}

	public void obtainTLogLock(String lock, int timeout)
			throws TLogLockingInfrastructureException, TimeoutException {
		boolean obtained = false;
		synchronized (list) {
			if (!list.contains(lock)) {
				list.add(lock);
				obtained = true;
			}
		}
		if (!obtained) {
			long endTime = System.currentTimeMillis() + timeout;
			do {
				try {
					Thread.sleep(50L);
				} catch (InterruptedException e) {
					//$JL-EXC$
				}

				synchronized (list) {
					if (!list.contains(lock)) {
						list.add(lock);
						obtained = true;
						break;
					}
				}
			} while (System.currentTimeMillis() <= endTime);
		}

		if (!obtained)
			throw new TimeoutException("Cannot obtain lock.");
	}

	public void unlockTLog(String logID)
			throws TLogLockingInfrastructureException {
		synchronized (list) {
			list.remove(logID);
		}
	}

}
