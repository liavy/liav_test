package com.sap.archtech.archconn.values;

import java.io.Serializable;

/**
 * The <code>WebDavStoreConformanceData</code> class holds all WebDAV store meta data regarding ILM conformance.
 */
public class WebDavStoreConformanceData implements Serializable
{
	private static final long serialVersionUID = 42L;
	
	private final String davConformance;
	private final short ilmConformance;
	private final short ilmAlConformance;
	
	public WebDavStoreConformanceData(String davConformance, short ilmConformance, short ilmAlConformance)
	{
		this.davConformance = davConformance != null ? davConformance : "";
		this.ilmConformance = ilmConformance;
		this.ilmAlConformance = ilmAlConformance;	
	}
	
	
	/**
	 * @return DAV compliance class according to RFC 4918, chapter 18
	 */
	public String getDavConformance() 
	{
		return davConformance;
	}

	/**
	 * @return ILM ArchiveLink conformance class
	 */
	public short getIlmAlConformance() 
	{
		return ilmAlConformance;
	}

	/**
	 * @return ILM conformance class
	 */
	public short getIlmConformance() 
	{
		return ilmConformance;
	}

}
