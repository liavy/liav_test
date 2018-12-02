package com.sap.engine.services.webservices.espbase.client.dynamic.types.impl;

import com.sap.engine.services.webservices.espbase.client.dynamic.util.Util;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DDocumentation;

public class DDocumentationImpl extends DAnnotationItemImpl implements DDocumentation {
  
  private String language;

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }
  
  public String toString() {
    StringBuffer toStringBuffer = new StringBuffer();
    initToStringBuffer(toStringBuffer, "");
    return(toStringBuffer.toString());
  }
  
  public void initToStringBuffer(StringBuffer toStringBuffer, String offset) {
    Util.initToStringBuffer_AppendOffsetAndId(toStringBuffer, offset, "DDocumentation");
    Util.initToStringBuffer_ObjectValue(toStringBuffer, offset, "language : ", language);
  }
}
