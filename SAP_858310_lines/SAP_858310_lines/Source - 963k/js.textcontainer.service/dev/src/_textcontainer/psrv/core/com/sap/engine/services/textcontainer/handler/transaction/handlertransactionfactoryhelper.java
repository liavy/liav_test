package com.sap.engine.services.textcontainer.handler.transaction;

import java.lang.reflect.Constructor;
import java.util.Hashtable;

import com.sap.engine.services.content.handler.api.ContentTransaction;
import com.sap.engine.services.content.handler.api.TransactionContext;
import com.sap.engine.services.content.handler.api.TransactionFactory;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public abstract class HandlerTransactionFactoryHelper implements TransactionFactory
{	

	protected static Hashtable<Class, TransactionFactory> factories = new Hashtable<Class, TransactionFactory>();
	
	public static TransactionFactory getFactoryInstance(Class implClass)
	{
		TransactionFactory tFact = factories.get(implClass);

		if (tFact == null)
		{
			try
			{
				tFact = (TransactionFactory) implClass.newInstance();

				factories.put(implClass, tFact);
			}
			catch (Exception e)
			{
	    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getFactoryInstance", e);
			}
		}

		return tFact;
	}
	
	public ContentTransaction getTransactionInstance(TransactionContext ctx)
	{
		ContentTransaction transInstance = null;

		try
		{
			Constructor constr = getTransactionImplementationClass().getConstructor(new Class[]{/*TransactionContext.class*/});
			transInstance = (ContentTransaction) constr.newInstance(/*new Object[]{ctx}*/);
		}
		catch (Exception e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getTransactionInstance", e);
		}

		return transInstance;
	}
	
	protected abstract Class getTransactionImplementationClass();

	// Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.handler.transaction.HandlerTransactionFactoryHelper");
	private static final Category CATEGORY = Category.SYS_SERVER;

}
