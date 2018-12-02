/*
 * Copyright (c) 2002 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.dispatcher;

import com.sap.engine.frame.cluster.transport.TransportSupplier;
import com.sap.engine.frame.cluster.transport.TransportFactory;

/**
 * @author Alexander Zubev
 */
public class WSClientsTransportSupplier implements TransportSupplier {

  /**
   * Retreaves the supplier's transport factory.
   *
   * @param   underlineFactory The underline factory.
   * @return     TransportFactory object.
   */
  public TransportFactory getTransportFactory(TransportFactory underlineFactory) {
    return new WSClientsTransportFactory();
  }
}
