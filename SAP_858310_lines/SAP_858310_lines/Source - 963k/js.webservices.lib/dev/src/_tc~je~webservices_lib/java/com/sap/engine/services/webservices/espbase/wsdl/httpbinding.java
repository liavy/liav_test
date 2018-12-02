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
package com.sap.engine.services.webservices.espbase.wsdl;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;

/**
 * Instances of this class represent HTTP binding.
 * The class declares specific properties related to the HTTP binding.
 * Only HTTPBindingOperation and ExtensionElement object could be attached as children to an
 * instance of this class. 
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-2-8
 */
public class HTTPBinding extends Binding {
  /**
   * Constant representing binding type.
   * Implementation of #getBindingType() returns this constant.
   */
  public static final String HTTP_BINDING  = "http";
  /**
   * Http method property. POST, GET, ...
   */
  public static final String HTTP_METHOD  =  "http-method";
  /**
   * Http GET method value.
   */
  public static final String HTTP_GET_METHOD  =  "GET";
  /**
   * Http POST method value.
   */
  public static final String HTTP_POST_METHOD  =  "POST";
  
  private static final int MASK  =  Base.HTTPBINDING_OPERATION_ID | Base.EXTENSION_ELEMENT_ID;
  /**
   * Constructs HTTPBinding instance.
   * 
   * @param qname binding's qualified name.
   * @throws WSDLException
   */
  public HTTPBinding(QName qname) throws WSDLException {
    super(qname, Base.HTTPBINDING_ID, Base.HTTPBINDING_NAME);
  }

  public String getBindingType() {
    return HTTP_BINDING;
  }

  public void appendChild(Base child) throws WSDLException {
    appendChild(child, MASK);
  }
  
  /**
   * @return ObjectList of all child HTTPBindingOperation instances.
   */  
  public ObjectList getOperations() {
    return getChildren(Base.HTTPBINDING_OPERATION_ID);
  }
  
  /**
   * Creates and appends HTTPBindingOperaiton object to the
   * list of children of this object.
   * 
   * @param name operation name.
   * @return newly created HTTPBindingOperation instance.
   */
  public HTTPBindingOperation appendOperation(String name) throws WSDLException {
    HTTPBindingOperation op = new HTTPBindingOperation(name);
    appendChild(op);
    return op;
  }  
}
