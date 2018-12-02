package com.sap.engine.services.webservices.espbase.client.dynamic.types.impl;

import com.sap.engine.services.webservices.espbase.client.dynamic.util.Util;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAttribute;

public class DAttributeImpl extends DInfoItemImpl implements DAttribute {

  private boolean required;
  
  protected DAttributeImpl() {
    super();
    required = false;
  }
  
  public boolean isRequired() {
  	return required;
  }
  
  public void setRequired(boolean required) {
  	this.required = required;
  }
  
  public String toString() {
    StringBuffer toStringBuffer = new StringBuffer();
    initToStringBuffer(toStringBuffer, "");
    return(toStringBuffer.toString());
  }
  
  public void initToStringBuffer(StringBuffer toStringBuffer, String offset) {
    Util.initToStringBuffer_AppendOffsetAndId(toStringBuffer, offset, "DAttribute");
    initToStringBuffer_DInfoItem(toStringBuffer, offset + Util.TO_STRING_OFFSET);
    Util.initToStringBuffer_BooleanValue(toStringBuffer, offset + Util.TO_STRING_OFFSET, "is required : ", required);
  }
}
