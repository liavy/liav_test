/*
 * Created on Apr 24, 2006
 */
package com.sap.engine.services.textcontainer.api;

import com.sap.engine.interfaces.textcontainer.TextContainerExtension;

/**
 * @author d029702
 */
public class TextContainerExtensionImpl implements TextContainerExtension
{

	public TextContainerExtensionImpl(String extension, String father, String termDomain, String collKey)
	{
		this.extension = extension;
		this.father = father;
		this.termDomain = termDomain;
		this.collKey = collKey;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerExtension#getExtension()
	 */
	public String getExtension()
	{
		return extension;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerExtension#getFather()
	 */
	public String getFather()
	{
		return father;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerExtension#getTermDomain()
	 */
	public String getTermDomain()
	{
		return termDomain;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.textcontainer.TextContainerExtension#getCollKey()
	 */
	public String getCollKey()
	{
		return collKey;
	}

	public boolean equals(Object obj)
	{
		if (!(obj instanceof TextContainerExtension))
			return false;

		TextContainerExtension objExtension = (TextContainerExtension) obj;

		return (extension.equals(objExtension.getExtension()) &&
				father.equals(objExtension.getFather()) &&
				termDomain.equals(objExtension.getTermDomain()) &&
				collKey.equals(objExtension.getCollKey()));
	}

	public int hashCode()
	{
		String concat = extension + father + termDomain + collKey;

		return concat.hashCode();
	}

	protected String extension;
	protected String father;
	protected String termDomain;
	protected String collKey;

}
