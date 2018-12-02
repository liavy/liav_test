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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.PluginGenerator;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Indentifier;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspElement;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspTag;

/*
 * Class that parses &lt;jsp:plugin&gt; tag and then invokes generation of output with PluginGenerator. 
 * 
 * @see com.sap.engine.services.servlets_jsp.lib.jspparser.PluginGenerator
 * @author Galin Galchev,Ivo Simeonov
 * @version 4.0
 */
public class PluginTag extends CustomJspTag {
  
  /*
   * Attribute of jsp:plugin since JSP 2.0. Possible values - <code>true</code> or <code>true</code>.
   */
  private static final String MAYSCRIPT_STR = "mayscript"; 
  /*
   * all attributes that this element can
   * accept
   */
  public static final char[][] attributeNames = {"type".toCharArray(), "code".toCharArray(), "codebase".toCharArray(), 
    "align".toCharArray(), "archive".toCharArray(), "height".toCharArray(), "hspace".toCharArray(), 
    "jreversion".toCharArray(), "name".toCharArray(), "vspace".toCharArray(), "width".toCharArray(), 
    "nspluginurl".toCharArray(), "iepluginurl".toCharArray(), "title".toCharArray(), MAYSCRIPT_STR.toCharArray()};
  

  /**
   * Creates new PluginTag
   *
   */
  public PluginTag() {
    super();
    _name = "plugin";
    START_TAG_INDENT = (_default_prefix_tag_start + _name).toCharArray();
    CMP_0 = (_default_prefix_tag_end + _name + END).toCharArray();
  }

  //  /**
  //   *
  //   *
  //   * @param   startTagIndent
  //   * @param   endTagIndent
  //   */
  //  public PluginTag(char[] startTagIndent ,char[] endTagIndent) {
  //    this();
  //    START_TAG_INDENT = startTagIndent;
  //    CMP_0 = endTagIndent;
  //  }
  //
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
        Hashtable attrs = new Hashtable();
        Hashtable param = new Hashtable();
        String value;
        String key;

        for (int i = 0; i < attributeNames.length; i++) {
          key = String.copyValueOf(attributeNames[i]);
          value = getAttributeValue(key);

          if (value != null) {
            attrs.put(key, value);
          }
        } 

        try {
          PluginGenerator plugin = new PluginGenerator(attrs, param, "", this);
          plugin.init(null);
          plugin.generate(buffer);
          debugInfo.writeEnd(buffer);
        } catch (OutOfMemoryError e) {
          throw e;
        } catch (ThreadDeath e) {
          throw e;
        } catch (Throwable e) {
          throw new JspParseException(JspParseException.ERROR_PARSING_PLUGIN + getException(e));
        }
        break;
      }
      case START_TAG:
      case END_TAG:
      default:
    }
  }
  
  /**
   * Transforms throwable stack trace into String
   *
   * @param   t  a throwable
   * @return  String representing the stack trace  of the
   * specified throwable
   */
  private String getException(Throwable t) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(new StringWriter());
    t.printStackTrace(printWriter);
    return stringWriter.toString();
  }

  public void action() throws JspParseException {
    action(parser.getScriptletsCode());
  }

  /**
   * Retuns the indentification for this tag
   *
   * @return  array containing chars
   * "<jsp:plugin"
   */
  public char[] indent() {
    return START_TAG_INDENT;
  }

  /**
   * Returnes element to witch will be associated
   * current parsing.
   *
   * @return object that is associated as current element
   * of parsing
   */
  protected CustomJspTag createThis() {
    return (this.copy) ? new PluginTag() : this;
  }

  /**
   * Returnes element to witch will be associated
   * current parsing.
   *
   * @return object that is associated as current element
   * of parsing
   */
  protected JspElement createJspElement() {
    return new JspElement(JspElement.PLUGIN);
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
    return (JspTag) (new PluginTag().parse(this.parser));
  }

  /**
   * Verifies the attributes of this tag
   *
   * @exception   JspParseException  thrown if error occures
   * during verification
   */
  public void verifyAttributes() throws JspParseException {
    if (attributes == null || attributes.length < 3) {
      throw new JspParseException(JspParseException.ACTION_MUST_HAVE_AT_LEAST_THREE_SPECIFIED_ATTRIBUTES, new Object[]{_name}, parser.currentFileName(), debugInfo.start);
    }

    Indentifier e = null;
    Indentifier value = null;
    boolean[] flags = new boolean[attributeNames.length];
    Arrays.fill(flags, true);

    for (int i = 0; i < attributes.length; i++) {
      e = attributes[i].name;
      value = attributes[i].value;

      if (e.equals(attributeNames[0])) {
        if (flags[0]) {
          flags[0] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"type", _name}, parser.currentFileName(), debugInfo.start);
        }
        if (!(value.equals("bean") || value.equals("applet"))){
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_ACTION, new Object[]{value.toString(), "type", _name}, parser.currentFileName(), debugInfo.start);
        }
        if (isRuntimeExpr(value.toString())) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_ACTION, new Object[]{value.toString(), e.toString(), _name}, parser.currentFileName(), debugInfo.start);
        }
      } else if (e.equals(attributeNames[1])) {
        if (flags[1]) {
          flags[1] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"code", _name}, parser.currentFileName(), debugInfo.start);
        }
        if (isRuntimeExpr(value.toString())) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_ACTION, new Object[]{value.toString(), e.toString(), _name}, parser.currentFileName(), debugInfo.start);
        }
        //ok
      } else if (e.equals(attributeNames[2])) {
        if (flags[2]) {
          flags[2] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"codeBase", _name}, parser.currentFileName(), debugInfo.start);
        }
        if (isRuntimeExpr(value.toString())) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_ACTION, new Object[]{value.toString(), e.toString(), _name}, parser.currentFileName(), debugInfo.start);
        }
        //ok
      } else if (e.equals(attributeNames[3])) {
        if (flags[3]) {
          flags[3] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"align", _name}, parser.currentFileName(), debugInfo.start);
        }
        if (isRuntimeExpr(value.toString())) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_ACTION, new Object[]{value.toString(), e.toString(), _name}, parser.currentFileName(), debugInfo.start);
        }
      } else if (e.equals(attributeNames[4])) {
        if (flags[4]) {
          flags[4] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"archive", _name}, parser.currentFileName(), debugInfo.start);
        }
        if (isRuntimeExpr(value.toString())) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_ACTION, new Object[]{value.toString(), e.toString(), _name}, parser.currentFileName(), debugInfo.start);
        }
      } else if (e.equals(attributeNames[5])) {  //   height
        if (flags[5]) {
          flags[5] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"height", _name}, parser.currentFileName(), debugInfo.start);
        }
      } else if (e.equals(attributeNames[6])) {
        if (flags[6]) {
          flags[6] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"hspace", _name}, parser.currentFileName(), debugInfo.start);
        }
        if (isRuntimeExpr(value.toString())) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_ACTION, new Object[]{value.toString(), e.toString(), _name}, parser.currentFileName(), debugInfo.start);
        }
      } else if (e.equals(attributeNames[7])) {
        if (flags[7]) {
          flags[7] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"jreversion", _name}, parser.currentFileName(), debugInfo.start);
        }
        if (isRuntimeExpr(value.toString())) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_ACTION, new Object[]{value.toString(), e.toString(), _name}, parser.currentFileName(), debugInfo.start);
        }
        //ok
      } else if (e.equals(attributeNames[8])) {
        if (flags[8]) {
          flags[8] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"name", _name}, parser.currentFileName(), debugInfo.start);
        }
        if (isRuntimeExpr(value.toString())) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_ACTION, new Object[]{value.toString(), e.toString(), _name}, parser.currentFileName(), debugInfo.start);
        }
        //ok or may perform a check for valid url
      } else if (e.equals(attributeNames[9])) {
        if (flags[9]) {
          flags[9] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"vspace", _name}, parser.currentFileName(), debugInfo.start);
        }
        if (isRuntimeExpr(value.toString())) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_ACTION, new Object[]{value.toString(), e.toString(), _name}, parser.currentFileName(), debugInfo.start);
        }
      } else if (e.equals(attributeNames[10])) { //width
        if (flags[10]) {
          flags[10] = false;
        } else {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_ACTION, new Object[]{value.toString(), e.toString(), _name}, parser.currentFileName(), debugInfo.start);
        }

        //ok
      } else if (e.equals(attributeNames[11])) {
        if (flags[11]) {
          flags[11] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"nspluginurl", _name}, parser.currentFileName(), debugInfo.start);
        }
        if (isRuntimeExpr(value.toString())) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_ACTION, new Object[]{value.toString(), e.toString(), _name}, parser.currentFileName(), debugInfo.start);
        }
        //ok
      } else if (e.equals(attributeNames[12])) {
        if (flags[12]) {
          flags[12] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"iepluginurl", _name}, parser.currentFileName(), debugInfo.start);
        }
        if (isRuntimeExpr(value.toString())) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_ACTION, new Object[]{value.toString(), e.toString(), _name}, parser.currentFileName(), debugInfo.start);
        }
        //ok
      } else if (e.equals(attributeNames[13])) {
        // title attribute
        if (flags[13]) {
          flags[13] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"title", _name}, parser.currentFileName(), debugInfo.start);
        }
        if (isRuntimeExpr(value.toString())) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_ACTION, new Object[]{value.toString(), e.toString(), _name}, parser.currentFileName(), debugInfo.start);
        }
        //ok
      } else if (e.equals(attributeNames[14])) { 
        /* 
         * "mayscript" attribute. Defined since JSP 2.0. Not mentioned in last HTML spec (4.01) but found in old <applet> HTML tag.
         * We generate only OBJECT and EMBED tags (recommended in JSP spec) so this attribute will be parsed but not included in andy output. 
         */ 
        if (flags[14]) {
          flags[14] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{MAYSCRIPT_STR, _name}, parser.currentFileName(), debugInfo.start);
        }

        if (!("true".equals(value.toString()) || "false".equals(value.toString()))) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_ACTION, new Object[]{value, MAYSCRIPT_STR, _name}, parser.currentFileName(), debugInfo.start);
        }
      } else {
        throw new JspParseException(JspParseException.UNRECOGNIZED_ATTRIBUTE_IN_ACTION, new Object[]{e, _name}, parser.currentFileName(), debugInfo.start);
      }
    } 

    if (flags[0] || flags[1]) {
      throw new JspParseException(JspParseException.MISSING_ATTRIBUTE_OR_ATTRIBUTE_IN_ACTION, new Object[]{"type", "code", _name}, parser.currentFileName(), debugInfo.start);
    }
  }

}

