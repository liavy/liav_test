/*
 * Created on Apr 6, 2006
 */
package com.sap.engine.services.textcontainer;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.Locale;
import java.util.MissingResourceException;

import javax.sql.DataSource;

import com.sap.engine.frame.core.load.ClassInfo;
import com.sap.engine.frame.core.load.ClassWithLoaderInfo;
import com.sap.engine.frame.core.load.LoadContext;
import com.sap.engine.interfaces.textcontainer.context.TextContainerRules;
import com.sap.engine.services.textcontainer.cache.SAPResourceBundleCache;
import com.sap.engine.services.textcontainer.cache.SAPResourceBundleCacheObject;
import com.sap.engine.services.textcontainer.context.AllowedLanguages;
import com.sap.engine.services.textcontainer.context.Context;
import com.sap.engine.services.textcontainer.context.ContextResolution;
import com.sap.engine.services.textcontainer.context.SystemContext;
import com.sap.engine.services.textcontainer.context.TransferFunctions;
import com.sap.engine.services.textcontainer.dbaccess.ContextData;
import com.sap.engine.services.textcontainer.dbaccess.Ctx;
import com.sap.engine.services.textcontainer.dbaccess.LoadData;
import com.sap.engine.services.textcontainer.runtime.ITextContainerRuntime;
import com.sap.engine.services.textcontainer.security.TextContainerMessageDigest;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * @author d029702
 */
public class TextContainerRuntimeImpl implements ITextContainerRuntime
{

	String getComponentNameFromClass(Class cls)
	{
		String componentName = "";

		try
		{
			LoadContext lc = TextContainerService.getServiceContext().getCoreContext().getLoadContext();
			ClassInfo ci = lc.getClassInfo(cls);
			ClassWithLoaderInfo cwli = (ClassWithLoaderInfo) ci;
			if (cwli != null)
			{
				componentName = cwli.getComponent().getName().replace("~", "/");
			}
		}
		catch (IllegalArgumentException ie)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getComponentNameFromClass", "Parameters: {0}", new Object[] { cls }, ie);
        	throw new MissingResourceException("Cannot retrieve component name for class object " + cls + " (illegal argument exception)", "", "");
		}
		catch (RuntimeException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getComponentNameFromClass", "Parameters: {0}", new Object[] { cls }, e);
        	throw new MissingResourceException("Cannot retrieve component name for class object " + cls + " (runtime exception)", "", "");
		}

		if ((componentName == null) || (componentName.length() == 0))
		{
    		CATEGORY.logT(Severity.ERROR, LOCATION, "getComponentNameFromClass", "Parameters: {0}", new Object[] { cls });
        	throw new MissingResourceException("Cannot retrieve component name for class object " + cls + " (null or length == 0)", "", "");
		}

		return componentName;
	}

	String getComponentNameFromClassLoader(ClassLoader loader)
	{
		String componentName = "";

		try
		{
			LoadContext lc = TextContainerService.getServiceContext().getCoreContext().getLoadContext();
			ClassInfo ci = lc.getLoaderComponentInfo(loader);
			ClassWithLoaderInfo cwli = (ClassWithLoaderInfo) ci;
			if (cwli != null)
			{
				componentName = cwli.getComponent().getName().replace("~", "/");
			}
		}
		catch (IllegalArgumentException ie)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getComponentNameFromClassLoader", "Parameters: {0}", new Object[] { loader }, ie);
        	throw new MissingResourceException("Cannot retrieve component name for classloader object " + loader + " (illegal argument exception)", "", "");
		}
		catch (RuntimeException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getComponentNameFromClassLoader", "Parameters: {0}", new Object[] { loader }, e);
        	throw new MissingResourceException("Cannot retrieve component name for classloader object " + loader + " (runtime exception)", "", "");
		}

		if ((componentName == null) || (componentName.length() == 0))
		{
    		CATEGORY.logT(Severity.ERROR, LOCATION, "getComponentNameFromClassLoader", "Parameters: {0}", new Object[] { loader });
        	throw new MissingResourceException("Cannot retrieve component name for classloader object " + loader + " (null or length == 0)", "", "");
		}

		return componentName;
	}

	Hashtable HandleResourceBundleFallback(MissingResourceException mre, String baseName, Locale locale, Class cls)
	{
		// Fallback to java.util.ResourceBundle (depending on settings for recipient etc.)
		throw mre;
	}

	Hashtable HandleResourceBundleFallback(MissingResourceException mre, String baseName, Locale locale, ClassLoader loader)
	{
		// Fallback to java.util.ResourceBundle (depending on settings for recipient etc.)
		throw mre;
	}

	Hashtable HandleResourceBundleFallback(MissingResourceException mre, String baseName, Locale locale, String componentName)
	{
		// Fallback to java.util.ResourceBundle (depending on settings for recipient etc.)
		throw mre;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.services.textcontainer.runtime.service.ITextContainerRuntime#getTexts(java.lang.String, java.util.Locale, java.lang.Class)
	 */
	public Hashtable getTexts(String baseName, Locale locale, Class cls)
	{
		Hashtable<String, String> texts = null;

		if (!TextContainerService.isInitialized())
		{
    		CATEGORY.logT(Severity.WARNING, LOCATION, "getTexts", "Text Container Service is not initialized! Parameters: {0}, {1}, {2}", new Object[] { baseName, locale, cls } );
        	throw new MissingResourceException("Text Container Service is not initialized!", "", "");
		}

		String componentName = getComponentNameFromClass(cls);

		try
		{
			texts = getTexts(componentName, baseName, locale);
		}
		catch (MissingResourceException mre)
		{
			HandleResourceBundleFallback(mre, baseName, locale, cls);
		}
        catch (Exception e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getTexts", "Parameters: {0}, {1}, {2}(, {3})", new Object[] { baseName, locale, cls, componentName }, e);
        	throw new MissingResourceException("MissingResourceException because of exception during text retrieval (see log for details)!", "", "");
		}

		return texts;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.services.textcontainer.runtime.service.ITextContainerRuntime#getTexts(java.lang.String, java.util.Locale, java.lang.ClassLoader)
	 */
	public Hashtable getTexts(String baseName, Locale locale, ClassLoader loader)
	{
		Hashtable<String, String> texts = null;

		if (!TextContainerService.isInitialized())
		{
    		CATEGORY.logT(Severity.WARNING, LOCATION, "getTexts", "Text Container Service is not initialized! Parameters: {0}, {1}, {2}", new Object[] { baseName, locale, loader } );
        	throw new MissingResourceException("Text Container Service is not initialized!", "", "");
		}

		String componentName = getComponentNameFromClassLoader(loader);

		try
		{
			texts = getTexts(componentName, baseName, locale);
		}
		catch (MissingResourceException mre)
		{
			HandleResourceBundleFallback(mre, baseName, locale, loader);
		}
        catch (Exception e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getTexts", "Parameters: {0}, {1}, {2}(, {3})", new Object[] { baseName, locale, loader, componentName }, e);
        	throw new MissingResourceException("MissingResourceException because of exception during text retrieval (see log for details)!", "", "");
		}

		return texts;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.services.textcontainer.runtime.service.ITextContainerRuntime#getTexts(java.lang.String, java.util.Locale, java.lang.String)
	 */
	public Hashtable getTexts(String baseName, Locale locale, String componentName)
	{
		Hashtable<String, String> texts = null;

		if (!TextContainerService.isInitialized())
		{
    		CATEGORY.logT(Severity.WARNING, LOCATION, "getTexts", "Text Container Service is not initialized! Parameters: {0}, {1}, {2}", new Object[] { baseName, locale, componentName } );
        	throw new MissingResourceException("Text Container Service is not initialized!", "", "");
		}

		try
		{
			texts = getTexts(componentName, baseName, locale);
		}
		catch (MissingResourceException mre)
		{
			HandleResourceBundleFallback(mre, baseName, locale, componentName);
		}
        catch (Exception e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getTexts", "Parameters: {0}, {1}, {2}", new Object[] { baseName, locale, componentName }, e);
        	throw new MissingResourceException("MissingResourceException because of exception during text retrieval (see log for details)!", "", "");
		}

		return texts;
	}

	protected synchronized Hashtable<String, String> getTexts(String componentName, String baseName, Locale locale) throws TextContainerException, java.sql.SQLException
	{
		Hashtable<String, String> texts = null;

		SAPResourceBundleCacheObject object = SAPResourceBundleCache.get(componentName, baseName, locale);

		if (object == null)
		{
			DataSource dataSource = TextContainerService.getDataSource();

			if (dataSource == null)
			{
	    		CATEGORY.logT(Severity.ERROR, LOCATION, "getTexts", "Cannot retrieve data source from Text Container Service!" );
	        	throw new NullPointerException("dataSource == null");
			}

			Connection connection = dataSource.getConnection();

			if (connection == null)
			{
	    		CATEGORY.logT(Severity.ERROR, LOCATION, "getTexts", "Cannot retrieve connection from data source!" );
	        	throw new NullPointerException("connection == null");
			}

			Ctx ctx = new Ctx(connection);

			try
			{
				Context systemContext = SystemContext.getInstance().get();

				byte[] componentHash = TextContainerMessageDigest.getDigestOptimizeAscii(componentName);

				String localeString = locale.toString();

		    	ContextData context = new ContextData(localeString, systemContext.getIndustry(), systemContext.getRegion(), systemContext.getExtension());

				// If text load is not used for caching perform context resolution and return texts:
				if (!TextContainerConfiguration.useTextLoadForCachingIsActive())
				{
					texts = ContextResolution.getInstance().resolveComponentBundleWithContext(ctx, componentHash, TextContainerMessageDigest.getDigestOptimizeAscii(baseName), context);
				}
				else
				{
					int contextId = (localeString.length() > 5 ? 0 : ContextData.contextToId(ctx, context));

					texts = getTextsInternal(ctx, componentHash, TextContainerMessageDigest.getDigestOptimizeAscii(baseName), localeString, systemContext, contextId);

					if (texts == null)
						texts = getTextsInternal(ctx, componentHash, TextContainerMessageDigest.getDigestOptimizeAscii(baseName.replace(".", "/")), localeString, systemContext, contextId);
				}
			}
			finally
			{
				if (ctx != null)
					ctx.close();
			}

	        SAPResourceBundleCache.put(componentName, baseName, locale, new SAPResourceBundleCacheObject(texts));
		}
		else
		{
			texts = object.getObject();
		}

		if (texts == null)
        {
			MissingResourceException e = new MissingResourceException("Can't find bundle for component " +
        			componentName + ", base name " + baseName + ", locale " + locale,
        			componentName + "_" + baseName + "_" + locale,
        			"");
			if (LOCATION.beDebug())
				CATEGORY.logThrowableT(Severity.DEBUG, LOCATION, "getTexts", "Parameters: {0}, {1}, {2}", new Object[] { componentName, baseName, locale }, e);
        	throw e;
        }

        return texts;
	}

	protected Hashtable<String, String> getTextsInternal(Ctx ctx, byte[] componentHash, byte[] bundleHash, String localeString, Context systemContext, int contextId) throws TextContainerException, java.sql.SQLException
	{
		Hashtable<String, String> texts = null;

		LoadData[] load = null;

		if (contextId != 0)
		{
			// Load from the text load with the requested locale and the system context:
			load = LoadData.select(ctx, contextId, componentHash, bundleHash);

			if (load != null)
			{
				texts = new Hashtable<String, String>();

				for (int j = 0; j < load.length; j++)
					texts.put(load[j].getTextKey(), load[j].getText());
			}
		}

        if (texts == null)
		{
			// There are no texts in the text load with the requested locale and the system context:
			boolean processedLanguagesAvailable = AllowedLanguages.getInstance().getProcessedLanguagesAvailable();

			if ((!processedLanguagesAvailable) ||
				((processedLanguagesAvailable) && (AllowedLanguages.getInstance().contains(getLocaleWithoutCountry(localeString)))))
			{
				String[] localeChain = TransferFunctions.getInstance().getLocaleChain(TextContainerRules.CR_VIEW, localeString, null);

				for (int i = 1; i < localeChain.length; i++)
				{
					localeString = localeChain[i];

					if (processedLanguagesAvailable)
					{
						if (!AllowedLanguages.getInstance().contains(getLocaleWithoutCountry(localeString)))
							continue;
					}

					ContextData context = new ContextData(localeString, systemContext.getIndustry(), systemContext.getRegion(), systemContext.getExtension());
					contextId = ContextData.contextToId(ctx, context);

					if (contextId != 0)
					{
						load = LoadData.select(ctx, contextId, componentHash, bundleHash);

						if (load != null)
						{
							texts = new Hashtable<String, String>();

							for (int j = 0; j < load.length; j++)
								texts.put(load[j].getTextKey(), load[j].getText());

							break;
						}
					}

					if (processedLanguagesAvailable)
					{
						if ((localeString.equals(getLocaleWithoutCountry(localeString))) &&
							(AllowedLanguages.getInstance().contains(getLocaleWithoutCountry(localeString))))
							break;
					}
				}
			}
		}

        return texts;
	}

	protected String getLocaleWithoutCountry(String locale)
	{
		if (locale == null)
			return null;

		int index = locale.indexOf("_");

		if (index < 0)
			return locale;
		else
			return locale.substring(0, index);
	}

	// Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.TextContainerRuntimeImpl");
	private static final Category CATEGORY = Category.SYS_SERVER;

}
