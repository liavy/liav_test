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
public interface SyncResultIF {
	
	public ComponentElementIF[] successfulSyncs ();	// all successful synced component versions. Might be empty array. Never null
	
	public SyncElementStatusIF[] failedSyncs ();			// all component versions, which could not be synced. Might be empty array. Never null

}
