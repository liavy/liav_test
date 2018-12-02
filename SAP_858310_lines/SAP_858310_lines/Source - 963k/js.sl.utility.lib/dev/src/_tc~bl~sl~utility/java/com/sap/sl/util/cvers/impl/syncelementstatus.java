/*
 * Created on 16.01.2007
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.sl.util.cvers.impl;

import com.sap.sl.util.components.api.ComponentElementIF;
import com.sap.sl.util.cvers.api.SyncElementStatusIF;

/**
 * @author d036263
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SyncElementStatus implements SyncElementStatusIF {
	
	private Throwable reason;
	private String errorText;
	private ComponentElementIF element;
	
	public SyncElementStatus (ComponentElementIF _element, String _errorText, Throwable _reason) {
		reason = _reason;
		errorText = _errorText;
		element = _element;
	}
	
	/*
	 * Returns the exception, which caused the sync failure.
	 * Might be null, if no exception caused the failure.
	 */
	public Throwable getReason () {
		return reason;
	}

	/**
	 * Returns the error text of the sync failure.
	 */
	public String getErrorText () {
		return errorText;
	}

	/**
	 * Returns the ComponentElement, which could not be synced.
	 */
	public ComponentElementIF getElement () {
		return element;
	}

}
