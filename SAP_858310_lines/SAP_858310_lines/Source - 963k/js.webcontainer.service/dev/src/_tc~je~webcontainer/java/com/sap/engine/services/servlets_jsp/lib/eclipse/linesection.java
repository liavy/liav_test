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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * The <code>LineSection</code> class represents the line section in the SMAP 
 * format. The line information is kept in a list of <code>LineInfo</code>s objects.
 * 
 * @author Diyan Yordanov
 */
public class LineSection {

  /**
   * A list of <code>LineInfo</code> objects.
   */
  private List<LineInfo> lineInfos;
  
  /**
   * Creates new <code>LineSection</code> object.
   */
  public LineSection() {
    lineInfos = new ArrayList<LineInfo>();
  }

  /**
   * Adds a new line information in this LineSection.
   * @param lineInfo a LineInfo object to be add to this LineSection.
   */
  public void addLineInfo(LineInfo lineInfo) {
    lineInfos.add(lineInfo);
  }
  
  /**
   * Returns a String representation of all lines included in this section.
   * @param numberOfFiles if the numberOfFiles is 1, then no file ID will be 
   * included in the line information.
   * @return a String representation of all lines included in this section.
   */
  public String toString(int numberOfFiles) {
    StringBuilder buff = new StringBuilder();
    buff.append("*L\r\n");
    int lastFileId = -1;
    boolean printFileId = true;
    LineInfo[] lineInfoArr = lineInfos.toArray(new LineInfo[lineInfos.size()]);
    Arrays.sort(lineInfoArr);
    for (int i = 0; i < lineInfoArr.length ; i++ ) {
      LineInfo lineInfo = lineInfoArr[i];
      int fileId = lineInfo.getLineFileId();
      if (fileId == lastFileId) {
        printFileId = false;
      } else {
        printFileId = true;
      }
      lastFileId = fileId;
      buff.append(lineInfo.toString(numberOfFiles, printFileId));
    }
    return buff.toString();
  }
  
  /**
   * Returns a String representation of all lines included in this section.
   * @return a String representation of all lines included in this section.
   */
  public String toString(){
    return toString(0);
  }
  
}
