/*
 * Created on 20.02.2005
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.sap.sl.util.components.api;


/**
 * Defines a software component version.
 * 
 * @author jm
 *
 */
public interface SCVersionIF extends SoftwareComponentIF {

	/**
	 * Returns the software provider of the software component.
	 */
	public String getProvider ();
	
	/**
	 * Returns the location of the software component.
	 * 
	 */
	public String getLocation ();
	
	/**
	 * Returns the counter of the software component.
	 * 
	 */
	public String getCounter ();
	
	/*
	 * Returns component levels of current component.
	 */
	 
	 public SCLevelIF getComponentLevel ();
	 
	/**
	 * Checks, if the current component has a predecessor.
	 */
	public boolean hasMoreElements ();
	
	/**
	 * Returns the predecessor of the current component.
	 * Returns 'null' if no predecessor exists.
	 */	
	public SCVersionIF getNextElement ();

	/**
	 * Sets the source pointer of the current component.
	 */
	public void setSourcePointer (SCVersionIF history);
	
	/**
	 * Sets the counter of the current component.
	 */
	public void setCounter (String counter);
	
	/**
	 * Sets the location of the current component.
	 */
	public void setLocation (String location);
	
	/**
	 * Sets the provider of the current component.
	 */
	public void setProvider (String provider);
	
	/**
	 *  Gets all component versions, which are required by current version.
	 */
	 public SCRequirementIF[] getRequired ();

	/**
	 * Returns all covered software component versions.
	 */	
	public SCLevelIF[] getCoverages ();

	/**
	 * Returns all software component versions, which are interface compatible to the current version.
	 */		
	public SCLevelIF[] getCompatibles ();
	
	/**
	 * Returns the current data as XML string.
	 */
	
	public String getXML ();
	
	/**
	 * Returns a string to display current version.
	 */
	
	public String toString ();
	
}
