package com.sap.sl.util.sduread.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.sap.sl.util.jarsl.api.ConstantsIF;
import com.sap.sl.util.sduread.api.Dependency;
import com.sap.sl.util.sduread.api.IllFormattedSduManifestException;

/**
 * @author d030435
 */

class DependencyParser implements ConstantsIF {
  private static String NODE_TOP = "TOP";
  
  private String xml=null;
  DependencyParser(String xml) {
    this.xml=xml;
  }
  Dependency[] parseXml() throws IllFormattedSduManifestException {
    if (xml==null || xml.trim().equals("")) {
      return null;
    }
    else {
      DocumentBuilder builder;
      try {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        builder = dbf.newDocumentBuilder();
      }
      catch (ParserConfigurationException exc) {
        throw new IllFormattedSduManifestException(exc.getMessage(),exc);
      }
      Document doc=null;
      try {
        doc=builder.parse(new InputSource(new StringReader("<"+NODE_TOP+">"+xml+"</"+NODE_TOP+">")));
      }
      catch (SAXException sex) {
        throw new IllFormattedSduManifestException("The dependency element ("+xml+") is not valid: ("+sex.getMessage()+").",sex);
      }
      catch (IOException ioe) {
        throw new IllFormattedSduManifestException("The dependency element ("+xml+") is not valid: ("+ioe.getMessage()+").",ioe);
      }
      Element elem = doc.getDocumentElement();
      return loopRoot(elem);  
    }
  }
  private Dependency[] loopRoot(Node aNode) throws IllFormattedSduManifestException {
    Vector depobjs=new Vector();
    String name=aNode.getLocalName();
    if (name==null || name.equals("")) {
      name=aNode.getNodeName();
    }
    if (!name.equals(NODE_TOP)) {
      throw new IllFormattedSduManifestException("The dependency element ("+xml+") is not valid.");
    }
    NodeList children = aNode.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node myNode = children.item(i);
      loopDependency(myNode,depobjs);
    }
    return (Dependency[])depobjs.toArray(new Dependency[0]);
  }
  private void loopDependency(Node aNode, Vector depobjs) throws IllFormattedSduManifestException {
    String name=aNode.getLocalName();
    String dcname=null;
    String dcvendor=null;
    String dccounter=null;
    if (name==null || name.equals("")) {
      name=aNode.getNodeName();
    }
    if (!name.equals(TAGDEPENDENCY)) {
      return;
    }
    if (aNode.hasAttributes()) {
      NamedNodeMap myMap = aNode.getAttributes();
      if (myMap!=null) {
        Node myvalue=myMap.getNamedItem(ATTKEYNAME);
        if (myvalue!=null && myvalue.getNodeValue()!=null) {
          dcname=myvalue.getNodeValue();
        }
        myvalue=myMap.getNamedItem(ATTKEYVENDOR);
        if (myvalue!=null && myvalue.getNodeValue()!=null) {
          dcvendor=myvalue.getNodeValue();
        }
        myvalue=myMap.getNamedItem(ATTKEYCOUNTER);
        if (myvalue!=null && myvalue.getNodeValue()!=null) {
          dccounter=myvalue.getNodeValue();
        }
      }
    }
    if (dcname==null || dcvendor==null) {
      throw new IllFormattedSduManifestException("The dependency element ("+xml+") is not valid.");
    }
    else {
      if (dccounter!=null) {
        depobjs.addElement(new VersionedDependencyImpl(dcname,dcvendor,dccounter));
      }
      else {
        depobjs.addElement(new DependencyImpl(dcname,dcvendor));
      }
    }
  }
}
