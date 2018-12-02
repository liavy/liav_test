/**
 * Copyright (c) 2002 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.interfaces.webservices.runtime;

/**
 * 
 * @author Alexander Zubev
 * 
 */
public interface ServletDispatcher {
  public static final String NAME = "WSDispatcher";
  
  public void doPost(Object request, Object response, Object servlet) throws Exception;
  public void doGet(Object request, Object response, Object servlet) throws Exception;
  public void doHead(Object request, Object response, Object servlet) throws Exception;
}
