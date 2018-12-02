package com.sap.engine.services.textcontainer.cache;

import java.util.Hashtable;

import com.sap.engine.services.textcontainer.TextContainerException;

public class SAPResourceBundleCacheHashtable implements ISAPResourceBundleCache
{

	// Taken from Web Dynpro: ServerConstants.EXPECTED_NUMBER_APPLICATIONS * 2
	public static final int initialCapacity = 400 * 2;

	SAPResourceBundleCacheHashtable() throws TextContainerException
	{
		cache = new Hashtable<String, SAPResourceBundleCacheObject>(initialCapacity);
	}

	public SAPResourceBundleCacheObject get(String key) throws TextContainerException
	{
		return cache.get(key);
	}

	public void put(String key, SAPResourceBundleCacheObject value) throws TextContainerException
	{
		cache.put(key, value);
	}

	public void remove(String key) throws TextContainerException
	{
		if (key.contains("**"))
			cache.clear();
		else
			cache.remove(key);
	}

	public void clear() throws TextContainerException
	{
		cache.clear();
	}

	public void dispose() throws TextContainerException
	{
		cache = null;
	}

	Hashtable<String, SAPResourceBundleCacheObject> cache;

}
