package com.sap.engine.services.webservices.espbase.client.dynamic.types.impl;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;

public final class NodeListSimpleImpl implements NodeList {
  
  //private static DocumentBuilder docBuilder;
  private static Document doc;
  private static synchronized Document getDocument() {
    try {      
      if (doc == null) {
        doc = SharedDocumentBuilders.newDocument();
      }
      return doc;
    } catch (Exception x) {
      return null;
    }
  }  
  
  private Node[] items;
  
  public NodeListSimpleImpl(final NodeList outsiderNodeList) {    
    items = new Node[outsiderNodeList.getLength()];
    Document newDoc = getDocument();
    for (int i=0; i<items.length; i++) {
      synchronized (newDoc) {
        items[i] = newDoc.importNode(outsiderNodeList.item(i),true);        
      }
    }
  }
      
  public int getLength() {
    return items.length;
  }

  public Node item(final int index) {
    try {
      return items[index];
    } catch (ArrayIndexOutOfBoundsException e) {
      return null;
    }    
  }
}
