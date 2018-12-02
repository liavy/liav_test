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

import com.sap.engine.interfaces.webservices.runtime.HTTPProxy;

/**
 * @author Nikolai Dimitrov
 * @version ENHP1
 */
public interface ESPWebserviceHttpProxyHelper {


  /**
   * @return a concrete configuration for the http proxy
   *         or an object with no host(empty) in case there is no configured proxy on the engine
   * @throws TechnicalException if there is some problem with the reading of the data:
   *                            - error in reading with the DB
   */
  public HTTPProxy getWebserviceHTTPProxy() throws ESPTechnicalException;

  /**
   * This method will save the global webservice http proxy settings.
   * The operation makes the changes cluster wide
   *
   * @param httpProxy - the concrete host with its additional settings or
   *                  an empty host in which case the http proxy usage will be removed
   * @throws TechnicalException if there is some problem with the storage of the data:
   *                            - error in reading or writing with the DB
   */
  public void saveWebserviceHTTPProxy(HTTPProxy httpProxy) throws ESPTechnicalException;
}
