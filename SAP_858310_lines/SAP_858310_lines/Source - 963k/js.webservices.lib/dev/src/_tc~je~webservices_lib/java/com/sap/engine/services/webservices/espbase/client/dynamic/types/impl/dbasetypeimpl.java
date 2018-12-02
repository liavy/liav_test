package com.sap.engine.services.webservices.espbase.client.dynamic.types.impl;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.client.dynamic.util.Util;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAnnotateable;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAnnotation;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAttributeInfoItem;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DBaseType;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAttributesMap;

public abstract class DBaseTypeImpl implements DBaseType, DAnnotateable, DAttributesMap {

  protected QName baseTypeName;
  protected QName typeName;
  protected boolean isBuiltIn;
  protected boolean anonymous;
  protected int type;
  protected DAnnotation dAnnotation;
  protected DAnnotation dTopAnnotation;
  protected DAttributeInfoItem[] attribInfoItems;

  protected DBaseTypeImpl() {
    isBuiltIn = false;
    anonymous = false;
    attribInfoItems = new DAttributeInfoItem[0];
  }

  public DAnnotation getTopLevelAnnotation() {
    return this.dTopAnnotation;
  }
  
  public void setTopLevelAnnotation(DAnnotation dAnnotation) {
    this.dTopAnnotation = dAnnotation;
  }
  
  public void setAnonymous(boolean anonymous) {
    this.anonymous = anonymous;
  }

  public boolean isAnonymous() {
    return(anonymous);
  }

  public QName getBaseTypeName() {
    return(baseTypeName);
  }

  public QName getTypeName() {
    return(typeName);
  }

  public boolean isBuiltIn() {
    return(isBuiltIn);
  }

  public void setBaseTypeName(QName name) {
    baseTypeName = name;
  }

  public void setTypeName(QName name) {
    typeName = name;
  }

  public void setBuiltIn(boolean isBuiltIn) {
    this.isBuiltIn = isBuiltIn;
  }
  
  public abstract void setType(int type);
  
  public int getType() {
    return(type);
  }
  
  public String toString() {
    StringBuffer toStringBuffer = new StringBuffer();
    initToStringBuffer(toStringBuffer, "");
    return(toStringBuffer.toString());
  }
  
  public DAnnotation getAnnotation() {
    return(dAnnotation);
  }
  
  public void setAnnotation(DAnnotation dAnnotation) {
    this.dAnnotation = dAnnotation;
  }
  
  public DAttributeInfoItem[] getAttributeInfoItems() {
    return(attribInfoItems);
  }
  
  public void setAttributeInfoItems(DAttributeInfoItem[] attribInfoItems) {
    this.attribInfoItems = attribInfoItems;
  }
  
  public abstract void initToStringBuffer(StringBuffer toStringBuffer, String offset);
  
  protected void initToStringBuffer_DBaseType(StringBuffer toStringBuffer, String offset) {
    Util.initToStringBuffer_IntValue(toStringBuffer, offset, "type : ", type);
    Util.initToStringBuffer_ObjectValue(toStringBuffer, offset, "base type name : ", baseTypeName);
    Util.initToStringBuffer_ObjectValue(toStringBuffer, offset, "type name : ", typeName);
    Util.initToStringBuffer_BooleanValue(toStringBuffer, offset, "is builtin : ", isBuiltIn);
    Util.initToStringBuffer_BooleanValue(toStringBuffer, offset, "is anonimous : ", anonymous);
    Util.initToStringBuffer_DAnnotationable(toStringBuffer, offset, this);
    Util.initToStringBuffer_DAttributesMap(toStringBuffer, offset, this);
  }
}
