package com.sap.archtech.archconn.exceptions;

/**
 * This exception is raised when an
 * ArchCommand method is invoked on
 * an archiving command, which does not support this method.
 * 
 * @author D025792
 * @version 1.0
 * 
 */

public class UnsupportedCommandException extends ArchConnException
{
  private static final long serialVersionUID = 42L;
  
	public UnsupportedCommandException()
	{
		super();
	}


	public UnsupportedCommandException(String s)
	{
		super(s);
	}
}
