﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Mon May 29 10:14:51 EEST 2006
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.sapcontrol.client.types.frm;

/**
 * Schema complexType Java representation.
 * Represents type {urn:SAPControl}SyslogEntry
 */
public  class SyslogEntrySD extends com.sap.engine.services.webservices.jaxrpc.encoding.GeneratedComplexType {

  public java.lang.String _d_originalUri() {
    return "urn:SAPControl";
  }

  public java.lang.String _d_originalLocalName() {
    return "SyslogEntry";
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
    FIELDINFO = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo[8];
    FIELDINFO[0] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    FIELDINFO[1] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    FIELDINFO[2] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    FIELDINFO[3] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    FIELDINFO[4] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    FIELDINFO[5] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    FIELDINFO[6] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    FIELDINFO[7] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    // Field 0
    FIELDINFO[0].defaultValue = null;
    FIELDINFO[0].fieldJavaName = "Time";
    FIELDINFO[0].fieldLocalName = "Time";
    FIELDINFO[0].fieldModel = 1;
    FIELDINFO[0].fieldUri = "";
    FIELDINFO[0].isSoapArray = false;
    FIELDINFO[0].maxOccurs = 1;
    FIELDINFO[0].minOccurs = 0;
    FIELDINFO[0].nillable = true;
    FIELDINFO[0].soapArrayDimensions = 0;
    FIELDINFO[0].soapArrayItemTypeJavaName = null;
    FIELDINFO[0].soapArrayItemTypeLocalName = null;
    FIELDINFO[0].soapArrayItemTypeUri = null;
    FIELDINFO[0].typeJavaName = "java.lang.String";
    FIELDINFO[0].typeLocalName = "string";
    FIELDINFO[0].typeUri = "http://www.w3.org/2001/XMLSchema";
    FIELDINFO[0].getterMethod = "getTime";
    FIELDINFO[0].setterMethod = "setTime";
    FIELDINFO[0].checkMethod = null;
    // Field 1
    FIELDINFO[1].defaultValue = null;
    FIELDINFO[1].fieldJavaName = "Typ";
    FIELDINFO[1].fieldLocalName = "Typ";
    FIELDINFO[1].fieldModel = 1;
    FIELDINFO[1].fieldUri = "";
    FIELDINFO[1].isSoapArray = false;
    FIELDINFO[1].maxOccurs = 1;
    FIELDINFO[1].minOccurs = 0;
    FIELDINFO[1].nillable = true;
    FIELDINFO[1].soapArrayDimensions = 0;
    FIELDINFO[1].soapArrayItemTypeJavaName = null;
    FIELDINFO[1].soapArrayItemTypeLocalName = null;
    FIELDINFO[1].soapArrayItemTypeUri = null;
    FIELDINFO[1].typeJavaName = "java.lang.String";
    FIELDINFO[1].typeLocalName = "string";
    FIELDINFO[1].typeUri = "http://www.w3.org/2001/XMLSchema";
    FIELDINFO[1].getterMethod = "getTyp";
    FIELDINFO[1].setterMethod = "setTyp";
    FIELDINFO[1].checkMethod = null;
    // Field 2
    FIELDINFO[2].defaultValue = null;
    FIELDINFO[2].fieldJavaName = "Client";
    FIELDINFO[2].fieldLocalName = "Client";
    FIELDINFO[2].fieldModel = 1;
    FIELDINFO[2].fieldUri = "";
    FIELDINFO[2].isSoapArray = false;
    FIELDINFO[2].maxOccurs = 1;
    FIELDINFO[2].minOccurs = 0;
    FIELDINFO[2].nillable = true;
    FIELDINFO[2].soapArrayDimensions = 0;
    FIELDINFO[2].soapArrayItemTypeJavaName = null;
    FIELDINFO[2].soapArrayItemTypeLocalName = null;
    FIELDINFO[2].soapArrayItemTypeUri = null;
    FIELDINFO[2].typeJavaName = "java.lang.String";
    FIELDINFO[2].typeLocalName = "string";
    FIELDINFO[2].typeUri = "http://www.w3.org/2001/XMLSchema";
    FIELDINFO[2].getterMethod = "getClient";
    FIELDINFO[2].setterMethod = "setClient";
    FIELDINFO[2].checkMethod = null;
    // Field 3
    FIELDINFO[3].defaultValue = null;
    FIELDINFO[3].fieldJavaName = "User";
    FIELDINFO[3].fieldLocalName = "User";
    FIELDINFO[3].fieldModel = 1;
    FIELDINFO[3].fieldUri = "";
    FIELDINFO[3].isSoapArray = false;
    FIELDINFO[3].maxOccurs = 1;
    FIELDINFO[3].minOccurs = 0;
    FIELDINFO[3].nillable = true;
    FIELDINFO[3].soapArrayDimensions = 0;
    FIELDINFO[3].soapArrayItemTypeJavaName = null;
    FIELDINFO[3].soapArrayItemTypeLocalName = null;
    FIELDINFO[3].soapArrayItemTypeUri = null;
    FIELDINFO[3].typeJavaName = "java.lang.String";
    FIELDINFO[3].typeLocalName = "string";
    FIELDINFO[3].typeUri = "http://www.w3.org/2001/XMLSchema";
    FIELDINFO[3].getterMethod = "getUser";
    FIELDINFO[3].setterMethod = "setUser";
    FIELDINFO[3].checkMethod = null;
    // Field 4
    FIELDINFO[4].defaultValue = null;
    FIELDINFO[4].fieldJavaName = "Tcode";
    FIELDINFO[4].fieldLocalName = "Tcode";
    FIELDINFO[4].fieldModel = 1;
    FIELDINFO[4].fieldUri = "";
    FIELDINFO[4].isSoapArray = false;
    FIELDINFO[4].maxOccurs = 1;
    FIELDINFO[4].minOccurs = 0;
    FIELDINFO[4].nillable = true;
    FIELDINFO[4].soapArrayDimensions = 0;
    FIELDINFO[4].soapArrayItemTypeJavaName = null;
    FIELDINFO[4].soapArrayItemTypeLocalName = null;
    FIELDINFO[4].soapArrayItemTypeUri = null;
    FIELDINFO[4].typeJavaName = "java.lang.String";
    FIELDINFO[4].typeLocalName = "string";
    FIELDINFO[4].typeUri = "http://www.w3.org/2001/XMLSchema";
    FIELDINFO[4].getterMethod = "getTcode";
    FIELDINFO[4].setterMethod = "setTcode";
    FIELDINFO[4].checkMethod = null;
    // Field 5
    FIELDINFO[5].defaultValue = null;
    FIELDINFO[5].fieldJavaName = "MNo";
    FIELDINFO[5].fieldLocalName = "MNo";
    FIELDINFO[5].fieldModel = 1;
    FIELDINFO[5].fieldUri = "";
    FIELDINFO[5].isSoapArray = false;
    FIELDINFO[5].maxOccurs = 1;
    FIELDINFO[5].minOccurs = 0;
    FIELDINFO[5].nillable = true;
    FIELDINFO[5].soapArrayDimensions = 0;
    FIELDINFO[5].soapArrayItemTypeJavaName = null;
    FIELDINFO[5].soapArrayItemTypeLocalName = null;
    FIELDINFO[5].soapArrayItemTypeUri = null;
    FIELDINFO[5].typeJavaName = "java.lang.String";
    FIELDINFO[5].typeLocalName = "string";
    FIELDINFO[5].typeUri = "http://www.w3.org/2001/XMLSchema";
    FIELDINFO[5].getterMethod = "getMNo";
    FIELDINFO[5].setterMethod = "setMNo";
    FIELDINFO[5].checkMethod = null;
    // Field 6
    FIELDINFO[6].defaultValue = null;
    FIELDINFO[6].fieldJavaName = "Text";
    FIELDINFO[6].fieldLocalName = "Text";
    FIELDINFO[6].fieldModel = 1;
    FIELDINFO[6].fieldUri = "";
    FIELDINFO[6].isSoapArray = false;
    FIELDINFO[6].maxOccurs = 1;
    FIELDINFO[6].minOccurs = 0;
    FIELDINFO[6].nillable = true;
    FIELDINFO[6].soapArrayDimensions = 0;
    FIELDINFO[6].soapArrayItemTypeJavaName = null;
    FIELDINFO[6].soapArrayItemTypeLocalName = null;
    FIELDINFO[6].soapArrayItemTypeUri = null;
    FIELDINFO[6].typeJavaName = "java.lang.String";
    FIELDINFO[6].typeLocalName = "string";
    FIELDINFO[6].typeUri = "http://www.w3.org/2001/XMLSchema";
    FIELDINFO[6].getterMethod = "getText";
    FIELDINFO[6].setterMethod = "setText";
    FIELDINFO[6].checkMethod = null;
    // Field 7
    FIELDINFO[7].defaultValue = null;
    FIELDINFO[7].fieldJavaName = "Severity";
    FIELDINFO[7].fieldLocalName = "Severity";
    FIELDINFO[7].fieldModel = 1;
    FIELDINFO[7].fieldUri = "";
    FIELDINFO[7].isSoapArray = false;
    FIELDINFO[7].maxOccurs = 1;
    FIELDINFO[7].minOccurs = 1;
    FIELDINFO[7].nillable = false;
    FIELDINFO[7].soapArrayDimensions = 0;
    FIELDINFO[7].soapArrayItemTypeJavaName = null;
    FIELDINFO[7].soapArrayItemTypeLocalName = null;
    FIELDINFO[7].soapArrayItemTypeUri = null;
    FIELDINFO[7].typeJavaName = "com.sap.sapcontrol.client.types.STATECOLOR";
    FIELDINFO[7].typeLocalName = "STATE-COLOR";
    FIELDINFO[7].typeUri = "urn:SAPControl";
    FIELDINFO[7].getterMethod = "getSeverity";
    FIELDINFO[7].setterMethod = "setSeverity";
    FIELDINFO[7].checkMethod = null;
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