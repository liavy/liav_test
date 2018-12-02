package com.sap.engine.services.textcontainer.handler.transaction;

public class DeployTransactionFactory extends HandlerTransactionFactoryHelper
{
	
	protected Class getTransactionImplementationClass()
	{
		return DeployTransaction.class;
	}

}
