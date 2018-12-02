package com.sap.engine.services.textcontainer.context;

import java.util.Hashtable;

import com.sap.engine.interfaces.textcontainer.context.TextContainerContext;
import com.sap.engine.services.textcontainer.TextContainerException;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * In this class all precompiled context chains are stored. This class can only
 * be instantiated as a singleton.
 * <p>
 * 
 * @author d041138: 26/08/2005 (adopted by d029702: Mar 28, 2006)
 */
public class ContextChains
{
    private static Hashtable<ContextChainHandle, ContextChain> contextChains;

    private static ContextChains instance;

    /**
     * Return the singleton instance of this class.
     * 
     * @return The singleton instance of this class.
     */
    public static ContextChains getInstance()
    {
        if (instance == null)
            instance = new ContextChains();

        return instance;
    }

    private ContextChains()
    {
    	initialize();
    }

    public void initialize()
    {
        contextChains = new Hashtable<ContextChainHandle, ContextChain>();
    }

    /**
     * Return the previously computed context chain for the given handle.
     * 
     * @param context
     *            The context.
     * @param task
     *            The task.
     * @param masterLocale
     *            The master locale.
     * @return The context chain or null if no chain has been computed for the
     *         given attributes values.
     * @throws TextContainerException
     *             if an input parameter or one of the required attributes is
     *             null
     */
    public ContextChain getContextChain(TextContainerContext context, String task, String masterLocale) throws TextContainerException
    {
		ContextChainHandle handle = new ContextChainHandle(context, task, masterLocale);

		return contextChains.get(handle);
    }

    /**
     * Add the given context chain to the list of already computed context
     * chains if there is not yet a context chain which is associated to the
     * index of the given context chain.
     * 
     * @param handle
     *            The handle.
     * @param cchain
     *            The context chain.
     * @return True if there was no previously computed context chain for the
     *         logical resource, context and rules associated with the given
     *         contex chain. False otherwise.
     * @throws TextContainerException
     */
    public boolean addContextChain(ContextChain cchain) throws TextContainerException
    {
        if (cchain == null)
		{
			NullPointerException e = new NullPointerException("Parameter null: cchain");
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "addContextChain", "Parameters: {0}", new Object[] { cchain }, e);
			throw e;
		}

        ContextChainHandle handle = cchain.getContextChainHandle();

        if (contextChains.containsKey(handle))
        {
            return false;
        }
        else
        {
            contextChains.put(handle, cchain);
            return true;
        }
    }

	// Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.context.ContextChains");
	private static final Category CATEGORY = Category.SYS_SERVER;
}