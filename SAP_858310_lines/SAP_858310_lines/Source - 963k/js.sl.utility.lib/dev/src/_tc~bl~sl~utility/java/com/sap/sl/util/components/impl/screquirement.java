/*
 * Created on 21.02.2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.sl.util.components.impl;

import com.sap.sl.util.components.api.SCLevelIF;
import com.sap.sl.util.components.api.SCRequirementIF;

/**  
 * @author d036263
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SCRequirement extends SoftwareComponent implements SCRequirementIF {

	private String provider = null;
	private SCLevelIF level = null;
							
	public String getXML () {

		return ("<screquirement name=\"" + name + "\" " +
			"vendor=\"" + this.vendor+"\" " + 
			"provider=\"" + this.provider + "\" " + level.getXML() + " />");
	}
	
	protected SCRequirement (String _vendor, 
	String _name,
	String _provider,
	SCLevelIF _level) {
		
	super (_vendor, _name);
		provider = _provider;
		level = _level;
	}
	
	public String getProvider () {
		return provider;
	}
	
	public SCLevelIF getComponentLevel () {
		return level;
	}
	
	public String toString () {
		return "SCRequirement: N='"+super.name+"' V='"+super.vendor+"' P='"+provider+"' "+level.toString();
	}
	
}
