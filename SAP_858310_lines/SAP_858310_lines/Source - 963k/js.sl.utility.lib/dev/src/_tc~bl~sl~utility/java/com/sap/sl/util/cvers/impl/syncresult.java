/*
 * Created on 15.01.2007
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.sl.util.cvers.impl;

import com.sap.sl.util.components.api.ComponentElementIF;
import com.sap.sl.util.cvers.api.SyncElementStatusIF;
import com.sap.sl.util.cvers.api.SyncResultIF;

/**
 * @author d036263
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SyncResult implements SyncResultIF {
	
	ComponentElementIF[] successful;
	SyncElementStatusIF[] failed;
	
	SyncResult (ComponentElementIF[] success, SyncElementStatusIF[] fail) {
		successful = success;
		failed = fail;
	}
	
	public ComponentElementIF[] successfulSyncs () {	// all successful synced component versions. Might be empty array. Never null
		return successful;
	}
	
	public SyncElementStatusIF[] failedSyncs () {			// all component versions, which could not be synced. Might be empty array. Never null
		return failed;
	}

}
