/*
 * Created on 20.02.2005
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.sap.sl.util.components.api;

/**
 * Defines name and vendor of a software component.
 * 
 * @author jm
 *
 */
public interface SoftwareComponentIF {

	/**
	 * Returns the name of the software component.
	 */
	public String getName ();
	
	/**
	 * Returns the vendor of the software component.
	 */
	public String getVendor ();

	/**
	 * Returns the current data as XML string.
	 */
	
	public String getXML ();
	
	
}
