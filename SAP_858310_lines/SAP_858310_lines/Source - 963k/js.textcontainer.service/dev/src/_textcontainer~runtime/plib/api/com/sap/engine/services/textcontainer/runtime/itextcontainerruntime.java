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

package com.sap.engine.services.textcontainer.runtime;

import java.util.Hashtable;
import java.util.Locale;

/**
*
* Interface for internal uasge only!
*
* <br/><br/>Copyright (c) 2006, SAP AG
* @author  Thomas Goering
* @version 1.0
*/
public interface ITextContainerRuntime {

	public Hashtable getTexts(String baseName, Locale locale, Class cls);

	public Hashtable getTexts(String baseName, Locale locale, ClassLoader loader);

	public Hashtable getTexts(String baseName, Locale locale, String componentName);

}
