﻿
package com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types;


/**
 * Schema complex type representation (generated by SAP Schema to Java generator).
 * Represents schema complex type {urn:uddi-org:api_v2}find_business
 */

public  class FindBusiness extends com.sap.engine.services.webservices.jaxrpc.encoding.GeneratedComplexType {


  public java.lang.String _d_originalUri() {
    return "urn:uddi-org:api_v2";
  }

  public java.lang.String _d_originalLocalName() {
    return "find_business";
  }

  private static com.sap.engine.services.webservices.jaxrpc.encoding.AttributeInfo[] ATTRIBUTEINFO;

  private synchronized static void initAttribs() {
    // Creating attribute fields
    if (ATTRIBUTEINFO != null) return;
    ATTRIBUTEINFO = new com.sap.engine.services.webservices.jaxrpc.encoding.AttributeInfo[2];
    ATTRIBUTEINFO[0] = new com.sap.engine.services.webservices.jaxrpc.encoding.AttributeInfo();
    ATTRIBUTEINFO[1] = new com.sap.engine.services.webservices.jaxrpc.encoding.AttributeInfo();
    // Attribute 0
    ATTRIBUTEINFO[0].fieldLocalName = "generic";
    ATTRIBUTEINFO[0].fieldUri = "";
    ATTRIBUTEINFO[0].fieldJavaName = "Generic";
    ATTRIBUTEINFO[0].typeName = "string";
    ATTRIBUTEINFO[0].typeUri = "http://www.w3.org/2001/XMLSchema";
    ATTRIBUTEINFO[0].typeJavaName = "java.lang.String";
    ATTRIBUTEINFO[0].defaultValue = null;
    ATTRIBUTEINFO[0].required = true;
    ATTRIBUTEINFO[0].setterMethod = "setGeneric";
    ATTRIBUTEINFO[0].getterMethod = "getGeneric";
    ATTRIBUTEINFO[0].checkMethod = "hasGeneric";
    // Attribute 1
    ATTRIBUTEINFO[1].fieldLocalName = "maxRows";
    ATTRIBUTEINFO[1].fieldUri = "";
    ATTRIBUTEINFO[1].fieldJavaName = "MaxRows";
    ATTRIBUTEINFO[1].typeName = "int";
    ATTRIBUTEINFO[1].typeUri = "http://www.w3.org/2001/XMLSchema";
    ATTRIBUTEINFO[1].typeJavaName = "int";
    ATTRIBUTEINFO[1].defaultValue = null;
    ATTRIBUTEINFO[1].required = false;
    ATTRIBUTEINFO[1].setterMethod = "setMaxRows";
    ATTRIBUTEINFO[1].getterMethod = "getMaxRows";
    ATTRIBUTEINFO[1].checkMethod = "hasMaxRows";
  }

  // Field information
  private static com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo[] FIELDINFO;

  private synchronized static void initFields() {
    // Creating fields
    if (FIELDINFO != null) return;
    FIELDINFO = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo[6];
    FIELDINFO[0] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    FIELDINFO[1] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    FIELDINFO[2] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    FIELDINFO[3] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    FIELDINFO[4] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    FIELDINFO[5] = new com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo();
    // Field 0
    FIELDINFO[0].defaultValue = null;
    FIELDINFO[0].fieldJavaName = "FindQualifiers";
    FIELDINFO[0].fieldLocalName = "findQualifiers";
    FIELDINFO[0].fieldModel = 1;
    FIELDINFO[0].fieldUri = "urn:uddi-org:api_v2";
    FIELDINFO[0].isSoapArray = false;
    FIELDINFO[0].maxOccurs = 1;
    FIELDINFO[0].minOccurs = 0;
    FIELDINFO[0].nillable = false;
    FIELDINFO[0].soapArrayDimensions = 0;
    FIELDINFO[0].soapArrayItemTypeJavaName = null;
    FIELDINFO[0].soapArrayItemTypeLocalName = null;
    FIELDINFO[0].soapArrayItemTypeUri = null;
    FIELDINFO[0].typeJavaName = "java.lang.String[]";
    FIELDINFO[0].typeLocalName = "findQualifiers";
    FIELDINFO[0].typeUri = "urn:uddi-org:api_v2";
    FIELDINFO[0].getterMethod = "getFindQualifiers";
    FIELDINFO[0].setterMethod = "setFindQualifiers";
    FIELDINFO[0].checkMethod = "hasFindQualifiers";
    // Field 1
    FIELDINFO[1].defaultValue = null;
    FIELDINFO[1].fieldJavaName = "Name";
    FIELDINFO[1].fieldLocalName = "name";
    FIELDINFO[1].fieldModel = 1;
    FIELDINFO[1].fieldUri = "urn:uddi-org:api_v2";
    FIELDINFO[1].isSoapArray = false;
    FIELDINFO[1].maxOccurs = 2147483647;
    FIELDINFO[1].minOccurs = 0;
    FIELDINFO[1].nillable = false;
    FIELDINFO[1].soapArrayDimensions = 0;
    FIELDINFO[1].soapArrayItemTypeJavaName = null;
    FIELDINFO[1].soapArrayItemTypeLocalName = null;
    FIELDINFO[1].soapArrayItemTypeUri = null;
    FIELDINFO[1].typeJavaName = "com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.Name";
    FIELDINFO[1].typeLocalName = "name";
    FIELDINFO[1].typeUri = "urn:uddi-org:api_v2";
    FIELDINFO[1].getterMethod = "getName";
    FIELDINFO[1].setterMethod = "setName";
    FIELDINFO[1].checkMethod = "hasName";
    // Field 2
    FIELDINFO[2].defaultValue = null;
    FIELDINFO[2].fieldJavaName = "IdentifierBag";
    FIELDINFO[2].fieldLocalName = "identifierBag";
    FIELDINFO[2].fieldModel = 1;
    FIELDINFO[2].fieldUri = "urn:uddi-org:api_v2";
    FIELDINFO[2].isSoapArray = false;
    FIELDINFO[2].maxOccurs = 1;
    FIELDINFO[2].minOccurs = 0;
    FIELDINFO[2].nillable = false;
    FIELDINFO[2].soapArrayDimensions = 0;
    FIELDINFO[2].soapArrayItemTypeJavaName = null;
    FIELDINFO[2].soapArrayItemTypeLocalName = null;
    FIELDINFO[2].soapArrayItemTypeUri = null;
    FIELDINFO[2].typeJavaName = "com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.KeyedReference[]";
    FIELDINFO[2].typeLocalName = "identifierBag";
    FIELDINFO[2].typeUri = "urn:uddi-org:api_v2";
    FIELDINFO[2].getterMethod = "getIdentifierBag";
    FIELDINFO[2].setterMethod = "setIdentifierBag";
    FIELDINFO[2].checkMethod = "hasIdentifierBag";
    // Field 3
    FIELDINFO[3].defaultValue = null;
    FIELDINFO[3].fieldJavaName = "CategoryBag";
    FIELDINFO[3].fieldLocalName = "categoryBag";
    FIELDINFO[3].fieldModel = 1;
    FIELDINFO[3].fieldUri = "urn:uddi-org:api_v2";
    FIELDINFO[3].isSoapArray = false;
    FIELDINFO[3].maxOccurs = 1;
    FIELDINFO[3].minOccurs = 0;
    FIELDINFO[3].nillable = false;
    FIELDINFO[3].soapArrayDimensions = 0;
    FIELDINFO[3].soapArrayItemTypeJavaName = null;
    FIELDINFO[3].soapArrayItemTypeLocalName = null;
    FIELDINFO[3].soapArrayItemTypeUri = null;
    FIELDINFO[3].typeJavaName = "com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.KeyedReference[]";
    FIELDINFO[3].typeLocalName = "categoryBag";
    FIELDINFO[3].typeUri = "urn:uddi-org:api_v2";
    FIELDINFO[3].getterMethod = "getCategoryBag";
    FIELDINFO[3].setterMethod = "setCategoryBag";
    FIELDINFO[3].checkMethod = "hasCategoryBag";
    // Field 4
    FIELDINFO[4].defaultValue = null;
    FIELDINFO[4].fieldJavaName = "TModelBag";
    FIELDINFO[4].fieldLocalName = "tModelBag";
    FIELDINFO[4].fieldModel = 1;
    FIELDINFO[4].fieldUri = "urn:uddi-org:api_v2";
    FIELDINFO[4].isSoapArray = false;
    FIELDINFO[4].maxOccurs = 1;
    FIELDINFO[4].minOccurs = 0;
    FIELDINFO[4].nillable = false;
    FIELDINFO[4].soapArrayDimensions = 0;
    FIELDINFO[4].soapArrayItemTypeJavaName = null;
    FIELDINFO[4].soapArrayItemTypeLocalName = null;
    FIELDINFO[4].soapArrayItemTypeUri = null;
    FIELDINFO[4].typeJavaName = "java.lang.String[]";
    FIELDINFO[4].typeLocalName = "tModelBag";
    FIELDINFO[4].typeUri = "urn:uddi-org:api_v2";
    FIELDINFO[4].getterMethod = "getTModelBag";
    FIELDINFO[4].setterMethod = "setTModelBag";
    FIELDINFO[4].checkMethod = "hasTModelBag";
    // Field 5
    FIELDINFO[5].defaultValue = null;
    FIELDINFO[5].fieldJavaName = "DiscoveryURLs";
    FIELDINFO[5].fieldLocalName = "discoveryURLs";
    FIELDINFO[5].fieldModel = 1;
    FIELDINFO[5].fieldUri = "urn:uddi-org:api_v2";
    FIELDINFO[5].isSoapArray = false;
    FIELDINFO[5].maxOccurs = 1;
    FIELDINFO[5].minOccurs = 0;
    FIELDINFO[5].nillable = false;
    FIELDINFO[5].soapArrayDimensions = 0;
    FIELDINFO[5].soapArrayItemTypeJavaName = null;
    FIELDINFO[5].soapArrayItemTypeLocalName = null;
    FIELDINFO[5].soapArrayItemTypeUri = null;
    FIELDINFO[5].typeJavaName = "com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DiscoveryURL[]";
    FIELDINFO[5].typeLocalName = "discoveryURLs";
    FIELDINFO[5].typeUri = "urn:uddi-org:api_v2";
    FIELDINFO[5].getterMethod = "getDiscoveryURLs";
    FIELDINFO[5].setterMethod = "setDiscoveryURLs";
    FIELDINFO[5].checkMethod = "hasDiscoveryURLs";
  }


  // Returns model Group Type
  public int _getModelType() {
    return 3;
  }

  // Attribute field
  private java.lang.String _a_Generic;
  private boolean _a_hasGeneric;
  // set method
  public void setGeneric(java.lang.String _Generic) {
    this._a_Generic = _Generic;
    this._a_hasGeneric = true;
  }
  // clear method
  public void clearGeneric(java.lang.String _Generic) {
    this._a_hasGeneric = false;
  }
  // get method
  public java.lang.String getGeneric() {
    return _a_Generic;
  }
  // has method
  public boolean hasGeneric() {
    return _a_hasGeneric;
  }

  // Attribute field
  private int _a_MaxRows;
  private boolean _a_hasMaxRows;
  // set method
  public void setMaxRows(int _MaxRows) {
    this._a_MaxRows = _MaxRows;
    this._a_hasMaxRows = true;
  }
  // clear method
  public void clearMaxRows(int _MaxRows) {
    this._a_hasMaxRows = false;
  }
  // get method
  public int getMaxRows() {
    return _a_MaxRows;
  }
  // has method
  public boolean hasMaxRows() {
    return _a_hasMaxRows;
  }

  // Element field
  private java.lang.String[] _f_FindQualifiers;

  private boolean _f_hasFindQualifiers;
  public void setFindQualifiers(java.lang.String[] _FindQualifiers) {
    this._f_FindQualifiers = _FindQualifiers;
    this._f_hasFindQualifiers = true;
  }
  public java.lang.String[] getFindQualifiers() {
    return this._f_FindQualifiers;
  }
  public boolean hasFindQualifiers() {
    return this._f_hasFindQualifiers;
  }
  public void clearFindQualifiers() {
    this._f_hasFindQualifiers = false;
  }

  // Element field
  private com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.Name[] _f_Name = new com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.Name[0];
  public void setName(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.Name[] _Name) {
    this._f_Name = _Name;
  }
  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.Name[] getName() {
    return _f_Name;
  }

  // Element field
  private com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.KeyedReference[] _f_IdentifierBag;

  private boolean _f_hasIdentifierBag;
  public void setIdentifierBag(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.KeyedReference[] _IdentifierBag) {
    this._f_IdentifierBag = _IdentifierBag;
    this._f_hasIdentifierBag = true;
  }
  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.KeyedReference[] getIdentifierBag() {
    return this._f_IdentifierBag;
  }
  public boolean hasIdentifierBag() {
    return this._f_hasIdentifierBag;
  }
  public void clearIdentifierBag() {
    this._f_hasIdentifierBag = false;
  }

  // Element field
  private com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.KeyedReference[] _f_CategoryBag;

  private boolean _f_hasCategoryBag;
  public void setCategoryBag(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.KeyedReference[] _CategoryBag) {
    this._f_CategoryBag = _CategoryBag;
    this._f_hasCategoryBag = true;
  }
  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.KeyedReference[] getCategoryBag() {
    return this._f_CategoryBag;
  }
  public boolean hasCategoryBag() {
    return this._f_hasCategoryBag;
  }
  public void clearCategoryBag() {
    this._f_hasCategoryBag = false;
  }

  // Element field
  private java.lang.String[] _f_TModelBag;

  private boolean _f_hasTModelBag;
  public void setTModelBag(java.lang.String[] _TModelBag) {
    this._f_TModelBag = _TModelBag;
    this._f_hasTModelBag = true;
  }
  public java.lang.String[] getTModelBag() {
    return this._f_TModelBag;
  }
  public boolean hasTModelBag() {
    return this._f_hasTModelBag;
  }
  public void clearTModelBag() {
    this._f_hasTModelBag = false;
  }

  // Element field
  private com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DiscoveryURL[] _f_DiscoveryURLs;

  private boolean _f_hasDiscoveryURLs;
  public void setDiscoveryURLs(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DiscoveryURL[] _DiscoveryURLs) {
    this._f_DiscoveryURLs = _DiscoveryURLs;
    this._f_hasDiscoveryURLs = true;
  }
  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DiscoveryURL[] getDiscoveryURLs() {
    return this._f_DiscoveryURLs;
  }
  public boolean hasDiscoveryURLs() {
    return this._f_hasDiscoveryURLs;
  }
  public void clearDiscoveryURLs() {
    this._f_hasDiscoveryURLs = false;
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