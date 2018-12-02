package com.sap.engine.services.sca.plugins.ws.sdo.das;

import java.util.List;

import javax.xml.namespace.QName;

import com.sap.engine.services.sca.plugins.ws.sdo.das.AbstractWsdlProvider.Direction;

import commonj.sdo.Type;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.TypeHelper;
import commonj.sdo.helper.XMLHelper;
import commonj.sdo.helper.XSDHelper;

public interface WebServiceInvocationMetaData {
  
  public static class MessageDescr {
    public Object typeOrProperty;
    public String name;
  }
  
  String getServiceLocation();
  List<MessageDescr> getPropertiesOrTypes(String name, Direction dir, String faultName);
  String getSoapAction(String name);
  WebServiceStyle getStyle(String name, Direction dir, String faultName);	
  String getExceptionFromDetailType(String operationName, Type type);
  QName getServiceName();	
  void loadService();
  QName getPortName();
  TypeHelper getTypeHelper();
  DataFactory getDataFactory();
  XMLHelper getXmlHelper();
  XSDHelper getXsdHelper();
  DataFactory getContainerDataFactory();
}
