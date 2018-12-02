/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */

package com.sap.engine.services.textcontainer.api;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.sap.engine.interfaces.textcontainer.context.TextContainerContext;
import com.sap.engine.services.textcontainer.TextContainerConfiguration;
import com.sap.engine.services.textcontainer.TextContainerException;
import com.sap.engine.services.textcontainer.TextContainerService;
import com.sap.engine.services.textcontainer.context.AllowedLanguages;
import com.sap.engine.services.textcontainer.context.Context;
import com.sap.engine.services.textcontainer.context.ContextResolution;
import com.sap.engine.services.textcontainer.context.SystemContext;
import com.sap.engine.services.textcontainer.dbaccess.ContainerData;
import com.sap.engine.services.textcontainer.dbaccess.ContainerIteratorLocales;
import com.sap.engine.services.textcontainer.dbaccess.ContextData;
import com.sap.engine.services.textcontainer.dbaccess.Ctx;
import com.sap.engine.services.textcontainer.dbaccess.LoadData;
import com.sap.engine.services.textcontainer.security.TextContainerMessageDigest;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
*
* Resource bundle implemenation for the Text Container.
*
* <br/><br/>Copyright (c) 2006, SAP AG
* @author  Thomas Goering
* @version 1.0
*/
public class TextContainerResourceBundle
{

	/**
	 * Gets a resource bundles using the specified component name and base name.
	 * <br/>Note, that the strategy for locating and instanciating the resource bundle differs from the
	 * <code>java.util.ResourceBundle</code> class. There is no class loader used in this method.
	 * 
	 * @param componentName The component name of the resource bundle.
	 * @param baseName The base name of the resource bundle.
	 * @return Resource bundle.
	 * @throws NullPointerException If <code>componentName</code> or <code>baseName</code> is <code>null</code>.
	 * @throws MissingResourceException - If no resource bundles for the specified component name and base name can be found.
	 */
	public static ResourceBundle[] getBundles(String componentName, String baseName)
    {
		ArrayList<ResourceBundle> bundles = new ArrayList<ResourceBundle>();

		if ((componentName == null) || (baseName == null))
		{
			String message = "Parameter null: ";
			String separator = "";
			if (componentName == null)
			{
				message += "componentName";
				separator = ", ";
			}
			if (baseName == null)
			{
				message += separator;
				message += "baseName";
			}
			NullPointerException e = new NullPointerException(message);
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getBundles", "Parameters: {0}, {1}", new Object[] { componentName, baseName }, e);
			throw e;
		}

    	try
    	{
    		Ctx ctx = new Ctx(TextContainerService.getDataSource().getConnection());

    		try
    		{
    			Context systemContext = SystemContext.getInstance().get();

    			byte[] componentHash = TextContainerMessageDigest.getDigestOptimizeAscii(componentName);

    			byte[] bundleHash = TextContainerMessageDigest.getDigestOptimizeAscii(baseName);

    			String[] languages;

    			if (AllowedLanguages.getInstance().getProcessedLanguagesAvailable())
    			{
//					At the moment PCD gets all languages that others also get, so the following code is commented out:
//					if (TextContainerRecipient.TCR_PCD.equals(BundleData.bundleToRecipient(ctx, bundleHash, componentHash)))
//	        			languages = LoadData.selectGroupedByLocale(ctx, componentHash, bundleHash, systemContext.getIndustry(), systemContext.getRegion(), systemContext.getExtension());
//					else
    				languages = AllowedLanguages.getInstance().getLanguages();
    			}
	        	else
	        	{
					// If text load is not used for caching get languages from the container table:
					if (!TextContainerConfiguration.useTextLoadForCachingIsActive())
					{
						ContainerIteratorLocales iterLocales = ContainerData.selectLocales(ctx, componentHash, bundleHash);

						ArrayList<String> list = new ArrayList<String>();

						while (iterLocales.next())
						{
					 		String locale = iterLocales.locale();
					 		if ((locale == null) || (locale.equals(" ")))
					 			locale = "";
							list.add(locale);
						}

						iterLocales.close();

						languages = (String[])list.toArray(new String[] {});
					}
					else
						languages = LoadData.selectGroupedByLocale(ctx, componentHash, bundleHash, systemContext.getIndustry(), systemContext.getRegion(), systemContext.getExtension());
	        	}

	        	if (languages != null)
	        	{
	        		int contextId;

					for (int i = 0; i < languages.length; i++)
					{
						ContextData contextData = new ContextData(languages[i], systemContext.getIndustry(), systemContext.getRegion(), systemContext.getExtension());

						contextId = ContextData.contextToId(ctx, contextData);

						if (contextId != 0)
						{
							try
							{
								bundles.add(new TextContainerResourceBundle().new TextContainerResourceBundleImpl(ctx, contextId, contextData, componentHash, bundleHash, languages[i]));
							}
							catch (MissingResourceException mre)
							{
//  							$JL-EXC$
							}
						}
					}
				}
			}
    		finally
    		{
    			if (ctx != null)
					ctx.close();
    		}
		}
        catch (Exception e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getBundles", "Parameters: {0}, {1}", new Object[] { componentName, baseName }, e);
		}

        if ((bundles == null) || (bundles.isEmpty()))
        {
        	throw new MissingResourceException("Can't find bundles for component " +
        			componentName + ", base name " + baseName,
        			componentName + "_" + baseName,
        			"");
        }

        return (ResourceBundle[]) bundles.toArray(new ResourceBundle[0]);
    }

	class TextContainerResourceBundleImpl extends ResourceBundle
	{

	    /**
		 * 
		 */
		public TextContainerResourceBundleImpl(Ctx ctx, int contextId, TextContainerContext context, byte[] componentHash, byte[] bundleHash, String locale) throws SQLException, TextContainerException
		{
			super();

			// If text load is not used for caching perform context resolution:
			if (!TextContainerConfiguration.useTextLoadForCachingIsActive())
			{
				texts = ContextResolution.getInstance().resolveComponentBundleWithContext(ctx, componentHash, bundleHash, context);
			}
			else
			{
				LoadData[] load = LoadData.select(ctx, contextId, componentHash, bundleHash);

				if (load != null)
				{
					texts = new Hashtable<String, String>();

					for (int j = 0; j < load.length; j++)
						texts.put(load[j].getTextKey(), load[j].getText());
				}
			}

	        if (texts == null)
	        {
	        	throw new MissingResourceException("Can't find bundle", "", "");
	        }

			this.textsLocale = new Locale(locale);
		}

		/* (non-Javadoc)
		 * @see java.util.ResourceBundle#getLocale()
		 */
		public Locale getLocale()
		{
			return textsLocale;
		}

		/* (non-Javadoc)
		 * @see java.util.ResourceBundle#setParent(java.util.ResourceBundle)
		 */
		protected void setParent(ResourceBundle parent)
		{
			super.setParent(parent);
		}

		/* (non-Javadoc)
		 * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
		 */
		protected Object handleGetObject(String key)
		{
			if (key == null)
			{
				NullPointerException e = new NullPointerException("Parameter null: key");
	    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "handleGetObject", "Parameters: {0}", new Object[] { key }, e);
				throw e;
			}

			return texts.get(key);
		}

		/* (non-Javadoc)
		 * @see java.util.ResourceBundle#getKeys()
		 */
		public Enumeration getKeys()
		{
			return texts.keys();
		}

		private Hashtable texts = null;

		private Locale textsLocale = null;

	}

	// Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.api.TextContainerResourceBundle");
	private static final Category CATEGORY = Category.SYS_SERVER;

}
