﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Mon May 21 11:37:23 EEST 2007
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.engine.services.webservices.espbase.sr.metadata.frm;

/**
 * Schema complexType Java representation.
 * Represents type {http://www.sap.com/webas/2006/11/sr/metadata}ClassificationSystemsMetaData
 */
public  class ClassificationSystemsMetaDataSD extends com.sap.engine.services.webservices.jaxrpc.encoding.GeneratedComplexType {

  public java.lang.String _d_originalUri() {
    return "http://www.sap.com/webas/2006/11/sr/metadata";
  }

  public java.lang.String _d_originalLocalName() {
    return "ClassificationSystemsMetaData";
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
    FIELDINFO[0].fieldJavaName = "ClassificationSystemMetaData";
    FIELDINFO[0].fieldLocalName = "ClassificationSystemMetaData";
    FIELDINFO[0].fieldModel = 1;
    FIELDINFO[0].fieldUri = "http://www.sap.com/webas/2006/11/sr/metadata";
    FIELDINFO[0].isSoapArray = false;
    FIELDINFO[0].maxOccurs = 2147483647;
    FIELDINFO[0].minOccurs = 0;
    FIELDINFO[0].nillable = false;
    FIELDINFO[0].soapArrayDimensions = 0;
    FIELDINFO[0].soapArrayItemTypeJavaName = null;
    FIELDINFO[0].soapArrayItemTypeLocalName = null;
    FIELDINFO[0].soapArrayItemTypeUri = null;
    FIELDINFO[0].typeJavaName = "com.sap.engine.services.webservices.espbase.sr.metadata.ClassificationSystemMetaData[]";
    FIELDINFO[0].typeLocalName = "ClassificationSystemMetaData";
    FIELDINFO[0].typeUri = "http://www.sap.com/webas/2006/11/sr/metadata";
    FIELDINFO[0].getterMethod = "getClassificationSystemMetaData";
    FIELDINFO[0].setterMethod = "setClassificationSystemMetaData";
    FIELDINFO[0].checkMethod = null;
    // Field 1
    FIELDINFO[1].defaultValue = null;
    FIELDINFO[1].fieldJavaName = "GroupClassificationSystemMetaData";
    FIELDINFO[1].fieldLocalName = "GroupClassificationSystemMetaData";
    FIELDINFO[1].fieldModel = 1;
    FIELDINFO[1].fieldUri = "http://www.sap.com/webas/2006/11/sr/metadata";
    FIELDINFO[1].isSoapArray = false;
    FIELDINFO[1].maxOccurs = 2147483647;
    FIELDINFO[1].minOccurs = 0;
    FIELDINFO[1].nillable = false;
    FIELDINFO[1].soapArrayDimensions = 0;
    FIELDINFO[1].soapArrayItemTypeJavaName = null;
    FIELDINFO[1].soapArrayItemTypeLocalName = null;
    FIELDINFO[1].soapArrayItemTypeUri = null;
    FIELDINFO[1].typeJavaName = "com.sap.engine.services.webservices.espbase.sr.metadata.GroupClassificationSystemMetaData[]";
    FIELDINFO[1].typeLocalName = "GroupClassificationSystemMetaData";
    FIELDINFO[1].typeUri = "http://www.sap.com/webas/2006/11/sr/metadata";
    FIELDINFO[1].getterMethod = "getGroupClassificationSystemMetaData";
    FIELDINFO[1].setterMethod = "setGroupClassificationSystemMetaData";
    FIELDINFO[1].checkMethod = null;
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
