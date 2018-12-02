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
package com.sap.engine.services.webservices.espbase.wsdl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;

import org.xml.sax.EntityResolver;

import com.sap.engine.lib.xml.parser.URLLoader;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLUnmarshalException;
import com.sap.engine.services.webservices.espbase.wsdl.wsdl11.WSDL11Constants;
import com.sap.engine.services.webservices.espbase.wsdl.wsdl11.WSDL11Loader;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;

/**
 * Instances of this class are reusable entities that are capable of loading WSDL documents
 * of different versions(WSLD11, WSDL20,..) from variaty of sources.
 * This class is not thread safe.
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-12-2
 */
public class WSDLLoader {

  private String proxyPort, proxyHost;
  private EntityResolver resolver;
  private URIResolver uriResolver;
  
  /**
   * Loads Definitions from InputStream.
   * @param input
   * @return
   * @throws WSDLException
   */
  public Definitions load(InputStream input) throws WSDLException {
    return load(input, "");
  }

  /**
   * Loads Definitions from InputStream with specified systemId.
   * @param input
   * @param systemId
   * @return
   * @throws WSDLException
   */
  public Definitions load(InputStream input, String systemId) throws WSDLException {
    return load(input, systemId, null);  
  }


  /**
   * Loads Definitions from file.
   * @param wsdlFileName
   * @return Returns built wsdl definitions.
   * @throws WSDLException
   */
  public Definitions load(String wsdlFileName) throws WSDLException {
    return load(wsdlFileName, null);
  }

  /**
   * Loads Definitions from mirror location (cache).
   * Must provide a hashtable with mapping with the following
   * "" -> Original WSDL Location
   * "All imported absolute locations" -> local relative locations
   * This map is returned from WSDL Import tool
   */
  public Definitions load(String wsdlFileName, Hashtable systemIdMap) throws WSDLException {
    //The below 'try' is needed due to changed behavior between JDK1.5 and JDK1.6.
    //The xerces parser does new URL(string), where when the string is 'c:/Document and Settings/xxx' it works fine with JDK1.5, but fails with JDK1.6 with
    //'java.net.MalformedURLException: unknown protocol: c'. While when URL(file:///c:/Document and Settings/xxx) is called it work fine with JDK1.6
    //and this is what the next line is doing - adding file protocol
    try {
      wsdlFileName = URLLoader.fileOrURLToURL(null, wsdlFileName).toString();
    } catch (IOException ioE) {
      throw new WSDLException(ioE);
    }
    DOMSource source  = SharedDocumentBuilders.loadDOMDocument(wsdlFileName,proxyHost,proxyPort,resolver);     
    return loadDefinitions(source, systemIdMap);
  }

  /**
   * Loads WSDL Definitions from mirror location (cache).
   * @param input
   * @param systemId
   * @param systemIdMap
   * @return
   * @throws WSDLException
   */
  public Definitions load(InputStream input, String systemId, Hashtable systemIdMap) throws WSDLException {
    DOMSource source = null;
    source = SharedDocumentBuilders.loadDOMDocument(input,systemId);
    return loadDefinitions(source, systemIdMap);
  }


  
  /**
   * Sets htpp proxy to be used for connections to to urls.
   * @param proxyHost
   * @param proxyPort
   */
  public void setHttpProxy(String proxyHost,String proxyPort) {
    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
  }  
  
  public Definitions loadDefinitions(DOMSource ds, Hashtable mappings) throws WSDLException {
    String dNS = ds.getNode().getNamespaceURI();
    
    if (WSDL11Constants.WSDL_NS.equals(dNS)) {
      WSDL11Loader wsdl11Loader = new WSDL11Loader();
      wsdl11Loader.setHttpProxy(this.proxyHost, this.proxyPort);
      wsdl11Loader.setWSDLResolver(this.resolver);
      wsdl11Loader.setURIResolver(this.uriResolver);
      if (mappings != null) {        
        return wsdl11Loader.loadMirrorWSDLDocument(ds, mappings);
      }
      return wsdl11Loader.loadWSDLDocument(ds);
    } else {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.INVALID_DEFINITIONS_ELEMENT, new Object[]{Definitions.WSDL11 + ", " + Definitions.WSDL20, ds.getNode()});
    }
  }
  

  public void setWSDLResolver(EntityResolver wsdlResolver) {
    this.resolver = wsdlResolver;
  }
  
  public void setURIResolver(URIResolver uriResolver) {
    this.uriResolver = uriResolver;
  }
  
}
