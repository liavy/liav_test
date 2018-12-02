/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */

package com.sap.engine.interfaces.textcontainer;

/**
 *
 * Interface for administrating the context attribute region for the TextContainer.
 *
 * <br/><br/>Copyright (c) 2006, SAP AG
 * @author  Thomas Goering
 * @version 1.0
 */
public interface TextContainerRegion
{

	/**
	 * Gets the context attribute region.
	 *
	 * @return Context attribute region.
	 */
	public String getRegion();

	/**
	 * Gets the context attribute father.
	 *
	 * @return Context attribute father.
	 */
	public String getFather();

	/**
	 * Gets the context attribute term domain.
	 *
	 * @return Context attribute term domain.
	 */
	public String getTermDomain();

	/**
	 * Gets the context attribute coll key.
	 *
	 * @return Context attribute coll key.
	 */
	public String getCollKey();

}
