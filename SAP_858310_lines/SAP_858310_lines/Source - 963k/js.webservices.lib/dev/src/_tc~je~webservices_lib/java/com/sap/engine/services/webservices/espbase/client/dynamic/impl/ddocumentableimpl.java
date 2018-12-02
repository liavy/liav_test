package com.sap.engine.services.webservices.espbase.client.dynamic.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sap.engine.services.webservices.espbase.client.dynamic.util.Util;
import com.sap.engine.services.webservices.espbase.wsdl.Base;
import com.sap.engine.services.webservices.espbase.wsdl.ExtensionElement;
import com.sap.engine.services.webservices.espbase.wsdl.ObjectList;
import com.sap.engine.services.webservices.espbase.wsdl.wsdl11.WSDL11Constants;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;

public class DDocumentableImpl {

  protected Element documentationElem;
  
  protected void initDocumentationElement(Base base) throws ParserConfigurationException {
    ObjectList extensions = base.getChildren();
    for(int i = 0; i < extensions.getLength(); i++) {
      Base extension = extensions.item(i);
      if(extension instanceof ExtensionElement) {
        Element content = ((ExtensionElement)extension).getContent();
        if(content != null && WSDL11Constants.WSDL_NS.equals(content.getNamespaceURI()) && WSDL11Constants.DOCUMENTATION_ELEMENT.equals(content.getLocalName())) {
          Document doc = SharedDocumentBuilders.newDocument();
          documentationElem = (Element)(doc.importNode(content, true));
          return;
        }
      }
    }
  }
  
  public Element getDocumentationElement() {
    return(documentationElem);
  }
  
  protected void initToStringBuffer_Documentation(StringBuffer buffer, String offset) {
    String documentationElemRepresent = createDocumentationElementRepresentation();
    if(documentationElemRepresent != null) {
      Util.initToStringBuffer_ObjectValue(buffer, offset, "documentation : ", documentationElemRepresent);
    }
  }
  
  protected String createDocumentationElementRepresentation() {
    if(documentationElem != null) {
      ByteArrayOutputStream xmlByteArrayOutput = new ByteArrayOutputStream(); 
      try {
        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(documentationElem), new StreamResult(xmlByteArrayOutput));
      } catch(Exception exc) {
        return(null);
      } finally {
        try {
          xmlByteArrayOutput.close();
        } catch(IOException ioExc) {
          //$JL-EXC$
          //nothing to do
        }
      }
      return(new String(xmlByteArrayOutput.toByteArray())); //$JL-I18N$
    }
    return(null);
  }
}
