﻿

/*
This file is generated by Code Generator
for CIMClass SAP_ITSAMArchResource
WARNING:DO NOT CHANGE THE CODE MANUALLY
*/

package com.sap.archtech.archconn.mbeans;	

import java.util.Date;
/* 
ManagedElement is an abstract class that provides a common superclass (or top of the inheritance tree) for the non-association classes in the CIM Schema. 
@version 1.0	
*/	

public class SAP_ITSAMArchResource{

private String type=null;
	
private String collectionType=null;
	
private String uri=null;
	
private Date creationTime=null;
	
private String user=null;
	
private long length=0;
	
private String checkStatus=null;
	
private boolean isPacked=false;
	
private boolean isFrozen=false;
	
public SAP_ITSAMArchResource(){

}

public SAP_ITSAMArchResource(String type,String collectionType,String uri,Date creationTime,String user,long length,String checkStatus,boolean isPacked,boolean isFrozen){

this.type = type;
	
this.collectionType = collectionType;
	
this.uri = uri;
	
this.creationTime = creationTime;
	
this.user = user;
	
this.length = length;
	
this.checkStatus = checkStatus;
	
this.isPacked = isPacked;
	
this.isFrozen = isFrozen;
	
}

/*

@return String
*/
public String gettype() 
{
return this.type;
}

/*

@return String
*/
public String getcollectionType() 
{
return this.collectionType;
}

/*

@return String
*/
public String geturi() 
{
return this.uri;
}

/*

@return Date
*/
public Date getcreationTime()
{
return this.creationTime;
}

/*

@return String
*/
public String getuser() 
{
return this.user;
}

/*			

@return long
*/
public long getlength()
{
return this.length;
}

/*

@return String
*/
public String getcheckStatus() 
{
return this.checkStatus;
}

/*

@return boolean
*/
public boolean getisPacked()
{
return this.isPacked;
}

/*

@return boolean
*/
public boolean getisFrozen()
{
return this.isFrozen;
}
	
}
