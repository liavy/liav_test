package com.sap.engine.services.textcontainer.context;

import java.util.Hashtable;

import com.sap.engine.interfaces.textcontainer.context.TextContainerRules;

public class Rules implements TextContainerRules
{
	Hashtable<String, String> attrs;

	public Rules()
	{
		attrs = new Hashtable<String, String>();
	}

	public void setParameterValue(String pname, String pvalue)
	{
		attrs.put(pname, pvalue);
	}

	public String getParameterValue(String pname)
	{
		return (String) attrs.get(pname);
	}
}