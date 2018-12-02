/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.espsrv;

import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * @author Nikolai Dimitrov
 * @version ENHP1
 */
public class ESPServiceFactory {

  private static final Location LOCATION = Location.getLocation(ESPServiceFactory.class);

  private ESPServiceFactory() {
  }

  public static ESPServiceProxy getEspService() throws ESPTechnicalException {
    Hashtable<String, String> env = new Hashtable<String, String>();
    env.put("domain", "true");
    String serviceId = "tc~esi~esp~srv";

    try {
      InitialContext ic = new InitialContext(env);
      ESPServiceProxy result = (ESPServiceProxy) ic.lookup(serviceId);
      if (result == null) {
        throw new ESPTechnicalException(serviceId + " is not started");
      }

      return result;
    } catch (NamingException e) {
      LOCATION.traceThrowableT(Severity.DEBUG, e.getMessage(), e);
      throw new ESPTechnicalException(e);
    }
  }
}
