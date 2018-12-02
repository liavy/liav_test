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
 * Instances of this class represent reference to XML Schema entities.
 * Instances are used in Parameter objects to specify XML Schema entity
 * to which a specific Parameter refers to. 
 * No chidlren could be attached to objects of this class.
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-18
 */
public class XSDRef extends Base {
  /**
   * Constant denoting that XSDRef instance points to XML Schema element.
   */
  public static final int ELEMENT  =  1;
  /**
   * Constant denoting that XSDRef instance points to XML Schema type.
   */
  public static final int TYPE  =  2;
  /**
   * Constant denoting that XSDRef instance has undefined type.
   * @see XSDRef#ELEMENT
   * @see XSDRef#TYPE
   */
  public static final int UNDEFINED  =  -1;
  
  private QName qname; 
  private int xsdType;
  
  /**
   * Creates object with specified reference type and qname.
   * 
   * @param qname schema type(element) qualified name.
   * @param type type of the reference of this object.
   * @throws WSDLException
   * @see #ELEMENT
   * @see #TYPE
   */
  public XSDRef(QName qname, int type) throws WSDLException {
    super(Base.XSD_TYPEREF_ID, Base.XSD_TYPEREF_NAME, null);
    this.qname = qname;
    this.xsdType = type;
  }
  
  public XSDRef() throws WSDLException {
    this(null, UNDEFINED);
  }
  
	public void appendChild(Base child) throws WSDLException {
    appendChild(child, Base.NONE_ID);
	}
  /**
   * @return qualified named of the XML Schema entity, to which this instance is reference
   */
	public QName getQName() {
		return qname;
	}
  /**
   * @return type of the XML Schema entity(element or type), to which this instance is reference.
   * @see #ELEMENT
   * @see #TYPE
   */
	public int getXSDType() {
		return xsdType;
	}
  /**
   * Sets qualified named of the XML Schema entity, to which this instance is reference
   */
	public void setQName(QName name) {
		qname = name;
	}
  /**
   * Sets type of the XML Schema entity(element or type), to which this instance is reference.
   * @see #ELEMENT
   * @see #TYPE
   */
	public void setXSDType(int type) {
		xsdType = type;
	}
  
	protected void toStringAdditionals(StringBuffer buffer) {
    buffer.append("Qname=" + qname).append(", ").append("type=" + getXSDType());
	}

}
