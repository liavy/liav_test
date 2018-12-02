/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime.soaphttp;

import com.sap.engine.interfaces.webservices.esp.*;


/**
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */

public interface MessageIDProtocol extends ProviderProtocol {

  /**
   * Contant containing the name of the protocol
   */
  public static final String MESSAGEID_PROTOCOLNAME  =  "MessageIDProtocol";

  /**
   * Returns the ID of the last Soap message.
   * @return
   */
  public String getMessageID(ConfigurationContext ctx);
}
