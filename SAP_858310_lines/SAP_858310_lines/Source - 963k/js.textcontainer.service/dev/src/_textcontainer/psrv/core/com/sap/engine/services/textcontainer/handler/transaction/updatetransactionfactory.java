package com.sap.engine.services.textcontainer.handler.transaction;

public class UpdateTransactionFactory extends HandlerTransactionFactoryHelper
{
	
	protected Class getTransactionImplementationClass()
	{
		return UpdateTransaction.class;
	}

}
