package com.sap.engine.interfaces.textcontainer.context;

/**
 * This interface must be implemented by all clients that want to use the
 * context resolution (method {@link TextContainerContextResolution#resolveLogicalResource(TextContainerVariant[], TextContainerContext, TextContainerRules)}).
 * 
 * <br/><br/>Copyright (c) 2006, SAP AG
 * @author  Thomas Goering
 * @version 1.0
 */
public interface TextContainerVariant extends TextContainerContext
{

	/**
     * Returns the value of the master locale attribute.
     * 
     * @return Value of the master locale attribute or null.
     */
    public String getMasterLocale();

}
