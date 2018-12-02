package com.sap.security.core.admin;

import com.sap.security.api.UMException;

public class AccessToLogicException extends UMException
{
	public AccessToLogicException()
	{
		super();
	}

	public AccessToLogicException(String message)
	{
		super(message);
	}

	public AccessToLogicException(Exception ex)
	{
		super(ex);
	}

}
