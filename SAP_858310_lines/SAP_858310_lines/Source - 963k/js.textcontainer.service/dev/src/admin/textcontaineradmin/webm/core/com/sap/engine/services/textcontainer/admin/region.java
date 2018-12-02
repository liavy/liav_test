/*
 * Created on Apr 24, 2006
 */
package com.sap.engine.services.textcontainer.admin;

import com.sap.engine.interfaces.textcontainer.TextContainerRegion;

/**
 * @author d029702
 */
public class Region implements TextContainerRegion
{

	/**
	 * 
	 */
	public Region(String region, String father, String termDomain, String collKey)
	{
		this.region = region;
		this.father = father;
		this.termDomain = termDomain;
		this.collKey = collKey;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerContextAttribute#getRegion()
	 */
	public String getRegion()
	{
		return region;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerContextAttribute#getFather()
	 */
	public String getFather()
	{
		return father;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerContextAttribute#getTermDomain()
	 */
	public String getTermDomain()
	{
		return termDomain;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerContextAttribute#getCollKey()
	 */
	public String getCollKey()
	{
		return collKey;
	}

	protected String region;
	protected String father;
	protected String termDomain;
	protected String collKey;

}
