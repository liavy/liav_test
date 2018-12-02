package com.sap.engine.services.servlets_jsp.server.deploy.util.jacc;

import com.sap.engine.services.httpserver.lib.protocol.Methods;

import java.util.*;

public class UrlPatternConstraint {
  private static final String[] STANDARD_HTTP_METHODS_ARR = {Methods.GET, Methods.POST, Methods.PUT, 
      Methods.DELETE, Methods.OPTIONS, Methods.HEAD, Methods.TRACE};
  private UrlPattern urlPattern = null;
  private Hashtable httpMethods = null;

  public UrlPatternConstraint(String urlPattern) {
    init(urlPattern);
  }

  private void init(String urlPattern) {
    this.urlPattern = new UrlPattern(urlPattern);
    httpMethods = new Hashtable();
  }

  public UrlPattern getUrlPattern() {
    return urlPattern;
  }

  public void addHttpMethodToRoleMapping(String httpMethodName, String roleName) {
    HttpMethod httpMethod = (HttpMethod)httpMethods.get(httpMethodName);
    if (httpMethod == null) {
      httpMethod = new HttpMethod(httpMethodName);
      httpMethods.put(httpMethodName, httpMethod);
    }
    //todo - if there are two constraints for the same urlPattern-httpMethod couple -
    //one with role and one with no roles - what is the result - no roles?
    httpMethod.addSecurityRole(roleName);
  }

  public void addHttpMethod(String httpMethodName, boolean methodDenied) {
    HttpMethod httpMethod = (HttpMethod)httpMethods.get(httpMethodName);
    if (httpMethod == null) {
      httpMethod = new HttpMethod(httpMethodName);
      httpMethods.put(httpMethodName, httpMethod);
    }
    if (methodDenied) {
      httpMethod.setMethodDenied();
    }
  }

  public void setTransportGuarantee(String httpMethodName, String transportGuarantee) {
    HttpMethod httpMethod = (HttpMethod)httpMethods.get(httpMethodName);
    if (httpMethod == null) {
      httpMethod = new HttpMethod(httpMethodName);
      httpMethods.put(httpMethodName, httpMethod);
    }
    httpMethod.setTransportGuarantee(transportGuarantee);
  }

  public HttpMethod[] getAllExcludedMethods() {
    Vector allExcludedMethods = new Vector();
    Enumeration en = httpMethods.elements();
    while (en.hasMoreElements()) {
      HttpMethod nextMethod = (HttpMethod)en.nextElement();
      if (nextMethod.isMethodDenied()) {
        allExcludedMethods.add(nextMethod);
      }
    }
    if (allExcludedMethods.isEmpty()) {
      return null;
    }
    HttpMethod[] result = new HttpMethod[allExcludedMethods.size()];
    en = allExcludedMethods.elements();
    int i = 0;
    while (en.hasMoreElements()) {
      result[i] = (HttpMethod)en.nextElement();
      i++;
    }
    return result;
  }

  public HttpMethod[] getAllMethods(String role) {
    Vector allExcludedMethods = new Vector();
    Enumeration en = httpMethods.elements();
    while (en.hasMoreElements()) {
      HttpMethod nextMethod = (HttpMethod)en.nextElement();
      if (!nextMethod.isMethodDenied() && nextMethod.containsRole(role)) {
        allExcludedMethods.add(nextMethod);
      }
    }
    if (allExcludedMethods.isEmpty()) {
      return null;
    }
    HttpMethod[] result = new HttpMethod[allExcludedMethods.size()];
    en = allExcludedMethods.elements();
    int i = 0;
    while (en.hasMoreElements()) {
      result[i] = (HttpMethod)en.nextElement();
      i++;
    }
    return result;
  }
  
  
  /**
   * Filters all HttpMethods which are either excluded/denied or unchecked with set transport guarantee (INTEGRAL or CONFIDENTIAL) 
   * @return 
   */
  public List<HttpMethod> getHttpMethodsDiffThanUncheckedNone() {
    List<HttpMethod> result = new LinkedList<HttpMethod>();
    Enumeration en = httpMethods.elements();
    while (en.hasMoreElements()) {
      HttpMethod nextMethod = (HttpMethod)en.nextElement();
      if (nextMethod.isMethodDenied() || !nextMethod.isNoneTransportGuarantee()) {
        result.add(nextMethod);
      }
    }
    if (result.isEmpty()) {
      return null;
    } else {
      return result;
    }
  } // getHttpMethodsDiffThanUncheckedNone()
  
  
  /**
   * Filters all HttpMethods suitable for remaining Unchecked-TrGuaranteeNone WebResourcePermission. I.e. get all methods:
   * - excluded/denied 
   * - with set role ResourcePermission
   * - unchecked with integral or confidential rule
   *  
   * @return List of HttpMethod elements 
   */
  public List<HttpMethod> getHttpMethodsForUncheckedNoneResPermExceptionList() {
    List<HttpMethod> result = new LinkedList<HttpMethod>();
    Enumeration en = httpMethods.elements();
    while (en.hasMoreElements()) {
      HttpMethod nextMethod = (HttpMethod)en.nextElement();
      if (nextMethod.isMethodDenied() || nextMethod.hasAnyRole() || !nextMethod.isNoneTransportGuarantee()) {
        result.add(nextMethod);
      }
    }
    if (result.isEmpty()) {
      return null;
    } else {
      return result;
    }
  } // getHttpMethodsForUncheckedNoneResPermExceptionList()

  public HttpMethod[] getAllNonStarndardMethods() {
    Vector allNonStarndardMethods = new Vector();
    Enumeration en = httpMethods.elements();
    while (en.hasMoreElements()) {
      HttpMethod nextMethod = (HttpMethod)en.nextElement();
      if (!nextMethod.isStandard()) {
        allNonStarndardMethods.add(nextMethod);
      }
    }
    if (allNonStarndardMethods.isEmpty()) {
      return null;
    }
    HttpMethod[] result = new HttpMethod[allNonStarndardMethods.size()];
    en = allNonStarndardMethods.elements();
    int i = 0;
    while (en.hasMoreElements()) {
      result[i] = (HttpMethod)en.nextElement();
      i++;
    }
    return result;
  }

  /**
   * Collects methods for unchecked permissions.
   * @param mentionedMtdsInUnchRules       For returning only mentioned (in web.xml) methods in unchecked rules
   * @param mentionedMtdsInExclOrRoleRules  For returning methods in exclude or role rules. They are use to generate 
   *          unchecked exception list (meaning allowed for all but these) 
   * 
   * 
//sss   * @param nonStdUsedMethods - for result. List of HttpMethod elements in either excluded or role permission. 
//sss   *        Used to generate negative permission - unchecked are all methods except these
//sss   * @return Only standard HTTP methods that are for unchecked rules
   */
//sss  public HttpMethod[] getAllUncheckedMethods(List nonStdUsedMethods) {
  void getAllUncheckedMethods(List<HttpMethod> mentionedMtdsInUnchRules, List<HttpMethod> mentionedMtdsInExclOrRoleRules) {
    if (mentionedMtdsInUnchRules == null) {
      throw new IllegalArgumentException("Passed null for 1st argument!");
    } else {
      mentionedMtdsInUnchRules.clear();
    }
    if (mentionedMtdsInExclOrRoleRules == null) {
      throw new IllegalArgumentException("Passed null for 2nd argument!");
    } else {
      mentionedMtdsInExclOrRoleRules.clear();
    }

// TODOsss - remove    
//    Vector usedMethods = new Vector();
//    Hashtable uncheckedMethods = new Hashtable();
    HttpMethod nextMethod;
    Enumeration en = httpMethods.elements();
    while (en.hasMoreElements()) { // 1st find explicitly defined unchecked methods (not excluded or in role)
      nextMethod = (HttpMethod)en.nextElement();
      if (nextMethod.isMethodDenied() || (nextMethod.getAllRoles() != null && nextMethod.getAllRoles().length != 0)) {
        //usedMethods.add(nextMethod.getMethodName());
        mentionedMtdsInExclOrRoleRules.add(nextMethod);
      } else {
        //uncheckedMethods.put(nextMethod.getMethodName(), nextMethod);
        mentionedMtdsInUnchRules.add(nextMethod);
      }
    }
// TODOsss - remove    
//    List usedNonStdMethodsList = new LinkedList();
//    findUncheckedMethods(usedMethods, uncheckedMethods, usedNonStdMethodsList); // gets non-mentioned standard methods - also unchecked  
//
//    // non-standard methods
//    if (nonStdUsedMethods != null) {
//      nonStdUsedMethods.clear();
//    }
//    if (usedNonStdMethodsList != null && usedNonStdMethodsList.size() != 0) {
//      Iterator iterator = usedNonStdMethodsList.iterator();
//      while (iterator.hasNext()) {
//        nonStdUsedMethods.add(new HttpMethod((String) iterator.next()));
//      }
//    }
//    
//    // standard methods
//    if (uncheckedMethods.isEmpty()) {
//      return null;
//    }
//    HttpMethod[] result = new HttpMethod[uncheckedMethods.size()];
//    en = uncheckedMethods.elements();
//    int i = 0;
//    while (en.hasMoreElements()) {
//      result[i] = (HttpMethod)en.nextElement();
//      i++;
//    }
//    return result;
  }

  public HttpMethod getHttpMethod(String methodName) {
    Enumeration en = httpMethods.elements();
    while (en.hasMoreElements()) {
      HttpMethod nextMethod = (HttpMethod)en.nextElement();
      if (nextMethod.getMethodName().equals(methodName)) {
        return nextMethod;
      }
    }
    return null;
  }

  public String[] getAllRoles() {
    HashSet allRoles = new HashSet();
    Enumeration en = httpMethods.elements();
    while (en.hasMoreElements()) {
      HttpMethod nextMethod = (HttpMethod)en.nextElement();
      String[] nextMethodRoles = nextMethod.getAllRoles();
      for (int i = 0; nextMethodRoles != null && i < nextMethodRoles.length; i++) {
        allRoles.add(nextMethodRoles[i]);
      }
    }

    if (allRoles.isEmpty()) {
      return null;
    }
    String[] result = new String[allRoles.size()];
    Iterator it = allRoles.iterator();
    int i = 0;
    while (it.hasNext()) {
      result[i++] = (String)it.next();
    }
    return result;
  }

  /**
   * Extracts from <code>usedMethods</code> standard unchecked HTTP methods and on the other hand used non-standard ones
   * @param usedMethods      Source Vector to extract/filter methods from 
   * @param uncheckedMethods Where to store the standard ones extracted.
   * @param usedNonStdMethods result container for non-standard methods in used methods (excluded or role)
   * @return Standard HTTP methods
   */
  private Hashtable findUncheckedMethods(Vector usedMethods, Hashtable uncheckedMethods, List usedNonStdMethods) {
    usedNonStdMethods.addAll(usedMethods); // copy all used methods and then remove just standard ones 
  
    for (int i = 0; i < STANDARD_HTTP_METHODS_ARR.length; i++) {
      String currentStdMethod = STANDARD_HTTP_METHODS_ARR[i];
      if (!usedMethods.contains(currentStdMethod)) {
        if ( !uncheckedMethods.containsKey(currentStdMethod)) {
          uncheckedMethods.put(currentStdMethod, new HttpMethod(currentStdMethod));
        }
      } else { // exists in used methods - remove it from usedNonStdMethods
        usedNonStdMethods.remove(currentStdMethod);
      }
    }
    
    return uncheckedMethods;
  }

  boolean hasMethodsDefined() {
    return httpMethods.size() != 0;
  }
}
