package com.sap.engine.services.webservices.espbase.client;

public interface PackageResolverInterface {
  
  public static final String JAXWS_BINDINGS_EXTENSION_NS = "http://java.sun.com/xml/ns/jaxws";
  public static final String JAXWS_BINDINGS_EXTENSION_ELEMENT_NAME = "bindings";
  public static final String JAXWS_PACKAGE_EXTENSION_ELEMENT_NAME = "package";
  public static final String JAXWS_PACKAGE_EXTENSION_ATTRIB_NAME = "name";  
  
  public String resolve(String namespace);
  
}
