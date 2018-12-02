package com.sap.engine.services.ts.facades.timer;

public interface TimeoutListener {

	/**
	 * Checks if the <code>timeout</code> method should be called
	 * 
	 * @return true if the <code>timeout</code> method should be called
	 */
	public boolean check();

	/**
	 * Called when after the timeout has expired but only if <code>check</code>
	 * method returns true
	 */
	public void timeout();

	/**
	 * Used from the <code>TimeoutManager</code> implementations to
	 * associate/get <code>TimeoutListener</code> wrapper
	 * 
	 * @param obj
	 *            the Object to be associated
	 */
	public void associateObject(Object obj);

	/**
	 * Used from the <code>TimeoutManager</code> implementations to
	 * associate/get <code>TimeoutListener</code> wrapper
	 * 
	 * @return associated with <code>associateObject</code> Object
	 */
	public Object getAssociateObject();
}
