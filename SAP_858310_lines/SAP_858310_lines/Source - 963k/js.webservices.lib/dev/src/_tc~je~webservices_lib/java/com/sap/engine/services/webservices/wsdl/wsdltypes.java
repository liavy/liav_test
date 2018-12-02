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

import org.w3c.dom.Element;

/**
 * Element holding wsdl types. Not very useful as schema keeps type information.
 * @author Chavdar Baykov, chavdar.baikov@sap.com
 * @version 2.0
 */
public class WSDLTypes extends WSDLNode {

  private Element content = null;//$JL-SER$

  public WSDLTypes() {
    super();
  }

  public WSDLTypes(WSDLNode parent) {
    super(parent);
  }

  public void setSchema(Element schema) {
    this.content = schema;
  }

  public Element getSchema() {
    return content;
  }

}

