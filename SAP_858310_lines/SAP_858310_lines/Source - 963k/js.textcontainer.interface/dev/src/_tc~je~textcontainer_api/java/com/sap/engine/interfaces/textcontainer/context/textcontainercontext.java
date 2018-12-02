package com.sap.engine.interfaces.textcontainer.context;

/**
 * This interface must be implemented by all clients that want to use the
 * context resolution (and other methods in class {@link TextContainerContextResolution}).
 * 
 * <br/><br/>Copyright (c) 2006, SAP AG
 * @author  Thomas Goering
 * @version 1.0
 */
public interface TextContainerContext
{

	/**
     * Returns the value of the locale attribute.
     * 
     * @return Value of the locale attribute or null.
     */
    public String getLocale();

    /**
     * Returns the value of the industry attribute.
     * 
     * @return Value of the industry attribute or null.
     */
    public String getIndustry();

    /**
     * Returns the value of the region attribute.
     * 
     * @return Value of the region attribute or null.
     */
    public String getRegion();

    /**
     * Returns the value of the extension attribute.
     * 
     * @return Value of the extension attribute or null.
     */
    public String getExtension();

}
