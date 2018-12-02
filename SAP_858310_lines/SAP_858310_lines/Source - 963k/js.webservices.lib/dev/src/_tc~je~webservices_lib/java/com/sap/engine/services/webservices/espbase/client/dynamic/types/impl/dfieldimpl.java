package com.sap.engine.services.webservices.espbase.client.dynamic.types.impl;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.client.dynamic.util.Util;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DField;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DNamedNode;

public abstract class DFieldImpl implements DField, DNamedNode {

	protected int type;
	protected QName fieldType;
	protected QName fieldScope;
  protected QName fieldName;

  protected DFieldImpl() {
    type = 0;
  }
  
  public QName getFieldName() {
    return(fieldName);
  }
  
  public void setFieldName(QName fieldName) {
    this.fieldName = fieldName;
  }
    
	public int getType() {
		return type;
	}

	public QName getFieldType() {
		return fieldType;
	}

	public QName getFieldScope() {
		return fieldScope;
	}

	public void setFieldScope(QName fieldScope) {
		this.fieldScope = fieldScope;
	}

	public void setFieldType(QName fieldType) {
		this.fieldType = fieldType;
	}

	public void setType(int type) {
		if(type < DField.SIMPLE || type > DField.ATTRIBUTE){
			throw new IllegalArgumentException("Type argument is outside of legal range!");
		}
		this.type = type;
	}
    
  public String toString() {
    StringBuffer toStringBuffer = new StringBuffer();
    initToStringBuffer(toStringBuffer, "");
    return(toStringBuffer.toString());
  }
  
  public abstract void initToStringBuffer(StringBuffer toStringBuffer, String offset);

  protected void initToStringBuffer_DField(StringBuffer toStringBuffer, String offset) {
    Util.initToStringBuffer_ObjectValue(toStringBuffer, offset, "field name : ", fieldName);
    Util.initToStringBuffer_ObjectValue(toStringBuffer, offset, "field type : ", fieldType);
    Util.initToStringBuffer_ObjectValue(toStringBuffer, offset, "field scope : ", fieldScope);
    Util.initToStringBuffer_IntValue(toStringBuffer, offset, "type : ", type);
  }
}
