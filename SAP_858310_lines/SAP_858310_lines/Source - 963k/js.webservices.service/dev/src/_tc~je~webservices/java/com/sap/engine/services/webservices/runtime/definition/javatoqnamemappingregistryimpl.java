package com.sap.engine.services.webservices.runtime.definition;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.util.HashMap;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */
public final class JavaToQNameMappingRegistryImpl implements com.sap.engine.interfaces.webservices.runtime.JavaToQNameMappingRegistry {

  private static final String ROOT_ELEMENT_NAME = "javaToQNameMappings";
  private static final String LITERAL_WRAPPER_NAME = "literalmappings";
  private static final String ENCODED_WRAPPER_NAME = "encodedmappings";

  private JavaToQNameMappingsImpl encodedMappings;
  private JavaToQNameMappingsImpl literalMappings;

  public JavaToQNameMappingRegistryImpl() {
    this.encodedMappings = new JavaToQNameMappingsImpl();
    this.literalMappings = this.encodedMappings;
  }

  public com.sap.engine.interfaces.webservices.runtime.JavaToQNameMappings getEncodedMappings() {
    return encodedMappings;
  }

  public void setEncodedMappings(JavaToQNameMappingsImpl encodedMappings) {
    this.encodedMappings = encodedMappings;
  }

  public void setEncodedMappings(HashMap encodedMappings) {
    this.encodedMappings = new JavaToQNameMappingsImpl(encodedMappings);
  }

  public com.sap.engine.interfaces.webservices.runtime.JavaToQNameMappings getLiteralMappings() {
    return literalMappings;
  }

  public void setLiteralMappings(JavaToQNameMappingsImpl literalMappings) {
    this.literalMappings = literalMappings;
  }

  public void setLiteralMappings(HashMap literalMappings) {
    this.literalMappings = new JavaToQNameMappingsImpl(literalMappings);
  }

  public void saveToFile(java.io.File file) throws Exception {
    org.w3c.dom.Document document = (DocumentBuilderFactory.newInstance()).newDocumentBuilder().newDocument();
    org.w3c.dom.Element rootElement = document.createElement(ROOT_ELEMENT_NAME);
    org.w3c.dom.Element literalMappings = document.createElement(LITERAL_WRAPPER_NAME);
    rootElement.appendChild(this.literalMappings.writeToDOMElement(literalMappings));
    org.w3c.dom.Element encodedMappings = document.createElement(ENCODED_WRAPPER_NAME);
    rootElement.appendChild(this.encodedMappings.writeToDOMElement(encodedMappings));
    document.appendChild(rootElement);
    javax.xml.transform.Transformer transformer = (TransformerFactory.newInstance()).newTransformer();
    java.io.FileOutputStream fileOutputStream = new java.io.FileOutputStream(file);
    transformer.transform(new DOMSource(document), new StreamResult(fileOutputStream));
    fileOutputStream.close();
  }

  public String toString() {
    return "\tLiteralMappings: " + this.literalMappings + "\n"
           + "\tEncodedMappings: " + this.encodedMappings + "\n" ;
  }

  public static final JavaToQNameMappingRegistryImpl loadFromFile(java.io.File file) throws Exception {
    javax.xml.parsers.DocumentBuilder documentBuilder = (DocumentBuilderFactory.newInstance()).newDocumentBuilder();
    java.io.FileInputStream fileInputStream = new java.io.FileInputStream(file);
    org.w3c.dom.Document document = null;
    try {
      document = documentBuilder.parse(fileInputStream);
    } finally {
      fileInputStream.close();
    }
    JavaToQNameMappingRegistryImpl registry = new JavaToQNameMappingRegistryImpl();
    org.w3c.dom.NodeList list = document.getDocumentElement().getElementsByTagName(LITERAL_WRAPPER_NAME);

    if (list.getLength() == 1) {
      registry.literalMappings = new JavaToQNameMappingsImpl();
      registry.literalMappings.loadFromDOMElement((org.w3c.dom.Element) list.item(0));
    }

    list = document.getDocumentElement().getElementsByTagName(ENCODED_WRAPPER_NAME);

    if (list.getLength() == 1) {
      registry.encodedMappings = new JavaToQNameMappingsImpl();
      registry.encodedMappings.loadFromDOMElement((org.w3c.dom.Element) list.item(0));
    }
    return registry;
  }

  //  public static void main(String[] args) throws Exception {
  //    HashMap mapLiteral = new HashMap();
  //    mapLiteral.put("one", new QName("http://one", "one"));
  //    mapLiteral.put("two", new QName("http://two", "two"));
  //
  //    HashMap mapEnc = new HashMap();
  //    mapEnc.put("encOne", new QName("http://encOne", "encOne"));
  //    mapEnc.put("encTwo", new QName("http://encTwo", "encTwo"));
  //
  //    JavaToQNameMappingRegistryImpl registry = new JavaToQNameMappingRegistryImpl();
  //    registry.setEncodedNameMapping(new JavaToQNameMappingsImpl(mapEnc));
  //    registry.setLiteralNameMapping(new JavaToQNameMappingsImpl(mapLiteral));
  //    registry.saveToFile(new java.io.File("d:/temp/javaToQname.xml"));
  //
  //    registry = JavaToQNameMappingRegistryImpl.loadFromFile(new java.io.File("d:/temp/javaToQname.xml"));
  //    System.out.println(registry.getEncodedNameMapping());
  //    System.out.println(registry.getLiteralNameMapping());
  //  }

}

