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

import javax.xml.namespace.QName;

/**
 * This class serves as Abstract Service Param carrier. 
 * For request params,response params and fault. It's used by TransportBinding and Stub.  
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class ServiceParam {
  
  /** Part schema name (type or part attribute). */
  public QName schemaName;
  /** Part name */
  public String name;
  /** true if part points to schema element */
  public boolean isElement; 
  /** Represents part content */
  public Object content; 
  /** Represents part java representation */
  public Class contentClass;
  /** Part Namespace not used by runtime */
  public String namespace;
  /** Param class name */
  public String contentClassName;
  /** Original part name */
  public String wsdlPartName;

}
