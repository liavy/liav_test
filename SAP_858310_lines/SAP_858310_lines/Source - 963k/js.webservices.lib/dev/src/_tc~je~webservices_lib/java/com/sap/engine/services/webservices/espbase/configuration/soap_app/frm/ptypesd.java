﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Thu Nov 30 15:20:54 EET 2006
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.engine.services.webservices.espbase.configuration.soap_app.frm;

/**
 * Schema complexType Java representation.
 * Represents type {http://xml.sap.com/2006/11/esi/conf/soapapplication}PType
 */
public  class PTypeSD extends com.sap.engine.services.webservices.jaxrpc.encoding.GeneratedComplexType {

  public java.lang.String _d_originalUri() {
    return "http://xml.sap.com/2006/11/esi/conf/soapapplication";
  }

  public java.lang.String _d_originalLocalName() {
    return "PType";
  }

  private static com.sap.engine.services.webservices.jaxrpc.encoding.AttributeInfo[] ATTRIBUTEINFO;

  private synchronized static void initAttribs() {
    // Creating attribute fields
    if (ATTRIBUTEINFO != null) return;
    ATTRIBUTEINFO = new com.sap.engine.services.webservices.jaxrpc.encoding.AttributeInfo[3];
    ATTRIBUTEINFO[0] = new com.sap.engine.services.webservices.jaxrpc.encoding.AttributeInfo();
    ATTRIBUTEINFO[1] = new com.sap.engine.services.webservices.jaxrpc.encoding.AttributeInfo();
    ATTRIBUTEINFO[2] = new com.sap.engine.services.webservices.jaxrpc.encoding.AttributeInfo();
    // Attribute 0
    ATTRIBUTEINFO[0].fieldLocalName = "namespace";
    ATTRIBUTEINFO[0].fieldUri = "";
    ATTRIBUTEINFO[0].fieldJavaName = "Namespace";
    ATTRIBUTEINFO[0].typeName = "string";
    ATTRIBUTEINFO[0].typeUri = "http://www.w3.org/2001/XMLSchema";
    ATTRIBUTEINFO[0].typeJavaName = "java.lang.String";
    ATTRIBUTEINFO[0].defaultValue = null;
    ATTRIBUTEINFO[0].required = true;
    ATTRIBUTEINFO[0].setterMethod = "setNamespace";
    ATTRIBUTEINFO[0].getterMethod = "getNamespace";
    ATTRIBUTEINFO[0].checkMethod = null;
    // Attribute 1
    ATTRIBUTEINFO[1].fieldLocalName = "name";
    ATTRIBUTEINFO[1].fieldUri = "";
    ATTRIBUTEINFO[1].fieldJavaName = "Name";
    ATTRIBUTEINFO[1].typeName = "string";
    ATTRIBUTEINFO[1].typeUri = "http://www.w3.org/2001/XMLSchema";
    ATTRIBUTEINFO[1].typeJavaName = "java.lang.String";
    ATTRIBUTEINFO[1].defaultValue = null;
    ATTRIBUTEINFO[1].required = true;
    ATTRIBUTEINFO[1].setterMethod = "setName";
    ATTRIBUTEINFO[1].getterMethod = "getName";
    ATTRIBUTEINFO[1].checkMethod = null;
    // Attribute 2
    ATTRIBUTEINFO[2].fieldLocalName = "value";
    ATTRIBUTEINFO[2].fieldUri = "";
    ATTRIBUTEINFO[2].fieldJavaName = "Value";
    ATTRIBUTEINFO[2].typeName = "string";
    ATTRIBUTEINFO[2].typeUri = "http://www.w3.org/2001/XMLSchema";
    ATTRIBUTEINFO[2].typeJavaName = "java.lang.String";
    ATTRIBUTEINFO[2].defaultValue = null;
    ATTRIBUTEINFO[2].required = false;
    ATTRIBUTEINFO[2].setterMethod = "setValue";
    ATTRIBUTEINFO[2].getterMethod = "getValue";
    ATTRIBUTEINFO[2].checkMethod = null;
  }

  // Field information
  private static com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo[] FIELDINFO;

  private synchronized static void initFields() {
    // Creating fields
    if (FIELDINFO != null) return;
    FIELDINFO = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo[0];
  }


  // Returns model Group Type
  public int _getModelType() {
    return 3;
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
