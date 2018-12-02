/*
 * Created on Apr 24, 2006
 */
package com.sap.engine.services.textcontainer.admin;

import com.sap.engine.interfaces.textcontainer.TextContainerLocale;

/**
 * @author d029702
 */
public class Locale implements TextContainerLocale
{

	/**
	 * 
	 */
	public Locale(String startLocale, int sequenceNumber, String locale)
	{
		this.startLocale = startLocale;
		this.sequenceNumber = sequenceNumber;
		this.locale = locale;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerContextAttribute#getStartLocale()
	 */
	public String getStartLocale()
	{
		return startLocale;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerContextAttribute#getSequenceNumber()
	 */
	public int getSequenceNumber()
	{
		return sequenceNumber;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerContextAttribute#getLocale()
	 */
	public String getLocale()
	{
		return locale;
	}

	protected String startLocale;
	protected int sequenceNumber;
	protected String locale;

}
