/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.client;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.dom.DOMSource;

/**
 * For JAX-WS web services require the support of external binding customization files to be applied.
 * For this this Class serves as container for parsed into dom wsdl,xsd,and jaxb and jaxws customization files.
 * 
 * @version 1.0 (2006-5-26)
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public class JAXWSFileContainer {
  
  private ArrayList<DOMResource> wsdlList;
  private ArrayList<DOMResource> xsdList;
  private ArrayList<DOMResource> jaxwsCustomList;
  private ArrayList<DOMResource> jaxbCustomList;
  
  public JAXWSFileContainer() {
    this.wsdlList = new ArrayList();
    this.xsdList = new ArrayList();
    this.jaxwsCustomList = new ArrayList();
    this.jaxbCustomList = new ArrayList();    
  }
  
  /**
   * Adds wsdl definition to the source list.
   * @param wsdl
   */
  public void addWSDL(DOMResource wsdl) {
    this.wsdlList.add(wsdl);
  }
  
  /**
   * Adds xsd definition to the source list.
   * @param xsd
   */
  public void addXSD(DOMResource xsd) {
    this.xsdList.add(xsd);
  }
  
  /**
   * Adds jax-ws customizations to the file list.
   * @param jaxwsBinding
   */
  public void addJaxWS(DOMResource jaxwsBinding) {
    this.jaxwsCustomList.add(jaxwsBinding);
  }
  
  /**
   * Adds jaxb customizations to the file list.
   * @param jaxb
   */
  public void addJaxB(DOMResource jaxb) {
    this.jaxbCustomList.add(jaxb);
  }
  
  public void clear() {
    this.wsdlList.clear();
    this.xsdList.clear();
    this.jaxbCustomList.clear();
    this.jaxwsCustomList.clear();
  }
  
  public DOMResource getResourceBySystemId(String systemId) {
    DOMResource wsdlResource = getWSDLBySystemId(systemId);
    if (wsdlResource != null) {
      return wsdlResource;
    }
    DOMResource xsdResource = getXSDBySystemId(systemId);
    if (xsdResource != null) {
      return xsdResource;
    }
    return null;
  }
  
  /**
   * Returns loaded wsdl from system id.
   * @param systemId
   * @return
   */
  public DOMResource getWSDLBySystemId(String systemId) {
    for (DOMResource resource: this.wsdlList) {
      if (systemId.equalsIgnoreCase(resource.getContent().getSystemId())) {
        return resource;
      }
    }
    return null;
  }
  
  /**
   * Returns xsd by system id.
   * @param systemId
   * @return
   */
  public DOMResource getXSDBySystemId(String systemId) {
    if (systemId == null) {
      return null;
    }
    for (DOMResource resource: this.xsdList) {
      if (systemId.equalsIgnoreCase(resource.getContent().getSystemId())) {
        return resource;
      }
    }
    return null;    
  }
  
  /**
   * Returns all jax-ws files.
   * @return
   */
  public List<DOMResource> getJaxWSResources() {
    return this.jaxwsCustomList;
  }
  
  /**
   * Returns all jaxb files.
   * @return
   */
  public List<DOMResource> getJaxBResources() {
    return this.jaxbCustomList;
  }
  
  /**
   * Returns all WSDL files.
   * @return
   */
  public List<DOMResource> getWSDLResources() {
    return this.wsdlList;
  }
  
  /**
   * Returns all XSD files.
   * @return
   */
  public List<DOMResource> getXSDResources() {
    return this.xsdList;
  }
}