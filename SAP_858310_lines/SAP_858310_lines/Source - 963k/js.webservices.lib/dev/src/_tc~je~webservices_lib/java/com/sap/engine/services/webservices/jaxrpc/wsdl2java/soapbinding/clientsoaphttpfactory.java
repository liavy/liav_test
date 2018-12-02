package com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding;

import com.sap.engine.interfaces.webservices.client.ClientFeatureProvider;
import com.sap.engine.interfaces.webservices.runtime.component.ClientTransportBindingFactory;

/**
 * Title:  
 * Description: 
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class ClientSOAPHTTPFactory implements ClientTransportBindingFactory {

  public ClientFeatureProvider newInstance() {
    return new MimeHttpBinding();
  }

}