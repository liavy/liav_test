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
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax;

import java.util.List;

import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.lib.jspruntime.FunctionMapperImpl;
import com.sap.engine.services.servlets_jsp.server.lib.StringUtils;

/*
 *
 * @author Ivo Simeonov
 * @version 4.0
 */
public class TemplateData extends Element {

  //chunks to this size
  public static final int MAXSIZE = 1024 * 32;
  /*
   * denotes the way of construction of this object
   */
  private boolean constructorCreated;
  /*
   * denotes if this Template is empty
   */
  private boolean isEmptyFlag = false;

  public String value = null;
  public boolean isRootTag = false;
  public boolean isTextTag = false;
  public boolean isTagDependent = false;

  /**
   * Constructs new template data
   *
   */
  public TemplateData() {
    constructorCreated = true;
    _name = "text";
  }
  /**
   * Constructs new template data
   *
   */
  public TemplateData(String val, boolean isRoot) {
    this.value = val;
    if (val != null && val.trim().length() == 0) {
      isEmptyFlag = true;
    }
    if (isRoot) {
      value = value.trim();
    }
    isRootTag = isRoot;
    constructorCreated = true;
    _name = "text";
  }
  /**
   * Constructs new template data
   *
   * @param   start  start position foe this data in the jsp
   * source
   * @param   end  end position foe this data in the jsp
   * source
   * @param   info  info for this element
   */
  public TemplateData(int start, int end, DebugInfo info) {
    startIndex = start;
    endIndex = end;
    debugInfo = info;
    elementType = Element.TEMPLATE_DATA;
    constructorCreated = true;
    _name = "text";
  }

  /**
   * Takes specific action coresponding to this jsp element
   * logic
   *
   */
  public void action(StringBuffer writer) throws JspParseException {
    String s = value;
    if (s == null) {
      s = parser.getChars(startIndex, endIndex);
    }
    s = parser.replaceTemplateTextQuotes(s);

    if (!isTextTag && s.trim().length() == 0) {
      isEmptyFlag = true;
      // ZARADI tapia CTS !!!
      if (parser.isXml()) {
        //debugInfo.writeEnd(writer);
        return;
      }
    }
    debugInfo.writeStart(writer, true);
    
    if( parser.isTagFile() ){    
      if( StringUtils.greaterThan(parser.getTagFileTLD().getRequiredVersion(), 2.0) &&  !parser.isDeferredSyntaxAllowedAsLiteral() && !parser.getIsELIgnored()){
        // 2.1
        if (ELExpression.checkForDeffered(s)) {
          throw new JspParseException(JspParseException.DEFERRED_SYNTAX_CANNOT_BE_USED_IN_TEMPLATE_TEXT);
        }
      }else{
        // 2.0
        // this is either old app or new app with deffered allowed as literal. 
      }
    }else {
      if( !parser.isDeferredSyntaxAllowedAsLiteral() && !parser.getIsELIgnored()){
        if (ELExpression.checkForDeffered(s)) { 
          throw new JspParseException(JspParseException.DEFERRED_SYNTAX_CANNOT_BE_USED_IN_TEMPLATE_TEXT);
        }        
      }
      if ("2.5".compareTo(parser.getJspProperty().getSpecificationVersion()) >= 0) {
        if(parser.isTrimDirectiveWhitespaces() && !parser.isXml() && s.trim().length() == 0) {
          debugInfo.writeEnd(writer);
          return;
        }
      }      
    }


    if (parser.getIsELIgnored() || isTagDependent) {
      doOldActionStyle(writer, s);
    } else {
      List<String> innerExpressions = InnerExpression.getInnerExpressions(s);
      for (int i=0; i < innerExpressions.size(); i++) {
        String iex = (String)innerExpressions.get(i);
        if (innerExpressions.size() == 1 && !iex.startsWith("${") ) {
          doOldActionStyle(writer, s);
          break;
        }
        if (iex.startsWith("${")) {
          String funcMapper = FunctionMapperImpl.getFunctionMapper(iex, parser);
          iex = InnerExpression.convertLiterals(iex);
          if (funcMapper == null) {
            writer.append("\t\t\tout.print(");
            writer.append("(java.lang.String) _jspx_pageContext.evaluateInternal(\""+StringUtils.escapeJavaCharacters(iex)+"\", java.lang.String.class, false, null)");
            writer.append(");").append("\r\n");
          } else {
            writer.append("\t\t\tout.print(");
            writer.append("(java.lang.String) _jspx_pageContext.evaluateInternal(\""+StringUtils.escapeJavaCharacters(iex)+"\", java.lang.String.class, false, " + funcMapper + ")");
            writer.append(");").append("\r\n");
          }
        } else {
          if (iex.startsWith("\\${")) {
            iex = InnerExpression.unescape$(iex);
          }
          writer.append("\t\t\tout.print(");
          writer.append('"');
          writer.append(StringUtils.escapeJavaCharacters(parser.replaceTemplateTextQuotes(iex)));
          writer.append('"');
          writer.append(");").append("\r\n");
        }
      }
    }

    debugInfo.writeEnd(writer);

  }

  public void action() throws JspParseException {
    action(parser.getScriptletsCode());
  }

  private void doOldActionStyle(StringBuffer writer, String s) {

    int mark = 0;
    int from = 0;
    int to = 0;
    int len = s.length();

    if (len > MAXSIZE) {
      while (mark < len) {
        from = mark;
        to = Math.min(mark + MAXSIZE, len);
        writer.append("\t\t\tout.print(\"").append(StringUtils.escapeJavaCharacters(s.substring(from, to))).append("\");").append("\r\n");
        mark = to;
      }
    } else {
      writer.append("\t\t\tout.print(\"").append(StringUtils.escapeJavaCharacters(s)).append("\");").append("\r\n");
    }
  }

  /**
   * This element hase no indent
   * @return  null
   */
  public char[] indent() {
    return null;
  }

  /**
   * Parses next tokens
   *
   * @param   parser parser to use
   * @return  newly created element
   */
  public Element parse(JspPageInterface parser) {
    this.parser = parser;

    if (constructorCreated) {
      return this;
    } else {
      //TO DO parsing to token or Specified Token
      return null;
    }
  }

  public void merge(TemplateData next) {
    endIndex = next.endIndex;
    debugInfo.end = next.debugInfo.end;
  }

  public boolean isEmpty() {
    return isEmptyFlag;
  }

  public String getClearString() {
    String s = parser.getChars(getStartIndex(), getEndIndex()).trim();

    if (s.startsWith("<![CDATA[")) {
      s = s.substring(9, s.length() - 3);
    }

    return s.trim();
  }

  public String getString(IDCounter id) {
    /*
     * JSP.6.2.3 Semantic model
     * The first step in processing a JSP document is to identify the
     * nodes of the document. Then, all textual nodes that have only white space are
     * dropped from the document; the only exception are nodes in a jsp:text element,
     * which are kept verbatim. 
     */
      String realValue = getValue();
      if( realValue == null ||  realValue.trim().equals(""))
        return "";
      return _default_prefix_tag_start + _name + getId(id) + ">" + getEscapedOnlyString() + _default_prefix_tag_end + _name + END;    
  }
  
  /**
   * Does the same as the getString(IDCounter id) except that the resulted String is not wrapped in <jsp:text>.
   * This is used in the XML view of the scriptlets, declarations and expressions.
   * See JSP.10.1.7 Declarations
   * @return
   */
  public String getEscapedOnlyString() {
    String result = value; 
    if (value == null) {
      result = parser.getChars(getStartIndex(), getEndIndex());
    }
    if( result == null ) {
      return "";
    }
    
    if( result.indexOf("<![CDATA[") < result.indexOf("]]>")){
      return  result ;
    } else {     
      return "<![CDATA[\n" + result + CDATA_END;
    }
  }

  public String getValue() {
    if (value == null) {
      return parser.getChars(getStartIndex(), getEndIndex());
    } else {
      return value;
    }
  }
}

