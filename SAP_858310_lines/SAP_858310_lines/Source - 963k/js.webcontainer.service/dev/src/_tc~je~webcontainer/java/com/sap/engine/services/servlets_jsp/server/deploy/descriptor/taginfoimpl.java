package com.sap.engine.services.servlets_jsp.server.deploy.descriptor;

import javax.servlet.jsp.tagext.*;

import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;

import java.util.Vector;
import java.util.Enumeration;

/**
 * Created by IntelliJ IDEA.
 * User: Mladen-M
 * Date: 2004-8-5
 * Time: 10:35:06
 * To change this template use File | Settings | File Templates.
 */
public class TagInfoImpl {

  public static final int ERROR_OK = 0;
  public static final int ERROR_DUPLICATE_NAME = 1;
  public static final int ERROR_VARIABLE_NAME_CONFLICTS_WITH_ATTRIBUTE_NAME = 2;
  public static final int ERROR_VARIABLE_ALIAS_CONFLICTS_WITH_ATTRIBUTE_NAME = 3;
  public static final int ERROR_DUPLICATE_VARIABLE_NAMEFROMATTRIBUTE = 4;
  public static final int ERROR_DUPLICATE_VARIABLE_ALIAS = 5;
  public static final int ERROR_NO_SUCH_ATTRIBUTE_NAME = 6;
  public static final int ERROR_CONFLICTS_WITH_DYNAMIC_ATTRIBUTE_NAME = 7;

  private String tagName;
  private String tagClassName;
  private String bodyContent;
  private String infoString;
  private TagLibraryInfo tagLibrary;
  private TagExtraInfo tagExtraInfo;
  private String displayName;
  private String smallIcon;
  private String largeIcon;
  private boolean dynamicAttributes;
  private String dynamicAttrsMapName;

  private Vector attributeInfos = null;
  private Vector variableInfos = null;

  public TagInfoImpl() {
    bodyContent = "scriptless";
    
    attributeInfos = new Vector();
    variableInfos = new Vector();
  }

  public String getDynamicAttrsMapName() {
    return dynamicAttrsMapName;
  }

  public void setDynamicAttrsMapName(String dynamicAttrsMapName) {
    this.dynamicAttrsMapName = dynamicAttrsMapName;
  }

  public String getTagName() {
    return tagName;
  }

  public void setTagName(String tagName) {
    this.tagName = tagName;
  }

  public String getTagClassName() {
    return tagClassName;
  }

  public void setTagClassName(String tagClassName) {
    this.tagClassName = tagClassName;
  }

  public String getBodyContent() {
    return bodyContent;
  }

  public void setBodyContent(String bodyContent) {
    this.bodyContent = bodyContent;
  }

  public String getInfoString() {
    return infoString;
  }

  public void setInfoString(String infoString) {
    this.infoString = infoString;
  }

  public TagLibraryInfo getTagLibrary() {
    return tagLibrary;
  }

  public void setTagLibrary(TagLibraryInfo tagLibrary) {
    this.tagLibrary = tagLibrary;
  }

  public TagExtraInfo getTagExtraInfo() {
    return tagExtraInfo;
  }

  public void setTagExtraInfo(TagExtraInfo tagExtraInfo) {
    this.tagExtraInfo = tagExtraInfo;
  }

  public TagAttributeInfo[] getTagAttributeInfo() {
    return (TagAttributeInfo[])attributeInfos.toArray(new TagAttributeInfo[0]);
  }

  public void addTagAttributeInfo(TagAttributeInfo attrInfo) {
    attributeInfos.add(attrInfo);
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getSmallIcon() {
    return smallIcon;
  }

  public void setSmallIcon(String smallIcon) {
    this.smallIcon = smallIcon;
  }

  public String getLargeIcon() {
    return largeIcon;
  }

  public void setLargeIcon(String largeIcon) {
    this.largeIcon = largeIcon;
  }

  public TagVariableInfo[] getTagVariableInfo() {
    return (TagVariableInfo[])variableInfos.toArray(new TagVariableInfo[0]);
  }

  public void addTagVariableInfo(TagVariableInfo variableInfo) {
    variableInfos.add(variableInfo);
  }

  public boolean hasDynamicAttributes() {
    return dynamicAttributes;
  }

  public void setDynamicAttributes(boolean dynamicAttributes) {
    this.dynamicAttributes = dynamicAttributes;
  }

  public TagInfo getTagInfo() {
    return new TagInfo(tagName, tagClassName, bodyContent, infoString, tagLibrary, tagExtraInfo,
        attributeInfos != null ? getTagAttributeInfo() : new TagAttributeInfo[0],
        displayName, smallIcon, largeIcon,
        variableInfos != null ? getTagVariableInfo() : new TagVariableInfo[0], dynamicAttributes);
  }

  /**
   * Check an attribute name value.
   *
   * A translation error will result if there is an attribute
   * directive with a name attribute equal to the value of the
   * name-given attribute of a variable directive or the dynamicattributes
   * attribute of a tag directive in this translation unit.
   */
  public int checkAttributeName(String name) {
    for (Enumeration attrs = attributeInfos.elements(); attrs.hasMoreElements(); ) {
      if (((TagAttributeInfo)attrs.nextElement()).getName().equals(name)) {
        return ERROR_DUPLICATE_NAME;
      }
    }
    for (Enumeration vars = variableInfos.elements(); vars.hasMoreElements(); ) {
      if (((TagVariableInfo)vars.nextElement()).getNameGiven().equals(name)) {
        return ERROR_VARIABLE_NAME_CONFLICTS_WITH_ATTRIBUTE_NAME;
      }
    }
    if (name.equals(dynamicAttrsMapName)) {
      return ERROR_CONFLICTS_WITH_DYNAMIC_ATTRIBUTE_NAME;
    }
    return ERROR_OK;
  }

  /**
   * Check a variable name-given value.
   *
   * A translation error will result if two variable directives have
   * the same name-given. A translation error will result if there
   * is a variable directive with a name-given attribute equal to
   * the value of the name attribute of an attribute directive or
   * the dynamic-attributes attribute of a tag directive in this
   * translation unit.
   */
  public int checkVariableNameGiven(String nameGiven) {
    for (Enumeration vars = variableInfos.elements(); vars.hasMoreElements(); ) {
      TagVariableInfo tagVarInfo = (TagVariableInfo)vars.nextElement();
      if (tagVarInfo.getNameGiven().equals(nameGiven)) {
        return ERROR_DUPLICATE_NAME;
      }
    }
    for (Enumeration attrs = attributeInfos.elements(); attrs.hasMoreElements(); ) {
      TagAttributeInfo tagAttrInfo = (TagAttributeInfo)attrs.nextElement();
      if (tagAttrInfo.getName().equals(nameGiven)) {
        return ERROR_VARIABLE_NAME_CONFLICTS_WITH_ATTRIBUTE_NAME;
      }
    }
    if (nameGiven!= null && nameGiven.equals(dynamicAttrsMapName)) {
      return ERROR_CONFLICTS_WITH_DYNAMIC_ATTRIBUTE_NAME;
    }
    return ERROR_OK;
  }

  /**
   * Check a variable name-from-attribute value.
   *
   * A translation error will result if there is no attribute directive
   * with a name attribute equal to the value of this attribute that
   * is of type java.lang.String, is required and not an rtexprvalue.
   * Either the name-given attribute or the name-fromattribute
   * attribute must be specified. Specifying neither or
   * both will result in a translation error. A translation error
   * will result if two variable directives have the same namefrom-
   * attribute.
   * @throws JspParseException
   */
  public int checkVariableNameFromAttribute(String nameFromAttribute) throws JspParseException {
    if( nameFromAttribute == null ){
      return ERROR_OK; 
    }
    for (Enumeration vars = variableInfos.elements(); vars.hasMoreElements(); ) {
      TagVariableInfo tagVarInfo = (TagVariableInfo)vars.nextElement();
      if (nameFromAttribute.equals(tagVarInfo.getNameFromAttribute())) {
        return ERROR_DUPLICATE_VARIABLE_NAMEFROMATTRIBUTE;
      }
    }
    TagAttributeInfo foundTag = null;
    for(Enumeration attributes = attributeInfos.elements(); attributes.hasMoreElements(); ){
      TagAttributeInfo tagAttributeInfo = (TagAttributeInfo) attributes.nextElement();
      if (tagAttributeInfo.getName().equals( nameFromAttribute ) ) {
        if ( tagAttributeInfo.getTypeName() != "java.lang.String" || !tagAttributeInfo.isRequired() || tagAttributeInfo.canBeRequestTime()){
          throw new JspParseException(JspParseException.WRONG_ATTRIBUTE_FOR_VARIABLE_FROM_ATTRIBUTE, new Object[]{nameFromAttribute});
        }
        foundTag = tagAttributeInfo;
      }
    }
    if( foundTag == null ){
      throw new JspParseException(JspParseException.ATTRIBUTE_WITH_NAMEFROMATTRIBUTE_NOT_DEFINED, new Object[]{nameFromAttribute});
    }
    
    return ERROR_OK;
  }

  /**
   * Checks a variable alias value.
   *
   * A translation error must occur if the value of alias
   * is the same as the value of a name attribute of an attribute
   * directive or the name-given attribute of a variable directive
   * in the same translation unit.
   */
  public int checkVariableAlias(String alias) {
    for (Enumeration vars = variableInfos.elements(); vars.hasMoreElements(); ) {
      TagVariableInfo tagVarInfo = (TagVariableInfo)vars.nextElement();
      if (alias != null && tagVarInfo.getNameGiven().equals(alias)) {
        return ERROR_DUPLICATE_VARIABLE_ALIAS;
      }
    }
    for (Enumeration attrs = attributeInfos.elements(); attrs.hasMoreElements(); ) {
      TagAttributeInfo tagAttrInfo = (TagAttributeInfo)attrs.nextElement();
      if (alias != null && tagAttrInfo.getName().equals(alias)) {
        return ERROR_VARIABLE_ALIAS_CONFLICTS_WITH_ATTRIBUTE_NAME;
      }
    }
    return ERROR_OK;
  }

}
