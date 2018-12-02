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
 * This class is container for wsdl operations with extracted java mapping infomration.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class OperationInterface {

  // The WSDL PortType that this operation is from.
  public QName portTypeName;
  // The operation name as said in wsdl.
  public String operationName;
  // The mapped operation java name.
  public String operationJavaName;
  // The request namespace - used in soap.
  public String operationRequestName;
  public String operationRequestNamespace;
  // The response namespace - used in soap.
  public String operationResponseNamespace;
  public String operationResponseName;
  // The operation  input params.
  public ServiceParam[] inputParams;
  // The operation output params.
  public ServiceParam[] outputParams;
  // The operation fault params.
  public ServiceParam[] faultParams;
  // The operation has element links.
  public boolean isDocumentStyle = false;

  //The operation soapAction if any
  public String  soapAction;


}
