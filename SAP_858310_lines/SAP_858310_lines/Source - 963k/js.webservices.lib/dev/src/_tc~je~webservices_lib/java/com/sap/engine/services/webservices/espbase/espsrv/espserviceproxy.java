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
import com.sap.engine.interfaces.webservices.runtime.HTTPProxy;

/**
 * @author Nikolai Dimitrov
 * @version ENHP1
 */
public interface ESPServiceProxy {

  public ESPWSReverseProxyConfiguration getESPWSReverseProxyConfiguration();

  public ESPWebserviceHttpProxyHelper getESPWebserviceHttpProxyHelper();

  /**
   *
   * @param bindingUri - the uri of this service endpoint
   * @return - service definition id to which this endpoint belongs
   * @throws ESPTechnicalException - in case the endpoint is not created with the esp api (came from the deploy)
   */
  public String getInterfaceId(String bindingUri) throws ESPTechnicalException;

}
