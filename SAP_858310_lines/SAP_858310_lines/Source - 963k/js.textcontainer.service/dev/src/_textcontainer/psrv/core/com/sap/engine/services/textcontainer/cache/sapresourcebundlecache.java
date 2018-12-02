package com.sap.engine.services.textcontainer.cache;

import java.util.Locale;

import com.sap.engine.services.textcontainer.TextContainerConfiguration;
import com.sap.engine.services.textcontainer.TextContainerException;

public class SAPResourceBundleCache
{

	public static void initialize() throws TextContainerException
	{
		if (cache != null)
			cache.dispose();

		if (TextContainerConfiguration.useCMLForRuntimeCacheIsActive())
			cache = new SAPResourceBundleCacheCML();
		else
			cache = new SAPResourceBundleCacheHashtable();
	}

	public static SAPResourceBundleCacheObject get(String componentName, String baseName, Locale locale) throws TextContainerException
	{
		if (cache == null)
			initialize();

		String key = componentName + "|" + baseName + "|" + locale.toString();

		return cache.get(key);
	}

	public static void put(String componentName, String baseName, Locale locale, SAPResourceBundleCacheObject value) throws TextContainerException
	{
		if (cache == null)
			initialize();

		cache.put(componentName + "|" + baseName + "|" + locale.toString(), value);
	}

	public static void clear() throws TextContainerException
	{
		if (cache == null)
			initialize();

		cache.clear();
	}

	public static void remove(String componentName) throws TextContainerException
	{
		remove(componentName.replace("~", "/"), "**");
	}

	public static void remove(String componentName, String baseName) throws TextContainerException
	{
		if (cache == null)
			initialize();

		if (!"**".equals(baseName))
		{
			cache.remove(componentName.replace("~", "/") + "|" + baseName + "|**");
			cache.remove(componentName.replace("~", "/") + "|" + baseName.replace("/", ".") + "|**");
		}
		else
		{
			cache.remove(componentName.replace("~", "/") + "|" + baseName);
			cache.remove(componentName.replace("~", "/") + "|" + baseName.replace("/", "."));
		}
	}

	private static ISAPResourceBundleCache cache = null; 

}
