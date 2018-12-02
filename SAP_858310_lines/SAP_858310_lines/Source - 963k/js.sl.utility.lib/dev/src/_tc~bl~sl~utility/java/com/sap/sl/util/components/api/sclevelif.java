/*
 * Created on 20.02.2005
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.sap.sl.util.components.api;

/**
 * Defines delivery version information of a software component
 * needed for component identification by CVL (common version library).
 * 
 * @author jm
 *
 */
public interface SCLevelIF {
	
	/**
	 * Returns the release of the software component.
	 */
	public String getRelease ();
	
	/**
	 * Returns the support package (service) level of the software component.
	 */
	public String getServiceLevel ();
	
	/**
	 * Returns the patchlevel of the software component.
	 */
	public String getPatchLevel ();

	/**
	 * Returns the current data as XML string.
	 */
	
	public String getXML ();
	
}
