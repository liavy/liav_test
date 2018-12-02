/*
 * Copyright (c) 2003 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */

package com.sap.engine.services.webservices.espbase.messaging;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * List with SOAP Headers of SOAP1.1 message. Contains all methods for SOAP header managements.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 1.0
 */
public class SOAPHeaderList {
  
  private Hashtable content = null;
  private Document document = null;
  private Hashtable headerNamespaces = null;
  
  /**
   * Returns enumeration of soap header namespaces.
   * @return
   */
  public Enumeration getHeaderNamespaces() {
    return headerNamespaces.keys();  
  }
  
  /**
   * Returns header namespace from prefix.
   * @param prefix
   * @return
   */
  public String getHeaderNamespacePrefix(String namespace) {
    return (String) headerNamespaces.get(namespace);
  }
  
  public void addHeaderNamespace(String namespace, String prefix) {
    this.headerNamespaces.put(namespace,prefix);
  }
  
  public void resetHeaderNamespaces(Map headerNS) {
    this.headerNamespaces.putAll(headerNS);
  }
  
  /**
   * @return reference to the internal Document object.
   */
  public Document getInternalDocument() {
    return this.document;
  }
  /**
   * Default Constructor.
   * Can not create header noder.
   */  
  public SOAPHeaderList() {
    content = new Hashtable();
  }
  
  /**
   * Creates header list with given parent header documents.
   * @param document
   */
  public SOAPHeaderList(Document document) {
    content = new Hashtable();
    headerNamespaces = new Hashtable();
    this.document = document;
  }
  
  
  /**
   * Adds SOAP Header to header list.
   * Two soap headers with the same QName are not allowed in Header section so if
   * header with the same QName as the element passed is woverwritten.
   * Reason: see SOAP 1.1 section 4.2
   * <p>
   * A header entry is identified by its fully qualified element name, 
   * which consists of the namespace URI and the local name.
   * All immediate child elements of the SOAP Header element MUST be namespace-qualified
   * </p> 
   * @param header
   * @return The overwritten SOAP Header or null if such Header does not exist in the list.
   */
  public Element addHeader(Element header) {
    if (header == null) {
      return null;
    }
    QName headerName = new QName(header.getNamespaceURI(),header.getLocalName());
    
    if (content.containsKey(headerName)) {
      Element result = (Element) content.get(headerName);
      return result;
    }
    content.put(headerName,header);    
    return null;
    
  }
  
  /**
   * Returns the header with given QName. 
   * @param headerName
   * @return Returns NULL if no header with this name is available.
   */  
  public Element getHeader(QName headerName) {    
    if (headerName == null) {
      return null;
    }
    Element result = (Element) content.get(headerName);
    return result;
  }
  
  /**
   * Creates header element using header parent document.
   * throws exception if no default parent document is configured.
   * @param headerName
   * @return
   */
  public Element createHeader(QName headerName) {
    if (headerName == null) {
      throw new IllegalArgumentException("Creating NULL headers is not possible.");
    }
    if (document == null) {
      throw new IllegalStateException("Can not create header element if not parent document is configured for this HeaderList.");
    }
    Element result = document.createElementNS(headerName.getNamespaceURI(),headerName.getLocalPart());
    return result;
  }
  
  /**
   * Removes SOAPHeader from header list.
   * @param headerName
   * @return the removed header or NULL if no such header exists.
   */
  public Element removeHeader(QName headerName) {
    if  (headerName == null) {
      return null;
    }
    if (content.containsKey(headerName)) {
      return (Element) content.remove(headerName);
    }
    return null;
  }
  
  /**
   * Checks if given SOAP Header is added to the message.
   * @param headerName
   * @return
   */
  public boolean containsHeader(QName headerName) {
    if (headerName == null) {
      return false;
    }
    return content.containsKey(headerName);
  }
  
  /**
   * Returns names of all headers in the SOAPMessage.
   * @return
   */
  public QName[] getHeaderNames() {
    QName[] result = new QName[content.size()];
    Enumeration enum1 = content.keys();
    int counter = 0;
    while (enum1.hasMoreElements()) {
      result[counter] = (QName) enum1.nextElement();
      counter++;
    }
    return result;
  }
  
  /**
   * Returns all Header elements in the list.
   * @return
   */
  public Element[] getHeaders() {
    Element[] result = new Element[content.size()];
    Enumeration enum1 = content.elements();
    int counter = 0;
    while (enum1.hasMoreElements()) {
      result[counter] = (Element) enum1.nextElement();
      counter++;
    }
    return result;    
  }  
  
  /**
   * Returns header count.
   * @return
   */
  public int size() {
    return content.size();
  }
  /**
   * Removes all header elements.
   */
  public void clear() {
    content.clear();
  }
}
