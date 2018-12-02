package com.sap.engine.services.webservices.additions.soaphttp;

import com.sap.engine.interfaces.webservices.runtime.component.TransportBindingFactory;
import com.sap.engine.services.webservices.runtime.wsdl.AbstractSOAPTransportBinding;

/**
 * Title:  
 * Description: 
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class SOAPHTTPFactroy implements TransportBindingFactory {

  public com.sap.engine.interfaces.webservices.runtime.TransportBinding newInstance() {
    return new AbstractSOAPTransportBinding();
  }

}