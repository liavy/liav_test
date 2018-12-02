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

import javax.servlet.jsp.tagext.TagAttributeInfo;

import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.TagInfoImpl;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.TagLibDescriptor;
import com.sap.engine.services.servlets_jsp.server.lib.StringUtils;

/**
 *
 * @author Bojidar kadrev
 * @author Mladen Markov
 * @version 7.0
 */
public class AttributeDirective extends JspDirective {

  /*
   * denotes the name of this type element
   */
  private static final String DIRECTIVE_NAME = "attribute";
  /*
   * all attributes that this element can
   * accept
   */
  private static final String[] attributeNames = {"name", "required", "fragment", "rtexprvalue", "type", "description",
                "deferredValue", "deferredValueType", "deferredMethod", "deferredMethodSignature"  }; //JSP2.1
  /*
   * Must save attributes for this directive, to use in XML view of jsp.
   */
  public Attribute[] attributes0 = null;

  /**
   * Constructs new AttributeDirective
   *
   */
  public AttributeDirective() {
    elementType = Element.PAGE_DIRECTIVE;
    name = DIRECTIVE_NAME.toCharArray();
    _name = DIRECTIVE_NAME;
  }

  /**
   * Takes specific action coresponding to this jsp element
   * logic
   *
   * @exception   JspParseException  thrown if error occures during
   * verification
   */
  public void action(StringBuffer buffer) throws JspParseException {

    TagInfoImpl tagInfo = parser.getTagInfo();
    if (tagInfo == null) {
      tagInfo = new TagInfoImpl();
      parser.setTagInfo(tagInfo);
    }

    String attrName = null;
    boolean required = false;
    boolean fragment = false;
    boolean rtexprvalue = true;
    String type = null;
    String description = null;
    boolean deferredValue = false;
    boolean deferredMethod = false;
    String deferredValueStr = null;
    String deferredMethodStr = null;
    String deferredValueType = "java.lang.String";
    String deferredMethodSignature = null;

    // name
    String attributeValue = getAttributeValue("name");
    if (attributeValue != null) {
      attrName = attributeValue;
    } else {
      throw new JspParseException(JspParseException.MISSING_ATTRIBUTE_IN_DIRECTIVE, new Object[]{"name", _name},
              parser.currentFileName(), debugInfo.start);
    }

    // required
    attributeValue = getAttributeValue("required");
    if (attributeValue != null) {
      required = Boolean.valueOf(attributeValue).booleanValue();
    }

    // fragment
    attributeValue = getAttributeValue("fragment");
    if (attributeValue != null) {
      fragment = Boolean.valueOf(attributeValue).booleanValue();
    }

    // rtexprvalue
    attributeValue = getAttributeValue("rtexprvalue");
    if (attributeValue != null) {
      if (fragment) {
        throw new JspParseException(JspParseException.ATTRIBUTE_CANNOT_BE_SPECIFIED_WHEN_OTHER_ATTRIBUTE_IS_TRUE, new Object[]{"rtexprvalue", _name, "fragment"},
                parser.currentFileName(), debugInfo.start);
      }
      rtexprvalue = Boolean.valueOf(attributeValue).booleanValue();
    }

    // type
    attributeValue = getAttributeValue("type");
    if (attributeValue != null) {
      type= attributeValue;
    }

    // description
    attributeValue = getAttributeValue("description");
    if (attributeValue != null) {
      description = attributeValue;
    }

    //JSP2.1
    // "deferredValue",
    deferredValueStr = getAttributeValue("deferredValue");
    if (deferredValueStr != null) {
      deferredValue = Boolean.valueOf(deferredValueStr).booleanValue();;
    }

    // "deferredValueType",
    attributeValue = getAttributeValue("deferredValueType");
    if (attributeValue != null) {
      deferredValueType = attributeValue;
      if (deferredValueStr == null) {
        deferredValue = true;
      } else {
         throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_DIRECTIVE, new Object[]{attrName, "deferredValue", _name},
         parser.currentFileName(), debugInfo.start);
      }
    } else if (deferredValue) {
      deferredValueType = "java.lang.Object";
    }

    // "deferredMethod",
    deferredMethodStr = getAttributeValue("deferredMethod");
    if (deferredMethodStr != null) {
      deferredMethod = Boolean.valueOf(deferredMethodStr).booleanValue();;
    }

    // "deferredMethodSignature"
    attributeValue = getAttributeValue("deferredMethodSignature");
    if (attributeValue != null) {
      deferredMethodSignature = attributeValue;
      if (deferredMethodStr == null) {
        deferredMethod = true;
      } else {
         throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_DIRECTIVE, new Object[]{attrName, "deferredMethod", _name},
        parser.currentFileName(), debugInfo.start);
      }
    } else if (deferredMethod) {
      //Table JSP.8-3 If deferredMethod is true and deferredMethodSignature is not
      //specified, it defaults to void methodname().
      deferredMethodSignature = "void method()";
    }

    switch (parser.getTagInfo().checkAttributeName(attrName)) {
      case TagInfoImpl.ERROR_DUPLICATE_NAME :
        throw new JspParseException(JspParseException.ATTRIBUTE_DUPLICATE_NAME, new Object[]{attrName},
                parser.currentFileName(), debugInfo.start);
      case TagInfoImpl.ERROR_VARIABLE_NAME_CONFLICTS_WITH_ATTRIBUTE_NAME :
        throw new JspParseException(JspParseException.ATTRIBUTE_NAME_CONFLICTS_WITH_VARIABLE_NAME, new Object[]{attrName},
                parser.currentFileName(), debugInfo.start);
      case TagInfoImpl.ERROR_CONFLICTS_WITH_DYNAMIC_ATTRIBUTE_NAME :
        throw new JspParseException(JspParseException.ATTRIBUTE_NAME_CONFLICTS_WITH_DYNAMIC_ATTRIBUTES, new Object[]{attrName},
                parser.currentFileName(), debugInfo.start);
      case TagInfoImpl.ERROR_OK :
        break;
      default:
        // should not happen
        throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_DIRECTIVE, new Object[]{attrName, "name", _name},
                parser.currentFileName(), debugInfo.start);
    }

    if (fragment && type != null) {
      throw new JspParseException(JspParseException.ATTRIBUTE_CANNOT_BE_SPECIFIED_WHEN_OTHER_ATTRIBUTE_IS_TRUE, new Object[]{"type", _name, "fragment"},
              parser.currentFileName(), debugInfo.start);
    }

    if (deferredValue && deferredMethod) {
      throw new JspParseException(JspParseException.ATTRIBUTE_CANNOT_BE_SPECIFIED_WHEN_OTHER_ATTRIBUTE_IS_TRUE, new Object[]{"deferredValue", _name, "deferredMethod"},
        parser.currentFileName(), debugInfo.start);
    }

    if (deferredMethod) {
      type = "javax.el.MethodExpression";
    }
    if (deferredValue) {
      type = "javax.el.ValueExpression";
    }

    //JSP.8.5.2 The attribute Directive. Table JSP.8-3 Details of attribute directive attributes
    //If this attribute is true (i.e. fragment), the type attribute is fixed at
    //javax.servlet.jsp.tagext.JspFragment
    //Defaultsto java.lang.String if not specified.
    if (fragment) {
      type = "javax.servlet.jsp.tagext.JspFragment";
    } else if (type == null) {
      type = "java.lang.String";
    }
    tagInfo.addTagAttributeInfo(new TagAttributeInfo(attrName, required, type, rtexprvalue, fragment, description, deferredValue, deferredMethod, deferredValueType , deferredMethodSignature ));
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
      throw new JspParseException(JspParseException.DIRECTIVE_MUST_HAVE_AT_LEAST_ONE_SPECIFIED_ATTRIBUTE, new Object[]{_name},
              parser.currentFileName(), debugInfo.start);
    }
    //save attributes
    attributes0 = attributes;

    for (int i = 0; i < attributes.length; i++) {
      String attrName = attributes[i].name.toString();
      String value = attributes[i].value.toString();
      boolean valid = true;
      if (attributeNames[0].equals(attrName)) { // name
      } else if (attributeNames[1].equals(attrName)) { // required
        valid = "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
      } else if (attributeNames[2].equals(attrName)) { // fragment
        valid = "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
      } else if (attributeNames[3].equals(attrName)) { // rtexprvalue
        valid = "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
      } else if (attributeNames[4].equals(attrName)) { // type
        // primitive types not allowed
        // types that begin with a capital letter or have a package name are valid
        valid = (value.indexOf('.') >= 0) || value.charAt(0) == value.toUpperCase().charAt(0);
      } else if (attributeNames[5].equals(attrName)) { // description
      } else if (attributeNames[6].equals(attrName)) { // deferredValue
        if ( StringUtils.lessThan(parser.getTagFileTLD().getRequiredVersion(), TagLibDescriptor.JSP_VERSION_21_DOUBLE) ) { 
          //Causes a translation error if specified in a tag file with a JSP version less than 2.1.
          throw new JspParseException(JspParseException.ATTRIBUTES_IN_TAGFILE_CANNOT_BE_USED_FOR_JSP_VERSION, new String[] {TagLibDescriptor.JSP_VERSION_21, attrName});
        }
        valid = "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
      } else if (attributeNames[7].equals(attrName)) { // deferredValueType
        if ( StringUtils.lessThan(parser.getTagFileTLD().getRequiredVersion(), TagLibDescriptor.JSP_VERSION_21_DOUBLE) ) { 
          //Causes a translation error if specified in a tag file with a JSP version less than 2.1.
          throw new JspParseException(JspParseException.ATTRIBUTES_IN_TAGFILE_CANNOT_BE_USED_FOR_JSP_VERSION, new String[] {TagLibDescriptor.JSP_VERSION_21, attrName});
        }
      } else if (attributeNames[8].equals(attrName)) { // deferredMethod        
        if ( StringUtils.lessThan(parser.getTagFileTLD().getRequiredVersion(), TagLibDescriptor.JSP_VERSION_21_DOUBLE) ) { 
          //Causes a translation error if specified in a tag file with a JSP version less than 2.1.
          throw new JspParseException(JspParseException.ATTRIBUTES_IN_TAGFILE_CANNOT_BE_USED_FOR_JSP_VERSION, new String[] {TagLibDescriptor.JSP_VERSION_21, attrName});
        }
        valid = "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
      } else if (attributeNames[9].equals(attrName)) { // deferredMethodSignature
        if ( StringUtils.lessThan(parser.getTagFileTLD().getRequiredVersion(), TagLibDescriptor.JSP_VERSION_21_DOUBLE) ) { 
          //Causes a translation error if specified in a tag file with a JSP version less than 2.1.
          throw new JspParseException(JspParseException.ATTRIBUTES_IN_TAGFILE_CANNOT_BE_USED_FOR_JSP_VERSION, new String[] {TagLibDescriptor.JSP_VERSION_21, attrName});
        }
        //the check is in InnerExpression
      } else {
        throw new JspParseException(JspParseException.UNRECOGNIZED_ATTRIBUTE_IN_DIRECTIVE, new Object[]{attrName, _name},
                parser.currentFileName(), debugInfo.start);
      }
      if (!valid) {
        throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_DIRECTIVE, new Object[]{value, attrName, _name},
                parser.currentFileName(), debugInfo.start);
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
    return s.concat("\n" + SLASH_END);
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

