﻿package com.sap.engine.services.webservices.espbase.client.dynamic.types.impl;

import com.sap.engine.services.webservices.espbase.client.dynamic.util.Util;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAppInfo;

public class DAppInfoImpl extends DAnnotationItemImpl implements DAppInfo {
  
  public String toString() {
    StringBuffer toStringBuffer = new StringBuffer();
    initToStringBuffer(toStringBuffer, "");
    return(toStringBuffer.toString());
  }
  
  public void initToStringBuffer(StringBuffer toStringBuffer, String offset) {
    Util.initToStringBuffer_AppendOffsetAndId(toStringBuffer, offset, "DAppInfo");
    initToStringBuffer_DAnnotationItem(toStringBuffer, offset + Util.TO_STRING_OFFSET);
  }

}