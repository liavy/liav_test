/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.client;

import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.jaxrpc.exceptions.ProxyGeneratorException;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;
import com.sap.engine.services.webservices.wsdl.WSDLException;

/**
 * Class that loads WSDL file and prepares the ProxyGenerator configuration with default customizations.
 * Pass the ProxyGeneratorConfigNew as parameter containing the wsdl path and other required information like Entity Resolver for 
 * WSDL downloading. The prepare step will download the WSDL files and fill the default namespace to package mappings in the
 * proper places in the configuration.
 * 
 * @version 1.0 (2006-11-1)
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public class JAXWS_WSDLPrepare {
  
  private final String JAXWS_PREFIX = "jaxws";
  
  boolean addGlobalBindings = false;
  private boolean serializableClasses = false;
  private HashSet<String> namespacesFound = new HashSet<String>();
  private java.util.Hashtable<String,String> namespaceToPackage = null;
  private String collectionType = null;
  private String outputPackage = null;
  private String elementSuffix = "Element";
  private String typeSuffix = "Type";
  private String anonymousSuffix = "Anonymous";
  private boolean createTopLevel = false;
  private String typeSafeEnumBase = null;
  
  public JAXWS_WSDLPrepare() {
    //this.elementSuffix = "E";
    //this.typeSuffix = "T";
    //this.anonymousSuffix ="A";
  }
  
  public void setTopLevelClasses(boolean flag) {
    this.createTopLevel = flag;
  }
  
  public void setOutputPackage(String packageName) {
    this.outputPackage = packageName;
  }
   
  /**
   * Sets namespace to package mapping preffered to be used.
   * @param map
   */
  public void setNamespaceToPackageMapping(Hashtable<String,String> map) {
    this.namespaceToPackage = map; 
  }
  
  /**
   * Sets the Serialzable glag on the generated jaxb classes.
   * @param flag
   */
  public void setSerializable(boolean flag) {
    this.serializableClasses = flag;
  }
  
  /**
   * Returns the namespaces found in the WSDL schemas.
   * @return
   */
  public HashSet<String> getSchemaNamespaces() {
    return namespacesFound;
  }
  
  /**
   * Downloads the WSDL files inside the ProxyGeneratorConfig in a format apropriate for JAX-WS proxy generation.
   * Note ! Does not load the customization files. 
   * @param config
   */
  public void loadWSDL(ProxyGeneratorConfigNew config) throws ProxyGeneratorException {
    String wsdlPath = config.getWsdlPath();
    if (wsdlPath == null || wsdlPath.length() == 0) {
      throw new IllegalArgumentException("WSDLPath is required to be specified when loading WSDL definitions.");
    }
    JAXWSFileParser fileParser = new JAXWSFileParser();
    if (config.getResolver() != null) {
      fileParser.setExternalResolver(config.getResolver());
    } else {
      fileParser.setHttpProxy(config.getHTTPProxyHost(), config.getHTTPProxyPort());
    }
    // The WSDL files are parsed and the WSDL path is cleared.
    config.setWsdl(null);
    namespacesFound.clear();
    try {
      fileParser.loadWSDL(wsdlPath);      
      config.setJaxWSFiles(fileParser);
      JAXWSFileContainer fileContainer =  fileParser.getFiles();
      List<DOMResource> wsdlFiles =  fileContainer.getWSDLResources();
      List<DOMResource> xsdFiles = fileContainer.getXSDResources();
      // Loads all schema namespaces
      for (int i=0; i<wsdlFiles.size(); i++) {
        DOMResource wsdlFile = wsdlFiles.get(i);
        DOMSource content = wsdlFile.getContent();
        Element wsdlRoot = (Element) content.getNode();
        Element typesSection = getElement(NS.WSDL,"types",wsdlRoot);
        if (typesSection != null) { // There are some schemas inside WSDL
          NodeList nodes = typesSection.getElementsByTagNameNS(NS.XS,"schema");
          for (int j=0; j<nodes.getLength(); j++) {            
            Element schemaNode = (Element) nodes.item(j);
            String targetNamespace = schemaNode.getAttribute("targetNamespace");
            namespacesFound.add(targetNamespace);                     
          }
        }
      }
      for (int i=0; i<xsdFiles.size(); i++) {
        DOMResource xsdFile = xsdFiles.get(i);
        DOMSource content = xsdFile.getContent();
        Element xsdRoot = (Element) content.getNode();
        String targetNamespace = xsdRoot.getAttribute("targetNamespace");
        namespacesFound.add(targetNamespace);                         
      }
    } catch (Exception e) {
      throw new ProxyGeneratorException(ProxyGeneratorException.WSDL_PARSING_PROBLEM, e);
    }    
  }  
  
  /**
   * Sets base simple types that should be converted to enumerations.
   * @param value
   */
  public void setTypeSafeEnumBase(String value) {
    this.typeSafeEnumBase = value;
  }
  
  /**
   * Creates JAX-WS customization file with package customization.
   * @param wsdlLocation
   * @param document
   * @return
   */
  private Element createJAXWSCustomization(final String wsdlLocation,Document document,String outputPackage) {
    Element result = null;
    String bindingElement = JAXWS_PREFIX+":bindings";
    result = document.createElementNS(JAXWSFileParser.JAXWS_NAMESPACE,bindingElement);
    setNSPrefix(JAXWS_PREFIX,JAXWSFileParser.JAXWS_NAMESPACE,result);
    result.setAttribute("wsdlLocation",wsdlLocation);    
    // Adds output package customization if there is output package.
    if (outputPackage != null) {
      Element packageNode = document.createElementNS(JAXWSFileParser.JAXWS_NAMESPACE,bindingElement);
      packageNode.setAttribute("node","wsdl:definitions");
      setNSPrefix("wsdl",NS.WSDL,packageNode);
      Element packageCustomization = document.createElementNS(JAXWSFileParser.JAXWS_NAMESPACE,"jaxws:package");
      packageCustomization.setAttribute("name",outputPackage);
      packageNode.appendChild(packageCustomization);
      result.appendChild(packageNode);
    }
    return result;
  }  
  
  /**
   * Appends JAXB customization nodes to JAXB customization root node.
   * @param rootElement
   * @param namespace
   */
  private void appendJAXBCustomizations(Element rootElement, String namespace) {
    Document document = rootElement.getOwnerDocument();
    // Creates serializable customization and preffered collection type
    if (addGlobalBindings) {      
      Element globalBindings = document.createElementNS(JAXWSFileParser.JAXB_NAMESPACE,"jxb:globalBindings");
      if (this.serializableClasses) {
        Element serializable = document.createElementNS(JAXWSFileParser.JAXB_NAMESPACE,"jxb:serializable");
        globalBindings.appendChild(serializable);
      }
      if (this.collectionType != null) {
        globalBindings.setAttribute("collectionType",this.collectionType);
      }
      globalBindings.setAttribute("underscoreBinding","asCharInWord");
      if (this.createTopLevel) {
        globalBindings.setAttribute("localScoping","toplevel");        
      }
      if (typeSafeEnumBase != null) {
        globalBindings.setAttribute("typesafeEnumBase",this.typeSafeEnumBase);
      }
      setNSPrefix("jxb",JAXWSFileParser.JAXB_NAMESPACE,globalBindings);
      rootElement.appendChild(globalBindings);
      serializableClasses = false;
      collectionType = null;
      addGlobalBindings = false;
    }
    // Creates schema bindings
    if (!namespacesFound.contains(namespace)) {
      return;
    }
    namespacesFound.remove(namespace);
    Element schemaBindings = document.createElementNS(JAXWSFileParser.JAXB_NAMESPACE,"jxb:schemaBindings");
    setNSPrefix("jxb",JAXWSFileParser.JAXB_NAMESPACE,schemaBindings);
    // JAXB Package customization
    if (this.namespaceToPackage != null && namespaceToPackage.get(namespace) != null) {
      Element packageCustomization = document.createElementNS(JAXWSFileParser.JAXB_NAMESPACE,"jxb:package");
      packageCustomization.setAttribute("name",(String) namespaceToPackage.get(namespace));
      schemaBindings.appendChild(packageCustomization);
    }
    // XMLNameTransform customizations
    Element xmlNameTransform = document.createElementNS(JAXWSFileParser.JAXB_NAMESPACE,"jxb:nameXmlTransform");
    schemaBindings.appendChild(xmlNameTransform);
    Element elementNameSuffix = document.createElementNS(JAXWSFileParser.JAXB_NAMESPACE,"jxb:elementName");
    elementNameSuffix.setAttribute("suffix",elementSuffix);
    xmlNameTransform.appendChild(elementNameSuffix);
    Element typeNameSuffix = document.createElementNS(JAXWSFileParser.JAXB_NAMESPACE,"jxb:typeName");
    typeNameSuffix.setAttribute("suffix",typeSuffix);
    xmlNameTransform.appendChild(typeNameSuffix);
    Element anonymousNameSuffix = document.createElementNS(JAXWSFileParser.JAXB_NAMESPACE,"jxb:anonymousTypeName");
    anonymousNameSuffix.setAttribute("suffix",anonymousSuffix);
    xmlNameTransform.appendChild(anonymousNameSuffix);            
    rootElement.appendChild(schemaBindings);
  }
  
  /**
   * Returns the XMLSchema targernamespace.
   * @param schemaRoot
   * @return
   */
  private String getTargerNamespace(Element schemaRoot) {
    return schemaRoot.getAttribute("targetNamespace");    
  }
  
  private Element createJAXBBindingsRoot(Document document) {
    Element result = null;
    result = document.createElementNS(JAXWSFileParser.JAXB_NAMESPACE,"jxb:bindings");
    setNSPrefix("jxb",JAXWSFileParser.JAXB_NAMESPACE,result);
    setNSPrefix("xs",NS.XS,result);
    return result;
  }
  private Element createJAXBBindingsNode(Document document,String schemaLocation) {
    Element result = null;
    result = document.createElementNS(JAXWSFileParser.JAXB_NAMESPACE,"jxb:bindings");
    result.setAttribute("node","/xs:schema");
    result.setAttribute("schemaLocation",schemaLocation);   
    return result;
  }
  
  
  /**
   * Function that creates customizations for the WSDL and XSD files.
   * Clears the WSDL path from the config so the parsed WSDL could be used.
   * @param config
   * @throws ProxyGeneratorException
   * @throws WSDLException 
   */
  public void attachCustomizations(ProxyGeneratorConfigNew config) throws ProxyGeneratorException, WSDLException {
    JAXWSFileParser parser = config.getJaxWSFiles();
    if (parser == null) {
      throw new IllegalArgumentException("There are not WSDL files parsed. Use .loadWSDL() method first to load the WSDL.");
    }
    if (config.getOutputPackage() != null) {
      outputPackage = config.getOutputPackage();
    }
    this.addGlobalBindings = true;
    JAXWSFileContainer fileContainer =  parser.getFiles();
    List<DOMResource> wsdlFiles =  fileContainer.getWSDLResources();
    List<DOMResource> xsdFiles = fileContainer.getXSDResources();
    List<DOMSource> customizationFiles = new ArrayList<DOMSource>();    
    // Parse WSDL files and add JAXWS customizations.
    for (int i=0; i<wsdlFiles.size(); i++) {
      Document document = SharedDocumentBuilders.newDocument();
      DOMResource wsdlFile = wsdlFiles.get(i);
      DOMSource content = wsdlFile.getContent();
      String location = content.getSystemId();
      Element wsdlRoot = (Element) content.getNode();      
      // Creates JAX-WS customization.
      Element jaxWSCustomization = createJAXWSCustomization(location,document,outputPackage);
      Element typesSection = getElement(NS.WSDL,"types",wsdlRoot);
      if (typesSection != null) { // There are some schemas inside WSDL
        NodeList nodes = typesSection.getElementsByTagNameNS(NS.XS,"schema");
        for (int j=0; j<nodes.getLength(); j++) {
          Element schemaNode = (Element) nodes.item(j);
          String xsdXPath = "/wsdl:definitions/wsdl:types/xs:schema["+String.valueOf(j+1)+"]";
          if (nodes.getLength() == 1) {
            xsdXPath = "/wsdl:definitions/wsdl:types/xs:schema";  
          }
          Element jaxWSNode = document.createElementNS(JAXWSFileParser.JAXWS_NAMESPACE,"jaxws:bindings");
          jaxWSNode.setAttribute("node",xsdXPath);
          setNSPrefix("wsdl",NS.WSDL,jaxWSNode);
          setNSPrefix("xs",NS.XS,jaxWSNode);
          appendJAXBCustomizations(jaxWSNode,getTargerNamespace(schemaNode));
          jaxWSCustomization.appendChild(jaxWSNode);                    
        }
      }
      document.appendChild(jaxWSCustomization);
      DOMSource custSource = new DOMSource(jaxWSCustomization, "jaxWSCust"+String.valueOf(i)+".xml");
      customizationFiles.add(custSource);
    }
    if (xsdFiles.size() > 0) {
      Document document = SharedDocumentBuilders.newDocument();
      Element jaxBCustomizationRoot = createJAXBBindingsRoot(document);      
      for (int i=0; i<xsdFiles.size(); i++) {
        DOMResource xsdFile = xsdFiles.get(i);
        DOMSource content = xsdFile.getContent();
        String location = content.getSystemId();
        Element xsdRoot = (Element) content.getNode();
        Element jaxbCustNode = createJAXBBindingsNode(document,location);
        appendJAXBCustomizations(jaxbCustNode,getTargerNamespace(xsdRoot));
        jaxBCustomizationRoot.appendChild(jaxbCustNode);        
      }
      document.appendChild(jaxBCustomizationRoot);
      DOMSource custSource = new DOMSource(jaxBCustomizationRoot, "jaxBCust.xml");
      customizationFiles.add(custSource);      
    }
    
    parser.loadCustomizations(customizationFiles);
    parser.applyJAXWSMappings();
    parser.applyJAXBMappings();
    if (config.getJaxWSWSDLLocation() == null) {
      config.setJaxWSWSDLLocation(config.getWsdlPath());
    }
    config.setWsdlPath(null);
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
  
  /**
   * Gets element child with specific name.
   * @param namespace
   * @param localName
   * @param parent
   * @return
   */
  private static final Element getElement(final String namespace,final String localName,final Element parent) {    
    Node node = parent.getFirstChild();
    while (node != null) {
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element element = (Element) node;
        if (localName.equals(element.getLocalName())) {
          String elementNS = element.getNamespaceURI();
          if (namespace == null && elementNS == null) {
            return element;
          } else {
            if (namespace != null && namespace.equals(elementNS)) {
              return element;
            }
          }           
        }
      }
      node = node.getNextSibling();
    }
    return null;
  }
  /*
  public static void generateClient() throws Exception {
    String outputPackage = "com.sap.my.pack";
    String outputPackageTypes = "com.sap.my.pack.types";
    String outputPath = "E:/workspace/JAXWSTest/";
    // Create ProxyGenerator config and set the WSDL Path. 
    // THE WSDL SHOULD NOT CONTAIN CUSTOMIZATIONS !!! IF IT DOES IT WILL NOT WORK !!!
    ProxyGeneratorConfigNew configNew = new ProxyGeneratorConfigNew();
    configNew.setWsdlPath("file:///E:/WORK/StockQuotes.wsdl");
    // JAXWSWSDLPrepare object will download the WSDL
    JAXWS_WSDLPrepare prepare = new JAXWS_WSDLPrepare();    
    prepare.loadWSDL(configNew);
    // Display all the schema namespaces ("" - stands for no namespace schema)
    // Also set packages to schemas and packages to proxy files, serializable flag to generated classes.
    HashSet<String> namespaces = prepare.getSchemaNamespaces();
    Hashtable<String,String> namespaceMap = new Hashtable<String,String>();
    Iterator it = namespaces.iterator();    
    while (it.hasNext()) {
      String namespace = (String) it.next();
      System.out.println("Schema with namespace :"+namespace);
      namespaceMap.put(namespace,outputPackageTypes);
    }
    // Set custom settings
    prepare.setSerializable(true);
    prepare.setNamespaceToPackageMapping(namespaceMap);    
    prepare.setOutputPackage(outputPackage);
    prepare.attachCustomizations(configNew);
    System.out.println("Ready With Preparation!");
    // Start proxy generation.
    configNew.setGenerationMode(ProxyGeneratorConfigNew.JAXWS_MODE);
    configNew.setOutputPath(outputPath);
    ProxyGeneratorNew generator = new ProxyGeneratorNew();    
    generator.generateAll(configNew);
    System.out.println("Proxy generated !");    
  }
  
  public static void testProxyGeneration(String wsdlPath) throws Exception {
    System.out.println("Generation of proxy for :"+wsdlPath);
    String outputPackage = "com.sap.my.pack";
    String outputPackageTypes = "com.sap.my.pack.types";
    String outputPath = "E:/workspace/JAXWSTest/";
    // Create ProxyGenerator config and set the WSDL Path. 
    // THE WSDL SHOULD NOT CONTAIN CUSTOMIZATIONS !!! IF IT DOES IT WILL NOT WORK !!!
    ProxyGeneratorConfigNew configNew = new ProxyGeneratorConfigNew();
    configNew.setWsdlPath(wsdlPath);
    // JAXWSWSDLPrepare object will download the WSDL
    JAXWS_WSDLPrepare prepare = new JAXWS_WSDLPrepare();    
    prepare.loadWSDL(configNew);
    // Display all the schema namespaces ("" - stands for no namespace schema)
    // Also set packages to schemas and packages to proxy files, serializable flag to generated classes.
    HashSet<String> namespaces = prepare.getSchemaNamespaces();
    Hashtable<String,String> namespaceMap = new Hashtable<String,String>();
    Iterator it = namespaces.iterator();    
    while (it.hasNext()) {
      String namespace = (String) it.next();
      System.out.println("Schema with namespace :"+namespace);
      namespaceMap.put(namespace,outputPackageTypes);
    }
    // Set custom settings
    prepare.setSerializable(true);
    prepare.setNamespaceToPackageMapping(namespaceMap);    
    prepare.setOutputPackage(outputPackage);
    prepare.attachCustomizations(configNew);
    System.out.println("Ready With Preparation!");
    // Start proxy generation.
    configNew.setGenerationMode(ProxyGeneratorConfigNew.JAXWS_MODE);
    configNew.setOutputPath(outputPath);
    ProxyGeneratorNew generator = new ProxyGeneratorNew();    
    generator.generateAll(configNew);
    System.out.println("Proxy generated !");    
  }*/
  /*
  public static void main(String[] args) throws Exception {
    File file = new File("E:/WORK/CAFWSDL/CAF_WSDLs/New Folder");
    File[] files = file.listFiles();
    for (int i=0; i<files.length; i++) {
      if (files[i].getName().endsWith(".xml")) {
        testProxyGeneration(files[i].getAbsolutePath());
      }
    }
  }*/

}
