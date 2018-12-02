package com.sap.engine.services.textcontainer.handler.transaction;

public class RemoveTransactionFactory extends HandlerTransactionFactoryHelper
{
	
	protected Class getTransactionImplementationClass()
	{
		return RemoveTransaction.class;
	}

}
