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
 * The <code>LineInfo</code> class represents the line information that is part
 * of the Line Section of the SMAP format.
 * 
 * @author Diyan Yordanov
 */
public class LineInfo implements Comparable<LineInfo>{

  /**
   * Start line in the input source. InputStartLine is greater than or equal to one.
   */
  private int inputStartLine = -1;
  
  /**
   * Specifies the source file containing the input source line. Each LineFileID 
   * must be a file ID present in the FileSection. 
   */
  private int lineFileId = -1;
  
  /**
   * Desribes the repeat count of this mapping.
   */
  private int repeatCount = 1;
  
  /**
   * Start line in the output source.
   */
  private int outputStartLine = -1;
  
  /**
   * Specifies the number of lines in the output source range.
   */
  private int outputLineIncrement = 1;
  
  
  /**
   * Creates new <code>LineInfo</code> object.
   * @param inputStartLine the start line in the input source.
   * @param outputStartLine the start line in the output source. 
   */
  public LineInfo(int inputStartLine, int outputStartLine) {
    this.inputStartLine = inputStartLine;
    this.outputStartLine = outputStartLine;
  }
  
  /**
   * Sets a line file ID for this LineInfo.
   * @param lineFileId the id for this LineInfo.
   */
  public void setLineFileId(int lineFileId) {
    this.lineFileId = lineFileId;
  }

  /**
   * Returns the line file ID set for this LineInfo.
   * @return the line file ID set for this LineInfo.
   */
  public int getLineFileId() {
    return lineFileId;
  }
  
  /**
   * Returns a String representation of this LiniInfo.
   * @param numberOfFiles if 1, then no file ID will be included.
   * @param printFileId if true, will include FileLineID.
   * @return a String representation of this LiniInfo.
   */
  public String toString(int numberOfFiles, boolean printFileId) {
    StringBuilder buff = new StringBuilder();
    if (inputStartLine != -1) {
      buff.append(inputStartLine);
    } else {
      throw new IllegalStateException("Wrong input start line in the LineInfo [" + inputStartLine + "]");
    }
    if (lineFileId != -1 && numberOfFiles != 1 && printFileId) {
      buff.append("#");
      buff.append(lineFileId);
    }
    if (repeatCount != 1) {// optimization - if 1, repeat is missed.
      buff.append(",");
      buff.append(repeatCount);
    }
    if (outputStartLine != -1) {
      buff.append(":");
      buff.append(outputStartLine);
    } else {
      throw new IllegalStateException("Wrong output start line in the LineInfo [" + outputStartLine + "]");
    }
    if (outputLineIncrement != 1) {
      buff.append(",");
      buff.append(outputLineIncrement);
    }
    buff.append("\r\n");
    return buff.toString();
  }
  
  /**
   * Returns the full format, including the fileIDs.
   * @return a String representation of this LineInfo.
   */
  public String toString(){
    return toString(0, true);
  }
 
  /**
   * Set value for the repaet count.
   * @param repeatCount value for the repeat count.
   */
  public void setRepeatCount(int repeatCount) {
    this.repeatCount = repeatCount;
  }

  /**
   * Returns the line position in the JSP file.  
   * @return the line position in the JSP file.
   */
  public int getInputStartLine() {
    return inputStartLine;
  }

  /**
   * Sets the number of lines in the output source range.
   * @param outputLineIncrement the value for the line increment. 
   */
  public void setOutputLineIncrement(int outputLineIncrement) {
    this.outputLineIncrement = outputLineIncrement;
  }
  
  public int compareTo(LineInfo arg0) {
    if (arg0 == null || !(arg0 instanceof LineInfo)) {
      return 0;
    }
    LineInfo otherInfo = (LineInfo) arg0;
    if( getLineFileId() != otherInfo.getLineFileId() ) {
      return getLineFileId() - otherInfo.getLineFileId();
    }
    return getInputStartLine() - otherInfo.getInputStartLine();
  }
}
