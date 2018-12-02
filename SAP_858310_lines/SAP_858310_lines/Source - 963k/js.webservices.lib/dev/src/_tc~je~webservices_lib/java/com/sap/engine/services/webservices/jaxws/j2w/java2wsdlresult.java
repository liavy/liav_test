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
package com.sap.engine.services.webservices.jaxws.j2w;

import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;

import com.sap.engine.services.webservices.espbase.mappings.EndpointMapping;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;

/**
 * @author Dimitar Velichkov (I033362) dimitar.velichkov@sap.com
 * This only wraps 
 */
public class Java2WsdlResult {

  private DOMSource[] schemas = null;
  private InterfaceMapping intefaceMapping = null;
  private ServiceMapping sMap = null;
  private SOAPBinding.Style bindingStyle = SOAPBinding.Style.DOCUMENT;
  private Class<?>[] usedClasses;
  
  public Java2WsdlResult(InterfaceMapping iMap, DOMSource[] schemas, ServiceMapping sMap, SOAPBinding.Style bindingStyle){
    
    if(iMap == null || sMap == null){
      throw new IllegalArgumentException();
    }
    
    intefaceMapping = iMap;
    this.sMap = sMap;       
    
    this.schemas = schemas;
    if(schemas == null){
      this.schemas = new DOMSource[0];
    }
    
    if(bindingStyle != null){
      this.bindingStyle = bindingStyle; 
    }
  }
  
  /**
   * @return Returns the intefaceMapping.
   */
  public InterfaceMapping getIntefaceMapping() {
    return intefaceMapping;
  }
  
  /**
   * @return Returns the schemas.
   */
  public DOMSource[] getSchemas() {
    return schemas;
  }
 
  /**
   * Creates a Service Mapping from the passed in service and port qname
   * @return
   */
  public ServiceMapping getServiceMapping(){
    return sMap;
  }

  /**
   * @return Returns the bindingStyle.
   */
  public SOAPBinding.Style getBindingStyle() {
    return bindingStyle;
  }

  /**
   * @return Returns the usedClasses.
   */
  public Class<?>[] getUsedClasses() {
    return usedClasses;
  }

  /**
   * @param usedClasses The usedClasses to set.
   */
  public void setUsedClasses(Class<?>[] usedClasses) {
    
    if(usedClasses == null){
      throw new IllegalArgumentException();
    }
    this.usedClasses = usedClasses;
  }
  
  

}
