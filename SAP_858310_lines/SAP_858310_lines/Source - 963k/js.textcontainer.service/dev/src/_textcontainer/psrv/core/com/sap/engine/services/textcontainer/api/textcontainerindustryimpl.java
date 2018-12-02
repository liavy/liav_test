/*
 * Created on Apr 24, 2006
 */
package com.sap.engine.services.textcontainer.api;

import com.sap.engine.interfaces.textcontainer.TextContainerIndustry;

/**
 * @author d029702
 */
public class TextContainerIndustryImpl implements TextContainerIndustry
{

	public TextContainerIndustryImpl(String industry, String father, String termDomain, String collKey)
	{
		this.industry = industry;
		this.father = father;
		this.termDomain = termDomain;
		this.collKey = collKey;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerIndustry#getIndustry()
	 */
	public String getIndustry()
	{
		return industry;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerIndustry#getFather()
	 */
	public String getFather()
	{
		return father;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerIndustry#getTermDomain()
	 */
	public String getTermDomain()
	{
		return termDomain;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerIndustry#getCollKey()
	 */
	public String getCollKey()
	{
		return collKey;
	}

	public boolean equals(Object obj)
	{
		if (!(obj instanceof TextContainerIndustry))
			return false;

		TextContainerIndustry objIndustry = (TextContainerIndustry) obj;

		return (industry.equals(objIndustry.getIndustry()) &&
				father.equals(objIndustry.getFather()) &&
				termDomain.equals(objIndustry.getTermDomain()) &&
				collKey.equals(objIndustry.getCollKey()));
	}

	public int hashCode()
	{
		String concat = industry + father + termDomain + collKey;

		return concat.hashCode();
	}

	protected String industry;
	protected String father;
	protected String termDomain;
	protected String collKey;

}
