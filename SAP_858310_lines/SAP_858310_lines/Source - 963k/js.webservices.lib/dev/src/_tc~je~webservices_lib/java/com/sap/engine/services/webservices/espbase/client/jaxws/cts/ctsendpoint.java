package com.sap.engine.services.webservices.espbase.client.jaxws.cts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointReference;
import javax.xml.transform.Source;
import javax.xml.ws.Binding;

import org.w3c.dom.Element;

public class CTSEndpoint extends Endpoint {
  
  private Executor executor;  
  private final Binding binding;
  private final Object implementor;
  private Map<String, Object> properties;
  private List<Source> metadata;
  
  
  public CTSEndpoint(String bindingId, Object impl)
  {
      implementor = impl;
      binding = new CTSSoapBinding();
  }

  public Binding getBinding()
  {
      return binding;
  }

  public Object getImplementor()
  {
      return implementor;
  }  
  
    
  public Executor getExecutor() {
    return executor;
  }


  public List<Source> getMetadata() {    
    return (metadata!=null)? metadata: new ArrayList<Source>();
  }

  public Map<String, Object> getProperties() {
    return (properties!=null)? properties: new HashMap<String, Object>();
  }

  public boolean isPublished() {
    throw new RuntimeException("Exception should be thrown by the spec.");
  }

  public void publish(String arg0) {
    throw new RuntimeException("Exception should be thrown by the spec.");    
  }

  public void publish(Object arg0) {
    throw new RuntimeException("Exception should be thrown by the spec.");    
  }

  public void setExecutor(Executor arg0) {
    this.executor = arg0;
  }

  public void setMetadata(List<Source> arg0) {
    this.metadata = arg0;
  }

  public void setProperties(Map<String, Object> arg0) {
    this.properties = arg0;
  }

  public void stop() {    
    throw new RuntimeException("Exception should be thrown by the spec.");
  }

  @Override
  public EndpointReference getEndpointReference(Element... referenceParameters) {
    throw new RuntimeException("Method not supported");
//    return null;
  }

  @Override
  public <T extends EndpointReference> T getEndpointReference(Class<T> clazz, Element... referenceParameters) {
    throw new RuntimeException("Method not supported");
//    return null;
  }

  
}
