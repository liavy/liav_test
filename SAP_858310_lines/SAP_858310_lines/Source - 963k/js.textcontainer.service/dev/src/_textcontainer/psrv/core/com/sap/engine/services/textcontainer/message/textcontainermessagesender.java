package com.sap.engine.services.textcontainer.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.sap.engine.frame.cluster.ClusterElement;
import com.sap.engine.frame.cluster.ClusterException;
import com.sap.engine.frame.cluster.message.MessageContext;
import com.sap.engine.interfaces.textcontainer.recipient.TextContainerRecipientChangedEvent;
import com.sap.engine.interfaces.textcontainer.recipient.TextContainerRecipientException;
import com.sap.engine.interfaces.textcontainer.recipient.TextContainerRecipientListener;
import com.sap.engine.services.textcontainer.TextContainerException;
import com.sap.engine.services.textcontainer.TextContainerService;
import com.sap.engine.services.textcontainer.api.TextContainerRecipientChangedEventImpl;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class TextContainerMessageSender
{

    public static void sendUpdateMessage(byte changed) throws TextContainerException
    {
		String METHOD = "sendUpdateMessage";

		// First send message to Text Container instances on other servers of the cluster:
    	MessageContext messageContext = TextContainerService.getServiceContext().getClusterContext().getMessageContext();

    	try
    	{
    		messageContext.send(-1, ClusterElement.SERVER, 0, new byte[]{(changed)}, 0, 1);
    	}
    	catch (ClusterException e)
    	{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, METHOD, e);
    		throw new TextContainerException("ClusterException", e);
    	}

    	// Now send notification to registered recipients:
		// Are there registered recipient listeners to be notified about changes?
		HashMap<String, ArrayList<TextContainerRecipientListener>> recipientListeners = TextContainerService.getRecipientListeners();
		if (!recipientListeners.isEmpty())
		{
			Iterator<Entry<String, ArrayList<TextContainerRecipientListener>>> iterRecipientListeners = recipientListeners.entrySet().iterator();

			while (iterRecipientListeners.hasNext())
			{
				Entry<String, ArrayList<TextContainerRecipientListener>> entryOfRecipientListeners = iterRecipientListeners.next();

				ArrayList<TextContainerRecipientListener> list = entryOfRecipientListeners.getValue();

				if (list != null)
				{
					for (int i = 0; i < list.size(); i++)
					{
						TextContainerRecipientListener recipientListener = list.get(i);

						if (recipientListener != null)
						{
							int changedForEvent = 0;

							if ((changed & TextContainerMessage.TXV_CHANGED_INDUSTRIES) != 0)
								changedForEvent += TextContainerRecipientChangedEvent.TXV_CHANGED_INDUSTRIES;

							if ((changed & TextContainerMessage.TXV_CHANGED_REGIONS) != 0)
								changedForEvent += TextContainerRecipientChangedEvent.TXV_CHANGED_REGIONS;

							if ((changed & TextContainerMessage.TXV_CHANGED_EXTENSIONS) != 0)
								changedForEvent += TextContainerRecipientChangedEvent.TXV_CHANGED_EXTENSIONS;

							if ((changed & TextContainerMessage.TXV_CHANGED_LANGUAGES) != 0)
								changedForEvent += TextContainerRecipientChangedEvent.TXV_CHANGED_LANGUAGES;

							if ((changed & TextContainerMessage.TXV_CHANGED_LOCALES) != 0)
								changedForEvent += TextContainerRecipientChangedEvent.TXV_CHANGED_LOCALES;

							if ((changed & TextContainerMessage.TXV_CHANGED_SYSTEM_CONTEXT) != 0)
								changedForEvent += TextContainerRecipientChangedEvent.TXV_CHANGED_SYSTEM_CONTEXT;

							if ((changed & TextContainerMessage.TXV_CHANGED_CONTEXT_IDS) != 0)
								changedForEvent += TextContainerRecipientChangedEvent.TXV_CHANGED_CONTEXT_IDS;

							TextContainerRecipientChangedEventImpl event = new TextContainerRecipientChangedEventImpl(changedForEvent);

							try
							{
								recipientListener.receive(event);
							}
							catch (TextContainerRecipientException e)
							{
								CATEGORY.logThrowableT(Severity.ERROR, LOCATION, METHOD, e);
								throw new TextContainerException("TextContainerRecipientException", e);
							}
							catch (Exception e)
							{
								CATEGORY.logThrowableT(Severity.ERROR, LOCATION, METHOD, e);
								throw new TextContainerException("Exception", e);
							}
						}
					}
				}
			}
		}
    }

    // Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.message.TextContainerMessageSender");
	private static final Category CATEGORY = Category.SYS_SERVER;

}
