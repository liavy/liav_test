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
package com.sap.engine.services.webservices.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.InvalidPropertiesFormatException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


import com.sap.engine.lib.xml.parser.URLLoader;
import com.sap.engine.services.webservices.espbase.wsdl.EmptyResolver;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * This class provides static methods for parsing xml to DOM.
 * The implementation uses shared DocumentBuilder instances and in this way descreases
 * the number of DocumentBuilder instances in the JVM.
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov (edited by Chavdar Baikov).
 * @version 1.0, 2005-3-28
 */
public class SharedDocumentBuilders {
  
  private static final String HTTP_HOST  =  "http.proxyHost";
  private static final String HTTP_PORT  =  "http.proxyPort";
  
  /**
   * Constant wrapping namespace-aware document builder instance.
   */
  public static final int NAMESPACE_AWARE_DB  =  0;
  
  private static final Location LOCATION =  Location.getLocation(SharedDocumentBuilders.class);
  
  private static final int DB_COUNT  = 1;// denotes how many DBuilder types are currently supported.
  
  private static final int DB_POOL_SIZE = 4; // denotes how many DBuilder instances are currently pooled. 
  
  private static final InstancesPool[] DB_INSTANCES = new InstancesPool[DB_COUNT];
  
  static {
    // Inits document builder pool.
    for (int i=0; i < DB_INSTANCES.length; i++) {
      DB_INSTANCES[i] = new InstancesPool(DB_POOL_SIZE,DB_POOL_SIZE);
    }
  }
  
  /**
   * Returns proper document builder instance.
   * @param documentBuilderId
   * @return
   * @throws IOException
   */
  private static DocumentBuilder getDocumentBuilder(final int documentBuilderId) throws IOException {
    DocumentBuilder result = (DocumentBuilder) DB_INSTANCES[documentBuilderId].getInstance();
    if (result == null) {
      // There are no pulled instances. Create a new instance.      
      result = createDocumentBuilder(documentBuilderId);
      LOCATION.debugT("SharedDocumentBuilders.getDocumentBuilder(): creates new document builder.");
    }
    return result;
  }  
  
  /**
   * Returns document builder instance to pool.
   * @param documentBuilderId
   * @param dBuilder
   */
  private static void returnDocumentBuilder(final int documentBuilderId, final DocumentBuilder dBuilder) {
    try {
      dBuilder.reset();
      dBuilder.setEntityResolver(EmptyResolver.EMPTY_RESOLVER);      
    } catch (UnsupportedOperationException x) {
      LOCATION.debugT("SharedDocumentBuilders.returnDocumentBuilder(): DocumentBuilder does not support reset.");
    }
    DB_INSTANCES[documentBuilderId].rollBackInstance(dBuilder);    
  }
  
  /**
   * Created new Document builger of specific type.
   * @param documentFuilderID
   * @return
   * @throws IOException 
   */
  private static DocumentBuilder createDocumentBuilder(final int documentBuilderId) throws IOException {
    DocumentBuilder dbResult = null;
    switch (documentBuilderId) {
    case NAMESPACE_AWARE_DB: {
      try {
        // Creates new DocumentBuilder with empty entity resolver.
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setNamespaceAware(true);   
        dbf.setExpandEntityReferences(true);        
        dbResult = dbf.newDocumentBuilder();
        dbResult.setEntityResolver(EmptyResolver.EMPTY_RESOLVER);
      } catch (Exception e) {
        throw new InvalidPropertiesFormatException(e);
      }
      break;
    }
    default:
      break;
    }
    return dbResult;
  }
  
  /**
   * Parses <code>is</code> to DOM.
   * @param dbID document build id.
   * @param is inputsource
   */
  public static Document parse(final int dbID,final InputSource is) throws SAXException, IOException {
    DocumentBuilder dBuilder = getDocumentBuilder(dbID);
    try {
      return dBuilder.parse(is);
    } finally {
      returnDocumentBuilder(dbID,dBuilder);
    }    
  }
  /**
   * Parses <code>is</code> to DOM
   * @param dbID document builder id.
   * @param is inputstream
   */
  public static Document parse(final int dbID,final InputStream is) throws SAXException, IOException {
    DocumentBuilder dBuilder = getDocumentBuilder(dbID);
    try {
      return dBuilder.parse(is);
    } finally {
      returnDocumentBuilder(dbID,dBuilder);
    }    
  }
  /**
   * Parses <code>is</code> to DOM
   * @param dbID document builder id.
   * @param is inputstream
   * @param systemId sustem id
   */
  public static Document parse(final int dbID,final  InputStream is,final String systemId) throws SAXException, IOException {
    DocumentBuilder dBuilder = getDocumentBuilder(dbID);
    try {
      return dBuilder.parse(is,systemId);
    } finally {
      returnDocumentBuilder(dbID,dBuilder);
    }  
  }
  /**
   * Parses <code>uri</code> to DOM
   * @param dbID document builder id.
   * @param uri source
   */
  public static Document parse(final int dbID,final String uri) throws SAXException, IOException {
    DocumentBuilder dBuilder = getDocumentBuilder(dbID);
    try {
      return dBuilder.parse(uri);
    } finally {
      returnDocumentBuilder(dbID,dBuilder);
    }  
  }
  
  /**
   * Parses <code>f</code> to DOM
   * @param dbID document builder id.
   * @param f file
   */
  public static Document parse(final int dbID,final File f) throws SAXException, IOException {
    DocumentBuilder dBuilder = getDocumentBuilder(dbID);
    try {
      return dBuilder.parse(f);
    } finally {
      returnDocumentBuilder(dbID,dBuilder);
    }    
  }
  
  /**
   * Parses <code>f</code> to DOM
   * @param dbID document builder id.
   * @param f file
   */
  public static Document parse(final int dbID,final String url,final EntityResolver resolver) throws SAXException, IOException {
    DocumentBuilder dBuilder = getDocumentBuilder(dbID);
    try {
      dBuilder.setEntityResolver(resolver);
      return dBuilder.parse(url);
    } finally {
      dBuilder.setEntityResolver(EmptyResolver.EMPTY_RESOLVER);
      returnDocumentBuilder(dbID,dBuilder);
    }    
  }
  
  /**
   * Creates <code>Document</code> instance.
   */  
  public static Document newDocument() {
    DocumentBuilder dBuilder = null;
    try {
      dBuilder = getDocumentBuilder(SharedDocumentBuilders.NAMESPACE_AWARE_DB);      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    try {
      return dBuilder.newDocument();
    } finally {
      returnDocumentBuilder(SharedDocumentBuilders.NAMESPACE_AWARE_DB,dBuilder);
    }        
    
  }
  

  
  /**
   * Loads DOMSource from input stream and System id and handles exceptions correctly.
   * @param input
   * @param systemId
   * @return
   * @throws WSDLException
   */
  public static DOMSource loadDOMDocument(final InputStream input,final String systemId) throws WSDLException {
    try {      
      Element root = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB,input).getDocumentElement();      
      DOMSource source = new DOMSource(root, systemId);      
      return source;
    } catch (Exception e) {
      throw new WSDLException(e);
    }
  }  
  
  public static DOMSource loadDOMDocument(String wsdlLocation, String proxyHost, String proxyPort, EntityResolver resolver) throws WSDLException {
    String oldHost="";
    String oldPort="";
    try {
      if (proxyHost != null) {
        oldHost = System.getProperty(HTTP_HOST);
        oldPort = System.getProperty(HTTP_PORT);
        System.setProperty(HTTP_HOST,proxyHost);
        System.setProperty(HTTP_PORT,proxyPort);
      }
      Document document = null;      
      if (resolver != null) {        
        InputSource is = resolver.resolveEntity(null, wsdlLocation);
        if (is != null) {
          document = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB,is);
          // If the location is redirected by resolver to a new location. 
          if (is.getSystemId() != null && !is.getSystemId().equals(wsdlLocation)) {
            wsdlLocation = is.getSystemId();
          }
        } else {
          document = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB,wsdlLocation,resolver);
        }        
      } else {
        document = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB,wsdlLocation);
      }
      Element root = document.getDocumentElement();
//      URL rooturl = new File(".").toURL();
      URL location = URLLoader.fileOrURLToURL(null,wsdlLocation);
      DOMSource source = new DOMSource(root, location.toString());
      return source;
    } catch (Exception e) {
      throw new WSDLException(e);
    } finally {
      if (proxyHost != null) { // Restore the system property
        if (oldHost == null || oldPort == null) {
          System.getProperties().remove(HTTP_HOST);
          System.getProperties().remove(HTTP_PORT);
        } else {
          System.setProperty(HTTP_HOST,oldHost);
          System.setProperty(HTTP_HOST,oldPort);
        }
      }
    }
  }  
}
