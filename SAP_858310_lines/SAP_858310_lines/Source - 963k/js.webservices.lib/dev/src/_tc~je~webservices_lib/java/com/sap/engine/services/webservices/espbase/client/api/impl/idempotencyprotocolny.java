package com.sap.engine.services.webservices.espbase.client.api.impl;

import java.io.IOException;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.ConsumerProtocol;
import com.sap.engine.interfaces.webservices.esp.ProtocolExtensions;
import com.sap.engine.interfaces.webservices.runtime.MessageException;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriter;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.SOAPTransportBinding;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;

public class IdempotencyProtocolNY implements ConsumerProtocol, ProtocolExtensions {
  
  public static final String NAME = "IdempotencyProtocol";
  
  public int afterDeserialization(ConfigurationContext ctx) throws ProtocolException, MessageException {
    return(CONTINUE);
  }

  public void beforeSerialization(ConfigurationContext ctx) throws ProtocolException {
    ClientConfigurationContext clienCfgCtx = (ClientConfigurationContext)ctx;
    if(PublicProperties.isOperationIdempotencyActive(clienCfgCtx)) {
      SOAPMessage soapMsg = (SOAPMessage)(clienCfgCtx.getMessage());
      XMLTokenWriter soapMsgBodyWriter = soapMsg.getBodyWriter();
      XMLIdempotencyTokenWriter idempotencyWriter = new XMLIdempotencyTokenWriter(); 
      idempotencyWriter.setWriter(soapMsgBodyWriter);
      idempotencyWriter.setSoapBodyElementNS(SOAPTransportBinding.getSOAPNS(clienCfgCtx));
      idempotencyWriter.setUUID(PublicProperties.getIdempotencyUUID(clienCfgCtx));
      try {
        soapMsg.replaceBodyWriter(idempotencyWriter);
      } catch(IOException ioExc) {
        throw new ProtocolException(ioExc);
      }
    }
  }
  
  public String getProtocolName() {
    return(NAME);
  }
  
  public int handleRequest(ConfigurationContext ctx) throws ProtocolException, MessageException {
    return(CONTINUE);
  }
  
  public int handleResponse(ConfigurationContext configCtx) throws ProtocolException {
    return(CONTINUE);
  }

  public int handleFault(ConfigurationContext ctx) throws ProtocolException {
    return(CONTINUE);
  }

  public void beforeHibernation(ConfigurationContext configCtx) throws ProtocolException {
  }

  public void afterHibernation(ConfigurationContext configCtx) throws ProtocolException {
  }
  
  public void finishMessageDeserialization(ConfigurationContext configCtx) throws ProtocolException {
  }

  public void finishHibernation(ConfigurationContext configCtx) throws ProtocolException {
  }
}
