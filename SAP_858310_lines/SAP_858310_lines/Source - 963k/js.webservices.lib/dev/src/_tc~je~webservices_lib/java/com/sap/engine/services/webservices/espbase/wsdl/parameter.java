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
 * Instances of this class represent parameter entities that are used
 * by portType(interface)/operation wsdl entities.
 * The Parameter object has type. Parameter types are defined as contants in the class - Parameter.IN, Parameter.OUT,...
 * Only XSDTypeRef and ExtensionElement objects are allowed to be attached as children to Parameter object.
 * The XSDTypeRef object specifies the xsd type of the parameter.
 *   
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-18
 */
public class Parameter extends Base {
  private static final int MASK  = Base.XSD_TYPEREF_ID | Base.EXTENSION_ELEMENT_ID;  
  /**
   * Denotes 'in' parameter type.
   */
  public static final int IN  =  1;
  /**
   * Denotes 'out' parameter type. 
   */
  public static final int OUT  =  2;
  /**
   * Denotes 'inout' parameter type. 
   */
  public static final int INOUT  =  4;
  /**
   * Denotes 'fault' parameter type. 
   */
  public static final int FAULT  =  8;
  /**
   * Denotes 'return' parameter. 
   */
  public static final int RETURN  =  16;
  /**
   * Denotes undefined parameter. 
   */
  public static final int UNDEFINED  =  -1;
  
  private String name;
  private int paramType;
  
  /**
   * Creates Parameter object.
   * 
   * @param name name of the parameter
   * @param type parameter type - in, out, ...
   * @throws WSDLException
   */
  public Parameter(String name, int type) throws WSDLException {
    super(Base.PARAMETER_ID, Base.PARAMETER_NAME, null);
    this.name = name;
    this.paramType = type;
  }
  
  /**
   * Creates Parameter object.
   * @throws WSDLException
   */
  public Parameter() throws WSDLException {
    this(null, UNDEFINED);
  }

	public void appendChild(Base child) throws WSDLException {
    if (child.getType() == Base.XSD_TYPEREF_ID) {
      XSDRef tRef = getXSDTypeRef(); 
      if (tRef != null) { //already has typeRef, remove it and attach the new one
        removeChild(tRef);
      }
    }
		super.appendChild(child, MASK);
	}
  
  /**
   * @return the name of the parameter
   */
	public String getName() {
		return name;
	}
  
  /**
   * Sets name of this parameter object
   * @param name parameter name
   */
	public void setName(String name) {
		this.name = name;
	}
  
  /**
   * Creates and appends as child XSDTypeRef object, denoting the xsd type of the parameter.
   * Each Parameter object could have only one XSDTypeRef child.
   * 
   * @param qname XSD qualified name - element qname or type qname
   * @param type XSDTypeRef type. Valid values here are avaiable as constants - XSDTypeRef.ELEMENT, XSDTypeRef.TYPE,...  
   * @return newly created XSDTypeRef object
   */
  public XSDRef appendXSDTypeRef(QName qname, int type) throws WSDLException {
    XSDRef xsdRef = new XSDRef(qname, type);
    appendChild(xsdRef);
    return xsdRef;
  }
  
  /**
   * @return XSDTypeRef object attached to this Parameter object, or null if none is attached. 
   */
  public XSDRef getXSDTypeRef() {
    ObjectList list = getChildren(Base.XSD_TYPEREF_ID);
    if (list.getLength() == 1) {
      return (XSDRef) list.item(0);
    }
    
    return null;
  }
  
  /**
   * @return parameter type. Valid values are constants defined in Parameter class - Parameter.IN, Parameter.OUT, ... 
   */
	public int getParamType() {
		return paramType;
	}
  
  /**
   * Sets parameter type.
   * @param i parameter type, Valid values are constants defined in Parameter class - Parameter.IN, Parameter.OUT, ...
   */
 	public void setParamType(int i) {
    paramType = i;
	}
  
	protected void toStringAdditionals(StringBuffer buffer) {
    buffer.append("Name=" + name).append(", type=" + getParamType());
	}

}
