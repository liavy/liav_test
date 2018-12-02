/*
 * Created on Feb 3, 2006
 */
package com.sap.engine.interfaces.textcontainer;

/**
 * <br/><br/>Copyright (c) 2006, SAP AG
 * @author  Thomas Goering
 * @version 1.0
 */
public class TextContainerManagerException extends Exception
{

	/**
	 * @param message
	 */
	public TextContainerManagerException(String message)
	{
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public TextContainerManagerException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
