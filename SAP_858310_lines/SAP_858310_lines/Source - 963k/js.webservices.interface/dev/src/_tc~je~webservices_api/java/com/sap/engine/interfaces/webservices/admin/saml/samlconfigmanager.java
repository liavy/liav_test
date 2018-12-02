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
public abstract class SAMLConfigManager {
  public static final int LOCAL_SAML_ATTESTERS = 0;
  public static final int SAML_ATTESTING_ENTITIES = 2;
  public static final int SAML_ISSUERS = 4;
  public static final int DEFAULT_ATTESTER_NAME = 8;
  
  /**
   * Returns all SAML Entities of the specified type 
   * @return an instance of PropertyListType 
   */
  public abstract Object retrieveSAMLEntities(int type) throws Exception;
  
  public Object retrieveLocalSAMLAttesters() throws Exception {
    return retrieveSAMLEntities(LOCAL_SAML_ATTESTERS) ;
  }
    
  public Object retrieveSAMLAttestingEntities() throws Exception {
    return retrieveSAMLEntities(SAML_ATTESTING_ENTITIES);
  }
    
  public Object retrieveSAMLIssuers() throws Exception {
    return retrieveSAMLEntities(SAML_ISSUERS);
  }
  
  public String getDefaultLocalAttesterName() throws Exception {
    return (String) retrieveSAMLEntities(DEFAULT_ATTESTER_NAME);
  }
  
  public abstract void registerSAMLChangeListener(SAMLChangeListener listener);
  public abstract void unregisterSAMLChangeListener(SAMLChangeListener listener);
}
