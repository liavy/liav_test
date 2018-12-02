/*
 * Created on Apr 24, 2006
 */
package com.sap.engine.services.textcontainer.api;

import com.sap.engine.interfaces.textcontainer.TextContainerLocale;

/**
 * @author d029702
 */
public class TextContainerLocaleImpl implements TextContainerLocale
{

	public TextContainerLocaleImpl(String startLocale, int sequenceNumber, String locale)
	{
		this.startLocale = startLocale;
		this.sequenceNumber = sequenceNumber;
		this.locale = locale;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerLocale#getStartLocale()
	 */
	public String getStartLocale()
	{
		return startLocale;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerLocale#getSequenceNumber()
	 */
	public int getSequenceNumber()
	{
		return sequenceNumber;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerLocale#getLocale()
	 */
	public String getLocale()
	{
		return locale;
	}

	public boolean equals(Object obj)
	{
		if (!(obj instanceof TextContainerLocale))
			return false;

		TextContainerLocale objLocale = (TextContainerLocale) obj;

		return (startLocale.equals(objLocale.getStartLocale()) &&
				sequenceNumber == objLocale.getSequenceNumber() &&
				locale.equals(objLocale.getLocale()));
	}

	public int hashCode()
	{
		String concat = startLocale + sequenceNumber + locale;

		return concat.hashCode();
	}

	protected String startLocale;
	protected int sequenceNumber;
	protected String locale;

}
