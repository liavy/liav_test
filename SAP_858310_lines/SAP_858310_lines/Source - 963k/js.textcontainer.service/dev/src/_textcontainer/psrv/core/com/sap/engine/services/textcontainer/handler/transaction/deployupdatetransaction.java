package com.sap.engine.services.textcontainer.handler.transaction;

import java.io.File;
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
import com.sap.engine.services.textcontainer.TextContainerException;
import com.sap.engine.services.textcontainer.TextContainerService;
import com.sap.engine.services.textcontainer.api.TextContainerRecipientDeploymentEventImpl;
import com.sap.engine.services.textcontainer.context.AllowedLanguages;
import com.sap.engine.services.textcontainer.dbaccess.ComponentData;
import com.sap.engine.services.textcontainer.dbaccess.Ctx;
import com.sap.engine.services.textcontainer.dbaccess.DbFunctions;
import com.sap.engine.services.textcontainer.deployment.TextComponentProcessorDeploymentDB;
import com.sap.engine.services.textcontainer.module.TextComponent;
import com.sap.engine.services.textcontainer.module.TextComponentException;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class DeployUpdateTransaction extends Transaction
{

	protected Ctx ctx;

	public ContentOperationInfo begin(ContentDeployItem cItem) throws ContentHandlerException, ContentHandlerWarning
	{
		String METHOD = "deploy";

		String componentName = cItem.getComponentName();

		LOCATION.infoT(METHOD, "Begin of deployment of component {0}", new Object[] { componentName });

		File archiveFile = getContentFile(cItem.getArchiveFiles());				

		try
		{
			TextComponentProcessorDeploymentDB oDeployment = null;

			try
			{
				Connection conn = TextContainerService.getDataSource().getConnection();
				conn.setAutoCommit(false);

				ctx = new Ctx(conn);

				oDeployment = new TextComponentProcessorDeploymentDB(ctx, componentName);

				oDeployment.beforeDeploy();

				LOCATION.infoT(METHOD, "Deploying file {0} of component {1}", new Object[] { archiveFile, componentName });

				TextComponent oTextComponent = new TextComponent(archiveFile);

				oTextComponent.load(oDeployment);

				oDeployment.afterDeploy();

				oDeployment.resolve();
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

			try 
			{
				String[] deployedLanguages = oDeployment.getDeployedLanguages();

				for (int i = 0; i < deployedLanguages.length; i++)
				{
					String deployedLanguage = deployedLanguages[i];

					if ((deployedLanguage != null) && (deployedLanguage.length() > 0) &&
						(!AllowedLanguages.getInstance().contains(deployedLanguage)))
						LOCATION.warningT(METHOD, "Texts in language {0} have been deployed although the language is not known!", new Object[] { deployedLanguage });
				}
			}
			catch (TextContainerException e)
			{
				doRollback();

				CATEGORY.logThrowableT(Severity.ERROR, LOCATION, METHOD, e);
				throw new ContentHandlerException("TextContainerException", e);
			}
			finally
			{
				ComponentData.clear();
			}

/* Removed because TextContainerRecipient notification has been switched off! 
			// Are there registered recipients to be notified about deployed texts?
			HashMap<String, TextContainerRecipient> recipients = TextContainerService.getRecipients();
			if (!recipients.isEmpty())
			{
				HashMap<String, HashMap<String, String[]>> deployedBundles = oDeployment.getDeployedBundles();

				Iterator<Entry<String, TextContainerRecipient>> iterRecipients = recipients.entrySet().iterator();

				while (iterRecipients.hasNext())
				{
					Entry<String, TextContainerRecipient> entryOfRecipient = iterRecipients.next();

					HashMap<String, String[]> deployedBundlesForRecipient = deployedBundles.get(entryOfRecipient.getKey());

					if (deployedBundlesForRecipient != null)
					{
						TextContainerRecipient recipient = entryOfRecipient.getValue();

						if (recipient != null)
						{
							try
							{
								recipient.deployedBundles(deployedBundlesForRecipient);
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
*/

			// Are there registered recipient listeners to be notified about deployed texts?
			HashMap<String, ArrayList<TextContainerRecipientListener>> recipientListeners = TextContainerService.getRecipientListeners();
			if (!recipientListeners.isEmpty())
			{
				HashMap<String, HashMap<String, String[]>> deployedBundles2 = oDeployment.getDeployedBundles2();

				Iterator<Entry<String, ArrayList<TextContainerRecipientListener>>> iterRecipientListeners = recipientListeners.entrySet().iterator();

				while (iterRecipientListeners.hasNext())
				{
					Entry<String, ArrayList<TextContainerRecipientListener>> entryOfRecipientListeners = iterRecipientListeners.next();

					HashMap<String, String[]> deployedBundles2ForRecipientListener = deployedBundles2.get(entryOfRecipientListeners.getKey());

					if (deployedBundles2ForRecipientListener != null)
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
										new TextContainerRecipientDeploymentEventImpl(TextContainerRecipientDeploymentEvent.TCR_DEPLOYMENT,
												componentName.replace("~", "/"),
												deployedBundles2ForRecipientListener);

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

			LOCATION.infoT(METHOD, "Finished deployment of component {0}", new Object[] { componentName });
		}
		finally
		{
			removeWorkDir();
		}

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
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.handler.transaction.DeployUpdateTransaction");
	private static final Category CATEGORY = Category.SYS_SERVER;

}
