package com.sap.engine.services.webservices.espbase.client.dynamic.types.impl;

import com.sap.engine.services.webservices.espbase.client.dynamic.util.Util;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DField;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DGroup;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DOccurrenceable;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DStructure;

public class DGroupImpl extends DFieldImpl implements DGroup, DStructure, DOccurrenceable {

  private DField[] fields;
  private int maxOccurs;
  private int minOccurs;
  private String id;
  
  protected DGroupImpl() {
    super();
    fields = new DField[0];
    maxOccurs = 0;
    minOccurs = 0;
  }
  
  public void setID(String id) {
    this.id = id;
  }
  
  public String getID() {
    return(id);
  }
    
  public DField[] getFields() {
    return(fields);
  }
  
  public int getMinOccurs() {
    return(minOccurs);
  }
  
  public int getMaxOccurs() {
    return(maxOccurs);
  }
  
  public void setFields(DField[] fields) {
    if(fields == null){
    	throw new IllegalArgumentException("Cannot set fields to null!");
      }
      this.fields = fields;
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
    Util.initToStringBuffer_AppendOffsetAndId(toStringBuffer, offset, "DGroup");
    initToStringBuffer_DField(toStringBuffer, offset + Util.TO_STRING_OFFSET);
    Util.initToStringBuffer_DOccurrenceable(toStringBuffer, offset + Util.TO_STRING_OFFSET, this);
    Util.initToStringBuffer_DStructure(toStringBuffer, offset + Util.TO_STRING_OFFSET, this);
  }
}
