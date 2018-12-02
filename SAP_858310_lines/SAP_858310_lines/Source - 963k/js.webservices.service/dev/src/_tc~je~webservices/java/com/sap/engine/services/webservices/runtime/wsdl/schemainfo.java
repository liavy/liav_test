package com.sap.engine.services.webservices.runtime.wsdl;

import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.lib.descriptors.ws04vi.TableState;
import com.sap.engine.lib.descriptors.ws04vi.TypeState;
import com.sap.engine.lib.descriptors.ws04vi.VirtualInterfaceTypes;
import com.sap.engine.services.webservices.jaxrpc.java2schema.StandardTypes;
import com.sap.engine.services.webservices.wsdl.WSDLException;
import com.sap.engine.services.webservices.runtime.RuntimeExceptionConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.*;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public final class SchemaInfo {

  private static final String URI_PREFIX   =  "urn:";
  private static final String PREFIX_BASE  =  "s";   //used as base: s0, s1, s2 so on
  private static final String EMPTYNAMESPACE  = "";

  private static DocumentBuilderFactory factory;

  static {
    factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
  }

  private Element schemaElement;
  private int prefixNom = 0;

  Hashtable imports;                  //uri-key mapped prefix value
  Hashtable originalImports = new Hashtable();
  String targetNamespace;
  //used for the schema ordering according to imports
  boolean isVisited = false;


//Constructors-------------------------------
  SchemaInfo(String targetNamespace, boolean isQualified) throws WSDLException {
    try {
      this.targetNamespace = targetNamespace;
      imports = new Hashtable();
//      SystemProperties.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sap.engine.lib.jaxp.DocumentBuilderFactoryImpl");
      org.w3c.dom.Document doc = factory.newDocumentBuilder().newDocument();
      this.schemaElement = doc.createElementNS(NS.XS, StandardTypes.SCHEMA_PREFIX + ":schema");
      schemaElement.setAttributeNS(NS.XMLNS, "xmlns:" + StandardTypes.SCHEMA_PREFIX, NS.XS);
      schemaElement.setAttributeNS("", "targetNamespace", targetNamespace);
      schemaElement.setAttributeNS(NS.XMLNS, "xmlns:tns", targetNamespace);
      if (isQualified) {
        schemaElement.setAttributeNS("", "elementFormDefault", "qualified");
      }
      //put standard mappings
      imports.put(targetNamespace, "tns");
      imports.put(NS.XS, StandardTypes.SCHEMA_PREFIX);
    } catch (javax.xml.parsers.ParserConfigurationException e) {
      throw new WSDLException(e);
    }

  }

  SchemaInfo(Element schemaElement) throws WSDLCreationException {
    //in case it is not schema element
    if (! schemaElement.getNamespaceURI().equals(NS.XS) || (! schemaElement.getLocalName().equals("schema"))) {
      throw new WSDLCreationException(RuntimeExceptionConstants.WRONG_SCHEMA_ELEMENT, new Object[]{schemaElement.getNamespaceURI(), schemaElement.getLocalName()});
    }

    this.imports = new Hashtable();
    this.schemaElement = schemaElement;
    this.targetNamespace = schemaElement.getAttribute("xmlns:tns");
    NamedNodeMap list = schemaElement.getAttributes();
    Attr attr;

    for (int i = 0; i < list.getLength(); i++) {
      attr = (Attr) list.item(i);
      if (attr.getName().startsWith("xmlns")) {
        originalImports.put(attr.getValue(), attr.getName().substring("xmlns".length() + 1));
        prefixNom++; //this is because the schemas import prefixes are numbered the same
      }
    }

    imports.put(targetNamespace, "tns");
    imports.put(NS.XS, StandardTypes.SCHEMA_PREFIX);
  }

//Public methods-----------------------------

  /**
   *  Oredering the schemas according to their imports
   * The hashTable keys are the schemas tns(String) and values SchemaInfo object
   */
  public static ArrayList resolveSchemas(Hashtable schemaInfos) throws WSDLCreationException {
    Iterator itr = schemaInfos.values().iterator();
    ArrayList resoved = new ArrayList();

    int totalNom = 0;
    while (itr.hasNext()) {
      resolveSchema((SchemaInfo) itr.next(), resoved, schemaInfos);
      totalNom++;
    }

    if (resoved.size() != totalNom) {
      throw new WSDLCreationException(RuntimeExceptionConstants.RESOLVE_SCHEMA_IMPORTS_FAILS);
    }

    return resoved;
  }

  private static void resolveSchema(SchemaInfo schema, ArrayList resolved, Hashtable schemas) throws WSDLCreationException {
    //in case it is resolved
    if (resolved.contains(schema)) {
      return;
    }

    schema.isVisited = true;

    String keyNS;
    SchemaInfo importedSchema;

    Enumeration enumeration  = schema.imports.keys();
    while (enumeration.hasMoreElements()) {
      keyNS = (String) enumeration.nextElement();

      //pass the standard imports
      if (keyNS.equals(schema.targetNamespace) || keyNS.equals(NS.XS)) {
        continue;
      }

      importedSchema = (SchemaInfo) schemas.get(keyNS);

      if (importedSchema == null) {
        throw new WSDLCreationException(RuntimeExceptionConstants.RESOLVE_SCHEMA_IMPORTS_FAILS, new Object[]{keyNS, schema.targetNamespace});
      }

      //in case this import is already resolved
      if (resolved.contains(importedSchema)) {
        continue;
      }

      //in case of cicle
      if (importedSchema.isVisited) {
        resolved.add(schema); //temporary add to resolve importedSchema;
      }

      resolveSchema(importedSchema, resolved, schemas);
    }

    if (resolved.remove(schema)) { //there were a cicle
      resolveSchema(schema, resolved, schemas);
    } else {
      //add it at the end
      resolved.add(schema);
    }
  }

  static String getUriFromTypeState(TypeState typeState, VirtualInterfaceTypes.Choice1[] allTypes, String suffix) throws Exception {
    if (com.sap.engine.services.webservices.runtime.wsdl.StandardTypes.isJavaUtilType(SchemaConvertor.getOriginalType(typeState))) {
      return SchemaConvertor.JAVA_UTIL_SCHEMA_NAMESPACE;
    }

    if (typeState instanceof TableState) {
      typeState = getArrayComponent((TableState) typeState, allTypes);
    }

    if (typeState.getTypeSoapExtensionType()!= null) {
      if (typeState.getTypeSoapExtensionType().getSoapExtensionType().getNamespace() != null) {
        String nsValue = typeState.getTypeSoapExtensionType().getSoapExtensionType().getNamespace();
        if (nsValue.length() > 0) {
          return nsValue;
        }
      }
    }

    return getUriFromClassName(SchemaConvertor.getOriginalType(typeState), suffix);
  }

  static String getUriFromClassName(String name, String suffix) {
    int i = name.lastIndexOf('.');
    if (i == -1) return URI_PREFIX + name;

    String packg = name.substring(0, i);
//    return URI_PREFIX + packg.replace('.', '/') + suffix;
    return URI_PREFIX + packg.replace('.', '/');

  }

  String getPrefixForTypeState(TypeState typeState, VirtualInterfaceTypes.Choice1[] allTypes, String suffix) throws Exception {
    String uri = getUriFromTypeState(typeState, allTypes, suffix);

    return getPrefixForUri(uri);
  }

  String getPrefixForUri(String uri) {
    //search the defined.
    String prefix = (String) originalImports.get(uri);
    if (prefix != null) {
      return prefix;
    }

    prefix = (String) imports.get(uri);
    if (prefix == null) {
      prefix = PREFIX_BASE + (prefixNom++);
      imports.put(uri, prefix);
    }
    return prefix;
  }

  String getTargetNamespace() {
    return targetNamespace;
  }

  Element getSchemaElement() {
    return schemaElement;
  }

  Element normalizeSchema() {
    //removing mapped in the constructor global namespace mappings
    imports.remove(this.targetNamespace);
    imports.remove(NS.XS);

    Enumeration uries = imports.keys();
    String tempUri;
    Node firstChild = schemaElement.getFirstChild();

    while (uries.hasMoreElements()) {
      tempUri = (String) uries.nextElement();
      String tempPrefix = (String) imports.get(tempUri);
      schemaElement.setAttributeNS(NS.XMLNS, "xmlns:" + tempPrefix, tempUri);
      if (!tempUri.equals(EMPTYNAMESPACE)) {
        Element importElement = schemaElement.getOwnerDocument().createElementNS(NS.XS, "xs:import");
        importElement.setAttributeNS("", "namespace", tempUri);
        schemaElement.insertBefore(importElement, firstChild);
      }
    }
    return schemaElement;
  }

  static TypeState getArrayComponent(TableState table,  VirtualInterfaceTypes.Choice1[] allTypes) throws Exception {
    String directTypeName = table.getTableLineType().getComplexTypeReference().getName().trim();
    TypeState tempState = SchemaConvertor.findTypeStateByOriginalName(directTypeName, allTypes);
    if (tempState instanceof TableState) {
      return getArrayComponent((TableState) tempState, allTypes);
    }
    return tempState;
  }

}