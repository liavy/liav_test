﻿
/*
This file is generated by Code Generator
for CIMClass SAP_ITSAMArchHierarchyAccessor
WARNING:DO NOT CHANGE THE CODE MANUALLY
*/

package com.sap.archtech.archconn.mbeans;	

import java.util.Date;
import javax.management.ObjectName;


/* 
ManagedElement is an abstract class that provides a common superclass (or top of the inheritance tree) for the non-association classes in the CIM Schema. 
@version  1.0	
*/	


public interface SAP_ITSAMArchHierarchyAccessor{

public void indexHierarchy();

public String insertCollection(String userName , String collURI , String archsetName);

public int deleteCollection(String userName , String archSetName , String collURI);

public int deleteSessionData(String archSetName , String sessionURI);

public String getCollectionID(String collURI);

public void setArchsessionUnmonitored(String collURI , String archSetName);

public com.sap.archtech.archconn.mbeans.SAP_ITSAMArchSession[] getChildNodes(byte[] collID);

public com.sap.archtech.archconn.mbeans.SAP_ITSAMArchSession[] getChildSessions(byte[] collID);
	
	}