/*
 * Created on Apr 24, 2006
 */
package com.sap.engine.services.textcontainer.api;

import com.sap.engine.interfaces.textcontainer.TextContainerRegion;

/**
 * @author d029702
 */
public class TextContainerRegionImpl implements TextContainerRegion
{

	public TextContainerRegionImpl(String region, String father, String termDomain, String collKey)
	{
		this.region = region;
		this.father = father;
		this.termDomain = termDomain;
		this.collKey = collKey;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerRegion#getRegion()
	 */
	public String getRegion()
	{
		return region;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerRegion#getFather()
	 */
	public String getFather()
	{
		return father;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerRegion#getTermDomain()
	 */
	public String getTermDomain()
	{
		return termDomain;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerRegion#getCollKey()
	 */
	public String getCollKey()
	{
		return collKey;
	}

	public boolean equals(Object obj)
	{
		if (!(obj instanceof TextContainerRegion))
			return false;

		TextContainerRegion objRegion = (TextContainerRegion) obj;

		return (region.equals(objRegion.getRegion()) &&
				father.equals(objRegion.getFather()) &&
				termDomain.equals(objRegion.getTermDomain()) &&
				collKey.equals(objRegion.getCollKey()));
	}

	public int hashCode()
	{
		String concat = region + father + termDomain + collKey;

		return concat.hashCode();
	}

	protected String region;
	protected String father;
	protected String termDomain;
	protected String collKey;

}
