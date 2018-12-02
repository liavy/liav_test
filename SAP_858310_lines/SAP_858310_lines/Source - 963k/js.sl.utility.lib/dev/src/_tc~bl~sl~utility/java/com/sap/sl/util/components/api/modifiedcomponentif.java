
package com.sap.sl.util.components.api;

/**
 * @author d036263
 */

public interface ModifiedComponentIF {

//	  methods
	  /**
	   *  Gets the name of the vendor of this component version. 
	   *@return the vendor
	   */
	  public String getVendor();
	
	  /**
	   *  Gets the name of this component version. 
	   *@return the name
	   */
	  public String getName();
	  
	  public String getCmsName ();
	  
	  public String getCmsURL ();
	  
	  public String getProvider ();
	  
	  /**
	   * Checks, if current component is a developed one in the corresponding CMS track.
	   */
	  public boolean isDevelopedComponent ();
	  
	  /**
	   *  Gets the name of the track, where this component was produced.
	   *@return the trackname.
	   */
	  public String getTrackname ();
	
	  /**
	   *  Gets the release of this component version. 
	   */
		  public String getRelease();
	  
		  /**
	   *  Gets the servicelevel of this component version. 
	   */
	  public String getServiceLevel();
	
	  /**
	   *  Gets the patchlevel of this component version. 
	   */
	  public String getPatchLevel();
	
	  /**
	   *  Gets the time the current component version was applied to the system. 
	   */
	  public String getApplyTime();
	
		/**
		 * Returns the software provider of the current component.
		 * 
		 */
		 public String getComponentProvider ();
	
	   /**
		*  Writes a string consisting of vendor, name, location and counter of the component element. 
		*@return a string with the four keys of a component element
		*/
	   
	   /**
	    * Component is deployed from a CMS/NWDI DEV system.
	    */
		public boolean comesFromDEVSystem ();
	
	/**
	 * Component is deployed from a CMS/NWDI CONS system.
	 */
		public boolean comesFromCONSSystem ();
		
	/**
	 * Component is deployed from a CMS/NWDI TEST system.
	 */
		public boolean comesFromTESTSystem ();
		
	/**
	 * Component is deployed from a CMS/NWDI PROD system.
	 */
		public boolean comesFromPRODSystem ();
			
		public String toString();

	}