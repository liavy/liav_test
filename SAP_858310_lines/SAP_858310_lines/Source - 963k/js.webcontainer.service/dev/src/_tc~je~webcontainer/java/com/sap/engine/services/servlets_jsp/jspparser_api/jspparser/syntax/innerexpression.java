/*
 * Created on 2004-7-9
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.lib.jspruntime.FunctionMapperImpl;

/**
 * Class representing a portion of template data
 */
public class InnerExpression extends Element {

  private String value;
  private boolean isEL = false;
  private Class toClass = java.lang.String.class;
  public static final String METHOD__RETURN_TYPE = "methodReturnType";
  public static final String METHOD_PARAMETERS = "methodParameters";
  
  public static final String METHOD_RETURN_TYPE_VOID = "void";
  /**
   * Construct new inner expression with
   * the specified value
   * @param value
   */
  public InnerExpression(String value) {
    this.value = value;
  }

  /**
   * Constructs new InnerExpression
   *
   * @param   start  start of the template data
   * @param   end end of the template data
   */
  public InnerExpression(int start, int end) {
    startIndex = start;
    endIndex = end;
  }

  /**
   * Takes specific action coresponding to this jsp element
   * logic
   *
   * @exception   JspParseException  thrown if error occures during
   * verification
   */
  public void action(StringBuffer writer) throws JspParseException {
    if (value == null) {
      value = parser.getChars(startIndex, endIndex);
    }
    if (isEL) {
      expressionAction(writer);
    } else {
      templateAction(writer);
    }
  }

  public void action() throws JspParseException {
    action(parser.getScriptletsCode());
  }

  /**
   * This element has no indent
   * @return  null
   */
  public char[] indent() {
    return null;
  }

  /**
   * No parsing is performed.
   *
   * @param   parser
   * @return  null
   */
  public Element parse(JspPageInterface parser) {
    return null;
  }

  /**
   * Sets whether this template data part
   * is an EL and its return type or not.
   *
   * @param toClass
   */
  public void setToClass(Class toClass) {
    this.toClass = toClass;
  }

  public boolean isEL() {
    return isEL;
  }


  /**
   * Performs action that coresponds to expression data which
   * return type is coerce to string.This can happen when the
   * expression is in
   */
  private void expressionAction(StringBuffer writer) throws JspParseException { //template
    if (!isEL || !toClass.equals("java.lang.String")) {
    	return;
    }
    String funcMapper = FunctionMapperImpl.getFunctionMapper(value, parser);
    if (funcMapper == null) {
      writer.append("\t\t\tout.print(");
      writer.append("(java.lang.String) _jspx_pageContext.evaluateInternal(\""+convertLiterals(value)+"\", java.lang.String.class, false, null)");
      writer.append(");").append("\r\n");
    } else {
      writer.append("\t\t\tout.print(");
      writer.append("(java.lang.String) _jspx_pageContext.evaluateInternal(\""+convertLiterals(value)+"\", java.lang.String.class, false, " + funcMapper + ")");
      writer.append(");").append("\r\n");
    }
  }

  public static String getAttributeExpresionEvaluation(String expressions, Class toClass, JspPageInterface parser, String defferedType,
                                                       String methodSigniture, boolean isDeferred) throws JspParseException {

    if (isDeferred) {
      if (toClass == javax.el.MethodExpression.class) {
        return getDeferredMethodEvaluation(escapeUnquoted(expressions.trim()), parser, defferedType, methodSigniture);
      } else if (toClass == javax.el.ValueExpression.class){
        return getDeferredValueEvaluation(escapeUnquoted(expressions.trim()), parser, defferedType);
      }
    } else {
       return getExpresionEvaluation(escapeUnquoted(expressions.trim()), toClass, parser);
    }
    return null;
  }

  public static String getExpresionEvaluation(String value, Class toClass, JspPageInterface parser) throws JspParseException {   //attribute
    StringBuffer call = new StringBuffer();

    String targetType = toClass.getName();
    String primitiveConverterMethod = null;
    if (toClass.isPrimitive()) {
      if (toClass.equals(Boolean.TYPE)) {
        targetType = Boolean.class.getName();
        primitiveConverterMethod = "booleanValue";
      } else if (toClass.equals(Byte.TYPE)) {
        targetType = Byte.class.getName();
        primitiveConverterMethod = "byteValue";
      } else if (toClass.equals(Character.TYPE)) {
        targetType = Character.class.getName();
        primitiveConverterMethod = "charValue";
      } else if (toClass.equals(Short.TYPE)) {
        targetType = Short.class.getName();
        primitiveConverterMethod = "shortValue";
      } else if (toClass.equals(Integer.TYPE)) {
        targetType = Integer.class.getName();
        primitiveConverterMethod = "intValue";
      } else if (toClass.equals(Long.TYPE)) {
        targetType = Long.class.getName();
        primitiveConverterMethod = "longValue";
      } else if (toClass.equals(Float.TYPE)) {
        targetType = Float.class.getName();
        primitiveConverterMethod = "floatValue";
      } else if (toClass.equals(Double.TYPE)) {
        targetType = Double.class.getName();
        primitiveConverterMethod = "doubleValue";
      }
    }
    targetType = toJavaSourceType(targetType);
    call.append("(");
    call.append(targetType);
    call.append(")");
    call.append("_jspx_pageContext.evaluateInternal(");
    call.append(quote(value));
    call.append(",").append(targetType);
    call.append(".class, false");
    if (parser.isTagFile()) {
      call.append(", (PageContext) this.getJspContext()");
    }
    String funcMapper = FunctionMapperImpl.getFunctionMapper(value, parser);
    if (funcMapper == null) {
      call.append(", null)");
    } else {
      call.append(", " + funcMapper + ")");
    }

    if (primitiveConverterMethod != null) {
      call.insert(0, "(");
      call.append(")." + primitiveConverterMethod + "()");
    }


    return call.toString();
  }

  private static String getDeferredValueEvaluation(String value, JspPageInterface parser, String defferedType) throws JspParseException {   //attribute
    if (defferedType == null) {
      defferedType = "java.lang.Object";
    }
    String funcMapper = FunctionMapperImpl.getFunctionMapper(value, parser);
    if (funcMapper == null) {
      funcMapper = ", null";
    } else {
      funcMapper = ", " + funcMapper;
    }
    if (parser.isTagFile()) {
      return "_jspx_pageContext.getValueExpression(" + quote(value) + ", (PageContext) this.getJspContext(), " + defferedType + ".class" + funcMapper + ")";
    } else {
      return "_jspx_pageContext.getValueExpression(" + quote(value) + ", _jspx_pageContext, " + defferedType + ".class" + funcMapper + ")";
    }
  }

  private static String getDeferredMethodEvaluation(String value, JspPageInterface parser, String defferedType, String methodSigniture) throws JspParseException {   //attribute
    if (defferedType == null) {
      defferedType = "Void";
    }
    String funcMapper = FunctionMapperImpl.getFunctionMapper(value, parser);
    if (funcMapper == null) {
      funcMapper = ", null";
    } else {
      funcMapper = ", " + funcMapper;
    }
    HashMap<String, Object> signature = null;
    String methodReturnType = null;
    String[] methodParamTypes = null;
    if (methodSigniture == null) {
      methodReturnType = "Void";
    } else {
      signature = parseSignature(methodSigniture);
      methodReturnType = (String) signature.get(METHOD__RETURN_TYPE);
      methodParamTypes = (String[]) signature.get(METHOD_PARAMETERS);
    }
    StringBuffer mParams = new StringBuffer();
    if (methodParamTypes != null) {
      for (int i = 0; i < methodParamTypes.length; i++) {
        if (i > 0) {
          mParams.append(", ");
        }
        mParams.append(methodParamTypes[i] + ".class");
      }
    }
    if (parser.isTagFile()) {
      return "_jspx_pageContext.getMethodExpression(" + quote(value) + ", (PageContext) this.getJspContext()" + funcMapper
      + ", " + methodReturnType + ".class, " + "new Class[] {" + mParams.toString() + "}" + ")";
    } else {
      return "_jspx_pageContext.getMethodExpression(" + quote(value) + ", _jspx_pageContext" + funcMapper
      + ", " + methodReturnType + ".class, " + "new Class[] {" + mParams.toString() + "}" + ")";
    }

  }


  private static String toJavaSourceType(String type) {

    if (type.charAt(0) != '[') {
      return type;
    }

    int dims = 1;
    String t = null;
    for (int i = 1; i < type.length(); i++) {
      if (type.charAt(i) == '[') {
        dims++;
      } else {
        switch (type.charAt(i)) {
          case 'Z':
            t = "boolean";
            break;
          case 'B':
            t = "byte";
            break;
          case 'C':
            t = "char";
            break;
          case 'D':
            t = "double";
            break;
          case 'F':
            t = "float";
            break;
          case 'I':
            t = "int";
            break;
          case 'J':
            t = "long";
            break;
          case 'S':
            t = "short";
            break;
          case 'L':
            t = type.substring(i + 1, type.indexOf(';'));
            break;
        }
        break;
      }
    }
    StringBuffer resultType = new StringBuffer(t);
    for (; dims > 0; dims--) {
      resultType.append("[]");
    }
    return resultType.toString();
  }

  /**
   * Performs action that coresponds to template jsp data
   *
   */
  private void templateAction(StringBuffer writer) {
    writer.append("\t\t\tout.print(");
    writer.append(quote(parser.replaceTemplateTextQuotes(value)));
    writer.append(");").append("\r\n");
  }

  public static String quote(String s) {
    if (s == null)
      return "null";

    return '"' + escape(s) + '"';
  }

  public String getString(IDCounter id) {
    return null;
  }

  /**
   * Tokenizes the string into plain strings and EL strings
   * @param string
   * @return
   * @throws JspParseException
   */

  public static List<String> getInnerExpressions(String string) throws JspParseException {
    List<String> result = new ArrayList<String>();
    int currentIndex = -1;
    int endIndex = -1;
    while (endIndex < string.length()) {
      currentIndex = string.indexOf("${",endIndex+1);
      if (currentIndex > 0 && string.charAt(currentIndex - 1) == '\\') {   //not expression
        currentIndex = currentIndex - 1;
      }
      if (currentIndex == -1) { //end of EL elements
        String value = string.substring(endIndex+1);
        //InnerExpression expression = new InnerExpression(value);
        //expression.parser = parser;
        result.add(value);
        break;
      }
      if (endIndex + 1 < currentIndex) { //has template data between EL
        String value = string.substring(endIndex+1,currentIndex);
        //InnerExpression expression = new InnerExpression(value);
        result.add(value);
      }

      endIndex = string.indexOf("}",currentIndex);
      if (endIndex < currentIndex + 2 ) {
        throw new JspParseException(JspParseException.EL_HAS_NO_CLOSE_BRACKET, new Object[] {currentIndex});
      }

      String value = string.substring(currentIndex,endIndex+1);
      //InnerExpression expression = new InnerExpression(value);
      //expression.isEL = true;
      if ("".equals(value.trim())) {
        continue;
      }
      result.add(value);
    }

    return result;
  }

  public static String convertLiterals(String expr) {
    expr = expr.replaceAll("&eq;", "==");
    expr = expr.replaceAll("&ne;", "!=");
    expr = expr.replaceAll("&lt;", "<");
    expr = expr.replaceAll("&gt;", ">");
    expr = expr.replaceAll("&le;", "<=");
    expr = expr.replaceAll("&ge;", ">=");
    expr = unescape$(expr);

//    expr.replaceAll(" and ", " && ");
//    expr.replaceAll(" or ", " || ");
//    expr.replaceAll(" not ", " ! ");
    return expr;
  }

  public static String unescape$(String expr) {
    StringBuffer sb = new StringBuffer();
    char[] c = expr.toCharArray();
    for (int i = 0; i < c.length; i++) {
      if (c[i] == '\\') {
        if (c[i + 1] == '$') {
          continue;
        }
      }
      sb.append(c[i]);
    }
    return sb.toString();
  }

  /**
   * @param s the input string
   * @return escaped string, per Java rule
   */
  public static String escape(String s) {

	  if (s == null)
		  return "";

	  StringBuffer b = new StringBuffer();
	  for (int i = 0; i < s.length(); i++) {
		  char c = s.charAt(i);
		  if (c == '"')
			  b.append('\\').append('"');
		  else if (c == '\\')
			  b.append('\\').append('\\');
		  else if (c == '\n')
			  b.append('\\').append('n');
		  else if (c == '\r')
			  b.append('\\').append('r');
		  else
			  b.append(c);
	  }
	  return b.toString();
  }

  /**
   * @param s the input string
   * @return escaped string, per Java rule
   */
  public static String escapeUnquoted(String s) {

	  if (s == null)
		  return "";

	  StringBuffer b = new StringBuffer();
    int i = 0;
	  while (i < s.length())  {
		  char c = s.charAt(i);
		  if (c == '\\' && (i + 1) < s.length()) {
        c = s.charAt(i + 1);
        if (c == '\\' || c == '\"' || c == '\'' || c == '>') {
          b.append(c);
          i += 2;
        } else {
          b.append('\\');
          ++i;
        }
      } else {
			  b.append(c);
        ++i;
	    }
    }
	  return b.toString();
  }

  /**
   * Helper method for parsing the valule of the "method-signature" or "function-signature" elements of the TLD.
   * function-signature element is as follows:
   * FunctionSignature ::= ReturnType S MethodName S?
   * ’(’ S? Parameters? S? ’)’ 
   * ReturnType ::= Type
   * MethodName ::= Identifier
   * Parameters ::= Parameter
   * | ( Parameter S? ’,’ S? Parameters
   * Parameter ::= Type
   * Where:   
   * Type is a basic type or a fully qualified Java class name (including package name), as per the ’Type’ production in the Java Language Specification, Second Edition, Chapter 18.
   * Identifier is a Java identifier, as per the ’Identifier’ production in the Java Language Specification, Second Edition, Chapter 18.
   * @param signature - the signature e.g. "java.lang.String nickName( java.lang.String, int )"
   * @return - HashMap with key "METHOD__RETURN_TYPE"- value java.lang.String theReturnType 
   * and key "METHOD_PARAMETERS" -  value java.lang.String[] - parameters
   * @throws JspParseException
   */
  public static HashMap<String, Object> parseSignature(String signature) throws JspParseException {
    if (signature == null) {
      throw new JspParseException(JspParseException.ILLEGAL_METHOD_SIGNATURE, new String[] {signature});
    }
    signature = signature.trim();
    HashMap<String, Object> result = new HashMap<String, Object>();
    int start = signature.indexOf(' ');
    if (start < 0) {
      throw new JspParseException(JspParseException.ILLEGAL_METHOD_SIGNATURE, new String[] {signature});
    }
    result.put(METHOD__RETURN_TYPE, signature.substring(0, start).trim());
    int end = signature.indexOf('(');
    if (end < 0) {
      throw new JspParseException(JspParseException.ILLEGAL_METHOD_SIGNATURE, new String[] {signature});
    }

    //params
    List<String> params = new ArrayList<String>();
    start = end + 1;
    boolean lastArg = false;
    while (true) {
      int p = signature.indexOf(',', start);
      if (p < 0) {
        p = signature.indexOf(')', start);
        if (p < 0) {
          throw new JspParseException(JspParseException.ILLEGAL_METHOD_SIGNATURE, new String[] {signature});
        }
        lastArg = true;
      }
      String arg = signature.substring(start, p).trim();
      if (!"".equals(arg)) {
        params.add(arg);
      }
      if (lastArg) {
        break;
      }
      start = p + 1;
    }
    result.put(METHOD_PARAMETERS , params.toArray(new String[params.size()]));
    return result;
  }
}
