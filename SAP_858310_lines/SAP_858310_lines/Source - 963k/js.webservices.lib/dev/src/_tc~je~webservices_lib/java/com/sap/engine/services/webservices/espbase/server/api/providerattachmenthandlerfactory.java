/* Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.server.api;

import com.sap.engine.services.webservices.espbase.client.api.AttachmentHandler;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.espbase.server.additions.attach.ProviderAttachmentProtocol;
import com.sap.engine.services.webservices.espbase.server.runtime.ApplicationWebServiceContextImpl;

/**
 * Provides method for obtaining implementation of <code>AttachmentHandler</code> interface 
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2006-3-15
 */
public class ProviderAttachmentHandlerFactory {
  
  public final static AttachmentHandler getAttachmentHandler() {
    ProviderContextHelper pCtx = (ProviderContextHelper) ApplicationWebServiceContextImpl.getSingleton().getConfigurationContext();
    if (pCtx == null) {
      throw new IllegalStateException("Provider side AttachmentHandler could be accessed only inside web service call.");
    }
    String bType = pCtx.getStaticContext().getInterfaceMapping().getBindingType();
    //only with SOAP and MIME bindings the attachment api could be used
    if ((! InterfaceMapping.SOAPBINDING.equals(bType)) && (! InterfaceMapping.MIMEBINDING.equals(bType))) {
      throw new IllegalStateException("Provider side AttachmentHandler could be used only with SOAP and MIME bindings.");
    }
    
    String[] ss = pCtx.getStaticContext().getProtocolsOrder();
    for (String string : ss) {
      if (string.equals(ProviderAttachmentProtocol.PROTOCOL_NAME)) {
        return ProviderAttachmentProtocol.SINGLETON; 
      }
    }
    throw new IllegalStateException("Provider side AttachmentHandler could not be obtained, since the responsible protocol is not available in the protocol's list.");
  }
}
