package com.sap.engine.services.webservices.espbase.client.dynamic.types.impl;

import com.sap.engine.services.webservices.espbase.client.dynamic.util.Util;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DElement;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DOccurrenceable;

public class DElementImpl extends DInfoItemImpl implements DElement, DOccurrenceable {

  private int minOccurs;
  private int maxOccurs;
  private boolean isNillable;
  
  protected DElementImpl() {
    super();
    minOccurs = 0;
    maxOccurs = 0;
    isNillable = false;
  }
  
  public int getMinOccurs() {
  	return minOccurs;
  }
  
  public int getMaxOccurs() {
  	return maxOccurs;
  }
  
  public boolean isNillable() {
  	return isNillable;
  }
  
  public void setNillable(boolean isNillable) {
  	this.isNillable = isNillable;
  }
  
  public void setMaxOccurs(int maxOccurs) {
  	this.maxOccurs = maxOccurs;
  }
  
  public void setMinOccurs(int minOccurs) {
  	this.minOccurs = minOccurs;
  }
  
  public String toString() {
    StringBuffer toStringBuffer = new StringBuffer();
    initToStringBuffer(toStringBuffer, "");
    return(toStringBuffer.toString());
  }
  
  public void initToStringBuffer(StringBuffer toStringBuffer, String offset) {
    Util.initToStringBuffer_AppendOffsetAndId(toStringBuffer, offset, "DElement");
    initToStringBuffer_DInfoItem(toStringBuffer, offset + Util.TO_STRING_OFFSET);
    Util.initToStringBuffer_DOccurrenceable(toStringBuffer, offset + Util.TO_STRING_OFFSET, this);
    Util.initToStringBuffer_BooleanValue(toStringBuffer, offset + Util.TO_STRING_OFFSET, "is nillable : ", isNillable);
  }
}
