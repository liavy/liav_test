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
package com.sap.engine.interfaces.webservices.admin.saml;

/**
 * Copyright (c) 2004, SAP-AG
 * @author Alexander Zubev
 * @version 1.1, 2005-5-3
 */
public interface SAMLChangeListener {
  
  /**
   * Invoked when the Property List representing the SAML Entities of the appropriate type have been changed
   * @param type The type of the SAML Entities as defined in SAMLConfigManager
   */
  public void onSAMLEntitiesChanged(int type);
}
