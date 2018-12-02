
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
package com.sap.engine.services.servlets_jsp.lib.jspruntime;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Array;
import java.util.*;

import javax.servlet.jsp.tagext.FunctionInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;

import com.sap.engine.lib.util.ConcurrentHashMapObjectObject;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.tc.logging.Location;

/**
 * Implements javax.el.FunctionMapper.
 * @author Bojidar Kadrev
 *
 */
public class FunctionMapperImpl extends javax.el.FunctionMapper {

  private static final String METHOD_NAME = "methodName";
  private static final String PARAMETERS = "parameters";
  private static final String[] elReserved = {"and", "eq", "gt", "true", "instanceof",
                                              "or", "ne", "le", "false", "empty",
                                              "not", "lt", "ge", "null", "div", "mod"};

  private static final String[] PRIMITIVE_STRINGS = new String[] { "boolean", "byte", "char", "double", "float", "int", "long", "short"};

  private static final Class[] PRIMITIVE_CLASSES = new Class[] { boolean.class, byte.class, char.class, double.class, float.class, int.class, long.class, short.class };

  private static Location currentLocation = Location.getLocation(FunctionMapperImpl.class);
  private static final String COLON = ":";
  private static final String DELIMETERS = " $!(){}/*+-%><=";

  /**
   * Maps "prefix:name" to java.lang.Method objects.
   */
  private ConcurrentHashMapObjectObject map = null;

  /**
   * Constructor
   */
  public FunctionMapperImpl() {
    this.map = new ConcurrentHashMapObjectObject();
  }

  /**
   * Implements method from  javax.el.FunctionMapper.
   * Resolves the specified local name and prefix into a java.lang.Method.
   * @param prefix the prefix of the function, or empty String if no prefix.
   * @param localName the short name of the function
   * @return  the result of the method mapping. Null means no entry found.
   */
  public Method resolveFunction(String prefix, String localName) {
    return (Method) map.get(prefix + ":" + localName);
  }

  /**
   * Instantiates new FunctionMapper implementation.
   * @return  FunctionMapperImpl
   */
  public static FunctionMapperImpl getFunctionMapper() {
    return new FunctionMapperImpl();
  }

  /**
   * Checks if the expression contains function in form of fn:name() and
   *
   * @param expr EL expression like : !fn:contains(pageContext.request.contextPath, \"test\")}
   * @return  String[] with all functions found in form of "fn:contains"
   */
  private static String[] parseFunctionsInExpression(String expr) {
    if (expr == null) {
      return null;
    }
    int ind = expr.indexOf(COLON);
    if (ind == -1) {
      return null;
    }
    Vector v = new Vector();
    StringTokenizer st = new StringTokenizer(expr, DELIMETERS);
    while(st.hasMoreTokens()){
      String token = st.nextToken();
      ind = token.indexOf(COLON);
      if (ind > 0) {
        if (!isReserved(token.substring(0, ind))) {
            v.add(token);
          }
      }
    }
    if (v.size() > 0) {
      Object[] obj = v.toArray();
      String[] res = new String[obj.length];
      for (int i = 0; i < obj.length; i++) {
        res[i] = (String)obj[i];
      }
      return res;
    }
    return null;
  }

  /**
   * Used to check the EL expresiion if it represents a function call
   * @param expression EL expression
   * @param parser
   * @return FunctionMapper name or null if it is not a function
   * @throws JspParseException
   */
  public static String getFunctionMapper(String expression, JspPageInterface parser) throws JspParseException {
    if (expression == null || !(expression.startsWith("${") || expression.startsWith("#{") || expression.endsWith("}"))) {
      return null;
    }
    String bodyExpr = expression.substring(2, expression.length() - 1).trim();
    String[] functions = parseFunctionsInExpression(bodyExpr);
    if (functions == null) {
      return null;
    }
    String funcMapperName = null;
    for (int i = 0; i < functions.length; i++) {
      funcMapperName = (String)parser.getFunctionsNames().get(functions[i]);
      if (funcMapperName != null) {
        break;
      }
    }

    for (int i = 0; i < functions.length; i++) {
      int ind1 = functions[i].indexOf(":");
      String prefix = functions[i].substring(0, ind1);
      String functionName = functions[i].substring(ind1 + 1);
      if (funcMapperName == null) {
        TagLibraryInfo info = (TagLibraryInfo)parser.getTaglibs().get(prefix);
        if (info != null) {
          FunctionInfo[] functionInfos = info.getFunctions();
          if (functionInfos != null) {
            for (int j=0; j < functionInfos.length; j++) {
              FunctionInfo function = functionInfos[j];
              if (function.getName().equals(functionName)) {
                Hashtable signature = parseSignature(function.getFunctionSignature(), functionName);
                String methodName = (String) signature.get(METHOD_NAME);
                if (checkFunction(function.getFunctionClass(), methodName, ((String[]) signature.get(PARAMETERS)), parser.getApplicationClassLoader())){
                  funcMapperName = "_jspx_fm_"+parser.getFunctionVarCount();
                  parser.getDeclarationsCode().append("static private "+ FunctionMapperImpl.class.getName()+" " + funcMapperName + ";\r\n");
                  parser.getFunctionsCode().append("\t" + funcMapperName);
                  parser.getFunctionsCode().append(" = "+FunctionMapperImpl.class.getName()+".getFunctionMapper();\r\n");
                  parser.getFunctionsCode().append("\t"+funcMapperName + ".map(");
                  parser.getFunctionsCode().append("\"" + functions[i] + "\", " + function.getFunctionClass() + ".class, \"" + methodName + "\", ");
                  parser.getFunctionsCode().append(processParams((String[]) signature.get(PARAMETERS)));
                  parser.getFunctionsCode().append(");\r\n");
                  parser.getFunctionsNames().put(functions[i], funcMapperName);
                  //return funcMapperName;
                }
              }
            }
          }
        }
        continue;
      }
      String funcExists = (String) parser.getFunctionsNames().get(functions[i]);
      if (funcExists != null && funcExists.equals(funcMapperName)) {
        continue;
      }
      TagLibraryInfo info = (TagLibraryInfo)parser.getTaglibs().get(prefix);
      if (info != null) {
        FunctionInfo[] functionInfos = info.getFunctions();
        if (functionInfos != null) {
          for (int j=0; j < functionInfos.length; j++) {
            FunctionInfo function = functionInfos[j];
            if (function.getName().equals(functionName)) {
              Hashtable signature = parseSignature(function.getFunctionSignature(), functionName);
              String methodName = (String) signature.get(METHOD_NAME);
              if (checkFunction(function.getFunctionClass(), methodName, ((String[]) signature.get(PARAMETERS)), parser.getApplicationClassLoader())){
                parser.getFunctionsCode().append("\t"+funcMapperName + ".map(");
                parser.getFunctionsCode().append("\"" + functions[i] + "\", " + function.getFunctionClass() + ".class, \"" + methodName + "\", ");
                parser.getFunctionsCode().append(processParams((String[]) signature.get(PARAMETERS)));
                parser.getFunctionsCode().append(");\r\n");
                parser.getFunctionsNames().put(functions[i], funcMapperName);
              }
            }
          }
        }
      }
    }
    return funcMapperName;
  }

  private static Hashtable parseSignature(String signature, String functionName) throws JspParseException {
    Hashtable h = new Hashtable();
    int start = signature.indexOf(' ');
    if (start < 0) {
      throw new JspParseException("Invalid signature of function "+functionName);
    }
    int end = signature.indexOf('(');
    if (end < 0) {
      throw new JspParseException("Invalid signature of function "+functionName);
    }
    h.put(METHOD_NAME, signature.substring(start + 1, end).trim());
    //params
    ArrayList params = new ArrayList();
    start = end + 1;
    boolean lastArg = false;
    while (true) {
      int p = signature.indexOf(',', start);
      if (p < 0) {
        p = signature.indexOf(')', start);
        if (p < 0) {
          throw new JspParseException("Invalid signature of function "+functionName);
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
    h.put(PARAMETERS , params.toArray(new String[params.size()]));
    return h;
  }

  private static StringBuffer processParams(String[] params) {
    StringBuffer sb = new StringBuffer("new Class[] {");
    for (int k = 0; k < params.length; k++) {
      if (k != 0) {
        sb.append(", ");
      }
      int iArray = params[k].indexOf('[');
      if (iArray < 0) {
        sb.append(params[k] + ".class");
      } else {
        String baseType = params[k].substring(0, iArray);
        sb.append("java.lang.reflect.Array.newInstance(");
        sb.append(baseType);
        sb.append(".class,");
        int aCount = 0;
        for (int jj = iArray; jj < params[k].length(); jj++) {
          if (params[k].charAt(jj) == '[') {
            aCount++;
          }
        }
        if (aCount == 1) {
          sb.append("0).getClass()");
        } else {
          sb.append("new int[" + aCount + "]).getClass()");
        }
      }
    }
    return sb.append("}");
  }

  /**
   * Used runtime to add map between function name and function static method from the given class.
   * @param qName prefix + ":" + localName
   * @param functionClass  The Class object containing function method.
   * @param methodName The method name of function
   * @param parameters Parameters for this method
   */
  public void map(String qName, final Class functionClass, final String methodName, final Class[] parameters) {
    Method m = null;
    try {
      m = functionClass.getDeclaredMethod(methodName, parameters);
    } catch (NoSuchMethodException e) {
    	//TODO:Polly Check this
    	//TODO:Polly type:trace
    	// if more readable message added -> log Error 
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000256",
              "Cannot find EL function implementation.", e, null, null);
    }
    map.put(qName, m);
  }

  /**
   * JSP.2.10.4
   * Locate the public static method with name and signature equal to the
   * value of the function-signature element. If any of these don�t exist, a translation-
   * time error shall occur.
   * @return true if is OK. otherwiese - throws exception.
   * @throws JspParseException
   */
  private static boolean checkFunction(String functionClassName, String methodName, String[] parameters, ClassLoader classLoader) throws JspParseException {
    Method m = null;
    try {
      Class functionClass = Class.forName(functionClassName, true, classLoader);
      Class[] params = convertParameters(parameters, classLoader);
      m = functionClass.getDeclaredMethod(methodName, params);
    } catch (NoSuchMethodException e) {
      throw new JspParseException(JspParseException.FUNCTION_NOT_FOUND, new Object[]{functionClassName, e.getMessage()});
    } catch (ClassNotFoundException e) {
       throw new JspParseException(JspParseException.FUNCTION_NOT_FOUND, new Object[]{functionClassName, e.getMessage()});
    }
    if (m != null && Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers())) {
      return true;
    } else {
      throw new JspParseException(JspParseException.FUNCTION_NOT_FOUND, new Object[]{functionClassName, " function's method is not public static!"});
    }
  }

  private static Class[] convertParameters(String[] parameters, ClassLoader classLoader) throws ClassNotFoundException {
    if (parameters == null) {
      return null;
    }
    Class[] result = new Class[parameters.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = forName(parameters[i], classLoader);
    }
    return result;
  }

  private static Class forName(String parameter, ClassLoader classLoader) throws ClassNotFoundException {
    if (parameter == null || parameter.equals("")){
      return null;
    }
    if (parameter.length() < 8) {
      for (int i = 0; i < PRIMITIVE_STRINGS.length; i++) {
        if (parameter.equals(PRIMITIVE_STRINGS[i])) {
          return PRIMITIVE_CLASSES[i];
        }
      }
    }
    int iArray = parameter.indexOf('[');
    if (iArray < 0) {
        return Class.forName(parameter, true, classLoader);
    } else {
        String baseType = parameter.substring(0, iArray);
        int aCount = 0;
        for (int jj = iArray; jj < parameter.length(); jj++) {
          if (parameter.charAt(jj) == '[') {
            aCount++;
          }
        }
        Class baseClass = Class.forName(baseType, true, classLoader);
        if (aCount == 1) {
          return Array.newInstance(baseClass, 0).getClass();
        } else {
          return Array.newInstance(baseClass, aCount).getClass();
        }
      }
  }

  private static boolean isReserved(String name) {
    if (name == null) {
      return false;
    }
    for (int i = 0; i < elReserved.length; i++) {
      if (name.equals(elReserved[i])) {
        return true;
      }
    }
    return false;
  }
}
