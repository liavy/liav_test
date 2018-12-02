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

import java.util.HashMap;

import com.sap.engine.interfaces.textcontainer.recipient.TextContainerRecipientListener;

/**
*
* Interface for recipients of texts for the TextContainer.
*
* <br/><br/>Copyright (c) 2006, SAP AG
* @author  Thomas Goering
* @version 1.0
* @deprecated use {@link TextContainerRecipientListener}
*/
public interface TextContainerRecipient
{

	final static String TCR_PCD = "PCD"; //$NON-NLS-1$

	final static String TCR_WEB_DYNPRO = "WD"; //$NON-NLS-1$

	/**
	 * All bundles that have just been deployed.
	 * 
	 * @param bundles
	 * 			  A hash map of component names with each entry containing an array of bundle names.
	 */
	public void deployedBundles(HashMap<String, String[]> bundles);

}
