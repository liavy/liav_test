package com.sap.engine.services.webservices.espbase.client.migration;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.jaxrpc.encoding.primitive.AnyTypeSD;
import com.sap.engine.services.webservices.jaxrpc.encoding.primitive.Base64BinarySD;
import com.sap.engine.services.webservices.jaxrpc.encoding.primitive.BooleanSD;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaMappingConstants;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaTypeInfo;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaTypeSet;
import com.sap.tc.logging.Location;

public class TypeMappingMigrationUtil {
  
  // Trace location.
  public static final Location LOC = Location.getLocation(TypeMappingMigrationUtil.class);   
  
  public static final String NS_LANG = "urn:java.lang";
  public static final String COMPLEX_TYPE = "complexType";
  public static final String TARGET_NAMESPACE = "targetNamespace";
  public static final String NAME = "name";
  public static final String BASE = "base";
  public static final String THROWABLE = "Throwable";
    
  private static void addFakeComplexType(Element schemaElement, String typeName) {
    Document doc = schemaElement.getOwnerDocument();
    Element complexType = doc.createElementNS(SchemaMappingConstants.SCHEMA_NAMESPACE,"xsd:complexType");
    complexType.setAttributeNS(NS.XMLNS,"xmlns:xsd",SchemaMappingConstants.SCHEMA_NAMESPACE);
    complexType.setAttributeNS(null,NAME,typeName);
    Element complexContent = doc.createElementNS(SchemaMappingConstants.SCHEMA_NAMESPACE,"xsd:complexContent");
    Element baseType = doc.createElementNS(SchemaMappingConstants.SCHEMA_NAMESPACE,"xsd:extension");
    baseType.setAttributeNS(null,BASE,"xsd:anyType");
    complexContent.appendChild(baseType);
    complexType.appendChild(complexContent);
    schemaElement.appendChild(complexType);    
  }
  
  /**
   * Adds {urn:java.lang}Throwable type that is missing ins some older WSDLs published by the 6.40 web services.
   * @param config
   */
  public static void addMissingSchemaTypes(ArrayList schemaSources) {        
    // Finds {urn:java.lang} schema.
    Element javaLangSchema = null;
    for (int i=0; i<schemaSources.size(); i++) {
      DOMSource domSource = (DOMSource) schemaSources.get(i);
      Element elementRoot = (Element) domSource.getNode();
      String targetNamespace = elementRoot.getAttributeNS(null,TARGET_NAMESPACE);
      if (NS_LANG.equals(targetNamespace)) {
        javaLangSchema = elementRoot;
        break;
      }
    }  
    // Adds "Throwable" type if it is not existing and schema from the required namespace is existing.
    if (javaLangSchema != null) {
      Node currentNode = javaLangSchema.getFirstChild();
      boolean throwableFound = false;
      while (currentNode != null) {
        if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
          // This is element node
          Element currentElement = (Element) currentNode;
          if (COMPLEX_TYPE.equals(currentElement.getLocalName())) {
            // This is comple type definition
            if (THROWABLE.equals(currentElement.getAttributeNS(null,NAME))) {
              throwableFound = true;
              break;
            }
          }
        }
        currentNode = currentNode.getNextSibling();
      }
      if (!throwableFound) { // Throwable type is not found. Add throwable xsd type to the schema definition.
        addFakeComplexType(javaLangSchema,THROWABLE);
      }
    }
  }
  
  /**
   * Parses the type mapping deplyed with 6.40 web services clients and removes inconsitencies.
   * @param types
   */
  public static void preProcessTypeMapping(SchemaTypeSet types) {
    Enumeration allSchemaTypes = types.getSchemaTypes();
    ArrayList<QName> toRemove = new ArrayList<QName>();
    while(allSchemaTypes.hasMoreElements()){      
      QName schemaType = (QName)allSchemaTypes.nextElement();
      // Removes invalid SOAP Encoding types added in old type mapping descriptors     
      if (NS.SOAPENC.equals(schemaType.getNamespaceURI())) {
        if ("anyType".equals(schemaType.getLocalPart())) {
          toRemove.add(schemaType); 
          continue;         
        }      
        if ("anySimpleType".equals(schemaType.getLocalPart())) {
          toRemove.add(schemaType); 
          continue;         
        }              
      }
      
      String javaType = types.getJavaType(schemaType);
      if(SchemaMappingConstants.JAXRPC_WRAPPER_TO_SIMPLE.containsKey(javaType)){        
        types.setJavaType(schemaType, (String)SchemaMappingConstants.JAXRPC_WRAPPER_TO_SIMPLE.get(javaType));
      }
    }
    for (int i=0; i<toRemove.size(); i++) {
      types.remove(toRemove.get(i));
    }    
  }
  
  /**
   * Removes inconsitencies in 6.40 type mapping after consumer migration.
   * @param types
   */
  public static void postProcessTypeMapping(SchemaTypeSet types) {
    // Removes the serializers for the soap encofing integrated types.
    Iterator iterator = SchemaMappingConstants.SOAP_ENCODING_BYPASSED_TYPE_NAMES_HASH.iterator();
    while (iterator.hasNext()) {
      String localName = (String) iterator.next();
      QName typeName = new QName(SchemaMappingConstants.SOAP_ENCODING,localName);
      types.remove(typeName);
    }
    // Sets serializers for known xsd types with no serializers.
    Enumeration allSchemaTypes = types.getSchemaTypes();
    while(allSchemaTypes.hasMoreElements()){      
      QName schemaType = (QName)allSchemaTypes.nextElement();
      SchemaTypeInfo typeInfo = types.get(schemaType);
      // Checks if all types have serializers and add serializers for known ones
      if (typeInfo.getSerializerClass() == null) {
        if (NS.SOAPENC.equals(schemaType.getNamespaceURI())) {
          if ("base64".equals(schemaType.getLocalPart())) {
            typeInfo.setSerializerClass(Base64BinarySD.class.getName());
            typeInfo.setInitParams(new String[] {schemaType.getNamespaceURI(),schemaType.getLocalPart()});
          }    
          if ("/xs:schema/xs:attribute[1]/xs:simpleType".equals(schemaType.getLocalPart())) {
            typeInfo.setSerializerClass(BooleanSD.class.getName());
            typeInfo.setInitParams(new String[] {schemaType.getNamespaceURI(),schemaType.getLocalPart(),"specialBoolean"});
          }    
        }
        if ("urn:java.lang".equals(schemaType.getNamespaceURI())) {
          if ("Throwable".equals(schemaType.getLocalPart())) {
            typeInfo.setSerializerClass(AnyTypeSD.class.getName());
            typeInfo.setInitParams(new String[] {schemaType.getNamespaceURI(),schemaType.getLocalPart()});
          }            
          if ("Cloneable".equals(schemaType.getLocalPart())) {
            typeInfo.setSerializerClass(AnyTypeSD.class.getName());
            typeInfo.setInitParams(new String[] {schemaType.getNamespaceURI(),schemaType.getLocalPart()});
          }                      
        }        
      }      
      if (typeInfo.getSerializerClass() == null) {
        System.out.println("Type with QName :"+schemaType+" does not have serializer !");
      }      
    }    
    // Fix references to the removed soap encoding types.
    Enumeration keys = types.getSchemaTypes();    
    while (keys.hasMoreElements()) {
      QName typeName = (QName) keys.nextElement();
      SchemaTypeInfo typeInfo = types.get(typeName);      
      QName parentType = typeInfo.getParentType();
      if (parentType != null && SchemaMappingConstants.SOAP_ENCODING.equals(parentType.getNamespaceURI()) && SchemaMappingConstants.SOAP_ENCODING_BYPASSED_TYPE_NAMES[1].equals(parentType.getLocalPart())) {
        typeInfo.setParentType(new QName(SchemaMappingConstants.SCHEMA_NAMESPACE,"anyType"));
      }
    }    
  }
 
}
