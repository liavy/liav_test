/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.lib.eclipse;

/**
 * The <code>Header</code> class represents the Header section of the SMAP file. 
 * 
 * @author Diyan Yordanov
 */
public class Header {
	
  /**
   * String representation of the <code>Header</code> object.
   */
  private String header;
  
  /**
   * Creates new <code>Header</code> object.
   * @param fileName name of the generated Java source file. This name is 
   * without path information.
   */
  public Header(String fileName) {
    StringBuilder buff = new StringBuilder();
    buff.append("SMAP\r\n");
    buff.append(fileName);
    buff.append("\r\nJSP\r\n");
    header = buff.toString();
  }
  
  /**
   * Returns a String representation of the Header section.
   * @return a String representation of the Header section.
   */
  public String toString() {
    return header; 
  }
}
