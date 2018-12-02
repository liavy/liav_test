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
 * Instance of this class holds DOM element object, which DOM element corresponds
 * to wsdl extension element - wsdl element which is not defined in the wsdl standard. 
 * Instances of this class cannot have children.
 *   
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-18
 */
public class ExtensionElement extends Base {
  
  private Element content;
  private String namespace;
  
  /**
   * Creates ExtensionElement instance, with specific content.
   * 
   * @param content DOM element object representing the actual wsdl extension element.
   * @param namespace owner namespace.
   * @throws WSDLException
   */  
  public ExtensionElement(Element content, String namespace) throws WSDLException {
    super(Base.EXTENSION_ELEMENT_ID, Base.EXTENSION_ELEMENT_NAME, null);
    this.content = content;
    this.namespace = namespace == null ? "" : namespace;
  }

  /**
   * Creates ExtensionElement instance, with specific content.
   * 
   * @param namespace owner namespace.
   * @throws WSDLException
   */  
  public ExtensionElement(String namespace) throws WSDLException {
    this(null, namespace);
  }
  
  /**
   * Creates an empty ExtensionElement instance.
   * @throws WSDLException
   */
  public ExtensionElement() throws WSDLException {
    this(null);
  }
  
  /**
   * It is not possible to append any children to this instance. 
   */
	public void appendChild(Base child) throws WSDLException {
    appendChild(child, Base.NONE_ID);
	}

  /**
   * @return DOM element object, representing the extension element
   */
	public Element getContent() {
		return content;
	}
	
  /**
   * @return owner namespace
   */
  public String getOwnerNamespace() {
    return namespace;
  }
  
  /**
   * Sets extension DOM element into this object. 
   * @param element extension DOM element.
   */
	public void setContent(Element element) {
		content = element;
	}
  
  /**
   * @return qualified name of the DOM extension element.
   */
  public QName getQName() {
    return new QName(content.getNamespaceURI(), content.getLocalName());
  }
 
	protected void toStringAdditionals(StringBuffer buffer) {
    String str = content != null ? System.getProperty(LINE_SEPARATOR) + content.toString() : "null";
    buffer.append(str);
	}

}
