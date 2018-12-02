/**
 * Title:        xml2000
 * Description:  Holds message part
 *
 * Copyright:    Copyright (c) 2001
 * Company:      InQMy
 * @author       Chavdar Baykov, Chavdarb@yahoo.com
 * @version      July 2001
 */
package com.sap.engine.services.webservices.wsdl;

import java.util.ArrayList;

public class WSDLMessage extends WSDLNamedNode {//$JL-EQUALS$ 

  private ArrayList parts;

  public WSDLMessage() {
    super();
    parts = new ArrayList();
  }

  public WSDLMessage(WSDLNode parent) {
    super(parent);
    parts = new ArrayList();
  }

  public void addPart(WSDLPart part) throws WSDLException {
    if (getPart(part.getName()) != null) {
      throw new WSDLException(" Dublicating WSDL 'part' nodes are not allowed in one message! Part name: '" + part.getName() + "'");
    }

    parts.add(part);
  }

  public ArrayList getParts() {
    return parts;
  }

  public WSDLPart getPart(int index) {
    return (WSDLPart) parts.get(index);
  }

  public int getPartCount() {
    return parts.size();
  }

  public WSDLPart getPart(String name) {
    for (int i = 0; i < parts.size(); i++) {
      WSDLPart part = (WSDLPart) parts.get(i);

      if (part.getName().equals(name)) {
        return part;
      }
    } 

    return null;
  }
  
  
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof WSDLMessage)) {
      return false;
    }
    WSDLMessage testedMessage = (WSDLMessage) obj;
    if (this.getName() != null && !this.getName().equals(testedMessage.getName())) {
      return false;
    }
    if (this.getName() == null && testedMessage.getName() != null) {
        return false;
    }
    if (this.getPartCount() != testedMessage.getPartCount()) {
      return false;
    }
    ArrayList testedMessagePartList = testedMessage.getParts();
    int listSize = testedMessagePartList.size();
    Object partObject;
    boolean found;

    for (int i = 0; i < listSize; i++) {
      partObject = this.parts.get(i);
      found = false;

      for (int j = 0; j < listSize; j++) {
        if (partObject.equals(testedMessagePartList.get(j))) {
          found = true;
          break;
        }
      } 

      if (!found) {
        return false;
      }
    } 

    return true;
  }

  public int hashCode() {
    return super.hashCode();
  }
}

