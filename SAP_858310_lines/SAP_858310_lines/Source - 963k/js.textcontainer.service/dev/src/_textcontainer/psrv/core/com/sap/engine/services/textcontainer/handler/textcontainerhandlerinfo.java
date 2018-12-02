package com.sap.engine.services.textcontainer.handler;

import com.sap.engine.services.content.handler.api.ContentHandlerInfo;
import com.sap.engine.services.content.handler.api.TransactionFactory;
import com.sap.engine.services.textcontainer.handler.transaction.DeployTransactionFactory;
import com.sap.engine.services.textcontainer.handler.transaction.HandlerTransactionFactoryHelper;
import com.sap.engine.services.textcontainer.handler.transaction.RemoveTransactionFactory;
import com.sap.engine.services.textcontainer.handler.transaction.UpdateTransactionFactory;

public class TextContainerHandlerInfo extends ContentHandlerInfo
{

	protected static String[] supportedSubTypes = {"text"};
	
	public TextContainerHandlerInfo()
	{
		super();
	}

	@Override
	public String getName()
	{
		return "textcontainer";
	}

	@Override
	public TransactionFactory getDeployTransactionFactory()
	{
		return HandlerTransactionFactoryHelper.getFactoryInstance(DeployTransactionFactory.class);
	}

	@Override
	public TransactionFactory getUpdateTransactionFactory()
	{
		return HandlerTransactionFactoryHelper.getFactoryInstance(UpdateTransactionFactory.class);
	}

	@Override
	public TransactionFactory getRemoveTransactionFactory()
	{
		return HandlerTransactionFactoryHelper.getFactoryInstance(RemoveTransactionFactory.class);
	}

	@Override
	public String getServiceName()
	{
		return "textcontainer";
	}

	@Override
	public String[] getResourceTypes()
	{
		return null;
	}

	@Override
	public String[] getSupportedSoftwareSubTypes() {
		return supportedSubTypes;
	}

}
