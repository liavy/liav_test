﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Thu Nov 30 15:20:54 EET 2006
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.engine.services.webservices.espbase.configuration.soap_app.frm;

/**
 * Schema complexType Java representation.
 * Represents type {http://xml.sap.com/2006/11/esi/conf/soapapplication}ConditionType
 */
public  class ConditionTypeSD extends com.sap.engine.services.webservices.jaxrpc.encoding.GeneratedComplexType {

  public java.lang.String _d_originalUri() {
    return "http://xml.sap.com/2006/11/esi/conf/soapapplication";
  }

  public java.lang.String _d_originalLocalName() {
    return "ConditionType";
  }

  private static com.sap.engine.services.webservices.jaxrpc.encoding.AttributeInfo[] ATTRIBUTEINFO;

  private synchronized static void initAttribs() {
    // Creating attribute fields
    if (ATTRIBUTEINFO != null) return;
    ATTRIBUTEINFO = new com.sap.engine.services.webservices.jaxrpc.encoding.AttributeInfo[0];
  }

  // Field information
  private static com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo[] FIELDINFO;

  private synchronized static void initFields() {
    // Creating fields
    if (FIELDINFO != null) return;
    FIELDINFO = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo[2];
    FIELDINFO[0] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    FIELDINFO[1] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    // Field 0
    FIELDINFO[0].defaultValue = null;
    FIELDINFO[0].fieldJavaName = "Properties";
    FIELDINFO[0].fieldLocalName = "properties";
    FIELDINFO[0].fieldModel = 1;
    FIELDINFO[0].fieldUri = "";
    FIELDINFO[0].isSoapArray = false;
    FIELDINFO[0].maxOccurs = 1;
    FIELDINFO[0].minOccurs = 1;
    FIELDINFO[0].nillable = false;
    FIELDINFO[0].soapArrayDimensions = 0;
    FIELDINFO[0].soapArrayItemTypeJavaName = null;
    FIELDINFO[0].soapArrayItemTypeLocalName = null;
    FIELDINFO[0].soapArrayItemTypeUri = null;
    FIELDINFO[0].typeJavaName = "com.sap.engine.services.webservices.espbase.configuration.soap_app.PListType";
    FIELDINFO[0].typeLocalName = "PListType";
    FIELDINFO[0].typeUri = "http://xml.sap.com/2006/11/esi/conf/soapapplication";
    FIELDINFO[0].getterMethod = "getProperties";
    FIELDINFO[0].setterMethod = "setProperties";
    FIELDINFO[0].checkMethod = "isSetProperties";
    // Field 1
    FIELDINFO[1].defaultValue = null;
    FIELDINFO[1].fieldJavaName = "BooleanValue";
    FIELDINFO[1].fieldLocalName = "boolean-value";
    FIELDINFO[1].fieldModel = 1;
    FIELDINFO[1].fieldUri = "";
    FIELDINFO[1].isSoapArray = false;
    FIELDINFO[1].maxOccurs = 1;
    FIELDINFO[1].minOccurs = 1;
    FIELDINFO[1].nillable = false;
    FIELDINFO[1].soapArrayDimensions = 0;
    FIELDINFO[1].soapArrayItemTypeJavaName = null;
    FIELDINFO[1].soapArrayItemTypeLocalName = null;
    FIELDINFO[1].soapArrayItemTypeUri = null;
    FIELDINFO[1].typeJavaName = "boolean";
    FIELDINFO[1].typeLocalName = "boolean";
    FIELDINFO[1].typeUri = "http://www.w3.org/2001/XMLSchema";
    FIELDINFO[1].getterMethod = "isBooleanValue";
    FIELDINFO[1].setterMethod = "setBooleanValue";
    FIELDINFO[1].checkMethod = "isSetBooleanValue";
  }


  // Returns model Group Type
  public int _getModelType() {
    return 2;
  }

  private static boolean init = false;
  public synchronized com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo[] _getFields() {
    if (init == false) {
      com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo[] parent = super._getFields();
      FIELDINFO =  _insertFieldInfo(parent,FIELDINFO);
      init = true;
    }
    return FIELDINFO;
  }

  public int _getNumberOfFields() {
    return (FIELDINFO.length+super._getNumberOfFields());
  }

  public com.sap.engine.services.webservices.jaxrpc.encoding.AttributeInfo[] _getAttributes() {
    return ATTRIBUTEINFO;
  }

  public int _getNumberOfAttributes() {
    return ATTRIBUTEINFO.length;
  }

  static {
    initFields();
    initAttribs();
  }
}
