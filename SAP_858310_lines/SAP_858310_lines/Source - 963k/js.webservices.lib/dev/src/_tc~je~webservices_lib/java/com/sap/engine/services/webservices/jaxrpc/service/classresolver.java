package com.sap.engine.services.webservices.jaxrpc.service;

import javax.xml.rpc.JAXRPCException;
import java.util.Hashtable;

/**
 * Created by IntelliJ IDEA.
 * User: Ivan-M
 * Date: 2004-8-2
 * Time: 19:07:02
 * To change this template use Options | File Templates.
 */
public class ClassResolver {

  private static final Hashtable PRIMITIVE_TYPE_WRAPPERS_COLLECTOR = new Hashtable();

  static {
    PRIMITIVE_TYPE_WRAPPERS_COLLECTOR.put("boolean", "java.lang.Boolean");
    PRIMITIVE_TYPE_WRAPPERS_COLLECTOR.put("byte", "java.lang.Byte");
    PRIMITIVE_TYPE_WRAPPERS_COLLECTOR.put("short", "java.lang.Short");
    PRIMITIVE_TYPE_WRAPPERS_COLLECTOR.put("int", "java.lang.Integer");
    PRIMITIVE_TYPE_WRAPPERS_COLLECTOR.put("long", "java.lang.Long");
    PRIMITIVE_TYPE_WRAPPERS_COLLECTOR.put("float", "java.lang.Float");
    PRIMITIVE_TYPE_WRAPPERS_COLLECTOR.put("double", "java.lang.Double");
  }

  protected static Class resolve(String className, ClassLoader classLoader) {
    Class requiredClass = null;
    try {
      requiredClass = classLoader.loadClass(className);
    } catch(ClassNotFoundException classNFExc) {
      requiredClass = resolveWrapperClass(className, classLoader);
    }
    return(requiredClass);
  }

  protected static Class resolveWrapperClass(String primitiveTypeName, ClassLoader classLoader) {
    String wrapperClassName = (String)(PRIMITIVE_TYPE_WRAPPERS_COLLECTOR.get(primitiveTypeName));
    Class requiredClass = null;
    if(wrapperClassName == null) {
      throw new JAXRPCException("Class '" + primitiveTypeName + "' can not be instanciated.");
    }
    try {
      requiredClass = classLoader.loadClass(wrapperClassName);
    } catch(ClassNotFoundException exc) {
      requiredClass = null;
    }
    return(requiredClass);
  }
}
