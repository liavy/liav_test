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

import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;

/**
 * Instances of this class represent HTTP binding operations. 
 * The class declares specific properties related to the HTTP binding.
 * Only ExtensionElement objects could be attached to instances of this class. 
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-2-8
 */
public class HTTPBindingOperation extends AbstractOperation {      
  /**
   * Location property.
   */
  public static final String LOCATION  =  "location";
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
  /**
   * Input serialization property.
   */
  public static final String INPUT_SERIALIZATION  =  "input-serialization";
  /**
   * Output serialization property.
   */
  public static final String OUTPUT_SERIALIZATION  =  "output-serialization";
  /**
   * Application/x-www-form-urlencoded serialization value.
   */
  public static final String URLENCODED_SERIALIZATION  =  "application/x-www-form-urlencoded";
  /**
   * Application/xml serialization value.
   */
  public static final String MIMEXML_SERIALIZATION  =  "application/xml";
  
  private static final int MASK  = Base.EXTENSION_ELEMENT_ID;
  
  /**
   * Constructs instance with spefic name
   * 
   * @param name opearation name
   * @throws WSDLException
   */   
  public HTTPBindingOperation(String name) throws WSDLException {
    super(Base.HTTPBINDING_OPERATION_ID, Base.HTTPBINDING_OPERATION_NAME, null, name);
  }

  public void appendChild(Base child) throws WSDLException {
    super.appendChild(child, MASK);
  }

  protected void toStringAdditionals(StringBuffer buffer) {
    buffer.append("name=" + name);
  }

}
