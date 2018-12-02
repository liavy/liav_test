/*
 * Copyright (c) 2002 by SAP AG.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP AG.
 * Created : July 2001
 */
package com.sap.engine.services.webservices.wsdl;

import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.lib.xml.util.QName;
import org.w3c.dom.Element;

/**
 * Base class for all WSDL tree elements.
 * @author Chavdar Baykov, chavdar.baikov@sap.com
 * @version 2.0
 */
public class WSDLNode implements java.io.Serializable {

  /**
   * Holds parent node of this WSDLNode.
   */
  private WSDLNode parent = null;

  /**
   * Default Constructor.
   */
  public WSDLNode() {
    parent = null;
  }

  /**
   * Constructor with parent node.
   */
  public WSDLNode(WSDLNode parent) {
    this.parent = parent;
  }

  /**
   * Sets the parent of this node.
   */
  public void setParentNode(WSDLNode node) {
    this.parent = node;
  }

  /**
   * Gets the parent of this node.
   */
  public WSDLNode getParentNode() {
    return this.parent;
  }

  /**
   * Returns the parent of all nodes WSDLDocument. Null if unasociated.
   */
  public WSDLNode getDocument() {
    return getParentNode().getDocument();
  }

  public QName getQName(String qname, Element element) throws WSDLException {
    String namespace = DOM.qnameToURI(qname, element);

    if (namespace == null) {
      if (DOM.qnameToPrefix(qname).length() != 0) {
        throw new WSDLException(" Unmapped prefix is used in qname '" + qname + "' !");
      }

      //namespace = ((WSDLDefinitions) getDocument()).getTargetNamespace();
    }

    return new QName(DOM.qnameToLocalName(qname), namespace);
  }

}

