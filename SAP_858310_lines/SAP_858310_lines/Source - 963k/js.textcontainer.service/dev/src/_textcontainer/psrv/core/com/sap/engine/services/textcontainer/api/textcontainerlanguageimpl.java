/*
 * Created on Apr 24, 2006
 */
package com.sap.engine.services.textcontainer.api;

import com.sap.engine.interfaces.textcontainer.TextContainerLanguage;

/**
 * @author d029702
 */
public class TextContainerLanguageImpl implements TextContainerLanguage
{

	public TextContainerLanguageImpl(String locale, boolean isSecondaryLocale)
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

	public boolean equals(Object obj)
	{
		if (!(obj instanceof TextContainerLanguage))
			return false;

		TextContainerLanguage objLanguage = (TextContainerLanguage) obj;

		return (locale.equals(objLanguage.getLocale()) &&
				isSecondaryLocale == objLanguage.getIsSecondaryLocale());
	}

	public int hashCode()
	{
		String concat = locale + isSecondaryLocale;

		return concat.hashCode();
	}

	protected String locale;
	protected boolean isSecondaryLocale;

}
