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
 * The <code>FileInfo</code> class represents the file information for each 
 * source file. FileInfo uses the second form (source name / source path) of 
 * the two possible forms for the line information. This form consists of two 
 * lines: a plus sign “+”, file ID, and source name on the first line and 
 * source path on the second.
 * 
 * @author Diyan Yordanov
 */
public class FileInfo {
	
  /**
   * String representation of the <code>FileInfo</code> object.
   */
  private String fileInfo;
  
  /**
   * Creates new <code>FileInfo</code> object.
   * @param id the unique file ID.
   * @param sourceName the source file name.
   * @param relativePathToSource the source path which is relative to the 
   * compilation source path.
   */
  public FileInfo(int id, String sourceName, String relativePathToSource) {
    StringBuilder buff = new StringBuilder();
    buff.append("+ ");
    buff.append(id);
    buff.append(" ");
    buff.append(sourceName);
    buff.append("\r\n");
    buff.append(relativePathToSource);
    buff.append("\r\n");
    fileInfo = buff.toString();
  }
  
  /**
   * Returns a String representation of this <code>FileInfo</code> object.
   * @return a String representation of this <code>FileInfo</code> object.
   */
  public String toString() {
    return fileInfo;
  }
}
