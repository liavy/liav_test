package com.sap.engine.services.webservices.espbase.server.additions.xi;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.espbase.server.runtime.ApplicationWebServiceContextImpl;
import com.sap.engine.services.webservices.espbase.xi.XIFrameworkConstants;

public class ProviderXIMessageContext {
  
  private static final ProviderXIMessageContext singleton = new ProviderXIMessageContext();
  
  private ProviderXIMessageContext() {
  }
  
  public static ProviderXIMessageContext getInstance() {
    return(singleton);
  }
  
  public Object getProperty(String key) {
    ProviderContextHelper providerContext = (ProviderContextHelper)(ApplicationWebServiceContextImpl.getSingleton().getConfigurationContext());
    return(PublicProperties.getDynamicProperty(key, providerContext));
  }
  
  public String getSenderPartyName() {
    return((String)getProperty(PublicProperties.XI_SENDER_PARTY_NAME_RT_PROP_NAME.toString()));
  }
  
  public String getApplicationAckRequested() {
    return((String)getProperty(PublicProperties.XI_APP_ACK_REQUESTED_RT_PROP_NAME.toString()));
  }
  
  public String getSystemAckRequested() {
    return((String)getProperty(PublicProperties.XI_SYS_ACK_REQUESTED_RT_PROP_NAME.toString()));
  }
  
  public String getApplicationErrorAckRequested() {
    return((String)getProperty(PublicProperties.XI_APP_NEGATIVE_ACK_REQUESTED_RT_PROP_NAME.toString()));
  }

  public String getSystemErrorAckRequested() {
    return((String)getProperty(PublicProperties.XI_SYS_NEGATIVE_ACK_REQUESTED_RT_PROP_NAME.toString()));
  }
  
  public String getSenderService() {
    return((String)getProperty(PublicProperties.XI_SENDER_SERVICE_RT_PROP_NAME.toString()));
  }
  
  public String getQueueId() {
    return((String)getProperty(PublicProperties.XI_QUEUE_ID_RT_PROP_NAME.toString()));
  }
  
  public QName getServiceInterfaceName() {
    return((QName)getProperty(PublicProperties.XI_SERVICE_INTERFACE_NAME_RT_PROP_NAME.toString()));
  }
  
  public boolean isAsync() {
    return(((Boolean)getProperty(PublicProperties.XI_IS_ASYNC_RT_PROP_NAME.toString())).booleanValue());
  }
}
