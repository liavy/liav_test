/*
 * Created on Apr 24, 2006
 */
package com.sap.engine.services.textcontainer.admin;

import com.sap.engine.interfaces.textcontainer.TextContainerLanguage;

/**
 * @author d029702
 */
public class Language implements TextContainerLanguage
{

	/**
	 * 
	 */
	public Language(String locale, boolean isSecondaryLocale)
	{
		this.locale = locale;
		this.isSecondaryLocale = isSecondaryLocale;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerLanguage#getLocale()
	 */
	public String getLocale()
	{
		return locale;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerLanguage#getIsSecondaryLocale()
	 */
	public boolean getIsSecondaryLocale()
	{
		return isSecondaryLocale;
	}

	protected String locale;
	protected boolean isSecondaryLocale;

}
