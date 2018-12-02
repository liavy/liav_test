/*
 * Copyright (c) 2000-2008 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.servlet.jsp.tagext.TagFileInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;

import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.CustomJspTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.taglib.tagfiles.TagFilesUtil;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.ImplicitTagLibDescriptor;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.TagLibDescriptor;


/*
 *
 * @author Galin Galchev,Ivo Simeonov
 * @version 4.0
 */
public class JspTaglibDirective extends JspDirective {


  /*
   * all attributes that this element can
   * accept
   */
  public static final char[][] attributeNames = {"uri".toCharArray(), "tagdir".toCharArray(), "prefix".toCharArray()};
  /*
   * all reserved values for attribute 'prefix'
   * accept
   */
  public static final char[][] reservedPrefixes = {"jsp".toCharArray(), "jspx".toCharArray(), "java".toCharArray(), "javax".toCharArray(), "servlet".toCharArray(), "sun".toCharArray(), "sunw".toCharArray()};

  /**
   * Constructs new JspTaglibDirective
   *
   */
  public JspTaglibDirective() {
    elementType = Element.TAGLIB_DIRECTIVE;
  }

  /**
   * Takes specific action corresponding to this jsp element
   * logic
   *
   * @exception   JspParseException  thrown if error occurs during
   * verification
   */
  public void action(StringBuffer buffer) throws JspParseException {
    // Table JSP 1-8
    // Packages java.lang.*, javax.servlet.*, javax.servlet.jsp.*, and
    // javax.servlet.http.* are imported implicitly by the JSP
    // container. No other packages may be part of this implicitly
    // imported list.
    if (parser.getWebContainerParameters().isExtendedJspImports()) {
      parser.getImportDirective().append("import javax.servlet.jsp.tagext.*;").append("\r\n");
    }
  }

  public void action() throws JspParseException {
    action(parser.getScriptletsCode());
  }

  /**
   * Verifies the attributes of this tag
   *
   * @exception   JspParseException  thrown if error occurs
   * during verification
   */
  public void verifyAttributes() throws JspParseException {
    if (attributes == null || attributes.length != 2) {
      throw new JspParseException(JspParseException.ELEMENT_MUST_HAVE_EXACTLY_TWO_SPECIFIED_ATTRIBUTE, new String[]{"taglib"},
              parser.currentFileName(), debugInfo.start);
    }

    Indentifier e = null;
    Indentifier value = null;
    boolean[] flags = new boolean[attributeNames.length];
    Arrays.fill(flags, true);

    for (int i = 0; i < attributes.length; i++) {
      e = attributes[i].name;
      value = attributes[i].value;

      if (e.equals(attributeNames[0])) {//uri
        if (flags[0]) {
          flags[0] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_DIRECTIVE, new Object[]{"uri", "taglib"},
                  parser.currentFileName(), debugInfo.start);
        }
      } else if (e.equals(attributeNames[1])) {//tagdir
        //this should throw exception
        //<%@ taglib prefix="t" tagdir="/WEB-INF/tagsEhoo" %>
        if ( !TagFilesUtil.isValidTagdir(value.toString()) ){
          throw new JspParseException(JspParseException.TAGDIR_MUST_START_WITH_WEBINF_TAGS,
              parser.currentFileName(), debugInfo.start);
        }
        if (flags[1]) {
          flags[1] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_DIRECTIVE, new Object[]{"tagdir", "taglib"},
                  parser.currentFileName(), debugInfo.start);
        }
      } else if (e.equals(attributeNames[2])) {//prefix
        if (flags[2]) {
          flags[2] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_DIRECTIVE, new Object[]{"prefix", "taglib"},
                  parser.currentFileName(), debugInfo.start);
        }

        for (int j = 0; j < reservedPrefixes.length; j++) {
          if (value.equals(reservedPrefixes[j])) {
            throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_DIRECTIVE_VALUE_IS_RESERVED, new Object[]{value.toString(), "prefix", "taglib"},
                    parser.currentFileName(), debugInfo.start);
          }
        }

        //ok
      } else {
        throw new JspParseException(JspParseException.UNRECOGNIZED_ATTRIBUTE_IN_DIRECTIVE, new Object[]{value.toString(), "taglib"},
                parser.currentFileName(), debugInfo.start);
      }
    }
    if (!flags[0] && !flags[1]) {
      throw new JspParseException(JspParseException.ATTRIBUTE_AND_ATTRIBUTE_CANNOT_BE_SPECIFED_TOGETHER_IN_ACTION, new Object[]{"uri", "tagdir", "taglib"},
              parser.currentFileName(), debugInfo.start);
    }
    String uri = getAttributeValue("uri");
    String tagDir = getAttributeValue("tagdir");
    String prefix = getAttributeValue("prefix");
    registerTaglib(uri, tagDir, prefix, parser);
  }

  /**
   * JSP.6.3 Syntactic Elements in JSP Documents
   * JSP.6.3.1 Namespaces, Standard Actions, and Tag Libraries
	 * In contrast to Section JSP.7.3.6.2, however, a translation error must not be
	 * generated if the given uri is not found in the taglib map. Instead, any actions in the
	 * namespace defined by the uri value must be treated as uninterpreted.
	 * In JSP documents if URI is not found, no exception will be thrown.
   * @param uri
   * @param tagDir
   * @param prefix
   * @param parser
   * @throws JspParseException
   */
  public void registerTaglib(String uri, String tagDir, String prefix, JspPageInterface parser) throws JspParseException {
    TagLibDescriptor tagD = null;
    TagInfo[] tags = null;

    //check if prefix is available
    if( prefix == null ){
      throw new JspParseException(JspParseException.MISSING_ATTRIBUTE_IN_DIRECTIVE, new Object[]{"prefix","taglib"});
    }
    // when prefix is empty string => we have default namespace -> xmlns="http://url"
    if( !parser.isXml() ) {
      // if this is not JSP document then no duplicated prefixes are allowed
      // for JSP documents it is legal to have duplicated prefixes:
      // <x:out xmlns:x="URL1">
      //  <x:out xmlns:x="URL2"/>
      // </x:out>
      //check if prefix is already defined
      TagLibraryInfo definedTaglib = (TagLibraryInfo) parser.getTaglibs().get(prefix);
      if(  definedTaglib != null &&
         (!definedTaglib.getURI().equals(uri) && !definedTaglib.getURI().equals(tagDir))
        ){
        throw new JspParseException(JspParseException.PREFIX_ALREADY_DEFINED, new Object[]{prefix,definedTaglib.getURI()});
      }
    }

    //check if prefix is already used before declaring taglib
    if( parser.getRootElement().isAlreadyUsedCustomTag(prefix) ) {
      throw new JspParseException(JspParseException.PREFIX_ALREADY_USED, new Object[]{prefix});
    }

    if (uri != null) {
      Object obj = parser.getTagLibDescriptors().get(uri);
      if (obj != null && obj instanceof Throwable) {
        throw new JspParseException(JspParseException.RETHROW_EXCEPTION,new Object[]{((Throwable)obj).getLocalizedMessage()}, ((Throwable)obj));
      }
      tagD = (TagLibDescriptor)obj;
      tags = null;
      boolean alreadyParsed = tagD != null;
      if (tagD != null) {
        tags = tagD.getTags();
      } else {

        TagLibDescriptor tagLibDoc = new TagLibDescriptor(getClassLoader(parser));
        try {
          String _uri = null;
          /* if the URI is not a URL (i.e. does not have a protocol and host
            then it is used as s resource path
          */
          if (uri.indexOf("://") < 0) {
            if( uri.indexOf("WEB-INF/tags") >= 0 ){
              // TLD files should not be placed in /WEB-INF/classes or /WEB-INF/lib, and must not be
              // placed inside /WEB-INF/tags or a subdirectory of it
              throw new JspParseException(JspParseException.ERROR_IN_PARSING_TAGLIB_TAG_IN_JSP_CANNOT_RESOLVE_URI, new Object[]{uri});
            }
            _uri = getRealUri(uri, parser);
          } else {
            //see java doc of the method
            if ( !parser.isXml() ){
              throw new JspParseException(JspParseException.ERROR_IN_PARSING_TAGLIB_TAG_IN_JSP_CANNOT_RESOLVE_URI, new Object[]{_uri == null ? uri : _uri});
            }else{
              if (LogContext.getLocationJspParser().beError()) {
								// not an error according to JSP specification, but probably not desired behavior
              	String tagDirStr = (tagDir == null) ? "null" : tagDir;
								LogContext.getLocation(LogContext.LOCATION_JSP_PARSER).traceError("ASJ.web.000590",
										"TLD not found:URI [{0}], prefix [{1}], tagdir[{2}]", new Object[] {uri, prefix, tagDirStr}, null, null);
								LogContext.getLocationJspParser().errorT(LogContext.getExceptionStackTrace(new Exception("TLD not found in JSP document.")));
							}
							return;
            }
          }

          // JSP 1.1 spec defines tag libraries inside jar files, with
          // the TLD fixed to META-INF/taglib.tld
          // should be supported as well
          if (_uri.toLowerCase().endsWith(".jar")) {
            JarFile jar = new JarFile(_uri);
            try {
            ZipEntry entry = jar.getEntry("META-INF/taglib.tld");
            if (entry == null) {
              throw new JspParseException(JspParseException.ERROR_IN_PARSING_TAGLIB_TAG_IN_JSP_CANNOT_RESOLVE_URI_JSP11, new Object[]{_uri});
            }
            tagLibDoc.loadDescriptorFromStream(jar.getInputStream(entry));
            } finally {
              jar.close();
            }
          } else {
		        tagLibDoc.loadDescriptorFromFile(_uri);
          }
          tagLibDoc.setURI(uri);
          tagLibDoc.setPrefixString(prefix);
          tagD = tagLibDoc;
          parser.getTagLibDescriptors().put(uri, tagD);
          tags = tagD.getTags();
        } catch (OutOfMemoryError ex) {
          throw ex;
        } catch (ThreadDeath ex) {
          throw ex;
        } catch (JspParseException ex) {
          throw ex;
        } catch (FileNotFoundException ex){
          //see javadoc of this method
          if ( !parser.isXml() ){
            throw new JspParseException(JspParseException.ERROR_IN_PARSING_TAGLIB_TAG_IN_JSP_CANNOT_RESOLVE_URI, new Object[]{uri}, ex);
          }else{
            if (LogContext.getLocationJspParser().beError()) {
							// not an error according to JSP specification, but probably not desired behavior
              LogContext.getLocation(LogContext.LOCATION_JSP_PARSER).traceError("ASJ.web.000604",
                "TLD not found: URI [{0}], prefix [{1}], tagdir[{2}]", new String[] {uri, prefix, tagDir}, null, null);
							LogContext.getLocationJspParser().errorT(LogContext.getExceptionStackTrace(ex));
						}
						return;
          }
        } catch (Throwable ex) {
          throw new JspParseException(JspParseException.ERROR_IN_PARSING_TAGLIB_TAG_IN_JSP_CANNOT_RESOLVE_URI, new Object[]{uri}, ex);
        }
      }

      // concatenate tags from the TLD with tags from tagfiles
      TagFileInfo[] tagFiles = tagD.getTagFiles();
      if (tagFiles.length > 0) {
        int tagsLength = 0;
        TagInfo[] tags_ = null;
        if (tags != null && tags.length > 0) {
          tagsLength = tags.length;
          tags_ = new TagInfo[tagsLength + tagFiles.length];
          System.arraycopy(tags, 0, tags_, 0, tagsLength);
        } else {
          tags_ = new TagInfo[tagFiles.length];
        }
        for (int i = 0; i < tagFiles.length; i++) {
          // parse declared tag file in the TLD - if there is no TagInfo, or it is declared with uri=<path to tld> and is not up to date
          if (tagFiles[i].getTagInfo() == null || (!alreadyParsed && !TagFilesUtil.isTagClassFileUpToDate(tagFiles[i], parser.getParserParameters().getApplicationRootDir()))) {
            try {
              tagFiles[i] = TagFilesUtil.parseTagFile(uri, tagFiles[i], (TagLibDescriptor)tagD, parser);
            } catch (OutOfMemoryError ex) {
              throw ex;
            } catch (ThreadDeath ex) {
              throw ex;
            } catch (JspParseException ex) {
              throw ex;
            } catch (Throwable t) {
              throw new JspParseException(JspParseException.ERROR_PARSING_TAGFILE, new Object[]{tagFiles[i].getPath()}, t);
            }
          }
          /*
           * JSP.8.4.3 Packaging Directly in a Web Application
           * If a directory contains two files with the same tag name (e.g. a.tag and a.tagx),
           * it is considered to be the same as having a TLD file with two <tag> elements
           * whose <name> sub-elements are identical. The tag library is therefore considered
           * invalid.
           */
          for (int k = 0; k < i; k++) {
            TagFileInfo alreadyAdded = tagFiles[k];
            if (alreadyAdded.getName().equals(tagFiles[i].getName())) {
              throw new JspParseException(JspParseException.TAG_FILE_HAS_DUPLICATE_NAME, new Object[]{tagFiles[i].getPath(), tagD.getURI()});
            }
          }
          tags_[tagsLength + i] = tagFiles[i].getTagInfo();
        }
        tags = tags_;
      }
      tagD.setURI(uri);
      tagD.setPrefixString(prefix);
      parser.getTaglibs().put(prefix, tagD.clone());

    } else {
      if (tagDir != null) {
        // implicti.tld
        ImplicitTagLibDescriptor implicitTLD = null;
        if (parser.isTagFile() && parser.getTagFileTLD() != null && parser.getTagFileTLD().getShortName().equals(tagDir)) {
          // the same implicit tld - reuse it.
          implicitTLD = (ImplicitTagLibDescriptor) parser.getTagFileTLD();
        } else {
          implicitTLD = TagFilesUtil.getImplicitTLDData(tagDir, parser);
        }

        try {
          // get tags from tagfiles in tagDir
          Object obj = parser.getTagLibDescriptors().get(tagDir);
          if (obj != null && obj instanceof Throwable) {
            throw new JspParseException(JspParseException.RETHROW_EXCEPTION, new Object[]{((Throwable) obj).getLocalizedMessage()}, ((Throwable) obj));
          }
          tagD = (TagLibDescriptor) obj;
          tags = null;
          boolean needParse = true;
          if (tagD == null) {
            if (parser.isTagFile()) {
              tagD = TagFilesUtil.createTLDforTagDir(tagDir, prefix, parser);
              if (implicitTLD != null) {
                tagD.setRequiredVersion(implicitTLD.getRequiredVersion());
              }
              return;
            } else {
              tagD = TagFilesUtil.parseTagFiles(tagDir, prefix, parser, implicitTLD);
            }
            needParse = false;
          }
          TagFileInfo[] tagFiles = tagD.getTagFiles();
//            if (needParse) {
//              for (int i = 0; i < tagFiles.length; i++) {
//                if (!TagFilesUtil.isTagClassFileUpToDate(tagFiles[i], parser.getParserParameters().getApplicationRootDir())) {
//                  tagD = TagFilesUtil.parseTagFiles(tagDir, parser);
//                  tagFiles = tagD.getTagFiles();
//                  break;
//                }
//              }
//            }
          tags = new TagInfo[tagFiles.length];
          for (int i = 0; i < tags.length; i++) {
            tags[i] = tagFiles[i].getTagInfo();
          }

          tagD.setURI(tagDir);
          tagD.setPrefixString(prefix);
          parser.getTaglibs().put(prefix, tagD.clone());
          if (implicitTLD != null) {
            // A JSP 2.1 container must consider only the JSP version and tlib-version specified by an implicit.tld file, and ignore its
            // short-name element.
            // no one can possibly use this.
            //tagD.getTagLibrary().setTlibVersion(implicitTLD.getTagLibrary().getTlibVersion());
            tagD.setRequiredVersion(implicitTLD.getRequiredVersion());
          }
        } catch (OutOfMemoryError ex) {
          throw ex;
        } catch (ThreadDeath ex) {
          throw ex;
        } catch (IOException io) {
          if (io instanceof FileNotFoundException) {
            Object[] params = {uri};
            throw new JspParseException(JspParseException.TLD_NOT_FOUND, params);
          } else {
            //do the same as in Throwable
            throw new JspParseException(JspParseException.ERROR_IN_PARSING_TAGLIB_TAG_IN_WEBXML_OR_TLD_FILE_OF_THE_TAGLIB_LIBRARY, io);
          }
        } catch (Throwable ex) {
          throw new JspParseException(JspParseException.ERROR_IN_PARSING_TAGLIB_TAG_IN_JSP_CANNOT_RESOLVE_TAGDIR, new Object[]{tagDir}, ex);
        }
        //}
      } else {
        throw new JspParseException(JspParseException.NEITHER_URI_NOR_TAGDIR_PRESENT);
      }
    }

    if (tags != null && tags.length > 0) {
      CustomJspTag cjt;
      String startIndend;
      String endIndend;

      if( !prefix.equals("") ) { // default namespace -> xmlns="http://url"
        prefix += ":";
      }
      for (int i = 0; i < tags.length; i++) {
        startIndend = "<" + prefix + tags[i].getTagName();
        endIndend = "</" + prefix + tags[i].getTagName() + ">";
        cjt = new CustomJspTag(startIndend.toCharArray(), endIndend.toCharArray());
        cjt.setCopy(true);
        parser.getRootElement().registerParsingElement(cjt);
      }
      parser.getRootElement().registerTagLibPrefix("<" + prefix );
    }
  }

  public String getString(IDCounter id) throws JspParseException {
    String prefix =  getAttributeValue("prefix");
    if( !prefix.trim().equals("") ) { // in case when NOT default namespace
      prefix = ":" + prefix;
    }
    return "xmlns" + prefix + "=\"" + getAttributeValue("uri") + "\"\n\t";
  }


  /**
   * The directive name denoted by "directive" in the following syntax - <%@ directive { attr='value' }* %>
   * @return - String with one of the directive names - "page", "include", "taglib", "tag", "attribute", "variable" or some custom type
   */
  public String getDirectiveName(){
    return DIRECTIVE_NAME_TAGLIB;
  }

  /**
   * Subclass this method if you want to perform some validation of the usage of this directive.
   * Note the tree of element is still not created.
   * @param parser
   * @throws JspParseException - if for example page directive is used in tag file or tag directive in JSP page
   */
  public void verifyDirective(JspPageInterface parser) throws JspParseException{
    //do nothing - taglib directive can be used both in tag files and JSP pages
  }

  /**
   * Returns the real path to TLD file.
   * This method is overridden by portal parser in order to provide real path to the portal application containing this TLD.
   * @param uri
   * @param parser
   * @return
   */
  protected String getRealUri(String uri, JspPageInterface parser) {
    // absolute URIs start from application root
    // relative URIs start from the JSP directory
    if (uri.charAt(0) == '/') {
      return parser.getParserParameters().getApplicationRootDir() + uri;
    } else {
      return parser.getPath() + File.separatorChar + uri;
    }
  }

  /**
   * Returns the application classloader.
   * This method is overridden by portal parser in order to provide the loader of portal application
   * @param parser
   * @return
   */
  protected ClassLoader getClassLoader(JspPageInterface parser) {
    return parser.getApplicationClassLoader();
  }
}

