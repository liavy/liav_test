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

import javax.servlet.jsp.tagext.TagVariableInfo;
import javax.servlet.jsp.tagext.VariableInfo;

import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.TagInfoImpl;

/**
 *
 * @author Bojidar kadrev
 * @author Mladen Markov
 * @version 7.0
 */
public class VariableDirective extends JspDirective {

  /*
   * denotes the name of this type element
   */
  private static final String DIRECTIVE_NAME = "variable";
  /*
   * all attributes that this element can
   * accept
   */
  public static final String[] attributeNames = {"name-given", "name-from-attribute", "alias", "variable-class", "declare", "scope", "description"};
  /*
   * Must save attributes for this directive, to use in XML view of jsp.
   */
  public Attribute[] attributes0 = null;

  /**
   * Constructs new VariableDirective
   *
   */
  public VariableDirective() {
    elementType = Element.PAGE_DIRECTIVE;
    name = DIRECTIVE_NAME.toCharArray();
    _name = new String(DIRECTIVE_NAME);
  }

  /**
   * Takes specific action coresponding to this jsp element
   * logic
   *
   * @exception   JspParseException  thrown if error occures during
   * verification
   */
  public void action(StringBuffer buffer) throws JspParseException {
/*
    // extends
    if (parser.getPageActionTaken()) {
      return;
    } else {
      parser.setPageActionTaken(true);
      attributes = new Attribute[parser.getPageAttributes().size()];
      Enumeration e = parser.getPageAttributes().elements();
      int i = 0;

      while (e.hasMoreElements()) {
        attributes[i] = (Attribute) e.nextElement();
        i++;
      }
    }
*/
    TagInfoImpl tagInfo = parser.getTagInfo();
    if (tagInfo == null) {
      tagInfo = new TagInfoImpl();
      parser.setTagInfo(tagInfo);
    }

    String varName = null;
    String alias = null;
    String varNameFromAttribute = null;
    String varClass = "java.lang.String";
    int scope = VariableInfo.NESTED;
    boolean declare = true;
//    String description;

    // name-given
    String attributeValue = getAttributeValue("name-given");
    if (attributeValue != null) {
      varName = attributeValue;
    }

    // name-from-attribute
    attributeValue = getAttributeValue("name-from-attribute");
    if (attributeValue != null) {
      if (varName != null) {
        throw new JspParseException(JspParseException.ATTRIBUTE_CANNOT_BE_SPECIFIED_WHEN_OTHER_ATTRIBUTE_IS_ALSO_SPECIFIED, new Object[]{"name-from-attribute", _name, "name-given"}, parser.currentFileName(), debugInfo.start);
      }
      varNameFromAttribute = attributeValue;
    } else {
      if (varName == null) {
        throw new JspParseException(JspParseException.MISSING_ATTRIBUTES_IN_DIRECTIVE, new Object[]{"name-given", "name-from-attribute", _name}, parser.currentFileName(), debugInfo.start);
      }
    }

    // alias
    attributeValue = getAttributeValue("alias");
    if (attributeValue != null) {
      if (varName != null) {
        throw new JspParseException(JspParseException.UNRECOGNIZED_ATTRIBUTE_IN_DIRECTIVE, new Object[]{"alias", _name}, parser.currentFileName(), debugInfo.start);
      }
      if (varNameFromAttribute == null) {
        throw new JspParseException(JspParseException.MISSING_ATTRIBUTE_IN_DIRECTIVE, new Object[]{"alias", _name}, parser.currentFileName(), debugInfo.start);
      }
      alias = attributeValue;
    }

    // variable-class
    attributeValue = getAttributeValue("variable-class");
    if (attributeValue != null) {
      varClass = attributeValue;
    }

    // declare
    attributeValue = getAttributeValue("declare");
    if (attributeValue != null) {
      declare = Boolean.valueOf(attributeValue).booleanValue();
    }

    // scope
    attributeValue = getAttributeValue("scope");
    if (attributeValue != null) {
      if (attributeValue.equalsIgnoreCase("AT_BEGIN")) {
        scope = VariableInfo.AT_BEGIN;
      } else if (attributeValue.equalsIgnoreCase("AT_END")) {
        scope = VariableInfo.AT_END;
      } else if (attributeValue.equalsIgnoreCase("NESTED")) {
        scope = VariableInfo.NESTED;
      } else {
        throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_DIRECTIVE, new Object[]{attributeValue, "scope", _name}, parser.currentFileName(), debugInfo.start);
      }
    }

    switch (parser.getTagInfo().checkVariableNameGiven(varName)) {
      case TagInfoImpl.ERROR_DUPLICATE_NAME :
        throw new JspParseException(JspParseException.VARIABLE_DUPLICATE_NAME, new Object[]{varName}, parser.currentFileName(), debugInfo.start);
      case TagInfoImpl.ERROR_VARIABLE_NAME_CONFLICTS_WITH_ATTRIBUTE_NAME :
        throw new JspParseException(JspParseException.VARIABLE_NAME_CONFLICTS_WITH_ATTRIBUTE_NAME, new Object[]{varName}, parser.currentFileName(), debugInfo.start);
      case TagInfoImpl.ERROR_CONFLICTS_WITH_DYNAMIC_ATTRIBUTE_NAME :
        throw new JspParseException(JspParseException.VARIABLE_NAME_CONFLICTS_WITH_DYNAMIC_ATTRIBUTES, new Object[]{varName}, parser.currentFileName(), debugInfo.start);
      case TagInfoImpl.ERROR_OK :
        break;
      default:
        // should not happen
        throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_DIRECTIVE, new Object[]{varName, "name-given", _name}, parser.currentFileName(), debugInfo.start);
    }
    switch (parser.getTagInfo().checkVariableNameFromAttribute(varNameFromAttribute)) {
      case TagInfoImpl.ERROR_DUPLICATE_VARIABLE_NAMEFROMATTRIBUTE :
        throw new JspParseException(JspParseException.VARIABLE_DUPLICATE_NAMEFROMATTRIBUTE, new Object[]{varNameFromAttribute}, parser.currentFileName(), debugInfo.start);
      case TagInfoImpl.ERROR_OK :
        break;
      default:
        // should not happen
        throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_DIRECTIVE, new Object[]{varNameFromAttribute, "name-from-attribute", _name}, parser.currentFileName(), debugInfo.start);
    }
    switch (parser.getTagInfo().checkVariableAlias(alias)) {
      case TagInfoImpl.ERROR_DUPLICATE_VARIABLE_ALIAS :
        throw new JspParseException(JspParseException.VARIABLE_DUPLICATE_ALIAS, new Object[]{alias}, parser.currentFileName(), debugInfo.start);
      case TagInfoImpl.ERROR_VARIABLE_ALIAS_CONFLICTS_WITH_ATTRIBUTE_NAME :
        throw new JspParseException(JspParseException.VARIABLE_ALIAS_CONFLICTS_WITH_ATTRIBUTE_NAME, new Object[]{alias}, parser.currentFileName(), debugInfo.start);
      case TagInfoImpl.ERROR_OK :
        break;
      default:
        // should not happen
        throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_DIRECTIVE, new Object[]{alias, "alias", _name}, parser.currentFileName(), debugInfo.start);
    }

    tagInfo.addTagVariableInfo(new TagVariableInfo(alias == null ? varName : alias, varNameFromAttribute, varClass, declare, scope));
  }

  public void action() throws JspParseException {
    action(parser.getScriptletsCode());
  }

  /**
   * Verifies the attributes of this tag
   *
   * @exception   JspParseException  thrown if error occures
   * during verification
   */
  public void verifyAttributes() throws JspParseException {
    if (attributes == null) {
      throw new JspParseException(JspParseException.DIRECTIVE_MUST_HAVE_AT_LEAST_ONE_SPECIFIED_ATTRIBUTE, new Object[]{_name}, parser.currentFileName(), debugInfo.start);
    }
    //save attributes
    attributes0 = attributes;

    for (int i = 0; i < attributes.length; i++) {
      String attrName = attributes[i].name.toString();
      String value = attributes[i].value.toString();
      boolean valid = true;
      if (attributeNames[0].equals(attrName)) { // name-given
      } else if (attributeNames[1].equals(attrName)) { // name-from-attribute
      } else if (attributeNames[2].equals(attrName)) { // alias
      } else if (attributeNames[3].equals(attrName)) { // variable-class
      } else if (attributeNames[4].equals(attrName)) { // declare
        valid = "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
      } else if (attributeNames[5].equals(attrName)) { // scope
        valid = value.equalsIgnoreCase("AT_BEGIN") || value.equalsIgnoreCase("AT_END") || value.equalsIgnoreCase("NESTED");
      } else if (attributeNames[6].equals(attrName)) { // description
      } else {
        throw new JspParseException(JspParseException.UNRECOGNIZED_ATTRIBUTE_IN_DIRECTIVE, new Object[]{attrName, _name}, parser.currentFileName(), debugInfo.start);
      }
      if (!valid) {
        throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_DIRECTIVE, new Object[]{value, attrName, _name}, parser.currentFileName(), debugInfo.start);
      }
    }
  }

  public String getString(IDCounter id) {
    String s = _default_prefix_tag_start + "directive." + _name;
    String name = null;
    String value = null;

    if (attributes0 != null) {
      for (int i = 0; i < attributes0.length; i++) {
        s = s + "\n\t";
        name = attributes0[i].name.toString();
        value = attributes0[i].value.toString();
        s = s + name + "=\"" + value + "\"";
      }
    }
    s = s + getId(id);
    return s.concat("\n/>");
  }
  
  /**
   * The directive name denoted by "directive" in the following syntax - <%@ directive { attr='value' }* %>
   * @return - String with one of the directive names - "page", "include", "taglib", "tag", "attribute", "variable" or some custom type
   */
  public String getDirectiveName(){
    return DIRECTIVE_NAME;
  }
  
  /**
   * Subclass this method if you want to perform some validation of the usage of this directive.
   * Note the tree of element is still not created.
   * @param parser
   * @throws JspParseException - if for example page directive is used in tag file or tag directive in JSP page 
   */
  public void verifyDirective(JspPageInterface parser) throws JspParseException{
    if( !parser.isTagFile() ){
      throw new JspParseException(JspParseException.JSP_ACTION_CANNOT_BE_USED_IN_JSP, new Object[]{getDirectiveName()}, parser.currentFileName(), parser.currentDebugPos());
    }
  }
}

