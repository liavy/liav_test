/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.wsdl;



import com.sap.engine.interfaces.webservices.runtime.HTTPProxyResolver;
import com.sap.engine.lib.xml.StandardDOMParser;
import com.sap.engine.lib.xml.parser.URLLoader;
import com.sap.engine.services.webservices.tools.WSDLDownloadResolver;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
 
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Tool that downloads WSDL file from given location and recreates import structure locally.
 *
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */

public class WSDLImportTool {

  protected String proxyHost = null;
  protected String proxyPort = null;
  protected HTTPProxyResolver proxyResolver;
  protected String username;
  protected String password;
  public static String WSDL_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/";
  public static String SCHEMA_NAMESPACE1 = "http://www.w3.org/2000/10/XMLSchema";
  public static String SCHEMA_NAMESPACE2 = "http://www.w3.org/2001/XMLSchema";
  public static String SCHEMA_NAMESPACE3 = "http://www.w3.org/1999/XMLSchema";
  public static String DEFINITIONS = "definitions";
  public static String TYPES = "types";
  public static String SCHEMA = "schema";
  public static String IMPORT = "import";
  public static final String INCLUDE = "include";
  public static final String PATH_SEPARATOR = "|";
  private int wsdlCounter = 1;
  private int schemaCounter = 1;
  private String localWSDLName = "wsdlfile";
  private String localWSDLRoot = "wsdlroot";
  private String localSchemaName = "wsdlschema";
  private String suffix = null;
  boolean fixImports = false;
  private Transformer transformer = null;
  private StandardDOMParser domparser = null;
  private Hashtable reachedFiles = new Hashtable();
  private EntityResolver wsdlResolver;
  
  public void setWSDLResolver(EntityResolver wsdlResolver) {
    this.wsdlResolver = wsdlResolver;
  }
  
  public EntityResolver getWSDLResolver() {
    return(wsdlResolver);
  }
  
  public void clearWSDLResolver() {
    wsdlResolver = null;
  }
  
  public void setImportFix(boolean flag) {
    this.fixImports = flag;
  }

  public void setSufix(String suffix) {
    if (suffix == null) {
      suffix = "";
    }
    this.suffix = suffix;
  }

  /**
   * Set's Htpp proxy for resolving url based wsdl-z
   * @param proxyHost
   * @param proxyPort
   */
  public void setHttpProxy(String proxyHost,String proxyPort) {
    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
  }
  
  public void setHTTPProxyResolver(HTTPProxyResolver proxyResolver) {
    this.proxyResolver = proxyResolver;
  }
  
  public void setUser(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public void clearHTTPProxy() {
    this.proxyHost = null;
    this.proxyHost = null;
  }

  protected StandardDOMParser getDOMParser() throws SAXException {
    if (this.domparser != null) {
      return this.domparser;
    }
    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
    domparser = new StandardDOMParser();
    domparser.setValidation(false);
    domparser.setReadDTD(false);
    domparser.setNamespaces(true);
    return this.domparser;
  }

  protected DOMSource loadDocument(String fileName) throws WSDLException {
    try {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Document document;
      try {
        /*Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        StandardDOMParser parser = new StandardDOMParser();
        parser.setValidation(false);
        parser.setReadDTD(false);
        parser.setNamespaces(true);*/
        StandardDOMParser parser = getDOMParser();
        EntityResolver resolver = determineWSDLResolver();
        parser.setEntityResolver(resolver);
        InputSource source = resolver.resolveEntity(null, fileName);
        if (source == null) {
          source = new InputSource(fileName);
        }
        document = parser.parse(source);
      } finally {
        Thread.currentThread().setContextClassLoader(loader);
      }

      Element root = document.getDocumentElement();
      DOMSource source = new DOMSource(root, fileName);
      return source;
    } catch (SAXException e) {
      throw new WSDLException("Parser exception occurred:"+e.getMessage(),e);
    } catch (FactoryConfigurationError e) {
      throw new WSDLException("Factory configuration error occurred:"+e.getMessage());
    } catch (IOException e) {
      throw new WSDLException("IO Exception occurred while parsing file:"+e.getMessage(),e);
    }
  }
  
  private EntityResolver determineWSDLResolver() {
    return(wsdlResolver == null ? createDefaultWSDLResolver() : wsdlResolver);
  }
  
  private EntityResolver createDefaultWSDLResolver() {
    WSDLDownloadResolver resolver = new WSDLDownloadResolver();
    if (proxyHost != null && proxyHost.length() > 0
            && proxyPort != null && proxyPort.length() > 0) {
      resolver.setProxyHost(proxyHost);
      resolver.setProxyPort(Integer.parseInt(proxyPort));
    } else if (proxyResolver != null) {
      resolver.setHTTPProxyResolver(proxyResolver);
    }
    resolver.setUsername(username);
    resolver.setPassword(password);
    return(resolver);
  }

  private DOMSource loadDocument(InputStream fileName, String systemId) throws WSDLException {
    try {
      // Uses sap implementation of Dom Parser only.
      //DocumentBuilderFactory factory = new DocumentBuilderFactoryImpl();
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(fileName);
      Element root = document.getDocumentElement();
      DOMSource source = new DOMSource(root, systemId);
      return source;
    } catch (SAXException e) {
      throw new WSDLException("Parser exception occurred:"+e.getMessage(),e);
    } catch (FactoryConfigurationError e) {
      throw new WSDLException("Factory configuration error occurred:"+e.getMessage());
    } catch (ParserConfigurationException e) {
      throw new WSDLException("Parser configuration exception occurred:"+e.getMessage(),e);
    } catch (IOException e) {
      throw new WSDLException("IO Exception occurred while parsing file:"+e.getMessage(),e);
    }
  }

  /**
   * Pass WSDL Definitions element and get all top level imports.
   * @param element
   * @return ArrayList containing import elements in definition.
   */
  private ArrayList getDefinitionsImport(Element element) {
    Node node = element.getFirstChild();
    ArrayList result = new ArrayList();
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
  private ArrayList getSchemaImport(Element element) {
    Node node = element.getFirstChild();
    ArrayList result = new ArrayList();
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

  private String getImportLocation(Element importElement) throws WSDLException {
    // Gets location
    String location = importElement.getAttribute("location");
    if (location.length() == 0) {
      location = importElement.getAttribute("schemaLocation");
    }
    return location;
  }

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
      result = loadDocument(locationURL.toExternalForm());
    }
    return result;

  }
  /**
   * This method sesolves import statements and returns imported xml document.
   * @param importElement
   * @return DOM Source with document contents.
   * @throws WSDLException
   */
  private DOMSource resolveImport(String rootId, Element importElement) throws WSDLException {
    URL rootURL, locationURL;
    DOMSource result = null;
    // Gets location
    String location = getImportLocation(importElement);
    if (location.length() != 0) { // Location is set
      try {
        rootURL = URLLoader.fileOrURLToURL(null, rootId);
        locationURL = URLLoader.fileOrURLToURL(rootURL, location);
      } catch (IOException e) {
        throw new WSDLException(" Unable to resolve location : {" + location + "} from base location: {" + rootId + "}");
      }
      result = loadDocument(locationURL.toExternalForm());
    }
    return result;
  }

  /**
   * Returns true if DOMSource points to WSDL Definition.
   * @param source
   * @return
   */
  private boolean isSchema(DOMSource source) {
    Element root = (Element) source.getNode();
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
   * Returns schema's in wsdl definitions.
   * @param wsdlSource
   * @return
   *
   */
  private ArrayList getWSDLSchemas(DOMSource wsdlSource) {
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
    ArrayList result = new ArrayList();
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
   * Returns true if DOMSOurce points to WSDL Definitions.
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

  private void fillReferences(String path, String destination, Hashtable idMapping) {
    Enumeration en = idMapping.keys();
    String startPath = null;
    while (en.hasMoreElements()) {
      Object key = en.nextElement();
      if (idMapping.get(key).equals(destination)) {
        startPath = (String) key;
        break;
      }
    }
    en = idMapping.keys();
    int pLength = startPath.length();
    Hashtable temp = new Hashtable();
    while (en.hasMoreElements()) {
      String key = (String) en.nextElement();
      if (key.startsWith(startPath) && key.length()>pLength) {
        String permKey = path+key.substring(pLength);
        temp.put(permKey,idMapping.get(key));
      }
    }
    en = temp.keys();
    while (en.hasMoreElements()) {
      Object key = en.nextElement();
      idMapping.put(key,temp.get(key));
    }
    idMapping.put(path,destination);
  }

  private ArrayList saveSchemaSpecial(DOMSource source, String path, File outputDir, String preferedName, Hashtable idMapping) throws WSDLException {
    ArrayList result = new ArrayList();
    String temp = (String) reachedFiles.get(source.getSystemId());
    if (temp != null)  {
      //idMapping.put(path,"*");
      fillReferences(path,temp,idMapping);
      //idMapping.put(path,temp);
      return result;
    }
    /*
    if (idMapping.containsKey(source.getSystemId())) {
      return result;
    } */
    if (preferedName == null) {
      if (suffix == null) {
        suffix = "";
      }
      preferedName = localSchemaName+suffix+Integer.toString(schemaCounter)+".xsd";
      schemaCounter++;
    }
    File outputFile = new File(outputDir,preferedName);
    result.add(outputFile);
    //register that this file will be downloaded
    idMapping.put(path, outputFile.getName());      
    reachedFiles.put(source.getSystemId(),outputFile.getName());
    ArrayList schemaImports = getSchemaImport((Element) source.getNode());
    for (int i = 0; i < schemaImports.size(); i++) {
      Element importTag = (Element) schemaImports.get(i);
      String relativeLocation = getImportLocation(importTag);
      DOMSource domdocument = resolveImport(source.getSystemId(),relativeLocation);
      if (domdocument != null && isSchema(domdocument)) {
        if (isSchema(domdocument)) { // Schema imported
          ArrayList resultFiles = saveSchemaSpecial(domdocument, extend(path,relativeLocation), outputDir, null, idMapping);
          result.addAll(resultFiles);
        } else {
          throw new WSDLException("In wsdl definitions only schema and wsdl's can be imported ! Bug in import statement of :"+source.getSystemId());
        }
      }
      if (fixImports && idMapping.get(extend(path,relativeLocation)) != null) {
        importTag.setAttribute("schemaLocation",(String) idMapping.get(extend(path,relativeLocation)));
      }
    }
    saveDOMSourceSpecial(source,path,outputFile,idMapping); // Downloads file, and registered that it is downloaded
    return result;
  }

  private ArrayList saveSchema(DOMSource source, File outputDir, String preferedName, Hashtable idMapping) throws WSDLException {
    ArrayList result = new ArrayList();
    if (idMapping.containsKey(source.getSystemId())) {
      return result;
    }
    if (preferedName == null) {
      if (suffix == null) {
        suffix = "";
      }
      preferedName = localSchemaName+suffix+Integer.toString(schemaCounter)+".xsd";
      schemaCounter++;
    }
    File outputFile = new File(outputDir,preferedName);
    result.add(outputFile);
    ArrayList schemaImports = getSchemaImport((Element) source.getNode());
    for (int i = 0; i < schemaImports.size(); i++) {
      Element importTag = (Element) schemaImports.get(i);
      DOMSource domdocument = resolveImport(source.getSystemId(),importTag);
      if (domdocument != null && isSchema(domdocument)) {
        if (isSchema(domdocument)) { // Schema imported
          ArrayList resultFiles = saveSchema(domdocument, outputDir, null, idMapping);
          result.addAll(resultFiles);
        } else {
          throw new WSDLException("In wsdl definitions only schema and wsdl's can be imported ! Bug in import statement of :"+source.getSystemId());
        }
      }
      if (fixImports && idMapping.get(domdocument.getSystemId()) != null) {
        importTag.setAttribute("schemaLocation",(String) idMapping.get(domdocument.getSystemId()));
      }
    }
    saveDOMSource(source,outputFile,idMapping); // Downloads file
    return result;
  }

  /**
   * Saves DOM Source to Output file. Also does mapping of System id's to local files.
   * @param source
   * @param outputFile
   * @throws WSDLException
   */
  private void saveDOMSource(DOMSource source, File outputFile, Hashtable idMapping) throws WSDLException {
//    if (idMapping.containsKey(source.getSystemId())) {
//      return;
//    }
    TransformerFactory factory = TransformerFactory.newInstance();
    Transformer transformer = null;
    try {
      transformer = factory.newTransformer();
    } catch (TransformerConfigurationException e) {
      throw new WSDLException("Unable to write file from "+source.getSystemId()+" to "+outputFile.getAbsolutePath());
    }
    transformer.setOutputProperty(OutputKeys.INDENT,"yes");
    try {
      OutputStream out = new FileOutputStream(outputFile);
      try {
        transformer.transform(source, new StreamResult(out));
      } finally {
        out.close();
      }
      idMapping.put(source.getSystemId(), outputFile.getName());
    } catch (IOException ioe) {
      throw new WSDLException("Unable to write file :"+outputFile.getAbsolutePath(), ioe);
    } catch (TransformerException t) {
      throw new WSDLException("Tranformer Exception on file saving :"+outputFile.getAbsolutePath());
    }

  }

  private Transformer getTransformer() throws TransformerConfigurationException {
    if (this.transformer != null) {
      return this.transformer;
    }
    TransformerFactory factory = TransformerFactory.newInstance();
    this.transformer = factory.newTransformer();
    this.transformer.setOutputProperty(OutputKeys.INDENT,"yes");
    return this.transformer;
  }
  /**
   * Saves DOM Source to Output file. Also does mapping of System id's to local files.
   * @param source
   * @param outputFile
   * @throws WSDLException
   */
  private void saveDOMSourceSpecial(DOMSource source, String path,File outputFile, Hashtable idMapping) throws WSDLException {
    Transformer transformer = null;
    try {
      transformer = getTransformer();
    } catch (TransformerConfigurationException e) {
      throw new WSDLException("Unable to write file from "+source.getSystemId()+" to "+outputFile.getAbsolutePath());
    }
    try {
      OutputStream out = new FileOutputStream(outputFile);
      try {
        transformer.transform(source, new StreamResult(out));
      } finally {
        out.close();
      }
//      idMapping.put(path, outputFile.getName());      
//      reachedFiles.put(source.getSystemId(),outputFile.getName());
    } catch (IOException f) {
      throw new WSDLException("Unable to write file :"+outputFile.getAbsolutePath(),f);
    } catch (TransformerException t) {
      throw new WSDLException("Tranformer Exception on file saving :"+outputFile.getAbsolutePath());
    }

  }

  /**
   * Combines two relative paths.
   * @param rootPath
   * @param extensionPath
   * @return
   */
  private String extend(String rootPath, String extensionPath) {
    return rootPath+PATH_SEPARATOR+extensionPath;
  }

  private ArrayList saveWSDLSpecial(DOMSource source, String path,File outputDir, String preferedName, Hashtable idMapping) throws WSDLException {
    // Checks output dir availability
    if (!outputDir.exists()) {
      outputDir.mkdirs();
      if (!outputDir.isDirectory()) {
        outputDir.delete();
      }
    } else {
      if (!outputDir.isDirectory()) {
        throw new WSDLException(outputDir.getAbsolutePath()+" is not a directory !");
      }
    }

    ArrayList result = new ArrayList();
    String temp = (String) reachedFiles.get(source.getSystemId());
    if (temp != null)  {
      //idMapping.put(path,"*");
      fillReferences(path,temp,idMapping);
      //idMapping.put(path,temp);
      return result;
    }

//    if (reachedFiles.containsKey(source.getSystemId()))  {
//      idMapping.put(path,reachedFiles.get(source.getSystemId()));
//      return result;
//    }
    /*
    if (idMapping.containsKey(path)) {
      return result;
    } */
    if (preferedName == null) {
      if (suffix == null) {
        suffix = "";
      }
      preferedName = localWSDLName+suffix+Integer.toString(wsdlCounter)+".wsdl";
      wsdlCounter++;
    }
    File outputFile = new File(outputDir,preferedName);
    result.add(outputFile);
    //register that these files will be downloaded
    idMapping.put(path, outputFile.getName());      
    reachedFiles.put(source.getSystemId(),outputFile.getName());
    //saveDOMSourceSpecial(source,path,outputFile,idMapping); // Downloads file and register that it is downloaded
    ArrayList definitionImports = getDefinitionsImport((Element) source.getNode());
    for (int i=0; i<definitionImports.size(); i++) {
      Element importState = (Element) definitionImports.get(i);
      String relativeLocation = getImportLocation(importState);
      DOMSource domdocument = resolveImport(source.getSystemId(),relativeLocation);
      if (domdocument != null) {
        if (isWSDLDefinitions(domdocument)) { // WSDL Definitions imported
          ArrayList resultFiles = saveWSDLSpecial(domdocument, extend(path,relativeLocation), outputDir, null, idMapping);
          result.addAll(resultFiles);
        } else if (isSchema(domdocument)) { // Schema imported
          ArrayList resultFiles = saveSchemaSpecial(domdocument, extend(path,relativeLocation), outputDir, null, idMapping);
          result.addAll(resultFiles);
        } else {
          throw new WSDLException("In wsdl definitions only schema and wsdl's can be imported ! Bug in import statement of :"+source.getSystemId());
        }
      }
      if (fixImports && idMapping.get(extend(path,relativeLocation)) != null) {
        importState.setAttribute("location",(String) idMapping.get(extend(path,relativeLocation)));
      }
    }
    ArrayList schemas = getWSDLSchemas(source);
    for (int i = 0; i < schemas.size(); i++) {
      Element schema = (Element) schemas.get(i);
      ArrayList schemaImports = getSchemaImport(schema);
      for (int j = 0; j < schemaImports.size(); j++) {
        Element importTag = (Element) schemaImports.get(j);
        String relativeLocation = getImportLocation(importTag);
        DOMSource domdocument = resolveImport(source.getSystemId(),relativeLocation);
        if (domdocument != null) {
          if (isSchema(domdocument)) { // Schema imported
            ArrayList resultFiles = saveSchemaSpecial(domdocument,extend(path,relativeLocation) , outputDir, null, idMapping);
            result.addAll(resultFiles);
          } else {
            throw new WSDLException("In wsdl definitions only schema and wsdl's can be imported ! Bug in import statement of :"+source.getSystemId());
          }
          // This might not work no more
          if (fixImports && idMapping.get(extend(path,relativeLocation)) != null) {
            importTag.setAttribute("schemaLocation",(String) idMapping.get(extend(path,relativeLocation)));
          }
        }

      }
    }
    saveDOMSourceSpecial(source,path,outputFile,idMapping); // Downloads file and register that it is downloaded
    return result;
  }

  /**
   * Gets WSDLDefinitions as DOM Source and saves it in output directory also resolves imports and puts in output dir.
   * @param source
   * @param outputDir
   * @return Returns File[] list of saved files.
   */
  private ArrayList saveWSDL(DOMSource source, File outputDir, String preferedName, Hashtable idMapping) throws WSDLException {
    // Checks output dir availability
    if (!outputDir.exists()) {
      outputDir.mkdirs();
      if (!outputDir.isDirectory()) {
        outputDir.delete();
      }
    } else {
      if (!outputDir.isDirectory()) {
        throw new WSDLException(outputDir.getAbsolutePath()+" is not a directory !");
      }
    }

    ArrayList result = new ArrayList();

    if (idMapping.containsKey(source.getSystemId())) {
      return result;
    }
    if (preferedName == null) {
      if (suffix == null) {
        suffix = "";
      }
      preferedName = localWSDLName+suffix+Integer.toString(wsdlCounter)+".wsdl";
      wsdlCounter++;
    }
    File outputFile = new File(outputDir,preferedName);
    result.add(outputFile);
    ArrayList definitionImports = getDefinitionsImport((Element) source.getNode());
    for (int i=0; i<definitionImports.size(); i++) {
      Element importState = (Element) definitionImports.get(i);
      DOMSource domdocument = resolveImport(source.getSystemId(),importState);
      if (domdocument != null) {
        if (isWSDLDefinitions(domdocument)) { // WSDL Definitions imported
          ArrayList resultFiles = saveWSDL(domdocument, outputDir, null, idMapping);
          result.addAll(resultFiles);
        } else if (isSchema(domdocument)) { // Schema imported
          ArrayList resultFiles = saveSchema(domdocument, outputDir, null, idMapping);
          result.addAll(resultFiles);
        } else {
          throw new WSDLException("In wsdl definitions only schema and wsdl's can be imported ! Bug in import statement of :"+source.getSystemId());
        }
      }
      if (fixImports && idMapping.get(domdocument.getSystemId()) != null) {
        importState.setAttribute("location",(String) idMapping.get(domdocument.getSystemId()));
      }
    }
    ArrayList schemas = getWSDLSchemas(source);
    for (int i = 0; i < schemas.size(); i++) {
      Element schema = (Element) schemas.get(i);
      ArrayList schemaImports = getSchemaImport(schema);
      for (int j = 0; j < schemaImports.size(); j++) {
        Element importTag = (Element) schemaImports.get(j);
        DOMSource domdocument = resolveImport(source.getSystemId(),importTag);
        if (domdocument != null) {
          if (isSchema(domdocument)) { // Schema imported
            ArrayList resultFiles = saveSchema(domdocument, outputDir, null, idMapping);
            result.addAll(resultFiles);
          } else {
            throw new WSDLException("In wsdl definitions only schema and wsdl's can be imported ! Bug in import statement of :"+source.getSystemId());
          }
          if (fixImports && idMapping.get(domdocument.getSystemId()) != null) {
            importTag.setAttribute("schemaLocation",(String) idMapping.get(domdocument.getSystemId()));
          }
        }

      }
    }
    saveDOMSource(source,outputFile,idMapping); // Downloads file
    return result;
  }
  /**
   * Download's remote wsdl structure to a local location and returns file list of downloaded files and  hashtable mapping
   * from absolute real locations to local locations.
   * @param wsdlLocation
   * @param outputLocation
   * @param idMapping
   * @return
   * @throws WSDLException
   */
  public File[] downloadWSDL(String wsdlLocation, File outputLocation, Hashtable idMapping) throws WSDLException {
    DOMSource source = loadDocument(wsdlLocation);
    idMapping.clear();
    reachedFiles.clear();
    idMapping.put("",wsdlLocation);
    idMapping.put("Version","2.0");
    if (isWSDLDefinitions(source)) {
      if (suffix == null) {
        suffix = "";
      }
      ArrayList perm = saveWSDLSpecial(source, source.getSystemId(),outputLocation, localWSDLRoot+suffix+".wsdl",idMapping);
      File[] result = new File[perm.size()];
      for (int i=0; i<perm.size(); i++) {
        result[i] = (File) perm.get(i);
      }
      this.transformer = null;
      this.domparser = null;
      return result;
    } else {
      this.transformer = null;
      this.domparser = null;
      throw new WSDLException("Given "+wsdlLocation+" is not WSDL definitions file !");
    }

  }

  public File[] downloadWSDL(InputStream wsdlStream, File outputLocation, Hashtable idMapping) throws WSDLException {
    DOMSource source = loadDocument(wsdlStream,"root"+this.suffix+".wsdl");
    idMapping.clear();
    reachedFiles.clear();
    idMapping.put("","root"+this.suffix+".wsdl");
    idMapping.put("Version","2.0");
    if (isWSDLDefinitions(source)) {
      if (suffix == null) {
        suffix = "";
      }
      ArrayList perm = saveWSDLSpecial(source, source.getSystemId(),outputLocation, localWSDLRoot+suffix+".wsdl",idMapping);
      File[] result = new File[perm.size()];
      for (int i=0; i<perm.size(); i++) {
        result[i] = (File) perm.get(i);
      }
      this.transformer = null;
      this.domparser = null;
      return result;
    } else {
      this.transformer = null;
      this.domparser = null;
      throw new WSDLException("Given WSDL is not WSDL definitions file !");
    }

  }

/*
  private static void printFileList(File[] fileList, String label) {
    System.out.println("File List : "+label);
    System.out.println("----------");
    for (int i=0; i<fileList.length; i++) {
      System.out.println(fileList[i].getAbsolutePath());
    }
    System.out.println("----------");
  }

  private static void printHashtableContents(Hashtable contents, String label) {
    System.out.println("Hash Map contents: "+label);
    System.out.println("-------------------");
    Enumeration enum = contents.keys();
    while (enum.hasMoreElements()) {
      String key = (String) enum.nextElement();
      String value = (String) contents.get(key);
      System.out.println("["+key+"] = ["+value+"]");

    }
    System.out.println("-------------------");
  }
*/
  /**
   * Demonstration for import tool usage.
   * @param wsdlLocation
   * @param outputLocation
   * @throws WSDLException
   */
  /*
  public static void testIt(String wsdlLocation, String outputLocation) throws WSDLException {
    Hashtable idMapping = new Hashtable();
    WSDLImportTool importTool = new WSDLImportTool();
    importTool.setHttpProxy("10.55.160.4","8080");
    //importTool.setImportFix(true);
    File[] files = importTool.downloadWSDL(wsdlLocation, new File(outputLocation), idMapping);
    printFileList(files,"Files downloaded.");
    printHashtableContents(idMapping,"Mapping received.");
    System.out.println("Generating proxy from mirror location");
    ProxyGenerator generator = new ProxyGenerator();
    ProxyGeneratorConfig config = new ProxyGeneratorConfig(files[0].getAbsolutePath(),files[0].getParent(),"proxy");
    config.setLogicalPortName("lports.xml");
    config.setCompile(true);
    config.setLocationMap(idMapping);
    try {
      generator.generateProxy(config);
      files = config.getAllGeneratedFiles();
    } catch (Exception e) {
      throw  new WSDLException(e);
    }
    printFileList(files,"Proxy Files generated.");
  }
  */

  public static void main(String[] args) throws Exception {
    //testIt("D:/Software/Sun-JWSDP-SA-1.00-BdAD-02/package/distribution/wsi-server/WEB-INF/ConfiguratorImpl.wsdl","d:/downloaded/webservicesnew3/");
    //testIt("D:/Software/Sun-JWSDP-SA-1.00-BdAD-02/package/distribution/wsi-server/WEB-INF/ManufacturerImpl.wsdl","d:/downloaded/webservicesnew5/");
    //testIt("http://ifr.sap.com/ws-notification/WS-BrokeredNotification.wsdl","d:/downloaded/webservicesnew6/");
    //testIt("D:/Problems/ws_base/WS-BaseNotification.wsdl","d:/downloaded/webservicesnew7/");

//    //testIt("E://Docs//wsi//wsdl//Warehouse.wsdl","d:/downloaded/webservicesnew/");
//    testIt("E:/wsrp_service.wsdl","d:/downloaded/perm/");
    //testIt(, "d:/temp/ws/");
    Hashtable idMapping = new Hashtable();
    WSDLImportTool importTool = new WSDLImportTool();
    importTool.setHttpProxy("proxy", "8080");
    importTool.setImportFix(true);
//    File[] files = importTool.downloadWSDL("\\\\plamen-p\\Box\\Misho\\Mathias\\SAP-NW05-oasis-wssx-ws-trust-10.wsdl", new File("d:/temp/ws/"), new Hashtable());
    //File[] files = importTool.downloadWSDL("http://ws.acrosscommunications.com/SMS.asmx?WSDL", new File("d:/temp/ws/"), idMapping);
    File[] files = importTool.downloadWSDL("http://localhost:56000/AttrDefMinOccService/AttrDefMinOccConfig?wsdl", new File("d:/temp/ws/"), idMapping);
  }
  
}
