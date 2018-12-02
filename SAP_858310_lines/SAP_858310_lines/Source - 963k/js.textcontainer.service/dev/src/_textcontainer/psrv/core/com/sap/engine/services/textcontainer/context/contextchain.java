package com.sap.engine.services.textcontainer.context;

import java.util.ArrayList;
import java.util.Hashtable;

import com.sap.engine.interfaces.textcontainer.context.TextContainerContext;
import com.sap.engine.services.textcontainer.TextContainerException;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * @author d041138: 25/08/2005 (adopted by d029702: Mar 28, 2006)
 */
public class ContextChain
{
	private Hashtable<TextContainerContext, Integer> contexts;

	private TextContainerContext[] contextChain;

	private int numOfContexts;

	private ContextChainHandle handle;

	public ContextChain(ContextChainHandle handle, ArrayList<TextContainerContext> contexts)
	{
		if ((handle == null) || (contexts == null))
		{
			String message = "Parameter null: ";
			String separator = "";
			if (handle == null)
			{
				message += "handle";
				separator = ", ";
			}
			if (contexts == null)
			{
				message += separator;
				message += "contexts";
			}
			NullPointerException e = new NullPointerException(message);
			CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "ContextChainHandle", "Parameters: {0}, {1}", new Object[] { handle, contexts }, e);
			throw e;
		}

		this.numOfContexts = contexts.size();
		this.contexts = new Hashtable<TextContainerContext, Integer>();
		for (int i = 0; i < numOfContexts; i++)
			this.contexts.put(contexts.get(i), new Integer(i));
		this.contextChain = contexts.toArray(new TextContainerContext[0]);
		this.handle = handle; 
	}
	
	/**
	 * Return a requested context.
	 * 
	 * @param index
	 *            The index
	 * @return The context.
	 */
	public TextContainerContext getContext(int index)
	{
		if ((index >= 0) && (index < numOfContexts))
			return contextChain[index];
		else
			return null;
	}

	/**
	 * Return the handle of the context chain.
	 * 
	 * @return The context chain handle.
	 */
	public ContextChainHandle getContextChainHandle()
	{
		return handle;
	}

	/**
	 * Return the context chain.
	 * 
	 * @return The context chain.
	 */
	public Hashtable<TextContainerContext, Integer> getContextChain()
	{
		return contexts;
	}

	/**
	 * Return the index of the given context in this context chain.
	 * 
	 * @param context
	 *            The context
	 * @return The index of the given context or -1 if the context is not
	 *         contained in this context chain.
	 * @throws TextContainerException if parameter is null.
	 */
	public int getIndex(TextContainerContext context) throws TextContainerException
	{
		if (context == null)
		{
			NullPointerException e = new NullPointerException("Parameter null: context");
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getIndex", "Parameters: {0}", new Object[] { context }, e);
			throw e;
		}

		Integer index = (Integer) contexts.get(context);
		if (index == null)
			return -1;

		return index.intValue();
	}

	// Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.context.ContextChain");
	private static final Category CATEGORY = Category.SYS_SERVER;
}