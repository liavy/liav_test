/*
 * Copyright (c) 2002 by SAP Labs Bulgaria AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.UnsupportedCharsetException;

import com.sap.engine.services.servlets_jsp.jspparser_api.JspConfigurationProperties;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.tc.logging.Location;

/**
 * This class serves as wrapper for the page detection algorithm. Here both tag files and JSP pages are detected.
 * See JSP 2.1 design document for detailed info on the algorithm:
 * https://ifp.wdf.sap.corp/sap/bc/bsp/sap/edms_files/edmsviewfilesession.htm?filename=dummy.pdf&refId=0C8785BFCCCE3542BC526139DAAC19CC&type=TTO&relId=L&objId=6DDF74D6C579B347914DC5C04B0AF2AB   
 * @author Todor Mollov DEV_webcontainer 2005-12-12
 *  
 */
public class PageEncodingHelper {
  private static final String JSPX = ".jspx";

  private static final String TAGX = ".tagx";
  /**
   * In XML spec 1.0 F.1 Detection Without External Encoding Information
   * UTF-8 without an encoding declaration, or else the data stream is mislabeled (lacking a required encoding declaration), 
   * corrupt, fragmentary, or enclosed in a wrapper of some kind
   */
  private static final String DEFAULT_ENCODING = "UTF-8";

  private static final String ISO_8859_1 = "ISO-8859-1";

  private static final String XML_ENCODING = "encoding";

  private static final String PAGE_ENCODING = "pageEncoding";

  private static final String IS_EL_IGNORED = "isELIgnored";

  private static final String CONTENT_TYPE = "contentType";

  private static final String JSP_DIRECTIVE_OPEN = "<jsp:directive.";
  
  private static final String JSP_DIRECTIVE_PAGE_OPEN = "<jsp:directive.page";

  private static final String JSP_DIRECTIVE_PAGE_CLOSE = "/>";

  private static final String XML_BEGIN = "<?xml";

  private static final String XML_END = "?>";

  private static final String CHARSET = "charset=";

  private static final String PAGE = "page";

  private static final String TAG = "tag";

  private static final String DIRECTIVE_OPEN = "<%@";

  private static final String DIRECTIVE_CLOSE = "%>";

  private static final int BUFFER = 1024 * 2;

  private static Location currentLocation = Location.getLocation(PageEncodingHelper.class);

  /**
   * The file which encoding should be detected.
   */
  private File fileToDetect;
  
  /**
   * The instance of the ParserImpl that this Helper helps to detect the syntax and page encoding.
   */
  private ParserImpl parserImpl;

  /**
   * If this is JSP or tag file in XML syntax or JSP syntax
   */
  private boolean isXml = false;

  /**
   * Indicates if the default encoding was used to read the file, because
   * nothing other was specified - neither in JspConfifuration, pageEncoding
   * directive, etc.
   */
  private boolean isDefaultEncodingUsed = false;

  private JspConfigurationProperties jspConfigurationProperties;

  private String encodingFromXMLbytes = null;

  private String encodingXMLProlog = null;

  //private String encodingFromFile = null;

  private String encodingFromPageEncodingPageDirective = null;
  
  private String encodingFromContentTypePageDirective = null; 

  private String encodingFromJSPProperty = null;

  private String isELIgnoredFromPageDirective = null;

  /**
   * With how many bytes the BOM is represented. 
   */
  private int bytesBom = -1;

  /**
    * Used to parse included jsps
    */
  protected String mainEncoding = null;

  /**
   * 
   * @param file -
   *          the file for which this helper will define its encoding
   */
  public PageEncodingHelper(File file, JspConfigurationProperties configurationProperties, ParserImpl impl) {
    this.jspConfigurationProperties = configurationProperties;
    this.fileToDetect = file;
    this.parserImpl = impl;
  }
  
  /**
   * The main method that starts the page encoding detection algorithm. There are 2 main steps. Detect page syntax and detect page encoding.
   * The algorithm is described in JSP 2.1 design doc: https://ifp.wdf.sap.corp/sap/bc/bsp/sap/edms_files/edmsviewfilesession.htm?filename=dummy.pdf&refId=0C8785BFCCCE3542BC526139DAAC19CC&type=TTO&relId=L&objId=6DDF74D6C579B347914DC5C04B0AF2AB 
   * @return
   * @throws IOException
   * @throws JspParseException
   */
  public String detect() throws IOException, JspParseException {
    String encoding = null;

    boolean syntaxFound = findExternalSyntax();
    boolean searchForRoot = !parserImpl.isTagFile() & !syntaxFound ;
    searchInFile(fileToDetect, searchForRoot);

    //JSP 3.3 - JSP Property Groups do not affect tag files
    if( jspConfigurationProperties != null && !parserImpl.isTagFile()){
      //  check jspProperty for pageEncoding
      if (jspConfigurationProperties.getPageEncoding() != null) {
        encodingFromJSPProperty = jspConfigurationProperties.getPageEncoding();
      }
    }
    //  check EIIngored
    if (isELIgnoredFromPageDirective == null) {
      if (parserImpl.isTagFile()) { //default is false
        parserImpl.setIsELIgnored(false);
      } else {
        if (jspConfigurationProperties != null) {
          if (jspConfigurationProperties.isELIgnored() != null) {
            parserImpl.setIsELIgnored(jspConfigurationProperties.isELIgnored().equalsIgnoreCase("true"));
          } else {
            parserImpl.setIsELIgnored(jspConfigurationProperties.isOldApplication());
          }
        } else {
          parserImpl.setIsELIgnored(false);
        }
      }
    }
    
    if( isXml ){// XML Syntax 
      if( bytesBom > 0 ){ // has BOM
        encoding = encodingFromXMLbytes;
      } else{ // has no BOM
        encoding = encodingXMLProlog;        
      }
      if( encoding == null ){
        encoding = encodingFromJSPProperty;
      }
      if( encoding == null ){
        encoding = encodingFromPageEncodingPageDirective;        
      }
      if( encoding == null){
        if (parserImpl.isIncluded()) {
          encoding = mainEncoding;
        } else {
          setIsDefaultEncodingUsed(true);
          encoding = DEFAULT_ENCODING;
        }
      }
     }else{// standard syntax
      if( bytesBom > 0 ){ // has BOM
        encoding = encodingFromXMLbytes;
      }else{ // no BOM
        encoding = encodingFromJSPProperty;
        if( encoding == null ){
          encoding = encodingFromPageEncodingPageDirective;
          if( encoding == null ){
            if( parserImpl.isTagFile() ){
              encoding = ISO_8859_1;  
              setIsDefaultEncodingUsed(true);
            }else{
              encoding = encodingFromContentTypePageDirective;
              if( encoding == null ){
                if (parserImpl.isIncluded()) {
                  encoding = mainEncoding;
                } else {
                  setIsDefaultEncodingUsed(true);
                  encoding = ISO_8859_1;
                }
              }
            }// end if tag file            
          }
        }
      }
    }
    verifyEncodings();
    return encoding;
  }
  
  /**
   * This method verifies that the encodings are consistent.
   * @throws JspParseException - thrown when encodings are not identical i.e. web.xml defiend "UTF-8", but BOM indicates "UTF-16". 
   */
  private void verifyEncodings() throws JspParseException{
    if( bytesBom > 0 ) {
      // check BOM and tag directive
      if( encodingFromPageEncodingPageDirective != null && !encodingFromPageEncodingPageDirective.equalsIgnoreCase(encodingFromXMLbytes) 
          && (!encodingFromPageEncodingPageDirective.startsWith("UTF-16") || !encodingFromXMLbytes.startsWith("UTF-16"))){
        throw new JspParseException(JspParseException.DIFFERENT_ENCODINGS_ATTRIBUTE_BOM, new Object[] { encodingFromXMLbytes, encodingFromPageEncodingPageDirective });
      }
    }
    if( parserImpl.isTagFile() ){
      // this is the only error condition for tag files defined by the spec.
      return;
    }
    
    // both XML nad JSP syntax should check this.
    // it is not explicitly defined in the specification, but Glassfish throws exception.      
    if (bytesBom > 0 ) {
      /*
       * Make sure the encoding specified in the BOM matches
       * that in the JSP config element, treating "UTF-16", "UTF-16BE",
       * and "UTF-16LE" as identical.
       */
      if( encodingFromJSPProperty != null && !encodingFromJSPProperty.equalsIgnoreCase(encodingFromXMLbytes) 
          && (!encodingFromJSPProperty.startsWith("UTF-16") || !encodingFromXMLbytes.startsWith("UTF-16"))){
        throw new JspParseException(JspParseException.DIFFERENT_ENCODINGS_PROPERTY_BOM, new Object[] { encodingFromJSPProperty, encodingFromXMLbytes });          
      }      

    }
    if( isXml ){// XML Syntax 
      if( bytesBom > 0 ){ // has BOM
        if ( encodingXMLProlog != null && !encodingXMLProlog.equalsIgnoreCase(encodingFromXMLbytes)
            && (!encodingFromPageEncodingPageDirective.startsWith("UTF-16") || !encodingXMLProlog.startsWith("UTF-16")) ){
          throw new JspParseException(JspParseException.DIFFERENT_ENCODINGS_XML_BODY, new Object[] { encodingXMLProlog, encodingFromXMLbytes });
        }
      }
      // XMLspec: 4.3.3 Character Encoding in Entities
      // Entities encoded in UTF-16 MUST and entities encoded in UTF-8 MAY begin with the Byte Order Mark
      // XML processors SHOULD match character encoding names in a case-insensitive way
      if( encodingXMLProlog != null && encodingFromPageEncodingPageDirective != null && !encodingXMLProlog.equalsIgnoreCase(encodingFromPageEncodingPageDirective)
          && (!encodingFromPageEncodingPageDirective.startsWith("UTF-16") || !encodingXMLProlog.startsWith("UTF-16"))
          ){
        throw new JspParseException(JspParseException.DIFFERENT_ENCODINGS_XML_BODY, new Object[] { encodingXMLProlog, encodingFromPageEncodingPageDirective });
      }
      /*
       * Make sure the encoding specified in the XML prolog matches
       * that in the JSP config element, treating "UTF-16", "UTF-16BE",
       * and "UTF-16LE" as identical.
       */
      if( encodingXMLProlog != null && encodingFromJSPProperty != null && !encodingXMLProlog.equalsIgnoreCase(encodingFromJSPProperty) 
          && (!encodingFromJSPProperty.startsWith("UTF-16") || !encodingXMLProlog.startsWith("UTF-16"))){
        throw new JspParseException(JspParseException.DIFFERENT_ENCODINGS_PROPERTY_XML, new Object[] { encodingFromJSPProperty, encodingXMLProlog });
      }
      if( encodingFromPageEncodingPageDirective != null && encodingFromJSPProperty != null && !encodingFromPageEncodingPageDirective.equalsIgnoreCase(encodingFromJSPProperty) 
          && (!encodingFromPageEncodingPageDirective.startsWith("UTF-16") || !encodingFromJSPProperty.startsWith("UTF-16"))){
        throw new JspParseException(JspParseException.DIFFERENT_ENCODINGS_PROPERTY_BODY, new Object[] { encodingFromJSPProperty, encodingFromPageEncodingPageDirective });
      }      
    }else{// standard syntax
      if( bytesBom < 0 ){ // no BOM
        if(encodingFromJSPProperty != null && encodingFromPageEncodingPageDirective != null && !encodingFromPageEncodingPageDirective.equalsIgnoreCase(encodingFromJSPProperty) 
            && (!encodingFromPageEncodingPageDirective.startsWith("UTF-16") || !encodingFromJSPProperty.startsWith("UTF-16"))){ 
          throw new JspParseException(JspParseException.DIFFERENT_ENCODINGS_PROPERTY_BODY, new Object[] { encodingFromJSPProperty, encodingFromPageEncodingPageDirective });
        }
      }
    }

  }
  /***
   * Tries to determine the syntaxt of the file without reading it. 
   * @return
   * @throws IOException
   * @throws JspParseException
   */
  private boolean findExternalSyntax() throws IOException, JspParseException{
    //JSP 3.3 - JSP Property Groups do not affect tag files
    if (jspConfigurationProperties != null && !parserImpl.isTagFile()) {
      if (jspConfigurationProperties.isXml() != null) {
        isXml = jspConfigurationProperties.isXml().equalsIgnoreCase("true");
        return true;
      } 
    }
    if ( (parserImpl.currentFileName().endsWith(JSPX) || parserImpl.currentFileName().endsWith(TAGX)) ) {
      isXml = true;
      return true;
    } else if( parserImpl.isTagFile() ){
      isXml = false;
      return true;        
    }
    return false;
  }

  private void searchInFile(File file, boolean searchForRoot) throws IOException, JspParseException {
    InputStreamReader isr = null;
    FileInputStream fis = null;
    try {
      char[] source = null;
      int[] firstBytes = {-1, -1, -1, -1};
      int read = 0;
      fis = new FileInputStream(file);
      try {
        for (; read<4; read++ ) {
          int readed = fis.read();
          if( readed == -1){
            break;
          }
          int currByte =  readed & 0xFF;
          firstBytes[read] = currByte;          
        }        
      } finally {
        fis.close();
      }
      Object[] resultForEncodingName = getEncodingName(firstBytes, read);
      String initialEncoding = (String)resultForEncodingName[0]; 
      encodingFromXMLbytes = initialEncoding;
      if( resultForEncodingName[1] != null){
        bytesBom = ((Integer)resultForEncodingName[1]).intValue();
      }      
      

      fis = new FileInputStream(file);

      Charset cs = null;
      try {
        cs = Charset.forName(initialEncoding);
      } catch (UnsupportedCharsetException e) {
        LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000255", 
          "Unsupported encoding [{0}] found during read of JSP file [{1}]. " +
          "Initial encoding is going to be set to the default one [{2}].", 
          new Object[]{initialEncoding, file.getAbsolutePath(), DEFAULT_ENCODING}, e, null, null);
        cs = Charset.forName(DEFAULT_ENCODING);
        initialEncoding = DEFAULT_ENCODING;
      }
      CharsetDecoder d = cs.newDecoder();
      d.onMalformedInput(CodingErrorAction.IGNORE);
      isr = new InputStreamReader(fis, d);

      source = new char[BUFFER];
      read = isr.read(source);
      if (read == -1) {
        return;
      }
      
      String str1 = new String(source, 0, read);
      if (searchForRoot) { // tag files will not search for root
        //first check the BUFFER size beggining of the page for jsp:root
        if( checkForRoot(str1) ){
	        isXml = true;	       
        }else{ // then search the rest of the page
          while ((read = isr.read(source)) > -1) {
            String str2 = new String(source);
	          // consume more memory than IO operations, 
	          // this already read string will be used for checkForEncoding()(it won't be necessary to read it again)
            str1 += str2;
  	        if( checkForRoot(str1) ){
  		        isXml = true;
  		        break;
  	        }
          }
        }
      }
      // so far page syntax is determined.
      
      String found = checkForEncoding(str1);
      if (found != null) {
        return;
      }
      while ((read = isr.read(source)) > -1) {
        String str2 = new String(source, 0, read);
        found = checkForEncoding(str1 + str2);
        if (found != null) {
          break;
        }
        // we could not concatenate, but replace, because no following read is expected. 
        str1 = str2;
      }
    } finally {
      if (isr != null) {
        try {
          isr.close();
        } catch (IOException e) {
          LogContext.getCategory(LogContext.CATEGORY_SERVICE).logWarning(currentLocation, "ASJ.web.000277", 
            "Cannot close file [{0}]. This may lead to file handlers leak.", new Object[]{file}, e, null, null);
        }
      } else if (fis != null) {
        try {
          fis.close();
        } catch (IOException e) {
          LogContext.getCategory(LogContext.CATEGORY_SERVICE).logWarning(currentLocation, "ASJ.web.000278", 
            "Cannot close file [{0}]. This may lead to file handlers leak.", new Object[]{file}, e, null, null);
        }
      }
    }
  }
  
  /**
   * Returns the IANA encoding name that is auto-detected from
   * the bytes specified, with the endian-ness of that encoding where
   * appropriate.
   *
   * @param firstFour    The first four bytes of the input.
   * @param count The number of bytes actually read.
   * @return a 2-element array:  the first element, an IANA-encoding string,
   * the second element - an Integer with the number of bytes the BOM consists of or null BOM is not found 
   *   
   */
  private Object[] getEncodingName(int[] firstFour, int count) {

      // default to UTF-8 if we don't have enough bytes to make a
      // good determination of the encoding
      if (count < 2) {
          return new Object [] {"UTF-8", null, null};
      }

      // UTF-16, with BOM
      int first = firstFour[0] ;
      int second = firstFour[1] ;
      int third = firstFour[2];
      int forth = firstFour[3] ;
      
      if (first == 0xFE && second == 0xFF && (third != 0x00 || forth != 0x00) ){
          // UTF-16, big-endian, with a BOM
          return new Object [] {"UTF-16BE", new Integer(2)};
      }

      if (first == 0xFF && second == 0xFE && (third != 0x00 || forth != 0x00) ){
          // UTF-16, little-endian, with a BOM
          return new Object [] {"UTF-16LE", new Integer(2)};
      }

      // UTF-8 with a BOM
      if (first == 0xEF && second == 0xBB && third == 0xBF) {
          return new Object [] {"UTF-8", new Integer(3)};
      }

      // default to UTF-8 if we don't have enough bytes to make a
      // good determination of the encoding
      if (count < 4) {
          return new Object [] {"UTF-8", null};
      }

      // other encodings

      if (first == 0x00 && second == 0x00 && third == 0x00 && forth == 0x3C) {
          // UCS-4, big endian (1234)
          return new Object [] {"UTF-32BE", null};
      }
      if (first == 0x3C && second == 0x00 && third == 0x00 && forth == 0x00) {
          // UCS-4, little endian (4321)
          return new Object [] {"UTF-32LE", null};
      }
      /*
       * Don't know what java encoding is this. 
      if (first == 0x00 && second == 0x00 && third == 0x3C && forth == 0x00) {
          // UCS-4, unusual octet order (2143)
          // REVISIT: What should this be?
          return new Object [] {"ISO-10646-UCS-4", null, null};
      }
      if (first == 0x00 && second == 0x3C && third == 0x00 && forth == 0x00) {
          // UCS-4, unusual octect order (3412)
          // REVISIT: What should this be?
          return new Object [] {"ISO-10646-UCS-4", null, null};
      }
      */
      if (first == 0x00 && second == 0x3C && third == 0x00 && forth == 0x3F) {
          // UTF-16, big-endian, no BOM
          return new Object [] {"UTF-16BE", null};
      }
      if (first == 0x3C && second == 0x00 && third == 0x3F && forth == 0x00) {
          // UTF-16, little-endian, no BOM
          // (or could turn out to be UCS-2...
          return new Object [] {"UTF-16LE", null};
      }
      if( first == 0x3C && second == 0x3f && third == 0x78 && forth == 0x6D){
        // ISO-8859-1
        return new Object[]{"ISO-8859-1", null};
      }

      if (first == 0x4C && second == 0x6F && third == 0xA7 && forth == 0x94) {
          // EBCDIC
          // a la xerces1, return CP037 instead of EBCDIC here
          // but according to JSP spec, it is IBM037.
          return new Object [] {"IBM037", null};
      }
      
      if (first == 0x00 && second == 0x00 && third == 0xFE && forth == 0xFF) {
          // UTF-32, big-endian, with a BOM
          return new Object [] {"UTF-32BE", new Integer(4)};
      }
      if (first == 0xFF && second == 0xFE && third == 0x00 && forth == 0x00) {
          // UTF-32, little-endian, with a BOM
          return new Object [] {"UTF-32LE", new Integer(4)};
      }


      // default encoding
      return new Object [] {"UTF-8", null, null};

  }

  private boolean checkForRoot(String root) {
    int rootInd = root.indexOf(":root");
    if (rootInd == -1) {
      return false;
    }
    
    // skip all comments before first tag. 
    boolean commentOpened = false;

    for (int i = 0; i < root.length(); i++) {
      //check for opening of a comment
      if( !commentOpened ){
        if( root.startsWith("<!--", i) ){       
          commentOpened = true;
        }
      }
      //check for end of a comment
      if( commentOpened ){
        if( root.startsWith("-->", i) ){       
          commentOpened = false;
        }
      }
      //skip xml tag and comments
      if (!commentOpened && root.charAt(i) == '<' && (root.charAt(i + 1) != '!' && root.charAt(i + 1) != '?') ) {
        rootInd = i;
        break;
      }
    }

    if (root.indexOf(":root", rootInd) == -1) {
      return false;
    }

    int rootEnd = root.indexOf('>', rootInd);
    if (rootEnd == -1) {
      return false;
    }

    Directive directive = null;
    try {
      directive = new Directive(root.substring(rootInd + 1, rootEnd));
    } catch (JspParseException e) {
      return false;
    }
    String name = directive.getName();
    if (name == null || name.indexOf(':') == -1) {
      return false;
    }
    String jspNS = (String) directive.getElements().get("xmlns:" + name.substring(0, name.indexOf(':')));
    if (jspNS != null && jspNS.equals(SAXTreeBuilder.URI_JSP)) {
      return true;
    }
    return false;
  }
  
  /**
   * Search for PAGE_ENCODING, CONTENT_TYPE in page directive or XML prolog.
   * Uses to read this String for page directive declarations.
   * @param sJsp
   * @return
   * @throws JspParseException
   */
  private String checkForEncoding(String sJsp) throws JspParseException {

    if (sJsp.indexOf(PAGE_ENCODING) == -1 && sJsp.indexOf(CONTENT_TYPE) == -1 && sJsp.indexOf(JSP_DIRECTIVE_PAGE_OPEN) == -1 &&
        sJsp.indexOf(IS_EL_IGNORED) == -1 && sJsp.indexOf(XML_BEGIN) == -1) {
      return null;
    }
    //proverka za XML
    String encodingFound = null;
    if (isXml) {
      encodingFound = checkForEncodingDirective(sJsp, JSP_DIRECTIVE_OPEN, JSP_DIRECTIVE_PAGE_CLOSE);
      checkForEncodingXMLDeclaration(sJsp);
    } else {      
      encodingFound = checkForEncodingDirective(sJsp, DIRECTIVE_OPEN, DIRECTIVE_CLOSE);
    }
    return encodingFound;
  }
  

  /**
   * Process page/tag directive for isElIgnored and encoding from pageEncoding atribute and charset from contentType attribute.
   * @param sJsp - the string in search will be performed.
   * @param directiveOpen - the opening string of the directive
   * @param directiveClose - the closing string of the directive
   * @return - the string in page encoding attrbibute. If no page encoding detected then from content type. Null if nothing is found. 
   * @throws JspParseException
   */
  private String checkForEncodingDirective(String sJsp, String directiveOpen, String directiveClose) throws JspParseException{

    String encodingFound = null;
    int pageStart = sJsp.indexOf(directiveOpen);

    while (pageStart > -1) {
      int pageEnd = sJsp.indexOf(directiveClose, pageStart);

      if (pageEnd == -1) {
        break;
      }

      String directiveString = sJsp.substring(pageStart + directiveOpen.length(), pageEnd);
      Directive directive = new Directive(directiveString);
      if (PAGE.equals(directive.getName()) || TAG.equals(directive.getName())) {
        if (isELIgnoredFromPageDirective == null) {
          isELIgnoredFromPageDirective = (String) directive.getElements().get(IS_EL_IGNORED);
        }
        String enc = (String) directive.getElements().get(PAGE_ENCODING);
        if (enc == null) {
          enc = (String) directive.getElements().get(CONTENT_TYPE);
          if (enc != null) {
            int loc = enc.indexOf(CHARSET);
            if (loc != -1) {
              encodingFound = enc.substring(loc + CHARSET.length());
              encodingFromContentTypePageDirective = encodingFound;
            }
          }
        } else {
          encodingFound = enc;
          encodingFromPageEncodingPageDirective = enc;
          break;
        }
      }
      pageStart = sJsp.indexOf(directiveOpen, pageEnd);
    }
    return encodingFound;
  
  }
  /**
   * Process XML declaration.
   * @param sJsp
   * @return - the value of the <?xml encoding="encodingName"
   * @throws JspParseException
   */
  private String checkForEncodingXMLDeclaration(String sJsp) throws JspParseException{
    String encodingFound = null;

    //XML encoding
    int pageStart = sJsp.indexOf(XML_BEGIN);

    if (pageStart > -1) {
      int pageEnd = sJsp.indexOf(XML_END, pageStart);

      if (pageEnd == -1) {
        return encodingFound;
      }

      String directiveString = sJsp.substring(pageStart + 2, pageEnd);
      Directive directive = new Directive(directiveString);

      String enc = (String) directive.getElements().get(XML_ENCODING);
      encodingXMLProlog = enc;
      encodingFound = enc;    
      pageStart = sJsp.indexOf(XML_BEGIN, pageEnd);
    }
    return encodingFound;
  }

  /**
   * indicates whether the page encoding used to read the JSP/tag file was
   * specified somehow or the default encoding was used.
   * 
   * @return
   */
  public boolean isDefaultEncodingUsed() {
    return isDefaultEncodingUsed;
  }

  /**
   * 
   * @param flag -
   *          if true, no explicitly defined encoding was used to read the
   *          JSP/tag file.
   */
  private void setIsDefaultEncodingUsed(boolean flag) {
    isDefaultEncodingUsed = flag;
  }

  public boolean isXml() {
    return isXml;
  }
  
  /**
   * Returns -1 if no BOM is found or the number of bytes the BOM is encoded with.
   * @return
   */
  public int getBomLength(){
    return bytesBom;
  }
}
