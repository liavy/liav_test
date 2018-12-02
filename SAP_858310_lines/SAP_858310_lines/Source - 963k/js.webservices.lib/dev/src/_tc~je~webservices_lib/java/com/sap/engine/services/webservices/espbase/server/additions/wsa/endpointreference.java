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
package com.sap.engine.services.webservices.espbase.server.additions.wsa;

import java.io.File;
import java.io.StringReader;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;

/**
 * This class represents 'EndpointReference' entity from WS-Addressing specification.
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-12-6
 */
public class EndpointReference implements Serializable {
	static final long serialVersionUID = -1592391201937414814L;
  
  private String address;
  private Set refParameters; //the set contains org.w3c.dom.Element instances
  /**
   * @param address value for the 'Address' property.
   */
  public EndpointReference(String address) {
    this.address = address;
    this.refParameters = Collections.unmodifiableSet(new HashSet());
  }
  /**
   * 
   * @param address value for the 'Address' property.
   * @param refParameters Set containing org.w3c.dom.Element object representing the value of 'ReferenceParameters' property.
   */  
  public EndpointReference(String address, Set refParameters) {
    this(address);
    this.refParameters = Collections.unmodifiableSet(refParameters);
  }
  /**
   * @return the value of 'Address' property.
   */
  public String getAddress() {
    return this.address;
  }
  /**
   * @return Set object containing org.w3c.dom.Element objects which represent the value of 'ReferenceParameters' property. 
   *             
   */
  public Set getRefParameters() {
    return this.refParameters;
  }
  
  public String toString() {
    try {
      StringBuffer b = new StringBuffer();
      b.append("address[").append(this.address.length()).append("]");
      b.append(this.address).append("; ");
      Iterator itr = refParameters.iterator();
      Element e;
      String s;
      while (itr.hasNext()) {
        e = (Element) itr.next();
        s = DOM.toSelfDescribingString(e); 
        b.append("header[" + s.length() + "]");
        b.append(s).append("; ");
      }
      return b.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  public static EndpointReference valueOf(String s) throws Exception {
    final String ADDRESS = "address[";
    final String HEADER = "header[";
    int add = s.indexOf(ADDRESS);
    if (add == -1) {
      throw new IllegalArgumentException("Missing " + ADDRESS + " in " + s);
    }
    int start = add + ADDRESS.length();
    int end = s.indexOf("]", start);
    String addressLength = s. substring(start, end);
    int addL = Integer.parseInt(addressLength);
    
    String address = s.substring(end + 1, end + 1 + addL);
    
    Set headers = new HashSet();
    end += addL + 1;
    
    int hInd = s.indexOf(HEADER, end);
    while (hInd != -1) { //there are headers for parsing
      start = hInd + HEADER.length();
      end = s.indexOf("]", start);
      String headerLength = s.substring(start, end);
      int headerL = Integer.parseInt(headerLength);
      String header = s.substring(end + 1, end + 1 + headerL);
      end = end + 1 + headerL; 
      StringReader r = new StringReader(header);
      Element e = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB, new InputSource(r)).getDocumentElement();
      headers.add(e);
      hInd = s.indexOf(HEADER, end);
    }
    
    return new EndpointReference(address, headers);
  }
  
  public static void main(String[] args) throws Exception {
    Element envelope = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB, new File("d:/temp/soapmessage.xml")).getDocumentElement();
    Element envelope2 = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB, new File("d:/temp/soapmessage.xml")).getDocumentElement();
    Element envelope3 = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB, new File("d:/temp/soapmessage.xml")).getDocumentElement();
    String res;
    String address = "some test addressr";
    Set set = new HashSet();
    set.add(envelope.getFirstChild());
    set.add(envelope2.getFirstChild().getFirstChild());
    set.add(envelope3.getFirstChild().getFirstChild().getFirstChild());
    
    EndpointReference ep = new EndpointReference(address, set);
    res = ep.toString();
    System.out.println(res); 
    ep = EndpointReference.valueOf(res);
    System.out.println(ep.toString());
  }
}
