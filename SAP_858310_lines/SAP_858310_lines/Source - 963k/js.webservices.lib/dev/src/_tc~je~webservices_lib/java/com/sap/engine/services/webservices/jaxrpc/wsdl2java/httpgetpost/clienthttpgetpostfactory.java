package com.sap.engine.services.webservices.jaxrpc.wsdl2java.httpgetpost;

import com.sap.engine.interfaces.webservices.runtime.component.ClientTransportBindingFactory;
import com.sap.engine.interfaces.webservices.client.ClientFeatureProvider;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2000
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */
public class ClientHTTPGetPostFactory implements ClientTransportBindingFactory {
  public ClientFeatureProvider newInstance() {
    return new HttpGetPostBinding();
  }
}
