/*
 * Copyright (c) 2000-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.taglib;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Vector;
import java.util.ArrayList;

import javax.el.ExpressionFactory;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.JspIdConsumer;
import javax.servlet.jsp.tagext.SimpleTag;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.TagVariableInfo;
import javax.servlet.jsp.tagext.ValidationMessage;
import javax.servlet.jsp.tagext.VariableInfo;

import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.LifoDuplicateMap;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Attribute;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.ELExpression;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.InnerExpression;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.TagLibDescriptor;
import com.sap.engine.services.servlets_jsp.server.lib.StringUtils;
import com.sap.engine.services.servlets_jsp.server.logging.LogLocation;
import com.sun.el.ExpressionFactoryImpl;


/**
 * Custom tag support. Generates a necessary code into a java file after encountering a beginning
 * action tag.
 *
 * @author Maria Jurova
 */
public class TagBeginGenerator extends TagGeneratorBase {

  /**
   * Current iddent append in the beginning of every line
   */
  private String ident = "			";
  /**
   * A prefix with which this tag library is available into a jsp file
   */
  private String prefix = null;
  /**
   * A name of the current tag action
   */
  private String shortTagName = null;
  /**
   * Information on Tag Attributes. Only the information needed to generate code is included here.
   */
  protected TagAttributeInfo[] attributes = null;
  /**
   * Attributes of the current action
   */
//  private Hashtable attrs = null;
  private Attribute[] attrs = null;
  /**
   * Contains the all TagCache object generated for the actions used in jsp file.
   */
  private TagLibraries tli = null;
  /**
   * Info of the current action
   */
  private TagInfo ti = null;
  /**
   * A name of the action used in jsp file which will be refference to the main action class
   * in a generated java file for this jsp.
   */
  private String baseVarName = null;
  /**
   * The whole name of the refference to the main class including tag name and baseVarName
   */
  private String thVarName = null;
  /**
   * A simple cache to hold results of one-time evaluation for a custom tag
   */
  private TagCache tc = null;
  /**
   * Tag instance attribute(s)/value(s)
   */
  private TagData tagData = null;

  /**
   * The Classname of the current JSP page. Necessary for generating the Fragment helper class
   */
  private String pageClassName = null;

  /**
   * The number of invokeN methods in the Fragment Helper class (N is a number from 0+).
   * A single JSP page uses only one helper class with as many
   * invoke methods as many fragments there are.
   * So, the count has to be passed from tag to tag in order to
   * generate the proper method name.
   */
  private int fragmentsCount;

  private boolean implementsDynamicAttribs = false;
  private boolean isSingleTag = false;
  private boolean hasBody = false;
  private JspPageInterface parser = null;

  /**
   * Initiates the instance with data of the current action.
   *
   * @param   prefix  Name of the tag library into a jsp
   * @param   shortTagName  Name of the action
   * @param   attrs  Attributes of the action
   * @param   ti  TagInfo of the current action
   */
  //public TagBeginGenerator(String prefix, String shortTagName, Attribute[] attrs, TagLibraries tli, TagInfo ti, Stack tagHandlerStack, Hashtable tagVarNumbers, String pageClassName, int fragmentsCount, Hashtable jspAttributesCode)

  public TagBeginGenerator(String prefix, String shortTagName, Attribute[] attrs, TagInfo ti, JspPageInterface parser)
  {
    this.prefix = prefix;
    this.shortTagName = shortTagName;
    this.attrs = attrs;
    this.tli = parser.getTagLibraries();
    this.ti = ti;
    this.attributes = ti.getAttributes();
    this.tagVarNumbers = parser.getTagVarNumbers();
    this.baseVarName = getTagVarName(prefix, shortTagName);
    this.thVarName = "_jspx_th_" + baseVarName;
    this.tagHandlerStack = parser.getTagsStack();
    this.pageClassName = parser.getPageClassname();
    if (pageClassName.indexOf('.') > 0){
      pageClassName = pageClassName.substring(pageClassName.lastIndexOf('.') + 1);
    }
    this.fragmentsCount = parser.getFragmentsCount();
    this.parser = parser;
  }

  /**
   * Initiates with a refference to ServletContext of the application.
   *
   * @exception   JspParseException
   */
  public void init(ClassLoader applicationClassLoader) throws JspParseException {
    //JSP2.1 Init TagLibraryInfos
    //If a tag library is imported more than once and bound to different prefices, 
    //only the TagLibraryInfo bound to the first prefix must be included in the returned array.
    TagLibraryInfo[] infos = TagLibUtil.getUniqueTaglibs(((LifoDuplicateMap<String, TagLibraryInfo>)parser.getTaglibs()));
    ((TagLibDescriptor)ti.getTagLibrary()).setTagLibraryInfos(infos);

    /*
    * If the tag has dynamic attributes, any missin attribute should
    * be set using the setDynamicAttribute() method, only if the tag
    * implements the DynamicAttributes interface.
    * Therefore the validation can be done only after the tag class
    * has already been loaded
    */
    if (!ti.hasDynamicAttributes()) {
      validate();
    }
    tc = tli.getTagCache(prefix + ":" + shortTagName);

    if (tc == null) {
      tc = new TagCache();
      Class clz = null;
      try {
        logTraces();
        clz = applicationClassLoader.loadClass(ti.getTagClassName());
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
        throw new JspParseException(JspParseException.CANNOT_INIT_TAGBEGINGENERATOR, e);
      }
      tc.setTagHandlerClass(clz);
      tli.putTagCache(prefix + ":" + shortTagName, tc);
    }

    isSimpleTag = SimpleTag.class.isAssignableFrom(tc.getTagHandlerClass());
    if (isSimpleTag) {
      if (ti.getBodyContent().equalsIgnoreCase(TagInfo.BODY_CONTENT_JSP)) {
        throw new JspParseException(JspParseException.SIMPLE_TAG_CANNOT_HAVE_JSP_BODY_CONTENT, new Object[]{ti.getTagName()});
      }
    }
    implementsDynamicAttribs = DynamicAttributes.class.isAssignableFrom(tc.getTagHandlerClass());
    if (ti.hasDynamicAttributes()) {
      validate();
    }
  }
  
  /**
   * @ deprecated - to be removed after bug with NullPointerException is fixed.
   *
   */
  private void logTraces() {
    if( ti.getTagClassName() == null ) {
      LogLocation parserLog = LogContext.getLocation(LogContext.LOCATION_JSP_PARSER);
      parserLog.traceError("ASJ.web.000362", "ti.getTagClassName() is null", null, null);
      parserLog.traceError("ASJ.web.000408", "JSP: {0}", new Object[]{parser.currentFileName()}, null, null);      
      parserLog.traceError("ASJ.web.000409", "ti.getTagLibrary(): {0}", new Object[]{ti.getTagLibrary()}, null, null);
      if( ti.getTagLibrary() != null ) {
        parserLog.traceError("ASJ.web.000410", "ti.getTagLibrary().getShortName(): {0}" , new Object[]{ti.getTagLibrary().getShortName()}, null, null);
        parserLog.traceError("ASJ.web.000411", "ti.getTagLibrary().getURI(): {0}", new Object[]{ti.getTagLibrary().getURI()}, null, null);
      }    
    }
  }

  /**
   * Checks if the all required attributes of the action are specified in the jsp file
   * and if their session scope is valid.
   *
   * @exception   JspParseException
   */
  public void validate() throws JspParseException {
    
    tagData = new TagData((Object[][])null);
    /*
     * Check if all supplied attributes are valid for this tag
     * Skipped if tag implements DynamicAttributes, since if
     * an attribute is not defined in the TLD is will be set using
     * the setDynamicAttribute() method
     */
    if (attrs != null) {
      for (int i = 0; i < attrs.length; i++) {
        String attrName = attrs[i].name.toString();
        String attrValue = attrs[i].value.toString();
        ELExpression expr = new ELExpression(attrValue);
        TagAttributeInfo attributeInfo = null;
        for (int j = 0; j < attributes.length; j++) {
          if (attrName.equals(attributes[j].getName())) {
            attributeInfo = attributes[j];
            break;
          }
        }

        if (!implementsDynamicAttribs) {
          if ( attributeInfo == null ) {
            throw new JspParseException(JspParseException.INVALID_ATTRIBUTE, new Object[] {attrName});
          }
        }
        if( !expr.isEL() && attributeInfo != null && attributeInfo.isDeferredMethod()  ) {
          String methodSignature = attributeInfo.getMethodSignature();
          HashMap< String, Object> signatureMap = InnerExpression.parseSignature(methodSignature);
          String methodReturnType = (String) signatureMap.get(InnerExpression.METHOD__RETURN_TYPE);
          //A String literal can be provided, as long as the return type of the deferred method signature is not void.  
          if( methodReturnType.equalsIgnoreCase(InnerExpression.METHOD_RETURN_TYPE_VOID) ) {
            throw new JspParseException(JspParseException.ILLEGAL_VALUE_FOR_METHOD_SIGNATURE, new String[] {attrValue, methodSignature});
          } else {
            // It is an error to specify a string literal
            // as a value for MethodExpression if the literal
            // cannot be coerced to the return type 
            try {
              Class methodReturnsClass = TagLibUtil.toClass(methodReturnType, parser.getApplicationClassLoader());
              ExpressionFactory expressionFactory = new ExpressionFactoryImpl();
              expressionFactory.coerceToType(attrValue, methodReturnsClass);              
            }catch (Exception e) {
              throw new JspParseException(JspParseException.ILLEGAL_VALUE_FOR_METHOD_SIGNATURE, new String[] {attrValue, methodSignature});
            }
          }           
        }
        
        //The distinguished object REQUEST_TIME_VALUE is only returned if the value is specified as a
        //request-time attribute expression or via the <jsp:attribute> action with a body that contains dynamic content
        //(scriptlets, scripting expressions, EL expressions, standard actions, or custom actions).
        if (attrs[i].isNamed || isExpression(attrValue) || expr.isEL()) {
          tagData.setAttribute(attrName, TagData.REQUEST_TIME_VALUE);
        } else {
          tagData.setAttribute(attrName, attrValue);
        }

      }
    }

    //validation should be done only by calling validate()
    // validate should call isValid() if necessary
    //if (!ti.isValid(tagData)) {
      //throw new JspParseException(JspParseException.INVALID_ATTRIBUTE, new Object[] {ti.getTagName()});
    //}

    TagExtraInfo tagExtraInfo = ti.getTagExtraInfo();
    if (tagExtraInfo != null) {
      if (tagExtraInfo.getVariableInfo(tagData) != null
        && tagExtraInfo.getVariableInfo(tagData).length > 0
        && ti.getTagVariableInfos().length > 0) {

        throw new JspParseException(JspParseException.ERROR_NON_NULL_TEI_AND_VAR_SUBELEMENTS, new Object[] {ti.getTagName()});
      }

      ValidationMessage[] messages = tagExtraInfo.validate(tagData);
      if (messages != null && messages.length > 0) {
        StringBuffer sb = new StringBuffer("\r\n");
        for (int i = 0; i < messages.length; i++) {
          sb.append("id["+messages[i].getId()+"] - "+messages[i].getMessage()+"\r\n");
        }
        throw new JspParseException(JspParseException.ERROR_TEI_VALIDATION_ERROR, new Object[] {ti.getTagName(), sb.toString()});
      }
    }



  }

  /**
   * Generates the all needed methods for setting attributes, a refference to PageContext and to parent action
   * into a generated java file.
   *
   * @param   writer  A StringBuffer containing the source of the generating java file
   * @param   parent  A name of the parent tag
   * @exception   JspParseException
   */
  private final void generateSetters(StringBuffer writer, Vector fragmentMethods, String parent, String mapName) throws JspParseException {
    if (!isSimpleTag) {
      writer.append(ident).append(thVarName).append(".setPageContext(_jspx_pageContext);").append("\n");
    } else {
      if( mapName == null ){
        writer.append(ident).append(thVarName).append(".setJspContext(_jspx_pageContext);").append("\n");
      }else{
        writer.append(ident).append(thVarName).append(".setJspContext(_jspx_pageContext,").append(mapName).append(");").append("\n");
      }
    }
    
    if( !getParentTag().isRootTag() ) {
      if (getParentTag().isSimpleTag | isInMethod) {
        String parentStr = "_jspx_parent";
        if (!isSimpleTag && getParentTag().isSimpleTag && !isInMethod) {
          parentStr = "new javax.servlet.jsp.tagext.TagAdapter((javax.servlet.jsp.tagext.SimpleTag) _jspx_parent)";
        }
        writer.append(ident + new String(thVarName + ".setParent(" + parentStr + ");")).append("\n");

        //writer.append(ident + new String(thVarName + ".setParent(_jspx_parent);")).append("\n");
      } else {
        writer.append(ident + new String(thVarName + ".setParent(" + parent + ");")).append("\n");
      }
    }

    /*
     * javax.servlet.jsp.tagext.JspIdConsumer interface indicates to the container that a tag handler wishes to be provided with a compiler generated ID.
     */
    if ( JspIdConsumer.class.isAssignableFrom(tc.getTagHandlerClass()) ){
      writer.append(ident + new String(thVarName + ".setJspId(\"" + parser.getNextTagID()+ "\");")).append("\n");
    }

    if (attrs != null) {
      for (int i = 0; i < attrs.length; i++)  {
        String attrName = attrs[i].name.toString();
        String attrValue = attrs[i].value.toStringUnquoted();

        TagAttributeInfo attrInfo = null;
        for (int j = 0; j < attributes.length; j++) {
          if (attributes[j].getName().endsWith(attrName)) {
            attrInfo = attributes[j];
            break;
          }
        }

        /*
         * If the tag implements DynamicAttributes then the attribute
         * will not be set using its setter method, but using the
         * setDynamicAttribute() method
         */
        Method m = tc.getSetterMethod(attrName);
        if (m == null && !implementsDynamicAttribs) {
          throw new JspParseException(JspParseException.UNABLE_TO_FIND_METHOD_FOR_ATTRIBUTE, new Object[]{attrName});
        }


        Class c[] = null;

        if (m != null) {
          c = m.getParameterTypes();
        }
        Class classForEl = c == null ? null : c[0];
        if (c == null) {
          c = new Class[1];
          c[0] = java.lang.String.class;
//        } else if (c[0] == java.lang.Object.class) {
//          c[0] = java.lang.String.class;
        } else if (c[0] == javax.servlet.jsp.tagext.JspFragment.class) {
          c[0] = java.lang.String.class;
        }
        //save the variable name before calculate
        String attrValueVariable = attrValue;
        attrValue = calculateAttributeValue(attrValue, attrName, attrs[i].isNamed, attrInfo, classForEl, c);

        if (attrInfo != null && attrInfo.isFragment()) {
          String varName = "_jspx_temp" + fragmentsCount;
          writer.append(ident + "javax.servlet.jsp.tagext.JspFragment " + varName + " = " + "new " + pageClassName + "_Helper(" + fragmentsCount + ", _jspx_pageContext, " + thVarName + ", null);").append("\n");

          if (isExpression(attrValue)) {
            attrValue = getExpr(attrValue);
          }

          StringBuffer fragmentMethod = new StringBuffer();
          fragmentMethod.append("\t\tpublic void invoke" + fragmentsCount + "(JspWriter out)\n");
          fragmentMethod.append("\t\t\tthrows Throwable\n");
          fragmentMethod.append("\t\t{\n");
          if (attrs[i].isNamed) {
            String jspAttrCode = (String) parser.getJspAttributesCode().get(attrValueVariable);
            if (jspAttrCode != null) {
              fragmentMethod.append("\t\t\t" + jspAttrCode);
              parser.getJspAttributesCode().remove(attrValueVariable);
            }
          }
          fragmentMethod.append("\t\t\tout.write(" + attrValue + ");\n");
          fragmentMethod.append("\t\t}\n");
          fragmentMethods.add(fragmentMethod);

          writer.append(ident + thVarName + "." + m.getName() + "(" + varName + ");").append("\n");

          fragmentsCount++;
        } else {
          if (attrs[i].isNamed) {
            String jspAttrCode = (String) parser.getJspAttributesCode().get(attrValueVariable);
            if (jspAttrCode != null) {
              writer.append(ident + jspAttrCode);
              parser.getJspAttributesCode().remove(attrValueVariable);
            }
          }
          if (m == null) {
            //Table JSP.8-2 Details of tag directive attributes
            // Only dynamic attributes with no uri are to be present in the Map with dynamic attrs; all other are ignored.
            if (attrName.startsWith(prefix + ":")) {
              writer.append(ident + thVarName + ".setDynamicAttribute(\""+prefix +"\", \"" + attrName.substring(prefix.length() + 1) + "\", " + attrValue + ");").append("\n");
            } else {
              writer.append(ident + thVarName + ".setDynamicAttribute(null, \"" + attrName + "\", " + attrValue + ");").append("\n");
            }
          } else {
            writer.append(ident + thVarName + "." + m.getName() + "(" + attrValue + ");").append("\n");
          }
        }
      }
    }
  }
  
  /**
   * Helper method that calculates the generated value for this attribute valule taken from the JSP.
   * @param attrValue - the value of the attribute as is in the JSP tag. 
   * @param attrName - the name of the attribute
   * @param isNamed - if this is <jsp:attribute> tag
   * @param attrInfo - the TLD info about this attribute
   * @param classForEl - the type of the generated 
   * @param c - the class of the attribute
   * @return the string for the generated java
   * @throws JspParseException
   */
  private String calculateAttributeValue(String attrValue, String  attrName, boolean isNamed, TagAttributeInfo attrInfo, Class classForEl, Class c[]) throws JspParseException {
    // Dynamic attrs are considered to accept request-time expression values (JSP.13.3)
    boolean acceptRequestTime = attrInfo == null ? true : attrInfo.canBeRequestTime();
    
    //check for EL expression
    ELExpression expr = new ELExpression(attrValue);
    boolean jspVersion21OrHigher = StringUtils.greaterThan(ti.getTagLibrary().getRequiredVersion(), 2.0);
    if (expr.isDeffered()){
      verifyDeferredAllowed(jspVersion21OrHigher, attrInfo, attrValue);
      if (classForEl == java.lang.Object.class) {
        classForEl = javax.el.ValueExpression.class;
      }
    } else {
      //JSP.5.10 <jsp:attribute>
      //For standard or custom action attributes that do not accept a request-time expression
      //value, the Container must use the body of the <jsp:attribute> action as
      //the value of the attribute. A translation error must result if the body of the
      //<jsp:attribute> action contains anything but template text.
      if (!acceptRequestTime && (isExpression(attrValue) || (expr.isEL() && !parser.getIsELIgnored()) || isNamed) ) {
        throw new JspParseException(JspParseException.ATTRIBUTE_CAN_ACCEPT_ONLY_STATIC_VALUES, new Object[]{attrName, "<"+prefix+":"+shortTagName+">"});
      }     
    }
    classForEl = classForEl == null ? c[0] : classForEl;
    
     if (isExpression(attrValue)) {
      // runtime expression
      if (acceptRequestTime) {
        attrValue = InnerExpression.escapeUnquoted(getExpr(attrValue));
      } else { 
        throw new JspParseException(JspParseException.ATTRIBUTE_CAN_ACCEPT_ONLY_STATIC_VALUES, new Object[]{attrName, "<"+prefix+":"+shortTagName+">"});            
      }
    } else if ( expr.isEL() && !parser.getIsELIgnored() ){
      // EL expression
      if(  ( expr.isDeffered() && jspVersion21OrHigher && !parser.isDeferredSyntaxAllowedAsLiteral()) ||
          !expr.isDeffered()) {
        // EL expression
        attrValue = InnerExpression.getAttributeExpresionEvaluation(attrValue, classForEl,
          parser, attrInfo == null ? null : attrInfo.getExpectedTypeName(), attrInfo == null ? null : attrInfo.getMethodSignature(), expr.isDeffered());
      } else if(expr.isDeffered() && jspVersion21OrHigher && parser.isDeferredSyntaxAllowedAsLiteral() && attrInfo != null && (attrInfo.isDeferredValue() || attrInfo.isDeferredMethod())) {
        //assume that should evaluate EL but with literal value, because parser.isDeferredSyntaxAllowedAsLiteral()=true
        attrValue = InnerExpression.getAttributeExpresionEvaluation("\\\\"+attrValue, classForEl,
          parser, attrInfo == null ? null : attrInfo.getExpectedTypeName(), attrInfo == null ? null : attrInfo.getMethodSignature(), expr.isDeffered());
      } else { // String literal for deferred attribute, but deferred not allowed
        attrValue = convertString(java.lang.String.class, attrValue, attrName, tc.getPropertyEditorClass(attrName), isNamed);
      }
    } else {
      String elValue = null;
      // template text
      if(  attrInfo!= null && (attrInfo.isDeferredMethod() || attrInfo.isDeferredValue() ) ) {// # EL expression - convert to ValueExpression or MethodExpression
        elValue = InnerExpression.getAttributeExpresionEvaluation(attrValue, classForEl ,
            parser, attrInfo == null ? null : attrInfo.getExpectedTypeName(), attrInfo == null ? null : attrInfo.getMethodSignature(), true);       
        if( elValue == null ) { // $ EL expression
          attrValue = convertString(c[0], attrValue, attrName, tc.getPropertyEditorClass(attrName), isNamed);
        } else {
          attrValue = elValue; 
        }
      }else {
        attrValue = convertString(c[0], attrValue, attrName, tc.getPropertyEditorClass(attrName), isNamed);
      }

    }
    return attrValue;
  }
  
  /**
   * Checks and throws exception if deferred values are not supported for this attribute.
   * @param jspVersion21OrHigher - if version of the taglibrary is 2.1 or greater
   * @param attrInfo - javax.servlet.jsp.tagext.TagAttributeInfo  
   * @param attrValue - the value for this attrrbiute taken from the JSP
   * @throws JspParseException - if deferred expression is not allowed for value of this attribute. 
   */
  private void verifyDeferredAllowed(boolean jspVersion21OrHigher, TagAttributeInfo attrInfo, String attrValue) throws JspParseException {
    if (jspVersion21OrHigher && !parser.isDeferredSyntaxAllowedAsLiteral() && !parser.getIsELIgnored()) {
      // JSP 2.1 or higher
      if (!attrInfo.isDeferredMethod() && !attrInfo.isDeferredValue()) {
        throw new JspParseException(JspParseException.DEFERRED_NOT_ALLOWED_FOR_TAG_ATTRIBUTE, new Object[] { prefix + ":" + ti.getTagName(), attrInfo.getName() + "=" + attrValue });
      }
    }
  }

  public boolean isSimpleTag() {
    return isSimpleTag;
  }

  /**
   * Generates a necessary code into a java file after encountering a beginning
   * action tag.
   * 
   * @param writer
   *          A StringBuffer containing the source of the generating java file
   * @exception JspParseException
   */
  private void generateServiceMethodStatements(StringBuffer writer, Vector fragmentMethods) throws JspParseException {
    writer.append(ident + "// TagBeginGenerator - start [").append(prefix+":"+shortTagName).append("]\n");

    TagVariableData top = topTag();
    String parent = top == null ? null : top.tagHandlerInstanceName;
    String evalVar = "_jspx_eval_" + baseVarName;
    tagBegin(new TagVariableData(thVarName, evalVar, isSimpleTag));
    writer.append(ident + new String("/* ----  " + prefix + ":" + shortTagName + " ---- */")).append("\n");
    writer.append(ident + new String(ti.getTagClassName() + " " + thVarName + " = new " + ti.getTagClassName() + "();")).append("\n");

    insertInjectSting(writer);
    
    String mapName = generateAliasMap(writer);
    generateSetters(writer, fragmentMethods, parent, mapName);
    VariableInfo[] vi = ti.getVariableInfo(tagData);
    declareVariables(writer, vi, true, false, VariableInfo.AT_BEGIN, ident);
    TagVariableInfo[] tvi = ti.getTagVariableInfos();
    declareVariables(writer, tvi, true, false, VariableInfo.AT_BEGIN, ident, attrs);
    if (isInMethod) {
      declareVariables(writer, vi, true, false, VariableInfo.AT_END, ident);
      declareVariables(writer, tvi, true, false, VariableInfo.AT_END, ident, attrs);
    }
    if (isSimpleTag)
    {
      if (hasBody) {
        String varName = "_jspx_temp" + fragmentsCount;
        writer.append(ident + "javax.servlet.jsp.tagext.JspFragment " + varName + " = " + "new " + pageClassName + "_Helper(" + fragmentsCount + ", _jspx_pageContext, " + thVarName + ", null);").append("\n");
        writer.append(ident + thVarName + ".setJspBody(" + varName + ");").append("\n");

        StringBuffer fragmentMethod = new StringBuffer();
        fragmentMethod.append("\t\tpublic void invoke" + fragmentsCount + "(JspWriter out)\n");
        fragmentMethod.append("\t\t\tthrows Throwable\n");
        fragmentMethod.append("\t\t{\n");
        fragmentMethods.add(fragmentMethod);

        fragmentsCount++;
      }
      writer.append(ident + new String(thVarName + ".doTag();")).append("\n");

      // variable synchronization is in TagEndGenerator
    } else {
      writer.append(ident + new String("try {" + "\n"));
      ident = ident.concat("	");
      writer.append(ident + new String("int " + evalVar + " = " + thVarName + ".doStartTag();")).append("\n");
      boolean implementsBodyTag = BodyTag.class.isAssignableFrom(tc.getTagHandlerClass());
      declareVariables(writer, vi, false, true, VariableInfo.AT_BEGIN, ident);
      declareVariables(writer, tvi, false, true, VariableInfo.AT_BEGIN, ident, attrs);

      //    if (implementsBodyTag) {
      //      writer.append(ident + new String("if (" + evalVar + " == Tag.EVAL_BODY_INCLUDE)")).append("\n");
      //      ident = ident.concat("  ");
      //      writer.append(ident + new String("throw new JspTagException(\"Since tag handler " + tc.getTagHandlerClass() + " implements BodyTag, it can't return Tag.EVAL_BODY_INCLUDE\");")).append("\n");
      //      ident = ident.substring(0, ident.length() - 1);
      //    } else {
      if (!implementsBodyTag) {
        writer.append(ident + "if (" + evalVar + " == javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_BUFFERED) {").append("\n");
        ident = ident.concat("\t");
        String msg = "Since tag handler " + tc.getTagHandlerClass() + " does not implement BodyTag, it can't return BodyTag.EVAL_BODY_BUFFERED";
        writer.append(ident + "throw new JspTagException(\"" + msg + "\");").append("\n");
        ident = ident.substring(0, ident.length() - 1);
        writer.append(ident + "}").append("\n");
      }

      if (hasBody && !isSingleTag) {
        writer.append(ident + "if (" + evalVar + " != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {").append("\n");
        ident = ident.concat("\t");

        if (implementsBodyTag) {
          writer.append(ident + "try {").append("\n");
          ident = ident.concat("\t");
          writer.append(ident + "if (" + evalVar + " == javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_BUFFERED) {").append("\n");
          ident = ident.concat("\t");
          writer.append(ident + "out = _jspx_pageContext.pushBody();").append("\n");
          writer.append(ident + thVarName + ".setBodyContent((javax.servlet.jsp.tagext.BodyContent) out);").append("\n");
          writer.append(ident + thVarName + ".doInitBody();").append("\n");
          ident = ident.substring(0, ident.length() - 1);
          writer.append(ident + "}").append("\n");
        }
//        if (implementsBodyTag) {
//          writer.append(ident + "try {").append("\n");
//          ident = ident.concat("\t");
//          writer.append(ident + "if (" + evalVar + " != Tag.EVAL_BODY_INCLUDE) {").append("\n");
//          ident = ident.concat("\t");
//          writer.append(ident + "out = _jspx_pageContext.pushBody();").append("\n");
//          writer.append(ident + thVarName + ".setBodyContent((BodyContent) out);").append("\n");
//          ident = ident.substring(0, ident.length() - 1);
//          writer.append(ident + "} else if (" + evalVar + " == BodyTag.EVAL_BODY_BUFFERED) {").append("\n");
//          writer.append(ident + "\t" + thVarName + ".doInitBody();").append("\n");
//          writer.append(ident + "}").append("\n");
//        }
        writer.append(ident + "do {").append("\n");
        ident = ident.concat("\t");
      }
      // variable synchronization for classic tags only
      // for SimpleTags varsync is in TagEndGenerator
      declareVariables(writer, vi, true, true, VariableInfo.NESTED, ident);
      declareVariables(writer, tvi, true, true, VariableInfo.NESTED, ident, attrs);

      declareVariables(writer, vi, false, true, VariableInfo.AT_BEGIN, ident);
      declareVariables(writer, tvi, false, true, VariableInfo.AT_BEGIN, ident, attrs);
    }

    writer.append(ident + "// TagBeginGenerator - end [").append(prefix+":"+shortTagName).append("]\n");
  }
  
  /**
   * Writes the code for injecting the tag hadler instance.
   * @param writer
   */
  private void insertInjectSting(StringBuffer writer) {
    // injection
    if (StringUtils.greaterThan(parser.getParserParameters().getJspConfigurationProperties().getSpecificationVersion(), 2.4)) {
      if (isInMethod) {
        writer.append(ident).append("com.sap.engine.services.servlets_jsp.server.application.InjectionWrapper _jspx_resourceInjector = ((com.sap.engine.services.servlets_jsp.server.application.ServletContextImpl)_jspx_pageContext.getServletContext()).getApplicationContext().getInjectionWrapper();")            .append("\n");
      }
      writer.append(ident).append("_jspx_resourceInjector.inject(").append(thVarName).append(");").append("\n");
    }
  }

  /**
   * Generates necessary code into a java file after encountering a closing action tag.
   *
   * @param   writer  A StringBuffer containing the source of the generating java file
   * @exception   JspParseException
   */
  public void generate(StringBuffer writer, Vector fragmentMethods, boolean isInMethod, boolean hasBody, boolean isSingleTag) throws JspParseException {
    this.isInMethod = isInMethod;
    this.isSingleTag = isSingleTag;
    this.hasBody = hasBody;
    generateServiceMethodStatements(writer, fragmentMethods);
  }

  /**
   * Checks if some String can be a runtime expression.
   *
   * @param   token  some String representing attribute value
   * @return     if the String is runtime expression
   */
  private boolean isExpression(String token) {
    if ((token.startsWith("<%=") || token.startsWith("<jsp:expression") || token.startsWith("%=")) && (token.endsWith("%>") || token.endsWith("/>") || token.endsWith("%"))) {
      return true;
    }

    return false;
  }

  /**
   * Checks if some String can be a runtime expression and return this expression as
   * a valid java code.
   *
   * @param   expression  some String representing attribute value
   * @return     the expression as a valid java code.
   */
  private String getExpr(String expression) {
    String returnString = "";

    if (expression.startsWith("<%=")) {
      returnString = expression.substring("<%=".length());
    } else if (expression.startsWith("<jsp:expression")) {
      returnString = expression.substring("<jsp:expression".length());
    } else if (expression.startsWith("%=")) {
      returnString = expression.substring("%=".length());
    }

    int length = returnString.length();

    if (expression.endsWith("%>")) {
      returnString = returnString.substring(0, length - "%>".length());
    } else if (expression.endsWith("/>")) {
      returnString = returnString.substring(0, length - "/>".length());
    } else if (expression.endsWith("%")) {
      returnString = returnString.substring(0, length - "%".length());
    }

    return returnString;//quoteString(returnString);
  }

  public String convertString(Class c, String s, String attrName, Class propertyEditorClass, boolean isNamedAttribute) throws JspParseException {
    if (propertyEditorClass != null) {
      return "(" + c.getName() + ") com.sap.engine.services.servlets_jsp.lib.jspruntime.RunLibrary.getValueFromBeanInfoPropertyEditor(" + (isNamedAttribute ? s : quoteString(s)) + ", " + propertyEditorClass.getName() + ".class)";
    }
    if (c == java.lang.Object.class) {
      c = java.lang.String.class;
    }
    //This is done in JspElement.evaluateJspAttribute
    //    if (isNamedAttribute) {
    //      s += "(out,_jspx_pageContext)";
    //    }
    if (c == String.class) {
      if (isNamedAttribute) {
        return s;
      } else {
        return quoteString(s);
      }
    } else if (c == boolean.class) {
      return Boolean.valueOf(s).toString();
    } else if (c == Boolean.class) {
      if (isNamedAttribute){
        return "Boolean.valueOf(" + s + ")";
      } else {
        return "new Boolean(" + Boolean.valueOf(s).toString() + ")";
      }
    } else if (c == byte.class) {
      return "((byte)" + Byte.valueOf(s).toString() + ")";
    } else if (c == Byte.class) {
      if (isNamedAttribute){
        return "Byte.valueOf(" + s + ")";
      } else {
        return "new Byte((byte)" + Byte.valueOf(s).toString() + ")";
      }
    } else if (c == char.class) {
      if (s.length() == 1) {
        char ch = s.charAt(0);
        return "((char) " + (int) ch + ")";
      } else {
        throw new JspParseException(JspParseException.UNKNOWN_VALUE_OF_CHARACTER);
      }
    } else if (c == Character.class) {
      if (s.length() == 1) {
        char ch = s.charAt(0);
        return "new Character((char) " + (int) ch + ")";
      } else if (isNamedAttribute){
        return "new Character(" + s + ".charAt(0))";
      } else {
        throw new JspParseException(JspParseException.UNKNOWN_VALUE_OF_CHARACTER);
      }
    } else if (c == double.class) {
      return Double.valueOf(s).toString();
    } else if (c == Double.class) {
      if (isNamedAttribute) {
       return "Double.valueOf(" + s + ")";
      } else {
        return "new Double(" + Double.valueOf(s).toString() + ")";
      }
    } else if (c == float.class) {
      return Float.valueOf(s).toString() + "f";
    } else if (c == Float.class) {
      if (isNamedAttribute) {
        return "Float.valueOf(" + s + ")";
      } else {
        return "new Float(" + Float.valueOf(s).toString() + "f)";
      }
    } else if (c == int.class) {
      return Integer.valueOf(s).toString();
    } else if (c == Integer.class) {
      if (isNamedAttribute) {
        return "Integer.valueOf("+ s + ")";
      } else {
        return "new Integer(" + Integer.valueOf(s).toString() + ")";
      }
    } else if (c == long.class) {
      return Long.valueOf(s).toString() + "l";
    } else if (c == Long.class) {
      if (isNamedAttribute) {
        return "Long.valueOf(" + s + ")";
      } else {
        return "new Long(" + Long.valueOf(s).toString() + "l)";
      }
    } else if (c == short.class) {
      return "((short) " + Short.valueOf(s).toString() + ")";
    } else if (c == Short.class) {
      if (isNamedAttribute) {
        return "Short.valueOf(" + s + ")";
      } else {
        return "new Short((short) " + Short.valueOf(s).toString() + ")";
      }
    } else {
      throw new JspParseException(JspParseException.UNKNOWN_CLASS_NAME, new Object[]{c.getName()});
    }
  }

  public String getBaseVarName() {
    return baseVarName;
  }

  public TagData getTagData() {
    return tagData;
  }

  public String getIdent() {
    return ident;
  }

  public int getFragmentsCount() {
    return fragmentsCount;
  }

  /**
   * 
   * @param writer
   * @return String - the name of the table where aliases are kept
   * @throws JspParseException
   */
  private final String generateAliasMap(StringBuffer writer) throws JspParseException{

    TagVariableInfo[] variableInfos = ti.getTagVariableInfos();
    if( variableInfos == null ){
      return null;
    }

    boolean mapAdded = false;
    String mapName = null;

    // name given is used for alias when name-from-attribute is present
    // see VariableDirectiveTag.action()
    for (int i = 0; i < variableInfos.length; i++) {
      TagVariableInfo info = variableInfos[i];
      if( info.getNameFromAttribute() != null && info.getNameGiven() != null ){
        if( !mapAdded ){
          mapName = thVarName+"_aliasMap";
          writer.append("\n").append(ident).append("java.util.HashMap ").append(  mapName).append(" = new java.util.HashMap();").append("\n");
          mapAdded = true;
        }
        Attribute attribute = getAttributeByName(info.getNameFromAttribute()) ;
        writer.append("\n").append(ident).append(mapName+".put(\""+info.getNameGiven()+"\",\""+attribute.value.toStringUnquoted()+"\");").append("\n");

      }
    }
    return mapName;
  }

  private Attribute getAttributeByName(String name) throws JspParseException{
    for (int i = 0; i < attrs.length; i++) {
      Attribute attribute = attrs[i];
      if ( attribute.isName(name) ){
        return attribute;
      }
    }
    return null;
  }
}

