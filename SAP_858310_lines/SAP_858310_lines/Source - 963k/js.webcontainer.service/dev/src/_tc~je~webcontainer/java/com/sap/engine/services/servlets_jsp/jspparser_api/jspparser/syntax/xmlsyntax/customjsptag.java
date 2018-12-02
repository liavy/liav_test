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
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax;

import java.util.List;

import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagFileInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;

import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Attribute;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.DebugInfo;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Element;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.ElementCollection;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.IDCounter;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspElement;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Position;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.TemplateData;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.taglib.TagBeginGenerator;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.taglib.TagEndGenerator;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.taglib.tagfiles.TagFilesUtil;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.TagLibDescriptor;

/*
 *
 * @author Galin Galchev,Ivo Simeonov
 * @version 4.0
 */
public class CustomJspTag extends JspTag {

  /*
   * start indentification
   */
  protected char[] START_TAG_INDENT;
  /*
   * end indentification
   */
  protected char[] CMP_0;
  /*
   * whether to create new element when parse
   */
  protected boolean copy;
  /*
   * tag info of this custom tag
   */
  public TagInfo tagInfo;
  /*
   * prefix of this custom tag
   */
  public String prefix;
  /*
   * short name of this custom tag
   */
  public String shortName;
  /*
   * all attributes of this tag
   */
//  public Hashtable tagAttributes;
  private boolean isEndTag = false;


  /*
   * Flag indicationg that the current tag is into a separated method, generated for the parent tag.
   */
  private boolean isInMethod = false;

  /**
   * Creates new CustomJspTag
   *
   */
  public CustomJspTag() {
    elementType = Element.XML_TAG;
    copy = false;
  }

  /**
   * Creates new CustomJspTag with the specified
   * start and end indents
   *
   * @param   startTagIndent  start indent
   * @param   endTagIndent  end indent
   */
  public CustomJspTag(char[] startTagIndent, char[] endTagIndent) {
    this();
    //this.isEndTag = isEndTag;
    START_TAG_INDENT = startTagIndent;
    CMP_0 = endTagIndent;
  }

  public void action() throws JspParseException {
    action(parser.getScriptletsCode());
  }

  /**
   * Takes specific action coresponding to this jsp element
   * logic
   *
   * @exception   JspParseException  thrown if error occures during
   * verification
   */
  public void action(StringBuffer buffer) throws JspParseException {
    switch (tagType) {
      case SINGLE_TAG: {
        debugInfo.writeStart(buffer, false);
        CustomJspTag tag = this;
        TagBeginGenerator beginTag = null;
        try {
          beginTag = new TagBeginGenerator(tag.prefix, tag.shortName, tag.attributes, tag.tagInfo, parser);
          beginTag.init(parser.getApplicationClassLoader());

          buffer.append(beginTag.getIdent() + "// CustomJspTag.action() - start").append("\n");
        } catch (JspParseException e) {
          throw new JspParseException(JspParseException.CANNOT_PARSE_CUSTOM_TAG, new Object[]{shortName}, e);
        }

        StringBuffer tmpBuffer = null;
//        boolean isScriptless = (tag.tagInfo.getVariableInfo(beginTag.getTagData()) == null) && !hasExpressionValue();
        boolean isScriptless = (tag.tagInfo.getVariableInfo(beginTag.getTagData()) == null || tag.tagInfo.getVariableInfo(beginTag.getTagData()).length == 0) &&
                                (tag.tagInfo.getTagVariableInfos().length == 0) && 
                                !hasExpressionValue();

        String parent = null;
        boolean isParentSimple = false;
        if (beginTag.topTag() != null) {
          if (beginTag.topTag().isSimpleTag) {
            isParentSimple = true;
            if (!beginTag.isSimpleTag()) {
              parent = "new javax.servlet.jsp.tagext.TagAdapter((javax.servlet.jsp.tagext.SimpleTag) _jspx_parent)";
            } else {
              parent = "_jspx_parent";
            }
          } else {
            parent = beginTag.topTag().tagHandlerInstanceName;
          }
        }
        if (isScriptless) {
          String methodName = "_jspx_method_" + beginTag.getBaseVarName();
          tmpBuffer = new StringBuffer();

          buffer.append(beginTag.getIdent() + "if (" + methodName + "(_jspx_pageContext, " + parent + ")) {\n");
          if (isInMethod && !isParentSimple){
            buffer.append(beginTag.getIdent() + "\treturn true;\r\n"+ beginTag.getIdent() + "}\r\n");
          } else if (parser.getJspBodyVariable() != null) {
            buffer.append(beginTag.getIdent() + "\r\n\tString result_" + parser.getJspBodyVariable() + " = ((javax.servlet.jsp.tagext.BodyContent)out).getString();");
            buffer.append(beginTag.getIdent() + "\r\n\tout = _jspx_pageContext.popBody();");
            buffer.append(beginTag.getIdent() + "\r\n\treturn result_" + parser.getJspBodyVariable() + ";");
            buffer.append(beginTag.getIdent() + "\r\n}\r\n");

          } else {
            buffer.append(beginTag.getIdent() + "\treturn;\r\n"+ beginTag.getIdent() + "}\r\n");
          }

          tmpBuffer.append("\t// CustomJspTag.action() - start").append("\n");

          // Declaration of the Tag method, goes into the parser.getTagFragmentMethodsCode() vector
          String parentClass = beginTag.isSimpleTag() ? "javax.servlet.jsp.tagext.JspTag" : "javax.servlet.jsp.tagext.Tag";
          tmpBuffer.append("\tprivate boolean "+ methodName +"(com.sap.engine.services.servlets_jsp.lib.jspruntime.PageContextImpl _jspx_pageContext, " +
              parentClass + " _jspx_parent) throws Throwable {\r\n");
          tmpBuffer.append("\t\tJspWriter out = _jspx_pageContext.getOut();\r\n");
          try {
            beginTag.generate(tmpBuffer, parser.getTagFragmentMethodsCode(), true, false, true);
          } catch (JspParseException e) {
            throw new JspParseException(JspParseException.CANNOT_PARSE_CUSTOM_TAG, new Object[]{shortName}, e);
          } catch (NoClassDefFoundError t) {
            throw new JspParseException(JspParseException.CANNOT_PARSE_CUSTOM_TAG, new Object[]{tag.shortName}, t);
          }
        } else {
          try {
            beginTag.generate(buffer, parser.getTagFragmentMethodsCode(), false, false, true);
          } catch (JspParseException e) {
            throw new JspParseException(JspParseException.CANNOT_PARSE_CUSTOM_TAG, new Object[]{shortName}, e);
          } catch (NoClassDefFoundError t) {
            throw new JspParseException(JspParseException.CANNOT_PARSE_CUSTOM_TAG, new Object[]{tag.shortName}, t);
          }  
        }
        // sets parent tag
        tag.parentCustomTag = beginTag.topTag()== null ? null : beginTag.topTag().tagHandlerInstanceName;

        parser.setFragmentsCount(beginTag.getFragmentsCount());

//        StringBuffer fragmentBody = null;
//        if (beginTag.isSimpleTag() && !parser.getTagFragmentMethodsCode().isEmpty()) {
//          fragmentBody = (StringBuffer)parser.getTagFragmentMethodsCode().lastElement();
//        }

        TagEndGenerator endTag = new TagEndGenerator(tag.prefix, tag.shortName, tag.attributes, tag.tagInfo, parser);
        if (isScriptless) {
          endTag.generate(tmpBuffer, null, null, isScriptless, false, true, parser.isTagFile());
          parser.getTagMethodsCode().add(tmpBuffer);

          tmpBuffer.append("\t// CustomJspTag.action() - end").append("\n");
        } else {
          endTag.generate(buffer, null, null, isInMethod, false, true, parser.isTagFile());
        }

        buffer.append(beginTag.getIdent() + "// CustomJspTag.action() - end").append("\n");
        debugInfo.writeEnd(buffer);
        break;
      }
      case START_TAG:
      case END_TAG:
      default:
    }
  }

  /**
   * Retuns the indentification for this tag
   *
   * @return  array containing chars that indentify
   * this tag
   */
  public char[] indent() {
    return START_TAG_INDENT;
  }

  /**
   * Parses next tokens
   *
   * @param   parser  parse to use
   * @return   newly created element
   * @exception   JspParseException  thrown if error occures during
   * parsing or verification
   */
  public Element parse(JspPageInterface parser) throws JspParseException {
    //assume current char is <
    Element ret = null;
    CustomJspTag _this = createThis();
    Position startDebugPos = parser.currentDebugPos();
    _this.startIndex = parser.currentPos();
    _this.parser = parser;
    char c = parser.nextChar();

    if (c == '/') {
      //end tag
      _this.tagType = JspTag.END_TAG;
      ret = _this;
      
      //   ETag ::=  '</' Name S? '>'
      char[] CMP_0Copy = new char[_this.CMP_0.length-1]; // the end tag without the closing bracket 
      for (int i = 0; i < _this.CMP_0.length-1; i++) { // copy the end tag except the closing bracket
        CMP_0Copy[i] = _this.CMP_0[i];
      }    
      boolean isEndTag = false; // error flag
      if (parser.compare(_this.startIndex, _this.startIndex + CMP_0Copy.length, CMP_0Copy)) { // check for matching  
        //check for whitespace followed by '>'  
        int checkPosition = _this.startIndex + CMP_0Copy.length ;
        int whiteSpaces = 0; // the whitespaces to be inserted in the closing tag in the place of the 'S?' ->   ETag ::=  '</' Name S? '>'
        while( !parser.endReached() && isWhiteSpace(parser.charAt(checkPosition)) ) {
          checkPosition++;
          whiteSpaces++;
        }
        if( parser.charAt(checkPosition) == '>') {
          isEndTag = true;
          CMP_0Copy = new char[_this.CMP_0.length + whiteSpaces];
          for (int i = 0; i < _this.CMP_0.length-1; i++) {
            CMP_0Copy[i] = _this.CMP_0[i]; // copy the content until the whitespaces before the closing bracket 
          } 
          for (int i = 0; i < whiteSpaces; i++) {
            CMP_0Copy[i] = ' '; // insert found whitespaces               
          }
          CMP_0Copy[CMP_0Copy.length-1] = '>'; // close the bracket
          _this.CMP_0 = CMP_0Copy;
        }
      }
      if( !isEndTag ) {
        throw new JspParseException(JspParseException.IMPLICIT_PARSING_ERROR_PARSING_ELEMENT_IS_NOT_VALID_FOR_TOKEN, parser.currentFileName(), parser.currentDebugPos());
      }
      for (int i = 0; i < CMP_0.length - 1; i++) {
        parser.nextChar();
      }
      _this.endIndex = parser.currentPos();
    } else {
      // start or empty tag
      for (int i = 0; i < _this.START_TAG_INDENT.length - 1; i++) {
        parser.nextChar();
      }

      if ( !parser.compare(_this.startIndex, parser.currentPos(), START_TAG_INDENT)) {
        throw new JspParseException(JspParseException.IMPLICIT_PARSING_ERROR_PARSING_ELEMENT_IS_NOT_VALID_FOR_TOKEN, parser.currentFileName(), parser.currentDebugPos());
      }

      _this.readAttributes("/>");

      if (parser.currentChar() == '/') {
        //empty tag
        if (parser.nextChar() == '>') {
          parser.nextChar();
          _this.tagType = JspTag.SINGLE_TAG;
          ret = _this;
          DebugInfo info = new DebugInfo(startDebugPos, parser.currentDebugPos(), parser.currentFileName(), parser.getWebContainerParameters().isJspDebugSupport());
          _this.debugInfo = info;
          //boby
          _this.endIndex = parser.currentPos();
        } else {
          throw new JspParseException(JspParseException.EXPECTING_INSTEAD_OF, new Object[]{">", escape(parser.currentChar())}, parser.currentFileName(), parser.currentDebugPos());
        }
        _this.verifyAttributes();
      } else if (parser.currentChar() == '>') {
        Position endDebugPos = parser.currentDebugPos();
        parser.nextChar();
        //start tag
        _this.tagType = JspTag.START_TAG;        
        _this.debugInfo = new DebugInfo(startDebugPos, endDebugPos, parser.currentFileName(), parser.getWebContainerParameters().isJspDebugSupport());

        JspElement jspElement = _this.createJspElement();
        jspElement.setStartIndex(_this.startIndex);
        jspElement.setStartTag(_this);
        _this.endIndex = parser.currentPos();

        //zaradi CTS-a se propuska skip na white space.
        //parser.skipWhiteSpace();
        if (parser.endReached()) {
          throw new JspParseException(JspParseException.JSP_TAG_IS_NOT_CLOSED, new Object[]{String.copyValueOf(_this.START_TAG_INDENT, 1, _this.START_TAG_INDENT.length - 1)}, parser.currentFileName(), parser.currentDebugPos());
        }
        if (checkTagDependentBody(parser) ) {
          parser.parseTo(CMP_0, false);
          TemplateData t = new TemplateData(parser.getChars(_this.endIndex, parser.currentPos()), false);
          t.parser = parser;
          t.debugInfo = new DebugInfo(startDebugPos, endDebugPos, parser.currentFileName(), parser.getWebContainerParameters().isJspDebugSupport());
          t.isTagDependent = true;
          jspElement.setBody(t);
        } else {
          Element el = ((ElementCollection)parser.getRootElement()).parseTo(parser, CMP_0);
          if (el.elements.length == 1 && el.elements[0] instanceof TemplateData) {
            jspElement.setBody(el.elements[0]);
          } else {
            jspElement.setBody(el);
            if( el instanceof ElementCollection){
              initJspElementsBodyElements((ElementCollection)el, jspElement);
            } else if( el instanceof JspElement){
              ((JspElement)el).setParentJspElement(jspElement);
            }
          }
        }
        parser.skipWhiteSpace();

        if (parser.endReached()) {
          throw new JspParseException(JspParseException.JSP_TAG_IS_NOT_CLOSED, new Object[]{String.copyValueOf(_this.START_TAG_INDENT, 1, _this.START_TAG_INDENT.length - 1)}, parser.currentFileName(), parser.currentDebugPos());
        }

        JspTag js = _this.createEndTag();
        ((CustomJspTag) js).setIsEndTag();
        jspElement.setEndTag(js);
        ret = jspElement;
        ret.parser = parser;
        jspElement.setEndIndex(parser.currentPos());
        DebugInfo info = new DebugInfo(startDebugPos, endDebugPos, parser.currentFileName(), parser.getWebContainerParameters().isJspDebugSupport());
        jspElement.debugInfo = info;
        jspElement.setType(Element.ACTION);
        jspElement.verify(jspElement.type);
      } else {
        throw new JspParseException(JspParseException.IMPLICIT_PARSING_ERROR_PARSING_ELEMENT_IS_NOT_VALID_FOR_TOKEN, parser.currentFileName(), parser.currentDebugPos());
      }
      //_this.verifyAttributes();
    }

    return ret;
  }
  
  /**
   * Loops over the whole element collection and sets the given parent to each element.
   * @param elementCollection
   * @param parent
   */
  private void initJspElementsBodyElements(ElementCollection elementCollection, JspElement parent){
    Element[]  elements = elementCollection.getElements();
    if( elements == null || elements.length == 0){
      return;
    }
    for (int i = 0; i < elements.length; i++) {
      Element currElement = elements[i];
      if( currElement instanceof JspElement ){
        JspElement jspElement = (JspElement) currElement;
        jspElement.setParentJspElement(parent);
      } 
      //else if ( currElement instanceof ElementCollection ){
      //  ElementCollection elementCollection2 = (ElementCollection) currElement;
      //  initJspElementsBodyElements(elementCollection2, parent);
      //}
    }
  }

  private boolean checkTagDependentBody(JspPageInterface parser) {
    String indent = String.copyValueOf(START_TAG_INDENT);
    if( indent.indexOf(':')> 0 ){
      prefix = indent.substring(1, indent.indexOf(':'));
      shortName = indent.substring(indent.indexOf(':') + 1);
    }else {
      prefix = "";
      shortName = indent.substring(1);
    }
    return TagFilesUtil.checkTagDependentBody(prefix, shortName, parser);
  }

  /**
   * Verifies the attributes of this tag
   *
   * @exception   JspParseException  thrown if error occures
   * during verification
   */
  public void verifyAttributes() throws JspParseException {

    String indent = String.copyValueOf(START_TAG_INDENT);
    if( indent.indexOf(':')> 0 ){
      prefix = indent.substring(1, indent.indexOf(':'));
      shortName = indent.substring(indent.indexOf(':') + 1);
    }else {
      prefix = "";
      shortName = indent.substring(1);
    }
    
    
    TagLibraryInfo tagLibraryInfo = (TagLibraryInfo) parser.getTaglibs().get(prefix);
    tagInfo = null;

    if (tagLibraryInfo == null) {
      throw new JspParseException(JspParseException.UNRECOGNIZED_TAG_LIBRARY_PREFIX, new Object[]{prefix}, parser.currentFileName(), debugInfo.start);
    }

    tagInfo = tagLibraryInfo.getTag(shortName);
    TagFileInfo tagFileInfo = tagLibraryInfo.getTagFile(shortName);

    if (tagInfo == null && tagFileInfo == null) {
      throw new JspParseException(JspParseException.UNRECOGNIZED_TAG_NAME, new Object[]{shortName}, parser.currentFileName(), debugInfo.start);
    }

    if (tagFileInfo != null) {
      Exception obj =  (Exception)((TagLibDescriptor)tagLibraryInfo).getExceptionsTable().get(tagFileInfo.getPath());
      if (obj != null) {
        if( obj instanceof JspParseException){
          throw (JspParseException) obj;
        }else{
          throw new JspParseException(JspParseException.RETHROW_EXCEPTION, new String[]{obj.getLocalizedMessage()});
        }
      }
    }

    if (tagFileInfo != null) {
      tagInfo = tagFileInfo.getTagInfo();
      if (!TagFilesUtil.isTagClassFileUpToDate(tagFileInfo, parser.getParserParameters().getApplicationRootDir())){
        try {
          tagFileInfo = TagFilesUtil.parseTagFile(tagFileInfo.getPath(), tagFileInfo, (TagLibDescriptor) tagLibraryInfo, parser);
          tagInfo = tagFileInfo.getTagInfo();
        } catch (OutOfMemoryError ex) {
          throw ex;
        } catch (ThreadDeath ex) {
          throw ex;
        } catch (JspParseException ex) {
          throw ex;
        } catch (Throwable ex) {
          throw new JspParseException(JspParseException.ERROR_PARSING_TAGFILE, ex);
        }
      }
    }
    //preset TagLibraryInfo if empty
    if (tagInfo.getTagLibrary() == null) {
      tagInfo.setTagLibrary(tagLibraryInfo);
    }



    TagAttributeInfo[] attributeInfos = tagInfo.getAttributes();
//    tagAttributes = new Hashtable();

    /*
     * Check if all supplied attributes are valid for this tag
     * Skipped if the tag has DynamicAttributes, since then if
     * an attribute is not defined in the TLD is will be set using
     * the setDynamicAttribute() method
     */
    if (attributes != null && !tagInfo.hasDynamicAttributes()) {
      for (int i = 0; i < attributes.length; i++) {
        String name = attributes[i].name.toString();
        String attrPrefix = null;
        String attrName = null;
        int qIndex = name.indexOf(':');
        if (qIndex > -1) {
          attrPrefix = name.substring(0, qIndex);
          attrName = name.substring(qIndex +1);
          if (prefix.equals(attrPrefix)) {
            name = attrName;
            attributes[i].name.value = name;
          }
        }
        for (int j = 0; j < attributeInfos.length; j++) {
          if (name.equals(attributeInfos[j].getName())) {
            break;
          } else if (j == attributeInfos.length - 1) {
            throw new JspParseException(JspParseException.ATTRIBUTE_NOT_FOUND_IN_TAG_LIBRARY_FOR_THE_TAG, new Object[]{name, tagLibraryInfo.getShortName(), shortName}, parser.currentFileName(), debugInfo.start);
          }
        }

      }
    }

    for (int j = 0; j < attributeInfos.length; j++) {
      Attribute attributeInJSP = null;
      if (attributes != null) {
        for (int i = 0; i < attributes.length; i++) {
          if (attributes[i].name.toString().equals(attributeInfos[j].getName())) {
            attributeInJSP = attributes[i];
            break;
          }
        }
      }
      if (attributeInfos[j].isRequired()) {
        if (attributeInJSP == null) {
          throw new JspParseException(JspParseException.ATTRIBUTE_IS_REQUIRED_BUT_WAS_NOT_FOUND_IN_THE_TAG_OF_THE_ACTION, new Object[]{attributeInfos[j].getName()}, parser.currentFileName(), debugInfo.start);
        }
      }
    }
  }

  /**
   * sets the copy flag of this object
   *
   * @param   copy
   */
  public void setCopy(boolean copy) {
    this.copy = copy;
  }

  /**
   * Returnes element to witch will be associated
   * current parsing.
   *
   * @return object that is associated as current element
   * of parsing
   */
  protected CustomJspTag createThis() {
    return (this.copy) ? new CustomJspTag(START_TAG_INDENT, CMP_0) : this;
  }

  /**
   * Returnes element to witch will be associated
   * current parsing.
   *
   * @return object that is associated as current element
   * of parsing
   */
  protected JspElement createJspElement() {
    return new JspElement(JspElement.CUSTOM);
  }

  /**
   * Returnes element to witch will be associated
   * current parsing.
   *
   * @return object that is associated as current element
   * of parsing
   * @exception   JspParseException  thrown if error occures
   * during verification or parsing
   */
  protected JspTag createEndTag() throws JspParseException {
    return (JspTag) (new CustomJspTag(START_TAG_INDENT, CMP_0).parse(this.parser));
  }

  public void setIsEndTag() {
    isEndTag = true;
  }

  private boolean getIsEndTag() {
    return isEndTag;
  }

  public String getString(IDCounter id) throws JspParseException {
    String s = "";

    if (this.getIsEndTag()) {
      return new String(CMP_0);
    } else {
      s = s + new String(START_TAG_INDENT);
    }

    String name = null;
    String value = null;

    if (attributes != null) {
      for (int i = 0; i < attributes.length; i++) {
        s = s + "\n\t";
        name = attributes[i].name.toString();
        value = convertAttributeValue(attributes[i].value.toString());
        s = s + name + "=\"" + value + "\"";
      }
    }
    
    if( getNameSpaceAttribute() != null && getNameSpaceAttribute().size() > 0){
      List<Attribute> nameSpaceAttributes = getNameSpaceAttribute();
      for(Attribute nameSpaceAttribute : nameSpaceAttributes) {
        s = s + " ";
        name = nameSpaceAttribute.name.toString();
        value = convertAttributeValue(nameSpaceAttribute.value.toString());
        s = s + name + "=\"" + value + "\"";              
      }
    }
    
    s = s + JSP_ID + "=\"" + id.getId() + "\"";

    if (tagType == JspTag.SINGLE_TAG) { 
      s = s + SLASH_END;
    } else {
      s = s + END;
    }

    return s;
  }

  private static String convertAttributeValue(String value) {
    String ret = value.trim();

    if (ret.startsWith("<%=") && ret.endsWith("%>")) {
      ret = ret.substring(1, ret.length() - 1);
    }

    StringBuffer sb = new StringBuffer();
    for(int i = 0; i < ret.length(); i++) {
        char c = ret.charAt(i);
        if (c == '<') {
            sb.append("&lt;");
        } else if (c == '>') {
            sb.append("&gt;");
        } else if (c == '\'') {
            sb.append("&apos;");
        } else if (c == '&') {
            sb.append("&amp;");
        } else if (c == '"') {
            sb.append("&quot;");
        } else {
            sb.append(c);
        }
    }
    return sb.toString();
  }

  private boolean hasExpressionValue() {
//    Enumeration en = tagAttributes.keys();
//    while (en.hasMoreElements()) {
    if (attributes != null) {
      for (int i = 0; i < attributes.length; i++) {
        // isXml ???
        if (isRuntimeExpr(attributes[i].value.toString())) {
          return true;
        }
      }
    }
    return false;
  }

  public void setStartTagIndent(char[] startTagIndent) {
    START_TAG_INDENT = startTagIndent;
  }

  public void setEndTagIndent(char[] endTagIndent) {
    CMP_0 = endTagIndent;
  }

  /**
   * Return true if the current tag is into a separated method, generated for the parent tag.
   * @return
   */
  public boolean isInMethod() {
    return isInMethod;
  }

  /**
   * Setter method for the flag , indicationg that the current tag is into a separated method, generated for the parent tag.
   * @param inMethod
   */
  public void setInMethod(boolean inMethod) {
    isInMethod = inMethod;
  }
}

