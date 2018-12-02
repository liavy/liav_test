package com.sap.archtech.archconn.ilm;

import java.util.Date;

/**
 * The <code>CollectionRoutingParams</code> class represents the parameters required to define the org routing and time routing
 * in the archive hierarchy. 
 */
public class CollectionRoutingParams 
{
	private final String orgRoutingPath;
	private final Date startOfRetention;
	
	public CollectionRoutingParams(String orgRoutingPath, Date startOfRetention)
	{
		this.orgRoutingPath = orgRoutingPath != null ? orgRoutingPath : "";
		if(startOfRetention == null)
		{
			throw new IllegalArgumentException("Missing start-of-retention date");
		}
		this.startOfRetention = new Date(startOfRetention.getTime());
	}

	/**
	 * Get the path of org routing collections below the home path collection 
	 */
	public String getOrgRoutingPath() 
	{
		return orgRoutingPath;
	}

	/**
	 * Get the start-of-retention date (used to create the time routing collection)
	 */
	public Date getStartOfRetention() 
	{
		return new Date(startOfRetention.getTime());
	}
}
