/*
 * Created on 24.02.2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.sl.util.components.api;

/**
 * Defines a required software component version.
 * 
 * @author d036263
 */
public interface SCRequirementIF extends SoftwareComponentIF {
	
	/**
	 * Returns the component levels of the required component.
	 */
	public SCLevelIF getComponentLevel ();
	
	/**
	 * Returns the provider of the current required component.
	 */	
	public String getProvider ();
	
	/**
	 * Returns the current data as XML string.
	 */
	
	public String getXML ();
	
}
