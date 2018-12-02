﻿

/*
This file is generated by Code Generator
for CIMClass SAP_ITSAMArchHierarchyNode
WARNING:DO NOT CHANGE THE CODE MANUALLY
*/

package com.sap.archtech.archconn.mbeans;	

import java.util.Date;
/* 
ManagedElement is an abstract class that provides a common superclass (or top of the inheritance tree) for the non-association classes in the CIM Schema. 
@version 1.0	
*/	

public class SAP_ITSAMArchHierarchyNode{

private String uri=null;
	
private String nodeID=null;
	
private String parentNodeID=null;
	
private String archSetName=null;
	
private boolean hasChildren=false;
	
public SAP_ITSAMArchHierarchyNode(){

}

public SAP_ITSAMArchHierarchyNode(String uri,String nodeID,String parentNodeID){

this.uri = uri;
	
this.nodeID = nodeID;
	
this.parentNodeID = parentNodeID;
	
}

/*

@return String
*/
public String geturi() 
{
return this.uri;
}

/*

@return String
*/
public String getnodeID() 
{
return this.nodeID;
}

/*

@return String
*/
public String getparentNodeID() 
{
return this.parentNodeID;
}

/*

@return String
*/
public String getarchSetName() 
{
return this.archSetName;
}

/*
@param String
*/
public void setarchSetName(String archSetName)
{
this.archSetName = archSetName;
}

/*

@return boolean
*/
public boolean gethasChildren()
{
return this.hasChildren;
}

/*
@param boolean
*/
public void sethasChildren(boolean hasChildren)
{
this.hasChildren = hasChildren;
}
	
}
