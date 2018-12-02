package com.sap.engine.services.webservices.additions.client.dsr;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.ConsumerProtocol;
import com.sap.engine.interfaces.webservices.runtime.MessageException;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.services.webservices.espbase.client.api.impl.HTTPControlInterfaceNYImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.guid.GUID;
import com.sap.tc.logging.Location;
import com.sap.jdsr.shared.ws.DSRWsClientHandler;



public class DSRConsumerProtocol implements ConsumerProtocol {

	public static final String PROTOCOL = "PassportProtocol";
	public static final String COMP_NAME = "Web Service";
	
	private static final String NOT_AVAILABLE = "n.a."; 
	private static final String PASSPORT_PROTOCOL_HEADER_NAME = "SAP-PASSPORT";
		
    public String getProtocolName() {
		return(PROTOCOL);
    }
    
    public int handleRequest(ConfigurationContext context) throws ProtocolException, MessageException {
    	ClientConfigurationContext clientCfgCtx = (ClientConfigurationContext)context;
	    
    	String endpointURL = PublicProperties.getEndpointURL(clientCfgCtx);		
		String hexStrPassport = DSRWsClientHandler.wsCallStarted(endpointURL, clientCfgCtx.getServiceContext().getServiceName().getLocalPart());
		if(hexStrPassport == null) {
			return(CONTINUE);
		}
		setPassportHeader(clientCfgCtx, hexStrPassport);
    
	    return(CONTINUE);
    }
    
    private void setPassportHeader(ClientConfigurationContext clientCfgCtx, String hexStrPassport) {
    	HTTPControlInterfaceNYImpl httpControl = new HTTPControlInterfaceNYImpl(clientCfgCtx);
    	httpControl.addRequesHeader(PASSPORT_PROTOCOL_HEADER_NAME, hexStrPassport);
    }
   
    public int handleResponse(ConfigurationContext context) throws ProtocolException {
      DSRWsClientHandler.wsCallFinished(0, 0);
    	return(CONTINUE);
    }

    public int handleFault(ConfigurationContext context) throws ProtocolException {
    	return(CONTINUE);
    }
}
