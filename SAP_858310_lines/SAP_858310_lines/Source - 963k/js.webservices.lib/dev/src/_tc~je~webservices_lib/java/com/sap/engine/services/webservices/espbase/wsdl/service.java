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

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;

/**
 * Instances of this class are java representatives of wsdl service entity.
 * Only Endpoint and ExtensionElement instances are allowed to be attached as children. 
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-18
 */
public class Service extends Base { 
  private static final int MASK  =  Base.ENDPOINT_ID | Base.EXTENSION_ELEMENT_ID;
  
  private QName qname;
  private Element sourceElement;
  
  /**
   * Creates Service instance.
   * 
   * @param qname qualified name of the service
   * @throws WSDLException
   */
  public Service(QName qname) throws WSDLException {
    super(Base.SERVICE_ID, Base.SERVICE_NAME, null);
    this.qname = qname;
  }
  
  /**
   * Creates an empty instance.
   * @throws WSDLException
   */
  public Service() throws WSDLException {
    this(null);
  }

	public void appendChild(Base child) throws WSDLException {
		appendChild(child, MASK);
	} 
  
  /**
   * @return qualified name of this service object.
   */
	public QName getName() {
		return qname;
	}
  
  /**
   * Sets qualified name of the service.
   * @param name service's qualified name.
   */
	public void setName(QName name) {
		qname = name;
	} 
  
  /**
   * Creates and appends as child Endpoint object
   * 
   * @param name name of the endpoint
   * @return newly created Endpoint object
   */
  public Endpoint appendEndpoint(String name) throws WSDLException {
    Endpoint e = new Endpoint(name);
    appendChild(e);
    return e;
  }
  
  
	protected void toStringAdditionals(StringBuffer buffer) {
    buffer.append("QName=" + qname);
	}
  
  /**
   * @return ObjectList instance containing all child Endpoint objects of this object.
   */
  public ObjectList getEndpoints() {
    return getChildren(Base.ENDPOINT_ID);
  }
  /**
   * 
   * @return endpoint with name <code>epName</code> or null.
   */
  public Endpoint getEndpoint(String epName) {
    ObjectList eps = getEndpoints();
    Endpoint ep;
    for (int i = 0; i < eps.getLength(); i++) {
      ep = (Endpoint) eps.item(i);
      if (ep.getName().equals(epName)) {
        return ep;
      }
    }
    return null;
  }
  /**
   * Sets wsdl DOM element, from which this object has been created. 
   * @param el wsdl DOM element.
   */
  public void setDomElement(Element el) {
    this.sourceElement = el;
  }
  
  /**
   * @return wsdl DOM element, from which this object has been created.  
   */
  public Element getDomElement() {
    return this.sourceElement;
  }  
}
