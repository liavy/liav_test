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
 * Title: EndpointWrapper 
 * Description: EndpointWrapper interface should be implemented by classes, that wrap service endpoint instances 
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public interface ServiceEndpointWrapper {
  
  /**
   * @return the wrapped service enpoint instance
   */
  public Object getServiceEndpointInstance(); 

}
