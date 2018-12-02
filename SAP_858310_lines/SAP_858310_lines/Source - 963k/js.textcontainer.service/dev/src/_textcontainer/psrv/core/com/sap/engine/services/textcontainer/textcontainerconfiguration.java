package com.sap.engine.services.textcontainer;

import java.util.Iterator;
import java.util.Properties;
import java.util.Map.Entry;

import com.sap.engine.frame.ServiceException;
import com.sap.engine.frame.container.runtime.RuntimeConfiguration;
import com.sap.engine.services.textcontainer.cache.SAPResourceBundleCache;
import com.sap.engine.services.textcontainer.context.Context;
import com.sap.engine.services.textcontainer.context.SystemContext;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class TextContainerConfiguration extends RuntimeConfiguration
{

	private static final String useCMLForRuntimeCache = "useCMLForRuntimeCache";
	private static final String useTextLoadForCaching = "useTextLoadForCaching";

	public static boolean useCMLForRuntimeCacheIsActive()
	{
		return useCMLForRuntimeCacheIsActive;
	}

	public static boolean useTextLoadForCachingIsActive()
	{
		return useTextLoadForCachingIsActive;
	}

	public static void setProperties(Properties properties) throws TextContainerException
	{
		Iterator<Entry<Object, Object>> iterProperties = properties.entrySet().iterator();

		while (iterProperties.hasNext())
		{
			Entry<Object, Object> entryProperties = iterProperties.next();

			Object key = entryProperties.getKey();
			Object value = entryProperties.getValue();

			if ((key != null) && (value != null))
			{
				TextContainerConfiguration.properties.put(key, value);

				if (useCMLForRuntimeCache.equals(key))
				{
					useCMLForRuntimeCacheIsActive = getPropertyValue(useCMLForRuntimeCache, true);

					// Initialize the SAPResourceBundleCache only with the set property,
					// if the Text Container service is already initialized.
					// This means that during service startup this is NOT done!
					if (TextContainerService.isInitialized())
						SAPResourceBundleCache.initialize();
				}
				else if (useTextLoadForCaching.equals(key))
				{
					useTextLoadForCachingIsActive = getPropertyValue(useTextLoadForCaching, true);

					// Initialize the text load only with the set property,
					// if the Text Container service is already initialized.
					// This means that during service startup this is NOT done!
					if (TextContainerService.isInitialized())
					{
						// Set the system context to the same value!
						// (This will initialize everything needed, e.g. text load, cache, ...)
						Context systemContext = SystemContext.getInstance().get();
						SystemContext.getInstance().set(systemContext.getIndustry(), systemContext.getRegion(), systemContext.getExtension());
					}
				}
			}
		}
	}

	@Override
	public void updateProperties(Properties properties) throws ServiceException
	{
		try
		{
			setProperties(properties);
		}
		catch (TextContainerException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "updateProperties", e);
            throw new ServiceException(LOCATION, e);
		}
	}

	private static boolean getPropertyValue(String propertyName, boolean defaultValue)
	{
		Object value = null;

		value = properties.get(propertyName);

		// Get property value from property bag:
		if ((value != null) && (value instanceof String))
			return Boolean.valueOf((String) value).booleanValue();

		return defaultValue;
	}

	private static Properties properties = new Properties();

	private static boolean useCMLForRuntimeCacheIsActive = true;
	private static boolean useTextLoadForCachingIsActive = true;

    // Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.TextContainerConfiguration");
	private static final Category CATEGORY = Category.SYS_SERVER;

}
