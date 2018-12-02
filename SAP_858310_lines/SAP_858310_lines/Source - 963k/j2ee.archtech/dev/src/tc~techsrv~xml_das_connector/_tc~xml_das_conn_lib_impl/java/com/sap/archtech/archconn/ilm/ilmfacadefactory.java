package com.sap.archtech.archconn.ilm;

/**
 * The <code>IlmFacadeFactory</code> class provides access to the ILM API implementation of the XML Archive API for Java
 */
public class IlmFacadeFactory 
{
	private static final IlmFacadeFactory theInstance = new IlmFacadeFactory();
	
	private IlmFacadeFactory(){}
	
	public static IlmFacadeFactory getInstance()
	{
		return theInstance;
	}

	public IIlmFacade getIlmFacade()
	{
		return new IlmFacadeImpl();
	}
}
