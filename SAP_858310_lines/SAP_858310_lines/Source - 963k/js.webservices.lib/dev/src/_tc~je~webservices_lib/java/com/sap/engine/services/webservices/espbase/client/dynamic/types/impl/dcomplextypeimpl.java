package com.sap.engine.services.webservices.espbase.client.dynamic.types.impl;

import com.sap.engine.services.webservices.espbase.client.dynamic.util.Util;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DComplexType;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DField;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DSimpleContent;

public class DComplexTypeImpl extends DBaseTypeImpl implements DComplexType {

  private DField[] fields;
  private DField[] attributes;
  private DSimpleContent simpleContent;
  private boolean isMixedContent;
  private String id;
  private boolean isAbstract;
    
  protected DComplexTypeImpl() {
    super();
    fields = new DField[0];
    attributes = new DField[0];
  }
  
  public void setID(String id) {
    this.id = id;
  }
  
  public String getID() {
    return(id);
  }
  
  public boolean isMixedContent() {
    return(isMixedContent);
  }
  
  public void setMixedContent(boolean isMixedContent) {
    this.isMixedContent = isMixedContent;
  }
  
  public DField[] getFields() {
    return(fields);
  }
  
  public void setFields(DField[] fields) {
    this.fields = fields;
  }
  
  public void setType(int type) {
    if (type < DComplexType.SIMPLE || type > DComplexType.ALL) {
    	throw new IllegalArgumentException("type argument outside legal range!");
      }
      this.type = type;
  }
  
  public DField[] getAttributes() {
    return(attributes);
  }
  
  public void setAttributes(DField[] attributes) {
    this.attributes = attributes;
  }
  
  /**
   * @return Returns the simpContent.
   */
  public DSimpleContent getSimpleContent() {
    return(simpleContent);
  }
  
  /**
   * @param simpContent The simpContent to set.
   */
  public void setSimpleContent(DSimpleContent simpContent) {
    this.simpleContent = simpContent;
  }
  
  public String toString() {
    StringBuffer toStringBuffer = new StringBuffer();
    initToStringBuffer(toStringBuffer, "");
    return(toStringBuffer.toString());
  }
  
  public void initToStringBuffer(StringBuffer toStringBuffer, String offset) {
    Util.initToStringBuffer_AppendOffsetAndId(toStringBuffer, offset, "DComplexType");
    initToStringBuffer_DBaseType(toStringBuffer, offset + Util.TO_STRING_OFFSET);
    Util.initToStringBuffer_BooleanValue(toStringBuffer, offset + Util.TO_STRING_OFFSET, "is mixed content : ", isMixedContent);
    Util.initToStringBuffer_BooleanValue(toStringBuffer, offset + Util.TO_STRING_OFFSET, "is abstract : ", isAbstract);
    initToStringBuffer_SimpleContent(toStringBuffer, offset + Util.TO_STRING_OFFSET);
    Util.initToStringBuffer_Fields(toStringBuffer, offset + Util.TO_STRING_OFFSET, "attributes", attributes);
    Util.initToStringBuffer_DStructure(toStringBuffer, offset + Util.TO_STRING_OFFSET, this);
  }
  
  private void initToStringBuffer_SimpleContent(StringBuffer toStringBuffer, String offset) {
    if(simpleContent != null) {
      Util.initToStringBuffer_AppendCReturnOffsetAndId(toStringBuffer, offset, "simple content");
      ((DSimpleContentImpl)simpleContent).initToStringBuffer(toStringBuffer, offset + Util.TO_STRING_OFFSET);
    }
  }

  public boolean isAbstract() {
    return(isAbstract);
  }

  public void setAbstract(boolean isAbstract) {
    this.isAbstract = isAbstract;
  }
}
