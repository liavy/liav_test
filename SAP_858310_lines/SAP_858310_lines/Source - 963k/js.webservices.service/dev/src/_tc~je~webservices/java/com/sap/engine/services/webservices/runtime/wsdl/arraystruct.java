package com.sap.engine.services.webservices.runtime.wsdl;

import com.sap.engine.lib.descriptors.ws04vi.*;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */
class ArrayStruct {

  static final String DEFAULT_STRING_SIMPLE_TYPE  =  "String";

  String javaClassName;
  String namespace;
  String complexTypeName;
  String componentComplexTypeName;
  String componentComplexTypeNS;
  String componentJavaClass;

  String itemName = SchemaConvertor.DEFAULT_ITEM_VALUE;

  int dimention; //in case of conveted(simple type) arrays
  TypeState componentState; //in case of complex arrays

  ArrayStruct(TableState table, VirtualInterfaceTypes.Choice1[] allTypes) throws Exception {
    this.complexTypeName = getComplexTypeName(table, allTypes);
    this.javaClassName = table.getTypeOriginal().getComplexTypeReference().getName();

    this.namespace = SchemaConvertor.getArrayNS(table, allTypes);

    this.componentJavaClass = table.getTableLineType().getComplexTypeReference().getName();
    this.componentState = SchemaConvertor.findTypeStateByOriginalName(this.componentJavaClass, allTypes);

    if (this.componentState instanceof TableState) {
      this.componentComplexTypeNS = SchemaConvertor.getArrayNS((TableState) this.componentState, allTypes);
      this.componentComplexTypeName = getComplexTypeName((TableState) this.componentState, allTypes);
    } else {
      this.componentComplexTypeNS = SchemaInfo.getUriFromTypeState(this.componentState, allTypes, null);
      this.componentComplexTypeName = SchemaConvertor.getComplexTypeName(this.componentState, allTypes);
    }

    //loading item value.
    TypeState typeState = SchemaInfo.getArrayComponent(table, allTypes);
    itemName = SchemaConvertor.getJavaClassName(typeState.getName()); //the name of the inner most type

    if (table.getTableSoapExtensionTable() != null) {
      if (table.getTableSoapExtensionTable().getSoapExtensionTable().getItemLabel() != null) {
        itemName = table.getTableSoapExtensionTable().getSoapExtensionTable().getItemLabel();
      }
    }
  }

  ArrayStruct(String inlineType, int dim, ConvertedTableReferenceSoapExtensionTable soapExtTable) throws WSDLCreationException {
//    this.dimention = simpleTypesTable.getDimension();
//    String baseClassName = simpleTypesTable.getConvertedTableReferenceLineType().getConvertedTypeReference().getName();
    this.dimention = dim;
    String baseClassName = inlineType;

    this.javaClassName = getJavaArrayDeclaration(baseClassName, dimention);

    this.namespace = SchemaInfo.getUriFromClassName(SchemaConvertor.DEFAULT_NAMESPACE, null);
    this.componentComplexTypeNS = this.namespace;

    //in case of java.lang.String only
    if (baseClassName.equals(String.class.getName())) {
      this.complexTypeName = getComplexTypeName(DEFAULT_STRING_SIMPLE_TYPE, this.dimention);
      this.componentComplexTypeName = getComplexTypeName(DEFAULT_STRING_SIMPLE_TYPE, this.dimention - 1);
      itemName = DEFAULT_STRING_SIMPLE_TYPE;
    } else {
      this.complexTypeName = getComplexTypeName(baseClassName, this.dimention);
      this.componentComplexTypeName = getComplexTypeName(baseClassName, this.dimention - 1);
      itemName = baseClassName;
    }

    this.componentJavaClass = baseClassName + getSuffixBrackets(this.dimention - 1);

//    if (simpleTypesTable.hasConvertedTableReferenceSoapExtensionTable()) {
//      if (simpleTypesTable.getConvertedTableReferenceSoapExtensionTable().getSoapExtensionTable().hasItemLabel()) {
//        itemName = simpleTypesTable.getConvertedTableReferenceSoapExtensionTable().getSoapExtensionTable().getItemLabel();
//      }
//    }
    if (soapExtTable != null) {
      if (soapExtTable.getSoapExtensionTable().getItemLabel() != null) {
        itemName = soapExtTable.getSoapExtensionTable().getItemLabel();
      }
    }
  }

  ArrayStruct(ConvertedTableReferenceState simpleTypesTable) throws WSDLCreationException {
    this(simpleTypesTable.getConvertedTableReferenceLineType().getConvertedTypeReference().getName(), simpleTypesTable.getDimension().intValue(), simpleTypesTable.getConvertedTableReferenceSoapExtensionTable());
  }

  //Used in Structure field references.
  ArrayStruct(TypeReferenceState typeRefState, VirtualInterfaceTypes.Choice1[] allTypes) throws Exception {
    //for compatibility with old version
    if (typeRefState instanceof ConvertedTypeReferenceState) {
      ConvertedTypeReferenceState cnv = (ConvertedTypeReferenceState) typeRefState;

      this.javaClassName = cnv.getOriginalType();
      this.dimention = getDimention(this.javaClassName);

      String baseClassName = cutArrayBrackets(this.javaClassName);
      this.namespace = SchemaInfo.getUriFromClassName(SchemaConvertor.DEFAULT_NAMESPACE, null);
      this.componentComplexTypeNS = this.namespace;

      //in case of java.lang.String only
      if (baseClassName.equals(String.class.getName())) {
        this.complexTypeName = getComplexTypeName(DEFAULT_STRING_SIMPLE_TYPE, this.dimention);
        this.componentComplexTypeName = getComplexTypeName(DEFAULT_STRING_SIMPLE_TYPE, this.dimention - 1);
        itemName = DEFAULT_STRING_SIMPLE_TYPE;
      } else {
        this.complexTypeName = getComplexTypeName(baseClassName, this.dimention);
        this.componentComplexTypeName = getComplexTypeName(baseClassName, this.dimention - 1);
        itemName = baseClassName;
      }

      this.componentJavaClass = baseClassName + getSuffixBrackets(this.dimention - 1);

    } else if (typeRefState instanceof ComplexTypeReferenceState) {
      ComplexTypeReferenceState complex = (ComplexTypeReferenceState) typeRefState;

      this.javaClassName = complex.getName();
      this.dimention = getDimention(this.javaClassName);

      String baseClassName = cutArrayBrackets(this.javaClassName);
      TypeState innerMostType = SchemaConvertor.findTypeStateByOriginalName(baseClassName, allTypes);
      this.namespace = SchemaInfo.getUriFromTypeState(innerMostType, allTypes, null);
      this.componentComplexTypeNS = this.namespace;

      this.complexTypeName = getComplexTypeName(innerMostType.getName(), this.dimention);

      itemName = innerMostType.getName();

      if (this.dimention > 1) {
        this.componentComplexTypeName = getComplexTypeName(innerMostType.getName(), this.dimention - 1);
      } else {
        this.componentComplexTypeName = innerMostType.getName();
      }
      this.componentJavaClass = baseClassName + getSuffixBrackets(this.dimention - 1);
    }
  }

  ArrayStruct(ArrayStruct parent) throws WSDLCreationException {
    this.dimention = parent.dimention - 1;

    this.javaClassName = parent.componentJavaClass;
    this.complexTypeName = parent.componentComplexTypeName;
    this.namespace = parent.componentComplexTypeNS;
    this.componentComplexTypeNS = this.namespace;

    String baseClassName = cutArrayBrackets(this.javaClassName);
    this.componentComplexTypeName = getComplexTypeName(baseClassName, this.dimention - 1);
    this.componentJavaClass = baseClassName + getSuffixBrackets(this.dimention - 1);

    this.itemName = parent.itemName;
  }

  private String getJavaArrayDeclaration(String javaClass, int dimention) {

    while (dimention > 0) {
      javaClass += "[]";
      dimention--;
    }

    return javaClass;
  }

  private String getComplexTypeName(TableState table, VirtualInterfaceTypes.Choice1[] allTypes) throws Exception {
    if (table.getTypeSoapExtensionType() != null && table.getTypeSoapExtensionType().getSoapExtensionType().getLocalname() != null) {
      return SchemaConvertor.filterNCName(table.getTypeSoapExtensionType().getSoapExtensionType().getLocalname(), table);
    }

//    boolean hasNS = false;
//    if (table.hasTypeSoapExtensionType() && table.getTypeSoapExtensionType().getSoapExtensionType().hasNamespace()
//                                         && table.getTypeSoapExtensionType().getSoapExtensionType().getNamespace().length() > 0) {
//      hasNS = true;
//    }
//
//    TypeState innerMost = SchemaInfo.getArrayComponent(table, allTypes);
//
//    if ((! hasNS) && innerMost.hasTypeSoapExtensionType()
//                  && innerMost.getTypeSoapExtensionType().getSoapExtensionType().hasNamespace()
//                  && innerMost.getTypeSoapExtensionType().getSoapExtensionType().getNamespace().length() > 0) {
//      hasNS = true;
//    }
//
//    if (hasNS) {
//      return SchemaConvertor.filterNCName(table.getName(), table);
//    }

    String complexTypeName = SchemaConvertor.getComplexTypeName(table, allTypes);
    String javaClassName = table.getTypeOriginal().getComplexTypeReference().getName();

    int dimention = getDimention(javaClassName);
//      String cutted = cutArrayBrackets(complexTypeName);
    complexTypeName = getComplexTypeName(complexTypeName, dimention);

    return complexTypeName;
  }

  private String cutArrayBrackets(String arrayName) {
    int delim = arrayName.indexOf("[]");
    return arrayName.substring(0, delim);
  }

  private int getDimention(String originalArray) {
    int delim = originalArray.indexOf("[]");
    return (originalArray.length() - delim) / 2;
  }

  private String getSuffixBrackets(int nom) {
    String res = "";

    for (int i = 0; i < nom; i++) {
       res += "[]";
    }
    return res;
  }

  private String getComplexTypeName(String baseName, int dim) throws WSDLCreationException {
    if (dim > 1) {
      return SchemaConvertor.filterNCName("ArrayOf" + baseName + dim + "D", null);
    }

    return SchemaConvertor.filterNCName("ArrayOf" + baseName, null);
  }

  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (! (obj instanceof ArrayStruct)) return false;
    ArrayStruct sObj = (ArrayStruct) obj;

    if (this.complexTypeName.equals(sObj.complexTypeName) && this.namespace.equals(sObj.namespace)) {
      return true;
    }

    return false;
  }

  public int hashCode() {
    return super.hashCode();
  }
}

