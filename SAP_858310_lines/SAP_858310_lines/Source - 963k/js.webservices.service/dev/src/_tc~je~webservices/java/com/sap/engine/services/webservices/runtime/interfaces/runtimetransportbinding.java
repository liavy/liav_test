package com.sap.engine.services.webservices.runtime.interfaces;

import com.sap.engine.interfaces.webservices.runtime.*;
import com.sap.engine.services.webservices.wsdl.WSDLDefinitions;

import javax.xml.namespace.QName;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */
public interface RuntimeTransportBinding /* extends TransportBinding*/ {

  /**
   * This method returns an array of String values denoting
   * the style supported by the implementation.
   */
  public String[] getSupportedSyles();

  /**
   *
   * @return for each style what portTypes are necessary
   *         as a bitwise OR v PortTypeDescriptor constants.
   */
  public int[] getNecessaryPortTypes();

  /**
   *
   * @return the style or styles which are accepted by
   *         default.
   */
  public String[] getDefaultStyles();


  /**
   * For now it will return object which will be cast to our WSDLBinding
   * but in future it will be replaces with javax.wsdl.Binding.
   * The WSDLFactory interface is ours but in future when JWSDL
   * is implemented it will be replaced with javax.wsdl.factory.WSDLFactory
   */
  public void generateBinding(String style, QName bindingName, Config globalConfig, OperationDefinition[] operations, PortTypeDescriptor portTypeDecriptors[], WSDLDefinitions definitions) throws TransportBindingException;
  
  public Key[] getOperationKeys(OperationDefinition operation);
}

