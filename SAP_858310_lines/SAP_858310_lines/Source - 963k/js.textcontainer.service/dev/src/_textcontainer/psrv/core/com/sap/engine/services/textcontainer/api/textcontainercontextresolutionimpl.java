package com.sap.engine.services.textcontainer.api;

import java.util.Hashtable;

import com.sap.engine.interfaces.textcontainer.TextContainerManagerException;
import com.sap.engine.interfaces.textcontainer.context.TextContainerContext;
import com.sap.engine.interfaces.textcontainer.context.TextContainerContextResolution;
import com.sap.engine.interfaces.textcontainer.context.TextContainerRules;
import com.sap.engine.interfaces.textcontainer.context.TextContainerVariant;
import com.sap.engine.services.textcontainer.TextContainerException;
import com.sap.engine.services.textcontainer.context.ContextResolution;
import com.sap.engine.services.textcontainer.context.TransferFunctions;

public class TextContainerContextResolutionImpl implements TextContainerContextResolution
{

	private static TextContainerContextResolutionImpl instance;

    private TextContainerContextResolutionImpl()
    {
    }

    /**
     * @return The singleton instance of the context resolution.
     */
    public static TextContainerContextResolution getInstance()
    {
        if (instance == null)
            instance = new TextContainerContextResolutionImpl();

    	return instance;
    }

	public TextContainerVariant resolveLogicalResource(
			TextContainerVariant[] variants, TextContainerContext context,
			TextContainerRules rules) throws TextContainerManagerException
	{
		TextContainerVariant variant = null;
		try
		{
			variant = ContextResolution.getInstance().resolveLogicalResource(variants, context, rules);
		}
		catch (TextContainerException e)
		{
			throw new TextContainerManagerException("TextContainerException", e);
		}
		return variant;
	}

	public Hashtable<TextContainerContext, Integer> computeContextChain(TextContainerContext context,
			TextContainerRules rules, String masterLocale) throws TextContainerManagerException
	{
		Hashtable<TextContainerContext, Integer> contextChain = null;
		try
		{
			contextChain = ContextResolution.getInstance().computeContextChain(context, rules, masterLocale).getContextChain();
		}
		catch (TextContainerException e)
		{
			throw new TextContainerManagerException("TextContainerException", e);
		}
		return contextChain;
	}

    public String[] computeLocaleChain(String locale, String masterLocale) throws TextContainerManagerException
    {
		String[] localeChain = null;
		try
		{
			localeChain = TransferFunctions.getInstance().getLocaleChain(TextContainerRules.CR_VIEW, locale, masterLocale);
		}
		catch (TextContainerException e)
		{
			throw new TextContainerManagerException("TextContainerException", e);
		}
		return localeChain;
    }

}
