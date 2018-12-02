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

import java.util.Enumeration;
import java.util.StringTokenizer;

import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.TagInfoImpl;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.TagLibDescriptor;
import com.sap.engine.services.servlets_jsp.server.lib.StringUtils;

import javax.servlet.jsp.tagext.TagAttributeInfo;

/*
 *
 * @author Bojidar kadrev
 * @version 7.0
 */
public class TagDirective extends JspDirective {

  /*
   * denotes the name of this type element
   */
  private static final String DIRECTIVE_NAME = "tag";
  /*
   * all attributes that this element can
   * accept
   */
  private static final String[] attributeNames = {"display-name", "body-content",
                                                  "dynamic-attributes", "small-icon",
                                                  "large-icon", "description", "example",
                                                  "language", "import", "pageEncoding",
                                                  "isELIgnored", "deferredSyntaxAllowedAsLiteral", "trimDirectiveWhitespaces"};

  /*
   * Must save attributes for this directive, to use in XML view of jsp.
   */
  public Attribute[] attributes0 = null;

  /**
   * Constructs new TagDirective
   *
   */
  public TagDirective() {
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

    TagInfoImpl tagInfo = parser.getTagInfo();
    if (tagInfo == null) {
      tagInfo = new TagInfoImpl();
      parser.setTagInfo(tagInfo);
    }

    // example
    String attributeValue = getAttributeValue("example");
    if (attributeValue != null) {
      // Table JSP.8-2 ...presents an informal description of an example of a use of this action.
    }

    // description
    attributeValue = getAttributeValue("description");
    if (attributeValue != null) {
      tagInfo.setInfoString(attributeValue);
    }

    // large-icon
    attributeValue = getAttributeValue("large-icon");
    if (attributeValue != null) {
      tagInfo.setLargeIcon(attributeValue);
    }

    // small-icon
    attributeValue = getAttributeValue("small-icon");
    if (attributeValue != null) {
      tagInfo.setSmallIcon(attributeValue);
    }

    // dynamic-attributes
    attributeValue = getAttributeValue("dynamic-attributes");
    if (attributeValue != null) {
      TagAttributeInfo[] attributes = tagInfo.getTagAttributeInfo();
      for (int i = 0; i < attributes.length; i++) {
        if (attributeValue.equals(attributes[i].getName())) {
           throw new JspParseException(JspParseException.ATTRIBUTE_NAME_CONFLICTS_WITH_DYNAMIC_ATTRIBUTES, new Object[]{attributeValue},
                parser.currentFileName(), debugInfo.start);
        }
      }
      tagInfo.setDynamicAttrsMapName(attributeValue);
      tagInfo.setDynamicAttributes(true);
    }

    // display-name
    attributeValue = getAttributeValue("display-name");
    if (attributeValue != null) {
      tagInfo.setDisplayName(attributeValue);
    }

    // body-content
    attributeValue = getAttributeValue("body-content");
    if (attributeValue != null) {
      tagInfo.setBodyContent(attributeValue);
    } else {
      tagInfo.setBodyContent("scriptless");
    }

    // import
    attributeValue = parser.getPageImportAttribute();

    if (attributeValue != null) {
      StringTokenizer toker = new StringTokenizer(attributeValue, ",");

      while (toker.hasMoreTokens()) {
        parser.getImportDirective().append("import ").append(toker.nextToken().trim()).append(';').append("\r\n");
      }
    }

    // language
    attributeValue = getAttributeValue("language");
    // The value should be "java". It is laready chcked in verifyAttributes(). Nothing to do here.

    // pageEncoding detected in page preprocessing - see ParserImpl.parse()
    //attributeValue = getAttributeValue("pageEncoding");
    //if (attributeValue != null) {
    //  parser.setPageEncoding(attributeValue);
    //}

    attributeValue = getAttributeValue("isELIgnored");
    if (attributeValue != null) {
      parser.setIsELIgnored(new Boolean(attributeValue).booleanValue());
    }

    attributeValue = getAttributeValue("deferredSyntaxAllowedAsLiteral");
    if (attributeValue != null) {
      parser.setDeferredSyntaxAllowedAsLiteral(new Boolean(attributeValue).booleanValue());
    }

    attributeValue = getAttributeValue("trimDirectiveWhitespaces");
    if (attributeValue != null) {
      parser.setTrimDirectiveWhitespaces(new Boolean(attributeValue).booleanValue());
    }
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
    Attribute o = null;
    String attributeName0;
    //save attributes
    attributes0 = attributes;

    for (int i = 0; i < attributes.length; i++) {
      attributeName0 = attributes[i].name.toString();
      if (attributeName0.equals("import")) {
        if (parser.getPageImportAttribute() == null) {
          parser.setPageImportAttribute(attributes[i].value.toString());
        } else {
          parser.setPageImportAttribute(parser.getPageImportAttribute() + "," + attributes[i].value.toString());
        }

        continue;
      }
      o = (Attribute)parser.getPageAttributes().get(attributeName0);
      if (o == null) {
        parser.getPageAttributes().put(attributeName0, attributes[i]);
      } else {
        //JSP.8.5.1 The tag Directive
        //Like the page directive, a translation unit can contain more than one
        //instance of the tag directive, all the attributes will apply to the complete translation
        //unit (i.e. tag directives are position independent). There shall be only one occurrence
        //of any attribute/value defined by this directive in a given translation unit,
        //unless the values for the duplicate attributes are identical for all occurrences.
        if (!attributes[i].value.equals(o.value.toString())) {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_DIRECTIVE, new Object[]{attributes[i].name, _name}, parser.currentFileName(), debugInfo.start);
        }
      }
    }

    for (int i = 0; i < attributes.length; i++) {
      String attrName = attributes[i].name.toString();
      String value = attributes[i].value.toString();
      boolean valid = true;
      if (attributeNames[0].equals(attrName)) { // display-name
      } else if (attributeNames[1].equals(attrName)) { // body-content
        valid = value.equalsIgnoreCase("scriptless") || value.equalsIgnoreCase("tagdependent") || value.equalsIgnoreCase("empty");
      } else if (attributeNames[2].equals(attrName)) { // dynamic-attributes
        //Check value for a valid java identifier
        //It is not obligatory by the spec, but if the identifier is not valid, it is better to determine the error 
        // in transalation time instead of compile time
        valid = StringUtils.isValidJavaIdentifier(value);
      } else if (attributeNames[3].equals(attrName)) { // small-icon
      } else if (attributeNames[4].equals(attrName)) { // large-icon
      } else if (attributeNames[5].equals(attrName)) { // description
      } else if (attributeNames[6].equals(attrName)) { // example
      } else if (attributeNames[7].equals(attrName)) { // language
        //JSP.8.5.1
        //Carries the same syntax and semantics of thelanguage attribute of the page directive.
        valid = "java".equals(value);
      } else if (attributeNames[8].equals(attrName)) { // import
      } else if (attributeNames[9].equals(attrName)) { // pageEncoding
      } else if (attributeNames[10].equals(attrName)) { // isELIgnored
        valid = "true".equals(value) || "false".equals(value);
      } else if (attributeNames[11].equals(attrName)) { // deferredSyntaxAllowedAsLiteral
        valid = "true".equals(value) || "false".equals(value);
        if ( StringUtils.lessThan(parser.getTagFileTLD().getRequiredVersion(), TagLibDescriptor.JSP_VERSION_21_DOUBLE) ) {
          throw new JspParseException(JspParseException.UNRECOGNIZED_ATTRIBUTE_IN_DIRECTIVE, new Object[]{attrName, _name}, parser.currentFileName(), debugInfo.start);
        }
      } else if (attributeNames[12].equals(attrName)) { // trimDirectiveWhitespaces
        valid = "true".equals(value) || "false".equals(value);
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

