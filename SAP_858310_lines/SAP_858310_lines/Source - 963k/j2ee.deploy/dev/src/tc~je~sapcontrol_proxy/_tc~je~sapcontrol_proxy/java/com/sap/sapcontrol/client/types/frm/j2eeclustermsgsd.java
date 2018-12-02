﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Mon May 29 10:14:51 EEST 2006
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.sapcontrol.client.types.frm;

/**
 * Schema complexType Java representation.
 * Represents type {urn:SAPControl}J2EEClusterMsg
 */
public  class J2EEClusterMsgSD extends com.sap.engine.services.webservices.jaxrpc.encoding.GeneratedComplexType {

  public java.lang.String _d_originalUri() {
    return "urn:SAPControl";
  }

  public java.lang.String _d_originalLocalName() {
    return "J2EEClusterMsg";
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
    FIELDINFO = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo[11];
    FIELDINFO[0] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    FIELDINFO[1] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    FIELDINFO[2] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    FIELDINFO[3] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    FIELDINFO[4] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    FIELDINFO[5] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    FIELDINFO[6] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    FIELDINFO[7] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    FIELDINFO[8] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    FIELDINFO[9] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    FIELDINFO[10] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    // Field 0
    FIELDINFO[0].defaultValue = null;
    FIELDINFO[0].fieldJavaName = "Service";
    FIELDINFO[0].fieldLocalName = "service";
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
    FIELDINFO[0].getterMethod = "getService";
    FIELDINFO[0].setterMethod = "setService";
    FIELDINFO[0].checkMethod = null;
    // Field 1
    FIELDINFO[1].defaultValue = null;
    FIELDINFO[1].fieldJavaName = "Id";
    FIELDINFO[1].fieldLocalName = "id";
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
    FIELDINFO[1].getterMethod = "getId";
    FIELDINFO[1].setterMethod = "setId";
    FIELDINFO[1].checkMethod = null;
    // Field 2
    FIELDINFO[2].defaultValue = null;
    FIELDINFO[2].fieldJavaName = "Count";
    FIELDINFO[2].fieldLocalName = "count";
    FIELDINFO[2].fieldModel = 1;
    FIELDINFO[2].fieldUri = "";
    FIELDINFO[2].isSoapArray = false;
    FIELDINFO[2].maxOccurs = 1;
    FIELDINFO[2].minOccurs = 1;
    FIELDINFO[2].nillable = false;
    FIELDINFO[2].soapArrayDimensions = 0;
    FIELDINFO[2].soapArrayItemTypeJavaName = null;
    FIELDINFO[2].soapArrayItemTypeLocalName = null;
    FIELDINFO[2].soapArrayItemTypeUri = null;
    FIELDINFO[2].typeJavaName = "long";
    FIELDINFO[2].typeLocalName = "long";
    FIELDINFO[2].typeUri = "http://www.w3.org/2001/XMLSchema";
    FIELDINFO[2].getterMethod = "getCount";
    FIELDINFO[2].setterMethod = "setCount";
    FIELDINFO[2].checkMethod = null;
    // Field 3
    FIELDINFO[3].defaultValue = null;
    FIELDINFO[3].fieldJavaName = "Length";
    FIELDINFO[3].fieldLocalName = "length";
    FIELDINFO[3].fieldModel = 1;
    FIELDINFO[3].fieldUri = "";
    FIELDINFO[3].isSoapArray = false;
    FIELDINFO[3].maxOccurs = 1;
    FIELDINFO[3].minOccurs = 1;
    FIELDINFO[3].nillable = false;
    FIELDINFO[3].soapArrayDimensions = 0;
    FIELDINFO[3].soapArrayItemTypeJavaName = null;
    FIELDINFO[3].soapArrayItemTypeLocalName = null;
    FIELDINFO[3].soapArrayItemTypeUri = null;
    FIELDINFO[3].typeJavaName = "long";
    FIELDINFO[3].typeLocalName = "long";
    FIELDINFO[3].typeUri = "http://www.w3.org/2001/XMLSchema";
    FIELDINFO[3].getterMethod = "getLength";
    FIELDINFO[3].setterMethod = "setLength";
    FIELDINFO[3].checkMethod = null;
    // Field 4
    FIELDINFO[4].defaultValue = null;
    FIELDINFO[4].fieldJavaName = "AvgLength";
    FIELDINFO[4].fieldLocalName = "avg-length";
    FIELDINFO[4].fieldModel = 1;
    FIELDINFO[4].fieldUri = "";
    FIELDINFO[4].isSoapArray = false;
    FIELDINFO[4].maxOccurs = 1;
    FIELDINFO[4].minOccurs = 1;
    FIELDINFO[4].nillable = false;
    FIELDINFO[4].soapArrayDimensions = 0;
    FIELDINFO[4].soapArrayItemTypeJavaName = null;
    FIELDINFO[4].soapArrayItemTypeLocalName = null;
    FIELDINFO[4].soapArrayItemTypeUri = null;
    FIELDINFO[4].typeJavaName = "long";
    FIELDINFO[4].typeLocalName = "long";
    FIELDINFO[4].typeUri = "http://www.w3.org/2001/XMLSchema";
    FIELDINFO[4].getterMethod = "getAvgLength";
    FIELDINFO[4].setterMethod = "setAvgLength";
    FIELDINFO[4].checkMethod = null;
    // Field 5
    FIELDINFO[5].defaultValue = null;
    FIELDINFO[5].fieldJavaName = "MaxLength";
    FIELDINFO[5].fieldLocalName = "max-length";
    FIELDINFO[5].fieldModel = 1;
    FIELDINFO[5].fieldUri = "";
    FIELDINFO[5].isSoapArray = false;
    FIELDINFO[5].maxOccurs = 1;
    FIELDINFO[5].minOccurs = 1;
    FIELDINFO[5].nillable = false;
    FIELDINFO[5].soapArrayDimensions = 0;
    FIELDINFO[5].soapArrayItemTypeJavaName = null;
    FIELDINFO[5].soapArrayItemTypeLocalName = null;
    FIELDINFO[5].soapArrayItemTypeUri = null;
    FIELDINFO[5].typeJavaName = "long";
    FIELDINFO[5].typeLocalName = "long";
    FIELDINFO[5].typeUri = "http://www.w3.org/2001/XMLSchema";
    FIELDINFO[5].getterMethod = "getMaxLength";
    FIELDINFO[5].setterMethod = "setMaxLength";
    FIELDINFO[5].checkMethod = null;
    // Field 6
    FIELDINFO[6].defaultValue = null;
    FIELDINFO[6].fieldJavaName = "CountP2PMsg";
    FIELDINFO[6].fieldLocalName = "count-p2p-msg";
    FIELDINFO[6].fieldModel = 1;
    FIELDINFO[6].fieldUri = "";
    FIELDINFO[6].isSoapArray = false;
    FIELDINFO[6].maxOccurs = 1;
    FIELDINFO[6].minOccurs = 1;
    FIELDINFO[6].nillable = false;
    FIELDINFO[6].soapArrayDimensions = 0;
    FIELDINFO[6].soapArrayItemTypeJavaName = null;
    FIELDINFO[6].soapArrayItemTypeLocalName = null;
    FIELDINFO[6].soapArrayItemTypeUri = null;
    FIELDINFO[6].typeJavaName = "long";
    FIELDINFO[6].typeLocalName = "long";
    FIELDINFO[6].typeUri = "http://www.w3.org/2001/XMLSchema";
    FIELDINFO[6].getterMethod = "getCountP2PMsg";
    FIELDINFO[6].setterMethod = "setCountP2PMsg";
    FIELDINFO[6].checkMethod = null;
    // Field 7
    FIELDINFO[7].defaultValue = null;
    FIELDINFO[7].fieldJavaName = "CountP2PRequest";
    FIELDINFO[7].fieldLocalName = "count-p2p-request";
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
    FIELDINFO[7].typeJavaName = "long";
    FIELDINFO[7].typeLocalName = "long";
    FIELDINFO[7].typeUri = "http://www.w3.org/2001/XMLSchema";
    FIELDINFO[7].getterMethod = "getCountP2PRequest";
    FIELDINFO[7].setterMethod = "setCountP2PRequest";
    FIELDINFO[7].checkMethod = null;
    // Field 8
    FIELDINFO[8].defaultValue = null;
    FIELDINFO[8].fieldJavaName = "CountP2PReply";
    FIELDINFO[8].fieldLocalName = "count-p2p-reply";
    FIELDINFO[8].fieldModel = 1;
    FIELDINFO[8].fieldUri = "";
    FIELDINFO[8].isSoapArray = false;
    FIELDINFO[8].maxOccurs = 1;
    FIELDINFO[8].minOccurs = 1;
    FIELDINFO[8].nillable = false;
    FIELDINFO[8].soapArrayDimensions = 0;
    FIELDINFO[8].soapArrayItemTypeJavaName = null;
    FIELDINFO[8].soapArrayItemTypeLocalName = null;
    FIELDINFO[8].soapArrayItemTypeUri = null;
    FIELDINFO[8].typeJavaName = "long";
    FIELDINFO[8].typeLocalName = "long";
    FIELDINFO[8].typeUri = "http://www.w3.org/2001/XMLSchema";
    FIELDINFO[8].getterMethod = "getCountP2PReply";
    FIELDINFO[8].setterMethod = "setCountP2PReply";
    FIELDINFO[8].checkMethod = null;
    // Field 9
    FIELDINFO[9].defaultValue = null;
    FIELDINFO[9].fieldJavaName = "CountBroadcastMsg";
    FIELDINFO[9].fieldLocalName = "count-broadcast-msg";
    FIELDINFO[9].fieldModel = 1;
    FIELDINFO[9].fieldUri = "";
    FIELDINFO[9].isSoapArray = false;
    FIELDINFO[9].maxOccurs = 1;
    FIELDINFO[9].minOccurs = 1;
    FIELDINFO[9].nillable = false;
    FIELDINFO[9].soapArrayDimensions = 0;
    FIELDINFO[9].soapArrayItemTypeJavaName = null;
    FIELDINFO[9].soapArrayItemTypeLocalName = null;
    FIELDINFO[9].soapArrayItemTypeUri = null;
    FIELDINFO[9].typeJavaName = "long";
    FIELDINFO[9].typeLocalName = "long";
    FIELDINFO[9].typeUri = "http://www.w3.org/2001/XMLSchema";
    FIELDINFO[9].getterMethod = "getCountBroadcastMsg";
    FIELDINFO[9].setterMethod = "setCountBroadcastMsg";
    FIELDINFO[9].checkMethod = null;
    // Field 10
    FIELDINFO[10].defaultValue = null;
    FIELDINFO[10].fieldJavaName = "CountBroadcastReply";
    FIELDINFO[10].fieldLocalName = "count-broadcast-reply";
    FIELDINFO[10].fieldModel = 1;
    FIELDINFO[10].fieldUri = "";
    FIELDINFO[10].isSoapArray = false;
    FIELDINFO[10].maxOccurs = 1;
    FIELDINFO[10].minOccurs = 1;
    FIELDINFO[10].nillable = false;
    FIELDINFO[10].soapArrayDimensions = 0;
    FIELDINFO[10].soapArrayItemTypeJavaName = null;
    FIELDINFO[10].soapArrayItemTypeLocalName = null;
    FIELDINFO[10].soapArrayItemTypeUri = null;
    FIELDINFO[10].typeJavaName = "long";
    FIELDINFO[10].typeLocalName = "long";
    FIELDINFO[10].typeUri = "http://www.w3.org/2001/XMLSchema";
    FIELDINFO[10].getterMethod = "getCountBroadcastReply";
    FIELDINFO[10].setterMethod = "setCountBroadcastReply";
    FIELDINFO[10].checkMethod = null;
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