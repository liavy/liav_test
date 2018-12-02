package com.sap.archtech.archconn.ilm;

import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;

import com.sap.archtech.archconn.ArchCommand;
import com.sap.archtech.archconn.ArchSession;
import com.sap.archtech.archconn.values.IndexPropValues;

/**
 * The <code>Write2ArchiveParams</code> class represents the parameters required to write a given resource to the archive. 
 */
public class Write2ArchiveParams 
{
	private final ArchSession writeSession;
	private final String resourceType;
	private final String checkLevel;
	private final String resourceName;
	private final IndexPropValues ipv;
	private final InputStream resourceAsStream;
	private final Date expirationDate;
	
	private static final HashSet<String> allowedResourceTypes = new HashSet<String>(4);
	static
	{
		allowedResourceTypes.add("XML");
		allowedResourceTypes.add("XSD");
		allowedResourceTypes.add("XSL");
		allowedResourceTypes.add("BIN");
	}
	
	private static final HashSet<String> allowedCheckLevels = new HashSet<String>(3);
	static
	{
		allowedCheckLevels.add(ArchCommand.CHECK_LEVEL_NO);
		allowedCheckLevels.add(ArchCommand.CHECK_LEVEL_PARSE);
		allowedCheckLevels.add(ArchCommand.CHECK_LEVEL_VALIDATE);
	}

	public Write2ArchiveParams(ArchSession writeSession, String resourceType, String checkLevel, String resourceName, IndexPropValues ipv, InputStream resourceAsStream, Date expirationDate)
	{
		if(writeSession == null)
		{
			throw new IllegalArgumentException("Missing write session");
		}
		this.writeSession = writeSession;
		if(resourceType == null || !allowedResourceTypes.contains(resourceType))
		{
			throw new IllegalArgumentException("Unsupported resource type: " + resourceType);
		}
		this.resourceType = resourceType;
		if(checkLevel == null || !allowedCheckLevels.contains(checkLevel))
		{
			throw new IllegalArgumentException("Unsupported check level: " + checkLevel);
		}
		this.checkLevel = checkLevel;
		this.resourceName = resourceName != null ? resourceName : "";
		this.ipv = ipv;
		if(resourceAsStream == null)
		{
			throw new IllegalArgumentException("Missing resource content stream");
		}
		this.resourceAsStream = resourceAsStream;
		if(expirationDate == null)
		{
			throw new IllegalArgumentException("Missing expiration date");
		}
		this.expirationDate = new Date(expirationDate.getTime());
	}

	/**
	 * Get the archive write session
	 */
	public ArchSession getWriteSession() 
	{
		return writeSession;
	}

	/**
	 * Get the type of the resource. Must be one out of ["XML", "XSD", "XSL", "BIN"].
	 */
	public String getResourceType() 
	{
		return resourceType;
	}

	/**
	 * Get the check level to be applied. Must be one out of [ArchCommand.CHECK_LEVEL_NO, ArchCommand.CHECK_LEVEL_PARSE, ArchCommand.CHECK_LEVEL_VALIDATE]
	 */
	public String getCheckLevel() 
	{
		return checkLevel;
	}

	/**
	 * Get the name of the resource to be archived. If this name is <code>null</code>, then the resource will be given a generated name (-> auto-naming feature of XML DAS).
	 */
	public String getResourceName() 
	{
		return resourceName;
	}

	/**
	 * Get the property index values required for value-based search (optional parameter).
	 */
	public IndexPropValues getIpv() 
	{
		return ipv;
	}

	/**
	 * Get the input stream containing the resource content
	 */
	public InputStream getResourceAsStream() 
	{
		return resourceAsStream;
	}

	/**
	 * Get the expiration date (to be set for the resource)
	 */
	public Date getExpirationDate() 
	{
		return new Date(expirationDate.getTime());
	}
}
