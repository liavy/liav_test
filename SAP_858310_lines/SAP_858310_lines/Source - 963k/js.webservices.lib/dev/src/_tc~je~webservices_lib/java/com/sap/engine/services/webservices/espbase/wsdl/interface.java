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
 * Instances of this class are java representatives of wsdl portType(wsdl1.1) and
 * interface(wsdl2.0) entities.
 * Only Operation and ExtensionElement object could be added as children to this object.
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-18
 */
public class Interface extends Base {
  /**
   * Property key under which is bound the value of 'location' attribute
   * of the wsdl document into which this interface is defined.
   * Used for serialization only. 
   */
  public static final String IMPORT_LOCATION = "ImportLocation";   
  
  private static final int MASK  =  Base.OPERATION_ID  | Base.EXTENSION_ELEMENT_ID | Base.EXTENSION_CONTEXT_ID;
  private QName name;
  private Element sourceElement;
  
  /**
   * Creates Interface instance with spefic qualified name.
   * @param name Interface qualified name.
   * @throws WSDLException
   */
  public Interface(QName name) throws WSDLException {  
    super(Base.INTERFACE_ID, Base.INTERFACE_NAME, null);
    this.name = name;
  }
  
  /**
   * Creates an empty Interface instance
   * @throws WSDLException
   */  
  public Interface() throws WSDLException {
    this(null);
  }
    
	public void appendChild(Base child) throws WSDLException {
	  super.appendChild(child, MASK);	
	}
  
  /**
   * @return Interface qualified name.
   */  
	public QName getName() {
		return name;
	}
  
  /**
   * Sets Interface qualified name.
   * @param name  Interface qualified name.
   */
	public void setName(QName name) {
		this.name = name;
	}
  
  /**
   * Creates and appends Operation child object to this object.
   * @param name operation name
   */
  public Operation appendOperation(String name) throws WSDLException {
    Operation op = new Operation(name);
    appendChild(op);
    return op;
  }
  
  /**
   * @param name operation name
   * @return Operation child object with name equal to <b>name</b> param. If no child Operation is found, null is returned.
   */
  public Operation getOperation(String name) {
    ObjectList ops = getChildren(Base.OPERATION_ID);
    for (int i = 0; i < ops.getLength(); i++) {
      if (((Operation) ops.item(i)).getName().equals(name)) {
        return (Operation) ops.item(i);
      }
    }
    return null;
  }
  
	protected void toStringAdditionals(StringBuffer buffer) {
    buffer.append("QName=" + name);
	}
  
  public ObjectList getOperations() {
    return getChildren(OPERATION_ID);
  }
  
  /**
   * @return DOM element from which this Interface object is constructed, or null if not set. 
   */
	public Element getDomElement() {
		return sourceElement;
	}
  
  /**
   * @param source DOM wsdl element from which this Interface object is constructed. 
   */
	public void setDomElement(Element source) {
		sourceElement = source;
	}

}
