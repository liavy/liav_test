package com.sap.engine.services.textcontainer.context;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import com.sap.engine.interfaces.textcontainer.context.TextContainerContext;
import com.sap.engine.interfaces.textcontainer.context.TextContainerRules;
import com.sap.engine.interfaces.textcontainer.context.TextContainerVariant;
import com.sap.engine.interfaces.textcontainer.recipient.TextContainerRecipientException;
import com.sap.engine.interfaces.textcontainer.recipient.TextContainerRecipientListener;
import com.sap.engine.services.textcontainer.TextContainerConfiguration;
import com.sap.engine.services.textcontainer.TextContainerException;
import com.sap.engine.services.textcontainer.TextContainerService;
import com.sap.engine.services.textcontainer.api.TextContainerRecipientTextsUpdatedEventImpl;
import com.sap.engine.services.textcontainer.cache.SAPResourceBundleCache;
import com.sap.engine.services.textcontainer.dbaccess.BundleData;
import com.sap.engine.services.textcontainer.dbaccess.ComponentData;
import com.sap.engine.services.textcontainer.dbaccess.ContainerData;
import com.sap.engine.services.textcontainer.dbaccess.ContainerIteratorGrouped;
import com.sap.engine.services.textcontainer.dbaccess.ContainerIteratorLocales;
import com.sap.engine.services.textcontainer.dbaccess.ContextData;
import com.sap.engine.services.textcontainer.dbaccess.Ctx;
import com.sap.engine.services.textcontainer.dbaccess.DbFunctions;
import com.sap.engine.services.textcontainer.dbaccess.DirtyData;
import com.sap.engine.services.textcontainer.dbaccess.LoadData;
import com.sap.sql.NoDataException;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * This class is the entry point to the context resolution. Part of the data
 * (e.g. context chains) that are generated during context resolution is stored
 * in singletons for reuse (performance enhancement).
 * 
 * @author d041138: Aug 25, 2005 (adopted by d029702: Mar 28, 2006)
 */
public class ContextResolution
{
    private static ContextResolution instance;

    private ContextResolution()
    {
    }

    /**
     * @return The singleton instance of the context resolution.
     */
    public static ContextResolution getInstance()
    {
        if (instance == null)
            instance = new ContextResolution();

    	return instance;
    }

    /**
     * For the given context and rules parameters, determine the matching
     * variant in the given variant list. The logical resource to which all the
     * variants belong is implicitly given by this list.
     * 
     * 
     * @param variants
     *            List of candidates.
     * @param context
     *            Context parameters with human readable strings.
     * @param rules
     *            Rules which control the context resolution.
     * @return One variant or null.
     * @throws TextContainerException
     */
    public TextContainerVariant resolveLogicalResource(TextContainerVariant[] variants, TextContainerContext context, TextContainerRules rules) throws TextContainerException
    {
		if ((variants == null) || (context == null) || (rules == null))
		{
			String message = "Parameter null: ";
			String separator = "";
			if (variants == null)
			{
				message += "variants";
				separator = ", ";
			}
			if (context == null)
			{
				message += separator;
				message += "context";
				separator = ", ";
			}
			if (rules == null)
			{
				message += separator;
				message += "rules";
			}
			NullPointerException e = new NullPointerException(message);
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "resolveLogicalResource", "Parameters: {0}, {1}, {2}", new Object[] { variants, context, rules }, e);
			throw e;
		}
        
        // Get the task
        String task = rules.getParameterValue(TextContainerRules.TASK);
        if (task == null)
		{
			NullPointerException e = new NullPointerException("Parameter null: task");
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "resolveLogicalResource", "Parameters: {0}, {1}, {2}", new Object[] { variants, context, rules }, e);
			throw e;
		}

        // Compute the context chain for the attributes of the logical resource,
        // the attributes in the start context and the parameters in the rules
        ContextChain cchain = computeContextChainWithTask(context, task, variants[0].getMasterLocale());

//		String contextLocale = context.getLocale();

		// Start from the beginning of the computed context chain, except for task CR_VERT:
		// Here we begin at the second context in the context chain (K - 1)!
        int startIndex = 0;

        if (task.equals(TextContainerRules.CR_VERT))
   	    	startIndex = 1;

        boolean varIndexIsSpecial = false;

        // Find the first entry in the context chain that matches an entry
        // in the variant list. For performance reasons we loop over the
        // list of variants and check for each variant whether there is an
        // entry in the context chain. This lookup is fast since a hashtable
        // is used within the context chain.
        int minContextIndex = -1; // Index of a matching context
        int varIndex = -1; // Index of a matching variant
        for (int i = 0; i < variants.length; i++)
        {
            // Create a context object with the relevant attribute values
            if (variants[i] != null)
            {
	            Context varContext = new Context(variants[i]);

	            int index = cchain.getIndex(varContext);

	            // Check new index in context chain:
	            // If current varIndex is special (see below) then the
	            // same index has higher priority because this variant
	            // is a given one and not a constructed one like below!
	            if ((index >= startIndex) &&
	            	((minContextIndex == -1) ||
	            	 ((!varIndexIsSpecial) && (index < minContextIndex)) ||
	            	 ((varIndexIsSpecial) && (index <= minContextIndex))))
	            {
	            	// Don't mark this varIndex as special:
	            	varIndexIsSpecial = false;
	                minContextIndex = index;
	                varIndex = i;
	            }

	            if (varContext.getLocale().length() == 0)
	            {
	            	String masterLocale = variants[i].getMasterLocale();

	            	if ((masterLocale != null) &&
	            		(masterLocale.length() > 0))
	            	{
            			// In case we just tested the empty locale (""),
            			// we now test the master locale if it is not "":
            			varContext.setLocale(masterLocale);

            			index = cchain.getIndex(varContext);

            			if ((index >= startIndex) && ((minContextIndex == -1) || (index < minContextIndex)))
            			{
            				// Mark this varIndex as special:
            				varIndexIsSpecial = true;
            				minContextIndex = index;
            				varIndex = i;
            			}
	            	}
	            }
            }
        }

        if (varIndex != -1) // A matching variant was found
            return variants[varIndex];
        else
            return null;
    }

    /**
     * <em>Local context resolution</em>
     * <p>
     * For the given logical resource, context and rules, return the context
     * chain. If the chain is requested for the first time, it is computed from
     * scratch and stored persistently. Later requests of the same chain return
     * it from the persistence layer.
     * <p>
     * The values of the attributes COMPONENT and RELEASE in the given context
     * are expected as keys. The values in the returned contexts are again keys.
     * <p>
     * Currently the following rules parameters are supported:
     * 
     * <table border="1" cellpadding="5">
     * <tr>
     * <td>TASK</td>
     * <td>CR_EDIT</td>
     * <td>The context resolution is called from the authoring perspective for
     * a content object.</td>
     * </tr>
     * <tr>
     * <td></td>
     * <td>CR_VIEW</td>
     * <td>The context resolution is called from the reader perspective
     * (enduser) for a content object.</td>
     * </tr>
     * <tr>
     * <td></td>
     * <td>CR_VERT</td>
     * <td>The context resolution is called from text verticalization.</td>
     * </tr>
     * <tr>
     * <td></td>
     * <td>CR_VIEW_EDIT_COLL</td>
     * <td>The context resolution is called for a collection object.</td>
     * </tr>
     * <tr>
     * <td></td>
     * <td>CR_VIEW_UI</td>
     * <td>The context resolution is called from text verticalization.</td>
     * </tr>
     * </table>
     * <p>
     * Here is a <a href="doc-files/StoringContextChains.html">list </a> of
     * attributes and parameters that are used to index context chains.
     * <p>
     * The contexts in the context chain current hold the parameters EXTENSION,
     * LOCALE, INDUSTRY, REGION. These
     * very same attributes must be checked in the method
     * {@link resolveLogicalResource}.
     * 
     * @param context
     *            The context
     * @param rules
     *            The set of rules parameters
     * @return List of contexts in the order in which they appear in the context
     *         chain.
     * @throws TextContainerException
     */
    public ContextChain computeContextChainWithTask(TextContainerContext context, String task, String masterLocale) throws TextContainerException
    {
        if ((context == null) || (task == null))
		{
			String message = "Parameter null: ";
			String separator = "";
			if (context == null)
			{
				message += "context";
				separator = ", ";
			}
			if (task == null)
			{
				message += separator;
				message += "task";
			}
			NullPointerException e = new NullPointerException(message);
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "computeContextChainWithTask", "Parameters: {0}, {1}, {2}", new Object[] { context, task, masterLocale }, e);
			throw e;
		}

        // Check whether there is already a context chain for the given
        // parameters
        ContextChain cchain = ContextChains.getInstance().getContextChain(context, task, masterLocale);
        if (cchain != null)
            return cchain;

		ArrayList<TextContainerContext> contexts = new ArrayList<TextContainerContext>();

        // For the given context and rules, no context chain was
        // previously computed
        String contextLocale = context.getLocale();
        String contextIndustry = context.getIndustry();
        String contextRegion = context.getRegion();
        String contextExtension = context.getExtension();

        if ((contextLocale == null) || (contextIndustry == null) || (contextRegion == null) || (contextExtension == null))
		{
			String message = "Parameter null: ";
			String separator = "";
			if (contextLocale == null)
			{
				message += "contextLocale";
				separator = ", ";
			}
			if (contextIndustry == null)
			{
				message += separator;
				message += "contextIndustry";
				separator = ", ";
			}
			if (contextRegion == null)
			{
				message += separator;
				message += "contextRegion";
				separator = ", ";
			}
			if (contextExtension == null)
			{
				message += separator;
				message += "contextExtension";
			}
			NullPointerException e = new NullPointerException(message);
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "computeContextChainWithTask", "Parameters: {0}, {1}, {2}", new Object[] { context, task, masterLocale }, e);
			throw e;
		}

        if (!AllowedIndustries.getInstance().industryExists(contextIndustry))
        {
        	TextContainerException e = new TextContainerException("Industry does noch exist: " + contextIndustry);
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "computeContextChainWithTask", "Parameters: {0}, {1}, {2}", new Object[] { context, task, masterLocale }, e);
			throw e;
        }
        if (!AllowedRegions.getInstance().regionExists(contextRegion))
        {
        	TextContainerException e = new TextContainerException("Region does noch exist: " + contextRegion);
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "computeContextChainWithTask", "Parameters: {0}, {1}, {2}", new Object[] { context, task, masterLocale }, e);
			throw e;
        }
        if (!AllowedExtensions.getInstance().extensionExists(contextExtension))
        {
        	TextContainerException e = new TextContainerException("Extension does noch exist: " + contextExtension);
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "computeContextChainWithTask", "Parameters: {0}, {1}, {2}", new Object[] { context, task, masterLocale }, e);
			throw e;
        }

        // Get locale chain
        String[] lchain = TransferFunctions.getInstance().getLocaleChain(task, contextLocale, masterLocale);
        for (int i = 0; i < lchain.length; i++)
        {
        	// Get start extension
        	String extension = contextExtension;
        	while (extension != null)
        	{
                // Get start industry
                String industry = contextIndustry;
                while (industry != null)
                {
                    // Get start region
                    String region = contextRegion;
                    while (region != null)
                    {
                        Context ct = new Context(lchain[i], industry, region, extension);
                        contexts.add(ct);
                        region = TransferFunctions.getInstance().getPreviousRegion(region);
                    }
                    industry = TransferFunctions.getInstance().getPreviousIndustry(industry);
                }
                extension = TransferFunctions.getInstance().getPreviousExtension(extension);
            }
        }

        ContextChainHandle handle = new ContextChainHandle(context, task, masterLocale);
        cchain = new ContextChain(handle, contexts);

        // Store the new context chain for subsequent calls.
        ContextChains.getInstance().addContextChain(cchain);

		return cchain;
    }

    public ContextChain computeContextChain(TextContainerContext context, TextContainerRules rules, String masterLocale) throws TextContainerException
    {
        if (rules == null)
		{
			NullPointerException e = new NullPointerException("Parameter null: rules");
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "computeContextChain", "Parameters: {0}, {1}, {2}", new Object[] { context, rules, masterLocale }, e);
			throw e;
		}

        String task = rules.getParameterValue(TextContainerRules.TASK);

        if (task == null)
		{
			NullPointerException e = new NullPointerException("Parameter null: task");
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "computeContextChain", "Parameters: {0}, {1}, {2}", new Object[] { context, rules, masterLocale }, e);
			throw e;
		}

        return computeContextChainWithTask(context, task, masterLocale);
    }

    public void resolveComponentBundleWithContextAndLocales(Ctx ctx, ArrayList<LoadData> loadDelete, ArrayList<LoadData> loadInsert, byte[] originalComponentHash, byte[] bundleHash, String[] locales, TextContainerContext context) throws TextContainerException
	{
		try
		{
			String recipient = BundleData.bundleToRecipient(ctx, bundleHash, originalComponentHash);

			ContainerData[] unresolvedTexts = ContainerData.select(ctx, originalComponentHash, bundleHash);

			// Start the context resolution:
			HashMap<String, HashMap<Integer, ContainerData>> mapOfUnresolvedTexts = new HashMap<String, HashMap<Integer, ContainerData>>();
			HashMap<Integer, ContainerData> mapOfTextsWithContextId;

			ContainerData text;
			ContextData contextData;

			// Prepare a hashmap of arrays with all unresolved texts
			for (int j = 0; j < unresolvedTexts.length; j++)
			{
				text = unresolvedTexts[j];

				if (!mapOfUnresolvedTexts.containsKey(text.getTextKey()))
					mapOfUnresolvedTexts.put(text.getTextKey(), new HashMap<Integer, ContainerData>());

				mapOfTextsWithContextId = mapOfUnresolvedTexts.get(text.getTextKey());
				mapOfTextsWithContextId.put(new Integer(text.getContextId()), unresolvedTexts[j]);
			}

			Rules rules = new Rules();
			if (TextContainerRecipientListener.TCR_PCD.equals(recipient))
			{
				rules.setParameterValue(TextContainerRules.TASK, TextContainerRules.CR_VIEW_NLF);
			}
			else
			{
				rules.setParameterValue(TextContainerRules.TASK, TextContainerRules.CR_VIEW);
			}

			boolean textInDefaultContextFound;
			String contextLocale, contextIndustry, contextRegion, contextExtension;
			Iterator<HashMap<Integer, ContainerData>> iterUnresolved = mapOfUnresolvedTexts.values().iterator();
			Iterator<ContainerData> iter;

			HashMap<Integer, ArrayList<ContainerData>> mapOfResolvedTexts = new HashMap<Integer, ArrayList<ContainerData>>();
			ArrayList<ContainerData> listOfTexts;

			// Go throught all unresolved texts:
			while (iterUnresolved.hasNext())
			{
				textInDefaultContextFound = false;

				mapOfTextsWithContextId = iterUnresolved.next();

				ArrayList<Variant> variants = new ArrayList<Variant>();

				iter = mapOfTextsWithContextId.values().iterator();

				// Add all variants of one text and check if there is one text in the default context:
				while (iter.hasNext())
				{
					text = iter.next();
					try
					{
						contextData = ContextData.idToContext(ctx, text.getContextId());

						contextLocale = contextData.getLocale();
						contextIndustry = contextData.getIndustry();
						contextRegion = contextData.getRegion();
						contextExtension = contextData.getExtension();

						if (((contextLocale == null) || (contextLocale.length() == 0)) &&
							((contextIndustry == null) || (contextIndustry.length() == 0)) &&
							((contextRegion == null) || (contextRegion.length() == 0)) &&
							((contextExtension == null) || (contextExtension.length() == 0)))
						{
							variants.add(0, new Variant(contextLocale, contextIndustry,
									contextRegion, contextExtension, text, text.getOriginalLocale()));

							textInDefaultContextFound = true;
						}
						else
						{
							variants.add(new Variant(contextLocale, contextIndustry,
									contextRegion, contextExtension, text, text.getOriginalLocale()));
						}
					}
					catch (NoDataException e)
					{
			    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "resolveComponentBundleWithContextAndLocales", "Parameters: {0}, {1}, {2}, {3}, {4} - Reason: Context id {5} not found, no variant created!", new Object[] { ctx, originalComponentHash, bundleHash, locales, context, text.getContextId() }, e);
						throw new TextContainerException("NoDataException in resolveComponentBundleWithContextAndLocales", e);
					}
				}

				if (textInDefaultContextFound)
				{
					// There is a text in the default context so we do the context resolution for each requested locale (array 'locales'):
					for (int i = 0; i < locales.length; i++)
					{
						ContextData dbContext = new ContextData(locales[i], context.getIndustry(), context.getRegion(), context.getExtension());

						int contextId = ContextData.contextToId(ctx, dbContext);

						// Create context id if not existing
						if (contextId == 0)
						{
							contextId = ContextData.createNewContextId(ctx, dbContext);
						}

						Variant variant = null;

						try
						{
							variant = (Variant) resolveLogicalResource((Variant[]) variants.toArray(new Variant[0]), dbContext, rules);
						}
						catch (Exception e)
						{
				    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "resolveComponentBundleWithContextAndLocales", "Parameters: {0}, {1}, {2}, {3}, {4} - Reason: resolveLogicalResource({5}, {6}, {7}) failed!", new Object[] { ctx, originalComponentHash, bundleHash, locales, context, variants, context, rules }, e);
							throw new TextContainerException("Exception in resolveComponentBundleWithContextAndLocales", e);
						}

						if (variant != null)
						{
							// We have found a fitting variant and put the text of this variant into the map of resolved texts (sorted by contextid):
							text = variant.getText();

							if (!mapOfResolvedTexts.containsKey(contextId))
								mapOfResolvedTexts.put(new Integer(contextId), new ArrayList<ContainerData>());

							listOfTexts = mapOfResolvedTexts.get(contextId);
							listOfTexts.add(text);
						}
					}
				}
			}

			Iterator<Entry<Integer, ArrayList<ContainerData>>> iterContextIds = mapOfResolvedTexts.entrySet().iterator();

			// Go through the resolved texts (contextid wise):
			while (iterContextIds.hasNext())
			{
				Entry<Integer, ArrayList<ContainerData>> entryOfContextId = iterContextIds.next();

				int contextId = entryOfContextId.getKey();

				// Delete the existing entries in the text load for the bundle and the current contextid:
				loadDelete.add(new LoadData(contextId, originalComponentHash, bundleHash, 0, "", "", 0));

				iter = entryOfContextId.getValue().iterator();

				int elementId = 0;

				// Get all resolved texts for the current contextid:
				while (iter.hasNext())
				{
					text = iter.next();

					loadInsert.add(new LoadData(contextId, text.getOriginalComponentHash(),
							text.getBundleHash(), ++elementId, text.getTextKey(),
							text.getText(), text.getContextId()));
				}
			}
		}
		catch (SQLException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "resolveComponentBundleWithContextAndLocales", "Parameters: {0}, {1}, {2}, {3}, {4} - Reason: SQLException!", new Object[] { ctx, originalComponentHash, bundleHash, locales, context }, e);
			throw new TextContainerException("SQLException in resolveComponentBundleWithContextAndLocales", e);
		}
	}

    public Hashtable<String, String> resolveComponentBundleWithContext(Ctx ctx, byte[] originalComponentHash, byte[] bundleHash, TextContainerContext context) throws TextContainerException
	{
		try
		{
			String recipient = BundleData.bundleToRecipient(ctx, bundleHash, originalComponentHash);

			ContainerData[] unresolvedTexts = ContainerData.select(ctx, originalComponentHash, bundleHash);

			// Start the context resolution:
			HashMap<String, HashMap<Integer, ContainerData>> mapOfUnresolvedTexts = new HashMap<String, HashMap<Integer, ContainerData>>();
			HashMap<Integer, ContainerData> mapOfTextsWithContextId;

			ContainerData text;
			ContextData contextData;

			// Prepare a hashmap of arrays with all unresolved texts
			for (int j = 0; j < unresolvedTexts.length; j++)
			{
				text = unresolvedTexts[j];

				if (!mapOfUnresolvedTexts.containsKey(text.getTextKey()))
					mapOfUnresolvedTexts.put(text.getTextKey(), new HashMap<Integer, ContainerData>());

				mapOfTextsWithContextId = mapOfUnresolvedTexts.get(text.getTextKey());
				mapOfTextsWithContextId.put(new Integer(text.getContextId()), unresolvedTexts[j]);
			}

			Rules rules = new Rules();
			if (TextContainerRecipientListener.TCR_PCD.equals(recipient))
			{
				rules.setParameterValue(TextContainerRules.TASK, TextContainerRules.CR_VIEW_NLF);
			}
			else
			{
				rules.setParameterValue(TextContainerRules.TASK, TextContainerRules.CR_VIEW);
			}

			boolean textInDefaultContextFound;
			String contextLocale, contextIndustry, contextRegion, contextExtension;
			Iterator<HashMap<Integer, ContainerData>> iterUnresolved = mapOfUnresolvedTexts.values().iterator();
			Iterator<ContainerData> iter;

			ArrayList<ContainerData> listOfTexts = new ArrayList<ContainerData>();

			// Go throught all unresolved texts:
			while (iterUnresolved.hasNext())
			{
				textInDefaultContextFound = false;

				mapOfTextsWithContextId = iterUnresolved.next();

				ArrayList<Variant> variants = new ArrayList<Variant>();

				iter = mapOfTextsWithContextId.values().iterator();

				// Add all variants of one text and check if there is one text in the default context:
				while (iter.hasNext())
				{
					text = iter.next();
					try
					{
						contextData = ContextData.idToContext(ctx, text.getContextId());

						contextLocale = contextData.getLocale();
						contextIndustry = contextData.getIndustry();
						contextRegion = contextData.getRegion();
						contextExtension = contextData.getExtension();

						if (((contextLocale == null) || (contextLocale.length() == 0)) &&
							((contextIndustry == null) || (contextIndustry.length() == 0)) &&
							((contextRegion == null) || (contextRegion.length() == 0)) &&
							((contextExtension == null) || (contextExtension.length() == 0)))
						{
							variants.add(0, new Variant(contextLocale, contextIndustry,
									contextRegion, contextExtension, text, text.getOriginalLocale()));

							textInDefaultContextFound = true;
						}
						else
						{
							variants.add(new Variant(contextLocale, contextIndustry,
									contextRegion, contextExtension, text, text.getOriginalLocale()));
						}
					}
					catch (NoDataException e)
					{
			    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "resolveComponentBundleWithContext", "Parameters: {0}, {1}, {2}, {3} - Reason: Context id {4} not found, no variant created!", new Object[] { ctx, originalComponentHash, bundleHash, context, text.getContextId() }, e);
						throw new TextContainerException("NoDataException in resolveComponentBundleWithContext", e);
					}
				}

				if (textInDefaultContextFound)
				{
					// There is a text in the default context so we do the context resolution for the requested context:
					Variant variant = null;

					try
					{
						variant = (Variant) resolveLogicalResource((Variant[]) variants.toArray(new Variant[0]), context, rules);
					}
					catch (Exception e)
					{
			    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "resolveComponentBundleWithContext", "Parameters: {0}, {1}, {2}, {3} - Reason: resolveLogicalResource({4}, {5}, {6}) failed!", new Object[] { ctx, originalComponentHash, bundleHash, context, variants, context, rules }, e);
						throw new TextContainerException("Exception in resolveComponentBundleWithContext", e);
					}

					if (variant != null)
					{
						// We have found a fitting variant and put the text of this variant into the map of resolved texts (sorted by contextid):
						text = variant.getText();

						listOfTexts.add(text);
					}
				}
			}

			if (!listOfTexts.isEmpty())
			{
				Hashtable<String, String> texts = new Hashtable<String, String>();

				// Go through the resolved texts:
				for (ContainerData containerData : listOfTexts)
					texts.put(containerData.getTextKey(), containerData.getText());

				return texts;
			}
			else
				return null;
		}
		catch (SQLException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "resolveComponentBundleWithContext", "Parameters: {0}, {1}, {2}, {3} - Reason: SQLException!", new Object[] { ctx, originalComponentHash, bundleHash, context }, e);
			throw new TextContainerException("SQLException in resolveComponentBundleWithContext", e);
		}
	}

    public void resolveDirtyTexts(Ctx ctx, TextContainerContext context, String[] deployedLanguages) throws TextContainerException
	{
    	try
		{
			// If text load is not used for caching don't resolve dirty texts:
			if (!TextContainerConfiguration.useTextLoadForCachingIsActive())
				return;

    		DirtyData[] dirty = DirtyData.select(ctx);

			String[] locales;

			if (AllowedLanguages.getInstance().getProcessedLanguagesAvailable())
    		{
				locales = AllowedLanguages.getInstance().getLanguages();
    		}
    		else
    		{
    			locales = deployedLanguages;
    		}

			TextContainerContext contextWithoutLocale = new Context("", context.getIndustry(), context.getRegion(), context.getExtension());

			ArrayList<LoadData> loadDelete = new ArrayList<LoadData>();

			ArrayList<LoadData> loadInsert = new ArrayList<LoadData>();

			for (int i = 0; i < dirty.length; i++)
			{
	        	byte[] componentHash = dirty[i].getOriginalComponentHash();

	        	byte[] bundleHash = dirty[i].getBundleHash();

//				At the moment PCD gets all languages that others also get, so the following code is commented out:
//				if (TextContainerRecipient.TCR_PCD.equals(BundleData.bundleToRecipient(ctx, bundleHash, componentHash)))
//				{
//					ContainerIteratorLocales iterLocales = ContainerData.selectLocales(ctx, componentHash, bundleHash);
//
//					ArrayList<String> list = new ArrayList<String>();
//
//					while (iterLocales.next())
//					{
//						list.add(iterLocales.locale());
//					}
//
//					iterLocales.close();
//
//					locales = (String[])list.toArray(new String[] {});
//				}

	        	if (locales != null)
	        	{
	        		resolveComponentBundleWithContextAndLocales(ctx, loadDelete, loadInsert, componentHash, bundleHash, locales, contextWithoutLocale);
	        	}
	        	else
	        	{
	    			String[] availableLocales = null;

	    			// This should only be the case for undeployment and
	        		// we need to resolve for all available locales:
		    		if (!AllowedLanguages.getInstance().getProcessedLanguagesAvailable())
					{
						ContainerIteratorLocales iterLocales = ContainerData.selectLocales(ctx, componentHash, bundleHash);

						ArrayList<String> list = new ArrayList<String>();

						while (iterLocales.next())
						{
							list.add(iterLocales.locale());
						}

						iterLocales.close();

						availableLocales = (String[])list.toArray(new String[] {});
					}

		    		if (availableLocales != null)
		    			resolveComponentBundleWithContextAndLocales(ctx, loadDelete, loadInsert, componentHash, bundleHash, availableLocales, contextWithoutLocale);
	        	}

				DirtyData.delete(ctx, dirty[i]);
			}

			// Delete entries from the text load:
			if (loadDelete.size() > 0)
				LoadData.delete(ctx, (LoadData[]) loadDelete.toArray(new LoadData[0]));

			// Store the texts in the text load:
			if (loadInsert.size() > 0)
				LoadData.insert(ctx, (LoadData[]) loadInsert.toArray(new LoadData[0]));
		}
		catch (SQLException e)
		{
			throw new TextContainerException("SQLException in resolveDirtyTexts", e);
		}
    }

    public void resolveAllTexts(Ctx ctx, TextContainerContext context) throws TextContainerException
	{
    	try
		{
    		ContainerIteratorGrouped iterTextContainer = ContainerData.selectGroupedByOriginalCompHashAndBundleHash(ctx);

    		ArrayList<Text> texts = new ArrayList<Text>();

    		// Get all selected bundles:
       		while (iterTextContainer.next())
       		{
       			texts.add(new Text(iterTextContainer.originalCompHash(), iterTextContainer.bundleHash()));
       		}

       		iterTextContainer.close();

    		String[] locales = null;

    		if (AllowedLanguages.getInstance().getProcessedLanguagesAvailable())
    		{
        		locales = AllowedLanguages.getInstance().getLanguages();
    		}

   			byte[] oldOriginalCompHash = null;
   			byte[] newOriginalCompHash;
   			String originalCompName;

       		TextContainerContext contextWithoutLocale = new Context("", context.getIndustry(), context.getRegion(), context.getExtension());

       		ArrayList<LoadData> loadDelete = new ArrayList<LoadData>();

       		ArrayList<LoadData> loadInsert = new ArrayList<LoadData>();

   			HashMap<String, TextContainerRecipientTextsUpdatedEventImpl> updateNotification = new HashMap<String, TextContainerRecipientTextsUpdatedEventImpl>();

       		Iterator<Text> iterTexts = texts.iterator();

       		while (iterTexts.hasNext())
       		{
       			Text text = iterTexts.next();

       			newOriginalCompHash = text.getOriginalCompHash();
       			originalCompName = ComponentData.select(ctx, newOriginalCompHash).getComponent();

       			if ((oldOriginalCompHash != null) && (!Arrays.equals(newOriginalCompHash, oldOriginalCompHash)))
       			{
					// Only use text load if corresponding configuration is active (text load is used for caching):
					if (TextContainerConfiguration.useTextLoadForCachingIsActive())
					{
	       				// Delete entries from the text load:
	       				if (loadDelete.size() > 0)
	       					LoadData.delete(ctx, (LoadData[]) loadDelete.toArray(new LoadData[0]));

	       				// Store the texts in the text load:
	       				if (loadInsert.size() > 0)
	       					LoadData.insert(ctx, (LoadData[]) loadInsert.toArray(new LoadData[0]));

	       				DbFunctions.commit(ctx);
					}

       				// Notify recipients about updates:
       				notifyRecipientsAboutTextsUpdates(updateNotification);

					// Only use text load if corresponding configuration is active (text load is used for caching):
					if (TextContainerConfiguration.useTextLoadForCachingIsActive())
					{
						loadDelete = new ArrayList<LoadData>();

       	       			loadInsert = new ArrayList<LoadData>();
					}

       	       		updateNotification = new HashMap<String, TextContainerRecipientTextsUpdatedEventImpl>();
       	       	}

   				oldOriginalCompHash = newOriginalCompHash;

	        	byte[] bundleHash = text.getBundleHash();
	        	BundleData bundleData = BundleData.select(ctx, bundleHash, newOriginalCompHash);
	        	String bundleName = bundleData.getBundle();
	        	String bundleRecipient = bundleData.getRecipient();

	        	TextContainerRecipientTextsUpdatedEventImpl updateNotificationForRecipient = updateNotification.get(bundleRecipient);

	        	if (updateNotificationForRecipient == null)
	        	{
	        		updateNotificationForRecipient = new TextContainerRecipientTextsUpdatedEventImpl(originalCompName);

	        		updateNotification.put(bundleRecipient, updateNotificationForRecipient);
	        	}

	        	updateNotificationForRecipient.addBaseName(bundleName);

	        	// If there are no processed languages available, then we get all deployed locales for the current bundle from the container table:

//				At the moment PCD gets all languages that others also get, so the following code is commented out:
//				if (TextContainerRecipient.TCR_PCD.equals(BundleData.bundleToRecipient(ctx, bundleHash, componentHash)))
	    		if (!AllowedLanguages.getInstance().getProcessedLanguagesAvailable())
				{
					ContainerIteratorLocales iterLocales = ContainerData.selectLocales(ctx, newOriginalCompHash, bundleHash);

					ArrayList<String> list = new ArrayList<String>();

					while (iterLocales.next())
					{
						list.add(iterLocales.locale());
					}

					iterLocales.close();

					locales = (String[])list.toArray(new String[] {});
				}

				// Only resolve texts if text load is used for caching:
				if (TextContainerConfiguration.useTextLoadForCachingIsActive())
					resolveComponentBundleWithContextAndLocales(ctx, loadDelete, loadInsert, newOriginalCompHash, bundleHash, locales, contextWithoutLocale);
			}

			// Only use text load if corresponding configuration is active (text load is used for caching):
			if (TextContainerConfiguration.useTextLoadForCachingIsActive())
			{
				// Delete entries from the text load:
				if (loadDelete.size() > 0)
					LoadData.delete(ctx, (LoadData[]) loadDelete.toArray(new LoadData[0]));

				// Store the texts in the text load:
				if (loadInsert.size() > 0)
					LoadData.insert(ctx, (LoadData[]) loadInsert.toArray(new LoadData[0]));

				DbFunctions.commit(ctx);
			}

			// Notify recipients about updates:
			notifyRecipientsAboutTextsUpdates(updateNotification);

			// Delete all entries from the cache:
			SAPResourceBundleCache.clear();
		}
		catch (SQLException e)
		{
			throw new TextContainerException("SQLException in resolveAllTexts", e);
		}
    }

    private void notifyRecipientsAboutTextsUpdates(HashMap<String, TextContainerRecipientTextsUpdatedEventImpl> updateNotification) throws TextContainerException
    {
		HashMap<String, ArrayList<TextContainerRecipientListener>> recipientListeners = TextContainerService.getRecipientListeners();
		if (!recipientListeners.isEmpty())
		{
			Iterator<Entry<String, ArrayList<TextContainerRecipientListener>>> iterRecipientListeners = recipientListeners.entrySet().iterator();

			while (iterRecipientListeners.hasNext())
			{
				Entry<String, ArrayList<TextContainerRecipientListener>> entryOfRecipientListeners = iterRecipientListeners.next();

				TextContainerRecipientTextsUpdatedEventImpl updateNotificationForRecipient = updateNotification.get(entryOfRecipientListeners.getKey());

				if (updateNotificationForRecipient != null)
				{
					ArrayList<TextContainerRecipientListener> list = entryOfRecipientListeners.getValue();

					if (list != null)
					{
						for (int i = 0; i < list.size(); i++)
						{
							TextContainerRecipientListener recipientListener = list.get(i);

							if (recipientListener != null)
							{
								TextContainerRecipientTextsUpdatedEventImpl event = updateNotificationForRecipient;

								try
								{
									recipientListener.receive(event);
								}
								catch (TextContainerRecipientException e)
								{
									CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "resolveAllTexts", e);
									throw new TextContainerException("TextContainerRecipientException in resolveAllTexts", e);
								}
								catch (Exception e)
								{
									CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "resolveAllTexts", e);
									throw new TextContainerException("Exception in resolveAllTexts", e);
								}
							}
						}
					}
				}
			}
		}
    }

	class Text
	{
		byte[] originalCompHash;
		byte[] bundleHash;

		Text(byte[] newOriginalCompHash, byte[] newBundleHash)
		{
			originalCompHash = newOriginalCompHash;
			bundleHash = newBundleHash;
		}

		byte[] getOriginalCompHash()
		{
			return originalCompHash;
		}

		byte[] getBundleHash()
		{
			return bundleHash;
		}
	}

	// Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.context.ContextResolution");
	private static final Category CATEGORY = Category.SYS_SERVER;
}