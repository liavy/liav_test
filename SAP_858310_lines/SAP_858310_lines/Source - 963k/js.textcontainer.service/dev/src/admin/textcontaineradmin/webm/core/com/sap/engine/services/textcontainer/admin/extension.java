/*
 * Created on Apr 24, 2006
 */
package com.sap.engine.services.textcontainer.admin;

import com.sap.engine.interfaces.textcontainer.TextContainerExtension;

/**
 * @author d029702
 */
public class Extension implements TextContainerExtension
{

	/**
	 * 
	 */
	public Extension(String extension, String father, String termDomain, String collKey)
	{
		this.extension = extension;
		this.father = father;
		this.termDomain = termDomain;
		this.collKey = collKey;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerContextAttribute#getExtension()
	 */
	public String getExtension()
	{
		return extension;
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

	protected String extension;
	protected String father;
	protected String termDomain;
	protected String collKey;

}
