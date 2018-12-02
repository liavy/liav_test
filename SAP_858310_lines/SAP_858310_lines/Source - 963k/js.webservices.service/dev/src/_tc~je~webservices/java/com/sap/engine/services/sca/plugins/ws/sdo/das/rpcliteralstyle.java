package com.sap.engine.services.sca.plugins.ws.sdo.das;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.xmlsoap.schemas.wsdl.TMessage;
import org.xmlsoap.schemas.wsdl.TOperation;
import org.xmlsoap.schemas.wsdl.TPart;

import com.sap.engine.interfaces.sca.wire.Operation;
import com.sap.engine.interfaces.sca.wire.Parameter;
import com.sap.sdo.api.helper.SapXsdHelper;
import com.sap.sdo.api.types.SapType;
import com.sap.sdo.api.util.URINamePair;
import com.sap.engine.services.sca.plugins.ws.sdo.das.AbstractWsdlProvider.Direction;
import com.sap.engine.services.sca.plugins.ws.sdo.das.WebServiceInvocationMetaData.MessageDescr;

import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;
import commonj.sdo.helper.HelperContext;


public class RpcLiteralStyle implements WebServiceStyle {
  
  static class ParameterInfo {
    QName propUri;
    QName typeUri;
    String name;
  }
  
  private final List<ParameterInfo> _params;
  
  public RpcLiteralStyle(WebServiceInvocationMetaData factory, String uri, String opName, Direction dir, String faultName) {
    _params = new ArrayList<ParameterInfo>();
    List<MessageDescr> types = factory.getPropertiesOrTypes(opName, dir, faultName);
    for (MessageDescr d: types) {
      ParameterInfo descr = new ParameterInfo();
      descr.name = d.name;
      _params.add(descr);
      
      if (d.typeOrProperty instanceof Property) {
	Property p = (Property)d.typeOrProperty;
	descr.propUri = new QName(p.getContainingType().getURI(), p.getName());
	descr.typeUri = new QName(p.getType().getURI(),p.getType().getName());
      } else {
	Type t = (Type)d.typeOrProperty;
	descr.typeUri = new QName(t.getURI(), t.getName());
      }
    }
  }
  
  public void setResult(Operation m, TOperation op, TMessage message, HelperContext ctx) {
    for (TPart part: message.getPart()) {
      Parameter p = (Parameter)((DataObject)m).createDataObject("results");
      p.setName(part.getName());
      URINamePair unp;
      
      if (part.getTPartType() != null) {
	unp = new URINamePair(part.getTPartType());
      } else {
	p.setPropertyUri(part.getElement());
	Property prop = ctx.getTypeHelper().getOpenContentProperty(part.getElement().getNamespaceURI(), part.getElement().getLocalPart());
	if (prop != null) {
	  unp = ((SapType)prop.getType()).getQName();
	} else {
	  throw new RuntimeException("could not find OC property");
	}
      }
      
      if (URINamePair.SCHEMA_URI.equals(unp.getURI())) {
	unp = ((SapXsdHelper)SapXsdHelper.INSTANCE).getSdoName(unp);
      }
      
      p.setTypeUri(new QName(unp.getURI(), unp.getName()));
    }
  }
  
  public void setArguments(Operation m, TOperation op, TMessage message, HelperContext ctx) {
    for (ParameterInfo p: _params) {
      Parameter descr = (Parameter)((DataObject)m).createDataObject("parameters");
      descr.setName(p.name);
      descr.setTypeUri(p.typeUri);
      
      if (p.propUri != null) {
	descr.setPropertyUri(p.propUri);
      }
      descr.setMultivalued(false);
    }
  }
  
  public DataObject getFault(DataObject faultDetail) {
    return faultDetail.getDataObject(0);
  }
  
  public Type getFaultType(Type type, TMessage message) {
    return type;
  }
}
