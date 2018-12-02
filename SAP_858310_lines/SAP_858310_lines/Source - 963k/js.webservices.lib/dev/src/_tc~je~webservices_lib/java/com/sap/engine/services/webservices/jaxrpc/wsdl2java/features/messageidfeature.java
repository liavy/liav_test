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
package com.sap.engine.services.webservices.jaxrpc.wsdl2java.features;

import com.sap.engine.services.webservices.jaxrpc.wsdl2java.AbstractProtocol;
import com.sap.engine.interfaces.webservices.runtime.component.ClientProtocolFactory;

/**
 * Message ID Feature properties.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public interface MessageIdFeature extends AbstractProtocol,ClientProtocolFactory {

  public static final String MESSAGEIDFEATURE = "http://www.sap.com/webas/640/soap/features/messageId/";
  public static final String DEFAULTNAME = "MessageIdProtocol";

  public String getMessageId();

}
