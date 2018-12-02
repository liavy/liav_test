package com.sap.engine.services.webservices.runtime.wsdl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Element;

import com.sap.engine.lib.descriptors.ws04vi.ComplexTypeReferenceState;
import com.sap.engine.lib.descriptors.ws04vi.ConvertedTypeReferenceState;
import com.sap.engine.lib.descriptors.ws04vi.ElementaryState;
import com.sap.engine.lib.descriptors.ws04vi.FaultState;
import com.sap.engine.lib.descriptors.ws04vi.FieldState;
import com.sap.engine.lib.descriptors.ws04vi.FieldTypeReference;
import com.sap.engine.lib.descriptors.ws04vi.FunctionState;
import com.sap.engine.lib.descriptors.ws04vi.InlinePrimitiveTableReferenceState;
import com.sap.engine.lib.descriptors.ws04vi.ParameterMappedTypeReference;
import com.sap.engine.lib.descriptors.ws04vi.ParameterState;
import com.sap.engine.lib.descriptors.ws04vi.SoapExtensionTypeState;
import com.sap.engine.lib.descriptors.ws04vi.StructureState;
import com.sap.engine.lib.descriptors.ws04vi.TableState;
import com.sap.engine.lib.descriptors.ws04vi.TypeOriginal;
import com.sap.engine.lib.descriptors.ws04vi.TypeReferenceState;
import com.sap.engine.lib.descriptors.ws04vi.TypeSoapExtensionType;
import com.sap.engine.lib.descriptors.ws04vi.TypeState;
import com.sap.engine.lib.descriptors.ws04vi.VirtualInterfaceState;
import com.sap.engine.lib.descriptors.ws04vi.VirtualInterfaceTypes;
import com.sap.engine.lib.xml.Symbols;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.runtime.RuntimeExceptionConstants;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class SchemaConvertor {

    //***********Prefixes and postfixes*******************
  public static final String SCHEMA_PREFIX            =   StandardTypes.SCHEMA_PREFIX + ":";
  public static final String SOAPENC_PREFIX           =  "soapenc:";
  public static final String TNS_STANDALONE           =  "tns";
  public static final String TNS_PREFIX               =  "tns:";
  public static final String ARRAY_PREFIX             =  "ArrayOf";
  public static final String ARRAY_DIMENTION_POSTFIX  =  "_";
  public static final String LITERAL_NS_SUFFIX        =  "literal";
  public static final String ENCODED_NS_SUFFIX        =  "encoded";

    //***********Elements*******************
  public static final String SCHEMA_COMPLEXTYPE       =  SCHEMA_PREFIX + "complexType";
  public static final String SCHEMA_COMPLEXCONTENT    =  SCHEMA_PREFIX + "complexContent";
  public static final String SCHEMA_RESTRICTION       =  SCHEMA_PREFIX + "restriction";
  public static final String SCHEMA_ATTRIBUTE         =  SCHEMA_PREFIX + "attribute";
  public static final String SCHEMA_ALL               =  SCHEMA_PREFIX + "all";
  public static final String SCHEMA_ELEMENT           =  SCHEMA_PREFIX + "element";
  public static final String SCHEMA_EXTENSION         =  SCHEMA_PREFIX + "extension";
  public static final String SCHEMA_SEQUENCE          =  SCHEMA_PREFIX + "sequence";
  public static final String SCHEMA_ANY_ELEMENT       =  "any";
  public static final String SCHEMA_PROCESSCONTENTS   =  "processContents";
  public static final String SCHEMA_NAMESPACE         =  "namespace";

     //*************Attributes****************
  public static final String SCHEMA_NAME              =  "name";
  public static final String SCHEMA_REF               =  "ref";
  public static final String SCHEMA_BASE              =  "base";
  public static final String SCHEMA_TYPE              =  "type";
  public static final String SCHEMA_NILLABLE          =  "nillable";
  public static final String SCHEMA_MAXOCCURS         =  "maxOccurs";
  public static final String SCHEMA_MINOCCURS         =  "minOccurs";
  public static final String SCHEMA_UNBOUNDED         =  "unbounded";
  public static final String SCHEMA_USE               =  "use";
  public static final String SCHEMA_USE_REQUIRED      =  "required";
  public static final String SCHEMA_USE_OPTIONAL      =  "optional";
  public static final String SCHEMA_ANYTYPE           =  "anyType";

    //**************Standard Attributes Values****
  public static final String SOAPENC_ARRAY_TYPE       =  SOAPENC_PREFIX + "arrayType";
  public static final String SOAPENC_ARRAY            =  SOAPENC_PREFIX + "Array";
  public static final String WSDL_ARRAY_TYPE          =  "wsdl:arrayType";

  public static final String DEFAULT_ITEM_VALUE       =  "item";

  public static final String DEFAULT_NAMESPACE        =  "java.lang.xxx";

  public static final String SIMPLE_CONTENT           =  "simpleContent";

  public static final String _ANY_FEATURE             =  "_" + SCHEMA_ANY_ELEMENT;

  //the targetNamespace of the schema for java.util types
  static final String JAVA_UTIL_SCHEMA_NAMESPACE   =  "java:sap/standard";

  private Element schemaElement;
  private ArrayList alreadyGenerated;
  private HashMap nameMapping;
  private Hashtable schemas = new Hashtable(); //SchemaInfo instances key-schema targetNamespace
  private VirtualInterfaceTypes.Choice1[] allTypes; //the types in the VI types section
  private ArrayList arrayTypes = new ArrayList();  //ArrayStruct objects denoting arrays
  private SchemaInfo currentSchema;

  private boolean isLiteralArrayRepresentation = false;
  private boolean isElFormDfQualified = true; //by default the schema is qualified
  private String currentSuffix = ENCODED_NS_SUFFIX;    //encoded or literal

  private StructureState thrState;

  boolean isSerUsed;
  boolean isThrOrExcUsed;

  public SchemaConvertor() {
    this(new NameMappingRegistry());
  }

  public SchemaConvertor(NameMappingRegistry nameMappingGegistry) {
    alreadyGenerated = new ArrayList();
    this.nameMapping = new HashMap();
  }

  public HashMap getJavaToQNameMappings() {
    return (HashMap) nameMapping.clone();
  }

  /**
  * Builds schema.
  *
  * @param typeState the target class.
  * @return Element containing all complexType elements which fully describe the targer class.
  */
  private Element parseTypeState(TypeState typeState, boolean exclude) throws Exception {
    String originalType = getOriginalType(typeState);

    //System.out.println("Try to parse: " + originalType);
    if (alreadyGenerated.contains(originalType)) return null;
    if (StandardTypes.isStandardType(originalType)) return null;

    //in case of array
    if (typeState instanceof TableState) {
      this.arrayTypes.add(new ArrayStruct((TableState) typeState, this.allTypes));
      return null;
    }

    //generate nothing for Object
    if (originalType.equals(java.lang.Object.class.getName()) ||
        exclude && originalType.equals(java.lang.Throwable.class.getName())) {
      return null;
    }

    //generate nothing for Serializable in case it is not used
    if (exclude && originalType.equals(java.io.Serializable.class.getName()) && (! isSerUsed)) {
      return null;
    }
    //generate nothing for Exception and Throwable in case it is not used
    if (exclude && originalType.equals(java.lang.Exception.class.getName())) {
      if (! isThrOrExcUsed) {
        return null;
      }
    }
//    //in case of some base java types miss processing.
//    if (originalType.equals(java.rmi.RemoteException.class.getName()) || originalType.equals(Throwable.class.getName()) ||
//        originalType.equals(Object.class.getName()) || originalType.equals("java.lang.StackTraceElement") ||
//        originalType.equals(java.io.Serializable.class.getName())) {
//      return null;
//    }
//    System.out.println("Parsering: " + originalType);
    alreadyGenerated.add(originalType);

    //in case of java.util type
    if (StandardTypes.isJavaUtilType(originalType)) {
      appendJavaUtilComplexType(originalType);
      this.nameMapping.put(originalType, new QName(JAVA_UTIL_SCHEMA_NAMESPACE, originalType.substring(originalType.lastIndexOf(".")+1)));
      return null;
    }

    DataFieldList list = obtainDataFieldList(typeState);
    parseStructure(list);
    for (int i = 0; i < list.size(); i++) {
    //        System.out.println("Processing listItem: " + list.item(i));
      if (list.item(i).getTypeState() != null) {
        parseTypeState(list.item(i).getTypeState(), exclude);
      }
    }

    return schemaElement;
  }

  public DOMSource[] parseInEncodedMode(VirtualInterfaceState vInterface) throws Exception {
    //loading isQualifed attrib value
    //if (vInterface.isSetVirtualInterfaceSoapExtensionVI()) {
    if (vInterface.getVirtualInterfaceSoapExtensionVI() != null) {
      isElFormDfQualified = vInterface.getVirtualInterfaceSoapExtensionVI().getSoapExtensionVI().isUseNamespaces();
    }

    //parsing the Types section
    //if (vInterface.isSetVirtualInterfaceTypes()) {
    if (vInterface.getVirtualInterfaceTypes() != null) {
      this.allTypes = vInterface.getVirtualInterfaceTypes().getChoiceGroup1();

      checkForSpecialUsage(vInterface.getVirtualInterfaceFunctions().getFunction());
      for (int i = 0; i < allTypes.length; i++) {
        parseTypeState(extractTypeStateFromChoice(allTypes[i]), true);
      }
      if (isThrOrExcUsed) {
        parseTypeState(this.thrState, false);
      }
    }

    //searching for arrays
    FunctionState functions[] = vInterface.getVirtualInterfaceFunctions().getFunction();
    this.parseFunctions(functions);
    this.parseArrays();
    DOMSource[] schemas = this.createDomSourcesFromSchemas();

    //clearing the schemas for next invoke of parseXXX
    this.schemas.clear();
    this.alreadyGenerated.clear();

    return schemas;
  }

  public DOMSource[] parseInEncodedMode(ArrayList typesList) throws Exception {
    DOMSource[] temp;
    DOMSource[] currentGenerated;
    DOMSource[] resultArray = new DOMSource[0];

    int listSize = typesList.size();
    for (int i = 0; i < listSize; i++) {
      currentGenerated = parseInEncodedMode((VirtualInterfaceState) typesList.get(i));
      temp = new DOMSource[currentGenerated.length + resultArray.length];
      System.arraycopy(resultArray, 0, temp, 0, resultArray.length);
      System.arraycopy(currentGenerated, 0, temp, resultArray.length, currentGenerated.length);
      resultArray = temp;
    }

    return resultArray;
  }

  public DOMSource[] parseInLiteralMode(VirtualInterfaceState vi) throws Exception {
    this.isLiteralArrayRepresentation = true;
    this.currentSuffix = LITERAL_NS_SUFFIX;
    return parseInEncodedMode(vi);
  }

  public DOMSource[] parseInLiteralMode(ArrayList typesList) throws Exception {
    this.isLiteralArrayRepresentation = true;
    this.currentSuffix = LITERAL_NS_SUFFIX;
    return this.parseInEncodedMode(typesList);
  }

  public void clearState() {
    this.isLiteralArrayRepresentation = false;
    this.currentSuffix = ENCODED_NS_SUFFIX;
    this.allTypes = null;

    this.nameMapping.clear();
    this.schemas.clear();
    this.arrayTypes.clear();
    alreadyGenerated.clear();
  }


  private void parseStructure(DataFieldList list) throws Exception {

    //parse in simpleContent mode
    if (list.isSimpleContent) {
      parserSimpleContent(list);
      return;
    }

    Element temp;
    DataField tempField;


    String schemaUri = SchemaInfo.getUriFromTypeState(list.getTypeState(), allTypes, currentSuffix);
    SchemaInfo tempSch = (SchemaInfo) schemas.get(schemaUri);

    if (tempSch == null) {
      tempSch = new SchemaInfo(schemaUri, isElFormDfQualified);
      schemas.put(schemaUri, tempSch);
    }

    this.schemaElement = tempSch.getSchemaElement();
    currentSchema = tempSch;

    Element complexType = schemaElement.getOwnerDocument().createElementNS(NS.XS, SCHEMA_COMPLEXTYPE);
    String nameValue = getComplexTypeName(list.getTypeState(), allTypes);
    complexType.setAttributeNS("", SCHEMA_NAME, nameValue);
    this.nameMapping.put(getOriginalType(list.getTypeState()), new QName(currentSchema.getTargetNamespace(), nameValue));

    Element seqOrAll;
    if (list.isUnordered) {
      seqOrAll = schemaElement.getOwnerDocument().createElementNS(NS.XS, SCHEMA_ALL);
    } else {
      seqOrAll = schemaElement.getOwnerDocument().createElementNS(NS.XS, SCHEMA_SEQUENCE);
    }
    //check whether this complexType has a parent and the parent is not java.lang.Object
    TypeState listTState = list.getTypeState();
    if (hasParentForProcesing(listTState)) {
      String baseClassJavaName = listTState.getParentTypeName();
      TypeState baseTState = findTypeStateByOriginalName(baseClassJavaName, allTypes);

      if (baseTState == null) {
        baseTState = getAdditionTypeState(baseClassJavaName);
      }
      if (baseTState == null || (baseTState instanceof TableState)) {
        throw new WSDLCreationException(RuntimeExceptionConstants.INCORRECT_BASETYPE_FOUND, new Object[]{(baseTState == null)?null:baseTState.getName(), listTState.getName(), baseClassJavaName});
      }

      String baseTypeUri = SchemaInfo.getUriFromTypeState(baseTState, allTypes, currentSuffix);
      String baseTypePref = tempSch.getPrefixForUri(baseTypeUri);
      String baseTypeCTName = getComplexTypeName(baseTState, allTypes);

      Element complexContent = schemaElement.getOwnerDocument().createElementNS(NS.XS, SCHEMA_COMPLEXCONTENT);
      Element extensionBase = schemaElement.getOwnerDocument().createElementNS(NS.XS, SCHEMA_EXTENSION);
      extensionBase.setAttributeNS("", SCHEMA_BASE, baseTypePref + ":" + baseTypeCTName);

      complexContent.appendChild(extensionBase);
      extensionBase.appendChild(seqOrAll);
      complexType.appendChild(complexContent);
    } else {
      complexType.appendChild(seqOrAll);
    }

    //for element entries processing
    for (int i = 0; i < list.size(); i++) {
      tempField = list.item(i);

      //in case of attribute miss it in the element generation
      if (tempField.isAttribute) {
        continue;
      }

      if (tempField.getFieldName().equals(_ANY_FEATURE)) { //_any field processing
        temp = schemaElement.getOwnerDocument().createElementNS(NS.XS, SCHEMA_ANY_ELEMENT);
        temp.setAttribute(SCHEMA_PROCESSCONTENTS, "lax");
        temp.setAttribute(SCHEMA_NAMESPACE, "##other");
        temp.setAttribute(SCHEMA_MINOCCURS, "0");
        temp.setAttribute(SCHEMA_MAXOCCURS, SCHEMA_UNBOUNDED);
      } else { //normal processing
        temp = schemaElement.getOwnerDocument().createElementNS(NS.XS, SCHEMA_ELEMENT);
        temp.setAttributeNS("", SCHEMA_NAME, tempField.getFieldName());

        //      System.out.println("JavatoSchema:parseCLass: This is tempField: " + tempField);
        if (setElementTypeAttributeValue(temp, tempField)) {
          list.remove(i--);
        }
      }
      seqOrAll.appendChild(temp);
    }

    //for attribute entries processing
    for (int i = 0; i < list.size(); i++) {
      tempField = list.item(i);

      //in case of attribute
      if (tempField.isAttribute) {
        temp = schemaElement.getOwnerDocument().createElementNS(NS.XS, SCHEMA_ATTRIBUTE);
        temp.setAttributeNS("", SCHEMA_NAME, tempField.getFieldName());
        setElementTypeAttributeValue(temp, tempField);
        list.remove(i--);
        if (temp.hasAttribute(SCHEMA_NILLABLE)) { //because for string nillable="true" is set
          temp.removeAttribute(SCHEMA_NILLABLE);
        }
        if (temp.hasAttribute(SCHEMA_MINOCCURS)) { //because for string minoccurs="0" is set
          temp.removeAttribute(SCHEMA_MINOCCURS);
        }

        if (StandardTypes.isNillableStandardType(tempField.getVirtualTypeName())) {
          temp.setAttributeNS("", SCHEMA_USE, SCHEMA_USE_OPTIONAL);
        } else {
          temp.setAttributeNS("", SCHEMA_USE, SCHEMA_USE_REQUIRED);
        }
        seqOrAll.getParentNode().appendChild(temp);
      }
    }

    schemaElement.appendChild(complexType);
  }

  private boolean hasParentForProcesing(TypeState typeState) throws Exception {
    //in case Exception come, set its parent to Throwable
    if (getOriginalType(typeState).equals(Exception.class.getName())) {
      typeState.setParentTypeName(Throwable.class.getName());
      return true;
    }

    if (isThrOrExcUsed && getOriginalType(typeState).equals(java.rmi.RemoteException.class.getName())) {
      typeState.setParentTypeName(Exception.class.getName());
      return true;
    }

    if (typeState.getParentTypeName() != null) {
      if ( typeState.getParentTypeName().equals(Object.class.getName())) {
        return false;
      }

      if (typeState.getParentTypeName().equals(Exception.class.getName())) {
        if (! isThrOrExcUsed) {
          return false;
        }
      }

      if (typeState.getParentTypeName().equals(java.io.Serializable.class.getName()) &&
              (! isSerUsed)) {
        return false;
      }

      return true;
    }

    return false;
  }

//  private boolean hasParentForProcesing(TypeState typeState) throws Exception {
//    if (typeState.isSetParentTypeName()) {
//      if ( typeState.getParentTypeName().equals(Object.class.getName()) ||
//           //typeState.getParentTypeName().equals(Exception.class.getName()) ||
//           typeState.getParentTypeName().equals(Throwable.class.getName()) ||
//           typeState.getParentTypeName().equals(java.io.Serializable.class.getName())) {
//        return false;
//      } else {
//        return true;
//      }
//    }
//
//    return false;
//  }

  private void parserSimpleContent(DataFieldList list) throws Exception {
    Element temp;
    DataField tempField;

    String schemaUri = SchemaInfo.getUriFromTypeState(list.getTypeState(), allTypes, currentSuffix);
    SchemaInfo tempSch = (SchemaInfo) schemas.get(schemaUri);

    if (tempSch == null) {
      tempSch = new SchemaInfo(schemaUri, isElFormDfQualified);
      schemas.put(schemaUri, tempSch);
    }

    this.schemaElement = tempSch.getSchemaElement();
    currentSchema = tempSch;

    Element complexType = schemaElement.getOwnerDocument().createElementNS(NS.XS, SCHEMA_COMPLEXTYPE);
    String nameValue = getComplexTypeName(list.getTypeState(), allTypes);
    complexType.setAttributeNS("", SCHEMA_NAME, nameValue);
    this.nameMapping.put(getOriginalType(list.getTypeState()), new QName(currentSchema.getTargetNamespace(), nameValue));


    Element simpleContent = schemaElement.getOwnerDocument().createElementNS(NS.XS, SCHEMA_PREFIX + SIMPLE_CONTENT);
    Element extension = null;

    for (int i = 0; i < list.size(); i++) {
      tempField = list.item(i);
      //create the extension base element
      if (tempField.getFieldName().equals(SIMPLE_CONTENT)) {
        extension = schemaElement.getOwnerDocument().createElementNS(NS.XS, SCHEMA_EXTENSION);
        extension.setAttributeNS("", SCHEMA_BASE, StandardTypes.getMapType(tempField.getVirtualTypeName()));
        simpleContent.appendChild(extension);
        list.remove(i--);
        break;
      }
    }


//    for (int i = 0; i < list.size(); i++) {
//      tempField = list.item(i);
//      if (tempField.getFieldName().equals("xml_lang")) {
//        temp = schemaElement.getOwnerDocument().createElementNS(NS.XS, SCHEMA_ATTRIBUTE);
//        schemaElement.setAttributeNS(NS.XMLNS, "xmlns:xml", NS.XML);
//        temp.setAttributeNS("", "ref", "xml:lang");
//        list.remove(i);
//        extension.appendChild(temp);
//        break;
//      }
//    }

    for (int i = 0; i < list.size(); i++) {
      tempField = list.item(i);
      temp = schemaElement.getOwnerDocument().createElementNS(NS.XS, SCHEMA_ATTRIBUTE);
      temp.setAttributeNS("", SCHEMA_NAME, tempField.getFieldName());

      //      System.out.println("JavatoSchema:parseCLass: This is tempField: " + tempField);
      temp.setAttributeNS("", SCHEMA_TYPE, StandardTypes.getMapType(tempField.getVirtualTypeName()));
      list.remove(i--);

      extension.appendChild(temp);
    }

    simpleContent.appendChild(extension);
    complexType.appendChild(simpleContent);
    schemaElement.appendChild(complexType);
  }

  private DataFieldList obtainDataFieldList(TypeState typeState) throws Exception {
    DataFieldList list = new DataFieldList(typeState);
    StructureState structureState;

//    if (typeState.getTypeOriginal().isSetFault()) { //adding this default for each faulf
      boolean put = true;
      if (typeState.getParentTypeName() != null) { //if the exception inherits other exception it will inherit the message field from there.
        if (! typeState.getParentTypeName().equals(Exception.class.getName())) {
          put = false;
        }
      }
      if (put && (! isThrOrExcUsed)) {
       // list.add(new DataField("message", "java.lang.String", "java.lang.String", null));
      }
//    }

//    if (getOriginalType(typeState).equals(java.rmi.RemoteException.class.getName())) { //adding this default for each faulf
//      if (! isThrOrExcUsed) {
//        list.add(new DataField("message", "java.lang.String", "java.lang.String", null));
//      }
//    }

    if (typeState instanceof ElementaryState) { //stop further processing
      return list;
    } else if (typeState instanceof StructureState) {
      structureState = (StructureState) typeState;
    } else {
      throw new WSDLCreationException(RuntimeExceptionConstants.VI_TYPES_INSTANCES_MISHMASH, new Object[]{typeState});
    }

    //search for unordered attribute and set its value
    //if (structureState.isSetStructureSoapExtensionStructure()) {
    if (structureState.getStructureSoapExtensionStructure() != null) {
      if (structureState.getStructureSoapExtensionStructure().getSoapExtensionStructure().isUnorderedFields()) {
        list.isUnordered = structureState.getStructureSoapExtensionStructure().getSoapExtensionStructure().isUnorderedFields();
      }
    }

    FieldState[] fields = null;
    if (structureState.getStructureFields() == null) {
      fields = new FieldState[0];
    } else {
      fields = structureState.getStructureFields().getField();
    }
    FieldTypeReference fTypeRef;
    DataField df = null;

    //SimpleContent check
    boolean found = false;
    for (int i = 0; i < fields.length; i++) {
      //mark it as simpleContent
      if (fields[i].getFieldName().equals(SIMPLE_CONTENT)) {
        if (found) {
          throw new WSDLCreationException(RuntimeExceptionConstants.INCORRECT_SIMPLECONTENT_DEFINITION, new Object[]{typeState.getName(), "more than one simpleContent"});
        }
        list.isSimpleContent = true;
        found = true;
      }
    }

    for (int i = 0; i < fields.length; i++) {
      fTypeRef = fields[i].getFieldTypeReference();

      //ensure that all fields of this structure are simple types
      if (list.isSimpleContent && !fTypeRef.isSetConvertedTypeReference()) {
        throw new WSDLCreationException(RuntimeExceptionConstants.INCORRECT_SIMPLECONTENT_DEFINITION, new Object[]{typeState.getName(), "not simple type"});
      }

      //simpleTypes
      if (fTypeRef.isSetConvertedTypeReference()) {
        ConvertedTypeReferenceState convType = fTypeRef.getConvertedTypeReference();
        df = new DataField(fields[i].getFieldName(), convType.getOriginalType(), convType.getName(), null);

        //in case it is attribute mark it
        //if (convType.isSetConvertedTypeReferenceSoapExtensionConvertedTypeRef()) {
        if (convType.getConvertedTypeReferenceSoapExtensionConvertedTypeRef() != null) {
          if (convType.getConvertedTypeReferenceSoapExtensionConvertedTypeRef().getSoapExtensionConvertedTypeRef().isIsAttribute()) {
            df.isAttribute = convType.getConvertedTypeReferenceSoapExtensionConvertedTypeRef().getSoapExtensionConvertedTypeRef().isIsAttribute();
          }
        }

        //to support the old style
        if (isArray(fTypeRef.getConvertedTypeReference())) {
          ArrayStruct aStr = new ArrayStruct(fTypeRef.getConvertedTypeReference(), allTypes);
          df.setArrayHolder(aStr);
          df.isAttribute = false; //an array cannot be attribute
          this.arrayTypes.add(aStr);
        }
      } else if (fTypeRef.isSetComplexTypeReference()) {
        TypeState tState = findTypeStateByOriginalName(fTypeRef.getComplexTypeReference().getName(), this.allTypes);
        if (tState == null) {

          //to support the old style(the one without describing the arrays in table elements)
          if (! isArray(fTypeRef.getComplexTypeReference())) { //in case of [][] array declaration
            throw new WSDLCreationException(RuntimeExceptionConstants.VI_TYPES_ORIGINALTYPE_ELEMENT_MISSING, new Object[]{fTypeRef.getComplexTypeReference().getName()});
          } else {
            ArrayStruct aStr = new ArrayStruct(fTypeRef.getComplexTypeReference(), allTypes);
            df = new DataField(fields[i].getFieldName(), aStr.javaClassName, aStr.componentComplexTypeName, null);
            df.setArrayHolder(aStr);
            this.arrayTypes.add(aStr);
            list.add(df);
            continue;
          }
        }

        df = new DataField(fields[i].getFieldName(), tState.getTypeOriginal().getComplexTypeReference().getName(), tState.getName(), tState);

        if (tState instanceof TableState) {
          df.setArrayHolder(new ArrayStruct((TableState) tState, allTypes));
          this.arrayTypes.add(df.getArrayHolder());
        }
      } else if (fTypeRef.isSetConvertedTableReference()) { //simpleType arrays
        ArrayStruct aStr = new ArrayStruct(fTypeRef.getConvertedTableReference());
        df = new DataField(fields[i].getFieldName(), aStr.javaClassName, aStr.componentComplexTypeName, null);
        df.setArrayHolder(aStr);
        this.arrayTypes.add(aStr);
      } else if (fTypeRef.isSetInlineComplexTableReference()) { //for unbounded complexTypes
        String complType = fTypeRef.getInlineComplexTableReference().getName();
        TypeState tState = findTypeStateByOriginalName(complType, this.allTypes);

        if (tState instanceof TableState) {
          tState = findTypeStateByOriginalName(((TableState) tState).getTableLineType().getComplexTypeReference().getName(), allTypes);
          df = new DataField(fields[i].getFieldName(), tState.getTypeOriginal().getComplexTypeReference().getName(), tState.getName(), tState);

          df.isUnbounded = true;

          if (tState instanceof TableState) {
            df.setArrayHolder(new ArrayStruct((TableState) tState, allTypes));
            this.arrayTypes.add(df.getArrayHolder());
          }
        } else {
          throw new WSDLCreationException(RuntimeExceptionConstants.VI_TYPES_ORIGINALTYPE_ELEMENT_MISSING, new Object[]{complType});
        }
      } else if (fTypeRef.isSetInlinePrimitiveTableReference()) { //for unbounded simple(int, String, ...)
        InlinePrimitiveTableReferenceState inLPrim = fTypeRef.getInlinePrimitiveTableReference();
        String inLType = inLPrim.getLineType();
        int dim = inLPrim.getDimension().intValue();

        if (dim == 1) {
          df = new DataField(fields[i].getFieldName(), inLType, inLType, null);
        } else {
          ArrayStruct aStr = new ArrayStruct(inLType, dim - 1, null);
          df = new DataField(fields[i].getFieldName(), aStr.javaClassName, aStr.componentComplexTypeName, null);
          df.setArrayHolder(aStr);
          this.arrayTypes.add(aStr);
        }
        df.isUnbounded = true;
      }

      list.add(df);
    }

    return list;
  }

  private boolean isArray(TypeReferenceState typeRefState) {
    if (typeRefState instanceof ConvertedTypeReferenceState) {
      if (((ConvertedTypeReferenceState) typeRefState).getOriginalType().endsWith("[]")) {
        return true;
      }
    }

    if (typeRefState instanceof ComplexTypeReferenceState) {
      if (typeRefState.getName().endsWith("[]")) {
        return true;
      }
    }

    return false;
  }

  private boolean setElementTypeAttributeValue(Element element, DataField df) throws Exception {

    //in case of unbounded set add unbounded to the element
    if (df.isUnbounded) {
      element.setAttributeNS("", SCHEMA_MAXOCCURS, "unbounded");
      element.setAttributeNS("", SCHEMA_MINOCCURS, Integer.toString(0));
    }

    if (df.getArrayHolder() != null) {
      ArrayStruct aStr = (ArrayStruct) df.getArrayHolder();
      if (aStr.componentJavaClass.equals(Byte.TYPE.getName())) {
        element.setAttributeNS("", SCHEMA_TYPE, StandardTypes.getMapType(aStr.javaClassName));
      } else {
        String prefix = currentSchema.getPrefixForUri(aStr.namespace);
        element.setAttributeNS("", SCHEMA_TYPE, prefix + ":" + aStr.complexTypeName);
      }
      element.setAttributeNS("", SCHEMA_NILLABLE, "true");
      element.setAttributeNS("", SCHEMA_MINOCCURS, Integer.toString(0));
      return true;
    }

    //in case of java.util class
    if (StandardTypes.isJavaUtilType(df.getOriginalTypeName())) {
      String prefix = currentSchema.getPrefixForUri(JAVA_UTIL_SCHEMA_NAMESPACE);
      element.setAttributeNS("", SCHEMA_TYPE, prefix + ":" + getJavaClassName(df.getOriginalTypeName()));
      element.setAttributeNS("", SCHEMA_NILLABLE, "true");
      element.setAttributeNS("", SCHEMA_MINOCCURS, Integer.toString(0));
      return true;
    }

    if (StandardTypes.isStandardType(df.getVirtualTypeName())) { //isSimpleType
      element.setAttributeNS("", SCHEMA_TYPE, StandardTypes.getMapType(df.getVirtualTypeName()));
      if (StandardTypes.isNillableStandardType(df.getVirtualTypeName())) {
        element.setAttributeNS("", SCHEMA_NILLABLE, "true");
        element.setAttributeNS("", SCHEMA_MINOCCURS, Integer.toString(0));
      }
      return true;
    } else {
      String ns = SchemaInfo.getUriFromTypeState(df.getTypeState(), allTypes, null);
      String prefix = currentSchema.getPrefixForUri(ns);
//      String prefix = currentSchema.getPrefixForUri(uri);
      element.setAttributeNS("", SCHEMA_TYPE, prefix + ":" + getComplexTypeName(df.getTypeState(), allTypes));
      element.setAttributeNS("", SCHEMA_NILLABLE, "true");
      element.setAttributeNS("", SCHEMA_MINOCCURS, Integer.toString(0));
    }
    return  false;
  }

  private boolean setElementTypeAttributeValue(Element element, ArrayStruct aStruct, boolean isEncodedArray) {
    if (StandardTypes.isStandardType(aStruct.componentJavaClass)) { //isSimpleType array
      if (!isEncodedArray) {
        element.setAttributeNS("", SCHEMA_TYPE, StandardTypes.getMapType(aStruct.componentJavaClass));
        element.setAttributeNS("", SCHEMA_NILLABLE, "true");
      } else {
        element.setAttributeNS(NS.WSDL, WSDL_ARRAY_TYPE, StandardTypes.getMapType(aStruct.componentJavaClass) + "[]");
      }
      return false;
    }

    String prefix = currentSchema.getPrefixForUri(aStruct.componentComplexTypeNS);
    if (!isEncodedArray) {
      element.setAttributeNS("", SCHEMA_TYPE, prefix + ":" + aStruct.componentComplexTypeName);
      element.setAttributeNS("", SCHEMA_NILLABLE, "true");
    } else {
      element.setAttributeNS(NS.WSDL, WSDL_ARRAY_TYPE, prefix + ":" + aStruct.componentComplexTypeName + "[]");
    }

    return  false;
  }

  /**
  * Takes the SchemaInfo elements, normalize them and create DOMSource for each.
  */
  private DOMSource[] createDomSourcesFromSchemas() throws WSDLCreationException {
//    Collection coll = this.schemas.values();
//    SchemaInfo schemaArray[] = (SchemaInfo[]) coll.toArray(new SchemaInfo[coll.size()]);
//    for (int i = 0; i < schemaArray.length; i++) {
//      resolveSchema(schemaArray[i], resoved);
//    }
//
//    if (resoved.size() != schemaArray.length) {
//      throw new WSDLCreationException(RuntimeExceptionConstants.RESOLVE_SCHEMA_IMPORTS_FAILS);
//    }


    ArrayList resoved = SchemaInfo.resolveSchemas(this.schemas);
    //ArrayList resoved = new ArrayList(this.schemas.values());

    DOMSource domSources[] = new DOMSource[resoved.size()];
    DOMSource tempSource;

    for (int i = 0; i < resoved.size(); i++) {
      Element normalizedSchema = ((SchemaInfo) resoved.get(i)).normalizeSchema();
      //tempSource = new DOMSource(((SchemaInfo) resoved.get(i)).normalizeSchema());
      //System.out.println("Schema ========== " + normalizedSchema);
      tempSource = new DOMSource(normalizedSchema);
      domSources[i] = tempSource;
    }

    return domSources;
  }

//  private void resolveSchema(SchemaInfo schema, ArrayList resolved) throws WSDLCreationException {
//    //in case it is resolved
//    if (resolved.contains(schema)) {
//      return;
//    }
//
//    schema.isVisited = true;
//
//    String keyNS;
//    SchemaInfo importedSchema;
//
//    Enumeration enumeration  = schema.imports.keys();
//    while (enumeration.isSetMoreElements()) {
//      keyNS = (String) enumeration.nextElement();
//
//      //pass the standard imports
//      if (keyNS.equals(schema.targetNamespace) || keyNS.equals(NS.XS)) {
//        continue;
//      }
//
//      importedSchema = (SchemaInfo) this.schemas.get(keyNS);
//
//      if (importedSchema == null) {
//        throw new WSDLCreationException(RuntimeExceptionConstants.RESOLVE_SCHEMA_IMPORTS_FAILS, new Object[]{keyNS, schema.targetNamespace});
//      }
//
//      //in case this import is already resolved
//      if (resolved.contains(importedSchema)) {
//        continue;
//      }
//
//      //in case of cicle
//      if (importedSchema.isVisited) {
//        resolved.add(schema); //temporary add to resolve importedSchema;
//      }
//
//      resolveSchema(importedSchema, resolved);
//    }
//
//    if (resolved.remove(schema)) { //there were a cicle
//      resolveSchema(schema, resolved);
//    } else {
//      //add it at the end
//      resolved.add(schema);
//    }
//  }

  private void parseFunctions(FunctionState[] functions) throws Exception {
    FunctionState function;
    ParameterState[] params;
    ParameterMappedTypeReference mtRef;

    for (int f = 0; f < functions.length; f++) {
      function = functions[f];
      //if (function.isSetFunctionIncomingParameters()) {
      if (function.getFunctionIncomingParameters() != null) {
        params = function.getFunctionIncomingParameters().getParameter();
        for (int i = 0; i < params.length; i++) {
          mtRef = params[i].getParameterMappedTypeReference();

          if (mtRef.isSetConvertedTableReference()) {
            arrayTypes.add(new ArrayStruct(mtRef.getConvertedTableReference()));
          }

          //to support the old style
          if (mtRef.isSetComplexTypeReference() && isArray(mtRef.getComplexTypeReference())) {
            //in case there is no TableType declared
            if (! (findTypeStateByOriginalName(mtRef.getComplexTypeReference().getName(), allTypes) instanceof TableState)) {
              arrayTypes.add(new ArrayStruct(mtRef.getComplexTypeReference(), allTypes));
            }
          }

          if (mtRef.isSetConvertedTypeReference() && isArray(mtRef.getConvertedTypeReference())) {
            arrayTypes.add(new ArrayStruct(mtRef.getConvertedTypeReference(), allTypes));
          }

          if (mtRef.isSetInlineComplexTableReference()) {
            throw new WSDLCreationException(RuntimeExceptionConstants.NOT_SUPPORTED_VIFUNCTION_PARAM_REF, new Object[]{"inlineComplexType"});
          }

          if (mtRef.isSetInlinePrimitiveTableReference()) {
            throw new WSDLCreationException(RuntimeExceptionConstants.NOT_SUPPORTED_VIFUNCTION_PARAM_REF, new Object[]{"inlinePrimitiveType"});
          }
        }
      }

      //if (function.isSetFunctionOutgoingParameters()) {
      if (function.getFunctionOutgoingParameters() != null) {
        params = function.getFunctionOutgoingParameters().getParameter();
        if (params.length != 1) {
          throw new WSDLCreationException(RuntimeExceptionConstants.VI_FUNCTION_OUT_PARAM_NUMBER, new Object[]{new Integer(1), function.getName()});
        }

        mtRef = params[0].getParameterMappedTypeReference();

        if (mtRef.isSetConvertedTableReference()) {
          arrayTypes.add(new ArrayStruct(mtRef.getConvertedTableReference()));
        }

        //to support the old style
        if (mtRef.isSetComplexTypeReference() && isArray(mtRef.getComplexTypeReference())) {
          //in case there is no TableType declared
          if (! (findTypeStateByOriginalName(mtRef.getComplexTypeReference().getName(), allTypes) instanceof TableState)) {
            arrayTypes.add(new ArrayStruct(mtRef.getComplexTypeReference(), allTypes));
          }
        }

        if (mtRef.isSetConvertedTypeReference() && isArray(mtRef.getConvertedTypeReference())) {
          arrayTypes.add(new ArrayStruct(mtRef.getConvertedTypeReference(), allTypes));
        }

        if (mtRef.isSetInlineComplexTableReference()) {
          throw new WSDLCreationException(RuntimeExceptionConstants.NOT_SUPPORTED_VIFUNCTION_PARAM_REF, new Object[]{"inlineComplexType"});
        }

        if (mtRef.isSetInlinePrimitiveTableReference()) {
          throw new WSDLCreationException(RuntimeExceptionConstants.NOT_SUPPORTED_VIFUNCTION_PARAM_REF, new Object[]{"inlinePrimitiveType"});
        }
      }
    }

  }

  private void appendJavaUtilComplexType(String javaUtilClName) throws Exception {
    SchemaInfo temp = (SchemaInfo) schemas.get(JAVA_UTIL_SCHEMA_NAMESPACE);
    if (temp == null) {
      temp = new SchemaInfo(JAVA_UTIL_SCHEMA_NAMESPACE, isElFormDfQualified);
      schemas.put(JAVA_UTIL_SCHEMA_NAMESPACE, temp);
    }
    Element parentSchemaEl = temp.getSchemaElement();

    String cmpTName = getJavaClassName(javaUtilClName);
    Element cmpTEl = parentSchemaEl.getOwnerDocument().createElementNS(NS.XS, SCHEMA_COMPLEXTYPE);
    cmpTEl.setAttributeNS("", SCHEMA_NAME, cmpTName);

    Element item = parentSchemaEl.getOwnerDocument().createElementNS(NS.XS, SCHEMA_ELEMENT);
    item.setAttributeNS("", SCHEMA_NAME, getJavaClassName(javaUtilClName));
    item.setAttributeNS("", SCHEMA_MINOCCURS, Integer.toString(0));
    item.setAttributeNS("", SCHEMA_MAXOCCURS, SCHEMA_UNBOUNDED);
    item.setAttributeNS("", SCHEMA_TYPE, SCHEMA_PREFIX + SCHEMA_ANYTYPE);

    Element seq = parentSchemaEl.getOwnerDocument().createElementNS(NS.XS, SCHEMA_SEQUENCE);
    seq.appendChild(item);
    cmpTEl.appendChild(seq);
    parentSchemaEl.appendChild(cmpTEl);
  }

  private void parseArrays() throws Exception {
    ArrayList generated = new ArrayList(this.arrayTypes.size());
    ArrayStruct aStr;

    for (int i = 0; i < this.arrayTypes.size(); i++) {
      aStr = (ArrayStruct) this.arrayTypes.get(i);
      if (generated.contains(aStr)) {//if is already generated
        continue;
      }

      //generate nothing for Byte[]
      if (aStr.componentJavaClass.equals(Byte.TYPE.getName())) {
        continue;
      }

      SchemaInfo temp = (SchemaInfo) schemas.get(aStr.namespace);
      if (temp == null) {
        temp = new SchemaInfo(aStr.namespace, isElFormDfQualified);
        schemas.put(aStr.namespace, temp);
      }
      this.schemaElement = temp.getSchemaElement();
      currentSchema = temp;

      if (isLiteralArrayRepresentation) {
        Element complexType = schemaElement.getOwnerDocument().createElementNS(NS.XS, SCHEMA_COMPLEXTYPE);
        String nameValue = aStr.complexTypeName;
        complexType.setAttributeNS("", SCHEMA_NAME, nameValue);
        nameMapping.put(aStr.javaClassName, new QName(currentSchema.getTargetNamespace(), nameValue));

        Element sequence = (Element) complexType.appendChild(schemaElement.getOwnerDocument().createElementNS(NS.XS, SCHEMA_SEQUENCE));
        Element arrayElement = (Element) sequence.appendChild(schemaElement.getOwnerDocument().createElementNS(NS.XS, SCHEMA_ELEMENT));
        arrayElement.setAttributeNS("", SCHEMA_MAXOCCURS, SCHEMA_UNBOUNDED);
        arrayElement.setAttributeNS("", SCHEMA_MINOCCURS, Integer.toString(0));
        arrayElement.setAttributeNS("", SCHEMA_NAME, aStr.itemName);

        setElementTypeAttributeValue(arrayElement, aStr, false);
        schemaElement.appendChild(complexType);
      } else {
        Element complexType = schemaElement.getOwnerDocument().createElementNS(NS.XS, SCHEMA_COMPLEXTYPE);
        String nameValue = aStr.complexTypeName;
        complexType.setAttributeNS("", SCHEMA_NAME, nameValue);
        nameMapping.put(aStr.javaClassName, new QName(currentSchema.getTargetNamespace(), nameValue));

        Element complexContent = (Element) complexType.appendChild(schemaElement.getOwnerDocument().createElementNS(NS.XS, SCHEMA_COMPLEXCONTENT));
        Element restriction = (Element) complexContent.appendChild(schemaElement.getOwnerDocument().createElementNS(NS.XS, SCHEMA_RESTRICTION));
        restriction.setAttributeNS(NS.XMLNS, "xmlns:soapenc", NS.SOAPENC);
        restriction.setAttributeNS("", SCHEMA_BASE, SOAPENC_ARRAY);
        Element attribute = (Element) restriction.appendChild(schemaElement.getOwnerDocument().createElementNS(NS.XS, SCHEMA_ATTRIBUTE));
        attribute.setAttributeNS("", SCHEMA_REF, SOAPENC_ARRAY_TYPE);

        setElementTypeAttributeValue(attribute, aStr, true);
        schemaElement.appendChild(complexType);
      }

      generated.add(aStr);
      //for multi dimentional arrays adding the inner arrays to be generated

      //for complex arrays
      if (aStr.componentState != null && (aStr.componentState instanceof TableState)) {
        this.arrayTypes.add(new ArrayStruct((TableState) aStr.componentState, allTypes));
      }

      //for simple arrays
      if (aStr.dimention > 1) {
        this.arrayTypes.add(new ArrayStruct(aStr));
      }
    }
  }

  static String getOriginalType(TypeState typeState) throws Exception {
    //if (typeState.isSetTypeOriginal()) {
    if (typeState.getTypeOriginal() != null) {
      if (typeState.getTypeOriginal().isSetComplexTypeReference()) {
        return typeState.getTypeOriginal().getComplexTypeReference().getName();
      } else {
        return typeState.getTypeOriginal().getFault().getName();
      }
    }
    return typeState.getName();
  }

  static TypeState findTypeStateByOriginalName(String originalName, VirtualInterfaceTypes.Choice1[] typeStates) throws Exception {
    TypeState tempState = null;
    for (int i = 0; i < typeStates.length; i++) {
      tempState = extractTypeStateFromChoice(typeStates[i]);
      String javaClass;
      if (tempState.getTypeOriginal().isSetComplexTypeReference()) {
        javaClass = tempState.getTypeOriginal().getComplexTypeReference().getName();
      } else if (tempState.getTypeOriginal().isSetFault()) {
        javaClass = tempState.getTypeOriginal().getFault().getName();
      } else {
        continue;
      }
      if (javaClass.equals(originalName)) {
        return tempState;
      }
    }

//    if (originalName.equals(Object.class.getName())) {
//      ElementaryState elem = new ElementaryState();
//      elem.setName(Object.class.getName());
//      TypeOriginal to = new TypeOriginal();
//      ComplexTypeReferenceState ctRef = new ComplexTypeReferenceState();
//      ctRef.setName(Object.class.getName());
//      to.setComplexTypeReference(ctRef);
//      elem.setTypeOriginal(to);
//
//      SoapExtensionTypeState soapExt = new SoapExtensionTypeState();
//      soapExt.setLocalname("Object");
//      soapExt.setName("SoapExtension");
//      soapExt.setNamespace("urn:java.lang");
//      TypeSoapExtensionType tsExt = new TypeSoapExtensionType();
//      tsExt.setSoapExtensionType(soapExt);
//      elem.setTypeSoapExtensionType(tsExt);
//      this.objState = elem;
//      return elem;
//    } else if (originalName.equals(Throwable.class.getName())) {
//      ElementaryState elem = new ElementaryState();
//      elem.setName(Throwable.class.getName());
//      elem.setParentTypeName(Object.class.getName());
//      TypeOriginal to = new TypeOriginal();
//      ComplexTypeReferenceState ctRef = new ComplexTypeReferenceState();
//      ctRef.setName(Throwable.class.getName());
//      to.setComplexTypeReference(ctRef);
//      elem.setTypeOriginal(to);
//
//      SoapExtensionTypeState soapExt = new SoapExtensionTypeState();
//      soapExt.setLocalname("Throwable");
//      soapExt.setName("SoapExtension");
//      TypeSoapExtensionType tsExt = new TypeSoapExtensionType();
//      tsExt.setSoapExtensionType(soapExt);
//      elem.setTypeSoapExtensionType(tsExt);
//      return elem;
//    }
    return null;
  }

  static TypeState extractTypeStateFromChoice(VirtualInterfaceTypes.Choice1 choice) throws Exception {
    if (choice.isSetElementary()) {
      return choice.getElementary();
    } else if (choice.isSetStructure()) {
      return choice.getStructure();
    } else if (choice.isSetTable()) {
      return choice.getTable();
    } else {
      return null;
    }
  }

  static String getArrayNS(TableState table, VirtualInterfaceTypes.Choice1[] allTypes) throws WSDLCreationException {
    //if (table.isSetTypeSoapExtensionType()) {
    if (table.getTypeSoapExtensionType() != null) {
      if (table.getTypeSoapExtensionType().getSoapExtensionType().getNamespace() != null) {
        String nsValue = table.getTypeSoapExtensionType().getSoapExtensionType().getNamespace();
        if (nsValue.length() > 0) {
          return nsValue;
        }
      }
    }

    try {
      TypeState typeState = SchemaInfo.getArrayComponent(table, allTypes);
      if (typeState == null) {
        throw new WSDLCreationException(RuntimeExceptionConstants.VI_TYPES_ORIGINALTYPE_ELEMENT_MISSING, new Object[]{table.getName()});
      }

      return SchemaInfo.getUriFromTypeState(typeState, allTypes, null);
    } catch (Exception e) {
      throw new WSDLCreationException(e);
    }
  }

  static String getComplexTypeName(TypeState state, VirtualInterfaceTypes.Choice1[] allTypes) throws Exception {
    //in case of java.util classes
    String origCl = getOriginalType(state);
    if (StandardTypes.isJavaUtilType(origCl)) {
      return getJavaClassName(origCl);
    }

//    System.out.println("SchemaConvertor getComplexTypeName for typeState: " + state.getName());

    //in case namespace is set use the VI provided information
    //if (state.isSetTypeSoapExtensionType()) {
    if (state.getTypeSoapExtensionType() != null) {
      if (state.getTypeSoapExtensionType().getSoapExtensionType().getLocalname() != null) { //support for the new localName attib
//        System.out.println("SchemaConvertor getComplexTypeName for typeState: " + state.getName() + " found localName: " + state.getTypeSoapExtensionType().getSoapExtensionType().getLocalname());
        return state.getTypeSoapExtensionType().getSoapExtensionType().getLocalname();
      }
      if (state.getTypeSoapExtensionType().getSoapExtensionType().getNamespace() != null) { //in case namespace is available use the
        String ns = state.getTypeSoapExtensionType().getSoapExtensionType().getNamespace();
        if (ns.length() > 0) {
//          System.out.println("SchemaConvertor getComplexTypeName for typeState: " + state.getName() + " two");
          return filterNCName(state.getName(), state);
        }
      }
    }

    if (state instanceof TableState) {
      state = SchemaInfo.getArrayComponent((TableState) state, allTypes);
    }

    if (state.getTypeOriginal().isSetComplexTypeReference()) {
      String s = getJavaClassName(state.getTypeOriginal().getComplexTypeReference().getName());
//      System.out.println("SchemaConvertor getComplexTypeName for typeState: " + s + " three");
      return filterNCName(s, state);
    } else {
      String s = getJavaClassName(state.getTypeOriginal().getFault().getName());
//      System.out.println("SchemaConvertor getComplexTypeName for typeState: " + s + " four");
      return filterNCName(s, state);
    }
  }

  static String getJavaClassName(String fullName) {
    int ind = fullName.lastIndexOf(".");
    if (ind == -1) {  // no package
      return fullName;
    } else {
      return fullName.substring(ind + 1);
    }
  }

  static String filterNCName(String s, TypeState type) throws WSDLCreationException {
    if (Symbols.isNCName(s)) {
      return s;
    }

    throw new WSDLCreationException(RuntimeExceptionConstants.INCORRECT_NCNAME_CHARACTER_FOUND, new Object[]{s, (type != null) ? type.getName() : "null"});
  }

  private TypeState getAdditionTypeState(String originalName) {
    if (this.thrState != null) {
      return this.thrState;
    }

    if (originalName.equals(Throwable.class.getName())) {
      StructureState strS = new StructureState();
      strS.setName(Throwable.class.getName());
      strS.setParentTypeName(Object.class.getName());
      TypeOriginal to = new TypeOriginal();
      ComplexTypeReferenceState ctRef = new ComplexTypeReferenceState();
      ctRef.setName(Throwable.class.getName());
      to.setComplexTypeReference(ctRef);
      strS.setTypeOriginal(to);

//      FieldState fMessage = new FieldState();
//      fMessage.setName("0");
//      fMessage.setFieldName("message");
//      ConvertedTypeReferenceState strRef = new ConvertedTypeReferenceState();
//      strRef.setName(String.class.getName());
//      strRef.setOriginalType(String.class.getName());
//      FieldTypeReference fTypeRef = new FieldTypeReference();
//      fTypeRef.setConvertedTypeReference(strRef);
//      fMessage.setFieldTypeReference(fTypeRef);
//      StructureFields sf = new StructureFields();
//      sf.setField(new FieldState[]{fMessage});
//      strS.setStructureFields(sf);

      SoapExtensionTypeState soapExt = new SoapExtensionTypeState();
      soapExt.setLocalname("Throwable");
      soapExt.setName("SoapExtension");
      soapExt.setNamespace("urn:java.lang");
      TypeSoapExtensionType tsExt = new TypeSoapExtensionType();
      tsExt.setSoapExtensionType(soapExt);
      strS.setTypeSoapExtensionType(tsExt);
      this.thrState = strS;
      return strS;
    }

    return null;
  }

  private void checkForSpecialUsage(FunctionState[] functions) throws Exception {
    checkTypesSection();
    checkFunctions(functions);
    getAdditionTypeState(Throwable.class.getName());
  }

  private void checkTypesSection() throws Exception {
    TypeState cur;
    String type = null;
    for (int i = 0; i < allTypes.length; i++) {
      cur = extractTypeStateFromChoice(allTypes[i]);
      if (cur instanceof StructureState) {
        StructureState s = (StructureState) cur;
        FieldState[] fields = null;
        if (s.getStructureFields() == null) {
          fields = new FieldState[0];
        } else {
          fields = s.getStructureFields().getField();
        }
        FieldState field;
        for (int f = 0; f < fields.length; f++) {
          field = fields[f];
          if (field.getFieldTypeReference().isSetComplexTypeReference()) {
            type = field.getFieldTypeReference().getComplexTypeReference().getName();
          } else if (field.getFieldTypeReference().isSetFault()) {
            type = field.getFieldTypeReference().getFault().getName();
          } else if (field.getFieldTypeReference().isSetInlineComplexTableReference()) {
            type = field.getFieldTypeReference().getInlineComplexTableReference().getName();
          }

          checkType(type);
        }
      } else if (cur instanceof TableState) {
        TableState table = (TableState) cur;
        if (table.getTableLineType().isSetComplexTypeReference()) {
          type = table.getTableLineType().getComplexTypeReference().getName();
        } else if (table.getTableLineType().isSetFault()) {
          type = table.getTableLineType().getFault().getName();
        } else if (table.getTableLineType().isSetInlineComplexTableReference()) {
          type = table.getTableLineType().getInlineComplexTableReference().getName();
        }

        checkType(type);
      }
    }
  }

  private void checkFunctions(FunctionState[] functions) throws Exception {
    FunctionState function;
    ParameterState params[];
    for (int i = 0; i < functions.length; i++) {
      function = functions[i];

      //if (function.isSetFunctionIncomingParameters()) {
      if (function.getFunctionIncomingParameters() != null) {
        params = function.getFunctionIncomingParameters().getParameter();
        checkParameters(params);
      }
      //if (function.isSetFunctionOutgoingParameters()) {
      if (function.getFunctionOutgoingParameters() != null) {
        params = function.getFunctionOutgoingParameters().getParameter();
        checkParameters(params);
      }
      //if (function.isSetFunctionFaults()) {
      if (function.getFunctionFaults() != null) {
        FaultState[] fFaults = function.getFunctionFaults().getFault();
        for (int ff = 0; ff < fFaults.length; ff++) {
          checkType(fFaults[ff].getName());
        }
      }
    } //end function loop
  }

  private void checkParameters(ParameterState[] params) throws Exception {
    ParameterState param;
    for (int p = 0; p < params.length; p++) {
      param = params[p];
      String type = null;
      if (param.getParameterMappedTypeReference().isSetComplexTypeReference()) {
        type = param.getParameterMappedTypeReference().getComplexTypeReference().getName();
      } else if (param.getParameterMappedTypeReference().isSetFault()) {
        type = param.getParameterMappedTypeReference().getFault().getName();
      } else if (param.getParameterMappedTypeReference().isSetInlineComplexTableReference()) {
        type = param.getParameterMappedTypeReference().getInlineComplexTableReference().getName();
      }
      checkType(type);
    }
  }

  private void checkType(String type) {
    if (type != null) {
      if (type.equals(java.io.Serializable.class.getName())) {
        this.isSerUsed = true;
      } else if (type.equals(java.lang.Throwable.class.getName()) ||
              type.equals(java.lang.Exception.class.getName())) {
        isThrOrExcUsed = true;
      }
    }
  }


//  public static void main(String args[]) throws Exception {
//////    VirtualInterfaceState viState = VInterfaceParser.getVInterface(new java.io.FileInputStream("D:/eclipse/workspace/XMLLangEJB/bin/XMLLangVI.videf"));
//    //VirtualInterfaceState viState = VInterfaceParser.getVInterface(new java.io.FileInputStream("D:/box/vvvlado/WSAdminVI8.videf"));
//    VirtualInterfaceState viState = (VirtualInterfaceState)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04VI).parse(new java.io.FileInputStream("C:/usr/sap/R39/JC72/j2ee/ATS/tests/sap.com~tc~je~webservices~tests~ats/basetests/ExceptionTest/service/ExceptionVI.videf"));
//    System.out.println("Session Vi OK");
////    VirtualInterfaceState viState1 = VInterfaceParser.getVInterface(new java.io.FileInputStream("D:/work/examples/prt/TestJavaProject1/pack1/Test1.videf"));
////    viState1.getVirtualInterfaceEndpointReference();
////    
////     System.out.println("Test Vi OK");
////    VirtualInterfaceState viState = VInterfaceParser.getVInterface(new java.io.FileInputStream("d:/work/wstest/vigen/outputVi_retailer.xml"));
////   VirtualInterfaceState viState = VInterfaceParser.getVInterface(new java.io.FileInputStream("D:/work/wsTest/complex1/webservices/newvi/complex1vi.videf"));
////    VirtualInterfaceState viState = VInterfaceParser.getVInterface(new java.io.FileInputStream("D:/Germany/VIs/22.04.2003/TypeNameAsReference.videf"));
//
//
//    SchemaConvertor schConvertor = new SchemaConvertor();
//    DOMSource[] sources = schConvertor.parseInLiteralMode(viState);
////    DOMSource[] sources = schConvertor.parseInEncodedMode(viState);
//
//    System.out.println("Namemappings:  ");
//    System.out.println(schConvertor.getJavaToQNameMappings());
//    SchemaToJavaGenerator generator = new SchemaToJavaGenerator();
//    for (int i = 0; i < sources.length; i++) {
//      System.out.println("This is the schema: " + sources[i].getNode());
//      generator.addSchemaSource(sources[i]);
//    }
//
//
//    generator.generateAll(new File("D:/box/vvvlado/"), "res", true);
////    compiler.compileExternal(".;D:/work/wsTest/complex1/webservices/test", new File("D:/work/wsTest/complex1/webservices/test"));
//
//  }

}