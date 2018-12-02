package com.sap.engine.services.webservices.espbase.xi.impl;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.messaging.ESPXIMessage;
import com.sap.engine.services.webservices.espbase.messaging.impl.ESPXIMessageImpl;
import com.sap.engine.services.webservices.espbase.server.runtime.RuntimeProcessingEnvironment;
import com.sap.engine.services.webservices.espbase.xi.ESPXIMessageProcessor;
import com.sap.engine.services.webservices.espbase.xi.exceptions.ESPXIException;

public class ESPXIMessageProcessorImpl implements ESPXIMessageProcessor {
  
  public static final String JNDI_BINDING_NAME = "wsContext/ESPXIMessageProcessor";
  
  private static final String RUNTIME_PROCESSING_ENV_JNDI_NAME = "wsContext/" + RuntimeProcessingEnvironment.JNDI_NAME;
  
  private RuntimeProcessingEnvironment runtimeProcessingEnv; 
  
  private RuntimeProcessingEnvironment determineRuntimeProcessingEnvironment() throws NamingException {
    if(runtimeProcessingEnv == null) {
      InitialContext initialCtx = new InitialContext(createInitialContextPropertis());
      runtimeProcessingEnv = (RuntimeProcessingEnvironment)(initialCtx.lookup(RUNTIME_PROCESSING_ENV_JNDI_NAME)); 
    }
    return(runtimeProcessingEnv);
  }
  
  public ESPXIMessage process(ESPXIMessage requestXIMessage) throws ESPXIException {
    if(requestXIMessage == null) {
      throw new ESPXIException("Request xi message is null!");
    }
    ESPXITransport xiTransport = createESPXITransport(requestXIMessage);
    try {
      RuntimeProcessingEnvironment runtimeProcessingEnv = determineRuntimeProcessingEnvironment();
      runtimeProcessingEnv.process(xiTransport);
    } catch(Exception exc) {
      throw new ESPXIException(exc);
    }
    Throwable serverError = xiTransport.getServerError();
    if(serverError != null) {
      throw new ESPXIException(serverError);
    }
    ESPXIMessage responseXIMessage = xiTransport.getResponseXIMessage();
    return(responseXIMessage);
  }
  
  private ESPXITransport createESPXITransport(ESPXIMessage requestXIMessage) throws ESPXIException {
    ESPXITransport xiTransport = new ESPXITransport(); 
    QName interfaceName = requestXIMessage.getServiceInterfaceName();
    if(interfaceName == null) {
      throw new ESPXIException("Request xi message interface name is null!");
    }
    xiTransport.setEntryPointID("/" + interfaceName.toString());
    xiTransport.setRequestXIMessage(requestXIMessage);
    xiTransport.setXIMessageProcessor(this);
    return(xiTransport);
  }
 
  public ESPXIMessage createMessage() {
    return(new ESPXIMessageImpl());
  }
  
  private Properties createInitialContextPropertis() {
    Properties initialCtxProps = new Properties();
    initialCtxProps.setProperty("domain", "true");
    return(initialCtxProps);
  }
}
