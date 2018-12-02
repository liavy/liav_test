package com.sap.engine.services.webservices.runtime.definition;

import com.sap.engine.lib.xml.util.NS;

import javax.xml.namespace.QName;
import java.util.HashMap;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */
public class JavaToQNameMappingsImpl implements com.sap.engine.interfaces.webservices.runtime.JavaToQNameMappings {

  private static HashMap standardMappings;

  static {
    standardMappings = new HashMap(22);
    standardMappings.put("boolean", new QName(NS.XS, "boolean"));
    standardMappings.put("byte", new QName(NS.XS, "byte"));
    standardMappings.put("short", new QName(NS.XS, "short"));
    standardMappings.put("int", new QName(NS.XS, "int"));
    standardMappings.put("long", new QName(NS.XS, "long"));
    standardMappings.put("float", new QName(NS.XS, "float"));
    standardMappings.put("double", new QName(NS.XS, "double"));
    standardMappings.put("char", new QName(NS.XS, "string"));
    standardMappings.put("java.lang.String", new QName(NS.XS, "string"));
    standardMappings.put("java.math.BigInteger", new QName(NS.XS, "integer"));
    standardMappings.put("java.math.BigDecimal", new QName(NS.XS, "decimal"));
    standardMappings.put("java.util.Calendar", new QName(NS.XS, "dateTime"));
    standardMappings.put("java.util.Date", new QName(NS.XS, "dateTime"));
    standardMappings.put(java.util.GregorianCalendar.class.getName(), new QName(NS.XS, "dateTime"));
    standardMappings.put(java.sql.Date.class.getName(), new QName(NS.XS, "dateTime"));
    standardMappings.put(java.sql.Time.class.getName(), new QName(NS.XS, "dateTime"));
    standardMappings.put("java.lang.Integer", new QName(NS.XS, "int"));
    standardMappings.put("java.lang.Float", new QName(NS.XS, "float"));
    standardMappings.put("java.lang.Double", new QName(NS.XS, "double"));
    standardMappings.put("java.lang.Boolean", new QName(NS.XS, "boolean"));
    standardMappings.put("java.lang.Short", new QName(NS.XS, "short"));
    standardMappings.put("java.lang.Byte", new QName(NS.XS, "byte"));
    standardMappings.put("java.lang.Long", new QName(NS.XS, "long"));
    standardMappings.put("java.lang.Character", new QName(NS.XS, "string"));
    standardMappings.put("byte[]", new QName(NS.XS, "base64Binary"));
    standardMappings.put("java.lang.Object", new QName(NS.XS, "anyType"));
  }

  private HashMap javaToQNameMappings;
  private HashMap qnameToJavaMappings;

  public JavaToQNameMappingsImpl(HashMap mappings) {
    this.javaToQNameMappings = mappings;
    this.qnameToJavaMappings = this.reverseKeyValues(this.javaToQNameMappings);
  }

  public JavaToQNameMappingsImpl() {
    this.javaToQNameMappings = new java.util.HashMap();
    this.qnameToJavaMappings = new java.util.HashMap();
  }

  public static boolean isBuiltInSchemaType(String javaClassName) {
    return standardMappings.containsKey(javaClassName);
  }

  public QName getMappedQName(String javaClassName) {
    return (QName) ((standardMappings.get(javaClassName) != null) ? standardMappings.get(javaClassName) : javaToQNameMappings.get(javaClassName));
  }

  public String getMappedJavaClass(QName qname) {
    return (String) this.qnameToJavaMappings.get(qname);
  }

  public HashMap getQNameToJavaMappings() {
    return this.qnameToJavaMappings;
  }

  public HashMap getJavaToQNameMappings() {
    return (HashMap) javaToQNameMappings.clone();
  }

  org.w3c.dom.Element writeToDOMElement(org.w3c.dom.Element parentElement) {
    java.util.Set keySet = this.javaToQNameMappings.keySet();
    java.util.Iterator itr = keySet.iterator();
    org.w3c.dom.Element tempElement;
    Object tempObject;
    QName tempValue;

    while (itr.hasNext()) {
      tempObject = itr.next();
      tempValue = (QName) javaToQNameMappings.get(tempObject);
      tempElement = parentElement.getOwnerDocument().createElement("entry");
      tempElement.setAttribute("javaClass", (String) tempObject);
      tempElement.setAttribute("uri", tempValue.getNamespaceURI());
      tempElement.setAttribute("localName", tempValue.getLocalPart());
      parentElement.appendChild(tempElement);
    }

    return parentElement;
  }

  void loadFromDOMElement(org.w3c.dom.Element parentElement) throws Exception {
    org.w3c.dom.Element tempElement;
    String javaClass, uri, localName;
    org.w3c.dom.NodeList childs = parentElement.getElementsByTagName("entry");

    for (int i = 0; i < childs.getLength(); i++) {
      tempElement = (org.w3c.dom.Element) childs.item(i);
      javaClass = tempElement.getAttribute("javaClass");
      uri = tempElement.getAttribute("uri");
      localName = tempElement.getAttribute("localName");

      if ((javaClass.length() > 0) && (uri.length() > 0) && (localName.length() > 0)) {
        this.javaToQNameMappings.put(javaClass, new QName(uri, localName));
      } else {
        throw new Exception("Incorrect element found." + tempElement.toString());
      }
    }

    this.qnameToJavaMappings = reverseKeyValues(this.javaToQNameMappings);
  }

  public String toString() {
    return this.javaToQNameMappings.toString();
  }

  private HashMap reverseKeyValues(HashMap map) {
    HashMap newMap = new HashMap();

    java.util.Iterator itr = map.keySet().iterator();
    String key;
    QName value;

    while (itr.hasNext()) {
      key = (String) itr.next();
      value  = (QName) map.get(key);
      newMap.put(value, key);
    }

    //adding standard mappings
    itr = standardMappings.keySet().iterator();
    while (itr.hasNext()) {
      key = (String) itr.next();
      value  = (QName) map.get(key);
      newMap.put(value, key);
    }

    return newMap;
  }
}

