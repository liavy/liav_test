/*
 * Created on Mar 9, 2007
 */
package com.sap.engine.services.textcontainer.api;

import com.sap.engine.interfaces.textcontainer.recipient.TextContainerRecipientChangedEvent;


/**
 * @author d029702
 */
public class TextContainerRecipientChangedEventImpl implements TextContainerRecipientChangedEvent
{

	public TextContainerRecipientChangedEventImpl(int changed)
	{
		this.changed = changed;
	}

	public int getChanged()
	{
		return changed;
	}

	protected int changed;

}
