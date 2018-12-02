/*
 * Created on Jan 18, 2007
 */
package com.sap.engine.interfaces.textcontainer;

/**
 * <br/><br/>Copyright (c) 2007, SAP AG
 * @author  Thomas Goering
 * @version 1.0
 */
public class TextContainerManagerMissingBundleException extends TextContainerManagerException
{

	/**
	 * @param message
	 */
	public TextContainerManagerMissingBundleException(String message)
	{
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public TextContainerManagerMissingBundleException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
