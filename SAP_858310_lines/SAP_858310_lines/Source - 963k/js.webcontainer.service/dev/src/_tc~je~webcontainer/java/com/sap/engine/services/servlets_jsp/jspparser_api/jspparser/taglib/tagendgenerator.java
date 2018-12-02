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

import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Attribute;
import com.sap.engine.services.servlets_jsp.server.lib.StringUtils;

import java.util.*;

import javax.servlet.jsp.tagext.*;

/**
 * Custom tag support. Generates necessary code into a java file after encountering a closing
 * action tag.
 *
 * @author Maria Jurova
 */
public class TagEndGenerator extends TagGeneratorBase {

  /**
   * Current iddent append in the beginning of every line
   */
  private String ident = "				";
  /**
   * A prefix with which this tag library is available into a jsp file
   */
  private String prefix = null;
  /**
   * A name of the current tag action
   */
  private String shortTagName = null;
  /**
   * Contains the all TagCache object generated for the actions used in jsp file.
   */
  private TagLibraries tli = null;
  /**
   * Info of the current action
   */
  private TagInfo ti = null;
  /**
   * Attributes of the current action
   */
  private Attribute[] attrs = null;

  /**
   * A name of the action used in jsp file which will be refference to the main action class
   * in a generated java file for this jsp.
   */
  private String baseVarName = null;
  private JspPageInterface parser = null;
  
  /**
   * Initiates the instance with data of the current action.
   *
   * @param   prefix  Name of the tag library into a jsp
   * @param   shortTagName  Name of the action
   * @param   attrs  Attributes of the action
   * @param   tli  TagLibraries of the jsp
   * @param   ti  TagInfo of the current action
   */
  public TagEndGenerator(String prefix, String shortTagName, Attribute[] attrs,  TagInfo ti, JspPageInterface parser) {
    this.prefix = prefix;
    this.shortTagName = shortTagName;
    this.tli = parser.getTagLibraries();
    this.ti = ti;
    this.attrs = attrs;
    this.tagHandlerStack = parser.getTagsStack();
    this.tagVarNumbers = parser.getTagVarNumbers();
    this.baseVarName = getTagVarName(prefix, shortTagName);
    this.parser = parser;
  }

  /**
   * Generates necessary code into a java file after encountering a closing action tag.
   *
   * @param   writer  A StringBuffer containing the source of the generating java file
   * @param   phase  if some other actions must be done before generating. For now nothing.
   */
  public void generate(StringBuffer writer, StringBuffer fragmentBody, Class phase, boolean hasGeneratedMethod, boolean hasBody, boolean isSingleTag, boolean isTagFile) {
    writer.append(ident + "// TagEndGenerator - start[").append(prefix+":"+shortTagName).append("]\n");

    TagVariableData tvd = tagEnd();
    String thVarName = tvd.tagHandlerInstanceName;
    String evalVarName = tvd.tagEvalVarName;
    TagData tagData = new TagData((Object[][])null);
    if (attrs != null) {
      for (int i = 0; i < attrs.length; i++) {
        tagData.setAttribute(attrs[i].name.toString(), attrs[i].value.toString());
      }
    }
    VariableInfo[] vi = ti.getVariableInfo(tagData);
    TagVariableInfo[] tvi = ti.getTagVariableInfos();
    Class tagHandlerClass = tli.getTagCache(prefix + ":" + shortTagName).getTagHandlerClass();
    boolean implementsBodyTag = BodyTag.class.isAssignableFrom(tagHandlerClass);
    boolean implementsIterationTag = IterationTag.class.isAssignableFrom(tagHandlerClass);
    isSimpleTag = SimpleTag.class.isAssignableFrom(tagHandlerClass);
    boolean implementsTryCatchFinally = TryCatchFinally.class.isAssignableFrom(tagHandlerClass);

    if (isSimpleTag) {
      if (fragmentBody != null) {
        fragmentBody.append("\t\t}\n");
      }
      insertInjectSting(writer, thVarName);
    } else {
      if (hasBody && !isSingleTag) {
        String evalVar = "_jspx_eval_DoAfterBody_" + baseVarName;
        ident = ident.concat("\t\t");

        if (implementsIterationTag) {
          ident = ident.concat("\t");
          writer.append(ident + "int " + evalVar + " = " + thVarName + ".doAfterBody();").append("\n");

          // AT_BEGIN & NESTED variables must be synchronized after
          // each call to doAfterBody()
          declareVariables(writer, vi, false, true, VariableInfo.AT_BEGIN, ident);
          declareVariables(writer, tvi, false, true, VariableInfo.AT_BEGIN, ident, attrs);
          declareVariables(writer, vi, false, true, VariableInfo.NESTED, ident);
          declareVariables(writer, tvi, false, true, VariableInfo.NESTED, ident, attrs);

          // evalVar is out of scope to check it out in the while() statement
          // it's too much effort to declare it in BeginTagGenerator, so we
          // take another approach
          writer.append(ident + "if (" + evalVar + " != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN) {").append("\n");
          writer.append(ident + "\tbreak;").append("\n");
          writer.append(ident + "}").append("\n");

          ident = ident.substring(0, ident.length() - 1);
          writer.append(ident + "} while (true);").append("\n");
        } else {
          writer.append(ident + "} while (false);").append("\n");
        }

//        declareVariables(writer, vi, false, true, VariableInfo.AT_BEGIN, ident);
//        declareVariables(writer, tvi, false, true, VariableInfo.AT_BEGIN, ident, attrs);

        if (implementsBodyTag) {
          ident = ident.substring(0, ident.length() - 1);
          writer.append(ident + "} finally {\n");
          ident = ident.concat("	");
          writer.append(ident + "if (" + evalVarName + " != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE)\n");
          ident = ident.concat("	");
          writer.append(ident + "out = _jspx_pageContext.popBody();\n");
          ident = ident.substring(0, ident.length() - 2);
          writer.append(ident + "}\n");
        }

        ident = ident.substring(0, ident.length() - 1);
        writer.append(ident + "}\n");
      }
      writer.append(ident + "if (" + thVarName + ".doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {\n");
      ident = ident.concat("	");
      if (isTagFile) {
        writer.append(ident + "throw new SkipPageException();\n");
      } else {
        if (hasGeneratedMethod) {
          writer.append(ident + "return true;\n");
        } else if (parser.getJspBodyVariable() != null) {
          writer.append(ident + "labelResult:;\n");
        } else {
          writer.append(ident + "return;\n");
        }
      }
      writer.append(ident + "}\n");
      ident = ident.substring(0, ident.length() - 2);

      if (implementsTryCatchFinally) {
        writer.append(ident + "} catch (Throwable _jspx_exception) {");
        ident = ident.concat("  ");
        writer.append(ident + thVarName + ".doCatch(_jspx_exception);");
        ident = ident.substring(0, ident.length() - 2);
      }

      writer.append(ident + "} finally {\n");
      ident = ident.concat("  ");

      if (implementsTryCatchFinally) {
        writer.append(ident + thVarName + ".doFinally();");
      }
      insertInjectSting(writer, thVarName);
      writer.append(ident + thVarName + ".release();\n");
      ident = ident.substring(0, ident.length() - 2);
      writer.append(ident + "}\n");
    }
    // AT_BEGIN & AT_END variables are synchronized after both classic and SimpleTags
    declareVariables(writer, vi, false, true, VariableInfo.AT_BEGIN, ident);
    declareVariables(writer, tvi, false, true, VariableInfo.AT_BEGIN, ident, attrs);

    declareVariables(writer, vi, true, true, VariableInfo.AT_END, ident);
    declareVariables(writer, tvi, true, true, VariableInfo.AT_END, ident, attrs);

    if (hasGeneratedMethod) {
      writer.append("\t\treturn false;\r\n\t}\r\n");
    }

    writer.append(ident + "// TagEndGenerator - end [").append(prefix+":"+shortTagName).append("]\n");

  }
  
  /**
   * Writes the code for injecting the tag hadler instance.
   * @param writer - 
   * @param thVarName - the name of the tag handler variable
   */
  private void insertInjectSting(StringBuffer writer, String thVarName){     
    
    if (StringUtils.greaterThan(parser.getParserParameters().getJspConfigurationProperties().getSpecificationVersion(), 2.4)) {
      writer.append(ident).append("_jspx_resourceInjector.invokePreDestroyMethod(").append(thVarName).append(");").append("\n");
    }
  }

}

