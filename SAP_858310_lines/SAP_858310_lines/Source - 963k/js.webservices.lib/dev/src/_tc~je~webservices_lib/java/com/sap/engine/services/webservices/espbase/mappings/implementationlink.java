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

package com.sap.engine.services.webservices.espbase.mappings;

/**
 * WSDL to Java interface mapping detail type.
 * Schema complexType Java representation.
 * Represents type {http://sapframework.sap.com}ImplementationLink
 */
public class ImplementationLink extends MappingContext {

  public static final String STUB_NAME = "StubName";
  public static final String SI_IMPL_NAME = "SI_Impl_Name";
  public static final String IMPLCONTAINER_ID  = "implementation-id"; //implementation container id property
  public static final String SERVICE_REF_JNDI_NAME = "ServiceRef_JNDI_Name";
  
  /**
   * Returns web services client jndi name.
   * @return
   */
  public String getServiceRefJNDIName() {
    return (String) super.getProperty(SERVICE_REF_JNDI_NAME);
  }
  
  /**
   * Sets web serviice client jndi name.
   * @param jndiName
   */
  public void setServiceRefJNDIName(String jndiName) {
    super.setProperty(SERVICE_REF_JNDI_NAME,jndiName);
  }
      
  /**
   * Sets binding implementation stub name.
   * @param stubName
   */
  public void setStubName(String stubName) {
    super.setProperty(STUB_NAME,stubName); 
  }
  
  /**
   * Returns binding implementation stub name.
   * @return
   */
  public String getStubName() {
    return super.getProperty(STUB_NAME);
  }  

  /**
   * Sets Service interface implementation name for WSDL Service.
   * @param SIImplName
   */
  public void setSIImplName(String SIImplName) {
    super.setProperty(SI_IMPL_NAME,SIImplName); 
  }
  
  /**
   * Returns service interface implementation name for WSDL Service.
   * @return
   */
  public String getSIImplName() {
    return super.getProperty(SI_IMPL_NAME);
  }
  
  /**
   * @return the id of implementation container, which container is supposed to process this ImplementationLink object content.
   */
  public String getImplementationContainerID() {
    return (String) getProperty(IMPLCONTAINER_ID);
  }
  /**
   * Sets <code>IMPLCONTAINER_ID</code> property to the <code>implID</code> value.
   */
  public void setImplementationContainerID(String implID) {
    super.setProperty(IMPLCONTAINER_ID, implID);
  }
}
