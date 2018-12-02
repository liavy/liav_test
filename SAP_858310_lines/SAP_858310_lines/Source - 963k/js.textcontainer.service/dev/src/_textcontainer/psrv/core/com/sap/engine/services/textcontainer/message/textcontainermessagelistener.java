package com.sap.engine.services.textcontainer.message;

import com.sap.engine.frame.cluster.message.ListenerAlreadyRegisteredException;
import com.sap.engine.frame.cluster.message.MessageAnswer;
import com.sap.engine.frame.cluster.message.MessageContext;
import com.sap.engine.frame.cluster.message.MessageListener;
import com.sap.engine.services.textcontainer.TextContainerException;
import com.sap.engine.services.textcontainer.TextContainerService;
import com.sap.engine.services.textcontainer.context.AllowedExtensions;
import com.sap.engine.services.textcontainer.context.AllowedIndustries;
import com.sap.engine.services.textcontainer.context.AllowedLanguages;
import com.sap.engine.services.textcontainer.context.AllowedRegions;
import com.sap.engine.services.textcontainer.context.ContextChains;
import com.sap.engine.services.textcontainer.context.SystemContext;
import com.sap.engine.services.textcontainer.context.TransferFunctions;
import com.sap.engine.services.textcontainer.dbaccess.ContextData;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class TextContainerMessageListener implements MessageListener
{

    private static TextContainerMessageListener instance;

    private TextContainerMessageListener()
    {
    }

    public static TextContainerMessageListener getInstance()
    {
        if (instance == null)
            instance = new TextContainerMessageListener();

    	return instance;
    }

	public static void register() throws TextContainerException
	{
		MessageContext messageContext = TextContainerService.getServiceContext().getClusterContext().getMessageContext();

    	try
    	{
    		messageContext.registerListener(getInstance());
    	}
    	catch (ListenerAlreadyRegisteredException e)
    	{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "register", e);
    		throw new TextContainerException("ListenerAlreadyRegisteredException", e);
    	}
	}

	public static void unregister() throws TextContainerException
	{
    	MessageContext messageContext = TextContainerService.getServiceContext().getClusterContext().getMessageContext();

   		messageContext.unregisterListener();
	}

	public void receive(int clusterId, int messageType, byte[] body, int offset, int length)
	{
    	String METHOD = "receive";

    	LOCATION.entering(METHOD);

   		CATEGORY.logT(Severity.DEBUG, LOCATION, "Message received");

		try
		{
			boolean initializeContextChains = false;

			byte changed = 0;

			if ((offset == 0) && (length == 1))
				changed = body[0];

			if ((changed & TextContainerMessage.TXV_CHANGED_INDUSTRIES) != 0)
			{
				AllowedIndustries.getInstance().initialize();
				initializeContextChains = true;
			}

			if ((changed & TextContainerMessage.TXV_CHANGED_REGIONS) != 0)
			{
				AllowedRegions.getInstance().initialize();
				initializeContextChains = true;
			}

			if ((changed & TextContainerMessage.TXV_CHANGED_EXTENSIONS) != 0)
			{
				AllowedExtensions.getInstance().initialize();
				initializeContextChains = true;
			}

			if ((changed & TextContainerMessage.TXV_CHANGED_LANGUAGES) != 0)
			{
				AllowedLanguages.getInstance().initialize();
				initializeContextChains = true;
			}

			if ((changed & TextContainerMessage.TXV_CHANGED_LOCALES) != 0)
			{
				TransferFunctions.getInstance().initialize();
				initializeContextChains = true;
			}

			if (initializeContextChains)
				ContextChains.getInstance().initialize();

			if ((changed & TextContainerMessage.TXV_CHANGED_SYSTEM_CONTEXT) != 0)
				SystemContext.getInstance().initialize();

			if ((changed & TextContainerMessage.TXV_CHANGED_CONTEXT_IDS) != 0)
				ContextData.clear();
		}
	   	catch (TextContainerException e)
		{
	   		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "receive", "Parameters: {0}, {1}, {2}, {3} and {4}", new Object[] { clusterId, messageType, body, offset, length }, e);
		}
	   	finally
	   	{
	   		LOCATION.exiting(METHOD);
	   	}
	}

	public MessageAnswer receiveWait(int clusterId, int messageType, byte[] body, int offset, int length) throws Exception
	{
		return null;
	}

    // Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.message.TextContainerMessageListener");
	private static final Category CATEGORY = Category.SYS_SERVER;

}
