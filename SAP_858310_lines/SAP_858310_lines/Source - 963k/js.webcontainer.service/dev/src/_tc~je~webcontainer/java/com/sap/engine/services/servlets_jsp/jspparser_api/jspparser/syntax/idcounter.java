/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax;

/**
 * Generates incremented values
 *
 * @author Bojidar Kadrev
 */
public class IDCounter {
  private int id;
  public IDCounter() {
    id = 0;
  }

  public int getId() {
    id++;
    return id;
  }
  
  /**
   * Thradesafe implementation.
   * @return
   */
  public int getIdSync() {
    synchronized(this) {
      id++;
      return id;
    }          
  }
}

