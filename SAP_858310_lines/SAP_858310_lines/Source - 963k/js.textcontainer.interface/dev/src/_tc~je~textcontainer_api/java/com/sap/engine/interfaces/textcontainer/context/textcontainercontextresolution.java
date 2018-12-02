package com.sap.engine.interfaces.textcontainer.context;

import java.util.Hashtable;

import com.sap.engine.interfaces.textcontainer.TextContainerManagerException;

/**
 * This interface must be implemented by all classes whose instances encapsulate
 * task parameters for the context resolution (method {@link TextContainerContextResolution#resolveLogicalResource(TextContainerVariant[], TextContainerContext, TextContainerRules)}).
 * 
 * <br/><br/>Copyright (c) 2006, SAP AG
 * @author  Thomas Goering
 * @version 1.0
 */
public interface TextContainerContextResolution
{

    /**
     * @param variants
     *            List of candidates.
     * @param context
     *            Context parameters with human readable strings.
     * @param rules
     *            Rules which control the context resolution.
     * @return One variant or null.
     * @throws TextContainerManagerException
     */
    public TextContainerVariant resolveLogicalResource(TextContainerVariant[] variants, TextContainerContext context, TextContainerRules rules) throws TextContainerManagerException;

    /**
     * @param context
     *            The context
     * @param rules
     *            The set of rules parameters
     * @param masterLocale
     * 			  The master locale
     * @return List of contexts in the order in which they appear in the context
     *         chain.
     * @throws TextContainerManagerException
     */
    public Hashtable<TextContainerContext, Integer> computeContextChain(TextContainerContext context, TextContainerRules rules, String masterLocale) throws TextContainerManagerException;

    /**
     * @param locale
     *            The locale
     * @param masterLocale
     * 			  The master locale
     * @return List of locales in the order in which they appear in the locale chain.
     * @throws TextContainerManagerException
     */
    public String[] computeLocaleChain(String locale, String masterLocale) throws TextContainerManagerException;

}
