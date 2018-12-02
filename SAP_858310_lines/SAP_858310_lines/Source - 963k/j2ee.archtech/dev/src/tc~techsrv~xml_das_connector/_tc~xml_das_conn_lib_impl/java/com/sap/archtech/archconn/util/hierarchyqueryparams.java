package com.sap.archtech.archconn.util;

public class HierarchyQueryParams 
{
	private final String archivePath;
	private final String range;
	private final boolean isRecursive;
	private final String resourceType;
	private final boolean onlyDeletedRes;
	private final String userName;
	private final boolean provideNrOfHits;

	/**
	 * @deprecated
	 */
	public HierarchyQueryParams(String archivePath, String range, boolean isRecursive, String resourceType, boolean onlyDeletedRes, String userName)
	{
		this(archivePath, range, isRecursive, resourceType, onlyDeletedRes, true, userName);
	}
	
	public HierarchyQueryParams(String archivePath, String range, boolean isRecursive, String resourceType, boolean onlyDeletedRes, boolean provideNrOfHits, String userName)
	{
		this.archivePath = archivePath;
		this.range = range;
		this.isRecursive = isRecursive;
		this.resourceType = resourceType;
		this.onlyDeletedRes = onlyDeletedRes;
		this.userName = userName;
		this.provideNrOfHits = provideNrOfHits;
	}

	String getArchivePath()
	{
		return archivePath;
	}
	
	String getRange()
	{
		return range;
	}
	
	boolean isRecursive()
	{
		return isRecursive;
	}
	
	String getResourceType()
	{
		return resourceType;
	}
	
	boolean onlyDeletedRes()
	{
		return onlyDeletedRes;
	}
	
	String getUserName()
	{
		return userName;
	}
	
	boolean provideNrOfHits()
	{
		return provideNrOfHits;
	}
}
