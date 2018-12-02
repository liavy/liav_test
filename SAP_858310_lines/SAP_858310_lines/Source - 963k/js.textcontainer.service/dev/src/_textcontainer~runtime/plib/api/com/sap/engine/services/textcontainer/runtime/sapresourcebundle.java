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

package com.sap.engine.services.textcontainer.runtime;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.naming.InitialContext;
import javax.naming.NamingException;

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
public class SAPResourceBundle
{

	/**
	 * Gets a resource bundle using the specified base name and locale,
	 * and one class object of the resource bundle's component.
	 * <br/>Note, that the strategy for locating and instanciating the resource bundle differs from the
	 * <code>java.util.ResourceBundle</code> class. There is no class loader used in this method.
	 * The class object is used for retrieving the component name.
	 * 
	 * @param baseName The base name of the resource bundle.
	 * @param locale The locale for which a resource bundle is desired.
	 * @param cls A class object of the component where the resource bundle is in. This is needed for retrieving the component name.
	 * @return Resource bundle.
	 * @throws NullPointerException If <code>baseName</code>, <code>locale</code> or <code>cls</code> is <code>null</code>.
	 * @throws MissingResourceException - If no resource bundle for the specified base name can be found.
	 */
	public static ResourceBundle getBundle(String baseName, Locale locale, Class cls)
    {
		if ((baseName == null) || (locale == null) || (cls == null))
		{
			String message = "Parameter null: ";
			String separator = "";
			if (baseName == null)
			{
				message += "baseName";
				separator = ", ";
			}
			if (locale == null)
			{
				message += separator;
				message += "locale";
				separator = ", ";
			}
			if (cls == null)
			{
				message += separator;
				message += "cls";
			}
			NullPointerException e = new NullPointerException(message);
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getBundle", "Parameters: {0}, {1}, {2}", new Object[] { baseName, locale, cls }, e);
			throw e;
		}

		return new SAPResourceBundle().new SAPResourceBundleImpl(baseName, locale, cls);
    }

	/**
	 * Gets a resource bundle using the specified base name and locale,
	 * and the class loader of the resource bundle's component.
	 * <br/>Note, that the strategy for locating and instanciating the resource bundle differs from the
	 * <code>java.util.ResourceBundle</code> class. The class loader is only used for retrieving the
	 * component name.
	 * 
	 * @param baseName The base name of the resource bundle.
	 * @param locale The locale for which a resource bundle is desired.
	 * @param loader The class loader of the component where the resource bundle is in. This is needed for retrieving the component name.
	 * @return Resource bundle.
	 * @throws NullPointerException If <code>baseName</code>, <code>locale</code> or <code>loader</code> is <code>null</code>.
	 * @throws MissingResourceException - If no resource bundle for the specified base name can be found.
	 */
	public static ResourceBundle getBundle(String baseName, Locale locale, ClassLoader loader)
    {
		if ((baseName == null) || (locale == null) || (loader == null))
		{
			String message = "Parameter null: ";
			String separator = "";
			if (baseName == null)
			{
				message += "baseName";
				separator = ", ";
			}
			if (locale == null)
			{
				message += separator;
				message += "locale";
				separator = ", ";
			}
			if (loader == null)
			{
				message += separator;
				message += "loader";
			}
			NullPointerException e = new NullPointerException(message);
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getBundle", "Parameters: {0}, {1}, {2}", new Object[] { baseName, locale, loader }, e);
			throw e;
		}

		return new SAPResourceBundle().new SAPResourceBundleImpl(baseName, locale, loader);
    }

	class SAPResourceBundleImpl extends ResourceBundle
	{

	    /**
		 * 
		 */
		public SAPResourceBundleImpl(String baseName, Locale locale, Class cls)
		{
			super();

			initializeRuntime();

			texts = runtimeImpl.getTexts(baseName, locale, cls);

			if (texts == null)
				texts = new Hashtable();
		}

	    /**
		 * 
		 */
		public SAPResourceBundleImpl(String baseName, Locale locale, ClassLoader loader)
		{
			super();

			initializeRuntime();

			texts = runtimeImpl.getTexts(baseName, locale, loader);

			if (texts == null)
				texts = new Hashtable();
		}

		/* (non-Javadoc)
		 * @see java.util.ResourceBundle#getLocale()
		 */
		public Locale getLocale()
		{
			return super.getLocale();
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

	}

	protected static void initializeRuntime()
	{
		if (runtimeImpl == null)
		{
			try
			{
				InitialContext ctx = new InitialContext();
				runtimeImpl = (ITextContainerRuntime) ctx.lookup("textcontainer");
			}
			catch (NamingException e)
			{
	    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "initializeRuntime", e);
			}

			if (runtimeImpl == null)
			{
				MissingResourceException e = new MissingResourceException("Can't retrieve Text Container interface!", "", "");
	    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "initializeRuntime", e);
	        	throw e;
			}
		}
	}

	protected static ITextContainerRuntime runtimeImpl;

	// Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.runtime.SAPResourceBundle");
	private static final Category CATEGORY = Category.SYS_SERVER;

}
