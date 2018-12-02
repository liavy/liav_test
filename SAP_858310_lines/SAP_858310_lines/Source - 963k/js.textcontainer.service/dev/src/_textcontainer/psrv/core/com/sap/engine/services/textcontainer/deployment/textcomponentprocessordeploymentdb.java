/*
 * Created on Jan 24, 2006
 */
package com.sap.engine.services.textcontainer.deployment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import com.sap.engine.services.textcontainer.TextContainerConfiguration;
import com.sap.engine.services.textcontainer.TextContainerException;
import com.sap.engine.services.textcontainer.cache.SAPResourceBundleCache;
import com.sap.engine.services.textcontainer.context.Context;
import com.sap.engine.services.textcontainer.context.ContextResolution;
import com.sap.engine.services.textcontainer.context.SystemContext;
import com.sap.engine.services.textcontainer.dbaccess.BundleData;
import com.sap.engine.services.textcontainer.dbaccess.ComponentData;
import com.sap.engine.services.textcontainer.dbaccess.ContainerData;
import com.sap.engine.services.textcontainer.dbaccess.ContainerIteratorGrouped;
import com.sap.engine.services.textcontainer.dbaccess.ContextData;
import com.sap.engine.services.textcontainer.dbaccess.Ctx;
import com.sap.engine.services.textcontainer.dbaccess.DirtyData;
import com.sap.engine.services.textcontainer.dbaccess.LoadData;
import com.sap.engine.services.textcontainer.module.TextComponentContext;
import com.sap.engine.services.textcontainer.module.TextComponentException;
import com.sap.engine.services.textcontainer.module.TextComponentS2XText;
import com.sap.engine.services.textcontainer.security.TextContainerMessageDigest;

/**
 * @author d029702
 */
public class TextComponentProcessorDeploymentDB extends TextComponentProcessorDeployment
{

	public TextComponentProcessorDeploymentDB(Ctx ctx, String component) throws TextComponentException
	{
		super(component);
		this.ctx = ctx;
		textContainer = new ArrayList<ContainerData>();
		bundleMap = new HashMap<String, HashSet<BundleData>>();
		componentHash = getComponentHash(this.component);
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.textcontainer.TextComponentProcessor#setContext(com.sap.engine.textcontainer.TextComponentContext)
	 */
	public void setContext(TextComponentContext oContext) throws TextComponentException
	{
		super.setContext(oContext);
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.services.textcontainer.deployment.TextComponentProcessorDeployment#beforeDeploy()
	 */
	public void beforeDeploy() throws TextComponentException
	{
		super.beforeDeploy();

		try
		{
			ContainerData.deleteWithDeployComponent(ctx, componentHash);
		}
		catch (SQLException e)
		{
	    	throw new TextComponentException("Failed with SQLException:\n"+e.getMessage(), e);
		}
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.services.textcontainer.deployment.TextComponentProcessorDeployment#afterDeploy()
	 */
	public void afterDeploy() throws TextComponentException
	{
		try
		{
			ContainerData.insert(ctx, (ContainerData[]) textContainer.toArray(new ContainerData[0]));

			textContainer = null;
			bundleMap = null;
		}
		catch (SQLException e)
		{
	    	throw new TextComponentException(
		    		"Failed with SQLException:\n"+e.getMessage(), e);
		}
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.textcontainer.TextComponentProcessorDeployment#undeploy()
	 */
	public void undeploy() throws TextComponentException
	{
		try
		{
			componentHash = TextContainerMessageDigest.getDigestOptimizeAscii(component);

			if (componentHash != null)
			{
				// Get all deployed bundles of the component:
				ContainerIteratorGrouped iterTextContainer = ContainerData.selectGroupedByOriginalCompHashAndBundleHash(ctx, componentHash);

				// Delete the texts of this (deploy-) component:
				ContainerData.deleteWithDeployComponent(ctx, componentHash);

				byte[] oldOriginalCompHash = null;
				byte[] oldBundleHash = null;
				byte[] newOriginalCompHash;
				byte[] newBundleHash;

				boolean groupChange;

				ArrayList<byte[]> componentHashs = new ArrayList<byte[]>();

				String component = "";
				String bundle = "";
				String recipient = "";

				while (iterTextContainer.next())
				{
					groupChange = false;

					newOriginalCompHash = iterTextContainer.originalCompHash();
					newBundleHash = iterTextContainer.bundleHash();

					if (!Arrays.equals(newOriginalCompHash, oldOriginalCompHash))
					{
						oldOriginalCompHash = newOriginalCompHash;
						groupChange = true;
						componentHashs.add(newOriginalCompHash);
					}

					if (!Arrays.equals(newBundleHash, oldBundleHash))
					{
						oldBundleHash = newBundleHash;
						groupChange = true;
					}

					if (groupChange)
					{
						// Only mark content as dirty if text load is used for caching:
						if (TextContainerConfiguration.useTextLoadForCachingIsActive())
							DirtyData.modify(ctx, newOriginalCompHash, newBundleHash);

						component = ComponentData.select(ctx, newOriginalCompHash).getComponent();

						BundleData bundleData = BundleData.select(ctx, newBundleHash, newOriginalCompHash);
						bundle = bundleData.getBundle();
						recipient = bundleData.getRecipient();

						// Delete from the cache:
			 			SAPResourceBundleCache.remove(component, bundle);

						// Prepare map for recipient notification:
						HashMap<String, HashSet<String>> map = undeployedBundles2.get(recipient);

						if (map == null)
						{
							map = new HashMap<String, HashSet<String>>();
							undeployedBundles2.put(recipient, map);
						}

						HashSet<String> set = map.get(component);

						if (set == null)
						{
							set = new HashSet<String>();
							map.put(component, set);
						}

						if (!set.contains(bundle))
							set.add(bundle);
					}
				}

				iterTextContainer.close();

				// componentHashs now contains all OriginalComponents
				// where the texts of this component belong to.

				byte[] groupedOriginalComponentHash;

				for (int i = 0; i < componentHashs.size(); i++)
				{
					groupedOriginalComponentHash = componentHashs.get(i);

					// If no more texts of the originalComponent exist...
					if (!ContainerData.existsWithOriginalComponent(ctx, groupedOriginalComponentHash))
					{
						// ...delete texts from the text load:
						// Only use text load if corresponding configuration is active (text load is used for caching):
						if (TextContainerConfiguration.useTextLoadForCachingIsActive())
							LoadData.delete(ctx, groupedOriginalComponentHash);
						// ...and from the cache:
			 			ComponentData componentData = ComponentData.select(ctx, groupedOriginalComponentHash);
			 			SAPResourceBundleCache.remove(componentData.getComponent());
						// ...and nothing to resolve:
						// Only delete dirty content if text load is used for caching:
						if (TextContainerConfiguration.useTextLoadForCachingIsActive())
							DirtyData.delete(ctx, groupedOriginalComponentHash);
						// ...and no bundles exist anymore:
						BundleData.delete(ctx, groupedOriginalComponentHash);
						// ...and the component is not used anymore:
						ComponentData.delete(ctx, groupedOriginalComponentHash);
					}
				}

				// If no more texts of the component as the originalComponent (!) exist...
				if (!ContainerData.existsWithOriginalComponent(ctx, componentHash))
				{
					// ...no bundles exist anymore:
					BundleData.delete(ctx, componentHash);
					// ...and the component is not used anymore:
					ComponentData.delete(ctx, componentHash);
				}
			}
		}
		catch (Exception e)
		{
	    	throw new TextComponentException("Failed with Exception:\n"+e.getMessage(), e);
		}
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.textcontainer.TextComponentProcessorDeployment#resolve()
	 */
	public void resolve() throws TextComponentException
	{
		try
		{
    		ContextResolution.getInstance().resolveDirtyTexts(ctx, SystemContext.getInstance().get(), getDeployedLanguages());
		}
		catch (TextContainerException e)
		{
	    	throw new TextComponentException(
		    		"Failed with TextContainerException:\n"+e.getMessage(), e);
		}
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.textcontainer.TextComponentProcessorDeployment#unresolve()
	 */
	public void unresolve() throws TextComponentException
	{
		try
		{
    		ContextResolution.getInstance().resolveDirtyTexts(ctx, SystemContext.getInstance().get(), null);
		}
		catch (TextContainerException e)
		{
	    	throw new TextComponentException(
		    		"Failed with TextContainerException:\n"+e.getMessage(), e);
		}
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.textcontainer.TextComponentProcessorDeployment#getComponentHash(java.lang.String, boolean)
	 */
	protected byte[] getComponentHash(String component) throws TextComponentException
	{
		byte[] componentHash = null;
		if (ctx != null)
		{
			try
			{
				componentHash = TextContainerMessageDigest.getDigestOptimizeAscii(component);
				ComponentData.modify(ctx, componentHash, component);
			}
			catch (Exception e)
			{
				throw new TextComponentException("Failed with Exception:\n"+e.getMessage(), e);
			}
		}
		return componentHash;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.textcontainer.TextComponentProcessorDeployment#getBundleHash(byte[], byte[], java.lang.String, java.lang.String)
	 */
	protected void getBundleHash(byte[] bundleHash, byte[] componentHash, String bundle, String recipient) throws TextComponentException
	{
		if (ctx != null)
		{
			try
			{
				BundleData bundleData = new BundleData(bundleHash, componentHash, bundle, recipient);

				HashSet<BundleData> bundleSet = bundleMap.get(Arrays.toString(componentHash));

				if (bundleSet == null)
				{
					bundleSet = BundleData.select(ctx, componentHash);

					bundleMap.put(Arrays.toString(componentHash), bundleSet);
				}

				if (!bundleSet.contains(bundleData))
				{
					BundleData.insert(ctx, bundleHash, componentHash, bundle, recipient);

					bundleSet.add(bundleData);
				}
			}
			catch (Exception e)
			{
		    	throw new TextComponentException("Failed with Exception:\n"+e.getMessage(), e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.textcontainer.TextComponentProcessorDeployment#getContextId(com.sap.engine.textcontainer.context.Context, boolean)
	 */
	protected int getContextId(Context context, boolean createNewId) throws TextComponentException
	{
		int contextId = 0;
		try
		{
			ContextData dbContext = new ContextData(context.getLocale(), context.getIndustry(), context.getRegion(), context.getExtension());

			contextId = ContextData.contextToId(ctx, dbContext);
			if ((contextId == 0) && (createNewId))
				contextId = ContextData.createNewContextId(ctx, dbContext);
		}
		catch (SQLException e)
		{
	    	throw new TextComponentException(
		    		"Failed with SQLException:\n"+e.getMessage(), e);
		}
		return contextId;
	}

	protected int insertTextContainer(byte[] deployComponentHash, int sequenceNumber, byte[] originalComponentHash,
			byte[] bundleHash, int contextId, TextComponentS2XText[] text, String originalLocale) throws TextComponentException
	{
		for (int i = 0; i < text.length; i++)
		{
			textContainer.add(new ContainerData(deployComponentHash, ++sequenceNumber, originalComponentHash,
					bundleHash, contextId, text[i].m_sElKey, text[i].m_sText, originalLocale));
		}
		return sequenceNumber;
	}

	protected void modifyDirty(byte[] originalComponentHash, byte[] bundleHash) throws TextComponentException
	{
		try
		{
			// Only mark dirty content if text load is used for caching:
			if (TextContainerConfiguration.useTextLoadForCachingIsActive())
				DirtyData.modify(ctx, originalComponentHash, bundleHash);
		}
		catch (SQLException e)
		{
	    	throw new TextComponentException(
		    		"Failed with SQLException:\n"+e.getMessage(), e);
		}
	}

	protected Ctx ctx;

	ArrayList<ContainerData> textContainer;
	HashMap<String, HashSet<BundleData>> bundleMap;

}
