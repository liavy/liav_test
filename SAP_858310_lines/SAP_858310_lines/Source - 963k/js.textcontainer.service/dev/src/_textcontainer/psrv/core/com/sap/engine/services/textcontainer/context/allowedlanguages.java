package com.sap.engine.services.textcontainer.context;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.sap.engine.interfaces.textcontainer.TextContainerLanguage;
import com.sap.engine.services.textcontainer.TextContainerException;
import com.sap.engine.services.textcontainer.TextContainerService;
import com.sap.engine.services.textcontainer.dbaccess.Ctx;
import com.sap.engine.services.textcontainer.dbaccess.LanguageData;
import com.sap.engine.services.textcontainer.message.TextContainerMessage;
import com.sap.engine.services.textcontainer.message.TextContainerMessageSender;
import com.sap.engine.services.textcontainer.security.TextContainerSecurity;
import com.sap.s2x.S2XDocument;
import com.sap.s2x.S2XTypes;
import com.sap.s2x.core.types.LangType;
import com.sap.s2x.validation.ValidationException;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Contains the processed values for the attribute LOCALE.
 * <p>
 * 
 * @author d041138: Mar 15, 2006 (adopted by d029702: Apr 04, 2006)
 */
public class AllowedLanguages
{
    private String[] languages;

    private String secondaryLocale;

    private boolean processedLanguagesAvailable;

    // Singleton instance of this class
    static private AllowedLanguages instance;

    static public AllowedLanguages getInstance() throws TextContainerException
    {
        if (instance == null)
            instance = new AllowedLanguages();

        return instance;
    }

    private AllowedLanguages() throws TextContainerException
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
    	languages = null;
    	secondaryLocale = null;
    	processedLanguagesAvailable = false;

    	ArrayList<String> languagesArray = new ArrayList<String>();

        try
        {
			LanguageData[] languageArray = LanguageData.select(ctx);

			for (int i = 0; i < languageArray.length; i++)
			{
				LanguageData language = languageArray[i];
				if (!language.getLocale().equals(""))
				{
					languagesArray.add(language.getLocale());

					if (language.getIsSecondaryLocale())
						secondaryLocale = language.getLocale();
				}
			}

			if (languagesArray.size() > 0)
			{
				languagesArray.add("");

				languages = (String[]) languagesArray.toArray(new String[0]);

				processedLanguagesAvailable = true;
			}
			else
			{
	    		try
	    		{
//	    			This dummy instanciation is needed for initializing the S2XTypes.getLangTypes() (see below)!
					new S2XDocument();
				}
	    		catch (IOException e)
	    		{
	    	    	CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "initialize", e);
	    	    	throw new TextContainerException("IOException", e);
				}
	    		catch (ParserConfigurationException e)
	    		{
	    	    	CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "initialize", e);
	    	    	throw new TextContainerException("ParserConfigurationException", e);
				}
	    		catch (SAXException e)
	    		{
	    	    	CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "initialize", e);
	    	    	throw new TextContainerException("SAXException", e);
				}
				catch (ValidationException e)
				{
			    	CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "initialize", e);
			    	throw new TextContainerException("ValidationException", e);
				}

				Iterator iter = S2XTypes.getInstance().getLangTypes().iterator();

				LangType value;

				while (iter.hasNext())
				{
					value = (LangType) iter.next();
					languagesArray.add(value.getXLIFFValue());
				}

				languages = (String[]) languagesArray.toArray(new String[0]);
			}
        }
	    catch (SQLException e)
		{
	    	CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "initialize", e);
	    	throw new TextContainerException("SQLException", e);
		}
    }

	public void set(HashMap<String, TextContainerLanguage> languages) throws TextContainerException
	{
		if (languages == null)
		{
			NullPointerException e = new NullPointerException("Parameter null: languages");
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "set", "Parameters: {0}", new Object[] { languages }, e);
			throw e;
		}

    	try
		{
    		TextContainerSecurity.checkAdministrationPermission();

			DataSource oDataSource = TextContainerService.getDataSource();

			Ctx ctx = new Ctx(oDataSource.getConnection());

			try
			{
				LanguageData.delete(ctx);

				TextContainerLanguage language;

				Iterator<TextContainerLanguage> iter = languages.values().iterator();

				while (iter.hasNext())
				{
					language = iter.next();

					LanguageData.insert(ctx, new LanguageData(language.getLocale(), language.getIsSecondaryLocale()));
				}
			}
			finally
			{
				if (ctx != null)
					ctx.close();
			}

			initialize();
			ContextChains.getInstance().initialize();

			TextContainerMessageSender.sendUpdateMessage(TextContainerMessage.TXV_CHANGED_LANGUAGES);
		}
    	catch (SQLException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "set", "Parameters: {0}", new Object[] { languages }, e);
    		throw new TextContainerException("SQLException", e);
		}
	}

	public boolean contains(String language)
	{
		boolean found = false;

		if (languages != null)
		{
			for (int i = 0; i < languages.length; i++)
			{
				if (languages[i].equals(language))
				{
					found = true;

					break;
				}
			}
		}

		return found;
	}

	public boolean isEmpty()
	{
		return (languages == null);
	}

	public String[] getLanguages()
	{
		return languages;
	}

	public String getSecondaryLocale()
	{
		return secondaryLocale;
	}

	public boolean getProcessedLanguagesAvailable()
	{
		return processedLanguagesAvailable;
	}

	// Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.context.AllowedLanguages");
	private static final Category CATEGORY = Category.SYS_SERVER;
}