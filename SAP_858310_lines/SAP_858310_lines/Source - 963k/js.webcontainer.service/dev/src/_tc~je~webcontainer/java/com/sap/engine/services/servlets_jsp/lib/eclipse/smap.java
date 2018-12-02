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

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.server.LogContext;

/**
 * The <code>Smap</code> object represents the Source Map Format used for
 * providing the Debug Support information according to JSR-045.
 *
 * @author Diyan Yordanov
 */
public class Smap {

  /**
   * Name of the class file in which the SMAP will be integrated.
   */
  private String classFileName = null;

  /**
   * The <code>JspLineMapper</code> object use for generating the mapping between
   * the JSP source and the output Java source.
   */
  private JspLineMapper sourceMapper = null;

  /**
   * The Header section in the SMAP format.
   */
  private Header header = null;

  /**
   * The File Section in the SMAP format.
   */
  private FileSection fileSection = null;

  /**
   * The Line Section in the SMAP format.
   */
  private LineSection lineSection = null;

  /**
   * Creates new <code>Smap</code> object. The constructor uses the generated
   * java source code as ByteArrayInputStream. It uses also passed name of the
   * class file and encoding to process the input stream.
   * @param classFileName the name of the class file which will be used for
   * saving the generated class that contains the SMAP.
   * @param byteArrayInputStream the input stream from which the source java file
   * to be read.
   * @param encoding the charset to be used when reading the input stream.
   * @throws Exception if any Exception occurs while processing of the source file.
   */
  public Smap(String classFileName, ByteArrayInputStream byteArrayInputStream, String encoding)
      throws Exception {
    this.classFileName = classFileName;
    header = new Header(classFileName.substring(classFileName.lastIndexOf(ParseUtils.separatorChar) + 1,
        classFileName.length() - "class".length()) + "java");
    fileSection = new FileSection();
    lineSection = new LineSection();
    sourceMapper = new JspLineMapper(classFileName, byteArrayInputStream, encoding, fileSection);
  }

  /**
   * Generates the content of the SMAP.
   * @return a String that represents the SMAP
   * @throws Exception if any exception occurs while generating the SMAP.
   */
  public String generateSmap() throws Exception {
    ArrayList javaToJspMap = sourceMapper.generate();
    String result = null;
    if (javaToJspMap.size() != 0) {
      for (int i = 0; i < javaToJspMap.size(); i++) {
        LineInfo lineInfo = (LineInfo) javaToJspMap.get(i);
        lineSection.addLineInfo(lineInfo);
      }
      result = toString();
      //dumping SMAP file only if JSP parser location is set to debug
      if (LogContext.getLocationJspParser().beDebug()) {
        writeToFile(result);
      }
    }
    if (result == null) {
      result = toString();
    }
    return result;
  }

  /**
   * Writes the SMAP into file. The name of the file is the same as the name of
   * the resultant class file with additional ".smap" extension.
   * @param smap the string content of the SMAP.
   * @throws IOException if any IOException occurs while writing the file.
   */
  private void writeToFile(String smap) throws IOException {
    OutputStream os = new FileOutputStream(classFileName + ".smap");
    try {
      os.write(smap.getBytes());
    } finally {
      os.close();
    }
  }

  /**
   * Returns a String representation of this SMAP.
   * @return a String representation of this SMAP.
   */
  public String toString(){
    StringBuilder buff = new StringBuilder();
    buff.append(header.toString());
    buff.append("*S JSP\r\n");
    buff.append(fileSection.toString());
    buff.append(lineSection.toString(fileSection.getNumberOfFiles()));
    buff.append("*E\r\n");
    return buff.toString();
  }
}