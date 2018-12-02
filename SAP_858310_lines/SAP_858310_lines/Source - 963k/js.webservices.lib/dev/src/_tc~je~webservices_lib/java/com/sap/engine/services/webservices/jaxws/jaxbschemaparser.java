/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.jaxws;

import java.net.URL;
import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sap.engine.lib.jaxp.MultiSource;
import com.sap.engine.lib.schema.components.*;
import com.sap.engine.lib.schema.exception.SchemaComponentException;
import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.jaxrpc.encoding.SerializationUtil;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaAutoImportURIResolver;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaToJavaConfig;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaToJavaGeneratorException;
import com.sun.xml.bind.api.impl.NameConverter;


/**
 * 
 * @author Dimitar Velichkov (I033362) dimitar.velichkov@sap.com
 */
public class JaxbSchemaParser {

  private HashMap<QName, QName> elemToTypeMap = new HashMap<QName, QName>();
  private HashMap<String, QName> keyToAnonymous = new HashMap<String,QName>();  
  private boolean shortInnerNames = false;
  private boolean fullPath = false; // Wether to use full path algorithm for short inner names
  private boolean addTopLevelPrefix = false; // Use this if the local types are mapped to top level types
  
  public void setFullPath(boolean flag) {
    this.fullPath = flag;  
  }
  
  private static class TempStruct {
    public QName typeQName;
    public String typeName;
    public Stack<String> elementPath;
  }
  
  public JaxbSchemaParser(){    
  }
  
  public void setShortInnerNames(boolean flag) {
    this.shortInnerNames = flag;
  }
  
  public void setTopLevelMapping() {
    if (this.shortInnerNames) {
      this.fullPath = true;
      this.addTopLevelPrefix = true;
    }
  }
  
/*
  public static String makeShortName(String longName) {
    longName = NameConverter.jaxrpcCompatible.toClassName(longName);
    StringBuffer result = new StringBuffer();
    int counter = 0;
    for (int i=0; i<longName.length(); i++) {
      char character = longName.charAt(i);
      if (Character.isUpperCase(character)) {
        result.append(character);
        counter = 0;
      } else {
        counter++;
        if (counter<3) {
          result.append(character);
        }
      }      
    }
    return result.toString();
  }
*/  
  /**
   * Parses schema into the schema API.
   */
  public void loadSchemaAndTypes(SchemaToJavaConfig config) throws SchemaToJavaGeneratorException {
    if (config.getSchema() == null) {              
      Loader schemaLoader = SchemaLoaderFactory.create();
      schemaLoader.setUriResolver(config.getSchemaResolver());
      schemaLoader.setValidateXSDDoc(false);
      schemaLoader.setBackwardsCompatibilityMode(true);
            
      MultiSource ms = new MultiSource(); // loads the schema into multisource
      int nSchemaSources = config.getSchemaSources().size();
      if (nSchemaSources == 0) {
        URL url = SchemaAutoImportURIResolver.class.getResource("preloaded/soapenc.xsd");
        if (url != null) {
          ms.addSource(new StreamSource(url.toString()));
        }
      } else {
        for (int i = 0; i < nSchemaSources; i++) {
          ms.addSource((Source) config.getSchemaSources().get(i));
        }
      }
      
      String oldHost = "";
      String oldPort = "";
      Schema schema = null;
      try {
        // set up proxy and load schema
        if (config.getProxyHost() != null && config.getProxyPort() != null) {
          oldHost = System.getProperty("http.proxyHost");
          oldPort = System.getProperty("http.proxyPort");
          System.setProperty("http.proxyHost",config.getProxyHost());
          System.setProperty("http.proxyPort",config.getProxyPort());
        }
        schemaLoader.setLoadPatternRegularExpressions(false);
        schema = schemaLoader.loadFromWSDLDocument(ms);
      } catch (SchemaComponentException schemaCompExc) {
        throw new SchemaToJavaGeneratorException(schemaCompExc);
      } finally {
        if (config.getProxyHost() != null) { // Restore the system property
          if (oldHost == null || oldPort == null) {
            System.getProperties().remove("http.proxyHost");
            System.getProperties().remove("http.proxyPort");
          } else {
            System.setProperty("http.proxyHost",oldHost);
            System.setProperty("http.proxyPort",oldPort);
          }
        }
      }
      
      config.setSchema(schema);
      // Parses information about schema components.
      getTopLevelAll(schema);            
    }
    
  }
  
  private String collectString(Stack<String> stringList) {
    StringBuffer result = new StringBuffer();
    for (int i=0; i<stringList.size()-1; i++) {
      result.append("<");
      result.append(stringList.get(i));
      result.append(">");
    }
    return result.toString();
  }
  
  /**
   * Reads all top level ement declarations.
   * @param sch
   * @throws SchemaToJavaGeneratorException
   */
  private void getTopLevelAll(Schema sch) throws SchemaToJavaGeneratorException {
    Vector allComponents = new Vector(); 
    Vector types = new Vector();
    Vector typesInsideElements = new Vector();
    // Finds anonymous complex type definitions in internal elements
    sch.getTopLevelTypeDefinitions(types);
    sch.getAllComponents(allComponents);         
    ElementDeclaration[] allElements = sch.getTopLevelElementDeclarationsArray();
    for(ElementDeclaration elDecl : allElements){
      
      elDecl.getName();
      TypeDefinitionBase tdef = elDecl.getTypeDefinition();
            
      QName elemName = new QName(elDecl.getTargetNamespace(), elDecl.getName());      
      QName elemType = null;
         
      if (tdef.getName().length() == 0) { // The type inside the element is anonymous
        elemType = new QName(tdef.getTargetNamespace(),DOM.toXPath(tdef.getAssociatedDOMNode()));   //anonymous case!
        typesInsideElements.add(elDecl); // Adds the anonymous type definitions inside top level elements
      } else {
        elemType = new QName(tdef.getTargetNamespace(), tdef.getName());
      }                  
      elemToTypeMap.put(elemName, elemType);      
    }
    for (int i=0; i<allComponents.size(); i++) {
      Object c = allComponents.get(i);
      if (c instanceof ElementDeclaration) { // This is a element declaration
        ElementDeclaration ed = (ElementDeclaration) c;
        TypeDefinitionBase td = ed.getTypeDefinition();
        if (!ed.isTopLevel() && td.isAnonymous() && (td instanceof ComplexTypeDefinition)) { // If the type is anonymous inside non top level element.
          ComplexTypeDefinition cType = (ComplexTypeDefinition) td;
          String targetNamespace = cType.getTargetNamespace();
          String xpath = DOM.toXPath(cType.getAssociatedDOMNode());
          QName elementQName = new QName(ed.getTargetNamespace(),ed.getName());
          TempStruct wrapperType = findType(types,typesInsideElements,ed);
          if (wrapperType != null) {
            // The element declaration is contained in complex type definition
            if (this.addTopLevelPrefix) { // This is top level names
              String key = "["+targetNamespace+"]"+wrapperType.typeName;
              QName aName = new QName(targetNamespace,xpath);
              if (this.keyToAnonymous.containsKey(key)) {
                System.out.println("Internal name collision detected for type: "+aName+ " [key] "+key);
              }
              //System.out.println("Registering anonymous type : " +key+" = "+aName);
              this.keyToAnonymous.put(key,aName);
            } else {              
              String key = createKey(targetNamespace,elementQName,wrapperType.typeQName,collectString(wrapperType.elementPath));
              QName aName = new QName(targetNamespace,xpath);
              if (this.keyToAnonymous.containsKey(key)) {
                System.out.println("Internal name collision detected for type: "+aName+ " [key] "+key);
              }
              //System.out.println("Registering anonymous type : " +key+" = "+aName);
              this.keyToAnonymous.put(key,aName);
            }
            // Adds Annotation to modify class name
            if (shortInnerNames) {
              Element element = (Element) cType.getAssociatedDOMNode();
              Element classCustomization = element.getOwnerDocument().createElementNS("http://java.sun.com/xml/ns/jaxb","jaxb:class");
              setNSPrefix("jaxb","http://java.sun.com/xml/ns/jaxb",classCustomization);
              Element appInfoNode = appendAnnotationAppInfo(element);
              classCustomization.setAttribute("name",wrapperType.typeName); 
              appInfoNode.appendChild(classCustomization);
            }
          }
        }         
      }
    }
  }
  
  /**
   * Sets specific namespace declaration.
   * @param prefix
   * @param namespace
   * @param scope
   */
  private static final void setNSPrefix(final String prefix,final String namespace,final Element scope) {
    if (prefix != null) {
      if (prefix.length() == 0) {
        scope.setAttributeNS(NS.XMLNS,"xmlns",namespace); 
      } else {
        scope.setAttributeNS(NS.XMLNS,"xmlns:"+prefix,namespace);
      }
    }
  }  
  
  private TempStruct findType(Vector topTypes,Vector elementTypes, ElementDeclaration ed) {
    TempStruct result = null;
    result = findComplexType(topTypes,ed,"C");
    if (result == null) {      
      result = findComplexTypeInElement(elementTypes,ed,"EC");      
    }
    return result; 
  }
  
  private TempStruct findComplexTypeInElement(Vector elements,ElementDeclaration ed, String prefix) {
    TempStruct result = null;
    for (int i=0; i<elements.size(); i++) {
      Object o = elements.get(i);
      if (o instanceof ElementDeclaration) { // This is element declaration
        ElementDeclaration myElement = (ElementDeclaration) o;
        TypeDefinitionBase type = myElement.getTypeDefinition();
        if (type instanceof ComplexTypeDefinition) {
          ComplexTypeDefinition cType = (ComplexTypeDefinition) type;
          String myPrefix = prefix;
          if (this.addTopLevelPrefix) { // If it needs to be topLevel we have to add the wrapper type name.
            String javaTypeName = NameConverter.jaxrpcCompatible.toClassName(myElement.getName())+"Element";
            myPrefix = javaTypeName+myPrefix;
          }
          TempStruct temp = findElement(cType,ed,myPrefix);  
          if (temp != null) {
            if (result != null) {
              // There are two top level types that are using the element.
              return null;
            }
            String typeLocalName = cType.getName();
            if (typeLocalName.length() == 0) {              
              typeLocalName = DOM.toXPath(cType.getAssociatedDOMNode());
            }          
            result = temp; 
            result.typeQName = new QName(cType.getTargetNamespace(),typeLocalName);
          }
        }
      }
    }
    return result;    
  }
  
  /**
   * Returns the QName of the complex type that contains the specific element declaration.
   * All types are top level so they have local names.
   * @param v
   * @param ed
   * @return
   */
  private TempStruct findComplexType(Vector types,ElementDeclaration ed,String prefix) {
    TempStruct result = null;
    for (int i=0; i<types.size(); i++) {
      Object o = types.get(i);
      if (o instanceof ComplexTypeDefinition) {
        ComplexTypeDefinition cType = (ComplexTypeDefinition) o;
        String myPrefix = prefix;
        if (this.addTopLevelPrefix) { // If it needs to be topLevel we have to add the wrapper type name.
          String javaTypeName = NameConverter.jaxrpcCompatible.toClassName(cType.getName())+"Type";
          myPrefix = javaTypeName+myPrefix;                    
        }
        TempStruct temp = findElement(cType,ed,myPrefix);  
        if (temp != null) {
          if (result != null) {
            // There are two top level types that are using the element.
            return null;
          }
          String typeLocalName = cType.getName();
          if (typeLocalName.length() == 0) {
            throw new RuntimeException(" The top level type should not be anonymous.");
          }          
          result = temp;          
          result.typeQName = new QName(cType.getTargetNamespace(),typeLocalName);          
        }
      }
    }
    return result;
  }
  
  public static final String BIG_A = "A";
  public static final String BIG_C = "C";
  public static final String BIG_S = "S";
  public static final String BIG_E = "E";
  
  /**
   * Finds element declaration inside particle and returns the schema path to it.
   * Returns null if the declaration is not found.
   * @param p
   * @param ed
   * @param name
   * @return
   */
  private boolean searchContent(Particle p, ElementDeclaration ed, Stack<String> complexPath, Stack<String> elementPath) {
    if (p == null) {
      // No particle is passed to the function
      return false;
    }
    Base term = p.getTerm();
    if (term instanceof ModelGroupDefinition) {
      // The term is model group definition (Top level component)
      ModelGroupDefinition mdg = (ModelGroupDefinition) term;
      term = mdg.getModelGroup();
    }
    if (term instanceof ModelGroup) {
      // The particle is a model group
      ModelGroup mg = (ModelGroup) term;
      if (mg.isCompositorAll()) { // All
        complexPath.push(BIG_A);
      }
      if (mg.isCompositorChoice()) { // Choice
        complexPath.push(BIG_C);        
      }
      if (mg.isCompositorSequence()) { // Sequence
        complexPath.push(BIG_S);        
      }      
      Particle[] pa = mg.getParticlesArray(); // Group particles
      for (int i = 0; i < pa.length; i++) {
        // searches the element declaration inside the group particles        
        if (pa[i].getTerm() != null && pa[i].getTerm() instanceof ElementDeclaration) {
          ElementDeclaration element = (ElementDeclaration) pa[i].getTerm();
          if (element.isTopLevel() == false) {
            complexPath.push(String.valueOf(i+1));
            elementPath.push(new QName(element.getTargetNamespace(),element.getName()).toString());
            boolean result = searchContent(pa[i],ed,complexPath,elementPath);
            if (result == true) return true;
            complexPath.pop();
            elementPath.pop();
          }
        } else {
          complexPath.push(String.valueOf(i+1));
          boolean result = searchContent(pa[i],ed,complexPath,elementPath);
          if (result) return result;
          complexPath.pop();
        }
      } 
      complexPath.pop();
    } else if (term instanceof ElementDeclaration) {
      // The particle is element declaration
      complexPath.push(BIG_E);      
      if (ed == term)  {
        return true;    
      }
      TypeDefinitionBase type = ((ElementDeclaration) term).getTypeDefinition();
      if (type instanceof ComplexTypeDefinition) {
        // The name is not found and this is another anonymous complex type
        ComplexTypeDefinition complexType = (ComplexTypeDefinition) type;
        if (complexType.isAnonymous() && complexType.isBuiltIn() == false && complexType.getContentTypeSimpleTypeDefinition() == null && complexType.getContentTypeContentModel() != null) {
          // Complex Type with complex content and anonymous. Dig deeper.
          Particle pp = complexType.getContentTypeContentModel();
          if (fullPath) {
            complexPath.push(BIG_C);
            boolean result = searchContent(pp,ed,complexPath,elementPath);
            if (result)return true;
            complexPath.pop();complexPath.pop();
          } else {
            Stack<String> path = new Stack<String>();
            path.push(BIG_C);
            boolean result = searchContent(pp,ed,path,elementPath);
            if (result) {
              complexPath.clear();
              complexPath.addAll(path);
              return true;
            }
            complexPath.pop();
          }
          
        }
      } else {
        complexPath.pop();
      }
      
    }
    return false;
  }
  
  
  /**
   * Finds element declaration inside complex type. Returns this type unique name inside the complex type.
   * @param ct
   * @param ed
   * @param namePrefix
   * @return
   */
  private TempStruct findElement(ComplexTypeDefinition ct, ElementDeclaration ed, String namePrefix) {    
    if (ct.isBuiltIn()) return null; // Complex type is builtin type
    if (ct.getContentTypeSimpleTypeDefinition() != null) return null; // Complex type is derived from simple type
    Particle p = ct.getContentTypeContentModel(); // Gets content model    
    Stack<String> path = new Stack<String>();
    path.push(namePrefix);
    Stack<String> elementPath = new Stack<String>();    
    boolean result = searchContent(p,ed,path,elementPath); // Searches the content model
    if (result) {
      TempStruct struct = new TempStruct();
      struct.elementPath = elementPath;
      StringBuffer typeName = new StringBuffer();
      for (int i=0; i<path.size(); i++) {
        typeName.append(path.get(i));        
      }
      struct.typeName = typeName.toString();
      return struct;
    }
    return null;
  }
  
  public HashMap<QName, QName> getAllElements() throws SchemaToJavaGeneratorException{           
    return elemToTypeMap;    
  }
  
  public HashMap<String,QName> getAnonymousTypes() {
    return this.keyToAnonymous;
  }
  
  
  public static final String SCHEMA_NAMESPACE2 = "http://www.w3.org/2001/XMLSchema";
  
  /**
   * Creates and adds appinfo customization node.
   * @param contentNode
   * @return
   */
  private Element appendAnnotationAppInfo(Element contentNode) {
    Document doc = contentNode.getOwnerDocument();
    Element annotation = SerializationUtil.getFirstElementChild((Element) contentNode);
    if (annotation == null || !"annotation".equals(annotation.getLocalName())) {
      annotation = doc.createElementNS(SCHEMA_NAMESPACE2,"xs:annotation");
      contentNode.insertBefore(annotation,contentNode.getFirstChild());
    }
    // create the appinfo node
    Element appinfoNode = null;    
    Node currentNode = annotation.getFirstChild();
    while (currentNode != null) {
      if (currentNode.getNodeType() == Node.ELEMENT_NODE && "appinfo".equals(currentNode.getLocalName())) {
        appinfoNode = (Element) currentNode;
        currentNode = null;
        break;
      }
      currentNode = currentNode.getNextSibling();        
    }
    if (appinfoNode == null) {
      appinfoNode = doc.createElementNS(SCHEMA_NAMESPACE2,"xs:appinfo");
      annotation.appendChild(appinfoNode);      
    }   
    return appinfoNode;
  }
   
  /**
   * Creates key that is used to match the anonymous types in the schema and types in the JAXB geberator
   * @param typeNamespace
   * @param elementQName
   * @param wrapperTypeName
   * @return
   */
  public static final String createKey(String typeNamespace,QName elementQName,QName wrapperTypeName,String innerElements) {
    String result = "["+typeNamespace+"]"+innerElements+"<"+elementQName+">"+wrapperTypeName;
    return result;
  }
  
}
