/*
 * Created on 21.02.2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.sl.util.components.impl;

import com.sap.sl.util.components.api.SCLevelIF;

/**  
 * @author d036263
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SCLevel implements SCLevelIF {

	String release = null;
	String serviceLevel = null;
	String patchLevel = null;
							
	protected SCLevel (String _release,
	String _serviceLevel,
	String _patchLevel) {
		
		release = _release;
		serviceLevel = _serviceLevel;
		patchLevel = _patchLevel;
	}
	
	public String getRelease () {
		return release;
	}
	
	public String getServiceLevel () {
		return serviceLevel;
	}
	
	public String getPatchLevel () {
		return patchLevel;
	}
	
	public String getXML () {

		return ("release=\"" + release + "\" " +
			"servicelevel=\"" + this.serviceLevel+"\" " + 
			"patchlevel=\"" + this.patchLevel + "\" ");
	}
	
	public String toString () {

		return "rel=" + release + " sl=" + this.serviceLevel+" pl=" + this.patchLevel;
	}
	
}
