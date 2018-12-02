/*
 * Created on 15.01.2007
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.sl.util.cvers.api;

import com.sap.sl.util.components.api.ComponentElementIF;

/**
 * @author d036263
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface SyncElementStatusIF {
	
	/*
	 * Returns the exception, which caused the sync failure.
	 * Might be null, if no exception caused the failure.
	 */
	public Throwable getReason ();
	
	/**
	 * Returns the error text of the sync failure.
	 */
	public String getErrorText ();
	
	/**
	 * Returns the ComponentElement, which could not be synced.
	 */
	public ComponentElementIF getElement ();
	
}
