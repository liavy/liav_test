/*
 * Copyright (c) 2004-2009 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.deploy.descriptor;

import com.sap.engine.lib.descriptors5.webjsptld.FunctionType;
import com.sap.engine.lib.descriptors5.webjsptld.TagFileType;
import com.sap.engine.lib.descriptors5.webjsptld.TagType;
import com.sap.engine.lib.descriptors5.webjsptld.TldAttributeType;
import com.sap.engine.lib.descriptors5.webjsptld.TldTaglibType;
import com.sap.engine.lib.descriptors5.webjsptld.VariableType;
import com.sap.engine.lib.processor.SchemaProcessor;
import com.sap.engine.lib.util.ConcurrentHashMapObjectObject;
import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.deploy.WebContainer;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebWrongDescriptorException;

import javax.servlet.jsp.tagext.FunctionInfo;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.TagFileInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.TagVariableInfo;
import javax.servlet.jsp.tagext.VariableInfo;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Vector;

/**
 * This class encapsulates the tag library descriptor
 * and some utility methods for working with it
 *
 * @author Georgi Gerginov
 * @version 1.0
 */
public class TagLibDescriptor extends TagLibraryInfo {

  // JSP.8.4.3 <tlib-version> for the tag library defaults to 1.0
  static final String TAG_LIB_VERSION = "1.0";
  // JSP.8.4.3 The JSP version of an implicit tag library defaults to 2.0.
  static final String JSP_VERSION_20 = "2.0";

  public static final String JSP_VERSION_21 = "2.1";
  public static final double JSP_VERSION_20_DOUBLE = 2.0;
  public static final double JSP_VERSION_21_DOUBLE = 2.1;

  private TldTaglibType tagLibrary;
  private ClassLoader appLoader = null;
  private ConcurrentHashMapObjectObject exceptionsTable = null;
  private TagLibraryInfo[] tagLibraryInfos = null;

  /**
   * Constructor
   */
  public TagLibDescriptor(ClassLoader appLoader) {
    super("", "");
    this.tagLibrary = new TldTaglibType();
    this.appLoader = appLoader;
    exceptionsTable = new ConcurrentHashMapObjectObject();
    // needed by the implicit.tld where the TLD is constructed but not read from XML file.
    setRequiredVersion(JSP_VERSION_20);
  }

  public TagLibDescriptor(String shortName, Vector tagFileInfos, ClassLoader appLoader) {
    this(appLoader);
    this.shortname = shortName;
    this.tagFiles = (TagFileInfo[]) tagFileInfos.toArray(new TagFileInfo[0]);
    for (int i = 0; i < tagFiles.length; i++) {
      tagFiles[i].getTagInfo().setTagLibrary(this);
    }
  }

  /**
   * This method loads the descriptor from the given input stream.
   *
   * @param stream
   * @throws Exception
   */
  public void loadDescriptorFromStream(InputStream stream) throws Exception {
    //validates the xml file
    SchemaProcessor webJspTldSchemaProcessor = ServiceContext.getServiceContext().getDeployContext().getWebJspTldSchemaProcessor();
    synchronized (webJspTldSchemaProcessor) {
      long start = -1;
      String tagName = "TagLibDescriptor.loadDescriptorFromStream";
      TldTaglibType taglibType = null;
      try {
        if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
          Accounting.beginMeasure(tagName, SchemaProcessor.class);
          start = System.currentTimeMillis();
        }//ACCOUNTING.start - END
        taglibType = (TldTaglibType) webJspTldSchemaProcessor.parse(stream);
      } finally {
        if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
          Accounting.endMeasure(tagName);
          start = System.currentTimeMillis() - start;
          WebContainer.parseTLD.addAndGet(start);
        }//ACCOUNTING.end - END
      }
      
      setTagLibrary(taglibType);
    }
  }//end of loadDescriptorFromStream(InputStream stream, boolean validation, boolean convert)

  /**
   * This method loads the descriptor from the given taglib descriptor file.
   *
   * @param fileName
   * @throws Exception
   */
  public void loadDescriptorFromFile(String fileName) throws Exception {
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(fileName);
      loadDescriptorFromStream(fis);
    } finally {
      if (fis != null) {
        fis.close();
      }
    }
  }//end of loadDescriptorFromFile(String fileName, boolean validation, boolean convert)

  /**
   * Gets tag library descriptor
   *
   * @return TldTaglibType
   */
  public TldTaglibType getTagLibrary() {
    return tagLibrary;
  }

  /**
   * Sets tag library descriptor
   *
   * @param type TldTaglibType
   */
  public void setTagLibrary(TldTaglibType type) throws WebWrongDescriptorException {
    if (type == null) {
      return;
    }
    tagLibrary = type;
    fillInfoMembers();
  }

  /**
   * This method fills the extended TagLibraryInfo class members
   * from the given TldTaglibType descriptor
   */
  private void fillInfoMembers() throws WebWrongDescriptorException {
    prefix = "";
    urn = "";

    if (tagLibrary.getUri() != null) {
      uri = tagLibrary.getUri().get_value().toString();
      urn = uri;
    } else {
      uri = null;
    }

    if (tagLibrary.getTlibVersion() != null) {
      tlibversion = tagLibrary.getTlibVersion().toString();
    } else {
      tlibversion = TAG_LIB_VERSION;
    }

    if (tagLibrary.getVersion() != null) {
      jspversion = tagLibrary.getVersion().toString();
    } else {
      jspversion = JSP_VERSION_20;
    }

    if (tagLibrary.getShortName() != null) {
      shortname = tagLibrary.getShortName().get_value();
    } else {
      shortname = null;
    }

    if (tagLibrary.getDescription() != null && tagLibrary.getDescription().length > 0) {
      info = tagLibrary.getDescription()[0].get_value();
    } else {
      info = "";
    }

    fillTagInfo();
    fillTagFileInfo();
    fillFunctionInfo();
  }

  /**
   * This method overwrites that from the extended class
   * in order to prevent System.err print-outs
   */
  public FunctionInfo getFunction(String name) {
    if (functions == null || functions.length == 0) {
      return null;
    }
    for (int i = 0; i < functions.length; i++) {
      if (functions[i].getName().equals(name)) {
        return functions[i];
      }
    }
    return null;
  }

  /**
   * Convert a String value to 'boolean'.
   * Besides the standard conversions done by
   * Boolean.valueOf(s).booleanValue(), the value "yes"
   * (ignore case) is also converted to 'true'.
   * If 's' is null, then 'false' is returned.
   *
   * @param s the string to be converted
   * @return the boolean value associated with the string s
   */
  public static boolean booleanValue(String s) {
    boolean b = false;
    if (s != null) {
      if (s.equalsIgnoreCase("yes")) {
        b = true;
      } else {
        b = Boolean.valueOf(s).booleanValue();
      }
    }
    return b;
  }

  /**
   * Converts TagType to TagInfo object
   */
  private TagInfo convertTagTypeToTagInfo(TagType type) throws WebWrongDescriptorException {

    String tagName = (type.getName() != null) ? type.getName().get_value() : null;
    String tagClassName = (type.getTagClass() != null) ? type.getTagClass().get_value() : null;
    String teiClassName = (type.getTeiClass() != null) ? type.getTeiClass().get_value() : null;
    String tagInfoString = (type.getDescription() != null && type.getDescription().length > 0) ? type.getDescription()[0].get_value() : null;
    String tagBodyContent = (type.getBodyContent() != null) ? type.getBodyContent().get_value() : null;
    String tagDisplayName = (type.getDisplayName() != null && type.getDisplayName().length > 0) ? type.getDisplayName()[0].get_value() : null;
    String tagLargeIcon = (type.getIcon() != null && type.getIcon().length > 0 && type.getIcon()[0].getLargeIcon() != null) ? type.getIcon()[0].getLargeIcon().get_value() : null;
    String tagSmallIcon = (type.getIcon() != null && type.getIcon().length > 0 && type.getIcon()[0].getSmallIcon() != null) ? type.getIcon()[0].getSmallIcon().get_value() : null;
    boolean tagDinamicAttr = (type.getDynamicAttributes() != null) && booleanValue(type.getDynamicAttributes().get_value());
    TagExtraInfo tagExtraInfo = null;
    if (teiClassName != null) {
      try {
        long startup = -1;
        if (Accounting.isEnabled()) {
          Accounting.beginMeasure("/loadClass(" + teiClassName + ")", appLoader.getClass());
          startup = System.currentTimeMillis();        
        }//ACCOUNTING.start - END
        Class teiClass = appLoader.loadClass(teiClassName);
        if (Accounting.isEnabled()) {
          startup = System.currentTimeMillis() - startup;
          WebContainer.loadClassTime.addAndGet(startup);
          Accounting.endMeasure("/loadClass(" + teiClassName + ")");
        }
        tagExtraInfo = (TagExtraInfo) teiClass.newInstance();
      } catch (ClassNotFoundException e) {
        throw new WebWrongDescriptorException(WebWrongDescriptorException.TAG_EXTRA_INFO_CLASS_NOT_FOUND,
          new Object[]{teiClassName}, e);
      } catch (InstantiationException e) {
        throw new WebWrongDescriptorException(WebWrongDescriptorException.TAG_EXTRA_INFO_CLASS_NOT_FOUND,
          new Object[]{teiClassName}, e);
      } catch (IllegalAccessException e) {
        throw new WebWrongDescriptorException(WebWrongDescriptorException.TAG_EXTRA_INFO_CLASS_NOT_FOUND,
          new Object[]{teiClassName}, e);
      } catch (NoClassDefFoundError e) {
        throw new WebWrongDescriptorException(WebWrongDescriptorException.CANNOT_LOAD_TEI_CLASS,
          new Object[]{teiClassName}, e);
      }
    }
    TagAttributeInfo[] tagAttributeInfo = fillTagAttributeInfo(type.getAttribute());
    TagVariableInfo[] tagVariableInfo = fillTagVariableInfo(type.getVariable());

    return new TagInfo(tagName, tagClassName, tagBodyContent, tagInfoString, this, tagExtraInfo, tagAttributeInfo, tagDisplayName, tagSmallIcon, tagLargeIcon, tagVariableInfo, tagDinamicAttr);
  }

  /**
   * Fills TagInfo objects from TagType descriptors
   */
  private void fillTagInfo() throws WebWrongDescriptorException {
    TagType[] tagsTemp = tagLibrary.getTag();
    if (tagsTemp != null && tagsTemp.length > 0) {
      tags = new TagInfo[tagsTemp.length];
      for (int i = 0; i < tagsTemp.length; i++) {
        tags[i] = convertTagTypeToTagInfo(tagsTemp[i]);
      }
    } else {
      tags = null;
    }
  }

  /**
   * Fills TagFileInfo objects from TagFileType descriptors
   */
  private void fillTagFileInfo() {
    TagFileType[] files = tagLibrary.getTagFile();
    if (files != null && files.length > 0) {
      tagFiles = new TagFileInfo[files.length];
      for (int i = 0; i < files.length; i++) {
        tagFiles[i] = convertTagFileTypeToTagFileInfo(files[i]);
      }
    } else {
      // according to spec, getTagFiles() must return a zero-sized
      // array if not tagfiles are defined
      tagFiles = new TagFileInfo[0];
    }
  }

  /**
   * Fills FunctionInfo objects from FunctionType descriptors
   */
  private void fillFunctionInfo() {
    FunctionType[] funcs = tagLibrary.getFunction();
    if (funcs != null && funcs.length > 0) {
      functions = new FunctionInfo[funcs.length];
      for (int i = 0; i < funcs.length; i++) {
        functions[i] = convertFunctionTypeToFunctionInfo(funcs[i]);
      }
    } else {
      // according to spec, getFunctions() must return a zero-sized
      // array if not tagfiles are defined
      functions = new FunctionInfo[0];
    }
  }

  /**
   * Converts TldAttrinbuteType to TagAttributeInfo arrays
   *
   * @param attrs TldAttrinbuteType array
   * @return TagAttributeInfo array
   * @throws WebWrongDescriptorException if incorrect data found
   */
  private TagAttributeInfo[] fillTagAttributeInfo(TldAttributeType[] attrs) throws WebWrongDescriptorException {

    if (attrs == null || attrs.length == 0) {
      return (new TagAttributeInfo[0]);
    }

    TagAttributeInfo[] infos = new TagAttributeInfo[attrs.length];
    String name = null;
    boolean required = false;
    boolean rtexprvalue = false;
    boolean fragment = false;
    boolean deferredValue = false;
    boolean deferredMethod = false;
    String expectedTypeName = null;
    String methodSignature = null;
    String description = null;
    String type = null;

    try {
      for (int i = 0; i < attrs.length; i++) {
        description = (attrs[i].getDescription() != null && attrs[i].getDescription().length > 0 && attrs[i].getDescription()[0] != null) ? attrs[i].getDescription()[0].get_value() : null;
        name = (attrs[i].getName() != null) ? attrs[i].getName().get_value() : null;
        required = (attrs[i].getRequired() != null) && booleanValue(attrs[i].getRequired().get_value());

        //setting default values for each attribute
        rtexprvalue = false;
        fragment = false;
        deferredValue = false;
        deferredMethod = false;
        expectedTypeName = "java.lang.Object";
        methodSignature = "void method()";
        type = null;

        if (attrs[i].getChoiceGroup1() != null && attrs[i].getChoiceGroup1().isSetSequenceGroup1()) {
          if (attrs[i].getChoiceGroup1().getSequenceGroup1().getSequenceGroup2() != null) {
            rtexprvalue = (attrs[i].getChoiceGroup1().getSequenceGroup1().getSequenceGroup2().getRtexprvalue() != null) && booleanValue(attrs[i].getChoiceGroup1().getSequenceGroup1().getSequenceGroup2().getRtexprvalue().get_value());
            type = (attrs[i].getChoiceGroup1().getSequenceGroup1().getSequenceGroup2().getType() != null) ? attrs[i].getChoiceGroup1().getSequenceGroup1().getSequenceGroup2().getType().get_value() : null;
          }

          if (attrs[i].getChoiceGroup1().getSequenceGroup1().getDeferredValue() != null) {
            deferredValue = true;
            if (attrs[i].getChoiceGroup1().getSequenceGroup1().getDeferredValue().getType() != null &&
              attrs[i].getChoiceGroup1().getSequenceGroup1().getDeferredValue().getType().get_value() != null) {
              expectedTypeName = attrs[i].getChoiceGroup1().getSequenceGroup1().getDeferredValue().getType().get_value();
            }
          }
          if (attrs[i].getChoiceGroup1().getSequenceGroup1().getDeferredMethod() != null) {
            deferredMethod = true;
            if (attrs[i].getChoiceGroup1().getSequenceGroup1().getDeferredMethod().getMethodSignature() != null &&
              attrs[i].getChoiceGroup1().getSequenceGroup1().getDeferredMethod().getMethodSignature().get_value() != null) {
              methodSignature = attrs[i].getChoiceGroup1().getSequenceGroup1().getDeferredMethod().getMethodSignature().get_value();
            }
          }
        }


        if (type == null && !rtexprvalue) {
          type = "java.lang.String";
        }
        fragment = (attrs[i].getChoiceGroup1() != null && attrs[i].getChoiceGroup1().isSetFragment()) && booleanValue(attrs[i].getChoiceGroup1().getFragment().get_value());
        if (fragment) {
          type = "javax.servlet.jsp.tagext.JspFragment";
          rtexprvalue = true;
        }
        infos[i] = new TagAttributeInfo(name, required, type, rtexprvalue, fragment, description, deferredValue, deferredMethod, expectedTypeName, methodSignature);
      }
    } catch (Exception ex) {
      throw new WebWrongDescriptorException(WebWrongDescriptorException.WRONG_TAG_VALUE,
        new Object[]{type}, ex);
    }

    return infos;
  }

  /**
   * Converts VariableType to TagVariableInfo arrays
   *
   * @param vars VariableType array
   * @return TagVariableInfo array
   * @throws WebWrongDescriptorException if incorrect data found
   */
  private TagVariableInfo[] fillTagVariableInfo(VariableType[] vars) throws WebWrongDescriptorException {

    if (vars == null || vars.length == 0) {
      return (new TagVariableInfo[0]);
    }

    TagVariableInfo[] infos = new TagVariableInfo[vars.length];
    String nameGiven = null;
    String nameFromAttribute = null;
    String variableClassDefault = "java.lang.String";
    boolean declareDefault = true;
    String variableClass = "java.lang.String";
    boolean declare = true;
    int scope = VariableInfo.NESTED;

    try {
      for (int i = 0; i < vars.length; i++) {
        nameGiven = (vars[i].getChoiceGroup1() != null && vars[i].getChoiceGroup1().isSetNameGiven()) ? vars[i].getChoiceGroup1().getNameGiven().get_value() : null;
        nameFromAttribute = (vars[i].getChoiceGroup1() != null && vars[i].getChoiceGroup1().isSetNameFromAttribute()) ? vars[i].getChoiceGroup1().getNameFromAttribute().get_value() : null;
        // default for <variable-class> if not defined is "java.lang.String"
        variableClass = (vars[i].getVariableClass() != null) ? vars[i].getVariableClass().get_value() : variableClassDefault;
        // default for <declare> if not defined is "true"
        declare = (vars[i].getDeclare() != null) ? booleanValue(vars[i].getDeclare().get_value()) : declareDefault;
        if (vars[i].getScope() != null) {
          String temp = vars[i].getScope().get_value();
          if ("NESTED".equalsIgnoreCase(temp)) {
            scope = VariableInfo.NESTED;
          } else if ("AT_BEGIN".equalsIgnoreCase(temp)) {
            scope = VariableInfo.AT_BEGIN;
          } else if ("AT_END".equalsIgnoreCase(temp)) {
            scope = VariableInfo.AT_END;
          }
        } else {
          scope = VariableInfo.NESTED;
        }
        infos[i] = new TagVariableInfo(nameGiven, nameFromAttribute, variableClass, declare, scope);
      }
    } catch (Exception ex) {
      throw new WebWrongDescriptorException(WebWrongDescriptorException.WRONG_TAG_VALUE,
        new Object[]{nameGiven + " or " + nameFromAttribute}, ex);
    }

    return infos;
  }

  /**
   * Converts TagFileType to TagFileInfo
   *
   * @param file TagFileType
   * @return TagFileInfo
   */
  private TagFileInfo convertTagFileTypeToTagFileInfo(TagFileType file) {
    String name = (file.getName() != null) ? file.getName().get_value() : null;
    String path = (file.getPath() != null) ? file.getPath().get_value() : null;
    return new TagFileInfo(name, path, null);
  }

  /**
   * Coverts FunctionType to FunctionInfo
   *
   * @param type FunctionType
   * @return FunctionInfo
   */
  private FunctionInfo convertFunctionTypeToFunctionInfo(FunctionType type) {
    String name = (type.getName() != null) ? type.getName().get_value() : null;
    String klass = (type.getFunctionClass() != null) ? type.getFunctionClass().get_value() : null;
    String signature = (type.getFunctionSignature() != null) ? type.getFunctionSignature().get_value() : null;
    return new FunctionInfo(name, klass, signature);
  }

  /**
   * Sets  prefix field of TagLibInfo object
   *
   * @param prefix String
   */
  public void setPrefixString(String prefix) {
    this.prefix = prefix;
  }

  /**
   * Sets URI field of TagLibInfo object
   *
   * @param uri String
   */
  public void setURI(String uri) {
    this.uri = uri;
  }

  public void setTagFileInfo(int index, TagFileInfo tagFileInfo) {
    tagFiles[index] = tagFileInfo;
  }

  /**
   * Holds all tag-files parsing exception for current TagLib
   */
  public ConcurrentHashMapObjectObject getExceptionsTable() {
    return exceptionsTable;
  }

  /**
   * Returns an array of TagLibraryInfo objects representing the entire set of tag libraries (including this TagLibraryInfo)
   * imported by taglib directives in the translation unit that references this TagLibraryInfo.
   * If a tag library is imported more than once and bound to different prefixes,
   * only the TagLibraryInfo bound to the first prefix must be included in the returned array.
   *
   * @return
   */
  public javax.servlet.jsp.tagext.TagLibraryInfo[] getTagLibraryInfos() {
    return tagLibraryInfos;
  }

  public void setTagLibraryInfos(javax.servlet.jsp.tagext.TagLibraryInfo[] getTagLibraryInfos) {
    this.tagLibraryInfos = getTagLibraryInfos;
  }

  /**
   * Sets the JSP version of this TLD.
   *
   * @param jspVersion
   */
  public void setRequiredVersion(String jspVersion) {
    jspversion = jspVersion;
  }

  /**
   * This method clones TagLibraryInfo to prevent incorrect presetting of prefix and tagLibraryInfos
   * @return
   */
  public TagLibDescriptor clone() {
    TagLibDescriptor newTLD = new TagLibDescriptor(this.appLoader);
    newTLD.prefix = this.prefix;
    newTLD.tagLibraryInfos = this.tagLibraryInfos;
    newTLD.tagLibrary = this.tagLibrary;
    newTLD.exceptionsTable = this.exceptionsTable;
    newTLD.functions = this.functions;
    newTLD.info = this.info;
    newTLD.jspversion = this.jspversion;
    newTLD.shortname = this.shortname;
    newTLD.tagFiles = this.tagFiles;
    newTLD.tags = this.tags;
    newTLD.tlibversion = this.tlibversion;
    newTLD.uri = this.uri;
    newTLD.urn = this.urn;
    return newTLD;
  }

  public boolean equalTLDs(TagLibDescriptor tld) {
    if (this.shortname != null && !this.shortname.equals(tld.shortname)) {
      return false;
    }
    if (this.uri != null && !this.uri.equals(tld.uri)) {
      return false;
    }
    if (this.jspversion != null && !this.jspversion.equals(tld.jspversion)) {
      return false;
    }
    return true;
  }
}
