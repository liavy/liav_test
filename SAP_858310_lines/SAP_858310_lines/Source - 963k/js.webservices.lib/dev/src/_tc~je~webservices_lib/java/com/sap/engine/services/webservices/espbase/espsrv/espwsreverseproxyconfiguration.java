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

import com.sap.engine.services.webservices.espbase.espsrv.proxy.WSMappingResult;

/**
 * @author Nikolai Dimitrov
 * @version ENHP1
 */
public interface ESPWSReverseProxyConfiguration {
  String getEndpointURL(String requestedHostname,
                        Integer requestedPort,
                        String bindingDisplayName,
                        String rtConfigName,
                        String ifDefName,
                        String style,
                        boolean srPublish)
          throws ESPTechnicalException;

  /**
   * @param bdataScheme       - http or https
   * @param requestedHostname - if called from an http request it will be the
   *                          invoking host, if called for SR publishing it will be null
   * @param requestedPort-    if called from an http request it will be the
   *                          invoking port number, if called for SR publishing it will be null
   * @param srPublish         - if the call is intended for SR
   * @return the url in format http(s)://hostname:portnumber
   *         boolean flag if mapping was done
   * @throws TechnicalException - if something fails
   */
  public WSMappingResult getTransportMapping(String bdataScheme,
                                             String requestedHostname,
                                             Integer requestedPort,
                                             boolean srPublish) throws ESPTechnicalException;

  /**
   * @param bdataUri        - the original uri without any mappings
   * @param wsdl            - if this uri is part of wsdl or endpoint url
   * @param transportMapped - if this is true, then we should make uri mapping
   * @return the uri in format /path?paramters
   * @throws TechnicalException - if something fails
   */
  public String getURIMapping(String bdataUri, boolean wsdl, boolean transportMapped) throws ESPTechnicalException;
}
