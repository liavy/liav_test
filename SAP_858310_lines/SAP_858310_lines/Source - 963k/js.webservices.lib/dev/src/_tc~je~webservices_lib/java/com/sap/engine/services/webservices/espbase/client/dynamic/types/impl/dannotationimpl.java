package com.sap.engine.services.webservices.espbase.client.dynamic.types.impl;

import com.sap.engine.services.webservices.espbase.client.dynamic.util.Util;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAnnotation;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAppInfo;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAttributeInfoItem;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAttributesMap;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DDocumentation;

public class DAnnotationImpl implements DAnnotation, DAttributesMap {

  private DAttributeInfoItem[] attribInfoItems;
  private DAppInfo[] appInfos;
  private DDocumentation[] documentations;
  
  public DAppInfo[] getAppInfos() {
    return(appInfos);
  }
  
  public void setAppInfos(DAppInfo[] appInfos) {
    this.appInfos = appInfos;
  }
  
  public DAttributeInfoItem[] getAttributeInfoItems() {
    return(attribInfoItems);
  }
  
  public void setAttributeInfoItems(DAttributeInfoItem[] attribInfoItems) {
    this.attribInfoItems = attribInfoItems;
  }
  
  public DDocumentation[] getDocumentations() {
    return(documentations);
  }
  
  public void setDocumentations(DDocumentation[] documentations) {
    this.documentations = documentations;
  }
  
  public String toString() {
    StringBuffer toStringBuffer = new StringBuffer();
    initToStringBuffer(toStringBuffer, "");
    return(toStringBuffer.toString());
  }
  
  public void initToStringBuffer(StringBuffer toStringBuffer, String offset) {
    Util.initToStringBuffer_AppendOffsetAndId(toStringBuffer, offset, "DAnnotation");
    Util.initToStringBuffer_DAttributesMap(toStringBuffer, offset + Util.TO_STRING_OFFSET, this);
    initToStringBuffer_AppInfos(toStringBuffer, offset + Util.TO_STRING_OFFSET);
    initToStringBuffer_Documentations(toStringBuffer, offset + Util.TO_STRING_OFFSET);
  }
  
  private void initToStringBuffer_AppInfos(StringBuffer toStringBuffer, String offset) {
    if(appInfos.length != 0) {
      Util.initToStringBuffer_AppendCReturnOffsetAndId(toStringBuffer, offset, "app infos");
      for(int i = 0; i < appInfos.length; i++) {
        DAppInfoImpl dAppInfo = (DAppInfoImpl)(appInfos[i]);
        toStringBuffer.append("\n");
        dAppInfo.initToStringBuffer(toStringBuffer, offset + Util.TO_STRING_OFFSET);
      }
    }
  }
  
  private void initToStringBuffer_Documentations(StringBuffer toStringBuffer, String offset) {
    if(documentations.length != 0) {
      Util.initToStringBuffer_AppendCReturnOffsetAndId(toStringBuffer, offset, "documentations");
      for(int i = 0; i < documentations.length; i++) {
        DDocumentationImpl dDocumentation = (DDocumentationImpl)(documentations[i]);
        toStringBuffer.append("\n");
        dDocumentation.initToStringBuffer(toStringBuffer, offset + Util.TO_STRING_OFFSET);
      }
    }
  }
}
