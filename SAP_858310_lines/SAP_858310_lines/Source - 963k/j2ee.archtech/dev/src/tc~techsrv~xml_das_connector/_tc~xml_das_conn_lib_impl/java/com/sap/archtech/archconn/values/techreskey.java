package com.sap.archtech.archconn.values;

/**
 * 
 * Value class; holds a technical key
 * for a resource. Used for PICK.
 * 
 * @author D025792
 * @version 1.0
 * 
 */
public class TechResKey
{
	
	private String reskey;
	
	public TechResKey(String key)
	{
		this.reskey = key;
	}
	
	public String toString()
	{
		return reskey;
	}

}
