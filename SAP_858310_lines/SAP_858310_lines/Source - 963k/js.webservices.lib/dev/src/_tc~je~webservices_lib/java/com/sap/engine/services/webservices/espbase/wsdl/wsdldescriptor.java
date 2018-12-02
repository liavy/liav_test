/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.wsdl;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

/**
 * Instances of this class contain information about wsdl entities available in 
 * wsdl DOM element object.
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-12-9
 */
public class WSDLDescriptor {
  private List bindings;
  private List interfaces;
  private List services;
  private List imports;
  private String targetNS;
  private Element wsdl;
  private String fileName; //used for serialization
  private String wsdlVersion;
  
  public WSDLDescriptor() {
    bindings = new ArrayList();  
    interfaces = new ArrayList();  
    imports = new ArrayList();  
    services = new ArrayList();
    wsdlVersion = Definitions.WSDL11; //default value
  }
  
  /**
   * Creates instance with specified wsdl DOM element
   * and its targetNamespace 
   * @param wsdl wsdl DOM element.
   * @param ns targetNamespace value of the wsdl element.
   */
  public WSDLDescriptor(Element wsdl, String ns) {
    this();
    this.wsdl = wsdl;
    this.targetNS = ns;
  }
  
  /**
   * @return List of String objects. These String objects are local names of
   * all bindings contained in this wsdl element.
   */
	public List getBindings() {
		return bindings;
	}

  /**
   * @return List of String objects. These String objects are URI values
   * of the namespaces of other wsdl documents imported by this wsdl element.
   */
	public List getImports() {
		return imports;
	}

  /**
   * @return List of String objects. These String objects are local names of
   * all portType/interface entities contained in this wsdl element.
   */
	public List getInterfaces() {
		return interfaces;
	}

  /**
   * @return List of String objects.These String objects are local names of
   * all service entities contained in this wsdl element.
   */
	public List getServices() {
		return services;
	}
  /** 
   * @return targetNamespace value of the wsdl elemente.
   */
	public String getTargetNS() {
		return targetNS;
	}
  /**
   * @return wsdl DOM element
   */
	public Element getWsdl() {
		return wsdl;
	}
  /**
   * Appends binding name to the binding names' list.
   */
	public void addBinding(String name) {
		bindings.add(name);
	}
  /**
   * Appends namespace uri to the imports' list.
   */
	public void addImport(String impt) {
		imports.add(impt);
	}
  /**
   * Appends interface name the interface names' list.
   */
	public void addInterface(String name) {
		interfaces.add(name);
	}
  /**
   * Appends service name the service names' list.
   */
	public void addService(String name) {
		services.add(name);
	}
  /**
   * Sets targetNamespace value.
   */
	public void setTargetNS(String string) {
		targetNS = string;
	}
  /**
   * Sets wsdl DOM element.
   */
	public void setWsdl(Element element) {
		wsdl = element;
	}

	public String toString() {
    String lineSeparator = System.getProperty(Base.LINE_SEPARATOR);
		return "targeNamespace:" + targetNS + lineSeparator
           + "imports:" + imports.toString() + lineSeparator 
           + "interfaces:" + interfaces.toString() + lineSeparator
           + "bindings:" + bindings.toString() + lineSeparator
           + "services:" + services.toString() + lineSeparator
           + wsdl.toString();
           
	}

//  public boolean containsSingleInterface() {
//    if (interfaces.size() == 1 && bindings.size() == 0 && services.size() == 0 && imports.size() == 0) {
//      return true;
//    }
//    return false;
//  }
//  
//  public boolean containsSingleBinding() {
//    if (interfaces.size() == 0 && bindings.size() == 1 && services.size() == 0 && imports.size() == 0) {
//      return true;
//    }
//    return false;
//  }
  
  /**
   * This method is used in the process of serializing wsdl DOM element into file.
   * 
   * @return name of file into which the DOM element object should be serialized.        
   */
	public String getFileName() {
		return fileName;
	}
  /**
   * This method is used in the process of serializing wsdl DOM element into file.
   * Sets the file name.
   * 
   * @param fileName file name
   */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
  /**
   * @return wsdl version of the wsdl DOM element. The returned values are defined as constants
   *         in Definition class. 
   * @see Definitions#WSDL11
   * @see Definitions#WSDL20 
   */
	public String getWsdlVersion() {
		return wsdlVersion;
	}

  /**
   * Sets wsdl version of the wsdl DOM element. The values are defined as constants
   * in #Definition class. 
   * @see Definitions#WSDL11
   * @see Definitions#WSDL20 
   */
	public void setWsdlVersion(String string) {
		wsdlVersion = string;
	}

}
