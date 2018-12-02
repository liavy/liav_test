package com.sap.archtech.archconn;

import java.io.IOException;
import java.util.ArrayList;

import com.sap.archtech.archconn.response.AbstractArchResponseImpl;
import com.sap.archtech.archconn.values.ServiceInfo;

/**
 * ArchReponse implementation used for "local"
 * archiving commands. These commands are
 * processed entirely in the API, no communication
 * with the XML Data Archiving Service is needed.
 * For an application using this API a local
 * archiving command looks just like a normal
 * archiving command.
 * <p>
 * Users of the API get an ArchResponse by invoking ArchCommand.getResponse().
 * 
 * @author d025792
 */
public class LocalArchResponse extends AbstractArchResponseImpl 
{
	private final int statusCode;
	private final String errorMessage;
	private final String serviceMessage;
	private final String protocolMessage;

	public LocalArchResponse(ArchCommand archCommand, int statusCode, String errorMessage, String serviceMessage, String protocolMessage)
	{
		super(archCommand);
		this.statusCode = statusCode;
		this.errorMessage = errorMessage;
		this.serviceMessage = serviceMessage;
		this.protocolMessage = protocolMessage;
	}

	public int getStatusCode() throws IOException 
	{
		return statusCode;
	}

	public String getProtMessage() throws IOException 
	{
		return protocolMessage;
	}

	public String getServiceMessage() 
	{
		return serviceMessage;
	}

	public String getErrorMessage() throws IOException 
	{
		return errorMessage;
	}

	public ServiceInfo getServiceInfo() 
	{
		throw new UnsupportedOperationException("Local response only - cannot retrieve XML DAS metadata");
	}

	public ArrayList<String> getStringResult() throws IOException 
	{
		throw new UnsupportedOperationException("Local response only - cannot retrieve the XML DAS HTTP response stream");
	}

	public String getHeaderField(String hfield) 
	{
		throw new UnsupportedOperationException("Local response only - cannot retrieve the XML DAS HTTP response header fields");
	}
}
