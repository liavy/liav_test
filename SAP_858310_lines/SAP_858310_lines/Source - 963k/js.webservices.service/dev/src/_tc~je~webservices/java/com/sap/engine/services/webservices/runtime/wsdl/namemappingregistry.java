package com.sap.engine.services.webservices.runtime.wsdl;

import com.sap.engine.services.webservices.jaxrpc.java2schema.StandardTypes;

import java.util.Hashtable;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class NameMappingRegistry extends Hashtable {

  public NameMappingRegistry() {
  }

  public void addEntry(String javaClassName, String complexTypeName) {
    super.put(javaClassName, complexTypeName);
  }

  public void addEntry(Class javaClass, String complexTypeName) {
    addEntry(javaClass.getName(), complexTypeName);
  }

  public String getJavaClassMappingName(String className) {
    String result = (String) super.get(className);
    if (StandardTypes.isStandardType(className)) return className;
    return (result != null) ? result:className;
  }

  public String getJavaClassMappingName(Class javaClass) {
    return getJavaClassMappingName(javaClass.getName());
  }

}