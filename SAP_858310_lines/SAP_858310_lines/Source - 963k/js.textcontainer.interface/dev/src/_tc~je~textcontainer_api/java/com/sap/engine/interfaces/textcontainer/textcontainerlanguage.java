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
 * Interface for administrating the language values for the TextContainer.
 *
 * <br/><br/>Copyright (c) 2006, SAP AG
 * @author  Thomas Goering
 * @version 1.0
 */
public interface TextContainerLanguage
{

	/**
	 * Gets the locale.
	 *
	 * @return Locale.
	 */
	public String getLocale();

	/**
	 * Gets the flag if this language is the secondary locale.
	 *
	 * @return Flag.
	 */
	public boolean getIsSecondaryLocale();

}
