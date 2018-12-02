/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.jaxrpc.wsdl2java;

import com.sap.engine.services.webservices.jaxrpc.util.NameConvertor;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Hashtable;

/**
 * This is class that is used by Proxy generator as a manager of portTypes.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class QNameManager {

  private HashSet usedPortTypes;
  private Hashtable qnameToJavaMapping;
  private Hashtable qnameToJavaQMapping;
  private NameConvertor convertor;

  public QNameManager() {
    convertor = new NameConvertor();
    usedPortTypes = new HashSet();
    qnameToJavaMapping = new Hashtable();
    qnameToJavaQMapping = new Hashtable();
  }

  public void clear() {
    usedPortTypes.clear();
    qnameToJavaMapping.clear();
  }

  public boolean isQNameUsed(QName qname) {
    if (usedPortTypes.contains(qname)) {
      return true;
    }
    return false;
  }

  public void useQName(QName qname) {
    usedPortTypes.add(qname);
  }

  /**
   * Creates new java name for this QName.
   * @param qname
   * @param packageName
   */
  public String getNewJavaName(QName qname, String packageName) {
    if (qnameToJavaMapping.containsKey(qname)) {
      return (String) qnameToJavaMapping.get(qname);
    }
    String interfaceName = convertor.attributeToClassName(qname.getLocalPart());
    String javaQName = interfaceName;
    if (packageName != null && packageName.length() != 0) {
      javaQName = packageName+"."+javaQName;
    }
    // Name collision resolving using packages
    while (qnameToJavaQMapping.containsValue(javaQName)) {
      interfaceName = interfaceName+"X";
      javaQName = javaQName+"X";
    }
    qnameToJavaMapping.put(qname, interfaceName);
    qnameToJavaQMapping.put(qname, javaQName);
    return interfaceName;
  }

  /**
   * Creates new java name for this QName.
   * @param qname
   * @param packageName
   */
  public String getNewJavaQName(QName qname, String packageName) {
    if (qnameToJavaMapping.containsKey(qname)) {
      return (String) qnameToJavaMapping.get(qname);
    }
    String interfaceName = convertor.attributeToClassName(qname.getLocalPart());
    String javaQName = interfaceName;
    if (packageName != null && packageName.length() != 0) {
      javaQName = packageName+"."+javaQName;
    }
    // Name collision resolving using packages
    while (qnameToJavaQMapping.containsValue(javaQName)) {
      interfaceName = interfaceName+"X";
      javaQName = javaQName+"X";
    }
    qnameToJavaMapping.put(qname, interfaceName);
    qnameToJavaQMapping.put(qname, javaQName);
    return javaQName;
  }

  /**
   * Returns java name for already used PortType.
   * @param qname
   * @return
   */
  public String getJavaName(QName qname) {
    return (String) qnameToJavaMapping.get(qname);
  }

  /**
   * Returns fully qualified java name out of portType name.
   * @param qname
   * @return
   */
  public String getJavaQName(QName qname) {
    return (String) qnameToJavaQMapping.get(qname);
  }

  /**
   * Returns true if this manager already contains this java name.
   * @param javaName
   * @return
   */
  public boolean containsJavaName(String javaName) {
    return qnameToJavaMapping.containsValue(javaName);
  }

  public String resolveJavaName(String javaName, String resolveString) {
    while (qnameToJavaMapping.containsValue(javaName)) {
      javaName = javaName+resolveString;
    }
    return javaName;
  }

  public String resolveJavaQName(String javaName, String resolveString) {
    while (qnameToJavaQMapping.containsValue(javaName)) {
      javaName = javaName+resolveString;
    }
    return javaName;
  }
}
