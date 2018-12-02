/*
 * Created on 2005-7-18
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.engine.services.webservices.espbase.client.dynamic.impl;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.client.dynamic.DParameter;
import com.sap.engine.services.webservices.espbase.client.dynamic.util.Util;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.jaxrpc.encoding.ExtendedTypeMapping;

/**
 * Representation of Dynamic Proxy api parameter.
 * @author I024072
 *
 */
public class DParameterImpl implements DParameter {
  
  private int type; // In,Out,Return etc.
  private String parameterName = null;
  private QName  parameterType = null;
  private Class  parameterClass = null;
  
  protected static QName determineParameterSchemaName(ParameterMapping parameterMapping, ExtendedTypeMapping typeMapping) {
    return(parameterMapping.isElement() ? typeMapping.getTypeForElement(parameterMapping.getSchemaQName()) : parameterMapping.getSchemaQName());
  }
  
  protected static String determineParameterName(ParameterMapping parameterMapping) {
    return(parameterMapping.isElement() ? parameterMapping.getSchemaQName().getLocalPart() : parameterMapping.getWSDLParameterName());
  }
  
  
  protected DParameterImpl(ParameterMapping parameterMapping, int type, ExtendedTypeMapping typeMapping) {
    this.type = type;
    this.parameterName = determineParameterName(parameterMapping);
    if (typeMapping != null) { // Default case 
      this.parameterType = determineParameterSchemaName(parameterMapping,typeMapping);
      this.parameterClass = typeMapping.getDefaultJavaClass(this.parameterType);
    } else {  // SDO Case
      this.parameterType = parameterMapping.getSchemaQName();
      this.parameterClass = Object.class;
    }
  }
  
  public int getParameterType() {
    return type;
  }
  
  public QName getSchemaName() {
    return(parameterType);
  }
  
  public String getName() {
    return(parameterName);
  }
    
  public Class getParameterClass() {
    return(parameterClass);
  }
    
  public String toString() {
    StringBuffer toStringBuffer = new StringBuffer();
    initToStringBuffer(toStringBuffer, "");
    return(toStringBuffer.toString());
  }
  
  protected void initToStringBuffer(StringBuffer toStringBuffer, String offset) {
    Util.initToStringBuffer_AppendOffsetAndId(toStringBuffer, offset, "Parameter");
    Util.initToStringBuffer_IntValue(toStringBuffer, offset + Util.TO_STRING_OFFSET, "parameter type : ", type);
    Util.initToStringBuffer_ObjectValue(toStringBuffer, offset + Util.TO_STRING_OFFSET, "schema name : ", getSchemaName());
    Util.initToStringBuffer_ObjectValue(toStringBuffer, offset + Util.TO_STRING_OFFSET, "name : ", getName());
    Util.initToStringBuffer_ObjectValue(toStringBuffer, offset + Util.TO_STRING_OFFSET, "parameter class : ", getParameterClass());
  }
}
