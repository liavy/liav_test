﻿

/*
This file is generated by Code Generator
for CIMClass SAP_ITSAMArchSelParm
WARNING:DO NOT CHANGE THE CODE MANUALLY
*/

package com.sap.archtech.archconn.mbeans;	

/* 
ManagedElement is an abstract class that provides a common superclass (or top of the inheritance tree) for the non-association classes in the CIM Schema. 
@version 1.0	
*/	

public class SAP_ITSAMArchSelParm{

private String archSetName=null;
	
private boolean IsParamNullable=false;
	
private String paramDefaultValue=null;
	
private String paramName=null;
	
private String paramNameLocalized=null;
	
private String paramType=null;
	
public SAP_ITSAMArchSelParm(){

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
indicates whether the parameter is nullable
@return boolean
*/
public boolean getIsParamNullable()
{
return this.IsParamNullable;
}

/*
@param boolean
*/
public void setIsParamNullable(boolean IsParamNullable)
{
this.IsParamNullable = IsParamNullable;
}

/*
=paramDefaultValue: string{write}
@return String
*/
public String getparamDefaultValue() 
{
return this.paramDefaultValue;
}

/*
@param String
*/
public void setparamDefaultValue(String paramDefaultValue)
{
this.paramDefaultValue = paramDefaultValue;
}

/*

@return String
*/
public String getparamName() 
{
return this.paramName;
}

/*
@param String
*/
public void setparamName(String paramName)
{
this.paramName = paramName;
}

/*

@return String
*/
public String getparamNameLocalized() 
{
return this.paramNameLocalized;
}

/*
@param String
*/
public void setparamNameLocalized(String paramNameLocalized)
{
this.paramNameLocalized = paramNameLocalized;
}

/*

@return String
*/
public String getparamType() 
{
return this.paramType;
}

/*
@param String
*/
public void setparamType(String paramType)
{
this.paramType = paramType;
}
	
}

