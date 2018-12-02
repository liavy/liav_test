/*
 * Created on 18.11.2005
 */
package com.sap.engine.services.textcontainer;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.sap.engine.frame.ApplicationServiceContext;
import com.sap.engine.frame.ApplicationServiceFrame;
import com.sap.engine.frame.ServiceException;
import com.sap.engine.frame.ServiceRuntimeException;
import com.sap.engine.interfaces.textcontainer.recipient.TextContainerRecipientListener;
import com.sap.engine.services.content.container.api.Helper;
import com.sap.engine.services.textcontainer.api.TextContainerManagerImpl;
import com.sap.engine.services.textcontainer.context.AllowedExtensions;
import com.sap.engine.services.textcontainer.context.AllowedIndustries;
import com.sap.engine.services.textcontainer.context.AllowedLanguages;
import com.sap.engine.services.textcontainer.context.AllowedRegions;
import com.sap.engine.services.textcontainer.context.ContextChains;
import com.sap.engine.services.textcontainer.context.SystemContext;
import com.sap.engine.services.textcontainer.context.TransferFunctions;
import com.sap.engine.services.textcontainer.dbaccess.ComponentData;
import com.sap.engine.services.textcontainer.dbaccess.ContextData;
import com.sap.engine.services.textcontainer.dbaccess.Ctx;
import com.sap.engine.services.textcontainer.handler.TextContainerHandler;
import com.sap.engine.services.textcontainer.message.TextContainerMessageListener;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * @author d028064
 */
public class TextContainerService implements ApplicationServiceFrame
{

	public void start(ApplicationServiceContext serviceContext) throws ServiceException
	{
        m_oAppServiceContext = serviceContext;

        Context ctx;

        try
		{
        	ctx = new InitialContext();
            TextContainerService.setDataSource((DataSource) ctx.lookup(DEFAULT_DATASOURCE_NAME));
		}
        catch (javax.naming.NamingException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "start", e);
            throw new ServiceException(LOCATION, e);
		}

        try
        {
			m_oHandler = TextContainerHandler.getInstance(new File(m_oAppServiceContext.getServiceState().getWorkingDirectoryName()).getCanonicalPath());
		}
        catch (IOException e)
        {
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "start", e);
            throw new ServiceException(LOCATION, e);
		}

        try
        {
			TextContainerConfiguration.setProperties(m_oAppServiceContext.getServiceState().getProperties());
		}
        catch (TextContainerException e)
        {
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "start", e);
            throw new ServiceException(LOCATION, e);
		}

        try
        {
        	TextContainerMessageListener.register();
        }
        catch (TextContainerException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "start", e);
            throw new ServiceException(LOCATION, e);
		}

        registerHandler(ctx);
        registerInterface();
        registerRuntimeConfiguration();

        // Initialization:
		try
		{
			Connection conn = getDataSource().getConnection();
			conn.setAutoCommit(false);
			Ctx dbCtx = new Ctx(conn);
			try
			{
				AllowedIndustries.getInstance().initialize(dbCtx);
				AllowedRegions.getInstance().initialize(dbCtx);
				AllowedExtensions.getInstance().initialize(dbCtx);
				AllowedLanguages.getInstance().initialize(dbCtx);
				TransferFunctions.getInstance().initialize(dbCtx);
				ContextChains.getInstance().initialize();
				SystemContext.getInstance().initialize(dbCtx);
				ComponentData.clear();
				ContextData.clear();
			}
			finally
			{
				if (dbCtx != null)
					dbCtx.close();
			}
		}
		catch (SQLException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "start", e);
            throw new ServiceException(LOCATION, e);
		}
		catch (TextContainerException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "start", e);
            throw new ServiceException(LOCATION, e);
		}

		initialized = true;
	}

    public void stop() throws ServiceRuntimeException
    {
    	initialized = false;

    	unregisterRuntimeConfiguration();
    	unregisterInterface();
    	unregisterHandler();

        try
        {
        	TextContainerMessageListener.unregister();
        }
        catch (TextContainerException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "stop", e);
            throw new ServiceRuntimeException(LOCATION, e);
		}

        // Don't clear the lists of all registered recipients and recipients2
        // because the service can reuse them when it is started again!
//      recipients = null;
//      recipients2 = null;

        // Don't clear the dataSource. It may be needed by the runtime (SAPResourceBundle),
        // although the Text Container service is stopped (not nice behaviour but nobody would
        // understand why there is a NullPointerException during text retrieval in this case. 
//    	TextContainerService.setDataSource(null);

        // Don't clear the serviceContext. It may be needed by the runtime (SAPResourceBundle),
        // although the Text Container service is stopped (not nice behaviour but nobody would
        // understand why there is a NullPointerException during text retrieval in this case. 
//    	m_oAppServiceContext = null;
	}

    private void registerHandler(Context ctx) throws ServiceException
    {
    	try
    	{
    		contentHelper = (Helper) ctx.lookup("tc~je~content");
    		contentHelper.getRegistryManager().registerContentHandler(m_oHandler);
    		handlerRegistered = true;
    	}
    	catch (NamingException e)
    	{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "registerHandler", e);
            throw new ServiceException(LOCATION, e);
    	}
    }

    private void unregisterHandler()
    {
    	if (handlerRegistered)
    	{
			contentHelper.getRegistryManager().unregisterContentHandler(m_oHandler.getInfo().getName());
			handlerRegistered = false;
    	}
    }

    private void registerInterface()
    {
    	m_oTextContainerRuntime = new TextContainerRuntimeImpl();
    	m_oTextContainerManager = new TextContainerManagerImpl();
    	m_oAppServiceContext.getContainerContext().getObjectRegistry().registerInterface(m_oTextContainerRuntime);
    	interfaceRegistered = true;
    	m_oAppServiceContext.getContainerContext().getObjectRegistry().registerInterfaceProvider(TEXTCONTAINER_API_INTERFACE, m_oTextContainerManager);
    	interfaceProviderRegistered = true;
    }

    private void unregisterInterface()
    {
    	if (interfaceRegistered)
    	{
    		m_oAppServiceContext.getContainerContext().getObjectRegistry().unregisterInterface();
    		interfaceRegistered = false;
    	}
    	if (interfaceProviderRegistered)
    	{
    		m_oAppServiceContext.getContainerContext().getObjectRegistry().unregisterInterfaceProvider(TEXTCONTAINER_API_INTERFACE);
    		interfaceProviderRegistered = false;
    	}
    	m_oTextContainerManager = null;
    	m_oTextContainerRuntime = null;
    }

    private void registerRuntimeConfiguration()
    {
    	runtimeConfiguration = new TextContainerConfiguration();
    	m_oAppServiceContext.getServiceState().registerRuntimeConfiguration(runtimeConfiguration);
    	runtimeConfigurationRegistered = true;
    }

    private void unregisterRuntimeConfiguration()
    {
    	if (runtimeConfigurationRegistered)
    	{
    		m_oAppServiceContext.getServiceState().unregisterRuntimeConfiguration();
    		runtimeConfigurationRegistered = false;
    	}
    	runtimeConfiguration = null;
    }

/* Removed because TextContainerRecipient notification has been switched off! 
    public static void registerRecipient(String recipient, TextContainerRecipient object) throws TextContainerException
	{
		if ((recipient == null) || (object == null))
		{
			TextContainerException e = new TextContainerException("Parameters must not be null!"); 
			CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "registerRecipient", "Parameters: {0, 1}", new Object[] { recipient, object }, e);
			throw e;
	    }

		if (recipients.containsKey(recipient))
			unregisterRecipient(recipient);

		recipients.put(recipient, object);
	}

	public static void unregisterRecipient(String recipient) throws TextContainerException
	{
		if (recipient == null)
		{
			TextContainerException e = new TextContainerException("Parameters must not be null!"); 
			CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "unregisterRecipient", "Parameters: {0}", new Object[] { recipient }, e);
			throw e;
	    }

		recipients.remove(recipient);
	}
*/

    public static void registerRecipientListener(String recipient, TextContainerRecipientListener listener) throws TextContainerException
	{
		if ((recipient == null) || (listener == null))
		{
			TextContainerException e = new TextContainerException("Parameters must not be null!"); 
			CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "registerRecipientListener", "Parameters: {0, 1}", new Object[] { recipient, listener }, e);
			throw e;
	    }

		ArrayList<TextContainerRecipientListener> list = recipientListeners.get(recipient);

		if (list == null)
		{
			list = new ArrayList<TextContainerRecipientListener>();

			recipientListeners.put(recipient, list);
		}

		if (!list.contains(listener))
			list.add(listener);
	}

	public static void unregisterRecipientListener(String recipient, TextContainerRecipientListener listener) throws TextContainerException
	{
		if ((recipient == null) || (listener == null))
		{
			TextContainerException e = new TextContainerException("Parameters must not be null!"); 
			CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "unregisterRecipientListener", "Parameters: {0, 1}", new Object[] { recipient, listener }, e);
			throw e;
	    }

		ArrayList<TextContainerRecipientListener> list = recipientListeners.get(recipient);

		if (list != null)
		{
			if (list.contains(listener))
			{
				list.remove(listener);

				if (list.isEmpty())
					recipientListeners.remove(recipient);
			}
		}
	}

    public static DataSource getDataSource()
	{
		return m_oDataSource;
	}

	public static void setDataSource(DataSource oDataSource)
	{
		m_oDataSource = oDataSource;
	}

	public static ApplicationServiceContext getServiceContext()
	{
		return m_oAppServiceContext;
	}

/* Removed because TextContainerRecipient notification has been switched off! 
	public static HashMap<String, TextContainerRecipient> getRecipients()
	{
		return recipients;
	}
*/

	public static HashMap<String, ArrayList<TextContainerRecipientListener>> getRecipientListeners()
	{
		return recipientListeners;
	}

	public static boolean isInitialized()
	{
		return initialized;
	}

	private static ApplicationServiceContext m_oAppServiceContext = null;
    private static TextContainerHandler m_oHandler;
    private static Helper contentHelper;
    private static TextContainerRuntimeImpl m_oTextContainerRuntime;
    private static TextContainerManagerImpl m_oTextContainerManager;
    private static TextContainerConfiguration runtimeConfiguration = null;

    private static boolean handlerRegistered;
    private static boolean interfaceRegistered;
    private static boolean interfaceProviderRegistered;
    private static boolean runtimeConfigurationRegistered;

/* Removed because TextContainerRecipient notification has been switched off! 
	private static HashMap<String, TextContainerRecipient> recipients = new HashMap<String, TextContainerRecipient>();
*/
	private static HashMap<String, ArrayList<TextContainerRecipientListener>> recipientListeners = new HashMap<String, ArrayList<TextContainerRecipientListener>>();

	private static boolean initialized = false;

	private static String TEXTCONTAINER_API_INTERFACE = "textcontainer_api";

    private static String DEFAULT_DATASOURCE_NAME = "jdbc/SAP/BC_TEXTCONTAINER";

    private static DataSource m_oDataSource = null;

    // Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.TextContainerService");
	private static final Category CATEGORY = Category.SYS_SERVER;

}
