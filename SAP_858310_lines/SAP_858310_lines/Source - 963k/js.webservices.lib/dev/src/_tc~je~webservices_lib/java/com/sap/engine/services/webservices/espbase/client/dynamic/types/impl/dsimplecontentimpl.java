package com.sap.engine.services.webservices.espbase.client.dynamic.types.impl;

import com.sap.engine.services.webservices.espbase.client.dynamic.util.Util;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DSimpleContent;

public class DSimpleContentImpl extends DFieldImpl implements DSimpleContent {
  
  protected DSimpleContentImpl() {
    super();
  }

  public String toString() {
    StringBuffer toStringBuffer = new StringBuffer();
    initToStringBuffer(toStringBuffer, "");
    return(toStringBuffer.toString());
  }
  
  public void initToStringBuffer(StringBuffer toStringBuffer, String offset) {
    Util.initToStringBuffer_AppendOffsetAndId(toStringBuffer, offset, "DSimpleContent");
    initToStringBuffer_DField(toStringBuffer, offset + Util.TO_STRING_OFFSET);
  }
}
