package com.sap.engine.services.textcontainer.cache;

import java.util.Hashtable;

public class SAPResourceBundleCacheObject
{

	public SAPResourceBundleCacheObject(Hashtable<String, String> object)
	{
		this.object = object;
	}

	public Hashtable<String, String> getObject()
	{
		return object;
	}

	private Hashtable<String, String> object;

}
