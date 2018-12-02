/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
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
 * This interface defines constants meaningful for EJB implementation container.
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-2-24
 */
public interface EJBImplementationLink {
  /**
   * Constant denoting EJB imlementation container id. 
   */
  public static final String IMPLEMENTATION_CONTAINER_ID  =  "ejb-impllink";
  /**
   * Property denoting bean name.
   */
  public static final String EJB_NAME = "ejb-name";
  /**
   * Property denoting jar name.
   */
  public static final String JAR_NAME = "jar-name";
  /**
   * Property denoting session bean type.
   */
  public static final String EJB_SESSION_TYPE = "session-type";
  /**
   * Constant denoting stateful bean type. 
   */  
  public static final String SESSION_STATEFUL = "stateful";
  /**
   * Constant denoting stateless bean type. 
   */  
  public static final String SESSION_STATELESS = "stateless";
  /**
   * Property denoting bean jndi name. Used for backwards compatibility.
   */
  public static final String EJB_JNDI_NAME = "jndi-name";
  /**
   * Property denoting which bean interface is the actual SEI (remote, local, service-endpoint-interface).
   */
  public static final String EJB_INTERFACE_TYPE = "interface-type";
  /**
   * Constant denoting 'local' SEI.
   */
  public static final String EJB_INTERFACE_LOCAL = "local";
  /**
   * Constant denoting 'remote' SEI.
   */
  public static final String EJB_INTERFACE_REMOTE = "remote";   
  /**
   * Constant denoting 'web service' SEI.
   */
  public static final String EJB_INTERFACE_SEI = "sei";
  /**
   * Property name denoting the version of the JEE(J2EE) - 1.4, 5.0, ... 
   */
  public static final String JEE_VERSION = "jee-version";
  /**
   * Constant denoting J2EE1.4 version. 
   */
  public static final String J2EE14 = "1.4";
  /**
   * Constant denoting J2EE1.5 version. 
   */
  public static final String JEE5 = "5";
  //public static final String EJB_JNDI_NAME_LOCAL = "ejb-jndi-name-local";
  //public static final String EJB_ID = "ejb-impllink";
  
}
