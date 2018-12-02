package com.sap.engine.services.textcontainer.handler.transaction;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.sap.engine.interfaces.textcontainer.recipient.TextContainerRecipientDeploymentEvent;
import com.sap.engine.interfaces.textcontainer.recipient.TextContainerRecipientException;
import com.sap.engine.interfaces.textcontainer.recipient.TextContainerRecipientListener;
import com.sap.engine.services.content.handler.api.ContentDeployItem;
import com.sap.engine.services.content.handler.api.ContentOperationInfo;
import com.sap.engine.services.content.handler.api.exception.ContentHandlerException;
import com.sap.engine.services.content.handler.api.exception.ContentHandlerWarning;
import com.sap.engine.services.textcontainer.TextContainerService;
import com.sap.engine.services.textcontainer.api.TextContainerRecipientDeploymentEventImpl;
import com.sap.engine.services.textcontainer.dbaccess.ComponentData;
import com.sap.engine.services.textcontainer.dbaccess.Ctx;
import com.sap.engine.services.textcontainer.dbaccess.DbFunctions;
import com.sap.engine.services.textcontainer.deployment.TextComponentProcessorDeploymentDB;
import com.sap.engine.services.textcontainer.module.TextComponentException;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class RemoveTransaction extends Transaction
{

	protected Ctx ctx;

	public ContentOperationInfo begin(ContentDeployItem cItem) throws ContentHandlerException, ContentHandlerWarning
	{
		String METHOD = "remove";

		String componentName = cItem.getComponentName();

		LOCATION.infoT(METHOD, "Begin of undeployment of component {0}", new Object[] { componentName });

		TextComponentProcessorDeploymentDB oDeployment = null;

		try
		{
			Connection conn = TextContainerService.getDataSource().getConnection();
			conn.setAutoCommit(false);

			ctx = new Ctx(conn);

			oDeployment = new TextComponentProcessorDeploymentDB(ctx, componentName);

			LOCATION.infoT(METHOD, "Undeploying component {0}", new Object[] { componentName });

			oDeployment.undeploy();

			oDeployment.unresolve();
		}
		catch (java.sql.SQLException e)
		{
			doRollback();

			CATEGORY.logThrowableT(Severity.ERROR, LOCATION, METHOD, e);
			throw new ContentHandlerException("SQLException", e);
		}
		catch (TextComponentException e)
		{
			doRollback();

			CATEGORY.logThrowableT(Severity.ERROR, LOCATION, METHOD, e);
			throw new ContentHandlerException("TextComponentException", e);
		}
		catch (RuntimeException e)
		{
			doRollback();

			CATEGORY.logThrowableT(Severity.ERROR, LOCATION, METHOD, e);
			throw new ContentHandlerException("RuntimeException", e);
		}
		finally
		{
			ComponentData.clear();
		}

		// Are there registered recipient listeners to be notified about undeployed texts?
		HashMap<String, ArrayList<TextContainerRecipientListener>> recipientListeners = TextContainerService.getRecipientListeners();
		if (!recipientListeners.isEmpty())
		{
			HashMap<String, HashMap<String, String[]>> undeployedBundles2 = oDeployment.getUndeployedBundles2();

			Iterator<Entry<String, ArrayList<TextContainerRecipientListener>>> iterRecipientListeners = recipientListeners.entrySet().iterator();

			while (iterRecipientListeners.hasNext())
			{
				Entry<String, ArrayList<TextContainerRecipientListener>> entryOfRecipientListeners = iterRecipientListeners.next();

				HashMap<String, String[]> undeployedBundles2ForRecipientListener = undeployedBundles2.get(entryOfRecipientListeners.getKey());

				if (undeployedBundles2ForRecipientListener != null)
				{
					ArrayList<TextContainerRecipientListener> list = entryOfRecipientListeners.getValue();

					if (list != null)
					{
						for (int i = 0; i < list.size(); i++)
						{
							TextContainerRecipientListener recipientListener = list.get(i);

							if (recipientListener != null)
							{
								TextContainerRecipientDeploymentEventImpl event =
									new TextContainerRecipientDeploymentEventImpl(TextContainerRecipientDeploymentEvent.TCR_UNDEPLOYMENT,
											componentName.replace("~", "/"),
											undeployedBundles2ForRecipientListener);

								try
								{
									recipientListener.receive(event);
								}
								catch (TextContainerRecipientException e)
								{
									doRollback();

									CATEGORY.logThrowableT(Severity.ERROR, LOCATION, METHOD, e);
									throw new ContentHandlerException("TextContainerRecipientException", e);
								}
								catch (Exception e)
								{
									doRollback();

									CATEGORY.logThrowableT(Severity.ERROR, LOCATION, METHOD, e);
									throw new ContentHandlerException("Exception", e);
								}
							}
						}
					}
				}
			}
		}

		doCommit();

		LOCATION.infoT(METHOD, "Finished undeployment of component {0}", new Object[] { componentName });

		return new ContentOperationInfo(new String[]{componentName});
	}

	public void commit() throws ContentHandlerWarning
	{
/*
		String METHOD = "commit";

		try
		{
			DbFunctions.commit(ctx);
		}
		catch (java.sql.SQLException e)
		{
			CATEGORY.logThrowableT(Severity.ERROR, LOCATION, METHOD, e);
			throw new ContentHandlerWarning("SQLException", e);
		}
		finally
		{
			try
			{
				if (ctx != null)
					ctx.close();
			}
			catch (java.sql.SQLException e)
			{
				CATEGORY.logThrowableT(Severity.ERROR, LOCATION, METHOD, e);
				throw new ContentHandlerWarning("SQLException", e);
			}
			finally
			{
				ctx = null;
			}
		}
*/
	}

	public void rollback() throws ContentHandlerWarning
	{
/*
		String METHOD = "rollback";

		try
		{
			DbFunctions.rollback(ctx);
		}
		catch (java.sql.SQLException e)
		{
			CATEGORY.logThrowableT(Severity.ERROR, LOCATION, METHOD, e);
			throw new ContentHandlerWarning("SQLException", e);
		}
		finally
		{
			try
			{
				if (ctx != null)
					ctx.close();
			}
			catch (java.sql.SQLException e)
			{
				CATEGORY.logThrowableT(Severity.ERROR, LOCATION, METHOD, e);
				throw new ContentHandlerWarning("SQLException", e);
			}
			finally
			{
				ctx = null;
			}
		}
*/
	}

	private void doCommit() throws ContentHandlerWarning
	{
		String METHOD = "doCommit";

		try
		{
			try
			{
				DbFunctions.commit(ctx);
			}
			finally
			{
				if (ctx != null)
					ctx.close();
			}
		}
		catch (java.sql.SQLException e)
		{
			CATEGORY.logThrowableT(Severity.ERROR, LOCATION, METHOD, e);
			throw new ContentHandlerWarning("SQLException", e);
		}
		finally
		{
			ctx = null;
		}
	}

	private void doRollback() throws ContentHandlerWarning
	{
		String METHOD = "doRollback";

		try
		{
			try
			{
				DbFunctions.rollback(ctx);
			}
			finally
			{
				if (ctx != null)
					ctx.close();
			}
		}
		catch (java.sql.SQLException e)
		{
			CATEGORY.logThrowableT(Severity.ERROR, LOCATION, METHOD, e);
			throw new ContentHandlerWarning("SQLException", e);
		}
		finally
		{
			ctx = null;
		}
	}

	// Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.handler.transaction.RemoveTransaction");
	private static final Category CATEGORY = Category.SYS_SERVER;

}
