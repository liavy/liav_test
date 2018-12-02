package com.sap.engine.services.webservices.espbase.client.dynamic.types.impl;

import com.sap.engine.services.webservices.espbase.client.dynamic.util.Util;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAnnotateable;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAnnotation;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAttributeInfoItem;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAttributesMap;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DInfoItem;

public abstract class DInfoItemImpl extends DFieldImpl implements  DInfoItem, DAnnotateable, DAttributesMap {
  
  protected DAnnotation dAnnotation;
  protected DAnnotation dTopAnnotation;
  protected DAttributeInfoItem[] attribInfoItems;
  protected String defaultValue;
   
  
  protected DInfoItemImpl() {
    super();
    attribInfoItems = new DAttributeInfoItem[0];
  }
  
  public DAnnotation getAnnotation() {
    return(dAnnotation);
  }
  
  public void setAnnotation(DAnnotation dAnnotation) {
    this.dAnnotation = dAnnotation;
  }
  
  public DAnnotation getTopLevelAnnotation() {
    return this.dTopAnnotation;
  }
  
  public void setTopLevelAnnotation(DAnnotation dAnnotation) {
    this.dTopAnnotation = dAnnotation;
  }
  
  public DAttributeInfoItem[] getAttributeInfoItems() {
    return(attribInfoItems);
  }
  
  public void setAttributeInfoItems(DAttributeInfoItem[] attribInfoItems) {
    this.attribInfoItems = attribInfoItems;
  }

  public String getDefaultValue() {
    return(defaultValue);
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }
  
  protected void initToStringBuffer_DInfoItem(StringBuffer toStringBuffer, String offset) {
    initToStringBuffer_DField(toStringBuffer, offset);
    Util.initToStringBuffer_DAnnotationable(toStringBuffer, offset, this);
    Util.initToStringBuffer_DAttributesMap(toStringBuffer, offset, this);
    Util.initToStringBuffer_ObjectValue(toStringBuffer, offset + Util.TO_STRING_OFFSET, "default value : ", defaultValue);
  }
}
