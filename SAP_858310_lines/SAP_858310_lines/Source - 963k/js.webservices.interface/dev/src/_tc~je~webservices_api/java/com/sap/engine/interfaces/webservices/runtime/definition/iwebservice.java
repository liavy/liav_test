/*
 * Copyright (c) 2003 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.interfaces.webservices.runtime.definition;

import com.sap.engine.interfaces.webservices.runtime.definition.WSIdentifier;

/**
 * Copyright (c) 2003, SAP-AG
 * @author Alexander Zubev (alexander.zubev@sap.com)
 * @version 1.1, 2004-2-5
 */
public interface IWebService extends IWSComponent {
  public WSIdentifier getWSIdentifier();
  
  public String getServiceName();
  
  public String getWsdDocumentation();
  
  public IWSEndpoint[] getEndpoints();

  public String[] getWsdlSupportedStyles();

  public String getWSLocationName();

  public String getWsDirectory();
//  public FeatureInfo[] getDesigntimeFeatures();
}
