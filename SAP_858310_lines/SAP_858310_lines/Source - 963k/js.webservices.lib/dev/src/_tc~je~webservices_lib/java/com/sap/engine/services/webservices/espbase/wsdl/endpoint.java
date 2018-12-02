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

import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;

/**
 * Instances of this class are java representatives of wsdl port(wsld1.1) and endpoint(wsdl2.0) entities.
 * Only children of type ExtensionElement could be attached.
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-18
 */
public class Endpoint extends Base {
  private static final int MASK = Base.EXTENSION_ELEMENT_ID;
  
  public static final String URL  =  "url";
  
  private String name;
  private QName binding;
  
  /**
   * Creates endpoint object with specific name.
   * @param name name of endpoint objecct 
   * @throws WSDLException
   */
  public Endpoint(String name) throws WSDLException {
    super(Base.ENDPOINT_ID, Base.ENDPOINT_NAME, null);
    this.name = name;
  }
  
  /**
   * Creates endpoint object.
   * @throws WSDLException
   */
  public Endpoint() throws WSDLException {
    this(null);
  }
  
	public void appendChild(Base child) throws WSDLException {
    appendChild(child, MASK);
	}

  /**
   * @return name of the endpoint.
   */
 	public String getName() {
		return name;
	}

  /**
   * Sets endpoint's name.
   * @param name 
   */
	public void setName(String name) {
		this.name = name;
	}
  
  /**
   * @return QName of the refernced by this endpoint object binding.
   */
	public QName getBinding() {
		return binding;
	}
  
  /**
   * Sets the name of the referenced binding.
   * @param name name of the referenced binding.
   */
	public void setBinding(QName name) {
		binding = name;
	}

	protected void toStringAdditionals(StringBuffer buffer) {
    buffer.append("Name=" + name).append(", ").append("binding=" + binding);
	}

}
