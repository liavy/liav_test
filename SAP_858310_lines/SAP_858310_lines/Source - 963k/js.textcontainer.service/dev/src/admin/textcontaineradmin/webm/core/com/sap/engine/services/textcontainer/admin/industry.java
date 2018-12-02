/*
 * Created on Apr 24, 2006
 */
package com.sap.engine.services.textcontainer.admin;

import com.sap.engine.interfaces.textcontainer.TextContainerIndustry;

/**
 * @author d029702
 */
public class Industry implements TextContainerIndustry
{

	/**
	 * 
	 */
	public Industry(String industry, String father, String termDomain, String collKey)
	{
		this.industry = industry;
		this.father = father;
		this.termDomain = termDomain;
		this.collKey = collKey;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerContextAttribute#getIndustry()
	 */
	public String getIndustry()
	{
		return industry;
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

	protected String industry;
	protected String father;
	protected String termDomain;
	protected String collKey;

}
