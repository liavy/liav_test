package com.sap.engine.services.webservices.espbase.client.dynamic.util;

import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAnnotateable;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAttributeInfoItem;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAttributesMap;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DField;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DOccurrenceable;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DStructure;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.impl.DAnnotationImpl;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.impl.DAttributeInfoItemImpl;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.impl.DFieldImpl;

public class Util {
  
  public static final String TO_STRING_OFFSET = "        ";
  
  public static void initToStringBuffer_IntValue(StringBuffer toStringBuffer, String offset, String intValueId, int intValue) {
    initToStringBuffer_AppendCReturnOffsetAndId(toStringBuffer, offset, intValueId);
    toStringBuffer.append(intValue);
  }

  public static void initToStringBuffer_BooleanValue(StringBuffer toStringBuffer, String offset, String booleanValueId, boolean booleanValue) {
    initToStringBuffer_AppendCReturnOffsetAndId(toStringBuffer, offset, booleanValueId);
    toStringBuffer.append(booleanValue);
  }
  
  public static void initToStringBuffer_ObjectValue(StringBuffer toStringBuffer, String offset, String id, Object object) {
    if(object != null) {
      initToStringBuffer_AppendCReturnOffsetAndId(toStringBuffer, offset, id);
      toStringBuffer.append(object);
    }
  }

  public static void initToStringBuffer_Fields(StringBuffer toStringBuffer, String offset, String id, DField[] fields) {
    if(fields.length != 0) {
      initToStringBuffer_AppendCReturnOffsetAndId(toStringBuffer, offset, id);
      for(int i = 0; i < fields.length; i++) {
        DFieldImpl dField = (DFieldImpl)(fields[i]); 
        toStringBuffer.append("\n");
        dField.initToStringBuffer(toStringBuffer, offset + TO_STRING_OFFSET);
      }
    }
  }
  
  public static void initToStringBuffer_DOccurrenceable(StringBuffer toStringBuffer, String offset, DOccurrenceable dOccurrenceable) {
    initToStringBuffer_IntValue(toStringBuffer, offset + TO_STRING_OFFSET, "min ocuurs : ", dOccurrenceable.getMinOccurs());
    initToStringBuffer_IntValue(toStringBuffer, offset + TO_STRING_OFFSET, "max ocuurs : ", dOccurrenceable.getMaxOccurs());
  }
  
  public static void initToStringBuffer_DAnnotationable(StringBuffer toStringBuffer, String offset, DAnnotateable dAnnotateable) {
    DAnnotationImpl annotation = (DAnnotationImpl)(dAnnotateable.getAnnotation());
    if(annotation != null) {
      initToStringBuffer_AppendCReturnOffsetAndId(toStringBuffer, offset, "annotation");
      toStringBuffer.append("\n");
      annotation.initToStringBuffer(toStringBuffer, offset + TO_STRING_OFFSET);
    }
  }
  
  public static void initToStringBuffer_DAttributesMap(StringBuffer toStringBuffer, String offset, DAttributesMap attribsMap) {
    DAttributeInfoItem[] attribInfoItems = attribsMap.getAttributeInfoItems();
    if(attribInfoItems.length != 0) {
      initToStringBuffer_AppendCReturnOffsetAndId(toStringBuffer, offset, "attribute info items");
      for(int i = 0; i < attribInfoItems.length; i++) {
        DAttributeInfoItemImpl dAttribInfoItem = (DAttributeInfoItemImpl)attribInfoItems[i];
        toStringBuffer.append("\n");
        dAttribInfoItem.initToStringBuffer(toStringBuffer, offset + TO_STRING_OFFSET);
      }
    }
  }
  
  public static void initToStringBuffer_DStructure(StringBuffer toStringBuffer, String offset, DStructure dStructure) {
    initToStringBuffer_Fields(toStringBuffer, offset, "fields", dStructure.getFields());
  }
  
  public static void initToStringBuffer_AppendCReturnOffsetAndId(StringBuffer toStringBuffer, String offset, String id) {
    toStringBuffer.append("\n");
    initToStringBuffer_AppendOffsetAndId(toStringBuffer, offset, id);
  }
  
  public static void initToStringBuffer_AppendOffsetAndId(StringBuffer toStringBuffer, String offset, String id) {
    toStringBuffer.append(offset);
    toStringBuffer.append(id);
  }
  
  public static String createQName(String prefix, String localName) {
    return(prefix + ":" + localName);
  }
}
