/*
 * Created on Mar 6, 2006
 */
package com.sap.engine.services.textcontainer.api;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.sql.DataSource;

import com.sap.engine.interfaces.textcontainer.TextContainerExtension;
import com.sap.engine.interfaces.textcontainer.TextContainerIndustry;
import com.sap.engine.interfaces.textcontainer.TextContainerLanguage;
import com.sap.engine.interfaces.textcontainer.TextContainerLocale;
import com.sap.engine.interfaces.textcontainer.TextContainerManager;
import com.sap.engine.interfaces.textcontainer.TextContainerManagerException;
import com.sap.engine.interfaces.textcontainer.TextContainerManagerMissingBundleException;
import com.sap.engine.interfaces.textcontainer.TextContainerManagerMissingComponentException;
import com.sap.engine.interfaces.textcontainer.TextContainerRecipient;
import com.sap.engine.interfaces.textcontainer.TextContainerRegion;
import com.sap.engine.interfaces.textcontainer.context.TextContainerContextResolution;
import com.sap.engine.interfaces.textcontainer.recipient.TextContainerRecipientListener;
import com.sap.engine.services.textcontainer.TextContainerException;
import com.sap.engine.services.textcontainer.TextContainerService;
import com.sap.engine.services.textcontainer.context.AllowedExtensions;
import com.sap.engine.services.textcontainer.context.AllowedIndustries;
import com.sap.engine.services.textcontainer.context.AllowedLanguages;
import com.sap.engine.services.textcontainer.context.AllowedRegions;
import com.sap.engine.services.textcontainer.context.SystemContext;
import com.sap.engine.services.textcontainer.context.TransferFunctions;
import com.sap.engine.services.textcontainer.dbaccess.BundleData;
import com.sap.engine.services.textcontainer.dbaccess.ComponentData;
import com.sap.engine.services.textcontainer.dbaccess.ContainerData;
import com.sap.engine.services.textcontainer.dbaccess.ContainerIteratorLocales;
import com.sap.engine.services.textcontainer.dbaccess.Ctx;
import com.sap.engine.services.textcontainer.dbaccess.ExtensionData;
import com.sap.engine.services.textcontainer.dbaccess.IndustryData;
import com.sap.engine.services.textcontainer.dbaccess.LanguageData;
import com.sap.engine.services.textcontainer.dbaccess.LocaleData;
import com.sap.engine.services.textcontainer.dbaccess.RegionData;
import com.sap.engine.services.textcontainer.security.TextContainerMessageDigest;
import com.sap.engine.services.textcontainer.security.TextContainerSecurity;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * @author d029702
 */
public class TextContainerManagerImpl implements TextContainerManager
{

	public String getSystemContextIndustry()
	{
		String industry = "";

		try
		{
			industry = SystemContext.getInstance().get().getIndustry();
		}
		catch (TextContainerException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getSystemContextIndustry", e);
		}

		return industry;
	}

	public String getSystemContextRegion()
	{
		String region = "";

		try
		{
			region = SystemContext.getInstance().get().getRegion();
		}
		catch (TextContainerException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getSystemContextRegion", e);
		}

		return region;
	}

	public String getSystemContextExtension()
	{
		String extension = "";

		try
		{
			extension = SystemContext.getInstance().get().getExtension();
		}
		catch (TextContainerException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getSystemContextExtension", e);
		}

		return extension;
	}

	public HashMap<String, TextContainerIndustry> getIndustryValues()
	{
		HashMap<String, TextContainerIndustry> industries = new HashMap<String, TextContainerIndustry>();

		try
		{
			DataSource oDataSource = TextContainerService.getDataSource();

			Ctx ctx = new Ctx(oDataSource.getConnection());

			try
			{
				IndustryData[] industryData = IndustryData.select(ctx);

				for (int i = 0; i < industryData.length; i++)
				{
					industries.put(industryData[i].getIndustry(),
							new TextContainerIndustryImpl(industryData[i].getIndustry(), industryData[i].getFather(),
									industryData[i].getTermDomain(), industryData[i].getCollKey()));
				}
			}
			finally
			{
				if (ctx != null)
					ctx.close();
			}
		}
    	catch (SQLException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getIndustryValues", e);
		}

    	return industries;
	}

	public HashMap<String, TextContainerRegion> getRegionValues()
	{
		HashMap<String, TextContainerRegion> regions = new HashMap<String, TextContainerRegion>();

		try
		{
			DataSource oDataSource = TextContainerService.getDataSource();

			Ctx ctx = new Ctx(oDataSource.getConnection());

			try
			{
				RegionData[] regionData = RegionData.select(ctx);

				for (int i = 0; i < regionData.length; i++)
				{
					regions.put(regionData[i].getRegion(),
							new TextContainerRegionImpl(regionData[i].getRegion(), regionData[i].getFather(),
									regionData[i].getTermDomain(), regionData[i].getCollKey()));
				}
			}
			finally
			{
				if (ctx != null)
					ctx.close();
			}
		}
    	catch (SQLException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getRegionValues", e);
		}

    	return regions;
	}

	public HashMap<String, TextContainerExtension> getExtensionValues()
	{
		HashMap<String, TextContainerExtension> extensions = new HashMap<String, TextContainerExtension>();

		try
		{
			DataSource oDataSource = TextContainerService.getDataSource();

			Ctx ctx = new Ctx(oDataSource.getConnection());

			try
			{
				ExtensionData[] extensionData = ExtensionData.select(ctx);

				for (int i = 0; i < extensionData.length; i++)
				{
					extensions.put(extensionData[i].getExtension(),
							new TextContainerExtensionImpl(extensionData[i].getExtension(), extensionData[i].getFather(),
									extensionData[i].getTermDomain(), extensionData[i].getCollKey()));
				}
			}
			finally
			{
				if (ctx != null)
					ctx.close();
			}
		}
    	catch (SQLException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getExtensionValues", e);
		}

    	return extensions;
	}

	public HashMap<String, TextContainerLanguage> getLanguageValues()
	{
		HashMap<String, TextContainerLanguage> languages = new HashMap<String, TextContainerLanguage>();

		try
		{
			DataSource oDataSource = TextContainerService.getDataSource();

			Ctx ctx = new Ctx(oDataSource.getConnection());

			try
			{
				LanguageData[] languageData = LanguageData.select(ctx);

				for (int i = 0; i < languageData.length; i++)
				{
					languages.put(languageData[i].getLocale(),
							new TextContainerLanguageImpl(languageData[i].getLocale(), languageData[i].getIsSecondaryLocale()));
				}
			}
			finally
			{
				if (ctx != null)
					ctx.close();
			}
		}
    	catch (SQLException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getLanguageValues", e);
		}

    	return languages;
	}

	public HashMap<String, TextContainerLocale[]> getLocaleValues()
	{
		HashMap<String, TextContainerLocale[]> locales = new HashMap<String, TextContainerLocale[]>();

		try
		{
			DataSource oDataSource = TextContainerService.getDataSource();

			Ctx ctx = new Ctx(oDataSource.getConnection());

			try
			{
				// Get the list (it's ordered by startLocale and sequenceNumber!):
				LocaleData[] localeData = LocaleData.select(ctx);

				String startLocale = "";

				ArrayList<TextContainerLocale> list = new ArrayList<TextContainerLocale>();

				for (int i = 0; i < localeData.length; i++)
				{
					if (!localeData[i].getStartLocale().equals(startLocale))
					{
						if (startLocale.length() > 0)
						{
							locales.put(startLocale, (TextContainerLocale[]) list.toArray(new TextContainerLocale[0]));

							list = new ArrayList<TextContainerLocale>();
						}

						startLocale = localeData[i].getStartLocale();
					}

					list.add(new TextContainerLocaleImpl(localeData[i].getStartLocale(), localeData[i].getSequenceNumber(),
							localeData[i].getLocale()));
				}

				if (startLocale.length() > 0)
				{
					locales.put(startLocale, (TextContainerLocale[]) list.toArray(new TextContainerLocale[0]));
				}
			}
			finally
			{
				if (ctx != null)
					ctx.close();
			}
		}
    	catch (SQLException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getLocaleValues", e);
		}

    	return locales;
	}

	public void registerRecipient(String recipient, TextContainerRecipient object) throws TextContainerManagerException
	{
/* Removed because TextContainerRecipient notification has been switched off! 
		try
		{
			TextContainerService.registerRecipient(recipient, object);
		}
		catch (TextContainerException e)
		{
			throw new TextContainerManagerException("TextContainerException", e);
		}
*/

		// Instead just a log entry is written:
		CATEGORY.logT(Severity.WARNING, LOCATION, "registerRecipient", "Interface TextContainerRecipient is deprecated. Use TextContainerRecipientListener!");
	}

	public void unregisterRecipient(String recipient) throws TextContainerManagerException
	{
/* Removed because TextContainerRecipient notification has been switched off! 
		try
		{
			TextContainerService.unregisterRecipient(recipient);
		}
		catch (TextContainerException e)
		{
			throw new TextContainerManagerException("TextContainerException", e);
		}
*/

		// Instead just a log entry is written:
		CATEGORY.logT(Severity.WARNING, LOCATION, "unregisterRecipient", "Interface TextContainerRecipient is deprecated. Use TextContainerRecipientListener!");
	}

	public void registerRecipientListener(String recipient, TextContainerRecipientListener listener) throws TextContainerManagerException
	{
		try
		{
			TextContainerService.registerRecipientListener(recipient, listener);
		}
		catch (TextContainerException e)
		{
			throw new TextContainerManagerException("TextContainerException", e);
		}
	}

	public void unregisterRecipientListener(String recipient, TextContainerRecipientListener listener) throws TextContainerManagerException
	{
		try
		{
			TextContainerService.unregisterRecipientListener(recipient, listener);
		}
		catch (TextContainerException e)
		{
			throw new TextContainerManagerException("TextContainerException", e);
		}
	}

	public TextContainerContextResolution getContextResolutionObject()
	{
		return TextContainerContextResolutionImpl.getInstance();
	}

	public ResourceBundle[] getTexts(String componentName, String baseName) throws TextContainerManagerException
	{
		ResourceBundle[] bundles = null;

		if ((componentName == null) || (baseName == null))
		{
			TextContainerManagerException e = new TextContainerManagerException("Parameters must not be null!"); 
			CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getTexts", "Parameter: {0}, {1}", new Object[] { componentName, baseName }, e);
			throw e;
		}

		try
		{
			DataSource oDataSource = TextContainerService.getDataSource();

			Ctx ctx = new Ctx(oDataSource.getConnection());

			try
			{
				byte[] componentHash = TextContainerMessageDigest.getDigestOptimizeAscii(componentName);

	        	if (!ComponentData.exists(ctx, componentHash, componentName))
	        	{
	        		TextContainerManagerMissingComponentException e = new TextContainerManagerMissingComponentException("Missing component"); 
	    			CATEGORY.logThrowableT(Severity.INFO, LOCATION, "getTexts", "Missing component {0}", new Object[] { componentName }, e);
	        		throw e;
	        	}

	        	byte[] bundleHash = TextContainerMessageDigest.getDigestOptimizeAscii(baseName);

	        	if (!BundleData.exists(ctx, bundleHash, componentHash))
	        	{
	        		TextContainerManagerMissingBundleException e = new TextContainerManagerMissingBundleException("Missing bundle"); 
	    			CATEGORY.logThrowableT(Severity.INFO, LOCATION, "getTexts", "Missing component {0}, {1}", new Object[] { componentName, baseName }, e);
	        		throw e;
	        	}
			}
			finally
			{
				if (ctx != null)
					ctx.close();
			}
		}
    	catch (SQLException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getTexts", e);
			throw new TextContainerManagerException("SQLException", e);
		}
    	catch (TextContainerException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getTexts", e);
			throw new TextContainerManagerException("TextContainerException", e);
		}

		try
		{
    		bundles = TextContainerResourceBundle.getBundles(componentName, baseName);
		}
		catch (MissingResourceException e)
		{
			throw new TextContainerManagerException("MissingResourceException", e);
		}

		return bundles;
	}

	public String[] getLocales(String componentName) throws TextContainerManagerException
	{
		String[] locales = null;

		if (componentName == null)
		{
			TextContainerManagerException e = new TextContainerManagerException("Parameter must not be null!"); 
			CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getLocales", "Parameter: {0}", new Object[] { componentName }, e);
			throw e;
		}

		try
		{
			DataSource oDataSource = TextContainerService.getDataSource();

			Ctx ctx = new Ctx(oDataSource.getConnection());

			try
			{
				byte[] componentHash = TextContainerMessageDigest.getDigestOptimizeAscii(componentName);

	        	if (!ComponentData.exists(ctx, componentHash, componentName))
	        	{
	        		TextContainerManagerMissingComponentException e = new TextContainerManagerMissingComponentException("Missing component"); 
	    			CATEGORY.logThrowableT(Severity.INFO, LOCATION, "getTexts", "Missing component {0}", new Object[] { componentName }, e);
	        		throw e;
	        	}

				ContainerIteratorLocales iterLocales = ContainerData.selectLocales(ctx, componentHash);

				ArrayList<String> list = new ArrayList<String>();

				while (iterLocales.next())
				{
					list.add(iterLocales.locale().trim());
				}

				iterLocales.close();

				locales = (String[])list.toArray(new String[] {});
			}
			finally
			{
				if (ctx != null)
					ctx.close();
			}
		}
    	catch (SQLException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getTexts", e);
			throw new TextContainerManagerException("SQLException", e);
		}
    	catch (TextContainerException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getTexts", e);
			throw new TextContainerManagerException("TextContainerException", e);
		}

		return locales;
	}

	public void setSystemContext(String industry, String region, String extension) throws TextContainerManagerException
	{
		try
		{
			SystemContext.getInstance().set(industry, region, extension);
		}
		catch (TextContainerException e)
		{
			throw new TextContainerManagerException("TextContainerException", e);
		}
	}

	public void setIndustryValues(HashMap<String, TextContainerIndustry> industries) throws TextContainerManagerException
	{
		if (industries == null)
		{
			TextContainerManagerException e = new TextContainerManagerException("Parameter must not be null!"); 
			CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "setIndustryValues", "Parameter: {0}", new Object[] { industries }, e);
			throw e;
		}

    	try
		{
    		AllowedIndustries.getInstance().set(industries);
		}
		catch (TextContainerException e)
		{
			throw new TextContainerManagerException("TextContainerException", e);
		}
	}

	public void setRegionValues(HashMap<String, TextContainerRegion> regions) throws TextContainerManagerException
	{
		if (regions == null)
		{
			TextContainerManagerException e = new TextContainerManagerException("Parameter must not be null!"); 
			CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "setRegionValues", "Parameter: {0}", new Object[] { regions }, e);
			throw e;
		}

    	try
		{
    		AllowedRegions.getInstance().set(regions);
		}
		catch (TextContainerException e)
		{
			throw new TextContainerManagerException("TextContainerException", e);
		}
	}

	public void setExtensionValues(HashMap<String, TextContainerExtension> extensions) throws TextContainerManagerException
	{
		if (extensions == null)
		{
			TextContainerManagerException e = new TextContainerManagerException("Parameter must not be null!"); 
			CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "setExtensionValues", "Parameter: {0}", new Object[] { extensions }, e);
			throw e;
		}

    	try
		{
    		AllowedExtensions.getInstance().set(extensions);
		}
		catch (TextContainerException e)
		{
			throw new TextContainerManagerException("TextContainerException", e);
		}
	}

	public void setLanguageValues(HashMap<String, TextContainerLanguage> languages) throws TextContainerManagerException
	{
		if (languages == null)
		{
			TextContainerManagerException e = new TextContainerManagerException("Parameter must not be null!"); 
			CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "setLanguageValues", "Parameter: {0}", new Object[] { languages }, e);
			throw e;
		}

    	try
		{
    		AllowedLanguages.getInstance().set(languages);
		}
		catch (TextContainerException e)
		{
			throw new TextContainerManagerException("TextContainerException", e);
		}
	}

	public void setLocaleValues(HashMap<String, TextContainerLocale[]> locales) throws TextContainerManagerException
	{
		if (locales == null)
		{
			TextContainerManagerException e = new TextContainerManagerException("Parameter must not be null!"); 
			CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "setLocaleValues", "Parameter: {0}", new Object[] { locales }, e);
			throw e;
		}

    	try
		{
    		TransferFunctions.getInstance().setLocaleValues(locales);
		}
		catch (TextContainerException e)
		{
			throw new TextContainerManagerException("TextContainerException", e);
		}
	}

	public boolean checkAdministrationPermission() throws TextContainerManagerException
	{
    	try
		{
    		return TextContainerSecurity.checkAdministrationPermission2();
		}
		catch (TextContainerException e)
		{
			throw new TextContainerManagerException("TextContainerException", e);
		}
	}

	// Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.api.TextContainerManagerImpl");
	private static final Category CATEGORY = Category.SYS_SERVER;

}
