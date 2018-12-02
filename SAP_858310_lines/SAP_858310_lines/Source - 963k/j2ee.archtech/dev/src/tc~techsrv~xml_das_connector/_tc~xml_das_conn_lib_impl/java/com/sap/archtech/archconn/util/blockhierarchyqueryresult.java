package com.sap.archtech.archconn.util;

import java.util.ArrayList;

import com.sap.archtech.archconn.ArchCommand;
import com.sap.archtech.archconn.ArchSession;
import com.sap.archtech.archconn.values.ResourceData;

public class BlockHierarchyQueryResult 
{
	private final ArrayList<ResourceData> resourceDataList;
	private final ArchCommand hierarchyQueryCmd;
	// Note: reference to the ArchSession must be kept as long as the command is needed - otherwise the HTTP connection
	// may become subject to garbage collection
	private final ArchSession archSessionRef;
	private final String nrOfHits;
	
	BlockHierarchyQueryResult(ArrayList<ResourceData> resourceDataList, ArchCommand hierarchyQueryCmd, ArchSession archSessionRef)
	{
		this.resourceDataList = new ArrayList<ResourceData>(resourceDataList.size());
		this.resourceDataList.addAll(resourceDataList);
		this.hierarchyQueryCmd = hierarchyQueryCmd;
		this.archSessionRef = archSessionRef;
		this.nrOfHits = hierarchyQueryCmd.getResponse().getHeaderField("nr_of_hits");
	}
	
	public ResourceData[] getResourceDataList()
	{
		return resourceDataList.toArray(new ResourceData[resourceDataList.size()]);
	}
	
	public ArchCommand getHierarchyQueryCommand()
	{
		return hierarchyQueryCmd;
	}
	
	public String getNrOfHits()
	{
		return nrOfHits;
	}
}
