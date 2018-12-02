package com.sap.engine.services.webservices.additions.soaphttp;

import com.sap.engine.interfaces.webservices.runtime.component.TransportBindingFactory;
import com.sap.engine.services.webservices.runtime.wsdl.AbstractHTTPTransportBinding;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class HTTPFactory implements TransportBindingFactory {

  public com.sap.engine.interfaces.webservices.runtime.TransportBinding newInstance() {
    //return new HTTPTransportBinding();
    return new AbstractHTTPTransportBinding();
  }
}	