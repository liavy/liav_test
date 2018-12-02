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
package com.sap.engine.services.webservices.espbase.client.api;

import com.sap.engine.services.webservices.espbase.client.api.impl.AttachmentHandlerNYImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.DynamicStubImpl;
import com.sap.engine.services.webservices.espbase.client.dynamic.impl.DInterfaceInvokerImpl;
import com.sap.engine.services.webservices.espbase.client.jaxws.core.JAXWSProxy;
import com.sap.engine.services.webservices.espbase.wsdas.impl.WSDASImpl;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.BaseGeneratedStub;

public class AttachmentHandlerFactory {
  
  /**
   * Returns AttachmentHandler instance for specific port or InterfaceInvoker.
   * @param port
   * @return
   */
  public static final AttachmentHandler getInterface(Object port) {
    if (port == null) {
      return null;
    }
    if (port instanceof DInterfaceInvokerImpl) {
      return new AttachmentHandlerNYImpl(((DInterfaceInvokerImpl) port)._getConfigurationContext());
    }
    if (port instanceof WSDASImpl) {
        return new AttachmentHandlerNYImpl(((WSDASImpl) port)._getConfigurationContext());
      }
    if (port instanceof DynamicStubImpl) {
      return new AttachmentHandlerNYImpl(((DynamicStubImpl) port)._getConfigurationContext());
    }
    if(port instanceof JAXWSProxy) {
      return new AttachmentHandlerNYImpl(((JAXWSProxy)port)._getConfigurationContext());
    }
    return null;
  }
  

}
