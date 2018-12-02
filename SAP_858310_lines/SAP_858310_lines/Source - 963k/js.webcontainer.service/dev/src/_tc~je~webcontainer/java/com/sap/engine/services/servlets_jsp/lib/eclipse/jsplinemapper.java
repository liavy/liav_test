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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;

import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;

/**
 * The <code>JspLineMapper</code> class is used by the <code>SMAP</code> object
 * in the process of mapping source line to output source line. It takes the output 
 * Java source, parses the inline comments generated during the JSP page generation,
 * and creates a map between the input source and the generated output source. 
 * 
 * Two kinds of comments are parsed from the Java source files:
 * 1) scripting - tags, scriptlets, expressions
 *    // begin [file="D:\\app\\sap.com\\QQQ\\servlet_jsp\\QQQweb\\root\\kol.jsp";from=(9,0);to=(17,2)]
 * 2) template text
 *    // HTML // begin [file="D:\\app\\sap.com\\QQQ\\servlet_jsp\\QQQweb\\root\\kol.jsp";from=(1,31);to=(2,0)]
 * 
 * @author Diyan Yordanov
 */
public class JspLineMapper {

  /**
   * Start tag of a comment made for template text. 
   */
  private static final String HTML_TAG = "// HTML ";
  
  /**
   * Start tag of a comment made for tags, scriptlets and expressions.
   */
  private static final String BEGIN_TAG = "// begin [file=";
  
  /**
   * End tag for a comment made for scripting or template text.
   */
  public static final String END_OF_COMMENT_TAG = "// end";

  /**
   * A tag that signals start position in the source file. 
   */
  private static final String FROM_TAG = ";from=(";
  
  /**
   * A tag that signals end position in the source file. 
   */
  private static final String TO_TAG = ");to=(";
  
  /**
   * The end tag in the comment line.
   */
  private static final String END_TAG = ")]";
  
  /**
   * Reference to the File Section that will be filled with file information
   * during source map generation.
   */
  private FileSection fileSection = null;
  
  /**
   * Name of the resultant class file.
   */
  private String classFileName;
  
  /**
   * An input stream for reading the translated source file.
   */
  private InputStreamReader inputSource;
  
  /**
   * Creates new <code>JspLineMapper</code> object for generating map between the
   * source positions in the JSP or Tag files and the source positions in the 
   * generated output source.
   *  
   * @param classFileName the name of the generated output class file.
   * @param byteArrayInputStream an input stream from where the output source to
   * be read.
   * @param encoding the charset to be used when reading the input stream.
   * @param fileSection FileSection to be filled while generatting the mapping.
   * @throws Exception if an error occurs while accessing the input stream.
   */
  public JspLineMapper(String classFileName, ByteArrayInputStream byteArrayInputStream, 
      String encoding, FileSection fileSection) throws Exception{
    this.classFileName = classFileName;
    this.fileSection = fileSection;
    inputSource = new InputStreamReader(byteArrayInputStream, encoding);
  }
  
  /**
   * Generates a mapping between the source positions in the JSP or Tag files 
   * and the source positions in the generated output source.
   * @return an ArrayList of <code>LineInfo</code> objects.
   * @throws JspParseException if an error occcurs while parsing the comments in
   * the source file.
   */
  public ArrayList generate() throws JspParseException {
    ArrayList<LineInfo> javaToJspMap = new ArrayList<LineInfo>();
    LineNumberReader javaFile = new LineNumberReader(inputSource);
    String line = null;
    try {
      line = javaFile.readLine();
      
      if (line != null) {
        do {
          line = line.trim();

          if (line.startsWith(BEGIN_TAG) && line.endsWith(END_TAG)) {
            //scriplet/tag
            createLineInfos(BEGIN_TAG, line, javaFile, javaToJspMap);
          } else if (line.startsWith(HTML_TAG) && line.endsWith(END_TAG)) {
            //template text
            createLineInfos(HTML_TAG, line, javaFile, javaToJspMap);
          }

          line = javaFile.readLine();
        } while (line != null);
      }
    } catch (Exception exc) {
      //Add java source line for additional info
      String fileName = classFileName.substring(0, classFileName.length() - "class".length()) + "java"; 
      Object[] params = new Object[]{fileName, javaFile.getLineNumber() + ""};
      throw new JspParseException(JspParseException.CANNOT_GENERATE_ECLIPSE_DEBUG, params, exc);
    } finally {
      if (javaFile != null) {
        try {
          javaFile.close();
        } catch (Exception exc) {
          throw new JspParseException(JspParseException.CANNOT_CLOSE_FILE, exc);
        }
      } 
    }
    return javaToJspMap;
  }

  /**
   * Creates the respective <code>LineInfo<code>s objects for the detected inline
   * comment.
   * @param startTag specifies the start tag of the comment - BEGIN_TAG or HTML_TAG.
   * @param line the line in the output source file in which the comment is detected.
   * @param javaFile a LineNumberReader object that represents the output source file.
   * @param javaToJspMap an ArrayList where the generated LineInfo objects to be stored. 
   * @throws IOException if an IOException occurse while reading the source file.
   */
  private void createLineInfos(String startTag, String line, LineNumberReader javaFile, 
      ArrayList<LineInfo> javaToJspMap) throws IOException {
    int fromTagIndex = line.indexOf(FROM_TAG);
    int toTagIndex = line.indexOf(TO_TAG);
    if (fromTagIndex == -1 || toTagIndex == -1) {
      throw new IllegalStateException("Cannot generate SMAP table. Wrong info on line [" + 
          javaFile.getLineNumber() + "] of the java source file.");
    }
    String jspFileName = line.substring(line.indexOf(BEGIN_TAG) + BEGIN_TAG.length(), fromTagIndex);
    
    String canonicalFileName = ParseUtils.canonicalize(jspFileName); 
    // there is a bug in debug commment that for file sperators always are used backslash '\'
    // on UNIX File.separator is '/' and they are never replaced with ParseUtils.separatorChar 
    canonicalFileName = canonicalFileName.replace('\\', ParseUtils.separatorChar);
    jspFileName = canonicalFileName.substring(canonicalFileName.lastIndexOf(ParseUtils.separatorChar) + 1, 
        canonicalFileName.length() - 1);
    // From "D:/app/sap.com/QQQ/servlet_jsp/QQQweb/root/jsp/kol.jsp"  leaves only "jsp/kol.jsp" 
    String relativePath = canonicalFileName.substring(canonicalFileName.indexOf("root") + 
        ("root"+ParseUtils.separatorChar).length(), canonicalFileName.length() -1);

    int lineFileId = fileSection.addFileInformation(jspFileName, relativePath);
    
    int k = line.indexOf(',', fromTagIndex);
    String lineNumber = line.substring(fromTagIndex + FROM_TAG.length(), k);
    
    if (startTag.equals(BEGIN_TAG)) {
      int inputStartLine = Integer.parseInt(lineNumber) ;
      k = line.indexOf(',', toTagIndex);
      lineNumber = line.substring(toTagIndex + TO_TAG.length(), k);
      int lastSourceLine = Integer.parseInt(lineNumber);
      int endTagIndex = line.indexOf(END_TAG);
      int columnTo = Integer.parseInt(line.substring(line.indexOf(',', k)+1, endTagIndex)); 

      int outputStartLine = javaFile.getLineNumber() + 1;
      int repeatCount = lastSourceLine - inputStartLine;
      if(repeatCount == 0 || columnTo != 0){
        repeatCount++;
      }
      int outputLineIncrement = 0;
      do {
        line = javaFile.readLine();
        if (line == null) {
          throw new IllegalStateException("Unexpected end of java source file reached.");
        }
        line = line.trim();

        if (line.startsWith(END_OF_COMMENT_TAG)) {
          LineInfo lineInfo = new LineInfo(inputStartLine, outputStartLine);
          lineInfo.setRepeatCount(repeatCount);
          //  JSP | JAVA
          // -----|-----
          //1) n  |  n   (scriplet n=n)
          // -----|-----
          //2) 1  |  m   (TAG)
          // -----|-----
          //3) m  |  1   (HTML)
          //if it is <x:out/> case
          if( repeatCount == 1){
            lineInfo.setOutputLineIncrement(outputLineIncrement);
          }
          lineInfo.setLineFileId(lineFileId);
          javaToJspMap.add(lineInfo);
          break;
        }     
        outputLineIncrement++;
      } while (true);
    } else if (startTag.equals(HTML_TAG)) {
      int firstSourceLine = Integer.parseInt(lineNumber) ;
      k = line.indexOf(',', toTagIndex);
      lineNumber = line.substring(toTagIndex + TO_TAG.length(), k);
      int lastSourceLine = Integer.parseInt(lineNumber) ;

      line = javaFile.readLine();
      if (line == null) {
        throw new IllegalStateException("Unexpected end of java source file reached.");
      }
      line = line.trim();

      LineInfo lineInfo = new LineInfo(firstSourceLine, javaFile.getLineNumber());
      lineInfo.setLineFileId(lineFileId);
      lineInfo.setRepeatCount(lastSourceLine - firstSourceLine +1);
      if( lastSourceLine - firstSourceLine > 0) {
        lineInfo.setOutputLineIncrement(0);
      }
      javaToJspMap.add(lineInfo);
          
      javaFile.readLine(); //read next lilne - END_OF_COMMENT_TAG
    } 
  }
  
}
