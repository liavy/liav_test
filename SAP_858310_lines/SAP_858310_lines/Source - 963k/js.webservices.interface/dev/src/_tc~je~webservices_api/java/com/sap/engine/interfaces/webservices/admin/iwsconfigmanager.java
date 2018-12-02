/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.interfaces.webservices.admin;

import com.sap.engine.interfaces.webservices.admin.saml.SAMLConfigManager;
import com.sap.engine.interfaces.webservices.admin.ui.UIMetaDataProvider;

/**
 * Copyright (c) 2004, SAP-AG
 * @author Alexander Zubev
 * @version 1.1, 2005-4-21
 */
public interface IWSConfigManager {
  public static final String INTERFACE_NAME = "tc~esi~esp~api";
  
  public void registerUIMetaDataProvider(UIMetaDataProvider provider);
  public void unregisterUIMetaDataProvider(UIMetaDataProvider provider);
  
  public SAMLConfigManager getSAMLConfigManager();
}
