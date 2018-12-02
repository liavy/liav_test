/*
 * Created on May 9, 2007
 */
package com.sap.engine.services.textcontainer.api;

import java.util.ArrayList;

import com.sap.engine.interfaces.textcontainer.recipient.TextContainerRecipientTextsUpdatedEvent;


/**
 * @author d029702
 */
public class TextContainerRecipientTextsUpdatedEventImpl implements TextContainerRecipientTextsUpdatedEvent
{

	public TextContainerRecipientTextsUpdatedEventImpl(String component)
	{
		this.component = component;
		this.baseNames = new ArrayList<String>();
	}

	public String getComponent()
	{
		return component;
	}

	public String[] getBaseNames()
	{
		return (String[]) baseNames.toArray(new String[0]);
	}

	public void addBaseName(String baseName)
	{
		baseNames.add(baseName);
	}

	protected String component;
	protected ArrayList<String> baseNames;

}
