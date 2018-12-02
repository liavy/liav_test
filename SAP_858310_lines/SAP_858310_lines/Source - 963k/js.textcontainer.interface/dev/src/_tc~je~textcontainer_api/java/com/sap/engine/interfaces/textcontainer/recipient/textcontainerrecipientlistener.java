/*
 * Copyright (c) 2007 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */

package com.sap.engine.interfaces.textcontainer.recipient;




/**
*
* Interface for Text Container recipient notification.
*
* <br/><br/>Copyright (c) 2007, SAP AG
* @author  Thomas Goering
* @version 1.0
*/
public interface TextContainerRecipientListener
{

	final public static String TCR_PCD = "PCD"; //$NON-NLS-1$

	final public static String TCR_WEB_DYNPRO = "WD"; //$NON-NLS-1$

	/**
	 * Event notification for recipients.
	 * 
	 * @param event
	 * 			  Can be one of the following sub-interfaces:
	 * <br>- {@link TextContainerRecipientDeploymentEvent} for deployment/undeployment events
	 * <br>- {@link TextContainerRecipientChangedEvent} for change events (e.g. system context has changed)
	 * <br>- {@link TextContainerRecipientTextsUpdatedEvent} to notify which texts have been updated (e.g. when the system context has changed)
	 * @throws TextContainerRecipientException If an error occured during the event processing.
	 */
	public void receive(TextContainerRecipientEvent event) throws TextContainerRecipientException;

}
