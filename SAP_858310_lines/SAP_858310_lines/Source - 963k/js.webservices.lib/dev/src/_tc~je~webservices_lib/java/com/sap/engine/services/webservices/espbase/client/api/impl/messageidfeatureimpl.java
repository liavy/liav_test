package com.sap.engine.services.webservices.espbase.client.api.impl;

import com.sap.engine.interfaces.webservices.client.ClientFeatureProvider;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.AbstractMessage;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ClientProtocolException;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.PropertyContext;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.MessageIdFeature;

/**
 * NY Implementation of message id feature.
 * @author i024072
 *
 */
public class MessageIdFeatureImpl implements MessageIdFeature {
  
  private ClientConfigurationContext clientContext;
  
  public MessageIdFeatureImpl(ClientConfigurationContext clientContext) {
    this.clientContext = clientContext;
  }
  
  public String getMessageId() {
    return (String) clientContext.getPersistableContext().getProperty(MessageIDProtocolNY.PERSISTABLE_GUID);   
  }

  public boolean handleFault(AbstractMessage message, PropertyContext context) throws ClientProtocolException {
    throw new UnsupportedOperationException("Such operation is invalid in JEE5 Environment.");
  }

  public boolean handleRequest(AbstractMessage message, PropertyContext context)
      throws ClientProtocolException {
    throw new UnsupportedOperationException("Such operation is invalid in JEE5 Environment.");
  }

  public boolean handleResponse(AbstractMessage message, PropertyContext context)
      throws ClientProtocolException {
    throw new UnsupportedOperationException("Such operation is invalid in JEE5 Environment.");
  }

  public void init(PropertyContext context) throws ClientProtocolException {
    throw new UnsupportedOperationException("Such operation is invalid in JEE5 Environment.");
  }

  public String[] getFeatures() {
    throw new UnsupportedOperationException("Such operation is invalid in JEE5 Environment.");
  }

  public String getName() {
    return MessageIDProtocolNY.NAME;        
  }

  public boolean isFeatureImplemented(String featureName, PropertyContext property) {
    throw new UnsupportedOperationException("Such operation is invalid in JEE5 Environment.");
  }

  public ClientFeatureProvider newInstance() {
    throw new UnsupportedOperationException("Such operation is invalid in JEE5 Environment.");
  }
}
