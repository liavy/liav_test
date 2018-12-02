package com.sap.engine.services.webservices.espbase.client.dynamic.types.impl;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.client.dynamic.util.Util;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAttributeInfoItem;

public class DAttributeInfoItemImpl implements DAttributeInfoItem {
  
  private QName name;
  private String value;
  
  public QName getName() {
    return(name);
  }

  public void setName(QName name) {
    this.name = name;
  }

  public String getValue() {
    return(value);
  }

  public void setValue(String value) {
    this.value = value;
  }
  
  public String toString() {
    StringBuffer toStringBuffer = new StringBuffer();
    initToStringBuffer(toStringBuffer, "");
    return(toStringBuffer.toString());
  }
  
  public void initToStringBuffer(StringBuffer toStirngBuffer, String offset) {
    Util.initToStringBuffer_AppendOffsetAndId(toStirngBuffer, offset, "DAttributeInfoItem");
    Util.initToStringBuffer_ObjectValue(toStirngBuffer, offset + Util.TO_STRING_OFFSET, "name : ", name);
    Util.initToStringBuffer_ObjectValue(toStirngBuffer, offset + Util.TO_STRING_OFFSET, "value : ", value);
  }
}
