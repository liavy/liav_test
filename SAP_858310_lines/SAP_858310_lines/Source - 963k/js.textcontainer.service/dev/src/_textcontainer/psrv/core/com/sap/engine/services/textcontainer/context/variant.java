package com.sap.engine.services.textcontainer.context;

import com.sap.engine.interfaces.textcontainer.context.TextContainerVariant;
import com.sap.engine.services.textcontainer.TextContainerException;
import com.sap.engine.services.textcontainer.dbaccess.ContainerData;

/**
 * This default implementation of the interface {@link IVariant}.
 * <p>
 * 
 * @author d041138: Oct 5, 2005 (adopted by d029702: Mar 28, 2006)
 */
public class Variant extends Context implements TextContainerVariant
{
	protected ContainerData text;

	protected String masterLocale;

	public Variant(String locale, String industry, String region, String extension, ContainerData text, String masterLocale) throws TextContainerException
	{
		super(locale, industry, region, extension);

		this.text = text;
		this.masterLocale = masterLocale;
	}

	public ContainerData getText()
	{
		return text;
	}

	public String getMasterLocale()
	{
		return masterLocale;
	}
}