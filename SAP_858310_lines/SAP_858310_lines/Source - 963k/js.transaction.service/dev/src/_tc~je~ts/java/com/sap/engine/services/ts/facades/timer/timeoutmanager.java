package com.sap.engine.services.ts.facades.timer;

public interface TimeoutManager {

	/**
	 * Registers TimeoutListner.
	 * 
	 * @param listener
	 *            - the TimeoutListener to be registered.
	 * @param delayTime
	 *            - Shows after how much time in milliseconds the
	 *            <code>timeout</code> method of the <code>listener</code> will
	 *            be invoked.
	 * @param repeatTime
	 *            - if is <= 0 than the the <code>timeout</code> method of the
	 *            the <code>listener</code> will be invoked only once, otherwise
	 *            it shows how much will be the time in milliseconds between two
	 *            successive calls of the <code>timeout</code> method of the
	 *            <code>listener</code>
	 */
	public void registerTimeoutListener(TimeoutListener listener,
			long delayTime, long repeatTime);

	/**
	 * Unregisters already registered TimeoutListener.
	 * 
	 * @param listener
	 *            - the TimeoutListener to be unregistered
	 */
	public void unregisterTimeoutListener(TimeoutListener listener);
}
