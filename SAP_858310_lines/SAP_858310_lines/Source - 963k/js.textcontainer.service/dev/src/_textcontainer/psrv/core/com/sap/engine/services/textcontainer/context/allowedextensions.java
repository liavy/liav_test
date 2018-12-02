package com.sap.engine.services.textcontainer.context;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.sql.DataSource;

import com.sap.engine.interfaces.textcontainer.TextContainerExtension;
import com.sap.engine.services.textcontainer.TextContainerException;
import com.sap.engine.services.textcontainer.TextContainerService;
import com.sap.engine.services.textcontainer.dbaccess.Ctx;
import com.sap.engine.services.textcontainer.dbaccess.ExtensionData;
import com.sap.engine.services.textcontainer.message.TextContainerMessage;
import com.sap.engine.services.textcontainer.message.TextContainerMessageSender;
import com.sap.engine.services.textcontainer.security.TextContainerSecurity;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Contains the allowed values for the attribute EXTENSION. The values of this
 * attribute are organized in a tree which is valid for all installations at
 * SAP, partners and customers. The tree nodes have names which are the possible
 * values of the attribute EXTENSION. Node names are always non-empty strings
 * except for the root node.
 * <p>
 * 
 * @author d041138: Mar 15, 2006 (adopted by d029702: Apr 04, 2006)
 */
public class AllowedExtensions
{
    // For every node in the tree, the parent is stored as String.
    private Hashtable<String, String> extensions;

    // Singleton instance of this class
    static private AllowedExtensions instance;

    static public AllowedExtensions getInstance() throws TextContainerException
    {
        if (instance == null)
            instance = new AllowedExtensions();

        return instance;
    }

    private AllowedExtensions() throws TextContainerException
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
    	extensions = new Hashtable<String, String>();

        try
        {
			ExtensionData[] extensionArray = ExtensionData.select(ctx);

			for (int i = 0; i < extensionArray.length; i++)
			{
				ExtensionData extension = extensionArray[i];
				if (!extension.getExtension().equals(""))
					extensions.put(extension.getExtension(), extension.getFather());
			}
		}
    	catch (SQLException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "initialize", e);
    		throw new TextContainerException("SQLException", e);
		}
    }

	public void set(HashMap<String, TextContainerExtension> extensions) throws TextContainerException
	{
		if (extensions == null)
		{
			NullPointerException e = new NullPointerException("Parameter null: extensions");
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "set", "Parameters: {0}", new Object[] { extensions }, e);
			throw e;
		}

    	try
		{
    		TextContainerSecurity.checkAdministrationPermission();

			DataSource oDataSource = TextContainerService.getDataSource();

			Ctx ctx = new Ctx(oDataSource.getConnection());

			try
			{
				ExtensionData.delete(ctx);

				TextContainerExtension extension;

				Iterator<TextContainerExtension> iter = extensions.values().iterator();

				while (iter.hasNext())
				{
					extension = iter.next();

					ExtensionData.insert(ctx, new ExtensionData(extension.getExtension(), extension.getFather(),
							extension.getTermDomain(), extension.getCollKey()));
				}
			}
			finally
			{
				if (ctx != null)
					ctx.close();
			}

			initialize();
			ContextChains.getInstance().initialize();

			TextContainerMessageSender.sendUpdateMessage(TextContainerMessage.TXV_CHANGED_EXTENSIONS);
		}
    	catch (SQLException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "set", "Parameters: {0}", new Object[] { extensions }, e);
    		throw new TextContainerException("SQLException", e);
		}
	}

	public boolean extensionExists(String extension)
	{
		boolean exists = false;

		if (extension != null)
		{
			// Root node always exists
			if (extension.equals(""))
				return true;

			if (extensions.get(extension) != null)
				exists = true;
		}

		return exists;
	}

    public String getParent(String extension)
    {
        return extensions.get(extension);
    }

	// Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.context.AllowedExtensions");
	private static final Category CATEGORY = Category.SYS_SERVER;
}