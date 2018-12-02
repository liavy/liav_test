package com.sap.engine.services.webservices.espbase.client.dynamic.types.impl;

import java.rmi.UnmarshalException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.engine.lib.schema.Constants;
import com.sap.engine.lib.schema.components.Annotation;
import com.sap.engine.lib.schema.components.AttributeDeclaration;
import com.sap.engine.lib.schema.components.AttributeUse;
import com.sap.engine.lib.schema.components.Base;
import com.sap.engine.lib.schema.components.ComplexTypeDefinition;
import com.sap.engine.lib.schema.components.ElementDeclaration;
import com.sap.engine.lib.schema.components.Facet;
import com.sap.engine.lib.schema.components.ModelGroup;
import com.sap.engine.lib.schema.components.ModelGroupDefinition;
import com.sap.engine.lib.schema.components.Particle;
import com.sap.engine.lib.schema.components.Schema;
import com.sap.engine.lib.schema.components.SimpleTypeDefinition;
import com.sap.engine.lib.schema.components.TypeDefinitionBase;
import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.espbase.client.dynamic.content.GenericObject;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAnnotation;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAny;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAppInfo;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAttribute;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAttributeInfoItem;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DBaseType;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DComplexType;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DDocumentation;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DElement;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DField;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DGroup;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DSimpleContent;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DSimpleType;
import com.sap.engine.services.webservices.jaxrpc.encoding.DeserializerBase;
import com.sap.engine.services.webservices.jaxrpc.encoding.ExtendedTypeMapping;
import com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo;
import com.sap.engine.services.webservices.jaxrpc.encoding.GeneratedComplexType;
import com.sap.engine.services.webservices.jaxrpc.encoding.GeneratedModelGroup;
import com.sap.engine.services.webservices.jaxrpc.encoding.SOAPDeserializationContext;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingImpl;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;
import com.sap.engine.services.webservices.jaxrpc.encoding.XMLTypeMapping;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaMappingConstants;
import com.sap.engine.services.webservices.jaxrpc.util.StaticQNameCache;

public class MetadataLoader {
  
  private static Hashtable STANDARD_TYPE_NAME_TO_METADATA_MAP;
  private static Hashtable<QName,DBaseType> BUILTIN_TYPE_TO_METADATA_MAP;
  private static int STRUCTURE_METADATA_ID = 0;
  private static final String CHOICE_GROUP_NAME_SUFFIKS = "ChoiceGroup";
  private static final String SEQUENCE_GROUP_NAME_SUFFIKS = "SequenceGroup";
  private static final String ALL_GROUP_NAME_SUFFIKS = "AllGroup";
  
  static {
    initTypeNameToDefinedMetadataMap();
    BUILTIN_TYPE_TO_METADATA_MAP = new Hashtable<QName,DBaseType>();
  }
  
  private static void initTypeNameToDefinedMetadataMap() {
    STANDARD_TYPE_NAME_TO_METADATA_MAP = new Hashtable();
    initTypeNameToDefinedMetadataMap(SchemaMappingConstants.SCHEMA_STANDART_TYPES[0]);
    initTypeNameToDefinedMetadataMap(SchemaMappingConstants.SCHEMA_STANDART_TYPES[1]);
    initTypeNameToDefinedMetadataMap(SchemaMappingConstants.SCHEMA_STANDART_TYPES[2]);
    initTypeNameToDefinedMetadataMap(SchemaMappingConstants.SCHEMA_STANDART_TYPES[3]);
    initTypeNameToDefinedMetadataMap(SchemaMappingConstants.SCHEMA_STANDART_TYPES[4]);
    initTypeNameToDefinedMetadataMap(SchemaMappingConstants.SCHEMA_STANDART_TYPES[5]);
  }
  
  private static void initTypeNameToDefinedMetadataMap(String typeName) { 
    DComplexType standardDComplexType = createDComplexType_Standard(typeName);
    STANDARD_TYPE_NAME_TO_METADATA_MAP.put(typeName, standardDComplexType);
  }
  
  private static DElement createDElement(DAnnotation dAnnotation,DAnnotation topLevelAnnotation, DAttributeInfoItem[] dAttribInfoItems, QName typeName, QName scopeName, QName name, String defaultValue, int minOccurs, int maxOccurs, boolean isNillable) {
    DElementImpl dElement = new DElementImpl();
    dElement.setAnnotation(dAnnotation);
    dElement.setTopLevelAnnotation(topLevelAnnotation);
    dElement.setAttributeInfoItems(dAttribInfoItems);
    initDField(dElement, typeName, scopeName, name, DField.ELEMENT);
    dElement.setDefaultValue(defaultValue);
    dElement.setMaxOccurs(maxOccurs);
    dElement.setMinOccurs(minOccurs);
    dElement.setNillable(isNillable);
    return(dElement);
  }
  
  private static DAttribute createDAttribute(DAnnotation dAnnotation,DAnnotation topLevelAnnotation, DAttributeInfoItem[] dAttribInfoItems, QName typeName, QName scopeName, QName name, String defaultValue, boolean isRequired) {
    DAttributeImpl dAttrib = new DAttributeImpl();
    dAttrib.setAnnotation(dAnnotation);
    dAttrib.setTopLevelAnnotation(topLevelAnnotation);
    dAttrib.setAttributeInfoItems(dAttribInfoItems);
    initDField(dAttrib, typeName, scopeName, name, DField.ATTRIBUTE);
    dAttrib.setDefaultValue(defaultValue);
    dAttrib.setRequired(isRequired);
    return(dAttrib);
  }
  
  private static DGroup createDGroup(QName scopeName, QName name, int type, int minOccurs, int maxOccurs, DField[] dFields, String id) {
    DGroupImpl dGroup = new DGroupImpl();
    initDField(dGroup, null, scopeName, name, type);
    dGroup.setMinOccurs(minOccurs);
    dGroup.setMaxOccurs(maxOccurs);
    dGroup.setFields(dFields);
    dGroup.setID(id);
    return(dGroup);
  }
  
  private static DSimpleContent createDSimpleContent(QName typeName, QName scopeName) {
    DSimpleContentImpl dSimpleContent = new DSimpleContentImpl();
    initDField(dSimpleContent, typeName, scopeName, GenericObject.SIMPLE_FIELD, DField.SIMPLE);
    return(dSimpleContent);
  }
  
  private static DComplexType createDComplexType(DAnnotation dAnnotation,DAnnotation topLevelAnnotation, DAttributeInfoItem[] dAttribInfoItems, int type, QName typeName, QName baseTypeName, boolean isBuiltIn, boolean isAnonimous, boolean isAbstract, boolean isMixedContent, DSimpleContent dSimpleContent, DField[] dAttribs, DField[] dFields, String id) {
    DComplexTypeImpl dComplexType = new DComplexTypeImpl();
    initDBaseType(dComplexType, dAnnotation,topLevelAnnotation, dAttribInfoItems, type, typeName, baseTypeName, isBuiltIn, isAnonimous);
    dComplexType.setAbstract(isAbstract);
    dComplexType.setMixedContent(isMixedContent);
    dComplexType.setSimpleContent(dSimpleContent);
    dComplexType.setAttributes(dAttribs);
    dComplexType.setFields(dFields);
    dComplexType.setID(id);
    return(dComplexType);
  }
  
  private static DSimpleType createDSimpleType(DAnnotation dAnnotation,DAnnotation topLevelAnnotation, DAttributeInfoItem[] dAttribInfoItems, int type, QName typeName, QName baseTypeName, boolean isBuiltIn, boolean isAnonimous, com.sap.engine.services.webservices.espbase.client.dynamic.types.Facet[] facets) {
    DSimpleTypeImpl dSimpleType = new DSimpleTypeImpl();
    initDBaseType(dSimpleType, dAnnotation,topLevelAnnotation, dAttribInfoItems, type, typeName, baseTypeName, isBuiltIn, isAnonimous);
    dSimpleType.setFacets(facets);
    return(dSimpleType);
  }
  
  private static void initDBaseType(DBaseTypeImpl dBaseType, DAnnotation dAnnotation,DAnnotation topLevelAnnotation, DAttributeInfoItem[] dAttribInfoItems, int type, QName typeName, QName baseTypeName, boolean isBuiltIn, boolean isAnonimous) {
    dBaseType.setAnnotation(dAnnotation);
    dBaseType.setTopLevelAnnotation(topLevelAnnotation);
    dBaseType.setAttributeInfoItems(dAttribInfoItems);
    dBaseType.setTypeName(typeName);
    dBaseType.setBaseTypeName(baseTypeName);
    dBaseType.setBuiltIn(isBuiltIn);
    dBaseType.setAnonymous(isAnonimous);
    dBaseType.setType(type);
  }
    
  private static void loadAnnotationContents(Element schemaAnnotationNode, Vector<Element> appInfos, Vector<Element> documentations ) {
    Node schemaChild = schemaAnnotationNode.getFirstChild();
    while (schemaChild != null) {
      if (schemaChild.getNodeType() == Node.ELEMENT_NODE) {
        Element schemaChildElement = (Element) schemaChild;
        // This is appInfo node.
        if (NS.XS.equals(schemaChildElement.getNamespaceURI()) && "appinfo".equals(schemaChildElement.getLocalName())) {
          appInfos.add(schemaChildElement);
        }          
        // This is documentation node.
        if (NS.XS.equals(schemaChildElement.getNamespaceURI()) && "documentation".equals(schemaChildElement.getLocalName())) {
          documentations.add(schemaChildElement);
        }  
      }
      schemaChild = schemaChild.getNextSibling();
    }
  }
  
  /**
   * Extracts DAnnotation information from xsd:schema node.
   * @param schemaNode
   * @return
   */
  private static DAnnotation createDAnnotation(Element schemaNode, Map contextCache) {
    if (schemaNode == null) {
      return null;
    }    
    DAnnotationImpl dAnnotation = (DAnnotationImpl) contextCache.get(schemaNode);
    if (dAnnotation != null) {
      return dAnnotation;
    }
    Node schemaChild = schemaNode.getFirstChild();
    while (schemaChild != null) {
      if (schemaChild.getNodeType() == Node.ELEMENT_NODE) {
        Element schemaChildElement = (Element) schemaChild;
        // This is annotation node.
        if (NS.XS.equals(schemaChildElement.getNamespaceURI()) && "annotation".equals(schemaChildElement.getLocalName())) {
          dAnnotation = new DAnnotationImpl();
          Vector<Element> appInfos = new Vector<Element>();
          Vector<Element> documentations = new Vector<Element>();
          loadAnnotationContents(schemaChildElement,appInfos,documentations);
          dAnnotation.setAppInfos(createDAppInfos(appInfos));
          dAnnotation.setDocumentations(createDDocumentations(documentations));       
          contextCache.put(schemaNode,dAnnotation);
          break;
        }
      }
      schemaChild = schemaChild.getNextSibling();
    }
    return dAnnotation;
  }  
  
  private static DAnnotation createDAnnotation(Annotation annotation) {
    DAnnotationImpl dAnnotation = null;
    if(annotation != null) {
      dAnnotation = new DAnnotationImpl();
      dAnnotation.setAttributeInfoItems(createDAttributeInfoItems(annotation.getAttributes()));
      dAnnotation.setAppInfos(createDAppInfos(annotation.getAppInformations()));
      dAnnotation.setDocumentations(createDDocumentations(annotation.getUserInformations()));
    }
    return(dAnnotation);
  }

  private static DDocumentation[] createDDocumentations(Vector<Element> docNodes) {
    DDocumentation[] dDocs = new DDocumentation[docNodes.size()];
    for(int i = 0; i < docNodes.size(); i++) {
      Element docNode = docNodes.get(i);
      dDocs[i] = createDDocumentation(docNode);
    }
    return(dDocs);
  }

  private static DDocumentation createDDocumentation(Element docNode) {
    DDocumentationImpl dDoc = new DDocumentationImpl(); 
    initDAnnotationItem(dDoc, docNode);
    Node langAttr = docNode.getAttributeNodeNS(NS.XML, "lang");
    dDoc.setLanguage(langAttr == null ? null : langAttr.getNodeValue());
    return(dDoc);
  }
  
  private static DAppInfo[] createDAppInfos(Vector<Element> appInfoNodes) {
    DAppInfo[] dAppInfos = new DAppInfo[appInfoNodes.size()];
    for(int i = 0; i < appInfoNodes.size(); i++) {
      Element appInfoNode = appInfoNodes.get(i);
      dAppInfos[i] = createDAppInfo(appInfoNode);
    }
    return(dAppInfos);
  }
  
  private static DAppInfo createDAppInfo(Element appInfoNode) {
    DAppInfoImpl dAppInfo = new DAppInfoImpl(); 
    initDAnnotationItem(dAppInfo, appInfoNode);
    return(dAppInfo);
  }      
  
  private static void initDAnnotationItem(DAnnotationItemImpl dAnnotationItem, Element annotationItemNode) {
    Node sourceAttr = annotationItemNode.getAttributeNode("source");   
    dAnnotationItem.setSource(sourceAttr == null ? null : sourceAttr.getNodeValue());        
    NodeList temp = new NodeListSimpleImpl(annotationItemNode.getChildNodes());
    dAnnotationItem.setContent(temp);
  }
  
  private static DAttributeInfoItem[] createDAttributeInfoItems(Node associatedDOMNode) {
    DAttributeInfoItem[] dAttribInfoItems = new DAttributeInfoItem[0]; 
    if(associatedDOMNode != null) {
      NamedNodeMap namedNodeMap = associatedDOMNode.getAttributes();
      dAttribInfoItems = new DAttributeInfoItem[namedNodeMap.getLength()];
      for(int i = 0; i < namedNodeMap.getLength(); i++) {
        Node attr = namedNodeMap.item(i);
        dAttribInfoItems[i] = createDAttributeInfoItem(attr);
      }
    }
    return(dAttribInfoItems);
  }
  
  private static DAttributeInfoItem[] createDAttributeInfoItems(Vector attribs) {
    DAttributeInfoItem[] dAttribInfoItems = new DAttributeInfoItem[attribs.size()];
    for(int i = 0; i < attribs.size(); i++) {
      Node attr = (Node)(attribs.get(i));
      dAttribInfoItems[i] = createDAttributeInfoItem(attr);
    }
    return(dAttribInfoItems);
  }
  
  private static DAttributeInfoItem createDAttributeInfoItem(Node attr) {
    DAttributeInfoItemImpl dAttribItem = new DAttributeInfoItemImpl();
    //dAttribItem.setName(new QName(attr.getNamespaceURI(), attr.getLocalName()));
    dAttribItem.setName(StaticQNameCache.get(attr.getNamespaceURI(), attr.getLocalName()));
    dAttribItem.setValue(attr.getNodeValue());
    return(dAttribItem);
  }
  
  private static DAny createDAny(QName scopeName, int minOccurs, int maxOccurs) {
    DAnyImpl dAny = new DAnyImpl();
    initDField(dAny, StaticQNameCache.get(Constants.SCHEMA_COMPONENTS_NS, Constants.TYPE_ANY_TYPE_NAME), scopeName, GenericObject.ANY_FIELD, DField.ANY);
    dAny.setMaxOccurs(maxOccurs);
    dAny.setMinOccurs(minOccurs);
    return(dAny);
  }
  
  private static void initDField(DFieldImpl dField, QName fieldTypeName, QName scopeName, QName dFieldName, int fieldType) {
    dField.setFieldName(dFieldName);
    dField.setFieldScope(scopeName);
    dField.setFieldType(fieldTypeName);
    dField.setType(fieldType);
  }
  
  private static com.sap.engine.services.webservices.espbase.client.dynamic.types.Facet createFacet(String name, String value, Object objectValue, int intValue) {
    com.sap.engine.services.webservices.espbase.client.dynamic.types.Facet facet = new com.sap.engine.services.webservices.espbase.client.dynamic.types.Facet();
    facet.setName(name);
    facet.setValue(value);
    facet.setObjectValue(objectValue);
    facet.setIntValue(intValue);
    return(facet);
  }
  
  private static DComplexType createDComplexType_Standard(String typeName) {
    QName typeQName = StaticQNameCache.get(SchemaMappingConstants.SAP_STANDART, typeName);
    return(createDComplexType(null,
                              null,
                              new DAttributeInfoItem[0],
                              DComplexType.SEQUENCE,
                              typeQName,
                              StaticQNameCache.get(Constants.SCHEMA_COMPONENTS_NS, Constants.TYPE_ANY_TYPE_NAME),
                              false,
                              false,
                              false,
                              false,
                              null,
                              new DField[0],
                              new DField[]{createDElement_Standard(typeQName)},
                              null));
  }
  
  private static DElement createDElement_Standard(QName elementName) {
    return(createDElement(null, 
                          null,
                          null,
                          StaticQNameCache.get(Constants.SCHEMA_COMPONENTS_NS, Constants.TYPE_ANY_TYPE_NAME),
                          elementName,
                          elementName,
                          null,
                          0,
                          Integer.MAX_VALUE,
                          false));
  }
  
  public synchronized static void loadMetaData(TypeMappingRegistryImpl typeMappingRegistry, Schema schema) throws Exception {
    loadBuiltInTypeMetadatas(typeMappingRegistry.getDefaultTypeMappingImpl(), schema);
    loadCustomTypeMetadatas(typeMappingRegistry.getDefaultTypeMappingImpl(), schema);
    finish();
  }
  
  private static void finish() {
    STRUCTURE_METADATA_ID = 0;
  }
  
  private static void loadBuiltInTypeMetadatas(TypeMappingImpl typeMapping, Schema schema) throws Exception {
    Map contextCache = new HashMap();
    TypeDefinitionBase[] builtInTypeDefinitions = schema.getBuiltInTypeDefinitions();
    for(int i = 0; i < builtInTypeDefinitions.length; i++) {
      TypeDefinitionBase builtInTypeDefinition = builtInTypeDefinitions[i];      
      QName typeName = determineDBaseTypeName(builtInTypeDefinition);
      DBaseType dBaseType = null;
      synchronized (BUILTIN_TYPE_TO_METADATA_MAP) {
        dBaseType = BUILTIN_TYPE_TO_METADATA_MAP.get(typeName);
        if (dBaseType == null) {
          dBaseType = builtInTypeDefinition instanceof SimpleTypeDefinition ? createDSimpleType(typeMapping, (SimpleTypeDefinition)builtInTypeDefinition, typeName, null,contextCache) : createDComplexType_AnyType();
          BUILTIN_TYPE_TO_METADATA_MAP.put(typeName,dBaseType);
        }
      }
      typeMapping.registerTypeMetadata(dBaseType);
    }
  }
  
  
  
  private static DComplexType createDComplexType_AnyType() {
    QName typeName = StaticQNameCache.get(Constants.SCHEMA_COMPONENTS_NS, Constants.TYPE_ANY_TYPE_NAME);
    return(createDComplexType(null,
                              null,
                              new DAttributeInfoItem[0],
                              DComplexType.SEQUENCE,
                              typeName,
                              typeName,
                              true,
                              false,
                              false,
                              true,
                              null,
                              new DField[0],
                              new DField[]{createDField_AnyType()},
                              null));
  }

  private static DField createDField_AnyType() {
    return(createDAny(StaticQNameCache.get(Constants.SCHEMA_COMPONENTS_NS, Constants.TYPE_ANY_TYPE_NAME),
                      0,
                      Integer.MAX_VALUE));
  }
  
  private static void loadCustomTypeMetadatas(TypeMappingImpl typeMapping, Schema schema) throws Exception {
    Map contextCache = new HashMap();
    Base[] allComponents = schema.getAllComponentsAsArray();
    for(int i = 0; i < allComponents.length; i++) {
      Base component = allComponents[i];
      if(component instanceof TypeDefinitionBase) {
        loadTypeMetadata(typeMapping, (TypeDefinitionBase)component,contextCache);
      }
    }
  }
  
  private static void loadTypeMetadata(TypeMappingImpl typeMapping, TypeDefinitionBase typeDefinition,Map contextCache) throws Exception {
    DBaseType dBaseType = null;
    String typeDefinitionNamespace = typeDefinition.getTargetNamespace();
    String typeDefinitionName = typeDefinition.getName();
    if(typeDefinitionNamespace.equals(SchemaMappingConstants.SAP_STANDART)) {
      dBaseType = (DBaseType)(STANDARD_TYPE_NAME_TO_METADATA_MAP.get(typeDefinitionName));
    } else if(SchemaMappingConstants.SOAP_ENCODING.equals(typeDefinitionNamespace) && !isBypassedSoapEncodingType(typeDefinitionName)) {
      dBaseType = createDBaseType_SoapEncoding(typeMapping, typeDefinition,contextCache); 
    } else {
      dBaseType = createDBaseType(typeMapping, typeDefinition,contextCache);
    }
    typeMapping.registerTypeMetadata(dBaseType);
  }

  private static DBaseType createDBaseType_SoapEncoding(TypeMappingImpl typeMapping, TypeDefinitionBase typeDefinition,Map contextCache) throws Exception {
    QName typeName = determineDBaseTypeName(typeDefinition);
    DBaseType dBaseType = null; 
    synchronized (BUILTIN_TYPE_TO_METADATA_MAP) {
      dBaseType = BUILTIN_TYPE_TO_METADATA_MAP.get(typeName);
      if (dBaseType == null) {
        QName baseType = null;
        if(typeDefinition instanceof ComplexTypeDefinition) { // ComplexType with simple content 
          typeDefinition = ((ComplexTypeDefinition)typeDefinition).getContentTypeSimpleTypeDefinition();
          baseType = determineDBaseTypeName(typeDefinition);
        }
        dBaseType = createDSimpleType(typeMapping, (SimpleTypeDefinition)typeDefinition, typeName, baseType,contextCache);
        BUILTIN_TYPE_TO_METADATA_MAP.put(typeName,dBaseType);
      }            
    }
    return dBaseType;
  }
  
  private static boolean isBypassedSoapEncodingType(String typeName) {
    return (SchemaMappingConstants.SOAP_ENCODING_BYPASSED_TYPE_NAMES_HASH.contains(typeName));
  }
  
  private static DBaseType createDBaseType(TypeMappingImpl typeMapping, TypeDefinitionBase typeDefinition,Map contextCache) throws Exception {
    TypeDefinitionBase baseTypeDefinition = typeDefinition.getBaseTypeDefinition();
    String baseTypeDefinitionNamespace = baseTypeDefinition.getTargetNamespace();
    String baseTypeDefinitionName = baseTypeDefinition.getName();
    if(baseTypeDefinitionNamespace.equals(NS.SOAPENC) && baseTypeDefinitionName.equals(SchemaMappingConstants.SOAP_ENCODING_ARRAY_TYPE_NAME)) {
      throw new IllegalArgumentException("Type '" + baseTypeDefinition.getQualifiedKey() + "' is not supported.");
    }
    if (NS.SOAPENC.equals(typeDefinition.getTargetNamespace())) {
      QName typeName = determineDBaseTypeName(typeDefinition);
      DBaseType dBaseType = null;
      synchronized (BUILTIN_TYPE_TO_METADATA_MAP) {
        dBaseType = BUILTIN_TYPE_TO_METADATA_MAP.get(typeName);
        if (dBaseType == null) {
          dBaseType = (typeDefinition instanceof SimpleTypeDefinition ? createDSimpleType(typeMapping, (SimpleTypeDefinition)typeDefinition, typeName, null,contextCache) : createDComplexType(typeMapping, (ComplexTypeDefinition)typeDefinition,contextCache));
          BUILTIN_TYPE_TO_METADATA_MAP.put(typeName,dBaseType);
        }               
      }
      return dBaseType;
    } else {
      return(typeDefinition instanceof SimpleTypeDefinition ? createDSimpleType(typeMapping, (SimpleTypeDefinition)typeDefinition, null, null,contextCache) : createDComplexType(typeMapping, (ComplexTypeDefinition)typeDefinition,contextCache));
    }    
  }
  
  
  
  private static DComplexType createDComplexType(TypeMappingImpl typeMapping, ComplexTypeDefinition complTypeDef, Map contextCache) {
    QName typeName = determineDBaseTypeName(complTypeDef);
    QName baseTypeName = determineDBaseTypeName(complTypeDef.getBaseTypeDefinition());
    boolean isAbstract = complTypeDef.isAbstract();
    DField[] dFields = null;
    String id = null;
    DSimpleContent dSimpleContent = null;
    int dComplTypeType = -1;
    if(isAbstract) {
      dFields = determineDFields(complTypeDef, typeName,contextCache);
      dComplTypeType = determineDComplexTypeType(complTypeDef);
    } else {
      GeneratedComplexType generatedComplexType = (GeneratedComplexType)determineDeserializerBase(typeMapping, typeName);
      dComplTypeType = determineDComplexTypeType(generatedComplexType);
      dFields = determineDFields(typeMapping, complTypeDef, generatedComplexType, typeName,contextCache);
      id = generateDStructureID();
      generatedComplexType._setMetadataID(id);
    }
    if(dFields.length > 0 && dFields[0].getType()== DField.SIMPLE) {
      dSimpleContent = (DSimpleContent)dFields[0];
      baseTypeName = dSimpleContent.getFieldType();
    }
    DAnnotation localAnnotation = createDAnnotation(complTypeDef.getAnnotation());    
    DAnnotation topLevelAnnotation = createDAnnotation(getSchemaNode((Element) complTypeDef.getAssociatedDOMNode()),contextCache);        
    DComplexType dComplType = createDComplexType(localAnnotation,
                                                 topLevelAnnotation,  
                                                 createDAttributeInfoItems(complTypeDef.getAssociatedDOMNode()),
                                                 dComplTypeType,
                                                 typeName,
                                                 baseTypeName,
                                                 complTypeDef.isBuiltIn(),
                                                 complTypeDef.isAnonymous(),
                                                 isAbstract,
                                                 complTypeDef.isMixed(),
                                                 dSimpleContent,
                                                 determineDAttributes(complTypeDef, typeName,contextCache),
                                                 dFields,
                                                 id);
    if(id != null) {
      typeMapping.registerStructureMetadata(dComplType);
    }
    return(dComplType);
  }
  
  private static DField[] determineDFields(TypeMappingImpl typeMapping, ComplexTypeDefinition complTypeDef, GeneratedComplexType generatedComplexType, QName scopeName,Map contextCache) {
    FieldInfo[] generatedComplTypeFields = generatedComplexType._getFields();
    DField[] dFields = new DField[generatedComplTypeFields.length];
    Vector contentParticles = determineContentModelParticles(complTypeDef);
    for(int i = 0; i < generatedComplTypeFields.length; i++) {
      FieldInfo fieldInfo = generatedComplTypeFields[i];
      Base base = complTypeDef.getContentTypeSimpleTypeDefinition() == null ? ((Particle)(contentParticles.get(i))).getTerm() : null;  
      dFields[i] = createDField(typeMapping, base, fieldInfo, scopeName,contextCache);
    }
    return(dFields);
  }
  
  private static Vector determineContentModelParticles(ComplexTypeDefinition compleTypeDef) {
    Vector contentModelParticles = new Vector();
    Particle groupParticle = compleTypeDef.getContentTypeContentModel();
    if(groupParticle != null) {
      if(groupParticle.getMinOccurs() == 1 && groupParticle.getMaxOccurs() == 1) {
        initGroupParticles(contentModelParticles, determineModelGroup(groupParticle), determineModelGroup(groupParticle).isCompositorSequence());
      } else {
        contentModelParticles.add(groupParticle);
      }
    }
    return(contentModelParticles);
  }
  
  private static DField[] determineDFields(ComplexTypeDefinition complTypeDef, QName scopeName, Map contextCache) {
    Particle contentModel = complTypeDef.getContentTypeContentModel();
    DField[] dFields = new DField[0];
    if(contentModel != null) {
      ModelGroup modelGroup = (ModelGroup)(contentModel.getTerm());
      Vector modelGroupParticles = modelGroup.getParticles();
      dFields = new DField[modelGroupParticles.size()];
      for(int i = 0; i < modelGroupParticles.size(); i++) {
        Particle modelGroupParticle = (Particle)(modelGroupParticles.get(i));
        dFields[i] = createDField(modelGroupParticle, scopeName,contextCache);
      }
    }
    SimpleTypeDefinition simpleTypeDefContentModel = complTypeDef.getContentTypeSimpleTypeDefinition();
    if(simpleTypeDefContentModel != null) {
      dFields = new DField[1];
      DSimpleContent dSimpleContent = createDSimpleContent(simpleTypeDefContentModel, scopeName);
      dFields[0] = dSimpleContent;
    }
    return(dFields);
  }
  
  private static DSimpleContent createDSimpleContent(SimpleTypeDefinition simpleTypeDef, QName scopeName) {
    return(createDSimpleContent(determineDBaseTypeName(simpleTypeDef), scopeName));
  }
  
  private static DField createDField(Particle particle, QName scopeName,Map contextCache) {
    Base base = particle.getTerm();
    int baseType = base.getTypeOfComponent();
    switch(baseType) {
      case Base.C_ELEMENT_DECLARATION : {
        return(createDElement(particle, scopeName,contextCache));
      }
      case Base.C_MODEL_GROUP : case Base.C_MODEL_GROUP_DEFINITION : {
        return(createDGroup(particle, scopeName,contextCache));
      }
    }
    return(createDAny(particle, scopeName));
  }
  
  private static DAny createDAny(Particle particle, QName scopeName) {
    return(createDAny(scopeName, particle.getMinOccurs(), particle.getMaxOccurs()));
  }
  
  private static DGroup createDGroup(Particle particle, QName scopeName, Map contextCache) {
    ModelGroup modelGroup = determineModelGroup(particle);
    int type = determineDGroupType(modelGroup);
    QName dGroupName = createDGroupName(scopeName, type);
    return(createDGroup(scopeName,
                        dGroupName,
                        type,
                        particle.getMinOccurs(),
                        particle.getMaxOccurs(),
                        determineDFields(modelGroup, dGroupName,contextCache),
                        null));
  }
  
  private static QName createDGroupName(QName scopeName, int type) {
    switch(type) {
      case DField.ALL : {
        return(createDGroupName(scopeName, ALL_GROUP_NAME_SUFFIKS));
      }
      case DField.SEQUENCE : {
        return(createDGroupName(scopeName, SEQUENCE_GROUP_NAME_SUFFIKS));
      }
    }
    return(createDGroupName(scopeName, CHOICE_GROUP_NAME_SUFFIKS));
  }
  
  private static QName createDGroupName(QName scopeName, String suffiksName) {
    return(StaticQNameCache.get(scopeName.getNamespaceURI(), scopeName.getLocalPart() + "#" + suffiksName));
  }
  
  private static int determineDGroupType(ModelGroup modelGroup) {
    if(modelGroup.isCompositorAll()) {
      return(DField.ALL);
    }
    if(modelGroup.isCompositorChoice()) {
      return(DField.CHOICE);
    }
    return(DField.SEQUENCE);
  }
  
  private static DField[] determineDFields(ModelGroup modelGroup, QName scopeName, Map contextCache) {
    Vector groupParticles = determineGroupParticles(modelGroup);
    DField[] dFields = new DField[groupParticles.size()];
    for(int i = 0; i < groupParticles.size(); i++) {
      Particle particle = (Particle)(groupParticles.get(i));
      dFields[i] = createDField(particle, scopeName,contextCache);
    }
    return(dFields);
  }
  
  private static DElement createDElement(Particle particle, QName scopeName, Map contextCache) {
    ElementDeclaration elemDeclr = (ElementDeclaration)(particle.getTerm());
    DAnnotation localAnnotation = createDAnnotation(elemDeclr.getAnnotation());    
    DAnnotation topLevelAnnotation = createDAnnotation(getSchemaNode((Element) elemDeclr.getAssociatedDOMNode()),contextCache);        
    return(createDElement(localAnnotation,
                          topLevelAnnotation,
                          createDAttributeInfoItems(elemDeclr.getAssociatedDOMNode()),
                          determineDBaseTypeName(elemDeclr.getTypeDefinition()),
                          scopeName,
                          StaticQNameCache.get(elemDeclr.getTargetNamespace(), elemDeclr.getName()),
                          elemDeclr.getValueConstraintDefault(),
                          particle.getMinOccurs(),
                          particle.getMaxOccurs(),
                          elemDeclr.isNillable()));
  }
  
  private static DField createDField(TypeMappingImpl typeMapping, Base base, FieldInfo fieldInfo, QName scopeName, Map contextCache) {
    int fieldModel = fieldInfo.getFieldModel();
    switch(fieldModel) {
      case FieldInfo.FIELD_ANY : {
        return(createDAny(fieldInfo, scopeName));
      }
      case FieldInfo.FIELD_ELEMENT : {
        return(createDElement((ElementDeclaration)base, fieldInfo, scopeName,contextCache));
      }
      case FieldInfo.FIELD_BASE : {
        return(createDSimpleContent(fieldInfo, scopeName));
      }
    } 
    return(createDGroup(typeMapping, determineModelGroup(base), fieldInfo, scopeName,contextCache));
  }
  
  private static DSimpleContent createDSimpleContent(FieldInfo fieldInfo, QName scopeName) {
    return(createDSimpleContent(fieldInfo.getTypeQName(), scopeName));
  }
  
  private static DGroup createDGroup(TypeMappingImpl typeMapping, ModelGroup modelGroup, FieldInfo fieldInfo, QName scopeName, Map contextCache) {
    String id = generateDStructureID();
    QName dGroupName = createDGroupName(scopeName, fieldInfo.getTypeJavaName());
    if (fieldInfo.getFieldLocalName() == null) {
      fieldInfo.setFieldUri(dGroupName.getNamespaceURI());
      fieldInfo.setFieldLocalName(dGroupName.getLocalPart());
    } else {
      dGroupName = fieldInfo.getFieldQName();
    }
    
    GeneratedModelGroup generatedModelGroup = (GeneratedModelGroup)(fieldInfo.objectValue);
    DGroupImpl dGroup = (DGroupImpl)(createDGroup(scopeName,
                                                  dGroupName,
                                                  determineDGroupType(modelGroup),
                                                  fieldInfo.getMinOccurs(),
                                                  fieldInfo.getMaxOccurs(),
                                                  determineDFields(typeMapping, generatedModelGroup, modelGroup, fieldInfo, dGroupName,contextCache),
                                                  id));
    generatedModelGroup._setMetadataID(id);
    typeMapping.registerStructureMetadata(dGroup);
    return(dGroup);
  }
  
  private static String generateDStructureID() {
    return("" + STRUCTURE_METADATA_ID++);
  }
  
  private static DField[] determineDFields(TypeMappingImpl typeMapping, GeneratedModelGroup generatedModelGroup, ModelGroup modelGroup, FieldInfo fieldInfo, QName scopeName,Map contextCache) {
    FieldInfo[] generatedModelGroupFileds = generatedModelGroup._getFields();
    DField[] dFields = new DField[generatedModelGroupFileds.length];
    Vector groupParticles = determineGroupParticles(modelGroup);
    for(int i = 0; i < generatedModelGroupFileds.length; i++) {
      FieldInfo generatedModelGroupFiled = generatedModelGroupFileds[i];
      Particle particle = (Particle)(groupParticles.get(i));
      dFields[i] = createDField(typeMapping, particle.getTerm(), generatedModelGroupFiled, scopeName,contextCache);
    }
    return(dFields);
  }
  
  private static DElement createDElement(ElementDeclaration elemDeclr, FieldInfo fieldInfo, QName scopeName, Map contextCache) {    
    DAnnotation localAnnotation = createDAnnotation(elemDeclr.getAnnotation());    
    DAnnotation topLevelAnnotation = createDAnnotation(getSchemaNode((Element) elemDeclr.getAssociatedDOMNode()),contextCache);            
    return(createDElement(localAnnotation,
                          topLevelAnnotation,
                          createDAttributeInfoItems(elemDeclr.getAssociatedDOMNode()),
                          fieldInfo.getTypeQName(),
                          scopeName,
                          fieldInfo.getFieldQName(),
                          fieldInfo.getDefaultValue(),
                          fieldInfo.getMinOccurs(),
                          fieldInfo.getMaxOccurs(),
                          fieldInfo.isNillable()));
  }
  
  private static DAny createDAny(FieldInfo fieldInfo, QName scopeName) {
    return(createDAny(scopeName, fieldInfo.getMinOccurs(), fieldInfo.getMaxOccurs()));
  }
  
  private static int determineDComplexTypeType(ComplexTypeDefinition complexTypeDefinition) {
    Particle contentModelParticle = complexTypeDefinition.getContentTypeContentModel();
    if(contentModelParticle != null) {
      ModelGroup modelGroup = determineModelGroup(contentModelParticle.getTerm());
      if(modelGroup.isCompositorAll()) {
        return(DComplexType.ALL);
      } else if(modelGroup.isCompositorChoice()) {
        return(DComplexType.CHOICE);
      } else {
        return(DComplexType.SEQUENCE);
      }
    } else if(complexTypeDefinition.getContentTypeSimpleTypeDefinition() != null) {
      return(DComplexType.SIMPLE);
    } else {
      return(DComplexType.SEQUENCE);
    }
  }
  
  private static int determineDComplexTypeType(GeneratedComplexType generatedComplType) {
    int modelType = generatedComplType._getModelType();
    switch(modelType) {
      case(FieldInfo.FIELD_ALL) : {
        return(DComplexType.ALL);
      }
      case(FieldInfo.FIELD_BASE) : {
        return(DComplexType.SIMPLE);
      }
      case(FieldInfo.FIELD_CHOICE) : {
        return(DComplexType.CHOICE);
      }
      case(FieldInfo.FIELD_SEQUENCE) : {
        return(DComplexType.SEQUENCE);
      }
    }
    return(DComplexType.SEQUENCE);
  }
  
  private static ModelGroup determineModelGroup(Base base) {
    return(base instanceof ModelGroup ? (ModelGroup)base : ((ModelGroupDefinition)base).getModelGroup());
  }
  
  private static DField[] determineDAttributes(ComplexTypeDefinition complexTypeDefinition, QName scopeName, Map contextCache) {
    Vector attributeUses = complexTypeDefinition.getAttributeUses();
    DField[] dAttributes = new DField[attributeUses.size()];
    for(int i = 0; i < attributeUses.size(); i++) {
      AttributeUse attributeUse = (AttributeUse)(attributeUses.get(i));
      dAttributes[i] = createDAttribute(attributeUse, scopeName,contextCache);
    }
    return(dAttributes);
  }
  
  private static Element getSchemaNode(Element elementNode) {
    if (elementNode == null) {
      return null;
    }
    Element result = elementNode.getOwnerDocument().getDocumentElement();
    if (NS.XS.equals(result.getNamespaceURI()) && "schema".equals(result.getLocalName())) {
      return result;
    }
    
    result = elementNode;
    while (result.getParentNode() != null && result.getParentNode().getNodeType() == Node.ELEMENT_NODE) {
      result = (Element) result.getParentNode();
      if (NS.XS.equals(result.getNamespaceURI()) && "schema".equals(result.getLocalName())) {
        return result;
      }      
    }
    return null;
  }

  private static DAttribute createDAttribute(AttributeUse attributeUse, QName scopeName,Map contextCache) {
    AttributeDeclaration attribDeclr = attributeUse.getAttributeDeclaration();
    DAnnotation localAnnotation = createDAnnotation(attribDeclr.getAnnotation());    
    DAnnotation topLevelAnnotation = createDAnnotation(getSchemaNode((Element) attribDeclr.getAssociatedDOMNode()),contextCache);    
    DAttribute result = (createDAttribute(localAnnotation,
                         topLevelAnnotation,
                         createDAttributeInfoItems(attribDeclr.getAssociatedDOMNode()),
                         determineDBaseTypeName(attribDeclr.getTypeDefinition()),
                         scopeName,
                         StaticQNameCache.get(attribDeclr.getTargetNamespace(), attribDeclr.getName()),
                         attributeUse.getValueConstraintDefault(),
                         attributeUse.isRequired()));    
    return result;
  }
  
  private static DSimpleType createDSimpleType(TypeMappingImpl typeMapping, SimpleTypeDefinition simpleTypeDef, QName typeName, QName baseTypeName, Map contextCache) throws Exception {
    QName dSimpleTypeName = (typeName == null ? determineDBaseTypeName(simpleTypeDef) : typeName);
    DAnnotation localAnnotation = createDAnnotation(simpleTypeDef.getAnnotation());    
    DAnnotation topLevelAnnotation = createDAnnotation(getSchemaNode((Element) simpleTypeDef.getAssociatedDOMNode()),contextCache);            
    return(createDSimpleType(localAnnotation,
                             topLevelAnnotation,
                             createDAttributeInfoItems(simpleTypeDef.getAssociatedDOMNode()),
                             determineDSimpleTypeType(simpleTypeDef),
                             dSimpleTypeName,
                             (baseTypeName == null ? determineBaseTypeName_DSimpleType(simpleTypeDef) : baseTypeName),
                             simpleTypeDef.isBuiltIn(),
                             simpleTypeDef.isAnonymous(),
                             determineFacets(simpleTypeDef, typeMapping, dSimpleTypeName)));
  }
  
  private static QName determineBaseTypeName_DSimpleType(SimpleTypeDefinition simpleTypeDefinition) {
    return(determineDBaseTypeName(simpleTypeDefinition.isVarietyList() ? simpleTypeDefinition.getItemTypeDefinition() : simpleTypeDefinition.getBaseTypeDefinition()));
  }
  
  private static com.sap.engine.services.webservices.espbase.client.dynamic.types.Facet[] determineFacets(SimpleTypeDefinition simpleTypeDef, ExtendedTypeMapping typeMapping, QName dSimpleTypeName) throws UnmarshalException {
    Vector simpleTypeDefFacets = simpleTypeDef.getFacets();
    com.sap.engine.services.webservices.espbase.client.dynamic.types.Facet[] facets = new com.sap.engine.services.webservices.espbase.client.dynamic.types.Facet[simpleTypeDefFacets.size()];
    Class dSimpleTypeClass = typeMapping.getDefaultJavaClass(dSimpleTypeName);
    SOAPDeserializationContext deserializationCtx = new SOAPDeserializationContext();   
    DeserializerBase  deserializerBase = determineDeserializerBase(typeMapping, dSimpleTypeName);
    for(int i = 0; i < simpleTypeDefFacets.size(); i++) {
      Facet simpleTypeDefFacet = (Facet)(simpleTypeDefFacets.get(i));
      deserializationCtx.clearContext();
      deserializationCtx.setExtendedTypeMapping(typeMapping);
      facets[i] = createFacet(simpleTypeDefFacet, deserializerBase, dSimpleTypeClass, deserializationCtx);
    }
    return(facets);
  }
  
  private static DeserializerBase determineDeserializerBase(ExtendedTypeMapping typeMapping, QName typeQName) {
    XMLTypeMapping xmlTypeMapping = typeMapping.getXmlTypeMapping(typeQName);
    DeserializerBase  deserializerBase = (DeserializerBase)(xmlTypeMapping.getDefaultDeserializer());
    return(deserializerBase);
  }
  
  private static com.sap.engine.services.webservices.espbase.client.dynamic.types.Facet createFacet(Facet simpeTypeDefinitionFacet, DeserializerBase deserializerBase, Class dSimpleTypeClass, SOAPDeserializationContext deserializationContext) throws UnmarshalException {
    Object objValue = null;
    int intValue = 0;
    String facetName = simpeTypeDefinitionFacet.getName();
    String facetValue = simpeTypeDefinitionFacet.getValue();
    if(facetName.equals(Constants.FACET_LENGTH_NAME) || 
       facetName.equals(Constants.FACET_MIN_LENGTH_NAME) ||
       facetName.equals(Constants.FACET_MAX_LENGTH_NAME) ||
       facetName.equals(Constants.FACET_TOTAL_DIGITS_NAME) ||
       facetName.equals(Constants.FACET_FRACTION_DIGITS_NAME)) {
      intValue = Integer.parseInt(facetValue);
    } else if(facetName.equals(Constants.FACET_ENUMERATION_NAME) ||
              facetName.equals(Constants.FACET_MAX_INCLUSIVE_NAME) ||
              facetName.equals(Constants.FACET_MAX_EXCLUSIVE_NAME) ||
              facetName.equals(Constants.FACET_MIN_INCLUSIVE_NAME) ||
              facetName.equals(Constants.FACET_MIN_EXCLUSIVE_NAME)) {
      objValue = deserializerBase.deserialize(facetValue, deserializationContext, dSimpleTypeClass);
    }
    return(createFacet(simpeTypeDefinitionFacet.getName(), simpeTypeDefinitionFacet.getValue(), objValue, intValue));
  }
  
  private static int determineDSimpleTypeType(SimpleTypeDefinition simpleTypeDefinition) {
    return(simpleTypeDefinition.isVarietyList() ? DSimpleType.LIST : DSimpleType.RESTRICTION);
  }
  
  private static QName determineDBaseTypeName(TypeDefinitionBase typeDefinitionBase) {
    return(StaticQNameCache.get(typeDefinitionBase.getTargetNamespace(), typeDefinitionBase.isAnonymous() ? DOM.toXPath(typeDefinitionBase.getAssociatedDOMNode()) : typeDefinitionBase.getName()));
  }

  private static Vector determineGroupParticles(ModelGroup modelGroup) {
    Vector groupParticles = new Vector();
    initGroupParticles(groupParticles, modelGroup, false);
    return(groupParticles);
  }
  
  private static boolean extractSequenceGroup(Particle particle) {
    if(particle.getMaxOccurs() == 1 && particle.getMinOccurs() == 1) {
      ModelGroup modelGroup = determineModelGroup(particle);
      return(modelGroup != null && modelGroup.isCompositorSequence());
    }
    return(false);
  }
  
  private static ModelGroup determineModelGroup(Particle particle) {
    Base term = particle.getTerm();
    if(term instanceof ModelGroup) {
      return((ModelGroup)term);
    }
    if(term instanceof ModelGroupDefinition) {
      return(((ModelGroupDefinition)term).getModelGroup());
    }
    return(null);
  }
  
  private static void initGroupParticles(Vector expandedGroupParticles, ModelGroup modelGroup, boolean extractSequenceGroups) {
    Vector groupParticles = modelGroup.getParticles();
    for(int i = 0; i < groupParticles.size(); i++) {
      Particle particle = (Particle)(groupParticles.get(i));
      if(extractSequenceGroups && extractSequenceGroup(particle)) {
        initGroupParticles(expandedGroupParticles, determineModelGroup(particle), true);
      } else {
        expandedGroupParticles.add(particle);
      }
    }
  }
}
