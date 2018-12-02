﻿
package com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types;


/**
 * Schema complex type representation (generated by SAP Schema to Java generator).
 * Represents schema complex type {urn:uddi-org:api_v2}serviceInfo
 */

public  class ServiceInfo extends com.sap.engine.services.webservices.jaxrpc.encoding.GeneratedComplexType {


  public java.lang.String _d_originalUri() {
    return "urn:uddi-org:api_v2";
  }

  public java.lang.String _d_originalLocalName() {
    return "serviceInfo";
  }

  private static com.sap.engine.services.webservices.jaxrpc.encoding.AttributeInfo[] ATTRIBUTEINFO;

  private synchronized static void initAttribs() {
    // Creating attribute fields
    if (ATTRIBUTEINFO != null) return;
    ATTRIBUTEINFO = new com.sap.engine.services.webservices.jaxrpc.encoding.AttributeInfo[2];
    ATTRIBUTEINFO[0] = new com.sap.engine.services.webservices.jaxrpc.encoding.AttributeInfo();
    ATTRIBUTEINFO[1] = new com.sap.engine.services.webservices.jaxrpc.encoding.AttributeInfo();
    // Attribute 0
    ATTRIBUTEINFO[0].fieldLocalName = "serviceKey";
    ATTRIBUTEINFO[0].fieldUri = "";
    ATTRIBUTEINFO[0].fieldJavaName = "ServiceKey";
    ATTRIBUTEINFO[0].typeName = "serviceKey";
    ATTRIBUTEINFO[0].typeUri = "urn:uddi-org:api_v2";
    ATTRIBUTEINFO[0].typeJavaName = "java.lang.String";
    ATTRIBUTEINFO[0].defaultValue = null;
    ATTRIBUTEINFO[0].required = true;
    ATTRIBUTEINFO[0].setterMethod = "setServiceKey";
    ATTRIBUTEINFO[0].getterMethod = "getServiceKey";
    ATTRIBUTEINFO[0].checkMethod = "hasServiceKey";
    // Attribute 1
    ATTRIBUTEINFO[1].fieldLocalName = "businessKey";
    ATTRIBUTEINFO[1].fieldUri = "";
    ATTRIBUTEINFO[1].fieldJavaName = "BusinessKey";
    ATTRIBUTEINFO[1].typeName = "businessKey";
    ATTRIBUTEINFO[1].typeUri = "urn:uddi-org:api_v2";
    ATTRIBUTEINFO[1].typeJavaName = "java.lang.String";
    ATTRIBUTEINFO[1].defaultValue = null;
    ATTRIBUTEINFO[1].required = true;
    ATTRIBUTEINFO[1].setterMethod = "setBusinessKey";
    ATTRIBUTEINFO[1].getterMethod = "getBusinessKey";
    ATTRIBUTEINFO[1].checkMethod = "hasBusinessKey";
  }

  // Field information
  private static com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo[] FIELDINFO;

  private synchronized static void initFields() {
    // Creating fields
    if (FIELDINFO != null) return;
    FIELDINFO = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo[1];
    FIELDINFO[0] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    // Field 0
    FIELDINFO[0].defaultValue = null;
    FIELDINFO[0].fieldJavaName = "Name";
    FIELDINFO[0].fieldLocalName = "name";
    FIELDINFO[0].fieldModel = 1;
    FIELDINFO[0].fieldUri = "urn:uddi-org:api_v2";
    FIELDINFO[0].isSoapArray = false;
    FIELDINFO[0].maxOccurs = 2147483647;
    FIELDINFO[0].minOccurs = 0;
    FIELDINFO[0].nillable = false;
    FIELDINFO[0].soapArrayDimensions = 0;
    FIELDINFO[0].soapArrayItemTypeJavaName = null;
    FIELDINFO[0].soapArrayItemTypeLocalName = null;
    FIELDINFO[0].soapArrayItemTypeUri = null;
    FIELDINFO[0].typeJavaName = "com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.Name";
    FIELDINFO[0].typeLocalName = "name";
    FIELDINFO[0].typeUri = "urn:uddi-org:api_v2";
    FIELDINFO[0].getterMethod = "getName";
    FIELDINFO[0].setterMethod = "setName";
    FIELDINFO[0].checkMethod = "hasName";
  }


  // Returns model Group Type
  public int _getModelType() {
    return 3;
  }

  // Attribute field
  private java.lang.String _a_ServiceKey;
  private boolean _a_hasServiceKey;
  // set method
  public void setServiceKey(java.lang.String _ServiceKey) {
    this._a_ServiceKey = _ServiceKey;
    this._a_hasServiceKey = true;
  }
  // clear method
  public void clearServiceKey(java.lang.String _ServiceKey) {
    this._a_hasServiceKey = false;
  }
  // get method
  public java.lang.String getServiceKey() {
    return _a_ServiceKey;
  }
  // has method
  public boolean hasServiceKey() {
    return _a_hasServiceKey;
  }

  // Attribute field
  private java.lang.String _a_BusinessKey;
  private boolean _a_hasBusinessKey;
  // set method
  public void setBusinessKey(java.lang.String _BusinessKey) {
    this._a_BusinessKey = _BusinessKey;
    this._a_hasBusinessKey = true;
  }
  // clear method
  public void clearBusinessKey(java.lang.String _BusinessKey) {
    this._a_hasBusinessKey = false;
  }
  // get method
  public java.lang.String getBusinessKey() {
    return _a_BusinessKey;
  }
  // has method
  public boolean hasBusinessKey() {
    return _a_hasBusinessKey;
  }

  // Element field
  private com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.Name[] _f_Name = new com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.Name[0];
  public void setName(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.Name[] _Name) {
    this._f_Name = _Name;
  }
  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.Name[] getName() {
    return _f_Name;
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
