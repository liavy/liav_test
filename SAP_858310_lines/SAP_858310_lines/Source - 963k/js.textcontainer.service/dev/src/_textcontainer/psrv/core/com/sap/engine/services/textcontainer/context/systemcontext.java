/*
 * Created on Jan 31, 2006
 */
package com.sap.engine.services.textcontainer.context;

import java.sql.Connection;
import java.sql.SQLException;

import com.sap.engine.services.textcontainer.TextContainerException;
import com.sap.engine.services.textcontainer.TextContainerService;
import com.sap.engine.services.textcontainer.dbaccess.ComponentData;
import com.sap.engine.services.textcontainer.dbaccess.ContextData;
import com.sap.engine.services.textcontainer.dbaccess.Ctx;
import com.sap.engine.services.textcontainer.dbaccess.DbFunctions;
import com.sap.engine.services.textcontainer.dbaccess.SystemContextData;
import com.sap.engine.services.textcontainer.message.TextContainerMessage;
import com.sap.engine.services.textcontainer.message.TextContainerMessageSender;
import com.sap.engine.services.textcontainer.security.TextContainerSecurity;
import com.sap.sql.NoDataException;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * @author d029702
 */
public class SystemContext
{
    // Singleton instance of this class
    static private SystemContext instance;

    static public SystemContext getInstance() throws TextContainerException
    {
        if (instance == null)
            instance = new SystemContext();

        return instance;
    }

    private SystemContext() throws TextContainerException
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
    	try
		{
			try
			{
				SystemContextData context = SystemContextData.select(ctx);

				systemContext.setIndustry(context.getIndustry());
				systemContext.setRegion(context.getRegion());
				systemContext.setExtension(context.getExtension());
			}
			catch (NoDataException e)
			{
				// $JL-EXC$
				systemContext.setIndustry("");
				systemContext.setRegion("");
				systemContext.setExtension("");
			}
		}
    	catch (SQLException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "initialize", e);
    		throw new TextContainerException("SQLException", e);
		}
	}

	public void set(String industry, String region, String extension) throws TextContainerException
	{
		TextContainerSecurity.checkAdministrationPermission();

		if ((industry == null) || (region == null) || (extension == null))
		{
			String message = "Parameter null: ";
			String separator = "";
			if (industry == null)
			{
				message += "industry";
				separator = ", ";
			}
			if (region == null)
			{
				message += separator;
				message += "region";
				separator = ", ";
			}
			if (extension == null)
			{
				message += separator;
				message += "extension";
			}
			NullPointerException e = new NullPointerException(message);
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "set", "Parameters: {0}, {1}, {2}", new Object[] { industry, region, extension }, e);
			throw e;
		}

		try
		{
			Connection conn = TextContainerService.getDataSource().getConnection();
			conn.setAutoCommit(false);

			Ctx ctx = new Ctx(conn);

			try
			{
				// Reread all other context attribute values and reset DB caches:
				AllowedIndustries.getInstance().initialize(ctx);
				AllowedRegions.getInstance().initialize(ctx);
				AllowedExtensions.getInstance().initialize(ctx);
				AllowedLanguages.getInstance().initialize(ctx);
				TransferFunctions.getInstance().initialize(ctx);
				ContextChains.getInstance().initialize();
				ComponentData.clear();
				ContextData.clear();

				if (!AllowedIndustries.getInstance().industryExists(industry))
		        {
		        	TextContainerException e = new TextContainerException("Industry does noch exist: " + industry);
		    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "set", "Parameters: {0}, {1}, {2}", new Object[] { industry, region, extension }, e);
					throw e;
		        }
		        if (!AllowedRegions.getInstance().regionExists(region))
		        {
		        	TextContainerException e = new TextContainerException("Region does noch exist: " + region);
		    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "set", "Parameters: {0}, {1}, {2}", new Object[] { industry, region, extension }, e);
					throw e;
		        }
		        if (!AllowedExtensions.getInstance().extensionExists(extension))
		        {
		        	TextContainerException e = new TextContainerException("Extension does noch exist: " + extension);
		    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "set", "Parameters: {0}, {1}, {2}", new Object[] { industry, region, extension }, e);
					throw e;
		        }

				systemContext.setIndustry(industry);
				systemContext.setRegion(region);
				systemContext.setExtension(extension);

				ContextResolution.getInstance().resolveAllTexts(ctx, get());

				SystemContextData.modify(ctx, new ContextData(systemContext.getLocale(), industry, region, extension));

				DbFunctions.commit(ctx);
			}
			finally
			{
				if (ctx != null)
					ctx.close();
			}

			initialize();

			TextContainerMessageSender.sendUpdateMessage(TextContainerMessage.TXV_CHANGED_SYSTEM_CONTEXT);
		}
    	catch (SQLException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "set", "Parameters: {0}, {1], {2]", new Object[] { industry, region, extension }, e);
    		throw new TextContainerException("SQLException", e);
		}
	}

	public Context get()
	{
		return systemContext;
	}

	protected Context systemContext = new Context("en", "", "", "");

	// Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.context.SystemContext");
	private static final Category CATEGORY = Category.SYS_SERVER;
}
