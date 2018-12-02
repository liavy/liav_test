package com.sap.engine.services.textcontainer.context;

import com.sap.engine.interfaces.textcontainer.context.TextContainerContext;
import com.sap.engine.services.textcontainer.TextContainerException;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * This class is used internally to index context chains. It stores exactly
 * those logical resource attributes, context attributes and rules parameters
 * that are relevant to compute the context chain (method
 * <code>computeContextChain</code> in class {@link ContextResolution}). If
 * attributes or parameters are added or removed from the context resolution,
 * this class must be adapted, in particular the <code>equals</code> and
 * <code>hashCode</code> methods.
 * <p>
 * Since instances of this class are used as keys in hashtables this class
 * overrides the <code>equals</code> and <code>hashCode</code> methods of
 * class <code>Object</code>.
 * 
 * All attribute values are human-readable strings.
 * 
 * @author d041138: 25/08/2005 (adopted by d029702: Dec 1, 2006)
 */
public class ContextChainHandle
{

	// Important: If you add or remove an attribute, the methods equals and
	// hashCode must be adapted !!!

	private String locale;

	private String industry;

	private String region;

	private String extension;

	private String task;

	private String masterLocale;

	/**
	 * Construct a new instance and store all index relevant attributes
	 * inside.
	 */
	protected ContextChainHandle(TextContainerContext context, String task, String masterLocale) throws TextContainerException
	{
		if ((context == null) || (task == null))
		{
			String message = "Parameter null: ";
			String separator = "";
			if (context == null)
			{
				message += "context";
				separator = ", ";
			}
			if (task == null)
			{
				message += separator;
				message += "task";
			}
			NullPointerException e = new NullPointerException(message);
			CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "ContextChainHandle", "Parameters: {0}, {1}, {2}", new Object[] { context, task, masterLocale }, e);
			throw e;
		}

		this.locale = context.getLocale();
		this.industry = context.getIndustry();
		this.region = context.getRegion();
		this.extension = context.getExtension();

		this.task = task;
		this.masterLocale = (masterLocale != null ? masterLocale : "");

	}

	public String getLocale()
	{
		return locale;
	}

	public String getIndustry()
	{
		return industry;
	}

	public String getRegion()
	{
		return region;
	}

	public String getExtension()
	{
		return extension;
	}

	public String getTask()
	{
		return task;
	}

	public String getMasterLocale()
	{
		return masterLocale;
	}

	public boolean equals(Object obj)
	{
		if (!(obj instanceof ContextChainHandle))
			return false;

		ContextChainHandle handle = (ContextChainHandle) obj;

		return ((locale.equals(handle.getLocale())) &&
				(industry.equals(handle.getIndustry())) &&
				(region.equals(handle.getRegion())) &&
				(extension.equals(handle.getExtension())) &&
				(task.equals(handle.getTask())) &&
				(masterLocale.equals(handle.getMasterLocale())));
	}

	public int hashCode()
	{
		String concat = masterLocale + industry + region + extension + locale + task;

		return concat.hashCode();
	}

	// Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.context.ContextChainHandle");
	private static final Category CATEGORY = Category.SYS_SERVER;
}
