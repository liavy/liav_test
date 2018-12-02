/*
 * Created on Apr 13, 2006
 */
package com.sap.engine.services.textcontainer.admin;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.JCoTable;

/**
 * @author d029702
 */
public class TXVRemote {

	public TXVRemote(String destination) throws Exception
	{
		this.destination = JCoDestinationManager.getDestination(destination);
	}

	public JCoTable retrieveIndustryValues() throws JCoException
	{
		JCoFunction function = callRemoteFunction("TXV_GET_INDUSTRY_VALUES");

		JCoParameterList pl = function.getTableParameterList();
		return pl.getTable("INDUSTRY_VALUES");
	}

	public JCoTable retrieveRegionValues() throws JCoException
	{
		JCoFunction function = callRemoteFunction("TXV_GET_REGION_VALUES");

		JCoParameterList pl = function.getTableParameterList();
		return pl.getTable("REGION_VALUES");
	}

	public JCoTable retrieveExtensionValues() throws JCoException
	{
		JCoFunction function = callRemoteFunction("TXV_GET_EXTENSION_VALUES");

		JCoParameterList pl = function.getTableParameterList();
		return pl.getTable("EXTENSION_VALUES");
	}

	public JCoFunction retrieveLanguageValues() throws JCoException
	{
		return callRemoteFunction("TXV_GET_LOCALE_ENVIRONMENT");
	}

	public JCoTable retrieveLocaleValues() throws JCoException
	{
		JCoFunction function = callRemoteFunction("TXV_GET_LOCALE_CHAINS");

		JCoParameterList pl = function.getTableParameterList();
		return pl.getTable("T_LOCALE_CHAIN");
	}

	public JCoFunction retrieveSystemContext() throws JCoException
	{
		return callRemoteFunction("TXV_GET_SYSTEM_CONTEXT");
	}

	protected JCoFunction callRemoteFunction(String functionName) throws JCoException
	{
		JCoRepository repository = destination.getRepository();

		JCoFunction function = repository.getFunction(functionName);

		function.execute(destination);

		return function;
	}

	protected JCoDestination destination;
}
