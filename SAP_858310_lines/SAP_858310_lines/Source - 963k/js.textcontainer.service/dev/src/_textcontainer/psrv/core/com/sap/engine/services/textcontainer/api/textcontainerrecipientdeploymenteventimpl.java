/*
 * Created on Mar 9, 2007
 */
package com.sap.engine.services.textcontainer.api;

import java.util.HashMap;

import com.sap.engine.interfaces.textcontainer.recipient.TextContainerRecipientDeploymentEvent;


/**
 * @author d029702
 */
public class TextContainerRecipientDeploymentEventImpl implements TextContainerRecipientDeploymentEvent
{

	public TextContainerRecipientDeploymentEventImpl(int action, String deployedComponent, HashMap<String, String[]> bundles)
	{
		this.action = action;
		this.deployedComponent = deployedComponent;
		this.bundles = bundles;
	}

	public int getAction()
	{
		return action;
	}

	public String getDeployedComponent()
	{
		return deployedComponent;
	}

	public HashMap<String, String[]> getBundles()
	{
		return bundles;
	}

	protected int action;
	protected String deployedComponent;
	protected HashMap<String, String[]> bundles;

}
