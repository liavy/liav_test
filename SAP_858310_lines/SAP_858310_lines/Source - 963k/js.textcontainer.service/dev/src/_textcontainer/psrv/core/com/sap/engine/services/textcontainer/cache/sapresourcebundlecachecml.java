package com.sap.engine.services.textcontainer.cache;

import com.sap.engine.services.textcontainer.TextContainerException;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.util.cache.CacheFacade;
import com.sap.util.cache.CacheRegion;
import com.sap.util.cache.CacheRegionFactory;
import com.sap.util.cache.exception.CacheException;

public class SAPResourceBundleCacheCML implements ISAPResourceBundleCache
{

	public static final String cacheRegionName = "TextContainer_SAPResourceBundle";

	SAPResourceBundleCacheCML() throws TextContainerException
	{
		CacheRegionFactory cacheRegionFactory = CacheRegionFactory.getInstance();

		if (cacheRegionFactory != null)
		{
			CacheRegion cacheRegion = cacheRegionFactory.getCacheRegion(cacheRegionName);

			if (cacheRegion != null)
				cacheFacade = cacheRegion.getCacheFacade();
		}
	}

	public SAPResourceBundleCacheObject get(String key) throws TextContainerException
	{
		return (SAPResourceBundleCacheObject) cacheFacade.get(key);
	}

	public void put(String key, SAPResourceBundleCacheObject value) throws TextContainerException
	{
		try
		{
			cacheFacade.put(key, value);
		}
		catch (CacheException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "put", "Parameters: {0}, {1}}", new Object[] { key, value }, e);
			throw new TextContainerException("CacheException in put", e);
		}
	}

	public void remove(String key) throws TextContainerException
	{
		cacheFacade.remove(key);
	}

	public void clear() throws TextContainerException
	{
		cacheFacade.clear();
	}

	public void dispose() throws TextContainerException
	{
		if (cacheFacade != null)
		{
			cacheFacade.clear();
			cacheFacade = null;
		}
	}

	private static CacheFacade cacheFacade = null; 

    // Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.cache.SAPResourceBundleCacheCML");
	private static final Category CATEGORY = Category.SYS_SERVER;

}
