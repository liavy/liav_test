﻿
/*
This file is generated by Code Generator
for CIMClass SAP_ITSAMArchSetPropAccessor
WARNING:DO NOT CHANGE THE CODE MANUALLY
*/

package com.sap.archtech.archconn.mbeans;	

import javax.management.ObjectName;


/* 
ManagedElement is an abstract class that provides a common superclass (or top of the inheritance tree) for the non-association classes in the CIM Schema. 
@version  1.0	
*/	


public interface SAP_ITSAMArchSetPropAccessor{

public String[] loadArchivingSets();

public com.sap.archtech.archconn.mbeans.SAP_ITSAMArchSetProp[] getArchSetProperties(String archSetName);

public void saveArchSetProperties(com.sap.archtech.archconn.mbeans.SAP_ITSAMArchSetProp[] propList);

public void deleteArchSetProperty(com.sap.archtech.archconn.mbeans.SAP_ITSAMArchSetProp toBeDeleted);

public boolean hasWStartProperty(String archSetName);

public String getHomePathCollectionURI(String archSetName);
	
	}