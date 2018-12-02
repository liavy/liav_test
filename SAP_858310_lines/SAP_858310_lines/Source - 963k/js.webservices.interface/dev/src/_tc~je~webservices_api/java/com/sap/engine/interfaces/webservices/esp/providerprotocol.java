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
package com.sap.engine.interfaces.webservices.esp;

/**
 *  <p>This interface is the base interface that should be implemented by protocols running on the provider
 * side.</p> 
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-9-13
 */
public interface ProviderProtocol extends Protocol {
  /**
   * Continues further processing.
   */
  public static int BACK_AND_CONTINUE_PROCESSING  =  5;

}
