package com.sap.engine.services.webservices.additions.server.dsr;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.ProviderProtocol;
import com.sap.engine.interfaces.webservices.runtime.MessageException;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.tc.logging.Location;
import com.sap.jdsr.shared.ws.DSRWsServerHandler;

public class DSRProviderProtocol implements ProviderProtocol {
	
	public static final String PROTOCOL = "ProviderPassportProtocol";
	
	private static Location location = Location.getLocation(DSRProviderProtocol.class);
	
	public String getProtocolName() {
		return PROTOCOL;
	}

	public int handleFault(ConfigurationContext arg0) throws ProtocolException {
		location.debugT("Handle Fault DSR Server Protocol");
		return ProviderProtocol.CONTINUE;
	}

	public int handleRequest(ConfigurationContext ctx) throws ProtocolException, MessageException {
		location.debugT("Handle Request DSR Server Protocol");
	    //fill DSR context
		String wsName =((ProviderContextHelper)ctx).getStaticContext().getWebServiceName();

		DSRWsServerHandler.webServiceCallStarted(wsName);
		return ProviderProtocol.CONTINUE;
	}

	public int handleResponse(ConfigurationContext arg0) throws ProtocolException {
		location.debugT("Handle Response DSR Server Protocol");
		return ProviderProtocol.CONTINUE;
	}

}
