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
 * This abstract class provides abstraction for the different bindings
 * that are part of the WSDL and contains commong bindings features.
 * Thus it should be used as a base class for binding specific classes. 
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-18
 */
public abstract class Binding extends Base {
  /**
   * Property key under which is bound the value of 'location' attribute
   * of the wsdl document into which this binding is defined.
   * Used for serialization only. 
   */
  public static final String IMPORT_LOCATION = "ImportLocation";   
                                                                                                                       
  private QName qname;
  private QName interfaceQName;
  private Element sourceElement;
     
  /**
   * 
   * @param qname binging qualified name
   * @param id ID of the binding, it should declared as constant in Base class. @see #Base.getType();
   * @param name object name of the binding, it should be declared as constant in Base class. @see Base.getObjectName();
   * @throws WSDLException
   */
  public Binding(QName qname, int id, String name) throws WSDLException {
    super(id, name, null);
    this.qname = qname;
  }
  
  /**
   * @return binding qualified name, or null if not set.
   */
	public QName getName() {
		return qname;
	}
  
  /**
   * @param name sets binding qualified name
   */
	public void setName(QName name) {
		qname = name;
	}
  
  /**
   * @return referenced interface qualified name, or null if not set. 
   */
	public QName getInterface() {
		return interfaceQName;
	}
  
  /**
   * @param name sets qualifed name of the referenced by the binding interface.
   */
	public void setInterface(QName name) {
		interfaceQName = name;
	}
  
  /**
   * The subclasses should implement this method to return, 
   * string object which denotes the binding type, e.g "soap", "mime"...
   * The value retuned by thiis method is used in #toStringAdditionals() implementation.
   */    
  public abstract String getBindingType();

	protected void toStringAdditionals(StringBuffer buffer) {
    buffer.append("QName=" + qname).append(", interface=" + interfaceQName).append(", binding-type=" + getBindingType());
	}
  
  /**
   * @param el DOM wsdl element from which this Binding object is constructed. 
   */
  public void setDomElement(Element el) {
    this.sourceElement = el;
  }
  
  /**
   * @return DOM wsdl element from which this Binding object is constructed, or null if not set.
   */
  public Element getDomElement() {
    return this.sourceElement;
  }  
  /**
   * Returns ObjectList containing all operation of this object
   */
  public ObjectList getOperations() {
    ObjectList ol = new ObjectList();
    ObjectList children = this.getChildren();
    for (int i = 0; i < children.getLength(); i++) {
      Base b = children.item(i);
      if (b instanceof AbstractOperation) {
        ol.add(b);
      }
    }
    return ol;
  }
  /**
   * Returns operation with name equal to <code>name</code> or null if not found. 
   */  
  public AbstractOperation getOperationByName(String name) {
    ObjectList ops = getOperations();
    for (int i = 0; i < ops.getLength(); i++) {
      AbstractOperation op = (AbstractOperation) ops.item(i);
      if (op.getName().equals(name)) {
        return op;
      }
    }
    return null;
  }
}
