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
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.taglib;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Stack;

import javax.servlet.jsp.tagext.TagVariableInfo;
import javax.servlet.jsp.tagext.VariableInfo;

import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Attribute;

/**
 * Common stuff for use with TagBegin and TagEndGenerators.
 *
 * @author Maria Jurova
 */
abstract class TagGeneratorBase {

  /**
   * A Stack containing TagVariableData objects for the actions used in the jsp
   */
  protected Stack<TagVariableData> tagHandlerStack = null;
  /**
   * Contains names of the references to action classes into the generated java file
   */
  protected HashMap<String, Integer> tagVarNumbers = null;

  private TagVariableData parentTag = null;
  protected boolean isSimpleTag = false;
  protected boolean isInMethod = false;

  /**
   * Adds TagVariableData for a new action tag found in a jsp. This class is used when
   * the jsp page is parsing and generating a java file.
   *
   * @param   tvd  TagVariableData for some tag
   */
  protected void tagBegin(TagVariableData tvd) {
    parentTag = (TagVariableData)tagHandlerStack.peek();
    tagHandlerStack.push(tvd);
  }

  /**
   * Returns the last added TagVariableData in the stack.
   *
   * @return     the last added TagVariableData
   */
  protected TagVariableData tagEnd() {
    TagVariableData top = (TagVariableData)tagHandlerStack.pop();
    parentTag = (TagVariableData)tagHandlerStack.peek();
    return top;
  }

  /**
   * Returns the last added TagVariableData in the stack but doesn't removed it from the stack.
   *
   * @return     the last added TagVariableData
   */
  public TagVariableData topTag() {
    if (tagHandlerStack.empty()) {
      return null;
    }

    return (TagVariableData) tagHandlerStack.peek();
  }

  protected TagVariableData getParentTag() {
    return parentTag;
  }

  /**
   * Generates and return a name for action used in jsp file which will be refference to the main action class
   * in a generated java file for this jsp.
   *
   * @param   prefix  prefix of the tag library in the jsp
   * @param   shortTagName  name of the action
   * @return     ganerated name of the main action class reference
   */
  protected String getTagVarName(String prefix, String shortTagName) {
    synchronized (tagVarNumbers) {
      String tag = prefix + ":" + shortTagName;
      String varName = replaceChars(prefix) + "_" + replaceChars(shortTagName) + "_";

      if (tagVarNumbers.get(tag) != null) {
        Integer i = tagVarNumbers.get(tag);
        varName = varName + i.intValue();
        tagVarNumbers.put(tag, new Integer(i.intValue() + 1));
        return varName;
      } else {
        tagVarNumbers.put(tag, new Integer(1));
        return varName + "0";
      }
    }
  }

  private String replaceChars(String namePart) {
    StringBuffer namePartOK = new StringBuffer();

    for (int i = 0; i < namePart.length(); i++) {
      char c = namePart.charAt(i);

      if (!((i == 0) ? Character.isJavaIdentifierStart(c) : Character.isJavaIdentifierPart(c))) {
        namePartOK.append('_');
        namePartOK.append(Integer.toHexString(c));
        namePartOK.append('_');
      } else {
        namePartOK.append(c);
      }
    }
    return namePartOK.toString();
  }

  private boolean variableAlreadyDeclared(String variable) {
    for (Enumeration tags = tagHandlerStack.elements(); tags.hasMoreElements(); ) {
      return ((TagVariableData)tags.nextElement()).declaredVariables.contains(variable);
    }
    return false;
  }

  /**
   * Declares variables used from some section into the generated java file. Writes them
   * directly into a StringBuffer representing generated java file.
   *
   * @param   writer  a StringBuffer representing the generating java file
   * @param   vi  info about used variables from this action that will be declared
   * @param   declare  if variables will be declared for the first time into a java file
   * @param   update  if the variables are alredy defined into a java file
   * @param   scope  scope of the variables
   * @param   ident  ident that will be written before variables in java file
   */
  protected void declareVariables(StringBuffer writer, VariableInfo[] vi, boolean declare, boolean update, int scope, String ident) {
    if (vi != null) {
      for (int i = 0; i < vi.length; i++) {
        if (vi[i].getScope() == scope) {
          if (vi[i].getDeclare() == true && declare == true) {
            if (!variableAlreadyDeclared(vi[i].getVarName())) {
              // if the variable has already been declared in a parent tag
              // then don't declare it once again
              writer.append(ident + vi[i].getClassName() + " " + vi[i].getVarName() + ";" + "\n");

              /*
               * save a list of variables that are already declared so that other tags with
               * the same variables don't try to declare them again
               */
              if (vi[i].getScope() == VariableInfo.AT_BEGIN || vi[i].getScope() == VariableInfo.AT_END) {
                parentTag.declaredVariables.add(vi[i].getVarName());
              }
//            } else if (isInMethod || (getParentTag() != null && getParentTag().isSimpleTag && !getParentTag().redeclaredVariables.contains(vi[i].getVarName()))) {
            } else if (isSimpleTag && getParentTag() != null && getParentTag().isSimpleTag && !getParentTag().redeclaredVariables.contains(vi[i].getVarName())) {
              // if inside an invokeN method for SimpleTag, we have to re-declare and update it
              // same for tags that have no body and are executed in separate method
              writer.append(ident + vi[i].getClassName() + " " + vi[i].getVarName() + ";" + "\n");
              update = true;
//              if (!isInMethod) {
                getParentTag().redeclaredVariables.add(vi[i].getVarName());
//              }
            }
          }

          if (update == true) {
            if ("String".equals(vi[i].getClassName()) || "java.lang.String".equals(vi[i].getClassName())) {
              writer.append(ident + new String(vi[i].getVarName() + " = (" + vi[i].getClassName() + ") _jspx_pageContext.getAttribute(" + quoteString(vi[i].getVarName()) + ");" + "\n"));
            } else if ("int".equals(vi[i].getClassName())) {
              writer.append(ident + new String(vi[i].getVarName() + " = " + "Integer.valueOf((String) _jspx_pageContext.getAttribute(" + quoteString(vi[i].getVarName()) + ")).intValue();" + "\n"));
            } else if ("long".equals(vi[i].getClassName())) {
              writer.append(ident + new String(vi[i].getVarName() + " = " + "Long.valueOf((String) _jspx_pageContext.getAttribute(" + quoteString(vi[i].getVarName()) + ")).longValue();" + "\n"));
            } else if ("float".equals(vi[i].getClassName())) {
              writer.append(ident + new String(vi[i].getVarName() + " = " + "Float.valueOf((String) _jspx_pageContext.getAttribute(" + quoteString(vi[i].getVarName()) + ")).floatValue();" + "\n"));
            } else if ("double".equals(vi[i].getClassName())) {
              writer.append(ident + new String(vi[i].getVarName() + " = " + "Double.valueOf((String) _jspx_pageContext.getAttribute(" + quoteString(vi[i].getVarName()) + ")).doubleValue();" + "\n"));
            } else if ("boolean".equals(vi[i].getClassName())) {
              writer.append(ident + new String(vi[i].getVarName() + " = " + "Boolean.valueOf((String) _jspx_pageContext.getAttribute(" + quoteString(vi[i].getVarName()) + ")).booleanValue();" + "\n"));
            } else if ("char".equals(vi[i].getClassName())) {
              writer.append(ident + new String("if (((String)_jspx_pageContext.getAttribute(" + quoteString(vi[i].getVarName()) + ")).toCharArray().length > 1 ) { \n"));
              writer.append(ident + new String("throw new JspException(\"Unable to parse attribute to char\");\n"));
              writer.append(ident + new String("}\n"));
              writer.append(ident + new String(vi[i].getVarName() + " = " + "((String)_jspx_pageContext.getAttribute(" + quoteString(vi[i].getVarName()) + ")).toCharArray()[0];" + "\n"));
            } else if ("Character".equals(vi[i].getClassName())) {
              writer.append(ident + new String("if (((String)_jspx_pageContext.getAttribute(" + quoteString(vi[i].getVarName()) + ")).toCharArray().length > 1 ) { \n"));
              writer.append(ident + new String("throw new JspException(\"Unable to parse attribute to char\");\n"));
              writer.append(ident + new String("}\n"));
              writer.append(ident + new String(vi[i].getVarName() + " = " + "new Character(((String)_jspx_pageContext.getAttribute(" + quoteString(vi[i].getVarName()) + ")).toCharArray()[0]);" + "\n"));
            } else {
              writer.append(ident + new String(vi[i].getVarName() + " = " + "(" + vi[i].getClassName() + ")" + "_jspx_pageContext.getAttribute(" + quoteString(vi[i].getVarName()) + ");" + "\n"));
            }
          }
        }
      }
    }
  }

  protected void declareVariables(StringBuffer writer, TagVariableInfo[] vi, boolean declare, boolean update, int scope, String ident, Attribute[] attrs) {
    if (vi != null) {
      boolean declareOld = declare;
      for (int i = 0; i < vi.length; i++) {
        if (vi[i].getScope() == scope) {
          String className = vi[i].getClassName();
          String name = vi[i].getNameGiven();
          declare = declareOld;
          if (name == null) {
            if (attrs != null) {
              for (int j = 0; j < attrs.length; j++) {
                String attrName = attrs[j].name.toString();
                if (attrName.equals(vi[i].getNameFromAttribute())) {
                  name = attrName;
                }
              }
            }
          }

          if (declare) {
            if (variableAlreadyDeclared(name)) {
//              if (isInMethod || (getParentTag() != null && getParentTag().isSimpleTag && !getParentTag().redeclaredVariables.contains(name))) {
              if (isSimpleTag && getParentTag() != null && getParentTag().isSimpleTag && !getParentTag().redeclaredVariables.contains(name)) {
                // if inside an invokeN method for SimpleTag, we have to re-declare and update it,
                // but only locally for this method
                update = true;
//                if (!isInMethod) {
                  getParentTag().redeclaredVariables.add(name);
//                }
              } else {
                declare = false;
              }
            } else {
              /*
               * save a list of variables that are already declared so that other tags with
               * the same variables don't try to declare them again
               */
              if (vi[i].getScope() == VariableInfo.AT_BEGIN || vi[i].getScope() == VariableInfo.AT_END) {
                parentTag.declaredVariables.add(name);
              }
            }
          }

          doDeclareVariables(writer, className, name, declare&vi[i].getDeclare(), update, ident);
        }
      }
    }
  }

  protected void doDeclareVariables(StringBuffer writer, String className, String name, boolean declare, boolean update, String ident) {
    if (declare == true) {
      writer.append(ident + className + " " + name + ";" + "\n");
    }

    if (update == true) {
      if ("String".equals(className) || "java.lang.String".equals(className)) {
        writer.append(ident + name + " = (" + className + ") _jspx_pageContext.getAttribute(" + quoteString(name) + ");" + "\n");
      } else if ("int".equals(className)) {
        writer.append(ident + name + " = " + "Integer.valueOf((String) _jspx_pageContext.getAttribute(" + quoteString(name) + ")).intValue();" + "\n");
      } else if ("long".equals(className)) {
        writer.append(ident + name + " = " + "Long.valueOf((String) _jspx_pageContext.getAttribute(" + quoteString(name) + ")).longValue();" + "\n");
      } else if ("float".equals(className)) {
        writer.append(ident + name + " = " + "Float.valueOf((String) _jspx_pageContext.getAttribute(" + quoteString(name) + ")).floatValue();" + "\n");
      } else if ("double".equals(className)) {
        writer.append(ident + name + " = " + "Double.valueOf((String) _jspx_pageContext.getAttribute(" + quoteString(name) + ")).doubleValue();" + "\n");
      } else if ("boolean".equals(className)) {
        writer.append(ident + name + " = " + "Boolean.valueOf((String) _jspx_pageContext.getAttribute(" + quoteString(name) + ")).booleanValue();" + "\n");
      } else if ("char".equals(className)) {
        writer.append(ident + "if (((String)_jspx_pageContext.getAttribute(" + quoteString(name) + ")).toCharArray().length > 1 ) { \n");
        writer.append(ident + "throw new JspException(\"Unable to parse attribute to char\");\n");
        writer.append(ident + "}\n");
        writer.append(ident + name + " = " + "((String)_jspx_pageContext.getAttribute(" + quoteString(name) + ")).toCharArray()[0];" + "\n");
      } else if ("Character".equals(className)) {
        writer.append(ident + "if (((String)_jspx_pageContext.getAttribute(" + quoteString(name) + ")).toCharArray().length > 1 ) { \n");
        writer.append(ident + "throw new JspException(\"Unable to parse attribute to char\");\n");
        writer.append(ident + "}\n");
        writer.append(ident + name + " = " + "new Character(((String)_jspx_pageContext.getAttribute(" + quoteString(name) + ")).toCharArray()[0]);" + "\n");
      } else {
        writer.append(ident + name + " = " + "(" + className + ")" + "_jspx_pageContext.getAttribute(" + quoteString(name) + ");" + "\n");
      }
    }
  }

  /**
   * Generates a valid in java quoted String.
   *
   * @param   s  a String containing quotes
   * @return     a valid java representation of this String
   */
  protected String quoteString(String s) {
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
      
      //already escaped characters should not be escaped again
      // <my:tag name="some \"value"> - should not be escaped again
      if (ch == '\\' && i + 1 < len) {
          char nextChar = s.charAt(i+1);
          if( nextChar != 'n' ||  
              nextChar != 't' ||
              nextChar != 'b' ||
              nextChar != 'f' ||
              nextChar != 'r' ||
              nextChar != '"' ||
              nextChar != '\'' ||
              nextChar != '\\' ||
              nextChar != '\uDDDD' ){
            sb.append(ch);
            sb.append(nextChar);
            i++;
            continue;
        }
      }

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

}


