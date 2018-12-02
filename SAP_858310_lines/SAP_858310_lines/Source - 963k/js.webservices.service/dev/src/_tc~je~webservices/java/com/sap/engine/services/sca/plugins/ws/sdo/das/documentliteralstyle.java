package com.sap.engine.services.sca.plugins.ws.sdo.das;

import java.util.List;

import javax.xml.namespace.QName;

import org.xmlsoap.schemas.wsdl.TMessage;
import org.xmlsoap.schemas.wsdl.TOperation;
import org.xmlsoap.schemas.wsdl.TPart;

import com.sap.engine.interfaces.sca.wire.Operation;
import com.sap.engine.interfaces.sca.wire.Parameter;
import commonj.sdo.Property;
import commonj.sdo.Type;
import commonj.sdo.helper.HelperContext;


public class DocumentLiteralStyle implements WebServiceStyle {
  
  private final WebServiceInvocationMetaData _factory;
  
  public DocumentLiteralStyle(WebServiceInvocationMetaData factory) {
    _factory = factory;
  }
  
  public void setResult(Operation m, TOperation op, TMessage message, HelperContext ctx) {
    createParamsFromParts(message, m.getResults(),ctx);
  }
  
  public void setArguments(Operation m, TOperation op, TMessage message, HelperContext ctx) {
    createParamsFromParts(message, m.getParameters(),ctx);	
  }
  
  private void createParamsFromParts(TMessage message, List<Parameter> list, HelperContext ctx) {
    if (message == null)
      return;
    
    for (TPart tpart: message.getPart()) {
      Parameter p = (Parameter)_factory.getContainerDataFactory().create(Parameter.class);
      list.add(p);
      p.setName(tpart.getName());
      QName unp = tpart.getElement();
      
      if (unp == null) {
	p.setTypeUri(tpart.getTPartType());
	continue;
      }
      
      p.setPropertyUri(unp);
      Property wrapperProp = _factory.getTypeHelper().getOpenContentProperty(unp.getNamespaceURI(), unp.getLocalPart());
      if (wrapperProp == null) {
	throw new RuntimeException("Could not find global property: "+tpart.getElement());
      }
      
      p.setTypeUri(new QName(wrapperProp.getType().getURI(), wrapperProp.getType().getName()));
    }
  }
  
  public Type getFaultType(Type type, TMessage message) {
    return type;
  }
}
