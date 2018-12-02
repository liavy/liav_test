package com.sap.engine.services.webservices.espbase.client.api.impl;

import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.api.XIManagementInterface;
import com.sap.engine.services.webservices.espbase.client.api.XIMessageContext;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.SOAPTransportBinding;
import com.sap.engine.services.webservices.espbase.client.jaxws.core.JAXWSProxy;
import com.sap.engine.services.webservices.espbase.client.jaxws.core.RequestContext;
import com.sap.engine.services.webservices.espbase.xi.ESPXIMessageProcessor;
import com.sap.engine.services.webservices.espbase.xi.XIFrameworkConstants;

public class XIManagementInterfaceImpl implements XIManagementInterface {
  
  private ClientConfigurationContext clientCfgCtx;
  private XIMessageContext requestXIMessageContext;
  
  public XIManagementInterfaceImpl(Object port) {
    clientCfgCtx = ((JAXWSProxy)port)._getConfigurationContext();
    requestXIMessageContext = new XIMessageContextImpl(clientCfgCtx); 
  }
  
  public void useXITransport(boolean useXICommunication) {
    PublicProperties.setUseXITransport(useXICommunication, clientCfgCtx);
  }
  
  public boolean getUseXITransport() {
    return(PublicProperties.getUseXITransport(clientCfgCtx));
  }
  
  public void setESPXIMessageProcessor(ESPXIMessageProcessor xiMessageProcessor) {
    PublicProperties.setESPXIMessageProcessor(xiMessageProcessor, clientCfgCtx);
  }
  
  public ESPXIMessageProcessor getESPXIMessageProcessor() throws NamingException {
    return(SOAPTransportBinding.getESPXIMessageProcessor(clientCfgCtx));
  }
  
  public XIMessageContext getRequestXIMessageContext() {
    return(requestXIMessageContext);
  }
}
