package com.sap.engine.interfaces.textcontainer.context;

/**
 * This interface must be implemented by all classes whose instances encapsulate
 * task parameters for the context resolution (method {@link TextContainerContextResolution#resolveLogicalResource(TextContainerVariant[], TextContainerContext, TextContainerRules)}).
 * 
 * <br/><br/>Copyright (c) 2006, SAP AG
 * @author  Thomas Goering
 * @version 1.0
 */
public interface TextContainerRules
{

	final static String TASK = "TASK"; //$NON-NLS-1$

	/**
	 * Resolution for all context attributes except locale and beginning with context K-1.
	 */
	final static String CR_VERT = "CR_VERT"; //$NON-NLS-1$

	/**
	 * Resolution for all context attributes.
	 */
	final static String CR_VIEW = "CR_VIEW"; //$NON-NLS-1$
	
	/**
	 * Resolution for all context attributes except locale (No Language Fallback).
	 */
	final static String CR_VIEW_NLF = "CR_VIEW_NLF"; //$NON-NLS-1$
	
	/**
	 * Returns the value of the given rules parameter.
	 * 
	 * @param pname
	 *            Name of the parameter
	 * @return Value of the parameter of null if the parameter is not defined.
	 */
	public String getParameterValue(String pname);

}
