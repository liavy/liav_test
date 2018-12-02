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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.*;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.lib.xml.parser.URLLoader;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.jaxrpc.encoding.SerializationUtil;
import com.sap.engine.services.webservices.jaxws.SmartInputSource;
import com.sap.engine.services.webservices.tools.WSDLDownloadResolver;
import com.sap.engine.services.webservices.wsdl.WSDLException;

/**
 * Pasrser for JAX-WS file resources. It parses and loads a collection of JAX-WS WSDL , XSDs and customization files.
 * After loading the customizations they are embedded in the corresponding WSDL or XSD file.
 * @version 1.0 (2006-5-26)
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public class JAXWSFileParser implements URIResolver,EntityResolver {
  
  /* Constants */
  public static final String WSDL_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/";
  public static final String SCHEMA_NAMESPACE1 = "http://www.w3.org/2000/10/XMLSchema";
  public static final String SCHEMA_NAMESPACE2 = "http://www.w3.org/2001/XMLSchema";
  public static final String SCHEMA_NAMESPACE3 = "http://www.w3.org/1999/XMLSchema";
  public static final String JAXWS_NAMESPACE = "http://java.sun.com/xml/ns/jaxws";
  public static final String JAXB_NAMESPACE = "http://java.sun.com/xml/ns/jaxb";
  public static final String DEFINITIONS = "definitions";
  public static final String TYPES = "types";
  public static final String SCHEMA = "schema";
  public static final String IMPORT = "import";
  public static final String INCLUDE = "include";
  public static final String BINDINGS = "bindings";
  public static final String JAXWS_VERSION    = "2.0";
  /* Variables */
  private JAXWSFileContainer fileContainer;
  private DocumentBuilder dombuilder = null;
  private WSDLDownloadResolver downloadResolver;
  private HashSet<String> reachedFiles;
  private EntityResolver externalResolver;
  private String rootSystemId;
  private Transformer transformer;     
  private ArrayList<DOMSource>  handlerConfiguration;
    
  /**
   * @param href
   * @param base
   * @return
   * @throws TransformerException
   */
  public Source resolve(String href, String base) throws TransformerException {
    DOMResource resource = this.fileContainer.getResourceBySystemId(base);
    if (resource != null) {
      String referencedId = resource.getResourceSystemId(href);
      if (referencedId != null) {
        resource = this.fileContainer.getResourceBySystemId(referencedId);
        if (resource != null) {
          return resource.getContent();
        }
      }
    }
    return null;
  }

  public JAXWSFileParser() {
    this.fileContainer = new JAXWSFileContainer();
    this.downloadResolver = new WSDLDownloadResolver();
    reachedFiles = new HashSet<String>();
    handlerConfiguration = new ArrayList<DOMSource>();
  }  
  
  /**
   * Sets external EntityResolver for file parsing.
   * @param resolver
   */
  public void setExternalResolver(EntityResolver resolver) {
    this.externalResolver = resolver;
  }
  
  private Transformer getTransformer() throws WSDLException {
    if (this.transformer != null) {
      return this.transformer;
    }
    TransformerFactory trFact = TransformerFactory.newInstance();
    try {
      this.transformer = trFact.newTransformer();
    } catch (TransformerConfigurationException t) {
      throw new WSDLException(t);      
    }
    return this.transformer;
  }
  
  private DocumentBuilder getDOMBuilder() {
    if (this.dombuilder != null)  {
      return this.dombuilder;
    }
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setExpandEntityReferences(true);
    dbf.setValidating(false);
    dbf.setNamespaceAware(true);
    try {
      dombuilder = dbf.newDocumentBuilder();
    } catch (ParserConfigurationException x) {
      x.printStackTrace();
    }
    dombuilder.setEntityResolver(new EntityResolver() {
      public InputSource resolveEntity(String a, String b) {
        return new InputSource(new StringReader(" "));
      }
    });
    return this.dombuilder;
  }
  
  /**
   * Parses XML document.
   * @param fileName
   * @return
   * @throws WSDLException
   */
  private DOMSource loadDocument(String fileName) throws WSDLException {
    try {
      //The below is needed due to changed behavior between JDK1.5 and JDK1.6.
      //The xerces parser does new URL(string), where when the string is 'c:/Document and Settings/xxx' it works fine with JDK1.5, but fails with JDK1.6 with
      //'java.net.MalformedURLException: unknown protocol: c'. While when URL(file:///c:/Document and Settings/xxx) is called it work fine with JDK1.6
      //and this is what the next line is doing - adding file protocol
      fileName = URLLoader.fileOrURLToURL(null, fileName).toString();
      
      Document document;
      try {
        DocumentBuilder parser = getDOMBuilder();
        InputSource source = null;
        if (this.externalResolver != null) {
          parser.setEntityResolver(this.externalResolver);
          source = this.externalResolver.resolveEntity(null, fileName);
        } else {
          parser.setEntityResolver(this.downloadResolver);
        }
        if (source == null) {
          source = this.downloadResolver.resolveEntity(null, fileName);
        }
        if (source == null) {
          source = new InputSource(fileName);
        }
        document = parser.parse(source);        
      } finally {
        //Thread.currentThread().setContextClassLoader(loader);
      }
      Element root = document.getDocumentElement();
      URL temp = URLLoader.fileOrURLToURL(null,fileName);      
      DOMSource source = new DOMSource(root, temp.toExternalForm());
      return source;
    } catch (SAXException e) {
      throw new WSDLException("Parser exception occurred:"+e.getMessage(),e);
    } catch (FactoryConfigurationError e) {
      throw new WSDLException("Factory configuration error occurred:"+e.getMessage());
    } catch (IOException e) {
      throw new WSDLException("IO Exception occurred while parsing file:"+e.getMessage(),e);
    }
  }
  
  /**
   * Set's Htpp proxy for resolving url based WSDLs.
   * @param proxyHost
   * @param proxyPort
   */
  public void setHttpProxy(String proxyHost,String proxyPort) {
    if (proxyHost != null && proxyHost.length() != 0) {
      this.downloadResolver.setProxyHost(proxyHost);
      this.downloadResolver.setProxyPort(Integer.valueOf(proxyPort));
    }
  }
  
  /**
   * Sets basic authentication settings for Internet based WSDL locations.
   * @param username
   * @param password
   */
  public void setUser(String username, String password) {
    this.downloadResolver.setUsername(username);
    this.downloadResolver.setPassword(password);
  }

  public void clearHTTPProxy() {
    this.downloadResolver.setProxyHost(null);
    this.downloadResolver.setProxyPort(8080);
  }
  
  /**
   * Pass WSDL Definitions element and get all top level imports.
   * @param element
   * @return ArrayList containing import elements in definition.
   */
  private ArrayList<Element> getDefinitionsImport(Element element) {
    Node node = element.getFirstChild();
    ArrayList<Element> result = new ArrayList<Element>();
    while (node != null) {
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element child = (Element) node;
        if (child.getLocalName().equals(IMPORT) && WSDL_NAMESPACE.equals(child.getNamespaceURI())) {
           result.add(child);
        }
      }
      node = node.getNextSibling();
    }
    return result;
  }
  
  /**
   * Pass Schema element and get all top level imports.
   * @param element
   * @return ArrayList containing schema import elements.
   */
  private ArrayList<Element> getSchemaImport(Element element) {
    Node node = element.getFirstChild();
    ArrayList<Element> result = new ArrayList<Element>();
    while (node != null) {
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element child = (Element) node;
        if (child.getLocalName().equals(IMPORT) && SCHEMA_NAMESPACE1.equals(child.getNamespaceURI())) {
           result.add(child);
        }
        if (child.getLocalName().equals(IMPORT) && SCHEMA_NAMESPACE2.equals(child.getNamespaceURI())) {
           result.add(child);
        }
        if (child.getLocalName().equals(IMPORT) && SCHEMA_NAMESPACE3.equals(child.getNamespaceURI())) {
           result.add(child);
        }
        if (child.getLocalName().equals(INCLUDE) && SCHEMA_NAMESPACE1.equals(child.getNamespaceURI())) {
           result.add(child);
        }
        if (child.getLocalName().equals(INCLUDE) && SCHEMA_NAMESPACE2.equals(child.getNamespaceURI())) {
           result.add(child);
        }
        if (child.getLocalName().equals(INCLUDE) && SCHEMA_NAMESPACE3.equals(child.getNamespaceURI())) {
           result.add(child);
        }
      }
      node = node.getNextSibling();
    }
    return result;
  }
  
  /**
   * Returns schema's in wsdl definitions.
   * @param wsdlSource
   * @return
   *
   */
  private ArrayList<Element> getWSDLSchemas(DOMSource wsdlSource) {
    Element root = (Element) wsdlSource.getNode();
    Node node = root.getFirstChild();
    Element types = null;
    while (node != null) {
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element element = (Element) node;
        if (element.getLocalName().equals(TYPES) && WSDL_NAMESPACE.equals(element.getNamespaceURI())) {
          types = element;
          break; // Types element found.
        }
      }
      node = node.getNextSibling();
    }
    ArrayList<Element> result = new ArrayList<Element>();
    if (types != null) {
      node = types.getFirstChild();
      while (node != null) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
          Element element = (Element) node;
          DOMSource source = new DOMSource(element,wsdlSource.getSystemId());
          if (isSchema(source)) {
            result.add(element);
          }
        }
        node = node.getNextSibling();
      }
    }
    return result;
  }    
  
  /**
   * Loads imported XML document.
   * @param rootId
   * @param relativeLocation
   * @return
   * @throws WSDLException
   */
  private DOMSource resolveImport(String rootId, String relativeLocation) throws WSDLException {
    URL rootURL, locationURL;
    DOMSource result = null;
    // Gets location
    if (relativeLocation.length() != 0) { // Location is set
      try {
        rootURL = URLLoader.fileOrURLToURL(null, rootId);
        locationURL = URLLoader.fileOrURLToURL(rootURL, relativeLocation);
      } catch (IOException e) {
        throw new WSDLException(" Unable to resolve location : {" + relativeLocation + "} from base location: {" + rootId + "}");
      }
      String location = locationURL.toExternalForm(); 
      result = loadDocument(location);
    }
    return result;
  }
  
  /**
   * This method sesolves import statements and returns the new import location.
   * @param importElement
   * @return DOM Source with document contents.
   * @throws WSDLException
   */  
  private String resolveImportLocation(String rootId, String relativeId) throws WSDLException {
    URL rootURL, locationURL;
    String result = null;
    // Gets location
    try {
      rootURL = URLLoader.fileOrURLToURL(null, rootId);
      locationURL = URLLoader.fileOrURLToURL(rootURL, relativeId);
    } catch (IOException e) {
      throw new WSDLException(" Unable to resolve location : {" + relativeId + "} from base location: {" + rootId + "}");
    }
    result = locationURL.toExternalForm();
    return result;
  }
  
  /**
   * Returns import location attribute. 
   * @param importElement
   * @return
   * @throws WSDLException
   */
  private String getWSDLLocation(DOMSource bindingSource) throws WSDLException {
    Element bindingElement = (Element) bindingSource.getNode();
    // Gets location    
    String location = bindingElement.getAttribute("wsdlLocation");
    if (location.length() == 0) {
      throw new WSDLException("The 'wsdlLocation' attribute in <jaxws:bindings> node must be specified in file "+bindingSource.getSystemId());
    }
    return location;
  }
  
  /**
   * Returns import location attribute. 
   * @param importElement
   * @return
   * @throws WSDLException
   */
  private String getXSDLocation(DOMSource bindingSource) throws WSDLException {
    Element bindingElement = (Element) bindingSource.getNode();
    // Gets location    
    String location = bindingElement.getAttribute("schemaLocation");
    if (location.length() == 0) {
      throw new WSDLException("The 'schemaLocation' attribute in <jaxb:bindings> node must be specified in file "+bindingSource.getSystemId());
    }
    return location;
  }  
  
  
  
  /**
   * Returns import location attribute. 
   * @param importElement
   * @return
   * @throws WSDLException
   */
  private String getImportLocation(Element importElement) throws WSDLException {
    // Gets location
    String location = importElement.getAttribute("location");
    if (location.length() == 0) {
      location = importElement.getAttribute("schemaLocation");
    }
    return location;
  }
  
  /**
   * Returns true if DOMSource points to WSDL Definition.
   * @param source
   * @return
   */
  private boolean isSchemaElement(Element root) {
    if (root.getLocalName().equals(SCHEMA) && SCHEMA_NAMESPACE1.equals(root.getNamespaceURI())) {
      return true;
    }
    if (root.getLocalName().equals(SCHEMA) && SCHEMA_NAMESPACE2.equals(root.getNamespaceURI())) {
      return true;
    }
    if (root.getLocalName().equals(SCHEMA) && SCHEMA_NAMESPACE3.equals(root.getNamespaceURI())) {
      return true;
    }
    return false;
  }
  
  
  /**
   * Returns true if DOMSource points to WSDL Definition.
   * @param source
   * @return
   */
  private boolean isSchema(DOMSource source) {
    Element root = (Element) source.getNode();
    return isSchemaElement(root);
  }
  
  
  /**
   * Returns true if DOMSource points to WSDL Definitions.
   * @param source
   * @return
   */
  private boolean isWSDLDefinitions(DOMSource source) {
    Element root = (Element) source.getNode();
    if (root.getLocalName().equals(DEFINITIONS) && WSDL_NAMESPACE.equals(root.getNamespaceURI())) {
      return true;
    }
    return false;
  }
  
  /**
   * Returns true if the DOMSource points to JAXWS Extensions. 
   * @param source
   * @return
   */
  private boolean isJAXWSExtensions(DOMSource source) {
    Element root = (Element) source.getNode();
    if (root.getLocalName().equals(BINDINGS) && JAXWS_NAMESPACE.equals(root.getNamespaceURI())) {
      return true;
    }
    return false;    
  }
  
  /**
   * Returns true if the DOMSource points to JAXWS Extensions. 
   * @param source
   * @return
   */
  private boolean isJAXBExtensions(DOMSource source) {
    Element root = (Element) source.getNode();
    if (root.getLocalName().equals(BINDINGS) && JAXB_NAMESPACE.equals(root.getNamespaceURI())) {
      return true;
    }
    return false;    
  }
  
  
  /**
   * Parses Schema contents for import statements.
   * @param resource
   * @throws WSDLException
   */
  private void parseSchema(DOMResource resource) throws WSDLException {
    DOMSource source = resource.getContent();   
    String baseLocation = source.getSystemId();
    ArrayList<Element> schemaImports = getSchemaImport((Element) source.getNode());
    for (Element importTag : schemaImports) {      
      String relativeLocation = getImportLocation(importTag);      
      DOMSource domdocument = resolveImport(baseLocation,relativeLocation);
      if (domdocument != null) { // Schema is imported
        if (isSchema(domdocument)) {
          if (this.reachedFiles.contains(domdocument.getSystemId())) {
            resource.addResourceRef(relativeLocation,domdocument.getSystemId());
          } else {
            resource.addResourceRef(relativeLocation,domdocument.getSystemId());
            DOMResource domResource = new DOMResource();
            domResource.setContent(domdocument);
            this.fileContainer.addXSD(domResource);
            reachedFiles.add(domdocument.getSystemId());
            parseSchema(domResource);
          }
        } else {        
          throw new WSDLException("In wsdl definitions only schema and wsdl's can be imported ! Bug in import statement of :"+domdocument.getSystemId());
        }
      } 
    }
  }  
  
  /**
   * Parses WSDL Document and loads all imported documents.
   * @param resource
   * @throws WSDLException
   */
  private void parseWSDL(DOMResource resource) throws WSDLException {
    DOMSource domSource = resource.getContent();
    String baseLocation = domSource.getSystemId();
    ArrayList<Element> definitionImports = getDefinitionsImport((Element) domSource.getNode());
    for (Element importStatement: definitionImports) {
      String relativeLocation = getImportLocation(importStatement);      
      DOMSource domdocument = resolveImport(baseLocation,relativeLocation);      
      if (domdocument != null) {
        if (isWSDLDefinitions(domdocument)) { // WSDL Definitions imported
          if (this.reachedFiles.contains(domdocument.getSystemId())) {
            // The file is already loaded just add reference to it.
            resource.addResourceRef(relativeLocation,domdocument.getSystemId());
          } else {
            DOMResource domResource = new DOMResource();
            domResource.setContent(domdocument);
            this.fileContainer.addWSDL(domResource);
            resource.addResourceRef(relativeLocation,domdocument.getSystemId());
            reachedFiles.add(domdocument.getSystemId());
            parseWSDL(domResource);
          }
        } else if (isSchema(domdocument)) { // Schema imported
          if (this.reachedFiles.contains(domdocument.getSystemId())) {
            resource.addResourceRef(relativeLocation,domdocument.getSystemId());
          } else {
            DOMResource domResource = new DOMResource();
            domResource.setContent(domdocument);
            this.fileContainer.addXSD(domResource);
            resource.addResourceRef(relativeLocation,domdocument.getSystemId());   
            reachedFiles.add(domdocument.getSystemId());
            parseSchema(domResource);
          }
        } else {
          throw new WSDLException("In wsdl definitions only schema and wsdl's can be imported ! Bug in import statement of :"+domdocument.getSystemId());
        }
      }
    }
    ArrayList<Element> schemas = getWSDLSchemas(domSource);
    for (Element schema : schemas) {
      ArrayList<Element> schemaImports = getSchemaImport(schema);
      for (Element importTag : schemaImports) {
        String relativeLocation = getImportLocation(importTag);
        DOMSource domdocument = resolveImport(baseLocation,relativeLocation);
        if (domdocument != null) {
          if (isSchema(domdocument)) { // Schema imported
            if (this.reachedFiles.contains(domdocument.getSystemId())) {
              resource.addResourceRef(relativeLocation,domdocument.getSystemId());
            } else {
              DOMResource domResource = new DOMResource();
              domResource.setContent(domdocument);
              this.fileContainer.addXSD(domResource);
              resource.addResourceRef(relativeLocation,domdocument.getSystemId());  
              reachedFiles.add(domdocument.getSystemId());
              parseSchema(domResource);
            }
          } else {
            throw new WSDLException("In wsdl definitions only schema and wsdl's can be imported ! Bug in import statement of :"+domSource.getSystemId());
          }
        }
      }
    }
  }
  
  
  /**
   * Download's remote wsdl structure to a resources.
   * @param wsdlLocation
   * @param outputLocation
   * @param idMapping
   * @return
   * @throws WSDLException
   */
  public void loadWSDL(String wsdlLocation) throws WSDLException {
    this.fileContainer.clear();
    this.reachedFiles.clear();
    this.handlerConfiguration.clear();
    this.rootSystemId = null;    
    DOMSource source = loadDocument(wsdlLocation);    
    DOMResource resource = new DOMResource();
    resource.setContent(source);    
    if (isWSDLDefinitions(source)) {
      this.fileContainer.addWSDL(resource);
      this.reachedFiles.add(source.getSystemId());
      this.rootSystemId = source.getSystemId();
      parseWSDL(resource);
      this.dombuilder = null;
    } else {
      this.dombuilder = null;
      throw new WSDLException("Given "+wsdlLocation+" is not WSDL definitions file !");
    }
  }

  /**
   * Loads customizations. Allways call this method after the wsdl is loaded. The WSDL loading method clears all customizations.
   * Also the method resolves relations between customization files and XSD/WSDL files.
   * @param fileLocations
   */
  public void loadCustomizations(List<DOMSource> fileLocations) throws WSDLException {
    for (DOMSource source : fileLocations) {
      if (isJAXWSExtensions(source)) {
        String relativeLocation = getWSDLLocation(source);        
        String wsdlSystemId = resolveImportLocation(source.getSystemId(),relativeLocation);
        if (this.fileContainer.getWSDLBySystemId(wsdlSystemId) == null) {
          throw new WSDLException("The extension file '"+source.getSystemId()+"' points to unavailable:"+wsdlSystemId);
        }
        DOMResource resource = new DOMResource();
        resource.setContent(source);            
        resource.addResourceRef(relativeLocation,wsdlSystemId);
        this.fileContainer.addJaxWS(resource);        
      }
      if (isJAXBExtensions(source)) {
        DOMSource[] containedExtensions = extractNestedJAXB(source);
        for (DOMSource jaxbExtension : containedExtensions) {
          String relativeLocation = getXSDLocation(jaxbExtension);
          String xsdSystemId = resolveImportLocation(jaxbExtension.getSystemId(),relativeLocation);
          if (this.fileContainer.getXSDBySystemId(xsdSystemId) == null) {
            throw new WSDLException("The extension file '"+source.getSystemId()+"' points to unavailable file :"+xsdSystemId);
          }        
          DOMResource resource = new DOMResource();
          resource.setContent(jaxbExtension);              
          resource.addResourceRef(relativeLocation,xsdSystemId);
          this.fileContainer.addJaxB(resource);                  
        }                
      }
    }
  }
  
  
  /**
   * Loads customizations. Allways call this method after the wsdl is loaded. The WSDL loading method clears all customizations.
   * Also the method resolves relations between customization files and XSD/WSDL files.
   * @param fileLocations
   */
  public void loadCustomizations(String[] fileLocations) throws WSDLException {
    for (String fileLocation : fileLocations) {
      DOMSource source = loadDocument(fileLocation);    
      if (isJAXWSExtensions(source)) {
        String relativeLocation = getWSDLLocation(source);        
        String wsdlSystemId = resolveImportLocation(source.getSystemId(),relativeLocation);
        if (this.fileContainer.getWSDLBySystemId(wsdlSystemId) == null) {
          throw new WSDLException("The extension file '"+fileLocation+"' points to unavailable:"+wsdlSystemId);
        }
        DOMResource resource = new DOMResource();
        resource.setContent(source);            
        resource.addResourceRef(relativeLocation,wsdlSystemId);
        this.fileContainer.addJaxWS(resource);        
      }
      if (isJAXBExtensions(source)) {
        DOMSource[] containedExtensions = extractNestedJAXB(source);
        for (DOMSource jaxbExtension : containedExtensions) {
          String relativeLocation = getXSDLocation(jaxbExtension);
          String xsdSystemId = resolveImportLocation(jaxbExtension.getSystemId(),relativeLocation);
          if (xsdSystemId.startsWith("file:///") && this.fileContainer.getXSDBySystemId(xsdSystemId) == null) {
            xsdSystemId = "file:/"+xsdSystemId.substring(8);
          }
          if (this.fileContainer.getXSDBySystemId(xsdSystemId) == null) {
            throw new WSDLException("The extension file '"+fileLocation+"' points to unavailable file :"+xsdSystemId);
          }        
          DOMResource resource = new DOMResource();
          resource.setContent(jaxbExtension);              
          resource.addResourceRef(relativeLocation,xsdSystemId);
          this.fileContainer.addJaxB(resource);                  
        }                
      }
    }
  }
  
  private String getVersion(Element jaxBExtension) {
    String version = jaxBExtension.getAttribute("version");    
    if (version.length() == 0) {
      version = jaxBExtension.getAttributeNS(JAXB_NAMESPACE,"version");      
    }
    if (version.length() == 0) {
      return null;
    } else {
      return version;
    }
  }
  
  /**
   * Extracts all jaxb customizations with schemaLocation set
   * @param jaxbResource
   * @throws SAXException 
   */
  private DOMSource[] extractNestedJAXB(DOMSource jaxbResource) throws WSDLException {
    Element jaxbRoot = (Element) jaxbResource.getNode();
    Document jaxbDocument = jaxbRoot.getOwnerDocument();
    String version = getVersion(jaxbRoot);
    NodeList bindingNodes = jaxbDocument.getElementsByTagNameNS(JAXB_NAMESPACE, BINDINGS);
    ArrayList<Element> results = new ArrayList<Element>();
    int nodeCount = bindingNodes.getLength();
    for (int i = 0; i < nodeCount; i++) {
      Element bindingNode = (Element) bindingNodes.item(i);
      String schemaLocation = bindingNode.getAttribute("schemaLocation");
      if (schemaLocation.length() != 0) { // This is a reference to schema
        results.add(bindingNode);
      }
    }
    DOMSource[] resourceArray = new DOMSource[results.size()];
    
    for (int i=0; i<results.size(); i++) {
      Element jaxBBinding = (Element) results.get(i);
      // Copy namespace declarations
      copyNamespaces(jaxBBinding,jaxBBinding);
      // Detach from the parent node.
      Node parent = jaxBBinding.getParentNode();      
      parent.removeChild(jaxBBinding);
        //StandardDOMParser parser = getDOMParser();
        DocumentBuilder builder = getDOMBuilder();
        Document doc = builder.newDocument();
        jaxBBinding = (Element) doc.importNode(jaxBBinding,true);
        doc.appendChild(jaxBBinding);
      // Set version attribute
      if (version != null && getVersion(jaxBBinding) == null) {
        jaxBBinding.setAttribute("version",version);
      }
      // Create the dom source
      DOMSource domSource = new DOMSource();
      domSource.setNode(jaxBBinding);
      domSource.setSystemId(jaxbResource.getSystemId());
      resourceArray[i] = domSource;
    }
    return resourceArray;    
  }
  
  private void checkJAXWSVersion(DOMSource jaxWSFile) throws WSDLException {
    Element jaxwsRoot = (Element) jaxWSFile.getNode();
    String version = jaxwsRoot.getAttribute("version");        
    if (version != null && version.length() != 0 && !version.equals(JAXWS_VERSION)) {
      throw new WSDLException("Incorrect jaxws binding version attribute !");
    }    
  }
  
  private void applyJaxWSMapping(DOMResource jaxWSResource) throws WSDLException {
    DOMSource jaxWSfile = jaxWSResource.getContent();
    String href = getWSDLLocation(jaxWSfile);
    String systemId = jaxWSResource.getResourceSystemId(href);
    DOMResource wsdlResource = this.fileContainer.getWSDLBySystemId(systemId);
    DOMSource wsdlFile = wsdlResource.getContent();
    checkJAXWSVersion(jaxWSfile);
    // Parse the extesion file
    Document jaxWSDocument = jaxWSfile.getNode().getOwnerDocument();
    Document wsdlDocument = wsdlFile.getNode().getOwnerDocument();
    ArrayList<Node> mapElementToExtensionsIndex = new ArrayList<Node>();
    ArrayList<List<Node>> mapElementToExtensionsBindings = new ArrayList<List<Node>>();
    NodeList bindingNodes = jaxWSDocument.getDocumentElement().getElementsByTagNameNS(JAXWS_NAMESPACE, BINDINGS);
    // XPath API
    XPathFactory xpathFactory = XPathFactory.newInstance();  
    XPath xpathAPI = xpathFactory.newXPath(); 
    int nodeCOunt = bindingNodes.getLength();
    NamespaceContextResolver resolver = new NamespaceContextResolver();
    resolver.setDefaultPrefix("wsdl",WSDL_NAMESPACE);
    for (int i = 0; i < nodeCOunt; i++) {      
      Element elem = (Element) bindingNodes.item(i);
      String xpath = elem.getAttribute("node");
      // root binding declaration, no 'node' attribute, so set it to the root element
      if (xpath.equals("")) {        
        xpath = "/wsdl:definitions";
        resolver.setScope(wsdlDocument.getDocumentElement());
        elem.setAttribute("node", xpath);
      } else {
        // this binding declaration MUST have a parent! the xpath is relative however, make it absolute
        String parentXPath = ((Element) elem.getParentNode()).getAttribute("node");         
        xpath = parentXPath + "/"+ xpath;
        elem.setAttribute("node", xpath);        
        resolver.setScope(elem);
      }
      xpathAPI.setNamespaceContext(resolver);
      Node found;
      try {        
        //found = DOM.toNode(xpath, wsdlDocument);        
        found = (Node) xpathAPI.evaluate(xpath,wsdlDocument.getDocumentElement(),XPathConstants.NODE);
      } catch (XPathExpressionException e) {
        throw new WSDLException("Externa binding contains incorrect xpath !");        
      }
      if (found == null) {
        throw new WSDLException("Externa binding contains incorrect xpath !");
      }
      if (found.getNodeType() == Node.ELEMENT_NODE && isSchemaElement((Element) found)) {
        // JAXB Binding to a Schema
        // create the annotation node
        ((Element) found).setAttributeNS(JAXB_NAMESPACE,"jaxb:version","2.0");
        ((Element) found).setAttributeNS(NS.XMLNS,"xmlns:jaxb",JAXB_NAMESPACE);
        ArrayList<Element> globalNodes = new ArrayList<Element>();
        ArrayList<Element> jaxbNodes = new ArrayList<Element>();
        Node currentNode = elem.getFirstChild();
        while (currentNode != null) {
          if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
            if (BINDINGS.equals(currentNode.getLocalName())) {
              jaxbNodes.add((Element) currentNode);              
            } else {
              globalNodes.add((Element) currentNode);
            }
          }
          currentNode = currentNode.getNextSibling();
        }
        if (globalNodes.size() > 0) {
          for (int j=0; j<globalNodes.size();j++) {
            Element insertMe = globalNodes.get(j);
            Element appInfoNode = createAppinfoNode((Element) found);
            insertMe = (Element) wsdlDocument.importNode(insertMe, true);
            // copy namespace declarations
            copyNamespaces(insertMe,insertMe);
            appInfoNode.appendChild(wsdlDocument.importNode(insertMe,true));                       
          }
        }
        XPathFactory xpathFactoryTemp = XPathFactory.newInstance();
        XPath xpathAPITemp = xpathFactory.newXPath(); 
        NamespaceContextResolver resolverTemp = new NamespaceContextResolver();
        xpathAPITemp.setNamespaceContext(resolverTemp);                      
        for (int j=0; j<jaxbNodes.size(); j++) {
          Element jaxbNode = jaxbNodes.get(j);
          applyJaxBMapping((Element) found,jaxbNode,xpath,xpathAPITemp,resolverTemp);                        
        }
      } else {
        ArrayList<Node> allBindingMods = new ArrayList<Node>();
        Node currentNode = elem.getFirstChild();
        while (currentNode != null) {
          if (currentNode.getNodeType() == Node.ELEMENT_NODE && !BINDINGS.equals(currentNode.getLocalName())) {
            // Detects handler configuration
            if ("handler-chains".equals(currentNode.getLocalName())) {
              DOMSource handlerConfigFile = new DOMSource();
              handlerConfigFile.setNode(currentNode);
              handlerConfigFile.setSystemId(wsdlFile.getSystemId());
              handlerConfiguration.add(handlerConfigFile);
            } else {               
              allBindingMods.add(currentNode);
            }
          }
          currentNode = currentNode.getNextSibling();
        }
        // map a node from the input WSDL to all required modifications
        mapElementToExtensionsIndex.add(found);
        mapElementToExtensionsBindings.add(allBindingMods);        
      }
    }
    //Modify the wsdl file 
    for (int i=0; i < mapElementToExtensionsIndex.size(); i++) {
      Element toMod = (Element) mapElementToExtensionsIndex.get(i);
      List<Node> mods = mapElementToExtensionsBindings.get(i);
      Element bindingEl = wsdlDocument.createElementNS(JAXWS_NAMESPACE,"jaxws:bindings");
      bindingEl.setAttributeNS(NS.XMLNS,"xmlns:jaxws",JAXWS_NAMESPACE);
      for (int j = 0; j < mods.size(); j++) {
        Element temp = (Element) mods.get(j);
        copyNamespaces(temp,temp);
        Element insertMe = (Element) wsdlDocument.importNode(temp, true);
        bindingEl.appendChild(insertMe);
      }
      toMod.appendChild(bindingEl);
    }    
  }
  
  /**
   * Returns handler configurations.
   * @return
   */
  public ArrayList<DOMSource> getHandlerConfigurations() {
    return this.handlerConfiguration;
  }
  
  /**
   * Gets the XSD Component appinfo node.
   * @param xsdComponent
   * @return
   */
  private Element createAppinfoNode(Element xsdComponent) {
    Element annotation = SerializationUtil.getFirstElementChild(xsdComponent);
    if (annotation == null || !"annotation".equals(annotation.getLocalName())) {
      Element temp = annotation;
      annotation = xsdComponent.getOwnerDocument().createElementNS(SCHEMA_NAMESPACE2,"xs:annotation");
      xsdComponent.insertBefore(annotation,temp);      
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
      appinfoNode = xsdComponent.getOwnerDocument().createElementNS(SCHEMA_NAMESPACE2,"xs:appinfo");
      annotation.appendChild(appinfoNode);
    } 
    return appinfoNode;
  }
  
  /**
   * Applies JaxB mapping to a XSD node inside WSDL file.
   * @param xmlSchemaNode
   * @param jaxbBindingNode
   * @param parentXPath
   * @param xpathAPI
   * @param resolver
   * @throws WSDLException
   */
  private void applyJaxBMapping(final Element xmlSchemaNode,final Element jaxbBindingNode,final String parentXPath,final XPath xpathAPI,final NamespaceContextResolver resolver) throws WSDLException {    
    String xpath = jaxbBindingNode.getAttribute("node");      
    if (xpath.equals("")) {
      // root binding declaration, no 'node' attribute, so set it to the root element
      throw new WSDLException("No node attribute is specified in xsd binding node !");
    } else {
      if (parentXPath != null && parentXPath.length() != 0) {
        if (!xpath.startsWith("/")) {
          xpath = parentXPath + "/" + xpath;
        } else {
          xpath = parentXPath + xpath;
        }
      }
      resolver.setScope(jaxbBindingNode);
    }
    Node found;
    try {        
      found = (Node) xpathAPI.evaluate(xpath,xmlSchemaNode.getOwnerDocument().getDocumentElement(),XPathConstants.NODE);
    } catch (XPathExpressionException e) {
      throw new WSDLException("Externa binding contains incorrect xpath ["+xpath+"] !",e);        
    }
    if (found == null) {
      throw new WSDLException("Externa binding contains incorrect xpath ["+xpath+"] !");
    }
    if (found.getNodeType() != Node.ELEMENT_NODE) {
      throw new WSDLException("Externa binding contains incorrect xpath ["+xpath+"] !");
    }
    ArrayList<Element> innerBindings = new ArrayList<Element>();
    ArrayList<Element> appendElements = new ArrayList<Element>();
    Node currentNode = jaxbBindingNode.getFirstChild();
    while (currentNode != null) {
      if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
        if (BINDINGS.equals(currentNode.getLocalName())) {
          innerBindings.add((Element) currentNode);
        } else {
          appendElements.add((Element) currentNode);
        }
      }
      currentNode = currentNode.getNextSibling();
    }
    if (appendElements.size() > 0) {
      Element appInfoNode = createAppinfoNode((Element) found);
      for (int i=0; i<appendElements.size(); i++) {
        Element insertMe = appendElements.get(i);
        copyNamespaces(insertMe,insertMe);
        appInfoNode.appendChild(found.getOwnerDocument().importNode(insertMe,true));
      }            
    }
    for (int i=0; i<innerBindings.size(); i++) {
      Element innerBinding = innerBindings.get(i);
      applyJaxBMapping(xmlSchemaNode,innerBinding,xpath,xpathAPI,resolver);
    }
  }

  private void applyJaxBMapping(DOMResource jaxBResource) throws WSDLException {
    DOMSource jaxBfile = jaxBResource.getContent();
    String href = getXSDLocation(jaxBfile);
    String systemId = jaxBResource.getResourceSystemId(href);
    DOMResource xsdResource = this.fileContainer.getXSDBySystemId(systemId);
    DOMSource xsdFile = xsdResource.getContent();
    // Parse the extesion file
    Document jaxBDocument = jaxBfile.getNode().getOwnerDocument();
    Document xsdDocument = xsdFile.getNode().getOwnerDocument();
    Element jaxbElement = jaxBDocument.getDocumentElement();
    Element xsdElement = xsdDocument.getDocumentElement();
    // Sets the jaxb version attribute
    String version = getVersion(jaxbElement);
    if (version == null) {
      version = "1.0";
    }
    xsdElement.setAttributeNS(JAXB_NAMESPACE,"jaxb:version",version);
    xsdElement.setAttributeNS(NS.XMLNS,"xmlns:jaxb",JAXB_NAMESPACE);        
    // XPath API
    XPathFactory xpathFactory = XPathFactory.newInstance();
    XPath xpathAPI = xpathFactory.newXPath(); 
    NamespaceContextResolver resolver = new NamespaceContextResolver();
    xpathAPI.setNamespaceContext(resolver);
    resolver.setScope(jaxbElement);
    String xpath = jaxbElement.getAttribute("node");      
    if (xpath.equals("")) {
      String prefix = resolver.getPrefix(NS.XS);
      xpath = "/"+prefix+":"+"schema";
      jaxbElement.setAttribute("node",xpath);
    }
    applyJaxBMapping(xsdElement,jaxbElement,"",xpathAPI, resolver);    
  }
  
  private void copyNamespaces(Element sourceElement, Element destinationElement) {
    // Get all valid namespace mappings in scope
    Hashtable hash = DOM.getNamespaceMappingsInScope(sourceElement);
    Enumeration enumeration = hash.keys();
    while (enumeration.hasMoreElements()) {
      String key = (String) enumeration.nextElement();
      String value = (String) hash.get(key);
      if (key == null || key.length() == 0) {
        destinationElement.setAttributeNS(NS.XMLNS,"xmlns",value);
      } else {
        destinationElement.setAttributeNS(NS.XMLNS,"xmlns:"+key,value);
      }
    }              
  }
    
  /**
   * Processes all JAXWS mappings to the corresponding WSDL files.
   * After the mappings are inlined in the WSDLs they are removed from the file list.
   */
  public void applyJAXWSMappings() throws WSDLException {
    List<DOMResource> jaxwsFiles = this.fileContainer.getJaxWSResources();
    for (DOMResource jaxWSFile : jaxwsFiles) {
      applyJaxWSMapping(jaxWSFile);      
    }
    jaxwsFiles.clear();
  }
  
  /**
   * Processes all JAXB mappings to the correspoinging JAXB files.
   * After the mappings are inlined in the XSDs they are removed from the file list. 
   */
  public void applyJAXBMappings() throws WSDLException {
    List<DOMResource> jaxBFiles = this.fileContainer.getJaxBResources();
    for (DOMResource jaxBFile : jaxBFiles) {
      applyJaxBMapping(jaxBFile);      
    }
    jaxBFiles.clear();    
  }
  
  /**
   * Returns the loaded files.
   * @return
   */
  public JAXWSFileContainer getFiles() {
    return this.fileContainer;
  }
  
  /**
   * Returns the root WSDL system id.
   * @return
   */
  public String getRootWSDLId() {
    return this.rootSystemId;
  }  
  
  /**
   * This
   * @return
   */
  public DOMSource getRootWSDL() {
    return this.fileContainer.getResourceBySystemId(this.rootSystemId).getContent();
  }
  
  
  
  /**
   * 
   * @param publicId
   * @param systemId
   * @return
   */
  public InputSource resolveEntity(String publicId, String systemId) {
    DOMResource resource = this.fileContainer.getXSDBySystemId(systemId);
    if (resource == null) {
      return null;
    }
    DOMSource domSource = resource.getContent();    
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    StreamResult res = new StreamResult(buffer);
    try {
      Transformer transformer = this.getTransformer();    
      transformer.transform(domSource, res);
    } catch (TransformerException e) {
      e.printStackTrace(); // What happened !!!
      return null;
    } catch (WSDLException x) {
      x.printStackTrace();
      return null;
    }
    ByteArrayInputStream inBuffer = new ByteArrayInputStream(buffer.toByteArray());
    InputSource schemaSource = new SmartInputSource(inBuffer);       
    schemaSource.setSystemId(domSource.getSystemId());    
    return schemaSource;
  }
  
}
