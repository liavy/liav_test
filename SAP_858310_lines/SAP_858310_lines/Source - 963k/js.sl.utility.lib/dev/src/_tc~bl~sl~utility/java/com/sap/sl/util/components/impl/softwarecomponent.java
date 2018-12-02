/*
 * Created on 21.02.2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.sl.util.components.impl;

import com.sap.sl.util.components.api.SoftwareComponentIF;

/**  
 * @author d036263
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SoftwareComponent implements SoftwareComponentIF {

	protected String name = null;
	protected String vendor = null;
	
	public String getXML () {
		
	return ("name=\"" + name + "\"" +
		"vendor=\"" + this.vendor+"\"");
	}
	
	protected SoftwareComponent (String _vendor, String _name) {
		name = _name;
		vendor = _vendor;
	}

	public String getName () {
		return name;
	}
	
	public String getVendor () {
		return vendor;
	}
}
