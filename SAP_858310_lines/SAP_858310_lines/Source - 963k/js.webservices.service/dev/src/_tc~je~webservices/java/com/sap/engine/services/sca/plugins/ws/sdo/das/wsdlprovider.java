package com.sap.engine.services.sca.plugins.ws.sdo.das;

import java.util.List;
import java.util.Set;

import org.xmlsoap.schemas.wsdl.TDefinitions;
import org.xmlsoap.schemas.wsdl.TOperation;
import org.xmlsoap.schemas.wsdl.TPortType;

import com.sap.engine.interfaces.sca.wire.NamedInterfaceMetadata;

import commonj.sdo.Property;
import commonj.sdo.Type;
import commonj.sdo.helper.HelperContext;

public interface WsdlProvider extends WebServiceInvocationMetaData {
  TDefinitions getWsdl();
  TPortType getPortType();
  Set<FaultDescription> getFaults(TOperation op);	
  NamedInterfaceMetadata getInterfaceMetaData();	
  List<Type> getTypes();	
  List<Property> getProperties();	
  HelperContext getHelperContext();
}
