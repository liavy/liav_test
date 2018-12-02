package com.sap.engine.services.webservices.espbase.client.jaxws.core;

import java.util.Hashtable;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;

public class RequestContext extends Hashtable<String, Object> {
  
  private ClientConfigurationContext clientCfgCtx;
  
  protected RequestContext(ClientConfigurationContext clientCfgCtx) {
    this.clientCfgCtx = clientCfgCtx;
    String endpointAddress = PublicProperties.getEndpointURL(clientCfgCtx);
    put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
    setSessionMethodProperty();
  }
  
  private void setSessionMethodProperty() {
    PropertyType sessionMethodPropertyType = clientCfgCtx.getStaticContext().getRTConfig().getSinglePropertyList().getProperty(PublicProperties.C_SESSION_METHOD);
    Boolean sessionMaintainPropertyValue = sessionMethodPropertyType == null || !PublicProperties.F_SESSION_METHOD_HTTP.equals(sessionMethodPropertyType.get_value()) ? Boolean.FALSE : Boolean.TRUE;
    updateProperty(BindingProvider.SESSION_MAINTAIN_PROPERTY, sessionMaintainPropertyValue);
  }
  
  public Object put(String key, Object value) {
    super.put(key, value);
    updateProperty(key,value);
    return(value);
  }
  
  private void updateProperty(String key, Object value) {
    if (BindingProvider.ENDPOINT_ADDRESS_PROPERTY.equals(key)) {
      putPersistableContextProperty(PublicProperties.P_ENDPOINT_URL,value);
    } else if (BindingProvider.USERNAME_PROPERTY.equals(key)) {
      putPersistableContextProperty(PublicProperties.C_SEC_METHOD.toString(),PublicProperties.F_SEC_METHOD_BASIC);
      putPersistableContextProperty(PublicProperties.C_SEC_USERNAME.toString(),value);
    } else if (BindingProvider.PASSWORD_PROPERTY.equals(key)) {
      putPersistableContextProperty(PublicProperties.C_SEC_PASSWORD.toString(),value);
    } else if (BindingProvider.SESSION_MAINTAIN_PROPERTY.equals(key)) {
      if ("true".equalsIgnoreCase(value.toString())) {
        putPersistableContextProperty(PublicProperties.C_SESSION_METHOD.toString(),PublicProperties.F_SESSION_METHOD_HTTP);
      } else {
        putPersistableContextProperty(PublicProperties.C_SESSION_METHOD.toString(),PublicProperties.F_SESSION_METHOD_NONE);
      }      
    } else if (BindingProvider.SOAPACTION_USE_PROPERTY.equals(key)) {
      //TODO should be implemented!
    } else if (BindingProvider.SOAPACTION_URI_PROPERTY.equals(key)) {
      //TODO should be implemented!
    } else {
      putDynamicContextProperty(key, value);
    }        
  }
  
  public Object remove(Object key) {
    Object result = super.remove(key);  
    if (key instanceof String) {
      updateProperty((String) key,(Object) null);
    }
    return result;
  }
  
  public void putDynamicContextProperty(String key, Object value) {
    PublicProperties.setDynamicProperty(key, value, clientCfgCtx);
  }
  
  public void putPersistableContextProperty(String key, Object value) {
    if(!(value instanceof String)) {
      throw new WebServiceException("Context value type should be java.lang.String!");
    }
    PublicProperties.setPersistableProperty(key, (String)value, clientCfgCtx);
  }
}
