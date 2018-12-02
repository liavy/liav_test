/*
 * Copyright (c) 2000-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.taglib.tagfiles;

import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspTaglibDirective;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.CustomJspTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.TagLibDescriptor;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.TagInfoImpl;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.ImplicitTagLibDescriptor;
import com.sap.engine.services.servlets_jsp.server.jsp.exceptions.CompilingException;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.tc.logging.Location;

import javax.servlet.jsp.tagext.TagFileInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.TagInfo;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.jar.JarFile;

/**
 *  Utility class containig some static method used in tag files parsing
 */
public class TagFilesUtil {
  
  public static final String WEB_INF_TAGS = "/WEB-INF/tags";
  public static final String META_INF_TAGS = "/META-INF/tags/";
  
  /**
   * Checks if a tag file changed
   * @param tagFileInfo
   * @param appRootDir
   * @return true if it is up to date, false if it is changed
   */
  public static boolean isTagClassFileUpToDate(TagFileInfo tagFileInfo, String appRootDir) {
    long timeLastModified = (new File(appRootDir + tagFileInfo.getPath())).lastModified();
    String classFile = tagFileInfo.getTagInfo().getTagClassName();
    if (classFile == null) {
      return false;
    }
    int lastUnderscore = classFile.lastIndexOf('_');
    if (lastUnderscore == -1) {
      return false;
    }
    long time = 0;
    try {
      time = Long.parseLong(classFile.substring(lastUnderscore + 1));
    } catch (NumberFormatException e) {
      time = 0;
    }
    return time >= timeLastModified;
  }

  /**
   *  Parse tag files in given directory
   * @param tagDir
   * @param parser
   * @return  TagLibraryInfo containing all tag files
   * @throws JspParseException
   * @throws IOException
   */
  public static TagLibDescriptor parseTagFiles(String tagDir, String prefix, JspPageInterface parser, TagLibDescriptor tagFileTLD) throws JspParseException, IOException {

    TagFileParser tagfileParser = new TagFileParser(parser.getApplicationClassLoader(), parser.getParserParameters(), parser.getWebContainerParameters());
    tagfileParser.setTagFileTLD(tagFileTLD);
    // tagDir is guaranteed to start with /WEB-INF/tags
    // remove the loading '/', since the root directory ends with '/'
    TagLibDescriptor tagD = tagfileParser.parseTagFiles(parser.getParserParameters().getApplicationRootDir() + tagDir.substring(1));
    if (tagD == null) {
      throw new JspParseException(JspParseException.ERROR_IN_PARSING_TAGLIB_TAG_IN_JSP_CANNOT_RESOLVE_TAGDIR, new Object[]{tagDir});
    }

    parser.getTagLibDescriptors().put(tagDir, tagD);

    TagFileInfo[] tagFiles = tagD.getTagFiles();
    String[] newTags = new String[tagFiles.length];
    for (int i = 0; i < tagFiles.length; i++) {
      newTags[i] = parser.getParserParameters().getApplicationRootDir() + tagFiles[i].getPath();
    }
    String[] included = (String[])parser.getParserParameters().getIncludedFilesHashtable().get(parser.currentFileName());
    if (included != null) {
      Vector v = new Vector();
      for (int i = 0; i < newTags.length; i++) {
        boolean found = false;
        for (int j = 0; j < included.length; j++) {
          if (included[j].equals(newTags[i])) {  //already exist
            found = true;
            break;
          }
        }
        if (!found) {
          v.add(newTags[i]);
        }
      }
      if (v.size() > 0) {
        Object[] tempArr = v.toArray();
        String[] temp = new String[included.length + tempArr.length];
        System.arraycopy(included, 0, temp, 0, included.length);
        System.arraycopy(tempArr, 0, temp, included.length, tempArr.length);
        parser.getParserParameters().getIncludedFilesHashtable().put(parser.currentFileName(), temp);
      }
    } else {
      parser.getParserParameters().getIncludedFilesHashtable().put(parser.currentFileName(), newTags);
    }
    return tagD;
  }

  /**
   * Parses given tag file, associated with given  TagLibDescriptor
   * @param uri
   * @param tagFileInfo
   * @param tagLibDesc
   * @param parser
   * @return
   * @throws JspParseException
   * @throws IOException
   * @throws CompilingException
   */
  public static TagFileInfo parseTagFile(String uri, TagFileInfo tagFileInfo, TagLibDescriptor tagLibDesc, JspPageInterface parser) throws JspParseException, IOException, CompilingException {

    TagFileParser tagfileParser = new TagFileParser(parser.getApplicationClassLoader(), parser.getParserParameters(), parser.getWebContainerParameters());
    tagfileParser.setTagFileTLD(parser.getTagFileTLD());
    String filename = null;
    //check for tag file in jar file
    if (uri.toLowerCase().endsWith(".jar")) {
      filename = parser.getParserParameters().getApplicationRootDir() + uri;
    } else {
      filename = parser.getParserParameters().getApplicationRootDir() + tagFileInfo.getPath();
    }

    //check if exists
    File file = new File(filename);
    if (!file.exists()) {
      throw new JspParseException(JspParseException.TAG_FILE_NOT_FOUND, new Object[]{tagFileInfo.getPath(), tagLibDesc.getURI()});
    }

    TagFileInfo result = null;
    if (tagFileInfo.getPath().startsWith(META_INF_TAGS)) {//if starts with /META-INF/ then this tag is in JAR
      JarFile jarFile = new JarFile(file);
      try {
        String entryName = tagFileInfo.getPath().charAt(0) == '/' ? tagFileInfo.getPath().substring(1) : tagFileInfo.getPath();
        ZipEntry entry = jarFile.getEntry(entryName);
        if (entry == null) {
          throw new JspParseException(JspParseException.TAG_FILE_NOT_FOUND, new Object[]{tagFileInfo.getPath(), tagLibDesc.getURI()});
        }
        String jarName = jarFile.getName().replace(File.separatorChar, ParseUtils.separatorChar);
        result = tagfileParser.parseTagFile(jarName, tagFileInfo.getName(), tagFileInfo.getPath(), jarFile.getInputStream(entry), entry.getTime(), parser.getParserParameters().getJspConfigurationProperties(), tagLibDesc);
      } finally {
        jarFile.close();
      }
    } else if (tagFileInfo.getPath().startsWith(WEB_INF_TAGS)) {
      result = tagfileParser.parseTagFile(filename, tagFileInfo.getName(), tagFileInfo.getPath(), file.lastModified(), parser.getParserParameters().getJspConfigurationProperties(), tagLibDesc);
    } else {
      throw new JspParseException(JspParseException.INVALID_TAG_FILE_PATH, new Object[]{tagFileInfo.getPath(), tagLibDesc.getURI(), WEB_INF_TAGS + " or " + META_INF_TAGS});
    }
    String[] included = (String[])parser.getParserParameters().getIncludedFilesHashtable().get(parser.currentFileName());
    if (included != null) {
      for (int j = 0; j < included.length; j++) {
        if (included[j].equals(filename)) {  //already exist
          String[] temp = new String[included.length + 1];
          System.arraycopy(included, 0, temp, 0, included.length);
          temp[temp.length - 1] = filename;
          parser.getParserParameters().getIncludedFilesHashtable().put(parser.currentFileName(), temp);
          break;
        }
      }
    } else {
      parser.getParserParameters().getIncludedFilesHashtable().put(parser.currentFileName(), new String[]{filename});
    }
    return result;
  }

  /**
   * Helper method that validates tagdir attribute of taglib directive.
   * Path tag in TLD always must start with /META-INF/tags/, because always points to a tag file. That's why path is excluded in this verification.
   * <p>
   * From JSP2.1 - Table JSP.1-9<p>
   * tagdir - Indicates this prefix is to be used to identify tag extensions
   * installed in the /WEB-INF/tags/ directory or a subdirectory.
   * An implicit tag library descriptor is used (see Section JSP.8.4, "Packaging Tag Files" for details). A
   * translation error must occur if the value does not start with
   * WEB-INF/tags. A translation error must occur if the value
   * does not point to a directory that exists. A translation error
   * must occur if used in conjunction with the uri attribute.
   * <p><p>
   * Checks if the tagdir value starts with "/WEB-INF/tags".
   * Check also if value is not exactly "/WEB-INF/tags" whether the next char is "/".
   * Note that the file separators are defined in the spec as  "/" not as File.separator
   * @param tagdir - tagdir value or path value
   * @return - true if this is valid value, false - otherwise
   */
  public static boolean isValidTagdir(String tagdir){
    boolean result = true;
    if( tagdir == null ){
      return false;
    }
    if( !tagdir.startsWith(WEB_INF_TAGS)){
      result = false;
    }else {
      if(!tagdir.equals(WEB_INF_TAGS) ){// check if it is not /WEB-INF/tags
        result = tagdir.substring(WEB_INF_TAGS.length()).startsWith("/");// then the next char is "/"
      }
    }
    return result;
  }

  /**
   * Creates a TLD with an "empty" TagFileInfo for currently parsed tag file.It will be used when one tag-file uses another.
   * @param tagDir  The tagdir parameter of jsp:taglib directive
   * @param parser  Curren parser
   * @return  TagLibDescriptor
   */
  public static TagLibDescriptor createTLDforTagFile(String tagDir, JspPageInterface parser) {
    String tagPath = parser.currentFileName().substring(parser.getParserParameters().getApplicationRootDir().length() - 1).replace('\\', ParseUtils.separatorChar);
    String tagName = tagPath.substring(tagPath.lastIndexOf(ParseUtils.separatorChar) + 1, tagPath.lastIndexOf(".tag"));
    TagFileInfo currentTagFileInfo = null;
    if (parser.getParserParameters().getTagCompilerParams().getTagFiles().containsKey(tagPath)) {
      currentTagFileInfo = (TagFileInfo) parser.getParserParameters().getTagCompilerParams().getTagFiles().get(tagPath);
    } else {
      TagInfoImpl ti = new TagInfoImpl();
      ti.setTagName(tagName);
      currentTagFileInfo = new TagFileInfo(tagName, tagPath, ti.getTagInfo());
    }
    Vector vTagFile = new Vector();
    vTagFile.add(currentTagFileInfo);
    return new TagLibDescriptor(tagDir, vTagFile, parser.getApplicationClassLoader());
  }

  /**
   * Creates a TLD with some "empty" TagFileInfo-s for currently parsed tagdir.It will be used when one tag-file uses another.
   * @param tagDir  The tagdir parameter of jsp:taglib directive
   * @param prefix  The prefix parameter of jsp:taglib directive
   * @param parser Curren parser
   * @return  TagLibDescriptor
   * @throws JspParseException
   * @throws IOException
   */
  public static TagLibDescriptor createTLDforTagDir(String tagDir, String prefix, JspPageInterface parser) throws JspParseException, IOException {
    if (!TagFilesUtil.isValidTagdir(tagDir)) {
      throw new JspParseException(JspParseException.TAGDIR_MUST_START_WITH_WEBINF_TAGS, parser.currentFileName(), parser.currentDebugPos());
    }
    File dir = new File(parser.getParserParameters().getApplicationRootDir() + tagDir.substring(1));
    if (dir.exists() && dir.isDirectory()) {
      File[] tagFiles = dir.listFiles();
      Vector vTagFiles = new Vector();
      for (int i = 0; i < tagFiles.length; i++) {
        if (!tagFiles[i].isDirectory() && (tagFiles[i].getName().toLowerCase().endsWith(".tag") || tagFiles[i].getName().toLowerCase().endsWith(".tagx"))) {
          String tagFile = tagFiles[i].getCanonicalPath().replace(File.separatorChar, ParseUtils.separatorChar);
          String tagPath = tagFile.substring(parser.getParserParameters().getApplicationRootDir().length() - 1);
          String tagName = tagPath.substring(tagPath.lastIndexOf(ParseUtils.separatorChar) + 1, tagPath.lastIndexOf(".tag"));
          TagFileInfo currentTagFileInfo = null;
          if (parser.getParserParameters().getTagCompilerParams().getTagFiles().containsKey(tagPath)) {
            currentTagFileInfo = (TagFileInfo) parser.getParserParameters().getTagCompilerParams().getTagFiles().get(tagPath);
          } else {
            TagInfoImpl ti = new TagInfoImpl();
            ti.setTagName(tagName);
            currentTagFileInfo = new TagFileInfo(tagName, tagPath, ti.getTagInfo());
          }

          CustomJspTag cjt = new CustomJspTag(("<" + prefix + ":" + tagName).toCharArray(), ("</" + prefix + ":" + tagName + ">").toCharArray());
          cjt.setCopy(true);
          parser.getRootElement().registerParsingElement(cjt);

          vTagFiles.add(currentTagFileInfo);
        }
      }
      if (vTagFiles.size() > 0) {
        parser.getRootElement().registerTagLibPrefix("<" + prefix + ":");
        TagLibDescriptor tagD = new TagLibDescriptor(tagDir, vTagFiles, parser.getApplicationClassLoader());
        ((TagLibDescriptor) tagD).setURI(tagDir);
        ((TagLibDescriptor) tagD).setPrefixString(prefix);
        parser.getTaglibs().put(prefix, tagD);
        return tagD;
      } else {
        return createTLDforTagFile(tagDir, parser);
      }
    } else {
      return createTLDforTagFile(tagDir, parser);
    }
  }

  /**
   * Invoked when tagdir attrbiute in taglib directive is encoutered
   * and  implicit, created by the container TLD for this tag dir should be created.
   * @param tagDir - the directory in which implicit.tld will be searched
   * @param parser - The parser for the given taglib directive.
   * @return null or ImplicitTagLibDescriptor
   * @throws JspParseException - if implicit.tld is not valid or for some parsing problems of the TLD as XML appeared.
   */
  public static ImplicitTagLibDescriptor getImplicitTLDData(String tagDir, JspPageInterface parser) throws JspParseException {
    String implicitTldPath = null;
    if (tagDir.endsWith(ParseUtils.separator)) {
      implicitTldPath = parser.getParserParameters().getApplicationRootDir() + tagDir.substring(1) + "implicit.tld";
    } else {
      implicitTldPath = parser.getParserParameters().getApplicationRootDir() + tagDir.substring(1) + ParseUtils.separator + "implicit.tld";
    }
    ImplicitTagLibDescriptor implicitTLD = new ImplicitTagLibDescriptor(parser.getApplicationClassLoader());
    if (new File(implicitTldPath).exists()) {

      try {
        implicitTLD.loadDescriptorFromFile(implicitTldPath);
      } catch (Exception e) {
          throw new JspParseException(JspParseException.ERROR_PARSING_IMPLICIT_TLD, new Object[]{implicitTldPath, tagDir}, e);
      }
      implicitTLD.validate();
    }
    implicitTLD.setShortName(tagDir);
    return implicitTLD;
  }

  /**
   *  Checks if the tag with given name is declared as "tagdependent"
   * @param prefix tag's prefix
   * @param shortName the name of the tag
   * @param parser  current parser
   * @return true if the tag body content is "tagdependent", otherwise - false
   */
  public static boolean checkTagDependentBody(String prefix, String shortName, JspPageInterface parser) {

    TagLibraryInfo tagLibraryInfo = (TagLibraryInfo) parser.getTaglibs().get(prefix);
    TagInfo tagInfo = null;

    if (tagLibraryInfo == null) {
      return false; //not custom tag
    }

    tagInfo = tagLibraryInfo.getTag(shortName);
    TagFileInfo tagFileInfo = tagLibraryInfo.getTagFile(shortName);

    if (tagInfo == null && tagFileInfo == null) {
      return false;
    }

    if (tagFileInfo != null) {
      tagInfo = tagFileInfo.getTagInfo();
    }
    if (tagInfo.getBodyContent() != null && tagInfo.getBodyContent().equalsIgnoreCase(TagInfo.BODY_CONTENT_TAG_DEPENDENT)) {
      return true;
    }
    return false;
  }
}
