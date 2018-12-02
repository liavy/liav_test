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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.sap.engine.lib.util.HashMapObjectInt;

/**
 * The <code>FileSection</code> class represents the file section in the SMAP
 * format which describes the translated-source file names.
 * 
 * @author Diyan Yordanov
 */
public class FileSection {

  /**
   * Collection of file information objects.
   */
  private List fileInfos = null;

  /**
   * The last value of the file ID.
   */
  private int id = 0;

  /**
   * A HashMap object which maps the source path the file ID.
   */
  private HashMapObjectInt fileIndex = new HashMapObjectInt();

  /**
   * The file section marker.
   */
  private static String SECTION_MARKER = "*F";

  /**
   * Creates an empty <code>FileSection</code> object.
   */
  public FileSection() {
    id = 0;
    fileInfos = new LinkedList();
  }

  /**
   * Returns a String representation of this <code>FileSection</code> object.
   * 
   * @return a String representation of this <code>FileSection</code> object.
   */
  public String toString() {
    StringBuilder buff = new StringBuilder();
    buff.append(SECTION_MARKER);
    buff.append("\r\n");
    for (Iterator iterator = fileInfos.iterator(); iterator.hasNext();) {
      FileInfo fileInfo = (FileInfo) iterator.next();
      buff.append(fileInfo.toString());
    }
    return buff.toString();
  }

  /**
   * Adds new file information into the File section. This method automatically
   * maps a file ID to the source name. The mapped file ID is returned.
   * @param sourceName the name of the translated sources.
   * @param relativePathToSource the path of the translated source.
   * @return a unique file ID under which the file information to be used in the
   * Line Section.
   */
  public int addFileInformation(String sourceName, String relativePathToSource) {
    int fileId;
    if (fileIndex.containsKey(relativePathToSource)) {
      fileId = fileIndex.get(relativePathToSource);
    } else {
      FileInfo fileInfo = new FileInfo(id, sourceName, relativePathToSource);
      fileId = id;
      fileInfos.add(fileInfo);
      fileIndex.put(relativePathToSource, fileId);
      id++;
    }
    return fileId;
  }

  /**
   * Returns the number of files in this file section.
   * @return number of files in this file section.
   */
  public int getNumberOfFiles() {
    return id;
  }
}
