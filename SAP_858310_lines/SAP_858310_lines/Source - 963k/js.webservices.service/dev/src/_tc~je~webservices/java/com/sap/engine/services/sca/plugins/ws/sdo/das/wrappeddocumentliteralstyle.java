package com.sap.engine.services.sca.plugins.ws.sdo.das;

import javax.xml.namespace.QName;

import org.xmlsoap.schemas.wsdl.TMessage;
import org.xmlsoap.schemas.wsdl.TOperation;
import org.xmlsoap.schemas.wsdl.TPart;

import com.sap.engine.interfaces.sca.wire.Operation;
import com.sap.engine.interfaces.sca.wire.Parameter;
import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;
import commonj.sdo.helper.HelperContext;

public class WrappedDocumentLiteralStyle implements WebServiceStyle {
  
  private final WebServiceInvocationMetaData _factory;
  
  public WrappedDocumentLiteralStyle(WebServiceInvocationMetaData factory) {
    _factory = factory;
  }
  
  public void setResult(Operation m, TOperation op, TMessage message, HelperContext ctx) {
    Property prop = getWrapperProperty(message);
    m.setOutputWrapperProperty(new QName(prop.getContainingType().getURI(), prop.getName()));
    Type outputWrapper = prop.getType();
    
    if (outputWrapper.getProperties().size() > 1) {
      Parameter p = (Parameter)((DataObject)m).createDataObject("results");
      p.setTypeUri(new QName(outputWrapper.getURI(), outputWrapper.getName()));
    } else if (outputWrapper.getProperties().size() > 0) {
      Property wrapperProp = (Property)outputWrapper.getProperties().get(0);
      Parameter p = (Parameter)((DataObject)m).createDataObject("results");
      p.setTypeUri(new QName(wrapperProp.getType().getURI(), wrapperProp.getType().getName()));
      p.setName(wrapperProp.getName());
      p.setMultivalued(wrapperProp.isMany());
    }	
  }
  
  public void setArguments(Operation m, TOperation op, TMessage message, HelperContext ctx) {
    if (message.getPart().size() != 1) {
      throw new RuntimeException("Too many parts");
    }
    
    Property wrapper = getWrapperProperty(message);
    m.setInputWrapperProperty(new QName(wrapper.getContainingType().getURI(), wrapper.getName()));
    m.setInputWrapped(false);
    Type wrapperType = wrapper.getType();

    for (int i=0; i<wrapperType.getProperties().size(); i++) {
      Property prop = (Property)wrapperType.getProperties().get(i);
      Parameter p = (Parameter)((DataObject)m).createDataObject("parameters");
      p.setName(prop.getName());
      p.setTypeUri(new QName(prop.getType().getURI(), prop.getType().getName()));
      p.setMultivalued(prop.isMany());
    }
  }
  
  private Property getWrapperProperty(TMessage message) {
    TPart tpart = message.getPart().get(0);
    QName unp = tpart.getElement();
    Property wrapperProp = _factory.getTypeHelper().getOpenContentProperty(unp.getNamespaceURI(), unp.getLocalPart());
    if (wrapperProp == null) {
      throw new RuntimeException("Could not find global property: "+tpart.getElement());
    }
    
    return wrapperProp;
  }
  
  public Type getFaultType(Type type, TMessage message) {
    return type;
  }
}
