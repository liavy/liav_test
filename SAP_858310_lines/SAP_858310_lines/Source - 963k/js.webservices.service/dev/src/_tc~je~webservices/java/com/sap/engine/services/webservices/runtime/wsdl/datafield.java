package com.sap.engine.services.webservices.runtime.wsdl;

import com.sap.engine.lib.descriptors.ws04vi.TypeState;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class DataField {

  private String fieldName;
  private String originalTypeName;
  private String schemaTypeName;
  private TypeState typeState;
  private Object arrayHolder;
  boolean isUnbounded;
  boolean isAttribute = false;
//  public DataField() {
//  }

  public DataField(String fieldName, String originalTypeName, String virtualTypeName, TypeState typeState) {
    this.fieldName = fieldName;
    this.originalTypeName = originalTypeName;
    this.schemaTypeName = virtualTypeName;
    this.typeState = typeState;
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getOriginalTypeName() {
    return this.originalTypeName;
  }

  public String getVirtualTypeName() {
    return this.schemaTypeName;
  }

  public void setArrayHolder(Object obj) {
    this.arrayHolder = obj;
  }

  public Object getArrayHolder() {
    return arrayHolder;
  }

  public boolean hasArrayHolder() {
    return (this.arrayHolder != null);
  }

//  public void setFieldName(String name) {
//    this.fieldName = name;
//  }
  public TypeState getTypeState() {
    return typeState;
  }

  public String toString() {
    return "DataField: name=" + fieldName + ", originalTypeName=" + this.originalTypeName + ", schemaTypeName=" + this.schemaTypeName;
  }

}