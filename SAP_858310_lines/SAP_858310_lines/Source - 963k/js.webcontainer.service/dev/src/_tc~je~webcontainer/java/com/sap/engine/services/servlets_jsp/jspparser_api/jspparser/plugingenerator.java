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

import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Element;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Generates java code into a java file of the jsp for the tags using applets.
 *
 * @author Maria Jurova
 */
public class PluginGenerator {

  /**
   * Default ID of the class of the applet
   */
  public String IE_CLASS_ID = "clsid:8AD9C840-044E-11D1-B3E9-00805F499D93";
  /**
   * Default codebase of the applet
   */
  public static final String IE_PLUGIN_URL = "http://java.sun.com/products/plugin/1.2.2/jinstall-1_2_2-win.cab#Version=1,2,2,0";
  /**
   * Default plugins page
   */
  public static final String NS_PLUGIN_URL = "http://java.sun.com/products/plugin/";
  /**
   * Used for undermining of the source
   */
  private String indent = "      ";
  /**
   * Attributes of the tag
   */
  private Hashtable attrs = null;
  /**
   * Parameters of the applet
   */
  private Hashtable param = null;
  /**
   * Some fallbacks of the applet
   */
  private String fallback = null;
  /**
   * ID of the class of the applet
   */
  private String ieClassId = null;

  private Element element = null;
  /**
   * Initiates the instance with applet's parameters, attributes of the tag and fallback of the applet.
   *
   * @param   attrs  attributes of the tag
   * @param   param  parameters of the applet
   * @param   fallback  fallback for the applet
   */
  public PluginGenerator(Hashtable attrs, Hashtable param, String fallback, Element element) {
    this.attrs = attrs;
    this.param = param;
    this.fallback = fallback;
    this.element = element;
  }

  /**
   * Initiates the class ID of the applet class.
   *
   * @param   ieClassId  ID of the applet class
   * @exception   JspParseException
   */
  public void init(String ieClassId) throws JspParseException {
    if (ieClassId == null) {
      this.ieClassId = IE_CLASS_ID;
    } else {
      this.ieClassId = ieClassId;
    }
  }

  /**
   * Generates a necessary code into a java file after encountering a tag useing applet.
   *
   * @param   writer  A StringBuffer containing the source of the generating java file
   * @exception   JspParseException
   */
  public void generate(StringBuffer writer) throws JspParseException {
    String type = getAttribute("type");
    String code = getAttribute("code");
    String codebase = getAttribute("codebase");
    String archive = getAttribute("archive");
    String jreversion = getAttribute("jreversion");
    String nspluginurl = getAttribute("nspluginurl");
    String iepluginurl = getAttribute("iepluginurl");

    String name = getAttribute("name");
    String title = getAttribute("title");
    /* 
     * "mayscript" jsp:plugin attribute clarification. It is defined since JSP 2.0. Not mentioned in last HTML spec (4.01) but found in old <applet> HTML tag.
     * We generate only OBJECT and EMBED tags (recommended in JSP spec) so this attribute will be parsed but not included in andy output.
     * 
     * commented: String mayscript = getAttribute("mayscript");
     */

    if (type == null) {
      throw new JspParseException(JspParseException.NO_ATTRIBUTE, new Object[]{"type"});
    }

    if (code == null) {
      throw new JspParseException(JspParseException.NO_ATTRIBUTE, new Object[]{"code"});
    }

    writer.append(indent + "/*Code generated for plugin*/" + "\n");
    // start <OBJECT> html tag generation
    writer.append(indent + "out.println (\"<OBJECT classid=\\\"");
    writer.append(ieClassId);
    writer.append("\\\"");
    /* add/generate attributes that have direct 1:1 correspondence between <jsp:plugin> 
      tag and generated html <OBJECT> and <EMBED> tags 
     */
    generateCommon(writer);
    writer.append(" codebase=\\\"");

    if (iepluginurl == null) {
      writer.append(IE_PLUGIN_URL);
    } else {
      writer.append(iepluginurl);
    }


    writer.append("\\\"");
    if (name != null) {
      writer.append(" name=\\\"" + name + "\\\"");
    }
    if (title != null) {
      writer.append(" title=\\\"" + title + "\\\"");
    }
    
    writer.append(">\");");
    writer.append("\n");
    writer.append(indent + "out.println (\"<PARAM name=\\\"java_code\\\"");
    writer.append(" value=\\\"");
    writer.append(code);
    writer.append("\\\"");
    writer.append(">\");");
    writer.append("\n");

    if (codebase != null) {
      writer.append(indent + "out.println (\"<PARAM name=\\\"java_codebase\\\"");
      writer.append(" value=\\\"");
      writer.append(codebase);
      writer.append("\\\"");
      writer.append(">\");");
      writer.append("\n");
    }

    if (archive != null) {
      writer.append(indent + "out.println (\"<PARAM name=\\\"java_archive\\\"");
      writer.append(" value=\\\"");
      writer.append(archive);
      writer.append("\\\"");
      writer.append(">\");");
      writer.append("\n");
    }

    writer.append(indent + "out.println (\"<PARAM name=\\\"type\\\"");
    writer.append(" value=\\\"");

    if (type.equals("applet")) {
      writer.append("application/x-java-applet;");
    } else if (type.equals("bean")) {
      writer.append("application/x-java-bean;");
    }

    if (jreversion != null) {
      writer.append("version=");
      writer.append(jreversion);
    }

    writer.append("\\\"");
    writer.append(">\");");
    writer.append("\n");
    Enumeration e = null;
    //String value [] = null;
    String value = null;
    String key = null;

    // output generation for <jsp:param> parameters 
    if (param != null) {
      e = param.keys();

      while (e.hasMoreElements()) {
        key = (String) e.nextElement();
        //value = (String[]) param.get(key);
        value = (String) param.get(key);
        writer.append(indent + "out.println (\"<PARAM name=\\\"");

        if (key.equalsIgnoreCase("object")) {
          writer.append("java_object");
        } else if (key.equalsIgnoreCase("type")) {
          writer.append("java_type");
        } else {
          writer.append(key);
        }

        writer.append("\\\"");
        writer.append(" value=");
        writer.append("\\\""); // open quote for value
        if (element.isRuntimeExpr(value)) {
          writer.append("\"+ " + element.evaluateExpression(value) + " +\"");
        } else {
          writer.append(value);
        }
        writer.append("\\\""); // closing quote for value
        writer.append(">\");");
        writer.append("\n");
      }
    }

    // start <EMBED> tag html generation 
    writer.append(indent + "out.println (\"<COMMENT>\");\n");
    writer.append(indent + "out.print(\"<EMBED type=\\\"");

    if (type.equals("applet")) {
      writer.append("application/x-java-applet;");
    } else if (type.equals("bean")) {
      writer.append("application/x-java-bean;");
    }

    if (jreversion != null) {
      writer.append("version=");
      writer.append(jreversion);
    }

    writer.append("\\\" ");
    /* add/generate attributes that have direct 1:1 correspondence between <jsp:plugin> 
      tag and generated html <OBJECT> and <EMBED> tags 
     */
    generateCommon(writer);
    writer.append("pluginspage=\\\"");

    if (nspluginurl == null) {
      writer.append(NS_PLUGIN_URL);
    } else {
      writer.append(nspluginurl);
    }

    writer.append("\\\" ");
    writer.append("java_code=\\\"");
    writer.append(code);
    writer.append("\\\" ");

    if (codebase != null) {
      writer.append("java_codebase=\\\"");
      writer.append(codebase);
      writer.append("\\\" ");
    }

    if (archive != null) {
      writer.append("java_archive=\\\"");
      writer.append(archive);
      writer.append("\\\" ");
    }

    if (param != null) {
      e = param.keys();
      key = null;
      value = null;

      while (e.hasMoreElements()) {
        key = (String) e.nextElement();
        //value = (String[]) param.get (key);
        value = (String) param.get(key);

        if (key.equalsIgnoreCase("object")) {
          writer.append("java_object");
        } else if (key.equalsIgnoreCase("type")) {
          writer.append("java_type");
        } else {
          writer.append(key);
        }

        writer.append("=\\\""); // opening quotes for attribute value
        if (element.isRuntimeExpr(value)) {
          writer.append("\"+ " + element.evaluateExpression(value) + " +\"");
        } else {
          writer.append(value);
        }
        writer.append("\\\" "); // closing quotes for attribute value
      }
    }

    writer.append("/>\\n\");"); // end of EMBED tag generation
    writer.append("\n");
    writer.append(indent + "out.println (\"<NOEMBED>\");\n");
    if (fallback != null) {
      fallback = quoteString(fallback);
      writer.append(indent + "out.println (");
      writer.append(fallback);
      writer.append(");");
      writer.append("\n");
    }
    writer.append(indent + "out.println (\"</NOEMBED>\");\n");
    writer.append(indent + "out.println (\"</COMMENT>\");\n");


    writer.append(indent + "out.println (\"</OBJECT>\");\n");
  }

  /**
   * Generates common code necessary for starting applet as width, hight, align, vspace, hspace.
   * These attributes have one to one correspondence between &lt;jsp:plugin&lt; tag and the attributes in the generated HTML code.
   *
   * @param   writer  A StringBuffer containing the source of the generating java file
   */
  public void generateCommon(StringBuffer writer) throws JspParseException {
    String align = getAttribute("align");
    String width = getAttribute("width");
    String height = getAttribute("height");
    String hspace = getAttribute("hspace");
    String vspace = getAttribute("vspace");

    if (width != null) {
      writer.append(" width=\\\""); // attribute allows runtime expression
      if (element.isRuntimeExpr(width)) {
        writer.append("\"+" + element.evaluateExpression(width) + "+\"");
      } else {
        writer.append(width);
      }
      writer.append("\\\" ");
    }

    if (height != null) {
      writer.append(" height=\\\""); // attribute allows runtime expression
      if (element.isRuntimeExpr(height)) {
        writer.append("\"+" + element.evaluateExpression(height) + "+\"");
      } else {
        writer.append(height);
      }
      writer.append("\\\" ");
    }

    if (hspace != null) {
      writer.append(" hspace=\\\"");
      writer.append(hspace);
      writer.append("\\\" ");
    }

    if (vspace != null) {
      writer.append(" vspace=\\\"");
      writer.append(vspace);
      writer.append("\\\" ");
    }

    if (align != null) {
      writer.append(" align=\\\"");
      writer.append(align);
      writer.append("\\\" ");
    }
  }

  /**
   * Transformes some String to a valid java expression.
   *
   * @param   s  some String
   * @return     a valid java expression of the String
   */
  public String quoteString(String s) {
    if (s == null) {
      return "null";
    }

    if (s.indexOf('"') < 0 && s.indexOf('\\') < 0 && s.indexOf('\n') < 0 && s.indexOf('\r') < 0) {
      return "\"" + s + "\"";
    }

    StringBuffer sb = new StringBuffer();
    int len = s.length();
    sb.append('"');

    for (int i = 0; i < len; i++) {
      char ch = s.charAt(i);

      if (ch == '\\' && i + 1 < len) {
        sb.append('\\');
        sb.append('\\');
        sb.append(s.charAt(++i));
      } else if (ch == '"') {
        sb.append('\\');
        sb.append('"');
      } else if (ch == '\n') {
        sb.append("\\n");
      } else if (ch == '\r') {
        sb.append("\\r");
      } else {
        sb.append(ch);
      }
    }

    sb.append('"');
    return sb.toString();
  }

  /**
   * Returns a value of the attribute with name name.
   *
   * @param   name  name of the attribute
   * @return     value of the attribute
   */
  public String getAttribute(String name) {
    return (attrs != null) ? (String) attrs.get(name) : null;
  }

}

