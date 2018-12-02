package com.sap.engine.services.textcontainer.admin.context;

import com.sap.engine.interfaces.textcontainer.context.TextContainerContext;
import com.sap.engine.interfaces.textcontainer.context.TextContainerVariant;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Instances of this class are used to return contexts to the caller of the
 * context resolution, e.g. inside an instance of ContextChain. Currently the
 * attributes LOCALE, INDUSTRY, REGION, EXTENSION are supported.
 * 
 * @author d041138, Sep 13, 2005 (adopted by d029702: Mar 28, 2006)
 */
public class Context implements TextContainerContext
{
	protected String locale;
	protected String industry;
	protected String region;
	protected String extension;

	/**
	 * Convenience constructor.
	 * 
	 * @param variant
	 */
	public Context(TextContainerVariant variant)
	{
		this(variant.getLocale(), variant.getIndustry(), variant.getRegion(), variant.getExtension());
	}

	public Context(String locale, String industry, String region, String extension)/* throws TextContainerException */
	{
		if ((locale == null) || (industry == null) || (region == null) || (extension == null))
		{
			String message = "Parameter null: ";
			String separator = "";
			if (locale == null)
			{
				message += "locale";
				separator = ", ";
			}
			if (industry == null)
			{
				message += separator;
				message += "industry";
				separator = ", ";
			}
			if (region == null)
			{
				message += separator;
				message += "region";
				separator = ", ";
			}
			if (extension == null)
			{
				message += separator;
				message += "extension";
			}
			NullPointerException e = new NullPointerException(message);
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "Context", "Parameters: {0}, {1}, {2}, {3}", new Object[] { locale, industry, region, extension }, e);
			throw e;
		}

		setLocale(locale);
		setExtension(extension);
		setIndustry(industry);
		setRegion(region);
	}

	public void setLocale(String locale)
	{
		this.locale = locale;
	}

	public void setIndustry(String industry)
	{
		this.industry = industry;
	}

	public void setRegion(String region)
	{
		this.region = region;
	}

	public void setExtension(String extension)
	{
		this.extension = extension;
	}

	public String getExtension()
	{
		return extension;
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

	/**
	 * Overrides the <code>equals</code> method of <code>Object</code>. Two
	 * instances are equal if and only if the contents of all the attributes and
	 * parameters stored in this object are equal. This is necessary for the
	 * method <code>getIndex</code> in class {@link ContextChain}.
	 * 
	 * @param obj
	 *            Object with which this object is compared
	 * @return True if the argument is an instance of this class and its methods
	 *         getExtension(), etc. return the same values as for this object.
	 */
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Context))
			return false;

		Context context = (Context) obj;

		return (extension.equals(context.getExtension())
				&& industry.equals(context.getIndustry())
				&& locale.equals(context.getLocale())
				&& region.equals(context.getRegion()));
	}

	/**
	 * Overrides the <code>hashCode</code> method of <code>Object</code>.
	 * This is necessary to make it compatible with the <code>equals</code>
	 * method (hashcode contract).
	 * 
	 * @return The hashcode of this object.
	 */
	public int hashCode()
	{
		String concat = industry + locale + region + extension;

		return concat.hashCode();
	}

	// Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.context.Context");
	private static final Category CATEGORY = Category.SYS_SERVER;

}