package com.sap.engine.services.textcontainer.context;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.sql.DataSource;

import com.sap.engine.interfaces.textcontainer.TextContainerIndustry;
import com.sap.engine.services.textcontainer.TextContainerException;
import com.sap.engine.services.textcontainer.TextContainerService;
import com.sap.engine.services.textcontainer.dbaccess.Ctx;
import com.sap.engine.services.textcontainer.dbaccess.IndustryData;
import com.sap.engine.services.textcontainer.message.TextContainerMessage;
import com.sap.engine.services.textcontainer.message.TextContainerMessageSender;
import com.sap.engine.services.textcontainer.security.TextContainerSecurity;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Contains the allowed values for the attribute INDUSTRY. The values of this
 * attribute are organized in a tree which is valid for all installations at
 * SAP, partners and customers. The tree nodes have names which are the possible
 * values of the attribute INDUSTRY. Node names are always non-empty strings
 * except for the root node.
 * <p>
 * 
 * @author d041138: Mar 15, 2006 (adopted by d029702: Apr 04, 2006)
 */
public class AllowedIndustries
{
    // For every node in the tree, the parent is stored as String.
    private Hashtable<String, String> industries;

    // Singleton instance of this class
    static private AllowedIndustries instance;

    static public AllowedIndustries getInstance() throws TextContainerException
    {
        if (instance == null)
            instance = new AllowedIndustries();

        return instance;
    }

    private AllowedIndustries() throws TextContainerException
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
        industries = new Hashtable<String, String>();

        try
        {
			IndustryData[] industryArray = IndustryData.select(ctx);

			for (int i = 0; i < industryArray.length; i++)
			{
				IndustryData industry = industryArray[i];
				if (!industry.getIndustry().equals(""))
					industries.put(industry.getIndustry(), industry.getFather());
			}
        }
    	catch (SQLException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "initialize", e);
    		throw new TextContainerException("SQLException", e);
		}
    }

	public void set(HashMap<String, TextContainerIndustry> industries) throws TextContainerException
	{
		if (industries == null)
		{
			NullPointerException e = new NullPointerException("Parameter null: industries");
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "set", "Parameters: {0}", new Object[] { industries }, e);
			throw e;
		}

    	try
		{
    		TextContainerSecurity.checkAdministrationPermission();

    		DataSource oDataSource = TextContainerService.getDataSource();

			Ctx ctx = new Ctx(oDataSource.getConnection());

			try
			{
				IndustryData.delete(ctx);

				TextContainerIndustry industry;

				Iterator<TextContainerIndustry> iter = industries.values().iterator();

				while (iter.hasNext())
				{
					industry = iter.next();

					IndustryData.insert(ctx, new IndustryData(industry.getIndustry(), industry.getFather(),
							industry.getTermDomain(), industry.getCollKey()));
				}
			}
			finally
			{
				if (ctx != null)
					ctx.close();
			}

			initialize();
			ContextChains.getInstance().initialize();

			TextContainerMessageSender.sendUpdateMessage(TextContainerMessage.TXV_CHANGED_INDUSTRIES);
		}
    	catch (SQLException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "set", "Parameters: {0}", new Object[] { industries }, e);
    		throw new TextContainerException("SQLException", e);
		}
	}

	public boolean industryExists(String industry)
	{
		boolean exists = false;

		if (industry != null)
		{
			// Root node always exists
			if (industry.equals(""))
				return true;

			if (industries.get(industry) != null)
				exists = true;
		}

		return exists;
	}

	public String getParent(String industry)
    {
        return industries.get(industry);
    }

	// Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.context.AllowedIndustries");
	private static final Category CATEGORY = Category.SYS_SERVER;
}