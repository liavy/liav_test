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
 * Instances of this class represent wsdl SOAP binding with its specifics.
 * Only SOAPBindingOperation and ExtensionElement entities are allowed to be attached as children. 
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-21
 */
public class SOAPBinding extends Binding {
  /**
   * Constant property key, under which URI(id) value of transport protocol is bound.
   * Example of transport protocol is http. The URI value in this case is - http://schemas.xmlsoap.org/soap/http 
   */
  public static final String TRANSPORT  =  "transport";
  
  public static final String TRANSPORT_SOAP_OVER_HTTP  =  "http://schemas.xmlsoap.org/soap/http";
  /**
   * Constant property key, under which binding style value is bound.
   * For WSDL1.1 valid binding style values are 'document' and 'rpc'. 
   */
  public static final String STYLE =  "style";
  /**
   * Constant representing document style value.
   */
  public static final String DOC_STYLE_VALUE = "document";
  /**
   * Constant representing rpc style value.
   */
  public static final String RPC_STYLE_VALUE = "rpc";
  /**
   * Constant representing binding type.
   * Implementation of #getBindingType() returns this constant.
   */
  public static final String SOAP_BINDING  = "soap";
  /**
   * Constant property key, under which soap version, which is in use by the binding, is bound
   */
  public static final String SOAP_VERSION = "soap-version";
  /**
   * Constant representing SOAP1.1 version.
   * @see #SOAP_VERSION
   */
  public static final String SOAP_VERSION_11 = "soap11";
  /**
   * Constant representing SOAP1.2 version.
   * @see #SOAP_VERSION
   */
  public static final String SOAP_VERSION_12 = "soap12";
  
  private static final int MASK  = Base.SOAPBINDING_OPERATION_ID | Base.EXTENSION_ELEMENT_ID;    
  
  /**
   * Creates instance with specific qualified name.
   * 
   * @param qname binding qualified name.
   * @throws WSDLException
   */  
	public SOAPBinding(QName qname) throws WSDLException {
		super(qname, Base.SOAPBINDING_ID, Base.SOAPBINDING_NAME);
	}

	public void appendChild(Base child) throws WSDLException {
    appendChild(child, MASK);  
	}
  
  /**
   * Creates and appends SOAPBindingOperaiton object to the
   * list of children of this object.
   * 
   * @param name operation name.
   * @return newly created SOAPBindingOperation instance.
   */
  public SOAPBindingOperation appendOperation(String name) throws WSDLException {
    SOAPBindingOperation op = new SOAPBindingOperation(name);
    appendChild(op);
    return op;
  }
  
  /**
   * @return constant value denoting the type of this binding. 
   * @see SOAPBinding#SOAP_BINDING.
   */
	public String getBindingType() {
		return SOAP_BINDING;
	}
  
  /**
   * @param name operation name.
   * @return child SOAPBindingOperation instance with specific name. Null is returned if valid operation is found. 
   */
  public SOAPBindingOperation getOperation(String name) throws WSDLException {
    ObjectList ops = getChildren(Base.SOAPBINDING_OPERATION_ID);
    for (int i = 0; i < ops.getLength(); i++) {
      if (((SOAPBindingOperation) ops.item(i)).getName().equals(name)) {
        return (SOAPBindingOperation) ops.item(i);
      }
    }
    return null;
  }
  
  /**
   * @return ObjectList of all child SOAPBindingOperation instances.
   */  
  public ObjectList getOperations() {
    return getChildren(Base.SOAPBINDING_OPERATION_ID);
  }
  /**
   * @return a constant value, denoting the soap version which is in use by this binding instance.
   *          If no soap version is set, null is returned.
   * @see SOAPBinding#SOAP_VERSION_11
   * @see SOAPBinding#SOAP_VERSION_12
   */ 
  public String getSOAPVersion() {
    return (String) super.getProperty(SOAP_VERSION); 
  }
} 
