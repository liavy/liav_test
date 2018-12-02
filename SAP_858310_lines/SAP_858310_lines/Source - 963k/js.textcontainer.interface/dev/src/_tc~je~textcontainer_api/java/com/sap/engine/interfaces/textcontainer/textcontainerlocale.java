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
 * Interface for administrating the context attribute locale for the TextContainer.
 *
 * <br/><br/>Copyright (c) 2006, SAP AG
 * @author  Thomas Goering
 * @version 1.0
 */
public interface TextContainerLocale
{

	/**
	 * Gets the context attribute start locale.
	 *
	 * @return Context attribute start locale.
	 */
	public String getStartLocale();

	/**
	 * Gets the context attribute sequence number.
	 *
	 * @return Context attribute sequence number.
	 */
	public int getSequenceNumber();

	/**
	 * Gets the context attribute term locale.
	 *
	 * @return Context attribute term locale.
	 */
	public String getLocale();

}
