package com.sap.engine.services.textcontainer.context;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.sql.DataSource;

import com.sap.engine.interfaces.textcontainer.TextContainerLocale;
import com.sap.engine.interfaces.textcontainer.context.TextContainerRules;
import com.sap.engine.services.textcontainer.TextContainerException;
import com.sap.engine.services.textcontainer.TextContainerService;
import com.sap.engine.services.textcontainer.dbaccess.Ctx;
import com.sap.engine.services.textcontainer.dbaccess.LocaleData;
import com.sap.engine.services.textcontainer.message.TextContainerMessage;
import com.sap.engine.services.textcontainer.message.TextContainerMessageSender;
import com.sap.engine.services.textcontainer.security.TextContainerSecurity;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * @author d041138: Sep 8, 2005 (adopted by d029702: Mar 28, 2006)
 */
public class TransferFunctions
{
    private static final String LOCALE_DELIMITER = "_"; //$NON-NLS-1$

    private Hashtable<String, String[]> locales;
    private Hashtable<String, HashMap<String, String[]>> localeChains;

    private static TransferFunctions instance;

    public static TransferFunctions getInstance() throws TextContainerException
    {
        if (instance == null)
            instance = new TransferFunctions();

        return instance;
    }

    private TransferFunctions() throws TextContainerException
    {
    	initialize();
    }

    public void initialize() throws TextContainerException
    {
    	Ctx ctx = null;

    	try
        {
			ctx = new Ctx(TextContainerService.getDataSource().getConnection());

			try
			{
				initialize(ctx);
			}
			finally
			{
				if (ctx != null)
					ctx.close();
			}
		}
    	catch (SQLException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "initialize", e);
    		throw new TextContainerException("SQLException", e);
		}
    }

    public void initialize(Ctx ctx) throws TextContainerException
    {
    	locales = new Hashtable<String, String[]>();
    	localeChains = new Hashtable<String, HashMap<String, String[]>>();

        try
        {
			LocaleData[] localeData = LocaleData.select(ctx);

			String startLocale = "";

			ArrayList<String> list = new ArrayList<String>();

			for (int i = 0; i < localeData.length; i++)
			{
				if (!localeData[i].getStartLocale().equals(startLocale))
				{
					if (startLocale.length() > 0)
					{
						locales.put(startLocale, (String[]) list.toArray(new String[0]));

						list = new ArrayList<String>();
					}

					startLocale = localeData[i].getStartLocale();
				}

				list.add(localeData[i].getLocale());
			}

			if (startLocale.length() > 0)
			{
				locales.put(startLocale, (String[]) list.toArray(new String[0]));
			}
		}
    	catch (SQLException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "initialize", e);
    		throw new TextContainerException("SQLException", e);
		}
    }

    public void setLocaleValues(HashMap<String, TextContainerLocale[]> locales) throws TextContainerException
    {
		if (locales == null)
		{
			NullPointerException e = new NullPointerException("Parameter null: locales");
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "setLocaleValues", "Parameters: {0}", new Object[] { locales }, e);
			throw e;
		}

    	try
		{
    		TextContainerSecurity.checkAdministrationPermission();

			DataSource oDataSource = TextContainerService.getDataSource();

			Ctx ctx = new Ctx(oDataSource.getConnection());

			try
			{
				LocaleData.delete(ctx);

				TextContainerLocale[] localeData;

				Iterator<TextContainerLocale[]> iter = locales.values().iterator();

				while (iter.hasNext())
				{
					localeData = iter.next();

					for (int i = 0; i < localeData.length; i++)
					{
						LocaleData.insert(ctx, new LocaleData(localeData[i].getStartLocale(), localeData[i].getSequenceNumber(),
								localeData[i].getLocale()));
					}
				}
			}
			finally
			{
				if (ctx != null)
					ctx.close();
			}

			initialize();
			ContextChains.getInstance().initialize();

			TextContainerMessageSender.sendUpdateMessage(TextContainerMessage.TXV_CHANGED_LOCALES);
		}
    	catch (SQLException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "setLocaleValues", "Parameters: {0}", new Object[] { locales }, e);
    		throw new TextContainerException("SQLException", e);
		}
    }

    /**
     * The values of the industry attribute are organized in a tree. The
     * predecessor of a value is the parent of the corresponding tree node. The
     * predecessor of the root node (industry="") does not exist.
     * 
     * @param industry
     *            Value of the industry attribute
     * @return Predecessor of the industry attribute. If no predecessor exists
     *         null is returned.
     */
    public String getPreviousIndustry(String industry) throws TextContainerException
    {
        return AllowedIndustries.getInstance().getParent(industry);
    }

    /**
     * The values of the region attribute are organized in a tree. The
     * predecessor of a value is the parent of the corresponding tree node. The
     * predecessor of the root node (region="") does not exist.
     * 
     * @param region
     *            Value of the region attribute
     * @return Predecessor of the industry attribute. If no predecessor exists
     *         null is returned.
     */
    public String getPreviousRegion(String region) throws TextContainerException
    {
        return AllowedRegions.getInstance().getParent(region);
    }

    /**
     * Currently the extension attribute is used as a flag that can take the
     * values "" and "X" where "" is followed by "X".
     * 
     * Implementation detail: This method seems to be over-pedantic. It is
     * introduced to separate the logic of finding the predecessor of an
     * extension from the context resolution proper.
     * 
     * @param extension
     *            The extension.
     * @return The predecessor ot the given extension or null if there is none.
     */
    /**
     * @param extension
     * @return
     */
    public String getPreviousExtension(String extension) throws TextContainerException
    {
      return AllowedExtensions.getInstance().getParent(extension);
    }

    /**
     * @param task
     *            The task.
     * @param locale
     *            The locale.
     * @param masterLocale
     *            The master locale.
     * @return The locale chain.
     * @throws TextContainerException
     */
    public String[] getLocaleChain(String task, String locale, String masterLocale) throws TextContainerException
    {
	    String defaultLocale = "en";

	    ArrayList<String> lc = new ArrayList<String>();

	    // Compute complete locale chain only for task CR_VIEW:
	    if (task.equals(TextContainerRules.CR_VIEW))
	    {
	    	HashMap<String, String[]> localeChainForLocale = localeChains.get(locale);
	    	String[] localeChain;

	    	// Is there a cached locale chain for the given locale?
	    	if (localeChainForLocale != null)
	    	{
	    		// Lookup the cached locale chain for the given masterLocale:
	    		localeChain = localeChainForLocale.get(masterLocale);

	    		// If there is one, we are finished:
	    		if (localeChain != null)
	    			return localeChain;
	    	}

	    	String pureLocale;
	    	String pureLanguage;

	   		ArrayList<String> templc = new ArrayList<String>();

	   		// Add the given locale to a temp locale chain:
	   		templc.add(locale);

	   		// Add the language+country of the given locale to a temp locale chain:
		    if ((pureLocale = removeVariant(locale)) != null)
		    	templc.add(pureLocale);

	   		// Add the language of the given locale to a temp locale chain:
		    if ((pureLanguage = removeCountry(locale)) != null)
		    	templc.add(pureLanguage);

		    String secondaryLocale = AllowedLanguages.getInstance().getSecondaryLocale();

		    // If there is a secondary locale, add it to the temp locale chain:
		    if ((secondaryLocale != null) && (!templc.contains(secondaryLocale)))
		    	templc.add(secondaryLocale);

		    // Add the default locale ('en)' to the temp locale chain:
		    if (!templc.contains(defaultLocale))
		    	templc.add(defaultLocale);

	    	String currentLocale;

	    	// Now loop over all entries in the temp locale chain ...
	    	for (int i = 0; i < templc.size(); i++)
	    	{
	    		currentLocale = templc.get(i);

	    		// ... and finish if the current entry has a predefined locale chain:
	    		if (addLocale(lc, currentLocale))
	    			break;

	    		lc.add(currentLocale);
	    	}

	    	// Add the master locale to the locale chain:
	    	if ((masterLocale != null) && (!lc.contains(masterLocale)))
	    		lc.add(masterLocale);

	    	// Add the empty locale to the locale chain:
	   		lc.add("");

	   		templc = lc;
	   		lc = new ArrayList<String>();

	   		// This loop ensures that only the first existence of a locale stays in the locale chain:
	   		for (int i = 0; i < templc.size(); i++)
	   		{
	   			if (!lc.contains(templc.get(i)))
	   				lc.add(templc.get(i));
	   		}

	   		// Add the new locale chain to the cache:
	   		if (localeChainForLocale == null)
    		{
    			localeChainForLocale = new HashMap<String, String[]>();
    			localeChains.put(locale, localeChainForLocale);
	    	}

	    	localeChain = (String[]) lc.toArray(new String[0]);
	    	localeChainForLocale.put(masterLocale, localeChain);

	    	return localeChain;
	    }
	    else
		{
		    // CR_VERT and CR_VIEW_NLF get a simple language fallback:
    		lc.add(locale);

	    	if ((masterLocale != null) && (masterLocale.equals(locale)))
	    		lc.add("");
		}

        return (String[]) lc.toArray(new String[0]);
    }

    private String removeVariant(String locale)
    {
    	if (locale == null)
    		return null;

    	int index = locale.indexOf(LOCALE_DELIMITER);

    	if (index < 0)
    	{
            return null;
    	}
        else
        {
        	int index2 = locale.indexOf(LOCALE_DELIMITER, index + 1);

        	if (index2 < 0)
                return null;
            else
                return locale.substring(0, index2);
        }
    }

    private String removeCountry(String locale)
    {
    	if (locale == null)
    		return null;

    	int index = locale.indexOf(LOCALE_DELIMITER);

    	if (index < 0)
            return null;
        else
            return locale.substring(0, index);
    }

    // Returns true if locale has it's own locale chain (also add the locale chain to lc)
    private boolean addLocale(ArrayList<String> lc, String locale)
    {
    	String[] lLocales;

    	if ((locale != null) && ((lLocales = locales.get(locale)) != null))
    	{
    		for (int i = 0; i < lLocales.length; i++)
    			lc.add(lLocales[i]);

    		return true;
    	}

    	return false;
    }

	// Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.context.TransferFunctions");
	private static final Category CATEGORY = Category.SYS_SERVER;
}