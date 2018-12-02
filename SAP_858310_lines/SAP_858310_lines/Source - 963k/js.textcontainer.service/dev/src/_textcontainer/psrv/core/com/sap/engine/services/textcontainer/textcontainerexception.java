/*
 * Created on Apr 20, 2006
 */
package com.sap.engine.services.textcontainer;

/**
 * @author d029702
 */
public class TextContainerException extends Exception
{

	public TextContainerException()
	{
		super();
	}

	/**
	 * @param message
	 */
	public TextContainerException(String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public TextContainerException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public TextContainerException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
