package com.sap.engine.services.textcontainer.context;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.sql.DataSource;

import com.sap.engine.interfaces.textcontainer.TextContainerRegion;
import com.sap.engine.services.textcontainer.TextContainerException;
import com.sap.engine.services.textcontainer.TextContainerService;
import com.sap.engine.services.textcontainer.dbaccess.Ctx;
import com.sap.engine.services.textcontainer.dbaccess.RegionData;
import com.sap.engine.services.textcontainer.message.TextContainerMessage;
import com.sap.engine.services.textcontainer.message.TextContainerMessageSender;
import com.sap.engine.services.textcontainer.security.TextContainerSecurity;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Contains the allowed values for the attribute REGION. The values of this
 * attribute are organized in a tree which is valid for all installations at
 * SAP, partners and customers. The tree nodes have names which are the possible
 * values of the attribute REGION. Node names are always non-empty strings
 * except for the root node.
 * <p>
 * 
 * @author d041138: Mar 15, 2006 (adopted by d029702: Apr 04, 2006)
 */
public class AllowedRegions
{
    // For every node in the tree, the parent is stored as String.
    private Hashtable<String, String> regions;

    // Singleton instance of this class
    static private AllowedRegions instance;

    static public AllowedRegions getInstance() throws TextContainerException
    {
        if (instance == null)
            instance = new AllowedRegions();

        return instance;
    }

    private AllowedRegions() throws TextContainerException
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
        regions = new Hashtable<String, String>();

        try
        {
			RegionData[] regionArray = RegionData.select(ctx);

			for (int i = 0; i < regionArray.length; i++)
			{
				RegionData region = regionArray[i];
				if (!region.getRegion().equals(""))
					regions.put(region.getRegion(), region.getFather());
			}
		}
	    catch (SQLException e)
		{
	    	CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "initialize", e);
	    	throw new TextContainerException("SQLException", e);
		}
    }

	public void set(HashMap<String, TextContainerRegion> regions) throws TextContainerException
	{
		if (regions == null)
		{
			NullPointerException e = new NullPointerException("Parameter null: regions");
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "set", "Parameters: {0}", new Object[] { regions }, e);
			throw e;
		}

    	try
		{
    		TextContainerSecurity.checkAdministrationPermission();

			DataSource oDataSource = TextContainerService.getDataSource();

			Ctx ctx = new Ctx(oDataSource.getConnection());

			try
			{
				RegionData.delete(ctx);

				TextContainerRegion region;

				Iterator<TextContainerRegion> iter = regions.values().iterator();

				while (iter.hasNext())
				{
					region = iter.next();

					RegionData.insert(ctx, new RegionData(region.getRegion(), region.getFather(),
							region.getTermDomain(), region.getCollKey()));
				}
			}
			finally
			{
				if (ctx != null)
					ctx.close();
			}

			initialize();
			ContextChains.getInstance().initialize();

			TextContainerMessageSender.sendUpdateMessage(TextContainerMessage.TXV_CHANGED_REGIONS);
		}
    	catch (SQLException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "set", "Parameters: {0}", new Object[] { regions }, e);
    		throw new TextContainerException("SQLException", e);
		}
	}

	public boolean regionExists(String region)
	{
		boolean exists = false;

		if (region != null)
		{
			// Root node always exists
			if (region.equals(""))
				return true;

			if (regions.get(region) != null)
				exists = true;
		}

		return exists;
	}

    public String getParent(String region)
    {
        return regions.get(region);
    }

	// Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.context.AllowedRegions");
	private static final Category CATEGORY = Category.SYS_SERVER;
}