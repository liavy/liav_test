/**
 * Title:        xml2000
 * Description:  Node for holding documentation nodes
 *
 * Copyright:    Copyright (c) 2001
 * Company:      InQMy
 * @author       Chavdar Baykov, Chavdarb@abv.bg
 * @version      July 2001
 */
package com.sap.engine.services.webservices.wsdl;

import com.sap.engine.lib.xml.parser.helpers.CharArray;
import org.w3c.dom.Element;

public class WSDLDocumentation extends WSDLNode {

  CharArray content;
  Element elementContent;//$JL-SER$

  public WSDLDocumentation() {
    content = new CharArray();
    elementContent = null;
  }

  public WSDLDocumentation(WSDLNode parent) {
    super(parent);
    content = new CharArray();
    elementContent = null;
  }

  public void setContent(CharArray text) {
    content.set(text);
  }

  public CharArray getContent() {
    return content;
  }

  /**
   * Sets element content of documentation node.
   * @param content
   */
  public void setElementContent(Element content) {
    this.elementContent = content;
  }

  /**
   * Gets element content of documentation node.
   * @return
   */
  public Element getElementContent() {
    return this.elementContent;
  }

}

