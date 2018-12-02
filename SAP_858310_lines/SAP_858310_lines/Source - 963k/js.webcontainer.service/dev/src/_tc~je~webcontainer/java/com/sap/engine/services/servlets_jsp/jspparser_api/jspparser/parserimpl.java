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
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser;

import java.io.CharArrayWriter;
import java.io.CharConversionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.sap.engine.lib.util.ConcurrentHashMapObjectObject;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspConfigurationProperties;
import com.sap.engine.services.servlets_jsp.jspparser_api.ParserParameters;
import com.sap.engine.services.servlets_jsp.jspparser_api.WebContainerParameters;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Attribute;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.AttributeDirective;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.DebugInfo;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Element;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.ElementCollection;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Indentifier;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspCommentElement;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspDeclarationScript;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspElement;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspExpression;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspIncludeDirective;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspPageDirective;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspScriptlet;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspTaglibDirective;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Position;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.TagDirective;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.VariableDirective;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.AttributeDirectiveTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.AttributeTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.BodyActionTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.CustomJspTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.DeclarationTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.DoBodyActionTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.ElementActionTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.ExpressionTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.FallbackTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.ForwardTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.GetPropertyTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.IncludeActionTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.InvokeActionTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.JspIncludeDirectiveTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.JspPageDirectiveTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.OutputActionTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.ParamTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.ParamsTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.PluginTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.RootTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.ScriptletTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.SetPropertyTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.TagDirectiveTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.TextTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.UseBeanTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.VariableDirectiveTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.XMLTag;

/*
*
* @author Ivo Simeonov
* @version 4.0
*/
public class ParserImpl extends Parser {
  //private Location currentLocation = Location.getLocation(ParserImpl.class);
  private WebContainerParameters containerParameters;
  /**
   * expert on the page encoding matters. Knows what si the page syntax and page encoding.
   */
  private PageEncodingHelper pageEncodingHelper;
  /*
   * jsp source
   */
  private char[] source;
  /*
   * current line
   */
  private int line;
  /*
   * current position in line
   */
  private int linePos;
  /*
   * current position in source
   */
  private int srcPos;
  /*
   * name of the file for that parser
   */
  private String currentFileName;
  /*
   * element that presents parsed jsp
   */
  private Element parsed;

  /*
   * name of the file for that parser
   */
  private File currentFile;
  private static final String ISO_8859_1 = "ISO-8859-1";
  private List<String> includePrelude = null;
  private List<String> includeCoda = null;
  public boolean isIncluded = false;

  /**
   * Creates new Parser that will parse specified file. Used as standalone app from main[] method in JSPCompiler.
   *
   * @param   file  file to parse
   * @exception   JspParseException  thrown if error occurs
   * during creation of the parser
   */
  public ParserImpl(File file, String encoding) throws JspParseException {
    init(file);
    try {
      if (encoding == null) {
        encoding = ISO_8859_1;
      }
      readFileWithEncoding(file, encoding);
    } catch (CharConversionException miex) { // sun.io.ByteToCharUTF8.convert() BUG
      // try with default encoding
      try {
        readFileWithEncoding(file, ISO_8859_1);
      } catch (IOException ex) {
        throw new JspParseException(JspParseException.IO_ERROR_IN_FILE, new Object[]{file.getName()}, ex);
      }
    } catch (IOException ex) {
      throw new JspParseException(JspParseException.IO_ERROR_IN_FILE, new Object[]{file.getName()}, ex);
    }
  }


	/**
	 * This is the most important constructor. Here a new parser is created the will server the requested JSP page/tag file.
	 * @param file
	 * @param className
	 * @param parserParameters
	 * @throws JspParseException
	 */
  public ParserImpl(File file,String className,
  					ParserParameters parserParameters, WebContainerParameters containerParameters
                    )throws JspParseException{
    init(file);
    this.parserProperties = parserParameters;
    this.containerParameters = containerParameters;
    this.tagLibDescriptors = parserParameters.getTagLibraryDescriptors();
    String filePath = null;
    try {
      filePath = file.getCanonicalPath();
    } catch (IOException ex) {
      throw new JspParseException(JspParseException.CANNOT_GET_CANNONICAL_JSP_PATH, new Object[]{file.getName()}, ex);
    }
    setPath(filePath.substring(0, filePath.lastIndexOf(File.separator)));
    setPageClassname(className);
    if (parserProperties.getJspConfigurationProperties().isDeferredSyntaxAllowedAsLiteral() != null) {
      this.setDeferredSyntaxAllowedAsLiteral(new Boolean(parserProperties.getJspConfigurationProperties().isDeferredSyntaxAllowedAsLiteral()).booleanValue());
    } else {
      this.setDeferredSyntaxAllowedAsLiteral(false); //default value
    }
    if (parserProperties.getJspConfigurationProperties().isTrimDirectiveWhitespaces() != null) {
      this.setTrimDirectiveWhitespaces(new Boolean(parserProperties.getJspConfigurationProperties().isTrimDirectiveWhitespaces()).booleanValue());
    } else {
      this.setTrimDirectiveWhitespaces(false); //default value
    }
    this.pageEncodingHelper = new PageEncodingHelper(file, parserParameters.getJspConfigurationProperties(), this);    
  }


  /**
   * Constructor for parser instances that will parse included files.
   * As the included files use the same environment (JSPConfigProperties,
   * parser parameters), the new parser will derive from the outer(including) one.
   *
   * @param   parser  parser to use as parent
   * @param   file  file to parse
   * @exception   JspParseException  thrown if error occurs
   * during creation of the parser
   */
  public ParserImpl(ParserImpl parser, File file,  ConcurrentHashMapObjectObject tagLibDescriptors) throws JspParseException {
    super(parser);
    currentFile = file;
    try {
      currentFileName = file.getCanonicalPath().replace(File.separatorChar, ParseUtils.separatorChar);
    } catch (IOException ex) {
      throw new JspParseException(JspParseException.IO_ERROR_IN_FILE, new Object[] { file.getName() }, ex);
    }
    //Jsp numbering starts from 1. Debug possition should start from 1 too. 
    this.line = 1;
    this.linePos = 0;
    this.srcPos = 0;
    this.tagLibDescriptors = tagLibDescriptors;
    this.containerParameters = parser.getWebContainerParameters();
     if (parserProperties.getJspConfigurationProperties().isDeferredSyntaxAllowedAsLiteral() != null) {
      this.setDeferredSyntaxAllowedAsLiteral(new Boolean(parserProperties.getJspConfigurationProperties().isDeferredSyntaxAllowedAsLiteral()).booleanValue());
    } else {
      this.setDeferredSyntaxAllowedAsLiteral(false); //default value
    }
    if (parserProperties.getJspConfigurationProperties().isTrimDirectiveWhitespaces() != null) {
      this.setTrimDirectiveWhitespaces(new Boolean(parserProperties.getJspConfigurationProperties().isTrimDirectiveWhitespaces()).booleanValue());
    } else {
      this.setTrimDirectiveWhitespaces(false); //default value
    }
    this.pageEncodingHelper = new PageEncodingHelper(file, parser.getParserParameters().getJspConfigurationProperties(), this);

  }

  /**
   *
   * @return current position in the jsp source
   */
  public int currentPos() {
    return srcPos;
  }

  /**
   *
   *
   * @return current debug position in the jsp source
   */
  public Position currentDebugPos() {
    return new Position(line, linePos);
  }

  /**
   *
   *
   * @return file name of the jsp source
   */
  public String currentFileName() {
    return currentFileName;
  }

  /**
   *
   *
   * @return next char in the jsp source
   */
  public char nextChar() {
    if (srcPos == source.length) {
      return '\0';
    }

    char c = source[srcPos];
    srcPos++;

    if ('\n' == c) {
      line++;
      linePos = 0;
    } else {
      linePos++;
    }

    if (srcPos == source.length) {
      return '\0';
    }

    return source[srcPos];
  }

  /**
   *
   *
   * @return current char in the jsp source
   */
  public char currentChar() {
    if (srcPos == source.length) {
      return '\0';
    }

    return source[srcPos];
  }

  /**
   *
   *
   * @param   pos index in the jsp source
   * @return  char at the specified position
   */
  public char charAt(int pos) {
    return source[pos];
  }

  /**
   * Takes parsing action to the specified token
   *
   * @param   symbols token used as end for parsing
   * @param   skip  denotes whether parser must skip
   * end token or stop parsing on it
   * @return   number of chars that are parsed
   * @exception   JspParseException  thrown if error occurs
   * during parsing
   */
  public int parseTo(char symbols, boolean skip) throws JspParseException {
    int startPosition = srcPos;

    while (symbols != currentChar()) {
      nextChar();

      if (endReached()) {
        throw new JspParseException(JspParseException.EXPECTING_CHAR_WHILE_READ_END_OF_FILE, new Object[]{symbols + ""}
                , currentFileName(), currentDebugPos());
      }
    }

    if (skip) {
      nextChar();
    }

    return srcPos - startPosition;
  }

  /**
   * Takes parsing action to the specified token
   *
   * @param   symbols token used as end for parsing
   * @param   skip  denotes whether parser must skip
   * end token or stop parsing on it
   * @return   number of chars that are parsed
   * @exception   JspParseException  thrown if error occurs
   * during parsing
   */
  public int parseTo(char[] symbols, boolean skip) throws JspParseException {
    try {
      if (symbols == null || symbols.length == 0) {
        throw new JspParseException(JspParseException.WRONG_ARRAY_PARAMETER);
      }

      int startPosition = srcPos;

      do {
        while (symbols[0] != currentChar()) {
          nextChar();

          if (endReached()) {
            throw new JspParseException(JspParseException.EXPECTING_CHAR_WHILE_READ_END_OF_FILE, new Object[]{String.copyValueOf(symbols)},
                    currentFileName(), currentDebugPos());
          }
        }

        if (cmp(srcPos, source, symbols)) {
          break;
        } else {
          nextChar();

          if (endReached()) {
            throw new JspParseException(JspParseException.EXPECTING_CHAR_WHILE_READ_END_OF_FILE, new Object[]{String.copyValueOf(symbols)},
                    currentFileName(), currentDebugPos());
          }

          continue;
        }
      } while (true);

      if (skip) {
        for (int i = 0; i < symbols.length; i++) {
          nextChar();
        }
      }

      return srcPos - startPosition;
    } catch (IndexOutOfBoundsException ioobex) {
      throw new JspParseException(JspParseException.EXPECTING_CHAR_WHILE_READ_END_OF_FILE, new Object[]{String.copyValueOf(symbols)},
              currentFileName(), currentDebugPos());
    }
  }

  /**
   * Skips current all current chars that are white
   * spaces
   *
   * @return   number of chars that are parsed
   */
  public int skipWhiteSpace() {
    int startPosition = srcPos;

    while (srcPos != source.length && "\r\n\t ".indexOf(source[srcPos]) != -1) {
      nextChar();
    }

    return srcPos - startPosition;
  }

  /**
   *
   *
   * @return  true if end of the jsp source is reached
   */
  public boolean endReached() {
    return srcPos == source.length;
  }

  /**
   * Skips current all current chars that are part of java
   * indentifier, including ':' and '-' which are valid in
   * tag and attribute names
   *
   * @return   number of chars that are parsed
   * @exception   JspParseException  thrown if error occurs
   * during parsing
   */
  public int skipIndentifier() throws JspParseException {
    int startPosition = srcPos;

    if (endReached()) {
      throw new JspParseException(JspParseException.EXPECTING_IDENTIFIER_WHILE_READING_END_OF_FILE,
              currentFileName(), currentDebugPos());
    }

    while ((Character.isJavaIdentifierStart(source[srcPos]) || (source[srcPos] == ':') || ((Character.isJavaIdentifierPart(source[srcPos]) || source[srcPos] == '-') && srcPos > startPosition)) && srcPos != source.length - 1) {
      nextChar();

      if (endReached()) {
        throw new JspParseException(JspParseException.EXPECTING_IDENTIFIER_WHILE_READING_END_OF_FILE,
                currentFileName(), currentDebugPos());
      }
    }

    return srcPos - startPosition;
  }

  /**
   * Compares a part of the jsp source with the specified
   * chars
   *
   * @param   start  start position for comparing in the
   * jsp source
   * @param   end  end position for comparing in the
   * jsp source
   * @param   symbols symbols witch are compared with
   * the jsp source
   * @return  true if chars are equal
   */
  public boolean compare(int start, int end, char[] symbols) throws JspParseException {
    if (start > end) {
      throw new JspParseException(JspParseException.WRONG_START_OR_END_INDEXES);
    }

    if (end - start != symbols.length) {
      return false;
    }

    return cmp(start, source, symbols);
  }
  
  /**
   * Compares a part of the jsp source with the specified chars.
   * If the end tag is of XML form whitespaces are ignored before the closng bracket. 
   * //   ETag ::=  '</' Name S? '>'
   * @param startPosition - start  start position for comparing in the jsp source
   * @param endIndent - symbols witch are compared with the jsp source, the closing tag - "%>", "</jsp:include>" or "</jsp:include >"
   * @return
   * @throws JspParseException
   */
  public boolean compareEndIndex(int startPosition, char[] endIndent) throws JspParseException{
    
    boolean result = compare(startPosition, startPosition + endIndent.length, endIndent);
    if( result && endIndent[endIndent.length-1] != '>') {
      return result;
    }
    char[] endIdentCopy = new char[endIndent.length - 1]; // copy of the end tag without the closing bracket
    for (int i = 0; i < endIndent.length - 1; i++) {
      endIdentCopy[i] = endIndent[i];
    }
    if (compare(startPosition, startPosition + endIdentCopy.length, endIdentCopy)) { // check if the begining chars are matching
      int checkPosition = startPosition + endIdentCopy.length;
      while (!endReached() && Element.isWhiteSpace(charAt(checkPosition))) {
        checkPosition++;        
      }
      if (charAt(checkPosition) == '>') {
        result = true;
      }    
    }
    return result;
  }

  /**
   * Compares a part of the jsp source with the specified
   * chars, ignoring case differences.
   *
   * @param   start  start position for comparing in the
   * jsp source
   * @param   end  end position for comparing in the
   * jsp source
   * @param   symbols symbols witch are compared with
   * the jsp source
   * @return  true if chars are equal
   */
  public boolean compareIgnoreCase(int start, int end, char[] symbols) throws JspParseException {
    if (start > end) {
      throw new JspParseException(JspParseException.WRONG_START_OR_END_INDEXES);
    }

    if (end - start != symbols.length) {
      return false;
    }

    return cmpIgnoreCase(start, source, symbols);
  }

  /**
   * Compares a portion form array with whole array
   *
   * @param   start  start position for the portion in
   * the first array
   * @param   ar0  first array
   * @param   ar1  second array
   * @return  true if chars of the arrays are equal
   */
  private boolean cmp(int start, char[] ar0, char[] ar1) {
    boolean ret = true;

    if (ar1.length + start > ar0.length) {
      return false;
    }

    for (int i = 0; i < ar1.length; i++, start++) {
      ret = ret && (ar0[start] == ar1[i]);
    }

    return ret;
  }

  /**
   * Compares a portion form array with whole array, ignoring case differences.
   *
   * @param   start  start position for the portion in
   * the first array
   * @param   ar0  first array
   * @param   ar1  second array
   * @return  true if chars of the arrays are equal
   */
  private boolean cmpIgnoreCase(int start, char[] ar0, char[] ar1) {
    boolean ret = true;

    if (ar1.length + start > ar0.length) {
      return false;
    }

    for (int i = 0; i < ar1.length; i++, start++) {
      ret = ret && (Character.toLowerCase(ar0[start]) == Character.toLowerCase(ar1[i]));
    }

    return ret;
  }


  /**
   * String representation of part of the jsp source
   *
   * @param   start  start index for the part in the
   * jsp source
   * @param   end  end index for the part in the
   * jsp source
   * @return  string representation of the portion
   */
  public String getChars(int start, int end) {
    return String.copyValueOf(source, start, end - start);
  }

  /**
   * Trimmed string representation of part of the jsp source
   *
   * @param   start  start index for the part in the
   * jsp source
   * @param   end  end index for the part in the
   * jsp source
   * @return  trimmed string representation of the portion
   */
  public String getCharsTrim(int start, int end) {
    return String.copyValueOf(source, start, end - start).trim();
  }

  /**
   * Parses specified file included
   *
   * @param   file  file to parse
   * @return   Element that represents all jsp source
   * @exception   JspParseException  thrown if error occurs
   * during parsing
   */
  public Element parse(File file) throws JspParseException {
		ParserImpl p = new ParserImpl(this, file, tagLibDescriptors);
    if (getTagInfo() != null) {
      p.setTagInfo(getTagInfo());
    }
    String oldPath = p.getPath();
    String filePath = null;
    try {
      filePath = file.getCanonicalPath();
    } catch (IOException io) {
      throw new JspParseException(JspParseException.CANNOT_GET_CANNONICAL_JSP_PATH, new Object[]{file.getPath()}, io);
    }
    JspConfigurationProperties jspCfg = null;
    if( parserProperties.getHttpAliasValue() == null ) {  // no JSP config for JSPs in http alias
      String fileRoot = filePath.substring(parserProperties.getApplicationRootDir().length() -1).replace(File.separatorChar, ParseUtils.separatorChar);
      jspCfg = parserProperties.getJspConfiguration().getJspProperty(fileRoot);
      // overwrite web.xml settings to the parser for the included JSP 
      if (jspCfg.isDeferredSyntaxAllowedAsLiteral() != null) {
        p.setDeferredSyntaxAllowedAsLiteral(new Boolean(jspCfg.isDeferredSyntaxAllowedAsLiteral()).booleanValue());
      } else {
        p.setDeferredSyntaxAllowedAsLiteral(false); //default value
      }
      if (jspCfg.isTrimDirectiveWhitespaces() != null) {
        p.setTrimDirectiveWhitespaces(new Boolean(jspCfg.isTrimDirectiveWhitespaces()).booleanValue());
      } else {
        p.setTrimDirectiveWhitespaces(false); //default value
      }
    }
    p.pageEncodingHelper = new PageEncodingHelper(file, jspCfg, p);
    p.pageEncodingHelper.mainEncoding = getPageEncoding();
    p.isIncluded = true;
    p.setPath(filePath.substring(0, filePath.lastIndexOf(File.separator)));
    Element element = p.parse();
    p.setPath(oldPath);
    return element;
  }


  /**
   * Parses jsp source and returns newly created element
   *
   * @return jsp element representing all jsp source
   * @exception   JspParseException  thrown if error occurs
   * during parsing
   */
  public Element parse() throws JspParseException {
    JspTag[] includePrelude = null;
    String detectedEncoding = null;
    try {
      detectedEncoding = preprocessPage();
    } catch (IOException e) {
      throw new JspParseException(JspParseException.IO_ERROR_IN_FILE, new Object[]{currentFile.getName()}, e);
    }
    this.setPageEncoding(detectedEncoding);
    if (isXml()) {
      SAXParserFactory sf = SAXParserFactory.newInstance();

      SAXParser sp = null;
      SAXTreeBuilder handler = null;
      try {
        sf.setNamespaceAware(true);
        sf.setFeature("http://xml.org/sax/features/namespace-prefixes", true);

        //todo - da se proveri dali garmi s tozi feature i ako ne - da se polzva vmesto proverkata za xmlns: v  SAXJspElenemtFactory
        // http://www.saxproject.org/apidoc/org/xml/sax/package-summary.html#package_description        
        // sf.setFeature("http://xml.org/sax/features/xmlns-uris", true);

        sf.setValidating(true);
       // sf.setFeature("http://xml.org/sax/features/validation",true);
        sf.setFeature("http://apache.org/xml/features/validation/dynamic", true);
        sp = sf.newSAXParser();
        handler = new SAXTreeBuilder(this);
        sp.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
        if (!isIncluded && this.includePrelude != null && this.includePrelude.size() > 0) {
          includePrelude = addIncludePrelude();
        }
        sp.parse(currentFileName, handler);       
        parsed = handler.getParsed();
      } catch (Exception e) {
        //e.printStackTrace();
        throw new JspParseException(JspParseException.ERROR_IN_PARSING_JSP_FILE , new Object[]{e.getMessage()}, e);
      }
    } else {
      try {
        readFileWithEncoding(currentFile, detectedEncoding);  //encoding
      } catch (CharConversionException miex) { // sun.io.ByteToCharUTF8.convert() BUG
        // try with default encoding
        try {
          readFileWithEncoding(currentFile, ISO_8859_1);
        } catch (IOException ex) {
          throw new JspParseException(JspParseException.IO_ERROR_IN_FILE, new Object[]{currentFile.getName()}, ex);
        }
      } catch (IOException ex) {
        throw new JspParseException(JspParseException.IO_ERROR_IN_FILE, new Object[]{currentFile.getName()}, ex);
      }
      if (!isIncluded && this.includePrelude != null && this.includePrelude.size() > 0) {
        includePrelude = addIncludePrelude();
      }
      parsed = rootElement.parse(this);
    }
    if (!isIncluded) {
      if (includePrelude != null) {
        if (parsed instanceof ElementCollection) {
          Element[] elementsTemp = new Element[parsed.elements.length + includePrelude.length];
          System.arraycopy(includePrelude, 0, elementsTemp, 0, includePrelude.length);
          System.arraycopy(parsed.elements, 0, elementsTemp, includePrelude.length, parsed.elements.length);
          parsed.elements = elementsTemp;
        } else { //JspRoot
          Element[] elementsTemp = new Element[((JspElement) parsed).getBody().elements.length + includePrelude.length];
          System.arraycopy(includePrelude, 0, elementsTemp, 0, includePrelude.length);
          System.arraycopy(((JspElement) parsed).getBody().elements, 0, elementsTemp, includePrelude.length, ((JspElement) parsed).getBody().elements.length);
          ((JspElement) parsed).getBody().elements = elementsTemp;
        }

      }
      if (includeCoda != null && includeCoda.size() > 0) {
        // codas are managed as include directives
        JspTag[] includes = new JspTag[includeCoda.size()];
        for (int i = 0; i < includes.length; i++) {
          includes[i] = new JspIncludeDirectiveTag();
          includes[i].debugInfo = new DebugInfo(currentDebugPos(), currentDebugPos(), currentFileName(), getWebContainerParameters().isJspDebugSupport());
          includes[i].parser = this;
          Indentifier name = new Indentifier("file");
          name.indentifierType = Indentifier.NOT_QUOTED;
          Indentifier value = new Indentifier(includeCoda.get(i));
          value.indentifierType = Indentifier.QUOTED;
          includes[i].setAttributes(new Attribute[]{new Attribute(name, value, false)});
          includes[i].verifyAttributes();
        }
        if (parsed instanceof ElementCollection) {
          Element[] elementsTemp = new Element[parsed.elements.length + includes.length];
          System.arraycopy(parsed.elements, 0, elementsTemp, 0, parsed.elements.length);
          System.arraycopy(includes, 0, elementsTemp, parsed.elements.length, includes.length);
          parsed.elements = elementsTemp;
        } else { //JspRoot
          Element[] elementsTemp = new Element[((JspElement) parsed).getBody().elements.length + includes.length];
          System.arraycopy(((JspElement) parsed).getBody().elements, 0, elementsTemp, 0, ((JspElement) parsed).getBody().elements.length);
          System.arraycopy(includes, 0, elementsTemp, ((JspElement) parsed).getBody().elements.length, includes.length);
          ((JspElement) parsed).getBody().elements = elementsTemp;
        }
      }
    }
    if( parsed instanceof ElementCollection){
      validate((ElementCollection)parsed);
    }else{
      validate(parsed);
    }
    
    return parsed;
  }
  
  /**
   * Here after parsin a verification of the parsed elements should be initiated.
   * The intention is to replace verify method from JspElement, but this is impossible for the moment,
   * as additional parsing is done in it.  
   * @param parsed - Either the tree with the parsed element or one element from it. 
   * @throws JspParseException
   */
  private void validate(Element parsed) throws JspParseException{
    if( parsed instanceof JspElement){
      JspElement element = (JspElement) parsed;
      JspElement parent = element.getParentJspElement();
      switch (element.type ) {
      case JspElement.ATTRIBUTE:
        if ( parent == null ){
          throw new JspParseException(JspParseException.MUST_BE_SUBELEMENT_OF_STANDARD_OR_CUSTOM_ACTION, new Object[]{getRootElement()._default_prefix_tag_start + "attribute"});
        } else {
         if( parent.getStartTag() == null || 
             parent.getStartTag().tagType == JspTag.SINGLE_TAG ||
             parent.type == JspElement.ATTRIBUTE  // A translation error must occur if <jsp:attribute> is used to define the value of an attribute of <jsp:attribute>.             
             ){
           throw new JspParseException(JspParseException.MUST_BE_SUBELEMENT_OF_STANDARD_OR_CUSTOM_ACTION, new Object[]{parent._default_prefix_tag_start + "attribute"});           
         }
        }
        break;
        case JspElement.BODY:
          if ( parent == null ){
            throw new JspParseException(JspParseException.MUST_BE_SUBELEMENT_OF_STANDARD_OR_CUSTOM_ACTION, new Object[]{getRootElement()._default_prefix_tag_start + "body"});
          } else {
           if( parent.getStartTag() == null || 
               parent.getStartTag().tagType == JspTag.SINGLE_TAG ||
               parent.type == JspElement.BODY || // It is also legal to use the <jsp:body> standard action to supply bodies to
               parent.type == JspElement.ATTRIBUTE || // standard actions, for any standard action that accepts a body (except for
               parent.type == JspElement.SCRIPLET || // <jsp:body>, <jsp:attribute>, <jsp:scriptlet>, <jsp:expression>, and <jsp:declaration>).
               parent.type == JspElement.EXPRESSION ||
               parent.type == JspElement.DECLARATION
               ){
             throw new JspParseException(JspParseException.MUST_BE_SUBELEMENT_OF_STANDARD_OR_CUSTOM_ACTION, new Object[]{parent._default_prefix_tag_start + "body"});           
           }
          }        
          break;
      default:
        break;      
      }
      if( element.getBody() != null && element.getBody() instanceof ElementCollection){
        validate((ElementCollection)element.getBody());
      }
    } else if ( parsed instanceof ElementCollection ){
      validate((ElementCollection)parsed);
    }
  }
  
  private void validate(ElementCollection parsed) throws JspParseException{
    if( parsed == null || parsed.getElements() == null || parsed.getElements().length == 0){
      return;
    }
    Element[] elements = parsed.getElements();
    for (int i = 0; i < elements.length; i++) {
      Element currentElement = elements[i];
      validate(currentElement);
    }
  }

  public Element getParsed() {
    return parsed;
  }

  /**
   * This method is called when constructing ParserImpl object.
   * Used to initialize the object properties.
   * @throws JspParseException
   *
   */
  private void init(File file) throws JspParseException{
    currentFile = file;
    try {
      currentFileName = file.getCanonicalPath().replace(File.separatorChar, ParseUtils.separatorChar);
    } catch (IOException ex) {
      throw new JspParseException(JspParseException.IO_ERROR_IN_FILE, new Object[] { file.getName() }, ex);
    }
    //Jsp numbering starts from 1. Debug possition should start from 1 too. 
    this.line = 1;
    this.linePos = 0;
    this.srcPos = 0;
    initStatics();
  }

  /**
   * Registers all jsp elements that take place in
   * parsing
   */
  private void initStatics() throws JspParseException {
    // JSP.1.5.1  Dynamic content that appears within HTML/XML comments, such as
    //actions, scriptlets and expressions, is still processed by the container.

    //OutputCommentElement outputCommentElement = new OutputCommentElement();
    //rootElement.registerParsingElement(outputCommentElement);

    JspCommentElement jspCommentElement = new JspCommentElement() ;
    rootElement.registerParsingElement(jspCommentElement);

    rootElement.registerParsingElement(new AttributeDirective());
    rootElement.registerParsingElement(new JspIncludeDirective());
    rootElement.registerParsingElement(new JspPageDirective());
    rootElement.registerParsingElement(new JspTaglibDirective());
    rootElement.registerParsingElement(new TagDirective());
    rootElement.registerParsingElement(new VariableDirective());

    CustomJspTag tag = new UseBeanTag();
    tag.setCopy(true);
    rootElement.registerParsingElement(tag);
    tag = new SetPropertyTag();
    tag.setCopy(true);
    rootElement.registerParsingElement(tag);
    tag = new GetPropertyTag();
    tag.setCopy(true);
    rootElement.registerParsingElement(tag);
    tag = new IncludeActionTag();
    tag.setCopy(true);
    rootElement.registerParsingElement(tag);
    if( !isIncluded() ){
      // if included will use the RootTag from the including JSP.
      rootElement.registerParsingElement(new RootTag());
    }

    tag = new TextTag() ;
    tag.setCopy(true);
    rootElement.registerParsingElement(tag);

    //rootElement.registerParsingElement(new JspDirectiveTag());

    tag = new DeclarationTag() ;
    rootElement.registerParsingElement(tag);

    rootElement.registerParsingElement(new XMLTag());

    //boby
    tag = new ForwardTag();
    tag.setCopy(true);
    rootElement.registerParsingElement(tag);
    tag = new ParamTag();
    tag.setCopy(true);
    rootElement.registerParsingElement(tag);
    tag = new PluginTag();
    tag.setCopy(true);
    rootElement.registerParsingElement(tag);
    tag = new ExpressionTag();
    tag.setCopy(true);
    rootElement.registerParsingElement(tag);
    tag = new ParamsTag();
    tag.setCopy(true);
    rootElement.registerParsingElement(tag);
    tag = new FallbackTag();
    tag.setCopy(true);
    rootElement.registerParsingElement(tag);

    JspDeclarationScript jspDeclarationScript = new JspDeclarationScript();
    rootElement.registerParsingElement(jspDeclarationScript);

    JspExpression jspExpression = new JspExpression();
    rootElement.registerParsingElement(jspExpression);

    JspScriptlet jspScriptlet = new JspScriptlet();
    rootElement.registerParsingElement(jspScriptlet);

    ScriptletTag scripletTag = new ScriptletTag();
    rootElement.registerParsingElement(scripletTag);
    //new JSP 2.0
    tag = new ElementActionTag();
    tag.setCopy(true);
    rootElement.registerParsingElement(tag);

    tag = new AttributeTag();
    tag.setCopy(true);
    rootElement.registerParsingElement(tag);

    tag = new BodyActionTag();
    tag.setCopy(true);
    rootElement.registerParsingElement(tag);

    tag = new DoBodyActionTag();
    tag.setCopy(true);
    rootElement.registerParsingElement(tag);

    tag = new InvokeActionTag();
    tag.setCopy(true);
    rootElement.registerParsingElement(tag);

    tag = new OutputActionTag();
    tag.setCopy(true);
    rootElement.registerParsingElement(tag);

    rootElement.registerParsingElement(new JspPageDirectiveTag());
    rootElement.registerParsingElement(new JspIncludeDirectiveTag());
    rootElement.registerParsingElement(new AttributeDirectiveTag());
    rootElement.registerParsingElement(new TagDirectiveTag());
    rootElement.registerParsingElement(new VariableDirectiveTag());
  }

  private void readFileWithEncoding(File file, String encoding) throws IOException, CharConversionException, JspParseException {
    FileInputStream fis = new FileInputStream(file);
    InputStreamReader isr = null;
    try {
      isr = new InputStreamReader(fis, encoding);
    } catch (UnsupportedEncodingException ue) {
      fis.close();
      throw new JspParseException(JspParseException.UNSUPPORTED_ENCODING, new Object[]{encoding}, ue);
    }
    try {
    CharArrayWriter caw = new CharArrayWriter();
    char[] buf = new char[1024];
    boolean bomSkipped = false;
    for (int i = 0; (i = isr.read(buf)) != -1;) {
      if( !bomSkipped && pageEncodingHelper.getBomLength() >0 ){
        bomSkipped = true;
        // BOM is one character - Unicode character code U+FEFF
        // it can consist of 2, 3 or 4 bytes 
        caw.write(buf, 1, i-1);
      }else{
        caw.write(buf, 0, i);
      }
      
    }

    source = caw.toCharArray();
    } finally {
      isr.close();
    }
  }

  public JspConfigurationProperties getJspProperty()
  {
  	return parserProperties.getJspConfigurationProperties();
  }


	public void registerParsingElement(Element el) throws JspParseException
	{
		rootElement.registerParsingElement(el);
	}
	/**
	 *
	 * @return true - if the currently parsed unit is requested by <jsp:include>
	 */
	public boolean isIncluded(){
	  return isIncluded;
	}
  public WebContainerParameters getWebContainerParameters() {
    return containerParameters;
  }

  /**
   * returns number of the character this JSP page contains
   * @return -1 if no body of JSP page is found
   */
  public int getSourceLength(){
    if( src != null ){
      return src.length();
    }else if( source != null ){
      return source.length;
    }
    return -1;
  }

  /**
   * Performes initial preprocessing of the JSP page/tag file:
   * - initiates the current ParserImpl object - JspConfigurationProperties - preludes
   * - detects page syntax
   * - determine if EL is ignored
   * @throws JspParseException
   * @throws IOException
   */
  public String preprocessPage() throws IOException, JspParseException {
    if (!isTagFile()) {
      if (parserProperties.getJspConfigurationProperties() != null) {
        includePrelude = parserProperties.getJspConfigurationProperties().getIncludePrelude();
        includeCoda = parserProperties.getJspConfigurationProperties().getIncludeCoda();
      }
    }
    return pageEncodingHelper.detect();
  }

  public boolean isDefaultEncodingUsed() {
    return pageEncodingHelper.isDefaultEncodingUsed();
  }
  public boolean isXml() {
    if (pageEncodingHelper != null){
      return pageEncodingHelper.isXml();
    }
    //default value is JSP syntax
    return false;
  }

  /**
   * Generates new unique for this page jspId that serves for the purpose of the tag handlers
   * which implement javax.servlet.jsp.tagext.JspIdConsumer.
   * @return - unique int for this JSP page.
   */
  public String getNextTagID(){
    return "id"+parserProperties.getJspIdGenerator().getIdSync();
  }

  private JspTag[] addIncludePrelude() throws JspParseException {
    JspTag[] includes = new JspTag[includePrelude.size()];
    for (int i = 0; i < includes.length; i++) {
      includes[i] = new JspIncludeDirectiveTag();
      includes[i].debugInfo = new DebugInfo(currentDebugPos(), currentDebugPos(), currentFileName(), getWebContainerParameters().isJspDebugSupport());
      includes[i].parser = this;
      Indentifier name = new Indentifier("file");
      name.indentifierType = Indentifier.NOT_QUOTED;
      Indentifier value = new Indentifier(includePrelude.get(i));
      value.indentifierType = Indentifier.QUOTED;
      includes[i].setAttributes(new Attribute[]{new Attribute(name, value, false)});
      includes[i].verifyAttributes();
    }
    return includes;
  }
}

