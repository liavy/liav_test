package com.sap.engine.services.textcontainer.handler.transaction;

import com.sap.engine.services.content.handler.api.ContentDeployItem;
import com.sap.engine.services.content.handler.api.ContentOperationInfo;
import com.sap.engine.services.content.handler.api.exception.ContentHandlerException;
import com.sap.engine.services.content.handler.api.exception.ContentHandlerWarning;

public class UpdateTransaction extends DeployUpdateTransaction
{

	public ContentOperationInfo begin(ContentDeployItem cItem) throws ContentHandlerException, ContentHandlerWarning
	{
		return super.begin(cItem);
	}

	public void commit() throws ContentHandlerWarning
	{
		super.commit();
	}

	public void rollback() throws ContentHandlerWarning
	{
		super.rollback();
	}

	// Logging:
//	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.handler.transaction.UpdateRemoveTransaction");
//	private static final Category CATEGORY = Category.SYS_SERVER;

}
